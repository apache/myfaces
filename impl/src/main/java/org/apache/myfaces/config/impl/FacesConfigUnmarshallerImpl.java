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
package org.apache.myfaces.config.impl;


import java.io.ByteArrayInputStream;
import javax.faces.context.ExternalContext;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.Consumer;
import javax.faces.FacesException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.myfaces.config.FacesConfigUnmarshaller;
import org.apache.myfaces.config.element.FacesFlowDefinition;
import org.apache.myfaces.config.impl.elements.AbsoluteOrderingImpl;
import org.apache.myfaces.config.impl.elements.ApplicationImpl;
import org.apache.myfaces.config.impl.elements.AttributeImpl;
import org.apache.myfaces.config.impl.elements.BehaviorImpl;
import org.apache.myfaces.config.impl.elements.ClientBehaviorRendererImpl;
import org.apache.myfaces.config.impl.elements.ConfigOthersSlotImpl;
import org.apache.myfaces.config.impl.elements.ContractMappingImpl;
import org.apache.myfaces.config.impl.elements.ConverterImpl;
import org.apache.myfaces.config.impl.elements.FaceletsProcessingImpl;
import org.apache.myfaces.config.impl.elements.FaceletsTemplateMappingImpl;
import org.apache.myfaces.config.impl.elements.FacesConfigExtensionImpl;
import org.apache.myfaces.config.impl.elements.FacesConfigImpl;
import org.apache.myfaces.config.impl.elements.FacesConfigNameSlotImpl;
import org.apache.myfaces.config.impl.elements.FacesFlowCallImpl;
import org.apache.myfaces.config.impl.elements.FacesFlowDefinitionImpl;
import org.apache.myfaces.config.impl.elements.FacesFlowMethodCallImpl;
import org.apache.myfaces.config.impl.elements.FacesFlowMethodParameterImpl;
import org.apache.myfaces.config.impl.elements.FacesFlowParameterImpl;
import org.apache.myfaces.config.impl.elements.FacesFlowReferenceImpl;
import org.apache.myfaces.config.impl.elements.FacesFlowReturnImpl;
import org.apache.myfaces.config.impl.elements.FacesFlowSwitchImpl;
import org.apache.myfaces.config.impl.elements.FacesFlowViewImpl;
import org.apache.myfaces.config.impl.elements.FactoryImpl;
import org.apache.myfaces.config.impl.elements.LocaleConfigImpl;
import org.apache.myfaces.config.impl.elements.NavigationCaseImpl;
import org.apache.myfaces.config.impl.elements.NavigationRuleImpl;
import org.apache.myfaces.config.impl.elements.OrderingImpl;
import org.apache.myfaces.config.impl.elements.PropertyImpl;
import org.apache.myfaces.config.impl.elements.RedirectImpl;
import org.apache.myfaces.config.impl.elements.RenderKitImpl;
import org.apache.myfaces.config.impl.elements.RendererImpl;
import org.apache.myfaces.config.impl.elements.ResourceBundleImpl;
import org.apache.myfaces.config.impl.elements.SystemEventListenerImpl;
import org.apache.myfaces.config.impl.elements.ViewParamImpl;
import org.apache.myfaces.config.impl.elements.ViewPoolMappingImpl;
import org.apache.myfaces.config.impl.elements.ViewPoolParameterImpl;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class FacesConfigUnmarshallerImpl implements FacesConfigUnmarshaller<FacesConfigImpl>
{
    private ExternalContext externalContext;
    
    public FacesConfigUnmarshallerImpl(ExternalContext externalContext)
    {
        this.externalContext = externalContext;
    }

    @Override
    public FacesConfigImpl getFacesConfig(String s) throws IOException, SAXException
    {
        return getFacesConfig(new ByteArrayInputStream(s.getBytes()), null);
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
            builder.setEntityResolver(new FacesConfigEntityResolver(externalContext));
            Document document;
            if (systemId == null)
            {
                document = builder.parse(in);
            }
            else
            {
                document = builder.parse(in, systemId);
            }
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
            onChild("protected-views", document.getDocumentElement(), (n) -> {
                onChild("url-pattern", n, (cn) -> {
                    facesConfig.addProtectedViewUrlPattern(cn.getTextContent()); 
                });  
            });
            onChild("faces-config-extension", document.getDocumentElement(),
                    (n) -> { facesConfig.addFacesConfigExtension(processFacesConfigExtension(n)); });
            onChild("navigation-rule", document.getDocumentElement(),
                    (n) -> { facesConfig.addNavigationRule(processNavigationRule(n)); });
            onChild("flow-definition", document.getDocumentElement(),
                    (n) -> { facesConfig.addFacesFlowDefinition(processFlowDefinition(n)); });
            
        }
        catch (Exception e)
        {
            throw new FacesException(e);
        }
        finally
        {
            in.close();
        }
          
        postProcessFacesConfig(systemId, facesConfig);

        return facesConfig;
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
        onChild("behavior-class", node, (n) -> { obj.setBehaviorClass(n.getTextContent()); });
                
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
    
    protected FacesConfigExtensionImpl processFacesConfigExtension(Node node)
    {
        FacesConfigExtensionImpl obj = new FacesConfigExtensionImpl();
        
        onChild("facelets-processing", node, (n) -> {
            FaceletsProcessingImpl fp = new FaceletsProcessingImpl();
            obj.addFaceletsProcessing(fp);
            onChild("file-extension", n, (cn) -> { fp.setFileExtension(cn.getTextContent()); });
            onChild("process-as", n, (cn) -> { fp.setProcessAs(cn.getTextContent()); });
            onChild("oam-compress-spaces", n, (cn) -> { fp.setOamCompressSpaces(cn.getTextContent()); });
        });

        onChild("view-pool-mapping", node, (n) -> {
            ViewPoolMappingImpl vpm = new ViewPoolMappingImpl();
            obj.addViewPoolMapping(vpm);
            onChild("url-pattern", n, (cn) -> { vpm.setUrlPattern(cn.getTextContent()); });
            onChild("parameter", n, (cn) -> {
                ViewPoolParameterImpl vpp = new ViewPoolParameterImpl();
                vpm.addParameter(vpp);
                onChild("name", cn, (ccn) -> { vpp.setName(ccn.getTextContent()); });
                onChild("value", cn, (ccn) -> { vpp.setValue(ccn.getTextContent()); });
            });
        });

        onChild("facelets-template-mapping", node, (n) -> {
            FaceletsTemplateMappingImpl ftm = new FaceletsTemplateMappingImpl();
            obj.addFaceletsTemplateMapping(ftm);
            onChild("url-pattern", n, (cn) -> { ftm.setUrlPattern(cn.getTextContent()); });
        });  
        
        return obj;
    }
    
    protected NavigationRuleImpl processNavigationRule(Node node)
    {
        NavigationRuleImpl obj = new NavigationRuleImpl();
        
        onChild("from-view-id", node, (n) -> { obj.setFromViewId(n.getTextContent()); });
                
        onChild("navigation-case", node, (n) -> {
            obj.addNavigationCase(processNavigationCase(n));
        });
        
        return obj;
    }
    
    protected FacesFlowDefinitionImpl processFlowDefinition(Node node)
    {
        FacesFlowDefinitionImpl obj = new FacesFlowDefinitionImpl();
        
        onAttribute("id", node, (v) -> { obj.setId(v); });
        onChild("start-node", node, (n) -> { obj.setStartNode(n.getTextContent()); });
        onChild("initializer", node, (n) -> { obj.setInitializer(n.getTextContent()); });
        onChild("finalizer", node, (n) -> { obj.setFinalizer(n.getTextContent()); });
        
        onChild("view", node, (n) -> {
            FacesFlowViewImpl ffv = new FacesFlowViewImpl();
            obj.addView(ffv);
            onAttribute("id", n, (v) -> { ffv.setId(v); });
            onChild("vdl-document", n, (cn) -> { ffv.setVdlDocument(cn.getTextContent()); });
        });
        
        onChild("switch", node, (n) -> {
            FacesFlowSwitchImpl ffs = new FacesFlowSwitchImpl();
            obj.addSwitch(ffs);
            onAttribute("id", n, (v) -> { ffs.setId(v); });
            onChild("case", n, (cn) -> { ffs.addNavigationCase(processNavigationCase(cn)); });
            onChild("default-outcome", n, (cn) -> {
                NavigationCaseImpl nc = new NavigationCaseImpl();
                ffs.setDefaultOutcome(nc);
                nc.setFromAction(cn.getTextContent());
            });
        });
        
        onChild("flow-return", node, (n) -> {
            FacesFlowReturnImpl ffr = new FacesFlowReturnImpl();
            obj.addReturn(ffr);
            onAttribute("id", n, (v) -> { ffr.setId(v); });
            onChild("from-outcome", n, (cn) -> {
                NavigationCaseImpl nc = new NavigationCaseImpl();
                ffr.setNavigationCase(nc);
                nc.setFromOutcome(cn.getTextContent());
            });
        });
 
        onChild("navigation-rule", node, (n) -> {
            NavigationRuleImpl nr = new NavigationRuleImpl();
            obj.addNavigationRule(nr);
            onChild("from-view-id", n, (cn) -> { nr.setFromViewId(cn.getTextContent()); });
            onChild("navigation-case", n, (cn) -> { nr.addNavigationCase(processNavigationCase(cn)); });
        });
                
        onChild("flow-call", node, (n) -> {
            FacesFlowCallImpl ffc = new FacesFlowCallImpl();
            obj.addFlowCall(ffc);
            onAttribute("id", n, (v) -> { ffc.setId(v); });
            onChild("flow-reference", n, (cn) -> {
                FacesFlowReferenceImpl ffr = new FacesFlowReferenceImpl();
                ffc.setFlowReference(ffr);
                onChild("flow-document-id", cn, (ccn) -> { ffr.setFlowDocumentId(ccn.getTextContent()); });
                onChild("flow-id", cn, (ccn) -> { ffr.setFlowId(ccn.getTextContent()); });
            });
            onChild("outbound-parameter", n, (cn) -> {
                FacesFlowParameterImpl ffp = new FacesFlowParameterImpl();
                ffc.addOutboundParameter(ffp);
                onChild("name", cn, (ccn) -> { ffp.setName(ccn.getTextContent()); });
                onChild("value", cn, (ccn) -> { ffp.setValue(ccn.getTextContent()); });
            });
        });
        
        onChild("method-call", node, (n) -> {
            FacesFlowMethodCallImpl ffmc = new FacesFlowMethodCallImpl();
            obj.addMethodCall(ffmc);
            onAttribute("id", n, (v) -> { ffmc.setId(v); });
            onChild("method", n, (cn) -> { ffmc.setMethod(cn.getTextContent()); });
            onChild("default-outcome", n, (cn) -> { ffmc.setDefaultOutcome(cn.getTextContent()); });
            onChild("parameter", n, (cn) -> {
                FacesFlowMethodParameterImpl ffmp = new FacesFlowMethodParameterImpl();
                ffmc.addParameter(ffmp);
                onChild("class", cn, (ccn) -> { ffmp.setClassName(ccn.getTextContent()); });
                onChild("value", cn, (ccn) -> { ffmp.setValue(ccn.getTextContent()); });
            });
        });
       
        onChild("inbound-parameter", node, (n) -> {
            FacesFlowParameterImpl ffp = new FacesFlowParameterImpl();
            obj.addInboundParameter(ffp);
            onChild("name", n, (cn) -> { ffp.setName(cn.getTextContent()); });
            onChild("value", n, (cn) -> { ffp.setValue(cn.getTextContent()); });
        });
       
        return obj;
    }

    private NavigationCaseImpl processNavigationCase(Node node)
    {
        NavigationCaseImpl obj = new NavigationCaseImpl();

        onChild("from-action", node, (n) -> { obj.setFromAction(n.getTextContent()); });
        onChild("from-outcome", node, (n) -> { obj.setFromOutcome(n.getTextContent()); });
        onChild("if", node, (n) -> { obj.setIf(n.getTextContent()); });
        onChild("to-view-id", node, (n) -> { obj.setToViewId(n.getTextContent()); });
       
        onChild("redirect", node, (n) -> {
            RedirectImpl r = new RedirectImpl();
            obj.setRedirect(r);
            onChild("include-view-params", n, (cn) -> { r.setIncludeViewParams("true"); });
            onChild("view-param", n, (cn) -> {
                ViewParamImpl vp = new ViewParamImpl();
                r.addViewParam(vp);
                onChild("name", cn, (ccn) -> { vp.setName(ccn.getTextContent()); });
                onChild("value", cn, (ccn) -> { vp.setValue(ccn.getTextContent()); });
            });
            onChild("redirect-param", n, (cn) -> {
                ViewParamImpl vp = new ViewParamImpl();
                r.addViewParam(vp);
                onChild("name", cn, (ccn) -> { vp.setName(ccn.getTextContent()); });
                onChild("value", cn, (ccn) -> { vp.setValue(ccn.getTextContent()); });
            });
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
