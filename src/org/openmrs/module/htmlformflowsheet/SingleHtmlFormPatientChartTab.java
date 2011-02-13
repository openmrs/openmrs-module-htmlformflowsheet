package org.openmrs.module.htmlformflowsheet;



/**
 * Displays a single html form, in view mode
 */
public class SingleHtmlFormPatientChartTab extends PatientChartTab {

	public enum Which {
		NONE,
		LAST,
		FIRST
	}
	
	private Integer formId;
	private Which which;
	private Integer defaultEncounterTypeId;
	
	public SingleHtmlFormPatientChartTab() {
	}

	
    /**
     * @return the formId
     */
    public Integer getFormId() {
    	return formId;
    }

	
    /**
     * @param formId the formId to set
     */
    public void setFormId(Integer formId) {
    	this.formId = formId;
    }

	
    /**
     * @return the which
     */
    public Which getWhich() {
    	return which;
    }

	
    /**
     * @param which the which to set
     */
    public void setWhich(Which which) {
    	this.which = which;
    }


    public Integer getDefaultEncounterTypeId() {
        return defaultEncounterTypeId;
    }


    public void setDefaultEncounterTypeId(Integer defaultEncounterTypeId) {
        this.defaultEncounterTypeId = defaultEncounterTypeId;
    }

    
    
	
}
