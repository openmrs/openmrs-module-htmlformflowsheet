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
package org.openmrs.module.htmlformflowsheet.activator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.BaseModuleActivator;
import org.openmrs.module.htmlformentry.HtmlFormEntryService;
import org.openmrs.module.htmlformflowsheet.handlers.HtmlFormFlowsheetHandler;

/**
 * This class contains the logic that is run every time this module
 * is either started or shutdown
 */
public class HtmlFormFlowsheetModuleActivator extends BaseModuleActivator {

    private Log log = LogFactory.getLog(this.getClass());

	@Override
	public void started() {
		log.info("htmlformflowsheet module started");
	}

	@Override
	public void contextRefreshed() {
		try {
			HtmlFormEntryService hfes = Context.getService(HtmlFormEntryService.class);
			hfes.addHandler("htmlformflowsheet", new HtmlFormFlowsheetHandler());
			log.info("Successfully registered htmlformflowsheet tag with htmlformentry...");
		}
		catch (Exception e){
			log.error("Failed to register htmlformflowsheet tag with htmlformentry...", e);
		}
	}

	@Override
	public void stopped() {
		try {
			HtmlFormEntryService hfes = Context.getService(HtmlFormEntryService.class);
			hfes.getHandlers().remove("htmlformflowsheet");
			log.info("Unregistered htmlformflowsheet tag with htmlformentry...");
		}
		catch (Exception e) {
			log.error("Failed to unregister htmlformflowsheet tag with htmlformentry...", e);
		}

		log.info("htmlformflowsheet module stopped");
	}
}
