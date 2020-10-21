import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfRect2d;
import org.opencv.core.Point;
import org.opencv.core.Rect2d;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.dnn.Dnn;
import org.opencv.dnn.Net;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;

import ij.IJ;
import ij.ImagePlus;
import ij.plugin.FolderOpener;

public class BoundingBoxAndMaskGeneration extends SwingWorker<String, String> {
	private String bboxFolderPath, bboxFolderVisPath, annotationFolderPath, temporarySlicePath, currentFilePath,
			maskFolderPath;
	private File bboxFolderVis, bboxFolder, maskFolder;
	ImagePlus tiffStack;
	ProgressPanel progress;
	Net net;

	public BoundingBoxAndMaskGeneration(ProgressPanel p) {
		progress = p;

		// File dll = new File(System.getProperty("user.dir") +
		// "/opencv/windows/x64/opencv_java3411.dll");
		File dll = new File(System.getProperty("user.dir") + "/plugins/MOSES/opencv/windows/x64/opencv_java3411.dll");
		File modelWeights = new File(
				System.getProperty("user.dir") + "/plugins/MOSES/Weights/yolov3-organoid_rescale_16500.weights");
		File modelConfiguration = new File(
				System.getProperty("user.dir") + "/plugins/MOSES/Weights/yolov3-organoid_rescale.cfg");

		try {
			System.load(dll.getAbsolutePath());
		} catch (UnsatisfiedLinkError e) {

			IJ.log("Native code library failed to load.\n" + e);
		}

		net = Dnn.readNetFromDarknet(modelConfiguration.getAbsolutePath(), modelWeights.getAbsolutePath());
	}

	private static List<String> getOutputNames(Net net) {
		List<String> names = new ArrayList<>();

		List<Integer> outLayers = net.getUnconnectedOutLayers().toList();
		List<String> layersNames = net.getLayerNames();

		outLayers.forEach((item) -> names.add(layersNames.get(item - 1)));
		return names;
	}

	protected String doInBackground() {
		progress.setVisibility(true);
		progress.setIndeterminate(true);
		progress.setFileCount(BoundingBoxAndMaskGenerationParameters.getFiles().size());

		int n = 1;
		for (String filePath : BoundingBoxAndMaskGenerationParameters.getFiles()) {
			currentFilePath = filePath;
			folderPaths();

			progress.setFileNumber(n);
			progress.setFileName(Globals.getName(filePath));

			tiffStack = new ImagePlus(filePath);

//			if (BoundingBoxAndMaskGenerationParameters.getIncreaseBBoxContrast())
//				for (int i = 1; i <= BoundingBoxAndMaskGenerationParameters.getBBoxContrastIncrase(); i++)
//					IJ.run(tiffStack, "Enhance Contrast...", "saturated=0.3 normalize process_all");

			progress.setIndeterminate(false);
			progress.setMaximum(tiffStack.getNSlices());
			for (int f = 1; f <= tiffStack.getNSlices(); f++) {
				progress.setMessage("Please wait, this may take a couple of minutes...");
				progress.setStringPainted(true);
				progress.setString(f + " / " + tiffStack.getNSlices() + " frames");
				progress.setValue(f);

				tiffStack.setSlice(f);
				temporarySlicePath = System.getProperty("java.io.tmpdir") + "MOSESimage" + f + ".png";
				File temporarySlice = new File(temporarySlicePath);
				IJ.saveAs(tiffStack, "PNG", temporarySlicePath);

				if (BoundingBoxAndMaskGenerationParameters.isOutput("bounding_box"))
					generateBBox();

				if (BoundingBoxAndMaskGenerationParameters.isOutput("mask"))
					generateMask();

				temporarySlice.delete();
			}

			if (BoundingBoxAndMaskGenerationParameters.isOutput("bounding_box_vis"))
				executeSaveOption("bounding_box_vis", bboxFolderVis, "boundary_box_vis");

			if (BoundingBoxAndMaskGenerationParameters.isOutput("mask"))
				executeSaveOption("mask", maskFolder, "mask_binary");

		}

		return "Done.";
	}

