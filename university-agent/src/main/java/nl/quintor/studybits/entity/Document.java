package nl.quintor.studybits.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Document implements Serializable {
    @Id
    @GeneratedValue
    private long id;
    @Column
    private String name;
    @Column
    private String type;
    @Column
    private byte[] data;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;
}
