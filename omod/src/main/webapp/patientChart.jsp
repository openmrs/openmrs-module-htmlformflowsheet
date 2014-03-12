<%@ include file="/WEB-INF/template/include.jsp" %>
<%@ taglib prefix="htmlformflowsheet" uri="/WEB-INF/view/module/htmlformflowsheet/taglib/htmlformflowsheet.tld" %>

<c:if test="${model.fullPage == 'true'}">
	<%@ include file="/WEB-INF/template/header.jsp" %>
</c:if>
<c:if test="${model.fullPage == 'false'}">
	<%@ include file="/WEB-INF/view/module/htmlformflowsheet/headerMinimal.jsp" %>
	<script>
		var addEvent = false;
	</script>
</c:if>

<htmlformflowsheet:htmlInclude file="/scripts/jquery/jquery-1.3.2.min.js" />
<htmlformflowsheet:htmlInclude file="/scripts/jquery-ui/js/jquery-ui-1.7.2.custom.min.js" />
<htmlformflowsheet:htmlInclude file="/moduleResources/htmlformflowsheet/encounterChart.js" />
<htmlformflowsheet:htmlInclude file="/dwr/engine.js" />
<htmlformflowsheet:htmlInclude file="/dwr/util.js" />
<htmlformflowsheet:htmlInclude file="/dwr/interface/HtmlFlowsheetDWR.js" />
<htmlformflowsheet:htmlInclude file="/moduleResources/htmlformflowsheet/smoothness.css" />
<htmlformflowsheet:htmlInclude file="/scripts/calendar/calendar.js" />


<script type="text/javascript">
	var $j = jQuery.noConflict();
	$j(document).ready(function() {
		$j('#patientChartTabs').tabs();
		$j('#patientChartTabs').tabs('select', ${model.selectTab});
	});
</script>


	<c:if test="${model.fullPage == 'true'}">
		<c:if test="${model.title != ''}">
			<span style="font-size:130%;  position:relative; left:1%">${model.title}</span><br/><br/>
		</c:if>
		<!-- <span style="font-size:115%;">
			${model.patient.familyName} ${model.patient.givenName} ${model.patient.middleName} |
			 <openmrs:formatDate date="${model.patient.birthdate}"/> |  
			${model.patient.patientIdentifier.identifier} 
		</span>--> 
	    <openmrs:portlet url="patientHeader" id="patientDashboardHeader" patientId="${model.patient.patientId}"/> 
		<br/>
	</c:if>
	<span style="<c:if test="${model.fullPage != 'false'}">position:absolute;right:10px;font-size:90%;</c:if>">
		<c:forEach var="link" items="${model.links}" varStatus="status">
			<a href="${pageContext.request.contextPath}${link.value}&patientId=${model.patientId}">${link.key}</a> 
		</c:forEach>
	</span>

<div id="patientChartTabs" style="font-size:80%;<c:if test="${model.fullPage == 'false'}">position:absolute;</c:if>">
	<c:if test="${fn:length(model.tabs) > 1}">
		<ul>
			<c:forEach var="tab" items="${model.tabs}" varStatus="status">
				<li><a href="#tabContent${status.count}"><span>${tab.title}</span></a></li>
			</c:forEach>
		</ul>
	</c:if>
	<c:forEach var="tab" items="${model.tabs}" varStatus="status">
		<div id="tabContent${status.count}">
			<c:choose>
				<c:when test="${tab['class'].simpleName == 'EncounterChartPatientChartTab'}">

					<openmrs:portlet
						id="encounterChart${status.count}"
						moduleId="htmlformflowsheet"
						url="encounterChart${status.count}"
						patientId="${model.patientId}"
						parameters="encounterTypeId=${tab.encounterTypeId}|readOnly=${model.readOnly}|view=${status.index}|formId=${tab.formId}|showAddAnother=${tab.showAddAnother}|showAllEncsWithEncType=${model.showAllEncsWithEncType}|addAnotherButtonLabel=${model.addAnotherButtonLabel}|windowHeight=${model.windowHeight}|showProvider=${model.showProvider}|providerHeader=${model.providerHeader}|showHtmlFormInstead=${model.showHtmlFormInstead}|conceptsToShow=${model.conceptsToShow}"
					/>

				</c:when>
				<c:when test="${tab['class'].simpleName == 'SingleHtmlFormPatientChartTab'}">
					<openmrs:portlet
						id="singleForm${status.count}" 
						url="singleHtmlForm"
						moduleId="htmlformflowsheet"
						patientId="${model.patientId}"
						parameters="formId=${tab.formId}|which=${tab.which}|view=${status.index}|defaultEncounterTypeId=${tab.defaultEncounterTypeId}|showAllEncsWithEncType=${model.showAllEncsWithEncType}|showProvider=${model.showProvider }|providerHeader=${model.providerHeader}|conceptsToShow=${model.conceptsToShow}"
					/>
				
				</c:when>
				<c:otherwise>

					<span class="error">Configuration error: don't know how to handle tab class ${tab['class'].name}</span>

				</c:otherwise>
			</c:choose>
		</div>
	</c:forEach>
</div>
<c:if test="${model.fullPage == 'true'}">
	<%@ include file="/WEB-INF/template/footer.jsp" %>
</c:if>
<c:if test="${model.fullPage == 'false'}">
	<%@ include file="/WEB-INF/template/footerMinimal.jsp" %>
</c:if>