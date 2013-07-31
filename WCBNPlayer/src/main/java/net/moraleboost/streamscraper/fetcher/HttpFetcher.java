/*
 **
 **  Jul. 20, 2009
 **
 **  The author disclaims copyright to this source code.
 **  In place of a legal notice, here is a blessing:
 **
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 **
 **                                         Stolen from SQLite :-)
 **  Any feedback is welcome.
 **  Kohei TAKETA <k-tak@void.in>
 **
 */
package net.moraleboost.streamscraper.fetcher;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.Proxy;
import java.net.URI;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import net.moraleboost.streamscraper.FetchException;
import net.moraleboost.streamscraper.Fetcher;

public class HttpFetcher implements Fetcher
{
    private static final String DEFAULT_USER_AGENT =
        "Mozilla/5.0 (compatible; StreamScraper/1.0; +http://code.google.com/p/streamscraper/)";
    
    private static final String REQUEST_METHOD = "GET";
    
    public HttpFetcher()
    {
    }

    public byte[] fetch(URI uri) throws FetchException
    {
    	return fetch(uri, null);
    }
    
    public byte[] fetch(URI uri, URI proxyUri) throws FetchException
    {
    	byte [] entity = null;
    	
    	HttpURLConnection conn = null;
    	BufferedReader reader = null;
    	StringBuffer body = new StringBuffer();
    	String line;

    	try {
    		URL url = uri.toURL();
    		
    		Proxy proxy = null;
    		if (proxyUri != null) {
    			URL proxyUrl = proxyUri.toURL();
        		proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyUrl.getHost(), proxyUrl.getPort()));
    		}
    		
    		if (url.getProtocol().equalsIgnoreCase("http")) {
    			if (proxy == null) {
    				conn = (HttpURLConnection) url.openConnection();
    			} else {
    				conn = (HttpURLConnection) url.openConnection(proxy);
    			}
    		} else if (url.getProtocol().equalsIgnoreCase("https")) {
    			if (proxy == null) {
    				conn = (HttpsURLConnection) url.openConnection();
    			} else {
    				conn = (HttpsURLConnection) url.openConnection(proxy);
    			}     		
    		}

    		conn.setRequestProperty("User-Agent", DEFAULT_USER_AGENT);
    		conn.setConnectTimeout(10000);
    		conn.setReadTimeout(10000);
    		conn.setRequestMethod(REQUEST_METHOD);

    		if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
    			throw new FetchException("Status code != 200");
            }
    		
    		reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
    		conn.connect();

    		while ((line = reader.readLine()) != null) {
    			body = body.append(line);
    		}
    		
    		entity = body.toString().getBytes();
    	} catch (MalformedURLException e) {
            throw new FetchException(e);
    	} catch (ProtocolException e) {
            throw new FetchException(e);
		} catch (IOException e) {
            throw new FetchException(e);
		} finally {
			closeBufferedReader(reader);
			closeURLConnection(conn);
		}
    	
    	return entity;
    }
    
    private void closeBufferedReader(BufferedReader reader) 
    {
    	if (reader != null) {
    		try {
				reader.close();
			} catch (IOException e) {
			}
    	}
    }

    private void closeURLConnection(HttpURLConnection conn)
    {
    	if (conn != null) {
			conn.disconnect();
    	}
    }
    
    public static void main(String[] args) throws Exception
    {
        HttpFetcher fetcher = new HttpFetcher();
        byte[] data = fetcher.fetch(new URI("http://code.google.com/p/streamscraper/"));
        System.out.println(new String(data, "UTF-8"));
    }
}
