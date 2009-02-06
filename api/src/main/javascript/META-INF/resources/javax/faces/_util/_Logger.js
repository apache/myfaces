/**
 * Generical simplified browser independend logger
 * on browsers which offer a console object
 * it logs into the
 *
 * It follows a factory pattern so that it can be reroutet
 * to other debugging divs! Loggers are cached internally
 * so using the factory should cause a limited performance
 * overhead
 *
 * The output is either to the console if present
 * or to a debugging div
 * or to the outer document if nothing is present
 *
 * var logger = myfaces._Logger.getInstance([loggingDivId]);
 * log.error("myError");
 * log.debug("from me", "this is my error");
 * etc...
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

    /*global logging configuration*/
    myfaces._Logger.LOG_CONSOLE     = true;
    myfaces._Logger.LOG_DIV         = true;
    myfaces._Logger.LOG_DOCUMENT    = true;
    myfaces._Logger.LOG_ALERT       = false;

    myfaces._Logger.getInstance = function(targetDiv) {
        var retVal = null;
        var targetDivId = null;
        if('undefined' != typeof(targetDiv) && null != targetDiv) {
            targetDivId = myfaces._JSF2Utils.isString(targetDiv) ? targetDiv : targetDiv.id;
            retVal = myfaces._Logger._loggerIdx[targetDivId];
        } else {
            retVal = myfaces._Logger._loggerIdx["myfaces._Logger.standardLogger"];
        }
        if('undefined' != typeof retVal || null != retVal) {
            return retVal;
        }

        retVal = new myfaces._Logger();

        if('undefined' != typeof(targetDiv) && null != targetDiv) {
            retVal.setTargetDiv(targetDiv);
            myfaces._Logger._loggerIdx[targetDivId] = retVal;
        } else {
            myfaces._Logger._loggerIdx["myfaces._Logger.standardLogger"] = retVal;
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

    /**
     * main logging algorithm
     * we either root into the console
     * or into the dom
     * or into the document
     * or into alerts depending on whehther one or the other
     * exists and the configuration!
     */
    myfaces._Logger.prototype._logIt = function(/*String*/logType,/*caller arguments*/ varArgs) {
        //TODO map the caller...
        if(this._hasConsole) {
            if(myfaces._Logger.LOG_CONSOLE) {
                //TODO find out how to reference the proper line number etc...
                console[logType.toLowerCase()](myfaces._JSF2Utils.arrayToString( varArgs ));
            }
        } else if(null != this._targetDiv) {
            if(myfaces._Logger.LOG_DIV) {
                this._targetDiv.innerHTML = this._targetDiv.innerHTML + "<br /> ["+logType+"] : "+ myfaces._JSF2Utils.arrayToString(varArgs, " ");
            }
        } else { /*in case a target fails we use document.write*/
            if(myfaces._Logger.LOG_DOCUMENT) {
                document.write("<br /> ["+logType+"] : " + myfaces._JSF2Utils.arrayToString(varArgs, " "));
            }
            if(myfaces._Logger.LOG_ALERT) {
                alert("<br /> ["+logType+"] : " + myfaces._JSF2Utils.arrayToString(varArgs, " "));
            }

        }
    }

    myfaces._Logger.prototype.debug = function(/*Object*/varArgs/*,...*/) {
        if(this._logLevel > this.LOG_LEVEL_DEBUG) return;

        this._logIt("DEBUG", arguments);
    };
    myfaces._Logger.prototype.error = function(varArgs/*,...*/) {
        if(this._logLevel > this.LOG_LEVEL_ERROR) return;
        this._logIt("ERROR", arguments);
    };
    myfaces._Logger.prototype.warn = function(varArgs/*,...*/) {
        if(this._logLevel > this.LOG_LEVEL_WARN) return;
        this._logIt("WARN", arguments);
    };
    myfaces._Logger.prototype.info = function(/*Object*/varArgs/*,...*/) {
        if(this._logLevel > this.LOG_LEVEL_WARN) return;
        this._logIt("INFO", arguments);
    };
}