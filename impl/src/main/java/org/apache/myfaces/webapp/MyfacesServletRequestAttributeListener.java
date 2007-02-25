package org.apache.myfaces.webapp;

import javax.servlet.ServletRequestAttributeEvent;
import javax.servlet.ServletRequestAttributeListener;

import org.apache.myfaces.config.ManagedBeanBuilder;

/**
 * @author Dennis Byrne
 */

public class MyfacesServletRequestAttributeListener extends AbstractMyFacesListener
		implements ServletRequestAttributeListener {
	
	public void attributeAdded(ServletRequestAttributeEvent event) { // noop
	}

	public void attributeRemoved(ServletRequestAttributeEvent event) { 
		doPreDestroy(event, ManagedBeanBuilder.REQUEST);
	}

	public void attributeReplaced(ServletRequestAttributeEvent event) {
		doPreDestroy(event, ManagedBeanBuilder.REQUEST);		
	}

}
