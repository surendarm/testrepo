package com.innoppl;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import org.json.JSONException;

@Path("/santest")
public class Yesthree {
	
	@GET
	@Produces("application/json")
	public Response clitest() throws JSONException, SQLException {

		String result = null;
		
		try {
			 
			Class.forName("org.postgresql.Driver");
 
		} catch (ClassNotFoundException e) {
 
			System.out.println("Where is your PostgreSQL JDBC Driver? "
					+ "Include in your library path!");
			e.printStackTrace();
 
		}
		result = "PostgreSQL JDBC Driver Registered!";
		
		Connection connection = null;
		Statement statement = null;
		String temp = null;
		
		try {
			 
			connection = DriverManager.getConnection(
					"jdbc:postgresql://127.0.0.1:5432/MightyIFS", "postgres",
					"te$ts3rv650");
			/*
			 * MightyIFS
			 * te$ts3rv650
			 * 
			 * mightyifs
			 * psqlpass
			 */
			
 
		} catch (SQLException e) {
 
			System.out.println("Connection Failed! Check output console");
			e.printStackTrace();
 
		}

		
		if (connection != null) {
			System.out.println("You made it, take control your database now!");
			String query="select user_id, user_name from user_ifs where user_id = 50617";
			statement = connection.createStatement();
			ResultSet rs=statement.executeQuery(query);
            while(rs.next())
            {
                System.out.print(" "+rs.getString(1));
                temp = rs.getString(2);
            }
		} else {
			System.out.println("Failed to make connection!");
		}   
		
		result = temp;
	        return Response.status(200).entity(result).build();
		 
	}

}
