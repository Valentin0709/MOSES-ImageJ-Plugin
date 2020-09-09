import java.awt.Component;
import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.scijava.ui.UIService;

import ij.IJ;
import ij.ImagePlus;

public class Globals {
	public static int frameHight = 500, frameWidth = 500;
	public static int installStatus;

	// method that returns file extension from file path

	public static String getExtension(String filePath) {
		String fileName = filePath.substring(filePath.lastIndexOf("\\"));
		String extension = fileName.substring(fileName.lastIndexOf("."));

		return extension;
	}

	// method that returns file name from file path

	public static String getName(String filePath) {
		String fileName = filePath.substring(filePath.lastIndexOf("\\") + 1);

		return fileName.replaceAll("\\s+", "");
	}

	public static String getNameWithoutExtension(String filePath) {
		String fileName = filePath.substring(filePath.lastIndexOf("\\") + 1, filePath.lastIndexOf("."));

		return fileName.replaceAll("\\s+", "");
	}

	// method that returns file directory

	public static String getDirectory(String filePath) {
		String fileName = filePath.substring(0, filePath.lastIndexOf("\\") + 1);

		return fileName.replaceAll("\\s+", "");
	}

	// method that imports a file from the 'open file' window provided by the
	// UIService

	public static String openFile(UIService ui, ArrayList<String> extensions, boolean open) {
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
				String filePath = file.getPath();

				if (open) {
					ImagePlus image = new ImagePlus(filePath);
					ui.show(image);
				}

				return filePath;
			} else
				return null;
		} else
			return null;
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

	public static boolean checkMOSESInstallationStatus() {
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
			p.waitFor();
			BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));

			// result = 0 or 1
			int result = new Integer(in.readLine()).intValue();

			if (result == 1)
				return true;
			else
				return false;

		} catch (IOException | InterruptedException e1) {
			IJ.handleException(e1);
		}

		file.delete();

		return false;
	}

	public static List<String> getMatlabFiles(String path) {
		List<String> output = new ArrayList<String>();

		String temporaryDirectorPath = System.getProperty("java.io.tmpdir");
		String scriptPath = temporaryDirectorPath + "get_matlab_files.py";
		File file = new File(scriptPath);

		try {
			FileWriter writer = new FileWriter(file);
			writer.write("import scipy.io as sio\r\n" + "import sys\r\n" + "files = sio.whosmat(sys.argv[1])\r\n"
					+ "for i in range(len(files)):\r\n" + "    print(files[i][0])");
			writer.close();
		} catch (IOException e2) {
			IJ.handleException(e2);
		}

		ProcessBuilder pb = new ProcessBuilder("python", scriptPath, path);
		try {
			Process p = pb.start();
			p.waitFor();
			BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));

			String fileName;
			while ((fileName = in.readLine()) != null)
				output.add(fileName);

		} catch (IOException | InterruptedException e1) {
			IJ.handleException(e1);
		}

		file.delete();

		return output;
	}

	public static String getMatlabMetadata(String path) {
		String fileName = null;

		String temporaryDirectorPath = System.getProperty("java.io.tmpdir");
		String scriptPath = temporaryDirectorPath + "get_matlab_metadata.py";
		File file = new File(scriptPath);

		try {
			FileWriter writer = new FileWriter(file);
			writer.write("import scipy.io as spio\r\n" + "import sys\r\n" + "file = spio.loadmat(sys.argv[1])\r\n"
					+ "print(str(file['metadata'][0][2]).replace('[','').replace(']','').replace('\\'',''))");

			writer.close();
		} catch (IOException e2) {
			IJ.handleException(e2);
		}

		ProcessBuilder pb = new ProcessBuilder("python", scriptPath, path);
		try {
			Process p = pb.start();
			p.waitFor();
			BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));

			fileName = in.readLine();

		} catch (IOException | InterruptedException e1) {
			IJ.handleException(e1);
		}

		file.delete();

		return fileName;
	}

	public static boolean checkPythonInstallationStatus() {

		ProcessBuilder pb = new ProcessBuilder("python", "--version");
		try {
			Process p = pb.start();
			p.waitFor();

			if (p.exitValue() == 0)
				return true;
			else
				return false;

		} catch (IOException | InterruptedException e1) {
			IJ.handleException(e1);
		}

		return false;
	}

	public static void moveComponent(Component component2, int x, int y) {
		component2.setBounds(component2.getBounds().x + x, component2.getBounds().y + y, component2.getWidth(),
				component2.getHeight());
	}

	public static void setPanelEnabled(JPanel panel, boolean enabled) {
		Component[] components = panel.getComponents();

		panel.setEnabled(enabled);
		for (Component component : components) {
			component.setEnabled(enabled);
			if (component instanceof JPanel)
				setPanelEnabled((JPanel) component, enabled);

		}

		panel.revalidate();
		panel.repaint();
	}

	public static void setPanelVisibility(JPanel panel, boolean visibility) {
		Component[] components = panel.getComponents();

		panel.setVisible(visibility);
		panel.setOpaque(visibility);
		for (Component component : components) {
			component.setVisible(visibility);
			if (component instanceof JPanel)
				setPanelEnabled((JPanel) component, visibility);

		}

		panel.revalidate();
		panel.repaint();
	}

	public static void updatePanelSize(JPanel panel) {
		Component[] components = panel.getComponents();

		int maxHeight = 0;
		for (Component component : components) {
			if (component.isVisible() && component.isOpaque()
					&& component.getBounds().y + component.getBounds().height > maxHeight)
				maxHeight = component.getBounds().y + component.getBounds().height;
		}

		panel.setPreferredSize(new Dimension(0, maxHeight + 20));

		SwingUtilities.getAncestorOfClass(JFrame.class, panel).validate();
		SwingUtilities.getAncestorOfClass(JFrame.class, panel).repaint();
	}
}
