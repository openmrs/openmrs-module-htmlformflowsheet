package org.openmrs.module.htmlformflowsheet.web.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.Drug;
import org.openmrs.DrugOrder;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Form;
import org.openmrs.Obs;
import org.openmrs.Order;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.htmlformentry.FormEntryContext.Mode;
import org.openmrs.module.htmlformentry.FormEntrySession;
import org.openmrs.module.htmlformentry.HtmlForm;
import org.openmrs.module.htmlformentry.HtmlFormEntryService;
import org.openmrs.module.htmlformentry.schema.DrugOrderAnswer;
import org.openmrs.module.htmlformentry.schema.DrugOrderField;
import org.openmrs.module.htmlformentry.schema.HtmlFormField;
import org.openmrs.module.htmlformentry.schema.HtmlFormSchema;
import org.openmrs.module.htmlformentry.schema.ObsField;
import org.openmrs.module.htmlformentry.schema.ObsFieldAnswer;
import org.openmrs.module.htmlformentry.schema.ObsGroup;
import org.openmrs.module.htmlformentry.web.controller.HtmlFormEntryController;
import org.openmrs.module.htmlformflowsheet.HtmlFormFlowsheetService;
import org.openmrs.module.htmlformflowsheet.HtmlFormFlowsheetUtil;
import org.springframework.ui.ModelMap;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

public class HtmlEncounterChartContentController implements Controller {

    private Log log = LogFactory.getLog(this.getClass());
    
    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        
        ModelMap model = new ModelMap();
        Integer patientId;
        try {
            patientId = Integer.valueOf(request.getParameter("patientId"));
        } catch (Exception ex){
            log.warn("htmlformflowsheet pulling patientId out of session");
            String formInProgressKey = HtmlFormEntryController.FORM_IN_PROGRESS_KEY;
            FormEntrySession fes = (FormEntrySession)  HtmlFormEntryController.getVolatileUserData(formInProgressKey);
            if (fes == null || fes.getPatient() == null)
            		throw new RuntimeException("Unable to pull patientId out of URL.  Please verify patientId in the url you're using to access this page.");
            patientId = fes.getPatient().getPatientId();
        }
        Integer encounterTypeId = Integer.valueOf(request.getParameter("encounterTypeId"));
        String formId = request.getParameter("formId");
        String addAnotherButtonLabel = (String) request.getParameter("addAnotherButtonLabel");
        if (addAnotherButtonLabel != null && !addAnotherButtonLabel.equals("") && !addAnotherButtonLabel.equals("null")){
            model.put("addAnotherButtonLabel", addAnotherButtonLabel); 
        }    
        String showHtmlFormInsteadStr = (String) request.getParameter("showHtmlFormInstead");
        String showHtmlFormInstead = "false";
        if (showHtmlFormInsteadStr != null && showHtmlFormInsteadStr.equals("true"))
            showHtmlFormInstead = "true";
        model.put("showHtmlFormInstead", showHtmlFormInstead);
        String windowHeight = (String) request.getParameter("windowHeight");
        if (windowHeight == null || windowHeight.equals("") || windowHeight.equals("null"))
            windowHeight = "400";
        model.put("windowHeight", Integer.valueOf(windowHeight));

		if ("true".equals((String)request.getParameter("showProvider"))) {
        	model.put("showProvider", true);
        	model.put("providerHeader", (String)request.getParameter("providerHeader"));
        }
        else {
        	model.put("showProvider", false);
        }
        
        Form form = HtmlFormFlowsheetUtil.getFormFromString(formId);
        formId = HtmlFormFlowsheetUtil.getFormIdAsString(form);
        String portletUUID = request.getParameter("portletUUID");
        String view = request.getParameter("view");
        model.put("view", view);
        String readOnly = request.getParameter("readOnly");
        if (readOnly == null || readOnly.equals(""))
            readOnly="false";
        model.put("readOnly", readOnly);
        model.put("personId", patientId);
        model.put("portletUUID", portletUUID.replace("-", ""));
        Patient patient = Context.getPatientService().getPatient(patientId);
        model.put("patient", patient);
        //TODO:  read-only version?
        model.put("formId", formId);
        model.put("encounterTypeId", encounterTypeId);
        model.put("encounterType", Context.getEncounterService().getEncounterType(encounterTypeId));
        model.put("conceptsToShow", request.getParameter("conceptsToShow"));

        
        //LOAD ALL ENCS
        List<Encounter> encs = new ArrayList<Encounter>();
        String showAllEncsWithEncType = request.getParameter("showAllEncsWithEncType");
        if (showAllEncsWithEncType == null || !showAllEncsWithEncType.equals("true")){
            //if showAllEncsWithEncType is false or null then restrict encounters by formId
            encs = HtmlFormFlowsheetUtil.getEncountersForPatient(patient, form,null);
            model.put("showAllEncsWithEncType", "false");
        }   else if ("*".equals(model.get("encounterTypeId"))) {
            //show all encounters
            encs = Context.getEncounterService().getEncountersByPatient(patient);
            model.put("showAllEncsWithEncType", "true");
        } else {
            // map from encounterTypeId to list of encounters of that type 
            EncounterType et = Context.getEncounterService().getEncounterType(encounterTypeId);
            encs = HtmlFormFlowsheetUtil.getEncountersForPatient(patient, null,et);
            model.put("showAllEncsWithEncType", "true");
        } 

