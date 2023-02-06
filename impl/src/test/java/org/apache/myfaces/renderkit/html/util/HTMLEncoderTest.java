/*
 * Copyright 2007 The Apache Software Foundation.
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

package org.apache.myfaces.renderkit.html.util;

import org.apache.myfaces.renderkit.html.util.HTMLEncoder;
import java.io.CharArrayWriter;
import java.io.IOException;

import org.apache.myfaces.test.base.junit.AbstractJsfTestCase;
import org.apache.myfaces.test.mock.MockFacesContext;
import org.junit.Assert;
import org.junit.Test;

/**
 * <code>HTMLEncoderTest</code> tests <code>org.apache.myfaces.shared.renderkit.html.util.HTMLEncoder</code>.
 */
public class HTMLEncoderTest extends AbstractJsfTestCase {
  private String stringNoSpecialChars = "Hello, this is MyFaces speaking!";
  private String stringNoSpecialCharsEncoded = "Hello, this is MyFaces speaking!";
  private String stringNoSpecialCharsEncodedPartial = "lo, this is MyFaces speakin";
  private String stringSpecialChars1 = "<\"Hello\", this is MyFaces speaking!>";
  private String stringSpecialChars1Encoded = "&lt;&quot;Hello&quot;, this is MyFaces speaking!&gt;";
  private String stringSpecialChars2 = "Hello & this is MyFaces speaking!>";
  private String stringSpecialChars2Encoded = "Hello &amp; this is MyFaces speaking!&gt;";
  private String stringLineBreak = "Hell\u00F6\nthis is MyFaces speaking!>";
  private String stringLineBreakEncoded1 = "Hell&ouml;<br/>this is MyFaces speaking!&gt;";
  private String stringLineBreakEncoded2 = "Hell&ouml;\nthis is MyFaces speaking!&gt;";
  private String stringLineBreakEncoded2Partial = "&ouml;\nthis is MyFaces speaking!";
  private String stringBlanks = "<Hello   this is MyFaces speaking!>";
  private String stringBlanksEncoded = "&lt;Hello   this is MyFaces speaking!&gt";

  /**
   * Test method for
   * {@link org.apache.myfaces.shared.renderkit.html.util.HTMLEncoder#encode(String)}.
   */
  @Test
  public void testEncodeStringNoSpecialChars() {
    String encodedStr = HTMLEncoder.encode(new MockFacesContext(), stringNoSpecialChars);
      Assert.assertEquals(stringNoSpecialCharsEncoded, encodedStr);
  }

  @Test
  public void testEncodeStringSpecialChars1() {
    String encodedStr = HTMLEncoder.encode(new MockFacesContext(), stringSpecialChars1);
    Assert.assertEquals(stringSpecialChars1Encoded, encodedStr);
  }

  @Test
  public void testEncodeStringSpecialChars2() {
    String encodedStr = HTMLEncoder.encode(new MockFacesContext(), stringSpecialChars2);
    Assert.assertEquals(stringSpecialChars2Encoded, encodedStr);
  }

  @Test
  public void testEncodeStringLineBreak1() {
    String encodedStr = HTMLEncoder.encode(new MockFacesContext(), stringLineBreak, true);
    Assert.assertEquals(stringLineBreakEncoded1, encodedStr);
  }

  @Test
  public void testEncodeStringLineBreak2() {
    String encodedStr = HTMLEncoder.encode(new MockFacesContext(), stringLineBreak, false);
    Assert.assertEquals(stringLineBreakEncoded2, encodedStr);
  }

  @Test
  public void testEncodeStringEmpty() {
    String encodedStr = HTMLEncoder.encode(new MockFacesContext(), "");
    Assert.assertEquals("", encodedStr);
  }

  @Test
  public void testEncodeStringNull() {
    String encodedStr = HTMLEncoder.encode(new MockFacesContext(), null);
    Assert.assertEquals("", encodedStr);
  }

  @Test
  public void testEncodeArrayNoSpecialChars() {
    try {
      CharArrayWriter writer = new CharArrayWriter();
      char[] source = stringNoSpecialChars.toCharArray();
      HTMLEncoder.encode(source, 0, source.length, writer);
      assertCharArrayEquals(stringNoSpecialCharsEncoded.toCharArray(), writer.toCharArray());
    } catch (IOException e) {
      Assert.fail(e.getMessage());
    }
  }

