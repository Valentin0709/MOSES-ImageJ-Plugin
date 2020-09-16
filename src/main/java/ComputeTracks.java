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

class ComputeTracks extends SwingWorker<String, String> {
	private PythonScript script;
	private ProgressPanel progress;
	private Process process;
	private int fileCount, fileNumber, scriptCount;
	private String currentFilePath, temporaryFilePath;
	private File forwardTracksImageSequenceFolder, backwardTracksImageSequenceFolder, motionFieldImageSequenceFolder,
			MOSESMeshImageSequenceFolder, radialMeshImageSequenceFolder, neighborMeshImageSequenceFolder, configFile,
			mainFolder, fileFolder, matlabFolder, imageFolder;

	public ComputeTracks(ProgressPanel p) {
		progress = p;
		scriptCount = fileCount = fileNumber = 0;
	}

	@Override
	protected String doInBackground() {
		publish("-Processing parameters...");

		SimpleDateFormat formatter = new SimpleDateFormat("HH'-'mm'_'dd'-'MM'-'yyyy");
		String mainFolderPath = ComputeTracksParameters.getSaveDirectory() + "MOSES_Workspace_"
				+ formatter.format(new Date(System.currentTimeMillis()));
		mainFolder = new File(mainFolderPath);
		mainFolder.mkdirs();

		if (ComputeTracksParameters.getBatchMode()) {

			ArrayList<String> validExtensions = new ArrayList<String>();
			validExtensions.addAll(Arrays.asList(".tif", ".tiff"));

			File fileDirectory = new File(ComputeTracksParameters.getFileDirectory());
			String[] files = fileDirectory.list();
			fileCount = 0;
			for (String fileTitle : files) {
				File selectedFile = new File(fileDirectory.getPath(), fileTitle);
				if (selectedFile.isFile() && Globals.checkExtension(selectedFile.getAbsolutePath(), validExtensions))
					fileCount++;
			}

			fileNumber = 1;
			for (String fileTitle : files) {
				File selectedFile = new File(fileDirectory.getPath(), fileTitle);

				if (selectedFile.isFile() && Globals.checkExtension(selectedFile.getAbsolutePath(), validExtensions)) {
					currentFilePath = selectedFile.getAbsolutePath();
					progress.setFileName(Globals.getName(currentFilePath));
					analyseFile();
					fileNumber++;
				}
			}
		} else {
			currentFilePath = ComputeTracksParameters.getFilePath();
			progress.setFileName(Globals.getName(currentFilePath));
			fileNumber = fileCount = 1;
			analyseFile();
		}

		return "Done";

	}

	public void destroy() {
		process.destroyForcibly();
		this.cancel(true);
	}

