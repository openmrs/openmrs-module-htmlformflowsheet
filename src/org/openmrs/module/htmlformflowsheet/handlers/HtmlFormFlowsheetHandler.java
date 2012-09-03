package org.openmrs.module.htmlformflowsheet.handlers;

import java.io.PrintWriter;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Encounter;
import org.openmrs.Form;
import org.openmrs.Patient;
import org.openmrs.module.htmlformentry.FormEntryContext.Mode;
import org.openmrs.module.htmlformentry.FormEntrySession;
import org.openmrs.module.htmlformentry.HtmlForm;
import org.openmrs.module.htmlformentry.handler.AbstractTagHandler;
import org.openmrs.module.htmlformentry.handler.AttributeDescriptor;
import org.openmrs.module.htmlformflowsheet.web.utils.HtmlFormFlowsheetWebUtils;
import org.openmrs.web.WebConstants;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * The tag handler for rendering an htmlform within a parent htmlform.
 * 
 * POSSIBLE ATTRIBUTES:
 * formId (required) -- expects the real form Id, not the htmlform ID.  Describes the form to render.
 * sharedEncounter (optional, default = 'false') -- loads all encounters of the same EncounterType, rather than default behavior,
 *              which is to load only Encounters mapped to the form given in formId.  Also, this will cause 
 *              a drop-down to be appended to the display that allows you to select an encounter to append to
 *              when performing a new entry.
 * addAnotherButtonLabel (optional, default = 'Add Another') -- changes the text label for the 'Add Another' button.
 * windowHeight (optional, default = 400) -- allows you to specify the height of the pop-up that you get when
 *              you click the 'Add New' button.
 * showHtmlForm (optional, default = 'false') If you set this to 'true', the htmlformflowsheet default table view of form observations
 *              will be substituted with a Mode.VIEW version of the rendered htmlform itself for each encounter in the table. 
 * readOnly (optional, default = 'false') -- if true, will render the htmlformflowsheet, but without the 
 *              'Add Another' button, or edit links.  in htmlformentry VIEW mode, this is always false.
 * showProvider (optional) -- show an additional column that shows the encounter provider name next to the date column. The value of 
 * 				attribute will be used as the display header for the provider column.
 */
public class HtmlFormFlowsheetHandler  extends AbstractTagHandler {

    /** The logger to use with this class */
    protected final Log log = LogFactory.getLog(getClass());

    protected List<AttributeDescriptor> createAttributeDescriptors() {
		List<AttributeDescriptor> attributeDescriptors = new ArrayList<AttributeDescriptor>();
		attributeDescriptors.add(new AttributeDescriptor("formId", Form.class));
		return Collections.unmodifiableList(attributeDescriptors);
	}
    
