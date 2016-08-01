package cn.leancloud.leanengine_test;

import java.util.List;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.AVUtils;

import cn.leancloud.EngineHook;
import cn.leancloud.EngineHookType;
import cn.leancloud.EngineRequestContext;

public class AllEngineHook {

  @EngineHook(className = "_User", type = EngineHookType.onLogin)
  public static void testOnLogin(AVUser user) throws Exception {
    if ("spamUser".equals(user.getUsername())) {
      throw new AVException(400, "forbidden");
    } else {
      return;
    }
  }

  @EngineHook(className = "_User", type = EngineHookType.onVerifiedSMS)
  public static void testSMSVerified(AVUser user) throws Exception {
    if (!"576ccfbbd342d30057b6e5af".equals(user.getObjectId())) {
      throw new AVException(400, "wrong user");
    }
  }

  @EngineHook(className = "TestReview", type = EngineHookType.beforeSave)
  public static AVObject beforeSave(AVObject obj) throws Exception {
    if (obj.getInt("star") <= 50) {
      if (obj.getInt("star") > 30) {
        obj.put("star", 30);
      }
      return obj;
    } else {
      // TODO 不建议让用户设置 400 这样的响应码，希望能隐藏 http 的细节。
      throw new AVException(400, "star should less than 50");
    }
  }

  @EngineHook(className = "TestReview", type = EngineHookType.afterSave)
  public static void testAfterSave(AVObject object) throws Exception {
    if (object == null) {
      throw new AVException(400, "empty object");
    } else if (AVUtils.isBlankString(object.getObjectId())) {
      throw new AVException(400, "object not saved");
    } else if (!"TestReview".equals(object.getClassName())) {
      throw new AVException(400, "className not match");
    }
    return;
  }

  @EngineHook(className = "TestReview", type = EngineHookType.beforeUpdate)
  public static AVObject testBeforeUpdate(AVObject object) throws Exception {
    List<String> updateKeys = EngineRequestContext.getUpdateKeys();
    if (updateKeys == null || updateKeys.isEmpty()) {
      throw new AVException(400, "nothing to update");
    } else {
      return object;
    }
  }

  @EngineHook(className = "TestReview", type = EngineHookType.afterUpdate)
  public static void testAfterUpdate(AVObject object) throws Exception {
    if (object == null) {
      throw new AVException(400, "empty object");
    }
    if (AVUtils.isBlankString(object.getObjectId())) {
      throw new AVException(400, "object not saved");
    } else if (!"TestReview".equals(object.getClassName())) {
      throw new AVException(400, "className not match");
    } else if (object.getInt("star") > 5 && object.getInt("star") < 1) {
      throw new AVException(400, "invalid star value");
    }
  }

  @EngineHook(className = "TestReview", type = EngineHookType.beforeDelete)
  public static void testBeforeDelete(AVObject object) throws Exception {
    if ("1234567890".equals(object.getObjectId())) {
      throw new AVException(400, "Object is being protected");
    } else {
      return;
    }
  }

  @EngineHook(className = "TestReview", type = EngineHookType.afterDelete)
  public static void testAfterDelete(AVObject object) throws Exception {
    if ("1234567890".equals(object.getObjectId())) {
      throw new AVException(400, "Object is being protected");
    } else {
      return;
    }
  }
}
