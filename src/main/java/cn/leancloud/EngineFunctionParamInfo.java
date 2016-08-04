package cn.leancloud;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVUtils;

public class EngineFunctionParamInfo {
  final String name;
  final Class type;

  public EngineFunctionParamInfo(Class type, String name) {
    this.name = name;
    this.type = type;
  }

  public String getName() {
    return name;
  }

  public Class getType() {
    return type;
  }

  public Object parseParams(String content) {
    if (AVObject.class.isAssignableFrom(type)) {
      return AVUtils.parseObjectFromMap(JSON.parseObject(content));
    } else {
      if (Map.class.isAssignableFrom(type)) {
        return parseParams((Map) JSON.parseObject(content, type));
      } else if (Collection.class.isAssignableFrom(type)) {
        return parseParams((Collection) JSON.parseObject(content, type));
      } else {
        return JSON.parseObject(content, type);
      }
    }
  }

  public Collection parseParams(Collection collection) {
    List result = new LinkedList();
    for (Object o : collection) {
      if (o instanceof Map) {
        result.add(parseParams((Map) o));
      } else if (o instanceof Collection) {
        result.add(parseParams((Collection) o));
      } else {
        result.add(o);
      }
    }
    return result;
  }

  public Object parseParams(Map<String, Object> map) {
    if (map != null && map.containsKey("className") && map.containsKey("__type")
        && "Object".equals(map.get("__type"))) {
      // 这肯定是一个AVObject吧
      AVObject object = AVUtils.newAVObjectByClassName((String) map.get("className"));
      AVUtils.copyPropertiesFromMapToAVObject(map, object);
      return object;
    } else if (map != null) {
      HashMap result = new HashMap();
      for (Map.Entry<String, Object> entry : map.entrySet()) {
        Object parsedValue = null;
        if (entry.getValue() instanceof Map) {
          parsedValue = parseParams((Map) entry.getValue());
        } else if (entry.getValue() instanceof Collection) {
          parsedValue = parseParams((Collection) entry.getValue());
        } else {
          parsedValue = entry.getValue();
        }
        result.put(entry.getKey(), parsedValue);
      }
      return result;
    }
    return null;
  }
}
