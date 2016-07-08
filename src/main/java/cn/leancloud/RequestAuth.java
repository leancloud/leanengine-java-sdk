package cn.leancloud;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.avos.avoscloud.internal.impl.EngineRequestSign;

class RequestAuth {

  public static final String ATTRIBUTE_KEY = "requestAuth";
  public static final String USER_KEY = "authUser";
  private String appId;
  private String appKey;
  private String masterKey;
  private String prod;
  private String sessionToken;
  private String sign;

  public static void auth(HttpServletRequest req) throws UnauthException {
    RequestAuth info = new RequestAuth(req);

    if (info.getAppId() == null) {
      throw new UnauthException();
    }

    if (LeanEngine.getAppId().equals(info.getAppId()) //
        && LeanEngine.getAppKey().equals(info.getAppKey()) //
        || LeanEngine.getMasterKey().equals(info.getAppKey()) //
        || LeanEngine.getMasterKey().equals(info.getMasterKey())) {
      if (LeanEngine.getMasterKey().equals(info.getMasterKey())) {
        req.setAttribute("authMasterKey", true);
      }
      req.setAttribute(ATTRIBUTE_KEY, info);
      return;
    }
    if (info.getSign() != null) {
      String[] split = info.getSign().split(",");
      String sign = split[0];
      String ts = split[1];
      String master = null;
      if (split.length > 2) {
        master = split[2];
      }
      boolean useMasterKey = "master".equals(master);
      String computedSign = EngineRequestSign.requestSign(Long.parseLong(ts), useMasterKey);
      if (info.getSign().equals(computedSign)) {
        req.setAttribute(ATTRIBUTE_KEY, info);
      }
    }
  }

  private RequestAuth(HttpServletRequest req) {
    if (req.getContentType() != null && req.getContentType().startsWith("text/plain")) {
      // TODO
    } else {
      appId = getHeaders(req, "x-lc-id", "x-avoscloud-application-id", "x-uluru-application-id");
      appKey =
          getHeaders(req, "x-lc-key", "x-avoscloud-application-key", "x-uluru-application-key");
      masterKey = getHeaders(req, "x-avoscloud-master-key", "x-uluru-master-key");
      if (appKey != null && appKey.indexOf(",master") > 0) {
        masterKey = appKey.substring(0, appKey.indexOf(",master"));
        appKey = null;
      }
      prod =
          getHeaders(req, "x-lc-prod", "x-avoscloud-application-production",
              "x-uluru-application-production");
      if ("false".equals(prod)) {
        prod = "0";
      }
      sessionToken =
          getHeaders(req, "x-lc-session", "x-uluru-session-token", "x-avoscloud-session-token");
      sign = getHeaders(req, "x-lc-sign", "x-avoscloud-request-sign");
    }
  }

  private String getHeaders(HttpServletRequest req, String... headers) {
    for (String header : headers) {
      String result = req.getHeader(header);
      if (result != null) {
        return result;
      }
    }
    return null;
  }

  public String getAppId() {
    return appId;
  }

  public String getAppKey() {
    return appKey;
  }

  public String getMasterKey() {
    return masterKey;
  }

  public String getProd() {
    return prod;
  }

  public String getSessionToken() {
    return sessionToken;
  }

  public String getSign() {
    return sign;
  }
}


class UnauthException extends Exception {

  private static final long serialVersionUID = -51778374436527741L;

  public void resp(HttpServletResponse resp) throws IOException {
    resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    resp.setContentType(LeanEngine.JSON_CONTENT_TYPE);
    resp.getWriter().println("{\"code\":\"401\",\"error\":\"Unauthorized.\"}");
  }

}


class InvalidParameterException extends Exception {
  public void resp(HttpServletResponse resp) throws IOException {
    resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    resp.setContentType(LeanEngine.JSON_CONTENT_TYPE);
    resp.getWriter().println("{\"code\":\"400\",\"error\":\"Invalid paramters.\"}");
  }
}
