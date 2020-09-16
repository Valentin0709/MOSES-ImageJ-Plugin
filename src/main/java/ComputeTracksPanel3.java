import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import ij.IJ;

public class ComputeTracksPanel3 extends JLayeredPane {

	private MainFrame parentFrame;
	private ComputeTracksPanel3 self = this;

	SaveOption saveOption1, saveOption2, saveOption3, saveOption4, saveOption5, saveOption6;
	ColorOption colorOption1, colorOption2, colorOption3, colorOption4, colorOption5;

	private JCheckBox saveCheckBox1 = null, saveCheckBox2 = null, saveCheckBox3 = null, saveCheckBox4 = null,
			saveCheckBox5 = null, saveCheckBox6, saveCheckBox7, saveCheckBox71, saveCheckBox72, saveCheckBox8,
			saveCheckBox112, saveCheckBox111, saveCheckBox9, saveCheckBox91, saveCheckBox92, saveCheckBox10,
			saveCheckBox11, saveCheckBox12, saveCheckBox13, saveCheckBox14;

	private JPanel optionPanel1, optionPanel2, optionPanel3, optionPanel4, optionPanel5, optionPanel6, optionPanel7,
			optionPanel8, optionPanel9, optionPanel10, optionPanel11, bigPanel;
	private JScrollPane scrollPane;

	private JRadioButton tifButtonGroup3, matButtonGroup3;

	private ArrayList<JRadioButton> radioButtonGroup1 = new ArrayList<JRadioButton>(),
			radioButtonGroup2 = new ArrayList<JRadioButton>(), radioButtonGroup3 = new ArrayList<JRadioButton>(),
			radioButtonGroup4 = new ArrayList<JRadioButton>();

	private JLabel tracksTitleLabel, meshTitleLabel, instructionLabel5, instructionLabel6, instructionLabel7,
			instructionLabel8, frameLabel, frameLabel2, frameLabel3, noteLabel, noteLabel2, noteLabel3,
			temporalSegmentLabel2, instructionLabel9, instructionLabel10, noteLabel4, noteLabel5, noteLabel6;
	private JFormattedTextField temporalSegmentField1, temporalSegmentField2, distanceTresholdField,
			distanceTresholdField2, frameField, frameField2, frameField3, KNeighborField;
	private JRadioButton backwardTracksButton1, forwardTracksButton1, backwardTracksButton2, forwardTracksButton2,
			backwardTracksButton3, forwardTracksButton3;

	private ComputeTracks swingWorker;
	private boolean swingWorkerStarted = false;

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

		// formats

		DecimalFormat decimalFormat = new DecimalFormat("#.##");
		decimalFormat.setGroupingUsed(false);
		DecimalFormat integerFormat = new DecimalFormat("###");
		integerFormat.setGroupingUsed(false);

		// page instructions

		JLabel backgroundPanel = new JLabel("Compute motion tracks and mesh", SwingConstants.CENTER);
		backgroundPanel.setBounds(0, 0, 500, 36);
		backgroundPanel.setVerticalTextPosition(SwingConstants.CENTER);
		backgroundPanel.setHorizontalTextPosition(SwingConstants.CENTER);
		backgroundPanel.setFont(new Font("Arial Black", Font.BOLD, 23));
		add(backgroundPanel);

		JLabel instructionLabel1 = new JLabel(
				"<html> Select you preferred outputs. You'll be asked to choose a saving directory at the end. </html>");
		instructionLabel1.setBounds(65, 40, 420, 36);
		instructionLabel1.setVerticalAlignment(SwingConstants.TOP);
		instructionLabel1.setHorizontalAlignment(SwingConstants.LEFT);
		instructionLabel1.setForeground(Color.DARK_GRAY);
		instructionLabel1.setFont(new Font("Roboto", Font.PLAIN, 15));
		add(instructionLabel1);

		JLabel stepLabel = new JLabel("STEP 3");
		stepLabel.setBounds(10, 40, 53, 30);
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
		setLayer(scrollPane, 0);
		scrollPane.setBorder(null);
		scrollPane.setBounds(20, 80, 460, 345);
		scrollPane.setViewportBorder(null);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		add(scrollPane);

		// save forward tracks checkbox

		saveCheckBox1 = new JCheckBox("<html>Forward motion tracks (.mat) </html>");
		saveCheckBox1.setHorizontalAlignment(SwingConstants.LEFT);
		saveCheckBox1.setFont(new Font("Roboto", Font.PLAIN, 15));
		saveCheckBox1.setBackground(new Color(252, 252, 252));
		saveCheckBox1.setBounds(0, 0, 400, 20);

		optionPanel10 = new JPanel();
		optionPanel10.setBounds(0, 0, 405, 85);
		optionPanel10.setOpaque(false);
		optionPanel10.setBackground(new Color(238, 238, 238));
		optionPanel10.setLayout(null);

		saveCheckBox13 = new JCheckBox("<html>Compute dense tracks</html>");
		saveCheckBox13.setHorizontalAlignment(SwingConstants.LEFT);
		saveCheckBox13.setFont(new Font("Roboto", Font.PLAIN, 15));
		saveCheckBox13.setBounds(5, 5, 300, 20);
		optionPanel10.add(saveCheckBox13);

		noteLabel5 = new JLabel(
				"<html> Note: Dense superpixel tracking is the preferred tracking approach when one expects the video to contain significant movement</html>");
		noteLabel5.setBounds(5, 25, 400, 60);
		noteLabel5.setVerticalAlignment(SwingConstants.TOP);
		noteLabel5.setHorizontalAlignment(SwingConstants.LEFT);
		noteLabel5.setForeground(Color.DARK_GRAY);
		noteLabel5.setFont(new Font("Roboto", Font.PLAIN, 15));
		optionPanel10.add(noteLabel5);

