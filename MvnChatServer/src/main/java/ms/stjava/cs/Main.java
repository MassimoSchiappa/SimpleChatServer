package ms.stjava.cs;

import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.InvalidPropertiesFormatException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;




/**
 * @author massimo schiappa
 * 
 * @version 1.0
 *  
 * This application is a very simple Chat Server
 */
public class Main 
{
	// Properties file default
	final static String conFile = "CSProperties.xml";
	
	// Listening port for the Chat server
	static int port = 1025;
	
	// Listeining port tag in properties file
	final static String portTAG = "serverport";
	
	// List of the clients connected to the Chat server
	public static List<Socket> sockClients = Collections.synchronizedList(new ArrayList<Socket>());
	
	// Map for the messages received by a client to be broadcasted to all the other clients
	public static Map<Socket,String> clientMessages = Collections.synchronizedMap(new HashMap<Socket,String>());
	
	// Logger by Log4j
	private static Logger logger = LogManager.getLogger(Main.class);
	
	/**
	 * 
	 * @param args
	 * 
	 * The main can accept a filename to be used instead of default properties filename
	 */
	public static void main(String[] args) 
	{
		// Local Vars
		String cFile = conFile;
		File tmpFile = null; 
		Properties props = new Properties();
		ServerSocket socketSrvChat = null;
		List<ReceiverThread> sockThreadList = new ArrayList<ReceiverThread>();
		ReceiverThread sockThread = null;
		SenderThread msgSender = null;
		ConsoleMng cns = null;
		Socket sock = null;
		
		logger.info("Chat server is starting ...");
		
		// Input arguments check
		if(args != null && args.length >= 1)
		{
			tmpFile = new File(args[0]);
			
			if(tmpFile.exists() == true)
			{
				cFile = args[0];
			}
		}

		// Loading properties
		try 
		{
			logger.info("Loading conf file "+cFile);
			props.loadFromXML(new FileInputStream(cFile));
		} 
		catch (InvalidPropertiesFormatException e) 
		{
			logger.error(e.getMessage());
			System.exit(-1);
		} 
		catch (FileNotFoundException e) 
		{

			logger.error(e.getMessage());
			System.exit(-1);
		} 
		catch (IOException e) 
		{
			logger.error(e.getMessage());
			System.exit(-1);
		}
		
		// -----------------------------------------------------------------------------
		// Logging the loaded configuration by properties file
		// -----------------------------------------------------------------------------		
		logger.info("*********** Chat Server Properties Begin ***************");	
		
		for (Entry<Object, Object> prop : props.entrySet())
		{
			logger.info(prop.getKey() + ": " + prop.getValue());

		}
		
		logger.info("*********** Chat Server Properties End***************");
		// -----------------------------------------------------------------------------		
		
		// Using the port number in properties file if a valid one is provided:
		// the numbers before 1025 usually are considered by systems
		if( Integer.parseInt(props.getProperty(portTAG))> 1024)
		{
			port = Integer.parseInt(props.getProperty(portTAG));
		}
		
		// Chat Server socket creation
		try 
		{
			socketSrvChat = new ServerSocket(port);
		} 
		catch (IOException e) 
		{
			logger.error(e.getMessage());
			System.exit(-1);
		}
		
		// Starting the Thread that will dispatch messages from a client to all the others
		msgSender = new SenderThread(clientMessages, sockClients);
		msgSender.start();
		
		// Starting a thread that will control the Chat server execution: if 'exit' is received by console the 
		// chat server will be stopped by closing socket
		cns = new ConsoleMng(socketSrvChat);
		cns.start();
		
		
		// -----------------------------------------------------------------------------
		// Chat server life cycle: for each new connection a Receiver Thread is opened and 
		// started: a Receiver Thread is intended to manage the client messages
		// -----------------------------------------------------------------------------
		do
		{
			try 
			{
				sock = socketSrvChat.accept();
				
				sockClients.add(sock);
				sockThread = new ReceiverThread(sock, clientMessages);
				logger.info("Thread Rec "+sockThread.myId+ " created!");
				sockThreadList.add(sockThread);
				sockThread.start();
				logger.info("Thread Rec "+sockThread.myId+ " started!");
				sockThread = null;
				
				
			} 
			catch (IOException e) 
			{
				logger.error(e.getMessage());
				System.exit(-1);
			}
			
		}
		while(true);
		// -----------------------------------------------------------------------------
	
		

	}

}
