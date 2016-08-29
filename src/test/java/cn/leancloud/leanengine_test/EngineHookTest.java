package cn.leancloud.leanengine_test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.junit.Test;

import com.alibaba.fastjson.JSON;
import com.avos.avoscloud.AVCloud;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.AVUtils;
import com.avos.avoscloud.okhttp.MediaType;
import com.avos.avoscloud.okhttp.OkHttpClient;
import com.avos.avoscloud.okhttp.Request;
import com.avos.avoscloud.okhttp.RequestBody;
import com.avos.avoscloud.okhttp.Response;

import cn.leancloud.LeanEngine;

public class EngineHookTest extends EngineBasicTest {

  @Override
  public void setUp() throws Exception {
    super.setUp();
    LeanEngine.register(AllEngineHook.class);
  }

  @Test
  public void testHook() throws Exception {
    String content = "{\"object\":{\"star\":35}}";
    OkHttpClient client = new OkHttpClient();
    Request.Builder builder = this.getBasicTestRequestBuilder();
    builder.url("http://localhost:3000/1.1/functions/hello/beforeSave");
    builder.post(RequestBody.create(MediaType.parse(getContentType()), content));
    Response response = client.newCall(builder.build()).execute();
    assertEquals(HttpServletResponse.SC_OK, response.code());
    assertTrue(new String(response.body().bytes()).indexOf("\"star\":30") != -1);
  }

