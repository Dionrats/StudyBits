package nl.quintor.studybits.service;

import io.ipfs.multihash.Multihash;
import lombok.extern.slf4j.Slf4j;
import nl.quintor.studybits.entity.Document;
import nl.quintor.studybits.entity.Student;
import nl.quintor.studybits.indy.wrapper.Verifier;
import nl.quintor.studybits.indy.wrapper.dto.*;
import nl.quintor.studybits.indy.wrapper.util.JSONUtil;
import nl.quintor.studybits.repository.FileRepository;
import nl.quintor.studybits.repository.IPFSRepository;
import org.apache.commons.codec.digest.DigestUtils;
import org.hyperledger.indy.sdk.IndyException;
import org.hyperledger.indy.sdk.crypto.Crypto;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;

@Slf4j
@Service
public class FileService {

    @Autowired
    private IPFSRepository ipfsRepository;
    @Autowired
    private FileRepository fileRepository;
    @Autowired
    private StudentService studentService;
    @Autowired
    private Verifier verifier;


    public String storeFile(Document document) {
        removeFileFromCache(document);

        return ipfsRepository.storeFile(document.getData());
    }

    public Document cacheFile(Document document) throws IndyException, ExecutionException, InterruptedException {
        JSONObject data = new JSONObject();
        data.put("data", document.getData());
        document.setData(verifier.authEncrypt(data.toString().getBytes(), document.getStudent().getStudentDid()).get().getMessage());

        return fileRepository.saveAndFlush(document);
    }

    public void removeFileFromCache(Document document) {
        Student student = document.getStudent();
        student.getDocuments().remove(document);
        studentService.updateStudent(student);
    }

    public List<Document> getDocumentsFromCache(long studentId) {
        return fileRepository.getDocumentsByStudent_Id(studentId);
    }

    public Document getDocumentFromCache(String nonce) {
        return fileRepository.getDocumentByNonce(nonce);
    }

    public void updateDocument(Document document) {
        fileRepository.saveAndFlush(document);
    }

    public boolean verifyDocument(byte[] file, byte[] validation) throws IOException, IndyException, ExecutionException, InterruptedException {

        //prepare validation jsonR
        JSONObject jsonObject = new JSONObject(new String(validation));
        JSONArray s = jsonObject.getJSONArray("s");
        jsonObject.remove("s");

        //check against ledger if validation has not been tampered with
        if (!Crypto.cryptoVerify(jsonObject.getString("k"), jsonObject.toString().getBytes(), parsePrimativeByteArray(s)).get())
            return false;

        //check against validation if data has not been tampered with
        if (!jsonObject.getString("h").equals(new Multihash(Multihash.Type.sha2_256, DigestUtils.sha256(file)).toBase58()))
            return false;

        //check against ledger if data and validation belong to valid credential
        ProofRequest proofRequest = JSONUtil.mapper.readValue(jsonObject.getString("r"), ProofRequest.class);
        Proof proof = JSONUtil.mapper.readValue(jsonObject.getString("p"), Proof.class);

        return verifier.validateProof(proofRequest, proof).get();
    }

    protected byte[] parsePrimativeByteArray(JSONArray in) {
        byte[] out = new byte[in.length()];
        for (int i = 0; i < out.length; i++) {
            out[i] = Byte.parseByte(String.valueOf(in.getInt(i)));
        }
        return out;
    }
}