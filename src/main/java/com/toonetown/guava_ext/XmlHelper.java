package com.toonetown.guava_ext;

import java.io.IOException;
import java.io.InputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * A helper class for parsing responses
 */
public class XmlHelper {
    /**
     * A thread-local instance of a DocumentBuilder
     */
    private static final ThreadLocal<DocumentBuilder> DOCUMENT_BUILDER = new ThreadLocal<DocumentBuilder>() {
        @Override protected DocumentBuilder initialValue() {
            try {
                return DocumentBuilderFactory.newInstance().newDocumentBuilder();
            } catch (ParserConfigurationException e) {
                throw new AssertionError("XML Document parsing should always exist", e);
            }
        }
    };

    /**
     * Returns a parsed Document based off our thread local
     */
    public static Document getParsedDocument(final InputStream stream) throws IOException, SAXException {
        DOCUMENT_BUILDER.get().reset();
        return DOCUMENT_BUILDER.get().parse(stream);
    }

    /**
     * Returns the inner value of the named node in the given document.  The node must only exist once
     *
     * @param doc the XML document
     * @param nodeName the name of the node
     * @return the value of the named node
     */
    public static String getNodeInnerValue(final Document doc, final String nodeName) throws SAXException {
        final NodeList nodes = doc.getElementsByTagName(nodeName);
        if (nodes.getLength() != 1) {
            throw new SAXException("Could not get single node for " + nodeName);
        }
        final NodeList childNodes = nodes.item(0).getChildNodes();
        if (childNodes.getLength() != 1) {
            throw new SAXException("Could not get single child node for " + nodeName);
        }
        return childNodes.item(0).getNodeValue();
    }
}
