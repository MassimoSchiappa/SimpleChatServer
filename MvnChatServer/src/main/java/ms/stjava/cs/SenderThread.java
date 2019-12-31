/**
 * 
 */
package ms.stjava.cs;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Massimo
 * 
 * @version 1.0
 *
 * This class implements a Thread that will manage the messages from the clients
 * sending them to all the destinations
 * 
 */
public class SenderThread extends Thread 
{
	// Log4j logger
	private Logger logger = LogManager.getLogger(SenderThread.class);
	
	// Attribute used to keep a refernce to the messages list
	private Map<Socket,String> clientMessages = null;
	
	// List of all the connected clients (possible destinations)
	private List<Socket> sockDestClients = null;
	
	// Temporary list used to record the disconnected clients to be removed from the specific list
	private List<Integer> discClients = new ArrayList<Integer>();
	
	// Flag to control the thread life cycle: not yet used from outside ...
	private boolean isAlive = true;
	
	/**
	 * Class constructor
	 */
	public SenderThread() 
	{ 
        super(); 
    } 
	
	/**
	 * Method created to manage the thread life cycle: it is not yet used from outside ...
	 */
	public void stopThread()
	{
		logger.info("Sender Thread stop has been required!");
		isAlive = false;
	}
	
	/**
	 * Life cycle method of the thread: it must manage the messages received by all the clients
	 * by sending them to all the destinations (other  clients)
	 */
	public void run() 
	{
		logger.info("Sender Thread started!");
		
		// -----------------------------------------------------------------------------
		// Sender thread life cycle: all the received messages must be sent to all the 
		// the connected clients excluding the sender
		// -----------------------------------------------------------------------------
		while(isAlive == true)
		{
			// Check the messages queue: if no message is found, then a 2 second time is waited
			// before the messages queue is again read
			if(clientMessages.isEmpty() == false)
			{
				logger.debug("Messages found!");
				synchronized (clientMessages)
				{
					logger.debug("Messages loop starting!");
					
					// -----------------------------------------------------------------------------
					// Loop to scan the messages in the list
					// -----------------------------------------------------------------------------
					for (Entry<Socket, String> sockMessage : clientMessages.entrySet() )
					{
						logger.debug("Client list scan starting!");
						
						// -----------------------------------------------------------------------------
						// Loop to scan the possible client destinations
						// -----------------------------------------------------------------------------
						for( int s = 0; s < sockDestClients.size(); s++)
						{
							// Check if the current client destination is still connected
							// otherwise it will be marked to be removed from the list
							if(sockDestClients.get(s).isConnected() == true )
							{
								// Excluding the client sender as a possible destination
								if(sockDestClients.get(s).equals(sockMessage.getKey()) == false)
								{
									OutputStream output = null;
									
									// Getting the message
									try 
									{
										output = sockDestClients.get(s).getOutputStream();
									} 
									catch (IOException e) 
									{
										logger.error(e.getMessage());
									}
									
									// Writing the message to the current destination client
									PrintWriter writer = new PrintWriter(output, true);
									writer.print(sockMessage.getValue());
									writer.flush();
									
									logger.info("Message sent to client: "+sockDestClients.get(s).getRemoteSocketAddress());
									
								}
							}
							else
							{
								discClients.add(s);
							}
						}
						// -----------------------------------------------------------------------------
						
						// Removing processed message
						clientMessages.remove(sockMessage.getKey(),sockMessage.getValue());
						
					}
					// -----------------------------------------------------------------------------
				}
				
				// Removing the disconnected clients from the list
				synchronized (sockDestClients)
				{
					logger.debug("Removing disconnected client from the list");
					for (int s = 0; s < discClients.size(); s++)
					{
						sockDestClients.remove(discClients.get(s).intValue());
					}
				}
				discClients.clear();
			}
			else
			{
				try 
				{
					Thread.sleep(2000);
				} 
				catch (InterruptedException e) 
				{
					logger.error(e.getMessage());
				}
			}
		}
		
		logger.info("Sender Thread has been stopped!");
    }
	
	/**
	 * Constructor for the class, used to keep a reference to messages map and the connected clients list
	 * 
	 * @param messagesList This parameter is the Map for the clients messages
	 * @param sockClients This parameter is the list of connected clients
	 */
	public SenderThread(Map<Socket,String> messagesList, List<Socket> sockClients)
	{
		clientMessages = messagesList;
		sockDestClients = sockClients;
	}
	

}
