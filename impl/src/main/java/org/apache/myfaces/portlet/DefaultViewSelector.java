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
package org.apache.myfaces.portlet;

import javax.portlet.PortletContext;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

/**
 * Imlementations of this interface allow a JSF application to specify which
 * JSF view will be selected when the incoming request does not provide a View
 * Id.  The implementation can optionally return <code>null</code> to revert to
 * the default View Id specified in portlet.xml.
 *
 * @author  Stan Silvert (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public interface DefaultViewSelector {

    /**
     * This method will be called by the MyFacesGenericPortlet in order to
     * give the selector an opportunity to store a reference to the
     * PortletContext.
     */
    public void setPortletContext(PortletContext portletContext);

    /**
     * This method allows a JSF application to specify which JSF view will be
     * when the incoming request does not provide a view id.
     *
     * @param request The RenderRequest
     * @param response The RenderResponse
     * @return a JSF View Id, or <code>null</code> if the selector wishes to
     *         revert to the default View Id specified in portlet.xml.
     * @throws PortletException if a View Id can not be determined because of
     *                          some underlying error.
     */
    public String selectViewId(RenderRequest request, RenderResponse response) throws PortletException;
}
