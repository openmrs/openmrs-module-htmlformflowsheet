package org.openmrs.module.htmlformflowsheet.web.controller;

import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.openmrs.Encounter;
import org.openmrs.Form;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.htmlformflowsheet.HtmlFormFlowsheetUtil;
import org.openmrs.module.htmlformflowsheet.SingleHtmlFormPatientChartTab;
import org.openmrs.module.htmlformflowsheet.SingleHtmlFormPatientChartTab.Which;
import org.openmrs.util.OpenmrsUtil;
import org.openmrs.web.controller.PortletController;


public class SingleHtmlFormPortletController extends PortletController {

    public SingleHtmlFormPortletController(){super();}
    
	/**
     * @see org.openmrs.web.controller.PortletController#populateModel(javax.servlet.http.HttpServletRequest, java.util.Map)
     */
    @Override
    protected void populateModel(HttpServletRequest request, Map<String, Object> model) {
    	String uuid = (String) model.get("portletUUID");
    	model.put("portletUUID", uuid.replace("-", ""));
    	
    	SingleHtmlFormPatientChartTab.Which which = SingleHtmlFormPatientChartTab.Which.valueOf((String) model.get("which"));
    	Form form = HtmlFormFlowsheetUtil.getFormFromString((String) model.get("formId"));
    	Object o = request.getAttribute("org.openmrs.portlet.patientId");
    	Patient p = null;
        if (o != null) {
            Integer patientId = (Integer) o;
            p = Context.getPatientService().getPatient(patientId);     
        }       
        if (p != null){
	    	List<Encounter> allEncs = Context.getEncounterService().getEncountersByPatient(p);
	    	Encounter theOne = null;
	    	if (allEncs != null && allEncs.size() > 0){
	        	if (which == Which.FIRST) {
	        		for (Encounter e : allEncs) {
	        			if (OpenmrsUtil.nullSafeEquals(form, e.getForm())) {
	        				theOne = e;
	        				break;
	        			}
	        		}
	        	} else if (which == Which.LAST) {
	        		for (ListIterator<Encounter> iter = allEncs.listIterator(allEncs.size()); iter.hasPrevious(); ) {
	        			Encounter e = iter.previous();
	        			if (OpenmrsUtil.nullSafeEquals(form, e.getForm())) {
	        				theOne = e;
	        				break;
	        			}
	        		}
	        	}
	    	}
	    	model.put("encounterToDisplay", theOne); 
        }
    	  
    	model.put("which", which);
    }

}
