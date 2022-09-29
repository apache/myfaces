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
import {ProbeClass} from "./AsynchronousProbe";
import {AsynchronousQueue} from "../../impl/util/AsyncQueue";
import {StandardInits} from "../frameworkBase/_ext/shared/StandardInits";
import {Implementation} from "../../impl/AjaxImpl";
import defaultMyFaces = StandardInits.defaultMyFaces;

describe('Asynchronous Queue tests', () => {


    beforeEach(async function () {

        let waitForResult = defaultMyFaces();

        return waitForResult.then((close) => {

            this.xhr = sinon.useFakeXMLHttpRequest();
            this.requests = [];
            this.xhr.onCreate = (xhr) => {
                this.requests.push(xhr);
            };
            (<any>global).XMLHttpRequest = this.xhr;
            (<any>window).XMLHttpRequest = this.xhr;

            this.jsfAjaxResponse = sinon.stub((<any>global).faces.ajax, "response");

            this.closeIt = () => {
                (<any>global).XMLHttpRequest = (<any>window).XMLHttpRequest = this.xhr.restore();
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

        const queue = new AsynchronousQueue();
        probe1.then(() => {
            expect(probe1.thenPerformed, "called").to.be.true;
            done();
        });
        queue.enqueue(probe1);
    });

    it('multiple  entries', (done) => {
        const queue = new AsynchronousQueue();
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

    //TODO error test?
});