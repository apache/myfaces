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
package org.apache.myfaces.view.facelets.tag.jsf;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import javax.faces.FacesException;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.component.UniqueIdVendor;
import javax.faces.context.FacesContext;
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.TagAttribute;
import javax.faces.view.facelets.TagAttributeException;

/**
 * 
 * @author Jacob Hookom
 * @version $Id: ComponentSupport.java,v 1.8 2008/07/13 19:01:46 rlubke Exp $
 */
public final class ComponentSupport
{

    private final static String MARK_DELETED = "org.apache.myfaces.view.facelets.MARK_DELETED";
    public final static String MARK_CREATED = "org.apache.myfaces.view.facelets.MARK_ID";

    /**
     * Used in conjunction with markForDeletion where any UIComponent marked will be removed.
     * 
     * @param component
     *            UIComponent to finalize
     */
    public static void finalizeForDeletion(UIComponent component)
    {
        // remove any existing marks of deletion
        component.getAttributes().remove(MARK_DELETED);

        // finally remove any children marked as deleted
        if (component.getChildCount() > 0)
        {
            for (Iterator<UIComponent> iter = component.getChildren().iterator(); iter.hasNext();)
            {
                UIComponent child = iter.next();
                if (child.getAttributes().containsKey(MARK_DELETED))
                {
                    iter.remove();
                }
            }
        }

        // remove any facets marked as deleted
        Map<String, UIComponent> facets = component.getFacets();
        if (!facets.isEmpty())
        {
            Collection<UIComponent> col = facets.values();
            for (Iterator<UIComponent> itr = col.iterator(); itr.hasNext();)
            {
                UIComponent fc = itr.next();
                if (fc.getAttributes().containsKey(MARK_DELETED))
                {
                    itr.remove();
                }
            }
        }
    }

    /**
     * A lighter-weight version of UIComponent's findChild.
     * 
     * @param parent
     *            parent to start searching from
     * @param id
     *            to match to
     * @return UIComponent found or null
     */
    public static UIComponent findChild(UIComponent parent, String id)
    {
        if (parent.getChildCount() > 0)
        {
            for (UIComponent child : parent.getChildren())
            {
                if (id.equals(child.getId()))
                {
                    return child;
                }
            }
        }

        return null;
    }

    /**
     * By TagId, find Child
     * 
     * @param parent
     * @param id
     * @return
     */
    public static UIComponent findChildByTagId(UIComponent parent, String id)
    {
        Iterator<UIComponent> itr = parent.getFacetsAndChildren();
        while (itr.hasNext())
        {
            UIComponent child = itr.next();
            if (id.equals(child.getAttributes().get(MARK_CREATED)))
            {
                return child;
            }
        }

        return null;
    }

    /**
     * According to JSF 1.2 tag specs, this helper method will use the TagAttribute passed in determining the Locale
     * intended.
     * 
     * @param ctx
     *            FaceletContext to evaluate from
     * @param attr
     *            TagAttribute representing a Locale
     * @return Locale found
     * @throws TagAttributeException
     *             if the Locale cannot be determined
     */
    public static Locale getLocale(FaceletContext ctx, TagAttribute attr) throws TagAttributeException
    {
        Object obj = attr.getObject(ctx);
        if (obj instanceof Locale)
        {
            return (Locale) obj;
        }
        if (obj instanceof String)
        {
            String s = (String) obj;
            if (s.length() == 2)
            {
                return new Locale(s);
            }

            if (s.length() == 5)
            {
                return new Locale(s.substring(0, 2), s.substring(3, 5).toUpperCase());
            }

            if (s.length() >= 7)
            {
                return new Locale(s.substring(0, 2), s.substring(3, 5).toUpperCase(), s.substring(6, s.length()));
            }

            throw new TagAttributeException(attr, "Invalid Locale Specified: " + s);
        }
        else
        {
            throw new TagAttributeException(attr, "Attribute did not evaluate to a String or Locale: " + obj);
        }
    }

    /**
     * Tries to walk up the parent to find the UIViewRoot, if not found, then go to FaceletContext's FacesContext for
     * the view root.
     * 
     * @param ctx
     *            FaceletContext
     * @param parent
     *            UIComponent to search from
     * @return UIViewRoot instance for this evaluation
     */
    public static UIViewRoot getViewRoot(FaceletContext ctx, UIComponent parent)
    {
        UIComponent c = parent;
        do
        {
            if (c instanceof UIViewRoot)
            {
                return (UIViewRoot) c;
            }
            else
            {
                c = c.getParent();
            }
        } while (c != null);

        return ctx.getFacesContext().getViewRoot();
    }
    
    public static UniqueIdVendor getClosestUniqueIdVendor(FacesContext facesContext, UIComponent parent)
    {
        UIComponent c = parent;
        do
        {
            if (c instanceof UniqueIdVendor)
            {
                return (UniqueIdVendor) c;
            }
            else
            {
                c = c.getParent();
            }
        } while (c != null);

        return facesContext.getViewRoot();
    }
   
    /*
    public static UniqueIdVendor getClosestUniqueIdVendor(UIComponent parent)
    {
        UIComponent c = parent;
        do
        {
            if (c instanceof UniqueIdVendor)
            {
                return (UniqueIdVendor) c;
            }
            else
            {
                c = c.getParent();
            }
        } while (c != null);

        return null;
    }*/

    /**
     * Marks all direct children and Facets with an attribute for deletion.
     * 
     * @see #finalizeForDeletion(UIComponent)
     * @param component
     *            UIComponent to mark
     */
    public static void markForDeletion(UIComponent component)
    {
        // flag this component as deleted
        component.getAttributes().put(MARK_DELETED, Boolean.TRUE);

        Iterator<UIComponent> iter = component.getFacetsAndChildren();
        while (iter.hasNext())
        {
            UIComponent child = iter.next();
            if (child.getAttributes().containsKey(MARK_CREATED))
            {
                child.getAttributes().put(MARK_DELETED, Boolean.TRUE);
            }
        }
    }

    public static void encodeRecursive(FacesContext context, UIComponent toRender) throws IOException, FacesException
    {
        if (toRender.isRendered())
        {
            toRender.encodeBegin(context);
            
            if (toRender.getRendersChildren())
            {
                toRender.encodeChildren(context);
            }
            else if (toRender.getChildCount() > 0)
            {
                for (UIComponent child : toRender.getChildren())
                {
                    encodeRecursive(context, child);
                }
            }
            
            toRender.encodeEnd(context);
        }
    }

    public static void removeTransient(UIComponent component)
    {
        if (component.getChildCount() > 0)
        {
            for (Iterator<UIComponent> itr = component.getChildren().iterator(); itr.hasNext();)
            {
                UIComponent child = itr.next();
                if (child.isTransient())
                {
                    itr.remove();
                }
                else
                {
                    removeTransient(child);
                }
            }
        }
        
        Map<String, UIComponent> facets = component.getFacets();
        if (!facets.isEmpty())
        {
            for (Iterator<UIComponent> itr = facets.values().iterator(); itr.hasNext();)
            {
                UIComponent facet = itr.next();
                if (facet.isTransient())
                {
                    itr.remove();
                }
                else
                {
                    removeTransient(facet);
                }
            }
        }
    }

    /**
     * Determine if the passed component is not null and if it's new to the tree. This operation can be used for
     * determining if attributes should be wired to the component.
     * 
     * @deprecated use ComponentHandler.isNew
     * @param component
     *            the component you wish to modify
     * @return true if it's new
     */
    @Deprecated
    public static boolean isNew(UIComponent component)
    {
        return component != null && component.getParent() == null;
    }
}
