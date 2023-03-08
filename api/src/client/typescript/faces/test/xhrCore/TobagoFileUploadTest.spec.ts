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
import {describe, it} from "mocha";
import * as sinon from "sinon";
import {expect} from "chai";
import {StandardInits} from "../frameworkBase/_ext/shared/StandardInits";
import {DomQuery} from "mona-dish";
import {Implementation} from "../../impl/AjaxImpl";
import defaultFileForm = StandardInits.tobagoFileForm;

declare var faces: any;

/**
 * specialized tests testing the xhr core behavior when it hits the xmlHttpRequest object
 */
describe('Tests on the xhr core when it starts to call the request', function () {
  beforeEach(async function () {

    let waitForResult = defaultFileForm();
    return waitForResult.then((close) => {

      this.xhr = sinon.useFakeXMLHttpRequest();
      this.requests = [];

      this.respond = (response: string): XMLHttpRequest => {
        let xhrReq = this.requests.shift();
        xhrReq.responsetype = "text/xml";
        xhrReq.respond(200, {'Content-Type': 'text/xml'}, response);
        return xhrReq;
      };

      this.xhr.onCreate = (xhr) => {
        this.requests.push(xhr);
      };
      (<any>global).XMLHttpRequest = this.xhr;
      window.XMLHttpRequest = this.xhr;

      this.closeIt = () => {
        (<any>global).XMLHttpRequest = window.XMLHttpRequest = this.xhr.restore();
        Implementation.reset();
        close();
      }
    });

  });
  afterEach(function () {
    this.closeIt();
  });

  it('tobago file upload', function (done) {
    let send = sinon.spy(XMLHttpRequest.prototype, "send");

    const POST = "POST";

    try {
      let fileUploadField = DomQuery.byId("page:fileAjax::field");
      let actionElement = DomQuery.byId("page:fileAjax");

      fileUploadField.addEventListener("change", (event: Event) => {
        faces.ajax.request(
            actionElement,
            event,
            {
              "jakarta.faces.behavior.event": "change",
              execute: 'page:fileAjax',
              render: null
            });
      }).dispatchEvent(new Event("change"));

      expect(this.requests.length).to.eq(1);
      let request = this.requests[0];
      expect(request.method).to.eq(POST);
      expect(request.async).to.be.true;
      expect(send.called).to.be.true;
      expect(send.callCount).to.eq(1);
      expect(request.requestBody instanceof FormData).to.be.true;

      let formData: FormData = request.requestBody;
      expect(formData.get("page::lastFocusId")).to.eq("");
      expect(formData.get("org.apache.myfaces.tobago.webapp.Secret")).to.eq("secretValue");
      expect(formData.get("jakarta.faces.ViewState")).to.eq("viewStateValue");
      expect(formData.get("jakarta.faces.RenderKitId")).to.eq("tobago");
      expect(formData.get("jakarta.faces.ClientWindow")).to.eq("clientWindowValue");
      expect(formData.get("jakarta.faces.behavior.event")).to.eq("change");
      expect(formData.get("jakarta.faces.partial.event")).to.eq("change");
      expect(formData.get("jakarta.faces.source")).to.eq("page:fileAjax");
      expect(formData.get("jakarta.faces.partial.ajax")).to.eq("true");
      expect(formData.get("page::form")).to.eq("page::form");
      expect(formData.get("jakarta.faces.partial.execute")).to.eq("page:fileAjax");

    } finally {
      send.restore();
    }
    done();
  });
});
