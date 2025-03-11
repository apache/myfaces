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
package org.apache.myfaces.renderkit.html.util;

import jakarta.faces.context.FacesContext;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import org.apache.myfaces.core.api.shared.lang.SharedStringBuilder;
import org.apache.myfaces.util.lang.FastByteArrayOutputStream;

/**
 * Converts Strings so that they can be used within HTML-Code.
 */
public abstract class HTMLEncoder
{
    private static final String SB_ENCODE = HTMLEncoder.class.getName() + "#SB_ENCODE";
    private static final String SB_ENCODE_URI_ATTR = HTMLEncoder.class.getName() + "#SB_ENCODE_URI_ATTR";
    private static final String SB_ENCODE_URI_QUERY = HTMLEncoder.class.getName() + "#SB_ENCODE_URI_QUERY";
    private static final String SB_ENCODE_PERCENT = HTMLEncoder.class.getName() + "#SB_ENCODE_PERCENT";

    /**
     * Variant of {@link #encode} where encodeNewline is false and encodeNbsp is true.
     * 
     * @param context
     * @param string the string to convert
     * 
     * @return the encoded string
     */
    public static String encode(FacesContext context, String string)
    {
        return encode(context, string, false, true);
    }

    /**
     * Variant of {@link #encode} where encodeNbsp is true.
     * 
     * @param context
     * @param string the string to convert
     * @param encodeNewline if true newline characters are converted to &lt;br&gt;'s
     * 
     * @return the encoded string
     */
    public static String encode(FacesContext context, String string, boolean encodeNewline)
    {
        return encode(context, string, encodeNewline, true);
    }

    /**
     * Variant of {@link #encode} where encodeNbsp and encodeNonLatin are true 
     * 
     * @param context
     * @param string the string to convert
     * @param encodeNewline if true newline characters are converted to &lt;br&gt;'s
     * @param encodeSubsequentBlanksToNbsp if true subsequent blanks are converted to &amp;nbsp;'s
     * 
     * @return the encoded string
     */
    public static String encode(FacesContext context, String string, boolean encodeNewline,
            boolean encodeSubsequentBlanksToNbsp)
    {
        return encode(context, string, encodeNewline, encodeSubsequentBlanksToNbsp, true);
    }

    /**
     * Encodes the given string, so that it can be used within a html page.
     * 
     * @param context
     * @param string the string to convert
     * @param encodeNewline if true newline characters are converted to &lt;br&gt;'s
     * @param encodeSubsequentBlanksToNbsp if true subsequent blanks are converted to &amp;nbsp;'s
     * @param encodeNonLatin if true encode non-latin characters as numeric character references
     * 
     * @return the encoded string
     */
    public static String encode(FacesContext context, String string,
                                boolean encodeNewline, boolean encodeSubsequentBlanksToNbsp, boolean encodeNonLatin)
    {
        if (string == null)
        {
            return "";
        }

        StringBuilder sb = null;    //create later on demand
        String app;
        char c = ' ';
        char prevC;
        int length = string.length();
        for (int i = 0; i < length; ++i)
        {
            app = null;
            prevC = c;
            c = string.charAt(i);
            
            // All characters before letters
            if ((int)c < 0x41)
            {
                switch (c)
                {
                    case '"': app = "&quot;"; break;    //"
                    case '&': app = "&amp;"; break;     //&
                    case '<': app = "&lt;"; break;      //<
                    case '>': app = "&gt;"; break;      //>
                    case ' ':
                        if (encodeSubsequentBlanksToNbsp && prevC == ' ')
                        {
                            //Space at beginning or after another space
                            app = "&#160;";
                        }
                        break;
                    case '\n':
                        if (encodeNewline)
                        {
                            app = "<br/>";
                        }
                        break;
                    default:
                        break;
                }
                // http://www.w3.org/MarkUp/html3/specialchars.html
                // From C0 extension U+0000-U+001F only U+0009, U+000A and
                // U+000D are valid control characters
                if (c <= 0x1F && c != 0x09 && c != 0x0A && c != 0x0D)
                {
                    // Ignore escape character
                    app = "";
                }
            }
            else if (encodeNonLatin && (int)c > 0x80)
            {
                 switch(c)
                 {
                    //german umlauts
                    case '\u00E4' : app = "&auml;";  break;
                    case '\u00C4' : app = "&Auml;";  break;
                    case '\u00F6' : app = "&ouml;";  break;
                    case '\u00D6' : app = "&Ouml;";  break;
                    case '\u00FC' : app = "&uuml;";  break;
                    case '\u00DC' : app = "&Uuml;";  break;
                    case '\u00DF' : app = "&szlig;"; break;

                    //misc
                    //case 0x80: app = "&euro;"; break;  sometimes euro symbol is ascii 128, should we support it?
                    case '\u20AC': app = "&euro;";  break;
                    case '\u00AB': app = "&laquo;"; break;
                    case '\u00BB': app = "&raquo;"; break;
                    case '\u00A0': app = "&#160;"; break;

                    default :
                        //encode all non basic latin characters
                        app = "&#" + ((int)c) + ';';
                    break;
                }
            }
            if (app != null)
            {
                if (sb == null)
                {
                    sb = SharedStringBuilder.get(context, SB_ENCODE, string.substring(0, i));
                }
                sb.append(app);
            }
            else
            {
                if (sb != null)
                {
                    sb.append(c);
                }
            }
        }

        if (sb == null)
        {
            return string;
        }
        else
        {
            return sb.toString();
        }
    }
    
