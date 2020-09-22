import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.LineBorder;

import ij.IJ;

public class SaliencyMapPanel1 extends JLayeredPane {
	private MainFrame parentFrame;
	private SaliencyMapPanel1 self = this;

	private JLabel selectedTracksLabel, importedImagesLabel;
	private JPanel step2Panel;

	boolean ok1 = false, ok2 = false;

	public SaliencyMapPanel1(MainFrame parentFrame) {
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

		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
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
		cancelButton.setBounds(10, 430, 140, 30);
		add(cancelButton);

		JButton finishButton = new JButton("Finish");
		finishButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (ok1 && ok2) {
					parentFrame.empty();
					parentFrame.saliencyMapPanel2 = new SaliencyMapPanel2(parentFrame);
					parentFrame.getContentPane().add(parentFrame.saliencyMapPanel2);
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
		finishButton.setVerticalTextPosition(SwingConstants.CENTER);
		finishButton.setHorizontalTextPosition(SwingConstants.CENTER);
		finishButton.setForeground(Color.WHITE);
		finishButton.setFont(new Font("Arial", Font.BOLD, 15));
		finishButton.setBackground(new Color(13, 59, 102));
		finishButton.setBounds(350, 430, 140, 30);
		add(finishButton);

		JPanel step1Panel = new JPanel();
		step1Panel.setLayout(null);
		step1Panel.setBorder(new LineBorder(new Color(0, 0, 0), 2));
		step1Panel.setBackground(new Color(252, 252, 252));
		step1Panel.setBounds(10, 45, 480, 150);
		add(step1Panel);

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
						SaliencyMapParameters.setWorkspace(workspacePath);

						FileSelecter selecter = new FileSelecter();
						selecter.setSelectAllButton(false);
						selecter.setVisible(true);
						selecter.tracksList(SaliencyMapParameters.getWorkspace(), Globals.getProjectList(workspacePath),
								"Select the motion tracks you want to use for generating the motion saliency maps.");

						selecter.importButton.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								List<String> tracksPaths = selecter.getSelected();
								selecter.dispose();

								SaliencyMapParameters.setTracksPaths(tracksPaths);

								if (tracksPaths.size() > 0)
									showSelectedTracks();
							}
						});
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

		JLabel step1Label = new JLabel("STEP 1");
		step1Label.setVerticalAlignment(SwingConstants.TOP);
		step1Label.setHorizontalAlignment(SwingConstants.LEFT);
		step1Label.setForeground(Color.DARK_GRAY);
		step1Label.setFont(new Font("Roboto", Font.BOLD, 15));
		step1Label.setBounds(5, 5, 53, 30);
		step1Panel.add(step1Label);

		JLabel instructionLabel1 = new JLabel("<html>Import MOSES motion tracks</html>");
		instructionLabel1.setVerticalAlignment(SwingConstants.TOP);
		instructionLabel1.setFont(new Font("Roboto", Font.PLAIN, 15));
		instructionLabel1.setBounds(60, 5, 415, 20);
		step1Panel.add(instructionLabel1);

		JScrollPane scrollPane1 = new JScrollPane();
		scrollPane1.setViewportBorder(null);
		scrollPane1.setBackground(new Color(252, 252, 252));
		scrollPane1.setBounds(5, 50, 470, 95);
		step1Panel.add(scrollPane1);

		selectedTracksLabel = new JLabel("No files selected");
		selectedTracksLabel.setVerticalAlignment(SwingConstants.TOP);
		selectedTracksLabel.setHorizontalAlignment(SwingConstants.LEFT);
		selectedTracksLabel.setForeground(Color.DARK_GRAY);
		selectedTracksLabel.setFont(new Font("Roboto", Font.PLAIN, 15));
		selectedTracksLabel.setBorder(null);
		selectedTracksLabel.setBackground(new Color(252, 252, 252));
		scrollPane1.setViewportView(selectedTracksLabel);

		step2Panel = new JPanel();
		step2Panel.setLayout(null);
		step2Panel.setBorder(new LineBorder(new Color(0, 0, 0), 2));
		step2Panel.setBackground(new Color(252, 252, 252));
		step2Panel.setBounds(10, 202, 480, 216);
		add(step2Panel);

		JButton importImagesButton = new JButton("Import images");
		importImagesButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String imageDirectoryPath = IJ.getDirectory("Choose image directory");

				if (imageDirectoryPath != null) {
					List<File> imageFiles = Globals.getFiles(new File(imageDirectoryPath).listFiles(),
							Arrays.asList(".tif", ".tiff"), false);

					List<String> imagePaths = new ArrayList<String>();
					for (File f : imageFiles)
						imagePaths.add(f.getAbsolutePath());

					SaliencyMapParameters.setImagePaths(imagePaths);

					List<String> errorList = SaliencyMapParameters.noImageMatch();
					if (errorList.size() > 0) {
						JFrame dialog = new JFrame();
						Object[] options = { "Ok" };
						JOptionPane.showOptionDialog(dialog, "No image imported for " + String.join(", ", errorList),
								"MOSES", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options,
								options[0]);
					}

					if (SaliencyMapParameters.getImagePaths().size() > 0)
						showSelectedImages();
				}
			}
		});
		importImagesButton.setVerticalTextPosition(SwingConstants.CENTER);
		importImagesButton.setHorizontalTextPosition(SwingConstants.CENTER);
		importImagesButton.setForeground(Color.WHITE);
		importImagesButton.setFont(new Font("Arial", Font.BOLD, 15));
		importImagesButton.setBackground(new Color(13, 59, 102));
		importImagesButton.setBounds(128, 65, 226, 20);
		step2Panel.add(importImagesButton);

		JLabel step2Label = new JLabel("STEP 2");
		step2Label.setVerticalAlignment(SwingConstants.TOP);
		step2Label.setHorizontalAlignment(SwingConstants.LEFT);
		step2Label.setForeground(Color.DARK_GRAY);
		step2Label.setFont(new Font("Roboto", Font.BOLD, 15));
		step2Label.setBounds(5, 5, 53, 30);
		step2Panel.add(step2Label);

		JLabel instructionLabel1_1 = new JLabel(
				"<html>Import the folder which contains the TIFF stacks used for computing the motion tracks. They will be paired up automatically to their corresponding projects. </html>");
		instructionLabel1_1.setVerticalAlignment(SwingConstants.TOP);
		instructionLabel1_1.setFont(new Font("Roboto", Font.PLAIN, 15));
		instructionLabel1_1.setBounds(60, 5, 415, 60);
		step2Panel.add(instructionLabel1_1);

		JScrollPane scrollPane2 = new JScrollPane();
		scrollPane2.setViewportBorder(null);
		scrollPane2.setBackground(new Color(252, 252, 252));
		scrollPane2.setBounds(5, 90, 470, 120);
		step2Panel.add(scrollPane2);

		importedImagesLabel = new JLabel("No files selected");
		importedImagesLabel.setVerticalAlignment(SwingConstants.TOP);
		importedImagesLabel.setHorizontalAlignment(SwingConstants.LEFT);
		importedImagesLabel.setForeground(Color.DARK_GRAY);
		importedImagesLabel.setFont(new Font("Roboto", Font.PLAIN, 15));
		importedImagesLabel.setBorder(null);
		importedImagesLabel.setBackground(new Color(252, 252, 252));
		scrollPane2.setViewportView(importedImagesLabel);

		Globals.setPanelEnabled(step2Panel, false);
		if (SaliencyMapParameters.getTracksPaths().size() > 0)
			showSelectedTracks();

		if (SaliencyMapParameters.getImagePaths().size() > 0)
			showSelectedImages();
	}

	public void showSelectedTracks() {
		String text = "Selected motion tracks: <br>" + String.join("<br>", SaliencyMapParameters.getTracksPaths());
		selectedTracksLabel.setText("<html>" + text + "</html>");

		Globals.setPanelEnabled(step2Panel, true);
		ok1 = true;
	}

	private void showSelectedImages() {
		String text = "Imported images: <br>" + String.join("<br>", SaliencyMapParameters.getImagePaths());

		List<String> errorList = SaliencyMapParameters.noImageMatch();
		if (errorList.size() > 0) {
			text += "<br> Warning! No image imported for the following projects: <br>"
					+ String.join("<br> ", errorList);
		}
		importedImagesLabel.setText("<html>" + text + "</html>");

		ok2 = true;
	}

}
