import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SaliencyMapParameters {
	private static List<String> motionTracksFilePaths;
	private static List<String> outputNames;
	private static Map<String, List<String>> motionTracksSubfiles;
	private static Map<String, Boolean> outputs = new HashMap<String, Boolean>();
	private static Map<String, SaveOption> saveOptions = new HashMap<String, SaveOption>();
	private static String saveDirectory;
	private static boolean batchMode;
	private static int paddingDistance, fileCount;
	private static double saliencyMapDistanceThreshold;

	public static void initialise() {
		resetMotionTracks();
		saliencyMapDistanceThreshold = 5;
		paddingDistance = 3;
		batchMode = false;

		outputNames = new ArrayList<String>();
		outputNames.addAll(Arrays.asList("config_file", "motion_saliency_map", "final_saliency_map_visualisation",
				"spatial_time_saliency_map_visualisation", "average_motion_saliency_map", "boundary_formation_index"));

		for (String outputName : outputNames)
			outputs.put(outputName, false);
	}

	public static void resetMotionTracks() {
		motionTracksFilePaths = new ArrayList<String>();
		motionTracksSubfiles = new HashMap<String, List<String>>();
		fileCount = 0;
	}

	public static void addMotionTracksFilePath(String s) {
		motionTracksFilePaths.add(s);
		motionTracksSubfiles.put(s, new ArrayList<String>());
		fileCount++;
	}

	public static void addMotionTracksFilePath(List<String> list) {
		for (String s : list) {
			motionTracksFilePaths.add(s);
			motionTracksSubfiles.put(s, new ArrayList<String>());
			fileCount++;
		}
	}

	public static void addMotionTrackSubfile(String parentFile, String s) {
		List<String> subfiles = motionTracksSubfiles.get(parentFile);
		subfiles.add(s);
		motionTracksSubfiles.put(parentFile, subfiles);
	}

	public static void setBatchMode(boolean b) {
		batchMode = b;
	}

	public static boolean getBatchMode() {
		return batchMode;
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

	public static void setSaveDirectory(String s) {
		saveDirectory = s;
	}

	public static String getSaveDirectory() {
		return saveDirectory;
	}

	public static void setOutput(String s) {
		outputs.put(s, true);
	}

	public static boolean isOutput(String s) {
		return outputs.get(s);
	}

	public static int getFileCount() {
		return fileCount;
	}

	public static List<String> getMotionTracksFilePaths() {
		return motionTracksFilePaths;
	}

	public static List<String> getMotionTracksSubfiles(String s) {
		return motionTracksSubfiles.get(s);
	}

	public static void setSaveOption(String s, SaveOption saveOption) {
		saveOptions.put(s, saveOption);
	}

	public static boolean getSaveOption(String s, String e) {
		SaveOption a = saveOptions.get(s);
		return a.getOption(e);
	}

	public static List<String> getSubfilesList() {
		List<String> result = new ArrayList<String>();

		for (String filePath : motionTracksFilePaths) {
			List<String> subfiles = motionTracksSubfiles.get(filePath);
			for (String subfile : subfiles)
				result.add(Globals.getName(filePath) + " : " + subfile);

		}

		return result;
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

		outputList.add("batchMode=" + batchMode);
		outputList.add("tracks=" + String.join(",", getSubfilesList()));

		if (isOutput("motion_saliency_map"))
			outputList.add("saliencyMapDistanceThreshold=" + saliencyMapDistanceThreshold);

		if (isOutput("spatial_time_saliency_map_visualisation")) {
			String saveList = "spatialTimeSaliencyMapVisualisationSaveOptions=";
			List<String> options = saveOptions.get("spatial_time_saliency_map_visualisation_save_options")
					.getOptionList();
			for (int index = 0; index < options.size(); index++) {
				saveList += options.get(index);
				if (index < options.size() - 1)
					saveList += ",";
			}
			outputList.add(saveList);
		}

		if (isOutput("boundary_formation_index")) {
			outputList.add("paddingDistance=" + paddingDistance);

			if (isOutput("average_motion_saliency_map")) {
				String saveList = "averageMotionSaliencyMapSaveOptions=";
				List<String> options = saveOptions.get("average_motion_saliency_map_save_options").getOptionList();
				for (int index = 0; index < options.size(); index++) {
					saveList += options.get(index);
					if (index < options.size() - 1)
						saveList += ",";
				}
				outputList.add(saveList);
			}
		}

		return outputList;
	}

}
