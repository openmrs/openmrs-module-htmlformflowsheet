<%@ page errorPage="/errorhandler.jsp" %>
<%@ page import="org.openmrs.web.WebConstants" %>
<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ taglib prefix="htmlformflowsheet" uri="/WEB-INF/view/module/htmlformflowsheet/taglib/htmlformflowsheet.tld" %>
<htmlformflowsheet:htmlInclude file="/dwr/engine.js" />
<htmlformflowsheet:htmlInclude file="/dwr/util.js" />
<openmrs:htmlInclude file="/dwr/interface/DWRAlertService.js" />
<htmlformflowsheet:htmlInclude file="/scripts/jquery/jquery-1.3.2.min.js" />
<script src='<%= request.getContextPath() %>/dwr/interface/HtmlFormFlowhseetFindPatient.js'></script>
<openmrs:htmlInclude file="/moduleResources/htmlformflowsheet/htmlFormFlowsheet.css"/>


<openmrs:globalProperty key="use_patient_attribute.healthCenter" var="useHealthCenter"/>
<openmrs:globalProperty key="use_patient_attribute.mothersName" var="useMothersName"/>
<openmrs:globalProperty key="use_patient_attribute.tribe" var="useTribe"/>
<openmrs:globalProperty key="htmlformflowsheet.programConfigurationMap" var="linkMapString"/>


