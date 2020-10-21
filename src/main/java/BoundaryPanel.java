import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import ij.IJ;
import ij.ImagePlus;
import ij.plugin.FolderOpener;

public class BoundaryPanel extends JLayeredPane {
	MainFrame parentFrame;
	JLayeredPane self = this;

	JLabel importedImageLabel, importedMaskLabel, importedBboxFolderLabel, importedForwardTracksLabel;
	String imagePath, maskPath, bboxFolderPath, forwardTracksPath, backwardTracksPath, saveDirectory1, saveDirectory2,
			workspacePath, projectName;
	JButton importMaskButton, importBboxFolder, importTracksButton, importImageButton;

	boolean ok1, ok2, ok3, ok4 = false;
	private JLabel stepLabel;
	private JLabel lblStep;
	private JLabel lblStep_1;
	private JLabel lblStep_2;
	private JLabel lblOutputBoundaryFile;

	public BoundaryPanel(MainFrame parentFrame) {
		setOpaque(true);

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

		JLabel titleLabel = new JLabel("Boundary visualisation", SwingConstants.CENTER);
		titleLabel.setVerticalTextPosition(SwingConstants.CENTER);
		titleLabel.setHorizontalTextPosition(SwingConstants.CENTER);
		titleLabel.setFont(new Font("Arial Black", Font.BOLD, 23));
		titleLabel.setBounds(0, 0, 500, 36);
		add(titleLabel);

		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
//				// stop swing worker
//				if (swingWorkerStarted)
//					swingWorker.destroy();

				// display menuPanel and close current panel
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
				if (ok1 && ok2 && ok3 && ok4) {
					// saveDirectory = IJ.getDirectory("Choose saving directory");
					// saveDirectory = saveDirectory.substring(0, saveDirectory.length() - 1);

					importMaskButton.setEnabled(false);
					importBboxFolder.setEnabled(false);
					importTracksButton.setEnabled(false);
					importImageButton.setEnabled(false);

					String timestamp = Globals.getFormattedDate();
					saveDirectory1 = workspacePath + "/" + projectName + "/data_analysis/images/" + timestamp;
					new File(saveDirectory1).mkdirs();

					saveDirectory2 = workspacePath + "/" + projectName + "/data_analysis/matlab_files/" + timestamp;
					new File(saveDirectory2).mkdirs();

//					File saveFolder = new File(saveDirectory + "/" + Globals.getNameWithoutExtension(imagePath)
//							+ "_boundary_visualisation_image_sequence");
//					saveFolder.mkdir();

					ProgressPanel progress = new ProgressPanel(self, 40, 200);
					self.add(progress);
					self.setLayer(progress, 1);
					progress.setVisibility(false);

					class BoundaryVis extends SwingWorker<String, String> {
						protected String doInBackground() throws Exception {
							progress.setVisibility(true);
							progress.setIndeterminate(true);
							progress.setFileCount(1);
							progress.setFileNumber(1);
							progress.setFileName(Globals.getName(imagePath));
							progress.setMessage("Please wait this may take a couple of minutes...");

//							PythonScript script = new PythonScript("Boundary visualisation");
//
//							script.addCommnet("import modules");
//							script.importModule("os");
//							script.importModule("sys");
//							script.importModule("scipy.io", "spio");
//							script.importModule("pylab", "plt");
//							script.importModule("numpy", "np");
//							script.importModuleFrom("read_multiimg_PIL", "MOSES.Utility_Functions.file_io");
//							script.importModuleFrom("plot_tracks", "MOSES.Visualisation_Tools.track_plotting");
//
//							List<Parameter> parameters = new ArrayList<Parameter>();
//							parameters.addAll(Arrays.asList(new Parameter("imagePath", "str", imagePath),
//									new Parameter("boundaryPath", "str", boundaryFilePath),
//									new Parameter("saveDirectory", "str", saveFolder.getAbsolutePath()),
//									new Parameter("fileName", "str", Globals.getNameWithoutExtension(imagePath))));
//
//							script.addCommnet("define parameters dictionary");
//							script.addParameter(parameters);
//							script.addScript(script.createParameterDictionary());
//							script.newLine();
//
//							script.addCommnet("import tiff stack");
//							script.addScript(PythonScript.setValue("vidstack",
//									PythonScript.callFunction("read_multiimg_PIL", "parameters.get('imagePath')")));
//							script.startIf("len(vidstack.shape) == 3");
//							script.addScript(PythonScript.setValue("vidstack", "vidstack[..., None]"));
//							script.addScript(PythonScript.callFunction("plt.set_cmap", PythonScript.addString("gray")));
//							script.stopIf();
//							script.addScript(PythonScript.setValue(
//									Arrays.asList("frames", "rows", "columns", "channels"), "vidstack.shape"));
//
//							script.addScript(PythonScript.setValue("boundaryFile",
//									PythonScript.callFunction("spio.loadmat", "parameters.get('boundaryPath')")));
//							script.addScript(PythonScript.setValue("boundaries", "boundaryFile['boundaries']"));
//
//							script.addScript(PythonScript.setValue("vidstack",
//									PythonScript.callFunction("np.squeeze", "vidstack")));
//							script.addScript(PythonScript.setValue("saveLocation", "parameters.get('saveDirectory')"));
//
//							script.addScript(PythonScript.print(PythonScript.addString("-Plotting boundaries...")));
//							script.addScript(
//									PythonScript.print(PythonScript.addString(">") + " + str(boundaries.shape[1])"));
//							script.startFor("i", "boundaries.shape[1]");
//							script.addScript(PythonScript.print(PythonScript.addString("!") + " + str(i)"));
//							script.addScript(PythonScript.setValue("frame_img", "vidstack[i]"));
//							script.addScript(PythonScript.setValue("fig", PythonScript.callFunction("plt.figure", "")));
//							script.addScript(PythonScript.callFunction("fig.set_size_inches",
//									Arrays.asList("float(columns) / rows", "1", "forward = False")));
//							script.addScript(PythonScript.setValue("ax",
//									PythonScript.callFunction("plt.Axes", Arrays.asList("fig", "[0.,0.,1.,1.]"))));
//							script.addScript(PythonScript.callFunction("ax.set_axis_off", ""));
//							script.addScript(PythonScript.callFunction("fig.add_axes", "ax"));
//							script.addScript(PythonScript.callFunction("ax.set_xlim", "[0, columns]"));
//							script.addScript(PythonScript.callFunction("ax.set_ylim", "[rows, 0]"));
//							script.addScript(PythonScript.callFunction("ax.grid", PythonScript.addString("off")));
//							script.addScript(PythonScript.callFunction("ax.axis", PythonScript.addString("off")));
//							script.addScript(
//									PythonScript.callFunction("ax.imshow", Arrays.asList("frame_img", "alpha = 0.6")));
//							script.newLine();
//
//							script.startFor("j", "boundaries.shape[0]");
//
//							script.startIf("boundaries[j, i].shape[0] == 100");
//							script.addScript(PythonScript.callFunction("plot_tracks",
//									Arrays.asList("boundaries[j, i:i+1]", "ax")));
//							script.stopIf();
//
//							script.addScript(PythonScript.callFunction("fig.savefig", Arrays.asList(
//									PythonScript.callFunction("os.path.join", Arrays.asList("saveLocation",
//											"parameters.get('fileName') + '_boundary_plot_%s' %(str(i).zfill(3)) + '.png'")),
//									"dpi = rows")));
//							script.addScript(PythonScript.callFunction("plt.close", "fig"));
//							script.stopFor();

							/////

							File script = new File(
									System.getProperty("user.dir") + "/plugins/MOSES/Script/compute_boundary.py");

							String[] command = new String[10];
							command[0] = "python";
							command[1] = script.getAbsolutePath();
							command[2] = imagePath;
							command[3] = bboxFolderPath;
							command[4] = maskPath;
							command[5] = forwardTracksPath;
							command[6] = backwardTracksPath;
							command[7] = saveDirectory1;
							command[8] = saveDirectory2;
							command[9] = Globals.getNameWithoutExtension(imagePath);

							// ProcessBuilder pb = new ProcessBuilder(command);

							try {
								// Process p = pb.start();
								Process p = Runtime.getRuntime().exec(command);

								BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
								String line;
								while ((line = in.readLine()) != null) {
									publish(line);
									Thread.yield();
								}

								p.waitFor();

							} catch (IOException e) {
								IJ.handleException(e);
							}

							////////

							publish("-Generating tiff stack...");
							Thread.yield();

							File boundaryFolder = new File(saveDirectory1 + "/boundaries");
							File pointsFolder = new File(saveDirectory1 + "/MOSES_pts");

							ImagePlus imp = FolderOpener.open(boundaryFolder.getAbsolutePath(), "");
							IJ.saveAs(imp, "Tiff", saveDirectory1 + "/" + Globals.getNameWithoutExtension(imagePath)
									+ "_boundary_visualisation.tif");

							ImagePlus imp2 = FolderOpener.open(pointsFolder.getAbsolutePath(), "");
							IJ.saveAs(imp2, "Tiff", saveDirectory1 + "/" + Globals.getNameWithoutExtension(imagePath)
									+ "_MOSES_points.tif");

							//////

							publish("-Deleting temporary files...");
							Thread.yield();

							String[] entries = boundaryFolder.list();
							for (String fileName : entries) {
								File currentFile = new File(boundaryFolder.getPath(), fileName);
								currentFile.delete();
							}

							boundaryFolder.delete();

							String[] entries2 = pointsFolder.list();
							for (String fileName : entries2) {
								File currentFile = new File(pointsFolder.getPath(), fileName);
								currentFile.delete();
							}

							pointsFolder.delete();

							return "Done.";
						}

						@Override
						protected void process(List<String> messages) {
							for (String m : messages) {
								boolean validMessage = false;
								char messageScope = m.charAt(0);
								m = m.substring(1);

								if (messageScope == '-') {
									validMessage = true;
									if (!progress.isIndeterminate()) {
										progress.setIndeterminate(true);
										progress.setStringPainted(false);
									}
									try {
										Thread.sleep(1000);
									} catch (InterruptedException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								}
								if (messageScope == '>') {
									validMessage = false;
									progress.setIndeterminate(false);
									progress.setStringPainted(true);
									progress.setMaximum(Integer.parseInt(m));
									progress.setValue(0);
								}
								if (messageScope == '!') {
									validMessage = false;
									progress.setValue(Integer.parseInt(m));
									progress.setString(m + " / " + progress.getMaximum());
								}
								if (messageScope == '+') {
									validMessage = false;

									JOptionPane pane = new JOptionPane(m, JOptionPane.ERROR_MESSAGE,
											JOptionPane.PLAIN_MESSAGE);
									JDialog dialog = pane.createDialog(null, "MOSES");
									dialog.setModal(false);
									dialog.show();
								}

								if (validMessage) {
									progress.setMessage("<html>" + m + "  </html>");
								}
							}
						}

						@Override
						protected void done() {
							progress.setVisibility(false);

							parentFrame.empty();
							parentFrame.menuPanel = new MenuPanel(parentFrame);
							parentFrame.getContentPane().add(parentFrame.menuPanel);
							parentFrame.validate();
						}

					}

					BoundaryVis getTracks = new BoundaryVis();
					getTracks.execute();
				} else {
					JFrame errorDialog = new JFrame();
					Object[] options2 = { "Ok" };
					JOptionPane.showOptionDialog(errorDialog, "Please import first all the required files.", "MOSES",
							JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, options2, options2[0]);

				}
			}

		});
		finishButton.setVerticalTextPosition(SwingConstants.CENTER);
		finishButton.setHorizontalTextPosition(SwingConstants.CENTER);
		finishButton.setForeground(Color.WHITE);
		finishButton.setFont(new Font("Arial", Font.BOLD, 15));
		finishButton.setBackground(new Color(13, 59, 102));
		finishButton.setBounds(350, 430, 140, 30);
		add(finishButton);

		importImageButton = new JButton("Import image (.tif)");
		importImageButton.setEnabled(false);
		importImageButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
