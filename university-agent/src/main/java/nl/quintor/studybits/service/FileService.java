package nl.quintor.studybits.service;

import lombok.extern.slf4j.Slf4j;
import nl.quintor.studybits.entity.Document;
import nl.quintor.studybits.repository.FileRepository;
import nl.quintor.studybits.repository.IPFSRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class FileService {

    private final IPFSRepository ipfsRepository;
    private final FileRepository fileRepository;


    @Autowired
    public FileService(IPFSRepository repository, FileRepository fileRepository) {
        this.ipfsRepository = repository;
        this.fileRepository = fileRepository;
    }

    public String processFile(Document document) {


        return ipfsRepository.storeFile(document);
    }

    public Document cacheFile(Document document) {
        return fileRepository.saveAndFlush(document);
    }

    public List<Document> getDocumentsFromCache(long studentId) {
        return fileRepository.getDocumentsByStudent_Id(studentId);
    }

    public Document getDocumentFromCache(String nonce) {
        return fileRepository.getDocumentByNonce(nonce);
    }

    public void updateDocument(Document document) {
        fileRepository.saveAndFlush(document);
    }

    public Document retrieveFile(String id) {
        return ipfsRepository.getFile(id);
    }


}
