/*
 * JSP generated by Resin Professional 4.0.36 (built Fri, 26 Apr 2013 03:33:09 PDT)
 */

package _jsp;
import javax.servlet.*;
import javax.servlet.jsp.*;
import javax.servlet.http.*;
import java.sql.*;
import java.io.*;
import java.util.*;
import java.util.Date;
import java.text.*;
import java.lang.String;
import CP_Classes.PrelimQuestion;

public class _raterstodolist__jsp extends com.caucho.jsp.JavaPage
{
  private static final java.util.HashMap<String,java.lang.reflect.Method> _jsp_functionMap = new java.util.HashMap<String,java.lang.reflect.Method>();
  private boolean _caucho_isDead;
  private boolean _caucho_isNotModified;
  private com.caucho.jsp.PageManager _jsp_pageManager;
  
  public void
  _jspService(javax.servlet.http.HttpServletRequest request,
              javax.servlet.http.HttpServletResponse response)
    throws java.io.IOException, javax.servlet.ServletException
  {
    javax.servlet.http.HttpSession session = request.getSession(true);
    com.caucho.server.webapp.WebApp _jsp_application = _caucho_getApplication();
    com.caucho.jsp.PageContextImpl pageContext = _jsp_pageManager.allocatePageContext(this, _jsp_application, request, response, null, session, 8192, true, false);

    TagState _jsp_state = null;

    try {
      _jspService(request, response, pageContext, _jsp_application, session, _jsp_state);
    } catch (java.lang.Throwable _jsp_e) {
      pageContext.handlePageException(_jsp_e);
    } finally {
      _jsp_pageManager.freePageContext(pageContext);
    }
  }
  
