package cn.leancloud;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.AVUtils;
import com.avos.avoscloud.GenericObjectCallback;
import com.avos.avoscloud.PaasClient;
/**
 * 用于解析 header 中间传递的请求用户信息
 * @author lbt05
 *
 */
class RequestUserParser {

  public static void parse(final HttpServletRequest req) {
    if (req.getAttribute(RequestAuth.ATTRIBUTE_KEY) == null) {
      return;
    }
    String sessionToken =
        ((RequestAuth) req.getAttribute(RequestAuth.ATTRIBUTE_KEY)).getSessionToken();
    if (sessionToken != null && !sessionToken.isEmpty()) {
      Map<String, String> header = new HashMap<String, String>();
      header.put("X-LC-Session", sessionToken);
      PaasClient.storageInstance().getObject("users/me", null, true, header,
          new GenericObjectCallback() {

            @Override
            public void onSuccess(String content, AVException e) {
              if (e != null) {
                e.printStackTrace();
              }
              AVUser resultUser = new AVUser();
              if (!AVUtils.isBlankContent(content)) {
                AVUtils.copyPropertiesFromJsonStringToAVObject(content, resultUser);
                AVUser.changeCurrentUser(resultUser, true);
                req.setAttribute(RequestAuth.USER_KEY, resultUser);
              }
            }
          });
    }
  }
}
