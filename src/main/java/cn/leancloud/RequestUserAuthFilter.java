package cn.leancloud;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;

import com.avos.avoscloud.AVUser;

@WebFilter(filterName = "requestUserAuthFilter", urlPatterns = {"/*"})
public class RequestUserAuthFilter implements Filter {

  public void init(FilterConfig filterConfig) throws ServletException {}

  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    chain.doFilter(request, response);
    AVUser.changeCurrentUser(null, true);
    EngineRequestContext.clean();
  }

  public void destroy() {

  }
}
