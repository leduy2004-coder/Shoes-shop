$images = @(
    "shoes-shop-api-shoes-service",
    "shoes-shop-api-file-service",
    "shoes-shop-api-config-server",
    "shoes-shop-api-eureka-server",
    "shoes-shop-api-auth-service",
    "shoes-shop-api-api-gateway"
)

$dockerhubUser = "lduy2004"
$tag = "1.0.0"

foreach ($image in $images) {
    $fullRepo = "${dockerhubUser}/${image}:${tag}"

    Write-Host "Tagging $image as $fullRepo"
    docker tag $image $fullRepo

    Write-Host "Pushing $fullRepo"
    docker push $fullRepo
}