//				ArrayList<String> validExtensions = new ArrayList<String>();
//				validExtensions.addAll(Arrays.asList(".tif", ".tiff"));
//
//				JFrame dialog = new JFrame();
//
//				// display import file window
//
//				String importedFilePath = Globals.openFile(parentFrame.ui, validExtensions, false);
//
//				if (importedFilePath != null) {
//					imagePath = importedFilePath;
//
//					ok1 = true;
//					importedImageLabel.setText("Selected file: " + importedFilePath);
//				} else {
//					// display error dialog box
//
//					JFrame errorDialog = new JFrame();
//					Object[] options2 = { "Ok" };
//					JOptionPane.showOptionDialog(errorDialog, "Current selected file has an invalid format", "MOSES",
//							JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, options2, options2[0]);
//
//					importedImageLabel.setText("No file selected.");
//					ok1 = false;
//				}

				JFrame dialog = new JFrame();
				Object[] options = { "Ok" };
				JOptionPane.showOptionDialog(dialog, "Plese choose the folder that contains the input images.", "MOSES",
						JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

				String imageDirectoryPath = IJ.getDirectory("Choose image directory");

				if (imageDirectoryPath != null) {
					List<File> imageFiles = Globals.getFiles(new File(imageDirectoryPath).listFiles(),
							Arrays.asList(".tif", ".tiff"), false);

					boolean match = false;
					for (File f : imageFiles)
						if (Globals.getNameWithoutExtension(f.getAbsolutePath()).equals(projectName)) {
							imagePath = f.getAbsolutePath();
							ok2 = true;
							importedImageLabel.setText("Selected file: " + Globals.getName(imagePath));

							importMaskButton.setEnabled(true);

							match = true;
							break;
						}

					if (!match) {
						JFrame errorDialog = new JFrame();
						Object[] options2 = { "Ok" };
						JOptionPane.showOptionDialog(errorDialog, "No matching file found", "MOSES",
								JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, options2, options2[0]);

						importedImageLabel.setText("No file selected.");
						ok1 = false;

						importMaskButton.setEnabled(false);
					}

				}
			}
		});
		importImageButton.setVerticalTextPosition(SwingConstants.CENTER);
		importImageButton.setHorizontalTextPosition(SwingConstants.CENTER);
		importImageButton.setForeground(Color.WHITE);
		importImageButton.setFont(new Font("Arial", Font.BOLD, 15));
		importImageButton.setBackground(new Color(13, 59, 102));
		importImageButton.setBounds(138, 189, 224, 22);
		add(importImageButton);

		importMaskButton = new JButton("Import image mask (.tif)");
		importMaskButton.setEnabled(false);
		importMaskButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
