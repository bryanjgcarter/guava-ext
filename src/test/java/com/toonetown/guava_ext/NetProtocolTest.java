package com.toonetown.guava_ext;

import org.testng.annotations.Test;
import static org.testng.Assert.*;

import com.toonetown.guava_ext.NetProtocol;
import com.toonetown.guava_ext.NotFoundException;

/**
 * Unit test for NetProtocol
 */
public class NetProtocolTest {
    
    @Test
    public void testFindByPort() throws NotFoundException {
        assertEquals(NetProtocol.find(21), NetProtocol.FTP);
    }
    
    @Test(expectedExceptions = NotFoundException.class)
    public void testFindByPort_notFound() throws NotFoundException {
        NetProtocol.find(0);
    }

    @Test
    public void testFindByScheme() throws NotFoundException {
        assertEquals(NetProtocol.find("imap"), NetProtocol.IMAP);
        assertEquals(NetProtocol.find("SMTP"), NetProtocol.SMTP);
    }

    @Test(expectedExceptions = NotFoundException.class)
    public void testFindByScheme_notFound() throws NotFoundException {
        NetProtocol.find("file");
    }

    @Test
    public void testFindByPortOrDefault() {
        assertEquals(NetProtocol.find(21, NetProtocol.HTTP), NetProtocol.FTP);
    }
    
    @Test
    public void testFindByPortOrDefault_notFound() {
        assertEquals(NetProtocol.find(0, NetProtocol.HTTP), NetProtocol.HTTP);
    }
    
    @Test
    public void testFindBySchemeOrDefault() {
        assertEquals(NetProtocol.find("imap", NetProtocol.HTTP), NetProtocol.IMAP);
        assertEquals(NetProtocol.find("SMTP", NetProtocol.HTTP), NetProtocol.SMTP);
    }
    
    @Test
    public void testFindBySchemeOrDefault_notFound() {
        assertEquals(NetProtocol.find("file", NetProtocol.HTTP), NetProtocol.HTTP);
    }

}
