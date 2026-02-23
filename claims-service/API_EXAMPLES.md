# API Examples and Testing Guide

This document provides sample API requests using cURL and instructions for testing with Postman.

## Authentication

All API endpoints (except `/` and `/actuator/health`) require HTTP Basic Authentication.

**Credentials**:
- Username: `ginja`
- Password: `claims2024`

## cURL Examples

### 1. Health Check (No Auth Required)

```bash
curl -X GET http://localhost:8080/actuator/health
```

**Expected Response**:
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP"
    },
    "diskSpace": {
      "status": "UP"
    }
  }
}
```

### 2. Welcome Endpoint (No Auth Required)

```bash
curl -X GET http://localhost:8080/
```

**Expected Response**:
```json
{
  "application": "Claims Service API",
  "version": "0.0.1-SNAPSHOT",
  "status": "running",
  "endpoints": {
    "GET /claims": "Get all claims",
    "GET /claims/{id}": "Get a claim by ID",
    "POST /claims": "Create a new claim"
  }
}
```

### 3. Submit a Claim - Approved Scenario

**Active member (M123) with valid claim amount**

```bash
curl -X POST http://localhost:8080/claims \
  -u ginja:claims2024 \
  -H "Content-Type: application/json" \
  -d '{
    "memberId": "M123",
    "providerId": "H456",
    "diagnosisCode": "D001",
    "procedureCode": "P001",
    "claimAmount": 30000
  }'
```

**Expected Response**:
```json
{
  "claimId": "550e8400-e29b-41d4-a716-446655440000",
  "status": "APPROVED",
  "fraudFlag": false,
  "approvedAmount": 30000.0
}
```

### 4. Submit a Claim - Partial Approval

**Claim amount exceeds max benefit (40,000)**

```bash
curl -X POST http://localhost:8080/claims \
  -u ginja:claims2024 \
  -H "Content-Type: application/json" \
  -d '{
    "memberId": "M123",
    "providerId": "H456",
    "diagnosisCode": "D001",
    "procedureCode": "P001",
    "claimAmount": 50000
  }'
```

**Expected Response**:
```json
{
  "claimId": "660e8400-e29b-41d4-a716-446655440001",
  "status": "PARTIAL",
  "fraudFlag": true,
  "approvedAmount": 40000.0
}
```

**Note**: Fraud flag is true because 50,000 > 2 × 20,000 (procedure P001 cost)

### 5. Submit a Claim - Rejected

**Inactive member (M999)**

```bash
curl -X POST http://localhost:8080/claims \
  -u ginja:claims2024 \
  -H "Content-Type: application/json" \
  -d '{
    "memberId": "M999",
    "providerId": "H456",
    "diagnosisCode": "D001",
    "procedureCode": "P001",
    "claimAmount": 30000
  }'
```

**Expected Response**:
```json
{
  "claimId": "770e8400-e29b-41d4-a716-446655440002",
  "status": "REJECTED",
  "fraudFlag": false,
  "approvedAmount": 0.0
}
```

### 6. Submit a Claim - Fraud Detection

**Claim amount significantly exceeds procedure cost**

```bash
curl -X POST http://localhost:8080/claims \
  -u ginja:claims2024 \
  -H "Content-Type: application/json" \
  -d '{
    "memberId": "M456",
    "providerId": "H456",
    "diagnosisCode": "D002",
    "procedureCode": "P002",
    "claimAmount": 35000
  }'
```

**Expected Response**:
```json
{
  "claimId": "880e8400-e29b-41d4-a716-446655440003",
  "status": "APPROVED",
  "fraudFlag": true,
  "approvedAmount": 35000.0
}
```

**Note**: P002 costs 15,000, and 35,000 > 2 × 15,000, triggering fraud flag

### 7. Get All Claims

```bash
curl -X GET http://localhost:8080/claims \
  -u ginja:claims2024
