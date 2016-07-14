package cn.leancloud.leanengine_test;

import java.util.EnumSet;

import javax.servlet.DispatcherType;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.FilterMapping;
import org.eclipse.jetty.servlet.ServletHandler;
import org.junit.After;
import org.junit.Before;

import cn.leancloud.HttpsRequestRedirectFilter;
import cn.leancloud.LeanEngine;

import com.avos.avoscloud.AVOSCloud;
import com.avos.avoscloud.internal.impl.EngineRequestSign;

public class EngineBasicTest {

  private static Server server;
  private static int port = 3000;

  @Before
  public void setUp() throws Exception {
    System.setProperty("LEANCLOUD_APP_PORT", "3000");
    AVOSCloud.initialize("uu2P5gNTxGhjyaJGAPPnjCtJ-gzGzoHsz", "j5lErUd6q7LhPD8CXhfmA2Rg",
        "atXAmIVlQoBDBLqumMgzXhcY");
    LeanEngine.setLocalEngineCallEnabled(true);
    EngineRequestSign.instance().setUserMasterKey(true);
    AVOSCloud.setDebugLogEnabled(true);

    server = new Server(port);
    ServletHandler handler = new ServletHandler();
    server.setHandler(handler);
    handler.addServletWithMapping(LeanEngine.class, "/1.1/functions/*");
    FilterMapping fmap = new FilterMapping();
    handler.addFilterWithMapping(HttpsRequestRedirectFilter.class, "/*",
        EnumSet.of(DispatcherType.INCLUDE, DispatcherType.REQUEST));
    server.start();
  }

  @After
  public void teardown() throws Exception {
    server.stop();
  }

}
