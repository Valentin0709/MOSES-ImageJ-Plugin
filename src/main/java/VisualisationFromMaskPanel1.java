import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JButton;
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

	private JPanel step1Panel, step2Panel;
	private JButton importMaskButton, importImageButton;
	private JLabel step1Label, step2Label, instructionLabel1, instructionLabel2, selectedTracksLabel,
			selectedImagesLabel;
	private JScrollPane scrollPane1, scrollPane2;

	boolean ok1, ok2 = false;

	public VisualisationFromMaskPanel1(MainFrame parentFrame) {
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

		this.setPreferredSize(new Dimension(500, 500));

		// set background color

		this.setBackground(new Color(252, 252, 252));

		// set layout

		setLayout(null);

		JLabel titleLabel = new JLabel("Custom visualisation", SwingConstants.CENTER);
		titleLabel.setVerticalTextPosition(SwingConstants.CENTER);
		titleLabel.setHorizontalTextPosition(SwingConstants.CENTER);
		titleLabel.setFont(new Font("Arial Black", Font.BOLD, 23));
		titleLabel.setBounds(0, 0, 500, 36);
		add(titleLabel);

		JButton nextButton = new JButton("Next");
		nextButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (ok1 && ok2) {
					parentFrame.empty();
					parentFrame.visualisationFromMaskPanel2 = new VisualisationFromMaskPanel2(parentFrame);
					parentFrame.getContentPane().add(parentFrame.visualisationFromMaskPanel2);
					parentFrame.validate();
				} else {
					// display error dialog box
					JFrame dialog = new JFrame();
					Object[] options = { "Ok" };
					JOptionPane.showOptionDialog(dialog, "Plese complete steps 1 and 2 before going to the next page.",
							"MOSES", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);

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
		step1Panel.setBounds(10, 45, 480, 150);
		add(step1Panel);
		step1Panel.setLayout(null);
		step1Panel.setBorder(new LineBorder(new Color(0, 0, 0), 2));
		step1Panel.setBackground(new Color(252, 252, 252));

		JButton selectWorkspaceButton = new JButton("Select motion tracks");
		selectWorkspaceButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFrame dialog = new JFrame();
				Object[] options = { "Ok" };
				int n = JOptionPane.showOptionDialog(dialog, "Plese select the workspace you want to work with.",
						"MOSES", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

				if (n == 0) {
					String workspacePath = Globals.getWorkspace();

					if (workspacePath != null) {
						VisualisationFromMaskParameters.setWorkspace(workspacePath);

						FileSelecter selecter = new FileSelecter();
						selecter.setSelectAllButton(false);
						selecter.setVisible(true);
						selecter.tracksList(VisualisationFromMaskParameters.getWorkspace(),
								Globals.getProjectList(workspacePath),
								"Select for each project the motion track you want to plot.");

						selecter.importButton.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								List<String> tracksPaths = selecter.getSelected();
								selecter.dispose();

								VisualisationFromMaskParameters.setTracksPaths(tracksPaths);

								if (tracksPaths.size() > 0)
									showSelectedTracks();

							}
						});
					} else {
						selectedTracksLabel.setText("<html>" + "No motion tracks selected" + "</html>");
						ok1 = false;

					}
				}
			}
		});
		selectWorkspaceButton.setVerticalTextPosition(SwingConstants.CENTER);
		selectWorkspaceButton.setHorizontalTextPosition(SwingConstants.CENTER);
		selectWorkspaceButton.setForeground(Color.WHITE);
		selectWorkspaceButton.setFont(new Font("Arial", Font.BOLD, 15));
		selectWorkspaceButton.setBackground(new Color(13, 59, 102));
		selectWorkspaceButton.setBounds(127, 25, 226, 20);
		step1Panel.add(selectWorkspaceButton);

		step1Label = new JLabel("STEP 1");
		step1Label.setVerticalAlignment(SwingConstants.TOP);
		step1Label.setHorizontalAlignment(SwingConstants.LEFT);
		step1Label.setForeground(Color.DARK_GRAY);
		step1Label.setFont(new Font("Roboto", Font.BOLD, 15));
		step1Label.setBounds(5, 5, 53, 30);
		step1Panel.add(step1Label);

		instructionLabel1 = new JLabel("<html>Select the motion tracks you want to visualise</html>");
		instructionLabel1.setVerticalAlignment(SwingConstants.TOP);
		instructionLabel1.setFont(new Font("Roboto", Font.PLAIN, 15));
		instructionLabel1.setBounds(60, 5, 415, 20);
		step1Panel.add(instructionLabel1);

		scrollPane1 = new JScrollPane();
		scrollPane1.setViewportBorder(null);
		scrollPane1.setBackground(new Color(252, 252, 252));
		scrollPane1.setBounds(5, 50, 470, 95);
		step1Panel.add(scrollPane1);

		selectedTracksLabel = new JLabel("No motion tracks selected");
		selectedTracksLabel.setVerticalAlignment(SwingConstants.TOP);
		selectedTracksLabel.setFont(new Font("Roboto", Font.PLAIN, 15));
		scrollPane1.setViewportView(selectedTracksLabel);

		step2Panel = new JPanel();
		step2Panel.setBounds(10, 200, 480, 220);
		add(step2Panel);
		step2Panel.setBorder(new LineBorder(new Color(0, 0, 0), 2));
		step2Panel.setBackground(new Color(252, 252, 252));
		step2Panel.setLayout(null);

		importImageButton = new JButton("Import images");
		importImageButton.setBounds(140, 80, 200, 20);
		step2Panel.add(importImageButton);
		importImageButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String imageDirectoryPath = IJ.getDirectory("Choose image directory");

				if (imageDirectoryPath != null) {
					List<File> imageFiles = Globals.getFiles(new File(imageDirectoryPath).listFiles(),
							Arrays.asList(".tif", ".tiff"), false);

					List<String> imagePaths = new ArrayList<String>();
					for (File f : imageFiles)
						imagePaths.add(f.getAbsolutePath());

					VisualisationFromMaskParameters.setImagePaths(imagePaths);

					List<String> errorList = VisualisationFromMaskParameters.noImageMatch();
					if (errorList.size() > 0) {
						JFrame dialog = new JFrame();
						Object[] options = { "Ok" };
						JOptionPane.showOptionDialog(dialog, "No image imported for " + String.join(", ", errorList),
								"MOSES", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options,
								options[0]);
					}

					if (VisualisationFromMaskParameters.getImagePaths().size() > 0)
						showSelectedImages();
					else {
						selectedImagesLabel.setText("<html>" + "No images imported" + "</html>");
						ok2 = false;
					}
				}
			}
		});
		importImageButton.setVerticalTextPosition(SwingConstants.CENTER);
		importImageButton.setHorizontalTextPosition(SwingConstants.CENTER);
		importImageButton.setForeground(Color.WHITE);
		importImageButton.setFont(new Font("Arial", Font.BOLD, 15));
		importImageButton.setBackground(new Color(13, 59, 102));

		step2Label = new JLabel("STEP 2");
		step2Label.setVerticalAlignment(SwingConstants.TOP);
		step2Label.setHorizontalAlignment(SwingConstants.LEFT);
		step2Label.setForeground(Color.DARK_GRAY);
		step2Label.setFont(new Font("Roboto", Font.BOLD, 15));
		step2Label.setBounds(5, 5, 53, 30);
		step2Panel.add(step2Label);

		instructionLabel2 = new JLabel(
				"<html>Import the folder which contains the TIFF stacks used for computing the motion tracks and creating the annotations. They will be paired up automatically to their corresponding projects.</html>");
		instructionLabel2.setVerticalAlignment(SwingConstants.TOP);
		instructionLabel2.setFont(new Font("Roboto", Font.PLAIN, 15));
		instructionLabel2.setBounds(60, 5, 415, 73);
		step2Panel.add(instructionLabel2);

		scrollPane2 = new JScrollPane();
		scrollPane2.setViewportBorder(null);
		scrollPane2.setBackground(new Color(252, 252, 252));
		scrollPane2.setBounds(5, 105, 470, 110);
		step2Panel.add(scrollPane2);

		selectedImagesLabel = new JLabel("No images imported");
		selectedImagesLabel.setVerticalAlignment(SwingConstants.TOP);
		selectedImagesLabel.setFont(new Font("Roboto", Font.PLAIN, 15));
		scrollPane2.setViewportView(selectedImagesLabel);

		Globals.setPanelEnabled(step2Panel, false);

		if (VisualisationFromMaskParameters.getTracksPaths().size() > 0)
			showSelectedTracks();

		if (VisualisationFromMaskParameters.getImagePaths().size() > 0)
			showSelectedImages();
	}

	public void showSelectedTracks() {
		String text = "Selected motion tracks: <br>"
				+ String.join("<br>", VisualisationFromMaskParameters.getTracksPaths());
		selectedTracksLabel.setText("<html>" + text + "</html>");

		Globals.setPanelEnabled(step2Panel, true);
		ok1 = true;
	}

	private void showSelectedImages() {
		String text = "Imported images: <br>" + String.join("<br>", VisualisationFromMaskParameters.getImagePaths());

		List<String> errorList = VisualisationFromMaskParameters.noImageMatch();
		if (errorList.size() > 0) {
			text += "<br> Warning! No image imported for the following projects: <br>"
					+ String.join("<br> ", errorList);
		}
		selectedImagesLabel.setText("<html>" + text + "</html>");

		ok2 = true;
	}
}
