package org.apache.myfaces.config.annotation;

import junit.framework.TestCase;

import org.apache.myfaces.config.ManagedBeanBuilder;
import org.apache.myfaces.config.annotation.AnnotatedManagedBeanHandler;
import org.apache.myfaces.config.impl.digester.elements.ManagedBean;

/**
 * @author Dennis Byrne
 */

public class AnnotatedRuntimeExceptionManagedBeanHandlerTestCase extends TestCase {

	protected AnnotatedManagedBean managedBean;
	protected AnnotatedManagedBeanHandler handler;
	protected ManagedBean managedBeanConf;
	
	public void setUp() {
		managedBean = new AnnotatedManagedBean(new RuntimeException());
		managedBeanConf = new ManagedBean();
		handler = new AnnotatedManagedBeanHandler(managedBean, managedBeanConf);
	}
	
	public void testShouldNotInvokeForNoneScope() {

		managedBeanConf.setScope(ManagedBeanBuilder.NONE);
		boolean threwUnchecked = handler.run();
		
		assertFalse(threwUnchecked);
		assertFalse(managedBean.isPostConstructCalled());
	}

	public void testShouldInvokeForRequestScope() {
		
		managedBeanConf.setScope(ManagedBeanBuilder.REQUEST);
		boolean threwUnchecked = handler.run();
		
		assertTrue(threwUnchecked);
		assertTrue(managedBean.isPostConstructCalled());
	}

	public void testShouldInvokeForSessionScope() {
		
		managedBeanConf.setScope(ManagedBeanBuilder.SESSION);
		boolean threwUnchecked = handler.run();
		
		assertTrue(threwUnchecked);
		assertTrue(managedBean.isPostConstructCalled());
	}

	public void testShouldInvokeForApplicationScope() {
		
		managedBeanConf.setScope(ManagedBeanBuilder.APPLICATION);
		boolean threwUnchecked = handler.run();
		
		assertTrue(threwUnchecked);
		assertTrue(managedBean.isPostConstructCalled());
	}
	
}
