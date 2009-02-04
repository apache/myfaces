/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sun.facelets.tag;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.el.ELException;
import javax.faces.FacesException;
import javax.faces.convert.Converter;
import javax.faces.validator.Validator;

import javax.faces.webapp.pdl.facelets.FaceletContext;
import javax.faces.webapp.pdl.facelets.FaceletException;
import javax.faces.webapp.pdl.facelets.FaceletHandler;
import com.sun.facelets.tag.jsf.ComponentConfig;
import com.sun.facelets.tag.jsf.ComponentHandler;
import com.sun.facelets.tag.jsf.ConvertHandler;
import com.sun.facelets.tag.jsf.ConverterConfig;
import com.sun.facelets.tag.jsf.ValidateHandler;
import com.sun.facelets.tag.jsf.ValidatorConfig;

/**
 * Base class for defining TagLibraries in Java
 * 
 * @author Jacob Hookom
 * @version $Id: AbstractTagLibrary.java,v 1.10 2008/07/13 19:01:36 rlubke Exp $
 */
public abstract class AbstractTagLibrary implements TagLibrary
{

    private static class ValidatorConfigWrapper implements ValidatorConfig
    {

        private final TagConfig parent;
        private final String validatorId;

        public ValidatorConfigWrapper(TagConfig parent, String validatorId)
        {
            this.parent = parent;
            this.validatorId = validatorId;
        }

        public String getValidatorId()
        {
            return this.validatorId;
        }

        public FaceletHandler getNextHandler()
        {
            return this.parent.getNextHandler();
        }

        public Tag getTag()
        {
            return this.parent.getTag();
        }

        public String getTagId()
        {
            return this.parent.getTagId();
        }
    }

    private static class ConverterConfigWrapper implements ConverterConfig
    {
        private final TagConfig parent;
        private final String converterId;

        public ConverterConfigWrapper(TagConfig parent, String converterId)
        {
            this.parent = parent;
            this.converterId = converterId;
        }

        public String getConverterId()
        {
            return this.converterId;
        }

        public FaceletHandler getNextHandler()
        {
            return this.parent.getNextHandler();
        }

        public Tag getTag()
        {
            return this.parent.getTag();
        }

        public String getTagId()
        {
            return this.parent.getTagId();
        }
    }

    private static class HandlerFactory implements TagHandlerFactory
    {
        private final static Class[] CONSTRUCTOR_SIG = new Class[] { TagConfig.class };

        protected final Class handlerType;

        public HandlerFactory(Class handlerType)
        {
            this.handlerType = handlerType;
        }

        public TagHandler createHandler(TagConfig cfg) throws FacesException, ELException
        {
            try
            {
                return (TagHandler) this.handlerType.getConstructor(CONSTRUCTOR_SIG).newInstance(new Object[] { cfg });
            }
            catch (InvocationTargetException ite)
            {
                Throwable t = ite.getCause();
                if (t instanceof FacesException)
                {
                    throw (FacesException) t;
                }
                else if (t instanceof ELException)
                {
                    throw (ELException) t;
                }
                else
                {
                    throw new FacesException("Error Instantiating: " + this.handlerType.getName(), t);
                }
            }
            catch (Exception e)
            {
                throw new FacesException("Error Instantiating: " + this.handlerType.getName(), e);
            }
        }
    }

    private static class ComponentConfigWrapper implements ComponentConfig
    {

        protected final TagConfig parent;

        protected final String componentType;

        protected final String rendererType;

        public ComponentConfigWrapper(TagConfig parent, String componentType, String rendererType)
        {
            this.parent = parent;
            this.componentType = componentType;
            this.rendererType = rendererType;
        }

        public String getComponentType()
        {
            return this.componentType;
        }

        public String getRendererType()
        {
            return this.rendererType;
        }

        public FaceletHandler getNextHandler()
        {
            return this.parent.getNextHandler();
        }

        public Tag getTag()
        {
            return this.parent.getTag();
        }

        public String getTagId()
        {
            return this.parent.getTagId();
        }
    }

    private static class UserTagFactory implements TagHandlerFactory
    {
        protected final URL location;

        public UserTagFactory(URL location)
        {
            this.location = location;
        }

        public TagHandler createHandler(TagConfig cfg) throws FacesException, ELException
        {
            return new UserTagHandler(cfg, this.location);
        }
    }

    private static class ComponentHandlerFactory implements TagHandlerFactory
    {

        protected final String componentType;

        protected final String renderType;

        /**
         * @param handlerType
         */
        public ComponentHandlerFactory(String componentType, String renderType)
        {
            this.componentType = componentType;
            this.renderType = renderType;
        }

        public TagHandler createHandler(TagConfig cfg) throws FacesException, ELException
        {
            ComponentConfig ccfg = new ComponentConfigWrapper(cfg, this.componentType, this.renderType);
            return new ComponentHandler(ccfg);
        }
    }

    private static class UserComponentHandlerFactory implements TagHandlerFactory
    {

        private final static Class[] CONS_SIG = new Class[] { ComponentConfig.class };

        protected final String componentType;

        protected final String renderType;

        protected final Class type;

