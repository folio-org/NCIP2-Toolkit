package org.extensiblecatalog.ncip.v2.service;

import org.apache.log4j.Logger;

public class Version2RequestIdentifierType extends RequestIdentifierType{
    private static final Logger LOG = Logger.getLogger(Version2RequestIdentifierType.class);

    /** The Scheme URI */
    public static final String VERSION_2_REQUEST_IDENTIFIER_TYPE = "Schema";

    public static final Version2RequestIdentifierType UUID
            = new Version2RequestIdentifierType(VERSION_2_REQUEST_IDENTIFIER_TYPE, "UUID");

    public Version2RequestIdentifierType(String scheme, String value) {
        super(scheme, value);
    }

    public static void loadAll() {
        LOG.debug("Loading Version2RequestIdentifierType.");
    }

}
