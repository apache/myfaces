package org.apache.myfaces.config.annotation;

import junit.framework.TestCase;

import org.apache.myfaces.config.ManagedBeanBuilder;

/**
 * @author Dennis Byrne
 */

public class AnnotatedRuntimeExceptionManagedBeanHandlerTestCase extends TestCase {

	protected AnnotatedManagedBean managedBean;
	protected AnnotatedManagedBeanHandler handler;
	protected final String NAME = "Thomas_Spiegl";
	
	public void setUp() {
		managedBean = new AnnotatedManagedBean(true);
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
		
		assertTrue(threwUnchecked);
		assertTrue(managedBean.isPostConstructCalled());
		assertFalse(managedBean.isPreDestroyCalled());
	}

	public void testPreDestroyShouldInvokeForRequestScope() {
		
		handler = new AnnotatedManagedBeanHandler(managedBean, 
				ManagedBeanBuilder.REQUEST, NAME);
		
		boolean threwUnchecked = handler.invokePreDestroy();
		
		assertTrue(threwUnchecked);
		assertFalse(managedBean.isPostConstructCalled());
		assertTrue(managedBean.isPreDestroyCalled());
	}
	
	public void testPostConstructShouldInvokeForSessionScope() {
		
		handler = new AnnotatedManagedBeanHandler(managedBean, 
				ManagedBeanBuilder.SESSION, NAME);
		
		boolean threwUnchecked = handler.invokePostConstruct();
		
		assertTrue(threwUnchecked);
		assertTrue(managedBean.isPostConstructCalled());
		assertFalse(managedBean.isPreDestroyCalled());
	}

	public void testPreDestroyShouldInvokeForSessionScope() {
		
		handler = new AnnotatedManagedBeanHandler(managedBean, 
				ManagedBeanBuilder.SESSION, NAME);
		
		boolean threwUnchecked = handler.invokePreDestroy();
		
		assertTrue(threwUnchecked);
		assertFalse(managedBean.isPostConstructCalled());
		assertTrue(managedBean.isPreDestroyCalled());
	}
	
	public void testPostConstructShouldInvokeForApplicationScope() {
		
		handler = new AnnotatedManagedBeanHandler(managedBean, 
				ManagedBeanBuilder.APPLICATION, NAME);
		
		boolean threwUnchecked = handler.invokePostConstruct();
		
		assertTrue(threwUnchecked);
		assertTrue(managedBean.isPostConstructCalled());
		assertFalse(managedBean.isPreDestroyCalled());
	}

	public void testPreDestroyShouldInvokeForApplicationScope() {
		
		handler = new AnnotatedManagedBeanHandler(managedBean, 
				ManagedBeanBuilder.APPLICATION, NAME);
		
		boolean threwUnchecked = handler.invokePreDestroy();
		
		assertTrue(threwUnchecked);
		assertFalse(managedBean.isPostConstructCalled());
		assertTrue(managedBean.isPreDestroyCalled());
	}
}
