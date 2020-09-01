import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class MotionMeasurements1 extends JPanel {
	public MainFrame parentFrame;
	MotionMeasurements1 self = this;

	JLabel importedFilesLabel;

	public MotionMeasurements1(MainFrame parentFrame) {

		// set look and feel

		try {
			UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}

		this.parentFrame = parentFrame;

		// set size

		this.setPreferredSize(new Dimension(Globals.frameWidth, Globals.frameHight));

		// set background color

		this.setBackground(new Color(252, 252, 252));

		// set layout

		setLayout(null);

		JLabel titleLabel = new JLabel("Extract motion measurements", SwingConstants.CENTER);
		titleLabel.setVerticalTextPosition(SwingConstants.CENTER);
		titleLabel.setHorizontalTextPosition(SwingConstants.CENTER);
		titleLabel.setFont(new Font("Arial Black", Font.BOLD, 23));
		titleLabel.setBounds(0, 0, 500, 36);
		add(titleLabel);

		JLabel importMotionTracksLabel = new JLabel("<html>Import  superpixel motion tracks (.mat file)</html>");
		importMotionTracksLabel.setVerticalAlignment(SwingConstants.TOP);
		importMotionTracksLabel.setHorizontalAlignment(SwingConstants.LEFT);
		importMotionTracksLabel.setForeground(Color.DARK_GRAY);
		importMotionTracksLabel.setFont(new Font("Roboto", Font.PLAIN, 15));
		importMotionTracksLabel.setBounds(10, 45, 300, 20);
		add(importMotionTracksLabel);

		JButton importMotionTracksButton = new JButton("Import");
		importMotionTracksButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ArrayList<String> validExtensions = new ArrayList<String>();
				validExtensions.addAll(Arrays.asList(".mat"));

				String importedFilePath = Globals.openFile(parentFrame.ui, validExtensions, false);

				if (importedFilePath != null) {
					import_motion_tracks(importedFilePath);
				} else {
					// display error dialog box
					JFrame dialog = new JFrame();
					JFrame errorDialog = new JFrame();
					Object[] options2 = { "Ok" };
					JOptionPane.showOptionDialog(dialog,
							"The selected file has an invalid file format. Please import a MATLAB file to continue.",
							"MOSES", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, options2,
							options2[0]);
				}
			}
		});
		importMotionTracksButton.setVerticalTextPosition(SwingConstants.CENTER);
		importMotionTracksButton.setHorizontalTextPosition(SwingConstants.CENTER);
		importMotionTracksButton.setForeground(Color.WHITE);
		importMotionTracksButton.setFont(new Font("Arial", Font.BOLD, 15));
		importMotionTracksButton.setBackground(new Color(13, 59, 102));
		importMotionTracksButton.setBounds(320, 45, 150, 18);
		add(importMotionTracksButton);

		importedFilesLabel = new JLabel("imported files list");
		importedFilesLabel.setVisible(false);
		importedFilesLabel.setVerticalAlignment(SwingConstants.TOP);
		importedFilesLabel.setHorizontalAlignment(SwingConstants.LEFT);
		importedFilesLabel.setForeground(Color.DARK_GRAY);
		importedFilesLabel.setFont(new Font("Roboto", Font.PLAIN, 13));
		importedFilesLabel.setBounds(10, 65, 460, 40);
		add(importedFilesLabel);

		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				parentFrame.empty();
				parentFrame.menuPanel = new MenuPanel(parentFrame);
				parentFrame.getContentPane().add(parentFrame.menuPanel);
				parentFrame.validate();
			}
		});
		cancelButton.setVerticalTextPosition(SwingConstants.CENTER);
		cancelButton.setHorizontalTextPosition(SwingConstants.CENTER);
		cancelButton.setForeground(Color.WHITE);
		cancelButton.setFont(new Font("Arial", Font.BOLD, 15));
		cancelButton.setBackground(new Color(13, 59, 102));
		cancelButton.setBounds(10, 430, 140, 30);
		add(cancelButton);

		JButton finishButton = new JButton("Finish");
		finishButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		finishButton.setVerticalTextPosition(SwingConstants.CENTER);
		finishButton.setHorizontalTextPosition(SwingConstants.CENTER);
		finishButton.setForeground(Color.WHITE);
		finishButton.setFont(new Font("Arial", Font.BOLD, 15));
		finishButton.setBackground(new Color(13, 59, 102));
		finishButton.setBounds(350, 430, 140, 30);
		add(finishButton);
	}

	public void import_motion_tracks(String s) {
		MotionMeasurementsParameters.setMotionTracksFilePath(s);
		List<String> files = Globals.getMatlabFiles(s);

		SelectMatlabFilesWindow selectPanel = new SelectMatlabFilesWindow(
				"<html>Select the superpixel motion tracks you want to use for extracting motion measurements</html>",
				files);
		JFrame frame = new JFrame();
		frame.getContentPane().add(selectPanel);
		frame.pack();
		frame.setResizable(false);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		selectPanel.importButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String importedFiles = "Selected tracks: ";
				boolean ok = false;

				for (JCheckBox checkbox : selectPanel.checkBoxList)
					if (checkbox.isSelected()) {
						ok = true;
						importedFiles += checkbox.getText() + "  ";
						MotionMeasurementsParameters.addMotionTrack(checkbox.getText());
					}

				if (ok) {
					importedFilesLabel.setVisible(true);
					importedFilesLabel.setText("<html>" + importedFiles + "</html>");
				}

				frame.dispose();
			}
		});
	}
}
