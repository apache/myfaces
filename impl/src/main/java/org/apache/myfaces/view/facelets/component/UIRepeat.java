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
package org.apache.myfaces.view.facelets.component;

import java.io.IOException;
import java.io.Serializable;
import java.sql.ResultSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.el.ValueExpression;
import javax.faces.FacesException;
import javax.faces.application.FacesMessage;
import javax.faces.component.EditableValueHolder;
import javax.faces.component.NamingContainer;
import javax.faces.component.UIComponent;
import javax.faces.component.UIComponentBase;
import javax.faces.component.UIData;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.FacesEvent;
import javax.faces.event.FacesListener;
import javax.faces.event.PhaseId;
import javax.faces.model.ArrayDataModel;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.model.ResultSetDataModel;
import javax.faces.model.ScalarDataModel;
import javax.faces.render.Renderer;

public class UIRepeat extends UIComponentBase implements NamingContainer
{
    public static final String COMPONENT_TYPE = "facelets.ui.Repeat";

    public static final String COMPONENT_FAMILY = "facelets";

    private final static DataModel<?> EMPTY_MODEL = new ListDataModel<Object>(Collections.emptyList());

    private final static SavedState NullState = new SavedState();

    private Map<String, SavedState> _childState;

    // our data
    private Object _value;

    // variables
    private String _var;

    //FIXME: varStatus isn't used, should support be added? private String _varStatus;

    private int _index = -1;

    // scoping
    private int _offset = -1;

    private int _size = -1;

    private transient StringBuffer _buffer;
    private transient DataModel<?> _model;
    private transient Object _origValue;

    public UIRepeat()
    {
        setRendererType("facelets.ui.Repeat");
    }

    public String getFamily()
    {
        return COMPONENT_FAMILY;
    }

    public int getOffset()
    {
        if (_offset != -1)
        {
            return _offset;
        }
        
        ValueExpression ve = getValueExpression("offset");
        if (ve != null)
        {
            return ((Integer)ve.getValue(getFacesContext().getELContext())).intValue();
        }
        
        return 0;
    }

    public void setOffset(int offset)
    {
        _offset = offset;
    }

    public int getSize()
    {
        if (_size != -1)
        {
            return _size;
        }
        
        ValueExpression ve = getValueExpression("size");
        if (ve != null)
        {
            return ((Integer)ve.getValue(getFacesContext().getELContext())).intValue();
        }
        
        return -1;
    }

    public void setSize(int size)
    {
        _size = size;
    }

    public String getVar()
    {
        return _var;
    }

    public void setVar(String var)
    {
        _var = var;
    }

    private synchronized void setDataModel(DataModel<?> model)
    {
        _model = model;
    }

    @SuppressWarnings("unchecked")
    private synchronized DataModel<?> getDataModel()
    {
        if (_model == null)
        {
            Object val = getValue();
            if (val == null)
            {
                _model = EMPTY_MODEL;
            }
            else if (val instanceof DataModel)
            {
                _model = (DataModel<?>) val;
            }
            else if (val instanceof List)
            {
                _model = new ListDataModel<Object>((List<Object>) val);
            }
            else if (Object[].class.isAssignableFrom(val.getClass()))
            {
                _model = new ArrayDataModel<Object>((Object[]) val);
            }
            else if (val instanceof ResultSet)
            {
                _model = new ResultSetDataModel((ResultSet) val);
            }
            else
            {
                _model = new ScalarDataModel(val);
            }
        }
        return _model;
    }

    public Object getValue()
    {
        if (_value == null)
        {
            ValueExpression ve = getValueExpression("value");
            if (ve != null)
            {
                return ve.getValue(getFacesContext().getELContext());
            }
        }
        
        return _value;
    }

    public void setValue(Object value)
    {
        _value = value;
    }

    @Override
    public String getClientId(FacesContext faces)
    {
        String id = super.getClientId(faces);
        if (_index >= 0)
        {
            id = _getBuffer().append(id).append(NamingContainer.SEPARATOR_CHAR).append(_index).toString();
        }
        return id;
    }

