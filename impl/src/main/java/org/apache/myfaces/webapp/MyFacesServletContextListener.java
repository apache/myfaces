package org.apache.myfaces.webapp;

import javax.servlet.ServletContextAttributeEvent;
import javax.servlet.ServletContextAttributeListener;

import org.apache.myfaces.config.ManagedBeanBuilder;

/**
 * @author Dennis Byrne
 */

public class MyFacesServletContextListener extends AbstractMyFacesListener implements ServletContextAttributeListener {

	public void attributeAdded(ServletContextAttributeEvent event) { // noop
	}

	public void attributeRemoved(ServletContextAttributeEvent event) {
		doPreDestroy(event, ManagedBeanBuilder.APPLICATION);
	}

	public void attributeReplaced(ServletContextAttributeEvent event) {
		doPreDestroy(event, ManagedBeanBuilder.APPLICATION);
	}

}
