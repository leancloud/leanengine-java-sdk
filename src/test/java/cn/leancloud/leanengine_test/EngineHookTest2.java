package cn.leancloud.leanengine_test;

import static org.junit.Assert.assertEquals;

import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.junit.Test;

import cn.leancloud.LeanEngine;

public class EngineHookTest2 extends EngineBasicTest {

  private HttpClient httpClient;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    LeanEngine.register(AllEngineHook.class);
    httpClient = new HttpClient();
    httpClient.start();
  }


  public void teardown() throws Exception {
    super.teardown();
    httpClient.stop();
  }

  @Test
  public void testHookWithError() throws Exception {
    String content = "{\"object\":{\"star\":100}}";
    ContentResponse response =
        httpClient.POST("http://localhost:3000/1.1/functions/TestReview/beforeSave")
            .header("x-lc-id", getAppId()).header("x-lc-key", getAppKey())
            .content(new StringContentProvider(content), getContentType()).send();
    assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.getStatus());
    assertEquals("{\"code\":1,\"error\":\"star should less than 50\"}",
        response.getContentAsString());
  }

}
