/* Licensed to the Apache Software Foundation (ASF) under one or more
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
 * testing various aspects of the jsf2
 * ajax api!
 */


function AjaxTest(name)
{
    TestCase.call( this, name );
}


/**
 * test the basic namespacing being active!
 */
function AjaxTest_testNameSpacing() {
    this.assertTrue( "namespacing javax exists" , 'undefined' != typeof javax && null != typeof(javax) );
    this.assertTrue( "namespacing jakarta.faces.Ajax exists" , 'undefined' != typeof jakarta.faces && null != typeof(jakarta.faces) );
    this.assertTrue( "namespacing jakarta.faces.Ajax exists" , 'undefined' != typeof jakarta.faces.Ajax && null != typeof(jakarta.faces.Ajax) );
}

/**
 * test for all public functions to be present!
 */
function AjaxTest_testPublicFunctionAvailability() {
    this.assertTrue("jakarta.faces.Ajax.ajaxRequest present", 'undefined' != typeof(jakarta.faces.Ajax.ajaxRequest) && null != jakarta.faces.Ajax.ajaxRequest);
    this.assertTrue("jakarta.faces.Ajax.ajaxResponse present", 'undefined' != typeof(jakarta.faces.Ajax.ajaxResponse) && null != jakarta.faces.Ajax.ajaxResponse);
    this.assertTrue("jakarta.faces.Ajax.getProjectStage present", 'undefined' != typeof(jakarta.faces.Ajax.getProjectStage) && null != jakarta.faces.Ajax.getProjectStage);
    this.assertTrue("jakarta.faces.Ajax.viewState ", 'undefined' != typeof(jakarta.faces.Ajax.viewState ) && null != jakarta.faces.Ajax.viewState );
}


//TODO add additional assertions covering the entire scripting interfaces
//all which has to be done is simply to check for an existing function
//within the jakarta.faces.Ajax function array!



AjaxTest.prototype = new TestCase();
AjaxTest.glue();

function AjaxAPITestTestSuite()
{
    TestSuite.call( this, "AjaxAPITestTest" );
    this.addTestSuite( AjaxTest );
}
AjaxAPITestTestSuite.prototype = new TestSuite();
AjaxAPITestTestSuite.prototype.suite = function (){
    return new AjaxAPITestTestSuite();
}