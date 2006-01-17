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
package javax.faces;

/**
 * see Javadoc of <a href="http://java.sun.com/j2ee/javaserverfaces/1.1_01/docs/api/index.html">JSF Specification</a>
 *
 * @author Manfred Geiler (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class FacesException
        extends RuntimeException
{
	private static final long serialVersionUID = 6592152487577416317L;

	public FacesException()
    {
        super();
    }

    public FacesException(Throwable cause)
    {
        super(cause);
    }

    public FacesException(String message)
    {
        super(message);
    }

    public FacesException(String message,
                          Throwable cause)
    {
        super(message, cause);
    }

    public Throwable getCause()
    {
        return super.getCause();
    }
}
