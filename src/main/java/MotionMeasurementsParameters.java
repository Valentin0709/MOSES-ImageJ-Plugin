import java.util.ArrayList;
import java.util.List;

public class MotionMeasurementsParameters {
	static String motionTracksFilePath, motionTracksFileName, motionTracksSimpleFileName, motionTracksFileDirectory;
	static List<String> motionTracksNames;

	public static void initialise() {
	}

	public static void setMotionTracksFilePath(String s) {
		motionTracksFilePath = s;
		motionTracksFileName = Globals.getName(s);
		motionTracksSimpleFileName = Globals.getNameWithoutExtension(s);
		motionTracksFileDirectory = Globals.getDirectory(s);

		motionTracksNames = new ArrayList<String>();
	}

	public static void addMotionTrack(String s) {
		motionTracksNames.add(s);
	}

}
