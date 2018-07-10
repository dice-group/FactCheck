# factcheck-api
[FactCheck](https://github.com/dice-group/FactCheck) is an algorithm that validates facts by finding evidences on the web to approve or deny a query.

[factcheck-api](https://github.com/danishahmed92/factcheck-api) is a Spring RESTful web service for Factcheck which requires an object, predicate and subject to be specified in the JSON request.

 A task ID - a number that identifies the request is also required. 
 
 The API returns the total score as well as proof sentences and their corresponding scores.


# Usage Instructions

## How to Install
1. Make sure that Oracle Java 1.8 (or higher) is installed (java -version). Or install it by the sudo add-apt-repository ppa:webupd8team/java && sudo apt-get update && sudo apt-get install oracle-java8-installer -y.
2. Clone this repository ( git clone https://github.com/danishahmed92/factcheck-api.git )
3. Add the factcheck jar as a Library.
For IntelliJ IDEA users:
File -> Project Structure -> Libraries -> Select New Project Library 

## How to run
1. Run the Application.java in IntelliJ.
   Requests can be posted to http://localhost:8080/api/execTask/
2. To perform health check:
   check in browser http://localhost:8080/api/default

