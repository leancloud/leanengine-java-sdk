package cn.leancloud;

import com.avos.avoscloud.internal.impl.JavaAppConfiguration;

public class EngineAppConfiguration extends JavaAppConfiguration {

  private String hookKey;
  private int port;
  private String appEnv;

  EngineAppConfiguration() {
    setApplicationId(getEnvOrProperty("LEANCLOUD_APP_ID"));
    setClientKey(getEnvOrProperty("LEANCLOUD_APP_KEY"));
    setMasterKey(getEnvOrProperty("LEANCLOUD_APP_MASTER_KEY"));
    hookKey = getEnvOrProperty("LEANCLOUD_APP_HOOK_KEY");
    port = Integer.parseInt(getEnvOrProperty("LEANCLOUD_APP_PORT"));
    appEnv = getEnvOrProperty("LEANCLOUD_APP_ENV");

  }

  EngineAppConfiguration(String applicationId, String clientKey, String masterKey, String hookKey, int port, String appEnv) {
    this.setApplicationId(applicationId);
    this.setClientKey(clientKey);
    this.setMasterKey(masterKey);
    this.hookKey = hookKey;
    this.port = port;
    this.appEnv = appEnv;
  }

  public int getPort() {
    return port;
  }

  String getHookKey() {
    return hookKey;
  }

  public String getAppEnv() {
    return appEnv;
  }

  void setPort(int port) {
    this.port = port;
  }

}
