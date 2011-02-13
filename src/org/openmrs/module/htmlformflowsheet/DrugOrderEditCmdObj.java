package org.openmrs.module.htmlformflowsheet;

import org.openmrs.DrugOrder;

public class DrugOrderEditCmdObj {
    private DrugOrder drugOrder;
    private String dialogToClose;
    
    public DrugOrderEditCmdObj(){}

    public DrugOrder getDrugOrder() {
        return drugOrder;
    }

    public void setDrugOrder(DrugOrder drugOrder) {
        this.drugOrder = drugOrder;
    }

    public String getDialogToClose() {
        return dialogToClose;
    }

    public void setDialogToClose(String dialogToClose) {
        this.dialogToClose = dialogToClose;
    }
    
}
