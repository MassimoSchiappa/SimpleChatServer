/**
 * 
 */
package ms.stjava.cs;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Scanner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Massimo
 *
 * @version 1.0
 * 
 * This class implements a Thread that will manage the chat server life cycle: it's used to stop it
 * 
 */
public class ConsoleMng extends Thread 
{
	// Attribute used to manage the console input
	private Scanner cinput = new Scanner(System.in);
	
	// Log4j logger
	private Logger logger = LogManager.getLogger(ConsoleMng.class);
	
	// Flag to control the thread life cycle: not yet used from outside ...
	private boolean isAlive = true;
	
	// Attribute to manage the Chat Server socket 
	private ServerSocket srvSock = null;
	
	/**
	 * Class Constructor used to receive the reference to the Chat Server Socket
	 * 
	 * @param sock Chat Server Socket
	 */
	ConsoleMng(ServerSocket sock)
	{
		this.srvSock = sock;
	}
	
	/**
	 * This method is used to stop the current thread and
	 * the main thread by closing the server socket
	 */
	public void stopThread()
	{
		logger.info("Console Thread stop has been required!");
		isAlive = false;
		
		try 
		{
			srvSock.close();
		} 
		catch (IOException e) 
		{
			logger.error(e.getMessage());
		}
	}
	
	/**
	 * Life cycle method of the thread: it must manage application life cycle
	 * by closing the application when 'exit' is received by console
	 */
	public void run() 
	{
		// Local Vars
		String cMessage = "";
		logger.info("Console Thread started!");
		
		// -----------------------------------------------------------------------------
		// Console Manager thread life cycle: all the console input are checked:
		// when 'exit' is prompted the stopThread will be called
		// -----------------------------------------------------------------------------
		while(isAlive == true)
		{
			cMessage = cinput.nextLine();
			
			if(cMessage.equals("exit"))
			{
				cinput.close();
				stopThread();
			}
			
		}
		// -----------------------------------------------------------------------------
	}
	

}
