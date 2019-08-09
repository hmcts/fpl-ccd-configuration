package uk.gov.hmcts.reform.fpl.validators.interfaces;

import javax.validation.GroupSequence;

@GroupSequence({ HasDocuments.class, HasDocumentStatus.class })
public @interface EPOSequenceGroup { }
