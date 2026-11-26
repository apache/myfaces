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
package org.apache.myfaces.view.facelets.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.myfaces.view.facelets.AbstractFaceletTestCase;
import org.apache.myfaces.view.facelets.compiler.SAXCompiler;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link DefaultFaceletFactory#_removeFirst(String, String)} (same-package access to protected).
 */
class DefaultFaceletFactoryTest extends AbstractFaceletTestCase
{
    @Test
    void removeFirstPrefix() throws Exception
    {
        DefaultFaceletFactory factory = new DefaultFaceletFactory(new SAXCompiler());
        assertEquals("/y/z", factory._removeFirst("/x/y/z", "/x"));
    }

    @Test
    void removeFirstOnlyFirstOccurrence() throws Exception
    {
        DefaultFaceletFactory factory = new DefaultFaceletFactory(new SAXCompiler());
        assertEquals("/a/b/a", factory._removeFirst("/a/b/a/b/a", "/a/b"));
    }

    @Test
    void removeFirstNotFound() throws Exception
    {
        DefaultFaceletFactory factory = new DefaultFaceletFactory(new SAXCompiler());
        assertEquals("/unchanged", factory._removeFirst("/unchanged", "/missing"));
    }

    @Test
    void removeFirstEmptyToRemove() throws Exception
    {
        DefaultFaceletFactory factory = new DefaultFaceletFactory(new SAXCompiler());
        assertEquals("/same", factory._removeFirst("/same", ""));
    }

    @Test
    void removeFirstNullToRemove() throws Exception
    {
        DefaultFaceletFactory factory = new DefaultFaceletFactory(new SAXCompiler());
        assertEquals("/same", factory._removeFirst("/same", null));
    }

    @Test
    void removeFirstRegexMetacharactersAreLiteral() throws Exception
    {
        DefaultFaceletFactory factory = new DefaultFaceletFactory(new SAXCompiler());
        assertEquals("prefixsuffix", factory._removeFirst("prefix.+suffix", ".+"));
    }

    @Test
    void removeFirstWholeString() throws Exception
    {
        DefaultFaceletFactory factory = new DefaultFaceletFactory(new SAXCompiler());
        assertEquals("", factory._removeFirst("/only", "/only"));
    }
}
