package CP_Classes;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Vector;

import jxl.Workbook;
import jxl.format.Alignment;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.format.Colour;
import jxl.read.biff.BiffException;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import CP_Classes.vo.voUser;
import CP_Classes.vo.voCompetency;
import CP_Classes.vo.votblAssignment;
import CP_Classes.vo.votblSurvey;
import CP_Classes.vo.votblSurveyBehaviour;

public class Report_RaterInputForTarget_SPF
{
	private Setting server;
	private User user;
	private EventViewer ev;
	private Create_Edit_Survey CE_Survey;
	
	/**
	 * Declaration of new object of class SurveyResult.
	 */
	private SurveyResult S;
	
	
	private String sDetail[] = new String[13];
	private String targetdetail[] = new String[13];
 	private String itemName = "Report";
 	private int commentIncluded = 0;
 	private final double CHARWIDTH = 1.6;
	
	private Label label;
	private WritableSheet writesheet;
	private WritableCellFormat cellBOLD;
	private WritableFont fontBold, fontFace;
	private WritableWorkbook workbook;
	private WritableCellFormat cellBOLD_Border;
	private	WritableCellFormat bordersData1;
	private WritableCellFormat bordersData2;
	private WritableCellFormat No_Borders, No_Borders_ctrAll,No_Borders_ctrAll_Bold, No_Borders_NoBold, No_Borders_NoWrap;
	private File outputWorkBook, inputWorkBook;
	
	private int SurveyID = 0; //Denise 07/01/2010 store surveyID as global variable for method getHideNA and get NA_Exclude
	public Report_RaterInputForTarget_SPF()
	{
		server = new Setting();
		user = new User();
		ev = new EventViewer();
		CE_Survey = new Create_Edit_Survey();
		S = new SurveyResult();
	}
	
