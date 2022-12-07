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
package tomcatrun;

import java.io.File;

import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.StandardRoot;

/**
 * starter for the embedded Tomcat
 */
public class Main {

	private static final String CLASSES_DIR = "target/classes";
	private static final String WEBAPP = "src/main/webapp/";
    private static final String webxmlDirLocation = "src/main/webapp/WEB-INF/web.xml";
    private static final String DEFAULT_PORT = "8080";

	public static void main(String[] args) throws Exception {

        String webappDirLocation = WEBAPP;
        Tomcat tomcat = new Tomcat();

        //The port that we should run on can be set into an environment variable
        //Look for that variable and default to 8080 if it isn't there.
        String webPort = System.getenv("PORT");
        if(webPort == null || webPort.isEmpty()) {
            webPort = DEFAULT_PORT;
        }

        tomcat.setPort(Integer.valueOf(webPort));

        StandardContext ctx = (StandardContext) tomcat.addWebapp("/IntegrationJSTest", new File(webappDirLocation).getAbsolutePath());

        File additionWebInfClasses = new File(CLASSES_DIR);
        WebResourceRoot resources = new StandardRoot(ctx);
        resources.addPreResources(new DirResourceSet(resources, "/WEB-INF/classes",
                additionWebInfClasses.getAbsolutePath(), "/"));
        ctx.setResources(resources);
        ctx.setDefaultWebXml(new File(webxmlDirLocation).getAbsolutePath());
        tomcat.getServer().setParentClassLoader(Thread.currentThread().getContextClassLoader());
        tomcat.getConnector(); // to init the connector
        tomcat.start();
        tomcat.getServer().await();
    }
}