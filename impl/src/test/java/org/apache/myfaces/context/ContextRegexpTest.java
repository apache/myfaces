/*
 *  Copyright 2008 werpu.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */
package org.apache.myfaces.context;

import junit.framework.TestCase;

/**
 * Regular Expression tests used within the faces context submodules
 *
 * @author Werner Punz(latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class ContextRegexpTest extends TestCase {

    static final String SPLITTER = "\\,";

    /**
     * condition valid string
     */
    public void testCondition1() {
        String[] splitted = " hello ,world ".split(SPLITTER);
        assertTrue("length assertion", splitted.length == 2);
        assertTrue(splitted[0].trim().equals("hello"));
        assertTrue(splitted[1].trim().equals("world"));
    }

    /**
     * test the condition 2
     * empty string
     */
    public void testCondition2() {
        String[] splitted = " ".split(SPLITTER);
        assertTrue(splitted.length == 1);
        assertTrue(splitted[0] != null);
    }

    /**
     * test the condition 3
     * empty string no blanks
     */
    public void testCondition3() {
        String[] splitted = "".split(SPLITTER);
        assertTrue(splitted.length == 1);
        assertTrue(splitted[0] != null);
    }
}
