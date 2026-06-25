package pl.nbp.backend.policy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import pl.nbp.backend.domain.RequestType;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest
class PolicyProviderTest {

    @Autowired
    PolicyProvider policyProvider;

    @Test
    void returnPolicyIsNonBlank() {
        assertThat(policyProvider.getPolicyText(RequestType.RETURN)).isNotBlank();
    }

    @Test
    void complaintPolicyIsNonBlank() {
        assertThat(policyProvider.getPolicyText(RequestType.COMPLAINT)).isNotBlank();
    }

    @Test
    void returnAndComplaintPoliciesDiffer() {
        String ret = policyProvider.getPolicyText(RequestType.RETURN);
        String comp = policyProvider.getPolicyText(RequestType.COMPLAINT);
        assertThat(ret).isNotEqualTo(comp);
    }
}
