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
 * <p>
 * Package-private utility class for determining which specifications are available
 * in the current process. See JIRA issue: http://issues.apache.org/jira/browse/MYFACES-2386
 * This is a stripped down version of: org.apache.myfaces.util.ExternalSpecifications
 * </p>
 * @since 2.0
 */
final class _ExternalSpecifications
{
    private static volatile Boolean beanValidationAvailable;

    /**
     * This method determines if Bean Validation is present.
     *
     * Eager initialization is used for performance. This means Bean Validation binaries
     * should not be added at runtime after this variable has been set.
     * @return true if Bean Validation is available, false otherwise.
     */
    public static boolean isBeanValidationAvailable()
    {
        if (beanValidationAvailable == null)
        {
            try
            {
                try
                {
                    beanValidationAvailable = (Class.forName("javax.validation.Validation") != null);
                }
                catch (ClassNotFoundException e)
                {
                    beanValidationAvailable = Boolean.FALSE;
                }
                
                if (beanValidationAvailable)
                {
                    try
                    {
                        // Trial-error approach to check for Bean Validation impl existence.
                        // If any Exception occurs here, we assume that Bean Validation is not available.
                        // The cause may be anything, i.e. NoClassDef, config error...
                        _ValidationUtils.tryBuildDefaultValidatorFactory();
                    }
                    catch (Throwable t)
                    {
                        beanValidationAvailable = false;
                    }
                }
            }
            catch (Throwable t)
            {
                beanValidationAvailable = false;
            }
        }
        return beanValidationAvailable;
    }

    /**
     * this class should not be instantiated.
     */
    private _ExternalSpecifications()
    {
    }
}