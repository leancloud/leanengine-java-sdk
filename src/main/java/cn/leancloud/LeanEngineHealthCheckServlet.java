package cn.leancloud;

import com.alibaba.fastjson.JSONObject;
import com.avos.avoscloud.internal.InternalConfigurationController;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 定义云函数中的健康检查函数
 *
 * @author lbt05
 */
public class LeanEngineHealthCheckServlet extends HttpServlet {

  private static final long serialVersionUID = -7406297470714318279L;

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
      IOException {
    resp.setHeader("content-type", LeanEngine.JSON_CONTENT_TYPE);
    JSONObject result = new JSONObject();
    result.put("runtime", System.getProperty("java.version"));
    result.put("version", InternalConfigurationController.globalInstance().getClientConfiguration()
        .getUserAgent());
    resp.getWriter().write(result.toJSONString());
  }
}
