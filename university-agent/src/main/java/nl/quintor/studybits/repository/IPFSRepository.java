package nl.quintor.studybits.repository;

import io.ipfs.api.IPFS;
import io.ipfs.api.NamedStreamable;
import io.ipfs.multihash.Multihash;
import lombok.extern.slf4j.Slf4j;
import nl.quintor.studybits.entity.Document;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Repository;

import java.io.IOException;

import static org.apache.commons.lang3.SerializationUtils.deserialize;
import static org.apache.commons.lang3.SerializationUtils.serialize;

@Slf4j
@Repository("IPFS")
public class IPFSRepository {

    private IPFS ipfs;

    @Autowired
    public IPFSRepository(Environment env) {
        log.debug("IPFS Node: {}", env.getProperty("ipfs.node.url"));
        ipfs = new IPFS(env.getProperty("IPFS.node.host"), Integer.valueOf(env.getProperty("IPFS.node.port")));
    }

    public byte[] getFile(String key) {
        Multihash filePointer = Multihash.fromBase58(key);
        try {
            return ipfs.cat(filePointer);

        } catch (IOException e) {
            log.error(e.getMessage());
        }
        return new byte[]{};
    }

    public String storeFile(byte[] data) {
        NamedStreamable.ByteArrayWrapper file = new NamedStreamable.ByteArrayWrapper(data);
        try {
            return ipfs.add(file).get(0).hash.toBase58();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        return null;
    }
}
