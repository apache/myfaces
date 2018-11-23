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
package org.apache.myfaces.view.facelets.tag;

import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.Metadata;

/**
 * 
 * @author Jacob Hookom
 * @version $Id$
 */
public final class MetadataImpl extends Metadata
{
    private final Metadata[] _mappers;
    private final int _size;

    public MetadataImpl(Metadata[] mappers)
    {
        _mappers = mappers;
        _size = mappers.length;
    }

    @Override
    public void applyMetadata(FaceletContext ctx, Object instance)
    {
        for (int i = 0; i < _size; i++)
        {
            _mappers[i].applyMetadata(ctx, instance);
        }
    }
}
