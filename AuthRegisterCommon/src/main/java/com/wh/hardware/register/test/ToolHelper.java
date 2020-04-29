package com.wh.hardware.register.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

import com.wh.encrypt.Asymmetric;

public class ToolHelper {
	public static File getRoot() {
		String path = System.getProperty("user.dir");
		return new File(path);
	}
	
	public static File getFile(String dir, String name) {
		File path = getRoot();
		if (dir != null)
			path = new File(path, dir);
		
		return new File(path, name);
	}
	
	public static String readLine(String msg) {
		System.out.print(msg + ":");
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
			return reader.readLine();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static void print(String name, String msg) {
		System.out.print(name + ":");
		System.out.println(msg);
	}

	public static File getPublicKeyFile(String name) {
		return ToolHelper.getFile("config", name + "_pub.key");
	}

	public static File getPriavteKeyFile(String name) {
		return ToolHelper.getFile("config", name + "_pri.key");
	}

	public static void genKeyFile(String name) throws Exception {
		File publicKeyFile = getPublicKeyFile(name);
		File privateKeyFile = getPriavteKeyFile(name);
		Asymmetric.getBase64KeyPairs(publicKeyFile, privateKeyFile);
	}

}
