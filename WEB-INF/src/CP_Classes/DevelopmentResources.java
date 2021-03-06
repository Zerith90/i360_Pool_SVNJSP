package CP_Classes;

import java.sql.*;
import java.util.Vector;

import CP_Classes.common.ConnectionBean;
import CP_Classes.vo.votblDRA;
import CP_Classes.vo.votblDRARES;

/**
 * This class implements all the operations for Development Resources, which is to be used in System Libraries, Survey, and Report..
 */
public class DevelopmentResources
{
	/**
	 * Declaration of new object of class Database. This object is declared private, which is to make sure that it is only accessible within this class Competency.
	 */
	//private Database db;
	
	/**
	 * Declaration of new object of class User.
	 */
	private User_Jenty U;
	
	/**
	 * Declaration of new object of class EventViewer.
	 */
	private EventViewer EV;
	
	/**
	 * Bean Variable to store the foreign key of Competency ID.
	 */
	public int FKComp;
	
	/**
	 * Bean Variable to store the primary key of DRARes for editing purpose.
	 */
	public int DRAResID;
	
	/**
	 * Bean Variable to store the DRA Resource type.
	 */
	public int ResType;
	
	/**
	 * Bean Variable for sorting purposes. Total Array depends on total SortType.
	 * 0 = ASC
	 * 1 = DESC
	 */
	public int Toggle [];	// 0=asc, 1=desc
	
	/**
	 * Bean Variable to store the Sorting type.
	 */
	public int SortType;

	/**
	 * Creates a new intance of Development Resources object.
	 */
	public DevelopmentResources() {
		//db = new Database();
		U = new User_Jenty();
		EV = new EventViewer();

		Toggle = new int [2];
		
		for(int i=0; i<2; i++)
			Toggle[i] = 0;
			
		SortType = 1;
		FKComp = 0;
		ResType = 0;
	}

	/**
	 * Store Bean Variable toggle either 1 or 0.
	 */
	public void setToggle(int toggle) {
		Toggle[SortType - 1] = toggle;
	}

	/**
	 * Get Bean Variable toggle.
	 */
	public int getToggle() {
		return Toggle [SortType - 1];
	}	

	/**
	 * Store Bean Variable DRAResID.
	 */
	public void setDRAResID(int DRAResID) {
		this.DRAResID = DRAResID;
	}

	/**
	 * Get Bean Variable DRAResID.
	 */
	public int getDRAResID() {
		return DRAResID;
	}
	
	/**
	 * Store Bean Variable Sort Type.
	 */
	public void setSortType(int SortType) {
		this.SortType = SortType;
	}

	/**
	 * Get Bean Variable SortType.
	 */
	public int getSortType() {
		return SortType;
	}	

	/**
	 * Store Bean Variable foreign key Competency ID.
	 */
	public void setFKComp(int FKComp) {
		this.FKComp = FKComp;
	}

	/**
	 * Get Bean Variable foreign key Competency ID.
	 */
	public int getFKComp() {
		return FKComp;
	}
	
	/**
	 * Store Bean Variable of Development Resources Type.
	 */
	public void setResType(int ResType) {
		this.ResType = ResType;
	}

	/**
	 * Get Bean Variable of Development Resources Type.
	 */
	public int getResType() {
		return ResType;
	}

