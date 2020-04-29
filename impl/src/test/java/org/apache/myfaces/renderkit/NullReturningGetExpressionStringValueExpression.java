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
package org.apache.myfaces.renderkit;
import java.io.Serializable;

import jakarta.el.ELContext;
import jakarta.el.ELException;
import jakarta.el.PropertyNotFoundException;
import jakarta.el.PropertyNotWritableException;
import jakarta.el.ValueExpression;

public class NullReturningGetExpressionStringValueExpression extends ValueExpression implements Serializable {
        private static final long serialVersionUID = 1L;

        @Override
        public String getExpressionString() {
            return null;
        }

        @Override
        public boolean isReadOnly(ELContext arg0) {
            return true;
        }

        @Override
        public Class<?> getExpectedType() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Class<?> getType(ELContext arg0)
                throws NullPointerException, PropertyNotFoundException,
                ELException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Object getValue(ELContext arg0) throws NullPointerException,
                PropertyNotFoundException, ELException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public void setValue(ELContext arg0, Object arg1)
                throws NullPointerException, PropertyNotFoundException,
                PropertyNotWritableException, ELException {
            // TODO Auto-generated method stub
            
        }

        @Override
        public boolean equals(Object arg0) {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public int hashCode() {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public boolean isLiteralText() {
            // TODO Auto-generated method stub
            return false;
        }
    }

