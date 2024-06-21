package se.voxelmanip.apksigner;

import android.content.Context;

import com.android.apksig.ApkSigner;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Collections;

public class APKSigner {

	private final Context mContext;

	public APKSigner(Context context) {
		mContext = context;
	}
	
	private byte[] readFileFromAssets(Context context, String fileName) {
		byte[] byteArray = null;
		try {
			InputStream inputStream = context.getAssets().open(fileName);
			int size = inputStream.available();
			byteArray = new byte[size];
			inputStream.read(byteArray);
			inputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return byteArray;
	}

	private PrivateKey getPrivateKey() throws NoSuchAlgorithmException, InvalidKeySpecException {
		byte[] keyBytes = readFileFromAssets(mContext, "key.pk8");
		PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
		KeyFactory kf = KeyFactory.getInstance("RSA");
		return kf.generatePrivate(spec);
	}

	private X509Certificate getCertificate() throws CertificateException {
		byte[] certBytes = readFileFromAssets(mContext, "key.crt");
		InputStream inputStream = new ByteArrayInputStream(certBytes);
		return (X509Certificate)CertificateFactory
				.getInstance("X509").generateCertificate(inputStream);
	}

	public void sign(File apkFile, File output) throws Exception {
		ApkSigner.SignerConfig signerConfig = new ApkSigner.SignerConfig.Builder("CERT", getPrivateKey(), Collections.singletonList(getCertificate())).build();
		ApkSigner.Builder builder = new ApkSigner.Builder(Collections.singletonList(signerConfig));
		builder.setInputApk(apkFile);
		builder.setOutputApk(output);
		builder.setCreatedBy("se.voxelmanip.apksigner");
		ApkSigner signer = builder.build();
		signer.sign();
	}
}
