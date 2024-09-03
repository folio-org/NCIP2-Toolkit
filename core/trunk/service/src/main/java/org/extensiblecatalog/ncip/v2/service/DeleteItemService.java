package org.extensiblecatalog.ncip.v2.service;

public interface DeleteItemService extends NCIPService<DeleteItemInitiationData, DeleteItemResponseData> {
    DeleteItemResponseData performService(DeleteItemInitiationData initData, ServiceContext context, RemoteServiceManager serviceManager) throws ServiceException, ValidationException;
}
