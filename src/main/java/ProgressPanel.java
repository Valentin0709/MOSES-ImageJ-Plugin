import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.SystemColor;

import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.LineBorder;

public class ProgressPanel extends JPanel {
	private JLabel fileLabel, messageLabel;
	JLayeredPane parentPanel;
	private String fileName;
	private JProgressBar progressBar;
	public int fileCount, fileNumber;

	public ProgressPanel(JLayeredPane panel, int x, int y) {
		// set look and feel
		try {
			UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}

		this.parentPanel = panel;

		this.setBounds(x, y, 420, 130);
		this.setVisible(false);
		this.setOpaque(false);
		this.setLayout(null);
		this.setBorder(new LineBorder(new Color(0, 0, 0), 2));
		this.setBackground(SystemColor.activeCaption);

		progressBar = new JProgressBar();
		progressBar.setFont(new Font("Roboto", Font.BOLD, 15));
		progressBar.setVisible(false);
		progressBar.setBounds(10, 90, 400, 26);
		this.add(progressBar);

		messageLabel = new JLabel("");
		messageLabel.setVerticalAlignment(SwingConstants.TOP);
		messageLabel.setFont(new Font("Roboto", Font.BOLD, 15));
		messageLabel.setBounds(10, 38, 376, 40);
		this.add(messageLabel);

		fileLabel = new JLabel("");
		fileLabel.setBounds(10, 12, 376, 26);
		fileLabel.setVerticalAlignment(SwingConstants.TOP);
		fileLabel.setFont(new Font("Roboto", Font.BOLD, 15));
		this.add(fileLabel);

		fileCount = fileNumber = 0;
	}

	public void update() {
		parentPanel.revalidate();
		parentPanel.repaint();
	}

	public void setVisibility(boolean visibility) {
		Component[] components = this.getComponents();

		this.setOpaque(visibility);
		this.setVisible(visibility);
		for (Component component : components) {
			component.setVisible(visibility);
		}
		update();
	}

	public void setMessage(String s) {
		messageLabel.setText(s);
		update();
	}

	public void updateFileLabel() {
		if (fileNumber != 0 && fileCount != 0)
			fileLabel.setText("File " + fileNumber + " out of " + fileCount + " (" + fileName + ")");
		update();
	}

	public void setFileName(String s) {
		fileName = s;
		updateFileLabel();
	}

	public void setFileNumber(int n) {
		fileNumber = n;
		updateFileLabel();
	}

	public void setFileCount(int n) {
		fileCount = n;
		updateFileLabel();
	}

	public void setIndeterminate(boolean b) {
		progressBar.setIndeterminate(b);
		update();
	}

	public boolean isIndeterminate() {
		return progressBar.isIndeterminate();
	}

	public void setStringPainted(boolean b) {
		progressBar.setStringPainted(b);
		update();
	}

	public void setString(String s) {
		progressBar.setString(s);
		update();
	}

	public void setMaximum(int m) {
		progressBar.setMaximum(m);
	}

	public int getMaximum() {
		return progressBar.getMaximum();
	}

	public void setValue(int v) {
		progressBar.setValue(v);
		update();
	}

}
