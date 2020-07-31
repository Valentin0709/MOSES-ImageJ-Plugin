import net.imagej.ImageJ;
import net.imagej.display.ImageDisplayService;
import javax.swing.SwingUtilities;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.UIService;

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
			
			//check if menu window has already been opened
			
			if (!mainFrame.isVisible()) mainFrame.display();
		});
		
	}
	
	public static void main(final String... args) {
		final ImageJ ij = new ImageJ();
		ij.launch(args);
		
		ij.command().run(MOSES.class, true);

	}
}
