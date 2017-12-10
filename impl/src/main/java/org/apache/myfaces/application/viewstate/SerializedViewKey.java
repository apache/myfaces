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
package org.apache.myfaces.application.viewstate;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Base implementation where all keys used to identify the state of a view should
 * extend.
 */
class SerializedViewKey implements Serializable
{
    final int _viewId;
    final byte[] _sequenceId;

    public SerializedViewKey(int viewId, byte[] sequence)
    {
        _sequenceId = sequence;
        _viewId = viewId;
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
        if (this._viewId != other._viewId)
        {
            return false;
        }
        if (!Arrays.equals(this._sequenceId, other._sequenceId))
        {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode()
    {
        int hash = 5;
        hash = 37 * hash + this._viewId;
        hash = 37 * hash + Arrays.hashCode(this._sequenceId);
        return hash;
    }
}
