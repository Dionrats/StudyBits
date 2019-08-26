package nl.quintor.studybits.controller;

import nl.quintor.studybits.entity.Document;
import nl.quintor.studybits.entity.Student;
import nl.quintor.studybits.exceptions.DocumentNotProvidedException;
import nl.quintor.studybits.service.FileService;
import nl.quintor.studybits.service.StudentService;
import org.hyperledger.indy.sdk.IndyException;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@RunWith(SpringRunner.class)
@WebMvcTest(value = DocumentController.class, secure = false)
public class DocumentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    FileService fileService;

    @MockBean
    StudentService studentService;

    private static Random random;
    private static Student student;

    private MockMultipartFile file;
    private MockMultipartFile validation;

    @BeforeClass
    public static void setupClass() {
        student = new Student();
        student.setStudentId("1");
        student.setFirstName("Lisa");
        student.setLastName("Veren");
        student.setPassword("1234");
        student.setStudentId("12345678");
        student.setStudentDid("SFefG4dn823rdf3");

        random = new Random();
    }

    @Before
    public void setUp() throws Exception {
        Mockito.when(studentService.getStudentByStudentId(student.getStudentId())).thenReturn(student);
        Mockito.when(fileService.cacheFile(any(Document.class))).thenAnswer(i -> i.getArgument(0));
    }

    @Test
    public void uploadFileTest() throws Exception {

        byte[] given = new byte[100];
        random.nextBytes(given);
        file = new MockMultipartFile("file", "test.tst", MediaType.TEXT_PLAIN.getType(), given);

        mockMvc.perform(multipart("/documents/upload")
                    .file(file)
                    .param("name", "test")
                    .param("type", "tst")
                    .param("student", "12345678"))
                .andExpect(status().isOk())
                .andExpect(jsonPath(DocumentController.RESPONSE_CODE, is("202 ACCEPTED")));
    }

    @Test
    public void uploadLargeFileTest() throws Exception {

        byte[] given = new byte[(int)Math.pow(1024, 3)];
        random.nextBytes(given);
        file = new MockMultipartFile("file", "test.tst", MediaType.TEXT_PLAIN.getType(), given);

        mockMvc.perform(multipart("/documents/upload")
                    .file(file)
                    .param("name", "test")
                    .param("type", "tst")
                    .param("student", "12345678"))
                .andExpect(status().isOk())
                .andExpect(jsonPath(DocumentController.RESPONSE_CODE, is("202 ACCEPTED")));
    }

    @Test
    public void uploadEmptyFileTest() throws Exception {
        file = new MockMultipartFile("file", "test.tst", MediaType.TEXT_PLAIN.getType(), new byte[0]);

        mockMvc.perform(multipart("/documents/upload")
                    .file(file)
                    .param("name", "test")
                    .param("type", "tst")
                    .param("student", "12345678"))
                .andExpect(status().isOk())
                .andExpect(jsonPath(DocumentController.RESPONSE_CODE, is("202 ACCEPTED")));
    }

    @Test
    public void uploadNoFileTest() {
        assertThatThrownBy(() -> {
            mockMvc.perform(multipart("/documents/upload")
                    .param("student", "12345678"));
        }).hasCauseInstanceOf(DocumentNotProvidedException.class);
    }

    @Test
    public void uploadNoStudentTest() throws Exception {
        byte[] given = new byte[100];
        random.nextBytes(given);
        file = new MockMultipartFile("file", "test.tst", MediaType.TEXT_PLAIN.getType(), given);

        assertThatThrownBy(() -> {
            mockMvc.perform(multipart("/documents/upload")
                    .file(file)
                    .param("name", "test")
                    .param("type", "tst"));
        }).hasCauseInstanceOf(NullPointerException.class);
    }

    @Test
    public void uploadNoContentTest() {
        assertThatThrownBy(() -> mockMvc.perform(multipart("/documents/upload")))
                .hasCauseInstanceOf(DocumentNotProvidedException.class);
    }

    @Test
    public void verifyDocumentTest() throws Exception {
        byte[] given = new byte[(int)Math.pow(1024, 3)];
        random.nextBytes(given);
        file = new MockMultipartFile("file", "test.tst", MediaType.TEXT_PLAIN.getType(), given);
        validation = new MockMultipartFile("validation", "test.tst.sbv", MediaType.TEXT_PLAIN.getType(), given);

        Mockito.when(fileService.verifyDocument(any(byte[].class), any(byte[].class))).thenReturn(true);

        mockMvc.perform(multipart("/documents/verify")
                    .file(file)
                    .file(validation))
                .andExpect(status().isOk())
                .andExpect(jsonPath(DocumentController.RESPONSE_ACCEPTED, is(true)));
    }

    @Test
    public void verifyDocumentNotProvidedTest() throws Exception {
        byte[] given = new byte[(int)Math.pow(1024, 3)];
        random.nextBytes(given);
        validation = new MockMultipartFile("validation", "test.tst.sbv", MediaType.TEXT_PLAIN.getType(), given);

        assertThatThrownBy(() -> mockMvc.perform(multipart("/documents/verify").file(validation)))
            .hasCauseInstanceOf(DocumentNotProvidedException.class);
    }


    @Test
    public void verifyDocumenValidationNotProvidedTest() {
        assertThatThrownBy(() -> mockMvc.perform(multipart("/documents/verify")))
                .hasCauseInstanceOf(DocumentNotProvidedException.class);
    }

    @Test
    public void verifyDocumenValidationNotingProvidedTest() {
        byte[] given = new byte[(int)Math.pow(1024, 3)];
        random.nextBytes(given);
        file = new MockMultipartFile("file", "test.tst", MediaType.TEXT_PLAIN.getType(), given);

        assertThatThrownBy(() -> mockMvc.perform(multipart("/documents/verify").file(file)))
                .hasCauseInstanceOf(DocumentNotProvidedException.class);
    }

    @Test
    public void verifyDocumentInvalidValidationTest() throws Exception {
        byte[] given = new byte[(int)Math.pow(1024, 3)];
        random.nextBytes(given);
        file = new MockMultipartFile("file", "test.tst", MediaType.TEXT_PLAIN.getType(), given);
        validation = new MockMultipartFile("validation", "test.docx", MediaType.TEXT_PLAIN.getType(), given);

        Mockito.when(fileService.verifyDocument(any(byte[].class), any(byte[].class))).thenReturn(false);

        mockMvc.perform(multipart("/documents/verify")
                .file(file)
                .file(validation))
                .andExpect(status().isOk())
                .andExpect(jsonPath(DocumentController.RESPONSE_ACCEPTED, is(false)));
    }

    @Test
    public void verifyEmptyDocumentTest() throws Exception {
        byte[] given = new byte[0];
        random.nextBytes(given);
        file = new MockMultipartFile("file", "test.tst", MediaType.TEXT_PLAIN.getType(), given);
        validation = new MockMultipartFile("validation", "test.tst.sbv", MediaType.TEXT_PLAIN.getType(), given);

        Mockito.when(fileService.verifyDocument(any(byte[].class), any(byte[].class))).thenReturn(false);

        mockMvc.perform(multipart("/documents/verify")
                .file(file)
                .file(validation))
                .andExpect(status().isOk())
                .andExpect(jsonPath(DocumentController.RESPONSE_ACCEPTED, is(true)))
                .andExpect(jsonPath(DocumentController.RESPONSE, is(false)));
    }

}
