package nl.quintor.studybits.controller;

import nl.quintor.studybits.entity.Document;
import nl.quintor.studybits.entity.DocumentDTO;
import nl.quintor.studybits.exceptions.DocumentNotProvidedException;
import nl.quintor.studybits.service.FileService;
import nl.quintor.studybits.service.StudentService;
import org.hyperledger.indy.sdk.IndyException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping(value = "/documents", produces = "application/json")
public class DocumentController {

    private static final String VALIDATION_EXTENTION = "sbv";
    static final String RESPONSE_CODE = "code";
    static final String RESPONSE_ACCEPTED = "accepted";
    static final String RESPONSE = "response";

    @Autowired
    private FileService fileService;
    @Autowired
    private StudentService studentService;


    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    @ResponseBody
    public String upload(@ModelAttribute DocumentDTO dto) throws IOException, InterruptedException, ExecutionException, IndyException {
        if (dto.getFile() == null)
            throw new DocumentNotProvidedException("Document was not included in request");

        Document document = fileService.cacheFile(convertToEntity(dto));

        JSONObject response = new JSONObject();
            response.put(RESPONSE_CODE, HttpStatus.ACCEPTED);
            response.put(RESPONSE_ACCEPTED, true);
            response.put(RESPONSE, document.toJson());
        return response.toString();
    }

    @PostMapping(value = "/verify", consumes = "multipart/form-data")
    @ResponseBody
    public String verify(MultipartFile file, MultipartFile validation) throws IOException, InterruptedException, ExecutionException, IndyException {
        if(file == null || validation == null)
            throw new DocumentNotProvidedException("Document(s) not provided in request");
        JSONObject response = new JSONObject();
        response.put(RESPONSE_CODE, HttpStatus.OK);

        String[] parts = validation.getOriginalFilename().split("[.]");
        response.put(RESPONSE_ACCEPTED, parts[parts.length-1].equals(VALIDATION_EXTENTION));

        if(!response.getBoolean(RESPONSE_ACCEPTED))
            return response.toString();

        response.put(RESPONSE, fileService.verifyDocument(file.getBytes(), validation.getBytes()));

        return response.toString();
    }

    private Document convertToEntity(DocumentDTO dto) throws IOException {
        Document document = new Document();

        document.setData(dto.getFile().getBytes());
        document.setName(dto.getName());
        document.setType(dto.getType());
        document.setStudent(studentService.getStudentByStudentId(dto.getStudent()));

        return document;
    }
}
