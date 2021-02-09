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

### Code Repositories 
- Application Code and Build Pipeline Definitions: https://github.com/ministryofjustice/hmpps-delius-api
- ECS Cluster Definition : https://github.com/ministryofjustice/hmpps-delius-network-terraform/tree/master/ecs-cluster
- Deployment Pipeline Definition: https://github.com/ministryofjustice/hmpps-delius-pipelines/tree/master/engineering/builds

### Deployment Pipelines 

#### Application Build, Test and Container Build 

- https://app.circleci.com/pipelines/github/ministryofjustice/hmpps-delius-api
- Each application branch is built and tested when pushed to GitHub
- A container is built and pushed to the application container repository on a 
  successful build of the `main` branch

### Technical Environments

- [Technical Environment Setup and Locations](./doc/architecture/technical-environments.md) 

## Building and Testing Locally 

``` sh
# Build and unit test the application 
./gradlew check 
```

## Development Setup

``` sh
# Install dependencies 
# JDK v11

# Check out the application code 
git clone git@github.com:ministryofjustice/hmpps-delius-api.git

# Run the application from the latest docker containers
docker-compose up

# Obtain an oauth token from the local HMPPS-Auth
AUTH_TOKEN=$( \
    curl --location \
         --request POST "http://localhost:9090/auth/oauth/token?grant_type=client_credentials" \
         --header "Authorization: Basic $(echo -n community-api-client:community-api-client | base64)" \
    | jq -r .access_token) 
    
# Make a request to the local Delius API
curl -v http://localhost:8081/health/ping --header "Authorization: Bearer $AUTH_TOKEN" | jq . 

# Run the application from the local filesystem sources 
./gradlew bootRun

```



