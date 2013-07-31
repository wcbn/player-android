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
package net.moraleboost.streamscraper.parser;

import java.net.URI;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.util.LinkedList;
import java.util.List;

import net.moraleboost.streamscraper.ParseException;
import net.moraleboost.streamscraper.Parser;
import net.moraleboost.streamscraper.Stream;
import net.moraleboost.streamscraper.util.CharsetUtils;
import net.moraleboost.streamscraper.util.JerichoHtmlUtils;

public class ShoutCastStatusPageParser implements Parser
{
    public static final String DEFAULT_CHARSET = "Shift_JIS";
    
    private String charset;
    
    public ShoutCastStatusPageParser()
    {
        this.charset = DEFAULT_CHARSET;
    }
    
    public void setCharset(String charset)
    {
        this.charset = charset;
    }

    public String getCharset()
    {
        return charset;
    }

    public List<Stream> parse(URI uri, byte[] src) throws ParseException
    {
        try {
            CharsetDecoder dec = CharsetUtils.createDecoder(
                    charset, CodingErrorAction.IGNORE, CodingErrorAction.IGNORE);
            String usrc = CharsetUtils.decode(dec, src);
            
            return parseSource(uri, usrc);
        } catch (Exception e) {
            throw new ParseException(e);
        }
    }
    
    private List<Stream> parseSource(URI uri, String src)
    {
        Stream stream = new Stream();
        
        stream.setUri(uri.resolve("/"));

        List<org.jsoup.nodes.Element> tables =
            JerichoHtmlUtils.findAllElements(src.toString(), "table", "align", "center");
        
        for (org.jsoup.nodes.Element table: tables) {
            if (parseTable2(uri, table, stream)) {
                List<Stream> ret = new LinkedList<Stream>();
                ret.add(stream);
                return ret;
            }
            stream.clear();
        }
        
        return new LinkedList<Stream>();
    }
    
    private boolean parseTable2(URI uri, org.jsoup.nodes.Element table, Stream stream)
    {
        boolean ret = false;
        
        List<org.jsoup.nodes.Element> rows =
            JerichoHtmlUtils.findAllChildElement(table, "tr");

        for (org.jsoup.nodes.Element row: rows) {
            List<org.jsoup.nodes.Element> cols =
                JerichoHtmlUtils.findAllChildElement(row, "td");
            if (cols.size() != 2) {
                continue;
            }
            
            String name = cols.get(0).text();
            String value = cols.get(1).text();
            
            ret = parseAttribute(name, value, stream) || ret;
        }
        
        return ret;
    }
    
    private boolean parseAttribute(String name, String value, Stream stream)
    {
        if (name.equalsIgnoreCase("Listener Peak:")) {
            try {
                stream.setPeakListenerCount(Integer.parseInt(value));
            } catch (NumberFormatException e) {}
            return true;
        } else if (name.equalsIgnoreCase("Stream Title:")) {
            stream.setTitle(value);
            return true;
        } else if (name.equalsIgnoreCase("Content Type:")) {
            stream.setContentType(value);
            return true;
        } else if (name.equalsIgnoreCase("Stream Genre:")) {
            stream.setGenre(value);
            return true;
        } else if (name.equalsIgnoreCase("Current Song:")) {
            stream.setCurrentSong(value);
            return true;
        }
        
        return false;
    }
}
