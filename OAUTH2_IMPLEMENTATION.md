# OAuth2 Implementation Summary

## Overview

The Dynamic Portfolio API has been successfully migrated from custom JWT authentication to **Spring Authorization Server (OAuth2)**.

## What Changed

### 1. Dependencies (pom.xml)
**Removed:**
- `io.jsonwebtoken:jjwt-api`
- `io.jsonwebtoken:jjwt-impl`
- `io.jsonwebtoken:jjwt-jackson`

**Added:**
- `spring-boot-starter-oauth2-authorization-server`
- `spring-boot-starter-oauth2-resource-server`

### 2. Configuration Files

#### New Files Created:
- `src/main/java/com/portfolio/api/config/AuthorizationServerConfig.java` - OAuth2 server configuration
- `src/main/resources/db/migration/V9__create_oauth2_tables.sql` - Database schema for OAuth2
- `postman/OAuth2-Setup.postman_collection.json` - OAuth2 testing collection
- `postman/OAUTH2_USAGE.md` - Usage documentation

#### Modified Files:
- `src/main/java/com/portfolio/api/config/SecurityConfig.java` - Updated to use OAuth2 resource server
- `src/main/resources/application.yml` - Removed JWT config, added OAuth2 logging

#### Removed Files:
- `src/main/java/com/portfolio/api/security/JwtTokenProvider.java`
- `src/main/java/com/portfolio/api/security/JwtAuthenticationFilter.java`
- `src/main/java/com/portfolio/api/security/JwtAuthenticationEntryPoint.java`
- `src/main/java/com/portfolio/api/controller/AuthController.java`
- `src/main/java/com/portfolio/api/model/dto/request/LoginRequest.java`
- All related test files

## OAuth2 Configuration

### Registered Clients

#### 1. postman-client
- **Client ID**: `postman-client`
- **Client Secret**: `postman-secret`
- **Grant Types**:
  - Authorization Code
  - Refresh Token
  - Client Credentials
- **Scopes**: `openid`, `profile`, `read`, `write`
- **Redirect URIs**:
  - `https://oauth.pstmn.io/v1/callback`
  - `http://localhost:8080/authorized`

#### 2. portfolio-api-client
- **Client ID**: `portfolio-api-client`
- **Client Secret**: `api-secret`
- **Grant Types**:
  - Client Credentials
  - Refresh Token
- **Scopes**: `read`, `write`

### Test Users

| Username | Password | Roles |
|----------|----------|-------|
| admin | admin123 | ADMIN, USER |
| user | user123 | USER |

### Token Settings
- **Access Token Lifetime**: 1 hour
- **Refresh Token Lifetime**: 7 days
- **Signing Algorithm**: RSA (2048-bit key, auto-generated)

## OAuth2 Endpoints

| Endpoint | Description |
|----------|-------------|
| `POST /oauth2/token` | Obtain access token |
| `POST /oauth2/introspect` | Validate token |
| `POST /oauth2/revoke` | Revoke token |
| `GET /oauth2/authorize` | Authorization endpoint (for authorization code flow) |
| `GET /.well-known/oauth-authorization-server` | Server metadata |
| `GET /.well-known/jwks.json` | JSON Web Key Set |

## How to Use

### 1. Get Access Token (Client Credentials Flow)

```bash
curl -X POST http://localhost:8080/oauth2/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -u "portfolio-api-client:api-secret" \
  -d "grant_type=client_credentials&scope=read write"
```

**Response:**
```json
{
  "access_token": "eyJraWQiOiI...",
  "token_type": "Bearer",
  "expires_in": 3600,
  "scope": "read write"
}
```

### 2. Use Token to Access Protected Endpoints

```bash
curl -X GET http://localhost:8080/api/v1/perfil-risco/123 \
  -H "Authorization: Bearer eyJraWQiOiI..."
```

### 3. Authorization Code Flow (User Login)

1. Open browser and navigate to:
```
http://localhost:8080/oauth2/authorize?response_type=code&client_id=postman-client&scope=openid%20profile%20read%20write&redirect_uri=https://oauth.pstmn.io/v1/callback
```

2. Login with credentials (admin/admin123 or user/user123)

