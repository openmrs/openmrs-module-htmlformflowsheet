package org.openmrs.module.htmlformflowsheet;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

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

public class HtmlFormFlowsheetServiceTest extends BaseModuleContextSensitiveTest {
    
    protected static final String XML_DATASET_PATH = "org/openmrs/module/htmlformflowsheet/include/";
    
    protected static final String XML_CONCEPT_DATASET_PATH = XML_DATASET_PATH + "RegressionTest-data.xml";

    @Override
    public Boolean useInMemoryDatabase(){
        return true;
    }
    
    @Before
    public void loadConcepts() throws Exception {
        executeDataSet(XML_CONCEPT_DATASET_PATH);
    }

    @Test
    public void shouldReturnAllDrugOrdersCorrectly() throws Exception {
        HtmlFormFlowsheetService hffs = Context.getService(HtmlFormFlowsheetService.class);
        Assert.assertTrue(hffs != null);
        
        Patient patient = Context.getPatientService().getPatient(2);
        if (patient == null)
            Assert.assertFalse(true);
        Encounter encounter = new Encounter();
        encounter.setCreator(Context.getAuthenticatedUser());
        encounter.setDateCreated(new Date());
        encounter.setEncounterDatetime(new Date());
        encounter.setEncounterType(Context.getEncounterService().getEncounterType(1));
        encounter.setVoided(false);
        encounter.setPatient(patient);
        encounter.setUuid(java.util.UUID.randomUUID().toString());
        encounter.setLocation(Context.getLocationService().getLocation(1));
        encounter.setProvider(Context.getUserService().getUser(1));
        
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.DAY_OF_MONTH, -30);
        Date thirtyDaysAgo = cal.getTime();
        
        Drug drug1 = Context.getConceptService().getDrug(2); //2,3,11 are valid
        DrugOrder dord = new DrugOrder();
        dord.setConcept(drug1.getConcept());
        dord.setDrug(drug1);
        dord.setCreator(Context.getAuthenticatedUser());
        dord.setVoided(false);
        dord.setStartDate(thirtyDaysAgo);
        encounter.addOrder(dord);
        
        Encounter enc = Context.getEncounterService().saveEncounter(encounter);
        Integer dordId = enc.getOrders().iterator().next().getOrderId();
        List<Encounter> encs = new ArrayList<Encounter>();
        
        //should return 3 drug orders for drug1 with empty encs
        List<DrugOrder> doList = hffs.getDrugOrders(patient, Collections.singleton(drug1), encs, false);
        //System.out.println("SIZE IS" + doList.size());
        for (DrugOrder dos : doList){
            //System.out.println(dos.getOrderId() + " patientID " + dos.getPatient().getPatientId());
        }
        Assert.assertTrue(doList.size() == 3);
        
        //should reutnr 3 drug orders for drug1 with null encs
        doList = hffs.getDrugOrders(patient, Collections.singleton(drug1), null, false);
        //System.out.println("SIZE IS" + doList.size());
        for (DrugOrder dos : doList){
            //System.out.println(dos.getOrderId() + " patientID " + dos.getPatient().getPatientId());
        }
        Assert.assertTrue(doList.size() == 3);
        
        //should return 2 drugOrders for drug 1, because we're excluding encounter created above.
        encs.add(encounter);
        doList = hffs.getDrugOrders(patient, Collections.singleton(drug1), encs, false);
        //System.out.println("SIZE IS" + doList.size());
        for (DrugOrder dos : doList){
            //System.out.println(dos.getOrderId() + " patientID " + dos.getPatient().getPatientId());
        }
        Assert.assertTrue(doList.size() == 2);
        
        
      //should return 4 drugOrders for all drugs, because we're excluding encounter created above.
        doList = hffs.getDrugOrders(patient, null, encs, false);
        //System.out.println("SIZE IS" + doList.size());
        for (DrugOrder dos : doList){
            //System.out.println(dos.getOrderId() + " patientID " + dos.getPatient().getPatientId());
        }
        Assert.assertTrue(doList.size() == 4);
        
