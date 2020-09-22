import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import ij.IJ;

public class MeshMetrics extends SwingWorker<String, String> {
	private ProgressPanel progress;
	private Process process;
	private PythonScript script;
	String timestamp;
	private File CSVSaveFolder, meshStrainCurveCSVFile, stabilityIndexCSVFile;
	String meshPath, tracksPath;
	int fileNumber;
	MatlabMetadata trackMetadata;

	public MeshMetrics(ProgressPanel p) {
		progress = p;
	}

	private void recordHistory() {
		File commandsHistoryFile = new File(MeshMetricsParameters.getWorkspace() + "/workspace_history.csv");

		Globals.writeCSV(commandsHistoryFile, Arrays.asList(""));
		Globals.writeCSV(commandsHistoryFile, Arrays.asList("Action:", "mesh metrics"));
		Globals.writeCSV(commandsHistoryFile, Arrays.asList("Timestamp:", timestamp));

		Globals.writeCSV(commandsHistoryFile, Arrays.asList("Motion tracks:"));
		Globals.writeCSV(commandsHistoryFile, Arrays.asList(String.join(", ", MeshMetricsParameters.getTracksPaths())));

		Globals.writeCSV(commandsHistoryFile, Arrays.asList("MOSES meshes:"));
		Globals.writeCSV(commandsHistoryFile, Arrays.asList(String.join(", ", MeshMetricsParameters.getMeshPaths())));

		Globals.writeCSV(commandsHistoryFile, Arrays.asList("Outputs:"));
		Globals.writeCSV(commandsHistoryFile, Arrays.asList(String.join(", ", MeshMetricsParameters.getOutputList())));

		Globals.writeCSV(commandsHistoryFile, Arrays.asList("Parameters:"));
		Globals.writeCSV(commandsHistoryFile,
				Arrays.asList(String.join(", ", MeshMetricsParameters.getParametersList())));
	}

	private void getMeshStrainCurve() {
		List<Parameter> parameters = new ArrayList<Parameter>();
		parameters.addAll(Arrays.asList(new Parameter("meshPath", "str", meshPath),
				new Parameter("tracksPath", "str", tracksPath)));

		addScriptHeader();

		script.addScript(PythonScript.print(PythonScript.addString("-Computing mesh strain curve...")));

		script.addCommnet("define parameters dictionary");
		script.addParameter(parameters);
		script.addScript(script.createParameterDictionary());
		script.newLine();

		script.addCommnet("import file");
		script.addScript(PythonScript.setValue("meshFile",
				PythonScript.callFunction("spio.loadmat", "parameters.get('meshPath')")));
		script.addScript(PythonScript.setValue("meshFileInfo",
				PythonScript.callFunction("spio.whosmat", "parameters.get('meshPath')")));
		script.addScript(PythonScript.setValue("tracksFile",
				PythonScript.callFunction("spio.loadmat", "parameters.get('tracksPath')")));
		script.addScript(PythonScript.setValue("tracksFile",
				PythonScript.callFunction("spio.whosmat", "parameters.get('tracksPath')")));

		script.addCommnet("compute mesh strain curve");
		List<Integer> channels = trackMetadata.getChannels();
		for (int i = 0; i < channels.size(); i++) {
			int channelIndex = channels.get(i) + 1;

			script.addScript(PythonScript.setValue("mesh_strain_time_" + channelIndex,
					"meshFile[meshFileInfo[" + (i * 2 + 1) + "][0]]"));

			if (MeshMetricsParameters.getNormaliseValues())
				script.addScript(PythonScript.setValue("mesh_strain_" + channelIndex,
						PythonScript.callFunction("compute_MOSES_mesh_strain_curve",
								Arrays.asList("mesh_strain_time_" + channelIndex, "normalise = True"))));
			else
				script.addScript(PythonScript.setValue("mesh_strain_" + channelIndex,
						PythonScript.callFunction("compute_MOSES_mesh_strain_curve",
								Arrays.asList("mesh_strain_time_" + channelIndex, "normalise = False"))));

			script.newLine();

			script.startFor("k", "len(mesh_strain_" + channelIndex + ")");
			script.addScript("print('.' + str(" + channelIndex + ") + ',' + str(mesh_strain_" + channelIndex + "[k]))");
			script.stopFor();
		}

		if (MeshMetricsParameters.getAverageValues()) {
			List<String> meshStrainList = new ArrayList<String>();
			for (int i = 0; i < channels.size(); i++) {
				int channelIndex = channels.get(i) + 1;
				meshStrainList.add("mesh_strain_" + channelIndex);
			}

			script.addScript(PythonScript.setValue("mesh_strain_av",
					"(" + String.join("+", meshStrainList) + ")/ " + channels.size()));

			script.startFor("k", "len(mesh_strain_av)");
			script.addScript("print('.' + 'video average' + ',' + str(mesh_strain_av[k]))");
			script.stopFor();
		}
	}

