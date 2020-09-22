import java.util.ArrayList;
import java.util.List;

public class VisualisationFromMaskParameters {
	private static boolean showLabels, showAnnotations, completeVis, longestTracksVis, allTracksVis;
	private static List<ProjectImageAnnotationTracks> files;
	private static List<Integer> channels;
	private static String saveDirectory, workspacePath, colorPalette;
	private static int tracksTemporalSegment, fontSize;
	private static SaveOption saveOption;

	public static void initialise() {
		allTracksVis = completeVis = longestTracksVis = showLabels = showAnnotations = false;
		saveDirectory = workspacePath = colorPalette = null;
		tracksTemporalSegment = 5;
		fontSize = 12;
		colorPalette = "bright";
		files = new ArrayList<ProjectImageAnnotationTracks>();
		channels = new ArrayList<Integer>();
	}

	public static List<String> getProjectNames() {
		List<String> result = new ArrayList<String>();

		for (ProjectImageAnnotationTracks imt : files)
			result.add(imt.getProjectName());

		return result;
	}

	public static void setImagePaths(List<String> paths) {
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

	public static List<Integer> getChannels() {
		return channels;
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

	public static void setCompleteVisualisation(boolean b) {
		completeVis = b;
	}

	public static boolean isCompleteVisualisation() {
		return completeVis;
	}

	public static void setLongestTracksVisualisation(boolean b) {
		longestTracksVis = b;
	}

	public static boolean isLongestTracksVisualisation() {
		return longestTracksVis;
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

	public static void setAllTracksVisualisation(boolean b) {
		allTracksVis = b;
	}

	public static boolean isAllTracksVisualisation() {
		return allTracksVis;
	}

}
