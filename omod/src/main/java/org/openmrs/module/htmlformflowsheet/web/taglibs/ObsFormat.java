package org.openmrs.module.htmlformflowsheet.web.taglibs;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTagSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.ConceptNumeric;
import org.openmrs.Obs;
import org.openmrs.api.context.Context;

public class ObsFormat extends BodyTagSupport {
    
    public static final long serialVersionUID = 128234353L;
    private final Log log = LogFactory.getLog(getClass());
    
    private Obs obs;
    
    public int doStartTag() throws JspException {
        Locale loc = Context.getLocale();
        String ret = getObsValueAsString(loc, obs);
            
        try {
            pageContext.getOut().write(ret);
        } catch (IOException e) {
            log.error("Could not write to pageContext", e);
        }
        release();
        return SKIP_BODY;
    }
    
    public int doEndTag() {
         obs = null;
         return EVAL_PAGE;
     }
    
    private  String getObsValueAsString(Locale locale, Obs o){
        String ret = "";
        if (o.getConcept() != null){
            String abbrev = o.getConcept().getDatatype().getHl7Abbreviation();
            if (abbrev.equals("DT")){
                return (o.getValueDatetime() == null ? "" : Context.getDateFormat().format(o.getValueDatetime()));
            } else if (abbrev.equals("TS") && o.getValueDatetime() != null ){
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                return sdf.format(o.getValueDatetime());
            } else if (abbrev.equals("NM") || abbrev.equals("SN")){
                try {
                    ConceptNumeric cn = Context.getConceptService().getConceptNumeric(obs.getConcept().getConceptId());
                    if (!cn.getAllowDecimal() && obs.getValueNumeric() != null)
                        ret = Integer.valueOf(obs.getValueNumeric().intValue()).toString();
                    else
                        ret = o.getValueAsString(locale);     
                } catch (Exception ex){
                    ret = o.getValueAsString(locale);
                }
            } else {
                ret = o.getValueAsString(locale);
            }    
        }
        return ret;
    }

    public Obs getObs() {
        return obs;
    }

    public void setObs(Obs obs) {
        this.obs = obs;
    }
    
    
}
