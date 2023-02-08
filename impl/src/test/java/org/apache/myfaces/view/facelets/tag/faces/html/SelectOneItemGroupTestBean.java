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
package org.apache.myfaces.view.facelets.tag.faces.html;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Named
@RequestScoped
public class SelectOneItemGroupTestBean {

    private List<Product> animalList;
    private Long selectedAnimalId;

    @PostConstruct
    public void init() {
        this.animalList = Arrays.asList(new Product(1L, "Dog"),new Product(2L, "Cat"), new Product(3L, "Fish"));

    }

    public List<Product> getAnimalList() {
        return this.animalList;
      }

    public Long getSelectedAnimalId() {
        return selectedAnimalId;
    }

    public void setSelectedAnimalId(Long selectedAnimalId) {
        this.selectedAnimalId = selectedAnimalId;
    }

    public static class Product {
        private Long id;
        private String name;

        public Product() {
        }

        public Product(Long id, String name) {
            this.id = id;
            this.name = name;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
