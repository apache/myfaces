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
package org.apache.myfaces.application;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import java.util.regex.Pattern;
import javax.faces.FacesException;
import javax.faces.application.ConfigurableNavigationHandler;
import javax.faces.application.FacesMessage;
import javax.faces.application.NavigationCase;
import javax.faces.application.ProjectStage;
import javax.faces.application.ViewHandler;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.component.visit.VisitCallback;
import javax.faces.component.visit.VisitContext;
import javax.faces.component.visit.VisitHint;
import javax.faces.component.visit.VisitResult;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.PartialViewContext;
import javax.faces.view.ViewDeclarationLanguage;
import javax.faces.view.ViewMetadata;

import org.apache.myfaces.config.RuntimeConfig;
import org.apache.myfaces.config.element.NavigationRule;
import org.apache.myfaces.shared.application.NavigationUtils;
import org.apache.myfaces.shared.renderkit.html.util.SharedStringBuilder;
import org.apache.myfaces.shared.util.HashMapUtils;
import org.apache.myfaces.shared.util.StringUtils;
import org.apache.myfaces.view.facelets.tag.jsf.PreDisposeViewEvent;

/**
 * @author Thomas Spiegl (latest modification by $Author$)
 * @author Anton Koinov
 * @version $Revision$ $Date$
 */
