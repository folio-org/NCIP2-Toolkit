package org.extensiblecatalog.ncip.v2.aleph.util;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Properties;
import java.util.Random;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.TransformerException;

import org.extensiblecatalog.ncip.v2.aleph.AlephLookupItemSetService;
import org.extensiblecatalog.ncip.v2.aleph.AlephXServices.AlephMediator;
import org.extensiblecatalog.ncip.v2.aleph.item.AlephItem;
import org.extensiblecatalog.ncip.v2.aleph.item.AlephRenewItem;
import org.extensiblecatalog.ncip.v2.aleph.item.AlephRequestItem;
import org.extensiblecatalog.ncip.v2.aleph.user.AlephRestDlfUser;
import org.extensiblecatalog.ncip.v2.aleph.util.SAXHandlers.AlephDoRequestHandler;
import org.extensiblecatalog.ncip.v2.aleph.util.SAXHandlers.AlephItemHandler;
import org.extensiblecatalog.ncip.v2.aleph.util.SAXHandlers.AlephLoanHandler;
import org.extensiblecatalog.ncip.v2.aleph.util.SAXHandlers.AlephLookupRequestHandler;
import org.extensiblecatalog.ncip.v2.aleph.util.SAXHandlers.AlephLookupRequestsHandler;
import org.extensiblecatalog.ncip.v2.aleph.util.SAXHandlers.AlephLookupUserHandler;
import org.extensiblecatalog.ncip.v2.aleph.util.SAXHandlers.AlephRenewHandler;
import org.extensiblecatalog.ncip.v2.aleph.util.SAXHandlers.AlephURLsHandler;
import org.extensiblecatalog.ncip.v2.aleph.util.SAXHandlers.AlephUpdateUserHandler;
import org.extensiblecatalog.ncip.v2.common.ConnectorConfigurationFactory;
import org.extensiblecatalog.ncip.v2.common.DefaultConnectorConfiguration;
import org.extensiblecatalog.ncip.v2.service.AddUserFields;
import org.extensiblecatalog.ncip.v2.service.AgencyAddressInformation;
import org.extensiblecatalog.ncip.v2.service.AgencyAddressRoleType;
import org.extensiblecatalog.ncip.v2.service.CancelRequestItemInitiationData;
import org.extensiblecatalog.ncip.v2.service.DeleteUserFields;
import org.extensiblecatalog.ncip.v2.service.ElectronicAddress;
import org.extensiblecatalog.ncip.v2.service.ItemId;
import org.extensiblecatalog.ncip.v2.service.LookupItemInitiationData;
import org.extensiblecatalog.ncip.v2.service.LookupItemSetInitiationData;
import org.extensiblecatalog.ncip.v2.service.LookupRequestInitiationData;
import org.extensiblecatalog.ncip.v2.service.LookupUserInitiationData;
import org.extensiblecatalog.ncip.v2.service.OrganizationNameInformation;
import org.extensiblecatalog.ncip.v2.service.PhysicalAddress;
import org.extensiblecatalog.ncip.v2.service.Problem;
import org.extensiblecatalog.ncip.v2.service.ProblemType;
import org.extensiblecatalog.ncip.v2.service.RenewItemInitiationData;
import org.extensiblecatalog.ncip.v2.service.RequestItemInitiationData;
import org.extensiblecatalog.ncip.v2.service.RequestedItem;
import org.extensiblecatalog.ncip.v2.service.ServiceError;
import org.extensiblecatalog.ncip.v2.service.ServiceException;
import org.extensiblecatalog.ncip.v2.service.ToolkitException;
import org.extensiblecatalog.ncip.v2.service.UnstructuredAddress;
import org.extensiblecatalog.ncip.v2.service.UpdateUserInitiationData;
import org.extensiblecatalog.ncip.v2.service.UserAddressInformation;
import org.extensiblecatalog.ncip.v2.service.Version1AgencyAddressRoleType;
import org.extensiblecatalog.ncip.v2.service.Version1ElectronicAddressType;
import org.extensiblecatalog.ncip.v2.service.Version1OrganizationNameType;
import org.extensiblecatalog.ncip.v2.service.Version1PhysicalAddressType;
import org.extensiblecatalog.ncip.v2.service.Version1UnstructuredAddressType;
import org.springframework.util.StringUtils;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.jamesmurty.utils.XMLBuilder;

public class RestDlfConnector extends AlephMediator {

	private static final long serialVersionUID = -4425639616999642735L;

	private static SAXParser parser;

	private Random random = new Random();

	public RestDlfConnector() throws ServiceException {

		try {
			parser = SAXParserFactory.newInstance().newSAXParser();
		} catch (ParserConfigurationException e1) {
			throw new ServiceException(ServiceError.RUNTIME_ERROR, "Failed to initialize SAX Parser from SAXParserFactory.");
		} catch (SAXException e1) {
			throw new ServiceException(ServiceError.RUNTIME_ERROR, "Failed to initialize SAX Parser from SAXParserFactory.");
		}
		try {
			DefaultConnectorConfiguration config = (DefaultConnectorConfiguration) new ConnectorConfigurationFactory(new Properties()).getConfiguration();
			AlephConfiguration alephConfig = new AlephConfiguration(config);

			LocalConfig.setServerName(alephConfig.getProperty(AlephConstants.REST_DLF_SERVER));
			LocalConfig.setServerPort(alephConfig.getProperty(AlephConstants.REST_DLF_PORT));
			LocalConfig.setServerSuffix(alephConfig.getProperty(AlephConstants.REST_DLF_SUFFIX));

			LocalConfig.setBibLibrary(alephConfig.getProperty(AlephConstants.BIBLIOGRAPHIC_LIBRARY));
			LocalConfig.setAdmLibrary(alephConfig.getProperty(AlephConstants.ALEPH_ADMINISTRATIVE_LIBRARY));

			LocalConfig.setDefaultAgency(alephConfig.getProperty(AlephConstants.DEFAULT_AGENCY));

			LocalConfig.setAgencyAddress(alephConfig.getProperty(AlephConstants.AGENCY_UNSTRUCTURED_ADDRESS));
			LocalConfig.setAgencyName(alephConfig.getProperty(AlephConstants.AGENCY_TRANSLATED_NAME));

			LocalConfig.setUserRegistrationLink(alephConfig.getProperty(AlephConstants.USER_REGISTRATION_LINK));
			LocalConfig.setAuthDataFormatType(alephConfig.getProperty(AlephConstants.AUTH_DATA_FORMAT_TYPE));

			LocalConfig.setTokenExpirationTime(Integer.parseInt(alephConfig.getProperty(AlephConstants.NEXT_ITEM_TOKEN_EXPIRATION_TIME)));

			LocalConfig.setEchoParticularProblemsToLUIS(Boolean.parseBoolean(alephConfig.getProperty(AlephConstants.INCLUDE_PARTICULAR_PROBLEMS_TO_LUIS)));

			LocalConfig.setUserPostalAndCityStoredIn(alephConfig.getProperty(AlephConstants.PATRON_ADDRESS_MAPPING_POSTAL_AND_CITY));
			LocalConfig.setUserIdCardStoredIn(alephConfig.getProperty(AlephConstants.PATRON_ADDRESS_MAPPING_IDENTITY_CARD));
			LocalConfig.setUserPhoneStoredIn(alephConfig.getProperty(AlephConstants.PATRON_ADDRESS_MAPPING_PHONE));
			LocalConfig.setUserStreetStoredIn(alephConfig.getProperty(AlephConstants.PATRON_ADDRESS_MAPPING_STREET));

			try {
				LocalConfig.setMaxItemPreparationTimeDelay(Integer.parseInt(alephConfig.getProperty(AlephConstants.MAX_ITEM_PREPARATION_TIME_DELAY)));
			} catch (Exception e) {
				e.printStackTrace();
			}

		} catch (ToolkitException e) {
			throw new ServiceException(ServiceError.CONFIGURATION_ERROR, "Toolkit configuration failed.");
		}

	}

