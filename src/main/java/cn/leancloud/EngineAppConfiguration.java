package cn.leancloud;

import com.avos.avoscloud.AVOSServices;
import com.avos.avoscloud.internal.impl.JavaAppConfiguration;

class EngineAppConfiguration extends JavaAppConfiguration {

  private final String appEnv;

  private final int port;

  EngineAppConfiguration(String applicationId, String clientKey, String masterKey) {
    setApplicationId(applicationId);
    setClientKey(clientKey);
    setMasterKey(masterKey);
    appEnv = getEnvOrProperty("LEANCLOUD_APP_ENV");
    port = Integer.parseInt(getEnvOrProperty("LEANCLOUD_APP_PORT"));

    serviceHostMap.put(AVOSServices.STORAGE_SERVICE.toString(),
        System.getProperty("LEANCLOUD_API_SERVER"));
    serviceHostMap.put(AVOSServices.FUNCTION_SERVICE.toString(),
        System.getProperty("LEANCLOUD_API_SERVER"));
  }

  public void setLocalEngineCallEnabled(boolean enabled) {
    if (enabled) {
      serviceHostMap.put(AVOSServices.FUNCTION_SERVICE.toString(), "http://0.0.0.0:" + getPort());
    } else {
      serviceHostMap.put(AVOSServices.FUNCTION_SERVICE.toString(),
          System.getProperty("LEANCLOUD_API_SERVER"));
    }
  }

  public String getAppEnv() {
    return appEnv;
  }

  public int getPort() {
    return port;
  }

  private String getEnvOrProperty(String key) {
    String value = System.getenv(key);
    if (value == null) {
      value = System.getProperty(key);
    }
    return value;
  }

}
