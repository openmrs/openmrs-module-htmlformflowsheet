package org.openmrs.module.htmlformflowsheet.web.controller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.openmrs.web.controller.PortletController;

/**
 * Custom controller for the encounterChart portlet
 */
public class EncounterChartPortletController extends PortletController {

    
    //TODO: this will fail if you have multiple obs groups that have concepts in common...
 
	/**
     * @see org.openmrs.web.controller.PortletController#populateModel(javax.servlet.http.HttpServletRequest, java.util.Map)
     */
    @Override
    protected void populateModel(HttpServletRequest request, Map<String, Object> model) {
    	String uuid = (String) model.get("portletUUID");
    	model.put("portletUUID", uuid.replace("-", ""));
    	

    	
    }
	
}
