package cn.leancloud.leanengine_test;

import java.io.IOException;

import org.junit.Test;

import cn.leancloud.LeanEngine;

import com.avos.avoscloud.okhttp.OkHttpClient;
import com.avos.avoscloud.okhttp.Request;
import com.avos.avoscloud.okhttp.Response;

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
    builder.url("http://0.0.0.0:3000/1.1/functions/redirectionTest");
    builder.header("host", "test.leanapp.cn");
    builder.header("x-forwarded-proto", "http");
    builder.get();

    Response response = client.newCall(builder.build()).execute();
    System.out.println(response.code());
  }

  @Override
  public void teardown() throws Exception {
    super.teardown();
    System.clearProperty("LEANCLOUD_APP_ENV");
    LeanEngine.setHttpsRedirectEnabled(false);
  }
}
