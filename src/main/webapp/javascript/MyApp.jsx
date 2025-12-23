import React, { useEffect, useState } from 'react';
import ReactDOM from 'react-dom';
import { Provider, TitleBar } from '@shopify/app-bridge-react';
import enTranslations from '@shopify/polaris/locales/en.json';
import { AppProvider, Page, Card, Button, EmptyState, Spinner } from '@shopify/polaris';
import { getSessionToken } from "@shopify/app-bridge-utils";
import { authenticatedFetch } from "@shopify/app-bridge-utils";
import createApp from "@shopify/app-bridge";
import { regeneratorRuntime } from "regenerator-runtime"
import { Redirect } from '@shopify/app-bridge/actions';



function getP(n, url = window.location.href) {
    n = n.replace(/[\[\]]/g, '\\$&');
    var regex = new RegExp('[?&]' + n + '(=([^&#]*)|&|#|$)'),
        res = regex.exec(url);
    if (!res) return null;
    if (!res[2]) return '';
    return decodeURIComponent(res[2].replace(/\+/g, ' '));
}

// Get host parameter from URL (Base64 encoded shop info from Shopify)
const host = new URLSearchParams(location.search).get("host");

// Get app hostname from the current page origin, or use a configured value
// In production, this should be set via server-side templating or environment config
const appApiHostname = window.SHOPIFY_APP_HOSTNAME || window.location.origin;

console.log('host: ' + host);
console.log('appApiHostname: ' + appApiHostname);

// Get API key - in production this should be injected by the server
const apiKey = window.SHOPIFY_API_KEY || 'YOUR_API_KEY_HERE';

const config = {
    apiKey: apiKey,
    host: host,
    forceRedirect: true
};

console.log('about to call createApp()');
const app = createApp(config);
console.log('app created')

function MyApp() {
    const [isAuthenticated, setIsAuthenticated] = useState(null); // null = checking, true = authenticated, false = not authenticated
    const [isLoading, setIsLoading] = useState(true);

    useEffect(() => {
        // Perform auth check on component mount
        const initializeApp = async () => {
            try {
                // First check authentication status
                const authResult = await authCheck();

                if (authResult.authenticated) {
                    setIsAuthenticated(true);
                    // Only proceed with token retrieval if authenticated
                    const token = await getSessionToken(app);
                    console.log("token: " + token);
                    window.sessionToken = token;
                    loadProductList();
                    keepRetrievingToken(app);
                } else {
                    setIsAuthenticated(false);
                    // Auth check will handle redirect
                }
            } catch (error) {
                console.error("Error during app initialization:", error);
                setIsAuthenticated(false);
            } finally {
                setIsLoading(false);
            }
        };

        initializeApp();
    }, []);

    if (isLoading) {
        return (
            <Provider config={config}>
                <AppProvider i18n={enTranslations}>
                    <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh' }}>
                        <Spinner accessibilityLabel="Loading" size="large" />
                    </div>
                </AppProvider>
            </Provider>
        );
    }

    return (
        <Provider config={config}>
            <AppProvider i18n={enTranslations}>
                <TitleBar title="My page title" />
                <h2>This is a test app</h2>
            </AppProvider>
        </Provider>
    );
}

const SESSION_TOKEN_REFRESH_INTERVAL = 2000; // Request a new token every 2s
// const restLink = new RestLink({
//     uri: "https://shopifytest.ngrok.io/",
//     credentials: "same-origin",
//     fetch: authenticatedFetch(app), // ensures that all requests triggered by the ApolloClient are authenticated
// });


// const client = new ApolloClient({
//     // link: new HttpLink({
//     //     credentials: "same-origin",
//     //     fetch: authenticatedFetch(app), // ensures that all requests triggered by the ApolloClient are authenticated
//     // }),
//     link: restLink,
//     cache: new InMemoryCache(),
// });

function keepRetrievingToken(app) {
    setInterval(() => {
        const tokenP = getSessionToken(app);
        tokenP.then(value => {
            console.log("token: " + value);
            window.sessionToken = value;
            console.log("sessionToken: " + window.sessionToken);
        }).catch(error => {
            console.log("error: " + error);
        });
    }, SESSION_TOKEN_REFRESH_INTERVAL);
}

const root = document.createElement('div');
document.body.appendChild(root);
ReactDOM.render(<MyApp />, root);

// const query = gql`
//   query ProductList {
//     person @rest(type: "Product", path: "/product-list") {
//       name
//     }
//   }
// `;

// client.query({ query }).then(response => {
//     console.log(response.data.name);
// });

function loadProductList() {

    const url = appApiHostname + "/product-list";
    fetch(url, {
        method: 'GET', // *GET, POST, PUT, DELETE, etc.
        mode: 'cors', // no-cors, *cors, same-origin
        cache: 'no-cache', // *default, no-cache, reload, force-cache, only-if-cached
        credentials: 'same-origin', // include, *same-origin, omit
        headers: {
            'Content-Type': 'application/json',
            'Authorization': window.sessionToken
            // 'Content-Type': 'application/x-www-form-urlencoded',
        },
        redirect: 'follow', // manual, *follow, error
        referrerPolicy: 'no-referrer', // no-referrer, *no-referrer-when-downgrade, origin, origin-when-cross-origin, same-origin, strict-origin, strict-origin-when-cross-origin, unsafe-url

    }).then(response => response.json())
        .then(data => console.log(data))
        .catch(error => console.error(error));
}

/**
 * Checks if the app is authenticated for the current shop.
 * Returns a promise that resolves with the authentication status.
 * If not authenticated, initiates the OAuth redirect flow.
 *
 * @returns {Promise<{authenticated: boolean, shopName?: string}>}
 */
async function authCheck() {
    console.log('authCheck called');
    const url = appApiHostname + "/embedded-auth-check";

    try {
        const response = await fetch(url, {
            method: 'GET',
            cache: 'no-cache',
            headers: {
                'Content-Type': 'application/json',
                // Pass the host parameter for shop identification
                'Authorization': host || ''
            },
            redirect: 'follow',
            referrerPolicy: 'no-referrer',
        });

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        const data = await response.json();
        console.log("authCheck data: ", data);

        // Handle the response - it may be wrapped in AuthCheckResponse or not
        const authData = data.AuthCheckResponse || data;

        if (authData.authenticated) {
            console.log("authCheck success - app is authenticated");
            return { authenticated: true, shopName: authData.shopName };
        } else {
            console.log("authCheck: app not authenticated, initiating OAuth flow");
            const shopName = authData.shopName;
            const scopes = authData.scopes;
            const redirectUri = appApiHostname + "/login/oauth2/code/shopify";

            // Build the OAuth permission URL
            const permissionUrl = `https://${shopName}/admin/oauth/authorize?client_id=${apiKey}&scope=${scopes}&redirect_uri=${encodeURIComponent(redirectUri)}`;

            console.log("Redirecting to: " + permissionUrl);

            // Handle redirect based on whether we're in an iframe or not
            if (window.top === window.self) {
                // Top-level window - direct redirect
                window.location.assign(permissionUrl);
            } else {
                // Embedded in iframe - use App Bridge Redirect
                Redirect.create(app).dispatch(Redirect.Action.REMOTE, permissionUrl);
            }

            return { authenticated: false, shopName: shopName };
        }
    } catch (error) {
        console.error("authCheck error:", error);
        throw error;
    }
}