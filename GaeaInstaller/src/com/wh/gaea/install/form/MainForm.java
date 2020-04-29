package com.wh.gaea.install.form;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.SystemColor;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Locale;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDesktopPane;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.MatteBorder;
import javax.swing.plaf.FontUIResource;

import org.json.JSONObject;

import com.alee.laf.WebLookAndFeel;
import com.wh.gaea.install.control.PageManager;
import com.wh.gaea.install.control.PageManager.UIInfo;
import com.wh.gaea.install.interfaces.InstallConfigureInfo;
import com.wh.gaea.install.interfaces.PageConfigureInfo;
import com.wh.swing.tools.MsgHelper;
import com.wh.swing.tools.SwingTools;
import com.wh.swing.tools.dialog.AbstractFileSelector;
import com.wh.swing.tools.dialog.AbstractFileSelector.INewFileSelector;
import com.wh.tools.FileHelp;
import com.wh.tools.ImageUtils;
import com.wh.tools.JsonHelp;

public class MainForm {

	private JDialog mainFrame;
	private JDesktopPane desktopPane;

	PageManager pageManager = new PageManager();
	InstallConfigureInfo installInfo = new InstallConfigureInfo();

	private JButton prevButton;
	private JButton nextButton;
	private JButton endButton;
	private JLabel titleView;
	private JLabel iconView;
	private JPanel buttonsView;

	protected static void createMainForm(String configFileName)
			throws Exception {
		MainForm window = new MainForm(configFileName);

		JDialog frame = window.mainFrame;
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				if (MsgHelper.showConfirmDialog("是否退出安装程序？",
						JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
					return;

				System.exit(0);
			}
		});