	/**
	 * Add a new record to the Development Resources table (tblDRARes).
	 *
	 * Parameters:
	 *		CompetencyID - foreign key of Competency.
	 *		Resource 	 - the name of the resource for this particular Competency.
	 *		ResType 	 - resource type (1=book, 2=web, 3=training courses, 4=AV Resources).
	 *		companyID	 - company ID.
	 *		orgID 		 - organization ID.
	 *
	 * Returns:
	 *		a boolean that represents the success of inserting to the database.
	 */
	//Added parameter sIsSystemGenerated and related logic codes. To determine if the Development Resources to be added is system generated or user generated, Sebastian 29 July 2010
	public boolean addRecord(int CompetencyID, String Resource, int ResType, String IsSystemGenerated, int companyID, int orgID, int pkUser, int userType) throws SQLException, Exception {
		int IsSysGenerated = 0;
		boolean bIsUpdated=false;
		//if(companyID == 1 && orgID == 1)
		//Modified Code to cater to IsSystemGenerated parameter, only sa is allowed to specify if the record is system or user generated, Sebastian 29 July 2010
		if(userType == 1)//if user is sa
		{
			if (IsSystemGenerated.trim().equals("1"))
			{
				IsSysGenerated = 1;
			}
		}
		//added "N" in front of resource by alvis on 08-Sep-09 to allow chinese support
		String sql = "Insert into tblDRARes (CompetencyID, Resource, ResType, IsSystemGenerated, FKCompanyID, FKOrganizationID) values (" + CompetencyID + ",N'" +
						Resource + "'," + ResType + ", " + IsSysGenerated + ", " + companyID + ", " + orgID + ")";
		Connection con = null;
		Statement st = null;

		try{
			con=ConnectionBean.getConnection();
			st=con.createStatement();
			int iSuccess = st.executeUpdate(sql);
			
			if(iSuccess!=0)
			bIsUpdated=true;

		}catch(Exception ex){
			System.out.println(ex);
		}finally{
			ConnectionBean.closeStmt(st); //Close statement
			ConnectionBean.close(con); //Close connection

		}
		
		String [] UserInfo = U.getUserDetail(pkUser);		
		try {
			EV.addRecord("Insert", "Development Resources", Resource, UserInfo[2], UserInfo[11], UserInfo[10]);
		} catch(SQLException SE) {
			System.out.println(SE.getMessage());
		}
			
		return bIsUpdated;
	}
	
	public boolean addRecord(int CompetencyID, String[] Resource, int ResType, String IsSystemGenerated, int companyID, int orgID, int pkUser, int userType) throws SQLException, Exception {
		int IsSysGenerated = 0;
		boolean bIsUpdated=false;
		//if(companyID == 1 && orgID == 1)
		//Modified Code to cater to IsSystemGenerated parameter, only sa is allowed to specify if the record is system or user generated, Sebastian 29 July 2010
		if(userType == 1)//if user is sa
		{
			if (IsSystemGenerated.trim().equals("1"))
			{
				IsSysGenerated = 1;
			}
		}
		//added "N" in front of resource by alvis on 08-Sep-09 to allow chinese support
		String sql = "Insert into tblDRARes (CompetencyID, Resource, Resource1, Resource2, Resource3, Resource4, Resource5," +
				" ResType, IsSystemGenerated, FKCompanyID, FKOrganizationID) values (" + CompetencyID + ",N'" +Resource[0] + "', ";
		if(Resource[1].trim().length() != 0)
			sql += "N'" + Resource[1] +"', ";
		else
			sql +="null,";
		if(Resource[2].trim().length() != 0)
			sql += "N'" + Resource[2] +"', ";
		else
			sql +="null,";
		if(Resource[3].trim().length() != 0)
			sql += "N'" + Resource[3] +"', ";
		else
			sql +="null,";
		if(Resource[4].trim().length() != 0)
			sql += "N'" + Resource[4] +"', ";
		else
			sql +="null,";
		if(Resource[5].trim().length() != 0)
			sql += "N'" + Resource[5] +"'," ;
		else
			sql +="null,";
		sql += ResType + ", " + IsSysGenerated + ", " + companyID + ", " + orgID + ")";
		Connection con = null;
		Statement st = null;;
		try{
			con=ConnectionBean.getConnection();
			st=con.createStatement();
			int iSuccess = st.executeUpdate(sql);
			
			if(iSuccess!=0)
			bIsUpdated=true;

		}catch(Exception ex){
			System.out.println(sql + ex);
		}finally{
			ConnectionBean.closeStmt(st); //Close statement
			ConnectionBean.close(con); //Close connection

		}
		
		String [] UserInfo = U.getUserDetail(pkUser);		
		try {
			EV.addRecord("Insert", "Development Resources", Resource[0], UserInfo[2], UserInfo[11], UserInfo[10]);
		} catch(SQLException SE) {
			System.out.println(SE.getMessage());
		}
			
		return bIsUpdated;
	}
	
	
	/**
	 * Add a new record to the Development Resources table (tblDRARes).
	 *
	 * Parameters:
	 *		CompetencyID - foreign key of Competency.
	 *		Resource 	 - the name of the resource for this particular Competency.
	 *		ResType 	 - resource type (1=book, 2=web, 3=training courses, 4=AV Resources).
	 *		companyID	 - company ID.
	 *		orgID 		 - organization ID.
	 *		Resource     - the name of the resource in foreign language
	 *		lang - language code of foreign language : 1 Indonesian, 2 Thai, 3 Korean, 4 Traditional Chinese, 5 Simplified Chinese
	 *
	 * Returns:
	 *		a boolean that represents the success of inserting to the database.
	 */
	//Added parameter sIsSystemGenerated and related logic codes. To determine if the Development Resources to be added is system generated or user generated, Sebastian 29 July 2010
	public boolean addRecord(int CompetencyID, String Resource, String Resource2, int lang, int ResType, String IsSystemGenerated, int companyID, int orgID, int pkUser, int userType) throws SQLException, Exception {
		int IsSysGenerated = 0;
		boolean bIsUpdated=false;
		//if(companyID == 1 && orgID == 1)
		//Modified Code to cater to IsSystemGenerated parameter, only sa is allowed to specify if the record is system or user generated, Sebastian 29 July 2010
		if(userType == 1)//if user is sa
		{
			if (IsSystemGenerated.trim().equals("1"))
			{
				IsSysGenerated = 1;
			}
		}
		String sql = null;
		//added "N" in front of resource by alvis on 08-Sep-09 to allow chinese support
		if(lang == 4 || lang == 5){
			sql = "Insert into tblDRARes (CompetencyID, Resource, Resource4, Resource5, ResType, IsSystemGenerated, FKCompanyID, FKOrganizationID) values (" + CompetencyID + ",N'" +
					Resource + "', N'"+ Resource2+"', N'" + Resource2+"', " + ResType + ", " + IsSysGenerated + ", " + companyID + ", " + orgID + ")";
		}else{
			sql = "Insert into tblDRARes (CompetencyID, Resource, Resource"+lang+", ResType, IsSystemGenerated, FKCompanyID, FKOrganizationID) values (" + CompetencyID + ",N'" +
					Resource + "', N'"+Resource2+ "', "+ ResType + ", " + IsSysGenerated + ", " + companyID + ", " + orgID + ")";
		}

		Connection con = null;
		Statement st = null;

		try{
			con=ConnectionBean.getConnection();
			st=con.createStatement();
			int iSuccess = st.executeUpdate(sql);
			
			if(iSuccess!=0)
			bIsUpdated=true;

		}catch(Exception ex){
			System.out.println(ex);
		}finally{
			ConnectionBean.closeStmt(st); //Close statement
			ConnectionBean.close(con); //Close connection

		}
		
		String [] UserInfo = U.getUserDetail(pkUser);		
		try {
			EV.addRecord("Insert", "Development Resources", Resource, UserInfo[2], UserInfo[11], UserInfo[10]);
		} catch(SQLException SE) {
			System.out.println(SE.getMessage());
		}
			
		return bIsUpdated;
	}

