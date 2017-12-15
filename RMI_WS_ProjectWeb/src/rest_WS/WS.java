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
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import rmi.*;

@RequestScoped
@Path("")
@Produces({ "application/xml", "application/json" })
@Consumes({ "application/xml", "application/json" })
public class WS {
	
	// Handles the data base
	public Statement getStatement(){
		String strResultado;
		try {
			InitialContext cxt = new InitialContext();
			DataSource ds = (DataSource) cxt.lookup("java:/PostgresXADS");
			if (ds == null){
				strResultado ="KO.DataSource";
			}
			else
			{
				Connection connection = ds.getConnection();
				Statement st = connection.createStatement();
				System.out.println("Devolvemos boy");
				return st;
				//connection.close();
				//st.close();
			}
		}catch(Exception e){ e.printStackTrace(); strResultado="KO.SQL " + e.getMessage(); 
		}
			//DataSource data = (DataSource) cxt.lookup("java:/PostgresXADS");
			//Connection connection = data.getConnection();
			//Statement statement = connection.createStatement();
			//return statement;
		return null;
				
		//} catch (Exception e) {
			//System.out.println("DB not loaded");
			//return null;
		//}
	}
	
	// CODE NOT TESTED
	// POST a File to Database
	@POST
	@Path("/upload")
	public Response addToDataBase(LocalFile f) throws SQLException {
		//try {
			Statement st = getStatement();
			String id = UUID.randomUUID().toString();
			System.out.println("We reached it :D");
			
			try{
				st.executeUpdate("INSERT INTO files(title, tags, server, ids, users) VALUES ("
						+ "'" + f.getTitle() + "'," 
						+ "'" + f.getTags() +  "',"
						+ "'" + f.getServer() +  "'," 
						+ "'" + f.getId() +  "',"
						+ "'" + f.getUser() + "');");
			} catch (Exception e) { 
	            System.err.println("Got an exception! "); 
	            System.err.println(e.getMessage()); 
	        } 

			System.out.println("done boy");
			st.close();
			return Response.status(201).entity(id).build();
			
		//} catch (SQLException e) {
			//return Response.status(500).entity("Database ERROR").build();
		//}
	}

}
