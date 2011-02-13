package org.openmrs.module.htmlformflowsheet.web.html;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.openmrs.Encounter;
import org.openmrs.Form;
import org.openmrs.Patient;
import org.openmrs.PatientProgram;
import org.openmrs.api.context.Context;
import org.openmrs.module.Extension;
import org.openmrs.module.htmlformflowsheet.web.utils.HtmlFormFlowsheetWebUtils;
import org.openmrs.web.WebConstants;


public class PatientHeaderChartLinks extends Extension {
    
    @Override
    public String getOverrideContent(String str){
        if (!Context.isAuthenticated()) {
            return "";
        }
        StringBuilder sbExisting = new StringBuilder("");
        StringBuilder sbNonExisting = new StringBuilder("");
        try {
        String patientId = this.getParameterMap().get("patientId");
        Patient p = Context.getPatientService().getPatient(Integer.valueOf(patientId));
        String gp = Context.getAdministrationService().getGlobalProperty("htmlformflowsheet.patientChartFormIds");
        for (StringTokenizer st = new StringTokenizer(gp, ","); st.hasMoreTokens(); ) {
            String formId = st.nextToken().trim();
            Map<Integer, Set<Integer>> progForms = new HashMap<Integer, Set<Integer>>();
            if (formId.contains(":")){
                String[] formIdSplit = formId.split(":");
                formId = formIdSplit[0];
                //check for required programs:
                String programInfo = formIdSplit[1];
                if (programInfo.contains("|")){
                    for (StringTokenizer strTok = new StringTokenizer(programInfo, "|"); strTok.hasMoreTokens(); ) {
                        String sTmp = strTok.nextToken().trim();
                        this.addFormToProgramList(progForms, Integer.valueOf(formId), Integer.valueOf(sTmp));
                    } 
                } else {
                    //todo: support lookup programs by uuid and forms by uuid?
                    this.addFormToProgramList(progForms, Integer.valueOf(formId), Integer.valueOf(programInfo));
                }
            }
            Form form = HtmlFormFlowsheetWebUtils.getFormFromString(formId);
            List<PatientProgram> pps = Context.getProgramWorkflowService().getPatientPrograms(p, null, null, null, null, null, false);
            
            //TODO:  if there are no associated program requirements, or if there is a program requirement for this form and patient is enrolled
            if (writeLinkForThisFormForThisPatient(pps, progForms, Integer.valueOf(formId))){
                List<Encounter> encs = Context.getEncounterService().getEncounters(p, null, null, null, Collections.singletonList(form), null, null, false);
                if (encs.size() == 0){
                    //TODO:  localize this:
                    if (sbNonExisting.toString().equals(""))
                        sbNonExisting.append(" Start A New Patient Chart: ");
                    sbNonExisting.append(" <a href=\"/" + WebConstants.WEBAPP_NAME + "/module/htmlformentry/htmlFormEntry.form?personId=" +p.getPersonId()+ "&patientId="+p.getPatientId()+"&returnUrl=&formId="+form.getFormId()+"\"> " + form.getName() + "</a> ");
                } else {
                    if (sbExisting.toString().equals(""))
                        sbExisting.append(" Patient Charts: ");
                    sbExisting.append(" <a href=\"/"+ WebConstants.WEBAPP_NAME + "/module/htmlformentry/htmlFormEntry.form?encounterId="+encs.get(encs.size()-1).getEncounterId()+"&mode=EDIT\">"+form.getName()+"</a>");
                }
            }
        }
        return "<table><tr><td>" + sbExisting.toString() + "</td></tr><tr><td>" + sbNonExisting.toString() + "</td></tr></table>";
        } catch (Exception ex){
        ex.printStackTrace();
        return "";
        }
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
     * @param program
     */
    private void addFormToProgramList(Map<Integer, Set<Integer>> progForms, Integer formId, Integer programId){
            if (!progForms.containsKey(programId)){
                Set<Integer> forms = new HashSet<Integer>();
                forms.add(formId);
                progForms.put(programId, forms);
            } else {
                Set<Integer> forms = progForms.get(programId);
                forms.add(formId);
                progForms.put(programId, forms);
            }
    }
    
    /**
     * 
     * if there are no associated program requirements, or if there is a program requirement for this form and patient is enrolled return true
     * 
     * @param pps
     * @param progForms
     * @param formId
     * @return
     */
    private boolean writeLinkForThisFormForThisPatient(List<PatientProgram> pps, Map<Integer, Set<Integer>> progForms, Integer formId){
        //the map of program : formIds
        boolean formFoundInMap = false;
        for (Map.Entry<Integer, Set<Integer>> e : progForms.entrySet()){
            //for each formId
            for (Integer formIdTmp : e.getValue()){
                //if the formId shows up as associated with a program
                if (formIdTmp.equals(formId)){
                    formFoundInMap = true;
                    //look for that program in the list of patientprograms
                    for (PatientProgram pp : pps){
                        //if the patient has a patientprogram for the programId then you can show link
                        if (pp.getProgram().getProgramId().equals(e.getKey()))
                            return true;
                    }
                }
            }
        }
        if (!formFoundInMap)
            return true;
        return false;
    }
}
