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
import ij.ImagePlus;
import ij.plugin.FolderOpener;

public class SaliencyMap extends SwingWorker<String, String> {
	private ProgressPanel progress;
	private int fileNumber;
	private Process process;
	private PythonScript script;
	private int scriptCount;
	private File configFile, spacialTimeMotionSaliencyMapFolder, boundaryIndexCSVFile;
	private String currentFilePath, spacialTimeMotionSaliencyMapPath;
	List<String> subfiles;

	public SaliencyMap(ProgressPanel p) {
		progress = p;
		fileNumber = scriptCount = 0;
	}

	private void folderPaths() {
		spacialTimeMotionSaliencyMapPath = SaliencyMapParameters.getSaveDirectory()
				+ Globals.getNameWithoutExtension(currentFilePath) + "_spacial_time_motion_saliency_map_image_sequence";
	}

	private void createConfigFile() {
		SimpleDateFormat formatter = new SimpleDateFormat("HH'-'mm'-'dd'-'MM'-'yyyy");
		configFile = new File(SaliencyMapParameters.getSaveDirectory() + "MOSES_config_file" + "_"
				+ formatter.format(new Date(System.currentTimeMillis())) + ".txt");

		try {
			configFile.createNewFile();
		} catch (IOException e) {
			IJ.handleException(e);
		}

		String configFileHeader = "MOSES config file \r\n";
		configFileHeader += "@Action: motion_saliency_map \r\n\r\n";

		configFileHeader += "@Outputs:\r\n";
		List<String> outputs = SaliencyMapParameters.getOutputList();
		for (String output : outputs)
			configFileHeader += "\t" + output + "\r\n";
		configFileHeader += "\r\n";

		configFileHeader += "@Parameters: \r\n";
		List<String> parameters = SaliencyMapParameters.getParametersList();
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

	private void generateMotionSaliencyMap() {
		List<Parameter> parameters = new ArrayList<Parameter>();
		parameters.addAll(Arrays.asList(new Parameter("filePath", "str", currentFilePath),
				new Parameter("subfiles", "str", String.join(",", subfiles)),
				new Parameter("distanceThreshold", "float", SaliencyMapParameters.getSaliencyMapDistanceThreshold()),
				new Parameter("padding", "int", SaliencyMapParameters.getPaddingDistance()),
				new Parameter("saveDirectory", "str", SaliencyMapParameters.getSaveDirectory()),
				new Parameter("fileName", "str", Globals.getNameWithoutExtension(currentFilePath))));

		addScriptHeader();

		script.addCommnet("define parameters dictionary");
		script.addParameter(parameters);
		script.addScript(script.createParameterDictionary());
		script.newLine();

		script.addCommnet("import file");
		script.addScript(
				PythonScript.setValue("file", PythonScript.callFunction("spio.loadmat", "parameters.get('filePath')")));
		script.addScript(PythonScript.setValue("rows", "int(file['metadata'][0][0])"));
		script.addScript(PythonScript.setValue("columns", "int(file['metadata'][0][1])"));
		script.addScript(PythonScript.setValue("subfiles", "parameters.get('subfiles').split(',')"));
		script.newLine();

		script.addCommnet("compute saliency map");
		for (int i = 0; i < subfiles.size(); i++) {
			script.addScript(PythonScript.print(PythonScript.addString("-Computing motion saliency map for ")
					+ " + subfiles[" + i + "] + " + PythonScript.addString(" ...")));
			script.addScript(PythonScript.setValue("track", "file[subfiles[" + i + "]]"));
			script.addScript(PythonScript.setValue("spixel_size", "track[1,0,1] - track[1,0,0]"));
			script.addScript(
					PythonScript.setValue(Arrays.asList("final_saliency_map_" + i, "spatial_time_saliency_map_" + i),
							PythonScript.callFunction("compute_motion_saliency_map",
									Arrays.asList("track",
											"dist_thresh = parameters.get('distanceThreshold') * spixel_size",
											"shape = (rows, columns)", "filt = 1", "filt_size = spixel_size"))));
			script.newLine();
		}

		script.addCommnet("save saliency map");
		script.addScript(PythonScript.setValue("saveLocation",
				"parameters.get('saveDirectory') + parameters.get('fileName') + '_saliency_map.mat'"));

		List<String> saveList = new ArrayList<String>();
		for (int i = 0; i < subfiles.size(); i++) {
			saveList.add("'final_saliency_map_' + subfiles[" + i + "]" + " : final_saliency_map_" + i);
		}

		script.addScript(PythonScript.callFunction("spio.savemat",
				Arrays.asList("saveLocation", "{" + String.join(", ", saveList) + "}")));
	}

	private void visualiseFinalMotionSaliencyMap() {
		script.addCommnet("plot figure");
		script.addScript(PythonScript.callFunctionWithResult(Arrays.asList("fig", "ax"), "plt.subplots",
				Arrays.asList("nrows = 1", "ncols = len(subfiles)")));
		script.addScript(PythonScript.setValue("saveLocation",
				"parameters.get('saveDirectory') + parameters.get('fileName') + '_final_saliency_map.png'"));

		if (subfiles.size() == 1) {
			script.addScript(PythonScript.print(PythonScript.addString("-Plotting final motion saliency map for ")
					+ " + subfiles[0] + " + PythonScript.addString(" ...")));

			script.addScript(PythonScript.callFunction("fig.set_size_inches",
					Arrays.asList("float(columns) / rows * len(subfiles)", "1", "forward = False")));
			script.addScript(PythonScript.callFunction("plt.subplots_adjust",
					Arrays.asList("left = 0", "right = 1", "bottom = 0", "top = 1", "hspace = 0", "wspace = 0")));
			script.addScript(
					PythonScript.callFunction("ax.imshow", Arrays.asList("final_saliency_map_0", "cmap='coolwarm'")));
			script.addScript(PythonScript.callFunction("ax.grid", PythonScript.addString("off")));
			script.addScript(PythonScript.callFunction("ax.axis", PythonScript.addString("off")));

			script.addScript(PythonScript.callFunction("fig.savefig", Arrays.asList("saveLocation", "dpi = rows")));
			script.addScript(PythonScript.callFunction("plt.close", "fig"));
		} else {
			script.addScript(PythonScript.callFunction("fig.set_size_inches",
					Arrays.asList("float(columns) / rows * len(subfiles) + 0.1", "1", "forward = False")));
			script.addScript(PythonScript.callFunction("plt.subplots_adjust",
					Arrays.asList("left = 0", "right = 1", "bottom = 0", "top = 1", "hspace = 0", "wspace = 0.1")));

			for (int i = 0; i < subfiles.size(); i++) {
				script.addScript(PythonScript.print(PythonScript.addString("-Plotting final motion saliency map for ")
						+ " + subfiles[" + i + "] +" + PythonScript.addString(" ...")));

				script.addScript(PythonScript.callFunction("ax[" + i + "].imshow",
						Arrays.asList("final_saliency_map_" + i, "cmap='coolwarm'")));
				script.addScript(PythonScript.callFunction("ax[" + i + "].grid", PythonScript.addString("off")));
				script.addScript(PythonScript.callFunction("ax[" + i + "].axis", PythonScript.addString("off")));

				script.addScript(PythonScript.callFunction("fig.savefig", Arrays.asList("saveLocation", "dpi = rows")));
				script.addScript(PythonScript.callFunction("plt.close", "fig"));
			}
		}
	}

	private void visualiseSpacialTimeMotionSaliencyMap() {
		spacialTimeMotionSaliencyMapFolder = new File(spacialTimeMotionSaliencyMapPath);
		spacialTimeMotionSaliencyMapFolder.mkdirs();

		script.addCommnet("spacial time motion saliency map");
		script.addScript(PythonScript.print(PythonScript.addString("-Plotting spacial time motion saliency map ...")));
		script.addScript(
				PythonScript.print(PythonScript.addString(">") + " + str(spatial_time_saliency_map_0.shape[0])"));
		script.startFor("frame", "spatial_time_saliency_map_0.shape[0]");
		script.addScript(PythonScript.print(PythonScript.addString("!") + " + str(frame + 1)"));

		script.addCommnet("plot figure");
		script.addScript(PythonScript.callFunctionWithResult(Arrays.asList("fig", "ax"), "plt.subplots",
				Arrays.asList("nrows = 1", "ncols = len(subfiles)")));
		script.addScript(PythonScript.setValue("saveLocation",
				"parameters.get('saveDirectory') + parameters.get('fileName') + '_spacial_time_motion_saliency_map_image_sequence'"));

		if (subfiles.size() == 1) {

			script.addScript(PythonScript.callFunction("fig.set_size_inches",
					Arrays.asList("float(columns) / rows * len(subfiles)", "1", "forward = False")));
			script.addScript(PythonScript.callFunction("plt.subplots_adjust",
					Arrays.asList("left = 0", "right = 1", "bottom = 0", "top = 1", "hspace = 0", "wspace = 0")));
			script.addScript(PythonScript.callFunction("ax.imshow",
					Arrays.asList("spatial_time_saliency_map_0[frame, :, :]", "cmap='coolwarm'")));
			script.addScript(PythonScript.callFunction("ax.grid", PythonScript.addString("off")));
			script.addScript(PythonScript.callFunction("ax.axis", PythonScript.addString("off")));

			script.addScript(PythonScript.callFunction("fig.savefig",
					Arrays.asList(PythonScript.callFunction("os.path.join", Arrays.asList("saveLocation",
							"'spatial_time_saliency_map_%s' %(str(frame + 1).zfill(3)) + '_' + parameters.get('fileName')  + '.png' ")),
							"dpi = rows")));
			script.addScript(PythonScript.callFunction("plt.close", "fig"));
		} else {
			script.addScript(PythonScript.callFunction("fig.set_size_inches",
					Arrays.asList("float(columns) / rows * len(subfiles) + 0.1", "1", "forward = False")));
			script.addScript(PythonScript.callFunction("plt.subplots_adjust",
					Arrays.asList("left = 0", "right = 1", "bottom = 0", "top = 1", "hspace = 0", "wspace = 0.1")));

			for (int i = 0; i < subfiles.size(); i++) {

				script.addScript(PythonScript.callFunction("ax[" + i + "].imshow",
						Arrays.asList("spatial_time_saliency_map_" + i + "[frame, :, :]", "cmap='coolwarm'")));
				script.addScript(PythonScript.callFunction("ax[" + i + "].grid", PythonScript.addString("off")));
				script.addScript(PythonScript.callFunction("ax[" + i + "].axis", PythonScript.addString("off")));

				script.addScript(PythonScript.callFunction("fig.savefig",
						Arrays.asList(PythonScript.callFunction("os.path.join", Arrays.asList("saveLocation",
								"'spatial_time_saliency_map_%s' %(str(frame + 1).zfill(3)) + '_' + parameters.get('fileName')  + '.png' ")),
								"dpi = rows")));
				script.addScript(PythonScript.callFunction("plt.close", "fig"));
			}
		}

		script.stopFor();
	}

	private void generateBoundaryFormationIndex() {
		script.addScript(PythonScript.print(PythonScript.addString("-Computing boundary formation index ...")));

		script.addScript(PythonScript.setValue(Arrays.asList("boundary_formation_index", "av_saliency_map"),
				PythonScript.callFunction("compute_boundary_formation_index", Arrays.asList("final_saliency_map_0",
						"final_saliency_map_1", "spixel_size", "pad_multiple = parameters.get('padding')"))));
		script.addScript(PythonScript.print(PythonScript.addString(".%.3f") + "%(boundary_formation_index)"));
		script.newLine();

	}

	private void getAverageMotionSaliencyMap() {
		if (SaliencyMapParameters.getSaveOption("average_motion_saliency_map_save_options", ".mat")) {

			script.addCommnet("save average motion saliency map");
			script.addScript(PythonScript.print(PythonScript.addString("-Saving average saliency map...")));
			script.addScript(PythonScript.setValue("saveLocation",
					"parameters.get('saveDirectory') + parameters.get('fileName') + '_average_saliency_map.mat'"));
			script.addScript(PythonScript.callFunction("spio.savemat",
					Arrays.asList("saveLocation", "{'average_saliency_map' : av_saliency_map}")));
		}

		if (SaliencyMapParameters.getSaveOption("average_motion_saliency_map_save_options", ".png")) {
			script.addCommnet("plot figure");
			script.addScript(PythonScript.print(PythonScript.addString("-Plotting average motion saliency map ...")));
			script.addScript(PythonScript.setValue("fig", PythonScript.callFunction("plt.figure", "")));
			script.addScript(PythonScript.callFunction("fig.set_size_inches", Arrays.asList(
					"float(av_saliency_map.shape[1]) / int(av_saliency_map.shape[0])", "1", "forward = False")));
			script.addScript(PythonScript.setValue("ax",
					PythonScript.callFunction("plt.Axes", Arrays.asList("fig", "[0., 0., 1., 1.]"))));
			script.addScript(PythonScript.callFunction("ax.set_axis_off", ""));
			script.addScript(PythonScript.callFunction("fig.add_axes", "ax"));
			script.addScript(PythonScript.callFunction("ax.set_xlim", "[0, av_saliency_map.shape[1]]"));
			script.addScript(PythonScript.callFunction("ax.set_ylim", "[av_saliency_map.shape[0], 0]"));
			script.addScript(PythonScript.callFunction("ax.grid", PythonScript.addString("off")));
			script.addScript(PythonScript.callFunction("ax.axis", PythonScript.addString("off")));
			script.addScript(
					PythonScript.callFunction("ax.imshow", Arrays.asList("av_saliency_map", "cmap='coolwarm'")));
			script.newLine();

			script.addCommnet("save figure");
			script.addScript(PythonScript.setValue("saveLocation",
					"parameters.get('saveDirectory') + parameters.get('fileName') + '_average_saliency_map.png'"));
			script.addScript(PythonScript.callFunction("fig.savefig",
					Arrays.asList("saveLocation", "dpi = av_saliency_map.shape[0]")));
			script.addScript(PythonScript.callFunction("plt.close", "fig"));
		}
	}

	@Override
	protected String doInBackground() throws Exception {
		publish("-Processing parameters...");

		if (SaliencyMapParameters.isOutput("config_file"))
			createConfigFile();

		if (SaliencyMapParameters.isOutput("boundary_formation_index")) {
			boundaryIndexCSVFile = new File(
					SaliencyMapParameters.getSaveDirectory() + "MOSES_boundary_formation_index.csv");

			try {
				boundaryIndexCSVFile.createNewFile();
			} catch (IOException e) {
				IJ.handleException(e);
			}

			try {
				FileWriter csvWriter = new FileWriter(boundaryIndexCSVFile, true);

				csvWriter.append(String.join(",", Arrays.asList("File", "Boundary formation index")));
				csvWriter.append("\n");
				csvWriter.flush();
				csvWriter.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		for (String filePath : SaliencyMapParameters.getMotionTracksFilePaths()) {
			currentFilePath = filePath;
			progress.setFileName(Globals.getName(filePath));
			fileNumber++;
			subfiles = SaliencyMapParameters.getMotionTracksSubfiles(filePath);
			folderPaths();

			if (SaliencyMapParameters.isOutput("motion_saliency_map") && !this.isCancelled()) {
				script = new PythonScript("Motion saliency map");
				generateMotionSaliencyMap();

				if (SaliencyMapParameters.isOutput("final_saliency_map_visualisation") && !this.isCancelled())
					visualiseFinalMotionSaliencyMap();

				if (SaliencyMapParameters.isOutput("spatial_time_saliency_map_visualisation") && !this.isCancelled())
					visualiseSpacialTimeMotionSaliencyMap();

				if (SaliencyMapParameters.isOutput("boundary_formation_index") && !this.isCancelled()) {
					if (subfiles.size() == 2) {
						generateBoundaryFormationIndex();

						if (SaliencyMapParameters.isOutput("average_motion_saliency_map") && !this.isCancelled())
							getAverageMotionSaliencyMap();
					} else
						publish("+Warning: Boundary formation index was not computed for " + Globals.getName(filePath)
								+ ". Please check if the file contains the right number of motion tracks.");
				}

				runScript();

				if (SaliencyMapParameters.isOutput("spatial_time_saliency_map_visualisation") && !this.isCancelled())
					executeSaveOption("spatial_time_saliency_map_visualisation_save_options",
							spacialTimeMotionSaliencyMapFolder, "spatial_time_saliency_map");
			}
		}

		return "Done.";

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

	public void destroy() {
		process.destroyForcibly();
		this.cancel(true);
	}

	private void addScriptHeader() {
		script.importModule("sys");
		script.importModule("os");
		script.importModule("pylab", "plt");
		script.importModule("scipy.io", "spio");
		script.importModuleFrom("compute_motion_saliency_map", "MOSES.Motion_Analysis.mesh_statistics_tools");
		script.importModuleFrom("compute_boundary_formation_index", "MOSES.Motion_Analysis.mesh_statistics_tools");
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
				writeCSV(boundaryIndexCSVFile, Arrays.asList(Globals.getName(currentFilePath), m));
			}

			if (validMessage) {
				progress.setMessage("<html>" + m + "</html>");
				progress.setFileCount(SaliencyMapParameters.getFileCount());
				progress.setFileNumber(fileNumber);
			}
		}
	}

	private void executeSaveOption(String saveOptionName, File folder, String outputName) {
		// save .tif
		if (SaliencyMapParameters.getSaveOption(saveOptionName, ".tif")) {
			publish("-Generating tiff stack...");
			Thread.yield();

			ImagePlus imp = FolderOpener.open(folder.getAbsolutePath(), "");
			imp.show();
			IJ.saveAs(imp, "Tiff", SaliencyMapParameters.getSaveDirectory()
					+ Globals.getNameWithoutExtension(currentFilePath) + "_" + outputName + ".tif");
		}

		// save .avi
		if (SaliencyMapParameters.getSaveOption(saveOptionName, ".avi")) {
			publish("-Generating avi video...");
			Thread.yield();

			ImagePlus imp = FolderOpener.open(folder.getAbsolutePath(), "");
			IJ.run(imp, "AVI... ", "compression=JPEG frame=7 save=" + SaliencyMapParameters.getSaveDirectory()
					+ Globals.getNameWithoutExtension(currentFilePath) + "_" + outputName + ".avi");
		}

		// delete image sequence folder
		if (!SaliencyMapParameters.getSaveOption(saveOptionName, ".png")) {
			publish("-Deleting temporary files...");
			Thread.yield();

			String[] entries = folder.list();
			for (String fileName : entries) {
				File currentFile = new File(folder.getPath(), fileName);
				currentFile.delete();
			}

			folder.delete();
		}
	}

	@Override
	protected void done() {
		progress.setVisibility(false);
	}

}
