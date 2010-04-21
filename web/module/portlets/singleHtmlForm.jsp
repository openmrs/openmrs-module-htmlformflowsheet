<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:htmlInclude file="/scripts/jquery/jquery-1.3.2.min.js" />
<openmrs:htmlInclude file="/scripts/jquery-ui/js/jquery-ui-1.7.2.custom.min.js" />
<openmrs:htmlInclude file="/scripts/calendar/calendar.js" />
<openmrs:htmlInclude file="/moduleResources/htmlformflowsheet/urlTools.js" />
<openmrs:htmlInclude file="/moduleResources/htmlformflowsheet/encounterChart.js" />


<script type="text/javascript">
	var $j = jQuery.noConflict();
	$j(document).ready(function() {
		$j('#encounterChartPopup${model.portletUUID}').dialog({
				title: 'TODO: put encounter details here dynamically',
				autoOpen: false,
				draggable: false,
				resizable: false,
				width: '95%',
				modal: true
		});
	});

	function closeEncounterChartPopup${model.portletUUID}() {	 
		//$j("#encounterChartPopup${model.portletUUID}").dialog('close');
			$j(".ui-widget-content").dialog('close');
	}
	
	function voidEncounter${model.portletUUID}(uuid, id, retVal){
		if (retVal == true){
			HtmlFlowsheetDWR.voidEncounter(id ,function(ret){
								if (!ret)
									alert('<spring:message code="htmlformflowsheet.cantdeleteencounter" />');
								else
									$j('#encounterWidget_' + uuid).load( openmrsContextPath + "/module/htmlformflowsheet/encounterChartContent.list?patientId=${model.personId}&portletUUID=" + uuid +"&encounterTypeId=${model.encounterTypeId}&view=${model.view}&formId=${model.formId}&count=${model.view + 1}"); 
							});
			
		}
	}
	
	function editHTMLForm${model.portletUUID}(encId,view){
		window.location = '${pageContext.request.contextPath}/module/htmlformentry/htmlFormEntry.form?encounterId=' + encId + '&mode=EDIT&returnUrl=${pageContext.request.contextPath}/module/htmlformflowsheet/testChart.list?selectTab=' + view;
	}

	function showSingleEntryPopupForEdit${model.portletUUID}(uuid, personId, formId, tabIndex, encounterId) {
		loadUrlIntoSingleFormPopup${model.portletUUID}(uuid, '', openmrsContextPath + '/module/htmlformentry/htmlFormEntry.form?encounterId=' + encounterId + '&mode=EDIT&closeAfterSubmission=closeEncounterChartPopup' + uuid, true, tabIndex, personId,  formId, encounterId);
	}
	
	
	function showSingleEntryPopup${model.portletUUID}(uuid, personId, formId, tabIndex) {
		loadUrlIntoSingleFormPopup${model.portletUUID}(uuid, '', openmrsContextPath + '/module/htmlformentry/htmlFormEntry.form?inPopup=true&personId=' + personId + '&formId=' + formId + '&closeAfterSubmission=closeEncounterChartPopup' + uuid, true, tabIndex, personId,  formId, '');
	}
	
	function loadUrlIntoSingleFormPopup${model.portletUUID}(uuid, title, url, reloadOnClose, tabIndex, personId,  formId, encounterId) {
		var elem = $j('#encounterChartPopup' + uuid);
		var iframe = $j("#encounterChartIFrame" + uuid);
		
		iframe.empty();
		elem.dialog('option', 'title', title)
			.dialog('option', 'height', $j(window).height() - 50);
		if (reloadOnClose) {	
			//elem.dialog('option', 'close', function(event, ui) { window.location = jQuery.queryString(window.location.href, 'selectTab=' + tabIndex); });
				elem.dialog('option', 'close', function(event, ui) { 
			    $j('#htmlForm_' + uuid).html("loading...");
			    
			    if (encounterId == ''){
				    HtmlFlowsheetDWR.getNewEncounterId('${model.which}', formId, personId,function(ret){
										if (ret != 0)
											$j('#editTag_${model.portletUUID}').html("<a href='#' onClick='showSingleEntryPopupForEdit${model.portletUUID}(\"${model.portletUUID}\", ${model.personId}, " + formId + ", ${model.view},  " + ret + ")'>edit</a>");
											$j('#htmlForm_${model.portletUUID}').load(openmrsContextPath + "/module/htmlformentry/htmlFormEntry.form?encounterId=" + ret +"&inPopup=true");
										});
				} else {
					$j('#htmlForm_${model.portletUUID}').load("${pageContext.request.contextPath}/module/htmlformentry/htmlFormEntry.form?encounterId=" + encounterId + "&inPopup=true");
				}
				
			});
			$j('.ui-dialog-titlebar-close').mousedown(function(){
				elem.dialog('option', 'close', function(){iframe.empty();});
			});
		} 
		elem.dialog('open');
		$j(iframe).attr("src", url);
	}
	
</script>

<%--
--%>

<div id="editTag_${model.portletUUID}" style="font-size:90%;position:relative;width:100%;text-align:right;"></div>
<c:if test="${model.encounterToDisplay != null}">
	<script type="text/javascript">
		var $j = jQuery.noConflict();
		$j(document).ready(function() {
			<c:if test="${model.encounterToDisplay.encounterId != null}">
				$j('#htmlForm_${model.portletUUID}').load("${pageContext.request.contextPath}/module/htmlformentry/htmlFormEntry.form?encounterId=${model.encounterToDisplay.encounterId}&inPopup=true");
				$j('#editTag_${model.portletUUID}').html("<a href='#' onClick='showSingleEntryPopupForEdit${model.portletUUID}(\"${model.portletUUID}\", ${model.personId}, ${model.formId}, ${model.view}, ${model.encounterToDisplay.encounterId})'>edit</a>");
			</c:if>
		});
	</script>
	<div id="htmlForm_${model.portletUUID}" style="font-size:90%;">
			<c:if test="${model.encounterToDisplay.encounterId != null}">
				<span>loading...</span>	
			</c:if>
		
	</div>
	
</c:if>
<c:if test="${model.encounterToDisplay == null}">
 	<div id="htmlForm_${model.portletUUID}" style="font-size:90%;">
 		<a href="#" onClick="showSingleEntryPopup${model.portletUUID}('${model.portletUUID}', ${model.personId}, ${model.formId}, ${model.view})">fill out form</a>
 	</div>
</c:if>

<div id="encounterChartPopup${model.portletUUID}">
	<iframe id="encounterChartIFrame${model.portletUUID}" width="100%" height="100%" marginWidth="0" marginHeight="0" frameBorder="0" scrolling="auto"></iframe>
</div>
