import javax.swing.JFrame;
import org.scijava.ui.UIService;
import net.imagej.display.ImageDisplayService;

public class Frame extends JFrame{
	public UIService ui;
	public ImageDisplayService imageDisplayService;
	
	public Frame(String title) {
		super(title);
		setResizable(false);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);	
	}
	
	public void Show() {
		if(!isVisible()) setVisible(true);
	}
	
	public void setUi(UIService ui) {
		this.ui = ui;
	}
	
	public void setImageDisplayService(ImageDisplayService imageDisplayService) {
		this.imageDisplayService = imageDisplayService;
	}

}
