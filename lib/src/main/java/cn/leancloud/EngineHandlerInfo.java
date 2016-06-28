package cn.leancloud;

import java.io.BufferedReader;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.alibaba.fastjson.JSON;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.AVUtils;
import com.avos.avoscloud.LogUtil;

public class EngineHandlerInfo {

  private static final String OBJECT = "object";
  private static final String USER = "user";

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

  public Object execute(HttpServletRequest request) throws InvalidParameterException, IOException,
      IllegalAccessException, InvocationTargetException {
    StringBuilder sb = new StringBuilder();
    String line = null;
    BufferedReader reader = request.getReader();
    while ((line = reader.readLine()) != null) {
      sb.append(line);
    }
    String requestBody = sb.toString();
    Object returnValue = null;
    try {
      switch (methodParameterList.size()) {
        case 0:
          returnValue = handlerMethod.invoke(null);
          break;
        case 1:
          Object param = null;
          // 不要想了这个地方肯定就是EngineHook,要么是user,要么是object
          try {
            if (!AVUtils.isBlankString(this.hookClass)) {
              Map<String, Object> hookParams = JSON.parseObject(requestBody, Map.class);

              String paramKey = null;
              if (hookParams.containsKey(USER) && hookParams.get(USER) != null) {
                param = new AVUser();
                paramKey = USER;
              } else if (hookParams.containsKey(OBJECT) && hookParams.get(OBJECT) != null) {
                param = new AVObject(hookClass);
                paramKey = OBJECT;
              } else {
                throw new InvalidParameterException();
              }
              EngineRequestContext.parseMetaData((Map<String, Object>) hookParams.get(paramKey));
              AVUtils.copyPropertiesFromMapToAVObject(
                  (Map<String, Object>) hookParams.get(paramKey), (AVObject) param);
              returnValue = handlerMethod.invoke(null, param);
            } else {
              EngineFunctionParamInfo paramInfo = methodParameterList.get(0);
              param = paramInfo.parseParams(requestBody);
            }
            // 这里是假设唯一的目标作为参数传递过来，而不是放在一个jsonObject中间
            returnValue = handlerMethod.invoke(null, param);
            break;
          } catch (Exception e) {
            // 如果解析出错了，就认为传递过来的是一个jsonObject可以按照多于1个参数的情况来解析
          }
        default:
          Map jsonParams = JSON.parseObject(requestBody, Map.class);
          Object[] params = new Object[methodParameterList.size()];
          for (int index = 0; index < methodParameterList.size(); index++) {
            Object p = jsonParams.get(methodParameterList.get(index).name);
            params[index] = methodParameterList.get(index).parseParams(JSON.toJSONString(p));
          }
          returnValue = handlerMethod.invoke(null, params);
      }
    } catch (IllegalArgumentException e) {
      throw new InvalidParameterException();
    }
    return returnValue;
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
    return new EngineHandlerInfo(functionName, method, params, method.getReturnType());
  }

  public static EngineHandlerInfo getEngineHandlerInfo(Method method, EngineHook hook) {
    List<EngineFunctionParamInfo> params = new LinkedList<EngineFunctionParamInfo>();
    if ("_User".equals(hook.className()) || hook.type() == EngineHookType.onLogin
        || hook.type() == EngineHookType.onVerified) {
      params.add(new EngineFunctionParamInfo(AVUser.class, USER));
    } else {
      params.add(new EngineFunctionParamInfo(AVObject.class, OBJECT));
    }
    return new EngineHandlerInfo(EndpointParser.getInternalEndpoint(hook.className(), hook.type()),
        method, params, null, hook.className());
  }

  public static EngineHandlerInfo getEngineHandlerInfo(Method method, IMHook hook) {
    List<EngineFunctionParamInfo> params = new LinkedList<EngineFunctionParamInfo>();
    params.add(new EngineFunctionParamInfo(Map.class, OBJECT));
    return new EngineHandlerInfo(hook.type().toString(), method, params, Map.class);
  }
}
