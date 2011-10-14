/*last file loaded, must restore the state of affairs*/
(function() {
    var _RT = myfaces._impl.core._Runtime;
	if(_RT._oldExtends) {
	    window._MF_CLS = _RT._oldExtends;
	}
	if(_RT._oldSingleton) {
	   window._MF_SINGLTN = _RT._oldSingleton;
	}
})();


