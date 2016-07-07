package cn.leancloud;

import java.io.BufferedReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.AVUtils;
import com.avos.avoscloud.LogUtil;

public abstract class EngineHandlerInfo {

  static final String OBJECT = "object";
  static final String USER = "user";

  public EngineHandlerInfo(String endpoint, Method handlerMethod,
      List<EngineFunctionParamInfo> params, Class returnType) {
    this(endpoint, handlerMethod, params, returnType, null);
  }

  public EngineHandlerInfo(String endpoint, Method handlerMethod,
      List<EngineFunctionParamInfo> params, Class returnType, String hookClass) {
    this.handlerMethod = handlerMethod;
    this.endPoint = endpoint;
    this.methodParameterList = params;
    this.returnType = returnType;
    this.hookClass = hookClass;
  }

  final Method handlerMethod;
  final String endPoint;
  final List<EngineFunctionParamInfo> methodParameterList;
  final Class returnType;
  final String hookClass;

  public Method getHandlerMethod() {
    return handlerMethod;
  }

  public String getEndPoint() {
    return endPoint;
  }

  public List<EngineFunctionParamInfo> getParamList() {
    return methodParameterList;
  }

  public Object execute(HttpServletRequest request, boolean rpcCall) throws Exception {
    StringBuilder sb = new StringBuilder();
    String line = null;
    BufferedReader reader = request.getReader();
    while ((line = reader.readLine()) != null) {
      sb.append(line);
    }
    String requestBody = sb.toString();
    Object returnValue = null;
    Object params = this.parseParams(requestBody);
    returnValue =
        methodParameterList.size() == 0 ? handlerMethod.invoke(null)
            : params.getClass().isArray() ? handlerMethod.invoke(null, (Object[]) params)
                : handlerMethod.invoke(null, params);
    if (returnValue != null) {
      returnValue = this.wrapperResponse(returnValue, requestBody, rpcCall);
    }
    return returnValue;
  }

  public abstract Object parseParams(String requestBody) throws InvalidParameterException;

  public Object wrapperResponse(Object returnValue, String requestBody, boolean rpcCall) {
    JSONObject result = new JSONObject();
    result.put("result", AVUtils.getParsedObject(returnValue, true, false, true));
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
    Class[] paramTypesArray = method.getParameterTypes();
    if (annotationMatrix.length != paramTypesArray.length) {
      LogUtil.avlog.e("Parameters not annotated correctly for EngineFunction:" + functionName);
    }
    for (int index = 0; index < paramTypesArray.length; index++) {
      Annotation[] array = annotationMatrix[index];
      for (Annotation an : array) {
        if (an instanceof EngineFunctionParam) {
          params.add(new EngineFunctionParamInfo(paramTypesArray[index], ((EngineFunctionParam) an)
              .value()));
        }
      }
    }
    return new EngineFunctionHandlerInfo(functionName, method, params, method.getReturnType());
  }

  public static EngineHandlerInfo getEngineHandlerInfo(Method method, EngineHook hook) {
    List<EngineFunctionParamInfo> params = new LinkedList<EngineFunctionParamInfo>();
    if ("_User".equals(hook.className()) || hook.type() == EngineHookType.onLogin
        || hook.type() == EngineHookType.onVerified) {
      params.add(new EngineFunctionParamInfo(AVUser.class, USER));
    } else {
      params.add(new EngineFunctionParamInfo(AVObject.class, OBJECT));
    }
    return new EngineHookHandlerInfo(EndpointParser.getInternalEndpoint(hook.className(),
        hook.type()), method, params, null, hook.className());
  }

  public static EngineHandlerInfo getEngineHandlerInfo(Method method, IMHook hook) {
    List<EngineFunctionParamInfo> params = new LinkedList<EngineFunctionParamInfo>();
    params.add(new EngineFunctionParamInfo(Map.class, OBJECT));
    return new IMHookHandlerInfo(hook.type().toString(), method, params, Map.class);
  }
}
