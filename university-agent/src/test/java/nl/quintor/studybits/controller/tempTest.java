package nl.quintor.studybits.controller;

import nl.quintor.studybits.entity.Student;
import nl.quintor.studybits.repository.StudentRepository;
import nl.quintor.studybits.service.StudentService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
public class tempTest {

    @Autowired
    private StudentService studentService;

    @Test
    public void dataTest() {
        Student student = studentService.getStudentByStudentDid("BZmLfqwMXCB3eV3oTvDteV");
    }
}
