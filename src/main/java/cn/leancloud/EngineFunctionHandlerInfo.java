package cn.leancloud;

import com.alibaba.fastjson.JSON;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

public class EngineFunctionHandlerInfo extends EngineHandlerInfo {

  public EngineFunctionHandlerInfo(String endpoint, Method handlerMethod,
                                   List<EngineFunctionParamInfo> params, Class returnType) {
    super(endpoint, handlerMethod, params, returnType, null);
  }

  @Override
  public Object parseParams(String requestBody) {
    if (methodParameterList.size() == 0) {
      return null;
    } else {
      Map jsonParams = JSON.parseObject(requestBody, Map.class);
      Object[] params = new Object[methodParameterList.size()];
      for (int index = 0; index < methodParameterList.size(); index++) {
        Object p = jsonParams.get(methodParameterList.get(index).name);
        if (p == null) {
          params[index] = null;
        } else {
          params[index] = methodParameterList.get(index).parseParams(JSON.toJSONString(p));
        }
      }
      return params;
    }
  }
}
