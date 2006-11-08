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
package org.apache.myfaces.util;

import javax.faces.component.UIComponent;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Stack;

/**
 * Iterates over a view structure, i.e. a component tree, in a
 * depth first manner.
 *
 * @author Manfred Geiler (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class ViewIterator
        implements Iterator
{
    //private static final Log log = LogFactory.getLog(ViewIterator.class);
    private UIComponent _next = null;
    private boolean _mayHaveNext = true;
    private UIComponent _current = null;
    private Stack _stack = new Stack();

    /**
     * @param root the root of the view structure to iterate over
     */
    public ViewIterator(UIComponent root)
    {
        _next = root;
        _current = null;
    }

    public boolean hasNext()
    {
        return getNext() != null;
    }

    /**
     * @return the next component in the view. The first element is always the given root
     * component.
     */
    public Object next()
    {
        if (!hasNext())
        {
            throw new NoSuchElementException();
        }
        _current = _next;
        _next = null;
        return _current;
    }

    private UIComponent getNext()
    {
        if (_next == null && _mayHaveNext)
        {
            //has child?
            Iterator children = _current.getFacetsAndChildren();
            if (children.hasNext())
            {
                _next = (UIComponent)children.next();
                //push siblings
                _stack.push(children);
            }
            else
            {
                //has next sibling?
                for (;;)
                {
                    if (_stack.empty())
                    {
                        _next = null;
                        _mayHaveNext = false;
                        break;
                    }

                    Iterator currentSiblings = (Iterator)_stack.peek();
                    if (currentSiblings.hasNext())
                    {
                        _next = (UIComponent)currentSiblings.next();
                        break;
                    }
                    else
                    {
                        _stack.pop();
                    }
                }
            }
        }
        return _next;
    }

    public void remove()
    {
        throw new UnsupportedOperationException(this.getClass().getName() + " UnsupportedOperationException");
    }
}
