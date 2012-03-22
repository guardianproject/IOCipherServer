package info.guardianproject.iocipher.server;

import info.guardianproject.iocipher.server.WebServerService.LocalBinder;

import java.io.File;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.ToggleButton;

public class IOCipherServerActivity extends Activity {
	
	private final static String TAG = "IOCipherServer";
	
    boolean mBound = false;

    private WebServerService mService;

	private int mWsPort = 8888;
	private boolean mWsUseSSL = true;
	
    private Thread mWsThread;
    
    private ToggleButton tButton;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        tButton = (ToggleButton)findViewById(R.id.toggleButton1);
        tButton.setEnabled(false);
        tButton.setOnCheckedChangeListener(new OnCheckedChangeListener () {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				
				if (isChecked)
					startWebServer();
				else
					stopWebServer();
				
			}
        	
        });
        
        
    }
    
    
    
    @Override
	protected void onResume() {
		super.onResume();
		
		bindService();
	    
	}

    @Override
    protected void onStop() {
        super.onStop();
        // Unbind from the service
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }


	public void bindService ()
    {
        Intent intent = new Intent(this, WebServerService.class);
		
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

    }
    
    public void startWebServer()
    {
    	
    	mWsThread = new Thread ()
    	{
    		
    		public void run ()
    		{
	    		try
	    		{
	    			mService.startServer(mWsPort, mWsUseSSL);
	    		}
	    		catch (Exception e)
	    		{
	    			Log.e(TAG, "unable to start secure server",e);
	    		}
	    		
    		}
    	};
    	
    	mWsThread.start();
    	
    	showStatus();
    }
    
    public void stopWebServer ()
    {
    	
    	if (mWsThread.isAlive())
    	{
    		mService.stopServer();
    		mWsThread.interrupt();
    		mWsThread = null;
    	}
    	
   		clearStatus ();
    }
    
    private void postBound ()
    {

        tButton.setEnabled(true);
        
        if (mService.getWebServer() != null)
        {
        	showStatus();
        }
        
    }
    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            LocalBinder binder = (LocalBinder) service;
            mService = binder.getService();
            mBound = true;
            
            postBound ();
            
            
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };
    
    private void showStatus ()
    {
    	TextView tv = (TextView)findViewById(R.id.textStatus);
    
    	String ip = getLocalIpAddress();
    	String fingerprint = "";
    	
    	File fileKS = new File(this.getFilesDir(),"iocipher.bks");
		String password = "changeme";
		String alias = "twjs";
		
		CACertManager ccm = new CACertManager();
		try {
			ccm.load(fileKS.getAbsolutePath(), password);
			fingerprint = ccm.getFingerprint(ccm.getCertificateChain(alias)[0], "SHA1");
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	if (ip != null)
    	{
    		tv.setText("Browser:\nhttps://" + ip + ":8888" 
    				+ "\n\n" +
    				
    				"WebDav (Secure):\nhttps://" + ip + ":8888/files"
    				+ "\n\n" +
    				"SHA1: " + fingerprint
    				);
    	}
    }
    
    private void clearStatus ()
    {
    	TextView tv = (TextView)findViewById(R.id.textStatus);
    
    	tv.setText("");
    }
    
	@Override
	protected void onStart() {
		super.onStart();
		
		
	}

	public String getLocalIpAddress() {
	    try {
	        for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
	            NetworkInterface intf = en.nextElement();
	            for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
	                InetAddress inetAddress = enumIpAddr.nextElement();
	                if (!inetAddress.isLoopbackAddress()) {
	                    return inetAddress.getHostAddress().toString();
	                }
	            }
	        }
	    } catch (SocketException ex) {
	        Log.e(TAG, ex.toString());
	    }
	    return null;
	}
		
}