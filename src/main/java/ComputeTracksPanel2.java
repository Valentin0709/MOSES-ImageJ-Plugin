import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.GeneralPath;
import java.text.DecimalFormat;

import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Overlay;
import ij.gui.Roi;
import ij.gui.ShapeRoi;

public class ComputeTracksPanel2 extends JPanel {
	public MainFrame parentFrame;
	public ComputeTracksPanel2 self = this;

	public ComputeTracksPanel2(MainFrame parentFrame) {

		// set look and feel

		try {
			UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}

		// set parent frame

		this.parentFrame = parentFrame;

		// set size

		this.setPreferredSize(new Dimension(Globals.frameWidth, Globals.frameHight));

		// set background color

		this.setBackground(new Color(252, 252, 252));

		// title labels

		JLabel titleLabel = new JLabel("Compute superpixel tracks", SwingConstants.CENTER);
		titleLabel.setBounds(0, 0, 500, 36);
		titleLabel.setVerticalTextPosition(SwingConstants.CENTER);
		titleLabel.setHorizontalTextPosition(SwingConstants.CENTER);
		titleLabel.setFont(new Font("Arial Black", Font.BOLD, 25));

		// collect parameters labels

		JLabel opticalFlowParameterLabel = new JLabel("Optical flow parameter", SwingConstants.CENTER);
		opticalFlowParameterLabel.setBounds(0, 0, 230, 39);
		opticalFlowParameterLabel.setVerticalTextPosition(SwingConstants.CENTER);
		opticalFlowParameterLabel.setHorizontalTextPosition(SwingConstants.CENTER);
		opticalFlowParameterLabel.setFont(new Font("Arial", Font.BOLD, 18));

		JLabel scaleFactorLabel = new JLabel("Scale factor:", SwingConstants.LEFT);
		scaleFactorLabel.setBounds(10, 50, 90, 20);
		scaleFactorLabel.setVerticalTextPosition(SwingConstants.CENTER);
		scaleFactorLabel.setHorizontalTextPosition(SwingConstants.CENTER);
		scaleFactorLabel.setFont(new Font("Roboto", Font.PLAIN, 15));

		JLabel levelsLabel = new JLabel("Levels:", SwingConstants.LEFT);
		levelsLabel.setBounds(10, 80, 90, 20);
		levelsLabel.setVerticalTextPosition(SwingConstants.CENTER);
		levelsLabel.setHorizontalTextPosition(SwingConstants.CENTER);
		levelsLabel.setFont(new Font("Roboto", Font.PLAIN, 15));

		JLabel windowSizeLabel = new JLabel("Window size:", SwingConstants.LEFT);
		windowSizeLabel.setBounds(10, 110, 90, 20);
		windowSizeLabel.setVerticalTextPosition(SwingConstants.CENTER);
		windowSizeLabel.setHorizontalTextPosition(SwingConstants.CENTER);
		windowSizeLabel.setFont(new Font("Roboto", Font.PLAIN, 15));

		JLabel iterationsLabel = new JLabel("Iterations:", SwingConstants.LEFT);
		iterationsLabel.setBounds(10, 140, 90, 20);
		iterationsLabel.setVerticalTextPosition(SwingConstants.CENTER);
		iterationsLabel.setHorizontalTextPosition(SwingConstants.CENTER);
		iterationsLabel.setFont(new Font("Roboto", Font.PLAIN, 15));

		JLabel polyNLabel = new JLabel("Poly n:", SwingConstants.LEFT);
		polyNLabel.setBounds(10, 170, 90, 20);
		polyNLabel.setVerticalTextPosition(SwingConstants.CENTER);
		polyNLabel.setHorizontalTextPosition(SwingConstants.CENTER);
		polyNLabel.setFont(new Font("Roboto", Font.PLAIN, 15));

		JLabel polySigmaLabel = new JLabel("Poly sigma:", SwingConstants.LEFT);
		polySigmaLabel.setBounds(10, 200, 90, 20);
		polySigmaLabel.setVerticalTextPosition(SwingConstants.CENTER);
		polySigmaLabel.setHorizontalTextPosition(SwingConstants.CENTER);
		polySigmaLabel.setFont(new Font("Roboto", Font.PLAIN, 15));

		JLabel numberSuperpixelsLabel = new JLabel("Number of superpixels", SwingConstants.CENTER);
		numberSuperpixelsLabel.setBounds(0, 0, 230, 39);
		numberSuperpixelsLabel.setVerticalTextPosition(SwingConstants.CENTER);
		numberSuperpixelsLabel.setHorizontalTextPosition(SwingConstants.CENTER);
		numberSuperpixelsLabel.setFont(new Font("Arial", Font.BOLD, 18));

		JLabel flagsLabel = new JLabel("Flags:", SwingConstants.LEFT);
		flagsLabel.setBounds(10, 230, 90, 20);
		flagsLabel.setVerticalTextPosition(SwingConstants.CENTER);
		flagsLabel.setHorizontalTextPosition(SwingConstants.CENTER);
		flagsLabel.setFont(new Font("Roboto", Font.PLAIN, 15));

		// collect parameters fields

		DecimalFormat decimalFormat = new DecimalFormat("#.##");
		decimalFormat.setGroupingUsed(false);
		DecimalFormat integerFormat = new DecimalFormat("###");
		decimalFormat.setGroupingUsed(false);

		JFormattedTextField scaleFactorField = new JFormattedTextField(decimalFormat) {
			@Override
			protected void processFocusEvent(final FocusEvent e) {
				if (e.isTemporary()) {
					return;
				}

				if (e.getID() == FocusEvent.FOCUS_LOST) {
					if (getText() == null || getText().isEmpty()) {
						setValue(0);
					}
				}
				super.processFocusEvent(e);
			}
		};

		scaleFactorField.setHorizontalAlignment(SwingConstants.CENTER);
		scaleFactorField.setFont(new Font("Roboto", Font.PLAIN, 15));
		scaleFactorField.setText(String.valueOf(Globals.pyr_scale));
		scaleFactorField.setBounds(125, 50, 60, 20);

		JFormattedTextField levelsField = new JFormattedTextField(integerFormat) {
			@Override
			protected void processFocusEvent(final FocusEvent e) {
				if (e.isTemporary()) {
					return;
				}

				if (e.getID() == FocusEvent.FOCUS_LOST) {
					if (getText() == null || getText().isEmpty()) {
						setValue(0);
					}
				}
				super.processFocusEvent(e);
			}
		};
		levelsField.setText(String.valueOf(Globals.levels));
		levelsField.setHorizontalAlignment(SwingConstants.CENTER);
		levelsField.setFont(new Font("Roboto", Font.PLAIN, 15));
		levelsField.setBounds(125, 80, 60, 20);

		JFormattedTextField windowSizeField = new JFormattedTextField(integerFormat) {
			@Override
			protected void processFocusEvent(final FocusEvent e) {
				if (e.isTemporary()) {
					return;
				}

				if (e.getID() == FocusEvent.FOCUS_LOST) {
					if (getText() == null || getText().isEmpty()) {
						setValue(0);
					}
				}
				super.processFocusEvent(e);
			}
		};
		windowSizeField.setText(String.valueOf(Globals.winSize));
		windowSizeField.setHorizontalAlignment(SwingConstants.CENTER);
		windowSizeField.setFont(new Font("Roboto", Font.PLAIN, 15));
		windowSizeField.setBounds(125, 110, 60, 20);

		JFormattedTextField iterationsField = new JFormattedTextField(integerFormat) {
			@Override
			protected void processFocusEvent(final FocusEvent e) {
				if (e.isTemporary()) {
					return;
				}

				if (e.getID() == FocusEvent.FOCUS_LOST) {
					if (getText() == null || getText().isEmpty()) {
						setValue(0);
					}
				}
				super.processFocusEvent(e);
			}
		};
		iterationsField.setText(String.valueOf(Globals.iterations));
		iterationsField.setHorizontalAlignment(SwingConstants.CENTER);
		iterationsField.setFont(new Font("Roboto", Font.PLAIN, 15));
		iterationsField.setBounds(125, 140, 60, 20);

		JFormattedTextField polyNField = new JFormattedTextField(integerFormat) {
			@Override
			protected void processFocusEvent(final FocusEvent e) {
				if (e.isTemporary()) {
					return;
				}

				if (e.getID() == FocusEvent.FOCUS_LOST) {
					if (getText() == null || getText().isEmpty()) {
						setValue(0);
					}
				}
				super.processFocusEvent(e);
			}
		};
		polyNField.setText(String.valueOf(Globals.polyn));
		polyNField.setHorizontalAlignment(SwingConstants.CENTER);
		polyNField.setFont(new Font("Roboto", Font.PLAIN, 15));
		polyNField.setBounds(125, 170, 60, 20);

		JFormattedTextField polySigmaField = new JFormattedTextField(decimalFormat) {
			@Override
			protected void processFocusEvent(final FocusEvent e) {
				if (e.isTemporary()) {
					return;
				}

				if (e.getID() == FocusEvent.FOCUS_LOST) {
					if (getText() == null || getText().isEmpty()) {
						setValue(0);
					}
				}
				super.processFocusEvent(e);
			}
		};
		polySigmaField.setText(String.valueOf(Globals.polysigma));
		polySigmaField.setHorizontalAlignment(SwingConstants.CENTER);
		polySigmaField.setFont(new Font("Roboto", Font.PLAIN, 15));
		polySigmaField.setBounds(125, 200, 60, 20);

		JFormattedTextField flagsField = new JFormattedTextField(integerFormat) {
			@Override
			protected void processFocusEvent(final FocusEvent e) {
				if (e.isTemporary()) {
					return;
				}

				if (e.getID() == FocusEvent.FOCUS_LOST) {
					if (getText() == null || getText().isEmpty()) {
						setValue(0);
					}
				}
				super.processFocusEvent(e);
			}
		};
		flagsField.setText(String.valueOf(Globals.flags));
		flagsField.setHorizontalAlignment(SwingConstants.CENTER);
		flagsField.setFont(new Font("Roboto", Font.PLAIN, 15));
		flagsField.setBounds(125, 230, 60, 20);

		JFormattedTextField numberSuperpixelsField = new JFormattedTextField(integerFormat) {
			@Override
			protected void processFocusEvent(final FocusEvent e) {
				if (e.isTemporary()) {
					return;
				}

				if (e.getID() == FocusEvent.FOCUS_LOST) {
					if (getText() == null || getText().isEmpty()) {
						setValue(0);
					}
				}
				super.processFocusEvent(e);
			}
		};
		numberSuperpixelsField.setBounds(65, 50, 100, 20);
		numberSuperpixelsField.setText(String.valueOf(Globals.numberSuperpixels));
		numberSuperpixelsField.setHorizontalAlignment(SwingConstants.CENTER);
		numberSuperpixelsField.setFont(new Font("Roboto", Font.PLAIN, 15));

		JFormattedTextField downsizeFactorField = new JFormattedTextField(decimalFormat) {
			@Override
			protected void processFocusEvent(final FocusEvent e) {
				if (e.isTemporary()) {
					return;
				}

				if (e.getID() == FocusEvent.FOCUS_LOST) {
					if (getText() == null || getText().isEmpty()) {
						setValue(0);
					}
				}
				super.processFocusEvent(e);
			}
		};
		downsizeFactorField.setText(String.valueOf(Globals.downsizeFactor));
		downsizeFactorField.setHorizontalAlignment(SwingConstants.CENTER);
		downsizeFactorField.setFont(new Font("Roboto", Font.PLAIN, 15));
		downsizeFactorField.setBounds(65, 50, 100, 20);

		JLabel downsizeFactorLabel = new JLabel("Downsize factor", SwingConstants.CENTER);
		downsizeFactorLabel.setVerticalTextPosition(SwingConstants.CENTER);
		downsizeFactorLabel.setHorizontalTextPosition(SwingConstants.CENTER);
		downsizeFactorLabel.setFont(new Font("Arial", Font.BOLD, 18));
		downsizeFactorLabel.setBounds(0, 0, 230, 39);

		// cancel button

		JButton cancelButton = new JButton("Cancel");
		cancelButton.setBounds(10, 430, 140, 30);
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// display menuPanel and close current panel

				parentFrame.empty();
				parentFrame.menuPanel = new MenuPanel(parentFrame);
				parentFrame.getContentPane().add(parentFrame.menuPanel);
				parentFrame.validate();
			}
		});
		cancelButton.setVerticalTextPosition(SwingConstants.CENTER);
		cancelButton.setHorizontalTextPosition(SwingConstants.CENTER);
		cancelButton.setForeground(Color.WHITE);
		cancelButton.setFont(new Font("Arial", Font.BOLD, 15));
		cancelButton.setBackground(new Color(13, 59, 102));

		// back button - return to previous panel

		JButton backButton = new JButton("Back");
		backButton.setBounds(200, 430, 140, 30);
		backButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Globals.pyr_scale = Double.parseDouble(scaleFactorField.getText());
				Globals.levels = Integer.parseInt(levelsField.getText());
				Globals.winSize = Integer.parseInt(windowSizeField.getText());
				Globals.iterations = Integer.parseInt(iterationsField.getText());
				Globals.polyn = Integer.parseInt(polyNField.getText());
				Globals.polysigma = Double.parseDouble(polySigmaField.getText());
				Globals.flags = Integer.parseInt(flagsField.getText());
				Globals.numberSuperpixels = Integer.parseInt(numberSuperpixelsField.getText());
				Globals.downsizeFactor = Double.parseDouble(downsizeFactorField.getText());

				// display menuPanel and close current panel

				parentFrame.empty();
				parentFrame.computeTracksPanel1 = new ComputeTracksPanel1(parentFrame);
				parentFrame.getContentPane().add(parentFrame.computeTracksPanel1);
				parentFrame.validate();
			}
		});
		backButton.setVerticalTextPosition(SwingConstants.CENTER);
		backButton.setHorizontalTextPosition(SwingConstants.CENTER);
		backButton.setForeground(Color.WHITE);
		backButton.setFont(new Font("Arial", Font.BOLD, 15));
		backButton.setBackground(new Color(13, 59, 102));

		// next button - generates tracks

		JButton nextButton = new JButton("Next");
		nextButton.setBounds(350, 430, 140, 30);
		nextButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Globals.pyr_scale = Double.parseDouble(scaleFactorField.getText());
				Globals.levels = Integer.parseInt(levelsField.getText());
				Globals.winSize = Integer.parseInt(windowSizeField.getText());
				Globals.iterations = Integer.parseInt(iterationsField.getText());
				Globals.polyn = Integer.parseInt(polyNField.getText());
				Globals.polysigma = Double.parseDouble(polySigmaField.getText());
				Globals.flags = Integer.parseInt(flagsField.getText());
				Globals.numberSuperpixels = Integer.parseInt(numberSuperpixelsField.getText());
				Globals.downsizeFactor = Double.parseDouble(downsizeFactorField.getText());

				// display next panel and close current panel

				parentFrame.empty();
				parentFrame.computeTracksPanel3 = new ComputeTracksPanel3(parentFrame);
				parentFrame.getContentPane().add(parentFrame.computeTracksPanel3);
				parentFrame.validate();
			}
		});

		nextButton.setVerticalTextPosition(SwingConstants.CENTER);
		nextButton.setHorizontalTextPosition(SwingConstants.CENTER);
		nextButton.setForeground(Color.WHITE);
		nextButton.setFont(new Font("Arial", Font.BOLD, 15));
		nextButton.setBackground(new Color(13, 59, 102));

		// panels

		JPanel numberSuperpixelsPanel = new JPanel();
		numberSuperpixelsPanel.setBounds(260, 140, 230, 90);

		JPanel opticalFlowParametersPanel = new JPanel();
		opticalFlowParametersPanel.setBounds(10, 140, 230, 260);
		opticalFlowParametersPanel.setLayout(null);

		JPanel downsizeFactorPanel = new JPanel();
		downsizeFactorPanel.setLayout(null);
		downsizeFactorPanel.setBounds(260, 254, 230, 90);
		add(downsizeFactorPanel);

		// help buttons

		JLabel numberSuperpixelsHelp = new JLabel("?");
		numberSuperpixelsHelp.setBounds(210, 70, 15, 16);
		numberSuperpixelsHelp.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				JFrame dialog = new JFrame("Number of superpixels");
				dialog.setPreferredSize(new Dimension(400, 200));
				dialog.setResizable(false);
				dialog.setLocationRelativeTo(null);
				dialog.getContentPane().setLayout(null);

				JLabel opticalFlowText = new JLabel(
						"<html>Superpixels are the square regions of interest in which the video frame is subdivided. "
								+ "For an image size of n x m pixels, a specification of N superpixels would yield individual superpixels with an approximate size of sqrt(nm/N) x sqrt(nm/N) pixels. "
								+ "For n = 512, m = 512, N = 1000, each superpixel will be of size ≈16×16 pixels. </html>");
				opticalFlowText.setFont(new Font("Roboto", Font.PLAIN, 15));
				opticalFlowText.setBounds(10, 10, 380, 180);

				dialog.getContentPane().add(opticalFlowText);
				dialog.pack();
				dialog.setVisible(true);

			}

			@Override
			public void mouseEntered(MouseEvent e) {
				numberSuperpixelsHelp.setForeground(new Color(255, 74, 28));
			}

			@Override
			public void mouseExited(MouseEvent e) {
				numberSuperpixelsHelp.setForeground(new Color(0, 0, 0));
			}
		});
		numberSuperpixelsPanel.setLayout(null);

		numberSuperpixelsHelp.setFont(new Font("Roboto", Font.BOLD, 20));
		numberSuperpixelsPanel.add(numberSuperpixelsHelp);

		JLabel opticalFlowHelp = new JLabel("?");
		opticalFlowHelp.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				JFrame dialog = new JFrame("Optical flow parameters");
				dialog.setPreferredSize(new Dimension(800, 600));
				dialog.setResizable(false);
				dialog.setLocationRelativeTo(null);
				dialog.getContentPane().setLayout(null);

				JLabel opticalFlowText = new JLabel(
						"<html>MOSES uses the Farneback flow algorithm to compute pixel velocities. You can change the following parameters to increase the accuracy of the motion field computation. <br><br>"
								+ "<b> pyr_scale </b> – parameter specifying the image scale (less than 1) to build pyramids for each image; pyr_scale = 0.5 means a classical pyramid, where each next layer is twice smaller than the previous one. <br><br>"
								+ "<b> levels </b> – number of pyramid layers including the initial image; levels = 1 means that no extra layers are created and only the original images are used. <br><br>"
								+ "<b> winsize </b> – averaging window size; larger values increase the algorithm robustness to image noise and give more chances for fast motion detection, but yield more blurred motion field. <br><br>"
								+ "<b> iterations</b> – number of iterations the algorithm does at each pyramid level. <br><br>"
								+ "<b>  poly_n </b> – size of the pixel neighborhood used to find polynomial expansion in each pixel; larger values mean that the image will be approximated with smoother surfaces, yielding more robust algorithm and more blurred motion field, typically poly_n = 5 or 7. <br><br>"
								+ "<b> poly_sigma </b>– standard deviation of the Gaussian that is used to smooth derivatives used as a basis for the polynomial expansion; for poly_n = 5, you can set poly_sigma = 1.1, for poly_n = 7, a good value would be poly_sigma = 1.5. <br><br>"
								+ "<b> flags </b >– operation flags that can be a combination of the following: <br>"
								+ "OPTFLOW_USE_INITIAL_FLOW uses the input flow as an initial flow approximation. <br><br> "
								+ "OPTFLOW_FARNEBACK_GAUSSIAN uses the Gaussian   filter instead of a box filter of the same size for optical flow estimation; usually, this option gives z more accurate flow than with a box filter, at the cost of lower speed; normally, winsize for a Gaussian window should be set to a larger value to achieve the same level of robustness. </html>");
				opticalFlowText.setFont(new Font("Roboto", Font.PLAIN, 15));
				opticalFlowText.setBounds(10, 10, 780, 580);

				dialog.getContentPane().add(opticalFlowText);
				dialog.pack();
				dialog.setVisible(true);

			}

			@Override
			public void mouseEntered(MouseEvent e) {
				opticalFlowHelp.setForeground(new Color(255, 74, 28));
			}

			@Override
			public void mouseExited(MouseEvent e) {
				opticalFlowHelp.setForeground(new Color(0, 0, 0));
			}
		});
		opticalFlowHelp.setBounds(210, 240, 15, 16);
		opticalFlowParametersPanel.add(opticalFlowHelp);
		opticalFlowHelp.setFont(new Font("Roboto", Font.BOLD, 20));

		// preview button

		JLabel lblStep = new JLabel("STEP 2");
		lblStep.setVerticalAlignment(SwingConstants.TOP);
		lblStep.setHorizontalAlignment(SwingConstants.LEFT);
		lblStep.setForeground(Color.DARK_GRAY);
		lblStep.setFont(new Font("Roboto", Font.BOLD, 15));
		lblStep.setBounds(10, 50, 53, 30);
		add(lblStep);

		JLabel lblset = new JLabel(
				"<html>Define the optical flow parameters and specify the target number of superpixels used for computing the motion tracks. Change the downsize factor if you wish to work with a smaller version of the image in the next step.</html>");
		lblset.setVerticalAlignment(SwingConstants.TOP);
		lblset.setHorizontalAlignment(SwingConstants.LEFT);
		lblset.setForeground(Color.DARK_GRAY);
		lblset.setFont(new Font("Roboto", Font.PLAIN, 15));
		lblset.setBounds(65, 50, 420, 78);
		add(lblset);

		JButton previewButton = new JButton("Preview");
		previewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				double downsizeFactor = Double.parseDouble(downsizeFactorField.getText());
				int numberSuperpixels = Integer.parseInt(numberSuperpixelsField.getText());

				ImagePlus currentImage = IJ.openImage(Globals.filePath);
				currentImage.setTitle(Globals.getNameWithoutExtension(Globals.filePath) + "_superpixel_preview_"
						+ "downsize factor=" + downsizeFactor + "_number_superpixels="
						+ Integer.parseInt(numberSuperpixelsField.getText()));
				IJ.run(currentImage, "Size...",
						"width=" + (int) (Globals.width / Math.sqrt(downsizeFactor)) + " height="
								+ (int) (Globals.height / Math.sqrt(downsizeFactor)) + " depth=" + Globals.frames
								+ " constrain average interpolation=Bilinear");

				GeneralPath path = new GeneralPath();
				int imageWidth = currentImage.getWidth();
				int imageHeight = currentImage.getHeight();
				double superpixelSize = Math.sqrt((imageWidth * imageHeight) / numberSuperpixels);

				for (int i = 0; i < imageWidth; i += superpixelSize) {
					path.moveTo(i, 0);
					path.lineTo(i, imageHeight);
				}
				for (int i = 0; i < imageHeight; i += superpixelSize) {
					path.moveTo(0, i);
					path.lineTo(imageWidth, i);
				}

				Roi roi = new ShapeRoi(path);
				roi.setStrokeColor(new Color(255, 255, 255));
				roi.setStrokeWidth(2);

				currentImage.setOverlay(new Overlay(roi));
				parentFrame.ui.show(currentImage);
			}
		});
		previewButton.setVerticalTextPosition(SwingConstants.CENTER);
		previewButton.setHorizontalTextPosition(SwingConstants.CENTER);
		previewButton.setForeground(Color.WHITE);
		previewButton.setFont(new Font("Arial", Font.BOLD, 15));
		previewButton.setBackground(new Color(13, 59, 102));
		previewButton.setBounds(260, 370, 230, 30);
		add(previewButton);

		// layout

		setLayout(null);
		add(titleLabel);
		add(cancelButton);
		add(backButton);
		add(nextButton);
		add(numberSuperpixelsPanel);
		add(opticalFlowParametersPanel);
		opticalFlowParametersPanel.add(levelsLabel);
		opticalFlowParametersPanel.add(polySigmaLabel);
		opticalFlowParametersPanel.add(opticalFlowParameterLabel);
		opticalFlowParametersPanel.add(scaleFactorLabel);
		opticalFlowParametersPanel.add(flagsLabel);
		opticalFlowParametersPanel.add(windowSizeLabel);
		opticalFlowParametersPanel.add(iterationsLabel);
		opticalFlowParametersPanel.add(polyNLabel);
		opticalFlowParametersPanel.add(scaleFactorField);
		opticalFlowParametersPanel.add(levelsField);
		opticalFlowParametersPanel.add(windowSizeField);
		opticalFlowParametersPanel.add(iterationsField);
		opticalFlowParametersPanel.add(polySigmaField);
		opticalFlowParametersPanel.add(flagsField);
		opticalFlowParametersPanel.add(polyNField);
		numberSuperpixelsPanel.add(numberSuperpixelsField);
		numberSuperpixelsPanel.add(numberSuperpixelsLabel);
		downsizeFactorPanel.add(downsizeFactorLabel);
		downsizeFactorPanel.add(downsizeFactorField);

	}
}
