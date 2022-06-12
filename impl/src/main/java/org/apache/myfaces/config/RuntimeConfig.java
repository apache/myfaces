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
package org.apache.myfaces.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.el.ELResolver;
import jakarta.el.ExpressionFactory;
import jakarta.faces.component.search.SearchKeywordResolver;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;

import org.apache.myfaces.config.element.ComponentTagDeclaration;
import org.apache.myfaces.config.element.FaceletsProcessing;
import org.apache.myfaces.config.element.FaceletsTemplateMapping;
import org.apache.myfaces.config.element.NavigationRule;
import org.apache.myfaces.config.element.ResourceBundle;
import org.apache.myfaces.config.element.ViewPoolMapping;
import org.apache.myfaces.config.element.facelets.FaceletTagLibrary;
import org.apache.myfaces.core.api.shared.lang.Assert;

/**
 * Holds all configuration information (from the faces-config xml files) that is needed later during runtime. The config
 * information in this class is only available to the MyFaces core implementation classes (i.e. the myfaces source
 * tree). See MyfacesConfig for config parameters that can be used for shared or component classes.
 * 
 * @author Manfred Geiler (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
@SuppressWarnings("deprecation")
public class RuntimeConfig
{
    private static final Logger log = Logger.getLogger(RuntimeConfig.class.getName());

    private static final String APPLICATION_MAP_PARAM_NAME = RuntimeConfig.class.getName();

    private final Collection<NavigationRule> _navigationRules = new ArrayList<>();
    private boolean _navigationRulesChanged = false;
    private final Map<String, ResourceBundle> _resourceBundles = new HashMap<>();
    
    private String _facesVersion;
    
    private List<ELResolver> facesConfigElResolvers;
    private List<ELResolver> applicationElResolvers;

    private ExpressionFactory _expressionFactory;
    
    private Comparator<ELResolver> _elResolverComparator;
    
    private Predicate<ELResolver> _elResolverPredicate;

    private final Map<String, org.apache.myfaces.config.element.Converter> _converterClassNameToConfigurationMap =
        new ConcurrentHashMap<>();
    
    private NamedEventManager _namedEventManager;
    
    private final Map<String, FaceletsProcessing> _faceletsProcessingByFileExtension =
        new HashMap<>();
    
    /**
     * Faces 2.2 section 11.4.2.1. 
     * 
     * Scanning for all available contracts is necessary because the spec says 
     * "... if the information from the application configuration resources refers 
     * to a contract that is not available to the application, an informative error 
     * message must be logged. ..."
     */
    private Set<String> _externalContextResourceLibraryContracts = new HashSet<>();
    private Set<String> _classLoaderResourceLibraryContracts = new HashSet<>();
    private Set<String> _resourceLibraryContracts = new HashSet<>();
    
    private Map<String, List<String>> _contractMappings = new HashMap<>();
    
    private List<ComponentTagDeclaration> _componentTagDeclarations = new ArrayList<>();
    
    private List<String> _resourceResolvers = new ArrayList<>();
    
    private List<FaceletTagLibrary> _faceletTagLibraries = new ArrayList<>();
    
    private Map<Integer, String> _namespaceById = new HashMap<>();
    private Map<String, Integer> _idByNamespace = new HashMap<>();
    
    private List<ViewPoolMapping> _viewPoolMappings = new ArrayList<>();
    
    private List<FaceletsTemplateMapping> _faceletsTemplateMappings = new ArrayList<>();
    
    private List<SearchKeywordResolver> _searchExpressionResolvers = new ArrayList<>();
    
    private List<String> _faceletTemplates = new ArrayList<>();

    public static RuntimeConfig getCurrentInstance(FacesContext facesContext)
    {
        return getCurrentInstance(facesContext.getExternalContext());
    }
    
    public static RuntimeConfig getCurrentInstance(ExternalContext externalContext)
    {
        return (RuntimeConfig) externalContext.getApplicationMap().computeIfAbsent(
                APPLICATION_MAP_PARAM_NAME, k -> new RuntimeConfig());
    }

    public void purge()
    {
        _navigationRules.clear();
        _navigationRulesChanged = false;
        _converterClassNameToConfigurationMap.clear();
        _externalContextResourceLibraryContracts.clear();
        _classLoaderResourceLibraryContracts.clear();
        _resourceLibraryContracts.clear();
        _faceletTagLibraries.clear();
        _faceletTemplates.clear();
        
        _resourceBundles.clear();
        if (facesConfigElResolvers != null)
        {
            facesConfigElResolvers.clear();
        }
        if (applicationElResolvers != null)
        {
            applicationElResolvers.clear();
        }
        _faceletsProcessingByFileExtension.clear();
        _contractMappings.clear();
        _componentTagDeclarations.clear();
        _resourceResolvers.clear();
        _namespaceById = new HashMap<>();
        _idByNamespace = new HashMap<>();
        _viewPoolMappings.clear();
        _faceletsTemplateMappings.clear();
    }

    /**
     * Return the navigation rules that can be used by the NavigationHandler implementation.
     * 
     * @return a Collection of {@link org.apache.myfaces.config.element.NavigationRule NavigationRule}s
     */
    public Collection<NavigationRule> getNavigationRules()
    {
        return Collections.unmodifiableCollection(_navigationRules);
    }

    public void addNavigationRule(NavigationRule navigationRule)
    {
        _navigationRules.add(navigationRule);

        _navigationRulesChanged = true;
    }

    public boolean isNavigationRulesChanged()
    {
        return _navigationRulesChanged;
    }

    public void setNavigationRulesChanged(boolean navigationRulesChanged)
    {
        _navigationRulesChanged = navigationRulesChanged;
    }

    public void addComponentTagDeclaration(ComponentTagDeclaration declaration)
    {
        _componentTagDeclarations.add(declaration);
    }
    
    public List<ComponentTagDeclaration> getComponentTagDeclarations()
    {
        return Collections.unmodifiableList(_componentTagDeclarations);
    }
    
    public void addFaceletTagLibrary(FaceletTagLibrary library)
    {
        _faceletTagLibraries.add(library);
    }
    
    public List<FaceletTagLibrary> getFaceletTagLibraries()
    {
        return Collections.unmodifiableList(_faceletTagLibraries);
    }
    
    public final void addConverterConfiguration(final String converterClassName,
            final org.apache.myfaces.config.element.Converter configuration)
    {
        Assert.notEmpty(converterClassName, "converterClassName");
        Assert.notNull(configuration, "configuration");

        _converterClassNameToConfigurationMap.put(converterClassName, configuration);
    }
    
    public org.apache.myfaces.config.element.Converter getConverterConfiguration(String converterClassName)
    {
        return (org.apache.myfaces.config.element.Converter)
                _converterClassNameToConfigurationMap.get(converterClassName);
    }

    /**
     * Return the resourcebundle which was configured in faces config by var name
     * 
     * @param name
     *            the name of the resource bundle (content of var)
     * @return the resource bundle or null if not found
     */
    public ResourceBundle getResourceBundle(String name)
    {
        return _resourceBundles.get(name);
    }

    /**
     * @return the resourceBundles
     */
    public Map<String, ResourceBundle> getResourceBundles()
    {
        return _resourceBundles;
    }

    public void addResourceBundle(ResourceBundle bundle)
    {
        if (bundle == null)
        {
            throw new IllegalArgumentException("bundle must not be null");
        }
        String var = bundle.getVar();
        if (_resourceBundles.containsKey(var) && log.isLoggable(Level.WARNING))
        {
            log.warning("Another resource bundle for var '" + var + "' with base name '"
                    + _resourceBundles.get(var).getBaseName() + "' is already registered. '"
                    + _resourceBundles.get(var).getBaseName() + "' will be replaced with '" + bundle.getBaseName()
                    + "'.");
        }
        _resourceBundles.put(var, bundle);
    }

    public void addFacesConfigElResolver(ELResolver resolver)
    {
        if (facesConfigElResolvers == null)
        {
            facesConfigElResolvers = new ArrayList<>();
        }
        facesConfigElResolvers.add(resolver);
    }

    public List<ELResolver> getFacesConfigElResolvers()
    {
        return facesConfigElResolvers;
    }

    public void addApplicationElResolver(ELResolver resolver)
    {
        if (applicationElResolvers == null)
        {
            applicationElResolvers = new ArrayList<>();
        }
        applicationElResolvers.add(resolver);
    }

    public List<ELResolver> getApplicationElResolvers()
    {
        return applicationElResolvers;
    }

    public ExpressionFactory getExpressionFactory()
    {
        return _expressionFactory;
    }

    public void setExpressionFactory(ExpressionFactory expressionFactory)
    {
        _expressionFactory = expressionFactory;
    }

    public String getFacesVersion()
    {
        return _facesVersion;
    }
    
    void setFacesVersion (String facesVersion)
    {
        _facesVersion = facesVersion;
    }

    public NamedEventManager getNamedEventManager()
    {
        return _namedEventManager;
    }

    public void setNamedEventManager(NamedEventManager namedEventManager)
    {
        this._namedEventManager = namedEventManager;
    }

    public Comparator<ELResolver> getELResolverComparator()
    {
        return _elResolverComparator;
    }
    
    public void setELResolverComparator(Comparator<ELResolver> elResolverComparator)
    {
        _elResolverComparator = elResolverComparator;
    }
    
    public Predicate<ELResolver> getELResolverPredicate()
    {
        return _elResolverPredicate;
    }
    
    public void setELResolverPredicate(Predicate<ELResolver> elResolverPredicate)
    {
        _elResolverPredicate = elResolverPredicate;
    }
    
    public void addFaceletProcessingConfiguration(String fileExtension, FaceletsProcessing configuration)
    {
        Assert.notEmpty(fileExtension, "fileExtension");
        Assert.notNull(configuration, "configuration");

        this._faceletsProcessingByFileExtension.put(fileExtension, configuration);
    }
    
    public FaceletsProcessing getFaceletProcessingConfiguration(String fileExtensions)
    {
        return _faceletsProcessingByFileExtension.get(fileExtensions);
    }
    
    public Collection<FaceletsProcessing> getFaceletProcessingConfigurations()
    {
        return _faceletsProcessingByFileExtension.values();
    }

    /**
     * @return the _externalContextResourceLibraryContracts
     */
    public Set<String> getExternalContextResourceLibraryContracts()
    {
        return _externalContextResourceLibraryContracts;
    }

    /**
     * @param externalContextResourceLibraryContracts the _externalContextResourceLibraryContracts to set
     */
    public void setExternalContextResourceLibraryContracts(Set<String> externalContextResourceLibraryContracts)
    {
        this._externalContextResourceLibraryContracts = externalContextResourceLibraryContracts;
        this._resourceLibraryContracts.clear();
        this._resourceLibraryContracts.addAll(this._externalContextResourceLibraryContracts);
        this._resourceLibraryContracts.addAll(this._classLoaderResourceLibraryContracts);
    }

    /**
     * @return the _classLoaderResourceLibraryContracts
     */
    public Set<String> getClassLoaderResourceLibraryContracts()
    {
        return _classLoaderResourceLibraryContracts;
    }

    /**
     * @param classLoaderResourceLibraryContracts the _classLoaderResourceLibraryContracts to set
     */
    public void setClassLoaderResourceLibraryContracts(Set<String> classLoaderResourceLibraryContracts)
    {
        this._classLoaderResourceLibraryContracts = classLoaderResourceLibraryContracts;
        this._resourceLibraryContracts.clear();
        this._resourceLibraryContracts.addAll(this._externalContextResourceLibraryContracts);
        this._resourceLibraryContracts.addAll(this._classLoaderResourceLibraryContracts);
    }

    /**
     * @return the _resourceLibraryContracts
     */
    public Set<String> getResourceLibraryContracts()
    {
        return _resourceLibraryContracts;
    }

    /**
     * @return the _contractMappings
     */
    public Map<String, List<String>> getContractMappings()
    {
        return _contractMappings;
    }

    public void addContractMapping(String urlPattern, String[] contracts)
    {
        List<String> contractsList = _contractMappings.computeIfAbsent(urlPattern, k -> new ArrayList<>());
        Collections.addAll(contractsList, contracts);
    }
    
    public void addContractMapping(String urlPattern, String contract)
    {
        List<String> contractsList = _contractMappings.computeIfAbsent(urlPattern, k -> new ArrayList<>());
        contractsList.add(contract);
    }    
    
    public List<String> getResourceResolvers()
    {
        return _resourceResolvers;
    }
    
    public void addResourceResolver(String resourceResolver)
    {
        _resourceResolvers.add(resourceResolver);
    }

    public Map<Integer, String> getNamespaceById()
    {
        return _namespaceById;
    }

    public void setNamespaceById(Map<Integer, String> namespaceById)
    {
        this._namespaceById = namespaceById;
    }

    public Map<String, Integer> getIdByNamespace()
    {
        return _idByNamespace;
    }

    public void setIdByNamespace(Map<String, Integer> idByNamespace)
    {
        this._idByNamespace = idByNamespace;
    }

    public List<ViewPoolMapping> getViewPoolMappings()
    {
        return _viewPoolMappings;
    }
    
    public void addViewPoolMapping(ViewPoolMapping mapping)
    {
        _viewPoolMappings.add(mapping);
    }
    
    public void addApplicationSearchExpressionResolver(SearchKeywordResolver resolver)
    {
        if (_searchExpressionResolvers == null)
        {
            _searchExpressionResolvers = new ArrayList<>();
        }
        _searchExpressionResolvers.add(resolver);
    }

    public List<SearchKeywordResolver> getApplicationSearchExpressionResolvers()
    {
        return _searchExpressionResolvers;
    }
    
    public List<FaceletsTemplateMapping> getFaceletsTemplateMappings()
    {
        return _faceletsTemplateMappings;
    }
    
    public void addFaceletsTemplateMapping(FaceletsTemplateMapping mapping)
    {
        _faceletsTemplateMappings.add(mapping);
    }
}
