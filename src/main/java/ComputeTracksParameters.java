import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ComputeTracksParameters {
	private static String filePath, fileName, simpleFileName, fileExtension, fileDirectory, saveDirectory;
	private static int frames, width, height, channels, numberSuperpixels, levels, winSize, iterations, polyN, flags;
	private static int forwardTracksTemporalSegment, backwardTracksTemporalSegment, MOSESMeshFrame, radialMeshFrame,
			neighborMeshFrame, KNeighbor;
	private static double pyrScale, polySigma, downsizeFactor, MOSESMeshDistanceThreshold, radialMeshDistanceThreshold;
	private static boolean batchMode, MOSESMeshForwardTracks, radialMeshForwardTracks, neighborMeshForwardTracks,
			denseForwardTracks, denseBackwardTracks;
	private static ArrayList<Integer> selectedChannels = new ArrayList<Integer>();
	private static Map<String, Boolean> outputs = new HashMap<String, Boolean>();
	private static Map<String, SaveOption> saveOptions = new HashMap<String, SaveOption>();
	private static Map<String, ColorOption> colorOptions = new HashMap<String, ColorOption>();

	public static void initialise() {
		batchMode = false;
		filePath = fileName = simpleFileName = fileExtension = saveDirectory = null;
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

		outputs.put("config_file", false);
		outputs.put("forward_tracks", false);
		outputs.put("forward_tracks_visualisation", false);
		outputs.put("backward_tracks", false);
		outputs.put("backward_tracks_visualisation", false);
		outputs.put("motion_field", false);
		outputs.put("MOSES_mesh", false);
		outputs.put("MOSES_mesh_visualisation", false);
		outputs.put("MOSES_mesh_frame_visualisation", false);
		outputs.put("MOSES_mesh_complete_visualisation", false);
		outputs.put("radial_mesh", false);
		outputs.put("radial_mesh_visualisation", false);
		outputs.put("radial_mesh_frame_visualisation", false);
		outputs.put("radial_mesh_complete_visualisation", false);
		outputs.put("neighbor_mesh", false);
		outputs.put("neighbor_mesh_visualisation", false);
		outputs.put("neighbor_mesh_frame_visualisation", false);
		outputs.put("neighbor_mesh_complete_visualisation", false);
	}

	public static void setFilePath(String s) {
		filePath = s;
		fileName = Globals.getName(s);
		simpleFileName = Globals.getNameWithoutExtension(s);
		fileExtension = Globals.getExtension(s);
		fileDirectory = Globals.getDirectory(s);
	}

	public static String getFileDirectory() {
		return fileDirectory;
	}

	public static String getSimpleFileName() {
		return simpleFileName;
	}

	public static void setSaveDirectory(String s) {
		saveDirectory = s;
	}

	public static String getSaveDirectory() {
		return saveDirectory;
	}

	public static String getFileName() {
		return fileName;
	}

	public static String getFilePath() {
		return filePath;
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

	public static void setBatchMode(boolean b) {
		batchMode = b;
	}

	public static boolean getBatchMode() {
		return batchMode;
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

	public static List<String> getOutputList() {
		List<String> outputList = new ArrayList<String>();

		if (isOutput("config_file"))
			outputList.add("config_file");
		if (isOutput("forward_tracks"))
			outputList.add("forward_tracks");
		if (isOutput("forward_tracks_visualisation"))
			outputList.add("forward_tracks_visualisation");
		if (isOutput("backward_tracks"))
			outputList.add("forward_tracks");
		if (isOutput("backward_tracks_visualisation"))
			outputList.add("forward_tracks_visualisation");
		if (isOutput("motion_field"))
			outputList.add("motion_field");
		if (isOutput("MOSES_mesh"))
			outputList.add("MOSES_mesh");
		if (isOutput("MOSES_mesh_visualisation"))
			outputList.add("MOSES_mesh_visualisation");
		if (isOutput("MOSES_mesh_frame_visualisation"))
			outputList.add("MOSES_mesh_frame_visualisation");
		if (isOutput("MOSES_mesh_complete_visualisation"))
			outputList.add("MOSES_mesh_complete_visualisation");
		if (isOutput("radial_mesh"))
			outputList.add("radial_mesh");
		if (isOutput("radial_mesh_visualisation"))
			outputList.add("radial_mesh_visualisation");
		if (isOutput("radial_mesh_frame_visualisation"))
			outputList.add("radial_mesh_frame_visualisation");
		if (isOutput("radial_mesh_complete_visualisation"))
			outputList.add("radial_mesh_complete_visualisation");
		if (isOutput("neighbor_mesh"))
			outputList.add("neighbor_mesh");
		if (isOutput("neighbor_mesh_visualisation"))
			outputList.add("neighbor_mesh_visualisation");
		if (isOutput("neighbor_mesh_frame_visualisation"))
			outputList.add("neighbor_mesh_frame_visualisation");
		if (isOutput("neighbor_mesh_complete_visualisation"))
			outputList.add("neighbor_mesh_complete_visualisation");

		return outputList;
	}

	public static List<String> getParametersList() {
		List<String> outputList = new ArrayList<String>();

		outputList.add("batchMode=" + batchMode);

		if (batchMode)
			outputList.add("directory=" + fileDirectory);
		else
			outputList.add("filePath=" + filePath);

		outputList.add("downsizeFactor=" + downsizeFactor);
		outputList.add("numberSuperpixels=" + numberSuperpixels);
		outputList.add("pyrScale=" + pyrScale);
		outputList.add("levels=" + levels);
		outputList.add("winSize=" + winSize);
		outputList.add("iterations=" + iterations);
		outputList.add("polyN = " + polyN);
		outputList.add("polySigma=" + polySigma);
		outputList.add("flags=" + flags);

		String selectedChannelsList = "selectedChannels=";
		for (int index = 0; index < selectedChannels.size(); index++) {
			selectedChannelsList += selectedChannels.get(index);
			if (index < selectedChannels.size() - 1)
				selectedChannelsList += ",";
		}
		outputList.add(selectedChannelsList);

		if (isOutput("forward_tracks"))
			outputList.add("denseForwardTracks=" + denseForwardTracks);

		if (isOutput("forward_tracks_visualisation")) {
			outputList.add("forwardTracksTemporalSegment=" + forwardTracksTemporalSegment);

			String colorList = "forwardTracksVisualisationColorOptions=";
			for (int index = 0; index < selectedChannels.size(); index++) {
				colorList += getColorOption("forward_tracks_colors", index);
				if (index < selectedChannels.size() - 1)
					colorList += ",";
			}
			outputList.add(colorList);

			String saveList = "forwardTracksVisualisationSaveOptions=";
			List<String> options = saveOptions.get("forward_tracks_save_options").getOptionList();
			for (int index = 0; index < options.size(); index++) {
				saveList += options.get(index);
				if (index < options.size() - 1)
					saveList += ",";
			}
			outputList.add(saveList);
		}

		if (isOutput("backward_tracks"))
			outputList.add("denseBackwardTracks=" + denseBackwardTracks);

		if (isOutput("backward_tracks_visualisation")) {
			outputList.add("backwardTracksTemporalSegment=" + backwardTracksTemporalSegment);

			String colorList = "backwardTracksVisualisationColorOptions=";
			for (int index = 0; index < selectedChannels.size(); index++) {
				colorList += getColorOption("backward_tracks_colors", index);
				if (index < selectedChannels.size() - 1)
					colorList += ",";
			}
			outputList.add(colorList);

			String saveList = "backwardTracksVisualisationSaveOptions=";
			List<String> options = saveOptions.get("backward_tracks_save_options").getOptionList();
			for (int index = 0; index < options.size(); index++) {
				saveList += options.get(index);
				if (index < options.size() - 1)
					saveList += ",";
			}
			outputList.add(saveList);
		}

		if (isOutput("motion_field")) {
			String saveList = "motionFieldSaveOptions=";
			List<String> options = saveOptions.get("motion_field_save_options").getOptionList();
			for (int index = 0; index < options.size(); index++) {
				saveList += options.get(index);
				if (index < options.size() - 1)
					saveList += ",";
			}
			outputList.add(saveList);
		}

		if (isOutput("MOSES_mesh")) {
			outputList.add("MOSESMeshDistanceThreshold=" + MOSESMeshDistanceThreshold);
			outputList.add("MOSESMeshForwardTracks=" + MOSESMeshForwardTracks);
		}

		if (isOutput("MOSES_mesh_visualisation")) {
			String colorList = "MOSESMeshVisualisationColorOptions=";
			for (int index = 0; index < selectedChannels.size(); index++) {
				colorList += getColorOption("MOSES_mesh_colors", index);
				if (index < selectedChannels.size() - 1)
					colorList += ",";
			}
			outputList.add(colorList);
		}

		if (isOutput("MOSES_mesh_frame_visualisation"))
			outputList.add("MOSESMeshFrame=" + MOSESMeshFrame);

		if (isOutput("MOSES_mesh_complete_visualisation")) {
			String saveList = "MOSESMeshVisualisationSaveOptions=";
			List<String> options = saveOptions.get("MOSES_mesh_complete_visualisation_save_options").getOptionList();
			for (int index = 0; index < options.size(); index++) {
				saveList += options.get(index);
				if (index < options.size() - 1)
					saveList += ",";
			}
			outputList.add(saveList);
		}

		if (isOutput("radial_mesh")) {
			outputList.add("radialMeshDistanceThreshold=" + radialMeshDistanceThreshold);
			outputList.add("radialMeshForwardTracks=" + radialMeshForwardTracks);
		}

		if (isOutput("radial_mesh_visualisation")) {
			String colorList = "radialMeshVisualisationColorOptions=";
			for (int index = 0; index < selectedChannels.size(); index++) {
				colorList += getColorOption("radial_mesh_colors", index);
				if (index < selectedChannels.size() - 1)
					colorList += ",";
			}
			outputList.add(colorList);
		}

		if (isOutput("radial_mesh_frame_visualisation"))
			outputList.add("radialMeshFrame=" + radialMeshFrame);

		if (isOutput("radial_mesh_complete_visualisation")) {
			String saveList = "radialMeshVisualisationSaveOptions=";
			List<String> options = saveOptions.get("radial_mesh_complete_visualisation_save_options").getOptionList();
			for (int index = 0; index < options.size(); index++) {
				saveList += options.get(index);
				if (index < options.size() - 1)
					saveList += ",";
			}
			outputList.add(saveList);
		}

		if (isOutput("neighbor_mesh")) {
			outputList.add("KNeighbor=" + KNeighbor);
			outputList.add("neighborMeshForwardTracks=" + neighborMeshForwardTracks);
		}

		if (isOutput("neighbor_mesh_visualisation")) {
			String colorList = "neighborMeshVisualisationColorOptions=";
			for (int index = 0; index < selectedChannels.size(); index++) {
				colorList += getColorOption("neighbor_mesh_colors", index);
				if (index < selectedChannels.size() - 1)
					colorList += ",";
			}
			outputList.add(colorList);
		}

		if (isOutput("neighbor_mesh_frame_visualisation"))
			outputList.add("neighborMeshFrame=" + neighborMeshFrame);

		if (isOutput("neighbor_mesh_complete_visualisation")) {
			String saveList = "neighborMeshVisualisationSaveOptions=";
			List<String> options = saveOptions.get("neighbor_mesh_complete_visualisation_save_options").getOptionList();
			for (int index = 0; index < options.size(); index++) {
				saveList += options.get(index);
				if (index < options.size() - 1)
					saveList += ",";
			}
			outputList.add(saveList);
		}

		return outputList;
	}
}
