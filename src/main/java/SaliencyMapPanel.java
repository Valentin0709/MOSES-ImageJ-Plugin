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
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import ij.IJ;

public class SaliencyMapPanel extends JLayeredPane {
	private MainFrame parentFrame;
	private SaliencyMapPanel self = this;

	private JLabel importedFilesLabel, instructionLabel, instructionLabel2, noteLabel;
	private JCheckBox saveCheckBox1, saveCheckBox2, saveCheckBox3, saveCheckBox4, saveCheckBox6, saveCheckBox7;
	private JFormattedTextField distanceTresholdField, paddingField;
	private JPanel optionPanel1, optionPanel2, optionPanel3, optionPanel4, bigPanel;
	private JScrollPane scrollPane2;

	SaveOption saveOption1, saveOption2;

	private SaliencyMap swingWorker;
	private boolean swingWorkerStarted = false;

	public SaliencyMapPanel(MainFrame parentFrame) {
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

		DecimalFormat decimalFormat = new DecimalFormat("#.##");
		decimalFormat.setGroupingUsed(false);

		JLabel titleLabel = new JLabel("Motion saliency map", SwingConstants.CENTER);
		titleLabel.setVerticalTextPosition(SwingConstants.CENTER);
		titleLabel.setHorizontalTextPosition(SwingConstants.CENTER);
		titleLabel.setFont(new Font("Arial Black", Font.BOLD, 23));
		titleLabel.setBounds(0, 0, 500, 36);
		add(titleLabel);

		JButton importMotionTracksButton = new JButton("Import motion tracks");
		importMotionTracksButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ArrayList<String> validExtensions = new ArrayList<String>();
				validExtensions.addAll(Arrays.asList(".mat"));

				int n = 0;
				if (SaliencyMapParameters.getBatchMode()) {
					// display dialog box
					JFrame dialog = new JFrame();
					Object[] options = { "Cancel", "Import now" };
					n = JOptionPane.showOptionDialog(dialog,
							"Batch mode is enabled. Please select a file from the folder you want to analyse and input your preffered settings. MOSES will automatically import and compute the motion measurements for all the other files that have a valid format using the same settings.",
							"MOSES", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);
				}
				if (!SaliencyMapParameters.getBatchMode() || n == 1) {
					String importedFilePath = Globals.openFile(parentFrame.ui, validExtensions, false);

					if (importedFilePath != null) {
						import_motion_tracks(importedFilePath);
					} else {
						// display error dialog box
						JFrame dialog = new JFrame();
						Object[] options = { "Ok" };
						JOptionPane.showOptionDialog(dialog,
								"The selected file has an invalid file format. Please import a MATLAB file to continue.",
								"MOSES", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, options,
								options[0]);
					}
				}
			}
		});
		importMotionTracksButton.setVerticalTextPosition(SwingConstants.CENTER);
		importMotionTracksButton.setHorizontalTextPosition(SwingConstants.CENTER);
		importMotionTracksButton.setForeground(Color.WHITE);
		importMotionTracksButton.setFont(new Font("Arial", Font.BOLD, 15));
		importMotionTracksButton.setBackground(new Color(13, 59, 102));
		importMotionTracksButton.setBounds(57, 105, 386, 20);
		add(importMotionTracksButton);

		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
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
		cancelButton.setForeground(Color.WHITE);
		cancelButton.setFont(new Font("Arial", Font.BOLD, 15));
		cancelButton.setBackground(new Color(13, 59, 102));
		cancelButton.setBounds(10, 430, 140, 30);
		add(cancelButton);

		JButton finishButton = new JButton("Finish");
		finishButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (saveCheckBox1.isEnabled()) {

					String saveDirectory = IJ.getDirectory("Choose saving directory");
					if (saveDirectory != null) {
						SaliencyMapParameters.setSaveDirectory(saveDirectory);

						if (saveCheckBox1.isSelected()) {
							SaliencyMapParameters.setOutput("motion_saliency_map");
							SaliencyMapParameters.setSaliencyMapDistanceThreshold(
									Double.parseDouble(distanceTresholdField.getText()));

							if (saveCheckBox2.isSelected())
								SaliencyMapParameters.setOutput("final_saliency_map_visualisation");

							if (saveCheckBox3.isSelected()) {
								SaliencyMapParameters.setOutput("spatial_time_saliency_map_visualisation");
								SaliencyMapParameters.setSaveOption(
										"spatial_time_saliency_map_visualisation_save_options", saveOption1);
							}

							if (saveCheckBox6.isSelected()) {
								SaliencyMapParameters.setOutput("boundary_formation_index");
								SaliencyMapParameters.setPaddingDistance(Integer.parseInt(paddingField.getText()));

								if (saveCheckBox4.isSelected()) {
									SaliencyMapParameters.setOutput("average_motion_saliency_map");
									SaliencyMapParameters.setSaveOption("average_motion_saliency_map_save_options",
											saveOption2);
								}
							}
						}

						if (saveCheckBox7.isSelected())
							SaliencyMapParameters.setOutput("config_file");

						// loading bar panel
						ProgressPanel progress = new ProgressPanel(self, 40, 200);
						self.add(progress);
						self.setLayer(progress, 1);
						progress.setVisibility(true);
						Globals.setPanelEnabled(bigPanel, false);

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

						swingWorker = new SaliencyMap(progress);
						swingWorkerStarted = true;
						swingWorker.addPropertyChangeListener(new SwingWorkerListener());
						swingWorker.execute();
					}
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

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportBorder(null);
		scrollPane.setBackground(new Color(252, 252, 252));
		scrollPane.setBounds(10, 130, 480, 65);
		add(scrollPane);

		importedFilesLabel = new JLabel("No files selected");
		importedFilesLabel.setLocation(7, 0);
		importedFilesLabel.setBorder(null);
		scrollPane.setViewportView(importedFilesLabel);
		importedFilesLabel.setVerticalAlignment(SwingConstants.TOP);
		importedFilesLabel.setHorizontalAlignment(SwingConstants.LEFT);
		importedFilesLabel.setForeground(Color.DARK_GRAY);
		importedFilesLabel.setBackground(new Color(252, 252, 252));
		importedFilesLabel.setFont(new Font("Roboto", Font.PLAIN, 15));

		// motion saliency map

		scrollPane2 = new JScrollPane();
		scrollPane2.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane2.setBorder(null);
		scrollPane2.setBounds(10, 200, 480, 220);
		scrollPane2.setBackground(new Color(252, 252, 252));
		add(scrollPane2);

		bigPanel = new JPanel();
		bigPanel.setLocation(10, 0);
		bigPanel.setBackground(new Color(252, 252, 252));
		scrollPane2.setViewportView(bigPanel);

		saveCheckBox1 = new JCheckBox("Motion saliency map (.mat)");
		saveCheckBox1.setEnabled(false);
		saveCheckBox1.setBackground(new Color(252, 252, 252));
		saveCheckBox1.setFont(new Font("Roboto", Font.PLAIN, 15));
		saveCheckBox1.setBounds(0, 0, 400, 20);

		optionPanel1 = new JPanel();
		optionPanel1.setBounds(0, 0, 450, 55);
		optionPanel1.setOpaque(false);
		optionPanel1.setBackground(new Color(238, 238, 238));
		optionPanel1.setLayout(null);

		instructionLabel = new JLabel(
				"<html> Please set the distace threshold used for computing the motion saliency map </html>");
		instructionLabel.setVisible(false);
		instructionLabel.setVerticalAlignment(SwingConstants.TOP);
		instructionLabel.setFont(new Font("Roboto", Font.PLAIN, 15));
		instructionLabel.setBounds(5, 5, 450, 40);
		optionPanel1.add(instructionLabel);

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
		distanceTresholdField.setText(String.valueOf(SaliencyMapParameters.getSaliencyMapDistanceThreshold()));
		distanceTresholdField.setVisible(false);
		distanceTresholdField.setHorizontalAlignment(SwingConstants.CENTER);
		distanceTresholdField.setFont(new Font("Roboto", Font.PLAIN, 15));
		distanceTresholdField.setBounds(120, 25, 100, 20);
		optionPanel1.add(distanceTresholdField);

		// motion saliency visualisation

		saveCheckBox2 = new JCheckBox("Visualize final saliency map (.png)");
		saveCheckBox2.setEnabled(false);
		saveCheckBox2.setBackground(new Color(252, 252, 252));
		saveCheckBox2.setFont(new Font("Roboto", Font.PLAIN, 15));
		saveCheckBox2.setBounds(0, 0, 400, 20);

		saveCheckBox3 = new JCheckBox("Visualize spatial time saliency map");
		saveCheckBox3.setEnabled(false);
		saveCheckBox3.setBackground(new Color(252, 252, 252));
		saveCheckBox3.setFont(new Font("Roboto", Font.PLAIN, 15));
		saveCheckBox3.setBounds(0, 0, 400, 20);

		optionPanel2 = new JPanel();
		optionPanel2.setBounds(0, 0, 450, 45);
		optionPanel2.setOpaque(false);
		optionPanel2.setBackground(new Color(238, 238, 238));
		optionPanel2.setLayout(null);

		saveOption1 = new SaveOption(Arrays.asList(".tif", ".avi", ".png"));
		saveOption1.show(optionPanel2, 0, 0, false);

		// average saliency map

		saveCheckBox4 = new JCheckBox("Get average motion saliency map");
		saveCheckBox4.setEnabled(false);
		saveCheckBox4.setBackground(new Color(252, 252, 252));
		saveCheckBox4.setFont(new Font("Roboto", Font.PLAIN, 15));
		saveCheckBox4.setBounds(0, 0, 400, 20);

		optionPanel4 = new JPanel();
		optionPanel4.setBounds(0, 0, 450, 45);
		optionPanel4.setOpaque(false);
		optionPanel4.setBackground(new Color(238, 238, 238));
		optionPanel4.setLayout(null);

		saveOption2 = new SaveOption(Arrays.asList(".mat", ".png"));
		saveOption2.show(optionPanel4, 0, 0, false);

		// boundary formation index

		saveCheckBox6 = new JCheckBox("Compute boundary formation index");
		saveCheckBox6.setEnabled(false);
		saveCheckBox6.setBackground(new Color(252, 252, 252));
		saveCheckBox6.setFont(new Font("Roboto", Font.PLAIN, 15));
		saveCheckBox6.setBounds(0, 0, 400, 20);

		optionPanel3 = new JPanel();
		optionPanel3.setBounds(0, 0, 450, 150);
		optionPanel3.setOpaque(false);
		optionPanel3.setBackground(new Color(238, 238, 238));
		optionPanel3.setLayout(null);

		noteLabel = new JLabel(
				"<html> Note: The boundary formation index is computed only for motion tracks saved together in the same MATLAB file </html>");
		noteLabel.setVisible(false);
		noteLabel.setVerticalAlignment(SwingConstants.TOP);
		noteLabel.setFont(new Font("Roboto", Font.PLAIN, 15));
		noteLabel.setBounds(5, 5, 450, 40);
		optionPanel3.add(noteLabel);

		instructionLabel2 = new JLabel(
				"<html> Please set the padding distance used to define the region of the image where the boundary formation index is computed. (This minimizes the effect created by cells moving out of the field of view of the video which causes superpixels to concentrate at the boundary of the videos) </html>");
		instructionLabel2.setVisible(false);
		instructionLabel2.setVerticalAlignment(SwingConstants.TOP);
		instructionLabel2.setFont(new Font("Roboto", Font.PLAIN, 15));
		instructionLabel2.setBounds(5, 43, 450, 100);
		optionPanel3.add(instructionLabel2);

		DecimalFormat integerFormat = new DecimalFormat("###");

		paddingField = new JFormattedTextField(integerFormat) {
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
		paddingField.setText(String.valueOf(SaliencyMapParameters.getPaddingDistance()));
		paddingField.setVisible(false);
		paddingField.setHorizontalAlignment(SwingConstants.CENTER);
		paddingField.setFont(new Font("Roboto", Font.PLAIN, 15));
		paddingField.setBounds(180, 125, 100, 20);
		optionPanel3.add(paddingField);

		// config file

		saveCheckBox7 = new JCheckBox("Save config file");
		saveCheckBox7.setBackground(new Color(252, 252, 252));
		saveCheckBox7.setFont(new Font("Roboto", Font.PLAIN, 15));
		saveCheckBox7.setBounds(0, 0, 400, 20);

		bigPanel.setLayout(null);

		JLabel instructionLabel0 = new JLabel(
				"<html>Import the superpixel motion tracks (MATLAB format) you wish to analyse, and then set your preferred options for computing the motion saliency map.</html>");
		instructionLabel0.setVerticalAlignment(SwingConstants.TOP);
		instructionLabel0.setHorizontalAlignment(SwingConstants.LEFT);
		instructionLabel0.setForeground(Color.DARK_GRAY);
		instructionLabel0.setFont(new Font("Roboto", Font.PLAIN, 15));
		instructionLabel0.setBounds(10, 40, 480, 60);
		add(instructionLabel0);

		// checkbox list
		CheckBoxList checkBoxList = new CheckBoxList(bigPanel);
		checkBoxList.addCheckBox(saveCheckBox1);
		checkBoxList.addPanelChild(saveCheckBox1, optionPanel1);
		checkBoxList.addCheckBoxChild(saveCheckBox1, saveCheckBox2);
		checkBoxList.addCheckBoxChild(saveCheckBox1, saveCheckBox3);
		checkBoxList.addPanelChild(saveCheckBox3, optionPanel2);
		checkBoxList.addCheckBoxChild(saveCheckBox1, saveCheckBox6);
		checkBoxList.addPanelChild(saveCheckBox6, optionPanel3);
		checkBoxList.addCheckBoxChild(saveCheckBox6, saveCheckBox4);
		checkBoxList.addPanelChild(saveCheckBox4, optionPanel4);
		checkBoxList.addCheckBox(saveCheckBox7);
		checkBoxList.show(0, 0);
	}

	public void import_motion_tracks(String s) {
		SaliencyMapParameters.resetMotionTracks();
		SelectMatlabFilesWindow selectPanel = new SelectMatlabFilesWindow(
				"<html>Select the superpixel motion tracks you want to use for extracting motion measurements</html>",
				s, SaliencyMapParameters.getBatchMode());
		SaliencyMapParameters.addMotionTracksFilePath(selectPanel.fileList);

		JFrame frame = new JFrame();
		frame.getContentPane().add(selectPanel);
		frame.pack();
		frame.setResizable(false);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);

		selectPanel.importButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				for (Pair<String, JCheckBox> checkbox : selectPanel.checkBoxList) {
					if (checkbox.getR().isSelected() && !checkbox.getR().getText().equals("metadata"))
						SaliencyMapParameters.addMotionTrackSubfile(checkbox.getL(), checkbox.getR().getText());
				}

				String importedFiles = "Selected tracks: <br>";
				List<String> subfiles = SaliencyMapParameters.getSubfilesList();
				for (String subfile : subfiles)
					importedFiles += subfile + "<br>";

				if (subfiles.size() > 0) {
					importedFilesLabel.setText("<html>" + importedFiles + "</html>");
					saveCheckBox1.setEnabled(true);
				} else {
					importedFilesLabel.setText("No files selected");
					saveCheckBox1.setEnabled(false);
					saveCheckBox1.setSelected(false);

					JFrame dialog = new JFrame();
					Object[] options = { "Ok" };
					JOptionPane.showOptionDialog(dialog, "No file imported.", "MOSES", JOptionPane.YES_NO_OPTION,
							JOptionPane.WARNING_MESSAGE, null, options, options[0]);
				}

				frame.dispose();
			}
		});

	}
}
