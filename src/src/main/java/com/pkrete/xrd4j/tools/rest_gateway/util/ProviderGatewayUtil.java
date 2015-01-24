package com.pkrete.xrd4j.tools.rest_gateway.util;

import com.pkrete.xrd4j.common.message.ServiceRequest;
import com.pkrete.xrd4j.common.util.MessageHelper;
import com.pkrete.xrd4j.rest.converter.JSONToXMLConverter;
import com.pkrete.xrd4j.tools.rest_gateway.endpoint.ProviderEndpoint;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides utility methods for Provider Gateway implementation.
 *
 * @author Petteri Kivimäki
 */
public class ProviderGatewayUtil {

    private final static Logger logger = LoggerFactory.getLogger(ProviderGatewayUtil.class);

    /**
     * Goes through the given properties and extracts all the defined provider
     * endpoints. Returns a map containing service id - provider endpoint
     * key-value pairs.
     * @param endpoints endpoint properties
     * @param props REST Provider Gateway general properties
     * @return map containing service id - provider endpoint key-value pairs
     */
    public static Map<String, ProviderEndpoint> extractProviders(Properties endpoints, Properties props) {
        Map<String, ProviderEndpoint> results = new HashMap<String, ProviderEndpoint>();
        logger.info("Start extracting provider endpoints from properties.");
        if (endpoints == null || endpoints.isEmpty()) {
            logger.warn("No endpoints were founds. The list was null or empty.");
            return results;
        }

        int i = 0;
        String key = Integer.toString(i);

        // Loop through all the endpoints
        while (endpoints.containsKey(key + "." + Constants.ENDPOINT_PROPS_ID)) {

            String id = endpoints.getProperty(key + "." + Constants.ENDPOINT_PROPS_ID);
            String url = endpoints.getProperty(key + "." + Constants.PROVIDER_PROPS_URL);

            if (id == null || id.isEmpty() || url == null || url.isEmpty()) {
                logger.warn("ID or URL is null or empty. Provider endpoint skipped.");
                i++;
                key = Integer.toString(i);
                continue;
            }

            ProviderEndpoint endpoint = new ProviderEndpoint(id, url);
            // Set default values to namespace properties
            endpoint.setNamespaceDeserialize(props.getProperty(Constants.ENDPOINT_PROPS_SERVICE_NAMESPACE_DESERIALIZE));
            endpoint.setNamespaceSerialize(props.getProperty(Constants.ENDPOINT_PROPS_SERVICE_NAMESPACE_SERIALIZE));
            endpoint.setPrefix(props.getProperty(Constants.ENDPOINT_PROPS_SERVICE_NAMESPACE_PREFIX_SERIALIZE));

            logger.info("New provider endpoint found. ID : \"{}\", URL : \"{}\".", id, url);

            // HTTP verb
            if (endpoints.containsKey(key + "." + Constants.ENDPOINT_PROPS_VERB)) {
                String value = endpoints.getProperty(key + "." + Constants.ENDPOINT_PROPS_VERB);
                endpoint.setHttpVerb(value);
                logger.info("\"{}\" setting found. Value : \"{}\".", Constants.ENDPOINT_PROPS_VERB, value);
            }
            // Content-Type HTTP header
            if (endpoints.containsKey(key + "." + Constants.PROVIDER_PROPS_CONTENT_TYPE)) {
                String value = endpoints.getProperty(key + "." + Constants.PROVIDER_PROPS_CONTENT_TYPE);
                endpoint.setContentType(value);
                logger.info("\"{}\" setting found. Value : \"{}\".", Constants.PROVIDER_PROPS_CONTENT_TYPE, value);
            }
            // Accept HTTP header
            if (endpoints.containsKey(key + "." + Constants.PROVIDER_PROPS_ACCEPT)) {
                String value = endpoints.getProperty(key + "." + Constants.PROVIDER_PROPS_ACCEPT);
                endpoint.setAccept(value);
                logger.info("\"{}\" setting found. Value : \"{}\".", Constants.PROVIDER_PROPS_ACCEPT, value);
            }
            // Object tag
            if (endpoints.containsKey(key + "." + Constants.PROVIDER_PROPS_OBJECT_TAG)) {
                String value = endpoints.getProperty(key + "." + Constants.PROVIDER_PROPS_OBJECT_TAG);
                endpoint.setObjectTagName(value);
                logger.info("\"{}\" setting found. Value : \"{}\".", Constants.PROVIDER_PROPS_OBJECT_TAG, value);
            }
            // Array tag
            if (endpoints.containsKey(key + "." + Constants.PROVIDER_PROPS_ARRAY_TAG)) {
                String value = endpoints.getProperty(key + "." + Constants.PROVIDER_PROPS_ARRAY_TAG);
                endpoint.setArrayTagName(value);
                logger.info("\"{}\" setting found. Value : \"{}\".", Constants.PROVIDER_PROPS_ARRAY_TAG, value);
            }
            // Attachment
            if (endpoints.containsKey(key + "." + Constants.PROVIDER_PROPS_ATTACHMENT)) {
                String value = endpoints.getProperty(key + "." + Constants.PROVIDER_PROPS_ATTACHMENT);
                endpoint.setAttachment(MessageHelper.strToBool(value));
                logger.info("\"{}\" setting found. Value : \"{}\".", Constants.PROVIDER_PROPS_ATTACHMENT, value);
            }
            // X-Road headers
            if (endpoints.containsKey(key + "." + Constants.PROVIDER_PROPS_SEND_XRD_HEADERS)) {
                String value = endpoints.getProperty(key + "." + Constants.PROVIDER_PROPS_SEND_XRD_HEADERS);
                endpoint.setSendXrdHeaders(MessageHelper.strToBool(value));
                logger.info("\"{}\" setting found. Value : \"{}\".", Constants.PROVIDER_PROPS_SEND_XRD_HEADERS, value);
            }
            // ServiceRequest namespace
            if (endpoints.containsKey(key + "." + Constants.ENDPOINT_PROPS_SERVICE_NAMESPACE_DESERIALIZE)) {
                String value = endpoints.getProperty(key + "." + Constants.ENDPOINT_PROPS_SERVICE_NAMESPACE_DESERIALIZE);
                endpoint.setNamespaceDeserialize(value);
                logger.info("\"{}\" setting found. Value : \"{}\".", Constants.ENDPOINT_PROPS_SERVICE_NAMESPACE_DESERIALIZE, value);
            }
            // ServiceResponse namespace
            if (endpoints.containsKey(key + "." + Constants.ENDPOINT_PROPS_SERVICE_NAMESPACE_SERIALIZE)) {
                String value = endpoints.getProperty(key + "." + Constants.ENDPOINT_PROPS_SERVICE_NAMESPACE_SERIALIZE);
                endpoint.setNamespaceSerialize(value);
                logger.info("\"{}\" setting found. Value : \"{}\".", Constants.ENDPOINT_PROPS_SERVICE_NAMESPACE_SERIALIZE, value);
            }
            // ServiceResponse namespace prefix
            if (endpoints.containsKey(key + "." + Constants.ENDPOINT_PROPS_SERVICE_NAMESPACE_PREFIX_SERIALIZE)) {
                String value = endpoints.getProperty(key + "." + Constants.ENDPOINT_PROPS_SERVICE_NAMESPACE_PREFIX_SERIALIZE);
                endpoint.setPrefix(value);
                logger.info("\"{}\" setting found. Value : \"{}\".", Constants.ENDPOINT_PROPS_SERVICE_NAMESPACE_PREFIX_SERIALIZE, value);
            }
            results.put(id, endpoint);

            // Increase counter by one
            i++;
            // Update keyD
            key = Integer.toString(i);
        }
        logger.info("{} provider endpoints extracted from properties.", results.size());
        return results;
    }

