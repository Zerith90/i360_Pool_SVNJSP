package CP_Classes;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import CP_Classes.common.ConnectionBean;
import CP_Classes.vo.voCluster;
import CP_Classes.vo.voCompetency;
import CP_Classes.vo.voKeyBehaviour;
import CP_Classes.vo.voRatingResult;
import CP_Classes.vo.voUser;
import CP_Classes.vo.votblAssignment;
import CP_Classes.vo.votblRelationHigh;
import CP_Classes.vo.votblScaleValue;
import CP_Classes.vo.votblSurveyRating;
import CP_Classes.Translate;
import CP_Classes.AdditionalQuestionController;
import CP_Classes.AdditionalQuestion;
import CP_Classes.PrelimQuestionController;
import CP_Classes.PrelimQuestion;

import com.sun.star.beans.XPropertySet;
import com.sun.star.chart.XChartDocument;
import com.sun.star.container.XIndexAccess;
import com.sun.star.document.XEmbeddedObjectSupplier;
import com.sun.star.drawing.XShape;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.sheet.XSpreadsheet;
import com.sun.star.table.XTableChart;
import com.sun.star.table.XTableCharts;
import com.sun.star.table.XTableChartsSupplier;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XInterface;

/**
 * This class implements all the operations for Individual Report in Excel.
 * It implements OpenOffice API.
 */

/*
 *CHANGE LOG
 *====================================================================================================================
 *	Date		Modified by			Method(s)							Reason
 *====================================================================================================================
 *	27/06/2012	Albert				SurveyInfo()						modified so that it includes useCluster info
 *									Replacement()						add cluster info at the report front page
 *									ClusterByName()						create method to retrieve all clusters in a particular survey
 *									Cluster()							create method to retrieve all clusters in a particular survey with sorting
 *									ClusterCompetencyByName()			create method to retrieve all competencies in a particular survey and cluster
 *									ClusterCompetency()					create method to retrieve all competencies in a particular survey and cluster with sorting
 *
 * 	28/06/2012	Albert				ClusterKBList()						create method to retrieve all KBs in a particular cluster and competency
 * 									totalCluster()						create method to return total number of cluster in a particular survey
 * 									insertGapCluster()					create method to generate Gap in the report using cluster
 * 									Report()							modify so that it calls cluster methods if cluster is used
 * 	
 * 	29/06/2012	Albert				insertClusterCompetency()			create method to generate report that include cluster in the competency report
 * 
 * 	02/07/2012	Albert				InsertClusterBlindSpotAnalysis()	create method to generate report that include cluster in the blind spot analysis
 * 
 * 	06/07/2012	Albert				ClusterListHaveComments()			create method to retrieve cluster list that have rater's comments
 * 									ClusterCompListHaveComments()		create method to retrieve competency list under particular cluster that have rater's comments
 * 									insertClusterComments()				create method to generate report that include cluster in the comments section
 * 
 * 	10/07/2012	Albert				insertProfileLegend()				modified so it printed legend for CPR breakdown if it is ticked
 * 
 * 	11/07/2012	Albert				drawChartBreakCPR()					add this method to draw chart when CPR breakdown is chosen
 * 									prepareCellsBreakCPR()				add this method to accomodate drawing charts with breakdown of CPR
 * 									insertCompetency()					modified method so it accommodates for inserting competency when CPR breakdown is chosen
 * 
 * 12/07/2012	Albert				insertClusterCompetency()			modified method so it accommodates for inserting competency when CPR breakdown is chosen	
 *
 * 16/07/2012   Liu Taichen         private int format                  created this field to allow outputing reports of different format
 *
 * 16/07/2012   Liu Taichen    		Report(int, int, int, String, int,  Allows file to be stored as pdf
 * 									String, String, String, 
 * 									String, int, String)     
 *
 * 16/07/2012 	Liu Taichen			InsertRatingScale()                 print the rating scales in the individual report
 *
 * 17/07/2012   Liu Taichen 		InsertRatingScaleList()              print the rating scales in the individual report as a list
 *
 * 17/07/2012   Liu Taichen         Report(int, int, int, String, int,   use InsertRatingScaleList() instead of InsertRatingScale()
 * 									String, String, String, 
 * 									String, int, String)
 * 
 * 24/08/2012   Albert 				InsertCompetency()              	modify to cater the addition of additional raters
 * 									InsertClusterCompetency()
 * 									prepareCells(), prepareCellsBreakCPR()
 */

/*****
 * 
 * Edited By Roger 13 June 2008 Add additional orgId when calling sendMail
 * 
 */
public class SPFIndividualReport {
	private NumberFormat			formatter				= new DecimalFormat(
																	"#0.00");
	private Calculation				C;
	private Questionnaire			Q;
	private OpenOffice				OO;
	private GlobalFunc				G;
	private SurveyResult			SR;
	private RaterRelation			RR;
	private ExcelQuestionnaire		EQ;
	private MailHTMLStd				EMAIL;
	private Setting					ST;
	private RatingScale				rscale;
	private Translate				trans					= new Translate();

	private Vector					vGapSorted;								// this
																				// is
																				// to
																				// store
																				// the
																				// gap
																				// of
																				// each
																				// competency
																				// so
																				// does
																				// not
																				// need
																				// to
																				// reopen
																				// another
																				// resultset
	private Vector					vGapUnsorted;
	private Vector					vCompID;
	private Vector					vCompName;
	private Vector					vCPValues;									// add
																				// to
																				// store
																				// CP
																				// values
																				// of
																				// each
																				// competency
																				// for
																				// sorting
																				// ,
																				// Mark
																				// Oei
																				// 16
																				// April
																				// 2010

	// These 4 vectors below are for Development Map
	private Vector					Q1						= new Vector();
	private Vector					Q2						= new Vector();
	private Vector					Q3						= new Vector();
	private Vector					Q4						= new Vector();

	private int						surveyID;
	private int						targetID;
	private int						iCancel					= 0;				// If
																				// user
																				// cancelled
																				// the
																				// printing
																				// process.
																				// 0=Not
																				// cancelled,
																				// 1=Cancelled
	private String					surveyInfo[];
	private int						arrN[];									// To
																				// print
																				// N
																				// (No
																				// of
																				// Raters)
																				// for
																				// Simplified
																				// report

	private final int				BGCOLOR					= 12632256;
	private final int				BGCOLORCLUSTER			= 16774400;
	private final int				ROWHEIGHT				= 560;

	private XMultiComponentFactory	xRemoteServiceManager	= null;
	private XComponent				xDoc					= null;
	private XSpreadsheet			xSpreadsheet0			= null;
	private XSpreadsheet			xSpreadsheet			= null;
	private XSpreadsheet			xSpreadsheet2			= null;
	private XSpreadsheet			xSpreadsheet3			= null;
	private XSpreadsheet			xSpreadsheet4			= null;
	private XSpreadsheet			xSpreadsheet5			= null;			// used
																				// for
																				// item
																				// frequency
																				// spreadsheet
	private String					storeURL;

	private int						row;
	private int						rowFreq;
	private int						groupRankingTableRow	= 0;
	private int						column;
	private int						columnFreq;
	private int						startColumn;
	private int						endColumn;
	private int						startColumnFreq;
	private int						endColumnFreq;
	private int						iReportType;								// 1=Simplified
																				// Report
																				// "No Competencies charts",
																				// 2=Standard
																				// Report
	private int						iNoCPR					= 1;				// 0=CPR
																				// is
																				// chosen
																				// for
																				// survey,
																				// 1=No
																				// CPR
																				// chosen
																				// for
																				// survey
	private int						totalColumn				= 12;
	private int						totalColumnFreq			= 24;

	private int						splitOthers				= 0;				// 0="Others"
																				// 1="Subordinates"
																				// and
																				// "Peers"
	private int						CPRorFPR				= 1;				// 1=CPR,
																				// 2=FPR
	private int						breakCPR				= 0;				// 0=do
																				// not
																				// break,
																				// 1
																				// breakdown
																				// CPR

	private int						lastPageRowCount		= 0;				// For
																				// keeping
																				// a
																				// record
																				// of
																				// the
																				// last
																				// page
																				// row
																				// count
	private boolean					isGroupCPLine			= false;			// For
																				// displaying/not
																				// displaying
																				// Group
																				// CP
																				// Line
	private boolean					weightedAverage			= false;
	private boolean					templateNameSPF			= false;

	private String					language				= "";				// To
																				// track
																				// the
																				// current
																				// language
	private int						templateLanguage		= 0;
	private int						format					= 0;				// To
	private boolean					combineDIRIDR			= false;			// track
	// the
	// format
	// to
	// use

	DecimalFormat					df						= new DecimalFormat(
																	"#.##");

	/**
	 * Creates a new instance of IndividualReport object.
	 */
	public SPFIndividualReport() {
		ST = new Setting();
		C = new Calculation();
		Q = new Questionnaire();
		OO = new OpenOffice();
		G = new GlobalFunc();
		SR = new SurveyResult();
		RR = new RaterRelation();
		EQ = new ExcelQuestionnaire();
		ST = new Setting();
		EMAIL = new MailHTMLStd();
		rscale = new RatingScale();

		vGapSorted = new Vector();
		vGapUnsorted = new Vector();
		vCompID = new Vector();
		vCompName = new Vector();
		vCPValues = new Vector(); // instantiate vCPValues object, Mark Oei 16
									// April 2010

		startColumn = 0;
		endColumn = 12;
		startColumnFreq = 0;
		endColumnFreq = 24;
	}

	public String[] groupSurveyInfo() throws SQLException {
		String[] info = new String[7];

		String query = "SELECT tblSurvey.LevelOfSurvey, tblJobPosition.JobPosition, tblSurvey.AnalysisDate, ";
		query = query
				+ "tblOrganization.NameSequence, tblSurvey.SurveyName, tblOrganization.OrganizationName, tblOrganization.OrganizationLogo FROM tblSurvey INNER JOIN ";
		query = query
				+ "tblJobPosition ON tblSurvey.JobPositionID = tblJobPosition.JobPositionID INNER JOIN ";
		query = query
				+ "tblOrganization ON tblSurvey.FKOrganization = tblOrganization.PKOrganization ";
		query = query + "WHERE tblSurvey.SurveyID = " + surveyID;

		Connection con = null;
		Statement st = null;
		ResultSet rs = null;

		try {
			con = ConnectionBean.getConnection();
			st = con.createStatement();
			rs = st.executeQuery(query);

			if (rs.next()) {
				for (int i = 0; i < 7; i++)
					info[i] = rs.getString(i + 1);
			}

		} catch (Exception ex) {
			System.out.println("GroupReport.java - SurveyInfo - "
					+ ex.getMessage());
		} finally {
			ConnectionBean.closeRset(rs); // Close ResultSet
			ConnectionBean.closeStmt(st); // Close statement
			ConnectionBean.close(con); // Close connection
		}

		return info;

	}

	/**
	 * Retrieves the survey details and stores in an array. Modified by Albert
	 * (27 June 2012) to include useCluster
	 */
	public String[] SurveyInfo() throws SQLException {
		String[] info = new String[10];

		String query = "SELECT tblSurvey.LevelOfSurvey, tblJobPosition.JobPosition, tblSurvey.AnalysisDate, ";
		query = query
				+ "[User].FamilyName, [User].GivenName, tblOrganization.NameSequence, tblSurvey.SurveyName, ";
		query = query
				+ "tblOrganization.OrganizationName, tblOrganization.OrganizationLogo , tblSurvey.useCluster FROM ";
		query = query + "tblSurvey INNER JOIN tblJobPosition ON ";
		query = query
				+ "tblSurvey.JobPositionID = tblJobPosition.JobPositionID INNER JOIN ";
		query = query
				+ "tblAssignment ON tblSurvey.SurveyID = tblAssignment.SurveyID INNER JOIN ";
		query = query
				+ "[User] ON tblAssignment.TargetLoginID = [User].PKUser INNER JOIN ";
		query = query
				+ "tblOrganization ON tblSurvey.FKOrganization = tblOrganization.PKOrganization ";
		query = query + "WHERE tblSurvey.SurveyID = " + surveyID;
		query = query + " AND tblAssignment.TargetLoginID = " + targetID;

		Connection con = null;
		Statement st = null;
		ResultSet rs = null;

		try {
			con = ConnectionBean.getConnection();
			st = con.createStatement();
			rs = st.executeQuery(query);

			if (rs.next()) {
				for (int i = 0; i < 10; i++) {
					info[i] = rs.getString(i + 1);

				}
			}

		} catch (Exception ex) {
			System.out.println("SPFIndividualReport.java - SurveyInfo - "
					+ ex.getMessage());
		} finally {
			ConnectionBean.closeRset(rs); // Close ResultSet
			ConnectionBean.closeStmt(st); // Close statement
			ConnectionBean.close(con); // Close connection
		}

		return info;
	}

	/**
	 * Initializes all processes dealing with Survey.
	 */
	public void InitializeSurvey(int surveyID, int targetID, String fileName)
			throws SQLException, IOException {
		// System.out.println("Initialize Survey");

		column = 0;
		this.surveyID = surveyID;
		this.targetID = targetID;

		surveyInfo = new String[10];
		surveyInfo = SurveyInfo();

		// System.out.println("Initialize Survey Completed");
	}

	/**
	 * Get the username based on the name sequence.
	 */
	public String UserName() {
		String name = "";

		int nameSeq = Integer.parseInt(surveyInfo[5]); // 0=familyname first

		String familyName = surveyInfo[3];
		String GivenName = surveyInfo[4];

		if (nameSeq == 0)
			name = familyName + " " + GivenName;
		else
			name = GivenName + " " + familyName;

		return name;
	}

	/**
	 * Retrieves clusters under the surveyID.
	 */
	public Vector ClusterByName() throws SQLException {
		String query = "";
		Vector v = new Vector();

		query = query
				+ "SELECT tblSurveyCluster.ClusterID, Cluster.ClusterName ";
		query = query + "FROM tblSurveyCluster INNER JOIN Cluster ON ";
		query = query + "tblSurveyCluster.ClusterID = Cluster.PKCluster ";
		query = query + "WHERE tblSurveyCluster.SurveyID = " + surveyID;
		query = query + " ORDER BY tblSurveyCluster.ClusterID";

		Connection con = null;
		Statement st = null;
		ResultSet rs = null;

		try {
			con = ConnectionBean.getConnection();
			st = con.createStatement();
			rs = st.executeQuery(query);

			while (rs.next()) {
				voCluster vo = new voCluster();
				vo.setClusterID(rs.getInt("ClusterID"));
				vo.setClusterName(rs.getString("ClusterName"));
				v.add(vo);
			}

		} catch (Exception ex) {
			System.out.println("SPFIndividualReport.java - ClusterByName - "
					+ ex.getMessage());
		} finally {
			ConnectionBean.closeRset(rs); // Close ResultSet
			ConnectionBean.closeStmt(st); // Close statement
			ConnectionBean.close(con); // Close connection
		}

		return v;
	}

	/**
	 * Retrieves clusters under the surveyID.
	 * 
	 * @param int iOrder 0 = Ascending, 1 = Descending
	 * 
	 * @return Vector Cluster
	 */
	public Vector Cluster(int iOrder) throws SQLException {
		String query = "";

		Vector v = new Vector();

		query = query
				+ "SELECT tblSurveyCluster.ClusterID, Cluster.ClusterName ";
		query = query + "FROM tblSurveyCluster INNER JOIN Cluster ON ";
		query = query + "tblSurveyCluster.ClusterID = Cluster.PKCluster ";
		query = query + "WHERE tblSurveyCluster.SurveyID = " + surveyID;
		query = query + " ORDER BY Cluster.ClusterName";

		if (iOrder == 0)
			query = query + " ORDER BY Cluster.ClusterName";
		else
			query = query + " ORDER BY Cluster.ClusterName DESC";

		Connection con = null;
		Statement st = null;
		ResultSet rs = null;

		try {
			con = ConnectionBean.getConnection();
			st = con.createStatement();
			rs = st.executeQuery(query);

			while (rs.next()) {
				voCluster vo = new voCluster();
				vo.setClusterID(rs.getInt("ClusterID"));
				vo.setClusterName(rs.getString("ClusterName"));
				v.add(vo);
			}

		} catch (Exception ex) {
			System.out.println("SPFIndividualReport.java - Cluster - "
					+ ex.getMessage());
		} finally {
			ConnectionBean.closeRset(rs); // Close ResultSet
			ConnectionBean.closeStmt(st); // Close statement
			ConnectionBean.close(con); // Close connection
		}

		return v;
	}

	/**
	 * Retrieves competencies under the surveyID and clusterID.
	 */
	public Vector ClusterCompetencyByName(int clusterID) throws SQLException {
		String query = "";
		int surveyLevel = Integer.parseInt(surveyInfo[0]);
		Vector v = new Vector();

		if (surveyLevel == 0) {
			query = query
					+ "SELECT Cluster.ClusterName, tblSurveyCompetency.CompetencyID, Competency.CompetencyName, ";
			query = query
					+ "CompetencyDefinition FROM tblSurveyCompetency INNER JOIN Competency ON ";
			query = query
					+ "tblSurveyCompetency.CompetencyID = Competency.PKCompetency INNER JOIN Cluster ON Cluster.PKCluster = tblSurveyCompetency.ClusterID ";
			query = query + "WHERE tblSurveyCompetency.SurveyID = " + surveyID
					+ " AND tblSurveyCompetency.ClusterID = " + clusterID;
			query = query
					+ " ORDER BY Cluster.ClusterName, Competency.CompetencyName";

		} else {

			query = query
					+ "SELECT DISTINCT Cluster.ClusterName, tblSurveyBehaviour.CompetencyID, Competency.CompetencyName, ";
			query = query
					+ "Competency.CompetencyDefinition FROM Competency INNER JOIN ";
			query = query
					+ "tblSurveyBehaviour ON Competency.PKCompetency = tblSurveyBehaviour.CompetencyID ";
			query = query
					+ "INNER JOIN Cluster ON Cluster.PKCluster = tblSurveyBehaviour.ClusterID ";
			query = query + "WHERE tblSurveyBehaviour.SurveyID = " + surveyID
					+ "AND tblSurveyBehaviour.ClusterID = " + clusterID;
			query = query
					+ " ORDER BY Cluster.ClusterName, Competency.CompetencyName";
		}

		Connection con = null;
		Statement st = null;
		ResultSet rs = null;

		try {
			con = ConnectionBean.getConnection();
			st = con.createStatement();
			rs = st.executeQuery(query);

			while (rs.next()) {
				voCompetency vo = new voCompetency();
				int compId = rs.getInt("CompetencyID");
				String compName = rs.getString("CompetencyName");
				String compDef = rs.getString("CompetencyDefinition");

				if (compName.equals("Performing as a Team")) {
					int tempId = compId;
					String tempName = compName;
					String tempDef = compDef;
					if (rs.next()) {
						vo.setCompetencyID(rs.getInt("CompetencyID"));
						vo.setCompetencyName(rs.getString("CompetencyName"));
						vo.setCompetencyDefinition(rs
								.getString("CompetencyDefinition"));
					}
					voCompetency vo1 = new voCompetency();
					vo1.setCompetencyID(tempId);
					vo1.setCompetencyName(tempName);
					vo1.setCompetencyDefinition(tempDef);
					v.add(vo);
					v.add(vo1);

					break;
				}

				if (compName.equals("Communicating Effectively")) {
					int tempId = compId; // this is for communicating
											// effectively
					String tempName = compName;
					String tempDef = compDef;
					voCompetency vo1 = new voCompetency();
					voCompetency vo2 = new voCompetency();
					vo1.setCompetencyID(tempId);
					vo1.setCompetencyName(tempName);
					vo1.setCompetencyDefinition(tempDef);
					if (rs.next()) {
						compId = rs.getInt("CompetencyID"); // for building
															// partnerships
						compName = rs.getString("CompetencyName");
						compDef = rs.getString("CompetencyDefinition");
						vo2.setCompetencyID(compId);
						vo2.setCompetencyName(compName);
						vo2.setCompetencyDefinition(compDef);
					}
					if (rs.next()) {
						vo.setCompetencyID(rs.getInt("CompetencyID")); // for
																		// using
																		// heartskills
						vo.setCompetencyName(rs.getString("CompetencyName"));
						vo.setCompetencyDefinition(rs
								.getString("CompetencyDefinition"));
						v.add(vo);
					}
					v.add(vo1);
					v.add(vo2);
					break;
				}

				vo.setCompetencyID(compId);
				vo.setCompetencyName(compName);
				vo.setCompetencyDefinition(compDef);
				v.add(vo);

			}

		} catch (Exception ex) {
			System.out
					.println("SPFIndividualReport.java - ClusterCompetencyByName - "
							+ ex.getMessage());
		} finally {
			ConnectionBean.closeRset(rs); // Close ResultSet
			ConnectionBean.closeStmt(st); // Close statement
			ConnectionBean.close(con); // Close connection
		}

		return v;
	}

	/**
	 * Retrieves competencies under the surveyID.
	 * 
	 * @param int iOrder 0 = Ascending, 1 = Descending
	 * 
	 * @return ResultSet Competency
	 */
	public Vector ClusterCompetency(int iOrder, int clusterID)
			throws SQLException {
		String query = "";
		int surveyLevel = Integer.parseInt(surveyInfo[0]);

		Vector v = new Vector();

		if (surveyLevel == 0) {
			query = query
					+ "SELECT Cluster.ClusterName, tblSurveyCompetency.CompetencyID, Competency.CompetencyName, ";
			query = query
					+ "CompetencyDefinition FROM tblSurveyCompetency INNER JOIN Competency ON ";
			query = query
					+ "tblSurveyCompetency.CompetencyID = Competency.PKCompetency INNER JOIN Cluster ON Cluster.PKCluster = tblSurveyCompetency.ClusterID";
			query = query + "WHERE tblSurveyCompetency.SurveyID = " + surveyID
					+ " AND tblSurveyCompetency.ClusterID = " + clusterID;

			// Changed by HA 07/07/08 Order should be by Competency Name instead
			// of CompetencyID
			if (iOrder == 0)
				query = query
						+ " ORDER BY Cluster.ClusterName, Competency.CompetencyName";
			else
				query = query
						+ " ORDER BY Cluster.ClusterName, Competency.CompetencyName DESC";

		} else {

			query = query
					+ "SELECT DISTINCT Cluster.ClusterName, tblSurveyBehaviour.CompetencyID, Competency.CompetencyName, ";
			query = query
					+ "Competency.CompetencyDefinition FROM Competency INNER JOIN ";
			query = query
					+ "tblSurveyBehaviour ON Competency.PKCompetency = tblSurveyBehaviour.CompetencyID ";
			query = query
					+ "INNER JOIN Cluster ON Cluster.PKCluster = tblSurveyBehaviour.ClusterID ";
			query = query + "WHERE tblSurveyBehaviour.SurveyID = " + surveyID
					+ " AND tblSurveyBehaviour.ClusterID = " + clusterID;

			// Changed by Ha 02/07/08 Order by CompetencyName instead of
			// CompetencyID
			// Problem with old query: It is ordered by competency ID while the
			// respective
			// value is ordered by Competency name. Therefore, name and value do
			// not match
			if (iOrder == 0)
				query = query
						+ " ORDER BY Cluster.ClusterName, Competency.CompetencyName";
			else
				query = query
						+ " ORDER BY Cluster.ClusterName, Competency.CompetencyName DESC";

		}

		Connection con = null;
		Statement st = null;
		ResultSet rs = null;

		try {
			con = ConnectionBean.getConnection();
			st = con.createStatement();
			rs = st.executeQuery(query);

			while (rs.next()) {
				voCompetency vo = new voCompetency();
				vo.setCompetencyID(rs.getInt("CompetencyID"));
				vo.setCompetencyName(rs.getString("CompetencyName"));
				vo.setCompetencyDefinition(rs.getString("CompetencyDefinition"));
				v.add(vo);

			}

		} catch (Exception ex) {
			System.out.println("SPFIndividualReport.java - Competency - "
					+ ex.getMessage());
		} finally {
			ConnectionBean.closeRset(rs); // Close ResultSet
			ConnectionBean.closeStmt(st); // Close statement
			ConnectionBean.close(con); // Close connection
		}

		return v;
	}

	/**
	 * Retrieves Key Behaviour lists based on CompetencyID and clusterID.
	 */
	public Vector ClusterKBList(int compID, int clusterID) throws SQLException {
		String query = "SELECT DISTINCT tblSurveyBehaviour.KeyBehaviourID, KeyBehaviour.KeyBehaviour ";
		query = query + "FROM tblSurveyBehaviour INNER JOIN KeyBehaviour ON ";
		query = query
				+ "tblSurveyBehaviour.KeyBehaviourID = KeyBehaviour.PKKeyBehaviour ";
		query = query + "WHERE tblSurveyBehaviour.SurveyID = " + surveyID
				+ " AND ";
		query = query + "tblSurveyBehaviour.CompetencyID = " + compID
				+ " AND tblSurveyBehaviour.ClusterID = " + clusterID;
		query = query + " ORDER BY tblSurveyBehaviour.KeyBehaviourID";

		Vector v = new Vector();

		Connection con = null;
		Statement st = null;
		ResultSet rs = null;

		try {
			con = ConnectionBean.getConnection();
			st = con.createStatement();
			rs = st.executeQuery(query);

			while (rs.next()) {
				voKeyBehaviour vo = new voKeyBehaviour();
				vo.setKeyBehaviourID(rs.getInt("KeyBehaviourID"));
				vo.setKeyBehaviour(rs.getString("KeyBehaviour"));
				v.add(vo);

			}

		} catch (Exception ex) {
			System.out.println("SPFIndividualReport.java - ClusterKBList - "
					+ ex.getMessage());
		} finally {
			ConnectionBean.closeRset(rs); // Close ResultSet
			ConnectionBean.closeStmt(st); // Close statement
			ConnectionBean.close(con); // Close connection
		}

		return v;
	}

	/**
	 * Retrieves competencies under the surveyID.
	 */
	public Vector CompetencyByName() throws SQLException {
		String query = "";
		int surveyLevel = Integer.parseInt(surveyInfo[0]);
		Vector v = new Vector();

		if (surveyLevel == 0) {
			query = query
					+ "SELECT tblSurveyCompetency.CompetencyID, Competency.CompetencyName, ";
			query = query
					+ "CompetencyDefinition FROM tblSurveyCompetency INNER JOIN Competency ON ";
			query = query
					+ "tblSurveyCompetency.CompetencyID = Competency.PKCompetency ";
			query = query + "WHERE tblSurveyCompetency.SurveyID = " + surveyID;
			query = query + " ORDER BY Competency.CompetencyName";

		} else {

			query = query
					+ "SELECT DISTINCT tblSurveyBehaviour.CompetencyID, Competency.CompetencyName, ";
			query = query
					+ "Competency.CompetencyDefinition FROM Competency INNER JOIN ";
			query = query
					+ "tblSurveyBehaviour ON Competency.PKCompetency = tblSurveyBehaviour.CompetencyID ";
			query = query
					+ "AND Competency.PKCompetency = tblSurveyBehaviour.CompetencyID ";
			query = query + "WHERE tblSurveyBehaviour.SurveyID = " + surveyID;
			query = query + " ORDER BY Competency.CompetencyName";
		}

		Connection con = null;
		Statement st = null;
		ResultSet rs = null;

		try {
			con = ConnectionBean.getConnection();
			st = con.createStatement();
			rs = st.executeQuery(query);

			while (rs.next()) {
				voCompetency vo = new voCompetency();
				vo.setCompetencyID(rs.getInt("CompetencyID"));
				vo.setCompetencyName(rs.getString("CompetencyName"));
				vo.setCompetencyDefinition(rs.getString("CompetencyDefinition"));
				v.add(vo);

			}

		} catch (Exception ex) {
			System.out.println("SPFIndividualReport.java - CompetencyByName - "
					+ ex.getMessage());
		} finally {
			ConnectionBean.closeRset(rs); // Close ResultSet
			ConnectionBean.closeStmt(st); // Close statement
			ConnectionBean.close(con); // Close connection
		}

		return v;
	}

	/**
	 * Retrieves competencies under the surveyID.
	 * 
	 * @param int iOrder 0 = Ascending, 1 = Descending
	 * 
	 * @return ResultSet Competency
	 */
	public Vector Competency(int iOrder) throws SQLException {
		String query = "";
		int surveyLevel = Integer.parseInt(surveyInfo[0]);

		Vector v = new Vector();

		if (surveyLevel == 0) {
			query = query
					+ "SELECT tblSurveyCompetency.CompetencyID, Competency.CompetencyName, ";
			query = query
					+ "CompetencyDefinition FROM tblSurveyCompetency INNER JOIN Competency ON ";
			query = query
					+ "tblSurveyCompetency.CompetencyID = Competency.PKCompetency ";
			query = query + "WHERE tblSurveyCompetency.SurveyID = " + surveyID;

			// Changed by HA 07/07/08 Order should be by Competency Name instead
			// of CompetencyID
			if (iOrder == 0)
				query = query + " ORDER BY Competency.CompetencyName";
			else
				query = query + " ORDER BY Competency.CompetencyName DESC";

		} else {

			query = query
					+ "SELECT DISTINCT tblSurveyBehaviour.CompetencyID, Competency.CompetencyName, ";
			query = query
					+ "Competency.CompetencyDefinition FROM Competency INNER JOIN ";
			query = query
					+ "tblSurveyBehaviour ON Competency.PKCompetency = tblSurveyBehaviour.CompetencyID ";
			query = query
					+ "AND Competency.PKCompetency = tblSurveyBehaviour.CompetencyID ";
			query = query + "WHERE tblSurveyBehaviour.SurveyID = " + surveyID;

			// Changed by Ha 02/07/08 Order by CompetencyName instead of
			// CompetencyID
			// Problem with old query: It is ordered by competency ID while the
			// respective
			// value is ordered by Competency name. Therefore, name and value do
			// not match
			if (iOrder == 0)
				query = query + " ORDER BY Competency.CompetencyName";
			else
				query = query + " ORDER BY Competency.CompetencyName DESC";

		}

		Connection con = null;
		Statement st = null;
		ResultSet rs = null;

		try {
			con = ConnectionBean.getConnection();
			st = con.createStatement();
			rs = st.executeQuery(query);

			while (rs.next()) {
				voCompetency vo = new voCompetency();
				vo.setCompetencyID(rs.getInt("CompetencyID"));
				vo.setCompetencyName(rs.getString("CompetencyName"));
				vo.setCompetencyDefinition(rs.getString("CompetencyDefinition"));
				v.add(vo);

			}

		} catch (Exception ex) {
			System.out.println("SPFIndividualReport.java - Competency - "
					+ ex.getMessage());
		} finally {
			ConnectionBean.closeRset(rs); // Close ResultSet
			ConnectionBean.closeStmt(st); // Close statement
			ConnectionBean.close(con); // Close connection
		}

		return v;
	}

	/**
	 * Retrieves Key Behaviour lists based on CompetencyID.
	 */
	public Vector KBList(int compID) throws SQLException {
		String query = "SELECT DISTINCT tblSurveyBehaviour.KeyBehaviourID, KeyBehaviour.KeyBehaviour ";
		query = query + "FROM tblSurveyBehaviour INNER JOIN KeyBehaviour ON ";
		query = query
				+ "tblSurveyBehaviour.KeyBehaviourID = KeyBehaviour.PKKeyBehaviour ";
		query = query + "WHERE tblSurveyBehaviour.SurveyID = " + surveyID
				+ " AND ";
		query = query + "tblSurveyBehaviour.CompetencyID = " + compID;
		query = query + " ORDER BY tblSurveyBehaviour.KeyBehaviourID";

		Vector v = new Vector();

		Connection con = null;
		Statement st = null;
		ResultSet rs = null;

		try {
			con = ConnectionBean.getConnection();
			st = con.createStatement();
			rs = st.executeQuery(query);

			while (rs.next()) {
				voKeyBehaviour vo = new voKeyBehaviour();
				vo.setKeyBehaviourID(rs.getInt("KeyBehaviourID"));
				vo.setKeyBehaviour(rs.getString("KeyBehaviour"));
				v.add(vo);

			}

		} catch (Exception ex) {
			System.out.println("SPFIndividualReport.java - KBLIst - "
					+ ex.getMessage());
		} finally {
			ConnectionBean.closeRset(rs); // Close ResultSet
			ConnectionBean.closeStmt(st); // Close statement
			ConnectionBean.close(con); // Close connection
		}

		return v;
	}

	/**
	 * To retrieve a list of clusters in the survey which contains comments by
	 * raters
	 * 
	 * @author Albert
	 **/
	public Vector ClusterListHaveComments() throws SQLException {
		String query = "SELECT DISTINCT(Cluster.PKCluster), Cluster.ClusterName FROM tblAssignment ";
		query = query
				+ "INNER JOIN tblComment ON tblAssignment.AssignmentID = tblComment.AssignmentID ";
		query = query
				+ "INNER JOIN Competency ON tblComment.CompetencyID = Competency.PKCompetency ";
		query = query
				+ "INNER JOIN tblSurveyCompetency ON (tblSurveyCompetency.CompetencyID = Competency.PKCompetency AND tblAssignment.SurveyID = tblSurveyCompetency.SurveyID) ";
		query = query
				+ "INNER JOIN Cluster ON Cluster.PKCluster = tblSurveyCompetency.ClusterID ";
		query = query + "WHERE tblAssignment.SurveyID = " + surveyID + " ";
		query = query + "AND tblAssignment.TargetLoginID = " + targetID + " ";
		query = query + "AND tblComment.Comment != '' ";
		query = query + "ORDER BY Cluster.ClusterName";

		Vector v = new Vector();

		Connection con = null;
		Statement st = null;
		ResultSet rs = null;

		try {
			con = ConnectionBean.getConnection();
			st = con.createStatement();
			rs = st.executeQuery(query);

			while (rs.next()) {
				voCluster vo = new voCluster();
				vo.setClusterID(rs.getInt("PKCluster"));
				vo.setClusterName(rs.getString("ClusterName"));
				v.add(vo);
			}

		} catch (Exception ex) {
			System.out
					.println("SPFIndividualReport.java - ClusterListHaveComments - "
							+ ex.getMessage());
		} finally {
			ConnectionBean.closeRset(rs); // Close ResultSet
			ConnectionBean.closeStmt(st); // Close statement
			ConnectionBean.close(con); // Close connection
		}

		return v;
	}

	/**
	 * To retrieve a list of competencies in the survey which belong to a
	 * particular cluster which contains comments by raters
	 * 
	 * @author Albert
	 **/
	public Vector ClusterCompListHaveComments(int clusterID)
			throws SQLException {
		String query = "SELECT DISTINCT(Competency.PKCompetency), Competency.CompetencyName, Competency.CompetencyDefinition FROM tblAssignment ";
		query = query
				+ "INNER JOIN tblComment ON tblAssignment.AssignmentID = tblComment.AssignmentID ";
		query = query
				+ "INNER JOIN Competency ON tblComment.CompetencyID = Competency.PKCompetency ";
		query = query
				+ "INNER JOIN tblSurveyCompetency ON (tblSurveyCompetency.CompetencyID = Competency.PKCompetency AND tblAssignment.SurveyID = tblSurveyCompetency.SurveyID) ";
		query = query
				+ "INNER JOIN Cluster ON Cluster.PKCluster = tblSurveyCompetency.ClusterID ";
		query = query + "WHERE tblAssignment.SurveyID = " + surveyID + " ";
		query = query + "AND tblAssignment.TargetLoginID = " + targetID + " ";
		query = query + "AND tblSurveyCompetency.ClusterID = " + clusterID
				+ " ";
		query = query + "AND tblComment.Comment != '' ";
		query = query + "ORDER BY Competency.CompetencyName";

		Vector v = new Vector();

		Connection con = null;
		Statement st = null;
		ResultSet rs = null;

		try {
			con = ConnectionBean.getConnection();
			st = con.createStatement();
			rs = st.executeQuery(query);

			while (rs.next()) {
				voCompetency vo = new voCompetency();
				vo.setCompetencyID(rs.getInt("PKCompetency"));
				vo.setCompetencyName(rs.getString("CompetencyName"));
				vo.setCompetencyDefinition(rs.getString("CompetencyDefinition"));
				v.add(vo);
			}

		} catch (Exception ex) {
			System.out
					.println("SPFIndividualReport.java - ClusterCompListHaveComments - "
							+ ex.getMessage());
		} finally {
			ConnectionBean.closeRset(rs); // Close ResultSet
			ConnectionBean.closeStmt(st); // Close statement
			ConnectionBean.close(con); // Close connection
		}

		return v;
	}

	/**
	 * To retrieve a list of competency in the survey which contains comments by
	 * raters
	 * 
	 * @author Sebastian
	 * @since v.1.3.12.78 (19 July 2010)
	 **/
	public Vector CompListHaveComments() throws SQLException {
		String query = "SELECT DISTINCT(Competency.PKCompetency), Competency.CompetencyName, Competency.CompetencyDefinition FROM tblAssignment ";
		query = query
				+ "INNER JOIN tblComment ON tblAssignment.AssignmentID = tblComment.AssignmentID ";
		query = query
				+ "INNER JOIN Competency ON tblComment.CompetencyID = Competency.PKCompetency ";
		query = query + "WHERE tblAssignment.SurveyID = " + surveyID + " ";
		query = query + "AND tblAssignment.TargetLoginID = " + targetID + " ";
		query = query + "AND tblComment.Comment != '' ";
		query = query + "ORDER BY Competency.CompetencyName";
		Vector v = new Vector();

		Connection con = null;
		Statement st = null;
		ResultSet rs = null;

		try {
			con = ConnectionBean.getConnection();
			st = con.createStatement();
			rs = st.executeQuery(query);

			while (rs.next()) {
				voCompetency vo = new voCompetency();
				vo.setCompetencyID(rs.getInt("PKCompetency"));
				vo.setCompetencyName(rs.getString("CompetencyName"));
				vo.setCompetencyDefinition(rs.getString("CompetencyDefinition"));
				v.add(vo);
			}

		} catch (Exception ex) {
			System.out
					.println("SPFIndividualReport.java - CompListHaveComments - "
							+ ex.getMessage());
		} finally {
			ConnectionBean.closeRset(rs); // Close ResultSet
			ConnectionBean.closeStmt(st); // Close statement
			ConnectionBean.close(con); // Close connection
		}

		return v;

	}

	/**
	 * To retreive the Key Behaviours in the competency that contains comments
	 * by raters
	 * 
	 * @param compID
	 *            - Specifiy the competency to reference
	 * @author Sebastian
	 * @since v.1.3.12.78 (19 July 2010)
	 **/
	public Vector KBListHaveComments(int compID) throws SQLException {
		String query = "SELECT DISTINCT(KeyBehaviour.PKKeyBehaviour), KeyBehaviour.KeyBehaviour FROM tblAssignment ";
		query = query
				+ "INNER JOIN tblComment ON tblAssignment.AssignmentID = tblComment.AssignmentID ";
		query = query
				+ "INNER JOIN Competency ON tblComment.CompetencyID = Competency.PKCompetency ";
		query = query
				+ "INNER JOIN KeyBehaviour ON tblComment.KeyBehaviourID = KeyBehaviour.PKKeyBehaviour ";
		query = query + "WHERE tblAssignment.SurveyID = " + surveyID + " ";
		query = query + "AND tblAssignment.TargetLoginID = " + targetID + " ";
		query = query + "AND Competency.PKCompetency = " + compID + " ";
		query = query + "AND tblComment.Comment != '' ";
		query = query + "ORDER BY KeyBehaviour.PKKeyBehaviour";

		Vector v = new Vector();

		Connection con = null;
		Statement st = null;
		ResultSet rs = null;

		try {
			con = ConnectionBean.getConnection();
			st = con.createStatement();
			rs = st.executeQuery(query);

			while (rs.next()) {
				voKeyBehaviour vo = new voKeyBehaviour();
				vo.setKeyBehaviourID(rs.getInt("PKKeyBehaviour"));
				vo.setKeyBehaviour(rs.getString("KeyBehaviour"));
				v.add(vo);

			}

		} catch (Exception ex) {
			System.out
					.println("SPFIndividualReport.java - KBListHaveComments - "
							+ ex.getMessage());
		} finally {
			ConnectionBean.closeRset(rs); // Close ResultSet
			ConnectionBean.closeStmt(st); // Close statement
			ConnectionBean.close(con); // Close connection
		}

		return v;
	}

	/**
	 * Retrieve all the rating task assigned to the specific survey.
	 */
	public Vector RatingTask() throws SQLException {
		// Changed by Ha 27/05/08: add keyword DISTINCT into the query
		String query = "SELECT DISTINCT tblSurveyRating.RatingTaskID, tblRatingTask.RatingCode, ";
		query = query
				+ "tblSurveyRating.RatingTaskName FROM tblSurveyRating INNER JOIN ";
		query = query
				+ "tblRatingTask ON tblSurveyRating.RatingTaskID = tblRatingTask.RatingTaskID ";
		query = query + "WHERE tblSurveyRating.SurveyID = " + surveyID;
		query = query + " and tblRatingTask.RatingCode in('CP', 'CPR', 'FPR')";
		query = query + " ORDER BY tblSurveyRating.RatingTaskID";

		Vector v = new Vector();

		Connection con = null;
		Statement st = null;
		ResultSet rs = null;

		try {
			con = ConnectionBean.getConnection();
			st = con.createStatement();
			rs = st.executeQuery(query);

			while (rs.next()) {
				votblSurveyRating vo = new votblSurveyRating();
				vo.setRatingTaskID(rs.getInt("RatingTaskID"));
				vo.setRatingCode(rs.getString("RatingCode"));
				vo.setRatingTaskName(rs.getString("RatingTaskName"));
				v.add(vo);
			}

		} catch (Exception ex) {
			System.out.println("SPFIndividualReport.java - RatingTask - "
					+ ex.getMessage());
		} finally {
			ConnectionBean.closeRset(rs); // Close ResultSet
			ConnectionBean.closeStmt(st); // Close statement
			ConnectionBean.close(con); // Close connection
		}

		return v;
	}

	/**
	 * Get the maximum scale, which is to be used in the alignment process.
	 */
	public int MaxScale() throws SQLException {
		int total = 0;

		String query = "SELECT MAX(tblScale.ScaleRange) AS Result FROM ";
		query = query + "tblScale INNER JOIN tblSurveyRating ON ";
		query = query + "tblScale.ScaleID = tblSurveyRating.ScaleID WHERE ";
		query = query + "tblSurveyRating.SurveyID = " + surveyID;

		Connection con = null;
		Statement st = null;
		ResultSet rs = null;

		try {
			con = ConnectionBean.getConnection();
			st = con.createStatement();
			rs = st.executeQuery(query);

			if (rs.next())
				total = rs.getInt(1);

		} catch (Exception ex) {
			System.out.println("SPFIndividualReport.java - MaxScale - "
					+ ex.getMessage());
		} finally {
			ConnectionBean.closeRset(rs); // Close ResultSet
			ConnectionBean.closeStmt(st); // Close statement
			ConnectionBean.close(con); // Close connection
		}

		return total;
	}

	/**
	 * Count the total clusters in the particular survey.
	 */
	public int totalCluster() throws SQLException {
		String query = "";

		int total = 0;

		query += "SELECT  COUNT(ClusterID) AS Total FROM tblSurveyCluster ";
		query += "WHERE SurveyID = " + surveyID;

		Connection con = null;
		Statement st = null;
		ResultSet rs = null;

		try {
			con = ConnectionBean.getConnection();
			st = con.createStatement();
			rs = st.executeQuery(query);

			if (rs.next())
				total = rs.getInt(1);

		} catch (Exception ex) {
			System.out.println("SPFIndividualReport.java - totalCluster - "
					+ ex.getMessage());
		} finally {
			ConnectionBean.closeRset(rs); // Close ResultSet
			ConnectionBean.closeStmt(st); // Close statement
			ConnectionBean.close(con); // Close connection
		}

		return total;
	}

	/**
	 * Count the total competencies in the particular survey.
	 */
	public int totalCompetency() throws SQLException {
		String query = "";
		int surveyLevel = Integer.parseInt(surveyInfo[0]);

		int total = 0;

		if (surveyLevel == 0) {
			query = query
					+ "SELECT  COUNT(CompetencyID) AS Total FROM tblSurveyCompetency ";
			query = query + "WHERE SurveyID = " + surveyID;
		} else {
			query = query
					+ "SELECT COUNT(DISTINCT CompetencyID) AS Total FROM ";
			query = query + "tblSurveyBehaviour WHERE SurveyID = " + surveyID;
		}

		Connection con = null;
		Statement st = null;
		ResultSet rs = null;

		try {
			con = ConnectionBean.getConnection();
			st = con.createStatement();
			rs = st.executeQuery(query);

			if (rs.next())
				total = rs.getInt(1);

		} catch (Exception ex) {
			System.out.println("SPFIndividualReport.java - totalCompetency - "
					+ ex.getMessage());
		} finally {
			ConnectionBean.closeRset(rs); // Close ResultSet
			ConnectionBean.closeStmt(st); // Close statement
			ConnectionBean.close(con); // Close connection
		}

		return total;
	}

	/**
	 * by Hemilda 23/09/2008 Count total Others for the particular survey and
	 * target. for KB level To calculate number of others rater for each rating
	 * task of each KB
	 */
	public int totalOth1(int iRatingTaskID, int iCompetencyID)
			throws SQLException {
		int total = 0;
		SurveyResult SR = new SurveyResult();
		Calculation cal = new Calculation();
		String query = "select max(table1.Cnt)AS Total ";
		query = query + " From( ";
		query = query
				+ " SELECT     COUNT(tblAssignment.RaterCode) AS Cnt,tblResultBehaviour.KeyBehaviourID ";
		query = query + " FROM         tblAssignment INNER JOIN ";
		query = query
				+ " tblResultBehaviour ON tblAssignment.AssignmentID = tblResultBehaviour.AssignmentID INNER JOIN ";
		query = query
				+ " KeyBehaviour ON tblResultBehaviour.KeyBehaviourID = KeyBehaviour.PKKeyBehaviour ";
		query = query + " WHERE     (tblAssignment.SurveyID =  " + surveyID
				+ ") AND (tblAssignment.TargetLoginID = " + targetID + ") ";
		if (cal.NAIncluded(surveyID) == 0)
			query = query
					+ " AND RaterCode LIKE 'OTH%' and RaterStatus in(1,2,4)";
		else
			query = query
					+ " AND RaterCode LIKE 'OTH%' and RaterStatus in(1,2,4,5)";
		query = query + "  AND (tblResultBehaviour.RatingTaskID = "
				+ iRatingTaskID + ")and (KeyBehaviour.FKCompetency = "
				+ iCompetencyID + ") ";
		if (cal.NAIncluded(surveyID) == 0)
			query = query + " AND (tblResultBehaviour.Result <> 0)";
		query = query + "  group by tblResultBehaviour.KeyBehaviourID ";
		query = query + "  ) table1 ";

		Connection con = null;
		Statement st = null;
		ResultSet rs = null;

		try {
			con = ConnectionBean.getConnection();
			st = con.createStatement();
			rs = st.executeQuery(query);

			if (rs.next())
				total = rs.getInt(1);

		} catch (Exception ex) {
			System.out.println("SPFIndividualReport.java - totalOth - "
					+ ex.getMessage());
		} finally {
			ConnectionBean.closeRset(rs); // Close ResultSet
			ConnectionBean.closeStmt(st); // Close statement
			ConnectionBean.close(con); // Close connection
		}
		// System.out.println(">>>>>>>>> "+query);
		return total;

	}

	/**
	 * Count total Others for the particular survey and target. Code edited by
	 * Ha adding RatingTask and Competency to method signature To calculate
	 * number of others rater for each rating task of each competency
	 */
	public int totalOth(int iRatingTaskID, int iCompetencyID)
			throws SQLException {
		int total = 0;
		SurveyResult SR = new SurveyResult();
		Calculation cal = new Calculation();
		// Query changed by Ha 07/07/08 to calculate number of others rated for
		// this target
		// exlcluded who put NA in their questionnaire if the survey is
		// NA_Excluded
		String query = "SELECT COUNT(RaterCode) AS Total FROM tblAssignment ";
		query = query
				+ " INNER JOIN  tblResultCompetency ON tblAssignment.AssignmentID = tblResultCompetency.AssignmentID ";
		query = query + "WHERE SurveyID = " + surveyID
				+ " AND TargetLoginID = " + targetID;
		if (cal.NAIncluded(surveyID) == 0)
			query = query
					+ " AND RaterCode LIKE 'OTH%' and RaterStatus in(1,2,4)";
		else
			query = query
					+ " AND RaterCode LIKE 'OTH%' and RaterStatus in(1,2,4,5)";
		query = query + " AND tblResultCompetency.RatingTaskID = "
				+ iRatingTaskID;
		query = query + " AND tblResultCompetency.CompetencyID = "
				+ iCompetencyID;
		if (cal.NAIncluded(surveyID) == 0)
			query = query + "AND tblResultCompetency.Result <> 0";

		Connection con = null;
		Statement st = null;
		ResultSet rs = null;

		try {
			con = ConnectionBean.getConnection();
			st = con.createStatement();
			rs = st.executeQuery(query);

			if (rs.next())
				total = rs.getInt(1);

		} catch (Exception ex) {
			System.out.println("SPFIndividualReport.java - totalOth - "
					+ ex.getMessage());
		} finally {
			ConnectionBean.closeRset(rs); // Close ResultSet
			ConnectionBean.closeStmt(st); // Close statement
			ConnectionBean.close(con); // Close connection
		}
		// System.out.println(">>>>>>>>> "+query);
		return total;

	}

	/**
	 * by Hemilda date 23/09/2008 Count total Others for the particular survey
	 * and target. for KB level To calculate number of others rater for each
	 * rating task of each competency
	 */
	public int totalOth(int iRatingTaskID, int iCompetencyID, int iKBId)
			throws SQLException {
		int total = 0;
		SurveyResult SR = new SurveyResult();
		Calculation cal = new Calculation();

		String query = "SELECT     COUNT(tblAssignment.RaterCode) AS Total ";
		query = query + " FROM         tblAssignment INNER JOIN ";
		query = query
				+ "  tblResultBehaviour ON tblAssignment.AssignmentID = tblResultBehaviour.AssignmentID ";
		query = query + "  WHERE     (tblAssignment.SurveyID =  " + surveyID
				+ ") AND (tblAssignment.TargetLoginID =" + targetID + ")";
		if (cal.NAIncluded(surveyID) == 0)
			query = query
					+ " AND RaterCode LIKE 'OTH%' and RaterStatus in(1,2,4)";
		else
			query = query
					+ " AND RaterCode LIKE 'OTH%' and RaterStatus in(1,2,4,5)";
		query = query + " AND (tblResultBehaviour.RatingTaskID ="
				+ iRatingTaskID + ") AND (tblResultBehaviour.KeyBehaviourID = "
				+ iKBId + ") ";
		if (cal.NAIncluded(surveyID) == 0)
			query = query + " AND (tblResultBehaviour.Result <> 0)";

		Connection con = null;
		Statement st = null;
		ResultSet rs = null;

		try {
			con = ConnectionBean.getConnection();
			st = con.createStatement();
			rs = st.executeQuery(query);

			if (rs.next())
				total = rs.getInt(1);

		} catch (Exception ex) {
			System.out.println("SPFIndividualReport.java - totalOth - "
					+ ex.getMessage());
		} finally {
			ConnectionBean.closeRset(rs); // Close ResultSet
			ConnectionBean.closeStmt(st); // Close statement
			ConnectionBean.close(con); // Close connection
		}
		// System.out.println(">>>>>>>>> "+query);
		return total;

	}

	/**
	 * by Hemilda 23/09/2008 Count total Supervisors for the particular survey
	 * and target. for KB level To calculate number of supervisor rater for each
	 * rating task of each KB
	 */
	public int totalSup1(int iRatingTaskID, int iCompetencyID)
			throws SQLException {
		int total = 0;
		SurveyResult SR = new SurveyResult();
		Calculation cal = new Calculation();
		String query = "select max(table1.Cnt)AS Total ";
		query = query + " From( ";
		query = query
				+ " SELECT     COUNT(tblAssignment.RaterCode) AS Cnt,tblResultBehaviour.KeyBehaviourID ";
		query = query + " FROM         tblAssignment INNER JOIN ";
		query = query
				+ " tblResultBehaviour ON tblAssignment.AssignmentID = tblResultBehaviour.AssignmentID INNER JOIN ";
		query = query
				+ " KeyBehaviour ON tblResultBehaviour.KeyBehaviourID = KeyBehaviour.PKKeyBehaviour ";
		query = query + " WHERE     (tblAssignment.SurveyID =  " + surveyID
				+ ") AND (tblAssignment.TargetLoginID = " + targetID + ") ";
		if (cal.NAIncluded(surveyID) == 0)
			query = query
					+ " AND RaterCode LIKE 'SUP%' and RaterStatus in(1,2,4)";
		else
			query = query
					+ " AND RaterCode LIKE 'SUP%' and RaterStatus in(1,2,4,5)";
		query = query + "  AND (tblResultBehaviour.RatingTaskID = "
				+ iRatingTaskID + ")and (KeyBehaviour.FKCompetency = "
				+ iCompetencyID + ") ";
		if (cal.NAIncluded(surveyID) == 0)
			query = query + " AND (tblResultBehaviour.Result <> 0)";
		query = query + "  group by tblResultBehaviour.KeyBehaviourID ";
		query = query + "  ) table1 ";

		Connection con = null;
		Statement st = null;
		ResultSet rs = null;

		try {
			con = ConnectionBean.getConnection();
			st = con.createStatement();
			rs = st.executeQuery(query);

			if (rs.next())
				total = rs.getInt(1);

		} catch (Exception ex) {
			System.out.println("SPFIndividualReport.java - totalSup - "
					+ ex.getMessage());
		} finally {
			ConnectionBean.closeRset(rs); // Close ResultSet
			ConnectionBean.closeStmt(st); // Close statement
			ConnectionBean.close(con); // Close connection
		}
		// System.out.println(">>>>>>>>> "+query);
		return total;

	}

	/**
	 * by Hemilda 23/09/2008 Count total Supervisors for the particular survey
	 * and target. KB level To calculate number of supervisor rater for each
	 * rating task of each competency
	 */
	public int totalSup(int iRatingTaskID, int iCompetencyID, int iKBId)
			throws SQLException {
		int total = 0;
		Calculation cal = new Calculation();

		String query = "SELECT COUNT(RaterCode) AS Total FROM tblAssignment ";
		query = query
				+ " INNER JOIN tblResultBehaviour ON tblAssignment.AssignmentID = tblResultBehaviour.AssignmentID ";
		query = query + "WHERE SurveyID = " + surveyID
				+ " AND TargetLoginID = " + targetID;
		if (cal.NAIncluded(surveyID) == 0)
			query = query
					+ " AND RaterCode LIKE 'SUP%' and RaterStatus in(1,2,4)";
		else
			query = query
					+ "AND RaterCode LIKE 'SUP%' and RaterStatus in(1,2,4,5)";
		query = query + " AND tblResultBehaviour.RatingTaskID = "
				+ iRatingTaskID;
		query = query + " AND tblResultBehaviour.KeyBehaviourID = " + iKBId;
		if (cal.NAIncluded(surveyID) == 0)
			query = query + " AND tblResultBehaviour.Result <> 0";
		Connection con = null;
		Statement st = null;
		ResultSet rs = null;

		try {
			con = ConnectionBean.getConnection();
			st = con.createStatement();
			rs = st.executeQuery(query);

			if (rs.next())
				total = rs.getInt(1);

		} catch (Exception ex) {
			System.out.println("SPFIndividualReport.java - totalSup - "
					+ ex.getMessage());
		} finally {
			ConnectionBean.closeRset(rs); // Close ResultSet
			ConnectionBean.closeStmt(st); // Close statement
			ConnectionBean.close(con); // Close connection
		}

		return total;
	}

	/**
	 * Count total Supervisors for the particular survey and target. Code edited
	 * by Ha adding RatingTask and Competency to method signature To calculate
	 * number of supervisor rater for each rating task of each competency
	 */
	public int totalSup(int iRatingTaskID, int iCompetencyID)
			throws SQLException {
		int total = 0;
		Calculation cal = new Calculation();
		// Query changed by Ha 07/07/08 to calculate number of supervisor rated
		// for this target
		// exlcluded who put NA in their questionnaire if survey is NA_Excluded
		String query = "SELECT COUNT(RaterCode) AS Total FROM tblAssignment ";
		query = query
				+ " INNER JOIN tblResultCompetency ON tblAssignment.AssignmentID = tblResultCompetency.AssignmentID ";
		query = query + "WHERE SurveyID = " + surveyID
				+ " AND TargetLoginID = " + targetID;
		if (cal.NAIncluded(surveyID) == 0)
			query = query
					+ " AND RaterCode LIKE 'SUP%' and RaterStatus in(1,2,4)";
		else
			query = query
					+ "AND RaterCode LIKE 'SUP%' and RaterStatus in(1,2,4,5)";
		query = query + " AND tblResultCompetency.RatingTaskID = "
				+ iRatingTaskID;
		query = query + " AND tblResultCompetency.CompetencyID = "
				+ iCompetencyID;
		if (cal.NAIncluded(surveyID) == 0)
			query = query + " AND tblResultCompetency.Result <> 0";
		Connection con = null;
		Statement st = null;
		ResultSet rs = null;

		try {
			con = ConnectionBean.getConnection();
			st = con.createStatement();
			rs = st.executeQuery(query);

			if (rs.next())
				total = rs.getInt(1);

		} catch (Exception ex) {
			System.out.println("SPFIndividualReport.java - totalSup - "
					+ ex.getMessage());
		} finally {
			ConnectionBean.closeRset(rs); // Close ResultSet
			ConnectionBean.closeStmt(st); // Close statement
			ConnectionBean.close(con); // Close connection
		}

		return total;
	}

	/**
	 * by Hemilda 23/09/2008 Count total Self for the particular survey and
	 * target. KB level To calculate number of SELF rater for each rating task
	 * of each KB
	 */
	public int totalSelf1(int iRatingTaskID, int iCompetencyID)
			throws SQLException {
		int total = 0;
		SurveyResult SR = new SurveyResult();
		Calculation cal = new Calculation();
		String query = "select max(table1.Cnt)AS Total ";
		query = query + " From( ";
		query = query
				+ " SELECT     COUNT(tblAssignment.RaterCode) AS Cnt,tblResultBehaviour.KeyBehaviourID ";
		query = query + " FROM         tblAssignment INNER JOIN ";
		query = query
				+ " tblResultBehaviour ON tblAssignment.AssignmentID = tblResultBehaviour.AssignmentID INNER JOIN ";
		query = query
				+ " KeyBehaviour ON tblResultBehaviour.KeyBehaviourID = KeyBehaviour.PKKeyBehaviour ";
		query = query + " WHERE     (tblAssignment.SurveyID =  " + surveyID
				+ ") AND (tblAssignment.TargetLoginID = " + targetID + ") ";
		if (cal.NAIncluded(surveyID) == 0)
			query = query
					+ " AND RaterCode LIKE 'SELF' and RaterStatus in(1,2,4)";
		else
			query = query
					+ " AND RaterCode LIKE 'SELF' and RaterStatus in(1,2,4,5)";
		query = query + "  AND (tblResultBehaviour.RatingTaskID = "
				+ iRatingTaskID + ")and (KeyBehaviour.FKCompetency = "
				+ iCompetencyID + ") ";
		if (cal.NAIncluded(surveyID) == 0)
			query = query + " AND (tblResultBehaviour.Result <> 0)";
		query = query + "  group by tblResultBehaviour.KeyBehaviourID ";
		query = query + "  ) table1 ";

		Connection con = null;
		Statement st = null;
		ResultSet rs = null;

		try {
			con = ConnectionBean.getConnection();
			st = con.createStatement();
			rs = st.executeQuery(query);

			if (rs.next())
				total = rs.getInt(1);

		} catch (Exception ex) {
			System.out.println("SPFIndividualReport.java - totalSelf - "
					+ ex.getMessage());
		} finally {
			ConnectionBean.closeRset(rs); // Close ResultSet
			ConnectionBean.closeStmt(st); // Close statement
			ConnectionBean.close(con); // Close connection
		}
		// System.out.println(">>>>>>>>> "+query);
		return total;

	}

	/**
	 * Count total Self for the particular survey and target. for KB level To
	 * calculate number of SELF rater for each rating task of each competency
	 */
	public int totalSelf(int iRatingTaskID, int iCompetencyID, int iKBId)
			throws SQLException {
		int total = 0;
		Calculation cal = new Calculation();

		String query = "SELECT COUNT(RaterCode) AS Total FROM tblAssignment INNER JOIN tblResultBehaviour";
		query = query
				+ " ON tblAssignment.AssignmentID = tblResultBehaviour.AssignmentID ";
		query = query + "WHERE SurveyID = " + surveyID
				+ " AND TargetLoginID = " + targetID;
		if (cal.NAIncluded(surveyID) == 0)
			query = query + " AND RaterCode = 'SELF' and RaterStatus in(1,2,4)";
		else
			query = query
					+ " AND RaterCode = 'SELF' and RaterStatus in(1,2,4,5)";
		query = query + " AND tblResultBehaviour.RatingTaskID  =  "
				+ iRatingTaskID;
		query = query + " AND tblResultBehaviour.KeyBehaviourID = " + iKBId;

		if (cal.NAIncluded(surveyID) == 0)
			query = query + " AND tblResultBehaviour.Result <>0";

		Connection con = null;
		Statement st = null;
		ResultSet rs = null;

		try {
			con = ConnectionBean.getConnection();
			st = con.createStatement();
			rs = st.executeQuery(query);

			if (rs.next())
				total = rs.getInt(1);

		} catch (Exception ex) {
			System.out.println("SPFIndividualReport.java - totalSelf - "
					+ ex.getMessage());
		} finally {
			ConnectionBean.closeRset(rs); // Close ResultSet
			ConnectionBean.closeStmt(st); // Close statement
			ConnectionBean.close(con); // Close connection
		}

		return total;
	}

	/**
	 * Count total Self for the particular survey and target. Code edited by Ha
	 * 07/07/08 add Rating task ID and Competency ID to method signature To
	 * calculate number of SELF rater for each rating task of each competency
	 */
	public int totalSelf(int iRatingTaskID, int iCompetencyID)
			throws SQLException {
		int total = 0;
		Calculation cal = new Calculation();
		// Query changed by Ha 07/07/08 to calculate number of SELF rated for
		// this target
		// exlcluded who put NA in their questionnaire if survey is NA_Excluded
		String query = "SELECT COUNT(RaterCode) AS Total FROM tblAssignment INNER JOIN tblResultCompetency";
		query = query
				+ " ON tblAssignment.AssignmentID = tblResultCompetency.AssignmentID ";
		query = query + "WHERE SurveyID = " + surveyID
				+ " AND TargetLoginID = " + targetID;
		if (cal.NAIncluded(surveyID) == 0)
			query = query + " AND RaterCode = 'SELF' and RaterStatus in(1,2,4)";
		else
			query = query
					+ " AND RaterCode = 'SELF' and RaterStatus in(1,2,4,5)";
		query = query + " AND tblResultCompetency.RatingTaskID  =  "
				+ iRatingTaskID;
		query = query + " AND tblResultCompetency.CompetencyID  = "
				+ iCompetencyID;

		if (cal.NAIncluded(surveyID) == 0)
			query = query + " AND tblResultCompetency.Result <>0";

		Connection con = null;
		Statement st = null;
		ResultSet rs = null;

		try {
			con = ConnectionBean.getConnection();
			st = con.createStatement();
			rs = st.executeQuery(query);

			if (rs.next())
				total = rs.getInt(1);

		} catch (Exception ex) {
			System.out.println("SPFIndividualReport.java - totalSelf - "
					+ ex.getMessage());
		} finally {
			ConnectionBean.closeRset(rs); // Close ResultSet
			ConnectionBean.closeStmt(st); // Close statement
			ConnectionBean.close(con); // Close connection
		}

		return total;
	}

	/**
	 * Count total groups for the particular survey and target.
	 */
	public int totalGroup() throws SQLException {
		int total = 0;

		String query = "SELECT COUNT(DISTINCT tblAssignment.RTRelation) AS TotalGroup ";
		query = query + "FROM tblAssignment INNER JOIN tblSurveyRating ON ";
		query = query
				+ "tblAssignment.SurveyID = tblSurveyRating.SurveyID INNER JOIN ";
		query = query
				+ "tblRatingTask ON tblSurveyRating.RatingTaskID = tblRatingTask.RatingTaskID ";
		query = query + "WHERE tblAssignment.SurveyID = " + surveyID + " AND ";
		query = query + "tblAssignment.TargetLoginID = " + targetID
				+ " AND tblRatingTask.RatingCode = 'CP'";
		query = query + " and tblAssignment.RaterStatus in(1,2,4)";

		Connection con = null;
		Statement st = null;
		ResultSet rs = null;

		try {
			con = ConnectionBean.getConnection();
			st = con.createStatement();
			rs = st.executeQuery(query);

			if (rs.next())
				total = rs.getInt(1);

		} catch (Exception ex) {
			System.out.println("SPFIndividualReport.java - totalGroup - "
					+ ex.getMessage());
		} finally {
			ConnectionBean.closeRset(rs); // Close ResultSet
			ConnectionBean.closeStmt(st); // Close statement
			ConnectionBean.close(con); // Close connection
		}

		return total;
	}

	/**
	 * Count total other rating tasks besides CP for the particular survey.
	 */
	public int totalOtherRT() throws SQLException {
		int total = 0;

		String query = "SELECT COUNT(tblRatingTask.RatingCode) AS TotalRT ";
		query = query + "FROM tblSurveyRating INNER JOIN tblRatingTask ON ";
		query = query
				+ "tblSurveyRating.RatingTaskID = tblRatingTask.RatingTaskID ";
		query = query + "WHERE tblSurveyRating.SurveyID = " + surveyID;
		query = query
				+ " AND (tblRatingTask.RatingCode = 'CPR' or tblRatingTask.RatingCode = 'FPR')";

		Connection con = null;
		Statement st = null;
		ResultSet rs = null;

		try {
			con = ConnectionBean.getConnection();
			st = con.createStatement();
			rs = st.executeQuery(query);

			if (rs.next())
				total = rs.getInt(1);

		} catch (Exception ex) {
			System.out.println("SPFIndividualReport.java - totalOtherRT - "
					+ ex.getMessage());
		} finally {
			ConnectionBean.closeRset(rs); // Close ResultSet
			ConnectionBean.closeStmt(st); // Close statement
			ConnectionBean.close(con); // Close connection
		}

		return total;
	}

	/**
	 * Retrieves the competency score for all. This is only applied in KB Level
	 * Analysis. The score is stored in table tblTrimmedMean
	 */
	public double CompTrimmedMeanforAll(int RTID, int compID)
			throws SQLException {
		double Result = 0;
		String query = "";
		int reliabilityIndex = C.ReliabilityCheck(surveyID);
		int type = 1;
		if (weightedAverage == true) {
			type = 10;
		}

		if (reliabilityIndex == 0) {
			query = query
					+ "SELECT CompetencyID, Type, round(TrimmedMean, 2) AS Result FROM tblTrimmedMean ";
			query += "WHERE SurveyID = " + surveyID;
			query += " AND TargetLoginID = " + targetID
					+ " AND RatingTaskID = " + RTID + " and CompetencyID = "
					+ compID;
			query += " ORDER BY CompetencyID";
		} else {
			query = "select RatingTaskID, CompetencyID, cast(AVG(AvgMean) as numeric(38,2)) as Result from tblAvgMean ";
			query = query + "where SurveyID = " + surveyID;
			query = query + " AND TargetLoginID = " + targetID;
			query = query + " and Type = " + type;
			query += " AND RatingTaskID = " + RTID + " AND CompetencyID = "
					+ compID;
			query = query + " group by CompetencyID, RatingTaskID";
		}

		Connection con = null;
		Statement st = null;
		ResultSet rs = null;

		try {
			con = ConnectionBean.getConnection();
			st = con.createStatement();
			rs = st.executeQuery(query);

			if (rs.next())
				Result = Double.parseDouble(df.format(rs.getDouble("Result")));

		} catch (Exception ex) {
			System.out
					.println("SPFIndividualReport.java - CompTrimmedMeanForAll - "
							+ ex.getMessage());
		} finally {
			ConnectionBean.closeRset(rs); // Close ResultSet
			ConnectionBean.closeStmt(st); // Close statement
			ConnectionBean.close(con); // Close connection
		}

		return Result;
	}

	/**
	 * Retrieves the average mean of KB for a specific competency. This is only
	 * applied in KB Level Analysis.
	 */
	public Vector KBMean(int RTID, int compID) throws SQLException {
		String query = "SELECT CompetencyID, Type, CAST(AVG(AvgMean) AS numeric(38, 2)) AS Result ";
		query = query + "FROM tblAvgMean WHERE SurveyID = " + surveyID
				+ " AND TargetLoginID = " + targetID;
		query = query + " AND CompetencyID = " + compID
				+ " and RatingTaskID = " + RTID;
		query = query + " GROUP BY CompetencyID, Type ORDER BY Type";

		Connection con = null;
		Statement st = null;
		ResultSet rs = null;
		Vector v = new Vector();

		try {
			con = ConnectionBean.getConnection();
			st = con.createStatement();
			rs = st.executeQuery(query);

			while (rs.next()) {
				String[] arr = new String[3];
				arr[0] = rs.getString(1);
				arr[1] = rs.getString(2);
				arr[2] = rs.getString(3);
				v.add(arr);
			}
		} catch (Exception ex) {
			System.out.println("SPFIndividualReport.java - KBMean - "
					+ ex.getMessage());
		} finally {
			ConnectionBean.closeRset(rs); // Close ResultSet
			ConnectionBean.closeStmt(st); // Close statement
			ConnectionBean.close(con); // Close connection
		}

		return v;
	}

	/**
	 * Retrieve the average or trimmed mean result based on competency and key
	 * behaviour for each group.
	 */
	public Vector MeanResult(int RTID, int compID, int KBID)
			throws SQLException {
		String query = "";
		int surveyLevel = Integer.parseInt(surveyInfo[0]);
		int reliabilityCheck = C.ReliabilityCheck(surveyID);

		String tblName = "tblAvgMean";
		String result = "AvgMean";

		if (reliabilityCheck == 0) {
			tblName = "tblTrimmedMean";
			result = "TrimmedMean";
		}
		// Changed by Ha 27/05/08: add keyword DISTINCT

		if (surveyLevel == 0) {
			query = query + "SELECT DISTINCT " + tblName + ".CompetencyID, ";
			query = query + tblName + ".Type, " + tblName + "." + result;
			query = query + " as Result FROM " + tblName;
			query = query + " WHERE " + tblName + ".SurveyID = " + surveyID
					+ " AND ";
			query = query + tblName + ".TargetLoginID = " + targetID;
			query = query + " AND " + tblName + ".RatingTaskID = " + RTID;
			query = query + " AND " + tblName + ".CompetencyID = " + compID;
			query = query + " ORDER BY " + tblName + ".Type";
		} else {
			query = query
					+ "SELECT DISTINCT CompetencyID, Type, AvgMean as Result, KeyBehaviourID ";
			query = query + "FROM tblAvgMean WHERE SurveyID = " + surveyID
					+ " AND ";
			query = query + "TargetLoginID = " + targetID
					+ " AND RatingTaskID = " + RTID;
			query = query + " AND CompetencyID = " + compID
					+ " AND KeyBehaviourID = " + KBID;
			query = query + " ORDER BY Type";
		}

		Connection con = null;
		Statement st = null;
		ResultSet rs = null;

		Vector v = new Vector();

		try {
			con = ConnectionBean.getConnection();
			st = con.createStatement();
			rs = st.executeQuery(query);

			while (rs.next()) {
				String[] arr = new String[3];
				arr[0] = rs.getString(1);
				arr[1] = df.format(Double.parseDouble(rs.getString(2)));
				arr[2] = df.format(Math.round(Double.parseDouble(rs
						.getString(3)) * 100) / 100.00);
				v.add(arr);
			}
		} catch (Exception ex) {
			System.out.println("SPFIndividualReport.java - MEanResult - "
					+ ex.getMessage());
		} finally {
			ConnectionBean.closeRset(rs); // Close ResultSet
			ConnectionBean.closeStmt(st); // Close statement
			ConnectionBean.close(con); // Close connection
		}

		return v;
	}

	/**
	 * Retrieves the individual level of agreement from tblLevelOfAgreement
	 * based on Competency and Key Behaviour. KBID = 0 if it is Competency Level
	 * Analysis.
	 */
	public double LevelOfAgreement(int compID, int KBID) throws SQLException {
		String query = "";
		int surveyLevel = Integer.parseInt(surveyInfo[0]);
		double LOA = -1;

		if (surveyLevel == 0) {

			query = query
					+ "select * from tblLevelOfAgreement where SurveyID = "
					+ surveyID;
			query = query + " and TargetLoginID = " + targetID
					+ " and CompetencyID = " + compID;

		} else {
			query = query
					+ "select * from tblLevelOfAgreement where SurveyID = "
					+ surveyID;
			query = query + " and TargetLoginID = " + targetID
					+ " and CompetencyID = " + compID;
			query = query + " and KeyBehaviourID = " + KBID;
		}

		Connection con = null;
		Statement st = null;
		ResultSet rs = null;

		try {
			con = ConnectionBean.getConnection();
			st = con.createStatement();
			rs = st.executeQuery(query);

			if (rs.next())
				LOA = rs.getDouble("LevelOfAgreement");

		} catch (Exception ex) {
			System.out.println("SPFIndividualReport.java - LevelOfAgreement - "
					+ ex.getMessage());
		} finally {
			ConnectionBean.closeRset(rs); // Close ResultSet
			ConnectionBean.closeStmt(st); // Close statement
			ConnectionBean.close(con); // Close connection
		}

		return LOA;
	}

	/**
	 * Calculate the average of individual level of agreement for each
	 * competency. This is only apply for KB Level Analysis.
	 */
	public double AvgLevelOfAgreement(int compID, int noOfRaters)
			throws SQLException {
		String query = "";
		int iBase = C.getLOABase(noOfRaters);
		double LOA = -1;
		int iMaxScale = rscale.getMaxScale(surveyID); // Get Maximum Scale of
														// this survey

		/*
		 * query = query +
		 * "SELECT tblResultBehaviour.RatingTaskID, KeyBehaviour.FKCompetency, "
		 * ; query = query +
		 * "cast((100-(stDev(tblResultBehaviour.Result * 10 / " + iMaxScale +
		 * ") * " + iBase + ")) AS numeric(38, 2)) AS LOA "; query = query +
		 * "FROM tblAssignment INNER JOIN tblResultBehaviour ON "; query = query
		 * +
		 * "tblAssignment.AssignmentID = tblResultBehaviour.AssignmentID INNER JOIN "
		 * ; query = query +
		 * "tblRatingTask ON tblResultBehaviour.RatingTaskID = tblRatingTask.RatingTaskID "
		 * ; query = query + "INNER JOIN KeyBehaviour ON "; query = query +
		 * "tblResultBehaviour.KeyBehaviourrID = KeyBehaviour.PKKeyBehaviour ";
		 * query = query + "WHERE tblAssignment.SurveyID = " + surveyID +
		 * " AND "; query = query + "tblAssignment.TargetLoginID = " + targetID
		 * + " AND "; query = query +
		 * "tblAssignment.RaterStatus IN (1, 2, 4) AND KeyBehaviour.FKCompetency = "
		 * + compID; query = query +
		 * " AND tblAssignment.RaterCode <> 'SELF' AND tblRatingTask.RatingCode = 'CP' "
		 * ; query = query +
		 * "GROUP BY tblResultBehaviour.RatingTaskID, KeyBehaviour.FKCompetency"
		 * ;
		 */
		// Edit by Roger 24 July 2008.
		// The base use in this calculation is wrong. Different competencies
		// have different number of raters used to make
		// the calculation, depending on whether the survey is exclude NA and if
		// the rater entered NA
		query = query
				+ "SELECT tblAvgMeanByRater.RatingTaskID, tblAvgMeanByRater.CompetencyID, count(*) as numOfRaters, ";
		query = query + "stDev(tblAvgMeanByRater.AvgMean * 10 / " + iMaxScale
				+ ") AS LOA ";
		query = query + "FROM tblAssignment INNER JOIN ";
		query = query
				+ "tblAvgMeanByRater ON tblAssignment.AssignmentID = tblAvgMeanByRater.AssignmentID INNER JOIN ";
		query = query
				+ "tblRatingTask ON tblAvgMeanByRater.RatingTaskID = tblRatingTask.RatingTaskID ";
		query = query + "WHERE tblAssignment.SurveyID = " + surveyID + " AND ";
		query = query + "tblAssignment.TargetLoginID = " + targetID + " AND ";
		query = query
				+ "tblAssignment.RaterStatus IN (1, 2, 4) AND tblAvgMeanByRater.CompetencyID = "
				+ compID;
		query = query
				+ " AND tblAssignment.RaterCode <> 'SELF' AND tblRatingTask.RatingCode = 'CP' ";
		query = query
				+ "GROUP BY tblAvgMeanByRater.RatingTaskID, tblAvgMeanByRater.CompetencyID";

		Connection con = null;
		Statement st = null;
		ResultSet rs = null;

		try {
			con = ConnectionBean.getConnection();
			st = con.createStatement();
			rs = st.executeQuery(query);

			if (rs.next()) {
				// Edit by Roger 24 July 2008
				// Shift the calculation of LOA from Query to here. It is
				// because the number of raters is get from the query
				// and we need to use the getLOABase formula seperately
				LOA = 100 - rs.getDouble("LOA")
						* C.getLOABase(rs.getInt("numOfRaters"));
				BigDecimal bd = new BigDecimal(LOA);
				bd = bd.setScale(2,BigDecimal.ROUND_HALF_UP); // round to 2
																// decimal place
				LOA = bd.doubleValue();
			}

		} catch (Exception ex) {
			System.out
					.println("SPFIndividualReport.java - AvgLevelOfAgreement - "
							+ ex.getMessage());
		} finally {
			ConnectionBean.closeRset(rs); // Close ResultSet
			ConnectionBean.closeStmt(st); // Close statement
			ConnectionBean.close(con); // Close connection
		}

		return LOA;
	}

	/**
	 * Retrieves the gap results from tblGap based on competency and key
	 * behaviour. KBID = 0 if it is Competency Level Analysis.
	 */
	public double Gap(int compID, int KBID) throws SQLException {
		String query = "";
		double gap = -1;

		query = query + "SELECT Gap FROM tblGap WHERE SurveyID = " + surveyID;
		query = query + " AND TargetLoginID = " + targetID
				+ " AND CompetencyID = " + compID;
		query = query + " and KeyBehaviourID = " + KBID;

		Connection con = null;
		Statement st = null;
		ResultSet rs = null;

		try {
			con = ConnectionBean.getConnection();
			st = con.createStatement();
			rs = st.executeQuery(query);

			if (rs.next())
				gap = rs.getDouble(1);

		} catch (Exception ex) {
			System.out.println("SPFIndividualReport.java - Gap - "
					+ ex.getMessage());
		} finally {
			ConnectionBean.closeRset(rs); // Close ResultSet
			ConnectionBean.closeStmt(st); // Close statement
			ConnectionBean.close(con); // Close connection
		}

		return gap;
	}

	/**
	 * Retrieves the importance result based on competency and key behaviour id.
	 * KBID = 0 if it is Competency Level Analysis.
	 */
	public Vector Importance(int compID, int KBID) throws SQLException {
		String query = "";
		int surveyLevel = Integer.parseInt(surveyInfo[0]);
		int reliabilityCheck = C.ReliabilityCheck(surveyID);

		String tblName = "tblAvgMean";
		String result = "AvgMean";

		if (reliabilityCheck == 0) {
			tblName = "tblTrimmedMean";
			result = "TrimmedMean";
		}

		// try {
		if (surveyLevel == 0) {
			query = query + "SELECT tblRatingTask.RatingCode, ";
			query = query + "tblSurveyRating.RatingTaskName, " + tblName + "."
					+ result + " as Result ";
			query = query + "FROM " + tblName + " INNER JOIN tblRatingTask ON ";
			query = query + tblName
					+ ".RatingTaskID = tblRatingTask.RatingTaskID ";
			query = query + "INNER JOIN tblSurveyRating ON ";
			query = query
					+ "tblRatingTask.RatingTaskID = tblSurveyRating.RatingTaskID AND ";
			query = query + tblName + ".SurveyID = tblSurveyRating.SurveyID ";
			query = query + "WHERE " + tblName + ".SurveyID = " + surveyID
					+ " AND ";
			query = query + tblName + ".TargetLoginID = " + targetID + " AND "
					+ tblName + ".Type = 1 AND ";
			query = query + tblName + ".CompetencyID = " + compID + " AND ";
			query = query
					+ "(tblRatingTask.RatingCode = 'IN' OR tblRatingTask.RatingCode = 'IF')";
		} else {
			query = query
					+ "SELECT tblRatingTask.RatingCode, tblSurveyRating.RatingTaskName, ";
			query = query
					+ "tblAvgMean.AvgMean AS Result FROM tblAvgMean INNER JOIN ";
			query = query
					+ "tblRatingTask ON tblAvgMean.RatingTaskID = tblRatingTask.RatingTaskID ";
			query = query + "INNER JOIN tblSurveyRating ON ";
			query = query
					+ "tblRatingTask.RatingTaskID = tblSurveyRating.RatingTaskID AND ";
			query = query + "tblAvgMean.SurveyID = tblSurveyRating.SurveyID ";
			query = query + "WHERE tblAvgMean.SurveyID = " + surveyID + " AND ";
			query = query + "tblAvgMean.TargetLoginID = " + targetID + " AND ";
			query = query + "tblAvgMean.CompetencyID = " + compID + " AND ";
			query = query + "tblAvgMean.KeyBehaviourID = " + KBID
					+ " AND tblAvgMean.Type = 1 ";
			query = query
					+ "AND (tblRatingTask.RatingCode = 'IN' OR tblRatingTask.RatingCode = 'IF')";
		}

		Connection con = null;
		Statement st = null;
		ResultSet rs = null;

		Vector v = new Vector();

		try {
			con = ConnectionBean.getConnection();
			st = con.createStatement();
			rs = st.executeQuery(query);

			while (rs.next()) {
				String[] arr = new String[3];
				arr[0] = rs.getString(1);
				arr[1] = rs.getString(2);
				arr[2] = rs.getString(3);
				v.add(arr);
			}
		} catch (Exception ex) {
			System.out.println("SPFIndividualReport.java - Importance - "
					+ ex.getMessage());
		} finally {
			ConnectionBean.closeRset(rs); // Close ResultSet
			ConnectionBean.closeStmt(st); // Close statement
			ConnectionBean.close(con); // Close connection
		}

		return v;
	}

	/**
	 * Calculate the average of importance for each competency. This is only
	 * apply for KB Level Analysis.
	 */
	public Vector AvgImportance(int compID) throws SQLException {
		String query = "";

		// try {
		query = query
				+ "SELECT tblRatingTask.RatingCode, tblSurveyRating.RatingTaskName, ";
		query = query
				+ "cast(avg(tblAvgMean.AvgMean) as numeric(38,2)) AS Result FROM tblAvgMean ";
		query = query
				+ "INNER JOIN tblRatingTask ON tblAvgMean.RatingTaskID = tblRatingTask.RatingTaskID ";
		query = query + "INNER JOIN tblSurveyRating ON ";
		query = query
				+ "tblRatingTask.RatingTaskID = tblSurveyRating.RatingTaskID AND ";
		query = query + "tblAvgMean.SurveyID = tblSurveyRating.SurveyID ";
		query = query + "WHERE tblAvgMean.SurveyID = " + surveyID;
		query = query + " AND tblAvgMean.TargetLoginID = " + targetID;
		query = query + " AND tblAvgMean.CompetencyID = " + compID;
		query = query + " AND tblAvgMean.Type = 1 AND ";
		query = query
				+ "(tblRatingTask.RatingCode = 'IN' OR tblRatingTask.RatingCode = 'IF') ";
		query = query
				+ "group by tblRatingTask.RatingTaskID,tblRatingTask.RatingCode, ";
		query = query + "tblSurveyRating.RatingTaskName";

		Connection con = null;
		Statement st = null;
		ResultSet rs = null;

		Vector v = new Vector();

		try {
			con = ConnectionBean.getConnection();
			st = con.createStatement();
			rs = st.executeQuery(query);

			while (rs.next()) {
				String[] arr = new String[3];
				arr[0] = rs.getString(1);
				arr[1] = rs.getString(2);
				arr[2] = rs.getString(3);
				v.add(arr);
			}
		} catch (Exception ex) {
			System.out.println("SPFIndividualReport.java - AvgImportance - "
					+ ex.getMessage());
		} finally {
			ConnectionBean.closeRset(rs); // Close ResultSet
			ConnectionBean.closeStmt(st); // Close statement
			ConnectionBean.close(con); // Close connection
		}

		return v;
	}

	/**
	 * Calculate the average of gap for each competency. This is only apply for
	 * KB Level Analysis.
	 */
	public double getAvgGap(int compID) throws SQLException {
		double gap = 0;

		String query = "Select cast(AVG(Gap) as numeric(38,2)) from tblGap where SurveyID = "
				+ surveyID;
		query = query + " AND TargetLoginID = " + targetID
				+ " and CompetencyID = " + compID;

		Connection con = null;
		Statement st = null;
		ResultSet rs = null;

		try {
			con = ConnectionBean.getConnection();
			st = con.createStatement();
			rs = st.executeQuery(query);

			if (rs.next())
				gap = rs.getDouble(1);

		} catch (Exception ex) {
			System.out.println("SPFIndividualReport.java - getAvgGap - "
					+ ex.getMessage());
		} finally {
			ConnectionBean.closeRset(rs); // Close ResultSet
			ConnectionBean.closeStmt(st); // Close statement
			ConnectionBean.close(con); // Close connection
		}

		return gap;
	}

	/**
	 * Retrieves the minimum and maximum gap, which was set when create/edit
	 * survey.
	 */
	public double[] getMinMaxGap() throws SQLException {
		double gap[] = new double[2];

		String query = "Select MIN_gap, MAX_Gap from tblSurvey where SurveyID = "
				+ surveyID;

		Connection con = null;
		Statement st = null;
		ResultSet rs = null;

		try {
			con = ConnectionBean.getConnection();
			st = con.createStatement();
			rs = st.executeQuery(query);

			if (rs.next()) {
				gap[0] = rs.getDouble(1);
				gap[1] = rs.getDouble(2);
			}

		} catch (Exception ex) {
			System.out.println("SPFIndividualReport.java - getMinMaxGap - "
					+ ex.getMessage());
		} finally {
			ConnectionBean.closeRset(rs); // Close ResultSet
			ConnectionBean.closeStmt(st); // Close statement
			ConnectionBean.close(con); // Close connection
		}

		return gap;
	}

	/**
	 * Retrieves all target results based on the reliability check.
	 */
	public Vector getAllTargetsResults() throws SQLException {
		int surveyLevel = Integer.parseInt(surveyInfo[0]);

		String query = "";

		int reliabilityCheck = C.ReliabilityCheck(surveyID);

		// try {
		if (reliabilityCheck == 0) {
			query = query
					+ "SELECT tblTrimmedMean.CompetencyID, Competency.CompetencyName, ";
			query = query
					+ "cast(AVG(tblTrimmedMean.TrimmedMean) as numeric(38,2)) AS Result ";
			query = query + "FROM tblRatingTask INNER JOIN tblTrimmedMean ON ";
			query = query
					+ "tblRatingTask.RatingTaskID = tblTrimmedMean.RatingTaskID INNER JOIN ";
			query = query
					+ "Competency ON tblTrimmedMean.CompetencyID = Competency.PKCompetency ";
			query = query + "WHERE tblTrimmedMean.SurveyID = " + surveyID;
			query = query + " AND tblTrimmedMean.TargetLoginID <> " + targetID;
			query = query
					+ " AND tblTrimmedMean.Type = 1 AND tblRatingTask.RatingCode = 'CP' ";
			query = query
					+ "GROUP BY tblTrimmedMean.CompetencyID, Competency.CompetencyName ";
			query = query + "order by Competency.CompetencyName";
		} else {
			if (surveyLevel == 0) {
				query = query
						+ "SELECT tblAvgMean.CompetencyID, Competency.CompetencyName, ";
				query = query
						+ "cast(AVG(tblAvgMean.AvgMean) as numeric(38,2)) AS Result ";
				query = query + "FROM tblAvgMean INNER JOIN tblRatingTask ON ";
				query = query
						+ "tblAvgMean.RatingTaskID = tblRatingTask.RatingTaskID ";
				query = query + "INNER JOIN Competency ON ";
				query = query
						+ "tblAvgMean.CompetencyID = Competency.PKCompetency ";
				query = query + "WHERE tblAvgMean.SurveyID = " + surveyID
						+ " AND ";
				query = query + "tblAvgMean.TargetLoginID <> " + targetID
						+ " AND tblAvgMean.Type = 1 AND ";
				query = query + "tblRatingTask.RatingCode = 'CP' ";
				query = query
						+ "GROUP BY tblAvgMean.CompetencyID, Competency.CompetencyName ";
				query = query + "order by Competency.CompetencyName";

			} else {

				query = query
						+ "SELECT tblAvgMean.CompetencyID, Competency.CompetencyName, ";
				query = query
						+ "CAST(AVG(tblAvgMean.AvgMean) AS numeric(38, 2)) AS Result ";
				query = query + "FROM tblRatingTask INNER JOIN tblAvgMean ON ";
				query = query
						+ "tblRatingTask.RatingTaskID = tblAvgMean.RatingTaskID ";
				query = query + "INNER JOIN Competency ON ";
				query = query
						+ "tblAvgMean.CompetencyID = Competency.PKCompetency ";
				query = query + "WHERE tblAvgMean.SurveyID = " + surveyID
						+ " AND ";
				query = query + "tblAvgMean.TargetLoginID <> " + targetID
						+ " AND tblAvgMean.Type = 1 AND ";
				query = query + "tblRatingTask.RatingCode = 'CP' ";
				query = query
						+ "GROUP BY tblAvgMean.CompetencyID, Competency.CompetencyName ";
				query = query + "order by Competency.CompetencyName";
			}

		}
		Connection con = null;
		Statement st = null;
		ResultSet rs = null;

		Vector v = new Vector();

		try {
			con = ConnectionBean.getConnection();
			st = con.createStatement();
			rs = st.executeQuery(query);

			while (rs.next()) {
				String[] arr = new String[3];
				arr[0] = rs.getString(1);
				arr[1] = rs.getString(2);
				arr[2] = rs.getString(3);
				v.add(arr);
			}
		} catch (Exception ex) {
			System.out.println("SPFIndividualReport.java - KBMean - "
					+ ex.getMessage());
		} finally {
			ConnectionBean.closeRset(rs); // Close ResultSet
			ConnectionBean.closeStmt(st); // Close statement
			ConnectionBean.close(con); // Close connection
		}

		return v;
	}

	/**
	 * Retrieves the results under that particular rating code.
	 */
	public Vector CPCPR(String RTCode) throws SQLException {
		String query = "";
		int surveyLevel = Integer.parseInt(surveyInfo[0]);
		int reliabilityCheck = C.ReliabilityCheck(surveyID);
		int cpAll = 1;
		if (weightedAverage == true) {
			cpAll = 10;
		} else {
			cpAll = 1;
		}
		if (reliabilityCheck == 0) {
			query = "SELECT tblTrimmedMean.CompetencyID, Competency.CompetencyName, tblTrimmedMean.TrimmedMean as Result ";
			query = query + "FROM tblTrimmedMean INNER JOIN tblRatingTask ON ";
			query = query
					+ "tblTrimmedMean.RatingTaskID = tblRatingTask.RatingTaskID ";
			query = query + "INNER JOIN Competency ON ";
			query = query
					+ "tblTrimmedMean.CompetencyID = Competency.PKCompetency ";
			query = query + "WHERE tblTrimmedMean.SurveyID = " + surveyID
					+ " AND ";
			query = query + "tblTrimmedMean.TargetLoginID = " + targetID
					+ " AND tblTrimmedMean.Type = 1 AND ";
			query = query + "tblRatingTask.RatingCode = '" + RTCode + "' ";
			query = query + "ORDER BY Competency.CompetencyName";
		} else {
			if (surveyLevel == 0) {
				query = "SELECT tblAvgMean.CompetencyID, Competency.CompetencyName, tblAvgMean.AvgMean as Result ";
				query = query + "FROM tblAvgMean INNER JOIN tblRatingTask ON ";
				query = query
						+ "tblAvgMean.RatingTaskID = tblRatingTask.RatingTaskID ";
				query = query + "INNER JOIN Competency ON ";
				query = query
						+ "tblAvgMean.CompetencyID = Competency.PKCompetency ";
				query = query + "WHERE tblAvgMean.SurveyID = " + surveyID
						+ " AND ";
				query = query + "tblAvgMean.TargetLoginID = " + targetID
						+ " AND tblAvgMean.Type =" + cpAll + " AND ";
				query = query + "tblRatingTask.RatingCode = '" + RTCode
						+ "' ORDER BY Competency.CompetencyName";
			} else {
				query = "SELECT tblAvgMean.CompetencyID, Competency.CompetencyName, ";
				query = query
						+ "CAST(AVG(tblAvgMean.AvgMean) AS numeric(38, 2)) AS Result ";
				query = query + "FROM tblRatingTask INNER JOIN tblAvgMean ON ";
				query = query
						+ "tblRatingTask.RatingTaskID = tblAvgMean.RatingTaskID ";
				query = query + "INNER JOIN Competency ON ";
				query = query
						+ "tblAvgMean.CompetencyID = Competency.PKCompetency ";
				query = query + "WHERE tblAvgMean.SurveyID = " + surveyID
						+ " AND ";
				query = query + "tblAvgMean.TargetLoginID = " + targetID
						+ " AND tblAvgMean.Type =" + cpAll + " AND ";
				query = query + "tblRatingTask.RatingCode = '" + RTCode
						+ "' GROUP BY tblAvgMean.CompetencyID, ";
				query = query
						+ "Competency.CompetencyName order by Competency.CompetencyName";
			}
		}
		// System.out.println("cpcpr "+query);
		Connection con = null;
		Statement st = null;
		ResultSet rs = null;

		Vector v = new Vector();

		try {
			con = ConnectionBean.getConnection();
			st = con.createStatement();
			rs = st.executeQuery(query);

			while (rs.next()) {
				String[] arr = new String[3];
				arr[0] = rs.getString(1);
				arr[1] = rs.getString(2);

				arr[2] = df.format(Math.round(Double.parseDouble(rs
						.getString(3)) * 100) / 100.00);
				v.add(arr);
			}
		} catch (Exception ex) {
			System.out.println("SPFIndividualReport.java - CPCPR - "
					+ ex.getMessage());
		} finally {
			ConnectionBean.closeRset(rs); // Close ResultSet
			ConnectionBean.closeStmt(st); // Close statement
			ConnectionBean.close(con); // Close connection
		}
		// System.out.println(query);
		return v;
	}

	/**
	 * Retrieves the results under that particular rating code sorted by
	 * CompetencyID
	 */
	public Vector CPCPRSortedID(String RTCode) throws SQLException {
		int surveyLevel = Integer.parseInt(surveyInfo[0]);

		String query = "";

		int reliabilityCheck = C.ReliabilityCheck(surveyID);

		if (reliabilityCheck == 0) {
			query = "SELECT tblTrimmedMean.CompetencyID, Competency.CompetencyName, tblTrimmedMean.TrimmedMean as Result ";
			query = query + "FROM tblTrimmedMean INNER JOIN tblRatingTask ON ";
			query = query
					+ "tblTrimmedMean.RatingTaskID = tblRatingTask.RatingTaskID ";
			query = query + "INNER JOIN Competency ON ";
			query = query
					+ "tblTrimmedMean.CompetencyID = Competency.PKCompetency ";
			query = query + "WHERE tblTrimmedMean.SurveyID = " + surveyID
					+ " AND ";
			query = query + "tblTrimmedMean.TargetLoginID = " + targetID
					+ " AND tblTrimmedMean.Type = 1 AND ";
			query = query + "tblRatingTask.RatingCode = '" + RTCode + "' ";
			query = query + "ORDER BY tblTrimmedMean.CompetencyID";
		} else {
			if (surveyLevel == 0) {
				query = "SELECT tblAvgMean.CompetencyID, Competency.CompetencyName, tblAvgMean.AvgMean as Result ";
				query = query + "FROM tblAvgMean INNER JOIN tblRatingTask ON ";
				query = query
						+ "tblAvgMean.RatingTaskID = tblRatingTask.RatingTaskID ";
				query = query + "INNER JOIN Competency ON ";
				query = query
						+ "tblAvgMean.CompetencyID = Competency.PKCompetency ";
				query = query + "WHERE tblAvgMean.SurveyID = " + surveyID
						+ " AND ";
				query = query + "tblAvgMean.TargetLoginID = " + targetID
						+ " AND tblAvgMean.Type = 1 AND ";
				query = query + "tblRatingTask.RatingCode = '" + RTCode
						+ "' ORDER BY tblAvgMean.CompetencyID";

			} else {
				query = "SELECT tblAvgMean.CompetencyID, Competency.CompetencyName, ";
				query = query
						+ "CAST(AVG(tblAvgMean.AvgMean) AS numeric(38, 2)) AS Result ";
				query = query + "FROM tblRatingTask INNER JOIN tblAvgMean ON ";
				query = query
						+ "tblRatingTask.RatingTaskID = tblAvgMean.RatingTaskID ";
				query = query + "INNER JOIN Competency ON ";
				query = query
						+ "tblAvgMean.CompetencyID = Competency.PKCompetency ";
				query = query + "WHERE tblAvgMean.SurveyID = " + surveyID
						+ " AND ";
				query = query + "tblAvgMean.TargetLoginID = " + targetID
						+ " AND tblAvgMean.Type = 1 AND ";
				query = query + "tblRatingTask.RatingCode = '" + RTCode
						+ "' GROUP BY tblAvgMean.CompetencyID, ";
				query = query
						+ "Competency.CompetencyName ORDER BY tblAvgMean.CompetencyID";
			}
		}

		Connection con = null;
		Statement st = null;
		ResultSet rs = null;

		Vector v = new Vector();

		try {
			con = ConnectionBean.getConnection();
			st = con.createStatement();
			rs = st.executeQuery(query);

			while (rs.next()) {
				String[] arr = new String[3];
				arr[0] = rs.getString(1);
				arr[1] = rs.getString(2);
				arr[2] = rs.getString(3);
				v.add(arr);
			}
		} catch (Exception ex) {
			System.out.println("SPFIndividualReport.java - CPCPRSorted - "
					+ ex.getMessage());
		} finally {
			ConnectionBean.closeRset(rs); // Close ResultSet
			ConnectionBean.closeStmt(st); // Close statement
			ConnectionBean.close(con); // Close connection
		}

		return v;
	}

	/**
	 * Retrieves target gaps based on the low, high value, and the type. Type: 1
	 * = Strength 2 = Meet Expectation 3 = Developmental Area
	 */
	public Vector getTargetGap(double low, double high, int type)
			throws SQLException {
		int surveyLevel = Integer.parseInt(surveyInfo[0]);
		String query = "";
		String filter = "";

		if (surveyLevel == 0) {
			switch (type) {
				case 1 :
					filter = " and tblGap.Gap >= " + low;
					break;
				case 2 :
					filter = " and tblGap.Gap > " + low + " and tblGap.Gap < "
							+ high;
					break;
				case 3 :
					filter = " and tblGap.Gap <= " + low;
					break;
			}

			query = query
					+ "SELECT tblGap.CompetencyID, Competency.CompetencyName, cast(tblGap.Gap as numeric(38,2)) as Result ";
			query = query
					+ "FROM tblGap INNER JOIN Competency ON tblGap.CompetencyID = Competency.PKCompetency ";
			query = query + "WHERE tblGap.SurveyID = " + surveyID
					+ " AND tblGap.TargetLoginID = " + targetID;
			query = query + filter;
			query = query + " ORDER BY Competency.CompetencyName";
		} else {
			switch (type) {
				case 1 :
					filter = " having CAST(AVG(tblGap.Gap) AS numeric(38, 2))  >= "
							+ low;
					break;
				case 2 :
					filter = " having CAST(AVG(tblGap.Gap) AS numeric(38, 2))  > "
							+ low
							+ " and CAST(AVG(tblGap.Gap) AS numeric(38, 2)) < "
							+ high;
					break;
				case 3 :
					filter = " having CAST(AVG(tblGap.Gap) AS numeric(38, 2))  <= "
							+ low;
					break;
			}

			query = query
					+ "SELECT tblGap.CompetencyID, Competency.CompetencyName, ";
			query = query
					+ "CAST(AVG(tblGap.Gap) AS numeric(38, 2)) AS Result ";
			query = query + "FROM tblGap INNER JOIN Competency ON ";
			query = query + "tblGap.CompetencyID = Competency.PKCompetency ";
			query = query + "WHERE tblGap.SurveyID = " + surveyID;
			query = query + " AND tblGap.TargetLoginID = " + targetID;
			query = query
					+ " GROUP BY tblGap.CompetencyID, Competency.CompetencyName ";
			query = query + filter;
			query = query + " ORDER BY Competency.CompetencyName";
		}

		Connection con = null;
		Statement st = null;
		ResultSet rs = null;
		Vector v = new Vector();

		try {
			con = ConnectionBean.getConnection();
			st = con.createStatement();
			rs = st.executeQuery(query);

			while (rs.next()) {
				String[] arr = new String[3];
				arr[0] = rs.getString(1);
				arr[1] = rs.getString(2);
				arr[2] = rs.getString(3);
				v.add(arr);
			}
		} catch (Exception ex) {
			System.out.println("SPFIndividualReport.java - getTargetGap - "
					+ ex.getMessage());
		} finally {
			ConnectionBean.closeRset(rs); // Close ResultSet
			ConnectionBean.closeStmt(st); // Close statement
			ConnectionBean.close(con); // Close connection
		}

		return v;
	}

	/**
	 * Retrieves rater results for each competency to calculate percentile in
	 * Normative Report.
	 */
	public Vector getOtherTargetResults(int ID, int compID) throws SQLException {
		String query = "";
		int surveyLevel = Integer.parseInt(surveyInfo[0]);
		int reliabilityCheck = C.ReliabilityCheck(surveyID);
		int type = 1;
		if (weightedAverage == true) {
			type = 10;
		}
		if (reliabilityCheck == 0) {
			query = query
					+ "SELECT tblTrimmedMean.CompetencyID, Competency.CompetencyName, tblTrimmedMean.TrimmedMean as Result ";
			query = query + "FROM tblTrimmedMean INNER JOIN tblRatingTask ON ";
			query = query
					+ "tblTrimmedMean.RatingTaskID = tblRatingTask.RatingTaskID ";
			query = query + "INNER JOIN Competency ON ";
			query = query
					+ "tblTrimmedMean.CompetencyID = Competency.PKCompetency ";
			query = query + "WHERE tblTrimmedMean.SurveyID = " + surveyID
					+ " AND ";
			query = query + "tblTrimmedMean.TargetLoginID = " + ID
					+ " AND tblTrimmedMean.Type = " + type + "  AND ";
			query = query
					+ "tblRatingTask.RatingCode = 'CP' and tblTrimmedMean.CompetencyID = "
					+ compID;
		} else {
			if (surveyLevel == 0) {
				query = query
						+ "SELECT tblAvgMean.CompetencyID, Competency.CompetencyName, tblAvgMean.AvgMean as Result ";
				query = query + "FROM tblAvgMean INNER JOIN tblRatingTask ON ";
				query = query
						+ "tblAvgMean.RatingTaskID = tblRatingTask.RatingTaskID ";
				query = query + "INNER JOIN Competency ON ";
				query = query
						+ "tblAvgMean.CompetencyID = Competency.PKCompetency ";
				query = query + "WHERE tblAvgMean.SurveyID = " + surveyID
						+ " AND ";
				query = query + "tblAvgMean.TargetLoginID = " + ID
						+ " AND tblAvgMean.Type =  " + type + "  AND ";
				query = query
						+ "tblRatingTask.RatingCode = 'CP' and tblAvgMean.CompetencyID = "
						+ compID;

			} else {
				query = query
						+ "SELECT tblAvgMean.CompetencyID, Competency.CompetencyName, ";
				query = query
						+ "CAST(AVG(tblAvgMean.AvgMean) AS numeric(38, 2)) AS Result ";
				query = query + "FROM tblRatingTask INNER JOIN tblAvgMean ON ";
				query = query
						+ "tblRatingTask.RatingTaskID = tblAvgMean.RatingTaskID ";
				query = query + "INNER JOIN Competency ON ";
				query = query
						+ "tblAvgMean.CompetencyID = Competency.PKCompetency ";
				query = query + "WHERE tblAvgMean.SurveyID = " + surveyID
						+ " AND ";
				query = query + "tblAvgMean.TargetLoginID = " + ID
						+ " AND tblAvgMean.Type =  " + type + " AND ";
				query = query
						+ "tblRatingTask.RatingCode = 'CP' and tblAvgMean.CompetencyID = "
						+ compID;
				query = query
						+ " GROUP BY tblAvgMean.CompetencyID, Competency.CompetencyName";
			}
		}
		// System.out.println("query1 "+query);
		Connection con = null;
		Statement st = null;
		ResultSet rs = null;
		Vector v = new Vector();

		try {
			con = ConnectionBean.getConnection();
			st = con.createStatement();
			rs = st.executeQuery(query);

			while (rs.next()) {
				String[] arr = new String[3];
				arr[0] = rs.getString(1);
				arr[1] = rs.getString(2);
				arr[2] = rs.getString(3);
				v.add(arr);
			}
		} catch (Exception ex) {
			System.out
					.println("SPFIndividualReport.java - getOtherTargetResults - "
							+ ex.getMessage());
		} finally {
			ConnectionBean.closeRset(rs); // Close ResultSet
			ConnectionBean.closeStmt(st); // Close statement
			ConnectionBean.close(con); // Close connection
		}

		return v;
	}

	/**
	 * Get total targets which status is completed included hinself
	 */
	public int TotalTargetsIncluded() throws SQLException {
		int total = 0;

		String query = "SELECT count(DISTINCT tblAssignment.TargetLoginID) as total FROM tblAssignment INNER JOIN ";
		query = query
				+ "[User] ON tblAssignment.TargetLoginID = [User].PKUser INNER JOIN ";
		query = query
				+ "[User] User_1 ON [User].Group_Section = User_1.Group_Section AND ";
		query = query + "tblAssignment.TargetLoginID = User_1.PKUser WHERE ";
		query = query + "tblAssignment.SurveyID = " + surveyID;
		query = query + " AND tblAssignment.RaterStatus <> 0";
		query += " AND tblAssignment.TargetLoginID IN (SELECT TargetLoginID FROM tblAssignment ";
		query += "WHERE SurveyID = " + surveyID
				+ " AND RaterStatus <> 0 AND RaterCode <> 'SELF')";

		Connection con = null;
		Statement st = null;
		ResultSet rs = null;

		try {
			con = ConnectionBean.getConnection();
			st = con.createStatement();
			rs = st.executeQuery(query);

			if (rs.next())
				total = rs.getInt(1);

		} catch (Exception ex) {
			System.out
					.println("SPFIndividualReport.java - totalTargetsIncluded - "
							+ ex.getMessage());
		} finally {
			ConnectionBean.closeRset(rs); // Close ResultSet
			ConnectionBean.closeStmt(st); // Close statement
			ConnectionBean.close(con); // Close connection
		}

		return total;
	}

	/**
	 * Get total targets which status is completed
	 */
	public int TotalTargets() throws SQLException {
		int total = 0;
		String query = "SELECT count(DISTINCT tblAssignment.TargetLoginID) as total FROM tblAssignment INNER JOIN ";
		query = query
				+ "[User] ON tblAssignment.TargetLoginID = [User].PKUser INNER JOIN ";
		query = query
				+ "[User] User_1 ON [User].Group_Section = User_1.Group_Section AND ";
		query = query + "tblAssignment.TargetLoginID = User_1.PKUser WHERE ";
		query = query + "tblAssignment.SurveyID = " + surveyID;
		query = query
				+ " AND tblAssignment.RaterStatus <> 0 AND tblAssignment.TargetLoginID <> "
				+ targetID;

		Connection con = null;
		Statement st = null;
		ResultSet rs = null;

		try {
			con = ConnectionBean.getConnection();
			st = con.createStatement();
			rs = st.executeQuery(query);

			if (rs.next())
				total = rs.getInt(1);

		} catch (Exception ex) {
			System.out.println("SPFIndividualReport.java - totalTargets - "
					+ ex.getMessage());
		} finally {
			ConnectionBean.closeRset(rs); // Close ResultSet
			ConnectionBean.closeStmt(st); // Close statement
			ConnectionBean.close(con); // Close connection
		}

		return total;
	}

	/**
	 * Retrieves all the comments input upon fill in the questionnaire.
	 * 
	 * @param raterCode
	 * @param compID
	 * @param KBID
	 * @return
	 * @throws SQLException
	 */
	public Vector getComments(String raterCode, int compID, int KBID)
			throws SQLException {
		String query = "";
		int surveyLevel = Integer.parseInt(surveyInfo[0]);

		if (surveyLevel == 0) {
			query = query
					+ "SELECT Competency.CompetencyName, tblComment.Comment, PKCompetency ";
			query = query + "FROM tblAssignment INNER JOIN tblComment ON ";
			query = query
					+ "tblAssignment.AssignmentID = tblComment.AssignmentID INNER JOIN ";
			query = query
					+ "[User] ON tblAssignment.TargetLoginID = [User].PKUser INNER JOIN ";
			query = query
					+ "Competency ON tblComment.CompetencyID = Competency.PKCompetency ";
			query = query + "WHERE tblAssignment.SurveyID = " + surveyID;
			query = query + " AND tblAssignment.TargetLoginID = " + targetID;
			query = query + " AND tblAssignment.RaterCode LIKE '" + raterCode
					+ "'";
			query = query + " AND Competency.PKCompetency = " + compID;
			query = query + " AND tblComment.Comment != ''"; // Added addition
																// condition to
																// filter out
																// competencies
																// that does not
																// have any
																// comments,
																// Sebastian 19
																// July 2010
			query = query + " ORDER BY tblComment.Comment";
		} else {
			query = query
					+ "SELECT Competency.CompetencyName, tblComment.Comment, KeyBehaviour.KeyBehaviour ";
			query = query + "FROM tblAssignment INNER JOIN tblComment ON ";
			query = query
					+ "tblAssignment.AssignmentID = tblComment.AssignmentID INNER JOIN ";
			query = query
					+ "[User] ON tblAssignment.TargetLoginID = [User].PKUser INNER JOIN ";
			query = query
					+ "Competency ON tblComment.CompetencyID = Competency.PKCompetency ";
			query = query + "INNER JOIN KeyBehaviour ON ";
			query = query
					+ "tblComment.KeyBehaviourID = KeyBehaviour.PKKeyBehaviour ";
			query = query + "WHERE tblAssignment.SurveyID = " + surveyID;
			query = query + " AND tblAssignment.TargetLoginID = " + targetID;
			query = query + " AND tblAssignment.RaterCode LIKE '" + raterCode
					+ "'";
			query = query + " AND Competency.PKCompetency = " + compID;
			query = query + " AND KeyBehaviour.PKKeyBehaviour = " + KBID;
			query = query + " AND tblComment.Comment != ''"; // Added addition
																// condition to
																// filter out
																// key
																// behaviours
																// that does not
																// have any
																// comments,
																// Sebastian 19
																// July 2010
			query = query + " ORDER BY tblComment.Comment";
		}

		Connection con = null;
		Statement st = null;
		ResultSet rs = null;
		Vector v = new Vector();

		try {
			con = ConnectionBean.getConnection();
			st = con.createStatement();
			rs = st.executeQuery(query);

			while (rs.next()) {
				String[] arr = new String[3];
				arr[0] = rs.getString(1);
				arr[1] = rs.getString(2);
				arr[2] = rs.getString(3);
				v.add(arr);
			}
		} catch (Exception ex) {
			System.out.println("SPFIndividualReport.java - getComments - "
					+ ex.getMessage());
		} finally {
			ConnectionBean.closeRset(rs); // Close ResultSet
			ConnectionBean.closeStmt(st); // Close statement
			ConnectionBean.close(con); // Close connection
		}

		return v;
	}

	/**
	 * Get target gap.
	 */
	public Vector getTargetGap() throws SQLException {
		String query = "";
		int surveyLevel = Integer.parseInt(surveyInfo[0]);

		if (surveyLevel == 0) {
			query = query
					+ "SELECT CompetencyID, Competency.CompetencyName, Gap FROM tblGap ";
			query += "INNER JOIN Competency ON tblGap.CompetencyID = Competency.PKCompetency WHERE ";
			query += "SurveyID = " + surveyID + " AND TargetLoginID = "
					+ targetID;
			query += " ORDER BY CompetencyID";
		} else {
			query = query
					+ "SELECT CompetencyID, Competency.CompetencyName, CAST(AVG(Gap) AS numeric(38, 2)) AS Gap ";
			query += "FROM tblGap INNER JOIN Competency ON tblGap.CompetencyID = Competency.PKCompetency WHERE SurveyID = "
					+ surveyID;
			query += " AND TargetLoginID = " + targetID;
			query += " GROUP BY CompetencyID, Competency.CompetencyName ORDER BY Competency.CompetencyName, tblGap.CompetencyID";
		}

		Connection con = null;
		Statement st = null;
		ResultSet rs = null;
		Vector v = new Vector();

		try {
			con = ConnectionBean.getConnection();
			st = con.createStatement();
			rs = st.executeQuery(query);

			while (rs.next()) {
				String[] arr = new String[3];
				arr[0] = rs.getString(1);
				arr[1] = rs.getString(2);
				arr[2] = rs.getString(3);
				v.add(arr);
			}
		} catch (Exception ex) {
			System.out.println("SPFIndividualReport.java - getTargetGap - "
					+ ex.getMessage());
		} finally {
			ConnectionBean.closeRset(rs); // Close ResultSet
			ConnectionBean.closeStmt(st); // Close statement
			ConnectionBean.close(con); // Close connection
		}

		return v;
	}

	public Vector getOtherTargetGap(int ID, int compID) throws SQLException {
		String query = "";
		int surveyLevel = Integer.parseInt(surveyInfo[0]);

		if (surveyLevel == 0) {
			query = query
					+ "SELECT CompetencyID, Competency.CompetencyName, Gap FROM tblGap ";
			query += "INNER JOIN Competency ON tblGap.CompetencyID = Competency.PKCompetency WHERE ";
			query += "SurveyID = " + surveyID + " AND TargetLoginID = " + ID
					+ " and CompetencyID = " + compID;
			query += " ORDER BY CompetencyID";
		} else {
			query = query
					+ "SELECT CompetencyID, Competency.CompetencyName, CAST(AVG(Gap) AS numeric(38, 2)) AS Gap ";
			query += "FROM tblGap INNER JOIN Competency ON tblGap.CompetencyID = Competency.PKCompetency WHERE SurveyID = "
					+ surveyID;
			query += " AND TargetLoginID = " + ID + " and CompetencyID = "
					+ compID;;
			query += " GROUP BY CompetencyID, Competency.CompetencyName ORDER BY Competency.CompetencyName, tblGap.CompetencyID";
		}

		Connection con = null;
		Statement st = null;
		ResultSet rs = null;
		Vector v = new Vector();

		try {
			con = ConnectionBean.getConnection();
			st = con.createStatement();
			rs = st.executeQuery(query);

			while (rs.next()) {
				String[] arr = new String[3];
				arr[0] = rs.getString(1);
				arr[1] = rs.getString(2);
				arr[2] = rs.getString(3);
				v.add(arr);
			}
		} catch (Exception ex) {
			System.out
					.println("SPFIndividualReport.java - getOtherTargetGap - "
							+ ex.getMessage());
		} finally {
			ConnectionBean.closeRset(rs); // Close ResultSet
			ConnectionBean.closeStmt(st); // Close statement
			ConnectionBean.close(con); // Close connection
		}

		return v;
	}

	/**
	 * Get all Targets' ID which status is completed
	 */
	public Vector TargetsID() {

		String query = "SELECT DISTINCT tblAssignment.TargetLoginID FROM tblAssignment INNER JOIN ";
		query = query
				+ "[User] ON tblAssignment.TargetLoginID = [User].PKUser INNER JOIN ";
		query = query
				+ "[User] User_1 ON [User].Group_Section = User_1.Group_Section AND ";
		query = query + "tblAssignment.TargetLoginID = User_1.PKUser WHERE ";
		query = query + "tblAssignment.SurveyID = " + surveyID;
		query = query
				+ " AND tblAssignment.RaterStatus <> 0 AND tblAssignment.TargetLoginID <> "
				+ targetID;

		// added in so that target who only completed by SELF does not counted
		// as complete.
		query += " AND RaterCode <> 'SELF' ";

		query = query + " ORDER BY tblAssignment.TargetLoginID";

		Connection con = null;
		Statement st = null;
		ResultSet rs = null;
		Vector v = new Vector();

		try {
			con = ConnectionBean.getConnection();
			st = con.createStatement();
			rs = st.executeQuery(query);

			while (rs.next()) {
				v.add(new Integer(rs.getInt(1)));
			}
		} catch (Exception ex) {
			System.out
					.println("SPFIndividualReport.java - getOtherTargetGap - "
							+ ex.getMessage());
		} finally {
			ConnectionBean.closeRset(rs); // Close ResultSet
			ConnectionBean.closeStmt(st); // Close statement
			ConnectionBean.close(con); // Close connection
		}

		return v;
	}

	/***************************************** SPREADSHEET ********************************************************/

	/**
	 * Initialize excel. For both customised and standard report
	 * 
	 * @param sSavedFileName
	 *            Name of the file when prompted to save
	 * @param sTemplateName
	 *            Name of the template of the excel file
	 * @throws IOException
	 * @throws Exception
	 */
	public void InitializeExcel(String sSavedFileName, String sTemplateName)
			throws IOException, Exception {
		System.out.println("2. Excel Initialisation Starts");

		storeURL = "file:///" + ST.getOOReportPath() + sSavedFileName;
		String templateURL = "file:///" + ST.getOOReportTemplatePath()
				+ sTemplateName;

		xRemoteServiceManager = OO
				.getRemoteServiceManager("uno:socket,host=localhost,port=2002;urp;StarOffice.ServiceManager");

		xDoc = OO.openDoc(xRemoteServiceManager,templateURL);

		// save as the template into a new file first. This is to avoid the
		// template being used.
		OO.storeDocComponent(xRemoteServiceManager,xDoc,storeURL);
		OO.closeDoc(xDoc);

		// open up the saved file and modify from there
		try {
			xDoc = OO.openDoc(xRemoteServiceManager,storeURL);
			xSpreadsheet = OO.getSheet(xDoc,"Individual Report");
			xSpreadsheet2 = OO.getSheet(xDoc,"Sheet2");
			xSpreadsheet3 = OO.getSheet(xDoc,"Item Frequency");
			xSpreadsheet5 = OO.getSheet(xDoc,"Group Ranking Table");
			xSpreadsheet4 = OO.getSheet(xDoc,"Cluster Ranking Table");
		} catch (Exception e) {
			// e.printStackTrace();
		}

		System.out.println("2. Excel Initialisation Completed");
	}

	public void InitializeExcelDevMap(String sSavedFileName,
			String sTemplateName) throws IOException, Exception {
		// System.out.println("2. Excel Initialisation Starts");

		storeURL = "file:///" + ST.getOOReportPath() + sSavedFileName;
		String templateURL = "file:///" + ST.getOOReportTemplatePath()
				+ sTemplateName;

		xRemoteServiceManager = OO
				.getRemoteServiceManager("uno:socket,host=localhost,port=8100;urp;StarOffice.ServiceManager");
		xDoc = OO.openDoc(xRemoteServiceManager,templateURL);

		// save as the template into a new file first. This is to avoid the
		// template being used.
		OO.storeDocComponent(xRemoteServiceManager,xDoc,storeURL);
		OO.closeDoc(xDoc);

		// open up the saved file and modify from there
		xDoc = OO.openDoc(xRemoteServiceManager,storeURL);

		xSpreadsheet0 = OO.getSheet(xDoc,"Cover");
		xSpreadsheet = OO.getSheet(xDoc,"Dev Map");
		xSpreadsheet2 = OO.getSheet(xDoc,"Quadrant Details");
	}

	/**
	 * Replace words with <> tags with another word
	 * 
	 * @throws Exception
	 * @throws IOException
	 */
	public void Replacement() throws Exception, IOException {
		// System.out.println("3. Replacement Starts");
		// System.out.println("1OK");
		int surveyLevel = Integer.parseInt(surveyInfo[0]);
		int cluster = Integer.parseInt(surveyInfo[9]);
		String after;
		String clusterOpt;
		// System.out.println("2OK");

		if (surveyLevel == 0) {
			after = "(Competency Level)";

			// if (ST.LangVer == 2) //Commented away to allow translation below,
			// Chun Yeong 1 Aug 2011
			// after = "(Tingkat Kompetensi)";

		} else {
			after = "(Key Behaviour Level)";
			// if (ST.LangVer == 2) //Commented away to allow translation below,
			// Chun Yeong 1 Aug 2011
			// after = "(Tingkat Perilaku Kunci)";
		}

		if (cluster == 0) {
			// remove clusterOpt for without cluster (Albert 17 July 2012)
			clusterOpt = "";
		} else {
			clusterOpt = "with Cluster";
		}

		// Allow dynamic translation, Chun Yeong 1 Aug 2011
		OO.findAndReplace(xSpreadsheet,"<Comp/KB Level>",
				trans.tslt(templateLanguage,after));
		OO.findAndReplace(xSpreadsheet,"<ClusterOption>",clusterOpt);
		OO.findAndReplace(xSpreadsheet,"<Job Position>",surveyInfo[1]);

		// Changed the default language to English by Chun Yeong 9 Jun 2011
		// Commented away to allow translation below, Chun Yeong 1 Aug 2011
		// if (ST.LangVer == 2) //Indonesian
		// OO.findAndReplace(xSpreadsheet, "<Target Name:>", "Nama Target: " +
		// UserName());
		// else // if (ST.LangVer == 1) English
		// OO.findAndReplace(xSpreadsheet, "<Target Name:>", "Target Name: " +
		// UserName());

		// Allow dynamic translation, Chun Yeong 1 Aug 2011
		OO.findAndReplace(xSpreadsheet,"<Target Name:>",
				trans.tslt(templateLanguage,"Feedback Recipient Name") + ": "
						+ UserName());
		System.out.println("3. Replacement Completed");
	}

	public void replacementDevelopmentMap() throws Exception, IOException {
		// System.out.println("3. Replacement Starts");

		OO.findAndReplace(xSpreadsheet0,"<target>",UserName());

		// Changed the default language to English by Chun Yeong 9 Jun 2011
		if (ST.LangVer == 2) { // Indonesian
			OO.findAndReplace(xSpreadsheet,"<competency>","Kompetensi");
			OO.findAndReplace(xSpreadsheet,"<behaviour>","Perilaku Kunci");
			OO.findAndReplace(xSpreadsheet,"<positive>","Selisih Positif");
			OO.findAndReplace(xSpreadsheet,"<negative>","Selisih Negatif");
		} else { // English
			OO.findAndReplace(xSpreadsheet,"<competency>","Competency");
			OO.findAndReplace(xSpreadsheet,"<behaviour>","Key Behaviour");
			OO.findAndReplace(xSpreadsheet,"<positive>","Positive Gap");
			OO.findAndReplace(xSpreadsheet,"<negative>","Negative Gap");
		}
	}

	/**
	 * Insert in the one legend in the indicated row and column
	 * 
	 * @author Qiao Li 22 Dec 2009
	 * @throws Exception
	 * 
	 */
	public void InsertIndividualProfileLegend(char num, String title,
			String desc, int row, int col) throws Exception {
		OO.insertString(xSpreadsheet,num + ".",row,column);
		OO.insertString(xSpreadsheet,trans.tslt(templateLanguage,title),row,
				column + 1);
		OO.mergeCells(xSpreadsheet,column + 1,endColumn,row,row);
		OO.insertString(xSpreadsheet,trans.tslt(templateLanguage,desc),row + 1,
				column + 1);
		OO.mergeCells(xSpreadsheet,column + 1,endColumn,row + 1,row + 1);
		OO.setRowHeight(xSpreadsheet,row,startColumn + 1,
				ROWHEIGHT * OO.countTotalRow(title,90));
		OO.setRowHeight(xSpreadsheet,row + 1,startColumn + 1,
				ROWHEIGHT * OO.countTotalRow(desc,105));
		OO.setFontSize(xSpreadsheet,0,11,row,row + 1,12);
	}

	/**
	 * Insert in the appropriate legends depending on whether there is CPR and
	 * whether split "Others" to "Subordinates" and "Peers"
	 * 
	 * @author Qiao Li 22 Dec 2009 precondition: the xSpreadsheet of the
	 *         template should contain string "<Profile Legend>"
	 */
	public void InsertProfileLegend() {
		int[] address;
		try {
			address = OO.findString(xSpreadsheet,"<Profile Legend>");
			column = address[0];
			row = address[1];
			char i = 'a';
			int j = 0;
			String desc = "";

			OO.findAndReplace(xSpreadsheet,"<Profile Legend>","");

			if (iNoCPR == 0) {// there is CPR
				desc = "This refers to the proficiency level that is required for the current job as perceived by all the other respondents.";
				InsertIndividualProfileLegend(i,
						"Current Proficiency Required - All (CPR - ALL)",desc,
						row + j,column);
				OO.setRowHeight(xSpreadsheet,row + j + 1,startColumn + 1,
						ROWHEIGHT * OO.countTotalRow(desc,90));
				i++;
				j += 3;
			}

			desc = "This refers to the Feedback Recipient's proficiency at the current moment, as perceived by all the respondents excluding self.";
			InsertIndividualProfileLegend(i,
					"Current Proficiency - All (CP - ALL)",desc,row + j,column);
			OO.setRowHeight(xSpreadsheet,row + j + 1,startColumn + 1,ROWHEIGHT
					* OO.countTotalRow(desc,90));
			i++;
			j += 3;

			if (iNoCPR == 0 && breakCPR == 1) { // there is CPR and breakdown of
												// CPR is ticked
				desc = "This refers to the proficiency level that is required for the current job as perceived by superior only.";
				InsertIndividualProfileLegend(
						i,
						"Current Proficiency Required - Superior (CPR - Superior)",
						desc,row + j,column);
				OO.insertRows(xSpreadsheet,startColumn,endColumn,row + j + 1,
						row + j + 4,3,1);
				i++;
				j += 3;
			}

			InsertIndividualProfileLegend(
					i,
					"Current Proficiency - Superior (CP - Superior)",
					"This refers to the Feedback Recipient's proficiency at the current moment, as perceived by superior only.",
					row + j,column);
			i++;
			j += 3;

			if (splitOthers == 0) {// No split of others
				if (iNoCPR == 0 && breakCPR == 1) { // there is CPR and
													// breakdown of CPR is
													// ticked
					InsertIndividualProfileLegend(
							i,
							"Current Proficiency Required - Others (CPR - Others)",
							"This refers to the proficiency level that is required for the current job as perceived by all the other respondents, excluding the input(s) of the Superior.",
							row + j,column);
					i++;
					j += 3;
				}

				InsertIndividualProfileLegend(
						i,
						"Current Proficiency - Others (CP - Others)",
						"This refers to the Feedback Recipient's proficiency as perceived by all the other respondents at the current moment, excluding the input(s) of the Superior.",
						row + j,column);
				i++;
				j += 3;

			}

			else {// split Others to Subordinates and Peers
				if (iNoCPR == 0 && breakCPR == 1) { // there is CPR and
													// breakdown of CPR is
													// ticked
					InsertIndividualProfileLegend(
							i,
							"Current Proficiency Required - Peers (CPR - Peers)",
							"This refers to the proficiency level that is required for the current job as perceived by peers only.",
							row + j,column);
					OO.insertRows(xSpreadsheet,startColumn,endColumn,row + j
							+ 1,row + j + 4,3,1);
					i++;
					j += 3;
				}

				InsertIndividualProfileLegend(
						i,
						"Current Proficiency - Peers (CP - Peers)",
						"This refers to the Feedback Recipient's proficiency at the current moment, as perceived by all the peers.",
						row + j,column);
				i++;
				j += 3;

				if (iNoCPR == 0 && breakCPR == 1) { // there is CPR and
													// breakdown of CPR is
													// ticked
					desc = "This refers to the proficiency level that is required for the current job as perceived by direct reports only.";
					InsertIndividualProfileLegend(
							i,
							"Current Proficiency Required - Direct Reports (CPR - Direct)",
							desc,row + j,column);
					OO.setRowHeight(xSpreadsheet,row + j + 1,startColumn + 1,
							ROWHEIGHT * OO.countTotalRow(desc,90));
					OO.insertRows(xSpreadsheet,startColumn,endColumn,row + j
							+ 2,row + j + 4,3,1);
					i++;
					j += 3;
				}

				InsertIndividualProfileLegend(
						i,
						"Current Proficiency - Direct Reports (CP - Direct)",
						"This refers to the Feedback Recipient's proficiency at the current moment, as perceived by all the direct reports.",
						row + j,column);
				i++;
				j += 3;

				if (iNoCPR == 0 && breakCPR == 1) { // there is CPR and
													// breakdown of CPR is
													// ticked
					InsertIndividualProfileLegend(
							i,
							"Current Proficiency Required - Indirect Reports(CPR - Indirect)",
							"This refers to the proficiency level that is required for the current job as perceived by indirect reports only.",
							row + j,column);
					OO.insertRows(xSpreadsheet,startColumn,endColumn,row + j
							+ 1,row + j + 4,3,1);
					i++;
					j += 3;
				}

				if (iNoCPR == 0 && breakCPR == 1 && splitOthers != 0) { // insert
																		// a
																		// page
																		// break
																		// so
																		// that
																		// it
																		// nicely
																		// start
																		// on a
																		// new
																		// page
					OO.insertPageBreak(xSpreadsheet,startColumn,endColumn,row
							+ j);
				}

				InsertIndividualProfileLegend(
						i,
						"Current Proficiency - Indirect Reports (CP - Indirect)",
						"This refers to the Feedback Recipient's proficiency at the current moment, as perceived by all the indirect reports.",
						row + j,column);
				i++;
				j += 3;
			}

			if (iNoCPR == 0 && breakCPR == 1) { // there is CPR and breakdown of
												// CPR is ticked
				InsertIndividualProfileLegend(
						i,
						"Current Proficiency Required - Self (CPR - Self)",
						"This refers to the proficiency level that is required for the current job as perceived by himself/herself.",
						row + j,column);
				OO.insertRows(xSpreadsheet,startColumn,endColumn,row + j + 1,
						row + j + 4,3,1);
				i++;
				j += 3;
			}

			InsertIndividualProfileLegend(
					i,
					"Current Proficiency - Self (CP - Self)",
					"This refers to the Feedback Recipient's proficiency as perceived by himself/herself.",
					row + j,column);
			i++;
			j += 3;

			if (iNoCPR == 0) {// There is CPR -> there is Gap
				// Change from CPR (All) - CP (All) to CP(All) to CPR(All), Mark
				// Oei 22 Mark 2010
				InsertIndividualProfileLegend(
						i,
						"Gap",
						"Gap = Current Proficiency (All) minus Current Proficiency Required (All)",
						row + j,column);
				i++;
				j += 3;

			}

			else if (CPRorFPR == 2) {
				InsertIndividualProfileLegend(i,"Gap",
						"Gap =  FPR (All) minus Current Proficiency (All)",row
								+ j,column);
				i++;
				j += 3;
			}

			// insert page break when cpr breakdown is ticked and others is not
			// split
			if (iNoCPR == 0 && breakCPR == 1 && splitOthers == 0) {
				// OO.insertPageBreak(xSpreadsheet, startColumn, endColumn, row+
				// j);
			}

			// Remove the following
			// "between all the raters' score given for that particular rating task."
			// after 'extent of agreement'.
			// Change wordings for clarify, Mark Oei 22 Mark 2010
			desc = "This refers to the extent of agreement amongst all the Current Proficiency scores for all the raters. The greater the agreement, the higher the percentage.";
			InsertIndividualProfileLegend(i,"Level Of Agreement",desc,row + j,
					column);
			OO.setRowHeight(xSpreadsheet,row + j + 1,startColumn + 1,ROWHEIGHT
					* OO.countTotalRow(desc,90));
			i++;
			j += 3;
			OO.deleteRows(xSpreadsheet,startColumn,endColumn,row + j + 1,row
					+ j + 6,5,1);
			// OO.insertPageBreak(xSpreadsheet, startColumn, endColumn, row+j);
		} catch (Exception e) {
			System.out.println("IndividualReport - InsertProfileLegend()");
			e.printStackTrace();
		}

	}

	/**
	 * Get Group Current Competency value of a specific competency from database
	 * base on surveyID and targetID
	 * 
	 * @param compName
	 *            the competency name
	 * @return Vector
	 * @throws SQLException
	 * @author Chun Yeong
	 * @since v1.3.12.108 //29 Jun 2011
	 */
	public double getGroupCPForOneCompetency(String compName)
			throws SQLException {
		String query = "";
		Double result = 0.0;
		int cpAll = 1;
		if (weightedAverage == true) {
			cpAll = 10;
		} else {
			cpAll = 1;
		}
		query = "SELECT Competency.PKCompetency AS CompetencyID, Competency.CompetencyName, "
				+ "ROUND(AVG(tblAvgMean.AvgMean),2) AS Result FROM tblAvgMean INNER JOIN Competency ON "
				+ "tblAvgMean.CompetencyID = Competency.PKCompetency INNER JOIN tblRatingTask ON "
				+ "tblAvgMean.RatingTaskID = tblRatingTask.RatingTaskID INNER JOIN [User] ON "
				+ "[User].PKUser = tblAvgMean.TargetLoginID "
				+ "WHERE Competency.CompetencyName = '"
				+ compName
				+ "' AND tblAvgMean.SurveyID = "
				+ surveyID
				+ " AND tblAvgMean.Type = "
				+ cpAll
				+ " AND tblRatingTask.RatingCode = 'CP' AND "
				+ "tblAvgMean.TargetLoginID IN "
				+ "(SELECT TargetLoginID FROM tblAssignment INNER JOIN [USER] ON [USER].PKUser = tblAssignment.TargetLoginID "
				+ "WHERE SurveyID = "
				+ surveyID
				+ " AND RaterCode <> 'SELF' AND RaterStatus IN (1, 2, 4) AND "
				+ "[USER].FKOrganization = (SELECT FKOrganization FROM [USER] WHERE PKUser = "
				+ targetID
				+ ")) "
				+ "GROUP BY tblAvgMean.SurveyID, Competency.PKCompetency, Competency.CompetencyName "
				+ "ORDER BY Competency.CompetencyName";

		Vector v = new Vector();
		Connection con = null;
		Statement st = null;
		ResultSet rs = null;

		try {
			con = ConnectionBean.getConnection();
			st = con.createStatement();
			rs = st.executeQuery(query);
			while (rs.next()) {
				result = Double.parseDouble(df.format(rs.getDouble("Result")));
			}
		} catch (Exception ex) {
			System.out
					.println("SPFIndividualReport.java - getGroupCPForOneCompetency - "
							+ ex.getMessage());
		} finally {
			ConnectionBean.closeRset(rs); // Close ResultSet
			ConnectionBean.closeStmt(st); // Close statement
			ConnectionBean.close(con); // Close connection
		}

		return result;
	}

	/**
	 * Get Group Current Competencies from database base on surveyID and
	 * targetID
	 * 
	 * @return Vector
	 * @throws SQLException
	 * @author Chun Yeong
	 * @since v1.3.12.96 //2 Jun 2011
	 */
	public Vector getGroupCP() throws SQLException {
		String query = "";
		int cpAll = 1;
		if (weightedAverage == true) {
			cpAll = 10;
		} else {
			cpAll = 1;
		}
		query = "SELECT Competency.PKCompetency AS CompetencyID, Competency.CompetencyName, "
				+ "ROUND(AVG(tblAvgMean.AvgMean),2) AS Result FROM tblAvgMean INNER JOIN Competency ON "
				+ "tblAvgMean.CompetencyID = Competency.PKCompetency INNER JOIN tblRatingTask ON "
				+ "tblAvgMean.RatingTaskID = tblRatingTask.RatingTaskID INNER JOIN [User] ON "
				+ "[User].PKUser = tblAvgMean.TargetLoginID "
				+ "WHERE tblAvgMean.SurveyID = "
				+ surveyID
				+ " AND tblAvgMean.Type = "
				+ cpAll
				+ " AND tblRatingTask.RatingCode = 'CP' AND "
				+ "tblAvgMean.TargetLoginID IN "
				+ "(SELECT TargetLoginID FROM tblAssignment INNER JOIN [USER] ON [USER].PKUser = tblAssignment.TargetLoginID "
				+ "WHERE SurveyID = "
				+ surveyID
				+ " AND RaterCode <> 'SELF' AND RaterStatus IN (1, 2, 4) AND "
				+ "[USER].FKOrganization = (SELECT FKOrganization FROM [USER] WHERE PKUser = "
				+ targetID
				+ ")) "
				+ "GROUP BY tblAvgMean.SurveyID, Competency.PKCompetency, Competency.CompetencyName "
				+ "ORDER BY Competency.CompetencyName";

		Vector v = new Vector();
		Connection con = null;
		Statement st = null;
		ResultSet rs = null;

		try {
			con = ConnectionBean.getConnection();
			st = con.createStatement();
			rs = st.executeQuery(query);
			while (rs.next()) {
				voRatingResult vo = new voRatingResult();
				vo.setCompetencyID(rs.getInt("CompetencyID"));
				vo.setCompetencyName(rs.getString("CompetencyName"));
				vo.setResult(Double.parseDouble(df.format(rs
						.getDouble("Result"))));

				v.add(vo);
			}
		} catch (Exception ex) {
			System.out.println("SPFIndividualReport.java - getGroupCP - "
					+ ex.getMessage());
		} finally {
			ConnectionBean.closeRset(rs); // Close ResultSet
			ConnectionBean.closeStmt(st); // Close statement
			ConnectionBean.close(con); // Close connection
		}

		return v;
	}

	/**
	 * Write CP versus CPR results to excel.
	 */
	public void InsertCPvsCPR() throws SQLException, IOException, Exception {

		System.out.println("4. CP Versus CPR Starts");

		int iNoOfRT = 0;
		String RTCode = "";
		Vector vComp = Competency(0);

		Vector RT = RatingTask();
		Vector CP = new Vector();
		Vector CPR = new Vector();
		Vector vGCP = new Vector(); // Added for Group Current Proficiency (GCP)
									// by Chun Yeong 2 Jun 2011
		CPRorFPR = 1; // 1=cpr, 2=fpr

		int[] address = OO.findString(xSpreadsheet,"<CP versus CPR Graph>");

		column = address[0];
		row = address[1];

		OO.findAndReplace(xSpreadsheet,"<CP versus CPR Graph>","");
		// OO.insertRows(xSpreadsheet, startColumn, endColumn, row, row+18, 18,
		// 1);
		// set as no CPR first (Qiao Li 21 Dec 2009)
		iNoCPR = 1;
		for (int i = 0; i < RT.size(); i++) {
			votblSurveyRating vo = (votblSurveyRating) RT.elementAt(i);

			RTCode = vo.getRatingCode();

			// System.out.println(RTCode);
			if (RTCode.equals("CP") || RTCode.equals("CPR")
					|| RTCode.equals("FPR")) {
				if (RTCode.equals("CP"))
					CP = CPCPR(RTCode);
				else {
					CPR = CPCPR(RTCode);
					if (RTCode.equals("CPR")) {
						CPRorFPR = 1;
						iNoCPR = 0; // CPR is chosen in this survey
					} else {
						CPRorFPR = 2;
					}
				}
			}
			iNoOfRT++;
		}

		// Chart Source Data
		// Vector vComp = CompetencyByName();
		// int total = totalCompetency(); // 1 for all
		// String title = "Current Proficiency Vs Required Proficiency";

		int r = row;
		int c = 0;

		OO.insertString(xSpreadsheet2,"CP",r,c + 1);
		if (CPRorFPR == 1) {
			// only insert CPR values when there is CPR.i.e do not insert when
			// all are 0s
			// Qiao Li 21 Dec 2009
			if (iNoCPR == 0) {
				OO.insertString(xSpreadsheet2,"CPR",r,c + 2);

				// Added, if there are CPR values, display/not display GCP on 3
				// column, by Chun Yeong 2 Jun 2011
				if (isGroupCPLine) {
					OO.insertString(xSpreadsheet2,"GCP",r,c + 3);
				}
			} else {
				// Added, if there are no CPR values, display/not display GCP on
				// 2 column, by Chun Yeong 2 Jun 2011
				if (isGroupCPLine) {
					OO.insertString(xSpreadsheet2,"GCP",r,c + 2);
				}
			}
		} else {
			OO.insertString(xSpreadsheet2,"FPR",r,c + 2);
			// title = "Current Proficiency Vs Future Required Proficiency";
		}

		r++;

		vGapSorted.clear();
		vGapUnsorted.clear();
		vCompID.clear();
		vCPValues.clear(); // clear the object, Mark Oei 16 April 2010

		double dCP = 0;
		double dCPR = 0;

		// Added for Group Current Proficiency (GCP) by Chun Yeong 2 Jun 2011
		double dGCP = 0;
		vGCP = getGroupCP();

		for (int i = 0; i < vComp.size(); i++) {

			voCompetency voComp = (voCompetency) vComp.elementAt(i);
			int compID = voComp.getCompetencyID();
			String statement = voComp.getCompetencyName();
			String compName = UnicodeHelper.getUnicodeStringAmp(statement);

			dCP = 0;
			dCPR = 0;
			dGCP = 0; // Added for Group Current Proficiency (GCP) by Chun Yeong
						// 2 Jun 2011

			if (CP.size() != 0 && i < CP.size()) {
				String arr[] = (String[]) CP.elementAt(i);
				dCP = Double.parseDouble(arr[2]);
			}

			if (CPR.size() != 0 && i < CPR.size()) {
				String arr[] = (String[]) CPR.elementAt(i);
				dCPR = Double.parseDouble(arr[2]);
			}

			// Added to insert values into Group Current Proficiency (GCP) by
			// Chun Yeong 2 Jun 2011
			if (vGCP.size() != 0 && i < vGCP.size()) {
				voRatingResult voGCP = (voRatingResult) vGCP.elementAt(i);
				dGCP = voGCP.getResult();
			}

			double gap = Math.round((dCP - dCPR) * 100.0) / 100.0;

			vCompID.add(new Integer(compID));
			vGapSorted.add(new String[]{compName, Double.toString(gap)});
			vGapUnsorted.add(new String[]{compName, Double.toString(gap)});
			vCPValues.add(new String[]{compName, Double.toString(dCP)}); // add
																			// competency
																			// name
																			// and
																			// CP
																			// values
																			// to
																			// object,
																			// Mark
																			// Oei
																			// 16
																			// April
																			// 2010

			// Added translation for the competency name, Chun Yeong 1 Aug 2011
			int compLocation = sortCompetency(compName);
			OO.insertString(xSpreadsheet2,getTranslatedCompetency(compName)
					.elementAt(0).toString(),r + compLocation,c);
			OO.insertNumeric(xSpreadsheet2,dCP,r + compLocation,c + 1);

			// only insert CPR values when there is CPR.i.e do not insert when
			// all are 0s
			// Qiao Li 21 Dec 2009
			if ((CPRorFPR == 1 && iNoCPR == 0) || CPRorFPR == 2) {
				OO.insertNumeric(xSpreadsheet2,dCPR,r,c + 2);

				// Added, if there are CPR values, display/not display GCP on 3
				// column, by Chun Yeong 2 Jun 2011
				if (isGroupCPLine) {
					OO.insertNumeric(xSpreadsheet2,dGCP,r + compLocation,c + 3);
				}
			} else {
				// Added, if there are no CPR values, display/not display GCP on
				// 2 column, by Chun Yeong 2 Jun 2011
				if (isGroupCPLine) {
					OO.insertNumeric(xSpreadsheet2,dGCP,r + compLocation,c + 2);
				}
			}

			// r++;
		}

		// String xAxis = "Competencies";
		// String yAxis = "Rating";
		// if (ST.LangVer == 2) {
		// xAxis = "Kompetensi";
		// yAxis = "Penilaian";
		// }

		/*
		 * rianto //draw chart OO.setFontSize(8); //XTableChart xtablechart =
		 * OO.getChart(xSpreadsheet, xSpreadsheet2, 0, c+2, row, r-1,
		 * "Executive", 13000, 9000, row, 1); XTableChart xtablechart =
		 * OO.getChart(xSpreadsheet, xSpreadsheet2, 0, c+2, row, r-1,
		 * "Executive", 15500, 11500, row, 1); xtablechart =
		 * OO.setChartTitle(xtablechart, title);
		 * OO.setChartProperties(xtablechart, false, true, true, true, true);
		 * xtablechart = OO.setAxes(xtablechart, xAxis, yAxis, 10, 1, 4500, 0);
		 * 
		 * //need to change to LineDiagram and set the scale of xAxis, and also
		 * the width of the line
		 * OO.changeChartType("com.sun.star.chart.LineDiagram", xtablechart);
		 */
		// set the Y Axis to the maximum scale of the survey rating Qiao Li 23
		// Dec 2009
		double maxScale = this.MaxScale();
		XTableChart chart = OO.changeAxesMax(xSpreadsheet,0,-1,maxScale);
		OO.changeAxesMin(chart,0,3.5);
		// OO.drawGridLines(chart, 0);
		// chart =
		// OO.setAxesMin(chart,"Competencies","Rating",maxScale,3.0,1.0,0,0);

		// only draw chart for CPR values when there is CPR.(iNoCPR == 0)
		// Qiao Li 21 Dec 2009
		if ((CPRorFPR == 1 && iNoCPR == 0) || CPRorFPR == 2) {
			// dynamically change the description about "CP versus CPR" graph
			// Qiao Li 23 Dec 2009
			// Added, if Group CP is checked/not check, display respective
			// columns, by Chun Yeong 2 Jun 2011
			if (!isGroupCPLine) {
				OO.findAndReplace(
						xSpreadsheet,
						"<Graph Description>",
						trans.tslt(templateLanguage,
								"Current Proficiency versus the Required Proficiency"));
				OO.setSourceData(xSpreadsheet,xSpreadsheet2,0,0,c + 2,r - 1,
						r + 11);
			} else {
				OO.findAndReplace(
						xSpreadsheet,
						"<Graph Description>",
						trans.tslt(templateLanguage,
								"Current Proficiency versus the Required Proficiency")
								+ ". "
								+ trans.tslt(
										templateLanguage,
										"The mean CP score for the entire group of Feedback Recipients for each competency is also presented in the graph (Group Current Proficiency)"));
				OO.setSourceData(xSpreadsheet,xSpreadsheet2,0,0,c + 3,r - 1,
						r + 11);
			}

			OO.insertPageBreak(xSpreadsheet,startColumn,endColumn,row + 1);
		} else {// CPR are all 0s
				// dynamically change the description about "CP versus CPR"
				// graph Qiao Li 23 Dec 2009
				// Added, if Group CP is checked/not check, display respective
				// columns, by Chun Yeong 2 Jun 2011
			if (!isGroupCPLine) {
				OO.findAndReplace(xSpreadsheet,"<Graph Description>",
						trans.tslt(templateLanguage,"Current Proficiency"));
				OO.setSourceData(xSpreadsheet,xSpreadsheet2,0,0,c + 1,r - 1,
						r + 11);
			} else {
				OO.findAndReplace(
						xSpreadsheet,
						"<Graph Description>",
						trans.tslt(templateLanguage,"Current Proficiency")
								+ ". "
								+ trans.tslt(
										templateLanguage,
										"The mean CP score for the entire group of Feedback Recipients for each competency is also presented in the graph (Group Current Proficiency)"));
				OO.setSourceData(xSpreadsheet,xSpreadsheet2,0,0,c + 2,r - 1,
						r + 11);
			}

			// Change the title of the xtablechart (Qiao Li 22 Dec 2009)
			OO.changeChartTitle(chart,
					trans.tslt(templateLanguage,"Current Proficiency"));

		}
		if (iNoCPR != 1)
			OO.insertPageBreak(xSpreadsheet,startColumn,endColumn,row);
		System.out.println("4. CP Versus CPR Completed");
	}

	/**
	 * Write target gap results to excel worksheet.
	 */

	// ****Added by Tracy 26 aug 08************************
	// Add dynamic title to Individual report

	public void InsertGapTitleCluster(int surveyID) throws SQLException,
			IOException, Exception {
		System.out.println("5.1 Gap Title Insertion Starts");

		int[] address = OO.findString(xSpreadsheet,"<Gap Title Cluster>");

		OO.findAndReplace(xSpreadsheet,"<Gap Title Cluster>","");

		column = address[0];
		row = address[1];

		int i = 0;
		Vector RTaskID = new Vector();
		Vector RTaskName = new Vector();

		// add to get the upper and lower cp limit for display
		// Mark Oei 16 April 2010
		double MinMaxGap[] = getMinMaxGap();
		double low = MinMaxGap[0];
		double high = MinMaxGap[1];

		// Get Rating from database according to s urvey ID
		String query = "SELECT a.RatingTaskID as RTaskID, b.RatingTask as RTaskName FROM tblSurveyRating a ";
		query += "INNER JOIN tblRatingTask b ON a.RatingTaskID=b.RatingTaskID  WHERE a.SurveyID = "
				+ surveyID;

		Connection con = null;
		Statement st = null;
		ResultSet rs = null;

		try {
			con = ConnectionBean.getConnection();
			st = con.createStatement();
			rs = st.executeQuery(query);

			while (rs.next()) {
				RTaskID.add(i,new Integer(rs.getInt("RTaskID")));
				RTaskName.add(i,new String(rs.getString("RTaskName")));
				i++;
			}

			// Check CPR or FPR
			String pType = "";
			String CPR = "";
			String CP = "";
			String FPR = "";

			for (int n = 0; RTaskID.size() - 1 >= n; n++) {
				if (((Integer) RTaskID.elementAt(n)).intValue() == 1) {
					CP = RTaskName.elementAt(n).toString();
				} else if (((Integer) RTaskID.elementAt(n)).intValue() == 2) {
					CPR = RTaskName.elementAt(n).toString();
					pType = "C";
				} else if (((Integer) RTaskID.elementAt(n)).intValue() == 3) {
					FPR = RTaskName.elementAt(n).toString();
					pType = "F";
				}
			}
			// changed by Hemilda 15/09/2009 change word add (All) and make it
			// fit the width of column
			String title = "";
			String info = "";
			if (templateNameSPF == false) {
				info = "The table below shows the competency scores of the Feedback Recipient against the group average in accordance to the clusters.";
			} else {
				info = "The table below shows the competency scores of the Feedback Recipient against the group average in accordance to the clusters.";
			}
			if (pType.equals("C")) {
				// added to display the information
				// Mark Oei 16 April 2010
				title = "Gap = " + CP + " (All) minus " + CPR + " (All)"; // :
																			// Strengths
																			// and
																			// Development
																			// Areas
																			// Report";
			} else if (pType.equals("F")) {
				title = "Gap = " + FPR + " (All) minus " + CP + " (All)"; // :
																			// Strengths
																			// and
																			// Development
																			// Areas
																			// Report";
			} else {
				title = "CP = " + CP
						+ " (All), GCP = Group Current Proficiency";
			}

			String gapTitle = "";
			// Insert title to excel file

			OO.insertString(xSpreadsheet,info,row,0);// added to display
														// info
														// programmatically,
														// Mark Oei 16 April
														// 2010
			OO.insertString(xSpreadsheet,gapTitle,row - 1,0);
			OO.setFontBold(xSpreadsheet,startColumn,endColumn,row - 1,row - 1);
			OO.insertString(xSpreadsheet,trans.tslt(templateLanguage,title),
					row + 1,0);
			OO.mergeCells(xSpreadsheet,startColumn,endColumn,row,row);
			OO.setRowHeight(xSpreadsheet,row - 3,1,
					ROWHEIGHT * OO.countTotalRow(info,90)); // added to allow
															// auto-increment
															// of row
															// height, Mark
															// Oei 16 April
															// 2010
			OO.setRowHeight(xSpreadsheet,row,1,
					ROWHEIGHT * OO.countTotalRow(title,90));

		} catch (Exception E) {
			System.err
					.println("SPFIndividualReport.java - InsertGapTitleCluster - "
							+ E);
		} finally {
			ConnectionBean.closeRset(rs); // Close ResultSet
			ConnectionBean.closeStmt(st); // Close statement
			ConnectionBean.close(con); // Close connection

		}
	}

	public void InsertGapTitle(int surveyID) throws SQLException, IOException,
			Exception {
		// System.out.println("5.1 Gap Title Insertion Starts");

		int[] address = OO.findString(xSpreadsheet,"<Gap Title>");

		OO.findAndReplace(xSpreadsheet,"<Gap Title>","");

		column = address[0];
		row = address[1];
		// OO.insertPageBreak(xSpreadsheet, startColumn, endColumn, row);
		int i = 0;
		Vector RTaskID = new Vector();
		Vector RTaskName = new Vector();

		// add to get the upper and lower cp limit for display
		// Mark Oei 16 April 2010
		double MinMaxGap[] = getMinMaxGap();
		double low = MinMaxGap[0];
		double high = MinMaxGap[1];

		// Get Rating from database according to s urvey ID
		String query = "SELECT a.RatingTaskID as RTaskID, b.RatingTask as RTaskName FROM tblSurveyRating a ";
		query += "INNER JOIN tblRatingTask b ON a.RatingTaskID=b.RatingTaskID  WHERE a.SurveyID = "
				+ surveyID;

		Connection con = null;
		Statement st = null;
		ResultSet rs = null;

		try {
			con = ConnectionBean.getConnection();
			st = con.createStatement();
			rs = st.executeQuery(query);

			while (rs.next()) {
				RTaskID.add(i,new Integer(rs.getInt("RTaskID")));
				RTaskName.add(i,new String(rs.getString("RTaskName")));
				i++;
			}

			// Check CPR or FPR
			String pType = "";
			String CPR = "";
			String CP = "";
			String FPR = "";

			for (int n = 0; RTaskID.size() - 1 >= n; n++) {
				if (((Integer) RTaskID.elementAt(n)).intValue() == 1) {
					CP = RTaskName.elementAt(n).toString();
				} else if (((Integer) RTaskID.elementAt(n)).intValue() == 2) {
					CPR = RTaskName.elementAt(n).toString();
					pType = "C";
				} else if (((Integer) RTaskID.elementAt(n)).intValue() == 3) {
					FPR = RTaskName.elementAt(n).toString();
					pType = "F";
				}
			}
			// changed by Hemilda 15/09/2009 change word add (All) and make it
			// fit the width of column
			String title = "";
			String info = "";
			if (pType.equals("C")) {
				// added to display the information
				// Mark Oei 16 April 2010
				info = trans
						.tslt(templateLanguage,
								"The table below indicates the Feedback Recipient strengths and areas for development.")
						+ " ";
				info += trans
						.tslt(templateLanguage,
								"For competencies where the gap is positive, these are the Feedback Recipient strength")
						+ " ";
				info += trans
						.tslt(templateLanguage,
								"For competencies where the gap is negative, these are areas where the Feedback Recipient requires development.");
				title = "Gap = " + CP + " (All) minus " + CPR + " (All)"; // :
																			// Strengths
																			// and
																			// Development
																			// Areas
																			// Report";
			} else if (pType.equals("F")) {
				info = trans
						.tslt(templateLanguage,
								"The table below indicates the Feedback Recipient strengths and areas for development.")
						+ " ";
				info += trans
						.tslt(templateLanguage,
								"For competencies where the gap is positive, these are the Feedback Recipient strengths.")
						+ "";
				info += trans
						.tslt(templateLanguage,
								"For competencies where the gap is negative, these are areas where the Feedback Recipient requires development.");
				title = "Gap = " + FPR + " (All) minus " + CP + " (All)"; // :
																			// Strengths
																			// and
																			// Development
																			// Areas
																			// Report";
			} else {
				info = trans
						.tslt(templateLanguage,
								"The table below indicates the Feedback Recipient strengths and areas for development.")
						+ " ";
				info += trans.tslt(templateLanguage,
						"For competencies where the CP is higher than")
						+ " "
						+ high
						+ trans.tslt(templateLanguage,
								", these are the Feedback Recipient strengths.")
						+ " ";
				info += trans.tslt(templateLanguage,
						"For competencies where the CP is lower than")
						+ " "
						+ low
						+ trans.tslt(templateLanguage,
								", these are areas where the Feedback Recipient requires development.");
				title = "CP = " + CP
						+ " (All), GCP = Group Current Proficiency";
			}

			// Insert title to excel file
			OO.insertString(xSpreadsheet,info,row,0);// added to display info
														// programmatically,
														// Mark Oei 16 April
														// 2010
			OO.justify(xSpreadsheet,0,11,row,row);
			OO.insertString(xSpreadsheet,trans.tslt(templateLanguage,title),
					row,0);
			OO.mergeCells(xSpreadsheet,0,10,row + 3,row + 3);
			OO.setRowHeight(xSpreadsheet,row,1,
					ROWHEIGHT * OO.countTotalRow(info,90)); // added to allow
															// auto-increment
															// of row
															// height, Mark
															// Oei 16 April
															// 2010
			OO.setRowHeight(xSpreadsheet,row + 3,1,
					ROWHEIGHT * OO.countTotalRow(title,90));
			OO.insertRows(xSpreadsheet,startColumn,endColumn,row + 3,row + 3,1,
					1);

		} catch (Exception E) {
			System.err.println("SPFIndividualReport.java - InsertGapTitle - "
					+ E);
		} finally {
			ConnectionBean.closeRset(rs); // Close ResultSet
			ConnectionBean.closeStmt(st); // Close statement
			ConnectionBean.close(con); // Close connection

		}
	}

	// ***End Tracy edit 26 aug 08****************************

	public void InsertGapCluster() throws SQLException, IOException, Exception {
		// System.out.println("5.2 Gap Insertion Starts");

		// Added to instantiate a local variable called xSpreadsheet2 to make it
		// common with Group Report.java
		// Mark Oei 25 Mar 2010
		XSpreadsheet xSpreadsheet2 = OO.getSheet(xDoc,"Sheet2");

		int[] address = OO.findString(xSpreadsheet,"<Gap Cluster>");

		column = address[0];
		row = address[1];

		int c = 0;
		// Added to define columns where CP, CPR, Gap are inserted in
		// spreadsheet
		// Mark Oei 25 Mar 2010
		int gapCol = 11;
		int cprCol = 10;
		int cpCol = 9;

		// To include gcp column, Chun Yeong 29 Jun 2011
		int gcpCol = 11;
		if (isGroupCPLine) {
			gapCol = 10;
			cprCol = 9;
			cpCol = 8;
		}

		int[] cpAddress;
		double cpValue = 0.0;
		double cprValue = 0.0;

		vGapSorted = G.sorting(vGapSorted,1);

		OO.findAndReplace(xSpreadsheet,"<Gap Cluster>","");

		double MinMaxGap[] = getMinMaxGap();

		double low = MinMaxGap[0];
		double high = MinMaxGap[1];
		// int type = 2; // 1 is >=, 2 is >x>, 3 is <

		// Added for Group Current Proficiency (GCP) by Chun Yeong 29 Jun 2011
		double gcpValue = 0.0;
		Vector vGCP = new Vector();

		if (iNoCPR == 0) // If CPR is chosen in this survey
		{
			String title = "COMPETENCY SCORES BY CLUSTER";

			OO.insertString(xSpreadsheet,trans.tslt(templateLanguage,title),
					row - 4,c);
			OO.setFontBold(xSpreadsheet,startColumn,endColumn,row,row);
			OO.setFontSize(xSpreadsheet,startColumn,endColumn,row,row,16);
			row++;
			OO.insertRows(xSpreadsheet,startColumn,endColumn,row,row + 2,2,1);

			int startBorder = row;
			int endBorder;
			Vector v = ClusterByName();
			for (int i = 0; i < v.size(); i++) {
				voCluster vCluster = (voCluster) v.elementAt(i);
				String clusterName = vCluster.getClusterName();
				int clusterID = vCluster.getClusterID();
				// Allow dynamic translation, Chun Yeong 1 Aug 2011
				OO.setFontSize(xSpreadsheet,9,11,row,row,12);
				OO.insertString(xSpreadsheet,clusterName,row,c);
				OO.insertString(xSpreadsheet,"CP",row,cpCol);
				OO.insertString(xSpreadsheet,"CPR",row,cprCol);

				OO.insertString(xSpreadsheet,
						trans.tslt(templateLanguage,"Gap"),row,gapCol);
				if (isGroupCPLine)
					OO.insertString(xSpreadsheet,"GCP",row,gcpCol); // Added
																	// a new
																	// column
																	// for
																	// GCP
																	// by
																	// Chun
																	// Yeong
																	// 29
																	// Jun
																	// 2011
				OO.setFontSize(xSpreadsheet,9,11,row,row,12);
				OO.setFontBold(xSpreadsheet,startColumn,endColumn,row,row);
				OO.setFontSize(xSpreadsheet,cpCol,gcpCol,row,row,12);
				OO.setCellAllignment(xSpreadsheet,cpCol,gcpCol,row,row,1,2);
				OO.setBGColor(xSpreadsheet,startColumn,endColumn - 1,row,row,
						BGCOLORCLUSTER);
				row++;

				Vector<String> compList = new Vector<String>();
				Vector vComp = ClusterCompetencyByName(clusterID);
				for (int k = 0; k < vComp.size(); k++) {
					voCompetency vCompetency = (voCompetency) vComp
							.elementAt(k);
					compList.add(vCompetency.getCompetencyName());
				}

				for (int j = 0; j < vGapSorted.size(); j++) {
					double gap = Double.valueOf(
							((String[]) vGapSorted.elementAt(j))[1])
							.doubleValue();
					String compName = ((String[]) vGapSorted.elementAt(j))[0];

					if (compList.contains(compName)) {
						OO.insertRows(xSpreadsheet,startColumn,endColumn,row,
								row + 1,1,1);

						// Added translation for the competency name, Chun Yeong
						// 1 Aug 2011
						OO.insertString(xSpreadsheet,
								getTranslatedCompetency(compName).elementAt(0)
										.toString(),row,c);
						OO.unWrapText(xSpreadsheet,c,endColumn,row,row);
						OO.insertNumeric(xSpreadsheet,gap,row,gapCol);

						// Insert CP and CPR values next to Gap, Mark Oei 25 Mar
						// 2010
						// Added translation for the competency name, Chun Yeong
						// 1 Aug 2011
						cpAddress = OO.findString(xSpreadsheet2,
								getTranslatedCompetency(compName).elementAt(0)
										.toString());
						cpValue = OO.getCellValue(xSpreadsheet2,cpAddress[1],
								cpAddress[0] + 1);
						cprValue = OO.getCellValue(xSpreadsheet2,cpAddress[1],
								cpAddress[0] + 2);
						OO.insertNumeric(xSpreadsheet,cpValue,row,cpCol);
						OO.insertNumeric(xSpreadsheet,cprValue,row,cprCol);

						// Insert GCP values next to Gap, Chun Yeong 29 Jun 2011
						if (isGroupCPLine) {
							gcpValue = getGroupCPForOneCompetency(compName
									.trim());
							OO.insertNumeric(xSpreadsheet,gcpValue,row,gcpCol);
						}

						OO.setFontSize(xSpreadsheet,cpCol,gcpCol,row,row,12);
						row++;
					}
				}
				row++;
				endBorder = row;
				OO.setTableBorder(xSpreadsheet,startColumn,endColumn - 1,
						startBorder,endBorder,false,false,true,true,true,true);
				// Add border lines from cpCol to gcpCol
				// Mark Oei 25 Mar 2010
				OO.setTableBorder(xSpreadsheet,cpCol,endColumn - 1,startBorder,
						endBorder,true,false,true,true,false,false);

				startBorder = endBorder;
			}
			row++;
			row++;
			OO.insertPageBreak(xSpreadsheet,startColumn,endColumn,row);

		} else { // if no CPR is chosen

			vCPValues = G.sorting(vCPValues,1); // added to sort CPValues, Mark
												// Oei 16 April 2010
			String title = "COMPETENCY SCORES BY CLUSTER";

			// Allow dynamic translation, Chun Yeong 1 Aug 2011
			OO.insertString(xSpreadsheet,trans.tslt(templateLanguage,title),
					row - 4,c);
			OO.setFontSize(xSpreadsheet,startColumn,endColumn,row - 4,row - 4,
					16);
			OO.setFontBold(xSpreadsheet,startColumn,endColumn,row - 4,row - 4);

			row++;
			OO.insertRows(xSpreadsheet,startColumn,endColumn,row,row + 2,2,1);

			int startBorder = row + 1;
			int endBorder;
			cpCol = 11;
			// shift the cp column to the left 1 to allow space for GCP column.
			if (isGroupCPLine) {
				cpCol = 10;
			}
			OO.setFontSize(xSpreadsheet,9,11,row,row,12);
			OO.insertString(xSpreadsheet,"CP",row,cpCol);
			OO.insertString(xSpreadsheet,"GCP",row,gcpCol);
			row++;
			Vector v = ClusterByName();

			for (int i = 0; i < v.size(); i++) {
				voCluster vCluster = (voCluster) v.elementAt(i);
				String clusterName = vCluster.getClusterName();

				int clusterID = vCluster.getClusterID();
				int clusterNameRow = row;
				double totalCP = 0;
				double totalGCP = 0;
				// Allow dynamic translation, Chun Yeong 1 Aug 2011
				OO.insertString(xSpreadsheet,clusterName,row,c);

				// OO.insertString(xSpreadsheet, "CP", row, cpCol);
				if (isGroupCPLine)
					// OO.insertString(xSpreadsheet, "GCP", row, gcpCol); //
					// Added
					// a new
					// column
					// for
					// GCP
					// by
					// Chun
					// Yeong
					// 29
					// Jun
					// 2011

					OO.setFontBold(xSpreadsheet,startColumn,endColumn,row,row);
				OO.setFontSize(xSpreadsheet,cpCol - 1,gcpCol,row,row,12);
				OO.setCellAllignment(xSpreadsheet,cpCol - 1,gcpCol,row,row,1,2);
				OO.setBGColor(xSpreadsheet,startColumn,endColumn - 1,row,row,
						BGCOLORCLUSTER);

				row++;

				Vector vComp = ClusterCompetencyByName(clusterID);
				/*
				 * for (int k = 0; k < vComp.size(); k++) { voCompetency
				 * vCompetency = (voCompetency) vComp .elementAt(k);
				 * compList.add(vCompetency.getCompetencyName()); }
				 */
				String[] compList = sortClusterCompetencyOrder(clusterName);
				int numOfComp = 0;

				for (int k = 0; k < compList.length; k++) {
					double cpValues = 0;
					for (int j = 0; j < vCPValues.size(); j++) {

						String compName = ((String[]) vCPValues.elementAt(j))[0];
						if (compName.equalsIgnoreCase(compList[k])) {

							cpValues = Double.valueOf(
									((String[]) vCPValues.elementAt(j))[1])
									.doubleValue();
						}
					}
					OO.insertRows(xSpreadsheet,startColumn,endColumn,row,
							row + 1,1,1);

					// Added translation for competency name, Chun Yeong
					// 1
					// Aug 2011
					if (compList[k] != null) {
						OO.insertString(xSpreadsheet,compList[k].toString(),
								row,c);
						OO.setRowHeight(
								xSpreadsheet,
								row,
								1,
								ROWHEIGHT
										* OO.countTotalRow(
												compList[k].toString(),90));
						OO.insertNumeric(xSpreadsheet,cpValues,row,cpCol);

						// Insert GCP values next to Gap, Chun Yeong 29 Jun
						// 2011
						if (isGroupCPLine) {
							gcpValue = getGroupCPForOneCompetency(compList[k]
									.toString().trim());
							OO.insertNumeric(xSpreadsheet,gcpValue,row,gcpCol);
						}

						OO.setFontSize(xSpreadsheet,cpCol - 1,gcpCol,row,row,12);
						totalCP += cpValues;
						totalGCP += gcpValue;
						row++;
						numOfComp++;
					}
				}

				OO.insertRows(xSpreadsheet,startColumn,endColumn,row,row + 1,1,
						1);

				OO.insertNumeric(xSpreadsheet,
						Double.parseDouble(df.format(totalCP / numOfComp)),
						clusterNameRow,cpCol);
				OO.insertNumeric(xSpreadsheet,
						Double.parseDouble(df.format(totalGCP / numOfComp)),
						clusterNameRow,gcpCol);
				OO.setCellAllignment(xSpreadsheet,10,11,startBorder - 1,row,1,2);
				row++;

				endBorder = row;
				OO.setTableBorder(xSpreadsheet,startColumn,endColumn - 1,
						startBorder,endBorder - 1,false,false,true,true,true,
						true);
				// Add border lines from cpCol to gcpCol
				// Mark Oei 25 Mar 2010
				OO.setTableBorder(xSpreadsheet,cpCol,endColumn - 1,startBorder,
						endBorder - 1,true,false,true,true,false,false);

				startBorder = endBorder;

			}

			row++;
			row++;
		}
		// OO.insertPageBreak(xSpreadsheet, startColumn, endColumn, row);
	}// end else

	// System.out.println("5. Gap Completed");

	public String[] sortClusterCompetencyOrder(String clusterName) {
		String[] v = new String[3];

		if (clusterName.equalsIgnoreCase("SELF")) {
			v[0] = "Personal Mastery";
		} else if (clusterName.equalsIgnoreCase("ACTION")) {
			v[0] = "Planning and Prioritising";
			v[1] = "Focusing on Mission and Vision";
			v[2] = "Facilitating Change";
		} else if (clusterName.equalsIgnoreCase("DELIBERATIONS")) {
			v[0] = "Solving Problems";
			v[1] = "Systems Thinking";
			v[2] = "Creating and Innovating";
		} else if (clusterName.equalsIgnoreCase("EMPLOYEES")) {
			v[0] = "Unleashing People Potential";
			v[1] = "Performing as a Team";
		} else if (clusterName.equalsIgnoreCase("RELATIONSHIP BUILDING")) {
			v[0] = "Using Heartskills";
			v[1] = "Communicating Effectively";
			v[2] = "Building Partnerships";
		}

		return v;
	}

	public Vector sortClusterCompetencyOrder(Vector vComp) {
		Vector v = new Vector(3);
		HashMap<Integer, voCompetency> hm1 = new HashMap<Integer, voCompetency>();

		for (int i = 0; i < vComp.size(); i++) {

			voCompetency voComp = (voCompetency) vComp.elementAt(i);
			String competency = voComp.getCompetencyName();

			if (competency.equalsIgnoreCase("Personal mastery")
					|| competency.equalsIgnoreCase("Planning and Prioritising")
					|| competency.equalsIgnoreCase("Solving Problems")
					|| competency
							.equalsIgnoreCase("Unleashing People Potential")
					|| competency.equalsIgnoreCase("Using HeartSkills")) {
				hm1.put(1,voComp);
			} else if (competency
					.equalsIgnoreCase("Focusing on Mission and Vision")
					|| competency.equalsIgnoreCase("Systems Thinking")
					|| competency.equalsIgnoreCase("Performing as a Team")
					|| competency.equalsIgnoreCase("Communicating Effectively")) {
				hm1.put(2,voComp);
			} else if (competency.equalsIgnoreCase("Facilitating Change")
					|| competency.equalsIgnoreCase("Creating and Innovating")
					|| competency.equalsIgnoreCase("Building Partnerships")) {
				hm1.put(3,voComp);
			}
		}
		for (int j = 0; j < vComp.size(); j++) {
			voCompetency v1 = hm1.get(j + 1);
			v.add(v1);
		}

		return v;
	}

	public void InsertGap() throws SQLException, IOException, Exception {
		// System.out.println("5.2 Gap Insertion Starts");

		// Added to instantiate a local variable called xSpreadsheet2 to make it
		// common with Group Report.java
		// Mark Oei 25 Mar 2010
		XSpreadsheet xSpreadsheet2 = OO.getSheet(xDoc,"Sheet2");
		System.out.println("Inserting gap");
		int[] address = OO.findString(xSpreadsheet,"<Gap>");

		column = address[0];
		row = address[1];
		int c = 0;
		// Added to define columns where CP, CPR, Gap are inserted in
		// spreadsheet
		// Mark Oei 25 Mar 2010
		int gapCol = 11;
		int cprCol = 10;
		int cpCol = 9;

		// To include gcp column, Chun Yeong 29 Jun 2011
		int gcpCol = 11;
		if (isGroupCPLine) {
			gapCol = 10;
			cprCol = 9;
			cpCol = 8;
		}

		int[] cpAddress;
		double cpValue = 0.0;
		double cprValue = 0.0;

		vGapSorted = G.sorting(vGapSorted,1);

		OO.findAndReplace(xSpreadsheet,"<Gap>","");

		double MinMaxGap[] = getMinMaxGap();

		double low = MinMaxGap[0];
		double high = MinMaxGap[1];
		// int type = 2; // 1 is >=, 2 is >x>, 3 is <

		// Added for Group Current Proficiency (GCP) by Chun Yeong 29 Jun 2011
		double gcpValue = 0.0;
		Vector vGCP = new Vector();
		if (iNoCPR == 0) // If CPR is chosen in this survey
		{
			// ResultSet Gap = null;
			System.out.println("Inserting gap : iNoCPR == 0");
			String title = "COMPETENCY";

			// Commented away to allow translation below, Chun Yeong 1 Aug 2011
			// if (ST.LangVer == 2)
			// title = "KOMPETENSI";
			OO.insertRows(xSpreadsheet,startColumn,endColumn,row - 1,row + 1,2,
					1);
			OO.insertString(xSpreadsheet,trans.tslt(templateLanguage,title),
					row,c);
			OO.setFontBold(xSpreadsheet,startColumn,endColumn,row,row);

			row++;
			OO.insertRows(xSpreadsheet,startColumn,endColumn,row,row + 2,2,1);

			int startBorder = row;

			// Allow dynamic translation, Chun Yeong 1 Aug 2011

			OO.insertString(
					xSpreadsheet,
					trans.tslt(templateLanguage,"STRENGTH") + " ( "
							+ trans.tslt(templateLanguage,"Gap") + " >= "
							+ high + " )",row,c);
			OO.insertString(xSpreadsheet,"CP",row,cpCol);
			OO.insertString(xSpreadsheet,"CPR",row,cprCol);
			OO.insertString(xSpreadsheet,trans.tslt(templateLanguage,"Gap"),
					row,gapCol);
			if (isGroupCPLine)
				OO.insertString(xSpreadsheet,"GCP",row,gcpCol); // Added a
																// new
																// column
																// for GCP
																// by Chun
																// Yeong 29
																// Jun 2011
			OO.setFontBold(xSpreadsheet,startColumn,endColumn,row,row);
			OO.setFontSize(xSpreadsheet,cpCol,gcpCol,row,row,12);
			OO.setCellAllignment(xSpreadsheet,cpCol,gcpCol,row,row,1,2);
			OO.setBGColor(xSpreadsheet,startColumn,endColumn - 1,row,row,
					BGCOLOR);
			row++;
			/*
			 * For those competencies exceeding the targetted Upper limit
			 */
			for (int i = 0; i < vGapSorted.size(); i++) {
				double gap = Double.valueOf(
						((String[]) vGapSorted.elementAt(i))[1]).doubleValue();

				if (gap >= high) {
					String compName = ((String[]) vGapSorted.elementAt(i))[0];

					OO.insertRows(xSpreadsheet,startColumn,endColumn,row,
							row + 1,1,1);

					// Added translation for the competency name, Chun Yeong 1
					// Aug 2011
					OO.insertString(xSpreadsheet,
							getTranslatedCompetency(compName).elementAt(0)
									.toString(),row,c);
					OO.unWrapText(xSpreadsheet,c,endColumn,row,row);
					OO.insertNumeric(xSpreadsheet,gap,row,gapCol);

					// Insert CP and CPR values next to Gap, Mark Oei 25 Mar
					// 2010
					// Added translation for the competency name, Chun Yeong 1
					// Aug 2011
					cpAddress = OO.findString(xSpreadsheet2,
							getTranslatedCompetency(compName).elementAt(0)
									.toString());
					cpValue = OO.getCellValue(xSpreadsheet2,cpAddress[1],
							cpAddress[0] + 1);
					cprValue = OO.getCellValue(xSpreadsheet2,cpAddress[1],
							cpAddress[0] + 2);
					OO.insertNumeric(xSpreadsheet,cpValue,row,cpCol);
					OO.insertNumeric(xSpreadsheet,cprValue,row,cprCol);

					// Insert GCP values next to Gap, Chun Yeong 29 Jun 2011
					if (isGroupCPLine) {
						gcpValue = getGroupCPForOneCompetency(compName.trim());
						OO.insertNumeric(xSpreadsheet,gcpValue,row,gcpCol);
					}

					OO.setFontSize(xSpreadsheet,cpCol,gcpCol,row,row,12);
					row++;
				}
			}

			// row++;
			int endBorder = row;
			OO.setTableBorder(xSpreadsheet,startColumn,endColumn - 1,
					startBorder,endBorder,false,false,true,true,true,true);
			// Add border lines from cpCol to gcpCol
			// Mark Oei 25 Mar 2010
			OO.setTableBorder(xSpreadsheet,cpCol,endColumn - 1,startBorder,
					endBorder,true,false,true,true,false,false);
			OO.setTableBorder(xSpreadsheet,cpCol,endColumn - 1,startBorder,
					endBorder,true,false,true,true,false,false);

			startBorder = endBorder + 1;
			row++;
			OO.insertRows(xSpreadsheet,startColumn,endColumn,row,row + 2,2,1);

			// Allow dynamic translation, Chun Yeong 1 Aug 2011
			OO.insertString(xSpreadsheet,
					trans.tslt(templateLanguage,"MEET EXPECTATIONS") + " ( "
							+ low + " <= " + trans.tslt(templateLanguage,"Gap")
							+ " < " + high + " )",row,c);
			OO.insertString(xSpreadsheet,"CP",row,cpCol);
			OO.insertString(xSpreadsheet,"CPR",row,cprCol);
			OO.insertString(xSpreadsheet,trans.tslt(templateLanguage,"Gap"),
					row,gapCol);
			if (isGroupCPLine)
				OO.insertString(xSpreadsheet,"GCP",row,gcpCol); // Added a
																// new
																// column
																// for GCP
																// by Chun
																// Yeong 29
																// Jun 2011

			OO.setFontBold(xSpreadsheet,startColumn,endColumn,row,row);
			OO.setFontSize(xSpreadsheet,cpCol,gcpCol,row,row,12);
			OO.setCellAllignment(xSpreadsheet,cpCol,gcpCol,row,row,1,2);
			OO.setBGColor(xSpreadsheet,startColumn,endColumn - 1,row,row,
					BGCOLOR);
			row++;
			/*
			 * For those within the acceptable range given by the company
			 */
			for (int i = 0; i < vGapSorted.size(); i++) {
				double gap = Double.valueOf(
						((String[]) vGapSorted.elementAt(i))[1]).doubleValue();

				if (gap < high && gap > low) {
					String compName = ((String[]) vGapSorted.elementAt(i))[0];

					OO.insertRows(xSpreadsheet,startColumn,endColumn,row,
							row + 1,1,1);

					// Added translation for the competency name, Chun Yeong 1
					// Aug 2011
					OO.insertString(xSpreadsheet,
							getTranslatedCompetency(compName).elementAt(0)
									.toString(),row,c);
					OO.unWrapText(xSpreadsheet,c,endColumn,row,row);
					OO.insertNumeric(xSpreadsheet,gap,row,gapCol);
					// Insert CP and CPR values next to Gap, Mark Oei 25 Mar
					// 2010
					// Added translation for the competency name, Chun Yeong 1
					// Aug 2011
					cpAddress = OO.findString(xSpreadsheet2,
							getTranslatedCompetency(compName).elementAt(0)
									.toString());
					cpValue = OO.getCellValue(xSpreadsheet2,cpAddress[1],
							cpAddress[0] + 1);
					cprValue = OO.getCellValue(xSpreadsheet2,cpAddress[1],
							cpAddress[0] + 2);
					OO.insertNumeric(xSpreadsheet,cpValue,row,cpCol);
					OO.insertNumeric(xSpreadsheet,cprValue,row,cprCol);

					// Insert GCP values next to Gap, Chun Yeong 29 Jun 2011
					if (isGroupCPLine) {
						gcpValue = getGroupCPForOneCompetency(compName.trim());
						OO.insertNumeric(xSpreadsheet,gcpValue,row,gcpCol);
					}

					OO.setFontSize(xSpreadsheet,cpCol,gcpCol,row,row,12);
					row++;
				}
			}

			row++;
			endBorder = row;
			OO.setTableBorder(xSpreadsheet,startColumn,endColumn - 1,
					startBorder,endBorder,false,false,true,true,true,true);
			// Add border lines from cpCol to gapCol
			// Mark Oei 25 Mar 2010
			OO.setTableBorder(xSpreadsheet,cpCol,endColumn - 1,startBorder,
					endBorder,true,false,true,true,false,false);

			startBorder = endBorder + 1;
			row++;

			OO.insertRows(xSpreadsheet,startColumn,endColumn,row,row + 2,2,1);

			// Insert 2 new labels CP and CPR before Gap and set font size to 12
			// Mark Oei 25 Mar 2010
			// Changed the default language to English by Chun Yeong 9 Jun 2011
			// Commented away to allow translation below, Chun Yeong 1 Aug 2011
			/*
			 * if (ST.LangVer == 2){ //Indonesian OO.insertString(xSpreadsheet,
			 * "AREA PERKEMBANGAN ( Gap <= " + low + " )", row, c);
			 * OO.insertString(xSpreadsheet, "CP", row, cpCol);
			 * OO.insertString(xSpreadsheet, "CPR", row, cprCol);
			 * OO.insertString(xSpreadsheet, "Selisih", row, gapCol);
			 * if(isGroupCPLine) OO.insertString(xSpreadsheet, "GCP", row,
			 * gcpCol); //Added a new column for GCP by Chun Yeong 29 Jun 2011 }
			 * else { //English OO.insertString(xSpreadsheet,
			 * "DEVELOPMENTAL AREA ( Gap <= " + low + " )", row, c);
			 * OO.insertString(xSpreadsheet, "CP", row, cpCol);
			 * OO.insertString(xSpreadsheet, "CPR", row, cprCol);
			 * OO.insertString(xSpreadsheet, "Gap", row, gapCol);
			 * if(isGroupCPLine) OO.insertString(xSpreadsheet, "GCP", row,
			 * gcpCol); //Added a new column for GCP by Chun Yeong 29 Jun 2011 }
			 */

			// Allow dynamic translation, Chun Yeong 1 Aug 2011
			OO.insertString(xSpreadsheet,
					trans.tslt(templateLanguage,"DEVELOPMENTAL AREA") + " ( "
							+ trans.tslt(templateLanguage,"Gap") + " < " + low
							+ " )",row,c);
			OO.insertString(xSpreadsheet,"CP",row,cpCol);
			OO.insertString(xSpreadsheet,"CPR",row,cprCol);
			OO.insertString(xSpreadsheet,trans.tslt(templateLanguage,"Gap"),
					row,gapCol);
			if (isGroupCPLine)
				OO.insertString(xSpreadsheet,"GCP",row,gcpCol); // Added a
																// new
																// column
																// for GCP
																// by Chun
																// Yeong 29
																// Jun 2011

			OO.setFontBold(xSpreadsheet,startColumn,endColumn,row,row);
			OO.setFontSize(xSpreadsheet,cpCol,gcpCol,row,row,12);
			OO.setCellAllignment(xSpreadsheet,cpCol,gcpCol,row,row,1,2);
			OO.setBGColor(xSpreadsheet,startColumn,endColumn - 1,row,row,
					BGCOLOR);

			row++;
			/*
			 * For those competencies that are below the lower limit
			 */
			for (int i = 0; i < vGapSorted.size(); i++) {

				double gap = Double.valueOf(
						((String[]) vGapSorted.elementAt(i))[1]).doubleValue();

				if (gap <= low) {
					String compName = ((String[]) vGapSorted.elementAt(i))[0];

					OO.insertRows(xSpreadsheet,startColumn,endColumn,row,
							row + 1,1,1);

					// Added translation for the competency name, Chun Yeong 1
					// Aug 2011
					OO.insertString(xSpreadsheet,
							getTranslatedCompetency(compName).elementAt(0)
									.toString(),row,c);
					OO.unWrapText(xSpreadsheet,c,endColumn,row,row);
					OO.insertNumeric(xSpreadsheet,gap,row,gapCol);
					// Insert CP and CPR values next to Gap, Mark Oei 25 Mar
					// 2010
					// Added translation for the competency name, Chun Yeong 1
					// Aug 2011
					cpAddress = OO.findString(xSpreadsheet2,
							getTranslatedCompetency(compName).elementAt(0)
									.toString());
					cpValue = OO.getCellValue(xSpreadsheet2,cpAddress[1],
							cpAddress[0] + 1);
					cprValue = OO.getCellValue(xSpreadsheet2,cpAddress[1],
							cpAddress[0] + 2);
					OO.insertNumeric(xSpreadsheet,cpValue,row,cpCol);
					OO.insertNumeric(xSpreadsheet,cprValue,row,cprCol);

					// Insert GCP values next to Gap, Chun Yeong 29 Jun 2011
					if (isGroupCPLine) {
						gcpValue = getGroupCPForOneCompetency(compName.trim());
						OO.insertNumeric(xSpreadsheet,gcpValue,row,gcpCol);
					}

					OO.setFontSize(xSpreadsheet,cpCol,gapCol,row,row,12);
					row++;
				}
			}

			endBorder = row;
			OO.setTableBorder(xSpreadsheet,startColumn,endColumn - 1,
					startBorder,endBorder,false,false,true,true,true,true);
			// Add border lines from cpCol to gapCol
			// Mark Oei 25 Mar 2010
			OO.setTableBorder(xSpreadsheet,cpCol,endColumn - 1,startBorder,
					endBorder,true,false,true,true,false,false);
		} else { // when no CPR is chosen for the survey
			// **************************************************************************
			// Delete the rows with Gap Table description from the report
			// adjusted to relative numbers to completely delete the page with
			// gap comments (Qiao Li 23 Dec 2009)

			// Commented Off deleteRows method and added codes to insert CP
			// values into column
			// Mark Oei 16 April 2010
			// OO.deleteRows(xSpreadsheet, startColumn, endColumn, row-8, row+6,
			// 14, 0);

			// ResultSet Gap = null;

			vCPValues = G.sorting(vCPValues,1); // added to sort CPValues, Mark
												// Oei 16 April 2010
			String title = "COMPETENCY";

			// Commented away to allow translation below, Chun Yeong 1 Aug 2011
			// if (ST.LangVer == 2)
			// title = "KOMPETENSI";

			// Allow dynamic translation, Chun Yeong 1 Aug 2011
			OO.insertString(xSpreadsheet,trans.tslt(templateLanguage,title),
					row,c);
			OO.setFontBold(xSpreadsheet,startColumn,endColumn,row,row);

			row++;
			OO.insertRows(xSpreadsheet,startColumn,endColumn,row,row + 2,2,1);

			int startBorder = row;

			cpCol = 11;
			// shift the cp column to the left 1 to allow space for GCP column.
			if (isGroupCPLine) {
				cpCol = 10;
			}
			OO.unMergeCells(xSpreadsheet,0,10,row,row);

			// Allow dynamic translation, Chun Yeong 1 Aug 2011
			OO.insertString(xSpreadsheet,
					trans.tslt(templateLanguage,"STRENGTH") + " ( CP >= "
							+ high + " )",row,c);
			OO.insertString(xSpreadsheet,"CP",row,cpCol);
			if (isGroupCPLine)
				OO.insertString(xSpreadsheet,"GCP",row,gcpCol); // Added a
																// new
																// column
																// for GCP
																// by Chun
																// Yeong 29
																// Jun 2011

			OO.setFontBold(xSpreadsheet,startColumn,endColumn,row,row);
			OO.setFontSize(xSpreadsheet,cpCol - 1,gcpCol,row,row,12);
			OO.setCellAllignment(xSpreadsheet,cpCol - 1,gcpCol,row,row,1,2);
			OO.setBGColor(xSpreadsheet,startColumn,endColumn - 1,row,row,
					BGCOLOR);
			row++;

			for (int i = 0; i < vCPValues.size(); i++) {

				double cpValues = Double.valueOf(
						((String[]) vCPValues.elementAt(i))[1]).doubleValue();

				if (cpValues >= high) {
					String compName = ((String[]) vCPValues.elementAt(i))[0];

					OO.insertRows(xSpreadsheet,startColumn,endColumn,row,
							row + 1,1,1);

					// Added translation for competency name, Chun Yeong 1 Aug
					// 2011
					OO.insertString(xSpreadsheet,
							getTranslatedCompetency(compName).elementAt(0)
									.toString(),row,c);
					OO.unWrapText(xSpreadsheet,c,endColumn,row,row);
					OO.insertNumeric(xSpreadsheet,cpValues,row,cpCol);

					// Insert GCP values next to Gap, Chun Yeong 29 Jun 2011
					if (isGroupCPLine) {
						gcpValue = getGroupCPForOneCompetency(compName.trim());
						OO.insertNumeric(xSpreadsheet,gcpValue,row,gcpCol);
					}

					OO.setFontSize(xSpreadsheet,cpCol - 1,gcpCol,row,row,12);
					OO.setCellAllignment(xSpreadsheet,10,11,row,row,1,2);
					row++;
				}
			}

			// row++;
			int endBorder = row;
			OO.setTableBorder(xSpreadsheet,startColumn,endColumn - 1,
					startBorder,endBorder,false,false,true,true,true,true);
			OO.setTableBorder(xSpreadsheet,cpCol,endColumn - 1,startBorder,
					endBorder,true,false,true,true,false,false);
			startBorder = endBorder + 1;
			row++;
			OO.insertRows(xSpreadsheet,startColumn,endColumn,row,row + 2,2,1);

			// Insert 2 new labels CP and CPR before Gap and set font size to 12
			// Mark Oei 25 Mar 2010
			// Changed the default language to English by Chun Yeong 9 Jun 2011
			// Commented away to allow translation below, Chun Yeong 1 Aug 2011
			/*
			 * if (ST.LangVer == 2){ //Indonesian OO.insertString(xSpreadsheet,
			 * "MEMENUHI PENGHARAPAN ( " + low + " < CP < " + high + " )", row,
			 * c); OO.insertString(xSpreadsheet, "CP", row, cpCol);
			 * if(isGroupCPLine) OO.insertString(xSpreadsheet, "GCP", row,
			 * gcpCol); //Added a new column for GCP by Chun Yeong 29 Jun 2011 }
			 * else { //English OO.insertString(xSpreadsheet,
			 * "MEET EXPECTATIONS ( " + low + " < CP < " + high + " )" , row,
			 * c); OO.insertString(xSpreadsheet, "CP", row, cpCol);
			 * if(isGroupCPLine) OO.insertString(xSpreadsheet, "GCP", row,
			 * gcpCol); //Added a new column for GCP by Chun Yeong 29 Jun 2011 }
			 */

			// Allow dynamic translation, Chun Yeong 1 Aug 2011
			// unwrap text cause it mysteriously wrap itself and hide the words
			// , Sherman 6/6/2013
			OO.unWrapText(xSpreadsheet,0,1,row,row);
			OO.insertString(xSpreadsheet,
					trans.tslt(templateLanguage,"MEET EXPECTATIONS") + " ( "
							+ low + " <= CP < " + high + " )",row,c);
			OO.insertString(xSpreadsheet,"CP",row,cpCol);
			if (isGroupCPLine)
				OO.insertString(xSpreadsheet,"GCP",row,gcpCol); // Added a
																// new
																// column
																// for GCP
																// by Chun
																// Yeong 29
																// Jun 2011

			OO.setFontBold(xSpreadsheet,startColumn,endColumn,row,row);
			OO.setFontSize(xSpreadsheet,cpCol - 1,gcpCol,row,row,12);
			OO.setCellAllignment(xSpreadsheet,cpCol - 1,gcpCol,row,row,1,2);
			OO.setBGColor(xSpreadsheet,startColumn,endColumn - 1,row,row,
					BGCOLOR);
			row++;

			for (int i = 0; i < vCPValues.size(); i++) {
				double cpValues = Double.valueOf(
						((String[]) vCPValues.elementAt(i))[1]).doubleValue();

				if (cpValues < high && cpValues > low) {
					String compName = ((String[]) vCPValues.elementAt(i))[0];

					OO.insertRows(xSpreadsheet,startColumn,endColumn,row,
							row + 1,1,1);

					// Added translation for competency name, Chun Yeong 1 Aug
					// 2011
					OO.insertString(xSpreadsheet,
							getTranslatedCompetency(compName).elementAt(0)
									.toString(),row,c);
					OO.unWrapText(xSpreadsheet,c,endColumn,row,row);
					OO.insertNumeric(xSpreadsheet,cpValues,row,cpCol);

					// Insert GCP values next to Gap, Chun Yeong 29 Jun 2011
					if (isGroupCPLine) {
						gcpValue = getGroupCPForOneCompetency(compName.trim());
						OO.insertNumeric(xSpreadsheet,gcpValue,row,gcpCol);
					}

					OO.setFontSize(xSpreadsheet,cpCol - 1,gcpCol,row,row,12);
					OO.setCellAllignment(xSpreadsheet,10,11,row,row,1,2);
					row++;
				}
			}

			// row++;
			endBorder = row;
			OO.setTableBorder(xSpreadsheet,startColumn,endColumn - 1,
					startBorder,endBorder,false,false,true,true,true,true);
			OO.setTableBorder(xSpreadsheet,cpCol,endColumn - 1,startBorder,
					endBorder,true,false,true,true,false,false);

			startBorder = endBorder + 1;
			row++;

			OO.insertRows(xSpreadsheet,startColumn,endColumn,row,row + 2,2,1);

			// Changed the default language to English by Chun Yeong 9 Jun 2011
			// Commented away to allow translation below, Chun Yeong 1 Aug 2011
			/*
			 * if (ST.LangVer == 2){ //Indonesian OO.insertString(xSpreadsheet,
			 * "AREA PERKEMBANGAN ( CP <= " + low + " )", row, c); //Change
			 * wording Gap to CP for CP Only, Mark 20 May 2010
			 * OO.insertString(xSpreadsheet, "CP", row, cpCol);
			 * if(isGroupCPLine) OO.insertString(xSpreadsheet, "GCP", row,
			 * gcpCol); //Added a new column for GCP by Chun Yeong 29 Jun 2011 }
			 * else { //English OO.insertString(xSpreadsheet,
			 * "DEVELOPMENTAL AREA ( CP <= " + low + " )", row, c); //Change
			 * wording Gap to CP for CP Only, Mark 20 May 2010
			 * OO.insertString(xSpreadsheet, "CP", row, cpCol);
			 * if(isGroupCPLine) OO.insertString(xSpreadsheet, "GCP", row,
			 * gcpCol); //Added a new column for GCP by Chun Yeong 29 Jun 2011 }
			 */

			// Allow dynamic translation, Chun Yeong 1 Aug 2011
			// unwrap text cause it mysteriously wrap itself and hide the words
			// , Sherman 6/6/2013
			OO.unWrapText(xSpreadsheet,0,1,row,row);
			OO.insertString(xSpreadsheet,
					trans.tslt(templateLanguage,"DEVELOPMENTAL AREA")
							+ " ( CP < " + low + " )",row,c); // Change
																// wording
																// Gap to CP
																// for CP
																// Only,
																// Mark 20
																// May 2010
			OO.insertString(xSpreadsheet,"CP",row,cpCol);
			if (isGroupCPLine)
				OO.insertString(xSpreadsheet,"GCP",row,gcpCol); // Added a
																// new
																// column
																// for GCP
																// by Chun
																// Yeong 29
																// Jun 2011

			OO.setFontBold(xSpreadsheet,startColumn,endColumn,row,row);
			OO.setFontSize(xSpreadsheet,cpCol - 1,gcpCol,row,row,12);
			OO.setCellAllignment(xSpreadsheet,cpCol - 1,gcpCol,row,row,1,2);
			OO.setBGColor(xSpreadsheet,startColumn,endColumn - 1,row,row,
					BGCOLOR);

			row++;

			for (int i = 0; i < vCPValues.size(); i++) {
				double cpValues = Double.valueOf(
						((String[]) vCPValues.elementAt(i))[1]).doubleValue();

				if (cpValues <= low) {
					String compName = ((String[]) vCPValues.elementAt(i))[0];

					OO.insertRows(xSpreadsheet,startColumn,endColumn,row,
							row + 1,1,1);

					// Added translation for competency name, Chun Yeong 1 Aug
					// 2011
					OO.insertString(xSpreadsheet,
							getTranslatedCompetency(compName).elementAt(0)
									.toString(),row,c);
					OO.unWrapText(xSpreadsheet,c,endColumn,row,row);
					OO.insertNumeric(xSpreadsheet,cpValues,row,cpCol);

					// Insert GCP values next to Gap, Chun Yeong 29 Jun 2011
					if (isGroupCPLine) {
						gcpValue = getGroupCPForOneCompetency(compName.trim());
						OO.insertNumeric(xSpreadsheet,gcpValue,row,gcpCol);
					}

					OO.setFontSize(xSpreadsheet,cpCol - 1,gcpCol,row,row,12);
					OO.setCellAllignment(xSpreadsheet,10,11,row,row,1,2);
					row++;
				}
			}

			endBorder = row;
			OO.setTableBorder(xSpreadsheet,startColumn,endColumn - 1,
					startBorder,endBorder,false,false,true,true,true,true);
			OO.setTableBorder(xSpreadsheet,cpCol,endColumn - 1,startBorder,
					endBorder,true,false,true,true,false,false);
		}

		// System.out.println("5. Gap Completed");
	}

	/*
	 * Sort the gap.
	 * 
	 * @param int type 0 = DESC, 1 = ASC
	 */
	public Vector sortGap(Vector vGapLocal, int type) throws SQLException,
			Exception {
		// System.out.println("Sort vGapLocal.size() = " + vGapLocal.size());
		Vector vLocal = (Vector) vGapLocal.clone();
		Vector vSorted = new Vector();
		double max = 0; // highest score
		double temp = 0; // temp score
		int curr = 0; // curr highest element

		while (!vLocal.isEmpty()) {
			max = Double.valueOf(((String[]) vLocal.elementAt(0))[1])
					.doubleValue();
			curr = 0;

			// do sorting here
			for (int t = 1; t < vLocal.size(); t++) {
				temp = Double.valueOf(((String[]) vLocal.elementAt(t))[1])
						.doubleValue();

				if (type == 0) {
					if (temp > max) {
						max = temp;
						curr = t;
					}
				} else {
					if (temp < max) {
						max = temp;
						curr = t;
					}
				}
			}

			String info[] = {((String[]) vLocal.elementAt(curr))[0],
					((String[]) vLocal.elementAt(curr))[1]};
			vSorted.add(info);

			vLocal.removeElementAt(curr);
		}

		return vSorted;
	}

	/**
	 * Write Normative results on excel worksheet.
	 */
	public void InsertNormative() throws SQLException, IOException, Exception {
		// Added by Tracy 01 Sep 08**************************************
		// Insert CP Rating into "Normative"
		int[] CPAddress = OO.findString(xSpreadsheet,"<CP>");
		String RTaskName = "";

		column = CPAddress[0];
		row = CPAddress[1];

		// Get CP Rating from database according to survey ID
		String query = "SELECT b.RatingTask as RTaskName FROM tblSurveyRating a ";
		query += "INNER JOIN tblRatingTask b ON a.RatingTaskID=b.RatingTaskID  WHERE a.SurveyID = "
				+ surveyID + " AND a.RatingTaskID=1";

		Connection con = null;
		Statement st = null;
		ResultSet rs = null;

		try {
			con = ConnectionBean.getConnection();
			st = con.createStatement();
			rs = st.executeQuery(query);

			if (rs.next()) {
				RTaskName = rs.getString("RTaskName");
			}
		} catch (Exception E) {
			System.err.println("SurveyResult.java - GroupSection - " + E);
		} finally {
			ConnectionBean.closeRset(rs); // Close ResultSet
			ConnectionBean.closeStmt(st); // Close statement
			ConnectionBean.close(con); // Close connection
		}

		OO.findAndReplace(xSpreadsheet,"<CP>",
				trans.tslt(templateLanguage,RTaskName));
		// End Edit by Tracy 01 Sep 08******************************
		int[] address = OO.findString(xSpreadsheet,"<Normative>");

		column = address[0];
		row = address[1];

		OO.findAndReplace(xSpreadsheet,"<Normative>","");
		// OO.insertRows(xSpreadsheet, startColumn, endColumn, row, row+26, 26,
		// 1);

		Vector allTargets = null;
		Vector targetResult = CPCPR("CP");
		Vector otherTarget = null;
		int totalTargets = TotalTargetsIncluded(); // include SELF

		// Changed by Alvis on 29 Sep 09 to ensure weight retains its decimal
		// value
		double weight = (double) 100 / totalTargets;
		int total = totalCompetency();
		int tot = 0;
		int compID = 0;

		// Initialise array for arrN
		arrN = new int[total * 10 * 6]; // size = total competency * max 10 KBs
										// * max 6 Rating

		// double [] normative = new double[total];
		// String comp [] = new String[total];
		double normative = 0;
		String comp = "";

		int r = row;

		// by Hemilda 15/09/2008 remove the competencies 0 at chart normative
		// report

		for (int i = 0; i < targetResult.size(); i++) { // particular target's
														// result by All
			String[] arr = (String[]) targetResult.elementAt(i); // arr is each
																	// competency,
																	// loop
																	// through
																	// all
																	// competencies

			allTargets = null;
			allTargets = TargetsID();

			tot = 0; // 1 for the target himself
			normative = 0;

			compID = Integer.parseInt(arr[0]); // get targetcompetency id
			comp = arr[1]; // get target competency name
			double target = Double.parseDouble(arr[2]); // target competency
														// score
			// System.out.println("AlltargetSize : " + allTargets.size());

			for (int j = 0; j < allTargets.size(); j++) {

				int iTargetLoginID = ((Integer) allTargets.elementAt(j))
						.intValue();
				otherTarget = getOtherTargetResults(iTargetLoginID,compID);
				// Changed by HA 07/07/08
				// The result returned is a vector with only one element because
				// it stores value of each Target, therefore should not have
				// j < otherTarget.size()

				if (otherTarget.size() != 0) {
					// Changed by Ha 07/07/08 should only retrieve the first
					// element
					String[] arrOther = (String[]) otherTarget.elementAt(0);

					double all = Double.parseDouble(arrOther[2]); // the other
																	// targetscore
					// System.out.println("target" + j + "score= " + all);

					if (target >= all)
						tot++; // count how many with lower or equal score than
								// target
				}
			}

			// to round up to 2 decimal points
			// int twodec = 100 - ((int)((double)tot/(double)totalTargets * 100
			// * 100));
			// Changed by Alvis on 25 Sep 09 formula to calculate normative
			// score and ensure normative score is >= 0.
			tot = tot + 1; // include target candidate himself and those with
							// lower or equal score(same as total no. of
							// candidates-no. of candidates with scores higher
							// than the target candidate)

			// Add by Alvis on 29 Sep 09 to make sure the person with the
			// highest score has a normative score of 100
			if ((totalTargets != 0) && ((allTargets.size() + 1 - tot) > 0)) {
				normative = tot * weight;

				// Added by Alvis on 29 Sep 09 to round off normative score to 2
				// decimal places
				normative = ((double) ((int) ((normative + 0.005) * 100.0))) / 100.0;
			} else {
				normative = 100;
			}

			int compLocation = sortCompetency(comp);

			// Added translation for the competency name, Chun Yeong 1 Aug 2011
			OO.insertString(
					xSpreadsheet2,
					getTranslatedCompetency(
							UnicodeHelper.getUnicodeStringAmp(comp)).elementAt(
							0).toString(),r + compLocation,0);
			OO.insertNumeric(xSpreadsheet2,normative,r + compLocation,1);

		}

		// rianto
		// int height = 2000 * (r - row);
		// if((r - row) > 6)
		// height = 14000;

		// Allow dynamic translation, Chun Yeong 1 Aug 2011
		String title = trans.tslt(templateLanguage,"Normative Report for")
				+ " " + UserName() + " vs " + surveyInfo[1];

		// Commented away to allow translation above, Chun Yeong 1 Aug 2011
		// if(ST.LangVer == 2)
		// title = "Laporan Normative untuk " + UserName() + " vs " +
		// surveyInfo[1];

		/*
		 * rianto //draw chart OO.setFontSize(8); //XTableChart xtablechart =
		 * OO.getChart(xSpreadsheet, xSpreadsheet2, 0, 1, row, r-1, "Normative",
		 * 13000, height, row, 1); XTableChart xtablechart =
		 * OO.getChart(xSpreadsheet, xSpreadsheet2, 0, 1, row, r-1, "Normative",
		 * 16000, height + 1500, row, 1); xtablechart =
		 * OO.setChartTitle(xtablechart, title); xtablechart =
		 * OO.setAxes(xtablechart, "Competencies", "Results (%)", 100, 10, 4500,
		 * 2000); OO.setChartProperties(xtablechart, false, true, true, true,
		 * true);
		 */

		XTableChart xtablechart = OO.getChartByIndex(xSpreadsheet,1);
		xtablechart = OO.setChartTitle(xtablechart,title);

		// OO.setSourceData(xSpreadsheet, xSpreadsheet2, 1, 0, 1, row+1, r-1);
		// Changed by Ha 26/05/08 change parameters passing to the following
		// method
		OO.setSourceData(xSpreadsheet,xSpreadsheet2,1,0,1,row,row + 11);

		// Set back to Bar Chart Horizontal, because system automatically set
		// back to Bar Chart Vertical.
		// Remove the horizontal gridlines, Mark Oei 25 Mar 2010
		OO.setChartProperties(xtablechart,false,true,true,true,true);

		// ("6. Normative Completed");
	}

	public int sortCompetency(String comp) {

		int compLocation = 0;

		if (comp.equalsIgnoreCase("Personal Mastery")) {
			compLocation = 0;
		} else if (comp.equalsIgnoreCase("Planning and Prioritising")) {
			compLocation = 1;
		} else if (comp.equalsIgnoreCase("Focusing on Mission and Vision")) {
			compLocation = 2;
		} else if (comp.equalsIgnoreCase("Facilitating Change")) {
			compLocation = 3;
		} else if (comp.equalsIgnoreCase("Solving Problems")) {
			compLocation = 4;
		} else if (comp.equalsIgnoreCase("Systems Thinking")) {
			compLocation = 5;
		} else if (comp.equalsIgnoreCase("Creating and Innovating")) {
			compLocation = 6;
		} else if (comp.equalsIgnoreCase("Unleashing People Potential")) {
			compLocation = 7;
		} else if (comp.equalsIgnoreCase("Performing as a Team")) {
			compLocation = 8;
		} else if (comp.equalsIgnoreCase("Using Heartskills")) {
			compLocation = 9;
		} else if (comp.equalsIgnoreCase("Communicating Effectively")) {
			compLocation = 10;
		} else if (comp.equalsIgnoreCase("Building Partnerships")) {
			compLocation = 11;
		}

		return compLocation;

	}

	/**
	 * Write competency report to excel.
	 */

	// Added by Tracy 01 Sep 08**********************************
	public void InsertCompGap(int surveyID) throws SQLException, Exception {
		// System.out.println("6.1 Competency Gap Insertion Starts");

		int i = 0;
		Vector RTaskID = new Vector();
		Vector RTaskName = new Vector();

		// Get Rating from database according to survey ID
		String query = "SELECT a.RatingTaskID as RTaskID, b.RatingTask as RTaskName FROM tblSurveyRating a ";
		query += "INNER JOIN tblRatingTask b ON a.RatingTaskID=b.RatingTaskID  WHERE a.SurveyID = "
				+ surveyID;

		Connection con = null;
		Statement st = null;
		ResultSet rs = null;

		try {
			con = ConnectionBean.getConnection();
			st = con.createStatement();
			rs = st.executeQuery(query);

			while (rs.next()) {
				RTaskID.add(i,new Integer(rs.getInt("RTaskID")));
				RTaskName.add(i,new String(rs.getString("RTaskName")));
				i++;
			}

			// Check CPR or FPR
			String pType = "";
			String CPR = "";
			String FPR = "";

			for (int n = 0; RTaskID.size() - 1 >= n; n++) {
				if (((Integer) RTaskID.elementAt(n)).intValue() == 1) {
					// CP=RTaskName.elementAt(n).toString();
				} else if (((Integer) RTaskID.elementAt(n)).intValue() == 2) {
					CPR = RTaskName.elementAt(n).toString();
					pType = "C";
				} else if (((Integer) RTaskID.elementAt(n)).intValue() == 3) {
					FPR = RTaskName.elementAt(n).toString();
					pType = "F";
				}
			}

			String RPTitle = "";
			if (pType.equals("C"))
				RPTitle = CPR;
			else if (pType.equals("F"))
				RPTitle = FPR;

			// Insert title to excel file
			OO.findAndReplace(xSpreadsheet,"<CompRP>",RPTitle);

		} catch (Exception E) {
			System.err.println("SurveyResult.java - InsertCompGap - " + E);
		} finally {
			ConnectionBean.closeRset(rs); // Close ResultSet
			ConnectionBean.closeStmt(st); // Close statement
			ConnectionBean.close(con); // Close connection

		}
	}

	// End add by Tracy 01 Sep 08*******************************
	/**
	 * Added by Santoso : 16 Oct 2008 A helper method to merge and center a cell
	 * 
	 */
	private void mergeAndCenter(XSpreadsheet xSpreadsheet, int startRow,
			int endRow) throws Exception {
		OO.mergeCells(xSpreadsheet,0,0,startRow,endRow);
		OO.setCellAllignment(xSpreadsheet,0,0,startRow,startRow,2,2);
	}

	/**
	 * Added by Santoso : 16 Oct 2008 To prepare the cells to write the number
	 * of rater
	 * 
	 * @return array of row position for writing nr. of rater starting from
	 *         bottom to top
	 * @throws Exception
	 *             Qiao Li (21 Dec 2009) precondition: totalRater can only be 4,
	 *             5 or 6
	 * 
	 */
	private int[] prepareCells(XSpreadsheet xSpreadsheet, int startingRow,
			int totalRater) throws Exception {
		int[] result = new int[7];
		OO.setFontSize(xSpreadsheet,0,0,startingRow,startingRow + 11,12);
		/*
		 * Change (s): seperate the cases to totalRater == 5 and totalRater ==
		 * 6, throw exeption if totalRoter not 4, 5 or 6 Reason (s): accomodate
		 * the different alignment of n for different number of raters Updated
		 * by: Qiao Li Date: 21 Dec 2009
		 */
		if (totalRater == 7) {
			result[0] = startingRow + 2;
			result[1] = startingRow + 3;
			mergeAndCenter(xSpreadsheet,result[1],result[1] + 1);
			result[2] = startingRow + 5;
			mergeAndCenter(xSpreadsheet,result[2],result[2] + 1);
			result[3] = startingRow + 7;
			result[4] = startingRow + 9;
			result[5] = startingRow + 10;
			mergeAndCenter(xSpreadsheet,result[5],result[5] + 1);
			result[6] = startingRow + 12;
		} else if (totalRater == 6) {
			// FOR: others are split into additional, peers, and subordinates
			result[0] = startingRow + 2;
			result[1] = startingRow + 4;
			result[2] = startingRow + 6;
			result[3] = startingRow + 8;
			result[4] = startingRow + 10;

			// Added so that the n value is aligned properly after splitting
			// peers and subordinates, Desmond 22 Oct 09
			result[5] = startingRow + 12;

		} else if (totalRater == 5) {
			result[0] = startingRow + 2;
			result[1] = startingRow + 4;
			result[2] = startingRow + 7;
			result[3] = startingRow + 9;
			result[4] = startingRow + 12;
			mergeAndCenter(xSpreadsheet,result[1],result[1] + 1);
			mergeAndCenter(xSpreadsheet,result[3],result[3] + 1);

		} else if (totalRater == 4) {
			// FOR: others is a combination of peers, subordinates, and peers
			result[0] = startingRow + 2;
			mergeAndCenter(xSpreadsheet,result[0],result[0] + 1);
			result[1] = startingRow + 5;
			mergeAndCenter(xSpreadsheet,result[1],result[1] + 1);
			result[2] = startingRow + 8;
			mergeAndCenter(xSpreadsheet,result[2],result[2] + 1);
			result[3] = startingRow + 11;
			mergeAndCenter(xSpreadsheet,result[3],result[3] + 1);
		} else {
			result[0] = startingRow + 2;
			result[1] = startingRow + 4;
			result[2] = startingRow + 6;
			result[3] = startingRow + 8;
			result[4] = startingRow + 10;
			result[5] = startingRow + 12;
			System.out
					.println("Invalid parameter totalRater - SPFIndividualReport.java");
		}
		return result;
	}

	/**
	 * Added by Albert : 10 July 2012 To prepare the cells to write the number
	 * of rater
	 * 
	 * @return array of row position for writing nr. of rater starting from
	 *         bottom to top
	 * @throws Exception
	 * 
	 */
	private int[] prepareCellsBreakCPR(XSpreadsheet xSpreadsheet,
			int startingRow, int totalRater) throws Exception {
		int[] result = new int[12];
		OO.setFontSize(xSpreadsheet,0,0,startingRow,startingRow + 11,11);

		if (totalRater == 12) {
			// addition of additional raters
			// Peer and subordinates are split
			result[0] = startingRow + 1;
			result[1] = startingRow + 2;
			result[2] = startingRow + 3;
			result[3] = startingRow + 4;
			result[4] = startingRow + 5;
			result[5] = startingRow + 6;
			result[6] = startingRow + 7;
			result[7] = startingRow + 8;
			result[8] = startingRow + 9;
			result[9] = startingRow + 10;
			result[10] = startingRow + 11;
			result[11] = startingRow + 12;
		} else if (totalRater == 10) {
			// Peer and subordinates are split
			result[0] = startingRow + 2;
			result[1] = startingRow + 3;
			result[2] = startingRow + 4;
			result[3] = startingRow + 5;
			result[4] = startingRow + 6;
			result[5] = startingRow + 7;
			OO.mergeCells(xSpreadsheet,0,0,startingRow + 7,startingRow + 8);
			OO.setCellAllignment(xSpreadsheet,0,0,startingRow + 7,
					startingRow + 8,2,2);
			result[6] = startingRow + 9;
			result[7] = startingRow + 10;
			result[8] = startingRow + 11;
			result[9] = startingRow + 12;

		} else if (totalRater == 8) {
			// peers and subordinates are combined to others
			result[0] = startingRow + 2;
			result[1] = startingRow + 3;
			OO.mergeCells(xSpreadsheet,0,0,startingRow + 3,startingRow + 4);
			OO.setCellAllignment(xSpreadsheet,0,0,startingRow + 3,
					startingRow + 4,2,2);
			result[2] = startingRow + 5;
			result[3] = startingRow + 6;
			OO.mergeCells(xSpreadsheet,0,0,startingRow + 6,startingRow + 7);
			OO.setCellAllignment(xSpreadsheet,0,0,startingRow + 6,
					startingRow + 7,2,2);
			result[4] = startingRow + 8;
			result[5] = startingRow + 9;
			result[6] = startingRow + 10;
			OO.mergeCells(xSpreadsheet,0,0,startingRow + 10,startingRow + 11);
			OO.setCellAllignment(xSpreadsheet,0,0,startingRow + 10,
					startingRow + 11,2,2);
			result[7] = startingRow + 12;
			// mergeAndCenter(xSpreadsheet, result[1], result[1]+1);
			// mergeAndCenter(xSpreadsheet, result[3], result[3]+1);
		} else {
			result[0] = startingRow + 2;
			result[1] = startingRow + 3;
			result[2] = startingRow + 4;
			result[3] = startingRow + 5;
			result[4] = startingRow + 6;
			result[5] = startingRow + 7;
			result[6] = startingRow + 8;
			result[7] = startingRow + 9;
			result[8] = startingRow + 10;
			result[9] = startingRow + 11;
			System.out
					.println("Invalid parameter totalRater - SPFIndividualReport.java");
		}
		return result;
	}

	private int findRatingIdx(String name, String[] rating, double[] result,
			int[] totalRater, String[] newRating, double[] newResult,
			int[] newTotalRater, int idx, List ratingProcessed) {
		int ratingIdx = -1;
		for (int i = 0; i < rating.length; i++) {
			if (rating[i] != null && rating[i].equals(name)) {
				ratingIdx = i;
				break;
			}
		}
		if (ratingIdx != -1) {
			newRating[idx] = rating[ratingIdx];
			newResult[idx] = result[ratingIdx];
			newTotalRater[idx++] = totalRater[ratingIdx];
			ratingProcessed.add(new Integer(ratingIdx));
		}
		return idx;
	}

	/**
	 * Added by Santoso Sort Rating and Result array The order is CP(ALL),
	 * CP(SELF), CP(OTHERS), CP(SUPERVISORS), CPR(ALL)
	 * 
	 * @param params
	 *            array containing Rating and Result
	 */
	private Object[] sortRatingResult(Object[] params) {
		String[] rating = (String[]) params[0];
		double[] result = (double[]) params[1];
		int[] totalRater = (int[]) params[2];
		String[] newRating = new String[rating.length];
		double[] newResult = new double[result.length];
		int[] newTotalRater = new int[totalRater.length];

		int idx = 0;
		List ratingProcessed = new ArrayList();
		// we do it in a simple way, hardcoding the rating name here
		idx = findRatingIdx("CP(All)",rating,result,totalRater,newRating,
				newResult,newTotalRater,idx,ratingProcessed);
		idx = findRatingIdx("CP(Self)",rating,result,totalRater,newRating,
				newResult,newTotalRater,idx,ratingProcessed);
		idx = findRatingIdx("CP(Others)",rating,result,totalRater,newRating,
				newResult,newTotalRater,idx,ratingProcessed);
		idx = findRatingIdx("CP(Supervisors)",rating,result,totalRater,
				newRating,newResult,newTotalRater,idx,ratingProcessed);
		idx = findRatingIdx("CPR(All)",rating,result,totalRater,newRating,
				newResult,newTotalRater,idx,ratingProcessed);

		// do we have some rating not inserted yet?
		if (ratingProcessed.size() < rating.length) {
			for (int i = 0; i < rating.length; i++) {
				if (ratingProcessed.contains(new Integer(i))) {
					continue;
				}
				newRating[idx] = rating[i];
				newResult[idx] = result[i];
				newTotalRater[idx++] = totalRater[i];
			}
		}

		return new Object[]{newRating, newResult, newTotalRater};
	}

	/**
	 * by Santoso 2008/10/29 Count total rater for the particular survey and
	 * target. for KB level To calculate number of others rater for each rating
	 * task of each KB
	 */
	/*
	 * Change (s): add in surveyLevel to use different tables
	 * (tblResultCompetency<-surveyLevel == 0 and tblResultKeyBehavior<-
	 * surveyLevel ==1) to retrieve number of raters Updated by: Qiao Li 21 Dec
	 * 2009
	 */
	public int totalRater(int iRatingTaskID, int iCompetencyID, int iKBID,
			String raterCode, int surveyLevel) throws SQLException {
		int total = 0;
		SurveyResult SR = new SurveyResult();
		Calculation cal = new Calculation();
		String query = "select count(*) AS Total ";
		query = query + " From( ";
		query = query + " SELECT DISTINCT tblAssignment.RaterCode";
		query = query + " FROM tblAssignment INNER JOIN ";
		if (surveyLevel == 0) {
			query = query
					+ "tblResultCompetency ON tblAssignment.AssignmentID = tblResultCompetency.AssignmentID ";
			query = query + " WHERE     (tblAssignment.SurveyID =  " + surveyID
					+ ") AND (tblAssignment.TargetLoginID = " + targetID + ") ";
			if (cal.NAIncluded(surveyID) == 0)
				query = query + " AND RaterCode LIKE '" + raterCode
						+ "' and RaterStatus in(1,2,4)";
			else
				query = query + " AND RaterCode LIKE '" + raterCode
						+ "' and RaterStatus in(1,2,4,5)";
			query = query + "  AND (tblResultCompetency.RatingTaskID = "
					+ iRatingTaskID
					+ ")and (tblResultCompetency.CompetencyID = "
					+ iCompetencyID + ") ";
			if (cal.NAIncluded(surveyID) == 0)
				query = query + " AND (tblResultCompetency.Result <> 0)";
			query = query + "  ) table1 ";
		} else {
			query = query
					+ " tblResultBehaviour ON tblAssignment.AssignmentID = tblResultBehaviour.AssignmentID INNER JOIN ";
			query = query
					+ " KeyBehaviour ON tblResultBehaviour.KeyBehaviourID = KeyBehaviour.PKKeyBehaviour ";
			query = query + " WHERE     (tblAssignment.SurveyID =  " + surveyID
					+ ") AND (tblAssignment.TargetLoginID = " + targetID + ") ";
			if (cal.NAIncluded(surveyID) == 0)
				query = query + " AND RaterCode LIKE '" + raterCode
						+ "' and RaterStatus in(1,2,4)";
			else
				query = query + " AND RaterCode LIKE '" + raterCode
						+ "' and RaterStatus in(1,2,4,5)";
			query = query + "  AND (tblResultBehaviour.RatingTaskID = "
					+ iRatingTaskID + ")and ";
			if (iKBID == -1)
				query += "(KeyBehaviour.FKCompetency = " + iCompetencyID + ") ";
			else
				query += "(KeyBehaviour.PKKeyBehaviour = " + iKBID + ") ";

			if (cal.NAIncluded(surveyID) == 0)
				query = query + " AND (tblResultBehaviour.Result <> 0)";
			query = query + "  ) table1 ";
		}

		Connection con = null;
		Statement st = null;
		ResultSet rs = null;

		try {
			con = ConnectionBean.getConnection();
			st = con.createStatement();
			rs = st.executeQuery(query);

			if (rs.next())
				total = rs.getInt(1);

		} catch (Exception ex) {
			System.out.println("SPFIndividualReport.java - totalRater - "
					+ ex.getMessage());
		} finally {
			ConnectionBean.closeRset(rs); // Close ResultSet
			ConnectionBean.closeStmt(st); // Close statement
			ConnectionBean.close(con); // Close connection
		}
		return total;

	}

	/*
	 * Added to print cluster in the report by Albert 29 June 2012
	 */
	public void InsertClusterCompetency(int reportType, int SurveyID,
			int breakCPR) throws SQLException, Exception {

		int iN = 0; // To be used as counter for arrN
		boolean combineDirIdr = false;
		// System.out.println("7. Competencies Starts");
		DecimalFormat df = new DecimalFormat("#.##");
		int[] address = OO.findString(xSpreadsheet,"<Report>");

		column = address[0];
		row = address[1];

		OO.findAndReplace(xSpreadsheet,"<Report>","");

		int surveyLevel = Integer.parseInt(surveyInfo[0]);

		if (breakCPR == 0) {// without breaking CPR
			int totalOtherRT = totalOtherRT();
			int total = totalGroup() + totalOtherRT + 1; // 1 for all
			int totalType = 9;

			// used variable totalType to determine the initialization of array
			// size (16 Dec 2009 Qiao Li)
			String[] Rating = new String[totalType + totalOtherRT]; // Changed
																	// from 4 +
																	// totalOtherRT
																	// to 5 +
																	// totalOtherRT,
																	// to clean
																	// up later
																	// on, DeZ

			double[] Result = new double[totalType + totalOtherRT]; // Changed
																	// from 4 +
																	// totalOtherRT
																	// to 5 +
																	// totalOtherRT,
																	// to clean
																	// up later
																	// on, DeZ
			int[] totalRater = new int[totalType + totalOtherRT]; // Changed
																	// from 4 +
																	// totalOtherRT
																	// to 5 +
																	// totalOtherRT,
																	// to clean
																	// up later
																	// on, DeZ

			// initialize Rating --> CP only, will be replace later on in this
			// method if scores exists for that row, added comments here,
			// Desmond 22 Oct 09
			Rating[0] = "CP(All)";
			Rating[1] = "CP(Superior)"; // Change from Supervisors to Superior,
										// Desmond 22 Oct 09
			// added back the others for type customization(16 Dec 2009 Qiao Li)
			Rating[2] = "CP(Others)"; // Commented away to remove CP(Others) to
										// cater for splitting Subordinates and
										// Peers, Desmond 22 Oct 09
			Rating[3] = "CP(Self)";
			// Add additional categories for bar graph to cater for splitting of
			// Subordinates and Peers, Desmond 21 Oct 09
			Rating[4] = "nil";
			Rating[5] = "CP(Peers)";
			Rating[6] = "CP(Additional)";
			Rating[7] = "CP(Direct)";
			Rating[8] = "CP(Indirect)";

			int maxScale = MaxScale();

			int count = 0; // to count total chart for each page, max = 1;
			int r1 = 1;
			int add = 13 / total;
			// added back the others for type customization(16 Dec 2009 Qiao Li)
			int totalSup = 0;
			int totalSelf = 0;
			int totalAll = 0;

			// Added new variables in order to cater for splitting of
			// Subordinates and Peers, Desmond 21 Oct 2009
			int totalPeer = 0;
			int totalAdd = 0;
			int totalOth = 0;
			int totalDirect = 0;
			int totalIndirect = 0;
			int totalSub = 0;
			Vector vClust = ClusterByName();
			Vector vComp = null;
			// OO.insertPageBreak(xSpreadsheet, startColumn, endColumn, row);

			// Allow dynamic translation, Chun Yeong 1 Aug 2011
			OO.insertString(xSpreadsheet,
					trans.tslt(templateLanguage,"COMPETENCY REPORT"),row,0);
			OO.setFontBold(xSpreadsheet,startColumn,endColumn,row,row);
			OO.setFontSize(xSpreadsheet,0,11,row,row,16);
			row += 2;

			if (surveyLevel == 0) { // Competency Level Survey

				int start = 0;
				int startRow = row; // for border
				int endRow = row;
				for (int m = 0; m < vClust.size(); m++) {
					voCluster voClust = (voCluster) vClust.elementAt(m);
					String clusterName = voClust.getClusterName();
					int clusterID = voClust.getClusterID();

					OO.insertString(xSpreadsheet,clusterName,row,column);
					OO.setFontBold(xSpreadsheet,startColumn,endColumn,row,row);
					OO.setBGColor(xSpreadsheet,startColumn,endColumn,row,row,
							BGCOLORCLUSTER);
					OO.setFontSize(xSpreadsheet,0,11,row,row,12);
					OO.setFontType(xSpreadsheet,0,11,row,row,"Times new Roman");
					OO.setRowHeight(xSpreadsheet,row,1,
							ROWHEIGHT * OO.countTotalRow(clusterName,90));
					row++;

					vComp = ClusterCompetencyByName(clusterID);
					vComp = sortClusterCompetencyOrder(vComp);

					for (int a = 0; a < vComp.size(); a++) {
						voCompetency voComp = (voCompetency) vComp.elementAt(a);

						int compID = voComp.getCompetencyID();

						String statement = voComp.getCompetencyName();

					}

					for (int i = 0; i < vComp.size(); i++) {

						// Add by Santoso (22/10/08) : reinitialize array per
						// loop
						// reinitialize the array each loop (otherwise it will
						// use the previous value)
						totalRater = new int[totalRater.length];
						Result = new double[Result.length];

						// Reset only rating[4] since Rating[0]..[3] always have
						// the same value
						/*
						 * if (Rating.length > 4) { Rating[4] = ""; }
						 */

						// Change by Santoso (22/10/08)
						// No need to calculate rowTotal anymore since the
						// raters displayed on the graph are fixed
						// rowTotal = row + 1;
						start = 0;
						int RTID = 0;

						int KBID = 0;
						String KB = "";

						voCompetency voComp = (voCompetency) vComp.elementAt(i);

						int compID = voComp.getCompetencyID();

						String statement = voComp.getCompetencyName();
						String desc = voComp.getCompetencyDefinition();

						startRow = row;

						// Added translation for competency name, Chun Yeong 1
						// Aug 2011
						OO.insertString(
								xSpreadsheet,
								getTranslatedCompetency(
										UnicodeHelper
												.getUnicodeStringAmp(statement))
										.elementAt(0).toString(),row,column);
						OO.setFontSize(xSpreadsheet,0,11,row,row,12);
						OO.setFontType(xSpreadsheet,0,11,row,row,
								"Times new Roman");
						OO.setFontBold(xSpreadsheet,startColumn,endColumn,row,
								row);
						OO.setBGColor(xSpreadsheet,startColumn,endColumn,row,
								row,BGCOLOR);
						row++;

						r1 = row;
						OO.setFontType(xSpreadsheet,0,11,row,row,
								"Times New Roman");
						OO.setFontSize(xSpreadsheet,0,11,row,row,12);
						OO.insertString(xSpreadsheet,
								UnicodeHelper.getUnicodeStringAmp(desc),row,
								column);
						OO.justify(xSpreadsheet,0,11,row,row);
						OO.setFontSize(xSpreadsheet,0,11,row,row,12);
						OO.setFontType(xSpreadsheet,0,11,row,row,
								"Times new Roman");
						OO.mergeCells(xSpreadsheet,startColumn,endColumn,row,
								row);
						// adjust the merged cell as top alignment (Qiao Li 21
						// Dec 2009 )
						OO.setCellAllignment(xSpreadsheet,startColumn,
								endColumn,row,row,2,1);
						OO.setRowHeight(xSpreadsheet,row,1,
								ROWHEIGHT * OO.countTotalRow(desc,90));

						row++;
						start++;

						String RTCode = "";

						Vector RT = RatingTask();

						boolean hasCPRFPR = false;
						// Print out the "N" in other section for simplified
						// report

						for (int j = 0; j < RT.size(); j++) {
							votblSurveyRating vo = (votblSurveyRating) RT
									.elementAt(j);

							RTID = vo.getRatingTaskID();
							RTCode = vo.getRatingCode();

							Vector result = MeanResult(RTID,compID,KBID);

							if (RTCode.equals("CP")) {
								// Changed by Ha 09/07/08 to calculate total
								// rater for each competency for each rating
								// task
								// added back the others for type
								// customization(16 Dec 2009 Qiao Li)
								totalSup = totalSup(1,compID);
								totalSelf = totalSelf(1,compID);

								// Added to cater to the splitting of peers and
								// subordinates, Desmond 21 Oct 09
								/*
								 * Change (s): add in surveyLevel to use
								 * different tables (tblResultCompetency and
								 * tblResultKeyBehavior) to retrieve number of
								 * raters Updated by: Qiao Li 21 Dec 2009
								 */
								totalPeer = totalRater(RTID,compID,-1,"PEER%",
										surveyLevel);

								totalDirect = totalRater(RTID,compID,-1,"DIR%",
										surveyLevel);
								totalIndirect = totalRater(RTID,compID,-1,
										"IDR%",surveyLevel);
								totalAdd = totalRater(RTID,compID,-1,"ADD%",
										surveyLevel);
								totalOth = totalRater(RTID,compID,-1,"OTH%",
										surveyLevel);
								totalSub = totalRater(RTID,compID,-1,"SUB%",
										surveyLevel);

								if (totalDirect < 3 || totalIndirect < 3) {
									combineDirIdr = true;
									combineDIRIDR = true;
								}
								// Re-locate and modified codes to include Peers
								// and Subordinates by Desmond 21 Oct 09
								totalAll = totalSup + totalPeer + totalDirect
										+ totalIndirect;
								boolean weightedAvg = false;
								for (int k = 0; k < result.size(); k++) {
									String[] arrOther = (String[]) result
											.elementAt(k);

									int type = Integer.parseInt(arrOther[1]);
									String t = "";

									if (type == 10) {
										type = 1;
										weightedAvg = true;

									}
									switch (type) {
										case 1 :
											t = "All";

											arrN[iN] = totalAll;
											iN++;
											// Change by Santoso 22/10/08
											// the total rater will be printed
											// later
											// below
											// set the valur of total all here
											totalRater[0] = totalAll;
											break;
										case 2 :
											t = "Superior"; // Change from
															// Supervisors to
															// Superior, Desmond
															// 22
															// Oct 09
											arrN[iN] = totalSup;
											iN++;
											// Change by Santoso 22/10/08
											// the total rater will be printed
											// later
											// below
											// set the valur of total sup here
											totalRater[1] = totalSup;
											break;
										// added back the others for type
										// customization(16 Dec 2009 Qiao Li)
										case 3 :
											t = "Others";
											arrN[iN] = totalOth;
											iN++;
											// Change by Santoso 22/10/08
											// the total rater will be printed
											// later
											// below
											// set the valur of total others
											// here
											totalRater[2] = totalOth;
											break;
										case 4 :
											t = "Self";
											arrN[iN] = totalSelf;
											iN++;
											// Change by Santoso 22/10/08
											// the total rater will be printed
											// later
											// below
											// set the valur of total self here
											totalRater[3] = totalSelf;
											break;

										// Added case 5 and 6 to cater to
										// splitting
										// of Peers and Subordinates by Desmond
										// 21
										// Oct 09
										case 5 :
											t = "Subordinates";
											arrN[iN] = totalSub;
											iN++;
											totalRater[4] = totalSub;
											break;
										case 6 :
											t = "Peers";
											arrN[iN] = totalPeer;
											iN++;
											totalRater[5] = totalPeer;
											break;

										// Added Additional Raters
										case 7 :
											t = "Additional";
											arrN[iN] = totalAdd;
											iN++;
											totalRater[6] = totalAdd;
											break;

										case 8 :
											t = "Direct";
											arrN[iN] = totalDirect;
											iN++;
											totalRater[7] = totalDirect;
											break;

										case 9 :
											t = "Indirect";
											arrN[iN] = totalIndirect;
											iN++;
											totalRater[8] = totalIndirect;
											break;

									}

									// Change by Santoso 22/10/08
									// Since we have fixed the order of rating
									// (according to the type)
									// we can set the Rating text here using
									// type-1 as the index

									// if direct and indirect <3 classify them
									// as subordinate
									//

									Rating[type - 1] = RTCode + "(" + t + ")";
									if (iReportType == 1)
										Rating[type - 1] = Rating[type - 1]
												.replaceAll("\n"," ");

									Result[type - 1] = Double
											.parseDouble(arrOther[2]);

									// If i don't split..means = others
									// Result[2] = value;
								}
								if (weightedAvg == true) {
									Rating[0] = RTCode + "(All)";
									String[] weightedAvgRow = (String[]) result
											.elementAt(result.size() - 1);

									double weightedAvgScore = Double
											.parseDouble(df.format(Double
													.parseDouble(weightedAvgRow[2])));
									Result[0] = weightedAvgScore;
								}
							}
						}

						// rater type can change depends on whether "Others" is
						// splitted
						// get the appropriate number and prepareCells(Qiao Li
						// 17 Dec 2009)
						int totRaterType = Rating.length;
						if (splitOthers == 0) {
							totRaterType -= 3;
						} else {
							totRaterType -= 1;
						}
						totRaterType = 6; // special case for SPF (All,
											// Superior, Peer, Direct, Indirect,
											// and Self)

						// cater for combination of both direct and indirect
						// reports into 1.
						if (totalDirect < 3 || totalIndirect < 3) {
							totRaterType -= 1;// so that the line spacing will
												// not go off for the count for
												// the graph.
							double directScore = Result[7];
							double indirectScore = Result[8];
							double newGroupScore = (directScore + indirectScore) / 2;
							Rating[8] = "nil";
							Result[8] = -1;
							Rating[7] = "CP(Subordinates)";
							Result[7] = Double.parseDouble(df
									.format((newGroupScore)));
							totalRater[8] = 0;
							totalRater[7] = totalDirect + totalIndirect;;

						}// while RT

						// Change by Santoso 22/10/08
						// Alignment of n number with the bar of the graph
						// was : rowTotal = row + 11;
						// int[] rowPos = prepareCells(xSpreadsheet, row,
						// totalRater.length);
						int[] rowPos = prepareCells(xSpreadsheet,row,
								totRaterType);
						row++; // start draw chart from here
						OO.setFontSize(12);
						System.out.println("Category 1");
						// Change by Santoso 22/10/08
						// the total rater will be printed in drawChart
						// therefore we need
						// to pass the rating point and the position
						drawChart(Rating,Result,totalRater,rowPos,maxScale,
								splitOthers);

						// 12/12/2009 Denise
						// reinitialize Result and totalRater
						// change from 5 to totalType for customization (16 Dec
						// 2009 Qiao Li)
						Result = new double[totalType + totalOtherRT];
						totalRater = new int[totalType + totalOtherRT];

						column = 9; // write the importance n gap
						int rtemp = row;

						Vector vImportance = Importance(compID,KBID);

						for (int j = 0; j < vImportance.size(); j++) {
							String[] arr = (String[]) vImportance.elementAt(j);
							String task = arr[1];
							double taskResult = Double.parseDouble(arr[2]);

							OO.insertString(xSpreadsheet,task + ": "
									+ taskResult,rtemp,column);
							OO.mergeCells(xSpreadsheet,column,endColumn,rtemp,
									rtemp + 1);
							OO.setCellAllignment(xSpreadsheet,column,endColumn,
									rtemp,rtemp + 1,2,1);

							rtemp += 3;
						}

						double gap = 0;

						// Change by Santoso 22/10/08
						// only calculate Gap if survey include CPR/FPR Rating
						// task
						if (hasCPRFPR) {
							gap = getAvgGap(compID);
							// If CPR is chosen in this survey
							{
								// Changed the default language to English by
								// Chun Yeong 9 Jun 2011
								// Commented away to allow translation below,
								// Chun Yeong 1 Aug 2011
								/*
								 * if (ST.LangVer == 2) //Indonesian
								 * OO.insertString(xSpreadsheet, "Selisih = " +
								 * gap, rtemp, column); else //if (ST.LangVer ==
								 * 1) English OO.insertString(xSpreadsheet,
								 * "Gap = " + gap, rtemp, column);
								 */

								// Allow dynamic translation, Chun Yeong 1 Aug
								// 2011
								OO.insertString(xSpreadsheet,
										trans.tslt(templateLanguage,"Gap")
												+ " = " + gap,rtemp,column);
								OO.mergeCells(xSpreadsheet,column,endColumn,
										rtemp,rtemp + 1);

								OO.setCellAllignment(xSpreadsheet,column,
										endColumn,rtemp,rtemp + 1,2,1);
							}
						}
						rtemp += 3;

						double LOA = LevelOfAgreement(compID,KBID);
						// Changed the default language to English by Chun Yeong
						// 9 Jun 2011
						// Commented away to allow translation below, Chun Yeong
						// 1 Aug 2011
						/*
						 * if (ST.LangVer == 2) //Indonesian
						 * OO.insertString(xSpreadsheet,
						 * "Tingkat Persetujuan: \n" + LOA + "%", rtemp,
						 * column); else //if (ST.LangVer == 1) English
						 * OO.insertString(xSpreadsheet,
						 * "Level Of Agreement: \n" + LOA + "%", rtemp, column);
						 */

						// Allow dynamic translation, Chun Yeong 1 Aug 2011
						OO.insertString(
								xSpreadsheet,
								trans.tslt(templateLanguage,
										"Level Of Agreement")
										+ ": \n"
										+ LOA
										+ "%",rtemp,column);
						OO.mergeCells(xSpreadsheet,column,endColumn,rtemp,
								rtemp + 2);
						OO.setCellAllignment(xSpreadsheet,column,endColumn,
								rtemp,rtemp + 2,2,1);

						column = 0;
						row += 15;

						endRow = row - 1;

						// comp name and definition
						OO.setTableBorder(xSpreadsheet,startColumn,endColumn,
								startRow,startRow + 1,false,false,true,true,
								true,true);

						// total sup n others
						OO.setTableBorder(xSpreadsheet,startColumn,startColumn,
								startRow + 2,endRow,false,false,true,true,true,
								true);
						// chart
						OO.setTableBorder(xSpreadsheet,startColumn + 1,8,
								startRow + 2,endRow,false,false,true,true,true,
								true);
						OO.setTableBorder(xSpreadsheet,9,endColumn,
								startRow + 2,endRow,false,false,true,true,true,
								true);

						OO.setCellAllignment(xSpreadsheet,startColumn,
								startColumn,startRow + 2,endRow,1,2);

						row++;
						row++;
						// Insert narrative comments here
						int startBorder1 = 1;
						int startBorder = 1;
						int endBorder = 1;
						int endBorder1 = 1;
						int selfIncluded = Q.SelfCommentIncluded(surveyID);
						int column = 0;

						// added by Ping Yang on 11/08/08, check raters assigned
						boolean blnSupIncluded = Q.SupCommentIncluded(surveyID,
								targetID);
						startBorder = row;
						count = 0;

						count++;

						int statementPos = row; // Denise 08/01/2009 store
												// position to insert competency
												// description

						row++;

						// Added by Ha 23/06/08 reset the value start to print
						// the header of comment correctly
						start = 0;
						Vector supComments = getComments("SUP%",compID,KBID);

						// Added variables to store comments from peers and
						// subordinates, Desmond 18 Nov 09
						Vector dirComments = getComments("DIR%",compID,KBID);
						Vector peerComments = getComments("PEER%",compID,KBID);
						Vector indirComments = getComments("IDR%",compID,KBID);
						int commentSize = supComments.size()
								+ dirComments.size() + peerComments.size()
								+ indirComments.size();
						if (blnSupIncluded) {// added by Ping Yang on 11/08/08,
												// check raters assigned
							boolean blnSupCommentExists = false;// Added by ping
																// yang on
																// 31/7/08 to
																// get rid of
																// extra '-'s

							for (int j = 0; j < supComments.size(); j++) {
								String[] arr = (String[]) supComments
										.elementAt(j);
								if (start == 0) {
									OO.setFontSize(xSpreadsheet,0,11,row,row,12);
									OO.setFontType(xSpreadsheet,0,11,row,row,
											"Times new Roman");
									OO.insertString(xSpreadsheet,trans.tslt(
											templateLanguage,"Superior"),row,
											column + 1); // Change from
															// Supervisors
															// to Superior,
															// Desmond 22
															// Oct 09
									OO.setFontBold(xSpreadsheet,startColumn,
											endColumn,row,row);
									OO.setFontItalic(xSpreadsheet,startColumn,
											endColumn,row,row);

									row++;
									start++;
								}
								OO.setFontSize(xSpreadsheet,9,11,row,row,12);
								OO.setFontType(xSpreadsheet,0,11,row,row,
										"Times new Roman");
								String comment = arr[1];
								if (!comment.trim().equals("")) {// Added by
																	// ping yang
																	// on
																	// 31/7/08
																	// to get
																	// rid of
																	// extra
																	// '-'s
									OO.setFontSize(xSpreadsheet,0,11,row,row,12);
									OO.setFontType(xSpreadsheet,0,11,row,row,
											"Times new Roman");

									OO.insertString(xSpreadsheet,UnicodeHelper
											.getUnicodeStringAmp(comment),row,
											column + 1);
									OO.justify(xSpreadsheet,column + 1,
											column + 1,row,row);
									OO.mergeCells(xSpreadsheet,column + 1,
											endColumn,row,row);
									OO.setRowHeight(
											xSpreadsheet,
											row,
											column + 1,
											ROWHEIGHT
													* OO.countTotalRow(comment,
															90));
									OO.setCellAllignment(xSpreadsheet,
											startColumn,startColumn,row,row,2,1);
									row++;
									blnSupCommentExists = true;
								}

							}

							start = 0;

							/*
							 * Change(s) : Added codes to point to next row
							 * below if exist rater code comments. Remove codes
							 * that add default empty comment in the report if
							 * rater code have no comments Reason(s) : To remove
							 * empty narrative comments by rater category, KB
							 * then Competency. i.e If competency has no
							 * comments from raters, remove the entire
							 * competency in the narrative comments. Updated By:
							 * Sebastian Updated On: 19 July 2010
							 */
							if (supComments.size() > 0) {
								row++;
							}
						}// end if(blnSupIncluded)

						boolean blnPeerCommentExist = false; // Added to get rid
																// of extra '-'s
						for (int k = 0; k < peerComments.size(); k++) {
							String[] arr = (String[]) peerComments.elementAt(k);
							String comment = arr[1];

							if (start == 0) {
								// Allow dynamic translation, Chun Yeong 1 Aug
								// 2011
								OO.setFontSize(xSpreadsheet,0,11,row,row,12);
								OO.setFontType(xSpreadsheet,0,11,row,row,
										"Times new Roman");
								OO.insertString(xSpreadsheet,
										trans.tslt(templateLanguage,"Peer(s)"),
										row,column + 1);
								OO.setFontBold(xSpreadsheet,startColumn,
										endColumn,row,row);
								OO.setFontItalic(xSpreadsheet,startColumn,
										endColumn,row,row);

								start++;
								row++;
							}

							if (!comment.trim().equals("")) {
								OO.setFontSize(xSpreadsheet,0,11,row,row,12);
								OO.setFontType(xSpreadsheet,0,11,row,row,
										"Times new Roman");
								comment = "- " + comment;
								OO.insertString(xSpreadsheet,UnicodeHelper
										.getUnicodeStringAmp(comment),row,
										column + 1);
								OO.justify(xSpreadsheet,column + 1,column + 1,
										row,row);
								OO.mergeCells(xSpreadsheet,column + 1,
										endColumn,row,row);
								OO.setRowHeight(
										xSpreadsheet,
										row,
										column + 1,
										ROWHEIGHT
												* OO.countTotalRow(comment,90));
								OO.setCellAllignment(xSpreadsheet,startColumn,
										endColumn,row,row,2,1);

								row++;
								blnPeerCommentExist = true;
							}
						}

						// Adjust counters
						start = 0;

						if (peerComments.size() > 0) {
							row++;
						}
						if (!combineDirIdr) {
							// Added to get rid
							boolean blnDirCommentExist = false; // of extra '-'s
							for (int k = 0; k < dirComments.size(); k++) {
								String[] arr = (String[]) dirComments
										.elementAt(k);
								String comment = arr[1];

								if (start == 0) {
									// Allow dynamic translation, Chun Yeong 1
									// Aug
									// 2011
									OO.setFontSize(xSpreadsheet,0,11,row,row,12);
									OO.setFontType(xSpreadsheet,0,11,row,row,
											"Times new Roman");
									OO.insertString(xSpreadsheet,trans
											.tslt(templateLanguage,
													"Direct Report(s)"),row,
											column + 1);
									OO.setFontBold(xSpreadsheet,startColumn,
											endColumn,row,row);
									OO.setFontItalic(xSpreadsheet,startColumn,
											endColumn,row,row);

									start++;
									row++;
								}
								OO.setFontSize(xSpreadsheet,0,11,row,row,12);
								OO.setFontType(xSpreadsheet,0,11,row,row,
										"Times new Roman");
								if (!comment.trim().equals("")) {
									comment = "- " + comment;
									OO.insertString(xSpreadsheet,UnicodeHelper
											.getUnicodeStringAmp(comment),row,
											column + 1);
									OO.justify(xSpreadsheet,column + 1,
											column + 1,row,row);
									OO.mergeCells(xSpreadsheet,column + 1,
											endColumn,row,row);
									OO.setRowHeight(
											xSpreadsheet,
											row,
											column + 1,
											ROWHEIGHT
													* OO.countTotalRow(comment,
															90));
									OO.setCellAllignment(xSpreadsheet,
											startColumn,endColumn,row,row,2,1);

									row++;
									blnDirCommentExist = true;
								}
							}

							// Adjust counters
							start = 0;

							if (dirComments.size() > 0) {
								row++;
							}

							boolean blnIndirCommentExist = false; // Added to
																	// get
																	// rid of
																	// extra
																	// '-'s
							for (int k = 0; k < indirComments.size(); k++) {
								String[] arr = (String[]) indirComments
										.elementAt(k);
								String comment = arr[1];
								OO.setFontSize(xSpreadsheet,0,11,row,row,12);
								OO.setFontType(xSpreadsheet,0,11,row,row,
										"Times new Roman");
								if (start == 0) {
									// Allow dynamic translation, Chun Yeong 1
									// Aug
									// 2011
									OO.insertString(xSpreadsheet,trans.tslt(
											templateLanguage,
											"Indirect Report(s)"),row,
											column + 1);
									OO.setFontBold(xSpreadsheet,startColumn,
											endColumn,row,row);
									OO.setFontItalic(xSpreadsheet,startColumn,
											endColumn,row,row);

									start++;
									row++;
								}
								OO.setFontSize(xSpreadsheet,0,11,row,row,12);
								OO.setFontType(xSpreadsheet,0,11,row,row,
										"Times new Roman");
								if (!comment.trim().equals("")) {
									comment = "- " + comment;
									OO.insertString(xSpreadsheet,UnicodeHelper
											.getUnicodeStringAmp(comment),row,
											column + 1);
									OO.mergeCells(xSpreadsheet,column + 1,
											endColumn,row,row);
									OO.justify(xSpreadsheet,column + 1,
											column + 1,row,row);
									OO.setRowHeight(
											xSpreadsheet,
											row,
											column + 1,
											ROWHEIGHT
													* OO.countTotalRow(comment,
															90));
									OO.setCellAllignment(xSpreadsheet,
											startColumn,endColumn,row,row,2,1);

									// row++;
									blnIndirCommentExist = true;
								}
							}

							// Adjust counters
							start = 0;

							if (indirComments.size() > 0) {
								row++;
							}
						} else {
							// if indirect or direct less than 3, combine them
							// to form 1 group called subordinates
							for (int k = 0; k < dirComments.size(); k++) {
								boolean blnDirCommentExist = false;
								String[] arr = (String[]) dirComments
										.elementAt(k);
								String comment = arr[1];

								if (start == 0) {
									// Allow dynamic translation, Chun Yeong 1
									// Aug
									// 2011
									OO.setFontSize(xSpreadsheet,0,11,row,row,12);
									OO.setFontType(xSpreadsheet,0,11,row,row,
											"Times new Roman");
									OO.insertString(xSpreadsheet,trans.tslt(
											templateLanguage,
											"Subordinate Report(s)"),row,
											column + 1);
									OO.setFontBold(xSpreadsheet,startColumn,
											endColumn,row,row);
									OO.setFontItalic(xSpreadsheet,startColumn,
											endColumn,row,row);

									start++;
									row++;
								}
								OO.setFontSize(xSpreadsheet,0,11,row,row,12);
								OO.setFontType(xSpreadsheet,0,11,row,row,
										"Times new Roman");
								if (!comment.trim().equals("")) {
									comment = "- " + comment;
									OO.insertString(xSpreadsheet,UnicodeHelper
											.getUnicodeStringAmp(comment),row,
											column + 1);
									OO.justify(xSpreadsheet,column + 1,
											column + 1,row,row);
									OO.mergeCells(xSpreadsheet,column + 1,
											endColumn,row,row);
									OO.setRowHeight(
											xSpreadsheet,
											row,
											column + 1,
											ROWHEIGHT
													* OO.countTotalRow(comment,
															90));
									OO.setCellAllignment(xSpreadsheet,
											startColumn,endColumn,row,row,2,1);

									row++;
									blnDirCommentExist = true;
								}
							}
							boolean blnIndirCommentExist = false; // Added to
							row++; // get
							// rid of extra
							// '-'s
							for (int k = 0; k < indirComments.size(); k++) {
								String[] arr = (String[]) indirComments
										.elementAt(k);
								String comment = arr[1];
								if (dirComments.size() == 0) {
									// Allow dynamic translation, Chun Yeong 1
									// Aug
									// 2011
									OO.setFontSize(xSpreadsheet,0,11,row,row,12);
									OO.setFontType(xSpreadsheet,0,11,row,row,
											"Times new Roman");
									OO.insertString(xSpreadsheet,trans.tslt(
											templateLanguage,
											"Subordinate Report(s)"),row,
											column + 1);
									OO.setFontBold(xSpreadsheet,startColumn,
											endColumn,row,row);
									OO.setFontItalic(xSpreadsheet,startColumn,
											endColumn,row,row);

									start++;
									row++;
								}
								OO.setFontSize(xSpreadsheet,0,11,row,row,12);
								OO.setFontType(xSpreadsheet,0,11,row,row,
										"Times new Roman");
								if (!comment.trim().equals("")) {
									comment = "- " + comment;
									OO.insertString(xSpreadsheet,UnicodeHelper
											.getUnicodeStringAmp(comment),row,
											column + 1);
									OO.mergeCells(xSpreadsheet,column + 1,
											endColumn,row,row);
									OO.justify(xSpreadsheet,column + 1,
											column + 1,row,row);
									OO.setRowHeight(
											xSpreadsheet,
											row,
											column + 1,
											ROWHEIGHT
													* OO.countTotalRow(comment,
															90));
									OO.setCellAllignment(xSpreadsheet,
											startColumn,endColumn,row,row,2,1);

									// row++;
									blnIndirCommentExist = true;
								}
							}

							// Adjust counters
							start = 0;

							if (indirComments.size() > 0) {
								row++;
							}

						}
						// changed the order of comments by moving "Self" to the
						// back
						// Qiao Li 23 Dec 2009
						boolean selfCommentExist = false;
						if (selfIncluded == 1) {
							selfCommentExist = true;
							Vector selfComments = getComments("SELF",compID,
									KBID);
							OO.setFontSize(xSpreadsheet,0,11,row,row,12);
							OO.setFontType(xSpreadsheet,0,11,row,row,
									"Times new Roman");
							if (selfComments != null) {
								boolean blnSelfCommentExists = false;// Added by
																		// ping
																		// yang
																		// on
																		// 31/7/08
																		// to
																		// get
																		// rid
																		// of
																		// extra
																		// '-'s
								for (int j = 0; j < selfComments.size(); j++) {
									String[] arr = (String[]) selfComments
											.elementAt(j);
									if (start == 0) {
										// Changed the default language to
										// English by Chun Yeong 9 Jun 2011
										// Commented away to allow translation
										// below, Chun Yeong 1 Aug 2011
										/*
										 * if (ST.LangVer == 2)
										 * OO.insertString(xSpreadsheet,
										 * "Diri Sendiri", row, column+1); else
										 * //if (ST.LangVer == 1)
										 * OO.insertString(xSpreadsheet, "Self",
										 * row, column+1);
										 */

										// Allow dynamic translation, Chun Yeong
										// 1 Aug 2011
										OO.setFontSize(xSpreadsheet,0,11,row,
												row,12);
										OO.setFontType(xSpreadsheet,0,11,row,
												row,"Times new Roman");
										OO.insertString(xSpreadsheet,trans
												.tslt(templateLanguage,"Self"),
												row,column + 1);
										OO.setFontBold(xSpreadsheet,
												startColumn,endColumn,row,row);
										OO.setFontItalic(xSpreadsheet,
												startColumn,endColumn,row,row);

										row++;
										start++;
									}

									String comment = arr[1];
									OO.setFontSize(xSpreadsheet,0,11,row,row,12);
									OO.setFontType(xSpreadsheet,0,11,row,row,
											"Times new Roman");
									if (!comment.trim().equals("")) {// Added by
																		// ping
																		// yang
																		// on
																		// 31/7/08
																		// to
																		// get
																		// rid
																		// of
																		// extra
																		// '-'s
										comment = "- " + comment;
										OO.insertString(
												xSpreadsheet,
												UnicodeHelper
														.getUnicodeStringAmp(comment),
												row,column + 1);
										OO.justify(xSpreadsheet,column + 1,
												column + 1,row,row);
										OO.mergeCells(xSpreadsheet,column + 1,
												endColumn,row,row);
										OO.setRowHeight(
												xSpreadsheet,
												row,
												column + 1,
												ROWHEIGHT
														* OO.countTotalRow(
																comment,109));

										OO.setCellAllignment(xSpreadsheet,
												startColumn,startColumn,row,
												row,2,1);

										row++;
										blnSelfCommentExists = true;
									}
								}

								if (selfComments.size() > 0) {
									row++;
								}
							} else {
								start = 0;
								row++;
							}
							row++;
						}

						endBorder = row - 1;

						/*
						 * Change(s) : Added codes to check height of the table
						 * to be added, and insert pagebreak if necessary
						 * Reason(s) : Fix the problem of a table being spilt
						 * into between two pages. Updated By: Alvis Updated On:
						 * 07 Aug 2009
						 */

						// Added translation to the competency name, Chun Yeong
						// 1 Aug 2011
						OO.insertString(xSpreadsheet,"Narrative Comments",
								statementPos,column + 1);
						OO.setFontSize(xSpreadsheet,0,11,statementPos,
								statementPos,12);
						OO.setFontType(xSpreadsheet,0,11,statementPos,
								statementPos,"Times New Roman");
						if (commentSize == 0 || selfCommentExist == false) {
							OO.insertString(xSpreadsheet,
									"No comments provided",statementPos + 1,
									column + 1);
						}

						OO.setFontBold(xSpreadsheet,startColumn,endColumn,
								statementPos,statementPos);
						OO.setBGColor(xSpreadsheet,startColumn,endColumn,
								statementPos,statementPos,BGCOLOR);

						startBorder = row;

						OO.insertPageBreak(xSpreadsheet,startColumn,endColumn,
								row);

					}// while Comp
					if (m == (vClust.size() - 1)) { // ensure next section is in
													// a new page
						OO.insertPageBreak(xSpreadsheet,startColumn,endColumn,
								row);
					}
				}// end of for vClust.size
			}// end if surveyLevel KB
		}// end if breakCPR==0
	} // End InsertClusterCompetency()

	/*
	 * Change: added in SurveyID to see whether "Others" is splitted Updated by:
	 * Qiao Li Date: 17 Dec 2009
	 */
	public void InsertCompetency(int reportType, int SurveyID, int breakCPR)
			throws SQLException, Exception {

		int iN = 0; // To be used as counter for arrN
		int[] address = OO.findString(xSpreadsheet,"<Report>");
		column = address[0];
		row = address[1];
		OO.findAndReplace(xSpreadsheet,"<Report>","");
		int surveyLevel = Integer.parseInt(surveyInfo[0]);

		if (breakCPR == 0) { // not breaking down the CPR(All)
			// Change by Santoso (22/10/08)
			// store the result of totalOtherRT to be reused below
			int totalOtherRT = totalOtherRT();
			int total = totalGroup() + totalOtherRT + 1; // 1 for all
			// added in a variable to store number of types we have (16 Dec 2009
			// Qiao Li)
			int totalType = 7;// Changed to 7 from 6 to add on addtional raters
								// (Albert 22 Aug 2012)

			// used variable totalType to determine the initialization of array
			// size (16 Dec 2009 Qiao Li)
			String[] Rating = new String[totalType + totalOtherRT]; // Changed
																	// from 4 +
																	// totalOtherRT
																	// to 5 +
																	// totalOtherRT,
																	// to clean
																	// up later
																	// on, DeZ
			double[] Result = new double[totalType + totalOtherRT]; // Changed
																	// from 4 +
																	// totalOtherRT
																	// to 5 +
																	// totalOtherRT,
																	// to clean
																	// up later
																	// on, DeZ
			int[] totalRater = new int[totalType + totalOtherRT]; // Changed
																	// from 4 +
																	// totalOtherRT
																	// to 5 +
																	// totalOtherRT,
																	// to clean
																	// up later
																	// on, DeZ

			// initialize Rating --> CP only, will be replace later on in this
			// method if scores exists for that row, added comments here,
			// Desmond 22 Oct 09
			Rating[0] = "CP(All)";
			Rating[1] = "CP(Superior)"; // Change from Supervisors to Superior,
										// Desmond 22 Oct 09
			// added back the others for type customization(16 Dec 2009 Qiao Li)
			Rating[2] = "CP(Others)"; // Commented away to remove CP(Others) to
										// cater for splitting Subordinates and
										// Peers, Desmond 22 Oct 09
			Rating[3] = "CP(Self)";
			// Add additional categories for bar graph to cater for splitting of
			// Subordinates and Peers, Desmond 21 Oct 09
			Rating[4] = "CP(Subordinates)";
			Rating[5] = "CP(Peers)";
			Rating[6] = "CP(Additional)";

			int maxScale = MaxScale();

			int count = 0; // to count total chart for each page, max = 2;
			int r1 = 1;
			int add = 13 / total;
			// added back the others for type customization(16 Dec 2009 Qiao Li)
			int totalOth = 0; // Commented away to remove CP(Others) to cater
								// for splitting Subordinates and Peers, Desmond
								// 22 Oct 09
			int totalSup = 0;
			int totalSelf = 0;
			int totalAll = 0;

			// Added new variables in order to cater for splitting of
			// Subordinates and Peers, Desmond 21 Oct 2009
			int totalPeer = 0;
			int totalSub = 0;
			// addded new variable to cater for additional raters, Albert 22 Aug
			// 2012
			int totalAdd = 0;

			Vector vComp = CompetencyByName();

			OO.insertPageBreak(xSpreadsheet,startColumn,endColumn,row);

			// Allow dynamic translation, Chun Yeong 1 Aug 2011
			OO.insertString(xSpreadsheet,
					trans.tslt(templateLanguage,"Competency Report"),row,0);
			OO.setFontBold(xSpreadsheet,startColumn,endColumn,row,row);

			row += 2;

			if (surveyLevel == 0) { // Competency Level Survey
				System.out
						.println("InsertCompetency() - Survey is at Competency Level"); // To
																						// Remove,
																						// Dez

				int endRow = row;
				// Added by Alvis 06-Aug-09 for pagebreak fix implemented below
				int currentPageHeight = 1076;// starting page height is set to
												// 1076 to include the title of
												// the competency report.

				for (int i = 0; i < vComp.size(); i++) {

					voCompetency voComp = (voCompetency) vComp.elementAt(i);

					int compID = voComp.getCompetencyID();
					String statement = voComp.getCompetencyName();
					String desc = voComp.getCompetencyDefinition();

					int startRow = row; // for border
					int RTID = 0;
					int KBID = 0;

					int statementPos = row;
					row++;

					r1 = row;
					// Added tranlsation for the competency definition, Chun
					// Yeong 1 Aug 2011
					OO.insertString(xSpreadsheet,
							getTranslatedCompetency(statement).elementAt(1)
									.toString(),row,0);
					OO.mergeCells(xSpreadsheet,startColumn,endColumn,row,row);
					// adjust the merged cell as top alignment (Qiao Li 21 Dec
					// 2009 )
					OO.setCellAllignment(xSpreadsheet,startColumn,endColumn,
							row,row,2,1);
					OO.setRowHeight(xSpreadsheet,row,1,
							ROWHEIGHT * OO.countTotalRow(desc,90));
					row++;

					String RTCode = "";

					Vector RT = RatingTask();
					int r = 0;

					boolean hasCPRFPR = false;
					for (int j = 0; j < RT.size(); j++) {
						votblSurveyRating vo = (votblSurveyRating) RT
								.elementAt(j);

						RTID = vo.getRatingTaskID();
						RTCode = vo.getRatingCode();

						Vector result = MeanResult(RTID,compID,KBID);

						if (RTCode.equals("CP")) {
							// Changed by Ha 09/07/08 to calculate total rater
							// for each competency for each rating task
							// added back the others for type customization(16
							// Dec 2009 Qiao Li)
							totalOth = totalOth(1,compID); // Commented away to
															// remove CP(Others)
															// to cater for
															// splitting
															// Subordinates and
															// Peers, Desmond 22
															// Oct 09
							totalSup = totalSup(1,compID);
							totalSelf = totalSelf(1,compID);

							// Added to cater to the splitting of peers and
							// subordinates, Desmond 21 Oct 09
							/*
							 * Change (s): add in surveyLevel to use different
							 * tables (tblResultCompetency and
							 * tblResultKeyBehavior) to retrieve number of
							 * raters Updated by: Qiao Li 21 Dec 2009
							 */

							totalPeer = totalRater(RTID,compID,-1,"PEER%",
									surveyLevel);
							totalSub = totalRater(RTID,compID,-1,"SUB%",
									surveyLevel);
							totalAdd = totalRater(RTID,compID,-1,"ADD%",
									surveyLevel);

							// Re-locate and modified codes to include Peers and
							// Subordinates by Desmond 21 Oct 09
							totalAll = totalSup + totalPeer + totalSub
									+ totalOth + totalAdd;

							for (int k = 0; k < result.size(); k++) {
								String[] arrOther = (String[]) result
										.elementAt(k);

								int type = Integer.parseInt(arrOther[1]);
								String t = "";
								switch (type) {
									case 1 :
										t = "All";

										arrN[iN] = totalAll;
										iN++;
										// Change by Santoso 22/10/08
										// the total rater will be printed later
										// below
										// set the valur of total all here
										totalRater[0] = totalAll;
										break;
									case 2 :
										t = "Superior"; // Change from
														// Supervisors
														// to Superior, Desmond
														// 22
														// Oct 09
										arrN[iN] = totalSup;
										iN++;
										// Change by Santoso 22/10/08
										// the total rater will be printed later
										// below
										// set the valur of total sup here
										totalRater[1] = totalSup;
										break;
									// added back the others for type
									// customization(16 Dec 2009 Qiao Li)
									case 3 :
										t = "Others";
										arrN[iN] = totalOth;
										iN++;
										// Change by Santoso 22/10/08
										// the total rater will be printed later
										// below
										// set the valur of total others here
										totalRater[2] = totalOth;
										break;
									case 4 :
										t = "Self";
										arrN[iN] = totalSelf;
										iN++;
										// Change by Santoso 22/10/08
										// the total rater will be printed later
										// below
										// set the valur of total self here
										totalRater[3] = totalSelf;
										break;

									// Added case 5 and 6 to cater to splitting
									// of
									// Peers and Subordinates by Desmond 21 Oct
									// 09
									case 5 :
										t = "Subordinates";
										arrN[iN] = totalSub;
										iN++;
										totalRater[4] = totalSub;
										break;
									case 6 :
										t = "Peers";
										arrN[iN] = totalPeer;
										iN++;
										totalRater[5] = totalPeer;
										break;

									// Added Additional Raters
									case 7 :
										t = "Additional";
										arrN[iN] = totalAdd;
										iN++;
										totalRater[6] = totalAdd;
										break;
								}

								// Change by Santoso 22/10/08
								// Since we have fixed the order of rating
								// (according to the type)
								// we can set the Rating text here using type-1
								// as the index
								Rating[type - 1] = RTCode + "(" + t + ")";
								if (iReportType == 1)
									Rating[type - 1] = Rating[r].replaceAll(
											"\n"," ");

								Result[type - 1] = Double
										.parseDouble(arrOther[2]);

								// If i don't split..means = others
								// Result[2] = value;
							}
						} else if (RTCode.equals("CPR") || RTCode.equals("FPR")) {
							// Changed by Ha 26/06/08 should not have j <
							// result.size in the condition
							// Problem with old condition: value were not
							// displayed correctly
							if (result.size() > 0) {
								// Change by Santoso 22/10/08
								// hasCPRFPR will be needed later, we need to
								// set the value to true here
								hasCPRFPR = true;
								String[] arrOther = (String[]) result
										.elementAt(0);

								// Should not insert a "\n". Will push col into
								// 2 rows. Printing will go haywire (Maruli)
								// Rating[r] = RTCode + "\n(All)";

								/*
								 * Change(s) : Change to get the correct number
								 * of raters for CPR when there is a split
								 * Updated By: Mark Oei 1 Mar 2010 Previous
								 * Updates: - Change by Santoso 22/10/08 :
								 * Rating order and value already initialized
								 * above (also the Result) we need to set the
								 * RTCode and the result at the appropriate
								 * position as already defined above - Change
								 * from 5 to totalType for customization (16 Dec
								 * 2009 Qiao Li)
								 */
								totalOth = totalRater(2,compID,-1,"OTH%",
										surveyLevel);
								totalSup = totalRater(2,compID,-1,"SUP%",
										surveyLevel);

								totalPeer = totalRater(2,compID,-1,"PEER%",
										surveyLevel);
								totalSub = totalRater(2,compID,-1,"SUB%",
										surveyLevel);

								totalAdd = totalRater(2,compID,-1,"ADD%",
										surveyLevel);

								Rating[totalType] = RTCode + "(All)"; // Changed
																		// from
																		// Rating[4]
																		// to
																		// Rating[5]
																		// to
																		// cater
																		// for
																		// new
																		// catergories
																		// Subordinates
																		// &
																		// Peers,
																		// Desmond
																		// 22
																		// Oct
																		// 09

								if (iReportType == 1)
									Rating[totalType] = Rating[totalType]
											.replaceAll("\n"," "); // Changed
																	// from
																	// Rating[4]
																	// to
																	// Rating[5]
																	// to cater
																	// for new
																	// catergories
																	// Subordinates
																	// & Peers,
																	// Desmond
																	// 22 Oct 09

								Result[totalType] = Double
										.parseDouble(arrOther[2]); // Changed
																	// from
																	// Rating[4]
																	// to
																	// Rating[5]
																	// to cater
																	// for new
																	// catergories
																	// Subordinates
																	// & Peers,
																	// Desmond
																	// 22 Oct 09

								if (RTCode.equals("CPR")) {
									// Change to get correct number of raters
									// for CPR when
									// there is a split, Mark Oei 01 Mar 2010
									if (splitOthers == 0)
										totalAll = totalSup(2,compID)
												+ totalOth(2,compID) + totalAdd;
									else
										totalAll = totalSup(2,compID)
												+ totalPeer + totalSub
												+ totalAdd;
								} else if (RTCode.equals("FPR"))
									totalAll = totalSup(3,compID)
											+ totalOth(3,compID) + totalAdd;

								arrN[iN] = totalAll;
								iN++;

								// Change by Santoso 22/10/08
								// the total rater will be printed later below
								// set the valur of total all here
								totalRater[totalType] = totalAll; // Changed
																	// from
																	// totalRater[4]
																	// to
																	// totalRater[5]
																	// to cater
																	// for new
																	// catergories
																	// Subordinates
																	// & Peers,
																	// Desmond
																	// 22 Oct 09

								// 11/12/2009 Denise
								// Change position to display CPR/FPR on the top
								// change from 5 to totalType for customization
								// (16 Dec 2009 Qiao Li)
								double CPRFPR_Result = Result[totalType];
								int CPRFPR_TotalRating = totalRater[totalType];

								// change from 5 to totalType for customization
								// (16 Dec 2009 Qiao Li)
								for (int x = totalType; x > 0; x--) {
									totalRater[x] = totalRater[x - 1];
									Result[x] = Result[x - 1];
								}

								Result[0] = CPRFPR_Result;
								totalRater[0] = CPRFPR_TotalRating;
								// change from 5 to totalType for customization
								// (17 Dec 2009 Qiao Li)
								Rating[0] = Rating[totalType];
								Rating[1] = "CP(All)";
								Rating[2] = "CP(Superior)";
								// added back the others for type
								// customization(16 Dec 2009 Qiao Li)
								// Change CP(Other) to CP(Others) in order for
								// the competency bar graph
								// to be displayed for split with CPR, 01 Mar
								// 2010 Mark Oei
								Rating[3] = "CP(Others)";
								Rating[4] = "CP(Self)";
								Rating[5] = "CP(Subordinates)";
								Rating[6] = "CP(Peers)";
								Rating[7] = "CP(Additional)";
							}
						}
					}
					// rater type can change depends on whether "Others" is
					// splitted
					// get the appropriate number and prepareCells(Qiao Li 17
					// Dec 2009)
					int totRaterType = Rating.length;
					if (splitOthers == 0) {
						totRaterType -= 3;// minus subordinates, peers, and
											// additional
					} else {
						totRaterType -= 1; // minus others
					}
					// while RT
					// Change by Santoso 22/10/08
					// Alignment of n number with the bar of the graph
					// was : rowTotal = row + 11;
					// int[] rowPos = prepareCells(xSpreadsheet, row,
					// totalRater.length);
					int[] rowPos = prepareCells(xSpreadsheet,row,totRaterType);
					row++; // start draw chart from here
					OO.setFontSize(12);

					// Change by Santoso 22/10/08
					// the total rater will be printed in drawChart therefore we
					// need
					// to pass the rating point and the position
					drawChart(Rating,Result,totalRater,rowPos,maxScale,
							splitOthers);

					// 12/12/2009 Denise
					// reinitialize Result and totalRater
					// change from 5 to totalType for customization (16 Dec 2009
					// Qiao Li)
					Result = new double[totalType + totalOtherRT];
					totalRater = new int[totalType + totalOtherRT];

					column = 9; // write the importance n gap
					int rtemp = row;

					Vector vImportance = Importance(compID,KBID);

					for (int j = 0; j < vImportance.size(); j++) {
						String[] arr = (String[]) vImportance.elementAt(j);
						String task = arr[1];
						double taskResult = Double.parseDouble(arr[2]);

						OO.insertString(xSpreadsheet,task + ": " + taskResult,
								rtemp,column);
						OO.mergeCells(xSpreadsheet,column,endColumn,rtemp,
								rtemp + 1);
						OO.setCellAllignment(xSpreadsheet,column,endColumn,
								rtemp,rtemp + 1,2,1);

						rtemp += 3;
					}

					double gap = 0;

					// Change by Santoso 22/10/08
					// only calculate Gap if survey include CPR/FPR Rating task
					if (hasCPRFPR) {
						gap = getAvgGap(compID);
						// If CPR is chosen in this survey
						{
							// Changed the default language to English by Chun
							// Yeong 9 Jun 2011
							// Commented away to allow translation below, Chun
							// Yeong 1 Aug 2011
							/*
							 * if (ST.LangVer == 2) //Indonesian
							 * OO.insertString(xSpreadsheet, "Selisih = " + gap,
							 * rtemp, column); else //if (ST.LangVer == 1)
							 * English OO.insertString(xSpreadsheet, "Gap = " +
							 * gap, rtemp, column);
							 */

							// Allow dynamic translation, Chun Yeong 1 Aug 2011
							OO.insertString(xSpreadsheet,
									trans.tslt(templateLanguage,"Gap") + " = "
											+ gap,rtemp,column);
							OO.mergeCells(xSpreadsheet,column,endColumn,rtemp,
									rtemp + 1);

							OO.setCellAllignment(xSpreadsheet,column,endColumn,
									rtemp,rtemp + 1,2,1);
						}
					}
					rtemp += 3;

					double LOA = LevelOfAgreement(compID,KBID);
					// Changed the default language to English by Chun Yeong 9
					// Jun 2011
					// Commented away to allow translation below, Chun Yeong 1
					// Aug 2011
					/*
					 * if (ST.LangVer == 2) //Indonesian
					 * OO.insertString(xSpreadsheet, "Tingkat Persetujuan: \n" +
					 * LOA + "%", rtemp, column); else //if (ST.LangVer == 1)
					 * English OO.insertString(xSpreadsheet,
					 * "Level Of Agreement: \n" + LOA + "%", rtemp, column);
					 */

					// Allow dynamic translation, Chun Yeong 1 Aug 2011
					OO.insertString(xSpreadsheet,
							trans.tslt(templateLanguage,"Level Of Agreement")
									+ ": \n" + LOA + "%",rtemp,column);
					OO.mergeCells(xSpreadsheet,column,endColumn,rtemp,rtemp + 2);
					OO.setCellAllignment(xSpreadsheet,column,endColumn,rtemp,
							rtemp + 2,2,1);

					column = 0;
					count++;

					// Removed by Alvis for Pagebreaks problem
					// if(count == 2) {
					// count = 0;
					// row += 17;
					// OO.insertPageBreak(xSpreadsheet, startColumn, endColumn,
					// row);
					// } else {
					// column = 0;
					// row += 16;
					// }
					// End of removal section by Alvis for Pagebreaks problem
					/*
					 * Change: standardize the number of rows for the charts to
					 * be 15 Reason: fit 2 charts in one page Updated by: Qiao
					 * Li Date: 23 Dec 2009
					 */
					row += 15;
					endRow = row - 1;

					/*
					 * Change(s) : Added codes to check height of the table to
					 * be added, and insert pagebreak if necessary Reason(s) :
					 * Fix the problem of a table being spilt into between two
					 * pages. Updated By: Alvis Updated On: 06 Aug 2009
					 */

					// Check height and insert pagebreak where necessary
					int pageHeightLimit = 22272;// Page limit is 22272
					int tableHeight = 0;

					// calculate the height of the table that is being dded.
					for (int i1 = startRow + 1; i1 <= endRow + 1; i1++) {
						int rowToCalculate = i1;
						tableHeight += OO.getRowHeight(xSpreadsheet,
								rowToCalculate,startColumn);

					}

					currentPageHeight = currentPageHeight + tableHeight; // add
																			// new
																			// table
																			// height
																			// to
																			// current
																			// pageheight.
					int dis = 2; // Denise 08/01/2009 to move the table two
									// lines down
					if (currentPageHeight > pageHeightLimit) {// adding the
																// table will
																// exceed a
																// single page
						OO.insertRows(xSpreadsheet,startColumn,endColumn,
								startRow,startRow + dis,dis,1);
						OO.insertPageBreak(xSpreadsheet,startColumn,endColumn,
								startRow);
						statementPos += dis;
						row += dis;
						startRow += dis;
						endRow += dis;
						currentPageHeight = tableHeight;
					}
					// Denise 08/01/2009 insert competency statement
					// Added translation for the competency name, Chun Yeong 1
					// Aug 2011
					OO.insertString(xSpreadsheet,UnicodeHelper
							.getUnicodeStringAmp(getTranslatedCompetency(
									statement).elementAt(0).toString()),
							statementPos,0);
					OO.setFontBold(xSpreadsheet,startColumn,endColumn,
							statementPos,statementPos);
					OO.setBGColor(xSpreadsheet,startColumn,endColumn,
							statementPos,statementPos,BGCOLOR);
					// comp name and definition
					OO.setTableBorder(xSpreadsheet,startColumn,endColumn,
							startRow,startRow + 1,false,false,true,true,true,
							true);

					// total sup n others

					OO.setTableBorder(xSpreadsheet,startColumn,startColumn,
							startRow + 2,endRow,false,false,true,true,true,true);

					// chart

					OO.setTableBorder(xSpreadsheet,startColumn + 1,8,
							startRow + 2,endRow,false,false,true,true,true,true);
					OO.setTableBorder(xSpreadsheet,9,endColumn,startRow + 2,
							endRow,false,false,true,true,true,true);
					OO.setCellAllignment(xSpreadsheet,startColumn,startColumn,
							startRow + 2,endRow,1,2);
					OO.setCellAllignment(xSpreadsheet,startColumn,startColumn,
							startRow + 2,endRow,1,2);

					// added by Alvis on 07-Aug-09 to ensure next section begin
					// on a new page.
					if (i == (vComp.size() - 1)) {// last table added
						// insertpagebreak
						OO.insertPageBreak(xSpreadsheet,startColumn,endColumn,
								endRow + 1);
					}

					// if (breakpage)
					// {
					// OO.insertRows(xSpreadsheet, startColumn, endColumn,
					// startRow, startRow+1, 1, 0);
					// row++;
					// }
				}// while Comp

				// End Competency Level

			} else {
				// Start KB level
				System.out
						.println("InsertCompetency() - Survey is at KB Level"); // To
																				// Remove,
																				// Dez
				int start = 0;
				int startRow = row; // for border
				int endRow = row;
				for (int i = 0; i < vComp.size(); i++) {

					// Add by Santoso (22/10/08) : reinitialize array per loop
					// reinitialize the array each loop (otherwise it will use
					// the previous value)
					totalRater = new int[totalRater.length];
					Result = new double[Result.length];

					// Reset only rating[4] since Rating[0]..[3] always have the
					// same value
					/*
					 * if (Rating.length > 4) { Rating[4] = ""; }
					 */

					// Change by Santoso (22/10/08)
					// No need to calculate rowTotal anymore since the raters
					// displayed on the graph are fixed
					// rowTotal = row + 1;
					start = 0;
					int RTID = 0;

					int KBID = 0;
					String KB = "";

					voCompetency voComp = (voCompetency) vComp.elementAt(i);

					int compID = voComp.getCompetencyID();

					String statement = voComp.getCompetencyName();
					String desc = voComp.getCompetencyDefinition();

					startRow = row;

					// Added translation for competency name, Chun Yeong 1 Aug
					// 2011
					OO.insertString(
							xSpreadsheet,
							getTranslatedCompetency(
									UnicodeHelper
											.getUnicodeStringAmp(statement))
									.elementAt(0).toString(),row,column);
					OO.setFontBold(xSpreadsheet,startColumn,endColumn,row,row);
					OO.setBGColor(xSpreadsheet,startColumn,endColumn,row,row,
							BGCOLOR);
					row++;

					r1 = row;
					OO.insertString(xSpreadsheet,
							UnicodeHelper.getUnicodeStringAmp(desc),row,column);
					OO.mergeCells(xSpreadsheet,startColumn,endColumn,row,row);
					// adjust the merged cell as top alignment (Qiao Li 21 Dec
					// 2009 )
					OO.setCellAllignment(xSpreadsheet,startColumn,endColumn,
							row,row,2,1);
					OO.setRowHeight(xSpreadsheet,row,1,
							ROWHEIGHT * OO.countTotalRow(desc,90));

					row++;
					start++;

					String RTCode = "";

					Vector RT = RatingTask();

					boolean hasCPRFPR = false;
					// Print out the "N" in other section for simplified report

					for (int j = 0; j < RT.size(); j++) {
						votblSurveyRating vo = (votblSurveyRating) RT
								.elementAt(j);

						RTID = vo.getRatingTaskID();
						RTCode = vo.getRatingCode();

						Vector result = null;

						if (RTCode.equals("CP")) {
							result = KBMean(RTID,compID);

							// Change by Santoso 2008/10/29
							// use a new query for retrieving total rater
							// added back the others for type customization(16
							// Dec 2009 Qiao Li)
							/*
							 * Change (s): add in surveyLevel to use different
							 * tables (tblResultCompetency and
							 * tblResultKeyBehavior) to retrieve number of
							 * raters Updated by: Qiao Li 21 Dec 2009
							 */
							totalOth = totalRater(RTID,compID,-1,"OTH%",
									surveyLevel); // Commented away to remove
													// CP(Others) to cater for
													// splitting Subordinates
													// and Peers, Desmond 22 Oct
													// 09
							totalSup = totalRater(RTID,compID,-1,"SUP%",
									surveyLevel);
							totalSelf = totalRater(RTID,compID,-1,"SELF",
									surveyLevel);

							// Added to cater to the splitting of peers and
							// subordinates, Desmond 21 Oct 09
							totalPeer = totalRater(RTID,compID,-1,"PEER%",
									surveyLevel);
							totalSub = totalRater(RTID,compID,-1,"SUB%",
									surveyLevel);

							totalAdd = totalRater(RTID,compID,-1,"ADD%",
									surveyLevel);

							// Re-locate and modified codes to include Peers and
							// Subordinates by Desmond 22 Oct 09
							totalAll = totalSup + totalPeer + totalSub
									+ totalOth + totalAdd;

							for (int k = 0; k < result.size(); k++) {

								String[] arr = (String[]) result.elementAt(k);

								// Updated adjustments to type in order to cater
								// for splitting of subordinates & peers,
								// Desmond 28 Oct 09
								int type = Integer.parseInt(arr[1]);
								// remove the hack, use back the type such that
								// results for others are
								// also retrieved (16 Dec 2009 Qiao Li)
								// if(type > 3) type = type -1; // Adjust values
								// > 3 because we removed Others from the list

								String t = "";
								switch (type) {
									case 1 :
										t = "All";

										arrN[iN] = totalAll;
										iN++;
										// Change by Santoso 22/10/08
										// the total rater will be printed later
										// below
										// set the valur of total all here
										totalRater[0] = totalAll;
										break;
									case 2 :
										t = "Superior"; // Change from
														// Supervisors
														// to Superior, Desmond
														// 22
														// Oct 09
										arrN[iN] = totalSup;
										iN++;
										// Change by Santoso 22/10/08
										// the total rater will be printed later
										// below
										// set the valur of total sup here
										totalRater[1] = totalSup;
										break;
									// added back the others for type
									// customization(16 Dec 2009 Qiao Li)
									case 3 :
										t = "Others";
										arrN[iN] = totalOth;
										iN++;
										// Change by Santoso 22/10/08
										// the total rater will be printed later
										// below
										// set the valur of total others here
										totalRater[2] = totalOth;
										break;
									case 4 :
										t = "Self";
										arrN[iN] = totalSelf;
										iN++;
										// Change by Santoso 22/10/08
										// the total rater will be printed later
										// below
										// set the valur of total self here
										totalRater[3] = totalSelf;
										break;
									// Commented away to remove CP(Others) to
									// cater
									// for splitting Subordinates and Peers,
									// Desmond
									// 22 Oct 09
									// Added case 5 and 6 to cater to splitting
									// of
									// Peers and Subordinates by Desmond 21 Oct
									// 09
									case 5 :
										t = "Subordinates";
										arrN[iN] = totalSub;
										iN++;
										totalRater[4] = totalSub;
										break;
									case 6 :
										t = "Peers";
										arrN[iN] = totalPeer;
										iN++;
										totalRater[5] = totalPeer;
										break;
									case 7 :
										t = "Additional";
										arrN[iN] = totalAdd;
										iN++;
										totalRater[6] = totalAdd;
										break;
								}
								// Should not insert a "\n". Will push col into
								// 2 rows. Printing will go haywire (Maruli)
								// Rating[r] = RTCode + "\n(" + t + ")";
								// Change by Santoso 22/10/08 : Rating order and
								// value already initialized above (also the
								// Result)
								// we need to set the RTCode and the result at
								// the appropriate position as already defined
								// above
								Rating[type - 1] = RTCode + "(" + t + ")";
								if (iReportType == 1)
									Rating[type - 1] = Rating[type - 1]
											.replaceAll("\n"," ");

								System.out.print("InsertCompetency() - Rating"
										+ "[" + (type - 1) + "]" + " = "
										+ Rating[type - 1]); // To Remove,
																// Desmond

								if (type == 1)
									Result[type - 1] = CompTrimmedMeanforAll(
											RTID,compID);
								else
									Result[type - 1] = Double
											.parseDouble(arr[2]);

							}
						} else if (RTCode.equals("CPR") || RTCode.equals("FPR")) {
							// Change by Santoso 22/10/08
							// need to keep track whether CPR/FPR is included in
							// the survey or not to keep Gap from printed if no
							// CPR/FPR
							hasCPRFPR = true;
							// Change by Santoso 2008/10/29
							// use a new query for retrieving total rater
							// added back the others for type customization(16
							// Dec 2009 Qiao Li)
							/*
							 * Change (s): add in surveyLevel to use different
							 * tables (tblResultCompetency and
							 * tblResultKeyBehavior) to retrieve number of
							 * raters Updated by: Qiao Li 21 Dec 2009
							 */
							totalOth = totalRater(RTID,compID,-1,"OTH%",
									surveyLevel); // Commented away to remove
													// CP(Others) to cater for
													// splitting Subordinates
													// and Peers, Desmond 22 Oct
													// 09
							totalSup = totalRater(RTID,compID,-1,"SUP%",
									surveyLevel);

							// Added to cater to the splitting of peers and
							// subordinates, Desmond 21 Oct 09
							totalPeer = totalRater(RTID,compID,-1,"PEER%",
									surveyLevel);
							totalSub = totalRater(RTID,compID,-1,"SUB%",
									surveyLevel);

							totalAdd = totalRater(RTID,compID,-1,"ADD%",
									surveyLevel);

							// Re-locate and modified codes to include Peers and
							// Subordinates by Desmond 22 Oct 09
							totalAll = totalSup + totalPeer + totalSub
									+ totalOth + totalAdd;

							arrN[iN] = totalAll;
							iN++;
							// Should not insert a "\n". Will push col into 2
							// rows. Printing will go haywire (Maruli)
							// Rating[r] = RTCode + "\n(All)";
							// Change by Santoso 22/10/08 : Rating order and
							// value already initialized above (also the Result)
							// we need to set the RTCode and the result at the
							// appropriate position as already defined above
							// change from 5 to totalType for customization (16
							// Dec 2009 Qiao Li)
							Rating[totalType] = RTCode + "(All)";
							if (iReportType == 1) {
								System.out
										.println("insertCompetency() - iReportType = 1 so replace all \n"); // To
																											// Remove,
																											// Desmond
								Rating[totalType] = Rating[totalType]
										.replaceAll("\n"," "); // Changed from
																// Rating[4] to
																// Rating[5] to
																// cater for new
																// catergories
																// Subordinates
																// & Peers,
																// Desmond 22
																// Oct 09
							}

							totalRater[totalType] = totalAll; // Changed from
																// Rating[4] to
																// Rating[6] to
																// cater for new
																// catergories
																// Subordinates
																// & Peers,
																// Desmond 22
																// Oct 09
							Result[totalType] = CompTrimmedMeanforAll(RTID,
									compID); // Changed from Rating[4] to
												// Rating[5] to cater for new
												// catergories Subordinates &
												// Peers, Desmond 22 Oct 09
							System.out
									.println("insertCompetency() - CPR Score - Result"
											+ "[5]" + " = " + Result[5]); // To
																			// Remove,
																			// Desmond

							// 11/12/2009 Denise
							// Change position to display CPR/FPR on the top
							double CPRFPR_Result = Result[totalType];
							int CPRFPR_TotalRating = totalRater[totalType];
							// change from x=5 to totalType for customization
							// (16 Dec 2009 Qiao Li)
							for (int x = totalType; x > 0; x--) {
								totalRater[x] = totalRater[x - 1];
								Result[x] = Result[x - 1];
							}

							Result[0] = CPRFPR_Result;
							totalRater[0] = CPRFPR_TotalRating;

							Rating[0] = Rating[totalType];
							Rating[1] = "CP(All)";
							Rating[2] = "CP(Superior)";
							// added back the others for type customization(16
							// Dec 2009 Qiao Li)
							Rating[3] = "CP(Others)";
							Rating[4] = "CP(Self)";
							Rating[5] = "CP(Subordinates)";
							Rating[6] = "CP(Peers)";
							Rating[7] = "CP(Additional)";
						}

					}// while RT
						// rater type can change depends on whether "Others" is
						// splitted
						// get the appropriate number and prepareCells(Qiao Li
						// 17 Dec 2009)
					int totRaterType = Rating.length;
					if (splitOthers == 0) {
						totRaterType -= 3;
					} else {
						totRaterType -= 1;
					}
					// Change by Santoso : Alignment of n number with the bar of
					// the graph
					// rowTotal is no longer needed, the raters displayed are
					// fixed
					// was : rowTotal = row + 11;
					// int[] rowPos = prepareCells(xSpreadsheet, row,
					// totalRater.length);
					int[] rowPos = prepareCells(xSpreadsheet,row,totRaterType);
					row++;
					// Change by Santoso (2008-10-08)
					// total rater is printed inside drawChart, we need to pass
					// the total rater list and the position to draw
					drawChart(Rating,Result,totalRater,rowPos,maxScale,
							splitOthers);

					// 12/12/2009 Denise
					// reinitialize Result and totalRater
					// change from 5 to totalType for customization (16 Dec 2009
					// Qiao Li)
					Result = new double[totalType + totalOtherRT]; // Changed
																	// from 4 +
																	// totalOtherRT
																	// to 5 +
																	// totalOtherRT,
																	// to clean
																	// up later
																	// on, DeZ
					totalRater = new int[totalType + totalOtherRT];

					column = 9;
					r1 = row;

					Vector Importance = AvgImportance(compID);

					for (int j = 0; j < Importance.size(); j++) {
						String[] arr = (String[]) Importance.elementAt(j);

						String task = arr[1];
						double taskResult = Double.parseDouble(arr[2]);

						OO.insertString(xSpreadsheet,task + ": " + taskResult,
								r1,column);
						OO.mergeCells(xSpreadsheet,column,endColumn,r1,r1 + 1);
						r1 += 3;
					}

					double gap = 0;
					// Change by Santoso 22/10/08
					// only calculate Gap if survey include CPR/FPR Rating task
					if (hasCPRFPR) {
						int element = vCompID.indexOf(new Integer(compID));
						gap = Double
								.valueOf(
										((String[]) vGapUnsorted
												.elementAt(element))[1])
								.doubleValue();
						// System.out.println(gap + "----" + compID + " --- " +
						// element);
						if (iNoCPR == 0) // If CPR is chosen in this survey
						{
							// Changed the default language to English by Chun
							// Yeong 9 Jun 2011
							// Commented away to allow translation below, Chun
							// Yeong 1 Aug 2011
							/*
							 * if (ST.LangVer == 2) //Indonesian
							 * OO.insertString(xSpreadsheet, "Selisih = " + gap,
							 * r1, column); else // if (ST.LangVer == 1) English
							 * OO.insertString(xSpreadsheet, "Gap = " + gap, r1,
							 * column);
							 */

							// Allow dynamic translation, Chun Yeong 1 Aug 2011
							OO.insertString(xSpreadsheet,
									trans.tslt(templateLanguage,"Gap") + " = "
											+ gap,r1,column);
							OO.mergeCells(xSpreadsheet,column,endColumn,r1,
									r1 + 1);
						}
					}
					r1 += 3;

					double LOA = AvgLevelOfAgreement(compID,totalAll);
					// System.out.println(LOA + "----" + compID );

					// Changed the default language to English by Chun Yeong 9
					// Jun 2011
					// Commented away to allow translation below, Chun Yeong 1
					// Aug 2011
					/*
					 * if (ST.LangVer == 2) //Indonesian
					 * OO.insertString(xSpreadsheet, "Tingkat Persetujuan: \n" +
					 * LOA + "%", r1, column); else //if (ST.LangVer == 1)
					 * //English OO.insertString(xSpreadsheet,
					 * "Level Of Agreement: \n" + LOA + "%", r1, column);
					 */

					// Allow dynamic translation, Chun Yeong 1 Aug 2011
					OO.insertString(xSpreadsheet,
							trans.tslt(templateLanguage,"Level Of Agreement")
									+ ": \n" + LOA + "%",r1,column);
					OO.mergeCells(xSpreadsheet,column,endColumn,r1,r1 + 2);
					r1 += 4;

					count++;
					column = 0;
					if (count == 2) {
						count = 0;

						row += 15;
						OO.insertPageBreak(xSpreadsheet,startColumn,endColumn,
								row);
					} else {
						row += 15;
					}

					endRow = row - 1;

					// comp name and definition
					OO.setTableBorder(xSpreadsheet,startColumn,endColumn,
							startRow,startRow + 1,false,false,true,true,true,
							true);

					// total sup n others
					OO.setTableBorder(xSpreadsheet,startColumn,startColumn,
							startRow + 2,endRow,false,false,true,true,true,true);
					// chart
					OO.setTableBorder(xSpreadsheet,startColumn + 1,8,
							startRow + 2,endRow,false,false,true,true,true,true);
					OO.setTableBorder(xSpreadsheet,9,endColumn,startRow + 2,
							endRow,false,false,true,true,true,true);

					OO.setCellAllignment(xSpreadsheet,startColumn,startColumn,
							startRow + 2,endRow,1,2);

					// KB LEVEL //

					// (30-Sep-05) Rianto: Replaced with Simplified report with
					// no competencies & KB charts
					// if(reportType == 2) { // only if standard report,
					// simplified report no need for KB

					Vector KBList = KBList(compID);

					for (int j = 0; j < KBList.size(); j++) {
						voKeyBehaviour voKB = (voKeyBehaviour) KBList
								.elementAt(j);
						KBID = voKB.getKeyBehaviourID();
						KB = voKB.getKeyBehaviour();

						startRow = row;
						r1 = row;

						// Added translation for the key behaviour, Chun Yeong 1
						// Aug 2011
						OO.insertString(
								xSpreadsheet,
								start
										+ ". "
										+ getTranslatedKeyBehavior(UnicodeHelper
												.getUnicodeStringAmp(KB)),row,0);
						OO.mergeCells(xSpreadsheet,startColumn,endColumn,row,
								row);
						// adjust the merged cell as top alignment (Qiao Li 21
						// Dec 2009 )
						OO.setCellAllignment(xSpreadsheet,startColumn,
								endColumn,row,row,2,1);
						OO.setRowHeight(xSpreadsheet,row,0,
								ROWHEIGHT * OO.countTotalRow(KB,90));

						row += 2;
						// row ++;
						start++;

						// change from 5 to totalType for customization (16 Dec
						// 2009 Qiao Li)
						totalRater = new int[totalType + totalOtherRT]; // Changed
																		// from
																		// 4 +
																		// totalOtherRT
																		// to 5
																		// +
																		// totalOtherRT,
																		// to
																		// clean
																		// up
																		// later
																		// on,
																		// DeZ
						Result = new double[totalType + totalOtherRT]; // Changed
																		// from
																		// 4 +
																		// totalOtherRT
																		// to 5
																		// +
																		// totalOtherRT,
																		// to
																		// clean
																		// up
																		// later
																		// on,
																		// DeZ

						RT = RatingTask();
						// Change by Santoso 22/10/08
						// only calculate Gap if survey include CPR/FPR Rating
						// task
						// initialize cpr/fpr flag
						hasCPRFPR = false;
						for (int k = 0; k < RT.size(); k++) {
							votblSurveyRating vo = (votblSurveyRating) RT
									.elementAt(k);
							RTID = vo.getRatingTaskID();
							RTCode = vo.getRatingCode();

							Vector result = MeanResult(RTID,compID,KBID);

							if (RTCode.equals("CP")) {
								// Comment off by Ha 02/07/08 should not re-set
								// the r to 0
								// It will print out the KB CP, CPR, FPR
								// incorrecly
								// r = 0;

								// by Hemilda 23/09/2008 to get total
								// oth,sup,self,all for each kb
								// added back the others for type
								// customization(16 Dec 2009 Qiao Li)
								totalOth = totalOth(RTID,compID,KBID); // Commented
																		// away
																		// to
																		// remove
																		// CP(Others)
																		// to
																		// cater
																		// for
																		// splitting
																		// Subordinates
																		// and
																		// Peers,
																		// Desmond
																		// 22
																		// Oct
																		// 09
								totalSup = totalSup(RTID,compID,KBID);
								totalSelf = totalSelf(RTID,compID,KBID);

								// Added to cater to the splitting of peers and
								// subordinates, Desmond 21 Oct 09
								/*
								 * Change (s): add in surveyLevel to use
								 * different tables (tblResultCompetency and
								 * tblResultKeyBehavior) to retrieve number of
								 * raters Updated by: Qiao Li 21 Dec 2009
								 */
								totalPeer = totalRater(RTID,compID,KBID,
										"PEER%",surveyLevel);
								totalSub = totalRater(RTID,compID,KBID,"SUB%",
										surveyLevel);

								totalAdd = totalRater(RTID,compID,KBID,"ADD%",
										surveyLevel);

								// Re-locate and modified codes to include Peers
								// and Subordinates by Desmond 22 Oct 09
								totalAll = totalSup + totalPeer + totalSub
										+ totalOth + totalAdd;

								for (int l = 0; l < result.size(); l++) {
									String[] arr = (String[]) result
											.elementAt(l);

									// Updated adjustments to type in order to
									// cater for splitting of subordinates &
									// peers, Desmond 28 Oct 09
									int type = Integer.parseInt(arr[1]);
									// remove the hack, use back the type such
									// that results for others are also
									// retrieved (16 Dec 2009 Qiao Li)
									// if(type > 3) type = type -1; // Adjust
									// values > 3 because we removed Others from
									// the list

									String t = "";

									switch (type) {
										case 1 :
											t = "All";
											arrN[iN] = totalAll;
											iN++;
											// Change by Santoso 22/10/08
											// the total rater will be printed
											// later
											// below
											// set the valur of total all here
											totalRater[0] = totalAll;
											break;
										case 2 :
											t = "Superior"; // Change from
															// Supervisors to
															// Superior, Desmond
															// 22
															// Oct 09
											arrN[iN] = totalSup;
											iN++;
											// Change by Santoso 22/10/08
											// the total rater will be printed
											// later
											// below
											// set the valur of total sup here
											totalRater[1] = totalSup;
											break;
										// Commented away to remove CP(Others)
										// to
										// cater for splitting Subordinates and
										// Peers, Desmond 22 Oct 09
										// added back the others for type
										// customization(16 Dec 2009 Qiao Li)
										case 3 :
											t = "Others";
											arrN[iN] = totalOth;
											iN++;
											// Change by Santoso 22/10/08
											// the total rater will be printed
											// later
											// below
											// set the valur of total others
											// here
											totalRater[2] = totalOth;
											break;
										case 4 :
											t = "Self";
											arrN[iN] = totalSelf;
											iN++;
											// Change by Santoso 22/10/08
											// the total rater will be printed
											// later
											// below
											// set the valur of total self here
											totalRater[3] = totalSelf;
											break;

										// Added case 5 and 6 to cater to
										// splitting
										// of Peers and Subordinates by Desmond
										// 21
										// Oct 09
										case 5 :
											t = "Subordinates";
											arrN[iN] = totalSub;
											iN++;
											totalRater[4] = totalSub;
											break;
										case 6 :
											t = "Peers";
											arrN[iN] = totalPeer;
											iN++;
											totalRater[5] = totalPeer;
											break;

										case 7 :
											t = "Additional";
											arrN[iN] = totalAdd;
											iN++;
											totalRater[6] = totalAdd;
											break;
									}

									// Should not insert a "\n". Will push col
									// into 2 rows. Printing will go haywire
									// (Maruli)
									// Rating[r] = RTCode + "\n(" + t + ")";

									// Change by Santoso 22/10/08 : Rating order
									// and value already initialized above (also
									// the Result)
									// we need to set the RTCode and the result
									// at the appropriate position as already
									// defined above
									Rating[type - 1] = RTCode + "(" + t + ")";

									if (iReportType == 1)
										Rating[type - 1] = Rating[type - 1]
												.replaceAll("\n"," ");

									Result[type - 1] = Double
											.parseDouble(arr[2]);;

								}
							} else if (RTCode.equals("CPR")
									|| RTCode.equals("FPR")) {
								// Comment off by Ha 02/07/08 should not re-set
								// the r to 0
								// It will print out the KB CP, CPR, FPR
								// incorrecly

								// r = 0;
								// Change by Santoso : 2008-10-19
								// Do we need to check k< result.size?
								// k refers to index of RatingTask and result
								// refers to the MeanResult
								if (result.size() != 0) { // && k<result.size())
															// {
									// Change by Santoso : 2008-10-19
									// Wrong value seems to be retrieved (sup's
									// value --> result[2])
									// Retrieve the correct value for All in
									// result[0]
									// String [] arr =
									// (String[])result.elementAt(k);
									String[] arr = (String[]) result
											.elementAt(0);
									hasCPRFPR = true;
									// Should not insert a "\n". Will push col
									// into 2 rows. Printing will go haywire
									// (Maruli)
									// Rating[r] = RTCode + "\n(All)";
									// Change by Santoso 22/10/08 : Rating order
									// and value already initialized above (also
									// the Result)
									// we need to set the RTCode and the result
									// at the appropriate position as already
									// defined above
									// change from 5 to totalType for
									// customization (16 Dec 2009 Qiao Li)
									Rating[totalType] = RTCode + "(All)"; // Changed
																			// from
																			// Rating[4]
																			// to
																			// Rating[5]
																			// to
																			// cater
																			// for
																			// new
																			// catergories
																			// Subordinates
																			// &
																			// Peers,
																			// Desmond
																			// 22
																			// Oct
																			// 09
									if (iReportType == 1)
										Rating[totalType] = Rating[totalType]
												.replaceAll("\n"," "); // Changed
																		// from
																		// Rating[4]
																		// to
																		// Rating[5]
																		// to
																		// cater
																		// for
																		// new
																		// catergories
																		// Subordinates
																		// &
																		// Peers,
																		// Desmond
																		// 22
																		// Oct
																		// 09

									// Change by Santoso (2008-10-19)
									// Fix CPR(All), we need to find it again
									// (instead of using the current totalAll
									// value)
									// added back the others for type
									// customization(16 Dec 2009 Qiao Li)
									totalOth = totalOth(RTID,compID,KBID); // Commented
																			// away
																			// to
																			// remove
																			// CP(Others)
																			// to
																			// cater
																			// for
																			// splitting
																			// Subordinates
																			// and
																			// Peers,
																			// Desmond
																			// 22
																			// Oct
																			// 09
									totalSup = totalSup(RTID,compID,KBID);

									// Added to cater to the splitting of peers
									// and subordinates, Desmond 21 Oct 09
									/*
									 * Change (s): add in surveyLevel to use
									 * different tables (tblResultCompetency and
									 * tblResultKeyBehavior) to retrieve number
									 * of raters Updated by: Qiao Li 21 Dec 2009
									 */
									totalPeer = totalRater(RTID,compID,KBID,
											"PEER%",surveyLevel);
									totalSub = totalRater(RTID,compID,KBID,
											"SUB%",surveyLevel);

									totalAdd = totalRater(RTID,compID,KBID,
											"ADD%",surveyLevel);

									// Re-locate and modified codes to include
									// Peers and Subordinates by Desmond 22 Oct
									// 09
									totalAll = totalSup + totalPeer + totalSub
											+ totalOth + totalAdd;

									// Changed from Rating[4] to Rating[5] to
									// cater for new catergories Subordinates &
									// Peers, Desmond 22 Oct 09
									// change from 5 to totalType for
									// customization (16 Dec 2009 Qiao Li)
									totalRater[totalType] = totalAll;
									Result[totalType] = Double
											.parseDouble(arr[2]);

									// 11/12/2009 Denise
									// Change postion to display CPR/FPR on the
									// top
									// change from 5 to totalType for
									// customization (16 Dec 2009 Qiao Li)
									double CPRFPR_Result = Result[totalType];
									int CPRFPR_TotalRating = totalRater[totalType];

									for (int x = totalType; x > 0; x--) {
										totalRater[x] = totalRater[x - 1];
										Result[x] = Result[x - 1];
									}

									Result[0] = CPRFPR_Result;
									totalRater[0] = CPRFPR_TotalRating;

									Rating[0] = Rating[totalType];
									Rating[1] = "CP(All)";
									Rating[2] = "CP(Superior)";
									// added back the others for type
									// customization(16 Dec 2009 Qiao Li)
									Rating[3] = "CP(Others)";
									Rating[4] = "CP(Self)";
									Rating[5] = "CP(Subordinates)";
									Rating[6] = "CP(Peers)";
									Rating[7] = "CP(Additional)";
								}
							}

						}// while RT
							// rater type can change depends on whether "Others"
							// is splitted
							// get the appropriate number and prepareCells(Qiao
							// Li 17 Dec 2009)
						totRaterType = Rating.length;
						if (splitOthers == 0) {
							totRaterType -= 3;
						} else {
							totRaterType -= 1;
						}
						// Change by Santoso : Alignment of n number with the
						// bar of the graph
						// the raters displayed are fixed, no need to calculate
						// rowTotal anymore
						// was : rowTotal = row + 11;

						// rowPos = prepareCells(xSpreadsheet, row,
						// totalRater.length);
						rowPos = prepareCells(xSpreadsheet,row,totRaterType);
						row++;
						// Change by Santoso (2008-10-08)
						// total rater is printed inside drawChart, we need to
						// pass
						// the total rater list and the position to draw
						drawChart(Rating,Result,totalRater,rowPos,maxScale,
								splitOthers);

						// 12/12/2009 Denise
						// reinitialize Result and totalRater
						// change from 5 to totalType for customization (16 Dec
						// 2009 Qiao Li)
						Result = new double[totalType + totalOtherRT]; // Changed
																		// from
																		// 4 +
																		// totalOtherRT
																		// to 5
																		// +
																		// totalOtherRT,
																		// to
																		// clean
																		// up
																		// later
																		// on,
																		// DeZ
						totalRater = new int[totalType + totalOtherRT];

						column = 9;
						r1 = row;

						Vector vImportance = Importance(compID,KBID);

						for (int k = 0; k < vImportance.size(); k++) {
							String[] arr = (String[]) vImportance.elementAt(k);
							String task = arr[1];
							double taskResult = Double.parseDouble(arr[2]);

							arrN[iN] = totalAll;
							iN++;
							OO.insertString(xSpreadsheet,task + ": "
									+ taskResult,r1,column);
							OO.mergeCells(xSpreadsheet,column,endColumn,r1,
									r1 + 1);
							r1 += 3;
						}

						// Change by Santoso 22/10/08
						// only calculate Gap if survey include CPR/FPR Rating
						// task
						if (hasCPRFPR) {
							gap = Gap(compID,KBID);
							gap = Math.round(gap * 100.0) / 100.0;

							if (iNoCPR == 0) // If CPR is chosen in this survey
							{
								// Changed the default language to English by
								// Chun Yeong 9 Jun 2011
								// Commented away to allow translation below,
								// Chun Yeong 1 Aug 2011
								/*
								 * if (ST.LangVer == 2) //Indonesian
								 * OO.insertString(xSpreadsheet, "Selisih = " +
								 * gap, r1, column); else //if (ST.LangVer == 1)
								 * English OO.insertString(xSpreadsheet,
								 * "Gap = " + gap, r1, column);
								 */

								// Allow dynamic translation, Chun Yeong 1 Aug
								// 2011
								OO.insertString(xSpreadsheet,
										trans.tslt(templateLanguage,"Gap")
												+ " = " + gap,r1,column);
								OO.mergeCells(xSpreadsheet,column,endColumn,r1,
										r1 + 1);
							}
						}
						r1 += 3;

						LOA = LevelOfAgreement(compID,KBID);
						// Changed the default language to English by Chun Yeong
						// 9 Jun 2011
						// Commented away to allow translation below, Chun Yeong
						// 1 Aug 2011
						/*
						 * if (ST.LangVer == 2) //Indonesian
						 * OO.insertString(xSpreadsheet,
						 * "Tingkat Persetujuan: \n" + LOA + "%", r1, column);
						 * else //if (ST.LangVer == 1) English
						 * OO.insertString(xSpreadsheet,
						 * "Level Of Agreement: \n" + LOA + "%", r1, column);
						 */

						// Allow dynamic translation, Chun Yeong 1 Aug 2011
						OO.insertString(
								xSpreadsheet,
								trans.tslt(templateLanguage,
										"Level Of Agreement")
										+ ": \n"
										+ LOA
										+ "%",r1,column);
						OO.mergeCells(xSpreadsheet,column,endColumn,r1,r1 + 2);
						r1 += 4;

						count++;
						column = 0;
						// added a page break at the end of the chart section of
						// KB Level Survey
						// to solve part of pagination problem (Qiao Li 23 Dec
						// 2009)
						if (count == 2
								|| (j == KBList.size() - 1 && i == vComp.size() - 1)) {
							count = 0;
							/*
							 * Change: standardize the number of rows for the
							 * charts to be 15 Reason: fit 2 charts in one page
							 * Updated by: Qiao Li Date: 23 Dec 2009
							 */
							row += 15;
							OO.insertPageBreak(xSpreadsheet,startColumn,
									endColumn,row);

						} else
							row += 15;

						endRow = row - 1;

						// comp name and definition
						OO.setTableBorder(xSpreadsheet,startColumn,endColumn,
								startRow,startRow + 1,false,false,true,true,
								true,true);
						// total sup n others
						OO.setTableBorder(xSpreadsheet,startColumn,startColumn,
								startRow + 2,endRow,false,false,true,true,true,
								true);
						// chart
						OO.setTableBorder(xSpreadsheet,startColumn + 1,8,
								startRow + 2,endRow,false,false,true,true,true,
								true);
						OO.setTableBorder(xSpreadsheet,9,endColumn,
								startRow + 2,endRow,false,false,true,true,true,
								true);

						OO.setCellAllignment(xSpreadsheet,startColumn,
								startColumn,startRow + 2,endRow,1,2);

						// /Denise 07/01/2009
						if (count == 0) { // move table two lines down
							OO.insertRows(xSpreadsheet,0,10,row + 1,row + 3,2,1);
							row += 2;
						}
					}// while KBList
						// } // end if of standard version
				}// while Comp
			}// end of if KB Level survey
		} else if (breakCPR == 1) {// breaking down CPR(All)
			int totalOtherRT = totalOtherRT(); // total other rating task beside
												// CP
			int total = totalGroup() + totalOtherRT + 1; // 1 for all
			int totalType = 7; // 6:All,Superior,Others,Self,Subordinates,Peers,Additional
								// Raters

			// these three variables below are related one to another
			String[] Rating = new String[totalType * 2]; // store the rating
															// name
			double[] Result = new double[totalType * 2]; // store the rating
															// result
			int[] totalRater = new int[totalType * 2];// store total no of
														// raters

			// initialize rating for CP and CPR
			Rating[0] = "CPR(All)";
			Rating[1] = "CP(All)";
			Rating[2] = "CPR(Superior)";
			Rating[3] = "CP(Superior)";
			Rating[4] = "CPR(Others)";
			Rating[5] = "CP(Others)";
			Rating[6] = "CPR(Self)";
			Rating[7] = "CP(Self)";
			Rating[8] = "CPR(Subordinates)";
			Rating[9] = "CP(Subordinates)";
			Rating[10] = "CPR(Peers)";
			Rating[11] = "CP(Peers)";
			Rating[12] = "CPR(Additional)";
			Rating[13] = "CP(Additional)";

			int maxScale = MaxScale();

			int count = 0; // to count total chart for each page, max = 2;
			int r1 = 1;
			int add = 13 / total;

			int totalOth = 0;
			int totalSup = 0;
			int totalSelf = 0;
			int totalAll = 0;
			int totalPeer = 0;
			int totalSub = 0;
			int totalAdd = 0;

			Vector vComp = CompetencyByName();

			OO.insertPageBreak(xSpreadsheet,startColumn,endColumn,row);

			// Allow dynamic translation, Chun Yeong 1 Aug 2011
			OO.insertString(xSpreadsheet,
					trans.tslt(templateLanguage,"Competency Report"),row,0);
			OO.setFontBold(xSpreadsheet,startColumn,endColumn,row,row);

			row += 2;

			if (surveyLevel == 0) { // Competency Level Survey
				System.out
						.println("InsertCompetency() - Survey is at Competency Level"); // To
																						// Remove,
																						// Dez

				int endRow = row;
				// Added by Alvis 06-Aug-09 for pagebreak fix implemented below
				int currentPageHeight = 1076;// starting page height is set to
												// 1076 to include the title of
												// the competency report.

				for (int i = 0; i < vComp.size(); i++) {

					voCompetency voComp = (voCompetency) vComp.elementAt(i);

					int compID = voComp.getCompetencyID();
					String statement = voComp.getCompetencyName();
					String desc = voComp.getCompetencyDefinition();

					int startRow = row; // for border
					int RTID = 0;
					int KBID = 0;

					int statementPos = row;
					row++;

					r1 = row;
					// Added tranlsation for the competency definition, Chun
					// Yeong 1 Aug 2011
					OO.insertString(xSpreadsheet,
							getTranslatedCompetency(statement).elementAt(1)
									.toString(),row,0);
					OO.mergeCells(xSpreadsheet,startColumn,endColumn,row,row);
					// adjust the merged cell as top alignment (Qiao Li 21 Dec
					// 2009 )
					OO.setCellAllignment(xSpreadsheet,startColumn,endColumn,
							row,row,2,1);
					OO.setRowHeight(xSpreadsheet,row,1,
							ROWHEIGHT * OO.countTotalRow(desc,90));
					row++;

					String RTCode = "";

					Vector RT = RatingTask();
					int r = 0;

					boolean hasCPRFPR = true;
					for (int j = 0; j < RT.size(); j++) {
						votblSurveyRating vo = (votblSurveyRating) RT
								.elementAt(j);

						RTID = vo.getRatingTaskID();
						RTCode = vo.getRatingCode();

						Vector result = MeanResult(RTID,compID,KBID);

						if (RTCode.equals("CP")) {
							totalOth = totalOth(1,compID);
							totalSup = totalSup(1,compID);
							totalSelf = totalSelf(1,compID);
							totalPeer = totalRater(RTID,compID,-1,"PEER%",
									surveyLevel);
							totalSub = totalRater(RTID,compID,-1,"SUB%",
									surveyLevel);
							totalAdd = totalRater(RTID,compID,-1,"ADD%",
									surveyLevel);
							totalAll = totalSup + totalPeer + totalSub
									+ totalOth + totalAdd;

							for (int k = 0; k < result.size(); k++) {
								String[] arrOther = (String[]) result
										.elementAt(k);

								int type = Integer.parseInt(arrOther[1]);
								String t = "";
								switch (type) {
									case 1 :
										t = "All";
										totalRater[1] = totalAll;
										break;

									case 2 :
										t = "Superior";
										totalRater[3] = totalSup;
										break;

									case 3 :
										t = "Others";
										totalRater[5] = totalOth;
										break;

									case 4 :
										t = "Self";
										totalRater[7] = totalSelf;
										break;

									case 5 :
										t = "Subordinates";
										totalRater[9] = totalSub;
										break;

									case 6 :
										t = "Peers";
										totalRater[11] = totalPeer;
										break;

									case 7 :
										t = "Additional";
										totalRater[13] = totalAdd;
										break;
								}

								Rating[type * 2 - 1] = RTCode + "(" + t + ")";
								if (iReportType == 1)
									Rating[type * 2 - 1] = Rating[r]
											.replaceAll("\n"," ");

								Result[type * 2 - 1] = Double
										.parseDouble(arrOther[2]);

							}// end of for result.size
						}// end of RT.code equals CP
						else if (RTCode.equals("CPR") || RTCode.equals("FPR")) {
							totalOth = totalRater(2,compID,-1,"OTH%",
									surveyLevel);
							totalSup = totalRater(2,compID,-1,"SUP%",
									surveyLevel);
							totalPeer = totalRater(2,compID,-1,"PEER%",
									surveyLevel);
							totalSub = totalRater(2,compID,-1,"SUB%",
									surveyLevel);
							totalAdd = totalRater(2,compID,-1,"ADD%",
									surveyLevel);
							totalAll = totalSup + totalPeer + totalSub
									+ totalOth + totalAdd;

							for (int l = 0; l < result.size(); l++) {
								String[] arrOther = (String[]) result
										.elementAt(l);

								int type = Integer.parseInt(arrOther[1]);
								String t = "";
								switch (type) {
									case 1 :
										t = "All";
										totalRater[0] = totalAll;
										Rating[0] = RTCode + "(" + t + ")";
										if (iReportType == 1)
											Rating[0] = Rating[0].replaceAll(
													"\n"," ");
										Result[0] = CompTrimmedMeanforAll(RTID,
												compID);
										break;

									case 2 :
										t = "Superior";
										totalRater[2] = totalSup;
										Rating[2] = RTCode + "(" + t + ")";
										if (iReportType == 1)
											Rating[2] = Rating[2].replaceAll(
													"\n"," ");
										Result[2] = Double
												.parseDouble(arrOther[2]);
										break;

									case 3 :
										t = "Others";
										totalRater[4] = totalOth;
										Rating[4] = RTCode + "(" + t + ")";
										if (iReportType == 1)
											Rating[4] = Rating[4].replaceAll(
													"\n"," ");
										Result[4] = Double
												.parseDouble(arrOther[2]);
										break;

									case 4 :
										t = "Self";
										totalRater[6] = totalSelf;
										Rating[6] = RTCode + "(" + t + ")";
										if (iReportType == 1)
											Rating[6] = Rating[6].replaceAll(
													"\n"," ");
										Result[6] = Double
												.parseDouble(arrOther[2]);
										break;

									case 5 :
										t = "Subordinates";
										totalRater[8] = totalSub;
										Rating[8] = RTCode + "(" + t + ")";
										if (iReportType == 1)
											Rating[8] = Rating[8].replaceAll(
													"\n"," ");
										Result[8] = Double
												.parseDouble(arrOther[2]);
										break;

									case 6 :
										t = "Peers";
										totalRater[10] = totalPeer;
										Rating[10] = RTCode + "(" + t + ")";
										if (iReportType == 1)
											Rating[10] = Rating[10].replaceAll(
													"\n"," ");
										Result[10] = Double
												.parseDouble(arrOther[2]);
										break;

									case 7 :
										t = "Additional";
										totalRater[12] = totalAdd;
										Rating[12] = RTCode + "(" + t + ")";
										if (iReportType == 1)
											Rating[12] = Rating[12].replaceAll(
													"\n"," ");
										Result[12] = Double
												.parseDouble(arrOther[2]);
										break;
								}
							}// end of result.size for loop
						}
					}// while RT

					// rater type can change depends on whether "Others" is
					// splitted
					int totRaterType = Rating.length;
					if (splitOthers == 0) {
						totRaterType -= 6;
					} else {
						totRaterType -= 2;
					}

					int[] rowPos = prepareCellsBreakCPR(xSpreadsheet,row,
							totRaterType);
					row++; // start draw chart from here
					OO.setFontSize(12);

					// the total rater will be printed in drawChart therefore we
					// need
					// to pass the rating point and the position
					drawChartBreakCPR(Rating,Result,totalRater,rowPos,maxScale,
							splitOthers,totRaterType);

					// reinitialize Result and totalRater
					Result = new double[totalType * 2];
					totalRater = new int[totalType * 2];

					column = 9; // write the importance n gap
					int rtemp = row;

					Vector vImportance = Importance(compID,KBID);

					for (int j = 0; j < vImportance.size(); j++) {
						String[] arr = (String[]) vImportance.elementAt(j);
						String task = arr[1];
						double taskResult = Double.parseDouble(arr[2]);

						OO.insertString(xSpreadsheet,task + ": " + taskResult,
								rtemp,column);
						OO.mergeCells(xSpreadsheet,column,endColumn,rtemp,
								rtemp + 1);
						OO.setCellAllignment(xSpreadsheet,column,endColumn,
								rtemp,rtemp + 1,2,1);

						rtemp += 3;
					}

					double gap = 0;
					// only calculate Gap if survey include CPR/FPR Rating task
					if (hasCPRFPR) {// If CPR is chosen in this survey
						gap = getAvgGap(compID);
						// Allow dynamic translation, Chun Yeong 1 Aug 2011
						OO.insertString(xSpreadsheet,
								trans.tslt(templateLanguage,"Gap") + " = "
										+ gap,rtemp,column);
						OO.mergeCells(xSpreadsheet,column,endColumn,rtemp,
								rtemp + 1);
						OO.setCellAllignment(xSpreadsheet,column,endColumn,
								rtemp,rtemp + 1,2,1);
						rtemp += 3;
					}

					double LOA = LevelOfAgreement(compID,KBID);
					// Allow dynamic translation, Chun Yeong 1 Aug 2011
					OO.insertString(xSpreadsheet,
							trans.tslt(templateLanguage,"Level Of Agreement")
									+ ": \n" + LOA + "%",rtemp,column);
					OO.mergeCells(xSpreadsheet,column,endColumn,rtemp,rtemp + 2);
					OO.setCellAllignment(xSpreadsheet,column,endColumn,rtemp,
							rtemp + 2,2,1);

					column = 0;
					count++;

					row += 15;
					endRow = row - 1;

					// Check height and insert pagebreak where necessary
					int pageHeightLimit = 22272;// Page limit is 22272
					int tableHeight = 0;

					// calculate the height of the table that is being dded.
					for (int i1 = startRow + 1; i1 <= endRow + 1; i1++) {
						int rowToCalculate = i1;
						tableHeight += OO.getRowHeight(xSpreadsheet,
								rowToCalculate,startColumn);
					}

					currentPageHeight = currentPageHeight + tableHeight; // add
																			// new
																			// table
																			// height
																			// to
																			// current
																			// pageheight.
					int dis = 2; // Denise 08/01/2009 to move the table two
									// lines down
					if (currentPageHeight > pageHeightLimit) {// adding the
																// table will
																// exceed a
																// single page
						OO.insertRows(xSpreadsheet,startColumn,endColumn,
								startRow,startRow + dis,dis,1);
						OO.insertPageBreak(xSpreadsheet,startColumn,endColumn,
								startRow);
						statementPos += dis;
						row += dis;
						startRow += dis;
						endRow += dis;
						currentPageHeight = tableHeight;
					}
					// Denise 08/01/2009 insert competency statement
					// Added translation for the competency name, Chun Yeong 1
					// Aug 2011
					OO.insertString(xSpreadsheet,UnicodeHelper
							.getUnicodeStringAmp(getTranslatedCompetency(
									statement).elementAt(0).toString()),
							statementPos,0);
					OO.setFontBold(xSpreadsheet,startColumn,endColumn,
							statementPos,statementPos);
					OO.setBGColor(xSpreadsheet,startColumn,endColumn,
							statementPos,statementPos,BGCOLOR);
					// comp name and definition
					OO.setTableBorder(xSpreadsheet,startColumn,endColumn,
							startRow,startRow + 1,false,false,true,true,true,
							true);

					// total sup n others

					OO.setTableBorder(xSpreadsheet,startColumn,startColumn,
							startRow + 2,endRow,false,false,true,true,true,true);

					// chart

					OO.setTableBorder(xSpreadsheet,startColumn + 1,8,
							startRow + 2,endRow,false,false,true,true,true,true);
					OO.setTableBorder(xSpreadsheet,9,endColumn,startRow + 2,
							endRow,false,false,true,true,true,true);
					OO.setCellAllignment(xSpreadsheet,startColumn,startColumn,
							startRow + 2,endRow,1,2);
					OO.setCellAllignment(xSpreadsheet,startColumn,startColumn,
							startRow + 2,endRow,1,2);

					// added by Alvis on 07-Aug-09 to ensure next section begin
					// on a new page.
					if (i == (vComp.size() - 1)) {// last table added
						// insertpagebreak
						OO.insertPageBreak(xSpreadsheet,startColumn,endColumn,
								endRow + 1);
					}
				}// while Comp

				// End Competency Level

			} else {
				// Start KB level
				System.out
						.println("InsertCompetency() - Survey is at KB Level");
				int start = 0;
				int startRow = row; // for border
				int endRow = row;
				for (int i = 0; i < vComp.size(); i++) {

					// Add by Santoso (22/10/08) : reinitialize array per loop
					// reinitialize the array each loop (otherwise it will use
					// the previous value)
					totalRater = new int[totalRater.length];
					Result = new double[Result.length];

					start = 0;
					int RTID = 0;
					int KBID = 0;
					String KB = "";

					voCompetency voComp = (voCompetency) vComp.elementAt(i);
					int compID = voComp.getCompetencyID();
					String statement = voComp.getCompetencyName();
					String desc = voComp.getCompetencyDefinition();

					startRow = row;

					// Added translation for competency name, Chun Yeong 1 Aug
					// 2011
					OO.insertString(
							xSpreadsheet,
							getTranslatedCompetency(
									UnicodeHelper
											.getUnicodeStringAmp(statement))
									.elementAt(0).toString(),row,column);
					OO.setFontBold(xSpreadsheet,startColumn,endColumn,row,row);
					OO.setBGColor(xSpreadsheet,startColumn,endColumn,row,row,
							BGCOLOR);
					row++;

					r1 = row;
					OO.insertString(xSpreadsheet,
							UnicodeHelper.getUnicodeStringAmp(desc),row,column);
					OO.mergeCells(xSpreadsheet,startColumn,endColumn,row,row);
					// adjust the merged cell as top alignment (Qiao Li 21 Dec
					// 2009 )
					OO.setCellAllignment(xSpreadsheet,startColumn,endColumn,
							row,row,2,1);
					OO.setRowHeight(xSpreadsheet,row,1,
							ROWHEIGHT * OO.countTotalRow(desc,90));

					row++;
					start++;

					String RTCode = "";

					Vector RT = RatingTask();

					boolean hasCPRFPR = true;

					for (int j = 0; j < RT.size(); j++) {
						votblSurveyRating vo = (votblSurveyRating) RT
								.elementAt(j);
						RTID = vo.getRatingTaskID();
						RTCode = vo.getRatingCode();

						Vector result = null;

						if (RTCode.equals("CP")) {
							result = KBMean(RTID,compID);

							totalOth = totalRater(RTID,compID,-1,"OTH%",
									surveyLevel);
							totalSup = totalRater(RTID,compID,-1,"SUP%",
									surveyLevel);
							totalSelf = totalRater(RTID,compID,-1,"SELF",
									surveyLevel);
							totalPeer = totalRater(RTID,compID,-1,"PEER%",
									surveyLevel);
							totalSub = totalRater(RTID,compID,-1,"SUB%",
									surveyLevel);
							totalAdd = totalRater(RTID,compID,-1,"ADD%",
									surveyLevel);
							totalAll = totalSup + totalPeer + totalSub
									+ totalOth + totalAdd;

							for (int k = 0; k < result.size(); k++) {
								String[] arr = (String[]) result.elementAt(k);

								int type = Integer.parseInt(arr[1]);
								if (type == 0)
									continue;
								String t = "";
								switch (type) {
									case 1 :
										t = "All";
										totalRater[1] = totalAll;
										break;

									case 2 :
										t = "Superior";
										totalRater[3] = totalSup;
										break;

									case 3 :
										t = "Others";
										totalRater[5] = totalOth;
										break;

									case 4 :
										t = "Self";
										totalRater[7] = totalSelf;
										break;

									case 5 :
										t = "Subordinates";
										totalRater[9] = totalSub;
										break;

									case 6 :
										t = "Peers";
										totalRater[11] = totalPeer;
										break;

									case 7 :
										t = "Additional";
										totalRater[13] = totalAdd;
										break;
								}
								Rating[(type * 2) - 1] = RTCode + "(" + t + ")";
								if (iReportType == 1)
									Rating[(type * 2) - 1] = Rating[(type * 2) - 1]
											.replaceAll("\n"," ");

								if (type == 1)
									Result[(type * 2) - 1] = CompTrimmedMeanforAll(
											RTID,compID);
								else
									Result[(type * 2) - 1] = Double
											.parseDouble(arr[2]);
							}
						} else if (RTCode.equals("CPR") || RTCode.equals("FPR")) {
							result = KBMean(RTID,compID);
							totalOth = totalRater(RTID,compID,-1,"OTH%",
									surveyLevel);
							totalSup = totalRater(RTID,compID,-1,"SUP%",
									surveyLevel);
							totalPeer = totalRater(RTID,compID,-1,"PEER%",
									surveyLevel);
							totalSub = totalRater(RTID,compID,-1,"SUB%",
									surveyLevel);
							totalAdd = totalRater(RTID,compID,-1,"ADD%",
									surveyLevel);
							totalAll = totalSup + totalPeer + totalSub
									+ totalOth + totalAdd;

							for (int k = 0; k < result.size(); k++) {
								String[] arr = (String[]) result.elementAt(k);

								int type = Integer.parseInt(arr[1]);
								String t = "";
								switch (type) {
									case 1 :
										t = "All";
										totalRater[0] = totalAll;
										Rating[0] = RTCode + "(" + t + ")";
										if (iReportType == 1)
											Rating[0] = Rating[0].replaceAll(
													"\n"," ");
										Result[0] = CompTrimmedMeanforAll(RTID,
												compID);
										break;

									case 2 :
										t = "Superior";
										totalRater[2] = totalSup;
										Rating[2] = RTCode + "(" + t + ")";
										if (iReportType == 1)
											Rating[2] = Rating[2].replaceAll(
													"\n"," ");
										Result[2] = Double.parseDouble(arr[2]);
										break;

									case 3 :
										t = "Others";
										totalRater[4] = totalOth;
										Rating[4] = RTCode + "(" + t + ")";
										if (iReportType == 1)
											Rating[4] = Rating[4].replaceAll(
													"\n"," ");
										Result[4] = Double.parseDouble(arr[2]);
										break;

									case 4 :
										t = "Self";
										totalRater[6] = totalSelf;
										Rating[6] = RTCode + "(" + t + ")";
										if (iReportType == 1)
											Rating[6] = Rating[6].replaceAll(
													"\n"," ");
										Result[6] = Double.parseDouble(arr[2]);
										break;

									case 5 :
										t = "Subordinates";
										totalRater[8] = totalSub;
										Rating[8] = RTCode + "(" + t + ")";
										if (iReportType == 1)
											Rating[8] = Rating[8].replaceAll(
													"\n"," ");
										Result[8] = Double.parseDouble(arr[2]);
										break;

									case 6 :
										t = "Peers";
										totalRater[10] = totalPeer;
										Rating[10] = RTCode + "(" + t + ")";
										if (iReportType == 1)
											Rating[10] = Rating[10].replaceAll(
													"\n"," ");
										Result[10] = Double.parseDouble(arr[2]);
										break;

									case 7 :
										t = "Additional";
										totalRater[12] = totalAdd;
										Rating[12] = RTCode + "(" + t + ")";
										if (iReportType == 1)
											Rating[12] = Rating[10].replaceAll(
													"\n"," ");
										Result[12] = Double.parseDouble(arr[2]);
										break;
								}
							}// end for loop
						}// end else if RTCode == CPR || FPR
					}// while RT

					// rater type can change depends on whether "Others" is
					// splitted
					int totRaterType = Rating.length;
					if (splitOthers == 0) {
						totRaterType -= 6;
					} else {
						totRaterType -= 2;
					}

					int[] rowPos = prepareCellsBreakCPR(xSpreadsheet,row,
							totRaterType);
					row++;

					drawChartBreakCPR(Rating,Result,totalRater,rowPos,maxScale,
							splitOthers,totRaterType);

					// reinitialize Result and totalRater
					Result = new double[totalType * 2];
					totalRater = new int[totalType * 2];

					column = 9;
					r1 = row;

					Vector Importance = AvgImportance(compID);

					for (int j = 0; j < Importance.size(); j++) {
						String[] arr = (String[]) Importance.elementAt(j);

						String task = arr[1];
						double taskResult = Double.parseDouble(arr[2]);

						OO.insertString(xSpreadsheet,task + ": " + taskResult,
								r1,column);
						OO.mergeCells(xSpreadsheet,column,endColumn,r1,r1 + 1);
						r1 += 3;
					}

					double gap = 0;
					// Change by Santoso 22/10/08
					// only calculate Gap if survey include CPR/FPR Rating task
					if (hasCPRFPR) {
						int element = vCompID.indexOf(new Integer(compID));
						gap = Double
								.valueOf(
										((String[]) vGapUnsorted
												.elementAt(element))[1])
								.doubleValue();
						// System.out.println(gap + "----" + compID + " --- " +
						// element);
						if (iNoCPR == 0) // If CPR is chosen in this survey
						{
							// Allow dynamic translation, Chun Yeong 1 Aug 2011
							OO.insertString(xSpreadsheet,
									trans.tslt(templateLanguage,"Gap") + " = "
											+ gap,r1,column);
							OO.mergeCells(xSpreadsheet,column,endColumn,r1,
									r1 + 1);
						}
					}
					r1 += 3;

					double LOA = AvgLevelOfAgreement(compID,totalAll);
					// Allow dynamic translation, Chun Yeong 1 Aug 2011
					OO.insertString(xSpreadsheet,
							trans.tslt(templateLanguage,"Level Of Agreement")
									+ ": \n" + LOA + "%",r1,column);
					OO.mergeCells(xSpreadsheet,column,endColumn,r1,r1 + 2);
					r1 += 4;

					count++;
					column = 0;
					if (count == 2) {
						count = 0;

						row += 15;
						OO.insertPageBreak(xSpreadsheet,startColumn,endColumn,
								row);
					} else {
						row += 15;
					}

					endRow = row - 1;

					// comp name and definition
					OO.setTableBorder(xSpreadsheet,startColumn,endColumn,
							startRow,startRow + 1,false,false,true,true,true,
							true);

					// total sup n others
					OO.setTableBorder(xSpreadsheet,startColumn,startColumn,
							startRow + 2,endRow,false,false,true,true,true,true);
					// chart
					OO.setTableBorder(xSpreadsheet,startColumn + 1,8,
							startRow + 2,endRow,false,false,true,true,true,true);
					OO.setTableBorder(xSpreadsheet,9,endColumn,startRow + 2,
							endRow,false,false,true,true,true,true);

					OO.setCellAllignment(xSpreadsheet,startColumn,startColumn,
							startRow + 2,endRow,1,2);

					// KB LEVEL //
					Vector KBList = KBList(compID);

					for (int j = 0; j < KBList.size(); j++) {
						voKeyBehaviour voKB = (voKeyBehaviour) KBList
								.elementAt(j);
						KBID = voKB.getKeyBehaviourID();
						KB = voKB.getKeyBehaviour();

						startRow = row;
						r1 = row;

						// Added translation for the key behaviour, Chun Yeong 1
						// Aug 2011
						OO.insertString(
								xSpreadsheet,
								start
										+ ". "
										+ getTranslatedKeyBehavior(UnicodeHelper
												.getUnicodeStringAmp(KB)),row,0);
						OO.mergeCells(xSpreadsheet,startColumn,endColumn,row,
								row);
						// adjust the merged cell as top alignment (Qiao Li 21
						// Dec 2009 )
						OO.setCellAllignment(xSpreadsheet,startColumn,
								endColumn,row,row,2,1);
						OO.setRowHeight(xSpreadsheet,row,0,
								ROWHEIGHT * OO.countTotalRow(KB,90));

						row += 2;
						// row ++;
						start++;

						totalRater = new int[totalType * 2]; // Changed from 4 +
																// totalOtherRT
																// to 5 +
																// totalOtherRT,
																// to clean up
																// later on, DeZ
						Result = new double[totalType * 2]; // Changed from 4 +
															// totalOtherRT to 5
															// + totalOtherRT,
															// to clean up later
															// on, DeZ

						RT = RatingTask();
						// Change by Santoso 22/10/08
						// only calculate Gap if survey include CPR/FPR Rating
						// task
						// initialize cpr/fpr flag
						hasCPRFPR = true;
						for (int k = 0; k < RT.size(); k++) {
							votblSurveyRating vo = (votblSurveyRating) RT
									.elementAt(k);
							RTID = vo.getRatingTaskID();
							RTCode = vo.getRatingCode();

							Vector result = MeanResult(RTID,compID,KBID);

							if (RTCode.equals("CP")) {
								totalOth = totalOth(RTID,compID,KBID);
								totalSup = totalSup(RTID,compID,KBID);
								totalSelf = totalSelf(RTID,compID,KBID);
								totalPeer = totalRater(RTID,compID,KBID,
										"PEER%",surveyLevel);
								totalSub = totalRater(RTID,compID,KBID,"SUB%",
										surveyLevel);
								totalAdd = totalRater(RTID,compID,KBID,"ADD%",
										surveyLevel);
								totalAll = totalSup + totalPeer + totalSub
										+ totalOth + totalAdd;

								for (int l = 0; l < result.size(); l++) {
									String[] arr = (String[]) result
											.elementAt(l);
									// Updated adjustments to type in order to
									// cater for splitting of subordinates &
									// peers, Desmond 28 Oct 09
									int type = Integer.parseInt(arr[1]);
									if (type == 0)
										continue;
									String t = "";
									switch (type) {
										case 1 :
											t = "All";
											totalRater[1] = totalAll;
											break;

										case 2 :
											t = "Superior"; // Change from
															// Supervisors to
															// Superior, Desmond
															// 22
															// Oct 09
											totalRater[3] = totalSup;
											break;

										case 3 :
											t = "Others";
											totalRater[5] = totalOth;
											break;

										case 4 :
											t = "Self";
											totalRater[7] = totalSelf;
											break;

										case 5 :
											t = "Subordinates";
											totalRater[9] = totalSub;
											break;

										case 6 :
											t = "Peers";
											totalRater[11] = totalPeer;
											break;

										case 7 :
											t = "Additional";
											totalRater[13] = totalAdd;
											break;
									}

									Rating[type * 2 - 1] = RTCode + "(" + t
											+ ")";
									if (iReportType == 1)
										Rating[type * 2 - 1] = Rating[type * 2 - 1]
												.replaceAll("\n"," ");
									Result[type * 2 - 1] = Double
											.parseDouble(arr[2]);;
								}

							} else if (RTCode.equals("CPR")
									|| RTCode.equals("FPR")) {
								totalOth = totalOth(RTID,compID,KBID);
								totalSup = totalSup(RTID,compID,KBID);
								totalPeer = totalRater(RTID,compID,KBID,
										"PEER%",surveyLevel);
								totalSub = totalRater(RTID,compID,KBID,"SUB%",
										surveyLevel);
								totalAdd = totalRater(RTID,compID,KBID,"ADD%",
										surveyLevel);
								totalAll = totalSup + totalPeer + totalSub
										+ totalOth + totalAdd;

								for (int l = 0; l < result.size(); l++) {
									String[] arr = (String[]) result
											.elementAt(l);
									int type = Integer.parseInt(arr[1]);
									String t = "";
									switch (type) {
										case 1 :
											t = "All";
											totalRater[0] = totalAll;
											Rating[0] = RTCode + "(" + t + ")";
											if (iReportType == 1)
												Rating[0] = Rating[0]
														.replaceAll("\n"," ");
											Result[0] = Double
													.parseDouble(arr[2]);
											break;

										case 2 :
											t = "Superior";
											totalRater[2] = totalSup;
											Rating[2] = RTCode + "(" + t + ")";
											if (iReportType == 1)
												Rating[2] = Rating[2]
														.replaceAll("\n"," ");
											Result[2] = Double
													.parseDouble(arr[2]);
											break;

										case 3 :
											t = "Others";
											totalRater[4] = totalOth;
											Rating[4] = RTCode + "(" + t + ")";
											if (iReportType == 1)
												Rating[4] = Rating[4]
														.replaceAll("\n"," ");
											Result[4] = Double
													.parseDouble(arr[2]);
											break;

										case 4 :
											t = "Self";
											totalRater[6] = totalSelf;
											Rating[6] = RTCode + "(" + t + ")";
											if (iReportType == 1)
												Rating[6] = Rating[6]
														.replaceAll("\n"," ");
											Result[6] = Double
													.parseDouble(arr[2]);
											break;

										case 5 :
											t = "Subordinates";
											totalRater[8] = totalSub;
											Rating[8] = RTCode + "(" + t + ")";
											if (iReportType == 1)
												Rating[8] = Rating[8]
														.replaceAll("\n"," ");
											Result[8] = Double
													.parseDouble(arr[2]);
											break;

										case 6 :
											t = "Peers";
											totalRater[10] = totalPeer;
											Rating[10] = RTCode + "(" + t + ")";
											if (iReportType == 1)
												Rating[10] = Rating[10]
														.replaceAll("\n"," ");
											Result[10] = Double
													.parseDouble(arr[2]);
											break;

										case 7 :
											t = "Additional";
											totalRater[12] = totalAdd;
											Rating[12] = RTCode + "(" + t + ")";
											if (iReportType == 1)
												Rating[12] = Rating[12]
														.replaceAll("\n"," ");
											Result[12] = Double
													.parseDouble(arr[2]);
											break;
									}
								}// end for loop
							}// end else if RTCode == CPR || FPR
						}// while RT

						// rater type can change depends on whether "Others" is
						// splitted
						totRaterType = Rating.length;
						if (splitOthers == 0) {
							totRaterType -= 6;
						} else {
							totRaterType -= 2;
						}

						rowPos = prepareCellsBreakCPR(xSpreadsheet,row,
								totRaterType);
						row++;

						drawChartBreakCPR(Rating,Result,totalRater,rowPos,
								maxScale,splitOthers,totRaterType);

						// reinitialize Result and totalRater
						Result = new double[totalType * 2];
						totalRater = new int[totalType * 2];

						column = 9;
						r1 = row;

						Vector vImportance = Importance(compID,KBID);

						for (int k = 0; k < vImportance.size(); k++) {
							String[] arr = (String[]) vImportance.elementAt(k);
							String task = arr[1];
							double taskResult = Double.parseDouble(arr[2]);

							arrN[iN] = totalAll;
							iN++;
							OO.insertString(xSpreadsheet,task + ": "
									+ taskResult,r1,column);
							OO.mergeCells(xSpreadsheet,column,endColumn,r1,
									r1 + 1);
							r1 += 3;
						}

						// Change by Santoso 22/10/08
						// only calculate Gap if survey include CPR/FPR Rating
						// task
						if (hasCPRFPR) {
							gap = Gap(compID,KBID);
							gap = Math.round(gap * 100.0) / 100.0;

							if (iNoCPR == 0) // If CPR is chosen in this survey
							{
								// Allow dynamic translation, Chun Yeong 1 Aug
								// 2011
								OO.insertString(xSpreadsheet,
										trans.tslt(templateLanguage,"Gap")
												+ " = " + gap,r1,column);
								OO.mergeCells(xSpreadsheet,column,endColumn,r1,
										r1 + 1);
							}
						}
						r1 += 3;

						LOA = LevelOfAgreement(compID,KBID);

						// Allow dynamic translation, Chun Yeong 1 Aug 2011
						OO.insertString(
								xSpreadsheet,
								trans.tslt(templateLanguage,
										"Level Of Agreement")
										+ ": \n"
										+ LOA
										+ "%",r1,column);
						OO.mergeCells(xSpreadsheet,column,endColumn,r1,r1 + 2);
						r1 += 4;

						count++;
						column = 0;
						// added a page break at the end of the chart section of
						// KB Level Survey
						// to solve part of pagination problem (Qiao Li 23 Dec
						// 2009)
						if (count == 2
								|| (j == KBList.size() - 1 && i == vComp.size() - 1)) {
							count = 0;
							/*
							 * Change: standardize the number of rows for the
							 * charts to be 15 Reason: fit 2 charts in one page
							 * Updated by: Qiao Li Date: 23 Dec 2009
							 */
							row += 15;
							OO.insertPageBreak(xSpreadsheet,startColumn,
									endColumn,row);

						} else
							row += 15;

						endRow = row - 1;

						// comp name and definition
						OO.setTableBorder(xSpreadsheet,startColumn,endColumn,
								startRow,startRow + 1,false,false,true,true,
								true,true);
						// total sup n others
						OO.setTableBorder(xSpreadsheet,startColumn,startColumn,
								startRow + 2,endRow,false,false,true,true,true,
								true);
						// chart
						OO.setTableBorder(xSpreadsheet,startColumn + 1,8,
								startRow + 2,endRow,false,false,true,true,true,
								true);
						OO.setTableBorder(xSpreadsheet,9,endColumn,
								startRow + 2,endRow,false,false,true,true,true,
								true);

						OO.setCellAllignment(xSpreadsheet,startColumn,
								startColumn,startRow + 2,endRow,1,2);

						// /Denise 07/01/2009
						if (count == 0) { // move table two lines down
							OO.insertRows(xSpreadsheet,0,10,row + 1,row + 3,2,1);
							row += 2;
						}
					}// while KBList
						// } // end if of standard version
				}// while Comp
			}// end of KB Level survey
		}// end of breakCPR ==1
	} // End InsertCompetency()

	/**
	 * Change by Santoso (2008-10-08) total rater is printed inside drawChart,
	 * we need to pass the total rater list and the position to draw
	 * 
	 * Draw bar chart for competency report.
	 */
	/**
	 * drawChart
	 * 
	 * 
	 * @param Rating
	 * @param Result
	 * @param totalRater
	 * @param rowPos
	 * @param maxScale
	 * @param splitOthers
	 *            changed by Qiao Li (17 Dec 2009) added in splitOthers to
	 *            indicate whether we should show "Others" or "Subordinates" and
	 *            "Peers" if splitOthers ==0, show "Others", if splitOthers==1,
	 *            show "Subordinates" and "Peers" precondition: Ratings should
	 *            only include:"CP(All)","CP(Superior)","CP(Others)","CP(Self)",
	 *            "CP(Subordinates)","CP(Peers)";
	 * */
	public void drawChart(String Rating[], double Result[], int[] totalRater,
			int[] rowPos, int maxScale, int splitOthers) throws IOException,
			Exception {
		// iReportType = 1 (Simplified Report "No Comp Chart"), 2 (Standard
		// Report)
		int r = row;
		int c = 0;
		/*
		 * Change (s): added in selfIdx to get values of "Self" Reason (s):
		 * rearrange "Self" to be at the bottom of the chart Updated by: Qiao Li
		 * Date: 17 Dec 2009
		 */
		int selfIdx = -1;
		int cellIdx = 0;

		if (iReportType == 1) {
			// Print heading for "N" and align
			OO.insertString(xSpreadsheet,"N",r,c + 5);
			OO.setCellAllignment(xSpreadsheet,c + 5,c + 5,r,r,1,3);
		}

		/*
		 * Change (s): Calculation of the Others value when split option is not
		 * selected Reason (s): To allow specific reports to have a not split
		 * option Updated by: Chun Yeong Date: 13 Jun 2011
		 */
		if (splitOthers == 0) {
			// Find the index of Others, Subordinate, Peer, and Additional
			int iOth = 0, iSub = 0, iPeer = 0, iAdd = 0, iDir = 0, iIndir = 0;
			for (int i = 0; i < Rating.length; i++) {

				if (Rating[i].contains("Oth")) {
					iOth = i;
				}
				if (Rating[i].contains("Sub")) {
					iSub = i;
				}
				if (Rating[i].contains("Peer")) {
					iPeer = i;
				}
				if (Rating[i].contains("Add")) {
					iAdd = i;
				}
				if (Rating[i].contains("Dir")) {
					iDir = i;
				}
				if (Rating[i].contains("Indir")) {
					iIndir = i;
				}

			}

			// Do reverse engineering to calculate the new Others value
			formatter.setRoundingMode(RoundingMode.HALF_UP);
			String value = formatter
					.format(((totalRater[iSub] * Result[iSub])
							+ (totalRater[iPeer] * Result[iPeer])
							+ (totalRater[iOth] * Result[iOth]) + (totalRater[iAdd] * Result[iAdd]))
							/ (totalRater[iSub] + totalRater[iPeer]
									+ totalRater[iOth] + totalRater[iAdd]));
			// Store the value into Others row in Result
			Result[iOth] = Double.parseDouble(value);
			// Store the total number of people into Others row in totalRater
			totalRater[iOth] += totalRater[iSub] + totalRater[iPeer]
					+ totalRater[iAdd];
		}

		for (int i = 0; i < Rating.length; i++) {
			// Change by Santoso (2008-10-08)
			// add protection in case Rating[i] is null --> we fixed the rating
			if (Rating[i] != null) {
				// identify "Self" (Qiao Li 17 Dec 2009)
				if (Rating[i].equals("CP(Self)")) {
					selfIdx = i;
					continue;
				}

				/*
				 * Change (s): added in flags Reason (s): show "others" or
				 * ("subordinates" and "peers") Updated by: Qiao Li Date: 17 Dec
				 * 2009
				 */
				if ((splitOthers == 1 && !Rating[i].equals("CP(Others)"))
						|| (splitOthers == 0
								&& !Rating[i].equals("CP(Subordinates)")
								&& !Rating[i].equals("CP(Additional)") && !Rating[i]
									.equals("CP(Peers)"))) {
					r++;
					OO.insertString(xSpreadsheet,
							trans.tslt(templateLanguage,Rating[i]),r,c + 2);
					OO.mergeCells(xSpreadsheet,c + 2,c + 3,r,r);

					OO.insertNumeric(xSpreadsheet,Result[i],r,c + 4);
					// change by Santoso (2008-10-08)
					// use totalRater (already sorted) instead of arrN
					// OO.insertNumeric(xSpreadsheet, arrN[i], r, c+5);
					OO.insertNumeric(xSpreadsheet,totalRater[i],r,c + 5);
				}
			}
		}
		if (selfIdx != -1) {
			// identify "Self" and put it at the bottom of the chart(Qiao Li 17
			// Dec 2009)
			r++;
			OO.insertString(xSpreadsheet,
					trans.tslt(templateLanguage,Rating[selfIdx]),r,c + 2);
			OO.mergeCells(xSpreadsheet,c + 2,c + 3,r,r);

			OO.insertNumeric(xSpreadsheet,Result[selfIdx],r,c + 4);
			// change by Santoso (2008-10-08)
			// use totalRater (already sorted) instead of arrN
			// OO.insertNumeric(xSpreadsheet, arrN[i], r, c+5);
			OO.insertNumeric(xSpreadsheet,totalRater[selfIdx],r,c + 5);
		}
		selfIdx = -1;// reset
		r = row; // reset
		if (iReportType == 2) {

			/*
			 * Change(s) : add a line to insert "n" at top of no. of raters
			 * column Reason(s) : as per requirement of software Added By: Alvis
			 * Added On: 06 Aug 2009
			 */
			// Print "n" right at the top of the no.of.raters column
			// qiaoli
			// OO.insertString(xSpreadsheet, "n", rowPos[0]-2, 0);
			OO.insertString(xSpreadsheet,"n",r - 1,0);
			// Add by Santoso (2008-10-08)
			// Print the total rater value here, instead of in many places
			/*
			 * commented out for loopdo the printing of n later instead such
			 * that we can put "self" at the bottom (Qiao Li 17 Dec 2009)
			 */
			/*
			 * for (int idx = 0; idx < totalRater.length; idx++) { //
			 * OO.insertNumeric(xSpreadsheet, totalAll, rowTotal, 0);
			 * OO.insertNumeric(xSpreadsheet, totalRater[idx], rowPos[idx], 0);
			 * }
			 */
			cellIdx = 0;
			for (int i = 0; i < Rating.length; i++) {
				// Added by Ha 25/06/08 to get rid of "0" line in the report
				if (Rating[i].equals("nil")) {
					continue;
				}
				if (Rating[i] != null) {
					// identify "self" (Qiao Li 17 Dec 2009)
					if (Rating[i].equals("CP(Self)")) {
						selfIdx = i;
						continue;
					}
					if (Rating[i].equals("CP(Others)")) {
						continue;
					}
					/*
					 * if (Rating[i].equals("CP(Subordinates)")) { continue; }
					 */
					if (Rating[i].equals("CP(Additional)")) {
						continue;
					}
					/*
					 * Change (s): added in flags Reason (s): show "others" or
					 * ("subordinates" and "peers") Updated by: Qiao Li Date: 17
					 * Dec 2009
					 */
					OO.insertString(xSpreadsheet2,
							trans.tslt(templateLanguage,Rating[i]),r,c);
					OO.insertNumeric(xSpreadsheet2,Result[i],r,c + 1);
					OO.insertNumeric(xSpreadsheet,totalRater[i],
							rowPos[cellIdx],0);
					r++;
					cellIdx++;
				}
			}
			if (selfIdx != -1) {
				OO.insertString(xSpreadsheet2,
						trans.tslt(templateLanguage,Rating[selfIdx]),r,c);
				OO.insertNumeric(xSpreadsheet2,Result[selfIdx],r,c + 1);
				// System.out.println("test " + rowPos.length + "  " + cellIdx +
				// " " + selfIdx + "  " + totalRater.length);
				OO.insertNumeric(xSpreadsheet,totalRater[selfIdx],
						rowPos[cellIdx],0);
				r++;
			}

			// draw chart
			XTableChart xtablechart = OO.getChart(xSpreadsheet,xSpreadsheet2,c,
					c + 1,row - 1,r - 1,Integer.toString(row),10000,7800,row,2);
			OO.setFontSize(10);
			xtablechart = OO.setChartTitle(xtablechart,"");
			OO.showLegend(xtablechart,false);
			// (22-08-06) Axes should all be 0 degree right? Otherwise, slanted
			// score seems funny (Maruli)
			// xtablechart = OO.setAxes(xtablechart, "", "Scores (%)", maxScale,
			// 1, 0, 4500);
			xtablechart = OO
					.setAxes(xtablechart,"","Scores (%)",maxScale,1,0,0);
			OO.drawGridLines(xtablechart,0); // draw the gridlines, Mark Oei 25
												// Mar 2010
			OO.setChartProperties(xtablechart,false,true,false,false,true); // display
																			// only
																			// the
																			// vertical
																			// lines,
																			// Mark
																			// Oei
																			// 25
																			// Mar
																			// 2010
		}// end iReportType
	}

	/**
	 * @param Rating
	 * @param Result
	 * @param totalRater
	 * @param rowPos
	 * @param maxScale
	 * @param splitOthers
	 * @author Albert
	 * @date 10-07-2012 if splitOthers ==0, show "Others", if splitOthers==1,
	 *       show "Subordinates" and "Peers"
	 * */
	public void drawChartBreakCPR(String Rating[], double Result[],
			int[] totalRater, int[] rowPos, int maxScale, int splitOthers,
			int totalBar) throws IOException, Exception {
		// iReportType = 1 (Simplified Report "No Comp Chart"), 2 (Standard
		// Report)
		int r = row;
		int c = 0;
		/*
		 * Change (s): added in selfIdx to get values of "Self" Reason (s):
		 * rearrange "Self" to be at the bottom of the chart Updated by: Qiao Li
		 * Date: 17 Dec 2009
		 */
		int selfIdx = -1;
		int cellIdx = 0;

		if (iReportType == 1) {
			// Print heading for "N" and align
			OO.insertString(xSpreadsheet,"N",r,c + 5);
			OO.setCellAllignment(xSpreadsheet,c + 5,c + 5,r,r,1,3);
		}

		/*
		 * Change (s): Calculation of the Others value when split option is not
		 * selected Reason (s): To allow specific reports to have a not split
		 * option Updated by: Chun Yeong Date: 13 Jun 2011
		 */
		if (splitOthers == 0) {
			// Find the index of Others, Subordinate and Peers
			int iOth = 0, iSub = 0, iPeer = 0, iAdd = 0;
			for (int i = 0; i < Rating.length; i++) {
				if (Rating[i].contains("Oth")) {
					iOth = i;
				}
				if (Rating[i].contains("Sub")) {
					iSub = i;
				}
				if (Rating[i].contains("Peer")) {
					iPeer = i;
				}
				if (Rating[i].contains("Add")) {
					iAdd = i;
				}
			}

			// Do reverse engineering to calculate the new Others value
			// for CP
			formatter.setRoundingMode(RoundingMode.HALF_UP);
			String value = formatter
					.format(((totalRater[iSub] * Result[iSub])
							+ (totalRater[iPeer] * Result[iPeer])
							+ (totalRater[iOth] * Result[iOth]) + (totalRater[iAdd] * Result[iAdd]))
							/ (totalRater[iSub] + totalRater[iPeer]
									+ totalRater[iOth] + totalRater[iAdd]));
			// Store the value into Others row in Result
			Result[iOth] = Double.parseDouble(value);
			// Store the total number of people into Others row in totalRater
			totalRater[iOth] += totalRater[iSub] + totalRater[iPeer]
					+ totalRater[iAdd];

			// for CPR
			value = formatter
					.format(((totalRater[iSub - 1] * Result[iSub - 1])
							+ (totalRater[iPeer - 1] * Result[iPeer - 1])
							+ (totalRater[iAdd - 1] * Result[iAdd - 1]) + (totalRater[iOth - 1] * Result[iOth - 1]))
							/ (totalRater[iSub - 1] + totalRater[iPeer - 1]
									+ totalRater[iAdd - 1] + totalRater[iOth - 1]));
			// Store the value into Others row in Result
			Result[iOth - 1] = Double.parseDouble(value);
			// Store the total number of people into Others row in totalRater
			totalRater[iOth - 1] += totalRater[iSub - 1]
					+ totalRater[iPeer - 1] + totalRater[iAdd - 1];
		}

		for (int i = 0; i < Rating.length; i++) {
			// Change by Santoso (2008-10-08)
			// add protection in case Rating[i] is null --> we fixed the rating
			if (Rating[i] != null) {
				// identify "Self" (Qiao Li 17 Dec 2009)
				if (Rating[i].equals("CPR(Self)")
						|| Rating[i].equals("CP(Self)")) {
					selfIdx = i;
					continue;
				}

				/*
				 * Change (s): added in flags Reason (s): show "others" or
				 * ("subordinates" and "peers") Updated by: Qiao Li Date: 17 Dec
				 * 2009
				 */
				if ((splitOthers == 1 && !Rating[i].equals("CP(Others)") && !Rating[i]
						.equals("CPR(Others)"))
						|| (splitOthers == 0
								&& !Rating[i].equals("CP(Subordinates)")
								&& !Rating[i].equals("CP(Peers)")
								&& !Rating[i].equals("CPR(Subordinates)")
								&& !Rating[i].equals("CPR(Peers)")
								&& !Rating[i].equals("CP(Additional)") && !Rating[i]
									.equals("CPR(Additional)"))) {
					r++;
					OO.insertString(xSpreadsheet,
							trans.tslt(templateLanguage,Rating[i]),r,c + 2);
					OO.mergeCells(xSpreadsheet,c + 2,c + 3,r,r);
					OO.insertNumeric(xSpreadsheet,Result[i],r,c + 4);
					OO.insertNumeric(xSpreadsheet,totalRater[i],r,c + 5);
				}
			}
		}
		if (selfIdx != -1) {
			// identify "Self" and put it at the bottom of the chart(Qiao Li 17
			// Dec 2009)
			r++;
			OO.insertString(xSpreadsheet,
					trans.tslt(templateLanguage,Rating[selfIdx - 1]),r,c + 2);
			OO.mergeCells(xSpreadsheet,c + 2,c + 3,r,r);
			OO.insertNumeric(xSpreadsheet,Result[selfIdx - 1],r,c + 4);
			OO.insertNumeric(xSpreadsheet,totalRater[selfIdx - 1],r,c + 5);
			r++;
			OO.insertString(xSpreadsheet,
					trans.tslt(templateLanguage,Rating[selfIdx]),r,c + 2);
			OO.mergeCells(xSpreadsheet,c + 2,c + 3,r,r);
			OO.insertNumeric(xSpreadsheet,Result[selfIdx],r,c + 4);
			OO.insertNumeric(xSpreadsheet,totalRater[selfIdx],r,c + 5);
		}
		selfIdx = -1;// reset
		r = row; // reset
		if (iReportType == 2) {
			/*
			 * Change(s) : add a line to insert "n" at top of no. of raters
			 * column Reason(s) : as per requirement of software Added By: Alvis
			 * Added On: 06 Aug 2009
			 */
			// Print "n" right at the top of the no.of.raters column
			OO.insertString(xSpreadsheet,"n",r - 1,0);

			cellIdx = 0;
			for (int i = 0; i < Rating.length; i++) {
				// Added by Ha 25/06/08 to get rid of "0" line in the report
				if (Rating[i] != null) {
					// identify "self" (Qiao Li 17 Dec 2009)
					if (Rating[i].equals("CP(Self)")
							|| Rating[i].equals("CPR(Self)")) {
						selfIdx = i;
						continue;
					}
					/*
					 * Change (s): added in flags Reason (s): show "others" or
					 * ("subordinates" and "peers") Updated by: Qiao Li Date: 17
					 * Dec 2009
					 */
					if ((splitOthers == 1 && !Rating[i].equals("CP(Others)") && !Rating[i]
							.equals("CPR(Others)"))
							|| (splitOthers == 0
									&& !Rating[i].equals("CP(Subordinates)")
									&& !Rating[i].equals("CP(Peers)")
									&& !Rating[i].equals("CPR(Subordinates)")
									&& !Rating[i].equals("CPR(Peers)")
									&& !Rating[i].equals("CP(Additional)") && !Rating[i]
										.equals("CPR(Additional)"))) {
						OO.insertString(xSpreadsheet2,
								trans.tslt(templateLanguage,Rating[i]),r,c);
						OO.insertNumeric(xSpreadsheet2,Result[i],r,c + 1);
						OO.insertNumeric(xSpreadsheet,totalRater[i],
								rowPos[cellIdx],0);
						r++;
						cellIdx++;
					}
				}
			}
			if (selfIdx != -1) {
				OO.insertString(xSpreadsheet2,
						trans.tslt(templateLanguage,Rating[selfIdx - 1]),r,c);
				OO.insertNumeric(xSpreadsheet2,Result[selfIdx - 1],r,c + 1);
				OO.insertNumeric(xSpreadsheet,totalRater[selfIdx - 1],
						rowPos[cellIdx],0);
				r++;
				cellIdx++;
				OO.insertString(xSpreadsheet2,
						trans.tslt(templateLanguage,Rating[selfIdx]),r,c);
				OO.insertNumeric(xSpreadsheet2,Result[selfIdx],r,c + 1);
				OO.insertNumeric(xSpreadsheet,totalRater[selfIdx],
						rowPos[cellIdx],0);
				r++;
				cellIdx++;
			}

			// draw chart

			XTableChart xtablechart = OO.getChart(xSpreadsheet,xSpreadsheet2,c,
					c + 1,row - 1,r - 1,Integer.toString(row),10000,7700,row,2);
			// OO.setRowHeight(xSpreadsheet,r+4,c,(ROWHEIGHT*1)+300);
			OO.setFontSize(8);
			OO.showLegend(xtablechart,false);
			OO.setBarTwoColor(xtablechart,0x000066,0xFF6600,totalBar);

			xtablechart = OO
					.setAxes(xtablechart,"","Scores (%)",maxScale,1,0,0);
			OO.drawGridLines(xtablechart,0); // draw the gridlines, Mark Oei 25
												// Mar 2010
			OO.setChartProperties(xtablechart,false,true,false,false,true); // display
																			// only
																			// the
																			// vertical
																			// lines,
																			// Mark
																			// Oei
																			// 25
																			// Mar
																			// 2010
		}// end iReportType
	}

	/**
	 * Write comments on excel.
	 */

	public void InsertClusterComments() throws SQLException, IOException,
			Exception {
		// Added by Alvis 07-Aug-09 for pagebreak fix implemented below
		int currentPageHeight = 1076;// 1076 is to accomodate the first two
										// cells, which contains the title.
		Vector vClust = ClusterListHaveComments();
		Vector vComp = null;
		row++;

		int surveyLevel = Integer.parseInt(surveyInfo[0]);
		column = 0;

		// Allow dynamic translation, Chun Yeong 1 Aug 2011
		OO.insertString(xSpreadsheet,
				trans.tslt(templateLanguage,"Narrative Comments"),row,column);
		OO.setFontBold(xSpreadsheet,startColumn,endColumn,row,row);

		row += 2;

		if (vClust.size() == 0) {
			OO.insertString(xSpreadsheet,"Nil",row,column);
		}
		row++;

		int startBorder1 = 1;
		int startBorder = 1;
		int endBorder = 1;
		int endBorder1 = 1;
		int selfIncluded = Q.SelfCommentIncluded(surveyID);
		int column = 0;

		// added by Ping Yang on 11/08/08, check raters assigned
		boolean blnSupIncluded = Q.SupCommentIncluded(surveyID,targetID);
		boolean blnOthIncluded = Q.OthCommentIncluded(surveyID,targetID);
		// To remove, Desmond 16 Nov 09
		if (surveyLevel == 0) {

			// ----------------- START COMPETENCY LEVEL SURVEY -----------------
			// //

			int count = 0;
			for (int l = 0; l < vClust.size(); l++) {
				voCluster voClust = (voCluster) vClust.elementAt(l);
				String clusterName = voClust.getClusterName();
				int clusterID = voClust.getClusterID();
				vComp = ClusterCompListHaveComments(clusterID);

				OO.insertString(xSpreadsheet,clusterName,row,column);
				OO.setFontBold(xSpreadsheet,startColumn,endColumn,row,row);
				OO.setBGColor(xSpreadsheet,startColumn,endColumn,row,row,
						BGCOLORCLUSTER);
				OO.setCellAllignment(xSpreadsheet,startColumn,endColumn,row,
						row,2,1); // Set alignment of competency name to top,
									// Desmond 16 Nov 09
				row++;

				startBorder = row;
				count = 0;
				for (int i = 0; i < vComp.size(); i++) {
					voCompetency voComp = (voCompetency) vComp.elementAt(i);

					int compID = voComp.getCompetencyID();
					String statement = voComp.getCompetencyName();

					count++;

					int statementPos = row; // Denise 08/01/2009 store position
											// to insert competency description

					int KBID = 0;
					int start = 0;
					row++;

					// Added by Ha 23/06/08 reset the value start to print the
					// header of comment correctly
					start = 0;
					Vector supComments = getComments("SUP%",compID,KBID);

					// Added variables to store comments from peers and
					// subordinates, Desmond 18 Nov 09
					Vector othComments = getComments("OTH%",compID,KBID);
					Vector peerComments = getComments("PEER%",compID,KBID);
					Vector subComments = getComments("SUB%",compID,KBID);
					Vector addComments = getComments("ADD%",compID,KBID);

					if (blnSupIncluded) {// added by Ping Yang on 11/08/08,
											// check raters assigned
						boolean blnSupCommentExists = false;// Added by ping
															// yang on 31/7/08
															// to get rid of
															// extra '-'s

						for (int j = 0; j < supComments.size(); j++) {
							String[] arr = (String[]) supComments.elementAt(j);
							if (start == 0) {

								OO.insertString(xSpreadsheet,trans.tslt(
										templateLanguage,"Superior(s)"),row,
										column + 1); // Change from Supervisors
														// to Superior, Desmond
														// 22 Oct 09
								OO.setFontBold(xSpreadsheet,startColumn,
										endColumn,row,row);
								OO.setFontItalic(xSpreadsheet,startColumn,
										endColumn,row,row);

								row++;
								start++;
							}

							String comment = arr[1];
							if (!comment.trim().equals("")) {// Added by ping
																// yang on
																// 31/7/08 to
																// get rid of
																// extra '-'s
								OO.insertString(
										xSpreadsheet,
										"- "
												+ UnicodeHelper
														.getUnicodeStringAmp(comment),
										row,column + 1);
								OO.mergeCells(xSpreadsheet,column + 1,
										endColumn,row,row);
								OO.setRowHeight(
										xSpreadsheet,
										row,
										column + 1,
										ROWHEIGHT
												* OO.countTotalRow(comment,85));
								OO.setCellAllignment(xSpreadsheet,startColumn,
										startColumn,row,row,2,1);
								row++;
								blnSupCommentExists = true;
							}

						}

						start = 0;

						/*
						 * Change(s) : Added codes to point to next row below if
						 * exist rater code comments. Remove codes that add
						 * default empty comment in the report if rater code
						 * have no comments Reason(s) : To remove empty
						 * narrative comments by rater category, KB then
						 * Competency. i.e If competency has no comments from
						 * raters, remove the entire competency in the narrative
						 * comments. Updated By: Sebastian Updated On: 19 July
						 * 2010
						 */
						if (supComments.size() > 0) {
							row++;
						}
					}// end if(blnSupIncluded)

					if (blnOthIncluded) {// added by Ping Yang on 11/08/08,
											// check raters assigned
						/*
						 * Change: determine whether to show Others'comments
						 * based on splitOthers Updated by: Qiao Li 23 dec 2009
						 */
						if (splitOthers == 1) {
							// Added codes to insert peers' comments, Desmond 23
							// Nov 09
							// Insert Peers' comments

							boolean blnPeerCommentExist = false; // Added to get
																	// rid of
																	// extra
																	// '-'s
							for (int k = 0; k < peerComments.size(); k++) {
								String[] arr = (String[]) peerComments
										.elementAt(k);
								String comment = arr[1];

								if (start == 0) {
									// Changed the default language to English
									// by Chun Yeong 9 Jun 2011
									// Commented away to allow translation
									// below, Chun Yeong 1 Aug 2011
									/*
									 * if (ST.LangVer == 2) //Indonesian
									 * OO.insertString(xSpreadsheet, "Lainnya",
									 * row, column + 1); else // if (ST.LangVer
									 * == 1) English
									 * OO.insertString(xSpreadsheet, "Peer(s)",
									 * row, column + 1);
									 */

									// Allow dynamic translation, Chun Yeong 1
									// Aug 2011
									OO.insertString(xSpreadsheet,trans.tslt(
											templateLanguage,"Peer(s)"),row,
											column + 1);
									OO.setFontBold(xSpreadsheet,startColumn,
											endColumn,row,row);
									OO.setFontItalic(xSpreadsheet,startColumn,
											endColumn,row,row);

									start++;
									row++;
								}

								if (!comment.trim().equals("")) {
									OO.insertString(
											xSpreadsheet,
											"- "
													+ UnicodeHelper
															.getUnicodeStringAmp(comment),
											row,column + 1);
									OO.mergeCells(xSpreadsheet,column + 1,
											endColumn,row,row);
									OO.setRowHeight(
											xSpreadsheet,
											row,
											column + 1,
											ROWHEIGHT
													* OO.countTotalRow(comment,
															85));
									OO.setCellAllignment(xSpreadsheet,
											startColumn,endColumn,row,row,2,1);

									row++;
									blnPeerCommentExist = true;
								}
							}

							// Adjust counters
							start = 0;

							if (peerComments.size() > 0) {
								row++;
							}

							// Added codes to insert subordinates' comments,
							// Desmond 23 Nov 09
							// Insert Subordinates' comments
							boolean blnSubCommentExist = false; // Added to get
																// rid of extra
																// '-'s
							for (int k = 0; k < subComments.size(); k++) {
								String[] arr = (String[]) subComments
										.elementAt(k);
								String comment = arr[1];

								if (start == 0) {
									// Changed the default language to English
									// by Chun Yeong 9 Jun 2011
									// Commented away to allow translation
									// below, Chun Yeong 1 Aug 2011
									/*
									 * if (ST.LangVer == 2) //Indonesian
									 * OO.insertString(xSpreadsheet, "Lainnya",
									 * row, column + 1); else //if (ST.LangVer
									 * == 1) English
									 * OO.insertString(xSpreadsheet,
									 * "Subordinate(s)", row, column + 1);
									 */

									// Allow dynamic translation, Chun Yeong 1
									// Aug 2011
									OO.insertString(xSpreadsheet,trans.tslt(
											templateLanguage,"Subordinate(s)"),
											row,column + 1);
									OO.setFontBold(xSpreadsheet,startColumn,
											endColumn,row,row);
									OO.setFontItalic(xSpreadsheet,startColumn,
											endColumn,row,row);

									start++;
									row++;
								}

								if (!comment.trim().equals("")) {
									OO.insertString(
											xSpreadsheet,
											"- "
													+ UnicodeHelper
															.getUnicodeStringAmp(comment),
											row,column + 1);
									OO.mergeCells(xSpreadsheet,column + 1,
											endColumn,row,row);
									OO.setRowHeight(
											xSpreadsheet,
											row,
											column + 1,
											ROWHEIGHT
													* OO.countTotalRow(comment,
															85));
									OO.setCellAllignment(xSpreadsheet,
											startColumn,endColumn,row,row,2,1);

									row++;
									blnSubCommentExist = true;
								}
							}

							// Adjust counters
							start = 0;

							if (subComments.size() > 0) {
								row++;
							}

							// Added codes to insert Additional Raters' comments
							// ~ Albert 5 Sept 2012
							boolean blnAddCommentExist = false; // Added to get
																// rid of extra
																// '-'s
							for (int k = 0; k < addComments.size(); k++) {
								String[] arr = (String[]) addComments
										.elementAt(k);
								String comment = arr[1];

								if (start == 0) {
									// Allow dynamic translation, Chun Yeong 1
									// Aug 2011
									OO.insertString(xSpreadsheet,trans.tslt(
											templateLanguage,
											"Additional Rater(s)"),row,
											column + 1);
									OO.setFontBold(xSpreadsheet,startColumn,
											endColumn,row,row);
									OO.setFontItalic(xSpreadsheet,startColumn,
											endColumn,row,row);

									start++;
									row++;
								}

								if (!comment.trim().equals("")) {
									OO.insertString(
											xSpreadsheet,
											"- "
													+ UnicodeHelper
															.getUnicodeStringAmp(comment),
											row,column + 1);
									OO.mergeCells(xSpreadsheet,column + 1,
											endColumn,row,row);
									OO.setRowHeight(
											xSpreadsheet,
											row,
											column + 1,
											ROWHEIGHT
													* OO.countTotalRow(comment,
															85));
									OO.setCellAllignment(xSpreadsheet,
											startColumn,endColumn,row,row,2,1);

									row++;
									blnAddCommentExist = true;
								}
							}

							// Adjust counters
							start = 0;

							if (addComments.size() > 0) {
								row++;
							}
						}

						// Added codes so that Others' comments (including the
						// header "Others") is displayed only if at least one
						// Others' comment exists, Desmond, 18 Nov 09
						// Execute this portion of codes only if there are
						// Others' comments, if not don't even print out the
						// header "Others"
						/*
						 * Change: determine whether to show Others'comments
						 * based on splitOthers Updated by: Qiao Li 23 dec 2009
						 */
						else {
							// Insert Others' comments

							// Added both peers and subordinate comments
							// together with other's comments to append them all
							// by Chun Yeong 1 Jul 2011
							othComments.addAll(peerComments);
							othComments.addAll(subComments);
							othComments.addAll(addComments);

							boolean blnOthCommentExists = false;// Added to get
																// rid of extra
																// '-'s

							for (int j = 0; j < othComments.size(); j++) {
								String[] arr = (String[]) othComments
										.elementAt(j);
								String comment = arr[1];

								if (start == 0) {
									// Changed the default language to English
									// by Chun Yeong 9 Jun 2011
									// Commented away to allow translation
									// below, Chun Yeong 1 Aug 2011
									/*
									 * if (ST.LangVer == 2) //Indonesian
									 * OO.insertString(xSpreadsheet,
									 * "Orang lain", row, column+1); else //if
									 * (ST.LangVer == 1) English
									 * OO.insertString(xSpreadsheet, "Others",
									 * row, column+1);
									 */

									// Allow dynamic translation, Chun Yeong 1
									// Aug 2011
									OO.insertString(xSpreadsheet,trans.tslt(
											templateLanguage,"Others"),row,
											column + 1);
									OO.setFontBold(xSpreadsheet,startColumn,
											endColumn,row,row);
									OO.setFontItalic(xSpreadsheet,startColumn,
											endColumn,row,row);

									start++;
									row++;
								}

								// Added to get rid of extra '-'s
								if (!comment.trim().equals("")) {
									OO.insertString(
											xSpreadsheet,
											"- "
													+ UnicodeHelper
															.getUnicodeStringAmp(comment),
											row,column + 1);
									OO.mergeCells(xSpreadsheet,column + 1,
											endColumn,row,row);
									OO.setRowHeight(
											xSpreadsheet,
											row,
											column + 1,
											ROWHEIGHT
													* OO.countTotalRow(comment,
															85));
									OO.setCellAllignment(xSpreadsheet,
											startColumn,startColumn,row,row,2,1);
									row++;
									blnOthCommentExists = true;
								}
							}

							start = 0;

							if (othComments.size() > 0) {
								row++;
							}
						} // end if(!othComments.isEmpty())
					}// end if(blnOthIncluded)

					// changed the order of comments by moving "Self" to the
					// back
					// Qiao Li 23 Dec 2009
					if (selfIncluded == 1) {
						Vector selfComments = getComments("SELF",compID,KBID);

						if (selfComments != null) {
							boolean blnSelfCommentExists = false;// Added by
																	// ping yang
																	// on
																	// 31/7/08
																	// to get
																	// rid of
																	// extra
																	// '-'s
							for (int j = 0; j < selfComments.size(); j++) {
								String[] arr = (String[]) selfComments
										.elementAt(j);
								if (start == 0) {
									// Changed the default language to English
									// by Chun Yeong 9 Jun 2011
									// Commented away to allow translation
									// below, Chun Yeong 1 Aug 2011
									/*
									 * if (ST.LangVer == 2)
									 * OO.insertString(xSpreadsheet,
									 * "Diri Sendiri", row, column+1); else //if
									 * (ST.LangVer == 1)
									 * OO.insertString(xSpreadsheet, "Self",
									 * row, column+1);
									 */

									// Allow dynamic translation, Chun Yeong 1
									// Aug 2011
									OO.insertString(
											xSpreadsheet,
											trans.tslt(templateLanguage,"Self"),
											row,column + 1);
									OO.setFontBold(xSpreadsheet,startColumn,
											endColumn,row,row);
									OO.setFontItalic(xSpreadsheet,startColumn,
											endColumn,row,row);

									row++;
									start++;
								}

								String comment = arr[1];

								if (!comment.trim().equals("")) {// Added by
																	// ping yang
																	// on
																	// 31/7/08
																	// to get
																	// rid of
																	// extra
																	// '-'s
									OO.insertString(
											xSpreadsheet,
											"- "
													+ UnicodeHelper
															.getUnicodeStringAmp(comment),
											row,column + 1);
									OO.mergeCells(xSpreadsheet,column + 1,
											endColumn,row,row);
									OO.setRowHeight(
											xSpreadsheet,
											row,
											column + 1,
											ROWHEIGHT
													* OO.countTotalRow(comment,
															85));
									OO.setCellAllignment(xSpreadsheet,
											startColumn,startColumn,row,row,2,1);

									row++;
									blnSelfCommentExists = true;
								}
							}

							if (selfComments.size() > 0) {
								row++;
							}
						} else {
							start = 0;
							row++;
						}
						row++;
					}

					endBorder = row - 1;

					/*
					 * Change(s) : Added codes to check height of the table to
					 * be added, and insert pagebreak if necessary Reason(s) :
					 * Fix the problem of a table being spilt into between two
					 * pages. Updated By: Alvis Updated On: 07 Aug 2009
					 */

					// Check height and insert pagebreak where necessary
					int pageHeightLimit = 22272;// Page limit is 22272
					int tableHeight = 0;

					// calculate the height of the table that is being added.
					for (int i1 = startBorder; i1 <= endBorder; i1++) {
						int rowToCalculate = i1;

						tableHeight += OO.getRowHeight(xSpreadsheet,
								rowToCalculate,startColumn);
					}
					currentPageHeight = currentPageHeight + tableHeight; // add
																			// new
																			// table
																			// height
																			// to
																			// current
																			// pageheight.
					int dis = 2; // Denise 08/01/2009 to move the table two
									// lines down
					if (currentPageHeight > pageHeightLimit) {// adding the
																// table will
																// exceed a
																// single page,
																// insert page
																// break
						OO.insertPageBreak(xSpreadsheet,startColumn,endColumn,
								startBorder);
						OO.insertRows(xSpreadsheet,startColumn,endColumn,
								startBorder,startBorder + dis,dis,1);
						row += dis;
						startBorder += dis;
						endBorder += dis;
						currentPageHeight = tableHeight;
						statementPos += dis;
					}
					// Denise 08/01/2009 insert competency description
					OO.insertString(xSpreadsheet,Integer.toString(count) + ".",
							statementPos,column);
					// Added translation to the competency name, Chun Yeong 1
					// Aug 2011
					OO.insertString(xSpreadsheet,UnicodeHelper
							.getUnicodeStringAmp(getTranslatedCompetency(
									statement).elementAt(0).toString()),
							statementPos,column + 1);
					OO.setFontBold(xSpreadsheet,startColumn,endColumn,
							statementPos,statementPos);
					OO.setBGColor(xSpreadsheet,startColumn,endColumn,
							statementPos,statementPos,BGCOLOR);
					OO.setCellAllignment(xSpreadsheet,startColumn,endColumn,
							statementPos,statementPos,2,1); // Set alignment
															// of competency
															// name to top,
															// Desmond 16
															// Nov 09

					OO.setTableBorder(xSpreadsheet,startColumn,endColumn,
							startBorder,endBorder,false,false,true,true,true,
							true);

					startBorder = row;
				}// end for-loop v.comp.size()
			}// end for-llop vClust.size()
				// ----------------- END COMPETENCY LEVEL SURVEY
				// ----------------- //

		} else {

			// ----------------- START KB LEVEL SURVEY ----------------- //

			int count = 0;
			int check = 0;
			int checkKB = 0;
			int lastKB = 0;

			startBorder = row;
			startBorder1 = row;
			for (int l = 0; l < vClust.size(); l++) {
				voCluster voClust = (voCluster) vClust.elementAt(l);
				int clusterID = voClust.getClusterID();
				String clusterName = voClust.getClusterName();
				OO.insertString(xSpreadsheet,clusterName,row,column);
				OO.setFontBold(xSpreadsheet,startColumn,endColumn,row,row);
				OO.setBGColor(xSpreadsheet,startColumn,endColumn,row,row,
						BGCOLORCLUSTER);
				OO.setCellAllignment(xSpreadsheet,startColumn,endColumn,row,
						row,2,1); // Set alignment of competency name to top,
									// Desmond 16 Nov 09
				row++;

				vComp = ClusterCompListHaveComments(clusterID);
				count = 0;
				check = 0;
				checkKB = 0;
				lastKB = 0;
				for (int i = 0; i < vComp.size(); i++) {
					check++;
					checkKB = 1;
					voCompetency voComp = (voCompetency) vComp.elementAt(i);
					count++;
					int compID = voComp.getCompetencyID();
					String statement = voComp.getCompetencyName();

					OO.insertString(xSpreadsheet,Integer.toString(count) + ".",
							row,column);
					// Added translation for the competency name, Chun Yeong 1
					// Aug 2011
					OO.insertString(xSpreadsheet,UnicodeHelper
							.getUnicodeStringAmp(getTranslatedCompetency(
									statement).elementAt(0).toString()),row,
							column + 1);
					OO.setFontBold(xSpreadsheet,startColumn,endColumn,row,row);
					OO.setBGColor(xSpreadsheet,startColumn,endColumn,row,row,
							BGCOLOR);
					OO.setCellAllignment(xSpreadsheet,startColumn,endColumn,
							row,row,2,1); // Set alignment of competency name
											// to top, Desmond 16 Nov 09

					row++;

					Vector KBList = KBListHaveComments(compID);

					for (int j = 0; j < KBList.size(); j++) {
						checkKB++;
						voKeyBehaviour voKB = (voKeyBehaviour) KBList
								.elementAt(j);
						int KBID = voKB.getKeyBehaviourID();
						String KB = voKB.getKeyBehaviour();

						OO.insertString(xSpreadsheet,"KB:",row,column); // Change
																		// from
																		// "-"
																		// to
																		// "KB:"
																		// to
																		// indicate
																		// each
																		// KB
																		// statement,
																		// Desmond
																		// 17
																		// Nov
																		// 09
						// Added translation for the key behaviour name, Chun
						// Yeong 1 Aug 2011
						OO.insertString(xSpreadsheet,
								getTranslatedKeyBehavior(UnicodeHelper
										.getUnicodeStringAmp(KB)),row,
								column + 1);
						OO.setFontBold(xSpreadsheet,column,column + 1,row,row); // Bold
																				// KB
																				// Statements,
																				// Desmond
																				// 17
																				// Nov
																				// 09
						OO.mergeCells(xSpreadsheet,column + 1,endColumn,row,row);
						OO.setRowHeight(xSpreadsheet,row,column + 1,ROWHEIGHT
								* OO.countTotalRow(KB,85));
						OO.setCellAllignment(xSpreadsheet,startColumn,
								startColumn,row,row,2,1);

						Vector supComments = getComments("SUP%",compID,KBID);

						// Added variables to store comments from peers and
						// subordinates, Desmond 18 Nov 09
						Vector othComments = getComments("OTH%",compID,KBID);
						Vector peerComments = getComments("PEER%",compID,KBID);
						Vector subComments = getComments("SUB%",compID,KBID);
						Vector addComments = getComments("ADD%",compID,KBID);

						int start = 0;
						row++;

						if (blnSupIncluded) {// added by Ping Yang on 11/08/08,
												// check raters assigned
							boolean blnSupCommentExist = false;// Added by ping
																// yang on
																// 31/7/08 to
																// get rid of
																// extra '-'s

							for (int k = 0; k < supComments.size(); k++) {
								String[] arr = (String[]) supComments
										.elementAt(k);

								String comment = arr[1];

								if (start == 0) {
									OO.insertString(xSpreadsheet,trans.tslt(
											templateLanguage,"Superior(s)"),
											row,column + 1); // Change from
																// Supervisors
																// to Superior,
																// Desmond 22
																// Oct 09
									OO.setFontBold(xSpreadsheet,startColumn,
											endColumn,row,row);
									OO.setFontItalic(xSpreadsheet,startColumn,
											endColumn,row,row);

									row++;
									start++;
								}

								if (!comment.trim().equals("")) {// Added by
																	// ping yang
																	// on
																	// 31/7/08
																	// to get
																	// rid of
																	// extra
																	// '-'s
									OO.insertString(
											xSpreadsheet,
											"- "
													+ UnicodeHelper
															.getUnicodeStringAmp(comment),
											row,column + 1);
									OO.mergeCells(xSpreadsheet,column + 1,
											endColumn,row,row);
									OO.setRowHeight(
											xSpreadsheet,
											row,
											column + 1,
											ROWHEIGHT
													* OO.countTotalRow(comment,
															85));
									OO.setCellAllignment(xSpreadsheet,
											startColumn,endColumn,row,row,2,1);
									row++;
									blnSupCommentExist = true;
								}

							}// end while sup comments

							start = 0;

							if (supComments.size() > 0) {
								row++;
							}
						}// end if(blnSupIncluded)

						// Execute this section of codes only if there are
						// Peers', Subordinates' or Others' comments
						if (blnOthIncluded) {// added by Ping Yang on 11/08/08,
												// check raters assigned

							/*
							 * Change: determine whether to show Others'comments
							 * based on splitOthers Updated by: Qiao Li 23 dec
							 * 2009
							 */
							if (splitOthers == 1) {
								boolean blnPeerCommentExist = false; // Added by
								// ping yang
								// on
								// 31/7/08
								// to get
								// rid of
								// extra
								// '-'s
								for (int k = 0; k < peerComments.size(); k++) {
									String[] arr = (String[]) peerComments
											.elementAt(k);
									String comment = arr[1];

									// Added codes to insert peers' comments,
									// Desmond 18 Nov 09
									// Insert Peers' comments
									if (start == 0) {
										// Changed the default language to
										// English by Chun Yeong 9 Jun 2011
										// Commented away to allow translation
										// below, Chun Yeong 1 Aug 2011
										/*
										 * if (ST.LangVer == 2) //Indonesian
										 * OO.insertString(xSpreadsheet,
										 * "Lainnya", row, column + 1); else
										 * //if (ST.LangVer == 1) English
										 * OO.insertString(xSpreadsheet,
										 * "Peer(s)", row, column + 1);
										 */

										// Allow dynamic translation, Chun Yeong
										// 1 Aug 2011
										OO.insertString(xSpreadsheet,trans
												.tslt(templateLanguage,
														"Peer(s)"),row,
												column + 1);
										OO.setFontBold(xSpreadsheet,
												startColumn,endColumn,row,row);
										OO.setFontItalic(xSpreadsheet,
												startColumn,endColumn,row,row);

										start++;
										row++;
									}

									if (!comment.trim().equals("")) {
										OO.insertString(
												xSpreadsheet,
												"- "
														+ UnicodeHelper
																.getUnicodeStringAmp(comment),
												row,column + 1);
										OO.mergeCells(xSpreadsheet,column + 1,
												endColumn,row,row);
										OO.setRowHeight(
												xSpreadsheet,
												row,
												column + 1,
												ROWHEIGHT
														* OO.countTotalRow(
																comment,85));
										OO.setCellAllignment(xSpreadsheet,
												startColumn,endColumn,row,row,
												2,1);

										row++;
										blnPeerCommentExist = true;
									}
								}

								// Adjust counters
								start = 0;

								if (peerComments.size() > 0) {
									row++;
								}

								boolean blnSubCommentExist = false; // Added by
																	// ping
								// yang on
								// 31/7/08 to
								// get rid of
								// extra '-'s
								for (int k = 0; k < subComments.size(); k++) {
									String[] arr = (String[]) subComments
											.elementAt(k);
									String comment = arr[1];

									// Added codes to insert subordinates'
									// comments,
									// Desmond 18 Nov 09
									// Insert Subordinates' comments
									if (start == 0) {
										// Changed the default language to
										// English by Chun Yeong 9 Jun 2011
										// Commented away to allow translation
										// below, Chun Yeong 1 Aug 2011
										/*
										 * if (ST.LangVer == 2) //Indonesian
										 * OO.insertString(xSpreadsheet,
										 * "Lainnya", row, column + 1); else
										 * //if (ST.LangVer == 1) English
										 * OO.insertString(xSpreadsheet,
										 * "Subordinate(s)", row, column + 1);
										 */

										// Allow dynamic translation, Chun Yeong
										// 1 Aug 2011
										OO.insertString(xSpreadsheet,trans
												.tslt(templateLanguage,
														"Subordinate(s)"),row,
												column + 1);
										OO.setFontBold(xSpreadsheet,
												startColumn,endColumn,row,row);
										OO.setFontItalic(xSpreadsheet,
												startColumn,endColumn,row,row);

										start++;
										row++;
									}

									if (!comment.trim().equals("")) {
										OO.insertString(
												xSpreadsheet,
												"- "
														+ UnicodeHelper
																.getUnicodeStringAmp(comment),
												row,column + 1);
										OO.mergeCells(xSpreadsheet,column + 1,
												endColumn,row,row);
										OO.setRowHeight(
												xSpreadsheet,
												row,
												column + 1,
												ROWHEIGHT
														* OO.countTotalRow(
																comment,85));
										OO.setCellAllignment(xSpreadsheet,
												startColumn,endColumn,row,row,
												2,1);

										row++;
										blnSubCommentExist = true;
									}
								}

								if (subComments.size() > 0) {
									row++;
								}
								// Adjust counters
								start = 0;

								boolean blnAddCommentExist = false;
								for (int k = 0; k < addComments.size(); k++) {
									String[] arr = (String[]) addComments
											.elementAt(k);
									String comment = arr[1];

									// Added codes to insert additional raters'
									// comments,
									if (start == 0) {
										// Allow dynamic translation, Chun Yeong
										// 1 Aug 2011
										OO.insertString(xSpreadsheet,trans
												.tslt(templateLanguage,
														"Additional Rater(s)"),
												row,column + 1);
										OO.setFontBold(xSpreadsheet,
												startColumn,endColumn,row,row);
										OO.setFontItalic(xSpreadsheet,
												startColumn,endColumn,row,row);
										start++;
										row++;
									}

									if (!comment.trim().equals("")) {
										OO.insertString(
												xSpreadsheet,
												"- "
														+ UnicodeHelper
																.getUnicodeStringAmp(comment),
												row,column + 1);
										OO.mergeCells(xSpreadsheet,column + 1,
												endColumn,row,row);
										OO.setRowHeight(
												xSpreadsheet,
												row,
												column + 1,
												ROWHEIGHT
														* OO.countTotalRow(
																comment,85));
										OO.setCellAllignment(xSpreadsheet,
												startColumn,endColumn,row,row,
												2,1);

										row++;
										blnAddCommentExist = true;
									}
								}

								if (addComments.size() > 0) {
									row++;
								}
								// Adjust counters
								start = 0;
							}
							// Added codes so that Others' comments (including
							// the header "Others") is displayed only if at
							// least Others' comment exists, Desmond, 18 Nov 09
							// Execute this portion of codes only if there are
							// Others' comments, if not don't even print out the
							// header "Others"
							/*
							 * Change: determine whether to show Others'comments
							 * based on splitOthers Updated by: Qiao Li 23 dec
							 * 2009
							 */
							else {
								boolean blnOthCommentExist = false; // Added by
																	// ping yang
																	// on
																	// 31/7/08
																	// to get
																	// rid of
																	// extra
																	// '-'s
								for (int k = 0; k < othComments.size(); k++) {
									String[] arr = (String[]) othComments
											.elementAt(k);
									String comment = arr[1];

									// Insert Others' comments
									if (start == 0) {
										// Changed the default language to
										// English by Chun Yeong 9 Jun 2011
										// Commented away to allow translation
										// below, Chun Yeong 1 Aug 2011
										/*
										 * if (ST.LangVer == 2) //Indonesian
										 * OO.insertString(xSpreadsheet,
										 * "Orang lain", row, column+1); else
										 * //if (ST.LangVer == 1) English
										 * OO.insertString(xSpreadsheet,
										 * "Others", row, column+1);
										 */

										// Allow dynamic translation, Chun Yeong
										// 1 Aug 2011
										OO.insertString(xSpreadsheet,
												trans.tslt(templateLanguage,
														"Others"),row,
												column + 1);
										OO.setFontBold(xSpreadsheet,
												startColumn,endColumn,row,row);
										OO.setFontItalic(xSpreadsheet,
												startColumn,endColumn,row,row);

										start++;
										row++;
									}

									if (!comment.trim().equals("")) {
										OO.insertString(
												xSpreadsheet,
												"- "
														+ UnicodeHelper
																.getUnicodeStringAmp(comment),
												row,column + 1);
										OO.mergeCells(xSpreadsheet,column + 1,
												endColumn,row,row);
										OO.setRowHeight(
												xSpreadsheet,
												row,
												column + 1,
												ROWHEIGHT
														* OO.countTotalRow(
																comment,85));
										OO.setCellAllignment(xSpreadsheet,
												startColumn,endColumn,row,row,
												2,1);

										row++;
										blnOthCommentExist = true;
									}
								}

								if (othComments.size() > 0) {
									row++;
								}
								start = 0;

							} // if(!othComments.isEmpty())
						}// end if(blnOthIncluded)

						if (selfIncluded == 1) {
							Vector selfComments = getComments("SELF",compID,
									KBID);

							boolean blnSelfCommentExist = false;// Added by ping
																// yang on
																// 31/7/08 to
																// get rid of
																// extra '-'s

							for (int k = 0; k < selfComments.size(); k++) {
								String[] arr = (String[]) selfComments
										.elementAt(k);
								String comment = arr[1];

								if (start == 0) {
									// Changed the default language to English
									// by Chun Yeong 9 Jun 2011
									// Commented away to allow translation
									// below, Chun Yeong 1 Aug 2011
									/*
									 * if (ST.LangVer == 2) //Indonesian
									 * OO.insertString(xSpreadsheet,
									 * "Diri Sendiri", row, column+1); else //
									 * if(ST.LangVer == 1) English
									 * OO.insertString(xSpreadsheet, "Self",
									 * row, column+1);
									 */

									// Allow dynamic translation, Chun Yeong 1
									// Aug 2011
									OO.insertString(
											xSpreadsheet,
											trans.tslt(templateLanguage,"Self"),
											row,column + 1);
									OO.setFontBold(xSpreadsheet,startColumn,
											endColumn,row,row);
									OO.setFontItalic(xSpreadsheet,startColumn,
											endColumn,row,row);

									row++;
									start++;
								}

								if (!comment.trim().equals("")) {// Added by
																	// ping yang
																	// on
																	// 31/7/08
																	// to get
																	// rid of
																	// extra
																	// '-'s
									OO.insertString(
											xSpreadsheet,
											"- "
													+ UnicodeHelper
															.getUnicodeStringAmp(comment),
											row,column + 1);
									OO.mergeCells(xSpreadsheet,column + 1,
											endColumn,row,row);
									OO.setRowHeight(
											xSpreadsheet,
											row,
											column + 1,
											ROWHEIGHT
													* OO.countTotalRow(comment,
															85));
									OO.setCellAllignment(xSpreadsheet,
											startColumn,endColumn,row,row,2,1); // Corrected
																				// the
																				// column
																				// range
																				// of
																				// cells
																				// that
																				// alignment
																				// is
																				// applied
																				// for
																				// self
																				// comments,
																				// Desmond
																				// 18
																				// Nov
																				// 09
									row++;
									blnSelfCommentExist = true;// Added by ping
																// yang on
																// 31/7/08 to
																// get rid of
																// extra '-'s
								}
							}

							if (selfComments.size() > 0) {
								row++;
							}
						}
						/*
						 * Change(s) : Give border and page break for KB
						 * Reason(s) : geting row for previous KB not this KB
						 * Updated By: Johanes Updated On: 02 Nov 2009
						 */
						if (endBorder == 1)
							endBorder1 = row;
						else
							endBorder1 = endBorder;
						endBorder = row;
						row++;

						// Check height and insert pagebreak where necessary
						int pageHeightLimit = 22272;// Page limit is 22272
						int tableHeight = 0;

						// calculate the height of the table that is being
						// added.
						for (int i1 = startBorder; i1 <= endBorder; i1++) {
							int rowToCalculate = i1;

							tableHeight += OO.getRowHeight(xSpreadsheet,
									rowToCalculate,startColumn);
						}
						currentPageHeight = currentPageHeight + tableHeight; // add
																				// new
																				// table
																				// height
																				// to
																				// current
																				// pageheight.
						// Denise 08/01/2009 to move the table two lines down
						int dis = 2;
						if (currentPageHeight > pageHeightLimit) {// adding the
																	// table
																	// will
																	// exceed a
																	// single
																	// page,
																	// insert
																	// page
																	// break
							OO.insertRows(xSpreadsheet,startColumn,endColumn,
									startBorder,startBorder + dis,dis,0);
							OO.setTableBorder(xSpreadsheet,startColumn,
									endColumn,startBorder1,endBorder1,false,
									false,true,true,true,true);
							OO.insertPageBreak(xSpreadsheet,startColumn,
									endColumn,startBorder);
							endBorder += dis;
							// OO.insertRow(xSpreadsheet, startColumn,
							// endColumn, 2);
							row += dis;
							startBorder += dis;
							startBorder1 = startBorder;
							lastKB = checkKB;
							currentPageHeight = tableHeight;
						}
						startBorder = row;

					} // kb

					/*
					 * Change(s) : Give border and page break for KB Reason(s) :
					 * give border for last page, without this code last page
					 * never have border Updated By: Johanes Updated On: 02 Nov
					 * 2009
					 */
					/*
					 * Change: remove lastKB != KBList.size() Reason: Sometimes
					 * the borders are not added when lastKB == KBList.size()
					 * Updated By: Qiao Li 29 Dec 2009
					 */
					if (check == vComp.size() /* && lastKB != KBList.size() */) {
						OO.setTableBorder(xSpreadsheet,startColumn,endColumn,
								startBorder1,endBorder,false,false,true,true,
								true,true);
					}
				}
			}

		}

		// ----------------- END KB LEVEL SURVEY ----------------- //

	} // End of InsertComments()

	public void InsertComments() throws SQLException, IOException, Exception {
		// Added by Alvis 07-Aug-09 for pagebreak fix implemented below
		int currentPageHeight = 1076;// 1076 is to accomodate the first two
										// cells, which contains the title.
		Vector vComp = CompListHaveComments();
		row++;

		int surveyLevel = Integer.parseInt(surveyInfo[0]);
		column = 0;

		// Allow dynamic translation, Chun Yeong 1 Aug 2011
		OO.insertString(xSpreadsheet,
				trans.tslt(templateLanguage,"Narrative Comments"),row,column);
		OO.setFontBold(xSpreadsheet,startColumn,endColumn,row,row);

		row += 2;

		if (vComp.size() == 0) {
			OO.insertString(xSpreadsheet,"Nil",row,column);
		}
		row++;

		int startBorder1 = 1;
		int startBorder = 1;
		int endBorder = 1;
		int endBorder1 = 1;
		int selfIncluded = Q.SelfCommentIncluded(surveyID);
		int column = 0;

		// added by Ping Yang on 11/08/08, check raters assigned
		boolean blnSupIncluded = Q.SupCommentIncluded(surveyID,targetID);
		boolean blnOthIncluded = Q.OthCommentIncluded(surveyID,targetID);

		if (surveyLevel == 0) {

			// ----------------- START COMPETENCY LEVEL SURVEY -----------------
			// //

			int count = 0;

			startBorder = row;
			for (int i = 0; i < vComp.size(); i++) {
				voCompetency voComp = (voCompetency) vComp.elementAt(i);

				int compID = voComp.getCompetencyID();
				String statement = voComp.getCompetencyName();

				count++;

				int statementPos = row; // Denise 08/01/2009 store position to
										// insert competency description

				int KBID = 0;
				int start = 0;
				row++;

				// Added by Ha 23/06/08 reset the value start to print the
				// header of comment correctly
				start = 0;
				Vector supComments = getComments("SUP%",compID,KBID);

				// Added variables to store comments from peers and
				// subordinates, Desmond 18 Nov 09
				Vector othComments = getComments("OTH%",compID,KBID);
				Vector peerComments = getComments("PEER%",compID,KBID);
				Vector subComments = getComments("SUB%",compID,KBID);
				Vector addComments = getComments("ADD%",compID,KBID); // get
																		// comments
																		// for
																		// additional
																		// raters

				if (blnSupIncluded) {// added by Ping Yang on 11/08/08, check
										// raters assigned
					boolean blnSupCommentExists = false;// Added by ping yang on
														// 31/7/08 to get rid of
														// extra '-'s

					for (int j = 0; j < supComments.size(); j++) {
						String[] arr = (String[]) supComments.elementAt(j);
						if (start == 0) {

							OO.insertString(xSpreadsheet,
									trans.tslt(templateLanguage,"Superior(s)"),
									row,column + 1); // Change from Supervisors
														// to Superior, Desmond
														// 22 Oct 09
							OO.setFontBold(xSpreadsheet,startColumn,endColumn,
									row,row);
							OO.setFontItalic(xSpreadsheet,startColumn,
									endColumn,row,row);

							row++;
							start++;
						}

						String comment = arr[1];
						if (!comment.trim().equals("")) {// Added by ping yang
															// on 31/7/08 to get
															// rid of extra '-'s
							OO.insertString(
									xSpreadsheet,
									"- "
											+ UnicodeHelper
													.getUnicodeStringAmp(comment),
									row,column + 1);
							OO.mergeCells(xSpreadsheet,column + 1,endColumn,
									row,row);
							OO.setRowHeight(xSpreadsheet,row,column + 1,
									ROWHEIGHT * OO.countTotalRow(comment,85));
							OO.setCellAllignment(xSpreadsheet,startColumn,
									startColumn,row,row,2,1);
							row++;
							blnSupCommentExists = true;
						}

					}

					start = 0;

					/*
					 * Change(s) : Added codes to point to next row below if
					 * exist rater code comments. Remove codes that add default
					 * empty comment in the report if rater code have no
					 * comments Reason(s) : To remove empty narrative comments
					 * by rater category, KB then Competency. i.e If competency
					 * has no comments from raters, remove the entire competency
					 * in the narrative comments. Updated By: Sebastian Updated
					 * On: 19 July 2010
					 */
					if (supComments.size() > 0) {
						row++;
					}
				}// end if(blnSupIncluded)

				if (blnOthIncluded) {// added by Ping Yang on 11/08/08, check
										// raters assigned
					/*
					 * Change: determine whether to show Others'comments based
					 * on splitOthers Updated by: Qiao Li 23 dec 2009
					 */
					if (splitOthers == 1) {
						// Added codes to insert peers' comments, Desmond 23 Nov
						// 09
						// Insert Peers' comments

						boolean blnPeerCommentExist = false; // Added to get rid
																// of extra '-'s
						for (int k = 0; k < peerComments.size(); k++) {
							String[] arr = (String[]) peerComments.elementAt(k);
							String comment = arr[1];

							if (start == 0) {
								// Changed the default language to English by
								// Chun Yeong 9 Jun 2011
								// Commented away to allow translation below,
								// Chun Yeong 1 Aug 2011
								/*
								 * if (ST.LangVer == 2) //Indonesian
								 * OO.insertString(xSpreadsheet, "Lainnya", row,
								 * column + 1); else // if (ST.LangVer == 1)
								 * English OO.insertString(xSpreadsheet,
								 * "Peer(s)", row, column + 1);
								 */

								// Allow dynamic translation, Chun Yeong 1 Aug
								// 2011
								OO.insertString(xSpreadsheet,
										trans.tslt(templateLanguage,"Peer(s)"),
										row,column + 1);
								OO.setFontBold(xSpreadsheet,startColumn,
										endColumn,row,row);
								OO.setFontItalic(xSpreadsheet,startColumn,
										endColumn,row,row);

								start++;
								row++;
							}

							if (!comment.trim().equals("")) {
								OO.insertString(
										xSpreadsheet,
										"- "
												+ UnicodeHelper
														.getUnicodeStringAmp(comment),
										row,column + 1);
								OO.mergeCells(xSpreadsheet,column + 1,
										endColumn,row,row);
								OO.setRowHeight(
										xSpreadsheet,
										row,
										column + 1,
										ROWHEIGHT
												* OO.countTotalRow(comment,85));
								OO.setCellAllignment(xSpreadsheet,startColumn,
										endColumn,row,row,2,1);

								row++;
								blnPeerCommentExist = true;
							}
						}

						// Adjust counters
						start = 0;

						if (peerComments.size() > 0) {
							row++;
						}

						// Added codes to insert subordinates' comments, Desmond
						// 23 Nov 09
						// Insert Subordinates' comments
						boolean blnSubCommentExist = false; // Added to get rid
															// of extra '-'s
						for (int k = 0; k < subComments.size(); k++) {
							String[] arr = (String[]) subComments.elementAt(k);
							String comment = arr[1];

							if (start == 0) {
								// Changed the default language to English by
								// Chun Yeong 9 Jun 2011
								// Commented away to allow translation below,
								// Chun Yeong 1 Aug 2011
								/*
								 * if (ST.LangVer == 2) //Indonesian
								 * OO.insertString(xSpreadsheet, "Lainnya", row,
								 * column + 1); else //if (ST.LangVer == 1)
								 * English OO.insertString(xSpreadsheet,
								 * "Subordinate(s)", row, column + 1);
								 */

								// Allow dynamic translation, Chun Yeong 1 Aug
								// 2011
								OO.insertString(xSpreadsheet,trans.tslt(
										templateLanguage,"Subordinate(s)"),row,
										column + 1);
								OO.setFontBold(xSpreadsheet,startColumn,
										endColumn,row,row);
								OO.setFontItalic(xSpreadsheet,startColumn,
										endColumn,row,row);

								start++;
								row++;
							}

							if (!comment.trim().equals("")) {
								OO.insertString(
										xSpreadsheet,
										"- "
												+ UnicodeHelper
														.getUnicodeStringAmp(comment),
										row,column + 1);
								OO.mergeCells(xSpreadsheet,column + 1,
										endColumn,row,row);
								OO.setRowHeight(
										xSpreadsheet,
										row,
										column + 1,
										ROWHEIGHT
												* OO.countTotalRow(comment,85));
								OO.setCellAllignment(xSpreadsheet,startColumn,
										endColumn,row,row,2,1);

								row++;
								blnSubCommentExist = true;
							}
						}

						// Adjust counters
						start = 0;

						if (subComments.size() > 0) {
							row++;
						}

						// Added codes to insert additional raters comments ~
						// Albert 5 Sept 2012
						boolean blnAddCommentExist = false; // Added to get rid
															// of extra '-'s
						for (int k = 0; k < addComments.size(); k++) {
							String[] arr = (String[]) addComments.elementAt(k);
							String comment = arr[1];

							if (start == 0) {
								// Allow dynamic translation, Chun Yeong 1 Aug
								// 2011
								OO.insertString(xSpreadsheet,
										trans.tslt(templateLanguage,
												"Additional Rater(s)"),row,
										column + 1);
								OO.setFontBold(xSpreadsheet,startColumn,
										endColumn,row,row);
								OO.setFontItalic(xSpreadsheet,startColumn,
										endColumn,row,row);

								start++;
								row++;
							}

							if (!comment.trim().equals("")) {
								OO.insertString(
										xSpreadsheet,
										"- "
												+ UnicodeHelper
														.getUnicodeStringAmp(comment),
										row,column + 1);
								OO.mergeCells(xSpreadsheet,column + 1,
										endColumn,row,row);
								OO.setRowHeight(
										xSpreadsheet,
										row,
										column + 1,
										ROWHEIGHT
												* OO.countTotalRow(comment,85));
								OO.setCellAllignment(xSpreadsheet,startColumn,
										endColumn,row,row,2,1);

								row++;
								blnAddCommentExist = true;
							}
						}

						// Adjust counters
						start = 0;

						if (addComments.size() > 0) {
							row++;
						}
					}

					// Added codes so that Others' comments (including the
					// header "Others") is displayed only if at least one
					// Others' comment exists, Desmond, 18 Nov 09
					// Execute this portion of codes only if there are Others'
					// comments, if not don't even print out the header "Others"
					/*
					 * Change: determine whether to show Others'comments based
					 * on splitOthers Updated by: Qiao Li 23 dec 2009
					 */
					else {
						// Insert Others' comments

						// Added both peers and subordinate comments together
						// with other's comments to append them all by Chun
						// Yeong 1 Jul 2011
						othComments.addAll(peerComments);
						othComments.addAll(subComments);
						othComments.addAll(addComments);

						boolean blnOthCommentExists = false;// Added to get rid
															// of extra '-'s

						for (int j = 0; j < othComments.size(); j++) {
							String[] arr = (String[]) othComments.elementAt(j);
							String comment = arr[1];

							if (start == 0) {
								// Changed the default language to English by
								// Chun Yeong 9 Jun 2011
								// Commented away to allow translation below,
								// Chun Yeong 1 Aug 2011
								/*
								 * if (ST.LangVer == 2) //Indonesian
								 * OO.insertString(xSpreadsheet, "Orang lain",
								 * row, column+1); else //if (ST.LangVer == 1)
								 * English OO.insertString(xSpreadsheet,
								 * "Others", row, column+1);
								 */

								// Allow dynamic translation, Chun Yeong 1 Aug
								// 2011
								OO.insertString(xSpreadsheet,
										trans.tslt(templateLanguage,"Others"),
										row,column + 1);
								OO.setFontBold(xSpreadsheet,startColumn,
										endColumn,row,row);
								OO.setFontItalic(xSpreadsheet,startColumn,
										endColumn,row,row);

								start++;
								row++;
							}

							// Added to get rid of extra '-'s
							if (!comment.trim().equals("")) {
								OO.insertString(
										xSpreadsheet,
										"- "
												+ UnicodeHelper
														.getUnicodeStringAmp(comment),
										row,column + 1);
								OO.mergeCells(xSpreadsheet,column + 1,
										endColumn,row,row);
								OO.setRowHeight(
										xSpreadsheet,
										row,
										column + 1,
										ROWHEIGHT
												* OO.countTotalRow(comment,85));
								OO.setCellAllignment(xSpreadsheet,startColumn,
										startColumn,row,row,2,1);
								row++;
								blnOthCommentExists = true;
							}
						}

						start = 0;

						if (othComments.size() > 0) {
							row++;
						}
					} // end if(!othComments.isEmpty())
				}// end if(blnOthIncluded)

				// changed the order of comments by moving "Self" to the back
				// Qiao Li 23 Dec 2009
				if (selfIncluded == 1) {
					Vector selfComments = getComments("SELF",compID,KBID);

					if (selfComments != null) {
						boolean blnSelfCommentExists = false;// Added by ping
																// yang on
																// 31/7/08 to
																// get rid of
																// extra '-'s
						for (int j = 0; j < selfComments.size(); j++) {
							String[] arr = (String[]) selfComments.elementAt(j);
							if (start == 0) {
								// Changed the default language to English by
								// Chun Yeong 9 Jun 2011
								// Commented away to allow translation below,
								// Chun Yeong 1 Aug 2011
								/*
								 * if (ST.LangVer == 2)
								 * OO.insertString(xSpreadsheet, "Diri Sendiri",
								 * row, column+1); else //if (ST.LangVer == 1)
								 * OO.insertString(xSpreadsheet, "Self", row,
								 * column+1);
								 */

								// Allow dynamic translation, Chun Yeong 1 Aug
								// 2011
								OO.insertString(xSpreadsheet,
										trans.tslt(templateLanguage,"Self"),
										row,column + 1);
								OO.setFontBold(xSpreadsheet,startColumn,
										endColumn,row,row);
								OO.setFontItalic(xSpreadsheet,startColumn,
										endColumn,row,row);

								row++;
								start++;
							}

							String comment = arr[1];

							if (!comment.trim().equals("")) {// Added by ping
																// yang on
																// 31/7/08 to
																// get rid of
																// extra '-'s
								OO.insertString(
										xSpreadsheet,
										"- "
												+ UnicodeHelper
														.getUnicodeStringAmp(comment),
										row,column + 1);
								OO.mergeCells(xSpreadsheet,column + 1,
										endColumn,row,row);
								OO.setRowHeight(
										xSpreadsheet,
										row,
										column + 1,
										ROWHEIGHT
												* OO.countTotalRow(comment,85));
								OO.setCellAllignment(xSpreadsheet,startColumn,
										startColumn,row,row,2,1);

								row++;
								blnSelfCommentExists = true;
							}
						}

						if (selfComments.size() > 0) {
							row++;
						}
					} else {
						start = 0;
						row++;
					}
					row++;
				}

				endBorder = row;

				/*
				 * Change(s) : Added codes to check height of the table to be
				 * added, and insert pagebreak if necessary Reason(s) : Fix the
				 * problem of a table being spilt into between two pages.
				 * Updated By: Alvis Updated On: 07 Aug 2009
				 */

				// Check height and insert pagebreak where necessary
				int pageHeightLimit = 22272;// Page limit is 22272
				int tableHeight = 0;

				// calculate the height of the table that is being added.
				for (int i1 = startBorder; i1 <= endBorder; i1++) {
					int rowToCalculate = i1;

					tableHeight += OO.getRowHeight(xSpreadsheet,rowToCalculate,
							startColumn);
				}
				currentPageHeight = currentPageHeight + tableHeight; // add new
																		// table
																		// height
																		// to
																		// current
																		// pageheight.
				int dis = 2; // Denise 08/01/2009 to move the table two lines
								// down
				if (currentPageHeight > pageHeightLimit) {// adding the table
															// will exceed a
															// single page,
															// insert page break
					OO.insertPageBreak(xSpreadsheet,startColumn,endColumn,
							startBorder);
					OO.insertRows(xSpreadsheet,startColumn,endColumn,
							startBorder,startBorder + dis,dis,1);
					row += dis;
					startBorder += dis;
					endBorder += dis;
					currentPageHeight = tableHeight;
					statementPos += dis;
				}
				// Denise 08/01/2009 insert competency description
				OO.insertString(xSpreadsheet,Integer.toString(count) + ".",
						statementPos,column);
				// Added translation to the competency name, Chun Yeong 1 Aug
				// 2011
				OO.insertString(xSpreadsheet,UnicodeHelper
						.getUnicodeStringAmp(getTranslatedCompetency(statement)
								.elementAt(0).toString()),statementPos,
						column + 1);
				OO.setFontBold(xSpreadsheet,startColumn,endColumn,statementPos,
						statementPos);
				OO.setBGColor(xSpreadsheet,startColumn,endColumn,statementPos,
						statementPos,BGCOLOR);
				OO.setCellAllignment(xSpreadsheet,startColumn,endColumn,
						statementPos,statementPos,2,1); // Set alignment of
														// competency name
														// to top, Desmond
														// 16 Nov 09

				OO.setTableBorder(xSpreadsheet,startColumn,endColumn,
						startBorder,endBorder,false,false,true,true,true,true);

				startBorder = row;
			}// end for-loop v.comp.size()

			// ----------------- END COMPETENCY LEVEL SURVEY -----------------
			// //

		} else {

			// ----------------- START KB LEVEL SURVEY ----------------- //

			int count = 0;
			int check = 0;
			int checkKB = 0;
			int lastKB = 0;

			startBorder = row;
			startBorder1 = row;
			for (int i = 0; i < vComp.size(); i++) {
				check++;
				checkKB = 1;
				voCompetency voComp = (voCompetency) vComp.elementAt(i);
				count++;
				int compID = voComp.getCompetencyID();
				String statement = voComp.getCompetencyName();

				OO.insertString(xSpreadsheet,Integer.toString(count) + ".",row,
						column);
				// Added translation for the competency name, Chun Yeong 1 Aug
				// 2011
				OO.insertString(xSpreadsheet,UnicodeHelper
						.getUnicodeStringAmp(getTranslatedCompetency(statement)
								.elementAt(0).toString()),row,column + 1);
				OO.setFontBold(xSpreadsheet,startColumn,endColumn,row,row);
				OO.setBGColor(xSpreadsheet,startColumn,endColumn,row,row,
						BGCOLOR);
				OO.setCellAllignment(xSpreadsheet,startColumn,endColumn,row,
						row,2,1); // Set alignment of competency name to top,
									// Desmond 16 Nov 09

				row++;

				Vector KBList = KBListHaveComments(compID);

				for (int j = 0; j < KBList.size(); j++) {
					checkKB++;
					voKeyBehaviour voKB = (voKeyBehaviour) KBList.elementAt(j);
					int KBID = voKB.getKeyBehaviourID();
					String KB = voKB.getKeyBehaviour();

					OO.insertString(xSpreadsheet,"KB:",row,column); // Change
																	// from
																	// "-"
																	// to
																	// "KB:"
																	// to
																	// indicate
																	// each
																	// KB
																	// statement,
																	// Desmond
																	// 17
																	// Nov
																	// 09
					// Added translation for the key behaviour name, Chun Yeong
					// 1 Aug 2011
					OO.insertString(xSpreadsheet,
							getTranslatedKeyBehavior(UnicodeHelper
									.getUnicodeStringAmp(KB)),row,column + 1);
					OO.setFontBold(xSpreadsheet,column,column + 1,row,row); // Bold
																			// KB
																			// Statements,
																			// Desmond
																			// 17
																			// Nov
																			// 09
					OO.mergeCells(xSpreadsheet,column + 1,endColumn,row,row);
					OO.setRowHeight(xSpreadsheet,row,column + 1,
							ROWHEIGHT * OO.countTotalRow(KB,85));
					OO.setCellAllignment(xSpreadsheet,startColumn,startColumn,
							row,row,2,1);

					Vector supComments = getComments("SUP%",compID,KBID);

					// Added variables to store comments from peers and
					// subordinates, Desmond 18 Nov 09
					Vector othComments = getComments("OTH%",compID,KBID);
					Vector peerComments = getComments("PEER%",compID,KBID);
					Vector subComments = getComments("SUB%",compID,KBID);
					Vector addComments = getComments("ADD%",compID,KBID);

					int start = 0;
					row++;

					if (blnSupIncluded) {// added by Ping Yang on 11/08/08,
											// check raters assigned
						boolean blnSupCommentExist = false;// Added by ping yang
															// on 31/7/08 to get
															// rid of extra '-'s

						for (int k = 0; k < supComments.size(); k++) {
							String[] arr = (String[]) supComments.elementAt(k);

							String comment = arr[1];

							if (start == 0) {
								OO.insertString(xSpreadsheet,trans.tslt(
										templateLanguage,"Superior(s)"),row,
										column + 1); // Change from Supervisors
														// to Superior, Desmond
														// 22 Oct 09
								OO.setFontBold(xSpreadsheet,startColumn,
										endColumn,row,row);
								OO.setFontItalic(xSpreadsheet,startColumn,
										endColumn,row,row);

								row++;
								start++;
							}

							if (!comment.trim().equals("")) {// Added by ping
																// yang on
																// 31/7/08 to
																// get rid of
																// extra '-'s
								OO.insertString(
										xSpreadsheet,
										"- "
												+ UnicodeHelper
														.getUnicodeStringAmp(comment),
										row,column + 1);
								OO.mergeCells(xSpreadsheet,column + 1,
										endColumn,row,row);
								OO.setRowHeight(
										xSpreadsheet,
										row,
										column + 1,
										ROWHEIGHT
												* OO.countTotalRow(comment,85));
								OO.setCellAllignment(xSpreadsheet,startColumn,
										endColumn,row,row,2,1);
								row++;
								blnSupCommentExist = true;
							}

						}// end while sup comments

						start = 0;

						if (supComments.size() > 0) {
							row++;
						}
					}// end if(blnSupIncluded)

					// Execute this section of codes only if there are Peers',
					// Subordinates' or Others' comments
					if (blnOthIncluded) {// added by Ping Yang on 11/08/08,
											// check raters assigned

						/*
						 * Change: determine whether to show Others'comments
						 * based on splitOthers Updated by: Qiao Li 23 dec 2009
						 */
						if (splitOthers == 1) {
							boolean blnPeerCommentExist = false; // Added by
							// ping yang
							// on
							// 31/7/08
							// to get
							// rid of
							// extra
							// '-'s
							for (int k = 0; k < peerComments.size(); k++) {
								String[] arr = (String[]) peerComments
										.elementAt(k);
								String comment = arr[1];

								// Added codes to insert peers' comments,
								// Desmond 18 Nov 09
								// Insert Peers' comments
								if (start == 0) {
									// Changed the default language to English
									// by Chun Yeong 9 Jun 2011
									// Commented away to allow translation
									// below, Chun Yeong 1 Aug 2011
									/*
									 * if (ST.LangVer == 2) //Indonesian
									 * OO.insertString(xSpreadsheet, "Lainnya",
									 * row, column + 1); else //if (ST.LangVer
									 * == 1) English
									 * OO.insertString(xSpreadsheet, "Peer(s)",
									 * row, column + 1);
									 */

									// Allow dynamic translation, Chun Yeong 1
									// Aug 2011
									OO.insertString(xSpreadsheet,trans.tslt(
											templateLanguage,"Peer(s)"),row,
											column + 1);
									OO.setFontBold(xSpreadsheet,startColumn,
											endColumn,row,row);
									OO.setFontItalic(xSpreadsheet,startColumn,
											endColumn,row,row);

									start++;
									row++;
								}

								if (!comment.trim().equals("")) {
									OO.insertString(
											xSpreadsheet,
											"- "
													+ UnicodeHelper
															.getUnicodeStringAmp(comment),
											row,column + 1);
									OO.mergeCells(xSpreadsheet,column + 1,
											endColumn,row,row);
									OO.setRowHeight(
											xSpreadsheet,
											row,
											column + 1,
											ROWHEIGHT
													* OO.countTotalRow(comment,
															85));
									OO.setCellAllignment(xSpreadsheet,
											startColumn,endColumn,row,row,2,1);

									row++;
									blnPeerCommentExist = true;
								}
							}

							// Adjust counters
							start = 0;

							if (peerComments.size() > 0) {
								row++;
							}

							boolean blnSubCommentExist = false; // Added by ping
							// yang on
							// 31/7/08 to
							// get rid of
							// extra '-'s
							for (int k = 0; k < subComments.size(); k++) {
								String[] arr = (String[]) subComments
										.elementAt(k);
								String comment = arr[1];

								// Added codes to insert subordinates' comments,
								// Desmond 18 Nov 09
								// Insert Subordinates' comments
								if (start == 0) {
									// Changed the default language to English
									// by Chun Yeong 9 Jun 2011
									// Commented away to allow translation
									// below, Chun Yeong 1 Aug 2011
									/*
									 * if (ST.LangVer == 2) //Indonesian
									 * OO.insertString(xSpreadsheet, "Lainnya",
									 * row, column + 1); else //if (ST.LangVer
									 * == 1) English
									 * OO.insertString(xSpreadsheet,
									 * "Subordinate(s)", row, column + 1);
									 */

									// Allow dynamic translation, Chun Yeong 1
									// Aug 2011
									OO.insertString(xSpreadsheet,trans.tslt(
											templateLanguage,"Subordinate(s)"),
											row,column + 1);
									OO.setFontBold(xSpreadsheet,startColumn,
											endColumn,row,row);
									OO.setFontItalic(xSpreadsheet,startColumn,
											endColumn,row,row);

									start++;
									row++;
								}

								if (!comment.trim().equals("")) {
									OO.insertString(
											xSpreadsheet,
											"- "
													+ UnicodeHelper
															.getUnicodeStringAmp(comment),
											row,column + 1);
									OO.mergeCells(xSpreadsheet,column + 1,
											endColumn,row,row);
									OO.setRowHeight(
											xSpreadsheet,
											row,
											column + 1,
											ROWHEIGHT
													* OO.countTotalRow(comment,
															85));
									OO.setCellAllignment(xSpreadsheet,
											startColumn,endColumn,row,row,2,1);

									row++;
									blnSubCommentExist = true;
								}
							}

							if (subComments.size() > 0) {
								row++;
							}
							// Adjust counters
							start = 0;

							boolean blnAddCommentExist = false;
							for (int k = 0; k < addComments.size(); k++) {
								String[] arr = (String[]) addComments
										.elementAt(k);
								String comment = arr[1];

								// Added codes to insert additional raters'
								// comments,
								if (start == 0) {
									// Allow dynamic translation, Chun Yeong 1
									// Aug 2011
									OO.insertString(xSpreadsheet,trans.tslt(
											templateLanguage,
											"Additional Rater(s)"),row,
											column + 1);
									OO.setFontBold(xSpreadsheet,startColumn,
											endColumn,row,row);
									OO.setFontItalic(xSpreadsheet,startColumn,
											endColumn,row,row);
									start++;
									row++;
								}

								if (!comment.trim().equals("")) {
									OO.insertString(
											xSpreadsheet,
											"- "
													+ UnicodeHelper
															.getUnicodeStringAmp(comment),
											row,column + 1);
									OO.mergeCells(xSpreadsheet,column + 1,
											endColumn,row,row);
									OO.setRowHeight(
											xSpreadsheet,
											row,
											column + 1,
											ROWHEIGHT
													* OO.countTotalRow(comment,
															85));
									OO.setCellAllignment(xSpreadsheet,
											startColumn,endColumn,row,row,2,1);

									row++;
									blnAddCommentExist = true;
								}
							}

							if (addComments.size() > 0) {
								row++;
							}
							// Adjust counters
							start = 0;
						}
						// Added codes so that Others' comments (including the
						// header "Others") is displayed only if at least
						// Others' comment exists, Desmond, 18 Nov 09
						// Execute this portion of codes only if there are
						// Others' comments, if not don't even print out the
						// header "Others"
						/*
						 * Change: determine whether to show Others'comments
						 * based on splitOthers Updated by: Qiao Li 23 dec 2009
						 */
						else {
							boolean blnOthCommentExist = false; // Added by ping
																// yang on
																// 31/7/08 to
																// get rid of
																// extra '-'s
							for (int k = 0; k < othComments.size(); k++) {
								String[] arr = (String[]) othComments
										.elementAt(k);
								String comment = arr[1];

								// Insert Others' comments
								if (start == 0) {
									// Changed the default language to English
									// by Chun Yeong 9 Jun 2011
									// Commented away to allow translation
									// below, Chun Yeong 1 Aug 2011
									/*
									 * if (ST.LangVer == 2) //Indonesian
									 * OO.insertString(xSpreadsheet,
									 * "Orang lain", row, column+1); else //if
									 * (ST.LangVer == 1) English
									 * OO.insertString(xSpreadsheet, "Others",
									 * row, column+1);
									 */

									// Allow dynamic translation, Chun Yeong 1
									// Aug 2011
									OO.insertString(xSpreadsheet,trans.tslt(
											templateLanguage,"Others"),row,
											column + 1);
									OO.setFontBold(xSpreadsheet,startColumn,
											endColumn,row,row);
									OO.setFontItalic(xSpreadsheet,startColumn,
											endColumn,row,row);

									start++;
									row++;
								}

								if (!comment.trim().equals("")) {
									OO.insertString(
											xSpreadsheet,
											"- "
													+ UnicodeHelper
															.getUnicodeStringAmp(comment),
											row,column + 1);
									OO.mergeCells(xSpreadsheet,column + 1,
											endColumn,row,row);
									OO.setRowHeight(
											xSpreadsheet,
											row,
											column + 1,
											ROWHEIGHT
													* OO.countTotalRow(comment,
															85));
									OO.setCellAllignment(xSpreadsheet,
											startColumn,endColumn,row,row,2,1);

									row++;
									blnOthCommentExist = true;
								}
							}

							if (othComments.size() > 0) {
								row++;
							}
							start = 0;

						} // if(!othComments.isEmpty())
					}// end if(blnOthIncluded)

					if (selfIncluded == 1) {
						Vector selfComments = getComments("SELF",compID,KBID);

						boolean blnSelfCommentExist = false;// Added by ping
															// yang on 31/7/08
															// to get rid of
															// extra '-'s

						for (int k = 0; k < selfComments.size(); k++) {
							String[] arr = (String[]) selfComments.elementAt(k);
							String comment = arr[1];

							if (start == 0) {
								// Changed the default language to English by
								// Chun Yeong 9 Jun 2011
								// Commented away to allow translation below,
								// Chun Yeong 1 Aug 2011
								/*
								 * if (ST.LangVer == 2) //Indonesian
								 * OO.insertString(xSpreadsheet, "Diri Sendiri",
								 * row, column+1); else // if(ST.LangVer == 1)
								 * English OO.insertString(xSpreadsheet, "Self",
								 * row, column+1);
								 */

								// Allow dynamic translation, Chun Yeong 1 Aug
								// 2011
								OO.insertString(xSpreadsheet,
										trans.tslt(templateLanguage,"Self"),
										row,column + 1);
								OO.setFontBold(xSpreadsheet,startColumn,
										endColumn,row,row);
								OO.setFontItalic(xSpreadsheet,startColumn,
										endColumn,row,row);

								row++;
								start++;
							}

							if (!comment.trim().equals("")) {// Added by ping
																// yang on
																// 31/7/08 to
																// get rid of
																// extra '-'s
								OO.insertString(
										xSpreadsheet,
										"- "
												+ UnicodeHelper
														.getUnicodeStringAmp(comment),
										row,column + 1);
								OO.mergeCells(xSpreadsheet,column + 1,
										endColumn,row,row);
								OO.setRowHeight(
										xSpreadsheet,
										row,
										column + 1,
										ROWHEIGHT
												* OO.countTotalRow(comment,85));
								OO.setCellAllignment(xSpreadsheet,startColumn,
										endColumn,row,row,2,1); // Corrected
																// the
																// column
																// range of
																// cells
																// that
																// alignment
																// is
																// applied
																// for self
																// comments,
																// Desmond
																// 18 Nov 09
								row++;
								blnSelfCommentExist = true;// Added by ping yang
															// on 31/7/08 to get
															// rid of extra '-'s
							}
						}

						if (selfComments.size() > 0) {
							row++;
						}
					}
					/*
					 * Change(s) : Give border and page break for KB Reason(s) :
					 * geting row for previous KB not this KB Updated By:
					 * Johanes Updated On: 02 Nov 2009
					 */
					if (endBorder == 1)
						endBorder1 = row;
					else
						endBorder1 = endBorder;
					endBorder = row;
					row++;

					// Check height and insert pagebreak where necessary
					int pageHeightLimit = 22272;// Page limit is 22272
					int tableHeight = 0;

					// calculate the height of the table that is being added.
					for (int i1 = startBorder; i1 <= endBorder; i1++) {
						int rowToCalculate = i1;

						tableHeight += OO.getRowHeight(xSpreadsheet,
								rowToCalculate,startColumn);
					}
					currentPageHeight = currentPageHeight + tableHeight; // add
																			// new
																			// table
																			// height
																			// to
																			// current
																			// pageheight.
					// Denise 08/01/2009 to move the table two lines down
					int dis = 2;
					if (currentPageHeight > pageHeightLimit) {// adding the
																// table will
																// exceed a
																// single page,
																// insert page
																// break
						OO.insertRows(xSpreadsheet,startColumn,endColumn,
								startBorder,startBorder + dis,dis,0);
						OO.setTableBorder(xSpreadsheet,startColumn,endColumn,
								startBorder1,endBorder1,false,false,true,true,
								true,true);
						OO.insertPageBreak(xSpreadsheet,startColumn,endColumn,
								startBorder);
						endBorder += dis;
						// OO.insertRow(xSpreadsheet, startColumn, endColumn,
						// 2);
						row += dis;
						startBorder += dis;
						startBorder1 = startBorder;
						lastKB = checkKB;
						currentPageHeight = tableHeight;
					}
					startBorder = row;

				} // kb

				/*
				 * Change(s) : Give border and page break for KB Reason(s) :
				 * give border for last page, without this code last page never
				 * have border Updated By: Johanes Updated On: 02 Nov 2009
				 */
				/*
				 * Change: remove lastKB != KBList.size() Reason: Sometimes the
				 * borders are not added when lastKB == KBList.size() Updated
				 * By: Qiao Li 29 Dec 2009
				 */
				if (check == vComp.size() /* && lastKB != KBList.size() */) {
					OO.setTableBorder(xSpreadsheet,startColumn,endColumn,
							startBorder1,endBorder,false,false,true,true,true,
							true);
				}
			}

		}

		// ----------------- END KB LEVEL SURVEY ----------------- //

	} // End of InsertComments()

	/*********************** GENERATES ALL REPORTS ************************************************************/

	public int NameSequence(int orgID) throws Exception {

		String query = "Select NameSequence from tblOrganization where PKOrganization = "
				+ orgID;

		Connection con = null;
		Statement st = null;
		ResultSet rs = null;

		int iNameSeq = 0;

		try {
			con = ConnectionBean.getConnection();
			st = con.createStatement();
			rs = st.executeQuery(query);

			if (rs.next()) {
				iNameSeq = rs.getInt(1);
			}

		} catch (Exception ex) {
			System.out.println("SPFIndividualReport.java - NameSequence - "
					+ ex.getMessage());
		} finally {
			ConnectionBean.closeRset(rs); // Close ResultSet
			ConnectionBean.closeStmt(st); // Close statement
			ConnectionBean.close(con); // Close connection
		}

		return iNameSeq;
	}

	/**
	 * Retrieves all targets under the particular division, department or group.
	 * Group = 0: all targets all groups
	 * 
	 * @param surveyID
	 * @param divID
	 * @param deptID
	 * @param groupID
	 * @param orgID
	 * @return Vector
	 * @throws Exception
	 */
	public Vector AllTargets(int surveyID, int divID, int deptID, int groupID,
			int orgID) throws Exception {
		int nameSeq = NameSequence(orgID);

		String query = "SELECT DISTINCT Asg.SurveyID, S.SurveyName, Asg.TargetLoginID, ";

		if (nameSeq == 0)
			query += "U.FamilyName + ' ' + U.GivenName AS FullName ";
		else
			query += "U.GivenName + ' ' +  U.FamilyName as FullName ";

		query += "FROM [User] U INNER JOIN tblAssignment Asg ON U.PKUser = Asg.TargetLoginID INNER JOIN tblSurvey S ON Asg.SurveyID = S.SurveyID ";
		query += "WHERE Asg.SurveyID = " + surveyID;

		if (divID > 0)
			query += " AND Asg.FKTargetDivision = " + divID;

		if (deptID > 0)
			query += " AND Asg.FKTargetDepartment = " + deptID;

		if (groupID > 0)
			query += " AND Asg.FKTargetGroup = " + groupID;

		query = query + " ORDER BY FullName";

		Connection con = null;
		Statement st = null;
		ResultSet rs = null;

		Vector vTargets = new Vector();

		try {
			con = ConnectionBean.getConnection();
			st = con.createStatement();
			rs = st.executeQuery(query);

			while (rs.next()) {
				String surveyName = rs.getString("SurveyName").trim();
				String targetID = rs.getString("TargetLoginID");
				String name = rs.getString("FullName").trim();

				String[] info = {surveyName, targetID, name};

				vTargets.add(info);
			}

		} catch (Exception ex) {
			System.out.println("SPFIndividualReport.java - AllTargets - "
					+ ex.getMessage());
		} finally {
			ConnectionBean.closeRset(rs); // Close ResultSet
			ConnectionBean.closeStmt(st); // Close statement
			ConnectionBean.close(con); // Close connection
		}

		return vTargets;
	}

	/**
	 * Get the competency avg mean for CP and CPR. The table to store Competency
	 * Level and KB Level Result are not the same.
	 * 
	 * For Competency Level: ReliabilityIndex is stored in tblAvgMean
	 * TrimmedMean is stored in tblTrimmedMean
	 * 
	 * For KBLevel, both are stored in tblAvgMean
	 * 
	 * This is a little bit messy, wrong design previously.
	 * 
	 */
	public Vector getCompAvg(int surveyID, Vector vCompID, String rtCode)
			throws SQLException {
		int surveyLevel = C.LevelOfSurvey(surveyID);
		int reliabilityCheck = C.ReliabilityCheck(surveyID); // 0=trimmed mean

		String query = "";
		String tableName = "";
		String columnName = "";

		Vector vCompCP = new Vector();
		Vector vScore = new Vector();
		Vector vResult = new Vector();

		if (surveyLevel == 1) { // KB Level

			query = "SELECT tblAvgMean.CompetencyID, ROUND(AVG(tblAvgMean.AvgMean), 2) AS AvgMean ";
			query += "FROM tblAvgMean INNER JOIN tblRatingTask ON tblAvgMean.RatingTaskID = tblRatingTask.RatingTaskID ";
			query += "WHERE tblAvgMean.SurveyID = " + surveyID;
			query += " AND (tblAvgMean.Type = 1) AND (tblRatingTask.RatingCode = '"
					+ rtCode + "') ";
			query += " and CompetencyID IN (";

			for (int i = 0; i < vCompID.size(); i++) {
				if (i != 0)
					query += ",";

				query += vCompID.elementAt(i);
			}

			query += ")";
			query += " GROUP BY tblAvgMean.CompetencyID";

		} else { // Competency Level

			if (reliabilityCheck == 0) {
				tableName = "tblTrimmedMean";
				columnName = "TrimmedMean";
			} else {
				tableName = "tblAvgMean";
				columnName = "AvgMean";
			}

			query = "SELECT " + tableName + ".CompetencyID, ROUND(AVG("
					+ tableName + "." + columnName + "), 2) AS AvgMean ";
			query += "FROM " + tableName + " INNER JOIN tblRatingTask ON ";
			query += tableName
					+ ".RatingTaskID = tblRatingTask.RatingTaskID WHERE ";
			query += tableName + ".Type = 1 AND " + tableName + ".SurveyID = "
					+ surveyID;
			query += " AND tblRatingTask.RatingCode = 'CP' ";

			query += " and CompetencyID IN (";

			for (int i = 0; i < vCompID.size(); i++) {
				if (i != 0)
					query += ",";

				query += vCompID.elementAt(i);
			}

			query += ") ";

			query += "GROUP BY " + tableName + ".CompetencyID";
		}

		// if(db.con == null)
		// db.openDB();

		Connection con = null;
		Statement st = null;
		ResultSet rs = null;

		try {
			con = ConnectionBean.getConnection();
			st = con.createStatement();
			rs = st.executeQuery(query);

			while (rs.next()) {
				String fkComp = rs.getString("CompetencyID");
				String score = rs.getString("AvgMean");

				vCompCP.add(fkComp);
				vScore.add(score);
			}

		} catch (Exception ex) {
			System.out.println("SPFIndividualReport.java - getCompAvg - "
					+ ex.getMessage());
		} finally {
			ConnectionBean.closeRset(rs); // Close ResultSet
			ConnectionBean.closeStmt(st); // Close statement
			ConnectionBean.close(con); // Close connection
		}

		// copy all the score into the correct order
		for (int i = 0; i < vCompID.size(); i++) {
			String score = "0";

			String sCompScore = (String) vCompID.elementAt(i).toString();
			int element = vCompCP.indexOf(sCompScore);

			if (element != -1)
				score = (String) vScore.elementAt(element);

			vResult.add(score);
		}

		return vResult;
	}

	public Vector getRatingScaleDescending(int RatingScaleID) {

		Vector v = new Vector();

		String query = "SELECT * FROM tblScaleValue WHERE ScaleID = "
				+ RatingScaleID;
		query = query + " ORDER BY LowValue DESC, HighValue DESC";

		Connection con = null;
		Statement st = null;
		ResultSet rs = null;

		try {
			con = ConnectionBean.getConnection();
			st = con.createStatement();
			rs = st.executeQuery(query);

			while (rs.next()) {
				votblScaleValue vo = new votblScaleValue();
				vo.setHighValue(rs.getInt("HighValue"));
				vo.setLowValue(rs.getInt("LowValue"));
				vo.setScaleDescription(rs.getString("ScaleDescription"));
				v.add(vo);
			}

		} catch (Exception E) {
			System.err
					.println("SPFIndividualReport.java - getRatingScaleDescending - "
							+ E);
		} finally {
			ConnectionBean.closeRset(rs); // Close ResultSet
			ConnectionBean.closeStmt(st); // Close statement
			ConnectionBean.close(con); // Close connection
		}

		return v;
	}

	/**
	 * Draw the development map
	 * 
	 * @param iSurvey
	 * @param iTarget
	 * @throws SQLException
	 * @throws IOException
	 * @throws Exception
	 * @see IndividualReport1#reportDevelopmentMap(int, int, String)
	 * @author Maruli
	 */
	public void drawDevelopmentMap() throws SQLException, IOException,
			Exception {
		/*
		 * +ve Q3 | Q4 (-ve) --------|-------- (+ve) Q1 | Q2 -ve
		 * 
		 * Quadrant classification Q1. Competency gap above 0 with all KB gap
		 * above 0 Q2. Competency gap above 0 with one or more KB gap below 0
		 * Q3. Competency gap below 0 with one or more KB gap above 0 Q4.
		 * Competency gap below 0 with all KB gap below 0
		 */
		// System.out.println("4. Drawing Development Map grid");

		double dCompGap = 0;
		double dBehvGap = 0;

		int iQ1 = 0;
		int iQ2 = 0;
		int iQ3 = 0;
		int iQ4 = 0;

		Q1.removeAllElements();
		Q2.removeAllElements();
		Q3.removeAllElements();
		Q4.removeAllElements();

		// double dMinGap = 0;
		// double [] arrGap = getMinMaxGap();
		// dMinGap = arrGap[0];

		// PROCESSING SECTION
		int iCompID = 0;
		int iCompRank = 0;
		String sComp = "";

		Vector rsComp = SR.getCompList(this.surveyID);
		Vector rsBehv = null;

		for (int i = 0; i < rsComp.size(); i++) {
			voCompetency vo = (voCompetency) rsComp.elementAt(i);
			iQ1 = 0;
			iQ2 = 0;
			iQ3 = 0;
			iQ4 = 0;

			iCompID = vo.getCompetencyID();
			sComp = vo.getCompetencyName();
			iCompRank = vo.getRank();

			// Get Competency's Gap
			dCompGap = SR.getCompAvgGapDevMap(this.surveyID,this.targetID,
					iCompID);

			if (dCompGap < 0) // Negative competency
			{
				// Q1 or Q2
				rsBehv = SR.getBehaviourGapDevMap(this.surveyID,this.targetID,
						iCompID);

				for (int j = 0; j < rsBehv.size(); j++) {

					String[] arr = (String[]) rsBehv.elementAt(j);
					dBehvGap = Double.parseDouble(arr[0]);

					if (dBehvGap < 0)
						iQ1++;
					else {
						// Since 1 KB is already +ve (Q2), break the loop
						iQ2++;
						break;
					}
				}

				if (iQ2 > 0)
					Q2.add(new String[]{Integer.toString(iCompID), sComp,
							Double.toString(dCompGap),
							Integer.toString(iCompRank)});
				else
					Q1.add(new String[]{Integer.toString(iCompID), sComp,
							Double.toString(dCompGap),
							Integer.toString(iCompRank)});
			} else // Positive competency
			{
				// Q3 or Q4
				rsBehv = SR.getBehaviourGapDevMap(this.surveyID,this.targetID,
						iCompID);

				for (int j = 0; j < rsBehv.size(); j++) {

					String[] arr = (String[]) rsBehv.elementAt(j);

					dBehvGap = Double.parseDouble(arr[0]);

					if (dBehvGap < 0) {
						// Since 1 KB is already -ve (Q3), break the loop
						iQ3++;
						break;
					} else
						iQ4++;
				}

				if (iQ3 > 0)
					Q3.add(new String[]{Integer.toString(iCompID), sComp,
							Double.toString(dCompGap),
							Integer.toString(iCompRank)});
				else
					Q4.add(new String[]{Integer.toString(iCompID), sComp,
							Double.toString(dCompGap),
							Integer.toString(iCompRank)});
			}
		}

		// END OF PROCESSING SECTION

		// DRAWING SECTION
		int i = 0; // For loop
		int iBigTotRow = 0;
		int iSmallTotRow = 0;
		String sBiggerQuad = "";
		String sSmallerQuad = "";
		String sBigQuadReplacement = "";
		String sSmallQuadReplacement = "";

		// Draw the upper part of the map (Q3 & Q4)
		// We need to know how many rows to insert.
		// Find out which one has more elements in it (Q3 or Q4). Store in
		// iTotRow
		if (Q3.size() > Q4.size()) {
			iBigTotRow = Q3.size(); // Q3 is bigger
			iSmallTotRow = Q4.size();
			sBiggerQuad = "<q3>";
			sSmallerQuad = "<q4>";
			sBigQuadReplacement = "Q3 - STRENGTHEN";
			sSmallQuadReplacement = "Q4 - SUSTAIN";
		} else if (Q4.size() > Q3.size()) {
			iBigTotRow = Q4.size(); // Q4 is bigger
			iSmallTotRow = Q3.size();
			sBiggerQuad = "<q4>";
			sSmallerQuad = "<q3>";
			sBigQuadReplacement = "Q4 - SUSTAIN";
			sSmallQuadReplacement = "Q3 - STRENGTHEN";
		} else // Same size
		{
			iBigTotRow = Q4.size(); // Equal size, whichever can be used
			iSmallTotRow = Q3.size();
			sBiggerQuad = "<q4>";
			sSmallerQuad = "<q3>";
			sBigQuadReplacement = "Q4 - SUSTAIN";
			sSmallQuadReplacement = "Q3 - STRENGTHEN";
		}

		int[] address = OO.findString(xSpreadsheet,sBiggerQuad);

		column = address[0];
		row = address[1];

		OO.findAndReplace(xSpreadsheet,sBiggerQuad,sBigQuadReplacement);
		row = row + 2;
		int iInitRow = row;

		for (i = 0; i < iBigTotRow; i++) {
			OO.insertRows(xSpreadsheet,startColumn,endColumn,row - 1,row,1,1);

			if (sBiggerQuad.equals("<q4>"))
				OO.insertString(xSpreadsheet,((String[]) Q4.elementAt(i))[1],
						row,column);
			else
				OO.insertString(xSpreadsheet,((String[]) Q3.elementAt(i))[1],
						row,column);

			row++;
		}

		address = OO.findString(xSpreadsheet,sSmallerQuad);

		column = address[0];
		row = address[1];

		OO.findAndReplace(xSpreadsheet,sSmallerQuad,sSmallQuadReplacement);

		for (i = 0; i < iSmallTotRow; i++) {
			if (sSmallerQuad.equals("<q4>"))
				OO.insertString(xSpreadsheet,((String[]) Q4.elementAt(i))[1],
						iInitRow,column);
			else
				OO.insertString(xSpreadsheet,((String[]) Q3.elementAt(i))[1],
						iInitRow,column);

			iInitRow++;
		}

		// Draw the bottom part of the map (Q1 & Q2)
		if (Q1.size() > Q2.size()) {
			iBigTotRow = Q1.size(); // Q1 is bigger
			iSmallTotRow = Q2.size();
			sBiggerQuad = "<q1>";
			sSmallerQuad = "<q2>";
			sBigQuadReplacement = "Q1 - ACQUIRE";
			sSmallQuadReplacement = "Q2 - INVEST";
		} else if (Q2.size() > Q1.size()) {
			iBigTotRow = Q2.size(); // Q2 is bigger
			iSmallTotRow = Q1.size();
			sBiggerQuad = "<q2>";
			sSmallerQuad = "<q1>";
			sBigQuadReplacement = "Q2 - INVEST";
			sSmallQuadReplacement = "Q1 - ACQUIRE";
		} else // Same size
		{
			iBigTotRow = Q2.size(); // Equal size, whichever can be used
			iSmallTotRow = Q1.size();
			sBiggerQuad = "<q2>";
			sSmallerQuad = "<q1>";
			sBigQuadReplacement = "Q2 - INVEST";
			sSmallQuadReplacement = "Q1 - ACQUIRE";
		}

		address = OO.findString(xSpreadsheet,sBiggerQuad);

		column = address[0];
		row = address[1];

		OO.findAndReplace(xSpreadsheet,sBiggerQuad,sBigQuadReplacement);
		row = row - 1;
		iInitRow = row;

		for (i = 0; i < iBigTotRow; i++) {
			OO.insertRows(xSpreadsheet,column,column,row,row + 1,1,1);

			if (sBiggerQuad.equals("<q2>"))
				OO.insertString(xSpreadsheet,((String[]) Q2.elementAt(i))[1],
						row,column);
			else
				OO.insertString(xSpreadsheet,((String[]) Q1.elementAt(i))[1],
						row,column);

			row++;
		}

		address = OO.findString(xSpreadsheet,sSmallerQuad);

		column = address[0];
		row = address[1];

		OO.findAndReplace(xSpreadsheet,sSmallerQuad,sSmallQuadReplacement);
		// row = row - 2; // Go up 2 spaces

		for (i = 0; i < iSmallTotRow; i++) {
			if (sSmallerQuad.equals("<q2>"))
				OO.insertString(xSpreadsheet,((String[]) Q2.elementAt(i))[1],
						iInitRow,column);
			else
				OO.insertString(xSpreadsheet,((String[]) Q1.elementAt(i))[1],
						iInitRow,column);

			iInitRow++;
		}
	}

	/**
	 * Write the Quadrant description and table heading
	 * 
	 * @param iQuadrant
	 * @param bBreakDown
	 * @throws Exception
	 * @see IndividualReport1#populateQuadrantDetail(boolean)
	 */
	public void writeQuadrantData(int iQuadrant, boolean bBreakDown)
			throws Exception {
		column = 0;
		startColumn = 0;
		endColumn = 0;

		String sTitle = "";
		String sDesc = "";

		switch (iQuadrant) {
			case 1 :
				sTitle = "QUADRANT 1 - ACQUIRE";
				sDesc = "This quadrant is typified by competencies that have negative gaps and with all key behaviours rated with negative gaps."
						+ " "
						+ "Raters generally think that the target does not meet expectations in these competencies."
						+ " "
						+ "The target would need to acquire these competencies through substantive development efforts.";
				break;

			case 2 :
				sTitle = "QUADRANT 2 - INVEST";
				sDesc = "This quadrant is typified by competencies that have negative gaps but with some key behaviours rated with positive gaps."
						+ " "
						+ "Although the target is rated as not meeting the required expectations for these competencies, raters do think that there are certain behaviours that are considered as meeting or exceeding expectations."
						+ " "
						+ "As such, the target only need to invest developmental efforts in those behaviours that are rated with negative gaps to improve the overall proficiency of the competencies.";
				break;

			case 3 :
				sTitle = "QUADRANT 3 - STRENGTHEN";
				sDesc = "This quadrant is typified by competencies that have positive gaps but with some key behaviours rated with negative gaps."
						+ " "
						+ "As such, raters generally think that the target has met expectations in these competencies but there are behaviours that are considered as below requirements."
						+ " "
						+ "The target could strengthen these competencies by working on those behaviours that are rated with negative gaps.";
				break;

			case 4 :
				sTitle = "QUADRANT 4 - SUSTAIN";
				sDesc = "This quadrant is typified by competencies that have positive gaps and with all key behaviours rated with positive gaps."
						+ " "
						+ "The target is seen to meet or exceed expectations for all these competencies."
						+ " "
						+ "The target only need to sustain proficiency in these competencies."
						+ " "
						+ "No developmental intervention is necessary in this quadrant.";
				break;
		}

		OO.insertString(xSpreadsheet2,sTitle,row,column);
		OO.mergeCells(xSpreadsheet2,startColumn,startColumn + 1,row,row);
		OO.setFontBold(xSpreadsheet2,startColumn,startColumn + 1,row,row);
		row++;

		OO.mergeCells(xSpreadsheet2,startColumn,startColumn + 6,row,row);
		OO.insertString(xSpreadsheet2,sDesc,row,column);
		OO.setRowHeight(xSpreadsheet2,row,1,
				ROWHEIGHT * OO.countTotalRow(sDesc,110));

		row += 2;
		OO.insertString(xSpreadsheet2,"Ranked Order",row,column);
		OO.mergeCells(xSpreadsheet2,column,column,row,row + 1);
		OO.setFontBold(xSpreadsheet2,column,column,row,row + 1);
		OO.setCellAllignment(xSpreadsheet2,column,column,row,row + 1,1,2);
		OO.setCellAllignment(xSpreadsheet2,column,column,row,row + 1,2,2);
		column++;

		OO.insertString(xSpreadsheet2,"Competency",row,column);
		OO.mergeCells(xSpreadsheet2,column,column,row,row + 1);
		OO.setFontBold(xSpreadsheet2,column,column,row,row + 1);
		OO.setCellAllignment(xSpreadsheet2,column,column,row,row + 1,1,2);
		OO.setCellAllignment(xSpreadsheet2,column,column,row,row + 1,2,2);
		column++;

		int iCategory = RR.getTotalRelation(this.surveyID,bBreakDown);

		OO.insertString(xSpreadsheet2,"Current Proficiency",row,column);
		// Calculate total no of categories and merge the cell
		OO.mergeCells(xSpreadsheet2,column,column + (iCategory - 1),row,row);
		OO.setFontBold(xSpreadsheet2,column,column + (iCategory - 1),row,
				row + 1);
		OO.setCellAllignment(xSpreadsheet2,column,column,row,row + 1,1,2);

		int iGapCol = column + iCategory;
		OO.insertString(xSpreadsheet2,"Gap",row,iGapCol);
		OO.mergeCells(xSpreadsheet2,iGapCol,iGapCol,row,row + 1);
		OO.setFontBold(xSpreadsheet2,iGapCol,iGapCol,row,row + 1);
		OO.setCellAllignment(xSpreadsheet2,iGapCol,iGapCol,row,row + 1,1,2);
		OO.setCellAllignment(xSpreadsheet2,iGapCol,iGapCol,row,row + 1,2,2);

		endColumn = iGapCol;
		OO.setTableBorder(xSpreadsheet2,startColumn,endColumn,row,row + 1,true,
				true,true,true,true,true);

		row++;

		if (bBreakDown)
			writeBreakDownQuadrantScore(iQuadrant);
		else
			writeQuadrantScore(iQuadrant);

		OO.insertPageBreak(xSpreadsheet2,0,1,row);
	}

	/**
	 * Main function to write the Quadrant Details (Sheet 2)
	 * 
	 * @param bBreakDown
	 * @throws SQLException
	 * @throws IOException
	 * @throws Exception
	 * @author Maruli
	 * @see IndividualReport1#reportDevelopmentMap(int, int, String, boolean)
	 */
	public void populateQuadrantDetail(boolean bBreakDown) throws Exception {
		// System.out.println("5. Populating Quadrant details");

		row = 0;

		writeQuadrantData(1,bBreakDown);
		writeQuadrantData(2,bBreakDown);
		writeQuadrantData(3,bBreakDown);
		writeQuadrantData(4,bBreakDown);
	}

	/**
	 * Write the Competency, KB, CP and Gap score (Category Relation High)
	 * 
	 * @param iQuadrant
	 * @throws Exception
	 * @author Maruli
	 * @see IndividualReport1#writeQuadrantData(int, boolean)
	 */
	public void writeQuadrantScore(int iQuadrant) throws Exception {
		int iCol = 2; // To iterate through each group of CP

		Vector vLocal = null;
		Vector rsBehv = null;
		Vector rsRelation = null;

		double dCompCP = 0;
		double dBehvCP = 0;
		double dCompGap = 0;
		double dBehvGap = 0;
		int iCompID = 0;
		int iBehvID = 0;
		int iCompRank = 0;
		String sComp = "";
		String sBehv = "";

		switch (iQuadrant) {
			case 1 :
				vLocal = (Vector) Q1.clone();
				break;
			case 2 :
				vLocal = (Vector) Q2.clone();
				break;
			case 3 :
				vLocal = (Vector) Q3.clone();
				break;
			case 4 :
				vLocal = (Vector) Q4.clone();
				break;
		}

		// vLocal = G.sortVector(vLocal, 1);

		for (int a = 0; a < vLocal.size(); a++) {
			if (a == 0) {
				String sRelation = "";

				OO.insertString(xSpreadsheet2,"Self",row,iCol);
				OO.setFontBold(xSpreadsheet2,iCol,iCol,row,row);
				OO.setCellAllignment(xSpreadsheet2,iCol,iCol,row,row,1,2);
				OO.setCellAllignment(xSpreadsheet2,iCol,iCol,row,row,2,2);
				iCol++;

				rsRelation = RR.getRelationHigh(this.surveyID);

				for (int i = 0; i < rsRelation.size(); i++) {
					votblRelationHigh vo = (votblRelationHigh) rsRelation
							.elementAt(i);
					sRelation = vo.getRelationHigh();
					OO.insertString(xSpreadsheet2,sRelation,row,iCol);
					OO.setFontBold(xSpreadsheet2,iCol,iCol,row,row);
					OO.setCellAllignment(xSpreadsheet2,iCol,iCol,row,row,1,2);
					OO.setCellAllignment(xSpreadsheet2,iCol,iCol,row,row,2,2);
					iCol++;
				}

				row++;
			}

			iCol = 0;

			iCompID = Integer.parseInt(((String[]) vLocal.elementAt(a))[0]);
			sComp = ((String[]) vLocal.elementAt(a))[1];
			dCompGap = Double.parseDouble(((String[]) vLocal.elementAt(a))[2]);
			iCompRank = Integer.parseInt(((String[]) vLocal.elementAt(a))[3]);

			OO.insertNumeric(xSpreadsheet2,iCompRank,row,iCol);
			OO.setCellAllignment(xSpreadsheet2,iCol,iCol,row,row,1,2);
			OO.setBGColor(xSpreadsheet2,iCol,iCol,row,row,BGCOLOR);
			iCol++;

			OO.insertString(xSpreadsheet2,sComp,row,iCol);
			OO.setBGColor(xSpreadsheet2,iCol,iCol,row,row,BGCOLOR);
			iCol++;

			// SELF CP goes first
			dCompCP = SR.getAvgCPComp(this.surveyID,this.targetID,4,iCompID);
			OO.insertNumeric(xSpreadsheet2,dCompCP,row,iCol);
			OO.setCellAllignment(xSpreadsheet2,iCol,iCol,row,row,1,2);
			OO.setBGColor(xSpreadsheet2,iCol,iCol,row,row,BGCOLOR);
			iCol++;

			int iRelID = 0;

			rsRelation = RR.getRelationHigh(this.surveyID);
			for (int i = 0; i < rsRelation.size(); i++) {
				votblRelationHigh vo = (votblRelationHigh) rsRelation
						.elementAt(i);

				iRelID = vo.getRTRelation();

				// In tblAvgMean SUP = 2 but in tblRelation SUP = 1. OTH is not
				// affected (Both 3)
				if (iRelID == 1)
					iRelID = 2;

				dCompCP = SR.getAvgCPComp(this.surveyID,this.targetID,iRelID,
						iCompID);
				OO.insertNumeric(xSpreadsheet2,dCompCP,row,iCol);
				OO.setCellAllignment(xSpreadsheet2,iCol,iCol,row,row,1,2);
				OO.setBGColor(xSpreadsheet2,iCol,iCol,row,row,BGCOLOR);
				iCol++;
			}

			OO.insertNumeric(xSpreadsheet2,dCompGap,row,iCol);
			OO.setCellAllignment(xSpreadsheet2,iCol,iCol,row,row,1,2);
			OO.setBGColor(xSpreadsheet2,iCol,iCol,row,row,BGCOLOR);

			OO.setTableBorder(xSpreadsheet2,startColumn,endColumn,row,row,true,
					true,true,true,true,true);

			row++;
			iCol = 1;

			// Get Behaviour's gap
			rsBehv = SR.getBehaviourGapDevMap(this.surveyID,this.targetID,
					iCompID);

			for (int j = 0; j < rsBehv.size(); j++) {

				String[] arr = (String[]) rsBehv.elementAt(j);

				iCol = 1;
				iBehvID = Integer.parseInt(arr[2]);
				sBehv = arr[1];
				dBehvGap = Double.parseDouble(arr[0]);

				OO.insertString(xSpreadsheet2,sBehv,row,iCol);
				iCol++;
				// SELF CP goes first
				dBehvCP = SR.getAvgCPKB(this.surveyID,this.targetID,4,iBehvID);
				OO.insertNumeric(xSpreadsheet2,dBehvCP,row,iCol);
				OO.setCellAllignment(xSpreadsheet2,iCol,iCol,row,row,1,2);
				iCol++;

				rsRelation = RR.getRelationHigh(this.surveyID);

				for (int i = 0; i < rsRelation.size(); i++) {
					votblRelationHigh vo = (votblRelationHigh) rsRelation
							.elementAt(i);
					iRelID = vo.getRTRelation();

					// In tblAvgMean SUP = 2 but in tblRelation SUP = 1. OTH is
					// not affected (Both 3)
					if (iRelID == 1)
						iRelID = 2;

					dBehvCP = SR.getAvgCPKB(this.surveyID,this.targetID,iRelID,
							iBehvID);
					OO.insertNumeric(xSpreadsheet2,dBehvCP,row,iCol);
					OO.setCellAllignment(xSpreadsheet2,iCol,iCol,row,row,1,2);
					iCol++;
				}

				OO.insertNumeric(xSpreadsheet2,dBehvGap,row,iCol);
				OO.setCellAllignment(xSpreadsheet2,iCol,iCol,row,row,1,2);

				OO.setTableBorder(xSpreadsheet2,startColumn,endColumn,row,row,
						true,true,true,true,true,true);
				row++;
			}
		}

		// If there is no competency in that particular Quadrant, insert
		// relation and leave 2 blank spaces
		if (vLocal.size() == 0) {
			String sRelation = "";

			OO.insertString(xSpreadsheet2,"Self",row,iCol);
			OO.setCellAllignment(xSpreadsheet2,iCol,iCol,row,row,1,2);
			iCol++;

			rsRelation = RR.getRelationHigh(this.surveyID);

			for (int i = 0; i < rsRelation.size(); i++) {
				votblRelationHigh vo = (votblRelationHigh) rsRelation
						.elementAt(i);

				sRelation = vo.getRelationHigh();
				OO.insertString(xSpreadsheet2,sRelation,row,iCol);
				OO.setCellAllignment(xSpreadsheet2,iCol,iCol,row,row,1,2);
				iCol++;
			}

			row++;
			OO.insertString(xSpreadsheet2,
					"There are no competencies under this Quadrant",row,0);
			OO.mergeCells(xSpreadsheet2,0,endColumn,row,row);
			row += 2;
		}
	}

	/**
	 * Write the Competency, KB, CP and Gap score (Category Relation High &
	 * Specific)
	 * 
	 * @param iQuadrant
	 * @throws Exception
	 * @author Maruli
	 * @see IndividualReport1#writeQuadrantData(int, boolean)
	 */
	public void writeBreakDownQuadrantScore(int iQuadrant) throws Exception {
		int iCol = 2; // To iterate through each group of CP

		Vector vLocal = null;
		Vector rsBehv = null;
		Vector rsRelationHigh = null;
		Vector rsRelationSpec = null;

		double dCompCP = 0;
		double dBehvCP = 0;
		double dCompGap = 0;
		double dBehvGap = 0;
		int iCompID = 0;
		int iBehvID = 0;
		int iCompRank = 0;
		String sComp = "";
		String sBehv = "";
		String sRelation = "";
		int iRelHigh = 0;

		switch (iQuadrant) {
			case 1 :
				vLocal = (Vector) Q1.clone();
				break;
			case 2 :
				vLocal = (Vector) Q2.clone();
				break;
			case 3 :
				vLocal = (Vector) Q3.clone();
				break;
			case 4 :
				vLocal = (Vector) Q4.clone();
				break;
		}

		// vLocal = G.sortVector(vLocal, 1);

		for (int a = 0; a < vLocal.size(); a++) {
			if (a == 0) {
				OO.insertString(xSpreadsheet2,"Self",row,iCol);
				OO.setCellAllignment(xSpreadsheet2,iCol,iCol,row,row,1,2);
				OO.setCellAllignment(xSpreadsheet2,iCol,iCol,row,row,2,2);
				iCol++;

				rsRelationHigh = RR.getRelationHigh(this.surveyID);

				for (int i = 0; i < rsRelationHigh.size(); i++) {
					votblRelationHigh vo = (votblRelationHigh) rsRelationHigh
							.elementAt(i);

					iRelHigh = vo.getRTRelation();

					rsRelationSpec = RR.getRelationSpecific(this.surveyID,
							iRelHigh);

					for (int j = 0; j < rsRelationSpec.size(); j++) {
						votblAssignment voTbl = (votblAssignment) rsRelationSpec
								.elementAt(j);
						sRelation = voTbl.getRelationSpecific();
						OO.insertString(xSpreadsheet2,sRelation,row,iCol);
						OO.setCellAllignment(xSpreadsheet2,iCol,iCol,row,row,1,
								2);
						OO.setCellAllignment(xSpreadsheet2,iCol,iCol,row,row,2,
								2);
						iCol++;
					}
				}

				row++;
			}

			iCol = 0;

			iCompID = Integer.parseInt(((String[]) vLocal.elementAt(a))[0]);
			sComp = ((String[]) vLocal.elementAt(a))[1];
			dCompGap = Double.parseDouble(((String[]) vLocal.elementAt(a))[2]);
			iCompRank = Integer.parseInt(((String[]) vLocal.elementAt(a))[3]);

			OO.insertNumeric(xSpreadsheet2,iCompRank,row,iCol);
			OO.setCellAllignment(xSpreadsheet2,iCol,iCol,row,row,1,2);
			OO.setBGColor(xSpreadsheet2,iCol,iCol,row,row,BGCOLOR);
			iCol++;

			OO.insertString(xSpreadsheet2,sComp,row,iCol);
			OO.setBGColor(xSpreadsheet2,iCol,iCol,row,row,BGCOLOR);
			iCol++;

			// SELF CP goes first
			dCompCP = SR.getAvgCPCompBreakDown(this.surveyID,this.targetID,2,0,
					iCompID);
			OO.insertNumeric(xSpreadsheet2,dCompCP,row,iCol);
			OO.setCellAllignment(xSpreadsheet2,iCol,iCol,row,row,1,2);
			OO.setBGColor(xSpreadsheet2,iCol,iCol,row,row,BGCOLOR);
			iCol++;

			rsRelationHigh = RR.getRelationHigh(this.surveyID);

			for (int i = 0; i < rsRelationHigh.size(); i++) {
				votblRelationHigh vo = (votblRelationHigh) rsRelationHigh
						.elementAt(i);

				iRelHigh = vo.getRTRelation();

				rsRelationSpec = RR.getRelationSpecific(this.surveyID,iRelHigh);

				for (int j = 0; j < rsRelationSpec.size(); j++) {
					votblAssignment voTbl = (votblAssignment) rsRelationSpec
							.elementAt(j);

					dCompCP = SR.getAvgCPCompBreakDown(this.surveyID,
							this.targetID,iRelHigh,voTbl.getRTSpecific(),
							iCompID);
					OO.insertNumeric(xSpreadsheet2,dCompCP,row,iCol);
					OO.setCellAllignment(xSpreadsheet2,iCol,iCol,row,row,1,2);
					OO.setBGColor(xSpreadsheet2,iCol,iCol,row,row,BGCOLOR);
					iCol++;
				}
			}

			OO.insertNumeric(xSpreadsheet2,dCompGap,row,iCol);
			OO.setCellAllignment(xSpreadsheet2,iCol,iCol,row,row,1,2);
			OO.setBGColor(xSpreadsheet2,iCol,iCol,row,row,BGCOLOR);

			OO.setTableBorder(xSpreadsheet2,startColumn,endColumn,row,row,true,
					true,true,true,true,true);

			row++;
			iCol = 1;

			// Get Behaviour's gap

			rsBehv = SR.getBehaviourGapDevMap(this.surveyID,this.targetID,
					iCompID);

			for (int j = 0; j < rsBehv.size(); j++) {

				String[] arr = (String[]) rsBehv.elementAt(j);

				iCol = 1;
				iBehvID = Integer.parseInt(arr[2]);
				sBehv = arr[1];
				dBehvGap = Double.parseDouble(arr[0]);;

				OO.insertString(xSpreadsheet2,sBehv,row,iCol);
				iCol++;
				// SELF CP goes first
				dBehvCP = SR.getAvgCPKBBreakDown(this.surveyID,this.targetID,2,
						0,iBehvID);
				OO.insertNumeric(xSpreadsheet2,dBehvCP,row,iCol);
				OO.setCellAllignment(xSpreadsheet2,iCol,iCol,row,row,1,2);
				iCol++;

				rsRelationHigh = RR.getRelationHigh(this.surveyID);

				for (int i = 0; i < rsRelationHigh.size(); i++) {
					votblRelationHigh vo = (votblRelationHigh) rsRelationHigh
							.elementAt(i);

					iRelHigh = vo.getRTRelation();

					rsRelationSpec = RR.getRelationSpecific(this.surveyID,
							iRelHigh);

					for (int k = 0; k < rsRelationSpec.size(); k++) {
						votblAssignment voTbl = (votblAssignment) rsRelationSpec
								.elementAt(k);

						dBehvCP = SR.getAvgCPKBBreakDown(this.surveyID,
								this.targetID,iRelHigh,voTbl.getRTSpecific(),
								iBehvID);
						OO.insertNumeric(xSpreadsheet2,dBehvCP,row,iCol);
						OO.setCellAllignment(xSpreadsheet2,iCol,iCol,row,row,1,
								2);
						iCol++;
					}
				}

				OO.insertNumeric(xSpreadsheet2,dBehvGap,row,iCol);
				OO.setCellAllignment(xSpreadsheet2,iCol,iCol,row,row,1,2);

				OO.setTableBorder(xSpreadsheet2,startColumn,endColumn,row,row,
						true,true,true,true,true,true);
				row++;
			}
		}

		// If there is no competency in that particular Quadrant, insert
		// relation and leave 2 blank spaces
		if (vLocal.size() == 0) {
			OO.insertString(xSpreadsheet2,"Self",row,iCol);
			OO.setCellAllignment(xSpreadsheet2,iCol,iCol,row,row,1,2);
			iCol++;

			rsRelationHigh = RR.getRelationHigh(this.surveyID);

			for (int i = 0; i < rsRelationHigh.size(); i++) {
				votblRelationHigh vo = (votblRelationHigh) rsRelationHigh
						.elementAt(i);

				iRelHigh = vo.getRTRelation();

				rsRelationSpec = RR.getRelationSpecific(this.surveyID,iRelHigh);

				for (int j = 0; j < rsRelationSpec.size(); j++) {
					votblAssignment voTbl = (votblAssignment) rsRelationSpec
							.elementAt(j);
					sRelation = voTbl.getRelationSpecific();

					OO.insertString(xSpreadsheet2,sRelation,row,iCol);
					OO.setCellAllignment(xSpreadsheet2,iCol,iCol,row,row,1,2);
					iCol++;
				}
			}

			row++;
			OO.insertString(xSpreadsheet2,
					"There are no competencies under this Quadrant",row,0);
			OO.mergeCells(xSpreadsheet2,0,endColumn,row,row);
			row += 2;
		}
	}

	public int getCompetencyRank(int iCompetency) throws SQLException {
		int iRank = 0;

		String sSQL = "SELECT CompetencyRank FROM tblSurveyCompetency WHERE CompetencyID = "
				+ iCompetency;
		Connection con = null;
		Statement st = null;
		ResultSet rs = null;

		try {
			con = ConnectionBean.getConnection();
			st = con.createStatement();
			rs = st.executeQuery(sSQL);

			if (rs.next())
				iRank = rs.getInt("CompetencyRank");

		} catch (Exception ex) {
			System.out
					.println("SPFIndividualReport.java - getCompetencyRank - "
							+ ex.getMessage());
		} finally {
			ConnectionBean.closeRset(rs); // Close ResultSet
			ConnectionBean.closeStmt(st); // Close statement
			ConnectionBean.close(con); // Close connection
		}

		return iRank;
	}

	/**
	 * Development Map Report (Main function)
	 * 
	 * @param surveyID
	 * @param targetID
	 * @param fileName
	 * @param bBreakDown
	 *            - True=Breakdown OTH into sub category
	 * @author Maruli
	 */
	public void reportDevelopmentMap(int surveyID, int targetID,
			String fileName, boolean bBreakDown) {
		try {

			// System.out.println("Development Map Generation Starts");

			InitializeExcelDevMap(fileName,"Development Map Template.xls");
			InitializeSurvey(surveyID,targetID,fileName);
			replacementDevelopmentMap();

			drawDevelopmentMap();
			populateQuadrantDetail(bBreakDown);

			Date timestamp = new Date();
			SimpleDateFormat dFormat = new SimpleDateFormat("dd/MM/yyyy");
			String temp = dFormat.format(timestamp);

			// changed copyright symbol to \u00a9 and registered symbol to
			// \u00AE by Chun Yeong 8 Jun 2011
			OO.insertHeaderFooter(
					xDoc,
					"",
					surveyInfo[6] + "\nTarget: " + UserName() + "\n",
					"Date of Printing: "
							+ temp
							+ "\n"
							+ "Copyright \u00a9 3-Sixty Profiler\u00AE is a product of Pacific Century Consulting Pte Ltd.",
					1,3);

			// System.out.println("Development Map Generation Completed");

		} catch (SQLException SE) {
			System.out.println("a " + SE.getMessage());
		} catch (Exception E) {
			System.out.println("b " + E.getMessage());
		} finally {

			try {
				OO.storeDocComponent(xRemoteServiceManager,xDoc,storeURL);
				OO.closeDoc(xDoc);
			} catch (SQLException SE) {
				System.out.println("a " + SE.getMessage());
			} catch (IOException IO) {
				System.err.println(IO);
			} catch (Exception E) {
				System.out.println("b " + E.getMessage());
			}
		}
	}

	/**
	 * Print Individual Report
	 * 
	 * @param surveyID
	 * @param targetID
	 * @param pkUser
	 * @param fileName
	 * @param type
	 *            - 1=Simplified(No charts), 2=Standard
	 * @param chkNormative
	 * @param chkGroupCPLine
	 *            - Added by Chun Yeong, 2 Jun 2011 to allow option to show
	 *            Group Current Proficiency
	 * @param chkSplit
	 *            - Added by Chun Yeong, 13 Jun 2011 to allow split others
	 *            option
	 * @param lang
	 *            - Added by Chun Yeong, 1 Aug 2011, to enable selection of
	 *            Language
	 * @param template
	 *            - Added by Chun Yeong, 1 Aug 2011, to enable selection of
	 *            template
	 * @author Jenty - Edited by Maruli
	 */
	// Edited printing report with Normation optional by Tracy 01 Sep 08
	public void Report(int surveyID, int targetID, int pkUser, String fileName,
			int type, String chkNormative, String chkGroupCPLine,
			String chkSplit, String chkBreakCPR, int lang, String template,
			boolean weightedAvg, boolean SPFFormat)
	// End add by Tracy 01 Sep 08
	{
		try {
			if (weightedAvg == true) {
				weightedAverage = true;
			}

			templateNameSPF = SPFFormat;

			/*
			 * We need to re-initialize the start and end column. If we need to
			 * send reports through email, the next time we tried to generate
			 * report, the start and end column won't be initialised by
			 * constructor anymore.
			 * 
			 * If there is a part which substract/add these 2 variables, the
			 * code is going to have problem. Possibly array out of bound will
			 * happen.
			 */
			startColumn = 0;
			endColumn = 12;

			// Added to populate the hashtable for translation, Chun Yeong 1 Aug
			// 2011
			trans.populateHashtable();
			// Check if template String contains the specific language type and
			// set the variable templateLanguage to the respective value
			// 1 - Indonesian
			// 2 - Thai
			// 3 - Korean
			// 4 - Chinese(Simplified)
			// 5 - Chinese(Traditional)
			if (template.toLowerCase().contains("indo"))
				templateLanguage = 1;
			else if (template.toLowerCase().contains("thai"))
				templateLanguage = 2;
			// else if(template.toLowerCase().contains("korean"))
			// templateLanguage = 3;
			// else if(template.toLowerCase().contains("chinese(simp)"))
			// templateLanguage = 4;
			// else if(template.toLowerCase().contains("chinese(trad)"))
			// templateLanguage = 5;
			else
				templateLanguage = 0; // English

			System.out.println("1. Individual Report Generation Starts");
			iReportType = type; // Set report type
			// added in a class varibble to indicate whether this survey split
			// "Others" to "Subordinates" and "Peers"
			Create_Edit_Survey ces = new Create_Edit_Survey();
			splitOthers = ces.getSplitOthersOption(surveyID);

			// Update from whether to reference the template or not to split
			// Peers and Subordinates by Chun Yeong 13 Jun 2011
			if (!chkSplit.equals("")) {
				// 1 for split, 0 for join
				if (splitOthers == 1) {
					splitOthers = 0;
				} else if (splitOthers == 0) {
					splitOthers = 1;
				}
			}

			/*
			 * Set the language type base on what the user selected. 1 -
			 * Indonesian 2 - Thai 3 - Korean 4 - Chinese (Simplified) 5 -
			 * Chinese (Traditional)If the lang == 0, the default is English,
			 * Chun Yeong 1 Aug 2011
			 */
			if (lang == 1) {
				language = "1";
			} else if (lang == 2) {
				language = "2";
			} else {
				language = "";
			}

			/*
			 * Change(s) : Added logic and relevant variables to facilitate
			 * switching between report templates Reason(s) : To facilitate
			 * switching between report templates for SPS and other future
			 * clients Updated By: Desmond Updated On: 23 Oct 2009 Previous
			 * Update: Edited printing ind report with Normative option, By
			 * Tracy 01 Sep 08
			 */
			String templateName = "";
			if (templateNameSPF == true) {
				templateName = "Individual Report Template_SPF(Modified).xls";
			} else {
				templateName = "Individual Report Template_SPF.xls";
			}

			// Variable for storing filename of template to be used for
			// generating report, Desmond 23 Oct 09
			// set back to "" such that we can have a choice of different
			// organizations (Qiao Li 22 Dec 2009)
			String org = "";
			InitializeExcel(fileName,templateName);

			// Added, set global boolean variable to display/not display Group
			// CP Line, by Chun Yeong 2 Jun 2011
			isGroupCPLine = (chkGroupCPLine.equals("")) ? false : true;

			InitializeSurvey(surveyID,targetID,fileName);
			InsertCalculateStatus();
			Replacement();

			InsertSuveyOverview(surveyID,targetID);

			PrelimQuestionController pqc = new PrelimQuestionController();
			if (pqc.getQuestions(surveyID).size() > 0) {
				InsertPrelimQuestions();
			}

			/*
			 * Remove for the time being as the modified template already has
			 * the scale
			 */
			// InsertRatingScaleList();
			InsertCPvsCPR();

			if (Integer.parseInt(surveyInfo[9]) == 0) { // without cluster
				int[] address = OO.findString(xSpreadsheet,
						"<Gap Title Cluster>");
				OO.findAndReplace(xSpreadsheet,"<Gap Title Cluster>","");
				OO.findAndReplace(xSpreadsheet,"<Gap Cluster>","");
				int tempRow = address[1];

				OO.deleteRows(xSpreadsheet,startColumn,endColumn,tempRow - 4,
						tempRow + 8,11,1);
				InsertGapTitle(surveyID);
				InsertGap();
			} else { // with cluster

				InsertGapTitleCluster(surveyID);
				InsertGapCluster();
				InsertGapTitle(surveyID);
				InsertGap();
			}

			// Edited printing ind report with Normative option
			// by Tracy 01 Sep 08***
			try {
				if (chkNormative != "") {
					InsertNormative();

				} else {
					// by Hemilda 23/08/2008 for not include normative hasn't
					// define
					// the array, always got error
					int total = totalCompetency();
					// Initialise array for arrN
					arrN = new int[total * 10 * 6]; // size = total competency *
													// max
													// 10 KBs * max 6 Rating

				}
			} catch (Exception ex) {
				System.out.println("Normative page missing");
			}

			// End edit by Tracy 01 Sep 08***

			// Added by Tracy 01 Sep 08************************
			// Page 6: Ind Profile report- Insert dynamic competency in Gap
			InsertCompGap(surveyID);
			// End add by Tracy 01 Sep 08**********************

			// insert the appropriate legends (w/o CPR, split "Others") (Qiao Li
			// 22 Dec 2009)
			if (!chkBreakCPR.equals("") && (iNoCPR == 0) && (CPRorFPR == 1)) { // breakCPR
																				// is
																				// ticked,
																				// survey
																				// has
																				// rating
																				// task
																				// CPR
																				// and
																				// not
																				// FPR
				breakCPR = 1;
			} else {
				breakCPR = 0;
			}
			InsertProfileLegend();

			int rowNumber = row;
			if (Integer.parseInt(surveyInfo[9]) == 0) { // without cluster
				InsertCompetency(type,surveyID,breakCPR);
			} else { // with cluster
				InsertClusterCompetency(type,surveyID,breakCPR);
			}
			// Insert IMPT info to the report if its importance survey
			if (hasImportance(surveyID)) {
				OO.findAndReplace(
						xSpreadsheet,
						"<IMPT Info>",
						"The IMPT score indicates how important the competency or behaviour is to the success of your job. The higher the score, the more important it is considered to be.");
			} else {
				int address[] = OO.findString(xSpreadsheet,"<IMPT Info>");
				int iColumn = address[0];
				int iRow = address[1];
				OO.findAndReplace(xSpreadsheet,"<IMPT Info>","");
				OO.deleteRows(xSpreadsheet,startColumn,endColumn,iRow,iRow + 3,
						2,1);
			}
			AddDynamicReportInfo();

			/*
			 * Change(s) : Added new section of blind spot analysis Reason(s) :
			 * New section required Updated By: Chun Yeong, 27 May 2011
			 */
			// Insert the header
			OO.insertString(xSpreadsheet,
					trans.tslt(templateLanguage,"BLIND SPOTS ANALYSIS"),row,0);
			OO.setFontBold(xSpreadsheet,startColumn,endColumn,row,row);
			OO.setFontSize(xSpreadsheet,startColumn,endColumn,row,row,16);
			OO.setRowHeight(xSpreadsheet,row,startColumn,600);
			OO.mergeCells(xSpreadsheet,startColumn,startColumn + 6,row,row);
			row++;

			if (hasRatedSelfKBLevel(surveyID,targetID)
					|| hasRatedSelfCompLevel(surveyID,targetID)) { // check if
																	// target
																	// has rated
																	// himself
				// target rate himself so insert blindspot
				if (Integer.parseInt(surveyInfo[9]) == 0) { // without cluster
					InsertBlindSpotAnalysis(true); // Insert positive table
					row++;
					InsertBlindSpotAnalysis(false); // Insert negative table
				} else { // with cluster
					InsertClusterBlindSpotAnalysis(true); // Insert positive
															// table
					row++;
					InsertClusterBlindSpotAnalysis(false); // Insert negative
															// table
				}
			} else {
				String titleDesc = "Not available as Target did not do self rating.";

				row += 1;
				// Insert the description
				OO.insertString(xSpreadsheet,
						UnicodeHelper.getUnicodeStringAmp(titleDesc),row,0);
				OO.mergeCells(xSpreadsheet,startColumn,endColumn - 1,row,row);
				row += 2;
			}
			lastPageRowCount = 0;
			OO.insertPageBreak(xSpreadsheet,startColumn,endColumn,row);
			// End add by Chun Yeong, 25 May 2011************

			int included = Q.commentIncluded(surveyID);
			// Added by Ha 23/06/09 to insert self commnent as well
			int selfIncluded = Q.SelfCommentIncluded(surveyID);

			/*
			 * Comments are moved to under each competency graph if(included ==
			 * 1||selfIncluded==1){ if(Integer.parseInt(surveyInfo[9])==0){
			 * //without cluster InsertComments(); } else{
			 * InsertClusterComments(); } }
			 */

			/*
			 * Change(s) : Added new section of blind spot analysis Reason(s) :
			 * New section required Updated By: Chun Yeong, 27 May 2011
			 */

			/*
			 * Change(s) : Added new section for generating additional questions
			 * in the report Updated By: Wei Han 16 Apr 2012
			 */
			AdditionalQuestionController aqc = new AdditionalQuestionController();
			if (aqc.getQuestions(surveyID).size() > 0)
				InsertAdditionalQuestions();

			int rowEnd = row;
			// testpagelength();
			// OO.insertRows(xSpreadsheet, 0, totalColumn, rowNumber, row,
			// row-rowNumber,rowNumber);

			if (surveyInfo[8] != "") // Org Logo
			{
				System.out.println("Report() - Logo location: "
						+ ST.getOOLogoPath() + surveyInfo[8]); // To Remove,
																// Desmond 19
																// Nov 09
				File F = new File(ST.getOOLogoPath() + surveyInfo[8]); // directory
																		// where
																		// the
																		// file
																		// supposed
																		// to be
																		// stored
				if (F.exists())
					OO.replaceLogo(xSpreadsheet,xDoc,"<Logo>",
							ST.getOOLogoPath() + surveyInfo[8]);
				else
					OO.replaceLogo(xSpreadsheet,xDoc,"<Logo>","");
			}

			Date timestamp = new Date();
			SimpleDateFormat dFormat = new SimpleDateFormat("dd/MM/yyyy");
			String temp = dFormat.format(timestamp);
			// changed copyright symbol to \u00a9 and registered symbol to
			// \u00AE by Chun Yeong 8 Jun 2011
			insertItemFrequency(surveyID);
			printTargetRank();
			insertCompetencyRankTable();
			// OO.insertHeaderFooter(
			// xDoc,
			// surveyInfo[1],
			// surveyInfo[6] + "\n" + UserName() + "\n",
			// "Date of printing: "
			// + temp
			// + "\n"
			// +
			// "Copyright \u00a9 3-Sixty Profiler\u00AE is a product of Pacific Century Consulting Pte Ltd.",
			// 1,5);
			// for MM 360
			OO.insertHeaderFooter(
					xDoc,
					"",
					surveyInfo[6] + "\n" + UserName() + "\n",
					"Date of printing: "
							+ "22/11/2013"
							+ "\n"
							+ "Copyright \u00a9 3-Sixty Profiler\u00AE is a product of Pacific Century Consulting Pte Ltd.",
					1,5);
			// System.out.println("Individual Report Generation Completed");

		} catch (SQLException SE) {
			System.out.println("a " + SE.getMessage());
		} catch (Exception E) {
			System.out.println("b " + E.getMessage());
			E.printStackTrace();
		} finally {

			try {
				if (format == 0) {
					OO.storeDocComponent(xRemoteServiceManager,xDoc,storeURL);
				} else {
					OO.storeDocComponentAsPDF(xRemoteServiceManager,xDoc,
							storeURL);
				}
				OO.closeDoc(xDoc);
			} catch (SQLException SE) {
				System.out.println("a " + SE.getMessage());
			} catch (IOException IO) {
				System.err.println(IO);
			} catch (Exception E) {
				System.out.println("b " + E.getMessage());
			}
		}
	}

	public void insertItemFrequency(int surveyID) throws SQLException,
			Exception {
		rowFreq = 1;
		columnFreq = 0;
		OO.insertString(xSpreadsheet3,
				trans.tslt(templateLanguage,"ITEM FREQUENCY REPORT"),rowFreq,0);
		OO.setFontBold(xSpreadsheet3,startColumnFreq,endColumnFreq,rowFreq,
				rowFreq);
		OO.setFontSize(xSpreadsheet3,startColumnFreq,endColumnFreq,rowFreq,
				rowFreq,16);
		OO.setRowHeight(xSpreadsheet3,rowFreq,startColumnFreq,600);
		OO.mergeCells(xSpreadsheet3,startColumnFreq,startColumnFreq + 23,
				rowFreq,rowFreq);
		rowFreq += 2;

		Vector vClust = ClusterByName();
		String clusterName = "";
		int clusterID;
		for (int i = 0; i < vClust.size(); i++) {
			columnFreq = 0;
			voCluster voClust = (voCluster) vClust.elementAt(i);
			clusterName = voClust.getClusterName();
			clusterID = voClust.getClusterID();
			if (i == 4) {
				OO.insertPageBreak(xSpreadsheet3,0,11,rowFreq);
			}
			OO.insertString(xSpreadsheet3,
					trans.tslt(templateLanguage,"CLUSTER - " + clusterName),
					rowFreq,0);
			OO.setFontBold(xSpreadsheet3,startColumnFreq,endColumnFreq,rowFreq,
					rowFreq);
			rowFreq++;
			int startBorder = rowFreq;
			if (!combineDIRIDR) {
				OO.setBGColor(xSpreadsheet3,startColumnFreq,endColumnFreq,
						rowFreq,rowFreq + 1,12632256);
				OO.mergeCells(xSpreadsheet3,startColumnFreq,startColumnFreq,
						rowFreq,rowFreq + 1);
			} else {
				OO.setBGColor(xSpreadsheet3,startColumnFreq,endColumnFreq - 6,
						rowFreq,rowFreq + 1,12632256);
				OO.mergeCells(xSpreadsheet3,startColumnFreq,startColumnFreq,
						rowFreq,rowFreq + 1);
			}
			columnFreq++;
			// suppose to start inserting the self/supervisor and other
			// relations
			OO.insertString(xSpreadsheet3,
					trans.tslt(templateLanguage,"Self/Supervisor"),rowFreq,
					columnFreq);
			OO.insertString(xSpreadsheet3,trans.tslt(templateLanguage,"Peers"),
					rowFreq,columnFreq + 6);
			// combine direct and indirect if there is a <3 in direct category
			if (!combineDIRIDR) {
				OO.insertString(xSpreadsheet3,
						trans.tslt(templateLanguage,"Direct Reports"),rowFreq,
						columnFreq + 12);
				OO.insertString(xSpreadsheet3,
						trans.tslt(templateLanguage,"Indirect Reports"),
						rowFreq,columnFreq + 18);
			} else {
				OO.insertString(xSpreadsheet3,
						trans.tslt(templateLanguage,"Subordinate Reports"),
						rowFreq,columnFreq + 12);
			}
			OO.mergeCells(xSpreadsheet3,columnFreq,columnFreq + 5,rowFreq,
					rowFreq);
			OO.setCellAllignment(xSpreadsheet3,columnFreq,columnFreq + 5,
					rowFreq,rowFreq,1,2);
			OO.mergeCells(xSpreadsheet3,columnFreq + 6,columnFreq + 11,rowFreq,
					rowFreq);
			OO.setCellAllignment(xSpreadsheet3,columnFreq + 6,columnFreq + 11,
					rowFreq,rowFreq,1,2);
			OO.mergeCells(xSpreadsheet3,columnFreq + 12,columnFreq + 17,
					rowFreq,rowFreq);
			OO.setCellAllignment(xSpreadsheet3,columnFreq + 12,columnFreq + 17,
					rowFreq,rowFreq,1,2);
			if (!combineDIRIDR) {
				OO.mergeCells(xSpreadsheet3,columnFreq + 18,columnFreq + 23,
						rowFreq,rowFreq);
				OO.setCellAllignment(xSpreadsheet3,columnFreq + 18,
						columnFreq + 23,rowFreq,rowFreq,1,2);
			}
			rowFreq++;
			OO.insertString(xSpreadsheet3,trans.tslt(templateLanguage,"1"),
					rowFreq,columnFreq);
			OO.insertString(xSpreadsheet3,trans.tslt(templateLanguage,"2"),
					rowFreq,columnFreq + 1);
			OO.insertString(xSpreadsheet3,trans.tslt(templateLanguage,"3"),
					rowFreq,columnFreq + 2);
			OO.insertString(xSpreadsheet3,trans.tslt(templateLanguage,"4"),
					rowFreq,columnFreq + 3);
			OO.insertString(xSpreadsheet3,trans.tslt(templateLanguage,"5"),
					rowFreq,columnFreq + 4);
			OO.insertString(xSpreadsheet3,trans.tslt(templateLanguage,"6"),
					rowFreq,columnFreq + 5);
			OO.insertString(xSpreadsheet3,trans.tslt(templateLanguage,"1"),
					rowFreq,columnFreq + 6);
			OO.insertString(xSpreadsheet3,trans.tslt(templateLanguage,"2"),
					rowFreq,columnFreq + 7);
			OO.insertString(xSpreadsheet3,trans.tslt(templateLanguage,"3"),
					rowFreq,columnFreq + 8);
			OO.insertString(xSpreadsheet3,trans.tslt(templateLanguage,"4"),
					rowFreq,columnFreq + 9);
			OO.insertString(xSpreadsheet3,trans.tslt(templateLanguage,"5"),
					rowFreq,columnFreq + 10);
			OO.insertString(xSpreadsheet3,trans.tslt(templateLanguage,"6"),
					rowFreq,columnFreq + 11);
			OO.insertString(xSpreadsheet3,trans.tslt(templateLanguage,"1"),
					rowFreq,columnFreq + 12);
			OO.insertString(xSpreadsheet3,trans.tslt(templateLanguage,"2"),
					rowFreq,columnFreq + 13);
			OO.insertString(xSpreadsheet3,trans.tslt(templateLanguage,"3"),
					rowFreq,columnFreq + 14);
			OO.insertString(xSpreadsheet3,trans.tslt(templateLanguage,"4"),
					rowFreq,columnFreq + 15);
			OO.insertString(xSpreadsheet3,trans.tslt(templateLanguage,"5"),
					rowFreq,columnFreq + 16);
			OO.insertString(xSpreadsheet3,trans.tslt(templateLanguage,"6"),
					rowFreq,columnFreq + 17);
			if (!combineDIRIDR) {
				OO.insertString(xSpreadsheet3,trans.tslt(templateLanguage,"1"),
						rowFreq,columnFreq + 18);
				OO.insertString(xSpreadsheet3,trans.tslt(templateLanguage,"2"),
						rowFreq,columnFreq + 19);
				OO.insertString(xSpreadsheet3,trans.tslt(templateLanguage,"3"),
						rowFreq,columnFreq + 20);
				OO.insertString(xSpreadsheet3,trans.tslt(templateLanguage,"4"),
						rowFreq,columnFreq + 21);
				OO.insertString(xSpreadsheet3,trans.tslt(templateLanguage,"5"),
						rowFreq,columnFreq + 22);
				OO.insertString(xSpreadsheet3,trans.tslt(templateLanguage,"6"),
						rowFreq,columnFreq + 23);
			}
			rowFreq++;
			columnFreq = startColumnFreq;
			// Insert competency now
			Vector vComp = ClusterCompetencyByName(clusterID);
			vComp = sortClusterCompetencyOrder(vComp);
			String compName = "";
			int compID;
			for (int j = 0; j < vComp.size(); j++) {
				columnFreq = startColumnFreq;
				voCompetency voComp = (voCompetency) vComp.elementAt(j);
				compName = voComp.getCompetencyName();
				compID = voComp.getCompetencyID();
				OO.insertString(xSpreadsheet3,
						trans.tslt(templateLanguage,compName),rowFreq,
						columnFreq);
				// insert the frequency for SELF/Supervisor
				// columnFreq++;
				int selfScore = getSelfScore(compID);
				for (int k = 1; k <= 6; k++) {
					if (selfScore == k) {
						OO.setBGColor(xSpreadsheet3,columnFreq + k,columnFreq
								+ k,rowFreq,rowFreq,16774400);
					}
					int scoreFreq = getFreqScore(compID,k,"SUP");
					if (scoreFreq != 0) {
						OO.insertString(xSpreadsheet3,
								trans.tslt(templateLanguage,"" + scoreFreq),
								rowFreq,columnFreq + k);
					}
				}
				columnFreq += 6;
				// insert frequency for Peers
				for (int k = 1; k <= 6; k++) {
					int scoreFreq = getFreqScore(compID,k,"PEER");
					if (scoreFreq != 0) {
						OO.insertString(xSpreadsheet3,
								trans.tslt(templateLanguage,"" + scoreFreq),
								rowFreq,columnFreq + k);
					}
				}
				columnFreq += 6;
				// insert frequency for Direct Reports
				if (!combineDIRIDR) {
					for (int k = 1; k <= 6; k++) {
						int scoreFreq = getFreqScore(compID,k,"DIR");
						if (scoreFreq != 0) {
							OO.insertString(
									xSpreadsheet3,
									trans.tslt(templateLanguage,"" + scoreFreq),
									rowFreq,columnFreq + k);
						}
					}
					columnFreq += 6;
					// insert frequency for Peers
					for (int k = 1; k <= 6; k++) {
						int scoreFreq = getFreqScore(compID,k,"IDR");
						if (scoreFreq != 0) {
							OO.insertString(
									xSpreadsheet3,
									trans.tslt(templateLanguage,"" + scoreFreq),
									rowFreq,columnFreq + k);
						}
					}
				} else {
					for (int k = 1; k <= 6; k++) {
						int scoreFreqDIR = getFreqScore(compID,k,"DIR");
						int scoreFreqIDR = getFreqScore(compID,k,"IDR");
						int total = scoreFreqDIR + scoreFreqIDR;
						if (total != 0) {
							OO.insertString(xSpreadsheet3,
									trans.tslt(templateLanguage,"" + total),
									rowFreq,columnFreq + k);
						}
					}
				}
				rowFreq++;
			}
			if (!combineDIRIDR) {
				OO.setTableBorder(xSpreadsheet3,startColumnFreq,endColumnFreq,
						startBorder,rowFreq - 1,true,true,true,true,true,true);
			} else {
				OO.setTableBorder(xSpreadsheet3,startColumnFreq,
						endColumnFreq - 6,startBorder,rowFreq - 1,true,true,
						true,true,true,true);
			}

			rowFreq++; // restart next cluster
		}
	}

	/**
	 * Retrieves result entered by SELF.
	 */
	public int getSelfScore(int compID) throws SQLException {
		String query = "";

		query = query
				+ "SELECT tblAssignment.AssignmentID, tblAssignment.RaterCode, tblResultCompetency.Result ";
		query = query
				+ "FROM tblAssignment INNER JOIN tblResultCompetency ON tblAssignment.AssignmentID = tblResultCompetency.AssignmentID ";
		query = query + "WHERE tblAssignment.SurveyID = " + surveyID
				+ " AND tblAssignment.TargetLoginID = " + targetID + " ";
		query = query + "AND tblResultCompetency.CompetencyID = " + compID
				+ " AND tblResultCompetency.RatingTaskID = 1 ";
		query = query + "AND tblAssignment.RaterCode LIKE '%SELF%'";

		Connection con = null;
		Statement st = null;
		ResultSet rs = null;
		int result = -1;
		try {
			con = ConnectionBean.getConnection();
			st = con.createStatement();
			rs = st.executeQuery(query);
			if (rs.next()) {
				result = rs.getInt("Result");
			}
		} catch (Exception ex) {
			System.out.println("SPFIndividualReport.java - getSelfScore - "
					+ ex.getMessage());
		} finally {
			ConnectionBean.closeRset(rs); // Close ResultSet
			ConnectionBean.closeStmt(st); // Close statement
			ConnectionBean.close(con); // Close connection
		}

		return result;
	}

	/**
	 * Retrieves frequency of certain result entered by ceratin rater type.
	 */
	public int getFreqScore(int compID, int score, String raterCode)
			throws SQLException {
		String query = "";

		query = query + "SELECT COUNT(tblAssignment.RaterCode) AS Freq ";
		query = query
				+ "FROM tblAssignment INNER JOIN tblResultCompetency ON tblAssignment.AssignmentID = tblResultCompetency.AssignmentID ";
		query = query + "WHERE tblAssignment.SurveyID = " + surveyID
				+ " AND tblAssignment.TargetLoginID = " + targetID + " ";
		query = query + "AND tblResultCompetency.CompetencyID = " + compID
				+ " AND tblResultCompetency.RatingTaskID = 1 ";
		query = query + "AND tblAssignment.RaterCode LIKE '%" + raterCode
				+ "%' AND tblResultCompetency.Result = " + score;

		Connection con = null;
		Statement st = null;
		ResultSet rs = null;
		int result = -1;
		try {
			con = ConnectionBean.getConnection();
			st = con.createStatement();
			rs = st.executeQuery(query);
			if (rs.next()) {
				result = rs.getInt("Freq");
			}
		} catch (Exception ex) {
			System.out.println("SPFIndividualReport.java - getFreqScore - "
					+ ex.getMessage());
		} finally {
			ConnectionBean.closeRset(rs); // Close ResultSet
			ConnectionBean.closeStmt(st); // Close statement
			ConnectionBean.close(con); // Close connection
		}

		return result;
	}

	/*
	 * Added this method to dynamically add information to the Individual Report
	 * Information (Interpreting Report) By Albert 7 Sept 2012
	 */
	public void AddDynamicReportInfo() throws Exception {
		int surveyLevel = Integer.parseInt(surveyInfo[0]);
		double MinMaxGap[] = getMinMaxGap();
		double low = MinMaxGap[0];
		double high = MinMaxGap[1];
		if (iNoCPR == 1) { // if CP Only (no CPR)
			if (surveyLevel == 0) { // Competency Level
				String interpretingCompReport = "This section provides the details of the ratings obtained for each of the competencies. The first graph is the aggregated score for the first competency (e.g. Personal Mastery). Following that is another competency.";
				String interpretingCompReport2 = "For competency level survey, the score obtained for the 'All' category is an average score of ratings from all the raters. Similarly, you cannot simply take the average scores from peers, subordinates and superior to arrive at the average for the Ò�llÒ category. This is because different categories have different number of raters.";
				OO.findAndReplace(xSpreadsheet,"<Interpreting Comp Report>",
						interpretingCompReport);
				OO.findAndReplace(xSpreadsheet,"<Interpreting Comp Report 2>",
						interpretingCompReport2);
				OO.findAndReplace(xSpreadsheet,"<and behaviours>","");
			} else { // KB Level Survey
				String interpretingCompReport = "This section provides the details of the ratings obtained for each of the competencies and their corresponding key behaviours. The first graph is the aggregated score for the first competency (e.g. Developing Others) followed by ratings obtained for each of the 5 key behaviours under Developing Others. Following that is another competency and its corresponding key behaviours.";
				String interpretingCompReport2 = "For Key Behaviour Level survey, the ratings presented for the competency is a roll-up of the scores obtained for each key behaviour from the different raters. Hence, you cannot simply take the average scores from peers, subordinates and superior to arrive at the average for the Ò�llÒ category. This is because different categories have different number of raters. The Ò�llÒ category score was derived by averaging the raw scores of all the raters. Other than looking at the aggregated competency graph, the ratings obtained for each of its corresponding behaviour is also important as it highlights your weaker behaviour within the competency, if any. ";
				String andBehaviours = "and behaviours";
				OO.findAndReplace(xSpreadsheet,"<Interpreting Comp Report>",
						interpretingCompReport);
				OO.findAndReplace(xSpreadsheet,"<Interpreting Comp Report 2>",
						interpretingCompReport2);
				OO.findAndReplace(xSpreadsheet,"<and behaviours>",andBehaviours);
			}
		} else if (iNoCPR == 0) { // if has CPR
			if (surveyLevel == 0) { // Competency Level
				String interpretingCompReport = "This section provides the details of the ratings obtained for each of the competencies. The first graph is the aggregated score for the first competency (e.g. Personal Mastery). Following that is another competency.";
				String interpretingCompReport2 = "For competency level survey, the score obtained for the 'All' category is an average score of ratings from all the raters. Similarly, you cannot simply take the average scores from peers, subordinates and superior to arrive at the average for the Ò�llÒ category. This is because different categories have different number of raters.";
				OO.findAndReplace(xSpreadsheet,"<Interpreting Comp Report>",
						interpretingCompReport);
				OO.findAndReplace(xSpreadsheet,"<Interpreting Comp Report 2>",
						interpretingCompReport2);
				OO.findAndReplace(xSpreadsheet,"<and behaviours>","");
				OO.findAndReplace(xSpreadsheet,"<High Gap>","Gap >= " + high);
				OO.findAndReplace(xSpreadsheet,"<Mid Gap>","" + low
						+ " < Gap < " + high);
				OO.findAndReplace(xSpreadsheet,"<Low Gap>","Gap <= " + low);
			} else { // KB Level Survey
				String interpretingCompReport = "The score obtained for the 'All' category is an average score of ratings from all the raters. Similarly, you cannot simply take the average scores from peers, subordinates and superior to arrive at the average for the Ò�llÒ category. This is because different categories have different number of raters.";
				String interpretingCompReport2 = "The ratings presented for the competency is a roll-up of the scores obtained for each key behaviour from the different raters. Hence, you cannot simply take the average scores from peers, subordinates and superior to arrive at the average for the Ò�llÒ category. This is because different categories have different number of raters. The Ò�llÒ category score was derived by averaging the raw scores of all the raters. Other than looking at the aggregated competency graph, the ratings obtained for each of its corresponding behaviour is also important as it highlights your weaker behaviour within the competency, if any.";
				String andBehaviours = "and behaviours";
				OO.findAndReplace(xSpreadsheet,"<Interpreting Comp Report>",
						interpretingCompReport);
				OO.findAndReplace(xSpreadsheet,"<Interpreting Comp Report 2>",
						interpretingCompReport2);
				OO.findAndReplace(xSpreadsheet,"<and behaviours>",andBehaviours);
				OO.findAndReplace(xSpreadsheet,"<High Gap>","Gap >= " + high);
				OO.findAndReplace(xSpreadsheet,"<Mid Gap>","" + low
						+ " < Gap < " + high);
				OO.findAndReplace(xSpreadsheet,"<Low Gap>","Gap <= " + low);
			}
		}
	}

	public void InsertPrelimQuestions() {
		try {
			// insert the additional questions header
			int[] address = OO.findString(xSpreadsheet,"<PrelimQns>");

			column = address[0];
			row = address[1];

			OO.findAndReplace(xSpreadsheet,"<PrelimQns>","");
			// OO.insertPageBreak(xSpreadsheet, startColumn, endColumn, row);
			row++;
			OO.mergeCells(xSpreadsheet,column,column + 7,row,row);
			OO.insertString(xSpreadsheet,
					trans.tslt(templateLanguage,"PRELIMINARY QUESTIONS"),row,
					column);
			OO.setFontSize(xSpreadsheet,startColumn,endColumn,row,row,16);
			OO.setFontBold(xSpreadsheet,startColumn,endColumn,row,row);
			row++;
			row++;
			OO.insertString(xSpreadsheet,
					trans.tslt(templateLanguage,"No. of responses"),row,
					column + 10);
			OO.setFontBold(xSpreadsheet,startColumn,endColumn,row,row);
			OO.mergeCells(xSpreadsheet,column + 10,column + 11,row,row);
			row++;

			// loop through all questions in this survey
			PrelimQuestionController pqController = new PrelimQuestionController();
			Vector<PrelimQuestion> questions = pqController
					.getQuestions(surveyID);
			for (int i = 0; i < questions.size(); i++) {
				PrelimQuestion qn = questions.get(i);
				int ratingScaleId = qn.getPrelimRatingScaleId();
				// insert the question into the report and format it
				OO.mergeCells(xSpreadsheet,column,column + 9,row,row);
				OO.setFontSize(xSpreadsheet,column,column + 9,row,row,12);
				OO.insertString(xSpreadsheet,qn.getQuestion(),row,column);
				OO.setFontBold(xSpreadsheet,startColumn,endColumn,row,row);
				// OO.wrapText(xSpreadsheet, column, column + 9, row, row);
				OO.setRowHeight(xSpreadsheet,row,1,1120);
				OO.setCellAllignment(xSpreadsheet,startColumn,column + 9,row,
						row,2,1); // Set alignment of competency name to
									// top, Desmond 16 Nov 09
				row += 2;

				// insert the rating scale and answers
				if (ratingScaleId != -1) { // using rating scale

					Vector scales = pqController
							.getAllRatingInVector(ratingScaleId);
					Vector ratingAns = pqController.getRatingAnswers(
							qn.getPrelimQnId(),targetID);
					for (int a = 0; a < scales.size(); a++) {
						String ratingScale = (String) scales.get(a);
						int result = 0;
						for (int b = 0; b < ratingAns.size(); b++) {
							if (ratingScale.replaceAll("\\s","").equals(
									(String) ratingAns.get(b)))
								result++;
						}
						OO.setFontNormal(xSpreadsheet,0,10,row,row);
						OO.mergeCells(xSpreadsheet,column + 1,column + 3,row,
								row);
						OO.insertString(xSpreadsheet,"" + ratingScale,row,
								column + 1);
						OO.mergeCells(xSpreadsheet,column + 10,column + 11,row,
								row);
						OO.insertString(xSpreadsheet,"" + result,row,
								column + 10);
						OO.setRowHeight(xSpreadsheet,row,1,
								ROWHEIGHT * OO.countTotalRow(ratingScale,90));
						OO.setCellAllignment(xSpreadsheet,column + 10,
								column + 11,row,row,1,2); // Set alignment
															// of competency
															// name to top,
															// Desmond 16
															// Nov 09
						// OO.mergeCells(xSpreadsheet, column + 10, column + 11,
						// row, row);
						OO.setTableBorder(xSpreadsheet,column + 10,column + 11,
								row,row,false,false,true,true,true,true);
						row++;
					}
					row++;
				}
			}

		} catch (Exception e) {

		}
	}

	public void InsertSuveyOverview(int surveyID, int targetID) {
		try {
			// insert the additional questions header
			int[] address = OO.findString(xSpreadsheet,"<SurveyOverview>");

			column = address[0];
			row = address[1];
			int startRow = row;
			OO.findAndReplace(xSpreadsheet,"<SurveyOverview>","");
			// OO.insertPageBreak(xSpreadsheet, startColumn, endColumn, row);

			OO.mergeCells(xSpreadsheet,column,column + 2,row,row + 1);
			OO.insertString(xSpreadsheet,
					trans.tslt(templateLanguage,"Relationship"),row,column);
			// OO.setFontSize(xSpreadsheet, startColumn, endColumn, row, row,
			// 16);

			OO.mergeCells(xSpreadsheet,column + 3,column + 6,row,row);
			OO.insertString(xSpreadsheet,
					trans.tslt(templateLanguage,"Questionnaire"),row,column + 3);

			OO.mergeCells(xSpreadsheet,column + 7,column + 10,row,row);

			OO.insertString(xSpreadsheet,
					trans.tslt(templateLanguage,"Familiarity"),row,column + 7);

			row++;

			OO.mergeCells(xSpreadsheet,column + 3,column + 4,row,row);
			OO.insertString(xSpreadsheet,
					trans.tslt(templateLanguage,"Distributed"),row,column + 3);
			OO.mergeCells(xSpreadsheet,column + 5,column + 6,row,row);
			OO.insertString(xSpreadsheet,
					trans.tslt(templateLanguage,"Received"),row,column + 5);

			OO.mergeCells(xSpreadsheet,column + 7,column + 10,row,row);

			OO.insertString(xSpreadsheet,
					trans.tslt(templateLanguage,"(Maximum Score =5)"),row,
					column + 7);

			/*
			 * Calculate the number of groups that rated on this person
			 */
			row++;

			Vector numberOfGroupsOfUsers = getSurveyOverViewGroups(surveyID,
					targetID);
			for (Object i : numberOfGroupsOfUsers) {
				System.out.println(i);
			}
			String raterCode = "";
			String groupName = "";
			int self = -1;

			String newSubGroup = "Subordinates";
			int[] dir = getSurveyRaterStatus(targetID,"DIR%");
			int[] idr = getSurveyRaterStatus(targetID,"IDR%");
			boolean checkSplit = false;

			if (dir[0] < 3 || idr[0] < 3) {
				checkSplit = true;
			}

			int newNumOfRater = dir[0] + idr[0];
			int newRatedNumOfRaters = dir[1] + idr[1];
			boolean skipIdr = false;
			for (int i = 0; i < numberOfGroupsOfUsers.size(); i++) {

				int type = (Integer) numberOfGroupsOfUsers.elementAt(i);
				if (type == 2) {
					raterCode = "SUP%";
					groupName = "Superior";
				} else if (type == 4) {
					raterCode = "SELF";
					groupName = "Self";
					self = 1;
					continue;
				} else if (type == 6) {
					raterCode = "PEER%";
					groupName = "Peers";
				} else if (type == 8) {
					raterCode = "DIR%";
					groupName = "Direct";
				} else if (type == 9) {
					raterCode = "IDR%";
					groupName = "Indirect";
				} else if (type == 1 || type == 10) {
					continue;
				}
				int[] distribution = getSurveyRaterStatus(targetID,raterCode);

				if (distribution[0] != 0) {
					// condition where there is no need to split direct and
					// indirect reports
					if (checkSplit == false) {
						OO.mergeCells(xSpreadsheet,column,column + 2,row,row);
						OO.insertString(xSpreadsheet,groupName,row,column);
						OO.mergeCells(xSpreadsheet,column + 3,column + 4,row,
								row);
						OO.insertString(xSpreadsheet,
								String.valueOf(distribution[0]),row,column + 3);
						OO.mergeCells(xSpreadsheet,column + 5,column + 6,row,
								row);
						OO.insertString(xSpreadsheet,
								String.valueOf(distribution[1] / 2),row,
								column + 5);

						PrelimQuestionController pqc = new PrelimQuestionController();
						Vector<PrelimQuestion> pq = pqc.getQuestions(surveyID);
						double averageOfAllQuestions = 0;
						for (int j = 0; j < pq.size(); j++) {
							PrelimQuestion prelimQn = pq.elementAt(j);
							double prelimQnAns = pqc
									.getReportAnswersScore(raterCode,
											prelimQn.getPrelimQnId(),targetID);

							averageOfAllQuestions += prelimQnAns;
						}

						OO.mergeCells(xSpreadsheet,column + 7,column + 10,row,
								row);

						
							OO.insertString(xSpreadsheet,
									String.valueOf(df
											.format(averageOfAllQuestions
													/ pq.size())),row,
									column + 7);
					

					} else {
						// conditions where there is a need to split. Group is
						//direct
						if (groupName.equals("Direct")) {

							groupName = newSubGroup;

							PrelimQuestionController pqc = new PrelimQuestionController();
							Vector<PrelimQuestion> pq = pqc
									.getQuestions(surveyID);
							double averageOfAllQuestions = 0;
							for (int j = 0; j < pq.size(); j++) {
								PrelimQuestion prelimQn = pq.elementAt(j);
								double prelimQnAnsDir = pqc
										.getReportAnswersScore("DIR%",
												prelimQn.getPrelimQnId(),
												targetID);
								double prelimQnAnsIdr = pqc
										.getReportAnswersScore("IDR%",
												prelimQn.getPrelimQnId(),
												targetID);
								if ((prelimQnAnsIdr > 0)) {
									averageOfAllQuestions += (prelimQnAnsIdr);
								}
								if ((prelimQnAnsDir > 0)) {
									averageOfAllQuestions += (prelimQnAnsDir);
								}
							}

							OO.mergeCells(xSpreadsheet,column,column + 2,row,
									row);
							OO.insertString(xSpreadsheet,groupName,row,column);
							OO.mergeCells(xSpreadsheet,column + 3,column + 4,
									row,row);
							OO.insertString(xSpreadsheet,
									String.valueOf(newNumOfRater),row,
									column + 3);
							OO.mergeCells(xSpreadsheet,column + 5,column + 6,
									row,row);
							OO.insertString(xSpreadsheet,
									String.valueOf(newRatedNumOfRaters / 2),
									row,column + 5);

							OO.mergeCells(xSpreadsheet,column + 7,column + 10,
									row,row);

							
								OO.insertString(xSpreadsheet,String.valueOf(df
										.format(averageOfAllQuestions
												/ (pq.size() * 2))),row,
										column + 7);

							
							skipIdr = true;
						} else if (groupName.equals("Indirect")) {
							//if group is indirect 
							if (!skipIdr) {
								//if individual does not have a direct group
								groupName = newSubGroup;

								PrelimQuestionController pqc = new PrelimQuestionController();
								Vector<PrelimQuestion> pq = pqc
										.getQuestions(surveyID);
								double averageOfAllQuestions = 0;
								for (int j = 0; j < pq.size(); j++) {
									PrelimQuestion prelimQn = pq.elementAt(j);
									double prelimQnAnsDir = pqc
											.getReportAnswersScore("DIR%",
													prelimQn.getPrelimQnId(),
													targetID);
									double prelimQnAnsIdr = pqc
											.getReportAnswersScore("IDR%",
													prelimQn.getPrelimQnId(),
													targetID);
									if ((prelimQnAnsIdr > 0)) {
										averageOfAllQuestions += (prelimQnAnsIdr);
									}
									if ((prelimQnAnsDir > 0)) {
										averageOfAllQuestions += (prelimQnAnsDir);
									}
								}

								OO.mergeCells(xSpreadsheet,column,column + 2,
										row,row);
								OO.insertString(xSpreadsheet,groupName,row,
										column);
								OO.mergeCells(xSpreadsheet,column + 3,
										column + 4,row,row);
								OO.insertString(xSpreadsheet,
										String.valueOf(newNumOfRater),row,
										column + 3);
								OO.mergeCells(xSpreadsheet,column + 5,
										column + 6,row,row);
								OO.insertString(
										xSpreadsheet,
										String.valueOf(newRatedNumOfRaters / 2),
										row,column + 5);

								OO.mergeCells(xSpreadsheet,column + 7,
										column + 10,row,row);

							
									OO.insertString(
											xSpreadsheet,
											String.valueOf(df
													.format(averageOfAllQuestions
															/ (pq.size() * 2))),
											row,column + 7);

							
							} else {
								//if group already went past the direct group.
								continue;

							}
						} else {
							System.out.println("gn " + groupName);
							OO.mergeCells(xSpreadsheet,column,column + 2,row,
									row);
							OO.insertString(xSpreadsheet,groupName,row,column);
							OO.mergeCells(xSpreadsheet,column + 3,column + 4,
									row,row);
							OO.insertString(xSpreadsheet,
									String.valueOf(distribution[0]),row,
									column + 3);
							OO.mergeCells(xSpreadsheet,column + 5,column + 6,
									row,row);
							OO.insertString(xSpreadsheet,
									String.valueOf(distribution[1] / 2),row,
									column + 5);

							PrelimQuestionController pqc = new PrelimQuestionController();
							Vector<PrelimQuestion> pq = pqc
									.getQuestions(surveyID);
							double averageOfAllQuestions = 0;
							for (int j = 0; j < pq.size(); j++) {
								PrelimQuestion prelimQn = pq.elementAt(j);
								double prelimQnAns = pqc.getReportAnswersScore(
										raterCode,prelimQn.getPrelimQnId(),
										targetID);

								averageOfAllQuestions += prelimQnAns;
							}

							OO.mergeCells(xSpreadsheet,column + 7,column + 10,
									row,row);

							if (groupName.equalsIgnoreCase("Self")) {

								OO.insertString(xSpreadsheet,"N.A.",row,
										column + 7);
							} else {
								OO.insertString(xSpreadsheet,String.valueOf(df
										.format(averageOfAllQuestions
												/ pq.size())),row,column + 7);

							}
						}
					}
				} else {
					/*
					 * handle in the event that the individual is not rated by a
					 * particular group of raters
					 */
					OO.mergeCells(xSpreadsheet,column,column + 2,row,row);
					OO.insertString(xSpreadsheet,groupName,row,column);
					OO.mergeCells(xSpreadsheet,column + 3,column + 4,row,row);
					OO.insertString(xSpreadsheet,"0",row,column + 3);
					OO.mergeCells(xSpreadsheet,column + 5,column + 6,row,row);
					OO.insertString(xSpreadsheet,"0",row,column + 5);
					OO.mergeCells(xSpreadsheet,column + 7,column + 10,row,row);
					OO.insertString(xSpreadsheet,"N.A.",row,column + 7);
				}

				row++;
			}
		
				OO.mergeCells(xSpreadsheet,column,column + 2,row,row);
				OO.insertString(xSpreadsheet,"Self",row,column);
				OO.mergeCells(xSpreadsheet,column + 3,column + 4,row,row);
				OO.insertString(xSpreadsheet,"1",row,column + 3);
				OO.mergeCells(xSpreadsheet,column + 5,column + 6,row,row);
				OO.insertString(xSpreadsheet,"1",row,column + 5);
				OO.mergeCells(xSpreadsheet,column + 7,column + 10,row,row);
				OO.insertString(xSpreadsheet,"N.A.",row,column + 7);
			
			int endRow = row;

			OO.setCellAllignment(xSpreadsheet,0,11,startRow - 1,endRow,1,2);
			OO.setFontSize(xSpreadsheet,0,11,startRow - 1,endRow,12);
			OO.setTableBorder(xSpreadsheet,0,10,startRow,endRow,true,true,true,
					true,true,true);

		} catch (Exception e) {

		}
	}

	public void InsertAdditionalQuestions() {
		try {
			// insert the additional questions header

			int firstRowOfPage = row;
			OO.insertString(xSpreadsheet,
					trans.tslt(templateLanguage,"ADDITIONAL QUESTIONS"),row,
					column);
			OO.setFontSize(xSpreadsheet,startColumn,endColumn,row,row,16);
			OO.setFontBold(xSpreadsheet,startColumn,endColumn,row,row);
			row += 2;
			int pageHeightLimit = 22272;// Page limit is 22272
			int currentPageHeight = 1076;
			// calculate the height of the table that is being added.

			AdditionalQuestionController aqController = new AdditionalQuestionController();
			Vector<AdditionalQuestion> questions = aqController
					.getQuestions(surveyID);
			for (int i = 0; i < questions.size(); i++) {
				OO.insertPageBreak(xSpreadsheet,startColumn,endColumn,row);
				if (i == 0) {

					OO.insertString(
							xSpreadsheet,
							trans.tslt(templateLanguage,"ADDITIONAL QUESTIONS"),
							row,column);
					OO.setFontSize(xSpreadsheet,startColumn,endColumn,row,row,
							16);
					OO.setFontBold(xSpreadsheet,startColumn,endColumn,row,row);
				}
				int startborder = row + 1;
				AdditionalQuestion qn = questions.get(i);

				// insert the question into the report and format it
				OO.insertString(xSpreadsheet,Integer.toString(i + 1) + ".",row,
						column);
				OO.insertString(xSpreadsheet,qn.getQuestion(),row,column + 1);
				OO.setFontBold(xSpreadsheet,startColumn,endColumn,row,row);
				OO.setBGColor(xSpreadsheet,startColumn,endColumn,row,row,
						BGCOLOR);
				OO.mergeCells(xSpreadsheet,column + 1,endColumn,row,row);
				OO.wrapText(xSpreadsheet,column + 1,endColumn,row,row);
				OO.setCellAllignment(xSpreadsheet,startColumn,endColumn,row,
						row,2,1); // Set alignment of competency name to top,
									// Desmond 16 Nov 09
				row++;

				// loop through the questions in the survey and categorize the
				// answers
				// according to the user type

				// get all the answers to the questions and segregate them
				// according to the type
				Vector<String> supAns = aqController.getReportAnswers("SUP%",
						qn.getAddQnId(),targetID);
				Vector<String> subAns = aqController.getReportAnswers("SUB%",
						qn.getAddQnId(),targetID);
				Vector<String> peerAns = aqController.getReportAnswers("PEER%",
						qn.getAddQnId(),targetID);
				Vector<String> selfAns = aqController.getReportAnswers("SELF%",
						qn.getAddQnId(),targetID);
				Vector<String> othAns = aqController.getReportAnswers("OTH%",
						qn.getAddQnId(),targetID);
				Vector<String> idrAns = aqController.getReportAnswers("IDR%",
						qn.getAddQnId(),targetID);
				Vector<String> dirAns = aqController.getReportAnswers("DIR%",
						qn.getAddQnId(),targetID);

				// print the headings and the answers
				if (supAns.size() > 0)// if there are comments by superiors
										// print them
				{
					OO.insertString(xSpreadsheet,
							trans.tslt(templateLanguage,"Superior"),row,
							column + 1); // Change from Supervisors to Superior,
											// Desmond 22 Oct 09
					OO.setFontBold(xSpreadsheet,startColumn,endColumn,row,row);
					OO.setFontItalic(xSpreadsheet,startColumn,endColumn,row,row);
					row++;
					insertAnswer((supAns));

				} else {
					OO.insertString(xSpreadsheet,
							trans.tslt(templateLanguage,"Superior"),row,
							column + 1);
					row++;
					OO.insertString(xSpreadsheet,"No Comments Provided.",row,
							column + 1);
					row += 2;
				}

				if (splitOthers == 1)
				// check to see if split others options is enabled
				{

					if (subAns.size() > 0)// if there are comments by
											// subordinates print them
					{
						// row++;
						OO.insertString(xSpreadsheet,
								trans.tslt(templateLanguage,"Subordinate(s)"),
								row,column + 1); // Change from Supervisors to
													// Superior, Desmond 22 Oct
													// 09
						OO.setFontBold(xSpreadsheet,startColumn,endColumn,row,
								row);
						OO.setFontItalic(xSpreadsheet,startColumn,endColumn,
								row,row);
						row++;
						insertAnswer(subAns);

					}

					if (peerAns.size() > 0) {
						// row++;
						OO.insertString(xSpreadsheet,
								trans.tslt(templateLanguage,"Peer(s)"),row,
								column + 1); // Change from Supervisors to
												// Superior, Desmond 22 Oct 09
						OO.setFontBold(xSpreadsheet,startColumn,endColumn,row,
								row);
						OO.setFontItalic(xSpreadsheet,startColumn,endColumn,
								row,row);
						row++;
						insertAnswer(peerAns);

					} else {
						OO.insertString(xSpreadsheet,
								trans.tslt(templateLanguage,"Peer(s)"),row,
								column + 1);
						row++;
						OO.insertString(xSpreadsheet,"No Comments Provided.",
								row,column + 1);
						row += 2;
					}
					if (!combineDIRIDR) {
						if (dirAns.size() > 0)// if there are comments by
												// superiors
						// print them
						{
							// row++;
							OO.insertString(xSpreadsheet,trans.tslt(
									templateLanguage,"Direct Report(s)"),row,
									column + 1); // Change from Supervisors to
													// Superior,
													// Desmond 22 Oct 09
							OO.setFontBold(xSpreadsheet,startColumn,endColumn,
									row,row);
							OO.setFontItalic(xSpreadsheet,startColumn,
									endColumn,row,row);
							row++;
							insertAnswer(dirAns);

						} else {
							OO.insertString(xSpreadsheet,trans.tslt(
									templateLanguage,"Direct Report(s)"),row,
									column + 1);
							row++;
							OO.insertString(xSpreadsheet,
									"No Comments Provided.",row,column + 1);
							row += 2;
						}
						OO.insertPageBreak(xSpreadsheet,startColumn,endColumn,
								row);
						OO.setTableBorder(xSpreadsheet,startColumn,endColumn,
								startborder,row - 1,false,false,true,true,true,
								true);

						if (idrAns.size() > 0)// if there are comments by
												// superiors
						// print them
						{
							// row++;
							OO.insertString(xSpreadsheet,trans.tslt(
									templateLanguage,"Indirect Report(s)"),row,
									column + 1); // Change from Supervisors to
													// Superior,
													// Desmond 22 Oct 09
							OO.setFontBold(xSpreadsheet,startColumn,endColumn,
									row,row);
							OO.setFontItalic(xSpreadsheet,startColumn,
									endColumn,row,row);
							row++;
							insertAnswer(idrAns);

						} else {
							OO.insertString(xSpreadsheet,trans.tslt(
									templateLanguage,"Indirect Reports(s)"),
									row,column + 1);
							row++;
							OO.insertString(xSpreadsheet,
									"No Comments Provided.",row,column + 1);
							row += 2;
						}
					} else {
						if (dirAns.size() > 0 || idrAns.size() > 0)// if there
																	// are
																	// comments
																	// by
																	// superiors
						// print them
						{
							// row++;
							OO.insertString(xSpreadsheet,trans.tslt(
									templateLanguage,"Subordinate Report(s)"),
									row,column + 1); // Change from Supervisors
														// to
														// Superior,
														// Desmond 22 Oct 09
							OO.setFontBold(xSpreadsheet,startColumn,endColumn,
									row,row);
							OO.setFontItalic(xSpreadsheet,startColumn,
									endColumn,row,row);
							row++;
							insertAnswer(dirAns);

							// row++;

							OO.setFontBold(xSpreadsheet,startColumn,endColumn,
									row,row);
							OO.setFontItalic(xSpreadsheet,startColumn,
									endColumn,row,row);
							row++;
							insertAnswer(idrAns);

						} else {
							OO.insertString(xSpreadsheet,trans.tslt(
									templateLanguage,"Subordinate Report(s)"),
									row,column + 1);
							row++;
							OO.insertString(xSpreadsheet,
									"No Comments Provided.",row,column + 1);
							row += 1;
						}
						OO.insertPageBreak(xSpreadsheet,startColumn,endColumn,
								row);
						OO.setTableBorder(xSpreadsheet,startColumn,endColumn,
								startborder,row - 1,false,false,true,true,true,
								true);
					}

				} else// split others option disabled
				{
					if (peerAns.size() > 0 || subAns.size() > 0
							|| othAns.size() > 0) {
						// row++;
						OO.insertString(xSpreadsheet,
								trans.tslt(templateLanguage,"Others"),row,
								column + 1); // Change from Supervisors to
												// Superior, Desmond 22 Oct 09
						OO.setFontBold(xSpreadsheet,startColumn,endColumn,row,
								row);
						OO.setFontItalic(xSpreadsheet,startColumn,endColumn,
								row,row);
						row++;
						if (peerAns.size() > 0)
							insertAnswer(peerAns);

						if (subAns.size() > 0)
							insertAnswer(subAns);

						if (othAns.size() > 0)
							insertAnswer(othAns);
					}

					if (!combineDIRIDR) {
						if (dirAns.size() > 0)// if there are comments by
												// superiors
						// print them
						{
							// row++;
							OO.insertString(xSpreadsheet,trans.tslt(
									templateLanguage,"Direct Report(s)"),row,
									column + 1); // Change from Supervisors to
													// Superior,
													// Desmond 22 Oct 09
							OO.setFontBold(xSpreadsheet,startColumn,endColumn,
									row,row);
							OO.setFontItalic(xSpreadsheet,startColumn,
									endColumn,row,row);
							row++;
							insertAnswer(dirAns);

						} else {
							OO.insertString(xSpreadsheet,trans.tslt(
									templateLanguage,"Direct Report(s)"),row,
									column + 1);
							row++;
							OO.insertString(xSpreadsheet,
									"No Comments Provided.",row,column + 1);
							row += 2;
						}
						OO.insertPageBreak(xSpreadsheet,startColumn,endColumn,
								row);
						OO.setTableBorder(xSpreadsheet,startColumn,endColumn,
								startborder,row - 1,false,false,true,true,true,
								true);

						if (idrAns.size() > 0)// if there are comments by
												// superiors
						// print them
						{
							// row++;
							OO.insertString(xSpreadsheet,trans.tslt(
									templateLanguage,"Indirect Report(s)"),row,
									column + 1); // Change from Supervisors to
													// Superior,
													// Desmond 22 Oct 09
							OO.setFontBold(xSpreadsheet,startColumn,endColumn,
									row,row);
							OO.setFontItalic(xSpreadsheet,startColumn,
									endColumn,row,row);
							row++;
							insertAnswer(idrAns);

						} else {
							OO.insertString(xSpreadsheet,trans.tslt(
									templateLanguage,"Indirect Reports(s)"),
									row,column + 1);
							row++;
							OO.insertString(xSpreadsheet,
									"No Comments Provided.",row,column + 1);
							row += 2;
						}
					} else {
						if (dirAns.size() > 0 || idrAns.size() > 0)// if there
																	// are
																	// comments
																	// by
																	// superiors
						// print them
						{
							// row++;
							OO.insertString(xSpreadsheet,trans.tslt(
									templateLanguage,"Subordinate Report(s)"),
									row,column + 1); // Change from Supervisors
														// to
														// Superior,
														// Desmond 22 Oct 09
							OO.setFontBold(xSpreadsheet,startColumn,endColumn,
									row,row);
							OO.setFontItalic(xSpreadsheet,startColumn,
									endColumn,row,row);
							row++;
							insertAnswer(dirAns);

							// row++;

							OO.setFontBold(xSpreadsheet,startColumn,endColumn,
									row,row);
							OO.setFontItalic(xSpreadsheet,startColumn,
									endColumn,row,row);
							row++;
							insertAnswer(idrAns);

						} else {
							OO.insertString(xSpreadsheet,trans.tslt(
									templateLanguage,"Subordinate Report(s)"),
									row,column + 1);
							row++;
							OO.insertString(xSpreadsheet,
									"No Comments Provided.",row,column + 1);
							row += 2;
						}
						OO.insertPageBreak(xSpreadsheet,startColumn,endColumn,
								row);
						OO.setTableBorder(xSpreadsheet,startColumn,endColumn,
								startborder,row - 1,false,false,true,true,true,
								true);
					}

				}

				if (selfAns.size() > 0)// if there are comments by superiors
										// print them
				{
					row++;
					OO.insertString(xSpreadsheet,
							trans.tslt(templateLanguage,"Self"),row,column + 1); // Change
																					// from
																					// Supervisors
																					// to
																					// Superior,
																					// Desmond
																					// 22
																					// Oct
																					// 09
					OO.setFontBold(xSpreadsheet,startColumn,endColumn,row,row);
					OO.setFontItalic(xSpreadsheet,startColumn,endColumn,row,row);
					row++;
					insertAnswer(selfAns);

				}

			}
		} catch (Exception e) {

		}
	}

	public void insertAnswer(Vector<String> vans) {
		for (int q = 0; q < vans.size(); q++) {
			try {
				OO.insertString(xSpreadsheet,
						"-" + UnicodeHelper.getUnicodeStringAmp(vans.get(q)),
						row,column + 1);
				OO.mergeCells(xSpreadsheet,column + 1,endColumn,row,row);
				OO.setRowHeight(xSpreadsheet,row,column + 1,
						ROWHEIGHT * OO.countTotalRow(vans.get(q),105));
				OO.setCellAllignment(xSpreadsheet,startColumn,startColumn,row,
						row,2,1);
				OO.justify(xSpreadsheet,0,11,row,row);
				row += 2;
			} catch (Exception e) {

			}

		}

	}

	/**
	 * Send generated individual report through email
	 * 
	 * @param sTargetName
	 * @param sSurveyName
	 * @param sFilename
	 * @author Maruli
	 */
	public void sendIndividualReport(String sTargetName, String sSurveyName,
			String sEmail, String sFilename, int surveyId) {
		String sHeader = "INDIVIDUAL REPORT OF " + sTargetName + " FOR "
				+ sSurveyName;

		try {
			// Edited By Roger 13 June 2008
			EMAIL.sendMail_with_Attachment(ST.getAdminEmail(),sEmail,sHeader,
					"",sFilename,getOrgId(surveyId));
		} catch (Exception E) {
			System.out.println("a " + E.getMessage());
		}
	}

	/**
	 * Send generated development map through email
	 * 
	 * @param sTargetName
	 * @param sSurveyName
	 * @param sFilename
	 * @author Maruli
	 */
	public void sendDevelopmentMap(String sTargetName, String sSurveyName,
			String sEmail, String sFilename, int surveyId) {
		String sHeader = "DEVELOPMENT MAP OF " + sTargetName + " FOR "
				+ sSurveyName;

		try {
			// Edited By Roger 13 June 2008
			EMAIL.sendMail_with_Attachment(ST.getAdminEmail(),sEmail,sHeader,
					"",sFilename,getOrgId(surveyId));
		} catch (Exception E) {
			System.out.println("a " + E.getMessage());
		}
	}

	public void setCancelPrint(int iVar) {
		iCancel = iVar;
	}

	public int getCancelPrint() {
		return iCancel;
	}

	// Edited By Roger 13 June 2008
	// Get Org ID From SurveyID
	public int getOrgId(int surveyId) {

		Connection con = null;
		Statement st = null;
		ResultSet rs = null;
		int orgId = 0;
		try {
			String sql = "SELECT FKOrganization FROM tblSurvey WHERE SurveyID="
					+ surveyId;

			con = ConnectionBean.getConnection();
			st = con.createStatement();
			rs = st.executeQuery(sql);

			if (rs.next()) {
				orgId = rs.getInt("FKOrganization");
			}
		} catch (Exception e) {

		} finally {
			ConnectionBean.closeRset(rs); // Close ResultSet
			ConnectionBean.closeStmt(st); // Close statement
			ConnectionBean.close(con); // Close connection
		}
		return orgId;
	}

	/**
	 * This method retrieves the translated competency name of the selected
	 * language
	 * 
	 * @param compName
	 * @return a vector: element(0) for translated competency name, element(1)
	 *         for translated competency definition
	 * @author Chun Yeong
	 * @since v1.3.12.113 //1 Aug 2011
	 */
	public Vector getTranslatedCompetency(String compName) {
		String competencyN = "", competencyD = "";
		String query = "";
		Vector translatedComp = new Vector();
		query = "SELECT CompetencyName, CompetencyDefinition, CompetencyName"
				+ language + ", CompetencyDefinition" + language
				+ " FROM Competency " + "WHERE CompetencyName = '"
				+ compName.replaceAll("'","''").trim() + "'";

		Connection con = null;
		Statement st = null;
		ResultSet rs = null;

		try {
			con = ConnectionBean.getConnection();
			st = con.createStatement();
			rs = st.executeQuery(query);
			while (rs.next()) {
				// Retrieve the translated competency name.
				// When the competency name is null, set to the default english
				// language
				competencyN = rs.getString("CompetencyName" + language);
				if (rs.wasNull())
					competencyN = rs.getString("CompetencyName");

				// Retrieve the translated competency definition
				// When the competency definition is null, set to the default
				// english language
				competencyD = rs.getString("CompetencyDefinition" + language);
				if (rs.wasNull())
					competencyD = rs.getString("CompetencyDefinition");

				translatedComp.add(competencyN);
				translatedComp.add(competencyD);
			}
		} catch (Exception ex) {
			System.out
					.println("SPFIndividualReport.java - getTranslatedCompetency - "
							+ ex.getMessage());
		} finally {
			ConnectionBean.closeRset(rs); // Close ResultSet
			ConnectionBean.closeStmt(st); // Close statement
			ConnectionBean.close(con); // Close connection
		}
		return translatedComp;
	}

	/**
	 * This method retrieves the translated key behaviour of the selected
	 * language
	 * 
	 * @param KB
	 * @return translated Key Behavior String
	 * @author Chun Yeong
	 * @since v1.3.12.113 //1 Aug 2011
	 */
	public String getTranslatedKeyBehavior(String KB) {
		String translatedKB = "";
		String query = "";
		query = "SELECT KeyBehaviour, KeyBehaviour" + language
				+ " FROM KeyBehaviour " + "WHERE KeyBehaviour = '"
				+ KB.replaceAll("'","''").trim() + "'";

		Connection con = null;
		Statement st = null;
		ResultSet rs = null;

		try {
			con = ConnectionBean.getConnection();
			st = con.createStatement();
			rs = st.executeQuery(query);
			while (rs.next()) {
				// Retrieve the translated key behaviour
				// When the key behaviour is null, set to the default english
				// language
				translatedKB = rs.getString("KeyBehaviour" + language);
				if (rs.wasNull())
					translatedKB = rs.getString("KeyBehaviour");
			}
		} catch (Exception ex) {
			System.out
					.println("SPFIndividualReport.java - getTranslatedKeyBehavior - "
							+ ex.getMessage());
		} finally {
			ConnectionBean.closeRset(rs); // Close ResultSet
			ConnectionBean.closeStmt(st); // Close statement
			ConnectionBean.close(con); // Close connection
		}

		return translatedKB;
	}

	public void InsertClusterBlindSpotAnalysis(Boolean positive)
			throws Exception {
		int surveyLevel = Integer.parseInt(surveyInfo[0]);

		int compCount = 1;
		int compID = 0;
		String compName = "";
		String clusterName = "";
		int clusterID = 0;
		int maxScale = MaxScale();

		column = 0;
		int startRow = row;
		int firstColumn = 0;
		// Added to define columns where positive, negative, others and self are
		// inserted in spreadsheet
		int posCol = 6;
		int negCol = 8;
		int othersCol = 10;
		int selfCol = 11;
		double othersValue = 0.0;
		double selfValue = 0.0;

		Vector vClust = ClusterByName();
		Vector vComp = new Vector();
		Vector<Vector> vCompDisplay = new Vector<Vector>();
		String[] Result = new String[4];

		int RTID = 0;
		String RTCode = "";
		for (int m = 0; m < vClust.size(); m++) {
			voCluster voClust = (voCluster) vClust.elementAt(m);
			clusterID = voClust.getClusterID();
			vCompDisplay.add(new Vector());
			vComp = ClusterCompetencyByName(clusterID);
			for (int i = 0; i < vComp.size(); i++) {
				Result = new String[4];

				voCompetency voComp = (voCompetency) vComp.elementAt(i);
				compID = voComp.getCompetencyID();
				compName = voComp.getCompetencyName();

				Vector RT = RatingTask();
				for (int j = 0; j < RT.size(); j++) {
					votblSurveyRating vo = (votblSurveyRating) RT.elementAt(j);
					RTID = vo.getRatingTaskID();
					RTCode = vo.getRatingCode();

					// RTID = 1, RTCode = CP
					// RTID = 2, RTCode = CPR

					Vector result = null;
					if (surveyLevel == 0) { // Competency level
						result = MeanResult(RTID,compID,0);
					} else { // KB level
						result = KBMean(RTID,compID);
					}

					if (RTCode.equals("CP")) {
						for (int k = 0; k < result.size(); k++) {
							String[] arr = (String[]) result.elementAt(k);

							// arr[0]: CompetencyID
							// arr[1]: Type
							// arr[2]: CAST(AVG(AvgMean) AS numeric(38, 2)) AS
							// Result
							int type = Integer.parseInt(arr[1]);
							int weighted = 1;
							if (weightedAverage == true) {
								weighted = 10;
							}
							if (type == weighted || type == 4) {
								Result[0] = compID + "";
								Result[1] = compName;
								if (type == weighted) {
									Result[2] = CompTrimmedMeanforAll(RTID,
											compID) + "";
								} else {
									Result[3] = Double.parseDouble(arr[2]) + "";
								}
							}
						}
						try {
							othersValue = Double.parseDouble(Result[2]);
						} catch (Exception ex) {
							othersValue = 0.0;
						}
						try {
							selfValue = Double.parseDouble(Result[3]);
						} catch (Exception ex) {
							selfValue = 0.0;
						}

						if (positive) {
							// 3 conditions: 1) others must be more than
							// (maxScale/2)
							// 2) both others and self results are not the same
							// 3) others must be more than self
							if (othersValue >= 4.5 && (othersValue > selfValue)
									&& (othersValue != selfValue)) {
								vCompDisplay.elementAt(m).add(Result);
							}
						} else if (!positive) {
							// 3 conditions: 1) self must be more than
							// (maxScale/2)
							// 2) both others and self results are not the same
							// 3) self must be more than others
							if (selfValue >= 4.5 && (selfValue > othersValue)
									&& (othersValue != selfValue)) {
								vCompDisplay.elementAt(m).add(Result);
							}
						}
					} // else if RTCode is CPR/FPR
				}
			}// End of for loop, Competency list
		}// end of for loop, cluster list
		/***********************
		 * Construct the table *
		 ***********************/
		boolean construct = false;
		for (int i = 0; i < vClust.size(); i++) {
			if (vCompDisplay.elementAt(i).size() != 0) {
				construct = true;
				break;
			}
		}
		if (construct) {
			int startBorder = 0;
			// First time entering here, if last page row count is = 0, or >=
			// row
			// Set the startBorder to the row

			if (lastPageRowCount == 0) {
				startBorder = row;
				lastPageRowCount = row;
			} else {
				startBorder = lastPageRowCount;
			}

			// Set up headers, Chun Yeong 1 Aug 2011
			// \u2264 writes 'Smaller than or Equals to' or <=
			// \u003E writes 'Greater than' or >
			String title = "";
			String titleDesc = "";
			if (positive) {
				title = trans.tslt(templateLanguage,"Positive Blind Spots");
				titleDesc = trans
						.tslt(templateLanguage,
								"Competencies/KB where others rated you better than what you rated yourself "
										+ "and where the rating from others is above the effective point "
										+ "of the scale used (i.e.")
						+ " "
						+ "4.5"
						+ " \u2264 "
						+ trans.tslt(templateLanguage,"Other's Rating")
						+ " \u003E "
						+ trans.tslt(templateLanguage,"Self Rating")
						+ "). Since Meet Expectations has been set at 4.5, only ratings from others which is above the effective point of the scale have been included in the analysis. Ratings below 4.5 have not been included because they would need your development attention since they did not Meet Expectations.";
			} else {
				title = trans.tslt(templateLanguage,"Negative Blind Spots");
				titleDesc = trans
						.tslt(templateLanguage,
								"Competencies/KB where you rated yourself better than others rated you "
										+ "and where your self rating is above the effective point "
										+ "of the scale used (i.e. ")
						+ " "
						+ "4.5"
						+ " \u2264 "
						+ trans.tslt(templateLanguage,"Self Rating")
						+ " \u003E "
						+ trans.tslt(templateLanguage,"Other's Rating") + "). ";
			}

			// To cater to if the negative table is starting on a new page
			// And reset the variables: startBorder, lastPageRowCount and
			// currentPageHeight
			// Check height and insert pagebreak where necessary
			/*
			 * How 22272 is derived? Cant be found online. Most probably trial
			 * and error by the past coders. Same for 1076 per page.
			 */
			int pageHeightLimit = 25200;// Page limit is 22272
			int currentPageHeight = 1076;
			// calculate the height of the table that is being added.
			for (int i = startBorder; i <= row; i++) {
				currentPageHeight += OO
						.getRowHeight(xSpreadsheet,i,startColumn);
			}

			// currentPageHeight += 1614;
			/*
			 * Reduce the limit from 3000 to 2000 as it is causing the page
			 * break to execute even though there is enough space for the table
			 * that is going to be printed on the following page.
			 */
			if ((startRow - row) > 45
			/* || ((pageHeightLimit - currentPageHeight) <= 500 */) {// adding
																		// the
																		// table
																		// will
																		// exceed
																		// a
																		// single
																		// page,
																		// insert
																		// page
																		// break
				// Draw the border
				OO.setTableBorder(xSpreadsheet,startColumn,endColumn - 1,
						row - 1,row - 1,false,false,false,false,false,true);
				// Insert page break
				OO.insertPageBreak(xSpreadsheet,startColumn,endColumn,row);
				startRow = row;
				// to move the table two lines down
				row += 2;
				// reset values
				startBorder = row;
				lastPageRowCount = row;
				currentPageHeight = 0;

			} // End of checking for page limit

			// Insert the title
			OO.insertString(xSpreadsheet,
					UnicodeHelper.getUnicodeStringAmp(title),row,firstColumn);
			OO.setFontBold(xSpreadsheet,startColumn,endColumn,row,row);
			OO.mergeCells(xSpreadsheet,startColumn,posCol - 1,row,row);

			row += 2;

			// Insert the description
			OO.insertString(xSpreadsheet,
					UnicodeHelper.getUnicodeStringAmp(titleDesc),row,
					firstColumn);
			OO.setRowHeight(xSpreadsheet,row,firstColumn,
					ROWHEIGHT * (OO.countTotalRow(titleDesc,105)));
			OO.justify(xSpreadsheet,firstColumn,11,row,row);
			OO.mergeCells(xSpreadsheet,startColumn,endColumn - 1,row,row);
			row += 2;

			// Insert 2 new labels CP and CPR before Gap and set font size to 12
			OO.insertString(xSpreadsheet,
					trans.tslt(templateLanguage,"Positive"),row,posCol);
			OO.mergeCells(xSpreadsheet,posCol,negCol - 1,row,row);
			OO.insertString(xSpreadsheet,
					trans.tslt(templateLanguage,"Negative"),row,negCol);
			OO.mergeCells(xSpreadsheet,negCol,othersCol - 1,row,row);
			OO.insertString(xSpreadsheet,trans.tslt(templateLanguage,"Others"),
					row,othersCol);
			OO.mergeCells(xSpreadsheet,othersCol,selfCol - 1,row,row);
			OO.insertString(xSpreadsheet,trans.tslt(templateLanguage,"Self"),
					row,selfCol);
			OO.mergeCells(xSpreadsheet,selfCol,endColumn - 1,row,row);

			OO.setFontBold(xSpreadsheet,startColumn,endColumn,row,row);
			OO.setFontSize(xSpreadsheet,posCol,selfCol,row,row,12);
			OO.setCellAllignment(xSpreadsheet,posCol,selfCol,row,row,1,2);
			OO.setTableBorder(xSpreadsheet,startColumn,posCol - 1,row,row,
					false,false,false,false,false,true);
			OO.setTableBorder(xSpreadsheet,posCol,negCol - 1,row,row,false,
					false,true,true,true,true);
			OO.setTableBorder(xSpreadsheet,negCol,othersCol - 1,row,row,false,
					false,true,true,true,true);
			OO.setTableBorder(xSpreadsheet,othersCol,endColumn - 1,row,row,
					true,false,true,true,true,true);

			row++;

			for (int m = 0; m < vClust.size(); m++) {
				voCluster voClust = (voCluster) vClust.elementAt(m);
				clusterName = voClust.getClusterName();

				// skip the cluster since it has no competency inside
				if (vCompDisplay.elementAt(m).size() == 0)
					continue;
				OO.setRowHeight(xSpreadsheet,row,0,560);
				OO.insertString(xSpreadsheet,clusterName.toUpperCase(),row,
						firstColumn);
				OO.mergeCells(xSpreadsheet,startColumn,posCol - 1,row,row);
				OO.setBGColor(xSpreadsheet,startColumn,endColumn - 1,row,row,
						BGCOLORCLUSTER);
				OO.setTableBorder(xSpreadsheet,firstColumn,firstColumn + 1,row,
						row,false,false,true,true,true,true);
				OO.setTableBorder(xSpreadsheet,startColumn,posCol - 1,row,row,
						false,false,false,false,false,true);
				OO.setTableBorder(xSpreadsheet,posCol,negCol - 1,row,row,false,
						false,true,true,true,true);
				OO.setTableBorder(xSpreadsheet,negCol,othersCol - 1,row,row,
						false,false,true,true,true,true);
				OO.setTableBorder(xSpreadsheet,othersCol,endColumn - 1,row,row,
						true,false,true,true,true,true);
				row++;
				compCount = 1;
				for (Object obj : vCompDisplay.elementAt(m)) {

					/****************************************************************************
					 * Check if line exceeds new page. If yes, insert page break
					 * * This section is similar to BOTTOM checking before
					 * printing key behavior. *
					 ****************************************************************************/
					// To cater to if the competency name is starting on a new
					// page
					// And reset the variables: startBorder, lastPageRowCount
					// and currentPageHeight
					// Check height and insert pagebreak where necessary

					pageHeightLimit = 25600;// Page limit is 22272
					// currentPageHeight = 1076;

					// calculate the height of the table that is being added.
					/*
					 * for (int i = startBorder; i <= row; i++) {
					 * currentPageHeight += OO.getRowHeight(xSpreadsheet, i,
					 * startColumn); }
					 */
					// currentPageHeight += 1076;

					/*
					 * Reduce the limit from 3000 to 2000 as it is causing the
					 * page break to execute even though there is enough space
					 * for the table that is going to be printed on the
					 * following page.
					 */
					if ((startRow - row) > 45
					/* || ((pageHeightLimit - currentPageHeight) <= 500) */) {// adding
						// the
						// table
						// will
						// exceed
						// a
						// single
						// page,
						// insert
						// page
						// break
						// Draw the border
						OO.setTableBorder(xSpreadsheet,startColumn,
								endColumn - 1,row - 1,row - 1,false,false,
								false,false,false,true);
						// Insert page break
						OO.insertPageBreak(xSpreadsheet,startColumn,endColumn,
								row);
						startRow = row;
						// to move the table two lines down
						row += 2;
						// reset values
						startBorder = row;
						lastPageRowCount = row;
						currentPageHeight = 0;

						// Insert 2 new labels CP and CPR before Gap and set
						// font size to 12
						OO.insertString(xSpreadsheet,
								trans.tslt(templateLanguage,"Positive"),row,
								posCol);
						OO.mergeCells(xSpreadsheet,posCol,negCol - 1,row,row);
						OO.insertString(xSpreadsheet,
								trans.tslt(templateLanguage,"Negative"),row,
								negCol);
						OO.mergeCells(xSpreadsheet,negCol,othersCol - 1,row,row);
						OO.insertString(xSpreadsheet,
								trans.tslt(templateLanguage,"Others"),row,
								othersCol);
						OO.mergeCells(xSpreadsheet,othersCol,selfCol - 1,row,
								row);
						OO.insertString(xSpreadsheet,
								trans.tslt(templateLanguage,"Self"),row,selfCol);
						OO.mergeCells(xSpreadsheet,selfCol,endColumn - 1,row,
								row);

						OO.setFontBold(xSpreadsheet,startColumn,endColumn,row,
								row);
						OO.setFontSize(xSpreadsheet,posCol,selfCol,row,row,12);
						OO.setCellAllignment(xSpreadsheet,posCol,selfCol,row,
								row,1,2);
						OO.setTableBorder(xSpreadsheet,startColumn,posCol - 1,
								row,row,false,false,false,false,false,true);
						OO.setTableBorder(xSpreadsheet,posCol,negCol - 1,row,
								row,false,false,true,true,true,true);
						OO.setTableBorder(xSpreadsheet,negCol,othersCol - 1,
								row,row,false,false,true,true,true,true);
						OO.setTableBorder(xSpreadsheet,othersCol,endColumn - 1,
								row,row,true,false,true,true,true,true);
						row++;
					} // End of checking for page limit

					// Print competency
					// temp[0] = competency id; temp[1] = competency name;
					// temp[2] = other's rating; temp[3] = self rating;
					String[] temp = (String[]) obj;
					compID = Integer.parseInt(temp[0]);
					compName = temp[1];
					try {
						othersValue = Double.parseDouble(temp[2]);
					} catch (Exception ex) {
						othersValue = 0.0;
					}
					try {
						selfValue = Double.parseDouble(temp[3]);
					} catch (Exception ex) {
						selfValue = 0.0;
					}
					if (positive) { // Positive, insert into positive column
						OO.insertString(xSpreadsheet,"X",row,posCol);
					} else if (!positive) { // Negative, insert into negative
											// column
						OO.insertString(xSpreadsheet,"X",row,negCol);
					}

					// Insert Competency Name
					// Added translation to competency name, Chun Yeong 1 Aug
					// 2011
					OO.insertString(
							xSpreadsheet,
							compCount
									+ ". "
									+ UnicodeHelper
											.getUnicodeStringAmp(getTranslatedCompetency(
													temp[1]).elementAt(0)
													.toString()),row,
							firstColumn);
					OO.mergeCells(xSpreadsheet,startColumn,posCol - 1,row,row);

					// Insert Others and Self values
					OO.mergeCells(xSpreadsheet,posCol,negCol - 1,row,row);
					OO.mergeCells(xSpreadsheet,negCol,othersCol - 1,row,row);
					OO.insertNumeric(xSpreadsheet,othersValue,row,othersCol);
					OO.insertNumeric(xSpreadsheet,selfValue,row,selfCol);
					OO.setFontSize(xSpreadsheet,posCol,selfCol,row,row,12);

					// Centralize and draw border for the data
					OO.setCellAllignment(xSpreadsheet,posCol,selfCol,row,row,1,
							2);

					// Color competency
					OO.setBGColor(xSpreadsheet,startColumn,endColumn - 1,row,
							row,BGCOLOR);

					// Increment Competency count
					compCount++;

					// Add border lines
					OO.setTableBorder(xSpreadsheet,startColumn,posCol - 1,row,
							row,false,false,true,true,true,true);
					OO.setTableBorder(xSpreadsheet,posCol,negCol - 1,row,row,
							false,false,true,true,true,true);
					OO.setTableBorder(xSpreadsheet,negCol,othersCol - 1,row,
							row,false,false,true,true,true,true);
					OO.setTableBorder(xSpreadsheet,othersCol,endColumn - 1,row,
							row,true,false,true,true,true,true);

					System.out
							.println("StartRow : " + startRow + "Row: " + row);
					OO.setRowHeight(xSpreadsheet,row,0,600);

					row++;

					/*************************************
					 * Print KB list for each competency *
					 *************************************/
					int KBID = 0;
					String KBName = "";

					Vector KBList = KBList(compID);
					if (KBList.size() != 0) {
						for (int j = 0; j < KBList.size(); j++) {
							voKeyBehaviour voKB = (voKeyBehaviour) KBList
									.elementAt(j);
							KBID = voKB.getKeyBehaviourID();
							KBName = voKB.getKeyBehaviour();

							Double[] KBResult = new Double[]{0.0, 0.0};
							Vector RT = RatingTask();

							for (int k = 0; k < RT.size(); k++) {
								votblSurveyRating vo = (votblSurveyRating) RT
										.elementAt(k);

								RTID = vo.getRatingTaskID();
								RTCode = vo.getRatingCode();

								if (RTCode.equals("CP")) {
									Vector result = MeanResult(RTID,compID,KBID);

									for (int l = 0; l < result.size(); l++) {
										String[] arr = (String[]) result
												.elementAt(l);

										// arr[0]: CompetencyID
										// arr[1]: Type
										// arr[2]: AvgMean as Result
										int type = Integer.parseInt(arr[1]);
										if (type == 1) {
											KBResult[0] = Double
													.parseDouble(arr[2]);
										} else if (type == 4) {
											KBResult[1] = Double
													.parseDouble(arr[2]);
										}
									} // End of for loop, results size

									// Skip when ratings of both others and self
									// are the same.
									if ((KBResult[0] > KBResult[1])
											|| (KBResult[0] < KBResult[1])) {

										/*************************************************************************
										 * Check if line exceeds new page. If
										 * yes, insert page break * This section
										 * is similar to ABOVE checking before
										 * printing competency. *
										 *************************************************************************/
										// To cater to if the Key Behavior name
										// is starting on a new page
										// And reset the variables: startBorder,
										// lastPageRowCount and
										// currentPageHeight
										// Check height and insert pagebreak
										// where necessary
										pageHeightLimit = 25600;// Page limit is
																// 22272
										// currentPageHeight = 1076;

										// calculate the height of the table
										// that is being added.
										for (int i = startBorder; i <= row; i++) {
											currentPageHeight += OO
													.getRowHeight(xSpreadsheet,
															i,startColumn);
										}

										currentPageHeight += 1076;

										if (currentPageHeight > pageHeightLimit) {// adding
																					// the
																					// table
																					// will
																					// exceed
																					// a
																					// single
																					// page,
																					// insert
																					// page
																					// break
											// Draw the border
											OO.setTableBorder(xSpreadsheet,
													startColumn,endColumn - 1,
													row - 1,row - 1,false,
													false,false,false,false,
													true);
											// Insert page break
											OO.insertPageBreak(xSpreadsheet,
													startColumn,endColumn,row);
											// to move the table two lines down
											row += 2;
											// reset values
											startBorder = row;
											lastPageRowCount = row;
											currentPageHeight = 0;

											// Insert 2 new labels CP and CPR
											// before Gap and set font size to
											// 12
											OO.insertString(xSpreadsheet,trans
													.tslt(templateLanguage,
															"Positive"),row,
													posCol);
											OO.mergeCells(xSpreadsheet,posCol,
													negCol - 1,row,row);
											OO.insertString(xSpreadsheet,trans
													.tslt(templateLanguage,
															"Negative"),row,
													negCol);
											OO.mergeCells(xSpreadsheet,negCol,
													othersCol - 1,row,row);
											OO.insertString(xSpreadsheet,trans
													.tslt(templateLanguage,
															"Others"),row,
													othersCol);
											OO.mergeCells(xSpreadsheet,
													othersCol,selfCol - 1,row,
													row);
											OO.insertString(xSpreadsheet,trans
													.tslt(templateLanguage,
															"Self"),row,selfCol);
											OO.mergeCells(xSpreadsheet,selfCol,
													endColumn - 1,row,row);

											OO.setFontBold(xSpreadsheet,
													startColumn,endColumn,row,
													row);
											OO.setFontSize(xSpreadsheet,posCol,
													selfCol,row,row,12);
											OO.setCellAllignment(xSpreadsheet,
													posCol,selfCol,row,row,1,2);
											OO.setTableBorder(xSpreadsheet,
													startColumn,posCol - 1,row,
													row,false,false,false,
													false,false,true);
											OO.setTableBorder(xSpreadsheet,
													posCol,negCol - 1,row,row,
													false,false,true,true,true,
													true);
											OO.setTableBorder(xSpreadsheet,
													negCol,othersCol - 1,row,
													row,false,false,true,true,
													true,true);
											OO.setTableBorder(xSpreadsheet,
													othersCol,endColumn - 1,
													row,row,true,false,true,
													true,true,true);
											row++;
										} // End of checking for page limit

										// Insert Key behavior Name
										// Added translation to the key
										// behaviour name, Chun Yeong 1 Aug 2011
										OO.insertString(
												xSpreadsheet,
												"KB: "
														+ getTranslatedKeyBehavior(UnicodeHelper
																.getUnicodeStringAmp(KBName)),
												row,firstColumn);
										OO.mergeCells(xSpreadsheet,0,
												posCol - 1,row,row);

										// Insert positive or negative
										if (KBResult[0] > KBResult[1]) { // If
																			// others(All)
																			// >
																			// self
											OO.insertString(xSpreadsheet,"X",
													row,posCol);
										} else if (KBResult[0] < KBResult[1]) { // If
																				// others(All)
																				// <
																				// self
											OO.insertString(xSpreadsheet,"X",
													row,negCol);
										}

										OO.mergeCells(xSpreadsheet,posCol,
												negCol - 1,row,row);
										OO.mergeCells(xSpreadsheet,negCol,
												othersCol - 1,row,row);

										// Insert Others and Self values
										OO.insertNumeric(xSpreadsheet,
												KBResult[0],row,othersCol);
										OO.insertNumeric(xSpreadsheet,
												KBResult[1],row,selfCol);
										OO.setFontSize(xSpreadsheet,posCol,
												selfCol,row,row,12);

										// Centralize the data
										OO.setCellAllignment(xSpreadsheet,
												posCol,selfCol,row,row,1,2);

										// Draw borders
										OO.setTableBorder(xSpreadsheet,
												startColumn,posCol - 1,row,row,
												false,false,true,true,false,
												false);
										OO.setTableBorder(xSpreadsheet,posCol,
												negCol - 1,row,row,false,false,
												true,true,false,false);
										OO.setTableBorder(xSpreadsheet,negCol,
												othersCol - 1,row,row,false,
												false,true,true,false,false);
										OO.setTableBorder(xSpreadsheet,
												othersCol,endColumn - 1,row,
												row,true,false,true,true,false,
												false);

										row++;
									}

								} // End of if RTCode is 'CP'

							}// End of for loop, RT size

						}// End of for loop, KB List

						// Add border lines
						OO.setTableBorder(xSpreadsheet,startColumn,
								endColumn - 1,row,row,false,false,true,true,
								false,true);
						OO.setTableBorder(xSpreadsheet,posCol,negCol - 1,row,
								row,false,false,true,true,false,false);
						OO.setTableBorder(xSpreadsheet,negCol,othersCol - 1,
								row,row,false,false,true,true,false,false);
						OO.setTableBorder(xSpreadsheet,othersCol,endColumn - 1,
								row,row,true,false,true,true,false,false);

						row++;
					} // End if, when there are KB to display

				} // End of for loop, each competency to display
			} // end of for loop , cluster list
		} // End if, table of data to display
	} // End of InsertPositiveBlindSpotAnalysis

	/**
	 * This method inserts the blind spot analysis into individual report.
	 * Warning: Currently only applicable for Current Proficiencies (CP).
	 * Missing indonesian translation.
	 * 
	 * @param positive
	 *            - Either TRUE for positive blind spots or FALSE for negative
	 *            blind spots
	 * @throws Exception
	 * 
	 * @author Chun Yeong
	 * @since v1.3.12.96 //27 May 2011
	 */
	public void InsertBlindSpotAnalysis(Boolean positive) throws Exception {
		int surveyLevel = Integer.parseInt(surveyInfo[0]);

		int compCount = 1;
		int compID = 0;
		String compName = "";
		int maxScale = MaxScale();

		column = 0;

		int firstColumn = 0;
		// Added to define columns where positive, negative, others and self are
		// inserted in spreadsheet
		int posCol = 6;
		int negCol = 8;
		int othersCol = 10;
		int selfCol = 11;
		double othersValue = 0.0;
		double selfValue = 0.0;
		Vector vComp = CompetencyByName();
		Vector vCompDisplay = new Vector();
		String[] Result = new String[4];

		int RTID = 0;
		String RTCode = "";

		for (int i = 0; i < vComp.size(); i++) {
			Result = new String[4];

			voCompetency voComp = (voCompetency) vComp.elementAt(i);
			compID = voComp.getCompetencyID();
			compName = voComp.getCompetencyName();

			Vector RT = RatingTask();
			for (int j = 0; j < RT.size(); j++) {
				votblSurveyRating vo = (votblSurveyRating) RT.elementAt(j);
				RTID = vo.getRatingTaskID();
				RTCode = vo.getRatingCode();

				// RTID = 1, RTCode = CP
				// RTID = 2, RTCode = CPR

				Vector result = null;
				if (surveyLevel == 0) { // Competency level
					result = MeanResult(RTID,compID,0);
				} else { // KB level
					result = KBMean(RTID,compID);
				}

				if (RTCode.equals("CP")) {

					for (int k = 0; k < result.size(); k++) {
						String[] arr = (String[]) result.elementAt(k);

						// arr[0]: CompetencyID
						// arr[1]: Type
						// arr[2]: CAST(AVG(AvgMean) AS numeric(38, 2)) AS
						// Result
						int type = Integer.parseInt(arr[1]);
						if (type == 1 || type == 4) {
							Result[0] = compID + "";
							Result[1] = compName;
							if (type == 1) {
								Result[2] = CompTrimmedMeanforAll(RTID,compID)
										+ "";
							} else {
								Result[3] = Double.parseDouble(arr[2]) + "";
							}
						}
					}
					try {
						othersValue = Double.parseDouble(Result[2]);
					} catch (Exception ex) {
						othersValue = 0.0;
					}
					try {
						selfValue = Double.parseDouble(Result[3]);
					} catch (Exception ex) {
						selfValue = 0.0;
					}

					if (positive) {
						// 3 conditions: 1) others must be more than
						// (maxScale/2)
						// 2) both others and self results are not the same
						// 3) others must be more than self
						if (othersValue >= (maxScale / (2 * 1.0))
								&& (othersValue > selfValue)
								&& (othersValue != selfValue)) {
							vCompDisplay.add(Result);
						}
					} else if (!positive) {
						// 3 conditions: 1) self must be more than (maxScale/2)
						// 2) both others and self results are not the same
						// 3) self must be more than others

						if (selfValue >= (maxScale / (2 * 1.0))
								&& (selfValue > othersValue)
								&& (othersValue != selfValue)) {
							vCompDisplay.add(Result);
						}
					}
				} // else if RTCode is CPR/FPR
			}
		}// End of for loop, Competency list

		/***********************
		 * Construct the table *
		 ***********************/
		if (vCompDisplay.size() != 0) {

			int startBorder = 0;
			// First time entering here, if last page row count is = 0, or >=
			// row
			// Set the startBorder to the row

			if (lastPageRowCount == 0) {
				startBorder = row;
				lastPageRowCount = row;
			} else {
				startBorder = lastPageRowCount;
			}

			// Set up headers, Chun Yeong 1 Aug 2011
			// \u2264 writes 'Smaller than or Equals to' or <=
			// \u003E writes 'Greater than' or >
			String title = "";
			String titleDesc = "";
			if (positive) {
				title = trans.tslt(templateLanguage,"Positive Blind Spots");
				titleDesc = trans
						.tslt(templateLanguage,
								"Competencies/KB where others rated you better than what you rated yourself "
										+ "and where the rating from others is above the effective point "
										+ "of the scale used (i.e.")
						+ " "
						+ maxScale
						/ (2 * 1.0)
						+ " \u2264 "
						+ trans.tslt(templateLanguage,"Other's Rating")
						+ " \u003E "
						+ trans.tslt(templateLanguage,"Self Rating") + ")";
			} else {
				title = trans.tslt(templateLanguage,"Negative Blind Spots");
				titleDesc = trans
						.tslt(templateLanguage,
								"Competencies/KB where you rated yourself better than others rated you "
										+ "and where your self rating is above the effective point "
										+ "of the scale used (i.e. ")
						+ " "
						+ maxScale
						/ (2 * 1.0)
						+ " \u2264 "
						+ trans.tslt(templateLanguage,"Self Rating")
						+ " \u003E "
						+ trans.tslt(templateLanguage,"Other's Rating") + ")";
			}

			// To cater to if the negative table is starting on a new page
			// And reset the variables: startBorder, lastPageRowCount and
			// currentPageHeight
			// Check height and insert pagebreak where necessary
			int pageHeightLimit = 22272;// Page limit is 22272
			int currentPageHeight = 1076;
			// calculate the height of the table that is being added.
			for (int i = startBorder; i <= row; i++) {
				currentPageHeight += OO
						.getRowHeight(xSpreadsheet,i,startColumn);
			}

			currentPageHeight += 1614;

			if (currentPageHeight > pageHeightLimit
					|| ((pageHeightLimit - currentPageHeight) <= 3000)) {// adding
																			// the
																			// table
																			// will
																			// exceed
																			// a
																			// single
																			// page,
																			// insert
																			// page
																			// break
				// Draw the border
				OO.setTableBorder(xSpreadsheet,startColumn,endColumn - 1,
						row - 1,row - 1,false,false,false,false,false,true);
				// Insert page break
				OO.insertPageBreak(xSpreadsheet,startColumn,endColumn,row);
				// to move the table two lines down
				row += 2;
				// reset values
				startBorder = row;
				lastPageRowCount = row;
				currentPageHeight = 0;

			} // End of checking for page limit

			// Insert the title
			OO.insertString(xSpreadsheet,
					UnicodeHelper.getUnicodeStringAmp(title),row,firstColumn);
			OO.setFontBold(xSpreadsheet,startColumn,endColumn,row,row);
			OO.mergeCells(xSpreadsheet,startColumn,posCol - 1,row,row);
			row += 2;

			// Insert the description
			OO.insertString(xSpreadsheet,
					UnicodeHelper.getUnicodeStringAmp(titleDesc),row,
					firstColumn);
			OO.mergeCells(xSpreadsheet,startColumn,endColumn - 1,row,row);
			row += 2;

			// Insert 2 new labels CP and CPR before Gap and set font size to 12
			OO.insertString(xSpreadsheet,
					trans.tslt(templateLanguage,"Positive"),row,posCol);
			OO.mergeCells(xSpreadsheet,posCol,negCol - 1,row,row);
			OO.insertString(xSpreadsheet,
					trans.tslt(templateLanguage,"Negative"),row,negCol);
			OO.mergeCells(xSpreadsheet,negCol,othersCol - 1,row,row);
			OO.insertString(xSpreadsheet,trans.tslt(templateLanguage,"Others"),
					row,othersCol);
			OO.mergeCells(xSpreadsheet,othersCol,selfCol - 1,row,row);
			OO.insertString(xSpreadsheet,trans.tslt(templateLanguage,"Self"),
					row,selfCol);
			OO.mergeCells(xSpreadsheet,selfCol,endColumn - 1,row,row);

			OO.setFontBold(xSpreadsheet,startColumn,endColumn,row,row);
			OO.setFontSize(xSpreadsheet,posCol,selfCol,row,row,12);
			OO.setCellAllignment(xSpreadsheet,posCol,selfCol,row,row,1,2);
			// OO.setBGColor(xSpreadsheet, startColumn, endColumn - 1, row, row,
			// BGCOLOR);
			OO.setTableBorder(xSpreadsheet,startColumn,posCol - 1,row,row,
					false,false,false,false,false,true);
			OO.setTableBorder(xSpreadsheet,posCol,negCol - 1,row,row,false,
					false,true,true,true,true);
			OO.setTableBorder(xSpreadsheet,negCol,othersCol - 1,row,row,false,
					false,true,true,true,true);
			OO.setTableBorder(xSpreadsheet,othersCol,endColumn - 1,row,row,
					true,false,true,true,true,true);

			row++;

			for (Object obj : vCompDisplay) {

				/****************************************************************************
				 * Check if line exceeds new page. If yes, insert page break *
				 * This section is similar to BOTTOM checking before printing
				 * key behavior. *
				 ****************************************************************************/
				// To cater to if the competency name is starting on a new page
				// And reset the variables: startBorder, lastPageRowCount and
				// currentPageHeight
				// Check height and insert pagebreak where necessary
				pageHeightLimit = 22272;// Page limit is 22272
				currentPageHeight = 1076;

				// calculate the height of the table that is being added.
				for (int i = startBorder; i <= row; i++) {
					currentPageHeight += OO.getRowHeight(xSpreadsheet,i,
							startColumn);
				}

				currentPageHeight += 1076;

				if (currentPageHeight > pageHeightLimit
						|| ((pageHeightLimit - currentPageHeight) <= 3000)) {// adding
																				// the
																				// table
																				// will
																				// exceed
																				// a
																				// single
																				// page,
																				// insert
																				// page
																				// break
					// Draw the border
					OO.setTableBorder(xSpreadsheet,startColumn,endColumn - 1,
							row - 1,row - 1,false,false,false,false,false,true);
					// Insert page break
					OO.insertPageBreak(xSpreadsheet,startColumn,endColumn,row);
					// to move the table two lines down
					row += 2;
					// reset values
					startBorder = row;
					lastPageRowCount = row;
					currentPageHeight = 0;

					// Insert 2 new labels CP and CPR before Gap and set font
					// size to 12
					OO.insertString(xSpreadsheet,
							trans.tslt(templateLanguage,"Positive"),row,posCol);
					OO.mergeCells(xSpreadsheet,posCol,negCol - 1,row,row);
					OO.insertString(xSpreadsheet,
							trans.tslt(templateLanguage,"Negative"),row,negCol);
					OO.mergeCells(xSpreadsheet,negCol,othersCol - 1,row,row);
					OO.insertString(xSpreadsheet,
							trans.tslt(templateLanguage,"Others"),row,othersCol);
					OO.mergeCells(xSpreadsheet,othersCol,selfCol - 1,row,row);
					OO.insertString(xSpreadsheet,
							trans.tslt(templateLanguage,"Self"),row,selfCol);
					OO.mergeCells(xSpreadsheet,selfCol,endColumn - 1,row,row);

					OO.setFontBold(xSpreadsheet,startColumn,endColumn,row,row);
					OO.setFontSize(xSpreadsheet,posCol,selfCol,row,row,12);
					OO.setCellAllignment(xSpreadsheet,posCol,selfCol,row,row,1,
							2);
					OO.setTableBorder(xSpreadsheet,startColumn,posCol - 1,row,
							row,false,false,false,false,false,true);
					OO.setTableBorder(xSpreadsheet,posCol,negCol - 1,row,row,
							false,false,true,true,true,true);
					OO.setTableBorder(xSpreadsheet,negCol,othersCol - 1,row,
							row,false,false,true,true,true,true);
					OO.setTableBorder(xSpreadsheet,othersCol,endColumn - 1,row,
							row,true,false,true,true,true,true);
					row++;
				} // End of checking for page limit

				// Print competency
				// temp[0] = competency id; temp[1] = competency name; temp[2] =
				// other's rating; temp[3] = self rating;
				String[] temp = (String[]) obj;
				compID = Integer.parseInt(temp[0]);
				compName = temp[1];
				try {
					othersValue = Double.parseDouble(temp[2]);
				} catch (Exception ex) {
					othersValue = 0.0;
				}
				try {
					selfValue = Double.parseDouble(temp[3]);
				} catch (Exception ex) {
					selfValue = 0.0;
				}
				if (positive) { // Positive, insert into positive column
					OO.insertString(xSpreadsheet,"X",row,posCol);
				} else if (!positive) { // Negative, insert into negative column
					OO.insertString(xSpreadsheet,"X",row,negCol);
				}

				// Insert Competency Name
				// Added translation to competency name, Chun Yeong 1 Aug 2011
				OO.insertString(
						xSpreadsheet,
						compCount
								+ ". "
								+ UnicodeHelper
										.getUnicodeStringAmp(getTranslatedCompetency(
												temp[1]).elementAt(0)
												.toString()),row,firstColumn);
				OO.mergeCells(xSpreadsheet,startColumn,posCol - 1,row,row);

				// Insert Others and Self values
				OO.mergeCells(xSpreadsheet,posCol,negCol - 1,row,row);
				OO.mergeCells(xSpreadsheet,negCol,othersCol - 1,row,row);
				OO.insertNumeric(xSpreadsheet,othersValue,row,othersCol);
				OO.insertNumeric(xSpreadsheet,selfValue,row,selfCol);
				OO.setFontSize(xSpreadsheet,posCol,selfCol,row,row,12);

				// Centralize and draw border for the data
				OO.setCellAllignment(xSpreadsheet,posCol,selfCol,row,row,1,2);

				// Color competency
				OO.setBGColor(xSpreadsheet,startColumn,endColumn - 1,row,row,
						BGCOLOR);

				// Increment Competency count
				compCount++;

				// Add border lines
				OO.setTableBorder(xSpreadsheet,startColumn,posCol - 1,row,row,
						false,false,true,true,true,true);
				OO.setTableBorder(xSpreadsheet,posCol,negCol - 1,row,row,false,
						false,true,true,true,true);
				OO.setTableBorder(xSpreadsheet,negCol,othersCol - 1,row,row,
						false,false,true,true,true,true);
				OO.setTableBorder(xSpreadsheet,othersCol,endColumn - 1,row,row,
						true,false,true,true,true,true);

				row++;

				/*************************************
				 * Print KB list for each competency *
				 *************************************/
				int KBID = 0;
				String KBName = "";

				Vector KBList = KBList(compID);
				if (KBList.size() != 0) {
					for (int j = 0; j < KBList.size(); j++) {
						voKeyBehaviour voKB = (voKeyBehaviour) KBList
								.elementAt(j);
						KBID = voKB.getKeyBehaviourID();
						KBName = voKB.getKeyBehaviour();

						Double[] KBResult = new Double[]{0.0, 0.0};
						Vector RT = RatingTask();

						for (int k = 0; k < RT.size(); k++) {
							votblSurveyRating vo = (votblSurveyRating) RT
									.elementAt(k);

							RTID = vo.getRatingTaskID();
							RTCode = vo.getRatingCode();

							if (RTCode.equals("CP")) {
								Vector result = MeanResult(RTID,compID,KBID);

								for (int l = 0; l < result.size(); l++) {
									String[] arr = (String[]) result
											.elementAt(l);

									// arr[0]: CompetencyID
									// arr[1]: Type
									// arr[2]: AvgMean as Result
									int type = Integer.parseInt(arr[1]);
									if (type == 1) {
										KBResult[0] = Double
												.parseDouble(arr[2]);
									} else if (type == 4) {
										KBResult[1] = Double
												.parseDouble(arr[2]);
									}
								} // End of for loop, results size

								// Skip when ratings of both others and self are
								// the same.
								if ((KBResult[0] > KBResult[1])
										|| (KBResult[0] < KBResult[1])) {

									/*************************************************************************
									 * Check if line exceeds new page. If yes,
									 * insert page break * This section is
									 * similar to ABOVE checking before printing
									 * competency. *
									 *************************************************************************/
									// To cater to if the Key Behavior name is
									// starting on a new page
									// And reset the variables: startBorder,
									// lastPageRowCount and currentPageHeight
									// Check height and insert pagebreak where
									// necessary
									pageHeightLimit = 22272;// Page limit is
															// 22272
									currentPageHeight = 1076;

									// calculate the height of the table that is
									// being added.
									for (int i = startBorder; i <= row; i++) {
										currentPageHeight += OO.getRowHeight(
												xSpreadsheet,i,startColumn);
									}

									currentPageHeight += 1076;

									if (currentPageHeight > pageHeightLimit) {// adding
																				// the
																				// table
																				// will
																				// exceed
																				// a
																				// single
																				// page,
																				// insert
																				// page
																				// break
										// Draw the border
										OO.setTableBorder(xSpreadsheet,
												startColumn,endColumn - 1,
												row - 1,row - 1,false,false,
												false,false,false,true);
										// Insert page break
										OO.insertPageBreak(xSpreadsheet,
												startColumn,endColumn,row);
										// to move the table two lines down
										row += 2;
										// reset values
										startBorder = row;
										lastPageRowCount = row;
										currentPageHeight = 0;

										// Insert 2 new labels CP and CPR before
										// Gap and set font size to 12
										OO.insertString(xSpreadsheet,trans
												.tslt(templateLanguage,
														"Positive"),row,posCol);
										OO.mergeCells(xSpreadsheet,posCol,
												negCol - 1,row,row);
										OO.insertString(xSpreadsheet,trans
												.tslt(templateLanguage,
														"Negative"),row,negCol);
										OO.mergeCells(xSpreadsheet,negCol,
												othersCol - 1,row,row);
										OO.insertString(xSpreadsheet,
												trans.tslt(templateLanguage,
														"Others"),row,othersCol);
										OO.mergeCells(xSpreadsheet,othersCol,
												selfCol - 1,row,row);
										OO.insertString(xSpreadsheet,trans
												.tslt(templateLanguage,"Self"),
												row,selfCol);
										OO.mergeCells(xSpreadsheet,selfCol,
												endColumn - 1,row,row);

										OO.setFontBold(xSpreadsheet,
												startColumn,endColumn,row,row);
										OO.setFontSize(xSpreadsheet,posCol,
												selfCol,row,row,12);
										OO.setCellAllignment(xSpreadsheet,
												posCol,selfCol,row,row,1,2);
										OO.setTableBorder(xSpreadsheet,
												startColumn,posCol - 1,row,row,
												false,false,false,false,false,
												true);
										OO.setTableBorder(xSpreadsheet,posCol,
												negCol - 1,row,row,false,false,
												true,true,true,true);
										OO.setTableBorder(xSpreadsheet,negCol,
												othersCol - 1,row,row,false,
												false,true,true,true,true);
										OO.setTableBorder(xSpreadsheet,
												othersCol,endColumn - 1,row,
												row,true,false,true,true,true,
												true);
										row++;
									} // End of checking for page limit

									// Insert Key behavior Name
									// Added translation to the key behaviour
									// name, Chun Yeong 1 Aug 2011
									OO.insertString(
											xSpreadsheet,
											"KB: "
													+ getTranslatedKeyBehavior(UnicodeHelper
															.getUnicodeStringAmp(KBName)),
											row,firstColumn);
									OO.mergeCells(xSpreadsheet,0,posCol - 1,
											row,row);

									// Insert positive or negative
									if (KBResult[0] > KBResult[1]) { // If
																		// others(All)
																		// >
																		// self
										OO.insertString(xSpreadsheet,"X",row,
												posCol);
									} else if (KBResult[0] < KBResult[1]) { // If
																			// others(All)
																			// <
																			// self
										OO.insertString(xSpreadsheet,"X",row,
												negCol);
									}

									OO.mergeCells(xSpreadsheet,posCol,
											negCol - 1,row,row);
									OO.mergeCells(xSpreadsheet,negCol,
											othersCol - 1,row,row);

									// Insert Others and Self values
									OO.insertNumeric(xSpreadsheet,KBResult[0],
											row,othersCol);
									OO.insertNumeric(xSpreadsheet,KBResult[1],
											row,selfCol);
									OO.setFontSize(xSpreadsheet,posCol,selfCol,
											row,row,12);

									// Centralize the data
									OO.setCellAllignment(xSpreadsheet,posCol,
											selfCol,row,row,1,2);

									// Draw borders
									OO.setTableBorder(xSpreadsheet,startColumn,
											posCol - 1,row,row,false,false,
											true,true,false,false);
									OO.setTableBorder(xSpreadsheet,posCol,
											negCol - 1,row,row,false,false,
											true,true,false,false);
									OO.setTableBorder(xSpreadsheet,negCol,
											othersCol - 1,row,row,false,false,
											true,true,false,false);
									OO.setTableBorder(xSpreadsheet,othersCol,
											endColumn - 1,row,row,true,false,
											true,true,false,false);

									row++;
								}

							} // End of if RTCode is 'CP'

						}// End of for loop, RT size

					}// End of for loop, KB List

					// Add border lines
					OO.setTableBorder(xSpreadsheet,startColumn,endColumn - 1,
							row,row,false,false,true,true,false,true);
					OO.setTableBorder(xSpreadsheet,posCol,negCol - 1,row,row,
							false,false,true,true,false,false);
					OO.setTableBorder(xSpreadsheet,negCol,othersCol - 1,row,
							row,false,false,true,true,false,false);
					OO.setTableBorder(xSpreadsheet,othersCol,endColumn - 1,row,
							row,true,false,true,true,false,false);
					row++;
				} // End if, when there are KB to display

			} // End of for loop, each competency to display

		} // End if, table of data to display

	} // End of InsertPositiveBlindSpotAnalysis

	public static void main(String[] args) throws IOException, Exception {

		SPFIndividualReport IR = new SPFIndividualReport();

		int surveyID = 438;
		int targetID = 2328;// 6636
		// System.out.println("TEST");
		long past = System.currentTimeMillis();
		// Commented by Tracy 01 Sep 08 IR.Report(489, 7711, 2,
		// "Individual Report (Test).xls", 2);
		long now = System.currentTimeMillis();

		IR.Report(498,6611,6404,"IndividualReport220908153454.xls",2,"","","",
				"",0,"",false,false);

	}

	/*
	 * This method insert the rating scale into the individual report
	 * 
	 * @author: Liu Taichen created on: 17/July/2012 modified by: Albert (18
	 * July 2012) changes: add new paragraph and dynamic placement of rating
	 * scale following the template
	 */
	public void InsertRatingScaleList() throws Exception {

		System.out.println("Printing Rating Scale List.");
		int address[] = OO.findString(xSpreadsheet,"<Rating Scale>");

		OO.findAndReplace(xSpreadsheet,"<Rating Scale>","");
		int iColumn = address[0];
		int iRow = address[1];

		// row and colume specify where the rating scale is to be inserted/
		try {

			// row = 88;
			// column = 0;
			ExcelQuestionnaire eq = new ExcelQuestionnaire();

			int totalColumn = 12;
			int maxScale = eq.maxScale(surveyID) + 1;

			Vector v = eq.SurveyRating(surveyID);
			int count = 0;

			try {
				OO.insertRows(xSpreadsheet,0,totalColumn,iRow,iRow + 1,1,1);
			} catch (Exception e) {
				System.out.println("it's here");
			}

			for (int i = 0; i < 1; i++) {
				votblSurveyRating vo = (votblSurveyRating) v.elementAt(i);
				count++;

				boolean hideNA = Q.getHideNAOption(surveyID);
				String code = vo.getRatingCode();
				String ratingTask = vo.getRatingTaskName();
				int scaleID = vo.getScaleID();

				Vector RS = Q.getRatingScale(scaleID);
				int tempIRow = iRow;
				int ratingScale = 0;
				double CR = 0.0;
				int CRPlusOne = 0;
				// OO.mergeCells(xSpreadsheet, startColumn, totalColumn,
				// iRow, iRow );

				for (int j = 0; j < RS.size(); j++) {
					String[] sRS = new String[3];

					sRS = (String[]) RS.elementAt(j);

					int low = Integer.parseInt(sRS[0]);
					int high = Integer.parseInt(sRS[1]);
					String desc = sRS[2];

					// Denise 29/12/2009 to hide NA if required
					if (!(hideNA && (desc.equalsIgnoreCase("NA")
							|| desc.equalsIgnoreCase("N/A")
							|| desc.equals("Not applicable")
							|| desc.contains("NA") || desc.contains("N/A")
							|| desc.contains("Not applicable") || desc
								.contains("Not Applicable")))) {

						iRow += 1;
						column = 0;
						OO.insertRows(xSpreadsheet,0,totalColumn,iRow,iRow + 1,
								1,1);
						String temp = "";
						boolean firstTime = true;
						if (low == high) {
							temp += Integer.toString(low);
						} else {
							while (low <= high) {
								if (firstTime) {
									temp += Integer.toString(low) + " to ";
									firstTime = false;
								} else {
									if (low == high)
										temp += Integer.toString(low);
									low++;
								}
							}
						}
						ratingScale = high;
						OO.insertString(xSpreadsheet,
								(temp + "  -  " + desc).trim(),iRow,iColumn); // add
																				// in
																				// scale
																				// description
						OO.setCellAllignment(xSpreadsheet,iColumn,totalColumn,
								iRow,iRow,2,2);
						// OO.setCellAllignment(xSpreadsheet, column,
						// totalColumn, row, row, 2, 2);
						OO.mergeCells(xSpreadsheet,0,totalColumn,iRow,iRow);
					}// end if to insert Rating scale
				}
				// to get the rating low point after the middle point
				boolean getNextOne = false;
				for (int j = 0; j < RS.size(); j++) {
					String[] sRS = new String[3];
					sRS = (String[]) RS.elementAt(j);
					int low = Integer.parseInt(sRS[0]);
					int high = Integer.parseInt(sRS[1]);
					if (getNextOne) {
						CRPlusOne = low;
						break;
					}
					if (ratingScale / 2 == low) {
						getNextOne = true;
					}

				}
				String paragraph = "All the graphs in this report with a "
						+ ratingScale
						+ " point scale use the following rating scale description: ";
				OO.insertString(xSpreadsheet,paragraph,tempIRow,startColumn);
				CR = (double) ratingScale / 2.0;
				OO.findAndReplace(xSpreadsheet,"<CR>",Double.toString(CR));
				if (iNoCPR == 1)// if no CPR (CP Only)
					OO.findAndReplace(xSpreadsheet,"<CR+1>",
							Integer.toString(CRPlusOne));
			}

			// OO.insertPageBreak(xSpreadsheet, 1, 12, row + 2);
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	// row = iRow;
	// column = iColumn;

	/*
	 * This method insert the rating scale into the individual report
	 * 
	 * @author: Liu Taichen created on: 16/July/2012
	 */
	public void InsertRatingScale() {
		int iRow = row;
		int iColumn = column;

		System.out.println("printing rating scale.");
		// row and colume specify where the rating scale is to be inserted/
		try {

			row = 60;
			column = 1;
			ExcelQuestionnaire eq = new ExcelQuestionnaire();

			int totalColumn = 12;
			int maxScale = eq.maxScale(surveyID) + 1;

			// int totalCells = totalColumn / maxScale;
			int totalCells = 2;
			int totalMerge = 0; // total cells to be merged after rounding
			double merge = 0; // total cells to be merged before rounding
			Vector v = eq.SurveyRating(surveyID);
			int count = 0;

			int[] scale = new int[2];
			scale[0] = 0;
			scale[1] = 0;

			try {
				OO.insertRows(xSpreadsheet,0,totalColumn,row,row + 1,1,1);
			} catch (Exception e) {
				System.out.println("it's here");
			}
			OO.insertString(xSpreadsheet,"Rating Scales used in this survey:",
					row,column);
			OO.setFontBold(xSpreadsheet,column,column,row,row);
			OO.mergeCells(xSpreadsheet,column,column + 4,row,row + 1);

			row++;
			OO.insertRows(xSpreadsheet,0,totalColumn,row,row + 1,1,1);
			OO.insertRows(xSpreadsheet,0,totalColumn,row,row + 1,1,1);

			for (int i = 0; i < 1; i++) {
				votblSurveyRating vo = (votblSurveyRating) v.elementAt(i);
				count++;

				boolean hideNA = Q.getHideNAOption(surveyID);
				String code = vo.getRatingCode();
				String ratingTask = vo.getRatingTaskName();
				int scaleID = vo.getScaleID();
				// Denise 29/12/2009 insert row here
				// OO.insertRows(xSpreadsheet, 0, 24, row, row+1, 1, 1);
				// OO.insertString(xSpreadsheet, count + ". " + ratingTask, row,
				// column);
				// OO.setFontBold(xSpreadsheet, column, column, row, row);

				// String statement = eq.RatingStatement(code);

				row++;
				OO.insertRows(xSpreadsheet,0,totalColumn,row,row + 1,1,1);
				scale[1] += 1;

				// OO.insertString(xSpreadsheet, statement, row, column+1);
				OO.mergeCells(xSpreadsheet,column + 1,totalColumn,row,row);

				// add rating scale
				row = row + 2;
				OO.insertRows(xSpreadsheet,1,1,row,row + 2,2,1);
				scale[1] += 2;

				int c = 1;
				int r = row;
				int to = c;

				Vector RS = Q.getRatingScale(scaleID);

				// OO.insertRows(xSpreadsheet, 0, 24, row, row+3, 3, 1);
				for (int j = 0; j < RS.size(); j++) {
					String[] sRS = new String[3];

					sRS = (String[]) RS.elementAt(j);

					int low = Integer.parseInt(sRS[0]);
					int high = Integer.parseInt(sRS[1]);
					String desc = sRS[2];
					// Denise 29/12/2009 to hide NA if required
					if (!(hideNA && (desc.equalsIgnoreCase("NA")
							|| desc.equalsIgnoreCase("N/A")
							|| desc.equals("Not applicable")
							|| desc.contains("NA") || desc.contains("N/A")
							|| desc.contains("Not applicable") || desc
								.contains("Not Applicable")))) {

						if (column + totalCells > totalColumn) {
							row += 2;
							column = 1;
							OO.insertRows(xSpreadsheet,0,totalColumn,row,
									row + 3,3,1);

							row += 1;

						}
						OO.insertString(xSpreadsheet,desc,row,column); // add
																		// in
																		// scale
																		// description
						OO.setCellAllignment(xSpreadsheet,column,column,row,
								row,1,2);
						OO.setCellAllignment(xSpreadsheet,column,column,row,
								row,2,2);

						r = row + 1;
						c = column;

						int start = c; // start merge cell
						String temp = "";

						while (low <= high) {
							if (low > 1)
								temp += "    ";
							temp = temp + Integer.toString(low);

							low++;
						}

						OO.insertString(xSpreadsheet,temp,r,c); // add in
																// rating
																// scale
																// value
						OO.setCellAllignment(xSpreadsheet,c,c,r,r,1,2);

						to = start + totalCells - 1; // merge cell for rating
														// scale value

						OO.mergeCells(xSpreadsheet,start,to,r,r);
						OO.setTableBorder(xSpreadsheet,start,to,r,r,true,true,
								true,true,true,true);

						OO.mergeCells(xSpreadsheet,start,to,row,row); // merge
																		// cell
																		// for
																		// rating
																		// scale
																		// description
						OO.setTableBorder(xSpreadsheet,start,to,row,row,true,
								true,true,true,true,true);
						OO.setBGColor(xSpreadsheet,start,to,row,row,BGCOLOR);

						merge = (double) desc.trim().length()
								/ (double) (totalCells);

						BigDecimal BD = new BigDecimal(merge);
						BD.setScale(0,BD.ROUND_UP);
						BigInteger BI = BD.toBigInteger();
						totalMerge = BI.intValue() + 1;

						OO.setRowHeight(xSpreadsheet,row,start,
								(150 * totalMerge));

						column = to + 1;
					}// end if to insert Rating scale
				}
				row = r + 2;
				// OO.insertRows(xSpreadsheet, 1, 1, row, row+3, 3, 1);
				scale[1] += 2;
				column = 0;
			}
			OO.insertPageBreak(xSpreadsheet,1,12,row);

		} catch (Exception e) {
			System.out.println(e);
		}
		row = iRow;
		column = iColumn;

	}

	public int getFormat() {
		return format;
	}

	public void setFormat(int format) {
		this.format = format;
	}

	/*
	 * To check if a particular survey is an importance survey or not
	 * 
	 * @author Albert
	 */
	public boolean hasImportance(int surveyID) {
		String query = "";
		boolean answer = false;
		int reliabilityCheck = C.ReliabilityCheck(surveyID);

		String tblName = "tblAvgMean";
		String result = "AvgMean";

		if (reliabilityCheck == 0) {
			tblName = "tblTrimmedMean";
			result = "TrimmedMean";
		}

		query = query + "SELECT DISTINCT tblRatingTask.RatingCode ";
		query = query + "FROM " + tblName + " INNER JOIN tblRatingTask ON ";
		query = query + tblName + ".RatingTaskID = tblRatingTask.RatingTaskID ";
		query = query + "INNER JOIN tblSurveyRating ON ";
		query = query
				+ "tblRatingTask.RatingTaskID = tblSurveyRating.RatingTaskID AND ";
		query = query + tblName + ".SurveyID = tblSurveyRating.SurveyID ";
		query = query + "WHERE " + tblName + ".SurveyID = " + surveyID
				+ " AND ";
		query = query
				+ "(tblRatingTask.RatingCode = 'IN' OR tblRatingTask.RatingCode = 'IF')";

		Connection con = null;
		Statement st = null;
		ResultSet rs = null;

		try {
			con = ConnectionBean.getConnection();
			st = con.createStatement();
			rs = st.executeQuery(query);

			if (rs.next()) {
				answer = true;
			}
		} catch (Exception ex) {
			System.out.println("SPFIndividualReport.java - hasImportance - "
					+ ex.getMessage());
		} finally {
			ConnectionBean.closeRset(rs); // Close ResultSet
			ConnectionBean.closeStmt(st); // Close statement
			ConnectionBean.close(con); // Close connection
		}

		return answer;
	}

	/*
	 * To check if a particular survey and target rate himself or not for
	 * Competency level survey
	 * 
	 * @author Albert
	 */
	public boolean hasRatedSelfCompLevel(int surveyID, int targetID) {
		String query = "";
		boolean answer = false;

		query += "SELECT DISTINCT tblAssignment.AssignmentID ";
		query += "FROM tblAssignment INNER JOIN tblResultCompetency ON tblAssignment.AssignmentID = tblResultCompetency.AssignmentID ";
		query += "WHERE tblAssignment.SurveyID = " + surveyID
				+ " AND tblAssignment.TargetLoginID = " + targetID
				+ " AND tblAssignment.RaterCode = 'SELF'";

		Connection con = null;
		Statement st = null;
		ResultSet rs = null;

		try {
			con = ConnectionBean.getConnection();
			st = con.createStatement();
			rs = st.executeQuery(query);

			if (rs.next()) {
				answer = true;
			}
		} catch (Exception ex) {
			System.out
					.println("SPFIndividualReport.java - hasRatedSelfCompLevel - "
							+ ex.getMessage());
		} finally {
			ConnectionBean.closeRset(rs); // Close ResultSet
			ConnectionBean.closeStmt(st); // Close statement
			ConnectionBean.close(con); // Close connection
		}

		return answer;
	}

	/*
	 * To check if a particular survey and target rate himself or not for KB
	 * level survey
	 * 
	 * @author Albert
	 */
	public boolean hasRatedSelfKBLevel(int surveyID, int targetID) {
		String query = "";
		boolean answer = false;

		query += "SELECT DISTINCT tblAssignment.AssignmentID ";
		query += "FROM tblAssignment INNER JOIN tblResultBehaviour ON tblAssignment.AssignmentID = tblResultBehaviour.AssignmentID ";
		query += "WHERE tblAssignment.SurveyID = " + surveyID
				+ " AND tblAssignment.TargetLoginID = " + targetID
				+ " AND tblAssignment.RaterCode = 'SELF'";

		Connection con = null;
		Statement st = null;
		ResultSet rs = null;

		try {
			con = ConnectionBean.getConnection();
			st = con.createStatement();
			rs = st.executeQuery(query);

			if (rs.next()) {
				answer = true;
			}
		} catch (Exception ex) {
			System.out
					.println("SPFIndividualReport.java - hasRatedSelfKBLevel - "
							+ ex.getMessage());
		} finally {
			ConnectionBean.closeRset(rs); // Close ResultSet
			ConnectionBean.closeStmt(st); // Close statement
			ConnectionBean.close(con); // Close connection
		}

		return answer;
	}

	public int getMaxScale() throws SQLException {
		String query = "";
		int total = 0;

		query = query + "SELECT MAX(tblScale.ScaleRange) AS Result FROM ";
		query = query + "tblScale INNER JOIN tblSurveyRating ON ";
		query = query + "tblScale.ScaleID = tblSurveyRating.ScaleID WHERE ";
		query = query + "tblSurveyRating.SurveyID = " + surveyID;

		Connection con = null;
		Statement st = null;
		ResultSet rs = null;

		try {
			con = ConnectionBean.getConnection();
			st = con.createStatement();
			rs = st.executeQuery(query);

			if (rs.next()) {
				total = rs.getInt(1);
			}

		} catch (Exception ex) {
			System.out.println("IndividualReport.java - getMaxScale - "
					+ ex.getMessage());
		} finally {
			ConnectionBean.closeRset(rs); // Close ResultSet
			ConnectionBean.closeStmt(st); // Close statement
			ConnectionBean.close(con); // Close connection
		}

		return total;
	}

	/**
	 * Get CP(All) values by Target
	 * 
	 * @return Vector
	 * 
	 * @author: Mark Oei
	 * @since v1.3.12.70 22 April 2010
	 */
	public Vector getCP() throws Exception {
		int type = 1;
		if (weightedAverage == true) {
			type = 10;
		}
		String[] surveyInfo = groupSurveyInfo();
		// System.out.println("Name Sequence " + surveyInfo[3]);
		String query = "select FamilyName, GivenName, TargetLoginID, RatingTaskID, CompetencyID, ";
		query += "ROUND(AVG(tblAvgMean.AvgMean), 2) as Result";
		query += " from [tblAvgMean] inner join [User] on PKUser=TargetLoginID ";
		query += "where SurveyID = " + surveyID + " and Type = " + type
				+ " and RatingTaskID = 1 ";
		query += " group by FamilyName, GivenName, TargetLoginID, RatingTaskID, CompetencyID order by TargetLoginID Asc, Result Desc";

		Vector v = new Vector();

		Connection con = null;
		Statement st = null;
		ResultSet rs = null;

		try {
			con = ConnectionBean.getConnection();
			st = con.createStatement();
			rs = st.executeQuery(query);
			while (rs.next()) {
				String[] arr = new String[3];
				if (Integer.parseInt(surveyInfo[3]) == 0)
					arr[0] = rs.getString("FamilyName").trim() + " "
							+ rs.getString("GivenName").trim();
				else
					arr[0] = rs.getString("GivenName").trim() + " "
							+ rs.getString("FamilyName").trim();

				arr[1] = rs.getString("TargetLoginID");
				arr[2] = rs.getString("Result");
				// System.out.println("IndividualResult = " + arr[2]);
				v.add(arr);
			}
		} catch (Exception ex) {
			System.out.println("Error in IndividualReportSPF.java - getCP - "
					+ ex.getMessage());
		} finally {
			ConnectionBean.closeRset(rs); // Close ResultSet
			ConnectionBean.closeStmt(st); // Close statement
			ConnectionBean.close(con); // Close connection
		}

		return v;
	} // End of getCP()

	public int getTotalTarget() throws SQLException {
		int total = 0;

		String query = "SELECT COUNT(DISTINCT tblAssignment.TargetLoginID) AS Total ";
		query += "FROM         tblAssignment INNER JOIN ";
		query += "[User] ON tblAssignment.TargetLoginID = [User].PKUser ";
		query += "WHERE     tblAssignment.SurveyID =  " + surveyID;

		Connection con = null;
		Statement st = null;
		ResultSet rs = null;

		try {
			con = ConnectionBean.getConnection();
			st = con.createStatement();
			rs = st.executeQuery(query);

			if (rs.next())
				total = rs.getInt(1);

		} catch (Exception ex) {
			System.out.println("GroupReport.java - getTotalTarget - "
					+ ex.getMessage());
		} finally {
			ConnectionBean.closeRset(rs); // Close ResultSet
			ConnectionBean.closeStmt(st); // Close statement
			ConnectionBean.close(con); // Close connection
		}

		return total;
	}

	public Vector getTotalNumberOfGroups(int surveyID) throws SQLException {
		Vector total = new Vector();

		String query = "select distinct type as result from tblAvgMean where SurveyID="
				+ surveyID + " and Type<>1 and Type<>10  order by type";

		Connection con = null;
		Statement st = null;
		ResultSet rs = null;

		try {
			con = ConnectionBean.getConnection();
			st = con.createStatement();
			rs = st.executeQuery(query);

			while (rs.next())
				total.add(rs.getInt("result"));

		} catch (Exception ex) {
			System.out.println("GroupReport.java - getTotalTarget - "
					+ ex.getMessage());
		} finally {
			ConnectionBean.closeRset(rs); // Close ResultSet
			ConnectionBean.closeStmt(st); // Close statement
			ConnectionBean.close(con); // Close connection
		}

		return total;
	}

	public Vector getSurveyOverViewGroups(int surveyID, int targetID)
			throws SQLException {
		Vector total = new Vector();

		String query = "SELECT distinct type FROM tbl_prelimqnans inner join tblassignment on fkraterid = raterloginid inner join";
		query += " tblavgmean on tblavgmean.targetloginid=tblassignment.targetloginid where tblassignment.targetloginid="
				+ targetID;

		Connection con = null;
		Statement st = null;
		ResultSet rs = null;

		try {
			con = ConnectionBean.getConnection();
			st = con.createStatement();
			rs = st.executeQuery(query);

			while (rs.next())
				total.add(rs.getInt("type"));

		} catch (Exception ex) {
			System.out
					.println("individualreport.java - getSurveyOverViewGroups - "
							+ ex.getMessage());
		} finally {
			ConnectionBean.closeRset(rs); // Close ResultSet
			ConnectionBean.closeStmt(st); // Close statement
			ConnectionBean.close(con); // Close connection
		}

		return total;
	}

	/******************************* TARGET RANK ***********************************************/
	/**
	 * Writes target rank to excel.
	 */
	public void printTargetRank() throws SQLException, IOException, Exception {

		System.out.println("8. Group Ranking Report");
		// int[] address2 =
		// OO.findString(xSpreadsheet,"<Group Ranking Report Title>");
		String info = "This report lists the MM Feedback Recipients in ranked order based on the aggregate of ";
		info += "all their CP (All) scores. The report is in descending order starting with the strongest MM Feedback Recipients.";
		int row = 0;

		// OO.findAndReplace(xSpreadsheet,
		// "<Group Ranking Report Title>","GROUP RANKING TABLE");
		OO.mergeCells(xSpreadsheet5,column,column + 4,row,row);
		OO.insertString(xSpreadsheet5,"GROUP RANKING TABLE",row,0);
		OO.setFontSize(xSpreadsheet5,0,11,row,row,16);
		OO.setFontBold(xSpreadsheet5,0,11,row,row);

		row += 2;
		OO.mergeCells(xSpreadsheet5,column,column + 11,row,row + 1);

		OO.insertString(xSpreadsheet5,info,row,column);
		// OO.setRowHeight(xSpreadsheet5, row, 0, 1120);
		row += 2;
		// Code copied from above
		// OO.findAndReplace(xSpreadsheet,
		// "<Table title>","GROUP RANKING TABLE");

		// int[] address = OO.findString(xSpreadsheet4, "<Group Rank>");
		// column = address[0];
		// row = address[1];
		// OO.findAndReplace(xSpreadsheet, "<Group Rank>", " ");

		int maxScale = getMaxScale();
		// End of copying

		Vector cpAll = new Vector(); // create an object to store the
										// CP(All) values
		vCPValues.clear(); // clear the previous values and reuse the
							// object
		cpAll.clear(); // clear all values from the object
		cpAll = getCP(); // get the CP(All) from database

		int totalTarget = getTotalTarget();

		int currentElement = 0;
		int count = 0;
		double sum = 0.0;
		String lastTarget[] = (String[]) cpAll.lastElement(); // get the
																// last
																// target
		String currTarget[] = (String[]) cpAll.firstElement(); // get
																// the
																// first
																// target
		String arr[] = new String[currTarget.length];
		row++;

		for (int targetLoop = 0; targetLoop < totalTarget; targetLoop++) {
			sum = 0.0; // for adding up all the different CP(All)
			count = 0; // for determing the number of CP(All) to add

			if (currentElement >= cpAll.size()) {
				currentElement -= 1;
			}

			currTarget = (String[]) cpAll.elementAt(currentElement); // get
																		// the
																		// current
																		// target
			for (int calcLoop = 0; calcLoop < cpAll.size(); calcLoop++) {
				arr = (String[]) cpAll.elementAt(calcLoop);
				// System.out.println(arr[0]+arr[1]+arr[2]);
				if (currTarget[1].equals(arr[1])) {
					sum = sum + Double.parseDouble(arr[2]);
					count++;
				}
			}

			sum = Math.round(sum * 100.0) / 100.0; // format values to 2
			// decimal places // decimal places
			// System.out.println("testing " + currTarget[1] + " " +
			// arr[1] + " " + count + " " + targetLoop);
			String[] targetInfo = new String[3];
			targetInfo[0] = currTarget[0];

			targetInfo[1] = Double.toString(sum);

			targetInfo[2] = currTarget[1];

			vCPValues.add(targetInfo);
			currentElement += count;

			// System.out.println(currentElement + " Current Target " +
			// currTarget[0] + " CP " + avg );

		}

		maxScale = count * maxScale; // set the maxScale based on number
										// of competencies
		vCPValues = G.sortingWithID(vCPValues,1); // sorting the CP (All)
													// values

		int startBorder = row;
		String sName = "Name";
		String sCPScore = "CP Score";

		if (ST.LangVer == 2) {
			sName = "Nama";
			sCPScore = "Selisih";
		}

		OO.insertString(xSpreadsheet5,"S/N",row,column);
		OO.insertString(xSpreadsheet5,sName,row,column + 2);
		OO.mergeCells(xSpreadsheet5,10,11,row,row);
		OO.insertString(xSpreadsheet5,sCPScore,row,column + 10);
		OO.setFontBold(xSpreadsheet5,startColumn,column + 10,row,row);
		OO.setCellAllignment(xSpreadsheet5,column + 9,column + 9,row,row,1,2);
		OO.setBGColor(xSpreadsheet5,startColumn,column + 10,row,row,BGCOLOR);

		row++;
		int i = 0;
		String[] target = new String[totalTarget];
		double cpOverall[] = new double[totalTarget];
		String nameFiller = "Feedback Recipient ";
		int nameCount = 1;
		for (int j = 0; i < vCPValues.size(); j++) {
			arr = (String[]) vCPValues.elementAt(i);

			target[i] = arr[0];

			int targetID1 = Integer.parseInt(arr[2]);
			if (j % 30 == 0 && j > 0) {
				OO.insertPageBreak(xSpreadsheet5,0,11,row);

				OO.insertString(xSpreadsheet5,"S/N",row,column);
				OO.insertString(xSpreadsheet5,sName,row,column + 2);
				OO.mergeCells(xSpreadsheet5,10,11,row,row);
				OO.insertString(xSpreadsheet5,sCPScore,row,column + 10);
				OO.setFontBold(xSpreadsheet5,startColumn,column + 10,row,row);
				OO.setCellAllignment(xSpreadsheet5,column + 9,column + 9,row,
						row,1,2);
				OO.setBGColor(xSpreadsheet5,startColumn,column + 10,row,row,
						BGCOLOR);
				OO.setTableBorder(xSpreadsheet5,0,11,row,row,false,false,true,
						true,true,true);
				row++;
			}
			cpOverall[i] = Double.valueOf(
					((String[]) vCPValues.elementAt(i))[1]).doubleValue();

			OO.mergeCells(xSpreadsheet5,column + 2,column + 4,row,row);
			OO.setFontSize(xSpreadsheet5,column,column + 11,row,row,12);
			OO.setFontNormal(xSpreadsheet5,startColumn,endColumn,row,row);
			OO.setFontRemoveItalic(xSpreadsheet5,startColumn,endColumn,row,row);
			OO.setRowHeight(xSpreadsheet5,row,1,
					ROWHEIGHT * OO.countTotalRow(nameFiller,90));

			if (target[i] != null) {
				OO.insertNumeric(xSpreadsheet5,i + 1,row,column);
				if (targetID1 == targetID) {
					// OO.mergeCells(xSpreadsheet, startColumn+2,startColumn+7,
					// row, row);
					OO.setFontBold(xSpreadsheet5,startColumn,endColumn,row,row);
					OO.setBGColor(xSpreadsheet5,startColumn,endColumn - 1,row,
							row,16737792);
					OO.insertString(xSpreadsheet5,target[i],row,column + 2);
				} else {
					// OO.mergeCells(xSpreadsheet, startColumn+2,startColumn+7,
					// row,row);
					OO.insertString(xSpreadsheet5,nameFiller + nameCount,row,
							column + 2);
					nameCount++;
				}
				OO.mergeCells(xSpreadsheet5,10,11,row,row);
				OO.insertNumeric(xSpreadsheet5,cpOverall[i],row,column + 10);
				OO.setCellAllignment(xSpreadsheet5,10,11,row,row,1,2);
			}
			i++;
			row++;

		}

		int endBorder = row - 1;
		OO.setTableBorder(xSpreadsheet5,0,11,startBorder,endBorder,false,false,
				true,true,true,true);
		// OO.setFontType(xSpreadsheet5, 0, 11, 0, 10000, "Times New Roman");
		// OO.setFontSize(xSpreadsheet5, 0, 11, 0, 10000, 12);
		row++;
		groupRankingTableRow = row;
	}

	public int[] getSurveyRaterStatus(int targetLoginID, String raterCode) {
		{
			int[] results = new int[2];
			Connection con = null;
			Statement st = null;
			ResultSet rs = null;
			String query = "SELECT count(distinct RaterLoginID) as Distribution,count(RaterStatus)as Rated ";
			query += "  FROM tbl_PrelimQnAns a inner join tblAssignment b on a.FKRaterID=b.RaterLoginID  and b.AssignmentID=a.FKAssignmentID";
			query += " inner join tbl_PrelimQn c on a.FKPrelimQnID=c.PrelimQnID where TargetLoginID="
					+ targetLoginID + " and RaterCode like '" + raterCode + "'";

			try {

				con = ConnectionBean.getConnection();
				st = con.createStatement();
				rs = st.executeQuery(query);

				while (rs != null && rs.next()) {
					results[0] = rs.getInt("Distribution");
					results[1] = rs.getInt("Rated");
				}

			} catch (Exception E) {
				System.err
						.println("PrelimQuestionController.java - updatePrelimQnHeader - "
								+ E);
			} finally {
				ConnectionBean.closeStmt(st); // Close statement
				ConnectionBean.close(con); // Close connection
			}
			return results;
		}

	}

	public Vector getCPScoreRanking(int compID, int surveyID) {
		Vector v = new Vector();
		Connection con = null;
		Statement st = null;
		ResultSet rs = null;
		String query = "Select FamilyName,GivenName ,round(AvgMean,2) as result ,targetLoginID from tblAvgMean a";
		query += "inner join Competency b on CompetencyID=PKCompetency ";
		query += "inner join [User] on targetLoginID = PKUser where CompetencyID ="
				+ compID + " and SurveyID=" + surveyID + " and TYPE =10";
		query += "  order by AvgMean DESC   ";
		try {
			con = ConnectionBean.getConnection();
			st = con.createStatement();
			rs = st.executeQuery(query);
			while (rs != null && rs.next()) {
				String[] nameScorePair = new String[3];
				nameScorePair[0] = rs.getString("FamilyName") + " "
						+ rs.getString("GivenName");
				nameScorePair[1] = String.valueOf(rs.getDouble("result"));
				nameScorePair[2] = rs.getString("targetLoginID");
				v.add(nameScorePair);
			}
		} catch (Exception E) {
			System.err
					.println("SPFIndividualReport.java - getCPScoreRanking - "
							+ E);
		} finally {
			ConnectionBean.closeStmt(st); // Close statement
			ConnectionBean.close(con); // Close connection
		}
		return v;
	}

	public void insertCompetencyRankTable() {
		Vector vComp = null;
		int row = 0;
		/*
		 * get clusters
		 */
		try {
			// OO.setFontSize(xSpreadsheet4, 0, 11, row, 1500, 12);
			// OO.setFontType(xSpreadsheet4, 0, 11, row, 1500,
			// "Times new Roman");
			Vector vClust = ClusterByName();

			if (vClust.size() > 0) {
				for (int i = 0; i < vClust.size(); i++) {
					voCluster voClust = (voCluster) vClust.elementAt(i);
					String clusterName = voClust.getClusterName();
					int clusterID = voClust.getClusterID();
					/*
					 * Put Self under the sheet with the group ranking table as
					 * it has only one column
					 */
					if (clusterName.equalsIgnoreCase("SELF")) {
						int startRow = groupRankingTableRow;
						int column = 0;
						String info = "This Group Ranking Table lists the MM 360 Feedback Recipients in ranked order based on their CP (All) scores ";
						info += "for each cluster and competency. The ranking is in descending order starting with the strongest MM 360 Feedback Recipient.";
						/*
						 * Table must be on another page
						 */
						OO.insertPageBreak(xSpreadsheet5,startColumn,endColumn,
								startRow);

						OO.insertString(
								xSpreadsheet5,
								"GROUP RANKING TABLE BY CLUSTER AND COMPETENCY",
								startRow,column);
						OO.setFontBold(xSpreadsheet5,0,11,startRow,startRow);
						OO.setFontSize(xSpreadsheet5,0,11,startRow,startRow,16);
						startRow += 2;
						OO.mergeCells(xSpreadsheet5,column,column + 11,
								startRow,startRow + 1);

						OO.insertString(xSpreadsheet5,info,startRow,column);
						OO.setRowHeight(xSpreadsheet5,startRow,0,1120);
						startRow += 3;

						vComp = ClusterCompetencyByName(clusterID);
						voCompetency v1 = (voCompetency) vComp.elementAt(0);
						String competencyName = v1.getCompetencyName();

						int compID = v1.getCompetencyID();
						int startOfTable = startRow;

						OO.mergeCells(xSpreadsheet5,column,column + 1,startRow,
								startRow);
						OO.insertString(xSpreadsheet5,clusterName,startRow,
								column);
						OO.setCellAllignment(xSpreadsheet5,0,1,startRow,
								startRow,1,2);
						OO.setBGColor(xSpreadsheet5,0,1,startRow,startRow,
								BGCOLORCLUSTER);

						OO.mergeCells(xSpreadsheet5,column + 2,column + 11,
								startRow,startRow);
						OO.insertString(xSpreadsheet5,competencyName,startRow,
								column + 2);
						startRow++;

						OO.mergeCells(xSpreadsheet5,column,column + 1,startRow,
								startRow);
						OO.insertString(xSpreadsheet5,"Ranked",startRow,column);
						OO.setCellAllignment(xSpreadsheet5,0,1,startRow,
								startRow,1,2);
						OO.mergeCells(xSpreadsheet5,column + 2,column + 9,
								startRow,startRow);
						OO.insertString(xSpreadsheet5,"Name",startRow,
								column + 2);
						OO.mergeCells(xSpreadsheet5,column + 10,column + 11,
								startRow,startRow);
						OO.insertString(xSpreadsheet5,"CP Score",startRow,
								column + 10);

						OO.setBGColor(xSpreadsheet5,0,11,startRow,startRow,
								BGCOLOR);
						OO.setTableBorder(xSpreadsheet5,0,11,startRow,startRow,
								true,true,true,true,true,true);
						OO.setCellAllignment(xSpreadsheet5,10,11,startRow,
								startRow,1,2);
						OO.setFontBold(xSpreadsheet5,0,11,startRow - 1,startRow);
						startRow++;

						Vector rankingTable = getCPScoreRanking(compID,surveyID);

						rankingTable = sortClusterCompetency(rankingTable);
						/*
						 * Each element returns: [0] = Individual's Name [1] =
						 * CP(All) Scores [2] = targetID
						 */

						for (int k = 0; k < rankingTable.size(); k++) {
							String[] individualScorePair = (String[]) rankingTable
									.elementAt(k);
							if (k % 30 == 0 && k > 0) {
								OO.insertPageBreak(xSpreadsheet5,0,11,startRow);
								OO.mergeCells(xSpreadsheet5,column,column + 1,
										startRow,startRow);
								OO.insertString(xSpreadsheet5,clusterName,
										startRow,column);
								OO.setCellAllignment(xSpreadsheet5,0,1,
										startRow,startRow,1,2);
								OO.setBGColor(xSpreadsheet5,0,1,startRow,
										startRow,BGCOLORCLUSTER);

								OO.mergeCells(xSpreadsheet5,column + 2,
										column + 11,startRow,startRow);
								OO.insertString(xSpreadsheet5,competencyName,
										startRow,column + 2);
								startRow++;

								OO.mergeCells(xSpreadsheet5,column,column + 1,
										startRow,startRow);
								OO.insertString(xSpreadsheet5,"Ranked",
										startRow,column);
								OO.setCellAllignment(xSpreadsheet5,0,1,
										startRow,startRow,1,2);
								OO.mergeCells(xSpreadsheet5,column + 2,
										column + 9,startRow,startRow);
								OO.insertString(xSpreadsheet5,"Name",startRow,
										column + 2);
								OO.mergeCells(xSpreadsheet5,column + 10,
										column + 11,startRow,startRow);
								OO.insertString(xSpreadsheet5,"CP Score",
										startRow,column + 10);

								OO.setBGColor(xSpreadsheet5,0,11,startRow,
										startRow,BGCOLOR);
								OO.setTableBorder(xSpreadsheet5,0,11,startRow,
										startRow,true,true,true,true,true,true);
								OO.setCellAllignment(xSpreadsheet5,10,11,
										startRow,startRow,1,2);
								OO.setFontBold(xSpreadsheet5,0,11,startRow - 1,
										startRow);
								startRow++;
							}
							int target = Integer
									.parseInt(individualScorePair[2]);
							OO.mergeCells(xSpreadsheet5,0,1,startRow,startRow);
							OO.insertNumeric(xSpreadsheet5,(k + 1),startRow,0);
							OO.setCellAllignment(xSpreadsheet5,0,1,startRow,
									startRow,1,2);
							/*
							 * Blank out Name is target is not the user of this
							 * individual Report
							 */
							if (target != targetID) {
								OO.mergeCells(xSpreadsheet5,2,4,startRow,
										startRow);
								OO.insertString(xSpreadsheet5,
										"Feedback Recipient " + (k + 1),
										startRow,2);
								OO.mergeCells(xSpreadsheet5,column + 10,
										column + 11,startRow,startRow);
								OO.insertNumeric(
										xSpreadsheet5,
										Math.round(Double
												.parseDouble(individualScorePair[1]) * 100.00) / 100.00,
										startRow,10);
								OO.setCellAllignment(xSpreadsheet5,10,11,
										startRow,startRow,1,2);
								startRow++;
							} else {
								OO.mergeCells(xSpreadsheet5,2,4,startRow,
										startRow);
								OO.insertString(xSpreadsheet5,
										individualScorePair[0],startRow,2);
								OO.insertNumeric(
										xSpreadsheet5,
										Math.round(Double
												.parseDouble(individualScorePair[1]) * 100.00) / 100.00,
										startRow,10);
								OO.mergeCells(xSpreadsheet5,column + 10,
										column + 11,startRow,startRow);
								OO.setCellAllignment(xSpreadsheet5,10,11,
										startRow,startRow,1,2);
								OO.setFontBold(xSpreadsheet5,0,11,startRow,
										startRow);
								OO.setBGColor(xSpreadsheet5,0,11,startRow,
										startRow,16737792);
								startRow++;
							}

						}
						OO.setTableBorder(xSpreadsheet5,0,11,startOfTable,
								startRow - 1,false,false,true,true,true,true);

					} else {
						OO.mergeCells(xSpreadsheet4,0,2,row,row);
						OO.insertString(xSpreadsheet4,clusterName,row,0);
						OO.setBGColor(xSpreadsheet4,0,2,row,row,BGCOLORCLUSTER);
						OO.setTableBorder(xSpreadsheet4,0,2,row,row,true,true,
								true,true,true,true);
						OO.setFontBold(xSpreadsheet4,0,10 + 1,row,row);
						OO.setCellAllignment(xSpreadsheet4,0,10 + 1,row,row,1,2);
						/*
						 * Return a vector with the Competencies in the
						 * particular cluster.
						 */
						vComp = ClusterCompetencyByName(clusterID);
						vComp = sortClusterCompetencyOrder(vComp);

						/*
						 * For use later to fill in the 1st row of the next
						 * cluster with people.
						 */
						int numberOfPeople = 0;

						int competencyStartCol = 3;
						int startRow = row;
						/*
						 * Start of running through the competencies
						 */
						Vector clusterScores = new Vector();
						for (int j = 0; j < vComp.size(); j++) {
							/*
							 * Within a cluster, iterate through the vector to
							 * get the data out for each competency
							 * 
							 * @listing = the row which will be running based on
							 * the number of people participating in the survey.
							 */

							int listing = startRow;
							voCompetency v1 = (voCompetency) vComp.elementAt(j);
							String competencyName = v1.getCompetencyName();
							int compID = v1.getCompetencyID();
							/*
							 * Insert the header - Name and CPScore
							 */
							OO.mergeCells(xSpreadsheet4,competencyStartCol,
									competencyStartCol + 1,listing,listing);
							OO.insertString(xSpreadsheet4,competencyName,
									listing,competencyStartCol);
							OO.setTableBorder(xSpreadsheet4,competencyStartCol,
									competencyStartCol + 1,listing,listing,
									true,true,true,true,true,true);
							OO.setFontBold(xSpreadsheet4,competencyStartCol,
									competencyStartCol + 1,listing,listing);
							OO.setCellAllignment(xSpreadsheet4,
									competencyStartCol,competencyStartCol + 1,
									listing,listing,1,2);
							listing++;
							OO.insertString(xSpreadsheet4,"Name",listing,
									competencyStartCol);
							OO.insertString(xSpreadsheet4,"CP Score",listing,
									competencyStartCol + 1);
							OO.setBGColor(xSpreadsheet4,competencyStartCol,
									competencyStartCol + 1,listing,listing,
									BGCOLOR);
							listing++;
							/*
							 * Query the database to get the CP(All) scores for
							 * the particular Competency for everyone in
							 * descending order
							 */

							Vector rankingTable = getCPScoreRanking(compID,
									surveyID);
							rankingTable = sortClusterCompetency(rankingTable);
							/*
							 * Each element returns: [0] = Individual's Name [1]
							 * = CP(All) Scores [2] = targetID
							 */
							numberOfPeople = rankingTable.size();

							for (int k = 0; k < rankingTable.size(); k++) {
								String[] individualScorePair = (String[]) rankingTable
										.elementAt(k);

								if (k % 30 == 0 && k > 0) {

									OO.insertPageBreak(xSpreadsheet4,0,11,
											listing);

									OO.mergeCells(xSpreadsheet4,
											competencyStartCol,
											competencyStartCol + 1,listing,
											listing);
									OO.insertString(xSpreadsheet4,
											competencyName,listing,
											competencyStartCol);
									OO.setTableBorder(xSpreadsheet4,
											competencyStartCol,
											competencyStartCol + 1,listing,
											listing,true,true,true,true,true,
											true);
									OO.setFontBold(xSpreadsheet4,
											competencyStartCol,
											competencyStartCol + 1,listing,
											listing);
									OO.setCellAllignment(xSpreadsheet4,
											competencyStartCol,
											competencyStartCol + 1,listing,
											listing,1,2);
									listing++;
									OO.insertString(xSpreadsheet4,"Name",
											listing,competencyStartCol);
									OO.insertString(xSpreadsheet4,"CP Score",
											listing,competencyStartCol + 1);
									OO.setBGColor(xSpreadsheet4,
											competencyStartCol,
											competencyStartCol + 1,listing,
											listing,BGCOLOR);
									listing++;
								}
								int target = Integer
										.parseInt(individualScorePair[2]);
								/*
								 * Blank out Name is target is not the user of
								 * this individual Report
								 */
								if (target != targetID) {
									OO.insertString(xSpreadsheet4,
											"Feedback Recipient " + (k + 1),
											listing,competencyStartCol);
									OO.insertNumeric(
											xSpreadsheet4,
											Math.round(Double
													.parseDouble(individualScorePair[1]) * 100.00) / 100.00,
											listing,competencyStartCol + 1);
									OO.setTableBorder(xSpreadsheet4,
											competencyStartCol,
											competencyStartCol + 1,listing,
											listing,false,true,true,true,false,
											false);
									listing++;
								} else {
									OO.insertString(xSpreadsheet4,
											individualScorePair[0],listing,
											competencyStartCol);

									OO.insertNumeric(
											xSpreadsheet4,
											Math.round(Double
													.parseDouble(individualScorePair[1]) * 100.00) / 100.00,
											listing,competencyStartCol + 1);
									OO.setBGColor(xSpreadsheet4,
											competencyStartCol,
											competencyStartCol + 1,listing,
											listing,16737792);
									OO.setTableBorder(xSpreadsheet4,
											competencyStartCol,
											competencyStartCol + 1,listing,
											listing,false,true,true,true,false,
											false);
									OO.setFontBold(xSpreadsheet4,
											competencyStartCol,
											competencyStartCol + 1,listing,
											listing);
									listing++;
								}
								boolean isExist = false;
								int index = 0;
								double updatedScore = Double
										.parseDouble(individualScorePair[1]);
								for (int m = 0; m < clusterScores.size(); m++) {
									String[] existingIndividualScorePair = (String[]) clusterScores
											.elementAt(m);
									if (existingIndividualScorePair[0]
											.equalsIgnoreCase(individualScorePair[0])) {
										isExist = true;
										index = m;
										updatedScore += Double
												.parseDouble(existingIndividualScorePair[1]);
										updatedScore = Math
												.round(updatedScore * 100.00) / 100.00;
									}
								}
								String[] updateRecord = new String[3];
								updateRecord[0] = individualScorePair[0];
								updateRecord[1] = String.valueOf(updatedScore);
								updateRecord[2] = individualScorePair[2];
								if (isExist == true) {
									clusterScores.set(index,updateRecord);
								} else {
									clusterScores.add(individualScorePair);
								}
							}
							competencyStartCol += 2;
						}
						OO.insertString(xSpreadsheet4,"Name",startRow + 1,1);
						OO.insertString(xSpreadsheet4,"CP Score",startRow + 1,2);
						OO.setBGColor(xSpreadsheet4,0,1 + 1,startRow + 1,
								startRow + 1,BGCOLOR);
						int insertClusterScores = startRow + 2;
						clusterScores = sortClusterCompetency(clusterScores);
						for (int p = 0; p < clusterScores.size(); p++) {

							if (p % 30 == 0 && p > 0) {

								OO.mergeCells(xSpreadsheet4,0,2,
										insertClusterScores,insertClusterScores);
								OO.insertString(xSpreadsheet4,clusterName,
										insertClusterScores,0);
								OO.setTableBorder(xSpreadsheet4,0,2,
										insertClusterScores,
										insertClusterScores,true,true,true,
										true,true,true);
								OO.setFontBold(xSpreadsheet4,0,10 + 1,
										insertClusterScores,insertClusterScores);
								OO.setCellAllignment(xSpreadsheet4,0,10 + 1,
										insertClusterScores,
										insertClusterScores,1,2);
								OO.setBGColor(xSpreadsheet4,0,2,
										insertClusterScores,
										insertClusterScores,BGCOLORCLUSTER);
								OO.insertString(xSpreadsheet4,"Name",
										insertClusterScores + 1,1);
								OO.insertString(xSpreadsheet4,"CP Score",
										insertClusterScores + 1,2);
								OO.setBGColor(xSpreadsheet4,0,1 + 1,
										insertClusterScores + 1,
										insertClusterScores + 1,BGCOLOR);
								insertClusterScores += 2;
							}
							OO.insertString(xSpreadsheet4,(p + 1) + ". ",
									insertClusterScores,0);
							String[] clusterScoreTable = (String[]) clusterScores
									.elementAt(p);
							int target = Integer.parseInt(clusterScoreTable[2]);
							if (target != targetID) {
								OO.insertString(xSpreadsheet4,
										"Feedback Recipient " + (p + 1),
										insertClusterScores,1);
								OO.insertNumeric(
										xSpreadsheet4,
										Math.round((Double
												.parseDouble(clusterScoreTable[1]) / vComp
												.size()) * 100.00) / 100.00,
										insertClusterScores,2);
								insertClusterScores++;
							} else {
								OO.insertString(xSpreadsheet4,
										clusterScoreTable[0],
										insertClusterScores,1);

								OO.insertNumeric(
										xSpreadsheet4,
										Math.round((Double
												.parseDouble(clusterScoreTable[1]) / vComp
												.size()) * 100.00) / 100.00,
										insertClusterScores,2);
								OO.setFontBold(xSpreadsheet4,0,2,
										insertClusterScores,insertClusterScores);
								OO.setBGColor(xSpreadsheet4,1,2,
										insertClusterScores,
										insertClusterScores,16737792);
								insertClusterScores++;
							}

						}

						row += (numberOfPeople + ((numberOfPeople / 30) + 1) * 2);
						// row += 3;
						OO.insertPageBreak(xSpreadsheet4,0,11,row);
					}
				}
			} else {

				/*
				 * Return a vector with the Competencies in the particular
				 * cluster.
				 */
				vComp = Competency(0);

				/*
				 * For use later to fill in the 1st row of the next cluster with
				 * people.
				 */
				int numberOfPeople = 0;

				int competencyStartCol = 3;
				int startRow = row;
				/*
				 * Start of running through the competencies
				 */
				Vector clusterScores = new Vector();
				for (int j = 0; j < vComp.size(); j++) {
					/*
					 * Within a cluster, iterate through the vector to get the
					 * data out for each competency
					 * 
					 * @listing = the row which will be running based on the
					 * number of people participating in the survey.
					 */

					int listing = startRow;
					voCompetency v1 = (voCompetency) vComp.elementAt(j);
					String competencyName = v1.getCompetencyName();
					int compID = v1.getCompetencyID();
					/*
					 * Insert the header - Name and CPScore
					 */
					OO.mergeCells(xSpreadsheet4,competencyStartCol,
							competencyStartCol + 1,listing,listing);
					OO.insertString(xSpreadsheet4,competencyName,listing,
							competencyStartCol);
					OO.setTableBorder(xSpreadsheet4,competencyStartCol,
							competencyStartCol + 1,listing,listing,true,true,
							true,true,true,true);
					OO.setFontBold(xSpreadsheet4,competencyStartCol,
							competencyStartCol + 1,listing,listing);
					OO.setCellAllignment(xSpreadsheet4,competencyStartCol,
							competencyStartCol + 1,listing,listing,1,2);
					listing++;
					OO.insertString(xSpreadsheet4,"Name",listing,
							competencyStartCol);
					OO.insertString(xSpreadsheet4,"CP Score",listing,
							competencyStartCol + 1);
					OO.setBGColor(xSpreadsheet4,competencyStartCol,
							competencyStartCol + 1,listing,listing,BGCOLOR);
					listing++;
					/*
					 * Query the database to get the CP(All) scores for the
					 * particular Competency for everyone in descending order
					 */

					Vector rankingTable = getCPScoreRanking(compID,surveyID);
					// rankingTable = sortClusterCompetency(rankingTable);
					/*
					 * Each element returns: [0] = Individual's Name [1] =
					 * CP(All) Scores [2] = targetID
					 */
					numberOfPeople = rankingTable.size();

					for (int k = 0; k < rankingTable.size(); k++) {
						String[] individualScorePair = (String[]) rankingTable
								.elementAt(k);

						int target = Integer.parseInt(individualScorePair[2]);
						/*
						 * Blank out Name is target is not the user of this
						 * individual Report
						 */
						if (target != targetID) {
							OO.insertString(xSpreadsheet4,"Feedback Recipient "
									+ (k + 1),listing,competencyStartCol);
							OO.insertNumeric(
									xSpreadsheet4,
									Math.round(Double
											.parseDouble(individualScorePair[1]) * 100.00) / 100.00,
									listing,competencyStartCol + 1);
							listing++;
						} else {
							OO.insertString(xSpreadsheet4,
									individualScorePair[0],listing,
									competencyStartCol);

							OO.insertNumeric(
									xSpreadsheet4,
									Math.round(Double
											.parseDouble(individualScorePair[1]) * 100.00) / 100.00,
									listing,competencyStartCol + 1);
							OO.setBGColor(xSpreadsheet4,competencyStartCol,
									competencyStartCol + 1,listing,listing,
									16737792);
							OO.setFontBold(xSpreadsheet4,competencyStartCol,
									competencyStartCol + 1,listing,listing);
							listing++;
						}
						boolean isExist = false;
						int index = 0;
						double updatedScore = Double
								.parseDouble(individualScorePair[1]);
						for (int m = 0; m < clusterScores.size(); m++) {
							String[] existingIndividualScorePair = (String[]) clusterScores
									.elementAt(m);
							if (existingIndividualScorePair[0]
									.equalsIgnoreCase(individualScorePair[0])) {
								isExist = true;
								index = m;
								updatedScore += Double
										.parseDouble(existingIndividualScorePair[1]);
								updatedScore = Math
										.round(updatedScore * 100.00) / 100.00;
							}
						}
						String[] updateRecord = new String[3];
						updateRecord[0] = individualScorePair[0];
						updateRecord[1] = String.valueOf(updatedScore);
						updateRecord[2] = individualScorePair[2];
						if (isExist == true) {
							clusterScores.set(index,updateRecord);
						} else {
							clusterScores.add(individualScorePair);
						}
					}
					competencyStartCol += 2;
				}
				OO.insertString(xSpreadsheet4,"Name",startRow + 1,1);
				OO.insertString(xSpreadsheet4,"CP Score",startRow + 1,2);
				OO.setBGColor(xSpreadsheet4,0,1 + 1,startRow + 1,startRow + 1,
						BGCOLOR);
				int insertClusterScores = startRow + 2;
				clusterScores = sortClusterCompetency(clusterScores);
				for (int p = 0; p < clusterScores.size(); p++) {
					OO.insertString(xSpreadsheet4,(p + 1) + ". ",
							insertClusterScores,0);
					String[] clusterScoreTable = (String[]) clusterScores
							.elementAt(p);
					int target = Integer.parseInt(clusterScoreTable[2]);
					if (target != targetID) {
						OO.insertString(xSpreadsheet4,"Feedback Recipient "
								+ (p + 1),insertClusterScores,1);
						OO.insertNumeric(xSpreadsheet4,Math.round((Double
								.parseDouble(clusterScoreTable[1]) / vComp
								.size()) * 100.00) / 100.00,
								insertClusterScores,2);
						insertClusterScores++;
					} else {
						OO.insertString(xSpreadsheet4,clusterScoreTable[0],
								insertClusterScores,1);

						OO.insertNumeric(xSpreadsheet4,Math.round((Double
								.parseDouble(clusterScoreTable[1]) / vComp
								.size()) * 100.00) / 100.00,
								insertClusterScores,2);
						OO.setFontBold(xSpreadsheet4,0,2,insertClusterScores,
								insertClusterScores);
						OO.setBGColor(xSpreadsheet4,1,2,insertClusterScores,
								insertClusterScores,16737792);
						insertClusterScores++;
					}

				}
				row += (numberOfPeople + ((numberOfPeople / 30) + 1) * 2);

				row += 3;
			}
		} catch (Exception e) {
			System.out
					.println("insertCopetencyRankTable - IndividualReport.java"
							+ e);
		}
	}

	public Vector sortClusterCompetency(Vector v) {
		Vector sorted = new Vector();
		int size = v.size();
		while (sorted.size() < size) {
			double maxValue = 0;
			String targetName = "";
			int index = 0;
			for (int i = 0; i < v.size(); i++) {
				String[] current = (String[]) v.elementAt(i);
				double score = Double.parseDouble(current[1]);
				if (score > maxValue) {
					maxValue = score;
					targetName = current[0];
					index = i;
				}
			}

			sorted.add(v.get(index));
			v.remove(index);
		}
		return sorted;
	}
	/*
	 * For new coders, this method is used to determine page length. Each row is
	 * approx 560(35 times the unit count in excel. In excel, 560(16x35) is
	 * equivalent to 16cm in excel) and a page is 25200 in height
	 */

	public void testpagelength() throws Exception {

		OO.insertPageBreak(xSpreadsheet,0,11,row);
		for (int i = 0; i < 1000; i++) {
			// OO.insertNumeric(xSpreadsheet, i, row, 0);
			OO.setRowHeight(xSpreadsheet,row,0,560);
			row++;
		}
	}
	public void InsertCalculateStatus() {
		try {
			double totalD = 0;
			double totalR = 0;
			int superiorDist = calculateStatus("SUP%",1);
			OO.findAndReplace(xSpreadsheet,"<supD>",
					String.valueOf(superiorDist));
			totalD += superiorDist;
			int peerDist = calculateStatus("peer%",1);
			OO.findAndReplace(xSpreadsheet,"<peerD>",String.valueOf(peerDist));
			totalD += peerDist;
			int DirectDist = calculateStatus("Dir%",1);
			OO.findAndReplace(xSpreadsheet,"<DRD>",String.valueOf(DirectDist));
			totalD += DirectDist;
			int IndirectDist = calculateStatus("IDR%",1);
			OO.findAndReplace(xSpreadsheet,"<idrD>",
					String.valueOf(IndirectDist));
			totalD += IndirectDist;
			int selfDist = calculateStatus("self%",1);
			OO.findAndReplace(xSpreadsheet,"<selfD>",String.valueOf(selfDist));
			totalD += selfDist;
			int superiorRec = calculateStatus("SUP%",0);
			OO.findAndReplace(xSpreadsheet,"<supR>",String.valueOf(superiorRec));
			totalR += superiorRec;
			int peerRec = calculateStatus("peer%",0);
			OO.findAndReplace(xSpreadsheet,"<peerR>",String.valueOf(peerRec));
			totalR += peerRec;
			int DirectRec = calculateStatus("Dir%",0);
			OO.findAndReplace(xSpreadsheet,"<DRR>",String.valueOf(DirectRec));
			totalR += DirectRec;
			int IndirectRec = calculateStatus("IDR%",0);
			OO.findAndReplace(xSpreadsheet,"<idrR>",String.valueOf(IndirectRec));
			totalR += IndirectRec;
			int selfRec = calculateStatus("self%",0);
			OO.findAndReplace(xSpreadsheet,"<selfR>",String.valueOf(selfRec));
			totalR += selfRec;
			double responseRate = 100 * totalR / totalD;
			OO.findAndReplace(xSpreadsheet,"<RR>",
					String.valueOf(df.format(responseRate)) + "%");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	// type 1=distributed, 0 = received
	public int calculateStatus(String catName, int type) {

		int distribution = 0;

		// retrieve the Assignment ID for a particular target login ID.
		// Comment updated on 1/4/2008 by Yun
		String query = "SELECT count(*) as dist from tblassignment where ratercode like '"
				+ catName + "'";
		query += " and surveyid = " + surveyID;

		if (type == 0) {
			query += " and CalculationStatus=1 ";
		}

		Connection con = null;
		Statement st = null;
		ResultSet rs = null;

		try {
			con = ConnectionBean.getConnection();
			st = con.createStatement();

			rs = st.executeQuery(query);
			if (rs.next())
				distribution = rs.getInt("dist");

		} catch (SQLException E) {
			System.err.println("spfindividual.java -  calculateStatus- " + E);
			E.printStackTrace();
		} finally {
			ConnectionBean.closeRset(rs); // Close ResultSet
			ConnectionBean.closeStmt(st); // Close statement
			ConnectionBean.close(con); // Close connection

		}
		return distribution;
	}
}
