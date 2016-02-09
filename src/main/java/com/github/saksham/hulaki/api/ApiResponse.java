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

package com.github.saksham.hulaki.api;


import com.github.saksham.hulaki.exceptions.ApiProtocolException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.StringReader;
import java.io.StringWriter;

public abstract class ApiResponse {
    public String marshalResponse() {
        StringWriter out = new StringWriter();
        try {
            JAXBContext context = JAXBContext.newInstance(this.getClass());
            Marshaller m = context.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            m.marshal(this, out);
        } catch (Exception ex) {
            throw new ApiProtocolException("Error while marshalling the response.", ex);
        }
        return out.toString();
    }

    public static ApiResponse unmarshalResponse(String response, Class responseClass) {
        try {
            JAXBContext context = JAXBContext.newInstance(responseClass);
            Unmarshaller um = context.createUnmarshaller();
            return (ApiResponse) um.unmarshal(new StringReader(response));
        } catch (Exception ex) {
            throw new ApiProtocolException("Could not parse the respose.", ex);
        }
    }
}
