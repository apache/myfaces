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

	public void testShouldNotBlowUpForNoneScope() {

		managedBeanConf.setScope(ManagedBeanBuilder.NONE);

		boolean exceptionThrown = false;
		
		try {
			handler.run();
		} catch (Exception e) {
			exceptionThrown = true;			
		}

		assertFalse(managedBean.isPostConstructCalled());
		assertFalse(exceptionThrown);
	}

	public void testShouldBlowUpForRequestScope() {

		managedBeanConf.setScope(ManagedBeanBuilder.REQUEST);

		boolean exceptionThrown = false;
		
		try {
			handler.run();
		} catch (Exception e) {
			exceptionThrown = true;			
		}

		assertTrue(exceptionThrown);
	}

	public void testShouldBlowUpForSessionScope() {

		managedBeanConf.setScope(ManagedBeanBuilder.SESSION);

		boolean exceptionThrown = false;
		
		try {
			handler.run();
		} catch (Exception e) {
			exceptionThrown = true;			
		}

		assertTrue(exceptionThrown);
	}

	public void testShouldBlowUpForApplicationScope() {

		managedBeanConf.setScope(ManagedBeanBuilder.APPLICATION);

		boolean exceptionThrown = false;
		
		try {
			handler.run();
		} catch (Exception e) {
			exceptionThrown = true;			
		}

		assertTrue(exceptionThrown);
	}

}
