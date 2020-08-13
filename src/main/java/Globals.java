import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

import org.scijava.ui.UIService;

import ij.IJ;
import ij.ImagePlus;

public class Globals {
	public static int frameHight = 500, frameWidth = 500;
	public static int installStatus;
	// public static BufferedImage currentImage = null;

	// globals for cell tracking
	public static String fileName, filePath, saveDirectory;
	public static int frames, width, height, channels, numberSuperpixels, levels, winSize, iterations, polyn, flags;
	public static double pyr_scale, polysigma;
	public static int[] activeChannels = new int[10];

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

	// method that imports a file from the 'open file' window provided by the
	// UIService

	public static void openFile(UIService ui) {
		File file = null;
		file = ui.chooseFile(null, "open");

		// check if a file was selected
		if (file != null) {
			filePath = file.getPath();
			ImagePlus image = new ImagePlus(filePath);
			// currentImage = image.crop("1-1").getBufferedImage();
			ui.show(image);
			IJ.log("Imported " + Globals.getName(filePath));
		}
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

}
