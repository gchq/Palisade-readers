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
# Hadoop Library

This is a Hadoop client library dependency which is used within Palisade. Hadoop contains several dependencies for its own use, which
Palisade also uses. The later versions Palisade uses are not always backwards-compatible, therefore we use this Hadoop dependency which
excludes others which have been found to cause problems within Palisade.

## Profiles
There are two Maven build profiles that can be used with this module:

1. `aws` - This profile should be activated at the command line (using `mvn -P aws ...`) and ensures that the Hadoop AWS
dependencies are included in the client library which will enable Palisade to read files from Amazon S3 filesystems. Note that
configuring the Hadoop resource service in Palisade to expect s3a:// file URIs will still be required.

2. `windows` - **THIS PROFILE AUTO ACTIVATES** This profile is used to ensure Maven includes certain other Hadoop artifacts
that are necessary to ensure Palisade functions within a Windows environment. Although it can be switched on manually with
`mvn -P windows ...` it will automatically be enabled when building Palisade on an Windows environment.