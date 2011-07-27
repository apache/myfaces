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
package org.apache.myfaces.shared.renderkit.html;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.faces.FacesException;
import javax.faces.application.ConfigurableNavigationHandler;
import javax.faces.application.NavigationCase;
import javax.faces.application.NavigationHandler;
import javax.faces.application.ProjectStage;
import javax.faces.application.ViewHandler;
import javax.faces.component.EditableValueHolder;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.component.UINamingContainer;
import javax.faces.component.UIOutcomeTarget;
import javax.faces.component.UIOutput;
import javax.faces.component.UIParameter;
import javax.faces.component.UISelectBoolean;
import javax.faces.component.UISelectMany;
import javax.faces.component.UISelectOne;
import javax.faces.component.UIViewRoot;
import javax.faces.component.behavior.ClientBehavior;
import javax.faces.component.behavior.ClientBehaviorContext;
import javax.faces.component.behavior.ClientBehaviorHint;
import javax.faces.component.behavior.ClientBehaviorHolder;
import javax.faces.component.html.HtmlDataTable;
import javax.faces.component.html.HtmlMessages;
import javax.faces.component.html.HtmlPanelGrid;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.convert.Converter;
import javax.faces.model.SelectItem;
import javax.faces.model.SelectItemGroup;

import org.apache.myfaces.shared.component.DisplayValueOnlyCapable;
import org.apache.myfaces.shared.component.EscapeCapable;
import org.apache.myfaces.shared.config.MyfacesConfig;
import org.apache.myfaces.shared.renderkit.ClientBehaviorEvents;
import org.apache.myfaces.shared.renderkit.JSFAttr;
import org.apache.myfaces.shared.renderkit.RendererUtils;
import org.apache.myfaces.shared.renderkit.html.util.FormInfo;
import org.apache.myfaces.shared.renderkit.html.util.HTMLEncoder;
import org.apache.myfaces.shared.renderkit.html.util.JavascriptUtils;
import org.apache.myfaces.shared.renderkit.html.util.ResourceUtils;

