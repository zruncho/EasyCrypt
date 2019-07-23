package com.hunterdavis.easyfileencrypt;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import javax.crypto.*;
import javax.crypto.spec.*;

import android.util.Base64;

import android.os.Handler;
import android.widget.ProgressBar;


/**
 * Usage:
 * 
 * <pre>
 * String crypto = SimpleCrypto.encrypt(masterpassword, cleartext)
 * ...
 * String cleartext = SimpleCrypto.decrypt(masterpassword, crypto)
 * </pre>
 * 
 * @author ferenc.hechler
 */
public class SimpleCrypto {
	IvParameterSpec IvParameters = new IvParameterSpec(
			new byte[] { 13, 33, 55, 68, 80, 82, 45, 23 });

    private static final char[] PASSWORD = "enfldsgbnlsngdlksdsgm".toCharArray();
    private static final byte[] SALT = {
            (byte) 0xde, (byte) 0x33, (byte) 0x10, (byte) 0x12,
            (byte) 0xde, (byte) 0x33, (byte) 0x10, (byte) 0x12,
    };


/*	public byte[] encryptBytes(byte[] seed, byte[] cleartext)
			throws Exception {
		byte[] rawKey = getRawKey(seed);
		return encrypt(rawKey, cleartext);
	}

	public byte[] decryptBytes(byte[] seed, byte[] encrypted)
			throws Exception {
		byte[] rawKey = getRawKey(seed);
		return decrypt(rawKey, encrypted);
	}

	public String encrypt(String seed, String cleartext)
			throws Exception {
		byte[] rawKey = getRawKey(seed.getBytes());
		byte[] result = encrypt(rawKey, cleartext.getBytes());
		return toHex(result);
	}

	public String decrypt(String seed, String encrypted)
			throws Exception {
		byte[] rawKey = getRawKey(seed.getBytes());
		byte[] enc = toByte(encrypted);
		byte[] result = decrypt(rawKey, enc);
		return new String(result);
	}*/

