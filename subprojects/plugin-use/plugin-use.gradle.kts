import org.gradle.gradlebuild.unittestandcompile.ModuleType
import org.gradle.gradlebuild.testing.integrationtests.cleanup.WhenNotEmpty

/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
plugins {
    `java-library`
    gradlebuild.classycle
}

dependencies {
    implementation(project(":baseServices"))
    implementation(project(":logging"))
    implementation(project(":messaging"))
    implementation(project(":fileCollections"))
    implementation(project(":coreApi"))
    implementation(project(":core"))
    implementation(project(":dependencyManagement"))

    runtimeOnly(project(":resourcesHttp"))

    implementation(library("groovy"))
    implementation(library("guava"))

    testImplementation(testFixtures(project(":resourcesHttp")))

    integTestImplementation(project(":baseServicesGroovy"))

    integTestRuntimeOnly(project(":plugins"))
    integTestRuntimeOnly(project(":pluginDevelopment"))
    integTestRuntimeOnly(project(":testKit"))
    integTestRuntimeOnly(project(":toolingApiBuilders"))
    integTestRuntimeOnly(project(":runtimeApiInfo"))
    integTestRuntimeOnly(project(":testingJunitPlatform"))
    integTestRuntimeOnly(project(":apiMetadata"))
    integTestRuntimeOnly(project(":kotlinDsl"))
    integTestRuntimeOnly(project(":kotlinDslProviderPlugins"))
}


gradlebuildJava {
    moduleType = ModuleType.CORE
}

testFilesCleanup {
    policy.set(WhenNotEmpty.REPORT)
}
