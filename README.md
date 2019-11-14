<!---
Copyright 2019 Crown Copyright

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

### Scalable Data Access Policy Management and Enforcement

The responsibility of the data service is to take the read request from the client, request the trusted details about the request from the Palisade service (what policies to apply, user details, context, etc).
The data service then passes that information to the `DataReader` which is then responsible for connecting to the resource, deserialising the data and then serialising the data ready to be sent to the client.
The data service is also responsible for ensuring the relevant audit logs are generated.

This repository contains implementations of the `DataReader`