<c:set var="numResults" value="10"/>
<script type="text/javascript">
        var programLinkMap = ${linkMapString};
		var classTmp = "";
		var from = 0;
		var jumps = ${numResults}; //this many patients at a time
		var to = jumps-1;
		var retSize = 0;
		var headerShown = 0;
		var savedRet = new Array();
		var mappedRet = new Array();
		var showLoading = 0;
		var $j = jQuery.noConflict();		
		$j(document).ready(function(){
   				$j('#results_${model.portletUUID}').css('display','none');
   				$j('#searchBox_${model.portletUUID}').val('');
   					$j('#searchBox_${model.portletUUID}').keyup(function(){
   						if ($j('#searchBox_${model.portletUUID}').val().length > 2){
   							//verify radio buttons are selected:
   							if ($j("input[name='programs_${model.portletUUID}']:checked") == null || $j("input[name='programs_${model.portletUUID}']:checked").length == 0){
   				   		    	alert("You must choose a program.");
   				   		  		return;
   				   		    }
   							if ($j("input[name='restrictByProgram']:checked") == null || $j("input[name='restrictByProgram']:checked").length == 0){
   				   		    	alert("Please select a 'restrict search' option.");
   				   		  		return;
   				   		    }
	   						HtmlFormFlowhseetFindPatient.findPatients($j('#searchBox_${model.portletUUID}').val(), $j("input[name='restrictByProgram']:checked").val(),$j("input[name='programs_${model.portletUUID}']:checked").val() ,false, function(ret){
	   						from = 0; 
	   						to = jumps-1; 
							if (ret.length <= to)
								to = ret.length -1;
	   						savedRet = ret; 
	   						retSize = ret.length;
	   						drawTable(savedRet);});
   						}
   						else {
   	   						$j('#results_${model.portletUUID}').hide();
   						}
   			});	
 		});
	
	function addRowEventsFindPatient(){
		var tbody = document.getElementById('resTableBody');
		var trs = tbody.getElementsByTagName("tr");
		for(i = 0; i < trs.length; i++){
			if (trs[i].firstChild.firstChild.innerHTML != ""){
				trs[i].onclick = function() {
					selectPatient(this.firstChild.firstChild.innerHTML);
				}
				trs[i].onmouseover = function() {
					 mouseOver(this);
				}
				trs[i].onmouseout = function() {
					$j('table.resTable tbody tr:odd').addClass('oddRow');
  					$j('table.resTable tbody tr:even').addClass('evenRow');
					 mouseOut(this);
					 $j('table.resTable tbody tr:odd').addClass('oddRow');
  					$j('table.resTable tbody tr:even').addClass('evenRow');
				}
			}	
		}
	}
	
	function selectPatient(input){
		<c:choose>
			<c:when test="${not empty model.callback}">
				${model.callback}(mappedRet[input]);
				$j('#results_${model.portletUUID}').css('display','none');
	   			$j('#searchBox_${model.portletUUID}').val('');
	   		</c:when>
	   		<c:otherwise>

	   		    if ($j("input[name='programs_${model.portletUUID}']:checked") == null || $j("input[name='programs_${model.portletUUID}']:checked").length == 0){
	   		    	alert("You must choose a program.");
	   		  		return;
	   		    } else {
	   		    	//get the configuration from the map and build url and then send
	   		    	var p = $j("input[name='programs_${model.portletUUID}']:checked").val();
	   		    	if (programLinkMap[p] == null){
	   		    		alert("invalid programID.  Please tell your local administrator to fix the global property htmlformflowsheet.programConfigurationMap");
	   		    		return;	
	   		    	}	
	   		    	window.location='${pageContext.request.contextPath}/module/htmlformflowsheet/patientWidgetChart.list?patientId=' + input + "&" + programLinkMap[p];
	   		    }
	   		    
	   		</c:otherwise>
	   	</c:choose>
	}
	function mouseOver(input){
		classTmp = this.className;
		input.className="rowMouseOver";
		refresh(this);	
	}
	function mouseOut(input){
		input.className = classTmp;
		refresh(this);
	}
	function refresh(input){
		if (input.className) input.className = input.className;
	}
	function formatDate(ymd) {
		if (ymd == null || ymd == '')
			return '';
		<c:choose>
			<c:when test="${model.locale == 'fr' || model.locale == 'en_GB'}">
				return ymd.substring(8, 10) + '/' + ymd.substring(5, 7) + '/' + ymd.substring(0, 4);
			</c:when>
			<c:otherwise>
				return ymd.substring(5, 7) + '/' + ymd.substring(8, 10) + '/' + ymd.substring(0, 4);
			</c:otherwise>
		</c:choose>
	}

	function getDateString(d) {
		var str = '';
		if (d != null) {
			
			// get the month, day, year values
			var month = "";
			var day = "";
			var year = "";
			var date = d.getDate();
			
			if (date < 10)
				day += "0";
			day += date;
			var m = d.getMonth() + 1;
			if (m < 10)
				month += "0";
			month += m;
			if (d.getYear() < 1900)
				year = (d.getYear() + 1900);
			else
				year = d.getYear();
		
			var datePattern = '<openmrs:datePattern />';
			var sep = datePattern.substr(2,1);
			var datePatternStart = datePattern.substr(0,1).toLowerCase();
			
			if (datePatternStart == 'm') /* M-D-Y */
				str = month + sep + day + sep + year
			else if (datePatternStart == 'y') /* Y-M-D */
				str = year + sep + month + sep + day
			else /* (datePatternStart == 'd') D-M-Y */
				str = day + sep + month + sep + year
			
		}
		return str;
	}
	
	
	function drawTable(ret){
   						DWRUtil.removeAllRows('resTableBody');
   						mappedRet = new Array();
   						var count = from+1;
   						var cellFuncs = [
   								// the cell counter
								function(patient) {
									if (patient.patientId != null && patient.patientId != "NaN"){
									var patientIdDiv='<div style="display:none" id="patientIdDiv">'+patient.patientId+'</div>';
									mappedRet[patient.patientId]=patient;
									return patientIdDiv + count++;
									}
								},
								function(patient) {
									if (patient.patientId != null && patient.patientId != "NaN")
										return patient.identifier;
								},
								//first name 
								function(patient) { 
										if (patient.patientId != null && patient.patientId != "NaN"){
										return patient.givenName;
										} else {
											return patient;
								 	      }
							  		},
							  	//middle name
								function(patient) { 
										if (patient.patientId != null && patient.patientId != "NaN")
										return  patient.middleName;
							  		},
							  	//family name
							  	function(patient) { 
										if (patient.patientId != null && patient.patientId != "NaN")
										return  patient.familyName;
									
							  		},
							  	function(patient) {
									if (patient.patientId != null && patient.patientId != "NaN")
										return patient.age;
								},
							  	function(patient) {
									if (patient.patientId != null && patient.patientId != "NaN")
										return patient.gender;
								},
								<c:if test ="${!empty useTribe}">
								<c:if test ="${useTribe}">
								function(patient) {
									if (patient.patientId != null && patient.patientId != "NaN"){
										if (patient.tribe != "" && patient.tribe != "Unknown")
										return patient.tribe;
									} 	
								},
								</c:if>
								</c:if>
								<c:if test ="${!empty useMothersName}">
								<c:if test ="${useMothersName}">
								function(patient) {
									var mnString = "test";
									if (patient.patientId != null && patient.patientId != "NaN"){
										if (patient.attributes != null){
												mnString = patient.attributes['Mother\'s Name'];
										}
									} 
									return mnString;	
								},
								</c:if>
								</c:if>
								function(patient) {
									if (patient.patientId != null && patient.patientId != "NaN"){
										if (patient.birthdateEstimated != "" && patient.birthdateEstimated != "Unknown"){
											if (patient.birthdateEstimated)
											return "\~";
										}
									}	
								},
								function(patient) {
									if (patient.patientId != null && patient.patientId != "NaN"){
										if (patient.birthdate != "" && patient.birthdate != "Unknown")
										return getDateString(patient.birthdate);
									} 	
								}
								<c:if test ="${!empty useHealthCenter}">
								<c:if test ="${useHealthCenter}">
								, function(patient) {
									var hcString = " ";
									if (patient.patientId != null && patient.patientId != "NaN"){
										if (patient.attributes != null){
												hcString = patient.attributes['Health Center'];
										}
									} 
									return hcString;	
								}
								</c:if>
								</c:if>
								];
							var cellFuncsHeader = [
							function() {return " "},
							function() {return '<b><spring:message code="htmlformflowsheet.Identifier"/></b>'},
							function() {return '<b><spring:message code="PersonName.givenName"/></b>'},
							function() {return '<b><spring:message code="PersonName.middleName"/></b>'},
							function() {return '<b><spring:message code="PersonName.familyName"/></b>'},
							function() {return '<b><spring:message code="Person.age"/></b>'},
							function() {return '<b><spring:message code="Person.gender"/></b>'},
							<c:if test ="${!empty useTribe}">
								<c:if test ="${useTribe}">
									function() {return '<b><spring:message code="htmlformflowsheet.tribe"/></b>'},
								</c:if>
							</c:if>	
							<c:if test ="${!empty useMothersName}">
								<c:if test ="${useMothersName}">
									function() {return '<b><spring:message code="htmlformflowsheet.mothersname"/></b>'},
								</c:if>
							</c:if>	
							function() {return " "},
							function() {return '<b><spring:message code="Person.birthdate"/></b>'}
							
							<c:if test ="${!empty useHealthCenter}">
								<c:if test ="${useHealthCenter}">		
									,function() {return '<b><spring:message code="htmlformflowsheet.healthcenter"/></b>'}
								</c:if>
							</c:if>
							];
							
							var cellFuncsNextN = [function(){return "t"}];	
							
							var formatCountCell = function(options) { 
								var td = document.createElement("td"); 
								td.setAttribute('colSpan','10');
								td.setAttribute('align', 'center');
								return td; 
							}
							
								if (ret[from]){
   								if (ret.length != retSize){
   										DWRUtil.removeAllRows('resTableHeader');
   										if (headerShown == 1)
   										headerShown--;
   										retSize = ret.length;
   									}
   								}
								if (headerShown == 0){
									DWRUtil.addRows('resTableHeader',[""], cellFuncsNextN, {cellCreator:formatCountCell,escapeHtml:false});
									DWRUtil.addRows('resTableHeader',[""], cellFuncsHeader, {escapeHtml:false});
									headerShown ++;
								}
								if (!ret[from]){
   										DWRUtil.removeAllRows('resTableHeader');
   										headerShown--;
   										if (ret.length == 0){
   								 		//no records found:
   								 		var cellFucsNoRecords = [function(){
   								 		var sb = document.getElementById("searchBox_${model.portletUUID}");
   								 		var searchText = sb.value;
   								 		return "<div><Br>&nbsp;&nbsp;&nbsp;<spring:message code="htmlformflowsheet.nopatientsfound"/> <i>"+searchText+"</i>.</div>";
   								 		}];
   								 		DWRUtil.addRows('resTableBody', ["nopatient"], cellFucsNoRecords,  {escapeHtml:false} );
   								 		
   								 		}
   								 		
   								}  else {
								DWRUtil.addRows('resTableBody', getPartOfSavedRet(from, to, ret), cellFuncs,  {escapeHtml:false} );
   								 $j('table.resTable tbody tr:odd').addClass('oddRow');
  								 $j('table.resTable tbody tr:even').addClass('evenRow');
   								 $j('table.resTable tbody tr').attr('onmouseover','javascript:mouseOver(this);refresh(this);');
   								 $j('table.resTable tbody tr').attr('onmouseout','javascript:mouseOut(this); refresh(this);');
   								 addRowEventsFindPatient();
   								 fixHeader();
   								 $j('#results_${model.portletUUID}').css('display','');		
   								} 				
   				}
   				
   	function fixHeader(){
   		var thead = document.getElementById('resTableHeader');
		var trs = thead.getElementsByTagName("tr");
		addClass(trs[0],"infoRow");
		addClass(trs[1],"oddRow");
		if (trs[0]){
			var toTmp = to;
			if (toTmp > savedRet.length)
			toTmp = savedRet.length -1;
			toTmp++;
			
			td = trs[0].getElementsByTagName('td')[0];
			var fromTmp = from + 1;
			var nextN = "&nbsp;&nbsp;&nbsp;" + fromTmp + " <spring:message code="htmlformflowsheet.to"/> " + toTmp +" <spring:message code="htmlformflowsheet.of"/> " + savedRet.length;
			
									var lastP = jumps;
									if (savedRet.length > toTmp){
										if (savedRet.length - toTmp < lastP)
										lastP = savedRet.length - toTmp;
									}
									var pipeTest = 0;
									if (savedRet.length > jumps && toTmp != savedRet.length){
									nextN = "<a href='javascript:next()'><spring:message code="htmlformflowsheet.next"/> " + lastP + "</a>&nbsp;&nbsp;"+nextN;
									pipeTest = 1;
									}
									if (fromTmp > 1){
									if (pipeTest == 1)
									nextN = "<a href='javascript:previous()'><spring:message code="htmlformflowsheet.previous"/> "+jumps+"</a>&nbsp;|&nbsp;" + nextN;
									else
									nextN = "<a href='javascript:previous()'><spring:message code="htmlformflowsheet.previous"/> "+jumps+"</a>&nbsp;&nbsp;" + nextN;
									}
			td.innerHTML = nextN;
		}
   	}			
	function next(){
		from = from + jumps;
		if (to + jumps > savedRet.length - 1)
			to = savedRet.length - 1;
		else	
		to = to + jumps;
		drawTable(savedRet);
	}
	
	function previous(){
		to = from - 1;
		from = from - jumps;
		if (to - from != jumps)
			from = to - jumps+1;
		if (to < jumps)
			to = jumps - 1;
		if (from < 0)
			from = 0;			
		drawTable(savedRet);
	}
	function getPartOfSavedRet(from, to, ret){
		var retTmp = new Array();
		if (ret[from]){
			toTmp = to;
			if (toTmp > ret.length)
				toTmp = ret.length -1;
			var c = 0;
			for (i = from; i <= toTmp; i++){
				retTmp[c] = ret[i];
				c++;
			}
		}
		return retTmp;
	}
		
	
