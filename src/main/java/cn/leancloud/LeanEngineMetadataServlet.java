package cn.leancloud;

import com.alibaba.fastjson.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class LeanEngineMetadataServlet extends HttpServlet {

  private static final long serialVersionUID = 171155874009103794L;
  private final LeanEngine engine;

  LeanEngineMetadataServlet(LeanEngine engine) {
    this.engine = engine;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    try {
      Object authMasterKey = req.getAttribute(AuthFilter.ATTRIBUTE_AUTH_MASTER_KEY);
      if (authMasterKey == null || !(boolean) authMasterKey) {
        throw new UnauthException();
      }

      resp.setContentType(LeanEngine.JSON_CONTENT_TYPE);
      JSONObject result = new JSONObject();
      result.put("result", engine.getMetaData());
      resp.getWriter().write(result.toJSONString());
    } catch (UnauthException e) {
      e.resp(resp);
    }
  }
}