  @Test
  public void testHookWithErrorResponseStatus() throws Exception {
    String content = "{\"object\":{\"star\":100}}";
    OkHttpClient client = new OkHttpClient();
    Request.Builder builder = this.getBasicTestRequestBuilder();
    builder.url("http://localhost:3000/1.1/functions/hello/beforeSave");
    builder.post(RequestBody.create(MediaType.parse(getContentType()), content));
    Response response = client.newCall(builder.build()).execute();
    assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.code());
    assertEquals("{\"code\":400,\"error\":\"star should less than 50\"}",
        new String(response.body().bytes()));
  }

  @Test
  public void testOnLogin() throws AVException {
    AVUser u = new AVUser();
    u.setUsername("spamUser");
    u.setPassword("123123123");
    try {
      u.signUp();
    } catch (AVException e) {
      u = AVUser.logIn("spamUser", "123123123");
    }
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("object", AVUtils.getParsedObject(u, true));

    try {
      AVCloud.callFunction("_User/onLogin", params);
    } catch (AVException e) {
      assertEquals(400, e.getCode());
      assertEquals("forbidden", e.getLocalizedMessage());
    }
  }

  @Test
  public void testOnLogin2() throws IOException {
    String content =
        "{\"object\":{\"objectId\":\"57c3b6110a2b58006cfb7be7\",\"username\":\"testUser\","
            + "\"__sign\":\"1470492196423,ecb100deddd9bcf45dc5450fdc199c294e3ffe7d\"}}";
    OkHttpClient client = new OkHttpClient();
    Request.Builder builder = this.getBasicTestRequestBuilder();
    builder.url("http://localhost:3000/1.1/functions/_User/onLogin");
    builder.post(RequestBody.create(MediaType.parse(getContentType()), content));
    Response response = client.newCall(builder.build()).execute();
    assertEquals(HttpServletResponse.SC_OK, response.code());
    assertEquals("{}", response.body().string());
  }

  @Test
  public void testOnLogin_error() throws IOException {
    String content =
        "{\"object\":{\"objectId\":\"576ccfbbd342d30057b6e5af\",\"username\":\"spamUser\","
            + "\"__sign\":\"1470492196423,ecb100deddd9bcf45dc5450fdc199c294e3ffe7d\"}}";
    OkHttpClient client = new OkHttpClient();
    Request.Builder builder = this.getBasicTestRequestBuilder();
    builder.url("http://localhost:3000/1.1/functions/_User/onLogin");
    builder.post(RequestBody.create(MediaType.parse(getContentType()), content));
    Response response = client.newCall(builder.build()).execute();
    assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.code());
    assertEquals("{\"code\":400,\"error\":\"forbidden\"}", response.body().string());
  }

  @Test
  public void testOnVerified() throws Exception {
    AVUser u = new AVUser();
    u.setUsername("spamUser");
    u.setPassword("123123123");
    try {
      u.signUp();
    } catch (AVException e) {
      u = AVUser.logIn("spamUser", "123123123");
    }
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("user", AVUtils.getParsedObject(u, true));

    Object result = AVCloud.callFunction("onVerified/sms", params);
    assertNull(result);
  }

  @Test
  public void testAfterSave() throws Exception {
    AVObject object = new AVObject("TestReview");
    Map<String, Object> restData = (Map<String, Object>) AVUtils.getParsedObject(object, true);
    restData.put("star", 500);
    Map<String, Object> p = new HashMap<String, Object>();
    p.put("object", restData);
    AVCloud.callFunction("TestReview/afterSave", p);
  }

  @Test
  public void testBeforeUpdate() throws Exception {
    AVObject object = new AVObject("TestReview");
    object.put("star", "100");
    object.save();
    Map<String, Object> restData = (Map<String, Object>) AVUtils.getParsedObject(object, true);
    restData.put("star", 500);
    restData.put("_updatedKeys", Arrays.asList("star"));
    Map<String, Object> p = new HashMap<String, Object>();
    p.put("object", restData);
    AVCloud.callFunction("TestReview/beforeUpdate", p);
  }

  @Test
  public void testBeforeUpdate2() throws Exception {
    String content = "{\"object\":{\"star\":35,\"ACL\":{\"*\":{\"write\":true,\"read\":true}}" //
        + ",\"createdAt\":\"2016-08-06T11:22:54.489Z\",\"updatedAt\":\"2016-08-06T14:03:16.422Z\"" //
        + ",\"objectId\":\"57a6bf9c8ac247005f2d8c7b\",\"_updatedKeys\":[\"comment\"]," //
        + "\"__before\":\"1470492196423,ecb100deddd9bcf45dc5450fdc199c294e3ffe7d\"},\"user\":null}";
    OkHttpClient client = new OkHttpClient();
    Request.Builder builder = this.getBasicTestRequestBuilder();
    builder.url("http://localhost:3000/1.1/functions/TestReview/beforeUpdate");
    builder.post(RequestBody.create(MediaType.parse(getContentType()), content));
    Response response = client.newCall(builder.build()).execute();
    assertEquals(HttpServletResponse.SC_OK, response.code());
    assertEquals(JSON.parseObject(content, Map.class).get("object"),
        JSON.parseObject(response.body().string(), Map.class));
  }

  @Test
  public void testAfterUpdate() throws Exception {
    AVObject object = new AVObject("TestReview");
    object.put("star", "100");
    object.save();
    Map<String, Object> restData = (Map<String, Object>) AVUtils.getParsedObject(object, true);
    restData.put("star", 500);
    Map<String, Object> p = new HashMap<String, Object>();
    p.put("object", restData);
    AVCloud.callFunction("TestReview/afterUpdate", p);
  }

  @Test
  public void testBeforeDelete() throws Exception {
    AVObject object = new AVObject("TestReview");
    object.put("star", "100");
    object.save();
    Map<String, Object> restData = (Map<String, Object>) AVUtils.getParsedObject(object, true);
    restData.put("star", 500);
    restData.put("objectId", "1234567890");
    Map<String, Object> p = new HashMap<String, Object>();
    p.put("object", restData);
    try {
      AVCloud.callFunction("TestReview/beforeDelete", p);
    } catch (AVException e) {
      assertEquals(400, e.getCode());
      assertEquals("Object is being protected", e.getMessage());
    }
  }

  @Test
  public void testAfterDelete() throws Exception {
    AVObject object = new AVObject("TestReview");
    object.put("star", "100");
    object.save();
    Map<String, Object> restData = (Map<String, Object>) AVUtils.getParsedObject(object, true);
    restData.put("star", 500);
    restData.put("objectId", "1234567890");
    Map<String, Object> p = new HashMap<String, Object>();
    p.put("object", restData);
    AVCloud.callFunction("TestReview/afterDelete", p);
  }

  @Test
  public void testSubObjectHook() throws Exception {
    String content = "{\"object\":{\"content\":\"shit\"}}";
    OkHttpClient client = new OkHttpClient();
    Request.Builder builder = this.getBasicTestRequestBuilder();
    builder.url("http://localhost:3000/1.1/functions/Todo/beforeSave");
    builder.post(RequestBody.create(MediaType.parse(getContentType()), content));
    Response response = client.newCall(builder.build()).execute();
    assertEquals(HttpServletResponse.SC_OK, response.code());
  }

}
