package info.guardianproject.iocipher.server;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.Security;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Calendar;
import java.util.Date;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;


public class KeyStoreGenerator {

	
	
public static void generateKeyStore(File keyStoreFile, String alias, int keyLength, String password, String cn, String o, String ou, String l, String st, String c) throws Exception {
   
        final java.security.KeyPairGenerator rsaKeyPairGenerator = java.security.KeyPairGenerator.getInstance("RSA");
        rsaKeyPairGenerator.initialize(keyLength);
        final KeyPair rsaKeyPair = rsaKeyPairGenerator.generateKeyPair();

        // Generate the key store de type JCEKS
        Provider[] ps = Security.getProviders();
        for (int i = 0; i < ps.length; i++)
            System.out.println("" + ps[i].getName());

        final KeyStore ks = KeyStore.getInstance("BKS");
        ks.load(null);

        final RSAPublicKey rsaPublicKey = (RSAPublicKey) rsaKeyPair.getPublic();

        char[] pw = password.toCharArray();

        final RSAPrivateKey rsaPrivateKey = (RSAPrivateKey) rsaKeyPair.getPrivate();
        final java.security.cert.X509Certificate certificate = makeCertificate(rsaPrivateKey, rsaPublicKey, cn, o, ou, l, st, c);
        final java.security.cert.X509Certificate[] certificateChain = { certificate };

        ks.setKeyEntry(alias, rsaKeyPair.getPrivate(), pw, certificateChain);

        final FileOutputStream fos = new FileOutputStream(
                keyStoreFile);
        ks.store(fos, pw);
        fos.close();
        System.out.println(keyStoreFile.getAbsolutePath());

   
}

public static X509Certificate makeCertificate(PrivateKey issuerPrivateKey,
        PublicKey subjectPublicKey, String cn, String o, String ou, String l, String st, String c) throws Exception {

    final org.spongycastle.asn1.x509.X509Name issuerDN = new org.spongycastle.asn1.x509.X509Name(
            "CN="+cn+", OU="+ou+", O="+o+", L="+l+", ST="+st+", C="+c);

    final org.spongycastle.asn1.x509.X509Name subjectDN = new org.spongycastle.asn1.x509.X509Name(
            "CN="+cn+", OU="+ou+", O="+o+", L="+l+", ST="+st+", C="+c);
    final int daysTillExpiry = 10 * 365;

    final Calendar expiry = Calendar.getInstance();
    expiry.add(Calendar.DAY_OF_YEAR, daysTillExpiry);

    final org.spongycastle.x509.X509V3CertificateGenerator certificateGenerator = new org.spongycastle.x509.X509V3CertificateGenerator();

    certificateGenerator.setSerialNumber(java.math.BigInteger
            .valueOf(System.currentTimeMillis()));
    certificateGenerator.setIssuerDN(issuerDN);

    certificateGenerator.setSubjectDN(subjectDN);
    certificateGenerator.setPublicKey(subjectPublicKey);
    certificateGenerator.setNotBefore(new Date());
    certificateGenerator.setNotAfter(expiry.getTime());

    certificateGenerator.setSignatureAlgorithm("MD5WithRSA");

    return certificateGenerator.generate(issuerPrivateKey);
}

}
