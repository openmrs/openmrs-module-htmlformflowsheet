package org.openmrs.module.htmlformflowsheet.web.html;

import java.util.LinkedHashMap;
import java.util.Map;

import org.openmrs.module.web.extension.AdministrationSectionExt;

public class FormListLinks extends AdministrationSectionExt {

    @Override
    public Map<String, String> getLinks() {
        // TODO Auto-generated method stub
        Map<String, String> ret = new LinkedHashMap<String, String>();
        ret.put("module/htmlformflowsheet/patientWidgetChart.list", "CAT IV Treatment Card Flowsheet");
        return ret;
    }

    @Override
    public String getTitle() {
        return "CAT IV Treatment Card Flowsheet";
    }

}