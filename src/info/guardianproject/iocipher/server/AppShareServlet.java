package info.guardianproject.iocipher.server;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

public class AppShareServlet extends HttpServlet 
{
	File fileRoot;
	String basePath;
	Context context;
	
	public AppShareServlet (File _fileRoot, String _basePath, Context _context)
	{
		fileRoot = _fileRoot;
		basePath = _basePath;
		context = _context;
		
	}
	
	public void service( javax.servlet.ServletRequest servletRequest, javax.servlet.ServletResponse servletResponse ) throws ServletException, IOException { 
        HttpServletRequest req = (HttpServletRequest) servletRequest; 
        HttpServletResponse resp = (HttpServletResponse) servletResponse; 
        
	}
	
	private void getApps ()
	{
        
        PackageManager pMgr = context.getPackageManager();
		
		List<ApplicationInfo> lAppInfo = pMgr.getInstalledApplications(0);
		
		Iterator<ApplicationInfo> itAppInfo = lAppInfo.iterator();
		
		ApplicationInfo aInfo;
		
		while (itAppInfo.hasNext())
		{
			aInfo = itAppInfo.next();
			
			String path = aInfo.sourceDir;
			String name = aInfo.name;
			
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

		
	} 
 
}
