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
 * we need a probe to test our asynchronous behavior
 * the asynchronous behavior needs to be in an asynchronous runnable
 */
import {IAsyncRunnable} from "../../impl/util/AsyncRunnable";
import {IValueHolder} from "mona-dish";

export class ProbeClass implements IAsyncRunnable<Promise<any>>, IValueHolder<Promise<any>> {

    value: Promise<any>;
    fCatch: (data: any) => any;
    fThen: (data: any) => any;
    fFinally: () => any;

    thenPerformed = false;
    catchPerformed = false;
    finallyPerformed = false;

    constructor(private $timeout: Function, private resolveIt = true) {
        this.value = new Promise((resolve, reject) => {
            this.$timeout(() => {
                if (this.resolveIt) {
                    resolve(() => true);
                } else {
                    reject();
                }
            }, 100);
        });
    }

    catch(func: (data: any) => any): IAsyncRunnable<boolean> {
        let catchFunc = (data: any) => {
            this.catchPerformed = true;
            return func(data);
        };
        if (this.value) {
            this.value.catch(catchFunc);
        } else {
            this.fCatch = catchFunc;
        }
        return this;
    }

    finally(func: () => void): IAsyncRunnable<boolean> {
        let finallyFunc = () => {
            this.finallyPerformed = true;
            func();
        };
        if (this.value) {
            this.value.finally(finallyFunc);
        } else {
            this.fFinally = finallyFunc;
        }
        return this;
    }

    start() {
        //starts the process

        if (this.fCatch) {
            this.value.catch(this.fCatch);
        }
        if (this.fThen) {
            this.value.then(this.fThen);
        }
        if (this.fFinally) {
            this.value.finally(this.fFinally);
        }
    }

    cancel() {
        //TODO do something with it
    }

    then(func: (data: any) => any): IAsyncRunnable<boolean> {

        let thenFunc = (data: any) => {
            this.thenPerformed = true;
            return func(data);
        };

        if (this.value) {
            this.value.then(thenFunc);
        } else {
            this.fThen = thenFunc;
        }
        return this;
    }
}