    /**
     * Variant of {@link #encode} where encodeNewline is false and encodeNbsp is true.
     *
     * @param writer
     * @param string
     * @throws IOException 
     */
    public static void encode(Writer writer, String string) throws IOException
    {
        encode(writer, string, false, true);
    }

    /**
     * Variant of {@link #encode} where encodeNbsp is true.
     * 
     * @param writer
     * @param string
     * @param encodeNewline
     * @throws IOException 
     */
    public static void encode(Writer writer, String string, boolean encodeNewline) throws IOException
    {
        encode(writer, string, encodeNewline, true);
    }

    /**
     * Variant of {@link #encode} where encodeNbsp and encodeNonLatin are true 
     * 
     * @param writer
     * @param string
     * @param encodeNewline
     * @param encodeSubsequentBlanksToNbsp
     * @throws IOException 
     */
    public static void encode(Writer writer, String string, 
            boolean encodeNewline, boolean encodeSubsequentBlanksToNbsp) throws IOException
    {
        encode(writer, string, encodeNewline, encodeSubsequentBlanksToNbsp, true);
    }
    
    /**
     * Encodes and writes the given string, so that it can be used within a html page.
     * 
     * @param writer
     * @param string
     * @param encodeNewline
     * @param encodeSubsequentBlanksToNbsp
     * @param encodeNonLatin
     * @throws IOException 
     */
    public static void encode(Writer writer, String string,
                                 boolean encodeNewline,
                                 boolean encodeSubsequentBlanksToNbsp,
                                 boolean encodeNonLatin) throws IOException
    {
        if (string == null)
        {
            return;
        }

        int start = 0;
        String app;
        char c = ' ';
        char prevC;
        int length = string.length();
        for (int i = 0; i < length; ++i)
        {
            app = null;
            prevC = c;
            c = string.charAt(i);
            
            // All characters before letters
            if ((int)c < 0x41)
            {
                switch (c)
                {
                    case '"': app = "&quot;"; break;    //"
                    case '&': app = "&amp;"; break;     //&
                    case '<': app = "&lt;"; break;      //<
                    case '>': app = "&gt;"; break;      //>
                    case ' ':
                        if (encodeSubsequentBlanksToNbsp &&
                                prevC == ' ')
                        {
                            //Space at beginning or after another space
                            app = "&#160;";
                        }
                        break;
                    case '\n':
                        if (encodeNewline)
                        {
                            app = "<br/>";
                        }
                        break;
                    default:
                        break;
                }
                // http://www.w3.org/MarkUp/html3/specialchars.html
                // From C0 extension U+0000-U+001F only U+0009, U+000A and
                // U+000D are valid control characters
                if (c <= 0x1F && c != 0x09 && c != 0x0A && c != 0x0D)
                {
                    // Ignore escape character
                    app = "";
                }
            }
            else if (encodeNonLatin && (int)c > 0x80)
            {
                 switch(c)
                 {
                    //german umlauts
                    case '\u00E4' : app = "&auml;";  break;
                    case '\u00C4' : app = "&Auml;";  break;
                    case '\u00F6' : app = "&ouml;";  break;
                    case '\u00D6' : app = "&Ouml;";  break;
                    case '\u00FC' : app = "&uuml;";  break;
                    case '\u00DC' : app = "&Uuml;";  break;
                    case '\u00DF' : app = "&szlig;"; break;

                    //misc
                    //case 0x80: app = "&euro;"; break;  sometimes euro symbol is ascii 128, should we support it?
                    case '\u20AC': app = "&euro;";  break;
                    case '\u00AB': app = "&laquo;"; break;
                    case '\u00BB': app = "&raquo;"; break;
                    case '\u00A0': app = "&#160;"; break;

                    default :
                        //encode all non basic latin characters
                        app = "&#" + ((int)c) + ';';
                    break;
                }
            }
            if (app != null)
            {
                if (start < i)
                {
                    writer.write(string, start, i-start);
                }
                start = i+1;
                writer.write(app);
            }
        }

        if (start == 0)
        {
            writer.write(string);
        }
        else if (start < length)
        {
            writer.write(string,start,length-start);
        }
    }


    /**
     * Variant of {@link #encode} where encodeNewline is false and encodeNbsp is true.
     * 
     * @param string the string to convert
     * @param offset
     * @param length
     * @param writer 
     * 
     * @throws IOException
     */
    public static void encode(char[] string, int offset, int length, Writer writer) throws IOException
    {
        encode(string, offset, length, false, true, writer);
    }

    /**
     * Variant of {@link #encode} where encodeNbsp is true.
     * 
     * @param string the string to convert
     * @param offset
     * @param length
     * @param encodeNewline if true newline characters are converted to &lt;br&gt;'s
     * @param writer 
     * 
     * @throws IOException
     */
    public static void encode(char[] string, int offset, int length, boolean encodeNewline, Writer writer)
        throws IOException
    {
        encode(string, offset, length, encodeNewline, true, writer);
    }

