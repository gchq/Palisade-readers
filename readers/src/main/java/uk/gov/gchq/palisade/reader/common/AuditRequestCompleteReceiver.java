package uk.gov.gchq.palisade.reader.common;

import uk.gov.gchq.palisade.reader.request.AuditRequest.ReadRequestCompleteAuditRequest;

public interface AuditRequestCompleteReceiver {
    void receive(ReadRequestCompleteAuditRequest readRequestCompleteAuditRequest);
}
