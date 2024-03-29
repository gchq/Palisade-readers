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

# Avro Serialiser
Deserialise [Apache Avro](https://avro.apache.org/) resources into streams of bytes.

### Example Configuration
The Data Service uses the provided default configuration, using the `AvroSerialiser` for any resource with the `avro/binary` format:
```yaml
data:
  serialisers:
    "[avro/binary]": "uk.gov.gchq.palisade.service.data.avro.AvroSerialiser"
```

