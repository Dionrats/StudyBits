package nl.quintor.studybits.repository;

import io.ipfs.api.IPFS;
import io.ipfs.api.MerkleNode;
import io.ipfs.multihash.Multihash;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;

@RunWith(SpringRunner.class)
public class IPFSRepositoryTest {

    @TestConfiguration
    static class IPFSRepositoryTestConfiguration {
        @Bean
        public IPFSRepository ipfsRepository() {
            return new IPFSRepository();
        }
    }

    @Autowired
    private IPFSRepository sut;

    @MockBean
    IPFS ipfs;

    private byte[] given;
    private Multihash hash;

    @Before
    public void setUp() {
        given = new byte[20];
        new Random().nextBytes(given);

        hash = Multihash.fromBase58("QmTpsrdqnc138oiQov9zTGewEcDcwf2XJtZbR4U1pNFPwo");
    }

    @Test
    public void getFileTest() throws IOException {
        Mockito.when(ipfs.cat(hash)).thenReturn(given);
        byte[] result = sut.getFile(hash.toString());

        assertEquals(given, result);
    }

    @Test
    public void getFileFailTest() throws IOException {
        Mockito.when(ipfs.cat(hash)).thenThrow(new IOException());
        byte[] result = sut.getFile(hash.toString());

        assertThat(result).isEmpty();
    }

    @Test
    public void storeFileTest() throws IOException {
        List<MerkleNode> nodes = new ArrayList<>();
        nodes.add(new MerkleNode(hash.toString()));
        Mockito.when(ipfs.add(any())).thenReturn(nodes);

        String result = sut.storeFile(given);

        assertThat(result).isEqualTo(hash.toBase58());
    }

    @Test
    public void storeFileFailTest() throws IOException {
        Mockito.when(ipfs.add(any())).thenThrow(new IOException());

        String result = sut.storeFile(given);

        assertThat(result).isNull();
    }

}
