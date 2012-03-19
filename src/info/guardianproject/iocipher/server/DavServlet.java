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

public class DavServlet extends HttpServlet 
{
	HttpManager httpManager;
	File fileRoot;
	String basePath;
	
	public DavServlet (File _fileRoot, String _basePath)
	{
		fileRoot = _fileRoot;
		basePath = _basePath;
	}
	
	public void service( javax.servlet.ServletRequest servletRequest, javax.servlet.ServletResponse servletResponse ) throws ServletException, IOException { 
        HttpServletRequest req = (HttpServletRequest) servletRequest; 
        HttpServletResponse resp = (HttpServletResponse) servletResponse; 
        try { 
        	
        	
            httpManager.process( new com.bradmcevoy.http.ServletRequest(req), new com.bradmcevoy.http.ServletResponse(resp) ); 
        } finally { 
            servletResponse.getOutputStream().flush(); 
            servletResponse.flushBuffer(); 
        } 
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

		 System.setProperty 
		 ("org.xml.sax.driver","org.xmlpull.v1.sax2.Driver"); 
		 
		NullSecurityManager securityManager = new NullSecurityManager();//SimpleSecurityManager();

		FileSystemResourceFactory fsrf = new FileSystemResourceFactory(fileRoot, securityManager, basePath);
		fsrf.setAllowDirectoryBrowsing(true);
		fsrf.setDigestAllowed(true);
		
		httpManager = new HttpManager(fsrf);
		
	} 
 
}
