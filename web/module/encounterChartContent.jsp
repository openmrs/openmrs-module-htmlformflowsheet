<%@ include file="/WEB-INF/template/include.jsp" %>
<%@ taglib prefix="htmlformflowsheet" uri="/WEB-INF/view/module/htmlformflowsheet/taglib/htmlformflowsheet.tld" %>

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
									$j('#encounterWidget_' + uuid).load( openmrsContextPath + "/module/htmlformflowsheet/encounterChartContent.list?patientId=${model.personId}&portletUUID=" + uuid +"&encounterTypeId=${model.encounterTypeId}&view=${model.view}&formId=${model.formId}&count=${model.view + 1}&showAllEncsWithEncType=${model.showAllEncsWithEncType}&showHtmlFormInstead=${model.showHtmlFormInstead}"); 
									repopulateEncounterSelectOptions(${model.personId}, ${model.encounterTypeId});
								}
							});
			
		}
	}
	
	function editHTMLForm${model.portletUUID}(encId,view){
		window.location = '${pageContext.request.contextPath}/module/htmlformentry/htmlFormEntry.form?encounterId=' + encId + '&mode=EDIT&returnUrl=${pageContext.request.contextPath}/module/htmlformflowsheet/testChart.list?showAllEncsWithEncType=${model.showAllEncsWithEncType}&selectTab=' + view;
	}
	
</script>

