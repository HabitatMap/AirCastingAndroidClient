/**
    AirCasting - Share your Air!
    Copyright (C) 2011-2012 HabitatMap, Inc.

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

    You can contact the authors by email at <info@habitatmap.org>
*/
package pl.llp.aircasting.util.http;

import cz.msebera.android.httpclient.client.methods.HttpDelete;
import pl.llp.aircasting.android.Logger;
import pl.llp.aircasting.helper.SettingsHelper;
import pl.llp.aircasting.util.Constants;

import com.google.common.base.Joiner;
import com.google.common.io.CharStreams;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Inject;
import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpException;
import cz.msebera.android.httpclient.HttpRequest;
import cz.msebera.android.httpclient.HttpRequestInterceptor;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.auth.AuthScope;
import cz.msebera.android.httpclient.auth.AuthState;
import cz.msebera.android.httpclient.auth.Credentials;
import cz.msebera.android.httpclient.auth.UsernamePasswordCredentials;
import cz.msebera.android.httpclient.client.methods.HttpGet;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.client.methods.HttpUriRequest;
import cz.msebera.android.httpclient.client.protocol.ClientContext;
import cz.msebera.android.httpclient.entity.mime.MultipartEntityBuilder;
import cz.msebera.android.httpclient.entity.mime.content.ByteArrayBody;
import cz.msebera.android.httpclient.entity.mime.content.ContentBody;
import cz.msebera.android.httpclient.entity.mime.content.StringBody;
import cz.msebera.android.httpclient.impl.auth.BasicScheme;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.protocol.HttpContext;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.io.Closeables.closeQuietly;
import static java.net.URLEncoder.encode;

public class HttpBuilder implements ChooseMethod, ChoosePath, PerformRequest
{
    @Inject static Gson gson;
    @Inject static SettingsHelper settingsHelper;

    private String login;
    private String password;
    private boolean useLogin;
    private List<Parameter> parameters = newArrayList();
    private String address;
    private Method method;

    private enum Method {
        GET, POST, DELETE;
    }

    public static ChooseMethod http() {
        return new HttpBuilder();
    }

    public static <T> HttpResult<T> error() {
        HttpResult<T> httpResult = new HttpResult<T>();
        httpResult.setStatus(Status.ERROR);

        return httpResult;
    }

    @Override
    public PerformRequest from(String path) {
        return to(path);
    }

    @Override
    public PerformRequest to(String address) {
        this.address = address;

        return this;
    }

    @Override
    public PerformRequest with(String key, String value) {
        Parameter parameter = new StringParameter(key, value);
        parameters.add(parameter);

        return this;
    }

    @Override
    public PerformRequest upload(String key, Uploadable thing) {
        Parameter parameter = new FileParameter(key, thing);
        parameters.add(parameter);

        return this;
    }

    @Override
    public PerformRequest authenticate(String login, String password) {
        this.login = login;
        this.password = password;
        this.useLogin = true;

        return this;
    }

    @Override
    public ChoosePath post() {
        method = Method.POST;

        return this;
    }

    @Override
    public ChoosePath get() {
        this.method = Method.GET;

        return this;
    }

    @Override
    public ChoosePath delete() {
        this.method = Method.DELETE;

        return this;
    }

    public HttpResult<Void> execute() {
        return into(Void.class);
    }

    public <T> HttpResult<T> into(Type target) {
        if (method == Method.POST) {
            return doPost(target);
        } else if (method == Method.GET) {
            return doGet(target);
        } else {
            return doDelete(target);
        }
    }

    private URI createPath(String path) throws URISyntaxException {
        return createPath(path, null);
    }

    private URI createPath(String path, String query) throws URISyntaxException {
        return new URI("http",
                null,
                settingsHelper.getBackendURL(),
                settingsHelper.getBackendPort(),
                path,
                query,
                null
        );
    }

    private String query() {
        StringBuilder query = new StringBuilder();

        for (Parameter parameter : parameters) {
            if (parameter.supportsGet()) {
                String name = parameter.key;
                String value = encode(parameter.getValue());

                query.append(name).append("=")
                        .append(value).append("&");
            }
        }

        return query.toString();
    }

    private <T> HttpResult<T> doGet(Type target) {
        try {
            URI path = createPath(address, query());
            HttpGet get = new HttpGet(path);

            return doRequest(get, target);
        } catch (URISyntaxException e) {
            Logger.e("Couldn't create path", e);
            return error();
        }
    }

