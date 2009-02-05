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
package com.sun.facelets;

import java.io.IOException;

import javax.el.ELException;
import javax.faces.FacesException;
import javax.faces.webapp.pdl.facelets.FaceletException;

/**
 * FaceletFactory for producing Facelets relative to the context of the underlying implementation.
 * 
 * @author Jacob Hookom
 * @version $Id: FaceletFactory.java,v 1.4 2008/07/13 19:01:39 rlubke Exp $
 */
public abstract class FaceletFactory
{

    private static ThreadLocal Instance = new ThreadLocal();

    /**
     * Return a Facelet instance as specified by the file at the passed URI.
     * 
     * @param uri
     * @return
     * @throws IOException
     * @throws FaceletException
     * @throws FacesException
     * @throws ELException
     */
    public abstract Facelet getFacelet(String uri) throws IOException;

    /**
     * Set the static instance
     * 
     * @param factory
     */
    public static final void setInstance(FaceletFactory factory)
    {
        Instance.set(factory);
    }

    /**
     * Get the static instance
     * 
     * @return
     */
    public static final FaceletFactory getInstance()
    {
        return (FaceletFactory) Instance.get();
    }
}