	private void generateBBox() {
		Mat image = Imgcodecs.imread(temporarySlicePath);
		Size sz = new Size(416, 416);
		Mat blob = Dnn.blobFromImage(image, 0.00392, sz, new Scalar(0), true, false);
		net.setInput(blob);

		List<Mat> result = new ArrayList<>();
		List<String> outBlobNames = getOutputNames(net);

		net.forward(result, outBlobNames);

		float confThreshold = BoundingBoxAndMaskGenerationParameters.getBBoxConfidenceTresh();
		List<Integer> clsIds = new ArrayList<>();
		List<Float> confs = new ArrayList<>();
		List<Rect2d> rects = new ArrayList<>();
		for (int i = 0; i < result.size(); ++i) {
			Mat level = result.get(i);
			for (int j = 0; j < level.rows(); ++j) {
				Mat row = level.row(j);
				Mat scores = row.colRange(5, level.cols());
				Core.MinMaxLocResult mm = Core.minMaxLoc(scores);
				float confidence = (float) mm.maxVal;
				Point classIdPoint = mm.maxLoc;
				if (confidence > confThreshold) {
					int centerX = (int) (row.get(0, 0)[0] * image.cols());
					int centerY = (int) (row.get(0, 1)[0] * image.rows());
					int width = (int) (row.get(0, 2)[0] * image.cols());
					int height = (int) (row.get(0, 3)[0] * image.rows());
					int left = centerX - width / 2;
					int top = centerY - height / 2;

					clsIds.add((int) classIdPoint.x);
					confs.add((float) confidence);
					rects.add(new Rect2d(left, top, width, height));
				}
			}
		}

		if (rects.size() != 0) {
			File bbTextFile = new File(bboxFolderPath + "/" + Globals.getNameWithoutExtension(currentFilePath)
					+ "_bounding_box_f" + tiffStack.getZ() + ".txt");

			try {
				bbTextFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}

			// Apply non-maximum suppression procedure.
			float nmsThresh = 0.1f;
			MatOfFloat confidences = new MatOfFloat(Converters.vector_float_to_Mat(confs));
			Rect2d[] boxesArray = rects.toArray(new Rect2d[0]);
			MatOfRect2d boxes = new MatOfRect2d(boxesArray);
			MatOfInt indices = new MatOfInt();
			Dnn.NMSBoxes(boxes, confidences, confThreshold, nmsThresh, indices);

			int[] ind = indices.toArray();
			for (int i = 0; i < ind.length; ++i) {
				int idx = ind[i];
				Rect2d box = boxesArray[idx];

				Globals.writeTXT(bbTextFile,
						Arrays.asList("organoid", String.valueOf(confs.get(idx)),
								String.valueOf(Math.round(box.x + box.width / 2)),
								String.valueOf(Math.round(box.y + box.height / 2)),
								String.valueOf(Math.round(box.width)), String.valueOf(Math.round(box.height))));

				if (BoundingBoxAndMaskGenerationParameters.isOutput("bounding_box_vis")) {
					Imgproc.rectangle(image, box.tl(), box.br(), new Scalar(0, 0, 255), 2);
				}
			}
		}

		Imgcodecs.imwrite(bboxFolderVisPath + "/" + Globals.getNameWithoutExtension(currentFilePath) + "_f"
				+ tiffStack.getZ() + ".png", image);
	}

