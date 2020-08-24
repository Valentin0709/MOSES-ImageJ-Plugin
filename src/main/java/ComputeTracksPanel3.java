import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.LineBorder;

import ij.IJ;
import ij.ImagePlus;
import ij.plugin.FolderOpener;

public class ComputeTracksPanel3 extends JLayeredPane {

	public MainFrame parentFrame;
	public ComputeTracksPanel3 self = this;

	public JCheckBox saveCheckBox1 = null, saveCheckBox2 = null, saveCheckBox3 = null, saveCheckBox4 = null,
			saveCheckBox5 = null;

	public JPanel optionPanel1, optionPanel2, optionPanel3, bigPanel;
	JScrollPane scrollPane;
	private JPanel loadingBarPanel;
	private JProgressBar loadingBar;

	public JRadioButton tifButtonGroup3, matButtonGroup3;

	ArrayList<JRadioButton> radioButtonGroup1 = new ArrayList<JRadioButton>(),
			radioButtonGroup2 = new ArrayList<JRadioButton>(), radioButtonGroup3 = new ArrayList<JRadioButton>();
	ArrayList<Pair<JComboBox, JLabel>> colorSelect1 = new ArrayList<Pair<JComboBox, JLabel>>(),
			colorSelect2 = new ArrayList<Pair<JComboBox, JLabel>>(),
			colorSelect3 = new ArrayList<Pair<JComboBox, JLabel>>();
	Pair<ArrayList<JRadioButton>, JLabel> saveOption1 = new Pair<>(new ArrayList<JRadioButton>(), new JLabel()),
			saveOption2 = new Pair<>(new ArrayList<JRadioButton>(), new JLabel()),
			saveOption3 = new Pair<>(new ArrayList<JRadioButton>(), new JLabel());

	File forwardTracksImageSequenceFolder, backwardTracksImageSequenceFolder, motionFieldImageSequenceFolder,
			MOSESMeshImageSequenceFolder;
	String forwardTracksImageSequenceFolderPath, backwardTracksImageSequenceFolderPath,
			motionFieldImageSequenceFolderPath, MOSESMeshImageSequenceFolderPath;

	File temporaryImageFile = null;
	private JCheckBox saveCheckBox6, saveCheckBox7;
	private JLabel tracksTitleLabel;
	private JLabel meshTitleLabel;
	private JPanel optionPanel4;
	private JLabel instructionLabel5;
	private JFormattedTextField distanceTresholdField;
	private JPanel optionPanel5;
	private JCheckBox saveCheckBox71;
	private JLabel frameLabel;
	private JFormattedTextField frameField;
	private JCheckBox saveCheckBox72;