	public AlephItem lookupItem(String id, LookupItemSetInitiationData initData) throws ParserConfigurationException, IOException, SAXException, AlephException {
		LookupItemInitiationData LIinitData = new LookupItemInitiationData();
		ItemId itemId = new ItemId();
		itemId.setItemIdentifierValue(id);
		LIinitData.setItemId(itemId);
		LIinitData.setBibliographicDescriptionDesired(initData.getBibliographicDescriptionDesired());
		LIinitData.setCirculationStatusDesired(initData.getCirculationStatusDesired());
		LIinitData.setHoldQueueLengthDesired(initData.getHoldQueueLengthDesired());
		LIinitData.setItemDescriptionDesired(initData.getItemDescriptionDesired());
		LIinitData.setItemUseRestrictionTypeDesired(initData.getItemUseRestrictionTypeDesired());
		LIinitData.setLocationDesired(initData.getLocationDesired());
		return lookupItem(LIinitData);
	}

	public AlephItem lookupItem(LookupItemInitiationData initData) throws ParserConfigurationException, IOException, SAXException, AlephException {

		String alephItemId = initData.getItemId().getItemIdentifierValue();

		String recordId = AlephUtil.parseRecordIdFromAlephItemId(alephItemId);
		String itemId = AlephUtil.parseItemIdFromAlephItemId(alephItemId);

		boolean isCorrectRecordId = AlephUtil.isCorrectRecordId(recordId, LocalConfig.getBibLibraryLength());
		boolean isCorrectItemId = AlephUtil.isCorrectItemId(itemId, LocalConfig.getBibLibraryLength());

		if (!isCorrectRecordId || !isCorrectItemId) {
			throw new AlephException(AlephException.INVALID_ITEM_IDENTIFIER_VALUE_FORMAT,
					"Item Id is accepted only in strict format with strict length. e.g. MZK01001276830-MZK50001311815000020");
		}

		String appProfileType = null;
		if (initData.getInitiationHeader() != null && initData.getInitiationHeader().getApplicationProfileType() != null)
			appProfileType = initData.getInitiationHeader().getApplicationProfileType().getValue();

		String lang = (appProfileType == null || appProfileType.isEmpty()) ? "en" : appProfileType;

		URL url = new URLBuilder().setBase(LocalConfig.getServerName(), LocalConfig.getServerPort())
				.setPath(LocalConfig.getServerSuffix(), AlephConstants.ITEM_PATH_ELEMENT, recordId, AlephConstants.PARAM_ITEMS, itemId).addRequest("lang", lang).toURL();

		InputSource streamSource = new InputSource(url.openStream());

		AlephItemHandler itemHandler = new AlephItemHandler(initData);

		parser.parse(streamSource, itemHandler);

		return itemHandler.getCurrentAlephItem();
	}

	public List<AlephItem> lookupItems(String id, LookupItemSetInitiationData luisInitData, AlephLookupItemSetService service) throws ParserConfigurationException, IOException,
			SAXException, AlephException {
		LookupItemInitiationData lookupItemInitData = new LookupItemInitiationData();
		lookupItemInitData.setBibliographicDescriptionDesired(luisInitData.getBibliographicDescriptionDesired());
		lookupItemInitData.setCirculationStatusDesired(luisInitData.getCirculationStatusDesired());
		lookupItemInitData.setHoldQueueLengthDesired(luisInitData.getHoldQueueLengthDesired());
		lookupItemInitData.setItemDescriptionDesired(luisInitData.getItemDescriptionDesired());
		lookupItemInitData.setItemUseRestrictionTypeDesired(luisInitData.getItemUseRestrictionTypeDesired());
		lookupItemInitData.setLocationDesired(luisInitData.getLocationDesired());
		lookupItemInitData.setInitiationHeader(luisInitData.getInitiationHeader());
		return lookupItems(id, lookupItemInitData, service);
	}

	public List<AlephItem> lookupItems(String recordId, LookupItemInitiationData lookupItemInitData, AlephLookupItemSetService service) throws ParserConfigurationException,
			IOException, SAXException, AlephException {

		boolean isCorrectRecordId = AlephUtil.isCorrectRecordId(recordId, LocalConfig.getBibLibraryLength());

		if (!isCorrectRecordId) {
			throw new AlephException(AlephException.INVALID_ITEM_IDENTIFIER_VALUE_FORMAT, "Record Id is accepted only in strict format with strict length. e.g. MZK01000000421");
		}

		// AppProfileType is currently used for language specification
		String appProfileType = null;
		if (lookupItemInitData.getInitiationHeader() != null && lookupItemInitData.getInitiationHeader().getApplicationProfileType() != null)
			appProfileType = lookupItemInitData.getInitiationHeader().getApplicationProfileType().getValue();

		String lang = (appProfileType == null || appProfileType.isEmpty()) ? "en" : appProfileType;

		URL url;
		InputSource streamSource;

		AlephItemHandler itemHandler = new AlephItemHandler(lookupItemInitData);

		// If there is maximumItemsCount set, then parse only URLs in range of maxItemsCount
		// Else parse all at once with "view=full" GET request

		if (service.getMaximumItemsCount() != 0) {

			ItemToken nextItemToken = service.getNextItemToken();

			int startFrom = (nextItemToken != null && nextItemToken.getBibliographicId().equalsIgnoreCase(recordId)) ? nextItemToken.getNoOfDoneItemIds() : 0;
			int maxLinks = service.getMaximumItemsCount() - service.getItemsForwarded();

			AlephURLsHandler urlsHandler = new AlephURLsHandler(startFrom, maxLinks);

			url = new URLBuilder().setBase(LocalConfig.getServerName(), LocalConfig.getServerPort())
					.setPath(LocalConfig.getServerSuffix(), AlephConstants.ITEM_PATH_ELEMENT, recordId, AlephConstants.PARAM_ITEMS).toURL();

			streamSource = new InputSource(url.openStream());

			parser.parse(streamSource, urlsHandler);

			if (urlsHandler.haveParsedMaxLinks()) {
				// Just create NextItemToken & continue

				// It is important to let token create even if the bibId is last of all desired bibIds in case of not all children items can be forwarded due to maximumItemsCount

				// Do not create NextItemToken if this is last item desired
				boolean createNewItemToken = !urlsHandler.haveParsedAll() || urlsHandler.haveParsedAll() && !service.isLastDesiredId();

				if (createNewItemToken) {
					// Set next item token
					ItemToken itemToken = new ItemToken();

					itemToken.setBibliographicId(recordId);

					itemToken.setIsRecordId(true);

					if (urlsHandler.haveParsedAll())
						itemToken.setDoneWithRecordId(true);
					else
						itemToken.setNoOfDoneItemIds(urlsHandler.getNextLinkIndex());

					String tokenKey = Integer.toString(random.nextInt());

					itemToken.setNextToken(tokenKey);

					service.addItemToken(tokenKey, itemToken);

					service.setNewTokenKey(tokenKey);

				}
			}

			service.setItemsForwarded(service.getItemsForwarded() + urlsHandler.getLinks().size());

			BigDecimal totalNumberOfPieces = new BigDecimal(urlsHandler.getTotalItemsCount());

			for (String link : urlsHandler.getLinks()) {

				url = new URLBuilder().parseLink(link).addRequest("lang", lang).toURL();

				streamSource = new InputSource(url.openStream());

				parser.parse(streamSource, itemHandler);

				itemHandler.getCurrentAlephItem().setNumberOfPieces(totalNumberOfPieces);

				// Now because handler doesn't know URL it's parsing, we need to set itemIds manually
				String[] linkParts = link.split("/");
				itemHandler.getCurrentAlephItem().setItemId(linkParts[5] + AlephConstants.UNIQUE_ITEM_ID_SEPARATOR + linkParts[7]);
			}

		} else {

			url = new URLBuilder().setBase(LocalConfig.getServerName(), LocalConfig.getServerPort())
					.setPath(LocalConfig.getServerSuffix(), AlephConstants.ITEM_PATH_ELEMENT, recordId, AlephConstants.PARAM_ITEMS).addRequest("view", "full")
					.addRequest("lang", lang).toURL();

			streamSource = new InputSource(url.openStream());

			parser.parse(streamSource, itemHandler);
		}
		return itemHandler.getListOfItems();

	}

