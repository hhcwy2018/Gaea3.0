package com.wh.tools;

import java.io.UnsupportedEncodingException;

import javax.swing.SwingUtilities;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.platform.win32.WinUser.WNDENUMPROC;
import com.sun.jna.win32.StdCallLibrary;

public class WindowHelper {
	public interface User32Api extends StdCallLibrary {
		User32Api INSTANCE = (User32Api) Native.load("user32", User32Api.class);

		boolean EnumWindows(WinUser.WNDENUMPROC lpEnumFunc, Pointer arg);

		int GetWindowThreadProcessId(HWND hwnd, Pointer lpdwProcessld);

		int GetWindowTextA(HWND hWnd, byte[] lpString, int nMaxCount);

		boolean SetForegroundWindow(HWND hWnd);

		void SwitchToThisWindow(HWND hWnd, boolean fAltTab);

		Long GetWindowLongA(HWND hWnd, int nlndex);

		Pointer GetWindowLongPtr(HWND hWnd, int nIndex);

		default Long getWindowLong(HWND hWnd, int nlndex) {
			try {
				return GetWindowLongA(hWnd, nlndex);
			} catch (Throwable e) {
				Pointer pointer = GetWindowLongPtr(hWnd, nlndex);
				return pointer.getLong(0);
			}
		}

		default String getWindowText(HWND hWnd) {
			byte[] windowText = new byte[512];
			int w = GetWindowTextA(hWnd, windowText, 512);
			try {
				return new String(windowText, 0, w, "gbk");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				return null;
			}
		}

		default boolean isWindowVisible(HWND hWnd) {
			return (getWindowLong(hWnd, User32.GWL_STYLE) & User32.WS_VISIBLE) != 0;
		}
	}

	static final User32Api user32 = User32Api.INSTANCE;

	public static void bringProcessToTop(int dwProcessId) {
		user32.EnumWindows(new WNDENUMPROC() {
			public boolean callback(HWND hWnd, Pointer arg1) {
				Pointer processId = new Memory(64);
				user32.GetWindowThreadProcessId(hWnd, processId);
				if (processId.getInt(0) == dwProcessId) {

					boolean bVisible = user32.isWindowVisible(hWnd);
					if (bVisible) {
						SwingUtilities.invokeLater(new Runnable() {

							@Override
							public void run() {
								user32.SwitchToThisWindow(hWnd, true);
							}
						});
						return false;
					}
				}
				return true;
			}
		}, null);

	}
}
