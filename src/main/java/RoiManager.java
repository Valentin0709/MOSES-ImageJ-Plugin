import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Overlay;
import ij.gui.Roi;
import ij.process.ImageProcessor;

public class RoiManager {
	ImagePlus image;
	JPanel displayPanel;
	Overlay overlay;
	List<String> roiNames;
	int roiNumber;
	JComboBox labelColorComboBox, strokeColorComboBox;

	public RoiManager(ImagePlus img, JPanel panel, JComboBox labelColor, JComboBox strokeColor) {
		image = img;
		displayPanel = panel;
		labelColorComboBox = labelColor;
		strokeColorComboBox = strokeColor;

		labelColorComboBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				overlay.setLabelColor(Globals.nameToColor(String.valueOf(labelColorComboBox.getSelectedItem())));
				image.setOverlay(overlay);
				show();
			}
		});
		strokeColorComboBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				overlay.setStrokeColor(Globals.nameToColor(String.valueOf(strokeColorComboBox.getSelectedItem())));
				image.setOverlay(overlay);
				show();
			}
		});

		image.createNewRoi(0, 0);

		overlay = new Overlay();
		overlay.drawLabels(true);
		overlay.drawNames(true);
		overlay.setStrokeColor(Globals.nameToColor(String.valueOf(strokeColorComboBox.getSelectedItem())));
		overlay.setStrokeWidth(8.0);
		overlay.setLabelFont(new Font("Roboto", Font.BOLD, 15));
		overlay.setLabelColor(Globals.nameToColor(String.valueOf(labelColorComboBox.getSelectedItem())));

		roiNames = new ArrayList<String>();
		roiNumber = 1;
	}

	public void addLastRoi() {
		Roi roi = image.getRoi();
		roi.setPosition(0, 0, 0);

		roiNames.add(String.valueOf(roiNumber));
		overlay.add(roi, String.valueOf(roiNumber));

//		Polygon pol = roi.getPolygon();
//		for (int i = 0; i < pol.npoints; i++)
//			IJ.log(pol.xpoints[i] + " " + pol.ypoints[i]);

		// IJ.log(String.valueOf(roi.getPolygon()));
		// IJ.log(String.valueOf(roi.getFloatPolygon()));

		overlay.setLabelColor(Globals.nameToColor(String.valueOf(labelColorComboBox.getSelectedItem())));
		overlay.setStrokeColor(Globals.nameToColor(String.valueOf(strokeColorComboBox.getSelectedItem())));

		image.setOverlay(overlay);
		roiNumber++;

		show();
	}

	public boolean saveMask(boolean saveOverlay) {
		IJ.run(image, "Select None", "");

		ImageProcessor mask = image.createRoiMask();
		ImagePlus maskImage = new ImagePlus("mask", mask);

		String saveDirectory = IJ.getDirectory("Choose saving directory");
		if (saveDirectory != null) {
			IJ.saveAs(maskImage, "PNG",
					saveDirectory + Globals.getNameWithoutExtension(image.getTitle()).replace("_", "") + "_mask.png");

			if (saveOverlay)
				IJ.saveAs(image, "PNG", saveDirectory
						+ Globals.getNameWithoutExtension(image.getTitle()).replace("_", "") + "_annotations.png");

			image.close();

			return true;
		}
		return false;
	}

	public void show() {
		int roiCount = 0;
		displayPanel.removeAll();

		for (String roiName : roiNames) {
			JLabel roiNameLabel = new JLabel(roiName);
			roiNameLabel.setHorizontalAlignment(SwingConstants.LEFT);
			roiNameLabel.setFont(new Font("Roboto", Font.PLAIN, 15));
			roiNameLabel.setBounds(10, 10 + roiCount * 25, 200, 20);
			roiNameLabel.setBackground(new Color(252, 252, 252));
			roiNameLabel.setOpaque(true);
			roiNameLabel.setVisible(true);

			final int roiCountCopy = roiCount;

			JButton deleteButton = new JButton("Delete");
			deleteButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					IJ.run(image, "Select None", "");

					overlay.remove(roiCountCopy);
					roiNames.remove(roiCountCopy);
					image.setOverlay(overlay);
					show();
				}
			});
			deleteButton.setVerticalTextPosition(SwingConstants.CENTER);
			deleteButton.setHorizontalTextPosition(SwingConstants.CENTER);
			deleteButton.setForeground(Color.WHITE);
			deleteButton.setFont(new Font("Arial", Font.BOLD, 15));
			deleteButton.setBackground(new Color(13, 59, 102));
			deleteButton.setBounds(230, 10 + roiCount * 25, 100, 20);
			displayPanel.add(deleteButton);

			JButton renameButton = new JButton("Rename");
			renameButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					IJ.run(image, "Select None", "");

					JFrame dialog = new JFrame();
					String s = (String) JOptionPane.showInputDialog(dialog, "Set selection name:\n", "MOSES",
							JOptionPane.PLAIN_MESSAGE, null, null, null);

					overlay.get(roiCountCopy).setName(s);
					roiNames.set(roiCountCopy, s);
					image.setOverlay(overlay);
					show();
				}
			});
			renameButton.setVerticalTextPosition(SwingConstants.CENTER);
			renameButton.setHorizontalTextPosition(SwingConstants.CENTER);
			renameButton.setForeground(Color.WHITE);
			renameButton.setFont(new Font("Arial", Font.BOLD, 15));
			renameButton.setBackground(new Color(13, 59, 102));
			renameButton.setBounds(350, 10 + roiCount * 25, 100, 20);
			displayPanel.add(renameButton);

			roiCount++;
			displayPanel.add(roiNameLabel);
		}
		Globals.updatePanelSize(displayPanel);
	}

}
