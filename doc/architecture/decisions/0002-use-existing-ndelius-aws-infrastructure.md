# 2. Use Existing nDelius AWS Infrastructure

Date: 2021-02-01

## Status

Accepted

## Context

The nDelius API application must be deployed into a cloud environment that
allows it to access the nDelius Oracle database to allow it to read and write
data. The database is currently deployed in the nDelius AWS account and the
network access setup allows applications deployed into this environment access
to the database. Other cloud environments, such as MoJ Cloud Platform, do not
have the network setup in place to access the nDelius database as-is.

## Decision

We will use the existing nDelius AWS infrastructure

## Consequences

By using the existing nDelius AWS infrastructure we expect the build
of the deployment infrastructure to be achieved quickly and without blockers.
Team members are able to administer the account and the infrastructure will
not need complex setup to enable access to the nDelius databases in the
various application environments. The decision does mean that the
infrastructure will be deployed using existing methods and repositories. This
implies:

- ECS for application deployment
- ECR for the application container repository
- CodePipeline for the infrastructure deployment
- CodeDeploy for the application deployment

The infrastructure will be defined in code in these repositories:

- https://github.com/ministryofjustice/hmpps-delius-network-terraform
- https://github.com/ministryofjustice/hmpps-delius-core-terraform

The application build, test and image creation pipeline will be defined in
this repository: 

- https://github.com/ministryofjustice/hmpps-delius-pipelines/

The ECR repository is open to the public and this means that the container
images we build will have open access, matching the access policy of the
application code repository.


