package com.innoppl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
//import com.innoppl.exception.FormNotDetectedException;
import com.sun.jersey.multipart.BodyPartEntity;
import com.sun.jersey.multipart.FormDataBodyPart;
import com.sun.jersey.multipart.FormDataMultiPart;


@Path("/servicetest")
public class SampleService {

	@GET
	@Path("/formsubmissionstatus/{form_submission_id}")
	@Produces("application/json")
	public Response formSubmissionStatus(@PathParam("form_submission_id") int passformSubmissionId) throws JSONException {
		String result = null;
		try {
			Connection conn = null;
			conn = connectToDatabaseOrDie();
			int FormSubmissionId = 0;
			JSONObject resultJsonObject = new JSONObject();
			int formStatus = 0;
			String formTimer="00:00";
			if (passformSubmissionId <= 0) {
				result = ""+ resultJsonObject.put("invalid Sumbmission Id ","form_submission_id is not valid input");
				return Response.status(200).entity(result).build();
			} else {
				Statement st = conn.createStatement();
				ResultSet rs = st.executeQuery("SELECT form_submission_id,status,timer FROM form_submission_index where form_submission_id="+ passformSubmissionId);
				if (rs.next()) {
					FormSubmissionId = rs.getInt("form_submission_id");
					formStatus = rs.getInt("status");
					formTimer=rs.getString("timer");
				} else {
					result = ""+ resultJsonObject.put("Error ","Invalid Submission Id");
					return Response.status(200).entity(result).build();
				}
				resultJsonObject.put("formSubmissionId", FormSubmissionId + "");
				resultJsonObject.put("status", formStatus);
				resultJsonObject.put("timer", formTimer);
				result = "" + resultJsonObject;
				return Response.status(200).entity(result).build();
			}

		} catch (Exception e) {
			result = "" + e.getMessage();
			System.out.println("Error " + e.getMessage());
			return Response.status(200).entity(result).build();
		}
	}

