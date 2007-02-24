package org.apache.myfaces.config.annotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.faces.FacesException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.config.ManagedBeanBuilder;
import org.apache.myfaces.config.element.ManagedBean;

/**
 * @see JSF spec 1.2, section 5.4
 * @warn Do not import javax.annotation.* in this class
 * @author Dennis Byrne
 */

public class AnnotatedManagedBeanHandler {

	private static Log log = LogFactory.getLog(AnnotatedManagedBeanHandler.class);

	private Object managedBean;

	private ManagedBean beanConfiguration;

	public AnnotatedManagedBeanHandler(Object managedBean, ManagedBean beanConfiguration) {

		if (managedBean == null) {
			throw new NullPointerException("Object managedBean");
		}

		if (beanConfiguration == null) {
			throw new NullPointerException("ManagedBean beanConfiguration");
		}

		this.managedBean = managedBean;
		this.beanConfiguration = beanConfiguration;

	}

	public boolean run() {

		boolean threwUnchecked = false;

		if (ManagedBeanBuilder.NONE.equals(beanConfiguration.getManagedBeanScope())) {
			; // this only applies to a, s, and r scope beans
		} else {
			threwUnchecked = run(managedBean.getClass().getMethods());
		}

		return threwUnchecked;
	}

	private boolean run(Method[] methods) {

		boolean threwUnchecked = false;

		for (Method method : methods)
			if (run(method)) {
				threwUnchecked = true;
				break;
			}// break if we invoke method ? or invoke all w/ annoation?

		return threwUnchecked;
	}

	private boolean run(Method method) {

		Annotation[] annotations = method.getAnnotations();
		boolean threwUnchecked = false;

		for (Annotation annotation : annotations) {
			if (isPostConstruct(annotation)) {
				if (run(annotation, method)) {
					threwUnchecked = true;
					break; // spec says not to call anymore methods on this
				}
			}
		}

		return threwUnchecked;
	}

	private boolean run(Annotation annotation, Method method) {

		boolean threwUnchecked = true; // start w/ pessimism

		try {

			method.invoke(managedBean, null);

			threwUnchecked = false;

		} catch (InvocationTargetException ite) { // catch most specific first

			final Throwable cause = ite.getCause();

			handleException(method, cause == null ? ite : cause);

		} catch (Exception e) {

			handleException(method, e);
		}

		return threwUnchecked;
	}

	private void handleException(Method method, Throwable e) {
		final String genericLoggingMessage = getGenericLoggingMessage(method, e);

		if (e instanceof RuntimeException) // why did they make RE extend E ?
		{
			log.error(genericLoggingMessage + " MyFaces cannot " + " put the bean in "
					+ beanConfiguration.getManagedBeanScope() + " scope " + " ... execution continues. ");
		} else {
			throw new FacesException(genericLoggingMessage + " The spec is ambivalent on checked exceptions.");
		}
	}

	private String getGenericLoggingMessage(Method method, Throwable e) {
		return "When invoking " + method.getName() + " on a managed bean '" + beanConfiguration.getManagedBeanName()
				+ "'," + " an exception " + e.getClass() + "{" + e.getMessage() + "} was thrown. "
				+ " See section 5.4.1 of the JSF specification.";
	}

	private boolean isPostConstruct(Annotation annotation) {

		final Class<? extends Annotation> annotationType = annotation.annotationType();
		final String name = annotationType.getName();
		// use the literal String because we want to avoid ClassDefNotFoundError
		return "javax.annotation.PostConstruct".equals(name);

	}
}
