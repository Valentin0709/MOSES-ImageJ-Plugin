import java.util.ArrayList;
import java.util.List;

import ij.ImagePlus;

public class VisualisationFromMaskParameters {
	private static boolean batchMode, completeVis, longestTracksVis;
	private static List<ImageMaskTracks> files;
	private static List<Integer> channels;
	private static String saveDirectory;
	private static int tracksTemporalSegment;
	private static ColorOption colorOption;
	private static SaveOption saveOption;

	public static void initialise() {
		batchMode = completeVis = longestTracksVis = false;
		saveDirectory = null;
		tracksTemporalSegment = 5;
		files = new ArrayList<ImageMaskTracks>();
		channels = new ArrayList<Integer>();
	}

	public static void setBatchMode(boolean b) {
		batchMode = b;
	}

	public static boolean getBatchMode() {
		return batchMode;
	}

	public static void setFilePath(String path) {
		ImageMaskTracks imt = new ImageMaskTracks();
		imt.setImagePath(path);
		files.add(imt);
	}

	public static void setFilePath(List<String> paths) {
		for (String path : paths)
			setFilePath(path);
	}

	public static boolean setMaskPath(String path, boolean matchName, boolean matchSize) {
		ImagePlus mask = new ImagePlus(path);
		String parentFile = String.valueOf(mask.getProperty("parentFile"));

		boolean addedMaskPath = false;
		;
		for (ImageMaskTracks imt : files) {
			ImagePlus img = new ImagePlus(imt.getImagePath());

			boolean ok1 = true;
			if (!Globals.getName(imt.getImagePath()).equals(parentFile) && matchName)
				ok1 = false;

			boolean ok2 = true;
			if (!(img.getWidth() == mask.getWidth() && img.getHeight() == mask.getHeight()) && matchSize)
				ok2 = false;

			if (ok1 && ok2) {
				addedMaskPath = true;
				imt.setMaskPath(path);
			}

		}
		return addedMaskPath;
	}

	public static boolean setTrackPath(String path) {
		MatlabMetadata trackMetadata = new MatlabMetadata(path);

		boolean addedTrackPath = false;
		if (trackMetadata.getFileType().equals("tracks")) {
			for (ImageMaskTracks imt : files) {

				if (trackMetadata.getParentFile().equals(Globals.getNameWithoutExtension(imt.getImagePath()))) {
					imt.setTrackPath(path);
					addedTrackPath = true;
				}
			}
		}

		if (addedTrackPath) {
			for (Integer channel : trackMetadata.getChannels())
				if (!channels.contains(channel))
					channels.add(channel);
		}

		return addedTrackPath;
	}

	public static List<String> getImagePaths() {
		List<String> result = new ArrayList<String>();

		for (ImageMaskTracks imt : files)
			result.add(imt.getImagePath());

		return result;
	}

	public static int getFileCount() {
		return files.size();
	}

	public static List<ImageMaskTracks> getFiles() {
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

	public static void setColorOption(ColorOption co) {
		colorOption = co;
	}

	public static String getColorOption(int channelIndex) {
		return colorOption.getColor(channelIndex);
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

}
