import org.scijava.ui.UIService;
import ij.IJ;
import io.scif.services.DatasetIOService;
import org.scijava.log.LogService;
import net.imagej.Dataset;
import net.imagej.display.ImageDisplayService;
import java.io.File;
import javax.swing.*; 
import java.awt.event.*;
import java.awt.*;

public class Frame1 extends JFrame {	
	private UIService ui;
	private DatasetIOService datasetIOService;
	private ImageDisplayService imageDisplayService;
	private LogService logService;
	
	public Frame1() {
		super("MOSES");
		setSize(300, 100);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		JLabel titleLabel = new JLabel("Motion Sensing Superpixels", JLabel.CENTER);
		titleLabel.setVerticalTextPosition(JLabel.CENTER);
		titleLabel.setHorizontalTextPosition(JLabel.CENTER);
		add(titleLabel);
		
		JButton superpixelTracksButton = new JButton("Compute superpixel tracks");
		superpixelTracksButton.setVerticalTextPosition(AbstractButton.CENTER);
		superpixelTracksButton.setHorizontalTextPosition(AbstractButton.CENTER);
		superpixelTracksButton.addActionListener(new ActionListener()
		{
		  public void actionPerformed(ActionEvent e) 
		  {
			  Dataset image = imageDisplayService.getActiveDataset();
			  
			  //if no image has been opened, ask the client to select an image to proceed
			  
			  if(image == null) {
				  	String filePath = IJ.getFilePath("Select TIFF stack");
				  	IJ.open(filePath);
			  }
		  }
		});
		add(superpixelTracksButton);
	}
	
	public void Show() {
		if(!isVisible()) setVisible(true);
	}
	
	public void setUi(UIService ui) {
		this.ui = ui;
	}
	
	public void setIDatasetIOService(DatasetIOService datasetIOService) {
		this.datasetIOService = datasetIOService;
	}
	
	public void setImageDisplayService(ImageDisplayService imageDisplayService) {
		this.imageDisplayService = imageDisplayService;
	}
	
	public void setLogService(LogService logService) {
		this.logService = logService;
	}
}
