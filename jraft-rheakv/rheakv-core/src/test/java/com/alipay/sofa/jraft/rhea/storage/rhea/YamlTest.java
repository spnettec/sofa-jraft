/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alipay.sofa.jraft.rhea.storage.rhea;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ThreadPoolExecutor;

import com.alipay.sofa.jraft.util.concurrent.FixedThreadsExecutorGroup;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.module.SimpleModule;
import org.junit.Test;

import com.alipay.sofa.jraft.rhea.options.RheaKVStoreOptions;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.dataformat.yaml.YAMLMapper;

/**
 * @author jiachun.fjc
 */
public class YamlTest {

    @Test
    public void parseStoreEngineOptionsTest() throws IOException {
        final YAMLMapper.Builder builder = YAMLMapper.builder();
        final InputStream in = YamlTest.class.getResourceAsStream("/conf/rhea_test_cluster_1.yaml");
        SimpleModule module = new SimpleModule();
        module.addDeserializer(ThreadPoolExecutor.class, new ValueDeserializer<>() {
            @Override
            public ThreadPoolExecutor deserialize(JsonParser p, DeserializationContext ctxt) throws
                    JacksonException {
                return null;
            }
        });
        module.addDeserializer(FixedThreadsExecutorGroup.class, new ValueDeserializer<FixedThreadsExecutorGroup>() {
            @Override
            public FixedThreadsExecutorGroup deserialize(JsonParser p, DeserializationContext ctxt) throws
                                                                                                   JacksonException {
                return null;
            }
        });
        builder.addModule(module);
        ObjectMapper mapper = builder.build();
        final RheaKVStoreOptions opts = mapper.readValue(in, RheaKVStoreOptions.class);
        System.out.println(opts);
    }
}
