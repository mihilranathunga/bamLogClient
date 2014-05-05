/**
 * 
 */
package agent;

import java.nio.file.Path;

/**
 *
 */
public class LogClientContext {

	private Path fullLogPath;
	private Path fileDirectory;
	private String absLogPath;
	private String fileName;
	private String fileLocation;
	private String fileKey;
	private long currentFilePointer;

	/**
	 * @param fullLogPath
	 * @param fileKey
	 * @param currentFilePointer
	 */
	public LogClientContext(Path fullLogPath, String fileKey, long currentFilePointer) {
		super();
		this.fullLogPath = fullLogPath;
		this.fileKey = fileKey;
		this.currentFilePointer = currentFilePointer;
		setupVariables(fullLogPath);

	}

	/**
	 * @param fullLogPath
	 * @param fileKey
	 */
	public LogClientContext(Path fullLogPath, String fileKey) {
		super();
		this.fullLogPath = fullLogPath;
		this.fileKey = fileKey;
		this.currentFilePointer = 0;
		setupVariables(fullLogPath);
	}
	/**
	 * Initialize other path variables
	 * @param fullLogPath
	 */

	private void setupVariables(Path fullLogPath) {

		this.fileDirectory = fullLogPath.getParent();
		this.absLogPath = fullLogPath.toString();
		this.fileLocation = fullLogPath.getParent().toString();
		this.fileName = fullLogPath.getFileName().toString();

	}

	public boolean hasReadTheFile() {
		if (currentFilePointer != 0) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * @return the fullLogPath
	 */
	public Path getFullLogPath() {
		return fullLogPath;
	}

	/**
	 * @return the fileDirectory
	 */
	public Path getFileDirectory() {
		return fileDirectory;
	}

	/**
	 * @return the absLogPath
	 */
	public String getAbsLogPath() {
		return absLogPath;
	}

	/**
	 * @return the fileName
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * @return the fileLocation
	 */
	public String getFileLocation() {
		return fileLocation;
	}

	/**
	 * @return the fileKey
	 */
	public String getFileKey() {
		return fileKey;
	}

	/**
	 * @return the currentFilePointer
	 */
	public long getCurrentFilePointer() {
		return currentFilePointer;
	}

	/**
	 * @param fullLogPath
	 *            the fullLogPath to set
	 */
	public void setFullLogPath(Path fullLogPath) {
		this.fullLogPath = fullLogPath;
		setupVariables(fullLogPath);
	}

	/**
	 * @param fileKey
	 *            the fileKey to set
	 */
	public void setFileKey(String fileKey) {
		this.fileKey = fileKey;
	}

	/**
	 * @param currentFilePointer
	 *            the currentFilePointer to set
	 */
	public void setCurrentFilePointer(long currentFilePointer) {
		this.currentFilePointer = currentFilePointer;
	}
	
	/**
	 * 
	 */
	
	public String toString(){
		
		return fullLogPath.toString()+" , "+fileDirectory.toString()+" , "+absLogPath+" , "+
		fileName+" , "+fileLocation+" , "+fileKey+" , : "+String.valueOf(currentFilePointer);	
	}

}
