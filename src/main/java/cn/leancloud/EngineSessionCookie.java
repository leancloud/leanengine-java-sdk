package cn.leancloud;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.fastjson.JSON;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.AVUtils;

public class EngineSessionCookie {

  private static final String SESSION_TOKEN = "_sessionToken";
  private static final String UID = "_uid";
  int maxAge;
  boolean fetchUser;
  String sessionKey;
  String secret;

  public EngineSessionCookie(String secret, int maxAge, boolean fetchUser) {
    this(secret, "avos:sess", maxAge, fetchUser);
  }

  public EngineSessionCookie(String secret, String sessionKey, int maxAge, boolean fetchUser) {
    this.maxAge = maxAge;
    this.fetchUser = fetchUser;
    this.sessionKey = sessionKey;
    this.secret = secret;
  }

  protected void parseCookie(HttpServletRequest req) {

    Cookie sessionCookie = getCookie(req, sessionKey);

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

  protected void wrappCookie(HttpServletRequest req, HttpServletResponse resp) {
    AVUser u = AVUser.getCurrentUser();
    if (u != null) {
      Cookie cookie = new Cookie(sessionKey, encodeUser(u));
      cookie.setMaxAge(maxAge);
      Cookie signCookie =
          new Cookie(sessionKey + ".sig", getCookieSign(sessionKey, cookie.getValue(), secret));
      signCookie.setMaxAge(maxAge);
      resp.addCookie(cookie);
      resp.addCookie(signCookie);
    } else {
      Cookie cookie = new Cookie(sessionKey, null);
      Cookie signCookie = new Cookie(sessionKey + ".sig", null);
      cookie.setMaxAge(0);
      signCookie.setMaxAge(0);
      resp.addCookie(cookie);
      resp.addCookie(signCookie);
    }
  }

  public static String getCookieSign(String key, String cookieValue, String secret) {
    String text = key + "=" + cookieValue;
    return signCookie(secret, text);
  }

  private static AVUser decodeUser(String cookieValue) {
    String userInfoStr = new String(Base64.getDecoder().decode(cookieValue));
    Map<String, Object> userInfo = JSON.parseObject(userInfoStr, Map.class);
    System.out.println(userInfo);
    if (userInfo.containsKey(UID) && userInfo.containsKey(SESSION_TOKEN)) {
      AVUser user;
      try {
        user = AVUser.createWithoutData(AVUser.class, (String) userInfo.get(UID));
        Map<String, Object> value = new HashMap<String, Object>();
        value.put(AVUser.SESSION_TOKEN_KEY, userInfo.get(SESSION_TOKEN));
        AVUtils.copyPropertiesFromMapToAVObject(value, user);
        System.out.println(user + "SHITSHIT");
        return user;
      } catch (AVException e) {
        e.printStackTrace();
      }
    }
    return null;
  }

  public static String encodeUser(AVUser user) {
    Map<String, String> userInfo = new HashMap<String, String>();
    userInfo.put(UID, user.getObjectId());
    userInfo.put(SESSION_TOKEN, user.getSessionToken());
    String userInfoStr = JSON.toJSONString(userInfo);
    return Base64.getEncoder().encodeToString(userInfoStr.getBytes());
  }

  private static Cookie getCookie(HttpServletRequest req, String cookieName) {
    Cookie[] cookies = req.getCookies();
    if (cookies == null) {
      return null;
    }

    for (Cookie cookie : cookies) {
      if (cookie.getName().equals(cookieName)) {
        return cookie;
      }
    }
    return null;
  }

  private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";

  public static String signCookie(String key, String cookie) {
    try {
      SecretKeySpec signingKey = new SecretKeySpec(key.getBytes(), HMAC_SHA1_ALGORITHM);
      Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
      mac.init(signingKey);
      byte[] rawHmac = mac.doFinal(cookie.getBytes());
      return Base64.getUrlEncoder().withoutPadding().encodeToString(rawHmac);
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
    } catch (InvalidKeyException e) {
      e.printStackTrace();
    }
    return null;
  }
}
