package nl.quintor.studybits.service;

import lombok.extern.slf4j.Slf4j;
import nl.quintor.studybits.entity.Document;
import nl.quintor.studybits.repository.FileRepository;
import nl.quintor.studybits.repository.IPFSRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class FileService {

    private final IPFSRepository ipfsRepository;
    private final FileRepository fileRepository;
    private final CredentialOfferService credentialOfferService;


    @Autowired
    public FileService(IPFSRepository repository, FileRepository fileRepository, CredentialOfferService credentialOfferService) {
        this.ipfsRepository = repository;
        this.fileRepository = fileRepository;
        this.credentialOfferService = credentialOfferService;
    }

    public String processFile(Document document) {


        return ipfsRepository.storeFile(document);
    }

    public Document cacheFile(Document document) {
        return fileRepository.saveAndFlush(document);
    }

//    public void sendCredentialOffer(Document document) throws JsonProcessingException, IndyException, ExecutionException, InterruptedException {
//        CredentialOffer offer = universityIssuer.createCredentialOffer(id, document.getStudent().getMyDid()).get();


      //  offer.hashCode();
   // }

    public Document retrieveFile(String id) {
        return ipfsRepository.getFile(id);
    }


}
