import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;

public class SelectMatlabFilesWindow extends JPanel {
	List<JCheckBox> checkBoxList;
	JButton importButton;

	public SelectMatlabFilesWindow(String s, List<String> fileNames) {

		this.setPreferredSize(new Dimension(400, 220));
		this.setBackground(new Color(252, 252, 252));
		setLayout(null);

		JLabel instructionLabel = new JLabel(s);
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

		checkBoxList = new ArrayList<JCheckBox>();
		for (int i = 0; i < fileNames.size(); i++) {
			JCheckBox checkBox = new JCheckBox(fileNames.get(i));
			checkBox.setSelected(false);
			checkBox.setFont(new Font("Roboto", Font.PLAIN, 14));
			checkBox.setBackground(new Color(252, 252, 252));
			checkBox.setVerticalAlignment(SwingConstants.CENTER);
			checkBox.setBounds(10, 5 + i * 20, 300, 20);

			checkBoxList.add(checkBox);
			checkBoxPanel.add(checkBox);
		}
	}
}