  @Test
  public void testEncodeArrayNoSpecialCharsPartial() {
    try {
      CharArrayWriter writer = new CharArrayWriter();
      char[] source = stringNoSpecialChars.toCharArray();
      HTMLEncoder.encode(source, 3, source.length - 5, writer);
      assertCharArrayEquals(stringNoSpecialCharsEncodedPartial.toCharArray(), writer.toCharArray());
    } catch (IOException e) {
      Assert.fail(e.getMessage());
    }
  }

  @Test
  public void testEncodeArraySpecialChars1() {
    try {
      CharArrayWriter writer = new CharArrayWriter();
      char[] source = stringSpecialChars1.toCharArray();
      HTMLEncoder.encode(source, 0, source.length, writer);
      assertCharArrayEquals(stringSpecialChars1Encoded.toCharArray(), writer.toCharArray());
    } catch (IOException e) {
      Assert.fail(e.getMessage());
    }
  }

  @Test
  public void testEncodeArraySpecialChars2() {
    try {
      CharArrayWriter writer = new CharArrayWriter();
      char[] source = stringSpecialChars2.toCharArray();
      HTMLEncoder.encode(source, 0, source.length, writer);
      assertCharArrayEquals(stringSpecialChars2Encoded.toCharArray(), writer.toCharArray());
    } catch (IOException e) {
      Assert.fail(e.getMessage());
    }
  }

  @Test
  public void testEncodeArrayEmpty() {
    try {
      CharArrayWriter writer = new CharArrayWriter();
      HTMLEncoder.encode(new char[]{}, 0, 1, writer);
      assertCharArrayEquals(new char[]{}, writer.toCharArray());
    } catch (IOException e) {
      Assert.fail(e.getMessage());
    }
  }

  @Test
  public void testEncodeArrayNull() {
    try {
      CharArrayWriter writer = new CharArrayWriter();
      HTMLEncoder.encode(null, 0, 0, writer);
      assertCharArrayEquals(new char[]{}, writer.toCharArray());
    } catch (IOException e) {
      Assert.fail(e.getMessage());
    }
  }

  @Test
  public void testEncodeArrayWrongIndex1() {
    try {
      CharArrayWriter writer = new CharArrayWriter();
      char[] source = stringSpecialChars2.toCharArray();
      HTMLEncoder.encode(source, 0, source.length - 100, writer);
        assertCharArrayEquals(new char[]{}, writer.toCharArray());
    } catch (IOException e) {
      Assert.fail(e.getMessage());
    }
  }

  @Test
  public void testEncodeArrayWrongIndex2() {
    try {
      CharArrayWriter writer = new CharArrayWriter();
      char[] source = stringSpecialChars2.toCharArray();
      HTMLEncoder.encode(source, -100, source.length, writer);
      assertCharArrayEquals(stringSpecialChars2Encoded.toCharArray(), writer.toCharArray());
    } catch (IOException e) {
      Assert.fail(e.getMessage());
    }
  }

  @Test
  public void testEncodeArrayWrongIndex3() {
    try {
      CharArrayWriter writer = new CharArrayWriter();
      char[] source = stringSpecialChars2.toCharArray();
      HTMLEncoder.encode(source, 100000, source.length, writer);
      assertCharArrayEquals(new char[]{}, writer.toCharArray());
    } catch (IOException e) {
      Assert.fail(e.getMessage());
    }
  }

  @Test
  public void testEncodeArrayLineBreak1() {
    try {
      CharArrayWriter writer = new CharArrayWriter();
      char[] source = stringLineBreak.toCharArray();
      HTMLEncoder.encode(source, 0, source.length, true, writer);
      assertCharArrayEquals(stringLineBreakEncoded1.toCharArray(), writer.toCharArray());
    } catch (IOException e) {
      Assert.fail(e.getMessage());
    }
  }

  @Test
  public void testEncodeArrayLineBreak2() {
    try {
      CharArrayWriter writer = new CharArrayWriter();
      char[] source = stringLineBreak.toCharArray();
      HTMLEncoder.encode(source, 0, source.length, false, writer);
      assertCharArrayEquals(stringLineBreakEncoded2.toCharArray(), writer.toCharArray());
    } catch (IOException e) {
      Assert.fail(e.getMessage());
    }
  }

  @Test
  public void testEncodeArrayLineBreak2WrongIndex() {
    try {
      CharArrayWriter writer = new CharArrayWriter();
      char[] source = stringLineBreak.toCharArray();
      HTMLEncoder.encode(source, 0, source.length + 5, false, writer);
      assertCharArrayEquals(stringLineBreakEncoded2.toCharArray(), writer.toCharArray());
    } catch (IOException e) {
      Assert.fail(e.getMessage());
    }
  }

