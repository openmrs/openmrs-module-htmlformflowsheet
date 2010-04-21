function loadUrlIntoEncounterChartPopup(uuid, title, url, reloadOnClose, tabIndex, personId, encounterTypeId,  formId) {
	var elem = $j('#encounterChartPopup' + uuid);
	var iframe = $j("#encounterChartIFrame" + uuid);
	iframe.empty();
	elem.dialog('option', 'title', title)
		.dialog('option', 'height', $j(window).height() - 50);
	if (reloadOnClose) {	
		//elem.dialog('option', 'close', function(event, ui) { window.location = jQuery.queryString(window.location.href, 'selectTab=' + tabIndex); });
		elem.dialog('option', 'close', function(event, ui) { 
		             $j('#encounterWidget_' + uuid).html("loading...");
					 $j('#encounterWidget_' + uuid).load(openmrsContextPath + "/module/htmlformflowsheet/encounterChartContent.list?patientId=" +personId+ "&personId=" +personId+ "&portletUUID=" + uuid + "&encounterTypeId=" + encounterTypeId +"&view=" + tabIndex+ "&formId=" +formId+ "&count=" + (tabIndex+1)); 
		});
		$j('.ui-dialog-titlebar-close').mousedown(function(){
			elem.dialog('option', 'close', function(){iframe.empty();});
		});
	} 
	elem.dialog('open');
	$j(iframe).attr("src", url);
	
}

function showEncounterPopup(uuid, encId) {
	loadUrlIntoEncounterChartPopup(uuid, '', openmrsContextPath + '/module/htmlformentry/htmlFormEntry.form?inPopup=true&encounterId=' + encId, false, "", "", "", "");
}
function showEncounterEditPopup(uuid, encId, personId, formId, tabIndex, encounterTypeId) {
	loadUrlIntoEncounterChartPopup(uuid, '', openmrsContextPath + '/module/htmlformentry/htmlFormEntry.form?inPopup=true&encounterId=' + encId + "&mode=EDIT&closeAfterSubmission=closeEncounterChartPopup" + uuid, true, tabIndex, personId, encounterTypeId,  formId);
}
function showEntryPopup(uuid, personId, formId, tabIndex, encounterTypeId) {
	loadUrlIntoEncounterChartPopup(uuid, '', openmrsContextPath + '/module/htmlformentry/htmlFormEntry.form?inPopup=true&personId=' + personId + '&formId=' + formId + '&returnUrl=\"'+openmrsContextPath+'/module/htmlformflowsheet/testChart.list?selectTab=' + tabIndex+ '\"&closeAfterSubmission=closeEncounterChartPopup' + uuid, true, tabIndex, personId, encounterTypeId,  formId);
}
function replaceOneChar(s,c,n){
	(s = s.split(''))[--n] = c;
	return s.join('');
};