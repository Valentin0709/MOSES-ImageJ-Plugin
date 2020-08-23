import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;

import ij.IJ;

public class InstallWindow extends JFrame {
	MainFrame parentFrame;
	InstallWindow self = this;

	public InstallWindow(MainFrame mainFrame) {
		// set title

		super("MOSES Installer");
		getContentPane().setBackground(Color.WHITE);
		getContentPane().setLayout(null);

		parentFrame = mainFrame;

		// components

		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				self.dispose();
			}
		});
		cancelButton.setVerticalTextPosition(SwingConstants.CENTER);
		cancelButton.setHorizontalTextPosition(SwingConstants.CENTER);
		cancelButton.setForeground(Color.WHITE);
		cancelButton.setFont(new Font("Arial", Font.BOLD, 15));
		cancelButton.setBackground(new Color(13, 59, 102));
		cancelButton.setBounds(100, 180, 140, 30);
		getContentPane().add(cancelButton);

		JProgressBar progressBar = new JProgressBar();
		progressBar.setVisible(false);
		progressBar.setBounds(25, 12, 450, 30);
		getContentPane().add(progressBar);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBorder(null);
		scrollPane.setViewportBorder(null);
		scrollPane.setBackground(Color.WHITE);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setBounds(25, 50, 450, 120);
		getContentPane().add(scrollPane);

		JTextPane messageTextbox = new JTextPane();
		messageTextbox.setPreferredSize(new Dimension(400, 110));
		messageTextbox.setEditable(false);
		messageTextbox.setBorder(null);
		messageTextbox.setMaximumSize(new Dimension(400, 2147483647));
		messageTextbox.setText("It seems like this is the first time you are using MOSES.");
		messageTextbox.setFont(new Font("Roboto", Font.PLAIN, 15));
		scrollPane.setViewportView(messageTextbox);

		JButton installNowButton = new JButton("Install now");
		installNowButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				messageTextbox.setText("");
				progressBar.setVisible(true);
				progressBar.setIndeterminate(true);

				class MyWorker extends SwingWorker<String, String> {
					String text = "";

					@Override
					protected String doInBackground() {
						String currentPath = getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
						currentPath = currentPath.substring(1, currentPath.lastIndexOf("/"));
						currentPath = currentPath.replaceAll("%20", " ");
						// IJ.log(currentPath + "/MOSES");

						ArrayList<String> command = new ArrayList<>();
						command.addAll(Arrays.asList("pip", "install", "-e", currentPath + "/MOSES"));

						ProcessBuilder pb = new ProcessBuilder(command);

						try {
							Process p = pb.start();

							BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
							String line;
							while ((line = in.readLine()) != null) {
								if (line.isEmpty()) {
									break;
								}
								publish(line);
								Thread.yield();
								// IJ.log(line);
							}

							p.waitFor();
						} catch (IOException | InterruptedException e1) {
							IJ.handleException(e1);
						}

						return "Done.";
					}

					@Override
					protected void process(List<String> messages) {
						for (String m : messages) {
							text += m + "\r\n";
							messageTextbox.setText(text);
							self.validate();
							self.repaint();
						}

					}

					@Override
					protected void done() {
						progressBar.setVisible(false);

						self.dispose();

						if (!self.parentFrame.isVisible())
							self.parentFrame.display();
					}
				}
				new MyWorker().execute();
			}
		});
		installNowButton.setVerticalTextPosition(SwingConstants.CENTER);
		installNowButton.setHorizontalTextPosition(SwingConstants.CENTER);
		installNowButton.setForeground(Color.WHITE);
		installNowButton.setFont(new Font("Arial", Font.BOLD, 15));
		installNowButton.setBackground(new Color(13, 59, 102));
		installNowButton.setBounds(260, 180, 140, 30);
		getContentPane().add(installNowButton);

		// set size

		this.setPreferredSize(new Dimension(500, 250));

		// set location to top-right corner

		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice defaultScreen = ge.getDefaultScreenDevice();
		java.awt.Rectangle rect = defaultScreen.getDefaultConfiguration().getBounds();
		this.setLocation((int) rect.getMaxX() - Globals.frameWidth, 0);

		// set frame properties

		this.setResizable(false);
		this.setVisible(false);
		this.setLocationRelativeTo(null);

		pack();

	}

	public void display() {
		this.setVisible(true);
	}

	public void empty() {
		this.getContentPane().removeAll();
		this.getContentPane().repaint();
	}
}
