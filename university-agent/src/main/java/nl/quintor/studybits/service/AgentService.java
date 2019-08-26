package nl.quintor.studybits.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import nl.quintor.studybits.entity.Document;
import nl.quintor.studybits.entity.Student;
import nl.quintor.studybits.indy.wrapper.Issuer;
import nl.quintor.studybits.indy.wrapper.TrustAnchor;
import nl.quintor.studybits.indy.wrapper.Verifier;
import nl.quintor.studybits.indy.wrapper.dto.*;
import nl.quintor.studybits.indy.wrapper.message.*;
import nl.quintor.studybits.indy.wrapper.util.JSONUtil;
import nl.quintor.studybits.messages.StudyBitsMessageTypes;
import nl.quintor.studybits.messages.CredentialDefinitionType;
import org.apache.commons.lang3.NotImplementedException;
import org.hyperledger.indy.sdk.IndyException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static nl.quintor.studybits.indy.wrapper.message.IndyMessageTypes.*;
import static nl.quintor.studybits.messages.StudyBitsMessageTypes.DOCUMENT_OFFERS;
import static nl.quintor.studybits.messages.StudyBitsMessageTypes.EXCHANGE_POSITIONS;
import static nl.quintor.studybits.messages.CredentialDefinitionType.*;

@Service
@Slf4j
public class AgentService {
    @Autowired
    private TrustAnchor universityTrustAnchor;
    @Autowired
    private Issuer universityIssuer;
    @Autowired
    private Verifier universityVerifier;
    @Autowired
    private StudentService studentService;
    @Autowired
    private CredentialDefinitionService credentialDefinitionService;
    @Autowired
    private ExchangePositionService exchangePositionService;
    @Autowired
    private MessageEnvelopeCodec messageEnvelopeCodec;
    @Autowired
    private FileService fileService;

    @Value("${nl.quintor.studybits.university.name}")
    private String universityName;

    public MessageEnvelope processMessage(MessageEnvelope messageEnvelope) throws IndyException, ExecutionException, InterruptedException, IOException {
        String messageTypeURN = messageEnvelope.getMessageType().getURN();

        if (messageTypeURN.equals(GET_REQUEST.getURN())) {
            MessageEnvelope<String> envelopeType = MessageEnvelope.convertEnvelope(messageEnvelope, GET_REQUEST);
            MessageType requestedMessageType = MessageTypes.forURN(messageEnvelopeCodec.decryptMessage(envelopeType).get());

            if(requestedMessageType.equals(CREDENTIAL_OFFERS)) {
                return getCredentialOffers(messageEnvelope.getDid());
            } else if(requestedMessageType.equals(EXCHANGE_POSITIONS)) {
                return exchangePositionService.getAll(messageEnvelope.getDid());
            }else if(requestedMessageType.equals(DOCUMENT_OFFERS)) {
                return getDocumentOffers(messageEnvelope.getDid());
            }
        }
        else if (messageTypeURN.equals(CREDENTIAL_REQUEST.getURN())) {
            return handleCredentialRequest(MessageEnvelope.convertEnvelope(messageEnvelope, CREDENTIAL_REQUEST));
        }
        else if (messageTypeURN.equals(PROOF.getURN())) {
            return handleProof(MessageEnvelope.convertEnvelope(messageEnvelope, PROOF));
        }

        throw new NotImplementedException("Processing of message type not supported: " + messageEnvelope.getMessageType());
    }

    // Student sets up a connection with university agent
    public MessageEnvelope<ConnectionResponse> login(MessageEnvelope<ConnectionRequest> messageEnvelope) throws IndyException, ExecutionException, InterruptedException, JsonProcessingException {
        ConnectionRequest connectionRequest = messageEnvelopeCodec.decryptMessage(messageEnvelope).get();

        //Get studentID / current user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String studentId = auth.getName();

        ConnectionResponse connectionResponse = universityTrustAnchor.acceptConnectionRequest(connectionRequest).get();

        studentService.setStudentDid(studentId, connectionRequest.getDid());
        return messageEnvelopeCodec.encryptMessage(connectionResponse, IndyMessageTypes.CONNECTION_RESPONSE, connectionRequest.getDid()).get();
    }

