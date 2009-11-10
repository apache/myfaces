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
package javax.faces.component.behavior;

import java.util.*;

import javax.el.ValueExpression;
import javax.faces.context.FacesContext;
import javax.faces.event.AjaxBehaviorListener;

/**
 * @author Simon Lessard (latest modification by $Author: slessard $)
 * @version $Revision: 696523 $ $Date: 2009-03-14 16:04:27 -0400 (mer., 17 sept. 2008) $
 * @since 2.0
 */
public class AjaxBehavior extends ClientBehaviorBase {

    /**
     * not needed anymore but enforced by the spec
     * theoretically a
     * @FacesBehavior(value = "javax.faces.behavior.Ajax")
     * could do it
     */
    public static final String BEHAVIOR_ID = "javax.faces.behavior.Ajax";

    //To enable delta state saving we need this one
    _AjaxBehaviorDeltaStateHelper<AjaxBehavior> deltaStateHelper = new _AjaxBehaviorDeltaStateHelper<AjaxBehavior>(this);
    private static final String ATTR_EXECUTE = "execute";
    private static final String ATTR_ON_ERROR = "onerror";
    private static final String ATTR_ON_EVENT = "onevent";
    private static final String ATTR_RENDER = "render";
    private static final String ATTR_DISABLED = "disabled";
    private static final String ATTR_IMMEDIATE = "immediate";


    /**
     * special render and execute targets
     */
    private static final String VAL_FORM = "@form";
    private static final String VAL_ALL = "@all";
    private static final String VAL_THIS = "@this";
    private static final String VAL_NONE = "@none";

    //we cannot use Collection - singletons here otherwise the delta state restore would fail
    private static final Collection<String> VAL_FORM_LIST = new ArrayList<String>(1);
    private static final Collection<String> VAL_ALL_LIST = new ArrayList<String>(1);
    private static final Collection<String> VAL_THIS_LIST = new ArrayList<String>(1);
    private static final Collection<String> VAL_NONE_LIST = new ArrayList<String>(1);

    /**
     * internal errors
     */
    private static final String ERR_KEY_IS_NULL = "Given value expression key is null";


    Map<String, ValueExpression> _valueExpressions = new HashMap<String, ValueExpression>();

    static {
        VAL_FORM_LIST.add(VAL_FORM);
        VAL_ALL_LIST.add(VAL_ALL);
        VAL_THIS_LIST.add(VAL_THIS);
        VAL_NONE_LIST.add(VAL_NONE);

    }


    public AjaxBehavior() {
        super();
        getValueExpressionMap();
    }


    private Map<String, ValueExpression> getValueExpressionMap() {
        return _valueExpressions;
    }


    public void addAjaxBehaviorListener(AjaxBehaviorListener listener) {
        super.addBehaviorListener(listener);
    }

    public Collection<String> getExecute() {
        Object execute = deltaStateHelper.eval(ATTR_EXECUTE);

        return (Collection<String>) execute;
    }


    public void setExecute(Collection<String> execute) {
        deltaStateHelper.put(ATTR_EXECUTE, execute);
    }

    public String getOnerror() {
        return (String) deltaStateHelper.eval(ATTR_ON_ERROR);
    }

    public void setOnerror(String onError) {
        deltaStateHelper.put(ATTR_ON_ERROR, onError);
    }

    public String getOnevent() {
        return (String) deltaStateHelper.eval(ATTR_ON_EVENT);
    }

    public void setOnevent(String onEvent) {
        deltaStateHelper.put(ATTR_ON_EVENT, onEvent);
    }

    public Collection<String> getRender() {
        Object render = deltaStateHelper.eval(ATTR_RENDER);

        return (Collection<String>) render;
    }

    public void setRender(Collection<String> render) {
        deltaStateHelper.put(ATTR_RENDER, render);
    }

    public ValueExpression getValueExpression(String name) {
        return getValueExpressionMap().get(name);
    }