	private void initialiseConfigFile() {
		configFile = new File(mainFolder.getAbsolutePath() + "/MOSES_config_file.txt");

		try {
			configFile.createNewFile();
		} catch (IOException e) {
			IJ.handleException(e);
		}

		String configFileHeader = "MOSES config file \r\n";
		configFileHeader += "@Action: tracks_and_mesh \r\n";

		configFileHeader += "@Outputs: ";
		List<String> outputs = ComputeTracksParameters.getOutputList();
		for (String output : outputs)
			configFileHeader += output + " ";
		configFileHeader += "\r\n";

		configFileHeader += "@Parameters: ";
		List<String> parameters = ComputeTracksParameters.getParametersList();
		for (String parameter : parameters)
			configFileHeader += parameter + " ";
		configFileHeader += "\r\n\r\n";

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

	private void addScriptHeader() {
		script.addCommnet("import modules");
		script.importModule("os");
		script.importModule("sys");
		script.importModule("scipy.io", "spio");
		script.importModule("pylab", "plt");
		script.importModule("numpy", "np");
		script.importModuleFrom("read_multiimg_PIL", "MOSES.Utility_Functions.file_io");
		script.importModuleFrom("plot_tracks", "MOSES.Visualisation_Tools.track_plotting");
		script.importModuleFrom("compute_grayscale_vid_superpixel_tracks",
				"MOSES.Optical_Flow_Tracking.superpixel_track");
		script.importModuleFrom("compute_grayscale_vid_superpixel_tracks_FB",
				"MOSES.Optical_Flow_Tracking.superpixel_track");
		script.importModuleFrom("view_ang_flow", "MOSES.Visualisation_Tools.motion_field_visualisation");
		script.importModuleFrom("construct_MOSES_mesh", "MOSES.Motion_Analysis.mesh_statistics_tools");
		script.importModuleFrom("visualise_mesh", "MOSES.Visualisation_Tools.mesh_visualisation");
		script.importModuleFrom("from_neighbor_list_to_graph", "MOSES.Motion_Analysis.mesh_statistics_tools");
		script.importModuleFrom("construct_radial_neighbour_mesh", "MOSES.Motion_Analysis.mesh_statistics_tools");
		script.importModuleFrom("construct_knn_neighbour_mesh", "MOSES.Motion_Analysis.mesh_statistics_tools");
		script.newLine();
	}

	private void importFile() {
		script.addCommnet("import tiff stack");
		script.addScript(PythonScript.setValue("vidstack",
				PythonScript.callFunction("read_multiimg_PIL", "parameters.get('filePath')")));
		script.startIf("len(vidstack.shape) == 3");
		script.addScript(PythonScript.setValue("vidstack", "vidstack[..., None]"));
		script.addScript(PythonScript.callFunction("plt.set_cmap", PythonScript.addString("gray")));
		script.stopIf();
		script.addScript(
				PythonScript.setValue(Arrays.asList("frames", "rows", "columns", "channels"), "vidstack.shape"));
	}

	private void generateForwardTracks() {
		List<Parameter> parameters = new ArrayList<Parameter>();
		parameters.addAll(Arrays.asList(new Parameter("filePath", "str", temporaryFilePath),
				new Parameter("pyr_scale", "float", ComputeTracksParameters.getPyrScale()),
				new Parameter("levels", "int", ComputeTracksParameters.getLevels()),
				new Parameter("winsize", "int", ComputeTracksParameters.getWinSize()),
				new Parameter("iterations", "int", ComputeTracksParameters.getIterations()),
				new Parameter("poly_n", "int", ComputeTracksParameters.getPolyn()),
				new Parameter("poly_sigma", "float", ComputeTracksParameters.getPolySigma()),
				new Parameter("flags", "int", ComputeTracksParameters.getFlags()),
				new Parameter("n_spixels", "int", ComputeTracksParameters.getNumberSuperpixels()),
				new Parameter("saveDirectory1", "str", matlabFolder.getAbsolutePath()),
				new Parameter("fileName", "str", Globals.getNameWithoutExtension(currentFilePath))));

		if (ComputeTracksParameters.isOutput("forward_tracks_visualisation")) {
			parameters.add(new Parameter("temporal_segment_length", "int",
					ComputeTracksParameters.getForwardTracksTemporalSegment()));

			for (int i = 0; i < ComputeTracksParameters.getNumberSelectedChannels(); i++) {
				int channelIndex = ComputeTracksParameters.getSelectedChannels(i);
				parameters.add(new Parameter("color_" + channelIndex, "str",
						ComputeTracksParameters.getColorOption("forward_tracks_colors", i)));
			}

			parameters.add(new Parameter("saveDirectory2", "str", forwardTracksImageSequenceFolder.getAbsolutePath()));
		}

		addScriptHeader();

		script.addCommnet("define parameters dictionary");
		script.addParameter(parameters);
		script.addScript(script.createParameterDictionary());
		script.newLine();

		importFile();

		script.addCommnet("set optical flow parameters");
		script.addScript(PythonScript.createDictionary("optical_flow_params",
				Arrays.asList(new Pair<>("pyr_scale", "parameters.get('pyr_scale')"),
						new Pair<>("levels", "parameters.get('levels')"),
						new Pair<>("winsize", "parameters.get('winsize')"),
						new Pair<>("iterations", "parameters.get('iterations')"),
						new Pair<>("poly_n", "parameters.get('poly_n')"),
						new Pair<>("poly_sigma", "parameters.get('poly_sigma')"),
						new Pair<>("flags", "parameters.get('flags')"))));
		script.newLine();

		script.addCommnet("generate forward tracks");
		for (int i = 0; i < ComputeTracksParameters.getNumberSelectedChannels(); i++) {
			int channelIndex = ComputeTracksParameters.getSelectedChannels(i);

			script.addScript(PythonScript.print(
					PythonScript.addString("-Computing forward tracks for channel " + (channelIndex + 1) + "...")));

			if (ComputeTracksParameters.getDenseForwardTracks())
				script.addScript(PythonScript.callFunctionWithResult(
						Arrays.asList("optflow_" + (channelIndex + 1), "forward_tracks_" + (channelIndex + 1)),
						"compute_grayscale_vid_superpixel_tracks",
						Arrays.asList("vidstack[:,:,:," + channelIndex + "]", "optical_flow_params",
								"parameters.get('n_spixels')", "dense = True", "mindensity = 1")));
			else
				script.addScript(PythonScript.callFunctionWithResult(
						Arrays.asList("optflow_" + (channelIndex + 1), "forward_tracks_" + (channelIndex + 1)),
						"compute_grayscale_vid_superpixel_tracks", Arrays.asList("vidstack[:,:,:," + channelIndex + "]",
								"optical_flow_params", "parameters.get('n_spixels')")));
		}
		script.newLine();

		script.addCommnet("save forward tracks");
		script.addScript(PythonScript.print(PythonScript.addString("-Saving forward tracks..")));
		script.addScript(PythonScript.setValue("saveLocation",
				"parameters.get('saveDirectory1') + '/' + parameters.get('fileName') + '_forward_tracks.mat'"));

		List<Pair<String, String>> saveList = new ArrayList<Pair<String, String>>();
		for (int i = 0; i < ComputeTracksParameters.getNumberSelectedChannels(); i++) {
			int channelIndex = ComputeTracksParameters.getSelectedChannels(i);

			saveList.add(new Pair<>("forward_tracks_" + (channelIndex + 1), "forward_tracks_" + (channelIndex + 1)));
		}
		// metadata
		saveList.add(new Pair<>("metadata", "np.array([[parameters.get('fileName'), [rows, columns], ["
				+ String.join(",", Globals.convertStringList(ComputeTracksParameters.getSelectedChannels()))
				+ "], 'tracks'], ['forward', '" + ComputeTracksParameters.getDenseForwardTracks()
				+ "', parameters.get('n_spixels'), parameters.get('pyr_scale'), parameters.get('levels'), parameters.get('winsize'), parameters.get('iterations'), parameters.get('poly_n'), parameters.get('poly_sigma'), parameters.get('flags')]])"));

		script.addScript(PythonScript.callFunction("spio.savemat",
				Arrays.asList("saveLocation", PythonScript.makeSaveList(saveList))));
		script.newLine();
	}

	private void visualiseForwardTracks() {
		script.addCommnet("plot forward tracks");
		script.addScript(PythonScript.setValue("vidstack", PythonScript.callFunction("np.squeeze", "vidstack")));
		script.addScript(PythonScript.setValue("saveLocation", "parameters.get('saveDirectory2')"));

		script.addScript(PythonScript.print(PythonScript.addString("-Plotting forward tracks...")));
		script.addScript(PythonScript.setValue("len_segment", "parameters.get('temporal_segment_length')"));
		script.addScript(PythonScript.print(PythonScript.addString(">") + " + str(frames - len_segment)"));
		script.startFor("frame", "len_segment", "frames", "1");
		script.addScript(PythonScript.print(PythonScript.addString("!") + " + str(frame - len_segment + 1)"));
		script.addScript(PythonScript.setValue("frame_img", "vidstack[frame]"));
		script.addScript(PythonScript.setValue("fig", PythonScript.callFunction("plt.figure", "")));
		script.addScript(PythonScript.callFunction("fig.set_size_inches",
				Arrays.asList("float(columns) / rows", "1", "forward = False")));
		script.addScript(PythonScript.setValue("ax",
				PythonScript.callFunction("plt.Axes", Arrays.asList("fig", "[0.,0.,1.,1.]"))));
		script.addScript(PythonScript.callFunction("ax.set_axis_off", ""));
		script.addScript(PythonScript.callFunction("fig.add_axes", "ax"));
		script.addScript(PythonScript.callFunction("ax.set_xlim", "[0, columns]"));
		script.addScript(PythonScript.callFunction("ax.set_ylim", "[rows, 0]"));
		script.addScript(PythonScript.callFunction("ax.grid", PythonScript.addString("off")));
		script.addScript(PythonScript.callFunction("ax.axis", PythonScript.addString("off")));
		script.addScript(PythonScript.callFunction("ax.imshow", Arrays.asList("frame_img", "alpha = 0.6")));
		script.newLine();

		for (int i = 0; i < ComputeTracksParameters.getNumberSelectedChannels(); i++) {
			int channelIndex = ComputeTracksParameters.getSelectedChannels(i);

			script.addScript(PythonScript.callFunction("plot_tracks",
					Arrays.asList("forward_tracks_" + (channelIndex + 1) + "[:, frame-len_segment:frame+1]", "ax",
							"color = parameters.get('color_" + channelIndex + "')", "lw = 1")));
		}
		script.newLine();

		script.addScript(PythonScript.callFunction("fig.savefig",
				Arrays.asList(PythonScript.callFunction("os.path.join", Arrays.asList("saveLocation",
						"parameters.get('fileName') + '_forward_track_plot_%s' %(str(frame - len_segment + 1).zfill(3)) + '.png'")),
						"dpi = rows")));
		script.addScript(PythonScript.callFunction("plt.close", "fig"));
		script.stopFor();
	}

	private void generateBackwardTracks() {
		List<Parameter> parameters = new ArrayList<Parameter>();
		parameters.addAll(Arrays.asList(new Parameter("filePath", "str", temporaryFilePath),
				new Parameter("pyr_scale", "float", ComputeTracksParameters.getPyrScale()),
				new Parameter("levels", "int", ComputeTracksParameters.getLevels()),
				new Parameter("winsize", "int", ComputeTracksParameters.getWinSize()),
				new Parameter("iterations", "int", ComputeTracksParameters.getIterations()),
				new Parameter("poly_n", "int", ComputeTracksParameters.getPolyn()),
				new Parameter("poly_sigma", "float", ComputeTracksParameters.getPolySigma()),
				new Parameter("flags", "int", ComputeTracksParameters.getFlags()),
				new Parameter("n_spixels", "int", ComputeTracksParameters.getNumberSuperpixels()),
				new Parameter("saveDirectory1", "str", matlabFolder.getAbsolutePath()),
				new Parameter("fileName", "str", Globals.getNameWithoutExtension(currentFilePath))));

		if (ComputeTracksParameters.isOutput("backward_tracks_visualisation")) {
			parameters.add(new Parameter("temporal_segment_length", "int",
					ComputeTracksParameters.getBackwardTracksTemporalSegment()));

			for (int i = 0; i < ComputeTracksParameters.getNumberSelectedChannels(); i++) {
				int channelIndex = ComputeTracksParameters.getSelectedChannels(i);
				parameters.add(new Parameter("color_" + channelIndex, "str",
						ComputeTracksParameters.getColorOption("backward_tracks_colors", i)));
			}

			parameters.add(new Parameter("saveDirectory2", "str", backwardTracksImageSequenceFolder.getAbsolutePath()));
		}

		addScriptHeader();

		script.addCommnet("define parameters dictionary");
		script.addParameter(parameters);
		script.addScript(script.createParameterDictionary());
		script.newLine();

		importFile();

		script.addCommnet("set optical flow parameters");
		script.addScript(PythonScript.createDictionary("optical_flow_params",
				Arrays.asList(new Pair<>("pyr_scale", "parameters.get('pyr_scale')"),
						new Pair<>("levels", "parameters.get('levels')"),
						new Pair<>("winsize", "parameters.get('winsize')"),
						new Pair<>("iterations", "parameters.get('iterations')"),
						new Pair<>("poly_n", "parameters.get('poly_n')"),
						new Pair<>("poly_sigma", "parameters.get('poly_sigma')"),
						new Pair<>("flags", "parameters.get('flags')"))));
		script.newLine();

		script.addCommnet("generate backward tracks");
		for (int i = 0; i < ComputeTracksParameters.getNumberSelectedChannels(); i++) {
			int channelIndex = ComputeTracksParameters.getSelectedChannels(i);

			script.addScript(PythonScript.print(
					PythonScript.addString("-Computing backward tracks for channel " + (channelIndex + 1) + "...")));

			if (ComputeTracksParameters.getDenseBackwardTracks())
				script.addScript(PythonScript.callFunctionWithResult(
						Arrays.asList("optflow_" + (channelIndex + 1), "forward_tracks_" + (channelIndex + 1),
								"backward_tracks_" + (channelIndex + 1)),
						"compute_grayscale_vid_superpixel_tracks_FB",
						Arrays.asList("vidstack[:,:,:," + channelIndex + "]", "optical_flow_params",
								"n_spixels = parameters.get('n_spixels')", "dense = True", "mindensity = 1")));
			else
				script.addScript(PythonScript.callFunctionWithResult(
						Arrays.asList("optflow_" + (channelIndex + 1), "forward_tracks_" + (channelIndex + 1),
								"backward_tracks_" + (channelIndex + 1)),
						"compute_grayscale_vid_superpixel_tracks_FB",
						Arrays.asList("vidstack[:,:,:," + channelIndex + "]", "optical_flow_params",
								"n_spixels = parameters.get('n_spixels')")));
		}
		script.newLine();

		script.addCommnet("save backward tracks");
		script.addScript(PythonScript.print(PythonScript.addString("-Saving backward tracks..")));
		script.addScript(PythonScript.setValue("saveLocation",
				"parameters.get('saveDirectory1') + '/' + parameters.get('fileName') + '_backward_tracks.mat'"));

		List<Pair<String, String>> saveList = new ArrayList<Pair<String, String>>();
		for (int i = 0; i < ComputeTracksParameters.getNumberSelectedChannels(); i++) {
			int channelIndex = ComputeTracksParameters.getSelectedChannels(i);

			saveList.add(new Pair<>("backward_tracks_" + (channelIndex + 1), "backward_tracks_" + (channelIndex + 1)));
		}
		// metadata
		saveList.add(new Pair<>("metadata", "np.array([[parameters.get('fileName'), [rows, columns], ["
				+ String.join(",", Globals.convertStringList(ComputeTracksParameters.getSelectedChannels()))
				+ "], 'tracks'], ['backward', '" + ComputeTracksParameters.getDenseForwardTracks()
				+ "', parameters.get('n_spixels'), parameters.get('pyr_scale'), parameters.get('levels'), parameters.get('winsize'), parameters.get('iterations'), parameters.get('poly_n'), parameters.get('poly_sigma'), parameters.get('flags')]])"));

		script.addScript(PythonScript.callFunction("spio.savemat",
				Arrays.asList("saveLocation", PythonScript.makeSaveList(saveList))));
		script.newLine();
	}

	private void visualiseBackwardTracks() {
		script.addCommnet("plot backward tracks");
		script.addScript(PythonScript.setValue("vidstack", PythonScript.callFunction("np.squeeze", "vidstack")));
		script.addScript(PythonScript.setValue("saveLocation", "parameters.get('saveDirectory2')"));

		script.addScript(PythonScript.print(PythonScript.addString("-Plotting backward tracks...")));
		script.addScript(PythonScript.setValue("len_segment", "parameters.get('temporal_segment_length')"));
		script.addScript(PythonScript.print(PythonScript.addString(">") + " + str(frames - len_segment)"));
		script.startFor("frame", "len_segment", "frames", "1");
		script.addScript(PythonScript.print(PythonScript.addString("!") + " + str(frame - len_segment + 1)"));
		script.addScript(PythonScript.setValue("frame_img", "vidstack[-(frame + 1)]"));
		script.addScript(PythonScript.setValue("fig", PythonScript.callFunction("plt.figure", "")));
		script.addScript(PythonScript.callFunction("fig.set_size_inches",
				Arrays.asList("float(columns) / rows", "1", "forward = False")));
		script.addScript(PythonScript.setValue("ax",
				PythonScript.callFunction("plt.Axes", Arrays.asList("fig", "[0.,0.,1.,1.]"))));
		script.addScript(PythonScript.callFunction("ax.set_axis_off", ""));
		script.addScript(PythonScript.callFunction("fig.add_axes", "ax"));
		script.addScript(PythonScript.callFunction("ax.set_xlim", "[0, columns]"));
		script.addScript(PythonScript.callFunction("ax.set_ylim", "[rows, 0]"));
		script.addScript(PythonScript.callFunction("ax.grid", PythonScript.addString("off")));
		script.addScript(PythonScript.callFunction("ax.axis", PythonScript.addString("off")));
		script.addScript(PythonScript.callFunction("ax.imshow", Arrays.asList("frame_img", "alpha = 0.6")));
		script.newLine();

		for (int i = 0; i < ComputeTracksParameters.getNumberSelectedChannels(); i++) {
			int channelIndex = ComputeTracksParameters.getSelectedChannels(i);

			script.addScript(PythonScript.callFunction("plot_tracks",
					Arrays.asList("backward_tracks_" + (channelIndex + 1) + "[:,frame-len_segment:frame+1]", "ax",
							"color = parameters.get('color_" + channelIndex + "')", "lw = 1")));
		}
		script.newLine();

		script.addScript(PythonScript.callFunction("fig.savefig",
				Arrays.asList(PythonScript.callFunction("os.path.join", Arrays.asList("saveLocation",
						"parameters.get('fileName') + '_backward_track_plot_%s' %(str(frame - len_segment + 1).zfill(3)) + '.png'")),
						"dpi = rows")));
		script.addScript(PythonScript.callFunction("plt.close", "fig"));
		script.stopFor();
	}

	private void generateMotionField() {
		List<Parameter> parameters = new ArrayList<Parameter>();
		parameters.addAll(Arrays.asList(new Parameter("filePath", "str", temporaryFilePath),
				new Parameter("pyr_scale", "float", ComputeTracksParameters.getPyrScale()),
				new Parameter("levels", "int", ComputeTracksParameters.getLevels()),
				new Parameter("winsize", "int", ComputeTracksParameters.getWinSize()),
				new Parameter("iterations", "int", ComputeTracksParameters.getIterations()),
				new Parameter("poly_n", "int", ComputeTracksParameters.getPolyn()),
				new Parameter("poly_sigma", "float", ComputeTracksParameters.getPolySigma()),
				new Parameter("flags", "int", ComputeTracksParameters.getFlags()),
				new Parameter("n_spixels", "int", ComputeTracksParameters.getNumberSuperpixels()),
				new Parameter("fileName", "str", Globals.getNameWithoutExtension(currentFilePath)),
				new Parameter("selected_channels", "int", ComputeTracksParameters.getNumberSelectedChannels())));

		if (ComputeTracksParameters.getSaveOption("motion_field_save_options", ".mat"))
			parameters.add(new Parameter("saveDirectory1", "str", matlabFolder.getAbsolutePath()));
		if (ComputeTracksParameters.getSaveOption("motion_field_save_options", ".tif"))
			parameters.add(new Parameter("saveDirectory2", "str", motionFieldImageSequenceFolder.getAbsolutePath()));

		addScriptHeader();

		script.addCommnet("define parameters dictionary");
		script.addParameter(parameters);
		script.addScript(script.createParameterDictionary());
		script.newLine();

		importFile();

		script.addCommnet("set optical flow parameters");
		script.addScript(PythonScript.createDictionary("optical_flow_params",
				Arrays.asList(new Pair<>("pyr_scale", "parameters.get('pyr_scale')"),
						new Pair<>("levels", "parameters.get('levels')"),
						new Pair<>("winsize", "parameters.get('winsize')"),
						new Pair<>("iterations", "parameters.get('iterations')"),
						new Pair<>("poly_n", "parameters.get('poly_n')"),
						new Pair<>("poly_sigma", "parameters.get('poly_sigma')"),
						new Pair<>("flags", "parameters.get('flags')"))));
		script.newLine();

		script.addCommnet("generate motion field");
		for (int i = 0; i < ComputeTracksParameters.getNumberSelectedChannels(); i++) {
			int channelIndex = ComputeTracksParameters.getSelectedChannels(i);

			script.addScript(PythonScript.print(
					PythonScript.addString("-Computing motion field for channel " + (channelIndex + 1) + "...")));
			script.addScript(PythonScript.callFunctionWithResult(
					Arrays.asList("optflow_" + (channelIndex + 1), "forward_tracks_" + (channelIndex + 1)),
					"compute_grayscale_vid_superpixel_tracks", Arrays.asList("vidstack[:,:,:," + channelIndex + "]",
							"optical_flow_params", "parameters.get('n_spixels')")));
		}
		script.newLine();

		if (ComputeTracksParameters.getSaveOption("motion_field_save_options", ".mat")) {
			script.addCommnet("save motion field");
			script.addScript(PythonScript.print(PythonScript.addString("-Saving motion field..")));
			script.addScript(PythonScript.setValue("saveLocation",
					"parameters.get('saveDirectory1') + '/'  + parameters.get('fileName') + '_motion_field.mat'"));

			List<Pair<String, String>> saveList = new ArrayList<Pair<String, String>>();
			for (int i = 0; i < ComputeTracksParameters.getNumberSelectedChannels(); i++) {
				int channelIndex = ComputeTracksParameters.getSelectedChannels(i);

				saveList.add(new Pair<>("optflow_" + (channelIndex + 1), "optflow_" + (channelIndex + 1)));
			}
			// metadata
			saveList.add(new Pair<>("metadata", "np.array([[parameters.get('fileName'), [rows, columns], ["
					+ String.join(",", Globals.convertStringList(ComputeTracksParameters.getSelectedChannels()))
					+ "], 'motion_field'], ['forward', 'false', parameters.get('n_spixels'), parameters.get('pyr_scale'), parameters.get('levels'), parameters.get('winsize'), parameters.get('iterations'), parameters.get('poly_n'), parameters.get('poly_sigma'), parameters.get('flags')]])"));

			script.addScript(PythonScript.callFunction("spio.savemat",
					Arrays.asList("saveLocation", PythonScript.makeSaveList(saveList))));
			script.newLine();
		}

		if (ComputeTracksParameters.getSaveOption("motion_field_save_options", ".tif")) {
			script.addCommnet("plot motion field");
			script.addScript(PythonScript.setValue("saveLocation", "parameters.get('saveDirectory2')"));

			script.addScript(PythonScript.print(PythonScript.addString("-Plotting motion field...")));
			script.addScript(PythonScript.print(PythonScript.addString(">") + " + str(frames)"));
			script.startFor("frame", "0", "frames", "1");
			script.addScript(PythonScript.print(PythonScript.addString("!") + " + str(frame + 1)"));
			script.addScript(PythonScript.callFunctionWithResult(Arrays.asList("fig", "ax"), "plt.subplots",
					Arrays.asList("nrows = 1", "ncols = parameters.get('selected_channels')")));

			if (ComputeTracksParameters.getNumberSelectedChannels() > 1) {
				script.addScript(PythonScript.callFunction("fig.set_size_inches", Arrays.asList(
						"float(columns) / rows * parameters.get('selected_channels') + 0.1", "1", "forward = False")));
				script.addScript(PythonScript.callFunction("plt.subplots_adjust",
						Arrays.asList("left = 0", "right = 1", "bottom = 0", "top = 1", "hspace = 0", "wspace = 0.1")));

				for (int i = 0; i < ComputeTracksParameters.getNumberSelectedChannels(); i++) {
					int channelIndex = ComputeTracksParameters.getSelectedChannels(i);

					script.addScript(PythonScript.callFunction("ax[" + i + "].imshow", Arrays.asList(
							PythonScript.callFunction("view_ang_flow", "optflow_" + (channelIndex + 1) + "[frame]"),
							"aspect = 'auto'")));
					script.addScript(PythonScript.callFunction("ax[" + i + "].grid", PythonScript.addString("off")));
					script.addScript(PythonScript.callFunction("ax[" + i + "].axis", PythonScript.addString("off")));
				}
			} else {
				script.addScript(PythonScript.callFunction("fig.set_size_inches", Arrays.asList(
						"float(columns) / rows * parameters.get('selected_channels')", "1", "forward = False")));
				script.addScript(PythonScript.callFunction("plt.subplots_adjust",
						Arrays.asList("left = 0", "right = 1", "bottom = 0", "top = 1", "hspace = 0", "wspace = 0")));

				script.addScript(PythonScript.callFunction("ax.imshow", PythonScript.callFunction("view_ang_flow",
						"optflow_" + (ComputeTracksParameters.getSelectedChannels(0) + 1) + "[frame]")));
				script.addScript(PythonScript.callFunction("ax.grid", PythonScript.addString("off")));
				script.addScript(PythonScript.callFunction("ax.axis", PythonScript.addString("off")));
			}

			script.addScript(PythonScript.callFunction("fig.savefig",
					Arrays.asList(PythonScript.callFunction("os.path.join", Arrays.asList("saveLocation",
							"parameters.get('fileName') + '_motion_field_plot_%s' %(str(frame + 1).zfill(3)) + '.png'")),
							"dpi = rows")));
			script.addScript(PythonScript.callFunction("plt.close", ""));
			script.stopFor();
		}

	}

	private void generateMOSESMesh() {
		String track;
		if (ComputeTracksParameters.getMOSESMeshForwardTracks())
			track = "forward";
		else
			track = "backward";

		List<Parameter> parameters = new ArrayList<Parameter>();
		parameters.addAll(Arrays.asList(new Parameter("filePath", "str", temporaryFilePath),
				new Parameter("pyr_scale", "float", ComputeTracksParameters.getPyrScale()),
				new Parameter("levels", "int", ComputeTracksParameters.getLevels()),
				new Parameter("winsize", "int", ComputeTracksParameters.getWinSize()),
				new Parameter("iterations", "int", ComputeTracksParameters.getIterations()),
				new Parameter("poly_n", "int", ComputeTracksParameters.getPolyn()),
				new Parameter("poly_sigma", "float", ComputeTracksParameters.getPolySigma()),
				new Parameter("flags", "int", ComputeTracksParameters.getFlags()),
				new Parameter("n_spixels", "int", ComputeTracksParameters.getNumberSuperpixels()),
				new Parameter("saveDirectory1", "str", matlabFolder.getAbsolutePath()),
				new Parameter("fileName", "str", Globals.getNameWithoutExtension(currentFilePath)),
				new Parameter("selected_channels", "int", ComputeTracksParameters.getNumberSelectedChannels()),
				new Parameter("MOSES_mesh_distance_threshold", "float",
						ComputeTracksParameters.getMOSESMeshDistanceThreshold())));

		if (ComputeTracksParameters.isOutput("MOSES_mesh_frame_visualisation")) {
			parameters.add(new Parameter("MOSES_mesh_frame", "int", ComputeTracksParameters.getMOSESMeshFrame()));
			parameters.add(new Parameter("saveDirectory2", "str", MOSESMeshImageSequenceFolder.getAbsolutePath()));
			parameters.add(new Parameter("saveDirectory3", "str", imageFolder.getAbsolutePath()));
		}

		addScriptHeader();

		script.addCommnet("define parameters dictionary");
		script.addParameter(parameters);
		script.addScript(script.createParameterDictionary());
		script.newLine();

		importFile();

		script.addCommnet("set optical flow parameters");
		script.addScript(PythonScript.createDictionary("optical_flow_params",
				Arrays.asList(new Pair<>("pyr_scale", "parameters.get('pyr_scale')"),
						new Pair<>("levels", "parameters.get('levels')"),
						new Pair<>("winsize", "parameters.get('winsize')"),
						new Pair<>("iterations", "parameters.get('iterations')"),
						new Pair<>("poly_n", "parameters.get('poly_n')"),
						new Pair<>("poly_sigma", "parameters.get('poly_sigma')"),
						new Pair<>("flags", "parameters.get('flags')"))));
		script.newLine();

		script.addCommnet("generate tracks");
		for (int i = 0; i < ComputeTracksParameters.getNumberSelectedChannels(); i++) {
			int channelIndex = ComputeTracksParameters.getSelectedChannels(i);

			script.addScript(PythonScript.print(PythonScript
					.addString("-Computing tracks for channel " + (channelIndex + 1) + " (MOSES mesh)...")));
			script.addScript(PythonScript.callFunctionWithResult(
					Arrays.asList("optflow_" + (channelIndex + 1), "forward_tracks_" + (channelIndex + 1),
							"backward_tracks_" + (channelIndex + 1)),
					"compute_grayscale_vid_superpixel_tracks_FB", Arrays.asList("vidstack[:,:,:," + channelIndex + "]",
							"optical_flow_params", "n_spixels = parameters.get('n_spixels')")));
		}
		script.newLine();

		script.addCommnet("generate MOSES mesh");
		script.addScript(PythonScript.setValue("spixel_size",
				"forward_tracks_" + (ComputeTracksParameters.getSelectedChannels(0) + 1) + "[1,0,1] - forward_tracks_"
						+ (ComputeTracksParameters.getSelectedChannels(0) + 1) + "[1,0,0]"));

		for (int i = 0; i < ComputeTracksParameters.getNumberSelectedChannels(); i++) {
			int channelIndex = ComputeTracksParameters.getSelectedChannels(i);

			script.addScript(PythonScript
					.print(PythonScript.addString("-Computing MOSES mesh for channel" + (channelIndex + 1) + "...")));

			script.addScript(PythonScript.setValue(
					Arrays.asList("MOSES_mesh_strain_time_" + (channelIndex + 1),
							"MOSES_mesh_neighborlist_" + (channelIndex + 1)),
					PythonScript.callFunction("construct_MOSES_mesh",
							Arrays.asList(track + "_tracks_" + (channelIndex + 1),
									"dist_thresh = parameters.get('MOSES_mesh_distance_threshold')",
									"spixel_size = spixel_size"))));

		}
		script.newLine();

		script.addCommnet("save MOSES mesh");
		script.addScript(PythonScript.print(PythonScript.addString("-Saving MOSES mesh...")));
		script.addScript(PythonScript.setValue("saveLocation",
				"parameters.get('saveDirectory1') + '/' + parameters.get('fileName') + '_" + track
						+ "_tracks_MOSES_mesh.mat'"));

		List<Pair<String, String>> saveList = new ArrayList<Pair<String, String>>();
		for (int i = 0; i < ComputeTracksParameters.getNumberSelectedChannels(); i++) {
			int channelIndex = ComputeTracksParameters.getSelectedChannels(i);

			saveList.add(new Pair<>("MOSES_mesh_neighborlist_" + (channelIndex + 1),
					"MOSES_mesh_neighborlist_" + (channelIndex + 1)));
			saveList.add(new Pair<>("MOSES_mesh_strain_time_" + (channelIndex + 1),
					"MOSES_mesh_strain_time_" + (channelIndex + 1)));
		}
		// metadata
		saveList.add(new Pair<>("metadata", "np.array([[parameters.get('fileName'), [rows, columns], ["
				+ String.join(",", Globals.convertStringList(ComputeTracksParameters.getSelectedChannels()))
				+ "], 'MOSES_mesh'], ['" + track
				+ "', 'false', parameters.get('n_spixels'), parameters.get('pyr_scale'), parameters.get('levels'), parameters.get('winsize'), parameters.get('iterations'), parameters.get('poly_n'), parameters.get('poly_sigma'),  parameters.get('flags')], [parameters.get('MOSES_mesh_distance_threshold')]])"));

		script.addScript(PythonScript.callFunction("spio.savemat",
				Arrays.asList("saveLocation", PythonScript.makeSaveList(saveList))));
		script.newLine();
	}

	private void generateMOSESMeshFrameVisualisation() {
		String track;
		if (ComputeTracksParameters.getMOSESMeshForwardTracks())
			track = "forward";
		else
			track = "backward";

		script.addScript(PythonScript.setValue("ok", "1"));
		script.newLine();

		script.addCommnet("modify neighbourlist for superpixels that don't have any valid meighbors");
		for (int i = 0; i < ComputeTracksParameters.getNumberSelectedChannels(); i++) {
			int channelIndex = ComputeTracksParameters.getSelectedChannels(i);

			script.startFor("i", "len(MOSES_mesh_neighborlist_" + (channelIndex + 1) + ")");
			script.startIf("len(MOSES_mesh_neighborlist_" + (channelIndex + 1) + "[i]) == 0");
			script.addScript(PythonScript.setValue("ok", "0"));
			script.addScript(PythonScript.setValue("MOSES_mesh_neighborlist_" + (channelIndex + 1) + "[i]", "[i]"));
			script.stopIf();
			script.stopFor();
			script.newLine();
		}

		script.addCommnet("compute MOSES mesh neighbour graphs");
		for (int i = 0; i < ComputeTracksParameters.getNumberSelectedChannels(); i++) {
			int channelIndex = ComputeTracksParameters.getSelectedChannels(i);

			script.addScript(PythonScript.print(PythonScript
					.addString("-Computing MOSES mesh neighbour graph for channel" + (channelIndex + 1) + "...")));

			if (track.equals("forward"))
				script.addScript(PythonScript.setValue("mesh_frame_networkx_G_" + (channelIndex + 1),
						PythonScript.callFunction("from_neighbor_list_to_graph",
								Arrays.asList("forward_tracks_" + (channelIndex + 1),
										"MOSES_mesh_neighborlist_" + (channelIndex + 1),
										"parameters.get('MOSES_mesh_frame')"))));
			else
				script.addScript(PythonScript.setValue("mesh_frame_networkx_G_" + (channelIndex + 1),
						PythonScript.callFunction("from_neighbor_list_to_graph",
								Arrays.asList("backward_tracks_" + (channelIndex + 1),
										"MOSES_mesh_neighborlist_" + (channelIndex + 1),
										"-(parameters.get('MOSES_mesh_frame') + 1)"))));
		}
		script.newLine();

		script.addCommnet("plot MOSES mesh frame");
		script.addScript(PythonScript.print(PythonScript.addString("-Plotting MOSES mesh...")));
		script.addScript(PythonScript.setValue("vidstack", PythonScript.callFunction("np.squeeze", "vidstack")));
		script.addScript(PythonScript.callFunctionWithResult(Arrays.asList("fig", "ax"), "plt.subplots",
				Arrays.asList("nrows = 1", "ncols = parameters.get('selected_channels')")));

		if (ComputeTracksParameters.getNumberSelectedChannels() > 1) {
			script.addScript(PythonScript.callFunction("fig.set_size_inches", Arrays.asList(
					"float(columns) / rows * parameters.get('selected_channels') + 0.1", "1", "forward = False")));
			script.addScript(PythonScript.callFunction("plt.subplots_adjust",
					Arrays.asList("left = 0", "right = 1", "bottom = 0", "top = 1", "hspace = 0", "wspace = 0.1")));

			for (int i = 0; i < ComputeTracksParameters.getNumberSelectedChannels(); i++) {
				int channelIndex = ComputeTracksParameters.getSelectedChannels(i);

				if (track.equals("forward"))
					script.addScript(PythonScript.callFunction("ax[" + i + "].imshow", Arrays
							.asList("vidstack[parameters.get('MOSES_mesh_frame')]", "alpha = 0.7", "aspect = 'auto'")));

				else
					script.addScript(PythonScript.callFunction("ax[" + i + "].imshow", Arrays.asList(
							"vidstack[-(parameters.get('MOSES_mesh_frame') + 1)]", "alpha = 0.7", "aspect = 'auto'")));

				script.addScript(PythonScript.callFunction("visualise_mesh",
						Arrays.asList("mesh_frame_networkx_G_" + (channelIndex + 1),
								track + "_tracks_" + (channelIndex + 1)
										+ "[:, parameters.get('MOSES_mesh_frame'), [1,0]]",
								"ax[" + i + "]", "node_size = spixel_size / 20", "linewidths = 0.3", "width = 0.3",
								"node_color = " + PythonScript
										.addString(ComputeTracksParameters.getColorOption("MOSES_mesh_colors", i)))));

				script.addScript(PythonScript.callFunction("ax[" + i + "].set_xlim", "[0, columns]"));
				script.addScript(PythonScript.callFunction("ax[" + i + "].set_ylim", "[rows, 0]"));
				script.addScript(PythonScript.callFunction("ax[" + i + "].grid", PythonScript.addString("off")));
				script.addScript(PythonScript.callFunction("ax[" + i + "].axis", PythonScript.addString("off")));
			}
		} else {
			script.addScript(PythonScript.callFunction("fig.set_size_inches", Arrays
					.asList("float(columns) / rows * parameters.get('selected_channels')", "1", "forward = False")));
			script.addScript(PythonScript.callFunction("plt.subplots_adjust",
					Arrays.asList("left = 0", "right = 1", "bottom = 0", "top = 1", "hspace = 0", "wspace = 0")));

			if (track.equals("forward"))
				script.addScript(PythonScript.callFunction("ax.imshow", Arrays
						.asList("vidstack[parameters.get('MOSES_mesh_frame')]", "alpha = 0.7", "aspect = 'auto'")));
			else
				script.addScript(PythonScript.callFunction("ax.imshow", Arrays.asList(
						"vidstack[-(parameters.get('MOSES_mesh_frame') + 1)]", "alpha = 0.7", "aspect = 'auto'")));

			script.addScript(PythonScript.callFunction("visualise_mesh", Arrays.asList(
					"mesh_frame_networkx_G_" + (ComputeTracksParameters.getSelectedChannels(0) + 1),
					track + "_tracks_" + (ComputeTracksParameters.getSelectedChannels(0) + 1)
							+ "[:, parameters.get('MOSES_mesh_frame'), [1,0]]",
					"ax", "node_size = spixel_size / 20", "linewidths = 0.3", "width = 0.3", "node_color = "
							+ PythonScript.addString(ComputeTracksParameters.getColorOption("MOSES_mesh_colors", 0)))));

			script.addScript(PythonScript.callFunction("ax.set_xlim", "[0, columns]"));
			script.addScript(PythonScript.callFunction("ax.set_ylim", "[rows, 0]"));
			script.addScript(PythonScript.callFunction("ax.grid", PythonScript.addString("off")));
			script.addScript(PythonScript.callFunction("ax.axis", PythonScript.addString("off")));
		}

		script.addScript(PythonScript.callFunction("fig.savefig", Arrays.asList(
				PythonScript.callFunction("os.path.join", Arrays.asList("parameters.get('saveDirectory3')",
						"parameters.get('fileName') + '_MOSES_mesh_" + track
								+ "_tracks_plot_%s' %(str(parameters.get('MOSES_mesh_frame')).zfill(3)) + '.png'")),
				"dpi = rows")));

		script.addScript(PythonScript.callFunction("plt.close", ""));

		script.startIf("ok == 0");
		script.addScript(PythonScript.print(PythonScript
				.addString("+Warning: Some superpixels do not have neighbors at current distance threshold")));
		script.stopIf();
	}

	private void generateMOSESMeshCompleteVisualisation() {
		String track;
		if (ComputeTracksParameters.getMOSESMeshForwardTracks())
			track = "forward";
		else
			track = "backward";

		script.addScript(PythonScript.setValue("ok", "1"));
		script.newLine();

		script.addCommnet("modify neighbourlist for superpixels that don't have any valid meighbors");
		for (int i = 0; i < ComputeTracksParameters.getNumberSelectedChannels(); i++) {
			int channelIndex = ComputeTracksParameters.getSelectedChannels(i);

			script.startFor("i", "len(MOSES_mesh_neighborlist_" + (channelIndex + 1) + ")");
			script.startIf("len(MOSES_mesh_neighborlist_" + (channelIndex + 1) + "[i]) == 0");
			script.addScript(PythonScript.setValue("ok", "0"));
			script.addScript(PythonScript.setValue("MOSES_mesh_neighborlist_" + (channelIndex + 1) + "[i]", "[i]"));
			script.stopIf();
			script.stopFor();
			script.newLine();
		}

		script.addScript(PythonScript.print(PythonScript.addString("-Plotting MOSES mesh...")));
		script.addScript(PythonScript.setValue("vidstack", PythonScript.callFunction("np.squeeze", "vidstack")));
		script.addScript(PythonScript.print(PythonScript.addString(">") + " + str(frames)"));
		script.startFor("frame", "frames");
		script.addScript(PythonScript.print(PythonScript.addString("!") + " + str(frame + 1)"));
		script.addCommnet("compute MOSES mesh neighbour graphs");
		for (int i = 0; i < ComputeTracksParameters.getNumberSelectedChannels(); i++) {
			int channelIndex = ComputeTracksParameters.getSelectedChannels(i);

			script.addScript(PythonScript.setValue("mesh_frame_networkx_G_" + (channelIndex + 1),
					PythonScript.callFunction("from_neighbor_list_to_graph",
							Arrays.asList(track + "_tracks_" + (channelIndex + 1),
									"MOSES_mesh_neighborlist_" + (channelIndex + 1), "frame"))));

		}
		script.newLine();

		script.addCommnet("plot MOSES mesh frame");
		script.addScript(PythonScript.callFunctionWithResult(Arrays.asList("fig", "ax"), "plt.subplots",
				Arrays.asList("nrows = 1", "ncols = parameters.get('selected_channels')")));

		if (ComputeTracksParameters.getNumberSelectedChannels() > 1) {
			script.addScript(PythonScript.callFunction("fig.set_size_inches", Arrays.asList(
					"float(columns) / rows * parameters.get('selected_channels') + 0.1", "1", "forward = False")));
			script.addScript(PythonScript.callFunction("plt.subplots_adjust",
					Arrays.asList("left = 0", "right = 1", "bottom = 0", "top = 1", "hspace = 0", "wspace = 0.1")));

			for (int i = 0; i < ComputeTracksParameters.getNumberSelectedChannels(); i++) {
				int channelIndex = ComputeTracksParameters.getSelectedChannels(i);

				if (track.equals("forward"))
					script.addScript(PythonScript.callFunction("ax[" + i + "].imshow",
							Arrays.asList("vidstack[frame]", "alpha = 0.7", "aspect = 'auto'")));
				else
					script.addScript(PythonScript.callFunction("ax[" + i + "].imshow",
							Arrays.asList("vidstack[-(frame + 1)]", "alpha = 0.7", "aspect = 'auto'")));

				script.addScript(PythonScript.callFunction("visualise_mesh",
						Arrays.asList("mesh_frame_networkx_G_" + (channelIndex + 1),
								track + "_tracks_" + (channelIndex + 1) + "[:, frame, [1,0]]", "ax[" + i + "]",
								"node_size = spixel_size / 20", "linewidths = 0.3", "width = 0.3",
								"node_color = " + PythonScript
										.addString(ComputeTracksParameters.getColorOption("MOSES_mesh_colors", i)))));

				script.addScript(PythonScript.callFunction("ax[" + i + "].set_xlim", "[0, columns]"));
				script.addScript(PythonScript.callFunction("ax[" + i + "].set_ylim", "[rows, 0]"));
				script.addScript(PythonScript.callFunction("ax[" + i + "].grid", PythonScript.addString("off")));
				script.addScript(PythonScript.callFunction("ax[" + i + "].axis", PythonScript.addString("off")));
			}
		} else {
			script.addScript(PythonScript.callFunction("fig.set_size_inches", Arrays
					.asList("float(columns) / rows * parameters.get('selected_channels')", "1", "forward = False")));
			script.addScript(PythonScript.callFunction("plt.subplots_adjust",
					Arrays.asList("left = 0", "right = 1", "bottom = 0", "top = 1", "hspace = 0", "wspace = 0")));

			if (track.equals("forward"))
				script.addScript(PythonScript.callFunction("ax.imshow",
						Arrays.asList("vidstack[frame]", "alpha = 0.7", "aspect = 'auto'")));
			else
				script.addScript(PythonScript.callFunction("ax.imshow",
						Arrays.asList("vidstack[-(frame + 1)]", "alpha = 0.7", "aspect = 'auto'")));

			script.addScript(PythonScript.callFunction("visualise_mesh", Arrays.asList(
					"mesh_frame_networkx_G_" + (ComputeTracksParameters.getSelectedChannels(0) + 1),
					track + "_tracks_" + (ComputeTracksParameters.getSelectedChannels(0) + 1) + "[:, frame, [1,0]]",
					"ax", "node_size = spixel_size / 20", "linewidths = 0.3", "width = 0.3", "node_color = "
							+ PythonScript.addString(ComputeTracksParameters.getColorOption("MOSES_mesh_colors", 0)))));

			script.addScript(PythonScript.callFunction("ax.set_xlim", "[0, columns]"));
			script.addScript(PythonScript.callFunction("ax.set_ylim", "[rows, 0]"));
			script.addScript(PythonScript.callFunction("ax.grid", PythonScript.addString("off")));
			script.addScript(PythonScript.callFunction("ax.axis", PythonScript.addString("off")));
		}

		script.addScript(PythonScript.setValue("saveLocation", "parameters.get('saveDirectory2')"));
		script.addScript(PythonScript.callFunction("fig.savefig",
				Arrays.asList(
						PythonScript.callFunction("os.path.join",
								Arrays.asList("saveLocation",
										"parameters.get('fileName') + '_MOSES_mesh_" + track
												+ "_tracks_plot_%s' %(str(frame + 1).zfill(3)) + '.png'")),
						"dpi = rows")));

		script.addScript(PythonScript.callFunction("plt.close", ""));
		script.stopFor();

		script.startIf("ok == 0");
		script.addScript(PythonScript.print(PythonScript
				.addString("+Warning: Some superpixels do not have neighbors at current distance threshold")));
		script.stopIf();
	}

	private void generateRadialMesh() {
		String track;
		if (ComputeTracksParameters.getRadialMeshForwardTracks())
			track = "forward";
		else
			track = "backward";

		List<Parameter> parameters = new ArrayList<Parameter>();
		parameters.addAll(Arrays.asList(new Parameter("filePath", "str", temporaryFilePath),
				new Parameter("pyr_scale", "float", ComputeTracksParameters.getPyrScale()),
				new Parameter("levels", "int", ComputeTracksParameters.getLevels()),
				new Parameter("winsize", "int", ComputeTracksParameters.getWinSize()),
				new Parameter("iterations", "int", ComputeTracksParameters.getIterations()),
				new Parameter("poly_n", "int", ComputeTracksParameters.getPolyn()),
				new Parameter("poly_sigma", "float", ComputeTracksParameters.getPolySigma()),
				new Parameter("flags", "int", ComputeTracksParameters.getFlags()),
				new Parameter("n_spixels", "int", ComputeTracksParameters.getNumberSuperpixels()),
				new Parameter("saveDirectory1", "str", matlabFolder.getAbsolutePath()),
				new Parameter("fileName", "str", Globals.getNameWithoutExtension(currentFilePath)),
				new Parameter("selected_channels", "int", ComputeTracksParameters.getNumberSelectedChannels()),
				new Parameter("radial_mesh_distance_threshold", "float",
						ComputeTracksParameters.getRadialMeshDistanceThreshold())));

		if (ComputeTracksParameters.isOutput("radial_mesh_frame_visualisation")) {
			parameters.add(new Parameter("radial_mesh_frame", "int", ComputeTracksParameters.getRadialMeshFrame()));
			parameters.add(new Parameter("saveDirectory2", "str", radialMeshImageSequenceFolder.getAbsolutePath()));
			parameters.add(new Parameter("saveDirectory3", "str", imageFolder.getAbsolutePath()));
		}

		addScriptHeader();

		script.addCommnet("define parameters dictionary");
		script.addParameter(parameters);
		script.addScript(script.createParameterDictionary());
		script.newLine();

		importFile();

		script.addCommnet("set optical flow parameters");
		script.addScript(PythonScript.createDictionary("optical_flow_params",
				Arrays.asList(new Pair<>("pyr_scale", "parameters.get('pyr_scale')"),
						new Pair<>("levels", "parameters.get('levels')"),
						new Pair<>("winsize", "parameters.get('winsize')"),
						new Pair<>("iterations", "parameters.get('iterations')"),
						new Pair<>("poly_n", "parameters.get('poly_n')"),
						new Pair<>("poly_sigma", "parameters.get('poly_sigma')"),
						new Pair<>("flags", "parameters.get('flags')"))));
		script.newLine();

		script.addCommnet("generate tracks");
		for (int i = 0; i < ComputeTracksParameters.getNumberSelectedChannels(); i++) {
			int channelIndex = ComputeTracksParameters.getSelectedChannels(i);

			script.addScript(PythonScript.print(PythonScript
					.addString("-Computing tracks for channel " + (channelIndex + 1) + " (Radial mesh)...")));
			script.addScript(PythonScript.callFunctionWithResult(
					Arrays.asList("optflow_" + (channelIndex + 1), "forward_tracks_" + (channelIndex + 1),
							"backward_tracks_" + (channelIndex + 1)),
					"compute_grayscale_vid_superpixel_tracks_FB", Arrays.asList("vidstack[:,:,:," + channelIndex + "]",
							"optical_flow_params", "n_spixels = parameters.get('n_spixels')")));
		}
		script.newLine();

		script.addCommnet("generate radial mesh");
		script.addScript(PythonScript.setValue("spixel_size",
				"forward_tracks_" + (ComputeTracksParameters.getSelectedChannels(0) + 1) + "[1,0,1] - forward_tracks_"
						+ (ComputeTracksParameters.getSelectedChannels(0) + 1) + "[1,0,0]"));

		for (int i = 0; i < ComputeTracksParameters.getNumberSelectedChannels(); i++) {
			int channelIndex = ComputeTracksParameters.getSelectedChannels(i);

			script.addScript(PythonScript
					.print(PythonScript.addString("-Computing radial mesh for channel " + (channelIndex + 1) + "...")));

			script.addScript(PythonScript.setValue(
					Arrays.asList("radial_mesh_strain_time_" + (channelIndex + 1),
							"radial_mesh_neighborlist_" + (channelIndex + 1)),
					PythonScript.callFunction("construct_radial_neighbour_mesh",
							Arrays.asList(track + "_tracks_" + (channelIndex + 1),
									"dist_thresh = parameters.get('radial_mesh_distance_threshold')",
									"spixel_size = spixel_size", "use_counts = False"))));

		}
		script.newLine();

		script.addCommnet("save radial mesh");
		script.addScript(PythonScript.print(PythonScript.addString("-Saving radial mesh...")));
		script.addScript(PythonScript.setValue("saveLocation",
				"parameters.get('saveDirectory1') + '/'  + parameters.get('fileName') + '_" + track
						+ "_tracks_radial_mesh.mat'"));

		List<Pair<String, String>> saveList = new ArrayList<Pair<String, String>>();
		for (int i = 0; i < ComputeTracksParameters.getNumberSelectedChannels(); i++) {
			int channelIndex = ComputeTracksParameters.getSelectedChannels(i);

			saveList.add(new Pair<>("radial_mesh_neighborlist_" + (channelIndex + 1),
					"radial_mesh_neighborlist_" + (channelIndex + 1)));
			saveList.add(new Pair<>("radial_mesh_strain_time_" + (channelIndex + 1),
					"radial_mesh_strain_time_" + (channelIndex + 1)));
		}
		// metadata
		saveList.add(new Pair<>("metadata", "np.array([[parameters.get('fileName'), [rows, columns], ["
				+ String.join(",", Globals.convertStringList(ComputeTracksParameters.getSelectedChannels()))
				+ "], 'radial_mesh'], ['" + track
				+ "', 'false', parameters.get('n_spixels'), parameters.get('pyr_scale'), parameters.get('levels'), parameters.get('winsize'), parameters.get('iterations'), parameters.get('poly_n'), parameters.get('poly_sigma'),  parameters.get('flags')], [parameters.get('radial_mesh_distance_threshold')]])"));

		script.addScript(PythonScript.callFunction("spio.savemat",
				Arrays.asList("saveLocation", PythonScript.makeSaveList(saveList))));
		script.newLine();
	}

	private void generateRadialMeshFrameVisualisation() {
		String track;
		if (ComputeTracksParameters.getRadialMeshForwardTracks())
			track = "forward";
		else
			track = "backward";

		script.addScript(PythonScript.setValue("ok", "1"));
		script.newLine();

		script.addCommnet("modify neighbourlist for superpixels that don't have any valid meighbors");
		for (int i = 0; i < ComputeTracksParameters.getNumberSelectedChannels(); i++) {
			int channelIndex = ComputeTracksParameters.getSelectedChannels(i);

			script.startFor("i",
					"len(radial_mesh_neighborlist_" + (channelIndex + 1) + "[parameters.get('radial_mesh_frame')])");
			script.startIf("len(radial_mesh_neighborlist_" + (channelIndex + 1)
					+ "[parameters.get('radial_mesh_frame')][i]) == 0");
			script.addScript(PythonScript.setValue("ok", "0"));
			script.addScript(PythonScript.setValue(
					"radial_mesh_neighborlist_" + (channelIndex + 1) + "[parameters.get('radial_mesh_frame')][i]",
					"[i]"));
			script.stopIf();
			script.stopFor();
			script.newLine();
		}

		script.addCommnet("compute radial mesh neighbour graphs");
		for (int i = 0; i < ComputeTracksParameters.getNumberSelectedChannels(); i++) {
			int channelIndex = ComputeTracksParameters.getSelectedChannels(i);

			script.addScript(PythonScript.print(PythonScript
					.addString("-Computing radial mesh neighbour graph for channel" + (channelIndex + 1) + "...")));

			if (track.equals("forward"))
				script.addScript(PythonScript.setValue("mesh_frame_networkx_G_" + (channelIndex + 1),
						PythonScript.callFunction("from_neighbor_list_to_graph",
								Arrays.asList("forward_tracks_" + (channelIndex + 1),
										"radial_mesh_neighborlist_" + (channelIndex + 1)
												+ "[parameters.get('radial_mesh_frame')]",
										"parameters.get('radial_mesh_frame')"))));
			else
				script.addScript(PythonScript.setValue("mesh_frame_networkx_G_" + (channelIndex + 1),
						PythonScript.callFunction("from_neighbor_list_to_graph",
								Arrays.asList("backward_tracks_" + (channelIndex + 1),
										"radial_mesh_neighborlist_" + (channelIndex + 1)
												+ "[parameters.get('radial_mesh_frame')]",
										"-(parameters.get('radial_mesh_frame') + 1)"))));
		}
		script.newLine();

		script.addCommnet("plot radial mesh frame");
		script.addScript(PythonScript.print(PythonScript.addString("-Plotting radial mesh...")));
		script.addScript(PythonScript.setValue("vidstack", PythonScript.callFunction("np.squeeze", "vidstack")));
		script.addScript(PythonScript.callFunctionWithResult(Arrays.asList("fig", "ax"), "plt.subplots",
				Arrays.asList("nrows = 1", "ncols = parameters.get('selected_channels')")));

		if (ComputeTracksParameters.getNumberSelectedChannels() > 1) {
			script.addScript(PythonScript.callFunction("fig.set_size_inches", Arrays.asList(
					"float(columns) / rows * parameters.get('selected_channels') + 0.1", "1", "forward = False")));
			script.addScript(PythonScript.callFunction("plt.subplots_adjust",
					Arrays.asList("left = 0", "right = 1", "bottom = 0", "top = 1", "hspace = 0", "wspace = 0.1")));

			for (int i = 0; i < ComputeTracksParameters.getNumberSelectedChannels(); i++) {
				int channelIndex = ComputeTracksParameters.getSelectedChannels(i);

				if (track.equals("forward"))
					script.addScript(PythonScript.callFunction("ax[" + i + "].imshow", Arrays.asList(
							"vidstack[parameters.get('radial_mesh_frame')]", "alpha = 0.7", "aspect = 'auto'")));

				else
					script.addScript(PythonScript.callFunction("ax[" + i + "].imshow", Arrays.asList(
							"vidstack[-(parameters.get('radial_mesh_frame') + 1)]", "alpha = 0.7", "aspect = 'auto'")));

				script.addScript(PythonScript.callFunction("visualise_mesh",
						Arrays.asList("mesh_frame_networkx_G_" + (channelIndex + 1),
								track + "_tracks_" + (channelIndex + 1)
										+ "[:, parameters.get('radial_mesh_frame'), [1,0]]",
								"ax[" + i + "]", "node_size = spixel_size / 20", "linewidths = 0.3", "width = 0.3",
								"node_color = " + PythonScript
										.addString(ComputeTracksParameters.getColorOption("radial_mesh_colors", i)))));

				script.addScript(PythonScript.callFunction("ax[" + i + "].set_xlim", "[0, columns]"));
				script.addScript(PythonScript.callFunction("ax[" + i + "].set_ylim", "[rows, 0]"));
				script.addScript(PythonScript.callFunction("ax[" + i + "].grid", PythonScript.addString("off")));
				script.addScript(PythonScript.callFunction("ax[" + i + "].axis", PythonScript.addString("off")));
			}
		} else {
			script.addScript(PythonScript.callFunction("fig.set_size_inches", Arrays
					.asList("float(columns) / rows * parameters.get('selected_channels')", "1", "forward = False")));
			script.addScript(PythonScript.callFunction("plt.subplots_adjust",
					Arrays.asList("left = 0", "right = 1", "bottom = 0", "top = 1", "hspace = 0", "wspace = 0")));

			if (track.equals("forward"))
				script.addScript(PythonScript.callFunction("ax.imshow", Arrays
						.asList("vidstack[parameters.get('radial_mesh_frame')]", "alpha = 0.7", "aspect = 'auto'")));
			else
				script.addScript(PythonScript.callFunction("ax.imshow", Arrays.asList(
						"vidstack[-(parameters.get('radial_mesh_frame') + 1)]", "alpha = 0.7", "aspect = 'auto'")));

			script.addScript(PythonScript.callFunction("visualise_mesh",
					Arrays.asList("mesh_frame_networkx_G_" + (ComputeTracksParameters.getSelectedChannels(0) + 1),
							track + "_tracks_" + (ComputeTracksParameters.getSelectedChannels(0) + 1)
									+ "[:, parameters.get('radial_mesh_frame'), [1,0]]",
							"ax", "node_size = spixel_size / 20", "linewidths = 0.3", "width = 0.3",
							"node_color = " + PythonScript
									.addString(ComputeTracksParameters.getColorOption("radial_mesh_colors", 0)))));

			script.addScript(PythonScript.callFunction("ax.set_xlim", "[0, columns]"));
			script.addScript(PythonScript.callFunction("ax.set_ylim", "[rows, 0]"));
			script.addScript(PythonScript.callFunction("ax.grid", PythonScript.addString("off")));
			script.addScript(PythonScript.callFunction("ax.axis", PythonScript.addString("off")));
		}

		script.addScript(PythonScript.callFunction("fig.savefig", Arrays.asList(
				PythonScript.callFunction("os.path.join", Arrays.asList("parameters.get('saveDirectory3')",
						"parameters.get('fileName') + '_radial_mesh_" + track
								+ "_tracks_plot_%s' %(str(parameters.get('radial_mesh_frame')).zfill(3)) + '.png'")),
				"dpi = rows")));

		script.addScript(PythonScript.callFunction("plt.close", ""));

		script.startIf("ok == 0");
		script.addScript(PythonScript.print(PythonScript
				.addString("+Warning: Some superpixels do not have neighbors at current distance threshold")));
		script.stopIf();
	}

	private void generateRadialMeshCompleteVisualisation() {
		String track;
		if (ComputeTracksParameters.getRadialMeshForwardTracks())
			track = "forward";
		else
			track = "backward";

		script.addScript(PythonScript.setValue("ok", "1"));
		script.newLine();

		script.startFor("frame", "frames");
		script.addCommnet("modify neighbourlist for superpixels that don't have any valid meighbors");
		for (int i = 0; i < ComputeTracksParameters.getNumberSelectedChannels(); i++) {
			int channelIndex = ComputeTracksParameters.getSelectedChannels(i);

			script.startFor("i", "len(radial_mesh_neighborlist_" + (channelIndex + 1) + ")");
			script.startIf("len(radial_mesh_neighborlist_" + (channelIndex + 1) + "[frame][i]) == 0");
			script.addScript(PythonScript.setValue("ok", "0"));
			script.addScript(
					PythonScript.setValue("radial_mesh_neighborlist_" + (channelIndex + 1) + "[frame][i]", "[i]"));
			script.stopIf();
			script.stopFor();
			script.newLine();
		}

		script.addScript(PythonScript.print(PythonScript.addString("-Plotting radial mesh...")));
		script.addScript(PythonScript.setValue("vidstack", PythonScript.callFunction("np.squeeze", "vidstack")));
		script.addScript(PythonScript.print(PythonScript.addString(">") + " + str(frames)"));
		script.addScript(PythonScript.print(PythonScript.addString("!") + " + str(frame + 1)"));
		script.addCommnet("compute radial mesh neighbour graphs");
		for (int i = 0; i < ComputeTracksParameters.getNumberSelectedChannels(); i++) {
			int channelIndex = ComputeTracksParameters.getSelectedChannels(i);

			script.addScript(PythonScript.setValue("mesh_frame_networkx_G_" + (channelIndex + 1),
					PythonScript.callFunction("from_neighbor_list_to_graph",
							Arrays.asList(track + "_tracks_" + (channelIndex + 1),
									"radial_mesh_neighborlist_" + (channelIndex + 1) + "[frame]", "frame"))));

		}
		script.newLine();

		script.addCommnet("plot radial mesh frame");
		script.addScript(PythonScript.callFunctionWithResult(Arrays.asList("fig", "ax"), "plt.subplots",
				Arrays.asList("nrows = 1", "ncols = parameters.get('selected_channels')")));

		if (ComputeTracksParameters.getNumberSelectedChannels() > 1) {
			script.addScript(PythonScript.callFunction("fig.set_size_inches", Arrays.asList(
					"float(columns) / rows * parameters.get('selected_channels') + 0.1", "1", "forward = False")));
			script.addScript(PythonScript.callFunction("plt.subplots_adjust",
					Arrays.asList("left = 0", "right = 1", "bottom = 0", "top = 1", "hspace = 0", "wspace = 0.1")));

			for (int i = 0; i < ComputeTracksParameters.getNumberSelectedChannels(); i++) {
				int channelIndex = ComputeTracksParameters.getSelectedChannels(i);

				if (track.equals("forward"))
					script.addScript(PythonScript.callFunction("ax[" + i + "].imshow",
							Arrays.asList("vidstack[frame]", "alpha = 0.7", "aspect = 'auto'")));
				else
					script.addScript(PythonScript.callFunction("ax[" + i + "].imshow",
							Arrays.asList("vidstack[-(frame + 1)]", "alpha = 0.7", "aspect = 'auto'")));

				script.addScript(PythonScript.callFunction("visualise_mesh",
						Arrays.asList("mesh_frame_networkx_G_" + (channelIndex + 1),
								track + "_tracks_" + (channelIndex + 1) + "[:, frame, [1,0]]", "ax[" + i + "]",
								"node_size = spixel_size / 20", "linewidths = 0.3", "width = 0.3",
								"node_color = " + PythonScript
										.addString(ComputeTracksParameters.getColorOption("radial_mesh_colors", i)))));

				script.addScript(PythonScript.callFunction("ax[" + i + "].set_xlim", "[0, columns]"));
				script.addScript(PythonScript.callFunction("ax[" + i + "].set_ylim", "[rows, 0]"));
				script.addScript(PythonScript.callFunction("ax[" + i + "].grid", PythonScript.addString("off")));
				script.addScript(PythonScript.callFunction("ax[" + i + "].axis", PythonScript.addString("off")));
			}
		} else {
			script.addScript(PythonScript.callFunction("fig.set_size_inches", Arrays
					.asList("float(columns) / rows * parameters.get('selected_channels')", "1", "forward = False")));
			script.addScript(PythonScript.callFunction("plt.subplots_adjust",
					Arrays.asList("left = 0", "right = 1", "bottom = 0", "top = 1", "hspace = 0", "wspace = 0")));

			if (track.equals("forward"))
				script.addScript(PythonScript.callFunction("ax.imshow",
						Arrays.asList("vidstack[frame]", "alpha = 0.7", "aspect = 'auto'")));
			else
				script.addScript(PythonScript.callFunction("ax.imshow",
						Arrays.asList("vidstack[-(frame + 1)]", "alpha = 0.7", "aspect = 'auto'")));

			script.addScript(PythonScript.callFunction("visualise_mesh",
					Arrays.asList("mesh_frame_networkx_G_" + (ComputeTracksParameters.getSelectedChannels(0) + 1),
							track + "_tracks_" + (ComputeTracksParameters.getSelectedChannels(0) + 1)
									+ "[:, frame, [1,0]]",
							"ax", "node_size = spixel_size / 20", "linewidths = 0.3", "width = 0.3",
							"node_color = " + PythonScript
									.addString(ComputeTracksParameters.getColorOption("radial_mesh_colors", 0)))));

			script.addScript(PythonScript.callFunction("ax.set_xlim", "[0, columns]"));
			script.addScript(PythonScript.callFunction("ax.set_ylim", "[rows, 0]"));
			script.addScript(PythonScript.callFunction("ax.grid", PythonScript.addString("off")));
			script.addScript(PythonScript.callFunction("ax.axis", PythonScript.addString("off")));
		}

		script.addScript(PythonScript.setValue("saveLocation", "parameters.get('saveDirectory2')"));
		script.addScript(PythonScript.callFunction("fig.savefig",
				Arrays.asList(
						PythonScript.callFunction("os.path.join",
								Arrays.asList("saveLocation",
										"parameters.get('fileName') + '_radial_mesh_" + track
												+ "_tracks_plot_%s' %(str(frame + 1).zfill(3)) + '.png'")),
						"dpi = rows")));

		script.addScript(PythonScript.callFunction("plt.close", ""));
		script.stopFor();

		script.startIf("ok == 0");
		script.addScript(PythonScript.print(PythonScript
				.addString("+Warning: Some superpixels do not have neighbors at current distance threshold")));
		script.stopIf();
	}

	private void generateNeighborMesh() {
		String track;
		if (ComputeTracksParameters.getNeighborMeshForwardTracks())
			track = "forward";
		else
			track = "backward";

		List<Parameter> parameters = new ArrayList<Parameter>();
		parameters.addAll(Arrays.asList(new Parameter("filePath", "str", temporaryFilePath),
				new Parameter("pyr_scale", "float", ComputeTracksParameters.getPyrScale()),
				new Parameter("levels", "int", ComputeTracksParameters.getLevels()),
				new Parameter("winsize", "int", ComputeTracksParameters.getWinSize()),
				new Parameter("iterations", "int", ComputeTracksParameters.getIterations()),
				new Parameter("poly_n", "int", ComputeTracksParameters.getPolyn()),
				new Parameter("poly_sigma", "float", ComputeTracksParameters.getPolySigma()),
				new Parameter("flags", "int", ComputeTracksParameters.getFlags()),
				new Parameter("n_spixels", "int", ComputeTracksParameters.getNumberSuperpixels()),
				new Parameter("saveDirectory1", "str", matlabFolder.getAbsolutePath()),
				new Parameter("fileName", "str", Globals.getNameWithoutExtension(currentFilePath)),
				new Parameter("selected_channels", "int", ComputeTracksParameters.getNumberSelectedChannels()),
				new Parameter("neighbors", "int", ComputeTracksParameters.getKNeighbor())));

		if (ComputeTracksParameters.isOutput("neighbor_mesh_frame_visualisation")) {
			parameters.add(new Parameter("neighbor_mesh_frame", "int", ComputeTracksParameters.getNeighborMeshFrame()));
			parameters.add(new Parameter("saveDirectory2", "str", neighborMeshImageSequenceFolder.getAbsolutePath()));
			parameters.add(new Parameter("saveDirectory3", "str", imageFolder.getAbsolutePath()));
		}

		addScriptHeader();

		script.addCommnet("define parameters dictionary");
		script.addParameter(parameters);
		script.addScript(script.createParameterDictionary());
		script.newLine();

		importFile();

		script.addCommnet("set optical flow parameters");
		script.addScript(PythonScript.createDictionary("optical_flow_params",
				Arrays.asList(new Pair<>("pyr_scale", "parameters.get('pyr_scale')"),
						new Pair<>("levels", "parameters.get('levels')"),
						new Pair<>("winsize", "parameters.get('winsize')"),
						new Pair<>("iterations", "parameters.get('iterations')"),
						new Pair<>("poly_n", "parameters.get('poly_n')"),
						new Pair<>("poly_sigma", "parameters.get('poly_sigma')"),
						new Pair<>("flags", "parameters.get('flags')"))));
		script.newLine();

		script.addCommnet("generate tracks");
		for (int i = 0; i < ComputeTracksParameters.getNumberSelectedChannels(); i++) {
			int channelIndex = ComputeTracksParameters.getSelectedChannels(i);

			script.addScript(PythonScript.print(PythonScript
					.addString("-Computing tracks for channel " + (channelIndex + 1) + " (neighbor mesh)...")));
			script.addScript(PythonScript.callFunctionWithResult(
					Arrays.asList("optflow_" + (channelIndex + 1), "forward_tracks_" + (channelIndex + 1),
							"backward_tracks_" + (channelIndex + 1)),
					"compute_grayscale_vid_superpixel_tracks_FB", Arrays.asList("vidstack[:,:,:," + channelIndex + "]",
							"optical_flow_params", "n_spixels = parameters.get('n_spixels')")));
		}
		script.newLine();

		script.addCommnet("generate neighbor mesh");
		script.addScript(PythonScript.setValue("spixel_size",
				"forward_tracks_" + (ComputeTracksParameters.getSelectedChannels(0) + 1) + "[1,0,1] - forward_tracks_"
						+ (ComputeTracksParameters.getSelectedChannels(0) + 1) + "[1,0,0]"));

		for (int i = 0; i < ComputeTracksParameters.getNumberSelectedChannels(); i++) {
			int channelIndex = ComputeTracksParameters.getSelectedChannels(i);

			script.addScript(PythonScript.print(
					PythonScript.addString("-Computing neighbor mesh for channel " + (channelIndex + 1) + "...")));

			script.addScript(PythonScript.setValue(
					Arrays.asList("neighbor_mesh_strain_time_" + (channelIndex + 1),
							"neighbor_mesh_neighborlist_" + (channelIndex + 1)),
					PythonScript.callFunction("construct_knn_neighbour_mesh", Arrays
							.asList(track + "_tracks_" + (channelIndex + 1), "k = parameters.get('neighbors')"))));

		}
		script.newLine();

		script.addCommnet("save neighbor mesh");
		script.addScript(PythonScript.print(PythonScript.addString("-Saving neighbor mesh...")));
		script.addScript(PythonScript.setValue("saveLocation",
				"parameters.get('saveDirectory1') + '/' + parameters.get('fileName') + '_" + track
						+ "_tracks_neighbor_mesh.mat'"));

		List<Pair<String, String>> saveList = new ArrayList<Pair<String, String>>();
		for (int i = 0; i < ComputeTracksParameters.getNumberSelectedChannels(); i++) {
			int channelIndex = ComputeTracksParameters.getSelectedChannels(i);

			saveList.add(new Pair<>("neighbor_mesh_neighborlist_" + (channelIndex + 1),
					"neighbor_mesh_neighborlist_" + (channelIndex + 1)));
			saveList.add(new Pair<>("neighbor_mesh_strain_time_" + (channelIndex + 1),
					"neighbor_mesh_strain_time_" + (channelIndex + 1)));
		}
		// metadata
		saveList.add(new Pair<>("metadata", "np.array([[parameters.get('fileName'), [rows, columns], ["
				+ String.join(",", Globals.convertStringList(ComputeTracksParameters.getSelectedChannels()))
				+ "], 'neighbor_mesh'], ['" + track
				+ "', 'false', parameters.get('n_spixels'), parameters.get('pyr_scale'), parameters.get('levels'), parameters.get('winsize'), parameters.get('iterations'), parameters.get('poly_n'), parameters.get('poly_sigma'),  parameters.get('flags')], [parameters.get('neighbors')]])"));

		script.addScript(PythonScript.callFunction("spio.savemat",
				Arrays.asList("saveLocation", PythonScript.makeSaveList(saveList))));
		script.newLine();
	}

	private void generateNeighborMeshFrameVisualisation() {
		String track;
		if (ComputeTracksParameters.getNeighborMeshForwardTracks())
			track = "forward";
		else
			track = "backward";

		script.addScript(PythonScript.setValue("ok", "1"));
		script.newLine();

		script.addCommnet("modify neighbourlist for superpixels that don't have any valid meighbors");
		for (int i = 0; i < ComputeTracksParameters.getNumberSelectedChannels(); i++) {
			int channelIndex = ComputeTracksParameters.getSelectedChannels(i);

			script.startFor("i", "len(neighbor_mesh_neighborlist_" + (channelIndex + 1)
					+ "[parameters.get('neighbor_mesh_frame')])");
			script.startIf("len(neighbor_mesh_neighborlist_" + (channelIndex + 1)
					+ "[parameters.get('neighbor_mesh_frame')][i]) == 0");
			script.addScript(PythonScript.setValue("ok", "0"));
			script.addScript(PythonScript.setValue(
					"neighbor_mesh_neighborlist_" + (channelIndex + 1) + "[parameters.get('neighbor_mesh_frame')][i]",
					"[i]"));
			script.stopIf();
			script.stopFor();
			script.newLine();
		}

		script.addCommnet("compute neighbor mesh neighbour graphs");
		for (int i = 0; i < ComputeTracksParameters.getNumberSelectedChannels(); i++) {
			int channelIndex = ComputeTracksParameters.getSelectedChannels(i);

			script.addScript(PythonScript.print(PythonScript
					.addString("-Computing neighbor mesh neighbour graph for channel" + (channelIndex + 1) + "...")));

			if (track.equals("forward"))
				script.addScript(PythonScript.setValue("mesh_frame_networkx_G_" + (channelIndex + 1),
						PythonScript.callFunction("from_neighbor_list_to_graph",
								Arrays.asList("forward_tracks_" + (channelIndex + 1),
										"neighbor_mesh_neighborlist_" + (channelIndex + 1)
												+ "[parameters.get('neighbor_mesh_frame')]",
										"parameters.get('neighbor_mesh_frame')"))));
			else
				script.addScript(PythonScript.setValue("mesh_frame_networkx_G_" + (channelIndex + 1),
						PythonScript.callFunction("from_neighbor_list_to_graph",
								Arrays.asList("backward_tracks_" + (channelIndex + 1),
										"neighbor_mesh_neighborlist_" + (channelIndex + 1)
												+ "[parameters.get('neighbor_mesh_frame')]",
										"-(parameters.get('neighbor_mesh_frame') + 1)"))));
		}
		script.newLine();

		script.addCommnet("plot neighbor mesh frame");
		script.addScript(PythonScript.print(PythonScript.addString("-Plotting neighbor mesh...")));
		script.addScript(PythonScript.setValue("vidstack", PythonScript.callFunction("np.squeeze", "vidstack")));
		script.addScript(PythonScript.callFunctionWithResult(Arrays.asList("fig", "ax"), "plt.subplots",
				Arrays.asList("nrows = 1", "ncols = parameters.get('selected_channels')")));

		if (ComputeTracksParameters.getNumberSelectedChannels() > 1) {
			script.addScript(PythonScript.callFunction("fig.set_size_inches", Arrays.asList(
					"float(columns) / rows * parameters.get('selected_channels') + 0.1", "1", "forward = False")));
			script.addScript(PythonScript.callFunction("plt.subplots_adjust",
					Arrays.asList("left = 0", "right = 1", "bottom = 0", "top = 1", "hspace = 0", "wspace = 0.1")));

			for (int i = 0; i < ComputeTracksParameters.getNumberSelectedChannels(); i++) {
				int channelIndex = ComputeTracksParameters.getSelectedChannels(i);

				if (track.equals("forward"))
					script.addScript(PythonScript.callFunction("ax[" + i + "].imshow", Arrays.asList(
							"vidstack[parameters.get('neighbor_mesh_frame')]", "alpha = 0.7", "aspect = 'auto'")));

				else
					script.addScript(PythonScript.callFunction("ax[" + i + "].imshow",
							Arrays.asList("vidstack[-(parameters.get('neighbor_mesh_frame') + 1)]", "alpha = 0.7",
									"aspect = 'auto'")));

				script.addScript(PythonScript.callFunction("visualise_mesh",
						Arrays.asList("mesh_frame_networkx_G_" + (channelIndex + 1),
								track + "_tracks_" + (channelIndex + 1)
										+ "[:, parameters.get('neighbor_mesh_frame'), [1,0]]",
								"ax[" + i + "]", "node_size = spixel_size / 20", "linewidths = 0.3", "width = 0.3",
								"node_color = " + PythonScript.addString(
										ComputeTracksParameters.getColorOption("neighbor_mesh_colors", i)))));

				script.addScript(PythonScript.callFunction("ax[" + i + "].set_xlim", "[0, columns]"));
				script.addScript(PythonScript.callFunction("ax[" + i + "].set_ylim", "[rows, 0]"));
				script.addScript(PythonScript.callFunction("ax[" + i + "].grid", PythonScript.addString("off")));
				script.addScript(PythonScript.callFunction("ax[" + i + "].axis", PythonScript.addString("off")));
			}
		} else {
			script.addScript(PythonScript.callFunction("fig.set_size_inches", Arrays
					.asList("float(columns) / rows * parameters.get('selected_channels')", "1", "forward = False")));
			script.addScript(PythonScript.callFunction("plt.subplots_adjust",
					Arrays.asList("left = 0", "right = 1", "bottom = 0", "top = 1", "hspace = 0", "wspace = 0")));

			if (track.equals("forward"))
				script.addScript(PythonScript.callFunction("ax.imshow", Arrays
						.asList("vidstack[parameters.get('neighbor_mesh_frame')]", "alpha = 0.7", "aspect = 'auto'")));
			else
				script.addScript(PythonScript.callFunction("ax.imshow", Arrays.asList(
						"vidstack[-(parameters.get('neighbor_mesh_frame') + 1)]", "alpha = 0.7", "aspect = 'auto'")));

			script.addScript(PythonScript.callFunction("visualise_mesh",
					Arrays.asList("mesh_frame_networkx_G_" + (ComputeTracksParameters.getSelectedChannels(0) + 1),
							track + "_tracks_" + (ComputeTracksParameters.getSelectedChannels(0) + 1)
									+ "[:, parameters.get('neighbor_mesh_frame'), [1,0]]",
							"ax", "node_size = spixel_size / 20", "linewidths = 0.3", "width = 0.3",
							"node_color = " + PythonScript
									.addString(ComputeTracksParameters.getColorOption("neighbor_mesh_colors", 0)))));

			script.addScript(PythonScript.callFunction("ax.set_xlim", "[0, columns]"));
			script.addScript(PythonScript.callFunction("ax.set_ylim", "[rows, 0]"));
			script.addScript(PythonScript.callFunction("ax.grid", PythonScript.addString("off")));
			script.addScript(PythonScript.callFunction("ax.axis", PythonScript.addString("off")));
		}

		script.addScript(PythonScript.callFunction("fig.savefig", Arrays.asList(
				PythonScript.callFunction("os.path.join", Arrays.asList("parameters.get('saveDirectory3')",
						"parameters.get('fileName') + '_neighbor_mesh_" + track
								+ "_tracks_plot_%s' %(str(parameters.get('neighbor_mesh_frame')).zfill(3)) + '.png'")),
				"dpi = rows")));

		script.addScript(PythonScript.callFunction("plt.close", ""));

		script.startIf("ok == 0");
		script.addScript(PythonScript.print(PythonScript
				.addString("+Warning: Some superpixels do not have neighbors at current distance threshold")));
		script.stopIf();
	}

	private void generateNeighborMeshCompleteVisualisation() {
		String track;
		if (ComputeTracksParameters.getNeighborMeshForwardTracks())
			track = "forward";
		else
			track = "backward";

		script.addScript(PythonScript.setValue("ok", "1"));
		script.newLine();

		script.startFor("frame", "frames");
		script.addCommnet("modify neighbourlist for superpixels that don't have any valid meighbors");
		for (int i = 0; i < ComputeTracksParameters.getNumberSelectedChannels(); i++) {
			int channelIndex = ComputeTracksParameters.getSelectedChannels(i);

			script.startFor("i", "len(neighbor_mesh_neighborlist_" + (channelIndex + 1) + ")");
			script.startIf("len(neighbor_mesh_neighborlist_" + (channelIndex + 1) + "[frame][i]) == 0");
			script.addScript(PythonScript.setValue("ok", "0"));
			script.addScript(
					PythonScript.setValue("neighbor_mesh_neighborlist_" + (channelIndex + 1) + "[frame][i]", "[i]"));
			script.stopIf();
			script.stopFor();
			script.newLine();
		}

		script.addScript(PythonScript.print(PythonScript.addString("-Plotting neighbor mesh...")));
		script.addScript(PythonScript.setValue("vidstack", PythonScript.callFunction("np.squeeze", "vidstack")));
		script.addScript(PythonScript.print(PythonScript.addString(">") + " + str(frames)"));
		script.addScript(PythonScript.print(PythonScript.addString("!") + " + str(frame + 1)"));
		script.addCommnet("compute neighbor mesh neighbour graphs");
		for (int i = 0; i < ComputeTracksParameters.getNumberSelectedChannels(); i++) {
			int channelIndex = ComputeTracksParameters.getSelectedChannels(i);

			script.addScript(PythonScript.setValue("mesh_frame_networkx_G_" + (channelIndex + 1),
					PythonScript.callFunction("from_neighbor_list_to_graph",
							Arrays.asList(track + "_tracks_" + (channelIndex + 1),
									"neighbor_mesh_neighborlist_" + (channelIndex + 1) + "[frame]", "frame"))));

		}
		script.newLine();

		script.addCommnet("plot neighbor mesh frame");
		script.addScript(PythonScript.callFunctionWithResult(Arrays.asList("fig", "ax"), "plt.subplots",
				Arrays.asList("nrows = 1", "ncols = parameters.get('selected_channels')")));

		if (ComputeTracksParameters.getNumberSelectedChannels() > 1) {
			script.addScript(PythonScript.callFunction("fig.set_size_inches", Arrays.asList(
					"float(columns) / rows * parameters.get('selected_channels') + 0.1", "1", "forward = False")));
			script.addScript(PythonScript.callFunction("plt.subplots_adjust",
					Arrays.asList("left = 0", "right = 1", "bottom = 0", "top = 1", "hspace = 0", "wspace = 0.1")));

			for (int i = 0; i < ComputeTracksParameters.getNumberSelectedChannels(); i++) {
				int channelIndex = ComputeTracksParameters.getSelectedChannels(i);

				if (track.equals("forward"))
					script.addScript(PythonScript.callFunction("ax[" + i + "].imshow",
							Arrays.asList("vidstack[frame]", "alpha = 0.7", "aspect = 'auto'")));
				else
					script.addScript(PythonScript.callFunction("ax[" + i + "].imshow",
							Arrays.asList("vidstack[-(frame + 1)]", "alpha = 0.7", "aspect = 'auto'")));

				script.addScript(PythonScript.callFunction("visualise_mesh",
						Arrays.asList("mesh_frame_networkx_G_" + (channelIndex + 1),
								track + "_tracks_" + (channelIndex + 1) + "[:, frame, [1,0]]", "ax[" + i + "]",
								"node_size = spixel_size / 20", "linewidths = 0.3", "width = 0.3",
								"node_color = " + PythonScript.addString(
										ComputeTracksParameters.getColorOption("neighbor_mesh_colors", i)))));

				script.addScript(PythonScript.callFunction("ax[" + i + "].set_xlim", "[0, columns]"));
				script.addScript(PythonScript.callFunction("ax[" + i + "].set_ylim", "[rows, 0]"));
				script.addScript(PythonScript.callFunction("ax[" + i + "].grid", PythonScript.addString("off")));
				script.addScript(PythonScript.callFunction("ax[" + i + "].axis", PythonScript.addString("off")));
			}
		} else {
			script.addScript(PythonScript.callFunction("fig.set_size_inches", Arrays
					.asList("float(columns) / rows * parameters.get('selected_channels')", "1", "forward = False")));
			script.addScript(PythonScript.callFunction("plt.subplots_adjust",
					Arrays.asList("left = 0", "right = 1", "bottom = 0", "top = 1", "hspace = 0", "wspace = 0")));

			if (track.equals("forward"))
				script.addScript(PythonScript.callFunction("ax.imshow",
						Arrays.asList("vidstack[frame]", "alpha = 0.7", "aspect = 'auto'")));
			else
				script.addScript(PythonScript.callFunction("ax.imshow",
						Arrays.asList("vidstack[-(frame + 1)]", "alpha = 0.7", "aspect = 'auto'")));

			script.addScript(PythonScript.callFunction("visualise_mesh",
					Arrays.asList("mesh_frame_networkx_G_" + (ComputeTracksParameters.getSelectedChannels(0) + 1),
							track + "_tracks_" + (ComputeTracksParameters.getSelectedChannels(0) + 1)
									+ "[:, frame, [1,0]]",
							"ax", "node_size = spixel_size / 20", "linewidths = 0.3", "width = 0.3",
							"node_color = " + PythonScript
									.addString(ComputeTracksParameters.getColorOption("neighbor_mesh_colors", 0)))));

			script.addScript(PythonScript.callFunction("ax.set_xlim", "[0, columns]"));
			script.addScript(PythonScript.callFunction("ax.set_ylim", "[rows, 0]"));
			script.addScript(PythonScript.callFunction("ax.grid", PythonScript.addString("off")));
			script.addScript(PythonScript.callFunction("ax.axis", PythonScript.addString("off")));
		}

		script.addScript(PythonScript.setValue("saveLocation", "parameters.get('saveDirectory2')"));
		script.addScript(PythonScript.callFunction("fig.savefig",
				Arrays.asList(
						PythonScript.callFunction("os.path.join",
								Arrays.asList("saveLocation",
										"parameters.get('fileName') + '_neighbor_mesh_" + track
												+ "_tracks_plot_%s' %(str(frame + 1).zfill(3)) + '.png'")),
						"dpi = rows")));

		script.addScript(PythonScript.callFunction("plt.close", ""));
		script.stopFor();

		script.startIf("ok == 0");
		script.addScript(PythonScript.print(PythonScript
				.addString("+Warning: Some superpixels do not have neighbors at current distance threshold")));
		script.stopIf();
	}

	private void runScript() {
		if (ComputeTracksParameters.isOutput("config_file")) {
			// update config file
			scriptCount++;
			FileWriter configFileWriter;
			try {
				configFileWriter = new FileWriter(configFile, true);
				configFileWriter.write("@Script " + scriptCount + "\r\n" + script.getScript() + "\r\n");
				configFileWriter.flush();
				configFileWriter.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

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

	private void folderPaths() {
		String fileFolderPath = mainFolder.getAbsolutePath() + "/" + Globals.getNameWithoutExtension(currentFilePath);
		fileFolder = new File(fileFolderPath);
		fileFolder.mkdirs();

		String matlabFolderPath = fileFolderPath + "/" + "matlab_files";
		matlabFolder = new File(matlabFolderPath);
		matlabFolder.mkdirs();

		String imageFolderPath = fileFolderPath + "/" + "images";
		imageFolder = new File(imageFolderPath);
		imageFolder.mkdirs();

		if (ComputeTracksParameters.isOutput("forward_tracks_visualisation")) {
			String forwardTracksImageSequenceFolderPath = imageFolderPath + "/"
					+ Globals.getNameWithoutExtension(currentFilePath) + "_forward_tracks_image_sequence";
			forwardTracksImageSequenceFolder = new File(forwardTracksImageSequenceFolderPath);
			forwardTracksImageSequenceFolder.mkdirs();
		}

		if (ComputeTracksParameters.isOutput("backward_tracks_visualisation")) {
			String backwardTracksImageSequenceFolderPath = imageFolderPath + "/"
					+ Globals.getNameWithoutExtension(currentFilePath) + "_backward_tracks_image_sequence";
			backwardTracksImageSequenceFolder = new File(backwardTracksImageSequenceFolderPath);
			backwardTracksImageSequenceFolder.mkdirs();
		}

		if (ComputeTracksParameters.isOutput("motion_field")
				&& ComputeTracksParameters.getSaveOption("motion_field_save_options", ".tif")) {
			String motionFieldImageSequenceFolderPath = imageFolderPath + "/"
					+ Globals.getNameWithoutExtension(currentFilePath) + "_motion_field_image_sequence";
			motionFieldImageSequenceFolder = new File(motionFieldImageSequenceFolderPath);
			motionFieldImageSequenceFolder.mkdirs();
		}

		if (ComputeTracksParameters.isOutput("MOSES_mesh_complete_visualisation")) {
			String track;
			if (ComputeTracksParameters.getMOSESMeshForwardTracks())
				track = "forward";
			else
				track = "backward";

			String MOSESMeshImageSequenceFolderPath = imageFolderPath + "/"
					+ Globals.getNameWithoutExtension(currentFilePath) + "_" + track
					+ "_tracks_MOSES_mesh_image_sequence";
			MOSESMeshImageSequenceFolder = new File(MOSESMeshImageSequenceFolderPath);
			MOSESMeshImageSequenceFolder.mkdirs();
		}

		if (ComputeTracksParameters.isOutput("radial_mesh_complete_visualisation")) {
			String track;
			if (ComputeTracksParameters.getRadialMeshForwardTracks())
				track = "forward";
			else
				track = "backward";

			String radialMeshImageSequenceFolderPath = imageFolderPath + "/"
					+ Globals.getNameWithoutExtension(currentFilePath) + "_" + track
					+ "_tracks_radial_mesh_image_sequence";
			radialMeshImageSequenceFolder = new File(radialMeshImageSequenceFolderPath);
			radialMeshImageSequenceFolder.mkdirs();
		}

		if (ComputeTracksParameters.isOutput("neighbor_mesh_complete_visualisation")) {
			String track;
			if (ComputeTracksParameters.getNeighborMeshForwardTracks())
				track = "forward";
			else
				track = "backward";

			String neighborMeshImageSequenceFolderPath = imageFolderPath + "/"
					+ Globals.getNameWithoutExtension(currentFilePath) + "_" + track
					+ "_tracks_neighbor_mesh_image_sequence";
			neighborMeshImageSequenceFolder = new File(neighborMeshImageSequenceFolderPath);
			neighborMeshImageSequenceFolder.mkdirs();
		}
	}

	private void analyseFile() {
		// create new file
		temporaryFilePath = System.getProperty("java.io.tmpdir") + "MOSESimage.tif";

		ImagePlus temporaryImage = IJ.openImage(currentFilePath);
		IJ.run(temporaryImage, "Size...",
				"width=" + (int) (temporaryImage.getWidth() / Math.sqrt(ComputeTracksParameters.getDownsizeFactor()))
						+ " height="
						+ (int) (temporaryImage.getHeight() / Math.sqrt(ComputeTracksParameters.getDownsizeFactor()))
						+ " depth=" + ComputeTracksParameters.getFrames()
						+ " constrain average interpolation=Bilinear");
		IJ.saveAs(temporaryImage, "Tiff", temporaryFilePath);
		temporaryImage.close();

		folderPaths();

		// config file

		if (ComputeTracksParameters.isOutput("config_file") && !this.isCancelled())
			initialiseConfigFile();

		// forward tracks

		if (ComputeTracksParameters.isOutput("forward_tracks") && !this.isCancelled()) {
			script = new PythonScript("Forward tracks script");
			generateForwardTracks();

			if (ComputeTracksParameters.isOutput("forward_tracks_visualisation") && !this.isCancelled())
				visualiseForwardTracks();

			runScript();

			if (ComputeTracksParameters.isOutput("forward_tracks_visualisation") && !this.isCancelled())
				executeSaveOption("forward_tracks_save_options", forwardTracksImageSequenceFolder, "forward_tracks");
		}

		// backward tracks

		if (ComputeTracksParameters.isOutput("backward_tracks") && !this.isCancelled()) {
			script = new PythonScript("Backward tracks script");
			generateBackwardTracks();

			if (ComputeTracksParameters.isOutput("backward_tracks_visualisation") && !this.isCancelled())
				visualiseBackwardTracks();

			runScript();

			if (ComputeTracksParameters.isOutput("backward_tracks_visualisation") && !this.isCancelled())
				executeSaveOption("backward_tracks_save_options", backwardTracksImageSequenceFolder, "backward_tracks");
		}

		// motion flow

		if (ComputeTracksParameters.isOutput("motion_field") && !this.isCancelled()) {
			script = new PythonScript("Motion field script");
			generateMotionField();

			runScript();

			if (ComputeTracksParameters.getSaveOption("motion_field_save_options", ".tif") && !this.isCancelled()) {
				publish("-Generating tiff stack...");
				Thread.yield();

				ImagePlus imp = FolderOpener.open(motionFieldImageSequenceFolder.getAbsolutePath(), "");
				imp.show();
				IJ.saveAs(imp, "Tiff",
						imageFolder + "/" + Globals.getNameWithoutExtension(currentFilePath) + "_motion_field.tif");

				publish("-Deleting temporary files...");
				Thread.yield();

				String[] entries = motionFieldImageSequenceFolder.list();
				for (String fileName : entries) {
					File currentFile = new File(motionFieldImageSequenceFolder.getPath(), fileName);
					currentFile.delete();
				}
				motionFieldImageSequenceFolder.delete();
			}
		}

		// MOSES mesh

		if (ComputeTracksParameters.isOutput("MOSES_mesh") && !this.isCancelled()) {
			script = new PythonScript("MOSES mesh script");
			generateMOSESMesh();

			if (ComputeTracksParameters.isOutput("MOSES_mesh_visualisation") && !this.isCancelled()) {
				if (ComputeTracksParameters.isOutput("MOSES_mesh_frame_visualisation") && !this.isCancelled())
					generateMOSESMeshFrameVisualisation();
				if (ComputeTracksParameters.isOutput("MOSES_mesh_complete_visualisation") && !this.isCancelled())
					generateMOSESMeshCompleteVisualisation();
			}

			runScript();

			if (ComputeTracksParameters.isOutput("MOSES_mesh_complete_visualisation") && !this.isCancelled()) {
				if (ComputeTracksParameters.getMOSESMeshForwardTracks())
					executeSaveOption("MOSES_mesh_complete_visualisation_save_options", MOSESMeshImageSequenceFolder,
							"forward_tracks_MOSES_mesh");
				else
					executeSaveOption("MOSES_mesh_complete_visualisation_save_options", MOSESMeshImageSequenceFolder,
							"backward_tracks_MOSES_mesh");
			}

		}

		// radial mesh

		if (ComputeTracksParameters.isOutput("radial_mesh") && !this.isCancelled()) {
			script = new PythonScript("Radial mesh script");
			generateRadialMesh();

			if (ComputeTracksParameters.isOutput("radial_mesh_visualisation") && !this.isCancelled()) {
				if (ComputeTracksParameters.isOutput("radial_mesh_frame_visualisation") && !this.isCancelled())
					generateRadialMeshFrameVisualisation();
				if (ComputeTracksParameters.isOutput("radial_mesh_complete_visualisation") && !this.isCancelled())
					generateRadialMeshCompleteVisualisation();
			}

			runScript();

			if (ComputeTracksParameters.isOutput("radial_mesh_complete_visualisation") && !this.isCancelled()) {
				if (ComputeTracksParameters.getRadialMeshForwardTracks())
					executeSaveOption("radial_mesh_complete_visualisation_save_options", radialMeshImageSequenceFolder,
							"forward_tracks_radial_mesh");
				else
					executeSaveOption("radial_mesh_complete_visualisation_save_options", radialMeshImageSequenceFolder,
							"radial_tracks_radial_mesh");
			}
		}

		// neighbor mesh

		if (ComputeTracksParameters.isOutput("neighbor_mesh") && !this.isCancelled()) {
			script = new PythonScript("K neighbor mesh script");
			generateNeighborMesh();

			if (ComputeTracksParameters.isOutput("neighbor_mesh_visualisation") && !this.isCancelled()) {
				if (ComputeTracksParameters.isOutput("neighbor_mesh_frame_visualisation") && !this.isCancelled())
					generateNeighborMeshFrameVisualisation();
				if (ComputeTracksParameters.isOutput("neighbor_mesh_complete_visualisation") && !this.isCancelled())
					generateNeighborMeshCompleteVisualisation();
			}

			runScript();

			if (ComputeTracksParameters.isOutput("neighbor_mesh_complete_visualisation") && !this.isCancelled()) {
				if (ComputeTracksParameters.getNeighborMeshForwardTracks())
					executeSaveOption("neighbor_mesh_complete_visualisation_save_options",
							neighborMeshImageSequenceFolder, "forward_tracks_neighbor_mesh");
				else
					executeSaveOption("neighbor_mesh_complete_visualisation_save_options",
							neighborMeshImageSequenceFolder, "radial_tracks_neighbor_mesh");
			}
		}
	}

	private void executeSaveOption(String saveOptionName, File folder, String outputName) {
		// save .tif
		if (ComputeTracksParameters.getSaveOption(saveOptionName, ".tif")) {
			publish("-Generating tiff stack...");
			Thread.yield();

			ImagePlus imp = FolderOpener.open(folder.getAbsolutePath(), "");
			imp.show();
			IJ.saveAs(imp, "Tiff",
					imageFolder + "/" + Globals.getNameWithoutExtension(currentFilePath) + "_" + outputName + ".tif");
		}

		// save .avi
		if (ComputeTracksParameters.getSaveOption(saveOptionName, ".avi")) {
			publish("-Generating avi video...");
			Thread.yield();

			ImagePlus imp = FolderOpener.open(folder.getAbsolutePath(), "");
			IJ.run(imp, "AVI... ", "compression=JPEG frame=7 save=" + imageFolder + "/"
					+ Globals.getNameWithoutExtension(currentFilePath) + "_" + outputName + ".avi");
		}

		// delete image sequence folder
		if (!ComputeTracksParameters.getSaveOption(saveOptionName, ".png")) {
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

			if (validMessage) {
				progress.setMessage("<html> Please wait, this may take a couple of minutes. <br> " + m + "  </html>");
				progress.setFileCount(fileCount);
				progress.setFileNumber(fileNumber);
			}
		}
	}

	@Override
	protected void done() {
		progress.setVisibility(false);
	}

}