    /**
     * Generates a Map containing HTTP headers that are sent to the service
     * endpoint.
     * @param request ServiceRequest holding X-Road specific headers
     * @param endpoint ProviderEndpoint holding standard HTTP headers
     * @return Map containing HTTP headers
     */
    public static Map<String, String> generateHttpHeaders(ServiceRequest request, ProviderEndpoint endpoint) {
        logger.info("Generate HTTP headers.");
        Map<String, String> headers = new HashMap<String, String>();
        if (endpoint.isSendXrdHeaders()) {
            logger.debug("Generate X-Road specific headers.");
            headers.put(Constants.XRD_HEADER_CLIENT, request.getConsumer().toString());
            headers.put(Constants.XRD_HEADER_SERVICE, request.getProducer().toString());
            headers.put(Constants.XRD_HEADER_MESSAGE_ID, request.getId());
            logger.debug("{} : {}", Constants.XRD_HEADER_CLIENT, request.getConsumer().toString());
            logger.debug("{} : {}", Constants.XRD_HEADER_SERVICE, request.getProducer().toString());
            logger.debug("{} : {}", Constants.XRD_HEADER_MESSAGE_ID, request.getId());
            if (request.getUserId() != null && !request.getUserId().isEmpty()) {
                logger.debug("{} : {}", Constants.XRD_HEADER_USER_ID, request.getUserId());
                headers.put(Constants.XRD_HEADER_USER_ID, request.getUserId());
            }
        } else {
            logger.debug("Generation of X-Road specific headers is disabled.");
        }
        if (endpoint.getContentType() != null && !endpoint.getContentType().isEmpty()) {
            logger.debug("{} : {}", Constants.HTTP_HEADER_CONTENT_TYPE, endpoint.getContentType());
            headers.put(Constants.HTTP_HEADER_CONTENT_TYPE, endpoint.getContentType());
        }
        if (endpoint.getAccept() != null && !endpoint.getAccept().isEmpty()) {
            logger.debug("{} : {}", Constants.HTTP_HEADER_ACCEPT, endpoint.getAccept());
            headers.put(Constants.HTTP_HEADER_ACCEPT, endpoint.getAccept());
        }
        logger.info("HTTP headers were succesfully generated.");
        return headers;
    }

    /**
     * Converts JSON string to XML string. XML string is wrapped inside
     * wrapper element defined by the given endpoint.
     * @param data JSON string to be converted
     * @param endpoint provider endpoint
     * @return XML string
     */
    public static String fromJSONToXML(String data, ProviderEndpoint endpoint) {
        // Get wrapper tag's name
        String wrapper = endpoint.getObjectTagName();
        // Modify JSON string, not required, but makes the output
        // more readable
        if (data.startsWith("[")) {
            data = "{\"" + wrapper + "\":" + data + "}";
            wrapper = endpoint.getArrayTagName();
        }
        // Convert service endpoint's response to XML
        data = new JSONToXMLConverter().convert(data);
        // Return data inside wrapper element
        return "<" + wrapper + ">" + data + "</" + wrapper + ">";
    }
}