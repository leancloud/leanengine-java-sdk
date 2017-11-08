package cn.leancloud;

import com.avos.avoscloud.internal.impl.JavaAppConfiguration;

public class EngineAppConfiguration extends JavaAppConfiguration {

  private final String appEnv;

  private final int port;

  EngineAppConfiguration(String applicationId, String clientKey, String masterKey) {
    setApplicationId(applicationId);
    setClientKey(clientKey);
    setMasterKey(masterKey);
    appEnv = getEnvOrProperty("LEANCLOUD_APP_ENV");
    port = Integer.parseInt(getEnvOrProperty("LEANCLOUD_APP_PORT"));
  }

  public String getAppEnv() {
    return appEnv;
  }

  public int getPort() {
    return port;
  }


}
