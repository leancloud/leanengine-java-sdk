package cn.leancloud.leanengine_test;

import java.util.List;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.AVUtils;
import com.avos.avoscloud.LogUtil;

import cn.leancloud.EngineHook;
import cn.leancloud.EngineHookType;
import cn.leancloud.EngineRequestContext;

public class SampleTest {

  @EngineHook(className = "Review", type = EngineHookType.beforeSave)
  public static AVObject reviewBeforeSaveHook(AVObject review) throws Exception {
    if (AVUtils.isBlankString(review.getString("comment"))) {
      throw new Exception("No Comment");
    } else if (review.getString("comment").length() > 140) {
      review.put("comment", review.getString("comment").substring(0, 137) + "...");
    }
    return review;
  }

  @EngineHook(className = "Review", type = EngineHookType.afterSave)
  public static void reviewAfterSaveHook(AVObject review) throws Exception {
    AVObject post = review.getAVObject("post");
    post.fetch();
    post.increment("comments");
    post.save();
  }


  @EngineHook(className = "_User", type = EngineHookType.afterSave)
  public static void userAfterSaveHook(AVUser user) throws Exception {
    LogUtil.avlog.d(user.toString());
    user.put("from", "LeanCloud");
    user.save();
  }

  @EngineHook(className = "Review", type = EngineHookType.beforeUpdate)
  public static AVObject reviewBeforeUpdateHook(AVObject review) throws Exception {
    List<String> updateKeys = EngineRequestContext.getUpdateKeys();
    for (String key : updateKeys) {
      if ("comment".equals(key) && review.getString("comment").length() > 140) {
        throw new Exception("comment 长度不得超过 140 字符");
      }
    }
    return review;
  }

  @EngineHook(className = "Article", type = EngineHookType.afterUpdate)
  public static void articleAfterUpdateHook(AVObject article) throws Exception {
    LogUtil.avlog.d("updated article,the id is:" + article.getObjectId());
  }

  @EngineHook(className = "Album", type = EngineHookType.beforeDelete)
  public static AVObject albumBeforeDeleteHook(AVObject album) throws Exception {
    AVQuery query = new AVQuery("Photo");
    query.whereEqualTo("album", album);
    int count = query.count();
    if (count > 0) {
      throw new Exception("can't delete album if there's still photoes");
    } else {
      return album;
    }
  }

  @EngineHook(className = "Album", type = EngineHookType.afterDelete)
  public static void albumAfterDeleteHook(AVObject album) throws Exception {
    AVQuery query = new AVQuery("Photo");
    query.whereEqualTo("album", album);
    List<AVObject> result = query.find();
    if (result != null && !result.isEmpty()) {
      AVObject.deleteAll(result);
    }
  }

  @EngineHook(className = "_User", type = EngineHookType.onVerified)
  public static void userOnVerifiedHook(AVUser user) throws Exception {
    LogUtil.avlog.d("onVerified: sms,user:" + user.getObjectId());
  }

  @EngineHook(className = "_User", type = EngineHookType.onVerified)
  public static AVUser userOnLoginHook(AVUser user) throws Exception {
    if ("noLogin".equals(user.getUsername())) {
      throw new Exception("Forbidden");
    } else {
      return user;
    }
  }

  @EngineHook(className = "Review", type = EngineHookType.beforeSave)
  public static AVObject reviewBeforeSaveHookException(AVObject review) throws Exception {
    throw new AVException(123, "自定义错误信息");
  }
}
