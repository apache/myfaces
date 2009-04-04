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
package org.apache.myfaces.view.facelets.tag;

import javax.faces.view.facelets.Tag;

/**
 * Provides the ability to completely change the Tag before it's processed for compiling with the associated TagHandler.
 * <p /> You could take &lt;input type="text" /> and convert it to &lth:inputText /> before compiling.
 * 
 * @author Jacob Hookom
 * @version $Id: TagDecorator.java,v 1.3 2008/07/13 19:01:35 rlubke Exp $
 */
public interface TagDecorator
{

    /**
     * If handled, return a new Tag instance, otherwise return null
     * 
     * @param tag
     *            tag to be decorated
     * @return a decorated tag, otherwise null
     */
    public Tag decorate(Tag tag);
}
