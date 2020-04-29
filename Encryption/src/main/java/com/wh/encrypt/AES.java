package com.wh.encrypt;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public class AES {
	public static String ARITHMETIC = "AES";
	Cipher cipher;
	String key;

	public static void saveFile(byte[] data, File file) throws Exception {
		FileOutputStream fileOutputStream = new FileOutputStream(file);
		saveStream(data, fileOutputStream);
		fileOutputStream.close();
	}

	public static void saveStream(byte[] data, OutputStream outputStream) throws Exception {
		outputStream.write(data, 0, data.length);
	}

	public static void saveStream(byte[] data, DataOutputStream outputStream) throws Exception {
		outputStream.writeInt(data.length);
		outputStream.write(data, 0, data.length);
	}

	public AES() {

	}

	public AES(String password) {
		this.key = password;
	}

	public void setKey(boolean isencrypt)
			throws InvalidKeyException, UnsupportedEncodingException, NoSuchPaddingException, NoSuchAlgorithmException {
		if (key.length() < 8) {
			int start = key.length();
			for (int i = start; i < 16; i++)
				key += "0";
		}
		byte[] raw = key.getBytes("ASCII");
		SecretKeySpec skeySpec = new SecretKeySpec(raw, ARITHMETIC);
		cipher = Cipher.getInstance(ARITHMETIC);
		if (isencrypt)
			cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
		else
			cipher.init(Cipher.DECRYPT_MODE, skeySpec);
	}

	public String encryptStr(String strMing) {
		byte[] byteMi = null;
		byte[] byteMing = null;
		String strMi = "";
		try {
			byteMing = strMing.getBytes("UTF8");
			byteMi = this.encryptByte(byteMing);
			strMi = Base64.getEncoder().encodeToString(byteMi);
		} catch (Exception e) {
			throw new RuntimeException("Error initializing SqlMap class. Cause: " + e);
		} finally {
			byteMing = null;
			byteMi = null;
		}
		return strMi;
	}

	public String decryptStr(String strMi) {
		byte[] byteMing = null;
		byte[] byteMi = null;
		String strMing = "";
		try {
			byteMi = Base64.getDecoder().decode(strMi);
			byteMing = this.decryptByte(byteMi);
			strMing = new String(byteMing, "UTF8");
		} catch (Exception e) {
			throw new RuntimeException("Error initializing SqlMap class. Cause: " + e);
		} finally {
			byteMing = null;
			byteMi = null;
		}
		return strMing;
	}

	private byte[] encryptByte(byte[] byteS) {
		byte[] byteFina = null;
		try {
			setKey(true);
			byteFina = cipher.doFinal(byteS);
		} catch (Exception e) {
			throw new RuntimeException("Error initializing SqlMap class. Cause: " + e);
		}
		cipher = null;
		return byteFina;
	}

	private byte[] decryptByte(byte[] byteD) {
		byte[] byteFina = null;
		try {
			setKey(false);
			byteFina = cipher.doFinal(byteD);
		} catch (Exception e) {
			throw new RuntimeException("Error initializing SqlMap class. Cause: " + e);
		}
		cipher = null;
		return byteFina;
	}

	public void encryptFile(String file, String destFile) throws Exception {
		try (InputStream is = new FileInputStream(file); OutputStream out = new FileOutputStream(destFile);) {
			encryptStream(is, out);
		}
	}

	public void decryptFile(String file, String dest) throws Exception {
		try (InputStream is = new FileInputStream(file); OutputStream out = new FileOutputStream(dest);) {
			decryptStream(is, out);
		}
	}

	public void encryptStream(InputStream is, OutputStream out) throws Exception {
		setKey(true);
		CipherInputStream cis = new CipherInputStream(is, cipher);
		byte[] buffer = new byte[1024];
		int r;
		while ((r = cis.read(buffer)) > 0) {
			out.write(buffer, 0, r);
		}
		cis.close();
	}

	public void decryptStream(InputStream is, OutputStream out) throws Exception {
		setKey(false);
		CipherOutputStream cos = new CipherOutputStream(out, cipher);
		byte[] buffer = new byte[1024];
		int r;
		while ((r = is.read(buffer)) >= 0) {
			cos.write(buffer, 0, r);
		}
		cos.close();
	}

	public static String decryptString(String password, String content) {
		AES des = new AES(password);
		String deStr = des.decryptStr(content);
		return deStr;
	}

	public static String encryptString(String password, String content) {
		AES des = new AES(password);
		String str = des.encryptStr(content);
		return str;
	}

	public static void encryptFile(String password, String sourceFileName, String destFileName) throws Exception {
		AES des = new AES(password);
		des.encryptFile(sourceFileName, destFileName);
	}

	public static void decryptFile(String password, String sourceFileName, String destFileName) throws Exception {
		AES des = new AES(password);
		des.decryptFile(sourceFileName, destFileName);
	}

	public static void encryptStream(String password, InputStream in, OutputStream out) throws Exception {
		AES des = new AES(password);
		des.encryptStream(in, out);
	}

	public static void decryptStream(String password, InputStream in, OutputStream out) throws Exception {
		AES des = new AES(password);
		des.decryptStream(in, out);
	}

}
