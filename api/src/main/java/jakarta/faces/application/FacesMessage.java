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
package jakarta.faces.application;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.apache.myfaces.core.api.shared.lang.Assert;

/**
 *<p>
 * <code>FacesMessage</code> represents a single validation (or other) message, which is typically associated with a
 * particular component in the view. A {@link FacesMessage} instance may be created based on a specific messageId. The
 * specification defines the set of messageIds for which there must be {@link FacesMessage} instances.
 * </p>
 * 
 * The implementation must take the following steps when creating FacesMessage instances given a messageId: 
 * <ul>
 * <li>Call
 * {@link Application#getMessageBundle()}. If <code>non-null</code>, locate the named <code>ResourceBundle</code>, using
 * the <code>Locale</code> from the current {@link jakarta.faces.component.UIViewRoot} and see if it has a value for 
 * the argument
 * <code>messageId</code>. If it does, treat the value as the <code>summary</code> of the {@link FacesMessage}. If it
 * does not, or if {@link Application#getMessageBundle()} returned null, look in the ResourceBundle named by the value
 * of the constant {@link #FACES_MESSAGES} and see if it has a value for the argument messageId. If it does, treat the
 * value as the summary of the <code>FacesMessage</code>. If it does not, there is no initialization information for the
 * <code>FacesMessage</code> instance.</li>
 * <li>In all cases, if a <code>ResourceBundle</code> hit is found for the
 * <code>{messageId}</code>, look for further hits under the key <code>{messageId}_detail</code>. Use this value, if
 * present, as the <code>detail</code> for the returned <code>FacesMessage</code>.</li> 
 * <li>Make sure to perform any
 * parameter substitution required for the <code>summary</code> and <code>detail</code> of the <code>FacesMessage</code>
 * .</li> 
 * </ul>
 * 
 */
public class FacesMessage implements Serializable
{
    private static final long serialVersionUID = 4851488727794169611L;

    /**
     * <code>ResourceBundle</code> identifier for messages whose message identifiers are defined in the JavaServer Faces
     * specification.
     */
    public static final String FACES_MESSAGES = "jakarta.faces.Messages";

    /**
     * Message severity level indicating an informational message rather than an error.
     */
    @Deprecated(since = "5.0", forRemoval = true)
    public static final FacesMessage.Severity SEVERITY_INFO = Severity.INFO;

    /**
     * Message severity level indicating that an error might have occurred.
     */
    @Deprecated(since = "5.0", forRemoval = true)
    public static final FacesMessage.Severity SEVERITY_WARN = Severity.WARN;

    /**
     * Message severity level indicating that an error has occurred.
     */
    @Deprecated(since = "5.0", forRemoval = true)
    public static final FacesMessage.Severity SEVERITY_ERROR = Severity.ERROR;

    /**
     * Message severity level indicating that a serious error has occurred.
     */
    @Deprecated(since = "5.0", forRemoval = true)
    public static final FacesMessage.Severity SEVERITY_FATAL = Severity.FATAL;

    /**
     * Immutable <code>Lis</code> of valid {@link FacesMessage.Severity}instances, in ascending order of their ordinal
     * value.
     */
    public static final List<FacesMessage.Severity> VALUES;

    /**
     * Immutable <code>Map</code> of valid {@link FacesMessage.Severity}instances, keyed by name.
     */
    public static final Map<String, FacesMessage.Severity> VALUES_MAP;

    static
    {
        Map<String, FacesMessage.Severity> map = new HashMap<>(7);
        map.put(Severity.INFO.toString(), Severity.INFO);
        map.put(Severity.SUCCESS.toString(), Severity.SUCCESS);
        map.put(Severity.WARN.toString(), Severity.WARN);
        map.put(Severity.ERROR.toString(), Severity.ERROR);
        map.put(Severity.FATAL.toString(), Severity.FATAL);
        VALUES_MAP = Collections.unmodifiableMap(map);

        List<FacesMessage.Severity> severityList = new ArrayList<>(map.values());
        Collections.sort(severityList); // the Faces spec requires it to be sorted
        VALUES = Collections.unmodifiableList(severityList);
    }

    private transient FacesMessage.Severity severity;  // transient, b/c FacesMessage.Severity is not Serializable
    private String summary;
    private String detail;
    private boolean rendered;

    /**
     *Construct a new {@link FacesMessage} with no initial values. The severity is set to Severity.INFO.
     */
    public FacesMessage()
    {
        severity = SEVERITY_INFO;
        rendered = false;
    }

    /**
     * Construct a new {@link FacesMessage} with just a summary. The detail is null, the severity is set to
     * <code>Severity.INFO</code>.
     */
    public FacesMessage(String summary)
    {
        this.summary = summary;
        this.severity = SEVERITY_INFO;
        this.rendered = false;
    }

