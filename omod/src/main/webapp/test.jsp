<%@ include file="/WEB-INF/template/include.jsp" %>
<%@ include file="/WEB-INF/template/header.jsp" %>

<hr/>
<spring:message code="htmlformflowsheet.allEncounters" />
<hr/>
<openmrs:portlet moduleId="patientchartwidgets" url="encounterChart" patientId="${command.patientId}" parameters="encounterTypeId=*|formId=18"/>

<hr/>
<spring:message code="htmlformflowsheet.encounterOne" />
<hr/>
<openmrs:portlet moduleId="patientchartwidgets" url="encounterChart" patientId="${command.patientId}" parameters="encounterTypeId=1|conceptsToShow=5089,5497|formId=18"/>

<%@ include file="/WEB-INF/template/footer.jsp" %>