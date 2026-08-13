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
package org.apache.myfaces.context;

import java.io.IOException;

/**
 * Thrown when a Facelet resource path is rejected by the security or mapping
 * validation checks in the Facelet factory.
 * <p>
 * The {@link Reason} enum identifies which specific check triggered the rejection,
 * allowing callers to distinguish between a blocked URI scheme, a path that escapes
 * the application base, a protected WEB-INF config file, and an extension that is
 * not a configured Facelet suffix.
 */
public class InvalidFileException extends IOException
{
    private static final long serialVersionUID = 1L;

    /**
     * Categorises why a resource path was rejected.
     */
    public enum Reason
    {
        /** The path contains a remote or otherwise disallowed URI scheme (e.g. {@code http:}, {@code ftp:}). */
        DISALLOWED_SCHEME,

        /** The resolved URL escapes the application's WAR/EAR base directory (path-traversal attempt). */
        PATH_TRAVERSAL,

        /** The path targets an XML configuration file under {@code WEB-INF/} (e.g. {@code web.xml}). */
        // WEBINF_CONFIG_FILE, // Likely not needed?

        /** The file extension is not among the configured Facelet suffixes. */
        INVALID_EXTENSION
    }

    private final Reason reason;

    /**
     * Constructs an {@code InvalidFileException} with the given rejection reason and detail message.
     *
     * @param reason  the specific cause of the rejection; must not be {@code null}
     * @param message a human-readable description of the rejected path and why it was blocked
     */
    public InvalidFileException(Reason reason, String message)
    {
        super(message);
        this.reason = reason;
    }

    /**
     * Constructs an {@code InvalidFileException} with the given rejection reason, detail message,
     * and underlying cause.
     *
     * @param reason  the specific cause of the rejection; must not be {@code null}
     * @param message a human-readable description of the rejected path and why it was blocked
     * @param cause   the original exception that triggered this rejection, or {@code null}
     */
    public InvalidFileException(Reason reason, String message, Throwable cause)
    {
        super(message);
        initCause(cause);
        this.reason = reason;
    }

    /**
     * Returns the reason this file path was considered invalid.
     *
     * @return the rejection {@link Reason}; never {@code null}
     */
    public Reason getReason()
    {
        return reason;
    }
}