    public MessageEnvelope<CredentialOfferList> getDocumentOffers(String did) throws JsonProcessingException, IndyException, ExecutionException, InterruptedException {
        log.debug("Getting document offers for did {}", did);
        Student student = studentService.getStudentByStudentDid(did);
        List<Document> documents = fileService.getDocumentsFromCache(student.getId());
        log.debug("Found student for which to get document offers {}", student);

        CredentialOfferList documentOffers = new CredentialOfferList();

        for (Document document : documents) {
            CompletableFuture<CredentialOffer> future = universityIssuer.createCredentialOffer(credentialDefinitionService.getCredentialDefinitionId(DOCUMENT), did);
            CredentialOffer documentOffer = future.get();
            documentOffers.addCredentialOffer(documentOffer);
            document.setNonce(documentOffer.getNonce());

            fileService.updateDocument(document);
        }

        log.debug("Returning documentOffers {}", documentOffers);
        return messageEnvelopeCodec.encryptMessage(documentOffers, StudyBitsMessageTypes.DOCUMENT_OFFERS, did).get();
    }

    public MessageEnvelope<CredentialOfferList> getCredentialOffers(String did) throws JsonProcessingException, IndyException, ExecutionException, InterruptedException {
        log.debug("Getting credential offers for did {}", did);
        Student student = studentService.getStudentByStudentDid(did);
        log.debug("Found student for which to get credential offers {}", student);

        CredentialOfferList credentialOffers = new CredentialOfferList();

        if (student.getTranscript() != null && !student.getTranscript().isProven()) {
            CredentialOffer credentialOffer = universityIssuer.createCredentialOffer(credentialDefinitionService.getCredentialDefinitionId(TRANSCRIPT), did).get();
            credentialOffers.addCredentialOffer(credentialOffer);
        }

        log.debug("Returning credentialOffers {}", credentialOffers);
        return messageEnvelopeCodec.encryptMessage(credentialOffers, IndyMessageTypes.CREDENTIAL_OFFERS, did).get();
    }

    private MessageEnvelope handleCredentialRequest(MessageEnvelope<CredentialRequest> messageEnvelope) throws IndyException, ExecutionException, InterruptedException, UnsupportedEncodingException, JsonProcessingException {
        CredentialRequest credentialRequest = messageEnvelopeCodec.decryptMessage(messageEnvelope).get();
        log.debug("Decrypted request");

        Map<String, Object> values = new HashMap<>();
        String credDefId = credentialRequest.getCredentialOffer().getCredDefId();
        if(credDefId.equals(credentialDefinitionService.getCredentialDefinitionId(CredentialDefinitionType.TRANSCRIPT))) {
            Student student = studentService.getStudentByStudentDid(messageEnvelope.getDid());
            values.put("first_name", student.getFirstName());
            values.put("last_name", student.getLastName());
            values.put("degree", student.getTranscript().getDegree());
            values.put("average", student.getTranscript().getAverage());
            values.put("status", student.getTranscript().getStatus());

            studentService.proveTranscript(student.getStudentId());
        } else if(credDefId.equals(credentialDefinitionService.getCredentialDefinitionId(CredentialDefinitionType.DOCUMENT))) {
            Document document = fileService.getDocumentFromCache(credentialRequest.getCredentialOffer().getNonce());
            values.put("name", document.getName() + "." + document.getType());
            values.put("size", document.getData().length);
            values.put("hash", fileService.storeFile(document));
            values.put("enc_type", "Indy:AuthCrypt");
        } else {
            throw new NotImplementedException("CredentialDefinition for CredentialRequest is not implemented.");
        }
        CredentialWithRequest credentialWithRequest = universityIssuer.createCredential(credentialRequest, values).get();

        return messageEnvelopeCodec.encryptMessage(credentialWithRequest, IndyMessageTypes.CREDENTIAL, messageEnvelope.getDid()).get();
    }

    private MessageEnvelope handleProof(MessageEnvelope<Proof> proofEnvelope) throws IndyException, ExecutionException, InterruptedException, IOException {
        log.debug("Handling proof");
        Student student = studentService.getStudentByStudentDid(proofEnvelope.getDid());
        ProofRequest proofRequest = JSONUtil.mapper.readValue(student.getProofRequest(), ProofRequest.class);

        Proof proof = messageEnvelopeCodec.decryptMessage(proofEnvelope).get();
        log.debug("Proof: {}", proof);
        universityVerifier.getVerifiedProofAttributes(proofRequest, proof, proofEnvelope.getDid()).get();


        exchangePositionService.fullfillPosition(student.getExchangePosition().getId());
        return null;
    }
}
