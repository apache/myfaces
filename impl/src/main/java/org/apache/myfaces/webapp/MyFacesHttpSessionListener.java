package org.apache.myfaces.webapp;

import java.util.Enumeration;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.apache.myfaces.config.ManagedBeanBuilder;

/**
 * @author Dennis Byrne
 */

public class MyFacesHttpSessionListener extends AbstractMyFacesListener implements HttpSessionListener {

	public void sessionCreated(HttpSessionEvent event) { // noop
	}

	public void sessionDestroyed(HttpSessionEvent event) {
		
		HttpSession session = event.getSession();
		Enumeration<String> attributes = session.getAttributeNames();
		
		while(attributes.hasMoreElements())
		{
			String name = attributes.nextElement();
			Object value = session.getAttribute(name);
			doPreDestroy(value, name, ManagedBeanBuilder.SESSION);
		}
		
	}

}
