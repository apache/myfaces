package org.apache.myfaces.config.annotation;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * @author Dennis Byrne
 */

class AnnotatedManagedBean {

	private boolean postConstructCalled = false; // using a stub for a mock

	private boolean preDestroyCalled = false; // using a stob for a mock here

	private Throwable throwable;

	public AnnotatedManagedBean(Throwable throwable) {
		this.throwable = throwable;
	}

	@PostConstruct
	public void postConstruct() throws Throwable {
		postConstructCalled = true;

		if (throwable != null)
			throw throwable;
	}

	@PreDestroy
	public void preDestroy() throws Throwable {
		preDestroyCalled = true;

		if (throwable != null)
			throw throwable;
	}

	boolean isPostConstructCalled() {
		return postConstructCalled;
	}

	boolean isPreDestroyCalled() {
		return preDestroyCalled;
	}

}
