package cn.leancloud;

import com.avos.avoscloud.internal.impl.JavaAppConfiguration;

public class EngineAppConfiguration extends JavaAppConfiguration {

  private static EngineAppConfiguration instance;

  private String appEnv;

  private int port;

  public static EngineAppConfiguration instance(String applicationId, String clientKey,
      String masterKey) {
    synchronized (EngineAppConfiguration.class) {
      if (instance == null) {
        instance = new EngineAppConfiguration();
      }
    }
    instance.setApplicationId(applicationId);
    instance.setClientKey(clientKey);
    instance.setMasterKey(masterKey);
    instance.setAppEnv(instance.getEnvOrProperty("LEANCLOUD_APP_ENV"));
    instance.setPort(Integer.parseInt(instance.getEnvOrProperty("LEANCLOUD_APP_PORT")));
    return instance;
  }

  private EngineAppConfiguration() {

  }

  private void setAppEnv(String appEnv) {
    this.appEnv = appEnv;
  }

  private void setPort(int port) {
    this.port = port;
  }

  public String getAppEnv() {
    return appEnv;
  }

  public int getPort() {
    return port;
  }


}
