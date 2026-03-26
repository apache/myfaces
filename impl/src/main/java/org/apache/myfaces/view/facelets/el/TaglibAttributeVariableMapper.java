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
package org.apache.myfaces.view.facelets.el;

import jakarta.el.VariableMapper;

/**
 * Marks VariableMapper layers that bind Facelets taglib attribute short names
 * ({@link org.apache.myfaces.view.facelets.tag.UserTagHandler}). Such bindings must not
 * apply inside nested composite component implementations (MYFACES-4589), where identifiers
 * like {@code #{color}} must resolve to managed beans (e.g. CDI) instead of shadowing
 * ancestor taglib attributes named {@code color}.
 */
public final class TaglibAttributeVariableMapper extends VariableMapperWrapper
{
    public TaglibAttributeVariableMapper(VariableMapper orig)
    {
        super(orig);
    }

    /**
     * Removes all taglib attribute mapper layers from the top of the chain.
     */
    public static VariableMapper unwrapTaglibAttributeScopes(VariableMapper mapper)
    {
        while (mapper instanceof TaglibAttributeVariableMapper)
        {
            mapper = ((TaglibAttributeVariableMapper) mapper).getWrapped();
        }
        return mapper;
    }
}
