package cn.leancloud.leanengine_test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jetty.server.Server;
import org.junit.Before;
import org.junit.Test;

import cn.leancloud.EngineSessionCookie;
import cn.leancloud.LeanEngine;

import com.avos.avoscloud.AVCloud;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVOSCloud;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.AVUtils;
import com.avos.avoscloud.okhttp.MediaType;
import com.avos.avoscloud.okhttp.OkHttpClient;
import com.avos.avoscloud.okhttp.Request;
import com.avos.avoscloud.okhttp.RequestBody;
import com.avos.avoscloud.okhttp.Response;
import com.avos.avoscloud.okio.BufferedSink;

public class FunctionTest extends EngineBasicTest {

  private static Server server;
  private static int port = 3000;

  @Before
  public void setUp() throws Exception {
    super.setUp();
    LeanEngine.register(AllEngineFunctions.class);
  }

  @Test
  public void testHello() throws Exception {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("name", "张三");
    Object result = AVCloud.callFunction("hello", params);
    assertEquals("hello 张三", result);
  }

  @Test
  public void testAVCloudFunction() throws Exception {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("ts", 123);
    AVUser.logOut();
    Map<String, Object> result = AVCloud.callFunction("ping", params);
    assertTrue(result == null);
  }

  @Test
  public void testRPCCall() throws Exception {
    AVUser registerUser = new AVUser();
    registerUser.setUsername(AVUtils.getRandomString(10) + System.currentTimeMillis());
    registerUser.setPassword(AVUtils.getRandomString(10));
    registerUser.signUp();
    AVUser u = AVCloud.rpcFunction("ping", 123);
    assertEquals(registerUser.getObjectId(), (u.getObjectId()));
  }

  @Test
  public void testSimpleObject() throws Exception {
    AVObject obj = new AVObject("rpcTest");
    obj.put("int", 12);
    obj.save();

    String result = AVCloud.rpcFunction("simpleObject", obj);
    assertEquals("success", result);

    obj.put("int", 3000);
    obj.save();
    result = AVCloud.rpcFunction("simpleObject", obj);
    assertEquals("failure", result);
  }

  @Test
  public void testComplexObject() throws Exception {
    Map<String, Object> params = new HashMap<String, Object>();
    int[] array = new int[3];
    for (int i = 0; i < array.length; i++) {
      array[i] = i + 123;
    }
    AVObject hello = new AVObject("hello");
    hello.put("int", 123);
    hello.save();

    List<AVObject> list = new ArrayList<AVObject>(2);
    list.add(hello);
    list.add(hello);
    params.put("array", array);
    params.put("avobject", hello);
    params.put("foo", "bar");
    params.put("list", list);

    Map<String, Object> result = AVCloud.rpcFunction("complexObject", params);
    assertEquals("bar", result.get("foo"));
    assertEquals(hello, result.get("avobject"));
    assertEquals(list, result.get("list"));
    for (int i = 0; i < array.length; i++) {
      assertEquals(array[i], ((List) result.get("array")).get(i));
    }
  }

  @Test
  public void testQueryResult() throws AVException {
    List<Map> result = AVCloud.callFunction("query", null);
    for (Map m : result) {
      assertNotNull(m.get("username"));
    }
    List<AVUser> userResults = AVCloud.rpcFunction("query", null);
    for (AVUser u : userResults) {
      assertNotNull(u.getUsername());
    }
  }

  @Test
  public void testCookieUser() throws AVException, URISyntaxException, IOException {
    AVOSCloud.initialize("uu2P5gNTxGhjyaJGAPPnjCtJ-gzGzoHsz", "j5lErUd6q7LhPD8CXhfmA2Rg",
        "atXAmIVlQoBDBLqumMgzXhcY");
    AVUser u = new AVUser();
    u.setUsername("spamUser");
    u.setPassword("123123123");
    try {
      u.signUp();
    } catch (AVException e) {
      u = AVUser.logIn("spamUser", "123123123");
    }
    String sessionToken = u.getSessionToken();
    u.logOut();
    String cookieValue = EngineSessionCookie.encodeUser(u);
    OkHttpClient client = new OkHttpClient();
    CookieManager cookieManager = new CookieManager();
    cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
    client.setCookieHandler(cookieManager);
    Request.Builder builder = this.getBasicTestRequestBuilder();
    List<String> values =
        new ArrayList<>(Arrays.asList("avos:sess=" + cookieValue, "avos:sess.sig="
            + EngineSessionCookie.getCookieSign("avos:sess", cookieValue, this.secret)));
    Map<String, List<String>> cookies = new HashMap<>();
    cookies.put("Set-Cookie", values);
    client.getCookieHandler().put(new URI("http://0.0.0.0:3000"), cookies);

    ((CookieManager) client.getCookieHandler()).put(new URI("http://0.0.0.0:3000"), cookies);
    builder.url("http://0.0.0.0:3000/1.1/call/cookieTest");
    builder.post(new RequestBody() {

      @Override
      public void writeTo(BufferedSink sink) throws IOException {
        // TODO Auto-generated method stub

      }

      @Override
      public MediaType contentType() {
        // TODO Auto-generated method stub
        return null;
      }
    });
    Request request = builder.build();
    Response response = client.newCall(request).execute();
    AVUser returnUser =
        (AVUser) AVCloud.convertCloudResponse((new String(response.body().bytes())));
    assertEquals(sessionToken, returnUser.getSessionToken());
    // 现在检查没有cookie的情况下，返回空
    client.setCookieHandler(null);
    builder = this.getBasicTestRequestBuilder();
    builder.url("http://0.0.0.0:3000/1.1/call/cookieTest");
    builder.post(new RequestBody() {

      @Override
      public void writeTo(BufferedSink sink) throws IOException {
        // TODO Auto-generated method stub

      }

      @Override
      public MediaType contentType() {
        // TODO Auto-generated method stub
        return null;
      }
    });
    request = builder.build();
    response = client.newCall(request).execute();
    String responseStr = new String(response.body().bytes());
    assertEquals("{\"result\":null}", responseStr);
  }
}
