package nl.quintor.studybits.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import nl.quintor.studybits.entity.CredentialOffer;
import nl.quintor.studybits.indy.wrapper.Issuer;
import nl.quintor.studybits.repository.CredentialOfferRepository;
import org.hyperledger.indy.sdk.IndyException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;

@Service
@Slf4j
public class CredentialOfferService {

    private final StudentService studentService;
    private final Issuer universityIssuer;
    private final CredentialOfferRepository repository;
    private final ModelMapper mapper;

    @Autowired
    public CredentialOfferService(StudentService studentService, Issuer universityIssuer, CredentialOfferRepository repository, ModelMapper mapper) {
        this.studentService = studentService;
        this.universityIssuer = universityIssuer;
        this.repository = repository;
        this.mapper = mapper;
    }

    public void createCredentialOffer(String credentialDefinitionId, String targetDid) throws JsonProcessingException, IndyException, ExecutionException, InterruptedException {
        CredentialOffer credentialOffer = mapper.map(universityIssuer.createCredentialOffer(credentialDefinitionId, targetDid).get(), CredentialOffer.class);

        if(credentialOffer.getStudent() == null)
            credentialOffer.setStudent(studentService.getStudentByStudentDid(targetDid));

        repository.saveAndFlush(credentialOffer);
    }

}