//				ArrayList<String> validExtensions = new ArrayList<String>();
//				validExtensions.addAll(Arrays.asList(".tif"));
//
//				JFrame dialog = new JFrame();
//
//				// display import file window
//
//				String importedFilePath = Globals.openFile(parentFrame.ui, validExtensions, false);
//
//				if (importedFilePath != null) {
//					maskPath = importedFilePath;
//
//					ok2 = true;
//					importedMaskLabel.setText("Selected file: " + importedFilePath);
//				} else {
//					// display error dialog box
//
//					JFrame errorDialog = new JFrame();
//					Object[] options2 = { "Ok" };
//					JOptionPane.showOptionDialog(errorDialog, "Current selected file has an invalid format.", "MOSES",
//							JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, options2, options2[0]);
//
//					importedMaskLabel.setText("No file selected.");
//					ok2 = false;
//				}

				FileSelecter selecter = new FileSelecter();
				selecter.setSelectAllButton(false);
				selecter.setVisible(true);
				selecter.maskList(workspacePath, Arrays.asList(projectName),
						"Select for each project the motion track you want to plot.");

				selecter.importButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						List<String> maskPaths = selecter.getSelected();
						selecter.dispose();

						if (maskPaths.size() >= 1) {
							maskPath = maskPaths.get(0);

							ok3 = true;
							importedMaskLabel.setText("Selected file: " + Globals.getName(maskPath));
							importBboxFolder.setEnabled(true);
						} else {
							ok3 = false;
							importedMaskLabel.setText("No file selected.");
							importBboxFolder.setEnabled(false);
						}

					}
				});
			}
		});
		importMaskButton.setVerticalTextPosition(SwingConstants.CENTER);
		importMaskButton.setHorizontalTextPosition(SwingConstants.CENTER);
		importMaskButton.setForeground(Color.WHITE);
		importMaskButton.setFont(new Font("Arial", Font.BOLD, 15));
		importMaskButton.setBackground(new Color(13, 59, 102));
		importMaskButton.setBounds(136, 264, 228, 22);
		add(importMaskButton);

		importedImageLabel = new JLabel("No file selected.");
		importedImageLabel.setHorizontalAlignment(SwingConstants.CENTER);
		importedImageLabel.setFont(new Font("Roboto", Font.PLAIN, 15));
		importedImageLabel.setBounds(50, 224, 400, 16);
		add(importedImageLabel);

		importedMaskLabel = new JLabel("No file selected.");
		importedMaskLabel.setHorizontalAlignment(SwingConstants.CENTER);
		importedMaskLabel.setFont(new Font("Roboto", Font.PLAIN, 15));
		importedMaskLabel.setBounds(50, 299, 400, 16);
		add(importedMaskLabel);

		importBboxFolder = new JButton("Import bounding boxes folder");
		importBboxFolder.setEnabled(false);
		importBboxFolder.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				// display import file window
