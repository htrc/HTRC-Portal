/*
  Copyright 2012 -2014 Michael Remond

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package org.pac4j.saml.client;

import org.apache.velocity.app.VelocityEngine;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.opensaml.Configuration;
import org.opensaml.DefaultBootstrap;
import org.opensaml.saml2.binding.decoding.HTTPPostDecoder;
import org.opensaml.saml2.binding.encoding.HTTPPostEncoder;
import org.opensaml.saml2.core.*;
import org.opensaml.saml2.encryption.Decrypter;
import org.opensaml.saml2.metadata.EntitiesDescriptor;
import org.opensaml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml2.metadata.provider.AbstractMetadataProvider;
import org.opensaml.saml2.metadata.provider.ChainingMetadataProvider;
import org.opensaml.saml2.metadata.provider.FilesystemMetadataProvider;
import org.opensaml.saml2.metadata.provider.MetadataProviderException;
import org.opensaml.xml.ConfigurationException;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.encryption.DecryptionException;
import org.opensaml.xml.io.Marshaller;
import org.opensaml.xml.io.MarshallerFactory;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.parse.StaticBasicParserPool;
import org.opensaml.xml.parse.XMLParserException;
import org.opensaml.xml.security.keyinfo.NamedKeyInfoGeneratorManager;
import org.opensaml.xml.security.x509.X509KeyInfoGeneratorFactory;
import org.opensaml.xml.signature.SignatureTrustEngine;
import org.opensaml.xml.util.Base64;
import org.pac4j.core.client.BaseClient;
import org.pac4j.core.client.Protocol;
import org.pac4j.core.client.RedirectAction;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.exception.RequiresHttpAction;
import org.pac4j.core.exception.TechnicalException;
import org.pac4j.core.util.CommonHelper;
import org.pac4j.saml.context.ExtendedSAMLMessageContext;
import org.pac4j.saml.context.Saml2ContextProvider;
import org.pac4j.saml.credentials.Saml2Credentials;
import org.pac4j.saml.crypto.CredentialProvider;
import org.pac4j.saml.crypto.EncryptionProvider;
import org.pac4j.saml.crypto.SignatureTrustEngineProvider;
import org.pac4j.saml.exceptions.SamlException;
import org.pac4j.saml.metadata.Saml2MetadataGenerator;
import org.pac4j.saml.profile.Saml2Profile;
import org.pac4j.saml.sso.Saml2AuthnRequestBuilder;
import org.pac4j.saml.sso.Saml2ResponseValidator;
import org.pac4j.saml.sso.Saml2SingleLogoutRequestBuilder;
import org.pac4j.saml.sso.Saml2WebSSOProfileHandler;
import org.pac4j.saml.transport.Pac4jHTTPPostDecoder;
import org.pac4j.saml.transport.SimpleResponseAdapter;
import org.pac4j.saml.util.VelocityEngineFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is the client to authenticate users with a SAML2 Identity Provider. This implementation relies on the Web
 * Browser SSO profile with HTTP-POST binding. (http://docs.oasis-open.org/security/saml/v2.0/saml-profiles-2.0-os.pdf).
 *
 * @author Michael Remond
 * @since 1.5.0
 */
public class Saml2Client extends BaseClient<Saml2Credentials, Saml2Profile> {

    protected static final Logger logger = LoggerFactory.getLogger(Saml2Client.class);

    // Identify the KeyInfoGenerator factory created during opensaml boostrap
    public static final String SAML_METADATA_KEY_INFO_GENERATOR = "MetadataKeyInfoGenerator";

    private String keystorePath;

    private String keystorePassword;

    private String privateKeyPassword;

    private String idpMetadataPath;

    private String idpEntityId;

    private Integer maximumAuthenticationLifetime;

    private CredentialProvider credentialProvider;

    private Saml2ContextProvider contextProvider;

    private Saml2AuthnRequestBuilder authnRequestBuilder;

    private Saml2SingleLogoutRequestBuilder logoutRequestBuilder;

    private Saml2WebSSOProfileHandler handler;

    private Saml2ResponseValidator responseValidator;

    private SignatureTrustEngineProvider signatureTrustEngineProvider;

    private EncryptionProvider encryptionProvider;

    private String spMetadata;

    // If this flag is on, SAML2 client will retrieve a OAuth2 token/refresh token pair
    // by invoking oauth2TokenEndpoint with SAML2 assertion.
    private boolean oauth2ExchangeEnabled = false;

