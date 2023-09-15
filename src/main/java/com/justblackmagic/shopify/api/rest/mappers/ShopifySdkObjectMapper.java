package com.justblackmagic.shopify.api.rest.mappers;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.fasterxml.jackson.module.jakarta.xmlbind.JakartaXmlBindAnnotationIntrospector;


/**
 * Instead of using the default Spring ObjectMapper we are using a custom one for the Shopify REST API. This way, we can customize the serialization
 * and deserialization process for the Shopify REST API, seperately from any Jackson based REST services we may wish to vend from our application
 * (which can then use the default Spring ObjectMapper).
 * 
 * @author justblackmagic
 */
public class ShopifySdkObjectMapper {

	private ShopifySdkObjectMapper() {}


	/**
	 * @return ObjectMapper
	 * 
	 */
	public static ObjectMapper buildMapper() {
		final AnnotationIntrospector pair = AnnotationIntrospector.pair(new JakartaXmlBindAnnotationIntrospector(TypeFactory.defaultInstance()),
				new JacksonAnnotationIntrospector());

		ObjectMapper objectMapper = JsonMapper.builder().enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
				.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS).configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
				.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).configure(MapperFeature.USE_ANNOTATIONS, true)
				.annotationIntrospector(pair).serializationInclusion(Include.NON_NULL).build();
		objectMapper.registerModule(new JodaModule());
		return objectMapper;
	}
}
