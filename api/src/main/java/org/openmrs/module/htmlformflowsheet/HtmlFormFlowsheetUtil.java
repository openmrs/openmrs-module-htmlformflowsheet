package org.openmrs.module.htmlformflowsheet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.Drug;
import org.openmrs.Encounter;
import org.openmrs.Form;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.Module;
import org.openmrs.module.ModuleFactory;
import org.openmrs.module.htmlformentry.FormEntryContext;
import org.openmrs.module.htmlformentry.FormEntrySession;
import org.openmrs.module.htmlformentry.HtmlForm;
import org.openmrs.module.htmlformentry.HtmlFormEntryService;
import org.openmrs.module.htmlformentry.HtmlFormEntryUtil;
import org.openmrs.module.htmlformentry.schema.HtmlFormField;
import org.openmrs.module.htmlformentry.schema.HtmlFormSchema;
import org.openmrs.module.htmlformentry.schema.HtmlFormSection;
import org.openmrs.module.htmlformentry.schema.ObsField;
import org.openmrs.module.htmlformentry.schema.ObsGroup;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.servlet.http.HttpSession;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

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

	public static String getCurrentHtmlFormEntryVersion() {
		Module hfeMod = ModuleFactory.getStartedModuleById("htmlformentry");
		if (hfeMod != null) {
			return hfeMod.getVersion();
		}
		return null;
	}

	public static FormEntrySession createFormEntrySession(Form form) {
		HtmlForm htmlForm = Context.getService(HtmlFormEntryService.class).getHtmlFormByForm(form);
		return createFormEntrySession(null, null, null, htmlForm, null, null);
	}

	/**
	 * Utility method to retrieve a new FormEntrySession from htmlformentry
	 * This is particularly useful since the API to construct a FormEntrySession
	 * has changed over versions, and this method encapsulates the logic to deal with this
	 */
	public static FormEntrySession createFormEntrySession(Patient patient, Encounter encounter,
														  FormEntryContext.Mode mode, HtmlForm htmlForm,
														  Location defaultLocation, HttpSession session) {
		try {
			if (patient == null) {
				patient = HtmlFormEntryUtil.getFakePerson();
			}
			if (mode == null) {
				mode = FormEntryContext.Mode.ENTER;
			}

			// In HFE 2.0.3, a non-backwards-compatible change was made to the FormEntrySession constructor,
			// which we need to work around here

			if (getCurrentHtmlFormEntryVersion().compareTo("2.0.5") >= 0) {
				Class[] argTypes = {Patient.class, Encounter.class, FormEntryContext.Mode.class, HtmlForm.class, Location.class, HttpSession.class, boolean.class, boolean.class};
				Constructor c = FormEntrySession.class.getDeclaredConstructor(argTypes);
				return (FormEntrySession) c.newInstance(patient, encounter, mode, htmlForm, defaultLocation, session, false, false);
			}
			else if (getCurrentHtmlFormEntryVersion().compareTo("2.0.3") >= 0) {
				Class[] argTypes = {Patient.class, Encounter.class, FormEntryContext.Mode.class, HtmlForm.class, Location.class, HttpSession.class};
				Constructor c = FormEntrySession.class.getDeclaredConstructor(argTypes);
				return (FormEntrySession) c.newInstance(patient, encounter, mode, htmlForm, defaultLocation, session);
			}
			else {
				Class[] argTypes = {Patient.class, Encounter.class, FormEntryContext.Mode.class, HtmlForm.class, Location.class};
				Constructor c = FormEntrySession.class.getDeclaredConstructor(argTypes);
				return (FormEntrySession) c.newInstance(patient, encounter, mode, htmlForm, defaultLocation);
			}
		}
		catch (Exception e) {
			throw new RuntimeException("Unable to construct new FormEntrySession", e);
		}
	}

    // This is copied from HFE version 2.6, which returns sets in we need lists in order

    public static List<HtmlFormField> getAllFields(HtmlFormSchema schema) {
        return getAllFieldsRecursiveSectionHelper(schema, new ArrayList<HtmlFormField>(), null);
    }

    private static List<HtmlFormField> getAllFieldsRecursiveSectionHelper(HtmlFormSchema schema, List<HtmlFormField> fields, HtmlFormSection section) {
        getAllFieldsRecursiveFieldHelper(fields, section == null ? getFieldsDirectlyOnSchema(schema) : section.getFields());
        for (HtmlFormSection s : section == null ? schema.getSections() : section.getSections()) {
            getAllFieldsRecursiveSectionHelper(schema, fields, s);
        }
        return fields;
    }

    private static List<HtmlFormField> getAllFieldsRecursiveFieldHelper(List<HtmlFormField> fields, List<HtmlFormField> fieldsToAdd) {
        if (fieldsToAdd != null) {
            for (HtmlFormField f : fieldsToAdd) {
                fields.add(f);
                if (f instanceof ObsGroup) {
                    getAllFieldsRecursiveFieldHelper(fields, ((ObsGroup) f).getChildren());
                }
            }
        }
        return fields;
    }

    // In HFE 2.6, field were added to top level of schema, but not before, so we need to use reflection for this for now
    private static List<HtmlFormField> getFieldsDirectlyOnSchema(HtmlFormSchema schema) {
        try {
            Method getFieldsMethod = HtmlFormSchema.class.getMethod("getFields");
            return (List<HtmlFormField>)getFieldsMethod.invoke(schema);
        }
        catch (NoSuchMethodException e) {
            // Assume that this is due to previous version not having this method
            return new ArrayList<HtmlFormField>();
        }
        catch (Exception e) {
            throw new IllegalArgumentException("Unable to retrieve fields from htmlform schema", e);
        }
    }

	public static Set<Concept> getAllConceptsUsedInHtmlForm(Form form){

		Set<Concept> concepts = new HashSet<Concept>();
		try {
			 FormEntrySession session = createFormEntrySession(form);
			String htmlToDisplay = session.getHtmlToDisplay();
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
		 FormEntrySession session = createFormEntrySession(form);
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
