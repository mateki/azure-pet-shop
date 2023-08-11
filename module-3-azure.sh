export RESOURCE_GROUP_PL=mdjava_rg_us
export RESOURCE_GROUP_EU=mdjava_rg_eu
export RESOURCE_GROUP=mdjava_rg_repo
export AZURE_REGION_PL=polandcentral
export AZURE_REGION_US=westus
export AZURE_REGION_EU=westeurope
export AZURE_APP_PLAN=mdjava_plan

export AZURE_WEB_APP_UI_EU=mdjava-app-eu
export AZURE_WEB_APP_ORDERS_EU=mdjava-orders-eu
export AZURE_WEB_APP_PRODUCTS_EU=mdjava-products-eu
export AZURE_WEB_APP_PETS_EU=mdjava-pets-eu

export AZURE_WEB_APP_UI_PL=mdjava-app-us
export AZURE_WEB_APP_ORDERS_PL=mdjava-orders-us
export AZURE_WEB_APP_PRODUCTS_PL=mdjava-products-us
export AZURE_WEB_APP_PETS_PL=mdjava-pets-us

export VM_LIN_NAME=mateusz-ubntu-az-java

export D_REG_NAME=mdjava
export AZ_NET=.azurewebsites.net
export SERVICE=http://mdjava-pets
export PRODUCTS=http://mdjava-products
export ORDERS=http://mdjava-orders
export HOST=-us.azurewebsites.net


az group create --location $AZURE_REGION_PL --resource-group $RESOURCE_GROUP
az group create --location $AZURE_REGION_EU --resource-group $RESOURCE_GROUP_EU
az group create --location $AZURE_REGION_PL --resource-group $RESOURCE_GROUP_PL


az acr create --location $AZURE_REGION --name $D_REG_NAME --resource-group $RESOURCE_GROUP --sku Basic --admin-enabled true
az acr credential show --resource-group $RESOURCE_GROUP --name $D_REG_NAME
az identity create --name myID --resource-group $RESOURCE_GROUP


//TO DO IN UI
PRINCIPAL_ID=$(az identity show --resource-group $RESOURCE_GROUP --name myID --query principalId --output tsv)
REGISTRY_ID=$(az acr show --resource-group $RESOURCE_GROUP --name $D_REG_NAME --query id --output tsv)
az role assignment create --assignee $PRINCIPAL_ID --scope $REGISTRY_ID --role "AcrPull"

az appservice plan create --name $AZURE_APP_PLAN --resource-group $RESOURCE_GROUP_EU --is-linux --location $AZURE_REGION_EU --sku FREE


export AZURE_APP_PLAN=mdjava_plan-eu
az webapp create -g $RESOURCE_GROUP_EU -p $AZURE_APP_PLAN -n $AZURE_WEB_APP_PETS_EU -i $D_REG_NAME.azurecr.io/petstorepetservice:latest
az webapp create -g $RESOURCE_GROUP_EU -p $AZURE_APP_PLAN -n $AZURE_WEB_APP_PRODUCTS_EU -i $D_REG_NAME.azurecr.io/petstoreproductservice:latest
az webapp create -g $RESOURCE_GROUP_EU -p $AZURE_APP_PLAN -n $AZURE_WEB_APP_ORDERS_EU -i $D_REG_NAME.azurecr.io/petstoreorderservice:latest
az webapp create -g $RESOURCE_GROUP -p $AZURE_APP_PLAN -n $AZURE_WEB_APP_UI_EU -i $D_REG_NAME.azurecr.io/petstoreapp:latest

export AZURE_APP_PLAN=mdjava_plan-eu
export SERVICE=http://mdjava-pets
export PRODUCTS=http://mdjava-products
export ORDERS=http://mdjava-orders
export HOST=-us.azurewebsites.net
az webapp config appsettings set -g $RESOURCE_GROUP_PL -n $AZURE_WEB_APP_PETS_PL --settings WEBSITES_PORT=8080
az webapp config appsettings set -g $RESOURCE_GROUP_PL -n $AZURE_WEB_APP_PRODUCTS_PL --settings WEBSITES_PORT=8080
az webapp config appsettings set -g $RESOURCE_GROUP_PL -n $AZURE_WEB_APP_ORDERS_PL --settings WEBSITES_PORT=8080
az webapp config appsettings set -g $RESOURCE_GROUP_PL -n $AZURE_WEB_APP_UI_PL --settings WEBSITES_PORT=8080 PETSTOREPETSERVICE_URL=$SERVICE$HOST PETSTOREPRODUCTSERVICE_URL=$PRODUCTS$HOST PETSTOREORDERSERVICE_URL=$ORDERS$HOST


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