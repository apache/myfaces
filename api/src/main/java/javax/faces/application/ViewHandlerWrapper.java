/*
 * Copyright 2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package javax.faces.application;

import java.io.IOException;
import java.util.Locale;
import javax.faces.FacesException;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;

/**
 * see Javadoc of <a href="http://java.sun.com/j2ee/javaserverfaces/1.2/docs/api/index.html">JSF Specification</a>
 *
 * @author Stan Silvert
 */
public abstract class ViewHandlerWrapper extends ViewHandler {
    
    @Override
	public String calculateCharacterEncoding(FacesContext context) {
		return getWrapped().calculateCharacterEncoding(context);
	}

	@Override
	public void initView(FacesContext context) throws FacesException {
		getWrapped().initView(context);
	}

    protected abstract ViewHandler getWrapped();
    
    public void renderView(FacesContext context, UIViewRoot viewToRender) throws IOException, FacesException {
        getWrapped().renderView(context, viewToRender);
    }

    public void writeState(FacesContext context) throws IOException {
        getWrapped().writeState(context);
    }

    public String calculateRenderKitId(FacesContext context) {
        return getWrapped().calculateRenderKitId(context);
    }

    public Locale calculateLocale(FacesContext context) {
        return getWrapped().calculateLocale(context);
    }

    public UIViewRoot restoreView(FacesContext context, String viewId) {
        return getWrapped().restoreView(context, viewId);
    }

    public String getResourceURL(FacesContext context, String path) {
        return getWrapped().getResourceURL(context, path);
    }

    public String getActionURL(FacesContext context, String viewId) {
        return getWrapped().getActionURL(context, viewId);
    }

    public UIViewRoot createView(FacesContext context, String viewId) {
        return getWrapped().createView(context, viewId);
    }
    
}
