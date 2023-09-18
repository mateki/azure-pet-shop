export RESOURCE_GROUP_US=mdjava_rg_us
export RESOURCE_GROUP_EU=mdjava_rg_eu
export RESOURCE_GROUP=mdjava_rg_repo
export AZURE_REGION_US=polandcentral
export AZURE_REGION_US=westus
export AZURE_REGION_EU=westeurope
export AZURE_APP_PLAN=mdjava_plan-eu

export AZURE_WEB_APP_UI_EU=mdjava-app-eu
export AZURE_WEB_APP_ORDERS_EU=mdjava-orders-eu
export AZURE_WEB_APP_PRODUCTS_EU=mdjava-products-eu
export AZURE_WEB_APP_PETS_EU=mdjava-pets-eu

export AZURE_WEB_APP_UI_US=mdjava-app-us
export AZURE_WEB_APP_ORDERS_US=mdjava-orders-us
export AZURE_WEB_APP_PRODUCTS_US=mdjava-products-us
export AZURE_WEB_APP_PETS_US=mdjava-pets-us

export VM_LIN_NAME=mateusz-ubntu-az-java

export D_REG_NAME=mdjava
export AZ_NET=.azurewebsites.net
export SERVICE=http://mdjava-pets
export PRODUCTS=http://mdjava-products
export ORDERS=http://mdjava-orders
export HOST=-us.azurewebsites.net


az group create --location $AZURE_REGION_EU --resource-group $RESOURCE_GROUP
az group create --location $AZURE_REGION_EU --resource-group $RESOURCE_GROUP_EU
az group create --location $AZURE_REGION_US --resource-group $RESOURCE_GROUP_US

az keyvault create --resource-group $RESOURCE_GROUP --location centralus --name mdjava-vault
az keyvault secret set --name szef --value Mdjava1@ --vault-name mdjava-vault
az keyvault secret show --name szef --vault-name mdjava-vault --query "value"

az webapp identity assign --name $AZURE_WEB_APP_ORDERS_US --resource-group $RESOURCE_GROUP_US
az webapp identity assign --name $AZURE_WEB_APP_PETS_US --resource-group $RESOURCE_GROUP_US
az webapp identity assign --name $AZURE_WEB_APP_PRODUCTS_US --resource-group $RESOURCE_GROUP_US
az webapp identity assign --name $AZURE_WEB_APP_UI_US --resource-group $RESOURCE_GROUP_US

az webapp identity assign --name $AZURE_WEB_APP_ORDERS_EU --resource-group $RESOURCE_GROUP_EU
az webapp identity assign --name $AZURE_WEB_APP_PETS_EU --resource-group $RESOURCE_GROUP_EU
az webapp identity assign --name $AZURE_WEB_APP_PRODUCTS_EU --resource-group $RESOURCE_GROUP_EU
az webapp identity assign --name $AZURE_WEB_APP_UI_EU --resource-group $RESOURCE_GROUP_EU

az keyvault set-policy --name "mdjava-vault" --object-id "f217b762-d88a-4214-9696-e243036bcded" --secret-permissions get list

