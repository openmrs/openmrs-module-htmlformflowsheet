<%@ include file="/WEB-INF/template/include.jsp" %>
<%@ include file="/WEB-INF/template/header.jsp" %>

<hr/>
This is the portlet for all encounters
<hr/>
<openmrs:portlet moduleId="patientchartwidgets" url="encounterChart" patientId="${command.patientId}" parameters="encounterTypeId=*|formId=18"/>

<hr/>
This is the portlet for encounter type 1
<hr/>
<openmrs:portlet moduleId="patientchartwidgets" url="encounterChart" patientId="${command.patientId}" parameters="encounterTypeId=1|conceptsToShow=5089,5497|formId=18"/>

<%@ include file="/WEB-INF/template/footer.jsp" %>