package org.openmrs.module.htmlformflowsheet;

import java.util.List;
import java.util.Set;

import org.openmrs.Drug;
import org.openmrs.DrugOrder;
import org.openmrs.Encounter;
import org.openmrs.Patient;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface HtmlFormFlowsheetService {
    
    @Transactional(readOnly = true)
    public List<DrugOrder> getDrugOrders(Patient patient, Set<Drug> drugs, List<Encounter> encountersToExclude, boolean active);
    
}
