import {describe} from "mocha";
import {StandardInits} from "../frameworkBase/_ext/shared/StandardInits";
import defaultMyFaces = StandardInits.defaultMyFaces;
import {expect} from "chai";

describe('API tests', () => {

    beforeEach(() => {
        return defaultMyFaces();
    });

    it("must pass into ajax.request properly with user parameters", () => {
        let passedSource = null;
        let passedEvent = null;
        let passedOptions = null;
        let oldRequest = (window?.faces ?? window.jsf).ajax.request;
        try {
            (window?.faces ?? window.jsf).ajax.request = function(source, event, options) {
                passedSource = source;
                passedEvent = event;
                passedOptions = options;
            }
            myfaces.ab(null, null, null, null, null, {}, {booga: "foobaz"});

            expect(passedSource).to.eq(null);
            expect(passedEvent).to.eq(null);
            expect(passedOptions).to.deep.eq({params:{booga: "foobaz"}});
        } finally {
            (window?.faces ?? window.jsf).ajax.request = oldRequest;
        }
    })

    it("must pass into ajax.request properly without user parameters", () => {
        let passedSource = null;
        let passedEvent = null;
        let passedOptions = null;
        let oldRequest = (window?.faces ?? window.jsf).ajax.request;
        try {
            (window?.faces ?? window.jsf).ajax.request = function(source, event, options) {
                passedSource = source;
                passedEvent = event;
                passedOptions = options;
            }
            myfaces.ab(null, null, null, null, null, {});

            expect(passedSource).to.eq(null);
            expect(passedEvent).to.eq(null);
            expect(passedOptions).to.deep.eq({params:{}});
        } finally {
            (window?.faces ?? window.jsf).ajax.request = oldRequest;
        }
    });

    it("must pass into ajax.request properly with null options", () => {
        let passedSource = null;
        let passedEvent = null;
        let passedOptions = null;
        let oldRequest = (window?.faces ?? window.jsf).ajax.request;
        try {
            (window?.faces ?? window.jsf).ajax.request = function(source, event, options) {
                passedSource = source;
                passedEvent = event;
                passedOptions = options;
            }
            myfaces.ab(null, null, null, null, null, null);

            expect(passedSource).to.eq(null);
            expect(passedEvent).to.eq(null);
            expect(passedOptions).to.deep.eq({params:{}});
        } finally {
            (window?.faces ?? window.jsf).ajax.request = oldRequest;
        }
    });

    it("must pass into ajax.request properly with null options and null user options", () => {
        let passedSource = null;
        let passedEvent = null;
        let passedOptions = null;
        let oldRequest = (window?.faces ?? window.jsf).ajax.request;
        try {
            (window?.faces ?? window.jsf).ajax.request = function(source, event, options) {
                passedSource = source;
                passedEvent = event;
                passedOptions = options;
            }
            myfaces.ab(null, null, null, null, null, null, null);

            expect(passedSource).to.eq(null);
            expect(passedEvent).to.eq(null);
            expect(passedOptions).to.deep.eq({params:{}});
        } finally {
            (window?.faces ?? window.jsf).ajax.request = oldRequest;
        }
    });

    it("must pass into ajax.request properly without options and user params", () => {
        let passedSource = null;
        let passedEvent = null;
        let passedOptions = null;
        let oldRequest = (window?.faces ?? window.jsf).ajax.request;
        try {
            (window?.faces ?? window.jsf).ajax.request = function(source, event, options) {
                passedSource = source;
                passedEvent = event;
                passedOptions = options;
            }
            myfaces.ab(null, null, null, null, null);

            expect(passedSource).to.eq(null);
            expect(passedEvent).to.eq(null);
            expect(passedOptions).to.deep.eq({params:{}});
        } finally {
            (window?.faces ?? window.jsf).ajax.request = oldRequest;
        }
    });

});