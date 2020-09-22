import java.awt.Color;
import java.awt.Font;
import java.awt.Polygon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Arrays;

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

		roiNumber = 1;
	}

	public void addLastRoi() {
		Roi roi = image.getRoi();
		roi.setPosition(0, 0, 0);

		overlay.add(roi, String.valueOf(roiNumber));
		overlay.setLabelColor(Globals.nameToColor(String.valueOf(labelColorComboBox.getSelectedItem())));
		overlay.setStrokeColor(Globals.nameToColor(String.valueOf(strokeColorComboBox.getSelectedItem())));

		image.setOverlay(overlay);
		roiNumber++;

		show();
	}

	public void saveMask(String directoryPath) {
		IJ.run(image, "Select None", "");

		ImageProcessor mask = image.createRoiMask();
		ImagePlus maskImage = new ImagePlus("mask", mask);

		IJ.saveAs(maskImage, "PNG",
				directoryPath + "/" + Globals.getNameWithoutExtension(image.getTitle()).replace("_", "") + "_mask.png");
	}

	public void saveOverlay(String directoryPath) {
		IJ.saveAs(image, "PNG", directoryPath + "/" + Globals.getNameWithoutExtension(image.getTitle()).replace("_", "")
				+ "_annotations.png");
	}

	public void saveAnnotation(String directoryPath) {
		File annotationCSVFile = new File(directoryPath + "/" + image.getShortTitle() + "_annotation.csv");

		// add metadata
		Globals.writeCSV(annotationCSVFile,
				Arrays.asList(image.getShortTitle(), String.valueOf(overlay.size()), Globals.getFormattedDate()));

		for (int i = 0; i < overlay.size(); i++) {
			Roi roi = overlay.get(i);
			Polygon pol = roi.getPolygon();

			Globals.writeCSV(annotationCSVFile, Arrays.asList(roi.getName(), String.valueOf(pol.npoints)));

			for (int j = 0; j < pol.npoints; j++)
				Globals.writeCSV(annotationCSVFile,
						Arrays.asList(String.valueOf(pol.xpoints[j]), String.valueOf(pol.ypoints[j])));

		}
	}

	public void closeImage() {
		image.close();
	}

	public int roiCount() {
		return overlay.size();
	}

	public void show() {
		displayPanel.removeAll();

		for (int i = 0; i < overlay.size(); i++) {
			final int iCopy = i;
			Roi roi = overlay.get(i);

			JLabel roiNameLabel = new JLabel(roi.getName());
			roiNameLabel.setHorizontalAlignment(SwingConstants.LEFT);
			roiNameLabel.setFont(new Font("Roboto", Font.PLAIN, 15));
			roiNameLabel.setBounds(10, 10 + i * 25, 200, 20);
			roiNameLabel.setBackground(new Color(252, 252, 252));
			roiNameLabel.setOpaque(true);
			roiNameLabel.setVisible(true);

			JButton deleteButton = new JButton("Delete");
			deleteButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					IJ.run(image, "Select None", "");

					overlay.remove(iCopy);
					image.setOverlay(overlay);
					show();
				}
			});
			deleteButton.setVerticalTextPosition(SwingConstants.CENTER);
			deleteButton.setHorizontalTextPosition(SwingConstants.CENTER);
			deleteButton.setForeground(Color.WHITE);
			deleteButton.setFont(new Font("Arial", Font.BOLD, 15));
			deleteButton.setBackground(new Color(13, 59, 102));
			deleteButton.setBounds(230, 10 + i * 25, 100, 20);
			displayPanel.add(deleteButton);

			JButton renameButton = new JButton("Rename");
			renameButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					IJ.run(image, "Select None", "");

					JFrame dialog = new JFrame();
					String s = (String) JOptionPane.showInputDialog(dialog, "Set selection name:\n", "MOSES",
							JOptionPane.PLAIN_MESSAGE, null, null, null);

					overlay.get(iCopy).setName(s);
					image.setOverlay(overlay);
					show();
				}
			});
			renameButton.setVerticalTextPosition(SwingConstants.CENTER);
			renameButton.setHorizontalTextPosition(SwingConstants.CENTER);
			renameButton.setForeground(Color.WHITE);
			renameButton.setFont(new Font("Arial", Font.BOLD, 15));
			renameButton.setBackground(new Color(13, 59, 102));
			renameButton.setBounds(350, 10 + i * 25, 100, 20);
			displayPanel.add(renameButton);

			displayPanel.add(roiNameLabel);
		}
		Globals.updatePanelSize(displayPanel);
	}

}
