# Newman Test Known Issues

**Status**: 68/82 tests passing (83% pass rate)
**Date**: 2025-11-17

## Fixed Issues ✅

1. **Invalid product type enum** - Now returns 400 Bad Request instead of 500 Internal Server Error
   - Fixed in: `GlobalExceptionHandler.java`
   - Commit: Added `HttpMessageNotReadableException` handler

## Remaining Issues (14 failures)

### API Bugs (SPEC Violations)

#### 1. Authentication - No user validation ⚠️ **HIGH PRIORITY**
**Issue**: `POST /auth/login` generates JWT tokens for ANY username, even non-existent users
**SPEC Requirement**: Should return 401 for invalid credentials
**Current Behavior**: Always returns 200 with valid JWT token
**Impact**: Critical security vulnerability

**Root Cause**: `AuthController.java` has no user validation - just generates token from username directly

**Fix Required**:
- Create User entity and repository
- Implement user authentication service
- Validate username/password against database
- Return 401 for invalid credentials
- Update test data SQL to hash passwords properly

**Test Affected**: `Login - Invalid Credentials`

---

### Test Assertion Updates Needed (SPEC Compliance)

These tests fail because they expect different field names than what THE SPEC actually specifies. The API is correct per SPEC, tests need updating:

#### 2. GET /simulacoes - Field name mismatch
**SPEC says**: `"valorInvestido": 10000.00`
**Test expects**: `"valor"`
**Fix**: Update Postman collection test assertion line for this endpoint

**Test Code to Change**:
```javascript
// Current (WRONG):
pm.expect(firstItem).to.have.property('valor');

// Should be (CORRECT per SPEC):
pm.expect(firstItem).to.have.property('valorInvestido');
```

---

#### 3. GET /simulacoes/por-produto-dia - Field name mismatch
**SPEC says**: `"mediaValorFinal": 11050.00`
**Test expects**: `"valorMedioFinal"`
**Fix**: Update Postman collection test assertion

**Test Code to Change**:
```javascript
// Current (WRONG):
pm.expect(firstItem).to.have.property('valorMedioFinal');

// Should be (CORRECT per SPEC):
pm.expect(firstItem).to.have.property('mediaValorFinal');
```

---

#### 4. GET /telemetria - Response structure mismatch
**SPEC says**: `{ "servicos": [...], "periodo": {...} }` (wrapper object)
**Test expects**: Array directly
**Fix**: Update Postman collection test assertion

**Test Code to Change**:
```javascript
// Current (WRONG):
pm.test("Response has telemetry data", () => {
    const json = pm.response.json();
    pm.expect(json).to.be.an('array');
});

// Should be (CORRECT per SPEC):
pm.test("Response has telemetry data", () => {
    const json = pm.response.json();
    pm.expect(json).to.have.property('servicos');
    pm.expect(json.servicos).to.be.an('array');
    pm.expect(json).to.have.property('periodo');
});
```

---

### Other Test Failures

#### 5. Login response - Missing `username` field
**Issue**: Response only contains `token` and `type`, not `username`
**Impact**: Minor - not in SPEC requirement
**Fix**: Either add username to response OR remove test assertion (username not required by SPEC)

---

#### 6. Validation error response format
**Issue**: Validation errors return different structure than expected
**Current**: `{ "status": 400, "message": "...", "errors": {...} }`
**Test expects**: Direct field error properties

**Investigation needed**: Check if SPEC defines validation error format

---

#### 7. Simulation result fields
**Issue**: Tests checking for `valorInicial`, `lucro`, `rentabilidadePercentual`
**SPEC shows**: `resultadoSimulacao` should have `valorFinal`, `rentabilidadeEfetiva`, `prazoMeses`

**Need to verify**: Does API return all SPEC-required fields?

---

#### 8. Boundary value test failures
**Issue**: Minimum value product not found (404)
**Cause**: Test data may not have products with R$ 1,000 minimum
**Fix**: Either add suitable products to test data OR adjust test expectations

---

#### 9. Large value test - 500 error
**Issue**: Simulating R$ 1,000,000 investment returns 500 error
**Cause**: Needs investigation - possible overflow or database constraint
**Fix**: Debug why large values fail

---

## Summary

### Must Fix (Blockers)
1. ✅ ~~Invalid enum handling~~ (FIXED)
2. ⚠️ Authentication validation (security issue)

### Should Fix (SPEC Compliance)
3. Update GET /simulacoes test assertion (field name)
4. Update GET /simulacoes/por-produto-dia test assertion (field name)
5. Update GET /telemetria test assertion (response structure)

### Nice to Fix
6. Login response username field
7. Validation error format investigation
8. Simulation response fields verification
9. Boundary value test data
10. Large value handling

## Quick Fix Commands

```bash
# Regenerate Postman collection with corrected assertions
# Edit: postman/Dynamic-Portfolio-API.postman_collection.json

# Fields to search and replace:
1. Find: "to.have.property('valor')" in GET /simulacoes test
   Replace with: "to.have.property('valorInvestido')"

2. Find: "to.have.property('valorMedioFinal')" in GET /simulacoes/por-produto-dia test
   Replace with: "to.have.property('mediaValorFinal')"

3. Find telemetry test expecting array
   Replace with: test expecting object with 'servicos' property
```

## Test Again

After fixes:
```bash
newman run postman/Dynamic-Portfolio-API.postman_collection.json \
  -e postman/environments/local-docker.postman_environment.json \
  --reporters cli
```

Expected result after quick fixes: **~75-78/82 passing** (92-95%)
After authentication fix: **~80/82 passing** (98%)

---

## References

- SPEC: `local-docs/00-challenge-specification-original.md`
- Test Plan: `local-docs/05-test-refactor-plan.md`
- Exception Handler: `src/main/java/com/portfolio/api/exception/GlobalExceptionHandler.java`
- Auth Controller: `src/main/java/com/portfolio/api/controller/AuthController.java`
