package cn.leancloud;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.fastjson.JSONObject;
import com.avos.avoscloud.PaasClient;

@WebServlet(name = "LeanEngineServlet", urlPatterns = {"/__engine/1/ping",}, loadOnStartup = 4)
public class LeanEngineHealthCheckServlet extends HttpServlet {

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
      IOException {
    super.doPost(req, resp);
    resp.setHeader("content-type", LeanEngine.JSON_CONTENT_TYPE);
    JSONObject result = new JSONObject();
    result.put("runtime", System.getProperty("java.version"));
    result.put("version", PaasClient.sdkVersion);
    resp.getWriter().write(result.toJSONString());
  }
}
