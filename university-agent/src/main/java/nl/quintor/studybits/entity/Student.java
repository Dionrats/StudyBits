package nl.quintor.studybits.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;
import java.util.Set;

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

    @OneToMany(mappedBy = "id")
    private Set<Document> documents;

    @OneToMany(mappedBy = "id")
    private List<CredentialOffer> credentialOffers;

    public boolean hasDid() {
        if(this.getStudentDid() != null){
            return true;
        }

        return false;
    }
}