    public boolean doStartTag(FormEntrySession session, PrintWriter out, Node parent, Node node) {
        Map<String, String> attributes = new HashMap<String, String>();        
        NamedNodeMap map = node.getAttributes();
        for (int i = 0; i < map.getLength(); ++i) {
            Node attribute = map.item(i);
            attributes.put(attribute.getNodeName(), attribute.getNodeValue());
        }
        
        //formId
        String configuration = null;
        Patient patient = session.getPatient();
        try {
            configuration = attributes.get("formId");
            Form form = HtmlFormFlowsheetWebUtils.getFormFromString(configuration);
            configuration = HtmlFormFlowsheetWebUtils.getFormIdAsString(form);
            if (configuration == null)
                throw new RuntimeException("htmlformflowsheet tag must have a formId attribute in your htmlform xml.");
        } catch (Exception ex){
            throw new IllegalArgumentException("Cannot find formId in "
                    + attributes);
        }
        
        //sharedEncounter
        String showAllWithEncType = null;
        try {
            showAllWithEncType = attributes.get("sharedEncounter");
        } catch (Exception ex){
            
        }
        
        //addAnotherButtonLabel
        String addAnotherButtonLabel = (String) attributes.get("addAnotherButtonLabel");
        try {
            if (!StringUtils.isEmpty(addAnotherButtonLabel)){
                addAnotherButtonLabel = URLEncoder.encode(addAnotherButtonLabel, "UTF-8");;
            }    
        } catch (Exception ex){
            ex.fillInStackTrace();
        }
        
        //for backward-compatibility:  TODO: Get rid of this soon...
        if (StringUtils.isEmpty(addAnotherButtonLabel)){
	        addAnotherButtonLabel = (String) attributes.get("addNewButtonLabel");
	        try {
	            if (!StringUtils.isEmpty(addAnotherButtonLabel)){
	                addAnotherButtonLabel = URLEncoder.encode(addAnotherButtonLabel, "UTF-8");;
	            }    
	        } catch (Exception ex){
	            ex.fillInStackTrace();
	        }
        }
        
        
        //windowHeight
        String windowHeight = (String) attributes.get("windowHeight");
        
        
        String showProvider = "false";
        String providerHeader = null;
        if((String) attributes.get("showProvider") != null)
        {
        	showProvider = "true";
        	providerHeader = (String) attributes.get("showProvider");
        }
        
        //showHtmlForm
        String showHtmlFormInstead = "false";
        String showHtmlFormInsteadStr = (String) attributes.get("showHtmlForm");
        if (!StringUtils.isEmpty(showHtmlFormInsteadStr) && showHtmlFormInsteadStr.equals("true"))
            showHtmlFormInstead = "true";
        
        //readOnly
        //defaults
        String readOnly = "false";
        if (session.getContext().getMode().equals(Mode.VIEW))
            readOnly = "true";
        //explicitly passed in:
        String readOnlyStr = (String) attributes.get("readOnly");
        if (readOnlyStr != null && readOnlyStr.equals("true"))
            readOnly = "true";
        else if (readOnlyStr != null && readOnlyStr.equals("false"))
            readOnly = "false";
        
        //setup the iframe
        if (patient != null){
            StringBuilder sb = new StringBuilder("");
            
            sb.append("<iframe id='iframeFor" + configuration + "' name='iframeFor" + configuration + "'");
            String source = "";
            String encounterTypeAddition = "";
            if (showAllWithEncType != null && showAllWithEncType.equals("true")){
                encounterTypeAddition = "&showAllEncsWithEncType=" + showAllWithEncType;
            }
            if (!session.getContext().getMode().equals(Mode.VIEW)){
                //sb.append("  src='/openmrs/module/htmlformflowsheet/patientWidgetChart.list?fullPage=false&patientId=" + patient.getPatientId() + "&configuration=F:BOO:" + configuration + "'  ");
                source = "'/"+WebConstants.WEBAPP_NAME+"/module/htmlformflowsheet/patientWidgetChart.list?readOnly=" + readOnly + "&fullPage=false&patientId=" + patient.getPatientId() + "&configuration=F:BOO:" + configuration + encounterTypeAddition + "&showHtmlForm=" + showHtmlFormInstead + "&windowHeight=" + windowHeight + "&showProvider=" + showProvider + "&providerHeader=" + providerHeader + "&addAnotherButtonLabel=" + addAnotherButtonLabel + "'";
            } else {
                //sb.append("  src='/openmrs/module/htmlformflowsheet/patientWidgetChart.list?readOnly=true&fullPage=false&patientId=" + patient.getPatientId() + "&configuration=F:BOO:" + configuration + "'  ");
                source = "'/"+WebConstants.WEBAPP_NAME+"/module/htmlformflowsheet/patientWidgetChart.list?readOnly=" + readOnly + "&fullPage=false&patientId=" + patient.getPatientId() + "&configuration=F:BOO:" + configuration + encounterTypeAddition + "&showHtmlForm=" + showHtmlFormInstead + "&windowHeight=" + windowHeight + "&showProvider=" + showProvider + "&providerHeader=" + providerHeader + "&addAnotherButtonLabel=" + addAnotherButtonLabel + "'";
            }
            sb.append(" src='/"+WebConstants.WEBAPP_NAME+"/moduleResources/htmlformflowsheet/pleaseWait.htm'  ");
            sb.append(" width='100%' frameborder='0' scrolling='no'></iframe><br/><script>window.frames['iframeFor" + configuration + "'].innerHTML = 'please wait...'; \n function iframe"+configuration+"(){window.frames['iframeFor" + configuration + "'].location = "+source+";} \n setTimeout('iframe"+configuration+"();', 1);</script>");
            
            out.print(sb.toString());
            
        } else {
            out.print("<div>You must create the patient first!</div>");
        }
        
        return true;
    }
    
    public void doEndTag(FormEntrySession session, PrintWriter out, Node parent, Node node) {
        // TODO Auto-generated method stub
        
    }
    
    
}
