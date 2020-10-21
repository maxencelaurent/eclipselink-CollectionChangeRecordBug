package com.github.maxencelaurent.elbug;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.maxencelaurent.elbug.rest.JacksonMapperProvider;
import java.io.ByteArrayOutputStream;
import java.io.File;
import org.apache.http.HttpEntity;
import org.apache.http.HttpMessage;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author maxence
 */
public class MyRESTClient {


    private static final Logger logger = LoggerFactory.getLogger(MyRESTClient.class);

    private String cookie;

    private final HttpClient client;
    private final String baseURL;

    private static ObjectMapper getObjectMapper() {
        return JacksonMapperProvider.getMapper();
    }

    public MyRESTClient(String baseURL) {
        this.client = HttpClientBuilder.create().build();
        this.baseURL = baseURL;
    }

    public String getBaseURL() {
        return baseURL;
    }

    private void setHeaders(HttpMessage msg) {
        msg.setHeader("Content-Type", "application/json");
        msg.setHeader("Accept", "*/*");
        msg.setHeader("Cookie", cookie);
        msg.setHeader("Managed-Mode", "false");
    }

    private String getEntityAsString(HttpEntity entity) throws Exception {
        if (entity != null) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            entity.writeTo(baos);
            return baos.toString("UTF-8");
        } else {
            return "";
        }
    }

    public <T> T get(String url, TypeReference<T> valueTypeRef) throws Exception {
        String get = this.get(url);
        if (get != null && !get.isEmpty()) {
            return getObjectMapper().readValue(get, valueTypeRef);
        } else {
            return null;
        }
    }

    public <T> T get(String url, Class<T> valueType) throws Exception {
        String get = this.get(url);
        if (get != null && !get.isEmpty()) {
            return getObjectMapper().readValue(get, valueType);
        } else {
            return null;
        }
    }

    public String get(String url) throws Exception {
        logger.info("GET" + " " + baseURL + url);
        HttpUriRequest get = new HttpGet(baseURL + url);
        setHeaders(get);

        HttpResponse response = client.execute(get);
        logger.info(" => " + response.getStatusLine());

        if (response.getStatusLine().getStatusCode() >= 300) {
            throw new Exception("Expected 2xx OK but got " + response.getStatusLine().getStatusCode());
        }

        return getEntityAsString(response.getEntity());
    }

    public String put(String url) throws Exception {
        return this.put(url, null);
    }

    public String put(String url, Object object) throws Exception {
        HttpResponse response = this._put(url, object);
        return getEntityAsString(response.getEntity());
    }

    public <T> T put(String url, Object object, Class<T> valueType) throws Exception {
        String response = this.put(url, object);
        return getObjectMapper().readValue(response, valueType);
    }

    private HttpResponse _put(String url, Object object) throws Exception {
        return this.sendRequest(url, "PUT", (object != null ? getObjectMapper().writeValueAsString(object) : null));
    }

    public String post(String url, Object object) throws Exception, Exception {
        HttpResponse response = this._post(url, object);
        String entity = this.getEntityAsString(response.getEntity());
        if (response.getStatusLine().getStatusCode() >= 400) {
            throw new Exception(entity);
        }
        return entity;
    }

    public <T> T post(String url, Object object, TypeReference<T> valueType) throws Exception, Exception {
        String post = this.post(url, object);
        if (post != null && !post.isEmpty()) {
            return getObjectMapper().readValue(post, valueType);
        } else {
            return null;
        }
    }

    public <T> T post(String url, Object object, Class<T> valueType) throws Exception, Exception {
        String post = this.post(url, object);
        if (post!=null && !post.isEmpty()){
            return getObjectMapper().readValue(post, valueType);
        } else {
            return null;
        }
    }

    private HttpResponse _post(String url, Object object) throws Exception {
        return this.sendRequest(url, "POST", (object != null ? getObjectMapper().writeValueAsString(object) : null));
    }

    private String post_asString(String url, Object object) throws Exception {
        String strObject = getObjectMapper().writeValueAsString(object);
        return this.postJSON_asString(url, strObject);
    }

    public String delete(String url) throws Exception, Exception {
        logger.info("DELETE " + baseURL + url);
        HttpUriRequest delete = new HttpDelete(baseURL + url);
        setHeaders(delete);

        HttpResponse response = client.execute(delete);

        logger.info(" => " + response.getStatusLine());

        if (response.getStatusLine().getStatusCode() >= HttpStatus.SC_BAD_REQUEST) {
            throw new Exception("DELETE failed");
        }

        return getEntityAsString(response.getEntity());

    }

    private HttpResponse sendRequest(String url, String method, String jsonContent) throws Exception, Exception {
        HttpEntityEnclosingRequestBase request = null;
        switch (method) {
            case "POST":
                request = new HttpPost(baseURL + url);
                break;
            case "PUT":
                request = new HttpPut(baseURL + url);
                break;
        }

        if (request != null) {
            setHeaders(request);

            logger.info(method + " " + baseURL + url + " WITH " + jsonContent);
            if (jsonContent != null) {
                StringEntity strEntity = new StringEntity(jsonContent, "UTF-8");
                request.setEntity(strEntity);
            }

            HttpResponse execute = client.execute(request);
            logger.info(" => " + execute.getStatusLine());

            return execute;
        } else {
            throw new Exception("Method not allowed");
        }
    }

    public <T> T postJSON_asString(String url, String jsonContent, Class<T> valueType) throws Exception, Exception {
        String postJSON_asString = this.postJSON_asString(url, jsonContent);
        if (postJSON_asString != null && !postJSON_asString.isEmpty()){
            return getObjectMapper().readValue(postJSON_asString, valueType);
        } else {
            return null;
        }
    }

    public String postJSON_asString(String url, String jsonContent) throws Exception, Exception {
        HttpResponse response = this.sendRequest(url, "POST", jsonContent);

        return getEntityAsString(response.getEntity());
    }

    public <T> T postJSONFromFile(String url, String jsonFile, Class<T> valueType) throws Exception {
        String postJSONFromFile = this.postJSONFromFile(url, jsonFile);
        if (postJSONFromFile != null && !postJSONFromFile.isEmpty()){
            return getObjectMapper().readValue(postJSONFromFile, valueType);
        } else {
            return null;
        }
    }

    public String postJSONFromFile(String url, String jsonFile) throws Exception {
        HttpPost post = new HttpPost(baseURL + url);
        setHeaders(post);

        FileEntity fileEntity = new FileEntity(new File(jsonFile));
        fileEntity.setContentType("application/json");
        post.setEntity(fileEntity);

        HttpResponse response = client.execute(post);

        if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
            throw new Exception("POST failed");
        }

        return getEntityAsString(response.getEntity());

    }
}
