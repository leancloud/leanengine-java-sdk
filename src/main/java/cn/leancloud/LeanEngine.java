package cn.leancloud;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Hex;

import com.avos.avoscloud.internal.InternalConfigurationController;
import com.avos.avoscloud.internal.impl.EnginePersistenceImplementation;
import com.avos.avoscloud.internal.impl.JavaRequestSignImplementation;


public class LeanEngine {

  static volatile boolean httpsRedirectionEnabled = false;

  static EngineAppConfiguration appConf;

  static final String JSON_CONTENT_TYPE = "application/json; charset=UTF-8";

  /**
   * <p>
   * Authenticates this client as belonging to your application. This must be called before your
   * application can use the AVOSCloud library. The recommended way is to put a call to
   * LeanEngine.initialize in each of your onCreate methods. An example:
   * </p>
   * 
   * <pre>
   * 
   * @param applicationId The application id provided in the AVOSCloud dashboard.
   * @param clientKey The client key provided in the AVOSCloud dashboard.
   * @param masterKey The master key provided in the AVOSCloud dashboard.
   */
  public static void initialize(String applicationId, String clientKey, String masterKey) {
    appConf = new EngineAppConfiguration(applicationId, clientKey, masterKey);
    InternalConfigurationController confController =
        InternalConfigurationController.globalInstance();
    confController.setInternalPersistence(EnginePersistenceImplementation.instance());
    confController.setAppConfiguration(appConf);
    confController.setInternalRequestSign(JavaRequestSignImplementation.instance());
  }

  private static Map<String, EngineHandlerInfo> funcs = new HashMap<String, EngineHandlerInfo>();

  private static EngineSessionCookie sessionCookie;

  /**
   * 请在ServletContextListener.contextInitialized中注册所有的云函数定义类
   * 
   * @param clazz
   */
  public static void register(Class<?> clazz) {
    for (Method m : clazz.getDeclaredMethods()) {
      EngineFunction func = m.getAnnotation(EngineFunction.class);
      if (func != null) {
        EngineHandlerInfo info = EngineHandlerInfo.getEngineHandlerInfo(m, func);
        if (info != null) {

          funcs.put(info.getEndPoint(), info);
        }
        continue;
      }
      EngineHook hook = m.getAnnotation(EngineHook.class);
      if (hook != null) {
        EngineHandlerInfo info = EngineHandlerInfo.getEngineHandlerInfo(m, hook);
        if (info != null) {
          funcs.put(info.getEndPoint(), info);
        }
      }

      IMHook imHook = m.getAnnotation(IMHook.class);
      if (imHook != null) {
        EngineHandlerInfo info = EngineHandlerInfo.getEngineHandlerInfo(m, imHook);
        if (info != null) {
          funcs.put(info.getEndPoint(), info);
        }
      }
    }
  }

  static EngineHandlerInfo getHandler(String key) {
    return funcs.get(key);
  }

  /**
   * 设置sessionCookie的实例
   * 
   * @param sessionCookie
   */

  public static void addSessionCookie(EngineSessionCookie sessionCookie) {
    LeanEngine.sessionCookie = sessionCookie;
  }

  public static EngineSessionCookie getSessionCookie() {
    return sessionCookie;
  }

  /**
   * 本方法用于本地调试期间，设置为 true 后所有的云函数调用都直接调用本地而非 LeanCloud 上已经部署的项目
   * 
   * @param enabled
   */
  public static void setLocalEngineCallEnabled(boolean enabled) {
    appConf.setLocalEngineCallEnabled(enabled);
  }

  /**
   * 设置是否打开 https 自动跳转
   * 
   * @param enabled
   */
  public static void setHttpsRedirectEnabled(boolean enabled) {
    httpsRedirectionEnabled = enabled;
  }

  public static String hmacSha1(String value, String key) {
    try {
      byte[] keyBytes = key.getBytes();
      SecretKeySpec signingKey = new SecretKeySpec(keyBytes, "HmacSHA1");
      Mac mac = Mac.getInstance("HmacSHA1");
      mac.init(signingKey);
      byte[] rawHmac = mac.doFinal(value.getBytes());
      byte[] hexBytes = new Hex().encode(rawHmac);
      return new String(hexBytes, "UTF-8");
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  protected static Set<String> getMetaData() {
    return funcs.keySet();
  }

  public static String getAppId() {
    return appConf.getApplicationId();
  }

  public static String getAppKey() {
    return appConf.getClientKey();
  }

  public static String getMasterKey() {
    return appConf.getMasterKey();
  }

  public static String getAppEnv() {
    return appConf.getAppEnv();
  }

}
