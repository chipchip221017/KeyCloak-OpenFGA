package com.twogenidentity.keycloak.service;

import com.twogenidentity.keycloak.utils.OpenFgaHelper;
import dev.openfga.sdk.api.client.ClientWriteRequest;
import dev.openfga.sdk.api.client.OpenFgaClient;
import dev.openfga.sdk.api.configuration.ClientConfiguration;
import dev.openfga.sdk.api.configuration.ClientWriteOptions;
import dev.openfga.sdk.api.model.*;
import dev.openfga.sdk.errors.FgaInvalidParameterException;
import com.twogenidentity.keycloak.event.EventParser;
import org.jboss.logging.Logger;
import org.keycloak.Config;

import java.time.Duration;
import java.util.concurrent.ExecutionException;

public class OpenFgaClientHandler {

    protected final Config.Scope config;
    private final OpenFgaClient fgaClient;
    private final OpenFgaHelper fgaHelper;
    private Boolean isClientInitialized = false;

    private final ClientWriteOptions clientWriteOptions;

    protected static final String OPENFGA_API_URL = "openfgaApiUrl";
    protected static final String OPENFGA_STORE_ID = "openfgaStoreId";
    protected static final String OPENFGA_AUTHORIZATION_MODEL_ID = "openfgaAuthorizationModelId";

    private static final Logger LOG = Logger.getLogger(OpenFgaClientHandler.class);

    public OpenFgaClientHandler(Config.Scope config ) throws FgaInvalidParameterException {
        this.config = config;

        ClientConfiguration configuration = new ClientConfiguration()
                .apiUrl(getOpenFgaApiUrl())
                .connectTimeout(Duration.ofSeconds(5))
                .readTimeout(Duration.ofSeconds(5));

        if (configuration != null) {
            LOG.info(configuration.getApiUrl());
        }
        if (config != null) {
            LOG.info(config);
            LOG.info(config.get(OPENFGA_STORE_ID));
            LOG.info(config.get(OPENFGA_AUTHORIZATION_MODEL_ID));

            if(getOpenFgaOpenStoreId() != null && !getOpenFgaOpenStoreId().isEmpty()
                    && getOpenFgaAuthorizationModelId() != null && !getOpenFgaAuthorizationModelId().isEmpty()) {
                configuration.storeId(getOpenFgaOpenStoreId());
                configuration.authorizationModelId(getOpenFgaAuthorizationModelId());
                this.isClientInitialized = true;
            }
        }

        this.fgaHelper = new OpenFgaHelper();
        this.clientWriteOptions = new ClientWriteOptions();
        this.fgaClient = new OpenFgaClient(configuration);
    }

    public void publish(String eventId , EventParser event) throws FgaInvalidParameterException, ExecutionException, InterruptedException {
        if(!this.isClientInitialized && !this.discoverClientConfiguration()) {
            LOG.error("[OpenFgaEventPublisher] Unable to initialized client for event " + eventId);
        }
        else {
            ClientWriteRequest request  = fgaHelper.toClientWriteRequest(event);
            if(!request.getWrites().isEmpty() || !request.getDeletes().isEmpty()) {
                LOG.debug("[OpenFgaEventPublisher] Publishing event id: " + eventId + " event: " + event.toString());
                var response = fgaClient.write(request, this.clientWriteOptions).get();
                LOG.debug("[OpenFgaEventPublisher] Successfully sent tuple key to OpenFga, response: " + response);
            }
        }
    }

    private boolean discoverClientConfiguration() throws FgaInvalidParameterException, ExecutionException, InterruptedException {
        LOG.info("[OpenFgaEventPublisher] Discover store and authorization model");
        ListStoresResponse stores = fgaClient.listStores().get();
        if(!stores.getStores().isEmpty()) {
           Store store = stores.getStores().get(0);
           LOG.info("[OpenFgaEventPublisher] Found store id:" + store.getId());
           this.fgaClient.setStoreId(store.getId());
           ReadAuthorizationModelsResponse authorizationModels = fgaClient.readAuthorizationModels().get();
           if(authorizationModels.getAuthorizationModels().size() > 0) {
               AuthorizationModel model = authorizationModels.getAuthorizationModels().get(0);
               LOG.info("[OpenFgaEventPublisher] Found authorization model id:" + model.getId());
               this.fgaClient.setAuthorizationModelId(model.getId());
               fgaHelper.loadModel(model);
               this.isClientInitialized = true;
           }
        }
        return this.isClientInitialized;
    }

    public String getOpenFgaApiUrl() {
        return config.get(OPENFGA_API_URL) != null ? config.get(OPENFGA_API_URL) : "http://openfga:8080";
    }

    public String getOpenFgaOpenStoreId() {
        return config.get(OPENFGA_STORE_ID);
    }

    public String getOpenFgaAuthorizationModelId() {
        return config.get(OPENFGA_AUTHORIZATION_MODEL_ID);
    }
}
