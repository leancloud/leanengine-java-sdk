package cn.leancloud;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.fastjson.JSON;

@WebServlet(name = "HealthCheckServlet", urlPatterns = { "/__engine/1/ping" }, loadOnStartup = -1)
public class HealthCheckServlet extends HttpServlet {

	private static final long serialVersionUID = -4946115749389928857L;

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setContentType("application/json");
		resp.setStatus(HttpServletResponse.SC_OK);
		resp.getWriter().println(JSON.toJSON(new RuntimeInfo("java-" + System.getProperty("java.version"), "0.0.1")));
	}

	class RuntimeInfo {
		
		String runtime;
		String version;

		public RuntimeInfo(String runtime, String version) {
			super();
			this.runtime = runtime;
			this.version = version;
		}

		public String getRuntime() {
			return runtime;
		}

		public void setRuntime(String runtime) {
			this.runtime = runtime;
		}

		public String getVersion() {
			return version;
		}

		public void setVersion(String version) {
			this.version = version;
		}

	}

}