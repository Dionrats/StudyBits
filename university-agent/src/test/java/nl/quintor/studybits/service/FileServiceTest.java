package nl.quintor.studybits.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.ipfs.multihash.Multihash;
import nl.quintor.studybits.entity.Document;
import nl.quintor.studybits.entity.Student;
import nl.quintor.studybits.entity.Transcript;
import nl.quintor.studybits.indy.wrapper.Verifier;
import nl.quintor.studybits.indy.wrapper.dto.EncryptedMessage;
import nl.quintor.studybits.indy.wrapper.dto.Proof;
import nl.quintor.studybits.indy.wrapper.dto.ProofRequest;
import nl.quintor.studybits.indy.wrapper.util.JSONUtil;
import nl.quintor.studybits.repository.FileRepository;
import nl.quintor.studybits.repository.IPFSRepository;
import org.hyperledger.indy.sdk.IndyException;
import org.hyperledger.indy.sdk.crypto.Crypto;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(SpringRunner.class)
@PrepareForTest({Crypto.class, FileService.class})
public class FileServiceTest {

    @TestConfiguration
    static class FileServiceTestConfiguration {
        @Bean
        public FileService fileService() {
            return new FileService();
        }
    }

    @Autowired
    FileService sut;

    @MockBean
    IPFSRepository ipfsRepository;
    @MockBean
    FileRepository fileRepository;
    @MockBean
    StudentService studentService;
    @MockBean
    Verifier verifier;

    private Document document;
    private Student student;

    @Before
    public void setUp() {
        student = new Student();
        student.setStudentId("1");
        student.setFirstName("Lisa");
        student.setLastName("Veren");
        student.setPassword("1234");
        student.setStudentDid("a1b2c3d4e5f6");
        student.setTranscript(new Transcript("Bachelor of Arts, Marketing", "enrolled", "8", false));

        List<Document> documents = new ArrayList<>();
        document = new Document(0, "test", "txt", new byte[1], "1234", student);
        documents.add(document);
        documents.add(new Document(1, "test2", "txt", new byte[1], "1234", student));

        student.setDocuments(documents);
    }

    @Test
    public void removeExistingFileFromCacheTest() {
        Mockito.doNothing().when(studentService).updateStudent(student);

        assertThat(student.getDocuments()).contains(document);

        sut.removeFileFromCache(document);

        assertThat(student.getDocuments()).doesNotContain(document);
    }

    @Test
    public void removeNonExistingFileFromCacheTest() {
        Mockito.doNothing().when(studentService).updateStudent(student);

        student.getDocuments().remove(document);
        assertThat(student.getDocuments()).doesNotContain(document);


        sut.removeFileFromCache(document);
    }


    @Test
    public void storeFileTest() {
        Mockito.when(ipfsRepository.storeFile(any(byte[].class))).thenReturn("result");
        Mockito.doNothing().when(studentService).updateStudent(any(Student.class));

        String result = sut.storeFile(document);

        assertThat(result).isEqualTo("result");
    }

    @Test
    public void cacheFileTest() throws IndyException, ExecutionException, InterruptedException {
        Mockito.when(fileRepository.saveAndFlush(document)).thenReturn(document);
        EncryptedMessage encryptedMessage = new EncryptedMessage(document.getData(), "");
        CompletableFuture<EncryptedMessage> future = CompletableFuture.completedFuture(encryptedMessage);
        Mockito.when(verifier.authEncrypt(any(byte[].class), anyString())).thenReturn(future);

        Document result = sut.cacheFile(document);

        assertThat(result.getData()).containsExactly(encryptedMessage.getMessage());
    }

    @Test
    public void updateDocumentTest() {
        Mockito.when(fileRepository.saveAndFlush(any(Document.class))).thenReturn(document);

        sut.updateDocument(document);

        verify(fileRepository, times(1)).saveAndFlush(document);
    }

    @Test
    public void getDocumentFromCacheTest() {
        Mockito.when(fileRepository.getDocumentByNonce(document.getNonce())).thenReturn(document);

        Document result = sut.getDocumentFromCache(document.getNonce());

        assertThat(result).isEqualTo(document);
    }

