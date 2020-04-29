package com.wh.gaea.install.control;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.json.JSONObject;

import com.wh.gaea.install.interfaces.InstallConfigureInfo;
import com.wh.gaea.install.interfaces.PageConfigureInfo;
import com.wh.tools.FileHelp;

public class CommandRunner {
	public static class ParameterException extends Exception {

		private static final long serialVersionUID = 1L;

		public ParameterException(String msg) {
			super(msg);
		}
	}

	File destDir;
	InstallConfigureInfo installInfo;

	public interface INotifyCommand {
		void notify(CommandType commandType, Enum<?> indicate, Object[] args);
	}

	public CommandRunner(InstallConfigureInfo installInfo) {
		this.destDir = installInfo.installDir.getAbsoluteFile();
		this.installInfo = installInfo;
	}

	public static File getInstallResourcePath(String name) {
		return new File(getInstallResourcePath(), name);
	}

	public static File getInstallResourcePath() {
		return FileHelp.getRootPath();
	}

	public enum CommandType {
		ctCopyDir(0, "拷贝目录"), ctCopyFile(1, "拷贝文件"), ctDelDir(2,
				"删除目录"), ctDelFile(3, "删除目录"), ctRun(4, "运行指令"), ctConfigure(4,
						"配置文件");

		int code;
		String msg;

		private CommandType(int code, String msg) {
			this.code = code;
			this.msg = msg;
		}

		@Override
		public String toString() {
			return msg;
		}

		public int getCode() {
			return code;
		}
	}

	public interface IIndicate {

	}

	public enum CopyCommandIndicate implements IIndicate {
		ciExistAndContinue(0, "目标存在则继续"), ciExistAndFail(0,
				"目标存在则退出"), ciExistAndReplace(0, "目标存在则替换");

		int code;
		String msg;

		private CopyCommandIndicate(int code, String msg) {
			this.code = code;
			this.msg = msg;
		}

		@Override
		public String toString() {
			return msg;
		}

		public int getCode() {
			return code;
		}
	}

	public enum DeleteCommandIndicate implements IIndicate {
		ciAnyWay(0, "删除失败继续"), ciNotDeleteAndFail(0, "删除失败退出");

		int code;
		String msg;

		private DeleteCommandIndicate(int code, String msg) {
			this.code = code;
			this.msg = msg;
		}

		@Override
		public String toString() {
			return msg;
		}

		public int getCode() {
			return code;
		}
	}

	public enum RunCommandIndicate implements IIndicate {
		ciWait(0, "等待命令执行完毕"), ciNoWait(0, "不等待继续执行");

		int code;
		String msg;

		private RunCommandIndicate(int code, String msg) {
			this.code = code;
			this.msg = msg;
		}

		@Override
		public String toString() {
			return msg;
		}

		public int getCode() {
			return code;
		}
	}

	public enum GaeaCommandIndicate implements IIndicate {
		ciNormal(0, "正常安装");

		int code;
		String msg;

		private GaeaCommandIndicate(int code, String msg) {
			this.code = code;
			this.msg = msg;
		}

		@Override
		public String toString() {
			return msg;
		}

		public int getCode() {
			return code;
		}
	}

	public enum ConfigureCommandIndicate implements IIndicate {
		ciFailAndFail(0, "配置失败退出"), ciFailAndContinue(0, "配置失败继续");

		int code;
		String msg;

		private ConfigureCommandIndicate(int code, String msg) {
			this.code = code;
			this.msg = msg;
		}

		@Override
		public String toString() {
			return msg;
		}

		public int getCode() {
			return code;
		}
	}

	public void copyCommand(CopyCommandIndicate indicate,
			INotifyCommand notifyCommand, Object... args) throws Exception {
		if (args.length < 2) {
			throw new ParameterException(
					"拷贝目录指令的参数【" + Arrays.deepToString(args) + "】错误！");
		}

		File source = new File(getInstallResourcePath(), (String) args[0]);
		for (int i = 1; i < args.length; i++) {
			File dest = new File(destDir, (String) args[i]);
			if (dest.exists()) {
				switch (indicate) {
					case ciExistAndFail :
						throw new FileAlreadyExistsException(
								dest.getAbsolutePath());
					case ciExistAndContinue :
						continue;
					case ciExistAndReplace :
						break;
				}
			}

			notifyCommand.notify(
					source.isDirectory()
							? CommandType.ctCopyDir
							: CommandType.ctCopyFile,
					indicate, new Object[]{source, dest});

			if (source.isDirectory())
				FileHelp.copyFilesTo(source, dest);
			else {
				FileHelp.copyFileTo(source, dest);
			}
		}
		
		Thread.sleep(1000);
	}

