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
package org.apache.myfaces.core.extensions.quarkus.deployment;

import static io.quarkus.deployment.pkg.PackageConfig.JarConfig.JarType.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import io.quarkus.deployment.IsNormal;
import io.quarkus.deployment.pkg.builditem.UberJarMergedResourceBuildItem;
import org.atteo.xmlcombiner.XmlCombiner;
import org.jboss.logging.Logger;
import org.xml.sax.SAXException;

import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.GeneratedResourceBuildItem;
import io.quarkus.deployment.pkg.PackageConfig;

/**
 * This class processes the faces configuration and web fragment XML files in an Uber JAR.
 * It merges the contents of "META-INF/web-fragment.xml" and "META-INF/faces-config.xml"
 * into single files if they are present in the resources.
 */
public class FacesUberJarProcessor
{

    private static final Logger log = Logger.getLogger(FacesUberJarProcessor.class);

    /**
     * Produces `UberJarMergedResourceBuildItem`s for each specified service file to be included in the Uber JAR.
     * <p>
     * This build step is only executed in "normal" mode and registers each of the listed services in
     * the `META-INF/services` directory.
     *
     * @param producer The build item producer for creating `UberJarMergedResourceBuildItem` instances.
     */
    @BuildStep(onlyIf = IsNormal.class)
    void uberJarServiceLoaders(BuildProducer<UberJarMergedResourceBuildItem> producer)
    {
        List<String> serviceFiles = List.of(
                "myfaces-metadata.xml",
                "services/jakarta.el.ExpressionFactory",
                "services/jakarta.enterprise.inject.spi.Extension",
                "services/jakarta.json.spi.JsonProvider",
                "services/jakarta.servlet.ServletContainerInitializer",
                "services/jakarta.websocket.ContainerProvider",
                "services/jakarta.websocket.server.ServerEndpointConfig$Configurator",
                "services/org.apache.myfaces.spi.AnnotationProvider",
                "services/org.apache.myfaces.spi.InjectionProvider"
        );

        for (String serviceFile : serviceFiles)
        {
            producer.produce(new UberJarMergedResourceBuildItem("META-INF/" + serviceFile));
        }
    }

    /**
     * Merges specified XML files if the package type is UBER_JAR and generates them
     * as resources in the Uber JAR.
     *
     * @param generatedResourcesProducer the producer to add generated resources
     * @param packageConfig the package configuration to check for UBER_JAR type
     */
    @BuildStep(onlyIf = IsNormal.class)
    void uberJarXmlFiles(BuildProducer<GeneratedResourceBuildItem> generatedResourcesProducer,
            PackageConfig packageConfig)
    {
        if (packageConfig.jar().type() == UBER_JAR)
        {
            mergeAndGenerateResource("META-INF/web-fragment.xml", generatedResourcesProducer);
            mergeAndGenerateResource("META-INF/faces-config.xml", generatedResourcesProducer);
        }
    }

    /**
     * Merges all occurrences of the specified XML file found in the resources and
     * generates a single combined version.
     *
     * @param filename the name of the XML file to be merged
     * @param generatedResourcesProducer the producer to add the merged resource
     */
    private void mergeAndGenerateResource(String filename,
            BuildProducer<GeneratedResourceBuildItem> generatedResourcesProducer)
    {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try
        {
            XmlCombiner combiner = new XmlCombiner();
            // Retrieve all instances of the specified file in the resources
            List<URL> resources = Collections.list(getClass().getClassLoader().getResources(filename));

            // Combine each resource file found
            for (URL resource : resources)
            {
                try (InputStream is = resource.openStream())
                {
                    combiner.combine(is);
                }
            }
            // Build the combined XML document
            combiner.buildDocument(outputStream);

            // Produce the merged resource for inclusion in the Uber JAR
            generatedResourcesProducer.produce(new GeneratedResourceBuildItem(filename, outputStream.toByteArray()));
        }
        catch (ParserConfigurationException | SAXException | TransformerException | IOException ex)
        {
            log.errorf("Unexpected error combining %s", filename, ex);
        }
    }
}