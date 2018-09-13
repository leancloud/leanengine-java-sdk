package cn.leancloud.leanengine_test;

import cn.leancloud.IMHook;
import cn.leancloud.IMHookType;
import com.alibaba.fastjson.JSONObject;
import com.avos.avoscloud.AVUtils;

import java.util.HashMap;
import java.util.Map;

public class AllIMHook {

  @IMHook(type = IMHookType.messageReceived)
  public static Map<String, Object> onMessageReceived(Map<String, Object> params) {
    Map<String, Object> result = new HashMap<String, Object>();
    if (!"99.99.99.99".equals(params.get("sourceIP"))) {
      result.put("drop", true);
    }
    return result;
  }

  @IMHook(type = IMHookType.receiversOffline)
  public static Map<String, Object> onReceiversOffline(Map<String, Object> params) {
    Map<String, Object> result = new HashMap<String, Object>();
    JSONObject object = new JSONObject();
    object.put("badge", "Increment");
    object.put("sound", "default");
    object.put("_profile", "dev");
    object.put("alert", params.get("content"));
    result.put("pushMessage", object);
    return result;
  }

  @IMHook(type = IMHookType.messageSent)
  public static void onMessageSent(Map<String, Object> params) throws Exception {
    if ((Boolean) params.get("bin")) {
      throw new Exception("binary message not granted");
    }
    return;
  }

  @IMHook(type = IMHookType.conversationStart)
  public static Map<String, Object> onConversationStart(Map<String, Object> params) {
    if ("lbt05".equals(params.get("initBy"))) {
      Map<String, Object> result = new HashMap<String, Object>();
      result.put("reject", true);
      result.put("code", 9890);
      return result;
    }
    return null;
  }

  @IMHook(type = IMHookType.conversationStarted)
  public static Map<String, Object> onConversationStarted(Map<String, Object> params)
      throws Exception {
    if (AVUtils.isBlankString((String) params.get("convId"))) {
      throw new Exception("wrong conversation");
    }
    return null;
  }

  @IMHook(type = IMHookType.conversationAdd)
  public static Map<String, Object> onConversationAdd(Map<String, Object> params) {
    if ("lbt05".equals(params.get("initBy"))) {
      Map<String, Object> result = new HashMap<String, Object>();
      result.put("reject", true);
      result.put("code", 9891);
      return result;
    }
    return null;
  }

  @IMHook(type = IMHookType.conversationRemove)
  public static Map<String, Object> onConversationRemove(Map<String, Object> params) {
    if ("lbt05".equals(params.get("initBy"))) {
      Map<String, Object> result = new HashMap<String, Object>();
      result.put("reject", true);
      result.put("code", 9892);
      return result;
    }
    return null;
  }

  @IMHook(type = IMHookType.conversationUpdate)
  public static Map<String, Object> onConversationUpdate(Map<String, Object> params) {
    Map<String, Object> result = new HashMap<String, Object>();
    if ("lbt05".equals(params.get("initBy"))) {
      result.put("reject", true);
      result.put("code", 9893);
    } else {
      result.put("attr", params.get("attr"));
      result.put("mute", params.get("mute"));
    }
    return result;
  }
}
