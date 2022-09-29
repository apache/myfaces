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
import {AsyncRunnable} from "./AsyncRunnable";

/**
 * Asynchronous queue which starts to work
 * through the callbacks until the queue is empty
 *
 * Every callback must be of async runnable
 * which is sort of an extended promise which has
 * added a dedicated cancel and start point
 *
 * This interface can be used as wrapper contract
 * for normal promises if needed.
 */
export class AsynchronousQueue<T extends AsyncRunnable<any>> {

    private runnableQueue = [];
    private delayTimeout: null | ReturnType<typeof setTimeout>;

    currentlyRunning: AsyncRunnable<any>;

    constructor() {
    }

    /**
     * simple is empty accessor, returns true if queue is empty atm
     */
    get isEmpty(): boolean {
        return !this.runnableQueue.length;
    }

    /**
     * enqueues an element and starts the
     * asynchronous work loop if not already running
     *
     * @param element the element to be queued and processed
     * @param delay possible delay after our usual process or drop if something newer is incoming algorithm
     */
    enqueue(element: T, delay = 0) {
        if (this.delayTimeout) {
            clearTimeout(this.delayTimeout);
            this.delayTimeout = null;
        }
        if (delay) {
            this.delayTimeout = setTimeout(() => {
                this.appendElement(element);
            });
        } else {
            this.appendElement(element);
        }
    }

    /**
     * fetches the next element from the queue (first in first out order)
     */
    dequeue(): T | undefined{
        return this.runnableQueue.shift();
    }

    /**
     * clears up all elements from the queue
     */
    cleanup() {
        this.currentlyRunning = null;
        this.runnableQueue.length = 0;
    }

    /**
     * cancels the currently running element and then cleans up the queue
     * aka cancel the queue entirely
     */
    cancel() {
        try {
            if (this.currentlyRunning) {
                this.currentlyRunning.cancel();
            }
        } finally {
            this.cleanup();
        }
    }

    private callForNextElementToProcess() {
        this.runEntry();
    }

    private appendElement(element: T) {
        //only if the first element is added we start with a trigger
        //otherwise a process already is running and not finished yet at that
        //time
        this.runnableQueue.push(element);
        if (!this.currentlyRunning) {
            this.runEntry();
        }
    }

    private runEntry() {
        if (this.isEmpty) {
            this.currentlyRunning = null;
            return;
        }
        this.currentlyRunning = this.dequeue();
        this.currentlyRunning
            .catch((e) => {
                //in case of an error we always clean up the remaining calls
                //to allow a clean recovery of the application
                this.cleanup();
                throw e;
            })
            .then(
                //the idea is to trigger the next over an event to reduce
                //the number of recursive calls (stacks might be limited
                //compared to ram)
                //naturally give we have a DOM, the DOM is the natural event dispatch system
                //which we can use, to decouple the calls from a recursive stack call
                //(the browser engine will take care of that)
                () => this.callForNextElementToProcess()
            ).start();
    }
}