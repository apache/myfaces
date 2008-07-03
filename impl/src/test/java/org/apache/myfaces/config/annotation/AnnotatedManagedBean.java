package org.apache.myfaces.config.annotation;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * @author Dennis Byrne
 */

class AnnotatedManagedBean {

    private boolean postConstructCalled = false; // using a stub for a mock

    private boolean preDestroyCalled = false; // using a stob for a mock here

    boolean throwExcetion;


    public AnnotatedManagedBean()
    {
    }

    public AnnotatedManagedBean(boolean throwExcetion) {
        this.throwExcetion = throwExcetion;
    }

    @PostConstruct
    public void postConstruct()  {
        postConstructCalled = true;

        if (throwExcetion) {
            throw new RuntimeException();
        }
    }

    @PreDestroy
    public void preDestroy() {
        preDestroyCalled = true;

        if (throwExcetion) {
            throw new RuntimeException();
        }
    }

    boolean isPostConstructCalled() {
        return postConstructCalled;
    }

    boolean isPreDestroyCalled() {
        return preDestroyCalled;
    }

}
