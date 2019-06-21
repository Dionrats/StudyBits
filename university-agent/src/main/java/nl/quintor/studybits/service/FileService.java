package nl.quintor.studybits.service;

import lombok.extern.slf4j.Slf4j;
import nl.quintor.studybits.entity.Document;
import nl.quintor.studybits.entity.Student;
import nl.quintor.studybits.indy.wrapper.IndyWallet;
import nl.quintor.studybits.repository.FileRepository;
import nl.quintor.studybits.repository.IPFSRepository;
import org.hyperledger.indy.sdk.IndyException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Slf4j
@Service
public class FileService {

    private final IPFSRepository ipfsRepository;
    private final FileRepository fileRepository;
    private final StudentService studentService;
    private final IndyWallet wallet;

    @Autowired
    public FileService(IPFSRepository repository, FileRepository fileRepository, StudentService studentService,IndyWallet wallet) {
        this.ipfsRepository = repository;
        this.fileRepository = fileRepository;
        this.studentService = studentService;
        this.wallet = wallet;
    }

    public String storeFile(Document document) {
        removeFileFromCache(document);

        return ipfsRepository.storeFile(document.getData());
    }

    public Document cacheFile(Document document) throws IndyException, ExecutionException, InterruptedException {
        //Asymmetric encryption of file using Student Pk and University Pk.
        document.setData(wallet.authEncrypt(document.getData(), document.getStudent().getStudentDid()).get().getMessage());

        return fileRepository.saveAndFlush(document);
    }

    public void removeFileFromCache(Document document) {
        Student student = document.getStudent();
        student.getDocuments().remove(document);
        studentService.updateStudent(student);
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

}