    private String oauth2TokenEndpoint;

    private String oauth2ClientID;

    private String oauth2ClientSecret;

    // If devMode flag is on, https certificate validation will be disabled.
    private boolean devMode = false;

    @Override
    protected void internalInit() {

        CommonHelper.assertNotBlank("keystorePath", this.keystorePath);
        CommonHelper.assertNotBlank("keystorePassword", this.keystorePassword);
        CommonHelper.assertNotBlank("privateKeyPassword", this.privateKeyPassword);
        CommonHelper.assertNotBlank("idpMetadataPath", this.idpMetadataPath);
        CommonHelper.assertNotBlank("callbackUrl", this.callbackUrl);

        if (this.oauth2ExchangeEnabled) {
            CommonHelper.assertNotBlank("oauth2TokenEndpoint", this.oauth2TokenEndpoint);
            CommonHelper.assertNotBlank("oauth2ClientID", this.oauth2ClientID);
            CommonHelper.assertNotBlank("oauth2ClientSecret", this.oauth2ClientSecret);
        }

        if (!this.callbackUrl.startsWith("http")) {
            throw new TechnicalException("SAML callbackUrl must be absolute");
        }

        // Bootsrap OpenSAML
        try {
            DefaultBootstrap.bootstrap();
            NamedKeyInfoGeneratorManager manager = Configuration.getGlobalSecurityConfiguration()
                    .getKeyInfoGeneratorManager();
            X509KeyInfoGeneratorFactory generator = new X509KeyInfoGeneratorFactory();
            generator.setEmitEntityCertificate(true);
            generator.setEmitEntityCertificateChain(true);
            manager.registerFactory(Saml2Client.SAML_METADATA_KEY_INFO_GENERATOR, generator);
        } catch (ConfigurationException e) {
            throw new SamlException("Error bootstrapping OpenSAML", e);
        }

        // load private key from the keystore and provide it as OpenSAML credentials
        this.credentialProvider = new CredentialProvider(this.keystorePath, this.keystorePassword,
                this.privateKeyPassword);

        // required parserPool for XML processing
        StaticBasicParserPool parserPool = new StaticBasicParserPool();
        try {
            parserPool.initialize();
        } catch (XMLParserException e) {
            throw new SamlException("Error initializing parserPool", e);
        }

        // load IDP metadata from a file
        FilesystemMetadataProvider idpMetadataProvider;
        try {
            URL url = CommonHelper.getURLFromName(this.idpMetadataPath);
            idpMetadataProvider = new FilesystemMetadataProvider(new File(url.toURI()));
            idpMetadataProvider.setParserPool(parserPool);
            idpMetadataProvider.initialize();
        } catch (MetadataProviderException e) {
            throw new SamlException("Error initializing idpMetadataProvider", e);
        } catch (URISyntaxException e) {
            throw new TechnicalException("Error converting idp Metadata path url to uri", e);
        }

        // If no idpEntityId declared, select first EntityDescriptor entityId as our IDP entityId
        if (this.idpEntityId == null) {
            try {
                XMLObject md = idpMetadataProvider.getMetadata();
                if (md instanceof EntitiesDescriptor) {
                    for (EntityDescriptor entity : ((EntitiesDescriptor) md).getEntityDescriptors()) {
                        this.idpEntityId = entity.getEntityID();
                        break;
                    }
                } else if (md instanceof EntityDescriptor) {
                    this.idpEntityId = ((EntityDescriptor) md).getEntityID();
                }
            } catch (MetadataProviderException e) {
                throw new SamlException("Error getting idp entityId from IDP metadata", e);
            }
            if (this.idpEntityId == null) {
                throw new SamlException("No idp entityId found");
            }
        }

        // Generate our Service Provider metadata
        Saml2MetadataGenerator metadataGenerator = new Saml2MetadataGenerator();
        metadataGenerator.setCredentialProvider(this.credentialProvider);
        // for the spEntityId, use the callback url
        String spEntityId = getCallbackUrl();
        metadataGenerator.setEntityId(spEntityId);
        // Assertion consumer service url is the callback url
        metadataGenerator.setAssertionConsumerServiceUrl(getCallbackUrl());
        // for now same for logout url
        metadataGenerator.setSingleLogoutServiceUrl(getCallbackUrl());
        AbstractMetadataProvider spMetadataProvider = metadataGenerator.buildMetadataProvider();

        // Initialize metadata provider for our SP and get the XML as a String
        try {
            spMetadataProvider.initialize();
            this.spMetadata = metadataGenerator.printMetadata();
        } catch (MetadataProviderException e) {
            throw new TechnicalException("Error initializing spMetadataProvider", e);
        } catch (MarshallingException e) {
            logger.warn("Unable to print SP metadata", e);
        }

        // Put IDP and SP metadata together
        ChainingMetadataProvider metadataManager = new ChainingMetadataProvider();
        try {
            metadataManager.addMetadataProvider(idpMetadataProvider);
            metadataManager.addMetadataProvider(spMetadataProvider);
        } catch (MetadataProviderException e) {
            throw new TechnicalException("Error adding idp or sp metadatas to manager", e);
        }

        // Build the contextProvider
        this.contextProvider = new Saml2ContextProvider(metadataManager, this.idpEntityId, spEntityId);

        // Get a velocity engine for the HTTP-POST binding (building of an HTML document)
        VelocityEngine velocityEngine = VelocityEngineFactory.getEngine();
        // Get an AuthnRequest builder
        this.authnRequestBuilder = new Saml2AuthnRequestBuilder();
        // LogoutRequest builder
        this.logoutRequestBuilder = new Saml2SingleLogoutRequestBuilder();

        // Build the WebSSO handler for sending and receiving SAML2 messages
        HTTPPostEncoder postEncoder = new HTTPPostEncoder(velocityEngine, "/templates/saml2-post-binding.vm");
        HTTPPostDecoder postDecoder = new Pac4jHTTPPostDecoder(parserPool);
        this.handler = new Saml2WebSSOProfileHandler(this.credentialProvider, postEncoder, postDecoder, parserPool);

        // Build provider for digital signature validation and encryption
        this.signatureTrustEngineProvider = new SignatureTrustEngineProvider(metadataManager);
        this.encryptionProvider = new EncryptionProvider(this.credentialProvider);

        // Build the SAML response validator
        this.responseValidator = new Saml2ResponseValidator();
        if (this.maximumAuthenticationLifetime != null) {
            this.responseValidator.setMaximumAuthenticationLifetime(this.maximumAuthenticationLifetime);
        }

    }

