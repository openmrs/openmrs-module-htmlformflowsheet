package org.openmrs.module.htmlformflowsheet.web.html;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.commons.lang.StringUtils;
import org.openmrs.Encounter;
import org.openmrs.Form;
import org.openmrs.Patient;
import org.openmrs.PatientProgram;
import org.openmrs.api.context.Context;
import org.openmrs.module.Extension;
import org.openmrs.module.htmlformflowsheet.HtmlFormFlowsheetUtil;
import org.openmrs.web.WebConstants;


public class PatientHeaderChartLinks extends Extension {
    
    @Override
    public String getOverrideContent(String str){
        if (!Context.isAuthenticated()) {
            return "";
        }
		String gp = Context.getAdministrationService().getGlobalProperty("htmlformflowsheet.patientChartFormIds");

		if (StringUtils.isEmpty(gp)) {
			return "";
		}

        StringBuilder sbExisting = new StringBuilder("");
        StringBuilder sbNonExisting = new StringBuilder("");
        try {
	        String patientId = this.getParameterMap().get("patientId");
	        Patient p = Context.getPatientService().getPatient(Integer.valueOf(patientId));

	        for (StringTokenizer st = new StringTokenizer(gp, ","); st.hasMoreTokens(); ) {
	        	
	        	Map<Integer, Set<Integer>> progForms = new HashMap<Integer, Set<Integer>>();
	            Set<Integer> programIds = new HashSet<Integer>();
	           
	            String formId = st.nextToken().trim();  
	            if (formId.contains(":")){
	                String[] formIdSplit = formId.split(":");
	                formId = formIdSplit[0];
	                //check for required programs:
	                String programInfo = formIdSplit[1];
	                if (programInfo.contains("|")){
	                    for (StringTokenizer strTok = new StringTokenizer(programInfo, "|"); strTok.hasMoreTokens(); ) {
	                        String sTmp = strTok.nextToken().trim();
	                        addFormToProgramList(progForms, Integer.valueOf(formId), Integer.valueOf(sTmp));
	                        programIds.add(Integer.valueOf(sTmp));
	                    } 
	                } else {
	                    //todo: support lookup programs by uuid and forms by uuid?
	                    addFormToProgramList(progForms, Integer.valueOf(formId), Integer.valueOf(programInfo));
	                    programIds.add(Integer.valueOf(programInfo));
	                }
	            }
	            Form form = HtmlFormFlowsheetUtil.getFormFromString(formId);
	            List<PatientProgram> pps = Context.getProgramWorkflowService().getPatientPrograms(p, null, null, null, null, null, false);
	            List<Encounter> encs = HtmlFormFlowsheetUtil.getEncountersForPatient(p, form,null);

//	            for (Map.Entry<Integer, Set<Integer>> m : progForms.entrySet()){
//	            	System.out.println(m.getKey()+ ":" + m.getValue());
//	            }
	            
	            //1. no program association to form.  always show.
	            if (progForms.get(Integer.valueOf(formId)) == null || progForms.get(Integer.valueOf(formId)).size() == 0){
	            	if (encs.size() == 0){
	            		//if no encs, show add new
	            		sbNonExisting.append(" | <a href=\"/" + WebConstants.WEBAPP_NAME + "/module/htmlformentry/htmlFormEntry.form?personId=" +p.getPersonId()+ "&patientId="+p.getPatientId()+"&returnUrl=&formId="+form.getFormId()+"\">" + form.getName() + "</a> | ");
	            	} else {
	            		//if encs, show existing flowsheet parent(s)
		            	for (Encounter e : encs){
		            		sbExisting.append(" | <a href=\"/"+ WebConstants.WEBAPP_NAME + "/module/htmlformentry/htmlFormEntry.form?encounterId="+e.getEncounterId()+"&mode=EDIT\">"+form.getName()+ " " + "(" + Context.getDateFormat().format(e.getEncounterDatetime()) + ")</a> | " );
		            	}
	            	}
	            } else {
		            //2. program(s) specified for form
	            	//this builds a map of encounter corresponding to the parent form creation to the patientProgram is was created in.
		            Map<Encounter, PatientProgram> encounterToPatientProgram = new HashMap<Encounter, PatientProgram>();
		            for (Encounter e: encs){
		            	for (PatientProgram pp : pps){
		            		
		            		//if encounter is later than start date and less than end date or end date is null
		            		if (programIds.contains(pp.getProgram().getProgramId()) 
		            				&&e.getEncounterDatetime().getTime() >= pp.getDateEnrolled().getTime() 
		            				&& ((pp.getDateCompleted() == null || pp.getDateCompleted().getTime() >= e.getEncounterDatetime().getTime()))){
		            			//encounter is in patientprogram
		            			encounterToPatientProgram.put(e, pp);
		            		}
		            	}
		            }
		          //show existing based on the map
		            for (Map.Entry<Encounter, PatientProgram> m : encounterToPatientProgram.entrySet()){
		            	sbExisting.append(" | <a href=\"/"+ WebConstants.WEBAPP_NAME + "/module/htmlformentry/htmlFormEntry.form?encounterId="+m.getKey().getEncounterId()+"&mode=EDIT\">"+form.getName());
		            	if (m.getValue() != null){
		            		sbExisting.append(" " + "(" + Context.getDateFormat().format(m.getValue().getDateEnrolled()) + " - ");
		            		if (m.getValue().getDateCompleted() != null)
		            			sbExisting.append(Context.getDateFormat().format(m.getValue().getDateCompleted()));
		            		sbExisting.append(")");
		            	}	
		            	sbExisting.append("</a> | " );
		            }
		                        
		            //show add new
		            	//if patient is in program currently, AND patient doesn't have an encounter for this program
		            PatientProgram ppActive = activePatientProgram(pps, programIds);
		            boolean found = false;
		            if (ppActive != null){
		            	for (Map.Entry<Encounter, PatientProgram> m : encounterToPatientProgram.entrySet()){
		            		if (m.getValue() != null && m.getValue().equals(ppActive))
		            			found = true;
		            	}
		            	if (!found)
		            		sbNonExisting.append(" | <a href=\"/" + WebConstants.WEBAPP_NAME + "/module/htmlformentry/htmlFormEntry.form?personId=" +p.getPersonId()+ "&patientId="+p.getPatientId()+"&returnUrl=&formId="+form.getFormId()+"\"> " + form.getName() + "</a> | ");
		            }
	            }
	        }
	        String retString = "<table><tr><td>";
	        if (!sbExisting.toString().equals(""))
	        	retString += "Existing Patient Chart(s): " + sbExisting.toString();
	        retString += "</td></tr><tr><td>";
	        if (!sbNonExisting.toString().equals(""))
	        	retString += "Create A New Patient Charts: " + sbNonExisting.toString();
	        retString += "</td></tr></table>";
	        return retString.replace("|  |", " | ");
	        
        } catch (Exception ex){
        	ex.printStackTrace();
        	return "";
        }
    }   
    
    /**
     * returns active patient program corresponding to valid programId
     * @param ppList
     * @param programIds
     * @return
     */
    private PatientProgram activePatientProgram(List<PatientProgram> ppList, Set<Integer> programIds){
    	if (ppList != null){
    		for (PatientProgram pp : ppList){
    			if (pp.getActive() && programIds.contains(pp.getProgram().getProgramId()))
    				return pp;
    		}
    	}
    	return null;
    }
    /**
     * @see org.openmrs.module.web.extension.AdministrationSectionExt#getMediaType()
     */
    public Extension.MEDIA_TYPE getMediaType() {
        return Extension.MEDIA_TYPE.html;
    }
    
    /**
     * 
     * add program to OK form list
     * 
     * @param progForms
     * @param formId
     * @param programId
     */
    private void addFormToProgramList(Map<Integer, Set<Integer>> progForms, Integer formId, Integer programId){
            if (!progForms.containsKey(programId)){
                Set<Integer> programIds = new HashSet<Integer>();
                programIds.add(programId);
                progForms.put(formId, programIds);
            } else {
                Set<Integer> programIds = progForms.get(formId);
                programIds.add(programId);
                progForms.put(formId,programIds);
            }
    }
}
