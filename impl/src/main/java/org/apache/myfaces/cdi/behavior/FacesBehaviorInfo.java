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

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.Objects;

public class FacesBehaviorInfo implements Serializable
{
    private Type type;
    private String behaviorId;

    public FacesBehaviorInfo(Type type, String behaviorId)
    {
        this.type = type;
        this.behaviorId = behaviorId;
    }

    public String getBehaviorId()
    {
        return behaviorId;
    }

    public void setBehaviorId(String behaviorId)
    {
        this.behaviorId = behaviorId;
    }

    public Type getType()
    {
        return type;
    }

    public void setType(Type type)
    {
        this.type = type;
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 53 * hash + Objects.hashCode(this.type);
        hash = 53 * hash + Objects.hashCode(this.behaviorId);
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
        final FacesBehaviorInfo other = (FacesBehaviorInfo) obj;
        if (!Objects.equals(this.type, other.type))
        {
            return false;
        }
        if (!Objects.equals(this.behaviorId, other.behaviorId))
        {
            return false;
        }
        return true;
    }
}