        //TODO:  TRIM ENCOUNTER ON NO MATCH IN FORM.

        // now figure out which concepts we want to display as columns
        Set<Map<Concept,String>> concepts = new LinkedHashSet<Map<Concept,String>>();
        List<DrugOrderField> searchDrugs = new ArrayList<DrugOrderField>();
        Map<Drug, String> drugNames = new HashMap<Drug, String>();
        Map<String,String> conceptAnswers = new HashMap<String,String>(); //key is questionConceptId.answerConceptId
        {
            String conceptsToShowString = (String) model.get("conceptsToShow");
            if (StringUtils.hasText(conceptsToShowString)) {
                for (String idAsString : conceptsToShowString.split(",")) {
					Concept c = Context.getConceptService().getConceptByUuid(idAsString);
					if (c == null) {
						c = Context.getConceptService().getConcept(idAsString);
					}
                    Map<Concept,String> conceptAndNameString = new HashMap<Concept,String>();
                    conceptAndNameString.put(c, c.getShortestName(Context.getLocale(), false).getName());
                    concepts.add(conceptAndNameString);
                }
            } else if (form != null) {
                HtmlForm htmlForm = Context.getService(HtmlFormEntryService.class).getHtmlFormByForm(form);
                Patient p = (Patient) model.get("patient");
                FormEntrySession fes = null;
                try {
					fes = HtmlFormFlowsheetUtil.createFormEntrySession(p, null, Mode.VIEW, htmlForm, null, null);
                    String htmlToDisplay = fes.getHtmlToDisplay();
                    HtmlFormSchema schema = fes.getContext().getSchema();
                    for (HtmlFormField field : HtmlFormFlowsheetUtil.getAllFields(schema)) {
                        fieldHelper(field, concepts, conceptAnswers, searchDrugs, drugNames);
                    }
                } catch (Exception ex) {
                    log.error("Failure inspecting form " + form.getFormId() + " schema for obs " + ex);
                    ex.printStackTrace();
                } finally {
                    fes = null;
                }
            }
        }
        
        // now go through the Obs in the encounters we want to view and find the ones that will go in the table
        Map<Encounter, Map<Integer, List<Obs>>> encounterToObsMap = new HashMap<Encounter, Map<Integer,List<Obs>>>();
        //encounter, conceptId, obs
        for (Encounter e : encs) {
            Map<Integer, List<Obs>> holder = new HashMap<Integer, List<Obs>>();
            encounterToObsMap.put(e, holder);
            for (Map<Concept,String> c : concepts) {
                holder.put(c.keySet().iterator().next().getConceptId(), new ArrayList<Obs>());
            }
            for (Obs o : e.getObs()) {
                Concept c = o.getConcept();
                for (Map<Concept, String> m : concepts){
                    if (m.keySet().contains(c)) {
                        holder.get(c.getConceptId()).add(o);
                        break;
                    }
                }
                //TODO:  this is wrong.
                for (Obs oInner : o.getGroupMembers()){
                    Concept cInner = oInner.getConcept();
                    holder.get(cInner.getConceptId()).add(o);
                }
            }
        }
        
        //use searchDrugs to build list of drugOrders, order by start_date asc, exclude encounters already in the list
        Set<Drug> drugSet = new HashSet<Drug>();
        for (DrugOrderField df : searchDrugs){
            for (DrugOrderAnswer doa : df.getDrugOrderAnswers()){
                if (doa.getDrug() != null)
                    drugSet.add(doa.getDrug());
            }
        }
        List<DrugOrder> drugOrders = null;
        if (drugSet.size() > 0)
            drugOrders = Context.getService(HtmlFormFlowsheetService.class).getDrugOrders(patient, drugSet, encs, false);
        //pull DrugOrders out of encounterList to initialize any lazy loading
        for (Encounter eTmp : encs){
            for (Order o : eTmp.getOrders()){
                if (o instanceof DrugOrder){
                    DrugOrder doTmp = (DrugOrder) o;
                }    
            }
        }
        
        List<Encounter> dummyEncs = new ArrayList<Encounter>();
        if (drugOrders != null){
            for (DrugOrder doTmp : drugOrders){
                
                Encounter encDummy = null;
                if (doTmp.getEncounter() != null)
                    encDummy = Context.getEncounterService().getEncounter(doTmp.getEncounter().getEncounterId());
    
                if (encDummy == null){
                    encDummy = new Encounter();
                    encDummy.setEncounterDatetime(doTmp.getDateActivated());
                    Context.evictFromSession(encDummy);
                }
                encDummy.addOrder(doTmp);
                dummyEncs.add(encDummy);
            }    
        }
        //reorder encounters
        if (dummyEncs.size() > 0){
            //if not in encounterListForChart, add dummy encounters (or actual) with the encounters (dangerous)?
            encs.addAll(dummyEncs);
        }
        
