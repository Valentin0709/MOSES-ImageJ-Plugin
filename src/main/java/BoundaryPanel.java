import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
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

	JLabel importedImageLabel, importedBoudaryFileLabel;
	String imagePath, boundaryFilePath;

	boolean ok1, ok2 = false;

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
				if (ok1 && ok2) {
					String saveDirectory = IJ.getDirectory("Choose saving directory");

					File saveFolder = new File(saveDirectory + "/" + Globals.getNameWithoutExtension(imagePath)
							+ "_boundary_visualisation_image_sequence");
					saveFolder.mkdir();

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
							progress.setMessage("Processing parameters...");

							PythonScript script = new PythonScript("Boundary visualisation");

							script.addCommnet("import modules");
							script.importModule("os");
							script.importModule("sys");
							script.importModule("scipy.io", "spio");
							script.importModule("pylab", "plt");
							script.importModule("numpy", "np");
							script.importModuleFrom("read_multiimg_PIL", "MOSES.Utility_Functions.file_io");
							script.importModuleFrom("plot_tracks", "MOSES.Visualisation_Tools.track_plotting");

							List<Parameter> parameters = new ArrayList<Parameter>();
							parameters.addAll(Arrays.asList(new Parameter("imagePath", "str", imagePath),
									new Parameter("boundaryPath", "str", boundaryFilePath),
									new Parameter("saveDirectory", "str", saveFolder.getAbsolutePath()),
									new Parameter("fileName", "str", Globals.getNameWithoutExtension(imagePath))));

							script.addCommnet("define parameters dictionary");
							script.addParameter(parameters);
							script.addScript(script.createParameterDictionary());
							script.newLine();

							script.addCommnet("import tiff stack");
							script.addScript(PythonScript.setValue("vidstack",
									PythonScript.callFunction("read_multiimg_PIL", "parameters.get('imagePath')")));
							script.startIf("len(vidstack.shape) == 3");
							script.addScript(PythonScript.setValue("vidstack", "vidstack[..., None]"));
							script.addScript(PythonScript.callFunction("plt.set_cmap", PythonScript.addString("gray")));
							script.stopIf();
							script.addScript(PythonScript.setValue(
									Arrays.asList("frames", "rows", "columns", "channels"), "vidstack.shape"));

							script.addScript(PythonScript.setValue("boundaryFile",
									PythonScript.callFunction("spio.loadmat", "parameters.get('boundaryPath')")));
							script.addScript(PythonScript.setValue("boundaries", "boundaryFile['boundaries']"));

							script.addScript(PythonScript.setValue("vidstack",
									PythonScript.callFunction("np.squeeze", "vidstack")));
							script.addScript(PythonScript.setValue("saveLocation", "parameters.get('saveDirectory')"));

							script.addScript(PythonScript.print(PythonScript.addString("-Plotting boundaries...")));
							script.addScript(
									PythonScript.print(PythonScript.addString(">") + " + str(boundaries.shape[1])"));
							script.startFor("i", "boundaries.shape[1]");
							script.addScript(PythonScript.print(PythonScript.addString("!") + " + str(i)"));
							script.addScript(PythonScript.setValue("frame_img", "vidstack[i]"));
							script.addScript(PythonScript.setValue("fig", PythonScript.callFunction("plt.figure", "")));
							script.addScript(PythonScript.callFunction("fig.set_size_inches",
									Arrays.asList("float(columns) / rows", "1", "forward = False")));
							script.addScript(PythonScript.setValue("ax",
									PythonScript.callFunction("plt.Axes", Arrays.asList("fig", "[0.,0.,1.,1.]"))));
							script.addScript(PythonScript.callFunction("ax.set_axis_off", ""));
							script.addScript(PythonScript.callFunction("fig.add_axes", "ax"));
							script.addScript(PythonScript.callFunction("ax.set_xlim", "[0, columns]"));
							script.addScript(PythonScript.callFunction("ax.set_ylim", "[rows, 0]"));
							script.addScript(PythonScript.callFunction("ax.grid", PythonScript.addString("off")));
							script.addScript(PythonScript.callFunction("ax.axis", PythonScript.addString("off")));
							script.addScript(
									PythonScript.callFunction("ax.imshow", Arrays.asList("frame_img", "alpha = 0.6")));
							script.newLine();

							script.startFor("j", "boundaries.shape[0]");

							script.startIf("boundaries[j, i].shape[0] == 100");
							script.addScript(PythonScript.callFunction("plot_tracks",
									Arrays.asList("boundaries[j, i:i+1]", "ax")));
							script.stopIf();

							script.addScript(PythonScript.callFunction("fig.savefig", Arrays.asList(
									PythonScript.callFunction("os.path.join", Arrays.asList("saveLocation",
											"parameters.get('fileName') + '_boundary_plot_%s' %(str(i).zfill(3)) + '.png'")),
									"dpi = rows")));
							script.addScript(PythonScript.callFunction("plt.close", "fig"));
							script.stopFor();

							/////

							String temporaryDirectorPath = System.getProperty("java.io.tmpdir");
							String scriptPath = temporaryDirectorPath + "get_tracks.py";
							File file = new File(scriptPath);

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

							IJ.log(script.getScript());
							IJ.log(String.valueOf(command));

							try {
								Process p = pb.start();
								p.waitFor();

							} catch (IOException | InterruptedException e1) {
								IJ.handleException(e1);
							}

							////////

							publish("-Generating tiff stack...");
							Thread.yield();

							ImagePlus imp = FolderOpener.open(saveFolder.getAbsolutePath(), "");
							IJ.saveAs(imp, "Tiff", saveDirectory + "/" + Globals.getNameWithoutExtension(imagePath)
									+ "_boundary_visualisation.tif");

							//////

							publish("-Deleting temporary files...");
							Thread.yield();

							String[] entries = saveFolder.list();
							for (String fileName : entries) {
								File currentFile = new File(saveFolder.getPath(), fileName);
								currentFile.delete();
							}

							saveFolder.delete();

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
						}

					}

					BoundaryVis getTracks = new BoundaryVis();
					getTracks.execute();
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

		JButton importImageButton = new JButton("Import image (.tif)");
		importImageButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				ArrayList<String> validExtensions = new ArrayList<String>();
				validExtensions.addAll(Arrays.asList(".tif", ".tiff"));

				JFrame dialog = new JFrame();

				// display import file window

				String importedFilePath = Globals.openFile(parentFrame.ui, validExtensions, false);

				if (importedFilePath != null) {
					imagePath = importedFilePath;

					ok1 = true;
					importedImageLabel.setText("Selected file: " + importedFilePath);
				} else {
					// display error dialog box

					JFrame errorDialog = new JFrame();
					Object[] options2 = { "Ok" };
					JOptionPane.showOptionDialog(errorDialog, "Current selected file has an invalid format", "MOSES",
							JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, options2, options2[0]);

					importedImageLabel.setText("No file selected.");
					ok1 = false;
				}
			}
		});
		importImageButton.setVerticalTextPosition(SwingConstants.CENTER);
		importImageButton.setHorizontalTextPosition(SwingConstants.CENTER);
		importImageButton.setForeground(Color.WHITE);
		importImageButton.setFont(new Font("Arial", Font.BOLD, 15));
		importImageButton.setBackground(new Color(13, 59, 102));
		importImageButton.setBounds(138, 65, 224, 22);
		add(importImageButton);

		JButton importBoundaryFileButton = new JButton("Import boundary file (.mat)");
		importBoundaryFileButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ArrayList<String> validExtensions = new ArrayList<String>();
				validExtensions.addAll(Arrays.asList(".mat"));

				JFrame dialog = new JFrame();

				// display import file window

				String importedFilePath = Globals.openFile(parentFrame.ui, validExtensions, false);

				if (importedFilePath != null) {
					boundaryFilePath = importedFilePath;

					ok2 = true;
					importedBoudaryFileLabel.setText("Selected file: " + importedFilePath);
				} else {
					// display error dialog box

					JFrame errorDialog = new JFrame();
					Object[] options2 = { "Ok" };
					JOptionPane.showOptionDialog(errorDialog, "Current selected file has an invalid format.", "MOSES",
							JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, options2, options2[0]);

					importedBoudaryFileLabel.setText("No file selected.");
					ok2 = false;
				}
			}
		});
		importBoundaryFileButton.setVerticalTextPosition(SwingConstants.CENTER);
		importBoundaryFileButton.setHorizontalTextPosition(SwingConstants.CENTER);
		importBoundaryFileButton.setForeground(Color.WHITE);
		importBoundaryFileButton.setFont(new Font("Arial", Font.BOLD, 15));
		importBoundaryFileButton.setBackground(new Color(13, 59, 102));
		importBoundaryFileButton.setBounds(136, 140, 228, 22);
		add(importBoundaryFileButton);

		importedImageLabel = new JLabel("No file selected.");
		importedImageLabel.setHorizontalAlignment(SwingConstants.CENTER);
		importedImageLabel.setFont(new Font("Roboto", Font.PLAIN, 15));
		importedImageLabel.setBounds(50, 100, 400, 16);
		add(importedImageLabel);

		importedBoudaryFileLabel = new JLabel("No file selected.");
		importedBoudaryFileLabel.setHorizontalAlignment(SwingConstants.CENTER);
		importedBoudaryFileLabel.setFont(new Font("Roboto", Font.PLAIN, 15));
		importedBoudaryFileLabel.setBounds(50, 175, 400, 16);
		add(importedBoudaryFileLabel);

	}
}