    @Override
    protected BaseClient<Saml2Credentials, Saml2Profile> newClient() {
        Saml2Client client = new Saml2Client();
        client.setKeystorePath(this.keystorePath);
        client.setKeystorePassword(this.keystorePassword);
        client.setPrivateKeyPassword(this.privateKeyPassword);
        client.setIdpMetadataPath(this.idpMetadataPath);
        client.setIdpEntityId(this.idpEntityId);
        client.setMaximumAuthenticationLifetime(this.maximumAuthenticationLifetime);
        client.setCallbackUrl(this.callbackUrl);
        return client;
    }

    @Override
    protected boolean isDirectRedirection() {
        return false;
    }

    @Override
    protected RedirectAction retrieveRedirectAction(final WebContext wc) {

        ExtendedSAMLMessageContext context = this.contextProvider.buildSpAndIdpContext(wc);
        final String relayState = getContextualCallbackUrl(wc);

        AuthnRequest authnRequest = this.authnRequestBuilder.build(context);

        this.handler.sendMessage(context, authnRequest, relayState);

        String content = ((SimpleResponseAdapter) context.getOutboundMessageTransport()).getOutgoingContent();

        return RedirectAction.success(content);
    }

    @Override
    protected Saml2Credentials retrieveCredentials(final WebContext wc) throws RequiresHttpAction {

        ExtendedSAMLMessageContext context = this.contextProvider.buildSpContext(wc);
        // assertion consumer url is pac4j callback url
        String callbackUrl = getCallbackUrl();
        context.setAssertionConsumerUrl(callbackUrl);

        SignatureTrustEngine trustEngine = this.signatureTrustEngineProvider.build();
        Decrypter decrypter = this.encryptionProvider.buildDecrypter();

        this.handler.receiveMessage(context, trustEngine);

        this.responseValidator.validateSamlResponse(context, trustEngine, decrypter);

        return buildSaml2Credentials(context, decrypter);

    }

