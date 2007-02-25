package org.apache.myfaces.config.annotation;

import junit.framework.TestCase;

import org.apache.myfaces.config.ManagedBeanBuilder;
import org.apache.myfaces.config.annotation.AnnotatedManagedBeanHandler;
import org.apache.myfaces.config.impl.digester.elements.ManagedBean;

/**
 * @author Dennis Byrne
 */

public class AnnotatedManagedBeanHandlerTestCase extends TestCase {
	
	protected AnnotatedManagedBean managedBean;
	protected AnnotatedManagedBeanHandler handler;
	protected final String NAME = "volker weber";
	
	public void setUp() {
		managedBean = new AnnotatedManagedBean(null);
	}

	public void testPostConstructShouldNotInvokeForNoneScope() {

		handler = new AnnotatedManagedBeanHandler(managedBean, 
				ManagedBeanBuilder.NONE, NAME);
		
		boolean threwUnchecked = handler.invokePostConstruct();
		
		assertFalse(threwUnchecked);
		assertFalse(managedBean.isPostConstructCalled());
		assertFalse(managedBean.isPreDestroyCalled());
	}

	public void testPreDestroyShouldNotInvokeForNoneScope() {

		handler = new AnnotatedManagedBeanHandler(managedBean, 
				ManagedBeanBuilder.NONE, NAME);
		
		boolean threwUnchecked = handler.invokePreDestroy();
		
		assertFalse(threwUnchecked);
		assertFalse(managedBean.isPostConstructCalled());
		assertFalse(managedBean.isPreDestroyCalled());
	}
	
	public void testPostConstructShouldInvokeForRequestScope() {
		
		handler = new AnnotatedManagedBeanHandler(managedBean, 
				ManagedBeanBuilder.REQUEST, NAME);
		
		boolean threwUnchecked = handler.invokePostConstruct();
		
		assertFalse(threwUnchecked);
		assertTrue(managedBean.isPostConstructCalled());
		assertFalse(managedBean.isPreDestroyCalled());
	}

	public void testPreDestroyShouldInvokeForRequestScope() {
		
		handler = new AnnotatedManagedBeanHandler(managedBean, 
				ManagedBeanBuilder.REQUEST, NAME);
		
		boolean threwUnchecked = handler.invokePreDestroy();
		
		assertFalse(threwUnchecked);
		assertFalse(managedBean.isPostConstructCalled());
		assertTrue(managedBean.isPreDestroyCalled());
	}
	
	public void testPostConstructShouldInvokeForSessionScope() {
		
		handler = new AnnotatedManagedBeanHandler(managedBean, 
				ManagedBeanBuilder.SESSION, NAME);
		
		boolean threwUnchecked = handler.invokePostConstruct();
		
		assertFalse(threwUnchecked);
		assertTrue(managedBean.isPostConstructCalled());
		assertFalse(managedBean.isPreDestroyCalled());
	}

	public void testPreDestroyShouldInvokeForSessionScope() {
		
		handler = new AnnotatedManagedBeanHandler(managedBean, 
				ManagedBeanBuilder.SESSION, NAME);
		
		boolean threwUnchecked = handler.invokePreDestroy();
		
		assertFalse(threwUnchecked);
		assertFalse(managedBean.isPostConstructCalled());
		assertTrue(managedBean.isPreDestroyCalled());
	}
	
	public void testPostConstructShouldInvokeForApplicationScope() {
		
		handler = new AnnotatedManagedBeanHandler(managedBean, 
				ManagedBeanBuilder.APPLICATION, NAME);
		
		boolean threwUnchecked = handler.invokePostConstruct();
		
		assertFalse(threwUnchecked);
		assertTrue(managedBean.isPostConstructCalled());
		assertFalse(managedBean.isPreDestroyCalled());
	}

	public void testPreDestroyShouldInvokeForApplicationScope() {
		
		handler = new AnnotatedManagedBeanHandler(managedBean, 
				ManagedBeanBuilder.APPLICATION, NAME);
		
		boolean threwUnchecked = handler.invokePreDestroy();
		
		assertFalse(threwUnchecked);
		assertFalse(managedBean.isPostConstructCalled());
		assertTrue(managedBean.isPreDestroyCalled());
	}
	
}
