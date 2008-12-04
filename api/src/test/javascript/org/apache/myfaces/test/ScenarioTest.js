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
 * JSUnit Test for our testing scenario to be able
 * to test the test integration
 */

function SimpleTest(name)
{
    TestCase.call( this, name );
}


function SimpleTest_testTestScenario() {
    this.assertTrue( "This test should succeed" , helloWorld() == "hello world" );
}

SimpleTest.prototype = new TestCase();
SimpleTest.glue();


function SimpleTestTestSuite()
{
    TestSuite.call( this, "SimpleTestTest" );
    this.addTestSuite( SimpleTest );
}
SimpleTestTestSuite.prototype = new TestSuite();
SimpleTestTestSuite.prototype.suite = function (){ return new SimpleTestTestSuite(); }