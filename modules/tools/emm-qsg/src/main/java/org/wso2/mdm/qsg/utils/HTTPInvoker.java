/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.mdm.qsg.utils;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.TrustStrategy;
import org.wso2.mdm.qsg.dto.HTTPResponse;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import java.io.*;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;

/**
 * Created by harshan on 7/21/16.
 */
public class HTTPInvoker {

    private static final String OAUTH_BEARER = "Bearer ";
    public static String oAuthToken;

    private static HttpClient createHttpClient ()
            throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
        HttpClientBuilder b = HttpClientBuilder.create();

        // setup a Trust Strategy that allows all certificates.
        //
        SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
            public boolean isTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                return true;
            }
        }).build();
        b.setSSLContext(sslContext);
        //b.setSSLHostnameVerifier(new NoopHostnameVerifier());

        // don't check Hostnames, either.
        //      -- use SSLConnectionSocketFactory.getDefaultHostnameVerifier(), if you don't want to weaken
        HostnameVerifier hostnameVerifier = SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;

        // here's the special part:
        //      -- need to create an SSL Socket Factory, to use our weakened "trust strategy";
        //      -- and create a Registry, to register it.
        //
        SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(sslContext, hostnameVerifier);
        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.getSocketFactory())
                .register("https", sslSocketFactory)
                .build();

        // now, we create connection-manager using our Registry.
        //      -- allows multi-threaded use
        PoolingHttpClientConnectionManager connMgr = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
        b.setConnectionManager( connMgr);

        // finally, build the HttpClient;
        //      -- done!
        CloseableHttpClient client = b.build();
        return client;
    }

    public static HTTPResponse sendHTTPPostWithURLParams (String url, List<NameValuePair> params, HashMap<String, String>
            headers) {
        HttpPost post = null;
        HttpResponse response = null;
        CloseableHttpClient httpclient = null;
        HTTPResponse httpResponse = new HTTPResponse();
        try {
            httpclient = (CloseableHttpClient) createHttpClient();
            post = new HttpPost(url);
            post.setEntity(new UrlEncodedFormEntity(params));
            for (String key :headers.keySet()) {
                post.setHeader(key, headers.get(key));
            }
            response = httpclient.execute(post);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }

        System.out.println("\nSending 'POST' request to URL : " + url);
        System.out.println("Post parameters : " + post.getEntity());
        System.out.println("Response Code : " +
                           response.getStatusLine().getStatusCode());

        BufferedReader rd = null;
        try {
            rd = new BufferedReader(
                    new InputStreamReader(response.getEntity().getContent()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        StringBuffer result = new StringBuffer();
        String line = "";
        try {
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        httpResponse.setResponseCode(response.getStatusLine().getStatusCode());
        httpResponse.setResponse(result.toString());
        return httpResponse;
    }

    public static HTTPResponse sendHTTPPost (String url, String payload, HashMap<String, String>
            headers) {
        HttpPost post = null;
        HttpResponse response = null;
        HTTPResponse httpResponse = new HTTPResponse();
        CloseableHttpClient httpclient = null;
        try {
            httpclient = (CloseableHttpClient) createHttpClient();
            StringEntity requestEntity = new StringEntity(payload, Constants.UTF_8);
            post = new HttpPost(url);
            post.setEntity(requestEntity);
            for (String key :headers.keySet()) {
                System.out.println(key);
                System.out.println(headers.get(key));
                post.setHeader(key, headers.get(key));
            }
            response = httpclient.execute(post);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }

        System.out.println("\nSending 'POST' request to URL : " + url);
        System.out.println("Post parameters : " + post.getEntity());
        System.out.println("Response Code : " +
                           response.getStatusLine().getStatusCode());

        BufferedReader rd = null;
        try {
            rd = new BufferedReader(
                    new InputStreamReader(response.getEntity().getContent()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        StringBuffer result = new StringBuffer();
        String line = "";
        try {
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        httpResponse.setResponseCode(response.getStatusLine().getStatusCode());
        httpResponse.setResponse(result.toString());
        return httpResponse;
    }

    public static HTTPResponse sendHTTPPostWithOAuthSecurity (String url, String payload, HashMap<String, String>
            headers) {
        HttpPost post = null;
        HttpResponse response = null;
        HTTPResponse httpResponse = new HTTPResponse();
        CloseableHttpClient httpclient = null;
        try {
            httpclient = (CloseableHttpClient) createHttpClient();
            StringEntity requestEntity = new StringEntity(payload, Constants.UTF_8);
            post = new HttpPost(url);
            post.setEntity(requestEntity);
            for (String key :headers.keySet()) {
                System.out.println(key);
                System.out.println(headers.get(key));
                post.setHeader(key, headers.get(key));
            }
            post.setHeader(Constants.AUTH_HEADER, OAUTH_BEARER + oAuthToken);
            response = httpclient.execute(post);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }

        System.out.println("\nSending 'POST' request to URL : " + url);
        System.out.println("Post parameters : " + post.getEntity());
        System.out.println("Response Code : " +
                           response.getStatusLine().getStatusCode());

        BufferedReader rd = null;
        try {
            rd = new BufferedReader(
                    new InputStreamReader(response.getEntity().getContent()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        StringBuffer result = new StringBuffer();
        String line = "";
        try {
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        httpResponse.setResponseCode(response.getStatusLine().getStatusCode());
        httpResponse.setResponse(result.toString());
        System.out.println(httpResponse.getResponse());
        return httpResponse;
    }

    public static HTTPResponse uploadFile (String url, String fileName, String fileContentType) {
        HttpPost post = null;
        HttpResponse response = null;
        HTTPResponse httpResponse = new HTTPResponse();
        CloseableHttpClient httpclient = null;
        try {
            httpclient = (CloseableHttpClient) createHttpClient();
            post = new HttpPost(url);
            File file = new File(fileName);

            MultipartEntity mpEntity = new MultipartEntity();
            ContentBody cbFile = new FileBody(file, fileContentType);
            mpEntity.addPart("file", cbFile);
            post.setEntity(mpEntity);
            post.setHeader(Constants.AUTH_HEADER, OAUTH_BEARER + oAuthToken);
            post.setHeader(Constants.CONTENT_TYPE_HEADER, "multipart/form-data");
            post.setHeader("Accept", Constants.APPLICATION_JSON);
            response = httpclient.execute(post);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }

        System.out.println("\nSending 'POST' request to URL : " + url);
        System.out.println("Post parameters : " + post.getEntity());
        System.out.println("Response Code : " +
                           response.getStatusLine().getStatusCode());

        BufferedReader rd = null;
        try {
            rd = new BufferedReader(
                    new InputStreamReader(response.getEntity().getContent()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        StringBuffer result = new StringBuffer();
        String line = "";
        try {
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        httpResponse.setResponseCode(response.getStatusLine().getStatusCode());
        httpResponse.setResponse(result.toString());
        return httpResponse;
    }
}