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
package org.apache.myfaces.renderkit.viewstate;

import java.io.Serializable;
import java.util.Arrays;

/**
 *
 */
public class SerializedViewKey implements Serializable
{
    private static final long serialVersionUID = -1170697124386063642L;

    private final String _viewId;
    private final int _viewIdHash;

    private final Object _sequenceId;
    private final byte[] _sequenceIdArray;

    public SerializedViewKey(String viewId, Object sequence)
    {
        _viewId = viewId;
        _viewIdHash = viewId != null ? viewId.hashCode() : 0;

        if (sequence == null)
        {
            _sequenceId = null;
            _sequenceIdArray = null;
        }
        else if (sequence instanceof byte[])
        {
            _sequenceId = null;
            _sequenceIdArray = (byte[]) sequence;
        }
        else
        {
            _sequenceId = sequence;
            _sequenceIdArray = null;
        }
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
        final SerializedViewKey other = (SerializedViewKey) obj;
        if (this._viewIdHash != other._viewIdHash)
        {
            return false;
        }

        if (this._viewId == null || !this._viewId.equals(other._viewId))
        {
            return false;
        }

        if (this._sequenceId == null && other._sequenceId != null ||
            this._sequenceId != null && other._sequenceId == null)
        {
            return false;
        }

        if (this._sequenceId != null &&
            !this._sequenceId.equals(other._sequenceId))
        {
            return false;
        }

        if (this._sequenceIdArray == null && other._sequenceIdArray != null ||
            this._sequenceIdArray != null && other._sequenceIdArray == null)
        {
            return false;
        }

        if (this._sequenceIdArray != null &&
            Arrays.equals(this._sequenceIdArray, other._sequenceIdArray))
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 83 * hash + this._viewIdHash;
        hash = 83 * hash + (this._sequenceId != null ? this._sequenceId.hashCode() : 0);
        hash = 83 * hash + (this._sequenceIdArray != null ? this._sequenceIdArray.hashCode() : 0);
        return hash;
    }

}
