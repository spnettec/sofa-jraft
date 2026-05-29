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
package com.alipay.sofa.jraft.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jiachun.fjc
 */
public final class SignalHelper {

    private static final Logger         LOG             = LoggerFactory.getLogger(SignalHelper.class);

    private static final SignalAccessor SIGNAL_ACCESSOR = getSignalAccessor0();

    public static final String          SIG_USR2        = "USR2";

    public static boolean supportSignal() {
        return SIGNAL_ACCESSOR != null;
    }

    /**
     * Registers user signal handlers.
     *
     * @param signalName a signal name
     * @param handlers   user signal handlers
     * @return true if support on current platform
     */
    public static boolean addSignal(final String signalName, final List<JRaftSignalHandler> handlers) {
        if (SIGNAL_ACCESSOR != null) {
            SIGNAL_ACCESSOR.addSignal(signalName, handlers);
            return true;
        }
        return false;
    }

    private static SignalAccessor getSignalAccessor0() {
        try {
            return new SignalAccessor();
        } catch (final Throwable t) {
            if (LOG.isWarnEnabled()) {
                LOG.warn("sun.misc.Signal: unavailable.", t);
            }
        }
        return null;
    }

    private SignalHelper() {
    }

    static class SignalAccessor {

        private final Class<?>      signalClass;
        private final Class<?>      signalHandlerClass;
        private final Constructor<?> signalConstructor;
        private final Method        signalHandleMethod;
        private final Method        signalGetNameMethod;

        SignalAccessor() throws ReflectiveOperationException {
            this.signalClass = Class.forName("sun.misc.Signal");
            this.signalHandlerClass = Class.forName("sun.misc.SignalHandler");
            this.signalConstructor = this.signalClass.getConstructor(String.class);
            this.signalHandleMethod = this.signalClass.getMethod("handle", this.signalClass, this.signalHandlerClass);
            this.signalGetNameMethod = this.signalClass.getMethod("getName");
        }

        public void addSignal(final String signalName, final List<JRaftSignalHandler> handlers) {
            try {
                final Object signal = this.signalConstructor.newInstance(signalName);
                final Object adapter = Proxy.newProxyInstance(this.signalHandlerClass.getClassLoader(),
                    new Class<?>[] { this.signalHandlerClass }, (proxy, method, args) -> {
                        if ("handle".equals(method.getName()) && args != null && args.length == 1) {
                            handleSignal(signal, args[0], handlers);
                        }
                        return null;
                    });
                this.signalHandleMethod.invoke(null, signal, adapter);
            } catch (final Throwable t) {
                LOG.error("Fail to add signal: {}.", signalName, t);
            }
        }

        private void handleSignal(final Object target, final Object signal, final List<JRaftSignalHandler> handlers) {
            try {
                if (!target.equals(signal)) {
                    return;
                }

                final String signalName = (String) this.signalGetNameMethod.invoke(signal);
                LOG.info("Handling signal {}.", signal);

                for (final JRaftSignalHandler h : handlers) {
                    h.handle(signalName);
                }
            } catch (final Throwable t) {
                LOG.error("Fail to handle signal: {}.", signal, t);
            }
        }
    }
}
