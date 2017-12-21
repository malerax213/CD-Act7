package rmi;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.*;
import java.rmi.server.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.google.gson.Gson;

import typeClass.LocalFile;
import typeClass.ServerClass;
import typeClass.UserClass;

import javax.ws.rs.core.MediaType;


/**
 * This class implements the remote interface RMIServerInterface.
 */
public class RMIServerImplementation extends UnicastRemoteObject implements RMIServerInterface {

	private static final long serialVersionUID = 1L;
	Map<RMIClientInterface, String> clients = new HashMap<>();    // Will contain all the clients
    Map<RMIServerInterface, String> servers = new HashMap<>();    // Will contain all the servers
    ServerClass server;
    String ws;
    
    public RMIServerImplementation() throws RemoteException {
        super();
    }

    @Override
    public byte[] downloadFile(String title) {
        byte buffer[] = null;
        LocalFile f = getFile(title);
        ServerClass s = getServer(f.server);
        try {
			return getRMIserver(s).downloadFileFinal(title);
		} catch (Exception e) {
			e.printStackTrace();
		} 
		return null;
    }
    
    public byte[] downloadFileFinal(String title) throws RemoteException {
        File folder = new File("Storage-Server");
        String path = "Storage-Server";
        File[] listOfFiles = folder.listFiles();
        path = searchFile(listOfFiles, path, title);
        byte buffer[] = null;
        
        if (path!=null){
            File objective = new File(path);
            buffer = new byte[(int) objective.length()];
            try {
                BufferedInputStream input = 
                        new BufferedInputStream(new FileInputStream(path));
                input.read(buffer, 0, buffer.length);
                input.close();
                if (buffer != null){
                    return buffer;
                }
            } catch (IOException e) {
                System.out.println("FileServer exception:" + e.getMessage());
                return null;
            }
        }
        // Handles multiple servers download
        return null;	
    }
    
