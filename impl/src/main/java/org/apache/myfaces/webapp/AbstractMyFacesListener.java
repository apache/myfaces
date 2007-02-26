package org.apache.myfaces.webapp;

import javax.servlet.ServletContextAttributeEvent;
import javax.servlet.ServletRequestAttributeEvent;
import javax.servlet.http.HttpSessionBindingEvent;

import org.apache.myfaces.config.annotation.AnnotatedManagedBeanHandler;

/**
 * @author Dennis Byrne
 */

public abstract class AbstractMyFacesListener {

	protected void doPreDestroy(ServletRequestAttributeEvent event, String scope) {
		doPreDestroy(event.getValue(), event.getName(), scope);
	}

	protected void doPreDestroy(HttpSessionBindingEvent event, String scope) {
		doPreDestroy(event.getValue(), event.getName(), scope);
	}

	protected void doPreDestroy(ServletContextAttributeEvent event, String scope) {
		doPreDestroy(event.getValue(), event.getName(), scope);
	}
	
	protected void doPreDestroy(Object value, String name, String scope) {
		
		if(value != null)
		{
			AnnotatedManagedBeanHandler handler = 
				new AnnotatedManagedBeanHandler(value, scope, name);

			handler.invokePreDestroy();
		}
		
	}
	
}
