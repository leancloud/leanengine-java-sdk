package cn.leancloud;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.fastjson.JSONObject;
import com.avos.avoscloud.AVUtils;

@WebServlet(name = "LeanEngineMetadataServlet", urlPatterns = {"/1/functions/_ops/metadatas",
    "/1.1/functions/_ops/metadatas", "/1/call/_ops/metadatas", "/1.1/call/_ops/metadatas"},
    loadOnStartup = 5)
public class LeanEngineMetadataServlet extends HttpServlet {


  @Override
  public void init() throws ServletException {
    super.init();
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
      IOException {
    try {
      RequestAuth.auth(req);
      RequestAuth auth = (RequestAuth) req.getAttribute(RequestAuth.ATTRIBUTE_KEY);
      if (auth == null || AVUtils.isBlankString(auth.getMasterKey())) {
        throw new UnauthException();
      }
    } catch (UnauthException e) {
      e.resp(resp);
      e.printStackTrace();
      return;
    }
    resp.setContentType(LeanEngine.JSON_CONTENT_TYPE);
    JSONObject result = new JSONObject();
    result.put("result", LeanEngine.getMetaData());
    resp.getWriter().write(result.toJSONString());
  }
}
