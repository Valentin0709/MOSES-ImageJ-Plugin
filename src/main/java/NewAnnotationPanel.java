import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
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

public class NewAnnotationPanel extends JLayeredPane {
	private MainFrame parentFrame;
	private NewAnnotationPanel self = this;

	JPanel bigPanel;
	JScrollPane scrollPane;
	JComboBox labelColorComboBox, strokeColorComboBox;
	JCheckBox overlayCheckBox, maskCheckBox, showTracksCheckBox, annotationCheckBox, boundingBoxCheckBox;

	public NewAnnotationPanel(MainFrame parentFrame) {
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

		scrollPane = new JScrollPane();
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setBounds(20, 63, 460, 152);
		add(scrollPane);

		bigPanel = new JPanel();
		bigPanel.setBorder(null);
		scrollPane.setViewportView(bigPanel);
		bigPanel.setBackground(new Color(252, 252, 252));
		bigPanel.setLayout(null);

		// progress panel

		ProgressPanel progress = new ProgressPanel(self, 40, 200);
		self.add(progress);
		self.setLayer(progress, 1);
		progress.setVisibility(false);

		// display options

		JPanel displayOptionsPanel = new JPanel();
		displayOptionsPanel.setLayout(null);
		displayOptionsPanel.setBounds(16, 245, 468, 50);
		add(displayOptionsPanel);

		JLabel displayOptionLabel = new JLabel("Display options");
		displayOptionLabel.setFont(new Font("Roboto", Font.BOLD, 15));
		displayOptionLabel.setBounds(5, 5, 177, 20);
		displayOptionsPanel.add(displayOptionLabel);

		JLabel labelColorLabel = new JLabel("Label color");
		labelColorLabel.setBounds(118, 5, 99, 16);
		displayOptionsPanel.add(labelColorLabel);
		labelColorLabel.setFont(new Font("Roboto", Font.PLAIN, 15));

		labelColorComboBox = new JComboBox();
		labelColorComboBox.setBounds(118, 22, 130, 25);
		displayOptionsPanel.add(labelColorComboBox);
		labelColorComboBox.setFont(new Font("Roboto", Font.PLAIN, 15));
		labelColorComboBox.setModel(
				new DefaultComboBoxModel(new String[] { "white", "black", "red", "yellow", "green", "blue" }));
		labelColorComboBox.setSelectedIndex(0);

		JLabel strokeColorLabel = new JLabel("Stroke color");
		strokeColorLabel.setBounds(292, 5, 99, 16);
		displayOptionsPanel.add(strokeColorLabel);
		strokeColorLabel.setFont(new Font("Roboto", Font.PLAIN, 15));

		strokeColorComboBox = new JComboBox();
		strokeColorComboBox.setBounds(292, 22, 130, 25);
		displayOptionsPanel.add(strokeColorComboBox);
		strokeColorComboBox.setModel(
				new DefaultComboBoxModel(new String[] { "white", "black", "red", "yellow", "green", "blue" }));
		strokeColorComboBox.setSelectedIndex(0);
		strokeColorComboBox.setFont(new Font("Roboto", Font.PLAIN, 15));

		// roi manager
		RoiManager rm = new RoiManager(Globals.lastImage, bigPanel, labelColorComboBox, strokeColorComboBox, progress);

		JLabel titleLabel = new JLabel("Create annotation", SwingConstants.CENTER);
		titleLabel.setVerticalTextPosition(SwingConstants.CENTER);
		titleLabel.setHorizontalTextPosition(SwingConstants.CENTER);
		titleLabel.setFont(new Font("Arial Black", Font.BOLD, 23));
		titleLabel.setBounds(0, 0, 500, 36);
		add(titleLabel);

		JButton AddCurrentSelectionButton = new JButton("Add current selection");
		AddCurrentSelectionButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				rm.addLastRoi();
			}
		});
		AddCurrentSelectionButton.setVerticalTextPosition(SwingConstants.CENTER);
		AddCurrentSelectionButton.setHorizontalTextPosition(SwingConstants.CENTER);
		AddCurrentSelectionButton.setForeground(Color.WHITE);
		AddCurrentSelectionButton.setFont(new Font("Arial", Font.BOLD, 15));
		AddCurrentSelectionButton.setBackground(new Color(13, 59, 102));
		AddCurrentSelectionButton.setBounds(20, 219, 289, 20);
		add(AddCurrentSelectionButton);

		JButton saveButton = new JButton("Save annotations");
		saveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (annotationCheckBox.isSelected() || overlayCheckBox.isSelected() || maskCheckBox.isSelected()
						|| boundingBoxCheckBox.isSelected()) {
					if (rm.roiCount() > 0) {
						JFrame dialog = new JFrame();
						Object[] options = { "Ok" };
						int n = JOptionPane.showOptionDialog(dialog,
								"Plese select the workspace where you want to save the file.", "MOSES",
								JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

						if (n == 0) {
							String workspacePath = Globals.getWorkspace();

							if (workspacePath != null) {
								String annotationFolderPath = workspacePath + "/" + Globals.lastImage.getShortTitle()
										+ "/" + "annotations" + "/" + Globals.getFormattedDate();
								File annotationFolder = new File(annotationFolderPath);
								annotationFolder.mkdirs();

								if (annotationCheckBox.isSelected())
									rm.saveAnnotation(annotationFolderPath);

								if (overlayCheckBox.isSelected())
									rm.saveOverlay(annotationFolderPath);

								if (maskCheckBox.isSelected())
									rm.saveMask(annotationFolderPath);

								if (boundingBoxCheckBox.isSelected())
									rm.saveBoundingBox(annotationFolderPath);

								JOptionPane.showOptionDialog(dialog, "Completed save.", "MOSES",
										JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options,
										options[0]);

							}
						}
					} else {
						JFrame dialog = new JFrame();
						Object[] options = { "Ok" };
						JOptionPane.showOptionDialog(dialog, "No selections to save.", "MOSES",
								JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
					}

				}
			}
		});
		saveButton.setVerticalTextPosition(SwingConstants.CENTER);
		saveButton.setHorizontalTextPosition(SwingConstants.CENTER);
		saveButton.setForeground(Color.WHITE);
		saveButton.setFont(new Font("Arial", Font.BOLD, 15));
		saveButton.setBackground(new Color(13, 59, 102));
		saveButton.setBounds(327, 430, 163, 30);

		add(saveButton);

		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
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

		overlayCheckBox = new JCheckBox("Save snapshot (.png)");
		overlayCheckBox.setFont(new Font("Roboto", Font.PLAIN, 15));
		overlayCheckBox.setBounds(0, 383, 200, 24);
		overlayCheckBox.setBackground(new Color(252, 252, 252));
		add(overlayCheckBox);

		maskCheckBox = new JCheckBox("Save selection mask (.png)");
		maskCheckBox.setFont(new Font("Roboto", Font.PLAIN, 15));
		maskCheckBox.setBackground(new Color(252, 252, 252));
		maskCheckBox.setBounds(0, 404, 211, 24);
		add(maskCheckBox);

		JPanel advancedOptionsPanel = new JPanel();
		advancedOptionsPanel.setBounds(16, 300, 468, 67);
		add(advancedOptionsPanel);
		advancedOptionsPanel.setLayout(null);

		JLabel advancedOptionsLabel = new JLabel("Advanced options");
		advancedOptionsLabel.setBounds(5, 5, 177, 20);
		advancedOptionsLabel.setFont(new Font("Roboto", Font.BOLD, 15));
		advancedOptionsPanel.add(advancedOptionsLabel);

		JButton btnImportMotionTracks = new JButton("Import motion tracks");
		btnImportMotionTracks.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFrame dialog = new JFrame();
				Object[] options = { "Ok" };
				int n = JOptionPane.showOptionDialog(dialog, "Plese select the workspace you want to work with.",
						"MOSES", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

				if (n == 0) {
					String workspacePath = Globals.getWorkspace();

					if (workspacePath != null) {
						FileSelecter selecter = new FileSelecter();
						selecter.setSelectAllButton(false);
						selecter.setVisible(true);
						selecter.tracksList(workspacePath, Globals.getProjectList(workspacePath),
								"Select the motion tracks you want to import.", true);

						selecter.importButton.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								List<String> tracksPaths = selecter.getSelected();
								selecter.dispose();

								if (tracksPaths.size() == 1) {
									rm.setTracksPath(tracksPaths.get(0));
									rm.showTracks();

								}

							}
						});

						showTracksCheckBox.setEnabled(true);
					} else
						showTracksCheckBox.setEnabled(false);
				}
			}
		});
		btnImportMotionTracks.setVerticalTextPosition(SwingConstants.CENTER);
		btnImportMotionTracks.setHorizontalTextPosition(SwingConstants.CENTER);
		btnImportMotionTracks.setForeground(Color.WHITE);
		btnImportMotionTracks.setFont(new Font("Arial", Font.BOLD, 15));
		btnImportMotionTracks.setBackground(new Color(13, 59, 102));
		btnImportMotionTracks.setBounds(224, 5, 209, 20);
		advancedOptionsPanel.add(btnImportMotionTracks);

		showTracksCheckBox = new JCheckBox("Show superpixels");
		showTracksCheckBox.setEnabled(false);
		showTracksCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (showTracksCheckBox.isSelected()) {
					rm.overlayTracks();
				} else {
					rm.noTracksOverlay();
				}
			}
		});
		showTracksCheckBox.setFont(new Font("Roboto", Font.PLAIN, 15));
		showTracksCheckBox.setBounds(5, 25, 165, 24);
		advancedOptionsPanel.add(showTracksCheckBox);

		JLabel lblSelectASuperpixel = new JLabel("Select a superpixel to reveal the complete motion track.");
		lblSelectASuperpixel.setFont(new Font("Roboto", Font.PLAIN, 15));
		lblSelectASuperpixel.setBounds(5, 48, 451, 16);
		advancedOptionsPanel.add(lblSelectASuperpixel);

		JButton deleteButton = new JButton("Delete all");
		deleteButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				rm.deleteAll();
			}

		});
		deleteButton.setVerticalTextPosition(SwingConstants.CENTER);
		deleteButton.setHorizontalTextPosition(SwingConstants.CENTER);
		deleteButton.setForeground(Color.WHITE);
		deleteButton.setFont(new Font("Arial", Font.BOLD, 15));
		deleteButton.setBackground(new Color(13, 59, 102));
		deleteButton.setBounds(371, 219, 109, 20);

		add(deleteButton);

		JLabel lblNamingConvention = new JLabel("Naming convention:");
		lblNamingConvention.setFont(new Font("Roboto", Font.PLAIN, 15));
		lblNamingConvention.setBounds(20, 35, 154, 25);
		add(lblNamingConvention);

		JLabel lblSaveoptions = new JLabel("Save options");
		lblSaveoptions.setFont(new Font("Roboto", Font.BOLD, 15));
		lblSaveoptions.setBounds(0, 366, 177, 20);
		add(lblSaveoptions);

		annotationCheckBox = new JCheckBox("Save annotation coordinates (.csv)");
		annotationCheckBox.setFont(new Font("Roboto", Font.PLAIN, 15));
		annotationCheckBox.setBackground(new Color(252, 252, 252));
		annotationCheckBox.setBounds(220, 383, 272, 24);
		add(annotationCheckBox);

		boundingBoxCheckBox = new JCheckBox("Save normalized bounding boxes (.txt)");
		boundingBoxCheckBox.setFont(new Font("Roboto", Font.PLAIN, 15));
		boundingBoxCheckBox.setBackground(new Color(252, 252, 252));
		boundingBoxCheckBox.setBounds(220, 404, 289, 24);
		add(boundingBoxCheckBox);

		JComboBox namingConventionComboBox = new JComboBox();
		namingConventionComboBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (namingConventionComboBox.getSelectedIndex() == 0)
					rm.renameNumbers();
				if (namingConventionComboBox.getSelectedIndex() == 1)
					rm.renameLetters();
				if (namingConventionComboBox.getSelectedIndex() == 2)
					rm.renameClass();
			}
		});
		namingConventionComboBox
				.setModel(new DefaultComboBoxModel(new String[] { "numbers", "letters", "class name" }));
		namingConventionComboBox.setFont(new Font("Roboto", Font.PLAIN, 15));
		namingConventionComboBox.setBounds(164, 35, 130, 25);
		add(namingConventionComboBox);

		JButton importFileButton = new JButton("Import file");
		importFileButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFrame dialog = new JFrame();
				Object[] options = { "Ok" };
				int n = JOptionPane.showOptionDialog(dialog,
						"Select a previously generated file with the annotation coordinates (.csv) or the corresponding bounding boxes (.txt). ",
						"MOSES", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

				if (n == 0) {
					boolean ok = false;

					ArrayList<String> validExtensions = new ArrayList<String>();
					validExtensions.addAll(Arrays.asList(".csv", ".txt"));

					String importedFilePath = Globals.openFile(parentFrame.ui, validExtensions, false);
					if (importedFilePath != null) {
						String[] nameParts = Globals.getNameWithoutExtension(importedFilePath).split("_");

						if (Globals.getExtension(importedFilePath).equals(".csv") && nameParts.length == 3
								&& nameParts[1].equals("annotation")) {
							rm.setAnnotation(importedFilePath);
							ok = true;
						}

						if (Globals.getExtension(importedFilePath).equals(".txt") && nameParts.length == 4
								&& nameParts[1].equals("bounding")) {
							rm.setBoundingBox(importedFilePath);
							ok = true;
						}
					}

					if (!ok)
						JOptionPane.showOptionDialog(dialog, "Invalid file. ", "MOSES", JOptionPane.YES_NO_OPTION,
								JOptionPane.WARNING_MESSAGE, null, options, options[0]);
				}
			}
		});
		importFileButton.setVerticalTextPosition(SwingConstants.CENTER);
		importFileButton.setHorizontalTextPosition(SwingConstants.CENTER);
		importFileButton.setForeground(Color.WHITE);
		importFileButton.setFont(new Font("Arial", Font.BOLD, 15));
		importFileButton.setBackground(new Color(13, 59, 102));
		importFileButton.setBounds(212, 430, 109, 30);
		add(importFileButton);

	}
}
