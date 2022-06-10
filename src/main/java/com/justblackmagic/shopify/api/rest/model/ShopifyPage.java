package com.justblackmagic.shopify.api.rest.model;

import java.util.ArrayList;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
public class ShopifyPage<T> extends ArrayList<T> {

	private static final long serialVersionUID = 7202410951814178409L;

	private String nextPageInfo;
	private String previousPageInfo;



	/**
	 * @return int
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((nextPageInfo == null) ? 0 : nextPageInfo.hashCode());
		result = prime * result + ((previousPageInfo == null) ? 0 : previousPageInfo.hashCode());
		return result;
	}


	/**
	 * @param obj
	 * @return boolean
	 */
	@Override
	public boolean equals(final Object obj) {
		return super.equals(obj);
	}


	/**
	 * @param name
	 * @param value
	 */
	@JsonAnySetter
	public void ignored(String name, Object value) {
		log.debug("ShopifyRestAPI Ignored Property: {} = {}", name, value);
	}
}