```

**Expected Response**:
```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "memberId": "M123",
    "providerId": "H456",
    "diagnosisCode": "D001",
    "procedureCode": "P001",
    "claimAmount": 30000.0,
    "approvedAmount": 30000.0,
    "status": "APPROVED",
    "fraudFlag": false,
    "createdAt": "2026-02-23T14:30:00"
  },
  {
    "id": "660e8400-e29b-41d4-a716-446655440001",
    "memberId": "M123",
    "providerId": "H456",
    "diagnosisCode": "D001",
    "procedureCode": "P001",
    "claimAmount": 50000.0,
    "approvedAmount": 40000.0,
    "status": "PARTIAL",
    "fraudFlag": true,
    "createdAt": "2026-02-23T14:31:00"
  }
]
```

### 8. Get Claim by ID

```bash
# Replace {claimId} with actual claim ID from previous responses
curl -X GET http://localhost:8080/claims/550e8400-e29b-41d4-a716-446655440000 \
  -u ginja:claims2024
```

**Expected Response**:
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "memberId": "M123",
  "providerId": "H456",
  "diagnosisCode": "D001",
  "procedureCode": "P001",
  "claimAmount": 30000.0,
  "approvedAmount": 30000.0,
  "status": "APPROVED",
  "fraudFlag": false,
  "createdAt": "2026-02-23T14:30:00"
}
```

### 9. Invalid Request - Missing Required Fields

```bash
curl -X POST http://localhost:8080/claims \
  -u ginja:claims2024 \
  -H "Content-Type: application/json" \
  -d '{
    "memberId": "M123",
    "claimAmount": 30000
  }'
```

**Expected Response** (400 Bad Request):
```json
{
  "timestamp": "2026-02-23T14:35:00",
  "status": 400,
  "error": "Validation Failed",
  "validationErrors": {
    "providerId": "must not be null",
    "diagnosisCode": "must not be null",
    "procedureCode": "must not be null"
  }
}
```

### 10. Claim Not Found

```bash
curl -X GET http://localhost:8080/claims/invalid-id \
  -u ginja:claims2024
```

**Expected Response** (404 Not Found):
```json
{
  "timestamp": "2026-02-23T14:36:00",
  "status": 404,
  "error": "Not Found",
  "message": "Claim not found"
}
```

### 11. Unauthorized Access

```bash
curl -X GET http://localhost:8080/claims
```

**Expected Response** (401 Unauthorized):
```json
{
  "timestamp": "2026-02-23T14:37:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Full authentication is required to access this resource"
}
```

## PowerShell Examples (Windows)

### Submit a Claim

```powershell
$headers = @{
    "Content-Type" = "application/json"
}

$body = @{
    memberId = "M123"
    providerId = "H456"
    diagnosisCode = "D001"
    procedureCode = "P001"
    claimAmount = 30000
} | ConvertTo-Json

$credentials = [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes("ginja:claims2024"))
$headers["Authorization"] = "Basic $credentials"

Invoke-RestMethod -Uri "http://localhost:8080/claims" `
    -Method Post `
    -Headers $headers `
    -Body $body
```

### Get All Claims

```powershell
$credentials = [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes("ginja:claims2024"))
$headers = @{
    "Authorization" = "Basic $credentials"
}

Invoke-RestMethod -Uri "http://localhost:8080/claims" `
    -Method Get `
    -Headers $headers
```

## Postman Collection

### Import to Postman

1. Create a new Collection named "Ginja Claims Service"
2. Add the following requests:

#### Collection Variables
- `baseUrl`: `http://localhost:8080`
- `username`: `ginja`
- `password`: `claims2024`

#### Collection Authorization
- Type: Basic Auth
- Username: `{{username}}`
- Password: `{{password}}`

#### Requests

**1. Health Check**
- Method: `GET`
- URL: `{{baseUrl}}/actuator/health`
- Auth: No Auth

**2. Welcome**
- Method: `GET`
- URL: `{{baseUrl}}/`
- Auth: No Auth

