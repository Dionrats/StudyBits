package nl.quintor.studybits.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Student {
    @Id
    @GeneratedValue
    private long id;

    @Column(unique = true)
    private String studentId;

    @Column
    private String password;

    @Column
    private String firstName;

    @Column
    private String lastName;

    @Column(unique = true)
    private String studentDid;

    @Lob
    private String proofRequest;

    @OneToOne
    private ExchangePosition exchangePosition;

    @Column
    private String myDid;

    @Embedded
    private Transcript transcript;

    @OneToMany(mappedBy = "student", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Document> documents;

    public boolean hasDid() {
        return this.getStudentDid() != null;
    }
}
