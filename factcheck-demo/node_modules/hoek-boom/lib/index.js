'use strict';

var Hoek = require('hoek'),
    Boom = require('boom'),
    BoomHelpers = ['wrap', 'create'],
    HoekKeys = Object.keys(Hoek),
    BoomKeys = Object.keys(Boom);


// apply Hoek functions to exports
for (var i = 0, il = HoekKeys.length; i < il; ++i) {
    exports[HoekKeys[i]] = Hoek[HoekKeys[i]];
}

// expose Boom
exports.Boom = Boom;

// assert that throws boom error if condition is not true
exports.assertBoom = function (condition /* message, boomarg2, boomarg3, errorName */) {

    if (condition) {
        return;
    }

    var argsLength = arguments.length << 0;

    // assertBoom(a === b)
    // assertBoom()
    if (argsLength <= 1) {
        throw new Error('Condition failed');
    }

    // assertBoom(a === b, new Error('example'))
    if (argsLength === 2 && arguments[1] instanceof Error) {
        throw arguments[1];
    }

    var errorName = arguments[argsLength - 1],
        isBoomErrorName = typeof errorName === 'string' && BoomHelpers.indexOf(errorName) === -1 && BoomKeys.indexOf(errorName) >= 0;

    if (argsLength === 2) {
        // assertBoom(a === b, 'badRequest')
        if (isBoomErrorName) {
            throw Boom[errorName]();
        }
        // assertBoom(a === b, 'some message')
        else {
            throw new Error(typeof arguments[1] === 'string' ? arguments[1] : Hoek.stringify(arguments[1]));
        }
    }

    // assertBoom(a === b, 'some', 'message', new Error('here'), 'for all')
    // assertBoom(a === b, 'some', 'message')
    if (!isBoomErrorName && argsLength >= 3) {
        // last arg isn't boom error, so concat arguments and rethrow

        // taken from Hoek's assert
        var msgs = [];
        for (var i = 1, il = argsLength; i < il; ++i) {
            if (arguments[i] !== '') {
                msgs.push(arguments[i]);
            }
        }

        msgs = msgs.map(function (msg) {
            return typeof msg === 'string' ? msg : msg instanceof Error ? msg.message : Hoek.stringify(msg);
        });

        throw new Error(msgs.join(' ') || 'Unknown error');
    }

    // assertBoom(a === b, 'check the docs', 'badRequest')
    if (argsLength === 3) {
        throw Boom[errorName](arguments[1]);
    }

    // assertBoom(a === b, 'check the docs', {username: 'johnsmith'}, 'badRequest')
    if (argsLength === 4) {
        throw Boom[errorName](arguments[1], arguments[2]);
    }

    // assertBoom(a === b, 'invalid password', 'sample', {username: 'johnsmith'}, 'unauthorized')
    throw Boom[errorName](arguments[1], arguments[2], arguments[3]);
};