    @Test
    public void getDocumentsFromCacheTest() {
        Mockito.when(fileRepository.getDocumentsByStudent_Id(student.getId())).thenReturn(student.getDocuments());

        List<Document> result = sut.getDocumentsFromCache(student.getId());

        assertThat(result).containsAll(student.getDocuments());
    }

    @Test
    public void parsePrimativeByteArray() {
        JSONArray jsonArray = new JSONArray();
        byte[] expected = new byte[10];
        new Random().nextBytes(expected);

        for (byte anExpected : expected) {
            jsonArray.put((int) anExpected);
        }

        byte[] result = sut.parsePrimativeByteArray(jsonArray);

        assertThat(result).containsExactly(expected);
    }

    @Test
    public void parseEmptyPrimativeByteArray() {
        JSONArray jsonArray = new JSONArray();
        byte[] expected = new byte[0];
        new Random().nextBytes(expected);
        byte[] result = sut.parsePrimativeByteArray(jsonArray);

        assertThat(result).containsExactly(expected);
    }

    @Test
    public void verifyDocumentTest() throws Exception {
        byte[] file = new byte[10];
        new Random().nextBytes(file);

        JSONObject validation = new JSONObject();
        validation.put("p", "{}");
        validation.put("r", "{}");
        validation.put("h", "hash");
        validation.put("k", "key");
        validation.put("s", new JSONArray());

        mockStatic(Crypto.class);
        Multihash mockHash = mock(Multihash.class);
        ObjectMapper mockMapper = mock(JSONUtil.mapper.getClass());

        ProofRequest pr = new ProofRequest();
        Proof p = new Proof();

        PowerMockito.when(Crypto.cryptoVerify(anyString(), any(byte[].class), any(byte[].class))).thenReturn(CompletableFuture.completedFuture(true));
        PowerMockito.whenNew(Multihash.class).withAnyArguments().thenReturn(mockHash);
        PowerMockito.when(mockHash.toBase58()).thenReturn(validation.getString("h"));
        when(mockMapper.readValue(validation.getString("r"), ProofRequest.class)).thenReturn(pr);
        when(mockMapper.readValue(validation.getString("p"), Proof.class)).thenReturn(p);
        when(verifier.validateProof(pr, p)).thenReturn(CompletableFuture.completedFuture(true));

        boolean result = sut.verifyDocument(file, validation.toString().getBytes());

        assertThat(result).isTrue();
    }

    @Test
    public void verifyDocumentTamperedValidationTest() throws Exception {
        byte[] file = new byte[10];
        new Random().nextBytes(file);

        JSONObject validation = new JSONObject();
        validation.put("p", "");
        validation.put("r", "");
        validation.put("h", "");
        validation.put("k", "");
        validation.put("s", new JSONArray());

        mockStatic(Crypto.class);

        PowerMockito.when(Crypto.cryptoVerify(anyString(), any(byte[].class), any(byte[].class))).thenReturn(CompletableFuture.completedFuture(false));

        boolean result = sut.verifyDocument(file, validation.toString().getBytes());

        assertThat(result).isFalse();
    }

    @Test
    public void verifyDocumentTamperedFileTest() throws Exception {
        byte[] file = new byte[10];
        new Random().nextBytes(file);

        JSONObject validation = new JSONObject();
        validation.put("p", "");
        validation.put("r", "");
        validation.put("h", "");
        validation.put("k", "");
        validation.put("s", new JSONArray());

        mockStatic(Crypto.class);
        Multihash mockHash = mock(Multihash.class);

        PowerMockito.when(Crypto.cryptoVerify(anyString(), any(byte[].class), any(byte[].class))).thenReturn(CompletableFuture.completedFuture(true));
        PowerMockito.whenNew(Multihash.class).withAnyArguments().thenReturn(mockHash);
        PowerMockito.when(mockHash.toBase58()).thenReturn(validation.getString("h") + "extra stuff");

        boolean result = sut.verifyDocument(file, validation.toString().getBytes());

        assertThat(result).isFalse();
    }





}
