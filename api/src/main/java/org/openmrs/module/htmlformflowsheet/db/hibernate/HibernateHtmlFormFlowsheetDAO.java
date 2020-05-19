package org.openmrs.module.htmlformflowsheet.db.hibernate;

import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Query;
import org.openmrs.Drug;
import org.openmrs.DrugOrder;
import org.openmrs.Encounter;
import org.openmrs.Patient;
import org.openmrs.api.db.hibernate.DbSessionFactory;
import org.openmrs.module.htmlformflowsheet.db.HtmlFormFlowsheetDAO;

public class HibernateHtmlFormFlowsheetDAO implements HtmlFormFlowsheetDAO {
        
    protected static final Log log = LogFactory.getLog(HibernateHtmlFormFlowsheetDAO.class);
    
    /**
     * Hibernate session factory
     */
    private DbSessionFactory sessionFactory;
    public DbSessionFactory getSessionFactory() {
        return sessionFactory;
    }
    public void setSessionFactory(DbSessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @SuppressWarnings("unchecked")
    public List<DrugOrder> getDrugOrders(Patient patient, Set<Drug> drugs, List<Encounter> encountersToExclude, boolean active){
        StringBuilder queryString = new StringBuilder();
        queryString.append(" from DrugOrder dor where dor.patient = :patient ");
        if (drugs != null && drugs.size() > 0) {
            queryString.append(" and dor.drug in (:drugs) ");
        }
        if (encountersToExclude != null && encountersToExclude.size() > 0) {
            queryString.append(" and (dor.encounter not in (:encounters) OR dor.encounter is null) ");
        }
        queryString.append(" and dor.voided = 0 ");
        if (active) {
            queryString.append(" and dor.dateActivated <= current_timestamp() and ");
            queryString.append(" (dor.dateStopped >= current_timestamp() OR dor.dateStopped is null) and ");
            queryString.append(" (dor.autoExpireDate >= current_timestamp() OR dor.autoExpireDate is null) ");
        }
        queryString.append(" order by dor.dateActivated asc ");
            
        Query query = sessionFactory.getCurrentSession().createQuery(queryString.toString());
        query.setParameter("patient", patient);
        if (drugs != null && drugs.size() > 0){
            query.setParameterList("drugs", drugs);
        }
        if (encountersToExclude != null && encountersToExclude.size() > 0){
           query.setParameterList("encounters", encountersToExclude);
        }
        return query.list();
    }
}
