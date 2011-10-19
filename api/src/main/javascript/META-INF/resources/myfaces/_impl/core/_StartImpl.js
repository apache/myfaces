/*we cannot privatize with a global function hence we store the values away for the init part*/
(function() {
    //some mobile browsers do not have a window object
    var target = window || document.body;
    var _RT = myfaces._impl.core._Runtime;
    var impl = "myfaces._impl.";
    var params = {_MF_CLS: _RT.extendClass,
        _MF_SINGLTN: _RT.singletonExtendClass,
        _PFX_UTIL: impl+"_util.",
        _PFX_CORE:impl+"core.",
        _PFX_XHR: impl+"xhrCore.",
        _PFX_I18N: impl+"i18n."};

    for (var key in params) {
        _RT[key] = target[key];
        target[key] = params[key];
    }
})();
