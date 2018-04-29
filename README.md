# factcheck-api
[FactCheck](https://github.com/dice-group/FactCheck) is algorithm used for validation of fact based on the muliple evidences found on the web and results a confidence score.
[factcheck-api] (https://github.com/danishahmed92/factcheck-api) is a RESTful web service which can be used to query the facts and obtain results.
The JSON requests containing Turtle format query along with TaskID can be posted to http://localhost:8080/api/execTask/
For posting requests [factcheck-demo](https://github.com/Fahad-Anwar/factcheck-demo) can be used.


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

