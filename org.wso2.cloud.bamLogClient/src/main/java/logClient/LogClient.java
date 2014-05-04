package logClient;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import utils.fileutils.FileManager;

import configuration.Node;
import configuration.parser.nginx.NodeBuilder;

import agent.WatchDir;

public class LogClient {

	public static Node rootNode;
	public static Node logsNode;
	
	
	public static void main(String[] args) {
		
		LogClient log = new LogClient();
		log.processConf();
		log.getPathList();
		
		try {
	        WatchDir watchService = new WatchDir(WatchDir.regPaths);
	        watchService.processEvents();
        } catch (IOException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
        }

	}
	
	private void processConf(){
		
		Node configuration = new Node();
		configuration.setName("conf");
		
		try {
	        rootNode = NodeBuilder.buildNode(configuration, FileManager.readFile("logClient.conf"));
	        logsNode = rootNode.findChildNodeByName("logs");
	        System.out.println(logsNode);
        } catch (IOException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
        }	
	}
	private void getPathList(){
		for(Map.Entry<String, String> properties: logsNode.getProperties().entrySet()){
			
			 Path p = Paths.get(properties.getValue());

			 // This list has names of the files with their path
	            WatchDir.absFileList.add(p.toString());
	         // This list only contains the file locations (parent directories)
	            WatchDir.regPaths.add(p.getParent());
	         // This list contains filenames with their absolutes paths to the file key which is used to identify
	         // the given path
	            WatchDir.fileKeys.put(p.toString(), properties.getKey());
		}
	}

}
