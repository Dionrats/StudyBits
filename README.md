# StudyBits
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=Dionrats_StudyBits&metric=alert_status)](https://sonarcloud.io/dashboard?id=Dionrats_StudyBits)  [![Coverage](https://sonarcloud.io/api/project_badges/measure?project=Dionrats_StudyBits&metric=coverage)](https://sonarcloud.io/dashboard?id=Dionrats_StudyBits) [![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=Dionrats_StudyBits&metric=security_rating)](https://sonarcloud.io/dashboard?id=Dionrats_StudyBits) 

[![Build](https://travis-ci.com/Dionrats/StudyBits.svg?branch=master)](https://travis-ci.com/Dionrats/StudyBits)



## Table of contents
1. [Introduction](#introduction)
2. [Contributing](#contributing)
  2.1 [Repository Structure](#structure)
  2.2 [Development Setup](#dev)
  2.3 [How to send a PR](#pullrequests)
3. [Running StudyBits](#running)
    3.1 [Running in Docker](#RunInDocker)
4. [Links](#links)


## Introduction <a name="introduction"></a>
StudyBits allows students to receive credentials from their universities, and issue zero-knowledge proofs to other universities.

StudyBits leverages the [Sovrin network](https://sovrin.org/) to store DIDs, Schemas and Credential Definitions.

This repository contains the University Agent, which is run by universities to distribute credentials to students. See the [architecture](https://github.com/Quintor/StudyBits/wiki/StudyBits-v0.3-architecture) for a bigger picture.
The agent is used through the [StudyBits Wallet](https://github.com/Quintor/StudyBitsWallet)


Contact us on [Gitter](https://gitter.im/StudyBits/Lobby)


## Contributing <a name="contributing"></a>

### Repository Structure <a name="structure"></a>

The StudyBits repo consists of the following parts:

```
* ci
  * Contains docker files used by Circle-ci for ci.
* docs
  * Contains design and implementation documentation.
* university-agent
  * Contains java source-code for the university agent.
```

### Development Setup <a name="dev"></a>

In order to develop for StudyBits a couple of dependencies need to be installed:

1. Install [libindy](https://github.com/hyperledger/indy-sdk/tree/master/libindy)
    The current version of StudyButs requires Libindy v1.6.6 to be installed, note that later versions are not supported currently.

2. Make sure [Project Lombok](https://projectlombok.org/) can work it's magic. In your IDE of choice, please enable precompiling.

### How to send a PR <a name="pullrequests"></a>

Before sending a PR make sure to be compliant with the following:

+ Do not create many PRs for one feature. Consider implementing a complete feature before sending a PR. 
+ Consider sending a design doc folder for a new feature.
+ Make sure that a new feature or fix is covered by tests (try following TDD).
+ Make sure that documentation is updated according to your changes.
+ Provide a full description of changes in the PR.
+ Make sure that static code validation passed.
+ You need to make sure that all the tests pass.
+ A reviewer or maintainer will merge the PR.

## Running StudyBits <a name="running"></a>

### Running in docker <a name="RunInDocker"></a>

Starting: Use the `./launch.sh` script to launch all required components of studybits.

Stopping: Use the `./stop.sh` script to stop all running studybits components.

Running tests: Use the `./launch.sh` script with the `--test / -t` flag to run studybits and run integrationtests.

Please note that studybits requires a valid active ipaddress in order to run.

## Links <a name="links"></a>

#### StudyBits Native
[Quindy](https://github.com/Quintor/quindy)
[StudentWallet](https://github.com/Quintor/StudyBitsWallet)

#### HyperLedger Indy
[Indy SDK](https://github.com/hyperledger/indy-sdk)
[Indy Node](https://github.com/hyperledger/indy-node)
[Indy Agents](https://github.com/hyperledger/indy-agent)
[indy plenum](https://github.com/hyperledger/indy-plenum/tree/master/docs)
[indy crypto](https://github.com/hyperledger/indy-crypto/blob/master/README.md)

[Indy documentation](https://hyperledger-indy.readthedocs.io/en/latest/)

#### Glossary
[Sovrin glossary](https://docs.google.com/document/d/1giOzpTFXypJ6bAUp_6g93kYOEiNa5eWI1KeIg6wb598/edit)

#### Communication 
[Agent-to-Agent communication videoexplaination](https://drive.google.com/file/d/1PHAy8dMefZG9JNg87Zi33SfKkZvUvXvx/view)

## Releasing

Use the release script. `./release.sh 0.3.0` to release version 0.3.0 of the agent.


