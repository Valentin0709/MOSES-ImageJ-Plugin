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

public class VisualisationFromMask extends SwingWorker<String, String> {
	private ProgressPanel progress;
	private int fileNumber;
	private PythonScript script;
	private Process process;
	private String annotationPath, trackPath, projectName, resizedImagePath, timestamp;
	private MatlabMetadata trackMetadata;
	private File imageSequenceFolder, saveFolder, longestTracksCoordinatesCSVFile;

	public VisualisationFromMask(ProgressPanel p) {
		progress = p;
	}

	private void recordHistory() {
		File commandsHistoryFile = new File(VisualisationFromMaskParameters.getWorkspace() + "/workspace_history.csv");

		Globals.writeCSV(commandsHistoryFile, Arrays.asList(""));
		Globals.writeCSV(commandsHistoryFile, Arrays.asList("Action:", "custom visualisation"));
		Globals.writeCSV(commandsHistoryFile, Arrays.asList("Timestamp:", timestamp));

		Globals.writeCSV(commandsHistoryFile, Arrays.asList("Motion tracks:"));
		Globals.writeCSV(commandsHistoryFile,
				Arrays.asList(String.join(", ", VisualisationFromMaskParameters.getTracksPaths())));

		Globals.writeCSV(commandsHistoryFile, Arrays.asList("Images:"));
		Globals.writeCSV(commandsHistoryFile,
				Arrays.asList(String.join(", ", VisualisationFromMaskParameters.getImagePaths())));

		Globals.writeCSV(commandsHistoryFile, Arrays.asList("Annotation files:"));
		Globals.writeCSV(commandsHistoryFile,
				Arrays.asList(String.join(", ", VisualisationFromMaskParameters.getAnnotationPaths())));

		Globals.writeCSV(commandsHistoryFile, Arrays.asList("Outputs:"));
		Globals.writeCSV(commandsHistoryFile,
				Arrays.asList(String.join(", ", VisualisationFromMaskParameters.getOutputList())));

		Globals.writeCSV(commandsHistoryFile, Arrays.asList("Parameters:"));
		Globals.writeCSV(commandsHistoryFile,
				Arrays.asList(String.join(", ", VisualisationFromMaskParameters.getParametersList())));
	}

	private void addScriptHeader() {
		script.importModule("sys");
		script.importModule("os");
		script.importModule("math");
		script.importModule("csv");
		script.importModule("pylab", "plt");
		script.importModule("scipy.io", "spio");
		script.importModule("numpy", "np");
		script.importModuleFrom("io", "skimage");
		script.importModule("matplotlib.path", "mplPath");
		script.importModule("matplotlib.patches", "patches");
		script.importModule("seaborn", "sns");
		script.importModule("matplotlib.patheffects", "PathEffects");
		script.importModuleFrom("read_multiimg_PIL", "MOSES.Utility_Functions.file_io");
		script.importModuleFrom("plot_tracks", "MOSES.Visualisation_Tools.track_plotting");
	}