	public void write(String fileName, int sheetID, int TargetID) throws IOException, WriteException, BiffException, Exception
	{
		System.out.println("FILENAME IS "+fileName +" with sheetID = "+sheetID);
		if(sheetID==0){
			String output = server.getReport_Path()+"\\"+fileName;
			outputWorkBook = new File(output);
			
			inputWorkBook = new File(server.getReport_Path_Template() + "\\HeaderFooter.xls");
			Workbook inputFile = Workbook.getWorkbook(inputWorkBook);
			
			workbook = Workbook.createWorkbook(outputWorkBook, inputFile);
		}	
		votblSurvey voSurvey = CE_Survey.getSurveyDetail(SurveyID);
		int nameSeq = 0;
		if(voSurvey != null){
			nameSeq = voSurvey.getNameSequence();
		}
		String TargetDetail[] = new String[14];
		TargetDetail = user.getUserDetail(TargetID, nameSeq);
		writesheet = workbook.createSheet(""+TargetDetail[0]+", "+TargetDetail[1],sheetID);
		
		fontFace = new WritableFont(WritableFont.TIMES, 12, WritableFont.NO_BOLD);
		fontBold = new WritableFont(WritableFont.TIMES, 12, WritableFont.BOLD); 
		
		cellBOLD = new WritableCellFormat(fontBold); 
		
		cellBOLD_Border = new WritableCellFormat(fontBold); 
		cellBOLD_Border.setBorder(Border.ALL, BorderLineStyle.THIN);
		cellBOLD_Border.setAlignment(Alignment.CENTRE);
		cellBOLD_Border.setWrap(true);
		cellBOLD_Border.setBackground(Colour.GRAY_25); 
		
		bordersData1 = new WritableCellFormat(fontFace);
		bordersData1.setBorder(Border.ALL, BorderLineStyle.THIN);
		bordersData1.setAlignment(Alignment.CENTRE);
					
		bordersData2 = new WritableCellFormat(fontFace);
		bordersData2.setBorder(Border.ALL, BorderLineStyle.THIN);
		bordersData2.setWrap(true);
				
		No_Borders_ctrAll = new WritableCellFormat(fontFace);
		No_Borders_ctrAll.setBorder(Border.NONE, BorderLineStyle.NONE);
		No_Borders_ctrAll.setAlignment(Alignment.CENTRE);
		
		No_Borders_ctrAll_Bold = new WritableCellFormat(fontBold);
		No_Borders_ctrAll_Bold.setBorder(Border.NONE, BorderLineStyle.NONE);
		No_Borders_ctrAll_Bold.setAlignment(Alignment.CENTRE);
		
		No_Borders = new WritableCellFormat(fontBold);
		No_Borders.setBorder(Border.NONE, BorderLineStyle.NONE);
		
		No_Borders_NoWrap = new WritableCellFormat(fontFace);
		No_Borders_NoWrap.setBorder(Border.NONE, BorderLineStyle.NONE);
		
		No_Borders_NoBold = new WritableCellFormat(fontFace);
		No_Borders_NoBold.setBorder(Border.NONE, BorderLineStyle.NONE);
		No_Borders_NoBold.setWrap(true);
		
	}
	
	
	public void Header(int SurveyID, int TargetID) 
		throws IOException, WriteException, SQLException, Exception
	{
		Label label = new Label(0, 0, "Raters' Input For Target",cellBOLD);
		if (server.LangVer == 2)
			label = new Label(0, 0, "MASUKAN PENILAI UNTUK TARGET",cellBOLD);
		writesheet.addCell(label); 
		writesheet.mergeCells(0, 0, 2, 0);
		
		
		String CompName=" ";
		String OrgName =" ";
		String SurveyName = " ";
		int NameSequence=0;
		String SurveyLevel = " ";
		
		
		votblSurvey voSurvey = CE_Survey.getSurveyDetail(SurveyID);
		if(voSurvey != null)	
		{
			CompName = voSurvey.getCompanyName();
			OrgName = voSurvey.getOrganizationName();
			SurveyName = voSurvey.getSurveyName();
			NameSequence = voSurvey.getNameSequence();
			int LevelOfSurvey = voSurvey.getLevelOfSurvey();
			commentIncluded = voSurvey.getComment_Included();
			
			
			if(LevelOfSurvey == 0)
				SurveyLevel = "Competency Level";
				if (server.LangVer == 2)
					SurveyLevel = "Tingkat Kompetensi";
			else if(LevelOfSurvey == 1)
				SurveyLevel = "Key Behaviour Level";
				if (server.LangVer == 2)
					SurveyLevel = "Tingkat Perilaku Kunci";
			
		}
			
		int row_title = 2;
		
		label= new Label(0, row_title, "Company:",cellBOLD);
		if (server.LangVer == 2)
			label= new Label(0, row_title, "Nama Perusahaan:",cellBOLD);
		writesheet.addCell(label); 
		writesheet.setColumnView(1,15);
		writesheet.mergeCells(0, row_title, 1, row_title);
						
		label= new Label(2, row_title, UnicodeHelper.getUnicodeStringAmp(CompName), No_Borders);
		writesheet.addCell(label); 
		writesheet.setColumnView(0,6);

		label= new Label(0, row_title + 2, "Organisation:",cellBOLD);
		if (server.LangVer == 2)
			label= new Label(0, row_title + 2, "Nama Organisasi:",cellBOLD);
		writesheet.addCell(label); 
		writesheet.mergeCells(0, row_title + 2, 1, row_title + 2);
		
		label= new Label(2, row_title + 2 , UnicodeHelper.getUnicodeStringAmp(OrgName) ,No_Borders);
		writesheet.addCell(label); 
		
		label= new Label(0, row_title + 4, "Survey Name:",cellBOLD);
		if (server.LangVer == 2)
			label= new Label(0, row_title + 4, "Nama Survei:",cellBOLD);
		writesheet.addCell(label); 
		writesheet.mergeCells(0, row_title + 4, 1, row_title + 4);
		
		label= new Label(2, row_title + 4 , UnicodeHelper.getUnicodeStringAmp(SurveyName) ,No_Borders);
		writesheet.addCell(label); 
		
		String TargetDetail[] = new String[14];
		TargetDetail = user.getUserDetail(TargetID, NameSequence);
		
		label= new Label(0, row_title + 6, "Target Name:",cellBOLD);
		if (server.LangVer == 2)
			label= new Label(0, row_title + 6, "Nama Target:",cellBOLD);
		writesheet.addCell(label); 
		writesheet.mergeCells(0, row_title + 6, 1, row_title + 6);
		
		label= new Label(2, row_title + 6 , TargetDetail[0]+", "+TargetDetail[1] ,No_Borders);
		writesheet.addCell(label); 


		label= new Label(0, row_title + 8 , SurveyLevel,No_Borders);
		writesheet.addCell(label); 
		
		//Date timestamp = new Date();
		//SimpleDateFormat dFormat = new SimpleDateFormat("dd/MM/yyyy");
		//String temp = dFormat.format(timestamp);
		//System.out.println(temp);
		//writesheet.setHeader("", "", "Pacific Century Consulting Pte Ltd.");
		//writesheet.setFooter("Date of printing: " + temp +  "\n" + "Copyright �3-Sixty Profiler�is a product of Pacific Century Consulting Pte Ltd.", "", "Page &P of &N");
	
	}
	
