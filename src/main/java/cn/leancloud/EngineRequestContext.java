package cn.leancloud;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 在云代码函数中获取请求相关的额外属性
 * 
 * @author lbt05
 *
 */
public class EngineRequestContext {

  private static final String UPDATED_KEYS = "_updatedKeys";
  static ThreadLocal<Map<String, Object>> localMeta = new ThreadLocal<Map<String, Object>>();

  public static Map<String, Object> getMeta() {
    return localMeta.get();
  }

  /**
   * 在 beforeUpdate 函数中调用可以查看 avobject 的哪些属性被更新了
   * 
   * @return
   */
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
    if (objectProperties.containsKey(UPDATED_KEYS)) {
      Map<String, Object> meta = new HashMap<String, Object>();
      Object updateKeys = objectProperties.remove(UPDATED_KEYS);
      meta.put(UPDATED_KEYS, updateKeys);
      setMeta(meta);
    }
  }
}
