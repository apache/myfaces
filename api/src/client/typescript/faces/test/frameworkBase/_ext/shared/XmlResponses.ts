export class XmlResponses {

    static EVAL_1 = `
    <partial-response>
    <changes>
        <eval><![CDATA[
            document.getElementById('evalarea1').innerHTML = 'eval test succeeded';
        ]]></eval>
    </changes>
    </partial-response>
    `;

    static UPDATE_INSERT_1 = `
    <partial-response>
    <changes>
        <update id="changesArea"><![CDATA[
        <div id='changesArea'>update succeeded 1</div><script type='text/javascript'>document.getElementById('evalarea2').innerHTML='embedded script at update succeed';</script>
        ]]></update>
        <insert id="inserted1" before="changesArea"><![CDATA[
            <div id='insertbefore'>insert before succeeded should display before test1</div><script type='text/javascript'>document.getElementById('evalarea3').innerHTML='embedded script at insert succeed';</script>
        ]]></insert>
        <insert id="inserted2" after="changesArea"><![CDATA[
            <div  id='insertafter'>insert after succeeded should display after test1</div>
        ]]></insert>
    </changes>
    </partial-response>
    `;

    static SHADOW_DOM_UPDATE=`
        <partial-response>
            <changes><update id="shadowContent"><![CDATA[<div id="shadowContent">after update</div>]]></update></changes>
        </partial-response>           
    `;

    static UPDATE_INSERT_2 = `
    <partial-response>
    <changes>
        <update id="changesArea"><![CDATA[
        <div id='changesArea'>update succeeded 2</div><script type='text/javascript'>document.getElementById('evalarea2').innerHTML='embedded script at update succeed';</script>
        ]]></update>
        <insert id="inserted1" >
            <before id='changesArea'><![CDATA[insert before succeeded should display before test1<script type='text/javascript'>document.getElementById('evalarea3').innerHTML='embedded script at insert succeed';</script>]]></before>
        </insert>    
        <insert id="inserted2">
            <after  id='changesArea'><![CDATA[insert after succeeded should display after test1]]></after>
        </insert>
    </changes>
    </partial-response>
    `;

    static DELETE_1 = `
    <partial-response>
    <changes>
        <delete id="deletable"></delete>
    </changes>
    </partial-response>
    `;

    static VIEWSTATE_1 = `
    <partial-response>
    <changes>
        <update id="jakarta.faces.ViewState"><![CDATA[hello world]]></update>
    </changes>
    </partial-response>
    `;

    static ATTRIBUTE_CHANGE = `
    <partial-response>
    <changes>
        <attributes id="attributeChange">
            <attribute name="style" value="color:rgb(100,100,100);"></attribute>
            <attribute name="style" value="border:1px solid black;"></attribute>
            <attribute name="onclick" value="document.getElementById('evalarea4').innerHTML = 'attributes onclick succeeded';"></attribute>
        </attributes>
    </changes>
    </partial-response>
    `;

    static ERROR_1 = `
    <partial-response>
        <error>
            <error-name>Error1</error-name>    
        </error>
    </partial-response>
    `;
    static ERROR_2 = `
    <partial-response>
        <error>
            <error-name>Erro21</error-name>    
            <error-message><![CDATA[Error2 Text]]></error-message>    
        </error>
    </partial-response>
    `;

    static BODY_REPLACEMENT = `<partial-response>
    <changes>
        <update id="jakarta.faces.ViewBody"><![CDATA[
            <body id="the_id" class="tundra">
            <div id='centerDiv'>
                <h1>Test for body change done</h1>
                <h3>Body replacement test successful</h3>
                <table>
                    <tbody>
                    <td>
                        <div id="embed_target" class='embed-target'>booga</div>
                    </td>
                    <td></td>
                    </tbody>
                </table>
                <script type='text/javascript'>
                    document.getElementById('embed_target').innerHTML = 'hello from embedded script in replacement body';
                </script>
            </div>
            </body>
            ]]>
        </update>
    </changes>
</partial-response>

`;

    static HEAD_REPLACEMENT = `<partial-response>
    <changes>
        <update id="jakarta.faces.ViewHead"><![CDATA[
            <head>
                <meta blarg="blarg2"></meta>
                <script type='text/javascript'>
                    document.getElementById('evalarea1').innerHTML = 'hello from embedded script in replacement head';
                </script>
            </head>
            ]]>
        </update>
    </changes>
</partial-response>

`;

    static VIEW_ROOT_REPLACEMENT = `<partial-response>
    <changes>
        <update id="jakarta.faces.ViewRoot"><![CDATA[
            <html>
            <head>
                <meta blarg="blarg2"></meta>
                <script type='text/javascript'>
                    document.getElementById('evalarea1').innerHTML = 'hello from embedded script in replacement head';
                </script>
            </head>
            <body id="the_id" class="tundra">
            <div id='centerDiv'>
                <h1>Test for body change done</h1>
                <h3>Body replacement test successful</h3>
                <table>
                    <tbody>
                    <td>
                        <div id="evalarea1">blarg</div>
                        <div id="embed_target" class='embed-target'>booga</div>
                    </td>
                    <td></td>
                    </tbody>
                </table>
                <script type='text/javascript'>
                    document.getElementById('embed_target').innerHTML = 'hello from embedded script in replacement body';
                </script>
            </div>
            </body>
            </html>
            ]]>
        </update>
    </changes>
</partial-response>
`;

    static SIMPLE_RESOURCE_RESPONSE = `
<partial-response>
<changes>
<update id="jakarta.faces.Resource">
    <![CDATA[<script src="../../../xhrCore/fixtures/addedViewHead1.js"></script>]]>
</update>
</changes>
</partial-response>
`;
    static MULTIPLE_RESOURCE_RESPONSE = `
<partial-response id="j_id__v_0"><changes><update id="jakarta.faces.Resource">
    <![CDATA[
    <script src="../../../xhrCore/fixtures/addedViewHead2.js"></script>
    <style type="text/css" rel="../../../xhrCore/fixtures/addedViewHead2.css"></style>
    ]]>
</update>
</changes>
</partial-response>
`

    static EMBEDDED_SCRIPTS_RESOURCE_RESPONSE = `
<partial-response id="j_id__v_0"><changes><update id="jakarta.faces.Resource">
    <![CDATA[
        <script src="../../../xhrCore/fixtures/addedViewHead3.js"></script>
        <link rel="stylesheet" href="../../../xhrCore/fixtures/addedViewHead2.css"></link>
        <script type="text/javascript">
            document.getElementById('resource_area_1').innerHTML = 'booga';
        </script>
    ]]>
</update>
</changes>
</partial-response>
`

    static HEAD_REPLACE = `
<partial-response id="j_id__v_0"><changes><update id="jakarta.faces.ViewHead">
    <![CDATA[
    <head>
        <script type="text/javascript" src="../../../xhrCore/fixtures/addedViewHead3.js"></script>
        <link rel="stylesheet" href="../../../xhrCore/fixtures/addedViewHead2.css"></link>
        <script type="text/javascript">
            setTimeout(() => document.getElementById('resource_area_1').innerHTML = 'booga', 100);
        </script>
    </head>    
    ]]>
</update>
</changes>
</partial-response>
`

    static HEAD_REPLACE2 = `
<partial-response id="j_id__v_0"><changes><update id="jakarta.faces.ViewHead">
    <![CDATA[
    <head>
        <title>After Update</title>
        <meta charset="UTF-8">
        <meta name="description" content="Free Web tutorials">
        <meta name="keywords" content="HTML, CSS, JavaScript, JSF">
        <meta name="viewport" content="width=device-width, initial-scale=0.8">
        <meta name="author" content="Whoever">
        <script type="text/javascript" src="../../../xhrCore/fixtures/addedViewHead3.js"></script>
        <link rel="stylesheet" href="../../../xhrCore/fixtures/addedViewHead2.css"></link>
        <script type="text/javascript">
            setTimeout(() => document.getElementById('resource_area_1').innerHTML = 'booga', 100);
        </script>
    </head>    
    ]]>
</update>
</changes>
</partial-response>
`


    static NONCE_REPLY = `
    <partial-response><changes><update id='nonce_result'>
    <![CDATA[<script nonce='test12d3' type='text/javascript' src='http://foobaz/nonce_script.js'></script>]]>
    </update></changes></partial-response>
    `;

    static ILLEGAL_RESP = `>>>> xxxx >YYYY-!->>>`;


    static  ERROR_CHAIN_RESPOND_OK = (cnt: number): string => {
        return `<partial-response><changes><update id='form1:out1'><![CDATA[<div id="form1:out1">${cnt}</div>]]>
    </update></changes></partial-response>`;
    }

}