import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.UIService;

import net.imagej.ImageJ;
import net.imagej.display.ImageDisplayService;

@Plugin(type = Command.class, headless = true, menuPath = "Plugins>Tracking>MOSES")
public class MOSES implements Command {

	@Parameter
	public UIService ui;

	@Parameter
	public ImageDisplayService imageDisplayService;

	public MainFrame mainFrame = new MainFrame();
	public InstallWindow installWindow = new InstallWindow(mainFrame);

	@Override
	public void run() {

		// set services

		mainFrame.setServices(ui, imageDisplayService);

		// display menu panel

		mainFrame.empty();
		mainFrame.add(mainFrame.menuPanel);
		mainFrame.validate();

		SwingUtilities.invokeLater(() -> {

			if (Globals.checkPythonInstallationStatus()) {

				// check if MOSES is installed
				if (Globals.checkMOSESInstallationStatus()) {
					if (!mainFrame.isVisible())
						mainFrame.display();
				} else {
					if (!installWindow.isVisible()) {
						installWindow.display();
					}
				}
			} else {
				JFrame dialog = new JFrame();
				Object[] options = { "Cancel", "Install now" };
				int n = JOptionPane.showOptionDialog(dialog,
						"MOSES requires Python to run properly. Please install the lastest version of Python and don't forget to select the 'Add Python to PATH' checkbox during setup",
						"MOSES", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);

				if (n == 1) {
					dialog.dispose();

					if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
						try {
							Desktop.getDesktop().browse(new URI("https://www.python.org/downloads/"));
						} catch (IOException | URISyntaxException e) {
							e.printStackTrace();
						}
					}

				}

			}

		});

	}

	public static void main(final String... args) {
		final ImageJ ij = new ImageJ();
		ij.launch(args);

		ij.command().run(MOSES.class, true);

	}
}
