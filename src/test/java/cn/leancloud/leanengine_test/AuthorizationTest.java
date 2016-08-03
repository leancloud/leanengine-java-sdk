package cn.leancloud.leanengine_test;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;

import com.avos.avoscloud.okhttp.MediaType;
import com.avos.avoscloud.okhttp.Request;
import com.avos.avoscloud.okhttp.RequestBody;
import com.avos.avoscloud.okhttp.Response;

import cn.leancloud.LeanEngine;

public class AuthorizationTest extends EngineBasicTest {

  @Before
  public void setUp() throws Exception {
    super.setUp();
    LeanEngine.register(AllEngineFunctions.class);
  }

  @Test
  public void test_ok() throws Exception {
    String content = "{}";
    Request.Builder builder = this.getBasicTestRequestBuilder();
    builder.url("http://localhost:3000/1.1/functions/foo");
    builder.post(RequestBody.create(MediaType.parse(getContentType()), content));
    Response response = client.newCall(builder.build()).execute();
    assertEquals(HttpServletResponse.SC_OK, response.code());
    assertEquals("{\"result\":\"bar\"}", new String(response.body().bytes()));
  }

  @Test
  public void test_no_appId_or_appKey() throws IOException {
    String content = "{}";
    Request.Builder builder = new Request.Builder();
    builder.addHeader("X-LC-Id", getAppId());
    builder.addHeader("Content-Type", getContentType());
    builder.url("http://localhost:3000/1.1/functions/foo");
    builder.post(RequestBody.create(MediaType.parse(getContentType()), content));
    Response response = client.newCall(builder.build()).execute();
    assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.code());
    assertEquals("{\"code\":}", new String(response.body().bytes()));
    assertEquals("{\"code\":401,\"error\":\"Unauthorized.\"}", new String(response.body().bytes()));

  }

  @Test
  public void test_mismatching() throws IOException {
    String content = "{}";
    Request.Builder builder = new Request.Builder();
    builder.addHeader("X-LC-Id", getAppId());
    builder.addHeader("X-LC-Key", "errorKey");
    builder.addHeader("Content-Type", getContentType());
    builder.url("http://localhost:3000/1.1/functions/foo");
    builder.post(RequestBody.create(MediaType.parse(getContentType()), content));
    Response response = client.newCall(builder.build()).execute();
    assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.code());
    assertEquals("{\"code\":401,\"error\":\"Unauthorized.\"}", new String(response.body().bytes()));
  }

  @Test
  public void test_masterKey() throws IOException {
    String content = "{}";
    Request.Builder builder = new Request.Builder();
    builder.addHeader("X-LC-Id", getAppId());
    builder.addHeader("x-uluru-master-key", getMasterKey());
    builder.addHeader("Content-Type", getContentType());
    builder.url("http://localhost:3000/1.1/functions/foo");
    builder.post(RequestBody.create(MediaType.parse(getContentType()), content));
    Response response = client.newCall(builder.build()).execute();
    assertEquals(HttpServletResponse.SC_OK, response.code());
    assertEquals("{\"result\":\"bar\"}", new String(response.body().bytes()));
  }

  @Test
  public void test_masterKey2() throws IOException {
    String content = "{}";
    Request.Builder builder = new Request.Builder();
    builder.addHeader("X-LC-Id", getAppId());
    builder.addHeader("X-LC-key", getMasterKey() + ",master");
    builder.addHeader("Content-Type", getContentType());
    builder.url("http://localhost:3000/1.1/functions/foo");
    builder.post(RequestBody.create(MediaType.parse(getContentType()), content));
    Response response = client.newCall(builder.build()).execute();
    assertEquals(HttpServletResponse.SC_OK, response.code());
    assertEquals("{\"result\":\"bar\"}", new String(response.body().bytes()));
  }

  @Test
  public void test_masterKey3() throws IOException {
    String content = "{}";
    Request.Builder builder = new Request.Builder();
    builder.addHeader("X-LC-Id", getAppId());
    builder.addHeader("X-LC-key", getAppKey() + ",master");
    builder.addHeader("Content-Type", getContentType());
    builder.url("http://localhost:3000/1.1/functions/foo");
    builder.post(RequestBody.create(MediaType.parse(getContentType()), content));
    Response response = client.newCall(builder.build()).execute();
    assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.code());
    assertEquals("{\"code\":401,\"error\":\"Unauthorized.\"}", new String(response.body().bytes()));
  }

  @Test
  public void test_sign() throws IOException {
    String content = "{}";
    Request.Builder builder = new Request.Builder();
    builder.addHeader("X-LC-Id", getAppId());
    builder.addHeader("x-lc-sig", "0c7d51b5b568bc9a25b2658a390692b0,1389085779854");
    builder.addHeader("Content-Type", getContentType());
    builder.url("http://localhost:3000/1.1/functions/foo");
    builder.post(RequestBody.create(MediaType.parse(getContentType()), content));
    Response response = client.newCall(builder.build()).execute();
    assertEquals(HttpServletResponse.SC_OK, response.code());
    assertEquals("{\"result\":\"bar\"}", new String(response.body().bytes()));
  }

  @Test
  public void test_sign_master() throws IOException {
    String content = "{}";
    Request.Builder builder = new Request.Builder();
    builder.addHeader("X-LC-Id", getAppId());
    builder.addHeader("x-lc-sig", "3d609239e19dac116263fe182e97fc0f,1389085779854,master");
    builder.addHeader("Content-Type", getContentType());
    builder.url("http://localhost:3000/1.1/functions/foo");
    builder.post(RequestBody.create(MediaType.parse(getContentType()), content));
    Response response = client.newCall(builder.build()).execute();
    assertEquals(HttpServletResponse.SC_OK, response.code());
    assertEquals("{\"result\":\"bar\"}", new String(response.body().bytes()));
  }

  @Test
  public void test_sign_mismatching() throws IOException {
    String content = "{}";
    Request.Builder builder = new Request.Builder();
    builder.addHeader("X-LC-Id", getAppId());
    builder.addHeader("x-lc-sig", "11111111111111111111111111111111,1389085779854");
    builder.addHeader("Content-Type", getContentType());
    builder.url("http://localhost:3000/1.1/functions/foo");
    builder.post(RequestBody.create(MediaType.parse(getContentType()), content));
    Response response = client.newCall(builder.build()).execute();
    assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.code());
    assertEquals("{\"code\":401,\"error\":\"Unauthorized.\"}", new String(response.body().bytes()));
  }

}
