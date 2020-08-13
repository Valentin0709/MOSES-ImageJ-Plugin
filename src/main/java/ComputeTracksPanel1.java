import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;

import javax.imageio.ImageIO;
import javax.swing.GroupLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;
import javax.swing.LayoutStyle.ComponentPlacement;
import org.scijava.ui.UIService;

import ij.IJ;
import net.imagej.display.ImageDisplayService;
import javax.swing.JButton;
import javax.swing.JCheckBox;

import java.awt.Color;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.awt.event.ActionEvent;
import java.awt.SystemColor;

public class ComputeTracksPanel1 extends JPanel {
	public UIService ui;
	public ImageDisplayService imageDisplayService;
	public MainFrame parentFrame;
	
	JLabel nameField, heightField, widthField, framesField, channelsField;
	JPanel selectChannelPanel;
	JCheckBox checkBoxList[];
	
	ComputeTracksPanel1 self = this;
	
	public ComputeTracksPanel1(MainFrame parentFrame) {
			
		this.parentFrame = parentFrame;
		
		//set size
		
		this.setPreferredSize(new Dimension(Globals.frameWidth, Globals.frameHight));
		
		//set background color
		
		this.setBackground(new Color(252, 252, 252));
		
		//set layout
		
		setLayout(null);
					
		//title labels
			
		JLabel titleLabel = new JLabel("Compute superpixel tracks", SwingConstants.CENTER);
		titleLabel.setBounds(0, 0, 500, 36);
		titleLabel.setVerticalTextPosition(SwingConstants.CENTER);
		titleLabel.setHorizontalTextPosition(SwingConstants.CENTER);
		titleLabel.setFont(new Font("Arial Black", Font.BOLD, 25));
					
		JLabel filePropertiesLabel = new JLabel("File properties", SwingConstants.CENTER);
		filePropertiesLabel.setBounds(10, 140, 230, 40);
		filePropertiesLabel.setVerticalTextPosition(SwingConstants.CENTER);
		filePropertiesLabel.setHorizontalTextPosition(SwingConstants.CENTER);
		filePropertiesLabel.setFont(new Font("Roboto", Font.BOLD, 18));
		
		JLabel selectChannelLabel = new JLabel("Select channels", SwingConstants.CENTER);
		selectChannelLabel.setVerticalTextPosition(SwingConstants.CENTER);
		selectChannelLabel.setHorizontalTextPosition(SwingConstants.CENTER);
		selectChannelLabel.setFont(new Font("Arial", Font.BOLD, 18));
		selectChannelLabel.setBounds(260, 140, 230, 40);
		
		//instruction label
		
		JLabel instructionLabel = new JLabel("<html>Check if the file properties match the file you imported. If you want to continue with the current file, select the image channels you wish to use for computing the superpixel motion tracks.</html>");
		instructionLabel.setVerticalAlignment(SwingConstants.TOP);
		instructionLabel.setForeground(Color.DARK_GRAY);
		instructionLabel.setHorizontalAlignment(SwingConstants.LEFT);
		instructionLabel.setFont(new Font("Roboto", Font.PLAIN, 15));
		instructionLabel.setBounds(65, 50, 420, 70);
		
		//file properties panel
		
		JPanel filePropertiesPanel = new JPanel();
		filePropertiesPanel.setBounds(10, 140, 230, 250);
		
		//select channel panel
		
		selectChannelPanel = new JPanel();
		selectChannelPanel.setBounds(260, 140, 230, 150);
			
		//file properties list
		
		JLabel nameLabel = new JLabel("Name:", SwingConstants.LEFT);
		nameLabel.setBounds(20, 190, 70, 20);
		nameLabel.setVerticalTextPosition(SwingConstants.CENTER);
		nameLabel.setHorizontalTextPosition(SwingConstants.CENTER);
		nameLabel.setFont(new Font("Roboto", Font.PLAIN, 15));
			
		JLabel framesLabel = new JLabel("Frames:", SwingConstants.LEFT);
		framesLabel.setBounds(20, 220, 70, 20);
		framesLabel.setVerticalTextPosition(SwingConstants.CENTER);
		framesLabel.setHorizontalTextPosition(SwingConstants.CENTER);
		framesLabel.setFont(new Font("Roboto", Font.PLAIN, 15));
			
		JLabel widthLabel = new JLabel("Width:", SwingConstants.LEFT);
		widthLabel.setBounds(20, 250, 70, 20);
		widthLabel.setVerticalTextPosition(SwingConstants.CENTER);
		widthLabel.setHorizontalTextPosition(SwingConstants.CENTER);
		widthLabel.setFont(new Font("Roboto", Font.PLAIN, 15));
			
		JLabel heightLabel = new JLabel("Height:", SwingConstants.LEFT);
		heightLabel.setBounds(20, 280, 70, 20);
		heightLabel.setVerticalTextPosition(SwingConstants.CENTER);
		heightLabel.setHorizontalTextPosition(SwingConstants.CENTER);
		heightLabel.setFont(new Font("Roboto", Font.PLAIN, 15));
			
		JLabel channelsLabel = new JLabel("Channels:", SwingConstants.LEFT);
		channelsLabel.setBounds(20, 310, 70, 20);
		channelsLabel.setVerticalTextPosition(SwingConstants.CENTER);
		channelsLabel.setHorizontalTextPosition(SwingConstants.CENTER);
		channelsLabel.setFont(new Font("Roboto", Font.PLAIN, 15));
			
		nameField = new JLabel("-", SwingConstants.LEFT);
		nameField.setBounds(100, 190, 130, 20);
		nameField.setVerticalTextPosition(SwingConstants.CENTER);
		nameField.setHorizontalTextPosition(SwingConstants.CENTER);
		nameField.setFont(new Font("Roboto", Font.PLAIN, 15));
			
		framesField = new JLabel("-", SwingConstants.LEFT);
		framesField.setBounds(100, 220, 130, 20);
		framesField.setVerticalTextPosition(SwingConstants.CENTER);
		framesField.setHorizontalTextPosition(SwingConstants.CENTER);
		framesField.setFont(new Font("Roboto", Font.PLAIN, 15));
			
		widthField = new JLabel("-", SwingConstants.LEFT);
		widthField.setBounds(100, 250, 130, 20);
		widthField.setVerticalTextPosition(SwingConstants.CENTER);
		widthField.setHorizontalTextPosition(SwingConstants.CENTER);
		widthField.setFont(new Font("Roboto", Font.PLAIN, 15));
			
		heightField = new JLabel("-", SwingConstants.LEFT);
		heightField.setBounds(100, 280, 130, 20);
		heightField.setVerticalTextPosition(SwingConstants.CENTER);
		heightField.setHorizontalTextPosition(SwingConstants.CENTER);
		heightField.setFont(new Font("Roboto", Font.PLAIN, 15));
			
		channelsField = new JLabel("-", SwingConstants.LEFT);
		channelsField.setBounds(100, 310, 130, 20);
		channelsField.setVerticalTextPosition(SwingConstants.CENTER);
		channelsField.setHorizontalTextPosition(SwingConstants.CENTER);
		channelsField.setFont(new Font("Roboto", Font.PLAIN, 15));
				
		//next step button
		
		JButton nextStepButton = new JButton("Next");
		nextStepButton.setBounds(350, 430, 140, 30);
		nextStepButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				for(int i = 0; i < Globals.channels; i++) {
					if(checkBoxList[i].isSelected()) Globals.activeChannels[i] = 1;
					else  Globals.activeChannels[i] = 0;
				}
				
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
		
		//cancel button
		
		JButton cancelButton = new JButton("Cancel");
		cancelButton.setBounds(10, 430, 140, 30);
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
		
		//add components

		add(titleLabel);
		add(filePropertiesLabel);
		add(framesLabel);
		add(widthLabel);
		add(heightLabel);
		add(nameLabel);
		add(channelsLabel);
		add(nameField);
		add(framesField);
		add(widthField);
		add(heightField);
		add(channelsField);
		add(cancelButton);
		add(nextStepButton);
		add(selectChannelLabel);
		add(filePropertiesPanel);
		filePropertiesPanel.setLayout(null);
		add(instructionLabel);
		self.add(selectChannelPanel);
		selectChannelPanel.setLayout(null);
		
		JButton channelToolButton = new JButton("Channel tool");
		channelToolButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				IJ.run("Channels Tool...");
				IJ.run("Make Composite", "");
			}
		});
		channelToolButton.setVerticalTextPosition(SwingConstants.CENTER);
		channelToolButton.setHorizontalTextPosition(SwingConstants.CENTER);
		channelToolButton.setForeground(Color.WHITE);
		channelToolButton.setFont(new Font("Arial", Font.BOLD, 15));
		channelToolButton.setBackground(new Color(13, 59, 102));
		channelToolButton.setBounds(260, 360, 230, 30);
		add(channelToolButton);
		
		JLabel tipLabel = new JLabel("<html> Tip: Use Fiji's channel tool to view the image channels.</html>");
		tipLabel.setBounds(260, 300, 230, 46);
		add(tipLabel);
		tipLabel.setHorizontalAlignment(SwingConstants.CENTER);
		tipLabel.setForeground(Color.DARK_GRAY);
		tipLabel.setFont(new Font("Roboto", Font.PLAIN, 15));
		
		JLabel stepLabel = new JLabel("STEP 1");
		stepLabel.setVerticalAlignment(SwingConstants.TOP);
		stepLabel.setBounds(10, 50, 53, 30);
		add(stepLabel);
		stepLabel.setHorizontalAlignment(SwingConstants.LEFT);
		stepLabel.setForeground(Color.DARK_GRAY);
		stepLabel.setFont(new Font("Roboto", Font.BOLD, 15));
	}
	
	public void setServices(UIService ui, ImageDisplayService imageDisplayService) {
		self.ui = ui;
		self.imageDisplayService = imageDisplayService;
	}
	
	public void generateCheckBoxList(){
		checkBoxList = new JCheckBox[Globals.channels];
		
		for(int i = 0; i < Globals.channels; i++) {
			checkBoxList[i] = new JCheckBox("channel " + (i + 1));
			checkBoxList[i].setBounds(300, 190 + i * 30, 200, 20);
			checkBoxList[i].setFont(new Font("Roboto", Font.PLAIN, 15));
			checkBoxList[i].setVisible(true);
			self.add(checkBoxList[i]);
		}
		
		self.add(selectChannelPanel);
	}
	
	public void updateFields() {
		nameField.setText(Globals.fileName);
		framesField.setText(String.valueOf(Globals.frames));
		widthField.setText(String.valueOf(Globals.width));
		heightField.setText(String.valueOf(Globals.height));
		channelsField.setText(String.valueOf(Globals.channels));
		
		generateCheckBoxList();
	}
}
