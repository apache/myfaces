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
package org.apache.myfaces.context.servlet;

import jakarta.faces.application.FacesMessage;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Iterator to properly handle iterating and removing FacesMessage records.
 */
public class FacesMessageIterator implements Iterator<FacesMessage>
{

    private final Map<String, List<FacesMessage>> messages;
    private int outerIndex = -1;
    private final int messagesSize;
    private Iterator<FacesMessage> inner;
    private final Iterator<String> keys;

    FacesMessageIterator(Map<String, List<FacesMessage>> messages)
    {
        this.messages = messages;
        messagesSize = messages.size();
        keys = messages.keySet().iterator();

    }

    @Override
    public boolean hasNext()
    {
        if (outerIndex == -1)
        {
            // pop our first List, if any;
            outerIndex++;
            inner = messages.get(keys.next()).iterator();

        }
        while (!inner.hasNext())
        {
            outerIndex++;
            if (outerIndex < messagesSize)
            {
                inner = messages.get(keys.next()).iterator();
            }
            else
            {
                return false;
            }
        }
        return inner.hasNext();
    }

    @Override
    public FacesMessage next()
    {
        if (outerIndex >= messagesSize)
        {
            throw new NoSuchElementException();
        }
        if (inner != null && inner.hasNext())
        {
            return inner.next();
        }
        else
        {
            // call this.hasNext() to properly initialize/position 'inner'
            if (!hasNext())
            {
                throw new NoSuchElementException();
            }
            else
            {
                return inner.next();
            }
        }
    }

    @Override
    public void remove()
    {
        if (outerIndex == -1)
        {
            throw new IllegalStateException();
        }
        inner.remove();
    }

}