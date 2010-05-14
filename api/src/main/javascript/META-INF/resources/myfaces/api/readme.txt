ongoing work in progress not finished yet
the final build will have one single jsf.js file
if you want to include the files manually follow following order:

<script type="text/javascript" src="./myfaces/_impl/core/_Runtime.js"></script>
<script type = "text/javascript" src = "myfaces/_impl/_util/_Lang.js"></script>
<script type = "text/javascript" src = "./myfaces/_impl/_util/_ListenerQueue.js"></script>
<script type = "text/javascript" src = "./myfaces/_impl/_util/_Dom.js"></script>
<script type = "text/javascript" src = "./myfaces/_impl/_util/_HtmlStripper.js"></script>

<script type = "text/javascript" src = "./myfaces/_impl/xhrCore/_Exception.js"></script>
<script type = "text/javascript" src = "./myfaces/_impl/xhrCore/_AjaxUtils.js"></script>
<script type = "text/javascript" src = "./myfaces/_impl/xhrCore/_AjaxRequestQueue.js"></script>
<script type = "text/javascript" src = "./myfaces/_impl/xhrCore/_AjaxRequest.js"></script>
<script type = "text/javascript" src = "./myfaces/_impl/xhrCore/_AjaxResponse.js"></script>
<script type = "text/javascript" src = "./myfaces/_impl/xhrCore/_xhrCoreAdapter.js"></script>
<script type = "text/javascript" src = "./myfaces/_impl/core/jsf_impl.js"></script>
<script type = "text/javascript" src = "./myfaces/api/jsf.js"></script>
