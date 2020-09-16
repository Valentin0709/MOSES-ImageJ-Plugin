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
				"<html> Extract and visualize superpixel motion tracks from a TIFF image stack<html>");
		instructionLabel1.setHorizontalAlignment(SwingConstants.CENTER);
		instructionLabel1.setFont(new Font("Roboto", Font.PLAIN, 15));
		instructionLabel1.setBounds(0, 92, 500, 24);
		add(instructionLabel1);

		batchModeCheckbox = new JCheckBox("Batch mode");
		batchModeCheckbox.setFont(new Font("Roboto", Font.PLAIN, 15));
		batchModeCheckbox.setBounds(196, 436, 111, 24);
		batchModeCheckbox.setBackground(new Color(252, 252, 252));
		add(batchModeCheckbox);
		add(titleLabel);

		// compute superpixel tracks button

		JButton superpixelTracksButton = new JButton("<html> Compute superpixel motion tracks and mesh </html> ");
		superpixelTracksButton.setHorizontalTextPosition(SwingConstants.CENTER);
		superpixelTracksButton.setBounds(20, 50, 460, 43);
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
							computeTracks(importedFilePath);
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
								computeTracks(importedFilePath);
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
							computeTracks(openFilePath);
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
									computeTracks(importedFilePath);
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

		JButton motionSaliencyMapButton = new JButton("Motion saliency map");
		motionSaliencyMapButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				motionMeasurements();
			}
		});
		motionSaliencyMapButton.setHorizontalTextPosition(SwingConstants.CENTER);
		motionSaliencyMapButton.setForeground(Color.WHITE);
		motionSaliencyMapButton.setFont(new Font("Arial", Font.BOLD, 20));
		motionSaliencyMapButton.setBackground(new Color(13, 59, 102));
		motionSaliencyMapButton.setBounds(10, 160, 230, 43);
		add(motionSaliencyMapButton);

		JLabel titleLabel2 = new JLabel("Extract motion measurements", SwingConstants.CENTER);
		titleLabel2.setVerticalTextPosition(SwingConstants.CENTER);
		titleLabel2.setHorizontalTextPosition(SwingConstants.CENTER);
		titleLabel2.setFont(new Font("Arial Black", Font.BOLD, 20));
		titleLabel2.setBounds(0, 125, 500, 29);
		add(titleLabel2);

		JButton meshStrainCurveButton = new JButton("Mesh metrics");
		meshStrainCurveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MeshMetricsParameters.initialise();
				MeshMetricsParameters.setBatchMode(batchModeCheckbox.isSelected());

				parentFrame.empty();
				parentFrame.meshMetricsPanel = new MeshMetricsPanel(parentFrame);
				parentFrame.getContentPane().add(parentFrame.meshMetricsPanel);
				parentFrame.validate();
			}
		});
		meshStrainCurveButton.setHorizontalTextPosition(SwingConstants.CENTER);
		meshStrainCurveButton.setForeground(Color.WHITE);
		meshStrainCurveButton.setFont(new Font("Arial", Font.BOLD, 20));
		meshStrainCurveButton.setBackground(new Color(13, 59, 102));
		meshStrainCurveButton.setBounds(260, 158, 230, 43);
		add(meshStrainCurveButton);

		JLabel titleLabel3 = new JLabel("Annotations", SwingConstants.CENTER);
		titleLabel3.setVerticalTextPosition(SwingConstants.CENTER);
		titleLabel3.setHorizontalTextPosition(SwingConstants.CENTER);
		titleLabel3.setFont(new Font("Arial Black", Font.BOLD, 20));
		titleLabel3.setBounds(-12, 210, 500, 29);
		add(titleLabel3);

		JButton CreateAnnotationButton = new JButton("New mask");
		CreateAnnotationButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ArrayList<String> validExtensions = new ArrayList<String>();
				validExtensions.addAll(Arrays.asList(".tif", ".tiff", ".png", ".jpeg"));

				JFrame dialog = new JFrame();

				if (parentFrame.imageDisplayService.getActiveDataset() == null) { // checks if any files are opened

					// display dialog box

					Object[] options = { "Cancel", "Import now" };
					int n = JOptionPane.showOptionDialog(dialog,
							"No file selected. Please import an image to continue.", "MOSES", JOptionPane.YES_NO_OPTION,
							JOptionPane.WARNING_MESSAGE, null, options, options[0]);

					// display import file window

					if (n == 1) {
						String importedFilePath = Globals.openFile(parentFrame.ui, validExtensions, true);

						if (importedFilePath != null)
							newAnnotation();
						else {

							// display error dialog box

							JFrame errorDialog = new JFrame();
							Object[] options2 = { "Ok" };
							JOptionPane.showOptionDialog(dialog,
									"Current selected file has an invalid format. Please import an image to continue.",
									"MOSES", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, options2,
									options2[0]);
						}
					}
				} else {

					String openFilePath = parentFrame.imageDisplayService.getActiveDataset().getSource();

					if (Globals.checkExtension(openFilePath, validExtensions))
						newAnnotation();
					else {
						// display dialog box

						Object[] options = { "Cancel", "Import now" };
						int n = JOptionPane.showOptionDialog(dialog,
								"Current selected file has an invalid format. Please import an image to continue.",
								"MOSES", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, options,
								options[0]);

						// display import file window

						if (n == 1) {
							String importedFilePath = Globals.openFile(parentFrame.ui, validExtensions, true);

							if (importedFilePath != null)
								newAnnotation();
							else {

								// display error dialog box

								Object[] options2 = { "Ok" };
								JOptionPane.showOptionDialog(dialog,
										"Current selected file has an invalid format. Please import an image to continue.",
										"MOSES", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, options2,
										options2[0]);
							}
						}
					}
				}
			}

		});
		CreateAnnotationButton.setHorizontalTextPosition(SwingConstants.CENTER);
		CreateAnnotationButton.setForeground(Color.WHITE);
		CreateAnnotationButton.setFont(new Font("Arial", Font.BOLD, 20));
		CreateAnnotationButton.setBackground(new Color(13, 59, 102));
		CreateAnnotationButton.setBounds(10, 245, 230, 43);
		add(CreateAnnotationButton);

		JButton btnCreateVisualisationFrom = new JButton("<html>Visualisation from mask</html>");
		btnCreateVisualisationFrom.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				VisualisationFromMaskParameters.initialise();
				VisualisationFromMaskParameters.setBatchMode(batchModeCheckbox.isSelected());

				parentFrame.empty();
				parentFrame.visualisationFromMaskPanel1 = new VisualisationFromMaskPanel1(parentFrame);
				parentFrame.getContentPane().add(parentFrame.visualisationFromMaskPanel1);
				parentFrame.validate();
			}
		});
		btnCreateVisualisationFrom.setHorizontalTextPosition(SwingConstants.CENTER);
		btnCreateVisualisationFrom.setForeground(Color.WHITE);
		btnCreateVisualisationFrom.setFont(new Font("Arial", Font.BOLD, 17));
		btnCreateVisualisationFrom.setBackground(new Color(13, 59, 102));
		btnCreateVisualisationFrom.setBounds(260, 245, 230, 43);
		add(btnCreateVisualisationFrom);

	}

	public void computeTracks(String filePath) {
		// initialize parameters

		ComputeTracksParameters.initialise();
		ComputeTracksParameters.setBatchMode(batchModeCheckbox.isSelected());
		ComputeTracksParameters.setFilePath(filePath);

		// run script

		Thread thread = new Thread() {

			public void run() {

				String temporaryDirectorPath = System.getProperty("java.io.tmpdir");
				String scriptPath = temporaryDirectorPath + "open_file.py";
				File file = new File(scriptPath);

				PythonScript script = new PythonScript("Read file");
				script.addParameter(
						Arrays.asList(new Parameter("filePath", "str", ComputeTracksParameters.getFilePath())));
				script.importModule("sys");
				script.importModuleFrom("read_multiimg_PIL", "MOSES.Utility_Functions.file_io");
				script.addScript(script.createParameterDictionary());
				script.addScript(PythonScript.setValue("vidstack",
						PythonScript.callFunction("read_multiimg_PIL", "parameters.get('filePath')")));
				script.startIf("len(vidstack.shape) == 3");
				script.addScript(PythonScript.setValue("vidstack", "vidstack[..., None]"));
				script.stopIf();
				script.addScript(PythonScript.setValue(Arrays.asList("n_frame", "n_rows", "n_cols", "n_channels"),
						"vidstack.shape"));

				script.addScript(PythonScript.print("n_frame"));
				script.addScript(PythonScript.print("n_rows"));
				script.addScript(PythonScript.print("n_cols"));
				script.addScript(PythonScript.print("n_channels"));

				try {
					FileWriter writer = new FileWriter(file);
					writer.write(script.getScript());
					writer.close();
				} catch (IOException e) {
					IJ.handleException(e);
				}

				ArrayList<String> command = new ArrayList<>();
				command.add("python");
				command.add(scriptPath);
				for (Parameter parameter : script.getParameters())
					command.add(parameter.getValue());

				ProcessBuilder pb = new ProcessBuilder(command);

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

	public void motionMeasurements() {
		SaliencyMapParameters.initialise();
		SaliencyMapParameters.setBatchMode(batchModeCheckbox.isSelected());

		parentFrame.empty();
		parentFrame.saliencyMapPanel = new SaliencyMapPanel(parentFrame);
		parentFrame.getContentPane().add(parentFrame.saliencyMapPanel);
		parentFrame.validate();
	}

	public void newAnnotation() {
		parentFrame.empty();
		parentFrame.newAnnotationPanel = new NewAnnotationPanel(parentFrame);
		parentFrame.getContentPane().add(parentFrame.newAnnotationPanel);
		parentFrame.validate();
	}
}
