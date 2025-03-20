openapi-ts-client
=================

This directory contains the generated **k9-tilbake.openapi.json** specification file and other files used as input to define
a generated openapi typescript client.

The k9-tilbake.openapi.json file is generated from the java source in this project, and represents a specification of the 
rest api the web server provides.

It is used to generate and publish a typescript/javascript client library npm package for communicating with the web server.

The k9-tilbake.openapi.json file is automatically generated during the build pipeline. Changes to the generated result is 
automatically committed to git in the build pipeline. One should never edit the file manually.

This is done so that the build pipeline can detect if there are changes in the api spec since last deployment, and only
publish a new package if there is changes. It also simplifies manual review of changes to the api spec during PR review.

The file is also intentionally git-ignored, so that if one runs the code generator locally changes should not be committed.

The package.json file in here is combined with predefined template in https://github.com/navikt/openapi-ts-clientmaker 
source code, to create the package.json for the published npm package. Also, the package version is overridden when the 
client is built in the GitHub pipeline. See the GitHub workflow definition for more details.

## Use of published package

The generated package is published as a **GitHub npm package**. It can be used like this:

### installation
Install the package as usual from GitHub package registry. More info about that here: 
 https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-npm-registry

### Code example
```typescript
import { K9TilbakeClient } from "@navikt/k9-tilbake-typescript-client";

const organisasjonsnr = 111222333; // input
const k9tilbake = new K9TilbakeClient();
const mottakerinfo = await k9tilbake.brev.getBrevMottakerinfoEreg({organisasjonsnr})

console.debug(`mottaker navn: ${mottakerinfo?.navn}`)
```
