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
package org.apache.myfaces.config.impl.digester;


import javax.faces.context.ExternalContext;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.function.Consumer;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.myfaces.config.FacesConfigUnmarshaller;
import org.apache.myfaces.config.element.FacesFlowDefinition;
import org.apache.myfaces.config.impl.digester.elements.AbsoluteOrderingImpl;
import org.apache.myfaces.config.impl.digester.elements.ApplicationImpl;
import org.apache.myfaces.config.impl.digester.elements.AttributeImpl;
import org.apache.myfaces.config.impl.digester.elements.BehaviorImpl;
import org.apache.myfaces.config.impl.digester.elements.ClientBehaviorRendererImpl;
import org.apache.myfaces.config.impl.digester.elements.ConfigOthersSlotImpl;
import org.apache.myfaces.config.impl.digester.elements.ContractMappingImpl;
import org.apache.myfaces.config.impl.digester.elements.ConverterImpl;
import org.apache.myfaces.config.impl.digester.elements.FacesConfigImpl;
import org.apache.myfaces.config.impl.digester.elements.FacesConfigNameSlotImpl;
import org.apache.myfaces.config.impl.digester.elements.FacesFlowDefinitionImpl;
import org.apache.myfaces.config.impl.digester.elements.FactoryImpl;
import org.apache.myfaces.config.impl.digester.elements.LocaleConfigImpl;
import org.apache.myfaces.config.impl.digester.elements.OrderingImpl;
import org.apache.myfaces.config.impl.digester.elements.PropertyImpl;
import org.apache.myfaces.config.impl.digester.elements.RenderKitImpl;
import org.apache.myfaces.config.impl.digester.elements.RendererImpl;
import org.apache.myfaces.config.impl.digester.elements.ResourceBundleImpl;
import org.apache.myfaces.config.impl.digester.elements.SystemEventListenerImpl;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class FacesConfigUnmarshallerImplNew implements FacesConfigUnmarshaller<FacesConfigImpl>
{
    public FacesConfigUnmarshallerImplNew(ExternalContext externalContext)
    {

    }

    private void postProcessFacesConfig(String systemId, FacesConfigImpl config)
    {
        for (org.apache.myfaces.config.element.Application application : config.getApplications())
        {
            for (org.apache.myfaces.config.element.LocaleConfig localeConfig : application.getLocaleConfig())
            {
                if (!localeConfig.getSupportedLocales().contains(localeConfig.getDefaultLocale()))
                {
                    localeConfig.getSupportedLocales().add(localeConfig.getDefaultLocale());
                }
            }
        }
        
        for (FacesFlowDefinition facesFlowDefinition : config.getFacesFlowDefinitions())
        {
            // JSF 2.2 section 11.4.3.1 says this: "... Flows are defined using the 
            // <flow-definition> element. This element must have an id attribute which uniquely 
            // identifies the flow within the scope of the Application Configuration Resource 
            // file in which the element appears. To enable multiple flows with the same id to 
            // exist in an application, the <faces-config><name> element is taken to 
            // be the definingDocumentId of the flow. If no <name> element is specified, 
            // the empty string is taken as the value for definingDocumentId. ..."
            if (config.getName() != null)
            {
                ((FacesFlowDefinitionImpl)facesFlowDefinition).setDefiningDocumentId(
                    config.getName());
            }
            else
            {
                ((FacesFlowDefinitionImpl)facesFlowDefinition).setDefiningDocumentId("");
            }
        }
    }
    
    @Override
    public FacesConfigImpl getFacesConfig(InputStream in, String systemId) throws IOException, SAXException
    {
        FacesConfigImpl facesConfig = new FacesConfigImpl();

        try
        {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setValidating(false);
            factory.setNamespaceAware(true);
            
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(in);
            document.getDocumentElement().normalize();
            
            onAttribute("metadata-complete", document.getDocumentElement(),
                    (v) -> { facesConfig.setMetadataComplete(v); });
            onAttribute("version", document.getDocumentElement(),
                    (v) -> { facesConfig.setVersion(v); });

            onChild("name", document.getDocumentElement(), 
                    (n) -> { facesConfig.setName(n.getTextContent()); });
            onChild("ordering", document.getDocumentElement(), 
                    (n) -> { facesConfig.setOrdering(processOrdering(n)); });
            onChild("absolute-ordering", document.getDocumentElement(), 
                    (n) -> { facesConfig.setAbsoluteOrdering(processAbsoluteOrdering(n)); });
            onChild("application", document.getDocumentElement(), 
                    (n) -> { facesConfig.addApplication(processApplication(n)); });
            onChild("factory", document.getDocumentElement(), 
                    (n) -> { facesConfig.addFactory(processFactory(n)); });
            onChild("component", document.getDocumentElement(), (n) -> {
                facesConfig.addComponent(
                        firstChildTextContent("component-type", n),
                        firstChildTextContent("component-class", n));
            });
            onChild("lifecycle", document.getDocumentElement(), (n) -> {
                onChild("phase-listener", n, (cn) -> {
                    facesConfig.addLifecyclePhaseListener(cn.getTextContent()); 
                });  
            });
            onChild("validator", document.getDocumentElement(), (n) -> {
                facesConfig.addValidator(
                        firstChildTextContent("validator-id", n),
                        firstChildTextContent("validator-class", n));
            });
            onChild("render-kit", document.getDocumentElement(),
                    (n) -> { facesConfig.addRenderKit(processRenderKit(n)); });
            onChild("behavior", document.getDocumentElement(),
                    (n) -> { facesConfig.addBehavior(processBehavior(n)); });
            onChild("converter", document.getDocumentElement(),
                    (n) -> { facesConfig.addConverter(processConverter(n)); });
            
            
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            in.close();
        }
            

            

            
            
                    
        postProcessFacesConfig(systemId, facesConfig);
 

        return facesConfig;
    }
    
    @Override
    public FacesConfigImpl getFacesConfig(Reader r) throws IOException, SAXException
    {
        return null;
    }

    @Override
    public void release()
    {

    }
    
    
    
    protected OrderingImpl processOrdering(Node node)
    {
        OrderingImpl obj = new OrderingImpl();
        
        onChild("before", node, (n) -> {
            onChild("name", n, (cn) -> { 
                obj.addBeforeSlot(new FacesConfigNameSlotImpl(cn.getTextContent()));
            });
            onChild("others", n, (cn) -> {
                obj.addBeforeSlot(new ConfigOthersSlotImpl());
            });
        });

        onChild("after", node, (n) -> {
            onChild("name", n, (cn) -> { 
                obj.addAfterSlot(new FacesConfigNameSlotImpl(cn.getTextContent()));
            });
            onChild("others", n, (cn) -> {
                obj.addAfterSlot(new ConfigOthersSlotImpl());
            });
        });

        return obj;
    }
    
    protected AbsoluteOrderingImpl processAbsoluteOrdering(Node node)
    {
        AbsoluteOrderingImpl obj = new AbsoluteOrderingImpl();
        
        onChild("name", node, (n) -> {
            obj.addOrderSlot(new FacesConfigNameSlotImpl(n.getTextContent()));
        });

        onChild("others", node, (n) -> {
            obj.addOrderSlot(new ConfigOthersSlotImpl());
        });

        return obj;
    }
    
    protected ApplicationImpl processApplication(Node node)
    {
        ApplicationImpl obj = new ApplicationImpl();
        
        onChild("action-listener", node, (n) -> { obj.addActionListener(n.getTextContent()); });
        onChild("message-bundle", node, (n) -> { obj.addMessageBundle(n.getTextContent()); });
        onChild("navigation-handler", node, (n) -> { obj.addNavigationHandler(n.getTextContent()); });
        onChild("partial-traversal", node, (n) -> { obj.addPartialTraversal(n.getTextContent()); });
        onChild("view-handler", node, (n) -> { obj.addViewHandler(n.getTextContent()); });
        onChild("state-manager", node, (n) -> { obj.addStateManager(n.getTextContent()); });
        onChild("property-resolver", node, (n) -> { obj.addPropertyResolver(n.getTextContent()); });
        onChild("variable-resolver", node, (n) -> { obj.addVariableResolver(n.getTextContent()); });
        onChild("el-resolver", node, (n) -> { obj.addElResolver(n.getTextContent()); });
        onChild("resource-handler", node, (n) -> { obj.addResourceHandler(n.getTextContent()); });
        onChild("default-render-kit-id", node, (n) -> { obj.addDefaultRenderkitId(n.getTextContent()); });
        onChild("search-expression-handler", node, (n) -> { obj.addSearchExpressionHandler(n.getTextContent()); });
        onChild("search-keyword-resolver", node, (n) -> { obj.addSearchKeywordResolver(n.getTextContent()); });

        onChild("default-validators", node, (n) -> {
            obj.setDefaultValidatorsPresent();
            onChild("validator-id", n, (cn) -> {
                obj.addDefaultValidatorId(cn.getTextContent());
            });
        });
        
        onChild("locale-config", node, (n) -> {
            LocaleConfigImpl lc = new LocaleConfigImpl();
            obj.addLocaleConfig(lc);
            onChild("default-locale", n, (cn) -> { lc.setDefaultLocale(cn.getTextContent()); });
            onChild("supported-locale", n, (cn) -> { lc.addSupportedLocale(cn.getTextContent()); });
        });

        onChild("resource-bundle", node, (n) -> {
            ResourceBundleImpl rb = new ResourceBundleImpl();
            obj.addResourceBundle(rb);
            onChild("base-name", n, (cn) -> { rb.setBaseName(cn.getTextContent()); });
            onChild("var", n, (cn) -> { rb.setVar(cn.getTextContent()); });
            onChild("display-name", n, (cn) -> { rb.setDisplayName(cn.getTextContent()); });
        });
        
        onChild("system-event-listener", node, (n) -> {
            SystemEventListenerImpl sel = new SystemEventListenerImpl();
            obj.addSystemEventListener(sel);
            onChild("system-event-listener-class", n, (cn) -> {
                sel.setSystemEventListenerClass(cn.getTextContent());
            });
            onChild("system-event-class", n, (cn) -> {
                sel.setSystemEventClass(cn.getTextContent());
            });
            onChild("source-class", n, (cn) -> {
                sel.setSourceClass(cn.getTextContent());
            });
        });

        onChild("resource-library-contracts", node, (n) -> {
            onChild("contract-mapping", n, (cn) -> {
                ContractMappingImpl cm = new ContractMappingImpl();
                obj.addResourceLibraryContractMapping(cm);
                onChild("url-pattern", cn, (ccn) -> {
                    cm.addUrlPattern(ccn.getTextContent());
                });
                onChild("contracts", cn, (ccn) -> {
                    cm.addContract(ccn.getTextContent());
                });
            });
        });

        return obj;
    }
    
    protected FactoryImpl processFactory(Node node)
    {
        FactoryImpl obj = new FactoryImpl();
        
        onChild("application-factory", node, (n) -> { obj.addApplicationFactory(n.getTextContent()); });
        onChild("faces-context-factory", node, (n) -> { obj.addFacesContextFactory(n.getTextContent()); });
        onChild("lifecycle-factory", node, (n) -> { obj.addLifecycleFactory(n.getTextContent()); });
        onChild("render-kit-factory", node, (n) -> { obj.addRenderkitFactory(n.getTextContent()); });
        onChild("exception-handler-factory", node, (n) -> { obj.addExceptionHandlerFactory(n.getTextContent()); });
        onChild("external-context-factory", node, (n) -> { obj.addExternalContextFactory(n.getTextContent()); });
        onChild("view-declaration-language-factory", node, (n) -> {
            obj.addViewDeclarationLanguageFactory(n.getTextContent());
        });
        onChild("partial-view-context-factory", node, (n) -> {
            obj.addPartialViewContextFactory(n.getTextContent());
        });
        onChild("tag-handler-delegate-factory", node, (n) -> {
            obj.addTagHandlerDelegateFactory(n.getTextContent());
        });
        onChild("visit-context-factory", node, (n) -> { obj.addVisitContextFactory(n.getTextContent()); });
        onChild("search-expression-context-factory", node, (n) -> {
            obj.addSearchExpressionContextFactory(n.getTextContent());
        });
        onChild("facelet-cache-factory", node, (n) -> { obj.addFaceletCacheFactory(n.getTextContent()); });
        onChild("flash-factory", node, (n) -> { obj.addFlashFactory(n.getTextContent()); });
        onChild("flow-handler-factory", node, (n) -> { obj.addFlowHandlerFactory(n.getTextContent()); });
        onChild("client-window-factory", node, (n) -> { obj.addClientWindowFactory(n.getTextContent()); });
        
        return obj;
    }
    
    protected RenderKitImpl processRenderKit(Node node)
    {
        RenderKitImpl obj = new RenderKitImpl();

        onChild("render-kit-id", node, (n) -> { obj.setId(n.getTextContent()); });
        onChild("render-kit-class", node, (n) -> { obj.addRenderKitClass(n.getTextContent()); });

        onChild("renderer", node, (n) -> {
            RendererImpl r = new RendererImpl();
            obj.addRenderer(r);
            onChild("component-family", n, (cn) -> { r.setComponentFamily(cn.getTextContent()); });
            onChild("renderer-type", n, (cn) -> { r.setRendererType(cn.getTextContent()); });
            onChild("renderer-class", n, (cn) -> { r.setRendererClass(cn.getTextContent()); });
        });
        
        onChild("client-behavior-renderer", node, (n) -> {
            ClientBehaviorRendererImpl r = new ClientBehaviorRendererImpl();
            obj.addClientBehaviorRenderer(r);
            onChild("client-behavior-renderer-type", n, (cn) -> { r.setRendererType(cn.getTextContent()); });
            onChild("client-behavior-renderer-class", n, (cn) -> { r.setRendererClass(cn.getTextContent()); });
        });

        return obj;
    }
    
    protected BehaviorImpl processBehavior(Node node)
    {
        BehaviorImpl obj = new BehaviorImpl();

        onChild("behavior-id", node, (n) -> { obj.setBehaviorId(n.getTextContent()); });
        onChild("behavior-class", node, (n) -> { obj.setBehaviorId(n.getTextContent()); });
                
        onChild("attribute", node, (n) -> {
            AttributeImpl a = new AttributeImpl();
            obj.addAttribute(a);
            onChild("description", n, (cn) -> { a.addDescription(cn.getTextContent()); });
            onChild("display-name", n, (cn) -> { a.addDisplayName(cn.getTextContent()); });
            onChild("icon", n, (cn) -> { a.addIcon(cn.getTextContent()); });
            onChild("attribute-name", n, (cn) -> { a.setAttributeName(cn.getTextContent()); });
            onChild("attribute-class", n, (cn) -> { a.setAttributeClass(cn.getTextContent()); });
            onChild("default-value", n, (cn) -> { a.setDefaultValue(cn.getTextContent()); });
            onChild("suggested-value", n, (cn) -> { a.setSuggestedValue(cn.getTextContent()); });
            onChild("attribute-extension", n, (cn) -> { a.addAttributeExtension(cn.getTextContent()); });
        });
        
        onChild("property", node, (n) -> {
            PropertyImpl p = new PropertyImpl();
            obj.addProperty(p);
            onChild("description", n, (cn) -> { p.addDescription(cn.getTextContent()); });
            onChild("display-name", n, (cn) -> { p.addDisplayName(cn.getTextContent()); });
            onChild("icon", n, (cn) -> { p.addIcon(cn.getTextContent()); });
            onChild("property-name", n, (cn) -> { p.setPropertyName(cn.getTextContent()); });
            onChild("property-class", n, (cn) -> { p.setPropertyClass(cn.getTextContent()); });
            onChild("default-value", n, (cn) -> { p.setDefaultValue(cn.getTextContent()); });
            onChild("suggested-value", n, (cn) -> { p.setSuggestedValue(cn.getTextContent()); });
            onChild("property-extension", n, (cn) -> { p.addPropertyExtension(cn.getTextContent()); });
        });
        
        return obj;
    }

    protected ConverterImpl processConverter(Node node)
    {
        ConverterImpl obj = new ConverterImpl();

        onChild("converter-id", node, (n) -> { obj.setConverterId(n.getTextContent()); });
        onChild("converter-for-class", node, (n) -> { obj.setForClass(n.getTextContent()); });
        onChild("converter-class", node, (n) -> { obj.setConverterClass(n.getTextContent()); });
                
        onChild("attribute", node, (n) -> {
            AttributeImpl a = new AttributeImpl();
            obj.addAttribute(a);
            onChild("description", n, (cn) -> { a.addDescription(cn.getTextContent()); });
            onChild("display-name", n, (cn) -> { a.addDisplayName(cn.getTextContent()); });
            onChild("icon", n, (cn) -> { a.addIcon(cn.getTextContent()); });
            onChild("attribute-name", n, (cn) -> { a.setAttributeName(cn.getTextContent()); });
            onChild("attribute-class", n, (cn) -> { a.setAttributeClass(cn.getTextContent()); });
            onChild("default-value", n, (cn) -> { a.setDefaultValue(cn.getTextContent()); });
            onChild("suggested-value", n, (cn) -> { a.setSuggestedValue(cn.getTextContent()); });
            onChild("attribute-extension", n, (cn) -> { a.addAttributeExtension(cn.getTextContent()); });
        });
        
        onChild("property", node, (n) -> {
            PropertyImpl p = new PropertyImpl();
            obj.addProperty(p);
            onChild("description", n, (cn) -> { p.addDescription(cn.getTextContent()); });
            onChild("display-name", n, (cn) -> { p.addDisplayName(cn.getTextContent()); });
            onChild("icon", n, (cn) -> { p.addIcon(cn.getTextContent()); });
            onChild("property-name", n, (cn) -> { p.setPropertyName(cn.getTextContent()); });
            onChild("property-class", n, (cn) -> { p.setPropertyClass(cn.getTextContent()); });
            onChild("default-value", n, (cn) -> { p.setDefaultValue(cn.getTextContent()); });
            onChild("suggested-value", n, (cn) -> { p.setSuggestedValue(cn.getTextContent()); });
            onChild("property-extension", n, (cn) -> { p.addPropertyExtension(cn.getTextContent()); });
        });

        return obj;
    }
    
    
    
    
    
    
    protected void onAttribute(String name, Node node, Consumer<String> val)
    {
        if (node instanceof Element)
        {
            Element element = (Element) node;
            if (element.hasAttribute(name))
            {
                val.accept(element.getAttribute(name));
            }
        }
    }
    
    protected void onChild(String name, Node node, Consumer<Node> val)
    {
        if (node.getChildNodes() != null)
        {
            for (int i = 0; i < node.getChildNodes().getLength(); i++)
            {
                Node childNode = node.getChildNodes().item(i);
                if (childNode == null)
                {
                    continue;
                }

                if (name.equals(childNode.getLocalName()))
                {
                    val.accept(childNode);
                }
            }
        }
    }
    
    protected String firstChildTextContent(String name, Node node)
    {
        if (node.getChildNodes() != null)
        {
            for (int i = 0; i < node.getChildNodes().getLength(); i++)
            {
                Node childNode = node.getChildNodes().item(i);
                if (childNode == null)
                {
                    continue;
                }

                if (name.equals(childNode.getLocalName()))
                {
                    return childNode.getTextContent();
                }
            }
        }

        return null;
    }
}
