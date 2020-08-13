import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.GeneralPath;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.scijava.ui.UIService;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Overlay;
import ij.gui.Roi;
import ij.gui.ShapeRoi;
import ij.plugin.FolderOpener;
import net.imagej.display.ImageDisplayService;

public class ComputeTracksPanel2 extends JPanel {
	protected static final int Thread = 0;
	public UIService ui;
	public ImageDisplayService imageDisplayService;
	public MainFrame parentFrame;
	private JTextField textField;
	public ComputeTracksPanel2 self = this;

	public ComputeTracksPanel2(MainFrame parentFrame) {

		try {
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.motif.MotifLookAndFeel");
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e1) {
			e1.printStackTrace();
		}

		this.parentFrame = parentFrame;

		// set size

		this.setPreferredSize(new Dimension(Globals.frameWidth, Globals.frameHight));

		// set background color

		this.setBackground(new Color(252, 252, 252));

		// title labels

		JLabel titleLabel = new JLabel("Compute superpixel tracks", SwingConstants.CENTER);
		titleLabel.setBounds(0, 0, 500, 36);
		titleLabel.setVerticalTextPosition(SwingConstants.CENTER);
		titleLabel.setHorizontalTextPosition(SwingConstants.CENTER);
		titleLabel.setFont(new Font("Arial Black", Font.BOLD, 25));

		// save tracks checkbox

		JCheckBox saveCheckBox = new JCheckBox("<html> Save the computed tracks in MATLAB format </html>");
		saveCheckBox.setHorizontalAlignment(SwingConstants.CENTER);
		saveCheckBox.setBounds(258, 348, 252, 52);
		saveCheckBox.setFont(new Font("Roboto", Font.PLAIN, 14));
		saveCheckBox.setBackground(new Color(252, 252, 252));
		saveCheckBox.setSelected(true);

		// collect parameters labels

		JLabel lblOpticalFlowParameter = new JLabel("Optical flow parameter", SwingConstants.CENTER);
		lblOpticalFlowParameter.setBounds(10, 140, 230, 39);
		lblOpticalFlowParameter.setVerticalTextPosition(SwingConstants.CENTER);
		lblOpticalFlowParameter.setHorizontalTextPosition(SwingConstants.CENTER);
		lblOpticalFlowParameter.setFont(new Font("Arial", Font.BOLD, 18));

		JLabel scaleFactorLabel = new JLabel("Scale factor:", SwingConstants.LEFT);
		scaleFactorLabel.setBounds(20, 190, 90, 20);
		scaleFactorLabel.setVerticalTextPosition(SwingConstants.CENTER);
		scaleFactorLabel.setHorizontalTextPosition(SwingConstants.CENTER);
		scaleFactorLabel.setFont(new Font("Roboto", Font.PLAIN, 15));

		JLabel levelsLabel = new JLabel("Levels:", SwingConstants.LEFT);
		levelsLabel.setBounds(20, 220, 90, 20);
		levelsLabel.setVerticalTextPosition(SwingConstants.CENTER);
		levelsLabel.setHorizontalTextPosition(SwingConstants.CENTER);
		levelsLabel.setFont(new Font("Roboto", Font.PLAIN, 15));

		JLabel windowSizeLabel = new JLabel("Window size:", SwingConstants.LEFT);
		windowSizeLabel.setBounds(20, 250, 90, 20);
		windowSizeLabel.setVerticalTextPosition(SwingConstants.CENTER);
		windowSizeLabel.setHorizontalTextPosition(SwingConstants.CENTER);
		windowSizeLabel.setFont(new Font("Roboto", Font.PLAIN, 15));

		JLabel iterationsLabel = new JLabel("Iterations:", SwingConstants.LEFT);
		iterationsLabel.setBounds(20, 280, 90, 20);
		iterationsLabel.setVerticalTextPosition(SwingConstants.CENTER);
		iterationsLabel.setHorizontalTextPosition(SwingConstants.CENTER);
		iterationsLabel.setFont(new Font("Roboto", Font.PLAIN, 15));

		JLabel polyNLabel = new JLabel("Poly n:", SwingConstants.LEFT);
		polyNLabel.setBounds(20, 310, 90, 20);
		polyNLabel.setVerticalTextPosition(SwingConstants.CENTER);
		polyNLabel.setHorizontalTextPosition(SwingConstants.CENTER);
		polyNLabel.setFont(new Font("Roboto", Font.PLAIN, 15));

		JLabel polySigmaLabel = new JLabel("Poly sigma:", SwingConstants.LEFT);
		polySigmaLabel.setBounds(20, 340, 90, 20);
		polySigmaLabel.setVerticalTextPosition(SwingConstants.CENTER);
		polySigmaLabel.setHorizontalTextPosition(SwingConstants.CENTER);
		polySigmaLabel.setFont(new Font("Roboto", Font.PLAIN, 15));

		JLabel superpixelsPanel = new JLabel("Number of superpixels", SwingConstants.CENTER);
		superpixelsPanel.setBounds(260, 140, 230, 39);
		superpixelsPanel.setVerticalTextPosition(SwingConstants.CENTER);
		superpixelsPanel.setHorizontalTextPosition(SwingConstants.CENTER);
		superpixelsPanel.setFont(new Font("Arial", Font.BOLD, 18));

		JLabel flagsLabel = new JLabel("Flags:", SwingConstants.LEFT);
		flagsLabel.setBounds(20, 370, 90, 20);
		flagsLabel.setVerticalTextPosition(SwingConstants.CENTER);
		flagsLabel.setHorizontalTextPosition(SwingConstants.CENTER);
		flagsLabel.setFont(new Font("Roboto", Font.PLAIN, 15));

		// cancel button

		JButton cancelButton = new JButton("Cancel");
		cancelButton.setBounds(10, 430, 140, 30);
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// display menuPanel and close current panel

				parentFrame.empty();
				parentFrame.getContentPane().add(parentFrame.menuPanel);
				parentFrame.validate();
			}
		});
		cancelButton.setVerticalTextPosition(SwingConstants.CENTER);
		cancelButton.setHorizontalTextPosition(SwingConstants.CENTER);
		cancelButton.setForeground(Color.WHITE);
		cancelButton.setFont(new Font("Arial", Font.BOLD, 15));
		cancelButton.setBackground(new Color(13, 59, 102));

		// back button - return to previous panel

		JButton backButton = new JButton("Back");
		backButton.setBounds(200, 430, 140, 30);
		backButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// display menuPanel and close current panel

				parentFrame.empty();
				parentFrame.getContentPane().add(parentFrame.computeTracksPanel1);
				parentFrame.computeTracksPanel1.updateFields();
				parentFrame.validate();
			}
		});
		backButton.setVerticalTextPosition(SwingConstants.CENTER);
		backButton.setHorizontalTextPosition(SwingConstants.CENTER);
		backButton.setForeground(Color.WHITE);
		backButton.setFont(new Font("Arial", Font.BOLD, 15));
		backButton.setBackground(new Color(13, 59, 102));

		JPanel numberSuperpixelsPanel = new JPanel();
		numberSuperpixelsPanel.setLayout(null);
		numberSuperpixelsPanel.setBounds(260, 140, 230, 90);
		add(numberSuperpixelsPanel);

		// collect parameters fields

		DecimalFormat decimalFormat = new DecimalFormat("#.##");
		decimalFormat.setGroupingUsed(false);
		DecimalFormat integerFormat = new DecimalFormat("###");
		decimalFormat.setGroupingUsed(false);

		JFormattedTextField scaleFactorField = new JFormattedTextField(decimalFormat) {
			@Override
			protected void processFocusEvent(final FocusEvent e) {
				if (e.isTemporary()) {
					return;
				}

				if (e.getID() == FocusEvent.FOCUS_LOST) {
					if (getText() == null || getText().isEmpty()) {
						setValue(0);
					}
				}
				super.processFocusEvent(e);
			}
		};

		scaleFactorField.setHorizontalAlignment(SwingConstants.CENTER);
		scaleFactorField.setFont(new Font("Roboto", Font.PLAIN, 15));
		scaleFactorField.setText("0.5");
		scaleFactorField.setBounds(135, 190, 60, 20);

		JFormattedTextField levelsField = new JFormattedTextField(integerFormat) {
			@Override
			protected void processFocusEvent(final FocusEvent e) {
				if (e.isTemporary()) {
					return;
				}

				if (e.getID() == FocusEvent.FOCUS_LOST) {
					if (getText() == null || getText().isEmpty()) {
						setValue(0);
					}
				}
				super.processFocusEvent(e);
			}
		};
		levelsField.setText("3");
		levelsField.setHorizontalAlignment(SwingConstants.CENTER);
		levelsField.setFont(new Font("Roboto", Font.PLAIN, 15));
		levelsField.setBounds(135, 220, 60, 20);

		JFormattedTextField windowSizeField = new JFormattedTextField(integerFormat) {
			@Override
			protected void processFocusEvent(final FocusEvent e) {
				if (e.isTemporary()) {
					return;
				}

				if (e.getID() == FocusEvent.FOCUS_LOST) {
					if (getText() == null || getText().isEmpty()) {
						setValue(0);
					}
				}
				super.processFocusEvent(e);
			}
		};
		windowSizeField.setText("15");
		windowSizeField.setHorizontalAlignment(SwingConstants.CENTER);
		windowSizeField.setFont(new Font("Roboto", Font.PLAIN, 15));
		windowSizeField.setBounds(135, 250, 60, 20);

		JFormattedTextField iterationsField = new JFormattedTextField(integerFormat) {
			@Override
			protected void processFocusEvent(final FocusEvent e) {
				if (e.isTemporary()) {
					return;
				}

				if (e.getID() == FocusEvent.FOCUS_LOST) {
					if (getText() == null || getText().isEmpty()) {
						setValue(0);
					}
				}
				super.processFocusEvent(e);
			}
		};
		iterationsField.setText("3");
		iterationsField.setHorizontalAlignment(SwingConstants.CENTER);
		iterationsField.setFont(new Font("Roboto", Font.PLAIN, 15));
		iterationsField.setBounds(135, 280, 60, 20);

		JFormattedTextField polyNField = new JFormattedTextField(integerFormat) {
			@Override
			protected void processFocusEvent(final FocusEvent e) {
				if (e.isTemporary()) {
					return;
				}

				if (e.getID() == FocusEvent.FOCUS_LOST) {
					if (getText() == null || getText().isEmpty()) {
						setValue(0);
					}
				}
				super.processFocusEvent(e);
			}
		};
		polyNField.setText("5");
		polyNField.setHorizontalAlignment(SwingConstants.CENTER);
		polyNField.setFont(new Font("Roboto", Font.PLAIN, 15));
		polyNField.setBounds(135, 308, 60, 20);

		JFormattedTextField polySigmaField = new JFormattedTextField(decimalFormat) {
			@Override
			protected void processFocusEvent(final FocusEvent e) {
				if (e.isTemporary()) {
					return;
				}

				if (e.getID() == FocusEvent.FOCUS_LOST) {
					if (getText() == null || getText().isEmpty()) {
						setValue(0);
					}
				}
				super.processFocusEvent(e);
			}
		};
		polySigmaField.setText("1.2");
		polySigmaField.setHorizontalAlignment(SwingConstants.CENTER);
		polySigmaField.setFont(new Font("Roboto", Font.PLAIN, 15));
		polySigmaField.setBounds(135, 340, 60, 20);

		JFormattedTextField flagsField = new JFormattedTextField(integerFormat) {
			@Override
			protected void processFocusEvent(final FocusEvent e) {
				if (e.isTemporary()) {
					return;
				}

				if (e.getID() == FocusEvent.FOCUS_LOST) {
					if (getText() == null || getText().isEmpty()) {
						setValue(0);
					}
				}
				super.processFocusEvent(e);
			}
		};
		flagsField.setText("0");
		flagsField.setHorizontalAlignment(SwingConstants.CENTER);
		flagsField.setFont(new Font("Roboto", Font.PLAIN, 15));
		flagsField.setBounds(135, 370, 60, 20);

		JFormattedTextField numberSuperpixelsField = new JFormattedTextField(integerFormat) {
			@Override
			protected void processFocusEvent(final FocusEvent e) {
				if (e.isTemporary()) {
					return;
				}

				if (e.getID() == FocusEvent.FOCUS_LOST) {
					if (getText() == null || getText().isEmpty()) {
						setValue(0);
					}
				}
				super.processFocusEvent(e);
			}
		};
		numberSuperpixelsField.setBounds(325, 190, 100, 20);
		numberSuperpixelsField.setText("1000");
		numberSuperpixelsField.setHorizontalAlignment(SwingConstants.CENTER);
		numberSuperpixelsField.setFont(new Font("Roboto", Font.PLAIN, 15));

		// next button - generates tracks

		JButton nextButton = new JButton("Next");
		nextButton.setBounds(350, 430, 140, 30);
		nextButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Globals.pyr_scale = Double.parseDouble(scaleFactorField.getText());
				Globals.levels = Integer.parseInt(levelsField.getText());
				Globals.winSize = Integer.parseInt(windowSizeField.getText());
				Globals.iterations = Integer.parseInt(iterationsField.getText());
				Globals.polyn = Integer.parseInt(polyNField.getText());
				Globals.polysigma = Double.parseDouble(polySigmaField.getText());
				Globals.flags = Integer.parseInt(flagsField.getText());
				Globals.numberSuperpixels = Integer.parseInt(numberSuperpixelsField.getText());

				if (saveCheckBox.isEnabled()) {
					Globals.saveDirectory = IJ.getDirectory("Choose save directory");
					IJ.log(Globals.saveDirectory);
				}

				JProgressBar progressBar = new JProgressBar(JProgressBar.HORIZONTAL);
				progressBar.setIndeterminate(true);
				progressBar.setVisible(true);
				progressBar.setBounds(0, 0, 300, 60);

				JDialog loadingDialog = new JDialog((JFrame) null, "Please wait");
				loadingDialog.setSize(300, 60);
				loadingDialog.setResizable(false);
				loadingDialog.setLocationRelativeTo(self);
				loadingDialog.setVisible(true);

				JPanel loadingDialogPanel = new JPanel();
				loadingDialog.getContentPane().add(loadingDialogPanel);
				loadingDialogPanel.setSize(300, 60);
				loadingDialogPanel.setLayout(null);
				loadingDialogPanel.add(progressBar);

				class MyWorker extends SwingWorker<String, Void> {
					protected String doInBackground() {

						Thread thread = new Thread() {

							@Override
							public void run() {
								// create temporary python script file

								String temporaryDirectorPath = System.getProperty("java.io.tmpdir");
								String scriptPath = temporaryDirectorPath + "compute_tracks.py";
								File file = new File(scriptPath);

								ArrayList<String> command = new ArrayList<>();
								command.addAll(Arrays.asList("python", scriptPath, Globals.filePath,
										String.valueOf(Globals.pyr_scale), String.valueOf(Globals.levels),
										String.valueOf(Globals.winSize), String.valueOf(Globals.iterations),
										String.valueOf(Globals.polyn), String.valueOf(Globals.polysigma),
										String.valueOf(Globals.flags), String.valueOf(Globals.numberSuperpixels)));

								String pythonScript = "import scipy.io as spio\r\n" + "import os\r\n" + "import sys\r\n"
										+ "from MOSES.Utility_Functions.file_io import read_multiimg_PIL\r\n"
										+ "from MOSES.Optical_Flow_Tracking.superpixel_track import compute_grayscale_vid_superpixel_tracks\r\n"
										+ "infile = sys.argv[1]\r\n" + "vidstack = read_multiimg_PIL(infile)\r\n"
										+ "optical_flow_params = dict(pyr_scale = float(sys.argv[2]), levels = int(sys.argv[3]), winsize = int(sys.argv[4]), iterations = int(sys.argv[5]), poly_n = int(sys.argv[6]), poly_sigma = float(sys.argv[7]), flags = int(sys.argv[8]))\r\n"
										+ "n_spixels = int(sys.argv[9])\r\n" + "print(\"Finished set up\")\r\n"
										+ "optflow_r, meantracks_r = compute_grayscale_vid_superpixel_tracks(vidstack[:,:,:,0], optical_flow_params, n_spixels)\r\n"
										+ "print(\"Finished extracting superpixel tracks for the 1st channel\")\r\n"
										+ "optflow_g, meantracks_g = compute_grayscale_vid_superpixel_tracks(vidstack[:,:,:,1], optical_flow_params, n_spixels)\r\n"
										+ "print(\"Finished extracting superpixel tracks for the 2nd channel\")\r\n";

								// save tracks if the checkbox is enabled

								if (saveCheckBox.isEnabled()) {
									pythonScript += "fname = os.path.split(infile)[-1]\r\n"
											+ "savetracksmat = (sys.argv[10] + 'meantracks_' + fname).replace('.tif', '.mat')\r\n"
											+ "spio.savemat(savetracksmat, {'meantracks_r': meantracks_r, 'meantracks_g': meantracks_g})\r\n"
											+ "print(\"Saved tracks\")\r\n";

									command.add(Globals.saveDirectory);
								}

								pythonScript += "from MOSES.Utility_Functions.file_io import mkdir\r\n"
										+ "from MOSES.Visualisation_Tools.track_plotting import plot_tracks\r\n"
										+ "import pylab as plt\r\n" + "\r\n"
										+ "save_frame_folder = sys.argv[10] + 'track_video'\r\n"
										+ "mkdir(save_frame_folder)\r\n" + "\r\n" + "len_segment = 5\r\n" + "\r\n"
										+ "for frame in range(len_segment, " + Globals.frames + ", 1):\r\n"
										+ "    frame_img = vidstack[frame]\r\n" + "    fig = plt.figure()\r\n"
										+ "    fig.set_size_inches(float(" + Globals.width + ")/ " + Globals.height
										+ ", 1, forward=False)\r\n"
										+ "    ax=plt.Axes(fig, [0.,0.,1.,1.]); ax.set_axis_off(); fig.add_axes(ax)\r\n"
										+ "    ax.imshow(frame_img, alpha=0.6)\r\n"
										+ "    plot_tracks(meantracks_r[:,frame-len_segment:frame+1],ax, color='r', lw=1)\r\n"
										+ "    plot_tracks(meantracks_g[:,frame-len_segment:frame+1],ax, color='g', lw=1)\r\n"
										+ "    ax.set_xlim([0, " + Globals.width + "]); ax.set_ylim([" + Globals.height
										+ ", 0])\r\n" + "    ax.grid('off'); ax.axis('off')\r\n"
										+ "    print(os.path.join(save_frame_folder, 'tracksimg-%s_' %(str(frame+1).zfill(3))+fname.replace('.tif','.png')))\r\n"
										+ "    fig.savefig(os.path.join(save_frame_folder, 'tracksimg-%s_' %(str(frame+1).zfill(3))+fname.replace('.tif','.png')), dpi="
										+ Globals.height + ")\r\n";

								try {
									FileWriter writer = new FileWriter(file);
									writer.write(pythonScript);
									writer.close();
								} catch (IOException e2) {
									IJ.handleException(e2);
								}

								// run the script

								ProcessBuilder pb = new ProcessBuilder(command);

								IJ.log("Parameters: " + command);
								IJ.log(pythonScript);

								try {
									Process p = pb.start();

									BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
									String line;
									while ((line = in.readLine()) != null) {
										if (line.isEmpty()) {
											break;
										}
										IJ.log(line);
									}
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

						return "Done.";

					}

					protected void done() {
						loadingDialog.setVisible(false);

						ImagePlus imp = FolderOpener.open(Globals.saveDirectory + "track_video", "");
						imp.show();
						IJ.saveAs(imp, "Tiff", Globals.saveDirectory + "track_video.tif");

					}

				}

				new MyWorker().execute();
			}
		});

		nextButton.setVerticalTextPosition(SwingConstants.CENTER);
		nextButton.setHorizontalTextPosition(SwingConstants.CENTER);
		nextButton.setForeground(Color.WHITE);
		nextButton.setFont(new Font("Arial", Font.BOLD, 15));
		nextButton.setBackground(new Color(13, 59, 102));

		// layout

		setLayout(null);
		add(titleLabel);
		add(levelsLabel);
		add(polySigmaLabel);
		add(lblOpticalFlowParameter);
		add(scaleFactorLabel);
		add(flagsLabel);
		add(superpixelsPanel);
		add(saveCheckBox);
		add(cancelButton);
		add(backButton);
		add(nextButton);
		add(windowSizeLabel);
		add(iterationsLabel);
		add(polyNLabel);
		add(scaleFactorField);
		add(levelsField);
		add(windowSizeField);
		add(iterationsField);
		add(polySigmaField);
		add(flagsField);
		add(polyNField);
		add(numberSuperpixelsField);
		add(numberSuperpixelsPanel);

		JLabel numberSuperpixelsHelp = new JLabel("?");
		numberSuperpixelsHelp.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				JFrame dialog = new JFrame("Number of superpixels");
				dialog.setPreferredSize(new Dimension(400, 200));
				dialog.setResizable(false);
				dialog.setLocationRelativeTo(null);
				dialog.getContentPane().setLayout(null);

				JLabel opticalFlowText = new JLabel(
						"<html>Superpixels are the square regions of interest in which the video frame is subdivided. "
								+ "For an image size of n x m pixels, a specification of N superpixels would yield individual superpixels with an approximate size of sqrt(nm/N) x sqrt(nm/N) pixels. "
								+ "For n = 512, m = 512, N = 1000, each superpixel will be of size ≈16×16 pixels. </html>");
				opticalFlowText.setFont(new Font("Roboto", Font.PLAIN, 15));
				opticalFlowText.setBounds(10, 10, 380, 180);

				dialog.getContentPane().add(opticalFlowText);
				dialog.pack();
				dialog.setVisible(true);

			}

			@Override
			public void mouseEntered(MouseEvent e) {
				numberSuperpixelsHelp.setForeground(new Color(255, 74, 28));
			}

			@Override
			public void mouseExited(MouseEvent e) {
				numberSuperpixelsHelp.setForeground(new Color(0, 0, 0));
			}
		});

		numberSuperpixelsHelp.setFont(new Font("Roboto", Font.BOLD, 20));
		numberSuperpixelsHelp.setBounds(210, 70, 15, 16);
		numberSuperpixelsPanel.add(numberSuperpixelsHelp);

		// panels

		JPanel opticalFlowParametersPanel = new JPanel();
		opticalFlowParametersPanel.setBounds(10, 140, 230, 260);
		add(opticalFlowParametersPanel);
		opticalFlowParametersPanel.setLayout(null);

		JLabel opticalFlowHelp = new JLabel("?");
		opticalFlowHelp.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				JFrame dialog = new JFrame("Optical flow parameters");
				dialog.setPreferredSize(new Dimension(800, 600));
				dialog.setResizable(false);
				dialog.setLocationRelativeTo(null);
				dialog.getContentPane().setLayout(null);

				JLabel opticalFlowText = new JLabel(
						"<html>MOSES uses the Farneback flow algorithm to compute pixel velocities. You can change the following parameters to increase the accuracy of the motion field computation. <br><br>"
								+ "<b> pyr_scale </b> – parameter specifying the image scale (less than 1) to build pyramids for each image; pyr_scale = 0.5 means a classical pyramid, where each next layer is twice smaller than the previous one. <br><br>"
								+ "<b> levels </b> – number of pyramid layers including the initial image; levels = 1 means that no extra layers are created and only the original images are used. <br><br>"
								+ "<b> winsize </b> – averaging window size; larger values increase the algorithm robustness to image noise and give more chances for fast motion detection, but yield more blurred motion field. <br><br>"
								+ "<b> iterations</b> – number of iterations the algorithm does at each pyramid level. <br><br>"
								+ "<b>  poly_n </b> – size of the pixel neighborhood used to find polynomial expansion in each pixel; larger values mean that the image will be approximated with smoother surfaces, yielding more robust algorithm and more blurred motion field, typically poly_n = 5 or 7. <br><br>"
								+ "<b> poly_sigma </b>– standard deviation of the Gaussian that is used to smooth derivatives used as a basis for the polynomial expansion; for poly_n = 5, you can set poly_sigma = 1.1, for poly_n = 7, a good value would be poly_sigma = 1.5. <br><br>"
								+ "<b> flags </b >– operation flags that can be a combination of the following: <br>"
								+ "OPTFLOW_USE_INITIAL_FLOW uses the input flow as an initial flow approximation. <br><br> "
								+ "OPTFLOW_FARNEBACK_GAUSSIAN uses the Gaussian   filter instead of a box filter of the same size for optical flow estimation; usually, this option gives z more accurate flow than with a box filter, at the cost of lower speed; normally, winsize for a Gaussian window should be set to a larger value to achieve the same level of robustness. </html>");
				opticalFlowText.setFont(new Font("Roboto", Font.PLAIN, 15));
				opticalFlowText.setBounds(10, 10, 780, 580);

				dialog.getContentPane().add(opticalFlowText);
				dialog.pack();
				dialog.setVisible(true);

			}

			@Override
			public void mouseEntered(MouseEvent e) {
				opticalFlowHelp.setForeground(new Color(255, 74, 28));
			}

			@Override
			public void mouseExited(MouseEvent e) {
				opticalFlowHelp.setForeground(new Color(0, 0, 0));
			}
		});
		opticalFlowHelp.setBounds(210, 240, 15, 16);
		opticalFlowParametersPanel.add(opticalFlowHelp);
		opticalFlowHelp.setFont(new Font("Roboto", Font.BOLD, 20));

		JLabel lblStep = new JLabel("STEP 2");
		lblStep.setVerticalAlignment(SwingConstants.TOP);
		lblStep.setHorizontalAlignment(SwingConstants.LEFT);
		lblStep.setForeground(Color.DARK_GRAY);
		lblStep.setFont(new Font("Roboto", Font.BOLD, 15));
		lblStep.setBounds(10, 50, 53, 30);
		add(lblStep);

		JLabel lblset = new JLabel(
				"<html>Define the optical flow parameters and specify the target number of superpixels used for computing the motion tracks. </html>");
		lblset.setVerticalAlignment(SwingConstants.TOP);
		lblset.setHorizontalAlignment(SwingConstants.LEFT);
		lblset.setForeground(Color.DARK_GRAY);
		lblset.setFont(new Font("Roboto", Font.PLAIN, 15));
		lblset.setBounds(65, 50, 420, 70);
		add(lblset);

		JButton previewButton = new JButton("Preview");
		previewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ImagePlus currentImage = (ImagePlus) IJ.getImage().clone();

				GeneralPath path = new GeneralPath();
				int imageWidth = currentImage.getWidth();
				int imageHeight = currentImage.getHeight();
				double superpixelSize = Math
						.sqrt((imageWidth * imageHeight) / Integer.parseInt(numberSuperpixelsField.getText()));

				for (int i = 0; i < imageWidth; i += superpixelSize) {
					path.moveTo(i, 0);
					path.lineTo(i, imageHeight);
				}
				for (int i = 0; i < imageHeight; i += superpixelSize) {
					path.moveTo(0, i);
					path.lineTo(imageWidth, i);
				}

				Roi roi = new ShapeRoi(path);
				roi.setStrokeColor(new Color(255, 255, 255));
				roi.setStrokeWidth(2);

				currentImage.setOverlay(new Overlay(roi));
				ui.show(currentImage);
			}
		});
		previewButton.setVerticalTextPosition(SwingConstants.CENTER);
		previewButton.setHorizontalTextPosition(SwingConstants.CENTER);
		previewButton.setForeground(Color.WHITE);
		previewButton.setFont(new Font("Arial", Font.BOLD, 15));
		previewButton.setBackground(new Color(13, 59, 102));
		previewButton.setBounds(260, 240, 230, 30);
		add(previewButton);

	}

	public void setServices(UIService ui, ImageDisplayService imageDisplayService) {
		this.ui = ui;
		this.imageDisplayService = imageDisplayService;
	}
}
