## Deploying

The plugin server relies on OAuth2.0 for external authentication.
Extended Support is provided for github and google.
To configure the available OAuth2.0 providers, refer to the [Spring Configuration Documentation](https://docs.spring.io/spring-security/reference/reactive/oauth2/login/core.html#oauth2login-boot-property-mappings) and set the according values in the environment variable `SPRING_APPLICATION_JSON` like this:

````
       SPRING_APPLICATION_JSON: '{
        "spring.security.oauth2.client.registration.github.client-id": "{YOUR_VALUE_HERE}",
        "spring.security.oauth2.client.registration.github.client-secret": "{YOUR_VALUE_HERE}",
        "spring.security.oauth2.client.registration.google.client-id": "{YOUR_VALUE_HERE}",
        "spring.security.oauth2.client.registration.google.client-secret": "{YOUR_VALUE_HERE}",
        "spring.security.oauth2.client.registration.google.scope": ["email","profile"]
       }'
````

It is important to explicitly set the scopes for google to `spring.security.oauth2.client.registration.google.scope=email,profile`.
Without this setting authentication with google will fail.
