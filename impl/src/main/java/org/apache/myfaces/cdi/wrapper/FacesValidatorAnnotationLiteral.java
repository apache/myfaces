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
package org.apache.myfaces.cdi.wrapper;

import java.util.Objects;
import javax.enterprise.util.AnnotationLiteral;
import jakarta.faces.validator.FacesValidator;

public class FacesValidatorAnnotationLiteral extends AnnotationLiteral<FacesValidator> implements FacesValidator
{
    private static final long serialVersionUID = 1L;

    private String value;

    public FacesValidatorAnnotationLiteral(String value)
    {
        this.value = value;
    }

    @Override
    public String value()
    {
        return value;
    }

    @Override
    public boolean managed()
    {
        return true;
    }

    @Override
    public boolean isDefault()
    {
        return false;
    }

    @Override
    public int hashCode()
    {
        int hash = 5;
        hash = 79 * hash + Objects.hashCode(this.value);
        return hash;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        final FacesValidatorAnnotationLiteral other = (FacesValidatorAnnotationLiteral) obj;
        if (!Objects.equals(this.value, other.value))
        {
            return false;
        }
        return true;
    }

}
