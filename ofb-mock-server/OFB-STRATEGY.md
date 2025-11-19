# OFB Integration Strategy - Complete Analysis

## Executive Summary

After thorough research of the Open Finance Brasil (OFB) ecosystem, here's the complete strategy for safely integrating OFB specifications into our mock server.

## Key Findings

### 1. Official Hosting & Sources

**Primary Source:**
- GitHub Repository: `https://github.com/OpenBanking-Brasil/openapi`
- GitHub Pages (Swagger UI): `https://openbanking-brasil.github.io/openapi/`
- Official Contact: gt-interfaces@openbankingbr.org

**Licensing:**
- **Apache 2.0 License** (confirmed in spec files)
- Specs are publicly available and free to use
- Attribution required

**Governance:**
- Maintained by Brazilian Central Bank regulatory body
- Active development (990+ commits, 30+ contributors)
- Last update: 4 days ago (Nov 14, 2025)

### 2. Version Analysis

**Available Versions Per Investment Type:**

| Investment Type | Latest Stable | Beta Versions | Total Versions |
|----------------|---------------|---------------|----------------|
| Bank Fixed Incomes | 1.0.4 | 1.1.0-beta.1 | 9 versions |
| Funds | 1.0.2 | 1.1.0-beta.1 | 7 versions |
| Treasure Titles | 1.0.2 | - | 6 versions |
| Variable Incomes | 1.2.1 | 1.3.0-beta.1 | 9 versions |
| Credit Fixed Incomes | 1.0.3 | - | 8 versions |

**Version Strategy Decision:**
- ✅ **Use LATEST STABLE** (non-RC, non-beta) versions
- ❌ **Avoid beta versions** for production mock server
- ✅ Current selections are correct (1.0.4, 1.0.2, 1.0.2, 1.2.1, 1.0.3)

### 3. Addressing "What if GitHub deletes it?"

**Multiple Mitigation Strategies:**

#### Strategy 1: Commit SHA Pinning (RECOMMENDED)
**Pros:**
- Immutable - will never change
- GitHub maintains git objects indefinitely
- Survives branch deletions

**Cons:**
- Less readable URLs
- Manual process to update versions

**Implementation:**
```xml
<!-- Use commit SHA instead of 'main' branch -->
<url>https://raw.githubusercontent.com/OpenBanking-Brasil/openapi/a1bb907e3391d58d89c49ef0e0242a58e435bac8/swagger-apis/bank-fixed-incomes/1.0.4.yml</url>
```

**Commit SHAs for Current Versions:**
- Bank Fixed Incomes 1.0.4: `a1bb907e3391d58d89c49ef0e0242a58e435bac8` (2024-11-12)
- Funds 1.0.2: `288e17281c052b32d9cf283c8d957e5e6b512a7d` (2023-09-27)
- Treasure Titles 1.0.2: `0d9a65e7ce1dd796735fd7cb52bd50b5c357fafc` (2025-05-29)
- Variable Incomes 1.2.1: `309904e810dffa41cc2beb05922ff065110e3eb5` (2025-01-07)
- Credit Fixed Incomes 1.0.3: `e85c2c3eac140f6a1668d252904df560e26a464c` (2024-11-12)

#### Strategy 2: Fork the Repository
**Pros:**
- Full control over availability
- Can apply custom patches if needed
- Immune to upstream deletion

**Cons:**
- Must maintain fork
- Not clearly attributing to official source
- Extra maintenance burden

**Implementation:**
1. ✅ **COMPLETED**: Forked to `https://github.com/Jportela4c/openapi-ofb-backup`
2. Fork available as fallback source
3. Can periodically sync with upstream using `gh repo sync Jportela4c/openapi-ofb-backup`

#### Strategy 3: Local Vendoring + Git Submodule
**Pros:**
- Complete offline capability
- Version controlled with project
- No runtime dependency on GitHub

**Cons:**
- Duplicates specs in repository (violates earlier decision)
- Harder to update
- License attribution required in README

#### Strategy 4: Multi-Source Fallback
**Pros:**
- Highest availability
- Automatic failover

**Cons:**
- Complex configuration
- Multiple URLs to maintain

**Implementation:**
```xml
<configuration>
    <url>https://raw.githubusercontent.com/OpenBanking-Brasil/openapi/main/swagger-apis/bank-fixed-incomes/1.0.4.yml</url>
    <!-- Fallback to GitHub Pages -->
    <url>https://openbanking-brasil.github.io/openapi/swagger-apis/bank-fixed-incomes/1.0.4.yml</url>
</configuration>
```

## Recommended Approach

**PRIMARY: Commit SHA Pinning + Fork Backup** ✅ **IMPLEMENTED**

1. ✅ **COMPLETED**: pom.xml updated to use commit SHAs instead of `main` branch
2. ✅ **COMPLETED**: Fork created at `https://github.com/Jportela4c/openapi-ofb-backup`
3. ✅ **COMPLETED**: Commit SHAs documented in this file
4. **Ongoing**: Periodically check for new stable versions

### Update Process

When updating to newer spec versions:

1. Check GitHub for new releases
2. Verify it's stable (not RC/beta)
3. Get commit SHA: `git log --oneline swagger-apis/bank-fixed-incomes/1.0.5.yml | head -1`
4. Update pom.xml with new SHA and version
5. Run tests to verify compatibility
6. Commit with message: `chore: update OFB specs to v1.0.5 (commit: abc1234)`

## Repository Deletion Risk Assessment

**Likelihood: VERY LOW**

Reasons:
1. **Regulatory Requirement**: Brazilian Central Bank mandates these specs
2. **Production Use**: Hundreds of financial institutions depend on these
3. **Legal Framework**: Part of Brazilian financial system infrastructure
4. **Active Maintenance**: Recent commits, active development
5. **Public Interest**: Open banking is government-backed initiative

**Impact if Deleted: MEDIUM**

Mitigation:
- Specs cached in Maven local repository (~/.m2/repository)
- Specs cached in CI/CD build cache
- Fork backup available
- Worst case: specs are in generated code, can extract and vendor

## Legal/Licensing Summary

- **License**: Apache 2.0 (permissive, commercial use allowed)
- **Attribution Required**: Yes (already in generated code headers)
- **Modifications Allowed**: Yes
- **Redistribution Allowed**: Yes (with license copy)
- **Patent Grant**: Yes (Apache 2.0 includes patent grant)

## Compliance Checklist

- [x] Using Apache 2.0 licensed specs
- [x] Attribution present in generated code
- [x] Not modifying specs (using as-is)
- [x] Using latest stable versions
- [x] Avoiding beta/RC versions
- [x] Build-time download (not committed to repo)
- [x] Pin to commit SHAs (implemented in pom.xml)
- [x] Create fork backup (https://github.com/Jportela4c/openapi-ofb-backup)
- [x] Document SHA update process (see Update Process section)

## References

- OFB GitHub (Upstream): https://github.com/OpenBanking-Brasil/openapi
- OFB Fork (Backup): https://github.com/Jportela4c/openapi-ofb-backup
- OFB Swagger UI: https://openbanking-brasil.github.io/openapi/
- Brazilian Central Bank: https://www.bcb.gov.br/estabilidadefinanceira/openbanking
- Apache 2.0 License: https://www.apache.org/licenses/LICENSE-2.0
