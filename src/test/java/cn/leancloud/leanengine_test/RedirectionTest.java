package cn.leancloud.leanengine_test;

import cn.leancloud.LeanEngine;
import com.avos.avoscloud.okhttp.OkHttpClient;
import com.avos.avoscloud.okhttp.Request;
import com.avos.avoscloud.okhttp.Response;
import org.junit.Test;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class RedirectionTest extends EngineBasicTest {

  @Override
  public void setUp() throws Exception {
    System.setProperty("LEANCLOUD_APP_ENV", "production");
    LeanEngine.setHttpsRedirectEnabled(true);
    super.setUp();
  }

  @Test
  public void testRedirection() throws IOException {
    assertRedirect("/hello", "test.leanapp.cn", "http", 302, null, "https://test.leanapp.cn/hello");
  }

  @Test
  public void testNotRedirection() throws IOException {
    assertRedirect("/hello", "test.leanapp.cn", "https", 200, "<html><head><title>Hello World!</title></head>\n" + "<body><h1>Hello World!</h1></body></html>\n", null);
  }

  @Test
  public void testRedirectionForCustomDomain() throws IOException {
    assertRedirect("/hello", "my-domain.com", "http", 302, null, "https://my-domain.com/hello");
  }

  @Test
  public void testNotRedirectionForCustomDomain() throws IOException {
    assertRedirect("/hello", "my-domain.com", "https", 200,
        "<html><head><title>Hello World!</title></head>\n" + "<body><h1>Hello World!</h1></body></html>\n", null);
  }

  @Test
  public void testNotRedirectionForCloudFuncPath() throws IOException {
    Request.Builder builder = new Request.Builder();
    builder.url("http://localhost:3000/1.1/functions/_ops/metadatas");
    builder.header("x-lc-id", getAppId());
    builder.header("x-lc-key", getMasterKey() + ",master");
    builder.get();
    Response response = client.newCall(builder.build()).execute();
    assertEquals(HttpServletResponse.SC_OK, response.code());
  }

  private void assertRedirect(String path, String host, String proto, int expectedStatusCode, String expectedResponse, String expectedLocation) {
     try {
      OkHttpClient client = new OkHttpClient();
      client.setFollowRedirects(false);
      Request.Builder builder = new Request.Builder();
      builder.url("http://0.0.0.0:3000" + path);
      builder.header("host", host);
      builder.header("x-forwarded-proto", proto);
      builder.get();

      Response response = client.newCall(builder.build()).execute();
      assertEquals(expectedStatusCode, response.code());
      if (expectedResponse != null) {
        String result = new String(response.body().bytes());
        assertEquals(expectedResponse, result);
      }
      if (expectedLocation != null) {
        assertEquals(expectedLocation, response.header("Location"));
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

  }

  @Override
  public void teardown() throws Exception {
    super.teardown();
    System.clearProperty("LEANCLOUD_APP_ENV");
    LeanEngine.setHttpsRedirectEnabled(false);
  }
}
