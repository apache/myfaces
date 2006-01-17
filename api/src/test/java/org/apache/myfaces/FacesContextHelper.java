/*
 * Copyright 2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.myfaces;

import javax.faces.context.FacesContext;

/**
 * Simple test helper class to allow unit tests to configure
 * mock FacesContext objects as the "current instance".
 * <p>
 * The method FacesContext.setCurrentInstance is protected, and
 * hence cannot be accessed by unit tests wanting to configure
 * a mock object as the value seen by code calling method
 * FacesContext.getCurrentInstance().
 * <p>
 * This class is abstract because an instance is not needed
 * in order to invoke the static helper method it provides.
 */

public abstract class FacesContextHelper extends FacesContext
{
    public static void setCurrentInstance(FacesContext other)
    {
        FacesContext.setCurrentInstance(other);
    }
}

