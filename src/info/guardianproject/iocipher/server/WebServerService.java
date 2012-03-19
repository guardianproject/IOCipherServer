package info.guardianproject.iocipher.server;


import java.io.File;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class WebServerService extends Service implements Runnable
{
	private final static String TAG = "IOCipherServer";

	
	class MyServ extends Acme.Serve.Serve {
		// Overriding method for public access
                    public void setMappingTable(PathTreeDictionary mappingtable) { 
                          super.setMappingTable(mappingtable);
                    }
                    // add the method below when .war deployment is needed
                    public void addWarDeployer(String deployerFactory, String throttles) {
                          super.addWarDeployer(deployerFactory, throttles);
                    }
            };
            
	MyServ srv;

	public void run ()
	{
		int port = 8888;
		boolean useSSL = true;
		
		try
		{
			startServer(port, useSSL);
		}
		catch (Exception e)
		{
			Log.e(TAG, "unable to start secure server",e);
		}
	}

	public void startServer (int port, boolean useSSL) throws Exception
	{
		
		srv = new MyServ();
 		// setting aliases, for an optional file servlet
            Acme.Serve.Serve.PathTreeDictionary aliases = new Acme.Serve.Serve.PathTreeDictionary();
            aliases.put("/*", new java.io.File("/sdcard"));
	//  note cast name will depend on the class name, since it is anonymous class
            srv.setMappingTable(aliases);
		// setting properties for the server, and exchangeable Acceptors
		java.util.Properties properties = new java.util.Properties();
		properties.put("port", port);
		properties.setProperty(Acme.Serve.Serve.ARG_NOHUP, "nohup");
		
		if (useSSL)
		{
			properties.setProperty("secure", "true");
			//properties.setProperty("socketFactory", "Acme.Serve.SSLServerSocketFactory");
			properties.setProperty("acceptorImpl", "Acme.Serve.SSLAcceptor");
			
			File fileKS = new File(this.getFilesDir(),"iocipher.bks");
			String password = "changeme";
			
			if (!fileKS.exists())
			{
				String alias = "twjs";
				String cn = "localhost";
				String on = "iocipher";
				
				KeyStoreGenerator.generateKeyStore(fileKS, alias, 2048, password, cn, on, on, "New York", "New York", "US");
		
			}
			
	
			properties.setProperty("keystoreFile",fileKS.getAbsolutePath());
			properties.setProperty("keystorePass",password);
			properties.setProperty("keystoreType",CACertManager.KEYSTORE_TYPE);
		}
		
		srv.arguments = properties;
		srv.addDefaultServlets(null); // optional file servlet
		
		srv.addServlet("/files/*", new DavServlet(new File("/sdcard"),"files"));
		
		
		srv.serve();

	}
	
	public void stopServer ()
	{
		srv.destroyAllServlets();
		
		srv.notifyStop();
		srv = null;
	}


	@Override
	public void onDestroy() {
		super.onDestroy();
		stopServer ();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		if (srv == null)
		{
			new Thread(this).start();
		}
		
		return Service.START_STICKY;
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
}
