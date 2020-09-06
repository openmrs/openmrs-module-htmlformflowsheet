	<c:if test="${model.showAddAnother != 'false' && model.readOnly == 'false'}"> 
		<tr>
			<c:choose>
				<c:when test="${empty model.showProvider}">
					<c:set var="addAnotherColspan" value="3"/>
				</c:when>
				<c:otherwise>
					<c:set var="addAnotherColspan" value="4"/>
				</c:otherwise>
			</c:choose>
			
			<c:if test="${empty model.showHtmlFormInstead || (!empty model.showHtmlFormInstead && model.showHtmlFormInstead != 'true')}">
				<c:choose>
					<c:when test="${model.showProvider}">
						<c:set var="addAnotherColspan" value="${fn:length(model.encounterChartConcepts) + 3}"/>
					</c:when>
					<c:otherwise>
						<c:set var="addAnotherColspan" value="${fn:length(model.encounterChartConcepts) + 2}"/>
					</c:otherwise>
				</c:choose>
				
			</c:if>
			<td colspan="${addAnotherColspan}" align="center">
				<div class="addAnother">
					<c:if test="${model.showAllEncsWithEncType == 'true'}">
						<table>
						<c:if test="${!empty model.encounterListForChart}">
							<tr><td style="border:0px; text-align:left"> Append a row to an existing visit: </td>
							<td style="border:0px; text-align:left">
							<select class="encounterSelect" onMouseUp="if ($j(this).val() != 0){resizeIFrame${model.portletUUID}(${model.windowHeight});showSelectEncounterEditPopup('${model.portletUUID}',$j(this).val(),${model.personId}, ${model.formId}, ${model.view}, ${model.encounterTypeId},${model.showHtmlFormInstead},${model.showProvider},'${model.providerHeader }','${model.conceptsToShow}');}" id="encounterSelect_${model.portletUUID}">
							<option value="0"></option>
							<c:forEach var="enc" items="${model.encounterListForChart}">
								<option value="${enc.encounterId}">
									<openmrs:formatDate date="${enc.encounterDatetime}"/> / <openmrs:format encounterProviders="${enc.providersByRoles}"/> / (${enc.location})
								</option>	
							</c:forEach>
							</select></td></tr>
						</c:if>
						<tr><td style="border:0px; text-align:left"> Start a New Encounter: </td>
						<td style="border:0px; text-align:left">
					</c:if>	
						<button onClick="resizeIFrame${model.portletUUID}(${model.windowHeight});showEntryPopup('${model.portletUUID}', ${model.personId}, ${model.formId}, ${model.view}, ${model.encounterTypeId}, ${model.showAllEncsWithEncType}, ${model.showHtmlFormInstead}, ${model.showProvider},'${model.providerHeader }','${model.conceptsToShow}');">
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