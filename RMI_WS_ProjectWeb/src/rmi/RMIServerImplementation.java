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

import javax.ws.rs.core.MediaType;


/**
 * This class implements the remote interface RMIServerInterface.
 */
public class RMIServerImplementation extends UnicastRemoteObject 
        implements RMIServerInterface {

    // Will contain all the clients
    Map<RMIClientInterface, String> clients = new HashMap<>();
    // Will contain all the servers
    Map<RMIServerInterface, String> servers = new HashMap<>();
    ServerClass server;
    
    public RMIServerImplementation() throws RemoteException {
        super();
    }

    @Override
    public byte[] downloadFile(String title, String caller) throws RemoteException {
    	
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
        for(Map.Entry<RMIServerInterface,String> server : servers.entrySet()){
            if(!server.getValue().equals(caller)){
                System.out.println("Searching in Server:" + server);
                buffer = server.getKey().downloadFile(title, this.server.name);
            }
            if (buffer != null){
                return buffer;
            }
        }
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
             URL url = new URL ("http://localhost:8080/RMI_WS_ProjectWeb/rest/upload/");
             HttpURLConnection conn = (HttpURLConnection) url.openConnection();
             conn.setDoOutput(true);
             conn.setRequestMethod("POST");
             conn.setRequestProperty("Content-Type", "application/json");
             
             OutputStream os = conn.getOutputStream();
             os.write(f.getJson().getBytes());
             os.flush();
             
             int status = conn.getResponseCode();
             System.out.println(status);
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
    
    // NOT USED ANYMORE
    public void addToLibrary(String title, String path, String user, String tags) 
            throws IOException {
        // Adds the information about the upload to the registry file (library)
        FileWriter fw = new FileWriter("Storage-Server/config/library", true);
        BufferedWriter bw = new BufferedWriter(fw);

        bw.write(title);
        bw.newLine();
        bw.write(user);
        bw.newLine();
        bw.write(path);
        bw.newLine();
        bw.write(tags);
        bw.newLine();

        bw.close();
    }

    public Map readLibrary() throws FileNotFoundException, IOException {
        // Reads the file library (where all the information about the uploads is saved)
        FileReader fr = new FileReader("Storage-Server/config/library");
        BufferedReader br = new BufferedReader(fr);

        String line = br.readLine();
        Map<String, ArrayList> library = new HashMap<>();
        String title = null;
        while (line != null) {
            ArrayList<Object> info = new ArrayList<>();
            for (int i = 0; i < 4; i++) {
                if (i == 0) {
                    title = line;
                }
                info.add(line);
                line = br.readLine();
            }
            library.put(title, info);
        }
        br.close();
        fr.close();
        // Returns a Map with the title and the content of every upload made by a client
        return library;
    }
    
        public void updateLibrary(Map<String, ArrayList> library) 
                throws FileNotFoundException, IOException {
        // Update the file library, after deleting an element
        PrintWriter writer = new PrintWriter("Storage-Server/config/library");
        writer.print("");
        writer.close();
        
        for(Map.Entry<String, ArrayList> entry : library.entrySet()){
            String title = String.valueOf(entry.getValue().get(0));
            String path = String.valueOf(entry.getValue().get(1));
            String user = String.valueOf(entry.getValue().get(2));
            String tags = String.valueOf(entry.getValue().get(3));
            addToLibrary(title, path, user, tags);
        }
    }

    @Override
    public List searchFiles(String tags, String caller) throws RemoteException {
        Map<String, ArrayList> library;
        List<String> result = new ArrayList();
        String[] tagslist = tags.split("[ ,]");
        
        try {
            library = readLibrary();
            for (Map.Entry<String, ArrayList> entry : library.entrySet()) {
                // The 3rd position of the value will be the TAG field
                String[] tagsfile = String.valueOf(entry.getValue().get(3))
                        .split("[ ,]");
                Boolean found = true;

                for(String tag : tagslist){
                    if (!Arrays.asList(tagsfile).contains(tag)) {
                        found = false;
                    }
                }
                if(found){
                    result.add(String.valueOf(entry.getValue().get(0)));
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(RMIServerImplementation.class.getName())
                    .log(Level.SEVERE, null, ex);
        }

        // Handles the search on multiple servers
        for(Map.Entry<RMIServerInterface,String> server : servers.entrySet()){
            if(!server.getValue().equals(caller)){
                System.out.println("Searching Tags in Server:" + server);
                result.addAll(server.getKey().searchFiles(tags, this.server.name));
            }
        }      
        return result;
    }
    
    @Override
    public Boolean deleteFile(String file, String user){
        // Handles the delete of the files
        Map<String, ArrayList> library = new HashMap<>();
        try {
            library = readLibrary();
            if (library.containsKey(file)){
                ArrayList info;
                info = library.get(file);
                if(String.valueOf(info.get(1)).equals(user)){
                    System.out.println("Eliminated:"+String.valueOf(info.get(2)));

                    File tmp = new File(String.valueOf(info.get(2)));
                    tmp.delete();
                    
                    String path = String.valueOf(info.get(2)).replace("/"+file,"");
                    tmp = new File(path);
                    tmp.delete();
                    
                    // The registry file (library) is being updated with the changes
                    library.remove(file);
                    updateLibrary(library);
                    
                    return true;
                }
            return false; 
            }        
        } catch (IOException ex) {
            Logger.getLogger(RMIServerImplementation.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
        return false;
    }
    
    @Override
    public void registerClient(RMIClientInterface client, String userName, String pass) 
            throws RemoteException{
    	UserClass userFromDB = getUser(userName);
    	
        if (userFromDB == null){
        	UserClass user = new UserClass(userName, pass);
            registerUser(user);
            client.sendMessage("Registered successfully with user: " + userName);
            System.out.println("User registed with user name: " + userName);
        }else{
        	 // If the username is already taken or already registered
            client.sendMessage("The user is already used or registered");
        }

    }
    
    public int registerUser(UserClass user) {
    	try{
	    	URL url = new URL ("http://localhost:8080/RMI_WS_ProjectWeb/rest/user");
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
	                return 400;
	            return 500;
	        }
	        
	        return 0;
        
    
    	} catch (IOException e) {
	        System.out.println(e.toString());
	        return 300;
	    }   
    }
    
    @Override
    public void registerServer() {
	    try {
	        URL url = new URL ("http://localhost:8080/RMI_WS_ProjectWeb/rest/servers/");
	        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	        conn.setDoOutput(true);
	        conn.setRequestMethod("POST");
	        conn.setRequestProperty("Content-Type", "application/json");
	        
	        String input = server.getJson();
	        OutputStream os = conn.getOutputStream();
	        os.write(input.getBytes());
	        os.flush();
	        
	        System.out.println("Server?");
	        
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
    
    public UserClass getUser(String name){
		try {
			
			URL url = new URL ("http://localhost:8080/RRMI_WS_ProjectWeb/rest/user/" + name);
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
}
