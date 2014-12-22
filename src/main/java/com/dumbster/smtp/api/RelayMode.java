/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dumbster.smtp.api;

import com.dumbster.smtp.exceptions.ApiProtocolException;

public enum RelayMode {
    ADD("ADD"),
    REMOVE("REMOVE"),
    GET("GET");

    private String mode;

    RelayMode(String mode) {
        this.mode = mode;
    }


    @Override
    public String toString() {
        return this.mode;
    }

    public static RelayMode[] all() {
        return new RelayMode[] {ADD, REMOVE, GET};
    }

    public static RelayMode parse(String modeAsString) {
        for(RelayMode mode : all()) {
            if (mode.toString().equalsIgnoreCase(modeAsString)) {
                return mode;
            }
        }
        throw new ApiProtocolException("Could not parse the relay-mode: " + modeAsString);
    }
}
