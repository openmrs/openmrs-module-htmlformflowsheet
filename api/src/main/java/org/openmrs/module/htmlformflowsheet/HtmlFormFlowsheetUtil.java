package org.openmrs.module.htmlformflowsheet;

import java.util.*;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.Drug;
import org.openmrs.Encounter;
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
import org.springframework.context.ApplicationContext;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class HtmlFormFlowsheetUtil {

	protected static final Log log = LogFactory.getLog(HtmlFormFlowsheetUtil.class);

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
						Form form = getFormFromString(tagString[2]);
						ecct.setFormId(form.getFormId());
						if (form.getEncounterType() != null)
							ecct.setEncounterTypeId(form.getEncounterType().getEncounterTypeId());
						ecct.setTitle(tagString[1]);
						config.addTab(ecct);

					} else if (tagString.length == 4){
						//Single Form
						SingleHtmlFormPatientChartTab shpt = new SingleHtmlFormPatientChartTab();
						Form form = getFormFromString(tagString[2]);
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

	/**
	 *
	 * Get form by formId or UUID
	 *
	 * @param s
	 * @return either form, or new Form()
	 */
	public static Form getFormFromString(String s){
		if (s != null && !s.equals("")){
			Form form = Context.getFormService().getFormByUuid(s);
			if (form != null)
				return form;
			else {
				try {
					Integer formId = Integer.valueOf(s);
					form = Context.getFormService().getForm(formId);
				} catch (Exception ex){
					ex.printStackTrace();
					throw new RuntimeException("The value you have passed in for formId:" + s + " is invalid");
				}
			}
			return form;
		}
		return new Form();
	}

	/**
	 *
	 * Simple util for getting a formId as a String, avoiding potential null pointer exceptions.
	 *
	 * @param form
	 * @return
	 */
	public static String getFormIdAsString(Form form){
		if (form == null || form.getFormId() == null)
			return "";
		else
			return form.getFormId().toString();
	}

	public static Set<Concept> getAllConceptsUsedInHtmlForm(Form form){

		HtmlForm htmlform = Context.getService(HtmlFormEntryService.class).getHtmlFormByForm(form);


		Set<Concept> concepts = new HashSet<Concept>();
		try {
			 FormEntrySession session = new FormEntrySession(HtmlFormEntryUtil.getFakePerson(), htmlform);
			 HtmlFormSchema schema = session.getContext().getSchema();
			 for (HtmlFormField hff : schema.getAllFields()){
				 findConceptsHelper(hff, concepts);
			 }


		} catch (Exception ex){
			throw new RuntimeException(ex);
		}
		return concepts;
	}

	private static void findConceptsHelper(HtmlFormField hff, Set<Concept> concepts){
		 if (hff instanceof ObsField){
			 ObsField of = (ObsField) hff;
			 Concept c = of.getQuestion();
			 if (c != null)
				 concepts.add(c);
			 else if (of.getQuestion() == null) {
				 //TODO:  use of.getQuestions() once new htmlformentry 1.7.4 comes out
			 }
		 }
		 if (hff instanceof ObsGroup){
			 ObsGroup og = (ObsGroup) hff;
			 Concept c = og.getConcept();
			 if (c != null)
				 concepts.add(c);
			 if (og.getChildren() != null){
				 for (HtmlFormField childHff: og.getChildren()){
					 findConceptsHelper(childHff, concepts);
				 }
			 }

		 }
	 }

	public static Set<Drug> getAllDrugsUsedInHtmlForm(Form form){

	 HtmlForm htmlform = Context.getService(HtmlFormEntryService.class).getHtmlFormByForm(form);
	 String xml = htmlform.getXmlData();
	 try {
			FormEntrySession session = new FormEntrySession(HtmlFormEntryUtil.getFakePerson(), htmlform);
			xml = session.createForm(xml); //this applies macros
	 } catch (Exception ex){
		 //pass
	 }
	 xml = xml.replace("&nbsp;", ""); // Hack to get the document to parse to valid xml

	 Set<Drug> drugs = new HashSet<Drug>();
	 try {
		 Document doc = HtmlFormEntryUtil.stringToDocument(xml);
		 NodeList obsnl = doc.getElementsByTagName("drugOrder");

		 if (obsnl != null){
			 for (int i = 0; i < obsnl.getLength(); i++){
				 Node node = obsnl.item(i);
				 NamedNodeMap nnm = node.getAttributes();
				 Node strNode = nnm.getNamedItem("drugNames");
				 if (strNode != null){
					 String drugNamesString = strNode.getNodeValue();
					 StringTokenizer tokenizer = new StringTokenizer(drugNamesString, ",");
					 while (tokenizer.hasMoreElements()) {
						 String drugName = (String) tokenizer.nextElement();
						 Drug drug = null;
						 // pattern to match a uuid, i.e., five blocks of alphanumerics separated by hyphens
						 if (Pattern.compile("\\w+-\\w+-\\w+-\\w+-\\w+").matcher(drugName.trim()).matches()) {
							 drug = Context.getConceptService().getDrugByUuid(drugName.trim());
						 } else {
							 drug = Context.getConceptService().getDrugByNameOrId(drugName.trim());
						 }
						 if (drug != null)
							 drugs.add(drug);
					 }
				 }
			 }
		 }
	 } catch (Exception ex){
		 throw new RuntimeException(ex);
	 }
	 return drugs;
 }
}
