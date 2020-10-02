import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import ij.IJ;

public class MatlabMetadata {
	private String parentFile, fileType, trackType, matlabFilePath;
	private boolean denseTracking;
	private int height, width, frames, numberSuperpixels, levels, winSize, iterations, polyN, flags, KNeighbor;
	private static double pyrScale, polySigma, downsizeFactor, MOSESMeshDistanceThreshold, radialMeshDistanceThresholda;
	private List<Integer> channels;

	public MatlabMetadata(String path) {
		matlabFilePath = path;

		String temporaryDirectorPath = System.getProperty("java.io.tmpdir");
		String scriptPath = temporaryDirectorPath + "get_matlab_metadata.py";
		File file = new File(scriptPath);

		PythonScript script = new PythonScript("Get metadata");
		script.importModule("sys");
		script.importModule("scipy.io", "spio");
		script.addScript(PythonScript.setValue("file", PythonScript.callFunction("spio.loadmat", "sys.argv[1]")));
		script.newLine();

		script.addScript(PythonScript.print("str(file['metadata'][0][0][0][0][0])"));
		script.addScript(PythonScript.print("str(file['metadata'][0][0][0][1][0][0])"));
		script.addScript(PythonScript.print("str(file['metadata'][0][0][0][1][0][1])"));
		script.addScript(PythonScript.print("str(file['metadata'][0][0][0][1][0][2])"));
		script.addScript(PythonScript.print("file['metadata'][0][0][0][2][0]"));
		script.addScript(PythonScript.print("float(file['metadata'][0][0][0][3][0])"));
		script.addScript(PythonScript.print("str(file['metadata'][0][0][0][4][0])"));

		script.addScript(PythonScript.print("str(file['metadata'][0][1][0])"));
		script.addScript(PythonScript.print("str(file['metadata'][0][1][1])"));
		script.addScript(PythonScript.print("str(file['metadata'][0][1][2])"));
		script.addScript(PythonScript.print("str(file['metadata'][0][1][3])"));
		script.addScript(PythonScript.print("str(file['metadata'][0][1][4])"));
		script.addScript(PythonScript.print("str(file['metadata'][0][1][5])"));
		script.addScript(PythonScript.print("str(file['metadata'][0][1][6])"));
		script.addScript(PythonScript.print("str(file['metadata'][0][1][7])"));
		script.addScript(PythonScript.print("str(file['metadata'][0][1][8])"));
		script.addScript(PythonScript.print("str(file['metadata'][0][1][9])"));
		script.newLine();

		script.addScript(PythonScript.setValue("fileType", "str(file['metadata'][0][0][0][4][0])"));
		script.startIf("fileType == 'MOSES_mesh' or fileType == 'radial_mesh' or fileType == 'neighbor_mesh'");
		script.addScript(PythonScript.print("str(file['metadata'][0][2][0][0])"));
		script.stopIf();

		try {
			FileWriter writer = new FileWriter(file);
			writer.write(script.getScript());
			writer.close();
		} catch (IOException e2) {
			IJ.handleException(e2);
		}

		ProcessBuilder pb = new ProcessBuilder("python", scriptPath, path);
		List<String> metadata = new ArrayList<>();
		try {
			Process p = pb.start();
			p.waitFor();
			BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));

			String output;
			while ((output = in.readLine()) != null)
				metadata.add(output);

		} catch (IOException | InterruptedException e1) {
			IJ.handleException(e1);
		}

		parentFile = metadata.get(0).replaceAll(" ", "");
		height = Integer.parseInt(metadata.get(1).replaceAll(" ", ""));
		width = Integer.parseInt(metadata.get(2).replaceAll(" ", ""));
		frames = Integer.parseInt(metadata.get(3).replaceAll(" ", ""));

		String[] channelList = metadata.get(4).replace("[", "").replace("]", "").split(" ");
		channels = new ArrayList<Integer>();
		for (String channel : channelList)
			channels.add(Integer.parseInt(channel));

		downsizeFactor = Double.parseDouble(metadata.get(5).replaceAll(" ", ""));
		fileType = metadata.get(6).replaceAll(" ", "");

		trackType = metadata.get(7).replaceAll(" ", "");
		denseTracking = Boolean.parseBoolean(metadata.get(8).replaceAll(" ", ""));
		numberSuperpixels = Integer.parseInt(metadata.get(9).replaceAll(" ", ""));
		pyrScale = Double.parseDouble(metadata.get(10).replaceAll(" ", ""));
		levels = Integer.parseInt(metadata.get(11).replaceAll(" ", ""));
		winSize = Integer.parseInt(metadata.get(12).replaceAll(" ", ""));
		iterations = Integer.parseInt(metadata.get(13).replaceAll(" ", ""));
		polyN = Integer.parseInt(metadata.get(14).replaceAll(" ", ""));
		polySigma = Double.parseDouble(metadata.get(15).replaceAll(" ", ""));
		flags = Integer.parseInt(metadata.get(16).replaceAll(" ", ""));

		if (fileType.equals("MOSES_mesh"))
			MOSESMeshDistanceThreshold = Double.parseDouble(metadata.get(17).replaceAll(" ", ""));
		if (fileType.equals("radial_mesh"))
			radialMeshDistanceThresholda = Double.parseDouble(metadata.get(17).replaceAll(" ", ""));
		if (fileType.equals("neighbor_mesh"))
			KNeighbor = Integer.parseInt(metadata.get(17).replaceAll(" ", ""));

		file.delete();
	}

	public String getParentFile() {
		return parentFile;
	}

	public double getMOSESMeshDistanceThreshold() {
		return MOSESMeshDistanceThreshold;
	}

	public String getFileType() {
		return fileType;
	}

	public int getFrames() {
		return frames;
	}

	public int getNumberSuperpixels() {
		return numberSuperpixels;
	}

	public double getDownsizeFactor() {
		return downsizeFactor;
	}

	public String getTrackType() {
		return trackType;
	}

	public List<Integer> getChannels() {
		return channels;
	}

	public List<String> tracksParametersList() {
		List<String> result = new ArrayList<String>();

		result.add("channels = [" + String.join(";", Globals.convertStringList(channels)) + "]");
		result.add("downsize factor = " + downsizeFactor);
		result.add("dense tracking = " + denseTracking);
		result.add("tracks type = " + trackType);
		result.add("number of superpixels = " + numberSuperpixels);
		result.add("scale factor = " + pyrScale);
		result.add("levels = " + levels);
		result.add("window size = " + winSize);
		result.add("iterations = " + iterations);
		result.add("poly n = " + polyN);
		result.add("poly sigma = " + polySigma);
		result.add("flags = " + flags);

		return result;
	}
}
