/**
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package agent;

import java.io.File;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;

import configuration.Node;

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
	private String hostAddress;
	private String hostName;
	private long currentFilePointer;
	
	private String streamVersion;

	/**
	 * 
	 * @param Node node
	 */
    public LogClientContext(Node node) throws Exception{
	    super();

	    this.fullLogPath = Paths.get(node.getProperty("path"));
	    this.fileKey = node.getName();
	    this.streamVersion = node.getProperty("stream_version");
	    this.currentFilePointer = (new File(this.fullLogPath.toString())).length();
		setupVariables(fullLogPath);
		this.hostAddress = calculateHostAddress();
		
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
	
	public String  calculateHostAddress() throws SocketException, UnknownHostException{
		String hostIp;
		
		 if (getLocalAddress() != null) {
			 hostIp = getLocalAddress().getHostAddress();
	       } else {
	    	   hostIp = "localhost"; // Defaults to localhost
	       }
		 return hostIp;
	}
	
    private static InetAddress getLocalAddress() throws SocketException, UnknownHostException {
        Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces();
        while (ifaces.hasMoreElements()) {
            NetworkInterface iface = ifaces.nextElement();
            Enumeration<InetAddress> addresses = iface.getInetAddresses();

            while (addresses.hasMoreElements()) {
                InetAddress addr = addresses.nextElement();
                if (addr instanceof Inet4Address && !addr.isLoopbackAddress()) {
                    return addr;
                }
            }
        }
        return InetAddress.getLocalHost();
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
	 * @return the hostAddress
	 */
	public String getHostAddress() {
		return hostAddress;
	}

	/**
	 * @return the currentFilePointer
	 */
	public long getCurrentFilePointer() {
		return currentFilePointer;
	}

	/**
	 * @return the streamVersion
	 */
	public String getStreamVersion() {
		return streamVersion;
	}

	/**
	 * Set version of the stream
	 * 
	 * @param streamVersion the streamVersion to set
	 */
	public void setStreamVersion(String streamVersion) {
		this.streamVersion = streamVersion;
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
	 * @param hostAddress the hostAddress to set
	 */
	public void setHostAddress(String hostAddress) {
		this.hostAddress = hostAddress;
	}

	/**
	 * @param currentFilePointer
	 *            the currentFilePointer to set
	 */
	public void setCurrentFilePointer(long currentFilePointer) {
		this.currentFilePointer = currentFilePointer;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
    @Override
    public String toString() {
	    return "LogClientContext [absLogPath=" + absLogPath + ", fileName=" + fileName +
	           ", fileLocation=" + fileLocation + ", fileKey=" + fileKey + ", hostAddress=" +
	           hostAddress + ", currentFilePointer=" + currentFilePointer + ", streamVersion=" +
	           streamVersion + "]";
    }


}
