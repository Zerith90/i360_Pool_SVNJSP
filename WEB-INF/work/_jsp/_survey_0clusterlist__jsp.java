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
import CP_Classes.vo.voCluster;

public class _survey_0clusterlist__jsp extends com.caucho.jsp.JavaPage
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
    CP_Classes.Login logchk;
    synchronized (pageContext.getSession()) {
      logchk = (CP_Classes.Login) pageContext.getSession().getAttribute("logchk");
      if (logchk == null) {
        logchk = new CP_Classes.Login();
        pageContext.getSession().setAttribute("logchk", logchk);
      }
    }
    out.write(_jsp_string1, 0, _jsp_string1.length);
    CP_Classes.Create_Edit_Survey CE_Survey;
    synchronized (pageContext.getSession()) {
      CE_Survey = (CP_Classes.Create_Edit_Survey) pageContext.getSession().getAttribute("CE_Survey");
      if (CE_Survey == null) {
        CE_Survey = new CP_Classes.Create_Edit_Survey();
        pageContext.getSession().setAttribute("CE_Survey", CE_Survey);
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
    out.write(_jsp_string3, 0, _jsp_string3.length);
    // by lydia Date 05/09/2008 Fix jsp file to support Thai language 
    out.write(_jsp_string4, 0, _jsp_string4.length);
    out.print((trans.tslt("No record selected")));
    out.write(_jsp_string5, 0, _jsp_string5.length);
    out.print((trans.tslt("No record available")));
    out.write(_jsp_string6, 0, _jsp_string6.length);
    out.print((trans.tslt("Assign Cluster")));
    out.write(_jsp_string7, 0, _jsp_string7.length);
    	
response.setHeader("Pragma", "no-cache");
response.setHeader("Cache-Control", "no-cache");
response.setDateHeader("expires", 0);

String username=(String)session.getAttribute("username");

if (!logchk.isUsable(username)){

    out.write(_jsp_string8, 0, _jsp_string8.length);
      
} else{
	
	/************************************************** ADDING TOGGLE FOR SORTING PURPOSE *************************************************/
	int toggle = CE_Survey.getToggle();	//0=asc, 1=desc
	int type = 1; //1=name, 2=origin		
			
	if(request.getParameter("name") != null){	 
		if(toggle == 0)
			toggle = 1;
		else
			toggle = 0;
		
		CE_Survey.setToggle(toggle);
		
		type = Integer.parseInt(request.getParameter("name"));			 
		CE_Survey.setSortType(type);									
	} 
/*********************************************************END ADDING TOGGLE FOR SORTING PURPOSE *************************************/

	if(request.getParameter("add") != null){
		int SurveyID = CE_Survey.getSurvey_ID();
	 	String [] chkSelect = request.getParameterValues("chkComp");
	 	boolean bClusterAdded = true;
	 	    
		if(chkSelect != null){ 
		    try{
				for(int i=0; i<chkSelect.length; i++){
					int iClusterID = 0;
					if(chkSelect[i] !=null){
						iClusterID = Integer.parseInt(chkSelect[i]);
						if(!CE_Survey.addCluster(iClusterID,SurveyID))
							bClusterAdded = false; 
					}
				}
			}
			catch(SQLException sqle){	
				bClusterAdded = false;
			}	
				
			if (bClusterAdded) {

    out.write(_jsp_string9, 0, _jsp_string9.length);
    
			} else {

    out.write(_jsp_string10, 0, _jsp_string10.length);
    
			}
		}

    out.write(_jsp_string11, 0, _jsp_string11.length);
    		
	}//end if request getParameter add


	int DisplayNo;
	int pkCluster; 
	String name, definition,origin;
	DisplayNo = 1;

	/************************************************** ADDING TOGGLE FOR SORTING PURPOSE *************************************************/

	

    out.write(_jsp_string12, 0, _jsp_string12.length);
    out.print((trans.tslt("Name")));
    out.write(_jsp_string13, 0, _jsp_string13.length);
     	
	Vector vCluster = CE_Survey.FilterRecordCluster(CE_Survey.get_survOrg(), CE_Survey.getSurvey_ID());
	for(int i=0; i<vCluster.size(); i++){
		voCluster vo = (voCluster)vCluster.elementAt(i);
		pkCluster = vo.getClusterID();
		
		name =  vo.getClusterName();

    out.write(_jsp_string14, 0, _jsp_string14.length);
    out.print((pkCluster));
    out.write(_jsp_string15, 0, _jsp_string15.length);
     out.print(name);
    out.write(_jsp_string16, 0, _jsp_string16.length);
     		DisplayNo++;
	}//end for loop
}

    out.write(_jsp_string17, 0, _jsp_string17.length);
    out.print((trans.tslt("Add")));
    out.write(_jsp_string18, 0, _jsp_string18.length);
    out.print((trans.tslt("Close")));
    out.write(_jsp_string19, 0, _jsp_string19.length);
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
    depend = new com.caucho.vfs.Depend(appDir.lookup("Survey_ClusterList.jsp"), -3135726641278167769L, false);
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

  private final static char []_jsp_string3;
  private final static char []_jsp_string11;
  private final static char []_jsp_string17;
  private final static char []_jsp_string8;
  private final static char []_jsp_string19;
  private final static char []_jsp_string18;
  private final static char []_jsp_string12;
  private final static char []_jsp_string4;
  private final static char []_jsp_string10;
  private final static char []_jsp_string7;
  private final static char []_jsp_string0;
  private final static char []_jsp_string6;
  private final static char []_jsp_string16;
  private final static char []_jsp_string5;
  private final static char []_jsp_string9;
  private final static char []_jsp_string2;
  private final static char []_jsp_string13;
  private final static char []_jsp_string14;
  private final static char []_jsp_string1;
  private final static char []_jsp_string15;
  static {
    _jsp_string3 = "\r\n\r\n\r\n<html>\r\n<head>\r\n<meta http-equiv=\"Content-Type\" content=\"text/html\">\r\n\r\n".toCharArray();
    _jsp_string11 = "					\r\n	<script>\r\n		window.close();\r\n	 	opener.location.href = 'SurveyCluster.jsp';\r\n	</script>\r\n".toCharArray();
    _jsp_string17 = "\r\n</table>\r\n</div>\r\n</td></tr>\r\n</table>\r\n<p></p>\r\n<table border=\"0\" width=\"55%\" cellspacing=\"0\" cellpadding=\"0\">\r\n	<tr>\r\n		<td width=\"210\"><input type=\"button\" name=\"Add\" value=\"".toCharArray();
    _jsp_string8 = " \r\n	<font size=\"2\">\r\n	<script>\r\n	parent.location.href = \"index.jsp\";\r\n	</script>\r\n".toCharArray();
    _jsp_string19 = "\" name=\"btnClose\" onclick=\"closeWindow()\"></td>\r\n	</tr>\r\n</table>\r\n&nbsp;&nbsp;&nbsp;\r\n\r\n</form>\r\n</body>\r\n</html>".toCharArray();
    _jsp_string18 = "\" onclick=\"ConfirmAdd(this.form,this.form.chkComp)\"></td>\r\n		<td><input type=\"button\" value=\"".toCharArray();
    _jsp_string12 = "\r\n<form name=\"ClusterList\" method=\"post\" action =\"Survey_ClusterList.jsp\">\r\n\r\n<table border=\"1\" width=\"610\">\r\n<tr><td>\r\n<div style='width:610px; height:500px; z-index:1; overflow:auto'>  \r\n<table border=\"1\" bordercolor=\"#3399FF\" bgcolor=\"#FFFFCC\">\r\n<th width=\"20\" bgcolor=\"navy\">\r\n	<font size=\"2\">\r\n	  <input type=\"checkbox\" name=\"checkAll\" onClick=\"checkedAll(this.form, this.form.chkComp,this.form.checkAll)\"></font>\r\n</th>\r\n\r\n\r\n<th width=\"81\" bgcolor=\"navy\" bordercolor=\"#3399FF\"><a href=\"Survey_ClusterList.jsp?name=1\"><b>\r\n<font style='color:white' face=\"Arial\" size=\"2\"><u>".toCharArray();
    _jsp_string4 = "\r\n<title>Add Competency to Survey</title>\r\n</head>\r\n\r\n\r\n<SCRIPT LANGUAGE=\"JavaScript\">\r\n<!-- Begin\r\n\r\n<!-- added by Albert (16/07/2012): add a checkbox on top to choose all -->\r\nfunction checkedAll(form, field, checkAll)\r\n{	\r\n	if(checkAll.checked == true) \r\n		for(var i=0; i<field.length; i++)\r\n			field[i].checked = true;\r\n	else \r\n		for(var i=0; i<field.length; i++)\r\n			field[i].checked = false;	\r\n}\r\n\r\nfunction check(field){\r\n	var isValid = 0;\r\n	var clickedValue = 0;\r\n	//check whether any checkbox selected\r\n	if( field == null ) {\r\n		isValid = 2;\r\n	} else {\r\n		if(isNaN(field.length) == false) {\r\n			for (i = 0; i < field.length; i++)\r\n				if(field[i].checked) {\r\n					clickedValue = field[i].value;\r\n					isValid = 1;\r\n				}\r\n		}else {		\r\n			if(field.checked) {\r\n				clickedValue = field.value;\r\n				isValid = 1;\r\n			}\r\n				\r\n		}\r\n	}\r\n	\r\n	if(isValid == 1)\r\n		return clickedValue;\r\n	else if(isValid == 0)\r\n		alert(\"".toCharArray();
    _jsp_string10 = "\r\n		 		<script>\r\n		 			alert(\"Added unsuccessfully\");\r\n		 		</script>\r\n".toCharArray();
    _jsp_string7 = "?\")){\r\n			form.action=\"Survey_ClusterList.jsp?add=1\";\r\n			form.method=\"post\";\r\n			form.submit();\r\n		}\r\n		\r\n	}\r\n} \r\n\r\nfunction closeWindow(){\r\n	window.close();\r\n}\r\n</script>\r\n\r\n\r\n<BODY>\r\n".toCharArray();
    _jsp_string0 = "\r\n\r\n\r\n\r\n".toCharArray();
    _jsp_string6 = "\");\r\n	\r\n	isValid = 0;\r\n\r\n}\r\n \r\nfunction ConfirmAdd(form, field){\r\n	if(check(field)){\r\n		if(confirm(\"".toCharArray();
    _jsp_string16 = "</font><font size=\"2\">\r\n			</font>\r\n       </td>\r\n   </tr>\r\n".toCharArray();
    _jsp_string5 = "\");\r\n	else if(isValid == 2)\r\n		alert(\"".toCharArray();
    _jsp_string9 = "\r\n				<script>\r\n					alert(\"Added successfully, survey status has been changed to Non Commissioned, to re-open survey please go to the Survey Detail page\");\r\n				</script>\r\n".toCharArray();
    _jsp_string2 = "\r\n".toCharArray();
    _jsp_string13 = "</u></font></b></a></th>\r\n\r\n".toCharArray();
    _jsp_string14 = "\r\n   <tr onMouseOver = \"this.bgColor = '#99ccff'\" onMouseOut = \"this.bgColor = '#FFFFcc'\">\r\n       <td>\r\n	   		<font style='font-size:11.0pt;font-family:Arial'>\r\n	   		<input type=\"checkbox\" name=\"chkComp\" value=".toCharArray();
    _jsp_string1 = "   \r\n".toCharArray();
    _jsp_string15 = "></font><font style='font-family:Arial' size=\"2\">\r\n            </font>\r\n	   </td>\r\n	   <td>\r\n           <font style='font-family:Arial' size=\"2\">".toCharArray();
  }
}