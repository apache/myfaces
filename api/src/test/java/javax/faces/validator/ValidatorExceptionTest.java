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
package javax.faces.validator;

import org.junit.Test;

import javax.faces.application.FacesMessage;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class ValidatorExceptionTest
{
    @Test
    public void singleFacesMessageNullSummary()
    {
        FacesMessage message = new FacesMessage(null, null);

        ValidatorException exception = new ValidatorException(message);
        assertEquals("", exception.getMessage());
    }

    @Test
    public void singleFacesMessageWithDetail()
    {
        FacesMessage message = new FacesMessage("summary", "detail");

        ValidatorException exception = new ValidatorException(message);
        assertEquals("summary: detail", exception.getMessage());
    }

    @Test
    public void singleFacesMessageEmptyDetail()
    {
        FacesMessage message = new FacesMessage("summary", "");

        ValidatorException exception = new ValidatorException(message);
        assertEquals("summary", exception.getMessage());

    }

    @Test
    public void singleFacesMaessageNullDetail()
    {
        FacesMessage message = new FacesMessage("summary", null);

        ValidatorException exception = new ValidatorException(message);
        assertEquals("summary: summary", exception.getMessage());
    }

    @Test
    public void multipleFacesMessagesSummaryNull()
    {
        List<FacesMessage> messages = new ArrayList<>();
        messages.add(new FacesMessage(null, null));
        messages.add(new FacesMessage("summary2", "detail2"));

        ValidatorException exception = new ValidatorException(messages);
        assertEquals("summary2: detail2", exception.getMessage());
    }


    @Test
    public void multipleFacesMessagesWithDetail()
    {
        List<FacesMessage> messages = new ArrayList<>();
        messages.add(new FacesMessage("summary1", "detail1"));
        messages.add(new FacesMessage("summary2", "detail2"));

        ValidatorException exception = new ValidatorException(messages);
        assertEquals("summary1: detail1, summary2: detail2", exception.getMessage());
    }

    @Test
    public void multipleFacesMessagesEmptyDetail()
    {
        List<FacesMessage> messages = new ArrayList<>();
        messages.add(new FacesMessage("summary1", ""));
        messages.add(new FacesMessage("summary2", "detail2"));

        ValidatorException exception = new ValidatorException(messages);
        assertEquals("summary1, summary2: detail2", exception.getMessage());

    }

    @Test
    public void multipleFacesMessagesNullDetail()
    {
        List<FacesMessage> messages = new ArrayList<>();
        messages.add(new FacesMessage("summary1", null));
        messages.add(new FacesMessage("summary2", "detail2"));

        ValidatorException exception = new ValidatorException(messages);
        assertEquals("summary1: summary1, summary2: detail2", exception.getMessage());
    }
}
