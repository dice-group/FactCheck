# FactCheckDemo

FactCheckDemo was generated with [Angular CLI](https://github.com/angular/angular-cli) version 1.7.3, 
Nodejs v8.9.4 and Npm v5.8.0  .

## Change Log
- Implemented Waiting (loading) UI, when user sends request to server.  
- Added Clear button with full functionality.
- Fixed duplication of labels for subject and object.
- URI label and Labels are now hidden when there are no labels for subject and object.  
- Replaced and implemented http request code with Angular http. 
- Added error handling for server requests. (Eg; if clint-end gets status code of error in reponse to request to server). 
- Also implemented request error handling using Promisses.
- Creation of ttl file (NLP) is 100% done (NLP is done). 
Now ttl file is being created when user inputs Text(S.P.O). 
Now ttl formated JSON object is being sent on both cases.
I have created ttl format from natural language input (Subject, Predicate, Object and subject/object labels). 
- Implemented local storage for front-end. Works even page is refreshed or browser is closed and reopened. Tested on (Chrome, MS Edge and Firefox). 

## Prerequisites
Before you can use Angular CLI, you must have Node.js v8.9.4 and npm 5.8.0 or higher installed on your system.
You can download the latest version of Node.js for your operating system and consult the latest installation instructions on the official Node.js [website](https://nodejs.org/en/) or You can install nodejs and npm combined from [https://www.npmjs.com/get-npm](https://www.npmjs.com/get-npm)  .

If you already have Node.js and npm installed, you can verify their version by running:

```
$ node -v # => displays your Node.js version
$ npm -v # => displays your npm version
```


## Usage

1. Clone this repository in Visual Studio Code or any other IDE of your choice.
2. Locate to the root directory of the project using command prompt.
3. run command ' ng serve '
4. localhost is up on port 4200. Now open internet browser and type [localhost://4200](localhost://4200) .
 
## Development server

Run `ng serve` for a dev server. Navigate to `http://localhost:4200/`. The app will automatically reload if you change any of the source files.

## Build

To generate source code when developement is complete, Run `ng build` to build the project to upload it to server for live experience. The build artifacts will be stored in the `dist/` directory. Use the `-prod` flag for a production build.

## Running unit tests

Run `ng test` to execute the unit tests via [Karma](https://karma-runner.github.io).

## Running end-to-end tests

Run `ng e2e` to execute the end-to-end tests via [Protractor](http://www.protractortest.org/).

## Further help

To get more help on the Angular CLI use `ng help` or go check out the [Angular CLI README](https://github.com/angular/angular-cli/blob/master/README.md).