		// save forward tracks with overlay checkbox

		saveCheckBox2 = new JCheckBox("<html>Overlay forward motion tracks on the initial image</html>");
		saveCheckBox2.setEnabled(false);
		saveCheckBox2.setHorizontalAlignment(SwingConstants.LEFT);
		saveCheckBox2.setFont(new Font("Roboto", Font.PLAIN, 15));
		saveCheckBox2.setBackground(new Color(252, 252, 252));
		saveCheckBox2.setBounds(0, 0, 400, 20);

		optionPanel1 = new JPanel();
		optionPanel1.setBounds(0, 0, 405, 120);
		optionPanel1.setOpaque(false);
		optionPanel1.setBackground(new Color(238, 238, 238));
		optionPanel1.setLayout(null);

		JLabel instructionLabel2 = new JLabel("<html> Select the display color for each motion track </html>");
		instructionLabel2.setBounds(5, 5, 306, 18);
		instructionLabel2.setVerticalAlignment(SwingConstants.TOP);
		instructionLabel2.setHorizontalAlignment(SwingConstants.LEFT);
		instructionLabel2.setForeground(Color.DARK_GRAY);
		instructionLabel2.setFont(new Font("Roboto", Font.PLAIN, 15));
		optionPanel1.add(instructionLabel2);

		saveOption1 = new SaveOption(Arrays.asList(".tif", ".avi", ".png"));
		saveOption1.show(optionPanel1, 320, 5, true);
		colorOption1 = new ColorOption(ComputeTracksParameters.getSelectedChannels());
		colorOption1.show(optionPanel1, 10, 25, 3);

		JLabel temporalSegmentLabel1 = new JLabel("Plot tracks in temporal segments of length:");
		temporalSegmentLabel1.setVerticalAlignment(SwingConstants.TOP);
		temporalSegmentLabel1.setHorizontalAlignment(SwingConstants.LEFT);
		temporalSegmentLabel1.setVisible(false);
		temporalSegmentLabel1.setFont(new Font("Roboto", Font.PLAIN, 15));
		temporalSegmentLabel1.setBounds(5, 95, 295, 20);
		optionPanel1.add(temporalSegmentLabel1);

