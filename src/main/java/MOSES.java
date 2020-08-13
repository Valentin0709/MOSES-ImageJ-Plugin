import net.imagej.ImageJ;
import net.imagej.display.ImageDisplayService;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.UIService;

import ij.IJ;

@Plugin(type = Command.class, headless = true, menuPath = "Plugins>Tracking>MOSES")
public class MOSES implements Command {
	
	@Parameter
	public UIService ui;
		
	@Parameter
	public ImageDisplayService imageDisplayService;
			
	public MainFrame mainFrame = new MainFrame();
	
	@Override
	public void run() {
		
		
		//set services
		
		mainFrame.setServices(ui, imageDisplayService);
		
		//display menu panel
		
		mainFrame.empty();
		mainFrame.add(mainFrame.menuPanel);
		mainFrame.validate();
				
		SwingUtilities.invokeLater(() -> {
			
			//check if MOSES is installed
			Globals.checkInstallationStatus();
						
			if(Globals.installStatus == 1) {
			
				//check if menu window has already been opened
				
				if (!mainFrame.isVisible()) mainFrame.display();
			}
			else {
				 JFrame dialog = new JFrame();
				 Object[] options = {"Cancel", "Install now"};
				  int n = JOptionPane.showOptionDialog(dialog, "It seems like this is the first time you are using MOSES.", "MOSES", 
						  JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);
				  
				  if(n == 1) {
					  IJ.log("Installing MOSES...");
					  
					  Thread thread2 = new Thread() {
						  
							public void run() {
								String currentPath = getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
							    currentPath = currentPath.substring(1, currentPath.lastIndexOf("/"));
							    currentPath = currentPath.replaceAll("%20"," ");
							    IJ.log(currentPath + "/MOSES");
								
							    ArrayList<String> command = new ArrayList<>();
								command.addAll(Arrays.asList("pip", "install", "-e", currentPath + "/MOSES"));
							    
							    ProcessBuilder pb = new ProcessBuilder(command);
							    
							    try {
									Process p = pb.start();
									BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
									String line;
									while ((line = in.readLine()) != null) {
									    if (line.isEmpty()) {
									        break;
									    }
									    IJ.log(line);
									}
								} 
							    catch (IOException e1) {IJ.handleException(e1);}
							}
					  	};
					  
					  	thread2.start();
					  	
						try {
							thread2.join();
						}
						catch (InterruptedException e1) {IJ.handleException(e1);}
						
				  }
				  
				  Globals.checkInstallationStatus();
					
					if(Globals.installStatus == 1) {
						IJ.log("MOSES successfully installed");
					
						//check if menu window has already been opened
						
						if (!mainFrame.isVisible()) mainFrame.display();
					}
					else IJ.log("Something went wrong");
			}
			
			
		});
		
	}
	
	public static void main(final String... args) {
		final ImageJ ij = new ImageJ();
		ij.launch(args);
		
		ij.command().run(MOSES.class, true);

	}
}