    private <T> HttpResult<T> doDelete(Type target) {
        try {
            URI path = createPath(address, query());
            HttpDelete delete = new HttpDelete(path);

            return doRequest(delete, target);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return error();
        }
    }

    private <T> HttpResult<T> doPost(Type target) {
        try {
            URI path = createPath(address);
            HttpPost post = new HttpPost(path);

            HttpEntity entity = prepareMultipart();

            post.setEntity(entity);

            return doRequest(post, target);
        } catch (UnsupportedEncodingException e) {
            Logger.e("Couldn't process parameters", e);
            return error();
        } catch (URISyntaxException e) {
            Logger.e("Couldn't create path", e);
            return error();
        }
    }

    private HttpEntity prepareMultipart() throws UnsupportedEncodingException {
        MultipartEntityBuilder result = MultipartEntityBuilder.create();

        for (Parameter parameter : parameters) {
            result.addPart(parameter.getKey(), parameter.toBody());
        }

        return result.build();
    }

  private <T> HttpResult<T> doRequest(HttpUriRequest request, Type target)
  {
    DefaultHttpClient client = new DefaultHttpClient();
    HttpResult<T> result = new HttpResult<T>();
    Reader reader = null;
    InputStream content = null;

    // scoped so high for debugging connectivity in devMode
    String fullJson = null;
    try
    {
      client.addRequestInterceptor(preemptiveAuth(), 0);

      HttpResponse response = client.execute(request);

      if (response.getStatusLine().getStatusCode() == 204) {
          result.setStatus(Status.SUCCESS);
          return result;
      }

      content = response.getEntity().getContent();

      reader = new InputStreamReader(content);


      if(Constants.isDevMode())
      {
        List<String> strings = CharStreams.readLines(reader);
        fullJson = Joiner.on("\n").join(strings);
        reader = new StringReader(fullJson);
        Logger.i("Full http json: " + fullJson);
      }

      if(response.getStatusLine().getStatusCode() < 300) {
        result.setStatus(Status.SUCCESS);

        T output = gson.fromJson(reader, target);
        result.setContent(output);
      }
      else {
        result.setStatus(Status.FAILURE);

        Type type = new TypeToken<Map<String, String[]>>(){}.getType();
        Map<String, String[]> output = gson.fromJson(reader, type);
        result.setErrors(output);
      }
    }
    catch (Exception e)
    {
      Logger.e("Http request failed", e);
      result.setStatus(Status.ERROR);

      return result;
    }
    finally
    {
      closeQuietly(content);
      closeQuietly(reader);
      client.getConnectionManager().shutdown();
    }

    return result;
  }

  private HttpRequestInterceptor preemptiveAuth() {
        return new HttpRequestInterceptor() {
            @Override
            public void process(HttpRequest httpRequest, HttpContext httpContext) throws HttpException, IOException {
                AuthState authState = (AuthState) httpContext.getAttribute(ClientContext.TARGET_AUTH_STATE);

                Credentials credentials;
                if (useLogin) {
                    credentials = new UsernamePasswordCredentials(login, password);
                } else {
                    credentials = new UsernamePasswordCredentials(settingsHelper.getAuthToken(), "X");
                }

                authState.setAuthScope(AuthScope.ANY);
                authState.setAuthScheme(new BasicScheme());
                authState.setCredentials(credentials);
            }
        };
    }

    private abstract static class Parameter {
        private String key;

        public abstract ContentBody toBody() throws UnsupportedEncodingException;

        public Parameter(String key) {
            this.key = key;
        }

        public String getKey() {
            return key;
        }

        public abstract boolean supportsGet();

        public abstract String getValue();
    }

    private static class StringParameter extends Parameter {
        public String value;

        private StringParameter(String key, String value) {
            super(key);
            this.value = value;
        }

        @Override
        public ContentBody toBody() throws UnsupportedEncodingException {
            return new StringBody(value);
        }

        @Override
        public boolean supportsGet() {
            return true;
        }

        @Override
        public String getValue() {
            return value;
        }
    }

    private static class FileParameter extends Parameter {
        private Uploadable uploadable;

        public FileParameter(String key, Uploadable uploadable) {
            super(key);
            this.uploadable = uploadable;
        }

        @Override
        public ContentBody toBody() throws UnsupportedEncodingException {
            return new ByteArrayBody(uploadable.getData(), uploadable.getFilename());
        }

        @Override
        public boolean supportsGet() {
            return false;
        }

        @Override
        public String getValue() {
           return null;
        }
    }
}