	public void createVisualisation() {
		List<Parameter> parameters = new ArrayList<Parameter>();
		parameters.addAll(Arrays.asList(new Parameter("imagePath", "str", resizedImagePath),
				new Parameter("annotationPath", "str", annotationPath), new Parameter("trackPath", "str", trackPath),
				new Parameter("fileName", "str", projectName),
				new Parameter("saveDirectory", "str", imageSequenceFolder),
				new Parameter("temporal_segment_length", "int",
						VisualisationFromMaskParameters.getTracksTemporalSegment()),
				new Parameter("colorPalette", "str", VisualisationFromMaskParameters.getColorPalette()),
				new Parameter("fontSize", "int", VisualisationFromMaskParameters.getFontSize()),
				new Parameter("downsizeFactor", "float", Math.sqrt(trackMetadata.getDownsizeFactor()))));

		addScriptHeader();

		script.addScript(PythonScript.print(PythonScript.addString("-Plotting tracks for complete visualisation...")));
		script.newLine();

		script.addCommnet("define parameters dictionary");
		script.addParameter(parameters);
		script.addScript(script.createParameterDictionary());
		script.newLine();

		script.addCommnet("import tiff stack");
		script.addScript(PythonScript.setValue("vidstack",
				PythonScript.callFunction("read_multiimg_PIL", "parameters.get('imagePath')")));
		script.startIf("len(vidstack.shape) == 3");
		script.addScript(PythonScript.setValue("vidstack", "vidstack[..., None]"));
		script.addScript(PythonScript.callFunction("plt.set_cmap", PythonScript.addString("gray")));
		script.stopIf();
		script.addScript(
				PythonScript.setValue(Arrays.asList("frames", "rows", "columns", "channels"), "vidstack.shape"));
		script.newLine();

		script.addScript(
				PythonScript.setValue("readAnnotation",
						PythonScript.callFunction("list",
								PythonScript.callFunction("csv.reader",
										Arrays.asList(
												PythonScript.callFunction("open", "parameters.get('annotationPath')"),
												"delimiter = ','")))));
		script.addScript(PythonScript.callFunction("readAnnotation.reverse", ""));

		script.addScript(PythonScript.setValue("annotationFileMetadata", "readAnnotation.pop()"));
		script.addScript(PythonScript.setValue("annotationCount", "int(annotationFileMetadata[1])"));

		script.addScript(PythonScript.setValue("polygonNames", "[]"));
		script.addScript(PythonScript.setValue("polygonPaths", "[]"));

		script.startFor("i", "annotationCount");
		script.addScript(PythonScript.setValue("row", "readAnnotation.pop()"));
		script.addScript(PythonScript.setValue("polygonName", "row[0]"));
		script.addScript(PythonScript.setValue("vertexCount", "int(row[1])"));
		script.addScript(PythonScript.setValue("vertexList", "[]"));

		script.startFor("j", "vertexCount");
		script.addScript(PythonScript.setValue("row", "readAnnotation.pop()"));
		script.addScript(PythonScript.callFunction("vertexList.append",
				"[float(int(row[0]) / parameters.get('downsizeFactor')), float(int(row[1]) / parameters.get('downsizeFactor'))]"));
		script.stopFor();

		script.addScript(PythonScript.callFunction("polygonNames.append", "polygonName"));
		script.addScript(PythonScript.callFunction("polygonPaths.append", "mplPath.Path(np.array(vertexList))"));
		script.stopFor();

		script.addScript(PythonScript.setValue("trackFile",
				PythonScript.callFunction("spio.loadmat", "parameters.get('trackPath')")));

		List<Integer> channels = trackMetadata.getChannels();
		for (int i = 0; i < channels.size(); i++) {
			int channelIndex = channels.get(i) + 1;

			script.addScript(PythonScript.setValue("tracks_" + channelIndex,
					"trackFile['" + trackMetadata.getTrackType() + "_tracks_" + channelIndex + "']"));

			script.addScript(
					PythonScript.setValue("plot_select_ids_" + channelIndex, "[[] for i in range(len(polygonPaths))]"));
			script.startFor("i", "tracks_" + channelIndex + ".shape[0]");
			script.startFor("j", "len(polygonPaths)");

			script.startIf("polygonPaths[j].contains_point((tracks_" + channelIndex + "[i, 0, 1], tracks_"
					+ channelIndex + "[i, 0, 0]))");
			script.addScript("plot_select_ids_" + channelIndex + "[j].append(i)");
			script.stopIf();

			script.stopFor();
			script.stopFor();
		}
		script.newLine();

		script.addCommnet("plot forward tracks");
		script.addScript(PythonScript.setValue("vidstack", PythonScript.callFunction("np.squeeze", "vidstack")));
		script.addScript(PythonScript.setValue("saveLocation", "parameters.get('saveDirectory')"));
		script.addScript("colors = sns.color_palette(parameters.get('colorPalette') ,n_colors = len(polygonPaths))");

		script.addScript(PythonScript.setValue("len_segment", "parameters.get('temporal_segment_length')"));
		script.addScript(PythonScript.print(PythonScript.addString(">") + " + str(frames - len_segment)"));
		script.startFor("frame", "len_segment", "frames", "1");
		script.addScript(PythonScript.print(PythonScript.addString("!") + " + str(frame - len_segment + 1)"));

		if (trackMetadata.getTrackType().equals("forward"))
			script.addScript(PythonScript.setValue("frame_img", "vidstack[frame]"));
		else
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

		script.startFor("i", "len(polygonPaths)");
		for (int i = 0; i < channels.size(); i++) {
			int channelIndex = channels.get(i) + 1;

			script.addScript(PythonScript.callFunction("plot_tracks", Arrays.asList(
					"tracks_" + channelIndex + "[plot_select_ids_" + channelIndex + "[i], frame-len_segment:frame+1]",
					"ax", "color = colors[i]", "lw = 1")));

			if (VisualisationFromMaskParameters.getLabelVisibility()) {
				script.addScript(PythonScript.setValue("text", "ax.text(np.sum(tracks_" + channelIndex
						+ "[plot_select_ids_" + channelIndex + "[i], frame-len_segment, 1]) / len(tracks_"
						+ channelIndex + "[plot_select_ids_" + channelIndex + "[i], frame-len_segment]), np.sum(tracks_"
						+ channelIndex + "[plot_select_ids_" + channelIndex
						+ "[i], frame-len_segment, 0]) / len(tracks_" + channelIndex + "[plot_select_ids_"
						+ channelIndex
						+ "[i], frame-len_segment]), polygonNames[i], color = colors[i], fontsize = float(parameters.get('fontSize')) * 100 / rows)"));
				script.addScript("text.set_path_effects([PathEffects.withStroke(linewidth= 1, foreground='w')])");
			}
		}
		script.stopFor();
		script.newLine();

		script.addScript(PythonScript.callFunction("fig.savefig",
				Arrays.asList(PythonScript.callFunction("os.path.join", Arrays.asList("saveLocation",
						"parameters.get('fileName') + '_motion_track_plot_%s' %(str(frame - len_segment + 1).zfill(3)) + '.png'")),
						"dpi = rows")));
		script.addScript(PythonScript.callFunction("plt.close", "fig"));
		script.stopFor();

	}

