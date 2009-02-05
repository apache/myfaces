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
package com.sun.facelets.tag.jsf;

import javax.faces.component.ActionSource;
import javax.faces.component.ActionSource2;
import javax.faces.event.ActionEvent;
import javax.faces.event.MethodExpressionActionListener;

import javax.faces.webapp.pdl.facelets.FaceletContext;
import com.sun.facelets.el.LegacyMethodBinding;
import com.sun.facelets.tag.TagAttribute;
import com.sun.facelets.tag.Metadata;
import com.sun.facelets.tag.MetaRule;
import com.sun.facelets.tag.MetadataTarget;
import com.sun.facelets.util.FacesAPI;

/**
 * 
 * @author Jacob Hookom
 * @version $Id: ActionSourceRule.java,v 1.5 2008/07/13 19:01:46 rlubke Exp $
 */
final class ActionSourceRule extends MetaRule
{

    public final static Class[] ACTION_SIG = new Class[0];

    public final static Class[] ACTION_LISTENER_SIG = new Class[] { ActionEvent.class };

    final static class ActionMapper extends Metadata
    {

        private final TagAttribute attr;

        public ActionMapper(TagAttribute attr)
        {
            this.attr = attr;
        }

        public void applyMetadata(FaceletContext ctx, Object instance)
        {
            ((ActionSource) instance).setAction(new LegacyMethodBinding(this.attr
                    .getMethodExpression(ctx, String.class, ActionSourceRule.ACTION_SIG)));
        }
    }

    final static class ActionMapper2 extends Metadata
    {

        private final TagAttribute attr;

        public ActionMapper2(TagAttribute attr)
        {
            this.attr = attr;
        }

        public void applyMetadata(FaceletContext ctx, Object instance)
        {
            ((ActionSource2) instance).setActionExpression(this.attr.getMethodExpression(ctx, String.class,
                                                                                         ActionSourceRule.ACTION_SIG));
        }

    }

    final static class ActionListenerMapper extends Metadata
    {

        private final TagAttribute attr;

        public ActionListenerMapper(TagAttribute attr)
        {
            this.attr = attr;
        }

        public void applyMetadata(FaceletContext ctx, Object instance)
        {
            ((ActionSource) instance).setActionListener(new LegacyMethodBinding(this.attr
                    .getMethodExpression(ctx, null, ActionSourceRule.ACTION_LISTENER_SIG)));
        }

    }

    final static class ActionListenerMapper2 extends Metadata
    {

        private final TagAttribute attr;

        public ActionListenerMapper2(TagAttribute attr)
        {
            this.attr = attr;
        }

        public void applyMetadata(FaceletContext ctx, Object instance)
        {
            ((ActionSource2) instance).addActionListener(new MethodExpressionActionListener(this.attr
                    .getMethodExpression(ctx, null, ActionSourceRule.ACTION_LISTENER_SIG)));

        }

    }

    public final static ActionSourceRule Instance = new ActionSourceRule();

    public ActionSourceRule()
    {
        super();
    }

    public Metadata applyRule(String name, TagAttribute attribute, MetadataTarget meta)
    {
        if (meta.isTargetInstanceOf(ActionSource.class))
        {

            boolean elSupport = FacesAPI.getComponentVersion(meta.getTargetClass()) >= 12;

            if ("action".equals(name))
            {
                if (elSupport && meta.isTargetInstanceOf(ActionSource2.class))
                {
                    return new ActionMapper2(attribute);
                }
                else
                {
                    return new ActionMapper(attribute);
                }
            }

            if ("actionListener".equals(name))
            {
                if (elSupport && meta.isTargetInstanceOf(ActionSource2.class))
                {
                    return new ActionListenerMapper2(attribute);
                }
                else
                {
                    return new ActionListenerMapper(attribute);
                }
            }
        }
        return null;
    }
}
