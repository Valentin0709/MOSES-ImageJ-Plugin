import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class ColorOption {
	List<JComboBox> comboBoxGroup;
	List<Integer> channels;

	public ColorOption(List<Integer> list) {
		channels = list;
	}

	public void show(JPanel panel, int x, int y, int rows) {
		comboBoxGroup = new ArrayList<JComboBox>();
		for (int i = 0; i < channels.size(); i++) {
			int channelIndex = channels.get(i);

			JComboBox comboBox = new JComboBox();
			comboBox.setModel(new DefaultComboBoxModel(
					new String[] { "red", "blue", "green", "cyan", "magenta", "yellow", "black", "white" }));

			comboBox.setBounds(x + 110 + (i / rows) * 200, y + (i % rows) * 25, 90, 23);
			comboBox.setFont(new Font("Roboto", Font.PLAIN, 15));
			comboBox.setSelectedIndex(i);
			comboBox.setVisible(false);

			JLabel label = new JLabel("Channel " + (channelIndex + 1) + " color: ");
			label.setVerticalAlignment(SwingConstants.TOP);
			label.setHorizontalAlignment(SwingConstants.LEFT);
			label.setFont(new Font("Roboto", Font.PLAIN, 15));
			label.setBounds(x + (i / rows) * 200, y + (i % rows) * 25, 180, 20);
			label.setForeground(Color.DARK_GRAY);
			label.setVisible(false);

			panel.add(comboBox);
			panel.add(label);
			comboBoxGroup.add(comboBox);
		}
	}

	public String getColor(int index) {
		int selected = comboBoxGroup.get(index).getSelectedIndex();

		if (selected == 0)
			return "r";
		if (selected == 1)
			return "b";
		if (selected == 2)
			return "g";
		if (selected == 3)
			return "c";
		if (selected == 4)
			return "m";
		if (selected == 5)
			return "y";
		if (selected == 5)
			return "w";
		return "k";

	}
}