	/**
	 * Edit a record in the Development Resources table (tblDRARes).
	 *
	 * Parameters:
	 *		CompetencyID - the foreign key of Competency to determine which record to be edited.
	 *		ResID		 - the primary key of DRARes to be edited.
	 *		Resource 	 - the Resource description.
	 *
	 * Returns:
	 *		a boolean that represents the success of editing to the database.
	 */
	public boolean editRecord(int CompetencyID, int ResID, String Resource, int pkUser) throws SQLException, Exception {
		String oldStatement = DRAResStatement(ResID);
		boolean bIsUpdated=false;
		//added "N" in front of resource by alvis on 08-Sep-09 to allow chinese support
		String sql = "Update tblDRARes Set Resource = N'" + Resource +
						"', CompetencyID = " + CompetencyID + " where ResID = " + ResID;
		Connection con = null;
		Statement st = null;

		try{
			con=ConnectionBean.getConnection();
			st=con.createStatement();
			int iSuccess = st.executeUpdate(sql);
			
			if(iSuccess!=0)
			bIsUpdated=true;

		}catch(Exception ex){
			System.out.println(ex);
		}finally{
			ConnectionBean.closeStmt(st); //Close statement
			ConnectionBean.close(con); //Close connection

		}
		String [] UserInfo = U.getUserDetail(pkUser);		
		try {
			EV.addRecord("Update", "Development Resources", "(" + oldStatement + ") - (" + Resource + ")", UserInfo[2], UserInfo[11], UserInfo[10]);
		} catch(SQLException SE) {
			System.out.println(SE.getMessage());
		}
		
		return bIsUpdated;
	}
	