	public void longestTracksVisualisation() {
		List<Parameter> parameters = new ArrayList<Parameter>();
		parameters.addAll(Arrays.asList(new Parameter("imagePath", "str", resizedImagePath),
				new Parameter("annotationPath", "str", annotationPath), new Parameter("trackPath", "str", trackPath),
				new Parameter("fileName", "str", projectName), new Parameter("saveDirectory", "str", saveFolder),
				new Parameter("colorPalette", "str", VisualisationFromMaskParameters.getColorPalette()),
				new Parameter("fontSize", "int", VisualisationFromMaskParameters.getFontSize()),
				new Parameter("downsizeFactor", "float", Math.sqrt(trackMetadata.getDownsizeFactor()))));

		List<Integer> channels = trackMetadata.getChannels();

		addScriptHeader();

		script.addScript(
				PythonScript.print(PythonScript.addString("-Plotting longest track for each region of interest...")));
		script.newLine();

		script.addCommnet("define parameters dictionary");
		script.addParameter(parameters);
		script.addScript(script.createParameterDictionary());
		script.newLine();

		script.addCommnet("import tiff stack");
		script.addScript(PythonScript.setValue("vidstack",
				PythonScript.callFunction("read_multiimg_PIL", "parameters.get('imagePath')")));
		script.startIf("len(vidstack.shape) == 3");
		script.addScript(PythonScript.setValue("vidstack", "vidstack[..., None]"));
		script.addScript(PythonScript.callFunction("plt.set_cmap", PythonScript.addString("gray")));
		script.stopIf();
		script.addScript(
				PythonScript.setValue(Arrays.asList("frames", "rows", "columns", "channels"), "vidstack.shape"));
		script.newLine();

		script.addScript(
				PythonScript.setValue("readAnnotation",
						PythonScript.callFunction("list",
								PythonScript.callFunction("csv.reader",
										Arrays.asList(
												PythonScript.callFunction("open", "parameters.get('annotationPath')"),
												"delimiter = ','")))));
		script.addScript(PythonScript.callFunction("readAnnotation.reverse", ""));

		script.addScript(PythonScript.setValue("annotationFileMetadata", "readAnnotation.pop()"));
		script.addScript(PythonScript.setValue("annotationCount", "int(annotationFileMetadata[1])"));

		script.addScript(PythonScript.setValue("polygonNames", "[]"));
		script.addScript(PythonScript.setValue("polygonPaths", "[]"));

		script.startFor("i", "annotationCount");
		script.addScript(PythonScript.setValue("row", "readAnnotation.pop()"));
		script.addScript(PythonScript.setValue("polygonName", "row[0]"));
		script.addScript(PythonScript.setValue("vertexCount", "int(row[1])"));
		script.addScript(PythonScript.setValue("vertexList", "[]"));

		script.startFor("j", "vertexCount");
		script.addScript(PythonScript.setValue("row", "readAnnotation.pop()"));
		script.addScript(PythonScript.callFunction("vertexList.append",
				"[float(int(row[0]) / parameters.get('downsizeFactor')), float(int(row[1]) / parameters.get('downsizeFactor'))]"));
		script.stopFor();

		script.addScript(PythonScript.callFunction("polygonNames.append", "polygonName"));
		script.addScript(PythonScript.callFunction("polygonPaths.append", "mplPath.Path(np.array(vertexList))"));
		script.stopFor();

		script.addScript(PythonScript.setValue("trackFile",
				PythonScript.callFunction("spio.loadmat", "parameters.get('trackPath')")));

		for (int i = 0; i < channels.size(); i++) {
			int channelIndex = channels.get(i) + 1;

			script.addScript(PythonScript.setValue("tracks_" + channelIndex,
					"trackFile['" + trackMetadata.getTrackType() + "_tracks_" + channelIndex + "']"));

			script.addScript(PythonScript.setValue("plot_select_ids_" + channelIndex,
					"np.zeros(len(polygonPaths),  dtype=int)"));
			script.addScript(PythonScript.setValue("max_values_" + channelIndex, "np.zeros(len(polygonPaths))"));

			script.startFor("i", "tracks_" + channelIndex + ".shape[0]");
			script.startFor("j", "len(polygonPaths)");

			script.startIf("polygonPaths[j].contains_point((tracks_" + channelIndex + "[i, 0, 1], tracks_"
					+ channelIndex + "[i, 0, 0]))");
			script.addScript(PythonScript.setValue("d",
					PythonScript.callFunction("math.sqrt",
							"(tracks_" + channelIndex + "[i, 0, 0] - tracks_" + channelIndex
									+ "[i, -1, 0])**2 + (tracks_" + channelIndex + "[i, 0, 1] - tracks_" + channelIndex
									+ "[i, -1, 1])**2")));
			script.startIf("d > max_values_" + channelIndex + "[j]");
			script.addScript("max_values_" + channelIndex + "[j] = d");
			script.addScript("plot_select_ids_" + channelIndex + "[j] = i");
			script.stopIf();
			script.stopIf();
			script.stopFor();
			script.stopFor();
		}
		script.newLine();

		script.addCommnet("plot forward tracks");
		script.addScript(PythonScript.setValue("vidstack", PythonScript.callFunction("np.squeeze", "vidstack")));
		script.addScript(PythonScript.setValue("saveLocation", "parameters.get('saveDirectory')"));

		if (trackMetadata.getTrackType().equals("forward"))
			script.addScript(PythonScript.setValue("frame_img", "vidstack[0]"));
		else
			script.addScript(PythonScript.setValue("frame_img", "vidstack[-1]"));

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

		script.addScript("colors = sns.color_palette(parameters.get('colorPalette') ,n_colors = len(polygonPaths))");

		script.startFor("i", "len(polygonPaths)");
		for (int i = 0; i < channels.size(); i++) {
			int channelIndex = channels.get(i) + 1;

			script.addScript(PythonScript.setValue("plot_select_ids_" + channelIndex,
					PythonScript.callFunction("np.vstack", "plot_select_ids_" + channelIndex)));

			script.addScript(PythonScript.callFunction("plot_tracks",
					Arrays.asList("tracks_" + channelIndex + "[plot_select_ids_" + channelIndex + "[i], :]", "ax",
							"color = colors[i]", "lw = 1")));

			if (VisualisationFromMaskParameters.getAnnotationVisibility()) {
				script.addScript(PythonScript.setValue("patch", PythonScript.callFunction("patches.PathPatch",
						Arrays.asList("polygonPaths[i]", "facecolor = colors[i]", "lw = 0", "alpha = 0.4"))));
				script.addScript("ax.add_patch(patch)");
			}

			if (VisualisationFromMaskParameters.getLabelVisibility()) {
				script.addScript(PythonScript.setValue("text",
						"ax.text(np.sum(polygonPaths[i].vertices[:, 0]) / len(polygonPaths[i].vertices), np.sum(polygonPaths[i].vertices[:, 1]) / len(polygonPaths[i].vertices), polygonNames[i], color = colors[i], fontsize = float(parameters.get('fontSize')) * 100 / rows)"));
				script.addScript("text.set_path_effects([PathEffects.withStroke(linewidth= 1, foreground='w')])");
			}

			if (VisualisationFromMaskParameters.isOutput("longest_track_csv")) {
				script.addScript(
						PythonScript.print("\".\" + polygonNames[i], \",\", *polygonPaths[i].vertices[:, 0:2]"));
			}

		}
		script.stopFor();
		script.newLine();

		script.addScript(
				PythonScript.callFunction("fig.savefig",
						Arrays.asList(
								PythonScript.callFunction("os.path.join",
										Arrays.asList("saveLocation",
												"parameters.get('fileName') + '_longest_track_plot.png'")),
								"dpi = rows")));
		script.addScript(PythonScript.callFunction("plt.close", "fig"));
	}

