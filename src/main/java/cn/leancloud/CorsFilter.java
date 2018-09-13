package cn.leancloud;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class CorsFilter implements Filter {

  private static final String METHOD_OPTIONS = "OPTIONS";

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
    HttpServletRequest req = (HttpServletRequest) request;
    HttpServletResponse resp = (HttpServletResponse) response;

    setAllowOriginHeader(req, resp);
    if (METHOD_OPTIONS.equals(req.getMethod())) {
      resp.setHeader("Access-Control-Max-Age", "86400");
      resp.setHeader("Access-Control-Allow-Methods", "PUT, GET, POST, DELETE, OPTIONS");
      resp.setHeader("Access-Control-Allow-Headers",
          "X-LC-Id, X-LC-Key, X-LC-Session, X-LC-Sign, X-LC-Prod, X-LC-UA, X-Uluru-Application-Key, X-Uluru-Application-Id, X-Uluru-Application-Production, X-Uluru-Client-Version, X-Uluru-Session-Token, X-AVOSCloud-Application-Key, X-AVOSCloud-Application-Id, X-AVOSCloud-Application-Production, X-AVOSCloud-Client-Version, X-AVOSCloud-Session-Token, X-AVOSCloud-Super-Key, X-Requested-With, Content-Type, X-AVOSCloud-Request-sign");
      resp.setHeader("Content-Length", "0");
      resp.setStatus(HttpServletResponse.SC_OK);
      resp.getWriter().println();
    } else {
      chain.doFilter(request, response);
    }
  }

  private void setAllowOriginHeader(HttpServletRequest req, HttpServletResponse resp) {
    String allowOrigin = req.getHeader("origin");
    if (allowOrigin == null) {
      allowOrigin = "*";
    }
    resp.setHeader("Access-Control-Allow-Origin", allowOrigin);
  }

  @Override
  public void destroy() {
  }

}
