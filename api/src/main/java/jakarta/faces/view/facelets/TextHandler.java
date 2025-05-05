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
package jakarta.faces.view.facelets;

import jakarta.faces.component.UIComponent;

import java.io.IOException;

/**
 * @since 2.0
 */
public interface TextHandler extends FaceletHandler
{
    public String getText();
    
    public String getText(FaceletContext ctx);

    /**
     * <p class="changed_added_5_0">
     * The default implementation throws <code>UnsupportedOperationException</code> and is provided for the sole purpose of
     * not breaking existing implementations of this interface.
     * </p>
     */
    @Override
    default void apply(FaceletContext ctx, UIComponent parent) throws IOException
    {
        throw new UnsupportedOperationException();
    }
}