	/*
	 * Change : put the self comment to the right most column
	 * Reason : Change format of the report
	 * Add by : Denise
	 * Add on : 10/12/2009
	 */
	public int printRaterHeader(int Result_col,int row_data, int SurveyID, int TargetID)throws IOException, WriteException, SQLException, Exception
	{
		int xAssID = 0;
		int totalCol = 0;
		String RaterCode="";
		AssignTarget_Rater ATR = new AssignTarget_Rater();
		Vector v = ATR.getAssignmentDetail(SurveyID, TargetID);
		
		int selfPos = -1;
		for(int i=0; i<v.size(); i++)	
		{			
			votblAssignment vo = (votblAssignment)v.elementAt(i);
			
			int AssID = vo.getAssignmentID();
			RaterCode = vo.getRaterCode();
			
			if (RaterCode.equals("SELF")){ //store the position of the SELF  rater
				selfPos = i;
			}
			if(xAssID != AssID &&  !RaterCode.equals("SELF")) //output other raters first
			{				
				label= new Label(Result_col, row_data, RaterCode , cellBOLD_Border);
				writesheet.addCell(label);
				writesheet.setColumnView(Result_col, (int)(Math.ceil(Math.ceil(RaterCode.length()/2 *CHARWIDTH))));
				if(RaterCode.length() > 6)
					writesheet.setColumnView(Result_col, (int)(Math.ceil(Math.ceil(RaterCode.length()))));	
				xAssID = AssID;
				Result_col = Result_col+1;	
				totalCol ++;
			}
		}
		
		if(selfPos!=-1 && xAssID != selfPos ) //if have self comment, output the SELF rater name
		{				
			label= new Label(Result_col, row_data, "SELF" , cellBOLD_Border);
			writesheet.addCell(label);	
			writesheet.setColumnView(Result_col, (int)(Math.ceil(Math.round(RaterCode.length() *1.1))));
			totalCol++;
		} 
		return totalCol;
	}
	
