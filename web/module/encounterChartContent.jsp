<%@ include file="/WEB-INF/template/include.jsp" %>
<%@ taglib prefix="htmlformflowsheet" uri="/WEB-INF/view/module/htmlformflowsheet/taglib/htmlformflowsheet.tld" %>

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



<!--<htmlformflowsheet:htmlInclude file="/dwr/interface/HtmlFlowsheetDWR.js" />-->

<%-- If there are any encounters, we show a chart --%>
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

		try {
				var x = $j(parent.document).find('#iframeFor${model.formId}');
				if (x.length == 1){
					var frame = x[0];
					$j(frame).focus();
					var height = $j('#encContentTable${model.portletUUID}').outerHeight() + 24;
					frame.style.height = height + 'px';
				}	
		} catch (exception){} 
		
	});

   function resizeIFrame${model.portletUUID}(extraSpace){
	   try {
			var x = $j(parent.document).find('#iframeFor${model.formId}');
			if (x.length == 1)
				x[0].style.height = extraSpace + 'px';
	   } catch (exception){} 
    }


	function closeEncounterChartPopup${model.portletUUID}() {	 
		//$j("#encounterChartPopup${model.portletUUID}").dialog('close');
			$j(".ui-widget-content").dialog('close');
	}
	
	function voidEncounter${model.portletUUID}(uuid, id, formId, retVal){
		if (retVal == true){
			HtmlFlowsheetDWR.voidEncounter(id, formId, function(ret){
								if (!ret){
									alert('<spring:message code="htmlformflowsheet.cantdeleteencounter" />');
								} else {
									$j('#encounterWidget_' + uuid).load( openmrsContextPath + "/module/htmlformflowsheet/encounterChartContent.list?patientId=${model.personId}&portletUUID=" + uuid +"&encounterTypeId=${model.encounterTypeId}&view=${model.view}&formId=${model.formId}&count=${model.view + 1}&showAllEncsWithEncType=${model.showAllEncsWithEncType}"); 
									repopulateEncounterSelectOptions(${model.personId}, ${model.encounterTypeId});
								}
							});
			
		}
	}
	
	function editHTMLForm${model.portletUUID}(encId,view){
		window.location = '${pageContext.request.contextPath}/module/htmlformentry/htmlFormEntry.form?encounterId=' + encId + '&mode=EDIT&returnUrl=${pageContext.request.contextPath}/module/htmlformflowsheet/testChart.list?selectTab=' + view;
	}
	
</script>