**3. Submit Claim - Approved**
- Method: `POST`
- URL: `{{baseUrl}}/claims`
- Body (JSON):
```json
{
  "memberId": "M123",
  "providerId": "H456",
  "diagnosisCode": "D001",
  "procedureCode": "P001",
  "claimAmount": 30000
}
```

**4. Submit Claim - Partial**
- Method: `POST`
- URL: `{{baseUrl}}/claims`
- Body (JSON):
```json
{
  "memberId": "M123",
  "providerId": "H456",
  "diagnosisCode": "D001",
  "procedureCode": "P001",
  "claimAmount": 50000
}
```

**5. Submit Claim - Rejected**
- Method: `POST`
- URL: `{{baseUrl}}/claims`
- Body (JSON):
```json
{
  "memberId": "M999",
  "providerId": "H456",
  "diagnosisCode": "D001",
  "procedureCode": "P001",
  "claimAmount": 30000
}
```

**6. Get All Claims**
- Method: `GET`
- URL: `{{baseUrl}}/claims`

**7. Get Claim by ID**
- Method: `GET`
- URL: `{{baseUrl}}/claims/{{claimId}}`
- Note: Set `claimId` variable from previous response

### Postman Tests

Add these tests to your requests for automated validation:

```javascript
// For Submit Claim requests
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Response has claimId", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData.claimId).to.exist;
    pm.environment.set("claimId", jsonData.claimId);
});

pm.test("Response has status", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData.status).to.be.oneOf(["APPROVED", "PARTIAL", "REJECTED"]);
});

// For Get Claims requests
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Response is an array", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData).to.be.an('array');
});
```

## Test Scenarios

### Scenario 1: Happy Path - Full Approval
1. Submit claim for active member (M123) with amount 30,000
2. Verify status is "APPROVED"
3. Verify approvedAmount equals claimAmount
4. Verify fraudFlag is false
5. Get claim by ID and verify details match

### Scenario 2: Benefit Limit Cap
1. Submit claim for active member (M123) with amount 50,000
2. Verify status is "PARTIAL"
3. Verify approvedAmount is 40,000 (max benefit)
4. Verify fraudFlag is true (exceeds 2x procedure cost)

### Scenario 3: Member Validation
1. Submit claim for inactive member (M999)
2. Verify status is "REJECTED"
3. Verify approvedAmount is 0
4. Verify claim is saved in database

### Scenario 4: Fraud Detection
1. Submit claim with amount > 2× procedure cost
2. Verify fraudFlag is set to true
3. Verify claim still processes with appropriate status

### Scenario 5: Data Validation
1. Submit claim with missing required fields
2. Verify 400 Bad Request response
3. Verify validation error details in response

### Scenario 6: Security
1. Attempt to access protected endpoint without auth
2. Verify 401 Unauthorized response
3. Access with valid credentials
4. Verify 200 OK response

## Mock Data Reference

### Active Members
- `M123` - Active
- `M456` - Active
- `M789` - Active
- Any other ID - Inactive

### Procedure Costs
- `P001` - KES 20,000
- `P002` - KES 15,000
- `P003` - KES 100,000
- Any other code - KES 20,000 (default)

### Business Rules
- **Max Benefit**: KES 40,000
- **Fraud Threshold**: Claim amount > 2× procedure cost

## Troubleshooting

### Connection Refused
- Ensure the application is running: `./mvnw spring-boot:run`
- Check the port 8080 is not in use
- Verify application started successfully in logs

### Authentication Fails
- Check credentials: username `ginja`, password `claims2024`
- Ensure Basic Auth header is properly formatted
- Try encoding credentials manually

### 400 Bad Request
- Verify all required fields are present
- Check JSON syntax is valid
- Ensure field names match exactly (case-sensitive)

### 404 Not Found on Claim ID
- Verify the claim ID exists by listing all claims first
- Use the exact UUID from the create response
- Remember: H2 database is in-memory, data resets on restart

---

**Happy Testing! 🚀**
