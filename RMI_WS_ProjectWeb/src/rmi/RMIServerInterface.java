package rmi;
// file: SomeInterface.java
// to be implemented by a Java RMI server class.

import java.rmi.*;
import java.util.List;

public interface RMIServerInterface extends Remote {

    public byte[] downloadFile(String content, String server)
            throws java.rmi.RemoteException;

    public void saveFile(byte[] file, LocalFile f, RMIClientInterface cinter)
            throws java.rmi.RemoteException;

    public List searchFiles(String tags, String caller)
            throws java.rmi.RemoteException;

    public Boolean deleteFile(String file, String user)
            throws java.rmi.RemoteException;

    public void registerClient(RMIClientInterface client, String userName)
            throws java.rmi.RemoteException;

    public void disconnect(RMIClientInterface client)
            throws java.rmi.RemoteException;

    public void registerServer(RMIServerInterface server, String Name)
            throws java.rmi.RemoteException;
}