    private void _captureOrigValue()
    {
        if (_var != null)
        {
            _origValue = getFacesContext().getExternalContext().getRequestMap().get(_var);
        }
    }

    private StringBuffer _getBuffer()
    {
        if (_buffer == null)
        {
            _buffer = new StringBuffer();
        }
        
        _buffer.setLength(0);
        
        return _buffer;
    }

    private Map<String, SavedState> _getChildState()
    {
        if (_childState == null)
        {
            _childState = new HashMap<String, SavedState>();
        }
        
        return _childState;
    }

    private boolean _isIndexAvailable()
    {
        return getDataModel().isRowAvailable();
    }

    private boolean _isNestedInIterator()
    {
        UIComponent parent = getParent();
        while (parent != null)
        {
            if (parent instanceof UIData || parent instanceof UIRepeat)
            {
                return true;
            }
            parent = parent.getParent();
        }
        return false;
    }

    private boolean _keepSaved(FacesContext context)
    {
        for (String clientId : _getChildState().keySet())
        {
            for (FacesMessage message : context.getMessageList(clientId))
            {
                if (message.getSeverity().compareTo(FacesMessage.SEVERITY_ERROR) >= 0)
                {
                    return true;
                }
            }
        }
        
        return _isNestedInIterator();
    }

    private void _resetDataModel()
    {
        if (_isNestedInIterator())
        {
            setDataModel(null);
        }
    }

    private void _restoreOrigValue()
    {
        if (_var != null)
        {
            Map<String, Object> attrs = getFacesContext().getExternalContext().getRequestMap();
            if (_origValue != null)
            {
                attrs.put(_var, _origValue);
            }
            else
            {
                attrs.remove(_var);
            }
        }
    }

    private void _restoreChildState()
    {
        if (getChildCount() > 0)
        {
            FacesContext context = getFacesContext();
            for (UIComponent child : getChildren())
            {
                _restoreChildState(context, child);
            }
        }
    }

    private void _restoreChildState(FacesContext faces, UIComponent c)
    {
        // reset id
        String id = c.getId();
        c.setId(id);

        // hack
        if (c instanceof EditableValueHolder)
        {
            EditableValueHolder evh = (EditableValueHolder) c;
            String clientId = c.getClientId(faces);
            SavedState ss = _getChildState().get(clientId);
            if (ss != null)
            {
                ss.apply(evh);
            }
            else
            {
                NullState.apply(evh);
            }
        }

        // continue hack
        Iterator<UIComponent> itr = c.getFacetsAndChildren();
        while (itr.hasNext())
        {
            _restoreChildState(faces, itr.next());
        }
    }

    private void _saveChildState()
    {
        if (getChildCount() > 0)
        {
            FacesContext context = getFacesContext();
            for (UIComponent child : getChildren())
            {
                _saveChildState(context, child);
            }
        }
    }

    private void _saveChildState(FacesContext faces, UIComponent c)
    {
        if (c instanceof EditableValueHolder && !c.isTransient())
        {
            String clientId = c.getClientId(faces);
            SavedState ss = (SavedState) _getChildState().get(clientId);
            if (ss == null)
            {
                ss = new SavedState();
                _getChildState().put(clientId, ss);
            }
            
            ss.populate((EditableValueHolder) c);
        }

        // continue hack
        Iterator<UIComponent> itr = c.getFacetsAndChildren();
        while (itr.hasNext())
        {
            _saveChildState(faces, itr.next());
        }
    }

    private void _setIndex(int index)
    {
        // save child state
        _saveChildState();

        _index = index;
        
        DataModel<?> localModel = getDataModel();
        localModel.setRowIndex(index);

        if (_index != -1 && _var != null && localModel.isRowAvailable())
        {
            getFacesContext().getExternalContext().getRequestMap().put(_var, localModel.getRowData());
        }

        // restore child state
        _restoreChildState();
    }

