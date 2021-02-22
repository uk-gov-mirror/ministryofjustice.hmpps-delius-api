# 6. Use Client Users Rather Than User-Specific Users When Interacting with Delius

Date: 2021-02-19

## Status

Accepted

## Context

- The Delius API can connect to the Delius database create and update data
- The nDelius application uses an Oracle-specific feature called Virtual
  Private Database (VPD) to limit the scope of data access based on business
  rules 
- This VPD feature relies on a user being identified as an nDelius user and
  for that user to be set up in the Delius database so the business rules can
  be applied
- Not all end-users of the Delius API will have nDelius accounts
- There are classes of user that will never have nDelius accounts such as
  users from intervention providers external to the MoJ
- There may be a chain of services from the original user authentication with
  a service to the ultimate API call to the Delius API

### Authentication 

- HMPPS-Auth will authenticate either an end-user (using the authorisation
  code grant type) or a client (using the client credentials grant type)
- If the authorisation grant type is used the authentication token will supply
  an end-user's details (i.e. the username) for the system used as the
  identity service (NOMIS, Delius or HMPPS-Auth)
- If the client credentials grant type is used the authentication token will
  not supply any end-user details
- Community API deals with this by applying the VPD only if the authentication
  token supplies a valid Delius user
- If the authentication token supplies a non-Delius user (NOMIS, HMPPS-Auth or
  a client system) Community API does not apply the VPD context to the
  database connection
- This means that users of Community API may be able to access additional data
  when accessing Delius using an identity that is not known

### Limited Access Offenders

- Limitations on the service user details that can be accessed using a
  database call are applied using the VPD context on the connection

## Decision

- We will create a Delius user for each client service that will use the API 
- The Delius API will use this user value to set the VPD context on the Delius 
  database connection

## Consequences

- A consistent user is supplied to the Delius API for requests from a specific
  calling service
- Any audit records will be set using this service user
- The VPD context for the user needs to allow access to the right level of
  permissions for the Delius API to operate
- If the calling service is using an intermediate to interact with the Delius
  API then the intermediate must supply an valid authentication token with the
  username of the calling service supplied
- Community API currently has a mechanism to pass on the authentication token
  with the username of the caller
- The Delius API service will need to be configured to authenticate using
  these tokens