	public ComputeTracksPanel3(MainFrame parentFrame) {

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
		this.setOpaque(true);

		loadingBarPanel = new JPanel();
		setLayer(loadingBarPanel, 1);
		loadingBarPanel.setBounds(40, 200, 420, 130);
		loadingBarPanel.setVisible(false);
		setLayout(null);
		loadingBarPanel.setBorder(new LineBorder(new Color(0, 0, 0), 2));
		loadingBarPanel.setOpaque(false);
		loadingBarPanel.setBackground(SystemColor.activeCaption);
		add(loadingBarPanel);
		loadingBarPanel.setLayout(null);

		loadingBar = new JProgressBar();
		loadingBar.setVisible(false);
		loadingBar.setBounds(10, 90, 400, 26);
		loadingBarPanel.add(loadingBar);

		JLabel loadingBarLabel = new JLabel("");
		loadingBarLabel.setVerticalAlignment(SwingConstants.TOP);
		loadingBarLabel.setFont(new Font("Roboto", Font.BOLD, 15));
		loadingBarLabel.setBounds(10, 38, 376, 40);
		loadingBarPanel.add(loadingBarLabel);

		JLabel fileLabel = new JLabel("");
		fileLabel.setBounds(10, 12, 376, 26);
		loadingBarPanel.add(fileLabel);
		fileLabel.setVerticalAlignment(SwingConstants.TOP);
		fileLabel.setFont(new Font("Roboto", Font.BOLD, 15));

		// page instructions

		JLabel backgroundPanel = new JLabel("Compute superpixel tracks", SwingConstants.CENTER);
		backgroundPanel.setBounds(0, 0, 500, 36);
		backgroundPanel.setVerticalTextPosition(SwingConstants.CENTER);
		backgroundPanel.setHorizontalTextPosition(SwingConstants.CENTER);
		backgroundPanel.setFont(new Font("Arial Black", Font.BOLD, 25));
		add(backgroundPanel);

		JLabel instructionLabel1 = new JLabel(
				"<html> Select you preferred outputs. You'll be asked to choose a saving directory at the end. </html>");
		instructionLabel1.setBounds(65, 50, 420, 36);
		instructionLabel1.setVerticalAlignment(SwingConstants.TOP);
		instructionLabel1.setHorizontalAlignment(SwingConstants.LEFT);
		instructionLabel1.setForeground(Color.DARK_GRAY);
		instructionLabel1.setFont(new Font("Roboto", Font.PLAIN, 15));
		add(instructionLabel1);

		JLabel stepLabel = new JLabel("STEP 3");
		stepLabel.setBounds(10, 50, 53, 30);
		stepLabel.setVerticalAlignment(SwingConstants.TOP);
		stepLabel.setHorizontalAlignment(SwingConstants.LEFT);
		stepLabel.setForeground(Color.DARK_GRAY);
		stepLabel.setFont(new Font("Roboto", Font.BOLD, 15));
		add(stepLabel);

		// big panel with scroll pane

		bigPanel = new JPanel();
		bigPanel.setBorder(null);
		bigPanel.setPreferredSize(new Dimension(0, 290));
		bigPanel.setBackground(new Color(252, 252, 252));
		bigPanel.setLayout(null);

		scrollPane = new JScrollPane(bigPanel);
		scrollPane.setBorder(null);
		scrollPane.setBounds(20, 90, 460, 300);
		scrollPane.setViewportBorder(null);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		add(scrollPane);

		// save forward tracks checkbox

		saveCheckBox1 = new JCheckBox("<html>Forward motion tracks (.mat) </html>");
		saveCheckBox1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (saveCheckBox1.isSelected()) {
					saveCheckBox2.setEnabled(true);
				} else {
					saveCheckBox2.setEnabled(false);

					if (saveCheckBox2.isSelected()) {
						saveCheckBox2.setSelected(false);
						moveLowerComponents(optionPanel1, 0, -100);
						setPanelVisibility(optionPanel1, false);
						updateBigPanel();
					}

				}
			}
		});
		saveCheckBox1.setHorizontalAlignment(SwingConstants.LEFT);
		saveCheckBox1.setFont(new Font("Roboto", Font.PLAIN, 15));
		saveCheckBox1.setBackground(Color.WHITE);
		saveCheckBox1.setBounds(10, 25, 400, 20);
		bigPanel.add(saveCheckBox1);

		// save forward tracks with overlay checkbox

		saveCheckBox2 = new JCheckBox("<html>Overlay forward motion tracks on the initial image</html>");
		saveCheckBox2.setEnabled(false);
		saveCheckBox2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (saveCheckBox2.isEnabled()) {

					if (saveCheckBox2.isSelected()) {
						moveLowerComponents(optionPanel1, 0, 100);
						setPanelVisibility(optionPanel1, true);
						updateBigPanel();

					} else {
						moveLowerComponents(optionPanel1, 0, -100);
						setPanelVisibility(optionPanel1, false);
						updateBigPanel();
					}
				}
			}
		});
		saveCheckBox2.setHorizontalAlignment(SwingConstants.LEFT);
		saveCheckBox2.setFont(new Font("Roboto", Font.PLAIN, 15));
		saveCheckBox2.setBackground(Color.WHITE);
		saveCheckBox2.setBounds(30, 45, 400, 20);
		bigPanel.add(saveCheckBox2);

		optionPanel1 = new JPanel();
		optionPanel1.setBounds(30, 65, 405, 100);
		bigPanel.add(optionPanel1);
		optionPanel1.setOpaque(false);
		optionPanel1.setBackground(new Color(238, 238, 238));
		optionPanel1.setLayout(null);

		JLabel instructionLabel2 = new JLabel("<html> Select the display color for each motion track </html>");
		instructionLabel2.setVisible(false);
		instructionLabel2.setBounds(5, 5, 306, 18);
		optionPanel1.add(instructionLabel2);
		instructionLabel2.setVerticalAlignment(SwingConstants.TOP);
		instructionLabel2.setHorizontalAlignment(SwingConstants.LEFT);
		instructionLabel2.setForeground(Color.DARK_GRAY);
		instructionLabel2.setFont(new Font("Roboto", Font.PLAIN, 15));

		generateSaveOptionList(optionPanel1, saveOption1, 320, 5, true);
		generateChannelSelectionList(optionPanel1, colorSelect1, 10, 25);

		// save backward tracks checkbox

		saveCheckBox4 = new JCheckBox("<html>Backward motion tracks (.mat) </html>");
		saveCheckBox4.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (saveCheckBox4.isSelected()) {
					saveCheckBox5.setEnabled(true);
				} else {
					saveCheckBox5.setEnabled(false);

					if (saveCheckBox5.isSelected()) {
						saveCheckBox5.setSelected(false);
						moveLowerComponents(optionPanel2, 0, -100);
						setPanelVisibility(optionPanel2, false);
						updateBigPanel();
					}
				}
			}
		});
		saveCheckBox4.setHorizontalAlignment(SwingConstants.LEFT);
		saveCheckBox4.setFont(new Font("Roboto", Font.PLAIN, 15));
		saveCheckBox4.setBackground(Color.WHITE);
		saveCheckBox4.setBounds(10, 75, 400, 20);
		bigPanel.add(saveCheckBox4);

		// save backward tracks with overlay checkbox

		saveCheckBox5 = new JCheckBox("<html>Overlay backward motion tracks on the initial image</html>");
		saveCheckBox5.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (saveCheckBox5.isEnabled()) {

					if (saveCheckBox5.isSelected()) {
						moveLowerComponents(optionPanel2, 0, 100);
						setPanelVisibility(optionPanel2, true);
						updateBigPanel();

					} else {
						moveLowerComponents(optionPanel2, 0, -100);
						setPanelVisibility(optionPanel2, false);
						updateBigPanel();
					}
				}
			}
		});
		saveCheckBox5.setHorizontalAlignment(SwingConstants.LEFT);
		saveCheckBox5.setFont(new Font("Roboto", Font.PLAIN, 15));
		saveCheckBox5.setEnabled(false);
		saveCheckBox5.setBackground(Color.WHITE);
		saveCheckBox5.setBounds(30, 95, 400, 20);
		bigPanel.add(saveCheckBox5);

		optionPanel2 = new JPanel();
		optionPanel2.setOpaque(false);
		optionPanel2.setBounds(30, 115, 405, 100);
		optionPanel2.setBackground(new Color(238, 238, 238));
		bigPanel.add(optionPanel2);
		optionPanel2.setLayout(null);

		generateSaveOptionList(optionPanel2, saveOption2, 320, 5, true);
		generateChannelSelectionList(optionPanel2, colorSelect2, 10, 25);

		JLabel instructionLabel3 = new JLabel("<html> Select the display color for each motion track </html>");
		instructionLabel3.setVisible(false);
		instructionLabel3.setBounds(5, 5, 306, 18);
		instructionLabel3.setFont(new Font("Roboto", Font.PLAIN, 15));
		optionPanel2.add(instructionLabel3);

		// save motion flow checkbox

		saveCheckBox3 = new JCheckBox("<html> Motion field</html>");
		saveCheckBox3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				if (saveCheckBox3.isSelected()) {
					setPanelVisibility(optionPanel3, true);
					moveLowerComponents(optionPanel3, 0, 100);
					updateBigPanel();

				} else {
					setPanelVisibility(optionPanel3, false);
					moveLowerComponents(optionPanel3, 0, -100);
					updateBigPanel();
				}

			}
		});
		saveCheckBox3.setHorizontalAlignment(SwingConstants.LEFT);
		saveCheckBox3.setFont(new Font("Roboto", Font.PLAIN, 15));
		saveCheckBox3.setBackground(Color.WHITE);
		saveCheckBox3.setBounds(10, 125, 442, 20);
		bigPanel.add(saveCheckBox3);

		optionPanel3 = new JPanel();
		optionPanel3.setOpaque(false);
		optionPanel3.setBackground(new Color(238, 238, 238));
		optionPanel3.setBounds(10, 145, 425, 100);
		bigPanel.add(optionPanel3);
		optionPanel3.setLayout(null);

		JLabel saveAsLabel3 = new JLabel("Save as");
		saveAsLabel3.setFont(new Font("Roboto", Font.PLAIN, 15));
		saveAsLabel3.setBounds(5, 5, 80, 16);
		saveAsLabel3.setVisible(false);
		optionPanel3.add(saveAsLabel3);

		matButtonGroup3 = new JRadioButton(".mat");
		matButtonGroup3.setSelected(true);
		matButtonGroup3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				checkDefaultRadioButton(radioButtonGroup3);
			}
		});
		matButtonGroup3.setVisible(false);
		matButtonGroup3.setFont(new Font("Roboto", Font.PLAIN, 14));
		matButtonGroup3.setBounds(5, 20, 85, 18);
		optionPanel3.add(matButtonGroup3);

		tifButtonGroup3 = new JRadioButton(".tif");
		tifButtonGroup3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				checkDefaultRadioButton(radioButtonGroup3);
			}
		});
		tifButtonGroup3.setVisible(false);
		tifButtonGroup3.setFont(new Font("Roboto", Font.PLAIN, 14));
		tifButtonGroup3.setBounds(108, 20, 85, 18);
		optionPanel3.add(tifButtonGroup3);

		JLabel instructionLabel4 = new JLabel(
				"<html> Note: For large images saving the motion field takes up a lot of hard disk space. It is therefore not recommended to do so. The motion field is summarized by the superpixel tracks. </html>");
		instructionLabel4.setVisible(false);
		instructionLabel4.setFont(new Font("Roboto", Font.PLAIN, 14));
		instructionLabel4.setBounds(5, 40, 420, 54);
		optionPanel3.add(instructionLabel4);

		// save mesh

		saveCheckBox6 = new JCheckBox("<html>MOSES mesh (.mat)</html>");
		saveCheckBox6.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (saveCheckBox6.isSelected()) {
					saveCheckBox7.setEnabled(true);

					moveLowerComponents(optionPanel4, 0, 50);
					setPanelVisibility(optionPanel4, true);
					updateBigPanel();
				} else {
					saveCheckBox7.setEnabled(false);
					moveLowerComponents(optionPanel4, 0, -50);
					setPanelVisibility(optionPanel4, false);
					updateBigPanel();

					if (saveCheckBox7.isSelected()) {
						saveCheckBox7.setSelected(false);
						moveLowerComponents(optionPanel5, 0, -180);
						setPanelVisibility(optionPanel5, false);
						updateBigPanel();
					}

				}
			}
		});
		saveCheckBox6.setHorizontalAlignment(SwingConstants.LEFT);
		saveCheckBox6.setFont(new Font("Roboto", Font.PLAIN, 15));
		saveCheckBox6.setBackground(Color.WHITE);
		saveCheckBox6.setBounds(10, 175, 442, 20);
		bigPanel.add(saveCheckBox6);

		optionPanel4 = new JPanel();
		optionPanel4.setLayout(null);
		optionPanel4.setOpaque(false);
		optionPanel4.setBounds(10, 195, 425, 50);
		bigPanel.add(optionPanel4);

		instructionLabel5 = new JLabel(
				"<html> Please set the distace threshold used for computing the MOSES mesh  </html>");
		instructionLabel5.setVisible(false);
		instructionLabel5.setVerticalAlignment(SwingConstants.TOP);
		instructionLabel5.setFont(new Font("Roboto", Font.PLAIN, 15));
		instructionLabel5.setBounds(10, 5, 415, 36);
		optionPanel4.add(instructionLabel5);

		DecimalFormat decimalFormat = new DecimalFormat("#.##");
		decimalFormat.setGroupingUsed(false);
		DecimalFormat integerFormat = new DecimalFormat("###");
		decimalFormat.setGroupingUsed(false);

		distanceTresholdField = new JFormattedTextField(decimalFormat) {
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
		distanceTresholdField.setText("1.2");
		distanceTresholdField.setVisible(false);
		distanceTresholdField.setHorizontalAlignment(SwingConstants.CENTER);
		distanceTresholdField.setFont(new Font("Roboto", Font.PLAIN, 15));
		distanceTresholdField.setBounds(110, 24, 100, 22);
		optionPanel4.add(distanceTresholdField);

		saveCheckBox7 = new JCheckBox("Visualize MOSES mesh");
		saveCheckBox7.setEnabled(false);
		saveCheckBox7.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (saveCheckBox7.isEnabled()) {

					if (saveCheckBox7.isSelected()) {
						moveLowerComponents(optionPanel5, 0, 180);
						setPanelVisibility(optionPanel5, true);
						updateBigPanel();

					} else {
						moveLowerComponents(optionPanel5, 0, -180);
						setPanelVisibility(optionPanel5, false);
						updateBigPanel();
					}
				}
			}
		});
		saveCheckBox7.setBackground(Color.WHITE);
		saveCheckBox7.setFont(new Font("Roboto", Font.PLAIN, 15));
		saveCheckBox7.setBounds(30, 195, 196, 24);
		bigPanel.add(saveCheckBox7);
		;

		// buttons

		// cancel button

		JButton cancelButton = new JButton("Cancel");
		cancelButton.setBounds(10, 430, 140, 30);
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
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
		add(cancelButton);

		// back button

		JButton backButton = new JButton("Back");
		backButton.setBounds(200, 430, 140, 30);
		backButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// display menuPanel and close current panel

				parentFrame.empty();
				parentFrame.computeTracksPanel2 = new ComputeTracksPanel2(parentFrame);
				parentFrame.getContentPane().add(parentFrame.computeTracksPanel2);
				parentFrame.validate();
			}
		});
		backButton.setVerticalTextPosition(SwingConstants.CENTER);
		backButton.setHorizontalTextPosition(SwingConstants.CENTER);
		backButton.setForeground(Color.WHITE);
		backButton.setFont(new Font("Arial", Font.BOLD, 15));
		backButton.setBackground(new Color(13, 59, 102));
		add(backButton);

		// finish button

		JButton nextButton = new JButton("Finish");
		nextButton.setBounds(350, 430, 140, 30);
		nextButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				// load file chooser if any checkbox is selected

				if (saveCheckBox1.isEnabled() || saveCheckBox3.isEnabled() || saveCheckBox4.isEnabled()) {
					Globals.saveDirectory = IJ.getDirectory("Choose saving directory");
					// IJ.log(Globals.saveDirectory);
				}

				// loading bar

				setPanelVisibility(loadingBarPanel, true);
				updateBigPanel();
				loadingBar.setIndeterminate(true);
				loadingBarLabel.setText(
						"<html> Please wait, this may take a couple of minutes. <br> Processing parameters...  </html>");

				// execute script on different thread

				class MyWorker extends SwingWorker<String, String> {
					int fileCount = 1, fileNumber = 1;

					@Override
					protected String doInBackground() {
						if (Globals.batchMode) {

							ArrayList<String> validExtensions = new ArrayList<String>();
							validExtensions.addAll(Arrays.asList(".tif", ".tiff"));

							File fileDirectory = new File(Globals.getDirectory(Globals.filePath));
							IJ.log("Dir: " + Globals.getDirectory(Globals.filePath));
							String[] files = fileDirectory.list();
							for (String fileTitle : files) {
								File selectedFile = new File(fileDirectory.getPath(), fileTitle);
								IJ.log(selectedFile.getAbsolutePath());

								if (selectedFile.isFile()
										&& Globals.checkExtension(selectedFile.getAbsolutePath(), validExtensions))
									fileCount++;
							}
							fileCount--;

							for (String fileTitle : files) {
								File selectedFile = new File(fileDirectory.getPath(), fileTitle);

								if (selectedFile.isFile()
										&& Globals.checkExtension(selectedFile.getAbsolutePath(), validExtensions)) {
									analyseFile(selectedFile.getAbsolutePath());
									fileNumber++;
								}
							}
						} else
							analyseFile(Globals.filePath);

						return "Done";

					}

					public void analyseFile(String filePath) {
						// create new file

						String temporaryFilePath = System.getProperty("java.io.tmpdir") + "MOSESimage.tif";

						ImagePlus temporaryImage = IJ.openImage(filePath);
						IJ.run(temporaryImage, "Size...",
								"width=" + (int) (Globals.width / Math.sqrt(Globals.downsizeFactor)) + " height="
										+ (int) (Globals.height / Math.sqrt(Globals.downsizeFactor)) + " depth="
										+ Globals.frames + " constrain average interpolation=Bilinear");
						IJ.saveAs(temporaryImage, "Tiff", temporaryFilePath);
						temporaryImage.close();

						temporaryImageFile = new File(temporaryFilePath);

						//

						forwardTracksImageSequenceFolderPath = Globals.saveDirectory
								+ Globals.getNameWithoutExtension(filePath) + "_forward_tracks_image_sequence";

						backwardTracksImageSequenceFolderPath = Globals.saveDirectory
								+ Globals.getNameWithoutExtension(filePath) + "_backward_tracks_image_sequence";

						motionFieldImageSequenceFolderPath = Globals.saveDirectory
								+ Globals.getNameWithoutExtension(filePath) + "_motion_field_image_sequence";

						MOSESMeshImageSequenceFolderPath = Globals.saveDirectory
								+ Globals.getNameWithoutExtension(filePath) + "_MOSES_mesh_image_sequence";

						ArrayList<String> command = new ArrayList<>();
						command.addAll(Arrays.asList("python", Globals.scriptPath, temporaryFilePath,
								String.valueOf(Globals.pyr_scale), String.valueOf(Globals.levels),
								String.valueOf(Globals.winSize), String.valueOf(Globals.iterations),
								String.valueOf(Globals.polyn), String.valueOf(Globals.polysigma),
								String.valueOf(Globals.flags), String.valueOf(Globals.numberSuperpixels),
								String.valueOf(Globals.frames),
								String.valueOf((int) (Globals.width / Math.sqrt(Globals.downsizeFactor))),
								String.valueOf((int) (Globals.height / Math.sqrt(Globals.downsizeFactor))),
								Globals.saveDirectory, Globals.getNameWithoutExtension(filePath),
								String.valueOf(Globals.numberSelectedChannels), distanceTresholdField.getText(),
								frameField.getText()));

						String pythonScriptHeader = "import scipy.io as spio\r\n" + "import os\r\n" + "import sys\r\n"
								+ "from MOSES.Utility_Functions.file_io import read_multiimg_PIL\r\n"
								+ "from MOSES.Utility_Functions.file_io import mkdir\r\n"
								+ "from MOSES.Visualisation_Tools.track_plotting import plot_tracks\r\n"
								+ "from MOSES.Optical_Flow_Tracking.superpixel_track import compute_grayscale_vid_superpixel_tracks_FB  \r\n"
								+ "import pylab as plt\r\n"
								+ "from MOSES.Optical_Flow_Tracking.superpixel_track import compute_grayscale_vid_superpixel_tracks\r\n"
								+ "from MOSES.Visualisation_Tools.motion_field_visualisation import view_ang_flow \r\n"
								+ "from MOSES.Motion_Analysis.mesh_statistics_tools import construct_MOSES_mesh\r\n"
								+ "from MOSES.Visualisation_Tools.mesh_visualisation import visualise_mesh\r\n"
								+ "from MOSES.Motion_Analysis.mesh_statistics_tools import from_neighbor_list_to_graph\r\n"
								+ "infile = sys.argv[1]\r\n" + "vidstack = read_multiimg_PIL(infile)\r\n"
								+ "optical_flow_params = dict(pyr_scale = float(sys.argv[2]), levels = int(sys.argv[3]), winsize = int(sys.argv[4]), iterations = int(sys.argv[5]), poly_n = int(sys.argv[6]), poly_sigma = float(sys.argv[7]), flags = int(sys.argv[8]))\r\n"
								+ "n_spixels = int(sys.argv[9])\r\n" + "print(\"Finished set up\")\r\n";

						String pythonScript = null;

						// forward tracks

						if (saveCheckBox1.isSelected()) {

							pythonScript = pythonScriptHeader;

							for (int i = 0; i < Globals.selectedChannels.size(); i++) {
								int channelIndex = Globals.selectedChannels.get(i);

								pythonScript += "optflow_" + channelIndex + ", forward_tracks_" + channelIndex
										+ " = compute_grayscale_vid_superpixel_tracks(vidstack[:,:,:," + channelIndex
										+ "], optical_flow_params, n_spixels)\r\n"
										+ "print(\"Finished extracting superpixel tracks for channel " + channelIndex
										+ " \")\r\n";

							}

							pythonScript += "savetracksmat = (sys.argv[13] + sys.argv[14] + '_forward_tracks.mat')\r\n"
									+ "spio.savemat(savetracksmat, {";

							boolean firstchannel = true;
							for (int i = 0; i < Globals.selectedChannels.size(); i++) {
								int channelIndex = Globals.selectedChannels.get(i);

								if (!firstchannel) {
									pythonScript += ", ";
								} else {
									firstchannel = false;
								}

								pythonScript += "'forward_tracks_" + channelIndex + "': forward_tracks_" + channelIndex
										+ "";

							}

							pythonScript += "})\r\n" + "print(\"Saved tracks\")\r\n";

							if (saveCheckBox2.isSelected()) {

								forwardTracksImageSequenceFolder = new File(forwardTracksImageSequenceFolderPath);
								forwardTracksImageSequenceFolder.mkdirs();

								pythonScript += "save_frame_folder = sys.argv[13] + sys.argv[14] + '_forward_tracks_image_sequence' \r\n"
										+ "mkdir(save_frame_folder)\r\n" + "len_segment = 5\r\n"
										+ "for frame in range(len_segment, int(sys.argv[10]), 1):\r\n"
										+ "    frame_img = vidstack[frame]\r\n" + "    fig = plt.figure()\r\n"
										+ "    fig.set_size_inches(int(sys.argv[11]) / int(sys.argv[12]), 1, forward=False)\r\n"
										+ "    ax=plt.Axes(fig, [0.,0.,1.,1.]); ax.set_axis_off(); fig.add_axes(ax)\r\n"
										+ "    ax.imshow(frame_img, alpha=0.6)\r\n";

								for (int i = 0; i < Globals.selectedChannels.size(); i++) {
									int channelIndex = Globals.selectedChannels.get(i);

									pythonScript += "    plot_tracks(forward_tracks_" + channelIndex
											+ "[:,frame-len_segment:frame+1],ax, color='"
											+ Globals.color(colorSelect1.get(i).getL().getSelectedIndex())
											+ "', lw=1)\r\n";

								}

								pythonScript += "    ax.set_xlim([0, int(sys.argv[11])]); ax.set_ylim([int(sys.argv[12]), 0])\r\n"
										+ "    ax.grid('off'); ax.axis('off')\r\n"
										+ "    fig.savefig(os.path.join(save_frame_folder, 'tracksimg-%s_' %(str(frame+1).zfill(3)) + sys.argv[14] + '.png'), dpi=int(sys.argv[12]))\r\n"
										+ "    plt.close(fig)\r\n";

							}

							publish("Computing forward tracks...");
							Thread.yield();

							Globals.runScript(pythonScript, command);

							if (saveCheckBox2.isSelected()) {
								// save .tif
								publish("Generating tiff stack from forward tracks...");
								Thread.yield();

								if (saveOption1.getL().get(0).isSelected()) {
									ImagePlus imp = FolderOpener
											.open(forwardTracksImageSequenceFolder.getAbsolutePath(), "");
									imp.show();
									IJ.saveAs(imp, "Tiff", Globals.saveDirectory
											+ Globals.getNameWithoutExtension(filePath) + "_forward_tracks_video.tif");
								}

								// save .avi
								publish("Generating avi video from forward tracks...");
								Thread.yield();
								if (saveOption1.getL().get(1).isSelected()) {

									ImagePlus imp = FolderOpener
											.open(forwardTracksImageSequenceFolder.getAbsolutePath(), "");
									IJ.run(imp, "AVI... ", "compression=JPEG frame=7 save=" + Globals.saveDirectory
											+ Globals.getNameWithoutExtension(filePath) + "_forward_tracks_video.avi");
								}

								// delete image sequence folder
								publish("Deleting temporary files...");
								Thread.yield();
								if (!saveOption1.getL().get(2).isSelected()) {
									String[] entries = forwardTracksImageSequenceFolder.list();
									for (String fileName : entries) {
										File currentFile = new File(forwardTracksImageSequenceFolder.getPath(),
												fileName);
										currentFile.delete();
									}

									forwardTracksImageSequenceFolder.delete();
								}
							}

						}

						// backward tracks

						if (saveCheckBox4.isSelected()) {
							pythonScript = pythonScriptHeader;

							for (int i = 0; i < Globals.selectedChannels.size(); i++) {
								int channelIndex = Globals.selectedChannels.get(i);

								pythonScript += "optflow_" + channelIndex + ", forward_tracks_" + channelIndex
										+ ", backward_tracks_" + channelIndex
										+ " = compute_grayscale_vid_superpixel_tracks_FB(vidstack[:,:,:," + channelIndex
										+ "], optical_flow_params, n_spixels=n_spixels)\r\n";

							}

							pythonScript += "savetracksmat2 = (sys.argv[13] + sys.argv[14] + '_backward_tracks.mat') \r\n"
									+ "spio.savemat(savetracksmat2, {";

							boolean firstchannel = true;
							for (int i = 0; i < Globals.selectedChannels.size(); i++) {
								int channelIndex = Globals.selectedChannels.get(i);

								if (!firstchannel) {
									pythonScript += ", ";
								} else {
									firstchannel = false;
								}

								pythonScript += "'backward_tracks_" + channelIndex + "': backward_tracks_"
										+ channelIndex + "";

							}

							pythonScript += "})\r\n" + "print(\"Saved tracks\")\r\n";

							if (saveCheckBox5.isSelected()) {

								backwardTracksImageSequenceFolder = new File(backwardTracksImageSequenceFolderPath);
								backwardTracksImageSequenceFolder.mkdirs();

								pythonScript += "save_frame_folder = sys.argv[13] + sys.argv[14] + '_backward_tracks_image_sequence' \r\n"
										+ "mkdir(save_frame_folder)\r\n" + "len_segment = 5\r\n"
										+ "for frame in range(len_segment, int(sys.argv[10]), 1):\r\n"
										+ "    frame_img = vidstack[frame]\r\n" + "    fig = plt.figure()\r\n"
										+ "    fig.set_size_inches(int(sys.argv[11]) / int(sys.argv[12]), 1, forward=False)\r\n"
										+ "    ax=plt.Axes(fig, [0.,0.,1.,1.]); ax.set_axis_off(); fig.add_axes(ax)\r\n"
										+ "    ax.imshow(frame_img, alpha=0.6)\r\n";

								for (int i = 0; i < Globals.selectedChannels.size(); i++) {
									int channelIndex = Globals.selectedChannels.get(i);

									pythonScript += "    plot_tracks(backward_tracks_" + channelIndex
											+ "[:,frame-len_segment:frame+1],ax, color='"
											+ Globals.color(colorSelect2.get(i).getL().getSelectedIndex())
											+ "', lw=1)\r\n";

								}

								pythonScript += "    ax.set_xlim([0, int(sys.argv[11])]); ax.set_ylim([int(sys.argv[12]), 0])\r\n"
										+ "    ax.grid('off'); ax.axis('off')\r\n"
										+ "    fig.savefig(os.path.join(save_frame_folder, 'tracksimg-%s_' %(str(frame+1).zfill(3)) + sys.argv[14] + '.png'), dpi=int(sys.argv[12]))\r\n"
										+ "    plt.close(fig)\r\n";
							}

							publish("Computing backward tracks...");
							Thread.yield();

							Globals.runScript(pythonScript, command);

							if (saveCheckBox5.isSelected()) {
								// save .tif
								publish("Generating tiff stack from backward tracks...");
								Thread.yield();

								if (saveOption2.getL().get(0).isSelected()) {
									ImagePlus imp = FolderOpener
											.open(backwardTracksImageSequenceFolder.getAbsolutePath(), "");
									imp.show();
									IJ.saveAs(imp, "Tiff", Globals.saveDirectory
											+ Globals.getNameWithoutExtension(filePath) + "_backward_tracks_video.tif");
								}

								// save .avi
								publish("Generating avi video from backward tracks...");
								Thread.yield();
								if (saveOption2.getL().get(1).isSelected()) {
									ImagePlus imp = FolderOpener
											.open(backwardTracksImageSequenceFolder.getAbsolutePath(), "");
									IJ.run(imp, "AVI... ", "compression=JPEG frame=7 save=" + Globals.saveDirectory
											+ Globals.getNameWithoutExtension(filePath) + "_backward_tracks_video.avi");
								}

								// delete image sequence folder
								publish("Deleting temporary files...");
								Thread.yield();
								if (!saveOption2.getL().get(2).isSelected()) {
									String[] entries = backwardTracksImageSequenceFolder.list();
									for (String fileName : entries) {
										File currentFile = new File(backwardTracksImageSequenceFolder.getPath(),
												fileName);
										currentFile.delete();
									}

									backwardTracksImageSequenceFolder.delete();
								}
							}
						}

						// motion flow

						if (saveCheckBox3.isSelected()) {

							pythonScript = pythonScriptHeader;

							for (int i = 0; i < Globals.selectedChannels.size(); i++) {
								int channelIndex = Globals.selectedChannels.get(i);

								pythonScript += "optflow_" + channelIndex + ", forward_tracks_" + channelIndex
										+ " = compute_grayscale_vid_superpixel_tracks(vidstack[:,:,:," + channelIndex
										+ "], optical_flow_params, n_spixels)\r\n"
										+ "print(\"Finished extracting superpixel tracks for channel " + channelIndex
										+ " \")\r\n";

							}

							if (matButtonGroup3.isSelected()) {

								pythonScript += "save_optflow_mat = (sys.argv[13] + sys.argv[14] + '_optflow.mat')\r\n"
										+ "spio.savemat(save_optflow_mat, {";

								boolean firstchannel = true;
								for (int i = 0; i < Globals.selectedChannels.size(); i++) {
									int channelIndex = Globals.selectedChannels.get(i);

									if (!firstchannel) {
										pythonScript += ", ";
									} else {
										firstchannel = false;
									}

									pythonScript += "'optflow_" + channelIndex + "': optflow_" + channelIndex + "";

								}

								pythonScript += "})\r\n";
							}

							if (tifButtonGroup3.isSelected()) {

								motionFieldImageSequenceFolder = new File(motionFieldImageSequenceFolderPath);
								motionFieldImageSequenceFolder.mkdirs();

								pythonScript += "save_frame_folder = sys.argv[13] + sys.argv[14] + '_motion_field_image_sequence' \r\n"
										+ "mkdir(save_frame_folder)\r\n"
										+ "for frame in range(1, int(sys.argv[10]), 1):\r\n"
										+ "    fig, ax = plt.subplots(nrows = 1, ncols = int(sys.argv[15]))\r\n";

								for (int i = 0; i < Globals.selectedChannels.size(); i++) {
									int channelIndex = Globals.selectedChannels.get(i);

									if (Globals.numberSelectedChannels > 1) {

										pythonScript += "    ax[" + i + "].imshow(view_ang_flow(optflow_" + channelIndex
												+ "[frame])) \r\n" + "    ax[" + i + "].grid('off')\r\n";

									} else {

										pythonScript += "    ax.imshow(view_ang_flow(optflow_" + channelIndex
												+ "[frame])) \r\n" + "    ax.grid('off')\r\n";

									}
								}

								pythonScript += "    fig.savefig(os.path.join(save_frame_folder, 'motionfield-%s_' %(str(frame).zfill(3)) + sys.argv[14] + '.png'), dpi=int(sys.argv[12]))\r\n"
										+ "    plt.close()\r\n";
							}

							publish("Generating motion field...");
							Thread.yield();

							Globals.runScript(pythonScript, command);

							if (saveCheckBox3.isSelected()) {
								// make tiff stack from image sequence and delete the folder
								publish("Generating tiff stack from motion field...");
								Thread.yield();

								if (tifButtonGroup3.isSelected()) {
									ImagePlus imp = FolderOpener.open(motionFieldImageSequenceFolder.getAbsolutePath(),
											"");
									imp.show();
									IJ.saveAs(imp, "Tiff", Globals.saveDirectory
											+ Globals.getNameWithoutExtension(filePath) + "_motion_field_video.tif");

								}

								publish("Deleting temporary files...");
								Thread.yield();

								String[] entries = motionFieldImageSequenceFolder.list();
								for (String fileName : entries) {
									File currentFile = new File(motionFieldImageSequenceFolder.getPath(), fileName);
									currentFile.delete();
								}

								motionFieldImageSequenceFolder.delete();
							}
						}

						// MOSES mesh

						if (saveCheckBox6.isSelected()) {
							pythonScript = pythonScriptHeader;

							for (int i = 0; i < Globals.selectedChannels.size(); i++) {
								int channelIndex = Globals.selectedChannels.get(i);

								pythonScript += "optflow_" + channelIndex + ", forward_tracks_" + channelIndex
										+ " = compute_grayscale_vid_superpixel_tracks(vidstack[:,:,:," + channelIndex
										+ "], optical_flow_params, n_spixels)\r\n"
										+ "print(\"Finished extracting superpixel tracks for channel " + channelIndex
										+ " \")\r\n";
							}

							pythonScript += "spixel_size = forward_tracks_" + Globals.selectedChannels.get(0)
									+ "[1,0,1] - forward_tracks_" + Globals.selectedChannels.get(0) + "[1,0,0]\r\n";

							for (int i = 0; i < Globals.selectedChannels.size(); i++) {
								int channelIndex = Globals.selectedChannels.get(i);

								pythonScript += "MOSES_mesh_strain_time_" + channelIndex + ", MOSES_mesh_neighborlist_"
										+ channelIndex + " = construct_MOSES_mesh(forward_tracks_" + channelIndex
										+ ", dist_thresh= float(sys.argv[16]), spixel_size=spixel_size)\r\n";
							}

							pythonScript += "savetracksmat = (sys.argv[13] + sys.argv[14] + '_MOSES_mesh.mat')\r\n"
									+ "spio.savemat(savetracksmat, {";

							boolean firstchannel = true;
							for (int i = 0; i < Globals.selectedChannels.size(); i++) {
								int channelIndex = Globals.selectedChannels.get(i);

								if (!firstchannel) {
									pythonScript += ", ";
								} else {
									firstchannel = false;
								}

								pythonScript += "'MOSES_mesh_neighborlist_" + channelIndex
										+ "': MOSES_mesh_neighborlist_" + channelIndex + "";

							}

							pythonScript += "})\r\n";

							if (saveCheckBox7.isSelected()) {

								if (saveCheckBox71.isSelected()) {

									for (int i = 0; i < Globals.selectedChannels.size(); i++) {
										int channelIndex = Globals.selectedChannels.get(i);

										pythonScript += "for i in range(len(MOSES_mesh_neighborlist_" + channelIndex
												+ ")):\r\n" + "    MOSES_mesh_neighborlist_" + channelIndex
												+ "[i] = MOSES_mesh_neighborlist_" + channelIndex
												+ "[i].astype(int)\r\n" + "mesh_frame_networkx_G_" + channelIndex
												+ " = from_neighbor_list_to_graph(forward_tracks_" + channelIndex
												+ ", MOSES_mesh_neighborlist_" + channelIndex
												+ ", int(sys.argv[17]))\r\n";

									}

									pythonScript += "fig, ax = plt.subplots(nrows=1, ncols="
											+ Globals.numberSelectedChannels + ")\r\n";

									if (Globals.numberSelectedChannels > 1) {
										for (int i = 0; i < Globals.selectedChannels.size(); i++) {
											int channelIndex = Globals.selectedChannels.get(i);

											pythonScript += "ax[" + i
													+ "].imshow(vidstack[int(sys.argv[17])], alpha=0.7)\r\n"
													+ "visualise_mesh(mesh_frame_networkx_G_" + channelIndex
													+ ", forward_tracks_" + channelIndex
													+ "[:,int(sys.argv[17]),[1,0]], ax[" + i
													+ "], node_size=spixel_size/10, linewidths=1, width=1, node_color='"
													+ Globals.color(colorSelect3.get(i).getL().getSelectedIndex())
													+ "')\r\n" + "ax[" + i + "].set_ylim([int(sys.argv[12]),0])\r\n"
													+ "ax[" + i + "].set_xlim([0,int(sys.argv[11])])\r\n" + "ax[" + i
													+ "].grid('off'); ax[" + i + "].axis('off')\r\n";
										}
									} else {
										pythonScript += "ax.imshow(vidstack[int(sys.argv[17])], alpha=0.7)\r\n"
												+ "visualise_mesh(mesh_frame_networkx_G_"
												+ Globals.selectedChannels.get(0) + ", forward_tracks_"
												+ Globals.selectedChannels.get(0)
												+ "[:,int(sys.argv[17]),[1,0]], ax, node_size=spixel_size/10, linewidths=1, width=1, node_color='"
												+ Globals.color(colorSelect3.get(0).getL().getSelectedIndex())
												+ "')\r\n" + "ax.set_ylim([int(sys.argv[12]),0])\r\n"
												+ "ax.set_xlim([0,int(sys.argv[11])])\r\n"
												+ "ax.grid('off'); ax.axis('off')\r\n";
									}

									pythonScript += "fig.savefig(os.path.join(sys.argv[13], 'MOSES_mesh_' + sys.argv[14] + '.png'), dpi=int(sys.argv[12]))\r\n"
											+ "plt.close()\r\n";
								}

								if (saveCheckBox72.isSelected()) {

									MOSESMeshImageSequenceFolder = new File(MOSESMeshImageSequenceFolderPath);
									MOSESMeshImageSequenceFolder.mkdirs();

									pythonScript += "save_frame_folder = sys.argv[13] + sys.argv[14] + '_MOSES_mesh_image_sequence' \r\n"
											+ "mkdir(save_frame_folder)\r\n";

									pythonScript += "for frame in range(1, int(sys.argv[10]), 1):\r\n"
											+ "	fig, ax = plt.subplots(nrows=1, ncols=" + Globals.numberSelectedChannels
											+ ")\r\n";
									;

									for (int i = 0; i < Globals.selectedChannels.size(); i++) {
										int channelIndex = Globals.selectedChannels.get(i);

										pythonScript += "	for i in range(len(MOSES_mesh_neighborlist_" + channelIndex
												+ ")):\r\n" + "		MOSES_mesh_neighborlist_" + channelIndex
												+ "[i] = MOSES_mesh_neighborlist_" + channelIndex
												+ "[i].astype(int)\r\n" + "	mesh_frame_networkx_G_" + channelIndex
												+ " = from_neighbor_list_to_graph(forward_tracks_" + channelIndex
												+ ", MOSES_mesh_neighborlist_" + channelIndex + ", frame)\r\n";

									}

									if (Globals.numberSelectedChannels > 1) {
										for (int i = 0; i < Globals.selectedChannels.size(); i++) {
											int channelIndex = Globals.selectedChannels.get(i);

											pythonScript += "	ax[" + i + "].imshow(vidstack[frame], alpha=0.7)\r\n"
													+ "	visualise_mesh(mesh_frame_networkx_G_" + channelIndex
													+ ", forward_tracks_" + channelIndex + "[:,frame,[1,0]], ax[" + i
													+ "], node_size=spixel_size/10, linewidths=1, width=1, node_color='"
													+ Globals.color(colorSelect3.get(i).getL().getSelectedIndex())
													+ "')\r\n" + "	ax[" + i + "].set_ylim([int(sys.argv[12]),0])\r\n"
													+ "	ax[" + i + "].set_xlim([0,int(sys.argv[11])])\r\n" + "	ax[" + i
													+ "].grid('off'); ax[" + i + "].axis('off')\r\n";
										}
									} else {
										pythonScript += "	ax.imshow(vidstack[frame], alpha=0.7)\r\n"
												+ "	visualise_mesh(mesh_frame_networkx_G_"
												+ Globals.selectedChannels.get(0) + ", forward_tracks_"
												+ Globals.selectedChannels.get(0)
												+ "[:,frame,[1,0]], ax, node_size=spixel_size/10, linewidths=1, width=1, node_color='"
												+ Globals.color(colorSelect3.get(0).getL().getSelectedIndex())
												+ "')\r\n" + "	ax.set_ylim([int(sys.argv[12]),0])\r\n"
												+ "	ax.set_xlim([0,int(sys.argv[11])])\r\n"
												+ "	ax.grid('off'); ax.axis('off')\r\n";
									}

									pythonScript += "	fig.savefig(os.path.join(save_frame_folder, 'MOSES_mesh-%s_' %(str(frame).zfill(3)) + sys.argv[14] + '.png'), dpi=int(sys.argv[12]))\r\n"
											+ "	plt.close()\r\n";

								}
							}

							publish("Generating MOSES mesh...");
							Thread.yield();

							Globals.runScript(pythonScript, command);

							if (saveCheckBox72.isSelected()) {
								// save .tif
								publish("Generating tiff stack from MOSES mesh...");
								Thread.yield();

								if (saveOption3.getL().get(0).isSelected()) {
									ImagePlus imp = FolderOpener.open(MOSESMeshImageSequenceFolder.getAbsolutePath(),
											"");
									imp.show();
									IJ.saveAs(imp, "Tiff", Globals.saveDirectory
											+ Globals.getNameWithoutExtension(filePath) + "_MOSES_mesh_video.tif");
								}

								// save .avi
								publish("Generating avi video from MOSES mesh...");
								Thread.yield();
								if (saveOption3.getL().get(1).isSelected()) {
									ImagePlus imp = FolderOpener.open(MOSESMeshImageSequenceFolder.getAbsolutePath(),
											"");
									IJ.run(imp, "AVI... ", "compression=JPEG frame=7 save=" + Globals.saveDirectory
											+ Globals.getNameWithoutExtension(filePath) + "_MOSES_mesh_video.avi");
								}

								// delete image sequence folder
								publish("Deleting temporary files...");
								Thread.yield();
								if (!saveOption3.getL().get(2).isSelected()) {
									String[] entries = MOSESMeshImageSequenceFolder.list();
									for (String fileName : entries) {
										File currentFile = new File(MOSESMeshImageSequenceFolder.getPath(), fileName);
										currentFile.delete();
									}

									MOSESMeshImageSequenceFolder.delete();
								}
							}

						}

						temporaryImageFile.delete();

					}

					@Override
					protected void process(List<String> messages) {
						for (String m : messages) {
							loadingBarLabel.setText(
									"<html> Please wait, this may take a couple of minutes. <br> " + m + "  </html>");
							loadingBarLabel.validate();
							loadingBarLabel.repaint();

							fileLabel.setText("File " + fileNumber + " out of " + fileCount);
							fileLabel.validate();
							fileLabel.repaint();
						}

					}

					@Override
					protected void done() {
						setPanelVisibility(loadingBarPanel, false);
						updateBigPanel();

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
		add(nextButton);
		radioButtonGroup3.addAll(Arrays.asList(matButtonGroup3, tifButtonGroup3));

		tracksTitleLabel = new JLabel("<html> Superpixel motion tracks</html>");
		tracksTitleLabel.setHorizontalAlignment(SwingConstants.CENTER);
		tracksTitleLabel.setForeground(Color.DARK_GRAY);
		tracksTitleLabel.setFont(new Font("Roboto", Font.BOLD, 17));
		tracksTitleLabel.setBounds(0, 0, 460, 23);
		bigPanel.add(tracksTitleLabel);

		meshTitleLabel = new JLabel("<html> Mesh from motion tracks </html>");
		meshTitleLabel.setHorizontalAlignment(SwingConstants.CENTER);
		meshTitleLabel.setForeground(Color.DARK_GRAY);
		meshTitleLabel.setFont(new Font("Roboto", Font.BOLD, 17));
		meshTitleLabel.setBounds(0, 150, 460, 23);
		bigPanel.add(meshTitleLabel);

		// save mesh vis

		optionPanel5 = new JPanel();
		optionPanel5.setBounds(30, 215, 425, 180);
		bigPanel.add(optionPanel5);
		optionPanel5.setOpaque(false);
		optionPanel5.setLayout(null);

		saveCheckBox71 = new JCheckBox("Create visualization for a selected frame (.png)");
		saveCheckBox71.setVisible(false);
		saveCheckBox71.setFont(new Font("Roboto", Font.PLAIN, 15));
		saveCheckBox71.setBounds(5, 80, 401, 18);
		optionPanel5.add(saveCheckBox71);

		frameLabel = new JLabel("Frame:");
		frameLabel.setVisible(false);
		frameLabel.setFont(new Font("Roboto", Font.PLAIN, 15));
		frameLabel.setBounds(45, 100, 55, 20);
		optionPanel5.add(frameLabel);

		frameField = new JFormattedTextField(integerFormat) {

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
		frameField.setText("5");
		frameField.setHorizontalAlignment(SwingConstants.CENTER);
		frameField.setVisible(false);
		frameField.setFont(new Font("Roboto", Font.PLAIN, 15));
		frameField.setBounds(100, 100, 100, 20);
		optionPanel5.add(frameField);

		saveCheckBox72 = new JCheckBox("Create visualization for all frames");
		saveCheckBox72.setVisible(false);
		saveCheckBox72.setFont(new Font("Roboto", Font.PLAIN, 15));
		saveCheckBox72.setBounds(5, 120, 261, 18);
		optionPanel5.add(saveCheckBox72);

		JLabel lblSelectTheDisplay = new JLabel("<html> Select the display color for the mesh nodes </html>");
		lblSelectTheDisplay.setVisible(false);
		lblSelectTheDisplay.setFont(new Font("Roboto", Font.PLAIN, 15));
		lblSelectTheDisplay.setBounds(5, 5, 335, 20);
		optionPanel5.add(lblSelectTheDisplay);

		generateSaveOptionList(optionPanel5, saveOption3, 10, 140, false);
		generateChannelSelectionList(optionPanel5, colorSelect3, 5, 25);

	}

	public void generateChannelSelectionList(JPanel panel, ArrayList<Pair<JComboBox, JLabel>> colorSelect, int x,
			int y) {
		colorSelect.clear();

		for (int i = 0; i < Globals.selectedChannels.size(); i++) {
			int channelIndex = Globals.selectedChannels.get(i);

			JComboBox comboBox = new JComboBox();
			comboBox.setModel(new DefaultComboBoxModel(
					new String[] { "red", "blue", "green", "cyan", "magenta", "yellow", "black", "white" }));
			comboBox.setBounds(x + 110, y + i * 25, 100, 20);
			comboBox.setFont(new Font("Roboto", Font.PLAIN, 15));
			comboBox.setSelectedIndex(i);
			comboBox.setVisible(false);

			JLabel label = new JLabel("Channel " + (channelIndex + 1) + " color: ");
			label.setVerticalAlignment(SwingConstants.TOP);
			label.setHorizontalAlignment(SwingConstants.LEFT);
			label.setFont(new Font("Roboto", Font.PLAIN, 15));
			label.setBounds(x, y + i * 25, 180, 20);
			label.setForeground(Color.DARK_GRAY);
			label.setVisible(false);

			panel.add(comboBox);
			panel.add(label);

			colorSelect.add(new Pair(comboBox, label));
		}
	}

	public void generateSaveOptionList(JPanel panel, Pair<ArrayList<JRadioButton>, JLabel> saveOption, int x, int y,
			boolean vertical) {
		JLabel saveAsLabel;
		JRadioButton tifButton, aviButton, imageSequenceButton;

		// 315 5
		saveAsLabel = new JLabel("Save as:");
		saveAsLabel.setVisible(false);
		saveAsLabel.setFont(new Font("Roboto", Font.PLAIN, 15));
		saveAsLabel.setVerticalAlignment(SwingConstants.CENTER);
		if (vertical)
			saveAsLabel.setBounds(x, y, 85, 18);
		else
			saveAsLabel.setBounds(x, y, 85, 40);
		panel.add(saveAsLabel);

		tifButton = new JRadioButton(".tif");
		tifButton.setSelected(true);
		tifButton.setVisible(false);
		tifButton.setVerticalAlignment(SwingConstants.CENTER);
		tifButton.setFont(new Font("Roboto", Font.PLAIN, 14));
		if (vertical)
			tifButton.setBounds(x, y + 15, 85, 18);
		else
			tifButton.setBounds(x + 60, y, 55, 40);
		panel.add(tifButton);

		aviButton = new JRadioButton(".avi");
		aviButton.setVisible(false);
		aviButton.setVerticalAlignment(SwingConstants.CENTER);
		aviButton.setFont(new Font("Roboto", Font.PLAIN, 14));
		if (vertical)
			aviButton.setBounds(x, y + 30, 85, 18);
		else
			aviButton.setBounds(x + 120, y, 55, 40);
		panel.add(aviButton);

		imageSequenceButton = new JRadioButton("<html> image sequence </html>");
		imageSequenceButton.setVisible(false);
		imageSequenceButton.setHorizontalAlignment(SwingConstants.LEFT);
		imageSequenceButton.setVerticalAlignment(SwingConstants.CENTER);
		imageSequenceButton.setFont(new Font("Roboto", Font.PLAIN, 14));
		if (vertical)
			imageSequenceButton.setBounds(x, y + 45, 85, 40);
		else
			imageSequenceButton.setBounds(x + 180, y, 85, 40);
		panel.add(imageSequenceButton);

		ArrayList<JRadioButton> radioButtonGroup = new ArrayList<JRadioButton>();
		radioButtonGroup.addAll(Arrays.asList(tifButton, aviButton, imageSequenceButton));

		saveOption.setR(saveAsLabel);
		saveOption.setL(radioButtonGroup);

		final Pair<ArrayList<JRadioButton>, JLabel> saveOptionFinalCopy = saveOption;

		tifButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				checkDefaultRadioButton(saveOptionFinalCopy.l);
			}
		});

		aviButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				checkDefaultRadioButton(saveOptionFinalCopy.l);
			}
		});

		imageSequenceButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				checkDefaultRadioButton(saveOptionFinalCopy.l);
			}
		});
	}

	public void moveLowerComponents(Component component, int x, int y) {
		Component[] components = bigPanel.getComponents();

		for (Component component2 : components) {
			if (component2 != component && component.getBounds().y <= component2.getBounds().y)
				Globals.moveComponent(component2, x, y);
		}
	}

	public void setPanelVisibility(JPanel panel, boolean visibility) {
		Component[] components = panel.getComponents();

		panel.setOpaque(visibility);
		panel.setVisible(visibility);
		for (Component component : components) {
			component.setVisible(visibility);
		}
	}

	public void updateBigPanel() {
		Component[] components = bigPanel.getComponents();

		int maxHeight = 0;
		for (Component component : components) {
			if (component.isVisible() && component.isOpaque()
					&& component.getBounds().y + component.getBounds().height > maxHeight)
				maxHeight = component.getBounds().y + component.getBounds().height;
		}

		bigPanel.setPreferredSize(new Dimension(0, maxHeight + 20));

		validate();
		repaint();
	}

	public void checkDefaultRadioButton(ArrayList<JRadioButton> radioButtonGroup) {
		boolean selected = false;

		for (int i = 0; i < radioButtonGroup.size(); i++) {
			if (radioButtonGroup.get(i).isSelected()) {
				selected = true;
			}
		}

		if (!selected)
			radioButtonGroup.get(0).setSelected(true);

	}
}
