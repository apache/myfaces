/**
 * This class is for http based unit tests
 */
myfaces._impl.core._Runtime.singletonExtendClass("myfaces._impl._util._UnitTest", Object, {
    /**
     *
     */
    assertTrue: function(message, assertionOutcome) {
        var _Lang = myfaces._impl._util._Lang;

        if (!assertionOutcome) {
            _Lang.logError(message, "assertionOutcome:", assertionOutcome);
            throw Error(message, assertionOutcome);
        }
        _Lang.logInfo(message, "assertionOutcome:", assertionOutcome);
    },
    assertFalse: function(message, assertionOutcome) {
        var _Lang = myfaces._impl._util._Lang;

        if (assertionOutcome) {
            _Lang.logError(message, "assertionOutcome:", assertionOutcome);
            throw Error(message, assertionOutcome);
        }
        _Lang.logInfo(message, "assertionOutcome:", assertionOutcome);
    }
});