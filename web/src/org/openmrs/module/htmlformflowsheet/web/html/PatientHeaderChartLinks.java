package org.openmrs.module.htmlformflowsheet.web.html;

import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

import org.openmrs.Encounter;
import org.openmrs.Form;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.Extension;
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
            String s = st.nextToken().trim();
            Form form = Context.getFormService().getForm(Integer.valueOf(s));
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
}
