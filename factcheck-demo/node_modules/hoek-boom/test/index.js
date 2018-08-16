'use strict';

var Code = require('code'),
    HoekBoom = require('../'),
    Hoek = require('hoek'),
    Boom = require('boom'),
    Lab = require('lab');


var lab = exports.lab = Lab.script(),
    describe = lab.describe,
    it = lab.it,
    expect = Code.expect;


it('should expose Boom reference', function (done) {
    expect(HoekBoom.Boom).to.deep.equal(Boom);

    done();
});

describe('HoekBoom()', function () {
    Object.keys(Hoek).forEach(function (funcName) {
        it('should export reference to Hoek.' + funcName, function (done) {
            expect(HoekBoom[funcName]).to.deep.equal(Hoek[funcName]);

            done();
        });
    });
});

describe('assertBoom()', function () {

    // assertBoom(a === b)
    it('good condition', function (done) {
        var good = function () {
            return HoekBoom.assertBoom(1 === 1);
        };

        expect(good).to.not.throw();
        expect(good()).to.be.undefined();

        done();
    });

    // assertBoom()
    it('no arguments', function (done) {
        expect(function () {
            HoekBoom.assertBoom();
        }).to.throw(Error, 'Condition failed');

        done();
    });

    // assertBoom(a === b)
    it('only bad condition', function (done) {
        expect(function () {
            HoekBoom.assertBoom(1 === 0);
        }).to.throw(Error, 'Condition failed');

        done();
    });

    // assertBoom(a === b, new Error('example'))
    it('bad condition with error object as message', function (done) {
        expect(function () {
            HoekBoom.assertBoom(1 === 0, new Error('example'));
        }).to.throw(Error, 'example');

        done();
    });

    // assertBoom(a === b, 'badRequest')
    it('bad condition with only boom error name', function (done) {
        expect(function () {
            try {
                HoekBoom.assertBoom(1 === 0, 'badRequest');
            } catch (e) {
                expect(e.isBoom).to.be.true();
                throw e;
            }
        }).to.throw(Error);

        done();
    });

    // assertBoom(a === b, 'some message')
    it('bad condition with a single string as the message', function (done) {
        expect(function () {
            HoekBoom.assertBoom(1 === 0, 'some message');
        }).to.throw(Error, 'some message');

        done();
    });

    // assertBoom(a === b, 'some', 'message', new Error('here'), 'for all', {test: 'lolz'})
    it('bad condition with many mixed objects', function (done) {
        expect(function () {
            HoekBoom.assertBoom(1 === 0, 'some', 'message', new Error('here'), 'for all', {test: 'lolz'});
        }).to.throw(Error, 'some message here for all ' + JSON.stringify({test: 'lolz'}));

        done();
    });

    // assertBoom(a === b, 'some', 'message')
    it('bad condition with many strings', function (done) {
        expect(function () {
            HoekBoom.assertBoom(1 === 0, 'some', 'message');
        }).to.throw(Error, 'some message');

        done();
    });

    // assertBoom(a === b, '', '', '', '')
    it('bad condition with many empty strings', function (done) {
        expect(function () {
            HoekBoom.assertBoom(1 === 0, '', '', '', '');
        }).to.throw(Error, 'Unknown error');

        done();
    });

    // assertBoom(a === b, '', '', '', new Error(''))
    it('bad condition with many empty strings ending in empty error object', function (done) {
        expect(function () {
            HoekBoom.assertBoom(1 === 0, '', '', '', new Error(''));
        }).to.throw(Error, 'Unknown error');

        done();
    });

    // assertBoom(a === b, {test:'lolz'})
    it('bad condition with object as message', function (done) {
        expect(function () {
            HoekBoom.assertBoom(1 === 0, {test:'lolz'});
        }).to.throw(Error, JSON.stringify({test:'lolz'}));

        done();
    });

    // assertBoom(a === b, 'check the docs', 'badRequest')
    it('bad condition with boom error name and message', function (done) {
        expect(function () {
            try {
                HoekBoom.assertBoom(1 === 0, 'check the docs', 'badRequest');
            } catch (e) {
                expect(e.isBoom).to.be.true();
                throw e;
            }
        }).to.throw(Error, 'check the docs');

        done();
    });

    // assertBoom(a === b, 'check the docs', 'create')
    it('bad condition with blacklisted boom error name and message', function (done) {
        expect(function () {
            try {
                HoekBoom.assertBoom(1 === 0, 'check the docs', 'create');
            } catch (e) {
                expect(e.isBoom).to.be.undefined();
                throw e;
            }
        }).to.throw(Error, 'check the docs create');

        done();
    });

    // assertBoom(a === b, 'check the docs', {username: 'johnsmith'}, 'badRequest')
    it('bad condition with boom error name, data, and message', function (done) {
        var data = {username: 'johnsmith'};
        expect(function () {
            try {
                HoekBoom.assertBoom(1 === 0, 'check the docs', data, 'badRequest');
            } catch (e) {
                expect(e.isBoom).to.be.true();
                expect(e.data).to.deep.equal(data);
                throw e;
            }
        }).to.throw(Error, 'check the docs');

        done();
    });

    // assertBoom(a === b, 'invalid password', 'sample', {username: 'johnsmith'}, 'unauthorized')
    it('bad condition with boom error with 3 arguments', function (done) {
        var data = {username: 'johnsmith'};
        expect(function () {
            try {
                HoekBoom.assertBoom(1 === 0, 'invalid password', 'sample', data, 'unauthorized');
            } catch (e) {
                expect(e.isBoom).to.be.true();
                expect(e.data).to.be.null();
                expect(e.output.headers['WWW-Authenticate']).to.include(['sample', 'username', 'johnsmith']);
                throw e;
            }
        }).to.throw(Error, 'invalid password');

        done();
    });

    // assertBoom(a === b, 'invalid password', 'sample', {username: 'johnsmith'}, 'noop', 'unauthorized')
    it('bad condition with boom error with 4 arguments', function (done) {
        var data = {username: 'johnsmith'};
        expect(function () {
            try {
                HoekBoom.assertBoom(1 === 0, 'invalid password', 'sample', data, 'noop', 'unauthorized');
            } catch (e) {
                expect(e.isBoom).to.be.true();
                expect(e.data).to.be.null();
                expect(e.output.headers['WWW-Authenticate']).to.include(['sample', 'username', 'johnsmith']);
                throw e;
            }
        }).to.throw(Error, 'invalid password');

        done();
    });
});
