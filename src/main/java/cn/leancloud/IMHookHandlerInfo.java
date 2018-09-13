package cn.leancloud;

import com.alibaba.fastjson.JSON;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

class IMHookHandlerInfo extends EngineHandlerInfo {

  public IMHookHandlerInfo(String endpoint, Method handlerMethod,
                           List<EngineFunctionParamInfo> params, Class returnType, String hookKey) {
    super(endpoint, handlerMethod, params, returnType, hookKey);
  }

  @Override
  public Object parseParams(String requestBody) {
    return JSON.parseObject(requestBody, Map.class);
  }
}
