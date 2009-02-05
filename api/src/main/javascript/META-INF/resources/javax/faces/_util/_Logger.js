/**
 * Generical simplified browser independend logger
 * on browsers which offer a console object
 * it logs into the
 *
 * It follows a factory pattern so that it can be reroutet
 * to other debugging divs!
 *
 * The output is either to the console if present
 * or to a debugging div
 * or to the outer document if nothing is present
 */

_reserveMyfaces();

if(!myfaces._JSF2Utils.exists(myfaces, "_Logger")) {
    myfaces._Logger = function() {
        var targetDiv = document.getElementById("log_console");
        this._targetDiv = targetDiv;/*null or a valid element*/
        this._logLevel  = this.LOG_LEVEL_ALL;
        this._hasConsole = ('undefined' != typeof console && null != typeof console);
        
    };
    myfaces._Logger._loggerIdx = {};

    myfaces._Logger.prototype.LOG_LEVEL_ALL      = 1;
    myfaces._Logger.prototype.LOG_LEVEL_DEBUG    = 2;
    myfaces._Logger.prototype.LOG_LEVEL_ERROR    = 3;
    myfaces._Logger.prototype.LOG_LEVEL_WARN     = 4;
    myfaces._Logger.prototype.LOG_LEVEL_INFO     = 5;
    myfaces._Logger.prototype.LOG_LEVEL_OFF      = 6;

    myfaces._Logger.getInstance = function(targetDiv) {
        var retVal = null;
        var targetDivId = null;
        if('undefined' != typeof(targetDiv) && null != targetDiv) {
            targetDivId = myfaces._JSF2Utils.isString(targetDiv) ? targetDiv : targetDiv.id;
            retVal = myfaces._Logger._loggerIdx[targetDivId];
            if('undefined' != typeof retVal || null != retVal) {
                return retVal;
            }
        }
        retVal = new myfaces._Logger();

        if('undefined' != typeof(targetDiv) && null != targetDiv) {
            retVal.setTargetDiv(targetDiv);
            myfaces._Logger._loggerIdx[targetDivId] = retVal;
        }
        return retVal;
    };

    myfaces._Logger.prototype._setLogLevel = function(/*int*/ logLevel) {
        this._logLevel = logLevel;
    };

    myfaces._Logger.prototype._setTargetDiv = function(/*String*/ targetDiv) {
        targetDiv = document.getElementById(targetDiv);
        this._targetDiv = targetDiv;/*null or a valid element*/
    };

    myfaces._Logger.prototype.debug = function(/*Object*/varArgs/*,...*/) {
        if(this._logLevel > this.LOG_LEVEL_DEBUG) return;

        if(this._hasConsole) {
            console.debug(varArgs)
        } else if(null != this._targetDiv) {
            this._targetDiv.innerHTML = this._targetDiv.innerHTML + "<br /> [DEBUG] : "+ myfaces._JSF2Utils.arrayToString(arguments, " ");
        } else { /*in case a target fails we use document.write*/
            document.write("<br /> [DEBUG] : " + myfaces._JSF2Utils.arrayToString(arguments, " "));
        }
    };
    myfaces._Logger.prototype.error = function(varArgs/*,...*/) {
        if(this._logLevel > this.LOG_LEVEL_ERROR) return;
        if(this._hasConsole) {
            console.error(varArgs)
        } else if(null != this._targetDiv) {
            this._targetDiv.innerHTML = this._targetDiv.innerHTML + "<br /> [ERROR] : " + myfaces._JSF2Utils.arrayToString(arguments, " ");
        } else { /*in case a target fails we use document.write*/
            document.write("<br /> [ERROR] : "+ myfaces._JSF2Utils.arrayToString(arguments, " "));
        }
    };
    myfaces._Logger.prototype.warn = function(varArgs/*,...*/) {
        if(this._logLevel > this.LOG_LEVEL_WARN) return;
        if(this._hasConsole) {
            console.warn(varArgs)
        } else if(null != this._targetDiv) {
            this._targetDiv.innerHTML = this._targetDiv.innerHTML + "<br /> [WARN] : " + myfaces._JSF2Utils.arrayToString(arguments, " ");
        } else { /*in case a target fails we use document.write*/
            document.write("<br /> [WARN] : "+ myfaces._JSF2Utils.arrayToString(arguments, " "));
        }
    };
    myfaces._Logger.prototype.info = function(/*Object*/varArgs/*,...*/) {
        if(this._logLevel > this.LOG_LEVEL_WARN) return;
        if(this._hasConsole) {
            console.info(varArgs)
        } else if(null != this._targetDiv) {
            this._targetDiv.innerHTML = this._targetDiv.innerHTML + "<br /> [INFO] : "+ myfaces._JSF2Utils.arrayToString(arguments, " ");
        } else { /*in case a target fails we use document.write*/
            document.write("<br /> [INFO] : "+ myfaces._JSF2Utils.arrayToString(arguments, " "));
        }
    };
}