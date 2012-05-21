package info.guardianproject.iocipher.server;

/*
 * Includes code from:
 * http://code.google.com/p/swiftp/source/browse/trunk/src/org/swiftp/FTPServerService.java#482

Copyright 2009 David Revell

This file is part of SwiFTP.

SwiFTP is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

SwiFTP is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with SwiFTP.  If not, see <http://www.gnu.org/licenses/>.
*/

import info.guardianproject.iocipher.server.WebServerService.LocalBinder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.channels.FileChannel;
import java.util.Enumeration;
import java.util.StringTokenizer;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class IOCipherServerActivity extends SherlockActivity {
	
	public final static String TAG = "IOCipherServer";
	
	private final String ksFileName = "iocipher.bks";
	private final String ksAlias = "twjs";
	
	private final static String LOCALHOST = "127.0.0.1";
	
    boolean mBound = false;

    private WebServerService mService;

    private final static int DEFAULT_PORT = 8443;
	private int mWsPort = -1;
	private boolean mWsUseSSL = true;
	private boolean runOnBind = false;
	

	private MenuItem mMenuStartTop;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        setPreferences();

        /*
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
        */
        
    }
    
    private void setPreferences ()
    {
    	
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        
        mWsUseSSL = prefs.getBoolean("useSSL", true);
        mWsPort = Integer.parseInt(prefs.getString("prefPort", "" + DEFAULT_PORT));
    }
    
    
    @Override
	protected void onResume() {
		super.onResume();
		
        showStatus();
        
		bindService();
		
	    checkForImports();
	    
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
        startService(intent);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

    }
    
    public void startWebServer(String password)
    {
    	
    	if (mService == null)
    	{
    		runOnBind = true;
    		bindService();
    		
    	}
    	else
    	{
    		try
    		{
    			mService.startServer(mWsPort, mWsUseSSL, getMyAddress(), password);

    	    	showStatus();
    		}
    		catch (Exception e)
    		{
    			Log.e(TAG, "unable to start secure server",e);
    		}
    		
    	}
    }
    
    public void stopWebServer ()
    {
    	mService.stopServer();
    	
   		clearStatus ();
    }
    
    private void postBound ()
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        String password = prefs.getString("prefPass", "");
        if (runOnBind)
        {
            
        	startWebServer(password);
        }
        
        showStatus();
        
        handleImport();
        
        
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
    	StringBuffer msg = new StringBuffer();
    	
    	String ip = getMyAddress();
    	
    	msg.append("Wifi IP: ").append(ip);
    	msg.append("\n\n");
    	
    	if (mService != null && mService.isServerRunning())
    	{
    		if (mMenuStartTop != null)
    		mMenuStartTop.setIcon(android.R.drawable.ic_media_pause);
    		
    		String protocol = "https";
    		if (!mWsUseSSL)
    			protocol = "http";
    	
    		msg.append("Web Browser:").append('\n');
    		msg.append(protocol).append("://").append(ip).append(':').append(mWsPort).append("/public");
    		msg.append("\n\n");
    		
    		msg.append("WebDAV Share:").append('\n');
    		msg.append(protocol).append("://").append(ip).append(':').append(mWsPort).append("/sdcard");
    		msg.append("\n\n");
    		
    		/*
    		String fingerprint = "";
        	
    		
        	File fileKS = new File(this.getFilesDir(),ksFileName);
    	
    		if (fileKS.exists())
    		{
	    		CACertManager ccm = new CACertManager();
	    		try {
	    			ccm.load(fileKS.getAbsolutePath(), password);
	    			fingerprint = ccm.getFingerprint(ccm.getCertificateChain(ksAlias)[0], "SHA1");
	    			
	        		msg.append("SHA1 Fingerprint").append('\n');
	        		msg.append(fingerprint);
	
	    		} catch (Exception e) {
	    			Log.e(TAG,"error loading fingerprint",e);
	    		} 
    		}*/
    		
    	}
    	else
    	{
    		if (mMenuStartTop != null)
    		mMenuStartTop.setIcon(android.R.drawable.ic_media_play);
    		
    		msg.append("(Server deactivated)");
    		
    	}
		
		
		
		tv.setText(msg.toString());
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

    private String getMyAddress() {
        
    	//WifiManager wifi = (WifiManager)getSystemService( Context.WIFI_SERVICE );

        //WifiInfo connectionInfo = wifi.getConnectionInfo();
        InetAddress address = null;
        
        Log.w(TAG, "Not connected to wifi.  This may not work.");
        // Get the IP the usual Java way
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress();
                    }
                }
            }        
        } catch (SocketException e) {
            Log.e(TAG, "while enumerating interfaces", e);
            return null;
        }
       
     
        
        if (address == null || (address.getAddress() != null  && address.getHostAddress().equals("0.0.0.0")))
        {

        	//# ifconfig eth0
        	try
        	{
        		String ipcmd = "netstat";
        		
        		Process proc = Runtime.getRuntime().exec(ipcmd);
        		
        		BufferedReader br = new BufferedReader(new InputStreamReader(proc.getInputStream()));
        		String line = null;
        		
        		/*Proto Recv-Q Send-Q Local Address          Foreign Address        State
tcp        0      0 127.0.0.1:5037         0.0.0.0:*              LISTEN
tcp        0      0 0.0.0.0:8080           0.0.0.0:*              LISTEN
tcp        0      0 0.0.0.0:2006           0.0.0.0:*              LISTEN
tcp        0      0 0.0.0.0:1212           0.0.0.0:*              LISTEN
udp        0      0 172.29.149.193:698     0.0.0.0:*             
udp        0      0 0.0.0.0:698            0.0.0.0:*             
			*/
        		String wifiIp = null;
        		
        		while ((line = br.readLine()) != null)
        		{
        			if (!line.contains("0.0.0.0"))
        			{
        				StringTokenizer st = new StringTokenizer(line," ");
        				String linePart;
        				
        				while (st.hasMoreTokens())
        				{
        					linePart = st.nextToken();
        					
        					if (!linePart.startsWith("0.0.0.0"))
        						wifiIp = linePart.split(":")[0];
        				
        				}
        				
        			
        			}
        					
        		}
        		
        		if (wifiIp != null)
        		{
	        		Log.d(TAG,"got wifi IP from ifconfig: " + wifiIp);
	        		
	        		
	        		return wifiIp;
        		}	
        	}
        	catch (Exception e)
        	{
        		 Log.e(TAG, "unknown shell exception when looking up ip address",e);
                 return null;
        	}
//        	/getMyAddress
        	
        			
        }
        
        return null;
    }
    
    public static byte byteOfInt(int value, int which) {
        int shift = which * 8;
        return (byte)(value >> shift); 
}
    
    public static InetAddress intToInet(int value) {
        byte[] bytes = new byte[4];
        for(int i = 0; i<4; i++) {
                bytes[i] = byteOfInt(value, i);
        }
        try {
                return InetAddress.getByAddress(bytes);
        } catch (UnknownHostException e) {
                // This only happens if the byte array has a bad length
                return null;
        }
    }
    
  
	 
	public static final int byteArrayToInt(byte[] arr, int offset) {
	    if (arr == null || arr.length - offset < 4)
	        return -1;
	 
	    int r0 = (arr[offset] & 0xFF) << 24;
	    int r1 = (arr[offset + 1] & 0xFF) << 16;
	    int r2 = (arr[offset + 2] & 0xFF) << 8;
	    int r3 = arr[offset + 3] & 0xFF;
	    return r0 + r1 + r2 + r3;
	}
		
	
	 @Override
    public boolean onCreateOptionsMenu(Menu menu) {
      
		 mMenuStartTop = menu.add(Menu.NONE,1,Menu.NONE,"Start");
		 
		 mMenuStartTop.setIcon(android.R.drawable.ic_media_play)
         .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
    
		 
        menu.add(Menu.NONE,2,Menu.NONE,"Settings")
            .setIcon(android.R.drawable.ic_menu_preferences)
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        
        
        menu.add(Menu.NONE,3,Menu.NONE,"About")
        .setIcon(android.R.drawable.ic_menu_info_details)
        .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

        return true;
    }

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId())
		{

			case (1):
			
				if (!mService.isServerRunning())
				{

		            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		        	startWebServer(prefs.getString("prefPass", ""));
					item.setIcon(android.R.drawable.ic_media_pause);
				}
				else
				{
					stopWebServer();
					item.setIcon(android.R.drawable.ic_media_play);
				}
				
			
			break;
			
			case (2):
				showPrefs();
			break;
			
			case (3):
				showAbout ();
			break;
			default:
			
		}
		
		return super.onOptionsItemSelected(item);
	}
	 
	private void showPrefs ()
	{
		Intent intent = new Intent (this, IOCipherSettingsActivity.class);
		startActivity(intent);
	}
	
	private void showAbout ()
	{
		 new AlertDialog.Builder(this)
	         .setTitle(getString(R.string.app_name))
	         .setMessage(getString(R.string.about))
	         .create().show();
		
	}
	
	Uri intentData = null;
	
	private void checkForImports ()
	{
		intentData = getIntent().getData();
		
		if (intentData == null && getIntent().hasExtra(Intent.EXTRA_STREAM))
			intentData = (Uri) getIntent().getExtras().get(Intent.EXTRA_STREAM);
	}
	
	private void handleImport ()
	{
		if (intentData != null)
		{
			try
			{
				if (mService != null)
					mService.importFileToSecureStore(intentData);		
			}
			catch (Exception ioe)
			{
				Log.e(TAG,"error importing",ioe);
			}
			
			intentData = null;
		}
	}
	
	 
   
	 
}