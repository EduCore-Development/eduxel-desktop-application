package dev.educore.eduxel.service;

import dev.educore.eduxel.domain.school.ClassGroup;
import dev.educore.eduxel.domain.school.Student;
import dev.educore.eduxel.domain.school.Teacher;
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

    public long createClass(ClassGroup group) throws SQLException {
        long id = classRepo.createFull(group);
        group.setId(id);
        ActivityLogger.log("Schüler", "Klasse angelegt: " + group.getName(), null);
        return id;
    }

    public void updateClass(ClassGroup group) throws SQLException {
        classRepo.update(group);
        ActivityLogger.log("Schüler", "Klasse aktualisiert: " + group.getName(), null);
    }

    public void deleteClass(long classId) throws SQLException {
        classRepo.delete(classId);
        ActivityLogger.log("Schüler", "Klasse gelöscht: ID=" + classId, null);
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

    // Neue, Domain-basierte Methoden
    public long createStudent(Student student) throws SQLException {
        long id = studentRepo.createFull(student);
        student.setId(id);
        ActivityLogger.log("Schüler", "Neuer Schüler angelegt: " + student.getFirstName() + " " + student.getLastName(), null);
        return id;
    }

    public void updateStudent(Student student) throws SQLException {
        studentRepo.update(student);
        ActivityLogger.log("Schüler", "Schüler aktualisiert: " + student.getFirstName() + " " + student.getLastName(), null);
    }

    public Optional<Student> loadStudent(long id) throws SQLException {
        return Optional.ofNullable(studentRepo.findById(id));
    }

    public void deleteStudent(long id) throws SQLException {
        Optional<Student> existing = loadStudent(id);
        studentRepo.delete(id);
        existing.ifPresent(s -> ActivityLogger.log("Schüler", "Schüler gelöscht: " + s.getFirstName() + " " + s.getLastName(), null));
    }

    public void reassignStudentToClass(long studentId, Long classId) throws SQLException {
        if (classId == null) {
            // Klasse entfernen
            Student s = studentRepo.findById(studentId);
            if (s == null) return;
            s.setClassId(null);
            studentRepo.update(s);
        } else {
            studentRepo.assignToClass(studentId, classId);
        }
    }

    public long createTeacher(Teacher teacher) throws SQLException {
        long id = teacherRepo.createFull(teacher);
        teacher.setId(id);
        ActivityLogger.log("Schule", "Neuer Lehrer angelegt: " + teacher.getFirstName() + " " + teacher.getLastName(), null);
        return id;
    }

    public Optional<Teacher> loadTeacher(long id) throws SQLException {
        return teacherRepo.findById(id);
    }

    public void updateTeacher(Teacher teacher) throws SQLException {
        teacherRepo.update(teacher);
        ActivityLogger.log("Schule", "Lehrer aktualisiert: " + teacher.getFirstName() + " " + teacher.getLastName(), null);
    }

    public void deleteTeacher(long id) throws SQLException {
        Optional<Teacher> existing = loadTeacher(id);
        teacherRepo.delete(id);
        existing.ifPresent(t -> ActivityLogger.log("Schule", "Lehrer gelöscht: " + t.getFirstName() + " " + t.getLastName(), null));
    }

    // Neu: Klasse laden (für Detail-/Bearbeiten-Dialoge)
    public Optional<ClassGroup> loadClass(long id) throws SQLException {
        return Optional.ofNullable(classRepo.findById(id));
    }
}
