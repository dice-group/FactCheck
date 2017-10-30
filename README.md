# FactCheck
**Usage Instructions** 

To use FactCheck do the following:

 1. Clone the repository from https://github.com/dice-group/FactCheck.git
 2.  Build the project  **defacto-core**  by issuing the command `mvn clean install`
 3. Additionally, FactCheck needs MySQL 5.5 for query support. Set up [MySQL](https://dev.mysql.com/doc/refman/5.5/en/) environment. 
    - Create MySQL database named `dbpedia_metrics` and import the file `dbpedia_metrics.sql`(file can be found in the `FactCheck/data_for_installation` folder).
 4. Update the following entries in the defacto-core/target/classes/defacto.ini
    - set the entry [eval] : data-directory to map to `FactCheck/data` folder.
    - set the entry [evidence] : WORDNET_DICTIONARY to map to `FactCheck/data/wordnet/dict`
      (Unfortunately, the dictionary provided (WordNet 3.0) does not support Windows. Windows users can download [WordNet 2.1](https://wordnet.princeton.edu/wordnet/download/current-version/). Make sure to set the above entry to map to `dict` folder)
    - set the entry [mysql] : PASSWORD to the password for root user you chose during installation in **step 3**.
5. Run the file `DefactoDemo.java`
