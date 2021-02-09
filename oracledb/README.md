# Delius Oracle Database
Scripts for building and publishing a dev/test instance of an Oracle 18c XE database, loaded with the Delius PL/SQL code and a set of synthetic test data.

This is intended for use in integration tests where core Delius functionality (e.g. triggers) must be exercised.

## Building Locally
TODO...

## Running From ECR
A pre-built image is available from a private Delius ECR repository:
```
895523100917.dkr.ecr.eu-west-2.amazonaws.com/hmpps/delius-test-db
```
Access to this repository can be requested in the [#delius_infra_support](https://mojdt.slack.com/archives/CNXK9893K) Slack channel.

Once you have IAM access to the repository, follow these instructions to pull the image. 
1. Configure the AWS CLI to assume a role with access to ECR in the Delius Engineering account. 
   Below is a sample `~/.aws/credentials` file for assuming the `MoJDevelopers` role with support for MFA.
<pre>
[default]
aws_access_key_id = <b>ACCESS_KEY_ID</b>
aws_secret_access_key = <b>SECRET_ACCESS_KEY_ID</b>
[eng-dev]
source_profile = default
mfa_serial = arn:aws:iam::570551521311:mfa/<b>IAM_USERNAME</b>
role_arn = arn:aws:iam::895523100917:role/MoJDevelopers
</pre>

2. Login to ECR:
```shell
aws ecr get-login-password --profile eng-dev | docker login --username AWS --password-stdin 895523100917.dkr.ecr.eu-west-2.amazonaws.com
```
3. Pull the image and start a container:
```shell
docker run -p 1521:1521 895523100917.dkr.ecr.eu-west-2.amazonaws.com/hmpps/delius-test-db:latest
```

The Oracle Database will be available on local port `1521`, and can be accessed using any of the following accounts:
* `delius_app_schema` - for full access to the Delius application tables and code
* `delius_pool` - for restricted access (by Oracle VPD)
* `system` - for system-level access, useful for managing users etc.

All accounts have the same password: `NDelius1`, and the service name should be set to `XEPDB1`.

*Example:*
```shell
docker run -d -p 1521:1521 895523100917.dkr.ecr.eu-west-2.amazonaws.com/hmpps/delius-test-db:latest 
sqlplus delius_app_schema/NDelius1@XEPDB1
> SELECT COUNT(*) FROM OFFENDER;
1234
```
