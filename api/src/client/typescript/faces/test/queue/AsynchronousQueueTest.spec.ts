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

import {describe, it} from 'mocha';
import {expect} from 'chai';
import * as sinon from 'sinon';
import * as nise from "nise";

import {ProbeClass} from "./AsynchronousProbe";

import {StandardInits} from "../frameworkBase/_ext/shared/StandardInits";
import {Implementation} from "../../impl/AjaxImpl";
const defaultMyFaces = StandardInits.defaultMyFaces;
import {XhrQueueController} from "../../impl/util/XhrQueueController";
import {IAsyncRunnable} from "../../impl/util/AsyncRunnable";

class ControlledRunnable implements IAsyncRunnable<boolean> {
    started = 0;
    private thenHandlers: Array<(data: any) => any> = [];
    private catchHandlers: Array<(data: any) => any> = [];

    start(): void {
        this.started++;
    }

    cancel(): void {
    }

    then(func: (data: any) => any): IAsyncRunnable<boolean> {
        this.thenHandlers.push(func);
        return this;
    }

    catch(func: (data: any) => any): IAsyncRunnable<boolean> {
        this.catchHandlers.push(func);
        return this;
    }

    finally(func: () => void): IAsyncRunnable<boolean> {
        this.thenHandlers.push(func);
        this.catchHandlers.push(func);
        return this;
    }

    resolve(): void {
        this.thenHandlers.forEach(handler => handler(true));
    }

    reject(): void {
        this.catchHandlers.forEach(handler => handler(false));
    }
}

describe('Asynchronous Queue tests', () => {


    beforeEach(async function () {

        let waitForResult = defaultMyFaces();

        return waitForResult.then((close) => {

            this.xhr = nise.fakeXhr.useFakeXMLHttpRequest();
            this.requests = [];
            this.xhr.onCreate = (xhr) => {
                this.requests.push(xhr);
            };
            (global as any).XMLHttpRequest = this.xhr;
            window.XMLHttpRequest = this.xhr;

            this.jsfAjaxResponse = sinon.stub((global as any).faces.ajax, "response");

            this.closeIt = () => {
                (global as any).XMLHttpRequest = window.XMLHttpRequest = this.xhr.restore();
                this.jsfAjaxResponse.restore();
                Implementation.reset();
                close();
            }
        });
    });

    afterEach(function () {

        this.closeIt();
    });

    it('one entry', (done) => {

        const probe1 = new ProbeClass(setTimeout);

        const queue = new XhrQueueController();
        probe1.then(() => {
            expect(probe1.thenPerformed, "called").to.be.true;
            done();
        });
        queue.enqueue(probe1);
    });

    it('multiple  entries', (done) => {
        const queue = new XhrQueueController();
        const probe1 = new ProbeClass(setTimeout);
        const probe2 = new ProbeClass(setTimeout);
        const probe3 = new ProbeClass(setTimeout);

        let finallyCnt = 0;
        probe1.then(() => {
            expect(probe1.thenPerformed, "called").to.be.true;
            expect(queue.isEmpty).to.be.false;
            finallyCnt++;
        });
        probe2.then(() => {
            expect(probe2.thenPerformed, "called").to.be.true;
            expect(queue.isEmpty).to.be.false;
            finallyCnt++;
        });
        probe3.then(() => {
            expect(probe3.thenPerformed, "called").to.be.true;
            finallyCnt++;
        });

        queue.enqueue(probe1);
        queue.enqueue(probe2);
        queue.enqueue(probe3);

        Promise.all([probe1.value, probe2.value, probe3.value]).then(() => {
            expect(queue.isEmpty).to.be.true;
            (finallyCnt == 3) ? done() : null
        });

    });

    it('must not start a queued entry while another task is running', function () {
        const queue = new XhrQueueController();
        const first = new ControlledRunnable();
        const second = new ControlledRunnable();

        queue.enqueue(first);
        queue.enqueue(second);

        expect(first.started).to.eq(1);
        expect(second.started).to.eq(0);
        expect(queue.isEmpty).to.be.false;

        first.resolve();

        expect(second.started).to.eq(1);
        expect(queue.isEmpty).to.be.true;
    });

    it('must clear queued entries and reset running state', function () {
        const queue = new XhrQueueController();
        const first = new ControlledRunnable();
        const second = new ControlledRunnable();

        queue.enqueue(first);
        queue.enqueue(second);
        queue.clear();

        expect(queue.isEmpty).to.be.true;
        expect(queue.taskRunning).to.be.false;
        expect(second.started).to.eq(0);
    });

    it('must clear queued entries when a runnable rejects', function () {
        const queue = new XhrQueueController();
        const first = new ControlledRunnable();
        const second = new ControlledRunnable();

        queue.enqueue(first);
        queue.enqueue(second);

        first.reject();

        expect(queue.isEmpty).to.be.true;
        expect(queue.taskRunning).to.be.false;
        expect(second.started).to.eq(0);
    });

    it('must reset running state when next is called on an empty queue', function () {
        const queue = new XhrQueueController();
        const first = new ControlledRunnable();

        queue.enqueue(first);
        first.resolve();

        expect(queue.isEmpty).to.be.true;
        expect(queue.taskRunning).to.be.false;
        expect(() => queue.next()).not.to.throw();
        expect(queue.taskRunning).to.be.false;
    });

    it('must debounce delayed enqueues by queue key', function () {
        const clock = sinon.useFakeTimers();
        const queue = new XhrQueueController();
        const first = new ControlledRunnable();
        const second = new ControlledRunnable();

        try {
            queue.enqueue(first, 50);
            queue.enqueue(second, 50);

            clock.tick(49);
            expect(first.started).to.eq(0);
            expect(second.started).to.eq(0);

            clock.tick(1);
            expect(first.started).to.eq(0);
            expect(second.started).to.eq(1);
        } finally {
            clock.restore();
        }
    });

    it('must debounce two instances independently without cross-interference', function () {
        const clock = sinon.useFakeTimers();
        const queueA = new XhrQueueController();
        const queueB = new XhrQueueController();
        const runnableA = new ControlledRunnable();
        const runnableB = new ControlledRunnable();

        try {
            queueA.enqueue(runnableA, 50);
            queueB.enqueue(runnableB, 50);

            clock.tick(49);
            expect(runnableA.started).to.eq(0);
            expect(runnableB.started).to.eq(0);

            clock.tick(1);
            expect(runnableA.started).to.eq(1, "queueA runnable must start after its own debounce window");
            expect(runnableB.started).to.eq(1, "queueB runnable must start after its own debounce window");
        } finally {
            clock.restore();
        }
    });
});
