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
package org.apache.myfaces.view.facelets.el;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.el.FunctionMapper;
import org.apache.myfaces.util.Assert;
import org.apache.myfaces.util.ClassUtils;


/**
 * Default implementation of the FunctionMapper
 * 
 * @see java.lang.reflect.Method
 * @see javax.el.FunctionMapper
 * 
 * @author Jacob Hookom
 * @version $Id$
 */
public final class DefaultFunctionMapper extends FunctionMapper implements Externalizable
{
    private static final long serialVersionUID = 1L;

    private Map<String, Function> _functions = null;

    /*
     * (non-Javadoc)
     * 
     * @see javax.el.FunctionMapper#resolveFunction(java.lang.String, java.lang.String)
     */
    @Override
    public Method resolveFunction(String prefix, String localName)
    {
        if (_functions != null)
        {
            Function f = (Function) _functions.get(prefix + ':' + localName);
            return f.getMethod();
        }
        
        return null;
    }

    public void addFunction(String prefix, String localName, Method m)
    {
        if (_functions == null)
        {
            _functions = new HashMap<>();
        }
        
        Function f = new Function(prefix, localName, m);
        synchronized (this)
        {
            _functions.put(prefix + ':' + localName, f);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.io.Externalizable#writeExternal(java.io.ObjectOutput)
     */
    @Override
    public void writeExternal(ObjectOutput out) throws IOException
    {
        out.writeObject(_functions);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.io.Externalizable#readExternal(java.io.ObjectInput)
     */
    @SuppressWarnings("unchecked")
    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
    {
        _functions = (Map<String, Function>) in.readObject();
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(128);
        sb.append("FunctionMapper[\n");
        for (Function function : _functions.values())
        {
            sb.append(function).append('\n');
        }
        sb.append(']');
        
        return sb.toString();
    }

    private static class Function implements Externalizable
    {
        private static final long serialVersionUID = 1L;

        protected transient Method _m;
        protected String _owner;
        protected String _name;
        protected String[] _types;
        protected String _prefix;
        protected String _localName;

        public Function(String prefix, String localName, Method method)
        {
            Assert.notNull(localName, "localName");
            Assert.notNull(method, "method");
            
            _prefix = prefix;
            _localName = localName;
            _m = method;
        }

        public Function()
        {
            // for serialization
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.io.Externalizable#writeExternal(java.io.ObjectOutput)
         */
        @Override
        public void writeExternal(ObjectOutput out) throws IOException
        {
            out.writeUTF(_prefix != null ? _prefix : "");
            out.writeUTF(_localName);
            out.writeUTF(_m.getDeclaringClass().getName());
            out.writeUTF(_m.getName());
            out.writeObject(ClassUtils.toTypeNameArray(this._m.getParameterTypes()));
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.io.Externalizable#readExternal(java.io.ObjectInput)
         */
        @Override
        public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
        {
            _prefix = in.readUTF();
            if ("".equals(_prefix))
            {
                _prefix = null;
            }
            
            _localName = in.readUTF();
            _owner = in.readUTF();
            _name = in.readUTF();
            _types = (String[]) in.readObject();
        }

        public Method getMethod()
        {
            if (_m == null)
            {
                try
                {
                    Class<?> t = ClassUtils.forName(_owner);
                    Class<?>[] p = ClassUtils.toTypeArray(_types);
                    _m = t.getMethod(_name, p);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
            
            return _m;
        }

        public boolean matches(String prefix, String localName)
        {
            if (_prefix != null)
            {
                if (prefix == null)
                {
                    return false;
                }
                if (!_prefix.equals(prefix))
                {
                    return false;
                }
            }
            
            return _localName.equals(localName);
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj)
        {
            if (obj instanceof Function)
            {
                return hashCode() == obj.hashCode();
            }
            
            return false;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode()
        {
            return (_prefix + _localName).hashCode();
        }

        @Override
        public String toString()
        {
            StringBuilder sb = new StringBuilder(32);
            sb.append("Function[");
            if (_prefix != null)
            {
                sb.append(_prefix).append(':');
            }
            sb.append(_name).append("] ");
            sb.append(_m);
            
            return sb.toString();
        }
    }
}
