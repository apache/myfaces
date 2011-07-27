/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 * 
 *  http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.myfaces.shared.taglib;

import org.apache.myfaces.shared.renderkit.JSFAttr;

import javax.faces.component.UIComponent;
import javax.faces.webapp.UIComponentTag;

/**
 * @author Manfred Geiler (latest modification by $Author$)
 * @version $Revision$ $Date$
 *
 * @deprecated use {@link UIComponentELTagBase} instead
 */
public abstract class UIComponentTagBase
        extends UIComponentTag
{
    //private static final Log log = LogFactory.getLog(UIComponentTagBase.class);

    //UIComponent attributes
    private String _forceId;
    private String _forceIdIndex = "true";
    private String _javascriptLocation;
    private String _imageLocation;
    private String _styleLocation;

    //Special UIComponent attributes (ValueHolder, ConvertibleValueHolder)
    private String _value;
    private String _converter;
    //attributes id, rendered and binding are handled by UIComponentTag

    public void release() {
        super.release();

        _forceId=null;
        //see declaration of that property
        _forceIdIndex = "true";

        _value=null;
        _converter=null;
        
        _javascriptLocation = null;
        _imageLocation = null;
        _styleLocation = null;
    }

    protected void setProperties(UIComponent component)
    {
        super.setProperties(component);

        setBooleanProperty(component, org.apache.myfaces.shared.renderkit.JSFAttr.FORCE_ID_ATTR, _forceId);
        setBooleanProperty(component, org.apache.myfaces.shared.renderkit.JSFAttr.FORCE_ID_INDEX_ATTR, _forceIdIndex);
        if (_javascriptLocation != null) setStringProperty(component, JSFAttr.JAVASCRIPT_LOCATION, _javascriptLocation);
        if (_imageLocation != null) setStringProperty(component, org.apache.myfaces.shared.renderkit.JSFAttr.IMAGE_LOCATION, _imageLocation);
        if (_styleLocation != null) setStringProperty(component, JSFAttr.STYLE_LOCATION, _styleLocation);

        //rendererType already handled by UIComponentTag

        setValueProperty(component, _value);
        setConverterProperty(component, _converter);
    }

    /**
     * Sets the forceId attribute of the tag.  NOTE: Not every tag that extends this class will
     * actually make use of this attribute.  Check the TLD to see which components actually
     * implement it.
     *
     * @param aForceId The value of the forceId attribute.
     */
    public void setForceId(String aForceId)
    {
        _forceId = aForceId;
    }

    /**
     * Sets the forceIdIndex attribute of the tag.  NOTE: Not every tag that extends this class will
     * actually make use of this attribute.  Check the TLD to see which components actually implement it.
     *
     * @param aForceIdIndex The value of the forceIdIndex attribute.
     */
    public void setForceIdIndex(String aForceIdIndex)
    {
        _forceIdIndex = aForceIdIndex;
    }

    public void setValue(String value)
    {
        _value = value;
    }

    public void setConverter(String converter)
    {
        _converter = converter;
    }


    /**
     * Sets the javascript location attribute of the tag.  NOTE: Not every tag that extends this class will
     * actually make use of this attribute.  Check the TLD to see which components actually implement it.
     *
     * @param aJavascriptLocation The alternate javascript location to use.
     */
    public void setJavascriptLocation(String aJavascriptLocation)
    {
        _javascriptLocation = aJavascriptLocation;
    }

    /**
     * Sets the image location attribute of the tag.  NOTE: Not every tag that extends this class will
     * actually make use of this attribute.  Check the TLD to see which components actually implement it.
     *
     * @param aImageLocation The alternate image location to use.
     */
    public void setImageLocation(String aImageLocation)
    {
        _imageLocation = aImageLocation;
    }

    /**
     * Sets the style location attribute of the tag.  NOTE: Not every tag that extends this class will
     * actually make use of this attribute.  Check the TLD to see which components actually implement it.
     *
     * @param aStyleLocation The alternate style location to use.
     */
    public void setStyleLocation(String aStyleLocation)
    {
        _styleLocation = aStyleLocation;
    }

    // sub class helpers

    protected void setIntegerProperty(UIComponent component, String propName, String value)
    {
        UIComponentTagUtils.setIntegerProperty(getFacesContext(), component, propName, value);
    }

    protected void setLongProperty(UIComponent component, String propName, String value)
    {
        UIComponentTagUtils.setLongProperty(getFacesContext(), component, propName, value);
    }

    protected void setStringProperty(UIComponent component, String propName, String value)
    {
        UIComponentTagUtils.setStringProperty(getFacesContext(), component, propName, value);
    }

    protected void setBooleanProperty(UIComponent component, String propName, String value)
    {
        UIComponentTagUtils.setBooleanProperty(getFacesContext(), component, propName, value);
    }

    private void setValueProperty(UIComponent component, String value)
    {
        UIComponentTagUtils.setValueProperty(getFacesContext(), component, value);
    }

    private void setConverterProperty(UIComponent component, String value)
    {
        UIComponentTagUtils.setConverterProperty(getFacesContext(), component, value);
    }

    protected void setValidatorProperty(UIComponent component, String value)
    {
        UIComponentTagUtils.setValidatorProperty(getFacesContext(), component, value);
    }

    protected void setActionProperty(UIComponent component, String action)
    {
        UIComponentTagUtils.setActionProperty(getFacesContext(), component, action);
    }

    protected void setActionListenerProperty(UIComponent component, String actionListener)
    {
        UIComponentTagUtils.setActionListenerProperty(getFacesContext(), component, actionListener);
    }

    protected void setValueChangedListenerProperty(UIComponent component, String valueChangedListener)
    {
        UIComponentTagUtils.setValueChangedListenerProperty(getFacesContext(), component, valueChangedListener);
    }

    protected void setValueBinding(UIComponent component,
                                   String propName,
                                   String value)
    {
        UIComponentTagUtils.setValueBinding(getFacesContext(), component, propName, value);
    }


}

