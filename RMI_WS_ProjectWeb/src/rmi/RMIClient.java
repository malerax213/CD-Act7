package rmi;

import java.io.*;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;
import java.util.Scanner;

public class RMIClient {

    public static void main(String args[]) throws RemoteException, 
            NotBoundException, MalformedURLException, java.net.UnknownHostException {
        
        // This storage will contain the files that the client will be able to upload
        File dir = new File("Storage-Client");
        dir.mkdir();

        RMIServerInterface inter = lookForServer();
        
        Scanner reader = new Scanner(System.in);
        // We're asking this in order to set the hostname of the machine
        System.out.println("Enter the IP of this Machine:");
        String IP = reader.nextLine();
        System.setProperty("java.rmi.server.hostname", IP);
        
        
        RMIClientImplementation cinter = new RMIClientImplementation();

        try {
            // No valid registry at that port.
            Registry registry = LocateRegistry.getRegistry(1099);
            registry.list();
        } catch (RemoteException ex) {
            LocateRegistry.createRegistry(1099);
        }
    
        String registryURL = "rmi://" + IP + ":" + 1099 + "/some";
        Naming.rebind(registryURL, cinter);

        // Client is being registered to the server
        System.out.println("Enter your user Name:");
        String user = reader.nextLine();
        System.out.println("Enter your pass");
        String pass = reader.nextLine();
        inter.registerClient(cinter, user, pass);

        // All the client's actions will be handled on this loop
        while (true) {
            System.out.println("What should I do?(upload file, download file, "
                    + "search files, delete file or stop)");
            String input = reader.nextLine();

            if ("upload file".equals(input)) {
                uploadFileAction(inter, user, cinter);
            } else if ("download file".equals(input)) {
                downloadFileAction(inter);
            } else if ("search files".equals(input)) {
                searchFilesAction(inter);
            } else if ("delete file".equals(input)) {
                deleteFileAction(inter, user);
            } else if ("stop".equals(input)) {
                inter.disconnect(cinter);
                System.exit(0);
            } else {
                System.out.println("Unrecognized order");
            }
        }
        // end try

    }

    public static RMIServerInterface lookForServer() throws RemoteException, 
            NotBoundException, MalformedURLException {
        // Handles the configuration in order to connect with the server
        String portNum;
        String IP;
        Scanner reader = new Scanner(System.in);

        System.out.println("Enter IP address:");
        IP = reader.nextLine();

        System.out.println("Enter the port of the server:");
        portNum = reader.nextLine();

        String registryURL = "rmi://" + IP + ":" + portNum + "/some";
        RMIServerInterface inter = (RMIServerInterface) Naming.lookup(registryURL);
        return inter;
    }

    public static void uploadFileAction(RMIServerInterface inter, String user, 
            RMIClientInterface cinter) throws RemoteException {
        // Uploads the file with a title and tags to the server
        String title, tags;
        Scanner reader = new Scanner(System.in);

        System.out.println("Enter title name:");
        title = reader.nextLine();
        System.out.println("Enter some tags:");
        tags = reader.nextLine();

        File folder = new File("Storage-Client");
        String path = "Storage-Client/";
        File[] listOfFiles = folder.listFiles();
        File objective;
        path = searchFile(listOfFiles, path, title);
        LocalFile f = new LocalFile(title, user, "test", "test", tags); //ARREGLAR ESTA LINEA

        // If the path exists
        if (path != null) {
            objective = new File(path);
            byte buffer[] = new byte[(int) objective.length()];
            try {
                // Reads the file
                FileInputStream FIS = new FileInputStream(path);
                BufferedInputStream input = new BufferedInputStream(FIS);
                input.read(buffer, 0, buffer.length);
                input.close();
                
                // Uploads it
                inter.saveFile(buffer, f, cinter);
                System.out.println("File " + title + " has been uploaded to the server\n");
            } catch (IOException e) {
                System.out.println("FileServer exception:" + e.getMessage());
            }
        } else {
            System.out.println("File: " + title + ",not found");
        }
    }

    public static String searchFile(File[] listOfFiles, String path, String title) {
        // Looks if there is a file with the name "title"
        String found = null;
        for (File e : listOfFiles) {
            if (e.isFile()) {
                if (e.getName().equals(title)) {
                    return path + "/" + title;
                }
            } else if (e.isDirectory()) {
                File folder = new File(path + "/" + e.getName());
                found = searchFile(folder.listFiles(), path + "/" + 
                        folder.getName(), title);
                if (found != null) {
                    return found;
                }
            }
        }
        return found;
    }

    public static void downloadFileAction(RMIServerInterface inter) 
            throws RemoteException {
        // Downloads a file from the server's folder
        String input;
        Scanner reader = new Scanner(System.in);

        System.out.println("Insert the title of the file you want:");
        input = reader.nextLine();
        String path = "Storage-Client/" + input;
        byte[] file = inter.downloadFile(input,null);

        if (file == null) {
            System.out.println("The file hasn't been found\n");
        } else {
            try {
                FileOutputStream FOS = new FileOutputStream(path);
                BufferedOutputStream Output = new BufferedOutputStream(FOS);
                // Downloads the file and writes it to the client's folder
                Output.write(file, 0, file.length);
                Output.flush();
                Output.close();
                System.out.println("File " + input + " has been downloaded from the server");
            } catch (IOException e) {
                System.out.println("FileServer exception:" + e.getMessage());
            }
        }
    }

    public static void searchFilesAction(RMIServerInterface inter) throws RemoteException {
        // Prints the titles of the files with the tag "textualDescrption"
        System.out.println("Insert the tags you want to look for");

        Scanner reader = new Scanner(System.in);
        String tags = reader.nextLine();
        List<String> result = inter.searchFiles(tags,null);

        System.out.println("List of contents related to the entered tag:");
        if (!result.isEmpty()) {
            for (int i = 0; i < result.size(); i++) {
                System.out.println("Title number " + (i + 1) + ": " + result.get(i) + "\n");
            }
        } else {
            System.out.println("Nothing has been found!\n");
        }
    }

    public static void deleteFileAction(RMIServerInterface inter, String user) 
            throws RemoteException {
        // Deletes a file from the server
        System.out.println("Insert the file you want to erase from the server:");
        Scanner reader = new Scanner(System.in);
        String file = reader.nextLine();
        
        Boolean result;
        result = inter.deleteFile(file, user);

        if (result) {
            System.out.println("File erased\n");
        } else {
            System.out.println("Nothing has been found!\n");
        }
    }
}