  private void
  _jspService(javax.servlet.http.HttpServletRequest request,
              javax.servlet.http.HttpServletResponse response,
              com.caucho.jsp.PageContextImpl pageContext,
              javax.servlet.ServletContext application,
              javax.servlet.http.HttpSession session,
              TagState _jsp_state)
    throws Throwable
  {
    javax.servlet.jsp.JspWriter out = pageContext.getOut();
    final javax.el.ELContext _jsp_env = pageContext.getELContext();
    javax.servlet.ServletConfig config = getServletConfig();
    javax.servlet.Servlet page = this;
    javax.servlet.jsp.tagext.JspTag _jsp_parent_tag = null;
    com.caucho.jsp.PageContextImpl _jsp_parentContext = pageContext;
    response.setContentType("text/html");
    response.setCharacterEncoding("utf-8");

    out.write(_jsp_string0, 0, _jsp_string0.length);
    // by lydia Date 05/09/2008 Fix jsp file to support Thai language 
    out.write(_jsp_string1, 0, _jsp_string1.length);
    CP_Classes.RatersToDoList RTDL;
    synchronized (pageContext.getSession()) {
      RTDL = (CP_Classes.RatersToDoList) pageContext.getSession().getAttribute("RTDL");
      if (RTDL == null) {
        RTDL = new CP_Classes.RatersToDoList();
        pageContext.getSession().setAttribute("RTDL", RTDL);
      }
    }
    out.write(_jsp_string2, 0, _jsp_string2.length);
    CP_Classes.RatersDataEntry RDE;
    synchronized (pageContext.getSession()) {
      RDE = (CP_Classes.RatersDataEntry) pageContext.getSession().getAttribute("RDE");
      if (RDE == null) {
        RDE = new CP_Classes.RatersDataEntry();
        pageContext.getSession().setAttribute("RDE", RDE);
      }
    }
    out.write(_jsp_string2, 0, _jsp_string2.length);
    CP_Classes.DemographicEntry DemographicEntry;
    synchronized (pageContext.getSession()) {
      DemographicEntry = (CP_Classes.DemographicEntry) pageContext.getSession().getAttribute("DemographicEntry");
      if (DemographicEntry == null) {
        DemographicEntry = new CP_Classes.DemographicEntry();
        pageContext.getSession().setAttribute("DemographicEntry", DemographicEntry);
      }
    }
    out.write(_jsp_string2, 0, _jsp_string2.length);
    CP_Classes.Questionnaire Questionnaire;
    synchronized (pageContext.getSession()) {
      Questionnaire = (CP_Classes.Questionnaire) pageContext.getSession().getAttribute("Questionnaire");
      if (Questionnaire == null) {
        Questionnaire = new CP_Classes.Questionnaire();
        pageContext.getSession().setAttribute("Questionnaire", Questionnaire);
      }
    }
    out.write(_jsp_string2, 0, _jsp_string2.length);
    CP_Classes.Create_Edit_Survey CE_Survey;
    synchronized (pageContext.getSession()) {
      CE_Survey = (CP_Classes.Create_Edit_Survey) pageContext.getSession().getAttribute("CE_Survey");
      if (CE_Survey == null) {
        CE_Survey = new CP_Classes.Create_Edit_Survey();
        pageContext.getSession().setAttribute("CE_Survey", CE_Survey);
      }
    }
    out.write(_jsp_string2, 0, _jsp_string2.length);
    CP_Classes.Login logchk;
    synchronized (pageContext.getSession()) {
      logchk = (CP_Classes.Login) pageContext.getSession().getAttribute("logchk");
      if (logchk == null) {
        logchk = new CP_Classes.Login();
        pageContext.getSession().setAttribute("logchk", logchk);
      }
    }
    out.write(_jsp_string2, 0, _jsp_string2.length);
    CP_Classes.Translate trans;
    synchronized (pageContext.getSession()) {
      trans = (CP_Classes.Translate) pageContext.getSession().getAttribute("trans");
      if (trans == null) {
        trans = new CP_Classes.Translate();
        pageContext.getSession().setAttribute("trans", trans);
      }
    }
    out.write(_jsp_string2, 0, _jsp_string2.length);
    CP_Classes.PrelimQuestionController PrelimQController;
    synchronized (pageContext.getSession()) {
      PrelimQController = (CP_Classes.PrelimQuestionController) pageContext.getSession().getAttribute("PrelimQController");
      if (PrelimQController == null) {
        PrelimQController = new CP_Classes.PrelimQuestionController();
        pageContext.getSession().setAttribute("PrelimQController", PrelimQController);
      }
    }
    out.write(_jsp_string3, 0, _jsp_string3.length);
    out.print((trans.tslt("No record selected")));
    out.write(_jsp_string4, 0, _jsp_string4.length);
    
/************************************************** ADDING TOGGLE FOR SORTING PURPOSE *************************************************/
	//response.setHeader("Pragma", "no-cache");
	//response.setHeader("Cache-Control", "no-cache");
	//response.setDateHeader("expires", 0);

String username=(String)session.getAttribute("username");

  if (!logchk.isUsable(username)) 
  {
    out.write(_jsp_string5, 0, _jsp_string5.length);
      } 
  else 
  { 	
	int toggle = RTDL.getToggle();	//0=asc, 1=desc
	int type = 7; //1=name, 2=origin		
			
	if(request.getParameter("name") != null)
	{	 
		if(toggle == 0)
			toggle = 1;
		else
			toggle = 0;
		
		RTDL.setToggle(toggle);
		
		type = Integer.parseInt(request.getParameter("name"));			 
		RTDL.setSortType(type);
	} 
	
/************************************************** ADDING TOGGLE FOR SORTING PURPOSE *************************************************/	
	
	
	int raterID = logchk.getPKUser();
	
	if(request.getParameter("open") != null) {
		int asgtID = Integer.parseInt(request.getParameter("open"));		
		int info [] = RTDL.assignmentInfo(asgtID);
		
		RDE.setSurveyID(info[0]);
		RDE.setTargetID(info[1]);
		RDE.setRaterID(info[2]);
		Questionnaire.setAssignmentID(asgtID);
		Vector<PrelimQuestion> v = PrelimQController.getQuestions(RDE.getSurveyID());
		// If any demographic is chosen, forward to demo page. Otherwise, proceed into Questionnaire
		if(DemographicEntry.bDemographicSelected(info[0])){

    out.write(_jsp_string6, 0, _jsp_string6.length);
    		}
		else if(v.size() > 0){

    out.write(_jsp_string7, 0, _jsp_string7.length);
    		}
		else{

    out.write(_jsp_string8, 0, _jsp_string8.length);
    		}
	}
	//System.out.println("RaterID = " + raterID);
	Vector vList = RTDL.getToDoList(raterID);

    out.write(_jsp_string9, 0, _jsp_string9.length);
    out.print((trans.tslt("Rater's To Do List")));
    out.write(_jsp_string10, 0, _jsp_string10.length);
    out.print((trans.tslt("To Open a survey, click on the Survey Name")));
    out.write(_jsp_string11, 0, _jsp_string11.length);
    out.print((trans.tslt("Survey's Assignment")));
    out.write(_jsp_string12, 0, _jsp_string12.length);
    out.print((trans.tslt("Survey Name")));
    out.write(_jsp_string13, 0, _jsp_string13.length);
    out.print((trans.tslt("Target Name")));
    out.write(_jsp_string14, 0, _jsp_string14.length);
    out.print((trans.tslt("Deadline")));
    out.write(_jsp_string15, 0, _jsp_string15.length);
    //Changed by Alvis on 06-Aug-09: Header changed from "Relation" to "Your Relation to the Target"
    out.write(_jsp_string16, 0, _jsp_string16.length);
    out.print((trans.tslt("Your Relation to the Target")));
    out.write(_jsp_string17, 0, _jsp_string17.length);
    out.print((trans.tslt("Survey Status")));
    out.write(_jsp_string18, 0, _jsp_string18.length);
    out.print((trans.tslt("Assignment Status")));
    out.write(_jsp_string19, 0, _jsp_string19.length);
    
	for(int i=0; i<vList.size(); i++) {		
		String [] sToDoList = new String[7];
		sToDoList = (String[])vList.elementAt(i);
		
		int asgtID = Integer.parseInt(sToDoList[0]);	
		String surveyName = sToDoList[1];
		String name = sToDoList[2];
		String deadline = sToDoList[3];
		String RT = sToDoList[4];
		int surveyStatus = Integer.parseInt(sToDoList[5]);

    out.write(_jsp_string20, 0, _jsp_string20.length);
    out.print((i+1));
    out.write(_jsp_string21, 0, _jsp_string21.length);
    
	if(surveyStatus != 1) {

    out.write(_jsp_string22, 0, _jsp_string22.length);
    out.print((surveyName));
    out.write(_jsp_string23, 0, _jsp_string23.length);
    
	}else {

    out.write(_jsp_string24, 0, _jsp_string24.length);
    out.print((asgtID));
    out.write(_jsp_string25, 0, _jsp_string25.length);
    out.print((surveyName));
    out.write(_jsp_string23, 0, _jsp_string23.length);
     } 
    out.write(_jsp_string26, 0, _jsp_string26.length);
    out.print((name));
    out.write(_jsp_string27, 0, _jsp_string27.length);
    out.print((deadline));
    out.write(_jsp_string27, 0, _jsp_string27.length);
    out.print((RT));
    out.write(_jsp_string21, 0, _jsp_string21.length);
    
	
	String status = "";
	switch(surveyStatus) {
		case 0 : status = "N/A";
				 break;		
		case 1 : status = "Open";
				 break;
		case 2 : status = "Closed";
				 break;
		case 3 : status = "Not Commissioned / NC";
				 break;
	}

    out.write(_jsp_string28, 0, _jsp_string28.length);
    out.print((status));
    out.write(_jsp_string29, 0, _jsp_string29.length);
    
	int raterStatus = Integer.parseInt(sToDoList[6]);
	String rStatus = "";
	switch(raterStatus) {
		case 0 : rStatus = "Incomplete";
				 break;		
	}

    out.write(_jsp_string30, 0, _jsp_string30.length);
    out.print((rStatus));
    out.write(_jsp_string31, 0, _jsp_string31.length);
     } 
    out.write(_jsp_string32, 0, _jsp_string32.length);
     } 
    out.write(_jsp_string33, 0, _jsp_string33.length);
  }

