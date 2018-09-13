package cn.leancloud.leanengine_test;

import com.avos.avoscloud.*;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class EngineHookTest extends EngineBasicTest {

  @Test
  public void testHook() throws Exception {
    Map<String, Object> object = new HashMap() {{
      put("star", 35);
    }};
    Map result = requestHook("hello", "beforeSave", object, 200);
    assertEquals(30, result.get("star"));
  }

  @Test
  public void testHookWithErrorResponseStatus() throws Exception {
    Map<String, Object> object = new HashMap() {{
      put("star", 100);
    }};
    Map result = requestHook("hello", "beforeSave", object, 400);
    assertEquals("star should less than 50", result.get("error"));
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
    Map<String, Object> object = new HashMap() {{
      put("objectId", "57c3b6110a2b58006cfb7be7");
      put("username", "testUser");
      put("__sign", "1470492196423,ecb100deddd9bcf45dc5450fdc199c294e3ffe7d");
    }};
    Map result = requestHook("_User", "onLogin", object, 200);
    assertEquals(Collections.emptyMap(), result);
  }

  @Test
  public void testOnLogin_error() throws IOException {
    Map<String, Object> object = new HashMap() {{
      put("objectId", "576ccfbbd342d30057b6e5af");
      put("username", "spamUser");
      put("__sign", "1470492196423,ecb100deddd9bcf45dc5450fdc199c294e3ffe7d");
    }};
    Map result = requestHook("_User", "onLogin", object, 400);
    assertEquals("forbidden", result.get("error"));
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
    Map<String, Object> object = new HashMap() {{
      put("objectId", "57a6bf9c8ac247005f2d8c7b");
      put("star", 35);
      put("ACL", new HashMap() {{
        put("*", new HashMap() {{
          put("write", true);
          put("read", true);
        }});
      }});
      put("createdAt", "2016-08-06T11:22:54.489Z");
      put("updatedAt", "2016-08-06T14:03:16.422Z");
      put("_updatedKeys", new String[]{"comment"});
    }};
    requestHook("TestReview", "beforeUpdate", object, 200);
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
    Map<String, Object> object = new HashMap() {{
      put("content", "Hello World!");
    }};
    requestHook("Todo", "beforeSave", object, 200);
  }

  @Test
  public void testHook_currentUser() throws IOException, AVException {
    Map<String, Object> object = new HashMap() {{
      put("content", "Hello World!");
    }};
    AVUser user = AVUser.becomeWithSessionToken("w2jrtkbehp38otqmhbqu7ybs9");
    Map result = requestHook("Todo", "beforeSave", object, user, 200);
    assertEquals("54fd6a03e4b06c41e00b1f40", ((Map) result.get("author")).get("objectId"));

    result = requestHook("Todo", "beforeSave", object, 200);
    assertNull(result.get("author"));
  }

}
