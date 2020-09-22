import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AnnotationMetadata {
	private String annotationFilePath, parentFile, timestamp;
	private int numberAnnotations;

	public AnnotationMetadata(String path) {
		annotationFilePath = path;

		BufferedReader csvReader;
		try {
			csvReader = new BufferedReader(new FileReader(annotationFilePath));
			String row = csvReader.readLine();
			String[] data = row.split(",");
			csvReader.close();

			parentFile = data[0];
			numberAnnotations = Integer.parseInt(data[1]);
			timestamp = data[2];

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String getParentFile() {
		return parentFile;
	}

	public int getNumberAnnotations() {
		return numberAnnotations;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public List<String> metadataList() {
		List<String> result = new ArrayList<String>();

		result.add("number of annotations : " + numberAnnotations);
		result.add("timestamp : " + timestamp);

		return result;
	}
}
