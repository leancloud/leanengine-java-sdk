package cn.leancloud;

import com.avos.avoscloud.internal.InternalConfigurationController;
import com.avos.avoscloud.internal.InternalConfigurationController.Builder;
import com.avos.avoscloud.internal.impl.EnvAppRouter;
import com.avos.avoscloud.internal.impl.JavaRequestSignImplementation;
import com.avos.avoscloud.internal.impl.Log4j2Implementation;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.FilterMapping;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


public class LeanEngine {

  static final String JSON_CONTENT_TYPE = "application/json; charset=UTF-8";

  private EngineAppConfiguration appConf;
  private EnvAppRouter appRouter;
  private Map<String, EngineHandlerInfo> funcs = new HashMap<>();
  private Server server;

  /**
   * 使用默认参数初始化 Leanngine 对象，applicationId，appKey，masterKey 将从环境变量获取。
   */
  public LeanEngine() {
    this(new EngineAppConfiguration());
  }

  /**
   * 初始eanngine 对象。
   *
   * @param appId     从 LeanCloud 控制台获取 appId。
   * @param appKey    从 LeanCloud 控制台获取 appKey。
   * @param masterKey 从 LeanCloud 控制台获取 masterKey。
   * @param hookKey   从云引擎环境变量 LEANCLOUD_APP_HOOK_KEY 中获取。
   * @param port      云函数服务监听的端口。
   * @param appEnv    运行环境，development 为开发环境；production 为生产环境。
   */
  public LeanEngine(String appId, String appKey, String masterKey, String hookKey, int port, String appEnv) {
    this(new EngineAppConfiguration(appId, appKey, masterKey, hookKey, port, appEnv));
  }

  private LeanEngine(EngineAppConfiguration appConf) {
    this.appConf = appConf;
    appRouter = new EnvAppRouter(appConf);
    Builder builder = new Builder();
    builder.setInternalPersistence(new EnginePersistence())
        .setInternalLogger(Log4j2Implementation.instance()).setAppConfiguration(appConf)
        .setInternalRequestSign(JavaRequestSignImplementation.instance())
        .setAppRouter(appRouter).build();
    InternalConfigurationController.globalInstance().getAppRouter().updateServerHosts();
  }

  /**
   * 批量注册云函数或 hook 函数的 class，这些 class 中带有云函数和 hook 函数注解的方法将会在服务启动后生效。
   *
   * @param clazzs 有云函数方法的 class 数组
   * @return self
   */
  public LeanEngine register(Class[] clazzs) {
    for (Class c : clazzs) {
      register(c);
    }
    return this;
  }

  /**
   * 注册云函数或 hook 函数的 class，这些 class 中带有云函数和 hook 函数注解的方法将会在服务启动后生效。
   *
   * @param clazz 有云函数方法的 class
   * @return self
   */
  public LeanEngine register(Class<?> clazz) {
    for (Method m : clazz.getDeclaredMethods()) {
      EngineFunction func = m.getAnnotation(EngineFunction.class);
      if (func != null) {
        EngineHandlerInfo info = EngineHandlerInfo.getEngineHandlerInfo(m, func);
        funcs.put(info.getEndPoint(), info);
        continue;
      }
      EngineHook hook = m.getAnnotation(EngineHook.class);
      if (hook != null) {
        EngineHandlerInfo info = EngineHandlerInfo.getEngineHandlerInfo(m, hook, appConf.getHookKey());
        funcs.put(info.getEndPoint(), info);
      }

      IMHook imHook = m.getAnnotation(IMHook.class);
      if (imHook != null) {
        EngineHandlerInfo info = EngineHandlerInfo.getEngineHandlerInfo(m, imHook, appConf.getHookKey());
        funcs.put(info.getEndPoint(), info);
      }
    }
    return this;
  }

  public LeanEngine setPort(int port) {
    appConf.setPort(port);
    return this;
  }

  /**
   * 开始监听端口并提供云函数服务。请在所有配置设置完成，所有云函数 class 都注册之后调用该方法。
   *
   * @return self
   */
  public LeanEngine start() throws Exception {
    FilterHolder authFilterHolder = new FilterHolder(new AuthFilter(this));
    ServletHolder cloudCodeHolder = new ServletHolder(new CloudCodeServlet(this));
    ServletHolder metadataHolder = new ServletHolder(new LeanEngineMetadataServlet(this));

    ServletHandler handler = new ServletHandler();
    handler.addServletWithMapping(LeanEngineHealthCheckServlet.class, "/__engine/1/ping");

    for (String version : new String[]{"1", "1.1"}) {
      String rootPath = String.format("/%s", version);
      handler.addFilterWithMapping(CorsFilter.class, rootPath + "/*", FilterMapping.DEFAULT);
      handler.addFilterWithMapping(authFilterHolder, rootPath + "/*", FilterMapping.DEFAULT);
      handler.addFilterWithMapping(CurrentUserFilter.class, rootPath + "/*", FilterMapping.DEFAULT);
      handler.addServletWithMapping(cloudCodeHolder, rootPath + "/functions/*");
      handler.addServletWithMapping(cloudCodeHolder, rootPath + "/call/*");
      handler.addServletWithMapping(metadataHolder, rootPath + "/functions/_ops/metadatas");
    }

    server = new Server(appConf.getPort());
    server.setHandler(handler);
    server.start();
    return this;
  }

  public void stop() throws Exception {
    server.stop();
  }

  EngineHandlerInfo getHandler(String key) {
    return funcs.get(key);
  }

  /**
   * 本方法用于本地调试期间，设置为 true 后所有的云函数调用都直接调用本地而非 LeanCloud 上已经部署的项目
   *
   * @param enabled true 为调用本地云函数; false 为调用服务端云函数
   * @return self
   */
  public LeanEngine setLocalEngineCallEnabled(boolean enabled) {
    appRouter.setLocalEngineCallEnabled(enabled);
    InternalConfigurationController.globalInstance().getAppRouter().updateServerHosts();
    return this;
  }

  /**
   * 设置在与 LeanCloud 服务器进行沟通的时候是否使用 masterKey
   * <p>
   * 使用 masterKey 时， API 将拥有全部权限，不再受到权限的限制
   *
   * @param useMasterKey true 为使用 masterKey 发送请求
   * @return self
   */
  public LeanEngine setUseMasterKey(boolean useMasterKey) {
    JavaRequestSignImplementation.instance().setUseMasterKey(useMasterKey);
    return this;
  }

  protected Set<String> getMetaData() {
    return funcs.keySet();
  }

  public String getAppId() {
    return appConf.getApplicationId();
  }

  public String getAppKey() {
    return appConf.getClientKey();
  }

  public String getMasterKey() {
    return appConf.getMasterKey();
  }

  public String getAppEnv() {
    return appConf.getAppEnv();
  }

  /**
   * 可以通过改方法明确设置 SDK 调用北美节点服务，否则将根据 appId 自动判断。
   *
   * @return self
   */
  public LeanEngine useAVCloudUS() {
    appConf.setIsCN(false);
    return this;
  }

  /**
   * 可以通过改方法明确设置 SDK 调用国内节点服务，否则将根据 appId 自动判断。
   *
   * @return self
   */
  public LeanEngine useAVCloudCN() {
    appConf.setIsCN(true);
    return this;
  }

}
