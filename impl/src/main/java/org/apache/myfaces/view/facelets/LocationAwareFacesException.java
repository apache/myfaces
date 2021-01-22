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
package org.apache.myfaces.view.facelets;

import jakarta.faces.FacesException;
import jakarta.faces.component.UIComponent;
import jakarta.faces.view.Location;

public class LocationAwareFacesException extends FacesException implements LocationAware
{
    private Location location;
    
    public LocationAwareFacesException()
    {
        super();
    }
    
    public LocationAwareFacesException(Location location)
    {
        super();
        this.location = location;
    }

    public LocationAwareFacesException(Throwable cause)
    {
        super(cause);
    }
    
    public LocationAwareFacesException(Throwable cause, Location location)
    {
        super(cause);
        this.location = location;
    }
    
    public LocationAwareFacesException(Throwable cause, UIComponent component)
    {
        super(cause);
        this.location = (Location) component.getAttributes().get(UIComponent.VIEW_LOCATION_KEY);
    }

    public LocationAwareFacesException(String message)
    {
        super(message);
    }

    public LocationAwareFacesException(String message, Location location)
    {
        super(message);
        this.location = location;
    }
    
    public LocationAwareFacesException(String message, UIComponent component)
    {
        super(message);
        this.location = (Location) component.getAttributes().get(UIComponent.VIEW_LOCATION_KEY);
    }
    
    public LocationAwareFacesException(String message, Throwable cause)
    {
        super(message, cause);
    }
    
    public LocationAwareFacesException(String message, Throwable cause, Location location)
    {
        super(message, cause);
        this.location = location;
    }

    @Override
    public Location getLocation()
    {
        return location;
    }
}
