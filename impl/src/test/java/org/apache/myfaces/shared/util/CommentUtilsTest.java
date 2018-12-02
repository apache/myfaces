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
package org.apache.myfaces.shared.util;

import org.apache.myfaces.util.CommentUtils;
import java.io.IOException;

import org.apache.myfaces.test.base.junit4.AbstractJsfTestCase;
import org.junit.Test;

public class CommentUtilsTest extends AbstractJsfTestCase
{
    @Test
    public void testIsStartMatchWithCommentedCDATA() throws IOException
    {
        org.junit.Assert.assertTrue(CommentUtils.isStartMatchWithCommentedCDATA("/*"+CommentUtils.CDATA_SIMPLE_START));
        org.junit.Assert.assertTrue(CommentUtils.isStartMatchWithCommentedCDATA("/* "+CommentUtils.CDATA_SIMPLE_START));
        org.junit.Assert.assertTrue(CommentUtils.isStartMatchWithCommentedCDATA("/* \n\t"+CommentUtils.CDATA_SIMPLE_START));        
    }
    
    @Test
    public void testIsEndMatchWithCommentedCDATA() throws IOException
    {
        org.junit.Assert.assertTrue(CommentUtils.isEndMatchWithCommentedCDATA("fkdjslkfjsl "+CommentUtils.CDATA_SIMPLE_END+"*/"));
        org.junit.Assert.assertTrue(CommentUtils.isEndMatchWithCommentedCDATA("fkdjslkfjsl "+CommentUtils.CDATA_SIMPLE_END+" */"));
        org.junit.Assert.assertTrue(CommentUtils.isEndMatchWithCommentedCDATA("fkdjslkfjsl "+CommentUtils.CDATA_SIMPLE_END+"\n\t */"));
    }
    
    @Test
    public void testIsStartMatchWithInlineCommentedCDATA() throws IOException
    {
        org.junit.Assert.assertTrue(CommentUtils.isStartMatchWithInlineCommentedCDATA("//"+CommentUtils.CDATA_SIMPLE_START));
        org.junit.Assert.assertTrue(CommentUtils.isStartMatchWithInlineCommentedCDATA("// "+CommentUtils.CDATA_SIMPLE_START));
        org.junit.Assert.assertTrue(CommentUtils.isStartMatchWithInlineCommentedCDATA("// \t"+CommentUtils.CDATA_SIMPLE_START));
        org.junit.Assert.assertFalse(CommentUtils.isStartMatchWithInlineCommentedCDATA("// \n"+CommentUtils.CDATA_SIMPLE_START));
    }

    @Test
    public void testIsEndMatchWithInlineCommentedCDATA() throws IOException
    {
        org.junit.Assert.assertTrue(CommentUtils.isEndMatchWithInlineCommentedCDATA("fkdjslkfjsl //"+CommentUtils.CDATA_SIMPLE_END));
        org.junit.Assert.assertTrue(CommentUtils.isEndMatchWithInlineCommentedCDATA("fkdjslkfjsl // "+CommentUtils.CDATA_SIMPLE_END));
        org.junit.Assert.assertTrue(CommentUtils.isEndMatchWithInlineCommentedCDATA("fkdjslkfjsl //\t "+CommentUtils.CDATA_SIMPLE_END));
        org.junit.Assert.assertFalse(CommentUtils.isEndMatchWithInlineCommentedCDATA("fkdjslkfjsl //\n\t "+CommentUtils.CDATA_SIMPLE_END));
    }
    
    @Test
    public void testIsEndMatchtWithInlineCommentedXmlCommentTag() throws IOException
    {
        org.junit.Assert.assertTrue(CommentUtils.isEndMatchtWithInlineCommentedXmlCommentTag("fkdjslkfjsl //"+CommentUtils.COMMENT_SIMPLE_END));
        org.junit.Assert.assertTrue(CommentUtils.isEndMatchtWithInlineCommentedXmlCommentTag("fkdjslkfjsl // "+CommentUtils.COMMENT_SIMPLE_END));
        org.junit.Assert.assertTrue(CommentUtils.isEndMatchtWithInlineCommentedXmlCommentTag("fkdjslkfjsl //\t "+CommentUtils.COMMENT_SIMPLE_END));
        org.junit.Assert.assertFalse(CommentUtils.isEndMatchtWithInlineCommentedXmlCommentTag("fkdjslkfjsl //\n\t "+CommentUtils.COMMENT_SIMPLE_END));        
    }
}
