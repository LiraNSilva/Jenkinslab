/*
 * Copyright 2017 the original author or authors.
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

package org.gradle.internal.component.external.model;

import org.gradle.api.artifacts.VersionConstraint;
import org.gradle.api.capabilities.Capability;
import org.gradle.api.internal.attributes.ImmutableAttributes;
import org.gradle.internal.component.model.ExcludeMetadata;

import java.util.List;

public interface MutableComponentVariant {
    void addFile(String name, String uri);

    void addDependency(String group, String module, VersionConstraint versionConstraint, List<ExcludeMetadata> excludes, String reason, ImmutableAttributes attributes, List<? extends Capability> requestedCapabilities, boolean inheriting);

    void addDependencyConstraint(String group, String module, VersionConstraint versionConstraint, String reason, ImmutableAttributes attributes);

    void addCapability(String group, String name, String version);

    ImmutableAttributes getAttributes();

    void setAttributes(ImmutableAttributes updatedAttributes);
}
