export RESOURCE_GROUP=learn-49dbbf33-638e-49b4-b92b-d0a58a9a1564
export RESOURCE_GROUP=mateusz-learn-java-azure
export AZURE_REGION_PL=polandcentral
export AZURE_REGION_US=westus
export AZURE_REGION=westus
export AZURE_APP_PLAN=mateusz-web-app-plan-java-azure
export AZURE_WEB_APP=mateusz-web-app-java-azure
export VM_LIN_NAME=mateusz-ubntu-az-java
export D_REG_NAME=mdjava


az acr create --location $AZURE_REGION --name $D_REG_NAME --resource-group $RESOURCE_GROUP --sku Basic --admin-enabled true
az acr credential show --resource-group $RESOURCE_GROUP --name $D_REG_NAME
az identity create --name myID --resource-group $RESOURCE_GROUP


// TO-REpeAT
PRINCIPAL_ID=$(az identity show --resource-group $RESOURCE_GROUP --name myID --query principalId --output tsv)
REGISTRY_ID=$(az acr show --resource-group $RESOURCE_GROUP --name $D_REG_NAME --query id --output tsv)
az role assignment create --assignee $PRINCIPAL_ID --scope $REGISTRY_ID --role "AcrPull"




az group create --location $AZURE_REGION --resource-group $RESOURCE_GROUP
az appservice plan create --name $AZURE_APP_PLAN --resource-group $RESOURCE_GROUP --location $AZURE_REGION --sku FREE
az webapp create --resource-group MyResourceGroup --plan $AZURE_APP_PLAN --name $AZURE_WEB_APP --deployment-container-image-name nginx

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