@Microsoft.KeyVault(SecretUri=https://mdjava-vault.vault.azure.net/secrets/data-source-url/)
@Microsoft.KeyVault(SecretUri=https://mdjava-vault.vault.azure.net/secrets/data-source-szef-pass/)
@Microsoft.KeyVault(SecretUri=https://mdjava-vault.vault.azure.net/secrets/data-source-user/)

@Microsoft.KeyVault(SecretUri=https://mdjava-vault.vault.azure.net/secrets/cosmo-url/)
@Microsoft.KeyVault(SecretUri=https://mdjava-vault.vault.azure.net/secrets/cosmos-key/)

@Microsoft.KeyVault(SecretUri=https://mdjava-vault.vault.azure.net/secrets/b2c-client-id/)
@Microsoft.KeyVault(SecretUri=https://mdjava-vault.vault.azure.net/secrets/b2c-client-secret/)

az acr create --location $AZURE_REGION --name $D_REG_NAME --resource-group $RESOURCE_GROUP --sku Basic --admin-enabled true
az acr credential show --resource-group $RESOURCE_GROUP --name $D_REG_NAME
az identity create --name myID --resource-group $RESOURCE_GROUP


//TO DO IN UI
PRINCIPAL_ID=$(az identity show --resource-group $RESOURCE_GROUP --name myID --query principalId --output tsv)
REGISTRY_ID=$(az acr show --resource-group $RESOURCE_GROUP --name $D_REG_NAME --query id --output tsv)
az role assignment create --assignee $PRINCIPAL_ID --scope $REGISTRY_ID --role "AcrPull"

az appservice plan create --name $AZURE_APP_PLAN --resource-group $RESOURCE_GROUP_EU --is-linux --location $AZURE_REGION_EU --sku FREE


export AZURE_APP_PLAN=mdjava_plan-eu
az webapp create -g $RESOURCE_GROUP_EU -p $AZURE_APP_PLAN -n $AZURE_WEB_APP_PETS_US -i $D_REG_NAME.azurecr.io/petstorepetservice:latest
az webapp create -g $RESOURCE_GROUP_EU -p $AZURE_APP_PLAN -n $AZURE_WEB_APP_PRODUCTS_EU -i $D_REG_NAME.azurecr.io/petstoreproductservice:latest
az webapp create -g $RESOURCE_GROUP_EU -p $AZURE_APP_PLAN -n $AZURE_WEB_APP_ORDERS_EU -i $D_REG_NAME.azurecr.io/petstoreorderservice:latest
az webapp create -g $RESOURCE_GROUP_US -p $AZURE_APP_PLAN -n $AZURE_WEB_APP_UI_US -i $D_REG_NAME.azurecr.io/petstoreapp:latest

export SERVICE=http://mdjava-pets
export PRODUCTS=http://mdjava-products
export ORDERS=http://mdjava-orders
export HOST=-eu.azurewebsites.net

az monitor app-insights component create --app mdjava-insight --resource-group $RESOURCE_GROUP --location $AZURE_REGION_EU

PETSTORE_API_KEY=$(az monitor app-ins component show --resource-group $RESOURCE_GROUP --app mdjava-insight --query 'instrumentationKey' --output tsv)
echo "Setting APPLICATIONINSIGHTS_CONNECTION_STRING InstrumentationKey=$PETSTORE_API_KEY"

az webapp config appsettings set -g $RESOURCE_GROUP_EU -n $AZURE_WEB_APP_PETS_EU --settings WEBSITES_PORT=8080 APPINSIGHTS_INSTRUMENTATIONKEY=$PETSTORE_API_KEY APPLICATIONINSIGHTS_CONNECTION_STRING="InstrumentationKey=""$PETSTORE_API_KEY" ApplicationInsightsAgent_EXTENSION_VERSION="~2"
az webapp config appsettings set -g $RESOURCE_GROUP_EU -n $AZURE_WEB_APP_PRODUCTS_EU --settings WEBSITES_PORT=8080 APPINSIGHTS_INSTRUMENTATIONKEY=$PETSTORE_API_KEY APPLICATIONINSIGHTS_CONNECTION_STRING="InstrumentationKey=""$PETSTORE_API_KEY" ApplicationInsightsAgent_EXTENSION_VERSION="~2"
az webapp config appsettings set -g $RESOURCE_GROUP_EU -n $AZURE_WEB_APP_ORDERS_EU --settings WEBSITES_PORT=8080 APPINSIGHTS_INSTRUMENTATIONKEY=$PETSTORE_API_KEY APPLICATIONINSIGHTS_CONNECTION_STRING="InstrumentationKey=""$PETSTORE_API_KEY" ApplicationInsightsAgent_EXTENSION_VERSION="~2"
az webapp config appsettings set -g $RESOURCE_GROUP_EU -n $AZURE_WEB_APP_UI_EU --settings WEBSITES_PORT=8080 PETSTOREPETSERVICE_URL=$SERVICE$HOST PETSTOREPRODUCTSERVICE_URL=$PRODUCTS$HOST PETSTOREORDERSERVICE_URL=$ORDERS$HOST APPINSIGHTS_INSTRUMENTATIONKEY=$PETSTORE_API_KEY APPLICATIONINSIGHTS_CONNECTION_STRING="InstrumentationKey=""$PETSTORE_API_KEY" ApplicationInsightsAgent_EXTENSION_VERSION="~2"


az webapp config appsettings set -g $RESOURCE_GROUP_US -n $AZURE_WEB_APP_PETS_US --settings WEBSITES_PORT=8080 APPINSIGHTS_INSTRUMENTATIONKEY=$PETSTORE_API_KEY APPLICATIONINSIGHTS_CONNECTION_STRING="InstrumentationKey=""$PETSTORE_API_KEY" ApplicationInsightsAgent_EXTENSION_VERSION="~2"
az webapp config appsettings set -g $RESOURCE_GROUP_US -n $AZURE_WEB_APP_PRODUCTS_US --settings WEBSITES_PORT=8080 APPINSIGHTS_INSTRUMENTATIONKEY=$PETSTORE_API_KEY APPLICATIONINSIGHTS_CONNECTION_STRING="InstrumentationKey=""$PETSTORE_API_KEY" ApplicationInsightsAgent_EXTENSION_VERSION="~2"
az webapp config appsettings set -g $RESOURCE_GROUP_US -n $AZURE_WEB_APP_ORDERS_US --settings WEBSITES_PORT=8080 APPINSIGHTS_INSTRUMENTATIONKEY=$PETSTORE_API_KEY APPLICATIONINSIGHTS_CONNECTION_STRING="InstrumentationKey=""$PETSTORE_API_KEY" ApplicationInsightsAgent_EXTENSION_VERSION="~2"
az webapp config appsettings set -g $RESOURCE_GROUP_US -n $AZURE_WEB_APP_UI_US --settings WEBSITES_PORT=8080 PETSTOREPETSERVICE_URL=$SERVICE$HOST PETSTOREPRODUCTSERVICE_URL=$PRODUCTS$HOST PETSTOREORDERSERVICE_URL=$ORDERS$HOST APPINSIGHTS_INSTRUMENTATIONKEY=$PETSTORE_API_KEY APPLICATIONINSIGHTS_CONNECTION_STRING="InstrumentationKey=""$PETSTORE_API_KEY" ApplicationInsightsAgent_EXTENSION_VERSION="~2"


az webapp config appsettings set -g $RESOURCE_GROUP_EU -n $AZURE_WEB_APP_UI_US --settings WEBSITES_PORT=8080 PETSTOREPETSERVICE_URL=$SERVICE$HOST PETSTOREPRODUCTSERVICE_URL=$PRODUCTS$HOST PETSTOREORDERSERVICE_URL=$ORDERS$HOST


az group delete --resource-group $RESOURCE_GROUP

docker login mdjava.azurecr.io --username mdjava

//pets
docker tag petstorepetservice mdjava.azurecr.io/petstorepetservice:latest
docker push mdjava.azurecr.io/petstorepetservice:latest
//products
docker tag petstoreproductservice mdjava.azurecr.io/petstoreproductservice:latest
docker push mdjava.azurecr.io/petstoreproductservice:latest
//orders
docker tag petstoreorderservice mdjava.azurecr.io/petstoreorderservice:latest
docker push mdjava.azurecr.io/petstoreorderservice:latest
//apps
docker tag petstoreapp mdjava.azurecr.io/petstoreapp:latest
docker push mdjava.azurecr.io/petstoreapp:latest

# Retrieve InstrumentationKey


APPINSIGHTS_INSTRUMENTATIONKEY=$PETSTORE_API_KEY APPLICATIONINSIGHTS_CONNECTION_STRING="InstrumentationKey=""$PETSTORE_API_KEY" ApplicationInsightsAgent_EXTENSION_VERSION="~2"

az webapp config appsettings set --name $AZURE_WEB_APP_PETS_EU --resource-group $RESOURCE_GROUP_EU --settings APPLICATIONINSIGHTS_CONNECTION_STRING="InstrumentationKey=""$PETSTORE_API_KEY"