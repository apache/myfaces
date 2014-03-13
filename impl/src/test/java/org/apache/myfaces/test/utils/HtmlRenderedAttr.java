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
package org.apache.myfaces.test.utils;

public class HtmlRenderedAttr
{
    public static final int RENDERED_MORE_TIMES_THAN_EXPECTED = 1;
    public static final int RENDERED_LESS_TIMES_THAN_EXPECTED = 2;
    
    private String name;
    private String value;
    private String expectedHtml;
    private boolean renderSuccessful;
    private int errorCode;
    private int expectedOccurrences;
    private int actualOccurrences;
    
    public HtmlRenderedAttr(String name) {
        this(name, name, name + "=\"" + name + "\"");
        expectedOccurrences = 1;
    }
    
    public HtmlRenderedAttr(String name, int expectedOccurences) {
        this(name);
        this.expectedOccurrences = expectedOccurences;
    }
    
    public HtmlRenderedAttr(String name, String value)
    {
        this.name = name;
        this.value = value;
        this.expectedHtml = " "+name+"=\""+value+"\"";
        
        renderSuccessful = false;
        expectedOccurrences = 1;
    }
    
    /**
     * Represents an attribute of a component that is expected to be
     * rendered into html
     * @param name  The name of the attribute.
     * @param value The value of the attribute.
     * @param expectedHtml The expected html output for this attribute.  
     *     E.g. name="value".
     */
    public HtmlRenderedAttr(String name, String value, String expectedHtml) {
        this.name = name;
        this.value = value;
        this.expectedHtml = expectedHtml;
        
        renderSuccessful = false;
        expectedOccurrences = 1;
    }
    
    public HtmlRenderedAttr(String name, String value, String expectedHtml, int occurances) {
        this(name, value, expectedHtml);
        this.expectedOccurrences = occurances;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getValue()
    {
        return value;
    }

    public void setValue(String value)
    {
        this.value = value;
    }

    public String getExpectedHtml()
    {
        return expectedHtml;
    }

    public void setExpectedHtml(String expectedHtml)
    {
        this.expectedHtml = expectedHtml;
    }

    /**
     * This returns the result of the rendering of the attribute.
     * @return True if the rendered html output of this attribute is
     * the same as expectedHtml.  False if either the attribute was not
     * rendered, it was rendered multiple times, or the rendered html
     * is different from expectedHtml.
     */
    public boolean isRenderSuccessful()
    {
        return renderSuccessful;
    }

    public void setRenderSuccessful(boolean renderSuccessful)
    {
        this.renderSuccessful = renderSuccessful;
    }

    public int getErrorCode()
    {
        return errorCode;
    }

    public void setErrorCode(int errorCode)
    {
        this.errorCode = errorCode;
        setRenderSuccessful(false);
    }
    
    public void increaseActualOccurrences() {
        actualOccurrences++;
    }
    
    public int getActualOccurrences() {
        return this.actualOccurrences;
    }
    
    public int getExpectedOccurrences() {
        return this.expectedOccurrences;
    }
}
