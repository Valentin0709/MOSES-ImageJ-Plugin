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
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class FileSelecter extends JFrame {
	FileSelecter self = this;
	ArrayList<Pair<String, JCheckBox>> checkBoxList;
	JButton importButton;
	List<String> fileList;
	JPanel checkBoxPanel;
	JLabel instructionLabel;
	JScrollPane scrollPane;
	JProgressBar progressBar;
	private JButton selectAllButton;

	public FileSelecter() {

		super("MOSES file selecter");

		// set look and feel

		try {
			UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}

		getContentPane().setBackground(new Color(252, 252, 252));
		getContentPane().setLayout(null);

		this.setPreferredSize(new Dimension(600, 350));
		this.setResizable(false);
		this.setVisible(false);
		this.setLocationRelativeTo(null);

		progressBar = new JProgressBar();
		progressBar.setString("Looking for files...");
		progressBar.setBounds(75, 150, 430, 20);
		getContentPane().add(progressBar);
		progressBar.setVisible(false);
		progressBar.setIndeterminate(true);
		progressBar.setStringPainted(true);

		instructionLabel = new JLabel("");
		instructionLabel.setBounds(10, 10, 574, 40);
		instructionLabel.setVerticalAlignment(SwingConstants.TOP);
		instructionLabel.setFont(new Font("Roboto", Font.PLAIN, 15));
		getContentPane().add(instructionLabel);

		importButton = new JButton("Import ");
		importButton.setBounds(425, 282, 159, 28);
		importButton.setVerticalTextPosition(SwingConstants.CENTER);
		importButton.setHorizontalTextPosition(SwingConstants.CENTER);
		importButton.setForeground(Color.WHITE);
		importButton.setFont(new Font("Arial", Font.BOLD, 15));
		importButton.setBackground(new Color(13, 59, 102));
		getContentPane().add(importButton);

		scrollPane = new JScrollPane();
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setBounds(10, 60, 580, 210);
		scrollPane.setViewportBorder(null);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		getContentPane().add(scrollPane);

		checkBoxPanel = new JPanel();
		checkBoxPanel.setBackground(new Color(252, 252, 252));
		scrollPane.setViewportView(checkBoxPanel);
		checkBoxPanel.setLayout(null);

		selectAllButton = new JButton("Select all");
		selectAllButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				for (Pair<String, JCheckBox> pair : checkBoxList)
					pair.getR().setSelected(true);
			}
		});
		selectAllButton.setVerticalTextPosition(SwingConstants.CENTER);
		selectAllButton.setHorizontalTextPosition(SwingConstants.CENTER);
		selectAllButton.setForeground(Color.WHITE);
		selectAllButton.setFont(new Font("Arial", Font.BOLD, 15));
		selectAllButton.setBackground(new Color(13, 59, 102));
		selectAllButton.setBounds(12, 282, 159, 28);
		getContentPane().add(selectAllButton);

		pack();
	}

	public void setSelectAllButton(boolean b) {
		selectAllButton.setVisible(b);
	}

	public void listFiles(String path, List<String> validExtensions, boolean deepSearch, String instruction) {
		List<File> files = Globals.getFiles(new File(path).listFiles(), validExtensions, deepSearch);

		instructionLabel.setText("<html>" + instruction + "</html>");

		int y = 5;
		checkBoxList = new ArrayList<Pair<String, JCheckBox>>();
		for (File file : files) {
			String fileName = Globals.getName(file.getAbsolutePath());

			JCheckBox checkBox = new JCheckBox(fileName);
			checkBox.setFont(new Font("Roboto", Font.PLAIN, 14));
			checkBox.setBackground(new Color(252, 252, 252));
			checkBox.setVerticalAlignment(SwingConstants.CENTER);
			checkBox.setBounds(10, y, 300, 20);
			checkBoxList.add(new Pair<>(file.getAbsolutePath(), checkBox));
			checkBoxPanel.add(checkBox);
			y += 20;
		}

		checkBoxPanel.setPreferredSize(new Dimension(0, y + 10));

		if (files.size() == 0) {
			JLabel errorLabel = new JLabel("No files with the correct file format were found.");
			errorLabel.setFont(new Font("Roboto", Font.PLAIN, 14));
			errorLabel.setBackground(new Color(252, 252, 252));
			errorLabel.setForeground(new Color(252, 0, 0));
			errorLabel.setVerticalAlignment(SwingConstants.CENTER);
			errorLabel.setBounds(10, 5, 400, 20);
			checkBoxPanel.add(errorLabel);
		}
	}

	public void annotationList(String workspacePath, List<String> projectNames, String instruction) {
		instructionLabel.setText("<html>" + instruction + "</html>");
		progressBar.setVisible(true);
		checkBoxPanel.setVisible(false);

		class TrackListSwingWorker extends SwingWorker<String, String> {
			protected String doInBackground() throws Exception {
				int y = 5;
				checkBoxList = new ArrayList<Pair<String, JCheckBox>>();
				for (String projectName : projectNames) {

					JLabel fileNameLabel = new JLabel(projectName);
					fileNameLabel.setFont(new Font("Roboto", Font.BOLD, 14));
					fileNameLabel.setBackground(new Color(252, 252, 252));
					fileNameLabel.setVerticalAlignment(SwingConstants.CENTER);
					fileNameLabel.setBounds(5, y, 300, 20);
					checkBoxPanel.add(fileNameLabel);
					y += 20;

					String annotationFolderPath = workspacePath + "/" + projectName + "/annotations";
					File annotationFolder = new File(annotationFolderPath);

					List<File> annotationFiles = new ArrayList<File>();
					boolean first = true;
					if (annotationFolder.exists()) {
						annotationFiles = Globals.getFiles(annotationFolder.listFiles(), Arrays.asList(".csv"), true);

						for (File annotationFile : annotationFiles) {
							AnnotationMetadata annotationMetadata = new AnnotationMetadata(
									annotationFile.getAbsolutePath());

							if (annotationMetadata.getParentFile().equals(projectName)) {

								JCheckBox checkBox = new JCheckBox(Globals.getName(annotationFile.getAbsolutePath()));
								checkBox.setFont(new Font("Roboto", Font.PLAIN, 14));
								checkBox.setBackground(new Color(252, 252, 252));
								checkBox.setVerticalAlignment(SwingConstants.CENTER);
								checkBox.setBounds(10, y, 560, 20);

								checkBox.addActionListener(new ActionListener() {
									public void actionPerformed(ActionEvent e) {
										if (checkBox.isSelected()) {
											for (Pair<String, JCheckBox> cb : checkBoxList)
												if (checkBox != cb.getR()
														&& Globals.getName(annotationFile.getAbsolutePath())
																.equals(Globals.getName(cb.getL())))
													cb.getR().setSelected(false);
										} else {
											for (Pair<String, JCheckBox> cb : checkBoxList)
												if (Globals.getName(annotationFile.getAbsolutePath())
														.equals(Globals.getName(cb.getL()))) {
													cb.getR().setSelected(true);
													break;
												}
										}
									}
								});

								if (first) {
									checkBox.setSelected(true);
									first = false;
								}

								checkBoxList.add(new Pair<>(annotationFile.getAbsolutePath(), checkBox));
								checkBoxPanel.add(checkBox);
								y += 20;

								JLabel metadataInfoLabel = new JLabel(
										"<html>" + String.join(", ", annotationMetadata.metadataList()) + "</html>");
								metadataInfoLabel.setFont(new Font("Roboto", Font.PLAIN, 12));
								metadataInfoLabel.setBackground(new Color(252, 252, 252));
								metadataInfoLabel.setVerticalAlignment(SwingConstants.CENTER);
								metadataInfoLabel.setBounds(15, y, 560, 15);
								checkBoxPanel.add(metadataInfoLabel);
								y += 20;

							}
						}
					}

					if (annotationFiles.size() == 0) {
						JLabel errorLabel = new JLabel("No matching files were found.");
						errorLabel.setFont(new Font("Roboto", Font.PLAIN, 14));
						errorLabel.setBackground(new Color(252, 252, 252));
						errorLabel.setForeground(new Color(252, 0, 0));
						errorLabel.setVerticalAlignment(SwingConstants.CENTER);
						errorLabel.setBounds(10, y, 400, 20);
						checkBoxPanel.add(errorLabel);
						y += 20;
					}

					y += 10;
				}

				checkBoxPanel.setPreferredSize(new Dimension(0, y + 10));

				return "Done.";
			}

			protected void done() {
				progressBar.setVisible(false);
				checkBoxPanel.setVisible(true);
			}
		}
		TrackListSwingWorker trackListSwingWorker = new TrackListSwingWorker();
		trackListSwingWorker.execute();
	}

	public void tracksList(String workspacePath, List<String> projectNames, String instruction, boolean onlyOne) {
		instructionLabel.setText("<html>" + instruction + "</html>");
		progressBar.setVisible(true);
		checkBoxPanel.setVisible(false);

		class TrackListSwingWorker extends SwingWorker<String, String> {
			protected String doInBackground() throws Exception {
				int y = 5;
				checkBoxList = new ArrayList<Pair<String, JCheckBox>>();
				for (String projectName : projectNames) {

					JLabel fileNameLabel = new JLabel(projectName);
					fileNameLabel.setFont(new Font("Roboto", Font.BOLD, 14));
					fileNameLabel.setBackground(new Color(252, 252, 252));
					fileNameLabel.setVerticalAlignment(SwingConstants.CENTER);
					fileNameLabel.setBounds(5, y, 300, 20);
					checkBoxPanel.add(fileNameLabel);
					y += 20;

					String matlabFolderPath = workspacePath + "/" + projectName + "/data_analysis/matlab_files";
					File matlabFolder = new File(matlabFolderPath);

					List<File> tracksFiles = new ArrayList<File>();
					if (matlabFolder.exists()) {
						tracksFiles = Globals.getFiles(matlabFolder.listFiles(), Arrays.asList(".mat"), true);

						for (File tracksFile : tracksFiles) {
							MatlabMetadata tracksMetadata = new MatlabMetadata(tracksFile.getAbsolutePath());

							if (tracksMetadata.getParentFile().equals(projectName)
									&& tracksMetadata.getFileType().equals("tracks")) {

								JCheckBox checkBox = new JCheckBox(Globals.getName(tracksFile.getAbsolutePath()));
								checkBox.setFont(new Font("Roboto", Font.PLAIN, 14));
								checkBox.setBackground(new Color(252, 252, 252));
								checkBox.setVerticalAlignment(SwingConstants.CENTER);
								checkBox.setBounds(10, y, 560, 20);

								checkBox.addActionListener(new ActionListener() {
									public void actionPerformed(ActionEvent e) {
										if (checkBox.isSelected()) {
											for (Pair<String, JCheckBox> cb : checkBoxList) {
												if (onlyOne) {
													if (checkBox != cb.getR()) {
														cb.getR().setSelected(false);
													}

												} else if (checkBox != cb.getR() && Globals
														.getParentProject(tracksFile.getAbsolutePath(), workspacePath)
														.equals(Globals.getParentProject(cb.getL(), workspacePath))) {
													cb.getR().setSelected(false);
												}
											}
										}
									}
								});

								checkBoxList.add(new Pair<>(tracksFile.getAbsolutePath(), checkBox));
								checkBoxPanel.add(checkBox);
								y += 20;

								JLabel metadataInfoLabel = new JLabel("<html>"
										+ String.join(", ", tracksMetadata.tracksParametersList()) + "</html>");
								metadataInfoLabel.setFont(new Font("Roboto", Font.PLAIN, 12));
								metadataInfoLabel.setBackground(new Color(252, 252, 252));
								metadataInfoLabel.setVerticalAlignment(SwingConstants.CENTER);
								metadataInfoLabel.setBounds(15, y, 560, 45);
								checkBoxPanel.add(metadataInfoLabel);
								y += 50;

							}
						}
					}

					if (tracksFiles.size() == 0) {
						JLabel errorLabel = new JLabel("No tracks were found for this project.");
						errorLabel.setFont(new Font("Roboto", Font.PLAIN, 14));
						errorLabel.setBackground(new Color(252, 252, 252));
						errorLabel.setForeground(new Color(252, 0, 0));
						errorLabel.setVerticalAlignment(SwingConstants.CENTER);
						errorLabel.setBounds(10, y, 400, 20);
						checkBoxPanel.add(errorLabel);
						y += 20;
					}

					y += 10;
				}

				checkBoxPanel.setPreferredSize(new Dimension(0, y + 10));
				Globals.updatePanelSize(checkBoxPanel);

				return "Done.";
			}

			protected void done() {
				progressBar.setVisible(false);
				checkBoxPanel.setVisible(true);
			}
		}
		TrackListSwingWorker trackListSwingWorker = new TrackListSwingWorker();
		trackListSwingWorker.execute();
	}

	public void tracksAndMeshList(String workspacePath, List<String> projectNames, String instruction) {
		instructionLabel.setText("<html>" + instruction + "</html>");
		progressBar.setVisible(true);
		checkBoxPanel.setVisible(false);

		class TracksAndMeshList extends SwingWorker<String, String> {
			protected String doInBackground() throws Exception {
				int y = 5;
				checkBoxList = new ArrayList<Pair<String, JCheckBox>>();
				for (String projectName : projectNames) {

					JLabel fileNameLabel = new JLabel(projectName);
					fileNameLabel.setFont(new Font("Roboto", Font.BOLD, 14));
					fileNameLabel.setBackground(new Color(252, 252, 252));
					fileNameLabel.setVerticalAlignment(SwingConstants.CENTER);
					fileNameLabel.setBounds(5, y, 300, 20);
					checkBoxPanel.add(fileNameLabel);
					y += 20;

					String matlabFolderPath = workspacePath + "/" + projectName + "/data_analysis/matlab_files";
					File matlabFolder = new File(matlabFolderPath);

					List<File> matlabFiles = new ArrayList<File>();
					List<File> trackFiles = new ArrayList<File>();
					List<File> meshFiles = new ArrayList<File>();
					if (matlabFolder.exists()) {
						matlabFiles = Globals.getFiles(matlabFolder.listFiles(), Arrays.asList(".mat"), true);

						for (File matlabFile : matlabFiles) {
							MatlabMetadata matlabMetadata = new MatlabMetadata(matlabFile.getAbsolutePath());

							if (matlabMetadata.getParentFile().equals(projectName)
									&& matlabMetadata.getFileType().equals("tracks")) {
								trackFiles.add(matlabFile);
							}

							if (matlabMetadata.getParentFile().equals(projectName)
									&& matlabMetadata.getFileType().equals("MOSES_mesh")) {
								meshFiles.add(matlabFile);
							}
						}

						boolean ok = false;
						for (File meshFile : meshFiles) {
							MatlabMetadata meshFileMetadata = new MatlabMetadata(meshFile.getAbsolutePath());
							for (File tracksFile : trackFiles) {
								MatlabMetadata trackFileMetadata = new MatlabMetadata(tracksFile.getAbsolutePath());

								if (meshFileMetadata.tracksParametersList()
										.equals(trackFileMetadata.tracksParametersList())) {
									ok = true;

									JCheckBox checkBox = new JCheckBox(
											"<html>" + Globals.getName(meshFile.getAbsolutePath()) + "<br>"
													+ Globals.getName(tracksFile.getAbsolutePath()) + "</html>");
									checkBox.setFont(new Font("Roboto", Font.PLAIN, 14));
									checkBox.setBackground(new Color(252, 252, 252));
									checkBox.setVerticalAlignment(SwingConstants.CENTER);
									checkBox.setBounds(10, y, 560, 40);

									checkBox.addActionListener(new ActionListener() {
										public void actionPerformed(ActionEvent e) {
											if (checkBox.isSelected()) {
												for (Pair<String, JCheckBox> cb : checkBoxList)
													if (checkBox != cb.getR())
														cb.getR().setSelected(false);
											}
										}
									});

									checkBoxList.add(new Pair<>(
											meshFile.getAbsolutePath() + "," + tracksFile.getAbsolutePath(), checkBox));
									checkBoxPanel.add(checkBox);
									y += 40;

									JLabel metadataInfoLabel = new JLabel("<html>"
											+ String.join(", ", trackFileMetadata.tracksParametersList()) + "</html>");
									metadataInfoLabel.setFont(new Font("Roboto", Font.PLAIN, 12));
									metadataInfoLabel.setBackground(new Color(252, 252, 252));
									metadataInfoLabel.setVerticalAlignment(SwingConstants.CENTER);
									metadataInfoLabel.setBounds(15, y, 560, 45);
									checkBoxPanel.add(metadataInfoLabel);
									y += 45;

									JLabel metadataInfoLabel2 = new JLabel("MOSES mesh distance threshold = "
											+ meshFileMetadata.getMOSESMeshDistanceThreshold());
									metadataInfoLabel2.setFont(new Font("Roboto", Font.PLAIN, 12));
									metadataInfoLabel2.setBackground(new Color(252, 252, 252));
									metadataInfoLabel2.setVerticalAlignment(SwingConstants.CENTER);
									metadataInfoLabel2.setBounds(15, y, 560, 20);
									checkBoxPanel.add(metadataInfoLabel2);
									y += 25;

									break;
								}
							}
						}

						if (!ok) {
							JLabel errorLabel = new JLabel(
									"No compatible mesh - motion tracks pairs were found for this project.");
							errorLabel.setFont(new Font("Roboto", Font.PLAIN, 14));
							errorLabel.setBackground(new Color(252, 252, 252));
							errorLabel.setForeground(new Color(252, 0, 0));
							errorLabel.setVerticalAlignment(SwingConstants.CENTER);
							errorLabel.setBounds(10, y, 560, 20);
							checkBoxPanel.add(errorLabel);
							y += 20;
						}

						y += 10;
					}

				}

				checkBoxPanel.setPreferredSize(new Dimension(0, y + 10));

				return "Done.";
			}

			protected void done() {
				progressBar.setVisible(false);
				checkBoxPanel.setVisible(true);
			}
		}

		TracksAndMeshList tracksAndMeshListSwingWorker = new TracksAndMeshList();
		tracksAndMeshListSwingWorker.execute();

	}

	public List<String> getSelected() {
		List<String> result = new ArrayList<String>();
		for (Pair<String, JCheckBox> pair : checkBoxList)
			if (pair.getR().isSelected())
				result.add(pair.getL());

		return result;
	}
}
