package ms.stjava.cs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 
 * @author Massimo
 *
 * @version 1.0
 * 
 * This class implements a Thread that will manage the messages from a specific client
 * connected to the chat server
 */
public class ReceiverThread extends Thread 
{
	// Class attribute used to keep the socket connection for a specific client
	private Socket intSock = null;
	
	// Log4j logger
	private Logger logger = LogManager.getLogger(ReceiverThread.class);
	
	// Class attribute used to share the messages received by a client to be sent to all the others client
	private Map<Socket,String> clientMessages = null;
	
	// Id specific for the connected client: remote client address is used
	public String myId = null;
	
	// Flag to control the thread life cycle: not yet used from outside ...
	private boolean isAlive = true;
	
	/**
	 * Class constructor
	 */
	public ReceiverThread() 
	{ 
        super(); 
    } 
	
	/**
	 * Method created to manage the thread life cycle: it is not yet used from outside ...
	 */
	public void stopThread()
	{
		logger.info(myId+":"+"Receiver Thread stop has been required!");
		isAlive = false;
	}
	
	/**
	 * Life cycle method of the thread: it must manage the messages received by the client connected
	 * by enqueuing the message and the sender information in the provided Map
	 */
	public void run() 
	{
		// Local Vars
		InputStream input = null;
		InputStreamReader iStreamReader = null;
        BufferedReader buffReader = null;
		String msg = null;
		String message = "";
		Date date = null;
		SimpleDateFormat formatter = new SimpleDateFormat("dd/mm/yyyy HH:mm:ss");;
		
		logger.info(myId+":"+"Receiver Thread started!");

		// -----------------------------------------------------------------------------
		// Receiver thread life cycle: all the received messages from the connected client
		// must be put in the provide Map together with the sender ip address
		// -----------------------------------------------------------------------------
		while( isAlive == true)
		{
			try 
			{
				input = intSock.getInputStream();
				iStreamReader = new InputStreamReader(input, "UTF-8");
				buffReader = new BufferedReader(iStreamReader);
				msg = buffReader.readLine();
				
				// The message is cleaned to leave only ascii charcters
				msg = msg.replaceAll("[^\\p{ASCII}]", "");
				
			} 
			catch (IOException e1) 
			{
				logger.error(myId+":"+e1.getMessage());
				input = null;
			}
			
			
			if( msg != null)
			{
				logger.info(myId+":"+"Message received: " + msg);
				
				// The message is formatted to include sender info and date time
				date = new Date(System.currentTimeMillis());
				message = "Sender: " + myId + " Message received at: " + formatter.format(date) + System.lineSeparator()+msg+ System.lineSeparator();
				
				synchronized(clientMessages)
				{
					clientMessages.put(intSock,message);
				}
				logger.info(myId+":"+"Message queued!");
				
				message = null;
				msg = null;
				date = null;
			}
		}
		// -----------------------------------------------------------------------------
		
		// Trying to release the resource used
		try 
		{
			intSock.close();
		} 
		catch (IOException e) 
		{
			logger.error(myId+":"+e.getMessage());
		}
		
		logger.info(myId+":"+"Receiver Thread has been stopped!");
    }
	
	/**
	 * Constructor for the class, used to receive socket and messages map from the caller
	 * 
	 * @param sock This parameter is the socket object used for the connection with the specific client
	 * @param messagesList This parameter is the Map for the clients messages
	 */
	public ReceiverThread(Socket sock, Map<Socket,String> messagesList)
	{
		this.intSock = sock;
		this.myId = sock.getRemoteSocketAddress().toString();
		this.clientMessages = messagesList;
		logger.info(myId+":"+"Receiver Thread has been configured!");
	}
}
