package dev.educore.eduxel.service;

import dev.educore.eduxel.persistence.school.ClassGroupRepository;
import dev.educore.eduxel.persistence.school.StudentRepository;
import dev.educore.eduxel.persistence.school.TeacherRepository;

import java.sql.SQLException;
import java.util.Optional;

public class SchoolService {

    private final ClassGroupRepository classRepo = new ClassGroupRepository();
    private final StudentRepository studentRepo = new StudentRepository();
    private final TeacherRepository teacherRepo = new TeacherRepository();

    public Long ensureClassByName(String name, Integer grade) throws SQLException {
        Optional<Long> id = classRepo.findIdByName(name);
        if (id.isPresent()) return id.get();
        long newId = classRepo.create(name, grade);
        ActivityLogger.log("Schüler", "Klasse angelegt: " + name, null);
        return newId;
    }

    public long createClass(String name, Integer grade) throws SQLException {
        long id = classRepo.create(name, grade);
        ActivityLogger.log("Schüler", "Klasse angelegt: " + name, null);
        return id;
    }

    public long createStudent(String firstName, String lastName, Long classId) throws SQLException {
        long id = studentRepo.create(firstName, lastName, classId);
        ActivityLogger.log("Schüler", "Neuer Schüler angelegt: " + firstName + " " + lastName, null);
        return id;
    }

    public long createTeacher(String firstName, String lastName, String subject) throws SQLException {
        long id = teacherRepo.create(firstName, lastName, subject);
        ActivityLogger.log("Schüler", "Neuer Lehrer angelegt: " + firstName + " " + lastName, null);
        return id;
    }
}
