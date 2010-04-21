<%@ include file="/WEB-INF/template/include.jsp" %>

<c:if test="${model.fullPage == 'true'}">
	<%@ include file="/WEB-INF/template/header.jsp" %>
</c:if>
<c:if test="${model.fullPage == 'false'}">
	<script>
		var addEvent = false;
	</script>
</c:if>

<openmrs:htmlInclude file="/scripts/jquery/jquery-1.3.2.min.js" />
<openmrs:htmlInclude file="/scripts/jquery-ui/js/jquery-ui-1.7.2.custom.min.js" />
<openmrs:htmlInclude file="/scripts/dojoConfig.js"/>
<openmrs:htmlInclude file="/scripts/dojo/dojo.js"/>
<openmrs:htmlInclude file="/moduleResources/htmlformflowsheet/encounterChart.js" />
<openmrs:htmlInclude file="/dwr/engine.js" />
<openmrs:htmlInclude file="/dwr/util.js" />
<openmrs:htmlInclude file="/dwr/interface/HtmlFlowsheetDWR.js" />
<openmrs:htmlInclude file="/moduleResources/htmlformflowsheet/smoothness.css" />


<script type="text/javascript">
	var $j = jQuery.noConflict();
	$j(document).ready(function() {
		$j('#patientChartTabs').tabs();
		$j('#patientChartTabs').tabs('select', ${model.selectTab});
	});
</script>
<br/>
<h4>

	<c:if test="${model.fullPage == 'true'}">
		<span style="font-size:150%;">
		${model.patient.personName} | 
		${model.patient.patientIdentifier.identifier}
		</span>
	</c:if>
	<span style="<c:if test="${model.fullPage == 'true'}">position:absolute; right:10px; font-size:90%;</c:if>">
		<c:forEach var="link" items="${model.links}" varStatus="status">
			<a href="${pageContext.request.contextPath}${link.value}&patientId=${model.patientId}">${link.key}</a> 
		</c:forEach>
	</span>
	
</h4>
<br/>
<div id="patientChartTabs" style="font-size:80%;<c:if test="${model.fullPage == 'false'}">position:absolute;</c:if>">
	<ul>
		<c:forEach var="tab" items="${model.tabs}" varStatus="status">
			<li><a href="#tabContent${status.count}"><span>${tab.title}</span></a></li>
		</c:forEach>
	</ul>
	<c:forEach var="tab" items="${model.tabs}" varStatus="status">
		<div id="tabContent${status.count}">
			<c:choose>
				<c:when test="${tab.class.simpleName == 'EncounterChartPatientChartTab'}">

					<openmrs:portlet
						id="encounterChart${status.count}"
						moduleId="htmlformflowsheet"
						url="encounterChart"
						patientId="${model.patientId}"
						parameters="encounterTypeId=${tab.encounterTypeId}|view=${status.index}|formId=${tab.formId}|showAddAnother=${tab.showAddAnother}"
					/>

				</c:when>
				<c:when test="${tab.class.simpleName == 'SingleHtmlFormPatientChartTab'}">
				
					<openmrs:portlet
						id="singleForm${status.count}" 
						url="singleHtmlForm"
						moduleId="htmlformflowsheet"
						patientId="${model.patientId}"
						parameters="formId=${tab.formId}|which=${tab.which}|view=${status.index}|defaultEncounterTypeId=${tab.defaultEncounterTypeId}"
					/>
				
				</c:when>
				<c:otherwise>

					<span class="error">Configuration error: don't know how to handle tab class ${tab.class.name}</span>

				</c:otherwise>
			</c:choose>
		</div>
	</c:forEach>
</div>
<c:if test="${model.fullPage == 'true'}">
	<%@ include file="/WEB-INF/template/footer.jsp" %>
</c:if>