/*
 * Copyright 2019 the original author or authors.
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

package org.gradle.java

import org.gradle.integtests.fixtures.AbstractIntegrationSpec
import spock.lang.Unroll

class JavaIncompatiblePluginsIntegrationTest  extends AbstractIntegrationSpec {

    @Unroll
    def "nag users when applying both java-platform and #plugin"() {
        when:
        buildFile << """
plugins {
    id 'java-platform'
    id '${plugin}'
}
"""
        then:
        executer.expectDeprecationWarning()
        succeeds 'help'

        where:
        plugin << ['java', 'java-library']
    }

    @Unroll
    def "cannot apply both #plugin and java-platform"() {
        when:
        buildFile << """
plugins {
    id '${plugin}'
    id 'java-platform'
}
"""
        then:
        fails 'help'
        failureHasCause("The \"java-platform\" plugin cannot be applied together with the \"java\" (or \"java-library\") plugin")

        where:
        plugin << ['java', 'java-library']
    }
}