    private Saml2Credentials buildSaml2Credentials(final ExtendedSAMLMessageContext context, final Decrypter decrypter) {

        NameID nameId = (NameID) context.getSubjectNameIdentifier();
        Assertion subjectAssertion = context.getSubjectAssertion();

        List<Attribute> attributes = new ArrayList<Attribute>();
        for (AttributeStatement attributeStatement : subjectAssertion.getAttributeStatements()) {
            for (Attribute attribute : attributeStatement.getAttributes()) {
                attributes.add(attribute);
            }
            for (EncryptedAttribute encryptedAttribute : attributeStatement.getEncryptedAttributes()) {
                try {
                    attributes.add(decrypter.decrypt(encryptedAttribute));
                } catch (DecryptionException e) {
                    logger.warn("Decryption of attribute failed, continue with the next one", e);
                }
            }
        }

        return new Saml2Credentials(nameId, attributes, getName(), subjectAssertion);
    }

    @Override
    protected Saml2Profile retrieveUserProfile(final Saml2Credentials credentials, final WebContext context) {

        Saml2Profile profile = new Saml2Profile();
        profile.setId(credentials.getNameId().getValue());
        for (Attribute attribute : credentials.getAttributes()) {
            List<String> values = new ArrayList<String>();
            for (XMLObject attributeValue : attribute.getAttributeValues()) {
                Element attributeValueElement = attributeValue.getDOM();
                String value = attributeValueElement.getTextContent();
                values.add(value);
            }
            profile.addAttribute(attribute.getName(), values);
        }

        if (oauth2ExchangeEnabled) {
            try {
                if (this.devMode) {
                    disableSelfSignedCertValidation();
                }

                String samlAssertion = marshall(credentials.getAssertion());
                String encodedSAMLAssertion = URLEncoder.encode(Base64.encodeBytes(samlAssertion.getBytes()), "UTF-8");

                String urlParameters = "grant_type=urn:ietf:params:oauth:grant-type:saml2-bearer&assertion=" + encodedSAMLAssertion + "&scope=PRODUCTION";

                //Create connection to the Token endpoint of API manger
                URL url = new URL(this.oauth2TokenEndpoint);

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");

                // Set the consumer-key and Consumer-secret
                connection.setRequestProperty("Authorization", "Basic " + Base64.encodeBytes((this.oauth2ClientID + ":" + this.oauth2ClientSecret).getBytes(), Base64.DONT_BREAK_LINES));
                connection.setUseCaches(false);
                connection.setDoInput(true);
                connection.setDoOutput(true);

                //Send request
                DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
                wr.writeBytes(urlParameters);
                wr.flush();
                wr.close();

                //Get Response
                InputStream is;
                int responseCode = connection.getResponseCode();

                if(responseCode == 200) {
                    is = connection.getInputStream();
                } else {
                    is = connection.getErrorStream();
                }

                BufferedReader rd = new BufferedReader(new InputStreamReader(is));

                String line;
                StringBuffer response = new StringBuffer();
                while ((line = rd.readLine()) != null) {
                    response.append(line);
                    response.append('\r');
                }

                rd.close();

                // TODO: Currently we only support OAuth2 token endpoints with JSON response in following format.
                // TODO:    - {"token_type":"bearer","expires_in":2232,"refresh_token":"815a65497c66ee186164418d518bdcea","access_token":"18b85eda38175bfa54ba12c7354f3dd8"}

                if(responseCode == 200) {
                    JSONParser jsonParser = new JSONParser();
                    JSONObject oauthTokenResponse = (JSONObject) jsonParser.parse(response.toString());

                    profile.addAttribute("access_token", oauthTokenResponse.get("access_token"));
                    profile.addAttribute("refresh_token", oauthTokenResponse.get("refresh_token"));
                } else {
                    String error = "Server returned with error code "+ responseCode + " and error message: \n" + response.toString();
                    logger.error(error);
                    throw new IOException("Server returned with error code "+ responseCode + " for request to " + this.oauth2TokenEndpoint);
                }
            } catch (Exception e) {
                String errMessage = "Unable to exchange SAML2 assertion for a OAuth2 token.";
                logger.error(errMessage, e);
                throw new SamlException(errMessage, e);
            }
        }

        return profile;
    }

    @Override
    public Protocol getProtocol() {
        return Protocol.SAML;
    }

