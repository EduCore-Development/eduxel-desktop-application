package dev.educore.eduxel.service;

import dev.educore.eduxel.domain.school.ClassGroup;
import dev.educore.eduxel.domain.school.Student;
import dev.educore.eduxel.domain.school.Teacher;
import dev.educore.eduxel.persistence.school.ClassGroupRepository;
import dev.educore.eduxel.persistence.school.StudentRepository;
import dev.educore.eduxel.persistence.school.TeacherRepository;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Service für globale Such- und Filter-Funktionalität
 */
public class SearchService {

    private final StudentRepository studentRepo = new StudentRepository();
    private final TeacherRepository teacherRepo = new TeacherRepository();
    private final ClassGroupRepository classRepo = new ClassGroupRepository();

    private static final int DEFAULT_LIMIT = 100;

    /**
     * Sucht Schüler asynchron
     */
    public CompletableFuture<List<Student>> searchStudentsAsync(String searchTerm, Long classIdFilter) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return studentRepo.searchStudents(searchTerm, classIdFilter, DEFAULT_LIMIT);
            } catch (SQLException e) {
                e.printStackTrace();
                return new ArrayList<>();
            }
        });
    }

    /**
     * Sucht Schüler synchron
     */
    public List<Student> searchStudents(String searchTerm, Long classIdFilter) throws SQLException {
        return studentRepo.searchStudents(searchTerm, classIdFilter, DEFAULT_LIMIT);
    }

    /**
     * Sucht Lehrer asynchron
     */
    public CompletableFuture<List<Teacher>> searchTeachersAsync(String searchTerm, String subjectFilter) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return teacherRepo.searchTeachers(searchTerm, subjectFilter, DEFAULT_LIMIT);
            } catch (SQLException e) {
                e.printStackTrace();
                return new ArrayList<>();
            }
        });
    }

    /**
     * Sucht Lehrer synchron
     */
    public List<Teacher> searchTeachers(String searchTerm, String subjectFilter) throws SQLException {
        return teacherRepo.searchTeachers(searchTerm, subjectFilter, DEFAULT_LIMIT);
    }

    /**
     * Sucht Klassen asynchron
     */
    public CompletableFuture<List<ClassGroup>> searchClassesAsync(String searchTerm, Integer gradeFilter) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return classRepo.searchClasses(searchTerm, gradeFilter, DEFAULT_LIMIT);
            } catch (SQLException e) {
                e.printStackTrace();
                return new ArrayList<>();
            }
        });
    }

    /**
     * Sucht Klassen synchron
     */
    public List<ClassGroup> searchClasses(String searchTerm, Integer gradeFilter) throws SQLException {
        return classRepo.searchClasses(searchTerm, gradeFilter, DEFAULT_LIMIT);
    }

    /**
     * Globale Suche über alle Kategorien
     */
    public CompletableFuture<SearchResults> searchAllAsync(String searchTerm) {
        CompletableFuture<List<Student>> studentsFuture = searchStudentsAsync(searchTerm, null);
        CompletableFuture<List<Teacher>> teachersFuture = searchTeachersAsync(searchTerm, null);
        CompletableFuture<List<ClassGroup>> classesFuture = searchClassesAsync(searchTerm, null);

        return CompletableFuture.allOf(studentsFuture, teachersFuture, classesFuture)
                .thenApply(v -> {
                    try {
                        return new SearchResults(
                                studentsFuture.get(),
                                teachersFuture.get(),
                                classesFuture.get()
                        );
                    } catch (Exception e) {
                        e.printStackTrace();
                        return new SearchResults(new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
                    }
                });
    }

    /**
     * Container für Suchergebnisse über alle Kategorien
     */
    public static class SearchResults {
        private final List<Student> students;
        private final List<Teacher> teachers;
        private final List<ClassGroup> classes;

        public SearchResults(List<Student> students, List<Teacher> teachers, List<ClassGroup> classes) {
            this.students = students;
            this.teachers = teachers;
            this.classes = classes;
        }

        public List<Student> getStudents() {
            return students;
        }

        public List<Teacher> getTeachers() {
            return teachers;
        }

        public List<ClassGroup> getClasses() {
            return classes;
        }

        public int getTotalCount() {
            return students.size() + teachers.size() + classes.size();
        }
    }
}

