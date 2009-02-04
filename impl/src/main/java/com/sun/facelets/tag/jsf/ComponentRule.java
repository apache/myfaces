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

package com.sun.facelets.tag.jsf;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.faces.component.UIComponent;
import javax.faces.component.UIComponentBase;

import javax.faces.webapp.pdl.facelets.FaceletContext;
import com.sun.facelets.el.LegacyValueBinding;
import com.sun.facelets.tag.TagAttribute;
import com.sun.facelets.tag.Metadata;
import com.sun.facelets.tag.MetaRule;
import com.sun.facelets.tag.MetadataTarget;
import com.sun.facelets.util.FacesAPI;

/**
 * 
 * @author Jacob Hookom
 * @version $Id: ComponentRule.java,v 1.4 2008/07/13 19:01:47 rlubke Exp $
 */
final class ComponentRule extends MetaRule
{

    final class LiteralAttributeMetadata extends Metadata
    {

        private final String name;
        private final String value;

        public LiteralAttributeMetadata(String name, String value)
        {
            this.value = value;
            this.name = name;
        }

        public void applyMetadata(FaceletContext ctx, Object instance)
        {
            ((UIComponent) instance).getAttributes().put(this.name, this.value);
        }
    }

    final static class ValueExpressionMetadata extends Metadata
    {

        private final String name;

        private final TagAttribute attr;

        private final Class type;

        public ValueExpressionMetadata(String name, Class type, TagAttribute attr)
        {
            this.name = name;
            this.attr = attr;
            this.type = type;
        }

        public void applyMetadata(FaceletContext ctx, Object instance)
        {
            ((UIComponent) instance).setValueExpression(this.name, this.attr.getValueExpression(ctx, this.type));
        }

    }

    final static class ValueBindingMetadata extends Metadata
    {

        private final String name;

        private final TagAttribute attr;

        private final Class type;

        public ValueBindingMetadata(String name, Class type, TagAttribute attr)
        {
            this.name = name;
            this.attr = attr;
            this.type = type;
        }

        public void applyMetadata(FaceletContext ctx, Object instance)
        {
            ((UIComponent) instance).setValueBinding(this.name, new LegacyValueBinding(this.attr
                    .getValueExpression(ctx, this.type)));
        }

    }

    private final static Logger log = Logger.getLogger("facelets.tag.component");

    public final static ComponentRule Instance = new ComponentRule();

    public ComponentRule()
    {
        super();
    }

    public Metadata applyRule(String name, TagAttribute attribute, MetadataTarget meta)
    {
        if (meta.isTargetInstanceOf(UIComponent.class))
        {

            // if component and dynamic, then must set expression
            if (!attribute.isLiteral())
            {
                Class type = meta.getPropertyType(name);
                if (type == null)
                {
                    type = Object.class;
                }
                if (FacesAPI.getComponentVersion(meta.getTargetClass()) >= 12)
                {
                    return new ValueExpressionMetadata(name, type, attribute);
                }
                else
                {
                    return new ValueBindingMetadata(name, type, attribute);
                }
            }
            else if (meta.getWriteMethod(name) == null)
            {

                // this was an attribute literal, but not property
                warnAttr(attribute, meta.getTargetClass(), name);

                return new LiteralAttributeMetadata(name, attribute.getValue());
            }
        }
        return null;
    }

    private static void warnAttr(TagAttribute attr, Class type, String n)
    {
        if (log.isLoggable(Level.FINER))
        {
            log.finer(attr + " Property '" + n + "' is not on type: " + type.getName());
        }
    }

}
