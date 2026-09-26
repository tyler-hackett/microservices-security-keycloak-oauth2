# Keycloak configuration

[keycloak/caf-realm.json](../keycloak/caf-realm.json) is a partial export of `caf-realm` from the v3 environment. Keycloak masks client secrets in exports and leaves out users and master realm settings, so the steps below cover what the import does not.

## What the realm export contains

| Setting | Value |
|---|---|
| Realm | `caf-realm`, access token lifespan 300 s, SSO idle timeout 30 min |
| Events | Enabled, including LOGIN, CODE_TO_TOKEN, and TOKEN_EXCHANGE |
| Client `caf-webapp` | Confidential OIDC client, standard flow, root URL `http://localhost:8080`, redirect `http://localhost:8080/*`, standard token exchange enabled, refresh tokens for exchange limited to the same session |
| `caf-webapp` roles | `user`, `approver`, `admin` |
| Client `caf-rest` | Confidential OIDC client, root URL `http://localhost:5050`, direct access grants on for curl testing |
| `caf-rest` roles | `user`, `approver` |

## After importing

1. **Regenerate client secrets.** In caf-realm > Clients > `caf-webapp` > Credentials, click Regenerate and copy the value to `CAF_WEBAPP_CLIENT_SECRET` in `.env`. Do the same for `caf-rest` and `CAF_REST_CLIENT_SECRET`. The imported value is only the export mask.
2. **Turn `admin-cli` into a service account.** In the master realm, open Clients > `admin-cli`, turn on Client authentication, and under Authentication flow enable Service accounts roles. On the Service account roles tab, assign the `caf-realm-realm` client roles the webapp needs to manage users and look up its own client, for example `view-users`, `query-users`, `manage-users`, and `view-clients`. Copy the secret from the Credentials tab to `KEYCLOAK_ADMIN_CLIENT_SECRET`. With this setup the app authenticates with the client credentials grant and never holds an admin password.
3. **Create users.** See [Test users](../README.md#test-users-and-trying-the-app) in the README. Users need caf-rest roles as well as caf-webapp roles, because caf-rest authorizes on `resource_access/caf-rest/roles` in the exchanged token.
4. **Match the host.** Start Keycloak with `KC_HOSTNAME` set to the same value the services use. The issuer in every token comes from it, and token exchange rejects a subject token whose issuer does not match.

## Calling caf-rest directly

Direct access grants on `caf-rest` exist only so the API can be tested with curl. They should be off in a real deployment.

```bash
# Without a token: 401
curl -i http://localhost:5050/api/images

# Get an access token for a user with the caf-rest user role
curl -s -X POST "http://$KC_HOSTNAME:9090/realms/caf-realm/protocol/openid-connect/token" \
  --user "caf-rest:$CAF_REST_CLIENT_SECRET" \
  -d grant_type=password -d username=<USERNAME> -d password=<PASSWORD> | jq -r .access_token

# With the token
curl -i http://localhost:5050/api/images -H "Authorization: Bearer <ACCESS_TOKEN>"
```

caf-thumbnail answers 401 to any request without a JWT signed by the caf-rest private key:

```bash
curl -i "http://localhost:4040/api/thumbnail?bucket=caf-images&object=<IMAGE_ID>&width=150&height=150"
```