	/**
	 * @param patronId
	 * @param {@link LookupUserInitiationData} initData
	 * @return {@link AlephRestDlfUser}
	 * @throws AlephException
	 * @throws IOException
	 * @throws SAXException
	 */
	public AlephRestDlfUser lookupUser(String patronId, LookupUserInitiationData initData) throws AlephException, IOException, SAXException {

		boolean blockOrTrapDesired = initData.getBlockOrTrapDesired();
		boolean loanedItemsDesired = initData.getLoanedItemsDesired();
		boolean requestedItemsDesired = initData.getRequestedItemsDesired();
		boolean userFiscalAccountDesired = initData.getUserFiscalAccountDesired();
		boolean userPrivilegeDesired = initData.getUserPrivilegeDesired();

		boolean personalInfoDesired = initData.getNameInformationDesired() || initData.getUserIdDesired() || initData.getUserAddressInformationDesired();

		// If there are only loanedItems desired, that means to include loans history to output
		// Better suggestions how to forward loaned items history, please send to kozlovsky@mzk.cz
		boolean loanedItemsDesiredOnly = loanedItemsDesired && !(blockOrTrapDesired || requestedItemsDesired || userFiscalAccountDesired || personalInfoDesired);

		String appProfileType = null;
		if (initData.getInitiationHeader() != null && initData.getInitiationHeader().getApplicationProfileType() != null)
			appProfileType = initData.getInitiationHeader().getApplicationProfileType().getValue();

		String lang = (appProfileType == null || appProfileType.isEmpty()) ? "en" : appProfileType;

		// Create URL request only if specified service was desired
		URL addressUrl = null;
		if (personalInfoDesired) {
			addressUrl = new URLBuilder().setBase(LocalConfig.getServerName(), LocalConfig.getServerPort())
					.setPath(LocalConfig.getServerSuffix(), AlephConstants.USER_PATH_ELEMENT, patronId, AlephConstants.PARAM_PATRON_INFO, AlephConstants.PARAM_ADDRESS).toURL();
		}

		URL circulationsUrl = null;
		URL cashUrl = null;
		if (userFiscalAccountDesired) {
			circulationsUrl = new URLBuilder().setBase(LocalConfig.getServerName(), LocalConfig.getServerPort())
					.setPath(LocalConfig.getServerSuffix(), AlephConstants.USER_PATH_ELEMENT, patronId, AlephConstants.PARAM_CIRC_ACTIONS).toURL();
			cashUrl = new URLBuilder().setBase(LocalConfig.getServerName(), LocalConfig.getServerPort())
					.setPath(LocalConfig.getServerSuffix(), AlephConstants.USER_PATH_ELEMENT, patronId, AlephConstants.PARAM_CIRC_ACTIONS, "cash").addRequest("view", "full")
					.toURL();
		}

		URL loansUrl = null;
		URL loansHistoryUrl = null;
		if (loanedItemsDesired) {
			if (!loanedItemsDesiredOnly) {
				// If there is not history expected, parse regular loans
				loansUrl = new URLBuilder().setBase(LocalConfig.getServerName(), LocalConfig.getServerPort())
						.setPath(LocalConfig.getServerSuffix(), AlephConstants.USER_PATH_ELEMENT, patronId, AlephConstants.PARAM_CIRC_ACTIONS, AlephConstants.PARAM_LOANS)
						.addRequest("view", "full").addRequest("lang", lang).toURL();
			} else {
				loansHistoryUrl = new URLBuilder().setBase(LocalConfig.getServerName(), LocalConfig.getServerPort())
						.setPath(LocalConfig.getServerSuffix(), AlephConstants.USER_PATH_ELEMENT, patronId, AlephConstants.PARAM_CIRC_ACTIONS, AlephConstants.PARAM_LOANS)
						.addRequest("view", "full").addRequest("type", "history").addRequest("lang", lang).toURL();
			}
		}

		URL requestsUrl = null;
		if (requestedItemsDesired) {
			// We suppose desired requests are at http://aleph.mzk.cz:1892/rest-dlf/patron/930118BXGO/circulationActions/requests/holds?view=full
			requestsUrl = new URLBuilder()
					.setBase(LocalConfig.getServerName(), LocalConfig.getServerPort())
					.setPath(LocalConfig.getServerSuffix(), AlephConstants.USER_PATH_ELEMENT, patronId, AlephConstants.PARAM_CIRC_ACTIONS, AlephConstants.PARAM_REQUESTS,
							AlephConstants.PARAM_HOLDS).addRequest("view", "full").addRequest("lang", lang).toURL();
		}

		URL blocksOrTrapsUrl = null;
		if (blockOrTrapDesired) {
			blocksOrTrapsUrl = new URLBuilder().setBase(LocalConfig.getServerName(), LocalConfig.getServerPort())
					.setPath(LocalConfig.getServerSuffix(), AlephConstants.USER_PATH_ELEMENT, patronId, AlephConstants.PARAM_PATRON_STATUS, AlephConstants.PARAM_BLOCKS).toURL();
		}

		URL registrationUrl = null;
		if (userPrivilegeDesired) {
			registrationUrl = new URLBuilder().setBase(LocalConfig.getServerName(), LocalConfig.getServerPort())
					.setPath(LocalConfig.getServerSuffix(), AlephConstants.USER_PATH_ELEMENT, patronId, AlephConstants.PARAM_PATRON_STATUS, AlephConstants.PARAM_REGISTRATION)
					.toURL();
		}

		AlephLookupUserHandler userHandler = new AlephLookupUserHandler(initData);

		InputSource streamSource;

		if (loansUrl != null || loansHistoryUrl != null) {

			AlephLoanHandler loanHandler = new AlephLoanHandler();

			if (appProfileType != null && !appProfileType.isEmpty())
				loanHandler.setLocalizationDesired(true);

			if (loansUrl != null) {
				streamSource = new InputSource(loansUrl.openStream());
			} else
				streamSource = new InputSource(loansHistoryUrl.openStream());

			parser.parse(streamSource, loanHandler);
			userHandler.getAlephUser().setLoanedItems(loanHandler.getListOfLoanedItems());

		}

		if (requestsUrl != null) {
			AlephLookupRequestsHandler requestItemHandler = new AlephLookupRequestsHandler();

			if (appProfileType != null && !appProfileType.isEmpty())
				requestItemHandler.setLocalizationDesired(true);

			streamSource = new InputSource(requestsUrl.openStream());

			// Here parser parses all available info saveable into RequestItem class
			parser.parse(streamSource, requestItemHandler);

			List<RequestedItem> requestedItems = requestItemHandler.getRequestedItems();

			if (requestedItems.size() > 0) {

				// If there is set default item preparation delay, set it to all requested items
				if (LocalConfig.getMaxItemPreparationTimeDelay() != 0)
					for (RequestedItem requestedItem : requestedItems) {
						if (requestedItem.getDatePlaced() != null) {
							// Because Aleph does not support default delay between pickupDate and datePlaced, we will use custom configuration to set it
							GregorianCalendar pickupDate = (GregorianCalendar) requestedItem.getDatePlaced().clone();
							pickupDate.add(Calendar.DAY_OF_MONTH, LocalConfig.getMaxItemPreparationTimeDelay());
							requestedItem.setPickupDate(pickupDate);
						}
					}

				userHandler.getAlephUser().setRequestedItems(requestedItems);
			}
		}

		if (addressUrl != null) {
			streamSource = new InputSource(addressUrl.openStream());

			parser.parse(streamSource, userHandler.parseAddress());
		}
		if (cashUrl != null) {
			streamSource = new InputSource(cashUrl.openStream());

			parser.parse(streamSource, userHandler.parseCash());
		}
		if (circulationsUrl != null) {
			streamSource = new InputSource(circulationsUrl.openStream());

			parser.parse(streamSource, userHandler.parseCirculations());
		}
		if (blocksOrTrapsUrl != null) {
			streamSource = new InputSource(blocksOrTrapsUrl.openStream());

			parser.parse(streamSource, userHandler.parseBlockOrTraps());
		}
		if (registrationUrl != null) {
			streamSource = new InputSource(registrationUrl.openStream());

			parser.parse(streamSource, userHandler.parseRegistration());
		}

		return userHandler.getAlephUser();
	}

