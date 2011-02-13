package org.openmrs.module.htmlformflowsheet.impl;

import java.util.List;
import java.util.Set;

import org.openmrs.Drug;
import org.openmrs.DrugOrder;
import org.openmrs.Encounter;
import org.openmrs.Patient;
import org.openmrs.module.htmlformflowsheet.HtmlFormFlowsheetService;
import org.openmrs.module.htmlformflowsheet.db.HtmlFormFlowsheetDAO;
import org.springframework.transaction.annotation.Transactional;

public class HtmlFormFlowsheetImpl implements HtmlFormFlowsheetService {

    
    private HtmlFormFlowsheetDAO dao;

    
    public HtmlFormFlowsheetDAO getDao() {
        return dao;
    }


    public void setDao(HtmlFormFlowsheetDAO dao) {
        this.dao = dao;
    }


    @Transactional(readOnly = true)
    public List<DrugOrder> getDrugOrders(Patient patient, Set<Drug> drugs, List<Encounter> encountersToExclude, boolean active){
        return dao.getDrugOrders(patient, drugs, encountersToExclude, active);
    }
    
}