    public void process(FacesContext faces, PhaseId phase)
    {
        // stop if not rendered
        if (!isRendered())
            return;

        // clear datamodel
        _resetDataModel();

        // reset index
        _captureOrigValue();
        _setIndex(-1);

        try
        {
            // has children
            if (getChildCount() > 0)
            {
                int i = getOffset();
                int end = getSize();
                end = (end >= 0) ? i + end : Integer.MAX_VALUE - 1;

                // grab renderer
                String rendererType = getRendererType();
                Renderer renderer = null;
                if (rendererType != null)
                {
                    renderer = getRenderer(faces);
                }

                _setIndex(i);
                while (i <= end && _isIndexAvailable())
                {

                    if (PhaseId.RENDER_RESPONSE.equals(phase) && renderer != null)
                    {
                        renderer.encodeChildren(faces, this);
                    }
                    else
                    {
                        for (UIComponent child : getChildren())
                        {
                            if (PhaseId.APPLY_REQUEST_VALUES.equals(phase))
                            {
                                child.processDecodes(faces);
                            }
                            else if (PhaseId.PROCESS_VALIDATIONS.equals(phase))
                            {
                                child.processValidators(faces);
                            }
                            else if (PhaseId.UPDATE_MODEL_VALUES.equals(phase))
                            {
                                child.processUpdates(faces);
                            }
                            else if (PhaseId.RENDER_RESPONSE.equals(phase))
                            {
                                child.encodeAll(faces);
                            }
                        }
                    }
                    
                    i++;
                    
                    _setIndex(i);
                }
            }
        }
        catch (IOException e)
        {
            throw new FacesException(e);
        }
        finally
        {
            _setIndex(-1);
            _restoreOrigValue();
        }
    }

    // public boolean invokeOnComponent(FacesContext faces, String clientId,
    // ContextCallback callback) throws FacesException {
    // String id = super.getClientId(faces);
    // if (clientId.equals(id)) {
    // callback.invokeContextCallback(faces, this);
    // return true;
    // } else if (clientId.startsWith(id)) {
    // int prevIndex = this.index;
    // int idxStart = clientId.indexOf(NamingContainer.SEPARATOR_CHAR, id
    // .length());
    // if (idxStart != -1
    // && Character.isDigit(clientId.charAt(idxStart + 1))) {
    // int idxEnd = clientId.indexOf(NamingContainer.SEPARATOR_CHAR,
    // idxStart);
    // if (idxEnd != -1) {
    // int newIndex = Integer.parseInt(clientId.substring(
    // idxStart, idxEnd));
    // boolean found = false;
    // try {
    // this.captureOrigValue();
    // this.setIndex(newIndex);
    // if (this.isIndexAvailable()) {
    // found = super.invokeOnComponent(faces, clientId,
    // callback);
    // }
    // } finally {
    // this.setIndex(prevIndex);
    // this.restoreOrigValue();
    // }
    // return found;
    // }
    // } else {
    // return super.invokeOnComponent(faces, clientId, callback);
    // }
    // }
    // return false;
    // }

    @Override
    public void processDecodes(FacesContext faces)
    {
        if (!isRendered())
        {
            return;
        }
        
        setDataModel(null);
        if (!_keepSaved(faces))
        {
            _childState = null;
        }
        
        process(faces, PhaseId.APPLY_REQUEST_VALUES);
        decode(faces);
    }

    @Override
    public void processUpdates(FacesContext faces)
    {
        if (!isRendered())
        {
            return;
        }
        
        _resetDataModel();
        process(faces, PhaseId.UPDATE_MODEL_VALUES);
    }

    @Override
    public void processValidators(FacesContext faces)
    {
        if (!isRendered())
        {
            return;
        }
        
        _resetDataModel();
        process(faces, PhaseId.PROCESS_VALIDATIONS);
    }

    // from RI
    private final static class SavedState implements Serializable
    {
        private boolean _localValueSet;
        private Object _submittedValue;
        private boolean _valid = true;
        private Object _value;

        private static final long serialVersionUID = 2920252657338389849L;

        Object getSubmittedValue()
        {
            return (_submittedValue);
        }

        void setSubmittedValue(Object submittedValue)
        {
            _submittedValue = submittedValue;
        }

        boolean isValid()
        {
            return (_valid);
        }

        void setValid(boolean valid)
        {
            _valid = valid;
        }