	private void generateMask() {
		File weights = new File(
				System.getProperty("user.dir") + "/plugins/MOSES/Weights/organoid-seg-unet-master-v1.h5");

		String script = "import os  \r\n" + "import sys\r\n" + "import skimage.transform as sktform\r\n"
				+ "import numpy as np\r\n" + "from keras.optimizers import Adam\r\n"
				+ "from keras.models import Model, load_model\r\n"
				+ "from keras.layers import Conv2D, MaxPooling2D, UpSampling2D, Input, BatchNormalization, Activation, Add, Input\r\n"
				+ "from tensorflow.keras.layers import Conv2DTranspose\r\n"
				+ "from skimage.exposure import rescale_intensity\r\n" + "import glob \r\n"
				+ "import skimage.io as skio \r\n" + "\r\n"
				+ "parameters = dict(imagePath = str(sys.argv[1]), weightsPath = str(sys.argv[2]), savePath = str(sys.argv[3]), name = str(sys.argv[4]))\r\n"
				+ "\r\n" + "input_img = Input(shape=(512,512,3))\r\n" + "test_size = (512, 512)\r\n" + "\r\n"
				+ "L1 = Conv2D(32, (3, 3), activation=\"relu\", data_format=\"channels_last\", kernel_initializer=\"normal\", padding=\"same\", name='L1_1')(input_img)   # 32     128                   # 128\r\n"
				+ "L1 = Conv2D(32, (3, 3), activation=\"relu\", data_format=\"channels_last\", kernel_initializer=\"normal\", padding=\"same\", name='L1_2')(L1)   \r\n"
				+ "   \r\n"
				+ "L2 = MaxPooling2D(data_format=\"channels_last\", pool_size=(2, 2), padding=\"same\", name='pool1')(L1)                                       # 16 64\r\n"
				+ "L2 = Conv2D(64, (3, 3), activation=\"relu\", data_format=\"channels_last\", kernel_initializer=\"he_normal\", padding=\"same\", name='L2_1')(L2)   \r\n"
				+ "L2 = Conv2D(64, (3, 3), activation=\"relu\", data_format=\"channels_last\", kernel_initializer=\"he_normal\", padding=\"same\")(L2)          \r\n"
				+ "\r\n" + "\r\n"
				+ "L3 = MaxPooling2D(data_format=\"channels_last\", pool_size=(2, 2), padding=\"same\", name='pool2')(L2)                                       # 8 32\r\n"
				+ "L3 = Conv2D(64, (3, 3), activation=\"relu\", data_format=\"channels_last\", kernel_initializer=\"normal\", padding=\"same\", name='L3_1')(L3)\r\n"
				+ "L3 = Conv2D(64, (3, 3), activation=\"relu\", data_format=\"channels_last\", kernel_initializer=\"normal\", padding=\"same\", name='L3_2')(L3)   \r\n"
				+ "\r\n"
				+ "L4 = MaxPooling2D(data_format=\"channels_last\", pool_size=(2, 2), padding=\"same\", name='pool3')(L3)                                       # 8 32\r\n"
				+ "L4 = Conv2D(72, (3, 3), activation=\"relu\", data_format=\"channels_last\", kernel_initializer=\"he_normal\", padding=\"same\")(L4)\r\n"
				+ "L4 = Conv2D(72, (3, 3), activation=\"relu\", data_format=\"channels_last\", kernel_initializer=\"normal\", padding=\"same\")(L4)          \r\n"
				+ "\r\n" + "\r\n"
				+ "L5 = MaxPooling2D(data_format=\"channels_last\", pool_size=(2, 2), padding=\"same\")(L4)    \r\n"
				+ "L5 = Conv2D(96, (3, 3), activation=\"relu\", data_format=\"channels_last\", kernel_initializer=\"normal\", padding=\"same\")(L5)\r\n"
				+ "L5 = Conv2D(72, (3, 3), activation=\"relu\", data_format=\"channels_last\", kernel_initializer=\"normal\", padding=\"same\")(L5)          \r\n"
				+ "\r\n" + "\r\n"
				+ "U00 = UpSampling2D(size=(2, 2), data_format=\"channels_last\")(L5)                                                            # 16 64\r\n"
				+ "U00 = Add()([L4,U00])                                                                            # 16 + 16\r\n"
				+ "U00 = Conv2DTranspose(96, (3, 3), activation=\"relu\", data_format=\"channels_last\", kernel_initializer=\"he_normal\", padding=\"same\")(U00)          \r\n"
				+ "U00 = Conv2D(64, (3, 3), activation=\"linear\", data_format=\"channels_last\", kernel_initializer=\"he_normal\", padding=\"same\")(U00)   \r\n"
				+ "U00 = Activation('relu')(U00)\r\n" + "\r\n"
				+ "U0 = UpSampling2D(size=(2, 2), data_format=\"channels_last\")(U00)                                                            # 16 64\r\n"
				+ "U0 = Add()([L3,U0])\r\n"
				+ "U0 = Conv2DTranspose(72, (3, 3), activation=\"relu\", data_format=\"channels_last\", kernel_initializer=\"normal\", padding=\"same\")(U0)          \r\n"
				+ "U0 = Conv2D(64, (3, 3), activation=\"linear\", data_format=\"channels_last\", kernel_initializer=\"normal\", padding=\"same\")(U0)   \r\n"
				+ "U0 = Activation('relu')(U0)\r\n" + "\r\n" + "\r\n"
				+ "U1 = UpSampling2D(size=(2, 2), data_format=\"channels_last\")(U0)                                                            # 16 64\r\n"
				+ "U1 = Add()([L2,U1])\r\n"
				+ "U1 = Conv2DTranspose(64, (3, 3), activation=\"relu\", data_format=\"channels_last\", kernel_initializer=\"normal\", padding=\"same\")(U1)          \r\n"
				+ "U1 = Conv2D(32, (3, 3), activation=\"linear\", data_format=\"channels_last\", kernel_initializer=\"normal\", padding=\"same\")(U1)    \r\n"
				+ "U1 = Activation('relu')(U1)\r\n" + "\r\n"
				+ "U2 = UpSampling2D(size=(2, 2), data_format=\"channels_last\")(U1)                                                            # 32 up 2 128\r\n"
				+ "U2 = Add()([L1,U2])                                                                                     # 32 + 32\r\n"
				+ "U2 = Conv2DTranspose(32, (3, 3), activation=\"relu\", data_format=\"channels_last\", kernel_initializer=\"normal\", padding=\"same\")(U2)   \r\n"
				+ "\r\n"
				+ "U3 = Conv2D(2, (1, 1), activation=\"softmax\", data_format=\"channels_last\", kernel_initializer=\"normal\", padding=\"same\")(U2)   # 32\r\n"
				+ "final_model = Model(inputs = [input_img], outputs = [U3])\r\n" + "\r\n"
				+ "opt = Adam(lr=1e-4, beta_1=0.5, beta_2=0.999, epsilon=1e-08)\r\n" + "\r\n"
				+ "final_model.compile(loss='categorical_crossentropy', optimizer=opt, metrics=['accuracy'])\r\n"
				+ "final_model.load_weights(parameters.get('weightsPath'))\r\n" + "\r\n" + "    \r\n" + "  \r\n"
				+ "image = skio.imread(parameters.get('imagePath'))\r\n" + "\r\n"
				+ "im = sktform.resize(rescale_intensity(image), test_size, preserve_range=True)/255.\r\n"
				+ "                    \r\n" + "im = np.dstack([im, im, im])\r\n" + "im = im.astype(np.float32)\r\n"
				+ "\r\n" + "im_out = final_model.predict(im[None,:])\r\n" + "im_out = im_out[...,1][0]\r\n" + "\r\n"
				+ "im_out = sktform.resize(rescale_intensity(im_out), image.shape[:3], preserve_range=True) \r\n"
				+ "\r\n"
				+ "skio.imsave(os.path.join(parameters.get('savePath'), parameters.get('name') + \".png\"), im_out)";

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
			writer.write(script);
			writer.close();
		} catch (IOException e) {
			IJ.handleException(e);
		}

