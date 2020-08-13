import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import javax.swing.AbstractButton;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.GroupLayout.Alignment;
import org.scijava.ui.UIService;
import ij.IJ;
import ij.ImagePlus;
import net.imagej.display.ImageDisplayService;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import net.miginfocom.swing.MigLayout;
import javax.swing.SwingConstants;

public class MenuPanel extends JPanel{
	public UIService ui;
	public ImageDisplayService imageDisplayService;
	public MainFrame parentFrame;
	
	public MenuPanel(MainFrame parentFrame) {
		
		this.parentFrame = parentFrame;
		
		//set size
		
		this.setPreferredSize(new Dimension(Globals.frameWidth, Globals.frameHight));
		
		//set background color
		
		this.setBackground(new Color(252, 252, 252));
		setLayout(null);
		
		//title label
				
		JLabel titleLabel = new JLabel("Motion Sensing Superpixels", JLabel.CENTER);
		titleLabel.setBounds(0, 0, 500, 36);
		titleLabel.setFont(new Font("Arial Black", Font.BOLD, 25));
		titleLabel.setVerticalTextPosition(JLabel.CENTER);
		titleLabel.setHorizontalTextPosition(JLabel.CENTER);
		add(titleLabel);
		
		//compute superpixel tracks button
		
		JButton superpixelTracksButton = new JButton("Compute superpixel tracks");
		superpixelTracksButton.setBounds(87, 123, 327, 59);
		superpixelTracksButton.setFont(new Font("Arial", Font.BOLD, 20));
		superpixelTracksButton.setForeground(Color.WHITE);
		superpixelTracksButton.setVerticalTextPosition(AbstractButton.CENTER);
		superpixelTracksButton.setHorizontalTextPosition(AbstractButton.CENTER);
		superpixelTracksButton.setBackground(new Color(13, 59, 102));
		superpixelTracksButton.addActionListener(new ActionListener()
		{
		  public void actionPerformed(ActionEvent e) 
		  {			  
			  String filePath = null;    //path to the current active image
			   			  
			  JFrame dialog = new JFrame();
			  if(imageDisplayService.getActiveDataset() == null) {	//checks if any files are opened
				  
				  	  //display dialog box
				  
					  Object[] options = {"Cancel", "Import now"};
					  int n = JOptionPane.showOptionDialog(dialog, "No file selected. Please import a TIFF stack to continue.", "MOSES", 
							  JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);
					  
					  //display import file window
					  
					  if(n == 1) Globals.openFile(ui);
			  }
			  else {
				  
				  filePath = imageDisplayService.getActiveDataset().getSource();
				  String extension = Globals.getExtension(filePath);
				  
				  if(!extension.equals(".tiff") && !extension.equals(".tif")) {  //check if current file is TIFF stack
					  
					  //display dialog box
					  
					  Object[] options = {"Cancel", "Import now"};
					  int n = JOptionPane.showOptionDialog(dialog, "Current selected image has an invalid file format. Please import a TIFF stack to continue.", "MOSES", 
							  JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);
					  
					  //display import file window
					  
					  if(n == 1) Globals.openFile(ui);
				  }
				  else {
					  
					  IJ.log("Launched Compute Superpixel Tracks");
					  IJ.log("Processing " + Globals.getName(filePath));
					  
					  final String finalFilePath = filePath;
					  
					  Globals.filePath = filePath;
					  Globals.fileName = Globals.getName(filePath);
					  
					  Thread thread = new Thread() {
						  
							public void run() {
								
								//create temporary python script file
								
								String temporaryDirectorPath = System.getProperty("java.io.tmpdir");
								String scriptPath = temporaryDirectorPath + "open_file.py";
								File file = new File(scriptPath); 
								
								try {
									FileWriter writer = new FileWriter(file);
									writer.write(
											"import sys\r\n" + 
								    		"from MOSES.Utility_Functions.file_io import read_multiimg_PIL\r\n" + 
								    		"\r\n" + 
								    		"infile = sys.argv[1]\r\n" + 
								    		"vidstack = read_multiimg_PIL(infile)\r\n" + 
								    		"n_frame, n_rows, n_cols, n_channels = vidstack.shape\r\n" + 
								    		"\r\n" + 
								    		"print(n_frame)\r\n" + 
								    		"print(n_rows)\r\n" + 
								    		"print(n_cols)\r\n" + 
								    		"print(n_channels)\r\n"
								    		);
									writer.close();
								} 
								catch (IOException e2) {IJ.handleException(e2);}
								  
							    ProcessBuilder pb = new ProcessBuilder("python", scriptPath, finalFilePath);
							    try {
									Process p = pb.start();
									BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
									
									//number of frames
									Globals.frames = new Integer(in.readLine()).intValue();
									
									//image height
									Globals.height = new Integer(in.readLine()).intValue();
								    
								    //image width
									Globals.width = new Integer(in.readLine()).intValue();
									
									//number of channels
									Globals.channels = new Integer(in.readLine()).intValue();
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
					  
						//display computeTracksPanel1 and close current panel
						
						parentFrame.empty();
						parentFrame.getContentPane().add(parentFrame.computeTracksPanel1);
						parentFrame.computeTracksPanel1.updateFields();
						parentFrame.validate();						  
				  }
			  }
		  }
		});
		add(superpixelTracksButton);
		
		JLabel lblNewLabel = new JLabel("Extract and visualize superpixel motion tracks from a TIFF file");
		lblNewLabel.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel.setFont(new Font("Roboto", Font.PLAIN, 15));
		lblNewLabel.setBounds(12, 72, 476, 43);
		add(lblNewLabel);
	}
	
	public void setServices(UIService ui, ImageDisplayService imageDisplayService) {
		this.ui = ui;
		this.imageDisplayService = imageDisplayService;
	}
}
