package nl.quintor.studybits.service;

import nl.quintor.studybits.entity.Document;
import nl.quintor.studybits.entity.Student;
import nl.quintor.studybits.entity.Transcript;
import nl.quintor.studybits.indy.wrapper.Issuer;
import nl.quintor.studybits.indy.wrapper.Verifier;
import nl.quintor.studybits.indy.wrapper.dto.*;
import nl.quintor.studybits.indy.wrapper.message.IndyMessageTypes;
import nl.quintor.studybits.indy.wrapper.message.MessageEnvelope;
import nl.quintor.studybits.indy.wrapper.message.MessageEnvelopeCodec;
import nl.quintor.studybits.messages.StudyBitsMessageTypes;
import nl.quintor.studybits.messages.CredentialDefinitionType;
import org.apache.commons.lang3.NotImplementedException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;

@RunWith(SpringRunner.class)
public class AgentServiceTest {

    @TestConfiguration
    static class AgentServiceTestConfiguration {
        @Bean
        public AgentService agentService() {
            return new AgentService();
        }
    }

    @Autowired
    private AgentService sut;

    @MockBean
    Issuer universityIssuer;
    @MockBean
    Verifier universityVerifier;
    @MockBean
    StudentService studentService;
    @MockBean
    CredentialDefinitionService credentialDefinitionService;
    @MockBean
    ExchangePositionService exchangePositionService;
    @MockBean
    MessageEnvelopeCodec messageEnvelopeCodec;
    @MockBean
    FileService fileService;

    private Student student;
    private CredentialOfferList credentialOfferList;

    @BeforeClass
    public static void setUpClass() {
        StudyBitsMessageTypes.init();
    }

    @Before
    public void setUp() throws Exception {
        student = new Student();
        student.setStudentId("0");
        student.setFirstName("Lisa");
        student.setLastName("Veren");
        student.setPassword("1234");
        student.setStudentDid("a1b2c3d4e5f6");
        student.setMyDid("b1c2d3e4f5g6");
        student.setDocuments(Collections.singletonList(new Document(0, "test", "txt", new byte[1], "1234", student)));
        student.setTranscript(new Transcript("Bachelor of Arts, Marketing", "enrolled", "8", false));

        String credDefId = "testCredDefId";

        CredentialOffer credentialOffer = new CredentialOffer(student.getMyDid(), "", credDefId, null, "123");
        credentialOfferList = new CredentialOfferList();
        credentialOfferList.addCredentialOffer(credentialOffer);


        CompletableFuture<CredentialOffer> f1 = CompletableFuture.completedFuture(credentialOffer);

        Mockito.when(studentService.getStudentByStudentDid(student.getStudentDid())).thenReturn(student);
        Mockito.when(fileService.getDocumentsFromCache(student.getId())).thenReturn(student.getDocuments());
        Mockito.when(credentialDefinitionService.getCredentialDefinitionId(any())).thenReturn(credDefId);
        Mockito.when(universityIssuer.createCredentialOffer(credDefId, student.getStudentDid())).thenReturn(f1);
        Mockito.when(messageEnvelopeCodec.encryptMessage(any(CredentialOfferList.class), any(), eq(student.getStudentDid()))).thenReturn(CompletableFuture.completedFuture(null));
        Mockito.doNothing().when(fileService).updateDocument(any(Document.class));

    }

    @Test(expected = NotImplementedException.class)
    public void unsupportedMessageTypeTest() throws Exception {
        JSONObject json = new JSONObject();
        json.put("id", student.getStudentDid());
        json.put("type", "urn:indy:sov:agent:message_type:sovrin.org/connection/1.0/verinym");
        json.put("message", "");
        MessageEnvelope messageEnvelope = MessageEnvelope.parseFromString(json.toString(), IndyMessageTypes.VERINYM);

        sut.processMessage(messageEnvelope);
    }

    @Test
    public void getDocumentOffersTest() throws Exception{
        JSONObject json = new JSONObject();
        json.put("id", student.getStudentDid());
        json.put("type", "urn:indy:sov:agent:message_type:sovrin.org/get/1.0/get_request");
        json.put("message", "");
        MessageEnvelope messageEnvelope = MessageEnvelope.parseFromString(json.toString(), IndyMessageTypes.GET_REQUEST);

        Mockito.when(messageEnvelopeCodec.decryptMessage(any())).thenReturn(CompletableFuture.completedFuture(StudyBitsMessageTypes.DOCUMENT_OFFERS.getURN()));

        sut.processMessage(messageEnvelope);

        //assert
        Mockito.verify(messageEnvelopeCodec).encryptMessage(credentialOfferList, StudyBitsMessageTypes.DOCUMENT_OFFERS, student.getStudentDid());

    }

    @Test
    public void getCredentialOffersTest() throws Exception{
        JSONObject json = new JSONObject();
        json.put("id", student.getStudentDid());
        json.put("type", "urn:indy:sov:agent:message_type:sovrin.org/get/1.0/get_request");
        json.put("message", "");
        MessageEnvelope messageEnvelope = MessageEnvelope.parseFromString(json.toString(), IndyMessageTypes.GET_REQUEST);

        Mockito.when(messageEnvelopeCodec.decryptMessage(any())).thenReturn(CompletableFuture.completedFuture(IndyMessageTypes.CREDENTIAL_OFFERS.getURN()));

        sut.processMessage(messageEnvelope);

        //assert
        Mockito.verify(messageEnvelopeCodec).encryptMessage(credentialOfferList, IndyMessageTypes.CREDENTIAL_OFFERS, student.getStudentDid());

    }

