package cn.leancloud;


import com.avos.avoscloud.AVUtils;
import com.avos.avoscloud.internal.impl.JavaRequestSignImplementation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class AuthFilter implements Filter {

  static final String USER_KEY = "authUser";
  static final String ATTRIBUTE_KEY = "requestAuth";
  static final String ATTRIBUTE_AUTH_MASTER_KEY = "authMasterKey";
  private static final Logger logger = LogManager.getLogger(AuthFilter.class);
  private final LeanEngine engine;

  AuthFilter(LeanEngine engine) {
    this.engine = engine;
  }

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {

  }

  @Override
  public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
    try {
      AuthInfo info = new AuthInfo((HttpServletRequest) req);
      logger.debug("request auth: {}", info);

      if (info.appId == null) {
        throw new UnauthException();
      }

      if (engine.getAppId().equals(info.appId) //
          && (engine.getAppKey().equals(info.appKey) //
          || engine.getMasterKey().equals(info.appKey) //
          || engine.getMasterKey().equals(info.masterKey))) {
        if (engine.getMasterKey().equals(info.masterKey)) {
          // 只有masterKey时才能获取metaData
          req.setAttribute(ATTRIBUTE_AUTH_MASTER_KEY, true);
        }
        req.setAttribute(ATTRIBUTE_KEY, info);
        chain.doFilter(req, resp);
        return;
      }
      if (info.sign != null) {
        String[] split = info.sign.split(",");
        String sign = split[0];
        String ts = split[1];
        String master = null;
        if (split.length > 2) {
          master = split[2];
        }
        boolean useMasterKey = "master".equals(master);
        String computedSign =
            JavaRequestSignImplementation.requestSign(Long.parseLong(ts), useMasterKey);
        if (info.sign.equals(computedSign)) {
          req.setAttribute(ATTRIBUTE_KEY, info);
          chain.doFilter(req, resp);
          return;
        }
      }
      throw new UnauthException();
    } catch (UnauthException e) {
      e.resp((HttpServletResponse) resp);
    }
  }

  @Override
  public void destroy() {

  }

  static AuthInfo getAuthInfo(ServletRequest req) {
    return (AuthInfo) req.getAttribute(ATTRIBUTE_KEY);
  }

  class AuthInfo {

    final String appId;
    final String appKey;
    final String masterKey;
    final String hookKey;
    final String prod;
    final String sessionToken;
    final String sign;

    AuthInfo(HttpServletRequest req) {
      if (req.getContentType() != null && req.getContentType().startsWith("text/plain")) {
        appId = appKey = masterKey = hookKey = prod = sessionToken = sign = null;
      } else {
        appId = getHeaders(req, "x-lc-id", "x-avoscloud-application-id", "x-uluru-application-id");

        String tmpAppKey =
            getHeaders(req, "x-lc-key", "x-avoscloud-application-key", "x-uluru-application-key");
        String tmpMasterKey = getHeaders(req, "x-avoscloud-master-key", "x-uluru-master-key");
        if (tmpAppKey != null && tmpAppKey.indexOf(",master") > 0) {
          tmpMasterKey = tmpAppKey.substring(0, tmpAppKey.indexOf(",master"));
          tmpAppKey = null;
        }
        appKey = tmpAppKey;
        masterKey = tmpMasterKey;

        hookKey = getHeaders(req, "x-lc-hook-key");

        String tmpProd = getHeaders(req, "x-lc-prod", "x-avoscloud-application-production",
            "x-uluru-application-production");
        if ("false".equals(tmpProd)) {
          tmpProd = "0";
        }
        prod = tmpProd;

        sessionToken =
            getHeaders(req, "x-lc-session", "x-uluru-session-token", "x-avoscloud-session-token");
        sign = getHeaders(req, "x-lc-sign", "x-avoscloud-request-sign");

        // 放在这里只能算是一个side effect
        String remoteAddress = getHeaders(req, "x-real-ip", "x-forwarded-for");
        if (AVUtils.isBlankString(remoteAddress)) {
          remoteAddress = req.getRemoteAddr();
        }
        EngineRequestContext.setSessionToken(sessionToken);
        EngineRequestContext.setRemoteAddress(remoteAddress);
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


    @Override
    public String toString() {
      return "AuthInfo [appId=" + appId + ", appKey="
          + (appKey != null ? appKey.substring(0, 2) + "..." : null) //
          + ", masterKey=" + (masterKey != null ? masterKey.substring(0, 2) + "..." : null) //
          + ", prod=" + prod + ", sessionToken=" + sessionToken + ", sign=" + sign + "]";
    }

  }

}
