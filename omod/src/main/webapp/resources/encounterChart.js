function loadUrlIntoEncounterChartPopup(uuid, title, url, reloadOnClose, tabIndex, personId, encounterTypeId,  formId, showAllEncs, showHtmlFormInstead, showProvider, providerHeader, conceptsToShow) {
	var elem = $j('#encounterChartPopup' + uuid);
	var iframe = $j("#encounterChartIFrame" + uuid);
	iframe.empty();
	elem.dialog('option', 'title', title)
		.dialog('option', 'height', $j(window).height() - 50);
	if (reloadOnClose) {	
		//elem.dialog('option', 'close', function(event, ui) { window.location = jQuery.queryString(window.location.href, 'selectTab=' + tabIndex); });
		elem.dialog('option', 'close', function(event, ui) { 
             $j('#encounterWidget_' + uuid).html("loading...");
             var pathStr = openmrsContextPath + "/module/htmlformflowsheet/encounterChartContent.list?patientId=" +personId+ "&personId=" +personId+ "&portletUUID=" + uuid + "&encounterTypeId=" + encounterTypeId +"&view=" + tabIndex+ "&formId=" +formId+ "&count=" + (tabIndex+1) + "&showHtmlFormInstead=" + showHtmlFormInstead + "&showProvider=" + showProvider + "&providerHeader=" + providerHeader + "&conceptsToShow=" + conceptsToShow;
             if (showAllEncs)
             	pathStr = pathStr + "&showAllEncsWithEncType=true";
			 $j('#encounterWidget_' + uuid).load(pathStr);
			 repopulateEncounterSelectOptions(personId, encounterTypeId);
		});
	} 
	$j('.ui-dialog-titlebar-close').mousedown(function(){
		elem.dialog('option', 'close', function(){
			iframe.empty();
			resizeHtmlFormIframe(formId, uuid);
		});
	});
	elem.dialog('open');
	$j(iframe).attr("src", url);
}

function showEncounterPopup(uuid, encId, formId, showHtmlFormInstead, showProvider, providerHeader, conceptsToShow) {
	loadUrlIntoEncounterChartPopup(uuid, '', openmrsContextPath + '/module/htmlformentry/htmlFormEntry.form?inPopup=true&encounterId=' + encId +'&formId='+formId, false, "", "", "", formId, false, showHtmlFormInstead, showProvider, providerHeader, conceptsToShow);
}
function showEncounterEditPopup(uuid, encId, personId, formId, tabIndex, encounterTypeId, encType, showHtmlFormInstead, showProvider, providerHeader, conceptsToShow) {
	loadUrlIntoEncounterChartPopup(uuid, '', openmrsContextPath + '/module/htmlformentry/htmlFormEntry.form?inPopup=true&encounterId=' + encId + "&formId=" + formId + "&mode=EDIT&closeAfterSubmission=closeEncounterChartPopup" + uuid, true, tabIndex, personId, encounterTypeId, formId, encType, showHtmlFormInstead, showProvider, providerHeader, conceptsToShow);
}
function showSelectEncounterEditPopup(uuid, encId, personId, formId, tabIndex, encounterTypeId, showHtmlFormInstead, showProvider, providerHeader, conceptsToShow) {
	loadUrlIntoEncounterChartPopup(uuid, '', openmrsContextPath + '/module/htmlformentry/htmlFormEntry.form?inPopup=true&encounterId=' + encId + "&formId=" + formId + "&mode=EDIT&closeAfterSubmission=closeEncounterChartPopup" + uuid, true, tabIndex, personId, encounterTypeId, formId, true, showHtmlFormInstead, showProvider, providerHeader, conceptsToShow);
} 
function showEntryPopup(uuid, personId, formId, tabIndex, encounterTypeId, showAllEncs, showHtmlFormInstead, showProvider, providerHeader, conceptsToShow) {
	loadUrlIntoEncounterChartPopup(uuid, '', openmrsContextPath + '/module/htmlformentry/htmlFormEntry.form?inPopup=true&personId=' + personId + '&formId=' + formId + '&returnUrl='+openmrsContextPath+'/module/htmlformflowsheet/testChart.list%3FselectTab%3D' + tabIndex+ '&closeAfterSubmission=closeEncounterChartPopup' + uuid, true, tabIndex, personId, encounterTypeId,  formId, showAllEncs, showHtmlFormInstead, showProvider, providerHeader, conceptsToShow);
}

function replaceOneChar(s,c,n){
	(s = s.split(''))[--n] = c;
	return s.join('');
}
function resizeHtmlFormIframe(formId, uuid){
	var x = $j(parent.window.document).find('#iframeFor'+ formId);
	if (x.length == 1){
		var height = $j('#encContentTable'+uuid).outerHeight(true) + 24;
		x[0].style.height = height + 'px';
		//var height = x.contentWindow.document.body.offsetHeight + 24;
		//x.style.height = height + 'px';
	}
}

function repopulateEncounterSelectOptions(patientId, encounterTypeId){

	HtmlFlowsheetDWR.getAllEncsByPatientAndEncType(patientId, encounterTypeId, function(ret){
				var doc = parent.document;
				var iframes = doc.getElementsByTagName("iframe");
				try {
					if (iframes.length > 0){
						for (var i = 0; i < iframes.length; i++){
							var iframe = iframes[i];
							if (iframe.id.indexOf("iframeFor") > -1){
								var selects = $j(iframe).contents().find(".encounterSelect");
								for (var j = 0; j < selects.length; j++){
									var select = selects[j];
									//todo:  clear select
									$j(select).children().remove();
									$j(select).append(new Option("",0));
									//todo:  add options to select
									for (var k = 0; k < ret.length; k++){
										var encounterToAdd = ret[k];
										$j(select).append(new Option(encounterToAdd.encounterDatetime + " / " + encounterToAdd.provider + " / " + encounterToAdd.location, encounterToAdd.encounterId));
									}
								}
							}
						}
					}
				} catch (exception){}		
	});
}									
