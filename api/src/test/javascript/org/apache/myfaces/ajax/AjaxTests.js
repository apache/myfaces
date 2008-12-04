/* 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */
/**
 * testing various aspects of the jsf2
 * ajax api!
 */


function AjaxTest(name)
{
    TestCase.call( this, name );
}


function AjaxTest_testNameSpacing() {
    this.assertTrue(true);
    this.assertTrue( "namespacing javax exists" , 'undefined' != typeof javax );
    this.assertTrue( "namespacing javax.faces.Ajax exists" , 'undefined' != typeof javax.faces );
    this.assertTrue( "namespacing javax.faces.Ajax exists" , 'undefined' != typeof javax.faces.Ajax );
}


//TODO add additional assertions covering the entire scripting interfaces
//all which has to be done is simply to check for an existing function
//within the javax.faces.Ajax function array!



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