    /**
     * Variant of {@link #encode} where encodeNbsp and encodeNonLatin are true
     * 
     * @param string the string to convert
     * @param offset
     * @param length
     * @param encodeNewline if true newline characters are converted to &lt;br&gt;'s
     * @param encodeSubsequentBlanksToNbsp if true subsequent blanks are converted to &amp;nbsp;'s
     * @param writer 
     * 
     * @throws IOException
     */
    public static void encode(char[] string, int offset, int length, boolean encodeNewline, 
            boolean encodeSubsequentBlanksToNbsp, Writer writer) throws IOException
    {
        encode(string, offset, length, encodeNewline, encodeSubsequentBlanksToNbsp, true, writer);
    }


    /**
     * Encodes the given string, so that it can be used within a html page.
     *
     * @param string the string to convert
     * @param offset
     * @param length
     * @param encodeNewline if true newline characters are converted to &lt;br&gt;'s
     * @param encodeSubsequentBlanksToNbsp if true subsequent blanks are converted to &amp;nbsp;'s
     * @param encodeNonLatin if true encode non-latin characters as numeric character references
     * @param writer 
     * 
     * @throws IOException
     */
    public static void encode(char[] string, int offset, int length,
                                 boolean encodeNewline,
                                 boolean encodeSubsequentBlanksToNbsp,
                                 boolean encodeNonLatin, Writer writer) throws IOException
    {
        if (string == null || length < 0 || offset >= string.length)
        {
            return;
        }
        offset = Math.max(0, offset);
        int realLength = Math.min(length, string.length - offset);

        String app;
        char c = ' ';
        char prevC;
        int start = offset;
        
        for (int i = offset; i < offset + realLength; ++i)
        {
            app = null;
            prevC = c;
            c = string[i];

            // All characters before letters
            if ((int)c < 0x41)
            {
                switch (c)
                {
                    case '"': app = "&quot;"; break;    //"
                    case '&': app = "&amp;"; break;     //&
                    case '<': app = "&lt;"; break;      //<
                    case '>': app = "&gt;"; break;      //>
                    case ' ':
                        if (encodeSubsequentBlanksToNbsp &&
                                prevC == ' ')
                        {
                            //Space at beginning or after another space
                            app = "&#160;";
                        }
                        break;
                    case '\n':
                        if (encodeNewline)
                        {
                            app = "<br/>";
                        }
                        break;
                    default:
                        break;
                }
                // http://www.w3.org/MarkUp/html3/specialchars.html
                // From C0 extension U+0000-U+001F only U+0009, U+000A and
                // U+000D are valid control characters
                if (c <= 0x1F && c != 0x09 && c != 0x0A && c != 0x0D)
                {
                    // Ignore escape character
                    app = "";
                }
            }
            else if (encodeNonLatin && (int)c > 0x80)
            {
                 switch(c)
                 {
                    //german umlauts
                    case '\u00E4' : app = "&auml;";  break;
                    case '\u00C4' : app = "&Auml;";  break;
                    case '\u00F6' : app = "&ouml;";  break;
                    case '\u00D6' : app = "&Ouml;";  break;
                    case '\u00FC' : app = "&uuml;";  break;
                    case '\u00DC' : app = "&Uuml;";  break;
                    case '\u00DF' : app = "&szlig;"; break;

                    //misc
                    //case 0x80: app = "&euro;"; break;  sometimes euro symbol is ascii 128, should we support it?
                    case '\u20AC': app = "&euro;";  break;
                    case '\u00AB': app = "&laquo;"; break;
                    case '\u00BB': app = "&raquo;"; break;
                    case '\u00A0': app = "&#160;"; break;

                    default :
                        //encode all non basic latin characters
                        app = "&#" + ((int)c) + ';';
                    break;
                }
            }
            if (app != null)
            {
                if (start < i)
                {
                    writer.write(string, start, i-start);
                }
                start = i+1;
                writer.write(app);
            }
        }

        if (start == offset)
        {
            writer.write(string, offset, realLength);
        }
        else if (start < offset+realLength)
        {
            writer.write(string,start,offset+realLength-start);
        }
    }
    
    private static final String HEX_CHARSET = "0123456789ABCDEF";
    
    private static final String UTF8 = "UTF-8";
    
