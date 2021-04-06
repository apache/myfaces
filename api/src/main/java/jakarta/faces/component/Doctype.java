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
package jakarta.faces.component;

/**
 * <p class="changed_added_4_0">
 * <strong>Doctype</strong> is an interface that must be implemented by any {@link UIComponent} that represents a document type declaration.
 * </p>
 *
 * @since 4.0
 */
public interface Doctype {

    /**
     * Returns the name of the first element in the document, never <code>null</code>.
     * For example, <code>"html"</code>.
     * @return The name of the first element in the document, never <code>null</code>.
     */
    String getRootElement();

    /**
     * Returns the public identifier of the document, or <code>null</code> if there is none.
     * For example, <code>"-//W3C//DTD XHTML 1.1//EN"</code>.
     * @return The public identifier of the document, or <code>null</code> if there is none.
     */
    String getPublic();

    /**
     * Returns the system identifier of the document, or <code>null</code> if there is none.
     * For example, <code>"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"</code>.
     * @return The system identifier of the document, or <code>null</code> if there is none.
     */
    String getSystem();
}
