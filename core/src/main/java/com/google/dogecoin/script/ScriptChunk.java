/*
 * Copyright 2013 Google Inc.
 * Copyright 2014 Andreas Schildbach
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.dogecoin.script;

import com.google.dogecoin.core.Utils;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

import javax.annotation.Nullable;

import static com.google.dogecoin.script.ScriptOpCodes.OP_0;
import static com.google.dogecoin.script.ScriptOpCodes.OP_1;
import static com.google.dogecoin.script.ScriptOpCodes.OP_16;
import static com.google.dogecoin.script.ScriptOpCodes.OP_1NEGATE;
import static com.google.dogecoin.script.ScriptOpCodes.OP_PUSHDATA1;
import static com.google.dogecoin.script.ScriptOpCodes.OP_PUSHDATA2;
import static com.google.dogecoin.script.ScriptOpCodes.OP_PUSHDATA4;
import static com.google.dogecoin.script.ScriptOpCodes.getOpCodeName;
import static com.google.dogecoin.script.ScriptOpCodes.getPushDataName;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * An element that is either an opcode or a raw byte array (signature, pubkey, etc).
 */
public class ScriptChunk {
    public final int opcode;
    @Nullable
    public final byte[] data;
    private int startLocationInProgram;

    public ScriptChunk(int opcode, byte[] data) {
        this(opcode, data, -1);
    }

    public ScriptChunk(int opcode, byte[] data, int startLocationInProgram) {
        this.opcode = opcode;
        this.data = data;
        this.startLocationInProgram = startLocationInProgram;
    }

    public boolean equalsOpCode(int opcode) {
        return opcode == this.opcode;
    }

    /**
     * If this chunk is a single byte of non-pushdata content (could be OP_RESERVED or some invalid Opcode)
     */
    public boolean isOpCode() {
        return opcode > OP_PUSHDATA4;
    }

    /**
     * Returns true if this chunk is pushdata content, including the single-byte pushdatas.
     */
    public boolean isPushData() {
        return opcode <= OP_16;
    }

    public int getStartLocationInProgram() {
        checkState(startLocationInProgram >= 0);
        return startLocationInProgram;
    }

    /**
     * Called on a pushdata chunk, returns true if it uses the smallest possible way (according to BIP62) to push the data.
     */
    public boolean isShortestPossiblePushData() {
        checkState(isPushData());
        if (data.length == 0)
            return opcode == OP_0;
        if (data.length == 1) {
            byte b = data[0];
            if (b >= 0x01 && b <= 0x10)
                return opcode == OP_1 + b - 1;
            if (b == 0x81)
                return opcode == OP_1NEGATE;
        }
        if (data.length < OP_PUSHDATA1)
            return opcode == data.length;
        if (data.length < 256)
            return opcode == OP_PUSHDATA1;
        if (data.length < 65536)
            return opcode == OP_PUSHDATA2;

        // can never be used, but implemented for completeness
        return opcode == OP_PUSHDATA4;
    }

    public void write(OutputStream stream) throws IOException {
        if (isOpCode()) {
            checkState(data == null);
            stream.write(opcode);
        } else if (data != null) {
            checkNotNull(data);
            if (opcode < OP_PUSHDATA1) {
                checkState(data.length == opcode);
                stream.write(opcode);
            } else if (opcode == OP_PUSHDATA1) {
                checkState(data.length <= 0xFF);
                stream.write(OP_PUSHDATA1);
                stream.write(data.length);
            } else if (opcode == OP_PUSHDATA2) {
                checkState(data.length <= 0xFFFF);
                stream.write(OP_PUSHDATA2);
                stream.write(0xFF & data.length);
                stream.write(0xFF & (data.length >> 8));
            } else if (opcode == OP_PUSHDATA4) {
                checkState(data.length <= Script.MAX_SCRIPT_ELEMENT_SIZE);
                stream.write(OP_PUSHDATA4);
                Utils.uint32ToByteStreamLE(data.length, stream);
            } else {
                throw new RuntimeException("Unimplemented");
            }
            stream.write(data);
        } else {
            stream.write(opcode); // smallNum
        }
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        if (isOpCode()) {
            buf.append(getOpCodeName(opcode));
        } else if (data != null) {
            // Data chunk
            buf.append(getPushDataName(opcode));
            buf.append("[");
            buf.append(Utils.HEX.encode(data));
            buf.append("]");
        } else {
            // Small num
            buf.append(Script.decodeFromOpN(opcode));
        }
        return buf.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ScriptChunk other = (ScriptChunk) o;

        if (opcode != other.opcode) return false;
        if (startLocationInProgram != other.startLocationInProgram) return false;
        if (!Arrays.equals(data, other.data)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = opcode;
        result = 31 * result + (data != null ? Arrays.hashCode(data) : 0);
        result = 31 * result + startLocationInProgram;
        return result;
    }
}