        protected final Constructor constructor;

        /**
         * @param handlerType
         */
        public UserComponentHandlerFactory(String componentType, String renderType, Class type)
        {
            this.componentType = componentType;
            this.renderType = renderType;
            this.type = type;
            try
            {
                this.constructor = this.type.getConstructor(CONS_SIG);
            }
            catch (Exception e)
            {
                throw new FaceletException("Must have a Constructor that takes in a ComponentConfig", e);
            }
        }

        public TagHandler createHandler(TagConfig cfg) throws FacesException, ELException
        {
            try
            {
                ComponentConfig ccfg = new ComponentConfigWrapper(cfg, this.componentType, this.renderType);
                return (TagHandler) this.constructor.newInstance(new Object[] { ccfg });
            }
            catch (InvocationTargetException e)
            {
                throw new FaceletException(e.getCause().getMessage(), e.getCause().getCause());
            }
            catch (Exception e)
            {
                throw new FaceletException("Error Instantiating ComponentHandler: " + this.type.getName(), e);
            }
        }
    }

    private static class ValidatorHandlerFactory implements TagHandlerFactory
    {

        protected final String validatorId;

        public ValidatorHandlerFactory(String validatorId)
        {
            this.validatorId = validatorId;
        }

        public TagHandler createHandler(TagConfig cfg) throws FacesException, ELException
        {
            return new ValidateHandler(new ValidatorConfigWrapper(cfg, this.validatorId));
        }
    }

    private static class ConverterHandlerFactory implements TagHandlerFactory
    {

        protected final String converterId;

        public ConverterHandlerFactory(String converterId)
        {
            this.converterId = converterId;
        }

        public TagHandler createHandler(TagConfig cfg) throws FacesException, ELException
        {
            return new ConvertHandler(new ConverterConfigWrapper(cfg, this.converterId));
        }
    }

    private static class UserConverterHandlerFactory implements TagHandlerFactory
    {
        private final static Class[] CONS_SIG = new Class[] { ConverterConfig.class };

        protected final String converterId;

        protected final Class type;

        protected final Constructor constructor;

        public UserConverterHandlerFactory(String converterId, Class type)
        {
            this.converterId = converterId;
            this.type = type;
            try
            {
                this.constructor = this.type.getConstructor(CONS_SIG);
            }
            catch (Exception e)
            {
                throw new FaceletException("Must have a Constructor that takes in a ConverterConfig", e);
            }
        }

        public TagHandler createHandler(TagConfig cfg) throws FacesException, ELException
        {
            try
            {
                ConverterConfig ccfg = new ConverterConfigWrapper(cfg, this.converterId);
                return (TagHandler) this.constructor.newInstance(new Object[] { ccfg });
            }
            catch (InvocationTargetException e)
            {
                throw new FaceletException(e.getCause().getMessage(), e.getCause().getCause());
            }
            catch (Exception e)
            {
                throw new FaceletException("Error Instantiating ConverterHandler: " + this.type.getName(), e);
            }
        }
    }

    private static class UserValidatorHandlerFactory implements TagHandlerFactory
    {
        private final static Class[] CONS_SIG = new Class[] { ValidatorConfig.class };

        protected final String validatorId;

        protected final Class type;

        protected final Constructor constructor;

        public UserValidatorHandlerFactory(String validatorId, Class type)
        {
            this.validatorId = validatorId;
            this.type = type;
            try
            {
                this.constructor = this.type.getConstructor(CONS_SIG);
            }
            catch (Exception e)
            {
                throw new FaceletException("Must have a Constructor that takes in a ConverterConfig", e);
            }
        }

        public TagHandler createHandler(TagConfig cfg) throws FacesException, ELException
        {
            try
            {
                ValidatorConfig ccfg = new ValidatorConfigWrapper(cfg, this.validatorId);
                return (TagHandler) this.constructor.newInstance(new Object[] { ccfg });
            }
            catch (InvocationTargetException e)
            {
                throw new FaceletException(e.getCause().getMessage(), e.getCause().getCause());
            }
            catch (Exception e)
            {
                throw new FaceletException("Error Instantiating ValidatorHandler: " + this.type.getName(), e);
            }
        }
    }

    private final Map factories;

    private final String namespace;

    private final Map functions;

    public AbstractTagLibrary(String namespace)
    {
        this.namespace = namespace;
        this.factories = new HashMap();
        this.functions = new HashMap();
    }

    /**
     * Add a ComponentHandler with the specified componentType and rendererType, aliased by the tag name.
     * 
     * @see ComponentHandler
     * @see javax.faces.application.Application#createComponent(java.lang.String)
     * @param name
     *            name to use, "foo" would be &lt;my:foo />
     * @param componentType
     *            componentType to use
     * @param rendererType
     *            rendererType to use
     */
    protected final void addComponent(String name, String componentType, String rendererType)
    {
        this.factories.put(name, new ComponentHandlerFactory(componentType, rendererType));
    }

