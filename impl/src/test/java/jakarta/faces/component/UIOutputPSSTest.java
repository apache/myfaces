/*
 * Copyright 2012 The Apache Software Foundation.
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
package jakarta.faces.component;

import static org.junit.jupiter.api.Assertions.*;

import java.io.Serializable;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.LongConverter;
import org.apache.myfaces.test.base.junit.AbstractFacesTestCase;
import org.junit.jupiter.api.Test;

class UIOutputPSSTest extends AbstractFacesTestCase
{

    /**
     * Check null delta when converter only implements Converter interface.
     * 
     * @throws Exception 
     */
    @Test
    void converterDeltaState1() throws Exception
    {
        UIOutput output = new UIOutput();
        output.setId("output1");
        output.setConverter(new LongConverter());
        output.markInitialState();
        
        Object state1 = output.saveState(facesContext);
        
        // null delta expected
        assertNull(state1);
        
        UIOutput output2 = new UIOutput();
        output2.setId("output1");
        Converter converter2 = new LongConverter();
        output2.setConverter(converter2);
        output2.markInitialState();
        
        output2.restoreState(facesContext, state1);
        
        // Check converter is not replaced, because it is not necessary
        assertEquals(converter2, output2.getConverter());
    }

    @Test
    void converterDeltaState2() throws Exception
    {
        UIOutput output = new UIOutput();
        output.setId("output1");
        output.setConverter(new LongSerializableConverter());
        output.markInitialState();
        
        Object state1 = output.saveState(facesContext);
        
        // null delta expected
        assertNull(state1);
        
        UIOutput output2 = new UIOutput();
        output2.setId("output1");
        Converter converter2 = new LongSerializableConverter();
        output2.setConverter(converter2);
        output2.markInitialState();
        
        output2.restoreState(facesContext, state1);
        
        // Check converter is not replaced, because it is not necessary
        assertEquals(converter2, output2.getConverter());
    }

    @Test
    void converterDeltaState3() throws Exception
    {
        UIOutput output = new UIOutput();
        output.setId("output1");
        output.setConverter(new LongStateHolderConverter());
        output.markInitialState();
        
        Object state1 = output.saveState(facesContext);
        
        // StateHolder only force always to save the state
        assertNotNull(state1);
        
        UIOutput output2 = new UIOutput();
        output2.setId("output1");
        Converter converter2 = new LongStateHolderConverter();
        output2.setConverter(converter2);
        output2.markInitialState();
        
        output2.restoreState(facesContext, state1);
        
        // Check converter IS replaced, because it should be always stored into the state
        assertNotSame(converter2, output2.getConverter());
    }

    @Test
    void converterDeltaState4() throws Exception
    {
        UIOutput output = new UIOutput();
        output.setId("output1");
        output.setConverter(new LongPartialStateHolderConverter());
        output.markInitialState();
        
        Object state1 = output.saveState(facesContext);
        
        // PartialStateHolder only save state if delta is null
        assertNull(state1);
        
        UIOutput output2 = new UIOutput();
        output2.setId("output1");
        Converter converter2 = new LongPartialStateHolderConverter();
        output2.setConverter(converter2);
        output2.markInitialState();
        
        output2.restoreState(facesContext, state1);
        
        // Check converter is not replaced, because it is not necessary
        assertEquals(converter2, output2.getConverter());
    }

    @Test
    void converterDeltaState5() throws Exception
    {
        UIOutput output = new UIOutput();
        output.setId("output1");
        output.setConverter(new LongPartialStateHolderConverter2());
        output.markInitialState();
        
        Object state1 = output.saveState(facesContext);
        
        // PartialStateHolder only save state if delta is null
        assertNotNull(state1);
        
        UIOutput output2 = new UIOutput();
        output2.setId("output1");
        Converter converter2 = new LongPartialStateHolderConverter2();
        output2.setConverter(converter2);
        output2.markInitialState();
        
        output2.restoreState(facesContext, state1);
        
        // Check converter is not replaced, because it is not necessary (delta change)
        assertEquals(converter2, output2.getConverter());
    }

    @Test
    void converterDeltaState6() throws Exception
    {
        UIOutput output = new UIOutput();
        output.setId("output1");
        output.setConverter(new LongConverter());
        output.markInitialState();
        
        //Force parentDelta to be not null
        output.getAttributes().put("style", "style1");
        
        Object[] state1 = (Object[])output.saveState(facesContext);
        
        // null delta expected
        assertNotNull(state1);
        assertEquals(1, state1.length);
        
        UIOutput output2 = new UIOutput();
        output2.setId("output1");
        Converter converter2 = new LongConverter();
        output2.setConverter(converter2);
        output2.markInitialState();
        
        output2.restoreState(facesContext, state1);
        
        // Check converter is not replaced, because it is not necessary
        assertEquals(converter2, output2.getConverter());
        
        // Check parentDelta works
        assertEquals("style1", output2.getAttributes().get("style"));
    }

    @Test
    void converterDeltaState7() throws Exception
    {
        UIOutput output = new UIOutput();
        output.setId("output1");
        output.setConverter(new LongSerializableConverter());
        output.markInitialState();
        
        //Force parentDelta to be not null
        output.getAttributes().put("style", "style1");
        
        Object[] state1 = (Object[])output.saveState(facesContext);
        
        // null delta expected
        assertNotNull(state1);
        assertEquals(1, state1.length);
        
        UIOutput output2 = new UIOutput();
        output2.setId("output1");
        Converter converter2 = new LongSerializableConverter();
        output2.setConverter(converter2);
        output2.markInitialState();
        
        output2.restoreState(facesContext, state1);
        
        // Check converter is not replaced, because it is not necessary
        assertEquals(converter2, output2.getConverter());
        
        // Check parentDelta works
        assertEquals("style1", output2.getAttributes().get("style"));
    }

    @Test
    void converterDeltaState8() throws Exception
    {
        UIOutput output = new UIOutput();
        output.setId("output1");
        output.setConverter(new LongStateHolderConverter());
        output.markInitialState();
        
        //Force parentDelta to be not null
        output.getAttributes().put("style", "style1");
        
        Object[] state1 = (Object[])output.saveState(facesContext);
        
        // StateHolder force state
        assertNotNull(state1);
        assertEquals(2, state1.length);
        assertNotNull(state1[1]);
        assertNotNull(state1[0]);
        
        UIOutput output2 = new UIOutput();
        output2.setId("output1");
        Converter converter2 = new LongStateHolderConverter();
        output2.setConverter(converter2);
        output2.markInitialState();
        
        output2.restoreState(facesContext, state1);
        
        // Check converter IS replaced, because it should be always stored into the state
        assertNotSame(converter2, output2.getConverter());
        
        // Check parentDelta works
        assertEquals("style1", output2.getAttributes().get("style"));
    }

    @Test
    void converterDeltaState9() throws Exception
    {
        UIOutput output = new UIOutput();
        output.setId("output1");
        output.setConverter(new LongPartialStateHolderConverter());
        output.markInitialState();
        
        //Force parentDelta to be not null
        output.getAttributes().put("style", "style1");
        
        Object[] state1 = (Object[])output.saveState(facesContext);
        
        // null delta expected
        assertNotNull(state1);
        assertEquals(1, state1.length);
        
        UIOutput output2 = new UIOutput();
        output2.setId("output1");
        Converter converter2 = new LongPartialStateHolderConverter();
        output2.setConverter(converter2);
        output2.markInitialState();
        
        output2.restoreState(facesContext, state1);
        
        // Check converter is not replaced, because it is not necessary
        assertEquals(converter2, output2.getConverter());
        
        // Check parentDelta works
        assertEquals("style1", output2.getAttributes().get("style"));
    }

    @Test
    void converterDeltaState10() throws Exception
    {
        UIOutput output = new UIOutput();
        output.setId("output1");
        output.setConverter(new LongPartialStateHolderConverter2());
        output.markInitialState();
        
        //Force parentDelta to be not null
        output.getAttributes().put("style", "style1");
        
        Object[] state1 = (Object[])output.saveState(facesContext);
        
        // PartialStateHolder force state only when there is delta
        assertNotNull(state1);
        assertEquals(2, state1.length);
        assertNotNull(state1[1]);
        assertNotNull(state1[0]);
        
        UIOutput output2 = new UIOutput();
        output2.setId("output1");
        Converter converter2 = new LongPartialStateHolderConverter2();
        output2.setConverter(converter2);
        output2.markInitialState();
        
        output2.restoreState(facesContext, state1);
        
        // Check converter is not replaced, because it is not necessary (delta change)
        assertEquals(converter2, output2.getConverter());
        
        // Check parentDelta works
        assertEquals("style1", output2.getAttributes().get("style"));
    }

    /**
     * Check set null converter after markInitialState case
     * 
     * @throws Exception 
     */
    @Test
    void converterDeltaState11() throws Exception
    {
        Converter[] converters = new Converter[]{
            new LongConverter(),
            new LongSerializableConverter(),
            new LongStateHolderConverter(),
            new LongPartialStateHolderConverter(),
            new LongPartialStateHolderConverter2()
        };
        Converter[] converters2 = new Converter[]{
            new LongConverter(),
            new LongSerializableConverter(),
            new LongStateHolderConverter(),
            new LongPartialStateHolderConverter(),
            new LongPartialStateHolderConverter2()
        };
        for (int i = 0; i < converters.length; i++)
        {
            UIOutput output = new UIOutput();
            output.setId("output1");
            output.setConverter(converters[i]);
            output.markInitialState();

            output.setConverter(null);

            Object[] state1 = (Object[])output.saveState(facesContext);

            // The null spot force state to be set.
            assertNotNull(state1);
            assertEquals(2, state1.length);
            assertNull(state1[1]); // null spot in state means null Converter
            assertNotNull(state1[0]); //because _isConverterSet()

            UIOutput output2 = new UIOutput();
            output2.setId("output1");
            Converter converter2 = converters2[i];
            output2.setConverter(converter2);
            output2.markInitialState();

            output2.restoreState(facesContext, state1);

            // Check converter is set to null
            assertNull(output2.getConverter());
        }
    }

    @Test
    void converterDeltaState12() throws Exception
    {
        UIOutput output = new UIOutput();
        output.setId("output1");
        output.setConverter(new LongTransientStateHolderConverter());
        output.markInitialState();
        
        Object state1 = output.saveState(facesContext);
        
        // Transient means no state, but the effect is the converter disappear
        // (because is transient, and in Faces 1.2/1.1 that was the effect).
        assertNotNull(state1);
        
        UIOutput output2 = new UIOutput();
        output2.setId("output1");
        Converter converter2 = new LongTransientStateHolderConverter();
        output2.setConverter(converter2);
        output2.markInitialState();
        
        output2.restoreState(facesContext, state1);
        
        // Check no converter should be found
        assertNull(output2.getConverter());
    }

    @Test
    void converterDeltaState13() throws Exception
    {
        UIOutput output = new UIOutput();
        output.setId("output1");
        output.setConverter(new LongTransientPartialStateHolderConverter());
        output.markInitialState();
        
        Object state1 = output.saveState(facesContext);
        
        // Transient means no state, but the effect is the converter disappear
        // (because is transient, and in Faces 1.2/1.1 that was the effect).
        assertNotNull(state1);
        
        UIOutput output2 = new UIOutput();
        output2.setId("output1");
        Converter converter2 = new LongTransientPartialStateHolderConverter();
        output2.setConverter(converter2);
        output2.markInitialState();
        
        output2.restoreState(facesContext, state1);
        
        // Check no converter should be found
        assertNull(output2.getConverter());
    }

    public static class LongSerializableConverter extends LongConverter implements Serializable
    {
        
    }
    
    public static class LongStateHolderConverter extends LongConverter implements StateHolder
    {
        public Object saveState(FacesContext context)
        {
            return null;
        }

        public void restoreState(FacesContext context, Object state)
        {
        }

        public boolean isTransient()
        {
            return false;
        }

        public void setTransient(boolean newTransientValue)
        {
        }

    }
    
    public static class LongTransientStateHolderConverter extends LongConverter implements StateHolder
    {
        public Object saveState(FacesContext context)
        {
            return null;
        }

        public void restoreState(FacesContext context, Object state)
        {
        }

        public boolean isTransient()
        {
            return true;
        }

        public void setTransient(boolean newTransientValue)
        {
        }

    }
    
    public static class LongPartialStateHolderConverter extends LongConverter implements PartialStateHolder
    {
        private boolean _markInitialState;

        public void clearInitialState()
        {
            _markInitialState = false;
        }

        public boolean initialStateMarked()
        {
            return _markInitialState;
        }

        public void markInitialState()
        {
            _markInitialState = true;
        }
        
        public Object saveState(FacesContext context)
        {
            return null;
        }

        public void restoreState(FacesContext context, Object state)
        {
        }

        public boolean isTransient()
        {
            return false;
        }

        public void setTransient(boolean newTransientValue)
        {
        }
    }
    
    public static class LongPartialStateHolderConverter2 
        extends LongPartialStateHolderConverter implements PartialStateHolder
    {
        
        public Object saveState(FacesContext context)
        {
            return 1;
        }

        public void restoreState(FacesContext context, Object state)
        {
            //ignore
        }

        public boolean isTransient()
        {
            return false;
        }

        public void setTransient(boolean newTransientValue)
        {
        }
    }
    
    public static class LongTransientPartialStateHolderConverter
        extends LongPartialStateHolderConverter implements PartialStateHolder
    {
        
        public Object saveState(FacesContext context)
        {
            return 1;
        }

        public void restoreState(FacesContext context, Object state)
        {
            //ignore
        }

        public boolean isTransient()
        {
            return true;
        }

        public void setTransient(boolean newTransientValue)
        {
        }
    }
    
}