	public void deleteCommand(DeleteCommandIndicate indicate,
			INotifyCommand notifyCommand, Object... args) throws Exception {
		if (args.length < 1) {
			throw new ParameterException(
					"删除目录指令的参数【" + Arrays.deepToString(args) + "】错误！");
		}

		for (int i = 0; i < args.length; i++) {
			File dest = new File(destDir, (String) args[i]);

			boolean isok = false;

			notifyCommand.notify(
					dest.isDirectory()
							? CommandType.ctDelDir
							: CommandType.ctDelFile,
					indicate, new Object[]{dest});

			if (dest.isDirectory())
				FileHelp.delDir(dest);
			else {
				isok = FileHelp.DeleteFile(dest);
			}
			if (!isok) {
				switch (indicate) {
					case ciAnyWay :
						break;
					case ciNotDeleteAndFail :
						throw new IOException("删除文件【" + dest + "】失败！");
				}
			}
		}
	}

	public void runCommand(RunCommandIndicate indicate,
			INotifyCommand notifyCommand, Object... args) throws Exception {
		if (args.length < 1) {
			throw new ParameterException(
					"运行指令的参数【" + Arrays.deepToString(args) + "】错误！");
		}

		int start = 2;
		boolean checkResult = (boolean) args[0];
		long waitTime = (long)args[1];
		
		for (int i = start; i < args.length; i++) {
			String command = (String) args[i];
			if (command.startsWith("\\") || command.startsWith("/")) {
				command = destDir.getAbsolutePath() + File.separator + command.substring(1);
			}
			
			notifyCommand.notify(CommandType.ctRun, indicate,
					new Object[]{command});

			Process process = Runtime.getRuntime().exec(command);
			switch (indicate) {
				case ciNoWait :
					break;
				case ciWait :
					int ret = 0;
					if (waitTime == -1)
						ret = process.waitFor();
					else {
						ret = process.waitFor(waitTime, TimeUnit.SECONDS) ? 0 : -1;
						if (ret != 0 && process.isAlive())
							process.destroy();
					}
					
					if (checkResult && ret != 0) {
						throw new IOException("执行指令【" + command + "】失败！");
					}
					
					break;
			}
		}
	}

	public void configureCommand(ConfigureCommandIndicate indicate,
			INotifyCommand notifyCommand, Object... args) throws Exception {
		if (args.length < 1) {
			throw new ParameterException(
					"配置指令的参数【" + Arrays.deepToString(args) + "】错误！");
		}

		for (int i = 0; i < args.length; i++) {
			Object arg = args[i];
			PageConfigureInfo pageInfo;
			if (arg instanceof JSONObject) {
				pageInfo = new PageConfigureInfo((JSONObject) arg);
				pageInfo = installInfo.get(pageInfo.id);
			} else
				pageInfo = (PageConfigureInfo) arg;

			notifyCommand.notify(CommandType.ctConfigure, indicate,
					new Object[]{pageInfo});

			try {
				pageInfo.saveToFile(destDir);
			} catch (Exception e) {
				switch (indicate) {
					case ciFailAndContinue :
						break;
					case ciFailAndFail :
						throw e;
				}
			}
		}
	}

	public void execute(CommandType commandType, IIndicate indicate,
			INotifyCommand notifyCommand, Object... args) throws Exception {
		switch (commandType) {
			case ctCopyDir :
			case ctCopyFile : {
				copyCommand((CopyCommandIndicate) indicate, notifyCommand,
						args);
				break;
			}
			case ctDelDir :
			case ctDelFile : {
				deleteCommand((DeleteCommandIndicate) indicate, notifyCommand,
						args);
				break;
			}
			case ctRun : {
				runCommand((RunCommandIndicate) indicate, notifyCommand, args);
				break;
			}
			case ctConfigure : {
				configureCommand((ConfigureCommandIndicate) indicate,
						notifyCommand, args);
				break;
			}
		}
	}
}
