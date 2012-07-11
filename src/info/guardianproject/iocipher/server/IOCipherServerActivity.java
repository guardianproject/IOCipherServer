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
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.StringTokenizer;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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
	
	private String adminPwd = null;
	
	private MenuItem mMenuStartTop;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        

        
    }
    
    
    private void askForPassword ()
    {
    	
    	 // This example shows how to add a custom layout to an AlertDialog
        LayoutInflater factory = LayoutInflater.from(this);
        final View textEntryView = factory.inflate(R.layout.alert_dialog_text_entry, null);
        new AlertDialog.Builder(this)
            .setTitle(getString(R.string.app_name))
            .setView(textEntryView)
            .setMessage(R.string.password_msg)
            .setPositiveButton(getString(R.string.button_ok), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {

                	EditText eText = ((android.widget.EditText)textEntryView.findViewById(R.id.password_edit));
                	adminPwd = eText.getText().toString();
            	
                	postBound();

                }
            })
            .setNegativeButton(getString(R.string.button_cancel), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {

                    /* User clicked cancel so do some stuff */
                	IOCipherServerActivity.this.finish();
                }
            })
            .create().show();
	
    }
    
    
    @Override
	protected void onResume() {
		super.onResume();
		
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
    
    public void startWebServer()
    {
    	
    	if (mService == null)
    	{
    		runOnBind = true;
    		bindService();
    		
    	}
    	else if (!mService.isServerRunning())
    	{

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            
            mWsUseSSL = prefs.getBoolean("useSSL", true);
            mWsPort = Integer.parseInt(prefs.getString("prefPort", "" + DEFAULT_PORT));
            
    		try
    		{
    			mService.startServer(mWsPort, mWsUseSSL, getNetworkAddress(), adminPwd);

    	    	showStatus();
    		}
    		catch (IllegalArgumentException e)
    		{
    			adminPwd = null;
    			
    			Log.e(TAG, "unable to start secure server",e);
    			
    		}
    		catch (Exception e)
    		{
    			Log.e(TAG, "unable to start secure server",e);
    			
    			Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
    			
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

    	if (!mService.isServerRunning())
    	{
    		if (adminPwd == null)
    		{
    			askForPassword ();
    		}
    		else if (runOnBind)
    		{    
    			startWebServer();
    		}
    	}
    	else
    	{
    		showStatus();
	        new Thread ()
	        {
	        	public void run ()
	        	{
	        		handleImport();
	        	}
	        }.start();
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
    	StringBuffer msg = new StringBuffer();
    	
    	String ip = getNetworkAddress();
    	
    	msg.append("Wifi IP: ").append(ip);
    	msg.append("\n\n");
    	
    	if (mService != null && mService.isServerRunning())
    	{
    		if (mMenuStartTop != null)
    		mMenuStartTop.setIcon(android.R.drawable.ic_media_pause);
    		
    		String protocol = "https";
    		if (!mWsUseSSL)
    			protocol = "http";
    	
    		msg.append("Public Share:").append('\n');
    		msg.append(protocol).append("://").append(ip).append(':').append(mWsPort).append("/public/");
    		msg.append("\n\n");
    		
    		msg.append("Private Share:").append('\n');
    		msg.append(protocol).append("://").append(ip).append(':').append(mWsPort).append("/private/");
    		msg.append("\n\n");
    		
    		
    		if (adminPwd != null)
    		{
	    		String fingerprint = "";
	        	
	        	File fileKS = new File(this.getFilesDir(),ksFileName);
	    	
	    		if (fileKS.exists())
	    		{
		    		CACertManager ccm = new CACertManager();
		    		try {
	
		    			ccm.load(fileKS.getAbsolutePath(), adminPwd);
		    			fingerprint = ccm.getFingerprint(ccm.getCertificateChain(ksAlias)[0], "SHA1");
		    			
		        		msg.append("SHA1 Fingerprint").append('\n');
		        		msg.append(fingerprint);
		
		    		} catch (Exception e) {
		    			Log.e(TAG,"error loading fingerprint",e);
		    		} 
	    		}
    		}
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
	
	private String getNetworkAddress() {
		ConnectivityManager connMgr = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);

		final android.net.NetworkInfo wifi = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

		final android.net.NetworkInfo mobile = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

		if (wifi.isAvailable()) {

			WifiManager myWifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
			WifiInfo myWifiInfo = myWifiManager.getConnectionInfo();
			int ip = myWifiInfo.getIpAddress();
			String ipString = android.text.format.Formatter.formatIpAddress(ip);
			Log.w(TAG, "Wifi address: " + ipString);
			return ipString;
		} else if (mobile.isAvailable()) {
			Log.w(TAG, "No wifi available (mobile, yes)");
		} else {
			Log.w(TAG, "No network available");
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

		        	startWebServer();
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
				{
					Message msg = new Message();
					msg.getData().putInt("action", 0);
					handler.sendMessage(msg);
					
					mService.importFileToSecureStore(intentData);		

					Message msg2 = new Message();
					msg2.getData().putInt("action", 1);
					handler.sendMessage(msg2);
					
					
				}
			}
			catch (Exception ioe)
			{
				Log.e(TAG,"error importing",ioe);
			}
			
			intentData = null;
		}
	}
	
	ProgressDialog pd;
	
   private Handler handler = new Handler() {
       @Override
       public void handleMessage(Message msg) {
           
    	   int action = msg.getData().getInt("action");
    	   
    	   if (action==0)
    	   {
    		   pd = ProgressDialog.show(IOCipherServerActivity.this, "Working..", "Importing File To Secure Store", true,
                       false);
    	   }
    	   else if (action == 1)
    	   {
    		   if (pd != null)
    			   pd.cancel();
    	   }
    	   else if (action == 2)
    	   {
    		   String status = msg.getData().getString("status");
    		   if (pd != null)
    			   pd.setMessage(status);
    	   }
    	   

       }
   };
   
	 
}