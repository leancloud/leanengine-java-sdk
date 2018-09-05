package cn.leancloud.leanengine_test;

import cn.leancloud.LeanEngine;
import com.alibaba.fastjson.JSON;
import com.avos.avoscloud.*;
import com.avos.avoscloud.internal.impl.DefaultAVUserCookieSign;
import com.avos.avoscloud.okhttp.*;
import com.avos.avoscloud.okio.BufferedSink;
import org.eclipse.jetty.server.Server;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.*;
import java.util.*;

import static org.junit.Assert.*;

public class FunctionTest extends EngineBasicTest {

  private static Server server;
  private static int port = 3000;

  @Before
  public void setUp() throws Exception {
    super.setUp();
    LeanEngine.register(AllEngineFunctions.class);
  }

  @Test
  public void test_ping() throws IOException {
    Request.Builder builder = new Request.Builder();
    builder.url("http://localhost:3000/__engine/1/ping");
    builder.get();
    Response response = client.newCall(builder.build()).execute();
    assertEquals(HttpServletResponse.SC_OK, response.code());
    String body = new String(response.body().bytes());
    assertTrue(body.indexOf("runtime") > 0);
    assertTrue(body.indexOf("version") > 0);
  }

  @Test
  public void testHello() throws Exception {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("name", "张三");
    Object result = AVCloud.callFunction("hello", params);
    assertEquals("hello 张三", result);
  }

  @Test
  public void testHelloWithWrongParam() throws  Exception {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("name1ss", "张三");
    Object result = AVCloud.callFunction("hello", params);
    assertEquals("hello", result);
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

    Map<String, Object> params = new HashMap<String, Object>();
    params.put("ts", 123);
    AVUser u = AVCloud.rpcFunction("ping", params);
    assertEquals(registerUser.getObjectId(), (u.getObjectId()));
  }

  @Test
  public void testSimpleObject() throws Exception {
    Map<String, AVObject> rpcTestMap = new HashMap<String, AVObject>();

    AVObject obj = new AVObject("rpcTest");
    obj.put("int", 12);
    obj.save();
    rpcTestMap.put("obj", obj);

    String result = AVCloud.rpcFunction("simpleObject", rpcTestMap);
    assertEquals("success", result);

    obj.put("int", 3000);
    obj.save();
    result = AVCloud.rpcFunction("simpleObject", rpcTestMap);
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
  public void testGetCurrentUserInfo() {
    Map<String, Object> result = run("currentUserInfo", null, "0hgr13u12tmgyv4x594682sv5");
    assertTrue(result.containsKey("objectId"));
    assertTrue(result.containsValue("zhangsan"));

    result = run("currentUserInfo", null, null);
    assertTrue(result.isEmpty());

    result = run("currentUserInfo", null, "invalidSessionToken");
    assertTrue(result.isEmpty());
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
    DefaultAVUserCookieSign sign = new DefaultAVUserCookieSign(secret, 3000);
    Cookie userCookie = sign.encodeUser(u);
    Cookie cookieSign = sign.getCookieSign(u);
    OkHttpClient client = new OkHttpClient();
    CookieManager cookieManager = new CookieManager();
    cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
    client.setCookieHandler(cookieManager);
    Request.Builder builder = this.getBasicTestRequestBuilder();
    List<String> values = new ArrayList<String>(Arrays.asList("avos.sess=" + userCookie.getValue(),
        "avos.sess.sig=" + cookieSign.getValue()));
    Map<String, List<String>> cookies = new HashMap<String, List<String>>();
    cookies.put("Set-Cookie", values);
    client.getCookieHandler().put(new URI("http://0.0.0.0:3000"), cookies);

    ((CookieManager) client.getCookieHandler()).put(new URI("http://0.0.0.0:3000"), cookies);
    builder.url("http://0.0.0.0:3000/1.1/call/cookieTest");
    builder.post(new EmptyRequestBody());
    Request request = builder.build();
    Response response = client.newCall(request).execute();
    AVUser returnUser =
        (AVUser) AVCloud.convertCloudResponse((new String(response.body().bytes())));
    assertEquals(sessionToken, returnUser.getSessionToken());
    // 现在检查没有cookie的情况下，返回空
    client.setCookieHandler(null);
    builder = this.getBasicTestRequestBuilder();
    builder.url("http://0.0.0.0:3000/1.1/call/cookieTest");
    builder.post(new EmptyRequestBody());
    request = builder.build();
    response = client.newCall(request).execute();
    String responseStr = new String(response.body().bytes());
    assertEquals("{\"result\":null}", responseStr);
  }

  @Test
  public void testMetadata() throws AVException, IOException {
    Map<String, Object> result = run("metadata", null, "mySessionToken");
    assertTrue(result.containsKey("remoteAddress"));
    assertEquals("mySessionToken", result.get("sessionToken"));

    result = run("metadata", null, null);
    assertNull(result.get("sessionToken"));
  }

  private Map<String, Object> run(String funcName, Map<String, Object> params, String sessionToken) {
    try {
      Request.Builder builder = new Request.Builder();
      builder.url("http://localhost:3000/1.1/functions/" + funcName);
      builder.header("x-lc-id", getAppId());
      builder.header("x-lc-key", getAppKey());
      if (sessionToken != null) {
        builder.header("x-lc-session", sessionToken);
      }
      builder.post(new EmptyRequestBody());
      Response response = client.newCall(builder.build()).execute();
      assertEquals(HttpServletResponse.SC_OK, response.code());
      String body = new String(response.body().bytes());
      Map<String, Object> result =  JSON.parseObject(body, Map.class);
      return (Map<String, Object>) result.get("result");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  public void testErrorCode() throws AVException {
    try {
      Map<String, Object> result = AVCloud.callFunction("errorCode", null);
      fail();
    } catch (AVException e) {
      assertEquals(211, e.getCode());
      assertEquals("Could not find user.", e.getMessage());
    }
  }

  @Test
  public void testCustomErrorCode() throws AVException {
    try {
      Map<String, Object> result = AVCloud.callFunction("customErrorCode", null);
      fail();
    } catch (AVException e) {
      assertEquals(123, e.getCode());
      assertEquals("custom error message", e.getMessage());
    }
  }

  @Test
  public void testUncaughtError() throws AVException {
    try {
      Map<String, Object> result = AVCloud.callFunction("uncaughtError", null);
      fail();
    } catch(AVException e) {
      assertEquals(1, e.getCode());
      assertEquals("Index: 0, Size: 0", e.getMessage());
    }
  }
}

class EmptyRequestBody extends RequestBody {

  @Override
  public void writeTo(BufferedSink sink) throws IOException {

  }

  @Override
  public MediaType contentType() {
    return null;
  }
}
