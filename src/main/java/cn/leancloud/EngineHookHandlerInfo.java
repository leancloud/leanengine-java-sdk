package cn.leancloud;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.AVUtils;

public class EngineHookHandlerInfo extends EngineHandlerInfo {

  public EngineHookHandlerInfo(String endpoint, Method handlerMethod,
      List<EngineFunctionParamInfo> params, Class returnType, String hookClass) {
    super(endpoint, handlerMethod, params, returnType, hookClass);
  }

  @Override
  public Object parseParams(String requestBody) throws InvalidParameterException {
    Map<String, Object> hookParams = JSON.parseObject(requestBody, Map.class);
    AVObject param = null;
    EngineFunctionParamInfo paramInfo = methodParameterList.get(0);
    if (AVUser.class.isAssignableFrom(paramInfo.type)) {
      param = new AVUser();
    } else {
      param = new AVObject(hookClass);
    }
    EngineRequestContext.parseMetaData((Map<String, Object>) hookParams.get(paramInfo.getName()));
    AVUtils.copyPropertiesFromMapToAVObject(
        (Map<String, Object>) hookParams.get(paramInfo.getName()), param);
    return param;
  }

  @Override
  public Object wrapperResponse(Object result, String requestBody, boolean rpcCall) {
    Map<String, Object> hookParams = new HashMap<String, Object>();
    Map<String, Object> objectMapping =
        (Map<String, Object>) AVUtils.getParsedObject(result, true, true);
    objectMapping.remove("__type");
    objectMapping.remove("className");
    hookParams.putAll(objectMapping);
    return hookParams;
  }
}
