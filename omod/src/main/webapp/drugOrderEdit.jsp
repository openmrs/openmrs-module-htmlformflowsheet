<%@ include file="/WEB-INF/template/include.jsp" %>
<%@ include file="/WEB-INF/template/headerMinimal.jsp" %>

	<openmrs:htmlInclude file="/scripts/jquery/jquery-1.3.2.min.js" />
	
	<script type="text/javascript">
		$j = jQuery.noConflict();
		
		function validateSubmission(){
			var voided = document.getElementById('voided');
			var voidReason = document.getElementById('voidReason');
			var drug = document.getElementById('drugName');
			var startDate = document.getElementById('startDate');
			var discontinuedDate = document.getElementById('discontinuedDate');
			var autoexpiredDate = document.getElementById('autoExpireDate');
			
			if (drug.value == ''){
				alert('<spring:message code="htmlformflowsheet.error.drugIsNull"/>');
				return false;
			}
			if (startDate.value == null || startDate.value == ''){
				alert('<spring:message code="htmlformflowsheet.error.startDateIsNull"/>');
				return false;
			}
			if (voided.value == 'true' && (voidReason.value == null || voidReason.value == '')){
				alert('<spring:message code="htmlformflowsheet.error.voidReasonIsNull"/>');
				return false;
			 } 	 
			return true;
		}
		
	</script>
	<openmrs:htmlInclude file="/scripts/calendar/calendar.js" />
	<openmrs:htmlInclude file="/moduleResources/htmlformentry/htmlFormEntry.css" />
	<form method="post" onSubmit="return validateSubmission();">
	<input type="hidden" name="refCloseAfterSubmission" value="${model.dialogToClose}"/>
	<input type="hidden" name="refDrugOrderId" value="${model.drugOrder.orderId}"/>
	<table>
		<tr>
			<th><spring:message code="DrugOrder.drug"/></th>
			<th><spring:message code="DrugOrder.dose"/></th>
			<th><spring:message code="DrugOrder.frequency"/></th>
		</tr>
		<tr>
			<td>
				<select name="refDrugName" id="drugName">
					<option value=""> </option>
					<c:forEach var="drug" items="${allDrugs}">
						<option value="${drug.key.drugId}"
							<c:if test="${model.drugOrder.drug.drugId == drug.key.drugId}">
							      SELECTED   
						    </c:if>
						>${drug.value}</option>
					</c:forEach>
				</select>
			</td>
			<td>
				<input type="text" name="refDose" id="dose" value="${model.drugOrder.dose}" size="4"/>
			</td>
			<td>
				<input type="text" name="refFrequency" id="frequency" value="${model.drugOrder.frequency}"/>
			</td>
		</tr>
	</table>
	<table>
		<tr>
			<th><spring:message code="general.dateStart"/></th>
			<th><spring:message code="general.dateAutoExpire"/></th>
			<th><spring:message code="general.dateDiscontinued"/></th>
			<th><spring:message code="general.discontinuedReason"/></th>			
		</tr>
		<tr>
			<td>
				<input type="text" name="refStartDate" id="startDate" value="<openmrs:formatDate date="${model.drugOrder.effectiveStartDate}" />" size="10" onClick='showCalendar(this)'/>
			</td>
			<td>
				<input type="text" name="refAutoExpireDate" id="autoExpireDate" value="<openmrs:formatDate date="${model.drugOrder.autoExpireDate}" />" size="10" onClick='showCalendar(this)'/>
			</td>
			<td>
				<input type="text" name="refDiscontinedDate" id="discontinuedDate" value="<openmrs:formatDate date="${model.drugOrder.dateStopped}" />" size="10" onClick='showCalendar(this)'/>
			</td>
			<td>  
				<select name="refDiscontinueReason" id="discontinueReason">
					<option value=""> </option>
					<c:forEach var="reason" items="${discontinueReasons}">
						<option value="${reason.conceptId}"
							<c:if test="${model.drugOrder.discontinuedReason.conceptId == reason.conceptId}">
							      SELECTED   
						    </c:if>
						>${reason.name}</option>
					</c:forEach>
				</select>
			</td>
	</tr>
	</table>
	<table>
	<tr>
		<th><spring:message code="general.instructions"/></th>
			<th><spring:message code="DrugOrder.prn"/></th>
	</tr>
	<tr>
		<td>
				<textarea name="refInstructions" id="instructions" />${model.drugOrder.instructions}</textarea>
			</td>
			
			<td>
				<select type="text" name="refPrn" id="prn">
					<option value="false"
						<c:if test="${model.drugOrder.prn == false}"> SELECTED 
						</c:if>
					><spring:message code="general.false"/></option>
					<option value="true"
						<c:if test="${model.drugOrder.prn == true}"> SELECTED 
						</c:if>
					><spring:message code="general.true"/></option>
				</select>
			</td>
	</tr>
	</table>
	<table>
	<tr>
			<th><spring:message code="general.voided"/></th>
			<th><spring:message code="general.voidReason"/></th>
	</tr>
	<tr>
			<td>
				<select type="text" name="refVoided" id="voided">
					<option value="false"
						<c:if test="${model.drugOrder.voided == false}"> SELECTED 
						</c:if>
					><spring:message code="general.false"/></option>
					<option value="true"
						<c:if test="${model.drugOrder.voided == true}"> SELECTED 
						</c:if>
					><spring:message code="general.true"/></option>
				</select>
			</td>
			<td>
				<input type="text" name="refVoidReason" id="voidReason" value="${model.drugOrder.voidReason}"/>
			</td>
		</tr>
	</table>		
	
	<spring:message code="general.general.submit" var="submitname" text="submit" scope="page"/>
	<input type="submit" value="${submitname}"/>
	
	</form>
<%@ include file="/WEB-INF/template/footerMinimal.jsp" %>
