package cn.leancloud.leanengine_test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import cn.leancloud.EngineSessionCookie;

public class SessionCookieTest {

  @Test
  public void testCookieSign() {
    String key = "05XgTktKPMkU";
    String cookie =
        "avos:sess=eyJfdWlkIjoiNTc4ODUxOWZjNGM5NzEwMDVlY2QxNGQzIiwiX3Nlc3Npb25Ub2tlbiI6Indvc2NqdHByYjVhNXJwbDdpMmd3ZWt5dXIifQ==";
    String sign = EngineSessionCookie.signCookie(key, cookie);
    System.out.println(sign);
    assertEquals("t-Hd40ns4tpEY0pWgseuIefTdvg", sign);
  }
}
