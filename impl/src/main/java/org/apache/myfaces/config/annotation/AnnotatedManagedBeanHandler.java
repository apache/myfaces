package org.apache.myfaces.config.annotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.faces.FacesException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.config.ManagedBeanBuilder;

/**
 * @see JSF spec 1.2, section 5.4
 * @warn Do not import javax.annotation.* in this class
 * @author Dennis Byrne
 */

public class AnnotatedManagedBeanHandler {

	private static Log log = LogFactory.getLog(AnnotatedManagedBeanHandler.class);

	private Object managedBean;

	private String scope;
	
	private String name;
	
	private static final String POST_CONSTRUCT = "javax.annotation.PostConstruct";
	
	private static final String PRE_DESTROY = "javax.annotation.PreDestroy";

	public AnnotatedManagedBeanHandler(Object managedBean, String scope, String name) {

		if (managedBean == null) {
			throw new NullPointerException("Object managedBean");
		}

		if (scope == null) {
			throw new NullPointerException("scope");
		}

		if(name == null)
			if(log.isWarnEnabled())
				log.warn("managed bean " + managedBean.getClass() + "in " + scope + " scope has no name ");
		
		this.managedBean = managedBean;
		this.scope = scope;
		this.name = name;
	}

	public boolean invokePreDestroy() {
		return invoke(PRE_DESTROY);
	}

	public boolean invokePostConstruct() {
		return invoke(POST_CONSTRUCT);
	}
	
	private boolean invoke(String annotationName) {

		boolean threwUnchecked = false;

		if (ManagedBeanBuilder.NONE.equals(scope)) {
			if(log.isDebugEnabled())
				log.debug( annotationName + " not processed for managed bean " + name 
						+ " because it is not in request, session, or "
						+ "application scope.  See section 5.4 of the JSF 1.3 spec"); 
		} else {
			threwUnchecked = invoke(managedBean.getClass().getMethods(), annotationName);
		}

		return threwUnchecked;
	}

	private boolean invoke(Method[] methods, String annotationName) {

		boolean threwUnchecked = false;

		for (Method method : methods)
			if (invoke(method, annotationName)) {
				threwUnchecked = true;
				break;
			}// break if we invoke method ? or invoke all w/ annoation?

		return threwUnchecked;
	}

	private boolean invoke(Method method, String annotationName) {

		Annotation[] annotations = method.getAnnotations();
		boolean threwUnchecked = false;

		for (Annotation annotation : annotations) 
		{
			if (isMatch(annotation, annotationName)) 
			{
				if (invoke(annotation, method)) 
				{
					threwUnchecked = true;
					break; // spec says not to call any more methods on this
				}
			}
		}

		return threwUnchecked;
	}

	private boolean invoke(Annotation annotation, Method method) {

		boolean threwUnchecked = true; // start w/ pessimism

		try {

			method.invoke(managedBean, null); // what do we do for parameters?

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
					+ scope + " scope " + " ... execution continues. ");
		} else {
			throw new FacesException(genericLoggingMessage + " The spec is ambivalent on checked exceptions.");
		}
	}

	private String getGenericLoggingMessage(Method method, Throwable e) {
		return "When invoking " + method.getName() + " on a managed bean '" + name
				+ "'," + " an exception " + e.getClass() + "{" + e.getMessage() + "} was thrown. "
				+ " See section 5.4.1 of the JSF specification.";
	}

	private boolean isMatch(Annotation annotation, String annotationName) {

		final Class<? extends Annotation> annotationType = annotation.annotationType();
		final String name = annotationType.getName();
		// use the literal String because we want to avoid ClassDefNotFoundError
		return annotationName.equals(name);

	}
}
