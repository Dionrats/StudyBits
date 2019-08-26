package nl.quintor.studybits.repository;

import io.ipfs.api.IPFS;
import io.ipfs.api.NamedStreamable;
import io.ipfs.multihash.Multihash;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.io.IOException;

@Slf4j
@Repository("IPFS")
public class IPFSRepository {

    @Autowired
    private IPFS ipfs;

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
