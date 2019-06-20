package nl.quintor.studybits.controller;

import nl.quintor.studybits.entity.Document;
import nl.quintor.studybits.entity.DocumentDTO;
import nl.quintor.studybits.exceptions.DocumentNotProvidedException;
import nl.quintor.studybits.service.CredentialDefinitionService;
import nl.quintor.studybits.service.CredentialOfferService;
import nl.quintor.studybits.service.FileService;
import nl.quintor.studybits.utils.CredentialDefinitionType;
import org.hyperledger.indy.sdk.IndyException;
import org.json.JSONObject;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping(value = "/documents", produces = "application/json")
public class DocumentController {

    private final FileService fileService;
    private final CredentialOfferService credentialOfferService;
    private final CredentialDefinitionService credentialDefinitionService;
    private final ModelMapper mapper;


    @Autowired
    public DocumentController(FileService fileService, ModelMapper mapper, CredentialOfferService credentialOfferService, CredentialDefinitionService credentialDefinitionService) {
        this.fileService = fileService;
        this.mapper = mapper;
        this.credentialOfferService = credentialOfferService;
        this.credentialDefinitionService = credentialDefinitionService;
    }

    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    @ResponseBody
    public String upload(@ModelAttribute DocumentDTO dto) throws IOException, InterruptedException, ExecutionException, IndyException {
        if (dto == null) {
            throw new DocumentNotProvidedException("Document was not included in request");
        }
        Document document = fileService.cacheFile(convertToEntity(dto));
        credentialOfferService.createCredentialOffer(credentialDefinitionService.getCredentialDefinitionId(CredentialDefinitionType.DOCUMENT), document.getStudent().getStudentDid());

        JSONObject response = new JSONObject();
            response.put("code", HttpStatus.ACCEPTED);
            response.put("accepted", true);
            response.put("response", converToDTO(document).toJSON());
        return response.toString();
    }

//    @GetMapping(value = "/download/{id}", produces = "application/octet-stream")
//    public ResponseEntity<byte[]> download(@PathVariable("id") String id) {}


    private DocumentDTO converToDTO(Document document) {
        return mapper.map(document, DocumentDTO.class);
    }

    private Document convertToEntity(DocumentDTO dto) throws IOException {
        Document document = mapper.map(dto, Document.class);
        if(document.getData() == null)
            document.setData(dto.getFile().getBytes());

        return document;
    }
}
