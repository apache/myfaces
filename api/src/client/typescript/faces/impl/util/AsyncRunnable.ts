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
 * the usage should be similar as Promise. from the outside.
 * but with a dedicated start point. The problem why we cannot use
 * promises here, is mostly related to the needed cancel functionality
 * and that the queue expects a runnable as entry.
 *
 * from the implementation side it is mostly registering callbacks
 * and calling them at the appropriate time.
 */
export interface IAsyncRunnable<T> {
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
     * and then triggers all the registered then functions
     * when it is complete
     *
     * @param func the then functor
     */
    then(func: (data: any) => any): IAsyncRunnable<T>;

    /**
     * callback for catch functionality
     * triggered when the async run is complete
     *
     * the async runnable must register the passed function
     * and then triggers all the registered catch handlers
     * when an error has occurred
     *
     * @param func
     */
    catch(func: (data: any) => any): IAsyncRunnable<T>;


    /**
     * finally called when all then and catches are performed
     * same this is a register function
     * and once the finally time for the promise has
     * come the finally functions must be performed
     */
    finally(func: () => void): IAsyncRunnable<T>;
}


/**
 * pretty much the same as cancellable Promise, but given
 * we do not have that on browser level yet this is sort
 * of a non - intrusive Shim!
 */
export abstract class AsyncRunnable<T> implements IAsyncRunnable<T>{
    /**
     * helper support so that we do not have to drag in Promise shims
     */
    private catchFunctions: Array<Function> = [];
    private thenFunctions: Array<Function> = [];

    /**
     * cancel the run of the runnable (which then depending on the implementation
     * either triggers indirectly resolve or reject)
     */
    abstract cancel(): void;

    /**
     * extended functionality start to trigger the runnable
     */
    abstract start(): void;

    /**
     * resolve handler function which calls the then chain
     * and after that finally
     * @param data
     */
    resolve(data: any) {
        this.thenFunctions.reduce((inputVal: any, thenFunc: any) => {
            return thenFunc(inputVal);
        }, data)
    }

    /**
     * reject handler function which triggers the catch chain
     * @param data
     */
    reject(data: any) {
        this.catchFunctions.reduce((inputVal: any, catchFunc: any) => {
            return catchFunc(inputVal);
        }, data);
    }

    /**
     * register a catch functor
     * @param func the functor for the catch monad
     */
    catch(func: (data: any) => any): IAsyncRunnable<T> {
        this.catchFunctions.push(func);
        return this;
    }

    /**
     * registers a finally functor
     * @param func the functor for the finally handling chanin
     */
    finally(func: () => void): IAsyncRunnable<T> {
        // no ie11 support we probably are going to revert to shims for that one
        this.catchFunctions.push(func);
        this.thenFunctions.push(func);
        return this;
    }

    /**
     * @param func then functor similar to promise
     */
    then(func: (data: any) => any): IAsyncRunnable<T> {
        this.thenFunctions.push(func);
        return this;
    }
}

