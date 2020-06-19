package com.tle.reporting.oda;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.datatools.connectivity.oda.IParameterMetaData;
import org.eclipse.datatools.connectivity.oda.OdaException;

import com.thoughtworks.xstream.XStream;
import com.tle.reporting.IResultSetExt;
import com.tle.reporting.LearningEdgeOdaDelegate;
import com.tle.reporting.oda.ui.TLEOdaPlugin;
import com.tle.reporting.oda.webservice.Constants;

public class RemoteLearningEdgeOdaDelegate implements LearningEdgeOdaDelegate
{
	private static final String STATUS_OK = "OK"; //$NON-NLS-1$
	private final HttpClient httpClient;
	private String url;
	private final XStream xstream;

	public RemoteLearningEdgeOdaDelegate(HttpClient httpClient, Properties properties, XStream xstream)
	{
		this.httpClient = httpClient;
		url = properties.getProperty(Constants.WEBSERVICE_URL);
		if( !url.endsWith("/") ) //$NON-NLS-1$
		{
			url += '/';
		}
		url += "reportingstream.do"; //$NON-NLS-1$
		this.xstream = xstream;
	}

	@SuppressWarnings("nls")
	public IResultSetExt executeQuery(String queryType, String query, List<Object> indexParams, int maxRows)
		throws OdaException
	{
		final List<NameValuePair> params = new ArrayList<>();
		params.add(new BasicNameValuePair("method", "query"));
		params.add(new BasicNameValuePair("type", queryType));
		params.add(new BasicNameValuePair("query", query));
		params.add(new BasicNameValuePair("maxRows", Integer.toString(maxRows)));
		if( indexParams != null && indexParams.size() > 0 )
		{
			params.add(new BasicNameValuePair("params", xstream.toXML(indexParams)));
		}

		final HttpPost method = createPOST(url, params);
		try
		{
			HttpResponse response = httpClient.execute(method);
			InputStream stream = response.getEntity().getContent();
			ObjectInputStream ois = new ObjectInputStream(stream);
			String status = (String) ois.readObject();
			if( !status.equals(STATUS_OK) )
			{
				String errorMsg = (String) ois.readObject();
				throw new OdaException(errorMsg);
			}
			return new StreamedResultSet(method, ois);
		}
		catch( Exception e )
		{
			method.releaseConnection();
			if( e instanceof OdaException )
			{
				throw (OdaException) e;
			}
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings({ "unchecked", "nls" })
	public Map<String, ?> getDatasourceMetadata(String queryType) throws OdaException
	{
		final List<NameValuePair> params = new ArrayList<>();
		params.add(new BasicNameValuePair("method", "metadata"));
		params.add(new BasicNameValuePair("type", queryType));
		
		final HttpPost method = createPOST(url, params);
		try
		{
			HttpResponse response = httpClient.execute(method);
			InputStream stream = response.getEntity().getContent();
			ObjectInputStream ois = new ObjectInputStream(stream);
			String status = (String) ois.readObject();
			if( !status.equals(STATUS_OK) )
			{
				String errorMsg = (String) ois.readObject();
				throw new OdaException(errorMsg);
			}
			return (Map<String, ?>) ois.readObject();
		}
		catch( Exception e )
		{
			if( e instanceof OdaException )
			{
				throw (OdaException) e;
			}
			throw new RuntimeException(e);
		}
		finally
		{
			method.releaseConnection();
		}
	}

	@SuppressWarnings("nls")
	public IParameterMetaData getParamterMetadata(String queryType, String query, List<Object> indexParams)
		throws OdaException
	{
		final List<NameValuePair> params = new ArrayList<>();
		params.add(new BasicNameValuePair("method", "paramMetadata"));
		params.add(new BasicNameValuePair("type", queryType));
		params.add(new BasicNameValuePair("query", query));
		if( indexParams != null && indexParams.size() > 0 )
		{
			params.add(new BasicNameValuePair("params", xstream.toXML(indexParams)));
		}

		final HttpPost method = createPOST(url, params);
		try
		{
			HttpResponse response = httpClient.execute(method);
			InputStream stream = response.getEntity().getContent();
			ObjectInputStream ois = new ObjectInputStream(stream);
			String status = (String) ois.readObject();
			if( !status.equals(STATUS_OK) )
			{
				String errorMsg = (String) ois.readObject();
				throw new OdaException(errorMsg);
			}
			return (IParameterMetaData) ois.readObject();
		}
		catch( Exception e )
		{
			throw new RuntimeException(e);
		}
		finally
		{
			method.releaseConnection();
		}
	}

	@SuppressWarnings("nls")
	public String login(String username, String password) throws OdaException
	{
		final List<NameValuePair> params = new ArrayList<>();
		params.add(new BasicNameValuePair("method", "login"));
		params.add(new BasicNameValuePair("username", username));
		params.add(new BasicNameValuePair("password", password));
		
		final HttpPost method = createPOST(url, params);
		try
		{
			HttpResponse response = httpClient.execute(method);
			InputStream stream = response.getEntity().getContent();
			ObjectInputStream ois = new ObjectInputStream(stream);
			String status = (String) ois.readObject();
			if( !status.equals(STATUS_OK) )
			{
				throw new OdaException("Failed to login: (" + status + ")");
			}
		}
		catch( Exception e )
		{
			method.releaseConnection();
			if( e instanceof OdaException )
			{
				throw (OdaException) e;
			}
			TLEOdaPlugin plugin = TLEOdaPlugin.getDefault();
			plugin.getLog().log(new Status(IStatus.ERROR, plugin.getBundle().getSymbolicName(), "ERROR", e));
			throw new RuntimeException(e);
		}
		method.releaseConnection();
		return ""; //$NON-NLS-1$
	}

	private HttpPost createPOST(String url, List<NameValuePair> params) {
		return new HttpPost(appendQueryString(url, queryStringNv(params)));
	}

	private String appendQueryString(String url, String queryString) {
		return url
			+ (queryString == null || queryString.equals("")
				? ""
				: (url.contains("?") ? '&' : '?') + queryString);
	  }

	  private String queryStringNv(List<NameValuePair> params) {
		if (params == null) {
		  return null;
		}
		return URLEncodedUtils.format(params, "utf-8");
	  }
}
