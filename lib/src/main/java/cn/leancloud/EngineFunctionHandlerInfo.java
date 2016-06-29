package cn.leancloud;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;

public class EngineFunctionHandlerInfo extends EngineHandlerInfo {

  public EngineFunctionHandlerInfo(String endpoint, Method handlerMethod,
      List<EngineFunctionParamInfo> params, Class returnType) {
    super(endpoint, handlerMethod, params, returnType);
  }

  @Override
  public Object parseParams(String requestBody) {
    switch (methodParameterList.size()) {
      case 0:
        return null;
      case 1:
        Object param = null;
        try {
          EngineFunctionParamInfo paramInfo = methodParameterList.get(0);
          param = paramInfo.parseParams(requestBody);
          // 这里是假设唯一的目标作为参数传递过来，而不是放在一个jsonObject中间
          return param;
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
        return params;
    }
  }
}
