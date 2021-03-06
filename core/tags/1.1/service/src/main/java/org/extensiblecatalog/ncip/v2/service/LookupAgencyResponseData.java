/**
 * Copyright (c) 2010 eXtensible Catalog Organization
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the MIT/X11 license. The text of the license can be
 * found at http://www.opensource.org/licenses/mit-license.php. 
 */

package org.extensiblecatalog.ncip.v2.service;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import java.util.GregorianCalendar;
import java.util.List;
import java.math.BigDecimal;

/**
 * Carries data elements describing the LookupAgencyResponse.
 */
public class LookupAgencyResponseData implements NCIPResponseData {

//    public MessageType getMessageType() { return NCIPData.MessageType.RESPONSE; }
//    public boolean isInitiationMessage() { return false; }
//    public boolean isResponseMessage() { return true; }

    /** Version attribute */
    protected String version;

    public String getVersion() {
        return version;
    }
    public void setVersion(String version) {
        this.version = version;
    }


    /**
     * ResponseHeader
     */
    protected org.extensiblecatalog.ncip.v2.service.ResponseHeader responseHeader;

    /**
     * Set ResponseHeader.
     */
    public void setResponseHeader(org.extensiblecatalog.ncip.v2.service.ResponseHeader responseHeader) {

        this.responseHeader = responseHeader;

    }

    /**
     * Get ResponseHeader.
     */
    public org.extensiblecatalog.ncip.v2.service.ResponseHeader getResponseHeader() {

        return responseHeader;

    }

    /**
     * Problems
     */
    protected List<org.extensiblecatalog.ncip.v2.service.Problem> problems;

    /**
     * Set Problems.
     */
    public void setProblems(List<org.extensiblecatalog.ncip.v2.service.Problem> problems) {

        this.problems = problems;

    }

    /**
     * Get Problems.
     */
    public List<org.extensiblecatalog.ncip.v2.service.Problem> getProblems() {

        return problems;

    }

    /**
     * AgencyId
     */
    protected org.extensiblecatalog.ncip.v2.service.AgencyId agencyId;

    /**
     * Set AgencyId.
     */
    public void setAgencyId(org.extensiblecatalog.ncip.v2.service.AgencyId agencyId) {

        this.agencyId = agencyId;

    }

    /**
     * Get AgencyId.
     */
    public org.extensiblecatalog.ncip.v2.service.AgencyId getAgencyId() {

        return agencyId;

    }

    /**
     * OrganizationNameInformations
     */
    protected List<org.extensiblecatalog.ncip.v2.service.OrganizationNameInformation> organizationNameInformations;

    /**
     * Set OrganizationNameInformations.
     */
    public void setOrganizationNameInformations(
        List<org.extensiblecatalog.ncip.v2.service.OrganizationNameInformation> organizationNameInformations) {

        this.organizationNameInformations = organizationNameInformations;

    }

    /**
     * Get OrganizationNameInformations.
     */
    public List<org.extensiblecatalog.ncip.v2.service.OrganizationNameInformation> getOrganizationNameInformations() {

        return organizationNameInformations;

    }

    /**
     * AgencyAddressInformations
     */
    protected List<org.extensiblecatalog.ncip.v2.service.AgencyAddressInformation> agencyAddressInformations;

    /**
     * Set AgencyAddressInformations.
     */
    public void setAgencyAddressInformations(
        List<org.extensiblecatalog.ncip.v2.service.AgencyAddressInformation> agencyAddressInformations) {

        this.agencyAddressInformations = agencyAddressInformations;

    }

    /**
     * Get AgencyAddressInformations.
     */
    public List<org.extensiblecatalog.ncip.v2.service.AgencyAddressInformation> getAgencyAddressInformations() {

        return agencyAddressInformations;

    }

    /**
     * AuthenticationPrompts
     */
    protected List<org.extensiblecatalog.ncip.v2.service.AuthenticationPrompt> authenticationPrompts;

    /**
     * Set AuthenticationPrompts.
     */
    public void setAuthenticationPrompts(
        List<org.extensiblecatalog.ncip.v2.service.AuthenticationPrompt> authenticationPrompts) {

        this.authenticationPrompts = authenticationPrompts;

    }

    /**
     * Get AuthenticationPrompts.
     */
    public List<org.extensiblecatalog.ncip.v2.service.AuthenticationPrompt> getAuthenticationPrompts() {

        return authenticationPrompts;

    }

    /**
     * ApplicationProfileSupportedTypes
     */
    protected List<org.extensiblecatalog.ncip.v2.service.ApplicationProfileSupportedType>
        applicationProfileSupportedTypes;

    /**
     * Set ApplicationProfileSupportedTypes.
     */
    public void setApplicationProfileSupportedTypes(
        List<org.extensiblecatalog.ncip.v2.service.ApplicationProfileSupportedType> applicationProfileSupportedTypes) {

        this.applicationProfileSupportedTypes = applicationProfileSupportedTypes;

    }

    /**
     * Get ApplicationProfileSupportedTypes.
     */
    public List<org.extensiblecatalog.ncip.v2.service.ApplicationProfileSupportedType>
        getApplicationProfileSupportedTypes() {

        return applicationProfileSupportedTypes;

    }

    /**
     * ConsortiumAgreements
     */
    protected List<org.extensiblecatalog.ncip.v2.service.ConsortiumAgreement> consortiumAgreements;

    /**
     * Set ConsortiumAgreements.
     */
    public void setConsortiumAgreements(
        List<org.extensiblecatalog.ncip.v2.service.ConsortiumAgreement> consortiumAgreements) {

        this.consortiumAgreements = consortiumAgreements;

    }

    /**
     * Get ConsortiumAgreements.
     */
    public List<org.extensiblecatalog.ncip.v2.service.ConsortiumAgreement> getConsortiumAgreements() {

        return consortiumAgreements;

    }

    /**
     * AgencyUserPrivilegeTypes
     */
    protected List<org.extensiblecatalog.ncip.v2.service.AgencyUserPrivilegeType> agencyUserPrivilegeTypes;

    /**
     * Set AgencyUserPrivilegeTypes.
     */
    public void setAgencyUserPrivilegeTypes(
        List<org.extensiblecatalog.ncip.v2.service.AgencyUserPrivilegeType> agencyUserPrivilegeTypes) {

        this.agencyUserPrivilegeTypes = agencyUserPrivilegeTypes;

    }

    /**
     * Get AgencyUserPrivilegeTypes.
     */
    public List<org.extensiblecatalog.ncip.v2.service.AgencyUserPrivilegeType> getAgencyUserPrivilegeTypes() {

        return agencyUserPrivilegeTypes;

    }

    /**
     * Generic toString() implementation.
     *
     * @return String
     */
    @Override
    public String toString() {

        return ReflectionToStringBuilder.reflectionToString(this);

    }

}

