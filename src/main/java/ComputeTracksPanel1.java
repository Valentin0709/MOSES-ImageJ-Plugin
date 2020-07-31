import java.awt.Dimension;
import java.awt.Font;
import javax.swing.GroupLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import org.scijava.ui.UIService;
import net.imagej.display.ImageDisplayService;
import javax.swing.JButton;
import java.awt.Color;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class ComputeTracksPanel1 extends JPanel {
	public UIService ui;
	public ImageDisplayService imageDisplayService;
	public MainFrame parentFrame;
	
	JLabel nameField, heightField, widthField, framesField, channelsField;
	
	public ComputeTracksPanel1(MainFrame parentFrame) {
			
		this.parentFrame = parentFrame;
		
		//set size
		
		this.setPreferredSize(new Dimension(500, 600));
		
		//set background color
		
		this.setBackground(new Color(252, 252, 252));
					
		//title labels
			
		JLabel titleLabel = new JLabel("Compute superpixel tracks", SwingConstants.CENTER);
		titleLabel.setVerticalTextPosition(SwingConstants.CENTER);
		titleLabel.setHorizontalTextPosition(SwingConstants.CENTER);
		titleLabel.setFont(new Font("Arial Black", Font.BOLD, 25));
					
		JLabel titleLabel2 = new JLabel("File properties", SwingConstants.CENTER);
		titleLabel2.setVerticalTextPosition(SwingConstants.CENTER);
		titleLabel2.setHorizontalTextPosition(SwingConstants.CENTER);
		titleLabel2.setFont(new Font("Arial", Font.BOLD, 18));
			
		//file properties list
		
		JLabel nameLabel = new JLabel("Name:", SwingConstants.LEFT);
		nameLabel.setVerticalTextPosition(SwingConstants.CENTER);
		nameLabel.setHorizontalTextPosition(SwingConstants.CENTER);
		nameLabel.setFont(new Font("Arial", Font.PLAIN, 12));
			
		JLabel framesLabel = new JLabel("Frames:", SwingConstants.LEFT);
		framesLabel.setVerticalTextPosition(SwingConstants.CENTER);
		framesLabel.setHorizontalTextPosition(SwingConstants.CENTER);
		framesLabel.setFont(new Font("Arial", Font.PLAIN, 12));
			
		JLabel widthLabel = new JLabel("Width:", SwingConstants.LEFT);
		widthLabel.setVerticalTextPosition(SwingConstants.CENTER);
		widthLabel.setHorizontalTextPosition(SwingConstants.CENTER);
		widthLabel.setFont(new Font("Arial", Font.PLAIN, 12));
			
		JLabel heightLabel = new JLabel("Height:", SwingConstants.LEFT);
		heightLabel.setVerticalTextPosition(SwingConstants.CENTER);
		heightLabel.setHorizontalTextPosition(SwingConstants.CENTER);
		heightLabel.setFont(new Font("Arial", Font.PLAIN, 12));
			
		JLabel channelsLabel = new JLabel("Channels:", SwingConstants.LEFT);
		channelsLabel.setVerticalTextPosition(SwingConstants.CENTER);
		channelsLabel.setHorizontalTextPosition(SwingConstants.CENTER);
		channelsLabel.setFont(new Font("Arial", Font.PLAIN, 12));
			
		nameField = new JLabel("-", SwingConstants.LEFT);
		nameField.setText(Globals.fileName);
		nameField.setVerticalTextPosition(SwingConstants.CENTER);
		nameField.setHorizontalTextPosition(SwingConstants.CENTER);
		nameField.setFont(new Font("Arial", Font.PLAIN, 12));
			
		framesField = new JLabel("-", SwingConstants.LEFT);
		framesField.setVerticalTextPosition(SwingConstants.CENTER);
		framesField.setHorizontalTextPosition(SwingConstants.CENTER);
		framesField.setFont(new Font("Arial", Font.PLAIN, 12));
			
		widthField = new JLabel("-", SwingConstants.LEFT);
		widthField.setVerticalTextPosition(SwingConstants.CENTER);
		widthField.setHorizontalTextPosition(SwingConstants.CENTER);
		widthField.setFont(new Font("Arial", Font.PLAIN, 12));
			
		heightField = new JLabel("-", SwingConstants.LEFT);
		heightField.setVerticalTextPosition(SwingConstants.CENTER);
		heightField.setHorizontalTextPosition(SwingConstants.CENTER);
		heightField.setFont(new Font("Arial", Font.PLAIN, 12));
			
		channelsField = new JLabel("-", SwingConstants.LEFT);
		channelsField.setVerticalTextPosition(SwingConstants.CENTER);
		channelsField.setHorizontalTextPosition(SwingConstants.CENTER);
		channelsField.setFont(new Font("Arial", Font.PLAIN, 12));
		
		//Next step button
		
		JButton nextStepButton = new JButton("Next");
		nextStepButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//display computeTracksPanel2 and close current panel
				
				parentFrame.empty();
				parentFrame.getContentPane().add(parentFrame.computeTracksPanel2);
				parentFrame.validate();	
			}
		});
		nextStepButton.setVerticalTextPosition(SwingConstants.CENTER);
		nextStepButton.setHorizontalTextPosition(SwingConstants.CENTER);
		nextStepButton.setForeground(Color.WHITE);
		nextStepButton.setFont(new Font("Arial", Font.BOLD, 15));
		nextStepButton.setBackground(new Color(13, 59, 102));
		
		//Cancel button
		
		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//display menuPanel and close current panel
				
				parentFrame.empty();
				parentFrame.getContentPane().add(parentFrame.menuPanel);
				parentFrame.validate();	
			}
		});
		cancelButton.setVerticalTextPosition(SwingConstants.CENTER);
		cancelButton.setHorizontalTextPosition(SwingConstants.CENTER);
		cancelButton.setForeground(Color.WHITE);
		cancelButton.setFont(new Font("Arial", Font.BOLD, 15));
		cancelButton.setBackground(new Color(13, 59, 102));
			
		//Layout
			
		GroupLayout gl_mainPanel = new GroupLayout(this);
		gl_mainPanel.setHorizontalGroup(
			gl_mainPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_mainPanel.createSequentialGroup()
					.addComponent(titleLabel, GroupLayout.PREFERRED_SIZE, 497, GroupLayout.PREFERRED_SIZE)
					.addContainerGap())
				.addGroup(gl_mainPanel.createSequentialGroup()
					.addGap(36)
					.addGroup(gl_mainPanel.createParallelGroup(Alignment.LEADING)
						.addComponent(titleLabel2, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 297, Short.MAX_VALUE)
						.addGroup(gl_mainPanel.createSequentialGroup()
							.addGroup(gl_mainPanel.createParallelGroup(Alignment.LEADING)
								.addGroup(gl_mainPanel.createParallelGroup(Alignment.TRAILING)
									.addGroup(gl_mainPanel.createParallelGroup(Alignment.TRAILING)
										.addComponent(framesLabel, GroupLayout.PREFERRED_SIZE, 57, GroupLayout.PREFERRED_SIZE)
										.addComponent(widthLabel, Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 57, GroupLayout.PREFERRED_SIZE))
									.addComponent(heightLabel, GroupLayout.PREFERRED_SIZE, 57, GroupLayout.PREFERRED_SIZE)
									.addComponent(nameLabel, GroupLayout.PREFERRED_SIZE, 57, GroupLayout.PREFERRED_SIZE))
								.addComponent(channelsLabel))
							.addGap(18)
							.addGroup(gl_mainPanel.createParallelGroup(Alignment.LEADING)
								.addComponent(nameField, GroupLayout.DEFAULT_SIZE, 222, Short.MAX_VALUE)
								.addComponent(framesField, GroupLayout.DEFAULT_SIZE, 222, Short.MAX_VALUE)
								.addComponent(widthField, GroupLayout.DEFAULT_SIZE, 222, Short.MAX_VALUE)
								.addComponent(heightField, GroupLayout.DEFAULT_SIZE, 222, Short.MAX_VALUE)
								.addComponent(channelsField, GroupLayout.DEFAULT_SIZE, 222, Short.MAX_VALUE))))
					.addGap(176))
				.addGroup(gl_mainPanel.createSequentialGroup()
					.addContainerGap()
					.addComponent(cancelButton, GroupLayout.PREFERRED_SIZE, 140, GroupLayout.PREFERRED_SIZE)
					.addGap(194)
					.addComponent(nextStepButton, GroupLayout.PREFERRED_SIZE, 140, GroupLayout.PREFERRED_SIZE)
					.addGap(23))
		);
		gl_mainPanel.setVerticalGroup(
			gl_mainPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_mainPanel.createSequentialGroup()
					.addComponent(titleLabel, GroupLayout.PREFERRED_SIZE, 39, GroupLayout.PREFERRED_SIZE)
					.addGap(33)
					.addComponent(titleLabel2, GroupLayout.PREFERRED_SIZE, 39, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_mainPanel.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_mainPanel.createSequentialGroup()
							.addComponent(nameLabel, GroupLayout.PREFERRED_SIZE, 39, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(framesLabel, GroupLayout.PREFERRED_SIZE, 39, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(widthLabel, GroupLayout.PREFERRED_SIZE, 39, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(heightLabel, GroupLayout.PREFERRED_SIZE, 39, GroupLayout.PREFERRED_SIZE))
						.addGroup(gl_mainPanel.createSequentialGroup()
							.addGap(180)
							.addComponent(channelsLabel, GroupLayout.PREFERRED_SIZE, 39, GroupLayout.PREFERRED_SIZE))
						.addGroup(gl_mainPanel.createSequentialGroup()
							.addComponent(nameField, GroupLayout.PREFERRED_SIZE, 39, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(framesField, GroupLayout.PREFERRED_SIZE, 39, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(widthField, GroupLayout.PREFERRED_SIZE, 39, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(heightField, GroupLayout.PREFERRED_SIZE, 39, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(channelsField, GroupLayout.PREFERRED_SIZE, 39, GroupLayout.PREFERRED_SIZE)))
					.addPreferredGap(ComponentPlacement.RELATED, 207, Short.MAX_VALUE)
					.addGroup(gl_mainPanel.createParallelGroup(Alignment.BASELINE)
						.addComponent(cancelButton, GroupLayout.PREFERRED_SIZE, 37, GroupLayout.PREFERRED_SIZE)
						.addComponent(nextStepButton, GroupLayout.PREFERRED_SIZE, 37, GroupLayout.PREFERRED_SIZE))
					.addGap(20))
		);
		this.setLayout(gl_mainPanel);		
	}
	
	public void setServices(UIService ui, ImageDisplayService imageDisplayService) {
		this.ui = ui;
		this.imageDisplayService = imageDisplayService;
	}
	
	public void updateFields() {
		nameField.setText(Globals.fileName);
		framesField.setText(String.valueOf(Globals.frames));
		widthField.setText(String.valueOf(Globals.width));
		heightField.setText(String.valueOf(Globals.height));
		channelsField.setText(String.valueOf(Globals.channels));
	}
	
	}
