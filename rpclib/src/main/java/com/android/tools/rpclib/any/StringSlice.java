/*
 * Copyright (C) 2015 The Android Open Source Project
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
 *
 * THIS FILE WAS GENERATED BY codergen. EDIT WITH CARE.
 */
package com.android.tools.rpclib.any;

import org.jetbrains.annotations.NotNull;

import com.android.tools.rpclib.binary.BinaryClass;
import com.android.tools.rpclib.binary.BinaryID;
import com.android.tools.rpclib.binary.BinaryObject;
import com.android.tools.rpclib.binary.Decoder;
import com.android.tools.rpclib.binary.Encoder;
import com.android.tools.rpclib.binary.Namespace;

import java.io.IOException;

final class StringSlice extends Box implements BinaryObject {
    @Override
    public Object unwrap() {
        return getValue();
    }

    //<<<Start:Java.ClassBody:1>>>
    private String[] mValue;

    // Constructs a default-initialized {@link StringSlice}.
    public StringSlice() {}


    public String[] getValue() {
        return mValue;
    }

    public StringSlice setValue(String[] v) {
        mValue = v;
        return this;
    }

    @Override @NotNull
    public BinaryClass klass() { return Klass.INSTANCE; }

    private static final byte[] IDBytes = {80, 108, 0, 124, -76, 26, -9, -95, 89, 90, 110, 72, -88, 47, 7, 97, 113, 69, -72, 74, };
    public static final BinaryID ID = new BinaryID(IDBytes);

    static {
        Namespace.register(ID, Klass.INSTANCE);
    }
    public static void register() {}
    //<<<End:Java.ClassBody:1>>>
    public enum Klass implements BinaryClass {
        //<<<Start:Java.KlassBody:2>>>
        INSTANCE;

        @Override @NotNull
        public BinaryID id() { return ID; }

        @Override @NotNull
        public BinaryObject create() { return new StringSlice(); }

        @Override
        public void encode(@NotNull Encoder e, BinaryObject obj) throws IOException {
            StringSlice o = (StringSlice)obj;
            e.uint32(o.mValue.length);
            for (int i = 0; i < o.mValue.length; i++) {
                e.string(o.mValue[i]);
            }
        }

        @Override
        public void decode(@NotNull Decoder d, BinaryObject obj) throws IOException {
            StringSlice o = (StringSlice)obj;
            o.mValue = new String[d.uint32()];
            for (int i = 0; i <o.mValue.length; i++) {
                o.mValue[i] = d.string();
            }
        }
        //<<<End:Java.KlassBody:2>>>
    }
}
