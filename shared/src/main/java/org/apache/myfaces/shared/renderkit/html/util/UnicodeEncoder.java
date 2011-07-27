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
package org.apache.myfaces.shared.renderkit.html.util;

/**
 * Converts characters outside of latin-1 set in a string to numeric character references.
 * 
 */
public abstract class UnicodeEncoder
{
    /**
     * Encodes the given string, so that it can be used within a html page.
     * @param string the string to convert
     */
    public static String encode (String string)
    {
        if (string == null)
        {
            return "";
        }

        StringBuilder sb = null;
        char c;
        for (int i = 0; i < string.length (); ++i)
        {
            c = string.charAt(i);
            if (((int)c) >= 0x80)
            {
                if( sb == null ){
                    sb = new StringBuilder( string.length()+4 );
                    sb.append( string.substring(0,i) );
                }
                //encode all non basic latin characters
                sb.append("&#");
                sb.append((int)c);
                sb.append(";");
            }
            else if( sb != null )
            {
                sb.append(c);
            }
        }

        return sb != null ? sb.toString() : string;
    }


}