	public AlephRequestItem lookupRequest(LookupRequestInitiationData initData) throws AlephException, IOException, SAXException, ParserConfigurationException, ServiceException {

		String alephItemId = initData.getItemId().getItemIdentifierValue();

		String recordId = AlephUtil.parseRecordIdFromAlephItemId(alephItemId);
		String itemId = AlephUtil.parseItemIdFromAlephItemId(alephItemId);

		boolean isCorrectRecordId = AlephUtil.isCorrectRecordId(recordId, LocalConfig.getBibLibraryLength());

		if (!isCorrectRecordId) {
			throw new AlephException(AlephException.INVALID_ITEM_IDENTIFIER_VALUE_FORMAT,
					"Item Id is accepted only in strict format with strict length. e.g. MZK01001276830-MZK50001311815000020");
		}

		String patronId = initData.getUserId().getUserIdentifierValue();

		String appProfileType = null;
		if (initData.getInitiationHeader() != null && initData.getInitiationHeader().getApplicationProfileType() != null)
			appProfileType = initData.getInitiationHeader().getApplicationProfileType().getValue();

		String lang = (appProfileType == null || appProfileType.isEmpty()) ? "en" : appProfileType;

		URL holdsUrl = new URLBuilder()
				.setBase(LocalConfig.getServerName(), LocalConfig.getServerPort())
				.setPath(LocalConfig.getServerSuffix(), AlephConstants.USER_PATH_ELEMENT, patronId, AlephConstants.PARAM_CIRC_ACTIONS, AlephConstants.PARAM_REQUESTS,
						AlephConstants.PARAM_HOLDS).addRequest("lang", lang).toURL();

		AlephLookupRequestHandler requestHandler = new AlephLookupRequestHandler(initData, itemId);
		InputSource streamSource = new InputSource(holdsUrl.openStream());

		// Here parser finds requested request's link if any
		parser.parse(streamSource, requestHandler);

		if (requestHandler.requestWasFound() && requestHandler.getRequestLink() != null) {

			URL requestLink = new URL(requestHandler.getRequestLink());

			streamSource = new InputSource(requestLink.openStream());

			// Here parser parses info pasteable into LookupRequestResponseData
			parser.parse(streamSource, requestHandler);

			RequestDetails requestDetails = requestHandler.getAlephRequestItem().getRequestDetails();

			if (LocalConfig.getMaxItemPreparationTimeDelay() != 0 && requestDetails.getDatePlaced() != null) {
				GregorianCalendar pickupDate = (GregorianCalendar) requestDetails.getDatePlaced().clone();
				pickupDate.add(Calendar.DAY_OF_MONTH, LocalConfig.getMaxItemPreparationTimeDelay());
				requestDetails.setPickupDate(pickupDate);
			}

			boolean nameInformationDesired = initData.getNameInformationDesired();
			boolean userAddressInformationDesired = initData.getUserAddressInformationDesired();
			boolean userIdDesired = initData.getUserIdDesired();
			boolean userPrivilegeDesired = initData.getUserPrivilegeDesired();
			boolean blockOrTrapDesired = initData.getBlockOrTrapDesired();

			if (nameInformationDesired || userAddressInformationDesired || userIdDesired || userPrivilegeDesired || blockOrTrapDesired) {
				LookupUserInitiationData userInitData = new LookupUserInitiationData();
				userInitData.setNameInformationDesired(nameInformationDesired);
				userInitData.setUserAddressInformationDesired(userAddressInformationDesired);
				userInitData.setUserIdDesired(userIdDesired);
				userInitData.setUserPrivilegeDesired(userPrivilegeDesired);
				userInitData.setBlockOrTrapDesired(blockOrTrapDesired);

				userInitData.setInitiationHeader(initData.getInitiationHeader());

				AlephRestDlfUser user = lookupUser(patronId, userInitData);
				requestHandler.getAlephRequestItem().setUserOptionalFields(user.getUserOptionalFields());
			}

		} else {
			Problem problem = new Problem();
			problem.setProblemType(new ProblemType("Request does not exist."));
			requestHandler.getAlephRequestItem().setProblem(problem);
		}

		return requestHandler.getAlephRequestItem();
	}

