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
package org.apache.myfaces.core.api.shared;

import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

import jakarta.faces.application.FacesMessage;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ParametrizableFacesMessageTest
{
    @Test
    public void testMessagesWithDifferentParametersAreNotEqual()
    {
        FacesMessage firstMessage = new ParametrizableFacesMessage(
                FacesMessage.Severity.ERROR,
                "{0}: {1}",
                "{0}: {1}",
                new Object[] { "input", "should be uppercase" },
                Locale.ENGLISH);
        FacesMessage secondMessage = new ParametrizableFacesMessage(
                FacesMessage.Severity.ERROR,
                "{0}: {1}",
                "{0}: {1}",
                new Object[] { "input", "minimum size is 2" },
                Locale.ENGLISH);

        Set<FacesMessage> messages = new LinkedHashSet<>();
        messages.add(firstMessage);
        messages.add(secondMessage);

        Assertions.assertNotEquals(firstMessage, secondMessage);
        Assertions.assertEquals(2, messages.size());
    }

    @Test
    public void testMessagesWithSameParametersAreEqual()
    {
        FacesMessage firstMessage = new ParametrizableFacesMessage(
                FacesMessage.Severity.ERROR,
                "{0}: {1}",
                "{0}: {1}",
                new Object[] { "input", "should be uppercase" },
                Locale.ENGLISH);
        FacesMessage secondMessage = new ParametrizableFacesMessage(
                FacesMessage.Severity.ERROR,
                "{0}: {1}",
                "{0}: {1}",
                new Object[] { "input", "should be uppercase" },
                Locale.ENGLISH);

        Assertions.assertEquals(firstMessage, secondMessage);
        Assertions.assertEquals(firstMessage.hashCode(), secondMessage.hashCode());
    }

    @Test
    public void testMessageParametersAreUsedForFormatting()
    {
        FacesMessage message = new ParametrizableFacesMessage(
                FacesMessage.Severity.ERROR,
                "{0}: {1}",
                "Detail for {0}: {1}",
                new Object[] { "input", "should be uppercase" },
                Locale.ENGLISH);

        Assertions.assertEquals("input: should be uppercase", message.getSummary());
        Assertions.assertEquals("Detail for input: should be uppercase", message.getDetail());
    }
}