    /**
     * Encode an URI, escaping or percent-encoding all required characters and
     * following the rules mentioned on RFC 3986.  
     * 
     * @param context
     * @param string
     * @param characterEncoding
     * @return
     * @throws IOException
     */
    public static String encodeURIAttribute(FacesContext context, final String string, final String characterEncoding)
        throws IOException
    {
        StringBuilder sb = null;    //create later on demand
        String app;
        char c;
        boolean endLoop = false;
        int length = string.length();
        for (int i = 0; i < length; ++i)
        {
            app = null;
            c = string.charAt(i);
            
            // This are the guidelines to be taken into account by this algorithm to encode:
            
            // RFC 2396 Section 2.4.3 Excluded US-ASCII Characters
            //
            // control     = <US-ASCII coded characters 00-1F and 7F hexadecimal>
            // space       = <US-ASCII coded character 20 hexadecimal>
            // delims      = "<" | ">" | "#" | "%" | <">
            //               %3C   %3E   %23   %25   %22
            // unwise      = "{" | "}" | "|" | "\" | "^" | "[" | "]" | "`"
            //               %7D   %7B   %7C   %5C   %5E   %5B   %5D   %60
            //
            // ".... Data corresponding to excluded characters must be escaped in order to
            // be properly represented within a URI....."
            
            // RFC 3986 Section 3.  Syntax Components
            //
            // "... The generic URI syntax consists of a hierarchical sequence of
            // components referred to as the scheme, authority, path, query, and
            // fragment.
            //
            //   URI         = scheme ":" hier-part [ "?" query ] [ "#" fragment ]
            //
            //   hier-part   = "//" authority path-abempty
            //               / path-absolute
            //               / path-rootless
            //               / path-empty
            // ...."
            
            // RFC 3986 Section 2.2:
            // Reserved characters (should not be percent-encoded)
            // reserved    = gen-delims / sub-delims
            // gen-delims  = ":" / "/" / "?" / "#" / "[" / "]" / "@"
            //               %3A   %2F   %3F   %23   %5B   %5D   %40
            // sub-delims  = "!" / "$" / "&" / "'" / "(" / ")" / "*" / "+" / "," / ";" / "="
            //               %21   %24   %26   %27   %28   %29   %2A   %2B   %2C   %3B   %3D
            
            // Note than chars "[" and "]" are mentioned as they should be escaped on RFC 2396,
            // but on the part D. Changes from RFC 2396 says about this chars (used on IPv6) 
            // "...those rules were redefined to directly specify the characters allowed...."
            // There is also other characters moved from excluded list to reserved:
            // "[" / "]" / "#"  
            
            // RFC 3986 Section 2.3:
            // "... for consistency, percent-encoded octets in the ranges of ALPHA
            // (%41-%5A and %61-%7A), DIGIT (%30-%39), hyphen (%2D), period (%2E),
            // underscore (%5F), or tilde (%7E) should not be created by URI
            // producers...."
            
            // RFC 3986 Section  3.2.2.  Host

            // host = IP-literal / IPv4address / reg-name

            // The reg-name syntax allows percent-encoded octets in order to
            // represent non-ASCII registered names in a uniform way that is
            // independent of the underlying name resolution technology.  Non-ASCII
            // characters must first be encoded according to UTF-8 [STD63], and then
            // each octet of the corresponding UTF-8 sequence must be percent-
            // encoded to be represented as URI characters.  URI producing
            // applications must not use percent-encoding in host unless it is used
            // to represent a UTF-8 character sequence.
            
            // RFC 3986 Section 3.4 Query 
            //         query       = *( pchar / "/" / "?" )
            //
            // "...  However, as query components are often used to carry identifying information 
            // in the form of "key=value" pairs and one frequently used value is a reference to
            // another URI, it is sometimes better for usability to avoid percent-encoding those characters....."
            //
            // RFC 3986 Section 2.5 Identifying Data (Apply to query section)
            //
            // When a new URI scheme defines a component that represents textual
            // data consisting of characters from the Universal Character Set [UCS],
            // the data should first be encoded as octets according to the UTF-8
            // character encoding [STD63]; then only those octets that do not
            // correspond to characters in the unreserved set should be percent-
            // encoded.  For example, the character A would be represented as "A",
            // the character LATIN CAPITAL LETTER A WITH GRAVE would be represented
            // as "%C3%80", and the character KATAKANA LETTER A would be represented
            // as "%E3%82%A2".
            //
            // RFC 3986 Section 3.5 Fragment
            //         fragment    = *( pchar / "/" / "?" )
            //
            // Note that follows the same as query
            
            // Based on the extracts the strategy to apply on this method is:
            // 
            // On scheme ":" hier-part
            //
            // Escape or percent encode chars inside :
            // 
            // - From %00 to %20, 
            // - <"> %22, "%" %25 (If there is encode of "%", there is a risk of 
            //                     duplicate encoding, encode it when we are sure 
            //                     that there are not encoded twice)
            // - "<" %3C, ">" %3E
            // - "\" %5C, "^" %5E, "`" %60 
            // - "{" %7B, "|" %7C, "}" %7D
            // - From %7F ad infinitum (characters from %100 to infinitum should not be used in this
            //   part of an URI, but it is preferred to encode it that omit it).
            //
            // The remaining characters must not be encoded
            //
            // Characters after ? or # should be percent encoding but only the necessary ones:
            //
            // - From %00 to %20 (' ' %20 could encode as +, but %20 also works, so we keep %20)
            // - <"> %22, "%" %25 (If there is encode of "%", there is a risk of 
            //                     duplicate encoding, encode it when we are sure 
            //                     that there are not encoded twice)
            // - "<" %3C, ">" %3E,
            // - "\" %5C, "^" %5E, "`" %60 
            // - "{" %7B, "|" %7C, "}" %7D
            // - From %7F ad infinitum (each character as many bytes as necessary but take into account
            //   that a single char should contain 2,3 or more bytes!. This data should be encoded 
            //   translating from the document character encoding to percent encoding, because this values
            //   could be retrieved from httpRequest.getParameter() and it uses the current character encoding
            //   for decode values)
            //
            // "&" should be encoded as "&amp;" because this link is inside an html page, and 
            // put only & is invalid in this context.

            if (   (c <= (char)0x20) || (c >= (char)0x7F) || 
                    c == '"' || c == '<' ||
                    c == '>' || c == '\\' || c == '^' || c == '`' ||
                    c == '{' || c == '|' || c == '}')
            {
                // The percent encoding on this part should be done using UTF-8 charset
                // as RFC 3986 Section 3.2.2 says.
                // Also there is a reference on 
                // http://www.w3.org/TR/html40/appendix/notes.html#non-ascii-chars
                // that recommend use of UTF-8 instead the document character encoding.
                // Jetty set by default UTF-8 (see http://jira.codehaus.org/browse/JETTY-113)
                app = percentEncode(context, c, "UTF-8");
            }
            else if (c == '%')
            {
                if (i + 2 < length)
                {
                    char c1 = string.charAt(i+1);
                    char c2 = string.charAt(i+2);
                    if ((( c1 >= '0' && c1 <='9') || (c1 >='A' && c1 <='Z') || (c1 >='a' && c1 <='z')) &&
                        (( c2 >= '0' && c2 <='9') || (c2 >='A' && c2 <='Z') || (c2 >='a' && c2 <='z')))
                    {
                        // do not percent encode, because it could be already encoded
                        // and we don't want encode it twice
                    }
                    else
                    {
                        app = percentEncode(context, c, UTF8);
                    }
                }
                else
                {
                    app = percentEncode(context, c, UTF8);
                }
            }
            else if (c == '?' || c == '#')
            {
                if (i+1 < length)
                {
                    // The remaining part of the URI are data that should be encoded
                    // using the document character encoding.
                    app = c + encodeURIQuery(context, string.substring(i+1), characterEncoding);
                    endLoop = true;
                }
            }
            else
            {
                //No encoding, just do nothing, char will be added later.
            }
                        
            if (app != null)
            {
                if (sb == null)
                {
                    sb = SharedStringBuilder.get(context, SB_ENCODE_URI_ATTR, string.substring(0, i));
                }
                sb.append(app);
            }
            else
            {
                if (sb != null)
                {
                    sb.append(c);
                }
            }
            if (endLoop)
            {
                break;
            }
        }
        if (sb == null)
        {
            return string;
        }
        else
        {
            return sb.toString();
        }
    }
    
