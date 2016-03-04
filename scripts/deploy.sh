#!/bin/bash

gradle fatjar
mv backend/build/libs/Uppackaren-1.0.jar ../uppackaren_deploy/  
cd ../uppackaren_deploy/ 
git commit -am "new deploy" 
git push

cd ../Uppackaren
