package org.example;

import static org.junit.Assert.*;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.AVUtils;
import com.avos.avoscloud.AsyncHttpResponseHandler;
import com.avos.avoscloud.GenericObjectCallback;
import com.avos.avoscloud.PaasClient.AVHttpClient;
import com.avos.avoscloud.internal.InternalConfigurationController;
import com.avos.avoscloud.okhttp.Request;
import com.avos.avoscloud.okhttp.RequestBody;

import cn.leancloud.EngineFunction;
import cn.leancloud.EngineFunctionParam;
import cn.leancloud.EngineFunctionParamInfo;
import cn.leancloud.EngineHandlerInfo;
import cn.leancloud.EngineHook;
import cn.leancloud.ResponseUtil;

public class FunctionsTest {

  @Test
  public void testFunctionCallResponse() {
    AVUser user = new AVUser();
    JSONObject results = new JSONObject();
    results.put("results", user);
    JSONObject filteredResult =
        JSON.parseObject(ResponseUtil.filterResponse(AVUtils.restfulCloudData(results)),
            JSONObject.class);

    assertFalse(filteredResult.getJSONObject("results").containsKey("__type"));
  }

  @Test
  public void testParamsParse() {

    Map<String, Object> params = new HashMap<String, Object>();
    params.put("fromPeer", "123");
    params.put("convId", "456");
    params.put("content", "shit from Mars");
    params.put("timestamp", 123123123l);
    params.put("offlinePeers", Arrays.asList("12", "123"));

    String content = JSON.toJSONString(params);
    EngineFunctionParamInfo info = new EngineFunctionParamInfo(Map.class, "object");
    Map<String, Object> parsedParams = (Map<String, Object>) info.parseParams(content);
    assertEquals("shit from Mars", parsedParams.get("content"));
    assertEquals("123", parsedParams.get("fromPeer"));
  }
}
