package cn.leancloud;

import java.net.URL;
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
  ThreadLocal<HttpServletResponse> responseHolder = new ThreadLocal<>();
  ThreadLocal<HttpServletRequest> requestHolder = new ThreadLocal<>();
  static {
    System.setProperty("org.glassfish.web.rfc2109_cookie_names_enforced", "false");
  }

  public EngineSessionCookie(String secret, int maxAge, boolean fetchUser) {
    this(secret, "avos:sess", maxAge, fetchUser);
  }

  public EngineSessionCookie(String secret, String sessionKey, int maxAge, boolean fetchUser) {
    this.maxAge = maxAge;
    this.fetchUser = fetchUser;
    this.sessionKey = sessionKey;
    this.secret = secret;
  }

  protected void parseCookie(HttpServletRequest req, HttpServletResponse response) {
    this.responseHolder.set(response);
    this.requestHolder.set(req);
    Cookie sessionCookie = getCookie(req, sessionKey);
    Cookie cookieSign = getCookie(req, sessionKey + ".sig");
    if (sessionCookie == null
        || cookieSign == null
        || cookieSign.getValue() == null
        || sessionCookie.getValue() == null
        || !cookieSign.getValue().equals(
            getCookieSign(sessionKey, sessionCookie.getValue(), secret))) {
      return;
    }
    if (sessionCookie != null) {
      AVUser user = decodeUser(sessionCookie.getValue());
      if (fetchUser && user != null && !user.isDataAvailable()) {
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

  public void wrappCookie(boolean inResponse) {
    if (inResponse) {
      HttpServletResponse resp = responseHolder.get();
      HttpServletRequest req = requestHolder.get();
      if (resp != null) {
        AVUser u = AVUser.getCurrentUser();
        String host = null;
        try {
          URL requestURL = new URL(req.getRequestURL().toString());
          host = requestURL.getHost();
        } catch (Exception e) {
        }
        if (u != null) {
          Cookie cookie = new Cookie(sessionKey, encodeUser(u));
          cookie.setMaxAge(maxAge);
          cookie.setVersion(1);
          Cookie signCookie =
              new Cookie(sessionKey + ".sig", getCookieSign(sessionKey, cookie.getValue(), secret));
          signCookie.setVersion(1);
          signCookie.setMaxAge(maxAge);
          cookie.setPath("/");
          signCookie.setPath("/");
          if (AVUtils.isBlankString(host)) {
            cookie.setDomain(host);
            signCookie.setDomain(host);
          }
          addCookie(req, resp, cookie);
          addCookie(req, resp, signCookie);
        } else {
          Cookie cookie = new Cookie(sessionKey, null);
          Cookie signCookie = new Cookie(sessionKey + ".sig", null);
          cookie.setMaxAge(0);
          signCookie.setMaxAge(0);
          cookie.setPath("/");
          signCookie.setPath("/");
          if (AVUtils.isBlankString(host)) {
            cookie.setDomain(host);
            signCookie.setDomain(host);
          }
          addCookie(req, resp, cookie);
          addCookie(req, resp, signCookie);
        }
      }
    } else {
      responseHolder.set(null);
    }
  }

  public static String getCookieSign(String key, String cookieValue, String secret) {
    String text = key + "=" + cookieValue;
    return signCookie(secret, text);
  }

  private static AVUser decodeUser(String cookieValue) {
    String userInfoStr = new String(Base64.getDecoder().decode(cookieValue));
    Map<String, Object> userInfo = JSON.parseObject(userInfoStr, Map.class);
    if (userInfo.containsKey(UID) && userInfo.containsKey(SESSION_TOKEN)) {
      AVUser user;
      try {
        user = AVUser.createWithoutData(AVUser.class, (String) userInfo.get(UID));
        Map<String, Object> value = new HashMap<String, Object>();
        value.put(AVUser.SESSION_TOKEN_KEY, userInfo.get(SESSION_TOKEN));
        AVUtils.copyPropertiesFromMapToAVObject(value, user);
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

  public static void addCookie(HttpServletRequest request, HttpServletResponse response,
      Cookie cookie) {
    Cookie[] cookies = request.getCookies();
    boolean contains = false;
    if (cookies != null && cookies.length > 0) {
      for (Cookie existingCookie : cookies) {
        if (cookie.getName().equals(existingCookie.getName())) {
          String cookieValue = cookie.getValue();
          if (cookieValue == null) {
            contains = existingCookie.getValue() == null;
          } else {
            contains = cookieValue.equals(existingCookie.getValue());
          }
        }
      }
    }
    if (!contains) {
      response.addCookie(cookie);
    }
  }
}
