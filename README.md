# HMPPS Delius API Service

## Overview

Service providing specific API services for the nDelius application. Developed
as a collaboration between MoJ Digital and Unilink.

Please raise any questions in the #delius-api Slack channel

## Purpose of the API Service 

The API service is intended to work as a entry-point for integration with
nDelius where it is important to apply nDelius application logic to API
operations. The API is closely tied to the nDelius data model and exposes some
internal implementation details. Some endpoints may require some
nDelius-specific knowledge to use such as entity identifiers (i.e. reference codes
where these exist but possibly ids where codes are not available). For this
reason we expect the API to be used as a backend service in combination with
other, higher-level, API services such as [Community API](https://github.com/ministryofjustice/community-api) 
where the endpoints provided by this service can be combined with others to
create more probation domain-specific APIs. 

Read-access to nDelius is currently provided by [Community API](https://github.com/ministryofjustice/community-api) 
and we will not look to replace these services as a priority, although that
may be added to the product roadmap as it develops.

## Development

H2 Web console - <http://localhost:8080/h2-console>

* JDBC URL: `jdbc:h2:file:/tmp/hmpps-delius-api-dev;Mode=Oracle`
* USER: `sa`
* PASSWORD: `password`

### High-Level Architecture

![nDelius API](./doc/img/nDelius-API.png?raw=true)

## Initial Use-Cases 

### Writing Simple nDelius Contacts 

Writing data to the nDelius contact log, applying the nDelius application
logic for all associated actions triggered by adding the contact entry.

### Writing nDelius NSIs 

Writing NSIs of specific types to ensure contacts can be created and linked to
the correct container entities in nDelius 

## Key Technical Points

- Spring Boot service built mainly in Kotlin
- Intention is to reuse the logic in the nDelius Java application tier as far
  as possible 
- User and client authentication via HMPPS-Auth
- Requires access to the nDelius Oracle database 
- Deployed in the nDelius AWS environment

## Infrastructure 

### Deployment Environments 

### Deployment Pipelines 

## Building and Testing Locally 

## Development Setup




