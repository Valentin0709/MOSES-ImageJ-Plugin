import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MeshMetricsParameters {
	private static List<String> MOSESMeshFilePaths;
	private static List<String> outputNames;
	private static Map<String, List<String>> MOSESMeshSubfiles;
	private static Map<String, Boolean> outputs = new HashMap<String, Boolean>();
	private static Map<String, SaveOption> saveOptions = new HashMap<String, SaveOption>();
	private static String saveDirectory;
	private static boolean batchMode, normaliseValues, averageValues;
	private static int fileCount, lastFrames;

	public static void initialise() {
		resetMOSESMesh();
		batchMode = false;
		normaliseValues = averageValues = false;
		lastFrames = 5;

		outputNames = new ArrayList<String>();
		outputNames.addAll(Arrays.asList("config_file", "mesh_strain_curve", "stability_index"));

		for (String outputName : outputNames)
			outputs.put(outputName, false);
	}

	public static void resetMOSESMesh() {
		MOSESMeshFilePaths = new ArrayList<String>();
		MOSESMeshSubfiles = new HashMap<String, List<String>>();
		fileCount = 0;
	}

	public static void addMOSESMeshFilePath(String s) {
		MOSESMeshFilePaths.add(s);
		MOSESMeshSubfiles.put(s, new ArrayList<String>());
		fileCount++;
	}

	public static void addMOSESMeshFilePath(List<String> list) {
		for (String s : list) {
			MOSESMeshFilePaths.add(s);
			MOSESMeshSubfiles.put(s, new ArrayList<String>());
			fileCount++;
		}
	}

	public static void addMOSESMeshSubfile(String parentFile, String s) {
		List<String> subfiles = MOSESMeshSubfiles.get(parentFile);
		subfiles.add(s);
		MOSESMeshSubfiles.put(parentFile, subfiles);
	}

	public static void setBatchMode(boolean b) {
		batchMode = b;
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

	public static boolean getBatchMode() {
		return batchMode;
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

	public static List<String> getMOSESMeshFilePaths() {
		return MOSESMeshFilePaths;
	}

	public static List<String> getMOSESMeshSubfiles(String s) {
		return MOSESMeshSubfiles.get(s);
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

	public static List<String> getSubfilesList() {
		List<String> result = new ArrayList<String>();

		for (String filePath : MOSESMeshFilePaths) {
			List<String> subfiles = MOSESMeshSubfiles.get(filePath);
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
		return outputList;
	}

}
