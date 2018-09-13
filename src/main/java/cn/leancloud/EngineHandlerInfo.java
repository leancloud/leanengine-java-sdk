package cn.leancloud;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.AVUtils;
import com.avos.avoscloud.LogUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public abstract class EngineHandlerInfo {

  private static final Logger logger = LogManager.getLogger(EngineHandlerInfo.class);

  static final String OBJECT = "object";

  public EngineHandlerInfo(String endpoint, Method handlerMethod,
                           List<EngineFunctionParamInfo> params, Class<?> returnType, String hookKey) {
    this(endpoint, handlerMethod, params, returnType, hookKey, null);
  }

  public EngineHandlerInfo(String endpoint, Method handlerMethod,
                           List<EngineFunctionParamInfo> params, Class<?> returnType, String hookKey, String hookClass) {
    this.handlerMethod = handlerMethod;
    this.endPoint = endpoint;
    this.methodParameterList = params;
    this.returnType = returnType;
    this.hookKey = hookKey;
    this.hookClass = hookClass;
  }

  final Method handlerMethod;
  final String endPoint;
  final List<EngineFunctionParamInfo> methodParameterList;
  final Class<?> returnType;
  final String hookKey;
  final String hookClass;

  public String getEndPoint() {
    return endPoint;
  }

  public Object execute(HttpServletRequest request, boolean rpcCall) throws Exception {
    if (hookKey != null && !hookKey.equals(AuthFilter.getAuthInfo(request).hookKey)) {
      throw new UnauthException();
    }

    StringBuilder sb = new StringBuilder();
    String line = null;
    BufferedReader reader = request.getReader();
    while ((line = reader.readLine()) != null) {
      sb.append(line);
    }
    String requestBody = sb.toString();
    logger.debug("request body: {}", requestBody);
    Object returnValue = null;
    Object params = this.parseParams(requestBody);
    returnValue =
        methodParameterList.size() == 0 ? handlerMethod.invoke(null)
            : params.getClass().isArray() ? handlerMethod.invoke(null, (Object[]) params)
            : handlerMethod.invoke(null, params);
    returnValue = this.wrapperResponse(returnValue, requestBody, rpcCall);
    return returnValue;
  }

  public abstract Object parseParams(String requestBody);

  public Object wrapperResponse(Object returnValue, String requestBody, boolean rpcCall) {
    JSONObject result = new JSONObject();
    result.put("result", AVUtils.getParsedObject(returnValue, true, false, true, true, true));
    if (!rpcCall) {
      return JSON.parse(ResponseUtil.filterResponse(result.toJSONString()));
    }
    return result;
  }

  public static EngineHandlerInfo getEngineHandlerInfo(Method method, EngineFunction function) {
    String functionName =
        AVUtils.isBlankString(function.value()) ? method.getName() : function.value();
    List<EngineFunctionParamInfo> params = new LinkedList<EngineFunctionParamInfo>();
    Annotation[][] annotationMatrix = method.getParameterAnnotations();
    Class<?>[] paramTypesArray = method.getParameterTypes();
    for (int index = 0; index < paramTypesArray.length; index++) {
      Annotation[] array = annotationMatrix[index];
      if (array.length == 0) {
        LogUtil.avlog.e("Parameters not annotated correctly for EngineFunction:" + functionName);
      } else {
        for (Annotation an : array) {
          if (an instanceof EngineFunctionParam) {
            params.add(new EngineFunctionParamInfo(paramTypesArray[index], ((EngineFunctionParam) an)
                .value()));
          }
        }
      }
    }
    return new EngineFunctionHandlerInfo(functionName, method, params, method.getReturnType());
  }

  public static EngineHandlerInfo getEngineHandlerInfo(Method method, EngineHook hook, String hookKey) {
    List<EngineFunctionParamInfo> params = new LinkedList<EngineFunctionParamInfo>();
    params.add(new EngineFunctionParamInfo("_User".equals(hook.className()) ? AVUser.class
        : AVObject.class, OBJECT));
    if (EngineHookType.beforeUpdate.equals(hook.type())) {
      return new BeforeUpdateHookHandlerInfo(EndpointParser.getInternalEndpoint(hook.className(),
          hook.type()), method, params, null, hookKey, hook.className());
    }
    return new EngineHookHandlerInfo(EndpointParser.getInternalEndpoint(hook.className(),
        hook.type()), method, params, null, hookKey, hook.className());
  }

  public static EngineHandlerInfo getEngineHandlerInfo(Method method, IMHook hook, String hookKey) {
    List<EngineFunctionParamInfo> params = new LinkedList<EngineFunctionParamInfo>();
    params.add(new EngineFunctionParamInfo(Map.class, OBJECT));
    return new IMHookHandlerInfo(hook.type().toString(), method, params, Map.class, hookKey);
  }
}
