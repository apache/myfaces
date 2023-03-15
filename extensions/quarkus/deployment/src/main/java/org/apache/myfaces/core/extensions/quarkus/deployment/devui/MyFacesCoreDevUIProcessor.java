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
package org.apache.myfaces.core.extensions.quarkus.deployment.devui;

import org.apache.myfaces.config.webparameters.MyfacesConfig;

import io.quarkus.deployment.IsDevelopment;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.devui.spi.page.CardPageBuildItem;
import io.quarkus.devui.spi.page.Page;
import io.quarkus.devui.spi.page.PageBuilder;

/**
 * Dev UI card for displaying important details such as the library version.
 */
public class MyFacesCoreDevUIProcessor
{

    @BuildStep(onlyIf = IsDevelopment.class)
    void createVersion(BuildProducer<CardPageBuildItem> cardPageBuildItemBuildProducer)
    {
        final CardPageBuildItem card = new CardPageBuildItem("MyFaces Core");

        final PageBuilder versionPage = Page.externalPageBuilder("Version")
                .icon("font-awesome-solid:book")
                .url("https://myfaces.apache.org/#/core40")
                .isHtmlContent()
                .staticLabel(MyfacesConfig.class.getPackage().getImplementationVersion());

        card.addPage(versionPage);

        card.setCustomCard("qwc-myfaces-core-card.js");

        cardPageBuildItemBuildProducer.produce(card);
    }
}
