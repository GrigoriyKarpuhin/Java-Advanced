package info.kgeorgiy.ja.karpukhin.student;

import info.kgeorgiy.java.advanced.student.GroupName;
import info.kgeorgiy.java.advanced.student.Student;
import info.kgeorgiy.java.advanced.student.StudentQuery;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StudentDB implements StudentQuery {
    private <T> List<T> mapStudents(List<Student> students, Function<Student, T> f) {
        return students.stream().map(f).toList();
    }

    private Stream<Student> sortStudents(Stream<Student> studentsStream) {
        return studentsStream.sorted(Comparator.comparing(Student::getLastName)
                .thenComparing(Student::getFirstName)
                .thenComparing(Student::getId, Comparator.reverseOrder()));
    }

    private <T> Stream<Student> findStudentsBy(Collection<Student> students, Function<Student, T> extractor, T value) {
        return sortStudents(students.stream()
                .filter(student -> extractor.apply(student).equals(value)));
    }

    @Override
    public List<String> getFirstNames(List<Student> students) {
        return mapStudents(students, Student::getFirstName);
    }

    @Override
    public List<String> getLastNames(List<Student> students) {
        return mapStudents(students, Student::getLastName);
    }

    @Override
    public List<GroupName> getGroups(List<Student> students) {
        return mapStudents(students, Student::getGroup);
    }

    @Override
    public List<String> getFullNames(List<Student> students) {
        return students.stream()
                .flatMap(student -> Stream.of(student.getFirstName() + " " + student.getLastName()))
                .toList();
    }

    @Override
    public Set<String> getDistinctFirstNames(List<Student> students) {
        return new TreeSet<>(getFirstNames(students));
    }

    @Override
    public String getMaxStudentFirstName(List<Student> students) {
        return students.stream()
                .max(Student::compareTo)
                .map(Student::getFirstName)
                .orElse("");
    }

    @Override
    public List<Student> sortStudentsById(Collection<Student> students) {
        return students.stream()
                .sorted()
                .toList();
    }

    @Override
    public List<Student> sortStudentsByName(Collection<Student> students) {
        return sortStudents(students.stream()).toList();
    }

    @Override
    public List<Student> findStudentsByFirstName(Collection<Student> students, String name) {
        return findStudentsBy(students, Student::getFirstName, name).toList();
    }

    @Override
    public List<Student> findStudentsByLastName(Collection<Student> students, String name) {
        return findStudentsBy(students, Student::getLastName, name).toList();
    }

    @Override
    public List<Student> findStudentsByGroup(Collection<Student> students, GroupName group) {
        return findStudentsBy(students, Student::getGroup, group).toList();
    }

    @Override
    public Map<String, String> findStudentNamesByGroup(Collection<Student> students, GroupName group) {
        return findStudentsBy(students, Student::getGroup, group)
                .collect(Collectors.toMap(Student::getLastName, Student::getFirstName, (name1, name2) -> name1));
    }
}
