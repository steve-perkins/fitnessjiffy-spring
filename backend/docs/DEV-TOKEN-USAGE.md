# Development Token Generation for Manual API Testing

## Overview

For local development and manual API testing, the backend provides a development-only endpoint for generating JWT tokens without requiring Google OAuth. This is much more convenient than copying tokens from the HTML test page.

## Important Security Note

⚠️ **This endpoint is ONLY available in development environments** and will return a 401 Unauthorized error in production.

## Using the Dev Token Endpoint

### Prerequisites

1. Backend server running locally (default: http://localhost:3000)
2. Valid user email in the database (e.g., the email migrated from MySQL)

### Method 1: Using curl

```bash
# Generate JWT token for your user
curl -X POST http://localhost:3000/auth/dev-token \
  -H "Content-Type: application/json" \
  -d '{"email":"your-email@gmail.com"}' \
  | jq -r '.access_token'
```

**Pro tip**: Save the token to an environment variable:

```bash
# Generate and save token
export JWT_TOKEN=$(curl -s -X POST http://localhost:3000/auth/dev-token \
  -H "Content-Type: application/json" \
  -d '{"email":"your-email@gmail.com"}' \
  | jq -r '.access_token')

# Use the token in subsequent requests
curl -H "Authorization: Bearer $JWT_TOKEN" http://localhost:3000/users/me
```

### Method 2: Using HTTPie

```bash
# Generate token
http POST http://localhost:3000/auth/dev-token email=your-email@gmail.com

# Save token to variable
export JWT_TOKEN=$(http POST http://localhost:3000/auth/dev-token email=your-email@gmail.com --print=b | jq -r '.access_token')

# Use token
http GET http://localhost:3000/users/me Authorization:"Bearer $JWT_TOKEN"
```

### Method 3: Using Postman/Insomnia

1. **Create a POST request**: `http://localhost:3000/auth/dev-token`
2. **Headers**:
   - `Content-Type: application/json`
3. **Body** (JSON):
   ```json
   {
     "email": "your-email@gmail.com"
   }
   ```
4. **Send** → Copy the `access_token` from the response
5. **Use the token**:
   - In subsequent requests, add header: `Authorization: Bearer <token>`

### Method 4: Using VS Code REST Client Extension

Create a `.http` file:

```http
### Generate Dev Token
POST http://localhost:3000/auth/dev-token
Content-Type: application/json

{
  "email": "your-email@gmail.com"
}

### Store token in variable (after generating above)
@token = <paste-token-here>

### Use token to get user profile
GET http://localhost:3000/users/me
Authorization: Bearer {{token}}

### Get weights
GET http://localhost:3000/weights
Authorization: Bearer {{token}}

### Create weight
POST http://localhost:3000/weights
Authorization: Bearer {{token}}
Content-Type: application/json

{
  "date": "2026-04-18T00:00:00.000Z",
  "pounds": 180
}
```

## Response Format

```json
{
  "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": "uuid-here",
    "email": "your-email@gmail.com",
    "firstName": "Your",
    "lastName": "Name"
  }
}
```

## Token Lifespan

- **Duration**: 7 days
- **Payload**: Contains user ID, email, first name, and last name
- **Algorithm**: HS256 (HMAC SHA-256)

## Example API Calls with Token

### Get User Profile
```bash
curl -H "Authorization: Bearer $JWT_TOKEN" \
  http://localhost:3000/users/me
```

### Get Weights
```bash
curl -H "Authorization: Bearer $JWT_TOKEN" \
  http://localhost:3000/weights
```

### Create Weight
```bash
curl -X POST http://localhost:3000/weights \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "date": "2026-04-18T00:00:00.000Z",
    "pounds": 180
  }'
```

### Search Foods
```bash
curl -H "Authorization: Bearer $JWT_TOKEN" \
  "http://localhost:3000/foods/search?q=chicken"
```

### Add Food Eaten
```bash
curl -X POST http://localhost:3000/foods/eaten \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "foodId": "<food-uuid>",
    "date": "2026-04-18T00:00:00.000Z",
    "servingType": 1,
    "servingQty": 2
  }'
```

### Get Report Entries
```bash
curl -H "Authorization: Bearer $JWT_TOKEN" \
  "http://localhost:3000/report-entries?startDate=2026-04-11T00:00:00.000Z&endDate=2026-04-18T00:00:00.000Z"
```

## Automated Testing

E2E tests use this same endpoint to generate tokens programmatically:

```typescript
const authResponse = await request(app.getHttpServer())
  .post('/auth/dev-token')
  .send({ email: 'test@example.com' })
  .expect(200);

const jwtToken = authResponse.body.access_token;

// Use token in subsequent test requests
await request(app.getHttpServer())
  .get('/users/me')
  .set('Authorization', `Bearer ${jwtToken}`)
  .expect(200);
```

## Troubleshooting

### 401 Unauthorized - "User not found"
- **Problem**: Email doesn't exist in database
- **Solution**: Check your user email in the database or use the email from the Phase 1 migration

### 401 Unauthorized - "Test token generation is disabled in production"
- **Problem**: `NODE_ENV=production` is set
- **Solution**: Unset `NODE_ENV` or set it to `development`

### Token expired
- **Problem**: Token is older than 7 days
- **Solution**: Generate a new token using the dev-token endpoint

## Production Authentication

In production, this endpoint is **disabled**. Users must authenticate via:

1. Google OAuth (POST /auth/google with Google ID token)
2. Frontend handles OAuth popup and exchanges Google token for backend JWT
3. Backend verifies Google token and issues its own 7-day JWT

See `test-google-auth.html` for a working example of the production OAuth flow.