    public void setValueExpression(String name, ValueExpression item) {
        if (item == null) {
            getValueExpressionMap().remove(name);
            deltaStateHelper.remove(name);
        } else {
            Object value = item.getValue(FacesContext.getCurrentInstance().getELContext());
            //the tag handler sets the values over value expressions we do have to do a conversion
            if (name.equals(ATTR_EXECUTE) && value instanceof String) {
                //TODO please move this conversion code over to the tag handler
                //I do not think it belongs in here
                applyArrayAttributeFromString(ATTR_EXECUTE, value);
            } else if (name.equals(ATTR_RENDER) && value instanceof String) {
                applyArrayAttributeFromString(ATTR_RENDER, value);
            }
            getValueExpressionMap().put(name, item);
        }
    }

    /**
     * facelets applies its values over the value expressions
     * to have our system working fully we need to reapply
     * the values to our delta state saver
     * in converted form so that getters and setters can work on the maps instead
     * of the string value expression, the delta states have higher priority
     * than the value expressions
     * Since Behaviors do not have converters we have to do it here (the getter
     * or setter is a no go due to the call frequency of this method)
     *
     * @param attribute the attribute name
     * @param value     the value expression to be changed to a collection
     */
    private void applyArrayAttributeFromString(String attribute, Object value) {

        String stringValue = (String) value;
        //@special handling for @all, @none, @form and @this
        if (stringValue.equals(VAL_FORM)) {
            deltaStateHelper.put(attribute, VAL_FORM_LIST);
            return;
        } else if (stringValue.equals(VAL_ALL)) {
            deltaStateHelper.put(attribute, VAL_ALL_LIST);
            return;
        } else if (stringValue.equals(VAL_NONE)) {
            deltaStateHelper.put(attribute, VAL_NONE_LIST);
            return;
        } else if (stringValue.equals(VAL_THIS)) {
            deltaStateHelper.put(attribute, VAL_THIS_LIST);
            return;
        }

        String[] arrValue = ((String) stringValue).split(" ");

        //we have to manually convert otherwise the delta state saving fails
        //Arrays.asList returns a list which is not instantiable

        List<String> saveVal = new ArrayList<String>(arrValue.length);
        saveVal.addAll(Arrays.asList(arrValue));

        deltaStateHelper.put(attribute, saveVal);
    }


    public boolean isDisabled() {
        Boolean retVal = (Boolean) deltaStateHelper.eval(ATTR_DISABLED);
        retVal = (retVal == null) ? false : retVal;
        return retVal;
    }

    public void setDisabled(boolean disabled) {
        deltaStateHelper.put(ATTR_DISABLED, disabled);
    }

    public boolean isImmediate() {
        Boolean retVal = (Boolean) deltaStateHelper.eval(ATTR_IMMEDIATE);
        retVal = (retVal == null) ? false : retVal;
        return retVal;
    }

    public void setImmediate(boolean immediate) {
        deltaStateHelper.put(ATTR_IMMEDIATE, immediate);
    }

    public boolean isImmediateSet() {
        return deltaStateHelper.eval(ATTR_IMMEDIATE) != null;
    }


    public void removeAjaxBehaviorListener(AjaxBehaviorListener listener) {
        removeBehaviorListener(listener);
    }


    @Override
    public Set<ClientBehaviorHint> getHints() {
        return EnumSet.of(ClientBehaviorHint.SUBMITTING);
    }


    @Override
    public String getRendererType() {
        return BEHAVIOR_ID;
    }


    @Override
    public void restoreState(FacesContext facesContext, Object o) {
        Object[] values = (Object[]) o;

        if (values[0] != null) {
            super.restoreState(facesContext, values[0]);
        }
       deltaStateHelper.restoreState(facesContext, values[1]);
    }

    @Override
    public Object saveState(FacesContext facesContext) {
        Object[] values = new Object[2];
        values[0] = super.saveState(facesContext);
        values[1] = deltaStateHelper.saveState(facesContext);
        return values;
    }

    private void assertNull(Object item) {
        if (item == null) {
            throw new NullPointerException(ERR_KEY_IS_NULL);
        }
    }
    

}
