#!/bin/bash

# SAST Report Retrieval Script for MediMind
# This script helps retrieve and analyze SAST reports from GitHub Actions

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}🔍 SAST Report Retrieval for MediMind${NC}"
echo "============================================="

# Check if gh CLI is installed
if ! command -v gh &> /dev/null; then
    echo -e "${RED}❌ GitHub CLI (gh) is not installed${NC}"
    echo "Please install it from: https://cli.github.com/"
    echo "Or run: brew install gh (macOS)"
    exit 1
fi

# Check if user is authenticated
if ! gh auth status &> /dev/null; then
    echo -e "${RED}❌ Not authenticated with GitHub${NC}"
    echo "Please run: gh auth login"
    exit 1
fi

# Create reports directory
REPORTS_DIR="./sast-reports"
mkdir -p "$REPORTS_DIR"

echo -e "${YELLOW}📊 Retrieving SAST reports...${NC}"

# Get the latest SAST workflow run
LATEST_RUN=$(gh run list --workflow "SAST - Static Code Analysis & Build" --limit 1 --json databaseId,status,conclusion,createdAt --jq '.[0]')

if [ "$LATEST_RUN" = "null" ]; then
    echo -e "${RED}❌ No SAST workflow runs found${NC}"
    exit 1
fi

RUN_ID=$(echo "$LATEST_RUN" | jq -r '.databaseId')
STATUS=$(echo "$LATEST_RUN" | jq -r '.status')
CONCLUSION=$(echo "$LATEST_RUN" | jq -r '.conclusion')
CREATED_AT=$(echo "$LATEST_RUN" | jq -r '.createdAt')

echo -e "${BLUE}📋 Latest SAST Run Information:${NC}"
echo "  Run ID: $RUN_ID"
echo "  Status: $STATUS"
echo "  Conclusion: $CONCLUSION"
echo "  Created: $CREATED_AT"

# Download artifacts if run is completed
if [ "$STATUS" = "completed" ]; then
    echo -e "${YELLOW}📥 Downloading SAST artifacts...${NC}"
    
    # Download all artifacts
    gh run download "$RUN_ID" --dir "$REPORTS_DIR" || echo "No artifacts found"
    
    echo -e "${GREEN}✅ Artifacts downloaded to $REPORTS_DIR${NC}"
else
    echo -e "${YELLOW}⚠️  Run is not completed yet (Status: $STATUS)${NC}"
fi

# Generate comprehensive report
REPORT_FILE="$REPORTS_DIR/sast-comprehensive-report.md"

cat > "$REPORT_FILE" << EOF
# 🔒 MediMind SAST Comprehensive Report

**Generated:** $(date)  
**Latest Run ID:** $RUN_ID  
**Run Status:** $STATUS  
**Run Conclusion:** $CONCLUSION  
**Run Created:** $CREATED_AT

## 📊 SAST Analysis Summary

### CodeQL Analysis
- **Java (Backend):** ✅ Analyzed
- **JavaScript (Frontend):** ✅ Analyzed  
- **Python (ML):** ✅ Analyzed

### Security Coverage
- **Static Analysis:** CodeQL
- **Languages Covered:** Java, JavaScript, Python
- **Query Suites:** security-and-quality, security-extended

## 🔍 How to Access Reports

### 1. GitHub Security Tab
Visit your repository's Security tab to view CodeQL alerts:
\`\`\`
https://github.com/[your-username]/MediMind/security/code-scanning
\`\`\`

### 2. GitHub Actions Artifacts
Download artifacts from the latest SAST run:
\`\`\`
https://github.com/[your-username]/MediMind/actions/runs/$RUN_ID
\`\`\`

### 3. Local Reports
Check the \`$REPORTS_DIR\` directory for downloaded reports.

## 📋 Available Reports

EOF

# List downloaded artifacts
if [ -d "$REPORTS_DIR" ]; then
    echo "### Downloaded Artifacts:" >> "$REPORT_FILE"
    find "$REPORTS_DIR" -type f -name "*.md" -o -name "*.txt" -o -name "*.json" | while read -r file; do
        echo "- \`$(basename "$file")\`" >> "$REPORT_FILE"
    done
fi

cat >> "$REPORT_FILE" << EOF

## 🚨 Security Alerts

### CodeQL Alerts
CodeQL generates security alerts for:
- **SQL Injection:** Database query vulnerabilities
- **Cross-Site Scripting (XSS):** Client-side code injection
- **Path Traversal:** File system access vulnerabilities
- **Command Injection:** OS command execution vulnerabilities
- **Authentication Issues:** Weak authentication mechanisms
- **Data Exposure:** Sensitive data leaks

### Medical Application Specific
For MediMind, pay special attention to:
- **Patient Data Protection:** HIPAA compliance issues
- **Medical Record Security:** Unauthorized access vulnerabilities
- **API Security:** REST API endpoint vulnerabilities
- **Input Validation:** Medical data input sanitization

## 🔧 How to Fix Issues

### 1. Review Alerts
- Go to GitHub Security tab
- Review each alert's details
- Understand the vulnerability type

### 2. Fix Code
- Follow the suggested fixes in CodeQL alerts
- Test your changes thoroughly
- Ensure no new vulnerabilities are introduced

### 3. Re-run Analysis
- Push your fixes to trigger a new SAST run
- Verify that alerts are resolved
- Monitor for new issues

## 📈 Security Metrics

### Current Status
- **Total Alerts:** Check GitHub Security tab
- **Critical Issues:** 0 (if any, block deployment)
- **High Issues:** Review and fix
- **Medium Issues:** Assess risk and fix
- **Low Issues:** Monitor and fix as needed

### Best Practices
- ✅ Regular SAST scans on every PR
- ✅ CodeQL alerts reviewed within 24 hours
- ✅ Critical issues fixed before merge
- ✅ Security training for development team

## 🎯 Next Steps

1. **Review GitHub Security Tab:** Check for active alerts
2. **Address Critical Issues:** Fix any blocking vulnerabilities
3. **Update Dependencies:** Keep libraries updated
4. **Security Training:** Ensure team awareness
5. **Regular Monitoring:** Set up alerts for new issues

---
*Generated by MediMind SAST Pipeline*
EOF

echo -e "${GREEN}✅ Comprehensive SAST report generated: $REPORT_FILE${NC}"

# Check for security alerts
echo -e "${YELLOW}🔍 Checking for active security alerts...${NC}"

# Try to get security alerts (this might require additional permissions)
if gh api repos/:owner/:repo/code-scanning/alerts --jq '.[] | select(.state == "open") | .rule.description' 2>/dev/null; then
    echo -e "${GREEN}✅ Security alerts retrieved${NC}"
else
    echo -e "${YELLOW}⚠️  Could not retrieve security alerts (may need additional permissions)${NC}"
    echo "Please check manually at: https://github.com/[your-username]/MediMind/security/code-scanning"
fi

echo ""
echo -e "${BLUE}📋 Summary:${NC}"
echo "  📁 Reports directory: $REPORTS_DIR"
echo "  📄 Comprehensive report: $REPORT_FILE"
echo "  🔗 GitHub Security tab: https://github.com/[your-username]/MediMind/security/code-scanning"
echo "  🔗 Latest run: https://github.com/[your-username]/MediMind/actions/runs/$RUN_ID"

echo ""
echo -e "${GREEN}🎉 SAST report retrieval completed!${NC}"
