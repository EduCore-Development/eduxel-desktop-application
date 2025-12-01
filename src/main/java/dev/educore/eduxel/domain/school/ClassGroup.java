package dev.educore.eduxel.domain.school;

public class ClassGroup {
    private Long id;
    private String name;
    private Integer grade;
    private String schoolType;
    private String room;
    private Long teacherId;

    public ClassGroup() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getGrade() { return grade; }
    public void setGrade(Integer grade) { this.grade = grade; }

    public String getSchoolType() { return schoolType; }
    public void setSchoolType(String schoolType) { this.schoolType = schoolType; }

    public String getRoom() { return room; }
    public void setRoom(String room) { this.room = room; }

    public Long getTeacherId() { return teacherId; }
    public void setTeacherId(Long teacherId) { this.teacherId = teacherId; }
}
