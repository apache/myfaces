/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 * 
 *  http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.myfaces.util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.myfaces.test.base.junit.AbstractFacesTestCase;
import org.junit.jupiter.api.Test;

class CommentUtilsTest extends AbstractFacesTestCase
{
    @Test
    void isStartMatchWithCommentedCDATA() throws Exception
    {
        assertTrue(CommentUtils.isStartMatchWithCommentedCDATA("/*"+CommentUtils.CDATA_SIMPLE_START));
        assertTrue(CommentUtils.isStartMatchWithCommentedCDATA("/* "+CommentUtils.CDATA_SIMPLE_START));
        assertTrue(CommentUtils.isStartMatchWithCommentedCDATA("/* \n\t"+CommentUtils.CDATA_SIMPLE_START));        
    }

    @Test
    void isEndMatchWithCommentedCDATA() throws Exception
    {
        assertTrue(CommentUtils.isEndMatchWithCommentedCDATA("fkdjslkfjsl "+CommentUtils.CDATA_SIMPLE_END+"*/"));
        assertTrue(CommentUtils.isEndMatchWithCommentedCDATA("fkdjslkfjsl "+CommentUtils.CDATA_SIMPLE_END+" */"));
        assertTrue(CommentUtils.isEndMatchWithCommentedCDATA("fkdjslkfjsl "+CommentUtils.CDATA_SIMPLE_END+"\n\t */"));
    }

    @Test
    void isStartMatchWithInlineCommentedCDATA() throws Exception
    {
        assertTrue(CommentUtils.isStartMatchWithInlineCommentedCDATA("//"+CommentUtils.CDATA_SIMPLE_START));
        assertTrue(CommentUtils.isStartMatchWithInlineCommentedCDATA("// "+CommentUtils.CDATA_SIMPLE_START));
        assertTrue(CommentUtils.isStartMatchWithInlineCommentedCDATA("// \t"+CommentUtils.CDATA_SIMPLE_START));
        assertFalse(CommentUtils.isStartMatchWithInlineCommentedCDATA("// \n"+CommentUtils.CDATA_SIMPLE_START));
    }

    @Test
    void isEndMatchWithInlineCommentedCDATA() throws Exception
    {
        assertTrue(CommentUtils.isEndMatchWithInlineCommentedCDATA("fkdjslkfjsl //"+CommentUtils.CDATA_SIMPLE_END));
        assertTrue(CommentUtils.isEndMatchWithInlineCommentedCDATA("fkdjslkfjsl // "+CommentUtils.CDATA_SIMPLE_END));
        assertTrue(CommentUtils.isEndMatchWithInlineCommentedCDATA("fkdjslkfjsl //\t "+CommentUtils.CDATA_SIMPLE_END));
        assertFalse(CommentUtils.isEndMatchWithInlineCommentedCDATA("fkdjslkfjsl //\n\t "+CommentUtils.CDATA_SIMPLE_END));
    }

    @Test
    void isEndMatchtWithInlineCommentedXmlCommentTag() throws Exception
    {
        assertTrue(CommentUtils.isEndMatchtWithInlineCommentedXmlCommentTag("fkdjslkfjsl //"+CommentUtils.COMMENT_SIMPLE_END));
        assertTrue(CommentUtils.isEndMatchtWithInlineCommentedXmlCommentTag("fkdjslkfjsl // "+CommentUtils.COMMENT_SIMPLE_END));
        assertTrue(CommentUtils.isEndMatchtWithInlineCommentedXmlCommentTag("fkdjslkfjsl //\t "+CommentUtils.COMMENT_SIMPLE_END));
        assertFalse(CommentUtils.isEndMatchtWithInlineCommentedXmlCommentTag("fkdjslkfjsl //\n\t "+CommentUtils.COMMENT_SIMPLE_END));        
    }
}
