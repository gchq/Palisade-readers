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

# Hadoop Data Reader

The Hadoop Data Reader contains the Hadoop specific classes and configuration required to allow the Data Service to read data from [Apache Hadoop](https://hadoop.apache.org/) and for Palisade to work with Hadoop deployments.  
Using the Hadoop API outlined [here](https://hadoop.apache.org/docs/current/hadoop-project-dist/hadoop-common/FileSystemShell.html), and found [here in maven](https://mvnrepository.com/artifact/org.apache.hadoop/hadoop-common), the Data Service can then communicate with local file systems, HDFS (Hadoop file systems) and S3 object stores.

The `readRaw` method in the HadoopDataReader opens a connection to Hadoop, and download the data as an InputStream, which is read by the Data Service, and serialised into a human-readable form.

To choose the hadoop-data-reader as the technology in your Palisade deployment, you can do so by running the following:  
```java -Dloader.path=hadoop-data-reader/target -jar data-service.jar``` 

Or by configuring the implementation in the relevant yaml files:
```yaml
data:
    implementation: hadoop
```

Although, although Hadoop supports local File Systems and S3 Object stores, it won't be the most efficient, and other modules should be considered.  
For S3 object stores, read the [S3-Data-Reader README.md](../s3-data-reader/README.md).  
For local file systems, read the [SimpleDataService](../../Palisade-services/data-service/README.md#Data Service) implementation. 

