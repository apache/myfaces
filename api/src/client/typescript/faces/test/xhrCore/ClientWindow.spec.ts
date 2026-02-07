import {describe} from "mocha";
import * as sinon from "sinon";
import {Implementation} from "../../impl/AjaxImpl";
import {StandardInits} from "../frameworkBase/_ext/shared/StandardInits";
import {expect} from "chai";
import * as nise from "nise";

const jsdom = require("jsdom");
const {JSDOM} = jsdom;

describe('adds a getClientWindowTests', function () {

    // noinspection DuplicatedCode
    beforeEach(function () {

        let waitForResult = StandardInits.tobagoFileForm();

        return waitForResult.then((close) => {

            this.xhr = nise.fakeXhr.useFakeXMLHttpRequest();
            this.requests = [];
            this.xhr.onCreate = (xhr) => {
                this.requests.push(xhr);
            };
            (global as any).XMLHttpRequest = this.xhr;
            window.XMLHttpRequest = this.xhr;

            this.jsfAjaxResponse = sinon.spy((global as any).faces.ajax, "response");

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

    it("must handle a node based client window call", function (done) {
        let ret = faces.getClientWindow(document.getElementById("page::form"));
        expect(ret).to.eq("clientWindowValue");
        done();
    });

    it("must handle a url based client id as well", function (done) {
        let waitForResult = StandardInits.defaultMyFaces();
        let oldWindow = window;


        waitForResult.then(() => {
            global["window"] = Object.create(window);
            Object.defineProperty(window, 'location', {
                value: {
                    href: oldWindow.location.href + "?jfwid=test2"
                }
            });
            let ret = faces.getClientWindow(document.body);
            expect(ret).to.eq("test2");
            done();
        })
    });

    it("document highest priority", function (done) {
            const oldWindow = window;
            global["window"] = Object.create(window);
            Object.defineProperty(window, 'location', {
                value: {
                    href: oldWindow.location.href + "?jfwid=test2"
                }
            });
            let ret = faces.getClientWindow(document.body);
            expect(ret).to.eq("clientWindowValue");
            done();
        })

});