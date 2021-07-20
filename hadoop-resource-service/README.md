<!---
Copyright 2018-2021 Crown Copyright

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
--->

# <img src="..logos/logo.svg" width="180">

## A Tool for Complex and Scalable Data Access Policy Enforcement

# Hadoop Resource Service

The Hadoop Resource Service contains Hadoop specific configuration required to allow the Resource Service to communicate with Hadoop and check if resources exist.  
Using the Hadoop API outlined [here](https://hadoop.apache.org/docs/current/hadoop-project-dist/hadoop-common/FileSystemShell.html), and found [here in maven](https://mvnrepository.com/artifact/org.apache.hadoop/hadoop-common), the Resource Service can then communicate with local file systems, HDFS (Hadoop file systems) and S3 object stores to check if the resource exists by listing the files, and then add metadata to the returned LeafResource for use within the rest of Palisade, specifically for use within the Data Service when reading resources, and the Policy Service when finding and applying resource level policies.

To choose the hadoop-resource-service as the technology in your Palisade deployment, you can do so by running the following:  
```java -Dloader.path=hadoop-resource-service/target -jar resource-service.jar```  
Or by configuring the implementation in the relevant yaml files:
```yaml
resource:
        implementation: hadoop
```