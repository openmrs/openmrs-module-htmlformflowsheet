package org.openmrs.module.htmlformflowsheet.web;

/**
 * Configuration for a tab on a patient chart
 */
public abstract class PatientChartTab {

	private String title;

	
	public PatientChartTab() { }
	
    public String getTitle() {
    	return title;
    }
	
    public void setTitle(String title) {
    	this.title = title;
    }

}
