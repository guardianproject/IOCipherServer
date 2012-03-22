package info.guardianproject.iocipher.server;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Enumeration;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.ToggleButton;

public class IOCipherServerActivity extends Activity {
	
	private final static String TAG = "IOCipherServer";

	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        ToggleButton tButton = (ToggleButton)findViewById(R.id.toggleButton1);
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
    
    public void startWebServer ()
    {
        Intent intent = new Intent(this, WebServerService.class);
		startService(intent);
		
		//this.bindService(service, conn, flags)
		
		showStatus();
    }
    
    public void stopWebServer ()
    {
    	Intent intent = new Intent(this, WebServerService.class);
   		stopService(intent);
   		
    }
    
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