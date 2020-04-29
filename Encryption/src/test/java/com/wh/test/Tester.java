package com.wh.test;

import com.wh.encrypt.AES;
import com.wh.encrypt.Asymmetric;
import com.wh.encrypt.Asymmetric.KeyPairString;
import com.wh.encrypt.MD5;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class Tester extends TestCase {
	/**
	 * Create the test case
	 *
	 * @param testName name of the test case
	 */
	public Tester(String testName) {
		super(testName);
	}

	/**
	 * @return the suite of tests being tested
	 */
	public static Test suite() {
		return new TestSuite(Tester.class);
	}

	static String cryptText;
	protected static void print(String name, String msg) {
		cryptText = msg;
		System.out.print(name + ":");
		System.out.println(msg);
	}

	/**
	 * Rigourous Test :-)
	 * @throws Exception 
	 */
	public void testApp() throws Exception {
		String text = "123abc你好测试\n测试abc\nxyz！";
		String password = "123456";
		
		print("text", text);
		print("md5", MD5.encode(text));

		print("aes", "");
		print("encode", AES.encryptString(password, text));
		print("decode", AES.decryptString(password, cryptText));
		
		print("rsa", "");
		KeyPairString keyPair = Asymmetric.getBase64KeyPairs();
		String publicKey = keyPair.publicKey;
		String privateKey = keyPair.privateKey;
		
		print("publickey", publicKey);		
		print("privatekey", privateKey);		
		print("encode", Asymmetric.encrypt(text, publicKey));
		print("decode", Asymmetric.decrypt(cryptText, privateKey));
		
		String sign = Asymmetric.signatureBase64Data(text, privateKey);
		print("sign", sign);
		try {
			Asymmetric.checkSignature(publicKey, sign, text);
			print("check", "ok");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
