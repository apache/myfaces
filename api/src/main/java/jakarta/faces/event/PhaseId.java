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
package jakarta.faces.event;

import jakarta.faces.FacesException;
import org.apache.myfaces.core.api.shared.lang.Assert;

import java.util.List;

/**
 * see Javadoc of <a href="http://java.sun.com/javaee/javaserverfaces/1.2/docs/api/index.html">Faces Specification</a>
 */
public enum PhaseId
{
    ANY_PHASE,
    APPLY_REQUEST_VALUES,
    INVOKE_APPLICATION,
    PROCESS_VALIDATIONS,
    RENDER_RESPONSE,
    RESTORE_VIEW,
    UPDATE_MODEL_VALUES;

    public static final List<PhaseId> VALUES;

    static
    {
        VALUES = List.of(PhaseId.values());
    }

    public int getOrdinal()
    {
        return ordinal();
    }

    /*
     * @since 2.2
     */
    public String getName()
    {
        return name();
    }

    @Override
    public String toString()
    {
        return getName() + '(' + getOrdinal() + ')';
    }

    public static PhaseId phaseIdValueOf(String phase)
    {
        Assert.notNull(phase, "phase");
        for (int i = 0; i < VALUES.size(); i++)
        {
            PhaseId phaseId = VALUES.get(i);
            if (phaseId.getName().equals(phase))
            {
                return phaseId;
            }
        }
        throw new FacesException("Phase " + phase + " is invalid");
    }
}
