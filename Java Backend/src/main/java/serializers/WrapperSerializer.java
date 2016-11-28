package serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import model.Wrapper;

import java.io.IOException;

/**
 * Created by Robbe De Geyndt on 26/11/2016.
 */
public class WrapperSerializer extends JsonSerializer<Wrapper> {

    @Override
    public void serialize(Wrapper value, JsonGenerator gen, SerializerProvider serializers) throws IOException, JsonProcessingException {
        gen.writeStartObject();;
        gen.writeBooleanField("valid", value.getValid());
        gen.writeStringField("msg", value.getMsg());
        gen.writeStringField("name", value.getName());
        gen.writeObjectField("object", value.getObject());
        gen.writeEndObject();
    }
}