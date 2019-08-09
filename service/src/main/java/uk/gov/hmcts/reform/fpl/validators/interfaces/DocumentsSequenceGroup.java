package uk.gov.hmcts.reform.fpl.validators.interfaces;

import javax.validation.GroupSequence;

@GroupSequence({ HasDocumentStatus.class, HasDocumentStatus.class })
public @interface DocumentsSequenceGroup { }
