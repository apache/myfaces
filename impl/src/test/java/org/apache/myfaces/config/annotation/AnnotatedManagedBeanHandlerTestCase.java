package org.apache.myfaces.config.annotation;

import junit.framework.TestCase;

import org.apache.myfaces.config.ManagedBeanBuilder;
import org.apache.AnnotationProcessor;

import java.lang.reflect.InvocationTargetException;

/**
 * @author Dennis Byrne
 */

public class AnnotatedManagedBeanHandlerTestCase extends TestCase {
	
	protected AnnotatedManagedBean managedBean;
	protected AnnotatedManagedBeanHandler handler;
  protected AnnotationProcessor processor;
  protected final String NAME = "volker weber";
	
	public void setUp() {
		managedBean = new AnnotatedManagedBean(null);
    processor = new NoInjectionAnnotationProcessor();
  }

	public void testPostConstructShouldNotInvokeForNoneScope() throws IllegalAccessException, InvocationTargetException {

		/*handler = new AnnotatedManagedBeanHandler(managedBean,
				ManagedBeanBuilder.NONE, NAME);

		boolean threwUnchecked = handler.invokePostConstruct();
		
		assertFalse(threwUnchecked);*/
    processor.postConstruct(managedBean);
    assertFalse(managedBean.isPostConstructCalled());
		assertFalse(managedBean.isPreDestroyCalled());
	}

	public void testPreDestroyShouldNotInvokeForNoneScope() throws IllegalAccessException, InvocationTargetException {

		/*handler = new AnnotatedManagedBeanHandler(managedBean,
				ManagedBeanBuilder.NONE, NAME);
		
		boolean threwUnchecked = handler.invokePreDestroy();
		
		assertFalse(threwUnchecked);*/
    processor.preDestroy(managedBean);
    assertFalse(managedBean.isPostConstructCalled());
		assertFalse(managedBean.isPreDestroyCalled());
	}
	
	public void testPostConstructShouldInvokeForRequestScope() throws IllegalAccessException, InvocationTargetException {
		
		/*handler = new AnnotatedManagedBeanHandler(managedBean,
				ManagedBeanBuilder.REQUEST, NAME);
		
		boolean threwUnchecked = handler.invokePostConstruct();
		
		assertFalse(threwUnchecked);*/
    processor.postConstruct(managedBean);
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
