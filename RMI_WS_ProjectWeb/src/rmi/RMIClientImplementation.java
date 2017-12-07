package rmi;
import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;

/**
 * This class implements the remote interface RMIClientInterface.
 */
public class RMIClientImplementation extends UnicastRemoteObject 
        implements RMIClientInterface{
    
    public RMIClientImplementation() throws RemoteException{
        super();
    }

    @Override
    public void sendMessage(String msg) throws RemoteException{
        // Method used for receiving messages from the server
        System.out.println(msg);
    }


}
