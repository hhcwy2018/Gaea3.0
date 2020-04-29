package com.wh.encrypt;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.util.Base64;

import javax.crypto.Cipher;

public class Asymmetric {
	public static String ARITHMETIC = "RSA";
	public static final String SIGN_ALGORITHMS = "MD5withRSA";
	public static int SIGN_BITS = 256;
	public static int BITS = 1024;
	public static String DEFAULT_CHARSET = "utf8";

	public static void setArithmetic(String name) {
		ARITHMETIC = name;
	}

	public static class KeyPairString {
		public String publicKey;
		public String privateKey;
	}

	public static byte[] decodeBase64(String text) throws UnsupportedEncodingException {
		return Base64.getDecoder().decode(text.getBytes(StandardCharsets.ISO_8859_1));
	}

	public static String encodeBase64(byte[] buffer) throws UnsupportedEncodingException {
		byte[] encodeBuffer = Base64.getEncoder().encode(buffer);
		return new String(encodeBuffer, 0, encodeBuffer.length, StandardCharsets.ISO_8859_1);
	}

	public static KeyPairString getBase64KeyPairs() throws Exception {
		try (ByteArrayOutputStream publicKeyBuffer = new ByteArrayOutputStream();
				ByteArrayOutputStream privateKeyBuffer = new ByteArrayOutputStream();) {

			getKeyPairs(publicKeyBuffer, privateKeyBuffer);
			publicKeyBuffer.flush();
			privateKeyBuffer.flush();

			KeyPairString keyPair = new KeyPairString();
			keyPair.publicKey = encodeBase64(publicKeyBuffer.toByteArray());
			keyPair.privateKey = encodeBase64(privateKeyBuffer.toByteArray());
			return keyPair;
		}
	}

	/**
	 * 生成秘钥对写入到文件
	 * 
	 * @return
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 */
	public static void getBase64KeyPairs(File publicKeyFile, File privateKeyFile) throws Exception {
		try (FileOutputStream publickeyFileStream = new FileOutputStream(publicKeyFile);
				FileOutputStream privatekeyFileStream = new FileOutputStream(privateKeyFile)) {

			KeyPairString keypair = getBase64KeyPairs();
			publickeyFileStream.write(keypair.publicKey.getBytes(DEFAULT_CHARSET));
			privatekeyFileStream.write(keypair.privateKey.getBytes(DEFAULT_CHARSET));

			publickeyFileStream.flush();
			privatekeyFileStream.flush();
		}
	}

	public static void getKeyPairs(File publicKeyFile, File privateKeyFile)
			throws NoSuchAlgorithmException, IOException {
		getKeyPairs(new FileOutputStream(publicKeyFile), new FileOutputStream(privateKeyFile));
	}

	public static void getKeyPairs(OutputStream publicKeyStream, OutputStream privateKeyStream)
			throws IOException, NoSuchAlgorithmException {
		KeyPair keyPair = getKeyPairs();
		// 获取秘钥对
		PublicKey publicKey = keyPair.getPublic();
		PrivateKey privateKey = keyPair.getPrivate();

		// 直接写入公钥
		try (ObjectOutputStream out_pub = new ObjectOutputStream(publicKeyStream);
				ObjectOutputStream out_pri = new ObjectOutputStream(privateKeyStream);) {
			out_pub.writeObject(publicKey);
			out_pri.writeObject(privateKey);
		}
	}

	public static KeyPair getKeyPairs() throws NoSuchAlgorithmException {
		// 初始化秘钥管理器
		KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(ARITHMETIC);
		keyPairGenerator.initialize(BITS);
		KeyPair keyPair = keyPairGenerator.genKeyPair();
		return keyPair;
	}

	/**
	 * 使用私钥进行签名
	 * 
	 * @return @throws
	 * @throws IOException
	 * @throws FileNotFoundException
	 * @throws SignatureException
	 * @throws ClassNotFoundException
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeyException
	 */
	public static void signatureData(String info, File privateKeyFile, File signatureFile) throws Exception {
		try(FileOutputStream fileOutputStream = new FileOutputStream(signatureFile);){
			fileOutputStream.write(signatureData(info, new FileInputStream(privateKeyFile)));
		}
	}

	public static String signatureBase64Data(String info, String privateKey) throws Exception {
		ByteArrayInputStream privateKeyStream = new ByteArrayInputStream(decodeBase64(privateKey));
		return signatureBase64Data(info, privateKeyStream);
	}

	public static String signatureBase64Data(String info, InputStream privateKeyStream) throws Exception {
		return encodeBase64(signatureData(info, privateKeyStream));
	}

	public static byte[] signatureData(String info, InputStream privateKeyStream) throws Exception {
		// 1.读取生成的私钥对明文进行签名
		try (ObjectInputStream in_pri = new ObjectInputStream(privateKeyStream);) {
			PrivateKey privateKey = (PrivateKey) in_pri.readObject();
			// 初始化签名 对明文开始签名
			Signature signature = Signature.getInstance(SIGN_ALGORITHMS);
			signature.initSign(privateKey);
			signature.update(info.getBytes(DEFAULT_CHARSET));
			// 对信息的数字签名
			byte[] data = signature.sign();
			return data;
		}
	}

	/**
	 * 用公钥进行校验
	 * 
	 * @return
	 * @throws Exception
	 */
	public static void checkSignature(String publicKey, String signatureText, String verifyText) throws Exception {
		checkSignature(new ByteArrayInputStream(decodeBase64(publicKey)),
				new ByteArrayInputStream(decodeBase64(signatureText)),
				new ByteArrayInputStream(verifyText.getBytes(DEFAULT_CHARSET)));
	}

