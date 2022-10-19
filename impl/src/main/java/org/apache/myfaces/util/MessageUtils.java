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
package org.apache.myfaces.util;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.el.ValueExpression;
import jakarta.faces.FactoryFinder;
import jakarta.faces.application.Application;
import jakarta.faces.application.ApplicationFactory;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;

import org.apache.myfaces.config.webparameters.MyfacesConfig;
import org.apache.myfaces.util.lang.ClassUtils;

/**
 * Utility class to support multilingual FacesMessages using ResourceBundles.
 * Standard messages are stored at <code>DEFAULT_BUNDLE</code>.<br>
 * The summary of the message is stored at the requested key value. The detail
 * of the message is stored at &lt;messageId&gt;_detail.
 *
 * @see FacesMessage
 * @see java.util.ResourceBundle
 */
public final class MessageUtils
{
    /** Utility class, do not instatiate */
    private MessageUtils()
    {
        // nope
    }

    /** Default bundle for messages (<code>jakarta.faces.Messages</code>) */
    private static final String DEFAULT_BUNDLE = "jakarta.faces.Messages";

    /** Suffix for message details (<code>_detail</code>)*/
    private static final String DETAIL_SUFFIX = "_detail";

    private static Logger log = Logger.getLogger(MessageUtils.class.getName());

    /**
     * @param severity serverity of message
     * @param messageId id of message
     * @param arg arument of message
     *
     * @return generated FacesMessage
     */
    public static FacesMessage getMessage(FacesMessage.Severity severity,
                                          String messageId,
                                          Object arg)
    {
        return getMessage(severity,
                          messageId,
                          new Object[]{arg},
                          FacesContext.getCurrentInstance());
    }
    
    public static FacesMessage getMessage(String bundleBaseName, 
            FacesMessage.Severity severity,
            String messageId,
            Object arg)
    {
        return getMessage(bundleBaseName,
                          severity,
                          messageId,
                          new Object[]{arg},
                          FacesContext.getCurrentInstance());
    }

    /**
     * @param severity serverity of message
     * @param messageId id of message
     * @param args aruments of message
     *
     * @return generated FacesMessage
     */
    public static FacesMessage getMessage(FacesMessage.Severity severity,
                                          String messageId,
                                          Object[] args)
    {
        return getMessage(severity,
                          messageId,
                          args,
                          FacesContext.getCurrentInstance());
    }
    
    public static FacesMessage getMessage(String bundleBaseName, 
            FacesMessage.Severity severity,
            String messageId,
            Object[] args)
    {
        return getMessage(bundleBaseName,
                          severity,
                          messageId,
                          args,
                          FacesContext.getCurrentInstance());
    }    

    public static FacesMessage getMessage(FacesMessage.Severity severity,
                                          String messageId,
                                          Object[] args,
                                          FacesContext facesContext)
    {
        FacesMessage message = getMessage(facesContext, messageId, args);
        message.setSeverity(severity);

        return message;
    }

    public static FacesMessage getMessage(String bundleBaseName,
            FacesMessage.Severity severity,
            String messageId,
            Object[] args,
            FacesContext facesContext)
    {
        FacesMessage message = getMessage(bundleBaseName, facesContext, messageId, args);
        message.setSeverity(severity);
        
        return message;
    }

    public static void addMessage(FacesMessage.Severity severity,
                                  String messageId,
                                  Object[] args)
    {
        addMessage(severity, messageId, args, null, FacesContext.getCurrentInstance());
    }

    public static void addMessage(String bundleBaseName, 
            FacesMessage.Severity severity,
            String messageId,
            Object[] args)
    {
        addMessage(bundleBaseName, severity, messageId, args, null, FacesContext.getCurrentInstance());
    }

    public static void addMessage(FacesMessage.Severity severity,
                                  String messageId,
                                  Object[] args,
                                  FacesContext facesContext)
    {
        addMessage(severity, messageId, args, null, facesContext);
    }

