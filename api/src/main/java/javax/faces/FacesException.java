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
package javax.faces;

/**
 * see Javadoc of <a href="http://java.sun.com/j2ee/javaserverfaces/1.1_01/docs/api/index.html">JSF Specification</a>
 * 
 * 
 * @author Manfred Geiler (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class FacesException
        extends RuntimeException
{
    private static final long serialVersionUID = 1L;

    /**
     * The cause of this exception, i.e. the Throwable that caused this exception to be thrown.
     *<p>
     * Note that JSF1.1 is required to be compatible with java 1.3; so no "exception chaining"
     * support can be assumed in the base RuntimeException class. Here it is emulated so that
     * in a java-1.4 (or later) environment this class works like other exceptions.
     */
    private Throwable cause;

    public FacesException()
    {
        super();
    }

    public FacesException(Throwable cause)
    {
        this.cause = cause;
    }

    public FacesException(String message)
    {
        super(message);
    }

    public FacesException(String message,
                          Throwable cause)
    {
        super(message);
        this.cause = cause;
    }

    public Throwable getCause()
    {
        return cause;
    }
}
