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

import java.io.IOException;

import javax.faces.FactoryFinder;
import javax.faces.context.FacesContext;
import javax.faces.convert.ConverterException;
import javax.faces.render.RenderKit;
import javax.faces.render.RenderKitFactory;
import javax.faces.render.Renderer;

/**
 * TODO: PLUGINIZE?
 * 
 * @author Simon Lessard (latest modification by $Author: slessard $)
 * @version $Revision: 696523 $ $Date: 2009-03-14 14:43:57 -0400 (mer., 17 sept. 2008) $
 * 
 * @since 2.0
 */
public class UIViewParameter extends UIInput
{
    public static final String COMPONENT_FAMILY = "javax.faces.ViewParameter";
    public static final String COMPONENT_TYPE = "javax.faces.ViewParameter";

    private static final String DELEGATE_FAMILY = UIInput.COMPONENT_FAMILY;
    private static final String DELEGATE_RENDERER_TYPE = "javax.faces.Text";

    private static Renderer _delegateRenderer;

    private String _name;

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
        setSubmittedValue(context.getExternalContext().getRequestParameterMap().get(getName()));
    }

    @Override
    public void encodeAll(FacesContext context) throws IOException
    {
        setSubmittedValue(getStringValue(context));
    }

    public String getName()
    {
        return _name;
    }

    public String getStringValue(FacesContext context)
    {
        // TODO: IMPLEMENT

        return null;
    }

    public String getStringValueFromModel(FacesContext context) throws ConverterException
    {
        // TODO: IMPLEMENT

        return null;
    }

    @Override
    public String getSubmittedValue()
    {
        return (String)super.getSubmittedValue();
    }

    @Override
    public boolean isImmediate()
    {
        return false;
    }

    @Override
    public void processValidators(FacesContext context)
    {
        // TODO: IMPLEMENT
    }

    @Override
    public void restoreState(FacesContext context, Object state)
    {
        Object[] stateValues = (Object[]) state;
        super.restoreState(context, stateValues[0]);
        _name = (String) stateValues[1];
    }

    @Override
    public Object saveState(FacesContext context)
    {
        Object[] state = new Object[2];
        state[0] = super.saveState(context);
        state[1] = _name;

        return state;
    }

    public void setName(String name)
    {
        _name = name;
    }

    @Override
    public void updateModel(FacesContext context)
    {
        super.updateModel(context);
        
        // TODO: IMPLEMENT
    }

    @Override
    protected Object getConvertedValue(FacesContext context, Object submittedValue)
    {
        return getDelegateRenderer(context).getConvertedValue(context, this, submittedValue);
    }

    private static Renderer getDelegateRenderer(FacesContext context)
    {
        if (_delegateRenderer == null)
        {
            RenderKitFactory factory = (RenderKitFactory) FactoryFinder.getFactory(FactoryFinder.RENDER_KIT_FACTORY);
            RenderKit kit = factory.getRenderKit(context, RenderKitFactory.HTML_BASIC_RENDER_KIT);

            _delegateRenderer = kit.getRenderer(DELEGATE_FAMILY, DELEGATE_RENDERER_TYPE);
        }

        return _delegateRenderer;
    }

    /**
     * @author Simon Lessard (latest modification by $Author: slessard $)
     * @version $Revision: 696523 $ $Date: 2009-03-14 14:43:57 -0400 (mer., 17 sept. 2008) $
     * 
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
            // This constructor cause the StateHolder.saveState(javax.faces.context.FacesContext) method
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
                // Otherwise, call StateHolder.restoreState(javax.faces.context.FacesContext, java.lang.Object) on
                // the saved state and return the result.
                _param.restoreState(context, _state);

                return _param;
            }
        }
    }
}
