# Secure File Delivery API - Test Instructions

## Prerequisites

- Docker Desktop installed and running
- Java 21+ installed
- Maven 3.8+ installed
- Postman Desktop (for token obtaining)
- Microsoft account (Outlook/Live) for Azure AD authentication

## One-Command Setup

```bash
# Clone the repository
git clone https://github.com/sli-org/secure-file-delivery-api.git
git clone https://github.com/sli-org/common-api-starter-parent.git

cd secure-file-delivery-api
cd common-api-starter-parent

# Make the script executable
chmod +x setup.sh

# Run the setup (this does everything!)
./setup.sh