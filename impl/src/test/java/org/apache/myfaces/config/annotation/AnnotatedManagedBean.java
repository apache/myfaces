/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.myfaces.config.annotation;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

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
