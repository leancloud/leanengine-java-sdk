package com.avos.avoscloud.internal.impl;

import java.util.HashMap;
import java.util.Map;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVOSServices;
import com.avos.avoscloud.FunctionCallback;
import com.avos.avoscloud.internal.AppRouter;

import cn.leancloud.EngineAppConfiguration;

public class EnvAppRouter extends AppRouter {

  private boolean isLocalEngineCall = false;
  private EngineAppConfiguration appConf;

  public EnvAppRouter(EngineAppConfiguration appConf) {
    this.appConf = appConf;
  }

  @Override
  protected void fetchServerHosts(boolean sync,
      final FunctionCallback<Map<AVOSServices, String>> cb) {
    Map<AVOSServices, String> result = new HashMap<AVOSServices, String>();
    String appEnv = appConf.getAppEnv();
    if ("stage".equals(appEnv) || "production".equals(appEnv)) {
      result.put(AVOSServices.API, System.getProperty("LEANCLOUD_API_SERVER"));
      result.put(AVOSServices.PUSH, System.getProperty("LEANCLOUD_API_SERVER"));
      result.put(AVOSServices.RTM, System.getProperty("LEANCLOUD_API_SERVER"));
      result.put(AVOSServices.ENGINE, System.getProperty("LEANCLOUD_API_SERVER"));
      result.put(AVOSServices.STATS, System.getProperty("LEANCLOUD_API_SERVER"));
      cb.done(result, null);
      return;
    }

    DefaultAppRouter.instance().fetchServerHosts(sync,
        new FunctionCallback<Map<AVOSServices, String>>() {

      @Override
      public void done(Map<AVOSServices, String> hosts, AVException e) {
        if (e != null) {
          cb.done(null, e);
          return;
        }
        if (isLocalEngineCall) {
          hosts.put(AVOSServices.ENGINE, "http://0.0.0.0:" + appConf.getPort());
        }
        cb.done(hosts, null);
      }
    });
  }

  public void setLocalEngineCallEnabled(boolean enabled) {
    isLocalEngineCall  = enabled;
  }

}
