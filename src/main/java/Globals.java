import java.awt.Color;

public class Globals {
	public static int frameHight = 400, frameWidth = 500;
	public static Color color1 = new Color(252, 252, 252), color2 = new Color(13, 59, 102);
	
	public static String getExtension(String filePath) {   //returns file extension from path
		String fileName = filePath.substring(filePath.lastIndexOf("\\"));
		String extension = fileName.substring(fileName.lastIndexOf("."));
		
		return extension;
	}
	
	public static String getName(String filePath) {   //returns file name from path
		String fileName = filePath.substring(filePath.lastIndexOf("\\"));
		
		return fileName;
	}

}
