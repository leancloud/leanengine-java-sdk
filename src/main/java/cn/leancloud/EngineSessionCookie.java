package cn.leancloud;

import java.net.URL;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.internal.impl.DefaultAVUserCookieSign;

public class EngineSessionCookie {
  boolean fetchUser;

  ThreadLocal<HttpServletResponse> responseHolder = new ThreadLocal<>();
  ThreadLocal<HttpServletRequest> requestHolder = new ThreadLocal<>();

  AVUserCookieSign sign;

  public EngineSessionCookie(String secret, int maxAge, boolean fetchUser) {
    this(new DefaultAVUserCookieSign(secret, maxAge), fetchUser);
  }

  public EngineSessionCookie(String secret, String sessionKey, int maxAge, boolean fetchUser) {
    this(new DefaultAVUserCookieSign(secret, sessionKey, maxAge), fetchUser);
  }

  public EngineSessionCookie(AVUserCookieSign sign, boolean fetchUser) {
    this.fetchUser = fetchUser;
    this.sign = sign;
  }

  protected void parseCookie(HttpServletRequest req, HttpServletResponse response) {
    this.responseHolder.set(response);
    this.requestHolder.set(req);
    if (sign.validateCookieSign(req)) {
      AVUser user = sign.decodeUser(req);
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
        Cookie userCookie = sign.encodeUser(u);
        Cookie userSignCookie = sign.getCookieSign(u);
        if (userCookie != null) {
          userCookie.setDomain(host);
          userCookie.setPath("/");
          addCookie(req, resp, userCookie);
        }
        if (userSignCookie != null) {
          userSignCookie.setDomain(host);
          userSignCookie.setPath("/");
          addCookie(req, resp, userSignCookie);
        }
      }
    } else {
      responseHolder.set(null);
    }
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
