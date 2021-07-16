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
If the resource does exist, a `LeafResource` is created with the resourceId and connection detail information so that it can be read by the Data Service.

To choose the hadoop-resource-service as the technology in your Palisade deployment, you can do so by running the following:  
```java -Dloader.path=hadoop-resource-service/target -jar resource-service.jar```  
Or by configuring the implementation in the relevant yaml files:
```yaml
resource:
        implementation: hadoop
```