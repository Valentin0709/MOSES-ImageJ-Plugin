import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
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
	public static ImagePlus lastImage;

	// method that returns file extension from file path

	public static String getExtension(String filePath) {
		// String fileName = filePath.substring(filePath.lastIndexOf("\\"));
		if (new File(filePath).isFile()) {
			String extension = filePath.substring(filePath.lastIndexOf("."));
			return extension;
		}
		return "";
	}

	// method that returns file name from file path

	public static String getName(String filePath) {
		String fileName = filePath.substring(filePath.lastIndexOf("\\") + 1);

		return fileName.replaceAll("\\s+", "");
	}

	public static List<String> getName(List<String> filePaths) {
		List<String> result = new ArrayList<String>();

		for (String filePath : filePaths) {
			String fileName = filePath.substring(filePath.lastIndexOf("\\") + 1).replaceAll("\\s+", "");
			result.add(fileName);
		}

		return result;
	}

	public static String getNameWithoutExtension(String filePath) {
		String fileName = filePath.substring(filePath.lastIndexOf("\\") + 1, filePath.lastIndexOf("."));

		return fileName.replaceAll("\\s+", "");
	}

	// method that returns file directory

	public static String getDirectory(String filePath) {
		String fileDir = filePath.substring(0, filePath.lastIndexOf("\\") + 1);

		return fileDir;
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
					lastImage = new ImagePlus(filePath);
					ui.show(lastImage);
				}

				return filePath;
			} else
				return null;
		} else
			return null;
	}

	public static boolean checkExtension(String filePathh, List<String> extensions) {
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

	public static Color nameToColor(String name) {
		if (name.equals("white"))
			return Color.white;
		if (name.equals("black"))
			return Color.black;
		if (name.equals("red"))
			return Color.red;
		if (name.equals("green"))
			return Color.green;
		if (name.equals("blue"))
			return Color.blue;
		if (name.equals("yellow"))
			return Color.yellow;
		return Color.white;
	}

	public static List<String> convertStringList(List<Integer> list) {
		List<String> result = new ArrayList<String>();
		for (Integer x : list)
			result.add(String.valueOf(x));

		return result;
	}

	public static List<File> getFiles(File[] files, List<String> validExtensions, boolean deepSearch) {
		List<File> result = new ArrayList<File>();

		for (File file : files) {
			if (file.isDirectory() && deepSearch)
				result.addAll(getFiles(file.listFiles(), validExtensions, deepSearch));
			else if (checkExtension(file.getAbsolutePath(), validExtensions))
				result.add(file);
		}
		return result;
	}

	public static void writeCSV(File f, List<String> row) {
		try {
			FileWriter csvWriter = new FileWriter(f, true);

			csvWriter.append(String.join(",", row));
			csvWriter.append("\n");
			csvWriter.flush();
			csvWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static String getFormattedDate() {
		SimpleDateFormat formatter = new SimpleDateFormat("dd'-'MM'-'yyyy'_T'HH'-'mm");
		return formatter.format(new Date(System.currentTimeMillis()));
	}

	public static File createWorkspace(String path, String name) {
		String workspacePath = path + "MOSES_Workspace_" + name;
		File workspaceFolder = new File(workspacePath);
		workspaceFolder.mkdirs();

		File commandsHistoryFile = new File(workspacePath + "/workspace_history.csv");

		// header
		Globals.writeCSV(commandsHistoryFile, Arrays.asList("MOSES_Workspace_" + name + " history"));

		return workspaceFolder;
	}

	public static String getWorkspace() {
		String workspace = IJ.getDirectory("Choose saving directory");

		if (workspace == null)
			return null;

		workspace = workspace.substring(0, workspace.length() - 1);

		if (getName(workspace).contains("MOSES_Workspace"))
			return workspace;
		else {
			JFrame dialog = new JFrame();
			Object[] options = { "Ok" };
			JOptionPane.showOptionDialog(dialog, "Invalid workspace selected.", "MOSES", JOptionPane.YES_NO_OPTION,
					JOptionPane.WARNING_MESSAGE, null, options, options[0]);

			return null;
		}
	}

	public static List<String> getProjectList(String workspacePath) {
		List<String> result = new ArrayList<String>();

		File[] files = new File(workspacePath).listFiles();
		for (File file : files)
			if (file.isDirectory())
				result.add(getName(file.getAbsolutePath()));

		return result;
	}

	public static String getParentProject(String filePath, String workspacePath) {
		String pathSection = filePath.substring(workspacePath.length() + 1);

		return pathSection.substring(0, pathSection.indexOf("\\"));
	}
}
