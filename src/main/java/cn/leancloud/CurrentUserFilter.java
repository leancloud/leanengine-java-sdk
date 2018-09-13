package cn.leancloud;

import com.avos.avoscloud.*;

import javax.servlet.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CurrentUserFilter implements Filter {
  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
  }

  @Override
  public void doFilter(final ServletRequest req, final ServletResponse resp, final FilterChain chain) throws IOException, ServletException {
    AVUser.changeCurrentUser(null, false);
    if (req.getAttribute(AuthFilter.ATTRIBUTE_KEY) == null) {
      return;
    }
    String sessionToken =
        ((AuthFilter.AuthInfo) req.getAttribute(AuthFilter.ATTRIBUTE_KEY)).sessionToken;
    if (sessionToken != null && !sessionToken.isEmpty()) {
      Map<String, String> header = new HashMap<String, String>();
      header.put("X-LC-Session", sessionToken);
      final Object[] result = {null};
      PaasClient.storageInstance().getObject("users/me", null, true, header,
          new GenericObjectCallback() {
            @Override
            public void onSuccess(String content, AVException e) {
              if (e != null) {
                AVExceptionHolder.add(e);
              }
              AVUser resultUser = new AVUser();
              if (!AVUtils.isBlankContent(content)) {
                AVUtils.copyPropertiesFromJsonStringToAVObject(content, resultUser);
                AVUser.changeCurrentUser(resultUser, true);
                result[0] = resultUser;
              }
            }
          });
      if (AVExceptionHolder.exists()) {
        throw new RuntimeException(AVExceptionHolder.remove());
      }
      req.setAttribute(AuthFilter.USER_KEY, result[0]);
    }
    chain.doFilter(req, resp);
  }

  @Override
  public void destroy() {

  }
}