	public Boolean encryptFile(File in, File out, String seed, Handler handler, ProgressBar Bar ) throws IOException,
			InvalidKeyException, InvalidAlgorithmParameterException {
		byte[] raw = null;
		long FileLen = in.length();
		try {
			raw = getRawKey(seed.getBytes());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		SecretKeySpec skeySpec = new SecretKeySpec(raw, "DESede");
		Cipher cipher = null;
		try {
			cipher = Cipher.getInstance("DESede/CBC/PKCS5Padding");
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		cipher.init(Cipher.ENCRYPT_MODE, skeySpec, IvParameters);
		

		FileInputStream is = new FileInputStream(in);
		CipherOutputStream os = new CipherOutputStream(
				new FileOutputStream(out), cipher);

		copy(is, os, handler, Bar, FileLen);

		os.close();
		return true;
	}
	


	public Boolean decryptFile(File in, File out, String seed, Handler handler, ProgressBar Bar) throws IOException,
            InvalidKeyException, InvalidAlgorithmParameterException {
		byte[] raw = null;
		long FileLen=in.length();
		try {
			raw = getRawKey(seed.getBytes());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		SecretKeySpec skeySpec = new SecretKeySpec(raw, "DESede");
		Cipher cipher = null;
		try {
			cipher = Cipher.getInstance("DESede/CBC/PKCS5Padding");
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		cipher.init(Cipher.DECRYPT_MODE, skeySpec, IvParameters);
		
		CipherInputStream is = new CipherInputStream(new FileInputStream(in),
				cipher);
		FileOutputStream os = new FileOutputStream(out);

		copy(is, os, handler, Bar, FileLen);

		is.close();
		os.close();
		return true;
	}
	
	/*private void copy(InputStream is, OutputStream os) throws IOException {
	    int i;
	    byte[] b = new byte[1024];
	    while((i=is.read(b))!=-1) {
	      os.write(b, 0, i);
	    }
	  }*/

	private void copy(InputStream is, OutputStream os, Handler handler, final ProgressBar bar, long length) throws IOException {
		int i;
		byte[] b = new byte[1024];
		int barmax =(int) Math.ceil((double)(length/1024));
		float barstep = (barmax==0)?100:(float) (100.0/ barmax);
		float barproc=0;
		final int[] currproc={0};

		while((i=is.read(b))!=-1) {
			os.write(b, 0, i);
			barproc+=barstep;
			if((int)barproc > currproc[0])
			{
				currproc[0]=(int)barproc;
				handler.post(new Runnable() {
					public void run() {
						bar.setProgress(currproc[0]);
						}

				});
			}
		}
	}





	private static byte[] getRawKey(byte[] seed) throws Exception {
        /*		KeyGenerator kgen = KeyGenerator.getInstance("AES");
		SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
		sr.setSeed(seed);
		kgen.init(128, sr); // 192 and 256 bits may not be available
		SecretKey skey = kgen.generateKey(); */

		// Create an array to hold the key
		//byte[] encryptKey = "This is a test DESede key".getBytes();

		MessageDigest sha = MessageDigest.getInstance("SHA-1");
		seed = sha.digest(seed);
		seed = Arrays.copyOf(seed,DESedeKeySpec.DES_EDE_KEY_LEN);


		// Create a DESede key spec from the key
		DESedeKeySpec spec = new DESedeKeySpec(seed);

		// Get the secret key factor for generating DESede keys
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(
				"DESede");

	// Generate a DESede SecretKey object
		SecretKey theKey = keyFactory.generateSecret(spec);

		byte[] raw = theKey.getEncoded();
		return raw;
	}


	public String encrypt(String property, char [] Password) throws GeneralSecurityException, UnsupportedEncodingException {
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
        SecretKey key = keyFactory.generateSecret(new PBEKeySpec(Password));
        Cipher pbeCipher = Cipher.getInstance("PBEWithMD5AndDES");
        pbeCipher.init(Cipher.ENCRYPT_MODE, key, new PBEParameterSpec(SALT, 20));
        return base64Encode(pbeCipher.doFinal(property.getBytes()));
    }

    private static String base64Encode(byte[] bytes) {
        // NB: This class is internal, and you probably should use another impl
        return Base64.encodeToString(bytes,Base64.DEFAULT);
    }

    public String decrypt(String property,char [] Password) throws GeneralSecurityException, IOException {
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
        SecretKey key = keyFactory.generateSecret(new PBEKeySpec(Password));
        Cipher pbeCipher = Cipher.getInstance("PBEWithMD5AndDES");
        pbeCipher.init(Cipher.DECRYPT_MODE, key, new PBEParameterSpec(SALT, 20));
        return new String(pbeCipher.doFinal(base64Decode(property)));
    }

    private static byte[] base64Decode(String property) throws IOException {
        // NB: This class is internal, and you probably should use another impl
        return Base64.decode(property,Base64.DEFAULT);
    }





    /*private byte[] encrypt(byte[] raw, byte[] clear) throws Exception {
		SecretKeySpec skeySpec = new SecretKeySpec(raw, "DESede");
		Cipher cipher = Cipher.getInstance("DESede/CBC/PKCS5Padding");
		cipher.init(Cipher.ENCRYPT_MODE, skeySpec, IvParameters );
		byte[] encrypted = cipher.doFinal(clear);
		return encrypted;
	}

	private byte[] decrypt(byte[] raw, byte[] encrypted)
			throws Exception {
		SecretKeySpec skeySpec = new SecretKeySpec(raw, "DESede");
		Cipher cipher = Cipher.getInstance("DESede/CBC/PKCS5Padding");
		cipher.init(Cipher.DECRYPT_MODE, skeySpec,IvParameters);
		byte[] decrypted = cipher.doFinal(encrypted);
		return decrypted;
	}

	public static String toHex(String txt) {
		return toHex(txt.getBytes());
	}

	public static String fromHex(String hex) {
		return new String(toByte(hex));
	}

	public static byte[] toByte(String hexString) {
		int len = hexString.length() / 2;
		byte[] result = new byte[len];
		for (int i = 0; i < len; i++)
			result[i] = Integer.valueOf(hexString.substring(2 * i, 2 * i + 2),
					16).byteValue();
		return result;
	}

	public static String toHex(byte[] buf) {
		if (buf == null)
			return "";
		StringBuffer result = new StringBuffer(2 * buf.length);
		for (int i = 0; i < buf.length; i++) {
			appendHex(result, buf[i]);
		}
		return result.toString();
	}

	private final static String HEX = "0123456789ABCDEF";

	private static void appendHex(StringBuffer sb, byte b) {
		sb.append(HEX.charAt((b >> 4) & 0x0f)).append(HEX.charAt(b & 0x0f));
	}*/

}
