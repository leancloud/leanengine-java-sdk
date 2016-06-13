package cn.leancloud;

import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name = "LeanEngineServlet", urlPatterns = { "/1/functions/*", "/1.1/functions/*", "/1/call/*",
		"/1.1/call/*" }, loadOnStartup = 1)
public class LeanEngine extends HttpServlet {

	public static final long serialVersionUID = 3962660277165698922L;

	public static final String appId = System.getenv("LEANCLOUD_APP_ID");

	public static final String appKey = System.getenv("LEANCLOUD_APP_KEY");

	public static final String masterKey = System.getenv("LEANCLOUD_APP_MASTER_KEY");

	public static final String appEnv = System.getenv("LEANCLOUD_APP_ENV");

	private Map<String, Method> funcs = new HashMap<String, Method>();

	private Map<String, Method> hooks = new HashMap<String, Method>();

	public enum RequestType {
		Function, Call, Hook
	}

	public void register(Class<?> clazz) {
		for (Method m : clazz.getDeclaredMethods()) {
			EngineFunction func = m.getAnnotation(EngineFunction.class);
			if (func != null) {
				funcs.put(m.getName(), m);
				continue;
			}
			EngineHook hook = m.getAnnotation(EngineHook.class);
			if (hook != null) {

			}
		}
	}

	@Override
	protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		setAllowOriginHeader(req, resp);
		resp.setHeader("Access-Control-Max-Age", "86400");
		resp.setHeader("Access-Control-Allow-Methods", "PUT, GET, POST, DELETE, OPTIONS");
		resp.setHeader("Access-Control-Allow-Headers",
				"X-LC-Id, X-LC-Key, X-LC-Session, X-LC-Sign, X-LC-Prod, X-Uluru-Application-Key, X-Uluru-Application-Id, X-Uluru-Application-Production, X-Uluru-Client-Version, X-Uluru-Session-Token, X-AVOSCloud-Application-Key, X-AVOSCloud-Application-Id, X-AVOSCloud-Application-Production, X-AVOSCloud-Client-Version, X-AVOSCloud-Session-Token, X-AVOSCloud-Super-Key, X-Requested-With, Content-Type, X-AVOSCloud-Request-sign");
		resp.setHeader("Content-Length", "0");
		resp.setStatus(HttpServletResponse.SC_OK);
		resp.getWriter().println();
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		setAllowOriginHeader(req, resp);
		try {
			RequestAuth.auth(req);
		} catch (UnauthException e) {
			e.resp(resp);
			return;
		}
		RequestType requestType;
		String[] splited = req.getRequestURI().split("/");
		if (splited.length == 3) {
			if (splited[1].equals("functions")) {
				requestType = RequestType.Function;
				RequestUserParser.parse(req, requestType);
				 StringBuffer sb = new StringBuffer();
				  String line = null;
				    BufferedReader reader = req.getReader();
				    while ((line = reader.readLine()) != null)
				      sb.append(line);
				// TODO
			} else if (splited[2].equals("call")) {
				requestType = RequestType.Call;
				// TODO
			} else {
				resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				resp.setContentType("application/json; charset=UTF-8");
				resp.getWriter().println("{\"code\":\"400\",\"error\":\"Unsupported operation.\"}");
				return;
			}
		} else if (splited.length == 4) {
			requestType = RequestType.Hook;
			if (splited[2].equals("onVerified")) {
				// TODO
			} else if ((splited[2].equals("BigQuery") || splited[2].equals("Insight"))
					&& splited[3].equals("onComplete")) {
				// TODO
			} else if (splited[2].equals("_User") && splited[3].equals("onLogin")) {
				// TODO
			} else {
				
			}
		}
		System.out.println(">>" + req.getRequestURI());
		System.out.println(">>" + req.getRequestURL());
	}

	private void setAllowOriginHeader(HttpServletRequest req, HttpServletResponse resp) {
		String allowOrigin = req.getHeader("origin");
		if (allowOrigin == null) {
			allowOrigin = "*";
		}
		resp.setHeader("Access-Control-Allow-Origin", allowOrigin);
	}

}
