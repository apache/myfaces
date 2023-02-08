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
package jakarta.faces.component;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

import jakarta.faces.event.ActionListener;
import jakarta.faces.event.FacesListener;
import jakarta.faces.event.ValueChangeListener;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Created by IntelliJ IDEA.
 * User: mathias
 * Date: 18.03.2007
 * Time: 01:16:50
 * To change this template use File | Settings | File Templates.
 */
public class UIComponentBaseFacesListenerTest extends AbstractUIComponentBaseTest
{
    private ActionListener _actionListener;
    private ValueChangeListener _valueChangeListener;

    public UIComponentBaseFacesListenerTest()
    {
    }
    
    @Override
    @BeforeEach
    public void setUp() throws Exception
    {
        super.setUp();
        _actionListener = _mocksControl.createMock(ActionListener.class);
        _valueChangeListener = _mocksControl.createMock(ValueChangeListener.class);
    }
    
    @Override
    @AfterEach
    public void tearDown() throws Exception
    {
        super.tearDown();
        _actionListener = null;
        _valueChangeListener = null;
    }

    @Test
    public void testArgNPE() throws Exception
    {
        Assertions.assertThrows(NullPointerException.class, () -> {
            _testImpl.getFacesListeners(null);
        });
    }

    @Test
    public void testInvalidListenerType() throws Exception
    {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            _testImpl.getFacesListeners(Date.class);
        });
    }

    @Test
    public void testEmptyListener() throws Exception
    {
        FacesListener[] listener = _testImpl.getFacesListeners(ActionListener.class);
        Assertions.assertNotNull(listener);
        Assertions.assertEquals(0, listener.length);
    }

    @Test
    public void testGetFacesListeners()
    {
        _testImpl.addFacesListener(_actionListener);

        FacesListener[] listener = _testImpl.getFacesListeners(ValueChangeListener.class);
        Assertions.assertNotNull(listener);
        Assertions.assertEquals(0, listener.length);
        Assertions.assertTrue(ValueChangeListener.class.equals(listener.getClass().getComponentType()));

        _testImpl.addFacesListener(_valueChangeListener);

        listener = _testImpl.getFacesListeners(FacesListener.class);
        Assertions.assertNotNull(listener);
        Assertions.assertEquals(2, listener.length);
        Collection<FacesListener> col = Arrays.asList(listener);
        Assertions.assertTrue(col.contains(_actionListener));
        Assertions.assertTrue(col.contains(_valueChangeListener));
    }

    @Test
    public void testRemoveFacesListenerArgNPE() throws Exception
    {
        Assertions.assertThrows(NullPointerException.class, () -> {
            _testImpl.removeFacesListener(null);
        });
    }

    @Test
    public void testRemoveFacesListener() throws Exception
    {
        _testImpl.addFacesListener(_actionListener);
        Assertions.assertEquals(_actionListener, _testImpl.getFacesListeners(FacesListener.class)[0]);
        _testImpl.removeFacesListener(_actionListener);
        Assertions.assertEquals(0, _testImpl.getFacesListeners(FacesListener.class).length);
    }
}
