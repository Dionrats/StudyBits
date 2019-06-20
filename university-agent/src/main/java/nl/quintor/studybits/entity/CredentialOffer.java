package nl.quintor.studybits.entity;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CredentialOffer {
    @Id
    @GeneratedValue
    private long id;
    @Column
    private String issuerDid;
    @Column
    private String schemaId;
    @Column
    private String credDefId;
    @Column
    private String keyCorrectnessProof;
    @Column(unique = true)
    private String nonce;

    @ManyToOne
    private Student student;

}
