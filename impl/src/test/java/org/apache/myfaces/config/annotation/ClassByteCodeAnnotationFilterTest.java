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
package org.apache.myfaces.config.annotation;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import jakarta.faces.bean.ManagedBean;

import junit.framework.TestCase;

public class ClassByteCodeAnnotationFilterTest extends TestCase
{

    /**
     * The annotation names to scan for
     */
    private Set<String> annotationNames = null;

    /**
     * The tested class
     */
    private _ClassByteCodeAnnotationFilter filter = null;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        filter = new _ClassByteCodeAnnotationFilter();
        annotationNames = new HashSet<String>(
                Arrays.asList("Ljavax/faces/bean/ManagedBean;")
        );
    }

    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();
        filter = null;
        annotationNames = null;
    }

    /**
     * Test that filter returns <code>false</code> for beans without annotations 
     */
    public void testBeanWithoutAnnotations() throws IOException
    {
        DataInputStream byteCode = getDataInputStreamForClass(ClassWithoutAnnotations.class);
        assertFalse(filter.couldContainAnnotationsOnClassDef(byteCode, annotationNames));
    }

    /**
     * Test that filter returns <code>true</code> for a bean with a {@link ManagedBean} annotation 
     */
    public void testBeanWithManagedBeanAnnotation() throws IOException
    {
        DataInputStream byteCode = getDataInputStreamForClass(ClassWithManagedBeanAnnotation.class);
        assertTrue(filter.couldContainAnnotationsOnClassDef(byteCode, annotationNames));
    }

    /**
     * Test that filter returns <code>false</code> for a bean with no annotations and
     * a long constant in the constants pool. 
     */
    public void testBeanWithLongConstant() throws IOException
    {
        DataInputStream byteCode = getDataInputStreamForClass(ClassWithLongConstant.class);
        assertFalse(filter.couldContainAnnotationsOnClassDef(byteCode, annotationNames));
    }

    /**
     * Helper method to load a .class file from the classpath
     * @param clazz the {@link Class} object for the class to load
     * @return The {@link DataInputStream} of the .class file
     */
    private DataInputStream getDataInputStreamForClass(Class<?> clazz)
    {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        String resourceName = clazz.getName().replace('.', '/') + ".class";
        InputStream stream = cl.getResourceAsStream(resourceName);
        assertNotNull("Cannot find class: " + clazz.getName(), stream);
        return new DataInputStream(stream);
    }

    /**
     * A class without any annotations
     */
    public static class ClassWithoutAnnotations
    {
        // nothing
    }

    /**
     * A class with a {@link ManagedBean} annotation
     */
    @ManagedBean
    public static class ClassWithManagedBeanAnnotation
    {

    }

    /**
     * A class without a long constants in the constant pool
     */
    public static class ClassWithLongConstant
    {
        public final static long value1 = 9223362036854775807l;
        public final static long value2 = 9223362036854775808l;
        public final static long value3 = 9223362036854775809l;
        public final static long value4 = 9223362036854775801l;
    }

}
