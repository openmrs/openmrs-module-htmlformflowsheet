package org.openmrs.module.htmlformflowsheet;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * <pre>
 * Configuration settings for a PatientChartController.
 * Typically a module would create one of these and configure its own copy of a PatientChartController with it.
 * </pre>
 */
public class PatientChartConfiguration {
	
	private List<PatientChartTab> tabs = new ArrayList<PatientChartTab>();
	private Map<String, String> links = new LinkedHashMap<String, String>();
	
	public PatientChartConfiguration() { }
	
    public List<PatientChartTab> getTabs() {
    	return tabs;
    }
	
    public void setTabs(List<PatientChartTab> tabs) {
    	this.tabs = tabs;
    }
    
    public void addTab(PatientChartTab tab){
        this.tabs.add(tab);
    }

    public Map<String, String> getLinks() {
        return links;
    }

    public void setLinks(Map<String, String> links) {
        this.links.putAll(links);
    }
    
    public void addLink(String name, String link){
        this.links.put(name, link);
    }

}
