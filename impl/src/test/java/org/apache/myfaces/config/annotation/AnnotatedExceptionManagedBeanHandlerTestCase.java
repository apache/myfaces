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

	protected ManagedBean managedBeanConf;

	public void setUp() {
		managedBean = new AnnotatedManagedBean(new Exception());
		managedBeanConf = new ManagedBean();
		handler = new AnnotatedManagedBeanHandler(managedBean, managedBeanConf);
	}

	public void testPostConstructShouldNotBlowUpForNoneScope() {

		managedBeanConf.setScope(ManagedBeanBuilder.NONE);

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

		managedBeanConf.setScope(ManagedBeanBuilder.NONE);

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

		managedBeanConf.setScope(ManagedBeanBuilder.REQUEST);

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

		managedBeanConf.setScope(ManagedBeanBuilder.REQUEST);

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

		managedBeanConf.setScope(ManagedBeanBuilder.SESSION);

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

		managedBeanConf.setScope(ManagedBeanBuilder.SESSION);

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

		managedBeanConf.setScope(ManagedBeanBuilder.APPLICATION);

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

		managedBeanConf.setScope(ManagedBeanBuilder.APPLICATION);

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