    /**
     * Encode a unicode char value in percentEncode, decoding its bytes using a specified 
     * characterEncoding.
     * 
     * @param context
     * @param c
     * @param characterEncoding
     * @return
     */
    private static String percentEncode(FacesContext context, char c, String characterEncoding)
    {
        String app;
        if (c > (char)((short)0x007F))
        {
            //percent encode in the proper encoding to be consistent
            app = percentEncodeNonUsAsciiCharacter(context, c, characterEncoding);
        }
        else
        {
            //percent encode US-ASCII char (0x00-0x7F range)
            app = "%" + HEX_CHARSET.charAt( ((c >> 0x4) % 0x10)) + HEX_CHARSET.charAt(c % 0x10);
        }
        return app;
    }
    
    private static String percentEncodeNonUsAsciiCharacter(FacesContext context, char c, String characterEncoding)
    {
        FastByteArrayOutputStream baos = new FastByteArrayOutputStream(10);
        try
        {
            try (OutputStreamWriter writer = new OutputStreamWriter(baos, characterEncoding))
            {
                writer.write(c);
                writer.flush();
            }
        }
        catch (IOException e)
        {
            return null;
        }

        StringBuilder builder = SharedStringBuilder.get(context, SB_ENCODE_PERCENT);

        byte[] byteArray = baos.getByteArray();
        for (int i = 0; i < baos.getSize(); i++)
        {
            builder.append('%');
            builder.append(HEX_CHARSET.charAt( (( ((short) byteArray[i] & 0xFF ) >> 0x4) % 0x10)) );
            builder.append(HEX_CHARSET.charAt( ((short) byteArray[i] & 0xFF ) % 0x10));
        }

        return builder.toString();
    }