	public AlephRequestItem requestItem(RequestItemInitiationData initData) throws AlephException, IOException, SAXException, ParserConfigurationException {

		List<ItemId> itemIds = initData.getItemIds();
		String patronId = initData.getUserId().getUserIdentifierValue();

		AlephRequestItem requestItem = new AlephRequestItem();
		String alephItemId;

		for (ItemId itemId : itemIds) {
			alephItemId = itemId.getItemIdentifierValue();

			if (!alephItemId.isEmpty()) {
				String recordId = AlephUtil.parseRecordIdFromAlephItemId(alephItemId);
				String itemIdVal = AlephUtil.parseItemIdFromAlephItemId(alephItemId);

				boolean isCorrectRecordId = AlephUtil.isCorrectRecordId(recordId, LocalConfig.getBibLibraryLength());
				boolean isCorrectItemId = AlephUtil.isCorrectItemId(itemIdVal, LocalConfig.getBibLibraryLength());

				if (!isCorrectRecordId || !isCorrectItemId) {
					throw new AlephException(AlephException.INVALID_ITEM_IDENTIFIER_VALUE_FORMAT,
							"Item Id is accepted only in strict format with strict length. e.g. MZK01001276830-MZK50001311815000020");
				}

				String pickupLocation = null;
				if (initData.getPickupLocation() != null)
					pickupLocation = initData.getPickupLocation().getValue();

				String needBeforeDate = AlephUtil.convertToAlephDate(initData.getNeedBeforeDate());
				String earliestDateNeeded = AlephUtil.convertToAlephDate(initData.getEarliestDateNeeded());

				// We need to put HTML PUT request to e.g. http://aleph.mzk.cz:1892/rest-dlf/patron/700/record/MZK01001330134/holds/MZK50001365071000010
				URL holdUrl = new URLBuilder()
						.setBase(LocalConfig.getServerName(), LocalConfig.getServerPort())
						.setPath(LocalConfig.getServerSuffix(), AlephConstants.USER_PATH_ELEMENT, patronId, AlephConstants.PARAM_RECORD, recordId, AlephConstants.PARAM_HOLDS,
								itemIdVal).toURL();

				String firstNote = null;
				// Our own testing identifier system (Moravian Library's)
				if (patronId.equalsIgnoreCase("700") || true) {
					firstNote = "dg";
				}

				String XMLRequest = new RequestItemXMLBuilder().setParent("hold-request-parameters").setPickupLocation(pickupLocation).setLastInterestDate(needBeforeDate)
						.setStartInterestDate(earliestDateNeeded).setFirstNote(firstNote).setRush("N").toString();

				InputSource streamSource;
				HttpURLConnection httpCon = (HttpURLConnection) holdUrl.openConnection();
				httpCon.setDoOutput(true);
				httpCon.setRequestMethod("PUT");

				OutputStreamWriter outWriter = new OutputStreamWriter(httpCon.getOutputStream());
				outWriter.write(XMLRequest);
				outWriter.close();

				streamSource = new InputSource(httpCon.getInputStream());

				AlephDoRequestHandler requestItemHandler = new AlephDoRequestHandler(itemIdVal);

				// Here parser detects error if any
				parser.parse(streamSource, requestItemHandler);

				if (!requestItemHandler.returnedError()) {

					// Parse sequence number
					URL holdsUrl = new URLBuilder()
							.setBase(LocalConfig.getServerName(), LocalConfig.getServerPort())
							.setPath(LocalConfig.getServerSuffix(), AlephConstants.USER_PATH_ELEMENT, patronId, AlephConstants.PARAM_CIRC_ACTIONS, AlephConstants.PARAM_REQUESTS,
									AlephConstants.PARAM_HOLDS).toURL();

					streamSource = new InputSource(holdsUrl.openStream());

					// Here parser parses the newly created requestLink
					parser.parse(streamSource, requestItemHandler);

					if (requestItemHandler.requestWasFound()) {

						URL requestUrl = new URL(requestItemHandler.getLink());

						streamSource = new InputSource(requestUrl.openStream());

						// Here parser parses RequestId
						parser.parse(streamSource, requestItemHandler);

						requestItem.addRequestId(requestItemHandler.getRequestId());
					}

				} else if (itemIds.size() > 1) {
					// If there is more than one item requested, than it is a good habit to let user know of which items could not been requested and why
					String noteValue = requestItemHandler.getNoteValue();

					if (noteValue == null)
						noteValue = "";
					else
						noteValue = " - " + requestItemHandler.getNoteValue();

					requestItem.addRequestId(requestItemHandler.getReplyText() + noteValue);
					// Output of this will usually be: Failed to create request - Patron has already requested this item.
				} else {
					Problem problem = new Problem();

					problem.setProblemValue(requestItemHandler.getReplyText());

					if (requestItemHandler.getNoteValue() != null)
						problem.setProblemDetail(requestItemHandler.getNoteValue() + " (" + alephItemId + ")");

					problem.setProblemType(new ProblemType("Aleph returned error while processing the request. See details below."));
					requestItem.setProblem(problem);
				}
			} else if (itemIds.size() > 1) {
				// If there is more than one item requested, than it is a good habit to let user know of which items could not been requested and why
				requestItem.addRequestId("Failed to create request - Empty ItemIdentifierValue was specified here.");
			} else {
				Problem problem = new Problem();
				problem.setProblemDetail("Cannot request unknown item.");
				problem.setProblemType(new ProblemType("You have sent empty ItemIdentifierValue."));
				requestItem.setProblem(problem);
			}
		}

		return requestItem;
	}

	public AlephRequestItem cancelRequestItem(CancelRequestItemInitiationData initData) throws AlephException, IOException, SAXException, ParserConfigurationException {

		String alephItemId = initData.getItemId().getItemIdentifierValue();

		String recordId = AlephUtil.parseRecordIdFromAlephItemId(alephItemId);
		String itemId = AlephUtil.parseItemIdFromAlephItemId(alephItemId);

		boolean isCorrectRecordId = AlephUtil.isCorrectRecordId(recordId, LocalConfig.getBibLibraryLength());
		boolean isCorrectItemId = AlephUtil.isCorrectItemId(itemId, LocalConfig.getBibLibraryLength());

		if (!isCorrectRecordId || !isCorrectItemId) {
			throw new AlephException(AlephException.INVALID_ITEM_IDENTIFIER_VALUE_FORMAT,
					"Item Id is accepted only in strict format with strict length. e.g. MZK01001276830-MZK50001311815000020");
		}

		AlephRequestItem requestItem = new AlephRequestItem();

		String patronId = initData.getUserId().getUserIdentifierValue();

		// Parse sequence number
		URL holdsUrl = new URLBuilder()
				.setBase(LocalConfig.getServerName(), LocalConfig.getServerPort())
				.setPath(LocalConfig.getServerSuffix(), AlephConstants.USER_PATH_ELEMENT, patronId, AlephConstants.PARAM_CIRC_ACTIONS, AlephConstants.PARAM_REQUESTS,
						AlephConstants.PARAM_HOLDS).toURL();

		AlephDoRequestHandler requestItemHandler = new AlephDoRequestHandler(itemId);

		InputSource streamSource = new InputSource(holdsUrl.openStream());

		// Here parser finds request, detects if delete="Y"
		parser.parse(streamSource, requestItemHandler);

		if (requestItemHandler.requestWasFound() && requestItemHandler.isDeletable()) {

			URL holdRequestUrl = new URL(requestItemHandler.getLink());

			HttpURLConnection httpCon = (HttpURLConnection) holdRequestUrl.openConnection();
			httpCon.setDoOutput(true);
			httpCon.setRequestMethod("DELETE");

			streamSource = new InputSource(httpCon.getInputStream());

			// Here parser gets error if any
			parser.parse(streamSource, requestItemHandler);

			if (requestItemHandler.returnedError()) {
				Problem problem = new Problem();
				problem.setProblemValue(requestItemHandler.getReplyText());
				problem.setProblemType(new ProblemType("Aleph returned error while processing cancel request. See details below."));
				requestItem.setProblem(problem);
			}
		} else if (requestItemHandler.requestWasFound()) {
			Problem problem = new Problem();
			problem.setProblemValue("Delete value of specified hold request is set to \"N\"");
			problem.setProblemType(new ProblemType("Requested deletion is not allowed."));
			requestItem.setProblem(problem);
		} else {
			Problem problem = new Problem();
			problem.setProblemType(new ProblemType("Request does not exist."));
			requestItem.setProblem(problem);
		}
		return requestItem;
	}

