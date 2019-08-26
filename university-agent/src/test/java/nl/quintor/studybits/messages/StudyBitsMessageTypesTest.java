package nl.quintor.studybits.messages;

import nl.quintor.studybits.indy.wrapper.message.MessageTypes;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
public class StudyBitsMessageTypesTest {

    @BeforeClass
    public static void setUp() {
        StudyBitsMessageTypes.init();
    }

    @Test
    public void assertExchangePositionsTest() {
        assertThat(MessageTypes.forURN(StudyBitsMessageTypes.EXCHANGE_POSITIONS.getURN())).isNotNull();
    }

    @Test
    public void assertDocumentOffersTest() {
        assertThat(MessageTypes.forURN(StudyBitsMessageTypes.DOCUMENT_OFFERS.getURN())).isNotNull();
    }

    @Test
    public void alreadyInitializedTest() {
        StudyBitsMessageTypes.init();
    }



}
