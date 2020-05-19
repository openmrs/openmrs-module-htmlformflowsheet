package org.openmrs.module.htmlformflowsheet.web.controller;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.openmrs.Form;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.api.context.Context;
import org.openmrs.module.htmlformflowsheet.HtmlFormFlowsheetUtil;
import org.openmrs.util.PrivilegeConstants;
import org.openmrs.web.controller.PortletController;
import org.springframework.web.servlet.ModelAndView;

/**
 * Custom controller for the encounterChart portlet
 * NOTE:  if you uncomment everything here, it gives you the generic openmrs portletController, but without all of the hacky session usage
 */
public class EncounterChartPortletController extends PortletController {
    
    @Override
    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException,
    IOException {
        
//        AdministrationService as = Context.getAdministrationService();
//        ConceptService cs = Context.getConceptService();
        
        // find the portlet that was identified in the openmrs:portlet taglib
        Object uri = request.getAttribute("javax.servlet.include.servlet_path");
        String portletPath = "";
        Map<String, Object> model =  new HashMap<String, Object>();

        
        if (uri != null) {
            //long timeAtStart = System.currentTimeMillis();
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
                    if (Context.hasPrivilege(PrivilegeConstants.GET_PATIENTS)) {
                        Patient p = Context.getPatientService().getPatient(patientId);
                        model.put("patient", p);
                        
                        //TODO: Could we restrict these by encounterType?? add encounters if this user can view them
                        
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
                    
//                    if (Context.hasPrivilege(OpenmrsConstants.PRIV_VIEW_RELATIONSHIPS)) {
//                        List<Relationship> relationships = new ArrayList<Relationship>();
//                        relationships.addAll(Context.getPersonService().getRelationshipsByPerson(p));
//                        Map<RelationshipType, List<Relationship>> relationshipsByType = new HashMap<RelationshipType, List<Relationship>>();
//                        for (Relationship rel : relationships) {
//                            List<Relationship> list = relationshipsByType.get(rel.getRelationshipType());
//                            if (list == null) {
//                                list = new ArrayList<Relationship>();
//                                relationshipsByType.put(rel.getRelationshipType(), list);
//                            }
//                            list.add(rel);
//                        }
//                        
//                        model.put("personRelationships", relationships);
//                        model.put("personRelationshipsByType", relationshipsByType);
//                    }
                }
            }
            
            // if an encounter id is available, put "encounter" and "encounterObs" in the model
//            o = request.getAttribute("org.openmrs.portlet.encounterId");
//            if (o != null && !model.containsKey("encounterId")) {
//                if (!model.containsKey("encounter")) {
//                    if (Context.hasPrivilege(OpenmrsConstants.PRIV_VIEW_ENCOUNTERS)) {
//                        Encounter e = Context.getEncounterService().getEncounter((Integer) o);
//                        model.put("encounter", e);
//                        if (Context.hasPrivilege(OpenmrsConstants.PRIV_VIEW_OBS))
//                            model.put("encounterObs", e.getObs());
//                    }
//                    model.put("encounterId", (Integer) o);
//                }
//            }
//            
//            // if a user id is available, put "user" in the model
//            o = request.getAttribute("org.openmrs.portlet.userId");
//            if (o != null) {
//                if (!model.containsKey("user")) {
//                    if (Context.hasPrivilege(OpenmrsConstants.PRIV_VIEW_USERS)) {
//                        User u = Context.getUserService().getUser((Integer) o);
//                        model.put("user", u);
//                    }
//                    model.put("userId", (Integer) o);
//                }
//            }
            
            // if a list of patient ids is available, make a patientset out of it
//            o = request.getAttribute("org.openmrs.portlet.patientIds");
//            if (o != null && !"".equals(o) && !model.containsKey("patientIds")) {
//                if (!model.containsKey("patientSet")) {
//                    Cohort ps = new Cohort((String) o);
//                    model.put("patientSet", ps);
//                    model.put("patientIds", (String) o);
//                }
//            }
            
//            o = model.get("conceptIds");
//            if (o != null && !"".equals(o)) {
//                if (!model.containsKey("conceptMap")) {
//                    log.debug("Found conceptIds parameter: " + o);
//                    Map<Integer, Concept> concepts = new HashMap<Integer, Concept>();
//                    Map<String, Concept> conceptsByStringIds = new HashMap<String, Concept>();
//                    String conceptIds = (String) o;
//                    String[] ids = conceptIds.split(",");
//                    for (String cId : ids) {
//                        try {
//                            Integer i = Integer.valueOf(cId);
//                            Concept c = cs.getConcept(i);
//                            concepts.put(i, c);
//                            conceptsByStringIds.put(i.toString(), c);
//                        }
//                        catch (Exception ex) {}
//                    }
//                    model.put("conceptMap", concepts);
//                    model.put("conceptMapByStringIds", conceptsByStringIds);
//                }
//            }
            
            populateModel(request, model);
            //log.debug(portletPath + " took " + (System.currentTimeMillis() - timeAtStart) + " ms");
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
        
       
        String formId = (String) model.get("formId");
        Form form = HtmlFormFlowsheetUtil.getFormFromString(formId);
        model.put("formId", form.getFormId());
        
        String showAllEncsWithEncType = "false";
        try {
            showAllEncsWithEncType = (String) model.get("showAllEncsWithEncType");
        } catch (Exception ex){}
            model.put("showAllEncsWithEncType", showAllEncsWithEncType);
        
        
        return new ModelAndView(portletPath, "model", model);
        
    }

        
	
}
