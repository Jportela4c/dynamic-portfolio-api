# OAuth2 Authentication Guide

This API now uses OAuth2 for authentication instead of custom JWT tokens.

## Quick Start

### 1. Import Collections

Import both Postman collections:
- `OAuth2-Setup.postman_collection.json` - Token management
- `Dynamic-Portfolio-API.postman_collection.json` - API endpoints

### 2. Set Environment Variables

Create a Postman environment with these variables:
```
baseUrl = http://localhost:8080
clientId = portfolio-api-client
clientSecret = api-secret
```

### 3. Get Access Token

#### Option A: Client Credentials Flow (Recommended for Testing)

1. Open the "OAuth2-Setup" collection
2. Run "Get Token - Client Credentials"
3. Token is automatically saved to environment variable `accessToken`

#### Option B: Authorization Code Flow (User Authentication)

1. Open browser and navigate to:
   ```
   http://localhost:8080/oauth2/authorize?response_type=code&client_id=postman-client&scope=openid%20profile%20read%20write&redirect_uri=https://oauth.pstmn.io/v1/callback
   ```

2. Login with credentials:
   - Username: `admin`
   - Password: `admin123`

3. Copy the authorization code from redirect URL

4. In Postman environment, set: `authorizationCode = <copied_code>`

5. Run "Get Token - Authorization Code (Manual)"

6. Tokens are automatically saved to environment

### 4. Use Token with API Requests

The main API collection is configured to use `{{accessToken}}` automatically.

Just run any endpoint in the "Dynamic-Portfolio-API" collection after getting a token.

## Available OAuth2 Endpoints

| Endpoint | Description |
|----------|-------------|
| `POST /oauth2/token` | Get access token |
| `POST /oauth2/introspect` | Validate token |
| `POST /oauth2/revoke` | Revoke token |
| `GET /.well-known/oauth-authorization-server` | Server metadata |

## Registered Clients

### postman-client
- **Client ID**: `postman-client`
- **Client Secret**: `postman-secret`
- **Grants**: Authorization Code, Refresh Token, Client Credentials
- **Scopes**: openid, profile, read, write

### portfolio-api-client
- **Client ID**: `portfolio-api-client`
- **Client Secret**: `api-secret`
- **Grants**: Client Credentials, Refresh Token
- **Scopes**: read, write

## Test Users

| Username | Password | Roles |
|----------|----------|-------|
| admin | admin123 | ADMIN, USER |
| user | user123 | USER |

## Token Lifetimes

- **Access Token**: 1 hour
- **Refresh Token**: 7 days

## Troubleshooting

### Token Expired
Run the "Refresh Token" request in the OAuth2-Setup collection.

### Invalid Client
Check that `clientId` and `clientSecret` environment variables are correct.

### Unauthorized
Get a new token using one of the flows above.

## OAuth2 Grant Types Explained

### Client Credentials
- Best for machine-to-machine communication
- No user interaction required
- Returns only access token

### Authorization Code
- Best for user authentication
- Requires browser interaction
- Returns access token + refresh token
- Most secure for web applications

### Refresh Token
- Used to get new access token when expired
- Requires valid refresh token from authorization code flow