    /**
     * Construct a new {@link FacesMessage} with the specified initial values. The severity is set to Severity.INFO.
     * 
     * @param summary
     *            - Localized summary message text
     * @param detail
     *            - Localized detail message text
     */
    public FacesMessage(String summary, String detail)
    {
        this.summary = summary;
        this.detail = detail;
        this.severity = SEVERITY_INFO;
        this.rendered = false;
    }

    /**
     * Construct a new {@link FacesMessage}with the specified initial values.
     * 
     * @param severity
     *            - the severity
     * @param summary
     *            - Localized summary message text
     * @param detail
     *            - Localized detail message text
     */
    public FacesMessage(FacesMessage.Severity severity, String summary, String detail)
    {
        Assert.notNull(severity, "severity");

        this.severity = severity;
        this.summary = summary;
        this.detail = detail;
        this.rendered = false;
    }

    /**
     * 
     * @return
     */
    public FacesMessage.Severity getSeverity()
    {
        return this.severity;
    }

    /**
     * Return the severity level.
     */
    public void setSeverity(FacesMessage.Severity severity)
    {
        Assert.notNull(severity, "severity");

        this.severity = severity;
    }

    /**
     * Return the localized summary text.
     */
    public String getSummary()
    {
        return summary;
    }

    /**
     * Set the localized summary text.
     * 
     * @param summary
     *            - The new localized summary text
     */
    public void setSummary(String summary)
    {
        this.summary = summary;
    }

    /**
     * 
     * @return
     */
    public String getDetail()
    {
        if (this.detail == null)
        {
            // Javadoc:
            // If no localized detail text has been defined for this message, return the localized summary text instead
            return summary;
        }
        return this.detail;
    }

    /**
     * Set the localized detail text.
     * 
     * @param detail
     *            - The new localized detail text
     */
    public void setDetail(String detail)
    {
        this.detail = detail;
    }

    public boolean isRendered()
    {
        return this.rendered;
    }

    public void rendered()
    {
        this.rendered = true;
    }

    /**
     * @since 4.1
     */
    @Override
    public int hashCode()
    {
        return Objects.hash(severity, summary, detail);
    }

    /**
     * @since 4.1
     */
    @Override
    public boolean equals(Object object)
    {
        return (object == this) || (object != null && object.getClass() == getClass()
            && Objects.equals(severity, ((FacesMessage) object).severity)
            && Objects.equals(summary, ((FacesMessage) object).summary)
            && Objects.equals(detail, ((FacesMessage) object).detail));
    }

    /**
     * @since 4.1
     */
    @Override
    public String toString()
    {
        return getClass().getSimpleName() + "["
            + "severity='" + severity + "', "
            + "summary='" + summary + "', "
            + "detail='" + detail + "']"
        ;
    }
    
    private void writeObject(ObjectOutputStream out) throws IOException
    {
        out.defaultWriteObject();  // write summary, detail, rendered
        out.writeInt(this.severity.ordinal());  // FacesMessage.Severity is not Serializable, write ordinal only
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
    {
        in.defaultReadObject();  // read summary, detail, rendered

        // FacesMessage.Severity is not Serializable, read ordinal and get related FacesMessage.Severity
        int severityOrdinal = in.readInt();
        this.severity = VALUES.get(severityOrdinal);
    }

    /**
     * <p>
     * <span class="changed_modified_5_0">Enum</span> used to represent message severity levels.
     * </p>
     */
    public enum Severity
    {
        /**
         * <p>
         * Message severity level indicating an informational message rather than an error.
         * </p>
         * <p>
         * Historically, this was a constant class known as FacesMessage.SEVERITY_INFO.
         * </p>
         * @since 5.0
         */
        INFO,

        /**
         * <p>
         * Message severity level indicating an success message rather than an error.
         * </p>
         * @since 5.0
         */
        SUCCESS,

        /**
         * <p>
         * Message severity level indicating that an error might have occurred.
         * </p>
         * <p>
         * Historically, this was a constant class known as FacesMessage.SEVERITY_WARN.
         * </p>
         * @since 5.0
         */
        WARN,

        /**
         * <p>
         * Message severity level indicating that an error has occurred.
         * </p>
         * <p>
         * Historically, this was a constant class known as FacesMessage.SEVERITY_ERROR.
         * </p>
         * @since 5.0
         */
        ERROR,

        /**
         * <p>
         * Message severity level indicating that a serious error has occurred.
         * </p>
         * <p>
         * Historically, this was a constant class known as FacesMessage.SEVERITY_FATAL.
         * </p>
         * @since 5.0
         */
        FATAL;

        /**
         * <p>
         * Return a String representation of this {@link FacesMessage.Severity} instance.
         * </p>
         */
        @Override
        public String toString()
        {
            return name() + ' ' + ordinal();
        }

        // backward compatibility
        public int getOrdinal()
        {
            return ordinal();
        }
    }

}
