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

import org.joml.Math;

import ij.IJ;
import ij.ImagePlus;
import ij.plugin.FolderOpener;

public class SaliencyMap extends SwingWorker<String, String> {
	private ProgressPanel progress;
	private int fileNumber;
	private Process process;
	private PythonScript script;
	private int scriptCount;
	private File imageSaveFolder, CSVSaveFolder, matlabSaveFolder, configFile, spacialTimeMotionSaliencyMapFolder,
			boundaryIndexCSVFile;
	private String trackPath, resizedImagePath, projectName, timestamp;
	private MatlabMetadata trackMetadata;

	public SaliencyMap(ProgressPanel p) {
		progress = p;
		fileNumber = scriptCount = 0;

		imageSaveFolder = CSVSaveFolder = matlabSaveFolder = spacialTimeMotionSaliencyMapFolder = null;
	}

	private void recordHistory() {
		File commandsHistoryFile = new File(SaliencyMapParameters.getWorkspace() + "/workspace_history.csv");

		Globals.writeCSV(commandsHistoryFile, Arrays.asList(""));
		Globals.writeCSV(commandsHistoryFile, Arrays.asList("Action:", "motion saliency map"));
		Globals.writeCSV(commandsHistoryFile, Arrays.asList("Timestamp:", timestamp));

		Globals.writeCSV(commandsHistoryFile, Arrays.asList("Motion tracks:"));
		Globals.writeCSV(commandsHistoryFile, Arrays.asList(String.join(", ", SaliencyMapParameters.getTracksPaths())));

		Globals.writeCSV(commandsHistoryFile, Arrays.asList("Images:"));
		Globals.writeCSV(commandsHistoryFile, Arrays.asList(String.join(", ", SaliencyMapParameters.getImagePaths())));

		Globals.writeCSV(commandsHistoryFile, Arrays.asList("Outputs:"));
		Globals.writeCSV(commandsHistoryFile, Arrays.asList(String.join(", ", SaliencyMapParameters.getOutputList())));

		Globals.writeCSV(commandsHistoryFile, Arrays.asList("Parameters:"));
		Globals.writeCSV(commandsHistoryFile,
				Arrays.asList(String.join(", ", SaliencyMapParameters.getParametersList())));
	}

	private void folderPaths() {
		if (SaliencyMapParameters.isOutput("spatial_time_saliency_map_visualisation")
				|| SaliencyMapParameters.isOutput("final_saliency_map_visualisation")
				|| (SaliencyMapParameters.isOutput("average_motion_saliency_map")
						&& SaliencyMapParameters.getSaveOption("average_motion_saliency_map_save_options", ".png"))) {
			imageSaveFolder = new File(
					SaliencyMapParameters.getWorkspace() + "/" + projectName + "/data_analysis/images/" + timestamp);
			imageSaveFolder.mkdirs();
		}

		if (SaliencyMapParameters.isOutput("motion_saliency_map")
				|| (SaliencyMapParameters.isOutput("average_motion_saliency_map")
						&& SaliencyMapParameters.getSaveOption("average_motion_saliency_map_save_options", ".mat"))) {
			matlabSaveFolder = new File(SaliencyMapParameters.getWorkspace() + "/" + projectName
					+ "/data_analysis/matlab_files/" + timestamp);
			matlabSaveFolder.mkdirs();
		}

		if (SaliencyMapParameters.isOutput("spatial_time_saliency_map_visualisation")) {
			String spacialTimeMotionSaliencyMapPath = imageSaveFolder.getAbsolutePath() + "/" + projectName
					+ "_spacial_time_motion_saliency_map_image_sequence";
			spacialTimeMotionSaliencyMapFolder = new File(spacialTimeMotionSaliencyMapPath);
			spacialTimeMotionSaliencyMapFolder.mkdirs();
		}

		if (SaliencyMapParameters.isOutput("boundary_formation_index")) {
			CSVSaveFolder = new File(
					SaliencyMapParameters.getWorkspace() + "/" + projectName + "/data_analysis/CSV_files/" + timestamp);
			CSVSaveFolder.mkdirs();

			boundaryIndexCSVFile = new File(CSVSaveFolder.getAbsolutePath() + "/MOSES_motion_enrichment_index.csv");

			try {
				boundaryIndexCSVFile.createNewFile();
			} catch (IOException e) {
				IJ.handleException(e);
			}

			Globals.writeCSV(boundaryIndexCSVFile, Arrays.asList("Project name:", projectName));
			Globals.writeCSV(boundaryIndexCSVFile,
					Arrays.asList("Tracks parameters:", String.join(", ", trackMetadata.tracksParametersList())));
		}
	}