  @Test
  public void testEncodeArrayLineBreakPartial() {
    try {
      CharArrayWriter writer = new CharArrayWriter();
      char[] source = stringLineBreak.toCharArray();
      HTMLEncoder.encode(source, 4, source.length - 5, writer);
      char[] expected = stringLineBreakEncoded2Partial.toCharArray();
      assertCharArrayEquals(expected, writer.toCharArray());
    } catch (IOException e) {
      Assert.fail(e.getMessage());
    }
  }

  @Test
  public void testSimpleWriteURIAttribute() throws Exception
  {
      String cad1 = "http://myfaces.apache.org/hello.jsf?key1=val&key2=val2#id";
      String cad2 = "http://myfaces.apache.org/hello.jsf?key1=val&amp;key2=val2#id";
      String cad3 = HTMLEncoder.encodeURIAttribute(new MockFacesContext(), cad1, "UTF-8");
      Assert.assertEquals(cad2, cad3);      
  }
  
  @Test
  public void testUsAsciiEscapedCharactersBeforeQuery() throws Exception
  {
      // Escape
      // - From %00 to %20, 
      // - <"> %22, "%" %25
      // - "<" %3C, ">" %3E,
      // - "\" %5C, "^" %5E, "`" %60 
      // - "{" %7B, "|" %7C, "}" %7D
      // - From %7F ad infinitum
      String cad1 = "?key=\"%<>\\`{|}^\n "; //Omit %
      String cad2 = "?key=%22%25%3C%3E%5C%60%7B%7C%7D%5E%0A%20";
      String cad3 = HTMLEncoder.encodeURIAttribute(new MockFacesContext(), cad1,"UTF-8");
      Assert.assertEquals(cad2, cad3);
      
      String cad4 = "\"%<>\\`{|}^\n ";
      String cad5 = "%22%25%3C%3E%5C%60%7B%7C%7D%5E%0A%20";
      String cad6 = HTMLEncoder.encodeURIAttribute(new MockFacesContext(), cad4,"UTF-8");
      Assert.assertEquals(cad5, cad6);
      
      
  }
  
  @Test
  public void testUsAsciiEscapedCharactersBeforeQueryLowerCase() throws Exception
  {
      // Escape
      // - From %00 to %20, 
      // - <"> %22, "%" %25
      // - "<" %3C, ">" %3E,
      // - "\" %5C, "^" %5E, "`" %60 
      // - "{" %7B, "|" %7C, "}" %7D
      // - From %7F ad infinitum
      String cad1 = "?key=\"%<>\\`{|}^\n "; //Omit %
      String cad2 = "?key=%22%25%3c%3e%5c%60%7b%7c%7d%5e%0a%20";
      String cad3 = HTMLEncoder.encodeURIAttribute(new MockFacesContext(), cad1,"UTF-8");
      Assert.assertEquals(cad2.substring(0,5) + cad2.substring(5).toUpperCase(), cad3);
      
      String cad4 = "\"%<>\\`{|}^\n ";
      String cad5 = "%22%25%3c%3e%5c%60%7b%7c%7d%5e%0a%20";
      String cad6 = HTMLEncoder.encodeURIAttribute(new MockFacesContext(), cad4,"UTF-8");
      Assert.assertEquals(cad5.substring(0,5) + cad5.substring(5).toUpperCase(), cad6);
      
  }  
  
  @Test
  public void testWriteNonUsAsciiOnURIAttribute() throws Exception
  {
      // Character ü in ISO-8859-1 is %FC but on UTF-8 is %C3%BC. In this case,
      // it should encode as %C3%BC
	  byte [] array = new byte[]{(byte)0xFC};
      String cad1 = new String(array,"ISO-8859-1");//+(char)0xC3BC;//"http://myfaces.apache.org/heüll o.jsf?key=val#id";
      String cad2 = "%C3%BC";//"http://myfaces.apache.org/he%FCll%20o.jsf?key=val#id";
      String cad3 = HTMLEncoder.encodeURIAttribute(new MockFacesContext(), cad1,"UTF-8");
      Assert.assertEquals(cad2, cad3);

  }
  