    /**
     * Encode the query part using the document charset encoding provided.
     * 
     * @param context
     * @param string
     * @param characterEncoding
     * @return
     */
    private static String encodeURIQuery(FacesContext context, final String string, final String characterEncoding)
    {
        StringBuilder sb = null;    //create later on demand
        String app;
        char c;
        boolean endLoop = false;
        int length = string.length();
        for (int i = 0; i < length; ++i)
        {
            app = null;
            c = string.charAt(i);
            
            // - From %00 to %20 (' ' %20 could encode as +, but %20 also works, so we keep %20)
            // - <"> %22 (If there is encode of "%", there is a risk of duplicate encoding, so 
            //            we make easier and omit this one)
            // - "<" %3C, ">" %3E,
            // - "\" %5C, "^" %5E, "`" %60 
            // - "{" %7B, "|" %7C, "}" %7D
            // - From %7F ad infinitum (each character as many bytes as necessary but take into account
            //   that a single char should contain 2,3 or more bytes!. This data should be encoded 
            //   translating from the document character encoding to percent encoding)
            //
            // "&" should be encoded as "&amp;" because this link is inside an html page, and 
            // put & is invalid in this context   
            
            if (   (c <= (char)0x20) || (c >= (char)0x7F) || 
                    c == '"' || c == '<' ||
                    c == '>' || c == '\\' || c == '^' || c == '`' ||
                    c == '{' || c == '|' || c == '}')
            {
                // The percent encoding on this part should be done using UTF-8 charset
                // as RFC 3986 Section 3.2.2 says
                app = percentEncode(context, c, characterEncoding);
            }
            else if (c == '%')
            {
                if (i + 2 < length)
                {
                    char c1 = string.charAt(i+1);
                    char c2 = string.charAt(i+2);
                    if ((( c1 >= '0' && c1 <='9') || (c1 >='A' && c1 <='Z') || (c1 >='a' && c1 <='z')) &&
                        (( c2 >= '0' && c2 <='9') || (c2 >='A' && c2 <='Z') || (c2 >='a' && c2 <='z')))
                    {
                        // do not percent encode, because it could be already encoded
                    }
                    else
                    {
                        app = percentEncode(context, c, characterEncoding);
                    }
                }
                else
                {
                    app = percentEncode(context, c, characterEncoding);
                }
            }
            else if (c == '&')
            {
                if (i+4 < length )
                {
                    if ('a' == string.charAt(i+1) &&
                        'm' == string.charAt(i+2) &&
                        'p' == string.charAt(i+3) &&
                        ';' == string.charAt(i+4))
                    {
                        //Skip
                    }
                    else
                    {
                        app = "&amp;";
                    }
                }
                else
                {
                    app = "&amp;";
                }
            }
            else
            {
                //No encoding, just do nothing, char will be added later.
            }
                        
            if (app != null)
            {
                if (sb == null)
                {
                    sb = SharedStringBuilder.get(context, SB_ENCODE_URI_QUERY, string.substring(0, i));
                }
                sb.append(app);
            }
            else
            {
                if (sb != null)
                {
                    sb.append(c);
                }
            }
            if (endLoop)
            {
                break;
            }
        }
        if (sb == null)
        {
            return string;
        }
        else
        {
            return sb.toString();
        }
    }

