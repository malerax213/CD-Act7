package rest_WS;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

import javax.enterprise.context.RequestScoped;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import rmi.*;

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
	
	// CODE NOT TESTED
	// POST a File
	@POST
	@Path("/file")
	public Response createFile(File file){
		try {
			Statement st = getStatement();
			String id = UUID.randomUUID().toString();
			
			st.executeUpdate("INSERT INTO files(id, title, description, uploader, server) VALUES("
							+ "'" + id + "'," 
							+ "'" + file.getName() +  "');");
			return Response. status(201).entity(id).build();
			
		} catch (SQLException e) {
			return Response.status(500).entity("Database ERROR").build();
		}
	}

}
