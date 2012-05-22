package info.guardianproject.iocipher.server;

import info.guardianproject.iocipher.File;
import info.guardianproject.iocipher.FileOutputStream;
import info.guardianproject.iocipher.FileWriter;
import info.guardianproject.iocipher.VirtualFileSystem;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.provider.MediaStore.Images;
import android.util.Log;

public class WebServerService extends Service
{
	private final static String TAG = "IOCipherServer";

	private MdnsManager mdns;
	
	 // Binder given to clients
    private final IBinder mBinder = new LocalBinder();

    private Thread mWsThread;
    

	private int mPort;
	private boolean mUseSsl;
	private String mIpAddress;

	private String IOCIPHER_FOLDER = "iocipher";
	private String IOCIPHER_FILE = "iocipher.db";
	
    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
    	WebServerService getService() {
            // Return this instance of LocalService so clients can call public methods
            return WebServerService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
    	
    	
    	
        return mBinder;
    }
	
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

	public MyServ getWebServer ()
	{
		return srv;
	}
	
	private void startNotification ()
	{
		//This constructor is deprecated. Use Notification.Builder instead
		Notification notice = new Notification(R.drawable.iocipher, "Active: " + mIpAddress, System.currentTimeMillis());

		Intent intent = new Intent(WebServerService.this, IOCipherServerActivity.class);

		PendingIntent pendIntent = PendingIntent.getActivity(WebServerService.this, 0, intent, 0);

		//This method is deprecated. Use Notification.Builder instead.
		notice.setLatestEventInfo(WebServerService.this, "IOCipherServer", "Active: " + mIpAddress, pendIntent);

		notice.flags |= Notification.FLAG_NO_CLEAR;
		
		startForeground(mPort,notice);
	
	}
	
