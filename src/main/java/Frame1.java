import org.scijava.ui.UIService;
import io.scif.services.DatasetIOService;
import net.imagej.Dataset;
import net.imagej.display.ImageDisplayService;

import java.io.File;
import java.io.IOException;
import javax.swing.*; 
import java.awt.event.*;
import java.awt.*;

public class Frame1 extends JFrame {	
	private UIService ui;
	private DatasetIOService datasetIOService;
	private ImageDisplayService imageDisplayService;
	
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
					  File file = ui.chooseFile(null, "open");
					  Dataset dataset;
					  try {
								dataset = datasetIOService.open(file.getPath());
								ui.show(dataset);
					   }
					   catch (IOException e1) {
								e1.printStackTrace();
					   }
			  }
		  }
		});
		add(superpixelTracksButton);
	}
	
	public void Show() {
		if(!isVisible()) setVisible(true);
	}
	
	public void setUi(final UIService ui) {
		this.ui = ui;
	}
	
	public void setDatasetIOService(final DatasetIOService datasetIOService) {
		this.datasetIOService = datasetIOService;
	}
	
	public void setImageDisplayService(final ImageDisplayService imageDisplayService) {
		this.imageDisplayService = imageDisplayService;
	}
}
