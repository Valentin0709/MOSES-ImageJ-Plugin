import java.awt.Dimension;
import java.awt.Font;
import java.awt.List;

import javax.swing.GroupLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import org.scijava.ui.UIService;

import ij.IJ;
import net.imagej.display.ImageDisplayService;
import javax.swing.JButton;
import java.awt.Color;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.awt.event.ActionEvent;
import javax.swing.JTextField;
import javax.swing.JCheckBox;

public class ComputeTracksPanel2 extends JPanel {
	protected static final int Thread = 0;
	public UIService ui;
	public ImageDisplayService imageDisplayService;
	public MainFrame parentFrame;
	
	//components
	public JLabel nameField, framesField, widthField, heightField, channelsField;
	private JTextField scaleFactorField;
	private JTextField levelsField;
	private JTextField winSizeField;
	private JTextField iterationsField;
	private JTextField polynField;
	private JTextField polysigmaField;
	private JTextField numberSuperpixelsField;
	private JTextField flagsField;
	
	public ComputeTracksPanel2(MainFrame parentFrame) {
		
this.parentFrame = parentFrame;
		
		//set size
		
		this.setPreferredSize(new Dimension(500, 600));
		
		//set background color
		
		this.setBackground(new Color(252, 252, 252));
					
		//title labels
		
		JLabel titleLabel = new JLabel("Compute superpixel tracks", SwingConstants.CENTER);
		titleLabel.setVerticalTextPosition(SwingConstants.CENTER);
		titleLabel.setHorizontalTextPosition(SwingConstants.CENTER);
		titleLabel.setFont(new Font("Arial Black", Font.BOLD, 25));
		
		//save tracks checkbox
		
		JCheckBox saveCheckBox = new JCheckBox("Save computed tracks in MATLAB format");
		saveCheckBox.setFont(new Font("Arial", Font.PLAIN, 12));
		saveCheckBox.setBackground(new Color(252, 252, 252));
		saveCheckBox.setSelected(true);
		
		//next button - generates tracks
		
		JButton btnNext = new JButton("Next");
		btnNext.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				Globals.pyr_scale = Double.parseDouble(scaleFactorField.getText());
				Globals.levels = Integer.parseInt(levelsField.getText());
				Globals.winSize = Integer.parseInt(winSizeField.getText());
				Globals.iterations = Integer.parseInt(iterationsField.getText());
				Globals.polyn = Integer.parseInt(polynField.getText());
				Globals.polysigma = Double.parseDouble(polysigmaField.getText());
				Globals.flags = Integer.parseInt(flagsField.getText());
				Globals.numberSuperpixels = Integer.parseInt(numberSuperpixelsField.getText());
				
				if(saveCheckBox.isEnabled()) { 
					Globals.saveDirectory = IJ.getDirectory("Choose save directory");
					IJ.log(Globals.saveDirectory);
				}
				
				Thread thread = new Thread() {
					  
					public void run() {						
						//create temporary python script file
						
						String temporaryDirectorPath = System.getProperty("java.io.tmpdir");
						String scriptPath = temporaryDirectorPath + "compute_tracks.py";
						File file = new File(scriptPath); 
						
						ArrayList<String> command = new ArrayList<>();
						command.addAll(Arrays.asList("python", scriptPath, Globals.filePath, String.valueOf(Globals.pyr_scale), String.valueOf(Globals.levels), 
								  String.valueOf(Globals.winSize), String.valueOf(Globals.iterations), String.valueOf(Globals.polyn), 
								  String.valueOf(Globals.polysigma), String.valueOf(Globals.flags), String.valueOf(Globals.numberSuperpixels)));
						
						String pythonScript = "import scipy.io as spio\r\n" + 
								"import os\r\n" + 
								"import sys\r\n" + 
								"from MOSES.Utility_Functions.file_io import read_multiimg_PIL\r\n" + 
								"from MOSES.Optical_Flow_Tracking.superpixel_track import compute_grayscale_vid_superpixel_tracks\r\n" + 
								"infile = sys.argv[1]\r\n" + 
								"vidstack = read_multiimg_PIL(infile)\r\n" + 
								"optical_flow_params = dict(pyr_scale = float(sys.argv[2]), levels = int(sys.argv[3]), winsize = int(sys.argv[4]), iterations = int(sys.argv[5]), poly_n = int(sys.argv[6]), poly_sigma = float(sys.argv[7]), flags = int(sys.argv[8]))\r\n" + 
								"n_spixels = int(sys.argv[9])\r\n" +
								"print(\"Finished set up\")\r\n" + 
								"optflow_r, meantracks_r = compute_grayscale_vid_superpixel_tracks(vidstack[:,:,:,0], optical_flow_params, n_spixels)\r\n" + 
								"print(\"Finished extracting superpixel tracks for the 1st channel\")\r\n" + 
								"optflow_g, meantracks_g = compute_grayscale_vid_superpixel_tracks(vidstack[:,:,:,1], optical_flow_params, n_spixels)\r\n" +
								"print(\"Finished extracting superpixel tracks for the 2nd channel\")\r\n";
						
						//save tracks if the checkbox is enabled
						
						if(saveCheckBox.isEnabled()) {
							pythonScript +=  "fname = os.path.split(infile)[-1]\r\n" + 
										     "savetracksmat = (sys.argv[10] + 'meantracks_' + fname).replace('.tif', '.mat')\r\n" + 
										     "spio.savemat(savetracksmat, {'meantracks_r': meantracks_r, 'meantracks_g': meantracks_g})\r\n" +
										     "print(\"Saved tracks\")\r\n";
							
							command.add(Globals.saveDirectory);
						}
												
						try {
							FileWriter writer = new FileWriter(file);
							writer.write(pythonScript);
							writer.close();
						} 
						catch (IOException e2) {IJ.handleException(e2);}

						//run the script
						
					    ProcessBuilder pb = new ProcessBuilder(command);
					
					    IJ.log( "Parameters: " + command);
					    
					    try {
							Process p = pb.start();
							BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
							
							IJ.log(in.readLine());
							IJ.log(in.readLine());
							IJ.log(in.readLine());
						} 
				        catch (IOException e1) {IJ.handleException(e1);} 
					    
					    file.delete();   
					}
			  	};
			  
			  	thread.start();
			  	
				try {
					thread.join();
				} 
				catch (InterruptedException e1) {IJ.handleException(e1);}
			}
		});
		
		btnNext.setVerticalTextPosition(SwingConstants.CENTER);
		btnNext.setHorizontalTextPosition(SwingConstants.CENTER);
		btnNext.setForeground(Color.WHITE);
		btnNext.setFont(new Font("Arial", Font.BOLD, 15));
		btnNext.setBackground(new Color(13, 59, 102));
		
		//cancel button
		
		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//display menuPanel and close current panel
				
				parentFrame.empty();
				parentFrame.getContentPane().add(parentFrame.menuPanel);
				parentFrame.validate();	
			}
		});
		cancelButton.setVerticalTextPosition(SwingConstants.CENTER);
		cancelButton.setHorizontalTextPosition(SwingConstants.CENTER);
		cancelButton.setForeground(Color.WHITE);
		cancelButton.setFont(new Font("Arial", Font.BOLD, 15));
		cancelButton.setBackground(new Color(13, 59, 102));
		
		//back button - return to previous panel
		
		JButton backButton = new JButton("Back");
		backButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//display menuPanel and close current panel
				
				parentFrame.empty();
				parentFrame.getContentPane().add(parentFrame.computeTracksPanel1);
				parentFrame.computeTracksPanel1.updateFields();
				parentFrame.validate();	
			}
		});
		backButton.setVerticalTextPosition(SwingConstants.CENTER);
		backButton.setHorizontalTextPosition(SwingConstants.CENTER);
		backButton.setForeground(Color.WHITE);
		backButton.setFont(new Font("Arial", Font.BOLD, 15));
		backButton.setBackground(new Color(13, 59, 102));
		
		//collect parameters
		
		JLabel lblOpticalFlowParameter = new JLabel("Optical flow parameter", SwingConstants.CENTER);
		lblOpticalFlowParameter.setVerticalTextPosition(SwingConstants.CENTER);
		lblOpticalFlowParameter.setHorizontalTextPosition(SwingConstants.CENTER);
		lblOpticalFlowParameter.setFont(new Font("Arial", Font.BOLD, 18));
		
		JLabel lblScaleFactor = new JLabel("Scale factor:", SwingConstants.LEFT);
		lblScaleFactor.setVerticalTextPosition(SwingConstants.CENTER);
		lblScaleFactor.setHorizontalTextPosition(SwingConstants.CENTER);
		lblScaleFactor.setFont(new Font("Arial", Font.PLAIN, 12));
		
		JLabel lblLevels = new JLabel("Levels:", SwingConstants.LEFT);
		lblLevels.setVerticalTextPosition(SwingConstants.CENTER);
		lblLevels.setHorizontalTextPosition(SwingConstants.CENTER);
		lblLevels.setFont(new Font("Arial", Font.PLAIN, 12));
		
		JLabel lblWindowSize = new JLabel("Window size:", SwingConstants.LEFT);
		lblWindowSize.setVerticalTextPosition(SwingConstants.CENTER);
		lblWindowSize.setHorizontalTextPosition(SwingConstants.CENTER);
		lblWindowSize.setFont(new Font("Arial", Font.PLAIN, 12));
		
		JLabel lblIterations = new JLabel("Iterations:", SwingConstants.LEFT);
		lblIterations.setVerticalTextPosition(SwingConstants.CENTER);
		lblIterations.setHorizontalTextPosition(SwingConstants.CENTER);
		lblIterations.setFont(new Font("Arial", Font.PLAIN, 12));
		
		JLabel lblPolyN = new JLabel("Poly n:", SwingConstants.LEFT);
		lblPolyN.setVerticalTextPosition(SwingConstants.CENTER);
		lblPolyN.setHorizontalTextPosition(SwingConstants.CENTER);
		lblPolyN.setFont(new Font("Arial", Font.PLAIN, 12));
		
		JLabel lblPolySigma = new JLabel("Poly sigma:", SwingConstants.LEFT);
		lblPolySigma.setVerticalTextPosition(SwingConstants.CENTER);
		lblPolySigma.setHorizontalTextPosition(SwingConstants.CENTER);
		lblPolySigma.setFont(new Font("Arial", Font.PLAIN, 12));
		
		scaleFactorField = new JTextField();
		scaleFactorField.setText("0.5");
		scaleFactorField.setFont(new Font("Arial", Font.PLAIN, 12));
		scaleFactorField.setColumns(10);
		
		levelsField = new JTextField();
		levelsField.setText("3");
		levelsField.setFont(new Font("Arial", Font.PLAIN, 12));
		levelsField.setColumns(10);
		
		winSizeField = new JTextField();
		winSizeField.setText("15");
		winSizeField.setFont(new Font("Arial", Font.PLAIN, 12));
		winSizeField.setColumns(10);
		
		iterationsField = new JTextField();
		iterationsField.setText("3");
		iterationsField.setFont(new Font("Arial", Font.PLAIN, 12));
		iterationsField.setColumns(10);
		
		polynField = new JTextField();
		polynField.setText("5");
		polynField.setFont(new Font("Arial", Font.PLAIN, 12));
		polynField.setColumns(10);
		
		polysigmaField = new JTextField();
		polysigmaField.setText("1.2");
		polysigmaField.setFont(new Font("Arial", Font.PLAIN, 12));
		polysigmaField.setColumns(10);
		
		JLabel lblNumberOfSuperpixels = new JLabel("Number of superpixels", SwingConstants.CENTER);
		lblNumberOfSuperpixels.setVerticalTextPosition(SwingConstants.CENTER);
		lblNumberOfSuperpixels.setHorizontalTextPosition(SwingConstants.CENTER);
		lblNumberOfSuperpixels.setFont(new Font("Arial", Font.BOLD, 18));
		
		numberSuperpixelsField = new JTextField();
		numberSuperpixelsField.setText("1000");
		numberSuperpixelsField.setFont(new Font("Arial", Font.PLAIN, 12));
		numberSuperpixelsField.setColumns(10);
		
		JLabel flagsLabel = new JLabel("Flags:", SwingConstants.LEFT);
		flagsLabel.setVerticalTextPosition(SwingConstants.CENTER);
		flagsLabel.setHorizontalTextPosition(SwingConstants.CENTER);
		flagsLabel.setFont(new Font("Arial", Font.PLAIN, 12));
		
		flagsField = new JTextField();
		flagsField.setText("0");
		flagsField.setFont(new Font("Arial", Font.PLAIN, 12));
		flagsField.setColumns(10);
		
		//Layout

		GroupLayout groupLayout = new GroupLayout(this);
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addComponent(titleLabel, GroupLayout.PREFERRED_SIZE, 497, GroupLayout.PREFERRED_SIZE)
						.addGroup(groupLayout.createSequentialGroup()
							.addContainerGap()
							.addComponent(cancelButton, GroupLayout.PREFERRED_SIZE, 140, GroupLayout.PREFERRED_SIZE)
							.addGap(40)
							.addComponent(backButton, GroupLayout.PREFERRED_SIZE, 140, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(btnNext, GroupLayout.PREFERRED_SIZE, 140, GroupLayout.PREFERRED_SIZE))
						.addGroup(groupLayout.createSequentialGroup()
							.addContainerGap()
							.addComponent(lblLevels, GroupLayout.PREFERRED_SIZE, 81, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(levelsField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
						.addGroup(groupLayout.createSequentialGroup()
							.addContainerGap()
							.addComponent(lblWindowSize, GroupLayout.PREFERRED_SIZE, 81, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(winSizeField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
						.addGroup(groupLayout.createSequentialGroup()
							.addContainerGap()
							.addComponent(lblIterations, GroupLayout.PREFERRED_SIZE, 81, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(iterationsField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
						.addGroup(groupLayout.createSequentialGroup()
							.addContainerGap()
							.addComponent(lblPolyN, GroupLayout.PREFERRED_SIZE, 81, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(polynField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
						.addGroup(groupLayout.createSequentialGroup()
							.addContainerGap()
							.addComponent(lblPolySigma, GroupLayout.PREFERRED_SIZE, 81, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(polysigmaField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
						.addGroup(groupLayout.createSequentialGroup()
							.addContainerGap()
							.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
								.addComponent(lblOpticalFlowParameter, GroupLayout.PREFERRED_SIZE, 216, GroupLayout.PREFERRED_SIZE)
								.addGroup(groupLayout.createSequentialGroup()
									.addComponent(lblScaleFactor, GroupLayout.PREFERRED_SIZE, 81, GroupLayout.PREFERRED_SIZE)
									.addPreferredGap(ComponentPlacement.RELATED)
									.addComponent(scaleFactorField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
								.addGroup(groupLayout.createSequentialGroup()
									.addComponent(flagsLabel, GroupLayout.PREFERRED_SIZE, 81, GroupLayout.PREFERRED_SIZE)
									.addPreferredGap(ComponentPlacement.RELATED)
									.addComponent(flagsField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
							.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
								.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
									.addGroup(groupLayout.createSequentialGroup()
										.addGap(18)
										.addComponent(saveCheckBox, GroupLayout.DEFAULT_SIZE, 251, Short.MAX_VALUE)
										.addPreferredGap(ComponentPlacement.RELATED))
									.addGroup(Alignment.TRAILING, groupLayout.createSequentialGroup()
										.addPreferredGap(ComponentPlacement.RELATED)
										.addComponent(lblNumberOfSuperpixels, GroupLayout.PREFERRED_SIZE, 216, GroupLayout.PREFERRED_SIZE)
										.addGap(18)))
								.addGroup(Alignment.TRAILING, groupLayout.createSequentialGroup()
									.addPreferredGap(ComponentPlacement.RELATED)
									.addComponent(numberSuperpixelsField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
									.addGap(74)))))
					.addGap(3))
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addComponent(titleLabel, GroupLayout.PREFERRED_SIZE, 39, GroupLayout.PREFERRED_SIZE)
					.addGap(28)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(45)
							.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
								.addComponent(lblScaleFactor, GroupLayout.PREFERRED_SIZE, 39, GroupLayout.PREFERRED_SIZE)
								.addComponent(scaleFactorField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
							.addPreferredGap(ComponentPlacement.RELATED)
							.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
								.addComponent(lblLevels, GroupLayout.PREFERRED_SIZE, 39, GroupLayout.PREFERRED_SIZE)
								.addComponent(levelsField, GroupLayout.PREFERRED_SIZE, 18, GroupLayout.PREFERRED_SIZE)))
						.addGroup(groupLayout.createSequentialGroup()
							.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
								.addComponent(lblOpticalFlowParameter, GroupLayout.PREFERRED_SIZE, 39, GroupLayout.PREFERRED_SIZE)
								.addGroup(groupLayout.createSequentialGroup()
									.addPreferredGap(ComponentPlacement.RELATED)
									.addComponent(lblNumberOfSuperpixels, GroupLayout.PREFERRED_SIZE, 39, GroupLayout.PREFERRED_SIZE)))
							.addGap(12)
							.addComponent(numberSuperpixelsField, GroupLayout.PREFERRED_SIZE, 18, GroupLayout.PREFERRED_SIZE)))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblWindowSize, GroupLayout.PREFERRED_SIZE, 39, GroupLayout.PREFERRED_SIZE)
						.addComponent(winSizeField, GroupLayout.PREFERRED_SIZE, 18, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblIterations, GroupLayout.PREFERRED_SIZE, 39, GroupLayout.PREFERRED_SIZE)
						.addComponent(iterationsField, GroupLayout.PREFERRED_SIZE, 18, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblPolyN, GroupLayout.PREFERRED_SIZE, 39, GroupLayout.PREFERRED_SIZE)
						.addComponent(polynField, GroupLayout.PREFERRED_SIZE, 18, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblPolySigma, GroupLayout.PREFERRED_SIZE, 39, GroupLayout.PREFERRED_SIZE)
						.addComponent(polysigmaField, GroupLayout.PREFERRED_SIZE, 18, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(flagsLabel, GroupLayout.PREFERRED_SIZE, 39, GroupLayout.PREFERRED_SIZE)
						.addComponent(flagsField, GroupLayout.PREFERRED_SIZE, 18, GroupLayout.PREFERRED_SIZE)
						.addComponent(saveCheckBox, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
					.addPreferredGap(ComponentPlacement.RELATED, 20, Short.MAX_VALUE)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addComponent(cancelButton, GroupLayout.PREFERRED_SIZE, 37, GroupLayout.PREFERRED_SIZE)
						.addComponent(btnNext, GroupLayout.PREFERRED_SIZE, 37, GroupLayout.PREFERRED_SIZE)
						.addComponent(backButton, GroupLayout.PREFERRED_SIZE, 37, GroupLayout.PREFERRED_SIZE))
					.addGap(22))
		);
		setLayout(groupLayout);
	}
	
	public void setServices(UIService ui, ImageDisplayService imageDisplayService) {
		this.ui = ui;
		this.imageDisplayService = imageDisplayService;
	}
	}
