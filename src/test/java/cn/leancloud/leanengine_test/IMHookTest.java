package cn.leancloud.leanengine_test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jetty.server.Server;
import org.junit.Before;
import org.junit.Test;

import com.avos.avoscloud.AVCloud;
import com.avos.avoscloud.AVException;

import cn.leancloud.LeanEngine;

public class IMHookTest extends EngineBasicTest {

  private static Server server;
  private static int port = 3000;

  @Before
  public void setUp() throws Exception {
    super.setUp();
    LeanEngine.register(AllIMHook.class);
  }

  @Test
  public void testMessageReceived() throws AVException {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("fromPeer", "123");
    params.put("convId", "456");
    params.put("toPeerIds", "01,11");
    params.put("transient", true);
    params.put("content", "shit from Mars");
    params.put("receipt", true);
    params.put("timestamp", 123123123l);
    params.put("sourceIP", "123.123.123.123");

    Map<String, Object> result = AVCloud.callFunction("_messageReceived", params);
    assertTrue((Boolean) result.get("drop"));
  }

  @Test
  public void testReceiversOffline() throws AVException {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("fromPeer", "123");
    params.put("convId", "456");
    params.put("content", "shit from Mars");
    params.put("timestamp", 123123123l);
    params.put("offlinePeers", Arrays.asList("12", "123"));
    Map<String, Object> result = AVCloud.callFunction("_receiversOffline", params);
    assertEquals("shit from Mars", ((Map<String, Object>) result.get("pushMessage")).get("alert"));
  }

  @Test
  public void testConversationStart() throws AVException {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("initBy", "lbt05");
    params.put("members", Arrays.asList("12", "123"));
    Map<String, Object> result = AVCloud.callFunction("_conversationStart", params);
    assertTrue((Boolean) result.get("reject"));
    assertEquals(9890, result.get("code"));
  }

  @Test
  public void testConversationStarted() throws AVException {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("initBy", "lbt05");
    params.put("members", Arrays.asList("12", "123"));
    Map<String, Object> result = AVCloud.callFunction("_conversationStarted", params);
    assertNull(result);
  }

  @Test
  public void testConversationAdd() throws AVException {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("initBy", "lbt05");
    params.put("members", Arrays.asList("12", "123"));
    Map<String, Object> result = AVCloud.callFunction("_conversationAdd", params);
    assertTrue((Boolean) result.get("reject"));
    assertEquals(9891, result.get("code"));
  }

  @Test
  public void testConversationRemove() throws AVException {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("initBy", "lbt05");
    params.put("members", Arrays.asList("12", "123"));
    Map<String, Object> result = AVCloud.callFunction("_conversationRemove", params);
    assertTrue((Boolean) result.get("reject"));
    assertEquals(9892, result.get("code"));
  }

  @Test
  public void testConversationUpdate() throws AVException {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("initBy", "lbt05");
    params.put("convId", "123123123");
    params.put("mute", false);
    Map<String, Object> attributes = new HashMap<String, Object>();
    attributes.put("ts", "123123");
    attributes.put("shit", 123);
    params.put("attr", attributes);
    Map<String, Object> result = AVCloud.callFunction("_conversationUpdate", params);
    assertTrue((Boolean) result.get("reject"));
    assertEquals(9893, result.get("code"));

    params.put("initBy", "GoldenShit");
    result = AVCloud.callFunction("_conversationUpdate", params);
    assertFalse((Boolean) result.get("mute"));
    assertEquals(attributes, result.get("attr"));
  }
}