	public boolean AllRaters(int SurveyID, int TargetID, int PKUser, String fileName, boolean includeComment) throws IOException, WriteException, SQLException, Exception
	{
		boolean IsNull = false;	
		this.SurveyID = SurveyID; //Denise 07/01/2010

		Create_Edit_Survey CE_Survey = new Create_Edit_Survey();
		String OldName = CE_Survey.getSurveyName(SurveyID);

		Vector<Integer> targets = new Vector<Integer>();
		Vector vUsers = CE_Survey.getAllUsers(0, SurveyID);

		if(TargetID == 0){
			for(int m=0; m<vUsers.size(); m++){
				voUser vo = (voUser)vUsers.elementAt(m);
				int id = vo.getPKUser();
				targets.add(id);
			}
		}else{
			targets.add(TargetID);
		}
		System.out.println("targets size is "+targets.size());
		System.out.println("targets ids are: ");
		for(int test=0;test<targets.size();test++){
			System.out.println((test+1)+". "+targets.get(test));
		}
		for(int sheetID = 0; sheetID<targets.size(); sheetID++){
			TargetID = targets.get(sheetID);
			write(fileName,sheetID, TargetID);
			Header(SurveyID, TargetID);	

			int i = 0;
			int xAssID = 0;
			int row = 0;
			int row_data = 13;
			int xRatingTaskID  = 0;
			int Result_col = 2;
			int LevelOfSurvey = 0;
			int [] arr_AssID = new int[50];
			String[] raterCode = new String[50];

			AssignTarget_Rater ATR = new AssignTarget_Rater();
			Vector v = ATR.getTargetAssignmentIDs(SurveyID, TargetID);
			Vector v2 = ATR.getAssignmentDetail(SurveyID, TargetID);

			int selfPos = -1;

			//Denise 30/12/2009 find the position of SELF in the targetAssignemnt list
			int y = 0;
			while (y<v.size())
			{
				votblAssignment vo = (votblAssignment)v2.elementAt(y);		
				int AssID = vo.getAssignmentID();

				if(xAssID != AssID)
				{									
					if (vo.getRaterCode().equals("SELF"))
					{
						selfPos = i;
						break;
					}
					i++;
					xAssID = AssID;
				}
				y++;
			}

			i=0;
			xAssID = 0;

			for(int j=0; j<v.size(); j++)
			{
				votblAssignment vo = (votblAssignment)v.elementAt(j);
				int AssID = vo.getAssignmentID();

				if( xAssID!= AssID)
				{
					arr_AssID[i] = AssID;
					i++;
					xAssID = AssID;				
				}
			}
			//Denise 30/12/2009 to change the position of SELF to the end
			if (selfPos != -1)
			{
				int temp = arr_AssID[selfPos];
				for (int j = selfPos; j< arr_AssID.length-1; j++)
				{
					arr_AssID[j] = arr_AssID[j+1];
				}
				arr_AssID[i] = temp;
			}

			Vector vRating = ATR.getSurveyDetail(SurveyID, TargetID);
			row_data = printPrelimQn(row_data, SurveyID ,arr_AssID,TargetID );
			for(int j=0; j<vRating.size(); j++)
			{
				votblAssignment vo = (votblAssignment)vRating.elementAt(j);
				int col = 0;

				int RatingTaskID = vo.getRatingTaskID();
				String rating = vo.getRatingTaskName();
				LevelOfSurvey = vo.getLevelOfSurvey();
				int AssignmentID = vo.getAssignmentID();
				if(xRatingTaskID != RatingTaskID )
				{
					label= new Label(col, row_data-1, rating, No_Borders);
					writesheet.addCell(label);
					
					

					/* --------------------------START: Competency Level---------------------------*/
					label= new Label(col, row_data, "Competency",cellBOLD_Border);
					if (server.LangVer == 2)
						label= new Label(col, row_data, "Kompetensi",cellBOLD_Border);
					writesheet.addCell(label);
					writesheet.setColumnView(0,15);

					if(LevelOfSurvey == 0)
					{
						Result_col = 1;

						printRaterHeader(Result_col,row_data,SurveyID, TargetID);

						Vector vComp = ATR.getCompetencies(AssignmentID);

						row = row_data+1;
						int xCompID = 0;

						for(int k=0; k<vComp.size(); k++)
						{
							voCompetency voComp = (voCompetency)vComp.elementAt(k);

							col = 0;
							int CompID = voComp.getCompetencyID();
							String CompName = voComp.getCompetencyName();

							if(xCompID != CompID)
							{
								label= new Label(col, row, UnicodeHelper.getUnicodeStringAmp(CompName) , bordersData2);
								writesheet.addCell(label);
								
								// Edit by Denise 12/12/2009 use the new printRaterResult_KB function to change the SELF comment to the right most column
								//Edit by Denise 30/12/2009 use the old printRaterResult_KB function since the SELF comment has been moved to the end in the arr_AssID
								printRaterResult_Comp(Result_col, row, arr_AssID,CompID, RatingTaskID);      // can use this one if want to output the assignments
								// according to the order in the database
								//				printRaterResult_Comp(Result_col, row, arr_AssID,raterCode, CompID, RatingTaskID);

								xCompID = CompID;
								row = row+1;
							}

						}		
						col = col+1;
					}

					/* ---------------------------------------------START: Key Behaviour Level-------------------------------------*/

					if(LevelOfSurvey == 1)
					{
						Result_col = 2;

						printRaterHeader(Result_col,row_data,SurveyID, TargetID);
						label= new Label(col+1, row_data, "Key Behaviour Statement",cellBOLD_Border);
						if (server.LangVer == 2)
							label= new Label(col+1, row_data, "Pernyataan Perilaku Kunci",cellBOLD_Border);
						writesheet.addCell(label);
						writesheet.setColumnView(1,25);

						/*resultSQL = "SELECT * FROM tblSurveyBehaviour INNER JOIN";
					resultSQL = resultSQL + " Competency ON tblSurveyBehaviour.CompetencyID = Competency.PKCompetency AND";
					resultSQL = resultSQL + " tblSurveyBehaviour.CompetencyID = Competency.PKCompetency INNER JOIN";
					resultSQL = resultSQL + " KeyBehaviour ON tblSurveyBehaviour.KeyBehaviourID = KeyBehaviour.PKKeyBehaviour AND";
					resultSQL = resultSQL + " Competency.PKCompetency = KeyBehaviour.FKCompetency";
					resultSQL = resultSQL + " WHERE (tblSurveyBehaviour.SurveyID = "+SurveyID+" )";
						 */

						row = row_data+1;
						int xCompID = 0;
						int xKeyID = 0;

						SurveyKB SKB = new SurveyKB();
						Vector vKB = SKB.getSurveyKB(SurveyID);

						for(int k=0; k<vKB.size(); k++)
						{
							votblSurveyBehaviour voKB = (votblSurveyBehaviour)vKB.elementAt(k);
							col = 0;
							int CompID = voKB.getCompetencyID();
							String CompName = voKB.getCompetencyName();
							int KeyID = voKB.getKeyBehaviourID();
							String KeyName = voKB.getKBName();

							if(xCompID != CompID)
							{
								label= new Label(col, row, UnicodeHelper.getUnicodeStringAmp(CompName) , bordersData2);
								writesheet.addCell(label);

								label= new Label(col+1, row, " " , bordersData2);
								writesheet.addCell(label);

								xCompID = CompID;
							}
							else
							{
								label= new Label(col, row, " " , bordersData2);
								writesheet.addCell(label);

								label= new Label(col+1, row, " " , bordersData2);
								writesheet.addCell(label);

								label= new Label(col, row+1, " " , bordersData2);
								writesheet.addCell(label);
							}

							if(xKeyID != KeyID)
							{
								label= new Label(col, row+1, " " , bordersData2);
								writesheet.addCell(label);
								label= new Label(col+1, row+1, UnicodeHelper.getUnicodeStringAmp(KeyName) , bordersData2);
								writesheet.addCell(label);

								xKeyID = KeyID;
							}
							else
							{
								label= new Label(col+1, row-1, " " , bordersData2);
								writesheet.addCell(label);
							}

							// Edit by Denise 10/12/2009 use the new printRaterResult_KB function to change the SELF comment to the right most column
							//Edit by Denise 30/12/2009 use the old printRaterResult_KB function since the SELF comment has been moved to the end in the arr_AssID
							printRaterResult_KB(Result_col, row, arr_AssID, KeyID, RatingTaskID);		// can use this one if want to output the assignments
							// according to the order in the database
							//			printRaterResult_KB(Result_col, row, arr_AssID,raterCode, KeyID, RatingTaskID);
							row = row + 2; 
						}					
					}
					xRatingTaskID  = RatingTaskID ;	
				}
				row_data = row + 2;
			}
			/*
			 * Change : Put checking whether the comment is included in report or not
			 * Reason : no checking before
			 * Add by : Johanes
			 * Add on : 26/10/2009
			 */	
			if(commentIncluded == 1 && includeComment)
				row = printRaterComment(row+5, SurveyID, arr_AssID, TargetID);

			printAddQn(row+5, SurveyID, arr_AssID, TargetID);
			sDetail = CE_Survey.getUserDetail(PKUser);
			targetdetail = CE_Survey.getUserDetail(TargetID);
			ev.addRecord("Insert", itemName, "List Of Rater Input for "+targetdetail[0]+", "+targetdetail[1]+"(Target) for Survey "+OldName, sDetail[2], sDetail[11], sDetail[10]);

		}
		workbook.write();
		workbook.close(); 
		return IsNull;
	}
	
