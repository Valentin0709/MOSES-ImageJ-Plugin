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

import ij.IJ;

public class VisualisationFromMaskPanel2 extends JLayeredPane {
	private MainFrame parentFrame;
	private VisualisationFromMaskPanel2 self = this;

	JCheckBox saveCheckBox1, saveCheckBox2;
	JPanel optionPanel, bigPanel;
	SaveOption saveOption;
	ColorOption colorOption;
	JFormattedTextField temporalSegmentField;

	VisualisationFromMask swingWorker;
	boolean swingWorkerStarted;

	public VisualisationFromMaskPanel2(MainFrame parentFrame) {
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

		JLabel step4Label = new JLabel("STEP 4");
		step4Label.setVerticalAlignment(SwingConstants.TOP);
		step4Label.setHorizontalAlignment(SwingConstants.LEFT);
		step4Label.setForeground(Color.DARK_GRAY);
		step4Label.setFont(new Font("Roboto", Font.BOLD, 15));
		step4Label.setBounds(10, 45, 53, 30);
		add(step4Label);

		JLabel instructionLabel1 = new JLabel(
				"<html> Select you preferred outputs. You'll be asked to choose a saving directory at the end. </html>");
		instructionLabel1.setVerticalAlignment(SwingConstants.TOP);
		instructionLabel1.setFont(new Font("Roboto", Font.PLAIN, 15));
		instructionLabel1.setBounds(65, 45, 415, 41);
		add(instructionLabel1);

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
				if (saveCheckBox1.isSelected() || saveCheckBox2.isSelected()) {

					String saveDirectory = IJ.getDirectory("Choose saving directory");
					if (saveDirectory != null) {
						VisualisationFromMaskParameters.setSaveDirectory(saveDirectory);

						if (saveCheckBox1.isSelected()) {
							VisualisationFromMaskParameters.setCompleteVisualisation(true);
							VisualisationFromMaskParameters.setSaveOption(saveOption);
							VisualisationFromMaskParameters.setColorOption(colorOption);
							VisualisationFromMaskParameters
									.setTracksTemporalSegment(Integer.parseInt(temporalSegmentField.getText()));
						}

						if (saveCheckBox2.isSelected())
							VisualisationFromMaskParameters.setLongestTracksVisualisation(true);

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
				parentFrame.visualisationFromMaskPanel1 = new VisualisationFromMaskPanel1(parentFrame);
				parentFrame.getContentPane().add(parentFrame.visualisationFromMaskPanel1);
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

		bigPanel = new JPanel();
		bigPanel.setBounds(20, 98, 460, 252);
		bigPanel.setBackground(new Color(252, 252, 252));
		add(bigPanel);

		saveCheckBox1 = new JCheckBox("Create visualisation for all frames (.tif)");
		saveCheckBox1.setFont(new Font("Roboto", Font.PLAIN, 15));
		saveCheckBox1.setBounds(0, 0, 291, 24);
		saveCheckBox1.setBackground(new Color(252, 252, 252));

		saveCheckBox2 = new JCheckBox("Plot longest track for each region of interest (.png)");
		saveCheckBox2.setFont(new Font("Roboto", Font.PLAIN, 15));
		saveCheckBox2.setBounds(0, 0, 192, 24);
		saveCheckBox2.setBackground(new Color(252, 252, 252));

		optionPanel = new JPanel();
		optionPanel.setOpaque(false);
		optionPanel.setBounds(0, 0, 405, 160);
		optionPanel.setBackground(new Color(238, 238, 238));
		optionPanel.setLayout(null);

		saveOption = new SaveOption(Arrays.asList(".tif", ".avi", ".png"));
		saveOption.show(optionPanel, 320, 5, true);
		colorOption = new ColorOption(VisualisationFromMaskParameters.getChannels());
		colorOption.show(optionPanel, 10, 25, 3);

		JLabel instructionLabel = new JLabel("<html> Select the display color for each motion track </html>");
		instructionLabel.setVisible(false);
		instructionLabel.setBounds(5, 5, 306, 18);
		instructionLabel.setFont(new Font("Roboto", Font.PLAIN, 15));
		optionPanel.add(instructionLabel);

		JLabel temporalSegmentLabel = new JLabel("Plot tracks in temporal segments of length:");
		temporalSegmentLabel.setVisible(false);
		temporalSegmentLabel.setVerticalAlignment(SwingConstants.TOP);
		temporalSegmentLabel.setHorizontalAlignment(SwingConstants.LEFT);
		temporalSegmentLabel.setFont(new Font("Roboto", Font.PLAIN, 15));
		temporalSegmentLabel.setBounds(5, 95, 295, 20);
		optionPanel.add(temporalSegmentLabel);

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
		temporalSegmentField.setBounds(290, 97, 70, 18);
		optionPanel.add(temporalSegmentField);

		JLabel noteLabel = new JLabel(
				"<html>Note: If you are using backward motion tracks, the video will be plotted in reverse</html>");
		noteLabel.setFont(new Font("Roboto", Font.PLAIN, 15));
		noteLabel.setVerticalAlignment(SwingConstants.TOP);
		noteLabel.setHorizontalAlignment(SwingConstants.LEFT);
		noteLabel.setVisible(false);
		noteLabel.setBounds(5, 115, 400, 40);
		optionPanel.add(noteLabel);

		CheckBoxList checkBoxList = new CheckBoxList(bigPanel);
		bigPanel.setLayout(null);
		checkBoxList.addCheckBox(saveCheckBox1);
		checkBoxList.addPanelChild(saveCheckBox1, optionPanel);
		checkBoxList.addCheckBox(saveCheckBox2);
		checkBoxList.show(5, 5);
	}

}