    @Test
    public void getCredentialOffersIsProvenTest() throws Exception{
        student.getTranscript().setProven(true);

        sut.getCredentialOffers(student.getStudentDid());

        //assert
        Mockito.verify(messageEnvelopeCodec).encryptMessage(any(CredentialOfferList.class), eq(IndyMessageTypes.CREDENTIAL_OFFERS), eq(student.getStudentDid()));

    }

    @Test
    public void getCredentialOffersNoTranscriptTest() throws Exception{
        student.setTranscript(null);

        sut.getCredentialOffers(student.getStudentDid());

        //assert
        Mockito.verify(messageEnvelopeCodec).encryptMessage(any(CredentialOfferList.class), eq(IndyMessageTypes.CREDENTIAL_OFFERS), eq(student.getStudentDid()));

    }

    @Test
    public void handleCredentialRequestTest() throws Exception {
        CredentialRequest credentialRequest = new CredentialRequest("", "", credentialOfferList.getCredentialOffers().get(0));

        Mockito.when(messageEnvelopeCodec.decryptMessage(any())).thenReturn(CompletableFuture.completedFuture(credentialRequest));
        Mockito.when(studentService.getStudentByStudentDid(student.getStudentDid())).thenReturn(student);
        Mockito.doNothing().when(studentService).proveTranscript(student.getStudentDid());
        CredentialWithRequest credentialWithRequest = new CredentialWithRequest();
        Mockito.when(universityIssuer.createCredential(eq(credentialRequest), anyMap())).thenReturn(CompletableFuture.completedFuture(credentialWithRequest));
        Mockito.when(messageEnvelopeCodec.encryptMessage(credentialWithRequest, IndyMessageTypes.CREDENTIAL, student.getStudentDid())).thenReturn(CompletableFuture.completedFuture(null));


        JSONObject json = new JSONObject();
        json.put("id", student.getStudentDid());
        json.put("type", "urn:indy:sov:agent:message_type:sovrin.org/credential/1.0/credential_request");
        json.put("message", "");
        MessageEnvelope messageEnvelope = MessageEnvelope.parseFromString(json.toString(), IndyMessageTypes.CREDENTIAL_REQUEST);

        sut.processMessage(messageEnvelope);

        //assert
        Mockito.verify(messageEnvelopeCodec).encryptMessage(credentialWithRequest, IndyMessageTypes.CREDENTIAL, student.getStudentDid());
    }

    @Test
    public void handleDocumentRequestTest() throws Exception {
        CredentialRequest credentialRequest = new CredentialRequest("", "", credentialOfferList.getCredentialOffers().get(0));

        Mockito.when(messageEnvelopeCodec.decryptMessage(any())).thenReturn(CompletableFuture.completedFuture(credentialRequest));
        Mockito.when(credentialDefinitionService.getCredentialDefinitionId(CredentialDefinitionType.TRANSCRIPT)).thenReturn("");

        Mockito.when(fileService.getDocumentFromCache(credentialOfferList.getCredentialOffers().get(0).getNonce())).thenReturn(student.getDocuments().get(0));

        CredentialWithRequest credentialWithRequest = new CredentialWithRequest();
        Mockito.when(universityIssuer.createCredential(eq(credentialRequest), anyMap())).thenReturn(CompletableFuture.completedFuture(credentialWithRequest));
        Mockito.when(messageEnvelopeCodec.encryptMessage(credentialWithRequest, IndyMessageTypes.CREDENTIAL, student.getStudentDid())).thenReturn(CompletableFuture.completedFuture(null));

        JSONObject json = new JSONObject();
        json.put("id", student.getStudentDid());
        json.put("type", "urn:indy:sov:agent:message_type:sovrin.org/credential/1.0/credential_request");
        json.put("message", "");
        MessageEnvelope messageEnvelope = MessageEnvelope.parseFromString(json.toString(), IndyMessageTypes.CREDENTIAL_REQUEST);

        sut.processMessage(messageEnvelope);

        //assert
        Mockito.verify(messageEnvelopeCodec).encryptMessage(credentialWithRequest, IndyMessageTypes.CREDENTIAL, student.getStudentDid());
    }

    @Test(expected = NotImplementedException.class)
    public void handleNonImplementedRequestTest() throws Exception {
        CredentialRequest credentialRequest = new CredentialRequest("", "", credentialOfferList.getCredentialOffers().get(0));

        Mockito.when(messageEnvelopeCodec.decryptMessage(any())).thenReturn(CompletableFuture.completedFuture(credentialRequest));
        Mockito.when(credentialDefinitionService.getCredentialDefinitionId(CredentialDefinitionType.TRANSCRIPT)).thenReturn("");
        Mockito.when(credentialDefinitionService.getCredentialDefinitionId(CredentialDefinitionType.DOCUMENT)).thenReturn("");

        JSONObject json = new JSONObject();
        json.put("id", student.getStudentDid());
        json.put("type", "urn:indy:sov:agent:message_type:sovrin.org/credential/1.0/credential_request");
        json.put("message", "");
        MessageEnvelope messageEnvelope = MessageEnvelope.parseFromString(json.toString(), IndyMessageTypes.CREDENTIAL_REQUEST);


        sut.processMessage(messageEnvelope);
    }

}
