import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DecimalFormat;
import java.util.Arrays;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class VisualisationFromMaskPanel3 extends JLayeredPane {
	private MainFrame parentFrame;
	private VisualisationFromMaskPanel3 self = this;

	JCheckBox saveCheckBox1, saveCheckBox2, saveCheckBox3;
	JPanel optionPanel1, bigPanel;
	SaveOption saveOption;
	JFormattedTextField temporalSegmentField;

	VisualisationFromMask swingWorker;
	boolean swingWorkerStarted;

	public VisualisationFromMaskPanel3(MainFrame parentFrame) {
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

		JLabel titleLabel = new JLabel("Custom visualisation", SwingConstants.CENTER);
		titleLabel.setVerticalTextPosition(SwingConstants.CENTER);
		titleLabel.setHorizontalTextPosition(SwingConstants.CENTER);
		titleLabel.setFont(new Font("Arial Black", Font.BOLD, 23));
		titleLabel.setBounds(0, 0, 500, 36);
		add(titleLabel);

		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// stop swing worker
				if (swingWorkerStarted)
					swingWorker.destroy();

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
				if (saveCheckBox1.isSelected() || saveCheckBox2.isSelected() || saveCheckBox3.isSelected()) {

					if (saveCheckBox1.isSelected()) {
						VisualisationFromMaskParameters.setOutput("complete_visualisation");
						VisualisationFromMaskParameters.setSaveOption(saveOption);
						VisualisationFromMaskParameters
								.setTracksTemporalSegment(Integer.parseInt(temporalSegmentField.getText()));
					}

					if (saveCheckBox2.isSelected())
						VisualisationFromMaskParameters.setOutput("longest_track");

					if (saveCheckBox3.isSelected())
						VisualisationFromMaskParameters.setOutput("all_tracks");

					// loading bar panel
					ProgressPanel progress = new ProgressPanel(self, 40, 200);
					self.add(progress);
					self.setLayer(progress, 1);
					progress.setVisibility(true);

					Globals.setPanelEnabled(bigPanel, false);

					// run process
					class SwingWorkerListener implements PropertyChangeListener {
						@Override
						public void propertyChange(PropertyChangeEvent evt) {
							if (swingWorker.isDone()) {
								parentFrame.empty();
								parentFrame.menuPanel = new MenuPanel(parentFrame);
								parentFrame.getContentPane().add(parentFrame.menuPanel);
								parentFrame.validate();
							}

						}
					}

					swingWorker = new VisualisationFromMask(progress);
					swingWorkerStarted = true;
					swingWorker.addPropertyChangeListener(new SwingWorkerListener());
					swingWorker.execute();
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

		JButton backButton = new JButton("Back");
		backButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// stop swing worker
				if (swingWorkerStarted)
					swingWorker.destroy();

				parentFrame.empty();
				parentFrame.visualisationFromMaskPanel2 = new VisualisationFromMaskPanel2(parentFrame);
				parentFrame.getContentPane().add(parentFrame.visualisationFromMaskPanel2);
				parentFrame.validate();
			}
		});
		backButton.setVerticalTextPosition(SwingConstants.CENTER);
		backButton.setHorizontalTextPosition(SwingConstants.CENTER);
		backButton.setForeground(new Color(252, 252, 252));
		backButton.setFont(new Font("Arial", Font.BOLD, 15));
		backButton.setBackground(new Color(13, 59, 102));
		backButton.setBounds(200, 430, 140, 30);
		add(backButton);

		saveCheckBox1 = new JCheckBox("Create visualisation for all frames");
		saveCheckBox1.setFont(new Font("Roboto", Font.PLAIN, 15));
		saveCheckBox1.setBounds(0, 0, 400, 24);
		saveCheckBox1.setBackground(new Color(252, 252, 252));

		saveCheckBox3 = new JCheckBox("Plot all tracks for each region of interest (.png)");
		saveCheckBox3.setFont(new Font("Roboto", Font.PLAIN, 15));
		saveCheckBox3.setBounds(0, 0, 400, 24);
		saveCheckBox3.setBackground(new Color(252, 252, 252));

		saveCheckBox2 = new JCheckBox("Plot longest track for each region of interest (.png)");
		saveCheckBox2.setFont(new Font("Roboto", Font.PLAIN, 15));
		saveCheckBox2.setBounds(0, 0, 400, 24);
		saveCheckBox2.setBackground(new Color(252, 252, 252));

		optionPanel1 = new JPanel();
		optionPanel1.setOpaque(false);
		optionPanel1.setBounds(0, 0, 410, 100);
		optionPanel1.setBackground(new Color(238, 238, 238));
		optionPanel1.setLayout(null);

		saveOption = new SaveOption(Arrays.asList(".tif", ".avi", ".png"));
		saveOption.show(optionPanel1, 5, 0, false);

		JLabel temporalSegmentLabel = new JLabel("Plot tracks in temporal segments of length:");
		temporalSegmentLabel.setVisible(false);
		temporalSegmentLabel.setVerticalAlignment(SwingConstants.TOP);
		temporalSegmentLabel.setHorizontalAlignment(SwingConstants.LEFT);
		temporalSegmentLabel.setFont(new Font("Roboto", Font.PLAIN, 15));
		temporalSegmentLabel.setBounds(5, 40, 300, 20);
		optionPanel1.add(temporalSegmentLabel);

		DecimalFormat integerFormat = new DecimalFormat("###");
		integerFormat.setGroupingUsed(false);

		temporalSegmentField = new JFormattedTextField(integerFormat) {
			@Override
			protected void processFocusEvent(final FocusEvent e) {
				if (e.isTemporary()) {
					return;
				}

				if (e.getID() == FocusEvent.FOCUS_LOST) {
					if (getText() == null || getText().isEmpty()) {
						setValue(0);
					}
				}
				super.processFocusEvent(e);
			}
		};
		temporalSegmentField.setVisible(false);
		temporalSegmentField.setText(String.valueOf(VisualisationFromMaskParameters.getTracksTemporalSegment()));
		temporalSegmentField.setHorizontalAlignment(SwingConstants.CENTER);
		temporalSegmentField.setFont(new Font("Roboto", Font.PLAIN, 15));
		temporalSegmentField.setBounds(290, 40, 70, 20);
		optionPanel1.add(temporalSegmentField);

		JLabel noteLabel = new JLabel(
				"<html>Note: If you are using backward motion tracks, the video will be plotted in reverse</html>");
		noteLabel.setFont(new Font("Roboto", Font.PLAIN, 15));
		noteLabel.setVerticalAlignment(SwingConstants.TOP);
		noteLabel.setHorizontalAlignment(SwingConstants.LEFT);
		noteLabel.setVisible(false);
		noteLabel.setBounds(5, 60, 400, 40);
		optionPanel1.add(noteLabel);

		JPanel step5Panel = new JPanel();
		step5Panel.setLayout(null);
		step5Panel.setBorder(null);
		step5Panel.setBackground(new Color(252, 252, 252));
		step5Panel.setBounds(10, 45, 480, 373);
		add(step5Panel);

		JLabel step5Label = new JLabel("STEP 5");
		step5Label.setBounds(5, 5, 53, 30);
		step5Panel.add(step5Label);
		step5Label.setVerticalAlignment(SwingConstants.TOP);
		step5Label.setHorizontalAlignment(SwingConstants.CENTER);
		step5Label.setForeground(Color.DARK_GRAY);
		step5Label.setFont(new Font("Roboto", Font.BOLD, 15));

		JLabel instructionLabel1 = new JLabel(
				"<html> Select you preferred outputs. The computed files will be saved in the current MOSES workspace.</html>");
		instructionLabel1.setBounds(60, 5, 415, 41);
		step5Panel.add(instructionLabel1);
		instructionLabel1.setVerticalAlignment(SwingConstants.TOP);
		instructionLabel1.setFont(new Font("Roboto", Font.PLAIN, 15));

		bigPanel = new JPanel();
		bigPanel.setBounds(10, 52, 460, 309);
		step5Panel.add(bigPanel);
		bigPanel.setBackground(new Color(252, 252, 252));

		bigPanel.setLayout(null);

		CheckBoxList checkBoxList = new CheckBoxList(bigPanel);
		checkBoxList.addCheckBox(saveCheckBox1);
		checkBoxList.addPanelChild(saveCheckBox1, optionPanel1);
		checkBoxList.addCheckBox(saveCheckBox2);
		checkBoxList.addCheckBox(saveCheckBox3);
		checkBoxList.show(5, 5);
	}
}
