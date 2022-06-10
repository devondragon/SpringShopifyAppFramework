import React from 'react';
import ReactDOM from 'react-dom';
import { Buffer } from 'buffer';
import { Provider, TitleBar } from '@shopify/app-bridge-react';
import enTranslations from '@shopify/polaris/locales/en.json';
import { AppProvider, Page, Card, Button, EmptyState } from '@shopify/polaris';
import { getSessionToken } from "@shopify/app-bridge-utils";
import ApolloClient from "apollo-client";
import { authenticatedFetch } from "@shopify/app-bridge-utils";
import createApp from "@shopify/app-bridge";
import { HttpLink } from "apollo-link-http";
import { InMemoryCache } from "apollo-cache-inmemory";
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

var host = getP('host');

const appApiHostname = 'https://shopifytest.ngrok.io';

console.log('host: ' + host);

const config = {
    apiKey: 'aeb97ee2e4b822c6664812bd902d3264',
    host: host,
    // forceRedirect: true
};

console.log('abount to call createApp()');
const app = createApp(config);
console.log('app created')

function MyApp() {
    console.log('MyApp called');
    authCheck();
    var token = getSessionToken(app);
    token.then(value => {
        console.log("token: " + value);
        window.sessionToken = value;

        loadProductList();
    });
    keepRetrievingToken(app);

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

function authCheck() {
    console.log('authCheck called');
    const url = appApiHostname + "/embedded-auth-check";
    fetch(url, {
        method: 'GET', // *GET, POST, PUT, DELETE, etc.
        mode: 'cors', // no-cors, *cors, same-origin
        cache: 'no-cache', // *default, no-cache, reload, force-cache, only-if-cached
        credentials: 'same-origin', // include, *same-origin, omit
        headers: {
            'Content-Type': 'application/json',
            //'Authorization': window.sessionToken
            'Authorization': host
            // 'Content-Type': 'application/x-www-form-urlencoded',
        },
        redirect: 'follow', // manual, *follow, error
        referrerPolicy: 'no-referrer', // no-referrer, *no-referrer-when-downgrade, origin, origin-when-cross-origin, same-origin, strict-origin, strict-origin-when-cross-origin, unsafe-url

    }).then(response => response.json())
        .then(
            data => {
                console.log("authCheck data: ", data);
                if (data.AuthCheckResponse.authenticated) {
                    console.log("authCheck success");
                } else {
                    console.log("authCheck failed");
                    const shopName = data.AuthCheckResponse.shopName;
                    const scopes = data.AuthCheckResponse.scopes;
                    const apiKey = config.apiKey;
                    const redirectUri = appApiHostname + "/oauth2/authorization/shopify";

                    const permissionUrl = `https://${shopName}/admin/oauth/authorize?client_id=${apiKey}&scope=${scopes}&redirect_uri=${redirectUri}`;

                    console.log("Redirecting to: " + permissionUrl);
                    // window.location.href = data.AuthCheckResponse.authRedirectURL;
                    // If the current window is the 'parent', change the URL by setting location.href
                    if (window.top == window.self) {
                        window.location.assign(permissionUrl);

                        // If the current window is the 'child', change the parent's URL with Shopify App Bridge's Redirect action
                    } else {
                        Redirect.create(app).dispatch(Redirect.Action.REMOTE, permissionUrl);
                    }
                }
            }
        )
        .catch(error => console.error(error));
}