	/**
	 * 
	 * @param Result_col
	 * @param row
	 * @param arr_AssID
	 * @param KeyID
	 * @param RatingTaskID
	 * @throws IOException
	 * @throws WriteException
	 * @throws SQLException
	 * @throws Exception
	 */
	public void printRaterResult_KB(int Result_col, int row, int [] arr_AssID, int KeyID, int RatingTaskID)
		throws IOException, WriteException, SQLException, Exception
	{
		//Denise 06/01/2010 to check the hideNA and NA excluded option of the survey
		Create_Edit_Survey CE = new Create_Edit_Survey();
		boolean hideZero = CE.getHideNAOption(SurveyID)==1||CE.getNA_Included(SurveyID) == 0;
		
		for(int d = 0; d<arr_AssID.length; d++)
		{
			if(arr_AssID[d] != 0)
			{
				AssignTarget_Rater ATR = new AssignTarget_Rater();
				
				double result = ATR.getKBResult(arr_AssID[d], KeyID, RatingTaskID);
				
				double SurvResult = Math.round(result * 100.0) / 100.0;
				if(result != -1 && !(hideZero && (int)Math.round(SurvResult)==0))	//Denise 06/01/2010 , if the survey requires to hideNA or NA excluded and the result ==0
				{                                                           	// output empty cell						
					label= new Label(Result_col, row, " " , bordersData2);
					writesheet.addCell(label);
							
					Number num= new Number(Result_col, row+1, SurvResult, bordersData1);
					writesheet.addCell(num);
				}
				else
				{
					label= new Label(Result_col, row, " " , bordersData2);
					writesheet.addCell(label);
					
					label= new Label(Result_col, row+1, " ", bordersData2);
					writesheet.addCell(label);
				}
				
				Result_col = Result_col+1;
			}
		}
	}
	
	/**
	 * 
	 * @param Result_col
	 * @param row
	 * @param arr_AssID
	 * @param arr_raterCode
	 * @param KeyID
	 * @param RatingTaskID
	 * @throws IOException
	 * @throws WriteException
	 * @throws SQLException
	 * @throws Exception
	 */
	
	/*
	 * function printRaterResult_KB: print the result of each assignment. Use the arr_raterCode to store the rater of each assignment  
	 * Add by : Denise
	 * Add on : 10/12/2009
	 */		
/*	public void printRaterResult_KB(int Result_col, int row, int [] arr_AssID,String[] arr_raterCode, int KeyID, int RatingTaskID)
	throws IOException, WriteException, SQLException, Exception
	{
		int selfPos = -1;
		int d = 0;
		
		AssignTarget_Rater ATR = new AssignTarget_Rater();
		while ( d< arr_AssID.length && arr_raterCode[d]!=null)
		{	
			if (arr_raterCode[d].equals("SELF")) //store the position of the SELF assignment
				{
				  selfPos = d;
				  d++;
				  continue;
				}
			
			//output other assignments first
			printRaterResult_Cell(Result_col,row,arr_AssID[d], KeyID, RatingTaskID, arr_raterCode[d]);
			Result_col = Result_col+1;
			d++;
		}
		
		if (selfPos !=-1) //if have the SELF assignment, output the result
		{
			d = selfPos;
			printRaterResult_Cell(Result_col,row,arr_AssID[d], KeyID, RatingTaskID, arr_raterCode[d]);
		}
	}
	
	/*
	 * function printRaterResult_Cell: print the result of assignment AssID at row row and column Result_col
	 * Add by : Denise
	 * Add on : 10/12/2009
	 */	
/*	private void printRaterResult_Cell(int Result_col, int row, int  AssID, int KeyID, int RatingTaskID, String name)
	throws IOException, WriteException, SQLException, Exception
	{
		AssignTarget_Rater ATR = new AssignTarget_Rater();
		double result = ATR.getKBResult(AssID, KeyID, RatingTaskID);			
		if(result != -1)	
		{
			double SurvResult = Math.round(result * 100.0) / 100.0;
			
			label= new Label(Result_col, row, " " , bordersData2);
			writesheet.addCell(label);
				
			Number num= new Number(Result_col, row+1, SurvResult, bordersData1);
			writesheet.addCell(num);
		}
		else
		{
			label= new Label(Result_col, row, " " + name  , bordersData2);
			writesheet.addCell(label);
			
			label= new Label(Result_col, row+1, " " +name, bordersData2);
			writesheet.addCell(label);
		}
	}
	
	*/
	public void printRaterResult_Comp(int Result_col, int row, int [] arr_AssID, int CompID, int RatingTaskID)
	throws IOException, WriteException, SQLException, Exception{
	
		//Denise 06/01/2010 to check the hideNA and NA excluded option of the survey
		Create_Edit_Survey CE = new Create_Edit_Survey();
		boolean hideZero = CE.getHideNAOption(SurveyID)==1||CE.getNA_Included(SurveyID) == 0;

	for(int d = 0; d<arr_AssID.length; d++)
	{
		if(arr_AssID[d] != 0)
		{
			AssignTarget_Rater ATR = new AssignTarget_Rater();
			
			double result = ATR.getCompResult(arr_AssID[d], CompID, RatingTaskID);
		
			double SurvResult = Math.round(result * 100.0) / 100.0;
			
			if(result !=-1 && !(hideZero && (int)Math.round(SurvResult)==0))	//Denise 06/01/2010 , if the survey requires to hideNA and the result ==0
			{                                                               // output empty cell
				
				Number num= new Number(Result_col, row, SurvResult, bordersData1);
				writesheet.addCell(num);
			}
			else
			{
				label= new Label(Result_col, row, " ", bordersData2);
				writesheet.addCell(label);
			}
			
			Result_col = Result_col+1;
		}
	}
}
	