	public boolean editRecord(int CompetencyID, int ResID, String[] Resource, int pkUser) throws SQLException, Exception {
		String oldStatement = DRAResStatement(ResID);
		boolean bIsUpdated=false;
		//added "N" in front of resource by alvis on 08-Sep-09 to allow chinese support
		String sql = "Update tblDRARes Set Resource = N'" + Resource[0] +"',";
		if(Resource[1].trim().length() != 0)
			sql += " Resource1 = N'" + Resource[1] + "',";
		if(Resource[2].trim().length() != 0)
			sql += " Resource2 = N'" + Resource[2] + "',";
		if(Resource[3].trim().length() != 0)
			sql += " Resource3 = N'" + Resource[3] + "',";
		if(Resource[4].trim().length() != 0)
			sql += " Resource4 = N'" + Resource[4] + "',";
		if(Resource[5].trim().length() != 0)
			sql += " Resource5 = N'" + Resource[5] + "',";
		sql += " CompetencyID = " + CompetencyID + " where ResID = " + ResID;
		Connection con = null;
		Statement st = null;

		try{
			con=ConnectionBean.getConnection();
			st=con.createStatement();
			int iSuccess = st.executeUpdate(sql);
			
			if(iSuccess!=0)
			bIsUpdated=true;

		}catch(Exception ex){
			System.out.println(ex);
		}finally{
			ConnectionBean.closeStmt(st); //Close statement
			ConnectionBean.close(con); //Close connection

		}
		String [] UserInfo = U.getUserDetail(pkUser);		
		try {
			EV.addRecord("Update", "Development Resources", "(" + oldStatement + ") - (" + Resource[0] + ")", UserInfo[2], UserInfo[11], UserInfo[10]);
		} catch(SQLException SE) {
			System.out.println(SE.getMessage());
		}
		
		return bIsUpdated;
	}

	/**
	 * Delete an existing record from the Development Resources table (tblDRARes).
	 *
	 * Parameters:
	 *		ResID - the primary key of Resource to determine which record to be deleted.
	 *
	 * Returns:
	 *		a boolean that represents the success of deletion process.
	 */
	public boolean deleteRecord(int ResID, int pkUser)  throws SQLException, Exception {
		String oldStatement = DRAResStatement(ResID);
		boolean bIsDeleted=false;
		String sql = "Delete from tblDRARes where ResID = " + ResID;
		Connection con = null;
		Statement st = null;

		try{

			con=ConnectionBean.getConnection();
			st=con.createStatement();
			int iSuccess = st.executeUpdate(sql);
			
			if(iSuccess!=0)
			bIsDeleted=true;

		}catch(Exception ex){
			System.out.println(ex);
		}finally{
			ConnectionBean.closeStmt(st); //Close statement
			ConnectionBean.close(con); //Close connection

		}
				
		String [] UserInfo = U.getUserDetail(pkUser);		
		try {
			EV.addRecord("Delete", "Development Resources", oldStatement, UserInfo[2], UserInfo[11], UserInfo[10]);
		} catch(SQLException SE) {
			System.out.println(SE.getMessage());
		}
		
		return bIsDeleted;
	}

	/**
	 * Retrieves all Develoment Resouces from tblDRARes.
	 */
	public Vector getAllRecord() throws SQLException, Exception {
		String query = "Select * from tblDRARes order by Resource";
		
		Vector v=new Vector();
		
		Connection con = null;
		Statement st = null;
		ResultSet rs = null;

		try{
			con=ConnectionBean.getConnection();
			st=con.createStatement();
			rs=st.executeQuery(query);
			while(rs.next()){
				votblDRARES vo=new votblDRARES();
				vo.setCompetencyID(rs.getInt("CompetencyID"));
				
				vo.setFKCompanyID(rs.getInt("FKCompanyID"));
				vo.setFKOrganizationID(rs.getInt("FKOrganizationID"));
				vo.setIsSystemGenerated(rs.getInt("IsSystemGenerated"));
				vo.setResID(rs.getInt("ResID"));
				vo.setResource(rs.getString("Resource"));
				vo.setResType(rs.getInt("ResType"));
				
				v.add(vo);
				
			}
			
		}catch(Exception e){
			System.out.println("DevelpmentResources.java - getAllRecord- "+e);
		}finally{
			ConnectionBean.closeRset(rs); //Close ResultSet
			ConnectionBean.closeStmt(st); //Close statement
			ConnectionBean.close(con); //Close connection

		}
		return v;
	}

