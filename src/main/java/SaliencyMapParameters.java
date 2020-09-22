import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SaliencyMapParameters {
	private static List<ProjectImageAnnotationTracks> files;
	private static List<String> outputNames;
	private static Map<String, Boolean> outputs = new HashMap<String, Boolean>();
	private static Map<String, SaveOption> saveOptions = new HashMap<String, SaveOption>();
	private static String workspacePath;
	private static boolean gaussianSmoothing;
	private static int paddingDistance;
	private static double saliencyMapDistanceThreshold;

	public static void initialise() {
		saliencyMapDistanceThreshold = 5;
		paddingDistance = 3;
		gaussianSmoothing = false;

		files = new ArrayList<ProjectImageAnnotationTracks>();

		outputNames = new ArrayList<String>();
		outputNames.addAll(Arrays.asList("config_file", "motion_saliency_map", "final_saliency_map_visualisation",
				"final_saliency_map_visualisation_overlay", "spatial_time_saliency_map_visualisation",
				"spatial_time_saliency_map_visualisation_overlay", "average_motion_saliency_map",
				"boundary_formation_index"));

		for (String outputName : outputNames)
			outputs.put(outputName, false);
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

	public static void deleteFiles() {
		for (ProjectImageAnnotationTracks imt : files)
			if (imt.getImagePath() == null || imt.getTrackPath() == null)
				files.remove(imt);
	}

	public static void setGaussianSmoothing(boolean b) {
		gaussianSmoothing = b;
	}

	public static boolean getGaussianSmoothing() {
		return gaussianSmoothing;
	}

	public static void setSaliencyMapDistanceThreshold(double d) {
		saliencyMapDistanceThreshold = d;
	}

	public static double getSaliencyMapDistanceThreshold() {
		return saliencyMapDistanceThreshold;
	}

	public static void setPaddingDistance(int d) {
		paddingDistance = d;
	}

	public static int getPaddingDistance() {
		return paddingDistance;
	}

	public static void setWorkspace(String s) {
		workspacePath = s;
	}

	public static String getWorkspace() {
		return workspacePath;
	}

	public static void setOutput(String s) {
		outputs.put(s, true);
	}

	public static boolean isOutput(String s) {
		return outputs.get(s);
	}

	public static int getFileCount() {
		return files.size();
	}

	public static void setSaveOption(String s, SaveOption saveOption) {
		saveOptions.put(s, saveOption);
	}

	public static boolean getSaveOption(String s, String e) {
		SaveOption a = saveOptions.get(s);
		return a.getOption(e);
	}

	public static List<String> getOutputList() {
		List<String> outputList = new ArrayList<String>();

		for (String outputName : outputNames)
			if (isOutput(outputName))
				outputList.add(outputName);

		return outputList;
	}

	public static List<String> getParametersList() {
		List<String> outputList = new ArrayList<String>();

		outputList.add("saliency map distance threshold = " + saliencyMapDistanceThreshold);
		outputList.add("gaussian smoothing = " + gaussianSmoothing);

		if (isOutput("spatial_time_saliency_map_visualisation")) {
			List<String> options = saveOptions.get("spatial_time_saliency_map_visualisation_save_options")
					.getOptionList();
			outputList
					.add("spatial time saliency map visualisation save options = [" + String.join("; ", options) + "]");
		}

		if (isOutput("average_motion_saliency_map")) {
			List<String> options = saveOptions.get("average_motion_saliency_map_save_options").getOptionList();
			outputList.add("average motion saliency map save options = [" + String.join("; ", options) + "]");
		}

		if (isOutput("boundary_formation_index"))
			outputList.add("padding distance = " + paddingDistance);

		return outputList;
	}

	public static List<ProjectImageAnnotationTracks> getFiles() {
		return files;
	}

}