  private com.caucho.make.DependencyContainer _caucho_depends
    = new com.caucho.make.DependencyContainer();

  public java.util.ArrayList<com.caucho.vfs.Dependency> _caucho_getDependList()
  {
    return _caucho_depends.getDependencies();
  }

  public void _caucho_addDepend(com.caucho.vfs.PersistentDependency depend)
  {
    super._caucho_addDepend(depend);
    _caucho_depends.add(depend);
  }

  protected void _caucho_setNeverModified(boolean isNotModified)
  {
    _caucho_isNotModified = true;
  }

  public boolean _caucho_isModified()
  {
    if (_caucho_isDead)
      return true;

    if (_caucho_isNotModified)
      return false;

    if (com.caucho.server.util.CauchoSystem.getVersionId() != -7791540776389363938L)
      return true;

    return _caucho_depends.isModified();
  }

  public long _caucho_lastModified()
  {
    return 0;
  }

  public void destroy()
  {
      _caucho_isDead = true;
      super.destroy();
    TagState tagState;
  }

  public void init(com.caucho.vfs.Path appDir)
    throws javax.servlet.ServletException
  {
    com.caucho.vfs.Path resinHome = com.caucho.server.util.CauchoSystem.getResinHome();
    com.caucho.vfs.MergePath mergePath = new com.caucho.vfs.MergePath();
    mergePath.addMergePath(appDir);
    mergePath.addMergePath(resinHome);
    com.caucho.loader.DynamicClassLoader loader;
    loader = (com.caucho.loader.DynamicClassLoader) getClass().getClassLoader();
    String resourcePath = loader.getResourcePathSpecificFirst();
    mergePath.addClassPath(resourcePath);
    com.caucho.vfs.Depend depend;
    depend = new com.caucho.vfs.Depend(appDir.lookup("RatersToDoList.jsp"), -8732854295780486678L, false);
    _caucho_depends.add(depend);
  }

