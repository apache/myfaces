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
package org.apache.myfaces.webapp.webxml;

import java.util.logging.Logger;

import javax.faces.context.ExternalContext;

import org.apache.myfaces.config.MyfacesConfig;

public class WebXml
{
    private static final Logger log = Logger.getLogger(WebXml.class.getName());

    private static long refreshPeriod;
    private long parsingTime;

    private boolean errorPagePresent = false;

    protected void setParsingTime(long parsingTime)
    {
        this.parsingTime = parsingTime;
    }

    /**
     * Sets if, the web.xml contains an error-page entry
     * @param errorPagePresent
     */
    public void setErrorPagePresent(boolean errorPagePresent)
    {
        this.errorPagePresent = errorPagePresent;
    }
    
    /**
     * Determines, if the web.xml contains an error-page entry
     * @return
     */
    public boolean isErrorPagePresent()
    {
        return errorPagePresent;
    }

    protected boolean isOld(ExternalContext context)
    {
        if (refreshPeriod > 0)
        {
            long ttl = this.parsingTime + refreshPeriod;
            if (System.currentTimeMillis() > ttl)
            {
                long lastModified = WebXmlParser.getWebXmlLastModified(context);
                return lastModified == 0 || lastModified > ttl;
            }
        }
        return false;
    }

    private static final String WEB_XML_ATTR = WebXml.class.getName();
    public static WebXml getWebXml(ExternalContext context)
    {
        WebXml webXml = (WebXml)context.getApplicationMap().get(WEB_XML_ATTR);
        if (webXml == null)
        {
            init(context);
            webXml = (WebXml)context.getApplicationMap().get(WEB_XML_ATTR);
        }
        return webXml;
    }

    /**
     * should be called when initialising Servlet
     * @param context
     */
    public static void init(ExternalContext context)
    {
        WebXmlParser parser = new WebXmlParser(context);
        WebXml webXml = parser.parse();
        context.getApplicationMap().put(WEB_XML_ATTR, webXml);
        MyfacesConfig mfconfig = MyfacesConfig.getCurrentInstance(context);
        long configRefreshPeriod = mfconfig.getConfigRefreshPeriod();
        webXml.setParsingTime(System.currentTimeMillis());
        refreshPeriod = (configRefreshPeriod * 1000);
    }

    public static void update(ExternalContext context)
    {
        if (getWebXml(context).isOld(context))
        {
            WebXml.init(context);
        }
    }

}
