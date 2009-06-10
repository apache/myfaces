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
package org.apache.myfaces.component.visit;

import javax.faces.component.visit.*;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import javax.faces.component.NamingContainer;
import javax.faces.component.UIComponent;
import javax.faces.component.UINamingContainer;
import javax.faces.context.FacesContext;

/**
 * Implementation of a PartialVisitContext!
 * The partial visit context works on a subset of ids and has several
 * differences to a Full visit context!
 * First getIds returns a mutable set
 * Secondly  getSubtreeIdsToVisit returns either a valid id subset or ALL_IDS
 */
public class PartialVisitContext extends VisitContext {

    FacesContext _facesContext;
    Set<VisitHint> _hints;
    Set<String> _visited = new HashSet<String>();
    PartialVisitIdProxy _idProxy = null;

    public PartialVisitContext(FacesContext context, Collection<VisitHint> hints, Collection<String> ids) {
        super();
        _facesContext = context;
        _hints =   (hints == null) ? EnumSet.noneOf(VisitHint.class):EnumSet.copyOf(hints);
        _idProxy = new PartialVisitIdProxy(UINamingContainer.getSeparatorChar(context), ids);

    }

    private void assertNamingContainer(UIComponent component) throws IllegalArgumentException {
        if (!(component instanceof NamingContainer)) {
            throw new IllegalArgumentException("Component  " + component.getClientId(_facesContext) + "must be of type NamingContainer");
        }
    }

    @Override
    public FacesContext getFacesContext() {
        return _facesContext;
    }

    @Override
    public Set<VisitHint> getHints() {
        return _hints;
    }

    /**
     * Returns the collection of ids, as mutable collection!
     * (In our case our internal PartialVisitProxy doing the heavy lifting)
     */
    @Override
    public Collection<String> getIdsToVisit() {
        return _idProxy;
    }

    /**
     * returns all visit ids below the component given
     * the component has to be a naming container!
     * To speed things up we use an inverse cache,
     * the component itself does not have to be in the visting list
     * it can even be the body element!
     *
     *
     * @param component
     * @return
     */
    @Override
    public Collection<String> getSubtreeIdsToVisit(UIComponent component) {
        assertNamingContainer(component);

        //ok this is suboptimal, if we have forced ids we have to visit all components!
        if (_idProxy.getForcedIds().size() > 0) {
            return VisitContext.ALL_IDS;
        }
        //the alternative would be to descend into the subtrees
        //but given the spec this method just gives hints whether
        //we can shortcut or not!
        //given this method might be called many times, it is not really justified to make a deep scan here
        //it probably is better just to send an all have to be visited instead!


        //an alternative might be to add the deep scan ids to the result set every time
        //so that a component can shortcut if they are found!
        //but this method is safer because the component then cannot make the assumption
        //that a subtree id might be present while in reality it is not!
        //the main issue here is simply that we cannot rely on the positions of our deep scan ids
        //another one is performance, we have to treewalk here which is not optimal! especially
        //since this method might be called many times

        //I have to check this out if concatenation of the _deepScan Ids is possible in this case!
        //if we can do it that we the speedup definitely will be bigger than

        //also the hint system is ignored in our case, because UIComponent.isVisitable takes
        //care of it!

        Collection<String> retVal = new HashSet<String>(_idProxy.size());

        String clientId = component.getClientId(_facesContext);
        Collection<String> visitableComponents = _idProxy.getInverseCache().get(clientId);
        if (visitableComponents == null || visitableComponents.size() == 0) {
            return Collections.emptySet();
        }

        //not ideal still faster than influencing the visitableComponents data structure on the invoke side!
        for (String componentId : visitableComponents) {
            if (!_visited.contains(componentId)) {
                retVal.add(componentId);
            }
        }

        //this seems weird at the first look because it is not handled what has to happen
        //if someone removed or adds an id from the outside, what happens is following
        //that the caches are updated on the fly if this happens, the cache itself
        //is not thread safe because, servlets normally can expect to be in a single thread
        //context unless you work on singletons which is not the case in the case of a context!

        return retVal;
    }

    @Override
    public VisitResult invokeVisitCallback(UIComponent component, VisitCallback callback) {

        String clientId = component.getClientId();
        int idSize = _idProxy.size();


        //premature termination condition
        if (_visited.size() == idSize) {
            return VisitResult.COMPLETE;
        }

        //accept in case of not processing or processed
        boolean notProcessing = _visited.contains(clientId) ||
                !_idProxy.contains(clientId);

        if (notProcessing) {
            return VisitResult.ACCEPT;
        } else {
            _visited.add(clientId);
            VisitResult retVal = callback.visit(this, component);
            return retVal;
        }
    }
}
