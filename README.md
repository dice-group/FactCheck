# FactCheck


FactCheck is an algorithm for validating statements by finding confirming sources for it on the web. It takes a statement (such as "Jamaica Inn was directed by Alfred Hitchcock") as input and then tries to find evidence for the truth of that statement by searching for information in the web. In contrast to typical search engines, it does not just search for textual occurences of parts of the statement, but tries to find web pages, which contain the statement phrased in natural language. It presents the user with a confidence score for the input statement as well as a set of excerpts of relevant web pages, which allows the user to manually look at the evidence.

The project has three components:

1. [factcheck-core](https://github.com/danishahmed92/FactCheck/tree/master/factcheck-core): the main functionalities and libraries for the algorithm
2. [factcheck-service](https://github.com/danishahmed92/FactCheck/tree/master/factcheck-service): web application built using the Spring framework. The application currently supports requests from factcheck-demo as well as [factcheck-benchmark](https://github.com/hobbit-project/Fact-Checking-Benchmark).
3. [factcheck-demo](https://github.com/danishahmed92/FactCheck/tree/master/factcheck-demo): Angular application that allows users to submit queries as a .ttl file or by providing the subject, predicate and object of the query.

## Prerequisites
1. Nodejs
2. Npm
3. Java 1.8 (minimum)
## How to run
1. Goto **factcheck-demo** directory using command prompt and run following commands.
``` 
npm install 
ng build -prod
``` 
2. Set data-directory path in defacto.ini files
3. goto **factcheck** directory using command prompt and 
run following commands.
``` 
mvn clean install 
mvn spring-boot:run
``` 
Open [http://localhost:8080](http://localhost:8080) in your browser. 