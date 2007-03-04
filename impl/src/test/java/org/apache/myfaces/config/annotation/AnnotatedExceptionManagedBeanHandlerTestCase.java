package org.apache.myfaces.config.annotation;

import junit.framework.TestCase;

import org.apache.myfaces.config.ManagedBeanBuilder;

/**
 * @author Dennis Byrne
 */

public class AnnotatedExceptionManagedBeanHandlerTestCase extends TestCase {

	protected AnnotatedManagedBean managedBean;

	protected AnnotatedManagedBeanHandler handler;

	protected final String NAME = "sean_schofield";
	
	public void setUp() {
		managedBean = new AnnotatedManagedBean(true);
	}

	public void testPostConstructShouldNotBlowUpForNoneScope() {

		handler = new AnnotatedManagedBeanHandler(managedBean, 
				ManagedBeanBuilder.NONE, NAME);

		boolean exceptionThrown;
		
		try {
			exceptionThrown = handler.invokePostConstruct();
		} catch (RuntimeException e) {
			exceptionThrown = true;			
		}

		assertFalse(managedBean.isPostConstructCalled());
		assertFalse(managedBean.isPreDestroyCalled());
		assertFalse(exceptionThrown);
	}

	public void testPreDestroyShouldNotBlowUpForNoneScope() {

		handler = new AnnotatedManagedBeanHandler(managedBean, 
				ManagedBeanBuilder.NONE, NAME);

		boolean exceptionThrown;
		
		try {
			exceptionThrown = handler.invokePreDestroy();
		} catch (RuntimeException e) {
			exceptionThrown = true;			
		}

		assertFalse(managedBean.isPostConstructCalled());
		assertFalse(managedBean.isPreDestroyCalled());
		assertFalse(exceptionThrown);
	}
	
	public void testPostConstructShouldBlowUpForRequestScope() {

        try {
        handler = new AnnotatedManagedBeanHandler(managedBean,
				ManagedBeanBuilder.REQUEST, NAME);

		boolean exceptionThrown;
		
	    exceptionThrown = handler.invokePostConstruct();

		assertTrue(exceptionThrown);
		assertTrue(managedBean.isPostConstructCalled());
		assertFalse(managedBean.isPreDestroyCalled());
       } catch (Throwable e) {
			e.printStackTrace();			
		}
    }

	public void testPreDestroyShouldBlowUpForRequestScope() {

		handler = new AnnotatedManagedBeanHandler(managedBean, 
				ManagedBeanBuilder.REQUEST, NAME);

		boolean exceptionThrown;
		

	    exceptionThrown = handler.invokePreDestroy();

		assertTrue(exceptionThrown);
		assertFalse(managedBean.isPostConstructCalled());
		assertTrue(managedBean.isPreDestroyCalled());
	}
	
	public void testPostConstructShouldBlowUpForSessionScope() {

		handler = new AnnotatedManagedBeanHandler(managedBean, 
				ManagedBeanBuilder.SESSION, NAME);

		boolean exceptionThrown;
		
	    exceptionThrown = handler.invokePostConstruct();

		assertTrue(exceptionThrown);
		assertTrue(managedBean.isPostConstructCalled());
		assertFalse(managedBean.isPreDestroyCalled());
	}

	public void testPreDestroyShouldBlowUpForSessionScope() {

		handler = new AnnotatedManagedBeanHandler(managedBean, 
				ManagedBeanBuilder.SESSION, NAME);

		boolean exceptionThrown;
		
	    exceptionThrown = handler.invokePreDestroy();

		assertTrue(exceptionThrown);
		assertFalse(managedBean.isPostConstructCalled());
		assertTrue(managedBean.isPreDestroyCalled());
	}
	
	public void testPostConstructShouldBlowUpForApplicationScope() {

		handler = new AnnotatedManagedBeanHandler(managedBean, 
				ManagedBeanBuilder.APPLICATION, NAME);

		boolean exceptionThrown;
		
		exceptionThrown = handler.invokePostConstruct();

		assertTrue(exceptionThrown);
		assertTrue(managedBean.isPostConstructCalled());
		assertFalse(managedBean.isPreDestroyCalled());
	}

	public void testPreDestroyShouldBlowUpForApplicationScope() {

		handler = new AnnotatedManagedBeanHandler(managedBean, 
				ManagedBeanBuilder.APPLICATION, NAME);

		boolean exceptionThrown;
		
		exceptionThrown = handler.invokePreDestroy();

		assertTrue(exceptionThrown);
		assertFalse(managedBean.isPostConstructCalled());
		assertTrue(managedBean.isPreDestroyCalled());
	}
}
