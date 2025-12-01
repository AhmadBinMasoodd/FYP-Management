package com.company.fyp_management.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "grades")
public class Grades {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(unique = true, nullable = false)
    private Integer grade_id;

    @OneToOne
    @JoinColumn(name = "student_id", nullable = false, unique = true)
    private Student student;

    @Column(nullable = false)
    private int rubric1;

    @Column(nullable = false)
    private int rubric2;

    @Column(nullable = false)
    private int rubric3;

    @Column(nullable = false)
    private int rubric4;

    @Column(nullable = false)
    private int rubric5;

    @Column(nullable = false)
    private char grade;
    

    // Getters and setters
    public Integer getGrade_id() {
        return grade_id;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public Student getStudent() {
        return student;
    }

    public void setRubric1(int rubric1) {
        if (rubric1 < 1 || rubric1 > 5) {
            throw new IllegalArgumentException("Rubric must be between 1 and 5");
        }
        this.rubric1 = rubric1;
    }

    public int getRubric1() {
        return rubric1;
    }

    public void setRubric2(int rubric2) {
        if (rubric2 < 1 || rubric2 > 5) {
            throw new IllegalArgumentException("Rubric must be between 1 and 5");
        }
        this.rubric2 = rubric2;
    }

    public int getRubric2() {
        return rubric2;
    }

    public void setRubric3(int rubric3) {
        if (rubric3 < 1 || rubric3 > 5) {
            throw new IllegalArgumentException("Rubric must be between 1 and 5");
        }
        this.rubric3 = rubric3;
    }

    public int getRubric3() {
        return rubric3;
    }

    public void setRubric4(int rubric4) {
        if (rubric4 < 1 || rubric4 > 5) {
            throw new IllegalArgumentException("Rubric must be between 1 and 5");
        }
        this.rubric4 = rubric4;
    }

    public int getRubric4() {
        return rubric4;
    }

    public void setRubric5(int rubric5) {
        if (rubric5 < 1 || rubric5 > 5) {
            throw new IllegalArgumentException("Rubric must be between 1 and 5");
        }
        this.rubric5 = rubric5;
    }

    public int getRubric5() {
        return rubric5;
    }

    public void setGrade(char grade) {
        this.grade = grade;
    }

    public void setGrade(){
        int total = this.rubric1 + this.rubric2 + this.rubric3 + this.rubric4 + this.rubric5;
        double average = total / 5.0;

        if (average >= 4.5) {
            this.grade = 'A';
        } else if (average >= 3.5) {
            this.grade = 'B';
        } else if (average >= 2.5) {
            this.grade = 'C';
        } else if (average >= 1.5) {
            this.grade = 'D';
        } else {
            this.grade = 'F';
        }
    }

    public char getGrade() {
        return grade;
    }

    @PrePersist
    @PreUpdate
    private void computeGradeBeforeSave() {
        int total = this.rubric1 + this.rubric2 + this.rubric3 + this.rubric4 + this.rubric5;
        double average = total / 5.0;
        if (average >= 4.5) {
            this.grade = 'A';
        } else if (average >= 3.5) {
            this.grade = 'B';
        } else if (average >= 2.5) {
            this.grade = 'C';
        } else if (average >= 1.5) {
            this.grade = 'D';
        } else {
            this.grade = 'F';
        }
    }

}