	public AlephRenewItem renewItem(RenewItemInitiationData initData) throws AlephException, IOException, SAXException, ParserConfigurationException {

		String alephLoanId = initData.getItemId().getItemIdentifierValue();

		boolean isCorrectLoanId = AlephUtil.isCorrectLoanId(alephLoanId, LocalConfig.getBibLibraryLength());

		if (!isCorrectLoanId) {
			throw new AlephException(AlephException.INVALID_ITEM_IDENTIFIER_VALUE_FORMAT, "Loan Id is accepted only in strict format with strict length. e.g. MZK50004929137");
		}

		AlephRenewItem renewItem = new AlephRenewItem();

		String patronId = initData.getUserId().getUserIdentifierValue();

		URL loansUrl = new URLBuilder().setBase(LocalConfig.getServerName(), LocalConfig.getServerPort())
				.setPath(LocalConfig.getServerSuffix(), AlephConstants.USER_PATH_ELEMENT, patronId, AlephConstants.PARAM_CIRC_ACTIONS, AlephConstants.PARAM_LOANS).toURL();

		AlephRenewHandler renewHandler = new AlephRenewHandler(alephLoanId, renewItem);

		InputSource streamSource = new InputSource(loansUrl.openStream());

		// Here parser looks up our loan about to be renewed
		parser.parse(streamSource, renewHandler);

		if (renewHandler.loanWasFound() && renewHandler.isRenewable()) {

			URL loanLink = new URL(renewHandler.getLoanLink());

			String XMLRequest = AlephUtil.buildRenewPOSTXml(initData.getDesiredDateDue());

			HttpURLConnection httpCon = (HttpURLConnection) loanLink.openConnection();
			httpCon.setDoOutput(true);
			httpCon.setRequestMethod("POST");

			OutputStreamWriter outWriter = new OutputStreamWriter(httpCon.getOutputStream());
			outWriter.write(XMLRequest);
			outWriter.close();

			streamSource = new InputSource(httpCon.getInputStream());

			// Here parser finds out whether was renew successful or not & why
			parser.parse(streamSource, renewHandler);

			if (renewHandler.actionSucceeded()) {

				boolean nameInformationDesired = initData.getNameInformationDesired();
				boolean userAddressInformationDesired = initData.getUserAddressInformationDesired();
				boolean userIdDesired = initData.getUserIdDesired();
				boolean userPrivilegeDesired = initData.getUserPrivilegeDesired();
				boolean blockOrTrapDesired = initData.getBlockOrTrapDesired();

				if (nameInformationDesired || userAddressInformationDesired || userIdDesired || userPrivilegeDesired || blockOrTrapDesired) {
					LookupUserInitiationData userInitData = new LookupUserInitiationData();
					userInitData.setNameInformationDesired(nameInformationDesired);
					userInitData.setUserAddressInformationDesired(userAddressInformationDesired);
					userInitData.setUserIdDesired(userIdDesired);
					userInitData.setUserPrivilegeDesired(userPrivilegeDesired);
					userInitData.setBlockOrTrapDesired(blockOrTrapDesired);

					userInitData.setInitiationHeader(initData.getInitiationHeader());

					AlephRestDlfUser user = lookupUser(patronId, userInitData);
					renewItem.setUserOptionalFields(user.getUserOptionalFields());
				}

				boolean getBibDescription = initData.getBibliographicDescriptionDesired();
				boolean getCircStatus = initData.getCirculationStatusDesired();
				boolean getHoldQueueLength = initData.getHoldQueueLengthDesired();
				boolean getItemDescription = initData.getItemDescriptionDesired();
				boolean getLocation = initData.getLocationDesired();

				if (getBibDescription || getCircStatus || getHoldQueueLength || getItemDescription || getLocation) {

					streamSource = new InputSource(loanLink.openStream());

					// Here the parser looks for itemDocNo, bibDocNo & itemSeqNo
					parser.parse(streamSource, renewHandler);

					if (renewHandler.isFullIdFound()) {
						String alephItemId = AlephUtil.buildAlephItemId(renewHandler.getBibDocNumber(), renewHandler.getItemDocNumber(), renewHandler.getItemSequenceNumber());

						LookupItemInitiationData LIinitData = new LookupItemInitiationData();
						ItemId itemId = new ItemId();
						itemId.setItemIdentifierValue(alephItemId);

						LIinitData.setInitiationHeader(initData.getInitiationHeader());

						LIinitData.setItemId(itemId);
						LIinitData.setBibliographicDescriptionDesired(getBibDescription);
						LIinitData.setCirculationStatusDesired(getCircStatus);
						LIinitData.setHoldQueueLengthDesired(getHoldQueueLength);
						LIinitData.setItemDescriptionDesired(getItemDescription);
						LIinitData.setLocationDesired(getLocation);

						AlephItem item = lookupItem(LIinitData);

						renewItem.setItemOptionalFields(item.getItemOptionalFields());
					}
				}
			} else {
				Problem problem = new Problem();
				problem.setProblemDetail(renewHandler.getStatusText());
				problem.setProblemType(new ProblemType(renewHandler.getReplyText()));
				renewItem.setProblem(problem);
			}
		} else if (renewHandler.loanWasFound()) {
			Problem problem = new Problem();
			problem.setProblemType(new ProblemType("Loan is marked as not renewable."));
			renewItem.setProblem(problem);
		} else {
			Problem problem = new Problem();
			problem.setProblemType(new ProblemType("Loan does not exist."));
			renewItem.setProblem(problem);
		}
		return renewItem;
	}

