package cn.leancloud.leanengine_test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import cn.leancloud.LeanEngine;

import com.avos.avoscloud.AVCloud;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.AVUtils;

public class EngineHookTest extends EngineBasicTest {

  @Override
  public void setUp() throws Exception {
    super.setUp();
    LeanEngine.register(AllEngineHook.class);
  }

  @Test
  public void testHook() throws Exception {
    AVObject object = new AVObject("hello");
    Map<String, Object> restData = (Map<String, Object>) AVUtils.getParsedObject(object, true);
    restData.put("star", 1);
    Map<String, Object> p = new HashMap<String, Object>();
    p.put("object", restData);
    Map<String, Object> result = AVCloud.callFunction("hello/beforeSave", p);
    // assertEquals(1, result.get("star"));
    restData.put("star", 40);
    result = AVCloud.callFunction("hello/beforeSave", p);
    // assertEquals(1, result.get("star"));
  }

  @Test
  public void testHookWithError() throws Exception {
    AVObject object = new AVObject("hello");
    Map<String, Object> restData = (Map<String, Object>) AVUtils.getParsedObject(object, true);
    restData.put("star", 500);
    Map<String, Object> p = new HashMap<String, Object>();
    p.put("object", restData);
    try {
      AVCloud.callFunction("hello/beforeSave", p);
    } catch (AVException e) {
      assertEquals(400, e.getCode());
    }
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
    params.put("user", AVUtils.getParsedObject(u, true));

    try {
      AVCloud.callFunction("_User/onLogin", params);
    } catch (AVException e) {
      assertEquals(400, e.getCode());
      assertEquals("forbidden", e.getLocalizedMessage());
    }
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

}
