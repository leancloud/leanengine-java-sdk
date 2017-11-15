package cn.leancloud.leanengine_test;

import java.util.EnumSet;

import javax.servlet.DispatcherType;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;
import org.junit.After;
import org.junit.Before;

import com.avos.avoscloud.AVOSCloud;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.okhttp.OkHttpClient;
import com.avos.avoscloud.okhttp.Request;

import cn.leancloud.CloudCodeServlet;
import cn.leancloud.EngineSessionCookie;
import cn.leancloud.HttpsRequestRedirectFilter;
import cn.leancloud.LeanEngine;
import cn.leancloud.LeanEngineHealthCheckServlet;
import cn.leancloud.RequestUserAuthFilter;
import cn.leancloud.leanengine_test.data.Todo;

public class EngineBasicTest {

  private static Server server;
  private static int port = 3000;

  String secret = "05XgTktKPMkU";
  OkHttpClient client = new OkHttpClient();

  @Before
  public void setUp() throws Exception {
    System.setProperty("LEANCLOUD_APP_PORT", "3000");
    AVObject.registerSubclass(Todo.class);
    LeanEngine.initialize(getAppId(), getAppKey(), getMasterKey());
    LeanEngine.setLocalEngineCallEnabled(true);
    LeanEngine.setUseMasterKey(true);
    LeanEngine.addSessionCookie(new EngineSessionCookie(secret, 160000, true));
    AVOSCloud.setDebugLogEnabled(true);

    server = new Server(port);
    ServletHandler handler = new ServletHandler();
    server.setHandler(handler);
    handler.addServletWithMapping(LeanEngineHealthCheckServlet.class, "/__engine/1/ping");
    handler.addServletWithMapping(CloudCodeServlet.class, "/1.1/functions/*");
    handler.addServletWithMapping(CloudCodeServlet.class, "/1.1/call/*");

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
    return "4h2h4okwiyn8b6cle0oig00vitayum8ephrlsvg7xo8o19ne";
  }

  protected String getAppKey() {
    return "3xjj1qw91cr3ygjq9lt0g8c3qpet38rrxtwmmp0yffyoy2t4";
  }

  protected String getMasterKey() {
    return "3v7z633lzfec9qzx8sjql6zimvdpmtwypcchr2gelu5mrzb0";
  }

  protected String getContentType() {
    return "application/json";
  }

}
