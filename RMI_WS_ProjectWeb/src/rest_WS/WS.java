package rest_WS;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

import javax.enterprise.context.RequestScoped;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import rmi.*;
import typeClass.LocalFile;
import typeClass.ServerClass;
import typeClass.UserClass;

@RequestScoped
@Path("")
@Produces({ "application/xml", "application/json" })
@Consumes({ "application/xml", "application/json" })
public class WS {
	
	// Handles the data base
	public Statement getStatement(){
		try {
			InitialContext cxt = new InitialContext();
			DataSource data = (DataSource) cxt.lookup("java:/PostgresXADS");
			Connection connection = data.getConnection();
			Statement statement = connection.createStatement();
			return statement;
				
		} catch (Exception e) {
			System.out.println("DB not loaded");
			return null;
		}
	}
	
	// POST a File to Database
	@POST
	@Path("/files")
	public Response addToDataBase(LocalFile f) throws SQLException {
		//try {
			Statement st = getStatement();
			String id = UUID.randomUUID().toString();
			
			try{
				st.executeUpdate("INSERT INTO files(title, tags, server, ids, users) VALUES ("
						+ "'" + f.getTitle() + "'," 
						+ "'" + f.getTags() +  "',"
						+ "'" + f.getServer() +  "'," 
						+ "'" + f.getId() +  "',"
						+ "'" + f.getUser() + "');");
			} catch (Exception e) {  
	            System.err.println(e.getMessage()); 
	        } 

			st.close();
			return Response.status(201).entity(id).build();

	}

	// POST a Server to database
	@POST
	@Path("/servers")
	public Response postServer(ServerClass s) {
		try {
			Statement st = getStatement();
			
			st.executeUpdate("INSERT INTO servers(name, ip, port) VALUES("
							+ "'" + s.getName() + "'," 
							+ "'" + s.getIp() +  "',"
							+ "'" + s.getPort() +  "');");
			st.close();
			return Response.status(201).entity(s.getName()).build();
			
		} catch (SQLException e) {
			return Response.status(500).entity("Database ERROR").build();
		}
	}
	
	// GET Server
	@GET
	@Path("/server/{name}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getServer(@PathParam("name") String name){	 
		try {
			Statement state = getStatement();
			ResultSet resset = state.executeQuery("SELECT name,ip,port FROM server "
					+ "WHERE name='" + name + "';");
			ServerClass server = new ServerClass();
			
			if(!resset.isBeforeFirst())
				return Response.status(404).entity("server not found").build();
			else{
				resset.next();
				server.setName(resset.getString("name"));
				server.setIp(resset.getString("ip"));
				server.setPort(resset.getString("port"));
			}
			state.close();
			return Response.status(200).entity(server).build();
			
		} catch (SQLException e) {
			return Response.status(500).entity("Database ERROR" + e.toString()).build();
		}
	}
	
	// GET USER
	@GET
	@Path("/user/{name}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getUser(@PathParam("name") String name){	 
		try {
			Statement state = getStatement();
			ResultSet resset = state.executeQuery("SELECT name,pass FROM users "
					+ "WHERE name='" + name + "';");
			UserClass user = new UserClass();
			
			if(!resset.isBeforeFirst())
				return Response.status(404).entity("User not found").build();
			else{
				resset.next();
				user.setTitle(resset.getString("name"));
				user.setUser(resset.getString("pass"));
			}
			
			state.close();
			return Response.status(200).entity(user).build();
			
		} catch (SQLException e) {
			return Response.status(500).entity("Database ERROR" + e.toString()).build();
		}
	}
	
	// POST USER
	@POST
	@Path("/user")
	public Response createUser(UserClass user){
		try {
			Statement st = getStatement();
			ResultSet rs = st.executeQuery("SELECT name FROM users "
					+ "WHERE name='" + user.getTitle() + "';");
			if (rs.isBeforeFirst())
				return Response.status(409).entity("Name already in use").build();
			
			st.executeUpdate("INSERT INTO users(name,pass) VALUES("
							+ "'" + user.getTitle() + "'," 
							+ "'" + user.getUser() + "');");
			st.close();
			return Response.status(201).build();
			
		} catch (SQLException e) {
			return Response.status(500).entity("Database ERROR").build();
		}
	}
	
	// DELETE FILE by Title
	@DELETE
	@Path("/file/{title}/delete")
	@Produces(MediaType.APPLICATION_JSON)
	public Response deleteFile(@PathParam("title") String title){
		try{
			Statement st = getStatement();
			st.executeUpdate("DELETE FROM files WHERE title = '" + title + "';");
			st.close();
			return Response.status(204).build();
			
		}catch(SQLException ex){
			return Response.status(500).entity("Database ERROR").build();
		}
	}
	
	// GET FILE by TITLE
	@GET
	@Path("/file/{title}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getVideo(@PathParam("title") String title){	 
		try {
			Statement st = getStatement();
			ResultSet rs = st.executeQuery("SELECT tags, title, server, ids, users FROM files "
					+ "WHERE title='" + title + "';");
			LocalFile f = new LocalFile();
			if(!rs.isBeforeFirst()){
				return Response.status(404).entity("File not found").build();
			}else{
				rs.next();
				f.setTitle(title);
				f.setId(rs.getString("ids"));
				f.setUser(rs.getString("users"));
				f.setServer(rs.getString("server"));
				f.setTags(rs.getString("tags"));
			}
			
			st.close();
			return Response.status(200).entity(f).build();
			
		} catch (SQLException e) {
			return Response.status(500).entity("Database ERROR" + e.toString()).build();
		}
	}
	
}
