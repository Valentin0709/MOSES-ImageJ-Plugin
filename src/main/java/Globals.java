import java.awt.Component;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.scijava.ui.UIService;

import ij.IJ;
import ij.ImagePlus;

public class Globals {
	public static int frameHight = 500, frameWidth = 500;
	public static int installStatus;
	// public static BufferedImage currentImage = null;

	// globals for cell tracking
	public static String fileName, filePath, saveDirectory;
	public static String scriptPath = System.getProperty("java.io.tmpdir") + "MOSESscript.py";
	public static int frames, width, height, channels, numberSuperpixels, levels, winSize, iterations, polyn, flags,
			numberSelectedChannels;
	public static double pyr_scale, polysigma, downsizeFactor;
	static ArrayList<Integer> selectedChannels = new ArrayList<Integer>();

	// method that returns file extension from file path

	public static String getExtension(String filePath) {
		String fileName = filePath.substring(filePath.lastIndexOf("\\"));
		String extension = fileName.substring(fileName.lastIndexOf("."));

		return extension;
	}

	// method that returns file name from file path

	public static String getName(String filePath) {
		String fileName = filePath.substring(filePath.lastIndexOf("\\") + 1);

		return fileName;
	}

	public static String getNameWithoutExtension(String filePath) {
		String fileName = filePath.substring(filePath.lastIndexOf("\\") + 1, filePath.lastIndexOf("."));

		return fileName;
	}

	// method that imports a file from the 'open file' window provided by the
	// UIService

	public static boolean openFile(UIService ui, ArrayList<String> extensions) {
		// set look and feel

		try {
			UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}

		File file = null;
		file = ui.chooseFile(null, "open");

		// check if a file was selected
		if (file != null) {
			boolean validExtension = checkExtension(file.getPath(), extensions);

			if (validExtension) {
				filePath = file.getPath();
				fileName = Globals.getName(filePath);

				ImagePlus image = new ImagePlus(filePath);
				ui.show(image);
				IJ.log("Imported " + Globals.getName(filePath));
				return true;
			} else
				return false;
		} else
			return false;
	}

	public static boolean checkExtension(String filePathh, ArrayList<String> extensions) {
		boolean validExtension = false;
		for (int i = 0; i < extensions.size(); i++) {
			String ext = extensions.get(i);
			if (Globals.getExtension(filePathh).equals(ext)) {
				validExtension = true;
				break;
			}
		}

		return validExtension;

	}

	public static void checkInstallationStatus() {
		Globals.installStatus = 0;
		Thread thread = new Thread() {

			public void run() {

				// create temporary python script file

				String temporaryDirectorPath = System.getProperty("java.io.tmpdir");
				String scriptPath = temporaryDirectorPath + "check_install.py";
				File file = new File(scriptPath);

				try {
					FileWriter writer = new FileWriter(file);
					writer.write("try:\r\n"
							+ "    from MOSES.Optical_Flow_Tracking.superpixel_track import compute_grayscale_vid_superpixel_tracks\r\n"
							+ "    print(\"1\")\r\n" + "except ModuleNotFoundError:\r\n" + "    print(\"0\")");
					writer.close();
				} catch (IOException e2) {
					IJ.handleException(e2);
				}

				ProcessBuilder pb = new ProcessBuilder("python", scriptPath);
				try {
					Process p = pb.start();
					BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));

					// result = 0 or 1
					Globals.installStatus = new Integer(in.readLine()).intValue();

				} catch (IOException e1) {
					IJ.handleException(e1);
				}

				file.delete();
			}
		};

		thread.start();

		try {
			thread.join();
		} catch (InterruptedException e1) {
			IJ.handleException(e1);
		}
	}

	public static void moveComponent(Component component2, int x, int y) {
		component2.setBounds(component2.getBounds().x + x, component2.getBounds().y + y, component2.getWidth(),
				component2.getHeight());
	}

	public static char color(int index) {
		if (index == 0)
			return 'r';
		if (index == 1)
			return 'b';
		if (index == 2)
			return 'g';
		if (index == 3)
			return 'c';
		if (index == 4)
			return 'm';
		if (index == 5)
			return 'y';
		if (index == 5)
			return 'w';
		return 'k';

	}

	public static void runScript(String pythonScript, ArrayList<String> command) {
		File file = new File(scriptPath);
		try {
			file.createNewFile();
		} catch (IOException e) {
			IJ.handleException(e);
		}

		try {
			FileWriter writer = new FileWriter(file);
			writer.write(pythonScript);
			writer.close();
		} catch (IOException e) {
			IJ.handleException(e);
		}

		ProcessBuilder pb = new ProcessBuilder(command);

		IJ.log("Parameters: " + command);
		IJ.log(pythonScript);

		try {
			Process p = pb.start();
			p.waitFor();

		} catch (IOException | InterruptedException e) {
			IJ.handleException(e);
		}

		file.delete();

	}
}
