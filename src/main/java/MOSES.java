import net.imagej.ImageJ;
import net.imagej.display.ImageDisplayService;
import javax.swing.SwingUtilities;
import org.scijava.command.Command;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.UIService;
import io.scif.services.DatasetIOService;

@Plugin(type = Command.class, headless = true, menuPath = "Plugins>Tracking>MOSES")
public class MOSES implements Command {
	
	@Parameter
	private UIService ui;
	
	@Parameter
	private DatasetIOService datasetIOService ; 
	
	@Parameter
	private ImageDisplayService imageDisplayService;
		
	@Parameter
	private LogService logService;
	
	private static Frame1 mainFrame = null;
	
	@Override
	public void run() {
		SwingUtilities.invokeLater(() -> {
			if (mainFrame == null) {
				mainFrame = new Frame1();
			}

			mainFrame.setUi(ui);
			mainFrame.setImageDisplayService(imageDisplayService);
			
			mainFrame.Show();	
		});
		
	}
	
	public static void main(final String... args) {
		final ImageJ ij = new ImageJ();
		ij.launch(args);
		
		ij.command().run(MOSES.class, true);

	}
}
