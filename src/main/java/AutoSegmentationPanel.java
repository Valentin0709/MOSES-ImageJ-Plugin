import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.text.DecimalFormat;
import java.util.Arrays;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import ij.IJ;

public class AutoSegmentationPanel extends JLayeredPane {
	MainFrame parentFrame;
	JLayeredPane self = this;

	JCheckBox saveCheckBox1, saveCheckBox11, saveCheckBox2, saveCheckBox3;
	JPanel bigPanel;
	String saveDirectory;
	ProgressPanel progress;
	JPanel optionPanel1, optionPanel2, optionPanel3;
	SaveOption saveOption1, saveOption2;
	JFormattedTextField BBoxconfidenceTreshField;

	boolean swingWorkerStarted;
	BoundingBoxAndMaskGeneration swingWorker;

	public AutoSegmentationPanel(MainFrame parentFrame) {
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

		JLabel titleLabel = new JLabel("Auto-segmentation", SwingConstants.CENTER);
		titleLabel.setVerticalTextPosition(SwingConstants.CENTER);
		titleLabel.setHorizontalTextPosition(SwingConstants.CENTER);
		titleLabel.setFont(new Font("Arial Black", Font.BOLD, 23));
		titleLabel.setBounds(0, 0, 500, 36);
		add(titleLabel);

		bigPanel = new JPanel();
		bigPanel.setBounds(10, 48, 480, 347);
		bigPanel.setBackground(new Color(252, 252, 252));
		add(bigPanel);

		progress = new ProgressPanel(self, 40, 200);
		add(progress);
		setLayer(progress, 1);
		progress.setVisibility(false);

		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// stop swing worker
				if (swingWorkerStarted)
					swingWorker.cancel(true);

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
				if (saveCheckBox1.isSelected() || saveCheckBox3.isSelected()) {
					JFrame dialog = new JFrame();
					Object[] options = { "New MOSES workspace", "Use existing MOSES workspace" };
					int n = JOptionPane.showOptionDialog(dialog,
							"Do you want to save the files in an existing workspace or create a new MOSES workspace?",
							"MOSES", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);

					saveDirectory = null;
					if (n == 0) {
						JFrame dialog2 = new JFrame();
						String name = (String) JOptionPane.showInputDialog(dialog2, "Set workspace name:\n", "MOSES",
								JOptionPane.PLAIN_MESSAGE, null, null, null);

						String workspaceDirectory = IJ.getDirectory("Choose saving directory");
						if (workspaceDirectory != null) {
							File workspaceFolder = Globals.createWorkspace(workspaceDirectory, name);
							saveDirectory = workspaceFolder.getAbsolutePath();
						}
					}
					if (n == 1)
						saveDirectory = Globals.getWorkspace();

					if (saveDirectory != null) {
						BoundingBoxAndMaskGenerationParameters.setWorkspace(saveDirectory);
						BoundingBoxAndMaskGenerationParameters
								.setBBoxConfidenceTresh(Float.parseFloat(BBoxconfidenceTreshField.getText()));
						BoundingBoxAndMaskGenerationParameters.setSaveOption("bounding_box_vis", saveOption1);
						BoundingBoxAndMaskGenerationParameters.setSaveOption("mask", saveOption2);

						if (saveCheckBox1.isSelected())
							BoundingBoxAndMaskGenerationParameters.setOutput("bounding_box");

						if (saveCheckBox2.isSelected())
							BoundingBoxAndMaskGenerationParameters.setOutput("bounding_box_vis");

						if (saveCheckBox3.isSelected())
							BoundingBoxAndMaskGenerationParameters.setOutput("mask");

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

						swingWorker = new BoundingBoxAndMaskGeneration(progress);
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

		// bbox generation

		saveCheckBox1 = new JCheckBox("Generate bounding boxes (.txt)");
		saveCheckBox1.setBackground(new Color(252, 252, 252));
		saveCheckBox1.setFont(new Font("Roboto", Font.PLAIN, 15));
		saveCheckBox1.setBounds(0, 0, 350, 20);

		optionPanel1 = new JPanel();
		optionPanel1.setOpaque(false);
		optionPanel1.setBackground(new Color(238, 238, 238));
		optionPanel1.setBounds(27, 99, 450, 35);
		optionPanel1.setLayout(null);

		JLabel instructionLabel1 = new JLabel("Set confidence treshold:");
		instructionLabel1.setVisible(false);
		instructionLabel1.setFont(new Font("Roboto", Font.PLAIN, 14));
		instructionLabel1.setBounds(5, 5, 163, 20);
		optionPanel1.add(instructionLabel1);

		DecimalFormat decimalFormat = new DecimalFormat("#.##");
		decimalFormat.setGroupingUsed(false);

		BBoxconfidenceTreshField = new JFormattedTextField(decimalFormat) {
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
		BBoxconfidenceTreshField
				.setText(String.valueOf(BoundingBoxAndMaskGenerationParameters.getBBoxConfidenceTresh()));
		BBoxconfidenceTreshField.setHorizontalAlignment(SwingConstants.CENTER);
		BBoxconfidenceTreshField.setFont(new Font("Roboto", Font.PLAIN, 15));
		BBoxconfidenceTreshField.setBounds(170, 5, 100, 20);
		optionPanel1.add(BBoxconfidenceTreshField);

		// bbox visualization

		saveCheckBox2 = new JCheckBox("Visualize bounding boxes");
		saveCheckBox2.setBackground(new Color(252, 252, 252));
		saveCheckBox2.setFont(new Font("Roboto", Font.PLAIN, 15));
		saveCheckBox2.setBounds(0, 0, 350, 20);

		optionPanel2 = new JPanel();
		optionPanel2.setOpaque(false);
		optionPanel2.setBackground(new Color(238, 238, 238));
		optionPanel2.setBounds(27, 99, 450, 40);
		optionPanel2.setLayout(null);

		saveOption1 = new SaveOption(Arrays.asList(".tif", ".avi", "png"));
		saveOption1.show(optionPanel2, 5, 0, false);

		// mask

		saveCheckBox3 = new JCheckBox("Generate mask binary");
		saveCheckBox3.setBackground(new Color(252, 252, 252));
		saveCheckBox3.setFont(new Font("Roboto", Font.PLAIN, 15));
		saveCheckBox3.setBounds(0, 0, 350, 20);

		optionPanel3 = new JPanel();
		optionPanel3.setOpaque(false);
		optionPanel3.setBackground(new Color(238, 238, 238));
		optionPanel3.setBounds(27, 99, 450, 40);
		optionPanel3.setLayout(null);

		saveOption2 = new SaveOption(Arrays.asList(".tif", ".avi", "png"));
		saveOption2.show(optionPanel3, 5, 0, false);

		CheckBoxList checkBoxList = new CheckBoxList(bigPanel);
		bigPanel.setLayout(null);
		checkBoxList.addCheckBox(saveCheckBox1);
		checkBoxList.addPanelChild(saveCheckBox1, optionPanel1);
		checkBoxList.addCheckBoxChild(saveCheckBox1, saveCheckBox2);
		checkBoxList.addPanelChild(saveCheckBox2, optionPanel2);
		checkBoxList.addCheckBox(saveCheckBox3);
		checkBoxList.addPanelChild(saveCheckBox3, optionPanel3);
		checkBoxList.show(10, 10);

	}
}
