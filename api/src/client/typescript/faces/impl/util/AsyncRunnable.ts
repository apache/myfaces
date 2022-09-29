/*! Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Asynchronous queue member for our asynchronous queue
 * Ever object in the asynchronous queue needs to implement this interface
 *
 * the usage should be similar as a Promise from the outside.
 * but with a dedicated start point. The problem why we cannot use
 * promises here, is mostly related to the needed cancel functionality
 * and that the queue expects a runnable as entry.
 *
 * from the implementation side it is mostly registering callbacks
 * and calling them at the appropriate time.
 */
export interface AsyncRunnable<T> {
    /**
     * starts the runnable
     */
    start();

    /**
     * cancel the current ongoing op if possible
     */
    cancel();

    /**
     * callback for then functionality
     * triggered when the async run is complete
     *
     * the async runnable must register the passed function
     * and then triggers all the registered thens
     * when it is complete
     *
     * @param func
     */
    then(func: (data: any) => any): AsyncRunnable<T>;

    /**
     * callback for catch functionality
     * triggered when the asynch run is complete
     *
     * the async runnable must register the passed function
     * and then triggers all the registered catchs
     * when an error has occurred
     *
     * @param func
     */
    catch(func: (data: any) => any): AsyncRunnable<T>;


    /**
     * finally called when all then and catches are performed
     * same this is a register function
     * and once the finally time for the promise has
     * come the finally functions must be performed
     */
    finally(func: () => void): AsyncRunnable<T>;
}