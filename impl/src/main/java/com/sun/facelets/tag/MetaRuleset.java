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

/**
 * A mutable set of rules to be used in auto-wiring state to a particular object instance. Rules assigned to this object
 * will be composed into a single Metadata instance.
 * 
 * @author Jacob Hookom
 * @version $Id: MetaRuleset.java,v 1.3 2008/07/13 19:01:35 rlubke Exp $
 */
public abstract class MetaRuleset
{
    /**
     * @param attribute
     * @return
     */
    public abstract MetaRuleset ignore(String attribute);

    /**
     * @return
     */
    public abstract MetaRuleset ignoreAll();

    /**
     * @param attribute
     * @param property
     * @return
     */
    public abstract MetaRuleset alias(String attribute, String property);

    /**
     * @param mapper
     * @return
     */
    public abstract MetaRuleset add(Metadata mapper);

    /**
     * @param rule
     * @return
     */
    public abstract MetaRuleset addRule(MetaRule rule);

    /**
     * @return
     */
    public abstract Metadata finish();
}
