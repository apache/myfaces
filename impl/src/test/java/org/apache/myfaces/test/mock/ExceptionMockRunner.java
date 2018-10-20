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
package org.apache.myfaces.test.mock;

import org.easymock.classextension.IMocksControl;

public abstract class ExceptionMockRunner extends MockRunner
{
    private final Throwable _exception;

    public ExceptionMockRunner(IMocksControl mockControl, Throwable exception)
    {
        super(mockControl);
        _exception = exception;
    }

    @Override
    protected final void setUp(IMocksControl mockControl)
    {
        setUp(mockControl, _exception);
    }

    protected abstract void setUp(IMocksControl mockControl, Throwable exceptionToThrow);

}
