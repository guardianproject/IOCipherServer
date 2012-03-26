/* Copyright (c) 2009, Nathan Freitas, Orbot / The Guardian Project - http://openideals.com/guardian */
/* See LICENSE for licensing information */

package info.guardianproject.iocipher.server;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;


public class IOCipherSettingsActivity 
		extends PreferenceActivity implements OnPreferenceClickListener {


	private boolean hasRoot = false;
	

	
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		hasRoot = prefs.getBoolean("has_root",false);
		
	}
	
	
	@Override
	protected void onResume() {
	
		super.onResume();
	
				
		
	};
	
	
	
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onStop()
	 */
	@Override
	protected void onStop() {
		super.onStop();
		
		//Log.d(getClass().getName(),"Exiting Preferences");
	}

	public boolean onPreferenceClick(Preference preference) {
		
		
		
		return true;
	}

	
}
