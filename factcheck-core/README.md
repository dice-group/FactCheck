# FactCheck
**Usage Instructions** 

To use FactCheck do the following:

 1. Clone the repository from https://github.com/dice-group/FactCheck.git
 2. Build the project  **factcheck-core**  by issuing the command `mvn clean install -DskipTests=true`
 3. FactCheck uses pre-calculated Pointwise Mutual Information of pair of subject, predicate and object of a given input triple and use it as Statistical Evidence feature. For querying these values, FactCheck needs MySQL 5.5 for query support. Set up [MySQL](https://dev.mysql.com/doc/refman/5.5/en/) environment. 
    - Create MySQL database named `dbpedia_metrics` and import the file `dbpedia_metrics.sql`(file can be accessed at https://hobbitdata.informatik.uni-leipzig.de/teaching/FactCheck/dbpedia_metrics.zip).
 5. FactCheck can be adapted to use evidences either from the Web or a reference text corpus, for example Wikipedia.
    - For querying the Web, FactCheck uses the BING API service. Please update the entry BING_API_KEY with a valid key in the    factcheck-core/target/classes/defacto.ini file.
    - Alternatively, to use reference corpus, index all the text documents on [Elasticserach](https://www.elastic.co/). If you choose to index Wikipedia, you can download pre-processed Wikipedia dumps (dated: 01 Oct-2017) from https://hobbitdata.informatik.uni-leipzig.de/teaching/FactCheck/Wikipedia_dump.zip. Assuming you have an elasticsearch instance running on localhost:9200, having an index index_name and document type doc_type, issue the command 
    "curl -XPOST localhost:9200/index_name/doc_type/_bulk --data-binary  @link/to/jsonfile"
 for bulk indexing. Note that the command is to index single json file.
 5. Download the data folder (model files, resources etc) from https://hobbitdata.informatik.uni-leipzig.de/teaching/FactCheck/data.zip
 4. Update the following entries in the factcheck-core/target/classes/defacto.ini
    - set the entry [eval] : data-directory to map to `data` folder from **step 5**.
    - set the entry [evidence] : WORDNET_DICTIONARY to map to `data/wordnet/dict` from **step 5**.
      (Unfortunately, the dictionary provided (WordNet 3.0) does not support Windows. Windows users can download [WordNet 2.1](https://wordnet.princeton.edu/wordnet/download/current-version/). Make sure to set the above entry to map to `dict` folder)
    - set the entry [mysql] : PASSWORD to the password for root user you chose during installation in **step 3**.
    - set the entries under [elasticserach]
5. Run the file `DefactoDemo.java`
