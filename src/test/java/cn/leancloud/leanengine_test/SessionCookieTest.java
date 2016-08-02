package cn.leancloud.leanengine_test;

import static org.junit.Assert.assertEquals;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import org.junit.Test;

import com.avos.avoscloud.internal.impl.DefaultAVUserCookieSign;

public class SessionCookieTest {

  @Test
  public void testCookieSign() throws InvalidKeyException, NoSuchAlgorithmException {
    String key = "05XgTktKPMkU";
    String cookie =
        "avos:sess=eyJfdWlkIjoiNTc4ODUxOWZjNGM5NzEwMDVlY2QxNGQzIiwiX3Nlc3Npb25Ub2tlbiI6Indvc2NqdHByYjVhNXJwbDdpMmd3ZWt5dXIifQ==";
    String sign = DefaultAVUserCookieSign.encrypt(key, cookie);
    assertEquals("t-Hd40ns4tpEY0pWgseuIefTdvg", sign);
  }
}
