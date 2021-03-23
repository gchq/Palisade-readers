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

### Readers

The Readers library enables functionality for providing data that has
had the relevant data restrictions applied. It provides the services 
needed to locate the references to the requested records, uses this to 
retrieve the data and then to extract each record where it can be filtered
in using the restrictions defined by the rules in 
place for the data.

A good starting point to understanding this library is to start with the Data 
Service. The client will have sent the initial request to access data with
the Palisade Service and received a response with a unique identifier for the
request.  This is then used to send a request to the Data Service to retrieve
data.  The Reader library provides the functionality for the Data Service to 
retrieve the data and rules needed for filtering. This information is then 
used to return an output stream of the data to the client.

In terms of code, there are two interface that are core to this library: 
`DataReader`; and `ResponseWriter`. Their implementations in the 
class `SerialisingResponseWriter`, abstract class `SerialisedDataReader` and 
its the extended class `HadoopDataReader`. These provide the core 
functionality used to process the data.  In `SerialisingResponseWriter`'s 
`write` method involves using these classes to first retrieve the data from 
an input stream. It is then deserialised it into records where the rules are 
applied to filter the data. The return is the filtered data re-serialised 
back into an output stream.


#### Configuring for Hadoop on Windows

In order to use the [HadoopResourceService](hadoop-resource/src/main/java/uk/gov/gchq/palisade/service/resource/service/HadoopResourceService.java) and [HadoopDataReader](hadoop-reader/src/main/java/uk/gov/gchq/palisade/reader/HadoopDataReader.java) in a Windows environment, the provided [hadoop binaries](hadoop-resource/src/test/resources/bin/) should be added to the system PATH.
If using IntelliJ's built-in test runners, these binaries will also need to be added under the project's global libraries.