	private void generateMotionSaliencyMap() {
		List<Parameter> parameters = new ArrayList<Parameter>();
		parameters.addAll(Arrays.asList(new Parameter("trackPath", "str", trackPath),
				new Parameter("imagePath", "str", resizedImagePath),
				new Parameter("distanceThreshold", "float", SaliencyMapParameters.getSaliencyMapDistanceThreshold()),
				new Parameter("padding", "int", SaliencyMapParameters.getPaddingDistance()),
				new Parameter("fileName", "str", projectName)));

		if (matlabSaveFolder != null)
			parameters.add(new Parameter("saveDirectory", "str", matlabSaveFolder));

		if (imageSaveFolder != null)
			parameters.add(new Parameter("saveDirectory2", "str", imageSaveFolder));

		if (spacialTimeMotionSaliencyMapFolder != null)
			parameters.add(new Parameter("saveDirectory3", "str", spacialTimeMotionSaliencyMapFolder));

		addScriptHeader();

		script.addScript(PythonScript.print(PythonScript.addString("-Computing motion saliency map...")));
		script.newLine();

		script.addCommnet("define parameters dictionary");
		script.addParameter(parameters);
		script.addScript(script.createParameterDictionary());
		script.newLine();

		script.addCommnet("import file");
		script.addScript(PythonScript.setValue("file",
				PythonScript.callFunction("spio.loadmat", "parameters.get('trackPath')")));
		script.addScript(PythonScript.setValue("fileInfo",
				PythonScript.callFunction("spio.whosmat", "parameters.get('trackPath')")));

		script.addScript(PythonScript.setValue("rows", "int(file['metadata'][0][0][0][1][0][0])"));
		script.addScript(PythonScript.setValue("columns", "int(file['metadata'][0][0][0][1][0][1])"));
		script.newLine();

		script.addCommnet("compute saliency map");
		List<Integer> channels = trackMetadata.getChannels();
		for (int i = 0; i < channels.size(); i++) {
			int channelIndex = channels.get(i) + 1;

			script.addScript(PythonScript.setValue("track", "file[fileInfo[" + i + "][0]]"));
			script.addScript(PythonScript.setValue("spixel_size", "track[1,0,1] - track[1,0,0]"));
			script.addScript(PythonScript.setValue(
					Arrays.asList("final_saliency_map_" + channelIndex, "spatial_time_saliency_map_" + channelIndex),
					PythonScript.callFunction("compute_motion_saliency_map",
							Arrays.asList("track", "dist_thresh = parameters.get('distanceThreshold') * spixel_size",
									"shape = (rows, columns)", "filt = 1", "filt_size = spixel_size"))));

			if (SaliencyMapParameters.getGaussianSmoothing())
				script.addScript(PythonScript.setValue("final_saliency_map_" + channelIndex, PythonScript.callFunction(
						"gaussian", Arrays.asList("final_saliency_map_" + channelIndex, "spixel_size / 2"))));

			script.newLine();
		}
	}

	private void saveMotionSaliencyMap() {
		script.addCommnet("save saliency map");
		script.addScript(PythonScript.setValue("saveLocation", "parameters.get('saveDirectory')"));

		List<Integer> channels = trackMetadata.getChannels();
		List<String> saveList = new ArrayList<String>();
		for (int i = 0; i < channels.size(); i++) {
			int channelIndex = channels.get(i) + 1;

			saveList.add("'final_saliency_map_" + channelIndex + "' : final_saliency_map_" + channelIndex);
		}

		saveList.add(
				"'metadata' : np.array([[file['metadata'][0][0][0][0][0], [file['metadata'][0][0][0][1][0][0], file['metadata'][0][0][0][1][0][1]], file['metadata'][0][0][0][2][0], file['metadata'][0][0][0][3][0] ,'saliency_map'], file['metadata'][0][1]])");

		script.addScript(
				PythonScript.callFunction("spio.savemat",
						Arrays.asList(
								PythonScript.callFunction("os.path.join",
										Arrays.asList("saveLocation",
												"parameters.get('fileName') + '_saliency_map.mat'")),
								"{" + String.join(", ", saveList) + "}")));
	}