		frame.setVisible(true);
	}

	public static void main(String[] args) {
		try {
			// JFrame.setDefaultLookAndFeelDecorated(true);
			// JDialog.setDefaultLookAndFeelDecorated(true);

			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					try {

						SwingTools.setup(new INewFileSelector() {

							@Override
							public AbstractFileSelector createFileSelector() {
								return new AbstractFileSelector() {
									private static final long serialVersionUID = 1L;

									@Override
									public File getDefaultPath() {
										return FileHelp.getRootPath();
									}

								};
							}
						}, FileHelp.getRootPath());

						WebLookAndFeel.globalControlFont = new FontUIResource(
								"微软雅黑", 0, 12);
						WebLookAndFeel.globalMenuFont = new FontUIResource(
								"微软雅黑", 0, 12);
						WebLookAndFeel.globalAcceleratorFont = new FontUIResource(
								"微软雅黑", 0, 12);
						WebLookAndFeel.globalAlertFont = new FontUIResource(
								"微软雅黑", 0, 12);
						WebLookAndFeel.globalTextFont = new FontUIResource(
								"微软雅黑", 0, 12);
						WebLookAndFeel.globalTitleFont = new FontUIResource(
								"微软雅黑", 0, 13);
						WebLookAndFeel.globalTooltipFont = new FontUIResource(
								"微软雅黑", 0, 12);
						WebLookAndFeel.buttonFont = new FontUIResource("微软雅黑",
								0, 12);

						// WebTableStyle.rowHeight = 48;

						WebLookAndFeel.install();

						Locale.setDefault(Locale.SIMPLIFIED_CHINESE);

						createMainForm(args[0]);

					} catch (Exception e) {
						MsgHelper.showException(e);
					}

				}
			});
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	private void configWindow() {
		Toolkit tk = java.awt.Toolkit.getDefaultToolkit();
		Dimension screenSize = tk.getScreenSize();
		int width = (int) ((float) screenSize.width / 7 * 5);
		int height = (int) ((float) screenSize.height / 7 * 5);
		mainFrame.setSize(width, height);
		mainFrame.setLocationRelativeTo(null);
	}

	protected void setButtonsEnabled() {
		prevButton.setEnabled(!pageManager.isFirst() && !(pageManager.current().ui instanceof InstallingForm));
		nextButton.setEnabled(!pageManager.isLast() && !(pageManager.current().ui instanceof InstallingForm));
		endButton.setEnabled(pageManager.isLast());
	}

	protected void setSetupEnd(boolean isok) {
		for (int i = 0; i < buttonsView.getComponentCount(); i++) {
			buttonsView.getComponent(i).setVisible(false);
		}

		endButton.setVisible(true);

		buttonsView.updateUI();

		try {
			if (isok)
				PageManager.setUI(titleView, desktopPane, new UIInfo(
						InstallOkForm.class));
			else
				PageManager.setUI(titleView, desktopPane, new UIInfo(
						InstallFailForm.class));
		} catch (Exception e) {
		}
	}

	public MainForm(String configFileName) throws Exception {
		mainFrame = new JDialog();
		mainFrame.setResizable(false);
		mainFrame.setSize(923, 673);
		mainFrame.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		mainFrame.setIconImage(Toolkit.getDefaultToolkit()
				.getImage(MainForm.class.getResource("/image/browser.png")));

		JPanel panel = new JPanel();
		panel.setBackground(Color.WHITE);
		panel.setBorder(
				new MatteBorder(0, 0, 1, 0, (Color) new Color(0, 0, 0)));
		mainFrame.getContentPane().add(panel, BorderLayout.NORTH);
		panel.setLayout(new BorderLayout(0, 0));

		iconView = new JLabel("");
		iconView.setPreferredSize(new Dimension(128, 128));
		iconView.setMinimumSize(new Dimension(128, 128));
		iconView.setHorizontalAlignment(SwingConstants.CENTER);
		iconView.setMaximumSize(new Dimension(128, 128));
		iconView.setIcon(
				new ImageIcon(MainForm.class.getResource("/image/left.png")));
		panel.add(iconView, BorderLayout.WEST);

		titleView = new JLabel("New label");
		titleView.setFont(new Font("微软雅黑", Font.PLAIN, 48));
		titleView.setHorizontalAlignment(SwingConstants.CENTER);
		panel.add(titleView, BorderLayout.CENTER);

		buttonsView = new JPanel();
		buttonsView.setBorder(
				new MatteBorder(1, 0, 0, 0, (Color) SystemColor.activeCaption));
		mainFrame.getContentPane().add(buttonsView, BorderLayout.SOUTH);

		prevButton = new JButton("上一步");
		prevButton.setEnabled(false);
		prevButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					PageManager.setUI(titleView, desktopPane,
							pageManager.prev());
					setButtonsEnabled();
				} catch (Exception e1) {
					e1.printStackTrace();
					MsgHelper.showException(e1);
				}
			}
		});
		prevButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		buttonsView.add(prevButton);

		nextButton = new JButton("下一步");
		nextButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					pageManager.save();
					PageManager.setUI(titleView, desktopPane,
							pageManager.next());
					setButtonsEnabled();
				} catch (Exception e1) {
					e1.printStackTrace();
					MsgHelper.showException(e1);
				}
			}
		});
		nextButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		buttonsView.add(nextButton);

		JButton btnNewButton_2 = new JButton("取消");
		btnNewButton_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (MsgHelper.showConfirmDialog("是否退出安装程序？",
						JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
					return;

				System.exit(0);
			}
		});

		endButton = new JButton("完成");
		endButton.setVisible(false);
		endButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		endButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		buttonsView.add(endButton);
		btnNewButton_2.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		buttonsView.add(btnNewButton_2);

		desktopPane = new JDesktopPane();
		mainFrame.getContentPane().add(desktopPane, BorderLayout.CENTER);

		configWindow();

		installInfo.fromJson((JSONObject) JsonHelp
				.parseJson(new File(FileHelp.getRootPath(), configFileName), null));

		if (installInfo.iconName != null && !installInfo.iconName.isEmpty()) {
			File iconFile = new File(FileHelp.getRootPath(), "icon.png");
			if (iconFile.exists()) {
				BufferedImage image = ImageUtils.loadImage(iconFile);
				iconView.setIcon(new ImageIcon(image));

				iconView.updateUI();
			}
		}

		pageManager.add(new UIInfo(SelectInstallDirForm.class, installInfo));

		for (PageConfigureInfo pageInfo : installInfo) {
			UIInfo uiInfo = new UIInfo();
			uiInfo.createParams = pageInfo;
			uiInfo.uiClass = ConfigureForm.class;
			pageManager.add(uiInfo);
		}

		pageManager.add(new UIInfo(PrepareSetupForm.class));
		pageManager.add(new UIInfo(InstallingForm.class,
				new Object[]{installInfo,
						new InstallingForm.IInstall() {

							@Override
							public void onSetupFail(Throwable e) {
								MsgHelper.showException(e);
								setSetupEnd(false);
							}

							@Override
							public void onSetupEnd() {
								setSetupEnd(true);
							}
						}}));

		PageManager.setUI(titleView, desktopPane, pageManager.next());
	}
}
