/*
 * JSP generated by Resin Professional 4.0.36 (built Fri, 26 Apr 2013 03:33:09 PDT)
 */

package _jsp._coach;
import javax.servlet.*;
import javax.servlet.jsp.*;
import javax.servlet.http.*;
import java.sql.*;
import java.io.*;
import java.util.Date;
import java.util.Vector;
import java.text.SimpleDateFormat;
import CP_Classes.vo.*;

public class _addslottouserassignment__jsp extends com.caucho.jsp.JavaPage
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
    
	// Author: Dai Yong in AUG 2013

    out.write(_jsp_string1, 0, _jsp_string1.length);
    CP_Classes.Login logchk;
    synchronized (pageContext.getSession()) {
      logchk = (CP_Classes.Login) pageContext.getSession().getAttribute("logchk");
      if (logchk == null) {
        logchk = new CP_Classes.Login();
        pageContext.getSession().setAttribute("logchk", logchk);
      }
    }
    out.write(_jsp_string2, 0, _jsp_string2.length);
    Coach.LoginStatus LoginStatus;
    synchronized (pageContext.getSession()) {
      LoginStatus = (Coach.LoginStatus) pageContext.getSession().getAttribute("LoginStatus");
      if (LoginStatus == null) {
        LoginStatus = new Coach.LoginStatus();
        pageContext.getSession().setAttribute("LoginStatus", LoginStatus);
      }
    }
    out.write(_jsp_string2, 0, _jsp_string2.length);
    Coach.CoachDateGroup CoachDateGroup;
    CoachDateGroup = (Coach.CoachDateGroup) pageContext.getAttribute("CoachDateGroup");
    if (CoachDateGroup == null) {
      CoachDateGroup = new Coach.CoachDateGroup();
      pageContext.setAttribute("CoachDateGroup", CoachDateGroup);
    }
    out.write(_jsp_string2, 0, _jsp_string2.length);
    Coach.CoachSlotGroup CoachSlotGroup;
    synchronized (pageContext.getSession()) {
      CoachSlotGroup = (Coach.CoachSlotGroup) pageContext.getSession().getAttribute("CoachSlotGroup");
      if (CoachSlotGroup == null) {
        CoachSlotGroup = new Coach.CoachSlotGroup();
        pageContext.getSession().setAttribute("CoachSlotGroup", CoachSlotGroup);
      }
    }
    out.write(_jsp_string2, 0, _jsp_string2.length);
    Coach.CoachDate CoachDate;
    synchronized (pageContext.getSession()) {
      CoachDate = (Coach.CoachDate) pageContext.getSession().getAttribute("CoachDate");
      if (CoachDate == null) {
        CoachDate = new Coach.CoachDate();
        pageContext.getSession().setAttribute("CoachDate", CoachDate);
      }
    }
    out.write(_jsp_string2, 0, _jsp_string2.length);
    Coach.CoachSlot CoachSlot;
    synchronized (pageContext.getSession()) {
      CoachSlot = (Coach.CoachSlot) pageContext.getSession().getAttribute("CoachSlot");
      if (CoachSlot == null) {
        CoachSlot = new Coach.CoachSlot();
        pageContext.getSession().setAttribute("CoachSlot", CoachSlot);
      }
    }
    out.write(_jsp_string2, 0, _jsp_string2.length);
    Coach.Coach Coach;
    synchronized (pageContext.getSession()) {
      Coach = (Coach.Coach) pageContext.getSession().getAttribute("Coach");
      if (Coach == null) {
        Coach = new Coach.Coach();
        pageContext.getSession().setAttribute("Coach", Coach);
      }
    }
    out.write(_jsp_string2, 0, _jsp_string2.length);
    Coach.CoachVenue Venue;
    synchronized (pageContext.getSession()) {
      Venue = (Coach.CoachVenue) pageContext.getSession().getAttribute("Venue");
      if (Venue == null) {
        Venue = new Coach.CoachVenue();
        pageContext.getSession().setAttribute("Venue", Venue);
      }
    }
    out.write(_jsp_string2, 0, _jsp_string2.length);
    Coach.SessionSetup SessionSetup;
    synchronized (pageContext.getSession()) {
      SessionSetup = (Coach.SessionSetup) pageContext.getSession().getAttribute("SessionSetup");
      if (SessionSetup == null) {
        SessionSetup = new Coach.SessionSetup();
        pageContext.getSession().setAttribute("SessionSetup", SessionSetup);
      }
    }
    out.write(_jsp_string3, 0, _jsp_string3.length);
    
		String username = (String) session.getAttribute("username");

		if (!logchk.isUsable(username)) {
	
    out.write(_jsp_string4, 0, _jsp_string4.length);
    
 	} else {
 		//for maintaining the selected data in the dropdown list

 		Vector DateGroupList = new Vector();
 		DateGroupList = CoachDateGroup.getAllDateGroup();
 		Vector SlotGroupList = new Vector();
 		SlotGroupList = CoachSlotGroup.getAllSlotGroup();
 		Vector coachList = new Vector();
 		coachList = Coach.getAllCoach();
 		Vector venueList = new Vector();
 		venueList = Venue.getAllCoachVenue();

 		int selSessionID;
 		int selDateGroup;
 		int selDate;
 		int selSlotGroup;
 		int selSlot;
 		int selCoach;
 		int selVenue;
 		Vector coachDates = new Vector();
 		Vector coachSlots = new Vector();
 		if (request.getParameter("SessionID") != null) {
 			selSessionID = Integer.parseInt(request
 					.getParameter("SessionID"));
 			SessionSetup.setSelectedSession(selSessionID);
 		//	System.out.println("SessionID: " + selSessionID);
 		}
 		//Date
 		if (request.getParameter("selectDateGroup") != null) {
 			selDateGroup = Integer.parseInt(request
 					.getParameter("selDateGroup"));
 			SessionSetup.setSelectedDayGroup(selDateGroup);
 			coachDates = CoachDateGroup
 					.getSelectedDateGroupDetails(selDateGroup);
 		}
 		if (request.getParameter("selectDate") != null) {
 			selDate = Integer.parseInt(request.getParameter("selDate"));
 			SessionSetup.setSelectedDate(selDate);

 		}
 		if (SessionSetup.getSelectedDayGroup() != 0) {
 			coachDates = CoachDateGroup
 					.getSelectedDateGroupDetails(SessionSetup
 							.getSelectedDayGroup());
 		}
 		//Slot
 		if (request.getParameter("selectSlotGroup") != null) {
 			selSlotGroup = Integer.parseInt(request
 					.getParameter("selSlotGroup"));
 			SessionSetup.setSelectedSlotGroup(selSlotGroup);
 			coachSlots = CoachSlotGroup
 					.getSelectedSlotGroupDetails(selSlotGroup);
 		}
 		if (request.getParameter("selectSlot") != null) {
 			selSlot = Integer.parseInt(request.getParameter("selSlot"));
 			SessionSetup.setSelectedSlot(selSlot);
 		}
 		if (SessionSetup.getSelectedSlotGroup() != 0) {
 			coachSlots = CoachSlotGroup
 					.getSelectedSlotGroupDetails(SessionSetup
 							.getSelectedSlotGroup());
 		}
 		//Coach and Venue
 		if (request.getParameter("selectCoach") != null) {
 			selCoach = Integer.parseInt(request
 					.getParameter("selCoach"));
 			SessionSetup.setSelectedCoach(selCoach);
 		}
 		if (request.getParameter("selectVenue") != null) {
 			selVenue = Integer.parseInt(request
 					.getParameter("selVenue"));
 			SessionSetup.setSelectedVenue(selVenue);
 		}

 		if (request.getParameter("add") != null) {
 			if(SessionSetup.getSelectedDayGroup()==0){
 				 
    out.write(_jsp_string5, 0, _jsp_string5.length);
     
 			}else if(SessionSetup.getSelectedDate()==0){
 				 
    out.write(_jsp_string6, 0, _jsp_string6.length);
     
 			}else if(SessionSetup.getSelectedSlotGroup()==0){
 				 
    out.write(_jsp_string7, 0, _jsp_string7.length);
     
 			}else if(SessionSetup.getSelectedSlot()==0){
 				 
    out.write(_jsp_string8, 0, _jsp_string8.length);
     
 			}else if(SessionSetup.getSelectedCoach()==0){
				 
    out.write(_jsp_string9, 0, _jsp_string9.length);
     
			}
 			else{
 				boolean suc=SessionSetup.AddSlotToUserAssignment();
 				//add the time slot to user assignment
 				if(suc){
					
    out.write(_jsp_string10, 0, _jsp_string10.length);
     
				}
				else{
					
				}
 			}
 			

 					
 		}
 
    out.write(_jsp_string11, 0, _jsp_string11.length);
    
								voCoachDateGroup voCoachDateGroup = new voCoachDateGroup();

									for (int i = 0; i < DateGroupList.size(); i++) {
										voCoachDateGroup = (voCoachDateGroup) DateGroupList
												.elementAt(i);
										int DateGroupPK = voCoachDateGroup.getPK();
										String DateGroupName = voCoachDateGroup.getName();
										String DateGroupDis = voCoachDateGroup.getdescription();

										if (SessionSetup.getSelectedDayGroup() == DateGroupPK) {
							
    out.write(_jsp_string12, 0, _jsp_string12.length);
    out.print((DateGroupPK));
    out.write(_jsp_string13, 0, _jsp_string13.length);
    out.print((DateGroupName));
    out.write(_jsp_string14, 0, _jsp_string14.length);
    
									} else {
								
    out.write(_jsp_string15, 0, _jsp_string15.length);
    out.print((DateGroupPK));
    out.write('>');
    out.print((DateGroupName));
    out.write(_jsp_string14, 0, _jsp_string14.length);
    
									}
										}
								
    out.write(_jsp_string16, 0, _jsp_string16.length);
    
								int DisplayNo = 1;
									int DatePK = 0;
									voCoachDate voCoachDate = new voCoachDate();

									for (int i = 0; i < coachDates.size(); i++) {
										voCoachDate = (voCoachDate) coachDates.elementAt(i);
										DatePK = voCoachDate.getPK();
										String date = voCoachDate.getDate();
										String[] DateInParts = date.split(" ");
										String dateWithoutTime = DateInParts[0];
										String[] DateWithoutTimeInParts = dateWithoutTime
												.split("-");
										String finalDate = DateWithoutTimeInParts[2] + "-"
												+ DateWithoutTimeInParts[1] + "-"
												+ DateWithoutTimeInParts[0];
										if (SessionSetup.getSelectedDate() == DatePK) {
							
    out.write(_jsp_string12, 0, _jsp_string12.length);
    out.print((DatePK));
    out.write(_jsp_string13, 0, _jsp_string13.length);
    out.print((finalDate));
    out.write(_jsp_string14, 0, _jsp_string14.length);
    
									} else {
								
    out.write(_jsp_string15, 0, _jsp_string15.length);
    out.print((DatePK));
    out.write('>');
    out.print((finalDate));
    out.write(_jsp_string14, 0, _jsp_string14.length);
    
									}
										}
								
    out.write(_jsp_string17, 0, _jsp_string17.length);
    
								voCoachSlotGroup voCoachSlotGroup = new voCoachSlotGroup();

									for (int i = 0; i < SlotGroupList.size(); i++) {
										voCoachSlotGroup = (voCoachSlotGroup) SlotGroupList
												.elementAt(i);
										int slotGroupPK = voCoachSlotGroup.getPk();
										String slotGroupName = voCoachSlotGroup.getSlotGroupName();

										if (SessionSetup.getSelectedSlotGroup() == slotGroupPK) {
							
    out.write(_jsp_string12, 0, _jsp_string12.length);
    out.print((slotGroupPK));
    out.write(_jsp_string13, 0, _jsp_string13.length);
    out.print((slotGroupName));
    out.write(_jsp_string14, 0, _jsp_string14.length);
    
									} else {
								
    out.write(_jsp_string15, 0, _jsp_string15.length);
    out.print((slotGroupPK));
    out.write('>');
    out.print((slotGroupName));
    out.write(_jsp_string14, 0, _jsp_string14.length);
    
									}
										}
								
    out.write(_jsp_string18, 0, _jsp_string18.length);
    
								// asdf
									DisplayNo = 1;
									int slotPK = 0;

									for (int i = 0; i < coachSlots.size(); i++) {
										voCoachSlot voCoachSlot = new voCoachSlot();
										voCoachSlot = (voCoachSlot) coachSlots.elementAt(i);

										slotPK = voCoachSlot.getPK();
										int startingTime = voCoachSlot.getStartingtime();
										int endingingTime = voCoachSlot.getEndingtime();
										String startingTime4Digits;
										String endingTime4Digits;
										if (startingTime < 1000) {
											startingTime4Digits = "0" + startingTime;
										} else {
											startingTime4Digits = "" + startingTime;
										}
										if (endingingTime < 1000) {
											endingTime4Digits = "0" + endingingTime;
										} else {
											endingTime4Digits = "" + endingingTime;
										}

										if (SessionSetup.getSelectedSlot() == slotPK) {
							
    out.write(_jsp_string12, 0, _jsp_string12.length);
    out.print((slotPK));
    out.write(_jsp_string13, 0, _jsp_string13.length);
    out.print((startingTime4Digits));
    out.write(_jsp_string19, 0, _jsp_string19.length);
    out.print((endingTime4Digits));
    out.write(_jsp_string14, 0, _jsp_string14.length);
    
									} else {
								
    out.write(_jsp_string15, 0, _jsp_string15.length);
    out.print((slotPK));
    out.write('>');
    out.print((startingTime4Digits));
    out.write(_jsp_string19, 0, _jsp_string19.length);
    out.print((endingTime4Digits));
    out.write(_jsp_string14, 0, _jsp_string14.length);
    
									}
										}
								
    out.write(_jsp_string20, 0, _jsp_string20.length);
    
								voCoach voCoach = new voCoach();
									int coachPK;
									for (int i = 0; i < coachList.size(); i++) {
										voCoach = (voCoach) coachList.elementAt(i);

										coachPK = voCoach.getPk();
										String name = voCoach.getCoachName();

										if (SessionSetup.getSelectedCoach() == coachPK) {
							
    out.write(_jsp_string12, 0, _jsp_string12.length);
    out.print((coachPK));
    out.write(_jsp_string13, 0, _jsp_string13.length);
    out.print((name));
    out.write(_jsp_string14, 0, _jsp_string14.length);
    
									} else {
								
    out.write(_jsp_string15, 0, _jsp_string15.length);
    out.print((coachPK));
    out.write('>');
    out.print((name));
    out.write(_jsp_string14, 0, _jsp_string14.length);
    
									}
										}
								
    out.write(_jsp_string21, 0, _jsp_string21.length);
    
								voCoachVenue voVenue = new voCoachVenue();
									int venuePK;
									for (int i = 0; i < venueList.size(); i++) {
										voVenue = (voCoachVenue) venueList.elementAt(i);

										venuePK = voVenue.getVenuePK();
										String venue1 = voVenue.getVenue1();

										if (SessionSetup.getSelectedVenue() == venuePK) {
							
    out.write(_jsp_string12, 0, _jsp_string12.length);
    out.print((venuePK));
    out.write(_jsp_string13, 0, _jsp_string13.length);
    out.print((venue1));
    out.write(_jsp_string14, 0, _jsp_string14.length);
    
									} else {
								
    out.write(_jsp_string15, 0, _jsp_string15.length);
    out.print((venuePK));
    out.write('>');
    out.print((venue1));
    out.write(_jsp_string14, 0, _jsp_string14.length);
    
									}
										}
								
    out.write(_jsp_string22, 0, _jsp_string22.length);
    
 	}
 
    out.write(_jsp_string23, 0, _jsp_string23.length);
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
    depend = new com.caucho.vfs.Depend(appDir.lookup("Coach/AddSlotToUserAssignment.jsp"), 1364256312273013352L, false);
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

  private final static char []_jsp_string2;
  private final static char []_jsp_string22;
  private final static char []_jsp_string18;
  private final static char []_jsp_string8;
  private final static char []_jsp_string9;
  private final static char []_jsp_string21;
  private final static char []_jsp_string11;
  private final static char []_jsp_string17;
  private final static char []_jsp_string23;
  private final static char []_jsp_string5;
  private final static char []_jsp_string20;
  private final static char []_jsp_string13;
  private final static char []_jsp_string16;
  private final static char []_jsp_string1;
  private final static char []_jsp_string4;
  private final static char []_jsp_string14;
  private final static char []_jsp_string7;
  private final static char []_jsp_string10;
  private final static char []_jsp_string0;
  private final static char []_jsp_string6;
  private final static char []_jsp_string12;
  private final static char []_jsp_string19;
  private final static char []_jsp_string15;
  private final static char []_jsp_string3;
  static {
    _jsp_string2 = "\r\n	".toCharArray();
    _jsp_string22 = "\r\n							\r\n					</select></td>\r\n				</tr>\r\n				<!-- end of Venue -->\r\n\r\n				<tr>\r\n					<td height=\"20\" colspan=\"3\"><font face=\"Arial\" size=\"2\"></font></td>\r\n				</tr>\r\n				<tr>\r\n					<td width=\"140\" colspan=\"3\"><font color=\"#000080\" face=\"Arial\"\r\n						size=\"2\">Notes:</font></td>\r\n				</tr>\r\n				<tr>\r\n					<td width=\"140\" colspan=\"3\"><font color=\"#000080\" face=\"Arial\"\r\n						size=\"2\">New Coaching Date should be added in Coaching\r\n							Period Management.</font></td>\r\n				</tr>\r\n				<tr>\r\n					<td width=\"140\" colspan=\"3\"><font color=\"#000080\" face=\"Arial\"\r\n						size=\"2\">New Time Slot should be added in Time Slot\r\n							Management.</font></td>\r\n				</tr>\r\n				<tr>\r\n					<td height=\"20\" colspan=\"3\"><font face=\"Arial\" size=\"2\"></font></td>\r\n				</tr>\r\n			</table>\r\n\r\n\r\n\r\n			<blockquote>\r\n				<blockquote>\r\n					<p>\r\n						<font face=\"Arial\">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;\r\n						</font> <font face=\"Arial\" span\r\n							style=\"font-size: 10.0pt; font-family: Arial\"> <input\r\n							class=\"btn btn-primary\" type=\"button\" name=\"Submit\"\r\n							value=\"Submit\" onClick=\"confirmAdd(this.form)\">\r\n						</font><font span style='font-family: Arial'> </font> <font face=\"Arial\">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;\r\n						</font> <font face=\"Arial\" span\r\n							style=\"font-size: 10.0pt; font-family: Arial\"> <input\r\n							name=\"Cancel\" class=\"btn btn-primary\" type=\"button\" id=\"Cancel\"\r\n							value=\"Save and Exit\" onClick=\"cancelAdd()\">\r\n						</font>\r\n					</p>\r\n				</blockquote>\r\n			</blockquote>\r\n\r\n		</form> ".toCharArray();
    _jsp_string18 = "\r\n							\r\n					</select></td>\r\n				</tr>\r\n				<tr>\r\n					<td width=\"120\"><font face=\"Arial\" size=\"2\">Time Slot:</font></td>\r\n					<td width=\"23\">:</td>\r\n					<td width=\"500\" colspan=\"1\"><select size=\"1\" name=\"selSlot\"\r\n						onChange=\"selectSlot(this.form)\">\r\n							<option value=0>Please Select Time Slot</option>\r\n							".toCharArray();
    _jsp_string8 = "<script>\r\n				 alert(\"Please Select Coaching Time\");\r\n				 </script>".toCharArray();
    _jsp_string9 = "<script>\r\n				 alert(\"Please Select Coach\");\r\n				 </script>".toCharArray();
    _jsp_string21 = "\r\n							\r\n					</select></td>\r\n				</tr>\r\n				<!-- end of Coach -->\r\n\r\n				<!-- Venue -->\r\n				<tr>\r\n					<td width=\"140\"><font face=\"Arial\" size=\"2\">Venue:</font></td>\r\n					<td width=\"23\">:</td>\r\n					<td width=\"500\" colspan=\"1\"><select size=\"1\" name=\"selVenue\"\r\n						onChange=\"selectVenue(this.form)\">\r\n							<option value=0>Please select Venue</option>\r\n							".toCharArray();
    _jsp_string11 = "\r\n		<p>\r\n			<b><font color=\"#000080\" size=\"3\" face=\"Arial\">Add\r\n					Coaching Slot to Candidate Assignment</font></b>\r\n		</p>\r\n		<form name=\"AddSlotToUserAssignment\" method=\"post\">\r\n			<table>\r\n				<tr>\r\n					<td width=\"140\"><font face=\"Arial\" size=\"2\">Coaching\r\n							Period Name:</font></td>\r\n					<td width=\"23\">:</td>\r\n					<td width=\"500\" colspan=\"1\"><select size=\"1\"\r\n						name=\"selDateGroup\" onChange=\"selectDateGroup(this.form)\">\r\n							<option value=0>Please select a Coaching</option>\r\n							".toCharArray();
    _jsp_string17 = "\r\n							\r\n					</select></td>\r\n				</tr>\r\n				<tr>\r\n					<td height=\"20\"></td>\r\n				</tr>\r\n				<tr>\r\n					<td width=\"120\"><font face=\"Arial\" size=\"2\">Time Slot\r\n							Name:</font></td>\r\n					<td width=\"23\">:</td>\r\n					<td width=\"500\" colspan=\"1\"><select size=\"1\"\r\n						name=\"selSlotGroup\" onChange=\"selectSlotGroup(this.form)\">\r\n							<option value=0>Please Select Time Slot Name</option>\r\n							".toCharArray();
    _jsp_string23 = "\r\n</body>\r\n</html>".toCharArray();
    _jsp_string5 = "<script>\r\n				 alert(\"Please Select Coaching Period\");\r\n				 </script>".toCharArray();
    _jsp_string20 = "\r\n							\r\n					</select></td>\r\n				</tr>\r\n				<tr>\r\n					<td height=\"20\"></td>\r\n				</tr>\r\n				<!--Coach  -->\r\n				<tr>\r\n					<td width=\"140\"><font face=\"Arial\" size=\"2\">Coach Name:</font></td>\r\n					<td width=\"23\">:</td>\r\n					<td width=\"500\" colspan=\"1\"><select size=\"1\" name=\"selCoach\"\r\n						onChange=\"selectCoach(this.form)\">\r\n							<option value=0>Please select a Coach</option>\r\n							".toCharArray();
    _jsp_string13 = " selected>".toCharArray();
    _jsp_string16 = "\r\n							\r\n					</select></td>\r\n				</tr>\r\n				<tr>\r\n					<td width=\"140\"><font face=\"Arial\" size=\"2\">Coaching\r\n							Date:</font></td>\r\n					<td width=\"23\">:</td>\r\n					<td width=\"500\" colspan=\"1\"><select size=\"1\" name=\"selDate\"\r\n						onChange=\"selectDate(this.form)\">\r\n							<option value=0>Please select a Coaching Date</option>\r\n							".toCharArray();
    _jsp_string1 = "\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">\r\n<html>\r\n<head>\r\n\r\n<title>Add Slot To User Assignment</title>\r\n\r\n<meta http-equiv=\"Content-Type\" content=\"text/html\">\r\n<style type=\"text/css\">\r\n<!--\r\nbody {\r\n	background-color: #eaebf4;\r\n}\r\n-->\r\n</style>\r\n</head>\r\n\r\n<body style=\"background-color: #DEE3EF\">\r\n	".toCharArray();
    _jsp_string4 = "\r\n	<font size=\"2\"> <script>\r\n		parent.location.href = \"../index.jsp\";\r\n	</script> ".toCharArray();
    _jsp_string14 = "\r\n								".toCharArray();
    _jsp_string7 = "<script>\r\n				 alert(\"Please Select Time Slot Name\");\r\n				 </script>".toCharArray();
    _jsp_string10 = "\r\n					<script>\r\n					alert(\"Coaching Slot Added Successfully\");\r\n					\r\n					</script>\r\n					".toCharArray();
    _jsp_string0 = "\r\n".toCharArray();
    _jsp_string6 = "<script>\r\n				 alert(\"Please Select Coaching Date\");\r\n				 </script>".toCharArray();
    _jsp_string12 = "\r\n							<option value=".toCharArray();
    _jsp_string19 = " - ".toCharArray();
    _jsp_string15 = "\r\n							\r\n							<option value=".toCharArray();
    _jsp_string3 = "\r\n	<script language=\"javascript\">\r\n		function confirmAdd(form) {\r\n			form.action = \"AddSlotToUserAssignment.jsp?add=1\";\r\n			form.method = \"post\";\r\n			form.submit();\r\n		}\r\n		function selectDateGroup(form) {\r\n			form.action = \"AddSlotToUserAssignment.jsp?selectDateGroup=1\";\r\n			form.method = \"post\";\r\n			form.submit();\r\n		}\r\n		function selectDate(form) {\r\n			form.action = \"AddSlotToUserAssignment.jsp?selectDate=1\";\r\n			form.method = \"post\";\r\n			form.submit();\r\n		}\r\n		function selectSlotGroup(form) {\r\n			form.action = \"AddSlotToUserAssignment.jsp?selectSlotGroup=1\";\r\n			form.method = \"post\";\r\n			form.submit();\r\n		}\r\n		function selectSlot(form) {\r\n			form.action = \"AddSlotToUserAssignment.jsp?selectSlot=1\";\r\n			form.method = \"post\";\r\n			form.submit();\r\n		}\r\n		function selectCoach(form) {\r\n			form.action = \"AddSlotToUserAssignment.jsp?selectCoach=1\";\r\n			form.method = \"post\";\r\n			form.submit();\r\n		}\r\n		function selectVenue(form) {\r\n			form.action = \"AddSlotToUserAssignment.jsp?selectVenue=1\";\r\n			form.method = \"post\";\r\n			form.submit();\r\n		}\r\n\r\n		function cancelAdd() {\r\n			opener.location.href = \"UserAssignment.jsp\";\r\n			window.close();\r\n		}\r\n	</script>\r\n\r\n	".toCharArray();
  }
}