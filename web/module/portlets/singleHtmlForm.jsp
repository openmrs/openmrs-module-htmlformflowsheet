<%@ include file="/WEB-INF/template/include.jsp" %>
<%@ taglib prefix="htmlformflowsheet" uri="/WEB-INF/view/module/htmlformflowsheet/taglib/htmlformflowsheet.tld" %>

<!--<htmlformflowsheet:htmlInclude file="/scripts/jquery/jquery-1.3.2.min.js" />
<htmlformflowsheet:htmlInclude file="/scripts/jquery-ui/js/jquery-ui-1.7.2.custom.min.js" />
<htmlformflowsheet:htmlInclude file="/moduleResources/htmlformflowsheet/encounterChart.js" />-->

<htmlformflowsheet:htmlInclude file="/moduleResources/htmlformflowsheet/urlTools.js" />

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
		//does the original load...
		<c:if test="${!empty model.encounterToDisplay.encounterId}">
		    $j('#singleFormContentIframe_${model.portletUUID}').attr("src", "${pageContext.request.contextPath}/module/htmlformentry/htmlFormEntry.form?inPopup=true&encounterId=${model.encounterToDisplay.encounterId}");
			$j('#editTag_${model.portletUUID}').html("<a href='#' onClick='showSingleEntryPopupForEdit${model.portletUUID}(\"${model.portletUUID}\", ${model.personId}, ${model.formId}, ${model.view}, ${model.encounterToDisplay.encounterId})'>edit</a>");
		</c:if>	
	
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
		loadUrlIntoSingleFormPopup${model.portletUUID}(uuid, '', openmrsContextPath + '/module/htmlformentry/htmlFormEntry.form?inPopup=true&encounterId=' + encounterId + '&mode=EDIT&closeAfterSubmission=closeEncounterChartPopup' + uuid, true, tabIndex, personId,  formId, encounterId);
	}
	 
	
	function showSingleEntryPopup${model.portletUUID}(uuid, personId, formId, tabIndex) {
		loadUrlIntoSingleFormPopup${model.portletUUID}(uuid, '', openmrsContextPath + '/module/htmlformentry/htmlFormEntry.form?inPopup=true&personId=' + personId + '&formId=' + formId + '&closeAfterSubmission=closeEncounterChartPopup' + uuid, true, tabIndex, personId,  formId, '');
	} 
	
	function loadUrlIntoSingleFormPopup${model.portletUUID}(uuid, title, url, reloadOnClose, tabIndex, personId,  formId, encounterId) {
		var elem = $j('#encounterChartPopup' + uuid);
		var iframe = $j("#encounterChartIFrame" + uuid);
		
		$j(iframe).empty();
		elem.dialog('option', 'title', title)
			.dialog('option', 'height', $j(window).height() - 50);
		if (reloadOnClose) {	
			//elem.dialog('option', 'close', function(event, ui) { window.location = jQuery.queryString(window.location.href, 'selectTab=' + tabIndex); });
				elem.dialog('option', 'close', function(event, ui) { 
			    //$j('#htmlForm_' + uuid).html("loading...");
			    
			    if (encounterId == ''){
				    HtmlFlowsheetDWR.getNewEncounterId('${model.which}', formId, personId,function(ret){
										if (ret != 0){
											$j('#editTag_${model.portletUUID}').html("<a href='#' onClick='showSingleEntryPopupForEdit${model.portletUUID}(\"${model.portletUUID}\", ${model.personId}, " + formId + ", ${model.view},  " + ret + ")'>edit</a>");
											//$j('#htmlForm_${model.portletUUID}').load(openmrsContextPath + "/module/htmlformentry/htmlFormEntry.form?encounterId=" + ret +"&inPopup=true");
										    $j('#singleFormContentIframe_${model.portletUUID}').attr("src", "${pageContext.request.contextPath}/module/htmlformentry/htmlFormEntry.form?inPopup=true&encounterId=" + ret);
											$j('#fillOutFormDiv_${model.portletUUID}').html('');
										}
									});
				} else {
					//$j('#htmlForm_${model.portletUUID}').load("${pageContext.request.contextPath}/module/htmlformentry/htmlFormEntry.form?encounterId=" + encounterId + "&inPopup=true");
					$j('#singleFormContentIframe_${model.portletUUID}').attr("src", "${pageContext.request.contextPath}/module/htmlformentry/htmlFormEntry.form?inPopup=true&encounterId=${model.encounterToDisplay.encounterId}");
				}
				
			});
			$j('.ui-dialog-titlebar-close').mousedown(function(){
				elem.dialog('option', 'close', function(){iframe.empty();});
			});
		} 
		elem.dialog('open');
		$j(iframe).attr("src", url);
	}
	
	
	//this creates an iframe area that is exactly the size of the visible screen above the footer bar
	function onloadIframe(uuid){
 			var iframe = document.getElementById('singleFormContentIframe_' + uuid);
 			var relheight = $j(iframe).offset().top; //161
 			var footer = $j('#footer');
 			var winHeight = $j(window).height();
 			iframe.height=(winHeight - (relheight + 20));
	}
	
	
</script>

<%--
--%>

<div id="editTag_${model.portletUUID}" style="font-size:90%;position:relative;width:100%;text-align:right;"></div>
<div id="htmlForm_${model.portletUUID}" style="font-size:90%; bottom: 0px;">
	<c:if test="${model.encounterToDisplay != null}">
			<iframe id="singleFormContentIframe_${model.portletUUID}" width="100%" height="100%" marginWidth="0" marginHeight="0" frameBorder="0" scrolling="auto"><span>loading...</span></iframe>
	</c:if>
	<c:if test="${model.encounterToDisplay == null}">	
	 		<div id="fillOutFormDiv_${model.portletUUID}"><a href="#" onClick="showSingleEntryPopup${model.portletUUID}('${model.portletUUID}', ${model.personId}, ${model.formId}, ${model.view})">fill out form</a></div>
	 		<iframe id="singleFormContentIframe_${model.portletUUID}" width="100%" height="100%" marginWidth="0" marginHeight="0" frameBorder="0" scrolling="auto"></iframe>
	</c:if>
</div>
<div id="encounterChartPopup${model.portletUUID}">
	<iframe id="encounterChartIFrame${model.portletUUID}" width="100%" height="100%" marginWidth="0" marginHeight="0" frameBorder="0" scrolling="auto" onload="onloadIframe('${model.portletUUID}');"></iframe>
</div>
