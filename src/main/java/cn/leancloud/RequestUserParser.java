package cn.leancloud;

import javax.servlet.http.HttpServletRequest;

import cn.leancloud.LeanEngine.RequestType;

public class RequestUserParser {

	public static void parse(HttpServletRequest req, RequestType requestType) {
		if (requestType.equals(RequestType.Function) || requestType.equals(RequestType.Call)) {
			String sessionToken = ((RequestAuth) req.getAttribute(RequestAuth.ATTRIBUTE_KEY)).getSessionToken();
			if (sessionToken != null && !sessionToken.isEmpty()) {
				// TODO
			}
		} else {
			// TODO
		}

	}

}
