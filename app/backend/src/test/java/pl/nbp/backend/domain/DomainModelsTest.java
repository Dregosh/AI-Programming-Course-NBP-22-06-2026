package pl.nbp.backend.domain;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.List;
import static org.assertj.core.api.Assertions.*;

class DomainModelsTest {

    @Test
    void futurePurchaseDateIsInvalid() {
        assertThatThrownBy(() -> new ServiceRequest(
            RequestType.RETURN, EquipmentCategory.SMARTPHONE, "iPhone",
            LocalDate.now().plusDays(1), null))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void complaintWithBlankReasonIsInvalid() {
        assertThatThrownBy(() -> new ServiceRequest(
            RequestType.COMPLAINT, EquipmentCategory.SMARTPHONE, "iPhone",
            LocalDate.now().minusDays(10), ""))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void returnWithBlankReasonIsValid() {
        assertThatNoException().isThrownBy(() -> new ServiceRequest(
            RequestType.RETURN, EquipmentCategory.SMARTPHONE, "iPhone",
            LocalDate.now().minusDays(10), null));
    }

    @Test
    void approveDecisionHasBindingTrue() {
        var d = new Decision(DecisionOutcome.APPROVE, false, "OK", List.of(), List.of());
        assertThat(d.binding()).isTrue();
    }

    @Test
    void rejectDecisionHasBindingFalse() {
        var d = new Decision(DecisionOutcome.REJECT, true, "No", List.of(), List.of());
        assertThat(d.binding()).isFalse();
    }

    @Test
    void escalateDecisionHasBindingFalse() {
        var d = new Decision(DecisionOutcome.ESCALATE, true, "Unclear", List.of(), List.of());
        assertThat(d.binding()).isFalse();
    }
}
