import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Date;
import java.text.SimpleDateFormat;
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
	private int fileNumber;
	private Process process;
	private PythonScript script;
	private File configFile, meshStrainCurveCSVFile, stabilityIndexCSVFile;
	String currentFilePath;
	List<String> subfiles;

	public MeshMetrics(ProgressPanel p) {
		progress = p;
		fileNumber = 0;
	}

	private void createConfigFile() {
		SimpleDateFormat formatter = new SimpleDateFormat("HH'-'mm'-'dd'-'MM'-'yyyy");
		configFile = new File(MeshMetricsParameters.getSaveDirectory() + "MOSES_config_file" + "_"
				+ formatter.format(new Date(System.currentTimeMillis())) + ".txt");

		try {
			configFile.createNewFile();
		} catch (IOException e) {
			IJ.handleException(e);
		}

		String configFileHeader = "MOSES config file \r\n";
		configFileHeader += "@Action: mesh_metrics \r\n\r\n";

		configFileHeader += "@Outputs:\r\n";
		List<String> outputs = MeshMetricsParameters.getOutputList();
		for (String output : outputs)
			configFileHeader += "\t" + output + "\r\n";
		configFileHeader += "\r\n";

		configFileHeader += "@Parameters: \r\n";
		List<String> parameters = MeshMetricsParameters.getParametersList();
		for (String parameter : parameters)
			configFileHeader += "\t" + parameter + "\r\n";
		configFileHeader += "\r\n";

		FileWriter configFileWriter;
		try {
			configFileWriter = new FileWriter(configFile, true);
			configFileWriter.write(configFileHeader);
			configFileWriter.flush();
			configFileWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void writeCSV(File f, List<String> row) {
		try {
			FileWriter csvWriter = new FileWriter(f, true);

			csvWriter.append(String.join(",", row));
			csvWriter.append("\n");
			csvWriter.flush();
			csvWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void getMeshStrainCurve() {
		List<Parameter> parameters = new ArrayList<Parameter>();
		parameters.addAll(Arrays.asList(new Parameter("filePath", "str", currentFilePath),
				new Parameter("subfiles", "str", String.join(",", subfiles))));

		addScriptHeader();

		script.addCommnet("define parameters dictionary");
		script.addParameter(parameters);
		script.addScript(script.createParameterDictionary());
		script.newLine();

		script.addCommnet("import file");
		script.addScript(
				PythonScript.setValue("file", PythonScript.callFunction("spio.loadmat", "parameters.get('filePath')")));
		script.addScript(PythonScript.setValue("subfiles", "parameters.get('subfiles').split(',')"));

		script.addCommnet("compute mesh strain curve");
		for (int i = 0; i < subfiles.size(); i++) {
			script.addScript(PythonScript.print(PythonScript.addString("-Computing mesh strain curve for ")
					+ " + subfiles[" + i + "] + " + PythonScript.addString(" ...")));
			script.addScript(PythonScript.setValue("mesh_strain_time", "file[subfiles[" + i + "]]"));

			if (MeshMetricsParameters.getNormaliseValues())
				script.addScript(PythonScript.setValue("mesh_strain_" + i, PythonScript.callFunction(
						"compute_MOSES_mesh_strain_curve", Arrays.asList("mesh_strain_time", "normalise = True"))));
			else
				script.addScript(PythonScript.setValue("mesh_strain_" + i, PythonScript.callFunction(
						"compute_MOSES_mesh_strain_curve", Arrays.asList("mesh_strain_time", "normalise = False"))));

			script.newLine();

			script.startFor("k", "len(mesh_strain_" + i + ")");
			script.addScript("print('.' + subfiles[" + i + "] + ',' + str(mesh_strain_" + i + "[k]))");
			script.stopFor();
		}
		if (MeshMetricsParameters.getAverageValues()) {
			script.addScript(PythonScript.setValue("mesh_strain_av", "mesh_strain_0"));
			for (int i = 1; i < subfiles.size(); i++)
				script.addScript(PythonScript.setValue("mesh_strain_av", "mesh_strain_av + mesh_strain_" + i));
			script.addScript(PythonScript.setValue("mesh_strain_av", "mesh_strain_av / len(subfiles)"));

			script.startFor("k", "len(mesh_strain_av)");
			script.addScript("print('.' + 'video average' + ',' + str(mesh_strain_av[k]))");
			script.stopFor();
		}
	}

	private void getStabilityIndex() {
		List<Parameter> parameters = new ArrayList<Parameter>();
		parameters.addAll(Arrays.asList(new Parameter("filePath", "str", currentFilePath),
				new Parameter("subfiles", "str", String.join(",", subfiles)),
				new Parameter("lastFrames", "int", MeshMetricsParameters.getLastFrames())));

		addScriptHeader();

		script.addCommnet("define parameters dictionary");
		script.addParameter(parameters);
		script.addScript(script.createParameterDictionary());
		script.newLine();

		script.addCommnet("import file");
		script.addScript(
				PythonScript.setValue("file", PythonScript.callFunction("spio.loadmat", "parameters.get('filePath')")));
		script.addScript(PythonScript.setValue("subfiles", "parameters.get('subfiles').split(',')"));
		script.newLine();

		script.addScript(PythonScript.setValue(Arrays.asList("mesh_stability_index", "normalised_mesh_strain_curve"),
				PythonScript.callFunction("compute_MOSES_mesh_stability_index", Arrays.asList("file[subfiles[0]]",
						"file[subfiles[1]]", "last_frames = parameters.get('lastFrames')"))));
		script.addScript("print('*%.3f' %(mesh_stability_index))");

	}

	@Override
	protected String doInBackground() throws Exception {
		publish("-Processing parameters...");

		// mesh curve file
		if (MeshMetricsParameters.isOutput("mesh_strain_curve")) {
			meshStrainCurveCSVFile = new File(MeshMetricsParameters.getSaveDirectory() + "MOSES_mesh_strain_curve.csv");

			try {
				meshStrainCurveCSVFile.createNewFile();
			} catch (IOException e) {
				IJ.handleException(e);
			}

			try {
				FileWriter csvWriter = new FileWriter(meshStrainCurveCSVFile, true);

				csvWriter.append(String.join(",", Arrays.asList("File", "Output", "Mesh strain curve")));
				csvWriter.append("\n");
				csvWriter.flush();
				csvWriter.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		// stability index file
		if (MeshMetricsParameters.isOutput("stability_index")) {
			stabilityIndexCSVFile = new File(MeshMetricsParameters.getSaveDirectory() + "MOSES_stability_index.csv");

			try {
				stabilityIndexCSVFile.createNewFile();
			} catch (IOException e) {
				IJ.handleException(e);
			}

			try {
				FileWriter csvWriter = new FileWriter(stabilityIndexCSVFile, true);

				csvWriter.append(String.join(",", Arrays.asList("File", "Stability index")));
				csvWriter.append("\n");
				csvWriter.flush();
				csvWriter.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		for (String filePath : MeshMetricsParameters.getMOSESMeshFilePaths()) {
			currentFilePath = filePath;
			progress.setFileName(Globals.getName(filePath));
			fileNumber++;
			subfiles = MeshMetricsParameters.getMOSESMeshSubfiles(filePath);

			if (MeshMetricsParameters.isOutput("mesh_strain_curve") && !this.isCancelled()) {
				script = new PythonScript("Mesh strain curve");
				getMeshStrainCurve();

				runScript();
			}

			if (MeshMetricsParameters.isOutput("stability_index") && !this.isCancelled()) {
				if (subfiles.size() == 2) {
					script = new PythonScript("Stability index");
					getStabilityIndex();

					runScript();
				} else
					publish("+Warning: Stability index was not computed for " + Globals.getName(filePath)
							+ ". Please check if the file contains the right number of mesh strain .");
			}
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

//		IJ.log(script.getScript());
//		IJ.log(String.valueOf(command));

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
				writeCSV(meshStrainCurveCSVFile,
						Arrays.asList(Globals.getName(currentFilePath), splitMessage[0], splitMessage[1]));
			}
			if (messageScope == '*') {
				validMessage = false;
				writeCSV(stabilityIndexCSVFile, Arrays.asList(Globals.getName(currentFilePath), m));
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
