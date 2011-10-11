package org.openmrs.module.htmlformflowsheet.web.dwr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Cohort;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientProgram;
import org.openmrs.Person;
import org.openmrs.PersonAttributeType;
import org.openmrs.Program;
import org.openmrs.api.PatientService;
import org.openmrs.api.PersonService;
import org.openmrs.api.ProgramWorkflowService;
import org.openmrs.api.context.Context;
import org.openmrs.web.dwr.PatientListItem;

public class HtmlFormFlowhseetFindPatient {
    protected final Log log = LogFactory.getLog(getClass());
    
    public Collection findPatients(String searchValue, boolean restrictByProgram, Integer programId, boolean includeVoided) {
        
        Collection<Object> patientList = new Vector<Object>();

        Integer userId = -1;
        if (Context.isAuthenticated())
            userId = Context.getAuthenticatedUser().getUserId();
        
        PatientService ps = Context.getPatientService();
        List<Patient> patients= ps.getPatients(searchValue);
        patientList = new Vector<Object>(patients.size());

        //TODO:  sqlQuery to make this super-fast?
        if (restrictByProgram){
        	try {
	        	List<Patient> restrictedPatients = new ArrayList<Patient>();
	        	ProgramWorkflowService pws = Context.getProgramWorkflowService();
	    		Program prog = pws.getProgram(Integer.valueOf(programId));
	    		if (programId == null)
	    			throw new RuntimeException("HtmlFormFlowsheet find patient can't load program " + programId);
	    		
	    		Cohort c = new Cohort(patients);
	    		List<PatientProgram> pps = pws.getPatientPrograms(c, Collections.singletonList(prog));
	    		for (PatientProgram pp : pps){
	    			//if lazy loading is going to be necessary?:
	    			//restrictedPatients.add(Context.getPatientService().getPatient(pp.getPatient().getPatientId()));
	    			if (!restrictedPatients.contains(pp.getPatient()))
	    				restrictedPatients.add(pp.getPatient());
	    		}
	    		patients = restrictedPatients;
        	} catch (Exception ex){
        		log.error("Could not restrict patient search list by program in htmlformflowsheet patient lookup", ex);
        	}
        }
        
        for (Patient p : patients) {
            PatientListItem patientListItem = new PatientListItem(p);

            try {
                for (PatientIdentifier pi : p.getActiveIdentifiers()){
                	if (pi.isPreferred()){
                		 patientListItem.setIdentifier(pi.getIdentifier());
                		 break;
                	}                		
                }
                if (patientListItem.getIdentifier() ==  null || patientListItem.getIdentifier().equals("") && p.getActiveIdentifiers() != null && p.getActiveIdentifiers().size() > 0)
                	patientListItem.setIdentifier(p.getIdentifiers().iterator().next().getIdentifier());
            } catch (Exception ex){
            	ex.printStackTrace(System.out);
            }
            
        	patientList.add(patientListItem);
        }
                   
        
        // I'm taking out the 'minimal patients returned'
        // If this needs to be smarter, there should be a better findPatients(...)
                
        return patientList;
    }
    
    public Collection findPeople(String searchValue, String dateString, boolean includeVoided) {
        
        Collection<Object> patientList = new Vector<Object>();

        Integer userId = -1;
        if (Context.isAuthenticated())
            userId = Context.getAuthenticatedUser().getUserId();
        PersonService ps = Context.getPersonService();
        List<Person> patients;
        
        patients = ps.getPeople(searchValue, false);
        patientList = new Vector<Object>(patients.size());
        String persAttTypeString = Context.getAdministrationService().getGlobalProperty("mdrtb.treatment_supporter_person_attribute_type");
        PersonAttributeType pat = Context.getPersonService().getPersonAttributeTypeByName(persAttTypeString);
        for (Person p : patients){
            if (p.getAttributes(pat.getPersonAttributeTypeId()) != null && !p.getAttributes(pat.getPersonAttributeTypeId()).isEmpty())
                patientList.add(new HtmlFormFlowsheetPersonListItem(p, dateString));
        }        
        return patientList;
    }
    
 public Collection findAllPeople(String searchValue, String dateString, boolean includeVoided) {
        
        Collection<Object> patientList = new Vector<Object>();

        Integer userId = -1;
        if (Context.isAuthenticated())
            userId = Context.getAuthenticatedUser().getUserId();
        PersonService ps = Context.getPersonService();
        List<Person> patients;
        
        patients = ps.getPeople(searchValue, false);
        patientList = new Vector<Object>(patients.size());

        for (Person p : patients){
                patientList.add(new HtmlFormFlowsheetPersonListItem(p, dateString));
        }        
        return patientList;
    }
}
