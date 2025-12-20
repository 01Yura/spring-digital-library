$ErrorActionPreference = "Stop"

$ImageName = "01yura/spring-digital-bookstore"
$ImageTag  = "latest"

Write-Host "Building image $ImageName`:$ImageTag ..."
cd ..
docker build -f infra/Dockerfile -t "$ImageName`:$ImageTag" .

Write-Host "Logging in to Docker Hub..."
# Если уже залогинен в Docker Hub, можно закомментировать следующую строку:
docker login

Write-Host "Pushing image to Docker Hub..."
docker push "$ImageName`:$ImageTag"

Write-Host "Done."


