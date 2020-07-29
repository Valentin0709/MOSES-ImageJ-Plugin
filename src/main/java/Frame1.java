import ij.IJ;
import ij.ImagePlus;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import javax.swing.*; 
import java.awt.event.*;
import java.awt.*;
import javax.swing.GroupLayout.Alignment;

public class Frame1 extends Frame {	
	
	public Frame1() {
		
		//initialization
		
		super("MOSES - Menu");
		
		//main panel
		
		JPanel mainPanel = new JPanel();
		mainPanel.setBackground(Globals.color1);
		mainPanel.setPreferredSize(new Dimension(Globals.frameWidth, Globals.frameHight));
		getContentPane().add(mainPanel);
		
		//title 
		
		JLabel titleLabel = new JLabel("Motion Sensing Superpixels", JLabel.CENTER);
		titleLabel.setFont(new Font("Arial Black", Font.BOLD, 25));
		titleLabel.setVerticalTextPosition(JLabel.CENTER);
		titleLabel.setHorizontalTextPosition(JLabel.CENTER);
		
		//Compute superpixel tracks button
		
		JButton superpixelTracksButton = new JButton("Compute superpixel tracks");
		superpixelTracksButton.setFont(new Font("Arial", Font.BOLD, 20));
		superpixelTracksButton.setForeground(Color.WHITE);
		superpixelTracksButton.setVerticalTextPosition(AbstractButton.CENTER);
		superpixelTracksButton.setHorizontalTextPosition(AbstractButton.CENTER);
		superpixelTracksButton.setBackground(Globals.color2);
		superpixelTracksButton.addActionListener(new ActionListener()
		{
		  public void actionPerformed(ActionEvent e) 
		  {			  
			  String filePath = null;    //path to the current active image
			  			  
			  JFrame Dialog = new JFrame();
			  if(imageDisplayService.getActiveDataset() == null) {	//checks if any files are opened
				  
				  	  //display dialog box
				  
					  Object[] options = {"Cancel", "Import now"};
					  int n = JOptionPane.showOptionDialog(Dialog, "No file selected. Please import a TIFF stack to continue.", "MOSES", 
							  JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);
					  
					  //display import file window
					  
					  if(n == 1) {
						  File file = ui.chooseFile(null, "open");
						  filePath = file.getPath();
						  
					  	  ui.show(new ImagePlus(filePath));
					  	  
					  	  IJ.log("Imported " + Globals.getName(filePath));
					  }
			  }
			  else {
				  
				  filePath = imageDisplayService.getActiveDataset().getSource();
				  String extension = Globals.getExtension(filePath);
				  
				  if(!extension.equals(".tiff") && !extension.equals(".tif")) {  //check if current file is TIFF stack
					  
					  //display dialog box
					  
					  Object[] options = {"Cancel", "Import now"};
					  int n = JOptionPane.showOptionDialog(Dialog, "Current selected image has an invalid file format. Please import a TIFF stack to continue.", "MOSES", 
							  JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);
					  
					  //display import file window
					  
					  if(n == 1) {						  
						  File file = ui.chooseFile(null, "open");
						  filePath = file.getPath();
						  
					  	  ui.show(new ImagePlus(filePath));  
					  	  
					      IJ.log("Imported " + Globals.getName(filePath));
					  }
				  }
				  else {
					  
					  final String finalFilePath = filePath;
					  
					  IJ.log("Processing " + Globals.getName(filePath));
					  
					  Frame2 f2 = new Frame2();
					  f2.setUi(ui);
					  f2.setImageDisplayService(imageDisplayService);
					  f2.nameField.setText(Globals.getName(filePath));
					  
					  Thread thread = new Thread() {
						  
							public void run() {
							
								File file = new File("open_file.py"); 
								
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
								  
							    ProcessBuilder pb = new ProcessBuilder("python","open_file.py", finalFilePath);
							    try {
									Process p = pb.start();
									BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
									int ret = new Integer(in.readLine()).intValue();
									f2.framesField.setText(String.valueOf(ret));
									ret = new Integer(in.readLine()).intValue();
									f2.heightField.setText(String.valueOf(ret));
									ret = new Integer(in.readLine()).intValue();
									f2.widthField.setText(String.valueOf(ret));
									ret = new Integer(in.readLine()).intValue();
									f2.channelsField.setText(String.valueOf(ret));
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
					  
					   f2.Show();
					   dispose();
					  
				  }
			  }
		  }
		});
		
		//Layout (generated with WindowBuilder)
		
		GroupLayout gl_mainPanel = new GroupLayout(mainPanel);
		gl_mainPanel.setHorizontalGroup(
			gl_mainPanel.createParallelGroup(Alignment.TRAILING)
				.addGroup(Alignment.LEADING, gl_mainPanel.createSequentialGroup()
					.addGap(97)
					.addComponent(superpixelTracksButton, GroupLayout.PREFERRED_SIZE, 307, GroupLayout.PREFERRED_SIZE)
					.addContainerGap(96, Short.MAX_VALUE))
				.addComponent(titleLabel, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 500, Short.MAX_VALUE)
		);
		gl_mainPanel.setVerticalGroup(
			gl_mainPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_mainPanel.createSequentialGroup()
					.addComponent(titleLabel, GroupLayout.PREFERRED_SIZE, 39, GroupLayout.PREFERRED_SIZE)
					.addGap(106)
					.addComponent(superpixelTracksButton, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
					.addGap(222))
		);
		mainPanel.setLayout(gl_mainPanel);
		
		pack();
	}

}
