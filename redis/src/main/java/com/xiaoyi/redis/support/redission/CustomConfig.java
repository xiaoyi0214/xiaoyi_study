package com.xiaoyi.redis.support.redission;


import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.fasterxml.jackson.databind.type.TypeFactory;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URI;
import org.redisson.api.RedissonNodeInitializer;
import org.redisson.client.codec.Codec;
import org.redisson.codec.ReferenceCodecProvider;
import org.redisson.config.Config;
import org.redisson.config.ConfigSupport.ClassMixIn;
import org.redisson.config.ConfigSupport.ConfigMixIn;
import org.redisson.connection.AddressResolverGroupFactory;
import org.redisson.connection.balancer.LoadBalancer;

public class CustomConfig {
    public CustomConfig() {
    }

    public static <T> T fromJSON(File file, Class<T> configType) throws IOException {
        patchUriObject();
        ObjectMapper jsonMapper = createMapper((JsonFactory)null, (ClassLoader)null);
        return jsonMapper.readValue(file, configType);
    }

    private static ObjectMapper createMapper(JsonFactory mapping, ClassLoader classLoader) {
        ObjectMapper mapper = new ObjectMapper(mapping);
        mapper.addMixIn(Config.class, ConfigMixIn.class);
        mapper.addMixIn(ReferenceCodecProvider.class, ClassMixIn.class);
        mapper.addMixIn(AddressResolverGroupFactory.class, ClassMixIn.class);
        mapper.addMixIn(Codec.class, ClassMixIn.class);
        mapper.addMixIn(RedissonNodeInitializer.class, ClassMixIn.class);
        mapper.addMixIn(LoadBalancer.class, ClassMixIn.class);
        FilterProvider filterProvider = (new SimpleFilterProvider()).addFilter("classFilter", SimpleBeanPropertyFilter.filterOutAllExcept(new String[0]));
        mapper.setFilterProvider(filterProvider);
        mapper.setSerializationInclusion(Include.NON_NULL);
        if (classLoader != null) {
            TypeFactory tf = TypeFactory.defaultInstance().withClassLoader(classLoader);
            mapper.setTypeFactory(tf);
        }

        mapper.getFactory().enable(Feature.ALLOW_COMMENTS);
        return mapper;
    }

    private static void patchUriObject() throws IOException {
        patchUriField("lowMask", "L_DASH");
        patchUriField("highMask", "H_DASH");
    }

    private static void patchUriField(String methodName, String fieldName) throws IOException {
        try {
            Method lowMask = URI.class.getDeclaredMethod(methodName, String.class);
            lowMask.setAccessible(true);
            Long lowMaskValue = (Long)lowMask.invoke((Object)null, "-_");
            Field lowDash = URI.class.getDeclaredField(fieldName);
            Field modifiers = Field.class.getDeclaredField("modifiers");
            modifiers.setAccessible(true);
            modifiers.setInt(lowDash, lowDash.getModifiers() & -17);
            lowDash.setAccessible(true);
            lowDash.setLong((Object)null, lowMaskValue);
        } catch (Exception var6) {
            throw new IOException(var6);
        }
    }
}