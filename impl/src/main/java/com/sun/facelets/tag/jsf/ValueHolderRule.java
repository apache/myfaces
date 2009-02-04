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

import javax.faces.component.UIComponent;
import javax.faces.component.ValueHolder;
import javax.faces.convert.Converter;

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
 * @version $Id: ValueHolderRule.java,v 1.4 2008/07/13 19:01:46 rlubke Exp $
 */
final class ValueHolderRule extends MetaRule
{

    final static class LiteralConverterMetadata extends Metadata
    {

        private final String converterId;

        public LiteralConverterMetadata(String converterId)
        {
            this.converterId = converterId;
        }

        public void applyMetadata(FaceletContext ctx, Object instance)
        {
            ((ValueHolder) instance).setConverter(ctx.getFacesContext().getApplication()
                    .createConverter(this.converterId));
        }
    }

    final static class DynamicConverterMetadata extends Metadata
    {

        private final TagAttribute attr;

        public DynamicConverterMetadata(TagAttribute attr)
        {
            this.attr = attr;
        }

        public void applyMetadata(FaceletContext ctx, Object instance)
        {
            ((UIComponent) instance).setValueBinding("converter", new LegacyValueBinding(attr
                    .getValueExpression(ctx, Converter.class)));
        }
    }

    final static class DynamicConverterMetadata2 extends Metadata
    {

        private final TagAttribute attr;

        public DynamicConverterMetadata2(TagAttribute attr)
        {
            this.attr = attr;
        }

        public void applyMetadata(FaceletContext ctx, Object instance)
        {
            ((UIComponent) instance).setValueExpression("converter", attr.getValueExpression(ctx, Converter.class));
        }
    }

    final static class LiteralValueMetadata extends Metadata
    {

        private final String value;

        public LiteralValueMetadata(String value)
        {
            this.value = value;
        }

        public void applyMetadata(FaceletContext ctx, Object instance)
        {
            ((ValueHolder) instance).setValue(this.value);
        }
    }

    final static class DynamicValueExpressionMetadata extends Metadata
    {

        private final TagAttribute attr;

        public DynamicValueExpressionMetadata(TagAttribute attr)
        {
            this.attr = attr;
        }

        public void applyMetadata(FaceletContext ctx, Object instance)
        {
            ((UIComponent) instance).setValueExpression("value", attr.getValueExpression(ctx, Object.class));
        }
    }

    final static class DynamicValueBindingMetadata extends Metadata
    {

        private final TagAttribute attr;

        public DynamicValueBindingMetadata(TagAttribute attr)
        {
            this.attr = attr;
        }

        public void applyMetadata(FaceletContext ctx, Object instance)
        {
            ((UIComponent) instance).setValueBinding("value", new LegacyValueBinding(attr
                    .getValueExpression(ctx, Object.class)));
        }
    }

    public final static ValueHolderRule Instance = new ValueHolderRule();

    public Metadata applyRule(String name, TagAttribute attribute, MetadataTarget meta)
    {
        if (meta.isTargetInstanceOf(ValueHolder.class))
        {

            if ("converter".equals(name))
            {
                if (attribute.isLiteral())
                {
                    return new LiteralConverterMetadata(attribute.getValue());
                }
                else
                {
                    if (FacesAPI.getComponentVersion(meta.getTargetClass()) >= 12)
                    {
                        return new DynamicConverterMetadata2(attribute);
                    }
                    else
                    {
                        return new DynamicConverterMetadata(attribute);
                    }
                }
            }

            if ("value".equals(name))
            {
                if (attribute.isLiteral())
                {
                    return new LiteralValueMetadata(attribute.getValue());
                }
                else
                {
                    if (FacesAPI.getComponentVersion(meta.getTargetClass()) >= 12)
                    {
                        return new DynamicValueExpressionMetadata(attribute);
                    }
                    else
                    {
                        return new DynamicValueBindingMetadata(attribute);
                    }
                }
            }
        }
        return null;
    }

}
