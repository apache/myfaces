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
package javax.faces.context;

import java.io.IOException;
import java.util.Map;

/**
 * @author Simon Lessard (latest modification by $Author: slessard $)
 * @version $Revision: 696523 $ $Date: 2009-03-14 17:54:13 -0400 (mer., 17 sept. 2008) $
 * 
 * @since 2.0
 */
public class PartialResponseWriter extends ResponseWriterWrapper
{
    public static final String RENDER_ALL_MARKER = "javax.faces.ViewRoot";
    public static final String VIEW_STATE_MARKER = "javax.faces.ViewState";

    private ResponseWriter _wrapped;

    /**
     * 
     */
    public PartialResponseWriter(ResponseWriter writer)
    {
        _wrapped = writer;
    }

    public void delete(String targetId) throws IOException
    {
        // TODO: IMPLEMENT HERE
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void endDocument() throws IOException
    {
        // TODO: IMPLEMENT HERE
    }

    public void endError() throws IOException
    {
        // TODO: IMPLEMENT HERE
    }

    public void endEval() throws IOException
    {
        // TODO: IMPLEMENT HERE
    }

    public void endExtension() throws IOException
    {
        // TODO: IMPLEMENT HERE
    }

    public void endInsert() throws IOException
    {
        // TODO: IMPLEMENT HERE
    }

    public void endUpdate() throws IOException
    {
        // TODO: IMPLEMENT HERE
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseWriter getWrapped()
    {
        return _wrapped;
    }

    public void redirect(String url) throws IOException
    {
        // TODO: IMPLEMENT HERE
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void startDocument() throws IOException
    {
        // TODO: IMPLEMENT HERE
    }

    public void startError(String errorName) throws IOException
    {
        // TODO: IMPLEMENT HERE
    }

    public void startEval() throws IOException
    {
        // TODO: IMPLEMENT HERE
    }

    public void startExtension(Map<String, String> attributes) throws IOException
    {
        // TODO: IMPLEMENT HERE
    }

    public void startInsertAfter(String targetId) throws IOException
    {
        // TODO: IMPLEMENT HERE
    }

    public void startInsertBefore(String targetId) throws IOException
    {
        // TODO: IMPLEMENT HERE
    }

    public void startUpdate(String targetId) throws IOException
    {
        // TODO: IMPLEMENT HERE
    }

    public void updateAttributes(String targetId, Map<String, String> attributes) throws IOException
    {
        // TODO: IMPLEMENT HERE
    }
}
