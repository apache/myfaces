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
package jakarta.faces.component.html;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import jakarta.faces.component.ActionSource;
import jakarta.faces.component.EditableValueHolder;
import jakarta.faces.component.behavior.ClientBehaviorHolder;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.BehaviorEvent.FacesComponentEvent;

/**
 * <p class="changed_added_5_0">
 * Events supported by HTML elements as per
 * <a href="https://html.spec.whatwg.org/multipage/webappapis.html#event-handlers-on-elements,-document-objects,-and-window-objects">current spec</a>.
 * These can be used to supply {@link ClientBehaviorHolder#getEventNames()} and
 * {@link ClientBehaviorHolder#getDefaultEventName()}.
 * </p>
 * <p>
 * The events are split into two enums: {@link HtmlDocumentElementEvent} lists the events that fire on the element
 * itself, while {@link HtmlBodyElementEvent} lists the events that are also part of the HTML spec's
 * <code>GlobalEventHandlers</code> but have special behavior on the HTML <code>&lt;body&gt;</code> element where they
 * are forwarded to the <code>Window</code> object instead of firing on the element. Components that represent the HTML
 * <code>&lt;body&gt;</code> element should use {@link #getHtmlDocumentElementEventNames(FacesContext)} to avoid
 * exposing these window-level events as component behavior events, while all other components should use
 * {@link #getHtmlBodyElementEventNames(FacesContext)} or one of its supersets.
 * </p>
 *
 * @since 5.0
 */
public final class HtmlEvents
{

    /**
     * Events supported by all HTML document elements that always fire on the element itself.
     * See {@link HtmlBodyElementEvent} for events that are forwarded to the <code>Window</code> object on the HTML
     * <code>&lt;body&gt;</code> element.
     */
    public enum HtmlDocumentElementEvent
    {
        abort,
        auxclick,
        beforeinput,
        beforematch,
        beforetoggle,
        cancel,
        canplay,
        canplaythrough,
        change,
        click,
        close,
        command,
        contextlost,
        contextmenu,
        contextrestored,
        copy,
        cuechange,
        cut,
        dblclick,
        drag,
        dragend,
        dragenter,
        dragleave,
        dragover,
        dragstart,
        drop,
        durationchange,
        emptied,
        ended,
        formdata,
        input,
        invalid,
        keydown,
        keypress,
        keyup,
        loadeddata,
        loadedmetadata,
        loadstart,
        mousedown,
        mouseenter,
        mouseleave,
        mousemove,
        mouseout,
        mouseover,
        mouseup,
        paste,
        pause,
        play,
        playing,
        progress,
        ratechange,
        reset,
        securitypolicyviolation,
        seeked,
        seeking,
        select,
        slotchange,
        stalled,
        submit,
        suspend,
        timeupdate,
        toggle,
        volumechange,
        waiting,
        wheel;
    }

    /**
     * Events from the HTML spec's <code>GlobalEventHandlers</code> that have special behavior on the HTML body element:
     * instead of firing on the element itself, they are forwarded to the <code>Window</code> object.
     * These events are valid on all HTML elements, but are listed separately here because of this distinction.
     */
    public enum HtmlBodyElementEvent
    {
        blur,
        error,
        focus,
        load,
        resize,
        scroll,
        scrollend;
    }

    /**
     * The name of the context-param whose value must represent a space-separated list of additional HTML event names.
     * All supported HTML event names are defined in the enums {@link HtmlDocumentElementEvent} and
     * {@link HtmlBodyElementEvent}. Any HTML event name which you wish to add to these enums can be supplied via this
     * context-param. Duplicates will be automatically filtered, case sensitive.
     */
    public static final String ADDITIONAL_HTML_EVENT_NAMES_PARAM_NAME = "jakarta.faces.ADDITIONAL_HTML_EVENT_NAMES";

    private enum CacheKey
    {
        ADDITIONAL_HTML_EVENT_NAMES,
        HTML_DOCUMENT_ELEMENT_EVENT_NAMES,
        HTML_BODY_ELEMENT_EVENT_NAMES,
        FACES_ACTION_SOURCE_EVENT_NAMES,
        FACES_EDITABLE_VALUE_HOLDER_EVENT_NAMES;
    }

    private HtmlEvents()
    {
        throw new AssertionError();
    }

    /**
     * @param context The involved faces context.
     * @return All additional HTML event names specified via {@link #ADDITIONAL_HTML_EVENT_NAMES_PARAM_NAME}.
     */
    public static Collection<String> getAdditionalHtmlEventNames(FacesContext context)
    {
        return cache(context, CacheKey.ADDITIONAL_HTML_EVENT_NAMES, () -> getContextParam(context));
    }

