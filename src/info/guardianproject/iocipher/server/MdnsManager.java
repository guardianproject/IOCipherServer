package info.guardianproject.iocipher.server;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;

public class MdnsManager {

	private JmDNS mJmdns;
	 private Map<String, ServiceInfo> regMap; 
	 private Context mContext;
	 private MulticastLock mcLock;
	 
	public MdnsManager (Context context) throws IOException
	{
		mContext = context;
		 mJmdns = JmDNS.create();
		 regMap = new HashMap<String, ServiceInfo>();


	}
	
	public ServiceInfo[] list (String type)
	{
		  final ServiceInfo[] svcInfos = mJmdns.list(type);
	
		  return svcInfos;
	}
	
	public ServiceInfo register (String alias, String type, String name, int port, String text) throws IOException
	{
		  ServiceInfo svcInfo = ServiceInfo.create(type, name, port, text);
		
		   mJmdns.registerService(svcInfo);
		   regMap.put(alias, svcInfo);
		 
		   /*
		   WifiManager wifi = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
		   if(wifi != null)
		   {
		       mcLock = wifi.createMulticastLock("myLock");
		       mcLock.acquire();
		   }*/
		   return svcInfo;
	}
	
	public ServiceInfo unregister (String alias) {
	  final ServiceInfo svcInfo = this.regMap.remove(alias);
	  if (svcInfo != null) {
	   mJmdns.unregisterService(svcInfo);
	  }
	  
	  /*
	  if(mcLock != null && mcLock.isHeld())
	   {
	       mcLock.release();
	   }*/
	  
	  return svcInfo;
	 }
}

/*
//Get the Multicast Lock
WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
if(wifi != null)
{
    mcLock = wifi.createMulticastLock("myLock");
    mcLock.acquire();
}

...../

//Release the lock

//Release the Lock to save battery power
   if(mcLock.isHeld())
   {
       mcLock.release();
   }
*/