	/**
	 * @author xukun
	 * @param row
	 * @param surveyID
	 * @param arr_AssID
	 * @param TargetID
	 * @throws IOException
	 * @throws WriteException
	 * @throws SQLException
	 * @throws Exception
	 */
	public int printPrelimQn(int row, int surveyID, int [] arr_AssID, int TargetID) 
			throws IOException, WriteException, SQLException, Exception{
		AssignTarget_Rater ATR = new AssignTarget_Rater();
		Vector<Integer> qnIDs = ATR.getPrelimQn(surveyID);
		if(qnIDs.size()>0){
			label = new Label(0, row++, "PRELIMINARY QUESTIONS", No_Borders);
			writesheet.addCell(label);
			label = new Label(0, row, "Preliminary Question", cellBOLD_Border);
			writesheet.addCell(label);
			int totalCol = printRaterHeader(1,row++,surveyID, TargetID);
	
			for(int i = 0; i < qnIDs.size(); i++){
				label = new Label(0, row, ATR.getPrelimQnTitle(qnIDs.get(i)), bordersData1); 
				writesheet.addCell(label);
				writesheet.mergeCells(0, row, totalCol, row);
				row++;
				Vector<String> options = ATR.getPrelimQnOptions(qnIDs.get(i));
				for(int k = 0; k < options.size(); k++){
					label = new Label(0, row, options.get(k), bordersData1); 
					writesheet.addCell(label);
					//int startRow = ++row;
					int col = 1;
					for(int d = 0; d<arr_AssID.length; d++){
						String RaterCode = null;
						Vector v = ATR.getAssignmentDetail(arr_AssID[d]);
						for(int a=0; a<v.size(); a++)
						{
							votblAssignment vo = (votblAssignment)v.elementAt(a);
							RaterCode = vo.getRaterCode();
						}
						if(RaterCode != null){
							String ans = ATR.getPrelimQnAns(qnIDs.get(i), arr_AssID[d]);
							if((ans.replaceAll(" ", "")).equals(options.get(k).replaceAll(" ",""))){
								label = new Label(col, row, "1",bordersData1);
							}else{
								label = new Label(col, row, "",bordersData1);
							}
							writesheet.addCell(label);
							
							col++;
						}
					}
					row++;
				}
			}
			return row+4;
		}
		return row;
	}
	
