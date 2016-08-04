package cn.leancloud.leanengine_test;

import java.util.EnumSet;

import javax.servlet.DispatcherType;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;
import org.junit.After;
import org.junit.Before;

import cn.leancloud.EngineSessionCookie;
import cn.leancloud.HttpsRequestRedirectFilter;
import cn.leancloud.LeanEngine;
import cn.leancloud.LeanEngineHealthCheckServlet;
import cn.leancloud.RequestUserAuthFilter;

import com.avos.avoscloud.AVOSCloud;
import com.avos.avoscloud.internal.impl.JavaRequestSignImplementation;
import com.avos.avoscloud.okhttp.OkHttpClient;
import com.avos.avoscloud.okhttp.Request;

public class EngineBasicTest {

  private static Server server;
  private static int port = 3000;

  String secret = "05XgTktKPMkU";
  OkHttpClient client = new OkHttpClient();

  @Before
  public void setUp() throws Exception {
    System.setProperty("LEANCLOUD_APP_PORT", "3000");
    System.setProperty("LC_API_SERVER", "https://api.leancloud.cn");
    LeanEngine.initialize("uu2P5gNTxGhjyaJGAPPnjCtJ-gzGzoHsz", "j5lErUd6q7LhPD8CXhfmA2Rg",
        "atXAmIVlQoBDBLqumMgzXhcY");
    LeanEngine.setLocalEngineCallEnabled(true);
    JavaRequestSignImplementation.instance().setUseMasterKey(true);
    LeanEngine.addSessionCookie(new EngineSessionCookie(secret, 160000, true));
    AVOSCloud.setDebugLogEnabled(true);

    server = new Server(port);
    ServletHandler handler = new ServletHandler();
    server.setHandler(handler);
    handler.addServletWithMapping(LeanEngineHealthCheckServlet.class, "/__engine/1/ping");
    handler.addServletWithMapping(LeanEngine.class, "/1.1/functions/*");
    handler.addServletWithMapping(LeanEngine.class, "/1.1/call/*");

    handler.addFilterWithMapping(HttpsRequestRedirectFilter.class, "/*",
        EnumSet.of(DispatcherType.INCLUDE, DispatcherType.REQUEST));

    handler.addFilterWithMapping(RequestUserAuthFilter.class, "/*",
        EnumSet.of(DispatcherType.INCLUDE, DispatcherType.REQUEST));
    server.start();
  }

  @After
  public void teardown() throws Exception {
    server.stop();
  }

  public Request.Builder getBasicTestRequestBuilder() {
    Request.Builder builder = new Request.Builder();
    builder.addHeader("X-LC-Id", getAppId());
    builder.addHeader("X-LC-Key", getAppKey());
    builder.addHeader("x-uluru-master-key", getMasterKey());
    builder.addHeader("Content-Type", getContentType());
    return builder;
  }

  protected String getAppId() {
    return "uu2P5gNTxGhjyaJGAPPnjCtJ-gzGzoHsz";
  }

  protected String getAppKey() {
    return "j5lErUd6q7LhPD8CXhfmA2Rg";
  }

  protected String getMasterKey() {
    return "atXAmIVlQoBDBLqumMgzXhcY";
  }

  protected String getContentType() {
    return "application/json";
  }

}
