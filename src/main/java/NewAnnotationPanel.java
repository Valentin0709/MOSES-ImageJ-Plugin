import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import ij.IJ;

public class NewAnnotationPanel extends JLayeredPane {
	private MainFrame parentFrame;
	private NewAnnotationPanel self = this;

	JPanel bigPanel;
	JScrollPane scrollPane;
	JComboBox labelColorComboBox, strokeColorComboBox;
	JCheckBox overlayCheckBox, maskCheckBox;

	public NewAnnotationPanel(MainFrame parentFrame) {
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

		scrollPane = new JScrollPane();
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setBounds(20, 90, 460, 215);
		add(scrollPane);

		bigPanel = new JPanel();
		bigPanel.setBorder(null);
		scrollPane.setViewportView(bigPanel);
		bigPanel.setBackground(new Color(252, 252, 252));
		bigPanel.setLayout(null);

		JLabel labelColorLabel = new JLabel("Label color");
		labelColorLabel.setFont(new Font("Roboto", Font.PLAIN, 15));
		labelColorLabel.setBounds(20, 317, 99, 16);
		add(labelColorLabel);

		labelColorComboBox = new JComboBox();
		labelColorComboBox.setFont(new Font("Roboto", Font.PLAIN, 15));
		labelColorComboBox.setModel(
				new DefaultComboBoxModel(new String[] { "white", "black", "red", "yellow", "green", "blue" }));
		labelColorComboBox.setSelectedIndex(0);
		labelColorComboBox.setBounds(20, 338, 130, 25);
		add(labelColorComboBox);

		JLabel strokeColorLabel = new JLabel("Stroke color");
		strokeColorLabel.setFont(new Font("Roboto", Font.PLAIN, 15));
		strokeColorLabel.setBounds(350, 317, 99, 16);
		add(strokeColorLabel);

		strokeColorComboBox = new JComboBox();
		strokeColorComboBox.setModel(
				new DefaultComboBoxModel(new String[] { "white", "black", "red", "yellow", "green", "blue" }));
		strokeColorComboBox.setSelectedIndex(0);
		strokeColorComboBox.setFont(new Font("Roboto", Font.PLAIN, 15));
		strokeColorComboBox.setBounds(350, 338, 130, 25);
		add(strokeColorComboBox);

		// roi manager

		RoiManager rm = new RoiManager(Globals.lastImage, bigPanel, labelColorComboBox, strokeColorComboBox);

		JLabel titleLabel = new JLabel("Create annotation", SwingConstants.CENTER);
		titleLabel.setVerticalTextPosition(SwingConstants.CENTER);
		titleLabel.setHorizontalTextPosition(SwingConstants.CENTER);
		titleLabel.setFont(new Font("Arial Black", Font.BOLD, 23));
		titleLabel.setBounds(0, 0, 500, 36);
		add(titleLabel);

		JButton AddCurrentSelectionButton = new JButton("Add current selection");
		AddCurrentSelectionButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				rm.addLastRoi();
			}
		});
		AddCurrentSelectionButton.setVerticalTextPosition(SwingConstants.CENTER);
		AddCurrentSelectionButton.setHorizontalTextPosition(SwingConstants.CENTER);
		AddCurrentSelectionButton.setForeground(Color.WHITE);
		AddCurrentSelectionButton.setFont(new Font("Arial", Font.BOLD, 15));
		AddCurrentSelectionButton.setBackground(new Color(13, 59, 102));
		AddCurrentSelectionButton.setBounds(58, 59, 386, 20);
		add(AddCurrentSelectionButton);

		JButton saveButton = new JButton("Save annotations");
		saveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (rm.roiCount() > 0) {
					JFrame dialog = new JFrame();
					Object[] options = { "Ok" };
					JOptionPane.showOptionDialog(dialog, "Please select a MOSES workspace", "MOSES",
							JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

					String saveDirectory = IJ.getDirectory("Choose saving directory");

					if (saveDirectory != null) {
						String annotationFolderPath = saveDirectory + "/" + Globals.lastImage.getShortTitle() + "/"
								+ "annotations" + "/" + Globals.getFormattedDate();
						File annotationFolder = new File(annotationFolderPath);
						annotationFolder.mkdirs();

						rm.saveAnnotation(annotationFolderPath);

						if (overlayCheckBox.isSelected())
							rm.saveOverlay(annotationFolderPath);

						if (maskCheckBox.isSelected())
							rm.saveMask(annotationFolderPath);

						rm.closeImage();

						parentFrame.empty();
						parentFrame.menuPanel = new MenuPanel(parentFrame);
						parentFrame.getContentPane().add(parentFrame.menuPanel);
						parentFrame.validate();

					}
				} else {
					JFrame dialog = new JFrame();
					Object[] options = { "Ok" };
					JOptionPane.showOptionDialog(dialog, "No annotations to save.", "MOSES", JOptionPane.YES_NO_OPTION,
							JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
				}
			}
		});
		saveButton.setVerticalTextPosition(SwingConstants.CENTER);
		saveButton.setHorizontalTextPosition(SwingConstants.CENTER);
		saveButton.setForeground(Color.WHITE);
		saveButton.setFont(new Font("Arial", Font.BOLD, 15));
		saveButton.setBackground(new Color(13, 59, 102));
		saveButton.setBounds(323, 430, 167, 30);
		add(saveButton);

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

		overlayCheckBox = new JCheckBox("Save annotation overlay");
		overlayCheckBox.setFont(new Font("Roboto", Font.PLAIN, 15));
		overlayCheckBox.setBounds(20, 371, 200, 24);
		overlayCheckBox.setBackground(new Color(252, 252, 252));
		add(overlayCheckBox);

		maskCheckBox = new JCheckBox("Save image mask");
		maskCheckBox.setFont(new Font("Roboto", Font.PLAIN, 15));
		maskCheckBox.setBackground(new Color(252, 252, 252));
		maskCheckBox.setBounds(20, 398, 200, 24);
		add(maskCheckBox);
	}
}
