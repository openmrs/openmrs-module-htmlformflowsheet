package org.openmrs.module.htmlformflowsheet.web.utils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.Drug;
import org.openmrs.Form;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.module.htmlformentry.FormEntrySession;
import org.openmrs.module.htmlformentry.HtmlForm;
import org.openmrs.module.htmlformentry.HtmlFormEntryService;
import org.openmrs.module.htmlformentry.HtmlFormEntryUtil;
import org.openmrs.module.htmlformentry.schema.HtmlFormField;
import org.openmrs.module.htmlformentry.schema.HtmlFormSchema;
import org.openmrs.module.htmlformentry.schema.ObsField;
import org.openmrs.module.htmlformentry.schema.ObsGroup;
import org.openmrs.module.htmlformflowsheet.*;
import org.openmrs.module.htmlformflowsheet.web.controller.PatientChartController;
import org.springframework.context.ApplicationContext;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class HtmlFormFlowsheetWebUtils {

    
    private static Log log = LogFactory.getLog(HtmlFormFlowsheetWebUtils.class);
    
    /**
     * Get the patient PatientChartConfiguration out of the session.
     * the PatientChartConfiguration is a property of the controller
     * 
     * @return
     */
    public static PatientChartConfiguration getConfiguration() {
        try {
			List<PatientChartController> l = Context.getRegisteredComponents(PatientChartController.class);
			if (!l.isEmpty()) {
				return l.get(0).getConfiguration();
			}
        } catch (Exception e) {
            log.error("An error occurred while trying to get Patient Chart Configuration", e);
        }
		return null;
    }

 /**
  * 
  * Automatically configure the module based on the global properties.
  *
  */
 public static void configureTabsAndLinks(){
     try {
         PatientChartConfiguration config = HtmlFormFlowsheetWebUtils.getConfiguration();
         configureTabsAndLinks(config);
     } catch (APIException apiEx){
         
     }
 }

	/**
	 *
	 * Method that will generically return a PatientChartConfiguration from the config strings
	 * This is for URL-parameters override of default configuration that usually lives in the controller.
	 *
	 * @param gp
	 * @param linksGP
	 * @return
	 */
	public static PatientChartConfiguration buildGenericConfigurationFromStrings(String gp, String linksGP){
		PatientChartConfiguration config = new PatientChartConfiguration();
		configureTabsAndLinks(config, gp, linksGP);
		return config;
	}

	/**
	 *
	 * Grabs the configuration global properties and configures the tab layout and links for the module
	 *
	 * @param config
	 */
	public static void configureTabsAndLinks(PatientChartConfiguration config){

		String gp = Context.getAdministrationService().getGlobalProperty("htmlformflowsheet.configuration");
		String linksGP = Context.getAdministrationService().getGlobalProperty("htmlformflowsheet.links");

		if (config != null)
			configureTabsAndLinks(config, gp, linksGP);
		else
			log.error("Unable to configure HtmlFormFlowsheet -- configuration object is null");

	}

	/**
	 *
	 * Helper class that does the configuration work
	 *
	 * @param config
	 * @param gp
	 * @param linksGP
	 */
	public static void configureTabsAndLinks(PatientChartConfiguration config, String gp, String linksGP){
		if (gp != null && !"".equals(gp)){
			config.getTabs().clear();
			for (StringTokenizer st = new StringTokenizer(gp, "|"); st.hasMoreTokens(); ) {
				String thisTag = st.nextToken().trim();
				String[] tagString = thisTag.split(":");
				if (tagString.length == 3){
					//Flowsheet
					EncounterChartPatientChartTab ecct = new EncounterChartPatientChartTab();
					ecct.setShowAddAnother(true);
					Form form = HtmlFormFlowsheetUtil.getFormFromString(tagString[2]);
					ecct.setFormId(form.getFormId());
					if (form.getEncounterType() != null)
						ecct.setEncounterTypeId(form.getEncounterType().getEncounterTypeId());
					ecct.setTitle(tagString[1]);
					config.addTab(ecct);

				} else if (tagString.length == 4){
					//Single Form
					SingleHtmlFormPatientChartTab shpt = new SingleHtmlFormPatientChartTab();
					Form form = HtmlFormFlowsheetUtil.getFormFromString(tagString[2]);
					shpt.setFormId(form.getFormId());
					shpt.setDefaultEncounterTypeId(form.getEncounterType().getEncounterTypeId());
					shpt.setTitle(tagString[1]);
					shpt.setWhich(SingleHtmlFormPatientChartTab.Which.valueOf(tagString[3]));
					config.addTab(shpt);
				}
			}
		}
		if (linksGP != null && !linksGP.equals("")){
			config.getLinks().clear();
			for (StringTokenizer st = new StringTokenizer(linksGP, "|"); st.hasMoreTokens(); ) {
				String thisTag = st.nextToken().trim();
				String[] tagString = thisTag.split(":");
				if (tagString.length == 2){
					String name = tagString[0];
					String link = tagString[1];
					config.addLink(name, link);
				}
			}
		}
	}
}
