package cn.leancloud.leanengine_test;

import cn.leancloud.EngineFunction;
import cn.leancloud.EngineFunctionParam;
import cn.leancloud.EngineRequestContext;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AllEngineFunctions {

  @EngineFunction
  public static String foo() {
    return "bar";
  }

  @EngineFunction
  public static String hello(@EngineFunctionParam("name") String name) {
    if (null != name) {
      return "hello " + name;
    } else {
      return "hello";
    }
  }

  @EngineFunction("ping")
  public static AVObject ping(@EngineFunctionParam("ts") long ts) {
    return AVUser.getCurrentUser();
  }

  @EngineFunction("simpleObject")
  public static String simple(@EngineFunctionParam("obj") AVObject obj) throws AVException {
    if (obj.getInt("int") > 100) {
      return "failure";
    } else {
      obj.put("int", obj.getInt("int") + 100);
      obj.save();
      return "success";
    }
  }

  @EngineFunction("complexObject")
  public static Map<String, Object> complexObject(@EngineFunctionParam("foo") String foo,
                                                  @EngineFunctionParam("array") int[] array, @EngineFunctionParam("avobject") AVObject object,
                                                  @EngineFunctionParam("list") List<AVObject> list) {
    Map<String, Object> result = new HashMap<String, Object>();
    result.put("foo", foo);
    result.put("array", array);
    result.put("avobject", object);
    result.put("list", list);
    return result;
  }

  @EngineFunction("query")
  public static List<AVUser> query() throws AVException {
    List<AVUser> result = AVUser.getQuery().find();
    return result;
  }

  @EngineFunction("currentUserInfo")
  public static Map<String, Object> getCurrentUserInfo() {
    Map<String, Object> result = new HashMap<>();
    AVUser user = AVUser.getCurrentUser();
    if (user == null) {
      return result;
    }
    result.put("objectId", user.getObjectId());
    result.put("username", user.getUsername());
    result.put("sessionToken", user.getSessionToken());
    return result;
  }

  @EngineFunction("cookieTest")
  public static AVUser cookieTest() throws AVException {
    return AVUser.getCurrentUser();
  }

  @EngineFunction("remoteAddress")
  public static String remoteAddress() throws AVException {
    return EngineRequestContext.getRemoteAddress();
  }

  @EngineFunction("metadata")
  public static Map<String, Object> getMetadata() {
    Map<String, Object> result = new HashMap<>();
    result.put("remoteAddress", EngineRequestContext.getRemoteAddress());
    result.put("sessionToken", EngineRequestContext.getSessionToken());
    return result;
  }

  @EngineFunction()
  public static AVUser errorCode() throws AVException {
    return AVUser.logIn("NoThisUser", "lalala");
  }

  @EngineFunction()
  public static String customErrorCode() throws AVException {
    throw new AVException(123, "custom error message");
  }

  @EngineFunction()
  public static String uncaughtError() {
    return new ArrayList<String>().get(0);
  }

}
