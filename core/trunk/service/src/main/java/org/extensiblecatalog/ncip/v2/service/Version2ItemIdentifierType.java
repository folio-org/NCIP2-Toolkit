package org.extensiblecatalog.ncip.v2.service;

import org.apache.log4j.Logger;

public class Version2ItemIdentifierType extends ItemIdentifierType {

    private static final Logger LOG = Logger.getLogger(Version2ItemIdentifierType.class);

    public static final String VERSION_2_ITEM_IDENTIFIER_TYPE = "Schema";

    public static final Version2ItemIdentifierType ACCESSION_NUMBER
            = new Version2ItemIdentifierType(VERSION_2_ITEM_IDENTIFIER_TYPE, "Accession Number");
    public static final Version2ItemIdentifierType BARCODE
            = new Version2ItemIdentifierType(VERSION_2_ITEM_IDENTIFIER_TYPE, "Barcode");
    public static final Version2ItemIdentifierType UUID
            = new Version2ItemIdentifierType(VERSION_2_ITEM_IDENTIFIER_TYPE, "UUID");

    public static void loadAll() {
        LOG.debug("Loading Version1ItemIdentifierType.");
        // Do nothing - merely invoking this method forces the creation of the instances defined above.
    }

    public Version2ItemIdentifierType(String scheme, String value) {
        super(scheme, value);
    }

}