        //should return 5 drugOrders = all drugOrders
        doList = hffs.getDrugOrders(patient, null, null, false);
        //System.out.println("SIZE IS" + doList.size());
        for (DrugOrder dos : doList){
            //System.out.println(dos.getOrderId() + " patientID " + dos.getPatient().getPatientId());
        }
        Assert.assertTrue(doList.size() == 5);
        
      //should return 4 drugOrders = all drugOrders, but exclude the voided one.
        dord = (DrugOrder) Context.getOrderService().getOrder(dordId);
        Context.getOrderService().voidOrder(dord, "");
        
        doList = hffs.getDrugOrders(patient, null, null, false);
        //System.out.println("SIZE IS" + doList.size());
        for (DrugOrder dos : doList){
            //System.out.println(dos.getOrderId() + " patientID " + dos.getPatient().getPatientId());
        }
        Assert.assertTrue(doList.size() == 4);
    }   
    
    @Test
    public void shouldReturnAllActiveDrugOrdersCorrectly() throws Exception {
        HtmlFormFlowsheetService hffs = Context.getService(HtmlFormFlowsheetService.class);
        Assert.assertTrue(hffs != null);
        
        Patient patient = Context.getPatientService().getPatient(2);
        if (patient == null)
            Assert.assertFalse(true);
        Encounter encounter = new Encounter();
        encounter.setCreator(Context.getAuthenticatedUser());
        encounter.setDateCreated(new Date());
        encounter.setEncounterDatetime(new Date());
        encounter.setEncounterType(Context.getEncounterService().getEncounterType(1));
        encounter.setVoided(false);
        encounter.setPatient(patient);
        encounter.setUuid(java.util.UUID.randomUUID().toString());
        encounter.setLocation(Context.getLocationService().getLocation(1));
        encounter.setProvider(Context.getUserService().getUser(1));
        
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.DAY_OF_MONTH, -30);
        Date thirtyDaysAgo = cal.getTime();
        cal.add(Calendar.DAY_OF_MONTH, 15);
        Date fifteenDaysAgo = cal.getTime();
        cal.add(Calendar.DAY_OF_MONTH, 45);
        Date inFourWeeks = cal.getTime(); 
        
        Drug drug1 = Context.getConceptService().getDrug(2); //2,3,11 are valid
        DrugOrder dord = new DrugOrder();
        dord.setConcept(drug1.getConcept());
        dord.setDrug(drug1);
        dord.setCreator(Context.getAuthenticatedUser());
        dord.setVoided(false);
        dord.setStartDate(thirtyDaysAgo);
        encounter.addOrder(dord);
        Encounter enc = Context.getEncounterService().saveEncounter(encounter);
        Integer dordId = enc.getOrders().iterator().next().getOrderId();
        
        
        
        List<Encounter> encs = new ArrayList<Encounter>();
        
        List<Order> allOrders = Context.getOrderService().getOrdersByPatient(patient);
        //System.out.println("///////   ALL DRUG ORDERS ////////////");
        for (Order o: allOrders){
            if (o instanceof DrugOrder){
                DrugOrder oTmp = (DrugOrder) o;
//                ////System.out.println(oTmp.getOrderId() + " patientID " + oTmp.getPatient().getPatientId() + " drugId " + oTmp.getDrug().getDrugId() + " startDate" 
//                        + oTmp.getStartDate() + " discontinuedDate " + oTmp.getDiscontinuedDate() 
//                        + " autoexpireddate " + oTmp.getAutoExpireDate());
            }
        }
        //System.out.println("//////////////");
        
        //should return the only two 
        List<DrugOrder> doList = hffs.getDrugOrders(patient, Collections.singleton(drug1), encs, true);
        //System.out.println("SIZE IS" + doList.size());
        for (DrugOrder dos : doList){
//            //System.out.println(dos.getOrderId() + " patientID " + dos.getPatient().getPatientId() + " drugId " + dos.getDrug().getDrugId() + " startDate" 
//                    + dos.getStartDate() + " discontinuedDate " + dos.getDiscontinuedDate() 
//                    + " autoexpireddate " + dos.getAutoExpireDate());
        }
        Assert.assertTrue(doList.size() == 2);
        
        //now, lets auto-expire the drug, so list should go down to 1
        DrugOrder dor = (DrugOrder) Context.getOrderService().getOrder(dordId);