	/**
	 * Retrieves all records based on competency, resource type, company, and organization.
	 *
	 * Parameters:
	 *		competency 	  - optional.
	 *		resource type - optional.
	 */
	public Vector FilterRecordByType(int FKComp, int resType, int companyID, int orgID) throws SQLException, Exception {
		String query = "";
		
		// Changed by Ha 10/06/08 change ResType-->resType
		if(FKComp != 0 && resType != 0) {
			query = query + "Select tblDRARes.ResID, tblDRARes.Resource, tblDRARes.ResType, tblOrigin.Description ";
			query = query + "from tblDRARes ";
			query = query + " inner join tblOrigin on tblDRARes.IsSystemGenerated = tblOrigin.PKIsSystemGenerated ";
			query = query + "where (CompetencyID = " + FKComp;
			query = query + " and tblDRARes.ResType = " + resType;
			query = query + " and FKCompanyID = " + companyID + " and FKOrganizationID = " + orgID;
			query = query + ") or (CompetencyID = " + FKComp;
			query = query + " and tblDRARes.ResType = " + resType;
			query = query + " and IsSystemGenerated = 1) order by ";
			
		} else if(FKComp == 0 && resType != 0) {
			query = query + "SELECT ResID, Resource, ResType, Description FROM tblDRARes ";
			query = query + " inner join tblOrigin on tblDRARes.IsSystemGenerated = tblOrigin.PKIsSystemGenerated ";
			query = query + " WHERE (ResType = " + resType;
			query = query + " and FKCompanyID = " + companyID + " and FKOrganizationID = " + orgID;
			query = query + ") or (tblDRARes.ResType = " + resType;
			query = query + " and IsSystemGenerated = 1)";
			query = query + " ORDER BY ";
			
		} else if(FKComp != 0 && resType == 0) {
			query = query + "Select tblDRARes.ResID, tblDRARes.Resource, tblDRARes.ResType, tblOrigin.Description ";
			query = query + "from tblDRARes ";
			query = query + " inner join tblOrigin on tblDRARes.IsSystemGenerated = tblOrigin.PKIsSystemGenerated ";
			query = query + "where (CompetencyID = " + FKComp;
			query = query + " and FKCompanyID = " + companyID + " and FKOrganizationID = " + orgID;
			query = query + ") or (CompetencyID = " + FKComp;
			query = query + " and IsSystemGenerated = 1) order by ";
			
		} else {
			query = query + "Select tblDRARes.ResID, tblDRARes.Resource, tblDRARes.ResType, tblOrigin.Description ";
			query = query + "from tblDRARes ";
			query = query + " inner join tblOrigin on tblDRARes.IsSystemGenerated = tblOrigin.PKIsSystemGenerated ";
			query = query + "where (FKCompanyID = " + companyID + " and FKOrganizationID = " + orgID;
			query = query + ") or (IsSystemGenerated = 1) order by ";
		}
		
		if(SortType == 1)
			query = query + "tblDRARes.Resource";
		else
			query = query + "IsSystemGenerated";

		if(Toggle[SortType - 1] == 1)
			query = query + " DESC";
		
		Vector v=new Vector();
		
		Connection con = null;
		Statement st = null;
		ResultSet rs = null;

		try{
			con=ConnectionBean.getConnection();
			st=con.createStatement();
			rs=st.executeQuery(query);
			while(rs.next()){
				//tblDRARes.ResID, tblDRARes.Resource, tblDRARes.ResType, tblOrigin.Description 
				votblDRARES vo=new votblDRARES();
				
				vo.setResID(rs.getInt("ResID"));
				vo.setResource(rs.getString("Resource"));
				vo.setResType(rs.getInt("ResType"));
				vo.setDescription(rs.getString("Description"));
				v.add(vo);
				
			}
			
		}catch(Exception e){
			System.out.println("DevelpmentResources.java - FilterRecordByType - "+e);
		}finally{
			ConnectionBean.closeRset(rs); //Close ResultSet
			ConnectionBean.closeStmt(st); //Close statement
			ConnectionBean.close(con); //Close connection

		}
		System.out.println("QUERY: "+query);
		return v;
	}

