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
import ij.ImagePlus;
import ij.plugin.FolderOpener;

public class VisualisationFromMask extends SwingWorker<String, String> {
	private ProgressPanel progress;
	private int fileNumber;
	private PythonScript script;
	private Process process;
	private String imagePath, maskPath, trackPath;
	private MatlabMetadata trackMetadata;
	private File imageSequenceFolder, saveFolder;

	public VisualisationFromMask(ProgressPanel p) {
		progress = p;
	}

	private void addScriptHeader() {
		script.importModule("sys");
		script.importModule("os");
		script.importModule("math");
		script.importModule("pylab", "plt");
		script.importModule("scipy.io", "spio");
		script.importModule("numpy", "np");
		script.importModuleFrom("io", "skimage");
		script.importModuleFrom("read_multiimg_PIL", "MOSES.Utility_Functions.file_io");
		script.importModuleFrom("plot_tracks", "MOSES.Visualisation_Tools.track_plotting");
	}

	public void createVisualisation() {
		List<Parameter> parameters = new ArrayList<Parameter>();
		parameters.addAll(Arrays.asList(new Parameter("imagePath", "str", imagePath),
				new Parameter("maskPath", "str", maskPath), new Parameter("trackPath", "str", trackPath),
				new Parameter("fileName", "str", Globals.getNameWithoutExtension(imagePath)),
				new Parameter("saveDirectory", "str", imageSequenceFolder), new Parameter("temporal_segment_length",
						"int", VisualisationFromMaskParameters.getTracksTemporalSegment())));

		for (int i = 0; i < VisualisationFromMaskParameters.getChannels().size(); i++) {
			int channelIndex = VisualisationFromMaskParameters.getChannels().get(i);
			parameters.add(
					new Parameter("color_" + channelIndex, "str", VisualisationFromMaskParameters.getColorOption(i)));
		}

		addScriptHeader();

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
				PythonScript.setValue("mask", PythonScript.callFunction("io.imread", "parameters.get('maskPath')")));
		script.addScript(PythonScript.setValue("trackFile",
				PythonScript.callFunction("spio.loadmat", "parameters.get('trackPath')")));

		List<Integer> channels = trackMetadata.getChannels();
		for (Integer channel : channels) {
			script.addScript(PythonScript.setValue("tracks_" + (channel + 1),
					"trackFile['forward_tracks_" + (channel + 1) + "']"));

			script.addScript(PythonScript.setValue("plot_select_ids_" + (channel + 1), "[]"));
			script.startFor("i", "tracks_" + (channel + 1) + ".shape[0]");
			script.startIf("mask[tracks_" + (channel + 1) + "[i, 0, 0], tracks_" + (channel + 1) + "[i, 0, 1]] == 255");
			script.addScript("plot_select_ids_" + (channel + 1) + ".append(i)");
			script.stopIf();
			script.stopFor();
		}
		script.newLine();

		script.addCommnet("plot forward tracks");
		script.addScript(PythonScript.setValue("vidstack", PythonScript.callFunction("np.squeeze", "vidstack")));
		script.addScript(PythonScript.setValue("saveLocation", "parameters.get('saveDirectory')"));

		script.addScript(PythonScript.print(PythonScript.addString("-Plotting tracks for complete visualisation...")));
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

		for (int i = 0; i < VisualisationFromMaskParameters.getChannels().size(); i++) {
			int channelIndex = VisualisationFromMaskParameters.getChannels().get(i);

			script.addScript(PythonScript.callFunction("plot_tracks",
					Arrays.asList(
							"tracks_" + (channelIndex + 1) + "[plot_select_ids_" + (channelIndex + 1)
									+ ", frame-len_segment:frame+1]",
							"ax", "color = parameters.get('color_" + channelIndex + "')", "lw = 1")));
		}
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
		parameters.addAll(Arrays.asList(new Parameter("imagePath", "str", imagePath),
				new Parameter("maskPath", "str", maskPath), new Parameter("trackPath", "str", trackPath),
				new Parameter("fileName", "str", Globals.getNameWithoutExtension(imagePath)),
				new Parameter("saveDirectory", "str", saveFolder)));

