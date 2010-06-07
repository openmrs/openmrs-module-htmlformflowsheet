package org.openmrs.module.htmlformflowsheet.web.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.htmlformflowsheet.web.PatientChartConfiguration;
import org.openmrs.module.htmlformflowsheet.web.util.HtmlFormFlowsheetUtil;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

/**
 * Controller for Patient Charts. Should be configured by passing it a PatientChartConfiguration bean
 */
public class PatientChartController implements Controller {
	
	private String formView = "/module/htmlformflowsheet/patientChart";
	private PatientChartConfiguration configuration;
	
	public PatientChartController() {super();}
	
	@SuppressWarnings("unchecked")
	public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
	    if (configuration == null) {
			throw new RuntimeException("You need to provide a configuration");
		}
		Integer patientId = null;
		try{
		    patientId = Integer.valueOf(request.getParameter("patientId"));
		} catch (Exception ex){
		    throw new RuntimeException("You must provide a patientId.");
		}
		Patient patient = Context.getPatientService().getPatient(patientId);
		
		Integer selectTab = 0;
		try {
		    selectTab = Integer.valueOf(request.getParameter("selectTab"));
		} catch (Exception ex){}
		
		
		if (configuration.getTabs() == null || configuration.getTabs().size() == 0){
		    HtmlFormFlowsheetUtil.configureTabsAndLinks();
		}

		ModelMap model = new ModelMap();
		String fullPage = "true";
		String readOnly = "false";
		
		String configParam = request.getParameter("configuration");
		String linksParam = request.getParameter("links");
		String fullPageStr = request.getParameter("fullPage");
		String readOnlyStr = request.getParameter("readOnly");
		
		if (fullPageStr != null && fullPageStr.equals("false"))
		    fullPage = "false";
		if (readOnlyStr != null && readOnlyStr.equals("true"))
		    readOnly = "true";
		if (configParam != null && !configParam.equals("")){
		    //here's the url-override of the configuration
		    PatientChartConfiguration  pcc = HtmlFormFlowsheetUtil.buildGenericConfigurationFromStrings(configParam, linksParam);
		    model.put("tabs", pcc.getTabs());
            model.put("links", pcc.getLinks());
            model.put("configuration", configParam);
		} else {
		    //here's the default config that lives in the controller, and is directly configurable with global properties
		    model.put("tabs", configuration.getTabs());
	        model.put("links", configuration.getLinks());
		}
		model.put("fullPage", fullPage);
		model.put("readOnly", readOnly);
		model.put("patientId", patientId);
		model.put("patient", patient);
		model.put("selectTab", selectTab);
		
		return new ModelAndView(formView, "model", model); 
	}
	
	public void setConfiguration(PatientChartConfiguration configuration) {
		this.configuration = configuration;
	}
	
	public PatientChartConfiguration getConfiguration() {
		return configuration;
	}
	
    public String getFormView() {
    	return formView;
    }
	
    public void setFormView(String formView) {
    	this.formView = formView;
    }

}