	public void allTracksVisualisation() {
		List<Parameter> parameters = new ArrayList<Parameter>();
		parameters.addAll(Arrays.asList(new Parameter("imagePath", "str", resizedImagePath),
				new Parameter("annotationPath", "str", annotationPath), new Parameter("trackPath", "str", trackPath),
				new Parameter("fileName", "str", projectName), new Parameter("saveDirectory", "str", saveFolder),
				new Parameter("colorPalette", "str", VisualisationFromMaskParameters.getColorPalette()),
				new Parameter("fontSize", "int", VisualisationFromMaskParameters.getFontSize()),
				new Parameter("downsizeFactor", "float", Math.sqrt(trackMetadata.getDownsizeFactor()))));

		List<Integer> channels = trackMetadata.getChannels();

		addScriptHeader();

		script.addScript(PythonScript.print(PythonScript.addString("-Plotting tracks for each region of interest...")));
		script.newLine();

		script.addCommnet("define parameters dictionary");
		script.addParameter(parameters);
		script.addScript(script.createParameterDictionary());
		script.newLine();

		script.addCommnet("import tiff stack");
		script.addScript(PythonScript.setValue("vidstack",
				PythonScript.callFunction("read_multiimg_PIL", "parameters.get('imagePath')")));
		script.startIf("len(vidstack.shape) == 3");
		script.addScript(PythonScript.setValue("vidstack", "vidstack[..., None]"));
		script.addScript(PythonScript.callFunction("plt.set_cmap", PythonScript.addString("gray")));
		script.stopIf();
		script.addScript(
				PythonScript.setValue(Arrays.asList("frames", "rows", "columns", "channels"), "vidstack.shape"));
		script.newLine();

		script.addScript(
				PythonScript.setValue("readAnnotation",
						PythonScript.callFunction("list",
								PythonScript.callFunction("csv.reader",
										Arrays.asList(
												PythonScript.callFunction("open", "parameters.get('annotationPath')"),
												"delimiter = ','")))));
		script.addScript(PythonScript.callFunction("readAnnotation.reverse", ""));

		script.addScript(PythonScript.setValue("annotationFileMetadata", "readAnnotation.pop()"));
		script.addScript(PythonScript.setValue("annotationCount", "int(annotationFileMetadata[1])"));

		script.addScript(PythonScript.setValue("polygonNames", "[]"));
		script.addScript(PythonScript.setValue("polygonPaths", "[]"));

		script.startFor("i", "annotationCount");
		script.addScript(PythonScript.setValue("row", "readAnnotation.pop()"));
		script.addScript(PythonScript.setValue("polygonName", "row[0]"));
		script.addScript(PythonScript.setValue("vertexCount", "int(row[1])"));
		script.addScript(PythonScript.setValue("vertexList", "[]"));

		script.startFor("j", "vertexCount");
		script.addScript(PythonScript.setValue("row", "readAnnotation.pop()"));
		script.addScript(PythonScript.callFunction("vertexList.append",
				"[float(int(row[0]) / parameters.get('downsizeFactor')), float(int(row[1]) / parameters.get('downsizeFactor'))]"));
		script.stopFor();

		script.addScript(PythonScript.callFunction("polygonNames.append", "polygonName"));
		script.addScript(PythonScript.callFunction("polygonPaths.append", "mplPath.Path(np.array(vertexList))"));
		script.stopFor();

		script.addScript(PythonScript.setValue("trackFile",
				PythonScript.callFunction("spio.loadmat", "parameters.get('trackPath')")));

		for (int i = 0; i < channels.size(); i++) {
			int channelIndex = channels.get(i) + 1;

			script.addScript(PythonScript.setValue("tracks_" + channelIndex,
					"trackFile['" + trackMetadata.getTrackType() + "_tracks_" + channelIndex + "']"));

			script.addScript(
					PythonScript.setValue("plot_select_ids_" + channelIndex, "[[] for i in range(len(polygonPaths))]"));

			script.startFor("i", "tracks_" + channelIndex + ".shape[0]");
			script.startFor("j", "len(polygonPaths)");

			script.startIf("polygonPaths[j].contains_point((tracks_" + channelIndex + "[i, 0, 1], tracks_"
					+ channelIndex + "[i, 0, 0]))");
			script.addScript("plot_select_ids_" + channelIndex + "[j].append(i)");
			script.stopIf();
			script.stopFor();
			script.stopFor();
		}
		script.newLine();

		script.addCommnet("plot forward tracks");
		script.addScript(PythonScript.setValue("vidstack", PythonScript.callFunction("np.squeeze", "vidstack")));
		script.addScript(PythonScript.setValue("saveLocation", "parameters.get('saveDirectory')"));

		if (trackMetadata.getTrackType().equals("forward"))
			script.addScript(PythonScript.setValue("frame_img", "vidstack[0]"));
		else
			script.addScript(PythonScript.setValue("frame_img", "vidstack[-1]"));

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

		script.addScript("colors = sns.color_palette(parameters.get('colorPalette') ,n_colors = len(polygonPaths))");

		script.startFor("i", "len(polygonPaths)");
		for (int i = 0; i < channels.size(); i++) {
			int channelIndex = channels.get(i) + 1;

			script.addScript(PythonScript.callFunction("plot_tracks",
					Arrays.asList("tracks_" + channelIndex + "[plot_select_ids_" + channelIndex + "[i], :]", "ax",
							"color = colors[i]", "lw = 1", "alpha = 0.8")));

			if (VisualisationFromMaskParameters.getAnnotationVisibility()) {
				script.addScript(PythonScript.setValue("patch", PythonScript.callFunction("patches.PathPatch",
						Arrays.asList("polygonPaths[i]", "facecolor = colors[i]", "lw = 0", "alpha = 0.4"))));
				script.addScript("ax.add_patch(patch)");
			}

			if (VisualisationFromMaskParameters.getLabelVisibility()) {
				script.addScript(PythonScript.setValue("text",
						"ax.text(np.sum(polygonPaths[i].vertices[:, 0]) / len(polygonPaths[i].vertices), np.sum(polygonPaths[i].vertices[:, 1]) / len(polygonPaths[i].vertices), polygonNames[i], color = colors[i], fontsize = float(parameters.get('fontSize')) * 100 / rows)"));
				script.addScript("text.set_path_effects([PathEffects.withStroke(linewidth= 1, foreground='w')])");
			}

		}
		script.stopFor();
		script.newLine();

		script.addScript(
				PythonScript
						.callFunction("fig.savefig",
								Arrays.asList(
										PythonScript.callFunction("os.path.join",
												Arrays.asList("saveLocation",
														"parameters.get('fileName') + '_all_track_plot.png'")),
										"dpi = rows")));
		script.addScript(PythonScript.callFunction("plt.close", "fig"));
	}

