import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingConstants;

public class SaveOption {
	List<String> options;
	List<JRadioButton> radioButtonGroup;

	public SaveOption(List<String> o) {
		options = o;
	}

	public boolean getOption(String s) {
		for (JRadioButton r : radioButtonGroup)
			if (r.getText().equals(s))
				return r.isSelected();

		return false;
	}

	public List<String> getOptionList() {
		List<String> outputList = new ArrayList<String>();

		for (JRadioButton r : radioButtonGroup)
			if (r.isSelected())
				outputList.add(r.getText());

		return outputList;
	}

	public void show(JPanel panel, int x, int y, boolean vertical) {
		JLabel saveAsLabel = new JLabel("Save as:");
		saveAsLabel.setFont(new Font("Roboto", Font.PLAIN, 15));
		saveAsLabel.setVerticalAlignment(SwingConstants.CENTER);
		if (vertical)
			saveAsLabel.setBounds(x, y, 85, 18);
		else
			saveAsLabel.setBounds(x, y, 85, 40);
		panel.add(saveAsLabel);

		int buttonCount = 0;
		radioButtonGroup = new ArrayList<JRadioButton>();
		for (String option : options) {
			JRadioButton button = new JRadioButton(option);
			button.setSelected(false);

			button.setFont(new Font("Roboto", Font.PLAIN, 14));
			if (vertical) {
				button.setBounds(x, y + 18 + 22 * buttonCount, 85, 22);
				button.setVerticalAlignment(SwingConstants.TOP);
			} else {
				button.setBounds(x + 60 * (buttonCount + 1), y, 60, 40);
				button.setVerticalAlignment(SwingConstants.CENTER);
			}

			button.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					buttonsUpdate();
				}
			});

			panel.add(button);
			radioButtonGroup.add(button);

			buttonCount++;
		}
		radioButtonGroup.get(0).setSelected(true);
	}

	public void buttonsUpdate() {
		boolean selected = false;

		for (int i = 0; i < radioButtonGroup.size(); i++) {
			if (radioButtonGroup.get(i).isSelected())
				selected = true;
		}

		if (!selected)
			radioButtonGroup.get(0).setSelected(true);
	}
}
