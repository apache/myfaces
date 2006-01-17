/*
 * Copyright 2004-2006 The Apache Software Foundation.
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

package org.apache.myfaces.mock;

import java.io.IOException;

import javax.faces.application.StateManager;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;

public class MockStateManager extends StateManager {

	public MockStateManager() {
		super();
		// TODO Auto-generated constructor stub
	}

	public SerializedView saveSerializedView(FacesContext context) {
		// TODO Auto-generated method stub
		return null;
	}

	protected Object getTreeStructureToSave(FacesContext context) {
		// TODO Auto-generated method stub
		return null;
	}

	protected Object getComponentStateToSave(FacesContext context) {
		// TODO Auto-generated method stub
		return null;
	}

	public void writeState(FacesContext context, SerializedView state)
			throws IOException {
		// TODO Auto-generated method stub

	}

	public UIViewRoot restoreView(FacesContext context, String viewId,
			String renderKitId) {
		// TODO Auto-generated method stub
		return null;
	}

	protected UIViewRoot restoreTreeStructure(FacesContext context,
			String viewId, String renderKitId) {
		// TODO Auto-generated method stub
		return null;
	}

	protected void restoreComponentState(FacesContext context,
			UIViewRoot viewRoot, String renderKitId) {
		// TODO Auto-generated method stub

	}

}
