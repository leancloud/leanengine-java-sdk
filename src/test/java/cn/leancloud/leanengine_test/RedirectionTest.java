package cn.leancloud.leanengine_test;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;

import com.avos.avoscloud.okhttp.OkHttpClient;
import com.avos.avoscloud.okhttp.Request;
import com.avos.avoscloud.okhttp.Response;

import cn.leancloud.LeanEngine;

public class RedirectionTest extends EngineBasicTest {

  @Override
  public void setUp() throws Exception {
    System.setProperty("LEANCLOUD_APP_ENV", "production");
    LeanEngine.setHttpsRedirectEnabled(true);
    super.setUp();
  }

  @Test
  public void testRedirection() throws IOException {
    OkHttpClient client = new OkHttpClient();
    client.setFollowRedirects(false);
    Request.Builder builder = new Request.Builder();
    builder.url("http://0.0.0.0:3000/hello");
    builder.header("host", "test.leanapp.cn");
    builder.header("x-forwarded-proto", "http");
    builder.get();

    Response response = client.newCall(builder.build()).execute();
    assertEquals(302, response.code());
    assertEquals("https://test.leanapp.cn/hello", response.header("Location"));
  }

  @Test
  public void testNotRedirection() throws IOException {
    OkHttpClient client = new OkHttpClient();
    client.setFollowRedirects(false);
    Request.Builder builder = new Request.Builder();
    builder.url("http://0.0.0.0:3000/hello");
    builder.header("host", "test.leanapp.cn");
    builder.header("x-forwarded-proto", "https");
    builder.get();

    Response response = client.newCall(builder.build()).execute();
    assertEquals(200, response.code());
    String responseStr = new String(response.body().bytes());
    assertEquals("<html><head><title>Hello World!</title></head>\n" + "<body><h1>Hello World!</h1></body></html>\n", responseStr);
  }

  @Test
  public void testRedirectionForCustomDomain() throws IOException {
    OkHttpClient client = new OkHttpClient();
    client.setFollowRedirects(false);
    Request.Builder builder = new Request.Builder();
    builder.url("http://0.0.0.0:3000/hello");
    builder.header("host", "my-domain.com");
    builder.header("x-forwarded-proto", "http");
    builder.get();

    Response response = client.newCall(builder.build()).execute();
    assertEquals(302, response.code());
    assertEquals("https://my-domain.com/hello", response.header("Location"));
  }

  @Test
  public void testNotRedirectionForCustomDomain() throws IOException {
    OkHttpClient client = new OkHttpClient();
    client.setFollowRedirects(false);
    Request.Builder builder = new Request.Builder();
    builder.url("http://0.0.0.0:3000/hello");
    builder.header("host", "my-domain.com");
    builder.header("x-forwarded-proto", "https");
    builder.get();

    Response response = client.newCall(builder.build()).execute();
    assertEquals(200, response.code());
    String responseStr = new String(response.body().bytes());
    assertEquals("<html><head><title>Hello World!</title></head>\n" + "<body><h1>Hello World!</h1></body></html>\n", responseStr);
  }

  @Override
  public void teardown() throws Exception {
    super.teardown();
    System.clearProperty("LEANCLOUD_APP_ENV");
    LeanEngine.setHttpsRedirectEnabled(false);
  }
}
