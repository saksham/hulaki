package com.dumbster.smtp.api;


import com.dumbster.smtp.exceptions.ApiProtocolException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.OutputStream;
import java.io.StringReader;

public abstract class ApiResponse {

    public void marshalResponse(OutputStream out) throws ApiProtocolException {
        try {
            JAXBContext context = JAXBContext.newInstance(this.getClass());
            Marshaller m = context.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            m.marshal(this, out);
        } catch (Exception ex) {
            throw new ApiProtocolException("Error while marshalling the response.", ex);
        }
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