	public String updateUser(UpdateUserInitiationData initData) throws IOException, AlephException, SAXException, ParserConfigurationException, FactoryConfigurationError,
			TransformerException {

		String patronId = initData.getUserId().getUserIdentifierValue();

		// First parse mandatory fields to update with Rest Dlf - which are z304-address-1, z304-date-from & z304-date-to at patron address
		URL addressLink = new URLBuilder().setBase(LocalConfig.getServerName(), LocalConfig.getServerPort())
				.setPath(LocalConfig.getServerSuffix(), AlephConstants.USER_PATH_ELEMENT, patronId, AlephConstants.PARAM_PATRON_INFO, AlephConstants.PARAM_ADDRESS).toURL();

		InputSource streamSource = new InputSource(addressLink.openStream());

		AlephUpdateUserHandler updateUserHandler = new AlephUpdateUserHandler();

		// Parse values of z304-address-1 & z304-date-from & z304-date-to .. to correctly build post_xml with mandatory fields
		parser.parse(streamSource, updateUserHandler.setParsingMandatoryFields());

		if (updateUserHandler.isUpdateable() && updateUserHandler.parsedAllMandatoryFields()) {

			// Parse all other values in order to be capable of particular modifications in each element
			streamSource = new InputSource(addressLink.openStream());
			parser.parse(streamSource, updateUserHandler);

			AlephPatronAddress patronAddressParsed = updateUserHandler.getPatronAddress();

			AddUserFields addUserFields = initData.getAddUserFields();

			List<ElectronicAddress> emailsToAdd = null;
			List<ElectronicAddress> phonesToAdd = null;
			PhysicalAddress physicalAddressToAdd = null;

			String nodeUserPhoneStoredIn = LocalConfig.getNodeUserPhoneStoredIn();
			String nodeUserStreetStoredIn = LocalConfig.getNodeUserStreetStoredIn();
			String nodeUserPostalAndCityStoredIn = LocalConfig.getNodeUserPostalAndCityStoredIn();
			String nodeUserEmailStoredIn = AlephConstants.Z304_EMAIL_ADDRESS_NODE;

			if (addUserFields != null) {

				/*
				 Here we iterate over all UserAddressInformations to determine it's type.
				 
				 Now we have to parse values about to be added, in the next step we add those values using appropriate formatting
				  into z304-address-x based on configuration in toolkit.properties.
				 */
				for (UserAddressInformation uai : addUserFields.getUserAddressInformations()) {
					if (uai.getElectronicAddress() != null) {
						String typeValue = uai.getElectronicAddress().getElectronicAddressType().getValue();

						if (typeValue.equalsIgnoreCase(Version1ElectronicAddressType.MAILTO.getValue())) { // If type is MAILTO

							if (emailsToAdd == null)
								emailsToAdd = new ArrayList<ElectronicAddress>();

							emailsToAdd.add(uai.getElectronicAddress());

						} else if (typeValue.equalsIgnoreCase(Version1ElectronicAddressType.TEL.getValue())) { // If type is TEL

							if (phonesToAdd == null)
								phonesToAdd = new ArrayList<ElectronicAddress>();

							phonesToAdd.add(uai.getElectronicAddress());

						} else
							return "Cannot process ElectronicAddress of type \"" + typeValue + "\". It was found within AddUserField.";

					} else if (uai.getPhysicalAddress() != null) {
						if (physicalAddressToAdd == null)
							physicalAddressToAdd = uai.getPhysicalAddress();
						else
							return "Cannot process more than one PhysicalAddress. It was found within AddUserField.";
					}
				}

			}

			DeleteUserFields deleteUserFields = initData.getDeleteUserFields();

			List<ElectronicAddress> emailsToDelete = null;
			List<ElectronicAddress> phonesToDelete = null;
			PhysicalAddress physicalAddressToDelete = null;

			if (deleteUserFields != null) {

				/*
				 Here we iterate over all UserAddressInformations to determine it's type.
				 
				 Now we have to parse values about to be deleted, in the next step we delete those values using appropriate formatting
				  into z304-address-x based on configuration in toolkit.properties.
				 */
				for (UserAddressInformation uai : deleteUserFields.getUserAddressInformations()) {
					if (uai.getElectronicAddress() != null) {
						String typeValue = uai.getElectronicAddress().getElectronicAddressType().getValue();

						if (typeValue.equalsIgnoreCase(Version1ElectronicAddressType.MAILTO.getValue())) { // If type is MAILTO

							if (emailsToDelete == null)
								emailsToDelete = new ArrayList<ElectronicAddress>();

							emailsToDelete.add(uai.getElectronicAddress());

						} else if (typeValue.equalsIgnoreCase(Version1ElectronicAddressType.TEL.getValue())) { // If type is TEL

							if (phonesToDelete == null)
								phonesToDelete = new ArrayList<ElectronicAddress>();

							phonesToDelete.add(uai.getElectronicAddress());

						} else
							return "Cannot process ElectronicAddress of type \"" + typeValue + "\". It was found within DeleteUserField.";

					} else if (uai.getPhysicalAddress() != null) {
						if (physicalAddressToDelete == null)
							physicalAddressToDelete = uai.getPhysicalAddress();
						else
							return "Cannot process more than one PhysicalAddress. It was found within DeleteUserField.";
					}
				}
			}

			List<String> emails = null;
			List<String> phones = null;

			String street = null;
			String postalCodeAndCity = null;

			// Process Emails Deletion / Addition
			if (emailsToAdd != null || emailsToDelete != null) {

				String parsedEmails = patronAddressParsed.getZ304emailAddress();

				if (parsedEmails != null) {
					String[] parsedEmailsArray = StringUtils.trimArrayElements(parsedEmails.split(AlephConstants.PATRON_ADDRESS_DELIMITER));

					emails = new ArrayList<String>(Arrays.asList(parsedEmailsArray));

					// Delete emails desired to delete
					if (emailsToDelete != null) {
						for (ElectronicAddress emailToDelete : emailsToDelete) {
							emails.remove(emailToDelete.getElectronicAddressData());
						}
					}
				}

				// Add emails desired to add
				if (emailsToAdd != null) {
					if (parsedEmails == null)
						emails = new ArrayList<String>();

					for (ElectronicAddress emailToAdd : emailsToAdd) {
						if (!emailToAdd.getElectronicAddressData().isEmpty())
							emails.add(emailToAdd.getElectronicAddressData());
						else
							return "You have specified empty ElectornicAddressData of type \"mailto\" in AddUserFields.";
					}
				}

			}

			// Process Phones Deletion / Addition
			if (phonesToAdd != null || phonesToDelete != null) {

				String parsedPhones = patronAddressParsed.get(nodeUserPhoneStoredIn);

				if (parsedPhones != null) {
					String[] parsedPhonesArray = StringUtils.trimArrayElements(parsedPhones.split(AlephConstants.PATRON_ADDRESS_DELIMITER));

					phones = new ArrayList<String>(Arrays.asList(parsedPhonesArray));

					if (phonesToDelete != null) {
						for (ElectronicAddress phoneToDelete : phonesToDelete) {
							phones.remove(phoneToDelete.getElectronicAddressData());
						}
					}
				}

				if (phonesToAdd != null) {
					if (parsedPhones == null)
						phones = new ArrayList<String>();

					for (ElectronicAddress phoneToAdd : phonesToAdd) {
						if (!phoneToAdd.getElectronicAddressData().isEmpty())
							phones.add(phoneToAdd.getElectronicAddressData());
						else
							return "You have specified empty ElectronicAddressData of type \"tel\" in AddUserFields.";
					}
				}
			}

			// Process PhysicalAddress Deletion / Addition
			if (physicalAddressToAdd != null || physicalAddressToDelete != null) {

				street = patronAddressParsed.get(nodeUserStreetStoredIn);

				postalCodeAndCity = patronAddressParsed.get(nodeUserPostalAndCityStoredIn);

				String newPostal = null;
				String newCity = null;

				boolean streetDeletionDesired = false;
				boolean postalDeletionDesired = false;
				boolean cityDeletionDesired = false;

				boolean streetAdditionDesired = false;
				boolean postalAdditionDesired = false;
				boolean cityAdditionDesired = false;

				// Process Deletion
				if (physicalAddressToDelete != null && physicalAddressToDelete.getStructuredAddress() != null) {

					if (street != null) {

						String streetToDelete = physicalAddressToDelete.getStructuredAddress().getStreet();

						if (streetToDelete != null) {

							if (!streetToDelete.isEmpty())
								streetDeletionDesired = true;
							else
								return "You have specified empty Street in DeleteUserFields.";

							street = street.replace(streetToDelete, "");

							street = street.trim();

							if (!street.isEmpty()) {
								return "Street you have specified to delete could not be deleted. Please check it matches the street you have assigned now.";
							}
						}
					}

					if (postalCodeAndCity != null) {

						String cityToDelete = physicalAddressToDelete.getStructuredAddress().getLocality();

						if (cityToDelete != null) {

							if (!cityToDelete.isEmpty())
								cityDeletionDesired = true;
							else
								return "You have specified empty City in DeleteUserFields.";

							// Save the length of current state
							int postalCodeAndCityLength = postalCodeAndCity.length();

							postalCodeAndCity = postalCodeAndCity.replace(cityToDelete, "");

							// Was it deleted?
							boolean cityDeleted = postalCodeAndCity.length() == postalCodeAndCityLength - cityToDelete.length();

							if (!cityDeleted) {
								return "City you have specified to delete could not be deleted. Please check it matches the city you have assigned now.";
							}
						}

						String postalToDelete = physicalAddressToDelete.getStructuredAddress().getPostalCode();

						if (postalToDelete != null) {

							if (!postalToDelete.isEmpty())
								postalDeletionDesired = true;
							else
								return "You have specified empty PostalCode in DeleteUserFields.";

							// Save the length of current state
							int postalCodeAndCityLength = postalCodeAndCity.length();

							postalCodeAndCity = postalCodeAndCity.replace(postalToDelete, "");

							// Was it deleted?
							boolean postalDeleted = postalCodeAndCity.length() == postalCodeAndCityLength - postalToDelete.length();

							if (!postalDeleted) {
								return "Postal Code you have specified to delete could not be deleted. Please check it matches the postal you have assigned now.";
							}
						}

						postalCodeAndCity = postalCodeAndCity.trim();
					}
				}

				// Process Addition
				if (physicalAddressToAdd != null && physicalAddressToAdd.getStructuredAddress() != null) {
					String streetToAdd = physicalAddressToAdd.getStructuredAddress().getStreet();
					if (streetToAdd != null) {
						if (!streetToAdd.isEmpty()) {
							streetAdditionDesired = true;

							// Do not allow Adding a street if not deleted recently for security reasons
							if (streetDeletionDesired) {
								street = streetToAdd;
							} else
								return "You have to first delete Street before adding another.";
						} else
							return "You have specified empty Street in AddUserFields.";

					}

					String postalCodeToAdd = physicalAddressToAdd.getStructuredAddress().getPostalCode();
					if (postalCodeToAdd != null) {
						if (!postalCodeToAdd.isEmpty()) {
							postalAdditionDesired = true;

							// Do not allow Adding a postal if not deleted recently for security reasons
							if (postalDeletionDesired) {
								newPostal = postalCodeToAdd;
							} else
								return "You have to first delete PostalCode before adding another.";
						} else
							return "You have specified empty PostalCode in AddUserFields.";
					}

					String cityToAdd = physicalAddressToAdd.getStructuredAddress().getLocality();
					if (cityToAdd != null) {
						if (!cityToAdd.isEmpty()) {
							cityAdditionDesired = true;

							// Do not allow Adding a city if not deleted recently for security reasons
							if (cityDeletionDesired) {
								newCity = cityToAdd;
							} else
								return "You have to first delete City before adding another.";
						} else
							return "You have specified empty City in AddUserFields.";
					}

					// Do not allow Deleting a street if not deleted recently for security reasons
					if (streetDeletionDesired && !streetAdditionDesired)
						return "You have to specify Street to be replaced after desired Street deletion is done.";

					// Do not allow Deleting a city if not deleted recently for security reasons
					if (cityDeletionDesired && !cityAdditionDesired)
						return "You have to specify City to be replaced after desired City deletion is done.";

					// Do not allow Deleting a postal if not deleted recently for security reasons
					if (postalDeletionDesired && !postalAdditionDesired)
						return "You have to specify PostalCode to be replaced after desired PostalCode deletion is done.";

					// Handle joining city & postal into one String
					if (cityAdditionDesired && postalAdditionDesired) {
						postalCodeAndCity = newPostal + " " + newCity;
					} else if (cityAdditionDesired) {
						postalCodeAndCity += " " + newCity;
					} else {
						postalCodeAndCity = newPostal + " " + postalCodeAndCity;
					}
				}
			}

			// @formatter:off
			
			// Build XML POST request with mandatory fields filled in
			XMLBuilder xmlRequest = XMLBuilder.create(AlephConstants.GET_PAT_ADRS_NODE)
					.elem(AlephConstants.ADDRESS_INFORMATION_NODE)
						.elem(AlephConstants.Z304_ADDRESS_1_NODE).text(patronAddressParsed.getZ304address1())
							.up()
						.elem(AlephConstants.Z304_DATE_FROM_NODE).text(patronAddressParsed.getZ304dateFrom())
							.up()
						.elem(AlephConstants.Z304_DATE_TO_NODE).text(patronAddressParsed.getZ304dateTo());
			
			// @formatter:on

			if (street != null) {

				// Check street length against max_len attribute of element in which is street stored in (this we know from toolkit.properties)
				String maxLengthParsed = patronAddressParsed.get(nodeUserStreetStoredIn + AlephConstants.MAX_LEN_NODE_ATTRIBUTE);

				if (maxLengthParsed != null) {

					int streetMaxLength = Integer.parseInt(maxLengthParsed);

					if (street.length() > streetMaxLength)
						return "Cannot save " + street.length() + " chars to " + nodeUserStreetStoredIn + ". Maximum is " + maxLengthParsed
								+ " chars. (This means removal of some characters in Street is neccessary)";
				}

				xmlRequest.up().elem(nodeUserStreetStoredIn).text(street);
			}

			if (postalCodeAndCity != null) {

				// Check PostalCodeAndCity string length against max_len attribute of element in which is postal & city stored in (this we know from toolkit.properties)
				String maxLengthParsed = patronAddressParsed.get(nodeUserPostalAndCityStoredIn + AlephConstants.MAX_LEN_NODE_ATTRIBUTE);

				if (maxLengthParsed != null) {

					int postalCodeAndCityMaxLength = Integer.parseInt(maxLengthParsed);

					if (postalCodeAndCity.length() > postalCodeAndCityMaxLength)
						return "Cannot save " + postalCodeAndCity.length() + " chars to " + nodeUserPostalAndCityStoredIn + ". Maximum is " + maxLengthParsed
								+ " chars. (This means removal of some characters in Postal Code or City is neccessary)";
				}

				xmlRequest.up().elem(nodeUserPostalAndCityStoredIn).text(postalCodeAndCity);
			}

			if (phones != null) {
				String phonesJoined = "";

				// Join all phones
				for (int i = 0; i < phones.size(); ++i) {
					phonesJoined += phones.get(i);

					if (i != phones.size() - 1)
						phonesJoined += AlephConstants.PATRON_ADDRESS_DELIMITER;
				}

				// Check phones joined string length against max_len attribute of element in which are phones stored in (this we know from toolkit.properties)
				String maxLengthParsed = patronAddressParsed.get(nodeUserPhoneStoredIn + AlephConstants.MAX_LEN_NODE_ATTRIBUTE);

				if (maxLengthParsed != null) {
					int phonesMaxLength = Integer.parseInt(maxLengthParsed);

					if (phonesJoined.length() > phonesMaxLength)
						return "Cannot save " + phonesJoined.length() + " chars to " + nodeUserPhoneStoredIn + ". Maximum is " + maxLengthParsed
								+ " chars. (This means removal of some phones is neccessary)";
				}

				// Set joined phones to our XML
				xmlRequest.up().elem(nodeUserPhoneStoredIn).text(phonesJoined);
			}

			if (emails != null) {
				String emailsJoined = "";

				// Join all emails
				for (int i = 0; i < emails.size(); ++i) {
					emailsJoined += emails.get(i);

					if (i != emails.size() - 1)
						emailsJoined += AlephConstants.PATRON_ADDRESS_DELIMITER;
				}

				// Check if there can be saved so long email String
				String maxLengthParsed = patronAddressParsed.getZ304emailAddressMaxLength();

				if (maxLengthParsed != null) {
					int emailsMaxLength = Integer.parseInt(patronAddressParsed.getZ304emailAddressMaxLength());

					if (emailsJoined.length() > emailsMaxLength)
						return "Cannot save " + emailsJoined.length() + " chars to " + nodeUserEmailStoredIn + ". Maximum is " + maxLengthParsed
								+ " chars. (This means removal of some emails is neccessary)";
				}

				// Set joined emails to our XML
				xmlRequest.up().elem(nodeUserEmailStoredIn).text(emailsJoined);
			}

			String xmlToPost = "post_xml=" + xmlRequest.asString();

			HttpURLConnection httpCon = (HttpURLConnection) addressLink.openConnection();
			httpCon.setDoOutput(true);
			httpCon.setRequestMethod("POST");

			OutputStreamWriter outWriter = new OutputStreamWriter(httpCon.getOutputStream());
			outWriter.write(xmlToPost);
			outWriter.close();

			streamSource = new InputSource(httpCon.getInputStream());

			// Parse reply-code or reply-text
			parser.parse(streamSource, updateUserHandler);

			return updateUserHandler.getReplyText();

		} else if (!updateUserHandler.isUpdateable()) {
			return "Parameter updateable is set to \"N\".";
		} else
			return "Could not parse all mandatory fields, thus cannot build valid XML.";

	}

