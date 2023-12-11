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
package jakarta.faces.component;

import org.apache.myfaces.core.api.shared.lang.ClassUtils;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.el.ValueExpression;
import jakarta.faces.FactoryFinder;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.ConverterException;
import jakarta.faces.render.RenderKit;
import jakarta.faces.render.RenderKitFactory;
import jakarta.faces.render.Renderer;

import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFComponent;
import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFJspProperty;
import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFProperty;
import org.apache.myfaces.core.api.shared.MessageUtils;
import org.apache.myfaces.core.api.shared.lang.Assert;

/**
 * 
 * TODO: documentation on jsp and pld are not the same. It appear two
 * params: maxlength and for, but no property getter and setter founded here. 
 * If maxlength is used, we can put something like this: 
 * JSFJspProperty(name = "maxlength", returnType = "java.lang.String")
 * 
 * @since 2.0
 */
@JSFComponent(name = "f:viewParam")
@JSFJspProperty(name = "maxlength", returnType = "int",
                longDesc = "The max number or characters allowed for this param")
public class UIViewParameter extends UIInput
{
    private static final Logger log = Logger.getLogger(UIViewParameter.class.getName());
    public static final String COMPONENT_FAMILY = "jakarta.faces.ViewParameter";
    public static final String COMPONENT_TYPE = "jakarta.faces.ViewParameter";

    private static final String DELEGATE_FAMILY = UIInput.COMPONENT_FAMILY;
    private static final String DELEGATE_RENDERER_TYPE = "jakarta.faces.Text";
    
    private static ConcurrentHashMap<ClassLoader,Renderer> delegateRendererMap = 
        new ConcurrentHashMap<ClassLoader,Renderer>();

    public UIViewParameter()
    {
        setRendererType(null);
    }

    @Override
    public String getFamily()
    {
        return COMPONENT_FAMILY;
    }

    @Override
    public void decode(FacesContext context)
    {
        // Override behavior from superclass to pull a value from the incoming request parameter map under the 
        // name given by getName() and store it with a call to UIInput.setSubmittedValue(java.lang.Object).
        String value = context.getExternalContext().getRequestParameterMap().get(getName());
        
        // only apply the value if it is non-null (otherwise postbacks 
        // to a view with view parameters would not work correctly)
        if (value != null)
        {
            setSubmittedValue(value);
        }
    }

    @Override
    public void encodeAll(FacesContext context) throws IOException
    {
        Assert.notNull(context, "context");
        setSubmittedValue(getStringValue(context));
    }

    public String getName()
    {
        return (String) getStateHelper().eval(PropertyKeys.name);
    }

    public String getStringValue(FacesContext context)
    {
        if (getValueExpression ("value") != null) 
        {
            // Value specified as an expression, so do the conversion.
            
            return getStringValueFromModel (context);
        }
        
        // Otherwise, just return the local value.
        return ((String) this.getLocalValue());
    }

    public String getStringValueFromModel(FacesContext context) throws ConverterException
    {
        ValueExpression ve = getValueExpression ("value");
        Converter converter;
        Object value;
        
        if (ve == null) 
        {
            // No value expression, return null.
            return null;
        }
        
        value = ve.getValue (context.getELContext());
        
        if (value instanceof String s)
        {
            // No need to convert.
            return s;
        }
        
        converter = getConverter();
        
        if (converter == null) 
        {
            if (value == null) 
            {
                // No converter, no value, return null.
                return null;
            }
            
            // See if we can create the converter from the value type.
            
            converter = context.getApplication().createConverter (value.getClass());
            
            if (converter == null) 
            {
                // Only option is to call toString().
                
                return value.toString();
            }
        }
        
        return converter.getAsString (context, this, value);
    }

    @JSFProperty(tagExcluded=true)
    @Override
    public boolean isImmediate()
    {
        return false;
    }
    
    @JSFProperty(tagExcluded=true)
    @Override
    public boolean isRendered()
    {
        return super.isRendered();
    }
    
    @Override
    public void processValidators(FacesContext context)
    {
        if (context == null) 
        {
            throw new NullPointerException ("context");
        }
        
        // If value is null and required is set, validation fails.
        
        if ((getSubmittedValue() == null) && isRequired()) 
        {
            FacesMessage message;
            String required = getRequiredMessage();
            
            if (required != null) 
            {
                message = new FacesMessage (FacesMessage.SEVERITY_ERROR, required, required);
            }
            else 
            {
                Object label = MessageUtils.getLabel(context, this);
                
                message = MessageUtils.getMessage(context, context.getViewRoot().getLocale(),
                     FacesMessage.SEVERITY_ERROR, REQUIRED_MESSAGE_ID, new Object[] { label });
            }
            
            setValid (false);
            
            context.addMessage (getClientId (context), message);
            context.validationFailed();
            context.renderResponse();
            
            return;
        }
        
        super.processValidators (context);
    }
    
