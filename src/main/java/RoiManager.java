import java.awt.Color;
import java.awt.Font;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.ImageCanvas;
import ij.gui.ImageWindow;
import ij.gui.Overlay;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.plugin.ChannelSplitter;
import ij.process.ImageProcessor;

public class RoiManager {
	ImagePlus image, tracksImage;
	boolean okTracks;
	Roi lastRoi;
	JPanel displayPanel;
	Overlay overlay;
	int roiNumber;
	String tracksPath;
	JComboBox labelColorComboBox, strokeColorComboBox;
	private ProgressPanel progress;
	ImageWindow window;
	List<Integer> tracks;
	MatlabMetadata tracksMetadata;
	int superpixelLength;

	public RoiManager(ImagePlus img, JPanel panel, JComboBox labelColor, JComboBox strokeColor, ProgressPanel p) {
		image = img;

		window = image.getWindow();
		ImageCanvas canvas = window.getCanvas();
		canvas.addMouseListener(new MouseAdapter() {
			@Override

			public void mousePressed(MouseEvent e) {
				if (okTracks) {
					int index = searchTracks(e.getX(), e.getY());

					if (index != -1) {
						ImagePlus selectedTracksImage = tracksImage.duplicate();
						selectedTracksImage.setPosition(tracksImage.getC(), tracksImage.getZ(), tracksImage.getT());

						ImageStack redChannelStack = ChannelSplitter.getChannel(selectedTracksImage, 1);
						ImageStack greenChannelStack = ChannelSplitter.getChannel(selectedTracksImage, 2);
						ImageStack blueChannelStack = ChannelSplitter.getChannel(selectedTracksImage, 3);

						ImageProcessor redProcessor = redChannelStack.getProcessor(tracksImage.getZ());
						ImageProcessor greeenProcessor = greenChannelStack.getProcessor(tracksImage.getZ());
						ImageProcessor blueProcessor = blueChannelStack.getProcessor(tracksImage.getZ());

						for (int i = 0; i < tracksMetadata.getFrames(); i++) {

							int k;
							if (tracksMetadata.getTrackType().equals("forward"))
								k = tracksMetadata.getNumberSuperpixels() * i + index;
							else
								k = tracksMetadata.getNumberSuperpixels() * (tracksMetadata.getFrames() - i - 1)
										+ index;

							drawRect(tracks.get(2 * k), tracks.get(2 * k + 1), 2, redProcessor, greeenProcessor,
									blueProcessor, "blue");
						}

						window.setImage(selectedTracksImage);
					}
				}
			}

			public void mouseReleased(MouseEvent e) {
				if (okTracks)
					window.setImage(tracksImage);
			}

		});

		displayPanel = panel;
		labelColorComboBox = labelColor;
		strokeColorComboBox = strokeColor;
		progress = p;
		lastRoi = null;
		superpixelLength = 4;

		image.createNewRoi(0, 0);

		tracksImage = image.duplicate();
		tracksImage.setTitle(image.getTitle());
		okTracks = false;

		labelColorComboBox.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				overlay.setLabelColor(Globals.nameToColor(String.valueOf(labelColorComboBox.getSelectedItem())));
				image.setOverlay(overlay);
				showSelectionList();
			}
		});
		strokeColorComboBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				overlay.setStrokeColor(Globals.nameToColor(String.valueOf(strokeColorComboBox.getSelectedItem())));
				image.setOverlay(overlay);
				showSelectionList();
			}
		});

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

		JFrame dialog = new JFrame();
		Object[] options = { "Ok" };

		if (roi != null) {
			if (roi != lastRoi) {
				lastRoi = roi;
				roi.setPosition(0, 0, 0);

				overlay.add(roi, "id" + String.valueOf(roiNumber));
				overlay.setLabelColor(Globals.nameToColor(String.valueOf(labelColorComboBox.getSelectedItem())));
				overlay.setStrokeColor(Globals.nameToColor(String.valueOf(strokeColorComboBox.getSelectedItem())));

				image.setOverlay(overlay);
				tracksImage.setOverlay(overlay);

				roiNumber++;

				showSelectionList();
			} else
				JOptionPane.showOptionDialog(dialog, "Selection already added.", "MOSES", JOptionPane.YES_NO_OPTION,
						JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
		} else
			JOptionPane.showOptionDialog(dialog, "No selection made.", "MOSES", JOptionPane.YES_NO_OPTION,
					JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

	}

	public void saveMask(String directoryPath) {
		IJ.run(image, "Select None", "");

		ImageProcessor mask = image.createRoiMask();
		ImagePlus maskImage = new ImagePlus("mask", mask);

		IJ.saveAs(maskImage, "PNG",
				directoryPath + "/" + Globals.getNameWithoutExtension(image.getTitle()).replace("_", "") + "_mask_f"
						+ image.getZ() + ".png");
	}

	public void saveOverlay(String directoryPath) {
		ImagePlus imageSnap = image.duplicate();

		IJ.saveAs(imageSnap, "PNG",
				directoryPath + "/" + Globals.getNameWithoutExtension(image.getTitle()).replace("_", "") + "_snapshot_f"
						+ image.getZ() + ".png");
	}

	public void saveAnnotation(String directoryPath) {
		File annotationCSVFile = new File(
				directoryPath + "/" + image.getShortTitle() + "_annotation_f" + image.getZ() + ".csv");

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

	public void saveBoundingBox(String directoryPath) {
		File bbTextFile = new File(
				directoryPath + "/" + image.getShortTitle() + "_bounding_box_f" + image.getZ() + ".txt");

		for (int i = 0; i < overlay.size(); i++) {
			Roi roi = overlay.get(i);
			Polygon pol = roi.getPolygon();

			Rectangle bb = roi.getBounds();
			Globals.writeTXT(bbTextFile,
					Arrays.asList(roi.getName(), String.valueOf(bb.getCenterX() / image.getWidth()),
							String.valueOf(bb.getCenterY() / image.getHeight()),
							String.valueOf(bb.getWidth() / image.getWidth()),
							String.valueOf(bb.getHeight() / image.getHeight())));

		}
	}

	public void closeImage() {
		image.close();
	}

	public int roiCount() {
		return overlay.size();
	}

	public void showSelectionList() {
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
					showSelectionList();
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
					showSelectionList();
				}
			});
			renameButton.setVerticalTextPosition(SwingConstants.CENTER);
			renameButton.setHorizontalTextPosition(SwingConstants.CENTER);
			renameButton.setForeground(Color.WHITE);
			renameButton.setFont(new Font("Arial", Font.BOLD, 15));
			renameButton.setBackground(new Color(13, 59, 102));
			renameButton.setBounds(345, 10 + i * 25, 100, 20);
			displayPanel.add(renameButton);

			displayPanel.add(roiNameLabel);
		}
		Globals.updatePanelSize(displayPanel);
	}

	public void setTracksPath(String s) {
		tracksPath = s;
	}

	public int searchTracks(int x, int y) {
		int index = -1;

		int frame;
		if (tracksMetadata.getTrackType().equals("forward"))
			frame = tracksImage.getZ();
		else
			frame = tracksMetadata.getFrames() - tracksImage.getZ() + 1;

		for (int k = tracksMetadata.getNumberSuperpixels() * (frame - 1); k < tracksMetadata.getNumberSuperpixels()
				* frame; k++) {
			int sx = tracks.get(2 * k), sy = tracks.get(2 * k + 1);

			if (x >= sx - superpixelLength / 2 && x <= sx + superpixelLength / 2 && y >= sy - superpixelLength / 2
					&& y <= sy + superpixelLength / 2) {

				index = k - tracksMetadata.getNumberSuperpixels() * (frame - 1);
				break;
			}
		}

		return index;
	}

	public void showTracks() {
		class GetTracks extends SwingWorker<String, String> {

			protected String doInBackground() throws Exception {
				progress.setVisibility(true);
				progress.setIndeterminate(true);
				progress.setMessage("Processing motion tracks..");

				PythonScript script = new PythonScript("Get tracks");

				script.importModule("sys");
				script.importModule("scipy.io", "spio");

				script.addParameter(Arrays.asList(new Parameter("tracksPath", "str", tracksPath)));
				script.addScript(script.createParameterDictionary());

				script.addScript(PythonScript.setValue("tracksFile", "spio.loadmat(parameters.get('tracksPath'))"));
				script.addScript(PythonScript.setValue("tracksFileInfo", "spio.whosmat(parameters.get('tracksPath'))"));
				script.addScript(PythonScript.setValue("channels", "tracksFile['metadata'][0][0][0][2][0]"));

				script.startFor("i", "len(channels)");
				script.addScript(PythonScript.setValue("tracks", "tracksFile[tracksFileInfo[i][0]]"));
				script.startFor("j", "tracks.shape[1]");
				script.startFor("m", "tracks.shape[0]");
				script.addScript(PythonScript.print("tracks[m, j, 1]"));
				script.addScript(PythonScript.print("tracks[m, j, 0]"));
				script.stopFor();
				script.stopFor();
				script.stopFor();

				String temporaryDirectorPath = System.getProperty("java.io.tmpdir");
				String scriptPath = temporaryDirectorPath + "get_tracks.py";
				File file = new File(scriptPath);

				try {
					FileWriter writer = new FileWriter(file);
					writer.write(script.getScript());
					writer.close();
				} catch (IOException e) {
					IJ.handleException(e);
				}

				ProcessBuilder pb = new ProcessBuilder("python", scriptPath, tracksPath);
				tracks = new ArrayList<>();
				try {
					Process p = pb.start();

					BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));

					String output;
					while ((output = in.readLine()) != null) {
						tracks.add(Integer.parseInt(output));
					}
					p.waitFor();

				} catch (IOException | InterruptedException e1) {
					IJ.handleException(e1);
				}

				tracksMetadata = new MatlabMetadata(tracksPath);

				tracksImage = image.duplicate();
				tracksImage.setTitle(image.getTitle());

				int k = 0;

				ImageStack redChannelStack = ChannelSplitter.getChannel(tracksImage, 1);
				ImageStack greenChannelStack = ChannelSplitter.getChannel(tracksImage, 2);
				ImageStack blueChannelStack = ChannelSplitter.getChannel(tracksImage, 3);

				if (tracksMetadata.getTrackType().equals("forward")) {
					for (int j = 1; j <= tracksMetadata.getFrames(); j++) {

						ImageProcessor redProcessor = redChannelStack.getProcessor(j);
						ImageProcessor greeenProcessor = greenChannelStack.getProcessor(j);
						ImageProcessor blueProcessor = blueChannelStack.getProcessor(j);

						for (int m = 0; m < tracksMetadata.getNumberSuperpixels(); m++) {
							drawRect(tracks.get(2 * k), tracks.get(2 * k + 1), 4, redProcessor, greeenProcessor,
									blueProcessor, "red");
							k++;
						}
					}
				} else {
					for (int j = tracksMetadata.getFrames(); j >= 1; j--) {

						ImageProcessor redProcessor = redChannelStack.getProcessor(j);
						ImageProcessor greeenProcessor = greenChannelStack.getProcessor(j);
						ImageProcessor blueProcessor = blueChannelStack.getProcessor(j);

						for (int m = 0; m < tracksMetadata.getNumberSuperpixels(); m++) {
							drawRect(tracks.get(2 * k), tracks.get(2 * k + 1), superpixelLength, redProcessor,
									greeenProcessor, blueProcessor, "red");
							k++;
						}
					}

				}

				return "Done.";
			}

			@Override
			protected void process(List<String> messages) {

			}

			@Override
			protected void done() {
				progress.setVisibility(false);
			}

		}

		GetTracks getTracks = new GetTracks();
		getTracks.execute();
	}

	private void drawRect(int x, int y, int l, ImageProcessor redChannelProcessor, ImageProcessor greenChannelProcessor,
			ImageProcessor blueChannelProcessor, String color) {

		for (int i = x - l / 2; i <= x + l / 2; i++) {
			for (int j = y - l / 2; j <= y + l / 2; j++) {

				if (color.equals("red")) {
					redChannelProcessor.putPixel(i, j, 256);
					greenChannelProcessor.putPixel(i, j, 0);
					blueChannelProcessor.putPixel(i, j, 0);
				}

				if (color.equals("blue")) {
					redChannelProcessor.putPixel(i, j, 0);
					greenChannelProcessor.putPixel(i, j, 0);
					blueChannelProcessor.putPixel(i, j, 256);
				}
			}
		}

	}

	public void overlayTracks() {
		tracksImage.updatePosition(image.getC(), image.getZ(), image.getT());

		window.setImage(tracksImage);
		okTracks = true;
	}

	public void noTracksOverlay() {
		image.updatePosition(tracksImage.getC(), tracksImage.getZ(), tracksImage.getT());

		window.setImage(image);
		okTracks = false;
	}

	public void deleteAll() {
		image.createNewRoi(0, 0);
		tracksImage.createNewRoi(0, 0);

		overlay = new Overlay();
		overlay.drawLabels(true);
		overlay.drawNames(true);
		overlay.setStrokeColor(Globals.nameToColor(String.valueOf(strokeColorComboBox.getSelectedItem())));
		overlay.setStrokeWidth(8.0);
		overlay.setLabelFont(new Font("Roboto", Font.BOLD, 15));
		overlay.setLabelColor(Globals.nameToColor(String.valueOf(labelColorComboBox.getSelectedItem())));

		image.setOverlay(overlay);
		tracksImage.setOverlay(overlay);

		roiNumber = 1;

		showSelectionList();
	}

	public void renameNumbers() {
		IJ.run(image, "Select None", "");

		int name = 1;

		for (int i = 0; i < overlay.size(); i++) {
			overlay.get(i).setName(String.valueOf(name));
			name++;
		}

		image.setOverlay(overlay);
		tracksImage.setOverlay(overlay);

		showSelectionList();
	}

	public void renameLetters() {
		IJ.run(image, "Select None", "");

		for (int i = 0; i < overlay.size(); i++)
			overlay.get(i).setName(String.valueOf((char) ('a' + i)));

		image.setOverlay(overlay);
		tracksImage.setOverlay(overlay);

		showSelectionList();
	}

	public void renameClass() {
		IJ.run(image, "Select None", "");

		JFrame dialog = new JFrame();
		String s = (String) JOptionPane.showInputDialog(dialog, "Set selection name:\n", "MOSES",
				JOptionPane.PLAIN_MESSAGE, null, null, null);

		for (int i = 0; i < overlay.size(); i++)
			overlay.get(i).setName(s);

		image.setOverlay(overlay);
		tracksImage.setOverlay(overlay);

		showSelectionList();
	}

	public void setAnnotation(String path) {
		String[] nameParts = Globals.getNameWithoutExtension(path).split("_");
		int frame = Integer.parseInt(nameParts[2].substring(1));

		deleteAll();

		BufferedReader csvReader;
		try {
			csvReader = new BufferedReader(new FileReader(path));

			String[] data;
			String row = csvReader.readLine(); // metadata line
			row = csvReader.readLine();
			while (row != null) {
				data = row.split(",");

				String name = data[0];
				int vertices = Integer.parseInt(data[1]);

				float[] X = new float[vertices];
				float[] Y = new float[vertices];
				for (int i = 0; i < vertices; i++) {
					row = csvReader.readLine();
					data = row.split(",");
					X[i] = Integer.parseInt(data[0]);
					Y[i] = Integer.parseInt(data[1]);
				}

				PolygonRoi roi = new PolygonRoi(X, Y, Roi.POLYGON);
				roi.setName(name);
				overlay.add(roi);

				row = csvReader.readLine();
			}

			csvReader.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

		showSelectionList();

		tracksImage.setOverlay(overlay);
		image.setOverlay(overlay);
		tracksImage.setZ(frame);
		image.setZ(frame);
	}

	public void setBoundingBox(String path) {
		String[] nameParts = Globals.getNameWithoutExtension(path).split("_");
		int frame = Integer.parseInt(nameParts[3].substring(1));

		deleteAll();

		BufferedReader txtReader;
		try {
			txtReader = new BufferedReader(new FileReader(path));

			String[] data;
			String row = txtReader.readLine();
			while (row != null) {
				data = row.split(" ");

				String name = data[0];
				double sx = Double.parseDouble(data[1]) * image.getWidth();
				double sy = Double.parseDouble(data[2]) * image.getHeight();
				double width = Double.parseDouble(data[3]) * image.getWidth();
				double height = Double.parseDouble(data[4]) * image.getHeight();

				Roi roi = new Roi(sx - width / 2, sy - height / 2, width, width);
				roi.setName(name);
				overlay.add(roi);

				row = txtReader.readLine();
			}
			txtReader.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

		showSelectionList();

		tracksImage.setOverlay(overlay);
		image.setOverlay(overlay);
		tracksImage.setZ(frame);
		image.setZ(frame);
	}
}