	public AgencyAddressInformation getAgencyPhysicalAddressInformation() {
		AgencyAddressInformation agencyAddressInformation = new AgencyAddressInformation();

		PhysicalAddress physicalAddress = new PhysicalAddress();

		UnstructuredAddress unstructuredAddress = new UnstructuredAddress();

		unstructuredAddress.setUnstructuredAddressData(LocalConfig.getAgencyAddress());
		unstructuredAddress.setUnstructuredAddressType(Version1UnstructuredAddressType.NEWLINE_DELIMITED_TEXT);

		physicalAddress.setUnstructuredAddress(unstructuredAddress);

		physicalAddress.setPhysicalAddressType(Version1PhysicalAddressType.STREET_ADDRESS);
		agencyAddressInformation.setPhysicalAddress(physicalAddress);

		AgencyAddressRoleType agencyAddressRoleType = Version1AgencyAddressRoleType.OFFICIAL;
		agencyAddressInformation.setAgencyAddressRoleType(agencyAddressRoleType);

		return agencyAddressInformation;
	}

	public List<OrganizationNameInformation> getOrganizationNameInformations() {
		List<OrganizationNameInformation> organizationNameInformations = new ArrayList<OrganizationNameInformation>();
		OrganizationNameInformation organizationNameInfo = new OrganizationNameInformation();

		organizationNameInfo.setOrganizationName(LocalConfig.getAgencyName());
		organizationNameInfo.setOrganizationNameType(Version1OrganizationNameType.TRANSLATED_NAME);
		organizationNameInformations.add(organizationNameInfo);
		return organizationNameInformations;
	}
}
