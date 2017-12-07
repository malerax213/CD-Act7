package rmi;


import java.rmi.*;

public interface RMIClientInterface extends Remote {

    public void sendMessage(String msg) throws RemoteException;

} 
