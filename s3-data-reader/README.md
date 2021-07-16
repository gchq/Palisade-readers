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

# S3 Data Reader

The S3 Data Reader contains the S3 specific configuration required to allow the Data Service to read data from AWS S3 using [alpakka s3](https://doc.akka.io/docs/alpakka/current/s3.html).  

The `readRaw` method in the S3DataReader first checks if the user has access to the bucket within S3, and if so, downloads the LeafResource via Alpakkas S3 download API.

To choose the s3-data-reader as the technology in your Palisade deployment, you can do so by running the following:  
```java -Dloader.path=s3-data-reader/target -jar data-service.jar```  
Or by configuring the implementation in the relevant yaml files:
```yaml
data:
    implementation: s3
```