    /**
     * Encode an URI, escaping or percent-encoding all required characters and
     * following the rules mentioned on RFC 3986.  
     * 
     * @param writer
     * @param string
     * @param characterEncoding
     * @throws IOException
     */
    public static void encodeURIAttribute(Writer writer, final String string, final String characterEncoding)
        throws IOException
    {
        int start = 0;
        String app;
        char c;
        boolean endLoop = false;
        int length = string.length();
        for (int i = 0; i < length; ++i)
        {
            app = null;
            c = string.charAt(i);
            
            // This are the guidelines to be taken into account by this algorithm to encode:
            
            // RFC 2396 Section 2.4.3 Excluded US-ASCII Characters
            //
            // control     = <US-ASCII coded characters 00-1F and 7F hexadecimal>
            // space       = <US-ASCII coded character 20 hexadecimal>
            // delims      = "<" | ">" | "#" | "%" | <">
            //               %3C   %3E   %23   %25   %22
            // unwise      = "{" | "}" | "|" | "\" | "^" | "[" | "]" | "`"
            //               %7D   %7B   %7C   %5C   %5E   %5B   %5D   %60
            //
            // ".... Data corresponding to excluded characters must be escaped in order to
            // be properly represented within a URI....."
            
            // RFC 3986 Section 3.  Syntax Components
            //
            // "... The generic URI syntax consists of a hierarchical sequence of
            // components referred to as the scheme, authority, path, query, and
            // fragment.
            //
            //   URI         = scheme ":" hier-part [ "?" query ] [ "#" fragment ]
            //
            //   hier-part   = "//" authority path-abempty
            //               / path-absolute
            //               / path-rootless
            //               / path-empty
            // ...."
            
            // RFC 3986 Section 2.2:
            // Reserved characters (should not be percent-encoded)
            // reserved    = gen-delims / sub-delims
            // gen-delims  = ":" / "/" / "?" / "#" / "[" / "]" / "@"
            //               %3A   %2F   %3F   %23   %5B   %5D   %40
            // sub-delims  = "!" / "$" / "&" / "'" / "(" / ")" / "*" / "+" / "," / ";" / "="
            //               %21   %24   %26   %27   %28   %29   %2A   %2B   %2C   %3B   %3D
            
            // Note than chars "[" and "]" are mentioned as they should be escaped on RFC 2396,
            // but on the part D. Changes from RFC 2396 says about this chars (used on IPv6) 
            // "...those rules were redefined to directly specify the characters allowed...."
            // There is also other characters moved from excluded list to reserved:
            // "[" / "]" / "#"  
            
            // RFC 3986 Section 2.3:
            // "... for consistency, percent-encoded octets in the ranges of ALPHA
            // (%41-%5A and %61-%7A), DIGIT (%30-%39), hyphen (%2D), period (%2E),
            // underscore (%5F), or tilde (%7E) should not be created by URI
            // producers...."
            
            // RFC 3986 Section  3.2.2.  Host

            // host = IP-literal / IPv4address / reg-name

            // The reg-name syntax allows percent-encoded octets in order to
            // represent non-ASCII registered names in a uniform way that is
            // independent of the underlying name resolution technology.  Non-ASCII
            // characters must first be encoded according to UTF-8 [STD63], and then
            // each octet of the corresponding UTF-8 sequence must be percent-
            // encoded to be represented as URI characters.  URI producing
            // applications must not use percent-encoding in host unless it is used
            // to represent a UTF-8 character sequence.
            
            // RFC 3986 Section 3.4 Query 
            //         query       = *( pchar / "/" / "?" )
            //
            // "...  However, as query components are often used to carry identifying information 
            // in the form of "key=value" pairs and one frequently used value is a reference to
            // another URI, it is sometimes better for usability to avoid percent-encoding those characters....."
            //
            // RFC 3986 Section 2.5 Identifying Data (Apply to query section)
            //
            // When a new URI scheme defines a component that represents textual
            // data consisting of characters from the Universal Character Set [UCS],
            // the data should first be encoded as octets according to the UTF-8
            // character encoding [STD63]; then only those octets that do not
            // correspond to characters in the unreserved set should be percent-
            // encoded.  For example, the character A would be represented as "A",
            // the character LATIN CAPITAL LETTER A WITH GRAVE would be represented
            // as "%C3%80", and the character KATAKANA LETTER A would be represented
            // as "%E3%82%A2".
            //
            // RFC 3986 Section 3.5 Fragment
            //         fragment    = *( pchar / "/" / "?" )
            //
            // Note that follows the same as query
            
            // Based on the extracts the strategy to apply on this method is:
            // 
            // On scheme ":" hier-part
            //
            // Escape or percent encode chars inside :
            // 
            // - From %00 to %20, 
            // - <"> %22, "%" %25 (If there is encode of "%", there is a risk of 
            //                     duplicate encoding, encode it when we are sure 
            //                     that there are not encoded twice)
            // - "<" %3C, ">" %3E
            // - "\" %5C, "^" %5E, "`" %60 
            // - "{" %7B, "|" %7C, "}" %7D
            // - From %7F ad infinitum (characters from %100 to infinitum should not be used in this
            //   part of an URI, but it is preferred to encode it that omit it).
            //
            // The remaining characters must not be encoded
            //
            // Characters after ? or # should be percent encoding but only the necessary ones:
            //
            // - From %00 to %20 (' ' %20 could encode as +, but %20 also works, so we keep %20)
            // - <"> %22, "%" %25 (If there is encode of "%", there is a risk of 
            //                     duplicate encoding, encode it when we are sure 
            //                     that there are not encoded twice)
            // - "<" %3C, ">" %3E,
            // - "\" %5C, "^" %5E, "`" %60 
            // - "{" %7B, "|" %7C, "}" %7D
            // - From %7F ad infinitum (each character as many bytes as necessary but take into account
            //   that a single char should contain 2,3 or more bytes!. This data should be encoded 
            //   translating from the document character encoding to percent encoding, because this values
            //   could be retrieved from httpRequest.getParameter() and it uses the current character encoding
            //   for decode values)
            //
            // "&" should be encoded as "&amp;" because this link is inside an html page, and 
            // put only & is invalid in this context.

            if (   (c <= (char)0x20) || (c >= (char)0x7F) || 
                    c == '"' || c == '<' ||
                    c == '>' || c == '\\' || c == '^' || c == '`' ||
                    c == '{' || c == '|' || c == '}')
            {
                // The percent encoding on this part should be done using UTF-8 charset
                // as RFC 3986 Section 3.2.2 says.
                // Also there is a reference on 
                // http://www.w3.org/TR/html40/appendix/notes.html#non-ascii-chars
                // that recommend use of UTF-8 instead the document character encoding.
                // Jetty set by default UTF-8 (see http://jira.codehaus.org/browse/JETTY-113)
                //app = percentEncode(c, "UTF-8");
                if (start < i)
                {
                    writer.write(string, start, i-start);
                }
                start = i+1;
                percentEncode(writer, c, "UTF-8");
            }
            else if (c == '%')
            {
                if (i + 2 < length)
                {
                    char c1 = string.charAt(i+1);
                    char c2 = string.charAt(i+2);
                    if ((( c1 >= '0' && c1 <='9') || (c1 >='A' && c1 <='Z') || (c1 >='a' && c1 <='z')) &&
                        (( c2 >= '0' && c2 <='9') || (c2 >='A' && c2 <='Z') || (c2 >='a' && c2 <='z')))
                    {
                        // do not percent encode, because it could be already encoded
                        // and we don't want encode it twice
                    }
                    else
                    {
                        if (start < i)
                        {
                            writer.write(string, start, i-start);
                        }
                        start = i+1;
                        percentEncode(writer, c, UTF8);
                    }
                }
                else
                {
                    if (start < i)
                    {
                        writer.write(string, start, i-start);
                    }
                    start = i+1;
                    percentEncode(writer, c, UTF8);
                }
            }
            else if (c == '?' || c == '#')
            {
                if (i+1 < length)
                {
                    // The remaining part of the URI are data that should be encoded
                    // using the document character encoding.
                    if (start < i)
                    {
                        writer.write(string, start, i-start);
                    }
                    start = i+1;
                    writer.write(c);
                    encodeURIQuery(writer, string, i+1, characterEncoding);
                    endLoop = true;
                }
            }
            else
            {
                //No encoding, just do nothing, char will be added later.
            }
                        
            if (app != null)
            {
                if (start < i)
                {
                    writer.write(string, start, i-start);
                }
                start = i+1;
                writer.write(app);
            }
            if (endLoop)
            {
                start = length;
                break;
            }
        }

        if (start == 0)
        {
            writer.write(string);
        }
        else if (start < length)
        {
            writer.write(string,start,length-start);
        }
    }

