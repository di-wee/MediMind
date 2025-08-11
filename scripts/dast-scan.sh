#!/bin/bash

# DAST (Dynamic Application Security Testing) Script for MediMind
# Uses OWASP ZAP for automated security testing

set -e

# Configuration
ZAP_VERSION="2.14.0"
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

# Function to download and setup OWASP ZAP
setup_zap() {
    echo -e "${YELLOW}Setting up OWASP ZAP...${NC}"
    
    if [ ! -d "$ZAP_DIR" ]; then
        mkdir -p "$ZAP_DIR"
        
        # Download ZAP based on OS
        if [[ "$OSTYPE" == "darwin"* ]]; then
            # macOS
            ZAP_URL="https://github.com/zaproxy/zaproxy/releases/download/v${ZAP_VERSION}/ZAP_${ZAP_VERSION}_macos.tar.gz"
        elif [[ "$OSTYPE" == "linux-gnu"* ]]; then
            # Linux - Fixed URL format
            ZAP_URL="https://github.com/zaproxy/zaproxy/releases/download/v${ZAP_VERSION}/ZAP_${ZAP_VERSION}_Linux.tar.gz"
        else
            echo -e "${RED}Unsupported OS: $OSTYPE${NC}"
            exit 1
        fi
        
        echo "Downloading ZAP from: $ZAP_URL"
        curl -L "$ZAP_URL" -o "$ZAP_DIR/zap.tar.gz"
        
        # Check if download was successful
        if [ ! -f "$ZAP_DIR/zap.tar.gz" ] || [ ! -s "$ZAP_DIR/zap.tar.gz" ]; then
            echo -e "${RED}❌ Failed to download ZAP from $ZAP_URL${NC}"
            echo "Trying alternative download method..."
            # Try alternative URL format
            if [[ "$OSTYPE" == "linux-gnu"* ]]; then
                ALT_URL="https://github.com/zaproxy/zaproxy/releases/download/v${ZAP_VERSION}/ZAP_${ZAP_VERSION}_Linux.tar.gz"
                curl -L "$ALT_URL" -o "$ZAP_DIR/zap.tar.gz"
            fi
        fi
        
        # Verify the downloaded file
        if [ ! -f "$ZAP_DIR/zap.tar.gz" ] || [ ! -s "$ZAP_DIR/zap.tar.gz" ]; then
            echo -e "${RED}❌ ZAP download failed. Please check the URL and try again.${NC}"
            exit 1
        fi
        
        echo "Extracting ZAP..."
        tar -xzf "$ZAP_DIR/zap.tar.gz" -C "$ZAP_DIR" --strip-components=1
        rm "$ZAP_DIR/zap.tar.gz"
        
        echo -e "${GREEN}✅ ZAP setup complete${NC}"
    else
        echo -e "${GREEN}✅ ZAP already exists${NC}"
    fi
}

# Function to run ZAP baseline scan
run_baseline_scan() {
    local target_url=$1
    local report_name=$2
    
    echo -e "${YELLOW}Running baseline scan on $target_url...${NC}"
    
    # Start ZAP in daemon mode
    "$ZAP_DIR/zap.sh" -daemon -port 8080 -config api.disablekey=true &
    ZAP_PID=$!
    
    # Wait for ZAP to start
    sleep 10
    
    # Run baseline scan
    "$ZAP_DIR/zap-baseline.py" -t "$target_url" -J "$SCAN_REPORT_DIR/${report_name}_baseline.json" -r "$SCAN_REPORT_DIR/${report_name}_baseline.html"
    
    # Stop ZAP
    kill $ZAP_PID 2>/dev/null || true
    
    echo -e "${GREEN}✅ Baseline scan completed for $report_name${NC}"
}

# Function to run ZAP full scan
run_full_scan() {
    local target_url=$1
    local report_name=$2
    
    echo -e "${YELLOW}Running full scan on $target_url...${NC}"
    
    # Start ZAP in daemon mode
    "$ZAP_DIR/zap.sh" -daemon -port 8080 -config api.disablekey=true &
    ZAP_PID=$!
    
    # Wait for ZAP to start
    sleep 10
    
    # Run full scan
    "$ZAP_DIR/zap-full-scan.py" -t "$target_url" -J "$SCAN_REPORT_DIR/${report_name}_full.json" -r "$SCAN_REPORT_DIR/${report_name}_full.html" -m "$SCAN_TIMEOUT"
    
    # Stop ZAP
    kill $ZAP_PID 2>/dev/null || true
    
    echo -e "${GREEN}✅ Full scan completed for $report_name${NC}"
}

# Function to run API scan
run_api_scan() {
    local api_url=$1
    local report_name=$2
    
    echo -e "${YELLOW}Running API scan on $api_url...${NC}"
    
    # Start ZAP in daemon mode
    "$ZAP_DIR/zap.sh" -daemon -port 8080 -config api.disablekey=true &
    ZAP_PID=$!
    
    # Wait for ZAP to start
    sleep 10
    
    # Run API scan (assuming OpenAPI/Swagger endpoint)
    "$ZAP_DIR/zap-api-scan.py" -t "$api_url" -f openapi -J "$SCAN_REPORT_DIR/${report_name}_api.json" -r "$SCAN_REPORT_DIR/${report_name}_api.html"
    
    # Stop ZAP
    kill $ZAP_PID 2>/dev/null || true
    
    echo -e "${GREEN}✅ API scan completed for $report_name${NC}"
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
    
    # Setup ZAP
    setup_zap
    
    # Run scans based on what's accessible
    if [ "$frontend_accessible" = true ]; then
        run_baseline_scan "$FRONTEND_URL" "frontend"
        run_full_scan "$FRONTEND_URL" "frontend"
    fi
    
    if [ "$backend_accessible" = true ]; then
        run_baseline_scan "$BACKEND_URL" "backend"
        run_full_scan "$BACKEND_URL" "backend"
        
        # Try API scan if backend has API documentation
        if check_url "$BACKEND_URL/v3/api-docs" "API Documentation" 2>/dev/null; then
            run_api_scan "$BACKEND_URL/v3/api-docs" "backend"
        fi
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
