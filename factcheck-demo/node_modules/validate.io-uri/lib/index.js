/**
*
*	VALIDATE: uri
*
*
*	DESCRIPTION:
*		- Validates if a value is a URI.
*
*
*	NOTES:
*		[1]
*
*
*	TODO:
*		[1]
*
*
*	LICENSE:
*		MIT
*
*	Copyright (c) 2015. Athan Reines.
*
*
*	AUTHOR:
*		Athan Reines. kgryte@gmail.com. 2015.
*
*/

'use strict';

// MODULES //

var isString = require( 'validate.io-string' );


// REGEXP //

var URI,
	ILLEGALS,
	HEX1,
	HEX2,
	PATH,
	SCHEME;

// RFC 3986:
URI = /(?:([^:\/?#]+):)?(?:\/\/([^\/?#]*))?([^?#]*)(?:\?[^#]*)?(?:#.*)?/;
/**
	REGEXP

	<scheme name> : <hierarchical part > [ ? <query> ] [ # <fragment> ]

	(?:([^:\/?#]+):)
		-	match the scheme, including the ':', but only capture the scheme name

	?
		-	match the scheme 0 or 1 times

	(?:\/\/([^\/?#]*))
		-	match the hierarchical part which is everything which is not a '/', '#', or '?', but only capture whatever comes after the '//'

	?
		-	match the hierarchical part 0 or 1 times

	([^?#]*)
		-	capture everything (the path) until meeting a '?' or '#'

	(?:\?[^#]*)
		-	match, but don't capture, a query

	?
		-	match the query 0 or 1 times

	(?:#.*)
		-	match, but don't capture, a fragment

	?
		-	match the fragment 0 or 1 times
*/

// Illegal characters (anything which is not in between the square brackets):
ILLEGALS = /[^a-z0-9\:\/\?\#\[\]\@\!\$\&\'\(\)\*\+\,\;\=\.\-\_\~\%]/i;

// Incomplete HEX escapes:
HEX1 = /%[^0-9a-f]/i;
HEX2 = /%[0-9a-f](:?[^0-9a-f]|$)/i;

// If authority is not present, path must not begin with '//'
PATH = /^\/\//;

// Scheme must begin with a letter, then consist of letters, digits, '+', '.', or '-' => e.g., 'http', 'https', 'ftp'
SCHEME = /^[a-z][a-z0-9\+\-\.]*$/;


// URI //

/**
* FUNCTION: uri( value )
*	Validates if a value is a URI.
*
* @param {*} value - value to validate
* @returns {Boolean} boolean indicating if a value is a URI
*/
function uri( val ) {
	var parts,
		scheme,
		authority,
		path;
	if ( !isString( val ) ) {
		return false;
	}
	// [1] Check for illegal characters:
	if ( ILLEGALS.test( val ) ) {
		return false;
	}
	// [2] Check for incomplete HEX escapes:
	if ( HEX1.test( val ) || HEX2.test( val ) ) {
		return false;
	}
	// [3] Split the string into various URI components:
	parts = val.match( URI );
	scheme = parts[ 1 ];
	authority = parts[ 2 ];
	path = parts[ 3 ];

	// [4] Scheme is required and must be valid:
	if ( !scheme || !scheme.length || !SCHEME.test( scheme.toLowerCase() ) ) {
		return false;
	}
	// [5] If authority is not present, path must not begin with a '//':
	if ( !authority && PATH.test( path ) ) {
		return false;
	}
	return true;
} // end FUNCTION uri()


// EXPORTS //

module.exports = uri;
