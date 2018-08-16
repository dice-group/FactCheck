hoek-boom  [![Build Status](https://travis-ci.org/mrlannigan/hoek-boom.svg)](https://travis-ci.org/mrlannigan/hoek-boom) [![Dependency Status](https://david-dm.org/mrlannigan/hoek-boom.svg)](https://david-dm.org/mrlannigan/hoek-boom) [![devDependency Status](https://david-dm.org/mrlannigan/hoek-boom/dev-status.svg)](https://david-dm.org/mrlannigan/hoek-boom#info=devDependencies)
=========

![hoek Logo](https://raw.github.com/hapijs/hoek/master/images/hoek.png)

+

![boom Logo](https://raw.github.com/hapijs/boom/master/images/boom.png)

\+ a few additions

# Introduction

This library extends [Hoek](https://github.com/hapijs/hoek) and exposes [Boom](https://github.com/hapijs/boom).

# Additional Features

On top of the already exposed Hoek module API.

### assertBoom( condition, message, errorName )

```js
var a = 1, b = 2;

HoekBoom.assertBoom(a === b, 'a should equal b', 'expectationFailed');  // ABORT: a should equal b, throw boom error

// other uses ...
HoekBoom.assertBoom(); // throws Error
HoekBoom.assertBoom(a === b); // throws Error
HoekBoom.assertBoom(a === b, new Error('example')); // throws given error object
HoekBoom.assertBoom(a === b, 'badRequest'); // throws Boom.badRequest()
HoekBoom.assertBoom(a === b, 'some message'); // throws Error('some message')
HoekBoom.assertBoom(a === b, 'some', 'message', new Error('here'), 'for all', {test: 'lolz'});  // throws Error('some message here for all ' + JSON.stringify({test: 'lolz'}))
HoekBoom.assertBoom(a === b, 'some', 'message'); // throws Error('some message')
HoekBoom.assertBoom(a === b, 'check the docs', 'badRequest'); // throws Boom.badRequest('check the docs')
HoekBoom.assertBoom(a === b, 'check the docs', {username: 'johnsmith'}, 'badRequest'); // throws Boom.badRequest('check the docs', {username: 'johnsmith'})
HoekBoom.assertBoom(a === b, 'invalid password', 'sample', {username: 'johnsmith'}, 'unauthorized'); // throws Boom.unauthorized('invalid password', 'sample', {username: 'johnsmith'})
```

### Boom

```js
var Boom = HoekBoom.Boom;

reply(Boom.notFound('stuff wasn\'t found here'));
```