    public static void addMessage(String bundleBaseName, 
            FacesMessage.Severity severity,
            String messageId,
            Object[] args,
            FacesContext facesContext)
    {
        addMessage(bundleBaseName, severity, messageId, args, null, facesContext);
    }

    public static void addMessage(FacesMessage.Severity severity,
                                  String messageId,
                                  Object[] args,
                                  String forClientId)
    {
        addMessage(severity, messageId, args, forClientId, FacesContext.getCurrentInstance());
    }

    public static void addMessage(String bundleBaseName,
            FacesMessage.Severity severity,
            String messageId,
            Object[] args,
            String forClientId)
    {
        addMessage(bundleBaseName, severity, messageId, args, forClientId, FacesContext.getCurrentInstance());
    }

    public static void addMessage(FacesMessage.Severity severity,
                                  String messageId,
                                  Object[] args,
                                  String forClientId,
                                  FacesContext facesContext)
    {
        if(log.isLoggable(Level.FINEST))
        {
          log.finest("adding message " + messageId + " for clientId " + forClientId);
        }
        facesContext.addMessage(forClientId,
                                getMessage(severity, messageId, args, facesContext));
    }

    public static void addMessage(String bundleBaseName,
            FacesMessage.Severity severity,
            String messageId,
            Object[] args,
            String forClientId,
            FacesContext facesContext)
    {
        if(log.isLoggable(Level.FINEST))
        {
          log.finest("adding message " + messageId + " for clientId " + forClientId);
        }
        facesContext.addMessage(forClientId,
                  getMessage(bundleBaseName, severity, messageId, args, facesContext));
    }

    /**
     * Uses <code>MessageFormat</code> and the supplied parameters to fill in the param placeholders in the String.
     *
     * @param locale The <code>Locale</code> to use when performing the substitution.
     * @param msgtext The original parameterized String.
     * @param params The params to fill in the String with.
     * @return The updated String.
     */
    public static String substituteParams(Locale locale, String msgtext, Object[] params)
    {
        String localizedStr = null;
        if(params == null || msgtext == null)
        {
            return msgtext;
        }

        if(locale != null)
        {
            MessageFormat mf = new MessageFormat(msgtext,locale);            
            localizedStr = mf.format(params);
        }
        return localizedStr;
    }

    public static FacesMessage getMessage(String messageId, Object[] params)
    {
        Locale locale = getCurrentLocale();
        return getMessage(locale, messageId, params);
    }

    public static FacesMessage getMessageFromBundle(String bundleBaseName, String messageId, Object[] params)
    {
        Locale locale = null;
        FacesContext context = FacesContext.getCurrentInstance();
        if(context != null && context.getViewRoot() != null)
        {
            locale = context.getViewRoot().getLocale();
            if(locale == null)
            {
                locale = Locale.getDefault();
            }
        }
        else
        {
            locale = Locale.getDefault();
        }
        return getMessageFromBundle(bundleBaseName, context , locale, messageId, params);
    }

