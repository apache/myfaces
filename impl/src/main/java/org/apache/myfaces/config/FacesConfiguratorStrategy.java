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
package org.apache.myfaces.config;

import org.apache.myfaces.config.impl.digester.elements.FacesConfig;
import org.apache.myfaces.config.impl.digester.DigesterFacesConfigDispenserImpl;

import javax.faces.context.ExternalContext;
import javax.faces.FacesException;

/**
 * Abstraction for configuring the JSF runtime. The implementation can use one of
 * several ways to configure the runtime. For example, XML, annotations, Spring, Guice, etc.
 *
 * @author Jan-Kees van Andel
 * @version $Revision: 743355 $ $Date: 2009-02-11 16:02:41 +0100 (wo, 11 feb 2009) $
 * @since 2.0
 */
public abstract class FacesConfiguratorStrategy
{

    /**
     * The ExternalContext used when starting up.
     */
    protected final ExternalContext _externalContext;

    /**
     * The faces config dispenser.
     */
    private FacesConfigDispenser<FacesConfig> _dispenser;

    /**
     * ExternalContext constructor.
     * @param externalContext The ExternalContext used when starting up.
     */
    public FacesConfiguratorStrategy(ExternalContext externalContext)
    {
        if (externalContext == null)
        {
            throw new IllegalArgumentException("external context must not be null");
        }
        this._externalContext = externalContext;
    }

    /**
     * This method takes care of locating JSF artifacts to configure.
     */
    public abstract void feed() throws FacesException;

    /**
     * This method really configure the given artifacts into the JSF runtime.
     */
    public abstract void configure() throws FacesException;

    /**
     * Get the dispenser.
     *
     * @return The dispenser.
     */
    public final FacesConfigDispenser<FacesConfig> getDispenser() {
        return _dispenser;
    }

    /**
     * Set the dispenser.
     *
     * @param dispenser The dispenser.
     */
    public final void setDispenser(FacesConfigDispenser<FacesConfig> dispenser) {
        this._dispenser = dispenser;
    }
}
