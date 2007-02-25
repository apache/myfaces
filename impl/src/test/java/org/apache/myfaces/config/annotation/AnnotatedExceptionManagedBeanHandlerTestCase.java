package org.apache.myfaces.config.annotation;

import org.apache.myfaces.config.ManagedBeanBuilder;
import org.apache.myfaces.config.annotation.AnnotatedManagedBeanHandler;
import org.apache.myfaces.config.impl.digester.elements.ManagedBean;

import junit.framework.TestCase;

/**
 * @author Dennis Byrne
 */

public class AnnotatedExceptionManagedBeanHandlerTestCase extends TestCase {

	protected AnnotatedManagedBean managedBean;

	protected AnnotatedManagedBeanHandler handler;

	protected final String NAME = "sean_schofield";
	
	public void setUp() {
		managedBean = new AnnotatedManagedBean(new Exception());
	}

	public void testPostConstructShouldNotBlowUpForNoneScope() {

		handler = new AnnotatedManagedBeanHandler(managedBean, 
				ManagedBeanBuilder.NONE, NAME);

		boolean exceptionThrown = false;
		
		try {
			handler.invokePostConstruct();
		} catch (Exception e) {
			exceptionThrown = true;			
		}

		assertFalse(managedBean.isPostConstructCalled());
		assertFalse(managedBean.isPreDestroyCalled());
		assertFalse(exceptionThrown);
	}

	public void testPreDestroyShouldNotBlowUpForNoneScope() {

		handler = new AnnotatedManagedBeanHandler(managedBean, 
				ManagedBeanBuilder.NONE, NAME);

		boolean exceptionThrown = false;
		
		try {
			handler.invokePreDestroy();
		} catch (Exception e) {
			exceptionThrown = true;			
		}

		assertFalse(managedBean.isPostConstructCalled());
		assertFalse(managedBean.isPreDestroyCalled());
		assertFalse(exceptionThrown);
	}
	
	public void testPostConstructShouldBlowUpForRequestScope() {

		handler = new AnnotatedManagedBeanHandler(managedBean, 
				ManagedBeanBuilder.REQUEST, NAME);

		boolean exceptionThrown = false;
		
		try {
			handler.invokePostConstruct();
		} catch (Exception e) {
			exceptionThrown = true;			
		}

		assertTrue(exceptionThrown);
		assertTrue(managedBean.isPostConstructCalled());
		assertFalse(managedBean.isPreDestroyCalled());
	}

	public void testPreDestroyShouldBlowUpForRequestScope() {

		handler = new AnnotatedManagedBeanHandler(managedBean, 
				ManagedBeanBuilder.REQUEST, NAME);

		boolean exceptionThrown = false;
		
		try {
			handler.invokePreDestroy();
		} catch (Exception e) {
			exceptionThrown = true;			
		}

		assertTrue(exceptionThrown);
		assertFalse(managedBean.isPostConstructCalled());
		assertTrue(managedBean.isPreDestroyCalled());
	}
	
	public void testPostConstructShouldBlowUpForSessionScope() {

		handler = new AnnotatedManagedBeanHandler(managedBean, 
				ManagedBeanBuilder.SESSION, NAME);

		boolean exceptionThrown = false;
		
		try {
			handler.invokePostConstruct();
		} catch (Exception e) {
			exceptionThrown = true;			
		}

		assertTrue(exceptionThrown);
		assertTrue(managedBean.isPostConstructCalled());
		assertFalse(managedBean.isPreDestroyCalled());
	}

	public void testPreDestroyShouldBlowUpForSessionScope() {

		handler = new AnnotatedManagedBeanHandler(managedBean, 
				ManagedBeanBuilder.SESSION, NAME);

		boolean exceptionThrown = false;
		
		try {
			handler.invokePreDestroy();
		} catch (Exception e) {
			exceptionThrown = true;			
		}

		assertTrue(exceptionThrown);
		assertFalse(managedBean.isPostConstructCalled());
		assertTrue(managedBean.isPreDestroyCalled());
	}
	
	public void testPostConstructShouldBlowUpForApplicationScope() {

		handler = new AnnotatedManagedBeanHandler(managedBean, 
				ManagedBeanBuilder.APPLICATION, NAME);

		boolean exceptionThrown = false;
		
		try {
			handler.invokePostConstruct();
		} catch (Exception e) {
			exceptionThrown = true;			
		}

		assertTrue(exceptionThrown);
		assertTrue(managedBean.isPostConstructCalled());
		assertFalse(managedBean.isPreDestroyCalled());
	}

	public void testPreDestroyShouldBlowUpForApplicationScope() {

		handler = new AnnotatedManagedBeanHandler(managedBean, 
				ManagedBeanBuilder.APPLICATION, NAME);

		boolean exceptionThrown = false;
		
		try {
			handler.invokePreDestroy();
		} catch (Exception e) {
			exceptionThrown = true;			
		}

		assertTrue(exceptionThrown);
		assertFalse(managedBean.isPostConstructCalled());
		assertTrue(managedBean.isPreDestroyCalled());
	}
}
