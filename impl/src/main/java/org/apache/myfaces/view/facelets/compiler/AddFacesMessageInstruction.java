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
package org.apache.myfaces.view.facelets.compiler;

import java.io.IOException;

import jakarta.el.ELContext;
import jakarta.el.ExpressionFactory;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;

import org.apache.myfaces.view.facelets.AbstractFaceletContext;

/**
 * Adds a message that will be added on build view time. 
 * 
 * @author Leonardo Uribe
 *
 */
final class AddFacesMessageInstruction implements Instruction
{
    private final FacesMessage.Severity severity;
    private final String summary;
    private final String detail;
    
    public AddFacesMessageInstruction(FacesMessage.Severity severity, String summary, String detail)
    {
        this.severity = severity;
        this.summary = summary;
        this.detail = detail;
    }

    @Override
    public void write(FacesContext context) throws IOException
    {
    }

    @Override
    public Instruction apply(ExpressionFactory factory, ELContext ctx)
    {
        FacesContext facesContext = ((AbstractFaceletContext)ctx).getFacesContext();
        facesContext.addMessage(null, new FacesMessage(this.severity, this.summary, this.detail));
        return this;
    }

    @Override
    public boolean isLiteral()
    {
        return false;
    }

}
