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
package javax.faces.component;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;

class _PropertyDescriptorHolder
{
    private final PropertyDescriptor _descriptor;
    private final Method _readMethod;
    private Method _writeMethod;

    public _PropertyDescriptorHolder(PropertyDescriptor descriptor)
    {
        _descriptor = descriptor;
        _readMethod = _descriptor.getReadMethod();
    }

    public _PropertyDescriptorHolder(PropertyDescriptor descriptor, Method readMethod)
    {
        _descriptor = descriptor;
        _readMethod = readMethod;
    }
    
    public String getName()
    {
        return _descriptor.getName();
    }
    
    public Method getReadMethod()
    {
        return _readMethod;
    }
    
    public Method getWriteMethod()
    {
        // In facelets, the Method instance used to write the variable is stored
        // in a variable (see org.apache.myfaces.view.facelets.tag.BeanPropertyTagRule),
        // so the impact of this synchronized call at the end is minimal compared with 
        // getReadMethod. That's the reason why cache it here in a lazy way is enough
        // instead retrieve it as soon as this holder is created.
        if (_writeMethod == null)
        {
            _writeMethod = _descriptor.getWriteMethod(); 
        }
        return _writeMethod;
    }
    
    public PropertyDescriptor getPropertyDescriptor()
    {
        return _descriptor;
    }
}