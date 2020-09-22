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
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.LineBorder;

public class SaliencyMapPanel2 extends JLayeredPane {
	private MainFrame parentFrame;
	private SaliencyMapPanel2 self = this;

	boolean ok1 = false;

	private SaliencyMap swingWorker;
	private boolean swingWorkerStarted = false;

	private JPanel bigPanel, optionPanel2, optionPanel3, optionPanel6, optionPanel4;
	private JScrollPane scrollPane;
	private JCheckBox saveCheckBox1, saveCheckBox2, saveCheckBox3, saveCheckBox4, saveCheckBox31, saveCheckBox6,
			gaussianCheckBox, saveCheckBox7, saveCheckBox21;
	private JLabel noteLabel, instructionLabel2, selectedTracksLabel, importedImagesLabel;
	private JFormattedTextField distanceTresholdField, paddingField;
	SaveOption saveOption1, saveOption2;

	public SaliencyMapPanel2(MainFrame parentFrame) {
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
				// stop swing worker
				if (swingWorkerStarted)
					swingWorker.destroy();

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
				if (saveCheckBox1.isSelected() || saveCheckBox2.isSelected() || saveCheckBox3.isSelected()
						|| saveCheckBox4.isSelected() || saveCheckBox6.isSelected()) {

					SaliencyMapParameters
							.setSaliencyMapDistanceThreshold(Double.parseDouble(distanceTresholdField.getText()));
					if (gaussianCheckBox.isSelected())
						SaliencyMapParameters.setGaussianSmoothing(true);

					if (saveCheckBox1.isSelected())
						SaliencyMapParameters.setOutput("motion_saliency_map");

					if (saveCheckBox2.isSelected()) {
						SaliencyMapParameters.setOutput("final_saliency_map_visualisation");
						if (saveCheckBox21.isSelected())
							SaliencyMapParameters.setOutput("final_saliency_map_visualisation_overlay");
					}

					if (saveCheckBox3.isSelected()) {
						SaliencyMapParameters.setOutput("spatial_time_saliency_map_visualisation");
						SaliencyMapParameters.setSaveOption("spatial_time_saliency_map_visualisation_save_options",
								saveOption1);
						if (saveCheckBox31.isSelected())
							SaliencyMapParameters.setOutput("spatial_time_saliency_map_visualisation_overlay");
					}

					if (saveCheckBox4.isSelected()) {
						SaliencyMapParameters.setOutput("average_motion_saliency_map");
						SaliencyMapParameters.setSaveOption("average_motion_saliency_map_save_options", saveOption2);
					}

					if (saveCheckBox6.isSelected()) {
						SaliencyMapParameters.setOutput("boundary_formation_index");
						SaliencyMapParameters.setPaddingDistance(Integer.parseInt(paddingField.getText()));
					}

//					if (saveCheckBox7.isSelected())
//						SaliencyMapParameters.setOutput("config_file");

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

					swingWorker = new SaliencyMap(progress);
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

		JPanel step4Panel = new JPanel();
		step4Panel.setLayout(null);
		step4Panel.setBorder(new LineBorder(new Color(0, 0, 0), 2));
		step4Panel.setBackground(new Color(252, 252, 252));
		step4Panel.setBounds(10, 127, 480, 298);
		add(step4Panel);

		JLabel step4Label = new JLabel("STEP 4");
		step4Label.setVerticalAlignment(SwingConstants.TOP);
		step4Label.setHorizontalAlignment(SwingConstants.CENTER);
		step4Label.setForeground(Color.DARK_GRAY);
		step4Label.setFont(new Font("Roboto", Font.BOLD, 15));
		step4Label.setBounds(5, 5, 53, 30);
		step4Panel.add(step4Label);

		JLabel instructionLabel1 = new JLabel(
				"<html> Select you preferred outputs. The computed files will be saved in the current MOSES workspace.</html>");
		instructionLabel1.setVerticalAlignment(SwingConstants.TOP);
		instructionLabel1.setFont(new Font("Roboto", Font.PLAIN, 15));
		instructionLabel1.setBounds(60, 5, 415, 41);
		step4Panel.add(instructionLabel1);

		// motion saliency map

		scrollPane = new JScrollPane();
		scrollPane.setBounds(10, 47, 460, 239);
		step4Panel.add(scrollPane);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setBorder(null);
		scrollPane.setBackground(new Color(252, 252, 252));

		bigPanel = new JPanel();
		bigPanel.setLocation(12, 0);
		scrollPane.setViewportView(bigPanel);
		bigPanel.setBackground(new Color(252, 252, 252));

		bigPanel.setLayout(null);

		CheckBoxList checkBoxList = new CheckBoxList(bigPanel);

		JButton backButton = new JButton("Back");
		backButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				parentFrame.empty();
				parentFrame.saliencyMapPanel1 = new SaliencyMapPanel1(parentFrame);
				parentFrame.getContentPane().add(parentFrame.saliencyMapPanel1);
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

		JPanel step4Panel_1 = new JPanel();
		step4Panel_1.setLayout(null);
		step4Panel_1.setBorder(new LineBorder(new Color(0, 0, 0), 2));
		step4Panel_1.setBackground(new Color(252, 252, 252));
		step4Panel_1.setBounds(10, 48, 480, 71);
		add(step4Panel_1);

		JLabel step3Label = new JLabel("STEP 3");
		step3Label.setVerticalAlignment(SwingConstants.TOP);
		step3Label.setHorizontalAlignment(SwingConstants.LEFT);
		step3Label.setForeground(Color.DARK_GRAY);
		step3Label.setFont(new Font("Roboto", Font.BOLD, 15));
		step3Label.setBounds(5, 5, 53, 30);
		step4Panel_1.add(step3Label);

		JLabel instructionLabel0 = new JLabel("<html>Set saliency map options</html>");
		instructionLabel0.setVerticalAlignment(SwingConstants.TOP);
		instructionLabel0.setFont(new Font("Roboto", Font.PLAIN, 15));
		instructionLabel0.setBounds(60, 5, 415, 20);
		step4Panel_1.add(instructionLabel0);

		JLabel instructionLabel4 = new JLabel("<html>Distance threshold (relative to superpixel size): </html>");
		instructionLabel4.setVerticalAlignment(SwingConstants.TOP);
		instructionLabel4.setFont(new Font("Roboto", Font.PLAIN, 15));
		instructionLabel4.setBounds(5, 25, 365, 20);
		step4Panel_1.add(instructionLabel4);

		distanceTresholdField = new JFormattedTextField(decimalFormat) {
			@Override
			protected void processFocusEvent(final FocusEvent e) {
				if (e.isTemporary()) {
					return;
				}

				if (e.getID() == FocusEvent.FOCUS_LOST) {
					if (getText() == null || getText().isEmpty()) {
						setValue(String.valueOf(ComputeTracksParameters.getMOSESMeshDistanceThreshold()));
					}
				}
				super.processFocusEvent(e);
			}
		};
		distanceTresholdField.setBounds(350, 25, 100, 20);
		step4Panel_1.add(distanceTresholdField);
		distanceTresholdField.setText(String.valueOf(SaliencyMapParameters.getSaliencyMapDistanceThreshold()));
		distanceTresholdField.setHorizontalAlignment(SwingConstants.CENTER);
		distanceTresholdField.setFont(new Font("Roboto", Font.PLAIN, 15));

		gaussianCheckBox = new JCheckBox("Apply Gaussian smoothing to create a more continuous heatmap");
		gaussianCheckBox.setFont(new Font("Roboto", Font.PLAIN, 15));
		gaussianCheckBox.setBounds(5, 45, 470, 24);
		gaussianCheckBox.setBackground(new Color(252, 252, 252));
		step4Panel_1.add(gaussianCheckBox);

		saveCheckBox1 = new JCheckBox("Motion saliency map (.mat)");
		saveCheckBox1.setBackground(new Color(252, 252, 252));
		saveCheckBox1.setFont(new Font("Roboto", Font.PLAIN, 15));
		saveCheckBox1.setBounds(0, 0, 400, 20);

		// final motion saliency map visualisation

		saveCheckBox2 = new JCheckBox("Visualize final saliency map (.png)");
		saveCheckBox2.setBackground(new Color(252, 252, 252));
		saveCheckBox2.setFont(new Font("Roboto", Font.PLAIN, 15));
		saveCheckBox2.setBounds(0, 0, 400, 20);

		optionPanel2 = new JPanel();
		optionPanel2.setBounds(0, 0, 450, 30);
		optionPanel2.setOpaque(false);
		optionPanel2.setBackground(new Color(238, 238, 238));
		optionPanel2.setLayout(null);

		saveCheckBox21 = new JCheckBox("Overlay on parent image");
		saveCheckBox21.setFont(new Font("Roboto", Font.PLAIN, 15));
		saveCheckBox21.setBounds(5, 5, 400, 20);
		optionPanel2.add(saveCheckBox21);

		// spatial time motion saliency map visualisation

		saveCheckBox3 = new JCheckBox("Visualize spatial time saliency map");
		saveCheckBox3.setBackground(new Color(252, 252, 252));
		saveCheckBox3.setFont(new Font("Roboto", Font.PLAIN, 15));
		saveCheckBox3.setBounds(0, 0, 400, 20);

		optionPanel3 = new JPanel();
		optionPanel3.setBounds(0, 0, 450, 70);
		optionPanel3.setOpaque(false);
		optionPanel3.setBackground(new Color(238, 238, 238));
		optionPanel3.setLayout(null);

		saveOption1 = new SaveOption(Arrays.asList(".tif", ".avi", ".png"));
		saveOption1.show(optionPanel3, 0, 0, false);

		saveCheckBox31 = new JCheckBox("Overlay on parent image");
		saveCheckBox31.setFont(new Font("Roboto", Font.PLAIN, 15));
		saveCheckBox31.setBounds(5, 45, 400, 20);
		optionPanel3.add(saveCheckBox31);

		// average saliency map

		saveCheckBox4 = new JCheckBox("Get average motion saliency map");
		saveCheckBox4.setBackground(new Color(252, 252, 252));
		saveCheckBox4.setFont(new Font("Roboto", Font.PLAIN, 15));
		saveCheckBox4.setBounds(0, 0, 400, 20);

		optionPanel4 = new JPanel();
		optionPanel4.setBounds(0, 0, 450, 45);
		optionPanel4.setOpaque(false);
		optionPanel4.setBackground(new Color(238, 238, 238));
		optionPanel4.setLayout(null);

		saveOption2 = new SaveOption(Arrays.asList(".mat", ".png"));
		saveOption2.show(optionPanel4, 0, 0, false);

		// boundary formation index

		saveCheckBox6 = new JCheckBox("Compute motion enrichment index");
		saveCheckBox6.setBackground(new Color(252, 252, 252));
		saveCheckBox6.setFont(new Font("Roboto", Font.PLAIN, 15));
		saveCheckBox6.setBounds(0, 0, 400, 20);

		optionPanel6 = new JPanel();
		optionPanel6.setBounds(0, 0, 450, 110);
		optionPanel6.setOpaque(false);
		optionPanel6.setBackground(new Color(238, 238, 238));
		optionPanel6.setLayout(null);

		instructionLabel2 = new JLabel(
				"<html> Please set the padding distance used to define the region of the image where the boundary formation index is computed. (This minimizes the effect created by cells moving out of the field of view of the video which causes superpixels to concentrate at the boundary of the videos) </html>");
		instructionLabel2.setVisible(false);
		instructionLabel2.setVerticalAlignment(SwingConstants.TOP);
		instructionLabel2.setFont(new Font("Roboto", Font.PLAIN, 15));
		instructionLabel2.setBounds(5, 5, 450, 100);
		optionPanel6.add(instructionLabel2);

		DecimalFormat integerFormat = new DecimalFormat("###");

		paddingField = new JFormattedTextField(integerFormat) {
			@Override
			protected void processFocusEvent(final FocusEvent e) {
				if (e.isTemporary()) {
					return;
				}

				if (e.getID() == FocusEvent.FOCUS_LOST) {
					if (getText() == null || getText().isEmpty()) {
						setValue(String.valueOf(ComputeTracksParameters.getMOSESMeshDistanceThreshold()));
					}
				}
				super.processFocusEvent(e);
			}
		};
		paddingField.setText(String.valueOf(SaliencyMapParameters.getPaddingDistance()));
		paddingField.setVisible(false);
		paddingField.setHorizontalAlignment(SwingConstants.CENTER);
		paddingField.setFont(new Font("Roboto", Font.PLAIN, 15));
		paddingField.setBounds(180, 85, 100, 20);
		optionPanel6.add(paddingField);

		// config file

		saveCheckBox7 = new JCheckBox("Save config file");
		saveCheckBox7.setBackground(new Color(252, 252, 252));
		saveCheckBox7.setFont(new Font("Roboto", Font.PLAIN, 15));
		saveCheckBox7.setBounds(0, 0, 400, 20);

		selectedTracksLabel = new JLabel("No files selected");
		selectedTracksLabel.setVerticalAlignment(SwingConstants.TOP);
		selectedTracksLabel.setHorizontalAlignment(SwingConstants.LEFT);
		selectedTracksLabel.setForeground(Color.DARK_GRAY);
		selectedTracksLabel.setFont(new Font("Roboto", Font.PLAIN, 15));
		selectedTracksLabel.setBorder(null);
		selectedTracksLabel.setBackground(new Color(252, 252, 252));

		importedImagesLabel = new JLabel("No files selected");
		importedImagesLabel.setVerticalAlignment(SwingConstants.TOP);
		importedImagesLabel.setHorizontalAlignment(SwingConstants.LEFT);
		importedImagesLabel.setForeground(Color.DARK_GRAY);
		importedImagesLabel.setFont(new Font("Roboto", Font.PLAIN, 15));
		importedImagesLabel.setBorder(null);
		importedImagesLabel.setBackground(new Color(252, 252, 252));

		checkBoxList.addCheckBox(saveCheckBox1);
		checkBoxList.addCheckBox(saveCheckBox2);
		checkBoxList.addPanelChild(saveCheckBox2, optionPanel2);
		checkBoxList.addCheckBox(saveCheckBox3);
		checkBoxList.addPanelChild(saveCheckBox3, optionPanel3);
		checkBoxList.addCheckBox(saveCheckBox4);
		checkBoxList.addPanelChild(saveCheckBox4, optionPanel4);
		checkBoxList.addCheckBox(saveCheckBox6);
		checkBoxList.addPanelChild(saveCheckBox6, optionPanel6);
//		checkBoxList.addCheckBox(saveCheckBox7);
		checkBoxList.show(0, 0);
	}

}
