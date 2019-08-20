/*
 * Copyright 2012 the original author or authors.
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

package org.gradle.internal.nativeintegration.console


import net.rubygrapefruit.platform.terminal.Terminals
import org.gradle.internal.nativeintegration.ProcessEnvironment
import org.gradle.testfixtures.internal.NativeServicesTestFixture
import org.gradle.util.Requires
import org.gradle.util.TestPrecondition
import spock.lang.Specification

@Requires(TestPrecondition.SET_ENV_VARIABLE)
class NativePlatformConsoleDetectorTest extends Specification {
    private Terminals terminals = Mock()
    private NativePlatformConsoleDetector detector = new NativePlatformConsoleDetector(terminals)
    final ProcessEnvironment env = NativeServicesTestFixture.getInstance().get(ProcessEnvironment)
    String originalTerm

    def setup() {
        originalTerm = System.getenv("TERM")
        env.setEnvironmentVariable("TERM", "mock")
    }

    def cleanup() {
        if (originalTerm != null) {
            env.setEnvironmentVariable("TERM", originalTerm)
        } else {
            env.removeEnvironmentVariable("TERM")
        }
    }

    def "returns null when neither stdout or stderr is attached to console"() {
        given:
        terminals.isTerminal(Terminals.Output.Stdout) >> false
        terminals.isTerminal(Terminals.Output.Stderr) >> false

        expect:
        detector.console == null
    }

    @Requires(TestPrecondition.SMART_TERMINAL)
    def "returns metadata when stdout and stderr are attached to console"() {
        given:
        terminals.isTerminal(Terminals.Output.Stdout) >> true
        terminals.isTerminal(Terminals.Output.Stderr) >> true

        expect:
        detector.console != null
        detector.console.stdOut
        detector.console.stdErr
    }

    @Requires(TestPrecondition.SMART_TERMINAL)
    def "returns metadata when only stdout is attached to console"() {
        given:
        terminals.isTerminal(Terminals.Output.Stdout) >> true
        terminals.isTerminal(Terminals.Output.Stderr) >> false

        expect:
        detector.console != null
        detector.console.stdOut
        !detector.console.stdErr
    }

    @Requires(TestPrecondition.SMART_TERMINAL)
    def "returns metadata when only stderr is attached to console"() {
        given:
        terminals.isTerminal(Terminals.Output.Stdout) >> false
        terminals.isTerminal(Terminals.Output.Stderr) >> true

        expect:
        detector.console != null
        !detector.console.stdOut
        detector.console.stdErr
    }

    @Requires(TestPrecondition.UNIX)
    def "returns null when TERM is not set"() {
        given:
        env.removeEnvironmentVariable('TERM')

        expect:
        detector.console == null
    }

    def "returns null when TERM is set to dumb"() {
        given:
        env.setEnvironmentVariable("TERM", "dumb")

        expect:
        detector.console == null
    }
}