    enum PropertyKeys
    {
        name
    }
    
    public void setName(String name)
    {
        getStateHelper().put(PropertyKeys.name, name );
    }

    @Override
    public void updateModel(FacesContext context)
    {
        super.updateModel(context);
        
        // Put name in request map if value is not a value expression, is valid, and local
        // value was set.
        
        if ((getValueExpression ("value") == null) && isValid() && isLocalValueSet()) 
        {
            context.getExternalContext().getRequestMap().put(getName(), getLocalValue());
        }
    }

    @Override
    protected Object getConvertedValue(FacesContext context, Object submittedValue) throws ConverterException
    {
        return getDelegateRenderer(context).getConvertedValue(context, this, submittedValue);
    }

    private static Renderer getDelegateRenderer(FacesContext context)
    {
        ClassLoader classLoader = ClassUtils.getContextClassLoader();
        Renderer delegateRenderer = delegateRendererMap.get(classLoader);
        if(delegateRenderer == null)
        {
            RenderKitFactory factory = (RenderKitFactory) FactoryFinder.getFactory(FactoryFinder.RENDER_KIT_FACTORY);
            RenderKit kit = factory.getRenderKit(context, RenderKitFactory.HTML_BASIC_RENDER_KIT);

            delegateRenderer = kit.getRenderer(DELEGATE_FAMILY, DELEGATE_RENDERER_TYPE);
            delegateRendererMap.put(classLoader, delegateRenderer);
        }

        return delegateRenderer;
    }

    /**
     * The idea of this method is to be called from AbstractFacesInitializer (impl code).
     */
    @SuppressWarnings("unused")
    private static void releaseRenderer() 
    {
        if (log.isLoggable(Level.FINEST))
        {
            log.finest("releaseRenderer rendererMap -> " + delegateRendererMap.toString());
        }

        ClassLoader classLoader = ClassUtils.getContextClassLoader();
        
        if (log.isLoggable(Level.FINEST))
        {
            log.finest("releaseRenderer classLoader -> " + classLoader.toString() );
            log.finest("releaseRenderer renderer -> " + delegateRendererMap.get(classLoader));
        }
        
        delegateRendererMap.remove(classLoader);
        
        if (log.isLoggable(Level.FINEST))
        {
            log.finest("releaseRenderer renderMap -> " + delegateRendererMap.toString());
        }
        
    }

    @Override
    protected FacesContext getFacesContext()
    {
        //In theory the parent most of the times has 
        //the cached FacesContext instance, because this
        //element is purely logical, and the parent is the one
        //where encodeXXX was invoked. But only limit the
        //search to the closest parent.
        UIComponent parent = getParent();
        if (parent != null && parent.isCachedFacesContext())
        {
            return parent.getFacesContext();
        }
        else
        {
            return super.getFacesContext();
        }
    }

    /**
     * @since 2.0
     */
    public static class Reference
    {
        private int _index;
        private UIViewParameter _param;
        private Object _state;
        private String _viewId;

        public Reference(FacesContext context, UIViewParameter param, int indexInParent,
                         String viewIdAtTimeOfConstruction)
        {
            // This constructor cause the StateHolder.saveState(jakarta.faces.context.FacesContext) method
            // to be called on argument UIViewParameter.
            _param = param;
            _viewId = viewIdAtTimeOfConstruction;
            _index = indexInParent;
            _state = param.saveState(context);
        }

        public UIViewParameter getUIViewParameter(FacesContext context)
        {
            // If the current viewId is the same as the viewId passed to our constructor
            if (context.getViewRoot().getViewId().equals(_viewId))
            {
                // use the index passed to the constructor to find the actual UIViewParameter instance and return it.
                // FIXME: How safe is that when dealing with component trees altered by applications?
                return (UIViewParameter) _param.getParent().getChildren().get(_index);
            }
            else
            {
                // Otherwise, call StateHolder.restoreState(jakarta.faces.context.FacesContext, java.lang.Object) on
                // the saved state and return the result.
                _param.restoreState(context, _state);

                return _param;
            }
        }
    }
}