<table id="encContentTable${model.portletUUID}" class="thinBorder" style="width:100%;">
	<tr>
		<td colspan="2" style="color:darkblue"><spring:message code="htmlformflowsheet.date" /></td>
		<c:forEach var="concept" items="${model.encounterChartConcepts}">
			<td style="color:darkblue"><htmlformflowsheet:conceptFormat concept="${concept}" shortestName="true" /></td>
		</c:forEach>
	</tr>
	<c:forEach var="enc" items="${model.encounterListForChart}">
		<c:set var="found" value="false"/>
		<c:forEach var="concept" items="${model.encounterChartConcepts}">
			<c:forEach var="obs" items="${model.encounterChartObs[enc][concept.conceptId]}">
				<c:if test="${obs != null}">
					<c:set var="found" value="true"/>
				</c:if>
			</c:forEach>
		</c:forEach>
		<c:if test="${found == 'true'}">
			<tr style="height:30px;">
				<td style="width:38px;">
				 <c:if test="${model.readOnly == 'false'}">
					<input type="image" src="${pageContext.request.contextPath}/images/file.gif"  
						    name="editEncounter" 
							onclick="resizeIFrame${model.portletUUID}(${model.windowHeight});showEncounterEditPopup('${model.portletUUID}',${enc.encounterId}, ${model.personId}, ${model.formId}, ${model.view}, ${model.encounterTypeId})"
							title="edit" 
							alt="edit"/>			
					<input type="image" src="${pageContext.request.contextPath}/images/trash.gif"  
						    name="voidEncounter" 
							onclick="voidEncounter${model.portletUUID}('${model.portletUUID}',${enc.encounterId}, ${model.formId}, confirm('<spring:message code="Are you sure you want to delete this encounter?"/>'));" 
							title="<spring:message code="htmlformflowsheet.deleteEncounters"/>" 
							alt="<spring:message code="htmlformflowsheet.deleteEncounters"/>"/>
	             </c:if>
				</td>
				<td>
					<a href="javascript:void(0)" onClick="resizeIFrame${model.portletUUID}(${model.windowHeight});showEncounterPopup('${model.portletUUID}', ${enc.encounterId},${model.formId})">
						<openmrs:formatDate date="${enc.encounterDatetime}"/>
					</a>
				</td>
				<c:forEach var="concept" items="${model.encounterChartConcepts}">
					<td>
						<c:forEach var="obs" items="${model.encounterChartObs[enc][concept.conceptId]}">
							<c:if test="${obs.valueCoded != null}">
								<htmlformflowsheet:conceptFormat concept="${obs.valueCoded}" bestShortName="true" />
							</c:if>
							<c:if test="${obs.valueCoded == null}">
								<!-- HERE:  ugh... this is going to need a custom tag handler for valueNumerics --->
								<htmlformflowsheet:obsFormat obs="${obs}"/> 
							</c:if>
							<c:if test="${obs.accessionNumber != null}"> (${obs.accessionNumber})</c:if><br/>
						</c:forEach>
					</td>
				</c:forEach>
			</tr>
		</c:if>
	</c:forEach>
	<c:if test="${model.showAddAnother != 'false' && model.readOnly == 'false'}"> 
		<tr>
			<td colspan="${fn:length(model.encounterChartConcepts) + 2}" align="center">
				<div class="addAnother">
					<c:if test="${model.showAllEncsWithEncType == 'true'}">
						<table>
						<tr><td style="border:0px; text-align:left"> Append a row to an existing visit: </td>
						<td style="border:0px; text-align:left">
						<select class="encounterSelect" onMouseUp="if ($j(this).val() != 0){resizeIFrame${model.portletUUID}(${model.windowHeight});showSelectEncounterEditPopup('${model.portletUUID}',$j(this).val(),${model.personId}, ${model.formId}, ${model.view}, ${model.encounterTypeId} );}" id="encounterSelect_${model.portletUUID}">
						<option value="0"></option>
						<c:forEach var="enc" items="${model.encounterListForChart}">
							<option value="${enc.encounterId}">
								<openmrs:formatDate date="${enc.encounterDatetime}"/> / ${enc.provider.familyName} ${enc.provider.givenName} / (${enc.location})
							</option>	
						</c:forEach>
						</select></td></tr>
						<tr><td style="border:0px; text-align:left"> Or, start a new visit: </td>
						<td style="border:0px; text-align:left">
					</c:if>	
						<button onClick="resizeIFrame${model.portletUUID}(${model.windowHeight});showEntryPopup('${model.portletUUID}', ${model.personId}, ${model.formId}, ${model.view}, ${model.encounterTypeId}, ${model.showAllEncsWithEncType});"> 
							<c:if test="${!empty model.addAnotherButtonLabel}">
								${model.addAnotherButtonLabel}
							</c:if>
							<c:if test="${empty model.addAnotherButtonLabel}">
								<spring:message code="htmlformflowsheet.addanother" />
							</c:if>		
						</button>
					<c:if test="${model.showAllEncsWithEncType == 'true'}">	
						</td></tr></table>
					</c:if>
				</div>
			</td>
		</tr>	
	</c:if>
</table>

<div id="encounterChartPopup${model.portletUUID}">
	<iframe id="encounterChartIFrame${model.portletUUID}" width="100%" height="100%" marginWidth="0" marginHeight="0" frameBorder="0" scrolling="auto"></iframe>
</div>
<%-- Maybe show an "Add Another button --%>

