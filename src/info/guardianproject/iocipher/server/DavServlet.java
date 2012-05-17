package info.guardianproject.iocipher.server;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.bradmcevoy.http.HttpManager;
import com.ettrema.http.fs.FileSystemResourceFactory;
import com.ettrema.http.fs.SimpleSecurityManager;

public class DavServlet extends HttpServlet 
{
	HttpManager httpManager;
	File fileRoot;
	String basePath;
	String user;
	String pass;
	
	public DavServlet (File _fileRoot, String _basePath, String _user, String _pass)
	{
		fileRoot = _fileRoot;
		basePath = _basePath;
		user = _user;
		pass = _pass;
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
		
		
		
	}

	@Override
	public ServletConfig getServletConfig() {
		return null;
	}

	@Override
	public String getServletInfo() {
		return null;
	}

	@Override
	public void init(ServletConfig arg0) throws ServletException {

		 System.setProperty 
		 ("org.xml.sax.driver","org.xmlpull.v1.sax2.Driver"); 
		 
		 Map<String,String> mapUsers = new HashMap<String,String>();
		 mapUsers.put(user, pass);
	//	NullSecurityManager securityManager = new NullSecurityManager();//SimpleSecurityManager();
		 SimpleSecurityManager securityManager = new SimpleSecurityManager();
		 securityManager.setNameAndPasswords(mapUsers);
		 
		FileSystemResourceFactory fsrf = new FileSystemResourceFactory(fileRoot, securityManager, basePath);
		fsrf.setAllowDirectoryBrowsing(true);
		fsrf.setDigestAllowed(true);
		
		httpManager = new HttpManager(fsrf);
		
	} 
 
}
