package cn.leancloud.leanengine_test;

import com.avos.avoscloud.okhttp.MediaType;
import com.avos.avoscloud.okhttp.Request;
import com.avos.avoscloud.okhttp.RequestBody;
import com.avos.avoscloud.okhttp.Response;
import org.junit.Test;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class AuthorizationTest extends EngineBasicTest {

  @Test
  public void test_ok() {
    String response = request("/1.1/functions/foo", getAppKey(), null, null, "{}", 200);
    assertEquals("{\"result\":\"bar\"}", response);
  }

  @Test
  public void test_no_appKey() {
    String response = request("/1.1/functions/foo", null, null, null, "{}", 401);
    assertEquals("{\"code\":401,\"error\":\"Unauthorized.\"}", response);
  }

  @Test
  public void test_mismatching() {
    request("/1.1/functions/foo", "invalidAppKey", null, null, "{}", 401);
  }

  @Test
  public void test_masterKey() {
    String content = "{}";
    Request.Builder builder = new Request.Builder();
    builder.addHeader("X-LC-Id", getAppId());
    builder.addHeader("x-uluru-master-key", getMasterKey());
    builder.addHeader("Content-Type", getContentType());
    builder.url("http://localhost:3000/1.1/functions/foo");
    builder.post(RequestBody.create(MediaType.parse(getContentType()), content));
    try {
      Response response = client.newCall(builder.build()).execute();
      assertEquals(HttpServletResponse.SC_OK, response.code());
      assertEquals("{\"result\":\"bar\"}", new String(response.body().bytes()));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  public void test_masterKey2() {
    request("/1.1/functions/foo", getMasterKey() + ",master", null, null, "{}", 200);
  }

  @Test
  public void test_masterKey3() {
    request("/1.1/functions/foo", getAppKey() + ",master", null, null, "{}", 401);
  }

  @Test
  public void test_hookKey() {
    String content = "{\"object\": {\"content\":\"Hello World!\"}}";
    String response = request("/1.1/functions/Todo/beforeSave", getAppKey(), null, getHookKey(), content, 200);
    assertEquals("{\"content\":\"Hello World!\"}", response);
  }

  @Test
  public void test_hookKey_mismatching() {
    String content = "{\"object\": {\"content\":\"Hello World!\"}}";
    request("/1.1/functions/Todo/beforeSave", getAppKey(), null, "invalidHookKey", content, 401);
  }

  @Test
  public void test_sign() {
    request("/1.1/functions/foo", null, "4aaee8dee8821173931f03f7efd7067a,1389085779854", null, "{}", 200);
  }

  @Test
  public void test_sign_master() {
    request("/1.1/functions/foo", null, "c9bd13ecd484736ce550d1a2ff9dbc0f,1389085779854,master", null, "{}", 200);
  }

  @Test
  public void test_sign_mismatching() {
    request("/1.1/functions/foo", null, "11111111111111111111111111111111,1389085779854", null, "{}", 401);
  }

}