		List<Integer> channels = trackMetadata.getChannels();

		addScriptHeader();

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
				PythonScript.setValue("mask", PythonScript.callFunction("io.imread", "parameters.get('maskPath')")));
		script.addScript(PythonScript.setValue("trackFile",
				PythonScript.callFunction("spio.loadmat", "parameters.get('trackPath')")));

		script.addScript(PythonScript.print(PythonScript.addString("-Computing longest tracks...")));
		script.startFunction("fill_mask_region", Arrays.asList("x", "y", "f"));
		script.addScript(PythonScript.setValue("mask[x, y]", "f"));
		script.addScript(PythonScript.setValue("stack", "[]"));
		script.addScript(PythonScript.callFunction("stack.append", "[x, y]"));
		script.startWhile("len(stack) > 0");
		script.addScript(PythonScript.setValue("pair", "stack.pop()"));
		script.addScript(PythonScript.setValue("X", "pair[0]"));
		script.addScript(PythonScript.setValue("Y", "pair[1]"));

		script.startIf("mask[X + 1, Y] == 255");
		script.addScript(PythonScript.callFunction("stack.append", "[X + 1, Y]"));
		script.addScript(PythonScript.setValue("mask[X + 1, Y]", "f"));
		script.stopIf();

		script.startIf("mask[X, Y + 1] == 255");
		script.addScript(PythonScript.callFunction("stack.append", "[X, Y + 1]"));
		script.addScript(PythonScript.setValue("mask[X, Y + 1]", "f"));
		script.stopIf();

		script.startIf("mask[X - 1, Y] == 255");
		script.addScript(PythonScript.callFunction("stack.append", "[X - 1, Y]"));
		script.addScript(PythonScript.setValue("mask[X - 1, Y]", "f"));
		script.stopIf();

		script.startIf("mask[X, Y - 1] == 255");
		script.addScript(PythonScript.callFunction("stack.append", "[X, Y - 1]"));
		script.addScript(PythonScript.setValue("mask[X, Y - 1]", "f"));
		script.stopIf();

		script.stopWhile();
		script.stopFunction();

		script.addScript(PythonScript.setValue("region_count", "0"));
		script.startFor("i", "mask.shape[0]");
		script.startFor("j", "mask.shape[1]");
		script.startIf("mask[i, j] == 255");
		script.addScript(PythonScript.setValue("region_count", "region_count + 1"));
		script.addScript(PythonScript.callFunction("fill_mask_region", Arrays.asList("i", "j", "region_count")));
		script.stopIf();
		script.stopFor();
		script.stopFor();

		for (Integer channel : channels) {
			script.addScript(PythonScript.setValue("tracks_" + (channel + 1),
					"trackFile['forward_tracks_" + (channel + 1) + "']"));

			script.addScript(
					PythonScript.setValue("plot_select_ids_" + (channel + 1), "np.zeros(region_count,  dtype=int)"));
			script.addScript(PythonScript.setValue("max_values_" + (channel + 1), "np.zeros(region_count)"));

			script.startFor("i", "tracks_" + (channel + 1) + ".shape[0]");
			script.startIf("mask[tracks_" + (channel + 1) + "[i, 0, 0], tracks_" + (channel + 1)
					+ "[i, 0, 1]] in range(1, region_count + 1)");
			script.addScript(PythonScript.setValue("d",
					PythonScript.callFunction("math.sqrt",
							"(tracks_" + (channel + 1) + "[i, 0, 0] - tracks_" + (channel + 1)
									+ "[i, -1, 0])**2 + (tracks_" + (channel + 1) + "[i, 0, 1] - tracks_"
									+ (channel + 1) + "[i, -1, 1])**2")));
			script.addScript(PythonScript.setValue("region_number",
					"mask[tracks_" + (channel + 1) + "[i, 0, 0], tracks_" + (channel + 1) + "[i, 0, 1]] - 1"));
			script.startIf("d > max_values_" + (channel + 1) + "[region_number]");
			script.addScript(PythonScript.setValue("max_values_" + (channel + 1) + "[region_number]", "d"));
			script.addScript(PythonScript.setValue("plot_select_ids_" + (channel + 1) + "[region_number]", "i"));
			script.stopFor();
			script.stopIf();
			script.stopFor();
		}
		script.newLine();

		script.addCommnet("plot forward tracks");
		script.addScript(PythonScript.setValue("vidstack", PythonScript.callFunction("np.squeeze", "vidstack")));
		script.addScript(PythonScript.setValue("saveLocation", "parameters.get('saveDirectory')"));

		script.addScript(PythonScript.setValue("frame_img", "vidstack[0]"));
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

		script.addScript(PythonScript.setValue("color", "['r', 'g', 'b', 'm', 'y', 'c']"));
		for (Integer channel : channels) {
			script.addScript(PythonScript.setValue("plot_select_ids_" + (channel + 1),
					PythonScript.callFunction("np.vstack", "plot_select_ids_" + (channel + 1))));

			script.startFor("i", "len(plot_select_ids_" + (channel + 1) + ")");
			script.addScript(PythonScript.callFunction("plot_tracks",
					Arrays.asList("tracks_" + (channel + 1) + "[plot_select_ids_" + (channel + 1) + "[i], :]", "ax",
							"color=color[i % 6]", "lw = 1")));
			script.stopFor();
		}
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

	public void folderPaths() {
		if (VisualisationFromMaskParameters.getBatchMode()) {

		} else {
			saveFolder = new File(VisualisationFromMaskParameters.getSaveDirectory());

			if (VisualisationFromMaskParameters.isCompleteVisualisation()) {
				String imageSequenceFolderPath = VisualisationFromMaskParameters.getSaveDirectory() + "/"
						+ Globals.getNameWithoutExtension(imagePath) + "_tracks_visualisation_image_sequence";
				imageSequenceFolder = new File(imageSequenceFolderPath);
				imageSequenceFolder.mkdirs();
			}
		}
	}

	@Override
	protected String doInBackground() throws Exception {
		publish("-Processing parameters...");
		fileNumber = 1;

		for (ImageMaskTracks imt : VisualisationFromMaskParameters.getFiles()) {
			imagePath = imt.getImagePath();
			maskPath = imt.getMaskPath();
			trackPath = imt.getTrackPath();
			trackMetadata = new MatlabMetadata(trackPath);

			progress.setFileName(Globals.getName(imagePath));
			folderPaths();

			if (VisualisationFromMaskParameters.isCompleteVisualisation() && !this.isCancelled()) {
				script = new PythonScript("Mask visualisation");
				createVisualisation();
				runScript();

				executeSaveOption(imageSequenceFolder, saveFolder, "tracks_visualisation");
			}

			if (VisualisationFromMaskParameters.isLongestTracksVisualisation() && !this.isCancelled()) {
				script = new PythonScript("Longest tracks");
				longestTracksVisualisation();
				runScript();
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
			imp.show();
			IJ.saveAs(imp, "Tiff",
					saveFolder + "/" + Globals.getNameWithoutExtension(imagePath) + "_" + outputName + ".tif");
		}

		// save .avi
		if (VisualisationFromMaskParameters.getSaveOption(".avi")) {
			publish("-Generating avi video...");
			Thread.yield();

			ImagePlus imp = FolderOpener.open(imageFolder.getAbsolutePath(), "");
			IJ.run(imp, "AVI... ", "compression=JPEG frame=7 save=" + saveFolder + "/"
					+ Globals.getNameWithoutExtension(imagePath) + "_" + outputName + ".avi");
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

			if (validMessage) {
				progress.setMessage("<html>" + m + "</html>");
				progress.setFileCount(VisualisationFromMaskParameters.getFileCount());
				progress.setFileNumber(fileNumber);
			}
		}
	}

}
