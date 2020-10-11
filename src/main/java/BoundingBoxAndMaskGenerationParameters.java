import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BoundingBoxAndMaskGenerationParameters {
	private static List<String> files;
	private static List<String> outputNames;
	private static Map<String, Boolean> outputs = new HashMap<String, Boolean>();
	private static Map<String, SaveOption> saveOptions = new HashMap<String, SaveOption>();
	private static String workspacePath;
	private static float BBoxConfidenceTresh;

	public static void initialise() {

		BBoxConfidenceTresh = 0.0001f;

		files = new ArrayList<String>();

		outputNames = new ArrayList<String>();
		outputNames.addAll(Arrays.asList("bounding_box", "bounding_box_vis", "mask"));

		for (String outputName : outputNames)
			outputs.put(outputName, false);
	}

	public static void setFiles(List<String> filePaths) {
		files = filePaths;
	}

	public static List<String> getFiles() {
		return files;
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

	public static void setSaveOption(String s, SaveOption saveOption) {
		saveOptions.put(s, saveOption);
	}

	public static boolean getSaveOption(String s, String e) {
		SaveOption a = saveOptions.get(s);
		return a.getOption(e);
	}

	public static void setBBoxConfidenceTresh(float t) {
		BBoxConfidenceTresh = t;
	}

	public static float getBBoxConfidenceTresh() {
		return BBoxConfidenceTresh;
	}

	public static List<String> getOutputList() {
		List<String> outputList = new ArrayList<String>();

		for (String outputName : outputNames)
			if (isOutput(outputName))
				outputList.add(outputName);

		return outputList;
	}

}