	private void getStabilityIndex() {
		List<Parameter> parameters = new ArrayList<Parameter>();
		parameters.addAll(Arrays.asList(new Parameter("meshPath", "str", meshPath),
				new Parameter("tracksPath", "str", tracksPath),
				new Parameter("lastFrames", "int", MeshMetricsParameters.getLastFrames())));

		addScriptHeader();

		script.addScript(PythonScript.print(PythonScript.addString("-Computing mesh stability index...")));

		script.addCommnet("define parameters dictionary");
		script.addParameter(parameters);
		script.addScript(script.createParameterDictionary());
		script.newLine();

		script.addCommnet("import file");
		script.addScript(PythonScript.setValue("meshFile",
				PythonScript.callFunction("spio.loadmat", "parameters.get('meshPath')")));
		script.addScript(PythonScript.setValue("meshFileInfo",
				PythonScript.callFunction("spio.whosmat", "parameters.get('meshPath')")));
		script.addScript(PythonScript.setValue("tracksFile",
				PythonScript.callFunction("spio.loadmat", "parameters.get('tracksPath')")));
		script.addScript(PythonScript.setValue("tracksFile",
				PythonScript.callFunction("spio.whosmat", "parameters.get('tracksPath')")));

		List<Integer> channels = trackMetadata.getChannels();
		List<String> meshStrainTimeList = new ArrayList<String>();
		for (int i = 0; i < channels.size(); i++)
			meshStrainTimeList.add("meshFile[meshFileInfo[" + (i * 2 + 1) + "][0]]");

		script.addScript(PythonScript.setValue("mesh_strain_time_av",
				"(" + String.join("+", meshStrainTimeList) + ") / " + channels.size()));

		script.addScript(PythonScript.setValue(Arrays.asList("mesh_stability_index", "normalised_mesh_strain_curve"),
				PythonScript.callFunction("compute_MOSES_mesh_stability_index", Arrays.asList("mesh_strain_time_av",
						"mesh_strain_time_av", "last_frames = parameters.get('lastFrames')"))));
		script.addScript("print('*%.3f' %(mesh_stability_index))");

	}

	private void folderPaths() {
		CSVSaveFolder = new File(MeshMetricsParameters.getWorkspace() + "/"
				+ Globals.getParentProject(tracksPath, MeshMetricsParameters.getWorkspace())
				+ "/data_analysis/CSV_files/" + timestamp);
		CSVSaveFolder.mkdirs();

		// mesh curve file
		if (MeshMetricsParameters.isOutput("mesh_strain_curve")) {
			meshStrainCurveCSVFile = new File(CSVSaveFolder.getAbsoluteFile() + "/"
					+ Globals.getParentProject(tracksPath, MeshMetricsParameters.getWorkspace())
					+ "_MOSES_mesh_strain_curve.csv");

			try {
				meshStrainCurveCSVFile.createNewFile();
			} catch (IOException e) {
				IJ.handleException(e);
			}

			Globals.writeCSV(meshStrainCurveCSVFile, Arrays.asList("Project name:",
					Globals.getParentProject(tracksPath, MeshMetricsParameters.getWorkspace())));
			Globals.writeCSV(meshStrainCurveCSVFile,
					Arrays.asList("Tracks parameters:", String.join(", ", trackMetadata.tracksParametersList())));
			Globals.writeCSV(meshStrainCurveCSVFile,
					Arrays.asList("Mesh parameters:", "MOSES mesh distance threshold = "
							+ String.valueOf(trackMetadata.getMOSESMeshDistanceThreshold())));

			Globals.writeCSV(meshStrainCurveCSVFile, Arrays.asList(""));
			Globals.writeCSV(meshStrainCurveCSVFile, Arrays.asList("Channel", "Mesh strain curve"));
		}

		// stability index file
		if (MeshMetricsParameters.isOutput("stability_index")) {
			stabilityIndexCSVFile = new File(CSVSaveFolder.getAbsoluteFile() + "/"
					+ Globals.getParentProject(tracksPath, MeshMetricsParameters.getWorkspace())
					+ "_MOSES_stability_index.csv");

			try {
				stabilityIndexCSVFile.createNewFile();
			} catch (IOException e) {
				IJ.handleException(e);
			}

			Globals.writeCSV(stabilityIndexCSVFile, Arrays.asList("Project name:",
					Globals.getParentProject(tracksPath, MeshMetricsParameters.getWorkspace())));
			Globals.writeCSV(stabilityIndexCSVFile,
					Arrays.asList("Tracks parameters:", String.join(", ", trackMetadata.tracksParametersList())));
			Globals.writeCSV(stabilityIndexCSVFile, Arrays.asList("Mesh parameters:", "MOSES mesh distance threshold = "
					+ String.valueOf(trackMetadata.getMOSESMeshDistanceThreshold())));
			Globals.writeCSV(stabilityIndexCSVFile,
					Arrays.asList("Last frames:", String.valueOf(MeshMetricsParameters.getLastFrames())));

			Globals.writeCSV(stabilityIndexCSVFile, Arrays.asList(""));
			Globals.writeCSV(stabilityIndexCSVFile, Arrays.asList("Mesh stability index"));
		}
	}

