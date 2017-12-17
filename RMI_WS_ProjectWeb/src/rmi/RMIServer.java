package rmi;

import java.io.*;
import java.net.MalformedURLException;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

import typeClass.ServerClass;

public class RMIServer {
    static String name;
    static String ip;
    static String port;
            
    public static void main(String args[]) throws IOException {
        File dir = new File("Storage-Server");
        dir.mkdir();
        
        try {
            RMIServerImplementation self = confServer();
            self.registerServer();
        } catch (Exception ex) {
            System.out.println("An error has been found\n" + ex);
        }
    }

    public static RMIServerImplementation confServer() throws RemoteException, 
            NotBoundException, MalformedURLException {
        // Handles the configuration of the srever
        String portNum, IP;
        Scanner reader = new Scanner(System.in);
        
        System.out.println("Enter the name of this Server:");
        name = reader.nextLine();
        
        System.out.println("Enter IP address: (you can write localhost "
                + "to use the default one)");
        ip = reader.nextLine();

        System.out.println("Enter the port of the server:");
        port = reader.nextLine();

        // The hostname of the machine is being set
        System.setProperty("java.rmi.server.hostname", ip);
        RMIServerImplementation exportedObj = new RMIServerImplementation();
        startRegistry(Integer.parseInt(port));
            
        // Registers the object under the name â€œsomeâ€?
        String registryURL = "rmi://" + ip + ":" + port + "/some";
        Naming.rebind(registryURL, exportedObj);
        System.out.println("Server ready.\n");
        exportedObj.server = new ServerClass(name,ip,port);
        return exportedObj;
    }
    
    private static void startRegistry(int RMIPortNum)
            throws RemoteException {
        try {
            Registry registry = LocateRegistry.getRegistry(RMIPortNum);
            registry.list();

        // The above call will throw an exception if the registry does not already exist
        } catch (RemoteException ex) {
            System.out.println(
                    "RMI registry cannot be located at port " + RMIPortNum);
            LocateRegistry.createRegistry(RMIPortNum);
            System.out.println(
                    "RMI registry created at port " + RMIPortNum);
        }
    }
    
}
