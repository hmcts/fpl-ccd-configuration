
# Prerequisites:
# sudo apt-get install jq
# brew install coreutils
# Install docker from website




#!/bin/bash
yarn install

git submodule update

# This is not explicitly necessary, however without it there is a chance some docker images wont have auth to download.
az acr login --name hmctspublic --subscription 8999dec3-0104-4a27-94ee-6588559729d1

# Just in case something is updated for parsers as this is a dependency but not part of compose setup
docker pull hmctspublic.azurecr.io/ccd/definition-processor

cd fpla-docker/
./ccd login
./ccd compose pull

ES_ENABLED_DOCKER=true
XUI_LD_CLIENT_ID=5de6610b23ce5408280f2268
DOCMOSIS_KEY=3RAD-KLTK-JALP-EKIA-EBBH-2ELH-QBKQ-HS07-E7E1-B-31F4

./ccd compose up -d

./bin/add-services.sh
./bin/add-roles.sh
./bin/add-users.sh


cd ..
./bin/generate-local-user-mappings.sh
./bin/import-ccd-definition.sh

