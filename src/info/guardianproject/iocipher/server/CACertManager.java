/* Copyright (c) 2011, Nathan Freitas,/ The Guardian Project - https://guardianproject.info */
/* See LICENSE for licensing information */

package info.guardianproject.iocipher.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.text.MessageFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.UUID;

import javax.security.auth.x500.X500Principal;
import javax.security.cert.X509Certificate;

import org.spongycastle.asn1.x509.BasicConstraints;
import org.spongycastle.asn1.x509.ExtendedKeyUsage;
import org.spongycastle.asn1.x509.GeneralName;
import org.spongycastle.asn1.x509.GeneralNames;
import org.spongycastle.asn1.x509.KeyPurposeId;
import org.spongycastle.asn1.x509.KeyUsage;
import org.spongycastle.asn1.x509.X509Extensions;
import org.spongycastle.x509.X509V3CertificateGenerator;

public class CACertManager {

	static {
	    Security.addProvider(new org.spongycastle.jce.provider.BouncyCastleProvider());
	}
	
	private final static String TAG = "CACert";
	
    KeyStore ksCACert;
    public final static String KEYSTORE_TYPE = "BKS";
    
    public CACertManager ()
    {
    	
    }
    
    public void load (String path, String password) throws NoSuchAlgorithmException, KeyStoreException, CertificateException, IOException
    {
    	ksCACert = KeyStore.getInstance(KEYSTORE_TYPE);
    
    	InputStream trustStoreStream = new FileInputStream(new File(path));
    	ksCACert.load(trustStoreStream, password.toCharArray());
    }
    
    public void create (File path, String password, String alias) throws Exception
    {
    	
    	KeyStoreGenerator.generateKeyStore(path, alias, 2048, password, "iocipher", "iocipher", "iocipher", "Brooklyn", "New York", "US");
    	/*
    	ksCACert = KeyStore.getInstance(KEYSTORE_TYPE);
    	ksCACert.load(null, password.toCharArray());
    	
    	KeyPair kp = generateKeyPair ("DSA","SHA1PRNG",1024);
    	   PublicKey pubk = kp.getPublic();
   	    PrivateKey prvk = kp.getPrivate();
   	    
   	    ksCACert.setKeyEntry(alias, prvk, password.toCharArray(), null);
   	    ksCACert.setKeyEntry(alias + ".public", pubk, password.toCharArray(), null);

   	    save(path, password);
   	 	*/
 	
    }
    
    public KeyPair generateKeyPair (String algo, String algo2, int keySize) throws NoSuchAlgorithmException, NoSuchProviderException
    {
    	 KeyPairGenerator keyGen = KeyPairGenerator.getInstance(algo);
         SecureRandom random = SecureRandom.getInstance(algo2);
         keyGen.initialize(keySize, random);
    	    KeyPair kp = keyGen.generateKeyPair();
    	 
    	    return kp;
    }
    
   
    
    public Enumeration<String> getCertificateAliases () throws KeyStoreException
    {
    	return ksCACert.aliases();
    	
    	
    }
    
    public int size ()  throws KeyStoreException
    {
    	return ksCACert.size();
    }
  
    public Certificate getCertificate (String alias) throws KeyStoreException
    {
    	return ksCACert.getCertificate(alias);
    	
    }
    
    public Certificate[] getCertificateChain (String alias) throws KeyStoreException
    {
    	return ksCACert.getCertificateChain(alias);
    }

    public void addCertificate (String alias, Certificate cert) throws KeyStoreException
    {
    	ksCACert.setCertificateEntry(alias, cert);
    }
    
    public void delete(String alias)  throws KeyStoreException
    {
    	ksCACert.deleteEntry(alias);
    	
    }
    
    public void delete(Certificate cert)  throws KeyStoreException
    {
    	ksCACert.deleteEntry(ksCACert.getCertificateAlias(cert));
    	
    }
    
    public void save (File fileNew, String password) throws NoSuchAlgorithmException, KeyStoreException, CertificateException, IOException
    {
    	if (fileNew.exists() && (!fileNew.canWrite()))
    		throw new FileNotFoundException("Cannot write to: " + fileNew.getAbsolutePath());
    	else if (fileNew.getParentFile().exists() && (!fileNew.getParentFile().canWrite()))
    		throw new FileNotFoundException("Cannot write to: " + fileNew.getAbsolutePath());
    	
    	OutputStream trustStoreStream = new FileOutputStream(fileNew);
    	ksCACert.store(trustStoreStream, password.toCharArray());
    }
    
    public String getFingerprint (X509Certificate cert, String type)
    {
    	 try {
             MessageDigest md = MessageDigest.getInstance(type);
             byte[] publicKey = md.digest(cert.getPublicKey().getEncoded());

             StringBuffer hexString = new StringBuffer();
             for (int i=0;i<publicKey.length;i++) {
                 
            	 String appendString = Integer.toHexString(0xFF & publicKey[i]);

                 if(appendString.length()==1)
                	 hexString.append("0");
                 hexString.append(appendString);
                 hexString.append(' ');
             }

             	return hexString.toString();

         } catch (NoSuchAlgorithmException e1) {
             e1.printStackTrace();
             return null;
         } 
    }
    
    
}