		// construct command
		ArrayList<String> command = new ArrayList<>();
		command.add("python");
		command.add(scriptPath);
		command.add(temporarySlicePath);
		command.add(weights.getAbsolutePath());
		command.add(maskFolderPath);
		command.add("mask_f" + tiffStack.getZ());

		// run process
		ProcessBuilder pb = new ProcessBuilder(command);

		try {
			Process process = pb.start();

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

	private void executeSaveOption(String saveOptionName, File folder, String outputName) {
		// save .tif
		if (BoundingBoxAndMaskGenerationParameters.getSaveOption(saveOptionName, ".tif")) {
			publish("-Generating tiff stack...");
			Thread.yield();

			ImagePlus imp = FolderOpener.open(folder.getAbsolutePath(), "");
			IJ.saveAs(imp, "Tiff", annotationFolderPath + "/" + Globals.getNameWithoutExtension(currentFilePath) + "_"
					+ outputName + ".tif");
		}

		// save .avi
		if (BoundingBoxAndMaskGenerationParameters.getSaveOption(saveOptionName, ".avi")) {
			publish("-Generating avi video...");
			Thread.yield();

			ImagePlus imp = FolderOpener.open(folder.getAbsolutePath(), "");
			IJ.run(imp, "AVI... ", "compression=JPEG frame=7 save=[" + annotationFolderPath + "/"
					+ Globals.getNameWithoutExtension(currentFilePath) + "_" + outputName + ".avi]");
		}

		// delete image sequence folder
		if (!BoundingBoxAndMaskGenerationParameters.getSaveOption(saveOptionName, ".png")) {
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

	private void folderPaths() {
		annotationFolderPath = BoundingBoxAndMaskGenerationParameters.getWorkspace() + "/"
				+ Globals.getNameWithoutExtension(currentFilePath) + "/" + "annotations" + "/"
				+ Globals.getFormattedDate();
		File annotationFolder = new File(annotationFolderPath);
		annotationFolder.mkdirs();

		if (BoundingBoxAndMaskGenerationParameters.isOutput("bounding_box")) {
			bboxFolderPath = annotationFolderPath + "/" + "bounding_boxes";
			bboxFolder = new File(bboxFolderPath);
			bboxFolder.mkdirs();

			if (BoundingBoxAndMaskGenerationParameters.isOutput("bounding_box_vis")) {
				bboxFolderVisPath = annotationFolderPath + "/" + "bounding_boxes_visualization";
				bboxFolderVis = new File(bboxFolderVisPath);
				bboxFolderVis.mkdirs();
			}
		}

		if (BoundingBoxAndMaskGenerationParameters.isOutput("mask")) {
			maskFolderPath = annotationFolderPath + "/" + "mask_binary";
			maskFolder = new File(maskFolderPath);
			maskFolder.mkdirs();
		}
	}

	protected void done() {
		progress.setVisible(false);
	}
}