    /**
     * Encode a unicode char value in percentEncode, decoding its bytes using a specified 
     * characterEncoding.
     * 
     * @param c
     * @param characterEncoding
     * @return
     */
    private static void percentEncode(Writer writer, char c, String characterEncoding) throws IOException
    {
        if (c > (char)((short)0x007F))
        {
            //percent encode in the proper encoding to be consistent
            percentEncodeNonUsAsciiCharacter(writer, c, characterEncoding);
        }
        else
        {
            //percent encode US-ASCII char (0x00-0x7F range)
            writer.write('%');
            writer.write(HEX_CHARSET.charAt( ((c >> 0x4) % 0x10)));
            writer.write(HEX_CHARSET.charAt(c % 0x10));
        }
    }
    
    private static void percentEncodeNonUsAsciiCharacter(Writer currentWriter, char c, String characterEncoding) 
        throws IOException
    {
        FastByteArrayOutputStream baos = new FastByteArrayOutputStream(10);

        try
        {
            try (OutputStreamWriter writer = new OutputStreamWriter(baos, characterEncoding))
            {
                writer.write(c);
                writer.flush();
            }
        }
        catch (IOException e)
        {
            return;
        }

        byte[] byteArray = baos.getByteArray();
        for (int i = 0; i < baos.getSize(); i++)
        {
            currentWriter.write('%');
            currentWriter.write(HEX_CHARSET.charAt( (( ((short) byteArray[i] & 0xFF ) >> 0x4) % 0x10)) );
            currentWriter.write(HEX_CHARSET.charAt( ((short) byteArray[i] & 0xFF ) % 0x10));
        }
    }
    
    /**
     * Encode the query part using the document charset encoding provided.
     * 
     * 
     * @param string
     * @param characterEncoding
     * @return
     */
    private static void encodeURIQuery(Writer writer, final String string, int offset, final String characterEncoding)
            throws IOException
    {
        int start = offset;
        int length = string.length();
        int realLength = length-offset;
        String app;
        char c;

        for (int i = offset; i < length; ++i)
        {
            app = null;
            c = string.charAt(i);
            
            // - From %00 to %20 (' ' %20 could encode as +, but %20 also works, so we keep %20)
            // - <"> %22 (If there is encode of "%", there is a risk of duplicate encoding, so 
            //            we make easier and omit this one)
            // - "<" %3C, ">" %3E,
            // - "\" %5C, "^" %5E, "`" %60 
            // - "{" %7B, "|" %7C, "}" %7D
            // - From %7F ad infinitum (each character as many bytes as necessary but take into account
            //   that a single char should contain 2,3 or more bytes!. This data should be encoded 
            //   translating from the document character encoding to percent encoding)
            //
            // "&" should be encoded as "&amp;" because this link is inside an html page, and 
            // put & is invalid in this context   
            
            if (   (c <= (char)0x20) || (c >= (char)0x7F) || 
                    c == '"' || c == '<' ||
                    c == '>' || c == '\\' || c == '^' || c == '`' ||
                    c == '{' || c == '|' || c == '}')
            {
                // The percent encoding on this part should be done using UTF-8 charset
                // as RFC 3986 Section 3.2.2 says
                if (start < i)
                {
                    writer.write(string, start, i-start);
                }
                start = i+1;
                percentEncode(writer, c, characterEncoding);
            }
            else if (c == '%')
            {
                if (i + 2 < length)
                {
                    char c1 = string.charAt(i+1);
                    char c2 = string.charAt(i+2);
                    if ((( c1 >= '0' && c1 <='9') || (c1 >='A' && c1 <='Z') || (c1 >='a' && c1 <='z')) &&
                        (( c2 >= '0' && c2 <='9') || (c2 >='A' && c2 <='Z') || (c2 >='a' && c2 <='z')))
                    {
                        // do not percent encode, because it could be already encoded
                    }
                    else
                    {
                        if (start < i)
                        {
                            writer.write(string, start, i-start);
                        }
                        start = i+1;
                        percentEncode(writer, c, characterEncoding);
                    }
                }
                else
                {
                    //app = percentEncode(c, characterEncoding);
                    if (start < i)
                    {
                        writer.write(string, start, i-start);
                    }
                    start = i+1;
                    percentEncode(writer, c, characterEncoding);
                }
            }
            else if (c == '&')
            {
                if (i+4 < length )
                {
                    if ('a' == string.charAt(i+1) &&
                        'm' == string.charAt(i+2) &&
                        'p' == string.charAt(i+3) &&
                        ';' == string.charAt(i+4))
                    {
                        //Skip
                    }
                    else
                    {
                        app = "&amp;";
                    }
                }
                else
                {
                    app = "&amp;";
                }
            }
            else
            {
                //No encoding, just do nothing, char will be added later.
            }
                        
            if (app != null)
            {
                if (start < i)
                {
                    writer.write(string, start, i-start);
                }
                start = i+1;
                writer.write(app);
            }
        }

        if (start == offset)
        {
            writer.write(string, offset, realLength);
        }
        else if (start < length)
        {
            writer.write(string,start,length-start);
        }
    }
}
