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

The Readers library enables functionality for providing data after it has been
processed in accordance to the relevant data restrictions. It provides the 
services needed to locate the references to the requested records, uses this
to retrieve the data and then to extract each record where it can be filtered
in using the restrictions defined by the user, context and rules in 
place for the data.

A good starting point to understand this library is to look at the two core 
Interfaces: `DataReader`; and `ResponseWriter`, their implementations in the 
class `SerialisingResponseWriter`, abstract class `SerialisedDataReader` and 
its the extended class `HadoopDataReader`. These provide the core 
functionality used to process the data.  In `SerialisingResponseWriter`'s 
`write` method involves using these classes to first retrieve the data from 
an input stream. It is then deserialised it into records where the rules are 
applied to filter the data. The return is the filtered data re-serialised 
back into an output stream.

The Data Service depends on this library to process client requests for records 
that has been registered with the Palisade Services. The client sends a request 
with a unique identifier for the request.  This is used to identify the records 
that are contained in request and with this the information about the user, the 
context of the request and the rules that are to be implemented for this request.
This information is then passed onto the classes in this library that will return
an output stream that is given to the client.


#### Configuring for Hadoop on Windows

In order to use the [HadoopResourceService](hadoop-resource/src/main/java/uk/gov/gchq/palisade/service/resource/service/HadoopResourceService.java) and [HadoopDataReader](hadoop-reader/src/main/java/uk/gov/gchq/palisade/reader/HadoopDataReader.java) in a Windows environment, the provided [hadoop binaries](hadoop-resource/src/test/resources/bin/) should be added to the system PATH.
If using IntelliJ's built-in test runners, these binaries will also need to be added under the project's global libraries.
