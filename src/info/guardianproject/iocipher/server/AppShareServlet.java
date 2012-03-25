package info.guardianproject.iocipher.server;

import java.io.File;
import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.bradmcevoy.http.HttpManager;
import com.ettrema.http.fs.FileSystemResourceFactory;
import com.ettrema.http.fs.NullSecurityManager;

public class AppShareServlet extends HttpServlet 
{
	File fileRoot;
	String basePath;
	
	public AppShareServlet (File _fileRoot, String _basePath)
	{
		fileRoot = _fileRoot;
		basePath = _basePath;
	}
	
	public void service( javax.servlet.ServletRequest servletRequest, javax.servlet.ServletResponse servletResponse ) throws ServletException, IOException { 
        HttpServletRequest req = (HttpServletRequest) servletRequest; 
        HttpServletResponse resp = (HttpServletResponse) servletResponse; 
      
    }

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ServletConfig getServletConfig() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getServletInfo() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void init(ServletConfig arg0) throws ServletException {
		// TODO Auto-generated method stub

		
	} 
 
}
