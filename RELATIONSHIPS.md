# FYP Management System - Entity Relationships & Helper Methods

## Overview
This document describes all bidirectional relationships and helper methods for managing them across entities.

---

## 1. Student → FileSubmission (1:Many)
**Relationship Type:** One-to-Many (Bidirectional)

**In Student.java:**
```java
@OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
private List<FileSubmission> fileSubmissions = new ArrayList<>();

// Add submission
public void addFileSubmission(FileSubmission fileSubmission) {
    if (fileSubmission != null) {
        fileSubmissions.add(fileSubmission);
        fileSubmission.setStudent(this);
    }
}

// Remove submission
public void removeFileSubmission(FileSubmission fileSubmission) {
    if (fileSubmission != null) {
        fileSubmissions.remove(fileSubmission);
        fileSubmission.setStudent(null);
    }
}

// Get all submissions
public List<FileSubmission> getFileSubmissions() {
    return fileSubmissions;
}
```

**In FileSubmission.java:**
```java
@ManyToOne
@JoinColumn(name = "student_id", nullable = false)
private Student student;

public void setStudent(Student student) {
    this.student = student;
}
```

**Usage:**
```java
Student student = studentRepo.findById(1).get();
FileSubmission submission = new FileSubmission();
submission.setFilename("proposal.pdf");

// Method 1: Add through Student
student.addFileSubmission(submission);

// Method 2: Set directly
submission.setStudent(student);

// Get all submissions
List<FileSubmission> submissions = student.getFileSubmissions();
```

---

## 2. Student → Grades (1:1)
**Relationship Type:** One-to-One (Bidirectional)

**In Student.java:**
```java
@OneToOne(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
private Grades grades;

// Set grades
public void setGrades(Grades grades) {
    this.grades = grades;
    if (grades != null) {
        grades.setStudent(this);
    }
}

// Get grades
public Grades getGrades() {
    return grades;
}

// Remove grades
public void removeGrades() {
    if (this.grades != null) {
        this.grades.setStudent(null);
        this.grades = null;
    }
}
```

**In Grades.java:**
```java
@OneToOne
@JoinColumn(name = "student_id", nullable = false, unique = true)
private Student student;

public void setStudent(Student student) {
    this.student = student;
}
```

**Usage:**
```java
Student student = studentRepo.findById(1).get();
Grades grades = new Grades();
grades.setRubric1(5);
grades.setRubric2(4);

// Set relationship
student.setGrades(grades);

// Get grades
Grades studentGrades = student.getGrades();
```

---

## 3. FileSubmission → Feedback (1:Many)
**Relationship Type:** One-to-Many (Bidirectional)

**In FileSubmission.java:**
```java
@OneToMany(mappedBy = "submission", cascade = CascadeType.ALL, orphanRemoval = true)
private List<Feedback> feedbacks = new ArrayList<>();

// Add feedback
public void addFeedback(Feedback feedback) {
    if (feedback != null) {
        feedbacks.add(feedback);
        feedback.setSubmission(this);
    }
}

// Remove feedback
public void removeFeedback(Feedback feedback) {
    if (feedback != null) {
        feedbacks.remove(feedback);
        feedback.setSubmission(null);
    }
}

// Get all feedbacks
public List<Feedback> getFeedbacks() {
    return feedbacks;
}
```

**In Feedback.java:**
```java
@ManyToOne(fetch = FetchType.LAZY, optional = false)
@JoinColumn(name = "submission_id", nullable = false)
private FileSubmission submission;

public void setSubmission(FileSubmission submission) {
    this.submission = submission;
}
```

**Usage:**
```java
FileSubmission submission = submissionRepo.findById(5).get();
Feedback feedback = new Feedback();
feedback.setContent("Good proposal but needs more details");

// Add feedback
submission.addFeedback(feedback);

// Get all feedbacks for submission
List<Feedback> feedbacks = submission.getFeedbacks();

// Remove feedback
submission.removeFeedback(feedback);
```

---

## 4. FileSubmission → DocumentTypes (Many:1)
**Relationship Type:** Many-to-One (Unidirectional - no back reference needed)

