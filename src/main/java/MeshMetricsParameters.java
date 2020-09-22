import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MeshMetricsParameters {
	private static List<Pair<String, String>> files;
	private static List<String> outputNames;
	private static Map<String, Boolean> outputs = new HashMap<String, Boolean>();
	private static Map<String, SaveOption> saveOptions = new HashMap<String, SaveOption>();
	private static String workspacePath;
	private static boolean normaliseValues, averageValues;
	private static int lastFrames;

	public static void initialise() {
		normaliseValues = averageValues = false;
		lastFrames = 5;

		files = new ArrayList<Pair<String, String>>();

		outputNames = new ArrayList<String>();
		outputNames.addAll(Arrays.asList("config_file", "mesh_strain_curve", "stability_index"));

		for (String outputName : outputNames)
			outputs.put(outputName, false);
	}

	public static void resetFiles() {
		files = new ArrayList<Pair<String, String>>();
	}

	public static void setPair(String meshPath, String tracksPath) {
		files.add(new Pair<>(meshPath, tracksPath));
	}

	public static List<Pair<String, String>> getFiles() {
		return files;
	}

	public static List<String> getMeshPaths() {
		List<String> meshPaths = new ArrayList<String>();

		for (Pair<String, String> file : files)
			meshPaths.add(file.getL());

		return meshPaths;
	}

	public static List<String> getTracksPaths() {
		List<String> tracksPaths = new ArrayList<String>();

		for (Pair<String, String> file : files)
			tracksPaths.add(file.getR());

		return tracksPaths;
	}

	public static boolean getNormaliseValues() {
		return normaliseValues;
	}

	public static void setNormaliseValues(boolean b) {
		normaliseValues = b;
	}

	public static boolean getAverageValues() {
		return averageValues;
	}

	public static void setAverageValues(boolean b) {
		averageValues = b;
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

	public static void setLastFrames(int f) {
		lastFrames = f;
	}

	public static int getLastFrames() {
		return lastFrames;
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

		if (MeshMetricsParameters.isOutput("mesh_strain_curve")) {
			outputList.add("mesh strain curve normalise values = " + normaliseValues);
			outputList.add("mesh strain curve average values = " + averageValues);
		}

		if (MeshMetricsParameters.isOutput("stability_index"))
			outputList.add("last frames = " + lastFrames);

		return outputList;
	}

}
