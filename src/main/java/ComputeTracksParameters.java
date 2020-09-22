import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ComputeTracksParameters {
	private static String saveDirectory;
	private static List<String> filePaths, outputNames;
	private static int frames, width, height, channels, numberSuperpixels, levels, winSize, iterations, polyN, flags;
	private static int forwardTracksTemporalSegment, backwardTracksTemporalSegment, MOSESMeshFrame, radialMeshFrame,
			neighborMeshFrame, KNeighbor;
	private static double pyrScale, polySigma, downsizeFactor, MOSESMeshDistanceThreshold, radialMeshDistanceThreshold;
	private static boolean MOSESMeshForwardTracks, radialMeshForwardTracks, neighborMeshForwardTracks,
			denseForwardTracks, denseBackwardTracks;
	private static ArrayList<Integer> selectedChannels = new ArrayList<Integer>();
	private static Map<String, Boolean> outputs = new HashMap<String, Boolean>();
	private static Map<String, SaveOption> saveOptions = new HashMap<String, SaveOption>();
	private static Map<String, ColorOption> colorOptions = new HashMap<String, ColorOption>();

	public static void initialise() {
		filePaths = new ArrayList<String>();
		saveDirectory = null;
		frames = width = height = channels = 0;
		numberSuperpixels = 1000;
		pyrScale = 0.5;
		levels = 3;
		winSize = 15;
		iterations = 3;
		polyN = 5;
		polySigma = 1.2;
		flags = 0;
		downsizeFactor = 1;
		denseForwardTracks = denseBackwardTracks = false;
		forwardTracksTemporalSegment = backwardTracksTemporalSegment = 5;
		neighborMeshFrame = MOSESMeshFrame = radialMeshFrame = 1;
		MOSESMeshDistanceThreshold = radialMeshDistanceThreshold = 1.2;
		KNeighbor = 4;
		selectedChannels.clear();

		outputNames = new ArrayList<String>();
		outputNames.addAll(Arrays.asList("config_file", "forward_tracks", "forward_tracks_visualisation",
				"backward_tracks", "backward_tracks_visualisation", "motion_field", "MOSES_mesh",
				"MOSES_mesh_visualisation", "MOSES_mesh_frame_visualisation", "MOSES_mesh_complete_visualisation",
				"radial_mesh", "radial_mesh_visualisation", "radial_mesh_frame_visualisation",
				"radial_mesh_complete_visualisation", "neighbor_mesh", "neighbor_mesh_visualisation",
				"neighbor_mesh_frame_visualisation", "neighbor_mesh_complete_visualisation"));

		for (String outputName : outputNames)
			outputs.put(outputName, false);
	}

	public static void setFilePath(String s) {
		filePaths.add(s);
	}

	public static void setFilePath(List<String> s) {
		filePaths.addAll(s);
	}

	public static String getFilePath(int x) {
		return filePaths.get(x);
	}

	public static int getFileCount() {
		return filePaths.size();
	}

	public static void setSaveDirectory(String s) {
		saveDirectory = s;
	}

	public static String getSaveDirectory() {
		return saveDirectory;
	}

	public static void setFrames(int f) {
		frames = f;
	}

	public static int getFrames() {
		return frames;
	}

	public static void setWidth(int w) {
		width = w;
	}

	public static int getWidth() {
		return width;
	}

	public static void setHeight(int h) {
		height = h;
	}

	public static int getHeight() {
		return height;
	}

	public static void setChannels(int c) {
		channels = c;
	}

	public static int getChannels() {
		return channels;
	}

	public static void setNumberSuperpixels(int n) {
		numberSuperpixels = n;
	}

	public static int getNumberSuperpixels() {
		return numberSuperpixels;
	}

	public static void setLevels(int l) {
		levels = l;
	}

	public static int getLevels() {
		return levels;
	}

	public static void setWinSize(int w) {
		winSize = w;
	}

	public static int getWinSize() {
		return winSize;
	}

	public static void setIterations(int i) {
		iterations = i;
	}

	public static int getIterations() {
		return iterations;
	}

	public static void setPolyn(int p) {
		polyN = p;
	}

	public static int getPolyn() {
		return polyN;
	}

	public static void setFlags(int f) {
		flags = f;
	}

	public static int getFlags() {
		return flags;
	}

	public static void setMOSESMeshFrame(int f) {
		MOSESMeshFrame = f;
	}

	public static int getMOSESMeshFrame() {
		return MOSESMeshFrame;
	}

	public static void setRadialMeshFrame(int f) {
		radialMeshFrame = f;
	}

	public static int getRadialMeshFrame() {
		return radialMeshFrame;
	}

	public static void setMOSESMeshDistanceThreshold(double d) {
		MOSESMeshDistanceThreshold = d;
	}

	public static double getMOSESMeshDistanceThreshold() {
		return MOSESMeshDistanceThreshold;
	}

	public static void setRadialMeshDistanceThreshold(double d) {
		radialMeshDistanceThreshold = d;
	}

	public static double getRadialMeshDistanceThreshold() {
		return radialMeshDistanceThreshold;
	}

	public static void setPyrScale(double p) {
		pyrScale = p;
	}

	public static double getPyrScale() {
		return pyrScale;
	}

	public static void setPolySigma(double p) {
		polySigma = p;
	}

	public static double getPolySigma() {
		return polySigma;
	}

	public static void setDownsizeFactor(double d) {
		downsizeFactor = d;
	}

	public static double getDownsizeFactor() {
		return downsizeFactor;
	}

	public static void setSelectedChannel(int c) {
		selectedChannels.add(c);
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

	public static void setColorOption(String s, ColorOption c) {
		colorOptions.put(s, c);
	}

	public static String getColorOption(String s, int channelIndex) {
		return colorOptions.get(s).getColor(channelIndex);
	}

	public static void clearSelectedChannels() {
		selectedChannels.clear();
	}

	public static int getNumberSelectedChannels() {
		return selectedChannels.size();
	}

	public static int getSelectedChannels(int index) {
		return selectedChannels.get(index);
	}

	public static List<Integer> getSelectedChannels() {
		return selectedChannels;
	}

	public static void setMOSESMeshForwardTracks(boolean b) {
		MOSESMeshForwardTracks = b;
	}

	public static boolean getMOSESMeshForwardTracks() {
		return MOSESMeshForwardTracks;
	}

	public static void setRadialMeshForwardTracks(boolean b) {
		radialMeshForwardTracks = b;
	}

	public static boolean getRadialMeshForwardTracks() {
		return radialMeshForwardTracks;
	}

	public static void setNeighborMeshForwardTracks(boolean b) {
		neighborMeshForwardTracks = b;
	}

	public static boolean getNeighborMeshForwardTracks() {
		return neighborMeshForwardTracks;
	}

	public static void setForwardTracksTemporalSegment(int t) {
		forwardTracksTemporalSegment = t;
	}

	public static int getForwardTracksTemporalSegment() {
		return forwardTracksTemporalSegment;
	}

	public static void setBackwardTracksTemporalSegment(int t) {
		backwardTracksTemporalSegment = t;
	}

	public static int getBackwardTracksTemporalSegment() {
		return backwardTracksTemporalSegment;
	}

	public static void setKNeighbor(int k) {
		KNeighbor = k;
	}

	public static int getKNeighbor() {
		return KNeighbor;
	}

	public static void setNeighborMeshFrame(int f) {
		neighborMeshFrame = f;
	}

	public static int getNeighborMeshFrame() {
		return neighborMeshFrame;
	}

	public static void setDenseForwardTracks(boolean b) {
		denseForwardTracks = b;
	}

	public static boolean getDenseForwardTracks() {
		return denseForwardTracks;
	}

	public static void setDenseBackwardTracks(boolean b) {
		denseForwardTracks = b;
	}

	public static boolean getDenseBackwardTracks() {
		return denseForwardTracks;
	}

	public static List<String> getFiles() {
		return filePaths;
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

		outputList.add("downsize factor = " + downsizeFactor);
		outputList.add("number superpixels = " + numberSuperpixels);
		outputList.add("pyr scale = " + pyrScale);
		outputList.add("levels = " + levels);
		outputList.add("windows size = " + winSize);
		outputList.add("iterations = " + iterations);
		outputList.add("poly n = " + polyN);
		outputList.add("poly sigma = " + polySigma);
		outputList.add("flags = " + flags);
		outputList.add("selectedChannels = [" + String.join("; ", Globals.convertStringList(selectedChannels)) + "]");

		if (isOutput("forward_tracks"))
			outputList.add("dense forward tracks = " + denseForwardTracks);

		List<String> colorList;
		if (isOutput("forward_tracks_visualisation")) {
			outputList.add("forward tracks temporal segment length = " + forwardTracksTemporalSegment);

			colorList = new ArrayList<String>();
			for (int index = 0; index < selectedChannels.size(); index++) {
				colorList.add(getColorOption("forward_tracks_colors", index));

				outputList.add("forward tracks visualisation color options = [" + String.join("; ", colorList) + "]");

				List<String> options = saveOptions.get("forward_tracks_save_options").getOptionList();
				outputList.add("forward tracks visualisation save options = [" + String.join("; ", options) + "]");
			}
		}

		if (isOutput("backward_tracks"))
			outputList.add("dense backward tracks = " + denseBackwardTracks);

		if (isOutput("backward_tracks_visualisation")) {
			outputList.add("backward tracks temporal segment length = " + backwardTracksTemporalSegment);

			colorList = new ArrayList<String>();
			for (int index = 0; index < selectedChannels.size(); index++)
				colorList.add(getColorOption("backward_tracks_colors", index));

			outputList.add("backward tracks visualisation color options = [" + String.join("; ", colorList) + "]");

			List<String> options = saveOptions.get("backward_tracks_save_options").getOptionList();
			outputList.add("backward tracks visualisation save options = [" + String.join("; ", options) + "]");
		}

		if (isOutput("motion_field")) {
			List<String> options = saveOptions.get("motion_field_save_options").getOptionList();
			outputList.add("motion field save options = [" + String.join("; ", options) + "]");
		}

		if (isOutput("MOSES_mesh")) {
			outputList.add("MOSES mesh distance threshold = " + MOSESMeshDistanceThreshold);
			if (MOSESMeshForwardTracks)
				outputList.add("MOSES mesh tracks = forward");

			else
				outputList.add("MOSES mesh tracks = backward");
		}

		if (isOutput("MOSES_mesh_visualisation")) {
			colorList = new ArrayList<String>();
			for (int index = 0; index < selectedChannels.size(); index++)
				colorList.add(getColorOption("MOSES_mesh_colors", index));

			outputList.add("MOSES mesh visualisation color options = [" + String.join("; ", colorList) + "]");
		}

		if (isOutput("MOSES_mesh_frame_visualisation"))
			outputList.add("MOSES Mesh frame = " + MOSESMeshFrame);

		if (isOutput("MOSES_mesh_complete_visualisation")) {
			List<String> options = saveOptions.get("MOSES_mesh_complete_visualisation_save_options").getOptionList();
			outputList.add("MOSES mesh visualisation save options = [" + String.join("; ", options) + "]");
		}

		if (isOutput("radial_mesh")) {
			outputList.add("radial mesh distance threshold = " + radialMeshDistanceThreshold);
			if (radialMeshForwardTracks)
				outputList.add("radial mesh forward tracks = forward");
			else
				outputList.add("radial mesh forward tracks = backward");
		}

		if (isOutput("radial_mesh_visualisation")) {
			colorList = new ArrayList<String>();
			for (int index = 0; index < selectedChannels.size(); index++)
				colorList.add(getColorOption("radial_mesh_colors", index));

			outputList.add("radial mesh visualisation color options = [" + String.join("; ", colorList) + "]");
		}

		if (isOutput("radial_mesh_frame_visualisation"))
			outputList.add("radial mesh frame = " + radialMeshFrame);

		if (isOutput("radial_mesh_complete_visualisation")) {
			List<String> options = saveOptions.get("radial_mesh_complete_visualisation_save_options").getOptionList();
			outputList.add("radial mesh visualisation save options = [" + String.join("; ", options) + "]");
		}

		if (isOutput("neighbor_mesh")) {
			outputList.add("K neighbors = " + KNeighbor);
			if (neighborMeshForwardTracks)
				outputList.add("neighbor mesh forward tracks = forward");
			else
				outputList.add("neighbor mesh forward tracks = backward");
		}

		if (isOutput("neighbor_mesh_visualisation")) {
			colorList = new ArrayList<String>();
			for (int index = 0; index < selectedChannels.size(); index++)
				colorList.add(getColorOption("neighbor_mesh_colors", index));

			outputList.add("neighbor mesh visualisation color options = [" + String.join("; ", colorList) + "]");
		}

		if (isOutput("neighbor_mesh_frame_visualisation"))
			outputList.add("neighbor mesh frame = " + neighborMeshFrame);

		if (isOutput("neighbor_mesh_complete_visualisation")) {
			List<String> options = saveOptions.get("neighbor_mesh_complete_visualisation_save_options").getOptionList();
			outputList.add("neighbor mesh visualisation save options = [" + String.join(", ", options) + "]");
		}

		return outputList;
	}
}