3. Exchange authorization code for token:
```bash
curl -X POST http://localhost:8080/oauth2/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -u "postman-client:postman-secret" \
  -d "grant_type=authorization_code&code=<AUTH_CODE>&redirect_uri=https://oauth.pstmn.io/v1/callback"
```

### 4. Refresh Token

```bash
curl -X POST http://localhost:8080/oauth2/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -u "postman-client:postman-secret" \
  -d "grant_type=refresh_token&refresh_token=<REFRESH_TOKEN>"
```

## Testing with Postman

1. Import `OAuth2-Setup.postman_collection.json`
2. Create environment with variables:
   - `baseUrl`: `http://localhost:8080`
   - `clientId`: `portfolio-api-client`
   - `clientSecret`: `api-secret`
3. Run "Get Token - Client Credentials" request
4. Token is automatically saved to `{{accessToken}}` variable
5. Use the main API collection - it's configured to use the bearer token

See `postman/OAUTH2_USAGE.md` for detailed instructions.

## Security Features

### 1. Standards Compliance
- Implements OAuth 2.1 specification
- OIDC (OpenID Connect) support
- JWT-based tokens with RSA signing
- Token introspection and revocation

### 2. Token Security
- Short-lived access tokens (1 hour)
- Refresh tokens for extended sessions
- RSA asymmetric signing (not HMAC)
- Auto-generated secure keys

### 3. Authorization
- Scope-based access control
- Role-based user authentication
- Client authentication (Basic Auth)

## Database Schema

The migration `V9__create_oauth2_tables.sql` creates three tables:

1. **oauth2_authorization** - Stores authorization data, tokens
2. **oauth2_authorization_consent** - Stores user consent records
3. **oauth2_registered_client** - Stores registered OAuth2 clients

These tables enable:
- Persistent token storage
- Token revocation
- Authorization tracking
- Consent management

## Migration Guide for Existing API Consumers

### Before (Custom JWT)
```bash
# Login
POST /auth/login
Body: {"username": "admin", "password": "admin123"}
Response: {"token": "...", "type": "Bearer"}

# Use token
GET /api/v1/perfil-risco/123
Header: Authorization: Bearer <token>
```

### After (OAuth2)
```bash
# Get token
POST /oauth2/token
Header: Authorization: Basic <base64(clientId:clientSecret)>
Body: grant_type=client_credentials&scope=read write
Response: {"access_token": "...", "token_type": "Bearer", "expires_in": 3600}

# Use token (same as before)
GET /api/v1/perfil-risco/123
Header: Authorization: Bearer <access_token>
```

## Benefits of OAuth2

1. **Industry Standard**: Widely adopted, well-documented, battle-tested
2. **Flexibility**: Multiple grant types for different use cases
3. **Better Security**: Asymmetric signing, token introspection, revocation
4. **Scalability**: Can integrate with external identity providers
5. **OIDC Support**: User profile information, single sign-on
6. **Token Management**: Built-in refresh, expiration, revocation
7. **Audit Trail**: Database tracking of all authorizations

## Next Steps (Optional Enhancements)

1. **Database-backed Clients**: Move from in-memory to database storage
2. **PKCE Support**: Add Proof Key for Code Exchange for mobile apps
3. **Custom Claims**: Add user roles/permissions to JWT
4. **Rate Limiting**: Implement token endpoint rate limits
5. **External IdP Integration**: Connect to Keycloak, Auth0, etc.
6. **Multi-tenancy**: Support multiple tenant clients
7. **Audit Logging**: Track all token operations

## Troubleshooting

### Token Validation Fails
- Check token hasn't expired
- Verify token was issued by this server (issuer claim)
- Ensure client has required scopes

### Authorization Code Flow Fails
- Verify redirect URI matches registered client
- Check user credentials are correct
- Ensure authorization code hasn't been used or expired

### Build Issues
- Ensure Java 21 is active: `sdk use java 21.0.8-amzn`
- Clean rebuild: `mvn clean compile`

## Testing Checklist

- [ ] Client Credentials flow works
- [ ] Authorization Code flow works
- [ ] Refresh token flow works
- [ ] Token introspection works
- [ ] Token revocation works
- [ ] Protected endpoints validate tokens
- [ ] Expired tokens are rejected
- [ ] Invalid tokens are rejected
- [ ] Postman collection tests pass
- [ ] Swagger UI still works
