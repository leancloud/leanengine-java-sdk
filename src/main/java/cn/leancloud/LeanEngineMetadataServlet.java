package cn.leancloud;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;

@WebServlet(name = "LeanEngineServlet", urlPatterns = { "/1/functions/_ops/metadatas", "/1.1/functions/_ops/metadatas",
		"/1/call/_ops/metadatas", "/1.1/call/_ops/metadatas" })
public class LeanEngineMetadataServlet extends HttpServlet {

}
