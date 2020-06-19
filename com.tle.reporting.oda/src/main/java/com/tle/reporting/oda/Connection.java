package com.tle.reporting.oda;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.eclipse.datatools.connectivity.oda.IConnection;
import org.eclipse.datatools.connectivity.oda.IDataSetMetaData;
import org.eclipse.datatools.connectivity.oda.IQuery;
import org.eclipse.datatools.connectivity.oda.OdaException;

import com.ibm.icu.util.ULocale;
import com.thoughtworks.xstream.XStream;
import com.tle.reporting.LearningEdgeOdaDelegate;
import com.tle.reporting.oda.webservice.Constants;

public class Connection implements IConnection
{
	private Map<Object, Object> appContext;
	private List<Query> openQueries = new ArrayList<Query>();
	private LearningEdgeOdaDelegate delegate;

	private static Map<String, LearningEdgeOdaDelegate> loggedInProxies = Collections
		.synchronizedMap(new HashMap<String, LearningEdgeOdaDelegate>());
	private static HttpClient httpClient;
	private static XStream xstream;

	public void close() throws OdaException
	{
		List<Query> removeList = new ArrayList<Query>(openQueries);
		for( Query closeable : removeList )
		{
			closeable.close();
		}
	}

	public void queryClosed(Query query)
	{
		openQueries.remove(query);
	}

	public void commit() throws OdaException
	{
		// nothing
	}

	public int getMaxQueries() throws OdaException
	{
		return 0;
	}

	public IDataSetMetaData getMetaData(String arg0) throws OdaException
	{
		return new DataSetMetadata(this);
	}

	public boolean isOpen() throws OdaException
	{
		return delegate != null;
	}

	public IQuery newQuery(String datasetId) throws OdaException
	{
		Query query = new Query(this, datasetId, delegate);
		openQueries.add(query);
		return query;
	}

	public void open(Properties properties) throws OdaException
	{
		if( delegate == null && appContext != null )
		{
			delegate = (LearningEdgeOdaDelegate) appContext.get(com.tle.reporting.Constants.DELEGATE_APP_CONTEXT_KEY);
		}

		if( delegate == null && properties != null )
		{
			String username = properties.getProperty(Constants.WEBSERVICE_USER);
			String url = properties.getProperty(Constants.WEBSERVICE_URL);
			String key = username + url;
			delegate = loggedInProxies.get(key);

			if( delegate == null )
			{
				if( httpClient == null )
				{
					try
					{
						setupRemote();
					} 
					catch (Exception e)
					{
						throw new OdaException(e);
					}
				}
				delegate = new RemoteLearningEdgeOdaDelegate(httpClient, properties, xstream);
			}
			delegate.login(username, properties.getProperty(Constants.WEBSERVICE_PASSWORD));
			loggedInProxies.put(key, delegate);
		}

		if( delegate == null )
		{
			throw new OdaException("Both the application context, and the given properties, are null!"); //$NON-NLS-1$
		}

	}

	public void rollback() throws OdaException
	{
		// do nothing
	}

	@SuppressWarnings("unchecked")
	public void setAppContext(Object appContext) throws OdaException
	{
		if( appContext instanceof Map )
		{
			this.appContext = (Map<Object, Object>) appContext;
		}
	}

	@SuppressWarnings({"deprecation", "nls"})
	private static synchronized void setupRemote() throws NoSuchAlgorithmException, KeyManagementException
	{
		/*
		MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
		HttpConnectionManagerParams params = new HttpConnectionManagerParams();
		params.setDefaultMaxConnectionsPerHost(10);
		connectionManager.setParams(params);
		BlindSSLSocketFactory.register();
		final SSLSocketFactory defaultSSL = BlindSSLSocketFactory.getDefaultSSL();
		Protocol.registerProtocol("https", new Protocol("https", new SecureProtocolSocketFactory()
		{
			public Socket createSocket(Socket socket, String s, int i, boolean flag) throws IOException,
				UnknownHostException
			{
				return defaultSSL.createSocket(socket, s, i, flag);
			}

			public Socket createSocket(String s, int i) throws IOException, UnknownHostException
			{
				return defaultSSL.createSocket(s, i);
			}

			public Socket createSocket(String s, int i, InetAddress inetaddress, int j) throws IOException,
				UnknownHostException
			{
				return defaultSSL.createSocket(s, i, inetaddress, j);
			}

			public Socket createSocket(String arg0, int arg1, InetAddress arg2, int arg3, HttpConnectionParams arg4)
				throws IOException, UnknownHostException
			{
				return defaultSSL.createSocket(arg0, arg1, arg2, arg3);
			}
		}, 443));*/
		X509TrustManager trustManager =
        new X509TrustManager() {
          final X509Certificate[] acceptedIssuers = new X509Certificate[] {};

          @Override
          public X509Certificate[] getAcceptedIssuers() {
            return acceptedIssuers;
          }

          @Override
          public void checkServerTrusted(X509Certificate[] chain, String authType)
              throws CertificateException {
            // Nothing to do here
          }

          @Override
          public void checkClientTrusted(X509Certificate[] chain, String authType)
              throws CertificateException {
            // Nothing to do here
          }
        };
		SSLContext context = SSLContext.getInstance("TLS");
    	context.init(null, new TrustManager[] {trustManager}, new SecureRandom());
		SSLSocketFactory socketFactory =
        new SSLSocketFactory(context, SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
		PoolingClientConnectionManager conMan = new PoolingClientConnectionManager();
		conMan.getSchemeRegistry().register(new Scheme("https", 443, socketFactory));
		conMan.setMaxTotal(10000);
		conMan.setDefaultMaxPerRoute(1000);
		xstream = new XStream();
		httpClient = new DefaultHttpClient(conMan);
	}

	public LearningEdgeOdaDelegate getDelegate()
	{
		return delegate;
	}

	public void setLocale(ULocale arg0) throws OdaException
	{
		// whatevs
	}

}