  @Test
  public void testReservedCharactersOnURIAttribute() throws Exception
  {
      //Reserved
      // Reserved characters (should not be percent-encoded)
      // reserved    = gen-delims / sub-delims
      // gen-delims  = ":" / "/" / "?" / "#" / "[" / "]" / "@"
      //               %3A   %2F   %3F   %23   %5B   %5D   %40
      // sub-delims  = "!" / "$" / "&" / "'" / "(" / ")" / "*" / "+" / "," / ";" / "="
      //               %21   %24   %26   %27   %28   %29   %2A   %2B   %2C   %3B   %3D
      
      String cad1 = "?key=:/[]@!$'()*+,;="; //Omit &
      String cad2 = HTMLEncoder.encodeURIAttribute(new MockFacesContext(), cad1,"UTF-8");
      Assert.assertEquals(cad1, cad2);
      
      String cad7 = ":/[]@!$&'()*+,;=";
      String cad8 = HTMLEncoder.encodeURIAttribute(new MockFacesContext(), cad7,"UTF-8");
      Assert.assertEquals(cad7, cad8);
  }

  @Test
  public void testNonEncodedCharactersOnURIAttribute() throws Exception
  {
      // "... for consistency, percent-encoded octets in the ranges of ALPHA
      // (%41-%5A and %61-%7A), DIGIT (%30-%39), hyphen (%2D), period (%2E),
      // underscore (%5F), or tilde (%7E) should not be created by URI
      // producers...."
      String cad1 = "?key=ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-._~";
      String cad2 = HTMLEncoder.encodeURIAttribute(new MockFacesContext(), cad1,"UTF-8");
      Assert.assertEquals(cad1, cad2);
      
      String cad3 = "#somefile?key=ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-._~";
      String cad4 = HTMLEncoder.encodeURIAttribute(new MockFacesContext(), cad3,"UTF-8");
      Assert.assertEquals(cad3, cad4);
      
      String cad5 = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-._~";
      String cad6 = HTMLEncoder.encodeURIAttribute(new MockFacesContext(), cad5,"UTF-8");
      Assert.assertEquals(cad5, cad6);
  }

  @Test
  public void testWriteURIAttribute() throws Exception
  {
      //Note char 256 or 0x100 should not be passed or percent encoded, because it is not
      //valid for URIs.
	  byte [] array11 = new byte[]{(byte) 0xC2,(byte) 0xA1,(byte) 0xC2,(byte) 0xA2,
			  (byte) 0xC2,(byte) 0xA3,(byte) 0xC2,(byte) 0xA4,(byte) 0xC2,(byte) 0xA5,
			  (byte) 0xC2,(byte) 0xA6,(byte) 0xC2,(byte) 0xA7,(byte) 0xC2,(byte) 0xA8,
			  (byte) 0xC2,(byte) 0xA9,(byte) 0xC2,(byte) 0xAA,(byte) 0xC2,(byte) 0xAB,
			  (byte) 0xC2,(byte) 0xAC,(byte) 0xC2,(byte) 0xAD,(byte) 0xC2,(byte) 0xAE,
			  (byte) 0xC2,(byte) 0xAF,(byte) 0xC2,(byte) 0xB0,(byte) 0xC2,(byte) 0xB1};
	  
      String cad11 = new String(array11,"UTF-8") + ((char)(0xFF))+((char)(0x100));
      String cad12 = "%C2%A1%C2%A2%C2%A3%C2%A4%C2%A5%C2%A6%C2%A7%C2%A8%C2%A9%C2%AA%C2%AB%C2%AC%C2%AD"+
                     "%C2%AE%C2%AF%C2%B0%C2%B1%C3%BF%C4%80";
      String cad13 = HTMLEncoder.encodeURIAttribute(new MockFacesContext(), cad11,"UTF-8");
      Assert.assertEquals(cad12, cad13);
      
      String cad1= "?key=" + new String(array11,"UTF-8")+((char)(0xFF))+((char)(0x100));
      String cad2 = "?key=%C2%A1%C2%A2%C2%A3%C2%A4%C2%A5%C2%A6%C2%A7%C2%A8%C2%A9%C2%AA%C2%AB%C2%AC%C2%AD"+
                     "%C2%AE%C2%AF%C2%B0%C2%B1%C3%BF%C4%80";
      String cad3 = HTMLEncoder.encodeURIAttribute(new MockFacesContext(), cad1,"UTF-8");
      Assert.assertEquals(cad2, cad3);
            
      //String cad14 = "http://myfaces.apache.org/page.jsf?key="+((char)0xFF)+((char)0x100);
      //String cad15 = HTMLEncoder.encodeURIAttribute(cad14,false);
      //assertEquals(cad14,cad15);
  }
    
  private void assertCharArrayEquals(char[] expected, char[] actual) {
    if ((expected == null ^ actual == null) || expected.length != actual.length) {
      Assert.fail();
    }
    for (int i = 0; i < expected.length; i++) {
      Assert.assertEquals(expected[i], actual[i]);
    }
  }
  
}