	private void visualiseFinalMotionSaliencyMap() {
		script.addScript(PythonScript.print(PythonScript.addString("-Plotting final motion saliency map...")));

		List<Integer> channels = trackMetadata.getChannels();

		script.addCommnet("plot figure");
		script.addScript(PythonScript.callFunctionWithResult(Arrays.asList("fig", "ax"), "plt.subplots",
				Arrays.asList("nrows = 1", "ncols = " + channels.size())));
		script.addScript(PythonScript.setValue("saveLocation", "parameters.get('saveDirectory2')"));

		if (SaliencyMapParameters.isOutput("final_saliency_map_visualisation_overlay")) {
			script.addScript(PythonScript.setValue("vidstack",
					PythonScript.callFunction("read_multiimg_PIL", "parameters.get('imagePath')")));

			if (trackMetadata.getTrackType().equals("forward"))
				script.addScript(PythonScript.setValue("frame_img", "vidstack[0]"));
			else
				script.addScript(PythonScript.setValue("frame_img", "vidstack[0]")); // or -1
		}

		if (channels.size() == 1) {
			int channelIndex = channels.get(0) + 1;

			script.addScript(PythonScript.callFunction("fig.set_size_inches",
					Arrays.asList("float(columns) / rows * " + channels.size(), "1", "forward = False")));
			script.addScript(PythonScript.callFunction("plt.subplots_adjust",
					Arrays.asList("left = 0", "right = 1", "bottom = 0", "top = 1", "hspace = 0", "wspace = 0")));

			if (SaliencyMapParameters.isOutput("final_saliency_map_visualisation_overlay")) {
				script.addScript(PythonScript.callFunction("ax.imshow", Arrays.asList("frame_img")));

				script.addScript(PythonScript.callFunction("ax.imshow",
						Arrays.asList("final_saliency_map_" + channelIndex, "cmap='coolwarm'", "alpha = 0.7")));
			} else
				script.addScript(PythonScript.callFunction("ax.imshow",
						Arrays.asList("final_saliency_map_" + channelIndex, "cmap='coolwarm'")));

			script.addScript(PythonScript.callFunction("ax.grid", PythonScript.addString("off")));
			script.addScript(PythonScript.callFunction("ax.axis", PythonScript.addString("off")));

		} else {
			script.addScript(PythonScript.callFunction("fig.set_size_inches",
					Arrays.asList("float(columns) / rows * " + channels.size() + " + 0.1", "1", "forward = False")));
			script.addScript(PythonScript.callFunction("plt.subplots_adjust",
					Arrays.asList("left = 0", "right = 1", "bottom = 0", "top = 1", "hspace = 0", "wspace = 0.1")));

			for (int i = 0; i < channels.size(); i++) {
				int channelIndex = channels.get(i) + 1;

				if (SaliencyMapParameters.isOutput("final_saliency_map_visualisation_overlay")) {
					script.addScript(PythonScript.callFunction("ax[" + i + "].imshow", Arrays.asList("frame_img")));

					script.addScript(PythonScript.callFunction("ax[" + i + "].imshow",
							Arrays.asList("final_saliency_map_" + channelIndex, "cmap='coolwarm'", "alpha = 0.7")));
				} else
					script.addScript(PythonScript.callFunction("ax[" + i + "].imshow",
							Arrays.asList("final_saliency_map_" + channelIndex, "cmap='coolwarm'")));

				script.addScript(PythonScript.callFunction("ax[" + i + "].grid", PythonScript.addString("off")));
				script.addScript(PythonScript.callFunction("ax[" + i + "].axis", PythonScript.addString("off")));
			}
		}

		script.addScript(
				PythonScript.callFunction("fig.savefig",
						Arrays.asList(
								PythonScript.callFunction("os.path.join",
										Arrays.asList("saveLocation",
												"parameters.get('fileName') + '_final_saliency_map.png'")),
								"dpi = rows")));
		script.addScript(PythonScript.callFunction("plt.close", "fig"));
	}

