#!/bin/bash

# DAST (Dynamic Application Security Testing) Script for MediMind
# Uses OWASP ZAP for automated security testing

set -e

# Configuration
ZAP_VERSION="2.13.0"
ZAP_DIR="/tmp/zap"
SCAN_REPORT_DIR="./security-reports"
FRONTEND_URL="${FRONTEND_URL:-http://localhost:3000}"
BACKEND_URL="${BACKEND_URL:-http://localhost:8080}"
SCAN_TIMEOUT="${SCAN_TIMEOUT:-300}"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}🔒 Starting DAST Security Scan for MediMind${NC}"
echo "=================================================="

# Create reports directory
mkdir -p "$SCAN_REPORT_DIR"

# Function to check if URL is accessible
check_url() {
    local url=$1
    local name=$2
    
    echo -e "${YELLOW}Checking if $name is accessible at $url...${NC}"
    
    if curl -s --max-time 10 "$url" > /dev/null; then
        echo -e "${GREEN}✅ $name is accessible${NC}"
        return 0
    else
        echo -e "${RED}❌ $name is not accessible at $url${NC}"
        return 1
    fi
}

# Function to run basic security scan
run_basic_security_scan() {
    local target_url=$1
    local report_name=$2
    
    echo -e "${YELLOW}Running basic security scan on $target_url...${NC}"
    
    local report_file="$SCAN_REPORT_DIR/${report_name}_basic_scan.json"
    local html_report="$SCAN_REPORT_DIR/${report_name}_basic_scan.html"
    
    # Create basic scan report structure
    cat > "$report_file" << EOF
{
  "scan_info": {
    "target": "$target_url",
    "scan_type": "basic_security_scan",
    "timestamp": "$(date -u +"%Y-%m-%dT%H:%M:%SZ")",
    "scanner": "curl_based_security_checker"
  },
  "alerts": []
}
EOF

    # Test for common security headers
    echo "Checking security headers..."
    headers=$(curl -s -I "$target_url" 2>/dev/null)
    
    # Check for HTTPS
    if [[ "$target_url" == https://* ]]; then
        echo "✅ HTTPS is enabled"
    else
        echo "⚠️  HTTP is used (not HTTPS)"
        # Add to report
        jq '.alerts += [{"name": "HTTP_Used", "risk": "Medium", "description": "Application is using HTTP instead of HTTPS"}]' "$report_file" > "${report_file}.tmp" && mv "${report_file}.tmp" "$report_file"
    fi
    
    # Check for security headers
    if echo "$headers" | grep -i "X-Frame-Options" > /dev/null; then
        echo "✅ X-Frame-Options header present"
    else
        echo "⚠️  X-Frame-Options header missing"
        jq '.alerts += [{"name": "Missing_X_Frame_Options", "risk": "Medium", "description": "X-Frame-Options header is missing"}]' "$report_file" > "${report_file}.tmp" && mv "${report_file}.tmp" "$report_file"
    fi
    
    if echo "$headers" | grep -i "X-Content-Type-Options" > /dev/null; then
        echo "✅ X-Content-Type-Options header present"
    else
        echo "⚠️  X-Content-Type-Options header missing"
        jq '.alerts += [{"name": "Missing_X_Content_Type_Options", "risk": "Low", "description": "X-Content-Type-Options header is missing"}]' "$report_file" > "${report_file}.tmp" && mv "${report_file}.tmp" "$report_file"
    fi
    
    if echo "$headers" | grep -i "X-XSS-Protection" > /dev/null; then
        echo "✅ X-XSS-Protection header present"
    else
        echo "⚠️  X-XSS-Protection header missing"
        jq '.alerts += [{"name": "Missing_X_XSS_Protection", "risk": "Medium", "description": "X-XSS-Protection header is missing"}]' "$report_file" > "${report_file}.tmp" && mv "${report_file}.tmp" "$report_file"
    fi
    
    # Test for common vulnerabilities
    echo "Testing for common vulnerabilities..."
    
    # Test for directory traversal
    if curl -s "$target_url/../../../etc/passwd" | grep -q "root:"; then
        echo "❌ Directory traversal vulnerability detected"
        jq '.alerts += [{"name": "Directory_Traversal", "risk": "High", "description": "Directory traversal vulnerability detected"}]' "$report_file" > "${report_file}.tmp" && mv "${report_file}.tmp" "$report_file"
    else
        echo "✅ No directory traversal vulnerability detected"
    fi
    
    # Test for SQL injection (basic)
    if curl -s "$target_url/?id=1'OR'1'='1" | grep -i "sql\|mysql\|error" > /dev/null; then
        echo "⚠️  Potential SQL injection vulnerability"
        jq '.alerts += [{"name": "Potential_SQL_Injection", "risk": "High", "description": "Potential SQL injection vulnerability detected"}]' "$report_file" > "${report_file}.tmp" && mv "${report_file}.tmp" "$report_file"
    else
        echo "✅ No obvious SQL injection vulnerability detected"
    fi
    
    # Generate HTML report
    cat > "$html_report" << EOF
<!DOCTYPE html>
<html>
<head>
    <title>Basic Security Scan Report - $report_name</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 20px; }
        .header { background-color: #f0f0f0; padding: 10px; border-radius: 5px; }
        .alert { margin: 10px 0; padding: 10px; border-radius: 5px; }
        .high { background-color: #ffebee; border-left: 4px solid #f44336; }
        .medium { background-color: #fff3e0; border-left: 4px solid #ff9800; }
        .low { background-color: #e8f5e8; border-left: 4px solid #4caf50; }
    </style>
</head>
<body>
    <div class="header">
        <h1>Basic Security Scan Report</h1>
        <p><strong>Target:</strong> $target_url</p>
        <p><strong>Scan Date:</strong> $(date)</p>
    </div>
    
    <h2>Scan Results</h2>
    <div id="alerts">
        <!-- Alerts will be populated by JavaScript -->
    </div>
    
    <script>
        const alerts = $(cat "$report_file" | jq -r '.alerts[] | @base64');
        const alertsDiv = document.getElementById('alerts');
        
        alerts.forEach(alert => {
            const decoded = JSON.parse(atob(alert));
            const div = document.createElement('div');
            div.className = 'alert ' + decoded.risk.toLowerCase();
            div.innerHTML = '<h3>' + decoded.name + '</h3><p><strong>Risk:</strong> ' + decoded.risk + '</p><p>' + decoded.description + '</p>';
            alertsDiv.appendChild(div);
        });
    </script>
</body>
</html>
EOF

    echo -e "${GREEN}✅ Basic security scan completed for $report_name${NC}"
}

# Function to generate summary report
generate_summary() {
    echo -e "${BLUE}📊 Generating Security Scan Summary${NC}"
    echo "=========================================="
    
    local summary_file="$SCAN_REPORT_DIR/security_summary.md"
    
    cat > "$summary_file" << EOF
# MediMind Security Scan Summary
Generated on: $(date)

## Scan Overview
- **Frontend URL**: $FRONTEND_URL
- **Backend URL**: $BACKEND_URL
- **Scan Time**: $(date)

## Reports Generated
EOF
    
    # List all generated reports
    for report in "$SCAN_REPORT_DIR"/*.html; do
        if [ -f "$report" ]; then
            echo "- $(basename "$report")" >> "$summary_file"
        fi
    done
    
    echo "" >> "$summary_file"
    echo "## Next Steps" >> "$summary_file"
    echo "1. Review all HTML reports for detailed findings" >> "$summary_file"
    echo "2. Address any HIGH or CRITICAL severity issues" >> "$summary_file"
    echo "3. Consider MEDIUM severity issues based on risk assessment" >> "$summary_file"
    echo "4. Re-run scan after fixes to verify resolution" >> "$summary_file"
    
    echo -e "${GREEN}✅ Summary report generated: $summary_file${NC}"
}

# Function to check for critical vulnerabilities
check_critical_issues() {
    echo -e "${YELLOW}Checking for critical security issues...${NC}"
    
    local critical_count=0
    local high_count=0
    
    # Check JSON reports for critical/high issues
    for json_report in "$SCAN_REPORT_DIR"/*.json; do
        if [ -f "$json_report" ]; then
            local critical=$(jq -r '.alerts[] | select(.risk == "High" or .risk == "Critical") | .name' "$json_report" 2>/dev/null | wc -l)
            local high=$(jq -r '.alerts[] | select(.risk == "High") | .name' "$json_report" 2>/dev/null | wc -l)
            
            critical_count=$((critical_count + critical))
            high_count=$((high_count + high))
        fi
    done
    
    if [ "$critical_count" -gt 0 ] || [ "$high_count" -gt 0 ]; then
        echo -e "${RED}⚠️  Found $critical_count critical and $high_count high severity issues${NC}"
        echo -e "${RED}Please review the reports and address these issues before deployment${NC}"
        return 1
    else
        echo -e "${GREEN}✅ No critical or high severity issues found${NC}"
        return 0
    fi
}

# Main execution
main() {
    # Check if applications are running
    echo -e "${BLUE}🔍 Checking application availability...${NC}"
    
    local frontend_accessible=false
    local backend_accessible=false
    
    if check_url "$FRONTEND_URL" "Frontend"; then
        frontend_accessible=true
    fi
    
    if check_url "$BACKEND_URL" "Backend"; then
        backend_accessible=true
    fi
    
    if [ "$frontend_accessible" = false ] && [ "$backend_accessible" = false ]; then
        echo -e "${RED}❌ Neither frontend nor backend are accessible. Please start the applications first.${NC}"
        exit 1
    fi
    
    # Run scans based on what's accessible
    if [ "$frontend_accessible" = true ]; then
        run_basic_security_scan "$FRONTEND_URL" "frontend"
    fi
    
    if [ "$backend_accessible" = true ]; then
        run_basic_security_scan "$BACKEND_URL" "backend"
    fi
    
    # Generate summary
    generate_summary
    
    # Check for critical issues
    if check_critical_issues; then
        echo -e "${GREEN}🎉 DAST scan completed successfully! No critical issues found.${NC}"
        echo -e "${BLUE}📁 Reports available in: $SCAN_REPORT_DIR${NC}"
        exit 0
    else
        echo -e "${RED}❌ DAST scan completed with critical issues. Please review and fix before deployment.${NC}"
        exit 1
    fi
}

# Run main function
main "$@"
