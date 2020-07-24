import net.imagej.ImageJ;

import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

@Plugin(type = Command.class, headless = true, menuPath = "Plugins>Hello, World!")
public class MOSES implements Command {

	@Parameter(type = ItemIO.OUTPUT)
	private String greeting;

	@Override
	public void run() {
		greeting = "Hello!";
	}
	
	public static void main(final String... args) {
		// Launch ImageJ as usual.
		final ImageJ ij = new ImageJ();
		ij.launch(args);

		// Launch our "Hello World" command right away.
		ij.command().run(MOSES.class, true);
	}
}
