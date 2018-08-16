URI
===
[![NPM version][npm-image]][npm-url] [![Build Status][travis-image]][travis-url] [![Coverage Status][coveralls-image]][coveralls-url] [![Dependencies][dependencies-image]][dependencies-url]

> Validates if a value is a [URI](http://en.wikipedia.org/wiki/URI_scheme).


## Installation

``` bash
$ npm install validate.io-uri
```

For use in the browser, use [browserify](https://github.com/substack/node-browserify).


## Usage

``` javascript
var isURI = require( 'validate.io-uri' );
```

#### isURI( value )

Validates if a `value` is a [URI](http://en.wikipedia.org/wiki/URI_scheme).

``` javascript
var value = 'http://google.com';

var bool = isURI( value );
// returns true
```

__Note__: for non-string input `values`, the function returns `false`.


## Examples

``` javascript
var isURI = require( 'validate.io-uri' );

// VALID //

var bool = isURI( 'http://google.com' );
console.log( bool );
// returns true

bool = isURI( 'http://localhost/' );
console.log( bool );
// returns true

bool = isURI( 'http://example.w3.org/path%20with%20spaces.html' );
console.log( bool );
// returns true

bool = isURI( 'http://example.w3.org/%20' );
console.log( bool );
// returns true

bool = isURI( 'ftp://ftp.is.co.za/rfc/rfc1808.txt' );
console.log( bool );
// returns true

bool = isURI( 'ftp://ftp.is.co.za/../../../rfc/rfc1808.txt' );
console.log( bool );
// returns true

bool = isURI( 'http://www.ietf.org/rfc/rfc2396.txt' );
console.log( bool );
// returns true

bool = isURI( 'ldap://[2001:db8::7]/c=GB?objectClass?one' );
console.log( bool );
// returns true

bool = isURI( 'mailto:John.Doe@example.com' );
console.log( bool );
// returns true

bool = isURI( 'news:comp.infosystems.www.servers.unix' );
console.log( bool );
// returns true

bool = isURI( 'tel:+1-816-555-1212' );
console.log( bool );
// returns true

bool = isURI( 'telnet://192.0.2.16:80/' );
console.log( bool );
// returns true

bool = isURI( 'urn:oasis:names:specification:docbook:dtd:xml:4.1.2' );
console.log( bool );
// returns true


// INVALID //

// No scheme:
bool = isURI( '' );
console.log( bool );
// returns false

// No scheme:
bool = isURI( 'foo' );
console.log( bool );
// returns false

// No scheme:
bool = isURI( 'foo@bar' );
console.log( bool );
// returns false

// No scheme:
bool = isURI( '://foo/' );
console.log( bool );
// returns false

// Illegal characters:
bool = isURI( 'http://<foo>' );
console.log( bool );
// returns false

// Invalid path:
bool = isURI( 'http:////foo.html' );
console.log( bool );
// returns false

// Incomplete hex escapes...
bool = isURI( 'http://example.w3.org/%a' );
console.log( bool );
// returns false

bool = isURI( 'http://example.w3.org/%a/foo' );
console.log( bool );
// returns false

bool = isURI( 'http://example.w3.org/%at' );
console.log( bool );
// returns false
```

To run the example code from the top-level application directory,

``` bash
$ node ./examples/index.js
```


## Notes

*	See [RFC 3986](http://tools.ietf.org/html/rfc3986) and [Wikipedia](http://en.wikipedia.org/wiki/URI_scheme) for information regarding the URI scheme.
*	This module uses the same test URIs as [valid-url](https://github.com/ogt/valid-url) (a clone of a corresponding Perl [package](http://anonscm.debian.org/cgit/users/dom/libdata-validate-uri-perl.git/tree/lib/Data/Validate/URI.pm)), which are based on examples from [RFC 3986](http://tools.ietf.org/html/rfc3986).


## Tests

### Unit

Unit tests use the [Mocha](http://mochajs.org) test framework with [Chai](http://chaijs.com) assertions. To run the tests, execute the following command in the top-level application directory:

``` bash
$ make test
```

All new feature development should have corresponding unit tests to validate correct functionality.


### Test Coverage

This repository uses [Istanbul](https://github.com/gotwarlost/istanbul) as its code coverage tool. To generate a test coverage report, execute the following command in the top-level application directory:

``` bash
$ make test-cov
```

Istanbul creates a `./reports/coverage` directory. To access an HTML version of the report,

``` bash
$ make view-cov
```


---
## License

[MIT license](http://opensource.org/licenses/MIT). 


## Copyright

Copyright &copy; 2015. Athan Reines.


[npm-image]: http://img.shields.io/npm/v/validate.io-uri.svg
[npm-url]: https://npmjs.org/package/validate.io-uri

[travis-image]: http://img.shields.io/travis/validate-io/uri/master.svg
[travis-url]: https://travis-ci.org/validate-io/uri

[coveralls-image]: https://img.shields.io/coveralls/validate-io/uri/master.svg
[coveralls-url]: https://coveralls.io/r/validate-io/uri?branch=master

[dependencies-image]: http://img.shields.io/david/validate-io/uri.svg
[dependencies-url]: https://david-dm.org/validate-io/uri

[dev-dependencies-image]: http://img.shields.io/david/dev/validate-io/uri.svg
[dev-dependencies-url]: https://david-dm.org/dev/validate-io/uri

[github-issues-image]: http://img.shields.io/github/issues/validate-io/uri.svg
[github-issues-url]: https://github.com/validate-io/uri/issues
