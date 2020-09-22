import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VisualisationFromMaskParameters {
	private static boolean showLabels, showAnnotations;
	private static List<ProjectImageAnnotationTracks> files;
	private static String saveDirectory, workspacePath, colorPalette;
	private static int tracksTemporalSegment, fontSize;
	private static SaveOption saveOption;
	private static List<String> outputNames;
	private static Map<String, Boolean> outputs = new HashMap<String, Boolean>();

	public static void initialise() {
		showLabels = showAnnotations = false;
		saveDirectory = workspacePath = colorPalette = null;
		tracksTemporalSegment = 5;
		fontSize = 12;
		colorPalette = "bright";
		files = new ArrayList<ProjectImageAnnotationTracks>();

		outputNames = new ArrayList<String>();
		outputNames.addAll(Arrays.asList("complete_visualisation", "longest_track", "all_tracks"));

		for (String outputName : outputNames)
			outputs.put(outputName, false);
	}

	public static List<String> getProjectNames() {
		List<String> result = new ArrayList<String>();

		for (ProjectImageAnnotationTracks imt : files)
			result.add(imt.getProjectName());

		return result;
	}

	public static void setImagePaths(List<String> paths) {
		for (ProjectImageAnnotationTracks imt : files)
			imt.setImagePath(null);

		for (String path : paths) {
			for (ProjectImageAnnotationTracks imt : files)
				if (Globals.getNameWithoutExtension(path).equals(imt.getProjectName())) {
					imt.setImagePath(path);
					break;
				}
		}

	}

	public static List<String> getImagePaths() {
		List<String> result = new ArrayList<String>();

		for (ProjectImageAnnotationTracks imt : files)
			if (imt.getImagePath() != null)
				result.add(imt.getImagePath());

		return result;
	}

	public static List<String> noImageMatch() {
		List<String> result = new ArrayList<String>();

		for (ProjectImageAnnotationTracks imt : files)
			if (imt.getImagePath() == null)
				result.add(imt.getProjectName());

		return result;
	}

	public static void setAnnotationPaths(List<String> paths) {
		for (ProjectImageAnnotationTracks imt : files)
			imt.setAnnotationPath(null);

		for (String path : paths) {
			for (ProjectImageAnnotationTracks imt : files)
				if (Globals.getParentProject(path, workspacePath).equals(imt.getProjectName())) {
					imt.setAnnotationPath(path);
					break;
				}
		}
	}

	public static List<String> getAnnotationPaths() {
		List<String> result = new ArrayList<String>();

		for (ProjectImageAnnotationTracks imt : files)
			if (imt.getAnnotationPath() != null)
				result.add(imt.getAnnotationPath());

		return result;
	}

	public static List<String> noAnnotationMatch() {
		List<String> result = new ArrayList<String>();

		for (ProjectImageAnnotationTracks imt : files)
			if (imt.getAnnotationPath() == null)
				result.add(imt.getProjectName());

		return result;
	}

	public static void setTracksPaths(List<String> paths) {
		files = new ArrayList<ProjectImageAnnotationTracks>();

		for (String path : paths) {
			ProjectImageAnnotationTracks imt = new ProjectImageAnnotationTracks();

			imt.setProjectName(Globals.getParentProject(path, workspacePath));
			imt.setTrackPath(path);
			files.add(imt);
		}
	}

	public static List<String> getTracksPaths() {
		List<String> result = new ArrayList<String>();

		for (ProjectImageAnnotationTracks imt : files)
			if (imt.getTrackPath() != null)
				result.add(imt.getTrackPath());

		return result;
	}

	public static int getFileCount() {
		return files.size();
	}

	public static List<ProjectImageAnnotationTracks> getFiles() {
		return files;
	}

	public static void setSaveDirectory(String path) {
		saveDirectory = path;
	}

	public static String getSaveDirectory() {
		return saveDirectory;
	}

	public static void setTracksTemporalSegment(int t) {
		tracksTemporalSegment = t;
	}

	public static int getTracksTemporalSegment() {
		return tracksTemporalSegment;
	}

	public static void setSaveOption(SaveOption so) {
		saveOption = so;
	}

	public static boolean getSaveOption(String ext) {
		return saveOption.getOption(ext);
	}

	public static void setWorkspace(String path) {
		workspacePath = path;
	}

	public static String getWorkspace() {
		return workspacePath;
	}

	public static void setColorPalette(String c) {
		colorPalette = c;
	}

	public static String getColorPalette() {
		return colorPalette;
	}

	public static void setLabelVisibility(boolean b) {
		showLabels = b;
	}

	public static boolean getLabelVisibility() {
		return showLabels;
	}

	public static void setAnnotationVisibility(boolean b) {
		showAnnotations = b;
	}

	public static boolean getAnnotationVisibility() {
		return showAnnotations;
	}

	public static void setFontSize(int x) {
		fontSize = x;
	}

	public static int getFontSize() {
		return fontSize;
	}

	public static void setOutput(String s) {
		outputs.put(s, true);
	}

	public static boolean isOutput(String s) {
		return outputs.get(s);
	}

	public static List<String> getOutputList() {
		List<String> outputList = new ArrayList<String>();

		for (String outputName : outputNames)
			if (isOutput(outputName))
				outputList.add(outputName);

		return outputList;
	}

	public static void trimFiles() {
		for (ProjectImageAnnotationTracks imt : files)
			if (imt.getImagePath() == null || imt.getTrackPath() == null || imt.getAnnotationPath() == null)
				files.remove(imt);
	}

	public static List<String> getParametersList() {
		List<String> outputList = new ArrayList<String>();

		outputList.add("show labels = " + showLabels);
		outputList.add("show annotations = " + showAnnotations);
		outputList.add("fontsize = " + fontSize);
		outputList.add("color palette = " + colorPalette);

		if (isOutput("complete_visualisation")) {
			outputList.add(
					"complete_visualisation save options = [" + String.join("; ", saveOption.getOptionList()) + "]");
			outputList.add("complete_visualisation tracks temporal segment length = " + tracksTemporalSegment);
		}

		return outputList;
	}

}
