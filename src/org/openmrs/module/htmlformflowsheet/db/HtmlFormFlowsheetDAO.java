package org.openmrs.module.htmlformflowsheet.db;

import java.util.List;
import java.util.Set;

import org.openmrs.Drug;
import org.openmrs.DrugOrder;
import org.openmrs.Encounter;
import org.openmrs.Patient;

public interface HtmlFormFlowsheetDAO {

    public List<DrugOrder> getDrugOrders(Patient patient, Set<Drug> drugs, List<Encounter> encountersToExclude, boolean active);
    
}
