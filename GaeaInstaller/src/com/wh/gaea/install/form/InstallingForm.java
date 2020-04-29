package com.wh.gaea.install.form;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.io.File;
import java.io.FileInputStream;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import com.wh.gaea.install.control.CommandRunner;
import com.wh.gaea.install.control.CommandRunner.CommandType;
import com.wh.gaea.install.control.CommandRunner.INotifyCommand;
import com.wh.gaea.install.control.CommandRunner.RunCommandIndicate;
import com.wh.gaea.install.interfaces.CommandConfigureInfo;
import com.wh.gaea.install.interfaces.InstallConfigureInfo;
import com.wh.gaea.install.interfaces.PageConfigureInfo;
import com.wh.swing.tools.MsgHelper;
import com.wh.tools.FileHelp;
import com.wh.tools.FileHelp.IDelDir;
import com.wh.tools.ZipManager;

public class InstallingForm extends BaseForm {

	private static final long serialVersionUID = -3469632872672185703L;
	private JPanel contentView;

	public interface IInstall {
		void onSetupEnd();
		void onSetupFail(Throwable e);
	}

	class ExecuteThread extends Thread {
		protected void fireFail(Throwable e) {
			try {
				SwingUtilities.invokeAndWait(new Runnable() {

					@Override
					public void run() {
						install.onSetupFail(e);
					}
				});
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}

		protected void fireOk() {
			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					install.onSetupEnd();
				}
			});
		}

		protected File getInstallDestFile(String name) {
			return new File(installInfo.installDir, name);
		}

		protected String getInstallDestPath(String name) {
			return getInstallDestFile(name).getAbsolutePath();
		}

		protected void setProgressMax(int max) {
			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					progressBar.setMaximum(max);
				}
			});
		}

		protected void incProgressMax() {
			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					progressBar.setMaximum(progressBar.getMaximum() + 1);
				}
			});
		}

		protected void setProgressValue(int value) {
			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					progressBar.setValue(value);
					progressBar.updateUI();
				}
			});
		}

		protected void incProgressValue() {
			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					progressBar.setValue(progressBar.getValue() + 1);
					progressBar.updateUI();
				}
			});
		}

		protected boolean executeCommand(CommandRunner commandRunner,
				String command, boolean checkResult, long waitTime,
				String title, String msg) {
			try {
				commandRunner.execute(CommandType.ctRun,
						RunCommandIndicate.ciWait, new INotifyCommand() {

							@Override
							public void notify(CommandType commandType,
									Enum<?> indicate, Object[] args) {
								titleView.setText(title);
								msgView.setText(msg);
							}
						}, checkResult, waitTime, command);
				return true;
			} catch (Exception e) {
				e.printStackTrace();
				fireFail(e);
				return false;
			}

		}

		IDelDir iDelDir = new IDelDir() {

			@Override
			public boolean prepareDeleteFile(File file) {
				return true;
			}

			@Override
			public boolean deletedFile(File file, boolean isok) {
				SwingUtilities.invokeLater(new Runnable() {

					@Override
					public void run() {
						msgView.setText("删除【" + file.getAbsolutePath() + "】"
								+ (isok ? "成功" : "失败"));
					}
				});
				return isok;
			}
		};
		@Override
		public void run() {
			CommandRunner commandRunner = new CommandRunner(installInfo);

			setProgressMax(installInfo.commands.size());
			setProgressValue(0);

			if (installInfo.includeGaeaRuntime) {
				incProgressMax();

				File gaeaRuntimeRootDir = new File(installInfo.gaeaRuntimePath,
						"xampp");

				File command = new File(gaeaRuntimeRootDir,
						"\\apache\\bin\\httpd.exe");

				if (gaeaRuntimeRootDir.exists()) {
					if (command.exists() && !executeCommand(commandRunner,
							command.getAbsolutePath() + " -k stop", true, -1,
							"正在停止apache服务",
							gaeaRuntimeRootDir.getAbsolutePath()))
						return;

					try {
						titleView.setText("正在删除现有Gaea运行环境");
						msgView.setText(gaeaRuntimeRootDir.getAbsolutePath());
						FileHelp.delDir(gaeaRuntimeRootDir, iDelDir);

						Thread.sleep(1000);
					} catch (Exception e1) {
						e1.printStackTrace();
						fireFail(e1);
						return;
					}
				}

				titleView.setText("正在建立Gaea运行环境");
				msgView.setText(gaeaRuntimeRootDir.getAbsolutePath());
				try (FileInputStream stream = new FileInputStream(
						CommandRunner.getInstallResourcePath("xampp.zip"));) {
					ZipManager.UnZipFolder(stream,
							installInfo.gaeaRuntimePath.getAbsolutePath(),
							null);
					incProgressValue();
				} catch (Exception e) {
					e.printStackTrace();
					fireFail(e);
					return;
				}

				if (command.exists() && !executeCommand(commandRunner,
						command.getAbsolutePath() + " -k stop", true, -1,
						"正在停止apache服务", gaeaRuntimeRootDir.getAbsolutePath()))
					return;

				if (command.exists() && !executeCommand(commandRunner,
						command.getAbsolutePath()
								+ " -k uninstall -n \"Apache 2.4\"",
						false, -1, "正在删除apache服务",
						gaeaRuntimeRootDir.getAbsolutePath()))
					return;

				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					fireFail(e);
					return;
				}

				if (command.exists() && !executeCommand(commandRunner,
						command.getAbsolutePath()
								+ " -k install -n \"Apache 2.4\"",
						false, 5, "正在安装apache服务",
						gaeaRuntimeRootDir.getAbsolutePath()))
					return;

				if (command.exists() && !executeCommand(commandRunner,
						command.getAbsolutePath() + " -k start", true, -1,
						"正在开始apache服务", gaeaRuntimeRootDir.getAbsolutePath()))
					return;

			}

			if (installInfo.includeGaea) {
				incProgressMax();

				File dir = new File(installInfo.gaeaDocRootPath,
						installInfo.gaeaProjectName);

				titleView.setText("正在释放Gaea项目");
				msgView.setText(dir.getAbsolutePath());

				if (dir.exists())
					try {
						FileHelp.delDir(dir, iDelDir);
					} catch (Exception e1) {
						e1.printStackTrace();
						MsgHelper.showException(e1);
					}

				try (FileInputStream stream = new FileInputStream(
						CommandRunner.getInstallResourcePath("gaea.dat"))) {
					ZipManager.UnZipFolder(stream, dir.getAbsolutePath(), null);
				} catch (Exception e) {
					e.printStackTrace();
					fireFail(e);
					return;
				}
				incProgressValue();
			}

			for (CommandConfigureInfo command : installInfo.commands) {
				try {
					boolean isRunCommand = command
							.getCommandType() == CommandType.ctRun;
					Object[] args = new Object[command.datas.length()
							+ (isRunCommand ? 2 : 0)];
					int start = 0;
					if (isRunCommand) {
						args[0] = command.checkResult;
						args[1] = command.waitTime;
						start = 2;
					}
					for (int i = start; i < args.length; i++) {
						args[i] = command.datas.get(i - start);
					}

					commandRunner.execute(command.getCommandType(),
							command.commandIndicate, new INotifyCommand() {

								@Override
								public void notify(CommandType commandType,
										Enum<?> indicate, Object[] args) {
									SwingUtilities.invokeLater(new Runnable() {

										@Override
										public void run() {
											switch (commandType) {
												case ctConfigure :
													PageConfigureInfo pageInfo = (PageConfigureInfo) args[0];
													titleView
															.setText("正在处理配置信息【"
																	+ pageInfo.title
																	+ "】");
													msgView.setText(
															pageInfo.saveFile);
													break;
												case ctCopyDir :
													titleView.setText("正在拷贝目录【"
															+ args[1].toString()
															+ "】");
													msgView.setText(
															CommandRunner
																	.getInstallResourcePath(
																			args[0].toString())
																	+ " 至  "
																	+ getInstallDestPath(
																			args[1].toString()));
													break;
												case ctCopyFile :
													titleView.setText("正在拷贝文件【"
															+ args[1].toString()
															+ "】");
													msgView.setText(
															CommandRunner
																	.getInstallResourcePath(
																			args[0].toString())
																	+ " 至  "
																	+ getInstallDestPath(
																			args[1].toString()));
													break;
												case ctDelDir :
													titleView.setText("正在删除目录【"
															+ args[0].toString()
															+ "】");
													msgView.setText("目录："
															+ getInstallDestPath(
																	args[0].toString()));
													break;
												case ctDelFile :
													titleView.setText("正在删除文件【"
															+ args[0].toString()
															+ "】");
													msgView.setText("文件："
															+ getInstallDestPath(
																	args[0].toString()));
													break;
												case ctRun :
													titleView.setText(
															"正在执行指令。。。");
													msgView.setText("指令："
															+ getInstallDestPath(
																	args[0].toString()));
													break;
											}
										}
									});
								}
							}, args);

					incProgressValue();
				} catch (Exception e) {
					e.printStackTrace();
					fireFail(e);
					return;
				}
			}

			fireOk();
		}
	}
	IInstall install;
	InstallConfigureInfo installInfo;
	private JLabel msgView;
	private JProgressBar progressBar;
	private JLabel titleView;

	public InstallingForm(InstallConfigureInfo installInfo, IInstall install) {
		this.install = install;
		this.installInfo = installInfo;

		getContentPane().setBackground(Color.WHITE);
		setBorder(new EmptyBorder(0, 0, 0, 0));
		setBounds(100, 100, 821, 273);
		getContentPane().setLayout(new BorderLayout(0, 0));

		msgView = new JLabel("");
		msgView.setFont(new Font("微软雅黑", Font.PLAIN, 14));
		msgView.setHorizontalAlignment(SwingConstants.CENTER);
		getContentPane().add(msgView, BorderLayout.CENTER);

		JPanel panel = new JPanel();
		panel.setOpaque(false);
		getContentPane().add(panel, BorderLayout.SOUTH);

		progressBar = new JProgressBar();
		progressBar.setStringPainted(true);
		progressBar.setIndeterminate(true);
		getContentPane().add(progressBar, BorderLayout.SOUTH);

		titleView = new JLabel("请等待，安装正在进行中。。。");
		titleView.setHorizontalAlignment(SwingConstants.CENTER);
		titleView.setFont(new Font("微软雅黑", Font.PLAIN, 22));
		getContentPane().add(titleView, BorderLayout.NORTH);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setHorizontalScrollBarPolicy(
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		contentView = new JPanel();
		scrollPane.setViewportView(contentView);
		GridBagLayout gbl_contentView = new GridBagLayout();
		gbl_contentView.columnWeights = new double[]{};
		gbl_contentView.rowWeights = new double[]{};
		contentView.setLayout(gbl_contentView);

	}

	@Override
	public String getId() {
		return pageInfo.id;
	}

	@Override
	public CheckResult check() {
		return new CheckResult(true);
	}

	@Override
	public void save() throws Exception {
	}

	@Override
	public String getFormTitle() {
		return "正在安装，请等待。。。";
	}

	@Override
	public void load(PageConfigureInfo info) {
		super.load(info);
		new ExecuteThread().start();
	}
}
