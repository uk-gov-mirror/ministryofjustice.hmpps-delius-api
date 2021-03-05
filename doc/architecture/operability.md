 # Operability

Overview of operability and 'ready for production' concerns based on the
'Operability Check' questions of the [Multi-team Software Delivery Assessment](https://github.com/ConfluxDigital/software-delivery-assessment/)

## Collaboration
- The Delius API development team is a temporary team made up of members of
  NDST, Probation Devops (Delius Infrastructure), Probation End-to-End and
  HMPPS Technical Architecture
- The team collaborates closely with Probation Devops on definition and
  deployment of services to cloud hosting environments. Team members have
  responsibilities across both teams which ensures a close working
  relationship via Slack and video conference
- Collaboration with DPS Tech / Platform team via Slack and video conference
  on the use of common platform services and methods of monitoring, tracing
  and alerting
- Team operations are based in an open Slack channel (#delius-api) on the [MOJ Digital & Technology Slack team](mojdt.slack.com)
- Application and infrastructure code repositories are open and publicly
  available in the [MoJ GitHub organisation](MOJ Digital & Technology)
- The team actively engages both it's dependencies (Delius, HMPPS platform
  services) and dependent services (Interventions) on operational aspects of
  the service as part of system design work

## Spend on Operability
- The team has had a full-time operations-focused member and has allocated
  time to set up deployment pipelines, configuration management and
  application logging as part of the initial system development work
- There is no formal split between effort on product and operational work and
  this is not tracked beyond high-level metadata in the JIRA tickets (e.g.
  named JIRA epics)
- The operational principles for the system have been informed by NDST methods
  and tooling
- Over time the system will be taken on by NDST and the operation of the
  services will transfer to them

## Feature Toggles
- The initial scope of the product is small and therefore there is currently
  no need for feature toggles on any specific feature or API endpoint
- The Swagger document for the API shows the currently active endpoints in a
  specific environment

## Configuration Deployment
- The infrastructure and application configuration is held in a separate
  repository with a dedicated deployment pipeline
- Infrastructure and application configuration changes can be deployed without
  affecting the deployed application version
- The infrastructure pipeline requires manual approval beyond the test stage
- Infrastructure and configuration changes will not trigger a refresh of the
  application containers so changes are picked up on the next application
  deployment

## System Health

### Health Check
- System health check endpoint is monitored by the load balancer
- The ECS service will replace any failing container instances

### Basic System Metrics
- Basic system metrics for the ECS service are collected using CloudWatch

### Correct Operation
- Key scenarios are executed using an end-to-end test executed on each
  pre-production environment after a deployment

## Service KPIs
- We will define KPIs for normal execution of API operations i.e. successful
  creation of Delius entities and successful updating of Delius entities
- We will define KPIs for the expected failure rate of API operations
- Some of the KPIs will be defined across Delius API and Community API
- We will use App Insights to track, report and alert on KPI boundaries
- App Insights covers both API services so should allow us to track KPIs
  across both
- **[JIRA]** Create App Insights KPI dashboard and alerts

## Logging
- We do not currently test that logging is working correctly to a local log appender
- We do not currently test that logging is working correctly to CloudWatch
- We do not currently test that logging is working correctly to App Insights
- **[JIRA]** Create tests for correct operation of log entries

## Testability
- We run unit, integration and end-to-end tests as part of the software
  delivery pipeline
- **[JIRA]** Document with examples how to add different types of test to the
  delivery pipeline

## Certificate Management
- Our TLS certificates are managed by AWS Certificate Manager
- All actively used TLS certificates are automatically renewed by AWS
- Certificate expiry is monitored by a custom lambda that monitors ACM and
  alerts to Slack before expiry
- https://dsdmoj.atlassian.net/wiki/spaces/DAM/pages/3000041541/Certificate+Expiry+Alerts

## Sensitive Data
- We do not currently test for sensitive data in the logs
- **[JIRA]** Create tests to identify if we write sensitive data in the logs

## Performance
- We do not currently test for performance characteristics of the API
- **[JIRA]** Create tests to identify the performance characteristics of the API
  operations

## Failure Modes
- We use a known set of Spring-managed exceptions to classify API service
  failures
- All exceptions are logged and errors and the exception type is included in
  the log message

## Call Tracing
- We use App Insights to trace end-to-end calls through the system
- The services making up the system are instrumented and therefore we can
  trace a complete API call across all components

## Service Status
- API service status is not currently on the main operations dashboard
- Service status can be monitored using a CloudWatch query
- **[JIRA]** Add the API service to the main Delius operations dashboard
