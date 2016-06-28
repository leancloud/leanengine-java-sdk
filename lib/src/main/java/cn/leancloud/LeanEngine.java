package cn.leancloud;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cn.leancloud.EndpointParser.EndpointInfo;

import com.alibaba.fastjson.JSONObject;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVUtils;
import com.avos.avoscloud.internal.InternalConfigurationController;
import com.avos.avoscloud.internal.impl.EngineAppConfiguration;
import com.avos.avoscloud.internal.impl.EnginePersistenceImplementation;


@WebServlet(name = "LeanEngineServlet", urlPatterns = {"/1/functions/*", "/1.1/functions/*",
    "/1/call/*", "/1.1/call/*"}, loadOnStartup = 0)
public class LeanEngine extends HttpServlet {

  public static final String JSON_CONTENT_TYPE = "application/json; charset=UTF-8";

  public static final long serialVersionUID = 3962660277165698922L;

  private static Map<String, EngineHandlerInfo> funcs = new HashMap<String, EngineHandlerInfo>();
  static {
    InternalConfigurationController.globalInstance().setInternalPersistence(
        EnginePersistenceImplementation.instance());
  }

  public static void register(Class<?> clazz) {
    for (Method m : clazz.getDeclaredMethods()) {
      EngineFunction func = m.getAnnotation(EngineFunction.class);
      if (func != null) {
        EngineHandlerInfo info = EngineHandlerInfo.getEngineHandlerInfo(m, func);
        if (info != null) {
          funcs.put(info.getEndPoint(), info);
        }
        continue;
      }
      EngineHook hook = m.getAnnotation(EngineHook.class);
      if (hook != null) {
        EngineHandlerInfo info = EngineHandlerInfo.getEngineHandlerInfo(m, hook);
        if (info != null) {
          funcs.put(info.getEndPoint(), info);
        }
      }

      IMHook imHook = m.getAnnotation(IMHook.class);
      if (imHook != null) {
        EngineHandlerInfo info = EngineHandlerInfo.getEngineHandlerInfo(m, imHook);
        if (info != null) {
          funcs.put(info.getEndPoint(), info);
        }
      }
    }
  }

  @Override
  protected void doOptions(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    setAllowOriginHeader(req, resp);
    resp.setHeader("Access-Control-Max-Age", "86400");
    resp.setHeader("Access-Control-Allow-Methods", "PUT, GET, POST, DELETE, OPTIONS");
    resp.setHeader(
        "Access-Control-Allow-Headers",
        "X-LC-Id, X-LC-Key, X-LC-Session, X-LC-Sign, X-LC-Prod, X-LC-UA, X-Uluru-Application-Key, X-Uluru-Application-Id, X-Uluru-Application-Production, X-Uluru-Client-Version, X-Uluru-Session-Token, X-AVOSCloud-Application-Key, X-AVOSCloud-Application-Id, X-AVOSCloud-Application-Production, X-AVOSCloud-Client-Version, X-AVOSCloud-Session-Token, X-AVOSCloud-Super-Key, X-Requested-With, Content-Type, X-AVOSCloud-Request-sign");
    resp.setHeader("Content-Length", "0");
    resp.setStatus(HttpServletResponse.SC_OK);
    resp.getWriter().println();
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
      IOException {
    setAllowOriginHeader(req, resp);
    try {
      RequestAuth.auth(req);
    } catch (UnauthException e) {
      e.resp(resp);
      e.printStackTrace();
      return;
    }
    EndpointInfo internalEndpoint = EndpointParser.getInternalEndpoint(req);

    if (internalEndpoint == null || AVUtils.isBlankString(internalEndpoint.getInternalEndpoint())
        || !funcs.containsKey(internalEndpoint.getInternalEndpoint())) {
      resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      resp.setContentType(JSON_CONTENT_TYPE);
      resp.getWriter().println("{\"code\":\"400\",\"error\":\"Unsupported operation.\"}");
      return;
    } else {
      EngineHandlerInfo handler = funcs.get(internalEndpoint.getInternalEndpoint());
      try {
        Object returnValue = handler.execute(req);
        if (internalEndpoint.isNeedResponse()) {
          String respJSONStr = null;
          JSONObject result = new JSONObject();
          result.put("result", AVUtils.getParsedObject(returnValue));
          respJSONStr = result.toJSONString();
          if (!internalEndpoint.isRPCcall()) {
            respJSONStr = ResponseUtil.filterResponse(respJSONStr);
          }
          resp.setContentType(JSON_CONTENT_TYPE);
          resp.getWriter().write(respJSONStr);
        }

      } catch (InvalidParameterException e) {
        if (internalEndpoint.isNeedResponse()) {
          e.resp(resp);
        }
        e.printStackTrace();
      } catch (Exception e) {
        if (internalEndpoint.isNeedResponse()) {
          resp.setContentType(JSON_CONTENT_TYPE);
          JSONObject result = new JSONObject();
          result.put("code", 400);
          result.put("error", e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
          resp.getWriter().write(result.toJSONString());
        }
        e.printStackTrace();
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

  protected static Set<String> getMetaData() {
    return funcs.keySet();
  }

  public static String getAppId() {
    return new String(EngineAppConfiguration.instance().applicationId);
  }

  public static String getAppKey() {
    return new String(EngineAppConfiguration.instance().clientKey);
  }

  public static String getMasterKey() {
    return new String(EngineAppConfiguration.instance().masterKey);
  }

  public static String getAppEnv() {
    return System.getenv("LEANCLOUD_APP_ENV");
  }

}
