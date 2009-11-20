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

import java.util.Map;

import javax.el.ValueExpression;
import javax.faces.application.Application;
import javax.faces.application.ResourceDependencies;
import javax.faces.application.ResourceDependency;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFComponent;
import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFProperty;

/**
 * Displays a value to the user.
 */
@JSFComponent(defaultRendererType = "javax.faces.Text")
public class UIOutput extends UIComponentBase implements ValueHolder
{
    public static final String COMPONENT_TYPE = "javax.faces.Output";
    public static final String COMPONENT_FAMILY = "javax.faces.Output";

    private Converter _converter;

    /**
     * Construct an instance of the UIOutput.
     */
    public UIOutput()
    {
        setRendererType("javax.faces.Text");
    }

    @Override
    public String getFamily()
    {
        return COMPONENT_FAMILY;
    }

    public Object getLocalValue()
    {
        return  getStateHelper().get(PropertyKeys.value);
    }

    /**
     * Gets The initial value of this component.
     * 
     * @return the new value value
     */
    @JSFProperty
    public Object getValue()
    {
        return  getStateHelper().eval(PropertyKeys.value);
    }

    /**
     * The initial value of this component.
     */
    public void setValue(Object value)
    {
        getStateHelper().put(PropertyKeys.value, value );
    }

    /**
     * An expression that specifies the Converter for this component.
     * <p>
     * The value can either be a static value (ID) or an EL expression. When a static id is
     * specified, an instance of the converter type registered with that id is used. When this
     * is an EL expression, the result of evaluating the expression must be an object that
     * implements the Converter interface.
     * </p>
     */
    @JSFProperty(stateHolder=true)
    public Converter getConverter()
    {
        if (_converter != null)
        {
            return _converter;
        }
        ValueExpression expression = getValueExpression("converter");
        if (expression != null)
        {
            return (Converter) expression.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    public void setConverter(Converter converter)
    {
        this._converter = converter;
        if (initialStateMarked())
        {
            getStateHelper().put(PropertyKeys.converterSet,Boolean.TRUE);
        }
        // The argument converter must be inspected for the presence of the ResourceDependency annotation.
        //_handleAnnotations(FacesContext.getCurrentInstance(), converter);
    }
    
    private boolean _isSetConverter()
    {
        Boolean value = (Boolean) getStateHelper().get(PropertyKeys.converterSet);
        return value == null ? false : value;
    }
    
    public void markInitialState()
    {
        super.markInitialState();
        if (_converter != null && 
            _converter instanceof PartialStateHolder)
        {
            ((PartialStateHolder)_converter).markInitialState();
        }
    }
    
    public void clearInitialState()
    {
        if (initialStateMarked())
        {
            super.clearInitialState();
            if (_converter != null && 
                _converter instanceof PartialStateHolder)
            {
                ((PartialStateHolder)_converter).clearInitialState();
            }
        }
    }
    
    enum PropertyKeys
    {
         value
        , converterSet
    }

    @Override
    public Object saveState(FacesContext facesContext)
    {
        if (initialStateMarked())
        {
            Object parentSaved = super.saveState(facesContext);
            Object converterSaved = null;
            boolean nullDelta = true;
            if (!_isSetConverter() &&
                _converter != null && 
                _converter instanceof PartialStateHolder)
            {
                //Delta
                StateHolder holder = (StateHolder) _converter;
                if (!holder.isTransient())
                {
                    Object attachedState = holder.saveState(facesContext);
                    if (attachedState != null)
                    {
                        nullDelta = false;
                    }
                    converterSaved = new _AttachedDeltaWrapper(_converter.getClass(),
                        attachedState);
                }
                else
                {
                    converterSaved = null;
                }
            }
            else if (_isSetConverter() || _converter != null)
            {
                //Full
                converterSaved = saveAttachedState(facesContext,_converter);
                nullDelta = false;
            }
            
            if (parentSaved == null && nullDelta)
            {
                //No values
                return null;
            }   
            return new Object[]{parentSaved, converterSaved};
        }
        else
        {
            Object[] values = new Object[2];
            values[0] = super.saveState(facesContext);
            values[1] = saveAttachedState(facesContext,_converter);
            return values;
        } 
    }

    @Override
    public void restoreState(FacesContext facesContext, Object state)
    {
        if (state == null)
        {
            return;
        }
        
        Object[] values = (Object[])state;
        super.restoreState(facesContext,values[0]);
        if (values[1] instanceof _AttachedDeltaWrapper)
        {
            //Delta
            ((StateHolder)_converter).restoreState(facesContext, ((_AttachedDeltaWrapper) values[1]).getWrappedStateObject());
        }
        else
        {
            //Full
            _converter = (javax.faces.convert.Converter) restoreAttachedState(facesContext,values[1]);
        }         
    }
    
    /*
    void _handleAnnotations(FacesContext context, Object inspected)
    {
        if (inspected == null) {
            return;
        }
        
        ResourceDependency annotation = inspected.getClass().getAnnotation(ResourceDependency.class);
        
        if (annotation == null)
        {
            // If the ResourceDependency annotation is not present, the argument must be inspected for the presence 
            // of the ResourceDependencies annotation. 
            ResourceDependencies dependencies = inspected.getClass().getAnnotation(ResourceDependencies.class);
            if (dependencies != null)
            {
                // If the ResourceDependencies annotation is present, the action described in ResourceDependencies 
                // must be taken.
                for (ResourceDependency dependency : dependencies.value())
                {
                    _handleResourceDependency(context, dependency);
                }
            }
        }
        else
        {
            // If the ResourceDependency annotation is present, the action described in ResourceDependency must be 
            // taken. 
            _handleResourceDependency(context, annotation);
        }
    }
    
    private void _handleResourceDependency(FacesContext context, ResourceDependency annotation)
    {
        // If this annotation is not present on the class in question, no action must be taken. 
        if (annotation != null)
        {
            Application application = context.getApplication();
            
            // Create a UIOutput instance by passing javax.faces.Output. to 
            // Application.createComponent(java.lang.String).
            UIOutput output = (UIOutput) application.createComponent(COMPONENT_TYPE);
            
            // Get the annotation instance from the class and obtain the values of the name, library, and 
            // target attributes.
            String name = annotation.name();
            
            // Obtain the renderer-type for the resource name by passing name to 
            // ResourceHandler.getRendererTypeForResourceName(java.lang.String).
            String rendererType = application.getResourceHandler().getRendererTypeForResourceName(name);
            
            // Call setRendererType on the UIOutput instance, passing the renderer-type.
            output.setRendererType(rendererType);
            
            // Obtain the Map of attributes from the UIOutput component by calling UIComponent.getAttributes().
            Map<String, Object> attributes = output.getAttributes();
            
            // Store the name into the attributes Map under the key "name".
            attributes.put("name", name);
            
            // If library is the empty string, let library be null.
            String library = annotation.library();
            if (library != null && library.length() > 0)
            {
                // If library is non-null, store it under the key "library".
                attributes.put("library", library);
            }
            
            // If target is the empty string, let target be null.
            String target = annotation.target();
            if (target != null && target.length() > 0)
            {
                // If target is non-null, store it under the key "target".
                attributes.put("target", target);
            }
            else
            {
                // Otherwise, if target is null, call UIViewRoot.addComponentResource(javax.faces.context.FacesContext, 
                // javax.faces.component.UIComponent), passing the UIOutput instance as the second argument.
                context.getViewRoot().addComponentResource(context, output);
            }
        }
    }
    */
}
