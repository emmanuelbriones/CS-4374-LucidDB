/*
// $Id$
// Farrago is an extensible data management system.
// Copyright (C) 2005 The Eigenbase Project
// Copyright (C) 2005 SQLstream, Inc.
// Copyright (C) 2005 Dynamo BI Corporation
// Portions Copyright (C) 2004 John V. Sichi
//
// This program is free software; you can redistribute it and/or modify it
// under the terms of the GNU General Public License as published by the Free
// Software Foundation; either version 2 of the License, or (at your option)
// any later version approved by The Eigenbase Project.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/
package net.sf.farrago.test;

import java.util.*;

import javax.security.auth.*;
import javax.security.auth.callback.*;
import javax.security.auth.login.*;
import javax.security.auth.spi.*;


/**
 * Mock login module for testing Farrago authentication.
 *
 * @author Oscar Gothberg
 * @version $Id$
 */

public class FarragoMockLoginModule
    implements LoginModule
{
    //~ Instance fields --------------------------------------------------------

    CallbackHandler callbackHandler;
    Subject subject;
    Map<String, ?> sharedState;
    Map<String, ?> options;

    List<FarragoMockCredential> tempCredentials;
    List<FarragoMockPrincipal> tempPrincipals;

    // authentication status
    boolean success;

    // config options
    boolean debug;

    //~ Constructors -----------------------------------------------------------

    public FarragoMockLoginModule()
    {
        success = false;
        debug = false;
        tempCredentials = new ArrayList<FarragoMockCredential>();
        tempPrincipals = new ArrayList<FarragoMockPrincipal>();
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * Called if LoginContext's required authentications failed.
     */
    public boolean abort()
        throws LoginException
    {
        success = false;
        tempPrincipals.clear();
        tempCredentials.clear();
        return true;
    }

    /**
     * Called if the LoginContext's required authentications succeeded.
     */
    public boolean commit()
        throws LoginException
    {
        if (success) {
            try {
                subject.getPrincipals().addAll(tempPrincipals);
                subject.getPublicCredentials().addAll(tempCredentials);

                tempPrincipals.clear();
                tempCredentials.clear();
            } catch (Exception ex) {
                LoginException le = new LoginException(ex.getMessage());
                le.initCause(ex);
                throw le;
            }
        } else {
            tempPrincipals.clear();
            tempCredentials.clear();
            return false;
        }

        return true;
    }

    /**
     * Initializes this LoginModule.
     */
    public void initialize(
        Subject subject,
        CallbackHandler callbackHandler,
        Map<String, ?> sharedState,
        Map<String, ?> options)
    {
        // save the initial state
        this.callbackHandler = callbackHandler;
        this.subject = subject;
        this.sharedState = sharedState;
        this.options = options;

        // initialize any configured options
        if (options.containsKey("debug")) {
            debug = "true".equalsIgnoreCase((String) options.get("debug"));
        }
    }

    /**
     * Attempts to log in a user.
     */
    public boolean login()
        throws LoginException
    {
        if (callbackHandler == null) {
            throw new LoginException("No callback handler available");
        }

        try {
            Callback [] callbacks =
                new Callback[] {
                    new NameCallback("Username: "),
                    new PasswordCallback("Password: ", false)
                };

            callbackHandler.handle(callbacks);
            String username = ((NameCallback) callbacks[0]).getName();
            String password =
                new String(((PasswordCallback) callbacks[1]).getPassword());
            ((PasswordCallback) callbacks[1]).clearPassword();

            // hardcoded accts
            if (username.equals("MockLoginModuleTestUser")) {
                // acct testuser requires a correct password
                success = password.equals("secret");
            } else {
                // all other usernames are just let through
                success = true;
            }

            if (success) {
                // dummy credential handling that does nothing
                FarragoMockCredential c = new FarragoMockCredential();
                c.setProperty("delete_perm", "0");
                c.setProperty("update_perm", "1");
                this.tempCredentials.add(c);
                this.tempPrincipals.add(new FarragoMockPrincipal(username));
            }
        } catch (Exception ex) {
            success = false;
            throw new LoginException(ex.getMessage());
        }

        return true;
    }

    /**
     * Logs out currently logged in subject.
     */
    public boolean logout()
        throws LoginException
    {
        tempPrincipals.clear();
        tempCredentials.clear();

        // remove principals
        for (
            FarragoMockPrincipal p
            : subject.getPrincipals(FarragoMockPrincipal.class))
        {
            subject.getPrincipals().remove(p);
        }

        // remove credentials
        for (
            FarragoMockCredential c
            : subject.getPublicCredentials(FarragoMockCredential.class))
        {
            subject.getPublicCredentials().remove(c);
        }

        return true;
    }
}

// End FarragoMockLoginModule.java
