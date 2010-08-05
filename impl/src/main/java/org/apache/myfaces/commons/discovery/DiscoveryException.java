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
package org.apache.myfaces.commons.discovery;


/**
 * <p>An exception that is thrown only if a suitable service
 * instance cannot be created by <code>ServiceFactory</code></p>
 * 
 * <p>Copied from LogConfigurationException<p>
 *
 * @author Craig R. McClanahan
 * @version $Revision: 480374 $ $Date: 2006-11-28 22:33:25 -0500 (Mar, 28 Nov 2006) $
 */
public class DiscoveryException extends RuntimeException {


    /**
     * Construct a new exception with <code>null</code> as its detail message.
     */
    public DiscoveryException() {
        super();
    }

    /**
     * Construct a new exception with the specified detail message.
     *
     * @param message The detail message
     */
    public DiscoveryException(String message) {
        super(message);
    }

    /**
     * Construct a new exception with the specified cause and a derived
     * detail message.
     *
     * @param cause The underlying cause
     */
    public DiscoveryException(Throwable cause) {
        this((cause == null) ? null : cause.toString(), cause);
    }

    /**
     * Construct a new exception with the specified detail message and cause.
     *
     * @param message The detail message
     * @param cause The underlying cause
     */
    public DiscoveryException(String message, Throwable cause) {
        super(message);
        this.cause = cause; // Two-argument version requires JDK 1.4 or later
    }

    /**
     * The underlying cause of this exception.
     */
    protected Throwable cause = null;

    /**
     * Return the underlying cause of this exception (if any).
     */
    public Throwable getCause() {
        return this.cause;
    }
    
    public String toString() {
        String ls = System.getProperty("line.separator");
        String str = super.toString();
        if (cause != null) {
            str = str + ls +
                  "*****" + ls +
                  stackToString(cause);
        }
        return str;
    }

    private static String stackToString(Throwable e){
      java.io.StringWriter sw= new java.io.StringWriter(1024); 
      java.io.PrintWriter pw= new java.io.PrintWriter(sw); 
      e.printStackTrace(pw);
      pw.close();
      return sw.toString();
    }
}
