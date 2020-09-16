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
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
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

		this.setPreferredSize(new Dimension(400, 250));
		this.setResizable(false);
		this.setVisible(false);
		this.setLocationRelativeTo(null);

		instructionLabel = new JLabel("");
		instructionLabel.setBounds(10, 10, 380, 40);
		instructionLabel.setVerticalAlignment(SwingConstants.TOP);
		instructionLabel.setFont(new Font("Roboto", Font.PLAIN, 15));
		getContentPane().add(instructionLabel);

		importButton = new JButton("Import files");
		importButton.setBounds(225, 182, 159, 28);
		importButton.setVerticalTextPosition(SwingConstants.CENTER);
		importButton.setHorizontalTextPosition(SwingConstants.CENTER);
		importButton.setForeground(Color.WHITE);
		importButton.setFont(new Font("Arial", Font.BOLD, 15));
		importButton.setBackground(new Color(13, 59, 102));
		getContentPane().add(importButton);

		scrollPane = new JScrollPane();
		scrollPane.setBounds(10, 60, 380, 110);
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
		selectAllButton.setBounds(10, 183, 159, 28);
		getContentPane().add(selectAllButton);

		pack();

//		// generate checkbox list
//
//		ArrayList<String> validExtensions = new ArrayList<String>();
//		validExtensions.addAll(Arrays.asList(".mat"));
//		fileList = new ArrayList<String>();
//
//		if (batchMode) {
//			File fileDirectory = new File(Globals.getDirectory(path));
//			String[] files = fileDirectory.list();
//			for (String fileNames : files) {
//				File selectedFile = new File(fileDirectory.getPath(), fileNames);
//				if (selectedFile.isFile() && Globals.checkExtension(selectedFile.getAbsolutePath(), validExtensions))
//					fileList.add(selectedFile.getAbsolutePath());
//			}
//		} else
//			fileList.add(path);
//
//		int y = 5;
//		checkBoxList = new ArrayList<Pair<String, JCheckBox>>();
//		for (String fileName : fileList) {
//			List<String> subfileNames = Globals.getMatlabFiles(fileName);
//
//			JLabel fileNameLabel = new JLabel(Globals.getName(fileName));
//			fileNameLabel.setFont(new Font("Roboto", Font.BOLD, 14));
//			fileNameLabel.setBackground(new Color(252, 252, 252));
//			fileNameLabel.setVerticalAlignment(SwingConstants.CENTER);
//			fileNameLabel.setBounds(5, y, 300, 20);
//			checkBoxPanel.add(fileNameLabel);
//			y += 20;
//
//			for (int i = 0; i < subfileNames.size(); i++) {
//				JCheckBox checkBox = new JCheckBox(subfileNames.get(i));
//				checkBox.setFont(new Font("Roboto", Font.PLAIN, 14));
//				checkBox.setBackground(new Color(252, 252, 252));
//				checkBox.setVerticalAlignment(SwingConstants.CENTER);
//				checkBox.setBounds(10, y, 300, 20);
//				y += 20;
//				if (subfileNames.get(i).equals("metadata")) {
//					checkBox.setSelected(true);
//					checkBox.setEnabled(false);
//				} else
//					checkBox.setSelected(false);
//
//				checkBoxList.add(new Pair<>(fileName, checkBox));
//				checkBoxPanel.add(checkBox);
//			}
//
//			y += 10;
//		}
//
//		checkBoxPanel.setPreferredSize(new Dimension(0, y + 10));
	}

	public void listFiles(String path, ArrayList<String> validExtensions, boolean deepSearch, String instruction) {
		List<File> files = Globals.getFiles(new File(path).listFiles(), Arrays.asList(".tif", ".tiff"), deepSearch);

		instructionLabel.setText(instruction);

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
	}

	public void maskList(String path, List<String> parentFiles, List<String> validExtensions, boolean deepSearch,
			String instruction) {
		List<File> files = Globals.getFiles(new File(path).listFiles(), Arrays.asList(".png", ".jpeg"), deepSearch);

		instructionLabel.setText(instruction);

		int y = 5;
		checkBoxList = new ArrayList<Pair<String, JCheckBox>>();
		for (String parentFile : parentFiles) {
			JLabel fileNameLabel = new JLabel(Globals.getName(parentFile));
			fileNameLabel.setFont(new Font("Roboto", Font.BOLD, 14));
			fileNameLabel.setBackground(new Color(252, 252, 252));
			fileNameLabel.setVerticalAlignment(SwingConstants.CENTER);
			fileNameLabel.setBounds(5, y, 300, 20);
			checkBoxPanel.add(fileNameLabel);
			y += 20;

			for (int i = 0; i < files.size(); i++) {
				String filePath = files.get(i).getAbsolutePath();

				List<JCheckBox> masks = new ArrayList<JCheckBox>();
				if (Globals.getName(filePath).split("_")[0]
						.equals(Globals.getNameWithoutExtension(parentFile).replace("_", ""))) {
					JCheckBox checkBox = new JCheckBox(Globals.getName(filePath));
					checkBox.setFont(new Font("Roboto", Font.PLAIN, 14));
					checkBox.setBackground(new Color(252, 252, 252));
					checkBox.setVerticalAlignment(SwingConstants.CENTER);
					checkBox.setBounds(10, y, 300, 20);

					checkBox.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							if (checkBox.isSelected()) {
								for (Pair<String, JCheckBox> cb : checkBoxList)
									if (checkBox != cb.getR() && Globals.getName(filePath).split("_")[0]
											.equals(Globals.getName(cb.getL()).split("_")[0]))
										cb.getR().setSelected(false);
							} else {
								for (Pair<String, JCheckBox> cb : checkBoxList)
									if (Globals.getName(filePath).split("_")[0]
											.equals(Globals.getName(cb.getL()).split("_")[0])) {
										cb.getR().setSelected(true);
										break;
									}
							}
						}
					});

					checkBoxList.add(new Pair<>(filePath, checkBox));
					checkBoxPanel.add(checkBox);
					y += 20;
				}
			}

			y += 10;
		}
		checkBoxPanel.setPreferredSize(new Dimension(0, y + 10));
	}

	public List<String> getSelected() {
		List<String> result = new ArrayList<String>();
		for (Pair<String, JCheckBox> pair : checkBoxList)
			if (pair.getR().isSelected())
				result.add(pair.getL());

		return result;
	}
}
