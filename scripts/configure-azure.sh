#!/usr/bin/env bash
# Run in Azure Cloud Shell (Bash) after creating the Java 21 / F1 Web App.
set -euo pipefail

if [[ $# -ne 3 ]]; then
  echo "Usage: bash configure-azure.sh RESOURCE_GROUP WEB_APP_NAME GITHUB_OWNER/REPOSITORY" >&2
  exit 2
fi
resource_group="$1"
web_app="$2"
repository="$3"
if [[ ! "$repository" =~ ^[A-Za-z0-9_.-]+/[A-Za-z0-9_.-]+$ ]]; then
  echo "Repository must have the form OWNER/REPOSITORY." >&2
  exit 2
fi
identity_name="${web_app}-github"
web_app_id=$(az webapp show --resource-group "$resource_group" --name "$web_app" --query id --output tsv)
location=$(az webapp show --resource-group "$resource_group" --name "$web_app" --query location --output tsv)

az identity create --resource-group "$resource_group" --name "$identity_name" --location "$location" --output none
az identity federated-credential create \
  --resource-group "$resource_group" --identity-name "$identity_name" --name github-main \
  --issuer https://token.actions.githubusercontent.com \
  --subject "repo:${repository}:ref:refs/heads/main" \
  --audiences api://AzureADTokenExchange --output none

principal_id=$(az identity show --resource-group "$resource_group" --name "$identity_name" --query principalId --output tsv)
az role assignment create --assignee-object-id "$principal_id" --assignee-principal-type ServicePrincipal \
  --role 'Website Contributor' --scope "$web_app_id" --output none

echo 'Add the following REPOSITORY SECRETS in GitHub Settings > Secrets and variables > Actions:'
echo "AZURE_CLIENT_ID=$(az identity show --resource-group "$resource_group" --name "$identity_name" --query clientId --output tsv)"
echo "AZURE_TENANT_ID=$(az account show --query tenantId --output tsv)"
echo "AZURE_SUBSCRIPTION_ID=$(az account show --query id --output tsv)"
echo 'Add this REPOSITORY VARIABLE in the Variables tab:'
echo "AZURE_WEBAPP_NAME=${web_app}"
echo "Deployment URL: https://$(az webapp show --resource-group "$resource_group" --name "$web_app" --query defaultHostName --output tsv)"
