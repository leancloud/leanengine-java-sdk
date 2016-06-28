package cn.leancloud;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EngineRequestContext {

  private static final String UPDATED_KEYS = "_updatedKeys";
  static ThreadLocal<Map<String, Object>> localMeta = new ThreadLocal<Map<String, Object>>();

  public static Map<String, Object> getMeta() {
    return localMeta.get();
  }

  public static List<String> getUpdateKeys() {
    Map<String, Object> meta = getMeta();
    if (meta != null && meta.containsKey(UPDATED_KEYS)) {
      return (List) meta.get(UPDATED_KEYS);
    }
    return null;
  }

  protected static void setMeta(Map<String, Object> meta) {
    localMeta.set(meta);
  }

  protected static void parseMetaData(Map<String, Object> objectProperties) {
    System.out.println(objectProperties);
    if (objectProperties.containsKey(UPDATED_KEYS)) {
      Map<String, Object> meta = new HashMap<String, Object>();
      Object updateKeys = objectProperties.remove(UPDATED_KEYS);
      meta.put(UPDATED_KEYS, updateKeys);
      setMeta(meta);
    }
  }
}
