import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.LineBorder;

public class VisualisationFromMaskPanel2 extends JPanel {
	private MainFrame parentFrame;
	private VisualisationFromMaskPanel2 self = this;

	JButton importAnnotationButton;
	JPanel step3Panel, step4Panel;
	JLabel selectedAnnotationsLabel;
	JCheckBox showAnnotationsCheckBox, showLabelsCheckBox;
	JComboBox fontSizeComboBox, colorPaletteComboBox;

	boolean ok1 = false;

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

		this.setPreferredSize(new Dimension(1032, 500));

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

		JButton nextButton = new JButton("Next");
		nextButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (ok1) {
					VisualisationFromMaskParameters
							.setColorPalette(String.valueOf(colorPaletteComboBox.getSelectedItem()));
					VisualisationFromMaskParameters
							.setFontSize(Integer.parseInt(String.valueOf(fontSizeComboBox.getSelectedItem())));
					VisualisationFromMaskParameters.setLabelVisibility(showLabelsCheckBox.isSelected());
					VisualisationFromMaskParameters.setAnnotationVisibility(showAnnotationsCheckBox.isSelected());

					parentFrame.empty();
					parentFrame.visualisationFromMaskPanel3 = new VisualisationFromMaskPanel3(parentFrame);
					parentFrame.getContentPane().add(parentFrame.visualisationFromMaskPanel3);
					parentFrame.validate();
				} else {
					// display error dialog box
					JFrame dialog = new JFrame();
					Object[] options = { "Ok" };
					JOptionPane.showOptionDialog(dialog, "Plese complete steps 3 before going to the next page.",
							"MOSES", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);

				}
			}
		});
		nextButton.setVerticalTextPosition(SwingConstants.CENTER);
		nextButton.setHorizontalTextPosition(SwingConstants.CENTER);
		nextButton.setForeground(Color.WHITE);
		nextButton.setFont(new Font("Arial", Font.BOLD, 15));
		nextButton.setBackground(new Color(13, 59, 102));
		nextButton.setBounds(350, 430, 140, 30);
		add(nextButton);

		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
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

		step3Panel = new JPanel();
		step3Panel.setLayout(null);
		step3Panel.setBorder(new LineBorder(new Color(0, 0, 0), 2));
		step3Panel.setBackground(new Color(252, 252, 252));
		step3Panel.setBounds(10, 45, 480, 180);
		add(step3Panel);

		importAnnotationButton = new JButton("Select annotation");
		importAnnotationButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				FileSelecter selecter = new FileSelecter();
				selecter.setSelectAllButton(false);
				selecter.setVisible(true);
				selecter.annotationList(VisualisationFromMaskParameters.getWorkspace(),
						VisualisationFromMaskParameters.getProjectNames(),
						"Select for each project the annotation file you want to use for visualising the motion tracks.");

				selecter.importButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						List<String> annotationPaths = selecter.getSelected();
						selecter.dispose();

						VisualisationFromMaskParameters.setAnnotationPaths(annotationPaths);

						List<String> errorList = VisualisationFromMaskParameters.noAnnotationMatch();
						if (errorList.size() > 0) {
							JFrame dialog = new JFrame();
							Object[] options = { "Ok" };
							JOptionPane.showOptionDialog(dialog,
									"No annotation selected for " + String.join(", ", errorList), "MOSES",
									JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
						}

						if (annotationPaths.size() > 0)
							showSelectedAnnotations();
						else {
							selectedAnnotationsLabel.setText("No annotations selected");
							ok1 = false;
						}

					}
				});

			}
		});
		importAnnotationButton.setVerticalTextPosition(SwingConstants.CENTER);
		importAnnotationButton.setHorizontalTextPosition(SwingConstants.CENTER);
		importAnnotationButton.setForeground(Color.WHITE);
		importAnnotationButton.setFont(new Font("Arial", Font.BOLD, 15));
		importAnnotationButton.setBackground(new Color(13, 59, 102));
		importAnnotationButton.setBounds(140, 25, 200, 20);
		step3Panel.add(importAnnotationButton);

		JLabel step3Label = new JLabel("STEP 3");
		step3Label.setVerticalAlignment(SwingConstants.TOP);
		step3Label.setHorizontalAlignment(SwingConstants.LEFT);
		step3Label.setForeground(Color.DARK_GRAY);
		step3Label.setFont(new Font("Roboto", Font.BOLD, 15));
		step3Label.setBounds(5, 5, 53, 30);
		step3Panel.add(step3Label);

		JLabel instructionLabel3 = new JLabel("<html>For each project pick one annotation file</html>");
		instructionLabel3.setVerticalAlignment(SwingConstants.TOP);
		instructionLabel3.setFont(new Font("Roboto", Font.PLAIN, 15));
		instructionLabel3.setBounds(60, 5, 415, 20);
		step3Panel.add(instructionLabel3);

		JScrollPane scrollPane3 = new JScrollPane();
		scrollPane3.setViewportBorder(null);
		scrollPane3.setBackground(new Color(252, 252, 252));
		scrollPane3.setBounds(5, 50, 470, 125);
		step3Panel.add(scrollPane3);

		selectedAnnotationsLabel = new JLabel("No annotations selected");
		scrollPane3.setViewportView(selectedAnnotationsLabel);
		selectedAnnotationsLabel.setVerticalAlignment(SwingConstants.TOP);
		selectedAnnotationsLabel.setFont(new Font("Roboto", Font.PLAIN, 15));

		JButton backButton = new JButton("Back");
		backButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
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

		step4Panel = new JPanel();
		step4Panel.setLayout(null);
		step4Panel.setBorder(new LineBorder(new Color(0, 0, 0), 2));
		step4Panel.setBackground(new Color(252, 252, 252));
		step4Panel.setBounds(10, 229, 480, 135);
		add(step4Panel);

		JLabel step4Label = new JLabel("STEP 4");
		step4Label.setVerticalAlignment(SwingConstants.TOP);
		step4Label.setHorizontalAlignment(SwingConstants.LEFT);
		step4Label.setForeground(Color.DARK_GRAY);
		step4Label.setFont(new Font("Roboto", Font.BOLD, 15));
		step4Label.setBounds(5, 5, 53, 30);
		step4Panel.add(step4Label);

		JLabel instructionLabel1 = new JLabel("<html>Set display options.</html>");
		instructionLabel1.setVerticalAlignment(SwingConstants.TOP);
		instructionLabel1.setFont(new Font("Roboto", Font.PLAIN, 15));
		instructionLabel1.setBounds(60, 5, 415, 20);
		step4Panel.add(instructionLabel1);

		JLabel lblcolorPalette = new JLabel("<html>Color palette:</html>");
		lblcolorPalette.setFont(new Font("Roboto", Font.PLAIN, 15));
		lblcolorPalette.setBounds(5, 30, 91, 25);
		step4Panel.add(lblcolorPalette);

		colorPaletteComboBox = new JComboBox();
		colorPaletteComboBox.setModel(
				new DefaultComboBoxModel(new String[] { "bright", "deep", "muted", "pastel", "dark", "colorblind" }));
		colorPaletteComboBox.setSelectedItem(VisualisationFromMaskParameters.getColorPalette());
		colorPaletteComboBox.setFont(new Font("Roboto", Font.PLAIN, 15));
		colorPaletteComboBox.setBounds(115, 30, 150, 25);
		step4Panel.add(colorPaletteComboBox);

		showLabelsCheckBox = new JCheckBox("Show labels");
		showLabelsCheckBox.setFont(new Font("Roboto", Font.PLAIN, 15));
		showLabelsCheckBox.setBackground(new Color(252, 252, 252));
		showLabelsCheckBox.setBounds(191, 55, 182, 24);
		showLabelsCheckBox.setSelected(VisualisationFromMaskParameters.getLabelVisibility());
		step4Panel.add(showLabelsCheckBox);

		JLabel lblfontSize = new JLabel("<html>Label font size:</html>");
		lblfontSize.setFont(new Font("Roboto", Font.PLAIN, 15));
		lblfontSize.setBounds(5, 80, 104, 25);
		step4Panel.add(lblfontSize);

		fontSizeComboBox = new JComboBox();
		fontSizeComboBox
				.setModel(new DefaultComboBoxModel(new String[] { "10", "12", "14", "16", "18", "20", "22", "24" }));
		fontSizeComboBox.setSelectedItem(String.valueOf(VisualisationFromMaskParameters.getFontSize()));
		fontSizeComboBox.setFont(new Font("Roboto", Font.PLAIN, 15));
		fontSizeComboBox.setBounds(115, 80, 150, 25);
		step4Panel.add(fontSizeComboBox);

		showAnnotationsCheckBox = new JCheckBox("Show annotations");
		showAnnotationsCheckBox.setFont(new Font("Roboto", Font.PLAIN, 15));
		showAnnotationsCheckBox.setBackground(new Color(252, 252, 252));
		showAnnotationsCheckBox.setBounds(5, 55, 182, 24);
		showAnnotationsCheckBox.setSelected(VisualisationFromMaskParameters.getAnnotationVisibility());
		step4Panel.add(showAnnotationsCheckBox);

		if (VisualisationFromMaskParameters.getAnnotationPaths().size() > 0)
			showSelectedAnnotations();

	}

	public void showSelectedAnnotations() {
		String text = "Selected annotations: <br>"
				+ String.join("<br>", VisualisationFromMaskParameters.getAnnotationPaths());

		List<String> errorList = VisualisationFromMaskParameters.noAnnotationMatch();
		if (errorList.size() > 0) {
			text += "<br> Warning! No annotation selected for the following projects: <br>"
					+ String.join("<br> ", errorList);
		}
		selectedAnnotationsLabel.setText("<html>" + text + "</html>");

		ok1 = true;
	}

}