    /**
     * Add a ComponentHandler with the specified componentType and rendererType, aliased by the tag name. The Facelet
     * will be compiled with the specified HandlerType (which must extend AbstractComponentHandler).
     * 
     * @see AbstractComponentHandler
     * @param name
     *            name to use, "foo" would be &lt;my:foo />
     * @param componentType
     *            componentType to use
     * @param rendererType
     *            rendererType to use
     * @param handlerType
     *            a Class that extends AbstractComponentHandler
     */
    protected final void addComponent(String name, String componentType, String rendererType, Class handlerType)
    {
        this.factories.put(name, new UserComponentHandlerFactory(componentType, rendererType, handlerType));
    }

    /**
     * Add a ConvertHandler for the specified converterId
     * 
     * @see ConvertHandler
     * @see javax.faces.application.Application#createConverter(java.lang.String)
     * @param name
     *            name to use, "foo" would be &lt;my:foo />
     * @param converterId
     *            id to pass to Application instance
     */
    protected final void addConverter(String name, String converterId)
    {
        this.factories.put(name, new ConverterHandlerFactory(converterId));
    }

    /**
     * Add a ConvertHandler for the specified converterId of a TagHandler type
     * 
     * @see ConvertHandler
     * @see ConverterConfig
     * @see javax.faces.application.Application#createConverter(java.lang.String)
     * @param name
     *            name to use, "foo" would be &lt;my:foo />
     * @param converterId
     *            id to pass to Application instance
     * @param type
     *            TagHandler type that takes in a ConverterConfig
     */
    protected final void addConverter(String name, String converterId, Class type)
    {
        this.factories.put(name, new UserConverterHandlerFactory(converterId, type));
    }

    /**
     * Add a ValidateHandler for the specified validatorId
     * 
     * @see ValidateHandler
     * @see javax.faces.application.Application#createValidator(java.lang.String)
     * @param name
     *            name to use, "foo" would be &lt;my:foo />
     * @param validatorId
     *            id to pass to Application instance
     */
    protected final void addValidator(String name, String validatorId)
    {
        this.factories.put(name, new ValidatorHandlerFactory(validatorId));
    }

    /**
     * Add a ValidateHandler for the specified validatorId
     * 
     * @see ValidateHandler
     * @see ValidatorConfig
     * @see javax.faces.application.Application#createValidator(java.lang.String)
     * @param name
     *            name to use, "foo" would be &lt;my:foo />
     * @param validatorId
     *            id to pass to Application instance
     * @param type
     *            TagHandler type that takes in a ValidatorConfig
     */
    protected final void addValidator(String name, String validatorId, Class type)
    {
        this.factories.put(name, new UserValidatorHandlerFactory(validatorId, type));
    }

    /**
     * Use the specified HandlerType in compiling Facelets. HandlerType must extend TagHandler.
     * 
     * @see TagHandler
     * @param name
     *            name to use, "foo" would be &lt;my:foo />
     * @param handlerType
     *            must extend TagHandler
     */
    protected final void addTagHandler(String name, Class handlerType)
    {
        this.factories.put(name, new HandlerFactory(handlerType));
    }

    /**
     * Add a UserTagHandler specified a the URL source.
     * 
     * @see UserTagHandler
     * @param name
     *            name to use, "foo" would be &lt;my:foo />
     * @param source
     *            source where the Facelet (Tag) source is
     */
    protected final void addUserTag(String name, URL source)
    {
        this.factories.put(name, new UserTagFactory(source));
    }

    /**
     * Add a Method to be used as a Function at Compilation.
     * 
     * @see javax.el.FunctionMapper
     * 
     * @param name
     *            (suffix) of function name
     * @param method
     *            method instance
     */
    protected final void addFunction(String name, Method method)
    {
        this.functions.put(name, method);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sun.facelets.tag.TagLibrary#containsNamespace(java.lang.String)
     */
    public boolean containsNamespace(String ns)
    {
        return this.namespace.equals(ns);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sun.facelets.tag.TagLibrary#containsTagHandler(java.lang.String, java.lang.String)
     */
    public boolean containsTagHandler(String ns, String localName)
    {
        if (this.namespace.equals(ns))
        {
            if (this.factories.containsKey(localName))
            {
                return true;
            }
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sun.facelets.tag.TagLibrary#createTagHandler(java.lang.String, java.lang.String,
     * com.sun.facelets.tag.TagConfig)
     */
    public TagHandler createTagHandler(String ns, String localName, TagConfig tag) throws FacesException
    {
        if (this.namespace.equals(ns))
        {
            TagHandlerFactory f = (TagHandlerFactory) this.factories.get(localName);
            if (f != null)
            {
                return f.createHandler(tag);
            }
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sun.facelets.tag.TagLibrary#containsFunction(java.lang.String, java.lang.String)
     */
    public boolean containsFunction(String ns, String name)
    {
        if (this.namespace.equals(ns))
        {
            return this.functions.containsKey(name);
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sun.facelets.tag.TagLibrary#createFunction(java.lang.String, java.lang.String)
     */
    public Method createFunction(String ns, String name)
    {
        if (this.namespace.equals(ns))
        {
            return (Method) this.functions.get(name);
        }
        return null;
    }

    public String getNamespace()
    {
        return namespace;
    }
}