//
//				String importedFolderPath = IJ.getDirectory("Select bounding boxes folder");
//
//				if (importedFolderPath != null) {
//					bboxFolderPath = importedFolderPath.substring(0, importedFolderPath.length() - 1);
//
//					ok3 = true;
//					importedBboxFolderLabel.setText("Selected file: " + importedFolderPath);
//				} else {
//					// display error dialog box
//
//					JFrame errorDialog = new JFrame();
//					Object[] options2 = { "Ok" };
//					JOptionPane.showOptionDialog(errorDialog, "Current selected file has an invalid format.", "MOSES",
//							JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, options2, options2[0]);
//
//					importedBboxFolderLabel.setText("No file selected.");
//					ok3 = false;
//				}
				FileSelecter selecter = new FileSelecter();
				selecter.setSelectAllButton(false);
				selecter.setVisible(true);
				selecter.listFolders(workspacePath + "/" + projectName, "bounding_boxes",
						"Select the folder containg the bounding boxes");

				selecter.importButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						List<String> folderPaths = selecter.getSelected();
						selecter.dispose();

						if (folderPaths.size() >= 1) {
							bboxFolderPath = folderPaths.get(0);

							ok4 = true;
							importedBboxFolderLabel.setText("Selected file: " + Globals.getName(bboxFolderPath));
						} else {
							ok4 = false;
							importedBboxFolderLabel.setText("No file selected.");
						}

					}
				});

			}
		});
		importBboxFolder.setVerticalTextPosition(SwingConstants.CENTER);
		importBboxFolder.setHorizontalTextPosition(SwingConstants.CENTER);
		importBboxFolder.setForeground(Color.WHITE);
		importBboxFolder.setFont(new Font("Arial", Font.BOLD, 15));
		importBboxFolder.setBackground(new Color(13, 59, 102));
		importBboxFolder.setBounds(123, 327, 254, 22);
		add(importBboxFolder);

		importedBboxFolderLabel = new JLabel("No file selected.");
		importedBboxFolderLabel.setHorizontalAlignment(SwingConstants.CENTER);
		importedBboxFolderLabel.setFont(new Font("Roboto", Font.PLAIN, 15));
		importedBboxFolderLabel.setBounds(50, 362, 400, 16);
		add(importedBboxFolderLabel);

		importTracksButton = new JButton("Import tracks (.mat)");
		importTracksButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
