/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.htmlformflowsheet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.module.Activator;
import org.openmrs.module.htmlformentry.HtmlFormEntryService;
import org.openmrs.module.htmlformflowsheet.handler.HtmlFormFlowsheetHandler;
import org.openmrs.module.htmlformflowsheet.web.HtmlFormFlowsheetContextAware;
import org.openmrs.module.htmlformflowsheet.web.util.HtmlFormFlowsheetUtil;
import org.springframework.context.ApplicationContext;


/**
 * This class contains the logic that is run every time this module
 * is either started or shutdown
 */
public class HtmlFormFlowsheetModuleActivator implements Activator, Runnable  {

    private Log log = LogFactory.getLog(this.getClass());

    /**
     * @see org.openmrs.module.Activator#startup()
     */
    public final void shutdown() {
        onShutdown();
    }

    /**
     * @see org.openmrs.module.Activator#shutdown()
     */
    public final void startup() {
        onStartup();
        log.info("Starting HtmlFormFlowsheet");
        Thread contextChecker = new Thread(this);
        contextChecker.start();
    }

    /**
     * @see java.lang.Runnable#run()
     */
    public final void run() {
        // Wait for context refresh to finish

        ApplicationContext ac = null;
        HtmlFormEntryService fs = null;
        try {
            while (ac == null || fs == null) {
                Thread.sleep(30000);
                if (HtmlFormFlowsheetContextAware.getApplicationContext() != null){
                    try{
                        log.info("HtmlFormFlowsheet still waiting for app context and services to load...");
                        ac = HtmlFormFlowsheetContextAware.getApplicationContext();
                        fs = Context.getService(HtmlFormEntryService.class);
                    } catch (APIException apiEx){}
                }
            }
        } catch (InterruptedException ex) {}
        try {
            Thread.sleep(10000);
            // Start new OpenMRS session on this thread
            Context.openSession();
            Context.addProxyPrivilege("View Concept Classes");
            Context.addProxyPrivilege("View Concepts");
            Context.addProxyPrivilege("Manage Concepts");
            Context.addProxyPrivilege("View Global Properties");
            Context.addProxyPrivilege("Manage Global Properties");
            Context.addProxyPrivilege("SQL Level Access");
            Context.addProxyPrivilege("View Forms");
            Context.addProxyPrivilege("Manage Forms");
            onLoad(fs);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException("Could not pre-load concepts " + ex);
        } finally {
            Context.removeProxyPrivilege("SQL Level Access");
            Context.removeProxyPrivilege("View Concept Classes");
            Context.removeProxyPrivilege("View Concepts");
            Context.removeProxyPrivilege("Manage Concepts");
            Context.removeProxyPrivilege("View Global Properties");
            Context.removeProxyPrivilege("Manage Global Properties");
            Context.removeProxyPrivilege("View Forms");
            Context.removeProxyPrivilege("Manage Forms");
            Context.closeSession();
            
            log.info("Finished loading htmlformflowsheet metadata.");
        }   
    }
    
    /**
     * Called when module is being started
     */
    protected void onStartup() {        
    }
    
    /**
     * Called after module application context has been loaded. There is no authenticated
     * user so all required privileges must be added as proxy privileges
     */
    protected void onLoad(HtmlFormEntryService hfes) {     
        HtmlFormFlowsheetUtil.configureTabsAndLinks();
        hfes.addHandler("htmlformflowsheet", new HtmlFormFlowsheetHandler());
        log.info("registering htmlformflowsheet tag...");
    }
    
    /**
     * Called when module is being shutdown
     */
    protected void onShutdown() {       
    }

	
}