    @Override
    public RedirectAction retrieveLogoutRedirectAction(Saml2Profile saml2Profile, WebContext wc) {
        ExtendedSAMLMessageContext samlMessageContext = this.contextProvider.buildSpContext(wc);
        LogoutRequest logoutRequest = this.logoutRequestBuilder.build(samlMessageContext, saml2Profile);

        // TODO: Fix relay state.
        this.handler.sendMessage(samlMessageContext, logoutRequest, "");

        String content = ((SimpleResponseAdapter) samlMessageContext.getOutboundMessageTransport()).getOutgoingContent();

        return RedirectAction.success(content);
    }

    @Override
    public void validateSingleLogOut(WebContext webContext) {
        ExtendedSAMLMessageContext context = this.contextProvider.buildSpContext(webContext);
        // assertion consumer url is pac4j callback url
        String callbackUrl = getCallbackUrl();
        context.setAssertionConsumerUrl(callbackUrl);

        SignatureTrustEngine trustEngine = this.signatureTrustEngineProvider.build();
        Decrypter decrypter = this.encryptionProvider.buildDecrypter();

        this.handler.receiveMessage(context, trustEngine);

        this.responseValidator.validateSamlLogOutResponse(context, trustEngine, decrypter);
    }

    public void setIdpMetadataPath(final String idpMetadataPath) {
        this.idpMetadataPath = idpMetadataPath;
    }

    public void setIdpEntityId(final String idpEntityId) {
        this.idpEntityId = idpEntityId;
    }

    public void setKeystorePath(final String keystorePath) {
        this.keystorePath = keystorePath;
    }

    public void setKeystorePassword(final String keystorePassword) {
        this.keystorePassword = keystorePassword;
    }

    public void setPrivateKeyPassword(final String privateKeyPassword) {
        this.privateKeyPassword = privateKeyPassword;
    }

    public void setMaximumAuthenticationLifetime(Integer maximumAuthenticationLifetime) {
        this.maximumAuthenticationLifetime = maximumAuthenticationLifetime;
    }

    public String printClientMetadata() {
        init();
        return this.spMetadata;
    }

    private void disableSelfSignedCertValidation() {
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }

                    public void checkClientTrusted(
                            java.security.cert.X509Certificate[] certs, String authType) {
                    }

                    public void checkServerTrusted(
                            java.security.cert.X509Certificate[] certs, String authType) {
                    }
                }
        };

        // Install the all-trusting trust manager
        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(
                    new javax.net.ssl.HostnameVerifier() {

                        public boolean verify(String hostname,
                                              javax.net.ssl.SSLSession sslSession) {
                            return true;
                        }
                    });
        } catch (GeneralSecurityException e) {
        }
    }

    private String marshall(XMLObject xmlObject) throws Exception {
        System.setProperty("javax.xml.parsers.DocumentBuilderFactory",
                "org.apache.xerces.jaxp.DocumentBuilderFactoryImpl");

        MarshallerFactory marshallerFactory =
                org.opensaml.xml.Configuration.getMarshallerFactory();
        Marshaller marshaller = marshallerFactory.getMarshaller(xmlObject);
        Element element = marshaller.marshall(xmlObject);

        ByteArrayOutputStream byteArrayOutputStrm = new ByteArrayOutputStream();
        DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
        DOMImplementationLS impl = (DOMImplementationLS) registry.getDOMImplementation("LS");
        LSSerializer writer = impl.createLSSerializer();
        LSOutput output = impl.createLSOutput();
        output.setByteStream(byteArrayOutputStrm);
        writer.write(element, output);
        return byteArrayOutputStrm.toString();
    }

    public boolean isOauth2ExchangeEnabled() {
        return oauth2ExchangeEnabled;
    }

    public void setOauth2ExchangeEnabled(boolean oauth2ExchangeEnabled) {
        this.oauth2ExchangeEnabled = oauth2ExchangeEnabled;
    }

    public String getOauth2TokenEndpoint() {
        return oauth2TokenEndpoint;
    }

    public void setOauth2TokenEndpoint(String oauth2TokenEndpoint) {
        this.oauth2TokenEndpoint = oauth2TokenEndpoint;
    }

    public String getOauth2ClientID() {
        return oauth2ClientID;
    }

    public void setOauth2ClientID(String oauth2ClientID) {
        this.oauth2ClientID = oauth2ClientID;
    }

    public String getOauth2ClientSecret() {
        return oauth2ClientSecret;
    }

    public void setOauth2ClientSecret(String oauth2ClientSecret) {
        this.oauth2ClientSecret = oauth2ClientSecret;
    }

    public void setDevMode(boolean devMode) {
        this.devMode = devMode;
    }
}
