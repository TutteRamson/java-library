/*
 * Copyright (c) 2013-2014.  Urban Airship and Contributors
 */

package com.urbanairship.api.client;

import com.urbanairship.api.client.model.APIClientResponse;
import com.urbanairship.api.client.model.APIListSingleChannelResponse;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;
import org.junit.Test;

import java.io.ByteArrayInputStream;

import static org.junit.Assert.*;


public class ListSingleChannelAPIResponseHandlerTest {

    /* Header keys, values */
    public final static String CONTENT_TYPE_KEY = "Content-type";
    public final static String CONTENT_TYPE_TEXT_HTML = "text/html";
    public final static String CONTENT_TYPE_JSON = "application/json";
    public final static String UA_JSON_RESPONSE =
            "application/vnd.urbanairship+json; version=3; charset=utf8;";

    @Test
    public void testHandleSuccess() {

        String response = "{  \n" +
                "  \"channel\":{  \n" +
                "    \"channel_id\":\"01234567-890a-bcde-f012-34567890abc0\",\n" +
                "    \"device_type\":\"android\",\n" +
                "    \"installed\":true,\n" +
                "    \"opt_in\":false,\n" +
                "    \"push_address\":null,\n" +
                "    \"created\":\"2014-07-12T00:45:01\",\n" +
                "    \"last_registration\":\"2014-08-06T00:33:25\",\n" +
                "    \"alias\":null,\n" +
                "    \"tags\":[  \n" +
                "\n" +
                "    ]\n" +
                "  }\n" +
                "}";

        HttpResponse httpResponse = new BasicHttpResponse(new BasicStatusLine(
                new ProtocolVersion("HTTP", 1, 1), 200, "OK"));
        InputStreamEntity inputStreamEntity = new InputStreamEntity(
                new ByteArrayInputStream(response.getBytes()),
                response.getBytes().length);
        httpResponse.setEntity(inputStreamEntity);
        ListSingleChannelAPIResponseHandler handler = new ListSingleChannelAPIResponseHandler();

        try {
            APIClientResponse<APIListSingleChannelResponse> res =
                    handler.handleResponse(httpResponse);
            assertNotNull(res);
            assertTrue(httpResponse.getStatusLine().toString().equals("HTTP/1.1 200 OK"));
        } catch (Exception ex) {
            ex.printStackTrace();
            fail("Exception " + ex);
        }

    }

    @Test
    public void testAPIV3Error() {
        String errorJson = "{\"ok\" : false,\"operation_id\" : \"OpID\"," +
                "\"error\" : \"Could not parse request body\"," +
                "\"error_code\" : 40000," +
                "\"details\" : {\"error\" : \"Unexpected token '#'\"," +
                "\"location\" : {\"line\" : 10,\"column\" : 3}}}";
        HttpResponse httpResponse = new BasicHttpResponse(new BasicStatusLine(
                new ProtocolVersion("HTTP", 1, 1), 400, "Bad Request"));
        InputStreamEntity inputStreamEntity = new InputStreamEntity(
                new ByteArrayInputStream(errorJson.getBytes()),
                errorJson.getBytes().length);
        httpResponse.setEntity(inputStreamEntity);
        httpResponse.setHeader(new BasicHeader(CONTENT_TYPE_KEY,
                UA_JSON_RESPONSE));

        ListSingleChannelAPIResponseHandler handler = new ListSingleChannelAPIResponseHandler();

        try {
            handler.handleResponse(httpResponse);
        } catch (APIRequestException ex) {
            APIErrorDetails details = ex.getError().get().getDetails().get();
            assertTrue("Incorrect error details", details.getError().equals("Unexpected token '#'"));
            assertTrue("HttpResponse set incorrectly", ex.getHttpResponse().equals(httpResponse));
            return;
        } catch (Exception ex) {
            fail("Incorrect exception thrown " + ex);
        }
        fail("Test should have succeeded by now");
    }

    @Test
    public void testDeprecatedJSONError() {
        /* Build a BasicHttpResponse */
        String pushJSON = "{\"message\":\"Unauthorized\"}";
        HttpResponse httpResponse = new BasicHttpResponse(new BasicStatusLine(
                new ProtocolVersion("HTTP", 1, 1), 400, "Unauthorized"));
        InputStreamEntity inputStreamEntity = new InputStreamEntity(
                new ByteArrayInputStream(pushJSON.getBytes()),
                pushJSON.getBytes().length);
        httpResponse.setEntity(inputStreamEntity);
        httpResponse.setHeader(new BasicHeader(CONTENT_TYPE_KEY, CONTENT_TYPE_JSON));

        ListSingleChannelAPIResponseHandler handler = new ListSingleChannelAPIResponseHandler();

        try {
            handler.handleResponse(httpResponse);
        } catch (APIRequestException ex) {

            APIError error = ex.getError().get();
            String errorMessage = error.getError();
            assertTrue("Error message incorrect", errorMessage.equals("Unauthorized"));
            return;
        } catch (Exception ex) {
            fail("Failed with incorrect exception " + ex);
        }
        fail("Test should have succeeded by now");
    }

    /**
     * Test the deprecated API response where only a string is returned.
     */
    @Test
    public void testDeprecatedStringError() {
        // Build a BasicHttpResponse
        String errorString = "Unauthorized";
        HttpResponse httpResponse = new BasicHttpResponse(new BasicStatusLine(
                new ProtocolVersion("HTTP", 1, 1), 400, "Unauthorized"));
        InputStreamEntity inputStreamEntity = new InputStreamEntity(
                new ByteArrayInputStream(errorString.getBytes()),
                errorString.getBytes().length);
        httpResponse.setEntity(inputStreamEntity);
        httpResponse.setHeader(new BasicHeader(CONTENT_TYPE_KEY,
                CONTENT_TYPE_TEXT_HTML));

        ListSingleChannelAPIResponseHandler handler = new ListSingleChannelAPIResponseHandler();

        try {
            handler.handleResponse(httpResponse);
        } catch (APIRequestException ex) {
            APIError error = ex.getError().get();
            assertTrue("String error message is incorrect",
                    error.getError().equals("Unauthorized"));
            return;
        } catch (Exception ex) {
            fail("Failed with incorrect exception " + ex);
        }
        fail("Test should have succeeded by now");
    }
}

