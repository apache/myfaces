/**
 * This class is for http based unit tests
 */
myfaces._impl.core._Runtime.singletonExtendClass("myfaces._impl._util._UnitTest", Object, {

    /**
     * Simple assert true
     *
     * @param message the assertion message
     * @param assertionOutcome the assertion outcome (true or false)
     */
    assertTrue: function(message, assertionOutcome) {
        var _Lang = myfaces._impl._util._Lang;

        if (!assertionOutcome) {
            _Lang.logError(message, "assertionOutcome:", assertionOutcome);
            throw Error(message, assertionOutcome);
        }
        _Lang.logInfo(message, "assertionOutcome:", assertionOutcome);
    },
    
    /**
     * Simple assert false
     *
     * @param message the assertion message
     * @param assertionOutcome the assertion outcome (true or false)
     */
    assertFalse: function(message, assertionOutcome) {
        var _Lang = myfaces._impl._util._Lang;

        if (assertionOutcome) {
            _Lang.logError(message, "assertionOutcome:", assertionOutcome);
            throw Error(message, assertionOutcome);
        }
        _Lang.logInfo(message, "assertionOutcome:", assertionOutcome);
    }
});