//				ArrayList<String> validExtensions = new ArrayList<String>();
//				validExtensions.addAll(Arrays.asList(".mat"));
//
//				JFrame dialog = new JFrame();
//
//				// display import file window
//
//				String importedFilePath = Globals.openFile(parentFrame.ui, validExtensions, false);
//
//				if (importedFilePath != null) {
//					forwardTracksPath = importedFilePath;
//
//					ok4 = true;
//					importedForwardTracksLabel.setText("Selected file: " + importedFilePath);
//				} else {
//					// display error dialog box
//
//					JFrame errorDialog = new JFrame();
//					Object[] options2 = { "Ok" };
//					JOptionPane.showOptionDialog(errorDialog, "Current selected file has an invalid format.", "MOSES",
//							JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, options2, options2[0]);
//
//					importedForwardTracksLabel.setText("No file selected.");
//					ok4 = false;
//				}

				JFrame dialog = new JFrame();
				Object[] options = { "Ok" };
				int n = JOptionPane.showOptionDialog(dialog, "Plese select the workspace you want to work with.",
						"MOSES", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

				if (n == 0) {
					workspacePath = Globals.getWorkspace();

					if (workspacePath != null) {
						FileSelecter selecter = new FileSelecter();
						selecter.setSelectAllButton(false);
						selecter.setVisible(true);
						selecter.tracksFwBkwList(workspacePath, Globals.getProjectList(workspacePath),
								"Select the motion tracks you want to use.");

						selecter.importButton.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								List<String> tracksPaths = selecter.getSelected();
								selecter.dispose();

								if (tracksPaths.size() >= 1) {
									backwardTracksPath = tracksPaths.get(0).split(",")[0];
									forwardTracksPath = tracksPaths.get(0).split(",")[1];
									MatlabMetadata metadata = new MatlabMetadata(forwardTracksPath);
									projectName = metadata.getParentFile();

									ok1 = true;
									importedForwardTracksLabel
											.setText("<html>Selected file: " + Globals.getName(forwardTracksPath)
													+ "<br>" + Globals.getName(backwardTracksPath) + "</html>");

									importImageButton.setEnabled(true);
								}

							}
						});
					} else {
						JFrame errorDialog = new JFrame();
						Object[] options2 = { "Ok" };
						JOptionPane.showOptionDialog(errorDialog, "Current selected file has an invalid format.",
								"MOSES", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, options2,
								options2[0]);

						importedForwardTracksLabel.setText("No file selected.");
						ok1 = false;
					}
				}
			}
		});
		importTracksButton.setVerticalTextPosition(SwingConstants.CENTER);
		importTracksButton.setHorizontalTextPosition(SwingConstants.CENTER);
		importTracksButton.setForeground(Color.WHITE);
		importTracksButton.setFont(new Font("Arial", Font.BOLD, 15));
		importTracksButton.setBackground(new Color(13, 59, 102));
		importTracksButton.setBounds(123, 67, 254, 22);
		add(importTracksButton);

		importedForwardTracksLabel = new JLabel("No file selected.");
		importedForwardTracksLabel.setHorizontalAlignment(SwingConstants.CENTER);
		importedForwardTracksLabel.setFont(new Font("Roboto", Font.PLAIN, 15));
		importedForwardTracksLabel.setBounds(50, 102, 400, 54);
		add(importedForwardTracksLabel);

		stepLabel = new JLabel("STEP 1");
		stepLabel.setVerticalAlignment(SwingConstants.TOP);
		stepLabel.setHorizontalAlignment(SwingConstants.LEFT);
		stepLabel.setForeground(Color.DARK_GRAY);
		stepLabel.setFont(new Font("Roboto", Font.BOLD, 15));
		stepLabel.setBounds(24, 69, 53, 30);
		add(stepLabel);

		lblStep = new JLabel("STEP 2");
		lblStep.setVerticalAlignment(SwingConstants.TOP);
		lblStep.setHorizontalAlignment(SwingConstants.LEFT);
		lblStep.setForeground(Color.DARK_GRAY);
		lblStep.setFont(new Font("Roboto", Font.BOLD, 15));
		lblStep.setBounds(24, 191, 53, 30);
		add(lblStep);

		lblStep_1 = new JLabel("STEP 3");
		lblStep_1.setVerticalAlignment(SwingConstants.TOP);
		lblStep_1.setHorizontalAlignment(SwingConstants.LEFT);
		lblStep_1.setForeground(Color.DARK_GRAY);
		lblStep_1.setFont(new Font("Roboto", Font.BOLD, 15));
		lblStep_1.setBounds(24, 266, 53, 30);
		add(lblStep_1);

		lblStep_2 = new JLabel("STEP 4");
		lblStep_2.setVerticalAlignment(SwingConstants.TOP);
		lblStep_2.setHorizontalAlignment(SwingConstants.LEFT);
		lblStep_2.setForeground(Color.DARK_GRAY);
		lblStep_2.setFont(new Font("Roboto", Font.BOLD, 15));
		lblStep_2.setBounds(24, 327, 53, 30);
		add(lblStep_2);

		lblOutputBoundaryFile = new JLabel("Output: boundary file (.mat) + visualisation (.tif)");
		lblOutputBoundaryFile.setHorizontalAlignment(SwingConstants.CENTER);
		lblOutputBoundaryFile.setFont(new Font("Roboto", Font.PLAIN, 15));
		lblOutputBoundaryFile.setBounds(12, 402, 400, 16);
		add(lblOutputBoundaryFile);

	}
}
