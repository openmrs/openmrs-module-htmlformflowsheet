package org.openmrs.module.htmlformflowsheet.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/module/htmlformflowsheet/closeDialog")
public class CloseDialogController {

    @ModelAttribute("dialogToClose")
    public String getDialogToClose(@RequestParam(value="dialogToClose", required=true) String dialogToClose){
        return dialogToClose;
    }
       
    
    @RequestMapping(method = RequestMethod.GET)
    public void getRequest(ModelMap model){
        //just show the page
    }
    
}
