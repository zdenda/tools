/*
 * Copyright (C) 2012 The Android Open Source Project
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

package com.android.build.gradle.internal.dsl

import com.android.annotations.NonNull
import com.android.annotations.Nullable
import com.android.builder.core.AndroidBuilder
import com.android.builder.core.BuilderConstants
import com.android.builder.core.DefaultApiVersion
import com.android.builder.core.DefaultProductFlavor
import com.android.builder.model.ApiVersion
import com.android.builder.model.ClassField
import com.android.builder.model.NdkConfig
import com.android.builder.model.ProductFlavor
import org.gradle.api.Action
import org.gradle.api.internal.file.FileResolver
import org.gradle.api.logging.Logger
import org.gradle.internal.reflect.Instantiator

/**
 * DSL overlay to make methods that accept String... work.
 */
class ProductFlavorDsl extends DefaultProductFlavor {
    private static final long serialVersionUID = 1L

    @NonNull
    private final FileResolver fileResolver
    @NonNull
    protected final Logger logger

    private final NdkConfigDsl ndkConfig

    ProductFlavorDsl(@NonNull String name,
                     @NonNull FileResolver fileResolver,
                     @NonNull Instantiator instantiator,
                     @NonNull Logger logger) {
        super(name)
        this.fileResolver = fileResolver
        this.logger = logger
        ndkConfig = instantiator.newInstance(NdkConfigDsl.class)
    }

    @Override
    @Nullable
    public NdkConfig getNdkConfig() {
        return ndkConfig;
    }

    @NonNull
    public ProductFlavor setMinSdkVersion(int minSdkVersion) {
        setMinSdkVersion(new DefaultApiVersion(minSdkVersion));
        return this;
    }

    @NonNull
    public ProductFlavor minSdkVersion(int minSdkVersion) {
        setMinSdkVersion(minSdkVersion);
        return this;
    }

    @NonNull
    public ProductFlavor setMinSdkVersion(String minSdkVersion) {
        setMinSdkVersion(getApiVersion(minSdkVersion))
        return this;
    }

    @NonNull
    public ProductFlavor minSdkVersion(String minSdkVersion) {
        setMinSdkVersion(minSdkVersion);
        return this;
    }

    @NonNull
    public ProductFlavor setTargetSdkVersion(int targetSdkVersion) {
        setTargetSdkVersion(new DefaultApiVersion(targetSdkVersion));
        return this;
    }

    @NonNull
    public ProductFlavor targetSdkVersion(int targetSdkVersion) {
        setTargetSdkVersion(targetSdkVersion);
        return this;
    }

    @NonNull
    public ProductFlavor setTargetSdkVersion(String targetSdkVersion) {
        setTargetSdkVersion(getApiVersion(targetSdkVersion))
        return this;
    }

    @NonNull
    public ProductFlavor targetSdkVersion(String targetSdkVersion) {
        setTargetSdkVersion(targetSdkVersion);
        return this;
    }

    @Nullable
    private static ApiVersion getApiVersion(@Nullable String value) {
        if (value != null && !value.isEmpty()) {
            if (Character.isDigit(value.charAt(0))) {
                try {
                    int apiLevel = Integer.valueOf(value)
                    return new DefaultApiVersion(apiLevel)
                } catch (NumberFormatException e) {
                    throw new RuntimeException("'${value}' is not a valid API level. ", e)
                }
            }

            return new DefaultApiVersion(value)
        }

        return null
    }

    // -- DSL Methods. TODO remove once the instantiator does what I expect it to do.

    public void buildConfigField(
            @NonNull String type,
            @NonNull String name,
            @NonNull String value) {
        ClassField alreadyPresent = getBuildConfigFields().get(name);
        if (alreadyPresent != null) {
            String flavorName = getName();
            if (BuilderConstants.MAIN.equals(flavorName)) {
                logger.info(
                        "DefaultConfig: buildConfigField '$name' value is being replaced: ${alreadyPresent.value} -> $value");
            } else {
                logger.info(
                        "ProductFlavor($flavorName): buildConfigField '$name' value is being replaced: ${alreadyPresent.value} -> $value");
            }
        }
        addBuildConfigField(AndroidBuilder.createClassField(type, name, value));
    }

    public void resValue(
            @NonNull String type,
            @NonNull String name,
            @NonNull String value) {
        ClassField alreadyPresent = getResValues().get(name);
        if (alreadyPresent != null) {
            String flavorName = getName();
            if (BuilderConstants.MAIN.equals(flavorName)) {
                logger.info(
                        "DefaultConfig: resValue '$name' value is being replaced: ${alreadyPresent.value} -> $value");
            } else {
                logger.info(
                        "ProductFlavor($flavorName): resValue '$name' value is being replaced: ${alreadyPresent.value} -> $value");
            }
        }
        addResValue(AndroidBuilder.createClassField(type, name, value));
    }

    @NonNull
    public ProductFlavorDsl proguardFile(Object proguardFile) {
        proguardFiles.add(fileResolver.resolve(proguardFile))
        return this
    }

    @NonNull
    public ProductFlavorDsl proguardFiles(Object... proguardFileArray) {
        proguardFiles.addAll(fileResolver.resolveFiles(proguardFileArray).files)
        return this
    }

    @NonNull
    public ProductFlavorDsl setProguardFiles(Iterable<?> proguardFileIterable) {
        proguardFiles.clear()
        for (Object proguardFile : proguardFileIterable) {
            proguardFiles.add(fileResolver.resolve(proguardFile))
        }
        return this
    }

    @NonNull
    public ProductFlavorDsl consumerProguardFiles(Object... proguardFileArray) {
        consumerProguardFiles.addAll(fileResolver.resolveFiles(proguardFileArray).files)
        return this
    }

    @NonNull
    public ProductFlavorDsl setConsumerProguardFiles(Iterable<?> proguardFileIterable) {
        consumerProguardFiles.clear()
        for (Object proguardFile : proguardFileIterable) {
            consumerProguardFiles.add(fileResolver.resolve(proguardFile))
        }
        return this
    }

    void ndk(Action<NdkConfigDsl> action) {
        action.execute(ndkConfig)
    }

    void resConfig(@NonNull String config) {
        addResourceConfiguration(config);
    }

    void resConfigs(@NonNull String... config) {
        addResourceConfigurations(config);
    }
    void resConfigs(@NonNull Collection<String> config) {
        addResourceConfigurations(config);
    }
}
