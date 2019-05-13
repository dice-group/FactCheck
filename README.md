# FactCheck

[FactCheck](https://github.com/dice-group/FactCheck) is an algorithm that is used to validate triples i.e., <s, p, o> from a given Knowledge base KB. It does this by finding textual evidences from the Web or given reference corpus and use them to classify a triple as correct or incorrect.

FactCheck is modularized and contains the following three component:

1. **factcheck-demo** is Front-end which lets users to enter **subject**, **predicate** and **object** or a (ttl) **file** and submit to **factcheck-service**.

2. **factcheck-service** is a Spring RESTful web service for Factcheck which requires an object, predicate and subject to be specified in the JSON request. The API returns the total score as well as proof sentences and their corresponding scores.

3. **factcheck-core** contain the core algorithm that is used to validate a given triple. The algorithm is developed as a framework and can be adapted to use various state-of-the-art classification techcniques available in [WEKA](https://www.cs.waikato.ac.nz/ml/weka/). The algorithm can be used independently.

Detailed instructions on installation and usage is described under these modules. Additionally, all the links to external datasets, corpora are provided.