	public void folderPaths() {
		saveFolder = new File(VisualisationFromMaskParameters.getWorkspace() + "/" + projectName
				+ "/data_analysis/images/" + timestamp);
		saveFolder.mkdirs();

		if (VisualisationFromMaskParameters.isOutput("complete_visualisation")) {
			String imageSequenceFolderPath = saveFolder.getAbsolutePath() + "/" + projectName
					+ "_tracks_visualisation_image_sequence";
			imageSequenceFolder = new File(imageSequenceFolderPath);
			imageSequenceFolder.mkdirs();
		}

		if (VisualisationFromMaskParameters.isOutput("longest_track_csv")) {
			File saveFolder2 = new File(VisualisationFromMaskParameters.getWorkspace() + "/" + projectName
					+ "/data_analysis/CSV_files/" + timestamp);
			saveFolder2.mkdirs();

			String longestTracksCSVFilePath = saveFolder2.getAbsolutePath() + "/" + projectName
					+ "_longest_tracks_coordinates.csv";
			longestTracksCoordinatesCSVFile = new File(longestTracksCSVFilePath);
			try {
				longestTracksCoordinatesCSVFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	@Override
	protected String doInBackground() throws Exception {
		timestamp = Globals.getFormattedDate();
		VisualisationFromMaskParameters.trimFiles();
		recordHistory();

		fileNumber = 1;
		for (ProjectImageAnnotationTracks imt : VisualisationFromMaskParameters.getFiles()) {
			String imagePath = imt.getImagePath();
			annotationPath = imt.getAnnotationPath();
			trackPath = imt.getTrackPath();
			projectName = imt.getProjectName();

			if (imagePath != null && trackPath != null && annotationPath != null) {
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

				if (VisualisationFromMaskParameters.isOutput("complete_visualisation") && !this.isCancelled()) {
					script = new PythonScript("Complete tracks visualisation");
					createVisualisation();
					runScript();

					executeSaveOption(imageSequenceFolder, saveFolder, "tracks_visualisation");
				}

				if (VisualisationFromMaskParameters.isOutput("longest_track") && !this.isCancelled()) {
					script = new PythonScript("Longest tracks visualisation");
					longestTracksVisualisation();
					runScript();
				}

				if (VisualisationFromMaskParameters.isOutput("all_tracks") && !this.isCancelled()) {
					script = new PythonScript("All tracks visualisation");
					allTracksVisualisation();
					runScript();
				}

				if (VisualisationFromMaskParameters.isOutput("all_tracks") && !this.isCancelled()) {
					script = new PythonScript("All tracks visualisation");
					allTracksVisualisation();
					runScript();
				}

			}

			fileNumber++;
		}

		return "Done.";
	}

	private void executeSaveOption(File imageFolder, File saveFolder, String outputName) {
		// save .tif
		if (VisualisationFromMaskParameters.getSaveOption(".tif")) {
			publish("-Generating tiff stack...");
			Thread.yield();

			ImagePlus imp = FolderOpener.open(imageFolder.getAbsolutePath(), "");
			IJ.saveAs(imp, "Tiff", saveFolder + "/" + projectName + "_" + outputName + ".tif");
		}

		// save .avi
		if (VisualisationFromMaskParameters.getSaveOption(".avi")) {
			publish("-Generating avi video...");
			Thread.yield();

			ImagePlus imp = FolderOpener.open(imageFolder.getAbsolutePath(), "");
			IJ.run(imp, "AVI... ",
					"compression=JPEG frame=7 save=" + saveFolder + "/" + projectName + "_" + outputName + ".avi");
		}

		// delete image sequence folder
		if (!VisualisationFromMaskParameters.getSaveOption(".png")) {
			publish("-Deleting temporary files...");
			Thread.yield();

			String[] entries = imageFolder.list();
			for (String fileName : entries) {
				File currentFile = new File(imageFolder.getPath(), fileName);
				currentFile.delete();
			}

			imageFolder.delete();
		}
	}

	public void destroy() {
		process.destroyForcibly();
		this.cancel(true);
	}

	private void runScript() {
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

		ArrayList<String> command = new ArrayList<>();
		command.add("python");
		command.add(scriptPath);
		for (Parameter parameter : script.getParameters())
			command.add(parameter.getValue());

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
				Globals.writeCSV(longestTracksCoordinatesCSVFile, Arrays.asList(m));
			}

			if (validMessage) {
				progress.setMessage("<html>" + m + "</html>");
				progress.setFileCount(VisualisationFromMaskParameters.getFileCount());
				progress.setFileNumber(fileNumber);
			}
		}
	}

}
