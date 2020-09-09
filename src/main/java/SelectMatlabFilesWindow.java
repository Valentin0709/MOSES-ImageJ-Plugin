import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;

public class SelectMatlabFilesWindow extends JPanel {
	ArrayList<Pair<String, JCheckBox>> checkBoxList;
	JButton importButton;
	List<String> fileList;

	public SelectMatlabFilesWindow(String message, String path, boolean batchMode) {

		this.setPreferredSize(new Dimension(400, 220));
		this.setBackground(new Color(252, 252, 252));
		setLayout(null);

		JLabel instructionLabel = new JLabel(message);
		instructionLabel.setBounds(10, 10, 380, 40);
		instructionLabel.setVerticalAlignment(SwingConstants.TOP);
		instructionLabel.setFont(new Font("Roboto", Font.PLAIN, 15));
		add(instructionLabel);

		importButton = new JButton("Import files");
		importButton.setBounds(102, 181, 196, 28);
		importButton.setVerticalTextPosition(SwingConstants.CENTER);
		importButton.setHorizontalTextPosition(SwingConstants.CENTER);
		importButton.setForeground(Color.WHITE);
		importButton.setFont(new Font("Arial", Font.BOLD, 15));
		importButton.setBackground(new Color(13, 59, 102));
		add(importButton);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(10, 60, 380, 110);
		scrollPane.setViewportBorder(null);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		add(scrollPane);

		JPanel checkBoxPanel = new JPanel();
		checkBoxPanel.setBackground(new Color(252, 252, 252));
		scrollPane.setViewportView(checkBoxPanel);
		checkBoxPanel.setLayout(null);

		// generate checkbox list

		ArrayList<String> validExtensions = new ArrayList<String>();
		validExtensions.addAll(Arrays.asList(".mat"));
		fileList = new ArrayList<String>();

		if (batchMode) {
			File fileDirectory = new File(Globals.getDirectory(path));
			String[] files = fileDirectory.list();
			for (String fileNames : files) {
				File selectedFile = new File(fileDirectory.getPath(), fileNames);
				if (selectedFile.isFile() && Globals.checkExtension(selectedFile.getAbsolutePath(), validExtensions))
					fileList.add(selectedFile.getAbsolutePath());
			}
		} else
			fileList.add(path);

		int y = 5;
		checkBoxList = new ArrayList<Pair<String, JCheckBox>>();
		for (String fileName : fileList) {
			List<String> subfileNames = Globals.getMatlabFiles(fileName);

			JLabel fileNameLabel = new JLabel(Globals.getName(fileName));
			fileNameLabel.setFont(new Font("Roboto", Font.BOLD, 14));
			fileNameLabel.setBackground(new Color(252, 252, 252));
			fileNameLabel.setVerticalAlignment(SwingConstants.CENTER);
			fileNameLabel.setBounds(5, y, 300, 20);
			checkBoxPanel.add(fileNameLabel);
			y += 20;

			for (int i = 0; i < subfileNames.size(); i++) {
				JCheckBox checkBox = new JCheckBox(subfileNames.get(i));
				checkBox.setFont(new Font("Roboto", Font.PLAIN, 14));
				checkBox.setBackground(new Color(252, 252, 252));
				checkBox.setVerticalAlignment(SwingConstants.CENTER);
				checkBox.setBounds(10, y, 300, 20);
				y += 20;
				if (subfileNames.get(i).equals("metadata")) {
					checkBox.setSelected(true);
					checkBox.setEnabled(false);
				} else
					checkBox.setSelected(false);

				checkBoxList.add(new Pair<>(fileName, checkBox));
				checkBoxPanel.add(checkBox);
			}

			y += 10;
		}

		checkBoxPanel.setPreferredSize(new Dimension(0, y + 10));
	}
}
