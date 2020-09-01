import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import ij.IJ;

public class MenuPanel extends JPanel {
	public MainFrame parentFrame;

	JCheckBox batchModeCheckbox;

	public MenuPanel(MainFrame parentFrame) {

		// set look and feel

		try {
			UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}

		// set parent frame

		this.parentFrame = parentFrame;

		// set size

		this.setPreferredSize(new Dimension(Globals.frameWidth, Globals.frameHight));

		// set background color

		this.setBackground(new Color(252, 252, 252));
		setLayout(null);

		// title label

		JLabel titleLabel = new JLabel("Motion Sensing Superpixels", JLabel.CENTER);
		titleLabel.setBounds(0, 0, 500, 36);
		titleLabel.setFont(new Font("Arial Black", Font.BOLD, 25));
		titleLabel.setVerticalTextPosition(JLabel.CENTER);
		titleLabel.setHorizontalTextPosition(JLabel.CENTER);

		JLabel instructionLabel1 = new JLabel(
				"<html> Extract and visualize superpixel motion tracks (input TIFF image stack) <html>");
		instructionLabel1.setHorizontalAlignment(SwingConstants.CENTER);
		instructionLabel1.setFont(new Font("Roboto", Font.PLAIN, 15));
		instructionLabel1.setBounds(20, 70, 460, 43);
		add(instructionLabel1);

		batchModeCheckbox = new JCheckBox("Batch mode");
		batchModeCheckbox.setFont(new Font("Roboto", Font.PLAIN, 15));
		batchModeCheckbox.setBounds(195, 382, 111, 24);
		batchModeCheckbox.setBackground(new Color(252, 252, 252));
		add(batchModeCheckbox);
		add(titleLabel);

		// compute superpixel tracks button

		JButton superpixelTracksButton = new JButton("<html> Compute superpixel motion tracks and mesh </html> ");
		superpixelTracksButton.setHorizontalTextPosition(SwingConstants.CENTER);
		superpixelTracksButton.setBounds(20, 120, 460, 43);
		superpixelTracksButton.setFont(new Font("Arial", Font.BOLD, 20));
		superpixelTracksButton.setForeground(Color.WHITE);
		superpixelTracksButton.setBackground(new Color(13, 59, 102));
		superpixelTracksButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ArrayList<String> validExtensions = new ArrayList<String>();
				validExtensions.addAll(Arrays.asList(".tif", ".tiff"));

				JFrame dialog = new JFrame();

				// checks if batch mode is enabled
				if (batchModeCheckbox.isSelected()) {
					// display dialog box

					Object[] options = { "Cancel", "Import now" };
					int n = JOptionPane.showOptionDialog(dialog,
							"Batch mode is enabled. Please select a file from the folder you want to analyse and input your preffered settings. MOSES will automatically import and compute the motion tracks for all the other files that have a valid format using the same settings.",
							"MOSES", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);

					// display import file window

					if (n == 1) {
						String importedFilePath = Globals.openFile(parentFrame.ui, validExtensions, true);

						if (importedFilePath != null)
							nextStep(importedFilePath);
						else {
							// display error dialog box

							JFrame errorDialog = new JFrame();
							Object[] options2 = { "Ok" };
							JOptionPane.showOptionDialog(dialog,
									"Current selected image has an invalid file format. Please import a TIFF stack to continue.",
									"MOSES", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, options2,
									options2[0]);
						}
					}

				} else {
					if (parentFrame.imageDisplayService.getActiveDataset() == null) { // checks if any files are opened

						// display dialog box

						Object[] options = { "Cancel", "Import now" };
						int n = JOptionPane.showOptionDialog(dialog,
								"No file selected. Please import a TIFF stack to continue.", "MOSES",
								JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);

						// display import file window

						if (n == 1) {
							String importedFilePath = Globals.openFile(parentFrame.ui, validExtensions, true);

							if (importedFilePath != null)
								nextStep(importedFilePath);
							else {

								// display error dialog box

								JFrame errorDialog = new JFrame();
								Object[] options2 = { "Ok" };
								JOptionPane.showOptionDialog(dialog,
										"Current selected image has an invalid file format. Please import a TIFF stack to continue.",
										"MOSES", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, options2,
										options2[0]);
							}
						}
					} else {

						String openFilePath = parentFrame.imageDisplayService.getActiveDataset().getSource();
						// String extension = Globals.getExtension(filePath);

						if (Globals.checkExtension(openFilePath, validExtensions))
							nextStep(openFilePath);
						else {
							// display dialog box

							Object[] options = { "Cancel", "Import now" };
							int n = JOptionPane.showOptionDialog(dialog,
									"Current selected image has an invalid file format. Please import a TIFF stack to continue.",
									"MOSES", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, options,
									options[0]);

							// display import file window

							if (n == 1) {
								String importedFilePath = Globals.openFile(parentFrame.ui, validExtensions, true);

								if (importedFilePath != null)
									nextStep(importedFilePath);
								else {

									// display error dialog box

									JFrame errorDialog = new JFrame();
									Object[] options2 = { "Ok" };
									JOptionPane.showOptionDialog(dialog,
											"Current selected image has an invalid file format. Please import a TIFF stack to continue.",
											"MOSES", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null,
											options2, options2[0]);
								}
							}
						}
					}
				}
			}
		});
		add(superpixelTracksButton);

		/*
		 * JLabel instructionLabel1_1 = new JLabel(
		 * "<html> Extract motion measurements from superpixel motion tracks (input MATLAB file) </html>"
		 * ); instructionLabel1_1.setHorizontalAlignment(SwingConstants.CENTER);
		 * instructionLabel1_1.setFont(new Font("Roboto", Font.PLAIN, 15));
		 * instructionLabel1_1.setBounds(20, 180, 460, 43); add(instructionLabel1_1);
		 * 
		 * 
		 * JButton motionMeasurementsButton = new
		 * JButton("<html> Compute MOSES motion measurements </html>");
		 * motionMeasurementsButton.addActionListener(new ActionListener() { public void
		 * actionPerformed(ActionEvent e) { parentFrame.empty();
		 * parentFrame.motionMeasurements1 = new MotionMeasurements1(parentFrame);
		 * parentFrame.getContentPane().add(parentFrame.motionMeasurements1);
		 * parentFrame.validate(); } });
		 * motionMeasurementsButton.setHorizontalTextPosition(SwingConstants.CENTER);
		 * motionMeasurementsButton.setForeground(Color.WHITE);
		 * motionMeasurementsButton.setFont(new Font("Arial", Font.BOLD, 20));
		 * motionMeasurementsButton.setBackground(new Color(13, 59, 102));
		 * motionMeasurementsButton.setBounds(20, 230, 460, 43);
		 * add(motionMeasurementsButton);
		 */
	}

	public void nextStep(String filePath) {
		// initialize parameters

		ComputeTracksParameters.initialise();
		ComputeTracksParameters.setBatchMode(batchModeCheckbox.isSelected());
		ComputeTracksParameters.setFilePath(filePath);

		// run script

		Thread thread = new Thread() {

			public void run() {

				// create temporary python script file

				String temporaryDirectorPath = System.getProperty("java.io.tmpdir");
				String scriptPath = temporaryDirectorPath + "open_file.py";
				File file = new File(scriptPath);

				try {
					FileWriter writer = new FileWriter(file);
					writer.write("import sys\r\n" + "from MOSES.Utility_Functions.file_io import read_multiimg_PIL\r\n"
							+ "\r\n" + "infile = sys.argv[1]\r\n" + "vidstack = read_multiimg_PIL(infile)\r\n"
							+ "n_frame, n_rows, n_cols, n_channels = vidstack.shape\r\n" + "\r\n" + "print(n_frame)\r\n"
							+ "print(n_rows)\r\n" + "print(n_cols)\r\n" + "print(n_channels)\r\n");
					writer.close();
				} catch (IOException e2) {
					IJ.handleException(e2);
				}

				ProcessBuilder pb = new ProcessBuilder("python", scriptPath, ComputeTracksParameters.getFilePath());
				try {
					Process p = pb.start();
					BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));

					// number of frames
					ComputeTracksParameters.setFrames(new Integer(in.readLine()).intValue());

					// image height
					ComputeTracksParameters.setHeight(new Integer(in.readLine()).intValue());

					// image width
					ComputeTracksParameters.setWidth(new Integer(in.readLine()).intValue());

					// number of channels
					ComputeTracksParameters.setChannels(new Integer(in.readLine()).intValue());
				} catch (IOException e1) {
					IJ.handleException(e1);
				}

				file.delete();
			}
		};

		thread.start();

		try {
			thread.join();
		} catch (InterruptedException e1) {
			IJ.handleException(e1);
		}

		// display computeTracksPanel1 and close current panel

		parentFrame.empty();
		parentFrame.computeTracksPanel1 = new ComputeTracksPanel1(parentFrame);
		parentFrame.getContentPane().add(parentFrame.computeTracksPanel1);
		parentFrame.validate();
	}
}
