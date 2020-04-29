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
package org.apache.myfaces.cdi.behavior;

import java.util.Objects;
import jakarta.enterprise.util.AnnotationLiteral;
import jakarta.faces.component.behavior.FacesBehavior;

/**
 *
 */
public class FacesBehaviorAnnotationLiteral extends AnnotationLiteral<FacesBehavior> implements FacesBehavior
{
    private static final long serialVersionUID = 1L;

    private String value;
    private boolean managed;

    public FacesBehaviorAnnotationLiteral(String value, boolean managed)
    {
        this.value = value;
        this.managed = managed;
    }

    @Override
    public String value()
    {
        return value;
    }

    @Override
    public boolean managed()
    {
        return managed;
    }

    @Override
    public int hashCode()
    {
        int hash = 5;
        hash = 79 * hash + Objects.hashCode(this.value);
        hash = 79 * hash + (this.managed ? 1 : 0);
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
        final FacesBehaviorAnnotationLiteral other = (FacesBehaviorAnnotationLiteral) obj;
        if (!Objects.equals(this.value, other.value))
        {
            return false;
        }
        if (this.managed != other.managed)
        {
            return false;
        }
        return true;
    }

}
