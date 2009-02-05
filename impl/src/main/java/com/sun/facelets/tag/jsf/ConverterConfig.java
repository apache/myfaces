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
package com.sun.facelets.tag.jsf;

import com.sun.facelets.tag.TagConfig;

/**
 * Used in creating ConvertHandler's and all implementations.
 * 
 * @see com.sun.facelets.tag.jsf.ConvertHandler
 * @author Jacob Hookom
 * @version $Id: ConverterConfig.java,v 1.3 2008/07/13 19:01:47 rlubke Exp $
 */
public interface ConverterConfig extends TagConfig
{

    /**
     * The converter id to be used in instantiating this converter
     * 
     * @return the converter id that can be passed to Application.createConverter
     */
    public String getConverterId();

}
