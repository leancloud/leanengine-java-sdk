package cn.leancloud.leanengine_test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVUser;

import cn.leancloud.EngineFunction;
import cn.leancloud.EngineFunctionParam;

public class AllFunctions {
  @EngineFunction
  public static String hello(String name) {
    return "hello " + name;
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
}
