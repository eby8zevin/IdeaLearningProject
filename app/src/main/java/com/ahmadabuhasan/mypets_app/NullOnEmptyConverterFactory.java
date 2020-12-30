package com.ahmadabuhasan.mypets_app;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;

/*
 * Created by Ahmad Abu Hasan on 30/12/2020
 */

public class NullOnEmptyConverterFactory extends Converter.Factory {

    @Override
    public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
        final Converter<ResponseBody, ?> delegate = retrofit.nextResponseBodyConverter(this, type, annotations);
        return (Converter<ResponseBody, Object>) body -> {
            if (body.contentLength() == 0) return null;
            return delegate.convert(body);
        };
    }
}
