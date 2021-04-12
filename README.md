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

### Palisade Readers

The `Readers` library enables functionality for providing data with the relevant data restrictions applied.
It provides the services needed to locate the references to the requested records, uses this to retrieve the data and then to extract each record where it can be filtered in using the restrictions defined by the rules in place for the data.

A good starting point to understanding this library is with the Data Service.
The client will have sent the initial request to access data with the Palisade Service and received a response with a unique identifier for the request.
This is then used to send a request to the Data Service to retrieve the data.
The Reader library provides the functionality for the Data Service to retrieve the data plus the rules that need to be applied.
This information is then used to return an output stream of the filtered data to the client.

In terms of code, there are two interfaces that are core to this library, the [ResponseWriter](readers-common/src/main/java/uk/gov/gchq/palisade/reader/common/ResponseWriter.java) and the [DataReader](readers-common/src/main/java/uk/gov/gchq/palisade/reader/common/DataReader.java). 
These provide the basis of the solution for retrieving the data, filtering according to the rules for the query and provides a stream of data to be made available to the client.

The writer in the class [SerialisingResponseWriter](readers-common/src/main/java/uk/gov/gchq/palisade/reader/common/SerialisingResponseWriter.java) and the reader implementation in the abstract class [SerialisedDataReader](readers-common/src/main/java/uk/gov/gchq/palisade/reader/common/SerialisedDataReader.java) provide the implementation for the data retrieval.
The class [HadoopDataReader](hadoop-reader/src/main/java/uk/gov/gchq/palisade/reader/HadoopDataReader.java) is the reader implementation for Hadoop. In the
[SerialisingResponseWriter.write](readers-common/src/main/java/uk/gov/gchq/palisade/reader/common/SerialisingResponseWriter.java) method, these classes are used to

first retrieve the references to the data plus the rules that will be used for
filtering. This is used to set up a processing stream for the client's request.

The process will retrieve the data as an input stream. It will then be
deserialised into records where the rules are applied to redact or mask the records.
This is then re-serialised into an output stream.  This output stream will be
the data ready for the Data Service to provide to the client.
