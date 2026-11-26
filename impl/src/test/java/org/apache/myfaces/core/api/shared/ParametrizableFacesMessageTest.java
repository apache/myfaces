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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

import jakarta.faces.application.FacesMessage;

import org.junit.jupiter.api.Test;

class ParametrizableFacesMessageTest
{
    @Test
    void messagesWithDifferentParametersAreNotEqual()
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

        assertNotEquals(firstMessage, secondMessage);
        assertEquals(2, messages.size());
    }

    @Test
    void messagesWithSameParametersAreEqual()
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

        assertEquals(firstMessage, secondMessage);
        assertEquals(firstMessage.hashCode(), secondMessage.hashCode());
    }

    @Test
    void messageParametersAreUsedForFormatting()
    {
        FacesMessage message = new ParametrizableFacesMessage(
                FacesMessage.Severity.ERROR,
                "{0}: {1}",
                "Detail for {0}: {1}",
                new Object[] { "input", "should be uppercase" },
                Locale.ENGLISH);

        assertEquals("input: should be uppercase", message.getSummary());
        assertEquals("Detail for input: should be uppercase", message.getDetail());
    }
}