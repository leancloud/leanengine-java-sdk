package cn.leancloud.leanengine_test;

import java.util.List;
import java.util.Map;

import cn.leancloud.EngineHook;
import cn.leancloud.EngineHookType;
import cn.leancloud.EngineRequestContext;

import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.AVUtils;

public class AllEngineHook {

  @EngineHook(className = "hello", type = EngineHookType.beforeSave)
  public static void beforeSave(AVObject obj) throws Exception {
    if (obj.getInt("star") <= 50) {
      return;
    } else {
      throw new Exception("star should less than 50");
    }
  }

  @EngineHook(className = "_User", type = EngineHookType.onLogin)
  public static void testData(AVUser user) throws Exception {
    if ("spamUser".equals(user.getUsername())) {
      throw new Exception("forbidden");
    } else {
      return;
    }
  }

  @EngineHook(className = "_User", type = EngineHookType.onVerified)
  public static void testSMSVerified(AVUser user) throws Exception {
    if (!"576ccfbbd342d30057b6e5af".equals(user.getObjectId())) {
      throw new Exception("wrong user");
    }
  }

  @EngineHook(className = "TestReview", type = EngineHookType.afterSave)
  public static void testAfterSave(AVObject object) throws Exception {
    if (object == null) {
      throw new Exception("empty object");
    } else if (AVUtils.isBlankString(object.getObjectId())) {
      throw new Exception("object not saved");
    } else if (!"TestReview".equals(object.getClassName())) {
      throw new Exception("className not match");
    }
    return;
  }

  @EngineHook(className = "TestReview", type = EngineHookType.beforeUpdate)
  public static void testBeforeUpdate(AVObject object) throws Exception {
    List<String> updateKeys = EngineRequestContext.getUpdateKeys();
    if (updateKeys == null || updateKeys.isEmpty()) {
      throw new Exception("nothing to update");
    }
  }

  @EngineHook(className = "TestReview", type = EngineHookType.afterUpdate)
  public static void testAfterUpdate(AVObject object) throws Exception {
    if (object == null) {
      throw new Exception("empty object");
    }
    if (AVUtils.isBlankString(object.getObjectId())) {
      throw new Exception("object not saved");
    } else if (!"TestReview".equals(object.getClassName())) {
      throw new Exception("className not match");
    } else if (object.getInt("star") > 5 && object.getInt("star") < 1) {
      throw new Exception("invalid star value");
    }
  }

  @EngineHook(className = "TestReview", type = EngineHookType.beforeDelete)
  public static void testBeforeDelete(AVObject object) throws Exception {
    if ("1234567890".equals(object.getObjectId())) {
      throw new Exception("Object is being protected");
    } else {
      return;
    }
  }

  @EngineHook(className = "TestReview", type = EngineHookType.afterDelete)
  public static void testAfterDelete(AVObject object) throws Exception {
    if ("1234567890".equals(object.getObjectId())) {
      throw new Exception("Object is being protected");
    } else {
      return;
    }
  }
}
