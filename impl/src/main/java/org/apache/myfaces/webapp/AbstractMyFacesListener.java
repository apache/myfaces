package org.apache.myfaces.webapp;

import javax.servlet.ServletContextAttributeEvent;
import javax.servlet.ServletRequestAttributeEvent;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.faces.FacesException;
import javax.naming.NamingException;

import org.apache.myfaces.config.annotation.AnnotationProcessorFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.InvocationTargetException;

/**
 * @author Dennis Byrne
 */

public abstract class AbstractMyFacesListener {
    private static Log log = LogFactory.getLog(AbstractMyFacesListener.class);

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
			//AnnotatedManagedBeanHandler handler =
			//	new AnnotatedManagedBeanHandler(value, scope, name);

			//handler.invokePreDestroy();

            try
            {
            
                AnnotationProcessorFactory.getAnnotatonProcessor().postConstruct(value);
            } catch (IllegalAccessException e)
            {
                log.error("", e);
            } catch (InvocationTargetException e)
            {
                log.error("", e);
            }
        }
	}
	
}
