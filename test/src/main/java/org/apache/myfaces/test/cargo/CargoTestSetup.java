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

package org.apache.myfaces.test.cargo;

import java.io.File;

import junit.extensions.TestSetup;
import junit.framework.Test;

import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.ContainerType;
import org.codehaus.cargo.container.tomcat.Tomcat5xInstalledLocalContainer;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.deployable.DeployableType;
import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.configuration.ConfigurationType;
import org.codehaus.cargo.generic.deployable.DefaultDeployableFactory;
import org.codehaus.cargo.generic.configuration.ConfigurationFactory;
import org.codehaus.cargo.generic.configuration.DefaultConfigurationFactory;
import org.codehaus.cargo.generic.DefaultContainerFactory;
import org.codehaus.cargo.util.log.FileLogger;

/**
 * <p>Convenience <code>TestSetup</code> class which uses Cargo to start
 * and stop a Servlet container.</p>
 * 
 * @since 1.0.0
 */
public class CargoTestSetup extends TestSetup
{

    // ------------------------------------------------------------ Constructors

    /**
     * <p>Construct a new instance of this test setup.</p>
     *
     * @param test Tests to be run within this test setup.
     */
    public CargoTestSetup(Test test)
    {
        super(test);
    }

    // ------------------------------------------------------ Instance Variables

    /**
     * <p>The installed local container for this test setup.</p>
     */
    private InstalledLocalContainer container;

    // ------------------------------------------------------ Test Setup Methods

    /**
     * <p>Start the container prior to running the tests.</p>
     * <p>The following System properties are used:
     * <ul>
     * <li>cargo.container.id - ID of the container to use. [tomcat5x]</li>
     * <li>cargo.container.home - Full path to a local installation of the container.
     * If not set, uses the value of the TOMCAT_HOME environment variable.
     * One of cargo.container.home or TOMCAT_HOME is REQUIRED.</li>
     * <li>cargo.deployable - Full path to the war file to deploy. REQUIRED.</li>
     * <li>cargo.container.output - Full path to a file to use for output. [none]</li>
     * <li>cargo.container.log - Full path to a file to use for logging. [none]</li>
     * <li>cargo.servlet.port - The port on which the container should listen. [8080]</li>
     * </ul>
     * </p>
     *
     * @throws Exception if an error occurs.
     */
    protected void setUp() throws Exception
    {

        super.setUp();

        // If there is no container id, default to Tomcat 5x
        String containerId = System.getProperty("cargo.container.id");
        if (containerId == null)
        {
            containerId = Tomcat5xInstalledLocalContainer.ID;
        }
        System.out.println("[INFO] container id: " + containerId);

        // Construct the war, using the container id and the path to the war file
        String deployablePath = System.getProperty("cargo.deployable");
        System.out.println("[INFO] deployable: " + deployablePath);
        Deployable war = new DefaultDeployableFactory().createDeployable(
                containerId, deployablePath, DeployableType.WAR);

        // Container configuration
        ConfigurationFactory configurationFactory = new DefaultConfigurationFactory();

        LocalConfiguration configuration = (LocalConfiguration) configurationFactory
                .createConfiguration(containerId, ConfigurationType.STANDALONE);

        // Find and (if provided) set the port to use for the container.
        String servletPort = System.getProperty("cargo.servlet.port");
        if (servletPort != null)
        {
            configuration.setProperty("cargo.servlet.port", servletPort);
            System.out.println("[INFO] servlet port: " + servletPort);
        }

        configuration.addDeployable(war);

        container = (InstalledLocalContainer) new DefaultContainerFactory()
                .createContainer(containerId, ContainerType.INSTALLED,
                        configuration);

        // If 'cargo.container.home' is not set, or if an expression was
        // passed through, try to use the TOMCAT_HOME environment variable.
        String containerHome = System.getProperty("cargo.container.home");
        if (containerHome == null || containerHome.startsWith("$"))
        {
            containerHome = System.getenv("TOMCAT_HOME");
        }
        System.out.println("[INFO] container home: " + containerHome);
        container.setHome(new File(containerHome));

        // Find and (if provided) set the path to a log file
        String containerLog = System.getProperty("cargo.container.log");
        if (containerLog != null)
        {
            System.out.println("[INFO] container log: " + containerLog);
            container.setLogger(new FileLogger(containerLog, false));
        }

        // Find and (if provided) set the path to an output file
        String containerOutput = System.getProperty("cargo.container.output");
        if (containerOutput != null)
        {
            System.out.println("[INFO] container output: " + containerOutput);
            container.setOutput(new File(containerOutput));
        }

        container.start();
    }

    /**
     * Stop the container after running the tests.
     *
     * @throws Exception if an error occurs.
     */
    protected void tearDown() throws Exception
    {
        container.stop();
        super.tearDown();
    }

    /**
     * Return the name of the test setup.
     * (Temporarily required due to MSUREFIRE-119.)
     *
     * @return the name of the test setup.
     * @deprecated No replacement.
     */

    public String getName()
    {
        return "CargoTestSetup";
    }

}