	/**
	 * Get total DRA Resource in the table.
	 */
	public int getTotalRecord() throws SQLException, Exception {
		String query = "Select count(*) tblDRARes";
		Connection con = null;
		Statement st = null;
		ResultSet rs = null;
		int record=0;

		try{
			con=ConnectionBean.getConnection();
			st=con.createStatement();
			rs=st.executeQuery(query);
			if(rs.next()){
				record=rs.getInt(1);
			}
			
		}catch(Exception e){
			System.out.println("DevelpmentResources.java - getTotalRecord - "+e);
		}finally{
			ConnectionBean.closeRset(rs); //Close ResultSet
			ConnectionBean.closeStmt(st); //Close statement
			ConnectionBean.close(con); //Close connection

		}
		

		return record;
	}
	
	/**
	 * Check the existance of the particular Competency in the database.
	 * Returns: 0 = NOT Exist
	 *		    1 = Exist
	 */
	public int CheckCompetencyExist(int pkComp) throws SQLException, Exception {
		int exist = 0;

		String query = "Select * from tblDRARes where CompetencyID = " + pkComp;
		Connection con = null;
		Statement st = null;
		ResultSet rs = null;
	

		try{
			con=ConnectionBean.getConnection();
			st=con.createStatement();
			rs=st.executeQuery(query);

			
			if(rs.next())
				exist = 1;
			
		}catch(Exception e){
			System.out.println("DevelpmentResources.java - CheckCompetencyExist - "+e);
		}finally{
			ConnectionBean.closeRset(rs); //Close ResultSet
			ConnectionBean.closeStmt(st); //Close statement
			ConnectionBean.close(con); //Close connection

		}
		

		return exist;
	}
	
	/**
	 * Check the existance of the particular DRA Resource in the database.
	 * Returns: 0 = NOT Exist
	 *		    Else return the ID of DRA Resource ( changed by Ha 11/06/08)
	 */
	public int CheckDRAResExist(int CompID, String DRARes, int resType, int compID, int orgID) throws SQLException, Exception {
		int pkComp = 0;
		
		String query = "SELECT * FROM tblDRARes  ";
		query = query + "WHERE CompetencyID = " + CompID + " AND ";
		query = query + "Resource = '" + DRARes + "' and ResType = " + resType;
		query = query + " and ((FKCompanyID = " + compID + " and FKOrganizationID = " + orgID;
		query = query + ") or (IsSystemGenerated = 1))";

		Connection con = null;
		Statement st = null;
		ResultSet rs = null;
	

		try{
			con=ConnectionBean.getConnection();
			st=con.createStatement();
			rs=st.executeQuery(query);

			
			if(rs.next())
				pkComp = rs.getInt("ResID");
			
		}catch(Exception e){
			System.out.println("DevelpmentResources.java - CheckDRAResExist - "+e);
		}finally{
			ConnectionBean.closeRset(rs); //Close ResultSet
			ConnectionBean.closeStmt(st); //Close statement
			ConnectionBean.close(con); //Close connection

		}
		
		
			
		return pkComp;
	}
	
	/**
	 * Check whether the DRA Resource belonged to System Generated of User Generated.
	 */
	public int CheckSysLibDRARes(int ResID) throws SQLException, Exception {
		int pkDRA = 0;
		
		String query = "SELECT IsSystemGenerated FROM tblDRARes  ";
		query = query + "WHERE ResID = " + ResID;
		Connection con = null;
		Statement st = null;
		ResultSet rs = null;
	

		try{
			con=ConnectionBean.getConnection();
			st=con.createStatement();
			rs=st.executeQuery(query);

			if(rs.next())
				pkDRA = rs.getInt(1);
			
		}catch(Exception e){
			System.out.println("DevelpmentResources.java - CheckSysLibDRARes - "+e);
		}finally{
			ConnectionBean.closeRset(rs); //Close ResultSet
			ConnectionBean.closeStmt(st); //Close statement
			ConnectionBean.close(con); //Close connection

		}			
		
			
		return pkDRA;
	}		
	
