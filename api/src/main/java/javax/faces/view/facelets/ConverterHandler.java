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
package javax.faces.view.facelets;

import javax.faces.view.ValueHolderAttachedObjectHandler;

/**
 * Handles setting a Converter instance on a ValueHolder. Will wire all attributes set to the Converter instance
 * created/fetched. Uses the "binding" attribute for grabbing instances to apply attributes to. <p/> Will only
 * set/create Converter is the passed UIComponent's parent is null, signifying that it wasn't restored from an existing
 * tree.
 * 
 * @see javax.faces.webapp.ConverterELTag
 * @see javax.faces.convert.Converter
 * @see javax.faces.component.ValueHolder
 * @author Jacob Hookom
 * @version $Id: ConvertHandler.java,v 1.4 2008/07/13 19:01:46 rlubke Exp $
 */
public class ConverterHandler extends FaceletsAttachedObjectHandler implements ValueHolderAttachedObjectHandler
{
    private String converterId;
    
    public ConverterHandler(ConverterConfig config)
    {
        super(config);
        
        converterId = config.getConverterId();
    }
    
    public String getConverterId (FaceletContext ctx)
    {
        return converterId;
    }
    
    protected TagHandlerDelegate getTagHandlerHelper()
    {
        return delegateFactory.createConverterHandlerDelegate (this);
    }
}
