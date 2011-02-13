package org.openmrs.module.htmlformflowsheet.db.hibernate;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.openmrs.Drug;
import org.openmrs.DrugOrder;
import org.openmrs.Encounter;
import org.openmrs.Patient;
import org.openmrs.module.htmlformflowsheet.db.HtmlFormFlowsheetDAO;

public class HibernateHtmlFormFlowsheetDAO implements HtmlFormFlowsheetDAO {
        
    protected static final Log log = LogFactory.getLog(HibernateHtmlFormFlowsheetDAO.class);
    
    /**
     * Hibernate session factory
     */
    private SessionFactory sessionFactory;
    
    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @SuppressWarnings("unchecked")
    public List<DrugOrder> getDrugOrders(Patient patient, Set<Drug> drugs, List<Encounter> encountersToExclude, boolean active){
            String queryString = "";
            queryString += "from DrugOrder dor where dor.patient = :patient ";
            if (drugs != null && drugs.size() > 0)
                queryString += " and dor.drug in (:drugs) "; 
            if (encountersToExclude != null && encountersToExclude.size() > 0)
                queryString += " and (dor.encounter not in (:encounters) OR dor.encounter is null) ";
            queryString += " and dor.voided = 0 ";
            
            if (active){
                queryString += " and dor.startDate <= current_timestamp() and (dor.discontinuedDate >= current_timestamp() OR dor.discontinuedDate is null) and (dor.autoExpireDate >= current_timestamp() OR dor.autoExpireDate is null) ";
            }
            
            queryString += " order by dor.startDate asc ";
            
            Query query = sessionFactory.getCurrentSession().createQuery(queryString);
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
