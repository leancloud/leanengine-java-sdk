package cn.leancloud.leanengine_test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.junit.Test;

import com.avos.avoscloud.AVCloud;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVOSCloud;
import com.avos.avoscloud.AVOSServices;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.AVUtils;
import com.avos.avoscloud.PaasClient;
import com.avos.avoscloud.internal.impl.EngineRequestSign;

public class FunctionIntegrationTest extends TestCase {

  public static void setLocalEngineAddress() {
    try {
      Method setFunctionUrl =
          PaasClient.class.getDeclaredMethod("setServiceHost", AVOSServices.class, String.class);
      setFunctionUrl.setAccessible(true);
      setFunctionUrl.invoke(null, AVOSServices.FUNCTION_SERVICE, "http://0.0.0.0:3000");
    } catch (NoSuchMethodException e) {
      e.printStackTrace();
    } catch (SecurityException e) {
      e.printStackTrace();
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    } catch (IllegalArgumentException e) {
      e.printStackTrace();
    } catch (InvocationTargetException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void setUp() {
    AVOSCloud.initialize("uu2P5gNTxGhjyaJGAPPnjCtJ-gzGzoHsz", "j5lErUd6q7LhPD8CXhfmA2Rg",
        "atXAmIVlQoBDBLqumMgzXhcY");
    setLocalEngineAddress();
    AVOSCloud.setDebugLogEnabled(true);
  }

  @Test
  public void testHello() throws Exception {
    AVCloud.callFunction("hello", null);
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
}
