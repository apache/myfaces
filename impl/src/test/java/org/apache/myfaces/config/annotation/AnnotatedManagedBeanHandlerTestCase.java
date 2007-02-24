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
	protected ManagedBean managedBeanConf;
	
	public void setUp() {
		managedBean = new AnnotatedManagedBean(null);
		managedBeanConf = new ManagedBean();
		handler = new AnnotatedManagedBeanHandler(managedBean, managedBeanConf);
	}

	public void testPostConstructShouldNotInvokeForNoneScope() {

		managedBeanConf.setScope(ManagedBeanBuilder.NONE);
		boolean threwUnchecked = handler.invokePostConstruct();
		
		assertFalse(threwUnchecked);
		assertFalse(managedBean.isPostConstructCalled());
		assertFalse(managedBean.isPreDestroyCalled());
	}

	public void testPreDestroyShouldNotInvokeForNoneScope() {

		managedBeanConf.setScope(ManagedBeanBuilder.NONE);
		boolean threwUnchecked = handler.invokePreDestroy();
		
		assertFalse(threwUnchecked);
		assertFalse(managedBean.isPostConstructCalled());
		assertFalse(managedBean.isPreDestroyCalled());
	}
	
	public void testPostConstructShouldInvokeForRequestScope() {
		
		managedBeanConf.setScope(ManagedBeanBuilder.REQUEST);
		boolean threwUnchecked = handler.invokePostConstruct();
		
		assertFalse(threwUnchecked);
		assertTrue(managedBean.isPostConstructCalled());
		assertFalse(managedBean.isPreDestroyCalled());
	}

	public void testPreDestroyShouldInvokeForRequestScope() {
		
		managedBeanConf.setScope(ManagedBeanBuilder.REQUEST);
		boolean threwUnchecked = handler.invokePreDestroy();
		
		assertFalse(threwUnchecked);
		assertFalse(managedBean.isPostConstructCalled());
		assertTrue(managedBean.isPreDestroyCalled());
	}
	
	public void testPostConstructShouldInvokeForSessionScope() {
		
		managedBeanConf.setScope(ManagedBeanBuilder.SESSION);
		boolean threwUnchecked = handler.invokePostConstruct();
		
		assertFalse(threwUnchecked);
		assertTrue(managedBean.isPostConstructCalled());
		assertFalse(managedBean.isPreDestroyCalled());
	}

	public void testPreDestroyShouldInvokeForSessionScope() {
		
		managedBeanConf.setScope(ManagedBeanBuilder.SESSION);
		boolean threwUnchecked = handler.invokePreDestroy();
		
		assertFalse(threwUnchecked);
		assertFalse(managedBean.isPostConstructCalled());
		assertTrue(managedBean.isPreDestroyCalled());
	}
	
	public void testPostConstructShouldInvokeForApplicationScope() {
		
		managedBeanConf.setScope(ManagedBeanBuilder.APPLICATION);
		boolean threwUnchecked = handler.invokePostConstruct();
		
		assertFalse(threwUnchecked);
		assertTrue(managedBean.isPostConstructCalled());
		assertFalse(managedBean.isPreDestroyCalled());
	}

	public void testPreDestroyShouldInvokeForApplicationScope() {
		
		managedBeanConf.setScope(ManagedBeanBuilder.APPLICATION);
		boolean threwUnchecked = handler.invokePreDestroy();
		
		assertFalse(threwUnchecked);
		assertFalse(managedBean.isPostConstructCalled());
		assertTrue(managedBean.isPreDestroyCalled());
	}
	
}
