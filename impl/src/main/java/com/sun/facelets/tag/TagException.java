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
package com.sun.facelets.tag;

import javax.faces.webapp.pdl.facelets.FaceletException;

/**
 * An Exception caused by a Tag
 * 
 * @author Jacob Hookom
 * @version $Id: TagException.java,v 1.4 2008/07/13 19:01:35 rlubke Exp $
 */
public final class TagException extends FaceletException
{

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * 
     */
    public TagException(Tag tag)
    {
        super(tag.toString());
    }

    /**
     * @param message
     */
    public TagException(Tag tag, String message)
    {
        super(tag + " " + message);
    }

    /**
     * @param cause
     */
    public TagException(Tag tag, Throwable cause)
    {
        super(tag.toString(), cause);
    }

    /**
     * @param message
     * @param cause
     */
    public TagException(Tag tag, String message, Throwable cause)
    {
        super(tag + " " + message, cause);
    }

}
