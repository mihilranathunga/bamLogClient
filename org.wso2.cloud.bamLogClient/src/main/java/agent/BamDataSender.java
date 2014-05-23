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

import java.io.FileNotFoundException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Hashtable;

import logClient.LogClient;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.databridge.agent.thrift.Agent;
import org.wso2.carbon.databridge.agent.thrift.DataPublisher;
import org.wso2.carbon.databridge.agent.thrift.conf.AgentConfiguration;
import org.wso2.carbon.databridge.agent.thrift.exception.AgentException;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.exception.AuthenticationException;
import org.wso2.carbon.databridge.commons.exception.DifferentStreamDefinitionAlreadyDefinedException;
import org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException;
import org.wso2.carbon.databridge.commons.exception.NoStreamDefinitionExistException;
import org.wso2.carbon.databridge.commons.exception.StreamDefinitionException;
import org.wso2.carbon.databridge.commons.exception.TransportException;

import configuration.Node;

public class BamDataSender {
	
    public static Hashtable<String, String> streamIds = new Hashtable<String, String>();
	private static DataPublisher dataPublisher;
	
	private static final Log log = LogFactory.getLog(BamDataSender.class);
	
    public static void setupBamAgent(Node bamNode) throws SocketException, UnknownHostException, MalformedURLException, AgentException, AuthenticationException, TransportException {
    	
    	 log.debug("Setting up Bam data Publisher");
    	 
         AgentConfiguration agentConfiguration = new AgentConfiguration();
         Agent agent = new Agent(agentConfiguration);
         
         //create data publisher
         log.debug("Bam DataPublisher Creation: "+bamNode.getProperty("data_reciever_url")+" , "+bamNode.getProperty("data_reciever_username")+" , "+bamNode.getProperty("data_reciever_password")+" , "+agent);
        dataPublisher = new DataPublisher(bamNode.getProperty("data_reciever_url"), bamNode.getProperty("data_reciever_username"), bamNode.getProperty("data_reciever_password"), agent);
    }
    
    @SuppressWarnings("deprecation")
    public static void createStreams(LogClientContext context) throws AgentException, StreamDefinitionException, MalformedStreamDefinitionException, DifferentStreamDefinitionAlreadyDefinedException, FileNotFoundException, SocketException, UnknownHostException{
    	
    	log.debug("creating stream for context :"+context.toString());
    	String hostIP = context.getHostAddress();
    	String fileKey = context.getFileKey();
    	String version = context.getStreamVersion();
    	String streamId;
    	String streamName;
    	
    	streamName = "logs_"+hostIP;
         try {
             streamId = dataPublisher.findStream(streamName, version);
             log.info("Stream already defined");
         } catch (NoStreamDefinitionExistException e) {
             streamId = dataPublisher.defineStream("{" +
                     "  'name':'" + streamName+ "'," +
                     "  'version':'" + version + "'," +
                     "  'description': 'logs of "+hostIP+"'," +
                     "	'nickname':	'logs'," +
                     "  'metaData':[" +
                     "          {'name':'hostType','type':'STRING'}" +
                     "  ]," +
                     "  'payloadData':[" +
                     "			{'name':'filekey','type':'STRING'},"+	
                     "			{'name':'host_address','type':'STRING'},"+		
                     "			{'name':'timestamp','type':'STRING'}," +
                     "          {'name':'log','type':'STRING'}" +	
                     "  ]" +
                     "}");
             //Define event stream
         }
         log.info(fileKey+" , "+streamId);
         streamIds.put(fileKey, streamId);
    }

    public static void publishLogEvents(LogClientContext context, String aLog, String timeStamp) {
    	
    	String streamId = null;
		try {
			streamId = streamIds.get(context.getFileKey());
			
	        if (null != streamId && !streamId.isEmpty()) {

	        		Event event = new Event(streamId, System.currentTimeMillis(), new Object[] { "external" }, null, new Object[] { context.getFileKey(), context.getHostAddress(), timeStamp, aLog});
	        		dataPublisher.publish(event);    
	        }
        } catch (AgentException e) {
	        log.error("Error Publishing log events - "+e.getMessage(), e);
        }
	}

    public static void stopPublishing(){
    	dataPublisher.stop();
    }
}