		temporalSegmentField1 = new JFormattedTextField(integerFormat) {

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
		temporalSegmentField1.setText(String.valueOf(ComputeTracksParameters.getForwardTracksTemporalSegment()));
		temporalSegmentField1.setVisible(false);
		temporalSegmentField1.setHorizontalAlignment(SwingConstants.CENTER);
		temporalSegmentField1.setFont(new Font("Roboto", Font.PLAIN, 15));
		temporalSegmentField1.setBounds(290, 97, 70, 18);
		optionPanel1.add(temporalSegmentField1);

		// save backward tracks checkbox

		saveCheckBox4 = new JCheckBox("<html>Backward motion tracks (.mat) </html>");
		saveCheckBox4.setHorizontalAlignment(SwingConstants.LEFT);
		saveCheckBox4.setFont(new Font("Roboto", Font.PLAIN, 15));
		saveCheckBox4.setBackground(new Color(252, 252, 252));
		saveCheckBox4.setBounds(0, 0, 400, 20);

		optionPanel11 = new JPanel();
		optionPanel11.setBounds(0, 0, 405, 85);
		optionPanel11.setOpaque(false);
		optionPanel11.setBackground(new Color(238, 238, 238));
		optionPanel11.setLayout(null);

		saveCheckBox14 = new JCheckBox("<html>Compute dense tracks</html>");
		saveCheckBox14.setHorizontalAlignment(SwingConstants.LEFT);
		saveCheckBox14.setFont(new Font("Roboto", Font.PLAIN, 15));
		saveCheckBox14.setBounds(5, 5, 300, 20);
		optionPanel11.add(saveCheckBox14);

		noteLabel6 = new JLabel(
				"<html> Note: Dense superpixel tracking is the preferred tracking approach when one expects the video to contain significant movement</html>");
		noteLabel6.setBounds(5, 25, 400, 60);
		noteLabel6.setVerticalAlignment(SwingConstants.TOP);
		noteLabel6.setHorizontalAlignment(SwingConstants.LEFT);
		noteLabel6.setForeground(Color.DARK_GRAY);
		noteLabel6.setFont(new Font("Roboto", Font.PLAIN, 15));
		optionPanel11.add(noteLabel6);

		// save backward tracks with overlay checkbox

		saveCheckBox5 = new JCheckBox("<html>Overlay backward motion tracks on the initial image</html>");
		saveCheckBox5.setHorizontalAlignment(SwingConstants.LEFT);
		saveCheckBox5.setFont(new Font("Roboto", Font.PLAIN, 15));
		saveCheckBox5.setEnabled(false);
		saveCheckBox5.setBackground(new Color(252, 252, 252));
		saveCheckBox5.setBounds(0, 0, 400, 20);

		optionPanel2 = new JPanel();
		optionPanel2.setOpaque(false);
		optionPanel2.setBounds(0, 0, 405, 140);
		optionPanel2.setBackground(new Color(238, 238, 238));
		optionPanel2.setLayout(null);

		saveOption2 = new SaveOption(Arrays.asList(".tif", ".avi", ".png"));
		saveOption2.show(optionPanel2, 320, 5, true);
		colorOption2 = new ColorOption(ComputeTracksParameters.getSelectedChannels());
		colorOption2.show(optionPanel2, 10, 25, 3);

		JLabel instructionLabel3 = new JLabel("<html> Select the display color for each motion track </html>");
		instructionLabel3.setVisible(false);
		instructionLabel3.setBounds(5, 5, 306, 18);
		instructionLabel3.setFont(new Font("Roboto", Font.PLAIN, 15));
		optionPanel2.add(instructionLabel3);

		temporalSegmentLabel2 = new JLabel("Plot tracks in temporal segments of length:");
		temporalSegmentLabel2.setVisible(false);
		temporalSegmentLabel2.setVerticalAlignment(SwingConstants.TOP);
		temporalSegmentLabel2.setHorizontalAlignment(SwingConstants.LEFT);
		temporalSegmentLabel2.setFont(new Font("Roboto", Font.PLAIN, 15));
		temporalSegmentLabel2.setBounds(5, 95, 295, 20);
		optionPanel2.add(temporalSegmentLabel2);

		temporalSegmentField2 = new JFormattedTextField(integerFormat) {
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
		temporalSegmentField2.setVisible(false);
		temporalSegmentField2.setText(String.valueOf(ComputeTracksParameters.getBackwardTracksTemporalSegment()));
		temporalSegmentField2.setHorizontalAlignment(SwingConstants.CENTER);
		temporalSegmentField2.setFont(new Font("Roboto", Font.PLAIN, 15));
		temporalSegmentField2.setBounds(290, 97, 70, 18);
		optionPanel2.add(temporalSegmentField2);

		noteLabel = new JLabel("Note: The tracks are plotted on the reversed video");
		noteLabel.setFont(new Font("Roboto", Font.PLAIN, 15));
		noteLabel.setVerticalAlignment(SwingConstants.TOP);
		noteLabel.setHorizontalAlignment(SwingConstants.LEFT);
		noteLabel.setVisible(false);
		noteLabel.setBounds(5, 115, 400, 20);
		optionPanel2.add(noteLabel);

		// save motion flow checkbox

		saveCheckBox3 = new JCheckBox("<html> Motion field</html>");
		saveCheckBox3.setHorizontalAlignment(SwingConstants.LEFT);
		saveCheckBox3.setFont(new Font("Roboto", Font.PLAIN, 15));
		saveCheckBox3.setBackground(new Color(252, 252, 252));
		saveCheckBox3.setBounds(0, 0, 442, 20);

		optionPanel3 = new JPanel();
		optionPanel3.setOpaque(false);
		optionPanel3.setBackground(new Color(238, 238, 238));
		optionPanel3.setBounds(0, 0, 425, 100);
		optionPanel3.setLayout(null);

		saveOption6 = new SaveOption(Arrays.asList(".mat", ".tif"));
		saveOption6.show(optionPanel3, 5, 0, false);

		JLabel instructionLabel4 = new JLabel(
				"<html> Note: For large images saving the motion field takes up a lot of hard disk space. It is therefore not recommended to do so. The motion field is summarized by the superpixel tracks. </html>");
		instructionLabel4.setVisible(false);
		instructionLabel4.setFont(new Font("Roboto", Font.PLAIN, 14));
		instructionLabel4.setBounds(5, 40, 420, 54);
		optionPanel3.add(instructionLabel4);

		// save MOSES mesh

		saveCheckBox6 = new JCheckBox("<html>MOSES mesh (.mat)</html>");
		saveCheckBox6.setHorizontalAlignment(SwingConstants.LEFT);
		saveCheckBox6.setFont(new Font("Roboto", Font.PLAIN, 15));
		saveCheckBox6.setBackground(new Color(252, 252, 252));
		saveCheckBox6.setBounds(0, 0, 442, 20);

		optionPanel4 = new JPanel();
		optionPanel4.setBounds(0, 0, 425, 111);
		optionPanel4.setLayout(null);
		optionPanel4.setOpaque(false);

		instructionLabel5 = new JLabel(
				"<html> Please set the distace threshold used for computing the MOSES mesh  </html>");
		instructionLabel5.setVisible(false);
		instructionLabel5.setVerticalAlignment(SwingConstants.TOP);
		instructionLabel5.setFont(new Font("Roboto", Font.PLAIN, 15));
		instructionLabel5.setBounds(5, 5, 415, 36);
		optionPanel4.add(instructionLabel5);

		distanceTresholdField = new JFormattedTextField(decimalFormat) {
			@Override
			protected void processFocusEvent(final FocusEvent e) {
				if (e.isTemporary()) {
					return;
				}

				if (e.getID() == FocusEvent.FOCUS_LOST) {
					if (getText() == null || getText().isEmpty()) {
						setValue(String.valueOf(ComputeTracksParameters.getMOSESMeshDistanceThreshold()));
					}
				}
				super.processFocusEvent(e);
			}
		};
		distanceTresholdField.setText(String.valueOf(ComputeTracksParameters.getMOSESMeshDistanceThreshold()));
		distanceTresholdField.setVisible(false);
		distanceTresholdField.setHorizontalAlignment(SwingConstants.CENTER);
		distanceTresholdField.setFont(new Font("Roboto", Font.PLAIN, 15));
		distanceTresholdField.setBounds(120, 25, 100, 18);
		optionPanel4.add(distanceTresholdField);

		instructionLabel6 = new JLabel("Compute mesh from:");
		instructionLabel6.setVisible(false);
		instructionLabel6.setFont(new Font("Roboto", Font.PLAIN, 15));
		instructionLabel6.setBounds(5, 50, 151, 16);
		optionPanel4.add(instructionLabel6);

		forwardTracksButton1 = new JRadioButton("Forward tracks");
		forwardTracksButton1.setSelected(true);
		forwardTracksButton1.setVisible(false);
		forwardTracksButton1.setFont(new Font("Roboto", Font.PLAIN, 15));
		forwardTracksButton1.setBounds(25, 66, 137, 20);
		optionPanel4.add(forwardTracksButton1);

		backwardTracksButton1 = new JRadioButton("Backward tracks");
		backwardTracksButton1.setVisible(false);
		backwardTracksButton1.setFont(new Font("Roboto", Font.PLAIN, 15));
		backwardTracksButton1.setBounds(25, 86, 137, 20);
		optionPanel4.add(backwardTracksButton1);

		forwardTracksButton1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				backwardTracksButton1.setSelected(!forwardTracksButton1.isSelected());
			}
		});

		backwardTracksButton1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				forwardTracksButton1.setSelected(!backwardTracksButton1.isSelected());
			}
		});

		// MOSES mesh vis

		saveCheckBox7 = new JCheckBox("Visualize MOSES mesh");
		saveCheckBox7.setEnabled(false);
		saveCheckBox7.setBackground(new Color(252, 252, 252));
		saveCheckBox7.setFont(new Font("Roboto", Font.PLAIN, 15));
		saveCheckBox7.setBounds(0, 0, 196, 20);

		optionPanel5 = new JPanel();
		optionPanel5.setBounds(0, 0, 405, 220);
		optionPanel5.setOpaque(false);
		optionPanel5.setLayout(null);

		saveCheckBox71 = new JCheckBox("Create visualization for a selected frame (.png)");
		saveCheckBox71.setVisible(false);
		saveCheckBox71.setFont(new Font("Roboto", Font.PLAIN, 15));
		saveCheckBox71.setBounds(5, 80, 347, 18);
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
						setValue(String.valueOf(ComputeTracksParameters.getMOSESMeshFrame()));
					}
				}
				super.processFocusEvent(e);
			}
		};
		frameField.setText(String.valueOf(ComputeTracksParameters.getMOSESMeshFrame()));
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

		noteLabel2 = new JLabel(
				"<html> Note: If you are using the backward tracks, the MOSES mesh will be plotted on the reversed video </html>");
		noteLabel2.setVisible(false);
		noteLabel2.setFont(new Font("Roboto", Font.PLAIN, 15));
		noteLabel2.setBounds(5, 180, 400, 36);
		optionPanel5.add(noteLabel2);

		saveOption3 = new SaveOption(Arrays.asList(".tif", ".avi", ".png"));
		saveOption3.show(optionPanel5, 10, 140, false);
		colorOption3 = new ColorOption(ComputeTracksParameters.getSelectedChannels());
		colorOption3.show(optionPanel5, 5, 25, 2);

		// save radial mesh

		saveCheckBox8 = new JCheckBox("<html>Radial mesh (.mat)</html>");
		saveCheckBox8.setHorizontalAlignment(SwingConstants.LEFT);
		saveCheckBox8.setFont(new Font("Roboto", Font.PLAIN, 15));
		saveCheckBox8.setBackground(new Color(252, 252, 252));
		saveCheckBox8.setBounds(0, 0, 442, 20);

		optionPanel6 = new JPanel();
		optionPanel6.setBounds(0, 0, 425, 111);
		optionPanel6.setLayout(null);
		optionPanel6.setOpaque(false);

		instructionLabel7 = new JLabel(
				"<html> Please set the distace threshold used for computing the radial mesh  </html>");
		instructionLabel7.setVisible(false);
		instructionLabel7.setVerticalAlignment(SwingConstants.TOP);
		instructionLabel7.setFont(new Font("Roboto", Font.PLAIN, 15));
		instructionLabel7.setBounds(5, 5, 415, 36);
		optionPanel6.add(instructionLabel7);

		distanceTresholdField2 = new JFormattedTextField(decimalFormat) {
			@Override
			protected void processFocusEvent(final FocusEvent e) {
				if (e.isTemporary()) {
					return;
				}

				if (e.getID() == FocusEvent.FOCUS_LOST) {
					if (getText() == null || getText().isEmpty()) {
						setValue(String.valueOf(ComputeTracksParameters.getRadialMeshDistanceThreshold()));
					}
				}
				super.processFocusEvent(e);
			}
		};
		distanceTresholdField2.setText(String.valueOf(ComputeTracksParameters.getRadialMeshDistanceThreshold()));
		distanceTresholdField2.setVisible(false);
		distanceTresholdField2.setHorizontalAlignment(SwingConstants.CENTER);
		distanceTresholdField2.setFont(new Font("Roboto", Font.PLAIN, 15));
		distanceTresholdField2.setBounds(120, 25, 100, 18);
		optionPanel6.add(distanceTresholdField2);

		instructionLabel8 = new JLabel("Compute mesh from:");
		instructionLabel8.setVisible(false);
		instructionLabel8.setFont(new Font("Roboto", Font.PLAIN, 15));
		instructionLabel8.setBounds(5, 50, 151, 16);
		optionPanel6.add(instructionLabel8);

		forwardTracksButton2 = new JRadioButton("Forward tracks");
		forwardTracksButton2.setSelected(true);
		forwardTracksButton2.setVisible(false);
		forwardTracksButton2.setFont(new Font("Roboto", Font.PLAIN, 15));
		forwardTracksButton2.setBounds(25, 66, 137, 20);
		optionPanel6.add(forwardTracksButton2);

		backwardTracksButton2 = new JRadioButton("Backward tracks");
		backwardTracksButton2.setVisible(false);
		backwardTracksButton2.setFont(new Font("Roboto", Font.PLAIN, 15));
		backwardTracksButton2.setBounds(25, 86, 137, 20);
		optionPanel6.add(backwardTracksButton2);

		forwardTracksButton2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				backwardTracksButton2.setSelected(!forwardTracksButton2.isSelected());
			}
		});

		backwardTracksButton2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				forwardTracksButton2.setSelected(!backwardTracksButton2.isSelected());
			}
		});

		saveCheckBox9 = new JCheckBox("Visualize radial mesh");
		saveCheckBox9.setEnabled(false);
		saveCheckBox9.setBackground(new Color(252, 252, 252));
		saveCheckBox9.setFont(new Font("Roboto", Font.PLAIN, 15));
		saveCheckBox9.setBounds(0, 0, 196, 20);

		// save radial vis

		optionPanel7 = new JPanel();
		optionPanel7.setBounds(0, 0, 405, 220);
		optionPanel7.setOpaque(false);
		optionPanel7.setLayout(null);

		saveCheckBox91 = new JCheckBox("Create visualization for a selected frame (.png)");
		saveCheckBox91.setVisible(false);
		saveCheckBox91.setFont(new Font("Roboto", Font.PLAIN, 15));
		saveCheckBox91.setBounds(5, 80, 401, 18);
		optionPanel7.add(saveCheckBox91);

		frameLabel2 = new JLabel("Frame:");
		frameLabel2.setVisible(false);
		frameLabel2.setFont(new Font("Roboto", Font.PLAIN, 15));
		frameLabel2.setBounds(45, 100, 55, 20);
		optionPanel7.add(frameLabel2);

		frameField2 = new JFormattedTextField(integerFormat) {

			@Override
			protected void processFocusEvent(final FocusEvent e) {
				if (e.isTemporary()) {
					return;
				}

				if (e.getID() == FocusEvent.FOCUS_LOST) {
					if (getText() == null || getText().isEmpty()) {
						setValue(String.valueOf(ComputeTracksParameters.getRadialMeshFrame()));
					}
				}
				super.processFocusEvent(e);
			}
		};
		frameField2.setText(String.valueOf(ComputeTracksParameters.getRadialMeshFrame()));
		frameField2.setHorizontalAlignment(SwingConstants.CENTER);
		frameField2.setVisible(false);
		frameField2.setFont(new Font("Roboto", Font.PLAIN, 15));
		frameField2.setBounds(100, 100, 100, 20);
		optionPanel7.add(frameField2);

		saveCheckBox92 = new JCheckBox("Create visualization for all frames");
		saveCheckBox92.setVisible(false);
		saveCheckBox92.setFont(new Font("Roboto", Font.PLAIN, 15));
		saveCheckBox92.setBounds(5, 120, 261, 18);
		optionPanel7.add(saveCheckBox92);

		JLabel lblSelectTheDisplay2 = new JLabel("<html> Select the display color for the mesh nodes </html>");
		lblSelectTheDisplay2.setVisible(false);
		lblSelectTheDisplay2.setFont(new Font("Roboto", Font.PLAIN, 15));
		lblSelectTheDisplay2.setBounds(5, 5, 335, 20);
		optionPanel7.add(lblSelectTheDisplay2);

		noteLabel3 = new JLabel(
				"<html> Note: If you are using the backward tracks, the radial mesh will be plotted on the reversed video </html>");
		noteLabel3.setVisible(false);
		noteLabel3.setFont(new Font("Roboto", Font.PLAIN, 15));
		noteLabel3.setBounds(5, 180, 400, 36);
		optionPanel7.add(noteLabel3);

		// generateSaveOptionList(optionPanel7, saveList4, 10, 140, false);
		saveOption4 = new SaveOption(Arrays.asList(".tif", ".avi", ".png"));
		saveOption4.show(optionPanel7, 10, 140, false);
		colorOption4 = new ColorOption(ComputeTracksParameters.getSelectedChannels());
		colorOption4.show(optionPanel7, 5, 25, 2);

		// save K nearest neighbors mesh

		saveCheckBox10 = new JCheckBox("<html>K nearest neighbors mesh (.mat)</html>");
		saveCheckBox10.setHorizontalAlignment(SwingConstants.LEFT);
		saveCheckBox10.setFont(new Font("Roboto", Font.PLAIN, 15));
		saveCheckBox10.setBackground(new Color(252, 252, 252));
		saveCheckBox10.setBounds(0, 0, 442, 20);

		optionPanel8 = new JPanel();
		optionPanel8.setBounds(0, 0, 425, 111);
		optionPanel8.setLayout(null);
		optionPanel8.setOpaque(false);

		instructionLabel9 = new JLabel(
				"<html> Please set the K value used for computing the K nearest neighbors mesh </html>");
		instructionLabel9.setVisible(false);
		instructionLabel9.setVerticalAlignment(SwingConstants.TOP);
		instructionLabel9.setFont(new Font("Roboto", Font.PLAIN, 15));
		instructionLabel9.setBounds(5, 5, 415, 36);
		optionPanel8.add(instructionLabel9);

		KNeighborField = new JFormattedTextField(integerFormat) {
			@Override
			protected void processFocusEvent(final FocusEvent e) {
				if (e.isTemporary()) {
					return;
				}

				if (e.getID() == FocusEvent.FOCUS_LOST) {
					if (getText() == null || getText().isEmpty()) {
						setValue(String.valueOf(ComputeTracksParameters.getKNeighbor()));
					}
				}
				super.processFocusEvent(e);
			}
		};
		KNeighborField.setText(String.valueOf(ComputeTracksParameters.getKNeighbor()));
		KNeighborField.setVisible(false);
		KNeighborField.setHorizontalAlignment(SwingConstants.CENTER);
		KNeighborField.setFont(new Font("Roboto", Font.PLAIN, 15));
		KNeighborField.setBounds(120, 25, 100, 18);
		optionPanel8.add(KNeighborField);

		instructionLabel10 = new JLabel("Compute mesh from:");
		instructionLabel10.setVisible(false);
		instructionLabel10.setFont(new Font("Roboto", Font.PLAIN, 15));
		instructionLabel10.setBounds(5, 50, 151, 16);
		optionPanel8.add(instructionLabel10);

		forwardTracksButton3 = new JRadioButton("Forward tracks");
		forwardTracksButton3.setSelected(true);
		forwardTracksButton3.setVisible(false);
		forwardTracksButton3.setFont(new Font("Roboto", Font.PLAIN, 15));
		forwardTracksButton3.setBounds(25, 66, 137, 20);
		optionPanel8.add(forwardTracksButton3);

		backwardTracksButton3 = new JRadioButton("Backward tracks");
		backwardTracksButton3.setVisible(false);
		backwardTracksButton3.setFont(new Font("Roboto", Font.PLAIN, 15));
		backwardTracksButton3.setBounds(25, 86, 137, 20);
		optionPanel8.add(backwardTracksButton3);

		forwardTracksButton3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				backwardTracksButton3.setSelected(!forwardTracksButton3.isSelected());
			}
		});

		backwardTracksButton3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				forwardTracksButton3.setSelected(!backwardTracksButton3.isSelected());
			}
		});

		// save radial vis

		saveCheckBox11 = new JCheckBox("Visualize K nearest neighbors mesh");
		saveCheckBox11.setEnabled(false);
		saveCheckBox11.setBackground(new Color(252, 252, 252));
		saveCheckBox11.setFont(new Font("Roboto", Font.PLAIN, 15));
		saveCheckBox11.setBounds(0, 0, 300, 20);

		optionPanel9 = new JPanel();
		optionPanel9.setBounds(0, 0, 405, 220);
		optionPanel9.setOpaque(false);
		optionPanel9.setLayout(null);

		saveCheckBox111 = new JCheckBox("Create visualization for a selected frame (.png)");
		saveCheckBox111.setVisible(false);
		saveCheckBox111.setFont(new Font("Roboto", Font.PLAIN, 15));
		saveCheckBox111.setBounds(5, 80, 401, 18);
		optionPanel9.add(saveCheckBox111);

		frameLabel3 = new JLabel("Frame:");
		frameLabel3.setVisible(false);
		frameLabel3.setFont(new Font("Roboto", Font.PLAIN, 15));
		frameLabel3.setBounds(45, 100, 55, 20);
		optionPanel9.add(frameLabel3);

		frameField3 = new JFormattedTextField(integerFormat) {

			@Override
			protected void processFocusEvent(final FocusEvent e) {
				if (e.isTemporary()) {
					return;
				}

				if (e.getID() == FocusEvent.FOCUS_LOST) {
					if (getText() == null || getText().isEmpty()) {
						setValue(String.valueOf(ComputeTracksParameters.getNeighborMeshFrame()));
					}
				}
				super.processFocusEvent(e);
			}
		};
		frameField3.setText(String.valueOf(ComputeTracksParameters.getNeighborMeshFrame()));
		frameField3.setHorizontalAlignment(SwingConstants.CENTER);
		frameField3.setVisible(false);
		frameField3.setFont(new Font("Roboto", Font.PLAIN, 15));
		frameField3.setBounds(100, 100, 100, 20);
		optionPanel9.add(frameField3);

		saveCheckBox112 = new JCheckBox("Create visualization for all frames");
		saveCheckBox112.setVisible(false);
		saveCheckBox112.setFont(new Font("Roboto", Font.PLAIN, 15));
		saveCheckBox112.setBounds(5, 120, 261, 18);
		optionPanel9.add(saveCheckBox112);

		JLabel lblSelectTheDisplay3 = new JLabel("<html> Select the display color for the mesh nodes </html>");
		lblSelectTheDisplay3.setVisible(false);
		lblSelectTheDisplay3.setFont(new Font("Roboto", Font.PLAIN, 15));
		lblSelectTheDisplay3.setBounds(5, 5, 335, 20);
		optionPanel9.add(lblSelectTheDisplay3);

		noteLabel4 = new JLabel(
				"<html> Note: If you are using the backward tracks, the radial mesh will be plotted on the reversed video </html>");
		noteLabel4.setVisible(false);
		noteLabel4.setFont(new Font("Roboto", Font.PLAIN, 15));
		noteLabel4.setBounds(5, 180, 400, 36);
		optionPanel9.add(noteLabel4);

		saveOption5 = new SaveOption(Arrays.asList(".tif", ".avi", ".png"));
		saveOption5.show(optionPanel9, 10, 140, false);
		colorOption5 = new ColorOption(ComputeTracksParameters.getSelectedChannels());
		colorOption5.show(optionPanel9, 5, 25, 2);

		// buttons

		// cancel button

		JButton cancelButton = new JButton("Cancel");
		cancelButton.setBounds(10, 430, 140, 30);
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// stop swing worker
				if (swingWorkerStarted)
					swingWorker.destroy();

				// display menuPanel and close current panel
				parentFrame.empty();
				parentFrame.menuPanel = new MenuPanel(parentFrame);
				parentFrame.getContentPane().add(parentFrame.menuPanel);
				parentFrame.validate();
			}
		});
		cancelButton.setVerticalTextPosition(SwingConstants.CENTER);
		cancelButton.setHorizontalTextPosition(SwingConstants.CENTER);
		cancelButton.setForeground(new Color(252, 252, 252));
		cancelButton.setFont(new Font("Arial", Font.BOLD, 15));
		cancelButton.setBackground(new Color(13, 59, 102));
		add(cancelButton);

		// back button

		JButton backButton = new JButton("Back");
		backButton.setBounds(200, 430, 140, 30);
		backButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// stop swing worker
				if (swingWorkerStarted)
					swingWorker.destroy();

				// display menuPanel and close current panel

				parentFrame.empty();
				parentFrame.computeTracksPanel2 = new ComputeTracksPanel2(parentFrame);
				parentFrame.getContentPane().add(parentFrame.computeTracksPanel2);
				parentFrame.validate();
			}
		});
		backButton.setVerticalTextPosition(SwingConstants.CENTER);
		backButton.setHorizontalTextPosition(SwingConstants.CENTER);
		backButton.setForeground(new Color(252, 252, 252));
		backButton.setFont(new Font("Arial", Font.BOLD, 15));
		backButton.setBackground(new Color(13, 59, 102));
		add(backButton);

		// finish button

		JButton nextButton = new JButton("Finish");
		nextButton.setBounds(350, 430, 140, 30);
		nextButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// load file chooser and run script if any checkbox is selected

				if (saveCheckBox1.isEnabled() || saveCheckBox3.isEnabled() || saveCheckBox4.isEnabled()
						|| saveCheckBox6.isEnabled() || saveCheckBox8.isEnabled()) {

					String saveDirectory = IJ.getDirectory("Choose saving directory");
					if (saveDirectory != null) {
						ComputeTracksParameters.setSaveDirectory(saveDirectory);

						// add parameters

						if (saveCheckBox1.isSelected()) {
							ComputeTracksParameters.setOutput("forward_tracks");
							ComputeTracksParameters.setDenseForwardTracks(saveCheckBox13.isSelected());
						}
						if (saveCheckBox2.isSelected()) {
							ComputeTracksParameters.setOutput("forward_tracks_visualisation");
							ComputeTracksParameters.setSaveOption("forward_tracks_save_options", saveOption1);
							ComputeTracksParameters.setColorOption("forward_tracks_colors", colorOption1);
							ComputeTracksParameters
									.setForwardTracksTemporalSegment(Integer.parseInt(temporalSegmentField1.getText()));
						}

						if (saveCheckBox3.isSelected()) {
							ComputeTracksParameters.setOutput("motion_field");
							ComputeTracksParameters.setSaveOption("motion_field_save_options", saveOption6);
						}

						if (saveCheckBox4.isSelected()) {
							ComputeTracksParameters.setOutput("backward_tracks");
							ComputeTracksParameters.setDenseBackwardTracks(saveCheckBox14.isSelected());
						}
						if (saveCheckBox5.isSelected()) {
							ComputeTracksParameters.setOutput("backward_tracks_visualisation");
							ComputeTracksParameters.setSaveOption("backward_tracks_save_options", saveOption2);
							ComputeTracksParameters.setColorOption("backward_tracks_colors", colorOption2);
							ComputeTracksParameters.setBackwardTracksTemporalSegment(
									Integer.parseInt(temporalSegmentField2.getText()));
						}

						if (saveCheckBox6.isSelected()) {
							ComputeTracksParameters.setOutput("MOSES_mesh");
							ComputeTracksParameters
									.setMOSESMeshDistanceThreshold(Double.parseDouble(distanceTresholdField.getText()));
							ComputeTracksParameters.setMOSESMeshForwardTracks(forwardTracksButton1.isSelected());
						}
						if (saveCheckBox7.isSelected()) {
							ComputeTracksParameters.setOutput("MOSES_mesh_visualisation");
							ComputeTracksParameters.setColorOption("MOSES_mesh_colors", colorOption3);
						}
						if (saveCheckBox71.isSelected()) {
							ComputeTracksParameters.setOutput("MOSES_mesh_frame_visualisation");
							ComputeTracksParameters.setMOSESMeshFrame(Integer.parseInt(frameField.getText()));
						}
						if (saveCheckBox72.isSelected()) {
							ComputeTracksParameters.setOutput("MOSES_mesh_complete_visualisation");

							ComputeTracksParameters.setSaveOption("MOSES_mesh_complete_visualisation_save_options",
									saveOption3);
						}

						if (saveCheckBox8.isSelected()) {
							ComputeTracksParameters.setOutput("radial_mesh");
							ComputeTracksParameters.setRadialMeshDistanceThreshold(
									Double.parseDouble(distanceTresholdField2.getText()));
							ComputeTracksParameters.setRadialMeshForwardTracks(forwardTracksButton2.isSelected());
						}
						if (saveCheckBox9.isSelected()) {
							ComputeTracksParameters.setOutput("radial_mesh_visualisation");
							ComputeTracksParameters.setColorOption("radial_mesh_colors", colorOption4);
						}
						if (saveCheckBox91.isSelected()) {
							ComputeTracksParameters.setOutput("radial_mesh_frame_visualisation");
							ComputeTracksParameters.setRadialMeshFrame(Integer.parseInt(frameField2.getText()));
						}
						if (saveCheckBox92.isSelected()) {
							ComputeTracksParameters.setOutput("radial_mesh_complete_visualisation");
							ComputeTracksParameters.setSaveOption("radial_mesh_complete_visualisation_save_options",
									saveOption4);
						}

						if (saveCheckBox10.isSelected()) {
							ComputeTracksParameters.setOutput("neighbor_mesh");
							ComputeTracksParameters.setKNeighbor(Integer.parseInt(KNeighborField.getText()));
							ComputeTracksParameters.setNeighborMeshForwardTracks(forwardTracksButton3.isSelected());
						}
						if (saveCheckBox11.isSelected()) {
							ComputeTracksParameters.setOutput("neighbor_mesh_visualisation");
							ComputeTracksParameters.setColorOption("neighbor_mesh_colors", colorOption5);
						}
						if (saveCheckBox111.isSelected()) {
							ComputeTracksParameters.setOutput("neighbor_mesh_frame_visualisation");
							ComputeTracksParameters.setNeighborMeshFrame(Integer.parseInt(frameField3.getText()));
						}
						if (saveCheckBox112.isSelected()) {
							ComputeTracksParameters.setOutput("neighbor_mesh_complete_visualisation");
							ComputeTracksParameters.setSaveOption("neighbor_mesh_complete_visualisation_save_options",
									saveOption5);
						}

						if (saveCheckBox12.isSelected())
							ComputeTracksParameters.setOutput("config_file");

						// loading bar panel

						ProgressPanel progress = new ProgressPanel(self, 40, 200);
						self.add(progress);
						self.setLayer(progress, 1);
						progress.setVisibility(true);

						Globals.setPanelEnabled(bigPanel, false);
						scrollPane.setVerticalScrollBarPolicy(scrollPane.VERTICAL_SCROLLBAR_NEVER);

						// run process
						class SwingWorkerListener implements PropertyChangeListener {
							@Override
							public void propertyChange(PropertyChangeEvent evt) {
								if (swingWorker.isDone()) {
									parentFrame.empty();
									parentFrame.menuPanel = new MenuPanel(parentFrame);
									parentFrame.getContentPane().add(parentFrame.menuPanel);
									parentFrame.validate();
								}

							}
						}

						swingWorker = new ComputeTracks(progress);
						swingWorkerStarted = true;
						swingWorker.addPropertyChangeListener(new SwingWorkerListener());
						swingWorker.execute();
					}
				}

			}
		});

		nextButton.setVerticalTextPosition(SwingConstants.CENTER);
		nextButton.setHorizontalTextPosition(SwingConstants.CENTER);
		nextButton.setForeground(new Color(252, 252, 252));
		nextButton.setFont(new Font("Arial", Font.BOLD, 15));
		nextButton.setBackground(new Color(13, 59, 102));
		add(nextButton);
		radioButtonGroup3.addAll(Arrays.asList(matButtonGroup3, tifButtonGroup3));

		// titles

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

		// save config file

		saveCheckBox12 = new JCheckBox("Save config file");
		saveCheckBox12.setFont(new Font("Roboto", Font.PLAIN, 15));
		saveCheckBox12.setBounds(10, 325, 320, 20);
		saveCheckBox12.setBackground(new Color(252, 252, 252));
		bigPanel.add(saveCheckBox12);

		// generate checkbox list

		CheckBoxList checkBoxList = new CheckBoxList(bigPanel);
		checkBoxList.addCheckBox(saveCheckBox1);
		checkBoxList.addPanelChild(saveCheckBox1, optionPanel10);
		checkBoxList.addCheckBoxChild(saveCheckBox1, saveCheckBox2);
		checkBoxList.addPanelChild(saveCheckBox2, optionPanel1);
		checkBoxList.addCheckBox(saveCheckBox4);
		checkBoxList.addPanelChild(saveCheckBox4, optionPanel11);
		checkBoxList.addCheckBoxChild(saveCheckBox4, saveCheckBox5);
		checkBoxList.addPanelChild(saveCheckBox5, optionPanel2);
		checkBoxList.addCheckBox(saveCheckBox3);
		checkBoxList.addPanelChild(saveCheckBox3, optionPanel3);
		checkBoxList.show(10, 25);

		CheckBoxList checkBoxList2 = new CheckBoxList(bigPanel);
		checkBoxList2.addCheckBox(saveCheckBox6);
		checkBoxList2.addPanelChild(saveCheckBox6, optionPanel4);
		checkBoxList2.addCheckBoxChild(saveCheckBox6, saveCheckBox7);
		checkBoxList2.addPanelChild(saveCheckBox7, optionPanel5);
		checkBoxList2.addCheckBox(saveCheckBox8);
		checkBoxList2.addPanelChild(saveCheckBox8, optionPanel6);
		checkBoxList2.addCheckBoxChild(saveCheckBox8, saveCheckBox9);
		checkBoxList2.addPanelChild(saveCheckBox9, optionPanel7);
		checkBoxList2.show(10, 175);
		checkBoxList2.addCheckBox(saveCheckBox10);
		checkBoxList2.addPanelChild(saveCheckBox10, optionPanel8);
		checkBoxList2.addCheckBoxChild(saveCheckBox10, saveCheckBox11);
		checkBoxList2.addPanelChild(saveCheckBox11, optionPanel9);
		checkBoxList2.show(10, 175);
	}
}
