rootProject.name = "dockerfile-kotlin-dsl"

// Include the plugin build
includeBuild("plugin")

// Include the integration test builds
includeBuild("integration-test-kotlin")
includeBuild("integration-test-groovy")
