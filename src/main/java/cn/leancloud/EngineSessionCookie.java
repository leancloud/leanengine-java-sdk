package cn.leancloud;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.util.Base64;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.AVUtils;

public class EngineSessionCookie {

  int maxAge;
  boolean fetchUser;
  String sessionKey;

  public EngineSessionCookie(int maxAge, boolean fetchUser) {
    this("avos:sess", maxAge, fetchUser);
  }

  public EngineSessionCookie(String sessionKey, int maxAge, boolean fetchUser) {
    this.maxAge = maxAge;
    this.fetchUser = fetchUser;
    this.sessionKey = sessionKey;
  }

  protected void parseCookie(HttpServletRequest req) {
    Cookie[] cookies = req.getCookies();
    Cookie sessionCookie = null;
    for (Cookie cookie : cookies) {
      if (cookie.getName().equals(sessionKey)) {
        sessionCookie = cookie;
      }
    }
    if (sessionCookie != null) {
      AVUser user = decodeUser(sessionCookie.getValue());
      if (fetchUser && user != null) {
        try {
          user.fetch();
        } catch (AVException e) {
          e.printStackTrace();
        }
      }
      if (user != null) {
        AVUser.changeCurrentUser(user, true);
      }
    }
  }

  protected void wrappCookie(HttpServletResponse resp) {
    AVUser u = AVUser.getCurrentUser();
    if (u != null) {
      Cookie cookie = new Cookie(sessionKey, encodeUser(u));
      cookie.setMaxAge(maxAge);
      resp.addCookie(cookie);
    }
  }

  private static AVUser decodeUser(String cookieValue) {
    String userInfoStr = new String(Base64.decodeFast(cookieValue));
    Map<String, Object> userInfo = JSON.parseObject(userInfoStr, Map.class);
    AVUser user;
    try {
      user = AVUser.createWithoutData(AVUser.class, (String) userInfo.get("objectId"));
      AVUtils.copyPropertiesFromMapToAVObject(userInfo, user);
      return user;
    } catch (AVException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return null;
  }

  private static String encodeUser(AVUser user) {
    Map<String, String> userInfo = new HashMap<String, String>();
    userInfo.put("objectId", user.getObjectId());
    userInfo.put("sessionToken", user.getSessionToken());
    String userInfoStr = JSON.toJSONString(userInfo);
    return AVUtils.Base64Encode(userInfoStr);
  }
}
