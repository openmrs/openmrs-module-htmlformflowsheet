package org.openmrs.module.htmlformflowsheet.web.taglibs;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTagSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;


public class ConceptFormatTag extends BodyTagSupport {
public static final long serialVersionUID = 128234333L;
    
    private final Log log = LogFactory.getLog(getClass());
    
    private Concept concept;
    
    private Boolean bestName;
    
    private Boolean bestShortName;
    
    private Boolean shortestName;
    
    private Boolean preferredName;
    
    private String nameTaggedWith;

    public boolean isBestShortName() {
        return bestShortName;
    }

    public void setBestShortName(boolean bestShortName) {
        this.bestShortName = bestShortName;
    }

    public boolean isShortestName() {
        return shortestName;
    }

    public void setShortestName(boolean shortestName) {
        this.shortestName = shortestName;
    }

    public boolean isBestName() {
        return bestName;
    }

    public void setBestName(boolean bestName) {
        this.bestName = bestName;
    }

    public boolean isPreferredName() {
        return preferredName;
    }

    public void setPreferredName(boolean preferredName) {
        this.preferredName = preferredName;
    }

    public String getNameTaggedWith() {
        return nameTaggedWith;
    }

    public void setNameTaggedWith(String nameTaggedWith) {
        this.nameTaggedWith = nameTaggedWith;
    }
    
    
    public int doStartTag() throws JspException {
        ConceptService cs = Context.getConceptService();
        String ret = "";
            if (concept != null){
                if (bestName != null && bestName == true){
                    ret = concept.getName(Context.getLocale(), false).getName();
                } else if (bestShortName != null && bestShortName == true){
                    ret = concept.getShortestName(Context.getLocale(), false).getName();
                } else if (shortestName != null && shortestName == true){
                    ret = concept.getShortestName(Context.getLocale(), false).getName();
                } else if (preferredName != null && preferredName == true){
                    ret = concept.getPreferredName(Context.getLocale()).getName();
                } else if (nameTaggedWith != null && !nameTaggedWith.equals("")){
                    ret = concept.findNameTaggedWith(cs.getConceptNameTagByName(nameTaggedWith)).getName();
                }
            }
        try {
            pageContext.getOut().write(ret);
        } catch (IOException e) {
            log.error("Could not write to pageContext", e);
        }
        release();
        return SKIP_BODY;
    }
    
    public int doEndTag() {
        bestName = null;
        bestShortName = null;
         preferredName = null;
         shortestName = null;
         nameTaggedWith = null;
         return EVAL_PAGE;
     }
     
     public void release() {
         super.release();
         bestName = null;
         bestShortName = null;
          preferredName = null;
          shortestName = null;
          nameTaggedWith = null;
     }

    public Concept getConcept() {
        return concept;
    }

    public void setConcept(Concept concept) {
        this.concept = concept;
    }
    
    
    
    
}