    /**
     * @param context The involved faces context.
     * @return All supported event names for HTML document elements, including additional HTML event names.
     */
    public static Collection<String> getHtmlDocumentElementEventNames(FacesContext context)
    {
        return cache(context, CacheKey.HTML_DOCUMENT_ELEMENT_EVENT_NAMES,
                () -> merge(getAdditionalHtmlEventNames(context), HtmlDocumentElementEvent.values()));
    }

    /**
     * @param context The involved faces context.
     * @return All supported event names for HTML body elements, including HTML document element event names.
     */
    public static Collection<String> getHtmlBodyElementEventNames(FacesContext context)
    {
        return cache(context, CacheKey.HTML_BODY_ELEMENT_EVENT_NAMES,
                () -> merge(getHtmlDocumentElementEventNames(context), HtmlBodyElementEvent.values()));
    }

    /**
     * @param context The involved faces context.
     * @return All supported event names for HTML implementations of Faces {@link ActionSource} components, including
     *         HTML body element event names.
     */
    public static Collection<String> getFacesActionSourceEventNames(FacesContext context)
    {
        return cache(context, CacheKey.FACES_ACTION_SOURCE_EVENT_NAMES,
                () -> merge(getHtmlBodyElementEventNames(context), FacesComponentEvent.action));
    }

    /**
     * @param context The involved faces context.
     * @return All supported event names for HTML implementations of Faces {@link EditableValueHolder} components,
     *         including HTML body element event names.
     */
    public static Collection<String> getFacesEditableValueHolderEventNames(FacesContext context)
    {
        return cache(context, CacheKey.FACES_EDITABLE_VALUE_HOLDER_EVENT_NAMES,
                () -> merge(getHtmlBodyElementEventNames(context), FacesComponentEvent.valueChange));
    }

    private static Collection<String> getContextParam(FacesContext context)
    {
        String param = context.getExternalContext().getInitParameter(ADDITIONAL_HTML_EVENT_NAMES_PARAM_NAME);
        if (param == null || param.isBlank())
        {
            return Collections.emptyList();
        }

        List<String> eventNames = new ArrayList<>();
        for (String eventName : param.trim().split("\\s+"))
        {
            if (!eventName.isEmpty())
            {
                eventNames.add(eventName);
            }
        }
        return Collections.unmodifiableList(eventNames);
    }

    private static Collection<String> merge(Collection<String> eventNames, Enum<?>... enumValues)
    {
        // a TreeSet gives us the sorted + distinct semantics in one go.
        TreeSet<String> merged = new TreeSet<>(eventNames);
        for (Enum<?> enumValue : enumValues)
        {
            merged.add(enumValue.name());
        }
        return new EventNameCollection(merged);
    }

    /**
     * Sorted, distinct and unmodifiable like the plain <code>List</code> the spec javadoc suggests, but with
     * constant time {@link #contains}, which is what {@link jakarta.faces.component.UIComponentBase#addClientBehavior}
     * and the renderers actually do with this collection.
     */
    private static final class EventNameCollection extends java.util.AbstractList<String>
            implements java.util.RandomAccess
    {
        private final String[] sortedEventNames;
        private final Set<String> lookup;

        private EventNameCollection(TreeSet<String> sortedDistinctEventNames)
        {
            this.sortedEventNames = sortedDistinctEventNames.toArray(new String[sortedDistinctEventNames.size()]);
            this.lookup = new HashSet<>(sortedDistinctEventNames);
        }

        @Override
        public String get(int index)
        {
            return sortedEventNames[index];
        }

        @Override
        public int size()
        {
            return sortedEventNames.length;
        }

        @Override
        public boolean contains(Object eventName)
        {
            return lookup.contains(eventName);
        }
    }

    @SuppressWarnings("unchecked") // the event-name cache is stored under an Object-valued application-map entry
    private static Collection<String> cache(FacesContext context, CacheKey key, Supplier<Collection<String>> supplier)
    {
        Map<String, Object> applicationMap = context.getExternalContext().getApplicationMap();

        // NOTE: this may concurrently create more than one cache map, which is harmless as all of them would hold
        // equal values anyway, and the map itself is concurrent so no entry can ever get lost or corrupted.
        Map<CacheKey, Collection<String>> cache =
                (Map<CacheKey, Collection<String>>) applicationMap.get(ADDITIONAL_HTML_EVENT_NAMES_PARAM_NAME);
        if (cache == null)
        {
            cache = new ConcurrentHashMap<>(CacheKey.values().length);
            applicationMap.put(ADDITIONAL_HTML_EVENT_NAMES_PARAM_NAME, cache);
        }

        // NOTE: deliberately no computeIfAbsent, the suppliers build on each other and hence recursively populate
        // this very map, which a ConcurrentHashMap rejects with "Recursive update". A plain get/put is fine here:
        // the values are immutable and only ever computed from the same input, so a race just recomputes them.
        Collection<String> eventNames = cache.get(key);
        if (eventNames == null)
        {
            eventNames = supplier.get();
            cache.put(key, eventNames);
        }
        return eventNames;
    }
}
