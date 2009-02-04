/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sun.facelets.tag.jstl.fn;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import javax.faces.FacesException;

import com.sun.facelets.tag.TagConfig;
import com.sun.facelets.tag.TagHandler;
import com.sun.facelets.tag.TagLibrary;

/**
 * Library for JSTL Functions
 * 
 * @author Jacob Hookom
 * @version $Id: JstlFnLibrary.java,v 1.3 2008/07/13 19:01:51 rlubke Exp $
 */
public class JstlFnLibrary implements TagLibrary
{

    public final static String Namespace = "http://java.sun.com/jsp/jstl/functions";

    private final Map fns = new HashMap();

    public JstlFnLibrary()
    {
        super();
        try
        {
            Method[] methods = JstlFunction.class.getMethods();
            for (int i = 0; i < methods.length; i++)
            {
                if (Modifier.isStatic(methods[i].getModifiers()))
                {
                    fns.put(methods[i].getName(), methods[i]);
                }
            }
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public boolean containsNamespace(String ns)
    {
        return Namespace.equals(ns);
    }

    public boolean containsTagHandler(String ns, String localName)
    {
        return false;
    }

    public TagHandler createTagHandler(String ns, String localName, TagConfig tag) throws FacesException
    {
        return null;
    }

    public boolean containsFunction(String ns, String name)
    {
        if (Namespace.equals(ns))
        {
            return this.fns.containsKey(name);
        }
        return false;
    }

    public Method createFunction(String ns, String name)
    {
        if (Namespace.equals(ns))
        {
            return (Method) this.fns.get(name);
        }
        return null;
    }

    public static void main(String[] argv)
    {
        JstlFnLibrary lib = new JstlFnLibrary();
        System.out.println(lib.containsFunction(JstlFnLibrary.Namespace, "toUpperCase"));
    }

}