    public static FacesMessage getMessage(Locale locale, String messageId, Object[] params)
    {
        String summary = null;
        String detail = null;
        String bundleName = getApplication().getMessageBundle();
        ResourceBundle bundle = null;
        
        ResourceBundle.Control bundleControl = MyfacesConfig.getCurrentInstance().getResourceBundleControl();

        if (bundleName != null)
        {
            try
            {
                if (bundleControl == null)
                {
                    bundle = ResourceBundle.getBundle(bundleName, locale, ClassUtils.getCurrentLoader(bundleName));
                }
                else
                {
                    bundle = ResourceBundle.getBundle(bundleName, locale, ClassUtils.getCurrentLoader(bundleName),
                            bundleControl);
                }

                if (bundle.containsKey(messageId))
                {
                    summary = bundle.getString(messageId);
                }
            }
            catch (MissingResourceException e)
            {
                // NoOp
            }
        }

        if (summary == null)
        {
            try
            {
                if (bundleControl == null)
                {
                    bundle = ResourceBundle.getBundle(DEFAULT_BUNDLE, locale,
                            ClassUtils.getCurrentLoader(DEFAULT_BUNDLE));
                }
                else
                {
                    bundle = ResourceBundle.getBundle(DEFAULT_BUNDLE, locale,
                            ClassUtils.getCurrentLoader(DEFAULT_BUNDLE), bundleControl);
                }

                if (bundle == null)
                {
                    throw new NullPointerException();
                }
                
                if (bundle.containsKey(messageId))
                {
                    summary = bundle.getString(messageId);
                }
            }
            catch(MissingResourceException e)
            {
                // NoOp
            }
        }

        if (summary == null)
        {
            summary = messageId;
        }

        if (bundle == null)
        {
            throw new NullPointerException(
                "Unable to locate ResourceBundle: bundle is null");
        }
        
        String detailMessageId = messageId + DETAIL_SUFFIX;
        if (params != null && locale != null)
        {
            try
            {
                if (bundle.containsKey(detailMessageId))
                {
                    detail = bundle.getString(detailMessageId);
                }
            }
            catch(MissingResourceException e)
            {
                // NoOp
            }
            return new ParametrizableFacesMessage(summary, detail, params, locale);
        }
        else
        {
            summary = substituteParams(locale, summary, params);
            try
            {
                if (bundle.containsKey(detailMessageId))
                {
                    detail = substituteParams(locale, bundle.getString(detailMessageId), params);
                }
            }
            catch(MissingResourceException e)
            {
                // NoOp
            }
            return new FacesMessage(summary, detail);
        }
    }
    
    public static FacesMessage getMessageFromBundle(String bundleBaseName,
                                                    FacesContext context, Locale locale,
                                                    String messageId, Object[] params)
    {
        String summary = null;
        String detail = null;
        String bundleName = context.getApplication().getMessageBundle();
        ResourceBundle bundle = null;
        
        ResourceBundle.Control bundleControl = MyfacesConfig.getCurrentInstance().getResourceBundleControl();

        if (bundleName != null)
        {
            try
            {
                if (bundleControl == null)
                {
                    bundle = ResourceBundle.getBundle(bundleName, locale, ClassUtils.getCurrentLoader(bundleName));
                }
                else
                {
                    bundle = ResourceBundle.getBundle(bundleName, locale, ClassUtils.getCurrentLoader(bundleName),
                            bundleControl);
                }

                if (bundle.containsKey(messageId))
                {
                    summary = bundle.getString(messageId);
                }
            }
            catch (MissingResourceException e)
            {
                // NoOp
            }
        }

        if (summary == null)
        {
            try
            {
                if (bundleControl == null)
                {
                    bundle = ResourceBundle.getBundle(bundleBaseName, locale,
                            ClassUtils.getCurrentLoader(bundleBaseName));
                }
                else
                {
                    bundle = ResourceBundle.getBundle(bundleBaseName, locale,
                            ClassUtils.getCurrentLoader(bundleBaseName), bundleControl);
                }

                if (bundle == null)
                {
                    throw new NullPointerException();
                }
                
                if (bundle.containsKey(messageId))
                {
                    summary = bundle.getString(messageId);
                }
            }
            catch(MissingResourceException e)
            {
                // NoOp
            }
        }
        
        if (summary == null)
        {
            try
            {
                if (bundleControl == null)
                {
                    bundle = ResourceBundle.getBundle(DEFAULT_BUNDLE, locale, 
                            ClassUtils.getCurrentLoader(DEFAULT_BUNDLE));
                }
                else
                {
                    bundle = ResourceBundle.getBundle(DEFAULT_BUNDLE, locale, 
                            ClassUtils.getCurrentLoader(DEFAULT_BUNDLE), bundleControl);
                }

                if (bundle == null)
                {
                    throw new NullPointerException();
                }
                
                if (bundle.containsKey(messageId))
                {
                    summary = bundle.getString(messageId);
                }
            }
            catch(MissingResourceException e)
            {
                // NoOp
            }
        }

        if (summary == null)
        {
            summary = messageId;
        }

        if (bundle == null)
        {
            throw new NullPointerException(
                "Unable to locate ResourceBundle: bundle is null");
        }
        
        String detailMessageId = messageId + DETAIL_SUFFIX;
        if (params != null && locale != null)
        {
            try
            {
                if (bundle.containsKey(detailMessageId))
                {
                    detail = bundle.getString(detailMessageId);
                }
            }
            catch(MissingResourceException e)
            {
                // NoOp
            }
            return new ParametrizableFacesMessage(summary, detail, params, locale);
        }
        else
        {
            summary = substituteParams(locale, summary, params);
            try
            {
                if (bundle.containsKey(detailMessageId))
                {
                    detail = substituteParams(locale, bundle.getString(detailMessageId), params);
                }
            }
            catch(MissingResourceException e)
            {
                // NoOp
            }
            return new FacesMessage(summary, detail);
        }
    }

