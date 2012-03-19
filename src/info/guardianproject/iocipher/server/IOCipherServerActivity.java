package info.guardianproject.iocipher.server;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
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
		
		
    }
    
    public void stopWebServer ()
    {
    	Intent intent = new Intent(this, WebServerService.class);
   		stopService(intent);
   		
    }
    
    
    
	@Override
	protected void onStart() {
		super.onStart();
		
		
	}

		
}