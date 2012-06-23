package org.openmrs.module.htmlformflowsheet;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.openmrs.Encounter;
import org.openmrs.api.context.Context;

public class HtmlFormFlowsheetUtil {

	public static List<Encounter> sortEncountersAccordingToGp(List<Encounter> encs){
        String gp = Context.getAdministrationService().getGlobalProperty("htmlformflowsheet.encountersChronology");
        if (gp != null && gp.trim().equals("asc")){
            Collections.sort(encs, new Comparator<Encounter>() {	
            	public int compare(Encounter enc1, Encounter enc2){
          		return enc1.getEncounterDatetime().compareTo(enc2.getEncounterDatetime());
          	}
           });
        } else {
            Collections.sort(encs, new Comparator<Encounter>() {	
              	public int compare(Encounter enc1, Encounter enc2){
            		return enc2.getEncounterDatetime().compareTo(enc1.getEncounterDatetime());
            	}
             });
        }
        return encs;
	}
}
