public class ProjectImageAnnotationTracks {
	private String projectName, imagePath, annotationPath, trackPath;

	public ProjectImageAnnotationTracks() {
		projectName = trackPath = imagePath = annotationPath = null;
	}

	public void setImagePath(String path) {
		imagePath = path;
	}

	public String getImagePath() {
		return imagePath;
	}

	public void setAnnotationPath(String path) {
		annotationPath = path;
	}

	public String getAnnotationPath() {
		return annotationPath;
	}

	public void setTrackPath(String path) {
		trackPath = path;
	}

	public String getTrackPath() {
		return trackPath;
	}

	public void setProjectName(String path) {
		projectName = path;
	}

	public String getProjectName() {
		return projectName;
	}
}
