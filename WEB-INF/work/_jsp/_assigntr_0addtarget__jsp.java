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
import java.text.*;
import java.lang.String;
import CP_Classes.vo.voGroup;
import CP_Classes.vo.voDepartment;
import CP_Classes.vo.voDivision;
import CP_Classes.vo.votblSurvey;
import CP_Classes.vo.voUser;

public class _assigntr_0addtarget__jsp extends com.caucho.jsp.JavaPage
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
    CP_Classes.AssignTarget_Rater assignTR;
    synchronized (pageContext.getSession()) {
      assignTR = (CP_Classes.AssignTarget_Rater) pageContext.getSession().getAttribute("assignTR");
      if (assignTR == null) {
        assignTR = new CP_Classes.AssignTarget_Rater();
        pageContext.getSession().setAttribute("assignTR", assignTR);
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
    out.write(_jsp_string1, 0, _jsp_string1.length);
    CP_Classes.GlobalFunc GFunc;
    synchronized (pageContext.getSession()) {
      GFunc = (CP_Classes.GlobalFunc) pageContext.getSession().getAttribute("GFunc");
      if (GFunc == null) {
        GFunc = new CP_Classes.GlobalFunc();
        pageContext.getSession().setAttribute("GFunc", GFunc);
      }
    }
    out.write(_jsp_string1, 0, _jsp_string1.length);
    CP_Classes.User user;
    synchronized (pageContext.getSession()) {
      user = (CP_Classes.User) pageContext.getSession().getAttribute("user");
      if (user == null) {
        user = new CP_Classes.User();
        pageContext.getSession().setAttribute("user", user);
      }
    }
    out.write(_jsp_string1, 0, _jsp_string1.length);
    CP_Classes.Login logchk;
    synchronized (pageContext.getSession()) {
      logchk = (CP_Classes.Login) pageContext.getSession().getAttribute("logchk");
      if (logchk == null) {
        logchk = new CP_Classes.Login();
        pageContext.getSession().setAttribute("logchk", logchk);
      }
    }
    out.write(_jsp_string1, 0, _jsp_string1.length);
    CP_Classes.Translate trans;
    synchronized (pageContext.getSession()) {
      trans = (CP_Classes.Translate) pageContext.getSession().getAttribute("trans");
      if (trans == null) {
        trans = new CP_Classes.Translate();
        pageContext.getSession().setAttribute("trans", trans);
      }
    }
    out.write(_jsp_string1, 0, _jsp_string1.length);
    CP_Classes.Department Department;
    synchronized (pageContext.getSession()) {
      Department = (CP_Classes.Department) pageContext.getSession().getAttribute("Department");
      if (Department == null) {
        Department = new CP_Classes.Department();
        pageContext.getSession().setAttribute("Department", Department);
      }
    }
    out.write(_jsp_string1, 0, _jsp_string1.length);
    CP_Classes.Division Division;
    synchronized (pageContext.getSession()) {
      Division = (CP_Classes.Division) pageContext.getSession().getAttribute("Division");
      if (Division == null) {
        Division = new CP_Classes.Division();
        pageContext.getSession().setAttribute("Division", Division);
      }
    }
    out.write(_jsp_string1, 0, _jsp_string1.length);
    CP_Classes.Group Group;
    synchronized (pageContext.getSession()) {
      Group = (CP_Classes.Group) pageContext.getSession().getAttribute("Group");
      if (Group == null) {
        Group = new CP_Classes.Group();
        pageContext.getSession().setAttribute("Group", Group);
      }
    }
    out.write(_jsp_string1, 0, _jsp_string1.length);
    CP_Classes.Organization Orgs;
    synchronized (pageContext.getSession()) {
      Orgs = (CP_Classes.Organization) pageContext.getSession().getAttribute("Orgs");
      if (Orgs == null) {
        Orgs = new CP_Classes.Organization();
        pageContext.getSession().setAttribute("Orgs", Orgs);
      }
    }
    out.write(_jsp_string2, 0, _jsp_string2.length);
    // by lydia Date 05/09/2008 Fix jsp file to support Thai language 
    out.write(_jsp_string3, 0, _jsp_string3.length);
    
	String username = (String) session.getAttribute("username");

	if (!logchk.isUsable(username)) {

    out.write(_jsp_string4, 0, _jsp_string4.length);
    
 	}

 	/************************************************** ADDING TOGGLE FOR SORTING PURPOSE *************************************************/

 	int toggle = assignTR.getToggle(); //0=asc, 1=desc

 	int type = assignTR.getSortType(); //1=name, 2=origin		

 	if (request.getParameter("name") != null) {

 		if (toggle == 0)
 			toggle = 1;
 		else
 			toggle = 0;

 		assignTR.setToggle(toggle);

 		type = Integer.parseInt(request.getParameter("name"));
 		assignTR.setSortType(type);
 	}

 	/*********************************************************END ADDING TOGGLE FOR SORTING PURPOSE *************************************/

 	if (request.getParameter("add") != null) 
 	{
 		String[] User = request.getParameterValues("chkUser");
 		boolean canAdd;
 		int count = 0;
 		// Changed by Ha on 22/05/08 to pop out the message after adding
 		for (int i = 0; i < User.length; i++)
 		{
 		 	String round_str = request.getParameter("selRound");
 		 	if(round_str != null && round_str.length() != 0) {
 		 		int selRound = Integer.parseInt(round_str);
 		 		assignTR.setRound(selRound);
 		 	} else if(request.getParameter("roundRadio") != null && !request.getParameter("roundRadio").equals("existing")){
 		 		int selRound = Integer.parseInt(request.getParameter("roundRadio"));
 		 		assignTR.setRound(selRound);
 		 	}else{
 		 		assignTR.setRound(1);
 		 	}
 			canAdd = assignTR.addTarget(CE_Survey.getSurvey_ID(),Integer.parseInt(User[i]), logchk.getPKUser(), assignTR.getRound());
 			if (canAdd) 
 			{
 				 count++;
 
    out.write(' ');
    
 			} 
 			else if (canAdd == false) 
 			{
 
    out.write(_jsp_string5, 0, _jsp_string5.length);
    out.print((trans.tslt("Added unsucessfully")));
    out.write(_jsp_string6, 0, _jsp_string6.length);
    
 			}
 		}
 		if (count == User.length)
 		{
 
    out.write(_jsp_string7, 0, _jsp_string7.length);
    out.print((trans.tslt("Added successfully")));
    out.write(_jsp_string8, 0, _jsp_string8.length);
    
 		}

 		assignTR.set_selectedTargetID(0);
 		assignTR.set_selectedAssID(0);
 
    out.write(' ');
    
 	}

 	//if(request.getParameter("refresh") != null)
 	//{
 	String div_str = request.getParameter("selDivision");
 	String dept_str = request.getParameter("selDepartment");
 	String group_str = request.getParameter("GroupName");
 	boolean existingRound = true;
 	if(request.getParameter("roundRadio")!= null && !request.getParameter("roundRadio").equals("existing")){
 		existingRound = false;
 	}

 	if (group_str != null && group_str.length() != 0) {
 		int group = Integer.parseInt(group_str);
 		assignTR.setGroupID(group);
 	} else {
 		assignTR.setGroupID(0);
 	}
 	if (div_str != null && div_str.length() != 0) {
 		int div = Integer.parseInt(div_str);
 		assignTR.setDivID(div);
 	} else {
 		assignTR.setDivID(0);
 	}
 	if (dept_str != null && dept_str.length() != 0) {
 		int dept = Integer.parseInt(dept_str);
 		assignTR.setDeptID(dept);
 	} else {
 		assignTR.setDeptID(0);
 	}

 	//}

 	if (request.getParameter("close") != null) {
 		assignTR.set_selectedTargetID(0);
 		assignTR.set_selectedAssID(0);
 
    out.write(_jsp_string9, 0, _jsp_string9.length);
    
 	}
 
    out.write(_jsp_string10, 0, _jsp_string10.length);
    out.print((trans.tslt("Selected Survey")));
    out.write(_jsp_string11, 0, _jsp_string11.length);
    
			String SurveyName = " ";
			int Org = 0;
			votblSurvey voSurvey = CE_Survey.getSurveyDetail(CE_Survey
					.getSurvey_ID());
			if (voSurvey != null) {
				SurveyName = voSurvey.getSurveyName();
				Org = voSurvey.getFKOrganization();
				CE_Survey.set_survOrg(Org);
			}
		
    out.write(' ');
    out.print((SurveyName));
    out.write(_jsp_string12, 0, _jsp_string12.length);
    out.print((trans.tslt("Search Name Through")));
    out.write(_jsp_string13, 0, _jsp_string13.length);
    out.print((trans.tslt("Division")));
    out.write(_jsp_string14, 0, _jsp_string14.length);
    
				Vector vDiv = Division.getAllDivisions(logchk.getOrg());

				for (int i = 0; i < vDiv.size(); i++) {
					voDivision voDiv = (voDivision) vDiv.elementAt(i);

					int div_ID = voDiv.getPKDivision();
					String div_Name = voDiv.getDivisionName();

					if (assignTR.getDivID() != 0 && assignTR.getDivID() == div_ID) {
			
    out.write(_jsp_string15, 0, _jsp_string15.length);
    out.print((div_ID));
    out.write(_jsp_string16, 0, _jsp_string16.length);
    out.print((div_Name));
    out.write(_jsp_string17, 0, _jsp_string17.length);
    
				} else {
			
    out.write(_jsp_string15, 0, _jsp_string15.length);
    out.print((div_ID));
    out.write('>');
    out.print((div_Name));
    out.write(_jsp_string17, 0, _jsp_string17.length);
    
				}
				}
			
    out.write(_jsp_string18, 0, _jsp_string18.length);
    out.print((trans.tslt("Department")));
    out.write(_jsp_string19, 0, _jsp_string19.length);
    
				int div = 0;
				if (request.getParameter("div") != null) {
					String divID = request.getParameter("div");
					if (divID.length() > 0) {
						div = Integer.parseInt(divID);
					}
				}
				Vector vDepartments = Department.getAllDepartments(logchk.getOrg(),
						div);

				for (int i = 0; i < vDepartments.size(); i++) {

					voDepartment voD = (voDepartment) vDepartments.elementAt(i);
					int dep_ID = voD.getPKDepartment();
					String dep_Name = voD.getDepartmentName();
					if (assignTR.getDeptID() != 0 && assignTR.getDeptID() == dep_ID) {
			
    out.write(_jsp_string15, 0, _jsp_string15.length);
    out.print((dep_ID));
    out.write(_jsp_string16, 0, _jsp_string16.length);
    out.print((dep_Name));
    out.write(_jsp_string17, 0, _jsp_string17.length);
    
				} else {
			
    out.write(_jsp_string15, 0, _jsp_string15.length);
    out.print((dep_ID));
    out.write('>');
    out.print((dep_Name));
    out.write(_jsp_string20, 0, _jsp_string20.length);
    
				}
				}
			
    out.write(_jsp_string21, 0, _jsp_string21.length);
    out.print((trans.tslt("Group")));
    out.write(_jsp_string22, 0, _jsp_string22.length);
    
				int dept = 0;
				if (request.getParameter("dept") != null) {
					String deptID = request.getParameter("dept");
					if (deptID.length() > 0) {
						dept = Integer.parseInt(deptID);
					}
				}

				Vector vGroup = Group.getAllGroups(logchk.getOrg(), dept);
				for (int i = 0; i < vGroup.size(); i++) {

					voGroup voG = (voGroup) vGroup.elementAt(i);
					int Group_ID = voG.getPKGroup();
					String Group_Desc = voG.getGroupName();

					if (assignTR.getGroupID() == Group_ID) {
			
    out.write(_jsp_string15, 0, _jsp_string15.length);
    out.print((Group_ID));
    out.write(_jsp_string16, 0, _jsp_string16.length);
    out.print((Group_Desc));
    out.write(_jsp_string17, 0, _jsp_string17.length);
    
				} else {
			
    out.write(_jsp_string15, 0, _jsp_string15.length);
    out.print((Group_ID));
    out.write('>');
    out.print((Group_Desc));
    out.write(_jsp_string17, 0, _jsp_string17.length);
    
				}

				}
			
    out.write(_jsp_string23, 0, _jsp_string23.length);
    out.print(( trans.tslt("Search") ));
    out.write(_jsp_string24, 0, _jsp_string24.length);
    out.print((trans.tslt("Add to Round1")));
    out.write(_jsp_string25, 0, _jsp_string25.length);
     if(existingRound) { 
    out.write(_jsp_string26, 0, _jsp_string26.length);
    } 
    out.write(_jsp_string27, 0, _jsp_string27.length);
    out.print((assignTR.getNewRound(voSurvey.getSurveyID())));
    out.write(_jsp_string28, 0, _jsp_string28.length);
     if(!existingRound) { 
    out.write(_jsp_string26, 0, _jsp_string26.length);
    } 
    out.write(_jsp_string29, 0, _jsp_string29.length);
    out.print((assignTR.getNewRound(voSurvey.getSurveyID())));
    out.write(_jsp_string30, 0, _jsp_string30.length);
     if(existingRound){ 
    out.write(_jsp_string31, 0, _jsp_string31.length);
    

				Vector<Integer> rounds  = assignTR.getAllRound(voSurvey.getSurveyID());
				for (int i = 0; i < rounds.size(); i++) {
					int round = rounds.get(i);
					if (assignTR.getRound() == round) {
			
    out.write(_jsp_string15, 0, _jsp_string15.length);
    out.print((round));
    out.write(_jsp_string16, 0, _jsp_string16.length);
    out.print((round));
    out.write(_jsp_string17, 0, _jsp_string17.length);
    
				} else {
			
    out.write(_jsp_string15, 0, _jsp_string15.length);
    out.print((round));
    out.write('>');
    out.print((round));
    out.write(_jsp_string17, 0, _jsp_string17.length);
    
				}

				}
			
    out.write(_jsp_string32, 0, _jsp_string32.length);
     } 
    out.write(_jsp_string33, 0, _jsp_string33.length);
    out.print((trans.tslt("Targets")));
    out.write(_jsp_string34, 0, _jsp_string34.length);
    
			int NameSeqe = Orgs.getNameSeq(logchk.getOrg());
			String First = "Family Name"; //default
			String Second = "Other Name"; //default
			int FirstOrder = 1; //Name Order in the first column
			int SecondOrder = 2; //Name Order in the second column

			if (NameSeqe != 0) {
				First = "Other Name";
				Second = "Family Name";
				FirstOrder = 2;
				SecondOrder = 1;
			} //end if(NameSeqe != 0)
		
    out.write(_jsp_string35, 0, _jsp_string35.length);
    out.print((FirstOrder));
    out.write(_jsp_string36, 0, _jsp_string36.length);
    out.print((trans.tslt(First)));
    out.write(_jsp_string37, 0, _jsp_string37.length);
    out.print((SecondOrder));
    out.write(_jsp_string38, 0, _jsp_string38.length);
    out.print((trans.tslt(Second)));
    out.write(_jsp_string39, 0, _jsp_string39.length);
    out.print((trans.tslt("Login Name")));
    out.write(_jsp_string40, 0, _jsp_string40.length);
    
		// Changed by Ha 21/05/08 to sort the list
		//if(request.getParameter("refresh") != null)
		//{
		Vector vUser = assignTR.getUserList(CE_Survey.getSurvey_ID(),
				logchk.getPKUser(), CE_Survey.get_survOrg(), assignTR
						.getDivID(), assignTR.getDeptID(), assignTR
						.getGroupID(), logchk.getUserType());
		for (int k = 0; k < vUser.size(); k++) {
			voUser vo = (voUser) vUser.elementAt(k);

			int PKUser = vo.getPKUser();
			String FirstName = vo.getFamilyName(); //default order based on NameSeq == 0 (Family name first)
			String SecondName = vo.getGivenName(); //default order based on NameSeq == 0 (Family name first)
			String LoginName = vo.getLoginName();

			if (NameSeqe != 0) {
				//Swap name order
				String sTempName = FirstName;
				FirstName = SecondName;
				SecondName = sTempName;
			} //end if(NameSeqe != 0)
	
    out.write(_jsp_string41, 0, _jsp_string41.length);
    out.print((PKUser));
    out.write(_jsp_string42, 0, _jsp_string42.length);
    out.print((FirstName));
    out.write(_jsp_string43, 0, _jsp_string43.length);
    out.print((SecondName));
    out.write(_jsp_string44, 0, _jsp_string44.length);
    out.print((LoginName));
    out.write(_jsp_string45, 0, _jsp_string45.length);
    
		}
		//}
	
    out.write(_jsp_string46, 0, _jsp_string46.length);
    out.print(( trans.tslt("Cancel") ));
    out.write(_jsp_string47, 0, _jsp_string47.length);
    out.print(( trans.tslt("Add") ));
    out.write(_jsp_string48, 0, _jsp_string48.length);
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
    depend = new com.caucho.vfs.Depend(appDir.lookup("AssignTR_AddTarget.jsp"), 6781539376758237474L, false);
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

  private final static char []_jsp_string47;
  private final static char []_jsp_string42;
  private final static char []_jsp_string27;
  private final static char []_jsp_string32;
  private final static char []_jsp_string37;
  private final static char []_jsp_string29;
  private final static char []_jsp_string28;
  private final static char []_jsp_string48;
  private final static char []_jsp_string17;
  private final static char []_jsp_string20;
  private final static char []_jsp_string36;
  private final static char []_jsp_string9;
  private final static char []_jsp_string13;
  private final static char []_jsp_string18;
  private final static char []_jsp_string40;
  private final static char []_jsp_string30;
  private final static char []_jsp_string31;
  private final static char []_jsp_string4;
  private final static char []_jsp_string33;
  private final static char []_jsp_string35;
  private final static char []_jsp_string16;
  private final static char []_jsp_string46;
  private final static char []_jsp_string3;
  private final static char []_jsp_string10;
  private final static char []_jsp_string25;
  private final static char []_jsp_string2;
  private final static char []_jsp_string5;
  private final static char []_jsp_string44;
  private final static char []_jsp_string15;
  private final static char []_jsp_string43;
  private final static char []_jsp_string38;
  private final static char []_jsp_string8;
  private final static char []_jsp_string19;
  private final static char []_jsp_string34;
  private final static char []_jsp_string26;
  private final static char []_jsp_string39;
  private final static char []_jsp_string12;
  private final static char []_jsp_string6;
  private final static char []_jsp_string1;
  private final static char []_jsp_string22;
  private final static char []_jsp_string45;
  private final static char []_jsp_string7;
  private final static char []_jsp_string11;
  private final static char []_jsp_string24;
  private final static char []_jsp_string21;
  private final static char []_jsp_string41;
  private final static char []_jsp_string23;
  private final static char []_jsp_string14;
  private final static char []_jsp_string0;
  static {
    _jsp_string47 = "\" name=\"btnCancel\"\r\n			onClick=\"closeME(this.form)\"></td>\r\n		<td align=\"left\"><input type=\"button\"\r\n			value=\"".toCharArray();
    _jsp_string42 = "></span></font></td>\r\n		<td width=\"198\" align=\"left\"><font face=\"Arial\" size=\"2\">".toCharArray();
    _jsp_string27 = "\r\n		onChange=\"refresh(this.form)\">Existing Round\r\n		<input type = \"radio\" name = \"roundRadio\" value = \"".toCharArray();
    _jsp_string32 = "\r\n		</select></span></font></td>\r\n		<td width=\"85\"\r\n			style=\"border-right-style: solid; border-right-width: 1px; border-top-style: none; border-top-width: medium; border-bottom-style: none; border-bottom-width: medium\">\r\n		</td>\r\n	</tr>".toCharArray();
    _jsp_string37 = "</u></font></a></b></td>\r\n		<td width=\"200\" align=\"center\" bgcolor=\"#000080\"><b> <a\r\n			href=\"AssignTR_AddTarget.jsp?name=".toCharArray();
    _jsp_string29 = "\r\n		onChange=\"refresh(this.form)\"> New Round(Round ".toCharArray();
    _jsp_string28 = "\" ".toCharArray();
    _jsp_string48 = "\" name=\"btnAdd\"\r\n			onClick=\"return add(this.form,this.form.chkUser)\"></td>\r\n	</tr>\r\n</table>\r\n</form>\r\n</body>\r\n</html>".toCharArray();
    _jsp_string17 = "</option>\r\n			".toCharArray();
    _jsp_string20 = "</option>\r\n\r\n			".toCharArray();
    _jsp_string36 = "\"> <font\r\n			style='font-family: Arial; color: white' size=\"2\"> <u>".toCharArray();
    _jsp_string9 = " <script>\r\n		window.close();\r\n		opener.location.href ='AssignTarget_Rater.jsp';\r\n	</script> ".toCharArray();
    _jsp_string13 = ":</font></b></td>\r\n	</tr>\r\n	<tr>\r\n		<td width=\"9\"\r\n			style=\"border-left-style: solid; border-left-width: 1px; border-right-style: none; border-right-width: medium; border-top-style: solid; border-top-width: 1px\">&nbsp;</td>\r\n		<td width=\"118\"\r\n			style=\"border-left-style: none; border-left-width: medium; border-top-style: solid; border-top-width: 1px\">&nbsp;</td>\r\n		<td colspan=\"2\"\r\n			style=\"border-right-style: solid; border-right-width: 1px; border-top-style: solid; border-top-width: 1px\">&nbsp;\r\n		</td>\r\n	</tr>\r\n	<tr>\r\n		<td width=\"9\"\r\n			style=\"border-left-style: solid; border-left-width: 1px; border-right-style: none; border-right-width: medium\">&nbsp;</td>\r\n		<td width=\"118\"\r\n			style=\"border-left-style: none; border-left-width: medium\"><font\r\n			face=\"Arial\" size=\"2\">".toCharArray();
    _jsp_string18 = "\r\n\r\n		</select></td>\r\n	</tr>\r\n	<tr>\r\n		<td width=\"9\"\r\n			style=\"border-left-style: solid; border-left-width: 1px; border-right-style: none; border-right-width: medium\">&nbsp;</td>\r\n		<td width=\"118\"\r\n			style=\"border-left-style: none; border-left-width: medium\">&nbsp;</td>\r\n		<td colspan=\"2\"\r\n			style=\"border-right-style: solid; border-right-width: 1px\">&nbsp;</td>\r\n	</tr>\r\n	<tr>\r\n		<td width=\"9\"\r\n			style=\"border-left-style: solid; border-left-width: 1px; border-right-style: none; border-right-width: medium\">&nbsp;</td>\r\n		<td width=\"118\"\r\n			style=\"border-left-style: none; border-left-width: medium\"><font\r\n			face=\"Arial\" size=\"2\">".toCharArray();
    _jsp_string40 = "</u></font></a></b></td>\r\n	</tr>\r\n	".toCharArray();
    _jsp_string30 = ")</td>\r\n		<td width=\"60\"\r\n			style=\"border-right-style: solid; border-right-width: 1px; border-top-style: none; border-top-width: medium; border-bottom-style: none; border-bottom-width: medium\">\r\n		</td>\r\n	</tr>\r\n	".toCharArray();
    _jsp_string31 = "\r\n	<tr>\r\n		<td width=\"9\"\r\n			style=\"border-left-style: solid; border-left-width: 1px; border-right-style: none; border-right-width: medium\">&nbsp;</td>\r\n		<td width=\"118\"\r\n			style=\"border-left-style: none; border-left-width: medium\"></td>\r\n		<td width=\"209\"><font face=\"Arial\"><span\r\n			style=\"font-size: 11pt\"><select size=\"1\" name=\"selRound\">\r\n			".toCharArray();
    _jsp_string4 = "\r\n<font size=\"2\"> <script>\r\n	parent.location.href = \"index.jsp\";\r\n</script> ".toCharArray();
    _jsp_string33 = "\r\n</table>\r\n<div style='width: 433px; height: 277px; z-index: 1; overflow: auto'>\r\n<table border=\"1\" width=\"100%\" bgcolor=\"#FFFFCC\" bordercolor=\"#3399FF\">\r\n	<tr>\r\n		<td width=\"421\" colspan=\"4\" bgcolor=\"#000080\">\r\n		<p align=\"center\"><b> <font face=\"Arial\" color=\"#FFFFFF\"\r\n			size=\"2\">".toCharArray();
    _jsp_string35 = "\r\n		<td width=\"198\" align=\"center\" bgcolor=\"#000080\"><b> <a\r\n			href=\"AssignTR_AddTarget.jsp?name=".toCharArray();
    _jsp_string16 = " selected>".toCharArray();
    _jsp_string46 = "\r\n</table>\r\n</div>\r\n<table border=\"0\" width=\"46%\" cellspacing=\"0\" cellpadding=\"0\">\r\n	<tr>\r\n		<td width=\"266\" align=\"right\">&nbsp;</td>\r\n		<td align=\"right\">&nbsp;</td>\r\n	</tr>\r\n	<tr>\r\n		<td width=\"266\" align=\"left\"><input type=\"button\"\r\n			value=\"".toCharArray();
    _jsp_string3 = "\r\n</head>\r\n<SCRIPT LANGUAGE=JAVASCRIPT>\r\nfunction closeME(form)\r\n{ \r\n	form.action = \"AssignTR_AddTarget.jsp?close=1\";\r\n	form.method=\"post\";\r\n	form.submit();\r\n}\r\nfunction check(field)\r\n{\r\n	var isValid = 0;\r\n	var clickedValue = 0;\r\n	//check whether any checkbox selected\r\n	if( field == null ) {\r\n		isValid = 2;\r\n	\r\n	} else {\r\n\r\n		if(isNaN(field.length) == false) {\r\n			for (i = 0; i < field.length; i++)\r\n				if(field[i].checked) {\r\n					clickedValue = field[i].value;\r\n					isValid = 1;\r\n				}\r\n		}else {		\r\n			if(field.checked) {\r\n				clickedValue = field.value;\r\n				isValid = 1;\r\n			}\r\n				\r\n		}\r\n	}\r\n	\r\n	if(isValid == 1)\r\n		return clickedValue;\r\n	else if(isValid == 0)\r\n		alert(\"No record selected\");\r\n	else if(isValid == 2)\r\n		alert(\"No record available\");\r\n	\r\n	isValid = 0;\r\n		\r\n}\r\nfunction refresh(form, field1, field2, field3)\r\n{\r\n	form.action=\"AssignTR_AddTarget.jsp?refresh=\"+field3.value + \"&div=\" + field1.value + \"&dept=\" + field2.value;\r\n	form.method=\"post\";\r\n	form.submit();	\r\n}\r\n\r\nfunction add(form, field)\r\n{\r\n//\\\\ Changed by Ha on 20/05/08 to pop out the Add Target?\r\n	if(check(field))\r\n	{\r\n		if (confirm(\"Add Target?\"))\r\n		{\r\n		form.action=\"AssignTR_AddTarget.jsp?add=1\";\r\n		form.method=\"post\";\r\n		form.submit();	\r\n		return true;\r\n		}\r\n		else\r\n		return false;\r\n	}\r\n}\r\n\r\n\r\nfunction checkedAll(form, field, checkAll)\r\n{	\r\n	if(checkAll.checked == true) \r\n		for(var i=0; i<field.length; i++)\r\n			field[i].checked = true;\r\n	else \r\n		for(var i=0; i<field.length; i++)\r\n			field[i].checked = false;	\r\n}\r\n\r\nfunction populateDept(form, field)\r\n{\r\n	form.action=\"AssignTR_AddTarget.jsp?div=\"+ field.value;\r\n	form.method=\"post\";\r\n	form.submit();	\r\n}\r\n\r\nfunction populateGrp(form, field1, field2)\r\n{\r\n	form.action=\"AssignTR_AddTarget.jsp?div=\"+ field1.value + \"&dept=\" + field2.value;\r\n	form.method=\"post\";\r\n	form.submit();	\r\n}\r\n\r\nfunction populate(form, field1, field2, field3)\r\n{\r\n	form.action=\"AssignTR_AddTarget.jsp?div=\" + field1.value + \"&dept=\" + field2.value + \"&GroupName=\" + field3.value;\r\n	form.method=\"post\";\r\n	form.submit();	\r\n}\r\n\r\nfunction refresh(form)\r\n{\r\n	var radios = document.getElementsByName(\"roundRadio\");\r\n	var v = null;\r\n	if(radios[0].checked){\r\n		v = radios[0].value;\r\n	}else{\r\n		v = radios[1].value;\r\n	}\r\n	form.action=\"AssignTR_AddTarget.jsp?div=\" + form.selDivision.value + \"&dept=\" + \r\n			form.selDepartment.value + \"&GroupName=\" + form.GroupName.value + \"&roundRadio=\"+v;\r\n	form.method=\"post\";\r\n	form.submit();	\r\n}\r\n\r\n\r\n</SCRIPT>\r\n<body bgcolor=\"#E2E6F1\">\r\n".toCharArray();
    _jsp_string10 = "\r\n<form name=\"AssignTR_AddTarget\" action=\"AssignTR_AddTarget.jsp\"\r\n	method=\"post\">\r\n<table border=\"0\" width=\"430\" cellspacing=\"0\"\r\n	style=\"border-left-width: 2px; border-right-width: 2px; border-top-width: 2px; border-bottom-width: 0px\"\r\n	bordercolor=\"#3399FF\">\r\n	<tr>\r\n		<td width=\"128\" colspan=\"2\"><font size=\"2\"> <b><font\r\n			face=\"Arial\" size=\"2\"> ".toCharArray();
    _jsp_string25 = ":</font></td>\r\n		<td width=\"239\">	\r\n		<input type = \"radio\" name = \"roundRadio\" value = \"existing\" ".toCharArray();
    _jsp_string2 = "\r\n\r\n\r\n\r\n\r\n\r\n<html>\r\n<head>\r\n<!-- CSS -->\r\n\r\n<link type=\"text/css\" rel=\"stylesheet\" href=\"lib/css/bootstrap.css\">\r\n<link type=\"text/css\" rel=\"stylesheet\" href=\"lib/css/bootstrap-responsive.css\">\r\n<link type=\"text/css\" rel=\"stylesheet\" href=\"lib/css/bootstrap.min.css\">\r\n<link type=\"text/css\" rel=\"stylesheet\" href=\"lib/css/bootstrap-responsive.min.css\">\r\n\r\n\r\n<!-- jQuery -->\r\n<script type=\"text/javascript\" src=\"lib/js/bootstrap.min.js\"></script>\r\n<script type=\"text/javascript\" src=\"lib/js/bootstrap.js\"></script>\r\n<script type=\"text/javascript\" src=\"lib/js/jquery-1.9.1.js\"></script>\r\n\r\n\r\n<script src=\"lib/js/bootstrap.min.js\" type=\"text/javascript\"></script>\r\n<script src=\"lib/js/bootstrap-dropdown.js\"></script>\r\n\r\n\r\n<meta http-equiv=\"Content-Type\" content=\"text/html\">\r\n".toCharArray();
    _jsp_string5 = " 		     <script>\r\n				 alert(\"".toCharArray();
    _jsp_string44 = "</font></td>\r\n		<td width=\"144\" align=\"left\"><font face=\"Arial\" size=\"2\">".toCharArray();
    _jsp_string15 = "\r\n			<option value=".toCharArray();
    _jsp_string43 = "</font></td>\r\n		<td width=\"200\" align=\"left\"><font face=\"Arial\" size=\"2\">".toCharArray();
    _jsp_string38 = "\"><font\r\n			style='font-family: Arial; color: white' size=\"2\"> <u>".toCharArray();
    _jsp_string8 = "\");\r\n		     window.close();				  		\r\n			 opener.location.href = 'AssignTarget_Rater.jsp';\r\n			</script> ".toCharArray();
    _jsp_string19 = ":</font></td>\r\n		<td colspan=\"2\"\r\n			style=\"border-right-style: solid; border-right-width: 1px\"><select\r\n			size=\"1\" name=\"selDepartment\"\r\n			onChange=\"populateGrp(this.form, this.form.selDivision, this.form.selDepartment)\">\r\n\r\n			<option value=\"0\" selected>All</option>\r\n			".toCharArray();
    _jsp_string34 = "</font></b>\r\n		</td>\r\n	</tr>\r\n	<tr>\r\n		<td width=\"28\" align=\"center\" bgcolor=\"#000080\"><input\r\n			type=\"checkbox\" name=\"checkAll\"\r\n			onclick=\"checkedAll(this.form, this.form.chkUser,this.form.checkAll)\">\r\n		</td>\r\n\r\n		".toCharArray();
    _jsp_string26 = " checked".toCharArray();
    _jsp_string39 = "</u></font></a></b></td>\r\n\r\n		<td width=\"144\" align=\"center\" bgcolor=\"#000080\"><b> <a\r\n			href=\"AssignTR_AddTarget.jsp?name=3\"><font\r\n			style='font-family: Arial; color: white' size=\"2\"> <u>".toCharArray();
    _jsp_string12 = " </font></td>\r\n	</tr>\r\n	<tr>\r\n		<td width=\"10\">&nbsp;</td>\r\n		<td width=\"118\">&nbsp;</td>\r\n		<td colspan=\"2\">&nbsp;</td>\r\n	</tr>\r\n	<tr>\r\n		<td colspan=\"4\"><b><font face=\"Arial\" size=\"2\">".toCharArray();
    _jsp_string6 = "\");\r\n			     window.close();				  		\r\n				 opener.location.href = 'AssignTR_AddTarget_Rater.jsp';\r\n				</script> ".toCharArray();
    _jsp_string1 = "\r\n".toCharArray();
    _jsp_string22 = ":</font></td>\r\n		<td width=\"209\"><font face=\"Arial\"><span\r\n			style=\"font-size: 11pt\"><select size=\"1\" name=\"GroupName\"\r\n			onChange=\"populate(this.form, this.form.selDivision, this.form.selDepartment, this.form.GroupName)\">\r\n\r\n			<option value=\"0\" selected>All</option>\r\n			".toCharArray();
    _jsp_string45 = "</font></td>\r\n	</tr>\r\n	".toCharArray();
    _jsp_string7 = "			 <script>\r\n			 alert(\"".toCharArray();
    _jsp_string11 = ":</font></b></td>\r\n		<td colspan=\"2\"><font face=\"Arial\" style=\"font-size: 12\">\r\n		".toCharArray();
    _jsp_string24 = "\" name=\"btnSearch\"\r\n			style=\"float: left\"\r\n			onclick=\"refresh(this.form, this.form.selDivision, this.form.selDepartment, this.form.GroupName)\">\r\n		<p></td>\r\n	</tr>\r\n	<tr>\r\n		<td width=\"9\"\r\n			style=\"border-left-style: solid; border-left-width: 1px; border-right-style: none; border-right-width: medium\">&nbsp;</td>\r\n		<td width=\"118\"\r\n			style=\"border-left-style: none; border-left-width: medium\">&nbsp;</td>\r\n		<td colspan=\"2\"\r\n			style=\"border-right-style: solid; border-right-width: 1px\">&nbsp;</td>\r\n	</tr>\r\n	<tr>\r\n	<td width=\"9\"\r\n			style=\"border-left-style: solid; border-left-width: 1px; border-right-style: none; border-right-width: medium\">&nbsp;</td>\r\n		<td width=\"118\"\r\n			style=\"border-left-style: none; border-left-width: medium\"><font\r\n			face=\"Arial\" size=\"2\">".toCharArray();
    _jsp_string21 = "\r\n		</select></td>\r\n	</tr>\r\n	<tr>\r\n		<td width=\"9\"\r\n			style=\"border-left-style: solid; border-left-width: 1px; border-right-style: none; border-right-width: medium\">&nbsp;</td>\r\n		<td width=\"118\"\r\n			style=\"border-left-style: none; border-left-width: medium\">&nbsp;</td>\r\n		<td colspan=\"2\"\r\n			style=\"border-right-style: solid; border-right-width: 1px\">&nbsp;</td>\r\n	</tr>\r\n	<tr>\r\n		<td width=\"9\"\r\n			style=\"border-left-style: solid; border-left-width: 1px; border-right-style: none; border-right-width: medium\">&nbsp;</td>\r\n		<td width=\"118\"\r\n			style=\"border-left-style: none; border-left-width: medium\"><font\r\n			face=\"Arial\" size=\"2\">".toCharArray();
    _jsp_string41 = "\r\n	<tr onMouseOver=\"this.bgColor = '#99ccff'\"\r\n		onMouseOut=\"this.bgColor = '#FFFFcc'\">\r\n		<td width=\"28\" align=\"center\"><font face=\"Arial\"><span\r\n			style=\"font-size: 11pt\"> <input type=\"checkbox\" name=\"chkUser\"\r\n			value=".toCharArray();
    _jsp_string23 = "\r\n		</select></span></font></td>\r\n		<td width=\"85\"\r\n			style=\"border-right-style: solid; border-right-width: 1px; border-top-style: none; border-top-width: medium; border-bottom-style: none; border-bottom-width: medium\">\r\n		</td>\r\n	</tr>\r\n<tr>\r\n		<td width=\"9\"\r\n			style=\"border-left-style: solid; border-left-width: 1px; border-right-style: none; border-right-width: medium\">&nbsp;</td>\r\n		<td width=\"118\"\r\n			style=\"border-left-style: none; border-left-width: medium\">&nbsp;</td>\r\n		<td colspan=\"2\"\r\n			style=\"border-right-style: solid; border-right-width: 1px\">&nbsp;</td>\r\n	</tr>\r\n	<tr>\r\n		<td width=\"9\"\r\n			style=\"border-left-style: solid; border-left-width: 1px; border-right-style: none; border-right-width: medium; border-bottom-style: solid; border-bottom-width: 1px\">&nbsp;</td>\r\n		<td width=\"118\"\r\n			style=\"border-left-style: none; border-left-width: medium; border-bottom-style: solid; border-bottom-width: 1px\">&nbsp;</td>\r\n		<td colspan=\"2\"\r\n			style=\"border-right-style: solid; border-right-width: 1px; border-bottom-style: solid; border-bottom-width: 1px\">\r\n		<font size=\"2\"> <input type=\"button\"\r\n			value=\"".toCharArray();
    _jsp_string14 = ":</font></td>\r\n		<td colspan=\"2\"\r\n			style=\"border-right-style: solid; border-right-width: 1px\"><select\r\n			size=\"1\" name=\"selDivision\"\r\n			onChange=\"populateDept(this.form, this.form.selDivision)\">\r\n\r\n			<option value=\"0\" selected>All</option>\r\n\r\n\r\n			".toCharArray();
    _jsp_string0 = "\r\n\r\n".toCharArray();
  }
}