function useMdrtbLoadingMessage(message) {
  var loadingMessage;
  if (message) loadingMessage = message;
  else loadingMessage = "Loading...";

 DWREngine.setPreHook(function() {
 	showLoading = 1;
 	setTimeout('function sitStill(){return "";}', 5000);
    var disabledZone = $('disabledZone');
    if (showLoading == 1){
    if (!disabledZone) {
      disabledZone = document.createElement('div');
      disabledZone.setAttribute('id', 'disabledZone');
      document.body.appendChild(disabledZone);
      var messageZone = document.createElement('div');
      messageZone.setAttribute('id', 'messageZone');
      disabledZone.appendChild(messageZone);
      var text = document.createTextNode(loadingMessage);
      messageZone.appendChild(text);
    }
    else {
      $('messageZone').innerHTML = loadingMessage;
      disabledZone.style.visibility = 'visible';
    }
    }
  });

  DWREngine.setPostHook(function() {
    $('disabledZone').style.visibility = 'hidden';
    showLoading = 0;
  });
}
	</script>


<openmrs:require privilege="View Patients" otherwise="/login.htm" redirect="/index.htm"/>
<c:if test="${model.authenticatedUser != null}">

	<c:choose>
		<c:when test="${model.size=='mini'}">
			<span id="findPatient">
				<c:choose>
					<c:when test="${!empty model.labelCode}"><span style="font-weight:bold"><spring:message code="${model.labelCode}"/></span></c:when>
					<c:otherwise><span style="font-weight:bold"><spring:message code="Patient.find"/></span></c:otherwise>
				</c:choose>
				<input type="text" value="" id="searchBox_${model.portletUUID}" name="searchBox_${model.portletUUID}">
				<div id="results_${model.portletUUID}" style="position:absolute; z-index:1000; border:2px solid black; background-color:#CCCCCC; ${model.resultStyle}">
					<table id="resTable" class="resTable" cellpadding="2" cellspacing="0" style="border-collapse: collapse">
						<thead id="resTableHeader" class="resTableHeader"/>	
						<tbody class="resTableBody" id="resTableBody" style="vertical-align: center"/>
						<tfoot id="resTableFooter" class="resTableFooter"/>	
					</table>
				</div>
			</span>
		</c:when>
		<c:otherwise>
			<div id="findPatient">
				
				<b class="boxHeader"><spring:message code="Patient.find" /></b>
				<div class="box" style="padding: 15px 15px 15px 15px;">
				   <table>
				    <tr><td>1.  <spring:message code="Program.header"/> </td>
				        <td> 
							
							   <c:forEach var="program" items="${model.program}">
							          <input type="radio" name="programs_${model.portletUUID}" value="${program}" autocomplete='off'/>
							          <htmlformflowsheet:programName program="${program}"/>&nbsp;&nbsp;
							   </c:forEach>
						  </td> 
					</tr>
					<tr><td>2. <spring:message code="htmlformflowsheet.restrictByProgram"/></td>
					    <td>
								  <input type="radio" name="restrictByProgram" value="false"/> <spring:message code="general.no" />
								  <input type="radio" name="restrictByProgram" value="true"/> <spring:message code="general.yes" />
						</td>
					</tr>		  
				    <tr><td>3.
				     <c:choose>
							<c:when test="${!empty model.labelCode}"><spring:message code="${model.labelCode}"/></c:when>
							<c:otherwise><spring:message code="Patient.find"/></c:otherwise>
						</c:choose>
				    </td> 
					<td/>
					</tr>	
					</table>
					<input type="text" value="" id="searchBox_${model.portletUUID}" name="searchBox_${model.portletUUID}" style="width:50%;">   &nbsp;&nbsp;<br>

					<div id="results_${model.portletUUID}">
						<table id="resTable" class="resTable" cellpadding="2" cellspacing="0" style="border-collapse: collapse">
							<thead id="resTableHeader" class="resTableHeader"/>	
							<tbody class="resTableBody" id="resTableBody" style="vertical-align: center"/>
							<tfoot id="resTableFooter" class="resTableFooter"/>	
						</table>
					</div>
				</div>
			</div>
		</c:otherwise>
	</c:choose>

	<script>
	 function init() {
	 useMdrtbLoadingMessage('<spring:message code="htmlformflowsheet.loadingmessage"/>');
	 }
	 window.onload=init;
	</script>
	 <br/>
	
	<!-- CREATE PATIENT -->
	<openmrs:globalProperty key="htmlformflowsheet.programConfigurationMap" var="createPatientMapString" />
	
	<c:if test="${!empty model.createPatientFormId && !empty createPatientMapString}">
				
				<div>
				    <b class="boxHeader"><spring:message code="Patient.create" /></b>
					<div class="box" style="padding: 15px 15px 15px 15px;">
						<c:forEach var="program" items="${model.program}">
								<input type="radio" name="createPatientProgram_${model.portletUUID}" value="${program}" autocomplete='off'/>
							    <htmlformflowsheet:programName program="${program}"/><br/>
						</c:forEach>
						<!--  you can add as many radio buttons here as you want... --->
						<br />
						<a href="#" onClick="buildURL();"><spring:message code="Patient.create" /></a>
					</div>
				</div>
				
				<script>
					var createPatientLinkMap=${createPatientMapString};
					
					var createPatientHtmlFormId=${model.createPatientFormId};
					function buildURL(){
						if ($j("input[name='createPatientProgram_${model.portletUUID}']:checked") == null || $j("input[name='createPatientProgram_${model.portletUUID}']:checked").length == 0){
					    	alert("You must choose a program.");
					  		return;
					    } else {
					    	//get the configuration from the map and build url and then send
					    	var p = $j("input[name='createPatientProgram_${model.portletUUID}']:checked").val();
					    	if (createPatientLinkMap[p] == null){
					    		alert("invalid programID.  Please check your Global Property!");
					    		return;	
					    	}	
					    	window.location='${pageContext.request.contextPath}/module/htmlformentry/htmlFormEntry.form?formId=' + createPatientHtmlFormId + '&mode=enter&returnUrl=${pageContext.request.contextPath}/module/htmlformflowsheet/patientWidgetChart.list?' + createPatientLinkMap[p];
					    }
					}
					
				</script>
	</c:if>
	
	<c:if test="${empty model.createPatientFormId}">
		<br><span style="color:red;">Cannot show create patient box.  Missing the createPatientFormId in the portlet parameters.</span> <br>
	</c:if>
	<c:if test="${empty createPatientMapString}">
	    <br><span style="color:red;">Cannot show create patient box.  Global property htmlformflowsheet.programConfigurationMap is empty. </span> <br>
	</c:if>


</c:if>










