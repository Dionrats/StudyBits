package nl.quintor.studybits.repository;

import nl.quintor.studybits.entity.Document;
import nl.quintor.studybits.entity.Student;
import nl.quintor.studybits.entity.Transcript;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@DataJpaTest
public class FileRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private FileRepository fileRepository;

    private Student student;
    private Document document;

    @Before
    public void setUp() {
        student = new Student();
        student.setStudentId("1");
        student.setFirstName("Lisa");
        student.setLastName("Veren");
        student.setPassword("1234");
        student.setStudentDid(null);
        student.setTranscript(new Transcript("Bachelor of Arts, Marketing", "enrolled", "8", false));
        entityManager.persist(student);

        document = new Document(0, "test", "txt", new byte[1], "1234", student);
        entityManager.persist(document);
        entityManager.flush();
    }

    @Test
    public void findDocumentByNonceTest() {
        Document found = fileRepository.getDocumentByNonce(document.getNonce());

        assertThat(found.getNonce())
                .isEqualTo(document.getNonce());
    }

    @Test
    public void findDocumentByStudent() {
        List<Document> found = fileRepository.getDocumentsByStudent_Id(student.getId());

        for(Document doc : found) {
            assertThat(doc.getStudent().getId())
                    .isEqualTo(student.getId());
        }
    }


}
