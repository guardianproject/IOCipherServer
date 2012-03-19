package info.guardianproject.iocipher.server;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class IOCipherServerActivity extends Activity {
	
	private final static String TAG = "IOCipherServer";

	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        Intent intent = new Intent(this, WebServerService.class);
		startService(intent);
		
		
    }
    
    
    
	@Override
	protected void onStart() {
		super.onStart();
		
		
	}

		
}