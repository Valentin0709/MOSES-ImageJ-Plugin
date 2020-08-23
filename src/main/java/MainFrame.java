import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;

import javax.swing.JFrame;

import org.scijava.ui.UIService;

import net.imagej.display.ImageDisplayService;

public class MainFrame extends JFrame {

	// services

	public UIService ui;
	public ImageDisplayService imageDisplayService;

	// panels

	public MenuPanel menuPanel = new MenuPanel(this);
	public ComputeTracksPanel1 computeTracksPanel1 = new ComputeTracksPanel1(this);
	public ComputeTracksPanel2 computeTracksPanel2 = new ComputeTracksPanel2(this);
	public ComputeTracksPanel3 computeTracksPanel3 = new ComputeTracksPanel3(this);

	public MainFrame() {

		// set title

		super("MOSES");

		// set size

		this.setPreferredSize(new Dimension(Globals.frameWidth, Globals.frameHight));

		// set location to top-right corner

		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice defaultScreen = ge.getDefaultScreenDevice();
		java.awt.Rectangle rect = defaultScreen.getDefaultConfiguration().getBounds();
		this.setLocation((int) rect.getMaxX() - Globals.frameWidth, 0);

		// set frame properties

		this.setResizable(false);
		this.setVisible(false);
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.getContentPane().setLayout(new CardLayout(0, 0));

		pack();

	}

	public void display() {
		this.setVisible(true);
	}

	public void empty() {
		this.getContentPane().removeAll();
		this.getContentPane().repaint();
	}

	public void setServices(UIService ui, ImageDisplayService imageDisplayService) {
		this.ui = ui;
		this.imageDisplayService = imageDisplayService;
	}

}
