/*
 * Copyright 2007 The Apache Software Foundation.
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
package org.apache.myfaces.config.impl.digester.elements;

/**
 * @author Mathias Broekelmann (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class ResourceBundle
{
    private String baseName;
    private String var;

    /**
     * @return the baseName
     */
    public String getBaseName()
    {
        return baseName;
    }

    /**
     * @param baseName
     *            the baseName to set
     */
    public void setBaseName(String baseName)
    {
        this.baseName = baseName;
    }

    /**
     * @return the var
     */
    public String getVar()
    {
        return var;
    }

    /**
     * @param var
     *            the var to set
     */
    public void setVar(String var)
    {
        this.var = var;
    }
}
