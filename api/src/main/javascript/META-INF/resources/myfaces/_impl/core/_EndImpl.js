/*last file loaded, must restore the state of affairs*/
(function() {
    //some mobile browsers do not have a window object
    var target = window || document;
    var _RT = myfaces._impl.core._Runtime;
    var resetAbbreviation = function (name) {
        (!!_RT[name]) ?
                target[name] = _RT[name] : null;
    };
    myfaces._impl._util._Lang.arrForEach(["_MF_CLS",
                           "_MF_SINGLTN",
                           "_MF_OBJECT",
                           "_PFX_UTIL",
                           "_PFX_XHR",
                           "_PFX_CORE",
                           "_PFX_I18N"], resetAbbreviation);
})();