    public String searchFile(File[] listOfFiles, String path, String title) {
        // Search a title in a list of files and returns the path of it
        String found = null;
        for (File e : listOfFiles) {
            if (e.isFile()) {
                if (e.getName().equals(title)) {
                    return path + "/" + title;
                }
            } else if (e.isDirectory() && !"config".equals(e.getName())) {
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

    @Override
    public void saveFile(byte[] buffer, LocalFile f, RMIClientInterface cinter) throws RemoteException {
    	
        // The random ID is being generated each time a file is saved
        // on the server's folder
        String uniqueID = UUID.randomUUID().toString();
        File dir = new File("Storage-Server/" + uniqueID);
        dir.mkdir();
        String path = "Storage-Server/" + uniqueID + "/" + f.getTitle();
        f.setServer(server.name);
        f.setId(uniqueID);
        
        try {
            // All the information about the uploads will be stored
            // on the data base
            addToDataBase(f); 
        } catch (IOException ex) {
            Logger.getLogger(RMIServerImplementation.class.getName())
                    .log(Level.SEVERE, null, ex);
        }

        try {
            FileOutputStream FOS = new FileOutputStream(path);
            BufferedOutputStream Output = new BufferedOutputStream(FOS);
            Output.write(buffer, 0, buffer.length);
            Output.flush();
            Output.close();
            notifyClients(cinter, f.getTitle());
        } catch (IOException e) {
            System.out.println("FileServer exception:" + e.getMessage());
        }
    }

    public void addToDataBase(LocalFile f) 
    		throws IOException{
    	 try {
             URL url = new URL ("http://"+this.ws+":8080/RMI_WS_ProjectWeb/rest/files/");
             HttpURLConnection conn = (HttpURLConnection) url.openConnection();
             conn.setDoOutput(true);
             conn.setRequestMethod("POST");
             conn.setRequestProperty("Content-Type", "application/json");
             
             OutputStream os = conn.getOutputStream();
             os.write(f.getJson().getBytes());
             os.flush();
             
             int status = conn.getResponseCode();
             if(status != HttpURLConnection.HTTP_CREATED){ 
                 throw new IOException();
             }
             BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
 			 String id = br.readLine();
             conn.disconnect();
             
             
         } catch (IOException e) {
             System.out.println(e.toString());
         }  
    }
    


    @Override
    public List<String> searchTags(String tags) throws RemoteException {
    	LocalFile[] allfiles = getAllFiles();
    	
        List<String> result = new ArrayList();
        String[] tagslist = tags.split("[ ,]");
        for (LocalFile file : allfiles) {
        	String[] tagsfile = file.getTags().split("[ ,]");
		    Boolean found = true;
		    
		    for(String tag : tagslist){
		        if (!Arrays.asList(tagsfile).contains(tag)) {
		            found = false;
		        }
		    }
		    if(found){
		        result.add(file.getTitle());
		    }
		}
        return result;
    }
    
    
    public LocalFile[] getAllFiles(){
    	try {
			
			URL url = new URL ("http://"+this.ws+":8080/RMI_WS_ProjectWeb/rest/files/");
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Accept", MediaType.APPLICATION_JSON);
		
			if(conn.getResponseCode() != 200){
				return null;
			}

			BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String output = br.readLine();
			conn.disconnect();
			
			Gson g = new Gson();
			LocalFile[] allfiles = g.fromJson(output, LocalFile[].class);
			return allfiles;
        } catch (IOException ex) {
        	return null;
        }  
			
    }
    
    
    @Override
    public Boolean deleteFile(String file, String user){
        // Handles the delete of the files.
        // If the file is in the DB
    	LocalFile f = getFile(file);
        if (f.getTitle().equals(file)){
            System.out.println("Eliminated: "+f.getTitle());

            String uniqueID = getID(file).toString();
            String path = "Storage-Server/" + uniqueID + "/" + file;
            File tmp = new File(path);
            File folder = new File("Storage-Server/" + getID(f.getTitle()));
            tmp.delete();
            folder.delete();
            
            // Deleting the file from the data base
            if(deleteFileByTitle(file) == 0){
            	System.out.println("Deleted successfully from DB");
            	return true;
            }else{
            	System.out.println("An error has occured while deleting from the DB");
            	return false;
            }
            
          }else{
        	  System.out.println("The file is not in the DB");
        	  return false;
          }

    }

    
    @Override
    public int deleteFileByTitle(String title){
        try {
            URL url = new URL ("http://"+this.ws+":8080/RMI_WS_ProjectWeb/rest/file/" + title + "/delete");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("DELETE");
            conn.setRequestProperty("Content-Type", "application/json");

            int status = conn.getResponseCode();
            conn.disconnect();
            if(status != 204){ 
                    return 1;
            }
            return 0;
        
        } catch (IOException ex) {
        	return 1;
        }  
    }
    
    @Override
    public void registerClient(RMIClientInterface client, String userName, String pass) 
            throws RemoteException{
    	
    	int code = registerUser(new UserClass(userName,pass));
    	
    	// If it is a new user
        if (code == 0){
        	UserClass user = new UserClass(userName, pass);
            registerUser(user);
            clients.put(client, userName);
            client.sendMessage("Registered successfully with user: " + userName);
            System.out.println("User registed with user name: " + userName);
            
        }else if(code == 1){
        	 // If the username is already taken or already registered
            client.sendMessage("Welcome back! " + userName);
            System.out.println("User " + userName + " found in the database.");
            
        }else if(code == 2){
        	// If an error has occured
            client.sendMessage("Register failed, restart the client");
        }

    }
    
    
    @Override
    public int registerUser(UserClass user) {
    	try{
	    	URL url = new URL ("http://"+this.ws+":8080/RMI_WS_ProjectWeb/rest/user");
	        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	        conn.setDoOutput(true);
	        conn.setRequestMethod("POST");
	        conn.setRequestProperty("Content-Type", "application/json");
	
	        Gson g = new Gson();
	        String input = g.toJson(user);
	
	        OutputStream os = conn.getOutputStream();
	        os.write(input.getBytes());
	        os.flush();
	
	        int status = conn.getResponseCode();
	        conn.disconnect();
	        
	        if(status != HttpURLConnection.HTTP_CREATED){ 
	            if(status == 409)
	                return 1;
	            return 2;
	        }
	        
	        return 0;  
    
    	} catch (IOException e) {
	        System.out.println(e.toString());
	        return 300;
	    }   
    }
    
    @Override
    public void registerServer(String ws) {
	    this.ws = ws;
    	try {
	        URL url = new URL ("http://"+this.ws+":8080/RMI_WS_ProjectWeb/rest/servers/");
	        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	        conn.setDoOutput(true);
	        conn.setRequestMethod("POST");
	        conn.setRequestProperty("Content-Type", "application/json");
	        
	        String input = server.getJson();
	        
	        OutputStream os = conn.getOutputStream();
	        os.write(input.getBytes());
	        os.flush();
	        
	        int status = conn.getResponseCode();
	        if(status != HttpURLConnection.HTTP_CREATED){ 
	            throw new IOException();
	        }
	        conn.disconnect();
	        System.out.println("Server registered");
	        
	    } catch (IOException e) {
	        System.out.println(e.toString());
	    }  
    }
    
    public String getID(String title){
		try {
			
			URL url = new URL ("http://"+this.ws+":8080/RMI_WS_ProjectWeb/rest/ID/" + title);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Accept", MediaType.APPLICATION_JSON);
			if(conn.getResponseCode() != 200)
				return null;
			
			BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String output = br.readLine();
			conn.disconnect();

			return output;
					
		} catch (Exception e) { 
			return null; 
		}
	}
    
    public UserClass getUser(String name){
		try {
			
			URL url = new URL ("http://"+this.ws+":8080/RMI_WS_ProjectWeb/rest/user/" + name);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Accept", MediaType.APPLICATION_JSON);
			if(conn.getResponseCode() != 200)
				return null;
			
			BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String output = br.readLine();
			conn.disconnect();
			Gson g = new Gson();
			UserClass user = g.fromJson(output, UserClass.class);
			return user;
					
		} catch (Exception e) { 
			return null; 
		}
	}
    
    public LocalFile getFile(String title){
    	try {
			URL url = new URL ("http://"+this.ws+":8080/RMI_WS_ProjectWeb/rest/file/" + title);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Accept", MediaType.APPLICATION_JSON);
			if(conn.getResponseCode() != 200)
				return new LocalFile("","","","","");
			
			BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String output = br.readLine();
			conn.disconnect();
			
			Gson g = new Gson();
			LocalFile f = g.fromJson(output, LocalFile.class);
			return f;
					
		} catch (Exception e) { return null; }
    }
    
    public ServerClass getServer(String name){
    	try {
			URL url = new URL ("http://"+this.ws+":8080/RMI_WS_ProjectWeb/rest/server/" + name);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Accept", MediaType.APPLICATION_JSON);
			if(conn.getResponseCode() != 200)
				return null;
			
			BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String output = br.readLine();
			conn.disconnect();
			
			Gson g = new Gson();
			ServerClass s = g.fromJson(output, ServerClass.class);
			return s;
					
		} catch (Exception e) { return null; }
    }
    
    public void notifyClients(RMIClientInterface client, String title) 
            throws RemoteException{
	        List<RMIClientInterface> clients_interface = 
	                new ArrayList<>(clients.keySet());
	        
	        // We iterate through all the clients
	        for(RMIClientInterface cl: clients_interface){
	            if(!cl.equals(client))
	                cl.sendMessage("New File Uploaded: "+title);
	        }
    }  

    public void disconnect(RMIClientInterface client) throws RemoteException{
        // Removes the client from the clients Map
        System.out.println("Client " + clients.get(client) + " --> disconnected");
        clients.remove(client);    
    }
    
   
    public RMIServerInterface getRMIserver(ServerClass server) throws RemoteException, 
    NotBoundException, MalformedURLException{
        String registryURL = "rmi://" + server.getIp().replace(" ","") + ":" + server.getPort().replace(" ","") + "/some";
		try {
	        RMIServerInterface objective = (RMIServerInterface) Naming.lookup(registryURL);
	        return objective; 
		} catch (Exception e) {
			return null;
		}
        
   
    }
    
}
