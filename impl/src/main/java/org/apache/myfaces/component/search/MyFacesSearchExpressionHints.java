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
package org.apache.myfaces.component.search;

import java.util.EnumSet;
import java.util.Set;
import jakarta.faces.component.search.SearchExpressionHint;

public class MyFacesSearchExpressionHints
{
    public static final Set<SearchExpressionHint> SET_IGNORE_NO_RESULT =
            EnumSet.of(SearchExpressionHint.IGNORE_NO_RESULT);
    
    public static final Set<SearchExpressionHint> SET_RESOLVE_CLIENT_SIDE_RESOLVE_SINGLE_COMPONENT =
            EnumSet.of(SearchExpressionHint.RESOLVE_CLIENT_SIDE, SearchExpressionHint.RESOLVE_SINGLE_COMPONENT);
    
    public static final Set<SearchExpressionHint> SET_RESOLVE_SINGLE_COMPONENT_IGNORE_NO_RESULT =
            EnumSet.of(SearchExpressionHint.RESOLVE_SINGLE_COMPONENT, SearchExpressionHint.IGNORE_NO_RESULT);
}