/**
 * @author Manfred Geiler (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public final class HtmlRendererUtils {
    //private static final Log log = LogFactory.getLog(HtmlRendererUtils.class);
    private static final Logger log = Logger.getLogger(HtmlRendererUtils.class.getName());

    //private static final String[] EMPTY_STRING_ARRAY = new String[0];
    private static final String LINE_SEPARATOR = System.getProperty(
            "line.separator", "\r\n");
    private static final char TABULATOR = '\t';

    public static final String HIDDEN_COMMANDLINK_FIELD_NAME = "_idcl";
    public static final String HIDDEN_COMMANDLINK_FIELD_NAME_MYFACES_OLD = "_link_hidden_";
    public static final String HIDDEN_COMMANDLINK_FIELD_NAME_TRINIDAD = "source";

    public static final String CLEAR_HIDDEN_FIELD_FN_NAME =
            "clearFormHiddenParams";
    public static final String SUBMIT_FORM_FN_NAME = "oamSubmitForm";
    public static final String SUBMIT_FORM_FN_NAME_JSF2 = "myfaces.oam.submitForm";
    public static final String ALLOW_CDATA_SECTION_ON = "org.apache.myfaces.ResponseWriter.CdataSectionOn";

    private static final String SET_HIDDEN_INPUT_FN_NAME = "oamSetHiddenInput";
    private static final String SET_HIDDEN_INPUT_FN_NAME_JSF2 = "myfaces.oam.setHiddenInput";
    private static final String CLEAR_HIDDEN_INPUT_FN_NAME = "oamClearHiddenInput";

    private static final String AUTO_SCROLL_PARAM = "autoScroll";
    private static final String AUTO_SCROLL_FUNCTION = "getScrolling";

    private static final String FIRST_SUBMIT_SCRIPT_ON_PAGE = "org.apache.MyFaces.FIRST_SUBMIT_SCRIPT_ON_PAGE";

    public static final String NON_SUBMITTED_VALUE_WARNING
            = "There should always be a submitted value for an input if it is rendered,"
            + " its form is submitted, and it was not originally rendered disabled or read-only."
            + "  You cannot submit a form after disabling an input element via javascript."
            + "  Consider setting read-only to true instead"
            + " or resetting the disabled value back to false prior to form submission.";
    private static final String STR_EMPTY = "";


    private HtmlRendererUtils() {
        // utility class, do not instantiate
    }

    /**
     * Utility to set the submitted value of the provided component from the
     * data in the current request object.
     * <p/>
     * Param component is required to be an EditableValueHolder. On return
     * from this method, the component's submittedValue property will be
     * set if the submitted form contained that component.
     */
    public static void decodeUIInput(FacesContext facesContext,
                                     UIComponent component) {
        if (!(component instanceof EditableValueHolder)) {
            throw new IllegalArgumentException("Component "
                    + component.getClientId(facesContext)
                    + " is not an EditableValueHolder");
        }
        Map paramMap = facesContext.getExternalContext()
                .getRequestParameterMap();
        String clientId = component.getClientId(facesContext);

        if (isDisabledOrReadOnly(component))
            return;

        if (paramMap.containsKey(clientId)) {
            ((EditableValueHolder) component).setSubmittedValue(paramMap
                    .get(clientId));
        } else {
            log.warning(NON_SUBMITTED_VALUE_WARNING +
                    " Component : " +
                    RendererUtils.getPathToComponent(component));
        }
    }

    /**
     * X-CHECKED: tlddoc h:selectBooleanCheckbox
     *
     * @param facesContext
     * @param component
     */
    public static void decodeUISelectBoolean(FacesContext facesContext,
                                             UIComponent component) {
        if (!(component instanceof EditableValueHolder)) {
            throw new IllegalArgumentException("Component "
                    + component.getClientId(facesContext)
                    + " is not an EditableValueHolder");
        }

        if (isDisabledOrReadOnly(component))
            return;

        Map paramMap = facesContext.getExternalContext()
                .getRequestParameterMap();
        String clientId = component.getClientId(facesContext);
        if (paramMap.containsKey(clientId)) {
            String reqValue = (String) paramMap.get(clientId);
            if ((reqValue.equalsIgnoreCase("on")
                    || reqValue.equalsIgnoreCase("yes") || reqValue
                    .equalsIgnoreCase("true"))) {
                ((EditableValueHolder) component)
                        .setSubmittedValue(Boolean.TRUE);
            } else {
                ((EditableValueHolder) component)
                        .setSubmittedValue(Boolean.FALSE);
            }
        } else {
            ((EditableValueHolder) component)
                    .setSubmittedValue(Boolean.FALSE);
        }
    }

    public static boolean isDisabledOrReadOnly(UIComponent component) {
        return isDisplayValueOnly(component) ||
                isDisabled(component) ||
                isReadOnly(component);
    }

    public static boolean isDisabled(UIComponent component) {
        return isTrue(component.getAttributes().get("disabled"));
    }

    public static boolean isReadOnly(UIComponent component) {
        return isTrue(component.getAttributes().get("readonly"));
    }

    private static boolean isTrue(Object obj) {
        if (obj instanceof String) {
            return new Boolean((String) obj);
        }

        if (!(obj instanceof Boolean))
            return false;

        return ((Boolean) obj).booleanValue();
    }

    /**
     * X-CHECKED: tlddoc h:selectManyListbox
     *
     * @param facesContext
     * @param component
     */
    public static void decodeUISelectMany(FacesContext facesContext,
                                          UIComponent component) {
        if (!(component instanceof EditableValueHolder)) {
            throw new IllegalArgumentException("Component "
                    + component.getClientId(facesContext)
                    + " is not an EditableValueHolder");
        }
        Map paramValuesMap = facesContext.getExternalContext()
                .getRequestParameterValuesMap();
        String clientId = component.getClientId(facesContext);

        if (isDisabledOrReadOnly(component))
            return;

        if (paramValuesMap.containsKey(clientId)) {
            String[] reqValues = (String[]) paramValuesMap.get(clientId);
            ((EditableValueHolder) component).setSubmittedValue(reqValues);
        } else {
            /* request parameter not found, nothing to decode - set submitted value to an empty array
               as we should get here only if the component is on a submitted form, is rendered
               and if the component is not readonly or has not been disabled.

               So in fact, there must be component value at this location, but for listboxes, comboboxes etc.
               the submitted value is not posted if no item is selected. */
            ((EditableValueHolder) component).setSubmittedValue(new String[]{});
        }
    }

    /**
     * X-CHECKED: tlddoc h:selectManyListbox
     *
     * @param facesContext
     * @param component
     */
    public static void decodeUISelectOne(FacesContext facesContext,
                                         UIComponent component) {
        if (!(component instanceof EditableValueHolder)) {
            throw new IllegalArgumentException("Component "
                    + component.getClientId(facesContext)
                    + " is not an EditableValueHolder");
        }

        if (isDisabledOrReadOnly(component))
            return;

        Map paramMap = facesContext.getExternalContext()
                .getRequestParameterMap();
        String clientId = component.getClientId(facesContext);
        if (paramMap.containsKey(clientId)) {
            //request parameter found, set submitted value
            ((EditableValueHolder) component).setSubmittedValue(paramMap
                    .get(clientId));
        } else {
            //see reason for this action at decodeUISelectMany
            ((EditableValueHolder) component).setSubmittedValue(STR_EMPTY);
        }
    }

    /**
     * @param facesContext
     * @param component
     * @since 4.0.0
     */
    public static void decodeClientBehaviors(FacesContext facesContext,
                                             UIComponent component) {
        if (component instanceof ClientBehaviorHolder) {
            ClientBehaviorHolder clientBehaviorHolder = (ClientBehaviorHolder) component;

            Map<String, List<ClientBehavior>> clientBehaviors =
                    clientBehaviorHolder.getClientBehaviors();

            if (clientBehaviors != null && !clientBehaviors.isEmpty()) {
                Map<String, String> paramMap = facesContext.getExternalContext().
                        getRequestParameterMap();

                String behaviorEventName = paramMap.get("javax.faces.behavior.event");

                if (behaviorEventName != null) {
                    List<ClientBehavior> clientBehaviorList = clientBehaviors.get(behaviorEventName);

                    if (clientBehaviorList != null && !clientBehaviorList.isEmpty()) {
                        String clientId = paramMap.get("javax.faces.source");

                        if (component.getClientId().equals(clientId)) {
                            for (ClientBehavior clientBehavior : clientBehaviorList) {
                                clientBehavior.decode(facesContext, component);
                            }
                        }
                    }
                }
            }
        }
    }

    /*
     * public static void renderCheckbox(FacesContext facesContext, UIComponent
     * uiComponent, String value, String label, boolean checked) throws
     * IOException { String clientId = uiComponent.getClientId(facesContext);
     *
     * ResponseWriter writer = facesContext.getResponseWriter();
     *
     * writer.startElement(HTML.INPUT_ELEM, uiComponent);
     * writer.writeAttribute(HTML.TYPE_ATTR, HTML.INPUT_TYPE_CHECKBOX, null);
     * writer.writeAttribute(HTML.NAME_ATTR, clientId, null);
     * writer.writeAttribute(HTML.ID_ATTR, clientId, null);
     *
     * if (checked) { writer.writeAttribute(HTML.CHECKED_ATTR,
     * HTML.CHECKED_ATTR, null); }
     *
     * if ((value != null) && (value.length() > 0)) {
     * writer.writeAttribute(HTML.VALUE_ATTR, value, null); }
     *
     * renderHTMLAttributes(writer, uiComponent,
     * HTML.INPUT_PASSTHROUGH_ATTRIBUTES); renderDisabledOnUserRole(writer,
     * uiComponent, facesContext);
     *
     * if ((label != null) && (label.length() > 0)) {
     * writer.write(HTML.NBSP_ENTITY); writer.writeText(label, null); }
     *
     * writer.endElement(HTML.INPUT_ELEM); }
     */

    public static void renderListbox(FacesContext facesContext, UISelectOne selectOne,
                                     boolean disabled, int size, Converter converter)
            throws IOException {
        internalRenderSelect(facesContext, selectOne, disabled, size, false, converter);
    }

    public static void renderListbox(FacesContext facesContext, UISelectMany selectMany,
                                     boolean disabled, int size, Converter converter)
            throws IOException {
        internalRenderSelect(facesContext, selectMany, disabled, size, true, converter);
    }

    public static void renderMenu(FacesContext facesContext, UISelectOne selectOne,
                                  boolean disabled, Converter converter) throws IOException {
        internalRenderSelect(facesContext, selectOne, disabled, 1, false, converter);
    }

    public static void renderMenu(FacesContext facesContext, UISelectMany selectMany,
                                  boolean disabled, Converter converter) throws IOException {
        internalRenderSelect(facesContext, selectMany, disabled, 1, true, converter);
    }

    private static void internalRenderSelect(FacesContext facesContext,
                                             UIComponent uiComponent, boolean disabled, int size,
                                             boolean selectMany, Converter converter) throws IOException {
        ResponseWriter writer = facesContext.getResponseWriter();

        writer.startElement(HTML.SELECT_ELEM, uiComponent);
        if (uiComponent instanceof ClientBehaviorHolder
                && JavascriptUtils.isJavascriptAllowed(facesContext.getExternalContext())
                && !((ClientBehaviorHolder) uiComponent).getClientBehaviors().isEmpty()) {
            writer.writeAttribute(HTML.ID_ATTR,
                    uiComponent.getClientId(facesContext), null);
        } else {
            HtmlRendererUtils.writeIdIfNecessary(writer, uiComponent, facesContext);
        }
        writer.writeAttribute(HTML.NAME_ATTR,
                uiComponent.getClientId(facesContext), null);

        List selectItemList;
        if (selectMany) {
            writer.writeAttribute(HTML.MULTIPLE_ATTR, HTML.MULTIPLE_ATTR, null);
            selectItemList = org.apache.myfaces.shared.renderkit.RendererUtils
                    .getSelectItemList((UISelectMany) uiComponent, facesContext);
        } else {
            selectItemList = RendererUtils
                    .getSelectItemList((UISelectOne) uiComponent, facesContext);
        }

        if (size == Integer.MIN_VALUE) {
            //No size given (Listbox) --> size is number of select items
            writer.writeAttribute(HTML.SIZE_ATTR, Integer
                    .toString(selectItemList.size()), null);
        } else {
            writer.writeAttribute(HTML.SIZE_ATTR, Integer.toString(size), null);
        }
        Map<String, List<ClientBehavior>> behaviors = null;
        if (uiComponent instanceof ClientBehaviorHolder && JavascriptUtils.isJavascriptAllowed(facesContext.getExternalContext())) {
            behaviors = ((ClientBehaviorHolder) uiComponent).getClientBehaviors();
            renderBehaviorizedOnchangeEventHandler(facesContext, writer, uiComponent, behaviors);
            renderBehaviorizedEventHandlers(facesContext, writer, uiComponent, behaviors);
            renderBehaviorizedFieldEventHandlersWithoutOnchange(facesContext, writer, uiComponent, behaviors);
            renderHTMLAttributes(writer, uiComponent, HTML.SELECT_PASSTHROUGH_ATTRIBUTES_WITHOUT_DISABLED_AND_EVENTS);
        } else {
            renderHTMLAttributes(writer, uiComponent,
                    HTML.SELECT_PASSTHROUGH_ATTRIBUTES_WITHOUT_DISABLED);
        }

        if (disabled) {
            writer.writeAttribute(HTML.DISABLED_ATTR, Boolean.TRUE, null);
        }

        if (isReadOnly(uiComponent)) {
            writer.writeAttribute(HTML.READONLY_ATTR, HTML.READONLY_ATTR, null);
        }

        Set lookupSet = getSubmittedOrSelectedValuesAsSet(selectMany, uiComponent, facesContext, converter);

        renderSelectOptions(facesContext, uiComponent, converter, lookupSet,
                selectItemList);
        // bug #970747: force separate end tag
        writer.writeText(STR_EMPTY, null);
        writer.endElement(HTML.SELECT_ELEM);
    }

    public static Set getSubmittedOrSelectedValuesAsSet(boolean selectMany, UIComponent uiComponent, FacesContext facesContext, Converter converter) {
        Set lookupSet;

        if (selectMany) {
            UISelectMany uiSelectMany = (UISelectMany) uiComponent;
            lookupSet = RendererUtils.getSubmittedValuesAsSet(facesContext, uiComponent, converter, uiSelectMany);
            if (lookupSet == null) {
                lookupSet = RendererUtils.getSelectedValuesAsSet(facesContext, uiComponent, converter, uiSelectMany);
            }
        } else {
            UISelectOne uiSelectOne = (UISelectOne) uiComponent;
            Object lookup = uiSelectOne.getSubmittedValue();
            if (lookup == null) {
                lookup = uiSelectOne.getValue();
                String lookupString = RendererUtils.getConvertedStringValue(facesContext, uiComponent, converter, lookup);
                lookupSet = Collections.singleton(lookupString);
            } else if (STR_EMPTY.equals(lookup)) {
                lookupSet = Collections.EMPTY_SET;
            } else {
                lookupSet = Collections.singleton(lookup);
            }
        }
        return lookupSet;
    }

    public static Converter findUISelectManyConverterFailsafe(FacesContext facesContext, UIComponent uiComponent) {
        // invoke with considerValueType = false
        return findUISelectManyConverterFailsafe(facesContext, uiComponent, false);
    }

    public static Converter findUISelectManyConverterFailsafe(FacesContext facesContext,
                                                              UIComponent uiComponent, boolean considerValueType) {
        Converter converter;
        try {
            converter = RendererUtils.findUISelectManyConverter(
                    facesContext, (UISelectMany) uiComponent, considerValueType);
        } catch (FacesException e) {
            log.log(Level.SEVERE, "Error finding Converter for component with id "
                    + uiComponent.getClientId(facesContext), e);
            converter = null;
        }
        return converter;
    }

    public static Converter findUIOutputConverterFailSafe(FacesContext facesContext, UIComponent uiComponent) {
        Converter converter;
        try {
            converter = RendererUtils.findUIOutputConverter(facesContext,
                    (UIOutput) uiComponent);
        } catch (FacesException e) {
            log.log(Level.SEVERE, "Error finding Converter for component with id "
                    + uiComponent.getClientId(facesContext), e);
            converter = null;
        }
        return converter;
    }

    /**
     * Renders the select options for a <code>UIComponent</code> that is
     * rendered as an HTML select element.
     *
     * @param context        the current <code>FacesContext</code>.
     * @param component      the <code>UIComponent</code> whose options need to be
     *                       rendered.
     * @param converter      <code>component</code>'s converter
     * @param lookupSet      the <code>Set</code> to use to look up selected options
     * @param selectItemList the <code>List</code> of <code>SelectItem</code> s to be
     *                       rendered as HTML option elements.
     * @throws IOException
     */
    public static void renderSelectOptions(FacesContext context,
                                           UIComponent component, Converter converter, Set lookupSet,
                                           List selectItemList) throws IOException {
        ResponseWriter writer = context.getResponseWriter();

        // check for the hideNoSelectionOption attribute
        boolean hideNoSelectionOption = isHideNoSelectionOption(component);

        for (Iterator it = selectItemList.iterator(); it.hasNext();) {
            SelectItem selectItem = (SelectItem) it.next();

            if (selectItem instanceof SelectItemGroup) {
                writer.startElement(HTML.OPTGROUP_ELEM, component);
                writer.writeAttribute(HTML.LABEL_ATTR, selectItem.getLabel(),
                        null);
                SelectItem[] selectItems = ((SelectItemGroup) selectItem)
                        .getSelectItems();
                renderSelectOptions(context, component, converter, lookupSet,
                        Arrays.asList(selectItems));
                writer.endElement(HTML.OPTGROUP_ELEM);
            } else {
                String itemStrValue = org.apache.myfaces.shared.renderkit.RendererUtils.getConvertedStringValue(context, component,
                        converter, selectItem);
                boolean selected = lookupSet.contains(itemStrValue); //TODO/FIX: we always compare the String vales, better fill lookupSet with Strings only when useSubmittedValue==true, else use the real item value Objects

                // IF the hideNoSelectionOption attribute of the component is true
                // AND this selectItem is the "no selection option"
                // AND there are currently selected items 
                // AND this item (the "no selection option") is not selected
                // (if there is currently no value on UISelectOne, lookupSet contains "")
                if (hideNoSelectionOption && selectItem.isNoSelectionOption()
                        && lookupSet.size() != 0 && !(lookupSet.size() == 1 && lookupSet.contains(""))
                        && !selected) {
                    // do not render this selectItem
                    continue;
                }

                writer.write(TABULATOR);
                writer.startElement(HTML.OPTION_ELEM, component);
                if (itemStrValue != null) {
                    writer.writeAttribute(HTML.VALUE_ATTR, itemStrValue, null);
                }
                else 
                {
                    writer.writeAttribute(HTML.VALUE_ATTR, "", null);
                }
                

                if (selected) {
                    writer.writeAttribute(HTML.SELECTED_ATTR,
                            HTML.SELECTED_ATTR, null);
                }

                boolean disabled = selectItem.isDisabled();
                if (disabled) {
                    writer.writeAttribute(HTML.DISABLED_ATTR,
                            HTML.DISABLED_ATTR, null);
                }

                String labelClass = null;
                boolean componentDisabled = isTrue(component.getAttributes().get("disabled"));

                if (componentDisabled || disabled) {
                    labelClass = (String) component.getAttributes().get(JSFAttr.DISABLED_CLASS_ATTR);
                } else {
                    labelClass = (String) component.getAttributes().get(JSFAttr.ENABLED_CLASS_ATTR);
                }
                if (labelClass != null) {
                    writer.writeAttribute("class", labelClass, "labelClass");
                }

                boolean escape;
                if (component instanceof EscapeCapable) {
                    escape = ((EscapeCapable) component).isEscape();

                    // Preserve tomahawk semantic. If escape=false
                    // all items should be non escaped. If escape
                    // is true check if selectItem.isEscape() is
                    // true and do it.
                    // This is done for remain compatibility.
                    if (escape && selectItem.isEscape()) {
                        writer.writeText(selectItem.getLabel(), null);
                    } else {
                        writer.write(selectItem.getLabel());
                    }
                } else {
                    escape = RendererUtils.getBooleanAttribute(component, JSFAttr.ESCAPE_ATTR,
                            false);
                    //default is to escape
                    //In JSF 1.2, when a SelectItem is created by default 
                    //selectItem.isEscape() returns true (this property
                    //is not available on JSF 1.1).
                    //so, if we found a escape property on the component
                    //set to true, escape every item, but if not
                    //check if isEscape() = true first.
                    if (escape || selectItem.isEscape()) {
                        writer.writeText(selectItem.getLabel(), null);
                    } else {
                        writer.write(selectItem.getLabel());
                    }
                }

                writer.endElement(HTML.OPTION_ELEM);
            }
        }
    }

    /*
     * public static void renderRadio(FacesContext facesContext, UIInput
     * uiComponent, String value, String label, boolean checked) throws
     * IOException { String clientId = uiComponent.getClientId(facesContext);
     *
     * ResponseWriter writer = facesContext.getResponseWriter();
     *
     * writer.startElement(HTML.INPUT_ELEM, uiComponent);
     * writer.writeAttribute(HTML.TYPE_ATTR, HTML.INPUT_TYPE_RADIO, null);
     * writer.writeAttribute(HTML.NAME_ATTR, clientId, null);
     * writer.writeAttribute(HTML.ID_ATTR, clientId, null);
     *
     * if (checked) { writer.writeAttribute(HTML.CHECKED_ATTR,
     * HTML.CHECKED_ATTR, null); }
     *
     * if ((value != null) && (value.length() > 0)) {
     * writer.writeAttribute(HTML.VALUE_ATTR, value, null); }
     *
     * renderHTMLAttributes(writer, uiComponent,
     * HTML.INPUT_PASSTHROUGH_ATTRIBUTES); renderDisabledOnUserRole(writer,
     * uiComponent, facesContext);
     *
     * if ((label != null) && (label.length() > 0)) {
     * writer.write(HTML.NBSP_ENTITY); writer.writeText(label, null); }
     *
     * writer.endElement(HTML.INPUT_ELEM); }
     */

    public static void writePrettyLineSeparator(FacesContext facesContext)
            throws IOException {
        if (org.apache.myfaces.shared.config.MyfacesConfig.getCurrentInstance(facesContext.getExternalContext())
                .isPrettyHtml()) {
            facesContext.getResponseWriter().write(LINE_SEPARATOR);
        }
    }

    public static void writePrettyIndent(FacesContext facesContext)
            throws IOException {
        if (org.apache.myfaces.shared.config.MyfacesConfig.getCurrentInstance(facesContext.getExternalContext())
                .isPrettyHtml()) {
            facesContext.getResponseWriter().write('\t');
        }
    }

    /**
     * @return true, if the attribute was written
     * @throws java.io.IOException
     */
    public static boolean renderHTMLAttribute(ResponseWriter writer,
                                              String componentProperty, String attrName, Object value)
            throws IOException {
        if (!RendererUtils.isDefaultAttributeValue(value)) {
            // render JSF "styleClass" and "itemStyleClass" attributes as "class"
            String htmlAttrName =
                    attrName.equals(HTML.STYLE_CLASS_ATTR) ?
                            HTML.CLASS_ATTR : attrName;
            writer.writeAttribute(htmlAttrName, value, componentProperty);
            return true;
        }

        return false;
    }

    /**
     * @return true, if the attribute was written
     * @throws java.io.IOException
     */
    public static boolean renderHTMLAttribute(ResponseWriter writer,
                                              UIComponent component, String componentProperty, String htmlAttrName)
            throws IOException {
        Object value = component.getAttributes().get(componentProperty);
        return renderHTMLAttribute(writer, componentProperty, htmlAttrName,
                value);
    }

    /**
     * @return true, if an attribute was written
     * @throws java.io.IOException
     */
    public static boolean renderHTMLAttributes(ResponseWriter writer,
                                               UIComponent component, String[] attributes) throws IOException {
        boolean somethingDone = false;
        for (int i = 0, len = attributes.length; i < len; i++) {
            String attrName = attributes[i];
            if (renderHTMLAttribute(writer, component, attrName, attrName)) {
                somethingDone = true;
            }
        }
        return somethingDone;
    }

    public static boolean renderHTMLAttributeWithOptionalStartElement(
            ResponseWriter writer, UIComponent component, String elementName,
            String attrName, Object value, boolean startElementWritten)
            throws IOException {
        if (!org.apache.myfaces.shared.renderkit.RendererUtils.isDefaultAttributeValue(value)) {
            if (!startElementWritten) {
                writer.startElement(elementName, component);
                startElementWritten = true;
            }
            renderHTMLAttribute(writer, attrName, attrName, value);
        }
        return startElementWritten;
    }

    public static boolean renderHTMLAttributesWithOptionalStartElement(
            ResponseWriter writer, UIComponent component, String elementName,
            String[] attributes) throws IOException {
        boolean startElementWritten = false;
        for (int i = 0, len = attributes.length; i < len; i++) {
            String attrName = attributes[i];
            Object value = component.getAttributes().get(attrName);
            if (!RendererUtils.isDefaultAttributeValue(value)) {
                if (!startElementWritten) {
                    writer.startElement(elementName, component);
                    startElementWritten = true;
                }
                renderHTMLAttribute(writer, attrName, attrName, value);
            }
        }
        return startElementWritten;
    }

    public static boolean renderOptionalEndElement(ResponseWriter writer,
                                                   UIComponent component, String elementName, String[] attributes)
            throws IOException {
        boolean endElementNeeded = false;
        for (int i = 0, len = attributes.length; i < len; i++) {
            String attrName = attributes[i];
            Object value = component.getAttributes().get(attrName);
            if (!RendererUtils.isDefaultAttributeValue(value)) {
                endElementNeeded = true;
                break;
            }
        }
        if (endElementNeeded) {
            writer.endElement(elementName);
            return true;
        }

        return false;
    }

    public static void writeIdIfNecessary(ResponseWriter writer, UIComponent component,
                                          FacesContext facesContext)
            throws IOException {
        if (component.getId() != null && !component.getId().startsWith(UIViewRoot.UNIQUE_ID_PREFIX)) {
            writer.writeAttribute(HTML.ID_ATTR, component.getClientId(facesContext), null);
        }
    }

    public static void writeIdAndNameIfNecessary(ResponseWriter writer, UIComponent component,
                                                 FacesContext facesContext)
            throws IOException {
        if (component.getId() != null && !component.getId().startsWith(UIViewRoot.UNIQUE_ID_PREFIX)) {
            String clientId = component.getClientId(facesContext);
            writer.writeAttribute(HTML.ID_ATTR, clientId, null);
            writer.writeAttribute(HTML.NAME_ATTR, clientId, null);
        }
    }

    public static void writeIdAndName(ResponseWriter writer, UIComponent component,
                                      FacesContext facesContext)
            throws IOException {
        String clientId = component.getClientId(facesContext);
        writer.writeAttribute(HTML.ID_ATTR, clientId, null);
        writer.writeAttribute(HTML.NAME_ATTR, clientId, null);
    }

    public static void renderDisplayValueOnlyForSelects(FacesContext facesContext,
                                                        UIComponent uiComponent) throws IOException {
        // invoke renderDisplayValueOnlyForSelects with considerValueType = false
        renderDisplayValueOnlyForSelects(facesContext, uiComponent, false);
    }

    public static void renderDisplayValueOnlyForSelects(FacesContext facesContext,
                                                        UIComponent uiComponent, boolean considerValueType)
            throws IOException {
        ResponseWriter writer = facesContext.getResponseWriter();

        List selectItemList = null;
        Converter converter = null;
        boolean isSelectOne = false;

        if (uiComponent instanceof UISelectBoolean) {
            converter = findUIOutputConverterFailSafe(facesContext, uiComponent);

            writer.startElement(HTML.SPAN_ELEM, uiComponent);
            writeIdIfNecessary(writer, uiComponent, facesContext);
            renderDisplayValueOnlyAttributes(uiComponent, writer);
            writer.writeText(RendererUtils.getConvertedStringValue(facesContext, uiComponent,
                    converter, ((UISelectBoolean) uiComponent).getValue()), JSFAttr.VALUE_ATTR);
            writer.endElement(HTML.SPAN_ELEM);

        } else {
            if (uiComponent instanceof UISelectMany) {
                isSelectOne = false;
                selectItemList = RendererUtils
                        .getSelectItemList((UISelectMany) uiComponent, facesContext);
                converter = findUISelectManyConverterFailsafe(facesContext, uiComponent, considerValueType);
            } else if (uiComponent instanceof UISelectOne) {
                isSelectOne = true;
                selectItemList = RendererUtils
                        .getSelectItemList((UISelectOne) uiComponent, facesContext);
                converter = findUIOutputConverterFailSafe(facesContext, uiComponent);
            }

            writer.startElement(isSelectOne ? HTML.SPAN_ELEM : HTML.UL_ELEM, uiComponent);
            writeIdIfNecessary(writer, uiComponent, facesContext);

            renderDisplayValueOnlyAttributes(uiComponent, writer);

            Set lookupSet = getSubmittedOrSelectedValuesAsSet(
                    uiComponent instanceof UISelectMany,
                    uiComponent, facesContext, converter);

            renderSelectOptionsAsText(facesContext, uiComponent, converter, lookupSet,
                    selectItemList, isSelectOne);

            // bug #970747: force separate end tag
            writer.writeText(STR_EMPTY, null);
            writer.endElement(isSelectOne ? HTML.SPAN_ELEM : HTML.UL_ELEM);
        }

    }

    public static void renderDisplayValueOnlyAttributes(UIComponent uiComponent, ResponseWriter writer) throws IOException {
        if (!(uiComponent instanceof org.apache.myfaces.shared.component.DisplayValueOnlyCapable)) {
            log.severe("Wrong type of uiComponent. needs DisplayValueOnlyCapable.");
            renderHTMLAttributes(writer, uiComponent,
                    HTML.COMMON_PASSTROUGH_ATTRIBUTES);

            return;
        }

        if (getDisplayValueOnlyStyle(uiComponent) != null || getDisplayValueOnlyStyleClass(uiComponent) != null) {
            if (getDisplayValueOnlyStyle(uiComponent) != null) {
                writer.writeAttribute(HTML.STYLE_ATTR, getDisplayValueOnlyStyle(uiComponent), null);
            } else if (uiComponent.getAttributes().get("style") != null) {
                writer.writeAttribute(HTML.STYLE_ATTR, uiComponent.getAttributes().get("style"), null);
            }

            if (getDisplayValueOnlyStyleClass(uiComponent) != null) {
                writer.writeAttribute(HTML.CLASS_ATTR, getDisplayValueOnlyStyleClass(uiComponent), null);
            } else if (uiComponent.getAttributes().get("styleClass") != null) {
                writer.writeAttribute(HTML.CLASS_ATTR, uiComponent.getAttributes().get("styleClass"), null);
            }

            renderHTMLAttributes(writer, uiComponent,
                    HTML.COMMON_PASSTROUGH_ATTRIBUTES_WITHOUT_STYLE);
        } else {
            renderHTMLAttributes(writer, uiComponent,
                    HTML.COMMON_PASSTROUGH_ATTRIBUTES);
        }
    }

    private static void renderSelectOptionsAsText(FacesContext context,
                                                  UIComponent component, Converter converter, Set lookupSet,
                                                  List selectItemList, boolean isSelectOne) throws IOException {
        ResponseWriter writer = context.getResponseWriter();

        for (Iterator it = selectItemList.iterator(); it.hasNext();) {
            SelectItem selectItem = (SelectItem) it.next();

            if (selectItem instanceof SelectItemGroup) {
                SelectItem[] selectItems = ((SelectItemGroup) selectItem)
                        .getSelectItems();
                renderSelectOptionsAsText(context, component, converter, lookupSet,
                        Arrays.asList(selectItems), isSelectOne);
            } else {
                String itemStrValue = RendererUtils.getConvertedStringValue(context, component,
                        converter, selectItem);

                if (lookupSet.contains(itemStrValue)) {  //TODO/FIX: we always compare the String vales, better fill lookupSet with Strings only when useSubmittedValue==true, else use the real item value Objects

                    if (!isSelectOne)
                        writer.startElement(HTML.LI_ELEM, component);
                    writer.writeText(selectItem.getLabel(), null);
                    if (!isSelectOne)
                        writer.endElement(HTML.LI_ELEM);

                    if (isSelectOne) {
                        //take care of several choices with the same value; use only the first one
                        return;
                    }
                }
            }
        }
    }

    public static void renderTableCaption(FacesContext context,
                                          ResponseWriter writer,
                                          UIComponent component)
            throws IOException {
        UIComponent captionFacet = component.getFacet("caption");
        if (captionFacet == null) return;

        String captionClass;
        String captionStyle;

        if (component instanceof HtmlPanelGrid) {
            HtmlPanelGrid panelGrid = (HtmlPanelGrid) component;
            captionClass = panelGrid.getCaptionClass();
            captionStyle = panelGrid.getCaptionStyle();
        } else if (component instanceof HtmlDataTable) {
            HtmlDataTable dataTable = (HtmlDataTable) component;
            captionClass = dataTable.getCaptionClass();
            captionStyle = dataTable.getCaptionStyle();
        } else {
            captionClass = (String) component.getAttributes().get(org.apache.myfaces.shared.renderkit.JSFAttr.CAPTION_CLASS_ATTR);
            captionStyle = (String) component.getAttributes().get(org.apache.myfaces.shared.renderkit.JSFAttr.CAPTION_STYLE_ATTR);
        }

        HtmlRendererUtils.writePrettyLineSeparator(context);
        writer.startElement(HTML.CAPTION_ELEM, component);

        if (captionClass != null) {
            writer.writeAttribute(HTML.CLASS_ATTR, captionClass, null);
        }

        if (captionStyle != null) {
            writer.writeAttribute(HTML.STYLE_ATTR, captionStyle, null);
        }

        RendererUtils.renderChild(context, captionFacet);

        writer.endElement(HTML.CAPTION_ELEM);
    }

    public static String getDisplayValueOnlyStyleClass(UIComponent component) {

        if (component instanceof org.apache.myfaces.shared.component.DisplayValueOnlyCapable) {
            if (((org.apache.myfaces.shared.component.DisplayValueOnlyCapable) component).getDisplayValueOnlyStyleClass() != null)
                return ((org.apache.myfaces.shared.component.DisplayValueOnlyCapable) component).getDisplayValueOnlyStyleClass();

            UIComponent parent = component;

            while ((parent = parent.getParent()) != null) {
                if (parent instanceof org.apache.myfaces.shared.component.DisplayValueOnlyCapable &&
                        ((org.apache.myfaces.shared.component.DisplayValueOnlyCapable) parent).getDisplayValueOnlyStyleClass() != null) {
                    return ((org.apache.myfaces.shared.component.DisplayValueOnlyCapable) parent).getDisplayValueOnlyStyleClass();
                }
            }
        }

        return null;
    }

    public static String getDisplayValueOnlyStyle(UIComponent component) {

        if (component instanceof DisplayValueOnlyCapable) {
            if (((org.apache.myfaces.shared.component.DisplayValueOnlyCapable) component).getDisplayValueOnlyStyle() != null)
                return ((org.apache.myfaces.shared.component.DisplayValueOnlyCapable) component).getDisplayValueOnlyStyle();

            UIComponent parent = component;

            while ((parent = parent.getParent()) != null) {
                if (parent instanceof org.apache.myfaces.shared.component.DisplayValueOnlyCapable &&
                        ((DisplayValueOnlyCapable) parent).getDisplayValueOnlyStyle() != null) {
                    return ((DisplayValueOnlyCapable) parent).getDisplayValueOnlyStyle();
                }
            }
        }

        return null;
    }

    public static boolean isDisplayValueOnly(UIComponent component) {

        if (component instanceof DisplayValueOnlyCapable) {
            if (((DisplayValueOnlyCapable) component).isSetDisplayValueOnly())
                return ((org.apache.myfaces.shared.component.DisplayValueOnlyCapable) component).isDisplayValueOnly();

            UIComponent parent = component;

            while ((parent = parent.getParent()) != null) {
                if (parent instanceof DisplayValueOnlyCapable &&
                        ((DisplayValueOnlyCapable) parent).isSetDisplayValueOnly()) {
                    return ((org.apache.myfaces.shared.component.DisplayValueOnlyCapable) parent).isDisplayValueOnly();
                }
            }
        }

        return false;
    }

    public static void renderDisplayValueOnly(FacesContext facesContext, UIInput input) throws IOException {
        ResponseWriter writer = facesContext.getResponseWriter();
        writer.startElement(org.apache.myfaces.shared.renderkit.html.HTML.SPAN_ELEM, input);

        writeIdIfNecessary(writer, input, facesContext);

        renderDisplayValueOnlyAttributes(input, writer);

        String strValue = RendererUtils.getStringValue(facesContext, input);
        writer.write(HTMLEncoder.encode(strValue, true, true));

        writer.endElement(HTML.SPAN_ELEM);
    }

    public static void appendClearHiddenCommandFormParamsFunctionCall(StringBuffer buf, String formName) {
        appendClearHiddenCommandFormParamsFunctionCall(new ScriptContext(buf, false), formName);
    }

    private static void appendClearHiddenCommandFormParamsFunctionCall(ScriptContext context, String formName) {

        String functionName = HtmlRendererUtils.getClearHiddenCommandFormParamsFunctionName(formName);

        if (formName == null) {
            context.prettyLine();
            context.append("var clearFn = ");
            context.append(functionName);
            context.append(";");
            context.prettyLine();
            context.append("if(typeof window[clearFn] =='function')");
            context.append("{");
            context.append("window[clearFn](formName);");
            context.append("}");
        } else {
            context.prettyLine();
            context.append("if(typeof window.");
            context.append(functionName);
            context.append("=='function')");
            context.append("{");
            context.append(functionName).append("('").append(formName).append("');");
            context.append("}");
        }
    }

    @SuppressWarnings("unchecked")
    public static void renderFormSubmitScript(FacesContext facesContext)
            throws IOException {

        Map map = facesContext.getExternalContext().getRequestMap();
        Boolean firstScript = (Boolean) map.get(FIRST_SUBMIT_SCRIPT_ON_PAGE);

        if (firstScript == null || firstScript.equals(Boolean.TRUE)) {
            map.put(FIRST_SUBMIT_SCRIPT_ON_PAGE, Boolean.FALSE);
            HtmlRendererUtils.renderFormSubmitScriptIfNecessary(facesContext);

            //we have to render the config just in case
            renderConfigOptionsIfNecessary(facesContext);
        }
    }

    private static void renderConfigOptionsIfNecessary(FacesContext facesContext) throws IOException {
        ResponseWriter writer = facesContext.getResponseWriter();
        MyfacesConfig config = MyfacesConfig.getCurrentInstance(facesContext.getExternalContext());
        ScriptContext script = new ScriptContext(config.isPrettyHtml());
        boolean autoScroll = config.isAutoScroll();
        boolean autoSave = JavascriptUtils.isSaveFormSubmitLinkIE(facesContext.getExternalContext());

        if (autoScroll || autoSave) {
            script.prettyLine();
            script.increaseIndent();
            script.append("(!window.myfaces) ? window.myfaces = {} : null;");
            script.append("(!myfaces.core) ? myfaces.core = {} : null;");
            script.append("(!myfaces.core.config) ? myfaces.core.config = {} : null;");
        }

        if (autoScroll) {
            script.append("myfaces.core.config.autoScroll = true;");
        }
        if (autoSave) {
            script.append("myfaces.core.config.ieAutoSave = true;");
        }
        if (autoScroll || autoSave) {
            writer.startElement(HTML.SCRIPT_ELEM, null);
            writer.writeAttribute(HTML.TYPE_ATTR, "text/javascript", null);
            writer.writeText(script.toString(), null);
            writer.endElement(HTML.SCRIPT_ELEM);
        }
    }

    /**
     * @param facesContext
     * @throws IOException
     */
    private static void renderFormSubmitScriptIfNecessary(FacesContext facesContext) throws IOException {
        final ExternalContext externalContext = facesContext.getExternalContext();
        final MyfacesConfig currentInstance = MyfacesConfig.getCurrentInstance(externalContext);
        ResponseWriter writer = facesContext.getResponseWriter();

        if (currentInstance.isRenderFormSubmitScriptInline())
        {
            writer.startElement(HTML.SCRIPT_ELEM, null);
            writer.writeAttribute(HTML.TYPE_ATTR, "text/javascript", null);
    
            boolean autoScroll = currentInstance.isAutoScroll();
    
            ScriptContext context = new ScriptContext(currentInstance
                    .isPrettyHtml());
            context.prettyLine();
            context.increaseIndent();
    
            prepareScript(facesContext, context, autoScroll);
    
            writer.writeText(context.toString(), null);
    
            writer.endElement(HTML.SCRIPT_ELEM);
        }
        else
        {
            ResourceUtils.renderMyfacesJSInlineIfNecessary(facesContext, writer);
        }
    }

    /**
     * @param facesContext
     * @param context
     * @param autoScroll
     */
    private static void prepareScript(FacesContext facesContext, ScriptContext context, boolean autoScroll) {

        final char separatorChar = UINamingContainer.getSeparatorChar(facesContext);
        context.prettyLine();

        //render a function to create a hidden input, if it doesn't exist
        context.append("function ");
        context.append(SET_HIDDEN_INPUT_FN_NAME).append("(formname, name, value)");
        context.append("{");
        context.append("var form = document.forms[formname];");
        context.prettyLine();
        context.append("if (typeof form == 'undefined')");
        context.append("{");
        context.append("form = document.getElementById(formname);");
        context.append("}");
        context.prettyLine();
        context.append("if(typeof form.elements[name]!='undefined' && (form.elements[name].nodeName=='INPUT' || form.elements[name].nodeName=='input'))");
        context.append("{");
        context.append("form.elements[name].value=value;");
        context.append("}");
        context.append("else");
        context.append("{");
        context.append("var newInput = document.createElement('input');");
        context.prettyLine();
        context.append("newInput.setAttribute('type','hidden');");
        context.prettyLine();
        context.append("newInput.setAttribute('id',name);");  // IE hack; See MYFACES-1805
        context.prettyLine();
        context.append("newInput.setAttribute('name',name);");
        context.prettyLine();
        context.append("newInput.setAttribute('value',value);");
        context.prettyLine();
        context.append("form.appendChild(newInput);");
        context.append("}");

        context.append("}");

        context.prettyLine();

        context.prettyLine();

        //render a function to clear a hidden input, if it exists        
        context.append("function ");
        context.append(CLEAR_HIDDEN_INPUT_FN_NAME).append("(formname, name, value)");
        context.append("{");
        context.append("var form = document.forms[formname];");
        context.prettyLine();
        context.append("if (typeof form == 'undefined')");
        context.append("{");
        context.append("form = document.getElementById(formname);");
        context.append("}");
        context.prettyLine();
        context.append("var hInput = form.elements[name];");
        context.prettyLine();
        context.append("if(typeof hInput !='undefined')");
        context.append("{");
        //context.append("form.elements[name].value=null;");
        context.append("form.removeChild(hInput);");
        context.append("}");

        context.append("}");

        context.prettyLine();

        context.append("function ");
        context.append(SUBMIT_FORM_FN_NAME).append("(formName, linkId, target, params)");
        context.append("{");

        //call the script to clear the form (clearFormHiddenParams_<formName>) method - optionally, only necessary for IE5.5.
        //todo: if IE5.5. is ever desupported, we can get rid of this and instead rely on the last part of this script to
        //clear the parameters
        HtmlRendererUtils.appendClearHiddenCommandFormParamsFunctionCall(context, null);

        if (autoScroll) {
            appendAutoScrollAssignment(facesContext, context, null);
        }

        context.prettyLine();

        context.append("var form = document.forms[formName];");
        context.prettyLine();
        context.append("if (typeof form == 'undefined')");
        context.append("{");
        context.append("form = document.getElementById(formName);");
        context.append("}");
        context.prettyLine();

        if (JavascriptUtils.isSaveFormSubmitLinkIE(FacesContext.getCurrentInstance().getExternalContext())) {
            context.append("var agentString = navigator.userAgent.toLowerCase();");
            context.prettyLine();
            //context.append("var isIE = false;");
            context.prettyLine();
            context.append("if (agentString.indexOf('msie') != -1)");

            context.append("{");
            context.append("if (!(agentString.indexOf('ppc') != -1 && agentString.indexOf('windows ce') != -1 && version >= 4.0))");
            context.append("{");
            context.append("window.external.AutoCompleteSaveForm(form);");
//        context.append("isIE = false;");
            context.append("}");
//        context.append("else");
//        context.append("{");
//        context.append("isIE = true;");
//        context.prettyLine();
//        context.append("}");

            context.append("}");

            context.prettyLine();
        }
        //set the target (and save it). This should be done always, 
        //and the default value of target is always valid.
        context.append("var oldTarget = form.target;");
        context.prettyLine();
        context.append("if(target != null)");
        context.append("{");
        context.prettyLine();
        context.append("form.target=target;");
        context.append("}");

        //set the submit parameters

        context.append("if((typeof params!='undefined') && params != null)");
        context.append("{");
        context.prettyLine();
        context.append("for(var i=0, param; (param = params[i]); i++)");
        context.append("{");
        context.append(SET_HIDDEN_INPUT_FN_NAME).append("(formName,param[0], param[1]);");
        context.append("}");
        context.append("}");

        context.prettyLine();

        context.append(SET_HIDDEN_INPUT_FN_NAME);
        context.append("(formName,formName +'" + separatorChar +
                "'+'" + HtmlRendererUtils.HIDDEN_COMMANDLINK_FIELD_NAME + "',linkId);");

        context.prettyLine();
        context.prettyLine();

        //do the actual submit calls

        context.append("if(form.onsubmit)");
        context.append("{");
        context.append("var result=form.onsubmit();");
        context.prettyLine();
        context.append("if((typeof result=='undefined')||result)");
        context.append("{");
        context.append("try");
        context.append("{");
        context.append("form.submit();");
        context.append("}");
        context.append("catch(e){}");
        context.append("}");
        context.append("}");
        context.append("else ");
        context.append("{");
        context.append("try");
        context.append("{");
        context.append("form.submit();");
        context.append("}");
        context.append("catch(e){}");
        context.append("}");

        //reset the target
        context.prettyLine();
        //Restore the old target, no more questions asked
        context.append("form.target=oldTarget;");
        context.prettyLine();

        //clear the individual parameters - to make sure that even if the clear-function isn't called,
        // the back button/resubmit functionality will still work in all browsers except IE 5.5.

        context.append("if((typeof params!='undefined') && params != null)");
        context.append("{");
        context.prettyLine();
        context.append("for(var i=0, param; (param = params[i]); i++)");
        context.append("{");
        context.append(CLEAR_HIDDEN_INPUT_FN_NAME).append("(formName,param[0], param[1]);");
        context.append("}");
        context.append("}");

        context.prettyLine();

        context.append(CLEAR_HIDDEN_INPUT_FN_NAME);
        context.append("(formName,formName +'" + separatorChar +
                "'+'" + HtmlRendererUtils.HIDDEN_COMMANDLINK_FIELD_NAME + "',linkId);");


        //return false, so that browser does not handle the click
        context.append("return false;");
        context.append("}");

        context.prettyLineDecreaseIndent();
    }

    /**
     * Adds the hidden form input value assignment that is necessary for the autoscroll
     * feature to an html link or button onclick attribute.
     */
    public static void appendAutoScrollAssignment(StringBuffer onClickValue, String formName) {
        appendAutoScrollAssignment(FacesContext.getCurrentInstance(), new ScriptContext(onClickValue, false), formName);
    }
    
    /**
     * Adds the hidden form input value assignment that is necessary for the autoscroll
     * feature to an html link or button onclick attribute.
     */
    public static void appendAutoScrollAssignment(FacesContext context, StringBuffer onClickValue, String formName) {
        appendAutoScrollAssignment(context, new ScriptContext(onClickValue, false), formName);
    }    

    private static void appendAutoScrollAssignment(FacesContext context, ScriptContext scriptContext, String formName) {
        String formNameStr = formName == null ? "formName" : (new StringBuffer("'").append(formName).append("'").toString());
        String paramName = new StringBuffer().append("'").
                append(AUTO_SCROLL_PARAM).append("'").toString();
        String value = new StringBuffer().append(AUTO_SCROLL_FUNCTION).append("()").toString();

        scriptContext.prettyLine();
        scriptContext.append("if(typeof window." + AUTO_SCROLL_FUNCTION + "!='undefined')");
        scriptContext.append("{");
        if (MyfacesConfig.getCurrentInstance(context.getExternalContext()).isRenderFormSubmitScriptInline())
        {
            scriptContext.append(SET_HIDDEN_INPUT_FN_NAME);
        }
        else
        {
            scriptContext.append(SET_HIDDEN_INPUT_FN_NAME_JSF2);
        }
        scriptContext.append("(").append(formNameStr).append(",").append(paramName).append(",").append(value).append(");");
        scriptContext.append("}");

    }

    /**
     * Renders the hidden form input that is necessary for the autoscroll feature.
     */
    public static void renderAutoScrollHiddenInput(FacesContext facesContext, ResponseWriter writer) throws IOException {
        writePrettyLineSeparator(facesContext);
        writer.startElement(HTML.INPUT_ELEM, null);
        writer.writeAttribute(HTML.TYPE_ATTR, "hidden", null);
        writer.writeAttribute(HTML.NAME_ATTR, AUTO_SCROLL_PARAM, null);
        writer.endElement(HTML.INPUT_ELEM);
        writePrettyLineSeparator(facesContext);
    }

    /**
     * Renders the autoscroll javascript function.
     */
    public static void renderAutoScrollFunction(FacesContext facesContext,
                                                ResponseWriter writer) throws IOException {
        writePrettyLineSeparator(facesContext);
        writer.startElement(HTML.SCRIPT_ELEM, null);
        writer.writeAttribute(HTML.SCRIPT_TYPE_ATTR, HTML.SCRIPT_TYPE_TEXT_JAVASCRIPT, null);
        writer.writeText(getAutoScrollFunction(facesContext), null);
        writer.endElement(HTML.SCRIPT_ELEM);
        writePrettyLineSeparator(facesContext);
    }

    public static String getAutoScrollFunction(FacesContext facesContext) {
        ScriptContext script = new ScriptContext(
                MyfacesConfig.getCurrentInstance(facesContext.getExternalContext()).isPrettyHtml());

        script.prettyLineIncreaseIndent();

        script.append("function ");
        script.append(AUTO_SCROLL_FUNCTION);
        script.append("()");
        script.append("{");
        script.append("var x = 0; var y = 0;");
        script.append("if (self.pageXOffset || self.pageYOffset)");
        script.append("{");
        script.append("x = self.pageXOffset;");
        script.prettyLine();
        script.append("y = self.pageYOffset;");
        script.append("}");
        script.append(" else if ((document.documentElement && document.documentElement.scrollLeft)||(document.documentElement && document.documentElement.scrollTop))");
        script.append("{");
        script.append("x = document.documentElement.scrollLeft;");
        script.prettyLine();
        script.append("y = document.documentElement.scrollTop;");
        script.append("}");
        script.append(" else if (document.body) ");
        script.append("{");
        script.append("x = document.body.scrollLeft;");
        script.prettyLine();
        script.append("y = document.body.scrollTop;");
        script.append("}");
        script.append("return x + \",\" + y;");
        script.append("}");

        ExternalContext externalContext = facesContext.getExternalContext();
        String oldViewId = JavascriptUtils.getOldViewId(externalContext);
        if (oldViewId != null && oldViewId.equals(facesContext.getViewRoot().getViewId())) {
            //ok, we stayed on the same page, so let's scroll it to the former place
            String scrolling = (String) externalContext.getRequestParameterMap().get(AUTO_SCROLL_PARAM);
            if (scrolling != null && scrolling.length() > 0) {
                int x = 0;
                int y = 0;
                int comma = scrolling.indexOf(',');
                if (comma == -1) {
                    log.warning("Illegal autoscroll request parameter: " + scrolling);
                } else {
                    try {
                        //we convert to int against XSS vulnerability
                        x = Integer.parseInt(scrolling.substring(0, comma));
                    } catch (NumberFormatException e) {
                        log.warning("Error getting x offset for autoscroll feature. Bad param value: " + scrolling);
                        x = 0; //ignore false numbers
                    }

                    try {
                        //we convert to int against XSS vulnerability
                        y = Integer.parseInt(scrolling.substring(comma + 1));
                    } catch (NumberFormatException e) {
                        log.warning("Error getting y offset for autoscroll feature. Bad param value: " + scrolling);
                        y = 0; //ignore false numbers
                    }
                }
                script.append("window.scrollTo(").append(x).append(",").append(y).append(");\n");
            }
        }

        return script.toString();
    }

    public static boolean isAllowedCdataSection(FacesContext fc) {
        Boolean value = null;

        if (fc != null) {
            value = (Boolean) fc.getExternalContext().getRequestMap().get(ALLOW_CDATA_SECTION_ON);
        }

        return value != null && ((Boolean) value).booleanValue();
    }

    public static void allowCdataSection(FacesContext fc, boolean cdataSectionAllowed) {
        fc.getExternalContext().getRequestMap().put(ALLOW_CDATA_SECTION_ON, Boolean.valueOf(cdataSectionAllowed));
    }

    public static class LinkParameter {
        private String _name;

        private Object _value;

        public String getName() {
            return _name;
        }

        public void setName(String name) {
            _name = name;
        }

        public Object getValue() {
            return _value;
        }

        public void setValue(Object value) {
            _value = value;
        }

    }

    public static void renderHiddenCommandFormParams(ResponseWriter writer,
                                                     Set dummyFormParams) throws IOException {
        for (Iterator it = dummyFormParams.iterator(); it.hasNext();) {
            Object name = it.next();
            renderHiddenInputField(writer, name, null);
        }
    }

    public static void renderHiddenInputField(ResponseWriter writer, Object name, Object value)
            throws IOException {
        writer.startElement(HTML.INPUT_ELEM, null);
        writer.writeAttribute(HTML.TYPE_ATTR, HTML.INPUT_TYPE_HIDDEN, null);
        writer.writeAttribute(HTML.NAME_ATTR, name, null);
        if (value != null) {
            writer.writeAttribute(HTML.VALUE_ATTR, value, null);
        }
        writer.endElement(HTML.INPUT_ELEM);
    }

    /**
     * @deprecated Replaced by
     *             renderLabel(ResponseWriter writer,
     *             UIComponent component,
     *             String forClientId,
     *             SelectItem item,
     *             boolean disabled).
     *             Renders a label HTML element
     */
    @Deprecated
    public static void renderLabel(ResponseWriter writer,
                                   UIComponent component,
                                   String forClientId,
                                   String labelValue,
                                   boolean disabled) throws IOException {
        writer.startElement(HTML.LABEL_ELEM, component);
        writer.writeAttribute(HTML.FOR_ATTR, forClientId, null);

        String labelClass = null;

        if (disabled) {
            labelClass = (String) component.getAttributes().get(JSFAttr.DISABLED_CLASS_ATTR);
        } else {
            labelClass = (String) component.getAttributes().get(org.apache.myfaces.shared.renderkit.JSFAttr.ENABLED_CLASS_ATTR);
        }
        if (labelClass != null) {
            writer.writeAttribute("class", labelClass, "labelClass");
        }

        if ((labelValue != null) && (labelValue.length() > 0)) {
            writer.write(HTML.NBSP_ENTITY);
            writer.writeText(labelValue, null);
        }

        writer.endElement(HTML.LABEL_ELEM);
    }

    /**
     * Renders a label HTML element
     */
    public static void renderLabel(ResponseWriter writer,
                                   UIComponent component,
                                   String forClientId,
                                   SelectItem item,
                                   boolean disabled) throws IOException {
        writer.startElement(HTML.LABEL_ELEM, component);
        writer.writeAttribute(HTML.FOR_ATTR, forClientId, null);

        String labelClass = null;

        if (disabled) {
            labelClass = (String) component.getAttributes().get(JSFAttr.DISABLED_CLASS_ATTR);
        } else {
            labelClass = (String) component.getAttributes().get(org.apache.myfaces.shared.renderkit.JSFAttr.ENABLED_CLASS_ATTR);
        }
        if (labelClass != null) {
            writer.writeAttribute("class", labelClass, "labelClass");
        }

        if ((item.getLabel() != null) && (item.getLabel().length() > 0)) {
            // writer.write(HTML.NBSP_ENTITY);
            writer.write(" ");
            if (item.isEscape()) {
                //writer.write(item.getLabel());
                writer.writeText(item.getLabel(), null);
            } else {
                //writer.write(HTMLEncoder.encode (item.getLabel()));
                writer.write(item.getLabel());
            }
        }

        writer.endElement(HTML.LABEL_ELEM);
    }

    /**
     * Renders a label HTML element
     */
    public static void renderLabel(ResponseWriter writer,
                                   UIComponent component,
                                   String forClientId,
                                   SelectItem item,
                                   boolean disabled,
                                   boolean selected) throws IOException {
        writer.startElement(HTML.LABEL_ELEM, component);
        writer.writeAttribute(HTML.FOR_ATTR, forClientId, null);

        String labelClass = null;

        if (disabled) {
            labelClass = (String) component.getAttributes().get(JSFAttr.DISABLED_CLASS_ATTR);
        } else {
            labelClass = (String) component.getAttributes().get(org.apache.myfaces.shared.renderkit.JSFAttr.ENABLED_CLASS_ATTR);
        }

        String labelSelectedClass = null;

        if (selected) {
            labelSelectedClass = (String) component.getAttributes().get(JSFAttr.SELECTED_CLASS_ATTR);
        } else {
            labelSelectedClass = (String) component.getAttributes().get(JSFAttr.UNSELECTED_CLASS_ATTR);
        }

        if (labelSelectedClass != null) {
            if (labelClass == null) {
                labelClass = labelSelectedClass;
            } else {
                labelClass = labelClass + " " + labelSelectedClass;
            }
        }

        if (labelClass != null) {
            writer.writeAttribute("class", labelClass, "labelClass");
        }

        if ((item.getLabel() != null) && (item.getLabel().length() > 0)) {
            writer.write(HTML.NBSP_ENTITY);
            if (item.isEscape()) {
                //writer.write(item.getLabel());
                writer.writeText(item.getLabel(), null);
            } else {
                //writer.write(HTMLEncoder.encode (item.getLabel()));
                writer.write(item.getLabel());
            }
        }

        writer.endElement(HTML.LABEL_ELEM);
    }

    /**
     * Render the javascript function that is called on a click on a commandLink
     * to clear the hidden inputs. This is necessary because on a browser back,
     * each hidden input still has it's old value (browser cache!) and therefore
     * a new submit would cause the according action once more!
     *
     * @param writer
     * @param formName
     * @param dummyFormParams
     * @param formTarget
     * @throws IOException
     */
    public static void renderClearHiddenCommandFormParamsFunction(
            ResponseWriter writer, String formName, Set dummyFormParams,
            String formTarget) throws IOException {
        //render the clear hidden inputs javascript function
        String functionName = getClearHiddenCommandFormParamsFunctionName(formName);
        writer.startElement(HTML.SCRIPT_ELEM, null);
        writer.writeAttribute(HTML.TYPE_ATTR, "text/javascript", null);

        // Using writeComment instead of write with <!-- tag
        StringBuffer script = new StringBuffer();
        script.append("function ");
        script.append(functionName);
        script.append("() {");
        if (dummyFormParams != null) {
            script.append("\n  var f = document.forms['");
            script.append(formName);
            script.append("'];");
            int i = 0;
            for (Iterator it = dummyFormParams.iterator(); it.hasNext();) {
                String elemVarName = "elem" + i;
                script.append("\n  var ").append(elemVarName).append(" = ");
                script.append("f.elements['").append((String) it.next()).append("'];");
                script.append("\n  if(typeof ").append(elemVarName).append(" !='undefined' && ");
                script.append(elemVarName).append(".nodeName=='INPUT'){");
                script.append("\n   if (").append(elemVarName).append(".value != '') {");
                script.append("\n    " + elemVarName + ".value='';");
                script.append("\n   }");
                script.append("\n  }");
                i++;
            }
        }
        // clear form target
        script.append("\n  f.target=");
        if (formTarget == null || formTarget.length() == 0) {
            //Normally one would think that setting target to null has the
            //desired effect, but once again IE is different...
            //Setting target to null causes IE to open a new window!
            script.append("'';");
        } else {
            script.append("'");
            script.append(formTarget);
            script.append("';");
        }
        script.append("\n}");

        //Just to be sure we call this clear method on each load.
        //Otherwise in the case, that someone submits a form by pressing Enter
        //within a text input, the hidden inputs won't be cleared!
        script.append("\n");
        script.append(functionName);
        script.append("();");

        writer.writeText(script.toString(), null);
        writer.endElement(HTML.SCRIPT_ELEM);
    }

    /**
     * Prefixes the given String with "clear_" and removes special characters
     *
     * @param formName
     * @return String
     */
    public static String getClearHiddenCommandFormParamsFunctionName(
            String formName) {
        final char separatorChar = UINamingContainer.getSeparatorChar(FacesContext.getCurrentInstance());
        if (formName == null) {
            return "'" + CLEAR_HIDDEN_FIELD_FN_NAME
                    + "_'+formName.replace(/-/g, '\\$" + separatorChar + "').replace(/" + separatorChar + "/g,'_')";
        }

        return JavascriptUtils.getValidJavascriptNameAsInRI(CLEAR_HIDDEN_FIELD_FN_NAME
                + "_"
                + formName
                .replace(separatorChar, '_'));
    }

    public static String getClearHiddenCommandFormParamsFunctionNameMyfacesLegacy(
            String formName) {
        return "clear_"
                + JavascriptUtils.getValidJavascriptName(formName, false);
    }


    /**
     * Get the name of the request parameter that holds the id of the
     * link-type component that caused the form to be submitted.
     * <p/>
     * Within each page there may be multiple "link" type components that
     * cause page submission. On the server it is necessary to know which
     * of these actually caused the submit, in order to invoke the correct
     * listeners. Such components therefore store their id into the
     * "hidden command link field" in their associated form before
     * submitting it.
     * <p/>
     * The field is always a direct child of each form, and has the same
     * <i>name</i> in each form. The id of the form component is therefore
     * both necessary and sufficient to determine the full name of the
     * field.
     */
    public static String getHiddenCommandLinkFieldName(FormInfo formInfo) {
        if (RendererUtils.isAdfOrTrinidadForm(formInfo.getForm())) {
            return HIDDEN_COMMANDLINK_FIELD_NAME_TRINIDAD;
        }
        return formInfo.getFormName() + UINamingContainer.getSeparatorChar(FacesContext.getCurrentInstance())
                + HIDDEN_COMMANDLINK_FIELD_NAME;
    }

    public static boolean isPartialOrBehaviorSubmit(FacesContext facesContext, String clientId) {
        Map<String, String> params = facesContext.getExternalContext().getRequestParameterMap();

        String sourceId = params.get("javax.faces.source");
        if (sourceId == null || !sourceId.equals(clientId)) {
            return false;
        }
        String behaviorEvent = params.get("javax.faces.behavior.event");
        if (behaviorEvent != null) {
            return ClientBehaviorEvents.ACTION.equals(behaviorEvent);
        }
        String partialEvent = params.get("javax.faces.partial.event");
        if (partialEvent != null) {
            return ClientBehaviorEvents.CLICK.equals(partialEvent);
        }
        return false;
    }

    /**
     * @param formInfo
     * @return
     * @deprecated Use getHiddenCommandLinkFieldNameMyfaces(FormInfo) instead
     */
    public static String getHiddenCommandLinkFieldNameMyfacesOld(FormInfo formInfo) {
        return formInfo.getFormName() + UINamingContainer.getSeparatorChar(FacesContext.getCurrentInstance())
                + HIDDEN_COMMANDLINK_FIELD_NAME_MYFACES_OLD;
    }

    public static String getOutcomeTargetHref(
            FacesContext facesContext, UIOutcomeTarget component) throws IOException {
        String outcome = component.getOutcome();
        outcome = (outcome == null) ? facesContext.getViewRoot().getViewId() : outcome;
        outcome = ((outcome == null) ? STR_EMPTY : outcome.trim());

        // Get the correct URL for the outcome.
        NavigationHandler nh = facesContext.getApplication().getNavigationHandler();
        if (!(nh instanceof ConfigurableNavigationHandler)) {
            throw new FacesException("Navigation handler must be an instance of " +
                    "ConfigurableNavigationHandler for using h:link or h:button");
        }
        ConfigurableNavigationHandler navigationHandler = (ConfigurableNavigationHandler) nh;
        // fromAction is null because there is no action method that was called to get the outcome
        NavigationCase navigationCase = navigationHandler.getNavigationCase(facesContext, null, outcome);

        // when navigation case is null, force the link or button to be disabled and log a warning
        if (navigationCase == null)
        {
            // log a warning
            log.warning("Could not determine NavigationCase for UIOutcomeTarget component "
                    + RendererUtils.getPathToComponent(component));

            return null;
        }

        Map<String, List<String>> parameters = null;
        // handle URL parameters
        if (component.getChildCount() > 0)
        {
            parameters = new HashMap<String, List<String>>();
            List<UIParameter> validParams = getValidUIParameterChildren(
                    facesContext, component.getChildren(), true, false);
            for (UIParameter param : validParams) {
                String name = param.getName();
                Object value = param.getValue();
                if (parameters.containsKey(name)) {
                    parameters.get(name).add(value.toString());
                } else {
                    List<String> list = new ArrayList<String>(1);
                    list.add(value.toString());
                    parameters.put(name, list);
                }
            }
        }

        // handle NavigationCase parameters
        Map<String, List<String>> navigationCaseParams = navigationCase.getParameters();
        if (navigationCaseParams != null) {
            if (parameters == null)
            {
                parameters = new HashMap<String, List<String>>(); 
            }
            parameters.putAll(navigationCaseParams);
        }
        
        if (parameters == null)
        {
            parameters = Collections.emptyMap();
        }

        // In theory the precedence order to deal with params is this:
        // component parameters, navigation-case parameters, view parameters
        // getBookmarkableURL deal with this details.
        ViewHandler viewHandler = facesContext.getApplication().getViewHandler();
        String href = viewHandler.getBookmarkableURL(facesContext,
                navigationCase.getToViewId(facesContext), parameters,
                navigationCase.isIncludeViewParams() || component.isIncludeViewParams());

        // handle fragment (viewId#fragment)
        String fragment = (String) component.getAttributes().get("fragment");
        if (fragment != null) {
            fragment = fragment.trim();

            if (fragment.length() > 0) {
                href += "#" + fragment;
            }
        }

        return href;
    }

    private static String HTML_CONTENT_TYPE = "text/html";
    private static String TEXT_ANY_CONTENT_TYPE = "text/*";
    private static String ANY_CONTENT_TYPE = "*/*";

    public static String DEFAULT_CHAR_ENCODING = "ISO-8859-1";
    private static String XHTML_CONTENT_TYPE = "application/xhtml+xml";
    private static String APPLICATION_XML_CONTENT_TYPE = "application/xml";
    private static String TEXT_XML_CONTENT_TYPE = "text/xml";

    // The order is important in this case.
    private static final String[] SUPPORTED_CONTENT_TYPES = {
            HTML_CONTENT_TYPE, //Prefer this over any other, because IE does not support XHTML content type
            XHTML_CONTENT_TYPE,
            APPLICATION_XML_CONTENT_TYPE,
            TEXT_XML_CONTENT_TYPE,
            TEXT_ANY_CONTENT_TYPE,
            ANY_CONTENT_TYPE
    };

    public static String selectContentType(String contentTypeListString) {
        if (contentTypeListString == null) {
            FacesContext context = FacesContext.getCurrentInstance();
            if (context != null) {
                contentTypeListString = (String)
                        context.getExternalContext().getRequestHeaderMap().get("Accept");

                // There is a windows mobile IE client (6.12) sending
                // "application/vnd.wap.mms-message;*/*"
                // Note that the Accept header should be written as 
                // "application/vnd.wap.mms-message,*/*" ,
                // so this is bug of the client. Anyway, this is a workaround ...
                if (contentTypeListString != null &&
                        contentTypeListString.startsWith("application/vnd.wap.mms-message;*/*")) {
                    contentTypeListString = "*/*";
                }
            }

            if (contentTypeListString == null) {
                if (log.isLoggable(Level.FINE))
                    log.fine("No content type list given, creating HtmlResponseWriterImpl with default content type.");

                contentTypeListString = HTML_CONTENT_TYPE;
            }
        }

        List contentTypeList = splitContentTypeListString(contentTypeListString);
        String[] supportedContentTypeArray = getSupportedContentTypes();

        String selectedContentType = null;

        for (int i = 0; i < supportedContentTypeArray.length; i++) {
            String supportedContentType = supportedContentTypeArray[i].trim();

            for (int j = 0; j < contentTypeList.size(); j++) {
                String contentType = (String) contentTypeList.get(j);

                if (contentType.indexOf(supportedContentType) != -1) {
                    if (isHTMLContentType(contentType)) {
                        selectedContentType = HTML_CONTENT_TYPE;
                    } else if (isXHTMLContentType(contentType)) {
                        selectedContentType = XHTML_CONTENT_TYPE;
                    }
                    break;
                }
            }
            if (selectedContentType != null) {
                break;
            }
        }

        if (selectedContentType == null) {
            throw new IllegalArgumentException("ContentTypeList does not contain a supported content type: " +
                    contentTypeListString);
        }
        return selectedContentType;
    }

    public static String[] getSupportedContentTypes() {
        //String[] supportedContentTypeArray = new String[]{HTML_CONTENT_TYPE,TEXT_ANY_CONTENT_TYPE,ANY_CONTENT_TYPE,
        //                                                  XHTML_CONTENT_TYPE,APPLICATION_XML_CONTENT_TYPE,TEXT_XML_CONTENT_TYPE};
        return SUPPORTED_CONTENT_TYPES;
    }

    private static boolean isHTMLContentType(String contentType) {
        return contentType.indexOf(HTML_CONTENT_TYPE) != -1 ||
                contentType.indexOf(ANY_CONTENT_TYPE) != -1 ||
                contentType.indexOf(TEXT_ANY_CONTENT_TYPE) != -1;
    }

    public static boolean isXHTMLContentType(String contentType) {
        return contentType.indexOf(XHTML_CONTENT_TYPE) != -1 ||
                contentType.indexOf(APPLICATION_XML_CONTENT_TYPE) != -1 ||
                contentType.indexOf(TEXT_XML_CONTENT_TYPE) != -1;
    }

    private static List splitContentTypeListString(String contentTypeListString) {
        List contentTypeList = new ArrayList();

        StringTokenizer st = new StringTokenizer(contentTypeListString, ",");
        while (st.hasMoreTokens()) {
            String contentType = st.nextToken().trim();

            int semicolonIndex = contentType.indexOf(";");

            if (semicolonIndex != -1) {
                contentType = contentType.substring(0, semicolonIndex);
            }

            contentTypeList.add(contentType);
        }

        return contentTypeList;
    }

    public static String getJavascriptLocation(UIComponent component) {
        if (component == null)
            return null;

        return (String) component.getAttributes().get(JSFAttr.JAVASCRIPT_LOCATION);
    }

    public static String getImageLocation(UIComponent component) {
        if (component == null)
            return null;

        return (String) component.getAttributes().get(JSFAttr.IMAGE_LOCATION);
    }

    public static String getStyleLocation(UIComponent component) {
        if (component == null)
            return null;

        return (String) component.getAttributes().get(JSFAttr.STYLE_LOCATION);
    }

    /**
     * Checks if the given component has a behavior attachment with a given name.
     *
     * @param eventName the event name to be checked for
     * @param behaviors map of behaviors attached to the component
     * @return true if client behavior with given name is attached, false otherwise
     * @since 4.0.0
     */
    public static boolean hasClientBehavior(String eventName,
                                            Map<String, List<ClientBehavior>> behaviors,
                                            FacesContext facesContext) {
        if (behaviors == null) {
            return false;
        }
        return (behaviors.get(eventName) != null);
    }

    public static Collection<ClientBehaviorContext.Parameter> getClientBehaviorContextParameters(
            Map<String, String> params) {
        List<ClientBehaviorContext.Parameter> paramList = null;
        if (params != null) {
            paramList = new ArrayList<ClientBehaviorContext.Parameter>(
                    params.size());
            for (Map.Entry<String, String> paramEntry : params.entrySet()) {
                paramList.add(new ClientBehaviorContext.Parameter(paramEntry
                        .getKey(), paramEntry.getValue()));
            }
        }
        return paramList;
    }

    /**
     * builds the chained behavior script which then can be reused
     * in following order by the other script building parts
     * <p/>
     * user defined event handling script
     * behavior script
     * renderer default script
     *
     * @param eventName    event name ("onclick" etc...)
     * @param uiComponent  the component which has the attachement (or should have)
     * @param facesContext the facesContext
     * @param params       params map of params which have to be dragged into the request
     * @return a string representation of the javascripts for the attached event behavior, an empty string if none is present
     * @since 4.0.0
     */
    private static boolean getClientBehaviorScript(FacesContext facesContext,
                                                   UIComponent uiComponent, String eventName,
                                                   Map<String, List<ClientBehavior>> clientBehaviors,
                                                   ScriptContext target, Collection<ClientBehaviorContext.Parameter> params)
    {
        return getClientBehaviorScript(facesContext, 
                                       uiComponent, uiComponent.getClientId(facesContext), eventName, 
                                       clientBehaviors, 
                                       target, params);
    }
    
    private static boolean getClientBehaviorScript(FacesContext facesContext,
                                                   UIComponent uiComponent, String targetClientId, String eventName,
                                                   Map<String, List<ClientBehavior>> clientBehaviors,
                                                   ScriptContext target, Collection<ClientBehaviorContext.Parameter> params) {
    
        if (!(uiComponent instanceof ClientBehaviorHolder)) {
            target.append(STR_EMPTY);
            return false;
        }

        ExternalContext externalContext = facesContext.getExternalContext();

        boolean renderClientBehavior = JavascriptUtils
                .isJavascriptAllowed(externalContext)
                && clientBehaviors != null && clientBehaviors.size() > 0;
        if (!renderClientBehavior) {
            target.append(STR_EMPTY);
            return false;
        }

        List<ClientBehavior> attachedEventBehaviors = clientBehaviors
                .get(eventName);
        if (attachedEventBehaviors == null
                || attachedEventBehaviors.size() == 0) {
            target.append(STR_EMPTY);
            return false;
        }

        ClientBehaviorContext context = ClientBehaviorContext
                .createClientBehaviorContext(facesContext, uiComponent,
                        eventName, targetClientId,
                        params);


        boolean submitting = false;
        Iterator<ClientBehavior> clientIterator = attachedEventBehaviors
                .iterator();
        while (clientIterator.hasNext()) {
            ClientBehavior clientBehavior = clientIterator.next();
            String script = clientBehavior.getScript(context);

            // The script _can_ be null, and in fact is for <f:ajax disabled="true" />

            if (script != null) {
                //either strings or functions, but I assume string is more appropriate since it allows access to the
                //origin as this!
                target.append("'" + escapeJavaScriptForChain(script) + "'");
                if (clientIterator.hasNext()) {
                    target.append(", ");
                }
            }
            if (!submitting) {
                submitting = clientBehavior.getHints().contains(ClientBehaviorHint.SUBMITTING);
            }
        }
        return submitting;
    }

    /**
     * @param facesContext
     * @param uiComponent
     * @param clientBehaviors
     * @param eventName
     * @param userEventCode
     * @param serverEventCode
     * @param params
     * @return
     * @since 4.0.0
     */
    public static String buildBehaviorChain(FacesContext facesContext,
                                            UIComponent uiComponent,
                                            String eventName, Collection<ClientBehaviorContext.Parameter> params,
                                            Map<String, List<ClientBehavior>> clientBehaviors,
                                            String userEventCode, String serverEventCode)
    {
        return buildBehaviorChain(facesContext, 
                                  uiComponent, uiComponent.getClientId(facesContext), 
                                  eventName, params, 
                                  clientBehaviors, 
                                  userEventCode, serverEventCode);
    }
    
    public static String buildBehaviorChain(FacesContext facesContext,
                                            UIComponent uiComponent, String targetClientId,
                                            String eventName, Collection<ClientBehaviorContext.Parameter> params,
                                            Map<String, List<ClientBehavior>> clientBehaviors,
                                            String userEventCode, String serverEventCode) {

        ExternalContext externalContext = facesContext.getExternalContext();
        boolean renderCode = JavascriptUtils
                .isJavascriptAllowed(externalContext);
        if (!renderCode) {
            return STR_EMPTY;
        }
        List<String> finalParams = new ArrayList<String>(3);
        if (userEventCode != null && !userEventCode.trim().equals(STR_EMPTY)) {
            // escape every ' in the user event code since it will
            // be a string attribute of jsf.util.chain
            finalParams.add('\'' + escapeJavaScriptForChain(userEventCode) + '\'');
        }

        final MyfacesConfig currentInstance = MyfacesConfig
                .getCurrentInstance(externalContext);
        ScriptContext behaviorCode = new ScriptContext();
        ScriptContext retVal = new ScriptContext(currentInstance.isPrettyHtml());

        getClientBehaviorScript(facesContext, uiComponent, targetClientId, eventName, clientBehaviors,
                behaviorCode, params);
        if (behaviorCode != null
                && !behaviorCode.toString().trim().equals(STR_EMPTY)) {
            finalParams.add(behaviorCode.toString());
        }
        if (serverEventCode != null
                && !serverEventCode.trim().equals(STR_EMPTY)) {
            finalParams.add('\'' + escapeJavaScriptForChain(serverEventCode) + '\'');
        }
        Iterator<String> it = finalParams.iterator();

        // It's possible that there are no behaviors to render.  For example, if we have
        // <f:ajax disabled="true" /> as the only behavior.

        if (it.hasNext()) {
            //according to the spec jsf.util.chain has to be used to build up the behavior and scripts
            retVal.append("jsf.util.chain(document.getElementById('"
                    + targetClientId + "'), event,");
            while (it.hasNext()) {
                retVal.append(it.next());
                if (it.hasNext()) {
                    retVal.append(", ");
                }
            }
            retVal.append(");");
        }

        return retVal.toString();

    }

    /**
     * @param facesContext
     * @param uiComponent
     * @param clientBehaviors
     * @param eventName1
     * @param eventName2
     * @param userEventCode
     * @param serverEventCode
     * @param params
     * @return
     * @since 4.0.0
     */
    public static String buildBehaviorChain(FacesContext facesContext,
                                            UIComponent uiComponent,
                                            String eventName1, Collection<ClientBehaviorContext.Parameter> params,
                                            String eventName2, Collection<ClientBehaviorContext.Parameter> params2,
                                            Map<String, List<ClientBehavior>> clientBehaviors,
                                            String userEventCode,
                                            String serverEventCode)
    {
        return buildBehaviorChain(facesContext, 
                                  uiComponent, uiComponent.getClientId(facesContext), 
                                  eventName1, params, 
                                  eventName2, params2, 
                                  clientBehaviors, 
                                  userEventCode, 
                                  serverEventCode);
    }
    
    public static String buildBehaviorChain(FacesContext facesContext,
            UIComponent uiComponent, String targetClientId,
            String eventName1, Collection<ClientBehaviorContext.Parameter> params,
            String eventName2, Collection<ClientBehaviorContext.Parameter> params2,
            Map<String, List<ClientBehavior>> clientBehaviors,
            String userEventCode,
            String serverEventCode) {

        ExternalContext externalContext = facesContext.getExternalContext();
        boolean renderCode = JavascriptUtils
                .isJavascriptAllowed(externalContext);
        if (!renderCode) {
            return STR_EMPTY;
        }
        List<String> finalParams = new ArrayList<String>(3);
        if (userEventCode != null && !userEventCode.trim().equals(STR_EMPTY)) {
            finalParams.add('\'' + escapeJavaScriptForChain(userEventCode) + '\'');
        }

        final MyfacesConfig currentInstance = MyfacesConfig
                .getCurrentInstance(externalContext);
        ScriptContext behaviorCode = new ScriptContext();
        ScriptContext retVal = new ScriptContext(currentInstance.isPrettyHtml());

        boolean submitting1 = getClientBehaviorScript(facesContext, uiComponent, targetClientId, eventName1, clientBehaviors,
                behaviorCode, params);
        boolean submitting2 = getClientBehaviorScript(facesContext, uiComponent, targetClientId, eventName2, clientBehaviors,
                behaviorCode, params2);

        // ClientBehaviors for both events have to be checked for the Submitting hint
        boolean submitting = submitting1 || submitting2;

        if (behaviorCode != null
                && !behaviorCode.toString().trim().equals(STR_EMPTY)) {
            finalParams.add(behaviorCode.toString());
        }
        if (serverEventCode != null
                && !serverEventCode.trim().equals(STR_EMPTY)) {
            finalParams.add('\'' + escapeJavaScriptForChain(serverEventCode) + '\'');
        }
        Iterator<String> it = finalParams.iterator();

        // It's possible that there are no behaviors to render.  For example, if we have
        // <f:ajax disabled="true" /> as the only behavior.

        if (it.hasNext()) {
            if (!submitting) {
                retVal.append("return ");
            }
            //according to the spec jsf.util.chain has to be used to build up the behavior and scripts
            retVal.append("jsf.util.chain(document.getElementById('"
                    +  targetClientId + "'), event,");
            while (it.hasNext()) {
                retVal.append(it.next());
                if (it.hasNext()) {
                    retVal.append(", ");
                }
            }
            retVal.append(");");
            if (submitting) {
                retVal.append(" return false;");
            }
        }

        return retVal.toString();

    }

    /**
     * This function correctly escapes the given JavaScript code
     * for the use in the jsf.util.chain() JavaScript function.
     * It also handles double-escaping correclty.
     *
     * @param javaScript
     * @return
     */
    public static String escapeJavaScriptForChain(String javaScript)
    {
        // first replace \' with \\'
        //String escaped = StringUtils.replace(javaScript, "\\'", "\\\\'");

        // then replace ' with \'
        // (this will replace every \' in the original to \\\')
        //escaped = StringUtils.replace(escaped, '\'', "\\'");
        
        //return escaped;

        StringBuffer out = null;
        for (int pos = 0; pos < javaScript.length(); pos++)
        {
            char c = javaScript.charAt(pos);
            
            if (c == '\\' || c == '\'')
            {
                if (out == null)
                {
                    out = new StringBuffer(javaScript.length() + 8);
                    if (pos > 0)
                    {
                        out.append(javaScript, 0, pos);
                    }
                }
                out.append('\\');
            }
            if (out != null)
            {
                out.append(c);
            }
        }
        
        if (out == null)
        {
            return javaScript;
        }
        else
        {
            return out.toString(); 
        }
    }

    /**
     * @param facesContext
     * @param uiComponent
     * @return
     */
    public static Map<String, String> mapAttachedParamsToStringValues(FacesContext facesContext, UIComponent uiComponent) {
        Map<String, String> retVal = null;

        if (uiComponent.getChildCount() > 0)
        {
            List<UIParameter> validParams = getValidUIParameterChildren(
                    facesContext, uiComponent.getChildren(), true, true);
            for (UIParameter param : validParams) {
                String name = param.getName();
                Object value = param.getValue();
                if (retVal == null)
                {
                    retVal = new HashMap<String, String>();
                }
                if (value instanceof String)
                {
                    retVal.put(name, (String) value);
                }
                else
                {
                    retVal.put(name, value.toString());
                }
            }
        }
        
        if (retVal == null)
        {
            retVal = Collections.emptyMap();
        }

        return retVal;
    }

    /**
     * Calls getValidUIParameterChildren(facesContext, children, skipNullValue, skipUnrendered, true);
     *
     * @param facesContext
     * @param children
     * @param skipNullValue
     * @param skipUnrendered
     * @return
     */
    public static List<UIParameter> getValidUIParameterChildren(FacesContext facesContext,
                                                                List<UIComponent> children, boolean skipNullValue, boolean skipUnrendered) {
        return getValidUIParameterChildren(facesContext, children, skipNullValue, skipUnrendered, true);
    }

    /**
     * Returns a List of all valid UIParameter children from the given children.
     * Valid means that the UIParameter is not disabled, its name is not null
     * (if skipNullName is true), its value is not null (if skipNullValue is true)
     * and it is rendered (if skipUnrendered is true). This method also creates a
     * warning for every UIParameter with a null-name (again, if skipNullName is true)
     * and, if ProjectStage is Development and skipNullValue is true, it informs the
     * user about every null-value.
     *
     * @param facesContext
     * @param children
     * @param skipNullValue  should UIParameters with a null value be skipped
     * @param skipUnrendered should UIParameters with isRendered() returning false be skipped
     * @param skipNullName   should UIParameters with a null name be skipped
     *                       (normally true, but in the case of h:outputFormat false)
     * @return
     */
    public static List<UIParameter> getValidUIParameterChildren(FacesContext facesContext,
                                                                List<UIComponent> children, boolean skipNullValue, boolean skipUnrendered, boolean skipNullName) {
        List<UIParameter> params = null;

        for (int i = 0, size = children.size(); i < size; i++) {
            UIComponent child = children.get(i);
            if (child instanceof UIParameter) {
                UIParameter param = (UIParameter) child;

                // check for the disable attribute (since 2.0)
                // and the render attribute (only if skipUnrendered is true)
                if (param.isDisable() ||
                        (skipUnrendered && !param.isRendered())) {
                    // ignore this UIParameter and continue
                    continue;
                }

                // check the name
                String name = param.getName();
                if (skipNullName && (name == null || STR_EMPTY.equals(name))) {
                    // warn for a null-name
                    log.log(Level.WARNING, "The UIParameter " + RendererUtils.getPathToComponent(param) +
                            " has a name of null or empty string and thus will not be added to the URL.");
                    // and skip it
                    continue;
                }

                // check the value
                if (skipNullValue && param.getValue() == null) {
                    if (facesContext.isProjectStage(ProjectStage.Development)) {
                        // inform the user about the null value when in Development stage
                        log.log(Level.INFO, "The UIParameter " + RendererUtils.getPathToComponent(param) +
                                " has a value of null and thus will not be added to the URL.");
                    }
                    // skip a null-value
                    continue;
                }

                // add the param
                if (params == null) {
                    params = new ArrayList<UIParameter>();
                }
                params.add(param);
            }
        }

        if (params == null) {
            params = Collections.emptyList();
        }
        return params;
    }

    /**
     * Render an attribute taking into account the passed event and
     * the component property. It will be rendered as "componentProperty"
     * attribute.
     *
     * @param facesContext
     * @param writer
     * @param componentProperty
     * @param component
     * @param eventName
     * @param clientBehaviors
     * @return
     * @throws IOException
     * @since 4.0.1
     */
    public static boolean renderBehaviorizedAttribute(
            FacesContext facesContext, ResponseWriter writer,
            String componentProperty, UIComponent component,
            String eventName, Map<String, List<ClientBehavior>> clientBehaviors) throws IOException {
        return renderBehaviorizedAttribute(
                facesContext, writer,
                componentProperty, component,
                eventName, clientBehaviors, componentProperty);
    }
    
    public static boolean renderBehaviorizedAttribute(
            FacesContext facesContext, ResponseWriter writer,
            String componentProperty, UIComponent component, String targetClientId,
            String eventName, Map<String, List<ClientBehavior>> clientBehaviors) throws IOException {
        return renderBehaviorizedAttribute(
                facesContext, writer,
                componentProperty, component, targetClientId,
                eventName, clientBehaviors, componentProperty);
    }


    /**
     * Render an attribute taking into account the passed event and
     * the component property. The event will be rendered on the selected
     * htmlAttrName
     *
     * @param facesContext
     * @param writer
     * @param component
     * @param clientBehaviors
     * @param eventName
     * @param componentProperty
     * @param htmlAttrName
     * @return
     * @throws IOException
     * @since 4.0.1
     */
    public static boolean renderBehaviorizedAttribute(
            FacesContext facesContext, ResponseWriter writer,
            String componentProperty, UIComponent component,
            String eventName, Map<String, List<ClientBehavior>> clientBehaviors,
            String htmlAttrName) throws IOException {
        return renderBehaviorizedAttribute(facesContext, writer,
                componentProperty, component,
                eventName, null, clientBehaviors,
                htmlAttrName, (String) component.getAttributes().get(componentProperty));
    }
    
    public static boolean renderBehaviorizedAttribute(
            FacesContext facesContext, ResponseWriter writer,
            String componentProperty, UIComponent component, String targetClientId,
            String eventName, Map<String, List<ClientBehavior>> clientBehaviors,
            String htmlAttrName) throws IOException {
        return renderBehaviorizedAttribute(facesContext, writer,
                componentProperty, component, targetClientId,
                eventName, null, clientBehaviors,
                htmlAttrName, (String) component.getAttributes().get(componentProperty));
    }

    /**
     * Render an attribute taking into account the passed event,
     * the component property and the passed attribute value for the component
     * property. The event will be rendered on the selected htmlAttrName.
     *
     * @param facesContext
     * @param writer
     * @param componentProperty
     * @param component
     * @param eventName
     * @param clientBehaviors
     * @param htmlAttrName
     * @param attributeValue
     * @return
     * @throws IOException
     */
    public static boolean renderBehaviorizedAttribute(
            FacesContext facesContext, ResponseWriter writer,
            String componentProperty, UIComponent component,
            String eventName, Collection<ClientBehaviorContext.Parameter> eventParameters, Map<String, List<ClientBehavior>> clientBehaviors,
            String htmlAttrName, String attributeValue) throws IOException
    {
        return renderBehaviorizedAttribute(
                facesContext, writer,
                componentProperty, component, component.getClientId(facesContext),
                eventName, eventParameters, clientBehaviors,
                htmlAttrName, attributeValue);
    }
    
    public static boolean renderBehaviorizedAttribute(
            FacesContext facesContext, ResponseWriter writer,
            String componentProperty, UIComponent component, String targetClientId,
            String eventName, Collection<ClientBehaviorContext.Parameter> eventParameters, Map<String, List<ClientBehavior>> clientBehaviors,
            String htmlAttrName, String attributeValue) throws IOException
    {

        List<ClientBehavior> cbl = (clientBehaviors != null) ? clientBehaviors.get(eventName) : null;

        if (cbl == null || cbl.size() == 0) {
            return renderHTMLAttribute(writer, componentProperty, htmlAttrName, attributeValue);
        }

        if (cbl.size() > 1 || (cbl.size() == 1 && attributeValue != null)) {
            return renderHTMLAttribute(writer, componentProperty, htmlAttrName,
                    HtmlRendererUtils.buildBehaviorChain(facesContext,
                            component, targetClientId, eventName, eventParameters, clientBehaviors,
                            attributeValue, STR_EMPTY));
        } else {
            //Only 1 behavior and attrValue == null, so just render it directly
            return renderHTMLAttribute(writer, componentProperty, htmlAttrName,
                    cbl.get(0).getScript(
                            ClientBehaviorContext
                                    .createClientBehaviorContext(facesContext, component,
                                            eventName, targetClientId,
                                            eventParameters)));
        }
    }

    /**
     * Render an attribute taking into account the passed event,
     * the passed attribute value for the component property.
     * and the specific server code.
     * The event will be rendered on the selected htmlAttrName.
     *
     * @param facesContext
     * @param writer
     * @param componentProperty
     * @param component
     * @param eventName
     * @param clientBehaviors
     * @param htmlAttrName
     * @param attributeValue
     * @param serverSideScript
     * @return
     * @throws IOException
     */
    public static boolean renderBehaviorizedAttribute(
            FacesContext facesContext, ResponseWriter writer,
            String componentProperty, UIComponent component,
            String eventName, Collection<ClientBehaviorContext.Parameter> eventParameters, Map<String, List<ClientBehavior>> clientBehaviors,
            String htmlAttrName, String attributeValue, String serverSideScript) throws IOException
    {
        return renderBehaviorizedAttribute(
                facesContext, writer,
                componentProperty, component, component.getClientId(facesContext),
                eventName, eventParameters, clientBehaviors,
                htmlAttrName, attributeValue, serverSideScript);
    }
    
    public static boolean renderBehaviorizedAttribute(
            FacesContext facesContext, ResponseWriter writer,
            String componentProperty, UIComponent component, String targetClientId,
            String eventName, Collection<ClientBehaviorContext.Parameter> eventParameters, Map<String, List<ClientBehavior>> clientBehaviors,
            String htmlAttrName, String attributeValue, String serverSideScript) throws IOException {

        List<ClientBehavior> cbl = (clientBehaviors != null) ? clientBehaviors.get(eventName) : null;

        if (((cbl != null) ? cbl.size() : 0) + (attributeValue != null ? 1 : 0) + (serverSideScript != null ? 1 : 0) <= 1) {
            if (cbl == null || cbl.size() == 0) {
                if (attributeValue != null) {
                    return renderHTMLAttribute(writer, componentProperty, htmlAttrName, attributeValue);
                } else {
                    return renderHTMLAttribute(writer, componentProperty, htmlAttrName, serverSideScript);
                }
            } else {
                return renderHTMLAttribute(writer, componentProperty, htmlAttrName,
                        cbl.get(0).getScript(
                                ClientBehaviorContext
                                        .createClientBehaviorContext(facesContext, component,
                                                eventName, targetClientId,
                                                eventParameters)));
            }
        } else {
            return renderHTMLAttribute(writer, componentProperty, htmlAttrName,
                    HtmlRendererUtils.buildBehaviorChain(facesContext,
                            component, targetClientId, eventName, eventParameters, clientBehaviors,
                            attributeValue, serverSideScript));
        }
    }

    /**
     * @param facesContext
     * @param writer
     * @param componentProperty
     * @param component
     * @param eventName
     * @param eventParameters
     * @param eventName2
     * @param eventParameters2
     * @param clientBehaviors
     * @param htmlAttrName
     * @param attributeValue
     * @param serverSideScript
     * @return
     * @throws IOException
     */
    public static boolean renderBehaviorizedAttribute(
            FacesContext facesContext, ResponseWriter writer,
            String componentProperty, UIComponent component,
            String eventName, Collection<ClientBehaviorContext.Parameter> eventParameters,
            String eventName2, Collection<ClientBehaviorContext.Parameter> eventParameters2,
            Map<String, List<ClientBehavior>> clientBehaviors,
            String htmlAttrName, String attributeValue, String serverSideScript) throws IOException {
        return renderBehaviorizedAttribute(
                facesContext, writer,
                componentProperty, component, component.getClientId(facesContext),
                eventName, eventParameters,
                eventName2, eventParameters2,
                clientBehaviors,
                htmlAttrName, attributeValue, serverSideScript);
    }
    
    public static boolean renderBehaviorizedAttribute(
            FacesContext facesContext, ResponseWriter writer,
            String componentProperty, UIComponent component, String targetClientId,
            String eventName, Collection<ClientBehaviorContext.Parameter> eventParameters,
            String eventName2, Collection<ClientBehaviorContext.Parameter> eventParameters2,
            Map<String, List<ClientBehavior>> clientBehaviors,
            String htmlAttrName, String attributeValue, String serverSideScript) throws IOException
    {
        List<ClientBehavior> cb1 = (clientBehaviors != null) ? clientBehaviors.get(eventName) : null;
        List<ClientBehavior> cb2 = (clientBehaviors != null) ? clientBehaviors.get(eventName2) : null;

        if (((cb1 != null) ? cb1.size() : 0) + ((cb2 != null) ? cb2.size() : 0) + (attributeValue != null ? 1 : 0) <= 1) {
            if (attributeValue != null) {
                return renderHTMLAttribute(writer, componentProperty, htmlAttrName, attributeValue);
            } else if (serverSideScript != null) {
                return renderHTMLAttribute(writer, componentProperty, htmlAttrName, serverSideScript);
            } else if (((cb1 != null) ? cb1.size() : 0) > 0) {
                return renderHTMLAttribute(writer, componentProperty, htmlAttrName,
                        cb1.get(0).getScript(
                                ClientBehaviorContext
                                        .createClientBehaviorContext(facesContext, component,
                                                eventName, targetClientId,
                                                eventParameters)));
            } else {
                return renderHTMLAttribute(writer, componentProperty, htmlAttrName,
                        cb2.get(0).getScript(
                                ClientBehaviorContext
                                        .createClientBehaviorContext(facesContext, component,
                                                eventName2, targetClientId,
                                                eventParameters2)));
            }
        } else {
            return renderHTMLAttribute(writer, componentProperty, htmlAttrName,
                    HtmlRendererUtils.buildBehaviorChain(
                            facesContext, component, targetClientId,
                            eventName, eventParameters,
                            eventName2, eventParameters2,
                            clientBehaviors, attributeValue, serverSideScript));
        }
    }

    /**
     * @param facesContext
     * @param writer
     * @param uiComponent
     * @param clientBehaviors
     * @throws IOException
     * @since 4.0.0
     */
    public static void renderBehaviorizedEventHandlers(
            FacesContext facesContext, ResponseWriter writer, UIComponent uiComponent,
            Map<String, List<ClientBehavior>> clientBehaviors) throws IOException {
        renderBehaviorizedEventHandlers(facesContext, writer, uiComponent, uiComponent.getClientId(facesContext), clientBehaviors);
    }
    
    public static void renderBehaviorizedEventHandlers(
            FacesContext facesContext, ResponseWriter writer, UIComponent uiComponent, String targetClientId,
            Map<String, List<ClientBehavior>> clientBehaviors) throws IOException {
        renderBehaviorizedAttribute(facesContext, writer, HTML.ONCLICK_ATTR, uiComponent, targetClientId,
                ClientBehaviorEvents.CLICK, clientBehaviors, HTML.ONCLICK_ATTR);
        renderBehaviorizedAttribute(facesContext, writer, HTML.ONDBLCLICK_ATTR, uiComponent, targetClientId,
                ClientBehaviorEvents.DBLCLICK, clientBehaviors, HTML.ONDBLCLICK_ATTR);
        renderBehaviorizedAttribute(facesContext, writer, HTML.ONMOUSEDOWN_ATTR, uiComponent, targetClientId,
                ClientBehaviorEvents.MOUSEDOWN, clientBehaviors, HTML.ONMOUSEDOWN_ATTR);
        renderBehaviorizedAttribute(facesContext, writer, HTML.ONMOUSEUP_ATTR, uiComponent, targetClientId,
                ClientBehaviorEvents.MOUSEUP, clientBehaviors, HTML.ONMOUSEUP_ATTR);
        renderBehaviorizedAttribute(facesContext, writer, HTML.ONMOUSEOVER_ATTR, uiComponent, targetClientId,
                ClientBehaviorEvents.MOUSEOVER, clientBehaviors, HTML.ONMOUSEOVER_ATTR);
        renderBehaviorizedAttribute(facesContext, writer, HTML.ONMOUSEMOVE_ATTR, uiComponent, targetClientId,
                ClientBehaviorEvents.MOUSEMOVE, clientBehaviors, HTML.ONMOUSEMOVE_ATTR);
        renderBehaviorizedAttribute(facesContext, writer, HTML.ONMOUSEOUT_ATTR, uiComponent, targetClientId,
                ClientBehaviorEvents.MOUSEOUT, clientBehaviors, HTML.ONMOUSEOUT_ATTR);
        renderBehaviorizedAttribute(facesContext, writer, HTML.ONKEYPRESS_ATTR, uiComponent, targetClientId,
                ClientBehaviorEvents.KEYPRESS, clientBehaviors, HTML.ONKEYPRESS_ATTR);
        renderBehaviorizedAttribute(facesContext, writer, HTML.ONKEYDOWN_ATTR, uiComponent, targetClientId,
                ClientBehaviorEvents.KEYDOWN, clientBehaviors, HTML.ONKEYDOWN_ATTR);
        renderBehaviorizedAttribute(facesContext, writer, HTML.ONKEYUP_ATTR, uiComponent, targetClientId,
                ClientBehaviorEvents.KEYUP, clientBehaviors, HTML.ONKEYUP_ATTR);
    }

    /**
     * @param facesContext
     * @param writer
     * @param uiComponent
     * @param clientBehaviors
     * @throws IOException
     * @since 4.0.0
     */
    public static void renderBehaviorizedEventHandlersWithoutOnclick(
            FacesContext facesContext, ResponseWriter writer, UIComponent uiComponent,
            Map<String, List<ClientBehavior>> clientBehaviors) throws IOException {
        renderBehaviorizedAttribute(facesContext, writer, HTML.ONDBLCLICK_ATTR, uiComponent,
                ClientBehaviorEvents.DBLCLICK, clientBehaviors, HTML.ONDBLCLICK_ATTR);
        renderBehaviorizedAttribute(facesContext, writer, HTML.ONMOUSEDOWN_ATTR, uiComponent,
                ClientBehaviorEvents.MOUSEDOWN, clientBehaviors, HTML.ONMOUSEDOWN_ATTR);
        renderBehaviorizedAttribute(facesContext, writer, HTML.ONMOUSEUP_ATTR, uiComponent,
                ClientBehaviorEvents.MOUSEUP, clientBehaviors, HTML.ONMOUSEUP_ATTR);
        renderBehaviorizedAttribute(facesContext, writer, HTML.ONMOUSEOVER_ATTR, uiComponent,
                ClientBehaviorEvents.MOUSEOVER, clientBehaviors, HTML.ONMOUSEOVER_ATTR);
        renderBehaviorizedAttribute(facesContext, writer, HTML.ONMOUSEMOVE_ATTR, uiComponent,
                ClientBehaviorEvents.MOUSEMOVE, clientBehaviors, HTML.ONMOUSEMOVE_ATTR);
        renderBehaviorizedAttribute(facesContext, writer, HTML.ONMOUSEOUT_ATTR, uiComponent,
                ClientBehaviorEvents.MOUSEOUT, clientBehaviors, HTML.ONMOUSEOUT_ATTR);
        renderBehaviorizedAttribute(facesContext, writer, HTML.ONKEYPRESS_ATTR, uiComponent,
                ClientBehaviorEvents.KEYPRESS, clientBehaviors, HTML.ONKEYPRESS_ATTR);
        renderBehaviorizedAttribute(facesContext, writer, HTML.ONKEYDOWN_ATTR, uiComponent,
                ClientBehaviorEvents.KEYDOWN, clientBehaviors, HTML.ONKEYDOWN_ATTR);
        renderBehaviorizedAttribute(facesContext, writer, HTML.ONKEYUP_ATTR, uiComponent,
                ClientBehaviorEvents.KEYUP, clientBehaviors, HTML.ONKEYUP_ATTR);
    }

    public static void renderBehaviorizedEventHandlersWithoutOnmouseoverAndOnmouseout(
            FacesContext facesContext, ResponseWriter writer, UIComponent uiComponent,
            Map<String, List<ClientBehavior>> clientBehaviors) throws IOException {
        renderBehaviorizedAttribute(facesContext, writer, HTML.ONCLICK_ATTR, uiComponent,
                ClientBehaviorEvents.CLICK, clientBehaviors, HTML.ONCLICK_ATTR);
        renderBehaviorizedAttribute(facesContext, writer, HTML.ONDBLCLICK_ATTR, uiComponent,
                ClientBehaviorEvents.DBLCLICK, clientBehaviors, HTML.ONDBLCLICK_ATTR);
        renderBehaviorizedAttribute(facesContext, writer, HTML.ONMOUSEDOWN_ATTR, uiComponent,
                ClientBehaviorEvents.MOUSEDOWN, clientBehaviors, HTML.ONMOUSEDOWN_ATTR);
        renderBehaviorizedAttribute(facesContext, writer, HTML.ONMOUSEUP_ATTR, uiComponent,
                ClientBehaviorEvents.MOUSEUP, clientBehaviors, HTML.ONMOUSEUP_ATTR);
        renderBehaviorizedAttribute(facesContext, writer, HTML.ONMOUSEMOVE_ATTR, uiComponent,
                ClientBehaviorEvents.MOUSEMOVE, clientBehaviors, HTML.ONMOUSEMOVE_ATTR);
        renderBehaviorizedAttribute(facesContext, writer, HTML.ONKEYPRESS_ATTR, uiComponent,
                ClientBehaviorEvents.KEYPRESS, clientBehaviors, HTML.ONKEYPRESS_ATTR);
        renderBehaviorizedAttribute(facesContext, writer, HTML.ONKEYDOWN_ATTR, uiComponent,
                ClientBehaviorEvents.KEYDOWN, clientBehaviors, HTML.ONKEYDOWN_ATTR);
        renderBehaviorizedAttribute(facesContext, writer, HTML.ONKEYUP_ATTR, uiComponent,
                ClientBehaviorEvents.KEYUP, clientBehaviors, HTML.ONKEYUP_ATTR);
    }

    /**
     * @param facesContext
     * @param writer
     * @param uiComponent
     * @param clientBehaviors
     * @throws IOException
     * @since 4.0.0
     */
    public static void renderBehaviorizedFieldEventHandlers(
            FacesContext facesContext, ResponseWriter writer, UIComponent uiComponent,
            Map<String, List<ClientBehavior>> clientBehaviors) throws IOException {
        renderBehaviorizedAttribute(facesContext, writer, HTML.ONFOCUS_ATTR, uiComponent,
                ClientBehaviorEvents.FOCUS, clientBehaviors, HTML.ONFOCUS_ATTR);
        renderBehaviorizedAttribute(facesContext, writer, HTML.ONBLUR_ATTR, uiComponent,
                ClientBehaviorEvents.BLUR, clientBehaviors, HTML.ONBLUR_ATTR);
        renderBehaviorizedAttribute(facesContext, writer, HTML.ONCHANGE_ATTR, uiComponent,
                ClientBehaviorEvents.CHANGE, clientBehaviors, HTML.ONCHANGE_ATTR);
        renderBehaviorizedAttribute(facesContext, writer, HTML.ONSELECT_ATTR, uiComponent,
                ClientBehaviorEvents.SELECT, clientBehaviors, HTML.ONSELECT_ATTR);
    }

    public static void renderBehaviorizedFieldEventHandlersWithoutOnfocus(
            FacesContext facesContext, ResponseWriter writer, UIComponent uiComponent,
            Map<String, List<ClientBehavior>> clientBehaviors) throws IOException {
        renderBehaviorizedAttribute(facesContext, writer, HTML.ONBLUR_ATTR, uiComponent,
                ClientBehaviorEvents.BLUR, clientBehaviors, HTML.ONBLUR_ATTR);
        renderBehaviorizedAttribute(facesContext, writer, HTML.ONCHANGE_ATTR, uiComponent,
                ClientBehaviorEvents.CHANGE, clientBehaviors, HTML.ONCHANGE_ATTR);
        renderBehaviorizedAttribute(facesContext, writer, HTML.ONSELECT_ATTR, uiComponent,
                ClientBehaviorEvents.SELECT, clientBehaviors, HTML.ONSELECT_ATTR);
    }

    /**
     * @param facesContext
     * @param writer
     * @param uiComponent
     * @param clientBehaviors
     * @throws IOException
     * @since 4.0.0
     */
    public static void renderBehaviorizedFieldEventHandlersWithoutOnchange(
            FacesContext facesContext, ResponseWriter writer, UIComponent uiComponent,
            Map<String, List<ClientBehavior>> clientBehaviors) throws IOException {
        renderBehaviorizedAttribute(facesContext, writer, HTML.ONFOCUS_ATTR, uiComponent,
                ClientBehaviorEvents.FOCUS, clientBehaviors, HTML.ONFOCUS_ATTR);
        renderBehaviorizedAttribute(facesContext, writer, HTML.ONBLUR_ATTR, uiComponent,
                ClientBehaviorEvents.BLUR, clientBehaviors, HTML.ONBLUR_ATTR);
        renderBehaviorizedAttribute(facesContext, writer, HTML.ONSELECT_ATTR, uiComponent,
                ClientBehaviorEvents.SELECT, clientBehaviors, HTML.ONSELECT_ATTR);
    }
    
    public static void renderBehaviorizedFieldEventHandlersWithoutOnchange(
            FacesContext facesContext, ResponseWriter writer, UIComponent uiComponent, String targetClientId,
            Map<String, List<ClientBehavior>> clientBehaviors) throws IOException {
        renderBehaviorizedAttribute(facesContext, writer, HTML.ONFOCUS_ATTR, uiComponent, targetClientId,
                ClientBehaviorEvents.FOCUS, clientBehaviors, HTML.ONFOCUS_ATTR);
        renderBehaviorizedAttribute(facesContext, writer, HTML.ONBLUR_ATTR, uiComponent, targetClientId,
                ClientBehaviorEvents.BLUR, clientBehaviors, HTML.ONBLUR_ATTR);
        renderBehaviorizedAttribute(facesContext, writer, HTML.ONSELECT_ATTR, uiComponent, targetClientId,
                ClientBehaviorEvents.SELECT, clientBehaviors, HTML.ONSELECT_ATTR);
    }


    public static void renderBehaviorizedFieldEventHandlersWithoutOnchangeAndOnselect(
            FacesContext facesContext, ResponseWriter writer, UIComponent uiComponent,
            Map<String, List<ClientBehavior>> clientBehaviors) throws IOException {
        renderBehaviorizedAttribute(facesContext, writer, HTML.ONFOCUS_ATTR, uiComponent,
                ClientBehaviorEvents.FOCUS, clientBehaviors, HTML.ONFOCUS_ATTR);
        renderBehaviorizedAttribute(facesContext, writer, HTML.ONBLUR_ATTR, uiComponent,
                ClientBehaviorEvents.BLUR, clientBehaviors, HTML.ONBLUR_ATTR);
    }

    /**
     * @param facesContext
     * @param writer
     * @param uiComponent
     * @param clientBehaviors
     * @return
     * @throws IOException
     * @since 4.0.0
     */
    public static boolean renderBehaviorizedOnchangeEventHandler(
            FacesContext facesContext, ResponseWriter writer, UIComponent uiComponent,
            Map<String, List<ClientBehavior>> clientBehaviors) throws IOException {
        boolean hasChange = HtmlRendererUtils.hasClientBehavior(ClientBehaviorEvents.CHANGE, clientBehaviors, facesContext);
        boolean hasValueChange = HtmlRendererUtils.hasClientBehavior(ClientBehaviorEvents.VALUECHANGE, clientBehaviors, facesContext);

        if (hasChange && hasValueChange) {
            String chain = HtmlRendererUtils.buildBehaviorChain(facesContext,
                    uiComponent, ClientBehaviorEvents.CHANGE, null, ClientBehaviorEvents.VALUECHANGE, null, clientBehaviors,
                    (String) uiComponent.getAttributes().get(HTML.ONCHANGE_ATTR), null);

            return HtmlRendererUtils.renderHTMLAttribute(writer, HTML.ONCHANGE_ATTR, HTML.ONCHANGE_ATTR, chain);
        } else if (hasChange) {
            return HtmlRendererUtils.renderBehaviorizedAttribute(facesContext, writer, HTML.ONCHANGE_ATTR, uiComponent,
                    ClientBehaviorEvents.CHANGE, clientBehaviors, HTML.ONCHANGE_ATTR);
        } else if (hasValueChange) {
            return HtmlRendererUtils.renderBehaviorizedAttribute(facesContext, writer, HTML.ONCHANGE_ATTR, uiComponent,
                    ClientBehaviorEvents.VALUECHANGE, clientBehaviors, HTML.ONCHANGE_ATTR);
        } else {
            return HtmlRendererUtils.renderHTMLAttribute(writer, uiComponent, HTML.ONCHANGE_ATTR, HTML.ONCHANGE_ATTR);
        }
    }
    
    public static boolean renderBehaviorizedOnchangeEventHandler(
            FacesContext facesContext, ResponseWriter writer, UIComponent uiComponent, String targetClientId,
            Map<String, List<ClientBehavior>> clientBehaviors) throws IOException {
        boolean hasChange = HtmlRendererUtils.hasClientBehavior(ClientBehaviorEvents.CHANGE, clientBehaviors, facesContext);
        boolean hasValueChange = HtmlRendererUtils.hasClientBehavior(ClientBehaviorEvents.VALUECHANGE, clientBehaviors, facesContext);

        if (hasChange && hasValueChange) {
            String chain = HtmlRendererUtils.buildBehaviorChain(facesContext,
                    uiComponent, targetClientId, ClientBehaviorEvents.CHANGE, null, ClientBehaviorEvents.VALUECHANGE, null, clientBehaviors,
                    (String) uiComponent.getAttributes().get(HTML.ONCHANGE_ATTR), null);

            return HtmlRendererUtils.renderHTMLAttribute(writer, HTML.ONCHANGE_ATTR, HTML.ONCHANGE_ATTR, chain);
        } else if (hasChange) {
            return HtmlRendererUtils.renderBehaviorizedAttribute(facesContext, writer, HTML.ONCHANGE_ATTR, uiComponent, targetClientId,
                    ClientBehaviorEvents.CHANGE, clientBehaviors, HTML.ONCHANGE_ATTR);
        } else if (hasValueChange) {
            return HtmlRendererUtils.renderBehaviorizedAttribute(facesContext, writer, HTML.ONCHANGE_ATTR, uiComponent, targetClientId,
                    ClientBehaviorEvents.VALUECHANGE, clientBehaviors, HTML.ONCHANGE_ATTR);
        } else {
            return HtmlRendererUtils.renderHTMLAttribute(writer, uiComponent, HTML.ONCHANGE_ATTR, HTML.ONCHANGE_ATTR);
        }
    }    

    public static void renderViewStateJavascript(FacesContext facesContext, String hiddenId, String serializedState) throws IOException {
        ResponseWriter writer = facesContext.getResponseWriter();

        writer.startElement(HTML.SCRIPT_ELEM, null);
        writer.writeAttribute(HTML.TYPE_ATTR, "text/javascript", null);

        final ExternalContext externalContext = facesContext.getExternalContext();
        final MyfacesConfig currentInstance = MyfacesConfig.getCurrentInstance(externalContext);

        ScriptContext context = new ScriptContext(currentInstance.isPrettyHtml());
        context.prettyLine();
        context.increaseIndent();

        context.append("function setViewState() {\n");
        context.append("\tvar state = '");
        context.append(serializedState);
        context.append("';\n");
        context.append("\tfor (var i = 0; i < document.forms.length; i++) {\n");
        context.append("\t\tdocument.forms[i]['" + hiddenId + "'].value = state;\n");
        context.append("\t}\n");
        context.append("}\n");
        context.append("setViewState();\n");

        context.decreaseIndent();

        writer.writeText(context.toString(), null);

        writer.endElement(HTML.SCRIPT_ELEM);
    }

    /**
     * Returns the value of the hideNoSelectionOption attribute of the given UIComponent
     *
     * @param component
     * @return
     */
    public static boolean isHideNoSelectionOption(UIComponent component) {
        // check hideNoSelectionOption for literal value (String) or ValueExpression (Boolean)
        Object hideNoSelectionOptionAttr = component.getAttributes().get(JSFAttr.HIDE_NO_SELECTION_OPTION_ATTR);
        return ((hideNoSelectionOptionAttr instanceof String && "true".equalsIgnoreCase((String) hideNoSelectionOptionAttr))
                || (hideNoSelectionOptionAttr instanceof Boolean && ((Boolean) hideNoSelectionOptionAttr)));
    }

    /**
     * Renders all FacesMessages which have not been rendered yet with
     * the help of a HtmlMessages component.
     *
     * @param facesContext
     */
    public static void renderUnhandledFacesMessages(FacesContext facesContext) throws IOException {
        // create and configure HtmlMessages component
        HtmlMessages messages = (HtmlMessages) facesContext.getApplication()
                .createComponent(HtmlMessages.COMPONENT_TYPE);
        messages.setId("javax_faces_developmentstage_messages");
        messages.setTitle("Project Stage[Development]: Unhandled Messages");
        messages.setStyle("color:orange");
        messages.setRedisplay(false);

        // render the component
        messages.encodeAll(facesContext);
    }

    /**
     * The ScriptContext offers methods and fields
     * to help with rendering out a script and keeping a
     * proper formatting.
     */
    public static class ScriptContext {
        private long currentIndentationLevel;
        private StringBuffer buffer = new StringBuffer();
        private boolean prettyPrint = false;
        /**
         * automatic formatting will render
         * new-lines and indents if blocks are opened
         * and closed - attention: you need to append
         * opening and closing brackets of blocks separately in this case!
         */
        private boolean automaticFormatting = true;

        public ScriptContext() {

        }

        public ScriptContext(boolean prettyPrint) {
            this.prettyPrint = prettyPrint;
        }

        public ScriptContext(StringBuffer buf, boolean prettyPrint) {
            this.prettyPrint = prettyPrint;
            this.buffer = buf;
        }

        public void increaseIndent() {
            currentIndentationLevel++;
        }

        public void decreaseIndent() {
            currentIndentationLevel--;

            if (currentIndentationLevel < 0)
                currentIndentationLevel = 0;
        }

        public void prettyLine() {
            if (prettyPrint) {
                append(LINE_SEPARATOR);

                for (int i = 0; i < getCurrentIndentationLevel(); i++)
                    append(TABULATOR);
            }
        }

        public void prettyLineIncreaseIndent() {
            increaseIndent();
            prettyLine();
        }

        public void prettyLineDecreaseIndent() {
            decreaseIndent();
            prettyLine();
        }

        public long getCurrentIndentationLevel() {
            return currentIndentationLevel;
        }

        public void setCurrentIndentationLevel(long currentIndentationLevel) {
            this.currentIndentationLevel = currentIndentationLevel;
        }

        public ScriptContext append(String str) {

            if (automaticFormatting && str.length() == 1) {
                boolean openBlock = str.equals("{");
                boolean closeBlock = str.equals("}");

                if (openBlock) {
                    prettyLine();
                } else if (closeBlock) {
                    prettyLineDecreaseIndent();
                }

                buffer.append(str);

                if (openBlock) {
                    prettyLineIncreaseIndent();
                } else if (closeBlock) {
                    prettyLine();
                }
            } else {
                buffer.append(str);
            }
            return this;
        }

        public ScriptContext append(char c) {
            buffer.append(c);
            return this;
        }

        public ScriptContext append(int i) {
            buffer.append(i);
            return this;
        }

        public String toString() {
            return buffer.toString();
        }
    }
}
