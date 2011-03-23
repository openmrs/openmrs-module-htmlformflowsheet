package org.openmrs.module.htmlformflowsheet.handlers;

import java.io.PrintWriter;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Form;
import org.openmrs.Patient;
import org.openmrs.module.htmlformentry.FormEntrySession;
import org.openmrs.module.htmlformentry.FormEntryContext.Mode;
import org.openmrs.module.htmlformentry.handler.TagHandler;
import org.openmrs.module.htmlformflowsheet.web.utils.HtmlFormFlowsheetWebUtils;
import org.openmrs.web.WebConstants;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;


public class HtmlFormFlowsheetHandler  implements TagHandler {

    /** The logger to use with this class */
    protected final Log log = LogFactory.getLog(getClass());

    
    public boolean doStartTag(FormEntrySession session, PrintWriter out, Node parent, Node node) {
        Map<String, String> attributes = new HashMap<String, String>();        
        NamedNodeMap map = node.getAttributes();
        for (int i = 0; i < map.getLength(); ++i) {
            Node attribute = map.item(i);
            attributes.put(attribute.getNodeName(), attribute.getNodeValue());
        }
        
        
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
        
        String showAllWithEncType = null;
        try {
            showAllWithEncType = attributes.get("sharedEncounter");
        } catch (Exception ex){
            
        }
        
        String addAnotherButtonLabel = (String) attributes.get("addAnotherButtonLabel");
        try {
            if (addAnotherButtonLabel != null && !addAnotherButtonLabel.equals("")){
                addAnotherButtonLabel = URLEncoder.encode(addAnotherButtonLabel, "UTF-8");;
            }    
        } catch (Exception ex){
            ex.fillInStackTrace();
        }
        
        String windowHeight = (String) attributes.get("windowHeight");
        
        String showHtmlFormInstead = "false";
        String showHtmlFormInsteadStr = (String) attributes.get("showHtmlForm");
        if (showHtmlFormInsteadStr != null && showHtmlFormInsteadStr.equals("true"))
            showHtmlFormInstead = "true";
        
            
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
                source = "'/"+WebConstants.WEBAPP_NAME+"/module/htmlformflowsheet/patientWidgetChart.list?fullPage=false&patientId=" + patient.getPatientId() + "&configuration=F:BOO:" + configuration + encounterTypeAddition + "&showHtmlForm=" + showHtmlFormInstead + "&windowHeight=" + windowHeight + "&addAnotherButtonLabel=" + addAnotherButtonLabel + "'";
            } else {
                //sb.append("  src='/openmrs/module/htmlformflowsheet/patientWidgetChart.list?readOnly=true&fullPage=false&patientId=" + patient.getPatientId() + "&configuration=F:BOO:" + configuration + "'  ");
                source = "'/"+WebConstants.WEBAPP_NAME+"/module/htmlformflowsheet/patientWidgetChart.list?readOnly=true&fullPage=false&patientId=" + patient.getPatientId() + "&configuration=F:BOO:" + configuration + encounterTypeAddition + "&showHtmlForm=" + showHtmlFormInstead + "&windowHeight=" + windowHeight + "&addAnotherButtonLabel=" + addAnotherButtonLabel + "'";
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
