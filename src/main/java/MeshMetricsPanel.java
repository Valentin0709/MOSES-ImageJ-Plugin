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

public class MeshMetricsPanel extends JLayeredPane {
	private MainFrame parentFrame;
	private MeshMetricsPanel self = this;

	private JLabel importedFilesLabel, instructionLabel, instructionLabel2, noteLabel;
	private JCheckBox saveCheckBox1, saveCheckBox2, saveCheckBox3, saveCheckBox4, saveCheckBox6, saveCheckBox11,
			saveCheckBox12;
	private JFormattedTextField lastFrameField;
	private JPanel optionPanel1, optionPanel2, optionPanel3, optionPanel4, bigPanel;
	private JScrollPane scrollPane2;

	SaveOption saveOption1, saveOption2;

	private MeshMetrics swingWorker;
	private boolean swingWorkerStarted = false;

	public MeshMetricsPanel(MainFrame parentFrame) {
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

		JLabel titleLabel = new JLabel("Mesh metrics", SwingConstants.CENTER);
		titleLabel.setVerticalTextPosition(SwingConstants.CENTER);
		titleLabel.setHorizontalTextPosition(SwingConstants.CENTER);
		titleLabel.setFont(new Font("Arial Black", Font.BOLD, 23));
		titleLabel.setBounds(0, 0, 500, 36);
		add(titleLabel);

		JButton importMotionTracksButton = new JButton("Import MOSES mesh");
		importMotionTracksButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ArrayList<String> validExtensions = new ArrayList<String>();
				validExtensions.addAll(Arrays.asList(".mat"));

				int n = 0;
				if (MeshMetricsParameters.getBatchMode()) {
					// display dialog box
					JFrame dialog = new JFrame();
					Object[] options = { "Cancel", "Import now" };
					n = JOptionPane.showOptionDialog(dialog,
							"Batch mode is enabled. Please select a file from the folder you want to analyse and input your preffered settings. MOSES will automatically import and compute the motion measurements for all the other files that have a valid format using the same settings.",
							"MOSES", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);
				}
				if (!MeshMetricsParameters.getBatchMode() || n == 1) {
					String importedFilePath = Globals.openFile(parentFrame.ui, validExtensions, false);

					if (importedFilePath != null) {
						import_MOSES_mesh(importedFilePath);
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
						MeshMetricsParameters.setSaveDirectory(saveDirectory);

						if (saveCheckBox1.isSelected()) {
							MeshMetricsParameters.setOutput("mesh_strain_curve");
							MeshMetricsParameters.setNormaliseValues(saveCheckBox11.isSelected());
							MeshMetricsParameters.setAverageValues(saveCheckBox12.isSelected());
						}

						if (saveCheckBox2.isSelected()) {
							MeshMetricsParameters.setOutput("stability_index");
							MeshMetricsParameters.setLastFrames(Integer.parseInt(lastFrameField.getText()));
						}

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

						swingWorker = new MeshMetrics(progress);
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
		bigPanel.setLayout(null);

		JLabel instructionLabel0 = new JLabel(
				"<html>Import the MOSES mesh file (MATLAB format) you wish to analyse, select the mesh strain variables, and then set your preferred options for computing the mesh metrics</html>");
		instructionLabel0.setVerticalAlignment(SwingConstants.TOP);
		instructionLabel0.setHorizontalAlignment(SwingConstants.LEFT);
		instructionLabel0.setForeground(Color.DARK_GRAY);
		instructionLabel0.setFont(new Font("Roboto", Font.PLAIN, 15));
		instructionLabel0.setBounds(10, 40, 480, 60);
		add(instructionLabel0);

		// mesh curve

		saveCheckBox1 = new JCheckBox("Mesh strain curve");
		saveCheckBox1.setEnabled(false);
		saveCheckBox1.setBackground(new Color(252, 252, 252));
		saveCheckBox1.setFont(new Font("Roboto", Font.PLAIN, 15));
		saveCheckBox1.setBounds(0, 0, 400, 20);

		optionPanel1 = new JPanel();
		optionPanel1.setBounds(0, 0, 450, 50);
		optionPanel1.setOpaque(false);
		optionPanel1.setBackground(new Color(238, 238, 238));
		optionPanel1.setLayout(null);

		saveCheckBox11 = new JCheckBox("Normalise values");
		saveCheckBox11.setBackground(new Color(238, 238, 238));
		saveCheckBox11.setFont(new Font("Roboto", Font.PLAIN, 15));
		saveCheckBox11.setBounds(5, 5, 400, 20);
		optionPanel1.add(saveCheckBox11);

		saveCheckBox12 = new JCheckBox("Compute average mesh strain curve");
		saveCheckBox12.setBackground(new Color(238, 238, 238));
		saveCheckBox12.setFont(new Font("Roboto", Font.PLAIN, 15));
		saveCheckBox12.setBounds(5, 25, 400, 20);
		optionPanel1.add(saveCheckBox12);

		// stability index

		saveCheckBox2 = new JCheckBox("Stability index");
		saveCheckBox2.setEnabled(false);
		saveCheckBox2.setBackground(new Color(252, 252, 252));
		saveCheckBox2.setFont(new Font("Roboto", Font.PLAIN, 15));
		saveCheckBox2.setBounds(0, 0, 400, 20);

		optionPanel2 = new JPanel();
		optionPanel2.setBounds(0, 0, 450, 65);
		optionPanel2.setOpaque(false);
		optionPanel2.setBackground(new Color(238, 238, 238));
		optionPanel2.setLayout(null);

		noteLabel = new JLabel(
				"<html> Note: The stability index is computed only for motion tracks saved together in the same MATLAB file </html>");
		noteLabel.setVisible(false);
		noteLabel.setVerticalAlignment(SwingConstants.TOP);
		noteLabel.setFont(new Font("Roboto", Font.PLAIN, 15));
		noteLabel.setBounds(5, 5, 450, 40);
		optionPanel2.add(noteLabel);

		instructionLabel = new JLabel("<html> Last frames </html>");
		instructionLabel.setVisible(false);
		instructionLabel.setVerticalAlignment(SwingConstants.TOP);
		instructionLabel.setFont(new Font("Roboto", Font.PLAIN, 15));
		instructionLabel.setBounds(5, 45, 450, 100);
		optionPanel2.add(instructionLabel);

		DecimalFormat integerFormat = new DecimalFormat("###");

		lastFrameField = new JFormattedTextField(integerFormat) {
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
		lastFrameField.setText(String.valueOf(MeshMetricsParameters.getLastFrames()));
		lastFrameField.setVisible(false);
		lastFrameField.setHorizontalAlignment(SwingConstants.CENTER);
		lastFrameField.setFont(new Font("Roboto", Font.PLAIN, 15));
		lastFrameField.setBounds(120, 45, 100, 20);
		optionPanel2.add(lastFrameField);

		// checkbox list
		CheckBoxList checkBoxList = new CheckBoxList(bigPanel);
		checkBoxList.addCheckBox(saveCheckBox1);
		checkBoxList.addPanelChild(saveCheckBox1, optionPanel1);
		checkBoxList.addCheckBox(saveCheckBox2);
		checkBoxList.addPanelChild(saveCheckBox2, optionPanel2);
		checkBoxList.show(0, 0);

	}

	public void import_MOSES_mesh(String s) {
		MeshMetricsParameters.resetMOSESMesh();
		SelectMatlabFilesWindow selectPanel = new SelectMatlabFilesWindow(
				"<html>Select the MOSES mesh strain you want to use for extracting motion measurements</html>", s,
				MeshMetricsParameters.getBatchMode());
		MeshMetricsParameters.addMOSESMeshFilePath(selectPanel.fileList);
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
						MeshMetricsParameters.addMOSESMeshSubfile(checkbox.getL(), checkbox.getR().getText());
				}

				String importedFiles = "Selected meshes: <br>";
				List<String> subfiles = MeshMetricsParameters.getSubfilesList();
				for (String subfile : subfiles)
					importedFiles += subfile + "<br>";

				if (subfiles.size() > 0) {
					importedFilesLabel.setText("<html>" + importedFiles + "</html>");
					saveCheckBox1.setEnabled(true);
					saveCheckBox2.setEnabled(true);
				} else {
					importedFilesLabel.setText("No files selected");
					saveCheckBox1.setEnabled(false);
					saveCheckBox2.setEnabled(false);
					saveCheckBox1.setSelected(false);
					saveCheckBox2.setSelected(false);

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
