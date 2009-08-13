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

package org.apache.myfaces.view.facelets.bean;

public class Example {
    
    private static String[] Departments = new String[] { "HR", "RD" };

    public Example() {
        super();
    }
    
    public static Company createCompany() {
        Company c = new Company();
        c.setName("Enverio");
        c.setPresident(new Employee(1, "Hookom", "Jacob", true));
        c.getDepartments().add(createHR());
        c.getDepartments().add(createRD());
        return c;
    }
    
    public static Department createDepartment() {
        return createRD();
    }
    
    public static Department createHR() {
        Department d = new Department();
        d.setDirector(new Employee(2, "Ashenbrener", "Aubrey", true));
        d.setName("HR");
        d.getEmployees().add(new Employee(3, "Ellen", "Sue", false));
        d.getEmployees().add(new Employee(4, "Scooner", "Mary", false));
        return d;
    }
    
    public static Department createRD() {
        Department d = new Department();
        d.setDirector(new Employee(5, "Winer", "Adam", true));
        d.setName("RD");
        d.getEmployees().add(new Employee(6, "Burns", "Ed", false));
        d.getEmployees().add(new Employee(7, "Lubke", "Ryan", false));
        d.getEmployees().add(new Employee(8, "Kitain", "Roger", false));
        return d;
    }

}
