package cn.leancloud;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import com.avos.avoscloud.AVUser;

public interface AVUserCookieSign {

  public AVUser decodeUser(HttpServletRequest request);

  public Cookie encodeUser(AVUser user);

  public Cookie getCookieSign(AVUser user);

  public boolean validateCookieSign(HttpServletRequest request);
}
