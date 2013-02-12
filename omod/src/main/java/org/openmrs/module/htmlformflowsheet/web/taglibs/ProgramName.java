package org.openmrs.module.htmlformflowsheet.web.taglibs;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTagSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Program;
import org.openmrs.api.context.Context;

public class ProgramName extends BodyTagSupport {
	
	
	private String program;
    public static final long serialVersionUID = 128234334L;    
    private final Log log = LogFactory.getLog(getClass());
    
	public String getProgram() {
		return program;
	}
	public void setProgram(String program) {
		this.program = program;
	}
    
    
    public int doStartTag() throws JspException {
        String ret = "";
        if (program != null){
        	Program prog = null;
        	try {
        		prog = Context.getProgramWorkflowService().getProgram(Integer.valueOf(program.trim()));
        	} catch (Exception ex){
        		//pass
        	}
        	if (prog == null)
        		prog = Context.getProgramWorkflowService().getProgramByName(program);
        	if (prog == null)
        		prog = Context.getProgramWorkflowService().getProgramByUuid(program);
        	
        	if (prog != null)
        		ret = prog.getName();
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
         program = null;
         return EVAL_PAGE;
     }
     
     public void release() {
         super.release();
         program = null;
     }
    
}