        //order encounters according to gp
        encs = HtmlFormFlowsheetUtil.sortEncountersAccordingToGp(encs);
        
        Map<Encounter, Set<DrugOrder>> encsToDrugOrders = new HashMap<Encounter, Set<DrugOrder>>();
        for (Encounter enc: encs){
            for (Order o : enc.getOrders()){
                if (o instanceof DrugOrder && !o.isVoided()){
                    if (!encsToDrugOrders.containsKey(enc))
                        encsToDrugOrders.put(enc, new LinkedHashSet<DrugOrder>());
                    Set<DrugOrder> oSet = encsToDrugOrders.get(enc);
                    oSet.add((DrugOrder) o);
                }
            }
        }
        model.put("drugSet", drugSet);//HERE -- pass these through to the non-encounter drug order page...
        model.put("encounterToDrugOrderMap", encsToDrugOrders);
        model.put("drugNames", drugNames);
        model.put("encounterListForChart", encs);
        model.put("encounterChartConcepts", concepts);
        model.put("encounterChartObs", encounterToObsMap);
        
        //check to see if there's a match (i.e. its worth showing this row because there's a match on an obs or a drug order.)
        Map<Encounter,Boolean> foundEncounters = new HashMap<Encounter,Boolean>();
        for (Encounter enc: encs){
            Boolean found = false;
            for (Map<Concept,String> m : concepts){
                Concept c = m.keySet().iterator().next();
                if (c.getConceptId() != null && encounterToObsMap.get(enc) != null && encounterToObsMap.get(enc).get(c.getConceptId()).size() > 0){
                    found = true;
                    break;
                } 
            }
            //match on drugOrders:
            if (enc.getOrders() != null && found == false){
                Set<Order> oSet = enc.getOrders();
                for (Order o : oSet){
                    if (o instanceof DrugOrder && !o.isVoided()){
                        for (Map<Concept,String> m : concepts){
                            Concept c = m.keySet().iterator().next();
                            DrugOrder drugO = (DrugOrder) o;
                            if (drugO.getDrug() != null && c.getUuid().contains(drugO.getDrug().getName())){
                                found = true;
                                break;
                            }
                        }    
                    }
                }
            }
            foundEncounters.put(enc, found);
        }
        model.put("foundEncounters", foundEncounters);
        model.put("conceptAnswers", conceptAnswers);
        return new ModelAndView("/module/htmlformflowsheet/encounterChartContent", "model", model);
    }
    
    /**
     * Gets all Concepts out of this field (and subfields) and adds them to the set.
     * TODO: handle obs groups
     * 
     * @param field
     * @param concepts
     */
    private void fieldHelper(HtmlFormField field, Set<Map<Concept,String>> concepts, Map<String,String> conceptAnswers, List<DrugOrderField> searchDrugs, Map<Drug, String> drugNames) {
        if (field instanceof ObsField) {
            ObsField of = (ObsField) field;
            Map<Concept,String> conceptAndNameString = new HashMap<Concept,String>();
            String name = of.getName();
            if (name != null)
                name = name.toUpperCase();
            conceptAndNameString.put(of.getQuestion(),name);
            concepts.add(conceptAndNameString);
            List<ObsFieldAnswer> answerList = of.getAnswers();
            if (answerList != null && answerList.size() > 0){
                for (ObsFieldAnswer ofa : answerList){
                    String answName = ofa.getDisplayName();
                    if (ofa.getConcept() != null){
						conceptAnswers.put(of.getQuestion().getConceptId() + "." + ofa.getConcept().getConceptId(),answName);
                    } 
                }
            }
        } else if (field instanceof ObsGroup){
            ObsGroup og = (ObsGroup) field;
            for (HtmlFormField ofInner:og.getChildren()){
                fieldHelper(ofInner, concepts, conceptAnswers, searchDrugs, drugNames);
            }
        } else if (field instanceof DrugOrderField){
            DrugOrderField dof = (DrugOrderField) field;
            searchDrugs.add(dof);
            {
                //SUPER HACK -- adding drug matching key to concept UUID field.  These Concepts are never meant to be saved...
                Map<Concept,String> answConceptAndNameString = new HashMap<Concept,String>();
                Concept dummyC = new Concept();
                Context.evictFromSession(dummyC);
                String tmp = "";
                //adding display names to string to match to
                for (DrugOrderAnswer doa :dof.getDrugOrderAnswers()){
                    tmp = tmp + doa.getDrug().getName() + "-";
                    //load up drug to displayName object:
                    drugNames.put(doa.getDrug(), doa.getDisplayName());
                }
                dummyC.setUuid(tmp);
                answConceptAndNameString.put(dummyC,Context.getMessageSourceService().getMessage("DrugOrder.drug"));
                //added to concepts to create a row.  Concept.UUID will contain list of drug names.
                concepts.add(answConceptAndNameString);
            }
            
        } else {
            log.debug(field.getClass() + " not yet implemented");
        }
    }
}
