package org.openmrs.module.htmlformflowsheet.web.dwr;

import org.openmrs.Encounter;
import org.openmrs.EncounterProvider;
import org.openmrs.api.context.Context;

public class HtmlFormFlowsheetDWREncounterObj {

    private Integer encounterId;
    private String provider;
    private String location;
    private String encounterDatetime;
    
    public HtmlFormFlowsheetDWREncounterObj(){}
    public HtmlFormFlowsheetDWREncounterObj(Encounter enc){
        encounterId = enc.getEncounterId();
        provider = "";
        for (EncounterProvider ep : enc.getEncounterProviders()) {
            provider += (provider.length() == 0 ? "" : ", ") + ep.getProvider().getName();
        }
        location = enc.getLocation().getName();
        encounterDatetime = Context.getDateFormat().format(enc.getEncounterDatetime());
     }
    
    public Integer getEncounterId() {
        return encounterId;
    }
    public void setEncounterId(Integer encounterId) {
        this.encounterId = encounterId;
    }
    public String getProvider() {
        return provider;
    }
    public void setProvider(String provider) {
        this.provider = provider;
    }
    public String getLocation() {
        return location;
    }
    public void setLocation(String location) {
        this.location = location;
    }
    public String getEncounterDatetime() {
        return encounterDatetime;
    }
    public void setEncounterDatetime(String encounterDatetime) {
        this.encounterDatetime = encounterDatetime;
    }

}
