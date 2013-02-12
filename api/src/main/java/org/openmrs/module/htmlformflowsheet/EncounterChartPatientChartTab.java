package org.openmrs.module.htmlformflowsheet;

/**
 * A tab that shows a table of existing encounters, and an optional "Add another" button
 * that adds another via HTML Form
 */
public class EncounterChartPatientChartTab extends PatientChartTab {

	private Integer encounterTypeId;
	private Integer formId;
	private boolean showAddAnother = true;
	private Boolean showAllWithEncType = false;
	
	public EncounterChartPatientChartTab() {
	}
	
	public Integer getEncounterTypeId() {
    	return encounterTypeId;
    }
	
    public void setEncounterTypeId(Integer encounterTypeId) {
    	this.encounterTypeId = encounterTypeId;
    }
	
    public Integer getFormId() {
    	return formId;
    }
	
    public void setFormId(Integer formId) {
    	this.formId = formId;
    }

    public boolean isShowAddAnother() {
    	return showAddAnother;
    }

    public void setShowAddAnother(boolean showAddAnother) {
    	this.showAddAnother = showAddAnother;
    }

    public Boolean getShowAllWithEncType() {
        return showAllWithEncType;
    }

    public void setShowAllWithEncType(Boolean showAllWithEncType) {
        this.showAllWithEncType = showAllWithEncType;
    }
    
}
