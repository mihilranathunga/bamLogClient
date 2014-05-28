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

package logClient;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.PropertyConfigurator;

import utils.Constants;
import utils.fileutils.FileManager;
import agent.BamDataSender;
import agent.FileTracker;
import agent.LogClientContext;
import configuration.Node;
import configuration.parser.nginx.NodeBuilder;

public class LogClient {

	public static Node rootNode;
	public static Node logsNode;
	public static Node bamNode;
	public static String hostName;
	
	private static final Log log = LogFactory.getLog(LogClient.class);

	public static void main(String[] args) {
		
		PropertyConfigurator.configure(Constants.LOG4J_PROPERTY_PATH);
		
        System.setProperty("javax.net.ssl.trustStore", Constants.TRUST_STORE_PATH);
        System.setProperty("javax.net.ssl.trustStorePassword", Constants.TRUST_STORE_PASSWORD);

		LogClient logClient = new LogClient();
		logClient.processConf();
		logClient.setupBam();
		logClient.getPathList();
		
		try {
			FileTracker watchService = new FileTracker(FileTracker.regPaths);
			watchService.processEvents();
		} catch (IOException e) {
			log.fatal("Error initializing file watch client -"+e.getMessage(), e);
			log.fatal("Log client is Exiting..");
        	System.exit(-1);
		}
	}

	private void processConf() {
		Node configuration = new Node();
		configuration.setName("conf");

		try {
			rootNode = NodeBuilder.buildNode(configuration, FileManager.readFile(Constants.LOGCLIENT_CONF_PATH));
			logsNode = rootNode.findChildNodeByName("logs");
			bamNode  = rootNode.findChildNodeByName("bam_configuration");
			hostName = logsNode.getProperty("host_name");
			log.debug(logsNode);
			log.debug(bamNode);
		} catch (IOException e) {
			log.error("Error processing configuration file - "+e.getMessage(), e);
			log.fatal("Log client is Exiting..");
        	System.exit(-1);
		}
	}

	private void getPathList() {
		for (Node file : logsNode.getChildNodes()) {

			LogClientContext context = null;
			
			try {
	            context = new LogClientContext(file);
            } catch (Exception e) {
            	log.error("Error creating context for the given log file - "+e.getMessage(), e);
            	continue;
            }

		    // this table has context details of each log file with full file path as the key
			FileTracker.putContextToList(context.getAbsLogPath(), context);
			// This list only contains the file locations (parent directories)
			FileTracker.regPaths.add(context.getFileDirectory());
			
			//create Stream
			setupStreams(context);
		}
	}
	
	private void setupBam() {
		
		try {
	        BamDataSender.setupBamAgent(bamNode);
        } catch (Exception e) {
	        log.error("Error setting up Bam publisher - "+e.getMessage(), e);
	        log.fatal("Log client is Exiting..");
	        BamDataSender.stopPublishing();
        	System.exit(-1);
        }
	    
    }
	private void setupStreams(LogClientContext context){
		

	        try {
	            BamDataSender.createStreams(context);
            } catch (Exception e) {
	            log.error("Error setting up streams - "+e.getMessage(), e);
	            log.fatal("Log client is Exiting..");
	        	BamDataSender.stopPublishing();
	        	System.exit(-1);
            } 
       
	}

}
