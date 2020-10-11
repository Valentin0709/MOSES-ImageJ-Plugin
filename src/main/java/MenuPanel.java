import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import ij.IJ;
import ij.ImagePlus;
import ij.plugin.RGBStackMerge;

public class MenuPanel extends JPanel {
	public MainFrame parentFrame;
	List<String> filePaths;

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
				"<html> Extract and visualize motion tracks and meshes from a TIFF image stack<html>");
		instructionLabel1.setHorizontalAlignment(SwingConstants.CENTER);
		instructionLabel1.setFont(new Font("Roboto", Font.PLAIN, 15));
		instructionLabel1.setBounds(0, 92, 500, 24);
		add(instructionLabel1);
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
				List<String> validExtensions = new ArrayList<String>();
				validExtensions.addAll(Arrays.asList(".tif", ".tiff"));

				JFrame dialog = new JFrame();
				Object[] options = { "Ok" };
				int n = JOptionPane.showOptionDialog(dialog,
						"Please select the folder which contains the images you wish to analyse.", "MOSES",
						JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

				if (n == 0) {
					String saveDirectory = IJ.getDirectory("Choose saving directory");

					if (saveDirectory != null) {
						FileSelecter selecter = new FileSelecter();
						selecter.listFiles(saveDirectory, validExtensions, false,
								"Select the images you wish to work with from the list below");
						selecter.setVisible(true);
						selecter.importButton.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								filePaths = selecter.getSelected();
								selecter.dispose();

								if (filePaths.size() > 0) {
									if (filePaths.size() > 1) {
										JFrame dialog = new JFrame();
										Object[] options = { "Ok" };
										JOptionPane.showOptionDialog(dialog,
												"Multiple files were selected. Please input your preffered settings for the first image and MOSES will automatically run the same computation for all the other files.",
												"MOSES", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null,
												options, options[0]);
									}

									computeTracks();
								}
							}
						});
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

		JLabel titleLabel3 = new JLabel("Create selections", SwingConstants.CENTER);
		titleLabel3.setVerticalTextPosition(SwingConstants.CENTER);
		titleLabel3.setHorizontalTextPosition(SwingConstants.CENTER);
		titleLabel3.setFont(new Font("Arial Black", Font.BOLD, 20));
		titleLabel3.setBounds(0, 210, 500, 29);
		add(titleLabel3);

		JButton AnnotationButton = new JButton("Annotation");
		AnnotationButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ArrayList<String> validExtensions = new ArrayList<String>();
				validExtensions.addAll(Arrays.asList(".tif", ".tiff", ".png", ".jpeg"));

				JFrame dialog = new JFrame();

				// display dialog box

				Object[] options = { "Cancel", "Import now" };
				int n = JOptionPane.showOptionDialog(dialog, "Please import an image to continue.", "MOSES",
						JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);

				// display import file window

				if (n == 1) {
					String importedFilePath = Globals.openFile(parentFrame.ui, validExtensions, true);

					if (importedFilePath != null) {
						ImagePlus[] imageList = { Globals.lastImage, Globals.lastImage, Globals.lastImage };
						ImagePlus newImage = RGBStackMerge.mergeChannels(imageList, true);
						newImage.setTitle(Globals.lastImage.getTitle());
						Globals.lastImage.flush();
						newImage.show();
						Globals.lastImage = newImage;

						newAnnotation();
					} else {

						// display error dialog box

						JFrame errorDialog = new JFrame();
						Object[] options2 = { "Ok" };
						JOptionPane.showOptionDialog(errorDialog,
								"Current selected file has an invalid format. Please import an image to continue.",
								"MOSES", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, options2,
								options2[0]);
					}
				}

			}

		});
		AnnotationButton.setHorizontalTextPosition(SwingConstants.CENTER);
		AnnotationButton.setForeground(Color.WHITE);
		AnnotationButton.setFont(new Font("Arial", Font.BOLD, 20));
		AnnotationButton.setBackground(new Color(13, 59, 102));
		AnnotationButton.setBounds(10, 250, 230, 43);

		add(AnnotationButton);

		JButton customVisualisationButton = new JButton("<html>Annotated tracks\r\n visualisation</html>");
		customVisualisationButton.setAlignmentY(Component.TOP_ALIGNMENT);
		customVisualisationButton.setMargin(new Insets(0, 0, 0, 0));
		customVisualisationButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				VisualisationFromMaskParameters.initialise();

				parentFrame.empty();
				parentFrame.visualisationFromMaskPanel1 = new VisualisationFromMaskPanel1(parentFrame);
				parentFrame.getContentPane().add(parentFrame.visualisationFromMaskPanel1);
				parentFrame.validate();
			}
		});
		customVisualisationButton.setHorizontalTextPosition(SwingConstants.CENTER);
		customVisualisationButton.setForeground(Color.WHITE);
		customVisualisationButton.setFont(new Font("Arial", Font.BOLD, 20));
		customVisualisationButton.setBackground(new Color(13, 59, 102));
		customVisualisationButton.setBounds(20, 340, 460, 43);
		add(customVisualisationButton);

		JButton boundaryButton = new JButton("<html>Boundary visualisation</html>");
		boundaryButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				parentFrame.empty();
				parentFrame.boundaryPanel = new BoundaryPanel(parentFrame);
				parentFrame.getContentPane().add(parentFrame.boundaryPanel);
				parentFrame.validate();
			}
		});
		boundaryButton.setMargin(new Insets(0, 0, 0, 0));
		boundaryButton.setHorizontalTextPosition(SwingConstants.CENTER);
		boundaryButton.setForeground(Color.WHITE);
		boundaryButton.setFont(new Font("Arial", Font.BOLD, 20));
		boundaryButton.setBackground(new Color(13, 59, 102));
		boundaryButton.setAlignmentY(0.0f);
		boundaryButton.setBounds(20, 390, 460, 43);
		add(boundaryButton);

		JButton AutoSegmentationButton = new JButton("Auto-segmentation");
		AutoSegmentationButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				List<String> validExtensions = new ArrayList<String>();
				validExtensions.addAll(Arrays.asList(".tif", ".tiff"));

				JFrame dialog = new JFrame();
				Object[] options = { "Ok" };
				int n = JOptionPane.showOptionDialog(dialog,
						"Please select the folder which contains the images you wish to analyse.", "MOSES",
						JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

				if (n == 0) {
					String saveDirectory = IJ.getDirectory("Choose saving directory");

					if (saveDirectory != null) {
						FileSelecter selecter = new FileSelecter();
						selecter.listFiles(saveDirectory, validExtensions, false,
								"Select the images you wish to work with from the list below");
						selecter.setVisible(true);
						selecter.importButton.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								filePaths = selecter.getSelected();
								selecter.dispose();

								if (filePaths.size() > 0) {
									if (filePaths.size() > 1) {
										JFrame dialog = new JFrame();
										Object[] options = { "Ok" };
										JOptionPane.showOptionDialog(dialog,
												"Multiple files were selected. Please input your preffered settings for the first image and MOSES will automatically run the same computation for all the other files.",
												"MOSES", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null,
												options, options[0]);
									}

									BoundingBoxAndMaskGenerationParameters.initialise();
									BoundingBoxAndMaskGenerationParameters.setFiles(filePaths);

									parentFrame.empty();
									parentFrame.autoSegmentationPanel = new AutoSegmentationPanel(parentFrame);
									parentFrame.getContentPane().add(parentFrame.autoSegmentationPanel);
									parentFrame.validate();
								}
							}
						});
					}
				}
			}
		});
		AutoSegmentationButton.setHorizontalTextPosition(SwingConstants.CENTER);
		AutoSegmentationButton.setForeground(Color.WHITE);
		AutoSegmentationButton.setFont(new Font("Arial", Font.BOLD, 20));
		AutoSegmentationButton.setBackground(new Color(13, 59, 102));
		AutoSegmentationButton.setBounds(260, 250, 230, 43);
		add(AutoSegmentationButton);

		JLabel titleLabel4 = new JLabel("Visualizations", SwingConstants.CENTER);
		titleLabel4.setVerticalTextPosition(SwingConstants.CENTER);
		titleLabel4.setHorizontalTextPosition(SwingConstants.CENTER);
		titleLabel4.setFont(new Font("Arial Black", Font.BOLD, 20));
		titleLabel4.setBounds(0, 300, 500, 29);
		add(titleLabel4);

	}

	public void computeTracks() {
		// initialize parameters

		ComputeTracksParameters.initialise();
		ComputeTracksParameters.setFilePath(filePaths);

		// run script

		Thread thread = new Thread() {

			public void run() {

				String temporaryDirectorPath = System.getProperty("java.io.tmpdir");
				String scriptPath = temporaryDirectorPath + "open_file.py";
				File file = new File(scriptPath);

				PythonScript script = new PythonScript("Read file");
				script.addParameter(
						Arrays.asList(new Parameter("filePath", "str", ComputeTracksParameters.getFilePath(0))));
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

		// open image

		IJ.open(ComputeTracksParameters.getFilePath(0));

		// display computeTracksPanel1 and close current panel

		parentFrame.empty();
		parentFrame.computeTracksPanel1 = new ComputeTracksPanel1(parentFrame);
		parentFrame.getContentPane().add(parentFrame.computeTracksPanel1);
		parentFrame.validate();
	}

	public void motionMeasurements() {
		SaliencyMapParameters.initialise();

		parentFrame.empty();
		parentFrame.saliencyMapPanel1 = new SaliencyMapPanel1(parentFrame);
		parentFrame.getContentPane().add(parentFrame.saliencyMapPanel1);
		parentFrame.validate();
	}

	public void newAnnotation() {
		parentFrame.empty();
		parentFrame.newAnnotationPanel = new NewAnnotationPanel(parentFrame);
		parentFrame.getContentPane().add(parentFrame.newAnnotationPanel);
		parentFrame.validate();
	}
}
