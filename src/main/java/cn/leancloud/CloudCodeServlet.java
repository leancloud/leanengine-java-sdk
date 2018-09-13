package cn.leancloud;

import cn.leancloud.EndpointParser.EndpointInfo;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVOSCloud;
import com.avos.avoscloud.AVUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class CloudCodeServlet extends HttpServlet {

  private static final long serialVersionUID = -5828358153354045625L;

  private static final Logger logger = LogManager.getLogger(CloudCodeServlet.class);
  private final LeanEngine engine;

  CloudCodeServlet(LeanEngine engine) {
    this.engine = engine;
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
    EndpointInfo internalEndpoint = EndpointParser.getInternalEndpoint(req);
    logger.debug("endpoint info: {}", internalEndpoint);

    if (internalEndpoint == null || AVUtils.isBlankString(internalEndpoint.getInternalEndpoint())
        || engine.getHandler(internalEndpoint.getInternalEndpoint()) == null) {
      resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      resp.setContentType(LeanEngine.JSON_CONTENT_TYPE);
      resp.getWriter().println("{\"code\":\"400\",\"error\":\"Unsupported operation.\"}");
      return;
    } else {
      try {
        Object returnValue = engine.getHandler(internalEndpoint.getInternalEndpoint())
            .execute(req, internalEndpoint.isRPCcall());
        if (internalEndpoint.isNeedResponse()) {
          String respJSONStr = JSON.toJSONString(returnValue, SerializerFeature.WriteMapNullValue);
          resp.setContentType(LeanEngine.JSON_CONTENT_TYPE);
          resp.getWriter().write(respJSONStr);
          logger.debug("resp json string: {}", respJSONStr);
        }
      } catch (IllegalArgumentException e) {
        if (internalEndpoint.isNeedResponse()) {
          invalidParameterResp(resp);
        }
        if (AVOSCloud.isDebugLogEnabled()) {
          e.printStackTrace();
        }
      } catch (UnauthException e) {
        e.resp(resp);
      } catch (Exception e) {
        if (internalEndpoint.isNeedResponse()) {
          resp.setContentType(LeanEngine.JSON_CONTENT_TYPE);
          JSONObject result = new JSONObject();
          if (e.getCause() instanceof AVException) {
            AVException ave = (AVException) e.getCause();
            result.put("code", ave.getCode());
            result.put("error", ave.getMessage());
            resp.setStatus(400);
          } else {
            e.printStackTrace();
            result.put("code", 1);
            result.put("error", e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
            resp.setStatus(500);
          }
          resp.getWriter().write(result.toJSONString());
        }
      }
    }
  }

  private void invalidParameterResp(HttpServletResponse resp) throws IOException {
    resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    resp.setContentType(LeanEngine.JSON_CONTENT_TYPE);
    resp.getWriter().println("{\"code\":400,\"error\":\"Invalid paramters.\"}");
  }

}

class UnauthException extends RuntimeException {

  public UnauthException() {
    super();
  }

  public UnauthException(String s) {
    super(s);
  }

  public UnauthException(String s, Throwable throwable) {
    super(s, throwable);
  }

  public UnauthException(Throwable throwable) {
    super(throwable);
  }

  public UnauthException(String s, Throwable throwable, boolean b, boolean b1) {
    super(s, throwable, b, b1);
  }

  void resp(HttpServletResponse resp) throws IOException {
    resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    resp.setContentType(LeanEngine.JSON_CONTENT_TYPE);
    resp.getWriter().printf("{\"code\":401,\"error\":\"%s\"}", getMessage() != null ? getMessage() : "Unauthorized.");
  }

}
