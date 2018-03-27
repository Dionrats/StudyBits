package nl.quintor.studybits.university.services;

import lombok.AllArgsConstructor;
import nl.quintor.studybits.university.entities.StudentUser;
import nl.quintor.studybits.university.entities.University;
import nl.quintor.studybits.university.entities.User;
import nl.quintor.studybits.university.models.UserModel;
import nl.quintor.studybits.university.repositories.UniversityRepository;
import nl.quintor.studybits.university.repositories.UserRepository;
import org.dozer.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class StudentService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UniversityRepository universityRepository;

    @Autowired
    private Mapper mapper;

    private UserModel toModel(Object user) {
        return mapper.map(user, UserModel.class);
    }

    private User toEntity(Object userModel) {
        return mapper.map(userModel, User.class);
    }

    public List<UserModel> findAllStudents() {
        return userRepository
                .findAllByStudentUserIsNotNull()
                .stream()
                .map(this::toModel)
                .collect(Collectors.toList());
    }

    public List<UserModel> findAllForUniversity(String universityName) {
        return userRepository
                .findAllByStudentUserIsNotNullAndUniversityNameIgnoreCase(universityName)
                .stream()
                .map(this::toModel)
                .collect(Collectors.toList());
    }


    public Optional<UserModel> findById(Long id) {
        return userRepository
                .findById(id)
                .map(this::toModel);
    }

    public Optional<UserModel> findByUniversityAndUserName(String universityName, String userName) {
        return userRepository
                .findAllByStudentUserIsNotNullAndUniversityNameIgnoreCaseAndUserNameIgnoreCase(universityName, userName)
                .map(this::toModel);
    }


    public UserModel createStudent(String universityName, UserModel userModel) {
        User user = toEntity(userModel);
        University university = universityRepository.findByNameIgnoreCase(universityName)
                .orElseThrow(() -> new IllegalArgumentException("Unknown university."));
        user.setUniversity(university);
        user.setClaims(new ArrayList<>());
        StudentUser studentUser = new StudentUser(null, user, new HashSet<>(), new ArrayList<>());
        user.setStudentUser(studentUser);
        return toModel(userRepository.save(user));
    }

}