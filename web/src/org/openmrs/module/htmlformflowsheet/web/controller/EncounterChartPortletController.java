package org.openmrs.module.htmlformflowsheet.web.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.openmrs.Cohort;
import org.openmrs.Concept;
import org.openmrs.ConceptNumeric;
import org.openmrs.DrugOrder;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.Relationship;
import org.openmrs.RelationshipType;
import org.openmrs.User;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.order.RegimenSuggestion;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.web.WebConstants;
import org.openmrs.web.controller.PortletController;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.ModelAndView;

/**
 * Custom controller for the encounterChart portlet
 */
public class EncounterChartPortletController extends PortletController {

    
    //TODO: this will fail if you have multiple obs groups that have concepts in common...
 

    @Override
    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException,
    IOException {
        
        AdministrationService as = Context.getAdministrationService();
        ConceptService cs = Context.getConceptService();
        
        // find the portlet that was identified in the openmrs:portlet taglib
        Object uri = request.getAttribute("javax.servlet.include.servlet_path");
        String portletPath = "";
        Map<String, Object> model =  new HashMap<String, Object>();;

        
        if (uri != null) {
            long timeAtStart = System.currentTimeMillis();
            portletPath = uri.toString();
            
            // Allowable extensions are '' (no extension) and '.portlet'
            if (portletPath.endsWith("portlet"))
                portletPath = portletPath.replace(".portlet", "");
            else if (portletPath.endsWith("jsp"))
                throw new ServletException(
                        "Illegal extension used for portlet: '.jsp'. Allowable extensions are '' (no extension) and '.portlet'");
            
            log.debug("Loading portlet: " + portletPath);
            
            String id = (String) request.getAttribute("org.openmrs.portlet.id");
            String size = (String) request.getAttribute("org.openmrs.portlet.size");
            Map<String, Object> params = (Map<String, Object>) request.getAttribute("org.openmrs.portlet.parameters");
            Map<String, Object> moreParams = (Map<String, Object>) request.getAttribute("org.openmrs.portlet.parameterMap");
            
            model.put("now", new Date());
            model.put("id", id);
            model.put("size", size);
            model.put("locale", Context.getLocale());
            model.put("portletUUID", UUID.randomUUID().toString().replace("-", ""));
            List<String> parameterKeys = new ArrayList<String>(params.keySet());
            model.putAll(params);
            if (moreParams != null) {
                model.putAll(moreParams);
                parameterKeys.addAll(moreParams.keySet());
            }
            model.put("parameterKeys", parameterKeys); // so we can clean these up in the next request
            
            // if there's an authenticated user, put them, and their patient set, in the model
            if (Context.getAuthenticatedUser() != null) {
                model.put("authenticatedUser", Context.getAuthenticatedUser());
            }
            
            Integer personId = null;
            
            // if a patient id is available, put patient data documented above in the model
            Object o = request.getAttribute("org.openmrs.portlet.patientId");
            if (o != null) {
                String patientVariation = "";
                Integer patientId = (Integer) o;
                if (!model.containsKey("patient")) {
                    // we can't continue if the user can't view patients
                    if (Context.hasPrivilege(OpenmrsConstants.PRIV_VIEW_PATIENTS)) {
                        Patient p = Context.getPatientService().getPatient(patientId);
                        model.put("patient", p);
                        
                        // add encounters if this user can view them
                        if (Context.hasPrivilege(OpenmrsConstants.PRIV_VIEW_ENCOUNTERS))
                            model.put("patientEncounters", Context.getEncounterService().getEncountersByPatient(p));
                        
                        if (Context.hasPrivilege(OpenmrsConstants.PRIV_VIEW_OBS)) {
                            List<Obs> patientObs = Context.getObsService().getObservationsByPerson(p);
                            model.put("patientObs", patientObs);
                            Obs latestWeight = null;
                            Obs latestHeight = null;
                            String bmiAsString = "?";
                            try {
                                String weightString = as.getGlobalProperty("concept.weight");
                                ConceptNumeric weightConcept = null;
                                if (StringUtils.hasLength(weightString))
                                    weightConcept = cs.getConceptNumeric(cs.getConcept(Integer.valueOf(weightString))
                                            .getConceptId());
                                String heightString = as.getGlobalProperty("concept.height");
                                ConceptNumeric heightConcept = null;
                                if (StringUtils.hasLength(heightString))
                                    heightConcept = cs.getConceptNumeric(cs.getConcept(Integer.valueOf(heightString))
                                            .getConceptId());
                                for (Obs obs : patientObs) {
                                    if (obs.getConcept().equals(weightConcept)) {
                                        if (latestWeight == null
                                                || obs.getObsDatetime().compareTo(latestWeight.getObsDatetime()) > 0)
                                            latestWeight = obs;
                                    } else if (obs.getConcept().equals(heightConcept)) {
                                        if (latestHeight == null
                                                || obs.getObsDatetime().compareTo(latestHeight.getObsDatetime()) > 0)
                                            latestHeight = obs;
                                    }
                                }
                                if (latestWeight != null)
                                    model.put("patientWeight", latestWeight);
                                if (latestHeight != null)
                                    model.put("patientHeight", latestHeight);
                                if (latestWeight != null && latestHeight != null) {
                                    double weightInKg;
                                    double heightInM;
                                    if (weightConcept.getUnits().equals("kg"))
                                        weightInKg = latestWeight.getValueNumeric();
                                    else if (weightConcept.getUnits().equals("lb"))
                                        weightInKg = latestWeight.getValueNumeric() * 0.45359237;
                                    else
                                        throw new IllegalArgumentException("Can't handle units of weight concept: "
                                                + weightConcept.getUnits());
                                    if (heightConcept.getUnits().equals("cm"))
                                        heightInM = latestHeight.getValueNumeric() / 100;
                                    else if (heightConcept.getUnits().equals("m"))
                                        heightInM = latestHeight.getValueNumeric();
                                    else if (heightConcept.getUnits().equals("in"))
                                        heightInM = latestHeight.getValueNumeric() * 0.0254;
                                    else
                                        throw new IllegalArgumentException("Can't handle units of height concept: "
                                                + heightConcept.getUnits());
                                    double bmi = weightInKg / (heightInM * heightInM);
                                    model.put("patientBmi", bmi);
                                    String temp = "" + bmi;
                                    bmiAsString = temp.substring(0, temp.indexOf('.') + 2);
                                }
                            }
                            catch (Exception ex) {
                                if (latestWeight != null && latestHeight != null)
                                    log.error("Failed to calculate BMI even though a weight and height were found", ex);
                            }
                            model.put("patientBmiAsString", bmiAsString);
                        } else {
                            model.put("patientObs", new HashSet<Obs>());
                        }
                        
                        // information about whether or not the patient has exited care
                        Obs reasonForExitObs = null;
                        String reasonForExitConceptString = as.getGlobalProperty("concept.reasonExitedCare");
                        if (StringUtils.hasLength(reasonForExitConceptString)) {
                            Concept reasonForExitConcept = cs.getConcept(reasonForExitConceptString);
                            if (reasonForExitConcept != null) {
                                List<Obs> patientExitObs = Context.getObsService().getObservationsByPersonAndConcept(p,
                                    reasonForExitConcept);
                                if (patientExitObs != null) {
                                    log.debug("Exit obs is size " + patientExitObs.size());
                                    if (patientExitObs.size() == 1) {
                                        reasonForExitObs = patientExitObs.iterator().next();
                                        Concept exitReason = reasonForExitObs.getValueCoded();
                                        Date exitDate = reasonForExitObs.getObsDatetime();
                                        if (exitReason != null && exitDate != null) {
                                            patientVariation = "Exited";
                                        }
                                    } else {
                                        if (patientExitObs.size() == 0) {
                                            log.debug("Patient has no reason for exit");
                                        } else {
                                            log.error("Too many reasons for exit - not putting data into model");
                                        }
                                    }
                                }
                            }
                        }
                        model.put("patientReasonForExit", reasonForExitObs);
                        
                        if (Context.hasPrivilege(OpenmrsConstants.PRIV_VIEW_ORDERS)) {
                            List<DrugOrder> drugOrderList = Context.getOrderService().getDrugOrdersByPatient(p);
                            model.put("patientDrugOrders", drugOrderList);
                            List<DrugOrder> currentDrugOrders = new ArrayList<DrugOrder>();
                            List<DrugOrder> discontinuedDrugOrders = new ArrayList<DrugOrder>();
                            Date rightNow = new Date();
                            for (Iterator<DrugOrder> iter = drugOrderList.iterator(); iter.hasNext();) {
                                DrugOrder next = iter.next();
                                if (next.isCurrent() || next.isFuture())
                                    currentDrugOrders.add(next);
                                if (next.isDiscontinued(rightNow))
                                    discontinuedDrugOrders.add(next);
                            }
                            model.put("currentDrugOrders", currentDrugOrders);
                            model.put("completedDrugOrders", discontinuedDrugOrders);
                            
                            List<RegimenSuggestion> standardRegimens = Context.getOrderService().getStandardRegimens();
                            if (standardRegimens != null)
                                model.put("standardRegimens", standardRegimens);
                        }
                        
                        if (Context.hasPrivilege(OpenmrsConstants.PRIV_VIEW_PROGRAMS)
                                && Context.hasPrivilege(OpenmrsConstants.PRIV_VIEW_PATIENT_PROGRAMS)) {
                            model.put("patientPrograms", Context.getProgramWorkflowService().getPatientPrograms(p, null,
                                null, null, null, null, false));
                            model.put("patientCurrentPrograms", Context.getProgramWorkflowService().getPatientPrograms(p,
                                null, null, new Date(), new Date(), null, false));
                        }
                        
                        model.put("patientId", patientId);
                        if (p != null) {
                            personId = p.getPatientId();
                            model.put("personId", personId);
                        }
                        
                        model.put("patientVariation", patientVariation);
                    }
                }
            }
            
            // if a person id is available, put person and relationships in the model
            if (personId == null) {
                o = request.getAttribute("org.openmrs.portlet.personId");
                if (o != null) {
                    personId = (Integer) o;
                    model.put("personId", personId);
                }
            }
            if (personId != null) {
                if (!model.containsKey("person")) {
                    Person p = (Person) model.get("patient");
                    if (p == null)
                        p = Context.getPersonService().getPerson(personId);
                    model.put("person", p);
                    
                    if (Context.hasPrivilege(OpenmrsConstants.PRIV_VIEW_RELATIONSHIPS)) {
                        List<Relationship> relationships = new ArrayList<Relationship>();
                        relationships.addAll(Context.getPersonService().getRelationshipsByPerson(p));
                        Map<RelationshipType, List<Relationship>> relationshipsByType = new HashMap<RelationshipType, List<Relationship>>();
                        for (Relationship rel : relationships) {
                            List<Relationship> list = relationshipsByType.get(rel.getRelationshipType());
                            if (list == null) {
                                list = new ArrayList<Relationship>();
                                relationshipsByType.put(rel.getRelationshipType(), list);
                            }
                            list.add(rel);
                        }
                        
                        model.put("personRelationships", relationships);
                        model.put("personRelationshipsByType", relationshipsByType);
                    }
                }
            }
            
            // if an encounter id is available, put "encounter" and "encounterObs" in the model
            o = request.getAttribute("org.openmrs.portlet.encounterId");
            if (o != null && !model.containsKey("encounterId")) {
                if (!model.containsKey("encounter")) {
                    if (Context.hasPrivilege(OpenmrsConstants.PRIV_VIEW_ENCOUNTERS)) {
                        Encounter e = Context.getEncounterService().getEncounter((Integer) o);
                        model.put("encounter", e);
                        if (Context.hasPrivilege(OpenmrsConstants.PRIV_VIEW_OBS))
                            model.put("encounterObs", e.getObs());
                    }
                    model.put("encounterId", (Integer) o);
                }
            }
            
            // if a user id is available, put "user" in the model
            o = request.getAttribute("org.openmrs.portlet.userId");
            if (o != null) {
                if (!model.containsKey("user")) {
                    if (Context.hasPrivilege(OpenmrsConstants.PRIV_VIEW_USERS)) {
                        User u = Context.getUserService().getUser((Integer) o);
                        model.put("user", u);
                    }
                    model.put("userId", (Integer) o);
                }
            }
            
            // if a list of patient ids is available, make a patientset out of it
            o = request.getAttribute("org.openmrs.portlet.patientIds");
            if (o != null && !"".equals(o) && !model.containsKey("patientIds")) {
                if (!model.containsKey("patientSet")) {
                    Cohort ps = new Cohort((String) o);
                    model.put("patientSet", ps);
                    model.put("patientIds", (String) o);
                }
            }
            
            o = model.get("conceptIds");
            if (o != null && !"".equals(o)) {
                if (!model.containsKey("conceptMap")) {
                    log.debug("Found conceptIds parameter: " + o);
                    Map<Integer, Concept> concepts = new HashMap<Integer, Concept>();
                    Map<String, Concept> conceptsByStringIds = new HashMap<String, Concept>();
                    String conceptIds = (String) o;
                    String[] ids = conceptIds.split(",");
                    for (String cId : ids) {
                        try {
                            Integer i = Integer.valueOf(cId);
                            Concept c = cs.getConcept(i);
                            concepts.put(i, c);
                            conceptsByStringIds.put(i.toString(), c);
                        }
                        catch (Exception ex) {}
                    }
                    model.put("conceptMap", concepts);
                    model.put("conceptMapByStringIds", conceptsByStringIds);
                }
            }
            
            populateModel(request, model);
            log.debug(portletPath + " took " + (System.currentTimeMillis() - timeAtStart) + " ms");
        }
        
        portletPath = portletPath.replaceAll("0", "");
        portletPath = portletPath.replaceAll("1", "");
        portletPath = portletPath.replaceAll("2", "");
        portletPath = portletPath.replaceAll("3", "");
        portletPath = portletPath.replaceAll("4", "");
        portletPath = portletPath.replaceAll("5", "");
        portletPath = portletPath.replaceAll("6", "");
        portletPath = portletPath.replaceAll("7", "");
        portletPath = portletPath.replaceAll("8", "");
        portletPath = portletPath.replaceAll("9", "");
        
        String uuid = (String) model.get("portletUUID");
        model.put("portletUUID", uuid.replace("-", ""));
        //TODO:  unpack the portlet request, and verify formId
        String formId = (String) model.get("formId");
        model.put("formId", formId);
        
        return new ModelAndView(portletPath, "model", model);
        
    }

        
	
}