	@Override
	protected String doInBackground() throws Exception {
		publish("-Processing parameters...");
		timestamp = Globals.getFormattedDate();
		recordHistory();

		fileNumber = 1;
		for (Pair<String, String> filePathsPair : MeshMetricsParameters.getFiles()) {
			meshPath = filePathsPair.getL();
			tracksPath = filePathsPair.getR();
			trackMetadata = new MatlabMetadata(tracksPath);

			progress.setFileName(Globals.getName(meshPath));
			folderPaths();

			if (MeshMetricsParameters.isOutput("mesh_strain_curve") && !this.isCancelled()) {
				script = new PythonScript("Mesh strain curve");
				getMeshStrainCurve();

				runScript();
			}

			if (MeshMetricsParameters.isOutput("stability_index") && !this.isCancelled()) {
				script = new PythonScript("Stability index");
				getStabilityIndex();

				runScript();
			}

			fileNumber++;

		}

		return "Done.";

	}

	public void destroy() {
		process.destroyForcibly();
		this.cancel(true);
	}

	private void addScriptHeader() {
		script.importModule("sys");
		script.importModule("os");
		script.importModule("pylab", "plt");
		script.importModule("scipy.io", "spio");
		script.importModuleFrom("compute_MOSES_mesh_strain_curve", "MOSES.Motion_Analysis.mesh_statistics_tools");
		script.importModuleFrom("compute_MOSES_mesh_stability_index", "MOSES.Motion_Analysis.mesh_statistics_tools");
	}

	private void runScript() {

		// create new temporary file and write script
		String scriptPath = System.getProperty("java.io.tmpdir") + "MOSESscript.py";
		File file = new File(scriptPath);
		try {
			file.createNewFile();
		} catch (IOException e) {
			IJ.handleException(e);
		}

		try {
			FileWriter writer = new FileWriter(file);
			writer.write(script.getScript());
			writer.close();
		} catch (IOException e) {
			IJ.handleException(e);
		}

		// construct command
		ArrayList<String> command = new ArrayList<>();
		command.add("python");
		command.add(scriptPath);
		for (Parameter parameter : script.getParameters())
			command.add(parameter.getValue());

		// run process
		ProcessBuilder pb = new ProcessBuilder(command);

		try {
			process = pb.start();

			BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line;
			while ((line = in.readLine()) != null) {
				publish(line);
				Thread.yield();
			}

			process.waitFor();
		} catch (IOException e) {
			IJ.handleException(e);
		} catch (InterruptedException ignored) {
			JFrame dialog = new JFrame();
			Object[] options = { "Ok" };
			JOptionPane.showOptionDialog(dialog, "Task was stopped before being completed", "MOSES",
					JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);
		}

		file.delete();
	}

	@Override
	protected void process(List<String> messages) {
		for (String m : messages) {
			boolean validMessage = false;
			char messageScope = m.charAt(0);
			m = m.substring(1);

			if (messageScope == '-') {
				validMessage = true;
				if (!progress.isIndeterminate()) {
					progress.setIndeterminate(true);
					progress.setStringPainted(false);
				}
			}
			if (messageScope == '>') {
				validMessage = false;
				progress.setIndeterminate(false);
				progress.setStringPainted(true);
				progress.setMaximum(Integer.parseInt(m));
				progress.setValue(0);
			}
			if (messageScope == '!') {
				validMessage = false;
				progress.setValue(Integer.parseInt(m));
				progress.setString(m + " / " + progress.getMaximum());
			}
			if (messageScope == '+') {
				validMessage = false;

				JOptionPane pane = new JOptionPane(m, JOptionPane.ERROR_MESSAGE, JOptionPane.PLAIN_MESSAGE);
				JDialog dialog = pane.createDialog(null, "MOSES");
				dialog.setModal(false);
				dialog.show();
			}
			if (messageScope == '.') {
				validMessage = false;
				String[] splitMessage = m.split(",");
				Globals.writeCSV(meshStrainCurveCSVFile, Arrays.asList(splitMessage[0], splitMessage[1]));
			}
			if (messageScope == '*') {
				validMessage = false;
				Globals.writeCSV(stabilityIndexCSVFile, Arrays.asList(m));
			}

			if (validMessage) {
				progress.setMessage("<html>" + m + "</html>");
				progress.setFileCount(MeshMetricsParameters.getFileCount());
				progress.setFileNumber(fileNumber);
			}
		}
	}

	@Override
	protected void done() {
		progress.setVisibility(false);
	}

}