	private void visualiseSpacialTimeMotionSaliencyMap() {
		List<Integer> channels = trackMetadata.getChannels();
		script.addCommnet("spacial time motion saliency map");
		script.addScript(PythonScript.setValue("saveLocation", "parameters.get('saveDirectory3')"));

		if (SaliencyMapParameters.isOutput("spatial_time_saliency_map_visualisation_overlay")) {
			script.addScript(PythonScript.setValue("vidstack",
					PythonScript.callFunction("read_multiimg_PIL", "parameters.get('imagePath')")));
		}

		script.addScript(PythonScript.print(PythonScript.addString("-Plotting spacial time motion saliency map...")));

		int channelIndex0 = channels.get(0) + 1;

		script.addScript(PythonScript.print(
				PythonScript.addString(">") + " + str(spatial_time_saliency_map_" + channelIndex0 + ".shape[0])"));
		script.startFor("frame", "spatial_time_saliency_map_" + channelIndex0 + ".shape[0]");
		script.addScript(PythonScript.print(PythonScript.addString("!") + " + str(frame + 1)"));

		script.addCommnet("plot figure");
		script.addScript(PythonScript.callFunctionWithResult(Arrays.asList("fig", "ax"), "plt.subplots",
				Arrays.asList("nrows = 1", "ncols = " + channels.size())));

		if (SaliencyMapParameters.isOutput("spatial_time_saliency_map_visualisation_overlay")) {
			if (trackMetadata.getTrackType().equals("forward"))
				script.addScript(PythonScript.setValue("frame_img", "vidstack[frame]"));
			else
				script.addScript(PythonScript.setValue("frame_img", "vidstack[-(frame + 1)]"));
		}
		if (channels.size() == 1) {

			script.addScript(PythonScript.callFunction("fig.set_size_inches",
					Arrays.asList("float(columns) / rows * " + channels.size(), "1", "forward = False")));
			script.addScript(PythonScript.callFunction("plt.subplots_adjust",
					Arrays.asList("left = 0", "right = 1", "bottom = 0", "top = 1", "hspace = 0", "wspace = 0")));

			if (SaliencyMapParameters.getGaussianSmoothing())
				script.addScript(
						PythonScript.setValue("spatial_time_saliency_map_" + channelIndex0 + "[frame, :, :]",
								PythonScript.callFunction("gaussian",
										Arrays.asList("spatial_time_saliency_map_" + channelIndex0 + "[frame, :, :]",
												"spixel_size / 2"))));

			if (SaliencyMapParameters.isOutput("spatial_time_saliency_map_visualisation_overlay")) {
				script.addScript(PythonScript.callFunction("ax.imshow", Arrays.asList("frame_img")));

				script.addScript(PythonScript.callFunction("ax.imshow",
						Arrays.asList("spatial_time_saliency_map_" + channelIndex0 + "[frame, :, :]", "cmap='coolwarm'",
								"alpha = 0.7")));
			} else
				script.addScript(PythonScript.callFunction("ax.imshow", Arrays
						.asList("spatial_time_saliency_map_" + channelIndex0 + "[frame, :, :]", "cmap='coolwarm'")));

			script.addScript(PythonScript.callFunction("ax.grid", PythonScript.addString("off")));
			script.addScript(PythonScript.callFunction("ax.axis", PythonScript.addString("off")));
		} else {
			script.addScript(PythonScript.callFunction("fig.set_size_inches",
					Arrays.asList("float(columns) / rows * " + channels.size() + " + 0.1", "1", "forward = False")));
			script.addScript(PythonScript.callFunction("plt.subplots_adjust",
					Arrays.asList("left = 0", "right = 1", "bottom = 0", "top = 1", "hspace = 0", "wspace = 0.1")));

			for (int i = 0; i < channels.size(); i++) {
				int channelIndex = channels.get(i) + 1;

				if (SaliencyMapParameters.getGaussianSmoothing())
					script.addScript(
							PythonScript.setValue("spatial_time_saliency_map_" + channelIndex + "[frame, :, :]",
									PythonScript.callFunction("gaussian",
											Arrays.asList("spatial_time_saliency_map_" + channelIndex + "[frame, :, :]",
													"spixel_size / 2"))));

				if (SaliencyMapParameters.isOutput("spatial_time_saliency_map_visualisation_overlay")) {
					script.addScript(PythonScript.callFunction("ax[" + i + "].imshow", Arrays.asList("frame_img")));

					script.addScript(PythonScript.callFunction("ax[" + i + "].imshow",
							Arrays.asList("spatial_time_saliency_map_" + channelIndex + "[frame, :, :]",
									"cmap='coolwarm'", "alpha = 0.7")));
				} else
					script.addScript(PythonScript.callFunction("ax[" + i + "].imshow", Arrays
							.asList("spatial_time_saliency_map_" + channelIndex + "[frame, :, :]", "cmap='coolwarm'")));

				script.addScript(PythonScript.callFunction("ax[" + i + "].grid", PythonScript.addString("off")));
				script.addScript(PythonScript.callFunction("ax[" + i + "].axis", PythonScript.addString("off")));
			}
		}
		script.addScript(PythonScript.callFunction("fig.savefig",
				Arrays.asList(PythonScript.callFunction("os.path.join", Arrays.asList("saveLocation",
						"'spatial_time_saliency_map_%s' %(str(frame + 1).zfill(3)) + '_' + parameters.get('fileName')  + '.png' ")),
						"dpi = rows")));
		script.addScript(PythonScript.callFunction("plt.close", "fig"));

		script.stopFor();
	}

