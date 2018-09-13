package cn.leancloud.leanengine_test;


import cn.leancloud.LeanEngine;
import cn.leancloud.leanengine_test.data.Todo;
import com.alibaba.fastjson.JSON;
import com.avos.avoscloud.AVOSCloud;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.AVUtils;
import com.avos.avoscloud.okhttp.*;
import org.junit.After;
import org.junit.Before;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class EngineBasicTest {

  OkHttpClient client = new OkHttpClient();
  private LeanEngine engine;

  @Before
  public void setUp() throws Exception {
    AVObject.registerSubclass(Todo.class);
    AVOSCloud.setDebugLogEnabled(true);
    System.setProperty("LEANCLOUD_APP_ID", getAppId());
    System.setProperty("LEANCLOUD_APP_KEY", getAppKey());
    System.setProperty("LEANCLOUD_APP_MASTER_KEY", getMasterKey());
    System.setProperty("LEANCLOUD_APP_HOOK_KEY", getHookKey());
    System.setProperty("LEANCLOUD_APP_PORT", "3000");
    System.setProperty("LEANCLOUD_APP_ENV", "development");
    engine = new LeanEngine()
        .register(new Class[]{AllEngineFunctions.class, AllEngineHook.class, AllIMHook.class})
        .setLocalEngineCallEnabled(true)
        .setUseMasterKey(true)
        .start();
  }

  @After
  public void teardown() throws Exception {
    engine.stop();
  }

  public Request.Builder getBasicTestRequestBuilder() {
    Request.Builder builder = new Request.Builder();
    builder.addHeader("X-LC-Id", getAppId());
    builder.addHeader("X-LC-Key", getMasterKey() + ",master");
    builder.addHeader("Content-Type", getContentType());
    return builder;
  }

  protected String request(String path, String appKey, String sign, String hookKey, String content, int expectedStatusCode) {
    Request.Builder builder = new Request.Builder();
    builder.addHeader("X-LC-Id", getAppId());
    if (appKey != null) {
      builder.addHeader("X-LC-key", appKey);
    }
    if (sign != null) {
      builder.addHeader("x-lc-sign", sign);
    }
    if (hookKey != null) {
      builder.addHeader("X-LC-Hook-key", hookKey);
    }
    builder.addHeader("Content-Type", getContentType());
    builder.url("http://localhost:3000" + path);
    builder.post(RequestBody.create(MediaType.parse(getContentType()), content));
    try {
      Response response = client.newCall(builder.build()).execute();
      assertEquals(expectedStatusCode, response.code());
      return new String(response.body().bytes());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  protected Map requestHook(String className, String action, final Map<String, Object> object, int expectedStatusCode) {
    return requestHook(className, action, object, null, expectedStatusCode);
  }

  protected Map requestHook(String className, String action, final Map<String, Object> object, final AVUser currentUser, int expectedStatusCode) {
    String content = JSON.toJSONString(new HashMap() {{
      put("object", object);
      if (currentUser != null) {
        Map user = (Map) AVUtils.getParsedObject(currentUser, true, true, true);
        user.remove("__type");
        user.remove("className");
        put("user", user);
      }
    }});
    String response = request(String.format("/1.1/functions/%s/%s", className, action), getAppKey(), null, getHookKey(), content, expectedStatusCode);
    return JSON.parseObject(response, Map.class);
  }

  protected String getAppId() {
    return "4h2h4okwiyn8b6cle0oig00vitayum8ephrlsvg7xo8o19ne";
  }

  protected String getAppKey() {
    return "3xjj1qw91cr3ygjq9lt0g8c3qpet38rrxtwmmp0yffyoy2t4";
  }

  protected String getMasterKey() {
    return "3v7z633lzfec9qzx8sjql6zimvdpmtwypcchr2gelu5mrzb0";
  }

  protected String getHookKey() {
    return "qlkcRqv9v5J5A11Byr3mzori";
  }

  protected String getContentType() {
    return "application/json";
  }

}
