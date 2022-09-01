run mvn clean install
then run the generated jar file in target folder 
for more help run with the "h" key
```
java -jar ./target/factcheck-trainer-0.0.1-SNAPSHOT.jar h

```
use 'train' key for train the classifier
```
java -jar ./target/factcheck-trainer-0.0.1-SNAPSHOT.jar train [full path for save classifier] [full path for save evaluation] [full path to training file] [classifier one of these options: MultilayerPerceptron, J48, LibSVM] [comma separated index of the training data for delete like this : 29,27,5]
```
