public class ImageMaskTracks {
	private String imagePath, maskPath, trackPath;

	public ImageMaskTracks() {
		trackPath = imagePath = maskPath = null;
	}

	public void setImagePath(String path) {
		imagePath = path;
	}

	public String getImagePath() {
		return imagePath;
	}

	public void setMaskPath(String path) {
		maskPath = path;
	}

	public String getMaskPath() {
		return maskPath;
	}

	public void setTrackPath(String path) {
		trackPath = path;
	}

	public String getTrackPath() {
		return trackPath;
	}
}
