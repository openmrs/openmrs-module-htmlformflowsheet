package org.openmrs.module.htmlformflowsheet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Drug;
import org.openmrs.DrugOrder;
import org.openmrs.Encounter;
import org.openmrs.Order;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * The standard test dataset contains a patient (id = 2), with an encounter (id=6), that contains 12 orders
 * 8 of these orders are drug orders.  4 of these are for Triomune-30 (drug=2).  2 of these are active.  None are voided.
 *
 * We add add additional encounter for this patient with an additional Triomune-30 order from the past, no longer active.
 */
public class HtmlFormFlowsheetServiceTest extends BaseModuleContextSensitiveTest {
    
    protected static final String XML_DATASET_PATH = "org/openmrs/module/htmlformflowsheet/include/";
    protected static final String XML_TEST_DATASET_NAME = XML_DATASET_PATH + "htmlformflowsheet-data.xml";

    @Autowired
    @Qualifier("htmlFormFlowsheetService")
    HtmlFormFlowsheetService hffs;

    @Override
    public Boolean useInMemoryDatabase(){
        return true;
    }

    Patient patient;
    Drug triomune30;
    Encounter encounterWithSingleOrder;
    Order singleOrder;
    List<Encounter> emptyEncList = new ArrayList<Encounter>();
    List<Encounter> singleEncList;
    
    @Before
    public void loadConcepts() {
        executeDataSet(XML_TEST_DATASET_NAME);
        patient = Context.getPatientService().getPatient(2);
        triomune30 = Context.getConceptService().getDrug(2);
        encounterWithSingleOrder = Context.getEncounterService().getEncounter(10001);
        singleOrder = Context.getOrderService().getOrder(20002);
        emptyEncList = new ArrayList<Encounter>();
        singleEncList = Collections.singletonList(encounterWithSingleOrder);
    }

    @Test
    public void shouldReturnAllDrugOrdersCorrectly() {

        Assert.assertNotNull(hffs);
        Assert.assertNotNull(patient);

        int numOrders = 9; // 8 drug orders in standard test data set + 1 in module data set for patient 2
        int numTrimune30 = 5; // 4 triomune3- in standard test data set + 1 in module data set for patient 2

        // an empty encounter list should be treated as no constraint
        List<DrugOrder> doList = hffs.getDrugOrders(patient, Collections.singleton(triomune30), emptyEncList, false);
        Assert.assertEquals(numTrimune30, doList.size());

        // a null encounter list should be treated as no constraint
        doList = hffs.getDrugOrders(patient, Collections.singleton(triomune30), null, false);
        Assert.assertEquals(numTrimune30, doList.size());

        // excluding encounters should exlude the orders in those encounters
        doList = hffs.getDrugOrders(patient, Collections.singleton(triomune30), singleEncList, false);
        Assert.assertEquals(numTrimune30-1, doList.size());

        // should return all
        doList = hffs.getDrugOrders(patient, null, null, false);
        Assert.assertEquals(numOrders, doList.size());

        // should exclude orders in passed encounters
        doList = hffs.getDrugOrders(patient, null, singleEncList, false);
        Assert.assertEquals(numOrders-1, doList.size());

        // should exclude oided
        Context.getOrderService().voidOrder(singleOrder, "Voiding to test exclusion");
        doList = hffs.getDrugOrders(patient, null, null, false);
        Assert.assertEquals(numOrders-1, doList.size());
    }

    /**
     * This tests auto-expire on 2 orders (which are DISCONTINUE orders)
     * This tests date-stopped on 2 orders (which were the one's that the DISCONTINUE orders affected)
     * The remaining 2 orders for Triomune-30 are active.  This is allowed as their CARE_SETTING differs (1, 2)
     */
    @Test
    public void shouldReturnAllActiveDrugOrdersCorrectly() {
        // should only return active orders for Triomune-30
        Set<Drug> drugs = Collections.singleton(triomune30);
        List<DrugOrder> doList = hffs.getDrugOrders(patient, drugs, null, true);
        Assert.assertEquals(2, doList.size());
    }
}
