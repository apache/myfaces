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
package javax.faces.validator;

import javax.faces.FacesException;
import javax.faces.application.FacesMessage;

/**
 * see Javadoc of <a href="http://java.sun.com/javaee/javaserverfaces/1.2/docs/api/index.html">JSF Specification</a>
 *
 * @author Manfred Geiler (latest modification by $Author$)
 * @author Thomas Spiegl
 * @version $Revision$ $Date$
 */
public class ValidatorException
        extends FacesException
{
    private static final long serialVersionUID = 5965885122446047949L;
    private FacesMessage _facesMessage;

    public ValidatorException(FacesMessage message)
    {
        super(facesMessageToString(message));
        _facesMessage = message;
    }

    public ValidatorException(FacesMessage message,
                              Throwable cause)
    {
        super(facesMessageToString(message), cause);
        _facesMessage = message;
    }

    public FacesMessage getFacesMessage()
    {
        return _facesMessage;

    }

    private static String facesMessageToString(FacesMessage message)
    {
        final String summary = message.getSummary();
        final String detail = message.getDetail();
        
        if (summary != null)
        {
            if (detail != null)
            {
                return summary + ": " + detail;
            }
            
            return summary;
        }
        
        return detail != null ? detail : "";
    }

}
