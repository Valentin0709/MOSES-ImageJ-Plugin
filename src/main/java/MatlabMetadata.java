import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import ij.IJ;

public class MatlabMetadata {
	private String parentFile, fileType, trackType;
	private boolean denseTracking;
	private int height, width, numberSuperpixels, levels, winSize, iterations, polyN, flags, KNeighbor;
	private static double pyrScale, polySigma, MOSESMeshDistanceThreshold, radialMeshDistanceThresholda;
	private List<Integer> channels;

	public MatlabMetadata(String path) {
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
		script.addScript(PythonScript.print("str(file['metadata'][0][0][0][2][0])"));
		script.addScript(PythonScript.print("str(file['metadata'][0][0][0][3][0])"));

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

		script.addScript(PythonScript.setValue("fileType", "str(file['metadata'][0][0][0][3][0])"));
		script.startIf("fileType == 'MOSES_mesh' or fileType == 'radial_mesh' or fileType == 'neighbor_mesh'");
		script.addScript(PythonScript.print("str(file['metadata'][0][2])"));
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
				metadata.add(output.replaceAll(" ", ""));

		} catch (IOException | InterruptedException e1) {
			IJ.handleException(e1);
		}

		parentFile = metadata.get(0);
		height = Integer.parseInt(metadata.get(1));
		width = Integer.parseInt(metadata.get(2));

		String[] channelList = metadata.get(3).replace("[", "").replace("]", "").split(" ");
		channels = new ArrayList<Integer>();
		for (String channel : channelList)
			channels.add(Integer.parseInt(channel));

		fileType = metadata.get(4);

		trackType = metadata.get(5);
		denseTracking = Boolean.parseBoolean(metadata.get(6));
		numberSuperpixels = Integer.parseInt(metadata.get(7));
		pyrScale = Double.parseDouble(metadata.get(8));
		levels = Integer.parseInt(metadata.get(9));
		winSize = Integer.parseInt(metadata.get(10));
		iterations = Integer.parseInt(metadata.get(11));
		polyN = Integer.parseInt(metadata.get(12));
		polySigma = Double.parseDouble(metadata.get(13));
		flags = Integer.parseInt(metadata.get(14));

		if (fileType.equals("MOSES_mesh"))
			MOSESMeshDistanceThreshold = Double.parseDouble(metadata.get(15));
		if (fileType.equals("radial_mesh"))
			radialMeshDistanceThresholda = Double.parseDouble(metadata.get(15));
		if (fileType.equals("neighbor_mesh"))
			KNeighbor = Integer.parseInt(metadata.get(15));

		file.delete();
	}

	public String getParentFile() {
		return parentFile;
	}

	public String getFileType() {
		return fileType;
	}

	public List<Integer> getChannels() {
		return channels;
	}
}
