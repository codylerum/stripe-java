package com.stripe.net;

import com.stripe.BaseStripeTest;
import com.stripe.Stripe;
import com.stripe.exception.AuthenticationException;
import com.stripe.exception.InvalidRequestException;
import com.stripe.net.OAuth;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.net.URL;
import java.net.URLDecoder;
import java.net.MalformedURLException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class OAuthTest extends BaseStripeTest {
	@Before
	public void setUpClientId() {
		Stripe.clientId = "ca_test";
	}

	@After
	public void tearDownClientId() {
		Stripe.clientId = null;
	}

	private static Map<String, String> splitQuery(String query) throws UnsupportedEncodingException {
		Map<String, String> queryPairs = new HashMap<String, String>();
		String[] pairs = query.split("&");
		for (String pair : pairs) {
			int idx = pair.indexOf("=");
			queryPairs.put(URLDecoder.decode(pair.substring(0, idx), "UTF8"), URLDecoder.decode(pair.substring(idx + 1), "UTF8"));
		}
		return queryPairs;
	}

	@Test
	public void testAuthorizeURL() throws AuthenticationException, InvalidRequestException, MalformedURLException, UnsupportedEncodingException {
		Map<String, Object> urlParams = new HashMap<String, Object>();
		urlParams.put("scope", "read_write");
		urlParams.put("state", "csrf_token");
		Map<String, Object> stripeUserParams = new HashMap<String, Object>();
		stripeUserParams.put("email", "test@example.com");
		stripeUserParams.put("url", "https://example.com/profile/test");
		stripeUserParams.put("country", "US");
		urlParams.put("stripe_user", stripeUserParams);

		String urlStr = OAuth.authorizeURL(urlParams, null);

		URL url = new URL(urlStr);
		Map<String, String> queryPairs = splitQuery(url.getQuery());

		assertEquals("https", url.getProtocol());
		assertEquals("connect.stripe.com", url.getHost());
		assertEquals("/oauth/authorize", url.getPath());

		assertEquals("ca_test", queryPairs.get("client_id"));
		assertEquals("read_write", queryPairs.get("scope"));
		assertEquals("test@example.com", queryPairs.get("stripe_user[email]"));
		assertEquals("https://example.com/profile/test", queryPairs.get("stripe_user[url]"));
		assertEquals("US", queryPairs.get("stripe_user[country]"));
	}
}