	/**
	 * Retrieve all the Development Resources under the particular Competency.
	 */
	public Vector getRecord(int pkComp, int compID, int orgID) throws SQLException, Exception {
		String query = "SELECT * from tblDRARes where (CompetencyID = " + pkComp;
		query = query + " and IsSystemGenerated = 1)";
		query = query + " or (CompetencyID = " + pkComp + " and FKCompanyID = " + compID;
		query = query + " and FKOrganizationID = " + orgID + ")";
		Vector v=new Vector();
		
		Connection con = null;
		Statement st = null;
		ResultSet rs = null;

		try{
			con=ConnectionBean.getConnection();
			st=con.createStatement();
			rs=st.executeQuery(query);
			while(rs.next()){
				votblDRARES vo=new votblDRARES();
				vo.setCompetencyID(rs.getInt("CompetencyID"));
				
				vo.setFKCompanyID(rs.getInt("FKCompanyID"));
				vo.setFKOrganizationID(rs.getInt("FKOrganizationID"));
				vo.setIsSystemGenerated(rs.getInt("IsSystemGenerated"));
				vo.setResID(rs.getInt("ResID"));
				vo.setResource(rs.getString("Resource"));
				vo.setResType(rs.getInt("ResType"));
				
				v.add(vo);
				
			}
			
		}catch(Exception e){
			System.out.println("DevelpmentResources.java - getRecord- "+e);
		}finally{
			ConnectionBean.closeRset(rs); //Close ResultSet
			ConnectionBean.closeStmt(st); //Close statement
			ConnectionBean.close(con); //Close connection

		}
		return v;
	}
	
	/**
	 * Get the Development Resources Statement description based on DRARes ID.
	 */
	public String DRAResStatement(int DRAResID) throws SQLException, Exception {
		String desc = "";
		
		String query = "SELECT * from tblDRARes where ResID = " + DRAResID;
		Connection con = null;
		Statement st = null;
		ResultSet rs = null;
	

		try{
			con=ConnectionBean.getConnection();
			st=con.createStatement();
			rs=st.executeQuery(query);
			if(rs.next())
				desc = rs.getString("Resource");
			
			
		}catch(Exception e){
			System.out.println("DevelpmentResources.java - DRAResStatement - "+e);
		}finally{
			ConnectionBean.closeRset(rs); //Close ResultSet
			ConnectionBean.closeStmt(st); //Close statement
			ConnectionBean.close(con); //Close connection

		}			
		
		
		return desc;
	}
	
	/**	(31-01-04) Maruli
	 *	For Import DRA purpose
	 *	Check whether Resource already exist in database
	 */
	public int checkExist(int CompID, String Resource, int ResType, int CompanyID, int OrgID) throws SQLException, Exception {
		int draID = 0;
		
		String query = "SELECT ResID FROM tblDRARes WHERE (CompetencyID = " + CompID + ") AND ";
		query = query + "(Resource LIKE '" + Resource + "') AND (ResType = " + ResType + ") AND ";
		query = query + "(FKCompanyID = " + CompanyID + ") AND (FKOrganizationID = " + OrgID + ") ";
		
		Connection con = null;
		Statement st = null;
		ResultSet rs = null;
	

		try{
			con=ConnectionBean.getConnection();
			st=con.createStatement();
			rs=st.executeQuery(query);
			
			if (rs.next())
				draID = rs.getInt("ResID");
			
			
		}catch(Exception e){
			System.out.println("DevelpmentResources.java - checkExist - "+e);
		}finally{
			ConnectionBean.closeRset(rs); //Close ResultSet
			ConnectionBean.closeStmt(st); //Close statement
			ConnectionBean.close(con); //Close connection

		}			
		
	
		
		return draID;
	}
	
	/** (31-01-04) Maruli
	 *	For Import DRA RES purpose
	 *	Add records from excel sheet into database (Sheet 1)
	 */
	public boolean importRecord(int CompetencyID, String Resource, int ResType, int companyID, int orgID) throws SQLException, Exception {
		Connection con = null;
		Statement st = null;
		ResultSet rs = null;
		boolean bIsUpdated=false;
		try{
			int IsSysGenerated = 1;
		
			String sql = "Insert into tblDRARes (CompetencyID, Resource, ResType, IsSystemGenerated, FKCompanyID, FKOrganizationID) values (";
			//added "N" in front of resource by alvis on 08-Sep-09 to allow chinese support
			sql = sql + CompetencyID + ", N'" + Resource + "', " + ResType + ", " + IsSysGenerated + ", " + companyID + ", " + orgID + ")";
			
			
			
				con=ConnectionBean.getConnection();
				st=con.createStatement();
				int iSuccess = st.executeUpdate(sql);
				
				if(iSuccess!=0)
				bIsUpdated=true;

				
			
		
		} catch(Exception e){
			System.out.println("DevelpmentResources.java - importRecord - "+e);
		}finally{
		
			ConnectionBean.closeStmt(st); //Close statement
			ConnectionBean.close(con); //Close connection

		}			
		
		return	bIsUpdated;	
	}
	
