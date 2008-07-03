package org.apache.myfaces.webapp;

import javax.servlet.http.HttpSessionAttributeListener;
import javax.servlet.http.HttpSessionBindingEvent;

import org.apache.myfaces.config.ManagedBeanBuilder;

/**
 * @author Dennis Byrne
 */

public class MyFacesHttpSessionAttributeListener extends AbstractMyFacesListener
        implements HttpSessionAttributeListener {

    public void attributeAdded(HttpSessionBindingEvent event) { // noop
    }

    public void attributeRemoved(HttpSessionBindingEvent event) {
        doPreDestroy(event, ManagedBeanBuilder.SESSION);
    }

    public void attributeReplaced(HttpSessionBindingEvent event) {
        doPreDestroy(event, ManagedBeanBuilder.SESSION);
    }

}
