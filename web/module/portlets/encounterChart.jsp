<%@ include file="/WEB-INF/template/include.jsp" %>
<%@ taglib prefix="mpcw" uri="/WEB-INF/view/module/htmlformflowsheet/taglib/htmlformflowsheet.tld" %>
<%--
Parameters:
	encounterTypeId: (int, required) tells what encounter type to show in this table
					 use the special value '*' to signify all encounters regardless of type
	conceptsToShow: (comma-separated list of concept ids) tells what concepts to show the
					obs of in the table. If not specified, then all concepts will be extracted
					from the specified form (not yet implemented)
	showAddAnother: (boolean, default = true) if 'false', there's no option for "add another"
	formId: which form to use for the "add another"
--%>

<%--
	(Internal documentation)
	passed from controller:
	* encounterListForChart: list of encounters
--%>
<openmrs:htmlInclude file="/scripts/jquery/jquery-1.3.2.min.js" />
<openmrs:htmlInclude file="/scripts/jquery-ui/js/jquery-ui-1.7.2.custom.min.js" />
<openmrs:htmlInclude file="/moduleResources/htmlformflowsheet/smoothness.css" />

<openmrs:htmlInclude file="/moduleResources/htmlformflowsheet/urlTools.js" />
	
	<div id="encounterWidget_${model.portletUUID}" style="font-size:90%;">
				<span>loading...</span>	
	</div>
	
	
	<script type="text/javascript">
		var $j = jQuery.noConflict();
		$j(document).ready(function() {
				$j('#encounterWidget_${model.portletUUID}').load("${pageContext.request.contextPath}/module/htmlformflowsheet/encounterChartContent.list?patientId=${model.patientId}&portletUUID=${model.portletUUID}&encounterTypeId=${model.encounterTypeId}&view=${model.view}&formId=${model.formId}&count=${model.view + 1}");
			
		});
	</script>
	