	public void startServer (int port, boolean useSSL, String ipAddress, final String password) throws Exception
	{
		
		//android.os.Debug.waitForDebugger();
		
		mPort = port;
		mUseSsl = useSSL;
		mIpAddress = ipAddress;
		
		startNotification();
		
		mWsThread = new Thread ()
		{
			
			public void run ()
			{
				try
				{
					
					try
					{
						if (mdns == null)
							mdns = new MdnsManager(WebServerService.this);
						
						mdns.register("iocs", "_webdavs._tcp.local", "iocipherwebdav", 8888, "path=/sdcard");
						mdns.register("iocs-https", "_https._tcp.local", "iocipherweb", 8888, "path=/public");
					}
					catch (Exception e)
					{
						Log.d(TAG, "mdns multicast not working");
					}
					
					srv = new MyServ();
					
					java.io.File filePublic = new java.io.File("/sdcard/public");
					
					if (!filePublic.exists())
					{
						filePublic.mkdir();
					}
					
			 		// setting aliases, for an optional file servlet
		            //Acme.Serve.Serve.PathTreeDictionary aliases = new Acme.Serve.Serve.PathTreeDictionary();
		            //aliases.put("/public/*", filePublic);
		            
			//  note cast name will depend on the class name, since it is anonymous class
		            //srv.setMappingTable(aliases);
					// setting properties for the server, and exchangeable Acceptors
					java.util.Properties properties = new java.util.Properties();
					properties.put("port", mPort);
					properties.setProperty(Acme.Serve.Serve.ARG_NOHUP, "nohup");
					
					if (mUseSsl)
					{
						properties.setProperty("secure", "true");
						//properties.setProperty("socketFactory", "Acme.Serve.SSLServerSocketFactory");
						properties.setProperty("acceptorImpl", "Acme.Serve.SSLAcceptor");
						
						java.io.File fileKS = new java.io.File(WebServerService.this.getFilesDir(),"iocipher.bks");
						
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
					
					srv.addServlet("/public/*", new FileServlet());
					

					java.io.File fileIoCipherDb = new java.io.File(getDir(IOCIPHER_FOLDER,
						Context.MODE_PRIVATE).getAbsoluteFile(),IOCIPHER_FILE);
					

					if (vfs == null)
					{
						setUpIOCipher(fileIoCipherDb, password);
					}
					
					srv.addServlet("/private/*", new IOCipherFileServlet(WebServerService.this));

//					srv.addDefaultServlets(null); // optional file servlet
					
					String davUser = "admin";
					DavServlet dServlet = new DavServlet(new java.io.File("/sdcard"),"sdcard", davUser, password);
					
					srv.addServlet("/sdcard/*", dServlet);
					
					srv.serve();
				}
				catch (Exception e)
				{
					handleException ("error starting server", e);
				}
		
			}
		};
		
		mWsThread.start();

		
	}
	
	private void handleException (String msg, Exception e)
	{
		Log.e(TAG, msg, e);
	}
	
	public void stopServer ()
	{
		

    	if (mWsThread.isAlive())
    	{
    		mWsThread.interrupt();
    		mWsThread = null;
    	}
    	
		if (srv != null)
		{
			srv.notifyStop();
			srv = null;
		}
		
		if (mdns != null)
		{
			mdns.unregister("iocs");
			mdns.unregister("iocs-https");
		}
		
		stopForeground(true);
		

		if (vfs != null)
			vfs.unmount();
		
	}

	public boolean isServerRunning ()
	{
		if (mWsThread != null && mWsThread.isAlive())
			return true;
		else
			return false;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		stopServer ();
		
		
	}

	/*
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		if (srv == null)
		{
			new Thread(this).start();
			
		}
		
		return Service.START_STICKY;
	}*/
	
	public void importFileToSecureStore (Uri uriSrc) throws IOException
	{
		String targetName = getName(uriSrc);
		info.guardianproject.iocipher.File fileNew = new info.guardianproject.iocipher.File(targetName);
		
		int i = 1;
		
		while (fileNew.exists())
		{
			fileNew = new info.guardianproject.iocipher.File((i++) + '.' + targetName);
		}
		
		InputStream is = getContentResolver().openInputStream(uriSrc);
		
		copyStreamToFile (is, fileNew);
	}
	
	private String getName (Uri uri)
	{
		String name = null;
		if (uri != null) {
			Cursor c = getContentResolver().query(uri, null, null, null, null);
			if (c != null && c.moveToFirst()) {
				int id = c.getColumnIndex(Images.Media.DATA);
				if (id != -1) {
					name = c.getString(id);
					if (name != null)
					return new File(name).getName();
				}
				
				id = c.getColumnIndex(Images.Media.DISPLAY_NAME);
				if (id != -1) {
					name = c.getString(id);
				}
			}
			
		}
		
		return name;
	}
	    
	    private static void copyStreamToFile(InputStream input, File fileOut) throws IOException {
	    	
	    	/*
	        // if both are file streams, use channel IO
	        if ((output instanceof FileOutputStream) && (input instanceof FileInputStream)) {
	          try {
	            FileChannel target = ((FileOutputStream) output).getChannel();
	            FileChannel source = ((FileInputStream) input).getChannel();

	            source.transferTo(0, Integer.MAX_VALUE, target);

	            source.close();
	            target.close();

	            return;
	          } catch (Exception e) { 
	          }
	        }*/
	    	
	    	BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(fileOut));
	    	byte[] buf = new byte[1024];
	        int len = -1;
	        
	        while ((len = input.read(buf))!=-1) {
	          out.write(buf,0,len);
	        }
	        
	        try {
	          input.close();
	        } catch (IOException ignore) {
	        	Log.e(TAG, "error closing input",ignore);
	        }
	        try {
	          out.close();
	        } catch (IOException ignore) {

	        	Log.e(TAG, "error closing output",ignore);
	        }
	      }
	    
	    private VirtualFileSystem vfs;

		protected synchronized void setUpIOCipher(java.io.File db, String password) {
			
			Log.v("IOCipher", "database file: " + db.getAbsolutePath());
			if (db.exists())
				Log.v("IOCipher", "exists: " + db.getAbsolutePath());
			try {
				vfs = new VirtualFileSystem(db.getAbsolutePath());
			} catch (Exception e) {
				Log.e("IOCipher", e.toString());
			}
			vfs.mount(password);
			
		}

}
