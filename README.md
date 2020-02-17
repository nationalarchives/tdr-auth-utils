## TDR Auth Utils

The aim of this project is to create a library of useful functions related to jwt tokens in general and Keycloak in particular. 

The functions are:

* verifyToken - Takes a jwt token as a string, verifies that it is valid against the keycloak server and hasn't expired and returns either an `Some(AccessToken)` or `Option.empty` depending on success or failure. 