**In FileSubmission.java:**
```java
@ManyToOne
@JoinColumn(name = "doc_type", referencedColumnName = "doc_type", nullable = false)
private DocumentTypes documentType;

public DocumentTypes getDocumentType() {
    return documentType;
}

public void setDocumentType(DocumentTypes documentType) {
    this.documentType = documentType;
}

public String getDoc_type() {
    return documentType == null ? null : documentType.getDoc_type();
}
```

**Usage:**
```java
FileSubmission submission = new FileSubmission();
DocumentTypes docType = docTypesRepo.findById("Proposal").get();

submission.setDocumentType(docType);

String type = submission.getDoc_type(); // "Proposal"
```

---

## 5. Faculty → Student (Supervisor) (Many:1)
**Relationship Type:** Many-to-One (Store supervisorId in Student)

**In Student.java:**
```java
@Column(name = "supervisor_id", nullable = false)
private Integer supervisorId = -1; // -1 means unassigned

public Integer getSupervisorId() {
    return supervisorId;
}

public void setSupervisorId(Integer supervisorId) {
    this.supervisorId = supervisorId;
}
```

**Usage:**
```java
Faculty supervisor = facultyRepo.findById(10).get();
Student student = studentRepo.findById(5).get();

// Assign supervisor
student.setSupervisorId(supervisor.getNumericId());
studentRepo.save(student);

// Get supervisor ID
Integer supervisorId = student.getSupervisorId();

// Get all students of a supervisor
List<Student> myStudents = studentRepo.findBySupervisorId(supervisorId);
```

---

## Cascade & Orphan Removal Rules

### Cascade.ALL
- **DELETE:** When parent is deleted, all children are deleted
- **PERSIST:** When parent is saved, unsaved children are automatically saved
- **UPDATE:** When parent is updated, children are updated

### orphanRemoval = true
- **ORPHAN:** Child entities with no parent reference are automatically deleted

### FetchType Options
- **LAZY:** Children are loaded only when accessed (more efficient)
- **EAGER:** Children are loaded immediately with parent

---

## Complete Entity Usage Example

```java
// Create a student
Student student = new Student();
student.setName("Ahmed Ali");
student.setEmail("ahmed@uet.edu");
student.setPassword("password123");
student.setAddress("Lahore, Pakistan");

// Create and assign supervisor
student.setSupervisorId(1);

// Create a document type
DocumentTypes proposalType = docTypesRepo.findById("Proposal").get();

// Create and add file submission
FileSubmission submission = new FileSubmission();
submission.setFilename("proposal.pdf");
submission.setDocumentType(proposalType);
student.addFileSubmission(submission); // Bidirectional

// Create and add feedback
Feedback feedback = new Feedback();
feedback.setContent("Excellent work!");
submission.addFeedback(feedback); // Bidirectional

// Create and add grades
Grades grades = new Grades();
grades.setRubric1(5);
grades.setRubric2(5);
grades.setRubric3(4);
grades.setRubric4(4);
grades.setRubric5(5);
grades.setGrade('A');
student.setGrades(grades); // Bidirectional

// Save (all related entities cascade)
studentRepo.save(student);
```

---

## Benefits of These Relationships

✅ **Automatic Cascade:** Save one, save all related entities  
✅ **Orphan Removal:** Delete parent → automatically clean children  
✅ **Bidirectional Consistency:** Changes reflected from both sides  
✅ **Lazy Loading:** Better performance for large data sets  
✅ **Clean API:** Helper methods like addFeedback(), removeGrades()  
✅ **Type Safety:** No casting or reflection needed  

---

## Service Layer Usage

```java
@Service
public class StudentService {
    
    @Autowired
    private StudentRepository studentRepository;
    
    public void submitStudentWork(Integer studentId, FileSubmission submission) {
        Student student = studentRepository.findById(studentId).get();
        student.addFileSubmission(submission);
        studentRepository.save(student);
    }
    
    public List<Feedback> getSubmissionFeedbacks(Integer submissionId) {
        FileSubmission submission = submissionRepo.findById(submissionId).get();
        return submission.getFeedbacks();
    }
    
    public void gradeStudent(Integer studentId, Grades grades) {
        Student student = studentRepository.findById(studentId).get();
        student.setGrades(grades);
        studentRepository.save(student);
    }
}
```