    /**
     *  Retrieve the message from a specific bundle. It does not look on application message bundle
     * or default message bundle. If it is required to look on those bundles use getMessageFromBundle instead
     * 
     * @param bundleBaseName baseName of ResourceBundle to load localized messages
     * @param messageId id of message
     * @param params parameters to set at localized message
     * @return generated FacesMessage
     */
    public static FacesMessage getMessage(String bundleBaseName, String messageId, Object[] params)
    {
        return getMessage(bundleBaseName, getCurrentLocale(), messageId, params);
    }
    
    /**
     * 
     * @return  currently applicable Locale for this request.
     */
    public static Locale getCurrentLocale()
    {
        return getCurrentLocale(FacesContext.getCurrentInstance());
    }
    
    public static Locale getCurrentLocale(FacesContext context)
    {
        Locale locale;
        if(context != null && context.getViewRoot() != null)
        {
            locale = context.getViewRoot().getLocale();
            if(locale == null)
            {
                locale = Locale.getDefault();
            }
        }
        else
        {
            locale = Locale.getDefault();
        }
        
        return locale;
    }

    /**
     * @param severity severity of message
     * @param bundleBaseName baseName of ResourceBundle to load localized messages
     * @param messageId id of message
     * @param params parameters to set at localized message
     * @return generated FacesMessage
     */
    public static FacesMessage getMessage(FacesMessage.Severity severity, String bundleBaseName,
                                          String messageId, Object[] params)
    {
      FacesMessage msg = getMessage(bundleBaseName, messageId, params);
      msg.setSeverity(severity);

      return msg;
    }

    /**
     *  Retrieve the message from a specific bundle. It does not look on application message bundle
     * or default message bundle. If it is required to look on those bundles use getMessageFromBundle instead
     * 
     * @param bundleBaseName baseName of ResourceBundle to load localized messages
     * @param locale current locale
     * @param messageId id of message
     * @param params parameters to set at localized message
     * @return generated FacesMessage
     */
    public static FacesMessage getMessage(String bundleBaseName, Locale locale, String messageId, Object[] params)
    {
      if (bundleBaseName == null)
      {
          throw new NullPointerException(
              "Unable to locate ResourceBundle: bundle is null");
      }

      ResourceBundle bundle = ResourceBundle.getBundle(bundleBaseName, locale);

      return getMessage(bundle, messageId, params);
    }
    /**
     * @param bundle ResourceBundle to load localized messages
     * @param messageId id of message
     * @param params parameters to set at localized message
     * @return generated FacesMessage
     */
    public static FacesMessage getMessage(ResourceBundle bundle, String messageId, Object[] params)
    {
        String summary = null;
        String detail = null;

        try
        {
            if (bundle.containsKey(messageId))
            {
                summary = bundle.getString(messageId);
            }
        }
        catch (MissingResourceException e)
        {
            // NoOp
        }

        if (summary == null)
        {
            summary = messageId;
        }
        summary = substituteParams(bundle.getLocale(), summary, params);

        try
        {
            String detailMessageId = messageId + DETAIL_SUFFIX;
            if (bundle.containsKey(detailMessageId))
            {
                detail = substituteParams(bundle.getLocale(),
                    bundle.getString(detailMessageId), params);
            }
        }
        catch(MissingResourceException e)
        {
            // NoOp
        }

        return new FacesMessage(summary, detail);
    }