	public static void checkSignature(File publickeyFile, File signatureFile, File verifyFile) throws Exception {
		checkSignature(new FileInputStream(publickeyFile), new FileInputStream(signatureFile),
				new FileInputStream(verifyFile));
	}

	public static void checkSignature(File publickeyFile, String signatureBase64, String verifyText) throws Exception {
		byte[] signatureData = decodeBase64(signatureBase64);
		byte[] verifyData = verifyText.getBytes(DEFAULT_CHARSET);
		checkSignature(new FileInputStream(publickeyFile), new ByteArrayInputStream(signatureData),
				new ByteArrayInputStream(verifyData));
	}

	public static void checkSignature(InputStream publickeyStream, InputStream signatureStream,
			InputStream verifyStream) throws Exception {
		try (
				// 读取公钥
				ObjectInputStream in_pub = new ObjectInputStream(publickeyStream);
				) {

			PublicKey publicKey = (PublicKey) in_pub.readObject();

			byte[] info = new byte[verifyStream.available()];
			verifyStream.read(info);
			// 用公钥进行校验
			byte[] signedbytes = new byte[signatureStream.available()];
			signatureStream.read(signedbytes);
			
			Signature signature = Signature.getInstance(SIGN_ALGORITHMS);
			signature.initVerify(publicKey);
			signature.update(info);
			// 签名信息校验
			if (signature.verify(signedbytes)) {
				return;
			} else {
				throw new Exception("check signature is failed!");
			}
		}
	}

	@SuppressWarnings("unchecked")
	public static <T> T toKey(String publicBase64Key) throws Exception {
		byte[] decoded = decodeBase64(publicBase64Key);
		ObjectInputStream stream = new ObjectInputStream(new ByteArrayInputStream(decoded));
		return (T) stream.readObject();

//		return KeyFactory.getInstance(ARITHMETIC).generatePublic(new X509EncodedKeySpec(decoded));
	}

	public static PublicKey toPublicKey(String publicBase64Key) throws Exception {
		return toKey(publicBase64Key);
//		byte[] decoded = decodeBase64(publicBase64Key);
//		return KeyFactory.getInstance(ARITHMETIC).generatePublic(new X509EncodedKeySpec(decoded));
	}

	public static PrivateKey toPrivateKey(String privateBase64Key) throws Exception {
		return toKey(privateBase64Key);
//		byte[] decoded = decodeBase64(privateBase64Key);  
//        return KeyFactory.getInstance(ARITHMETIC).generatePrivate(new PKCS8EncodedKeySpec(decoded));  
	}

	/**
	 * RSA公钥加密
	 * 
	 * @param str       加密字符串
	 * @param publicKey 公钥
	 * @return 密文
	 * @throws Exception 加密过程中的异常信息
	 */
	public static String encrypt(String text, String publicBase64Key) throws Exception {
		return encrypt(text, toPublicKey(publicBase64Key));
	}

	public static String encrypt(String text, PublicKey pubKey) throws Exception {
		byte[] encyptBuffer = encryptBuffer(text.getBytes(DEFAULT_CHARSET), pubKey);
		return encodeBase64(encyptBuffer);
	}

	public static byte[] encryptBuffer(byte[] sourceBuffer, PublicKey pubKey) throws Exception {
		try (ByteArrayOutputStream bufferStream = new ByteArrayOutputStream();
				DataOutputStream outputStream = new DataOutputStream(bufferStream);) {

			Cipher cipher = Cipher.getInstance(ARITHMETIC);
			cipher.init(Cipher.ENCRYPT_MODE, pubKey);
			int len = sourceBuffer.length;
			int start = 0;
			while (len > 0) {
				int doSize = Math.min(len, BITS / 8 - 11);

				byte[] buffer = cipher.doFinal(sourceBuffer, start, doSize);

				len -= doSize;
				start += doSize;
				outputStream.writeInt(buffer.length);
				outputStream.write(buffer);
			}

			outputStream.flush();
			return bufferStream.toByteArray();
		}
	}

	/**
	 * RSA私钥解密
	 * 
	 * @param str        加密字符串
	 * @param privateKey 私钥
	 * @return 铭文
	 * @throws Exception 解密过程中的异常信息
	 */
	public static String decrypt(String text, String privateBase64Key) throws Exception {
		return decrypt(text, toPrivateKey(privateBase64Key));
	}

	public static String decrypt(String text, PrivateKey privateKey) throws Exception {
		byte[] inputByte = decodeBase64(text);
		byte[] decryptBuffer = decryptBuffer(inputByte, privateKey);
		return new String(decryptBuffer, DEFAULT_CHARSET);
	}

	public static byte[] decryptBuffer(byte[] inputByte, PrivateKey privateKey) throws Exception {
		try (DataInputStream stream = new DataInputStream(new ByteArrayInputStream(inputByte));
				ByteArrayOutputStream outputStream = new ByteArrayOutputStream();) {

			Cipher cipher = Cipher.getInstance(ARITHMETIC);
			cipher.init(Cipher.DECRYPT_MODE, privateKey);

			while (stream.available() > 0) {
				int len = stream.readInt();
				byte[] data = new byte[len];
				stream.read(data);

				outputStream.write(cipher.doFinal(data));
			}
			return outputStream.toByteArray();
		}
	}

}
