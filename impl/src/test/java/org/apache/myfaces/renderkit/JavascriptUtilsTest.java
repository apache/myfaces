package org.apache.myfaces.renderkit;

import org.apache.myfaces.renderkit.html.util.JavascriptUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.stubbing.Answer;

import static org.apache.myfaces.renderkit.html.util.JavascriptUtils.getValidJavascriptName;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class JavascriptUtilsTest {

    private MockedStatic<JavascriptUtils> javascriptUtilsMockedStatic;

    @BeforeEach
    public void setUp() {
        javascriptUtilsMockedStatic = mockStatic(JavascriptUtils.class);

        javascriptUtilsMockedStatic.when(() -> getValidJavascriptName(anyString(), anyBoolean(), anyBoolean()))
                .thenCallRealMethod();
        javascriptUtilsMockedStatic.when(() -> getValidJavascriptName(anyString(), eq(true)))
                .thenAnswer(returnInputParameter1());
    }

    @AfterEach
    public void tearDown() {
        javascriptUtilsMockedStatic.close();
    }

    @Test
    @DisplayName("Passes namespaces without namespace encoding when allowNamespaces is true")
    public void testGetValidJavascriptNameWithNamespaces() {
        // WHEN
        String result = getValidJavascriptName("testName.testname2", true, true);

        // THEN
        assertEquals("testName.testname2", result);
        javascriptUtilsMockedStatic.verify(() -> getValidJavascriptName(anyString(), anyBoolean()), times(2));
    }

    @Test
    @DisplayName("Returns input name without modifications when no namespace is provided")
    public void testGetValidJavascriptNameWithoutNamespace() {
        // WHEN
        String result = getValidJavascriptName("testName", true, true);

        // THEN
        assertEquals("testName", result);
        javascriptUtilsMockedStatic.verify(() -> getValidJavascriptName(anyString(), anyBoolean()), times(1));
    }

    @Test
    @DisplayName("Handles namespaced input correctly without modification when allowNamespaces is false")
    public void testGetValidJavascriptNameWithNamespacedInput() {
        // WHEN
        String result = getValidJavascriptName("testName.namespace", false, true);

        // THEN
        assertEquals("testName.namespace", result); // Add assertion for clarity
        javascriptUtilsMockedStatic.verify(() -> getValidJavascriptName(anyString(), anyBoolean()), times(1));
    }

    private static Answer<Object> returnInputParameter1() {
        return invocation -> {
            Object[] args = invocation.getArguments();
            return args[0]; 
        };
    }
}