public class NavigationHandlerImpl
    extends ConfigurableNavigationHandler
{
    //private static final Log log = LogFactory.getLog(NavigationHandlerImpl.class);
    private static final Logger log = Logger.getLogger(NavigationHandlerImpl.class.getName());

    private static final String SKIP_ITERATION_HINT = "javax.faces.visit.SKIP_ITERATION";
    
    private static final Set<VisitHint> VISIT_HINTS = Collections.unmodifiableSet(
            EnumSet.of(VisitHint.SKIP_ITERATION));    
    
    private static final String OUTCOME_NAVIGATION_SB = "oam.navigation.OUTCOME_NAVIGATION_SB";
    
    private static final Pattern AMP_PATTERN = Pattern.compile("&(amp;)?"); // "&" or "&amp;"
    
    private static final String ASTERISK = "*";

    private Map<String, Set<NavigationCase>> _navigationCases = null;
    private List<String> _wildcardKeys = new ArrayList<String>();
    private Boolean _developmentStage;
    
    private NavigationHandlerSupport navigationHandlerSupport;

    public NavigationHandlerImpl()
    {
        if (log.isLoggable(Level.FINEST))
        {
            log.finest("New NavigationHandler instance created");
        }
    }

    @Override
    public void handleNavigation(FacesContext facesContext, String fromAction, String outcome)
    {
        NavigationCase navigationCase = getNavigationCase(facesContext, fromAction, outcome);

        if (navigationCase != null)
        {
            if (log.isLoggable(Level.FINEST))
            {
                log.finest("handleNavigation fromAction=" + fromAction + " outcome=" + outcome +
                          " toViewId =" + navigationCase.getToViewId(facesContext) +
                          " redirect=" + navigationCase.isRedirect());
            }
            if (navigationCase.isRedirect())
            { 
                //&& (!PortletUtil.isPortletRequest(facesContext)))
                // Spec section 7.4.2 says "redirects not possible" in this case for portlets
                //But since the introduction of portlet bridge and the 
                //removal of portlet code in myfaces core 2.0, this condition
                //no longer applies
                
                ExternalContext externalContext = facesContext.getExternalContext();
                ViewHandler viewHandler = facesContext.getApplication().getViewHandler();
                String toViewId = navigationCase.getToViewId(facesContext);
                

                String redirectPath = viewHandler.getRedirectURL(
                        facesContext, toViewId, 
                        NavigationUtils.getEvaluatedNavigationParameters(facesContext,
                        navigationCase.getParameters()) ,
                        navigationCase.isIncludeViewParams());
                
                //Clear ViewMap if we are redirecting to other resource
                UIViewRoot viewRoot = facesContext.getViewRoot(); 
                if (viewRoot != null && !toViewId.equals(viewRoot.getViewId()))
                {
                    //call getViewMap(false) to prevent unnecessary map creation
                    Map<String, Object> viewMap = viewRoot.getViewMap(false);
                    if (viewMap != null)
                    {
                        viewMap.clear();
                    }
                }
                
                // JSF 2.0 the javadoc of handleNavigation() says something like this 
                // "...If the view has changed after an application action, call
                // PartialViewContext.setRenderAll(true)...". The effect is that ajax requests
                // are included on navigation.
                PartialViewContext partialViewContext = facesContext.getPartialViewContext();
                String viewId = facesContext.getViewRoot() != null ? facesContext.getViewRoot().getViewId() : null;
                if ( partialViewContext.isPartialRequest() && 
                     !partialViewContext.isRenderAll() && 
                     toViewId != null &&
                     !toViewId.equals(viewId))
                {
                    partialViewContext.setRenderAll(true);
                }
                
                // JSF 2.0 Spec call Flash.setRedirect(true) to notify Flash scope and take proper actions
                externalContext.getFlash().setRedirect(true);
                try
                {
                    externalContext.redirect(redirectPath);
                    facesContext.responseComplete();
                }
                catch (IOException e)
                {
                    throw new FacesException(e.getMessage(), e);
                }
            }
            else
            {
                ViewHandler viewHandler = facesContext.getApplication().getViewHandler();
                //create new view
                String newViewId = navigationCase.getToViewId(facesContext);
                // JSF 2.0 the javadoc of handleNavigation() says something like this 
                // "...If the view has changed after an application action, call
                // PartialViewContext.setRenderAll(true)...". The effect is that ajax requests
                // are included on navigation.
                PartialViewContext partialViewContext = facesContext.getPartialViewContext();
                String viewId = facesContext.getViewRoot() != null ? facesContext.getViewRoot().getViewId() : null;
                if ( partialViewContext.isPartialRequest() && 
                     !partialViewContext.isRenderAll() && 
                     newViewId != null &&
                     !newViewId.equals(viewId))
                {
                    partialViewContext.setRenderAll(true);
                }

                if (facesContext.getViewRoot() != null &&
                    facesContext.getViewRoot().getAttributes().containsKey("oam.CALL_PRE_DISPOSE_VIEW"))
                {
                    try
                    {
                        facesContext.getAttributes().put(SKIP_ITERATION_HINT, Boolean.TRUE);

                        VisitContext visitContext = VisitContext.createVisitContext(facesContext, null, VISIT_HINTS);
                        facesContext.getViewRoot().visitTree(visitContext,
                                                             new PreDisposeViewCallback());
                    }
                    finally
                    {
                        facesContext.getAttributes().remove(SKIP_ITERATION_HINT);
                    }
                }

                // create UIViewRoot for new view
                UIViewRoot viewRoot = null;
                
                String derivedViewId = viewHandler.deriveViewId(facesContext, newViewId);

                if (derivedViewId != null)
                {
                    ViewDeclarationLanguage vdl = viewHandler.getViewDeclarationLanguage(facesContext, derivedViewId);
                    
                    if (vdl != null)
                    {
                        ViewMetadata metadata = vdl.getViewMetadata(facesContext, newViewId);
                        
                        if (metadata != null)
                        {
                            viewRoot = metadata.createMetadataView(facesContext);
                        }
                    }
                }
                
                // viewRoot can be null here, if ...
                //   - we don't have a ViewDeclarationLanguage (e.g. when using facelets-1.x)
                //   - there is no view metadata or metadata.createMetadataView() returned null
                //   - viewHandler.deriveViewId() returned null
                if (viewRoot == null)
                {
                    viewRoot = viewHandler.createView(facesContext, newViewId);
                }
                
                facesContext.setViewRoot(viewRoot);
                facesContext.renderResponse();
            }
        }
        else
        {
            // no navigationcase found, stay on current ViewRoot
            if (log.isLoggable(Level.FINEST))
            {
                log.finest("handleNavigation fromAction=" + fromAction + " outcome=" + outcome +
                          " no matching navigation-case found, staying on current ViewRoot");
            }
        }
    }

    /**
    * @return the navigationHandlerSupport
    */
    protected NavigationHandlerSupport getNavigationHandlerSupport()
    {
        if (navigationHandlerSupport == null)
        {
            navigationHandlerSupport = new DefaultNavigationHandlerSupport();
        }
        return navigationHandlerSupport;
    }

    public void setNavigationHandlerSupport(NavigationHandlerSupport navigationHandlerSupport)
    {
        this.navigationHandlerSupport = navigationHandlerSupport;
    }

    private static class PreDisposeViewCallback implements VisitCallback
    {

        public VisitResult visit(VisitContext context, UIComponent target)
        {
            context.getFacesContext().getApplication().publishEvent(context.getFacesContext(),
                                                                    PreDisposeViewEvent.class, target);
            
            return VisitResult.ACCEPT;
        }
    }

    /**
     * Returns the navigation case that applies for the given action and outcome
     */
    public NavigationCase getNavigationCase(FacesContext facesContext, String fromAction, String outcome)
    {
        String viewId = facesContext.getViewRoot() != null ? facesContext.getViewRoot().getViewId() : null;
        
        Map<String, Set<NavigationCase>> casesMap = getNavigationCases();
        NavigationCase navigationCase = null;
        
        Set<? extends NavigationCase> casesSet;
        if (viewId != null)
        {
            casesSet = casesMap.get(viewId);
            if (casesSet != null)
            {
                // Exact match?
                navigationCase = calcMatchingNavigationCase(facesContext, casesSet, fromAction, outcome);
            }
        }

        if (navigationCase == null)
        {
            // Wildcard match?
            List<String> sortedWildcardKeys = getSortedWildcardKeys();
            for (int i = 0; i < sortedWildcardKeys.size(); i++)
            {
                String fromViewId = sortedWildcardKeys.get(i);
                if (fromViewId.length() > 2)
                {
                    String prefix = fromViewId.substring(0, fromViewId.length() - 1);
                    if (viewId != null && viewId.startsWith(prefix))
                    {
                        casesSet = casesMap.get(fromViewId);
                        if (casesSet != null)
                        {
                            navigationCase = calcMatchingNavigationCase(facesContext, casesSet, fromAction, outcome);
                            if (navigationCase != null)
                            {
                                break;
                            }
                        }
                    }
                }
                else
                {
                    casesSet = casesMap.get(fromViewId);
                    if (casesSet != null)
                    {
                        navigationCase = calcMatchingNavigationCase(facesContext, casesSet, fromAction, outcome);
                        if (navigationCase != null)
                        {
                            break;
                        }
                    }
                }
            }
        }
        
        if (outcome != null && navigationCase == null)
        {
            //if outcome is null, we don't check outcome based nav cases
            //otherwise, if navgiationCase is still null, check outcome-based nav cases
            navigationCase = getOutcomeNavigationCase (facesContext, fromAction, outcome);
        }
        
        if (outcome != null && navigationCase == null && !facesContext.isProjectStage(ProjectStage.Production))
        {
            final FacesMessage facesMessage = new FacesMessage("No navigation case match for viewId " + viewId + 
                    ",  action " + fromAction + " and outcome " + outcome);
            facesMessage.setSeverity(FacesMessage.SEVERITY_WARN);
            facesContext.addMessage(null, facesMessage);
        }

        return navigationCase;  //if navigationCase == null, will stay on current view

    }
    
    /**
     * Performs the algorithm specified in 7.4.2 for situations where no navigation cases are defined and instead
     * the navigation case is to be determined from the outcome.
     * 
     * TODO: cache results?
     */
    private NavigationCase getOutcomeNavigationCase (FacesContext facesContext, String fromAction, String outcome)
    {
        String implicitViewId = null;
        boolean includeViewParams = false;
        int index;
        boolean isRedirect = false;
        String queryString = null;
        NavigationCase result = null;
        String viewId = facesContext.getViewRoot() != null ? facesContext.getViewRoot().getViewId() : null;
        //String viewIdToTest = outcome;
        StringBuilder viewIdToTest = SharedStringBuilder.get(facesContext, OUTCOME_NAVIGATION_SB);
        viewIdToTest.append(outcome);
        
        // If viewIdToTest contains a query string, remove it and set queryString with that value.
        index = viewIdToTest.indexOf ("?");
        if (index != -1)
        {
            queryString = viewIdToTest.substring (index + 1);
            //viewIdToTest = viewIdToTest.substring (0, index);
            viewIdToTest.setLength(index);
            
            // If queryString contains "faces-redirect=true", set isRedirect to true.
            if (queryString.indexOf ("faces-redirect=true") != -1)
            {
                isRedirect = true;
            }
            
            // If queryString contains "includeViewParams=true" or 
            // "faces-include-view-params=true", set includeViewParams to true.
            if (queryString.indexOf("includeViewParams=true") != -1 
                    || queryString.indexOf("faces-include-view-params=true") != -1)
            {
                includeViewParams = true;
            }
        }
        
        // If viewIdToTest does not have a "file extension", use the one from the current viewId.
        index = viewIdToTest.indexOf (".");
        if (index == -1)
        {
            if (viewId != null)
            {
                index = viewId.lastIndexOf(".");

                if (index != -1)
                {
                    //viewIdToTest += viewId.substring (index);
                    viewIdToTest.append(viewId.substring (index));
                }
            }
            else
            {
                // This case happens when for for example there is a ViewExpiredException,
                // and a custom ExceptionHandler try to navigate using implicit navigation.
                // In this case, there is no UIViewRoot set on the FacesContext, so viewId 
                // is null.

                // In this case, it should try to derive the viewId of the view that was
                // not able to restore, to get the extension and apply it to
                // the implicit navigation.
                String tempViewId = getNavigationHandlerSupport().calculateViewId(facesContext);
                if (tempViewId != null)
                {
                    index = tempViewId.lastIndexOf(".");
                    if(index != -1)
                    {
                        viewIdToTest.append(tempViewId.substring (index));
                    }
                }
            }
            if (log.isLoggable(Level.FINEST))
            {
                log.finest("getOutcomeNavigationCase -> viewIdToTest: " + viewIdToTest);
            } 
        }

        // If viewIdToTest does not start with "/", look for the last "/" in viewId.  If not found, simply prepend "/".
        // Otherwise, prepend everything before and including the last "/" in viewId.
        
        //if (!viewIdToTest.startsWith ("/") && viewId != null)
        boolean startWithSlash = false;
        if (viewIdToTest.length() > 0)
        {
            startWithSlash = (viewIdToTest.charAt(0) == '/');
        } 
        if (!startWithSlash) 
        {
            index = -1;
            if( viewId != null )
            {
               index = viewId.lastIndexOf ("/");
            }
            
            if (index == -1)
            {
                //viewIdToTest = "/" + viewIdToTest;
                viewIdToTest.insert(0,"/");
            }
            
            else
            {
                //viewIdToTest = viewId.substring (0, index + 1) + viewIdToTest;
                viewIdToTest.insert(0, viewId, 0, index + 1);
            }
        }
        
        // Call ViewHandler.deriveViewId() and set the result as implicitViewId.
        
        try
        {
            implicitViewId = facesContext.getApplication().getViewHandler().deriveViewId (
                    facesContext, viewIdToTest.toString());
        }
        
        catch (UnsupportedOperationException e)
        {
            // This is the case when a pre-JSF 2.0 ViewHandler is used.
            // In this case, the default algorithm must be used.
            // FIXME: I think we're always calling the "default" ViewHandler.deriveViewId() algorithm and we don't
            // distinguish between pre-JSF 2.0 and JSF 2.0 ViewHandlers.  This probably needs to be addressed.
        }
        
        if (implicitViewId != null)
        {
            // Append all params from the queryString
            // (excluding faces-redirect, includeViewParams and faces-include-view-params)
            Map<String, List<String>> params = null;
            if (queryString != null && !"".equals(queryString))
            {
                //String[] splitQueryParams = queryString.split("&(amp;)?"); // "&" or "&amp;"
                String[] splitQueryParams = AMP_PATTERN.split(queryString); // "&" or "&amp;"
                params = new HashMap<String, List<String>>(splitQueryParams.length, 
                        (splitQueryParams.length* 4 + 3) / 3);
                for (String queryParam : splitQueryParams)
                {
                    String[] splitParam = StringUtils.splitShortString(queryParam, '=');
                    if (splitParam.length == 2)
                    {
                        // valid parameter - add it to params
                        if ("includeViewParams".equals(splitParam[0])
                                || "faces-include-view-params".equals(splitParam[0])
                                || "faces-redirect".equals(splitParam[0]))
                        {
                            // ignore includeViewParams, faces-include-view-params and faces-redirect
                            continue;
                        }
                        List<String> paramValues = params.get(splitParam[0]);
                        if (paramValues == null)
                        {
                            // no value for the given parameter yet
                            paramValues = new ArrayList<String>();
                            params.put(splitParam[0], paramValues);
                        }
                        paramValues.add(splitParam[1]);
                    }
                    else
                    {
                        // invalid parameter
                        throw new FacesException("Invalid parameter \"" + 
                                queryParam + "\" in outcome " + outcome);
                    }
                }
            }
            
            // Finally, create the NavigationCase.
            result = new NavigationCase (viewId, fromAction, outcome, null, 
                    implicitViewId, params, isRedirect, includeViewParams);
        }
        
        return result;
    }
    
    /**
     * Returns the view ID that would be created for the given action and outcome
     */
    public String getViewId(FacesContext context, String fromAction, String outcome)
    {
        return this.getNavigationCase(context, fromAction, outcome).getToViewId(context);
    }

    /**
     * TODO
     * Invoked by the navigation handler before the new view component is created.
     * @param viewId The view ID to be created
     * @return The view ID that should be used instead. If null, the view ID passed
     * in will be used without modification.
     */
    public String beforeNavigation(String viewId)
    {
        return null;
    }

    private NavigationCase calcMatchingNavigationCase(FacesContext context,
                                                      Set<? extends NavigationCase> casesList,
                                                      String actionRef,
                                                      String outcome)
    {
        NavigationCase noConditionCase = null;
        NavigationCase firstCase = null;
        NavigationCase firstCaseIf = null;
        NavigationCase secondCase = null;
        NavigationCase secondCaseIf = null;
        NavigationCase thirdCase = null;
        NavigationCase thirdCaseIf = null;
        NavigationCase fourthCase = null;
        NavigationCase fourthCaseIf = null;
                        
        for (NavigationCase caze : casesList)
        {
            String cazeOutcome = caze.getFromOutcome();
            String cazeActionRef = caze.getFromAction();
            Boolean cazeIf = caze.getCondition(context);
            boolean ifMatches = (cazeIf == null ? false : cazeIf.booleanValue());
            // JSF 2.0: support conditional navigation via <if>.
            // Use for later cases.
            
            if(outcome == null && (cazeOutcome != null || cazeIf == null) && actionRef == null)
            {
                //To match an outcome value of null, the <from-outcome> must be absent and the <if> element present.
                continue;
            }
            
            //If there are no conditions on navigation case save it and return as last resort
            if (cazeOutcome == null && cazeActionRef == null &&
                cazeIf == null && noConditionCase == null && outcome != null)
            {
                noConditionCase = caze;
            }
            
            if (cazeActionRef != null)
            {
                if (cazeOutcome != null)
                {
                    if ((actionRef != null) && (outcome != null) && cazeActionRef.equals (actionRef) &&
                            cazeOutcome.equals (outcome))
                    {
                        // First case: match if <from-action> matches action and <from-outcome> matches outcome.
                        // Caveat: evaluate <if> if available.

                        if (cazeIf != null)
                        {
                            if (ifMatches)
                            {
                                firstCaseIf = caze;
                                //return caze;
                            }

                            continue;
                        }
                        else
                        {
                            firstCase = caze;
                            //return caze;
                        }
                    }
                }
                else
                {
                    if ((actionRef != null) && cazeActionRef.equals (actionRef))
                    {
                        // Third case: if only <from-action> specified, match against action.
                        // Caveat: if <if> is available, evaluate.  If not, only match if outcome is not null.

                        if (cazeIf != null)
                        {
                            if (ifMatches)
                            {
                                thirdCaseIf = caze;
                                //return caze;
                            }
                            
                            continue;
                        }
                        else
                        {
                            if (outcome != null)
                            {
                                thirdCase = caze;
                                //return caze;
                            }
                            
                            continue;
                        }
                    }
                    else
                    {
                        // cazeActionRef != null and cazeOutcome == null
                        // but cazeActionRef does not match. No additional operation
                        // required because cazeIf is only taken into account 
                        // it cazeActionRef match. 
                        continue;
                    }
                }
            }
            else
            {
                if (cazeOutcome != null)
                {
                    if ((outcome != null) && cazeOutcome.equals (outcome))
                    {
                        // Second case: if only <from-outcome> specified, match against outcome.
                        // Caveat: if <if> is available, evaluate.

                        if (cazeIf != null)
                        {
                            if (ifMatches)
                            {
                                secondCaseIf = caze;
                                //return caze;
                            }
                            
                            continue;
                        }
                        else
                        {
                            secondCase = caze;
                            //return caze;
                        }
                    }
                }
            }

            // Fourth case: anything else matches if outcome is not null or <if> is specified.

            if (outcome != null)
            {
                // Again, if <if> present, evaluate.
                if (cazeIf != null)
                {
                    if (ifMatches)
                    {
                        fourthCaseIf = caze;
                        //return caze;
                    }
                    
                    continue;
                }
            }

            if ((cazeIf != null) && ifMatches)
            {
                fourthCase = caze;
                //return caze;
            }
        }
        
        if (firstCaseIf != null)
        {
            return firstCaseIf;
        }
        else if (firstCase != null)
        {
            return firstCase;
        }
        else if (secondCaseIf != null)
        {
            return secondCaseIf;
        }
        else if (secondCase != null)
        {
            return secondCase;
        }
        else if (thirdCaseIf != null)
        {
            return thirdCaseIf;
        }
        else if (thirdCase != null)
        {
            return thirdCase;
        }
        else if (fourthCaseIf != null)
        {
            return fourthCaseIf;
        }
        else if (fourthCase != null)
        {
            return fourthCase;
        }
        
        return noConditionCase;
    }

    private List<String> getSortedWildcardKeys()
    {
        return _wildcardKeys;
    }

    @Override
    public Map<String, Set<NavigationCase>> getNavigationCases()
    {
        if (_developmentStage == null)
        {
            _developmentStage = FacesContext.getCurrentInstance().isProjectStage(ProjectStage.Development);
        }
        if (!Boolean.TRUE.equals(_developmentStage))
        {
            if (_navigationCases == null)
            {
                FacesContext facesContext = FacesContext.getCurrentInstance();
                ExternalContext externalContext = facesContext.getExternalContext();
                RuntimeConfig runtimeConfig = RuntimeConfig.getCurrentInstance(externalContext);
                
                calculateNavigationCases(facesContext, runtimeConfig);
            }
            return _navigationCases;
        }
        else
        {
            FacesContext facesContext = FacesContext.getCurrentInstance();
            ExternalContext externalContext = facesContext.getExternalContext();
            RuntimeConfig runtimeConfig = RuntimeConfig.getCurrentInstance(externalContext);

            if (_navigationCases == null || runtimeConfig.isNavigationRulesChanged())
            {
                calculateNavigationCases(facesContext, runtimeConfig);
            }
            return _navigationCases;
        }
    }
    
    private synchronized void calculateNavigationCases(FacesContext facesContext, RuntimeConfig runtimeConfig)
    {
        if (_navigationCases == null || runtimeConfig.isNavigationRulesChanged())
        {
            Collection<? extends NavigationRule> rules = runtimeConfig.getNavigationRules();
            int rulesSize = rules.size();

            Map<String, Set<NavigationCase>> cases = new HashMap<String, Set<NavigationCase>>(
                    HashMapUtils.calcCapacity(rulesSize));

            List<String> wildcardKeys = new ArrayList<String>();

            for (NavigationRule rule : rules)
            {
                String fromViewId = rule.getFromViewId();

                //specification 7.4.2 footnote 4 - missing fromViewId is allowed:
                if (fromViewId == null)
                {
                    fromViewId = ASTERISK;
                }
                else
                {
                    fromViewId = fromViewId.trim();
                }

                Set<NavigationCase> set = cases.get(fromViewId);
                if (set == null)
                {
                    set = new HashSet<NavigationCase>(convertNavigationCasesToAPI(rule));
                    cases.put(fromViewId, set);
                    if (fromViewId.endsWith(ASTERISK))
                    {
                        wildcardKeys.add(fromViewId);
                    }
                }
                else
                {
                    set.addAll(convertNavigationCasesToAPI(rule));
                }
            }

            Collections.sort(wildcardKeys, new KeyComparator());

            synchronized (cases)
            {
                // We do not really need this sychronization at all, but this
                // gives us the peace of mind that some good optimizing compiler
                // will not rearrange the execution of the assignment to an
                // earlier time, before all init code completes
                _navigationCases = cases;
                _wildcardKeys = wildcardKeys;

                runtimeConfig.setNavigationRulesChanged(false);
            }
        }
    }

    private static final class KeyComparator implements Comparator<String>
    {
        public int compare(String s1, String s2)
        {
            return -s1.compareTo(s2);
        }
    }
    
    private Set<NavigationCase> convertNavigationCasesToAPI(NavigationRule rule)
    {
        Collection<? extends org.apache.myfaces.config.element.NavigationCase> configCases = rule.getNavigationCases();
        Set<NavigationCase> apiCases = new HashSet<NavigationCase>(configCases.size());
        
        for(org.apache.myfaces.config.element.NavigationCase configCase : configCases)
        {   
            if(configCase.getRedirect() != null)
            {
                String includeViewParamsAttribute = configCase.getRedirect().getIncludeViewParams();
                boolean includeViewParams = false; // default value is false
                if (includeViewParamsAttribute != null)
                {
                    includeViewParams = new Boolean(includeViewParamsAttribute);
                }
                apiCases.add(new NavigationCase(rule.getFromViewId(),configCase.getFromAction(),
                                                configCase.getFromOutcome(),configCase.getIf(),configCase.getToViewId(),
                                                configCase.getRedirect().getViewParams(),true,includeViewParams));
            }
            else
            {
                apiCases.add(new NavigationCase(rule.getFromViewId(),configCase.getFromAction(),
                                                configCase.getFromOutcome(),configCase.getIf(),
                                                configCase.getToViewId(),null,false,false));
            }
        }
        
        return apiCases;
    }

}
