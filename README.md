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

# <img src="logos/logo.svg" width="180">

## A Tool for Complex and Scalable Data Access Policy Enforcement
Windows is not an explicitly supported environment, although where possible Palisade has been made compatible.  
For Windows developer environments, we recommend setting up [WSL](https://docs.microsoft.com/en-us/windows/wsl/).

# Palisade Readers

The Palisade-readers repository enables functionality for providing the implementations needed for Palisade to integrate with existing products and technologies.

A good starting point to understanding these modules is with the Data Service.
For a single request to the Data Service, the request might look like `GET /read/chunked resourceId=hdfs:/some/protected/employee_file0.avro token=some-uuid-token`, which can be broken down into a number of capabilities that are required:
* Reading data from an `HDFS` cluster
* Deserialising an `Avro` data-stream
* Understanding what an `Employee` datatype looks like and how the rules on the `/protected` directory will apply to the fields
* How to return data for the `/read/chunked` API endpoint

The Palisade-readers repository therefore implements many of these functions, abstracted away from the inner workings of the core [Palisade-services](https://github.com/gchq/Palisade-services):
* In this case, the Data Service's default API is for the `/read/chunked` endpoint, and is therefore already implemented in the [ReadChunkedDataService](https://github.com/gchq/Palisade-services/blob/develop/data-service/src/main/java/uk/gov/gchq/palisade/service/data/service/ReadChunkedDataService.java), but we could imagine other protocols.
* To read from an `HDFS` filesystem, we need the Resource Service to discover the available resources (like doing an `ls` on a directory), as well as needing the Data Service to read the raw bytes of that resource.
  We implement the [Hadoop Resource Service](hadoop-resource-service) and [Hadoop Data Reader](hadoop-data-reader) to enable this functionality.
* To work with the raw bytes returned from the Data Reader, we need to deserialise into Java objects.
  We implement the [Avro Serialiser](avro-serialiser) that, given a domain class, will serialise and deserialise between Java objects of this class and plain bytes.
* The domain class for the aforementioned serialiser in this case is `Employee`, which is implemented elsewhere and equivalent to a schema definition and is generally a property of the specific dataset, not the Palisade deployment in general.
  All that is important is that this POJO exists somewhere on the classpath.

The decoupling of these technology-specific implementations allows Palisade to be flexible enough to be trivially implemented into existing tech stacks and datasets.
The above deployment could as easily have been using the [S3 Resource Service](s3-resource-service) and [S3 Data Reader](s3-data-reader) to serve a request for `GET /read/chunked resourceId=s3:/some/protected/employee_file0.avro token=some-uuid-token`.

For information on the different implementations, see the following modules:
- Apache Avro Format
    - [Avro Serialiser](avro-serialiser/README.md)
- Apache Hadoop Distributed File System
    - [Hadoop Resource Service](hadoop-resource-service/README.md)
    - [Hadoop Data Reader](hadoop-data-reader/README.md)
- Amazon S3 Object Storage
    - [S3 Resource Service](s3-resource-service/README.md)
    - [S3 Data Reader](s3-data-reader/README.md)
