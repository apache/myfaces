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
package org.apache.myfaces.flow;

import java.io.Serializable;

/**
 * This class works as a reference that can be stored into session to 
 * the current flow.
 * 
 * @author Leonardo Uribe
 */
public class FlowReference implements Serializable
{
    private String documentId;
    private String id;

    public FlowReference(String documentId, String id)
    {
        this.documentId = documentId;
        this.id = id;
    }

    public String getDocumentId()
    {
        return documentId;
    }

    public void setDocumentId(String documentId)
    {
        this.documentId = documentId;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }
    
    @Override
    public int hashCode()
    {
        int hash = 3;
        hash = 23 * hash + (this.documentId != null ? this.documentId.hashCode() : 0);
        hash = 23 * hash + (this.id != null ? this.id.hashCode() : 0);
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
        final FlowReference other = (FlowReference) obj;
        if ((this.documentId == null) ? (other.documentId != null) : !this.documentId.equals(other.documentId))
        {
            return false;
        }
        if ((this.id == null) ? (other.id != null) : !this.id.equals(other.id))
        {
            return false;
        }
        return true;
    }
    
}
