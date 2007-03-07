/*
 * Copyright 2004 The Apache Software Foundation.
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

import javax.faces.FacesException;
import javax.faces.context.ExternalContext;

import java.io.UnsupportedEncodingException;
import java.util.Locale;

/**
 * see Javadoc of <a href="http://java.sun.com/javaee/javaserverfaces/1.2/docs/api/index.html">JSF Specification</a>
 *
 * @author Manfred Geiler (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public abstract class ViewHandler
{
    public static final String CHARACTER_ENCODING_KEY = "javax.faces.request.charset";
    public static final String DEFAULT_SUFFIX_PARAM_NAME = "javax.faces.DEFAULT_SUFFIX";
    public static final String DEFAULT_SUFFIX = ".jsp";

    /**
     * @since JSF 1.2
     */
    public String calculateCharacterEncoding(javax.faces.context.FacesContext context)
    {
    	String _encoding = null;
    	ExternalContext externalContext = context.getExternalContext();
        String _contentType = (String) externalContext.getRequestHeaderMap().get("Content-Type");
    	int _indexOf = _contentType == null ? -1 :_contentType.indexOf("charset");
    	if(_indexOf != -1)
    	{
			String _tempEnc =_contentType.substring(_indexOf); //charset=UTF-8 
    		_encoding = _tempEnc.substring(_tempEnc.indexOf("=")+1); //UTF-8
    	}
    	else 
    	{
    		boolean _sessionAvailable = externalContext.getSession(false) != null;
    		if(_sessionAvailable)
    		{
    			Object _sessionParam = externalContext.getSessionMap().get(CHARACTER_ENCODING_KEY); 
    			if (_sessionParam != null)
    			{
    				_encoding = _sessionParam.toString();
    			}
    		}
    	}
    	
    	return _encoding;
    }
    
    public abstract Locale calculateLocale(javax.faces.context.FacesContext context);

    public abstract String calculateRenderKitId(javax.faces.context.FacesContext context);

    public abstract javax.faces.component.UIViewRoot createView(javax.faces.context.FacesContext context,
                                                                String viewId);

    public abstract String getActionURL(javax.faces.context.FacesContext context,
                                        String viewId);

    public abstract String getResourceURL(javax.faces.context.FacesContext context,
                                          String path);
    
    /**
     * Method must be called by the JSF impl at the beginning of Phase <i>Restore View</i> of the JSF
     * lifecycle.
     * 
     * @since JSF 1.2
     */
    public void initView(javax.faces.context.FacesContext context) throws FacesException
    {
    	String _encoding = this.calculateCharacterEncoding(context);
    	if(_encoding != null)
    	{
    		try
    		{
        		context.getExternalContext().setRequestCharacterEncoding(_encoding);
    		}
    		catch(UnsupportedEncodingException uee)
    		{
    			throw new FacesException(uee);
    		}
    	}
    }
    
    public abstract void renderView(javax.faces.context.FacesContext context,
                                    javax.faces.component.UIViewRoot viewToRender)
            throws java.io.IOException,
            FacesException;

    public abstract javax.faces.component.UIViewRoot restoreView(javax.faces.context.FacesContext context,
                                                                 String viewId);

    public abstract void writeState(javax.faces.context.FacesContext context)
            throws java.io.IOException;
}
