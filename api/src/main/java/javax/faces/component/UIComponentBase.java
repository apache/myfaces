/*
 * Copyright 2004 The Apache Software Foundation.
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
package javax.faces.component;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.el.ValueExpression;
import javax.faces.FacesException;
import javax.faces.FactoryFinder;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.FacesEvent;
import javax.faces.event.FacesListener;
import javax.faces.render.RenderKit;
import javax.faces.render.RenderKitFactory;
import javax.faces.render.Renderer;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Standard implementation of the UIComponent base class; all standard JSF
 * components extend this class.
 * <p>
 * <i>Disclaimer</i>: The official definition for the behaviour of
 * this class is the JSF 1.1 specification but for legal reasons the
 * specification cannot be replicated here. Any javadoc here therefore
 * describes the current implementation rather than the spec, though
 * this class has been verified as correctly implementing the spec.
 *
 * see Javadoc of <a href="http://java.sun.com/javaee/javaserverfaces/1.2/docs/api/index.html">JSF Specification</a> for more.
 * 
 * @author Manfred Geiler (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public abstract class UIComponentBase
        extends UIComponent
{
    private static Log log = LogFactory.getLog(UIComponentBase.class);

    private _ComponentAttributesMap _attributesMap = null;
    private List<UIComponent> _childrenList = null;
    private Map<String,UIComponent> _facetMap = null;
    private List<FacesListener> _facesListeners = null;
    private String _clientId = null;
    private String _id = null;
    private UIComponent _parent = null;
    private boolean _transient = false;
    
    public UIComponentBase()
    {
    }
    
    /**
     * Get a map through which all the UIComponent's properties, value-bindings
     * and non-property attributes can be read and written.
     * <p>
     * When writing to the returned map:
     * <ul>
     * <li>If this component has an explicit property for the specified key
     *  then the setter method is called. An IllegalArgumentException is
     *  thrown if the property is read-only. If the property is readable
     *  then the old value is returned, otherwise null is returned.
     * <li>Otherwise the key/value pair is stored in a map associated with
     * the component.
     * </ul>
     * Note that value-bindings are <i>not</i> written by put calls to this map.
     * Writing to the attributes map using a key for which a value-binding 
     * exists will just store the value in the attributes map rather than
     * evaluating the binding, effectively "hiding" the value-binding from
     * later attributes.get calls. Setter methods on components commonly do
     * <i>not</i> evaluate a binding of the same name; they just store the
     * provided value directly on the component.
     * <p>
     * When reading from the returned map:
     * <ul>
     * <li>If this component has an explicit property for the specified key
     *  then the getter method is called. If the property exists, but is
     *  read-only (ie only a setter method is defined) then an
     *  IllegalArgumentException is thrown.
     * <li>If the attribute map associated with the component has an entry
     *  with the specified key, then that is returned.
     * <li>If this component has a value-binding for the specified key, then
     * the value-binding is evaluated to fetch the value.
     * <li>Otherwise, null is returned.
     * </ul>
     * Note that components commonly define getter methods such that they
     * evaluate a value-binding of the same name if there isn't yet a
     * local property.
     * <p>
     * Assigning values to the map which are not explicit properties on
     * the underlying component can be used to "tunnel" attributes from
     * the JSP tag (or view-specific equivalent) to the associated renderer
     * without modifying the component itself.
     * <p>
     * Any value-bindings and non-property attributes stored in this map
     * are automatically serialized along with the component when the view
     * is serialized.
     */
    public Map<String, Object> getAttributes()
    {
        if (_attributesMap == null)
        {
            _attributesMap = new _ComponentAttributesMap(this);
        }
        return _attributesMap;
    }

    /**
     * Get the named value-binding associated with this component.
     * <p>
     * Value-bindings are stored in a map associated with the component,
     * though there is commonly a property (setter/getter methods) 
     * of the same name defined on the component itself which
     * evaluates the value-binding when called.
     *
     * @deprecated Replaced by getValueExpression
     */
    public ValueBinding getValueBinding(String name)
    {
        ValueExpression expression = getValueExpression(name);
        if (expression != null)
        {
            if (expression instanceof _ValueBindingToValueExpression)
            {
                return ((_ValueBindingToValueExpression) expression).getValueBinding();
            }
            return new _ValueExpressionToValueBinding(expression);
        }
        return null;
    }

    /**
     * Put the provided value-binding into a map of value-bindings
     * associated with this component.
     *
     * @deprecated Replaced by setValueExpression
     */
    public void setValueBinding(String name,
                                ValueBinding binding)
    {
        setValueExpression(name, binding == null ? null : new _ValueBindingToValueExpression(binding));
    }

    /**
     * Get a string which can be output to the response which uniquely
     * identifies this UIComponent within the current view.
     * <p>
     * The component should have an id attribute already assigned to it;
     * however if the id property is currently null then a unique id
     * is generated and set for this component. This only happens when
     * components are programmatically created without ids, as components
     * created by a ViewHandler should be assigned ids when they are created.
     * <p>
     * If this component is a descendant of a NamingContainer then the
     * client id is of form "{namingContainerId}:{componentId}". Note that
     * the naming container's id may itself be of compound form if it has
     * an ancestor naming container. Note also that this only applies to
     * naming containers; other UIComponent types in the component's
     * ancestry do not affect the clientId.
     * <p>
     * Finally the renderer associated with this component is asked to
     * convert the id into a suitable form. This allows escaping of any
     * characters in the clientId which are significant for the markup
     * language generated by that renderer.
     */
    public String getClientId(FacesContext context)
    {
        if (context == null) throw new NullPointerException("context");

        if (_clientId != null) return _clientId;

        boolean idWasNull = false;
        String id = getId();
        if (id == null)
        {
            //Although this is an error prone side effect, we automatically create a new id
            //just to be compatible to the RI
            UIViewRoot viewRoot = context.getViewRoot();
            if (viewRoot != null)
            {
                id = viewRoot.createUniqueId();
            }
            else
            {
                // The RI throws a NPE
                throw new FacesException(
                        "Cannot create clientId. No id is assigned for component to create an id and UIViewRoot is not defined: "
                                + getPathToComponent(this));
            }
            setId(id);
            //We remember that the id was null and log a warning down below
            idWasNull = true;
        }

        UIComponent namingContainer = _ComponentUtils.findParentNamingContainer(this, false);
        if (namingContainer != null)
        {
            _clientId = namingContainer.getContainerClientId(context) + NamingContainer.SEPARATOR_CHAR + id;
        }
        else
        {
            _clientId = id;
        }

        Renderer renderer = getRenderer(context);
        if (renderer != null)
        {
            _clientId = renderer.convertClientId(context, _clientId);
        }

        if (idWasNull && log.isWarnEnabled())
        {            
            log.warn("WARNING: Component " + _clientId
                    + " just got an automatic id, because there was no id assigned yet. "
                    + "If this component was created dynamically (i.e. not by a JSP tag) you should assign it an "
                    + "explicit static id or assign it the id you get from "
                    + "the createUniqueId from the current UIViewRoot "
                    + "component right after creation! Path to Component: " + getPathToComponent(this));
        }

        return _clientId;
    }

    /**
     * Get a string which uniquely identifies this UIComponent within the scope of the nearest ancestor NamingContainer
     * component. The id is not necessarily unique across all components in the current view.
     */
    public String getId()
    {
        return _id;
    }
    
    
    /**
     * <code>invokeOnComponent</code> must be implemented in <code>UIComponentBase</code> too...
     */
    public boolean invokeOnComponent(FacesContext context, String clientId, ContextCallback callback) throws FacesException{
    	return super.invokeOnComponent(context, clientId, callback);
    }

    /**
     * Set an identifier for this component which is unique within the
     * scope of the nearest ancestor NamingContainer component. The id is
     * not necessarily unique across all components in the current view.
     * <p>
     * The id must start with an underscore if it is generated by the JSF
     * framework, and must <i>not</i> start with an underscore if it has
     * been specified by the user (eg in a JSP tag).
     * <p>
     * The first character of the id must be an underscore or letter.
     * Following characters may be letters, digits, underscores or dashes.
     * <p>
     * Null is allowed as a parameter, and will reset the id to null.
     * <p>
     * The clientId of this component is reset by this method; see
     * getClientId for more info.
     *  
     * @throws IllegalArgumentException if the id is not valid.
     */
    public void setId(String id)
    {
        isIdValid(id);
        _id = id;
        _clientId = null;
    }

    public UIComponent getParent()
    {
        return _parent;
    }

    public void setParent(UIComponent parent)
    {
        _parent = parent;
    }

    /**
     * Indicates whether this component or its renderer manages the
     * invocation of the rendering methods of its child components.
     * When this is true:
     * <ul>
     * <li>This component's encodeBegin method will only be called
     * after all the child components have been created and added
     * to this component.
     * <li>This component's encodeChildren method will be called
     * after its encodeBegin method. Components for which this
     * method returns false do not get this method invoked at all.
     * <li>No rendering methods will be called automatically on
     * child components; this component is required to invoke the
     * encodeBegin/encodeEnd/etc on them itself.
     * </ul>
     */
    public boolean getRendersChildren()
    {
        Renderer renderer = getRenderer(getFacesContext());
        return renderer != null ? renderer.getRendersChildren() : false;
    }

    /**
     * Return a list of the UIComponent objects which are direct children
     * of this component.
     * <p>
     * The list object returned has some non-standard behaviour:
     * <ul>
     * <li>The list is type-checked; only UIComponent objects can be added.
     * <li>If a component is added to the list with an id which is the same
     * as some other component in the list then an exception is thrown. However
     * multiple components with a null id may be added.
     * <li>The component's parent property is set to this component. If the
     * component already had a parent, then the component is first removed
     * from its original parent's child list.
     * </ul>
     */
    public List<UIComponent> getChildren()
    {
        if (_childrenList == null)
        {
            _childrenList = new _ComponentChildrenList(this);
        }
        return _childrenList;
    }

    /**
     * Return the number of direct child components this component has.
     * <p>
     * Identical to getChildren().size() except that when this component
     * has no children this method will not force an empty list to be
     * created.
     */
    public int getChildCount()
    {
        return _childrenList == null ? 0 : _childrenList.size();
    }

    /**
     * Standard method for finding other components by id, inherited by
     * most UIComponent objects.
     * <p>
     * The lookup is performed in a manner similar to finding a file
     * in a filesystem; there is a "base" at which to start, and the
     * id can be for something in the "local directory", or can include
     * a relative path. Here, NamingContainer components fill the role
     * of directories, and ":" is the "path separator". Note, however,
     * that although components have a strict parent/child hierarchy,
     * component ids are only prefixed ("namespaced") with the id of
     * their parent when the parent is a NamingContainer.
     * <p>
     * The base node at which the search starts is determined as
     * follows:
     * <ul>
     * <li>When expr starts with ':', the search starts with the root
     * component of the tree that this component is in (ie the ancestor
     * whose parent is null).
     * <li>Otherwise, if this component is a NamingContainer then the search
     * starts with this component.
     * <li>Otherwise, the search starts from the nearest ancestor 
     * NamingContainer (or the root component if there is no NamingContainer
     * ancestor).
     * </ul>
     * 
     * @param expr is of form "id1:id2:id3".
     * @return UIComponent or null if no component with the specified id is
     * found.
     */

    public UIComponent findComponent(String expr)
    {
        if (expr == null) 
            throw new NullPointerException("expr");
        if (expr.length() == 0) 
            return null;

        UIComponent findBase;
        if (expr.charAt(0) == NamingContainer.SEPARATOR_CHAR)
        {
            findBase = _ComponentUtils.getRootComponent(this);
            expr = expr.substring(1);
        }
        else
        {
            if (this instanceof NamingContainer)
            {
                findBase = this;
            }
            else
            {
                findBase = _ComponentUtils.findParentNamingContainer(this, true /* root if not found */);
            }
        }

        int separator = expr.indexOf(NamingContainer.SEPARATOR_CHAR);
        if (separator == -1)
        {
            return _ComponentUtils.findComponent(findBase, expr);
        }

        String id = expr.substring(0, separator);
        findBase = _ComponentUtils.findComponent(findBase, id);
        if (findBase == null)
        {
        	return null;
        }
        
        if (!(findBase instanceof NamingContainer))
                    throw new IllegalArgumentException("Intermediate identifier " + id + " in search expression " +
                        expr + " identifies a UIComponent that is not a NamingContainer");
        
        return findBase.findComponent(expr.substring(separator + 1));
        
    }


    public Map<String, UIComponent> getFacets()
    {
        if (_facetMap == null)
        {
            _facetMap = new _ComponentFacetMap(this);
        }
        return _facetMap;
    }

    public UIComponent getFacet(String name)
    {
        return _facetMap == null ? null : _facetMap.get(name);
    }

    public Iterator<UIComponent> getFacetsAndChildren()
    {
        return new _FacetsAndChildrenIterator<UIComponent>(_facetMap, _childrenList);
    }

    /**
     * Invoke any listeners attached to this object which are listening
     * for an event whose type matches the specified event's runtime
     * type.
     * <p>
     * This method does not propagate the event up to parent components,
     * ie listeners attached to parent components don't automatically
     * get called.
     * <p>
     * If any of the listeners throws AbortProcessingException then
     * that exception will prevent any further listener callbacks
     * from occurring, and the exception propagates out of this
     * method without alteration.
     * <p>
     * ActionEvent events are typically queued by the renderer associated
     * with this component in its decode method; ValueChangeEvent events by
     * the component's validate method. In either case the event's source
     * property references a component. At some later time the UIViewRoot
     * component iterates over its queued events and invokes the broadcast
     * method on each event's source object.
     * 
     * @param event must not be null.
     */
    public void broadcast(FacesEvent event)
            throws AbortProcessingException
    {
        if (event == null) throw new NullPointerException("event");
        if (_facesListeners == null) return;
        for (Iterator<FacesListener> it = _facesListeners.iterator(); it.hasNext(); )
        {
            FacesListener facesListener = it.next();
            if (event.isAppropriateListener(facesListener))
            {
                event.processListener(facesListener);
            }
        }
    }

    /**
     * Check the submitted form parameters for data associated with this
     * component. This default implementation delegates to this component's
     * renderer if there is one, and otherwise ignores the call.
     */
    public void decode(FacesContext context)
    {
        if (context == null) throw new NullPointerException("context");
        Renderer renderer = getRenderer(context);
        if (renderer != null)
        {
            renderer.decode(context, this);
        }
    }

    public void encodeBegin(FacesContext context)
            throws IOException
    {
        if (context == null) throw new NullPointerException("context");
        if (!isRendered()) return;
        Renderer renderer = getRenderer(context);
        if (renderer != null)
        {
            renderer.encodeBegin(context, this);
        }
    }

    public void encodeChildren(FacesContext context)
            throws IOException
    {
        if (context == null) throw new NullPointerException("context");
        if (!isRendered()) return;
        Renderer renderer = getRenderer(context);
        if (renderer != null)
        {
            renderer.encodeChildren(context, this);
        }
    }

    public void encodeEnd(FacesContext context)
            throws IOException
    {
        if (context == null) throw new NullPointerException("context");
        if (!isRendered()) return;
        Renderer renderer = getRenderer(context);
        if (renderer != null)
        {
            renderer.encodeEnd(context, this);
        }
    }

    protected void addFacesListener(FacesListener listener)
    {
        if (listener == null) throw new NullPointerException("listener");
        if (_facesListeners == null)
        {
            _facesListeners = new ArrayList<FacesListener>();
        }
        _facesListeners.add(listener);
    }

    protected FacesListener[] getFacesListeners(Class clazz)
    {
        if (_facesListeners == null)
        {
            return (FacesListener[])Array.newInstance(clazz, 0);
        }
        List<FacesListener> lst = null;
        for (Iterator<FacesListener> it = _facesListeners.iterator(); it.hasNext(); )
        {
            FacesListener facesListener = it.next();
            if (clazz.isAssignableFrom(facesListener.getClass()))
            {
                if (lst == null) lst = new ArrayList<FacesListener>();
                lst.add(facesListener);
            }
        }
        if (lst == null)
        {
            return (FacesListener[])Array.newInstance(clazz, 0);
        }
        
        return lst.toArray((FacesListener[])Array.newInstance(clazz, lst.size()));
    }

    protected void removeFacesListener(FacesListener listener)
    {
        if (_facesListeners != null)
        {
            _facesListeners.remove(listener);
        }
    }

    public void queueEvent(FacesEvent event)
    {
        if (event == null) throw new NullPointerException("event");
        UIComponent parent = getParent();
        if (parent == null)
        {
            throw new IllegalStateException("component is not a descendant of a UIViewRoot");
        }
        parent.queueEvent(event);
    }

    public void processDecodes(FacesContext context)
    {
        if (context == null) throw new NullPointerException("context");
                if (!isRendered()) return;
        for (Iterator it = getFacetsAndChildren(); it.hasNext(); )
        {
            UIComponent childOrFacet = (UIComponent)it.next();
            childOrFacet.processDecodes(context);
        }
        try
        {
            decode(context);
        }
        catch (RuntimeException e)
        {
            context.renderResponse();
            throw e;
        }
    }


    public void processValidators(FacesContext context)
    {
        if (context == null) throw new NullPointerException("context");
        if (!isRendered()) return;

        for (Iterator it = getFacetsAndChildren(); it.hasNext(); )
        {
            UIComponent childOrFacet = (UIComponent)it.next();
            childOrFacet.processValidators(context);
        }
    }

    /**
     * This isn't an input component, so just pass on the processUpdates
     * call to child components and facets that might be input components.
     * <p>
     * Components that were never rendered can't possibly be receiving
     * update data (no corresponding fields were ever put into the response)
     * so if this component is not rendered then this method does not
     * invoke processUpdates on its children.
     */
    public void processUpdates(FacesContext context)
    {
        if (context == null) throw new NullPointerException("context");
        if (!isRendered()) return;

        for (Iterator it = getFacetsAndChildren(); it.hasNext(); )
        {
            UIComponent childOrFacet = (UIComponent)it.next();
            childOrFacet.processUpdates(context);
        }
    }

    public Object processSaveState(FacesContext context)
    {
        if (context == null) throw new NullPointerException("context");
        if (isTransient()) return null;
        Map facetMap = null;
        for (Iterator it = getFacets().entrySet().iterator(); it.hasNext(); )
        {
            Map.Entry entry = (Map.Entry)it.next();
            if (facetMap == null) facetMap = new HashMap();
            UIComponent component = (UIComponent)entry.getValue();
            if (!component.isTransient())
            {
                facetMap.put(entry.getKey(), component.processSaveState(context));
            }
        }
        List<Object> childrenList = null;
        if (getChildCount() > 0)
        {
            for (Iterator it = getChildren().iterator(); it.hasNext(); )
            {
              UIComponent child = (UIComponent)it.next();
              if (childrenList == null) {
                childrenList = new ArrayList<Object>(getChildCount());
              }
              if(!child.isTransient()) 
              {
                  Object childState = child.processSaveState(context);
                  if (childState != null) {
                    childrenList.add(childState);
                  }
              }
            }
        }
        return new Object[] {saveState(context),
                             facetMap,
                             childrenList};
    }

    public void processRestoreState(FacesContext context,
                                    Object state)
    {
        if (context == null) throw new NullPointerException("context");
        Object myState = ((Object[])state)[0];
        Map facetMap = (Map)((Object[])state)[1];
        List childrenList = (List)((Object[])state)[2];
        if(facetMap != null)
        {
          for (Iterator it = getFacets().entrySet().iterator(); it.hasNext(); )
          {
              Map.Entry entry = (Map.Entry)it.next();
              Object facetState = facetMap.get(entry.getKey());
              if (facetState != null)
              {
                UIComponent component = (UIComponent)entry.getValue();
                component.processRestoreState(context, facetState);
              }
              else
              {
                  context.getExternalContext().log("No state found to restore facet " + entry.getKey());
              }
          }
        }
        if (childrenList != null && getChildCount() > 0)
        {
            int idx = 0;
            for (Iterator it = getChildren().iterator(); it.hasNext(); )
            {
                UIComponent child = (UIComponent)it.next();
                if(!child.isTransient())
                {
                  Object childState = childrenList.get(idx++);
                  if (childState != null)
                  {
                      child.processRestoreState(context, childState);
                  }
                  else
                  {
                      context.getExternalContext().log("No state found to restore child of component " + getId());
                  }
                }
            }
        }
        restoreState(context, myState);
    }

    protected FacesContext getFacesContext()
    {
        return FacesContext.getCurrentInstance();
    }

    protected Renderer getRenderer(FacesContext context)
    {
        if (context == null) throw new NullPointerException("context");
        String rendererType = getRendererType();
        if (rendererType == null) return null;
        String renderKitId = context.getViewRoot().getRenderKitId();
        RenderKitFactory rkf = (RenderKitFactory)FactoryFinder.getFactory(FactoryFinder.RENDER_KIT_FACTORY);
        RenderKit renderKit = rkf.getRenderKit(context, renderKitId);
        Renderer renderer = renderKit.getRenderer(getFamily(), rendererType);
        if (renderer == null)
        {
            getFacesContext().getExternalContext().log("No Renderer found for component " + getPathToComponent(this) + " (component-family=" + getFamily() + ", renderer-type=" + rendererType + ")");
            log.warn("No Renderer found for component " + getPathToComponent(this) + " (component-family=" + getFamily() + ", renderer-type=" + rendererType + ")");
        }
        return renderer;
    }

    private String getPathToComponent(UIComponent component)
    {
        StringBuffer buf = new StringBuffer();

        if(component == null)
        {
            buf.append("{Component-Path : ");
            buf.append("[null]}");
            return buf.toString();
        }

        getPathToComponent(component,buf);

        buf.insert(0,"{Component-Path : ");
        buf.append("}");

        return buf.toString();
    }

    private void getPathToComponent(UIComponent component, StringBuffer buf)
    {
        if(component == null)
            return;

        StringBuffer intBuf = new StringBuffer();

        intBuf.append("[Class: ");
        intBuf.append(component.getClass().getName());
        if(component instanceof UIViewRoot)
        {
            intBuf.append(",ViewId: ");
            intBuf.append(((UIViewRoot) component).getViewId());
        }
        else
        {
            intBuf.append(",Id: ");
            intBuf.append(component.getId());
        }
        intBuf.append("]");

        buf.insert(0,intBuf.toString());

        getPathToComponent(component.getParent(), buf);
    }

    public boolean isTransient()
    {
        return _transient;
    }

    public void setTransient(boolean transientFlag)
    {
        _transient = transientFlag;
    }

    /**
     * Serializes objects which are "attached" to this component but which are
     * not UIComponent children of it. Examples are validator and listener
     * objects. To be precise, it returns an object which implements
     * java.io.Serializable, and which when serialized will persist the
     * state of the provided object.     
     * <p>
     * If the attachedObject is a List then every object in the list is saved
     * via a call to this method, and the returned wrapper object contains
     * a List object.
     * <p>
     * If the object implements StateHolder then the object's saveState is
     * called immediately, and a wrapper is returned which contains both
     * this saved state and the original class name. However in the case
     * where the StateHolder.isTransient method returns true, null is
     * returned instead.
     * <p>
     * If the object implements java.io.Serializable then the object is simply
     * returned immediately; standard java serialization will later be used
     * to store this object.
     * <p>
     * In all other cases, a wrapper is returned which simply stores the type
     * of the provided object. When deserialized, a default instance of that
     * type will be recreated.
     */
    public static Object saveAttachedState(FacesContext context,
                                           Object attachedObject)
    {
        if (attachedObject == null) return null;
        if (attachedObject instanceof List)
        {
            List<Object> lst = new ArrayList<Object>(((List)attachedObject).size());
            for (Iterator it = ((List)attachedObject).iterator(); it.hasNext(); )
            {
                lst.add(saveAttachedState(context, it.next()));
            }
            return new _AttachedListStateWrapper(lst);
        }
        else if (attachedObject instanceof StateHolder)
        {
            if (((StateHolder)attachedObject).isTransient())
            {
                return null;
            }
            
            return new _AttachedStateWrapper(attachedObject.getClass(),
                                                 ((StateHolder)attachedObject).saveState(context));
        }
        else if (attachedObject instanceof Serializable)
        {
            return attachedObject;
        }
        else
        {
            return new _AttachedStateWrapper(attachedObject.getClass(), null);
        }
    }

    public static Object restoreAttachedState(FacesContext context,
                                              Object stateObj)
            throws IllegalStateException
    {
        if (context == null) throw new NullPointerException("context");
        if (stateObj == null) return null;
        if (stateObj instanceof _AttachedListStateWrapper)
        {
            List<Object> lst = ((_AttachedListStateWrapper)stateObj).getWrappedStateList();
            List<Object> restoredList = new ArrayList<Object>(lst.size());
            for (Iterator<Object> it = lst.iterator(); it.hasNext(); )
            {
                restoredList.add(restoreAttachedState(context, it.next()));
            }
            return restoredList;
        }
        else if (stateObj instanceof _AttachedStateWrapper)
        {
            Class clazz = ((_AttachedStateWrapper)stateObj).getClazz();
            Object restoredObject;
            try
            {
                restoredObject = clazz.newInstance();
            }
            catch (InstantiationException e)
            {
                throw new RuntimeException("Could not restore StateHolder of type " + clazz.getName() + " (missing no-args constructor?)", e);
            }
            catch (IllegalAccessException e)
            {
                throw new RuntimeException(e);
            }
            if (restoredObject instanceof StateHolder)
            {
                Object wrappedState = ((_AttachedStateWrapper)stateObj).getWrappedStateObject();
                ((StateHolder)restoredObject).restoreState(context, wrappedState);
            }
            return restoredObject;
        }
        else
        {
            return stateObj;
        }
    }


    /**
     * Invoked after the render phase has completed, this method
     * returns an object which can be passed to the restoreState
     * of some other instance of UIComponentBase to reset that
     * object's state to the same values as this object currently
     * has.
     */
    public Object saveState(FacesContext context)
    {
        Object values[] = new Object[7];
        values[0] = _id;
        values[1] = _rendered;
        values[2] = _rendererType;
        values[3] = _clientId;
        values[4] = saveAttributesMap();
        values[5] = saveAttachedState(context, _facesListeners);
        values[6] = saveBindings(context);
        return values;
    }

    /**
     * Invoked in the "restore view" phase, this initialises this
     * object's members from the values saved previously into the
     * provided state object.
     * <p>
     * @param state is an object previously returned by
     * the saveState method of this class.
     */
    public void restoreState(FacesContext context, Object state)
    {
        Object values[] = (Object[])state;
        _id = (String)values[0];
        _rendered = (Boolean)values[1];
        _rendererType = (String)values[2];
        _clientId = (String)values[3];
        restoreAttributesMap(values[4]);
        _facesListeners = (List<FacesListener>)restoreAttachedState(context, values[5]);
        restoreValueExpressionMap(context, values[6]);
    }


    private Object saveAttributesMap()
    {
    	return _attributesMap != null ? _attributesMap.getUnderlyingMap() : null;
    }

    private void restoreAttributesMap(Object stateObj)
    {
        if (stateObj != null)
        {
            _attributesMap = new _ComponentAttributesMap(this, (Map)stateObj);
        }
        else
        {
            _attributesMap = null;
        }
    }

    private Object saveBindings(FacesContext context)
    {
        if (bindings != null)
        {
            HashMap<String, Object> stateMap = new HashMap<String, Object>(bindings.size(), 1);
            for (Iterator<Entry<String, ValueExpression>> it = bindings.entrySet().iterator(); it.hasNext(); )
            {
                Entry<String, ValueExpression> entry = it.next();
                stateMap.put(entry.getKey(),
                             saveAttachedState(context, entry.getValue()));
            }
            return stateMap;
        }
        
        return null;
    }

    private void restoreValueExpressionMap(FacesContext context, Object stateObj)
    {
        if (stateObj != null)
        {
            Map stateMap = (Map)stateObj;
            int initCapacity = (stateMap.size() * 4 + 3) / 3;
            bindings = new HashMap<String, ValueExpression>(initCapacity);
            for (Iterator it = stateMap.entrySet().iterator(); it.hasNext(); )
            {
                Map.Entry entry = (Map.Entry)it.next();
                bindings.put((String)entry.getKey(),
                                     (ValueExpression)restoreAttachedState(context, entry.getValue()));
            }
        }
        else
        {
            bindings = null;
        }
    }


    /**
     * @param string the component id, that should be a vaild one.
     */
    private void isIdValid(String string)
    {

        //is there any component identifier ?
        if(string == null)
            return;

        //Component identifiers must obey the following syntax restrictions:
        //1. Must not be a zero-length String.
        if(string.length()==0)
        {
            throw new IllegalArgumentException("component identifier must not be a zero-length String");
        }

        //let's look at all chars inside of the ID if it is a valid ID!
        char[] chars = string.toCharArray();

        //2. First character must be a letter or an underscore ('_').
        if(!Character.isLetter(chars[0]) &&  chars[0] !='_')
        {
            throw new IllegalArgumentException("component identifier's first character must be a letter or an underscore ('_')! But it is \""+chars[0]+"\"");
        }
        for (int i = 1; i < chars.length; i++)
        {
            //3. Subsequent characters must be a letter, a digit, an underscore ('_'), or a dash ('-').
            if(!Character.isDigit(chars[i]) && !Character.isLetter(chars[i]) && chars[i] !='-' && chars[i] !='_')
            {
                throw new IllegalArgumentException("Subsequent characters of component identifier must be a letter, a digit, an underscore ('_'), or a dash ('-')! But component identifier contains \""+chars[i]+"\"");
            }
        }
    }
    
    <T> T getExpressionValue(String attribute, T explizitValue, T defaultValueIfExpressionNull)
    {
        return _ComponentUtils.getExpressionValue(this, attribute, explizitValue, defaultValueIfExpressionNull);
    }


    //------------------ GENERATED CODE BEGIN (do not modify!) --------------------

    private static final boolean DEFAULT_RENDERED = true;

    private Boolean _rendered = null;
    private String _rendererType = null;



    public void setRendered(boolean rendered)
    {
        _rendered = Boolean.valueOf(rendered);
    }

    public boolean isRendered()
    {
        return getExpressionValue("rendered", _rendered, DEFAULT_RENDERED);
    }

    public void setRendererType(String rendererType)
    {
        _rendererType = rendererType;
    }

    public String getRendererType()
    {
        return getExpressionValue("rendererType", _rendererType, null);
    }


    //------------------ GENERATED CODE END ---------------------------------------
    
    /**
     * @since 1.2
     */
    
    public int getFacetCount()
    {
        // not sure why the RI has this method in both 
        // UIComponent and UIComponentBase
        Map<String, UIComponent> facets = getFacets();
        return facets == null ? 0 : facets.size();
    }
}
