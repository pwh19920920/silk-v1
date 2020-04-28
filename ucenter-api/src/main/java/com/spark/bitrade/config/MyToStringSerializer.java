package com.spark.bitrade.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JacksonStdImpl;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.lang.reflect.Type;

@JacksonStdImpl
public class MyToStringSerializer extends StdSerializer<Object> {
    public static final MyToStringSerializer instance = new MyToStringSerializer();
    protected boolean _isString = false;

    public MyToStringSerializer() {
        super(Object.class);
    }

    public MyToStringSerializer(Class<?> handledType) {
        super(handledType, false);
    }

    @Override
    public boolean isEmpty(SerializerProvider prov, Object value) {
        if (value == null) {
            return true;
        } else {
            String str = value.toString();
            return str.isEmpty();
        }
    }

    @Override
    public void serialize(Object value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        //兼容处理，长度大于16的long类型
        if (StringUtils.hasText(value.toString()) && value.toString().length() > 16) {
            _isString = true;
            gen.writeString(value.toString());
        } else {
            gen.writeNumber(value.toString());
        }
    }

    @Override
    public void serializeWithType(Object value, JsonGenerator gen, SerializerProvider provider, TypeSerializer typeSer) throws IOException {
        typeSer.writeTypePrefixForScalar(value, gen);
        this.serialize(value, gen, provider);
        typeSer.writeTypeSuffixForScalar(value, gen);
    }

    @Override
    public JsonNode getSchema(SerializerProvider provider, Type typeHint) throws JsonMappingException {
        return this.createSchemaNode(this._isString ? "string" : "number", true);
    }

    @Override
    public void acceptJsonFormatVisitor(JsonFormatVisitorWrapper visitor, JavaType typeHint) throws JsonMappingException {
        this.visitStringFormat(visitor, typeHint);
    }
}