    /**
     *
     * @param context
     * @param messageId
     * @return generated FacesMessage
     */
    public static FacesMessage getMessage(FacesContext context, String messageId)
    {
        return getMessage(context, messageId, ((Object []) (null)));
    }
    
    public static FacesMessage getMessage(String bundleBaseName, FacesContext context, String messageId)
    {
        return getMessage(bundleBaseName, context, messageId, ((Object []) (null)));
    }

    /**
     *
     * @param context
     * @param messageId
     * @param params
     * @return generated FacesMessage
     */
    public static FacesMessage getMessage(FacesContext context, String messageId, Object[] params)
    {
        if(context == null || messageId == null)
        {
            throw new NullPointerException(" context " + context + " messageId " + messageId);
        }
        Locale locale = getCurrentLocale(context);
        if(null == locale)
        {
            throw new NullPointerException(" locale " + locale);
        }
        FacesMessage message = getMessage(locale, messageId, params);
        if(message != null)
        {
            return message;
        } 
        else
        {
            // TODO /FIX:  Note that this has fallback behavior to default Locale for message,
            // but similar behavior above does not.  The methods should probably behave
            locale = Locale.getDefault();
            return getMessage(locale, messageId, params);
        }
    }
    
    public static FacesMessage getMessage(String bundleBaseName, FacesContext context,
                                          String messageId, Object[] params)
    {
        if(context == null || messageId == null)
        {
            throw new NullPointerException(" context " + context + " messageId " + messageId);
        }
        Locale locale = getCurrentLocale(context);
        if(null == locale)
        {
            throw new NullPointerException(" locale " + locale);
        }
        FacesMessage message = getMessageFromBundle(bundleBaseName, context, locale, messageId, params);
        if(message != null)
        {
            return message;
        } 
        else
        {
            // TODO /FIX:  Note that this has fallback behavior to default Locale for message,
            // but similar behavior above does not.  The methods should probably behave
            locale = Locale.getDefault();
            return getMessageFromBundle(bundleBaseName, context, locale, messageId, params);
        }
    }
    
    public static Object getLabel(FacesContext facesContext, UIComponent component)
    {
        Object label = component.getAttributes().get("label");
        ValueExpression expression = null;
        if (label != null && 
            label instanceof String && ((String)label).length() == 0 )
        {
            // Note component.getAttributes().get("label") internally try to 
            // evaluate the EL expression for the label, but in some cases, 
            // when PSS is disabled and f:loadBundle is used, when the view is 
            // restored the bundle is not set to the EL expression returns an 
            // empty String. It is not possible to check if there is a 
            // hardcoded label, but we can check if there is
            // an EL expression set, so the best in this case is use that, and if
            // there is an EL expression set, use it, otherwise use the hardcoded
            // value. See MYFACES-3591 for details.
            expression = component.getValueExpression("label");
            if (expression != null)
            {
                // Set the label to null and use the EL expression instead.
                label = null;
            }
        }
            
        if(label != null)
        {
            return label;
        }
        
        expression = (expression == null) ? component.getValueExpression("label") : expression;
        if(expression != null)
        {
            return expression;
        }
        
        //If no label is not specified, use clientId
        return component.getClientId( facesContext );
    }

    private static Application getApplication()
    {
        FacesContext context = FacesContext.getCurrentInstance();
        if(context != null)
        {
            return context.getApplication();
        }
        else
        {
            ApplicationFactory afactory = (ApplicationFactory)FactoryFinder.getFactory(
                    FactoryFinder.APPLICATION_FACTORY);
            return afactory.getApplication();
        }
    }
}