  final static class TagState {

    void release()
    {
    }
  }

  public java.util.HashMap<String,java.lang.reflect.Method> _caucho_getFunctionMap()
  {
    return _jsp_functionMap;
  }

  public void caucho_init(ServletConfig config)
  {
    try {
      com.caucho.server.webapp.WebApp webApp
        = (com.caucho.server.webapp.WebApp) config.getServletContext();
      init(config);
      if (com.caucho.jsp.JspManager.getCheckInterval() >= 0)
        _caucho_depends.setCheckInterval(com.caucho.jsp.JspManager.getCheckInterval());
      _jsp_pageManager = webApp.getJspApplicationContext().getPageManager();
      com.caucho.jsp.TaglibManager manager = webApp.getJspApplicationContext().getTaglibManager();
      com.caucho.jsp.PageContextImpl pageContext = new com.caucho.jsp.InitPageContextImpl(webApp, this);
    } catch (Exception e) {
      throw com.caucho.config.ConfigException.create(e);
    }
  }

  private final static char []_jsp_string18;
  private final static char []_jsp_string28;
  private final static char []_jsp_string22;
  private final static char []_jsp_string29;
  private final static char []_jsp_string4;
  private final static char []_jsp_string14;
  private final static char []_jsp_string7;
  private final static char []_jsp_string24;
  private final static char []_jsp_string21;
  private final static char []_jsp_string25;
  private final static char []_jsp_string9;
  private final static char []_jsp_string0;
  private final static char []_jsp_string20;
  private final static char []_jsp_string10;
  private final static char []_jsp_string26;
  private final static char []_jsp_string11;
  private final static char []_jsp_string27;
  private final static char []_jsp_string1;
  private final static char []_jsp_string33;
  private final static char []_jsp_string31;
  private final static char []_jsp_string15;
  private final static char []_jsp_string32;
  private final static char []_jsp_string17;
  private final static char []_jsp_string16;
  private final static char []_jsp_string13;
  private final static char []_jsp_string6;
  private final static char []_jsp_string2;
  private final static char []_jsp_string23;
  private final static char []_jsp_string19;
  private final static char []_jsp_string5;
  private final static char []_jsp_string12;
  private final static char []_jsp_string30;
  private final static char []_jsp_string8;
  private final static char []_jsp_string3;
  static {
    _jsp_string18 = "</u></font></b></a></th>\r\n<th width=\"100\" align=\"center\" bgcolor=\"navy\"><a href=\"RatersToDoList.jsp?name=6\"><b><font style='font-size:10.0pt;font-family:Arial;color:white'><u>".toCharArray();
    _jsp_string28 = "	\r\n	<td align=\"center\">".toCharArray();
    _jsp_string22 = "\r\n	<td><a style=\"color:black;\">".toCharArray();
    _jsp_string29 = "</td>\r\n	\r\n".toCharArray();
    _jsp_string4 = "!\");\r\n		\r\n	isValid = 0;	\r\n	\r\n}\r\n\r\nfunction confirmOpen(form, field)\r\n{\r\n	var value = check(field);\r\n	\r\n	if(value)\r\n	{		\r\n		form.action=\"RatersToDoList.jsp?open=\" + value;\r\n		form.method=\"post\";\r\n		form.submit();\r\n	}\r\n	\r\n} \r\n\r\n</script>\r\n\r\n\r\n<body>\r\n".toCharArray();
    _jsp_string14 = "</u></font></b></a></th>\r\n<th width=\"100\" align=\"center\" bgcolor=\"navy\"><a href=\"RatersToDoList.jsp?name=3\"><b><font style='font-size:10.0pt;font-family:Arial;color:white'><u>".toCharArray();
    _jsp_string7 = "			<script>\r\n			window.location.href = \"PrelimQAnswers.jsp?entry=1\";\r\n			</script>\r\n".toCharArray();
    _jsp_string24 = "	\r\n    <td><a style=\"color:blue;\" href=\"RatersToDoList.jsp?open=".toCharArray();
    _jsp_string21 = "</td>\r\n".toCharArray();
    _jsp_string25 = "\">".toCharArray();
    _jsp_string9 = "\r\n<form action=\"RatersToDoList.jsp\" method=\"post\" name=\"RatersToDoList\">\r\n<table border=\"0\" width=\"592\" cellspacing=\"0\" cellpadding=\"0\" font span style='font-size:10.0pt;font-family:Arial;'>\r\n	<tr>\r\n	  <td colspan=\"3\"><b><font color=\"#000080\" size=\"2\" face=\"Arial\">".toCharArray();
    _jsp_string0 = "  \r\n				 \r\n<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">\r\n<html>\r\n<head>\r\n<title>Rater's To Do List</title>\r\n\r\n<meta http-equiv=\"Content-Type\" content=\"text/html\">\r\n\r\n".toCharArray();
    _jsp_string20 = "\r\n<tr>\r\n\r\n	<td align=\"center\">".toCharArray();
    _jsp_string10 = " </font></b></td>\r\n	  <td>&nbsp;</td>\r\n    </tr>	\r\n	<tr>\r\n	  <td colspan=\"4\">&nbsp;</td>\r\n    </tr>\r\n	<tr>\r\n	  <td colspan=\"4\"><ul>\r\n          <li><font face=\"Arial\" size=\"2\">".toCharArray();
    _jsp_string26 = "\r\n    <td>".toCharArray();
    _jsp_string11 = ". </font></li>\r\n          </ul></td>\r\n    </tr>\r\n  </table>\r\n<div style='width:610px; height:50%; z-index:1; overflow:auto;'> \r\n<table border=\"1\" bordercolorlight = \"#3399FF\" bgcolor=\"#FFFFCC\" width=\"593\" font style='font-size:10.0pt;font-family:Arial'>\r\n<tr>\r\n<td colspan=\"8\" align=\"center\" bgcolor=\"navy\" font style='font-size:10.0pt;font-family:Arial;color:white'><b>".toCharArray();
    _jsp_string27 = "</td>\r\n    <td align=\"center\">".toCharArray();
    _jsp_string1 = "\r\n\r\n</head>\r\n\r\n".toCharArray();
    _jsp_string33 = "\r\n</body>\r\n</html>\r\n\r\n".toCharArray();
    _jsp_string31 = "</td>\r\n</tr>\r\n\r\n".toCharArray();
    _jsp_string15 = "</u></font></b></a></th>\r\n".toCharArray();
    _jsp_string32 = "\r\n</table>\r\n</div>\r\n<p></p>\r\n</form>\r\n".toCharArray();
    _jsp_string17 = "</u></font></b></a></th>\r\n<th width=\"100\" align=\"center\" bgcolor=\"navy\"><a href=\"RatersToDoList.jsp?name=5\"><b><font style='font-size:10.0pt;font-family:Arial;color:white'><u>".toCharArray();
    _jsp_string16 = "\r\n<th width=\"100\" align=\"center\" bgcolor=\"navy\"><a href=\"RatersToDoList.jsp?name=4\"><b><font style='font-size:10.0pt;font-family:Arial;color:white'><u>".toCharArray();
    _jsp_string13 = "</u></font></b></a></th>\r\n<th width=\"100\" align=\"center\" bgcolor=\"navy\"><a href=\"RatersToDoList.jsp?name=2\"><b><font style='font-size:10.0pt;font-family:Arial;color:white'><u>".toCharArray();
    _jsp_string6 = "			<script>\r\n				window.location.href = \"DemographicEntry.jsp\";\r\n			</script>\r\n".toCharArray();
    _jsp_string2 = "\r\n".toCharArray();
    _jsp_string23 = "</a></td>\r\n".toCharArray();
    _jsp_string19 = "</u></font></b></a></th>\r\n\r\n".toCharArray();
    _jsp_string5 = " <font size=\"2\">\r\n   \r\n    	    	<script>\r\n	parent.location.href = \"index.jsp\";\r\n</script>\r\n".toCharArray();
    _jsp_string12 = "</b></td>\r\n</tr>\r\n<th width=\"20\" align=\"center\" bgcolor=\"navy\"><b><font style='font-size:10.0pt;font-family:Arial;color:white'>No</font></b></th>\r\n<th width=\"300\" align=\"center\" bgcolor=\"navy\"><a href=\"RatersToDoList.jsp?name=1\"><b><font style='font-size:10.0pt;font-family:Arial;color:white'><u>".toCharArray();
    _jsp_string30 = "		\r\n	<td align=\"center\">".toCharArray();
    _jsp_string8 = "			<script>\r\n				window.location.href = \"Questionnaire.jsp\";\r\n			</script>\r\n".toCharArray();
    _jsp_string3 = "\r\n\r\n\r\n\r\n<script language=\"javascript\">\r\nvar x = parseInt(window.screen.width) / 2 - 200;  // the number 250 is the exact half of the width of the pop-up and so should be changed according to the size of the pop-up\r\nvar y = parseInt(window.screen.height) / 2 - 200;\r\n\r\nfunction check(field)\r\n{\r\n	var isValid = 0;\r\n	var clickedValue = 0;\r\n	//check whether any checkbox selected\r\n	\r\n	for (i = 0; i < field.length; i++) \r\n		if(field[i].checked) {		\r\n			clickedValue = field[i].value;\r\n			field[i].checked = false;\r\n			isValid = 1;\r\n		}\r\n		\r\n	if(isValid == 0 && field != null)  {\r\n		if(field.checked) {\r\n			clickedValue = field.value;\r\n			isValid = 1;\r\n		}\r\n	}\r\n	\r\n	if (isValid == 1)\r\n		return clickedValue;\r\n	else\r\n		alert(\"".toCharArray();
  }
}