/*
  Copyright 2014 Milinda Pathirage

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

package org.pac4j.saml.sso;

import org.joda.time.DateTime;
import org.opensaml.Configuration;
import org.opensaml.common.SAMLObjectBuilder;
import org.opensaml.common.binding.SAMLMessageContext;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.core.Issuer;
import org.opensaml.saml2.core.LogoutRequest;
import org.opensaml.saml2.core.NameID;
import org.opensaml.saml2.core.SessionIndex;
import org.opensaml.saml2.core.impl.IssuerBuilder;
import org.opensaml.saml2.core.impl.LogoutRequestBuilder;
import org.opensaml.saml2.core.impl.NameIDBuilder;
import org.opensaml.saml2.core.impl.SessionIndexBuilder;
import org.opensaml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.saml2.metadata.SPSSODescriptor;
import org.opensaml.saml2.metadata.SingleLogoutService;
import org.opensaml.xml.XMLObjectBuilderFactory;
import org.pac4j.core.profile.CommonProfile;

import java.util.Random;
import java.util.UUID;

public class Saml2SingleLogoutRequestBuilder {

    private final XMLObjectBuilderFactory builderFactory = Configuration.getBuilderFactory();


    public LogoutRequest build(SAMLMessageContext samlMessageContext, CommonProfile user){
        IDPSSODescriptor idpssoDescriptor = (IDPSSODescriptor) samlMessageContext.getPeerEntityRoleMetadata();

        LogoutRequestBuilder logoutRequestBuilder = new LogoutRequestBuilder();
        LogoutRequest logoutRequest = logoutRequestBuilder.buildObject();

        logoutRequest.setID(generateID());

        for(SingleLogoutService singleLogoutService : idpssoDescriptor.getSingleLogoutServices()){
            if(singleLogoutService.getBinding().equals(SAMLConstants.SAML2_POST_BINDING_URI)){
                logoutRequest.setDestination(singleLogoutService.getLocation());
            }
        }

        logoutRequest.setIssueInstant(new DateTime());

        IssuerBuilder issuerBuilder = new IssuerBuilder();
        Issuer issuer = issuerBuilder.buildObject();
        issuer.setValue(samlMessageContext.getLocalEntityId());

        logoutRequest.setIssuer(issuer);

        NameID nameId = new NameIDBuilder().buildObject();
        nameId.setFormat("urn:oasis:names:tc:SAML:2.0:nameid-format:entity");
        nameId.setValue(user.getUsername());

        logoutRequest.setNameID(nameId);

        SessionIndex sessionIndex = new SessionIndexBuilder().buildObject();
        sessionIndex.setSessionIndex(UUID.randomUUID().toString());
        logoutRequest.getSessionIndexes().add(sessionIndex);

        logoutRequest.setReason("Single Logout");

        return logoutRequest;
    }

    protected String generateID() {
        Random r = new Random();
        return '_' + Long.toString(Math.abs(r.nextLong()), 16) + Long.toString(Math.abs(r.nextLong()), 16);
    }
}
