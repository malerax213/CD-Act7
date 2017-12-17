package rmi;
// file: SomeInterface.java
// to be implemented by a Java RMI server class.

import java.net.MalformedURLException;
import java.rmi.*;
import java.util.List;

import typeClass.LocalFile;
import typeClass.ServerClass;
import typeClass.UserClass;

public interface RMIServerInterface extends Remote {

    public byte[] downloadFile(String content)
            throws java.rmi.RemoteException;

    public void saveFile(byte[] file, LocalFile f, RMIClientInterface cinter)
            throws java.rmi.RemoteException;

    public List searchFiles(String tags)
            throws java.rmi.RemoteException;

    public Boolean deleteFile(String file, String user)
            throws java.rmi.RemoteException;
    
    public int deleteFileByTitle(String file)
    		throws java.rmi.RemoteException;

    public void registerClient(RMIClientInterface client, String userName, String pass)
            throws java.rmi.RemoteException;
    
    public int registerUser(UserClass user)
    		throws java.rmi.RemoteException;
    
    public UserClass getUser(String name)
    		throws java.rmi.RemoteException;

    public void disconnect(RMIClientInterface client)
            throws java.rmi.RemoteException;

    public void registerServer()
            throws java.rmi.RemoteException;
    
    public byte[] downloadFileFinal(String title) throws java.rmi.RemoteException;
}
