package cn.leancloud;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVOSCloud;
import com.avos.avoscloud.AVUtils;

import cn.leancloud.EndpointParser.EndpointInfo;

@WebServlet(name = "CloudCodeServlet",
    urlPatterns = {"/1/functions/*", "/1.1/functions/*", "/1/call/*", "/1.1/call/*"},
    loadOnStartup = 0)
public class CloudCodeServlet extends HttpServlet {

  private static final long serialVersionUID = -5828358153354045625L;

  private static final Logger logger = LogManager.getLogger(CloudCodeServlet.class);

  @Override
  protected void doOptions(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    setAllowOriginHeader(req, resp);
    resp.setHeader("Access-Control-Max-Age", "86400");
    resp.setHeader("Access-Control-Allow-Methods", "PUT, GET, POST, DELETE, OPTIONS");
    resp.setHeader("Access-Control-Allow-Headers",
        "X-LC-Id, X-LC-Key, X-LC-Session, X-LC-Sign, X-LC-Prod, X-LC-UA, X-Uluru-Application-Key, X-Uluru-Application-Id, X-Uluru-Application-Production, X-Uluru-Client-Version, X-Uluru-Session-Token, X-AVOSCloud-Application-Key, X-AVOSCloud-Application-Id, X-AVOSCloud-Application-Production, X-AVOSCloud-Client-Version, X-AVOSCloud-Session-Token, X-AVOSCloud-Super-Key, X-Requested-With, Content-Type, X-AVOSCloud-Request-sign");
    resp.setHeader("Content-Length", "0");
    resp.setStatus(HttpServletResponse.SC_OK);
    resp.getWriter().println();
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    setAllowOriginHeader(req, resp);
    try {
      RequestAuth.auth(req);
    } catch (UnauthException e) {
      e.resp(resp);
      return;
    }
    EndpointInfo internalEndpoint = EndpointParser.getInternalEndpoint(req);
    logger.debug("endpoint info: {}", internalEndpoint);

    if (internalEndpoint == null || AVUtils.isBlankString(internalEndpoint.getInternalEndpoint())
        || LeanEngine.getHandler(internalEndpoint.getInternalEndpoint()) == null) {
      resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      resp.setContentType(LeanEngine.JSON_CONTENT_TYPE);
      resp.getWriter().println("{\"code\":\"400\",\"error\":\"Unsupported operation.\"}");
      return;
    } else {
      try {
        Object returnValue = LeanEngine.getHandler(internalEndpoint.getInternalEndpoint())
            .execute(req, internalEndpoint.isRPCcall());
        if (internalEndpoint.isNeedResponse()) {
          String respJSONStr = JSON.toJSONString(returnValue, SerializerFeature.WriteMapNullValue);
          resp.setContentType(LeanEngine.JSON_CONTENT_TYPE);
          resp.getWriter().write(respJSONStr);
          logger.debug("resp json string: {}", respJSONStr);
        }
      } catch (IllegalArgumentException e) {
        if (internalEndpoint.isNeedResponse()) {
          InvalidParameterException ex = new InvalidParameterException();
          ex.resp(resp);
        }
        if (AVOSCloud.isDebugLogEnabled()) {
          e.printStackTrace();
        }
      } catch (Exception e) {
        if (internalEndpoint.isNeedResponse()) {
          resp.setContentType(LeanEngine.JSON_CONTENT_TYPE);
          JSONObject result = new JSONObject();
          result.put("code",
              e.getCause() instanceof AVException ? ((AVException) e.getCause()).getCode() : 1);
          result.put("error", e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
          resp.setStatus(400);
          resp.getWriter().write(result.toJSONString());
        }
        if (AVOSCloud.isDebugLogEnabled()) {
          e.printStackTrace();
        }
      }
    }
  }


  private void setAllowOriginHeader(HttpServletRequest req, HttpServletResponse resp) {
    String allowOrigin = req.getHeader("origin");
    if (allowOrigin == null) {
      allowOrigin = "*";
    }
    resp.setHeader("Access-Control-Allow-Origin", allowOrigin);
  }
}
