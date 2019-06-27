package nl.quintor.studybits.service;

import nl.quintor.studybits.entity.Student;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class StudentUserDetailService implements UserDetailsService {
    @Autowired
    public StudentService studentService;

    public UserDetails loadUserByUsername(String studentId) {

        Student student = studentService.getStudentByStudentId(studentId);

        // If there is no studentID provided in the login message, create one for future purposes.
        if(studentId == null || studentId.isEmpty()) {
            student = studentService.createStudent(UUID.randomUUID().toString(), "", null);
        } else {
            if(student.hasDid()) {
                throw new AccessDeniedException("Access denied for student: " + studentId + ". DID already exists.");
            }
        }

        return User.withUsername(student.getStudentId()).password(student.getPassword()).roles("USER").build();
    }
}