	/** (31-01-04) Maruli
	 *	For Import DRA RES purpose
	 *	Delete records from database based on data in excel sheet (Sheet 2)
	 */
	public boolean importDeleteRecord(int CompetencyID, String Resource, int ResType, int companyID, int orgID) throws SQLException, Exception {

		Connection con = null;
		Statement st = null;
		boolean bIsDeleted=false;

		try{
			String sql = "Delete FROM tblDRARes WHERE CompetencyID = " + CompetencyID + " AND Resource = '" + Resource + "' ";
			sql = sql + " AND ResType = " + ResType + " AND FKCompanyID = " + companyID + " AND FKOrganizationID = " + orgID + " ";
			con=ConnectionBean.getConnection();
			st=con.createStatement();
			int iSuccess = st.executeUpdate(sql);
			
			if(iSuccess!=0)
			bIsDeleted=true;
	
		
		} catch(SQLException SE) {
			System.out.println(SE.getMessage());
		}finally{
			ConnectionBean.closeStmt(st); //Close statement
			ConnectionBean.close(con); //Close connection
		}			
		
		
		return bIsDeleted;	
	}
			
	public votblDRARES getRecord(int iResID) throws SQLException, Exception {
		//String query = "Select * from tblDRARes order by Resource";
		
		String query = "Select * from tblDRARes where ResID = " + iResID;
		votblDRARES vo=new votblDRARES();
		
		Connection con = null;
		Statement st = null;
		ResultSet rs = null;

		try{
			con=ConnectionBean.getConnection();
			st=con.createStatement();
			rs=st.executeQuery(query);
			if(rs.next()){
				
				
				vo.setCompetencyID(rs.getInt("CompetencyID"));
				
				vo.setFKCompanyID(rs.getInt("FKCompanyID"));
				vo.setFKOrganizationID(rs.getInt("FKOrganizationID"));
				vo.setIsSystemGenerated(rs.getInt("IsSystemGenerated"));
				vo.setResID(rs.getInt("ResID"));
				vo.setResource(rs.getString("Resource"));
				for(int i = 1; i < 6; i++){
					vo.setResource(i, rs.getString("Resource"+i));
				}
				vo.setResType(rs.getInt("ResType"));
				
			
				
			}
			
		}catch(Exception e){
			System.out.println("DevelpmentResources.java - getRecord- "+e);
		}finally{
			ConnectionBean.closeRset(rs); //Close ResultSet
			ConnectionBean.closeStmt(st); //Close statement
			ConnectionBean.close(con); //Close connection

		}
		return vo;
	}

	/**
	 * Retrieve all the development resources based on the Competency ID, Resource Type and TargetLogin ID
	 * @param iFKCompetency
	 * @param iResType
	 * @param iTargetLoginID
	 * @return
	 */
	public Vector getDevelopmentResources(int iFKCompetency, int iResType, int iTargetLoginID) {
		Vector v = new Vector();	
		
		Connection con = null;
		Statement st = null;
		ResultSet rs = null;

		try {
			String sql = "SELECT * FROM tblDRARes WHERE ResType = "+iResType+" AND CompetencyID = "+iFKCompetency;
			sql += " AND ResID NOT IN (Select ResID From tblDevelopmentPlan WHERE TargetLoginID = "+iTargetLoginID+") ORDER BY Resource";
			
			con=ConnectionBean.getConnection();
			st=con.createStatement();
			rs=st.executeQuery(sql);
			
			while(rs.next())	
			{	
				String Resource = rs.getString("Resource");
				String PKResource = rs.getString("ResID");
				String [] arr = {PKResource, Resource};
				v.add(arr);
			}
		}catch(Exception e){
			System.out.println("DevelpmentResources.java - getRecord- "+e);
		}finally{
			ConnectionBean.closeRset(rs); //Close ResultSet
			ConnectionBean.closeStmt(st); //Close statement
			ConnectionBean.close(con); //Close connection

		}
		
		return v;
	}
	
}