	private void generateBoundaryFormationIndex() {
		script.addScript(PythonScript.print(PythonScript.addString("-Computing motion enrichment index...")));

		List<Integer> channels = trackMetadata.getChannels();
		List<String> saliencyMaps = new ArrayList<String>();
		for (int i = 0; i < channels.size(); i++) {
			int channelIndex = channels.get(i) + 1;
			saliencyMaps.add("final_saliency_map_" + channelIndex);
		}

		script.addScript(PythonScript.setValue("av_saliency_map",
				"(" + String.join("+", saliencyMaps) + ") / " + channels.size()));

		script.addScript(PythonScript.setValue(Arrays.asList("boundary_formation_index", "avrg_saliency_map"),
				PythonScript.callFunction("compute_boundary_formation_index", Arrays.asList("av_saliency_map",
						"av_saliency_map", "spixel_size", "pad_multiple = parameters.get('padding')"))));
		script.addScript(PythonScript.print(PythonScript.addString(".%.3f") + "%(boundary_formation_index)"));
		script.newLine();

	}

	private void getAverageMotionSaliencyMap() {
		script.addScript(PythonScript.print(PythonScript.addString("-Computing average motion saliency map...")));

		List<Integer> channels = trackMetadata.getChannels();
		List<String> saliencyMaps = new ArrayList<String>();
		for (int i = 0; i < channels.size(); i++) {
			int channelIndex = channels.get(i) + 1;
			saliencyMaps.add("final_saliency_map_" + channelIndex);
		}

		script.addScript(PythonScript.setValue("av_saliency_map",
				"(" + String.join("+", saliencyMaps) + ") / " + channels.size()));

		if (SaliencyMapParameters.getSaveOption("average_motion_saliency_map_save_options", ".mat")) {
			script.addScript(PythonScript.setValue("saveLocation", "parameters.get('saveDirectory')"));

			List<String> saveList = new ArrayList<String>();
			saveList.add("'average_saliency_map' : av_saliency_map");
			saveList.add(
					"'metadata' : np.array([[file['metadata'][0][0][0][0][0], [file['metadata'][0][0][0][1][0][0], file['metadata'][0][0][0][1][0][1]], file['metadata'][0][0][0][2][0], file['metadata'][0][0][0][3][0] ,'saliency_map'], file['metadata'][0][1]])");

			script.addCommnet("save average motion saliency map");
			script.addScript(PythonScript.callFunction("spio.savemat",
					Arrays.asList(
							PythonScript.callFunction("os.path.join",
									Arrays.asList("saveLocation",
											"parameters.get('fileName') + '_average_saliency_map.mat'")),
							"{" + String.join(",", saveList) + "}")));
		}

		if (SaliencyMapParameters.getSaveOption("average_motion_saliency_map_save_options", ".png")) {
			script.addScript(PythonScript.setValue("saveLocation", "parameters.get('saveDirectory2')"));

			script.addCommnet("plot figure");
			script.addScript(PythonScript.print(PythonScript.addString("-Plotting average motion saliency map...")));
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
			script.addScript(PythonScript.callFunction("fig.savefig",
					Arrays.asList(
							PythonScript.callFunction("os.path.join",
									Arrays.asList("saveLocation",
											"parameters.get('fileName') + '_average_saliency_map.png'")),
							"dpi = av_saliency_map.shape[0]")));
			script.addScript(PythonScript.callFunction("plt.close", "fig"));
		}
	}