<table id="encContentTable${model.portletUUID}" class="thinBorder" style="width:100%;">
	<tr>
		<td colspan="2" style="color:darkblue"><spring:message code="htmlformflowsheet.date" /></td>
		<c:if test="${empty model.showHtmlFormInstead || (!empty model.showHtmlFormInstead && model.showHtmlFormInstead != 'true')}">
			<c:forEach var="concept" items="${model.encounterChartConcepts}"><!-- Each concept is a map with the concept itself and the name string from the form-->
				<td style="color:darkblue">
						<c:forEach var="entry" items="${concept}">
						     <c:if test="${empty entry.value || entry.value == ''}">
							 	 <htmlformflowsheet:conceptFormat concept="${entry.key}" bestShortName="true" />
							 </c:if>
							 <c:if test="${!empty entry.value && entry.value != ''}">
							 	 ${entry.value}
							 </c:if>
					    </c:forEach>		
				</td>
			</c:forEach>
		</c:if>
		<c:if test="${!empty model.showHtmlFormInstead && model.showHtmlFormInstead == 'true'}">
			<td>&nbsp;</td>
		</c:if>
	</tr>
	<c:set var="usedDrugOrders" value=""/>
	<c:set var="usedObs" value=""/>
	<c:forEach var="enc" items="${model.encounterListForChart}">
			<c:set var="found" value="false"/>
			<c:if test="${model.foundEncounters[enc] == 'true'}">
				<c:set var="found" value="true"/>
	        </c:if>
			
			<c:if test="${found == 'true'}">
					<tr style="height:30px;">
						<td style="width:38px;"> 
						 <c:if test="${model.readOnly == 'false' && !empty enc.encounterId}">
							<input type="image" src="${pageContext.request.contextPath}/images/file.gif"  
								    name="editEncounter" 
									onclick="resizeIFrame${model.portletUUID}(${model.windowHeight});showEncounterEditPopup('${model.portletUUID}',${enc.encounterId}, ${model.personId}, ${model.formId}, ${model.view}, ${model.encounterTypeId}, ${model.showAllEncsWithEncType}, ${model.showHtmlFormInstead});"
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
							<c:if test="${model.readOnly == 'false' && !empty enc.encounterId}">
								<a href="javascript:void(0)" onClick="resizeIFrame${model.portletUUID}(${model.windowHeight});showEncounterPopup('${model.portletUUID}', ${enc.encounterId},${model.formId},${model.showHtmlFormInstead})">
									<openmrs:formatDate date="${enc.encounterDatetime}"/>
								</a>
							</c:if>
							<c:if test="${model.readOnly == 'true' || empty enc.encounterId}">
								<openmrs:formatDate date="${enc.encounterDatetime}"/>
							</c:if>
						</td>
						
						<c:if test="${empty model.showHtmlFormInstead || (!empty model.showHtmlFormInstead && model.showHtmlFormInstead != 'true')}">
							<c:forEach var="conceptAndNameMap" items="${model.encounterChartConcepts}"> <!-- Set<Map<Concept,String>> -->
								<c:forEach var="conceptAndStrings" items="${conceptAndNameMap}"> <!-- unpacks Concept to String mapping -->
									<td>
										<!--  TODO:  HERE  use answerLabel if String not nothing or null --->
										<c:set var="matched" value="false"/>
										<c:forEach var="obs" items="${model.encounterChartObs[enc][conceptAndStrings.key.conceptId]}">
											<c:if test="${fn:contains(usedObs,obs.uuid) == false}">
											    <div>
												<c:if test="${obs.valueCoded != null}">
												    <c:set var="useConceptName" value="true"/>
													        <c:forEach var="concept" items="${model.conceptAnswers}"><!-- Each concept is a map with the concept itself and the name string from the form-->
																<c:forEach var="entry" items="${concept}">
									     								<c:if test="${obs.valueCoded.conceptId == entry.key.conceptId}">
									     									<c:if test="${!empty entry.value && entry.value != ''}">
									     											${entry.value}
									     											<c:set var="useConceptName" value="false"/>
									     									</c:if>
									     								</c:if>
									     						</c:forEach>
									     					</c:forEach>			
													<c:if test="${useConceptName == 'true'}">  
														<htmlformflowsheet:conceptFormat concept="${obs.valueCoded}" bestShortName="true" />
													</c:if>	
												</c:if>
												<c:if test="${obs.valueCoded == null}">
													<!-- HERE:  ugh... this is going to need a custom tag handler for valueNumerics --->
													<htmlformflowsheet:obsFormat obs="${obs}"/> 
												</c:if>
												<c:if test="${obs.accessionNumber != null}"> (${obs.accessionNumber})</c:if><br/>
												<c:if test="${enc.encounterDatetime != obs.obsDatetime}"> (<openmrs:formatDate date="${obs.obsDatetime}"/>)</c:if>
												<c:set var="matched" value="true"/>
												<c:set var="usedObs" value="${usedObs},${obs.uuid}"/>
												</div>
											</c:if>	
										</c:forEach>
										<c:if test="${matched == 'false'}">
											<c:forEach var="drugOrder" items="${model.encounterToDrugOrderMap[enc]}">
												<!-- look for concept match in drugOrders -->
												<c:if test="${fn:contains(conceptAndStrings.key.uuid, drugOrder.drug.name) == true}">
													<c:if test="${fn:contains(usedDrugOrders,drugOrder.uuid) == false}">	
														<c:if test="${empty enc.encounterId}">
														<input type="image" src="${pageContext.request.contextPath}/images/file.gif"  
									   						 name="editEncounter" 
															 onclick="resizeIFrame${model.portletUUID}(${model.windowHeight});showDrugOrderEditPopup('${model.portletUUID}', ${drugOrder.orderId}, ${model.personId}, ${model.view}, ${model.encounterTypeId}, ${model.formId},${model.showAllEncsWithEncType}, '<c:forEach var='drugOption' items='${model.drugSet}'>${drugOption.drugId},</c:forEach>', ${model.showHtmlFormInstead})"
															 title="edit" 
															 alt="edit"/>
														</c:if>	 
														<c:if test="${!empty drugNames[drugOrder.drug]}">
															${drugNames[drugOrder.drug]}
														</c:if>
														<c:if test="${empty drugNames[drugOrder.drug]}">
															${drugOrder.drug.name} 
														</c:if>
														<c:set var="expireDate" value="${drugOrder.discontinuedDate}"/>
														<c:if test="${empty expireDate}">
															<c:set var="expireDate" value="${drugOrder.autoExpireDate}"/>
														</c:if>
														<c:if test="${enc.encounterDatetime != drugOrder.startDate || !empty expireDate}"> 
															(<openmrs:formatDate date="${drugOrder.startDate}" /> <spring:message code="htmlformflowsheet.through" /> <c:if test="${empty expireDate}">- </c:if><c:if test="${!empty expireDate}"><openmrs:formatDate date="${expireDate}" /></c:if>)
														</c:if>
														<c:set var="usedDrugOrders" value="${usedDrugOrders},${drugOrder.uuid}"/>
														<br/>
													</c:if>
												</c:if>	
											</c:forEach>							
										</c:if>
									</td>
								</c:forEach>
							</c:forEach>
						</c:if>
						<c:if test="${!empty model.showHtmlFormInstead && model.showHtmlFormInstead == 'true'}">
							<!-- load the htmlform in edit mode -->
							<td><div id="div_${enc.encounterId}_${model.portletUUID}">...</div></td>
							<script type="text/javascript">
									$j('#div_${enc.encounterId}_${model.portletUUID}').load(openmrsContextPath + '/module/htmlformentry/htmlFormEntry.form?inPopup=true&encounterId=${enc.encounterId}&formId=${model.formId}&mode=VIEW htmlform', function(){ resizeHtmlFormIframe('${model.formId}','${model.portletUUID}'); });
							</script>
						</c:if>
					</tr>
			</c:if>
	</c:forEach>
	<c:if test="${model.showAddAnother != 'false' && model.readOnly == 'false'}"> 
		<tr>
			<c:set var="addAnotherColspan" value="3"/>
			<c:if test="${empty model.showHtmlFormInstead || (!empty model.showHtmlFormInstead && model.showHtmlFormInstead != 'true')}">
				<c:set var="addAnotherColspan" value="${fn:length(model.encounterChartConcepts) + 2}"/>
			</c:if>
			<td colspan="${addAnotherColspan}" align="center">
				<div class="addAnother">
					<c:if test="${model.showAllEncsWithEncType == 'true'}">
						<table>
						<c:if test="${!empty model.encounterListForChart}">
							<tr><td style="border:0px; text-align:left"> Append a row to an existing visit: </td>
							<td style="border:0px; text-align:left">
							<select class="encounterSelect" onMouseUp="if ($j(this).val() != 0){resizeIFrame${model.portletUUID}(${model.windowHeight});showSelectEncounterEditPopup('${model.portletUUID}',$j(this).val(),${model.personId}, ${model.formId}, ${model.view}, ${model.encounterTypeId},${model.showHtmlFormInstead} );}" id="encounterSelect_${model.portletUUID}">
							<option value="0"></option>
							<c:forEach var="enc" items="${model.encounterListForChart}">
								<option value="${enc.encounterId}">
									<openmrs:formatDate date="${enc.encounterDatetime}"/> / ${enc.provider.familyName} ${enc.provider.givenName} / (${enc.location})
								</option>	
							</c:forEach>
							</select></td></tr>
						</c:if>
						<tr><td style="border:0px; text-align:left"> Start a New Encounter: </td>
						<td style="border:0px; text-align:left">
					</c:if>	
						<button onClick="resizeIFrame${model.portletUUID}(${model.windowHeight});showEntryPopup('${model.portletUUID}', ${model.personId}, ${model.formId}, ${model.view}, ${model.encounterTypeId}, ${model.showAllEncsWithEncType}, ${model.showHtmlFormInstead});"> 
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