        Object getValue()
        {
            return _value;
        }

        public void setValue(Object value)
        {
            _value = value;
        }

        boolean isLocalValueSet()
        {
            return _localValueSet;
        }

        public void setLocalValueSet(boolean localValueSet)
        {
            _localValueSet = localValueSet;
        }

        @Override
        public String toString()
        {
            return ("submittedValue: " + _submittedValue + " value: " + _value + " localValueSet: " + _localValueSet);
        }

        public void populate(EditableValueHolder evh)
        {
            _value = evh.getValue();
            _valid = evh.isValid();
            _submittedValue = evh.getSubmittedValue();
            _localValueSet = evh.isLocalValueSet();
        }

        public void apply(EditableValueHolder evh)
        {
            evh.setValue(_value);
            evh.setValid(_valid);
            evh.setSubmittedValue(_submittedValue);
            evh.setLocalValueSet(_localValueSet);
        }
    }

    private final class IndexedEvent extends FacesEvent
    {
        private final FacesEvent _target;

        private final int _index;

        public IndexedEvent(UIRepeat owner, FacesEvent target, int index)
        {
            super(owner);
            _target = target;
            _index = index;
        }

        @Override
        public PhaseId getPhaseId()
        {
            return _target.getPhaseId();
        }

        @Override
        public void setPhaseId(PhaseId phaseId)
        {
            _target.setPhaseId(phaseId);
        }

        public boolean isAppropriateListener(FacesListener listener)
        {
            return _target.isAppropriateListener(listener);
        }

        public void processListener(FacesListener listener)
        {
            UIRepeat owner = (UIRepeat) getComponent();
            int prevIndex = owner._index;
            try
            {
                owner._setIndex(_index);
                if (owner._isIndexAvailable())
                {
                    _target.processListener(listener);
                }
            }
            finally
            {
                owner._setIndex(prevIndex);
            }
        }

        public int getIndex()
        {
            return _index;
        }

        public FacesEvent getTarget()
        {
            return _target;
        }

    }

    @Override
    public void broadcast(FacesEvent event) throws AbortProcessingException
    {
        if (event instanceof IndexedEvent)
        {
            IndexedEvent idxEvent = (IndexedEvent) event;
            _resetDataModel();
            int prevIndex = _index;
            try
            {
                _setIndex(idxEvent.getIndex());
                if (_isIndexAvailable())
                {
                    FacesEvent target = idxEvent.getTarget();
                    target.getComponent().broadcast(target);
                }
            }
            finally
            {
                _setIndex(prevIndex);
            }
        }
        else
        {
            super.broadcast(event);
        }
    }

    @Override
    public void queueEvent(FacesEvent event)
    {
        super.queueEvent(new IndexedEvent(this, event, _index));
    }

    @SuppressWarnings("unchecked")
    @Override
    public void restoreState(FacesContext faces, Object object)
    {
        Object[] state = (Object[]) object;
        super.restoreState(faces, state[0]);
        _childState = (Map<String, SavedState>) state[1];
        _offset = ((Integer) state[2]).intValue();
        _size = ((Integer) state[3]).intValue();
        _var = (String) state[4];
        _value = state[5];
    }

    @Override
    public Object saveState(FacesContext faces)
    {
        Object[] state = new Object[6];
        state[0] = super.saveState(faces);
        state[1] = _childState;
        state[2] = new Integer(_offset);
        state[3] = new Integer(_size);
        state[4] = _var;
        state[5] = _value;
        return state;
    }

    @Override
    public void encodeChildren(FacesContext faces) throws IOException
    {
        if (!isRendered())
        {
            return;
        }
        
        setDataModel(null);
        
        if (!_keepSaved(faces))
        {
            _childState = null;
        }
        
        process(faces, PhaseId.RENDER_RESPONSE);
    }

    @Override
    public boolean getRendersChildren()
    {
        if (getRendererType() != null)
        {
            Renderer renderer = getRenderer(getFacesContext());
            if (renderer != null)
            {
                return renderer.getRendersChildren();
            }
        }
        
        return true;
    }
}
