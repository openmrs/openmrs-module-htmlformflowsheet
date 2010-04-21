package org.openmrs.module.htmlformflowsheet.web.dwr;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Encounter;
import org.openmrs.api.EncounterService;
import org.openmrs.api.context.Context;
import org.openmrs.module.htmlformflowsheet.web.SingleHtmlFormPatientChartTab;
import org.openmrs.module.htmlformflowsheet.web.SingleHtmlFormPatientChartTab.Which;

public class HtmlFormFlowsheetDWR {

    protected final Log log = LogFactory.getLog(getClass());
    
    public boolean voidEncounter(String encIdSt){
        try{
            EncounterService es = Context.getEncounterService();
            Integer encId = Integer.valueOf(encIdSt);
            es.voidEncounter(es.getEncounter(encId), "mdrtb Cat-IV");
        } catch (Exception ex){
            return false;
        }
        return true;
    }
    
    
    public Integer getNewEncounterId(String whichEnc, Integer formId, Integer patientId){
            Integer ret = 0;
            try {
                List<Encounter> encs = Context.getEncounterService().getEncountersByPatientId(patientId);
                List<Encounter> byForm = new ArrayList<Encounter>();
                SingleHtmlFormPatientChartTab.Which which = SingleHtmlFormPatientChartTab.Which.valueOf(whichEnc);
                for (Encounter enc : encs){
                    if (enc.getForm() != null && enc.getForm().getFormId().equals(formId))
                        byForm.add(enc);
                }
                if (byForm.size() > 0){
                    if (which == Which.FIRST){
                        return byForm.get(0).getEncounterId();
                    } else {
                        return byForm.get(byForm.size()-1).getEncounterId();
                    }
                }
            } catch (Exception ex){
                return ret;
            }
            return ret;
    }
    
    
}
