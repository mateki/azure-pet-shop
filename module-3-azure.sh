export RESOURCE_GROUP=learn-49dbbf33-638e-49b4-b92b-d0a58a9a1564
export RESOURCE_GROUP=mdjava_rg
export AZURE_REGION_PL=polandcentral
export AZURE_REGION_US=westus
export AZURE_REGION=westeurope
export AZURE_APP_PLAN=mdjava_plan
export AZURE_WEB_APP_UI=mdjava-app
export AZURE_WEB_APP_ORDERS=mdjava-orders
export AZURE_WEB_APP_PRODUCTS=mdjava-products
export AZURE_WEB_APP_PETS=mdjava-pets
export VM_LIN_NAME=mateusz-ubntu-az-java
export D_REG_NAME=mdjava
export AZ_NET=.azurewebsites.net
export PETSTOREPETSERVICE_URL=http://mdjava-pets.azurewebsites.net
export PETSTOREPRODUCTSERVICE_URL=http://mdjava-products.azurewebsites.net
export PETSTOREORDERSERVICE_URL=http://mdjava-orders.azurewebsites.net

az group create --location $AZURE_REGION --resource-group $RESOURCE_GROUP


az acr create --location $AZURE_REGION --name $D_REG_NAME --resource-group $RESOURCE_GROUP --sku Basic --admin-enabled true
az acr credential show --resource-group $RESOURCE_GROUP --name $D_REG_NAME
az identity create --name myID --resource-group $RESOURCE_GROUP


//TO DO IN UI
PRINCIPAL_ID=$(az identity show --resource-group $RESOURCE_GROUP --name myID --query principalId --output tsv)
REGISTRY_ID=$(az acr show --resource-group $RESOURCE_GROUP --name $D_REG_NAME --query id --output tsv)
az role assignment create --assignee $PRINCIPAL_ID --scope $REGISTRY_ID --role "AcrPull"




az appservice plan create --name $AZURE_APP_PLAN --resource-group $RESOURCE_GROUP --is-linux --location $AZURE_REGION --sku FREE
az webapp create -g $RESOURCE_GROUP -p $AZURE_APP_PLAN -n $AZURE_WEB_APP_PETS -i $D_REG_NAME.azurecr.io/petstorepetservice:latest
az webapp config appsettings set -g $RESOURCE_GROUP -n $AZURE_WEB_APP_PETS --settings WEBSITES_PORT=8080
az webapp create -g $RESOURCE_GROUP -p $AZURE_APP_PLAN -n $AZURE_WEB_APP_PRODUCTS -i $D_REG_NAME.azurecr.io/petstoreproductservice:latest
az webapp config appsettings set -g $RESOURCE_GROUP -n $AZURE_WEB_APP_PRODUCTS --settings WEBSITES_PORT=8080
az webapp create -g $RESOURCE_GROUP -p $AZURE_APP_PLAN -n $AZURE_WEB_APP_ORDERS -i $D_REG_NAME.azurecr.io/petstoreorderservice:latest
az webapp config appsettings set -g $RESOURCE_GROUP -n $AZURE_WEB_APP_ORDERS --settings WEBSITES_PORT=8080

az webapp create -g $RESOURCE_GROUP -p $AZURE_APP_PLAN -n $AZURE_WEB_APP_UI -i $D_REG_NAME.azurecr.io/petstoreapp:latest
az webapp config appsettings set -g $RESOURCE_GROUP -n $AZURE_WEB_APP_UI --settings WEBSITES_PORT=8080 PETSTOREPETSERVICE_URL=$PETSTOREPETSERVICE_URL PETSTOREPRODUCTSERVICE_URL=$PETSTOREPRODUCTSERVICE_URL PETSTOREORDERSERVICE_URL=$PETSTOREORDERSERVICE_URL



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