	@Path("testinp/{franid}")
	@GET
	@Produces("application/json")
	public Response testinp(@PathParam("franid") int franid) throws JSONException{
		Connection conn = null;
		conn = connectToDatabaseOrDie();
		JSONObject res = new JSONObject();
		String result = null;


		int formSubmissionId = 0;
		JSONArray jarray= new JSONArray();
		try{
			Statement st = conn.createStatement();
			Statement st1 = conn.createStatement();
			Statement st2 = conn.createStatement();
			Statement st3 = conn.createStatement();

			ResultSet rs, newrs;
			rs = st.executeQuery("select * from form_submission_index where franid='"+franid+"'");
			if(rs.next()){
				newrs = st.executeQuery("select indtab.form_submission_id, indtab.timer, indtab.status, profile_value, profile_field from form_submission_profile_data protab join form_submission_index indtab on indtab.form_submission_id = protab.form_submission_id where indtab.franid = '"+franid+"' and profile_field in ('first_name', 'last_name', 'model') limit 60");
				int i =0;
				while(newrs.next()){

					formSubmissionId  = newrs.getInt("form_submission_id");
					//System.out.println(newrs.getInt("form_submission_id"));

					JSONObject jsonObject = new JSONObject();
					jsonObject.put("submissionID",formSubmissionId);
					jsonObject.put("timer", newrs.getString("timer"));
					jsonObject.put("status", newrs.getString("status"));

					jsonObject.put("profile_value",newrs.getString("profile_value"));
					jsonObject.put("profile_field",newrs.getString("profile_field"));

					jarray.put(i, jsonObject);
					i++;
				}


				System.out.println("Length: "+jarray.length());
				for(int j=0;j<jarray.length();j++){
					JSONObject json_data = new JSONObject();
					json_data = jarray.getJSONObject(j);
					System.out.println("@@@ :"+ json_data.getInt("submissionID"));
					System.out.println("$$$ :"+ json_data.getString("profile_field"));
					System.out.println("### :"+ json_data.getString("profile_value"));
				}
				System.out.println(jarray);
			}else{
				result = "Franchisee not found";
				return Response.status(200).entity(result).build();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		res.put("Inspections",jarray);
		result = "" + res;
		return Response.status(200).entity(result).build();

	}

	@Path("inspections/{franid}")
	@GET
	@Produces("application/json")
	public Response inspections(@PathParam("franid") int franid) throws JSONException{
		Connection conn = null;
		conn = connectToDatabaseOrDie();
		JSONObject res = new JSONObject();
		String result = null;
		int formSubmissionId = 0;
		JSONArray jarray= new JSONArray();

		try{
			Statement st = conn.createStatement();
			Statement st1 = conn.createStatement();
			Statement st2 = conn.createStatement();
			Statement st3 = conn.createStatement();

			ResultSet rs, newrs;
			rs = st.executeQuery("select * from form_submission_index where franid='"+franid+"'");
			if(rs.next()){
				newrs = st.executeQuery("select indtab.form_submission_id, indtab.timer, indtab.status, profile_value, profile_field from form_submission_profile_data protab join form_submission_index indtab on indtab.form_submission_id = protab.form_submission_id where indtab.franid = '"+franid+"' and profile_field in ('first_name', 'last_name', 'model', 'make', 'engine', 'year') order by indtab.form_submission_id desc limit 90");
				int i =0;
				Multimap<Integer, HashMap<String,String>> new_my = ArrayListMultimap.create();
				Multimap<FormIndexId, Multimap<Integer, HashMap<String,String>>> multiMap = ArrayListMultimap.create();
				while(newrs.next()){
					FormIndexId formIndexId=new FormIndexId();
					formIndexId.setSubmissionID(newrs.getInt("form_submission_id"));
					formIndexId.setStatus(newrs.getString("status"));
					formIndexId.setTimer(newrs.getString("timer"));
					HashMap<String, String> my=new HashMap<String, String>();
					my.put(newrs.getString("profile_field"),newrs.getString("profile_value"));
					//System.out.println("ONE: "+my);
					new_my.put(newrs.getInt("form_submission_id"), my);
					//System.out.println("TWO: "+new_my);
					multiMap.put(formIndexId, new_my);
					i++;
				}
				List<FormIndexId> formIndexIdList=new ArrayList<FormIndexId>();
				Set<FormIndexId> keys = multiMap.keySet();
				for (FormIndexId key : keys) {
					formIndexIdList.add(key);
				}
				Set<Integer> nkeys = new_my.keySet();
				//System.out.println("List Size : "+xx.size()+" set Size : "+nkeys.size());
				int inbount_count=0;
				for (Integer nkey : nkeys) {
					for (int i1 = 0; i1 < formIndexIdList.size(); i1++) {
						if(nkey==formIndexIdList.get(i1).getSubmissionID()){
							JSONObject jsonObject = new JSONObject();
							jsonObject.put("submissionID",formIndexIdList.get(i1).getSubmissionID());
							jsonObject.put("timer", formIndexIdList.get(i1).getTimer());
							jsonObject.put("status",formIndexIdList.get(i1).getStatus());
							jsonObject.put("profile_data ",new_my.get(nkey));
							jarray.put(inbount_count, jsonObject);
							i1=formIndexIdList.size();
						}
					}
					inbount_count++;
				}
			}else{
				result = "Franchisee not found";
				return Response.status(200).entity(result).build();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		res.put("Inspections",jarray);
		result = "" + res;
		return Response.status(200).entity(result).build();

	}



	@POST
	@Path("/newprofiledata")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response newprofiledata(String s) throws JSONException{

		Connection conn = null;
		conn = connectToDatabaseOrDie();  

		JSONObject tt = new JSONObject();
		String result = null;

		JSONObject json =  new JSONObject(s);
		JSONArray parr = new JSONArray(json.get("profile").toString());
		int userID = 0;
		int franID = 0;

		String formName = null;
		int formSubmissionID = 0;

		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date date1 = new Date();
		String createdDate = dateFormat.format(date1);

		try{
			userID = json.getInt("user_id");
			franID = json.getInt("fran_id");

			formName = json.get("form_name").toString();
			if(((int)Math.log10(userID) + 1) == 0){
				tt.put("error", "User ID is null");
				result =""+tt; 
				return Response.status(200).entity(result).build();
			}else{
				try{

					Statement st = conn.createStatement();
					st.execute("Insert into form_submission_index (user_id, form_name, franid, created_date, timer, status) VALUES ("+ userID +",'"+ formName +"','"+ franID +"','"+ createdDate +"','00:00','0')");
					ResultSet rs = st.executeQuery("select last_value from form_submission_index_form_submission_id_seq");
					while ( rs.next() ){
						formSubmissionID = rs.getInt("last_value");
					}


					int i =0;
					JSONObject jj = (JSONObject) parr.get(i);
					Iterator<?> keys = jj.keys();

					while( keys.hasNext() ){
						String key = (String)keys.next();
						String keyValue = jj.getString(key).replace("'", "''");
						st.execute("Insert into form_submission_profile_data " +
								"(form_submission_id, user_id, profile_field, profile_value)" +
								"VALUES" +
								"("+ formSubmissionID +","+ userID +",'"+ key +"','"+ keyValue +"')");
					}

					tt.put("submissionID", formSubmissionID);
					result =""+tt; 
					return Response.status(200).entity(result).build();
				}
				catch(Exception e){
					result =""+e; 
					e.printStackTrace();
					return Response.status(200).entity(result).build();
				}

			}

		}
		catch (Exception e){
			e.printStackTrace();
			tt.put("error", "User ID field is not available");
			result =""+tt; 
			return Response.status(200).entity(result).build();
		}


	}

	@POST
	@Path("/newcompdata")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response newcompdata(String s) throws JSONException{

		Connection conn = null;
		conn = connectToDatabaseOrDie();  

		JSONObject tt = new JSONObject();
		String result = null;
		String timer = "";

		JSONObject json =  new JSONObject(s);
		JSONArray jarr = new JSONArray(json.get("components").toString());
		JSONArray parr = new JSONArray(json.get("profile").toString());
		int userID = 0;
		int formSubmissionID = 0;

		try{
			userID = json.getInt("user_id");
			timer = json.getString("Timer");
			formSubmissionID = json.getInt("submission_id");
			if(((int)Math.log10(userID) + 1) == 0){
				tt.put("error", "User ID is null");
				result =""+tt; 
				return Response.status(200).entity(result).build();
			}else{
				try{


					Statement st = conn.createStatement();
					ResultSet rs = st.executeQuery("select * from form_submission_index where form_submission_id = "+formSubmissionID);
					if(rs.next()){


						for(int i =0;i< jarr.length();i++){
							JSONObject jj = (JSONObject) jarr.get(i);
							String componentName = null;
							String label = null;
							String optionDescription = null;
							String positionID = null;
							componentName = jj.getString("component_name");
							label = jj.getString("label");
							optionDescription = jj.getString("option_description");
							positionID = jj.getString("position_id");
							st.execute("Insert into form_submission_component_data " +
									"(form_submission_id, user_id, position_id, component_name, label, option_description) " +
									"VALUES " +
									"("+ formSubmissionID +","+ userID +",'"+ positionID +"','"+ componentName.replace("'", "''") +"','"+ label.replace("'", "''") +"','"+ optionDescription.replace("'", "''") +"')");
						}

						int i = 0;
						JSONObject jj = (JSONObject) parr.get(i);
						Iterator<?> keys = jj.keys();
						while (keys.hasNext()) {
							String key = (String) keys.next();
							String keyValue = jj.getString(key).replace("'", "''");
							// System.out.println("Profile Key Value "+ keyValue);
							st.execute("Insert into form_submission_profile_data "
									+ "(form_submission_id, user_id, profile_field, profile_value)"
									+ "VALUES" + "(" + formSubmissionID + ","
									+ userID + ",'" + key + "','" + keyValue + "')");
						}

						st.execute("update form_submission_index set status = '1' where form_submission_id= "+formSubmissionID);
						st.execute("update form_submission_index set timer = '"+ timer +"' where form_submission_id= "+formSubmissionID);

						tt.put("submissionID", formSubmissionID);
						result =""+tt; 
						return Response.status(200).entity(result).build();
					}else
					{
						tt.put("error", "Invalid Submission ID");
						result =""+tt; 
						return Response.status(200).entity(result).build();
					}


				}
				catch(Exception e){
					result =""+e; 
					e.printStackTrace();
					return Response.status(200).entity(result).build();
				}

			}

		}
		catch (Exception e){
			e.printStackTrace();
			tt.put("error", "User ID field is not available");
			result =""+tt; 
			return Response.status(200).entity(result).build();
		}


	}





	@POST
	@Path("/profiledata")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response profiledata(String s) throws JSONException{

		Connection conn = null;
		conn = connectToDatabaseOrDie();  

		//JSONArray tarr = new JSONArray();
		JSONObject tt = new JSONObject();
		String result = null;

		JSONObject json =  new JSONObject(s);
		//JSONArray jarr = new JSONArray(json.get("components").toString());
		JSONArray parr = new JSONArray(json.get("profile").toString());
		int userID = 0;
		//String timer = "";
		String formName = null;

		int formSubmissionID = 0;

		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date date1 = new Date();
		String createdDate = dateFormat.format(date1);

		try{
			userID = json.getInt("user_id");
			//timer = json.getString("Timer");
			formName = json.get("form_name").toString();

			if(((int)Math.log10(userID) + 1) == 0){
				tt.put("error", "User ID is null");
				result =""+tt; 
				return Response.status(200).entity(result).build();
			}else{
				try{


					Statement st = conn.createStatement();
					st.execute("Insert into form_submission_index (user_id, form_name, created_date) VALUES ("+ userID +",'"+ formName +"','"+ createdDate +"')");
					ResultSet rs = st.executeQuery("select last_value from form_submission_index_form_submission_id_seq");

					while ( rs.next() ){
						formSubmissionID = rs.getInt("last_value");
					}

					/*
					for(int i =0;i< jarr.length();i++){
						JSONObject jj = (JSONObject) jarr.get(i);
						String componentName = null;
						String label = null;
						String optionDescription = null;
						String positionID = null;
						componentName = jj.getString("component_name");
						label = jj.getString("label");
						optionDescription = jj.getString("option_description");
						positionID = jj.getString("position_id");
						st.execute("Insert into form_submission_component_data " +
								"(form_submission_id, user_id, position_id, component_name, label, option_description) " +
								"VALUES " +
								"("+ formSubmissionID +","+ userID +",'"+ positionID +"','"+ componentName.replace("'", "''") +"','"+ label.replace("'", "''") +"','"+ optionDescription.replace("'", "''") +"')");
					}
					 */

					int i =0;
					JSONObject jj = (JSONObject) parr.get(i);
					Iterator<?> keys = jj.keys();
					while( keys.hasNext() ){
						String key = (String)keys.next();
						String keyValue = jj.getString(key).replace("'", "''");
						//System.out.println("Profile Key Value "+ keyValue);
						st.execute("Insert into form_submission_profile_data " +
								"(form_submission_id, user_id, profile_field, profile_value)" +
								"VALUES" +
								"("+ formSubmissionID +","+ userID +",'"+ key +"','"+ keyValue +"')");
					}

					tt.put("submissionID", formSubmissionID);
					result =""+tt; 
					return Response.status(200).entity(result).build();


				}
				catch(Exception e){
					result =""+e; 
					e.printStackTrace();
					return Response.status(200).entity(result).build();
				}

			}

		}
		catch (Exception e){
			e.printStackTrace();
			tt.put("error", "User ID field is not available");
			result =""+tt; 
			return Response.status(200).entity(result).build();
		}


	}


	@POST
	@Path("/componenetdata")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response componenetdata(String s) throws JSONException{

		Connection conn = null;
		conn = connectToDatabaseOrDie();  

		//JSONArray tarr = new JSONArray();
		JSONObject tt = new JSONObject();
		String result = null;

		JSONObject json =  new JSONObject(s);
		JSONArray jarr = new JSONArray(json.get("components").toString());
		//JSONArray parr = new JSONArray(json.get("profile").toString());
		int userID = 0;
		//String timer = "";
		//String formName = null;
		int formSubmissionID = 0;

		//DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		//Date date1 = new Date();
		//String createdDate = dateFormat.format(date1);

		try{
			userID = json.getInt("user_id");
			//timer = json.getString("Timer");
			//formName = json.get("form_name").toString();
			formSubmissionID = json.getInt("submission_id");
			if(((int)Math.log10(userID) + 1) == 0){
				tt.put("error", "User ID is null");
				result =""+tt; 
				return Response.status(200).entity(result).build();
			}else{
				try{


					Statement st = conn.createStatement();
					ResultSet rs = st.executeQuery("select * from form_submission_index where form_submission_id = "+formSubmissionID);
					if(rs.next()){
						/*
					st.execute("Insert into form_submission_index (user_id, form_name, created_date) VALUES ("+ userID +",'"+ formName +"','"+ createdDate +"')");
					ResultSet rs = st.executeQuery("select last_value from form_submission_index_form_submission_id_seq");
					while ( rs.next() ){
						formSubmissionID = rs.getInt("last_value");
					}
						 */

						for(int i =0;i< jarr.length();i++){
							JSONObject jj = (JSONObject) jarr.get(i);
							String componentName = null;
							String label = null;
							String optionDescription = null;
							String positionID = null;
							componentName = jj.getString("component_name");
							label = jj.getString("label");
							optionDescription = jj.getString("option_description");
							positionID = jj.getString("position_id");
							st.execute("Insert into form_submission_component_data " +
									"(form_submission_id, user_id, position_id, component_name, label, option_description) " +
									"VALUES " +
									"("+ formSubmissionID +","+ userID +",'"+ positionID +"','"+ componentName.replace("'", "''") +"','"+ label.replace("'", "''") +"','"+ optionDescription.replace("'", "''") +"')");
						}
					}else{
						tt.put("error", "Invalid Submission ID");
						result =""+tt; 
						return Response.status(200).entity(result).build();	
					}
					/*
					int i =0;
					JSONObject jj = (JSONObject) parr.get(i);
					Iterator<?> keys = jj.keys();
					while( keys.hasNext() ){
						String key = (String)keys.next();
						String keyValue = jj.getString(key).replace("'", "''");
						//System.out.println("Profile Key Value "+ keyValue);
						st.execute("Insert into form_submission_profile_data " +
								"(form_submission_id, user_id, profile_field, profile_value)" +
								"VALUES" +
								"("+ formSubmissionID +","+ userID +",'"+ key +"','"+ keyValue +"')");
					}
					 */
					tt.put("submissionID", formSubmissionID);
					result ="Component Submitted"+tt; 
					return Response.status(200).entity(result).build();
				}
				catch(Exception e){
					result =""+e; 
					e.printStackTrace();
					return Response.status(200).entity(result).build();
				}

			}

		}
		catch (Exception e){
			e.printStackTrace();
			tt.put("error", "User ID field is not available");
			result =""+tt; 
			return Response.status(200).entity(result).build();
		}


	}


	@Path("servicesModelDetails/{makeid}/{yearid}")
	@GET
	@Produces("application/json")
	public Response servicesModelDetails(@PathParam("makeid") String makeid , @PathParam("yearid") String yearid) throws JSONException{
		Connection conn = null;
		conn = connectToDatabaseOrDie();
		JSONObject res = new JSONObject();
		String result = null;
		JSONArray array=new JSONArray();

		try{
			Statement st = conn.createStatement();
			ResultSet rs1;
			rs1 = st.executeQuery("select distinct mo.modelid,mo.modelname,ma.makeid,ma.makename from basevehicle bas,make ma,model mo  where bas.makeid =ma.makeid   AND bas.modelid = mo.modelid AND bas.makeid = "+ makeid + " AND bas.yearid =" + yearid + "order by mo.modelname");
			while ( rs1.next() )
			{
				JSONObject jsonObject2=new JSONObject();

				jsonObject2.put("ModelId",rs1.getInt("modelid"));
				jsonObject2.put("ModelName",rs1.getString("modelname"));
				array.put(jsonObject2);

			}

		}
		catch (Exception e) {
			e.printStackTrace();
		}

		res.put("Models Details:",array);
		result = "" + res;
		return Response.status(200).entity(result).build();

	}

	// ACTIVE to Production

	@Path("servicesMakeDetails/{yearid}")
	@GET
	@Produces("application/json")
	public Response servicesMakeDetails(@PathParam("yearid") String yearid) throws JSONException{
		Connection conn = null;
		conn = connectToDatabaseOrDie();
		JSONObject res = new JSONObject();
		String result = null;
		JSONArray array=new JSONArray();

		try{
			Statement st = conn.createStatement();
			ResultSet rs1;
			rs1 = st.executeQuery("SELECT distinct  ma.makeid, ma.makename FROM basevehicle bas  , make ma  where bas.makeid = ma.makeid and  bas.makeid  IN (16,58,44,66,89,73,31,45,46,17,47,39,18,19,40,41,7,54,50,48,59,93,3,68,71,37,20,42,21,11,75,55,80,63,56,57,32,72,67,51,28,43,52,2,1168,23,65,53,91,484,60,13,1,76,74,27,15,1230) AND bas.yearid = " + yearid + " order by ma.makename");
			while ( rs1.next() )
			{
				JSONObject jsonObject2=new JSONObject();
				jsonObject2.put("MakeId",rs1.getInt("makeid"));
				jsonObject2.put("MakeName",rs1.getString("makename"));
				array.put(jsonObject2);
			}

		}
		catch (Exception e) {
			e.printStackTrace();
		}

		res.put("Make Details:",array);
		result = "" + res;
		return Response.status(200).entity(result).build();

	}


	//Service Regarding to Get TSB Data    
	@Path("servicesTSBData/{makeid}/{modelid}/{yearid}/{econfigid}")
	@GET
	@Produces("application/json")
	public Response servicesTSBData(@PathParam("makeid") String makeid, @PathParam("modelid") String modelid , @PathParam("yearid") String yearid , @PathParam("econfigid") String econfigid) throws JSONException{
		Connection conn = null;
		conn = connectToDatabaseOrDie();
		JSONObject res = new JSONObject();
		String result = null;
		JSONArray jarray= new JSONArray();
		int makeiddb  =0;
		int modeliddb  =0;
		int basevehicleid  =0;
		int vehicleid  =0;
		int vehicletoengineconfigid  =0;

		try{
			Statement st = conn.createStatement();
			ResultSet rs;

			rs = st.executeQuery( "select makeid from make where makeid =" + makeid ) ;
			while ( rs.next() ){
				makeiddb = rs.getInt("makeid");
			}

			//rs = st.executeQuery("select modelid from model where modelname =" + "'"+ modelname +"'");
			rs = st.executeQuery("select modelid from model where modelid =" + modelid);
			while ( rs.next() ){
				modeliddb = rs.getInt("modelid");
			}

			rs = st.executeQuery("select basevehicleid from basevehicle where makeid = "+ makeiddb + " and modelid =" + modeliddb + " and yearid =" + yearid );
			while ( rs.next() ){
				basevehicleid = rs.getInt("basevehicleid");
			}

			rs = st.executeQuery("select vehicleid from vehicle where basevehicleid = "+ basevehicleid + "limit 1");
			while ( rs.next() ){
				vehicleid = rs.getInt("vehicleid");
			}

			// rs = st.executeQuery("select vehicletoengineconfigid from vehicletoengineconfig where vehicleid = "+ vehicleid + "AND engineconfigid = "+ econfigid );
			rs = st.executeQuery("select vehicletoengineconfigid from vehicletoengineconfig where vehicleid = "+ vehicleid + "limit 1" );
			while ( rs.next() ){
				vehicletoengineconfigid = rs.getInt("vehicletoengineconfigid");
			}

			rs = st.executeQuery("SELECT TSB_Application.vehicletoengineconfigid, tblTSB.tsbid, tblTSB.description, tblTSB.filenamepdf,tblTSB.manufacturernum,tblIssuer.issuer,tblTSB.issuedate, tblAutoSystem.autosystemdescription, tblType.type FROM ((TSB_Application INNER JOIN ((TblTSBToAutoSystemXref INNER JOIN TblAutoSystem ON TblTSBToAutoSystemXref.autosystemid = TblAutoSystem.autosystemid) INNER JOIN TblTSB ON TblTSBToAutoSystemXref.tsbid = TblTSB.tsbid) ON TSB_Application.tsbid = TblTSB.tsbid) INNER JOIN (TblTSBToTypeXref INNER JOIN TblType ON TblTSBToTypeXref.TypeID = TblType.TypeID) ON TblTSB.TSBID = TblTSBToTypeXref.TSBID) INNER JOIN (TblTSBToIssuerXref INNER JOIN TblIssuer ON TblTSBToIssuerXref.IssuerID = tblIssuer.IssuerID) ON TblTSB.TSBID = TblTSBToIssuerXref.TSBID WHERE (((TSB_Application.vehicletoengineconfigid)=" + vehicletoengineconfigid +")) ORDER BY TblAutoSystem.AutoSystemDescription");
			int i =0;
			while ( rs.next() )
			{
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("Vehicletoengineconfigid",rs.getInt("vehicletoengineconfigid"));
				jsonObject.put("Description",rs.getString("description"));
				jsonObject.put("Filenamepdf",rs.getString("filenamepdf"));
				jsonObject.put("Manufacturernum",rs.getString("manufacturernum"));
				jsonObject.put("Issuer",rs.getString("issuer"));
				jsonObject.put("Issuedate",rs.getString("issuedate"));
				jsonObject.put("Autosystemdescription",rs.getString("autosystemdescription"));
				jsonObject.put("Type",rs.getString("type"));
				jarray.put(i, jsonObject);
				i++;
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		res.put("TSBData :",jarray);
		result = "" + res;
		return Response.status(200).entity(result).build();

	}
	//Service Regarding to Get PM Data    
	@Path("servicesPMData/{makeid}/{modelid}/{yearid}/{econfigid}")
	@GET
	@Produces("application/json")
	public Response servicesPMData(@PathParam("makeid") String makeid, @PathParam("modelid") String modelid , @PathParam("yearid") String yearid , @PathParam("econfigid") String econfigid) throws JSONException{
		Connection conn = null;
		conn = connectToDatabaseOrDie();
		JSONObject res = new JSONObject();
		String result = null;
		JSONArray jarray= new JSONArray();
		int makeiddb  =0;
		int modeliddb  =0;
		int basevehicleid  =0;
		int vehicleid  =0;
		int vehicletoengineconfigid  =0;

		try{
			Statement st = conn.createStatement();
			ResultSet rs;

			rs = st.executeQuery( "select makeid from make where makeid =" + makeid ) ;
			while ( rs.next() ){
				makeiddb = rs.getInt("makeid");
			}

			rs = st.executeQuery("select modelid from model where modelid =" + modelid);
			while ( rs.next() ){
				modeliddb = rs.getInt("modelid");
			}

			rs = st.executeQuery("select basevehicleid from basevehicle where makeid = "+ makeiddb + " and modelid =" + modeliddb + " and yearid =" + yearid );
			while ( rs.next() ){
				basevehicleid = rs.getInt("basevehicleid");
			}
			System.out.println("BAseVehicle ID:"+ basevehicleid);
			/*
			    rs = st.executeQuery("select vehicleid from vehicle where basevehicleid = "+ basevehicleid + "limit 1");
			    while ( rs.next() ){
			    	vehicleid = rs.getInt("vehicleid");
			    }
			    // rs = st.executeQuery("select vehicletoengineconfigid from vehicletoengineconfig where vehicleid = "+ vehicleid + "AND engineconfigid = "+ econfigid );
			    rs = st.executeQuery("select vehicletoengineconfigid from vehicletoengineconfig where vehicleid = "+ vehicleid + "limit 1");
			    while ( rs.next() ){
			    	vehicletoengineconfigid = rs.getInt("vehicletoengineconfigid");
			    }
	  		    System.out.println("<<<<<<<<<<< vehicletoengineconfigid :  " + vehicletoengineconfigid);
			 */
			//rs = st.executeQuery("SELECT DISTINCT PM_ALL_Application.VehicleToEngineConfigID, PM_ALL.RecNo,PM_ALL.Qualifier, ITEM.Item, ACTION.Action, frequency.Freq, PM_ALL.IntMo, PM_ALL.IntMi, PM_ALL.IntKm, PM_ALL.W, PM_ALL.Ss, PM_ALL.Not1, PM_ALL.Not2 FROM (((((PM_ALL_Application INNER JOIN PM_ALL ON PM_ALL_Application.RecNo = PM_ALL.RecNo) INNER JOIN ACTION ON PM_ALL.Action = ACTION.CODE) INNER JOIN frequency ON PM_ALL.Freq = frequency.Code) INNER JOIN ITEM ON PM_ALL.Item = ITEM.CODE) LEFT JOIN WARRANTY ON PM_ALL.W = WARRANTY.CODE) LEFT JOIN NOTE_PM ON (PM_ALL.Not2 = NOTE_PM.CODE) AND (PM_ALL.Not1 = NOTE_PM.CODE)WHERE (((PM_ALL_Application.basevehicleid)=" + basevehicleid +"))");
			rs = st.executeQuery("SELECT DISTINCT pm_all.recno, pm_all_application.basevehicleiD, pm_all.qualifier, item.item,"
					+ " action.action, frequency.freq, pm_all.intmo, pm_all.intmi, pm_all.intkm "
					+ "FROM (((pm_all INNER JOIN pm_all_application ON pm_all.recno = pm_all_application.recno) "
					+ "INNER JOIN item ON pm_all.item = item.code) "
					+ "INNER JOIN frequency ON pm_all.freq = frequency.code) "
					+ "INNER JOIN action ON pm_all.action = action.code "
					+ "WHERE (((pm_all_application.basevehicleiD)=" + basevehicleid +")) order by frequency.freq");

			int i =0;
			while ( rs.next() )
			{
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("RecNo",rs.getString("RecNo"));
				jsonObject.put("Qualifier",rs.getString("Qualifier"));
				jsonObject.put("Item",rs.getString("Item"));
				jsonObject.put("Action",rs.getString("Action"));
				jsonObject.put("Freq",rs.getString("Freq"));
				jsonObject.put("IntMo",rs.getString("IntMo"));
				jsonObject.put("IntMi",rs.getString("IntMi"));
				jsonObject.put("IntKm",rs.getString("IntKm"));
				jarray.put(i, jsonObject);
				i++;
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		res.put("PMData :",jarray);
		result = "" + res;
		return Response.status(200).entity(result).build();

	}


	@Path("modeldetails/{makeid}")
	@GET
	@Produces("application/json")
	public Response servicesModelDetails(@PathParam("makeid") String makeid) throws JSONException{
		Connection conn = null;
		conn = connectToDatabaseOrDie();
		JSONObject res = new JSONObject();
		String result = null;
		JSONArray array=new JSONArray();

		try{
			Statement st = conn.createStatement();
			ResultSet rs1;
			rs1 = st.executeQuery("select distinct mo.modelid,mo.modelname,ma.makeid,ma.makename from basevehicle bas,make ma,model mo  where bas.makeid =ma.makeid   AND bas.modelid = mo.modelid AND ma.makeid = "+ makeid + " order by mo.modelname");
			while ( rs1.next() )
			{
				JSONObject jsonObject2=new JSONObject();

				jsonObject2.put("ModelId",rs1.getInt("modelid"));
				jsonObject2.put("ModelName",rs1.getString("modelname"));
				array.put(jsonObject2);

			}

		}
		catch (Exception e) {
			e.printStackTrace();
		}

		res.put("Models Details:",array);
		result = "" + res;
		return Response.status(200).entity(result).build();

	}

	@Path("makelist")
	@GET
	@Produces("application/json")
	public Response makeList() throws JSONException{
		Connection conn = null;
		conn = connectToDatabaseOrDie();
		JSONObject res = new JSONObject();
		String result = null;
		JSONArray jarray= new JSONArray();
		System.out.println("TEST M090");
		try{
			Statement st = conn.createStatement();
			ResultSet rs;

			rs = st.executeQuery("select * from make order by makename");
			int i =0;
			while ( rs.next() )
			{
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("makeId",rs.getString("makeid"));
				jsonObject.put("makeName",rs.getString("makename"));
				jarray.put(i, jsonObject);
				i++;
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		res.put("Make",jarray);
		result = "" + res;
		return Response.status(200).entity(result).build();

	}

	// ACTIVE to production

	//Service Regarding to Get EngineConfig Data    
	@Path("servicesEngineConfigData/{makeid}/{modelid}/{yearid}")
	@GET
	@Produces("application/json")
	public Response servicesEngineConfigData(
			@PathParam("makeid") String makeid1,
			@PathParam("modelid") String modelid1,
			@PathParam("yearid") String yearid) throws JSONException {
		Connection conn = null;
		conn = connectToDatabaseOrDie();
		JSONObject res = new JSONObject();
		String result = null;
		JSONArray jarray = new JSONArray();
		int makeid = 0;
		int modelid = 0;
		int basevehicleid = 0;
		int vehicleid = 0;
		int engineconfigid = 0;
		int enginebaseid = 0;

		try {
			Statement st = conn.createStatement();
			ResultSet rs;
			System.out.println("yearid : " + yearid);
			try {
				Integer.parseInt(yearid);
			} catch (NumberFormatException e) {
				res.put("msg :", "YearId Should be Integer Only.");
				result = "" + res;
				return Response.status(200).entity(result).build();
			}
			// rs = st.executeQuery( "select makeid from make where makename ="
			// + "'"+ makename +"'") ;
			rs = st.executeQuery("select makeid from make where makeid ="
					+ makeid1);
			while (rs.next()) {
				makeid = rs.getInt("makeid");
			}

			// rs =
			// st.executeQuery("select modelid from model where modelname =" +
			// "'"+ modelname +"'");
			rs = st.executeQuery("select modelid from model where modelid ="
					+ modelid1);
			while (rs.next()) {
				modelid = rs.getInt("modelid");
			}

			rs = st.executeQuery("select basevehicleid from basevehicle where makeid = "
					+ makeid
					+ " and modelid ="
					+ modelid
					+ " and yearid ="
					+ yearid);
			while (rs.next()) {
				basevehicleid = rs.getInt("basevehicleid");
			}

			// Query strat Point Find vehicleId
			rs = st.executeQuery("select vehicleid from vehicle where basevehicleid = "
					+ basevehicleid);
			StringBuilder sbf = new StringBuilder();
			while (rs.next()) {
				sbf.append(rs.getInt("vehicleid")).append(",").toString();
			}
			// Query End Point Find vehicleId
			// Query strat Point Find Engine Config Id
			try {
				rs = st.executeQuery("select engineconfigid from vehicletoengineconfig where vehicleid in ( "
						+ sbf.substring(0, sbf.length() - 1).toString() + " )");
				sbf.replace(0, sbf.length() - 1, "").toString();

				// Query End Point Find Engine Config Id
				// Query strat Point Find Engine Base ID
				StringBuilder sbf1 = new StringBuilder();
				while (rs.next()) {
					sbf1.append(rs.getInt("engineconfigid")).append(",")
					.toString();
				}
				rs = st.executeQuery("select enginebaseid  from engineconfig where engineconfigid in ( "
						+ sbf1.substring(0, sbf1.length() - 1).toString() + ")");
				StringBuilder sbf2 = new StringBuilder();
				while (rs.next()) {
					sbf2.append(rs.getInt("enginebaseid")).append(",")
					.toString();
				}
				rs = st.executeQuery("select distinct e.enginebaseid,eng.engineconfigid, e.liter , e.blocktype, e.cylinders from enginebase e,engineconfig eng where  e.enginebaseid = eng.enginebaseid and e.enginebaseid in ( "
						+ sbf2.substring(0, sbf2.length() - 1).toString()
						+ " ) order by e.enginebaseid ");
			} catch (NumberFormatException e) {
				System.out.println("Error : " + e.getMessage());
				e.printStackTrace();
			}
			/*
			 * rs = st.executeQuery(
			 * "select vehicleid from vehicle where basevehicleid = "+
			 * basevehicleid + "limit 1"); while ( rs.next() ){ vehicleid =
			 * rs.getInt("vehicleid"); }
			 *
			 * rs = st.executeQuery(
			 * "select engineconfigid from vehicletoengineconfig where vehicleid = "
			 * + vehicleid + "limit 1"); while ( rs.next() ){ engineconfigid =
			 * rs.getInt("engineconfigid"); }
			 *
			 * rs = st.executeQuery(
			 * "select enginebaseid  from engineconfig where engineconfigid = "+
			 * engineconfigid); while ( rs.next() ){ enginebaseid =
			 * rs.getInt("enginebaseid"); }
			 */

			// rs =
			// st.executeQuery("select distinct e.enginebaseid,eng.engineconfigid, e.liter , e.blocktype, e.cylinders from enginebase e,engineconfig eng,vehicletoengineconfig ve,vehicle veh where  e.enginebaseid = eng.enginebaseid and eng.engineconfigid =ve.engineconfigid and veh.vehicleid = ve.vehicleid and veh.basevehicleid = "+
			// basevehicleid + "order by e.enginebaseid limit 1");
			int i = 0;
			while (rs.next()) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("Value",
						rs.getString("blocktype") + rs.getString("cylinders")
						+ "-" + rs.getString("liter"));
				jsonObject.put("EngineBaseId", rs.getInt("enginebaseid"));
				jsonObject.put("Engineconfigid", rs.getInt("engineconfigid"));
				jarray.put(i, jsonObject);
				i++;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		res.put("EngineBaseData :", jarray);
		result = "" + res;
		return Response.status(200).entity(result).build();

	}		

	//Service Regarding to Get TSB Data    
	@Path("servicesPMData/{makename}/{modelname}/{yearid}")
	@GET
	@Produces("application/json")
	public Response servicesPMData(@PathParam("makename") String makename, @PathParam("modelname") String modelname , @PathParam("yearid") String yearid) throws JSONException{
		Connection conn = null;
		conn = connectToDatabaseOrDie();
		JSONObject res = new JSONObject();
		String result = null;
		JSONArray jarray= new JSONArray();
		int makeid =0;
		int modelid  =0;
		int basevehicleid  =0;
		int vehicleid  =0;
		int vehicletoengineconfigid  =0;

		try{
			Statement st = conn.createStatement();
			ResultSet rs;

			rs = st.executeQuery( "select makeid from make where makename =" + "'"+ makename +"'") ;
			while ( rs.next() ){
				makeid = rs.getInt("makeid");
			}

			rs = st.executeQuery("select modelid from model where modelname =" + "'"+ modelname +"'");
			while ( rs.next() ){
				modelid = rs.getInt("modelid");
			}

			rs = st.executeQuery("select basevehicleid from basevehicle where makeid = "+ makeid + " and modelid =" + modelid + " and yearid =" + yearid );
			while ( rs.next() ){
				basevehicleid = rs.getInt("basevehicleid");
			}
			/*
				rs = st.executeQuery("select vehicleid from vehicle where basevehicleid = "+ basevehicleid + "limit 1");
				while ( rs.next() ){
					vehicleid = rs.getInt("vehicleid");
				}

				rs = st.executeQuery("select vehicletoengineconfigid from vehicletoengineconfig where vehicleid = "+ vehicleid + "limit 1");
				while ( rs.next() ){
					vehicletoengineconfigid = rs.getInt("vehicletoengineconfigid");
				}

				System.out.println("<<<<<<<<<<< vehicletoengineconfigid :  " + vehicletoengineconfigid);
			 */
			rs = st.executeQuery("SELECT DISTINCT pm_all.recno, pm_all_application.basevehicleiD, pm_all.qualifier, item.item,"
					+ " action.action, frequency.freq, pm_all.intmo, pm_all.intmi, pm_all.intkm "
					+ "FROM (((pm_all INNER JOIN pm_all_application ON pm_all.recno = pm_all_application.recno) "
					+ "INNER JOIN item ON pm_all.item = item.code) "
					+ "INNER JOIN frequency ON pm_all.freq = frequency.code) "
					+ "INNER JOIN action ON pm_all.action = action.code "
					+ "WHERE (((pm_all_application.basevehicleiD)=" + basevehicleid +")) order by frequency.freq");

			int i =0;
			while ( rs.next() )
			{
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("RecNo",rs.getString("RecNo"));
				jsonObject.put("Qualifier",rs.getString("Qualifier"));
				jsonObject.put("Item",rs.getString("Item"));
				jsonObject.put("Action",rs.getString("Action"));
				jsonObject.put("Freq",rs.getString("Freq"));
				jsonObject.put("IntMo",rs.getString("IntMo"));
				jsonObject.put("IntMi",rs.getString("IntMi"));
				jsonObject.put("IntKm",rs.getString("IntKm"));
				jarray.put(i, jsonObject);
				i++;
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		res.put("PMData :",jarray);
		result = "" + res;
		return Response.status(200).entity(result).build();

	}



	//Service Regarding to Get TSB Data    
	@Path("servicesTSBData/{makename}/{modelname}/{yearid}")
	@GET
	@Produces("application/json")
	public Response servicesTSBData(@PathParam("makename") String makename, @PathParam("modelname") String modelname , @PathParam("yearid") String yearid) throws JSONException{
		Connection conn = null;
		conn = connectToDatabaseOrDie();
		JSONObject res = new JSONObject();
		String result = null;
		JSONArray jarray= new JSONArray();
		int makeid =0;
		int modelid  =0;
		int basevehicleid  =0;
		int vehicleid  =0;
		int vehicletoengineconfigid  =0;

		try{
			Statement st = conn.createStatement();
			ResultSet rs;

			rs = st.executeQuery( "select makeid from make where makename =" + "'"+ makename +"'") ;
			while ( rs.next() ){
				makeid = rs.getInt("makeid");
			}

			rs = st.executeQuery("select modelid from model where modelname =" + "'"+ modelname +"'");
			while ( rs.next() ){
				modelid = rs.getInt("modelid");
			}

			/* rs = st.executeQuery("select submodelid from submodel where submodelname  =" + "'"+ submodelname +"'");
  		    while ( rs.next() ){
  		    	submodelid = rs.getInt("submodelid");
  		    }*/

			rs = st.executeQuery("select basevehicleid from basevehicle where makeid = "+ makeid + " and modelid =" + modelid + " and yearid =" + yearid );
			while ( rs.next() ){
				basevehicleid = rs.getInt("basevehicleid");
			}

			rs = st.executeQuery("select vehicleid from vehicle where basevehicleid = "+ basevehicleid + "limit 1");
			while ( rs.next() ){
				vehicleid = rs.getInt("vehicleid");
				System.out.println("Vehicle ID:" + vehicleid);
			}

			rs = st.executeQuery("select vehicletoengineconfigid from vehicletoengineconfig where vehicleid = "+ vehicleid + "limit 1");
			while ( rs.next() ){
				vehicletoengineconfigid = rs.getInt("vehicletoengineconfigid");
			}

			rs = st.executeQuery("SELECT TSB_Application.vehicletoengineconfigid, tblTSB.tsbid, tblTSB.description, tblTSB.filenamepdf,tblTSB.manufacturernum,tblIssuer.issuer,tblTSB.issuedate, tblAutoSystem.autosystemdescription, tblType.type FROM ((TSB_Application INNER JOIN ((TblTSBToAutoSystemXref INNER JOIN TblAutoSystem ON TblTSBToAutoSystemXref.autosystemid = TblAutoSystem.autosystemid) INNER JOIN TblTSB ON TblTSBToAutoSystemXref.tsbid = TblTSB.tsbid) ON TSB_Application.tsbid = TblTSB.tsbid) INNER JOIN (TblTSBToTypeXref INNER JOIN TblType ON TblTSBToTypeXref.TypeID = TblType.TypeID) ON TblTSB.TSBID = TblTSBToTypeXref.TSBID) INNER JOIN (TblTSBToIssuerXref INNER JOIN TblIssuer ON TblTSBToIssuerXref.IssuerID = tblIssuer.IssuerID) ON TblTSB.TSBID = TblTSBToIssuerXref.TSBID WHERE (((TSB_Application.vehicletoengineconfigid)=" + vehicletoengineconfigid +")) ORDER BY TblAutoSystem.AutoSystemDescription");
			int i =0;
			while ( rs.next() )
			{
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("Vehicletoengineconfigid",rs.getInt("vehicletoengineconfigid"));
				jsonObject.put("Description",rs.getString("description"));
				jsonObject.put("Filenamepdf",rs.getString("filenamepdf"));
				jsonObject.put("Manufacturernum",rs.getString("manufacturernum"));
				jsonObject.put("Issuer",rs.getString("issuer"));
				jsonObject.put("Issuedate",rs.getString("issuedate"));
				jsonObject.put("Autosystemdescription",rs.getString("autosystemdescription"));
				jsonObject.put("Type",rs.getString("type"));
				jarray.put(i, jsonObject);
				i++;
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		res.put("TSBData :",jarray);
		result = "" + res;
		return Response.status(200).entity(result).build();

	}

	@Path("twolib")
	@GET
	@Produces("application/json")
	public Response twoLib() throws JSONException{
		Connection conn = null;
		conn = connectToDatabaseOrDie();
		JSONObject res = new JSONObject();
		String result = null;
		System.out.println("TEST M090");
		int i = 0;

		String csvFile = null;
		BufferedReader br0 = null, br1 = null, br2 = null, br3 = null, br4 = null, br5 = null;
		String line = "";

		try {


			csvFile = "/home/surendar/Documents/Technical_Service_Bulletins_ACES_Sample_20140918/tblSystem.txt";

			Statement st0 = conn.createStatement();
			st0.execute("Truncate table tbl_system");

			i = 0;
			br0 = new BufferedReader(new FileReader(csvFile));
			while ((line = br0.readLine()) != null) {
				if(i == 0){
				}else{
					String[] fields = line.split("\\|");
					Statement st = conn.createStatement();
					st.execute("Insert into tbl_system" +
							"(systemid, system_description, created_date)" +
							"VALUES" +
							"("+fields[0] +",'"+ fields[1] +"','"+ fields[2] +"')");
				}
				i++;
			}

			csvFile = "/home/surendar/Documents/Technical_Service_Bulletins_ACES_Sample_20140918/tblTSB.txt";
			//Statement st1 = conn.createStatement();
			//st1.execute("Truncate table tbl_tsb");

			i=0;
			br1 = new BufferedReader(new FileReader(csvFile));
			while ((line = br1.readLine()) != null) {
				if(i == 0){
				}else{
					String[] fields = line.split("\\|");
					Statement st = conn.createStatement();
					st.execute("Insert into tbl_tsb" +
							"(pritsbid, tsbid,  description, filename_pdf, manufacturer_num, issue_date, tsb_text, created_date, modified_date)" +
							"VALUES" +
							"(" +i+ ","+fields[0] +",'"+ fields[1] +"','"+ fields[2] +"','"+ fields[3] +"','"+ fields[4] +"','"+ fields[5] +"','"+ fields[6] +"','"+ fields[7] +"')");
				}
				i++;
			}


			csvFile = "/home/surendar/Documents/Technical_Service_Bulletins_ACES_Sample_20140918/tblSubComponent.txt";

			Statement st2 = conn.createStatement();
			st2.execute("Truncate table tbl_sub_component");


			i=0;
			br2 = new BufferedReader(new FileReader(csvFile));
			while ((line = br2.readLine()) != null) {
				if(i == 0){
				}else{
					String[] fields = line.split("\\|");
					Statement st = conn.createStatement();
					st.execute("Insert into tbl_sub_component" +
							"(subcomponentid, sub_component, tblcomponentid, created_date)" +
							"VALUES" +
							"("+fields[0] +",'"+ fields[1] +"',"+ fields[2] +",'"+ fields[3] +"')");
				}
				i++;
			}


			csvFile = "/home/surendar/Documents/Technical_Service_Bulletins_ACES_Sample_20140918/tblSubProblem.txt";

			Statement st3 = conn.createStatement();
			st3.execute("Truncate table tbl_sub_problem");

			i=0;
			br3 = new BufferedReader(new FileReader(csvFile));
			while ((line = br3.readLine()) != null) {
				if(i == 0){
				}else{
					String[] fields = line.split("\\|");
					Statement st = conn.createStatement();
					st.execute("Insert into tbl_sub_problem" +
							"(subproblemid, sub_problem, problemid, created_date)" +
							"VALUES" +
							"("+fields[0] +",'"+ fields[1] +"',"+ fields[2] +",'"+ fields[3] +"')");
				}
				i++;
			}

			csvFile = "/home/surendar/Documents/Technical_Service_Bulletins_ACES_Sample_20140918/tblSubSystem.txt";

			Statement st4 = conn.createStatement();
			st4.execute("Truncate table tbl_sub_system");

			i=0;
			br4 = new BufferedReader(new FileReader(csvFile));
			while ((line = br4.readLine()) != null) {
				if(i == 0){
				}else{
					String[] fields = line.split("\\|");
					Statement st = conn.createStatement();
					st.execute("Insert into tbl_sub_system" +
							"(subsystemid, sub_system, systemid, created_date)" +
							"VALUES" +
							"("+fields[0] +",'"+ fields[1] +"',"+ fields[2] +",'"+ fields[3] +"')");
				}
				i++;
			}

			csvFile = "/home/surendar/Documents/Technical_Service_Bulletins_ACES_Sample_20140918/tblTSBToAutoSystemXref.txt";

			Statement st5 = conn.createStatement();
			st5.execute("Truncate table tbl_tsb_auto_system");

			i=0;
			br5 = new BufferedReader(new FileReader(csvFile));
			while ((line = br5.readLine()) != null) {
				if(i == 0){
				}else{
					String[] fields = line.split("\\|");
					Statement st = conn.createStatement();
					st.execute("Insert into tbl_tsb_auto_system" +
							"(pritsbid, tsbid, autosystemid, created_date)" +
							"VALUES" +
							"(" +i+","+fields[0] +","+ fields[1] +",'"+ fields[2] +"')");
				}
				i++;
			}


		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			if (br0 != null) {
				try {
					br0.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (br1 != null) {
				try {
					br1.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (br2 != null) {
				try {
					br2.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (br3 != null) {
				try {
					br3.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (br4 != null) {
				try {
					br4.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (br5 != null) {
				try {
					br5.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}


		res.put("Test","Done");
		result = "" + res;
		return Response.status(200).entity(result).build();

	}



	@Path("onelib")
	@GET
	@Produces("application/json")
	public Response oneLib() throws JSONException{
		Connection conn = null;
		conn = connectToDatabaseOrDie();
		JSONObject res = new JSONObject();
		String result = null;
		System.out.println("TEST M090");
		int i = 0;

		String csvFile = null;
		BufferedReader br0 = null, br1 = null, br2 = null, br3 = null, br4 = null, br5 = null;
		String line = "";

		try {

			csvFile = "/home/surendar/Documents/Technical_Service_Bulletins_ACES_Sample_20140918/tblAutoSystem.txt";

			i = 0;
			br0 = new BufferedReader(new FileReader(csvFile));
			while ((line = br0.readLine()) != null) {
				if(i == 0){
				}else{
					String[] fields = line.split("\\|");
					Statement st = conn.createStatement();
					st.execute("Insert into tbl_auto_system" +
							"(autosystemid, auto_system_description, created_date)" +
							"VALUES" +
							"("+fields[0] +",'"+ fields[1] +"','"+ fields[2] +"')");
				}
				i++;
			}

			csvFile = "/home/surendar/Documents/Technical_Service_Bulletins_ACES_Sample_20140918/tblComponent.txt";

			i=0;
			br1 = new BufferedReader(new FileReader(csvFile));
			while ((line = br1.readLine()) != null) {
				if(i == 0){
				}else{
					String[] fields = line.split("\\|");
					Statement st = conn.createStatement();
					st.execute("Insert into tbl_component" +
							"(tblcomponentid, component, created_date)" +
							"VALUES" +
							"("+fields[0] +",'"+ fields[1] +"','"+ fields[2] +"')");
				}
				i++;
			}


			csvFile = "/home/surendar/Documents/Technical_Service_Bulletins_ACES_Sample_20140918/tblDTC.txt";

			i=0;
			br2 = new BufferedReader(new FileReader(csvFile));
			while ((line = br2.readLine()) != null) {
				if(i == 0){
				}else{
					String[] fields = line.split("\\|");
					Statement st = conn.createStatement();
					st.execute("Insert into tbl_dtc" +
							"(dtcid, dtc, created_date)" +
							"VALUES" +
							"("+fields[0] +",'"+ fields[1] +"','"+ fields[2] +"')");
				}
				i++;
			}


			csvFile = "/home/surendar/Documents/Technical_Service_Bulletins_ACES_Sample_20140918/tblIssuer.txt";

			i=0;
			br3 = new BufferedReader(new FileReader(csvFile));
			while ((line = br3.readLine()) != null) {
				if(i == 0){
				}else{
					String[] fields = line.split("\\|");
					Statement st = conn.createStatement();
					st.execute("Insert into tbl_issuer" +
							"(issuerid, issuer, created_date)" +
							"VALUES" +
							"("+fields[0] +",'"+ fields[1] +"','"+ fields[2] +"')");
				}
				i++;
			}

			csvFile = "/home/surendar/Documents/Technical_Service_Bulletins_ACES_Sample_20140918/tblProblem.txt";

			i=0;
			br4 = new BufferedReader(new FileReader(csvFile));
			while ((line = br4.readLine()) != null) {
				if(i == 0){
				}else{
					String[] fields = line.split("\\|");
					Statement st = conn.createStatement();
					st.execute("Insert into tbl_problem" +
							"(problemid, problem, created_date)" +
							"VALUES" +
							"("+fields[0] +",'"+ fields[1] +"','"+ fields[2] +"')");
				}
				i++;
			}


			csvFile = "/home/surendar/Documents/Technical_Service_Bulletins_ACES_Sample_20140918/tblSymptom.txt";

			i=0;
			br5 = new BufferedReader(new FileReader(csvFile));
			while ((line = br5.readLine()) != null) {
				if(i == 0){
				}else{
					String[] fields = line.split("\\|");
					Statement st = conn.createStatement();
					st.execute("Insert into tbl_symptom" +
							"(symptomid, symptom_description, created_date)" +
							"VALUES" +
							"("+fields[0] +",'"+ fields[1] +"','"+ fields[2] +"')");
				}
				i++;
			}


		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			if (br0 != null) {
				try {
					br0.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (br1 != null) {
				try {
					br1.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (br2 != null) {
				try {
					br2.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (br3 != null) {
				try {
					br3.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (br4 != null) {
				try {
					br4.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (br5 != null) {
				try {
					br5.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}


		res.put("Test","Done");
		result = "" + res;
		return Response.status(200).entity(result).build();

	}



	@Path("testlib")
	@GET
	@Produces("application/json")
	public Response testLib() throws JSONException{
		Connection conn = null;
		conn = connectToDatabaseOrDie();
		JSONObject res = new JSONObject();
		String result = null;
		JSONArray jarray= new JSONArray();
		//System.out.println("TEST M090");

		String iTemp  = null;
		String iNo = "NO";

		/*
		final File folder = new File("/home/surendar/Documents/Technical_Service_Bulletins_ACES_Sample_20140918");
		for (final File fileEntry : folder.listFiles()) {
			if (fileEntry.isDirectory()) {
				//listFilesForFolder(fileEntry);
				System.out.println("Folder detected");
			} else {
				System.out.println(fileEntry.getName());
				try{
					BufferedReader in = new BufferedReader(new FileReader("/home/surendar/Documents/Technical_Service_Bulletins_ACES_Sample_20140918/"+fileEntry.getName()));
					String str;
					str = in.readLine();
					System.out.println(str);
					while ((str = in.readLine()) != null) {
						int k = 0;
						System.out.println("ZZZZ" + k  + str);
						//if(k == 1){
						//System.out.println(k  + str);
						//}else{
						//	System.out.println("No");
						//}
						k++;
					}
				}
				catch (IOException e) {
					System.out.println("File Read Error");
				}
			}
		}
		 */


		try{
			BufferedReader in = new BufferedReader(new FileReader("/home/surendar/Documents/Technical_Service_Bulletins_ACES_Sample_20140918/tblAutoSystem.txt"));
			String str;
			str = in.readLine();
			//System.out.println("AAAA" + str);
			while ((str = in.readLine()) != null) {
				int k = 0;
				System.out.println("ZZZZ" + k  + str);
				k++;
			}
		}
		catch (IOException e) {
			System.out.println("File Read Error");
		}	


		try{
			Statement st = conn.createStatement();
			ResultSet rs;

			rs = st.executeQuery("select * from video_library");
			int i =0;
			while ( rs.next() )
			{
				iTemp = null;


				JSONObject jsonObject = new JSONObject();
				jsonObject.put("videoLibraryId",rs.getString("video_library_id"));
				//System.out.println(rs.getString("video_library_id"));
				jsonObject.put("videoTitle",rs.getString("video_title"));
				jsonObject.put("videoSource",rs.getString("video_source"));
				jsonObject.put("videoDescription",rs.getString("video_description"));
				jarray.put(i, jsonObject);
				i++;
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		res.put("VideoLibrary",jarray);
		result = "" + res;
		return Response.status(200).entity(result).build();

	}

	@Path("videolibrary")
	@GET
	@Produces("application/json")
	public Response videoLibrary() throws JSONException{
		Connection conn = null;
		conn = connectToDatabaseOrDie();
		JSONObject res = new JSONObject();
		String result = null;
		JSONArray jarray= new JSONArray();
		System.out.println("TEST M090");
		try{
			Statement st = conn.createStatement();
			ResultSet rs;

			rs = st.executeQuery("select * from video_library");
			int i =0;
			while ( rs.next() )
			{
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("videoLibraryId",rs.getString("video_library_id"));
				System.out.println(rs.getString("video_library_id"));
				jsonObject.put("videoTitle",rs.getString("video_title"));
				jsonObject.put("videoSource",rs.getString("video_source"));
				jsonObject.put("videoDescription",rs.getString("video_description"));
				jarray.put(i, jsonObject);
				i++;
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		res.put("VideoLibrary",jarray);
		result = "" + res;
		return Response.status(200).entity(result).build();

	}

	@POST
	@Path("/imageSubmission")
	@Consumes("multipart/form-data")
	public Response imageSubmission(FormDataMultiPart multiPart) throws JSONException{

		Connection conn = null;
		conn = connectToDatabaseOrDie();

		String submissionId = null;
		String userId = null;
		String contentHeader=null, fileName=null;
		BodyPartEntity bpe = null;
		InputStream imageStream = null;
		long epoch;
		MultivaluedMap <String, String> mvMap = null;
		List<String> headerList = null;


		Map<String,List<FormDataBodyPart>> bodyList = multiPart.getFields();
		for (String key:bodyList.keySet()){
			if(key.equals("submission_id")){
				submissionId = multiPart.getField(key).getValue();
			}
			if(key.equals("user_id")){
				userId = multiPart.getField(key).getValue();
			}

		}

		for (String key:bodyList.keySet()){
			if(key.equals("sign")){
				//System.out.println("Inside"+key);
				mvMap = multiPart.getField(key).getHeaders();
				headerList  =  mvMap.get("Content-Disposition");
				contentHeader = headerList.get(0);

				fileName = parseValueFromHeader(contentHeader, "filename");
				epoch = System.currentTimeMillis()/1000;
				fileName = epoch+fileName;
				//String uploadedFileLocation = "/home/surendar/Downloads/" + fileName;
				String uploadedFileLocation = "/home/innoppl/CVIFTabletApp/images/" + fileName;


				bpe = ((BodyPartEntity) multiPart.getField(key).getEntity());
				imageStream = bpe.getInputStream();

				saveToFile(imageStream, uploadedFileLocation);

				try{
					Statement st = conn.createStatement();
					st.execute("Insert into form_submission_sign_image" +
							"(form_submission_id, user_id, image_file_name)" +
							"VALUES" +
							"("+submissionId +","+ userId +",'"+ fileName +"')");
				}
				catch (Exception e) {
					e.printStackTrace();
				}
				imageStream = null;
				fileName = null;
				bpe = null;
			}

			if(!key.equals("submission_id") && !key.equals("user_id") && !key.equals("sign")){
				//System.out.println("Inside"+key);
				mvMap = multiPart.getField(key).getHeaders();
				headerList  =  mvMap.get("Content-Disposition");
				contentHeader = headerList.get(0);

				fileName = parseValueFromHeader(contentHeader, "filename");
				epoch = System.currentTimeMillis()/1000;
				fileName = epoch+fileName;
				//String uploadedFileLocation = "/home/surendar/Downloads/" + fileName;
				String uploadedFileLocation = "/home/innoppl/CVIFTabletApp/images/" + fileName;


				bpe = ((BodyPartEntity) multiPart.getField(key).getEntity());
				imageStream = bpe.getInputStream();

				saveToFile(imageStream, uploadedFileLocation);

				try{
					Statement st = conn.createStatement();
					st.execute("Insert into form_submission_image" +
							"(form_submission_id, user_id, image_file_name)" +
							"VALUES" +
							"("+submissionId +","+ userId +",'"+ fileName +"')");
				}
				catch (Exception e) {
					e.printStackTrace();
				}
				imageStream = null;
				fileName = null;
				bpe = null;
			}
		}

		JSONObject jsonObject = new JSONObject();

		jsonObject.put("submissionID", submissionId);
		jsonObject.put("userID", userId);

		String result = ""+jsonObject;

		return Response.status(200)
				.entity(result)
				.build();
	}



	@POST
	@Path("/camImageSubmission")
	@Consumes("multipart/form-data")
	public Response camImageSubmission(FormDataMultiPart multiPart) throws JSONException{

		Connection conn = null;
		conn = connectToDatabaseOrDie();

		String submissionId = null;
		String userId = null;
		String imageDesc = null;
		String contentHeader=null, fileName=null;
		BodyPartEntity bpe = null;
		InputStream imageStream = null;
		long epoch;
		MultivaluedMap <String, String> mvMap = null;
		List<String> headerList = null;


		Map<String,List<FormDataBodyPart>> bodyList = multiPart.getFields();
		for (String key:bodyList.keySet()){
			if(key.equals("submission_id")){
				submissionId = multiPart.getField(key).getValue();
			}
			if(key.equals("user_id")){
				userId = multiPart.getField(key).getValue();
			}
			if(key.equals("desc")){
				imageDesc = multiPart.getField(key).getValue();
			}

		}

		for (String key:bodyList.keySet()){
			//if(key.equals("sign")){
			//System.out.println("Inside"+key);
			mvMap = multiPart.getField(key).getHeaders();
			headerList  =  mvMap.get("Content-Disposition");
			contentHeader = headerList.get(0);

			fileName = parseValueFromHeader(contentHeader, "filename");
			epoch = System.currentTimeMillis()/1000;
			fileName = epoch+fileName;
			//String uploadedFileLocation = "/home/surendar/Downloads/" + fileName;
			String uploadedFileLocation = "/home/innoppl/CVIFTabletApp/images/" + fileName;


			bpe = ((BodyPartEntity) multiPart.getField(key).getEntity());
			imageStream = bpe.getInputStream();

			saveToFile(imageStream, uploadedFileLocation);

			try{
				Statement st = conn.createStatement();
				st.execute("Insert into form_submission_cam_image" +
						"(form_submission_id, user_id, image_file_name, image_description)" +
						"VALUES" +
						"("+submissionId +","+ userId +",'"+ fileName +"','"+ imageDesc +"')");
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			imageStream = null;
			fileName = null;
			bpe = null;
			//}

		}

		JSONObject jsonObject = new JSONObject();

		jsonObject.put("submissionID", submissionId);
		jsonObject.put("userID", userId);

		String result = ""+jsonObject;

		return Response.status(200)
				.entity(result)
				.build();
	}



	// save uploaded file to new location
	private void saveToFile(InputStream uploadedInputStream,
			String uploadedFileLocation) {

		try {
			OutputStream out = null;
			int read = 0;
			byte[] bytes = new byte[1024];

			out = new FileOutputStream(new File(uploadedFileLocation));
			while ((read = uploadedInputStream.read(bytes)) != -1) {
				out.write(bytes, 0, read);
			}
			out.flush();
			out.close();
		} catch (Exception e) {

			e.printStackTrace();
		}

	}



	@Path("interview")
	@GET
	@Produces("application/json")
	public Response servicesDescription() throws JSONException{
		JSONObject res = new JSONObject();
		String result = null;
		JSONArray jarray= new JSONArray();

		JSONObject jsonObject = new JSONObject();
		jsonObject.put("Title","Culinary Fruits");
		jsonObject.put("Description","Description One");
		jsonObject.put("Date","12/09/2000");
		jsonObject.put("Image","http://upload.wikimedia.org/wikipedia/commons/thumb/2/2f/Culinary_fruits_front_view.jpg/220px-Culinary_fruits_front_view.jpg");
		jarray.put(0, jsonObject);

		jsonObject = new JSONObject();
		jsonObject.put("Title","Beautiful Fruits");
		jsonObject.put("Description","Second Description");
		jsonObject.put("Date","10/06/1958");
		jsonObject.put("Image","http://1.bp.blogspot.com/-iBhxECHLyMo/UR3lM23ix8I/AAAAAAAACdw/faI0vTzUn94/s320/beautiful+fruits+wallpapers+28.jpg");
		jarray.put(1, jsonObject);

		jsonObject = new JSONObject();
		jsonObject.put("Title","Fruits Photo");
		jsonObject.put("Description","Desctiption Three");
		jsonObject.put("Date","17/09/1596");
		jsonObject.put("Image","http://www.leanitup.com/wp-content/uploads/2012/10/3635-fruit-photo.jpg");
		jarray.put(2, jsonObject);

		jsonObject = new JSONObject();
		jsonObject.put("Title","Fruits & Vegetables");
		jsonObject.put("Description","Description Four");
		jsonObject.put("Date","13/05/1695");
		jsonObject.put("Image","http://aseatatmytable.files.wordpress.com/2013/01/fruits-fruit-20461047-2560-1856.jpg");
		jarray.put(3, jsonObject);

		jsonObject = new JSONObject();
		jsonObject.put("Title","Apple World");
		jsonObject.put("Description","Description Five");
		jsonObject.put("Date","05/12/1932");
		jsonObject.put("Image","http://kpobococ.github.io/XtLightbox/images/fruit-1.jpg");
		jarray.put(4, jsonObject);

		jsonObject = new JSONObject();
		jsonObject.put("Title","Sliced Fruit");
		jsonObject.put("Description","Description Six");
		jsonObject.put("Date","02/06/2013");
		jsonObject.put("Image","http://images4.fanpop.com/image/photos/20400000/Fruits-fruit-20460964-1000-1024.jpg");
		jarray.put(5, jsonObject);

		res.put("Services",jarray);
		result = "" + res;
		return Response.status(200).entity(result).build();

	}


	@Path("historyFieldsFillup/{vin}")
	@GET
	@Produces("application/json")
	public Response historyFieldsFillup(@PathParam("vin") String vin) throws JSONException{
		Connection conn = null;
		conn = connectToDatabaseOrDie();
		JSONObject jsonObject = new JSONObject();
		String result = null, tempContent = null;
		int submissionID = 0;

		try{
			Statement st = conn.createStatement();
			ResultSet rs;
			rs = st.executeQuery("select form_submission_id from form_submission_profile_data where (profile_value = '"+ vin +"' and not profile_value =  '') and profile_field = 'vin'");
			if ( rs.next() ){
				submissionID = rs.getInt("form_submission_id");
				rs = st.executeQuery("select * from form_submission_profile_data where form_submission_id = '"+ submissionID +"'");
				while ( rs.next() ){
					tempContent = tempContent +" "+ rs.getString("profile_field") + "=\"" + rs.getString("profile_value") + "\"; ";

					jsonObject.put("form_submission_id",parseValueFromHeader(tempContent, "form_submission_id"));
					jsonObject.put("first_name",parseValueFromHeader(tempContent, "first_name"));
					jsonObject.put("last_name",parseValueFromHeader(tempContent, "last_name"));
					jsonObject.put("mobile_no",parseValueFromHeader(tempContent, "mobile_no"));
					jsonObject.put("vin",parseValueFromHeader(tempContent, "vin")); 
					jsonObject.put("make",parseValueFromHeader(tempContent, "make")); 
					jsonObject.put("model",parseValueFromHeader(tempContent, "model")); 
					jsonObject.put("year",parseValueFromHeader(tempContent, "year")); 
					jsonObject.put("mileage",parseValueFromHeader(tempContent, "mileage"));
					jsonObject.put("Comments",parseValueFromHeader(tempContent, "Comments"));
					jsonObject.put("Date",parseValueFromHeader(tempContent, "Date"));
					jsonObject.put("alternative_mobileno",parseValueFromHeader(tempContent, "alternative_mobileno"));


				}	

			}else{
				jsonObject.put("Error", "VIN number not found");
			}

		}
		catch (Exception e) {
			e.printStackTrace();
		}


		result = "" + jsonObject;
		return Response.status(200).entity(result).build();

	}


	@Path("servicesDescription/{u}/{id}")
	@GET
	@Produces("application/json")
	public Response servicesDescription(@PathParam("u") int u, @PathParam("id") int id) throws JSONException{
		Connection conn = null;
		conn = connectToDatabaseOrDie();
		JSONObject res = new JSONObject();
		String result = null;
		JSONArray jarray= new JSONArray();
		int userServiceID =0;

		try{
			Statement st = conn.createStatement();
			ResultSet rs;
			rs = st.executeQuery("select * from user_services where user_id = "+ u +" and service_id = "+ id);
			while ( rs.next() ){
				userServiceID = rs.getInt("user_service_id");
			}

			rs = st.executeQuery("select service_position_id, service_content from services_block_components where user_service_id =" + userServiceID + " and user_id =" + u + " order by service_position_id");
			int i =0;
			while ( rs.next() )
			{
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("positionId",rs.getInt("service_position_id"));
				jsonObject.put("content",rs.getString("service_content"));
				jarray.put(i, jsonObject);
				i++;
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		res.put("ServicesDescription",jarray);
		result = "" + res;
		return Response.status(200).entity(result).build();

	}

	@Path("fourHtml/{u}")
	@GET
	@Produces("text/html")
	public Response fourHtml(@PathParam("u") int u){
		Connection conn = null;
		conn = connectToDatabaseOrDie();
		String result = null;
		int submissionId = u;
		int i =0, j=0, k=0;
		int imageSize = 0, imageCamSize = 0;
		String ultimateContent = "", tempContent = "", css = "", profileContent= "";
		String firstName = "", lastName = "", mobileNo ="", alternativeMobileNo= "", serviceType= "";
		String vin = "", make = "", model ="", year ="", mileage= "", comments ="", formDate="";
		String[] tempImageContent = new String[125];
		String[] tempImageCamContent = new String[125];
		String[] tempImageCamDesc = new String[125];
		String imageContent = "", signImage = "", footContent1 = "", footContent2 = "", tick ="";
		String [][] tempBlockOneData = new String[25][2];
		String [][] tempBlockTwoData = new String[25][2];
		String [][] tempBlockThreeData = new String[25][2];
		String [][] tempBlockFourZeroData = new String[25][2];
		String [][] tempBlockFourOneData = new String[25][2];
		String [][] tempBlockFourTwoData = new String[25][2];
		String [][] tempBlockFourThreeData = new String[25][2];
		String [][] tempBlockBatteryData = new String[25][2];
		String [][] tempBlockInchesData = new String[25][2];
		String [][] tempBlockFiveData = new String[25][2];
		String [][] tempBlockPressureData = new String[25][2];
		String[] tempBlockServiceData = new String[25];
		String tempBlockPressureImgData = "", tempBlockPressureImgContent ="";
		String headerImageOne = "", headerImageTwo ="", headerImageThree ="";
		String blockOneContent = "", blockTwoContent = "", blockThreeContent = "", blockFourZeroLeftContent = "", blockFourZeroRightContent = "", blockFourOneContent = "", blockFourTwoContent = "", blockFourThreeContent = "", blockBatteryContent= "", interBlockFourContent = "", blockFiveContent = "", tempServiceContent = "", tempServiceFinalContent = "";
		int b1Size = 0, b2Size= 0, b3Size=0, b41Size = 0, b42Size = 0, b43Size = 0, bbSize = 0, tempUserId = 0;

		css = "<style type='text/css'>body{font-family: 'Helvetica'; }body,*{margin:0;	padding:0;}h1{	width:960px;height:44px;background-image:url("+Configuration.SERVICE_URL+"/images/system_files/tile_img.png);line-height:44px;color:#FFF;	font-size:18px;text-align:center;font-family:'Trebuchet MS', Arial, Helvetica, sans-serif;}#wrapper{width:960px; background-color:#FFF; 	margin:auto;}.inner-head {text-align:center;	width:960px;	height:40px;	}.inner-logo{	border:1px solid #000;	width:385px;	height:100px;	text-align:center;	float:left;	position:relative;left:290px;}.profile_title{		width:960px;text-align:left;height:35px;color:#000;font-size:15px;font-family:'Trebuchet MS', Arial, Helvetica, sans-serif;background-color:#e8e8e8;margin-top:10px;line-height:35px;}.userlist{font-size:13px;font-weight:bold;}.profile_title span{ font-weight:bold;	font-size:15px;padding-left:50px;}.profile-form-left ul{	float:left; text-align:left;	padding-left:50px;	width:440px;	padding-top:10px;}.profile-form-left ul li{	list-style:none;	height:50px;}.profile-form-left ul li label{	width:150px;	font-size:14px;	display:inline-block;}.profile-form-right ul{	float:right; text-align:left;	width:440px;	padding-top:10px;}.profile-form-right ul li{	list-style:none;	height:50px; font-size:14px;}.profile-form-right ul li label{	width:150px;	font-size:14px;	display:inline-block;}.photo-slider{	float:left; padding-left:30px; width:930px; background-color:#e8e8e8; height:35px; text-align:left;}.photo-slider span{	position:relative; background-color:#e8e8e8; font-weight:bold; 	padding-left:20px;	line-height:35px; font-size:14px;}.photo{	float:left;	padding-top:20px;	padding-left:50px;	width:910px;}.photo img{	float:left;  padding-right:10px; padding-bottom:10px;}.mileage{	float:left;	width:960px;	height:20px;	position:relative;	top:5px;	text-align:center;}.inn{	width:960px;	margin-top:210px;}.inn-left {	width:450px;	float:left;	padding:20px 0 0 50px;}.inn-left ul{	width:400px;	float:left;	padding:20px 0 0 50px;}.inn-left ul li{	list-style:none;	width:200px;	height:70px;}.inn-text{	color:mediumblue;	font-weight:bold;	font-size:20px;	position:relative;	bottom:5px;}.inn-black{font-weight:bold;	font-size:15px;	color:#000;	position:relative;	bottom:5px;}.inn-right{	width:400px;	float:left;	padding:20px 0 0 50px;}.inn-right ul{	width:450px;	float:left;	padding:20px 0 0 50px;}.inn-right ul li{	list-style:none;	width:200px;	height:70px;}.comments{	float:left; font-size:14px; font-weight:bold; margin-top:10px; padding-left:50px; width:910px; background-color:#e8e8e8; height:35px; line-height:35px; text-align:left; margin-bottom:10px;}.dat{	width:400px; line-height:35px;	height:50px;float:left;} p > input {    float: left;    height: 16px;    left: 0;    margin: 0;        padding: 0;    width: 16px;}p > label {    color: #000000;    float: left;    line-height: 16px;    padding: 0 0 0 5px;	font-size:15px;}.name{	width:320px;	height:50px;	float:left;}.mil{	width:320px;	height:50px;	float:left;}.make{	width:320px;	height:50px;	float:left;}.chk-mark{float:left;height:50px;}.chk-ok{	float:left;	padding: 2px 0 0 100px;	width: 250px;}.req-att{float:left;width:230px;}.req-imm{float:left;width:300px;padding-left:50px;}div > label{	bottom: 7px;    padding-left: 5px;    position: relative;    text-align: center;}.inn-lefts{ width: 340px; position: relative; left: 80px;  float:left;}.inn-rights{left: 100px;position: relative;top: 5px; width: 400px;float:left;}.box-1{float:left;width:340px;padding:5px;}.box-1{float:left;padding:5px;}.box-1 ul li{list-style:none;float:left;border:1px solid #e8e8e8; width:340px; line-height:25px;}.box-1 ul li p{font-size:15px;text-align:center;width:340px;font-size:10px;}.tit{width:340px;		color:#000;font-size:14px;font-family:'Trebuchet MS', Arial, Helvetica, sans-serif;background-color:#e8e8e8;line-height:35px;text-align:center;}.box-3-tit{width:390px;font-size:14px;font-family:'Trebuchet MS', Arial, Helvetica, sans-serif;background-color:#e8e8e8;text-align:center;line-height:35px;padding-left:10px;}.tit-span{font-weight:bold;font-size:14px; text-align:center;}.box-car{width:300px;text-align:center;}.box-3{float:left;width:400px;}.box-3 ul li{list-style:none;float:left;width:400px;border:1px solid #000;}.box-3 ul li p{font-size:15px;text-align:center;width:400px;font-size:10px;}.box-4{float:left;width:400px;border:1px solid #e8e8e8;}.box-4 ul li{list-style:none;float:left;}.box-4 ul li p{font-size:15px;text-align:center;width:400px;font-size:10px;}.box-cont label{padding:2px 2px;font-size:11px;position:relative;bottom:8px; }.box-conts-left{width:185px;float:left;position:relative;left:15px;}.box-conts-right{width:200px;float:left;position:relative;left:30px;}.box-tire {background-color:#f6f6f6;height:180px;width:400px; margin-top:20px;}.box-t{height:180px;width:200px;float:left;}.right_box-t{width:200px;float:left;height:180px;}.right_box-t ul li{float:left;width:98px;height:180px;border-left:1px solid #bebebe;}.right_box-t ul li div span{font-size: 13px;left: 13px;text-align: center;width: 100px;}.right_box-t .title{position: relative;top: 20px;}.wear{ float: left; width: 100px; text-align:center;}.box-conts span{font-weight:bold;position:relative;bottom:5px}.tire-input{height: 21px;width: 27px;position: relative;bottom: 8px;display: inline-block;}.tire-biginput{height: 38px;width: 27px;}.tire-top{float: left; width: 100px;}.tire-txt{float: left; width: 100px; }.tire-top label{font-size:15px;display:inline-block;width: 20px;}.tire-txt label{font-size:15px;display:inline-block;width: 20px;}.tire-top img{width:20px;height:20px}.tire-txt img{width:20px;height:20px}.air{width:100px;float:left; margin-top:10px;}.air-right{float:right;width:35px; }.air-txt {margin:1px;float:left;}.air-txt label{display: inline-block;width:20px; padding-right:3px;}.tire-top1{display:inline-block;padding:5px 0 10px 8px;float:left;}.chk{position: relative;top: 10px;width: 90px;}.box-cont{width:300px;float:left;position:relative;}.box-cont img{float:right;margin-top:5px;}.box-cont span {   bottom: 8px;   display: inline-block;   font-size: 15px;   padding: 2px;   position: relative;   top: 5px;   width: 200px;}.box-3 .box-cont{width:390px;}.battery-box{width:260px; font-size:14px; float:left;position:relative;top:10px;left:10px;}.bat-img{float:left;}.box-5{float:left;width:399px;border:1px solid #e8e8e8;position:relative;top:5px;}.box-5 ul li{list-style:none;float:left;width:400px;}.box-5  .tit{width:399px;}.brake-1{float:left;width:400px;padding:10px;}.brake-1 span{font-size:12px;display:inline-block;width:130px;}.brake-2{float:left;height:162px;width:399px;background-color:#f3f3f3;}.brake-3{float:right;width:220px;position:relative;margin-bottom:-90px;}.brake-im{position:relative;top:10px;left:10px;}.brake-im label{	width:20px;	display:inline-block;}.header{height:100px;width:960px;}.textbox{height:25px;border:1px #CCCCCC solid;width:250px;}.sign{margin-top:10px;width:300px;float:left;padding-left:40px;}.usercontent{font-size:14px;line-height:25px;padding-left:50px;}.tickimg{padding-left:5PX; padding-right:5PX;}.tickimg2{padding-right:5PX;}.tickimg3{padding-left:5PX;}.tech{float:left; font-size:15px; font-weight:bold;margin-top:10px;width:910px;padding-left:50px; background-color:#e8e8e8;height:35px;line-height:35px;text-align:left;}.inspire{	width:960px;	float:left; margin-top:10px;	}.legend{       width:960px;       height:35px;       float:left;       text-align:center;} .legend_left{       width:300px;       height:35px;       float:left;       margin-left:10px;       text-align:center;}.legend_center{       width:300px;       height:35px;       float:left;       margin-left:10px;       text-align:center;}.legend_right{       width:300px;       height:35px;       float:left;       margin-left:10px;       text-align:center;}  </style>";

		try{
			Statement st = conn.createStatement();
			ResultSet rs = st.executeQuery("select * from form_submission_component_data where form_submission_id = "+ submissionId +" and position_id >= 7 and position_id <= 10");
			while(rs.next()){
				b1Size++;
			}
			i = 0;
			rs = st.executeQuery("select * from form_submission_component_data where form_submission_id = "+ submissionId +" and position_id >= 7 and position_id <= 10");
			while (rs.next()){
				tempBlockOneData[i][0] = rs.getString("component_name");
				tempBlockOneData[i][1] = rs.getString("option_description");
				i++;
			}

			rs = st.executeQuery("select * from form_submission_component_data where form_submission_id = "+ submissionId +" and position_id >= 14 and position_id <= 20");
			while(rs.next()){
				b2Size++;
			}
			i = 0;
			rs = st.executeQuery("select * from form_submission_component_data where form_submission_id = "+ submissionId +" and position_id >= 14 and position_id <= 20");
			while (rs.next()){
				tempBlockTwoData[i][0] = rs.getString("component_name");
				tempBlockTwoData[i][1] = rs.getString("option_description");
				i++;
			}

			rs = st.executeQuery("select * from form_submission_component_data where form_submission_id = "+ submissionId +" and position_id = 12");
			while(rs.next()){
				bbSize++;
			}
			i = 0;
			rs = st.executeQuery("select * from form_submission_component_data where form_submission_id = "+ submissionId +" and position_id = 12");
			while (rs.next()){
				tempBlockBatteryData[i][0] = rs.getString("component_name");
				tempBlockBatteryData[i][1] = rs.getString("option_description");
				i++;
			}

			rs = st.executeQuery("select * from form_submission_component_data where form_submission_id = "+ submissionId +" and position_id >= 22 and position_id <= 26");
			while(rs.next()){
				b3Size++;
			}
			i = 0;
			rs = st.executeQuery("select * from form_submission_component_data where form_submission_id = "+ submissionId + " and position_id >= 22 and position_id <= 26");
			while (rs.next()){
				tempBlockThreeData[i][0] = rs.getString("component_name");
				tempBlockThreeData[i][1] = rs.getString("option_description");
				i++;
			}

			i = 0;
			rs = st.executeQuery("select * from form_submission_component_data where form_submission_id = "+ submissionId + " and position_id in (32, 34, 36, 38)");
			while (rs.next()){
				tempBlockFourZeroData[i][0] = rs.getString("component_name");
				tempBlockFourZeroData[i][1] = rs.getString("option_description");
				i++;
			}

			rs = st.executeQuery("select * from form_submission_component_data where form_submission_id = "+ submissionId + " and position_id >= 41 and position_id <= 44");
			while(rs.next()){
				b41Size++;
			}
			i = 0;
			rs = st.executeQuery("select * from form_submission_component_data where form_submission_id = "+ submissionId + " and position_id >= 41 and position_id <= 44");
			while (rs.next()){
				tempBlockFourOneData[i][0] = rs.getString("component_name");
				tempBlockFourOneData[i][1] = rs.getString("option_description");
				i++;
			}

			rs = st.executeQuery("select * from form_submission_component_data where form_submission_id = "+ submissionId + " and position_id >= 48 and position_id <= 51");
			while(rs.next()){
				b42Size++;
			}
			i = 0;
			rs = st.executeQuery("select * from form_submission_component_data where form_submission_id = "+ submissionId + " and position_id >= 48 and position_id <= 51");
			while (rs.next()){
				tempBlockFourTwoData[i][0] = rs.getString("component_name");
				tempBlockFourTwoData[i][1] = rs.getString("option_description");
				i++;
			}

			rs = st.executeQuery("select * from form_submission_component_data where form_submission_id = "+ submissionId + " and position_id >= 53 and position_id <= 56");
			while(rs.next()){
				b43Size++;
			}
			i = 0;
			rs = st.executeQuery("select * from form_submission_component_data where form_submission_id = "+ submissionId + " and position_id >= 53 and position_id <= 56");
			while (rs.next()){
				tempBlockFourThreeData[i][0] = rs.getString("option_description");
				tempBlockFourThreeData[i][1] = rs.getString("component_name");
				i++;
			}


			i = 0;
			rs = st.executeQuery("select * from form_submission_component_data where form_submission_id = "+ submissionId + " and position_id in (33, 35, 37, 39)");
			while (rs.next()){
				tempBlockInchesData[i][0] = rs.getString("component_name");
				tempBlockInchesData[i][1] = rs.getString("option_description");
				i++;
			}

			i = 0;
			rs = st.executeQuery("select * from form_submission_component_data where form_submission_id = "+ submissionId + " and position_id in (46, 47)");
			while (rs.next()){
				tempBlockPressureData[i][0] = rs.getString("component_name");
				tempBlockPressureData[i][1] = rs.getString("option_description");
				i++;
			}

			i = 0;
			rs = st.executeQuery("select * from form_submission_component_data where form_submission_id = "+ submissionId +" and position_id >= 62 and position_id <= 65");
			while (rs.next()){
				tempBlockFiveData[i][0] = rs.getString("component_name");
				tempBlockFiveData[i][1] = rs.getString("option_description");
				i++;
			}

			rs = st.executeQuery("select * from form_submission_profile_data where form_submission_id =  "+ submissionId);
			while (rs.next()){
				tempContent = tempContent +" "+ rs.getString("profile_field") + "=\"" + rs.getString("profile_value") + "\"; "; 
			}

			//Content for Services Block.

			rs = st.executeQuery("select profile_value from form_submission_profile_data where profile_field = 'service_type' and form_submission_id =  "+ submissionId);
			while (rs.next()){
				tempServiceContent = rs.getString("profile_value"); 
			}

			tempBlockServiceData = tempServiceContent.split(",");
			for(i =0;i<tempBlockServiceData.length;i++){
				System.out.println(tempBlockServiceData[i]);
				tempServiceFinalContent = tempBlockServiceData[i] + "<br>" + tempServiceFinalContent;  
			}

			/*
			 * 
			 * Image Related Code
			 * 
			 */

			rs = st.executeQuery("select * from form_submission_image where form_submission_id =  "+ submissionId);
			while (rs.next()){
				imageSize++;
			}


			rs = st.executeQuery("select * from form_submission_cam_image where form_submission_id =  "+ submissionId);
			while (rs.next()){
				imageCamSize++;
			}

			i=0;
			rs = st.executeQuery("select * from form_submission_image where form_submission_id =  "+ submissionId);
			while (rs.next()){
				tempImageContent[i] = rs.getString("image_file_name");
				i++;
			}

			i=0;
			rs = st.executeQuery("select * from form_submission_cam_image where form_submission_id =  "+ submissionId);
			while (rs.next()){
				tempImageCamContent[i] = rs.getString("image_file_name");
				tempImageCamDesc[i] = rs.getString("image_description");
				i++;
			}

			rs = st.executeQuery("select * from form_submission_sign_image where form_submission_id =  "+ submissionId);
			while (rs.next()){
				signImage = rs.getString("image_file_name");
			}

			imageContent = "<div class='photo-slider'><span> PHOTOS</span></div><div class='photo'>";

			for(int m=0; m<imageSize;m++){
				imageContent = imageContent + "<img src='"+Configuration.SERVICE_URL+"/images/" + tempImageContent[m] +"' width='150px' height='100px'>";
			}

			imageContent = imageContent + "</div>";

			for(int m=0; m<imageCamSize;m++){
				imageContent = imageContent + "<div class='photo'><img src='"+Configuration.SERVICE_URL+"/images/" + tempImageCamContent[m] +"' width='150px' height='100px'>"+ tempImageCamDesc[m] +"</div>";
			}


			rs = st.executeQuery("select * from form_submission_component_data where form_submission_id = "+ submissionId +" and position_id =45");
			while (rs.next()){
				tempBlockPressureImgData = rs.getString("option_description");
			}

			if(tempBlockPressureImgData.equals("1")){
				tempBlockPressureImgContent = "<img src='"+Configuration.SERVICE_URL+"/images/system_files/red_t.png' class='tickimg' height='20' width='20'>";
			}else{
				tempBlockPressureImgContent = "<img src='"+Configuration.SERVICE_URL+"/images/system_files/red_ut.png' class='tickimg' height='20' width='20'>";
			}


			rs = st.executeQuery("select user_id from form_submission_index where form_submission_id = "+ submissionId);

			while(rs.next()){
				tempUserId = rs.getInt("user_id");
			}

			rs = st.executeQuery("select image_file_name from user_settings_image where image_type='headerimageone' and user_id="+ tempUserId);
			while(rs.next()){
				headerImageOne = rs.getString("image_file_name");
			}
			if(headerImageOne.equals("")){
				headerImageOne = "top_left_img1.png";
			}

			rs = st.executeQuery("select image_file_name from user_settings_image where image_type='headerimagetwo' and user_id="+ tempUserId);
			while(rs.next()){
				headerImageTwo = rs.getString("image_file_name");
			}
			if(headerImageTwo.equals("")){
				headerImageTwo = "top_left_img2.png";
			}

			rs = st.executeQuery("select image_file_name from user_settings_image where image_type='headerimagethree' and user_id="+ tempUserId);
			while(rs.next()){
				headerImageThree = rs.getString("image_file_name");
			}
			if(headerImageThree.equals("")){
				headerImageThree = "top_left_img3.png";
			}



			firstName = parseValueFromHeader(tempContent, "first_name");
			lastName = parseValueFromHeader(tempContent, "last_name"); 
			mobileNo = parseValueFromHeader(tempContent, "mobile_no");
			vin = parseValueFromHeader(tempContent, "vin"); 
			make = parseValueFromHeader(tempContent, "make"); 
			model = parseValueFromHeader(tempContent, "model"); 
			year = parseValueFromHeader(tempContent, "year"); 
			mileage= parseValueFromHeader(tempContent, "mileage");
			comments= parseValueFromHeader(tempContent, "Comments");
			formDate = parseValueFromHeader(tempContent, "Date");
			alternativeMobileNo  = parseValueFromHeader(tempContent, "alternative_mobileno");
			if(parseValueFromHeader(tempContent, "service_type") == null){
				serviceType  = "";
			}else{
				serviceType  = parseValueFromHeader(tempContent, "service_type");
			}

			profileContent = "<div class='profile_title'><span class='profile_title'> PROFILE</span>"+
					"<div class='profile-form-left'><ul>"+
					"<li><label><b>First name </b></label> <label>"+ firstName +"</label></li>"+
					"<li><label><b>Last name </b></label> <label>"+ lastName +"</label></li>"+
					"<li><label><b>Moblie Phone </b></label> <label>"+ mobileNo +"</label></li>"+
					"<li><label><b>Alternate Phone </b></label> <label>"+ alternativeMobileNo +"</label></li>"+
					"<li><label><b>VIN </b></label> <label>"+ vin +"</label></li>"+
					"</ul></div><div class='profile-form-right'><ul>"+
					"<li><label><b>Make </b></label> <label>"+ make +"</label></li>"+
					"<li><label><b>Model </b></label> <label>"+ model +"</label></li>"+
					"<li><label><b>Year </b></label> <label>"+ year +"</label></li>"+
					"<li><label><b>Mileage </b></label> <label>"+ mileage +"</label></li>"+
					"</ul></div></div><br><br><br>";


			for(j=0;j<b1Size;j++){
				blockOneContent = blockOneContent+ "<li><div class='box-cont'>";
				for(k=0;k<2;k++){
					if(k == 0){
						blockOneContent = blockOneContent+ "<span>" + tempBlockOneData[j][k] + "</span>";
					}

					if(k==1){
						tick = tempBlockOneData[j][k];
						if (tick.equals("1")){
							blockOneContent = blockOneContent+
									"<div style='float:right;'><img src='"+Configuration.SERVICE_URL+"/images/system_files/red_ut.png' height='20' width='20'>" +
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/yellow_ut.png'  class='tickimg' height='20' width='20'>" +
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/green_t.png' height='20' width='20'></div>";
						}else if(tick.equals("2")){
							blockOneContent = blockOneContent+ 
									"<div style='float:right;'><img src='"+Configuration.SERVICE_URL+"/images/system_files/red_ut.png' height='20' width='20'>" +
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/yellow_t.png'  class='tickimg' height='20' width='20'>" +
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/green_ut.png' height='20' width='20'></div>";
						}else if(tick.equals("3")){
							blockOneContent = blockOneContent+
									"<div style='float:right;'><img src='"+Configuration.SERVICE_URL+"/images/system_files/red_t.png' height='20' width='20'>" +
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/yellow_ut.png'  class='tickimg' height='20' width='20'>" +
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/green_ut.png' height='20' width='20'></div>";
						}else{
							blockOneContent = blockOneContent+
									"<div style='float:right;'><img src='"+Configuration.SERVICE_URL+"/images/system_files/red_ut.png' height='20' width='20'>" +
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/yellow_ut.png' class='tickimg' height='20' width='20'>" +
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/green_ut.png' height='20' width='20'></div>";
						}
					}



				}
				blockOneContent =blockOneContent+ "</div></li>";
			}

			blockOneContent = "<div class='box-1'><ul><li><div class='tit'><span class='tit-span'>INTERIOR/EXTERIOR</span></div></li>"+

								blockOneContent + "</ul></div>";


			for(j=0;j<bbSize;j++){
				blockBatteryContent = blockBatteryContent+ "<div class='battery-box'><span>SEE ATTACHED PRINTOUT</span><br>";
				for(k=0;k<2;k++){
					if(k==1){
						tick = tempBlockBatteryData[j][k];
						if (tick.equals("1")){
							blockBatteryContent = blockBatteryContent+
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/green_t.png' height='20' width='20'>" +
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/yellow_ut.png'  class='tickimg' height='20' width='20'>" +
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/red_ut.png' height='20' width='20'>";
						}else if(tick.equals("2")){
							blockBatteryContent = blockBatteryContent+ 
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/green_ut.png' height='20' width='20'>" +
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/yellow_t.png'  class='tickimg' height='20' width='20'>" +
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/red_ut.png' height='20' width='20'>";
						}else if(tick.equals("3")){
							blockBatteryContent = blockBatteryContent+
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/green_ut.png' height='20' width='20'>" +
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/yellow_ut.png'  class='tickimg' height='20' width='20'>" +
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/red_t.png' height='20' width='20'>";
						}else{
							blockBatteryContent = blockBatteryContent+
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/green_ut.png' height='20' width='20'>" +
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/yellow_ut.png' class='tickimg' height='20' width='20'>" +
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/red_ut.png' height='20' width='20'>";
						}
					}
				}
				blockBatteryContent =blockBatteryContent+ "</div>";
			}

			blockBatteryContent = "<div class='box-1'><ul>"+
					"<li><div class='tit'><span class='tit-span'>BATTERY</span></div></li><li>"+
					blockBatteryContent + 
					"<div style='width:340px;'><img src='"+Configuration.SERVICE_URL+"/images/system_files/battery.png'></div></li></ul></div>";


			for(j=0;j<b2Size;j++){
				blockTwoContent = blockTwoContent+ "<li><div class='box-cont'>";
				for(k=0;k<2;k++){
					if(k == 0){
						blockTwoContent = blockTwoContent+ "<span>" + tempBlockTwoData[j][k] + "</span>";
					}
					if(k==1){
						tick = tempBlockTwoData[j][k];
						if (tick.equals("1")){
							blockTwoContent = blockTwoContent+
									"<div style='float:right;'><img src='"+Configuration.SERVICE_URL+"/images/system_files/red_ut.png' height='20' width='20'>" +
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/yellow_ut.png'  class='tickimg' height='20' width='20'>" +
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/green_t.png' height='20' width='20'></div>";
						}else if(tick.equals("2")){
							blockTwoContent = blockTwoContent+ 
									"<div style='float:right;'><img src='"+Configuration.SERVICE_URL+"/images/system_files/red_ut.png' height='20' width='20'>" +
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/yellow_t.png'  class='tickimg' height='20' width='20'>" +
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/green_ut.png' height='20' width='20'></div>";
						}else if(tick.equals("3")){
							blockTwoContent = blockTwoContent+
									"<div style='float:right;'><img src='"+Configuration.SERVICE_URL+"/images/system_files/red_t.png' height='20' width='20'>" +
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/yellow_ut.png'  class='tickimg' height='20' width='20'>" +
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/green_ut.png' height='20' width='20'></div>";
						}else{
							blockTwoContent = blockTwoContent+
									"<div style='float:right;'><img src='"+Configuration.SERVICE_URL+"/images/system_files/red_ut.png' height='20' width='20'>" +
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/yellow_ut.png' class='tickimg' height='20' width='20'>" +
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/green_ut.png' height='20' width='20'></div>";
						}
					}
				}
				blockTwoContent =blockTwoContent+ "</div></li>";
			}

			blockTwoContent = "<div class='box-1'><ul>"+
					"<li><div class='tit'><span class='tit-span'>UNDERHOOD</span></div></li>"+
					blockTwoContent + "</ul></div>";


			for(j=0;j<b3Size;j++){
				blockThreeContent = blockThreeContent+ "<li><div class='box-cont'>";
				for(k=0;k<2;k++){
					if(k == 0){
						blockThreeContent = blockThreeContent+ "<span>" + tempBlockThreeData[j][k] + "</span>";
					}
					if(k==1){
						tick = tempBlockThreeData[j][k];
						if (tick.equals("1")){
							blockThreeContent = blockThreeContent+
									"<div style='float:right;'><img src='"+Configuration.SERVICE_URL+"/images/system_files/red_ut.png' height='20' width='20'>" +
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/yellow_ut.png' class='tickimg'  height='20' width='20'>" +
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/green_t.png' height='20' width='20'></div>";
						}else if(tick.equals("2")){
							blockThreeContent = blockThreeContent+ 
									"<div style='float:right;'><img src='"+Configuration.SERVICE_URL+"/images/system_files/red_ut.png' height='20' width='20'>" +
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/yellow_t.png' class='tickimg'  height='20' width='20'>" +
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/green_ut.png' height='20' width='20'></div>";
						}else if(tick.equals("3")){
							blockThreeContent = blockThreeContent+
									"<div style='float:right;'><img src='"+Configuration.SERVICE_URL+"/images/system_files/red_t.png' height='20' width='20'>" +
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/yellow_ut.png' class='tickimg'  height='20' width='20'>" +
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/green_ut.png' height='20' width='20'></div>";
						}else{
							blockThreeContent = blockThreeContent+
									"<div style='float:right;'><img src='"+Configuration.SERVICE_URL+"/images/system_files/red_ut.png' height='20' width='20'>" +
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/yellow_ut.png' class='tickimg' height='20' width='20'>" +
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/green_ut.png' height='20' width='20'></div>";
						}
					}
				}
				blockThreeContent =blockThreeContent+ "</div></li>";
			}

			blockThreeContent = "<div class='box-1'><ul>"+
					"<li><div class='tit'><span class='tit-span'>UNDER VEHICLE</span></div></li>"+
					blockThreeContent + "</ul></div>";


			for(j=0;j<2;j++){
				if(j == 0){
					blockFourZeroLeftContent = blockFourZeroLeftContent+ "<div class='box-conts-left'>";
				}
				if(j == 1){
					blockFourZeroLeftContent = blockFourZeroLeftContent+ "<div class='box-conts-right'>";
				}

				for(k=0;k<2;k++){
					if(k == 0){
						blockFourZeroLeftContent = blockFourZeroLeftContent+ "<span>" + tempBlockFourZeroData[j][k] + "</span>";
					}
					if(k==1){
						tick = tempBlockFourZeroData[j][k];
						if (tick.equals("1")){
							blockFourZeroLeftContent = blockFourZeroLeftContent+
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/green_t.png' height='20' width='20'>" +
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/yellow_ut.png' class='tickimg'  height='20' width='20'>" +
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/red_ut.png' height='20' width='20'>";
						}else if(tick.equals("2")){
							blockFourZeroLeftContent = blockFourZeroLeftContent+ 
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/green_ut.png' height='20' width='20'>" +
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/yellow_t.png' class='tickimg'  height='20' width='20'>" +
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/red_ut.png' height='20' width='20'>";
						}else if(tick.equals("3")){
							blockFourZeroLeftContent = blockFourZeroLeftContent+
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/green_ut.png' height='20' width='20'>" +
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/yellow_ut.png' class='tickimg'  height='20' width='20'>" +
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/red_t.png' height='20' width='20'>";
						}else{
							blockFourZeroLeftContent = blockFourZeroLeftContent+
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/green_ut.png' height='20' width='20'>" +
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/yellow_ut.png' class='tickimg' height='20' width='20'>" +
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/red_ut.png' height='20' width='20'>";
						}
					}
				}
				blockFourZeroLeftContent =blockFourZeroLeftContent+ " " + tempBlockInchesData[j][1] +"/32\"</div>";
			}

			blockFourZeroLeftContent = "<li>"+ blockFourZeroLeftContent +"</li>";


			for(j=2;j<4;j++){
				if(j == 2){
					blockFourZeroRightContent = blockFourZeroRightContent+ "<div class='box-conts-left'>";
				}
				if(j == 3){
					blockFourZeroRightContent = blockFourZeroRightContent+ "<div class='box-conts-right'>";
				}

				for(k=0;k<2;k++){
					if(k == 0){
						blockFourZeroRightContent = blockFourZeroRightContent+ "<span>" + tempBlockFourZeroData[j][k] + "</span>";
					}
					if(k==1){
						tick = tempBlockFourZeroData[j][k];
						if (tick.equals("1")){
							blockFourZeroRightContent = blockFourZeroRightContent+
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/green_t.png' height='20' width='20'>" +
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/yellow_ut.png' class='tickimg'  height='20' width='20'>" +
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/red_ut.png' height='20' width='20'>";
						}else if(tick.equals("2")){
							blockFourZeroRightContent = blockFourZeroRightContent+ 
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/green_ut.png' height='20' width='20'>" +
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/yellow_t.png' class='tickimg'  height='20' width='20'>" +
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/red_ut.png' height='20' width='20'>";
						}else if(tick.equals("3")){
							blockFourZeroRightContent = blockFourZeroRightContent+
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/green_ut.png' height='20' width='20'>" +
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/yellow_ut.png'  class='tickimg' height='20' width='20'>" +
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/red_t.png' height='20' width='20'>";
						}else{
							blockFourZeroRightContent = blockFourZeroRightContent+
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/green_ut.png' height='20' width='20'>" +
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/yellow_ut.png' class='tickimg' height='20' width='20'>" +
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/red_ut.png' height='20' width='20'>";
						}
					}
				}
				blockFourZeroRightContent =blockFourZeroRightContent+ " " + tempBlockInchesData[j][1] +"/32\"</div>";
			}

			blockFourZeroRightContent = "<li>"+ blockFourZeroRightContent +"</li>";


			for(j=0;j<b41Size;j++){
				if(j==0){
					blockFourOneContent = blockFourOneContent+ "<div class='tire-top'><span class='wear' style='font-size:13px;'><b><i>Wear Pattern/ Damage</i></b></span>";
				}else{
					blockFourOneContent = blockFourOneContent+ "<div class='tire-txt'>";
				}

				for(k=0;k<2;k++){
					if(k == 0){
						blockFourOneContent = blockFourOneContent+ "<label style='padding-right:3px; vertical-align:middle; margin-top:5px;'>" + tempBlockFourOneData[j][k] + "</label>";
					}
					if(k==1){
						tick = tempBlockFourOneData[j][k];
						if (tick.equals("1")){
							blockFourOneContent = blockFourOneContent+
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/green_t.png' height='20' width='20'>" +
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/yellow_ut.png' class='tickimg'  height='20' width='20'>" +
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/red_ut.png' height='20' width='20'>";
						}else if(tick.equals("2")){
							blockFourOneContent = blockFourOneContent+ 
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/green_ut.png' height='20' width='20'>" +
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/yellow_t.png' class='tickimg'  height='20' width='20'>" +
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/red_ut.png' height='20' width='20'>";
						}else if(tick.equals("3")){
							blockFourOneContent = blockFourOneContent+
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/green_ut.png' height='20' width='20'>" +
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/yellow_ut.png' class='tickimg'  height='20' width='20'>" +
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/red_t.png' height='20' width='20'>";
						}else{
							blockFourOneContent = blockFourOneContent+
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/green_ut.png' height='20' width='20'>" +
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/yellow_ut.png' class='tickimg' height='20' width='20'>" +
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/red_ut.png' height='20' width='20'>";
						}
					}
				}
				blockFourOneContent =blockFourOneContent+ "</div>";
			}

			blockFourOneContent = "<div class='box-t'><img style='float:left' src='"+Configuration.SERVICE_URL+"/images/system_files/wheel.png'>"+
					blockFourOneContent + "</div>";


			for(j=0;j<b42Size;j++){
				blockFourTwoContent = blockFourTwoContent+ "<div class='air-txt'>";
				for(k=0;k<2;k++){
					if(k == 0)
						blockFourTwoContent = blockFourTwoContent+ "<label>" + tempBlockFourTwoData[j][k] + "</label>";
					if(k ==1 )
						blockFourTwoContent = blockFourTwoContent+ "<input type='text' class='tire-input' value='"+ tempBlockFourTwoData[j][k] +"' readonly />";
					System.out.println(tempBlockFourZeroData[j][k]);
				}
				blockFourTwoContent =blockFourTwoContent+ "</div>";
			}

			blockFourTwoContent = "<li><div><span><b><i>Air Pressure</i></b></span></div>" +
					"<div class='tire-top1'>"+ tempBlockPressureImgContent +
					"</div><div><img src='"+Configuration.SERVICE_URL+"/images/system_files/symbol.png'  width='40' height='45'></div>" +
					"<div style='width:100px; margin-left:20px;'><span style='font-size:6px; line-height:25px; padding-left:5px;'> BEFORE</span><span style='font-size:5px; padding-left:6px; line-height:20px;' >SHOULD BE</span></div>"+
					"<div class='air'>" +
					"<div class='air-right'><input type='text' class='tire-biginput' value='"+ tempBlockPressureData[0][1] +"' readonly />"+
					"<p style='height:1px;'>&nbsp;</p><input type='text' class='tire-biginput' value='"+ tempBlockPressureData[1][1] +"' readonly />" +
					"</div>"+
					blockFourTwoContent + 
					"</div></li>";

			for(j=0;j<b43Size;j++){
				blockFourThreeContent = blockFourThreeContent+ "<div>";
				for(k=0;k<2;k++){
					if(k == 0){
						if(tempBlockFourThreeData[j][k].equals("1")){
							blockFourThreeContent = blockFourThreeContent+ "<input type='checkbox' checked disabled />";
						}else{
							blockFourThreeContent = blockFourThreeContent+ "<input type='checkbox' disabled />";
						}
					}
					if(k ==1){
						blockFourThreeContent = blockFourThreeContent+ tempBlockFourThreeData[j][k];	
					}
				}
				blockFourThreeContent = blockFourThreeContent+ "</div>";
			}

			blockFourThreeContent = "<li><div><span><b><i>Based on Mileage and Wear</i></b></span></div><div class='chk'>" +
					blockFourThreeContent + "</div></li>";

			interBlockFourContent = "<div class='right_box-t'><ul>"  + 
					blockFourTwoContent + blockFourThreeContent +
					"</ul></div>";

			for(j=0;j<4;j++){
				blockFiveContent = blockFiveContent+ "<div class='brake-im'>";
				for(k=0;k<2;k++){
					if(k == 0){
						blockFiveContent = blockFiveContent+ "<label style='padding-right:3px; vertical-align:middle; margin-top:5px;'>" + tempBlockFiveData[j][k] + "</label>";
					}

					if(k==1){
						tick = tempBlockFiveData[j][k];
						if (tick.equals("1")){
							blockFiveContent = blockFiveContent+
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/green_t.png' height='20' width='20'>" +
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/yellow_ut.png' class='tickimg'  height='20' width='20'>" +
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/red_ut.png' height='20' width='20'>";
						}else if(tick.equals("2")){
							blockFiveContent = blockFiveContent+ 
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/green_ut.png' height='20' width='20'>" +
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/yellow_t.png' class='tickimg'  height='20' width='20'>" +
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/red_ut.png' height='20' width='20'>";
						}else if(tick.equals("3")){
							blockFiveContent = blockFiveContent+
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/green_ut.png' height='20' width='20'>" +
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/yellow_ut.png' class='tickimg'  height='20' width='20'>" +
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/red_t.png' height='20' width='20'>";
						}else{
							blockFiveContent = blockFiveContent+
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/green_ut.png' height='20' width='20'>" +
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/yellow_ut.png' class='tickimg'  height='20' width='20'>" +
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/red_ut.png' height='20' width='20'>";
						}
					}



				}
				blockFiveContent =blockFiveContent+ "</div>";
			}

			blockFiveContent = "<div class='box-5'><ul><li><div class='tit'><span class='tit-span'>BRAKES</span></div></li>"+
					"<li><div class='brake-1'>" +
					"<div style='float:left;width:180px;'>" +
					"<img src='"+Configuration.SERVICE_URL+"/images/system_files/green_ut.png' height='20' width='20'><span style='padding-left:4px;'>Over 5 mm (Disk) or 2 mm (Drum)</span><br/>"+
					"<img src='"+Configuration.SERVICE_URL+"/images/system_files/yellow_ut.png' height='20'width='20'><span style='padding-left:4px;'>3-5 mm (Disk) or 1.01-2 mm (Drum)</span><br/>"+
					"<img src='"+Configuration.SERVICE_URL+"/images/system_files/red_ut.png' height='20' width='20'><span style='padding-left:4px;'>Less than 3 mm (Disk) or 1 mm (Drum)</span><br/>"+
					"</div><div class='brake-3'><img style='float:left' src='"+Configuration.SERVICE_URL+"/images/system_files/inspect_Brakes.png'></div>" +	
					"</div><div class='brake-2'>" +
					blockFiveContent + 
					"</div><div style='background-color:#D4DAEE;float: left;text-align: center;width: 399px;'>" +
					"<span style='font-weight:bold;font-size:15px;position:relative;bottom:20px;'>We feature</span><img src='"+Configuration.SERVICE_URL+"/images/system_files/brands.png'><span style='font-weight:bold;font-size:15px;position:relative;bottom:20px;'>Brakes</span></div>"+
					"</li></ul></div>";



			footContent1 = "<div class='comments'>COMMENTS </div><div style='float:left;'><p class='usercontent'>"+ comments + "</p></div><div class='comments'>SERVICES </div><div style='float:left;'><p class='usercontent'>"+ tempServiceFinalContent + "</p></div>";
			footContent2 = "<div class='tech'><span style='float:left;'>TECHNICIAN  &nbsp;</span><span style='float:right; padding-right:50px;'>DATE   &nbsp;</span></div>";
			//footContent = footContent + "<div class='sign'><img src='http://localhost:8080/sampwebser/images/" + signImage +"' width='150px' height='100px'></div></span>";
			footContent2 = footContent2 + "<div class='sign'><img src='"+Configuration.SERVICE_URL+"/images/" + signImage +"' width='150px' height='100px'></div>";

			footContent2 = footContent2 + "<div style='float:right; width:450px; text-align:right;'><div class='dat'>"+formDate+"</div></div>";

			ultimateContent = "<html><head>"+css+ "</head><body><div id='wrapper'><div class='header'>" +
					"<img src='"+Configuration.SERVICE_URL+"/images/settings_images/"+headerImageOne+"' width='208' height='100'>" +
					"<img src='"+Configuration.SERVICE_URL+"/images/settings_images/"+headerImageTwo+"' width='590' height='100'>" +
					"<img src='"+Configuration.SERVICE_URL+"/images/settings_images/"+headerImageThree+"' width='161' height='100'></div>" +
					"<div class='inner-head'><h1>VEHICLE INSPECTION REPORT</h1></div>"+
					profileContent +
					"<div class='inn'>" +
					"<div class='legend_left'><img src='"+Configuration.SERVICE_URL+"/images/system_files/green_ut.png' height='20' width='20'>" +
					"<span style='font-size:15px; padding-left:5px; padding-right:5px; font-weight:bold;'>CHECKED AND OK</span></div><div class='legend_center'>" +
					"<img src='"+Configuration.SERVICE_URL+"/images/system_files/yellow_ut.png' height='20' width='20'><span style='font-size:15px; padding-left:5px; padding-right:5px; font-weight:bold;'>FUTURE ATTENTION</span></div>" +
					"<div class='legend_right'><img src='"+Configuration.SERVICE_URL+"/images/system_files/red_ut.png' height='20' width='20'><span style='font-size:15px; padding-left:5px; padding-right:5px; font-weight:bold;'>IMMEDIATE ATTENTION</span></div>" +
					"<div class='inn-lefts'>" +
					blockOneContent + blockBatteryContent + blockTwoContent + blockThreeContent	+  
					"</div><div class='inn-rights'>" +
					"<div class='box-4'><ul>" +
					"<li><div class='box-3-tit'><span class='tit-span'>TIRES</span></div></li>"+
					"<li><p><span style='font-size:13px; font-weight:bold'>Tread Depth</span><br><br></p></li>" +
					"<li><div style='width:400px;float:left;position:relative;'>" +
					"<img src='"+Configuration.SERVICE_URL+"/images/system_files/green_ut.png'  class='tickimg' height='20' width='20'><label style='font-style:italic; font-size:11px; font-weight:bold;'>7/32' greater</label>" +
					"<img src='"+Configuration.SERVICE_URL+"/images/system_files/yellow_ut.png' class='tickimg' height='20' width='20'><label style='font-style:italic; font-size:11px; font-weight:bold;'>3/32' to 6/32'</label>" +
					"<img src='"+Configuration.SERVICE_URL+"/images/system_files/red_ut.png'  class='tickimg' height='20' width='20'><label style='font-style:italic; font-size:11px; font-weight:bold;'>2/32' or less</label>" +
					"</div></li>" +
					blockFourZeroLeftContent + blockFourZeroRightContent +
					"<li><ul><li><div class='box-tire'>" + 
					blockFourOneContent + interBlockFourContent +
					"</div></li></ul></li>" + 
					"</ul></div>"+
					blockFiveContent +
					"</div></div>" +footContent1 + imageContent + footContent2 +"</div></div></body></html>"; 

		}
		catch (Exception e) {
			e.printStackTrace();
		}



		result = ultimateContent;
		return Response.status(200).entity(result).build();
	}



	@Path("threeHtml/{u}")
	@GET
	@Produces("text/html")
	public Response threeHtml(@PathParam("u") int u){
		Connection conn = null;
		conn = connectToDatabaseOrDie();
		String result = null;
		int submissionId = u;
		int i =0, j=0, k=0;
		int imageSize = 0, imageCamSize = 0;
		String ultimateContent = "", tempContent = "", css = "", profileContent= "";
		String firstName = "", lastName = "", mobileNo ="", alternativeMobileNo= "", serviceType= "";
		String vin = "", make = "", model ="", year ="", mileage= "", comments ="", formDate ="";
		String[] tempImageContent = new String[125];
		String[] tempImageCamContent = new String[125];
		String[] tempImageCamDesc = new String[125];
		String imageContent = "", signImage = "", footContent1 = "", footContent2 = "", tick ="";
		String [][] tempBlockOneData = new String[25][2];
		String [][] tempBlockTwoData = new String[25][2];
		String [][] tempBlockThreeData = new String[25][2];
		String [][] tempBlockFourZeroData = new String[25][2];
		String [][] tempBlockFourOneData = new String[25][2];
		String [][] tempBlockFourTwoData = new String[25][2];
		String [][] tempBlockFourThreeData = new String[25][2];
		String [][] tempBlockBatteryData = new String[25][2];
		String [][] tempBlockInchesData = new String[25][2];
		String [][] tempBlockPressureData = new String[25][2];
		String[] tempBlockServiceData = new String[25];
		String tempBlockPressureImgData = "", tempBlockPressureImgContent ="";
		String headerImageOne = "", headerImageTwo ="", headerImageThree ="";
		String blockOneContent = "", blockTwoContent = "", blockThreeContent = "", blockFourZeroLeftContent = "", blockFourZeroRightContent = "", blockFourOneContent = "", blockFourTwoContent = "", blockFourThreeContent = "", blockBatteryContent= "", interBlockFourContent = "", tempServiceContent = "", tempServiceFinalContent = "";
		int b1Size = 0, b2Size= 0, b3Size=0, b41Size = 0, b42Size = 0, b43Size = 0, bbSize = 0, tempUserId = 0;

		css = "<style type='text/css'>body{font-family: 'Helvetica'; }body,*{margin:0; padding:0;}h1{	width:960px;height:44px;background-image:url("+Configuration.SERVICE_URL+"/images/system_files/tile_img.png);line-height:44px;color:#FFF;	font-size:18px;text-align:center;font-family:'Trebuchet MS', Arial, Helvetica, sans-serif;}#wrapper{width:960px; background-color:#FFF; 	margin:auto;}.inner-head {text-align:center;	width:960px;	height:40px;	}.inner-logo{	border:1px solid #000;	width:385px;	height:100px;	text-align:center;	float:left;	position:relative;	left:290px;}.profile{	text-align:left;padding-left:20px;width:940px;height:35px;color:#000;font-size:15px;font-family:'Trebuchet MS', Arial, Helvetica, sans-serif;background-color:#e8e8e8;margin-top:10px;line-height:35px;}.profile_title{		width:960px;text-align:left;height:35px;color:#000;font-size:15px;font-family:'Trebuchet MS', Arial, Helvetica, sans-serif;background-color:#e8e8e8;margin-top:10px;line-height:35px;}.profile_title span{ font-weight:bold;	padding-left:50px;	font-size:15px;}.profile-form-left ul{	float:left; text-align:left;	padding-left:50px;	width:440px;	padding-top:10px;}.profile-form-left ul li{	list-style:none;	height:50px;}.profile-form-left ul li label{	width:150px;	font-size:14px;	display:inline-block;}.profile-form-right ul{	float:right;	width:440px; text-align:left;	padding-top:10px;}.profile-form-right ul li{	list-style:none;	height:50px; font-size:14px;}.profile-form-right ul li label{	width:150px;	font-size:14px;	display:inline-block;}.photo-slider{ text-align:left; float:left;	width:960px; background-color:#e8e8e8; height:35px; }.photo-slider span{	position:relative; background-color:#e8e8e8; font-weight:bold; 	padding-left:50px;	line-height:35px; font-size:14px;}.photo{	float:left;	padding-top:20px;	padding-left:50px;	width:910px;}.photo img{	float:left;  padding-right:10px; padding-bottom:10px;}.mileage{	float:left;	width:960px;	height:20px;	position:relative;	top:5px;	text-align:center;}.inn{width:960px;	margin-top:210px;}.inn-left {	width:450px;	float:left;	padding:20px 0 0 50px;}.inn-left ul{	width:400px;	float:left;	padding:20px 0 0 50px;}.inn-left ul li{	list-style:none;	width:200px;	height:70px;}.inn-text{	color:mediumblue;	font-weight:bold;	font-size:20px;	position:relative;	bottom:5px;}.inn-black{	font-weight:bold;	font-size:15px;	color:#000;	position:relative;	bottom:5px;}.inn-right{	width:400px;	float:left;	padding:20px 0 0 50px;}.inn-right ul{	width:450px;	float:left;	padding:20px 0 0 50px;}.inn-right ul li{	list-style:none;	width:200px;	height:70px;}.comments{	float:left; font-size:15px; font-weight:bold; margin-top:10px; width:910px; padding-left:50px; background-color:#e8e8e8; height:35px; line-height:35px; text-align:left; margin-bottom:10px;}.inspire{	width:960px;	float:left;	}.dat{	width:400px; line-height:35px;	height:50px;float:left;}p > input {    float: right; text-align:right;    height: 16px;    left: 0;    margin: 0;        padding: 0;    width: 16px;}p > label {    color: #000000;    float: left;    line-height: 16px;    padding: 0 0 0 5px;	font-size:15px;}.name{	width:320px;	height:50px;	float:left;}.mil{	width:320px;	height:50px;	float:left;}.make{	width:320px;	height:50px;	float:left;}.chk-mark{float:left;height:50px;}.chk-ok{	float:left;	padding: 2px 0 0 100px;	width: 250px;}.req-att{float:left;width:230px;}.req-imm{float:left;width:300px;padding-left:50px;}div > label{	bottom: 7px;    padding-left: 5px;    position: relative;    text-align: center;}.inn-lefts{ width: 300px; position: relative; left: 60px;  float:left;}.inn-rights{left: 140px;position: relative;top: 5px; width: 400px;float:left;}.box-1{float:left;padding:5px;}.box-1 ul li{list-style:none;float:left;border:1px solid #e8e8e8; line-height:25px;}.box-1 ul li p{font-size:15px;text-align:center;width:340px;font-size:10px;}.tit{width:340px;		color:#000;font-size:14px;font-family:'Trebuchet MS', Arial, Helvetica, sans-serif;background-color:#e8e8e8;line-height:35px;text-align:center;}.box-3-tit{width:390px;font-size:14px;font-family:'Trebuchet MS', Arial, Helvetica, sans-serif;background-color:#e8e8e8;text-align:center;line-height:35px;padding-left:10px;}.tit-span{font-weight:bold; font-size:14px; text-align:center;}.box-car{width:340px;text-align:center;}.box-cont{width:340px;float:left;position:relative;left:5px;} .box-cont img{margin-top:5px;} .box-cont span{	padding:2px 4px;	font-size:15px;	position:relative;	}.box-3{float:left;width:400px;}.box-3 ul li{list-style:none;float:left;width:400px;border:1px solid #e8e8e8; line-height:30px;}.box-3 ul li p{font-size:15px;text-align:center;width:400px; line-height:30px;}.box-4{float:left;width:400px;border:1px solid #e8e8e8; margin-top:10px;}.box-4 ul li{list-style:none;float:left;}.box-4 ul li p{font-size:15px;text-align:center;width:400px;font-size:10px;}.box-cont label{	padding:2px 2px;	font-size:11px;	position:relative;	}.box-conts-left{width:185px;float:left;position:relative;left:15px;}.box-conts-right{width:200px;float:left;position:relative;left:30px;}.box-tire {background-color:#f6f6f6;height:180px;width:400px; margin-top:20px;}.box-t{height:180px;width:200px;float:left;}.right_box-t{width:200px;float:left;height:180px;}.right_box-t ul li{float:left;width:98px;height:180px;border-left:1px solid #bebebe;}.right_box-t ul li div span{font-size: 13px;left: 13px;text-align: center;width: 100px;}.right_box-t .title{position: relative;op: 20px;}.wear{ float: left; width: 100px; text-align:center;}.box-conts span{font-weight:bold;position:relative;bottom:5px}.tire-input{height: 21px;width: 27px;position: relative;bottom: 8px;display: inline-block;}.tire-biginput{height: 38px;width: 27px;}.tire-top{float: left; width: 100px;}.tire-txt{float: left; width: 100px; }.tire-top label{font-size:15px;display:inline-block;width: 20px;}.tire-txt label{font-size:15px;display:inline-block;width: 20px;}.tire-top img{width:20px;height:20px}.tire-txt img{width:20px;height:20px}.air{width:100px;float:left;}.air-right{float:right;width:35px;}.air-txt {margin:1px;float:left;}.air-txt label{display: inline-block; padding-right:5px;width:20px;}.tire-top1{display:inline-block;float:left;}.chk{position: relative; font-size:14px; float:left;  top: 10px; width: 90px;}.bat-box{ float:left;width:244px; padding-left:5PX;}.header{height:100px;width:960px;}.userlist{font-size:13px;font-weight:bold;}.textbox{height:25px;border:1px #CCCCCC solid;width:250px;}.sign{margin-top:10px;width:300px;float:left;padding-left:40px;}.usercontent{font-size:14px;line-height:25px;padding-left:50px;}.tickimg{padding-left:5PX; padding-right:5PX;}.tech{float:left;font-size:15px; font-weight:bold; margin-top:10px;width:910px;padding-left:50px; background-color:#e8e8e8; height:35px; line-height:35px;text-align:left;	}.legend{       width:960px;       height:35px;       float:left;       text-align:center;} .legend_left{       width:300px;       height:35px;       float:left;       margin-left:10px;       text-align:center;}.legend_center{       width:300px;       height:35px;       float:left;       margin-left:10px;       text-align:center;}.legend_right{       width:300px;       height:35px;       float:left;       margin-left:10px;       text-align:center;}</style>";

		try{
			Statement st = conn.createStatement();
			ResultSet rs = st.executeQuery("select * from form_submission_component_data where form_submission_id = "+ submissionId +" and position_id >= 7 and position_id <= 13");
			while(rs.next()){
				b1Size++;
			}
			i = 0;
			rs = st.executeQuery("select * from form_submission_component_data where form_submission_id = "+ submissionId +" and position_id >= 7 and position_id <= 13");
			while (rs.next()){
				tempBlockOneData[i][0] = rs.getString("option_description");
				tempBlockOneData[i][1] = rs.getString("component_name");
				i++;
			}

			rs = st.executeQuery("select * from form_submission_component_data where form_submission_id = "+ submissionId +" and position_id >= 16 and position_id <= 25");
			while(rs.next()){
				b2Size++;
			}
			i = 0;
			rs = st.executeQuery("select * from form_submission_component_data where form_submission_id = "+ submissionId +" and position_id >= 16 and position_id <= 25");
			while (rs.next()){
				tempBlockTwoData[i][0] = rs.getString("option_description");
				tempBlockTwoData[i][1] = rs.getString("component_name");
				i++;
			}

			rs = st.executeQuery("select * from form_submission_component_data where form_submission_id = "+ submissionId +" and position_id >= 27 and position_id <= 29");
			while(rs.next()){
				bbSize++;
			}
			i = 0;
			rs = st.executeQuery("select * from form_submission_component_data where form_submission_id = "+ submissionId +" and position_id >= 27 and position_id <= 29");
			while (rs.next()){
				tempBlockBatteryData[i][0] = rs.getString("option_description");
				tempBlockBatteryData[i][1] = rs.getString("component_name");
				i++;
			}

			rs = st.executeQuery("select * from form_submission_component_data where form_submission_id = "+ submissionId +" and position_id >= 31 and position_id <= 37");
			while(rs.next()){
				b3Size++;
			}
			i = 0;
			rs = st.executeQuery("select * from form_submission_component_data where form_submission_id = "+ submissionId + " and position_id >= 31 and position_id <= 37");
			while (rs.next()){
				tempBlockThreeData[i][0] = rs.getString("option_description");
				tempBlockThreeData[i][1] = rs.getString("component_name");
				i++;
			}

			i = 0;
			rs = st.executeQuery("select * from form_submission_component_data where form_submission_id = "+ submissionId + " and position_id >= 44 and position_id <= 51");
			while (rs.next()){
				tempBlockFourZeroData[i][0] = rs.getString("component_name");
				tempBlockFourZeroData[i][1] = rs.getString("option_description");
				i++;
			}

			rs = st.executeQuery("select * from form_submission_component_data where form_submission_id = "+ submissionId + " and position_id >= 53 and position_id <= 56");
			while(rs.next()){
				b41Size++;
			}
			i = 0;
			rs = st.executeQuery("select * from form_submission_component_data where form_submission_id = "+ submissionId + " and position_id >= 53 and position_id <= 56");
			while (rs.next()){
				tempBlockFourOneData[i][0] = rs.getString("component_name");
				tempBlockFourOneData[i][1] = rs.getString("option_description");
				i++;
			}

			rs = st.executeQuery("select * from form_submission_component_data where form_submission_id = "+ submissionId + " and position_id >= 61 and position_id <= 64");
			while(rs.next()){
				b42Size++;
			}
			i = 0;
			rs = st.executeQuery("select * from form_submission_component_data where form_submission_id = "+ submissionId + " and position_id >= 61 and position_id <= 64");
			while (rs.next()){
				tempBlockFourTwoData[i][0] = rs.getString("component_name");
				tempBlockFourTwoData[i][1] = rs.getString("option_description");
				i++;
			}

			rs = st.executeQuery("select * from form_submission_component_data where form_submission_id = "+ submissionId + " and position_id >= 66 and position_id <= 69");
			while(rs.next()){
				b43Size++;
			}
			i = 0;
			rs = st.executeQuery("select * from form_submission_component_data where form_submission_id = "+ submissionId + " and position_id >= 66 and position_id <= 69");
			while (rs.next()){
				tempBlockFourThreeData[i][0] = rs.getString("component_name");
				tempBlockFourThreeData[i][1] = rs.getString("option_description");
				i++;
			}


			i = 0;
			rs = st.executeQuery("select * from form_submission_component_data where form_submission_id = "+ submissionId + " and position_id >= 48 and position_id <= 51");
			while (rs.next()){
				tempBlockInchesData[i][0] = rs.getString("component_name");
				tempBlockInchesData[i][1] = rs.getString("option_description");
				i++;
			}

			i = 0;
			rs = st.executeQuery("select * from form_submission_component_data where form_submission_id = "+ submissionId + " and position_id in (59, 60)");
			while (rs.next()){
				tempBlockPressureData[i][0] = rs.getString("component_name");
				tempBlockPressureData[i][1] = rs.getString("option_description");
				i++;
			}


			rs = st.executeQuery("select * from form_submission_profile_data where form_submission_id =  "+ submissionId);
			while (rs.next()){
				tempContent = tempContent +" "+ rs.getString("profile_field") + "=\"" + rs.getString("profile_value") + "\"; "; 
			}

			//Content for Services Block.

			rs = st.executeQuery("select profile_value from form_submission_profile_data where profile_field = 'service_type' and form_submission_id =  "+ submissionId);
			while (rs.next()){
				tempServiceContent = rs.getString("profile_value"); 
			}

			tempBlockServiceData = tempServiceContent.split(",");
			for(i =0;i<tempBlockServiceData.length;i++){
				System.out.println(tempBlockServiceData[i]);
				tempServiceFinalContent = tempBlockServiceData[i] + "<br>" + tempServiceFinalContent;  
			}

			/*
			 * 
			 * Image Related Code
			 * 
			 */

			rs = st.executeQuery("select * from form_submission_image where form_submission_id =  "+ submissionId);
			while (rs.next()){
				imageSize++;
			}


			rs = st.executeQuery("select * from form_submission_cam_image where form_submission_id =  "+ submissionId);
			while (rs.next()){
				imageCamSize++;
			}

			i=0;
			rs = st.executeQuery("select * from form_submission_image where form_submission_id =  "+ submissionId);
			while (rs.next()){
				tempImageContent[i] = rs.getString("image_file_name");
				i++;
			}

			i=0;
			rs = st.executeQuery("select * from form_submission_cam_image where form_submission_id =  "+ submissionId);
			while (rs.next()){
				tempImageCamContent[i] = rs.getString("image_file_name");
				tempImageCamDesc[i] = rs.getString("image_description");
				i++;
			}

			rs = st.executeQuery("select * from form_submission_sign_image where form_submission_id =  "+ submissionId);
			while (rs.next()){
				signImage = rs.getString("image_file_name");
			}

			imageContent = "<div class='photo-slider'><span> PHOTOS</span></div><div class='photo'>";

			for(int m=0; m<imageSize;m++){
				imageContent = imageContent + "<img src='"+Configuration.SERVICE_URL+"/images/" + tempImageContent[m] +"' width='150px' height='100px'>";
			}

			imageContent = imageContent + "</div>";

			for(int m=0; m<imageCamSize;m++){
				imageContent = imageContent + "<div class='photo'><img src='"+Configuration.SERVICE_URL+"/images/" + tempImageCamContent[m] +"' width='150px' height='100px'>"+ tempImageCamDesc[m] +"</div>";
			}

			/*
			 * 
			 * Header Area related Code
			 * 
			 */


			rs = st.executeQuery("select user_id from form_submission_index where form_submission_id = "+ submissionId);

			while(rs.next()){
				tempUserId = rs.getInt("user_id");
			}

			rs = st.executeQuery("select image_file_name from user_settings_image where image_type='headerimageone' and user_id="+ tempUserId);
			while(rs.next()){
				headerImageOne = rs.getString("image_file_name");
			}
			if(headerImageOne.equals("")){
				headerImageOne = "top_left_img1.png";
			}

			rs = st.executeQuery("select image_file_name from user_settings_image where image_type='headerimagetwo' and user_id="+ tempUserId);
			while(rs.next()){
				headerImageTwo = rs.getString("image_file_name");
			}
			if(headerImageTwo.equals("")){
				headerImageTwo = "top_left_img2.png";
			}

			rs = st.executeQuery("select image_file_name from user_settings_image where image_type='headerimagethree' and user_id="+ tempUserId);
			while(rs.next()){
				headerImageThree = rs.getString("image_file_name");
			}
			if(headerImageThree.equals("")){
				headerImageThree = "top_left_img3.png";
			}



			rs = st.executeQuery("select * from form_submission_component_data where form_submission_id = "+ submissionId +" and position_id =57");
			while (rs.next()){
				tempBlockPressureImgData = rs.getString("option_description");
			}

			if(tempBlockPressureImgData.equals("1")){
				tempBlockPressureImgContent = "<img src='"+Configuration.SERVICE_URL+"/images/system_files/red_t.png' class='tickimg' height='20' width='20'>";
			}else{
				tempBlockPressureImgContent = "<img src='"+Configuration.SERVICE_URL+"/images/system_files/red_ut.png' class='tickimg' height='20' width='20'>";
			}

			firstName = parseValueFromHeader(tempContent, "first_name");
			lastName = parseValueFromHeader(tempContent, "last_name"); 
			mobileNo = parseValueFromHeader(tempContent, "mobile_no");
			vin = parseValueFromHeader(tempContent, "vin"); 
			make = parseValueFromHeader(tempContent, "make"); 
			model = parseValueFromHeader(tempContent, "model"); 
			year = parseValueFromHeader(tempContent, "year"); 
			mileage= parseValueFromHeader(tempContent, "mileage");
			comments= parseValueFromHeader(tempContent, "Comments");
			formDate = parseValueFromHeader(tempContent, "Date");
			alternativeMobileNo  = parseValueFromHeader(tempContent, "alternative_mobileno");
			if(parseValueFromHeader(tempContent, "service_type") == null){
				serviceType  = "";
			}else{
				serviceType  = parseValueFromHeader(tempContent, "service_type");
			}

			profileContent = "<div class='profile_title'><span class='profile_title'> PROFILE</span>"+
					"<div class='profile-form-left'><ul>"+
					"<li><label><b>First name </b></label> <label>"+ firstName +"</label></li>"+
					"<li><label><b>Last name </b></label> <label>"+ lastName +"</label></li>"+
					"<li><label><b>Moblie Phone </b></label> <label>"+ mobileNo +"</label></li>"+
					"<li><label><b>Alternate Phone </b></label> <label>"+ alternativeMobileNo +"</label></li>"+
					"<li><label><b>VIN </b></label> <label>"+ vin +"</label></li>"+
					"</ul></div><div class='profile-form-right'><ul>"+
					"<li><label><b>Make </b></label> <label>"+ make +"</label></li>"+
					"<li><label><b>Model </b></label> <label>"+ model +"</label></li>"+
					"<li><label><b>Year </b></label> <label>"+ year +"</label></li>"+
					"<li><label><b>Mileage </b></label> <label>"+ mileage +"</label></li>"+
					"</ul></div></div><br><br><br>";


			for(j=0;j<b1Size;j++){
				blockOneContent = blockOneContent+ "<li><div class='box-cont'>";
				for(k=0;k<2;k++){
					if(k==0){
						tick = tempBlockOneData[j][k];
						if (tick.equals("1")){
							blockOneContent = blockOneContent+
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/green_t.png' height='20' width='20'>" +
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/yellow_ut.png' class='tickimg' height='20' width='20'>" +
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/red_ut.png' height='20' width='20'>";
						}else if(tick.equals("2")){
							blockOneContent = blockOneContent+ 
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/green_ut.png' height='20' width='20'>" +
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/yellow_t.png' class='tickimg' height='20' width='20'>" +
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/red_ut.png' height='20' width='20'>";
						}else if(tick.equals("3")){
							blockOneContent = blockOneContent+
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/green_ut.png' height='20' width='20'>" +
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/yellow_ut.png' class='tickimg' height='20' width='20'>" +
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/red_t.png' height='20' width='20'>";
						}else{
							blockOneContent = blockOneContent+
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/green_ut.png' height='20' width='20'>" +
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/yellow_ut.png' class='tickimg' height='20' width='20'>" +
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/red_ut.png' height='20' width='20'>";
						}
					}

					if(k == 1){
						blockOneContent = blockOneContent+ "<span>" + tempBlockOneData[j][k] + "</span>";
					}

				}
				blockOneContent =blockOneContent+ "</div></li>";
			}

			blockOneContent = "<div class='box-1'><ul><li><div class='tit'><span class='tit-span'>INTERIOR/EXTERIOR</span></div></li>"+

								blockOneContent + "</ul></div>";

			for(j=0;j<b2Size;j++){
				blockTwoContent = blockTwoContent+ "<li><div class='box-cont'>";
				for(k=0;k<2;k++){
					if(k == 1){
						blockTwoContent = blockTwoContent+ "<span>" + tempBlockTwoData[j][k] + "</span>";
					}
					if(k==0){
						tick = tempBlockTwoData[j][k];
						if (tick.equals("1")){
							blockTwoContent = blockTwoContent+
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/green_t.png' height='20' width='20'>" +
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/yellow_ut.png' class='tickimg' height='20' width='20'>" +
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/red_ut.png' height='20' width='20'>";
						}else if(tick.equals("2")){
							blockTwoContent = blockTwoContent+ 
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/green_ut.png' height='20' width='20'>" +
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/yellow_t.png' class='tickimg'  height='20' width='20'>" +
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/red_ut.png' height='20' width='20'>";
						}else if(tick.equals("3")){
							blockTwoContent = blockTwoContent+
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/green_ut.png' height='20' width='20'>" +
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/yellow_ut.png' class='tickimg'  height='20' width='20'>" +
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/red_t.png' height='20' width='20'>";
						}else{
							blockTwoContent = blockTwoContent+
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/green_ut.png' height='20' width='20'>" +
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/yellow_ut.png' class='tickimg' height='20' width='20'>" +
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/red_ut.png' height='20' width='20'>";
						}
					}
				}
				blockTwoContent =blockTwoContent+ "</div></li>";
			}

			blockTwoContent = "<div class='box-1'><ul>"+
					"<li><div class='tit'><span class='tit-span'>UNDERHOOD</span></div></li>"+
					blockTwoContent + "</ul></div>";

			for(j=0;j<bbSize;j++){
				blockBatteryContent = blockBatteryContent+ "<div class='bat-box'>";
				for(k=0;k<2;k++){
					if(k == 1){
						blockBatteryContent = blockBatteryContent+ "<span style='font-size:15px;'>" + tempBlockBatteryData[j][k] + "</span>";
					}
					if(k==0){
						tick = tempBlockBatteryData[j][k];
						if (tick.equals("1")){
							blockBatteryContent = blockBatteryContent+
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/green_t.png' height='20' width='20'>" +
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/yellow_ut.png' class='tickimg'  height='20' width='20'>" +
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/red_ut.png' height='20' width='20'>";
						}else if(tick.equals("2")){
							blockBatteryContent = blockBatteryContent+ 
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/green_ut.png' height='20' width='20'>" +
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/yellow_t.png' class='tickimg'  height='20' width='20'>" +
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/red_ut.png' height='20' width='20'>";
						}else if(tick.equals("3")){
							blockBatteryContent = blockBatteryContent+
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/green_ut.png' height='20' width='20'>" +
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/yellow_ut.png' class='tickimg'  height='20' width='20'>" +
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/red_t.png' height='20' width='20'>";
						}else{
							blockBatteryContent = blockBatteryContent+
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/green_ut.png' height='20' width='20'>" +
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/yellow_ut.png' class='tickimg' height='20' width='20'>" +
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/red_ut.png' height='20' width='20'>";
						}
					}
				}
				blockBatteryContent =blockBatteryContent+ "</div>";
			}

			blockBatteryContent = "<div class='box-1'><ul>"+
					"<li><div class='tit'><span class='tit-span'>BATTERY</span></div></li><li>"+
					blockBatteryContent + 
					"<div style='width:340px; text-align:right;'><img src='"+Configuration.SERVICE_URL+"/images/system_files/battery.png'></div></li></ul></div>";



			for(j=0;j<b3Size;j++){
				blockThreeContent = blockThreeContent+ "<li><div class='box-cont'>";
				for(k=0;k<2;k++){
					if(k == 1){
						blockThreeContent = blockThreeContent+ "<span>" + tempBlockThreeData[j][k] + "</span>";
					}
					if(k==0){
						tick = tempBlockThreeData[j][k];
						if (tick.equals("1")){
							blockThreeContent = blockThreeContent+
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/green_t.png' height='20' width='20'>" +
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/yellow_ut.png' class='tickimg'  height='20' width='20'>" +
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/red_ut.png' height='20' width='20'>";
						}else if(tick.equals("2")){
							blockThreeContent = blockThreeContent+ 
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/green_ut.png' height='20' width='20'>" +
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/yellow_t.png' class='tickimg'  height='20' width='20'>" +
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/red_ut.png' height='20' width='20'>";
						}else if(tick.equals("3")){
							blockThreeContent = blockThreeContent+
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/green_ut.png' height='20' width='20'>" +
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/yellow_ut.png' class='tickimg'  height='20' width='20'>" +
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/red_t.png' height='20' width='20'>";
						}else{
							blockThreeContent = blockThreeContent+
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/green_ut.png' height='20' width='20'>" +
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/yellow_ut.png' class='tickimg' height='20' width='20'>" +
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/red_ut.png' height='20' width='20'>";
						}
					}
				}
				blockThreeContent =blockThreeContent+ "</div></li>";
			}

			blockThreeContent = "<div class='box-3'><ul>"+
					"<li><div class='box-3-tit'><span class='tit-span'>UNDER VEHICLE</span></div></li>"+
					blockThreeContent + "</ul></div>";


			for(j=0;j<2;j++){
				if(j == 0){
					blockFourZeroLeftContent = blockFourZeroLeftContent+ "<div class='box-conts-left'>";
				}
				if(j == 1){
					blockFourZeroLeftContent = blockFourZeroLeftContent+ "<div class='box-conts-right'>";
				}

				for(k=0;k<2;k++){
					if(k == 0){
						blockFourZeroLeftContent = blockFourZeroLeftContent+ "<span>" + tempBlockFourZeroData[j][k] + "</span>";
					}
					if(k==1){
						tick = tempBlockFourZeroData[j][k];
						if (tick.equals("1")){
							blockFourZeroLeftContent = blockFourZeroLeftContent+
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/green_t.png' height='20' width='20'>" +
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/yellow_ut.png' class='tickimg'  height='20' width='20'>" +
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/red_ut.png' height='20' width='20'>";
						}else if(tick.equals("2")){
							blockFourZeroLeftContent = blockFourZeroLeftContent+ 
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/green_ut.png' height='20' width='20'>" +
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/yellow_t.png' class='tickimg'  height='20' width='20'>" +
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/red_ut.png' height='20' width='20'>";
						}else if(tick.equals("3")){
							blockFourZeroLeftContent = blockFourZeroLeftContent+
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/green_ut.png' height='20' width='20'>" +
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/yellow_ut.png' class='tickimg'  height='20' width='20'>" +
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/red_t.png' height='20' width='20'>";
						}else{
							blockFourZeroLeftContent = blockFourZeroLeftContent+
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/green_ut.png' height='20' width='20'>" +
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/yellow_ut.png' class='tickimg' height='20' width='20'>" +
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/red_ut.png' height='20' width='20'>";
						}
					}
				}
				blockFourZeroLeftContent =blockFourZeroLeftContent+ " " + tempBlockInchesData[j][1] +"/32\"</div>";
			}

			blockFourZeroLeftContent = "<li>"+ blockFourZeroLeftContent +"</li>";


			for(j=2;j<4;j++){
				if(j == 2){
					blockFourZeroRightContent = blockFourZeroRightContent+ "<div class='box-conts-left'>";
				}
				if(j == 3){
					blockFourZeroRightContent = blockFourZeroRightContent+ "<div class='box-conts-right'>";
				}

				for(k=0;k<2;k++){
					if(k == 0){
						blockFourZeroRightContent = blockFourZeroRightContent+ "<span>" + tempBlockFourZeroData[j][k] + "</span>";
					}
					if(k==1){
						tick = tempBlockFourZeroData[j][k];
						if (tick.equals("1")){
							blockFourZeroRightContent = blockFourZeroRightContent+
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/green_t.png' height='20' width='20'>" +
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/yellow_ut.png' class='tickimg'  height='20' width='20'>" +
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/red_ut.png' height='20' width='20'>";
						}else if(tick.equals("2")){
							blockFourZeroRightContent = blockFourZeroRightContent+ 
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/green_ut.png' height='20' width='20'>" +
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/yellow_t.png' class='tickimg'  height='20' width='20'>" +
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/red_ut.png' height='20' width='20'>";
						}else if(tick.equals("3")){
							blockFourZeroRightContent = blockFourZeroRightContent+
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/green_ut.png' height='20' width='20'>" +
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/yellow_ut.png' class='tickimg'  height='20' width='20'>" +
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/red_t.png' height='20' width='20'>";
						}else{
							blockFourZeroRightContent = blockFourZeroRightContent+
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/green_ut.png' height='20' width='20'>" +
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/yellow_ut.png' class='tickimg' height='20' width='20'>" +
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/red_ut.png' height='20' width='20'>";
						}
					}
				}
				blockFourZeroRightContent =blockFourZeroRightContent+ " " + tempBlockInchesData[j][1] +"/32\"</div>";
			}

			blockFourZeroRightContent = "<li>"+ blockFourZeroRightContent +"</li>";


			for(j=0;j<b41Size;j++){
				if(j==0){
					blockFourOneContent = blockFourOneContent+ "<div class='tire-top'><span class='wear' style='font-size:13px;'><b><i>Wear Pattern/ Damage</i></b></span>";
				}else{
					blockFourOneContent = blockFourOneContent+ "<div class='tire-txt'>";
				}

				for(k=0;k<2;k++){
					if(k == 0){
						blockFourOneContent = blockFourOneContent+ "<label style='padding-right:3px; vertical-align:middle; margin-top:5px;'>" + tempBlockFourOneData[j][k] + "</label>";
					}
					if(k==1){
						tick = tempBlockFourOneData[j][k];
						if (tick.equals("1")){
							blockFourOneContent = blockFourOneContent+
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/green_t.png' height='20' width='20'>" +
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/yellow_ut.png' class='tickimg'  height='20' width='20'>" +
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/red_ut.png' height='20' width='20'>";
						}else if(tick.equals("2")){
							blockFourOneContent = blockFourOneContent+ 
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/green_ut.png' height='20' width='20'>" +
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/yellow_t.png' class='tickimg'  height='20' width='20'>" +
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/red_ut.png' height='20' width='20'>";
						}else if(tick.equals("3")){
							blockFourOneContent = blockFourOneContent+
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/green_ut.png' height='20' width='20'>" +
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/yellow_ut.png' class='tickimg'  height='20' width='20'>" +
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/red_t.png' height='20' width='20'>";
						}else{
							blockFourOneContent = blockFourOneContent+
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/green_ut.png' height='20' width='20'>" +
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/yellow_ut.png' class='tickimg' height='20' width='20'>" +
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/red_ut.png' height='20' width='20'>";
						}
					}
				}
				blockFourOneContent =blockFourOneContent+ "</div>";
			}

			blockFourOneContent = "<div class='box-t'><img style='float:left' src='"+Configuration.SERVICE_URL+"/images/system_files/wheel.png'>"+
					blockFourOneContent + "</div>";


			for(j=0;j<b42Size;j++){
				blockFourTwoContent = blockFourTwoContent+ "<div class='air-txt'>";
				for(k=0;k<2;k++){
					if(k == 0)
						blockFourTwoContent = blockFourTwoContent+ "<label>" + tempBlockFourTwoData[j][k] + "</label>";
					if(k ==1 )
						blockFourTwoContent = blockFourTwoContent+ "<input type='text' class='tire-input' value='"+ tempBlockFourTwoData[j][k] +"' readonly />";
				}
				blockFourTwoContent =blockFourTwoContent+ "</div>";
			}

			blockFourTwoContent = "<li><div><span><b><i>Air Pressure</i></b></span></div>" +
					"<div class='tire-top1'></div><div class='tire-top1'>"+ tempBlockPressureImgContent +
					"<span style='font-size:12px; padding-left:2px;'>TPMS Warning System</span></div>" +
					"<div style='width:100px; margin-left:20px;'><span style='font-size:6px; line-height:25px; padding-left:5px;'> BEFORE</span><span style='font-size:6px; padding-left:8px; line-height:20px;' >OEM SPEC</span></div>"+
					"<div class='air'>" +
					"<div class='air-right'><input type='text' class='tire-biginput'  value='"+ tempBlockPressureData[0][1] +"' readonly />"+
					"<p style='height:1px;'>&nbsp;</p><input type='text' class='tire-biginput'  value='"+ tempBlockPressureData[1][1] +"' readonly />" +
					"</div>"+
					blockFourTwoContent + 
					"</div></li>";

			for(j=0;j<b43Size;j++){
				for(k=0;k<2;k++){
					if(k == 1){
						if(tempBlockFourThreeData[j][k].equals("1")){
							blockFourThreeContent = blockFourThreeContent+ "<input type='checkbox' checked disabled/>";
						}else{
							blockFourThreeContent = blockFourThreeContent+ "<input type='checkbox' disabled />";
						}
					}
					if(k ==0){
						blockFourThreeContent = blockFourThreeContent+ tempBlockFourThreeData[j][k];	
					}
				}
			}

			blockFourThreeContent = "<li><div><span><b><i>Tire Check/ OE Interval	Suggests</i></b></span></div><div class='chk'>" +
					blockFourThreeContent + "</div></li>";

			interBlockFourContent = "<div class='right_box-t'><ul>"  + 
					blockFourTwoContent + blockFourThreeContent +
					"</ul></div>";

			footContent1 = "<div class='comments'>COMMENTS </div><div style='float:left;'><p class='usercontent'>"+ comments + "</p></div><div class='comments'>SERVICES </div><div style='float:left;'><p class='usercontent'>"+ tempServiceFinalContent + "</p></div>";
			footContent2 = "<div class='tech'><span style='float:left;'>TECHNICIAN  &nbsp;</span><span style='float:right; padding-right:50px;'>DATE  &nbsp;</span></div>";
			footContent2 = footContent2 + "<div class='sign'><img src='"+Configuration.SERVICE_URL+"/images/" + signImage +"' width='150px' height='100px'></div>";

			footContent2 = footContent2 + "<div style='float:right; width:450px; text-align:right;'><div class='dat'>"+formDate+"</div></div>";

			ultimateContent = "<html><head>"+css+ "</head><body><div id='wrapper'><div class='header'>" +
					"<img src='"+Configuration.SERVICE_URL+"/images/settings_images/"+headerImageOne+"' width='208' height='100'>" +
					"<img src='"+Configuration.SERVICE_URL+"/images/settings_images/"+headerImageTwo+"' width='590' height='100'>" +
					"<img src='"+Configuration.SERVICE_URL+"/images/settings_images/"+headerImageThree+"' width='161' height='100'></div>" +
					"<div class='inner-head'><h1>VEHICLE INSPECTION REPORT</h1></div>"+
					profileContent + 
					"<div class='inn'><div class='legend_left'><img src='"+Configuration.SERVICE_URL+"/images/system_files/green_ut.png' height='20' width='20'>" +
					"<span style='font-size:15px; padding-left:5px; padding-right:5px; font-weight:bold;'>CHECKED AND OK</span></div><div class='legend_center'>" +
					"<img src='"+Configuration.SERVICE_URL+"/images/system_files/yellow_ut.png' height='20' width='20'><span style='font-size:15px; padding-left:5px; padding-right:5px; font-weight:bold;'>MAY REQUIRE ATTENTION</span></div><div class='legend_right'><img src='"+Configuration.SERVICE_URL+"/images/system_files/red_ut.png' height='20' width='20'><span style='font-size:15px; padding-left:5px; padding-right:5px; font-weight:bold;'>REQUIRES IMMEDIATE ATTENTION</span></div>" +
					"<div class='inn-lefts'>" +
					blockOneContent +blockTwoContent + blockBatteryContent + 
					"</div><div class='inn-rights'>" +
					blockThreeContent	+ 
					"<div class='box-4'><ul>" +
					"<li><div class='box-3-tit'><span class='tit-span'>TIRES</span></div>"+
					"<li><p><span style='font-size:13px; font-weight:bold'>Tread Depth</span><br><br></p></li>" +
					"<li><div class='box-cont'>" +
					"<img src='"+Configuration.SERVICE_URL+"/images/system_files/green_ut.png' height='20' width='20'><label style='font-style:italic; font-size:11px; font-weight:bold;'>7/32' greater</label>" +
					"<img src='"+Configuration.SERVICE_URL+"/images/system_files/yellow_ut.png' height='20' width='20' class='tickimg' ><label style='font-style:italic; font-weight:bold; font-size:11px;'>3/32' to 6/32'</label>" +
					"<img src='"+Configuration.SERVICE_URL+"/images/system_files/red_ut.png' height='20' width='20' class='tickimg' ><label style='font-style:italic; font-weight:bold;'>2/32' or less</label>" +
					"</div></li>" +
					blockFourZeroLeftContent + blockFourZeroRightContent +
					"<li><ul><li><div class='box-tire'>" + 
					blockFourOneContent + interBlockFourContent +
					"</div></li></ul></li>" + 
					"</ul></div></div></div>" +footContent1 + imageContent + footContent2 +"</div></div></body></html>"; 

		}
		catch (Exception e) {
			e.printStackTrace();
		}



		result = ultimateContent;
		return Response.status(200).entity(result).build();
	}




	@Path("twoHtml/{u}")
	@GET
	@Produces("text/html")
	public Response twoHTML(@PathParam("u") int u){
		Connection conn = null;
		conn = connectToDatabaseOrDie();
		String result = "", zeroContent = "", oneContent = "", tick = "", ultimateContent = "", tempContent = "", css = "", profileContent= "";
		String firstName = "", lastName = "", mobileNo ="", alternativeMobileNo= "", serviceType= "";
		String vin = "", make = "", model ="", year ="", mileage= "", comments ="", formDate="";
		String[] tempImageContent = new String[100];
		String[] tempImageCamContent = new String[125];
		String[] tempImageCamDesc = new String[125];
		String [][] tempBlockOneData = new String[100][2];
		String [][] tempBlockTwoData = new String[100][2];
		String[] tempBlockServiceData = new String[25];
		String imageContent = "", signImage = "", footContent1 = "", footContent2 = "", tempServiceContent = "", tempServiceFinalContent = "";
		int i= 0, j= 0, k= 0, tempUserId = 0;
		int imageSize = 0, imageCamSize = 0;
		int submissionId = u;
		int b1Size = 0, b2Size= 0;
		String headerImageOne = "", headerImageTwo ="", headerImageThree ="";

		css = "<style type='text/css'>body{	font-family: 'Helvetica';}body,*{margin:0;padding:0;	}h1{	width:960px;height:44px;background-image:url("+Configuration.SERVICE_URL+"/images/system_files/tile_img.png);line-height:44px;color:#FFF;font-size:18px;text-align:center;font-family:'Trebuchet MS', Arial, Helvetica, sans-serif;}#wrapper{width:960px; background-color:#FFF; margin:auto;}.inner-head {	text-align:center;	width:960px;	height:40px;	}.inner-logo{	border:1px solid #000;	width:385px;	height:100px;	text-align:center;	float:left;	position:relative;	left:290px;}.profile_title{width:960px;text-align:left;height:35px;color:#000;font-size:15px;font-family:'Trebuchet MS', Arial, Helvetica, sans-serif;background-color:#e8e8e8;margin-top:10px;line-height:35px;}.profile_title span{ font-weight:bold;	padding-left:50px;	font-size:15px;}.profile-form-left ul{	float:left; text-align:left;	padding-left:50px;	width:440px;	padding-top:10px;}.profile-form-left ul li{	list-style:none;	height:50px;}.profile-form-left ul li label{	width:150px;	font-size:14px;	display:inline-block;}.profile-form-right ul{	float:right;	width:440px; text-align:left;	padding-top:10px;}.profile-form-right ul li{	list-style:none;	height:50px; font-size:14px;}.profile-form-right ul li label{	width:150px;	font-size:14px;	display:inline-block;}.photo-slider{ text-align:left; float:left;	width:960px; background-color:#e8e8e8; height:35px; }.photo-slider span{	position:relative; background-color:#e8e8e8; font-weight:bold; 	padding-left:50px;	line-height:35px; font-size:14px;}.photo{	float:left;	padding-top:20px;	padding-left:50px;	width:910px;}.photo img{	float:left;  padding-right:10px; padding-bottom:10px;}.mileage{	float:left;	width:960px;	height:20px;	position:relative;	top:5px;	text-align:center;}.inn{	width:960px;	margin-top:210px;}.inn-left {	width:450px;	float:left;	padding:20px 0 0 50px;}.inn-left ul{	width:400px;	float:left;	padding:20px 0 0 50px;}.inn-left ul li{	list-style:none;	width:200px;	height:70px;}.inn-text{	color:mediumblue;	font-weight:bold;	font-size:20px;	position:relative;	bottom:5px;}.inn-black{	font-weight:bold;	font-size:15px;	color:#000;	position:relative;	bottom:5px;}.inn-right{	width:400px;	float:left;	padding:20px 0 0 50px;}.inn-right ul{	width:450px;	float:left;	padding:20px 0 0 50px;}.inn-right ul li{	list-style:none;	width:200px;	height:70px;}.comments{	float:left; font-size:15px; font-weight:bold; margin-top:10px; width:910px; padding-left:50px; background-color:#e8e8e8;height:35px; line-height:35px; text-align:left; margin-bottom:10px;}.inspire{	width:960px;	float:left;	padding:20px 0 0 30px;}.ins{	width:400px;	height:50px;	float:left;}.dat{	width:400px; line-height:35px;	height:50px;float:left;}p > input {    float: left;    height: 16px;    left: 0;    margin: 0;       padding: 0;    width: 16px;}p > label {    color: #000000;    float: left;    line-height: 16px;    padding: 0 0 0 5px;	font-size:15px;}.name{	width:320px;	height:50px;	float:left;}.mil{	width:320px;	height:50px;	float:left;}.make{	width:320px;	height:50px;	float:left;}.chk-mark{float:left;height:50px;}.chk-ok{	float:left;	padding: 2px 0 0 100px;	width: 250px;}.req-att{float:left;width:230px;}.req-imm{float:left;width:300px;padding-left:50px;}div > label{	bottom: 7px;    padding-left: 5px;    position: relative;    text-align: center;}.inn-lefts{ height: 500px; width: 300px; position: relative; left: 100px; top: 20px; float:left;}.inn-rights{height: 500px;left: 110px;position: relative;top: 20px;width: 400px;float:left;}.box-1{float:left;width:300px;height:400px;}.box-1 ul li{list-style:none;float:left;border:1px solid #000;}.box-1 ul li p{font-size:15px;text-align:center;width:300px;font-size:10px;}.tit{width:300px;text-align:center;background-color:blue;color:#fff;height:25px;}.box-3-tit{width:400px;text-align:center;background-color:blue;color:#fff;height:25px;}.tit-span{font-weight:bold;font-size:12px;}.box-car{width:300px;text-align:center;}.box-cont{width:300px;float:left;position:relative;left:5px;}.box-cont span{	padding:2px 2px;	font-size:15px;	position:relative;	bottom:8px;}.box-3{float:left;width:400px;height:300px;}.box-3 ul li{list-style:none;float:left;width:400px;border:1px solid #000;}.box-3 ul li p{font-size:15px;text-align:center;width:400px;font-size:10px;}.box-4{float:left;width:400px;height:400px;border:1px solid #000;}.box-4 ul li{list-style:none;float:left;}.box-4 ul li p{font-size:15px;text-align:center;width:400px;font-size:10px;}.box-cont label{	padding:2px 2px;	font-size:11px;	position:relative;	bottom:8px;}.box-conts{width:150px;float:left;position:relative;left:5px;text-align:center;}.box-tire {background-color:#d4daee;height:180px;width:400px;}.box-t{height:180px;width:200px;float:left;}.right_box-t{width:200px;float:left;height:180px;}.right_box-t ul li{float:left;width:99px;height:180px;border-left:1px solid blue;}.right_box-t ul li span{	 position: relative;    top: 20px;    width: 100px;}.wear{ float: left;    width: 100px;}.left_table{	float:left;	width:370px;	padding-left:100px;}.left_table  ul li{ 	list-style:none;	width:370px;   padding-bottom: 30px;  padding-left: 10px;    padding-top: 7px;}.right_table{	float:left;	width:370px;}.left_table_cell{	width:170px;height:45px;float:left; font-size:13px; color:#393939}.right_table ul li{	list-style:none;		width:370px;   padding-bottom: 30px;   padding-left: 0px;    padding-top: 7px;	}.right_table_cell{	width:170px;float:left;}.right_table  ul li img{	margin-left: 10px;}.left_table  ul li img{	margin-left: 10px;}.text_class{	float:left;	font-size:14;	font-weight:bold;}.clear{ clear:both; }.sign{margin-top:10px;width:300px;float:left;padding-left:40px;}.usercontent{font-size:14px;line-height:25px;padding-left:50px;}.tickimg{padding-left:5PX; padding-right:5PX;}.tech{float:left;font-size:15px; font-weight:bold; margin-top:10px;width:910px;padding-left:50px; background-color:#e8e8e8; height:35px; line-height:35px;text-align:left;	}.header{height:100px;width:960px;}.legend{width:960px;height:35px;float:left;text-align:center;}.legend_left{width:300px;height:35px;float:left;margin-left:10px;text-align:center;}.legend_center{width:300px;height:35px;float:left;margin-left:10px;text-align:center;}.legend_right{width:300px;height:35px;float:left;margin-left:10px;text-align:center;}.userlist{font-size:13px;font-weight:bold;}.usercontent{font-size:14px;line-height:30px;}	</style>";

		try{
			Statement st = conn.createStatement();
			ResultSet rs;

			rs = st.executeQuery("select * from form_submission_component_data where form_submission_id = "+ submissionId +" and position_id >= 7 and position_id <= 21");
			while(rs.next()){
				b1Size++;
			}
			i = 0;
			rs = st.executeQuery("select * from form_submission_component_data where form_submission_id = "+ submissionId +" and position_id >= 7 and position_id <= 21");
			while (rs.next()){
				tempBlockOneData[i][0] = rs.getString("component_name");
				tempBlockOneData[i][1] = rs.getString("option_description");
				i++;
			}

			rs = st.executeQuery("select * from form_submission_component_data where form_submission_id = "+ submissionId +" and position_id >= 24 and position_id <= 37");
			while(rs.next()){
				b2Size++;
			}
			i = 0;
			rs = st.executeQuery("select * from form_submission_component_data where form_submission_id = "+ submissionId +" and position_id >= 24 and position_id <= 37");
			while (rs.next()){
				tempBlockTwoData[i][0] = rs.getString("component_name");
				tempBlockTwoData[i][1] = rs.getString("option_description");
				i++;
			}

			rs = st.executeQuery("select * from form_submission_profile_data where form_submission_id =  "+ submissionId);
			while (rs.next()){
				tempContent = tempContent +" "+ rs.getString("profile_field") + "=\"" + rs.getString("profile_value") + "\"; "; 
			}

			//Content for Services Block.

			rs = st.executeQuery("select profile_value from form_submission_profile_data where profile_field = 'service_type' and form_submission_id =  "+ submissionId);
			while (rs.next()){
				tempServiceContent = rs.getString("profile_value"); 
			}

			tempBlockServiceData = tempServiceContent.split(",");
			for(i =0;i<tempBlockServiceData.length;i++){
				System.out.println(tempBlockServiceData[i]);
				tempServiceFinalContent = tempBlockServiceData[i] + "<br>" + tempServiceFinalContent;  
			}

			/*
			 * 
			 * Image Related Code
			 * 
			 */

			rs = st.executeQuery("select * from form_submission_image where form_submission_id =  "+ submissionId);
			while (rs.next()){
				imageSize++;
			}


			rs = st.executeQuery("select * from form_submission_cam_image where form_submission_id =  "+ submissionId);
			while (rs.next()){
				imageCamSize++;
			}

			i=0;
			rs = st.executeQuery("select * from form_submission_image where form_submission_id =  "+ submissionId);
			while (rs.next()){
				tempImageContent[i] = rs.getString("image_file_name");
				i++;
			}

			i=0;
			rs = st.executeQuery("select * from form_submission_cam_image where form_submission_id =  "+ submissionId);
			while (rs.next()){
				tempImageCamContent[i] = rs.getString("image_file_name");
				tempImageCamDesc[i] = rs.getString("image_description");
				i++;
			}

			rs = st.executeQuery("select * from form_submission_sign_image where form_submission_id =  "+ submissionId);
			while (rs.next()){
				signImage = rs.getString("image_file_name");
			}

			imageContent = "<div class='photo-slider'><span> PHOTOS</span></div><div class='photo'>";

			for(int m=0; m<imageSize;m++){
				imageContent = imageContent + "<img src='"+Configuration.SERVICE_URL+"/images/" + tempImageContent[m] +"' width='150px' height='100px'>";
			}

			imageContent = imageContent + "</div>";

			for(int m=0; m<imageCamSize;m++){
				imageContent = imageContent + "<div class='photo'><img src='"+Configuration.SERVICE_URL+"/images/" + tempImageCamContent[m] +"' width='150px' height='100px'>"+ tempImageCamDesc[m] +"</div>";
			}

			/*
			 * 
			 * Header Area related Code
			 * 
			 */

			rs = st.executeQuery("select user_id from form_submission_index where form_submission_id = "+ submissionId);

			while(rs.next()){
				tempUserId = rs.getInt("user_id");
			}

			rs = st.executeQuery("select image_file_name from user_settings_image where image_type='headerimageone' and user_id="+ tempUserId);
			while(rs.next()){
				headerImageOne = rs.getString("image_file_name");
			}
			if(headerImageOne.equals("")){
				headerImageOne = "top_left_img1.png";
			}

			rs = st.executeQuery("select image_file_name from user_settings_image where image_type='headerimagetwo' and user_id="+ tempUserId);
			while(rs.next()){
				headerImageTwo = rs.getString("image_file_name");
			}
			if(headerImageTwo.equals("")){
				headerImageTwo = "top_left_img2.png";
			}

			rs = st.executeQuery("select image_file_name from user_settings_image where image_type='headerimagethree' and user_id="+ tempUserId);
			while(rs.next()){
				headerImageThree = rs.getString("image_file_name");
			}
			if(headerImageThree.equals("")){
				headerImageThree = "top_left_img3.png";
			}


			rs = st.executeQuery("select image_file_name from user_settings_image where image_type='headerimagethree' and user_id="+ tempUserId);
			while(rs.next()){
				headerImageThree = rs.getString("image_file_name");
			}
			if(headerImageOne.equals("")){
				headerImageThree = "top_left_img3.png";
			}

			firstName = parseValueFromHeader(tempContent, "first_name");
			lastName = parseValueFromHeader(tempContent, "last_name"); 
			mobileNo = parseValueFromHeader(tempContent, "mobile_no");
			vin = parseValueFromHeader(tempContent, "vin"); 
			make = parseValueFromHeader(tempContent, "make"); 
			model = parseValueFromHeader(tempContent, "model"); 
			year = parseValueFromHeader(tempContent, "year"); 
			mileage= parseValueFromHeader(tempContent, "mileage");
			comments= parseValueFromHeader(tempContent, "Comments");
			formDate = parseValueFromHeader(tempContent, "Date");
			alternativeMobileNo  = parseValueFromHeader(tempContent, "alternative_mobileno");
			if(parseValueFromHeader(tempContent, "service_type") == null){
				serviceType  = "";
			}else{
				serviceType  = parseValueFromHeader(tempContent, "service_type");
			}

			profileContent = "<div class='profile_title'><span class='profile_title'> PROFILE</span>"+
					"<div class='profile-form-left'><ul>"+
					"<li><label><b>First name </b></label> <label>"+ firstName +"</label></li>"+
					"<li><label><b>Last name </b></label> <label>"+ lastName +"</label></li>"+
					"<li><label><b>Moblie Phone </b></label> <label>"+ mobileNo +"</label></li>"+
					"<li><label><b>Alternate Phone </b></label> <label>"+ alternativeMobileNo +"</label></li>"+
					"<li><label><b>VIN </b></label> <label>"+ vin +"</label></li>"+
					"</ul></div><div class='profile-form-right'><ul>"+
					"<li><label><b>Make </b></label> <label>"+ make +"</label></li>"+
					"<li><label><b>Model </b></label> <label>"+ model +"</label></li>"+
					"<li><label><b>Year </b></label> <label>"+ year +"</label></li>"+
					"<li><label><b>Mileage </b></label> <label>"+ mileage +"</label></li>"+
					"</ul></div></div><br><br><br>";



			for(j=0;j<b1Size;j++){
				zeroContent = zeroContent+ "<li>";
				for(k=0;k<2;k++){
					if(k==0){
						zeroContent = zeroContent+ "<div class='left_table_cell'>" + tempBlockOneData[j][k] + "</div>";	
					}

					if(k == 1){
						tick = tempBlockOneData[j][k];
						if (tick.equals("1")){
							zeroContent = zeroContent+ "<div class='left_table_cell'>" +
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/green_t.png' height='20' width='20'>" +
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/yellow_ut.png' height='20' width='20'>" +
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/red_ut.png' height='20' width='20'></div>";

						}else if(tick.equals("2")){
							zeroContent = zeroContent+ "<div class='left_table_cell'>" +
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/green_ut.png' height='20' width='20'>" +
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/yellow_t.png' height='20' width='20'>" +
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/red_ut.png' height='20' width='20'></div>";

						}else if(tick.equals("3")){
							zeroContent = zeroContent+ "<div class='left_table_cell'>" +
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/green_ut.png' height='20' width='20'>" +
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/yellow_ut.png' height='20' width='20'>" +
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/red_t.png' height='20' width='20'></div>";
						}else{
							zeroContent = zeroContent+ "<div class='left_table_cell'>" +
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/green_ut.png' height='20' width='20'>" +
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/yellow_ut.png' height='20' width='20'>" +
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/red_ut.png' height='20' width='20'></div>";
						}				
					}

				}
				zeroContent = zeroContent + "</li>";
			}

			zeroContent = "<div class='left_table'><ul><li><div class='left_table_cell text_class' style=' color:#870101;'>ITEMS</div><div class='left_table_cell text_class' style=''><span class='left_table_cell text_class' style=' color:#870101; padding-left:10px;'>CONDITION</span></div></li>" + zeroContent + "</ul></div>";


			for(j=0;j<b2Size;j++){
				oneContent = oneContent+ "<li>";
				for(k=0;k<2;k++){
					if(k==0){
						oneContent = oneContent+ "<div class='left_table_cell'>" + tempBlockTwoData[j][k] + "</div>";	
					}

					if(k == 1){
						tick = tempBlockTwoData[j][k];
						if (tick.equals("1")){
							oneContent = oneContent+ "<div class='left_table_cell'>" +
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/green_t.png' height='20' width='20'>" +
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/yellow_ut.png' height='20' width='20'>" +
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/red_ut.png' height='20' width='20'></div>";

						}else if(tick.equals("2")){
							oneContent = oneContent+ "<div class='left_table_cell'>" +
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/green_ut.png' height='20' width='20'>" +
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/yellow_t.png' height='20' width='20'>" +
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/red_ut.png' height='20' width='20'></div>";

						}else if(tick.equals("3")){
							oneContent = oneContent+ "<div class='left_table_cell'>" +
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/green_ut.png' height='20' width='20'>" +
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/yellow_ut.png' height='20' width='20'>" +
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/red_t.png' height='20' width='20'></div>";
						}else{
							oneContent = oneContent+ "<div class='left_table_cell'>" +
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/green_ut.png' height='20' width='20'>" +
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/yellow_ut.png' height='20' width='20'>" +
									"<img src='"+Configuration.SERVICE_URL+"/images/system_files/red_ut.png' height='20' width='20'></div>";
						}				
					}

				}
				oneContent = oneContent+ "</li>";
			}


			oneContent = "<div class='left_table'><ul><li><div class='left_table_cell text_class' style=' color:#870101;'>ITEMS</div><div class='left_table_cell text_class' style=''><span class='left_table_cell text_class' style=' color:#870101; padding-left:10px;'>CONDITION</span></div></li>" + oneContent + "</ul></div>";

			footContent1 = "<div class='comments'>COMMENTS </div><div style='float:left;'><p class='usercontent'>"+ comments + "</p></div><div class='comments'>SERVICES </div><div style='float:left;'><p class='usercontent'>"+ tempServiceFinalContent + "</p></div>";
			footContent2 = "<div class='tech'><span style='float:left;'>TECHNICIAN  &nbsp;</span><span style='float:right; padding-right:50px;'>DATE  &nbsp;</span></div>";
			footContent2 = footContent2 + "<div class='sign'><img src='"+Configuration.SERVICE_URL+"/images/" + signImage +"' width='150px' height='100px'></div>";

			footContent2 = footContent2 + "<div style='float:right; width:450px; text-align:right;'><div class='dat'>"+formDate+"</div></div>";

			ultimateContent = "<html><head>"+css+ "</head><body><div id='wrapper'><div class='header'>" +
					"<img src='"+Configuration.SERVICE_URL+"/images/settings_images/"+headerImageOne+"' width='208' height='100'>" +
					"<img src='"+Configuration.SERVICE_URL+"/images/settings_images/"+headerImageTwo+"' width='590' height='100'>" +
					"<img src='"+Configuration.SERVICE_URL+"/images/settings_images/"+headerImageThree+"' width='161' height='100'></div>" +
					"<div class='inner-head'><h1>VEHICLE INSPECTION REPORT</h1></div>"+
					profileContent +
					"<div class='clear'></div><div>&nbsp;</div><div class='legend'><div class='legend_left'><img src='"+Configuration.SERVICE_URL+"/images/system_files/green_ut.png'  height='20' width='20'><span style='font-size:15px; padding-left:5px; padding-right:5px; font-weight:bold;'>NO IMMEDIATE ATTENTION </span></div><div class='legend_center'><img src='"+Configuration.SERVICE_URL+"/images/system_files/yellow_ut.png' height='20' width='20'><span style='font-size:15px; padding-left:5px; padding-right:5px; font-weight:bold;'>MAY REQUIRE FUTURE ATTENTION</span></div> <div class='legend_right'><img src='"+Configuration.SERVICE_URL+"/images/system_files/red_ut.png' height='20' width='20'><span style='font-size:15px; padding-left:5px; padding-right:5px; font-weight:bold;'>IMMEDIATE ATTENDTION</span></div></div>" +
					"<div style='width:960px;float:left;'>" + zeroContent + oneContent +"<div class='clear' ></div><div>&nbsp;</div><div>&nbsp;</div>"+ footContent1 + imageContent + footContent2 +"</div></div></body></html>"; 

		}
		catch (Exception e) {
			e.printStackTrace();
		}


		//result =  oneContent + zeroContent;
		result = ultimateContent;
		return Response.status(200).entity(result).build();
	}



	@Path("oneHtml/{u}")
	@GET
	@Produces("text/html")
	public Response oneHtml(@PathParam("u") int u){
		Connection conn = null;
		conn = connectToDatabaseOrDie();
		String result = null;
		int submissionId = u;
		int i =0;
		int size = 0, imageSize = 0, imageCamSize = 0;
		int limit = 0, tempUserId = 0;
		String zeroContent = "", oneContent = "", ultimateContent = "", tempContent = "", css = "", profileContent= "";
		String firstName = "", lastName = "", mobileNo ="", alternativeMobileNo= "", serviceType= "";
		String vin = "", make = "", model ="", year ="", mileage= "", comments ="", formDate ="";
		String[][] tempComponentData = new String[125][3];
		String[] tempImageContent = new String[125];
		String[] tempImageCamContent = new String[125];
		String[] tempImageCamDesc = new String[125];
		String[] tempBlockServiceData = new String[25];
		String imageContent = "", signImage = "", footContent1 = "", footContent2 = "", tempServiceContent = "", tempServiceFinalContent = "";
		String headerImageOne = "", headerImageTwo ="", headerImageThree ="";


		css = "<style type='text/css'>body{font-family: 'Helvetica';}body,*{margin:0;	padding:0;}h1{	width:960px;height:44px;background-image:url("+Configuration.SERVICE_URL+"/images/system_files/tile_img.png);line-height:44px;color:#FFF;font-size:18px;text-align:center;	font-family:'Trebuchet MS', Arial, Helvetica, sans-serif;}#wrapper{width:960px; background-color:#FFF; 	margin:auto;}.inner-head {	text-align:center;	width:960px;	height:40px;	}.inner-logo{	border:1px solid #000;	width:385px;	height:100px;	text-align:center;	float:left;	position:relative;	left:290px;}.profile{	text-align:left;padding-left:20px;width:940px;height:35px;color:#000;font-size:15px;font-family:'Trebuchet MS', Arial, Helvetica, sans-serif;background-color:#e8e8e8;margin-top:10px;line-height:35px;}.profile_title{		width:935px;text-align:left;padding-left:25px;height:35px;color:#000;font-size:15px;font-family:'Trebuchet MS', Arial, Helvetica, sans-serif;background-color:#e8e8e8;margin-top:10px;line-height:35px;}.profile_title span{ font-weight:bold; text-align:left;	font-size:15px;}.profile-form-left ul{	float:left; text-align:left;	padding-left:25px;	width:440px;	padding-top:10px;}.profile-form-left ul li{	list-style:none;	height:50px;}.profile-form-left ul li label{	width:150px;	font-size:14px;	display:inline-block;}.profile-form-right ul{	float:right;	width:440px; text-align:left;	padding-top:10px;}.profile-form-right ul li{	list-style:none;	height:50px; font-size:14px;}.profile-form-right ul li label{	width:150px;	font-size:14px;	display:inline-block;}.photo-slider{ text-align:left; float:left; padding-left:50px;	width:910px; background-color:#e8e8e8; height:35px; line-height:35px; font-weight:bold; font-size:14px;}.photo{	float:left;	padding-top:20px;	padding-left:50px;	width:910px;}.photo img{	float:left;  padding-right:10px; padding-bottom:10px;}.mileage{	float:left;	width:960px;	height:20px;	position:relative;	top:5px;	text-align:center;}	.inn{	width:960px;	margin-top:210px;}.inn-left {	width:450px;	float:left;	padding:20px 0 0 50px;}.inn-left ul{	width:400px;	float:left;	padding:20px 0 0 50px;}.inn-left ul li{	list-style:none;	width:200px;	height:70px;}.inn-text{	color:#2172bc; line-height:25px;	font-weight:bold;	font-size:14px;	position:relative;	bottom:5px;}.inn-black{	font-weight:bold;	font-size:13px;	color:#020000;	position:relative;	bottom:5px;}.inn-right{	width:400px;	float:left;	padding:20px 0 0 50px;}.inn-right ul{	width:450px;	float:left;	padding:20px 0 0 50px;}.inn-right ul li{	list-style:none;	width:200px;	height:70px;}	.comments{	float:left; font-size:14px; font-weight:bold; margin-top:10px; width:910px; padding-left:50px; background-color:#e8e8e8; height:35px; line-height:35px; text-align:left; margin-bottom:10px;}.inspire{	width:960px;	float:left;	padding:20px 0 0 30px;}.ins{	width:400px;	height:50px;	float:left;}	.dat{	width:400px; line-height:35px;	height:50px;float:left;}p > input {    float: left;    height: 16px;    left: 0;    margin: 0;        padding: 0;    width: 16px;}p > label {color: #000000;float: left;line-height: 16px;font-size:15px;}.name{	width:320px;height:50px;	float:left;}.mil{	width:320px;	height:50px;	float:left;}.make{	width:320px;	height:50px;	float:left;}.chk-mark{float:left;height:50px;}.chk-ok{	float:left;	padding: 2px 0 0 100px;	width: 250px;}.req-att{float:left;width:230px;}.req-imm{float:left;width:300px;padding-left:50px;}div > label{	bottom: 7px;    padding-left: 5px;    position: relative;    text-align: center;}.inn-lefts{ height: 500px; width: 300px; position: relative; left: 100px;  top: 20px; float:left;}.inn-rights{height: 500px;left: 110px;position: relative;top: 20px; width: 400px;float:left;}.box-1{float:left;width:300px;height:400px;}.box-1 ul li{list-style:none;float:left;border:1px solid #000;}.box-1 ul li p{font-size:15px;text-align:center;width:300px;font-size:10px;}.tit{width:300px;text-align:center;background-color:blue;color:#fff;height:25px;}.box-3-tit{width:400px;text-align:center;background-color:blue;color:#fff;height:25px;}.tit-span{font-weight:bold;font-size:12px;}.box-car{width:300px;text-align:center;}.box-cont{width:300px;float:left;position:relative;left:5px;}.box-cont span{	padding:2px 2px;	font-size:15px;	position:relative;	bottom:8px;}.box-3{float:left;width:400px;height:300px;}.box-3 ul li{list-style:none;float:left;width:400px;border:1px solid #000;}.box-3 ul li p{font-size:15px;text-align:center;width:400px;font-size:10px;}.box-4{float:left;width:400px;height:400px;border:1px solid #000;}.box-4 ul li{list-style:none;float:left;}.box-4 ul li p{font-size:15px;text-align:center;width:400px;font-size:10px;}.box-cont label{	padding:2px 2px;	font-size:11px;	position:relative;	bottom:8px;}.box-conts{width:150px;float:left;position:relative;left:5px;text-align:center;}.box-tire {background-color:#d4daee;height:180px;width:400px;}.box-t{height:180px;width:200px;float:left;}.right_box-t{width:200px;float:left;height:180px;}.right_box-t ul li{float:left;width:99px;height:180px;border-left:1px solid blue;}.right_box-t ul li span{	 position: relative;    top: 20px;    width: 100px;}.wear{ float: left;    width: 100px;}.sign{font-size: 15px;padding: 5px 1px;width: 400px;float:left;padding-left:30px;}.tech{float:left;font-size:15px; font-weight:bold; margin-top:10px;width:910px;padding-left:50px; background-color:#e8e8e8; height:35px; line-height:35px;text-align:left;	}.header{height:100px;width:960px;}.userlist{font-size:13px;font-weight:bold;}.usercontent{font-size:14px;line-height:25px;padding-left:50px;}</style>";

		try{
			Statement st = conn.createStatement();
			ResultSet rs = st.executeQuery("select * from form_submission_component_data where form_submission_id = "+ submissionId);
			while(rs.next()){
				size++;
			}
			i = 0;
			rs = st.executeQuery("select * from form_submission_component_data where form_submission_id = "+ submissionId);
			while (rs.next()){
				tempComponentData[i][0] = rs.getString("component_name");
				tempComponentData[i][1] = rs.getString("label");
				tempComponentData[i][2] = rs.getString("option_description");
				i++;
			}

			rs = st.executeQuery("select * from form_submission_profile_data where form_submission_id =  "+ submissionId);
			while (rs.next()){
				tempContent = tempContent +" "+ rs.getString("profile_field") + "=\"" + rs.getString("profile_value") + "\"; "; 
			}

			//Content for Services Block.

			rs = st.executeQuery("select profile_value from form_submission_profile_data where profile_field = 'service_type' and form_submission_id =  "+ submissionId);
			while (rs.next()){
				tempServiceContent = rs.getString("profile_value"); 
			}

			tempBlockServiceData = tempServiceContent.split(",");
			for(i =0;i<tempBlockServiceData.length;i++){
				System.out.println(tempBlockServiceData[i]);
				tempServiceFinalContent = tempBlockServiceData[i] + "<br>" + tempServiceFinalContent;  
			}

			/*
			 * 
			 * Image Related Code
			 * 
			 */

			rs = st.executeQuery("select * from form_submission_image where form_submission_id =  "+ submissionId);
			while (rs.next()){
				imageSize++;
			}


			rs = st.executeQuery("select * from form_submission_cam_image where form_submission_id =  "+ submissionId);
			while (rs.next()){
				imageCamSize++;
			}

			i=0;
			rs = st.executeQuery("select * from form_submission_image where form_submission_id =  "+ submissionId);
			while (rs.next()){
				tempImageContent[i] = rs.getString("image_file_name");
				i++;
			}

			i=0;
			rs = st.executeQuery("select * from form_submission_cam_image where form_submission_id =  "+ submissionId);
			while (rs.next()){
				tempImageCamContent[i] = rs.getString("image_file_name");
				tempImageCamDesc[i] = rs.getString("image_description");
				i++;
			}

			rs = st.executeQuery("select * from form_submission_sign_image where form_submission_id =  "+ submissionId);
			while (rs.next()){
				signImage = rs.getString("image_file_name");
			}

			imageContent = "<div class='photo-slider'><span> PHOTOS</span></div><div class='photo'>";

			for(int m=0; m<imageSize;m++){
				imageContent = imageContent + "<img src='"+Configuration.SERVICE_URL+"/images/" + tempImageContent[m] +"' width='150px' height='100px'>";
			}

			imageContent = imageContent + "</div>";

			for(int m=0; m<imageCamSize;m++){
				imageContent = imageContent + "<div class='photo'><img src='"+Configuration.SERVICE_URL+"/images/" + tempImageCamContent[m] +"' width='150px' height='100px'>"+ tempImageCamDesc[m] +"</div>";
			}

			/*
			 * 
			 * Header Area related Code
			 * 
			 */


			rs = st.executeQuery("select user_id from form_submission_index where form_submission_id = "+ submissionId);

			while(rs.next()){
				tempUserId = rs.getInt("user_id");
			}

			rs = st.executeQuery("select image_file_name from user_settings_image where image_type='headerimageone' and user_id="+ tempUserId);
			while(rs.next()){
				headerImageOne = rs.getString("image_file_name");
			}
			if(headerImageOne.equals("")){
				headerImageOne = "top_left_img1.png";
			}

			rs = st.executeQuery("select image_file_name from user_settings_image where image_type='headerimagetwo' and user_id="+ tempUserId);
			while(rs.next()){
				headerImageTwo = rs.getString("image_file_name");
			}
			if(headerImageTwo.equals("")){
				headerImageTwo = "top_left_img2.png";
			}

			rs = st.executeQuery("select image_file_name from user_settings_image where image_type='headerimagethree' and user_id="+ tempUserId);
			while(rs.next()){
				headerImageThree = rs.getString("image_file_name");
			}
			if(headerImageThree.equals("")){
				headerImageThree = "top_left_img3.png";
			}



			firstName = parseValueFromHeader(tempContent, "first_name");
			lastName = parseValueFromHeader(tempContent, "last_name"); 
			mobileNo = parseValueFromHeader(tempContent, "mobile_no");
			vin = parseValueFromHeader(tempContent, "vin"); 
			make = parseValueFromHeader(tempContent, "make"); 
			model = parseValueFromHeader(tempContent, "model"); 
			year = parseValueFromHeader(tempContent, "year"); 
			mileage= parseValueFromHeader(tempContent, "mileage");
			comments= parseValueFromHeader(tempContent, "Comments");
			formDate = parseValueFromHeader(tempContent, "Date");
			alternativeMobileNo  = parseValueFromHeader(tempContent, "alternative_mobileno");
			if(parseValueFromHeader(tempContent, "service_type") == null){
				serviceType  = "";
			}else{
				serviceType  = parseValueFromHeader(tempContent, "service_type");
			}

			profileContent = "<div class='profile_title'><span class='profile_title'> PROFILE</span>"+
					"<div class='profile-form-left'><ul>"+
					"<li><label><b>First name </b></label> <label>"+ firstName +"</label></li>"+
					"<li><label><b>Last name </b></label> <label>"+ lastName +"</label></li>"+
					"<li><label><b>Moblie Phone </b></label> <label>"+ mobileNo +"</label></li>"+
					"<li><label><b>Alternate Phone </b></label> <label>"+ alternativeMobileNo +"</label></li>"+
					"<li><label><b>VIN </b></label> <label>"+ vin +"</label></li>"+
					"</ul></div><div class='profile-form-right'><ul>"+
					"<li><label><b>Make </b></label> <label>"+ make +"</label></li>"+
					"<li><label><b>Model </b></label> <label>"+ model +"</label></li>"+
					"<li><label><b>Year </b></label> <label>"+ year +"</label></li>"+
					"<li><label><b>Mileage </b></label> <label>"+ mileage +"</label></li>"+
					"</ul></div></div><br><br><br>";


			int ctemp = size/2;
			int cmod = size%2;
			if(cmod > 0)
				limit = ctemp + 1;
			else
				limit = ctemp;			

			for(int j=0, l=ctemp;j<limit;j++, l++){
				zeroContent = zeroContent+ "<li>";				
				for(int k=0;k<3;k++){

					if(j < ctemp){
						if(k == 0)
							zeroContent = zeroContent + "<div class='inn-text'>"+ tempComponentData[j][k] + "</div>";
						if(k == 1)
							zeroContent = zeroContent + "<div class='inn-black'>"+ tempComponentData[j][k] + "</div>";
						if(k == 2){
							if(tempComponentData[j][k].length() != 0){
								zeroContent = zeroContent + "<div style='float:left; width:300px;'><div style=' float:left;'><img src='"+Configuration.SERVICE_URL+"/images/system_files/grey_tick.png' class='tickimg'  height='20' width='20'><span style='padding-left:3px; padding-right:3px; font-size:14px;'>"+ tempComponentData[j][k] + "</div></div>";
							}else{
								zeroContent = zeroContent + "<br />";
							}
						}
					}
					if(j == ctemp){
						if(k == 0)
							zeroContent = zeroContent + "<div class='inn-text'>"+ tempComponentData[l][k] + "</div>";
						if(k == 1)
							zeroContent = zeroContent + "<div class='inn-black'>"+ tempComponentData[l][k] + "</div>";
						if(k == 2){
							if(tempComponentData[l][k].length() != 0){
								zeroContent = zeroContent + "<div style='float:left; width:300px;'><div style=' float:left;'><img src='"+Configuration.SERVICE_URL+"/images/system_files/grey_tick.png' class='tickimg'  height='20' width='20'><span style='padding-left:3px; padding-right:3px; font-size:14px;'>"+ tempComponentData[l][k] + "</div></div>";
							}else{
								zeroContent = zeroContent + "<br />";
							}
						}	
					}	
				}
				zeroContent = zeroContent + "</li>";
			}
			zeroContent = "<div class='inn-left'><ul>" + zeroContent + "</ul></div>";

			for(int j=0, l=ctemp;j<limit;j++, l++){
				oneContent = oneContent+ "<li>";
				for(int k=0;k<3;k++){
					if(j != limit-1){
						if(k == 0)
							oneContent = oneContent + "<div class='inn-text'>"+ tempComponentData[l][k] + "</div>";
						if(k == 1)
							oneContent = oneContent + "<div class='inn-black'>"+ tempComponentData[l][k] + "</div>";
						if(k == 2){
							if(tempComponentData[l][k].length() != 0){
								oneContent = oneContent + "<div style='float:left; width:300px;'><div style=' float:left;'><img src='"+Configuration.SERVICE_URL+"/images/system_files/grey_tick.png' class='tickimg'  height='20' width='20'><span style='padding-left:3px; padding-right:3px; font-size:14px;'>"+ tempComponentData[l][k] + "</div></div>";
							}else{
								oneContent = oneContent + "<br>";
							}
						}

					}	
				}
				oneContent = oneContent + "</li>";
			}

			oneContent = "<div class='inn-right'><ul>" + oneContent + "</ul></div>";

			footContent1 = "<div class='comments'>COMMENTS </div><div style='float:left;'><p class='usercontent'>"+ comments + "</p></div><div class='comments'>SERVICES </div><div style='float:left;'><p class='usercontent'>"+ tempServiceFinalContent + "</p></div>";
			footContent2 = "<div class='tech'><span style='float:left;'>TECHNICIAN  &nbsp;</span><span style='float:right; padding-right:50px;'>DATE  &nbsp;</span></div>";
			footContent2 = footContent2 + "<div class='sign'><img src='"+Configuration.SERVICE_URL+"/images/" + signImage +"' width='150px' height='100px'></div>";

			footContent2 = footContent2 + "<div style='float:right; width:450px; text-align:right;'><div class='dat'>"+formDate+"</div></div>";

			ultimateContent = "<html><head>"+css+ "</head><body><div id='wrapper'><div class='header'>" +
					"<img src='"+Configuration.SERVICE_URL+"/images/settings_images/"+headerImageOne+"' width='208' height='100'>" +
					"<img src='"+Configuration.SERVICE_URL+"/images/settings_images/"+headerImageTwo+"' width='590' height='100'>" +
					"<img src='"+Configuration.SERVICE_URL+"/images/settings_images/"+headerImageThree+"' width='161' height='100'></div>" +
					"<h1>VEHICLE INSPECTION REPORT</h1>" + 
					profileContent + 
					"<div class='inn'>" + zeroContent + oneContent +footContent1 + imageContent + footContent2 +"</div></div></div></body></html>"; 

		}
		catch (Exception e) {
			e.printStackTrace();
		}



		result = ultimateContent;
		return Response.status(200).entity(result).build();
	}

	@Path("profileData")
	@GET
	@Produces("text/html")
	public Response profileData(){
		Connection conn = null;
		conn = connectToDatabaseOrDie();
		String result = null;
		int submissionId = 23;
		String firstName = "", lastName = "", mobileNo ="";
		String vin = "", make = "", model ="", year ="", mileage= "";
		String tempContent = "profile-data;";

		try{
			Statement st = conn.createStatement();
			ResultSet rs = st.executeQuery("select * from form_submission_profile_data where form_submission_id =  "+ submissionId);
			while (rs.next()){
				tempContent = tempContent +" "+ rs.getString("profile_field") + "=\"" + rs.getString("profile_value") + "\"; "; 

			}

			firstName = parseValueFromHeader(tempContent, "first_name");
			lastName = parseValueFromHeader(tempContent, "last_name"); 
			mobileNo = parseValueFromHeader(tempContent, "mobile_no");
			vin = parseValueFromHeader(tempContent, "vin"); 
			make = parseValueFromHeader(tempContent, "make"); 
			model = parseValueFromHeader(tempContent, "model"); 
			year = parseValueFromHeader(tempContent, "year"); 
			mileage= parseValueFromHeader(tempContent, "mileage");

		}
		catch (SQLException se) {
			System.err.println("Threw a SQLException creating the list of blogs.");
			System.err.println(se.getMessage());
		}


		result = firstName +"<br />"+ lastName +"<br />"+ mobileNo +"<br />"+ vin +"<br />"+ make +"<br />"+ model +"<br />"+ year +"<br />"+ mileage;
		//result = ultimateContent;
		return Response.status(200).entity(result).build();
	}

	private static String parseValueFromHeader(String header, String parameterName) {
		String parameterValue = null;
		StringTokenizer st = new StringTokenizer(header, ";");
		while (st.hasMoreTokens()) {
			String token = st.nextToken();
			if (token.contains(parameterName)) {
				int quoteIndex = token.indexOf("\"");
				parameterValue = token.substring(quoteIndex+1,token.length()-1);
				// logger.finer("parameter " + parameterName + " is "+ parameterValue);
				break;
			}
		}
		return parameterValue;
	}


	@Path("formName/{u}")
	@GET
	@Produces("application/json")
	public Response formName(@PathParam("u") int u) throws JSONException{
		Connection conn = null;
		conn = connectToDatabaseOrDie();
		JSONObject jsonObject = new JSONObject();
		String result = null;
		int tempUserId = 0;
		try{
			Statement st = conn.createStatement();
			ResultSet mrs = st.executeQuery("select user_id from user_ifs where sess_user_id = "+ u);
			if (mrs.next() ) {
				do{
					System.out.println("TEST ME");
					tempUserId = mrs.getInt("user_id");
				}while(mrs.next());

				ResultSet rs = st.executeQuery("SELECT form_name, user_form_id from user_active_form where user_id = "+ tempUserId);
				if(rs.next()){
					do{
						jsonObject.put("User ID", u);	
						jsonObject.put("form_name", rs.getString("form_name"));
						jsonObject.put("form_id", rs.getInt("user_form_id"));
					}while ( rs.next() );
				}else{
					jsonObject.put("Error", "No Form Found for this User");
				}
			}else{
				jsonObject.put("Error", "User Not Found");
			}
		}
		catch (SQLException se) {
			System.err.println("Threw a SQLException creating the list of blogs.");
			System.err.println(se.getMessage());
		}

		result = "" + jsonObject;
		return Response.status(200).entity(result).build();

	}	

	@GET
	@Produces("application/json")
	public Response convertmlstokms() throws JSONException {

		JSONObject jsonObject = new JSONObject();
		Double miles = (double) 52;
		Double kilometers;
		kilometers = miles * 1.60934; 
		jsonObject.put("Miles", miles); 
		jsonObject.put("Kilometers", kilometers);

		String result = ""+jsonObject;
		return Response.status(200).entity(result).build();
	}

	@POST
	@Path("/ptest")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response ptest(String s) throws JSONException{

		Connection conn = null;
		conn = connectToDatabaseOrDie();  

		//JSONArray tarr = new JSONArray();
		JSONObject tt = new JSONObject();
		String result = null;

		JSONObject json =  new JSONObject(s);
		JSONArray jarr = new JSONArray(json.get("components").toString());
		JSONArray parr = new JSONArray(json.get("profile").toString());
		int userID = 0;
		String formName = null;
		int formSubmissionID = 0;


		try{
			userID = json.getInt("user_id");
			formName = json.get("form_name").toString();
			if(((int)Math.log10(userID) + 1) == 0){
				tt.put("error", "User ID is null");
				result =""+tt; 
				return Response.status(200).entity(result).build();
			}else{
				try{
					Statement st = conn.createStatement();
					st.execute("Insert into form_submission_index (user_id, form_name) VALUES ("+ userID +",'"+ formName +"')");
					ResultSet rs = st.executeQuery("select last_value from form_submission_index_form_submission_id_seq");
					while ( rs.next() ){
						formSubmissionID = rs.getInt("last_value");
					}

					for(int i =0;i< jarr.length();i++){
						JSONObject jj = (JSONObject) jarr.get(i);
						String componentName = null;
						String label = null;
						String optionDescription = null;
						String positionID = null;
						componentName = jj.getString("component_name");
						label = jj.getString("label");
						optionDescription = jj.getString("option_description");
						positionID = jj.getString("position_id");
						st.execute("Insert into form_submission_component_data " +
								"(form_submission_id, user_id, position_id, component_name, label, option_description) " +
								"VALUES " +
								"("+ formSubmissionID +","+ userID +",'"+ positionID +"','"+ componentName +"','"+ label +"','"+ optionDescription +"')");
					}

					int i =0;
					JSONObject jj = (JSONObject) parr.get(i);
					Iterator<?> keys = jj.keys();
					while( keys.hasNext() ){
						String key = (String)keys.next();
						//String keyValue = jj.getString(key);
						String keyValue = jj.getString(key).replace("'", "''");
						st.execute("Insert into form_submission_profile_data " +
								"(form_submission_id, user_id, profile_field, profile_value)" +
								"VALUES" +
								"("+ formSubmissionID +","+ userID +",'"+ key +"','"+ keyValue +"')");
					}

					tt.put("submissionID", formSubmissionID);
					result =""+tt; 
					return Response.status(200).entity(result).build();
				}
				catch(Exception e){
					result =""+e; 
					return Response.status(200).entity(result).build();
				}

			}

		}
		catch (Exception e){
			e.printStackTrace();
			tt.put("error", "User ID field is not available");
			result =""+tt; 
			return Response.status(200).entity(result).build();
		}


	}


	@Path("{f}")
	@GET
	@Produces("application/json")
	public Response convertFtoCfromInput(@PathParam("f") float miles) throws JSONException {

		JSONObject jsonObject = new JSONObject();
		float kilometers;
		String name = "Innoppl Technologies";
		kilometers = (float) (miles * 1.60934); 
		jsonObject.put("Miles", miles); 
		jsonObject.put("Kilometers", kilometers);
		jsonObject.put("Author", name);

		//String result = "@Produces(\"application/json\") Output: \n\nF to C Converter Output: \n\n" + jsonObject;
		String result1 = ""+jsonObject;
		return Response.status(200).entity(result1).build();
	}

	private Connection connectToDatabaseOrDie()
	{
		Connection conn = null;
		try
		{
			Class.forName("org.postgresql.Driver");
			//String url = "jdbc:postgresql://127.0.0.1:5432/stagingassist";
			//String url = "jdbc:postgresql://127.0.0.1:5432/assistfeb15";
			//String url = "jdbc:postgresql://127.0.0.1:5432/exstagingassist";
			//String url = "jdbc:postgresql://127.0.0.1:5432/prodengine";
			//conn = DriverManager.getConnection(url,"postgres", "te$ts3rv650");
			String url = "jdbc:postgresql://127.0.0.1:5432/copystaging";
			conn = DriverManager.getConnection(url,"postgres", "psqlpass");
		}
		catch (ClassNotFoundException e)
		{
			e.printStackTrace();
			System.exit(1);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			System.exit(2);
		}
		return conn;
	}

}     
