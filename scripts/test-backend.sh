#!/bin/bash

# Simple Backend Test Runner
# Uses your existing Maven wrapper and JDK 21

set -e

echo "🧪 Running MediMind Backend Tests"
echo "=================================="

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if we're in the right directory
if [ ! -f "pom.xml" ]; then
    print_error "pom.xml not found. Please run this script from the backend directory."
    exit 1
fi

# Check Java version
JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
if [ "$JAVA_VERSION" != "21" ]; then
    print_warning "Expected Java 21, found Java $JAVA_VERSION"
    print_warning "Tests may fail if Java version is incompatible"
fi

# Run tests
print_status "Running Maven tests with JDK 21..."
./mvnw clean test

print_status "Generating coverage report..."
./mvnw jacoco:report

# Generate simple summary
print_status "Generating test summary..."
echo "=== Backend Test Summary ===" > test-summary.txt
echo "Date: $(date)" >> test-summary.txt
echo "Java Version: $(java -version 2>&1 | head -n 1)" >> test-summary.txt
echo "" >> test-summary.txt

# Count test results
if [ -d "target/surefire-reports" ]; then
    TOTAL_TESTS=$(find target/surefire-reports -name "*.txt" -exec grep -l "Tests run:" {} \; | wc -l)
    echo "Total test classes: $TOTAL_TESTS" >> test-summary.txt
    
    # Count passed/failed tests
    PASSED=$(find target/surefire-reports -name "*.txt" -exec grep "Tests run:" {} \; | awk '{sum += $3} END {print sum}')
    FAILED=$(find target/surefire-reports -name "*.txt" -exec grep "Tests run:" {} \; | awk '{sum += $6} END {print sum}')
    SKIPPED=$(find target/surefire-reports -name "*.txt" -exec grep "Tests run:" {} \; | awk '{sum += $9} END {print sum}')
    
    echo "Tests passed: $PASSED" >> test-summary.txt
    echo "Tests failed: $FAILED" >> test-summary.txt
    echo "Tests skipped: $SKIPPED" >> test-summary.txt
    
    if [ "$FAILED" -gt 0 ]; then
        echo "❌ Some tests failed!" >> test-summary.txt
        print_error "Some tests failed!"
    else
        echo "✅ All tests passed!" >> test-summary.txt
        print_success "All tests passed!"
    fi
fi

# Coverage summary
if [ -f "target/site/jacoco/index.html" ]; then
    echo "" >> test-summary.txt
    echo "=== Coverage Summary ===" >> test-summary.txt
    echo "Coverage report: target/site/jacoco/index.html" >> test-summary.txt
    print_success "Coverage report generated"
fi

# Display summary
echo ""
echo "=== Test Results Summary ==="
cat test-summary.txt

# Open coverage report in browser (macOS)
if [[ "$OSTYPE" == "darwin"* ]] && [ -f "target/site/jacoco/index.html" ]; then
    print_status "Opening coverage report in browser..."
    open target/site/jacoco/index.html
fi

echo ""
print_success "Backend tests completed!"
