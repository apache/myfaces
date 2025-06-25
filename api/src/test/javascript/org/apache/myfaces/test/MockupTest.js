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
 * JSUnit Test for our testing scenario to be able
 * to test the test integration
 *
 * We need to test if the scenario test is given
 * then we have most of the setup in place
 * and do various tests on our test mockups if they behave accordingly!
 *
 */

function SimpleTest(name)
{
    TestCase.call( this, name );
}


function SimpleTest_testTestScenario() {
    this.assertTrue( "This test should succeed" , helloWorld() == "hello world" );
}

/**
 * check for existing dom and
 */
function SimpleTest_testDocumentAndWindowExists() {
    this.assertTrue("Window object exists", 'undefined' != typeof(window) && null != window);
    this.assertTrue("Document Object exists", 'undefined' != typeof(document) && null != document);
    this.assertTrue("Node type exists", 'undefined' != typeof(node) && null != node);
}

function SimpleTest_testDocumentHasNode() {
    this.assertTrue("Document has 1 node", 'undefined' != typeof(document.node) && null != document.node);
}

function SimpleTest_testElementInsertion() {
    var element = document.createElement("div");
    this.assertTrue("Element must be of the correct type", element instanceof node);
    element.id = "rootNode_after_document";
    document.node.appendChild(element);
     
    this.assertTrue("nodelist on the document must be size 1", document.node.childNodes.length == 1);

    var element2 = document.createElement("div");
    element2.id = "div2";
    document.node.appendChild(element2);

    this.assertTrue("nodelist on the document must be size 2", document.node.childNodes.length == 2);
    this.assertTrue("previoussibling test 1", 'undefined' == typeof(element.previousSibling) || null == element.previousSibling);
    this.assertTrue("previoussibling test 2", 'undefined' != typeof(element.nextSibling) && null != element.nextSibling);

    this.assertTrue("previoussibling test 3", 'undefined' == typeof(element2.nextSibling) || null == element2.nextSibling);
    this.assertTrue("previoussibling test 4", 'undefined' != typeof(element2.previousSibling) && null != element2.previousSibling);

    var element3 = document.createElement("div");
    element3.id = "div3";

    element2.appendChild(element3);

    var element4 = document.createElement("div");
    element4.id = "div4";
    document.node.appendChild(element4);


    this.assertTrue("previoussibling test 3", 'undefined' != typeof(element2.nextSibling) && null != element2.nextSibling);
    this.assertTrue("previoussibling test 4", 'undefined' != typeof(element2.previousSibling) && null != element2.previousSibling);

    this.assertTrue("previoussibling test 1", 'undefined' == typeof(element3.previousSibling) || null == element3.previousSibling);
    this.assertTrue("previoussibling test 3", 'undefined' == typeof(element3.nextSibling) || null == element3.nextSibling);

    var findElement = document.getElementById("div3");

    this.assertTrue("div 3 found", 'undefined' != typeof(findElement) && null != findElement);
    this.assertTrue("div3 is correct", findElement == element3);
    findElement = document.getElementById("div4");
    this.assertTrue("div 4 found", 'undefined' != typeof(findElement) && null != findElement);

    var tagNames = document.node.getElementsByTagName("div");
    this.assertTrue("checking for elementByTagName size", 'undefined' != typeof(tagNames) && null != tagNames );

    this.assertTrue("size of tags", tagNames.length == 4);
}


function SimpleTest_testElementRemoval() {
    /*lets throw away everything, do not do this in a browser environment!*/
    document._reset();
    var element = document.createElement("div");
    this.assertTrue("Element must be of the correct type", element instanceof node);
    element.id = "rootNode after document";
    document.node.appendChild(element);


    var element2 = document.createElement("div");
    element2.id = "div2";
    document.node.appendChild(element2);

    var element3 = document.createElement("div");
    element3.id = "div3";

    element2.appendChild(element3);

    var element4 = document.createElement("div");
    element4.id = "div4";
    document.node.appendChild(element4);

    var tagNames = document.node.getElementsByTagName("div");
    this.assertTrue("size of tags 1:"+tagNames.length, tagNames.length == 4);


    document.node.removeChild(element4);

    tagNames = document.node.getElementsByTagName("div");
    this.assertTrue("size of tags 2:"+tagNames.length, tagNames.length == 3);

    this.assertTrue("element2 next node == null", 'undefined' == typeof(element2.nextSibling) || null == element2.nextSibling);

    document.node.appendChild(element4);
    tagNames = document.node.getElementsByTagName("div");
    this.assertTrue("size of tags 4:"+tagNames.length, tagNames.length == 4);

    document.node.removeChild(element2);
    
    tagNames = document.node.getElementsByTagName("div");
    this.assertTrue("size of tags 4:"+tagNames.length, tagNames.length == 2);

    document.node.appendChild(element2);
    tagNames = document.node.getElementsByTagName("div");
    this.assertTrue("size of tags 4:"+tagNames.length, tagNames.length == 4);

}

SimpleTest.prototype = new TestCase();
SimpleTest.glue();








function SimpleTestTestSuite()
{
    TestSuite.call( this, "SimpleTestTest" );
    this.addTestSuite( SimpleTest );
}




SimpleTestTestSuite.prototype = new TestSuite();
SimpleTestTestSuite.prototype.suite = function (){ 
    return new SimpleTestTestSuite();
}
