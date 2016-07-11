package com.avos.avoscloud.internal.impl;

import com.avos.avoscloud.AVOSServices;

public class EngineAppConfiguration extends JavaAppConfiguration {
  public static EngineAppConfiguration instance() {
    synchronized (EngineAppConfiguration.class) {
      if (instance == null) {
        instance = new EngineAppConfiguration();
      }
    }
    return instance;
  }

  protected EngineAppConfiguration() {}

  private static EngineAppConfiguration instance;

  @Override
  protected void setEnv() {
    serviceHostMap
        .put(AVOSServices.STORAGE_SERVICE.toString(), System.getProperty("LC_API_SERVER"));
  }
}