	@Override
	protected String doInBackground() throws Exception {
		publish("-Processing parameters...");

		timestamp = Globals.getFormattedDate();
		SaliencyMapParameters.trimFiles();
		recordHistory();

		fileNumber = 1;
		for (ProjectImageAnnotationTracks imt : SaliencyMapParameters.getFiles()) {
			String imagePath = imt.getImagePath();
			trackPath = imt.getTrackPath();
			projectName = imt.getProjectName();

			if (imagePath != null && trackPath != null) {
				trackMetadata = new MatlabMetadata(trackPath);

				progress.setFileName(projectName);
				publish("-Processing parameters...");

				// create resized file
				resizedImagePath = System.getProperty("java.io.tmpdir") + "MOSESimage.tif";

				ImagePlus resizedImage = IJ.openImage(imagePath);
				IJ.run(resizedImage, "Size...", "width="
						+ (int) (resizedImage.getWidth() / Math.sqrt(trackMetadata.getDownsizeFactor())) + " height="
						+ (int) (resizedImage.getHeight() / Math.sqrt(trackMetadata.getDownsizeFactor())) + " depth="
						+ ComputeTracksParameters.getFrames() + " constrain average interpolation=Bilinear");
				IJ.saveAs(resizedImage, "Tiff", resizedImagePath);
				resizedImage.close();

				folderPaths();

				script = new PythonScript("Motion saliency map");
				generateMotionSaliencyMap();

				if (SaliencyMapParameters.isOutput("motion_saliency_map") && !this.isCancelled())
					saveMotionSaliencyMap();

				if (SaliencyMapParameters.isOutput("final_saliency_map_visualisation") && !this.isCancelled())
					visualiseFinalMotionSaliencyMap();

				if (SaliencyMapParameters.isOutput("spatial_time_saliency_map_visualisation") && !this.isCancelled())
					visualiseSpacialTimeMotionSaliencyMap();

				if (SaliencyMapParameters.isOutput("average_motion_saliency_map") && !this.isCancelled())
					getAverageMotionSaliencyMap();

				if (SaliencyMapParameters.isOutput("boundary_formation_index") && !this.isCancelled())
					generateBoundaryFormationIndex();

				runScript();

				if (SaliencyMapParameters.isOutput("spatial_time_saliency_map_visualisation") && !this.isCancelled())
					executeSaveOption("spatial_time_saliency_map_visualisation_save_options",
							spacialTimeMotionSaliencyMapFolder, "spatial_time_saliency_map");

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
		script.importModule("numpy", "np");
		script.importModuleFrom("compute_motion_saliency_map", "MOSES.Motion_Analysis.mesh_statistics_tools");
		script.importModuleFrom("compute_boundary_formation_index", "MOSES.Motion_Analysis.mesh_statistics_tools");
		script.importModuleFrom("read_multiimg_PIL", "MOSES.Utility_Functions.file_io");
		script.importModuleFrom("gaussian", "skimage.filters");
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
				Globals.writeCSV(boundaryIndexCSVFile, Arrays.asList("Motion enrichment index", m));
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
			IJ.saveAs(imp, "Tiff", imageSaveFolder.getAbsolutePath() + "/" + projectName + "_" + outputName + ".tif");
		}

		// save .avi
		if (SaliencyMapParameters.getSaveOption(saveOptionName, ".avi")) {
			publish("-Generating avi video...");
			Thread.yield();

			ImagePlus imp = FolderOpener.open(folder.getAbsolutePath(), "");
			IJ.run(imp, "AVI... ", "compression=JPEG frame=7 save=" + imageSaveFolder.getAbsolutePath() + "/"
					+ projectName + "_" + outputName + ".avi");
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
