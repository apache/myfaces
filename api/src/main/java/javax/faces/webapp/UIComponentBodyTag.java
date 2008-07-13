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
package javax.faces.webapp;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.BodyContent;
import javax.servlet.jsp.tagext.BodyTag;

/**
 * see Javadoc of <a href="http://java.sun.com/j2ee/javaserverfaces/1.1_01/docs/api/index.html">JSF Specification</a>
 *
 * @author Manfred Geiler (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public abstract class UIComponentBodyTag
    extends UIComponentTag
    implements BodyTag
{
    // API field
    protected BodyContent bodyContent;

    
    public int doAfterBody()
            throws JspException
    {
        return getDoAfterBodyValue();
    }

    public void doInitBody()
            throws JspException
    {
    }

    public void release()
    {
        super.release();
        bodyContent = null;
    }

    public BodyContent getBodyContent()
    {
        return bodyContent;
    }

    public void setBodyContent(BodyContent bodyContent)
    {
        this.bodyContent = bodyContent;
    }

    public JspWriter getPreviousOut()
    {
        return bodyContent.getEnclosingWriter();
    }

    protected int getDoStartValue()
            throws JspException
    {
        return BodyTag.EVAL_BODY_BUFFERED;
    }

    protected int getDoAfterBodyValue()
            throws JspException
    {
        return BodyTag.SKIP_BODY;
    }

}
