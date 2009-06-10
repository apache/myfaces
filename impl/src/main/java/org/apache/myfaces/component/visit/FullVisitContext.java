/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */
package org.apache.myfaces.component.visit;

import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import javax.faces.component.NamingContainer;
import javax.faces.component.UIComponent;
import javax.faces.component.visit.VisitCallback;
import javax.faces.component.visit.VisitContext;
import javax.faces.component.visit.VisitHint;
import javax.faces.component.visit.VisitResult;
import javax.faces.context.FacesContext;

/**
 *
 * @author Werner Punz (latest modification by $Author$)
 * @version $Rev$ $Date$
 * 
 *
 * Another visit context for the full Tree Traversal!
 *
 * This visit context is rather simplistic because full tree
 * traversal means always all components have to be visited!
 * 
 */
public class FullVisitContext extends VisitContext {

    FacesContext _facesContext;
    Set<VisitHint> _hints;
    Set<String> _ids;
    Set<String> _visited = new HashSet<String>();

    public FullVisitContext(FacesContext context, Set<VisitHint> hints) {
        _facesContext = context;
        _hints = (hints != null && hints.size() > 0) ?  EnumSet.copyOf(hints) : EnumSet.noneOf(VisitHint.class);
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

    @Override
    public Collection<String> getIdsToVisit() {
        return VisitContext.ALL_IDS;
    }

    @Override
    public Collection<String> getSubtreeIdsToVisit(UIComponent component) {
        assertNamingContainer(component);

        return VisitContext.ALL_IDS;
    }

    @Override
    public VisitResult invokeVisitCallback(UIComponent component, VisitCallback callback) {

        String clientId = component.getClientId();

        if (_visited.contains(clientId)) {
            return VisitResult.ACCEPT;
        } else {
            _visited.add(clientId);
            VisitResult retVal = callback.visit(this, component);
            return retVal;
        }

    }
}
