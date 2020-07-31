import java.awt.Color;

public class Globals {
	public static int frameHight = 500, frameWidth = 500;
	
	//globals for cell tracking
	public static String fileName, filePath, saveDirectory;
	public static int frames, width, height, channels, numberSuperpixels, levels, winSize, iterations, polyn, flags;
	public static double pyr_scale, polysigma;
	
	//method that returns file extension from file path
	
	public static String getExtension(String filePath) {  
		String fileName = filePath.substring(filePath.lastIndexOf("\\"));
		String extension = fileName.substring(fileName.lastIndexOf("."));
		
		return extension;
	}
	
	//method that returns file name from file path
	
	public static String getName(String filePath) {  
		String fileName = filePath.substring(filePath.lastIndexOf("\\") + 1);
		
		return fileName;
	}

}
