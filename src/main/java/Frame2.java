import java.awt.Dimension;
import javax.swing.JPanel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JLabel;
import java.awt.Font;
import javax.swing.SwingConstants;
import javax.swing.LayoutStyle.ComponentPlacement;
import java.awt.BorderLayout;

public class Frame2 extends Frame{
	
	JLabel nameField, framesField, widthField, heightField, channelsField;
	
	public Frame2() {
		
		//initialization
		
		super("MOSES - Compute superpixel tracks");
				
		//main panel
				
		JPanel mainPanel = new JPanel();
		mainPanel.setBackground(Globals.color1);
		mainPanel.setPreferredSize(new Dimension(Globals.frameWidth, Globals.frameHight));
		getContentPane().add(mainPanel, BorderLayout.CENTER);
		
		JLabel titleLabel = new JLabel("Compute superpixel tracks", SwingConstants.CENTER);
		titleLabel.setVerticalTextPosition(SwingConstants.CENTER);
		titleLabel.setHorizontalTextPosition(SwingConstants.CENTER);
		titleLabel.setFont(new Font("Arial Black", Font.BOLD, 25));
		
		JLabel titleLabel2 = new JLabel("File properties", SwingConstants.CENTER);
		titleLabel2.setVerticalTextPosition(SwingConstants.CENTER);
		titleLabel2.setHorizontalTextPosition(SwingConstants.CENTER);
		titleLabel2.setFont(new Font("Arial", Font.BOLD, 23));
		
		JLabel nameLabel = new JLabel("Name:", SwingConstants.LEFT);
		nameLabel.setVerticalTextPosition(SwingConstants.CENTER);
		nameLabel.setHorizontalTextPosition(SwingConstants.CENTER);
		nameLabel.setFont(new Font("Arial", Font.PLAIN, 16));
		
		JLabel framesLabel = new JLabel("Frames:", SwingConstants.LEFT);
		framesLabel.setVerticalTextPosition(SwingConstants.CENTER);
		framesLabel.setHorizontalTextPosition(SwingConstants.CENTER);
		framesLabel.setFont(new Font("Arial", Font.PLAIN, 16));
		
		JLabel widthLabel = new JLabel("Width:", SwingConstants.LEFT);
		widthLabel.setVerticalTextPosition(SwingConstants.CENTER);
		widthLabel.setHorizontalTextPosition(SwingConstants.CENTER);
		widthLabel.setFont(new Font("Arial", Font.PLAIN, 16));
		
		JLabel heightLabel = new JLabel("Height:", SwingConstants.LEFT);
		heightLabel.setVerticalTextPosition(SwingConstants.CENTER);
		heightLabel.setHorizontalTextPosition(SwingConstants.CENTER);
		heightLabel.setFont(new Font("Arial", Font.PLAIN, 16));
		
		JLabel channelsLabel = new JLabel("Channels:", SwingConstants.LEFT);
		channelsLabel.setVerticalTextPosition(SwingConstants.CENTER);
		channelsLabel.setHorizontalTextPosition(SwingConstants.CENTER);
		channelsLabel.setFont(new Font("Arial", Font.PLAIN, 16));
		
		nameField = new JLabel("-", SwingConstants.LEFT);
		nameField.setVerticalTextPosition(SwingConstants.CENTER);
		nameField.setHorizontalTextPosition(SwingConstants.CENTER);
		nameField.setFont(new Font("Arial", Font.PLAIN, 17));
		
		framesField = new JLabel("-", SwingConstants.LEFT);
		framesField.setVerticalTextPosition(SwingConstants.CENTER);
		framesField.setHorizontalTextPosition(SwingConstants.CENTER);
		framesField.setFont(new Font("Arial", Font.PLAIN, 16));
		
		widthField = new JLabel("-", SwingConstants.LEFT);
		widthField.setVerticalTextPosition(SwingConstants.CENTER);
		widthField.setHorizontalTextPosition(SwingConstants.CENTER);
		widthField.setFont(new Font("Arial", Font.PLAIN, 16));
		
		heightField = new JLabel("-", SwingConstants.LEFT);
		heightField.setVerticalTextPosition(SwingConstants.CENTER);
		heightField.setHorizontalTextPosition(SwingConstants.CENTER);
		heightField.setFont(new Font("Arial", Font.PLAIN, 16));
		
		channelsField = new JLabel("-", SwingConstants.LEFT);
		channelsField.setVerticalTextPosition(SwingConstants.CENTER);
		channelsField.setHorizontalTextPosition(SwingConstants.CENTER);
		channelsField.setFont(new Font("Arial", Font.PLAIN, 16));
		
		//Layout
		
		GroupLayout gl_mainPanel = new GroupLayout(mainPanel);
		gl_mainPanel.setHorizontalGroup(
			gl_mainPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_mainPanel.createSequentialGroup()
					.addGroup(gl_mainPanel.createParallelGroup(Alignment.LEADING)
						.addComponent(titleLabel, GroupLayout.PREFERRED_SIZE, 497, GroupLayout.PREFERRED_SIZE)
						.addGroup(gl_mainPanel.createSequentialGroup()
							.addGap(135)
							.addGroup(gl_mainPanel.createParallelGroup(Alignment.LEADING)
								.addGroup(gl_mainPanel.createParallelGroup(Alignment.TRAILING)
									.addComponent(framesLabel, GroupLayout.PREFERRED_SIZE, 106, GroupLayout.PREFERRED_SIZE)
									.addComponent(widthLabel, GroupLayout.PREFERRED_SIZE, 106, GroupLayout.PREFERRED_SIZE)
									.addGroup(gl_mainPanel.createParallelGroup(Alignment.LEADING)
										.addComponent(heightLabel, GroupLayout.PREFERRED_SIZE, 106, GroupLayout.PREFERRED_SIZE)
										.addComponent(channelsLabel, Alignment.TRAILING, GroupLayout.PREFERRED_SIZE, 106, GroupLayout.PREFERRED_SIZE)))
								.addComponent(nameLabel, GroupLayout.PREFERRED_SIZE, 106, GroupLayout.PREFERRED_SIZE))
							.addPreferredGap(ComponentPlacement.RELATED)
							.addGroup(gl_mainPanel.createParallelGroup(Alignment.LEADING, false)
								.addComponent(channelsField, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(heightField, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(widthField, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(framesField, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(nameField, GroupLayout.DEFAULT_SIZE, 164, Short.MAX_VALUE)))
						.addComponent(titleLabel2, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
					.addContainerGap())
		);
		gl_mainPanel.setVerticalGroup(
			gl_mainPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_mainPanel.createSequentialGroup()
					.addComponent(titleLabel, GroupLayout.PREFERRED_SIZE, 39, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addComponent(titleLabel2, GroupLayout.PREFERRED_SIZE, 39, GroupLayout.PREFERRED_SIZE)
					.addGap(27)
					.addGroup(gl_mainPanel.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_mainPanel.createSequentialGroup()
							.addComponent(nameField, GroupLayout.PREFERRED_SIZE, 39, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(framesField, GroupLayout.PREFERRED_SIZE, 39, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(widthField, GroupLayout.PREFERRED_SIZE, 39, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(heightField, GroupLayout.PREFERRED_SIZE, 39, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(channelsField, GroupLayout.PREFERRED_SIZE, 39, GroupLayout.PREFERRED_SIZE))
						.addGroup(gl_mainPanel.createSequentialGroup()
							.addComponent(nameLabel, GroupLayout.PREFERRED_SIZE, 39, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(framesLabel, GroupLayout.PREFERRED_SIZE, 39, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(widthLabel, GroupLayout.PREFERRED_SIZE, 39, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(heightLabel, GroupLayout.PREFERRED_SIZE, 39, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(channelsLabel, GroupLayout.PREFERRED_SIZE, 39, GroupLayout.PREFERRED_SIZE))))
		);
		mainPanel.setLayout(gl_mainPanel);
		
		pack();
	}
}