//        //System.out.println(dor.getOrderId() + " patientID " + dor.getPatient().getPatientId() + " drugId " + dor.getDrug().getDrugId() + " startDate" 
//                + dor.getStartDate() + " discontinuedDate " + dor.getDiscontinuedDate() 
//                + " autoexpireddate " + dor.getAutoExpireDate() + " uuid = " + dor.getUuid());
        
        dor.setAutoExpireDate(fifteenDaysAgo);
        
        doList = hffs.getDrugOrders(patient, Collections.singleton(drug1), encs, true);
        //System.out.println("SIZE IS" + doList.size());
        for (DrugOrder dos : doList){
//            //System.out.println(dos.getOrderId() + " patientID " + dos.getPatient().getPatientId() + " drugId " + dos.getDrug().getDrugId() + " startDate" 
//                    + dos.getStartDate() + " discontinuedDate " + dos.getDiscontinuedDate() 
//                    + " autoexpireddate " + dos.getAutoExpireDate());
        }
        Assert.assertTrue(doList.size() == 1);
        
        
        //now lets push the autoexpire date into the future for the same drug order and the count should go back to 2
      //now, lets auto-expire the drug, so list should go down to 1
        dor = (DrugOrder) Context.getOrderService().getOrder(dordId);
//        //System.out.println(dor.getOrderId() + " patientID " + dor.getPatient().getPatientId() + " drugId " + dor.getDrug().getDrugId() + " startDate" 
//                + dor.getStartDate() + " discontinuedDate " + dor.getDiscontinuedDate() 
//                + " autoexpireddate " + dor.getAutoExpireDate() + " uuid = " + dor.getUuid());
        
        dor.setAutoExpireDate(inFourWeeks);
        
        doList = hffs.getDrugOrders(patient, Collections.singleton(drug1), encs, true);
        //System.out.println("SIZE IS" + doList.size());
        for (DrugOrder dos : doList){
//            //System.out.println(dos.getOrderId() + " patientID " + dos.getPatient().getPatientId() + " drugId " + dos.getDrug().getDrugId() + " startDate" 
//                    + dos.getStartDate() + " discontinuedDate " + dos.getDiscontinuedDate() 
//                    + " autoexpireddate " + dos.getAutoExpireDate());
        }
        Assert.assertTrue(doList.size() == 2);
    }   
    
    
//    @Test
//    public void shouldReturnEncountersInAscendingThenDescendingOrder() throws Exception {
//    	  Encounter e1 = new Encounter();
//          e1.setEncounterDatetime(new Date(0));
//          
//          Encounter e2 = new Encounter();
//          e2.setEncounterDatetime(new Date(100000000));
//          
//          Encounter e3 = new Encounter();
//          e3.setEncounterDatetime(new Date(888888888));
//          
//          List<Encounter> encs = new ArrayList<Encounter>();
//          //out of order
//          encs.add(e2);
//          encs.add(e1);
//          encs.add(e3);
//         
//          //asc
//          Collections.sort(encs, new Comparator<Encounter>() {	
//          	public int compare(Encounter enc1, Encounter enc2){
//        		return enc1.getEncounterDatetime().compareTo(enc2.getEncounterDatetime());
//        	}
//          });
//          
//          for (Encounter e : encs){
//        	  System.out.println(e.getEncounterDatetime());
//          }
//          
//          //desc
//          Collections.sort(encs, new Comparator<Encounter>() {	
//            	public int compare(Encounter enc1, Encounter enc2){
//          		return enc2.getEncounterDatetime().compareTo(enc1.getEncounterDatetime());
//          	}
//           });
//            
//            for (Encounter e : encs){
//          	  System.out.println(e.getEncounterDatetime());
//            }
//    }
    
    
    
}
