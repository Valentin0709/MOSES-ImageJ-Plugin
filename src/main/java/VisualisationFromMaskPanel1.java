import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.LineBorder;

import ij.IJ;

public class VisualisationFromMaskPanel1 extends JPanel {
	private MainFrame parentFrame;
	private VisualisationFromMaskPanel1 self = this;

	private JPanel step1Panel, step2Panel, step3Panel;
	JButton importMotionTracksButton, importMaskButton, importImageButton;
	private JLabel step1Label;
	private JLabel instructionLabel1;
	private JScrollPane scrollPane;
	private JLabel selectedImagesLabel;
	private JLabel step2Label;
	private JLabel instructionLabel2;
	private JScrollPane scrollPane2;
	private JLabel selectedMasksLabel;
	private JLabel step3Label;
	private JLabel instructionLabel3;
	private JScrollPane scrollPane3;
	private JLabel selectedTracksLabel;
	private JCheckBox saveCheckBox1;
	private JCheckBox saveCheckBox2;

	boolean ok1, ok2, ok3;
	List<String> filePaths;;

	public VisualisationFromMaskPanel1(MainFrame parentFrame) {
		ok1 = ok2 = ok3 = false;

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

		JLabel titleLabel = new JLabel("Visualisation from mask", SwingConstants.CENTER);
		titleLabel.setVerticalTextPosition(SwingConstants.CENTER);
		titleLabel.setHorizontalTextPosition(SwingConstants.CENTER);
		titleLabel.setFont(new Font("Arial Black", Font.BOLD, 23));
		titleLabel.setBounds(0, 0, 500, 36);
		add(titleLabel);

		JButton nextButton = new JButton("Next");
		nextButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (ok1 && ok2 && ok3) {
					parentFrame.empty();
					parentFrame.visualisationFromMaskPanel2 = new VisualisationFromMaskPanel2(parentFrame);
					parentFrame.getContentPane().add(parentFrame.visualisationFromMaskPanel2);
					parentFrame.validate();
				} else {
					// display error dialog box
					JFrame dialog = new JFrame();
					Object[] options = { "Ok" };
					JOptionPane.showOptionDialog(dialog,
							"Plese complet the first 3 steps before going to the next page.", "MOSES",
							JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);

				}
			}
		});
		nextButton.setVerticalTextPosition(SwingConstants.CENTER);
		nextButton.setHorizontalTextPosition(SwingConstants.CENTER);
		nextButton.setForeground(Color.WHITE);
		nextButton.setFont(new Font("Arial", Font.BOLD, 15));
		nextButton.setBackground(new Color(13, 59, 102));
		nextButton.setBounds(350, 430, 140, 30);
		add(nextButton);

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

		step1Panel = new JPanel();
		step1Panel.setBorder(new LineBorder(new Color(0, 0, 0), 2));
		step1Panel.setBounds(10, 35, 480, 135);
		step1Panel.setBackground(new Color(252, 252, 252));
		add(step1Panel);
		step1Panel.setLayout(null);

		importImageButton = new JButton("Import image");
		importImageButton.setBounds(140, 55, 200, 20);
		step1Panel.add(importImageButton);
		importImageButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ArrayList<String> validExtensions = new ArrayList<String>();
				validExtensions.addAll(Arrays.asList(".tif", ".tiff"));

				if (!VisualisationFromMaskParameters.getBatchMode()) {
					String importedFilePath = Globals.openFile(parentFrame.ui, validExtensions, false);

					if (importedFilePath != null) {
						ok1 = true;

						VisualisationFromMaskParameters.setFilePath(importedFilePath);

						String text = "Selected files: <br>" + importedFilePath;
						selectedImagesLabel.setText("<html>" + text + "</html>");

						Globals.setPanelEnabled(step2Panel, true);
					} else {
						ok1 = false;

						Globals.setPanelEnabled(step2Panel, false);
						Globals.setPanelEnabled(step3Panel, false);

						JFrame dialog = new JFrame();
						Object[] options = { "Ok" };
						JOptionPane.showOptionDialog(dialog,
								"The selected file has an invalid file format. Please import a TIFF stack to continue.",
								"MOSES", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, options,
								options[0]);
					}
				} else {
					JFrame dialog = new JFrame();
					Object[] options = { "Ok" };
					JOptionPane.showOptionDialog(dialog,
							"Batch mode is on. Please select the folder which contains all the images you wish to analyse.",
							"MOSES", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);

					String saveDirectory = IJ.getDirectory("Choose saving directory");

					if (saveDirectory != null) {
						ok1 = true;

						FileSelecter selecter = new FileSelecter();
						selecter.listFiles(saveDirectory, validExtensions, false,
								"Select the images you wish to work with from the list below");
						selecter.setVisible(true);
						selecter.importButton.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								filePaths = selecter.getSelected();
								selecter.dispose();

								VisualisationFromMaskParameters.setFilePath(filePaths);

								String text = "Selected files: <br>" + String.join("<br>", filePaths);
								selectedImagesLabel.setText("<html>" + text + "</html>");

								Globals.setPanelEnabled(step2Panel, true);
							}
						});
					} else {
						ok1 = false;

						Globals.setPanelEnabled(step2Panel, false);
						Globals.setPanelEnabled(step3Panel, false);

						JFrame dialog2 = new JFrame();
						Object[] options2 = { "Ok" };
						JOptionPane.showOptionDialog(dialog2,
								"The selected file has an invalid file format. Please import a TIFF stack to continue.",
								"MOSES", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, options2,
								options2[0]);
					}
				}
			}
		});
		importImageButton.setVerticalTextPosition(SwingConstants.CENTER);
		importImageButton.setHorizontalTextPosition(SwingConstants.CENTER);
		importImageButton.setForeground(Color.WHITE);
		importImageButton.setFont(new Font("Arial", Font.BOLD, 15));
		importImageButton.setBackground(new Color(13, 59, 102));

		step1Label = new JLabel("STEP 1");
		step1Label.setVerticalAlignment(SwingConstants.TOP);
		step1Label.setHorizontalAlignment(SwingConstants.LEFT);
		step1Label.setForeground(Color.DARK_GRAY);
		step1Label.setFont(new Font("Roboto", Font.BOLD, 15));
		step1Label.setBounds(5, 5, 53, 30);
		step1Panel.add(step1Label);

		instructionLabel1 = new JLabel(
				"<html>Import the TIFF stack that whas used for computing the motion tracks and creating the mask image.</html>");
		instructionLabel1.setVerticalAlignment(SwingConstants.TOP);
		instructionLabel1.setFont(new Font("Roboto", Font.PLAIN, 15));
		instructionLabel1.setBounds(60, 5, 415, 43);
		step1Panel.add(instructionLabel1);

		scrollPane = new JScrollPane();
		scrollPane.setViewportBorder(null);
		scrollPane.setBackground(new Color(252, 252, 252));
		scrollPane.setBounds(5, 80, 470, 50);
		step1Panel.add(scrollPane);

		selectedImagesLabel = new JLabel("No selected files");
		selectedImagesLabel.setVerticalAlignment(SwingConstants.TOP);
		selectedImagesLabel.setFont(new Font("Roboto", Font.PLAIN, 15));
		scrollPane.setViewportView(selectedImagesLabel);

		step2Panel = new JPanel();
		step2Panel.setBorder(new LineBorder(new Color(0, 0, 0), 2));
		step2Panel.setBounds(10, 175, 480, 110);
		step2Panel.setBackground(new Color(252, 252, 252));
		add(step2Panel);
		step2Panel.setLayout(null);

		JButton importMaskButton = new JButton("Import mask");
		importMaskButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ArrayList<String> validExtensions = new ArrayList<String>();
				validExtensions.addAll(Arrays.asList(".png", ".jpeg"));

				if (!VisualisationFromMaskParameters.getBatchMode()) {
					String importedFilePath = Globals.openFile(parentFrame.ui, validExtensions, false);

					if (importedFilePath != null) {
						if (VisualisationFromMaskParameters.setMaskPath(importedFilePath, false, true)) {
							ok2 = true;

							String text = "Selected files: <br>" + importedFilePath;
							selectedMasksLabel.setText("<html>" + text + "</html>");

							Globals.setPanelEnabled(step3Panel, true);
						} else {
							ok2 = false;

							// display error dialog box
							JFrame dialog = new JFrame();
							Object[] options = { "Ok" };
							JOptionPane.showOptionDialog(dialog, "The selected mask doesn't match the image imported",
									"MOSES", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, options,
									options[0]);
						}
					} else {
						ok2 = false;

						Globals.setPanelEnabled(step3Panel, false);

						// display error dialog box
						JFrame dialog = new JFrame();
						Object[] options = { "Ok" };
						JOptionPane.showOptionDialog(dialog,
								"The selected file has an invalid file format. Please import an image to continue.",
								"MOSES", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, options,
								options[0]);
					}
				} else {
					JFrame dialog = new JFrame();
					Object[] options = { "Ok" };
					JOptionPane.showOptionDialog(dialog,
							"Batch mode is on. Please select the folder which contains all the masks you wish to use.",
							"MOSES", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);

					String saveDirectory = IJ.getDirectory("Choose saving directory");

					if (saveDirectory != null) {
						ok2 = true;

						FileSelecter selecter = new FileSelecter();
						selecter.maskList(saveDirectory, VisualisationFromMaskParameters.getImagePaths(),
								validExtensions, false, "Select the masks you wish to use from the list below");
						selecter.setVisible(true);
						selecter.importButton.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								filePaths = selecter.getSelected();
								selecter.dispose();

								for (String filePath : filePaths)
									VisualisationFromMaskParameters.setMaskPath(filePath, true, true);

								String text = "Selected files: <br>" + String.join("<br>", filePaths);
								selectedMasksLabel.setText("<html>" + text + "</html>");

								Globals.setPanelEnabled(step3Panel, true);
							}
						});
					} else {
						ok2 = false;

						Globals.setPanelEnabled(step3Panel, false);

						JFrame dialog2 = new JFrame();
						Object[] options2 = { "Ok" };
						JOptionPane.showOptionDialog(dialog2,
								"The selected file has an invalid file format. Please import a TIFF stack to continue.",
								"MOSES", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, options2,
								options2[0]);
					}

				}
			}
		});
		importMaskButton.setBounds(140, 25, 200, 20);
		step2Panel.add(importMaskButton);
		importMaskButton.setVerticalTextPosition(SwingConstants.CENTER);
		importMaskButton.setHorizontalTextPosition(SwingConstants.CENTER);
		importMaskButton.setForeground(Color.WHITE);
		importMaskButton.setFont(new Font("Arial", Font.BOLD, 15));
		importMaskButton.setBackground(new Color(13, 59, 102));

		step3Panel = new JPanel();
		step3Panel.setBorder(new LineBorder(new Color(0, 0, 0), 2));
		step3Panel.setBounds(10, 290, 480, 110);
		step3Panel.setBackground(new Color(252, 252, 252));
		add(step3Panel);
		step3Panel.setLayout(null);

		importMotionTracksButton = new JButton("Import motion tracks");
		importMotionTracksButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ArrayList<String> validExtensions = new ArrayList<String>();
				validExtensions.addAll(Arrays.asList(".mat"));

				if (!VisualisationFromMaskParameters.getBatchMode()) {
					String importedFilePath = Globals.openFile(parentFrame.ui, validExtensions, false);

					if (importedFilePath != null) {
						if (VisualisationFromMaskParameters.setTrackPath(importedFilePath)) {
							ok3 = true;

							String text = "Selected files: <br>" + importedFilePath;
							selectedTracksLabel.setText("<html>" + text + "</html>");
						} else {
							ok3 = false;

							// display error dialog box
							JFrame dialog = new JFrame();
							Object[] options = { "Ok" };
							JOptionPane.showOptionDialog(dialog,
									"The selected MATLAB file doesn't contain motion tracks that match the image imported",
									"MOSES", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, options,
									options[0]);
						}
					} else {
						ok3 = false;

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
		importMotionTracksButton.setBounds(140, 25, 200, 20);
		step3Panel.add(importMotionTracksButton);
		importMotionTracksButton.setVerticalTextPosition(SwingConstants.CENTER);
		importMotionTracksButton.setHorizontalTextPosition(SwingConstants.CENTER);
		importMotionTracksButton.setForeground(Color.WHITE);
		importMotionTracksButton.setFont(new Font("Arial", Font.BOLD, 15));
		importMotionTracksButton.setBackground(new Color(13, 59, 102));

		step2Label = new JLabel("STEP 2");
		step2Label.setVerticalAlignment(SwingConstants.TOP);
		step2Label.setHorizontalAlignment(SwingConstants.LEFT);
		step2Label.setForeground(Color.DARK_GRAY);
		step2Label.setFont(new Font("Roboto", Font.BOLD, 15));
		step2Label.setBounds(5, 5, 53, 30);
		step2Panel.add(step2Label);

		instructionLabel2 = new JLabel("<html>Import the corresponding image mask.</html>");
		instructionLabel2.setVerticalAlignment(SwingConstants.TOP);
		instructionLabel2.setFont(new Font("Roboto", Font.PLAIN, 15));
		instructionLabel2.setBounds(60, 5, 415, 20);
		step2Panel.add(instructionLabel2);

		scrollPane2 = new JScrollPane();
		scrollPane2.setViewportBorder(null);
		scrollPane2.setBackground(new Color(252, 252, 252));
		scrollPane2.setBounds(5, 50, 470, 55);
		step2Panel.add(scrollPane2);

		selectedMasksLabel = new JLabel("No selected files");
		selectedMasksLabel.setVerticalAlignment(SwingConstants.TOP);
		selectedMasksLabel.setFont(new Font("Roboto", Font.PLAIN, 15));
		scrollPane2.setViewportView(selectedMasksLabel);

		step3Label = new JLabel("STEP 3");
		step3Label.setVerticalAlignment(SwingConstants.TOP);
		step3Label.setHorizontalAlignment(SwingConstants.LEFT);
		step3Label.setForeground(Color.DARK_GRAY);
		step3Label.setFont(new Font("Roboto", Font.BOLD, 15));
		step3Label.setBounds(5, 5, 53, 30);
		step3Panel.add(step3Label);

		instructionLabel3 = new JLabel("<html>Import the motion tracks you wish to plot.</html>");
		instructionLabel3.setVerticalAlignment(SwingConstants.TOP);
		instructionLabel3.setFont(new Font("Roboto", Font.PLAIN, 15));
		instructionLabel3.setBounds(60, 5, 415, 20);
		step3Panel.add(instructionLabel3);

		scrollPane3 = new JScrollPane();
		scrollPane3.setViewportBorder(null);
		scrollPane3.setBackground(new Color(252, 252, 252));
		scrollPane3.setBounds(5, 50, 470, 55);
		step3Panel.add(scrollPane3);

		selectedTracksLabel = new JLabel("No selected files");
		selectedTracksLabel.setVerticalAlignment(SwingConstants.TOP);
		selectedTracksLabel.setFont(new Font("Roboto", Font.PLAIN, 15));
		scrollPane3.setViewportView(selectedTracksLabel);

		Globals.setPanelEnabled(step2Panel, false);
		Globals.setPanelEnabled(step3Panel, false);
	}
}
