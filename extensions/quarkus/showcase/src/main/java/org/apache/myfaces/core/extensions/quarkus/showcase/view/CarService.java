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
package org.apache.myfaces.core.extensions.quarkus.showcase.view;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

@Named("carService")
@ApplicationScoped
public class CarService
{

    private final static String[] COLORS;

    private final static String[] BRANDS;

    static
    {
        COLORS = new String[10];
        COLORS[0] = "Black";
        COLORS[1] = "White";
        COLORS[2] = "Green";
        COLORS[3] = "Red";
        COLORS[4] = "Blue";
        COLORS[5] = "Orange";
        COLORS[6] = "Silver";
        COLORS[7] = "Yellow";
        COLORS[8] = "Brown";
        COLORS[9] = "Maroon";

        BRANDS = new String[10];
        BRANDS[0] = "BMW";
        BRANDS[1] = "Mercedes";
        BRANDS[2] = "Volvo";
        BRANDS[3] = "Audi";
        BRANDS[4] = "Renault";
        BRANDS[5] = "Fiat";
        BRANDS[6] = "Volkswagen";
        BRANDS[7] = "Honda";
        BRANDS[8] = "Jaguar";
        BRANDS[9] = "Ford";
    }

    public List<Car> createCars(int size)
    {
        List<Car> list = new ArrayList<Car>();
        for (int i = 0; i < size; i++)
        {
            list.add(new Car(getRandomId(), getRandomBrand(), getRandomYear(),
                    getRandomColor(), getRandomPrice(), getRandomSoldState()));
        }

        return list;
    }

    private String getRandomId()
    {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    private int getRandomYear()
    {
        return (int) (Math.random() * 50 + 1960);
    }

    private String getRandomColor()
    {
        return COLORS[(int) (Math.random() * 10)];
    }

    private String getRandomBrand()
    {
        return BRANDS[(int) (Math.random() * 10)];
    }

    private int getRandomPrice()
    {
        return (int) (Math.random() * 100000);
    }

    private boolean getRandomSoldState()
    {
        return Math.random() > 0.5;
    }

    public List<String> getColors()
    {
        return Arrays.asList(COLORS);
    }

    public List<String> getBrands()
    {
        return Arrays.asList(BRANDS);
    }
}