	public int printAddQn(int row, int surveyID, int [] arr_AssID, int TargetID) 
			throws IOException, WriteException, SQLException, Exception
	{
		int qnColStart = 0;
		int qnColEnd = 4;
		int ansColStart = 5;
		int ansColEnd = 18;
		int charHeight = 570;
		AssignTarget_Rater ATR = new AssignTarget_Rater();
		Vector<Integer> qnIDs = ATR.getAddQn(surveyID);
		if(qnIDs.size()>0){
			label = new Label(0, row++, "ADDITIONAL QUESTIONS", No_Borders); 
			writesheet.addCell(label);
			for(int d = 0; d<arr_AssID.length; d++)
			{
				String RaterCode = null;
				Vector v = ATR.getAssignmentDetail(arr_AssID[d]);
				for(int i=0; i<v.size(); i++)
				{
					votblAssignment vo = (votblAssignment)v.elementAt(i);
					RaterCode = vo.getRaterCode();
				}
				if (RaterCode != null){
					row++;	
					label = new Label(0, row++, "Responses to Additional Question by " + RaterCode, No_Borders); 
					if (server.LangVer == 2)
						label = new Label(0, row++, "Responses to Additional Question by " + RaterCode, No_Borders); 
					writesheet.addCell(label);
					
					boolean isEmpty = false;
					OUTERLOOP: for(int i = 0; i<qnIDs.size(); i++){
						Vector<String> result = ATR.getAddQnAns(qnIDs.get(i), arr_AssID[d]);
						if(result.size() == 0 && i == qnIDs.size() -1){
							label = new Label(0, row, "No responses from "+ RaterCode +" to all the additional questions.", No_Borders_NoWrap);
							writesheet.addCell(label);	
							row+=2;
							isEmpty = true;
							break;
						}	
						for(int j = 0; j < result.size(); j++){
							if(result.get(j).length()>0){
								break OUTERLOOP;
							}							
							if(i == qnIDs.size()-1 && (j == result.size() - 1)){
								label = new Label(0, row, "No responses from "+ RaterCode +" to all the additional questions.", No_Borders_NoWrap);
								writesheet.addCell(label);	
								row+=2;
								isEmpty = true;
							}
						}	
					}
					if(!isEmpty){
						label = new Label(qnColStart, row, "Question", cellBOLD_Border); 
						if (server.LangVer == 2)
							label = new Label(qnColStart, row, "Kompetensi", cellBOLD_Border); 
						writesheet.addCell(label);
						writesheet.mergeCells(qnColStart, row, qnColEnd, row);
						label = new Label(ansColStart, row, "Responses to Additional Questions", cellBOLD_Border); 
						if (server.LangVer == 2)
							label = new Label(ansColStart, row, "Responses to Additional Questions", cellBOLD_Border); 
						writesheet.addCell(label);
						writesheet.mergeCells(ansColStart, row, ansColEnd, row);
						row++;
						for(int i = 0; i<qnIDs.size(); i++){
							String title = ATR.getAddQnTitle(qnIDs.get(i));
							Vector<String> result = ATR.getAddQnAns(qnIDs.get(i), arr_AssID[d]);
							label= new Label(qnColStart, row, title, bordersData2);
							writesheet.addCell(label);
							//writesheet.setRowView(row, 1000);
							writesheet.mergeCells(qnColStart, row, qnColEnd, row);
							String ans = "";
							for(int j = 0; j < result.size(); j++){
								ans += result.get(j);	
							}				
							label= new Label(ansColStart, row, ans, bordersData2);
							writesheet.addCell(label);
							//writesheet.setRowView(row, 1000);
							int maxHeight = Math.max((ans.length()/100) * charHeight, (title.length()/35) * charHeight);
							writesheet.setRowView(row, maxHeight);
							writesheet.mergeCells(ansColStart,row, ansColEnd, row);
			
							row++;
						}
					}
				}
				//row++;
			}
		}
		return row;
	}
	
	
	/*
	 * function printRaterResult_KB: print the result of each assignment. Use the arr_raterCode to store the rater of each assignment  
	 * Add by : Denise
	 * Add on : 10/12/2009
	 */	
/*	public void printRaterResult_Comp(int Result_col, int row, int [] arr_AssID, String[] arr_raterCode, int CompID, int RatingTaskID)
	throws IOException, WriteException, SQLException, Exception
{
		int d = 0;
		int selfPos = -1;
		
		while ( d< arr_AssID.length && arr_raterCode[d]!=null)
		{	
			if (arr_raterCode[d].equals("SELF")) //store position of the SELF assignment
				{
				  selfPos = d;
				  d++;
				  continue;
				}
			
			//output other rating result first
			AssignTarget_Rater ATR = new AssignTarget_Rater();
			
			double result = ATR.getCompResult(arr_AssID[d], CompID, RatingTaskID);
		
			if(result !=-1)	
			{
				double SurvResult = Math.round(result * 100.0) / 100.0;
				
				Number num = new Number(Result_col, row, SurvResult, bordersData1);
				writesheet.addCell(num);
			}
			else
			{
				label= new Label(Result_col, row, " ", bordersData2);
				writesheet.addCell(label);
			}
			
			Result_col = Result_col+1;
			d++;
		}
		
		if (selfPos !=-1) //if have SELF assignment, output self rating
		{
			d = selfPos;
			AssignTarget_Rater ATR = new AssignTarget_Rater();
			
			double result = ATR.getCompResult(arr_AssID[d], CompID, RatingTaskID);
		
			if(result !=-1)	
			{
				double SurvResult = Math.round(result * 100.0) / 100.0;
				
				Number num = new Number(Result_col, row, SurvResult, bordersData1);
				writesheet.addCell(num);
			}
			else
			{
				label= new Label(Result_col, row, " ", bordersData2);
				writesheet.addCell(label);
			}
			
			Result_col = Result_col+1;
		}
}
*/	

	
	public int printRaterComment(int r, int surveyID, int [] arr_AssID, int TargetID)
		throws IOException, WriteException, SQLException, Exception
	{
	//Added by Ha 13/06/08 to not print out the comment when survey does not have comment
		int compNameColStart = 0;
		int compNameColEnd = 1;
		int commentColStart = 2;
		int commentColEnd = 18;
		int charHeight = 580;
		Questionnaire Q = new Questionnaire();
		int selfIncluded = Q.SelfCommentIncluded(surveyID);
		int included = Q.commentIncluded(surveyID);
		
		int id = 0;
		String compName="";
		String RaterCode="";

		for(int d = 0; d<arr_AssID.length; d++)
		{
			if(arr_AssID[d] != 0)
			{
				
				AssignTarget_Rater ATR = new AssignTarget_Rater();
				Vector v = ATR.getAssignmentDetail(arr_AssID[d]);
				for(int i=0; i<v.size(); i++)
				{
					votblAssignment vo = (votblAssignment)v.elementAt(i);
					RaterCode = vo.getRaterCode();
					//Changed by Ha 13/06/08
					if (RaterCode != null)
					{
					//Ha 13/06/08: if selfComment or comment is included
						if (selfIncluded == 1|| !RaterCode.equals("SELF")&&included==1)
						{
							r++;		
							label = new Label(0, r++, "Narrative Comments by "+RaterCode, No_Borders); 
							if (server.LangVer == 2)
								label = new Label(0, r++, "Komentar Naratif oleh "+RaterCode, No_Borders); 
							writesheet.addCell(label);
							
							Vector compComment = S.CompListSurvey(surveyID);			
						
							id = 0;
							boolean isEmpty = false;
							// check for total empty case!!!
							UpperLoop: for(int l=0; l<compComment.size(); l++){
								voCompetency voComp = (voCompetency)compComment.elementAt(l);
								id = voComp.getPKCompetency();
								Vector vComment = S.getComment(arr_AssID[d], id);
								for(int m=0; m<vComment.size(); m++)
								{
									String sComment = (String)vComment.elementAt(m);
									if(sComment.length() > 0){
										break UpperLoop;
									}
									if(m == vComment.size() -1 && l== compComment.size() -1){
										label = new Label(0, r, "No narrative comments provided by "+ RaterCode +" for all competencies.", No_Borders_NoWrap);
										writesheet.addCell(label);	
										r+=2;
										isEmpty = true;
									}
								}
							}
							if(!isEmpty){
								
								label = new Label(0, r, "Competency", cellBOLD_Border); 
								if (server.LangVer == 2)
									label = new Label(0, r, "Kompetensi", cellBOLD_Border); 
								writesheet.addCell(label);
								writesheet.mergeCells(compNameColStart, r, compNameColEnd, r);
								label = new Label(commentColStart, r, "Narrative Comments", cellBOLD_Border); 
								if (server.LangVer == 2)
									label = new Label(commentColStart, r, "Komentar Naratif", cellBOLD_Border); 
								writesheet.addCell(label);
								writesheet.mergeCells(commentColStart, r, commentColEnd, r);
								r++;
								
								for(int l=0; l<compComment.size(); l++)
								{
									int start = 0;
									voCompetency voComp = (voCompetency)compComment.elementAt(l);
									id = voComp.getPKCompetency();
									
									compName = voComp.getCompetencyName();
									
									label = new Label(0, r, UnicodeHelper.getUnicodeStringAmp(compName), bordersData2); 
									writesheet.addCell(label);
									writesheet.mergeCells(compNameColStart, r, compNameColEnd, r);
									Vector vComment = S.getComment(arr_AssID[d], id);
									
									for(int m=0; m<vComment.size(); m++)
									{
										String sComment = (String)vComment.elementAt(m);
										if(start != 0) 
										{
											label = new Label(commentColStart, r, "", bordersData2); 
											writesheet.addCell(label);															
										}
										start++;				
										writesheet.mergeCells(commentColStart, r, commentColEnd, r);
										int maxHeight = Math.max((sComment.length()/90) * charHeight, (compName.length()/20) * charHeight);
										writesheet.setRowView(r, maxHeight);
										label = new Label(commentColStart, r, UnicodeHelper.getUnicodeStringAmp(sComment), bordersData2); 
										writesheet.addCell(label);
										r++;
									}
									if (start == 0) {
										writesheet.mergeCells(commentColStart, r, commentColEnd, r);
										writesheet.setRowView(r, 700);
										label = new Label(1, r++, "Nil", bordersData2); 
										writesheet.addCell(label);
									}
								}
							}
						}
					}
				}
			}			
		}
		return r;
	}
	
	public static void main (String[] args)throws SQLException, Exception
	{
		Report_RaterInputForTarget_SPF Rpt = new Report_RaterInputForTarget_SPF();

		// competency level = 340
		// key behaviour level = 343
		Rpt.AllRaters(464,13941,124,"", true);
	}
}	