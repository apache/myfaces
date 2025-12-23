import {StandardInits} from "../frameworkBase/_ext/shared/StandardInits";
import * as sinon from "sinon";
import {Implementation} from "../../impl/AjaxImpl";
import {expect} from "chai";
const protocolPage = StandardInits.protocolPage;

const jsdom = require("jsdom");
const {JSDOM} = jsdom;


describe("Should trigger the progress on xhr request", function () {
    beforeEach(async function () {
        let waitForResult = protocolPage();

        //build up the test fixture
        return waitForResult.then((close) => {
            //we generate an xhr mock class replacement
            this.xhr = sinon.useFakeXMLHttpRequest();
            this.requests = [];

            //we store the requests to have access to them later
            this.xhr.onCreate = (xhr) => {
                this.requests.push(xhr);
            };
            //we anchchor the mock into the fake dom
            (global as any).XMLHttpRequest = this.xhr;
            window.XMLHttpRequest = this.xhr;

            //general cleanup of overloaded resources
            this.closeIt = () => {
                (global as any).XMLHttpRequest = window.XMLHttpRequest = this.xhr.restore();
                Implementation.reset();
                close();
            };
        });
    });
    afterEach(function () {
        this.closeIt();
    });

    it("must trigger progress on xhr request", function() {
        let caughtProgressEvents = [];
        var preinitTriggered = false;
        var loadstartTriggered = false;
        var loadTriggered = false;
        var loadendTriggered = false;
        var timeoutTriggered = false;
        var abortTriggered = false;
        var errorTriggered = false;
        faces.ajax.request(document.getElementById("cmd_eval"), null,
            {
                render: '@form',
                execute: '@form',
                myfaces: {
                    upload: {
                        progress: (upload: XMLHttpRequestUpload, event: ProgressEvent) => {
                            caughtProgressEvents.push(event);
                        },
                        preinit: (upload: XMLHttpRequestUpload) => preinitTriggered = true,
                        loadstart: (upload: XMLHttpRequestUpload, event: ProgressEvent) => loadstartTriggered = true,
                        load: (upload: XMLHttpRequestUpload,  event: ProgressEvent) => loadTriggered = true,
                        loadend: (upload: XMLHttpRequestUpload,  event: ProgressEvent) => loadendTriggered = true,
                        error: (upload: XMLHttpRequestUpload,  event: ProgressEvent) => errorTriggered = true,
                        abort: (upload: XMLHttpRequestUpload,  event: ProgressEvent) => abortTriggered = true,
                        timeout: (upload: XMLHttpRequestUpload,  event: ProgressEvent) => timeoutTriggered = true,

                    }
                }
            });

        let progressEvent = new ProgressEvent("progress");
        let progressEvent2 =  new ProgressEvent("progress");
        let xhr = this.requests.shift();
        xhr.upload.dispatchEvent(new ProgressEvent("loadstart"));
        xhr.upload.dispatchEvent(new ProgressEvent("load"));
        xhr.upload.dispatchEvent(progressEvent);
        xhr.upload.dispatchEvent(progressEvent2);
        xhr.upload.dispatchEvent(new ProgressEvent("loadend"));
        xhr.upload.dispatchEvent(new ProgressEvent("error"));
        xhr.upload.dispatchEvent(new ProgressEvent("abort"));
        xhr.upload.dispatchEvent(new ProgressEvent("timeout"));

        expect(caughtProgressEvents.length).to.eq(2);
        expect(caughtProgressEvents[0] === progressEvent).to.eq(true);
        expect(caughtProgressEvents[1] === progressEvent2).to.eq(true);

        expect(preinitTriggered).to.eq(true);
        expect(loadstartTriggered).to.eq(true);
        expect(loadTriggered).to.eq(true);
        expect(loadendTriggered).to.eq(true);
        expect(errorTriggered).to.eq(true);
        expect(abortTriggered).to.eq(true);
        expect(timeoutTriggered).to.eq(true);
    });
});